package vn.tienlo.api.security.app;

public class AuthRateLimitException extends RuntimeException {
    private final long retryAfterSeconds;

    public AuthRateLimitException(long retryAfterSeconds) {
        super("Authentication rate limit exceeded");
        this.retryAfterSeconds = Math.max(1, retryAfterSeconds);
    }

    public long retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
