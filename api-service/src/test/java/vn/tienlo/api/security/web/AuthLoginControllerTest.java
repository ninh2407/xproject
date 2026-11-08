package vn.tienlo.api.security.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.tienlo.api.security.app.LoginResult;
import vn.tienlo.api.security.app.LoginService;
import vn.tienlo.api.security.app.AuthService;
import vn.tienlo.api.security.app.AuthRateLimitService;
import vn.tienlo.api.security.app.CaptchaVerifier;
import vn.tienlo.api.security.app.AuthSessionTokens;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.platform.web.ApiErrorHandler;

@ExtendWith(MockitoExtension.class)
class AuthLoginControllerTest {
    private static final UUID REQUEST_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Mock
    private LoginService loginService;

    @Mock
    private AuthService authService;

    @Mock
    private AuthRateLimitService rateLimitService;

    @Mock
    private CaptchaVerifier captchaVerifier;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AuthLoginController(loginService, authService, rateLimitService, captchaVerifier))
                .setControllerAdvice(new AuthLoginErrorHandler(), new ApiErrorHandler())
                .build();
    }

    @Test
    void API_R_001_loginReturnsContractTokensAndPreventsCaching() throws Exception {
        when(rateLimitService.requiresCaptcha("player@example.com")).thenReturn(false);
        when(loginService.login(any(), eq("login:request-1"), eq(REQUEST_ID), eq("127.0.0.1")))
                .thenReturn(new LoginResult(200, new AuthSessionTokens("access", "refresh", 900)));

        mockMvc.perform(post("/v1/auth/login")
                        .header("X-Request-Id", REQUEST_ID)
                        .header("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
                        .header("Idempotency-Key", "login:request-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player@example.com",
                                  "password": "correct horse battery staple",
                                  "device": "desktop"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string(
                        "Set-Cookie",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("__Secure-xproject_refresh=refresh"),
                                org.hamcrest.Matchers.containsString("Path=/v1/auth"),
                                org.hamcrest.Matchers.containsString("HttpOnly"),
                                org.hamcrest.Matchers.containsString("Secure"),
                                org.hamcrest.Matchers.containsString("SameSite=Strict"))))
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(loginService).login(any(), eq("login:request-1"), eq(REQUEST_ID), eq("127.0.0.1"));
    }

    @Test
    void API_R_030_refreshReadsHttpOnlyCookieAndRotatesIt() throws Exception {
        when(authService.refresh("refresh-r1", "refresh:request-1", REQUEST_ID, "127.0.0.1"))
                .thenReturn(new LoginResult(200, new AuthSessionTokens("access-r2", "refresh-r2", 900)));

        mockMvc.perform(post("/v1/auth/refresh")
                        .cookie(new Cookie(AuthLoginController.REFRESH_COOKIE, "refresh-r1"))
                        .header("X-Request-Id", REQUEST_ID)
                        .header("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
                        .header("Idempotency-Key", "refresh:request-1"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Set-Cookie",
                        org.hamcrest.Matchers.containsString("__Secure-xproject_refresh=refresh-r2")))
                .andExpect(jsonPath("$.accessToken").value("access-r2"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist());

        verify(authService).refresh("refresh-r1", "refresh:request-1", REQUEST_ID, "127.0.0.1");
    }

    @Test
    void API_R_030_invalidRefreshExpiresTheHttpOnlyCookie() throws Exception {
        when(authService.refresh("", "refresh:invalid", REQUEST_ID, "127.0.0.1"))
                .thenReturn(new LoginResult(
                        401,
                        new ErrorResponse(
                                new ErrorResponseError("TOKEN_REVOKED", "Invalid token", "err.token_revoked"),
                                REQUEST_ID)));

        mockMvc.perform(post("/v1/auth/refresh")
                        .header("X-Request-Id", REQUEST_ID)
                        .header("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
                        .header("Idempotency-Key", "refresh:invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(
                        "Set-Cookie",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("__Secure-xproject_refresh="),
                                org.hamcrest.Matchers.containsString("Max-Age=0"),
                                org.hamcrest.Matchers.containsString("HttpOnly"),
                                org.hamcrest.Matchers.containsString("Secure"),
                                org.hamcrest.Matchers.containsString("SameSite=Strict"))))
                .andExpect(jsonPath("$.error.code").value("TOKEN_REVOKED"));
    }

    @Test
    void API_AC_004_sixthFailedLoginRequiresCaptchaAndReturnsRetryAfter() throws Exception {
        when(rateLimitService.requiresCaptcha("player@example.com")).thenReturn(true);
        when(captchaVerifier.verify(null, "127.0.0.1")).thenReturn(false);

        mockMvc.perform(post("/v1/auth/login")
                        .header("X-Request-Id", REQUEST_ID)
                        .header("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
                        .header("Idempotency-Key", "login:request-6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "player@example.com",
                                  "password": "wrong password",
                                  "device": "desktop"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"));

        verify(loginService, never()).login(any(), any(), any(), any());
    }

    @Test
    void API_R_001_invalidLoginBodyUsesContractValidationError() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .header("X-Request-Id", REQUEST_ID)
                        .header("traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
                        .header("Idempotency-Key", "login:request-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "some password",
                                  "device": "desktop"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION"))
                .andExpect(jsonPath("$.requestId").value(REQUEST_ID.toString()));

        verify(loginService, never()).login(any(), any(), any(), any());
    }
}
