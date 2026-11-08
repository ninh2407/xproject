package vn.tienlo.api.security.app;

public class CaptchaUnavailableException extends RuntimeException {
    public CaptchaUnavailableException(String message) {
        super(message);
    }

    public CaptchaUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
