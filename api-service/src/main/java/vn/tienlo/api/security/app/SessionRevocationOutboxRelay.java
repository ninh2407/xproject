package vn.tienlo.api.security.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionRevocationOutboxRelay {
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public SessionRevocationOutboxRelay(
            JdbcTemplate jdbcTemplate,
            StringRedisTemplate redis,
            ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    public void relayPending() {
        List<PendingEvent> pending = jdbcTemplate.query(
                """
                SELECT id, payload::text
                FROM game.outbox_event
                WHERE type = 'account.sessions_revoked' AND published_at IS NULL
                ORDER BY id
                LIMIT 100
                """,
                (resultSet, rowNumber) -> new PendingEvent(
                        resultSet.getLong("id"),
                        resultSet.getString("payload")));
        for (PendingEvent event : pending) {
            relay(event);
        }
    }

    public void relay(UUID eventId) {
        jdbcTemplate.query(
                """
                SELECT id, payload::text
                FROM game.outbox_event
                WHERE event_id = ? AND type = 'account.sessions_revoked' AND published_at IS NULL
                """,
                (resultSet, rowNumber) -> new PendingEvent(
                        resultSet.getLong("id"),
                        resultSet.getString("payload")),
                eventId).forEach(this::relay);
    }

    private void relay(PendingEvent event) {
        try {
            JsonNode envelope = objectMapper.readTree(event.payload());
            for (JsonNode sessionId : envelope.path("payload").path("sessionIds")) {
                redis.opsForValue().set(
                        "auth:revoked:sid:" + sessionId.asText(),
                        "1",
                        java.time.Duration.ofMinutes(15));
            }
            MapRecord<String, String, String> record = StreamRecords.<String, String, String>mapBacked(
                            Map.of("event", event.payload()))
                    .withStreamKey("evt:account");
            redis.opsForStream().add(record);
            redis.opsForStream().trim("evt:account", 1_000_000, true);
            jdbcTemplate.update(
                    "UPDATE game.outbox_event SET published_at = now() WHERE id = ? AND published_at IS NULL",
                    event.id());
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            throw new IllegalStateException("Stored session-revocation event is invalid", exception);
        }
    }

    private record PendingEvent(long id, String payload) {}
}
