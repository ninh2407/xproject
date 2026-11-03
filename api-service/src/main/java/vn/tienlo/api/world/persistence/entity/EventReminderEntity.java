package vn.tienlo.api.world.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.event_reminder}; DDL: docs/design/24-world-events.md. */
@Entity
@Table(name = "event_reminder", schema = "game")
@IdClass(EventReminderEntity.Key.class)
public class EventReminderEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "event_id", nullable = false, length = 48)
    @Id
    private String eventId;

    public EventReminderEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String eventId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof EventReminderEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(eventId, that.eventId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, eventId);
        }
    }
}
