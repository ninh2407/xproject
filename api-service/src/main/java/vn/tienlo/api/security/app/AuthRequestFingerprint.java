package vn.tienlo.api.security.app;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import vn.tienlo.api.security.LoginHmacKey;
import vn.tienlo.api.security.api.RequestFingerprint;

@Component
public class AuthRequestFingerprint implements RequestFingerprint {
    private final LoginHmacKey hmacKey;

    public AuthRequestFingerprint(LoginHmacKey hmacKey) {
        this.hmacKey = hmacKey;
    }

    @Override
    public byte[] hash(String endpoint, String... values) {
        byte[] key = hmacKey.value();
        byte[] requestBytes = null;
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            try (DataOutputStream data = new DataOutputStream(payload)) {
                writeString(data, endpoint);
                for (String value : values) {
                    writeString(data, value == null ? "" : value);
                }
            }
            requestBytes = payload.toByteArray();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(requestBytes);
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalStateException("Could not fingerprint authentication request", exception);
        } finally {
            Arrays.fill(key, (byte) 0);
            if (requestBytes != null) {
                Arrays.fill(requestBytes, (byte) 0);
            }
        }
    }

    public String subjectKey(String value) {
        return java.util.HexFormat.of().formatHex(hash("auth-subject", value));
    }

    private static void writeString(DataOutputStream data, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        data.writeInt(bytes.length);
        data.write(bytes);
        Arrays.fill(bytes, (byte) 0);
    }
}
