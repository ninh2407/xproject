package vn.tienlo.api.security.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.tienlo.api.generated.publicapi.model.AccountCreated;
import vn.tienlo.api.generated.publicapi.model.ErrorResponse;
import vn.tienlo.api.generated.publicapi.model.ErrorResponseError;
import vn.tienlo.api.security.AuthIdempotencyCipher;
import vn.tienlo.api.security.app.AuthSessionTokens;
import vn.tienlo.api.security.app.LoginResult;

@Repository
public class LoginIdempotencyRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final AuthIdempotencyCipher responseCipher;

    public LoginIdempotencyRepository(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            AuthIdempotencyCipher responseCipher) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.responseCipher = responseCipher;
    }

    @Transactional
    public Optional<LoginResult> begin(String endpoint, String key, byte[] requestHash, UUID requestId) {
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
                SELECT key, endpoint, request_hash, status, http_status, response::text AS response
                FROM game.idempotency_record
                WHERE key = ?
                FOR UPDATE
                """,
                (resultSet, rowNumber) -> new StoredRecord(
                        resultSet.getString("key"),
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
        if (!endpoint.equals(record.endpoint()) || !MessageDigest.isEqual(requestHash, record.requestHash())) {
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

    public void complete(String endpoint, String key, LoginResult result) {
        final String response = responseCipher.encrypt(endpoint + ":" + key, result.body());
        int updated = jdbcTemplate.update(
                """
                UPDATE game.idempotency_record
                SET status = 'DONE', http_status = ?, response = ?::jsonb
                WHERE key = ? AND endpoint = ? AND status = 'IN_PROGRESS'
                """,
                result.httpStatus(),
                response,
                key,
                endpoint);
        if (updated != 1) {
            throw new IllegalStateException("Login idempotency record was not in progress");
        }
    }

    private LoginResult readResult(StoredRecord record) {
        if (record.httpStatus() == null || record.response() == null) {
            throw new IllegalStateException("Completed login idempotency record has no response");
        }
        try {
            String context = record.endpoint() + ":" + record.key();
            byte[] plaintext;
            if (responseCipher.isEncrypted(record.response())) {
                plaintext = responseCipher.decrypt(context, record.response());
            } else {
                JsonNode legacyResponse = objectMapper.readTree(record.response());
                String encrypted = responseCipher.encrypt(context, legacyResponse);
                jdbcTemplate.update(
                        """
                        UPDATE game.idempotency_record
                        SET response = ?::jsonb
                        WHERE key = ? AND status = 'DONE'
                        """,
                        encrypted,
                        record.key());
                plaintext = objectMapper.writeValueAsBytes(legacyResponse);
            }
            Object body;
            try {
                body = switch (record.httpStatus()) {
                    case 200 -> record.endpoint().equals("/v1/auth/login")
                                    || record.endpoint().equals("/v1/auth/refresh")
                            ? objectMapper.readValue(plaintext, AuthSessionTokens.class)
                            : objectMapper.readValue(plaintext, ErrorResponse.class);
                    case 201 -> objectMapper.readValue(plaintext, AccountCreated.class);
                    case 204 -> null;
                    default -> objectMapper.readValue(plaintext, ErrorResponse.class);
                };
            } finally {
                java.util.Arrays.fill(plaintext, (byte) 0);
            }
            return new LoginResult(record.httpStatus(), body);
        } catch (IOException exception) {
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

    private record StoredRecord(
            String key,
            String endpoint,
            byte[] requestHash,
            String status,
            Integer httpStatus,
            String response) {}
}
