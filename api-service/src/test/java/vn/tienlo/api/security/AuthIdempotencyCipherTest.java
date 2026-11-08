package vn.tienlo.api.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import org.junit.jupiter.api.Test;

class AuthIdempotencyCipherTest {
    @Test
    void idempotencyResponseIsEncryptedAndBoundToItsEndpointAndKey() {
        AuthIdempotencyCipher cipher =
                new AuthIdempotencyCipher(new LoginHmacKey(new byte[32]), new SecureRandom(), new ObjectMapper());
        String secretResponse = "{\"refreshToken\":\"sensitive-refresh-token\"}";

        String encrypted = cipher.encrypt("/v1/auth/login:login-key", secretResponse);

        assertFalse(encrypted.contains("sensitive-refresh-token"));
        assertTrue(new String(
                        cipher.decrypt("/v1/auth/login:login-key", encrypted),
                        StandardCharsets.UTF_8)
                .contains("sensitive-refresh-token"));
        assertThrows(
                IllegalStateException.class,
                () -> cipher.decrypt("/v1/auth/refresh:other-key", encrypted));
    }
}
