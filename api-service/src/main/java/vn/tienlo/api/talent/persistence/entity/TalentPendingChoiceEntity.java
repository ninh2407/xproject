package vn.tienlo.api.talent.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.talent_pending_choice}; DDL: docs/design/04-talents.md. */
@Entity
@Table(name = "talent_pending_choice", schema = "game")
public class TalentPendingChoiceEntity {
    @Column(name = "pending_id", nullable = false)
    @Id
    private UUID pendingId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "kind", nullable = false, length = 16)
    private String kind;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "offers", nullable = false, columnDefinition = "jsonb")
    private JsonNode offers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context", nullable = false, columnDefinition = "jsonb")
    private JsonNode context;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at", nullable = true)
    private Instant resolvedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resolution", nullable = true, columnDefinition = "jsonb")
    private JsonNode resolution;

    public TalentPendingChoiceEntity() {}

    public UUID getPendingId() {
        return pendingId;
    }

    public void setPendingId(UUID pendingId) {
        this.pendingId = pendingId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public JsonNode getOffers() {
        return offers;
    }

    public void setOffers(JsonNode offers) {
        this.offers = offers;
    }

    public JsonNode getContext() {
        return context;
    }

    public void setContext(JsonNode context) {
        this.context = context;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public JsonNode getResolution() {
        return resolution;
    }

    public void setResolution(JsonNode resolution) {
        this.resolution = resolution;
    }

}
