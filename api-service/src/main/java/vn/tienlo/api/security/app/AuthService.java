package vn.tienlo.api.security.app;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tienlo.api.generated.publicapi.model.AccountCreated;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.generated.publicapi.model.RegisterRequest;
import vn.tienlo.api.security.persistence.AccountRepository;
import vn.tienlo.api.security.persistence.AccountSessionRecord;
import vn.tienlo.api.security.persistence.LoginIdempotencyRepository;
import vn.tienlo.api.security.persistence.LoginSessionRepository;
import vn.tienlo.api.security.persistence.SessionRevocationOutboxRepository;

@Service
public class AuthService {
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    private final AccountRepository accountRepository;
    private final LoginSessionRepository sessionRepository;
    private final LoginIdempotencyRepository idempotencyRepository;
    private final AuthRequestFingerprint requestFingerprint;
    private final PasswordEncoder passwordEncoder;
    private final CommonPasswordPolicy commonPasswordPolicy;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final AccessTokenIssuer accessTokenIssuer;
    private final UuidV7Generator uuidV7Generator;
    private final ApplicationEventPublisher eventPublisher;
    private final SessionRevocationOutboxRepository revocationOutboxRepository;
    private final Clock clock;

    public AuthService(
            AccountRepository accountRepository,
            LoginSessionRepository sessionRepository,
            LoginIdempotencyRepository idempotencyRepository,
            AuthRequestFingerprint requestFingerprint,
            PasswordEncoder passwordEncoder,
            CommonPasswordPolicy commonPasswordPolicy,
            RefreshTokenGenerator refreshTokenGenerator,
            AccessTokenIssuer accessTokenIssuer,
            UuidV7Generator uuidV7Generator,
            ApplicationEventPublisher eventPublisher,
            SessionRevocationOutboxRepository revocationOutboxRepository,
            Clock clock) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.requestFingerprint = requestFingerprint;
        this.passwordEncoder = passwordEncoder;
        this.commonPasswordPolicy = commonPasswordPolicy;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.accessTokenIssuer = accessTokenIssuer;
        this.uuidV7Generator = uuidV7Generator;
        this.eventPublisher = eventPublisher;
        this.revocationOutboxRepository = revocationOutboxRepository;
        this.clock = clock;
    }

    /** SEC-R-010, API-R-002: create an account only after the CAPTCHA and IP checks at the HTTP boundary. */
    @Transactional
    public LoginResult register(RegisterRequest request, String idempotencyKey, UUID requestId) {
        String normalizedEmail = request.getEmail().toLowerCase(Locale.ROOT);
        Optional<LoginResult> existing = idempotencyRepository.begin(
                "/v1/auth/register",
                idempotencyKey,
                requestFingerprint.hash(
                        "/v1/auth/register",
                        normalizedEmail,
                        request.getPassword(),
                        request.getDisplayHandle(),
                        request.getLocale(),
                        request.getCaptchaToken()),
                requestId);
        if (existing.isPresent()) {
            return existing.get();
        }

        if (!commonPasswordPolicy.isAcceptable(request.getPassword())) {
            LoginResult result = errorResult(
                    400,
                    requestId,
                    "VALIDATION",
                    "err.validation",
                    "Password is too common");
            idempotencyRepository.complete("/v1/auth/register", idempotencyKey, result);
            return result;
        }

        UUID accountId = uuidV7Generator.next();
        String passwordHash = passwordEncoder.encode(request.getPassword());
        Optional<UUID> inserted = accountRepository.create(
                accountId,
                normalizedEmail,
                passwordHash,
                request.getDisplayHandle(),
                request.getLocale());
        LoginResult result = inserted
                .<LoginResult>map(id -> new LoginResult(201, new AccountCreated(id)))
                .orElseGet(() -> errorResult(
                        409,
                        requestId,
                        "UNIQUE_TAKEN",
                        "err.unique_taken",
                        "Email or display handle is unavailable"));
        idempotencyRepository.complete("/v1/auth/register", idempotencyKey, result);
        return result;
    }

    /** SEC-R-015, API-AC-005: atomically rotate a live token or revoke every account session on reuse. */
    @Transactional
    public LoginResult refresh(
            String refreshToken,
            String idempotencyKey,
            UUID requestId,
            String remoteIp) {
        Optional<LoginResult> existing = idempotencyRepository.begin(
                "/v1/auth/refresh",
                idempotencyKey,
                requestFingerprint.hash("/v1/auth/refresh", refreshToken),
                requestId);
        if (existing.isPresent()) {
            if (existing.get().httpStatus() == 200
                    || existing.get().httpStatus() == 401
                    || existing.get().httpStatus() == 403) {
                byte[] replayedHash = refreshTokenGenerator.hash(refreshToken);
                if (replayedHash.length != 0) {
                    sessionRepository.findByRefreshTokenHashForUpdate(replayedHash).ifPresent(session -> {
                        if (session.replacedBySessionId() != null) {
                            if (existing.get().httpStatus() == 200) {
                                publishRevoked(session.accountId(), List.of(session.sessionId()));
                            } else {
                                revokeAll(session.accountId());
                            }
                        } else if (existing.get().httpStatus() == 403) {
                            revokeAll(session.accountId());
                        }
                    });
                }
            }
            return existing.get();
        }

        byte[] tokenHash = refreshTokenGenerator.hash(refreshToken);
        Optional<AccountSessionRecord> session = tokenHash.length == 0
                ? Optional.empty()
                : sessionRepository.findByRefreshTokenHashForUpdate(tokenHash);
        LoginResult result;
        if (session.isEmpty()) {
            result = errorResult(401, requestId, "TOKEN_REVOKED", "err.token_revoked", "Refresh token is invalid");
        } else {
            AccountSessionRecord current = session.get();
            if (current.replacedBySessionId() != null) {
                revokeAll(current.accountId());
                result = errorResult(
                        401,
                        requestId,
                        "TOKEN_REVOKED",
                        "err.token_revoked",
                        "Refresh token has already been used");
            } else if (current.revokedAt() != null || !current.expiresAt().isAfter(clock.instant())) {
                result = errorResult(
                        401,
                        requestId,
                        "TOKEN_REVOKED",
                        "err.token_revoked",
                        "Refresh token is invalid");
            } else if ("SUSPENDED".equals(current.accountStatus()) || "BANNED".equals(current.accountStatus())) {
                revokeAll(current.accountId());
                result = suspendedResult(requestId, current.suspendedUntil());
            } else if (!"ACTIVE".equals(current.accountStatus())) {
                revokeAll(current.accountId());
                result = errorResult(
                        401,
                        requestId,
                        "TOKEN_REVOKED",
                        "err.token_revoked",
                        "Refresh token is invalid");
            } else {
                Instant now = clock.instant();
                UUID nextSessionId = uuidV7Generator.next();
                RefreshTokenGenerator.GeneratedRefreshToken nextToken = refreshTokenGenerator.generate();
                sessionRepository.rotate(
                        current.sessionId(),
                        nextSessionId,
                        current.accountId(),
                        nextToken.hash(),
                        current.device(),
                        remoteIp,
                        now.plus(REFRESH_TOKEN_TTL));
                publishRevoked(current.accountId(), List.of(current.sessionId()));
                String accessToken = accessTokenIssuer.issue(current.accountId(), nextSessionId, current.roles());
                result = new LoginResult(
                        200,
                        new AuthSessionTokens(accessToken, nextToken.token(), 900));
            }
        }
        idempotencyRepository.complete("/v1/auth/refresh", idempotencyKey, result);
        return result;
    }

    /** SEC-R-018: a logout request is idempotent and only revokes the bearer session named by the token. */
    @Transactional
    public LoginResult logout(
            String refreshToken,
            String idempotencyKey,
            UUID requestId,
            UUID accountId,
            UUID sessionId) {
        Optional<LoginResult> existing = idempotencyRepository.begin(
                "/v1/auth/logout",
                idempotencyKey,
                requestFingerprint.hash(
                        "/v1/auth/logout", accountId.toString(), sessionId.toString(), refreshToken),
                requestId);
        if (existing.isPresent()) {
            if (existing.get().httpStatus() == 204) {
                byte[] replayedHash = refreshTokenGenerator.hash(refreshToken);
                if (replayedHash.length != 0) {
                    sessionRepository.findByRefreshTokenHashForUpdate(replayedHash)
                            .filter(session -> session.accountId().equals(accountId)
                                    && session.sessionId().equals(sessionId))
                            .ifPresent(session -> publishRevoked(accountId, List.of(sessionId)));
                }
            }
            return existing.get();
        }

        byte[] tokenHash = refreshTokenGenerator.hash(refreshToken);
        Optional<AccountSessionRecord> session = tokenHash.length == 0
                ? Optional.empty()
                : sessionRepository.findByRefreshTokenHashForUpdate(tokenHash);
        LoginResult result;
        if (session.isPresent()
                && session.get().accountId().equals(accountId)
                && session.get().sessionId().equals(sessionId)) {
            sessionRepository.revoke(sessionId, accountId);
            publishRevoked(accountId, List.of(sessionId));
            result = new LoginResult(204, null);
        } else {
            result = errorResult(
                    401,
                    requestId,
                    "TOKEN_REVOKED",
                    "err.token_revoked",
                    "Refresh token does not match the authenticated session");
        }
        idempotencyRepository.complete("/v1/auth/logout", idempotencyKey, result);
        return result;
    }

    /** SEC-R-018: revoke all known sessions and publish their ids for access-token rejection. */
    @Transactional
    public LoginResult logoutAll(
            String idempotencyKey,
            UUID requestId,
            UUID accountId,
            UUID sessionId) {
        Optional<LoginResult> existing = idempotencyRepository.begin(
                "/v1/auth/logout-all",
                idempotencyKey,
                requestFingerprint.hash(
                        "/v1/auth/logout-all", accountId.toString(), sessionId.toString()),
                requestId);
        if (existing.isPresent()) {
            if (existing.get().httpStatus() == 204) {
                revokeAll(accountId);
            }
            return existing.get();
        }

        revokeAll(accountId);
        LoginResult result = new LoginResult(204, null);
        idempotencyRepository.complete("/v1/auth/logout-all", idempotencyKey, result);
        return result;
    }

    private void revokeAll(UUID accountId) {
        publishRevoked(accountId, sessionRepository.revokeAll(accountId));
    }

    private void publishRevoked(UUID accountId, List<UUID> sessionIds) {
        if (!sessionIds.isEmpty()) {
            UUID eventId = uuidV7Generator.next();
            Instant occurredAt = clock.instant();
            revocationOutboxRepository.add(eventId, accountId, occurredAt, sessionIds);
            eventPublisher.publishEvent(new SessionRevokedEvent(eventId, accountId, sessionIds));
        }
    }

    private static LoginResult suspendedResult(UUID requestId, Instant suspendedUntil) {
        Map<String, Object> details = new HashMap<>();
        details.put("until", suspendedUntil == null ? null : suspendedUntil.toString());
        ErrorResponse body = new ErrorResponse(
                new ErrorResponseError(
                        "ACCOUNT_SUSPENDED",
                        "Account is suspended",
                        "err.account_suspended")
                        .details(details),
                requestId);
        return new LoginResult(403, body);
    }

    private static LoginResult errorResult(
            int status,
            UUID requestId,
            String code,
            String messageKey,
            String message) {
        ErrorResponse body = new ErrorResponse(
                new ErrorResponseError(code, message, messageKey),
                requestId);
        return new LoginResult(status, body);
    }
}
