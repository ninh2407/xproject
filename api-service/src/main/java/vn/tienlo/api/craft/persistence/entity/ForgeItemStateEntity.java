package vn.tienlo.api.craft.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.forge_item_state}; DDL: docs/design/17-forging.md. */
@Entity
@Table(name = "forge_item_state", schema = "game")
public class ForgeItemStateEntity {
    @Column(name = "item_instance_id", nullable = false)
    @Id
    private UUID itemInstanceId;

    @Column(name = "enhance_pity", nullable = false)
    private Short enhancePity;

    @Column(name = "upgrade_pity", nullable = false)
    private Short upgradePity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enhance_stones_invested", nullable = false, columnDefinition = "jsonb")
    private JsonNode enhanceStonesInvested;

    public ForgeItemStateEntity() {}

    public UUID getItemInstanceId() {
        return itemInstanceId;
    }

    public void setItemInstanceId(UUID itemInstanceId) {
        this.itemInstanceId = itemInstanceId;
    }

    public Short getEnhancePity() {
        return enhancePity;
    }

    public void setEnhancePity(Short enhancePity) {
        this.enhancePity = enhancePity;
    }

    public Short getUpgradePity() {
        return upgradePity;
    }

    public void setUpgradePity(Short upgradePity) {
        this.upgradePity = upgradePity;
    }

    public JsonNode getEnhanceStonesInvested() {
        return enhanceStonesInvested;
    }

    public void setEnhanceStonesInvested(JsonNode enhanceStonesInvested) {
        this.enhanceStonesInvested = enhanceStonesInvested;
    }

}
