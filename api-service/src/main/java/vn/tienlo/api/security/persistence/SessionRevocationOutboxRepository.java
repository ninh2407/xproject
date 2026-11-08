package vn.tienlo.api.security.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRevocationOutboxRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public SessionRevocationOutboxRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void add(UUID eventId, UUID accountId, Instant occurredAt, List<UUID> sessionIds) {
        Map<String, Object> event = Map.of(
                "eventId", eventId.toString(),
                "type", "account.sessions_revoked",
                "version", 1,
                "occurredAt", occurredAt.toString(),
                "producer", "API_SERVICE",
                "accountId", accountId.toString(),
                "payload", Map.of(
                        "sessionIds",
                        sessionIds.stream().map(UUID::toString).toList()));
        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO game.outbox_event
                        (event_id, type, version, stream, account_id, payload)
                    VALUES (?, 'account.sessions_revoked', 1, 'account', ?, ?::jsonb)
                    """,
                    eventId,
                    accountId,
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize session-revocation event", exception);
        }
    }
}
