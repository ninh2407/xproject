package vn.tienlo.api.security.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.tienlo.api.security.persistence.AccountRepository;
import vn.tienlo.api.security.persistence.AccountSessionRecord;
import vn.tienlo.api.security.persistence.LoginIdempotencyRepository;
import vn.tienlo.api.security.persistence.LoginSessionRepository;
import vn.tienlo.api.security.persistence.SessionRevocationOutboxRepository;
import vn.tienlo.api.generated.publicapi.model.AccountCreated;
import vn.tienlo.api.generated.publicapi.model.RegisterRequest;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final UUID REQUEST_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID ACCOUNT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID OLD_SESSION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
    private static final UUID NEXT_SESSION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174003");
    private static final UUID EVENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174004");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoginSessionRepository sessionRepository;

    @Mock
    private LoginIdempotencyRepository idempotencyRepository;

    @Mock
    private AuthRequestFingerprint requestFingerprint;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CommonPasswordPolicy commonPasswordPolicy;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @Mock
    private UuidV7Generator uuidV7Generator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SessionRevocationOutboxRepository revocationOutboxRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                accountRepository,
                sessionRepository,
                idempotencyRepository,
                requestFingerprint,
                passwordEncoder,
                commonPasswordPolicy,
                refreshTokenGenerator,
                accessTokenIssuer,
                uuidV7Generator,
                eventPublisher,
                revocationOutboxRepository,
                Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void API_AC_005_reusedRotatedRefreshTokenRevokesEveryAccountSession() {
        when(requestFingerprint.hash("/v1/auth/refresh", "refresh-r1")).thenReturn(new byte[32]);
        when(idempotencyRepository.begin(
                        eq("/v1/auth/refresh"), eq("refresh:reuse-r1"), any(byte[].class), eq(REQUEST_ID)))
                .thenReturn(Optional.empty());
        when(refreshTokenGenerator.hash("refresh-r1")).thenReturn(new byte[32]);
        when(sessionRepository.findByRefreshTokenHashForUpdate(any()))
                .thenReturn(Optional.of(new AccountSessionRecord(
                        OLD_SESSION_ID,
                        ACCOUNT_ID,
                        "ACTIVE",
                        null,
                        List.of("PLAYER"),
                        "desktop",
                        Instant.parse("2026-12-03T12:00:00Z"),
                        Instant.parse("2026-11-03T11:59:00Z"),
                        NEXT_SESSION_ID)));
        when(sessionRepository.revokeAll(ACCOUNT_ID)).thenReturn(List.of(OLD_SESSION_ID, NEXT_SESSION_ID));
        when(uuidV7Generator.next()).thenReturn(EVENT_ID);

        LoginResult result = authService.refresh(
                "refresh-r1",
                "refresh:reuse-r1",
                REQUEST_ID,
                "192.0.2.20");

        assertEquals(401, result.httpStatus());
        assertEquals("TOKEN_REVOKED", ((vn.tienlo.api.generated.publicapi.model.ErrorResponse) result.body())
                .getError()
                .getCode());
        verify(sessionRepository).revokeAll(ACCOUNT_ID);
        verify(revocationOutboxRepository).add(
                eq(EVENT_ID),
                eq(ACCOUNT_ID),
                eq(Instant.parse("2026-11-03T12:00:00Z")),
                eq(List.of(OLD_SESSION_ID, NEXT_SESSION_ID)));
        verify(eventPublisher)
                .publishEvent(new SessionRevokedEvent(EVENT_ID, ACCOUNT_ID, List.of(OLD_SESSION_ID, NEXT_SESSION_ID)));
        verify(idempotencyRepository).complete(eq("/v1/auth/refresh"), eq("refresh:reuse-r1"), eq(result));
    }

    @Test
    void API_R_002_registerCreatesAccountWithNormalizedEmailAndArgonPasswordHash() {
        when(requestFingerprint.hash(
                        "/v1/auth/register",
                        "player@example.com",
                        "safe password 123",
                        null,
                        "en",
                        "captcha"))
                .thenReturn(new byte[32]);
        when(idempotencyRepository.begin(
                        eq("/v1/auth/register"), eq("register:request-1"), any(byte[].class), eq(REQUEST_ID)))
                .thenReturn(Optional.empty());
        when(commonPasswordPolicy.isAcceptable("safe password 123")).thenReturn(true);
        when(passwordEncoder.encode("safe password 123")).thenReturn("argon2id-hash");
        when(uuidV7Generator.next()).thenReturn(ACCOUNT_ID);
        when(accountRepository.create(
                        ACCOUNT_ID, "player@example.com", "argon2id-hash", null, "en"))
                .thenReturn(Optional.of(ACCOUNT_ID));

        LoginResult result = authService.register(
                new RegisterRequest("PLAYER@example.com", "safe password 123", "en", "captcha"),
                "register:request-1",
                REQUEST_ID);

        assertEquals(201, result.httpStatus());
        assertEquals(ACCOUNT_ID, ((AccountCreated) result.body()).getAccountId());
        verify(accountRepository).create(ACCOUNT_ID, "player@example.com", "argon2id-hash", null, "en");
        verify(idempotencyRepository).complete(eq("/v1/auth/register"), eq("register:request-1"), eq(result));
    }

    @Test
    void API_R_002_refreshRotatesActiveRefreshToken() {
        Instant now = Instant.parse("2026-11-03T12:00:00Z");
        when(requestFingerprint.hash("/v1/auth/refresh", "refresh-r1")).thenReturn(new byte[32]);
        when(idempotencyRepository.begin(
                        eq("/v1/auth/refresh"), eq("refresh:rotate-r1"), any(byte[].class), eq(REQUEST_ID)))
                .thenReturn(Optional.empty());
        when(refreshTokenGenerator.hash("refresh-r1")).thenReturn(new byte[32]);
        when(sessionRepository.findByRefreshTokenHashForUpdate(any()))
                .thenReturn(Optional.of(new AccountSessionRecord(
                        OLD_SESSION_ID,
                        ACCOUNT_ID,
                        "ACTIVE",
                        null,
                        List.of("PLAYER"),
                        "desktop",
                        Instant.parse("2026-12-03T12:00:00Z"),
                        null,
                        null)));
        when(uuidV7Generator.next()).thenReturn(NEXT_SESSION_ID, EVENT_ID);
        when(refreshTokenGenerator.generate())
                .thenReturn(new RefreshTokenGenerator.GeneratedRefreshToken("refresh-r2", new byte[32]));
        when(accessTokenIssuer.issue(ACCOUNT_ID, NEXT_SESSION_ID, List.of("PLAYER"))).thenReturn("access-r2");

        LoginResult result = authService.refresh(
                "refresh-r1",
                "refresh:rotate-r1",
                REQUEST_ID,
                "192.0.2.21");

        assertEquals(200, result.httpStatus());
        AuthSessionTokens tokens = (AuthSessionTokens) result.body();
        assertEquals("refresh-r2", tokens.refreshToken());
        assertEquals("access-r2", tokens.accessToken());
        verify(sessionRepository).rotate(
                eq(OLD_SESSION_ID),
                eq(NEXT_SESSION_ID),
                eq(ACCOUNT_ID),
                any(byte[].class),
                eq("desktop"),
                eq("192.0.2.21"),
                eq(now.plusSeconds(30 * 24 * 60 * 60L)));
        verify(revocationOutboxRepository).add(
                EVENT_ID,
                ACCOUNT_ID,
                now,
                List.of(OLD_SESSION_ID));
    }
}
