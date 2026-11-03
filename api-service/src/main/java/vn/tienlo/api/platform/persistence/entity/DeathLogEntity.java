package vn.tienlo.api.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.death_log}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "death_log", schema = "game")
public class DeathLogEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "map_id", nullable = false, length = 24)
    private String mapId;

    @Column(name = "cause", nullable = false, length = 24)
    private String cause;

    @Column(name = "payload", nullable = false)
    private byte[] payload;

    @Column(name = "size_bytes", nullable = false)
    private Integer sizeBytes;

    @Column(name = "needs_review", nullable = false)
    private Boolean needsReview;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DeathLogEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public Integer getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Integer sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Boolean getNeedsReview() {
        return needsReview;
    }

    public void setNeedsReview(Boolean needsReview) {
        this.needsReview = needsReview;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
