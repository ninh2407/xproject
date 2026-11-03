package vn.tienlo.api.cultivation.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.cultivation_ledger}; DDL: docs/design/10-cultivation.md. */
@Entity
@Table(name = "cultivation_ledger", schema = "game")
public class CultivationLedgerEntity {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "source", nullable = false, length = 24)
    private String source;

    @Column(name = "requested", nullable = false, precision = 14, scale = 2)
    private BigDecimal requested;

    @Column(name = "applied", nullable = false, precision = 14, scale = 2)
    private BigDecimal applied;

    @Column(name = "wasted", nullable = false, precision = 14, scale = 2)
    private BigDecimal wasted;

    @Column(name = "realm_code_after", nullable = false, length = 8)
    private String realmCodeAfter;

    @Column(name = "ref_id", nullable = true, length = 80)
    private String refId;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CultivationLedgerEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BigDecimal getRequested() {
        return requested;
    }

    public void setRequested(BigDecimal requested) {
        this.requested = requested;
    }

    public BigDecimal getApplied() {
        return applied;
    }

    public void setApplied(BigDecimal applied) {
        this.applied = applied;
    }

    public BigDecimal getWasted() {
        return wasted;
    }

    public void setWasted(BigDecimal wasted) {
        this.wasted = wasted;
    }

    public String getRealmCodeAfter() {
        return realmCodeAfter;
    }

    public void setRealmCodeAfter(String realmCodeAfter) {
        this.realmCodeAfter = realmCodeAfter;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
