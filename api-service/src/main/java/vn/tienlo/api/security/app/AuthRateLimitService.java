package vn.tienlo.api.security.app;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class AuthRateLimitService {
    private static final DefaultRedisScript<Long> TOKEN_BUCKET = new DefaultRedisScript<>(
            """
            local capacity = tonumber(ARGV[1])
            local refill_per_ms = tonumber(ARGV[2])
            local now_ms = tonumber(ARGV[3])
            local tokens = redis.call('HGET', KEYS[1], 'tokens')
            local updated_at = redis.call('HGET', KEYS[1], 'updated_at')
            if not tokens then
              tokens = capacity
            else
              tokens = math.min(capacity, tonumber(tokens) + math.max(0, now_ms - tonumber(updated_at)) * refill_per_ms)
            end
            if tokens < 1 then
              local wait_ms = math.ceil((1 - tokens) / refill_per_ms)
              return wait_ms
            end
            tokens = tokens - 1
            redis.call('HSET', KEYS[1], 'tokens', tokens, 'updated_at', now_ms)
            redis.call('PEXPIRE', KEYS[1], 120000)
            return 0
            """,
            Long.class);
    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL = new DefaultRedisScript<>(
            """
            local value = redis.call('INCR', KEYS[1])
            if value == 1 then
              redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return value
            """,
            Long.class);

    private static final Duration EMAIL_FAILURE_WINDOW = Duration.ofMinutes(15);
    private static final Duration IP_FAILURE_WINDOW = Duration.ofHours(1);
    private static final Duration LOCK_LEVEL_WINDOW = Duration.ofHours(24);
    private static final String EMAIL_FAILURES = "auth:fail:email:";
    private static final String IP_FAILURES = "auth:fail:ip:";
    private static final String IP_BLOCK = "auth:block:ip:";
    private static final String EMAIL_LOCK = "auth:lock:email:";
    private static final String EMAIL_LOCK_LEVEL = "auth:lock-level:email:";

    private final StringRedisTemplate redis;
    private final AuthRequestFingerprint fingerprint;
    private final Clock clock;

    public AuthRateLimitService(StringRedisTemplate redis, AuthRequestFingerprint fingerprint, Clock clock) {
        this.redis = redis;
        this.fingerprint = fingerprint;
        this.clock = clock;
    }

    public void consumeRequest(String remoteIp) {
        String ipKey = fingerprint.subjectKey(remoteIp);
        String blockKey = IP_BLOCK + ipKey;
        Long blockTtl = redis.getExpire(blockKey);
        if (blockTtl != null && blockTtl > 0) {
            throw new AuthRateLimitException(blockTtl);
        }

        Long retryMs = redis.execute(
                TOKEN_BUCKET,
                List.of("rl:auth:ip:" + ipKey),
                "10",
                Double.toString(10.0 / 60_000),
                Long.toString(clock.millis()));
        if (retryMs == null) {
            throw new IllegalStateException("Valkey did not return an authentication rate-limit result");
        }
        if (retryMs > 0) {
            throw new AuthRateLimitException((retryMs + 999) / 1000);
        }
    }

    public void assertLoginAllowed(String normalizedEmail) {
        String key = EMAIL_LOCK + fingerprint.subjectKey(normalizedEmail);
        Long ttl = redis.getExpire(key);
        if (ttl != null && ttl > 0) {
            throw new AuthRateLimitException(ttl);
        }
    }

    public boolean requiresCaptcha(String normalizedEmail) {
        String value = redis.opsForValue().get(EMAIL_FAILURES + fingerprint.subjectKey(normalizedEmail));
        long failures = value == null ? 0 : Long.parseLong(value);
        return failures >= 5;
    }

    public void recordLoginOutcome(String normalizedEmail, String remoteIp, boolean succeeded) {
        String emailKey = fingerprint.subjectKey(normalizedEmail);
        if (succeeded) {
            redis.delete(EMAIL_FAILURES + emailKey);
            redis.delete(EMAIL_LOCK + emailKey);
            return;
        }

        Long emailFailures = increment(EMAIL_FAILURES + emailKey, EMAIL_FAILURE_WINDOW);
        if (emailFailures >= 10) {
            Long level = increment(EMAIL_LOCK_LEVEL + emailKey, LOCK_LEVEL_WINDOW);
            int exponent = (int) Math.min(Math.max(0, level - 1), 7);
            long lockSeconds = Math.min(24 * 60 * 60L, 15 * 60L * (1L << exponent));
            redis.opsForValue().set(EMAIL_LOCK + emailKey, "1", Duration.ofSeconds(lockSeconds));
        }

        String ipKey = fingerprint.subjectKey(remoteIp);
        Long ipFailures = increment(IP_FAILURES + ipKey, IP_FAILURE_WINDOW);
        if (ipFailures >= 50) {
            redis.opsForValue().set(IP_BLOCK + ipKey, "1", Duration.ofHours(1));
        }
    }

    private Long increment(String key, Duration ttl) {
        Long value = redis.execute(INCREMENT_WITH_TTL, List.of(key), Long.toString(ttl.toMillis()));
        if (value == null) {
            throw new IllegalStateException("Valkey did not return a counter value");
        }
        return value;
    }
}
