package vn.tienlo.api.security.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginSessionRepository {
    private static final TypeReference<List<String>> ROLE_LIST = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public LoginSessionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void create(
            UUID sessionId,
            UUID accountId,
            byte[] refreshTokenHash,
            String device,
            String ipAddress,
            Instant expiresAt) {
        lockAccount(accountId);
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

    public Optional<AccountSessionRecord> findByRefreshTokenHashForUpdate(byte[] tokenHash) {
        Optional<UUID> accountId = jdbcTemplate.query(
                        "SELECT account_id FROM game.account_session WHERE refresh_token_hash = ?",
                        (resultSet, rowNumber) -> resultSet.getObject("account_id", UUID.class),
                        tokenHash)
                .stream()
                .findFirst();
        if (accountId.isEmpty()) {
            return Optional.empty();
        }
        lockAccount(accountId.get());
        return jdbcTemplate.query(
                        """
                        SELECT s.id, s.account_id, a.status, a.suspended_until, a.roles::text, s.device,
                               s.expires_at, s.revoked_at, s.replaced_by_session_id
                        FROM game.account_session s
                        JOIN game.account a ON a.id = s.account_id
                        WHERE s.refresh_token_hash = ?
                        FOR UPDATE OF s
                        """,
                        (resultSet, rowNumber) -> {
                            try {
                                java.sql.Timestamp suspendedUntil = resultSet.getTimestamp("suspended_until");
                                java.sql.Timestamp revokedAt = resultSet.getTimestamp("revoked_at");
                                return new AccountSessionRecord(
                                        resultSet.getObject("id", UUID.class),
                                        resultSet.getObject("account_id", UUID.class),
                                        resultSet.getString("status"),
                                        suspendedUntil == null ? null : suspendedUntil.toInstant(),
                                        objectMapper.readValue(resultSet.getString("roles"), ROLE_LIST),
                                        resultSet.getString("device"),
                                        resultSet.getTimestamp("expires_at").toInstant(),
                                        revokedAt == null ? null : revokedAt.toInstant(),
                                        resultSet.getObject("replaced_by_session_id", UUID.class));
                            } catch (java.io.IOException exception) {
                                throw new IllegalStateException("Account roles contain invalid JSON", exception);
                            }
                        },
                        tokenHash)
                .stream()
                .findFirst();
    }

    public void rotate(
            UUID oldSessionId,
            UUID newSessionId,
            UUID accountId,
            byte[] newTokenHash,
            String device,
            String ipAddress,
            Instant expiresAt) {
        int updated = jdbcTemplate.update(
                """
                UPDATE game.account_session
                SET revoked_at = now(), replaced_by_session_id = ?
                WHERE id = ? AND revoked_at IS NULL
                """,
                newSessionId,
                oldSessionId);
        if (updated != 1) {
            throw new IllegalStateException("Refresh session was not active while rotating");
        }
        create(newSessionId, accountId, newTokenHash, device, ipAddress, expiresAt);
    }

    public boolean revoke(UUID sessionId, UUID accountId) {
        return jdbcTemplate.update(
                        """
                        UPDATE game.account_session
                        SET revoked_at = COALESCE(revoked_at, now())
                        WHERE id = ? AND account_id = ?
                        """,
                        sessionId,
                        accountId)
                == 1;
    }

    public List<UUID> revokeAll(UUID accountId) {
        lockAccount(accountId);
        return jdbcTemplate.query(
                """
                WITH targets AS (
                    SELECT id FROM game.account_session WHERE account_id = ?
                ), revoked AS (
                    UPDATE game.account_session
                    SET revoked_at = COALESCE(revoked_at, now())
                    WHERE id IN (SELECT id FROM targets)
                    RETURNING id
                )
                SELECT id FROM targets
                """,
                (resultSet, rowNumber) -> resultSet.getObject("id", UUID.class),
                accountId);
    }

    public void deleteAllForAccount(UUID accountId) {
        jdbcTemplate.update("DELETE FROM game.account_session WHERE account_id = ?", accountId);
    }

    public List<UUID> revokeOthers(UUID accountId, UUID activeSessionId) {
        lockAccount(accountId);
        return jdbcTemplate.query(
                """
                WITH targets AS (
                    SELECT id
                    FROM game.account_session
                    WHERE account_id = ? AND id <> ? AND revoked_at IS NULL
                ), revoked AS (
                    UPDATE game.account_session
                    SET revoked_at = COALESCE(revoked_at, now())
                    WHERE id IN (SELECT id FROM targets)
                    RETURNING id
                )
                SELECT id FROM targets
                """,
                (resultSet, rowNumber) -> resultSet.getObject("id", UUID.class),
                accountId,
                activeSessionId);
    }

    private void lockAccount(UUID accountId) {
        int found = jdbcTemplate.query(
                "SELECT id FROM game.account WHERE id = ? FOR UPDATE",
                (resultSet, rowNumber) -> resultSet.getObject("id", UUID.class),
                accountId).size();
        if (found != 1) {
            throw new IllegalStateException("Account was not found while locking sessions");
        }
    }
}
