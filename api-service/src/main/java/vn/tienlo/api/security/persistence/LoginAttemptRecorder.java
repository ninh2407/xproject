package vn.tienlo.api.security.persistence;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginAttemptRecorder {
    private final JdbcTemplate jdbcTemplate;

    public LoginAttemptRecorder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void record(UUID accountId, String ipAddress, boolean succeeded) {
        jdbcTemplate.update(
                """
                INSERT INTO game.login_attempt (account_id, ip, succeeded)
                VALUES (?::uuid, ?::inet, ?)
                """,
                accountId,
                ipAddress,
                succeeded);
    }

    public int deleteExpiredBatch() {
        return jdbcTemplate.update(
                """
                DELETE FROM game.login_attempt
                WHERE id IN (
                    SELECT id
                    FROM game.login_attempt
                    WHERE created_at < now() - INTERVAL '90 days'
                    ORDER BY created_at
                    LIMIT 5000
                    FOR UPDATE SKIP LOCKED
                )
                """);
    }
}
