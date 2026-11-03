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
import vn.tienlo.api.generated.publicapi.model.AuthTokens;

@ExtendWith(MockitoExtension.class)
class AuthLoginControllerTest {
    private static final UUID REQUEST_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Mock
    private LoginService loginService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthLoginController(loginService))
                .setControllerAdvice(new AuthLoginErrorHandler())
                .build();
    }

    @Test
    void API_R_001_loginReturnsContractTokensAndPreventsCaching() throws Exception {
        when(loginService.login(any(), eq("login:request-1"), eq(REQUEST_ID), eq("127.0.0.1")))
                .thenReturn(new LoginResult(
                        200,
                        new AuthTokens("access", "refresh", AuthTokens.ExpiresInEnum.NUMBER_900)));

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
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(loginService).login(any(), eq("login:request-1"), eq(REQUEST_ID), eq("127.0.0.1"));
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
