package vn.tienlo.api.danger.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.corpse}; DDL: docs/design/27-danger-zones.md. */
@Entity
@Table(name = "corpse", schema = "game")
public class CorpseEntity {
    @Column(name = "corpse_id", nullable = false)
    @Id
    private UUID corpseId;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "position", nullable = false, columnDefinition = "jsonb")
    private JsonNode position;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CorpseEntity() {}

    public UUID getCorpseId() {
        return corpseId;
    }

    public void setCorpseId(UUID corpseId) {
        this.corpseId = corpseId;
    }

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public JsonNode getPosition() {
        return position;
    }

    public void setPosition(JsonNode position) {
        this.position = position;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
