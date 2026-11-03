package vn.tienlo.api.security.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {
    private static final TypeReference<List<String>> ROLE_LIST = new TypeReference<>() {};

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AccountRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<AccountRecord> findByLoginEmail(String normalizedEmail) {
        return jdbcTemplate
                .query(
                        """
                        SELECT id, password_hash, status, suspended_until, roles::text
                        FROM game.account
                        WHERE login_email = ?
                        """,
                        (resultSet, rowNumber) -> {
                            try {
                                java.sql.Timestamp suspendedUntil = resultSet.getTimestamp("suspended_until");
                                return new AccountRecord(
                                        resultSet.getObject("id", UUID.class),
                                        resultSet.getString("password_hash"),
                                        resultSet.getString("status"),
                                        suspendedUntil == null ? null : suspendedUntil.toInstant(),
                                        objectMapper.readValue(resultSet.getString("roles"), ROLE_LIST));
                            } catch (java.io.IOException exception) {
                                throw new IllegalStateException("Account roles contain invalid JSON", exception);
                            }
                        },
                        normalizedEmail)
                .stream()
                .findFirst();
    }

    public void updateLastLogin(UUID accountId) {
        int updated = jdbcTemplate.update(
                "UPDATE game.account SET last_login_at = now() WHERE id = ?",
                accountId);
        if (updated != 1) {
            throw new IllegalStateException("Authenticated account disappeared before session creation");
        }
    }
}
