package vn.tienlo.api.security.app;

public record AuthSessionTokens(String accessToken, String refreshToken, int expiresIn) {
    @Override
    public String toString() {
        return "AuthSessionTokens[redacted]";
    }
}
