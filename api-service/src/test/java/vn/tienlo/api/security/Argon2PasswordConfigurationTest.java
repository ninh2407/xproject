package vn.tienlo.api.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class Argon2PasswordConfigurationTest {
    @Test
    void SEC_R_010_hashesPasswordsWithConfiguredArgon2idParameters() {
        PasswordEncoder passwordEncoder = new Argon2PasswordConfiguration().passwordEncoder();
        String encoded = passwordEncoder.encode("correct horse battery staple");

        assertTrue(encoded.startsWith("$argon2id$v=19$m=65536,t=3,p=1$"));
        assertTrue(passwordEncoder.matches("correct horse battery staple", encoded));
        assertFalse(passwordEncoder.matches("incorrect password", encoded));
    }
}
