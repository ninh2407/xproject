package vn.tienlo.api.security.app;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.tienlo.api.generated.publicapi.model.AuthTokens;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.generated.publicapi.model.LoginRequest;
import vn.tienlo.api.security.Argon2PasswordConfiguration;
import vn.tienlo.api.security.LoginHmacKey;
import vn.tienlo.api.security.persistence.AccountRecord;
import vn.tienlo.api.security.persistence.AccountRepository;
import vn.tienlo.api.security.persistence.LoginIdempotencyRepository;
import vn.tienlo.api.security.persistence.LoginSessionRepository;

@Service
public class LoginService {
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    private final AccountRepository accountRepository;
    private final LoginSessionRepository sessionRepository;
    private final LoginIdempotencyRepository idempotencyRepository;
    private final PasswordEncoder passwordEncoder;
    private final Argon2PasswordConfiguration.DummyPasswordHash dummyPasswordHash;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final UuidV7Generator uuidV7Generator;
    private final LoginHmacKey loginHmacKey;
    private final Clock clock;

    public LoginService(
            AccountRepository accountRepository,
            LoginSessionRepository sessionRepository,
            LoginIdempotencyRepository idempotencyRepository,
            PasswordEncoder passwordEncoder,
            Argon2PasswordConfiguration.DummyPasswordHash dummyPasswordHash,
            AccessTokenIssuer accessTokenIssuer,
            RefreshTokenGenerator refreshTokenGenerator,
            UuidV7Generator uuidV7Generator,
            LoginHmacKey loginHmacKey,
            Clock clock) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = dummyPasswordHash;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.uuidV7Generator = uuidV7Generator;
        this.loginHmacKey = loginHmacKey;
        this.clock = clock;
    }

    /** API-R-002: persist the login result and session changes under one idempotent transaction. */
    @Transactional
    public LoginResult login(LoginRequest request, String idempotencyKey, UUID requestId, String ipAddress) {
        String normalizedEmail = request.getEmail().toLowerCase(Locale.ROOT);
        byte[] requestHash = requestHash(normalizedEmail, request.getPassword(), request.getDevice());
        Optional<LoginResult> existing = idempotencyRepository.begin(idempotencyKey, requestHash, requestId);
        if (existing.isPresent()) {
            return existing.get();
        }

        LoginResult result = authenticate(normalizedEmail, request, requestId, ipAddress);
        idempotencyRepository.complete(idempotencyKey, result);
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
            return errorResult(401, requestId, "INVALID_CREDENTIALS", "err.invalid_credentials");
        }

        AccountRecord account = accountResult.get();
        if (!passwordEncoder.matches(request.getPassword(), account.passwordHash())) {
            return errorResult(401, requestId, "INVALID_CREDENTIALS", "err.invalid_credentials");
        }
        if ("SUSPENDED".equals(account.status()) || "BANNED".equals(account.status())) {
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
            return errorResult(401, requestId, "INVALID_CREDENTIALS", "err.invalid_credentials");
        }

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
        accountRepository.updateLastLogin(account.id());

        AuthTokens tokens = new AuthTokens(
                accessToken,
                refreshToken.token(),
                AuthTokens.ExpiresInEnum.NUMBER_900);
        return new LoginResult(200, tokens);
    }

    private byte[] requestHash(String email, String password, String device) {
        byte[] key = loginHmacKey.value();
        byte[] requestBytes = null;
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            try (DataOutputStream data = new DataOutputStream(payload)) {
                writeString(data, email);
                writeString(data, password);
                writeString(data, device);
            }
            requestBytes = payload.toByteArray();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(requestBytes);
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalStateException("Could not fingerprint login request", exception);
        } finally {
            java.util.Arrays.fill(key, (byte) 0);
            if (requestBytes != null) {
                java.util.Arrays.fill(requestBytes, (byte) 0);
            }
        }
    }

    private static void writeString(DataOutputStream data, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        data.writeInt(bytes.length);
        data.write(bytes);
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
