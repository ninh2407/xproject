package vn.tienlo.api.security.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.UUID;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;

@RestControllerAdvice(basePackageClasses = AuthLoginController.class)
public class AuthLoginErrorHandler {
    @ExceptionHandler({
        ConstraintViolationException.class,
        HandlerMethodValidationException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class,
        ServletRequestBindingException.class,
        TypeMismatchException.class
    })
    ResponseEntity<ErrorResponse> invalidRequest(Exception exception, HttpServletRequest request) {
        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION",
                "Request is invalid",
                "err.validation",
                request);
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ErrorResponse> dependencyUnavailable(DataAccessException exception, HttpServletRequest request) {
        return error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "UNAVAILABLE",
                "Authentication service is temporarily unavailable",
                "err.unavailable",
                request);
    }

    private static ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            String messageKey,
            HttpServletRequest request) {
        return ResponseEntity.status(status)
                .header("Cache-Control", "no-store")
                .body(new ErrorResponse(
                        new ErrorResponseError(code, message, messageKey),
                        requestId(request)));
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
