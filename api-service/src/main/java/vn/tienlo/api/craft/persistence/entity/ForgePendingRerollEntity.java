package vn.tienlo.api.craft.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.forge_pending_reroll}; DDL: docs/design/17-forging.md. */
@Entity
@Table(name = "forge_pending_reroll", schema = "game")
public class ForgePendingRerollEntity {
    @Column(name = "pending_id", nullable = false)
    @Id
    private UUID pendingId;

    @Column(name = "item_instance_id", nullable = false)
    private UUID itemInstanceId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "mode", nullable = false, length = 8)
    private String mode;

    @Column(name = "line_index", nullable = true)
    private Short lineIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_affixes", nullable = false, columnDefinition = "jsonb")
    private JsonNode oldAffixes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_affixes", nullable = false, columnDefinition = "jsonb")
    private JsonNode newAffixes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public ForgePendingRerollEntity() {}

    public UUID getPendingId() {
        return pendingId;
    }

    public void setPendingId(UUID pendingId) {
        this.pendingId = pendingId;
    }

    public UUID getItemInstanceId() {
        return itemInstanceId;
    }

    public void setItemInstanceId(UUID itemInstanceId) {
        this.itemInstanceId = itemInstanceId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Short getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(Short lineIndex) {
        this.lineIndex = lineIndex;
    }

    public JsonNode getOldAffixes() {
        return oldAffixes;
    }

    public void setOldAffixes(JsonNode oldAffixes) {
        this.oldAffixes = oldAffixes;
    }

    public JsonNode getNewAffixes() {
        return newAffixes;
    }

    public void setNewAffixes(JsonNode newAffixes) {
        this.newAffixes = newAffixes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

}
