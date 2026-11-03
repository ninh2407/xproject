package vn.tienlo.api.security.app;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenGenerator {
    private final SecureRandom secureRandom;

    public RefreshTokenGenerator(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public GeneratedRefreshToken generate() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        try {
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(tokenBytes);
            return new GeneratedRefreshToken(token, hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", exception);
        } finally {
            java.util.Arrays.fill(tokenBytes, (byte) 0);
        }
    }

    public record GeneratedRefreshToken(String token, byte[] hash) {}
}
