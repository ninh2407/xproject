package vn.tienlo.api.security.app;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidV7Generator {
    private final Clock clock;
    private final SecureRandom secureRandom;

    public UuidV7Generator(Clock clock, SecureRandom secureRandom) {
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    public UUID next() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);

        long timestamp = clock.millis() & 0x0000_FFFF_FFFF_FFFFL;
        for (int index = 5; index >= 0; index--) {
            bytes[index] = (byte) timestamp;
            timestamp >>>= 8;
        }
        bytes[6] = (byte) ((bytes[6] & 0x0F) | 0x70);
        bytes[8] = (byte) ((bytes[8] & 0x3F) | 0x80);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
