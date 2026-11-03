package vn.tienlo.api.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.consumed_event}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "consumed_event", schema = "game")
@IdClass(ConsumedEventEntity.Key.class)
public class ConsumedEventEntity {
    @Column(name = "consumer", nullable = false, length = 48)
    @Id
    private String consumer;

    @Column(name = "event_id", nullable = false)
    @Id
    private UUID eventId;

    @Column(name = "consumed_at", nullable = false)
    private Instant consumedAt;

    public ConsumedEventEntity() {}

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public String consumer;
        public UUID eventId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ConsumedEventEntity.Key that)) return false;
            return Objects.equals(consumer, that.consumer) && Objects.equals(eventId, that.eventId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(consumer, eventId);
        }
    }
}
