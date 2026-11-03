package vn.tienlo.api.social.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.player_sect_ledger}; DDL: docs/design/20-social.md. */
@Entity
@Table(name = "player_sect_ledger", schema = "game")
public class PlayerSectLedgerEntity {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sect_id", nullable = false)
    private UUID sectId;

    @Column(name = "delta", nullable = false)
    private Long delta;

    @Column(name = "reason_code", nullable = false, length = 24)
    private String reasonCode;

    @Column(name = "account_id", nullable = true)
    private UUID accountId;

    @Column(name = "ref_id", nullable = true, length = 80)
    private String refId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public PlayerSectLedgerEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSectId() {
        return sectId;
    }

    public void setSectId(UUID sectId) {
        this.sectId = sectId;
    }

    public Long getDelta() {
        return delta;
    }

    public void setDelta(Long delta) {
        this.delta = delta;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
