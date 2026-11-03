package vn.tienlo.api.world.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.world_event_log}; DDL: docs/design/24-world-events.md. */
@Entity
@Table(name = "world_event_log", schema = "game")
public class WorldEventLogEntity {
    @Column(name = "id", nullable = false, length = 64)
    @Id
    private String id;

    @Column(name = "event_id", nullable = false, length = 48)
    private String eventId;

    @Column(name = "state", nullable = false, length = 12)
    private String state;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "scope_resolved", nullable = false, columnDefinition = "jsonb")
    private JsonNode scopeResolved;

    @Column(name = "seed", nullable = false)
    private byte[] seed;

    @Column(name = "announced_at", nullable = false)
    private Instant announcedAt;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "ended_at", nullable = true)
    private Instant endedAt;

    @Column(name = "outcome", nullable = true, length = 12)
    private String outcome;

    @Column(name = "participants", nullable = true)
    private Integer participants;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public WorldEventLogEntity() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public JsonNode getScopeResolved() {
        return scopeResolved;
    }

    public void setScopeResolved(JsonNode scopeResolved) {
        this.scopeResolved = scopeResolved;
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setSeed(byte[] seed) {
        this.seed = seed;
    }

    public Instant getAnnouncedAt() {
        return announcedAt;
    }

    public void setAnnouncedAt(Instant announcedAt) {
        this.announcedAt = announcedAt;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Instant startsAt) {
        this.startsAt = startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
