package vn.tienlo.api.encounter.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.legacy_cave}; DDL: docs/design/23-encounters.md. */
@Entity
@Table(name = "legacy_cave", schema = "game")
public class LegacyCaveEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "from_account_id", nullable = false)
    private UUID fromAccountId;

    @Column(name = "from_character_id", nullable = false)
    private UUID fromCharacterId;

    @Column(name = "region_id", nullable = false, length = 8)
    private String regionId;

    @Column(name = "message", nullable = true)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "item_ids", nullable = false, columnDefinition = "jsonb")
    private JsonNode itemIds;

    @Column(name = "claimed_by", nullable = true)
    private UUID claimedBy;

    @Column(name = "claimed_at", nullable = true)
    private Instant claimedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public LegacyCaveEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(UUID fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public UUID getFromCharacterId() {
        return fromCharacterId;
    }

    public void setFromCharacterId(UUID fromCharacterId) {
        this.fromCharacterId = fromCharacterId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonNode getItemIds() {
        return itemIds;
    }

    public void setItemIds(JsonNode itemIds) {
        this.itemIds = itemIds;
    }

    public UUID getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(UUID claimedBy) {
        this.claimedBy = claimedBy;
    }

    public Instant getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(Instant claimedAt) {
        this.claimedAt = claimedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
