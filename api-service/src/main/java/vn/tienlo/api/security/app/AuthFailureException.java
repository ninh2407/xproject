package vn.tienlo.api.security.app;

import vn.tienlo.api.generated.publicapi.model.ErrorResponse;

public class AuthFailureException extends RuntimeException {
    private final int httpStatus;
    private final ErrorResponse errorResponse;

    public AuthFailureException(int httpStatus, ErrorResponse errorResponse) {
        super("Authentication request failed with status " + httpStatus);
        this.httpStatus = httpStatus;
        this.errorResponse = errorResponse;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public ErrorResponse errorResponse() {
        return errorResponse;
    }
}
