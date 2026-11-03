package vn.tienlo.api.pvp.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.pvp_kill_log}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "pvp_kill_log", schema = "game")
@IdClass(PvpKillLogEntity.Key.class)
public class PvpKillLogEntity {
    @Column(name = "id", nullable = false)
    @Id
    private Long id;

    @Column(name = "killer_id", nullable = false)
    private UUID killerId;

    @Column(name = "victim_id", nullable = false)
    private UUID victimId;

    @Column(name = "map_id", nullable = false, length = 24)
    private String mapId;

    @Column(name = "victim_mode", nullable = false, length = 10)
    private String victimMode;

    @Column(name = "victim_tier", nullable = false, length = 8)
    private String victimTier;

    @Column(name = "infamy_delta", nullable = false)
    private Integer infamyDelta;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dropped", nullable = false, columnDefinition = "jsonb")
    private JsonNode dropped;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "created_at", nullable = false)
    @Id
    private Instant createdAt;

    public PvpKillLogEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getKillerId() {
        return killerId;
    }

    public void setKillerId(UUID killerId) {
        this.killerId = killerId;
    }

    public UUID getVictimId() {
        return victimId;
    }

    public void setVictimId(UUID victimId) {
        this.victimId = victimId;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getVictimMode() {
        return victimMode;
    }

    public void setVictimMode(String victimMode) {
        this.victimMode = victimMode;
    }

    public String getVictimTier() {
        return victimTier;
    }

    public void setVictimTier(String victimTier) {
        this.victimTier = victimTier;
    }

    public Integer getInfamyDelta() {
        return infamyDelta;
    }

    public void setInfamyDelta(Integer infamyDelta) {
        this.infamyDelta = infamyDelta;
    }

    public JsonNode getDropped() {
        return dropped;
    }

    public void setDropped(JsonNode dropped) {
        this.dropped = dropped;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public Long id;
        public Instant createdAt;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof PvpKillLogEntity.Key that)) return false;
            return Objects.equals(id, that.id) && Objects.equals(createdAt, that.createdAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, createdAt);
        }
    }
}
