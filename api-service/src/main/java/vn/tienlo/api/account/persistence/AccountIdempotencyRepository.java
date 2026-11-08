package vn.tienlo.api.account.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.MessageDigest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AccountIdempotencyRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AccountIdempotencyRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Attempt begin(String endpoint, String key, byte[] requestHash) {
        int inserted = jdbcTemplate.update(
                """
                INSERT INTO game.idempotency_record (key, endpoint, request_hash, status)
                VALUES (?, ?, ?, 'IN_PROGRESS')
                ON CONFLICT (key) DO NOTHING
                """,
                key,
                endpoint,
                requestHash);
        StoredRecord record = jdbcTemplate.queryForObject(
                """
                SELECT endpoint, request_hash, status, http_status, response::text AS response
                FROM game.idempotency_record
                WHERE key = ?
                FOR UPDATE
                """,
                (resultSet, rowNumber) -> new StoredRecord(
                        resultSet.getString("endpoint"),
                        resultSet.getBytes("request_hash"),
                        resultSet.getString("status"),
                        resultSet.getObject("http_status") == null
                                ? null
                                : ((Number) resultSet.getObject("http_status")).intValue(),
                        resultSet.getString("response")),
                key);

        if (!endpoint.equals(record.endpoint()) || !MessageDigest.isEqual(requestHash, record.requestHash())) {
            return new Attempt(AttemptState.MISMATCH, null, null);
        }
        if (inserted == 1) {
            return new Attempt(AttemptState.NEW, null, null);
        }
        if ("DONE".equals(record.status()) && record.httpStatus() != null && record.response() != null) {
            try {
                return new Attempt(
                        AttemptState.REPLAY,
                        record.httpStatus(),
                        objectMapper.readTree(record.response()));
            } catch (JsonProcessingException exception) {
                throw new IllegalStateException("Stored account idempotency response is invalid", exception);
            }
        }
        throw new IllegalStateException("A committed account idempotency record is still in progress");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void complete(String endpoint, String key, int httpStatus, Object response) {
        final String serialized;
        try {
            serialized = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize account idempotency response", exception);
        }
        int updated = jdbcTemplate.update(
                """
                UPDATE game.idempotency_record
                SET status = 'DONE', http_status = ?, response = ?::jsonb
                WHERE key = ? AND endpoint = ? AND status = 'IN_PROGRESS'
                """,
                httpStatus,
                serialized,
                key,
                endpoint);
        if (updated != 1) {
            throw new IllegalStateException("Account idempotency record was not in progress");
        }
    }

    public enum AttemptState {
        NEW,
        REPLAY,
        MISMATCH
    }

    public record Attempt(AttemptState state, Integer httpStatus, JsonNode response) {}

    private record StoredRecord(String endpoint, byte[] requestHash, String status, Integer httpStatus, String response) {}
}
