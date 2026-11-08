package vn.tienlo.api.platform.app;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PlatformRetentionJob {
    private final JdbcTemplate jdbcTemplate;

    public PlatformRetentionJob(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedDelay = 3_600_000L)
    public void deleteExpiredIdempotencyRecords() {
        jdbcTemplate.update(
                """
                DELETE FROM game.idempotency_record
                WHERE key IN (
                    SELECT key
                    FROM game.idempotency_record
                    WHERE created_at < now() - INTERVAL '7 days'
                    ORDER BY created_at
                    LIMIT 5000
                    FOR UPDATE SKIP LOCKED
                )
                """);
    }
}
