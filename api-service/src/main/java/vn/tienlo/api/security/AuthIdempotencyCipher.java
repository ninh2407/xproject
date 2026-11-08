package vn.tienlo.api.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class AuthIdempotencyCipher {
    private static final byte[] KEY_CONTEXT =
            "xproject/auth-idempotency-response/aes-gcm/v1".getBytes(StandardCharsets.UTF_8);
    private static final int NONCE_LENGTH = 12;

    private final LoginHmacKey hmacKey;
    private final SecureRandom secureRandom;
    private final ObjectMapper objectMapper;

    public AuthIdempotencyCipher(
            LoginHmacKey hmacKey,
            SecureRandom secureRandom,
            ObjectMapper objectMapper) {
        this.hmacKey = hmacKey;
        this.secureRandom = secureRandom;
        this.objectMapper = objectMapper;
    }

    public String encrypt(String context, Object response) {
        byte[] plaintext = null;
        byte[] nonce = new byte[NONCE_LENGTH];
        byte[] encrypted = null;
        byte[] key = deriveKey();
        try {
            plaintext = objectMapper.writeValueAsBytes(response);
            secureRandom.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(context.getBytes(StandardCharsets.UTF_8));
            encrypted = cipher.doFinal(plaintext);
            return objectMapper.writeValueAsString(new EncryptedResponse(
                    Base64.getUrlEncoder().withoutPadding().encodeToString(nonce),
                    Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted)));
        } catch (GeneralSecurityException | JsonProcessingException exception) {
            throw new IllegalStateException("Could not encrypt authentication idempotency response", exception);
        } finally {
            Arrays.fill(key, (byte) 0);
            Arrays.fill(nonce, (byte) 0);
            if (plaintext != null) {
                Arrays.fill(plaintext, (byte) 0);
            }
            if (encrypted != null) {
                Arrays.fill(encrypted, (byte) 0);
            }
        }
    }

    public byte[] decrypt(String context, String storedResponse) {
        byte[] key = deriveKey();
        byte[] nonce = null;
        byte[] ciphertext = null;
        try {
            EncryptedResponse encrypted = objectMapper.readValue(storedResponse, EncryptedResponse.class);
            nonce = Base64.getUrlDecoder().decode(encrypted.nonce());
            ciphertext = Base64.getUrlDecoder().decode(encrypted.ciphertext());
            if (nonce.length != NONCE_LENGTH) {
                throw new IllegalStateException("Stored authentication idempotency nonce is invalid");
            }

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(context.getBytes(StandardCharsets.UTF_8));
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException | JsonProcessingException | IllegalArgumentException exception) {
            throw new IllegalStateException("Stored authentication idempotency response cannot be decrypted", exception);
        } finally {
            Arrays.fill(key, (byte) 0);
            if (nonce != null) {
                Arrays.fill(nonce, (byte) 0);
            }
            if (ciphertext != null) {
                Arrays.fill(ciphertext, (byte) 0);
            }
        }
    }

    public boolean isEncrypted(String storedResponse) {
        try {
            JsonNode response = objectMapper.readTree(storedResponse);
            return response != null
                    && response.isObject()
                    && response.hasNonNull("nonce")
                    && response.hasNonNull("ciphertext");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored authentication idempotency response is invalid", exception);
        }
    }

    private byte[] deriveKey() {
        byte[] masterKey = hmacKey.value();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(masterKey, "HmacSHA256"));
            return mac.doFinal(KEY_CONTEXT);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not derive authentication response encryption key", exception);
        } finally {
            Arrays.fill(masterKey, (byte) 0);
        }
    }

    private record EncryptedResponse(String nonce, String ciphertext) {}
}
