package vn.tienlo.api.security.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginSessionRepository {
    private final JdbcTemplate jdbcTemplate;

    public LoginSessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(
            UUID sessionId,
            UUID accountId,
            byte[] refreshTokenHash,
            String device,
            String ipAddress,
            Instant expiresAt) {
        jdbcTemplate.update(
                """
                INSERT INTO game.account_session
                    (id, account_id, refresh_token_hash, device, ip, expires_at)
                VALUES (?, ?, ?, ?, ?::inet, ?)
                """,
                sessionId,
                accountId,
                refreshTokenHash,
                device,
                ipAddress,
                java.sql.Timestamp.from(expiresAt));
    }
}
