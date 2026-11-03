package vn.tienlo.api.security.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class UuidV7GeneratorTest {
    @Test
    void API_R_030_generatesVersionSevenSessionIds() {
        UuidV7Generator generator = new UuidV7Generator(
                Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC),
                new SecureRandom());

        var sessionId = generator.next();

        assertEquals(7, sessionId.version());
        assertEquals(2, sessionId.variant());
    }
}
