package vn.tienlo.api.security.app;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.generated.publicapi.model.LoginRequest;
import vn.tienlo.api.security.Argon2PasswordConfiguration;
import vn.tienlo.api.security.persistence.AccountRecord;
import vn.tienlo.api.security.persistence.AccountRepository;
import vn.tienlo.api.security.persistence.LoginAttemptRecorder;
import vn.tienlo.api.security.persistence.LoginIdempotencyRepository;
import vn.tienlo.api.security.persistence.LoginSessionRepository;
import vn.tienlo.api.security.persistence.SessionRevocationOutboxRepository;

@Service
public class LoginService {
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    private final AccountRepository accountRepository;
    private final LoginSessionRepository sessionRepository;
    private final LoginIdempotencyRepository idempotencyRepository;
    private final LoginAttemptRecorder loginAttemptRecorder;
    private final AuthRequestFingerprint requestFingerprint;
    private final ApplicationEventPublisher eventPublisher;
    private final SessionRevocationOutboxRepository revocationOutboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final Argon2PasswordConfiguration.DummyPasswordHash dummyPasswordHash;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final UuidV7Generator uuidV7Generator;
    private final Clock clock;

    public LoginService(
            AccountRepository accountRepository,
            LoginSessionRepository sessionRepository,
            LoginIdempotencyRepository idempotencyRepository,
            LoginAttemptRecorder loginAttemptRecorder,
            AuthRequestFingerprint requestFingerprint,
            ApplicationEventPublisher eventPublisher,
            SessionRevocationOutboxRepository revocationOutboxRepository,
            PasswordEncoder passwordEncoder,
            Argon2PasswordConfiguration.DummyPasswordHash dummyPasswordHash,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenGenerator refreshTokenGenerator,
            UuidV7Generator uuidV7Generator,
            Clock clock) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.loginAttemptRecorder = loginAttemptRecorder;
        this.requestFingerprint = requestFingerprint;
        this.eventPublisher = eventPublisher;
        this.revocationOutboxRepository = revocationOutboxRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = dummyPasswordHash;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.uuidV7Generator = uuidV7Generator;
        this.clock = clock;
    }

    /** API-R-002: persist the login result and session changes under one idempotent transaction. */
    @Transactional
    public LoginResult login(LoginRequest request, String idempotencyKey, UUID requestId, String ipAddress) {
        String normalizedEmail = request.getEmail().toLowerCase(Locale.ROOT);
        byte[] requestHash = requestFingerprint.hash(
                "/v1/auth/login",
                normalizedEmail,
                request.getPassword(),
                request.getDevice(),
                request.getCaptchaToken());
        Optional<LoginResult> existing =
                idempotencyRepository.begin("/v1/auth/login", idempotencyKey, requestHash, requestId);
        if (existing.isPresent()) {
            return existing.get();
        }

        LoginResult result = authenticate(normalizedEmail, request, requestId, ipAddress);
        idempotencyRepository.complete("/v1/auth/login", idempotencyKey, result);
        return result;
    }

    private LoginResult authenticate(
            String normalizedEmail,
            LoginRequest request,
            UUID requestId,
            String ipAddress) {
        Optional<AccountRecord> accountResult = accountRepository.findByLoginEmail(normalizedEmail);
        if (accountResult.isEmpty()) {
            passwordEncoder.matches(request.getPassword(), dummyPasswordHash.value());
            recordAttempt(normalizedEmail, null, ipAddress, false);
            return errorResult(401, requestId, "INVALID_CREDENTIALS", "err.invalid_credentials");
        }

        AccountRecord account = accountResult.get();
        if (!passwordEncoder.matches(request.getPassword(), account.passwordHash())) {
            recordAttempt(normalizedEmail, account.id(), ipAddress, false);
            return errorResult(401, requestId, "INVALID_CREDENTIALS", "err.invalid_credentials");
        }
        if ("SUSPENDED".equals(account.status()) || "BANNED".equals(account.status())) {
            recordAttempt(normalizedEmail, account.id(), ipAddress, false);
            Map<String, Object> details = new HashMap<>();
            details.put(
                    "until",
                    account.suspendedUntil() == null ? null : account.suspendedUntil().toString());
            ErrorResponse body = new ErrorResponse(
                    new ErrorResponseError(
                            "ACCOUNT_SUSPENDED",
                            "Account is suspended",
                            "err.account_suspended")
                            .details(details),
                    requestId);
            return new LoginResult(403, body);
        }
        if (!"ACTIVE".equals(account.status())) {
            recordAttempt(normalizedEmail, account.id(), ipAddress, false);
            return errorResult(401, requestId, "INVALID_CREDENTIALS", "err.invalid_credentials");
        }

        recordAttempt(normalizedEmail, account.id(), ipAddress, true);
        Instant now = clock.instant();
        UUID sessionId = uuidV7Generator.next();
        RefreshTokenGenerator.GeneratedRefreshToken refreshToken = refreshTokenGenerator.generate();
        Instant refreshExpiresAt = now.plus(REFRESH_TOKEN_TTL);
        String accessToken = accessTokenIssuer.issue(account.id(), sessionId, account.roles());

        sessionRepository.create(
                sessionId,
                account.id(),
                refreshToken.hash(),
                request.getDevice(),
                ipAddress,
                refreshExpiresAt);
        List<UUID> previousSessions = sessionRepository.revokeOthers(account.id(), sessionId);
        if (!previousSessions.isEmpty()) {
            UUID eventId = uuidV7Generator.next();
            Instant occurredAt = clock.instant();
            revocationOutboxRepository.add(eventId, account.id(), occurredAt, previousSessions);
            eventPublisher.publishEvent(new SessionRevokedEvent(eventId, account.id(), previousSessions));
        }
        accountRepository.updateLastLogin(account.id());

        AuthSessionTokens tokens = new AuthSessionTokens(accessToken, refreshToken.token(), 900);
        return new LoginResult(200, tokens);
    }

    private void recordAttempt(String email, UUID accountId, String ipAddress, boolean succeeded) {
        loginAttemptRecorder.record(accountId, ipAddress, succeeded);
        eventPublisher.publishEvent(new LoginAttemptOutcomeEvent(email, ipAddress, succeeded));
    }

    private static LoginResult errorResult(
            int status,
            UUID requestId,
            String code,
            String messageKey) {
        ErrorResponse body = new ErrorResponse(
                new ErrorResponseError(code, "Email or password is incorrect", messageKey),
                requestId);
        return new LoginResult(status, body);
    }
}
