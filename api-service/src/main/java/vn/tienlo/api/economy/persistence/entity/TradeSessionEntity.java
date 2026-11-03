package vn.tienlo.api.economy.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.trade_session}; DDL: docs/design/19-economy.md. */
@Entity
@Table(name = "trade_session", schema = "game")
public class TradeSessionEntity {
    @Column(name = "trade_id", nullable = false)
    @Id
    private UUID tradeId;

    @Column(name = "a_character_id", nullable = false)
    private UUID aCharacterId;

    @Column(name = "b_character_id", nullable = false)
    private UUID bCharacterId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "a_offer", nullable = false, columnDefinition = "jsonb")
    private JsonNode aOffer;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "b_offer", nullable = false, columnDefinition = "jsonb")
    private JsonNode bOffer;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @Column(name = "locked_at", nullable = true)
    private Instant lockedAt;

    @Column(name = "completed_at", nullable = true)
    private Instant completedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public TradeSessionEntity() {}

    public UUID getTradeId() {
        return tradeId;
    }

    public void setTradeId(UUID tradeId) {
        this.tradeId = tradeId;
    }

    public UUID getACharacterId() {
        return aCharacterId;
    }

    public void setACharacterId(UUID aCharacterId) {
        this.aCharacterId = aCharacterId;
    }

    public UUID getBCharacterId() {
        return bCharacterId;
    }

    public void setBCharacterId(UUID bCharacterId) {
        this.bCharacterId = bCharacterId;
    }

    public JsonNode getAOffer() {
        return aOffer;
    }

    public void setAOffer(JsonNode aOffer) {
        this.aOffer = aOffer;
    }

    public JsonNode getBOffer() {
        return bOffer;
    }

    public void setBOffer(JsonNode bOffer) {
        this.bOffer = bOffer;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
