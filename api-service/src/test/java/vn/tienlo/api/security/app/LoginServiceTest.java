package vn.tienlo.api.security.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.generated.publicapi.model.LoginRequest;
import vn.tienlo.api.security.Argon2PasswordConfiguration;
import vn.tienlo.api.security.LoginHmacKey;
import vn.tienlo.api.security.persistence.AccountRepository;
import vn.tienlo.api.security.persistence.AccountRecord;
import vn.tienlo.api.security.persistence.LoginIdempotencyRepository;
import vn.tienlo.api.security.persistence.LoginAttemptRecorder;
import vn.tienlo.api.security.persistence.LoginSessionRepository;
import vn.tienlo.api.security.persistence.SessionRevocationOutboxRepository;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    private static final UUID REQUEST_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID ACCOUNT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID SESSION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
    private static final UUID PREVIOUS_SESSION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174003");
    private static final UUID EVENT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174004");
    private static final Instant NOW = Instant.parse("2026-11-03T12:00:00Z");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LoginSessionRepository sessionRepository;

    @Mock
    private LoginIdempotencyRepository idempotencyRepository;

    @Mock
    private LoginAttemptRecorder loginAttemptRecorder;

    private AuthRequestFingerprint requestFingerprint;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SessionRevocationOutboxRepository revocationOutboxRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @Mock
    private RefreshTokenGenerator refreshTokenGenerator;

    @Mock
    private UuidV7Generator uuidV7Generator;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        requestFingerprint = new AuthRequestFingerprint(new LoginHmacKey(new byte[32]));
        loginService = new LoginService(
                accountRepository,
                sessionRepository,
                idempotencyRepository,
                loginAttemptRecorder,
                requestFingerprint,
                eventPublisher,
                revocationOutboxRepository,
                passwordEncoder,
                new Argon2PasswordConfiguration.DummyPasswordHash("dummy-hash"),
                accessTokenIssuer,
                refreshTokenGenerator,
                uuidV7Generator,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void API_R_030_activeAccountGetsAccessAndRefreshTokensAndPersistedSession() {
        when(idempotencyRepository.begin(
                        eq("/v1/auth/login"), eq("login:request-1"), any(byte[].class), eq(REQUEST_ID)))
                .thenReturn(Optional.empty());
        when(accountRepository.findByLoginEmail("player@example.com"))
                .thenReturn(Optional.of(new AccountRecord(ACCOUNT_ID, "argon-hash", "ACTIVE", null, List.of("PLAYER"))));
        when(passwordEncoder.matches("correct horse battery staple", "argon-hash")).thenReturn(true);
        when(uuidV7Generator.next()).thenReturn(SESSION_ID);
        when(refreshTokenGenerator.generate())
                .thenReturn(new RefreshTokenGenerator.GeneratedRefreshToken("opaque-refresh", new byte[32]));
        when(accessTokenIssuer.issue(ACCOUNT_ID, SESSION_ID, List.of("PLAYER"))).thenReturn("signed-jwt");

        LoginResult result = loginService.login(
                new LoginRequest("PLAYER@example.com", "correct horse battery staple", "desktop"),
                "login:request-1",
                REQUEST_ID,
                "192.0.2.10");

        assertEquals(200, result.httpStatus());
        AuthSessionTokens tokens = (AuthSessionTokens) result.body();
        assertEquals("signed-jwt", tokens.accessToken());
        assertEquals("opaque-refresh", tokens.refreshToken());
        assertEquals(900, tokens.expiresIn());
        verify(sessionRepository).create(
                eq(SESSION_ID),
                eq(ACCOUNT_ID),
                any(byte[].class),
                eq("desktop"),
                eq("192.0.2.10"),
                eq(NOW.plusSeconds(30 * 24 * 60 * 60L)));
        verify(accountRepository).updateLastLogin(ACCOUNT_ID);
        verify(loginAttemptRecorder).record(ACCOUNT_ID, "192.0.2.10", true);
        verify(idempotencyRepository).complete(eq("/v1/auth/login"), eq("login:request-1"), same(result));
    }

    @Test
    void API_R_030_unknownAccountReceivesGenericInvalidCredentialsWithoutSession() {
        when(idempotencyRepository.begin(
                        eq("/v1/auth/login"), eq("login:request-2"), any(byte[].class), eq(REQUEST_ID)))
                .thenReturn(Optional.empty());
        when(accountRepository.findByLoginEmail("missing@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("wrong password", "dummy-hash")).thenReturn(false);

        LoginResult result = loginService.login(
                new LoginRequest("missing@example.com", "wrong password", "desktop"),
                "login:request-2",
                REQUEST_ID,
                "192.0.2.11");

        assertEquals(401, result.httpStatus());
        assertEquals("INVALID_CREDENTIALS", ((ErrorResponse) result.body()).getError().getCode());
        verify(loginAttemptRecorder).record(null, "192.0.2.11", false);
        verify(sessionRepository, never()).create(any(), any(), any(), any(), any(), any());
        verify(idempotencyRepository).complete(eq("/v1/auth/login"), eq("login:request-2"), same(result));
    }

    @Test
    void SEC_R_017_newLoginRevokesPriorSessionsAndPublishesRevocation() {
        when(idempotencyRepository.begin(
                        eq("/v1/auth/login"), eq("login:new-device"), any(byte[].class), eq(REQUEST_ID)))
                .thenReturn(Optional.empty());
        when(accountRepository.findByLoginEmail("player@example.com"))
                .thenReturn(Optional.of(new AccountRecord(ACCOUNT_ID, "argon-hash", "ACTIVE", null, List.of("PLAYER"))));
        when(passwordEncoder.matches("correct horse battery staple", "argon-hash")).thenReturn(true);
        when(uuidV7Generator.next()).thenReturn(SESSION_ID, EVENT_ID);
        when(refreshTokenGenerator.generate())
                .thenReturn(new RefreshTokenGenerator.GeneratedRefreshToken("opaque-refresh", new byte[32]));
        when(accessTokenIssuer.issue(ACCOUNT_ID, SESSION_ID, List.of("PLAYER"))).thenReturn("signed-jwt");
        when(sessionRepository.revokeOthers(ACCOUNT_ID, SESSION_ID)).thenReturn(List.of(PREVIOUS_SESSION_ID));

        LoginResult result = loginService.login(
                new LoginRequest("player@example.com", "correct horse battery staple", "desktop"),
                "login:new-device",
                REQUEST_ID,
                "192.0.2.15");

        assertEquals(200, result.httpStatus());
        verify(revocationOutboxRepository).add(
                EVENT_ID,
                ACCOUNT_ID,
                NOW,
                List.of(PREVIOUS_SESSION_ID));
        verify(eventPublisher)
                .publishEvent(new SessionRevokedEvent(EVENT_ID, ACCOUNT_ID, List.of(PREVIOUS_SESSION_ID)));
    }

    @Test
    void API_R_031_suspendedAccountReturnsItsConfiguredDeadline() {
        Instant suspendedUntil = NOW.plusSeconds(3600);
        when(idempotencyRepository.begin(
                        eq("/v1/auth/login"), eq("login:request-4"), any(byte[].class), eq(REQUEST_ID)))
                .thenReturn(Optional.empty());
        when(accountRepository.findByLoginEmail("player@example.com"))
                .thenReturn(Optional.of(new AccountRecord(
                        ACCOUNT_ID, "argon-hash", "SUSPENDED", suspendedUntil, List.of("PLAYER"))));
        when(passwordEncoder.matches("correct horse battery staple", "argon-hash")).thenReturn(true);

        LoginResult result = loginService.login(
                new LoginRequest("player@example.com", "correct horse battery staple", "desktop"),
                "login:request-4",
                REQUEST_ID,
                "192.0.2.13");

        assertEquals(403, result.httpStatus());
        ErrorResponseError error = ((ErrorResponse) result.body()).getError();
        assertEquals("ACCOUNT_SUSPENDED", error.getCode());
        assertEquals(Map.of("until", suspendedUntil.toString()), error.getDetails());
        verify(loginAttemptRecorder).record(ACCOUNT_ID, "192.0.2.13", false);
        verify(sessionRepository, never()).create(any(), any(), any(), any(), any(), any());
    }

    @Test
    void API_R_031_permanentBanReturnsNullDeadline() {
        when(idempotencyRepository.begin(
                        eq("/v1/auth/login"), eq("login:request-5"), any(byte[].class), eq(REQUEST_ID)))
                .thenReturn(Optional.empty());
        when(accountRepository.findByLoginEmail("player@example.com"))
                .thenReturn(Optional.of(new AccountRecord(
                        ACCOUNT_ID, "argon-hash", "BANNED", null, List.of("PLAYER"))));
        when(passwordEncoder.matches("correct horse battery staple", "argon-hash")).thenReturn(true);

        LoginResult result = loginService.login(
                new LoginRequest("player@example.com", "correct horse battery staple", "desktop"),
                "login:request-5",
                REQUEST_ID,
                "192.0.2.14");

        assertEquals(403, result.httpStatus());
        ErrorResponseError error = ((ErrorResponse) result.body()).getError();
        assertEquals("ACCOUNT_SUSPENDED", error.getCode());
        assertEquals(java.util.Collections.singletonMap("until", null), error.getDetails());
        verify(sessionRepository, never()).create(any(), any(), any(), any(), any(), any());
    }

    @Test
    void API_R_002_replaysAnIdempotentLoginResultWithoutCreatingAnotherSession() {
        LoginResult cachedResult = new LoginResult(401, new ErrorResponse());
        when(idempotencyRepository.begin(
                        eq("/v1/auth/login"), eq("login:request-3"), any(byte[].class), eq(REQUEST_ID)))
                .thenReturn(Optional.of(cachedResult));

        LoginResult result = loginService.login(
                new LoginRequest("player@example.com", "any password", "desktop"),
                "login:request-3",
                REQUEST_ID,
                "192.0.2.12");

        assertEquals(cachedResult, result);
        verify(accountRepository, never()).findByLoginEmail(anyString());
        verify(sessionRepository, never()).create(any(), any(), any(), any(), any(), any());
        verify(idempotencyRepository, never()).complete(anyString(), anyString(), any());
    }
}
