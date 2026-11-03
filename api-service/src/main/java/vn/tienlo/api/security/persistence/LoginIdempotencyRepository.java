package vn.tienlo.api.security.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.tienlo.api.generated.publicapi.model.AuthTokens;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.security.app.LoginResult;

@Repository
public class LoginIdempotencyRepository {
    private static final String ENDPOINT = "/v1/auth/login";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public LoginIdempotencyRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Optional<LoginResult> begin(String key, byte[] requestHash, UUID requestId) {
        jdbcTemplate.update(
                "DELETE FROM game.idempotency_record WHERE created_at < now() - INTERVAL '7 days'");
        int inserted = jdbcTemplate.update(
                """
                INSERT INTO game.idempotency_record (key, endpoint, request_hash, status)
                VALUES (?, ?, ?, 'IN_PROGRESS')
                ON CONFLICT (key) DO NOTHING
                """,
                key,
                ENDPOINT,
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

        if (record == null) {
            throw new IllegalStateException("Idempotency record could not be loaded");
        }
        if (!ENDPOINT.equals(record.endpoint()) || !MessageDigest.isEqual(requestHash, record.requestHash())) {
            return Optional.of(errorResult(
                    422,
                    requestId,
                    "IDEMPOTENCY_MISMATCH",
                    "err.idempotency_mismatch"));
        }
        if (inserted == 1) {
            return Optional.empty();
        }
        if ("DONE".equals(record.status())) {
            return Optional.of(readResult(record));
        }
        throw new IllegalStateException("A committed login idempotency record is still in progress");
    }

    public void complete(String key, LoginResult result) {
        final String response;
        try {
            response = objectMapper.writeValueAsString(result.body());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize login response for idempotency", exception);
        }

        int updated = jdbcTemplate.update(
                """
                UPDATE game.idempotency_record
                SET status = 'DONE', http_status = ?, response = ?::jsonb
                WHERE key = ? AND endpoint = ? AND status = 'IN_PROGRESS'
                """,
                result.httpStatus(),
                response,
                key,
                ENDPOINT);
        if (updated != 1) {
            throw new IllegalStateException("Login idempotency record was not in progress");
        }
    }

    private LoginResult readResult(StoredRecord record) {
        if (record.httpStatus() == null || record.response() == null) {
            throw new IllegalStateException("Completed login idempotency record has no response");
        }
        try {
            Object body = record.httpStatus() == 200
                    ? objectMapper.readValue(record.response(), AuthTokens.class)
                    : objectMapper.readValue(record.response(), ErrorResponse.class);
            return new LoginResult(record.httpStatus(), body);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Stored login idempotency response is invalid", exception);
        }
    }

    private static LoginResult errorResult(
            int status,
            UUID requestId,
            String code,
            String messageKey) {
        ErrorResponse body = new ErrorResponse(
                new ErrorResponseError(code, "Request could not be processed", messageKey),
                requestId);
        return new LoginResult(status, body);
    }

    private record StoredRecord(String endpoint, byte[] requestHash, String status, Integer httpStatus, String response) {}
}
