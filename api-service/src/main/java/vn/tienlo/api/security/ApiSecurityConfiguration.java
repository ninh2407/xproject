package vn.tienlo.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.security.JwtDependencyUnavailableException;

@Configuration
public class ApiSecurityConfiguration {
    @Bean
    SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler)
            throws Exception {
        return http.cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/v1/auth/register",
                                "/v1/auth/login",
                                "/v1/auth/refresh")
                        .permitAll()
                        .requestMatchers("/internal/**", "/admin/**")
                        .denyAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${security.cors.allowed-origins:}") String configuredOrigins) {
        List<String> origins = Arrays.stream(configuredOrigins.split(","))
                .map(String::strip)
                .filter(origin -> !origin.isEmpty())
                .toList();
        if (origins.stream().anyMatch("*"::equals)) {
            throw new IllegalStateException("TL_CORS_ALLOWED_ORIGINS must not contain a wildcard");
        }

        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origins);
        cors.setAllowCredentials(true);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Idempotency-Key",
                "traceparent",
                "X-Request-Id"));
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/v1/**", cors);
        return source;
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            if (hasCause(exception, JwtDependencyUnavailableException.class)) {
                writeError(objectMapper, request, response, HttpStatus.SERVICE_UNAVAILABLE, "UNAVAILABLE", "err.unavailable");
            } else if (containsJwtError(exception, "token_expired")) {
                writeError(objectMapper, request, response, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "err.token_expired");
            } else if (containsJwtError(exception, "token_revoked")) {
                writeError(objectMapper, request, response, HttpStatus.UNAUTHORIZED, "TOKEN_REVOKED", "err.token_revoked");
            } else {
                writeError(
                        objectMapper,
                        request,
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHENTICATED",
                        "err.unauthenticated");
            }
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) ->
                writeError(objectMapper, request, response, HttpStatus.FORBIDDEN, "FORBIDDEN", "err.forbidden");
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    private static void writeError(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String messageKey)
            throws IOException {
        UUID requestId;
        try {
            requestId = UUID.fromString(request.getHeader("X-Request-Id"));
        } catch (IllegalArgumentException | NullPointerException exception) {
            requestId = UUID.randomUUID();
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Cache-Control", "no-store");
        objectMapper.writeValue(
                response.getOutputStream(),
                new ErrorResponse(
                        new ErrorResponseError(code, status.getReasonPhrase(), messageKey),
                        requestId));
    }

    private static boolean containsJwtError(Throwable throwable, String code) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof JwtValidationException validationException
                    && validationException.getErrors().stream().anyMatch(error -> code.equals(error.getErrorCode()))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean hasCause(Throwable throwable, Class<? extends Throwable> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
