package vn.tienlo.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class ApiSecurityConfigurationTest {
    @Test
    void API_AC_006_expiredAccessTokenReturnsTokenExpired() throws Exception {
        var entryPoint = new ApiSecurityConfiguration().authenticationEntryPoint(new ObjectMapper());
        var request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "123e4567-e89b-12d3-a456-426614174000");
        var response = new MockHttpServletResponse();
        var expired = new JwtValidationException(
                "Access token has expired",
                List.of(new OAuth2Error("token_expired", "Access token has expired", null)));

        entryPoint.commence(request, response, new BadCredentialsException("Invalid bearer token", expired));

        assertEquals(401, response.getStatus());
        assertEquals("TOKEN_EXPIRED", new ObjectMapper()
                .readTree(response.getContentAsString())
                .path("error")
                .path("code")
                .asText());
    }

    @Test
    void API_R_030_cookieCorsRequiresAnExplicitCredentialedOriginAllowlist() {
        ApiSecurityConfiguration configuration = new ApiSecurityConfiguration();
        CorsConfigurationSource source =
                configuration.corsConfigurationSource("https://game.example,capacitor://localhost");
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/v1/auth/refresh"));

        assertTrue(cors.getAllowCredentials());
        assertEquals(
                java.util.List.of("https://game.example", "capacitor://localhost"),
                cors.getAllowedOrigins());
        assertThrows(IllegalStateException.class, () -> configuration.corsConfigurationSource("*"));
    }
}
