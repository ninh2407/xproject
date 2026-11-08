package vn.tienlo.api.lifecycle.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.account_meta}; DDL: docs/design/03-lifecycle.md. */
@Entity
@Table(name = "account_meta", schema = "game")
public class AccountMetaEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "rebirth_points", nullable = false)
    private Integer rebirthPoints;

    @Column(name = "rebirth_points_total", nullable = false)
    private Integer rebirthPointsTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "unlocks", nullable = false, columnDefinition = "jsonb")
    private JsonNode unlocks;

    @Column(name = "vault_slots", nullable = false)
    private Integer vaultSlots;

    @Column(name = "vault_spirit_stones", nullable = false)
    private Long vaultSpiritStones;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "first_reach_realms", nullable = false, columnDefinition = "jsonb")
    private JsonNode firstReachRealms;

    @Column(name = "bag_expansion", nullable = false)
    private Integer bagExpansion;

    public AccountMetaEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Integer getRebirthPoints() {
        return rebirthPoints;
    }

    public void setRebirthPoints(Integer rebirthPoints) {
        this.rebirthPoints = rebirthPoints;
    }

    public Integer getRebirthPointsTotal() {
        return rebirthPointsTotal;
    }

    public void setRebirthPointsTotal(Integer rebirthPointsTotal) {
        this.rebirthPointsTotal = rebirthPointsTotal;
    }

    public JsonNode getUnlocks() {
        return unlocks;
    }

    public void setUnlocks(JsonNode unlocks) {
        this.unlocks = unlocks;
    }

    public Integer getVaultSlots() {
        return vaultSlots;
    }

    public void setVaultSlots(Integer vaultSlots) {
        this.vaultSlots = vaultSlots;
    }

    public Long getVaultSpiritStones() {
        return vaultSpiritStones;
    }

    public void setVaultSpiritStones(Long vaultSpiritStones) {
        this.vaultSpiritStones = vaultSpiritStones;
    }

    public JsonNode getFirstReachRealms() {
        return firstReachRealms;
    }

    public void setFirstReachRealms(JsonNode firstReachRealms) {
        this.firstReachRealms = firstReachRealms;
    }

    public Integer getBagExpansion() {
        return bagExpansion;
    }

    public void setBagExpansion(Integer bagExpansion) {
        this.bagExpansion = bagExpansion;
    }

}
