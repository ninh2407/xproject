package vn.tienlo.api.security;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(LoginSecurityProperties.class)
public class Argon2PasswordConfiguration {
    /** SEC-R-010: configure Argon2id with the specified memory, iteration, and parallelism values. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 65_536, 3);
    }

    @Bean
    SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    LoginHmacKey loginHmacKey(LoginSecurityProperties properties) {
        if (properties.idempotencyHmacKey() == null || properties.idempotencyHmacKey().isBlank()) {
            throw new IllegalStateException("TL_AUTH_IDEMPOTENCY_HMAC_KEY is required");
        }
        try {
            byte[] key = Base64.getDecoder().decode(properties.idempotencyHmacKey());
            if (key.length < 32) {
                java.util.Arrays.fill(key, (byte) 0);
                throw new IllegalStateException("TL_AUTH_IDEMPOTENCY_HMAC_KEY must contain at least 256 bits");
            }
            try {
                return new LoginHmacKey(key);
            } finally {
                java.util.Arrays.fill(key, (byte) 0);
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("TL_AUTH_IDEMPOTENCY_HMAC_KEY must be valid Base64", exception);
        }
    }

    @Bean
    DummyPasswordHash dummyPasswordHash(PasswordEncoder passwordEncoder, SecureRandom secureRandom) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String randomPassword = java.util.Base64.getEncoder().encodeToString(randomBytes);
        java.util.Arrays.fill(randomBytes, (byte) 0);
        return new DummyPasswordHash(passwordEncoder.encode(randomPassword));
    }

    public record DummyPasswordHash(String value) {}
}
