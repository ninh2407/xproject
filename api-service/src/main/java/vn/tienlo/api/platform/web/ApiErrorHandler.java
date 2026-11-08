package vn.tienlo.api.platform.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.beans.TypeMismatchException;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.platform.api.ApiFailureException;

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class ApiErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiErrorHandler.class);

    @ExceptionHandler(ApiFailureException.class)
    ResponseEntity<ErrorResponse> apiFailure(ApiFailureException exception, HttpServletRequest request) {
        return error(
                HttpStatus.valueOf(exception.httpStatus()),
                exception.code(),
                "Request could not be processed",
                exception.messageKey(),
                exception.requestId() == null ? requestId(request) : exception.requestId());
    }

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
                requestId(request));
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ErrorResponse> dependencyUnavailable(DataAccessException exception, HttpServletRequest request) {
        UUID requestId = requestId(request);
        LOGGER.warn(
                "API dependency failed for request {} ({})",
                requestId,
                exception.getClass().getSimpleName());
        return error(
                HttpStatus.SERVICE_UNAVAILABLE,
                "UNAVAILABLE",
                "A required service is temporarily unavailable",
                "err.unavailable",
                requestId);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpectedFailure(Exception exception, HttpServletRequest request) {
        UUID requestId = requestId(request);
        LOGGER.error(
                "Unhandled API failure for {} {} (requestId={})",
                request.getMethod(),
                request.getRequestURI(),
                requestId,
                exception.getClass().getSimpleName());
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                "err.internal_error",
                requestId);
    }

    private static ResponseEntity<ErrorResponse> error(
            HttpStatus status, String code, String message, String messageKey, UUID requestId) {
        return ResponseEntity.status(status)
                .header("Cache-Control", "no-store")
                .body(new ErrorResponse(new ErrorResponseError(code, message, messageKey), requestId));
    }

    private static UUID requestId(HttpServletRequest request) {
        String header = request.getHeader("X-Request-Id");
        if (header != null) {
            try {
                return UUID.fromString(header);
            } catch (IllegalArgumentException ignored) {
                LOGGER.debug("Ignoring invalid X-Request-Id header");
            }
        }
        return UUID.randomUUID();
    }
}
