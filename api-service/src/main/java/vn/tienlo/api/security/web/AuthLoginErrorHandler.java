package vn.tienlo.api.security.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.security.app.AuthFailureException;
import vn.tienlo.api.security.app.AuthRateLimitException;
import vn.tienlo.api.security.app.CaptchaUnavailableException;

@RestControllerAdvice(basePackageClasses = AuthLoginController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthLoginErrorHandler {
    @ExceptionHandler(AuthRateLimitException.class)
    ResponseEntity<ErrorResponse> rateLimited(AuthRateLimitException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.RETRY_AFTER, Long.toString(exception.retryAfterSeconds()))
                .body(body("RATE_LIMITED", "Authentication rate limit exceeded", "err.rate_limited", request));
    }

    @ExceptionHandler(CaptchaUnavailableException.class)
    ResponseEntity<ErrorResponse> authDependencyUnavailable(
            CaptchaUnavailableException exception,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body(
                        "UNAVAILABLE",
                        "Authentication service is temporarily unavailable",
                        "err.unavailable",
                        request));
    }

    @ExceptionHandler(AuthFailureException.class)
    ResponseEntity<ErrorResponse> authenticationFailure(
            AuthFailureException exception,
            HttpServletRequest request) {
        ResponseEntity.BodyBuilder response = ResponseEntity.status(exception.httpStatus())
                .header(HttpHeaders.CACHE_CONTROL, "no-store");
        if ((exception.httpStatus() == HttpStatus.UNAUTHORIZED.value()
                        || exception.httpStatus() == HttpStatus.FORBIDDEN.value())
                && ("/v1/auth/refresh".equals(request.getRequestURI())
                        || "/v1/auth/logout".equals(request.getRequestURI()))) {
            response.header(HttpHeaders.SET_COOKIE, AuthLoginController.expiredRefreshCookie());
        }
        return response.body(exception.errorResponse());
    }

    private static ErrorResponse body(
            String code,
            String message,
            String messageKey,
            HttpServletRequest request) {
        return new ErrorResponse(new ErrorResponseError(code, message, messageKey), requestId(request));
    }

    private static UUID requestId(HttpServletRequest request) {
        String header = request.getHeader("X-Request-Id");
        if (header == null) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(header);
        } catch (IllegalArgumentException exception) {
            return UUID.randomUUID();
        }
    }
}
