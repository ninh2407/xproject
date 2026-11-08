package vn.tienlo.api.platform.api;

import java.util.UUID;

public class ApiFailureException extends RuntimeException {
    private final int httpStatus;
    private final String code;
    private final String messageKey;
    private final UUID requestId;

    public ApiFailureException(int httpStatus, String code, String messageKey, UUID requestId) {
        super(code);
        this.httpStatus = httpStatus;
        this.code = code;
        this.messageKey = messageKey;
        this.requestId = requestId;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String code() {
        return code;
    }

    public String messageKey() {
        return messageKey;
    }

    public UUID requestId() {
        return requestId;
    }
}
