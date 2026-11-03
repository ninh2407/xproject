package vn.tienlo.api.technique.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.crafted_technique}; DDL: docs/design/14-technique-carfting.md. */
@Entity
@Table(name = "crafted_technique", schema = "game")
public class CraftedTechniqueEntity {
    @Column(name = "id", nullable = false, length = 16)
    @Id
    private String id;

    @Column(name = "name", nullable = false, length = 32)
    private String name;

    @Column(name = "name_status", nullable = false, length = 16)
    private String nameStatus;

    @Column(name = "creator_account_id", nullable = false)
    private UUID creatorAccountId;

    @Column(name = "creator_character_id", nullable = false)
    private UUID creatorCharacterId;

    @Column(name = "creator_name_snapshot", nullable = false, length = 24)
    private String creatorNameSnapshot;

    @Column(name = "tier", nullable = false, length = 8)
    private String tier;

    @Column(name = "sub", nullable = false, length = 8)
    private String sub;

    @Column(name = "core_id", nullable = false, length = 32)
    private String coreId;

    @Column(name = "element_id", nullable = true, length = 32)
    private String elementId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "modifier_ids", nullable = false, columnDefinition = "jsonb")
    private JsonNode modifierIds;

    @Column(name = "mutation_modifier_id", nullable = true, length = 32)
    private String mutationModifierId;

    @Column(name = "damage_type", nullable = false, length = 8)
    private String damageType;

    @Column(name = "q", nullable = false, precision = 5, scale = 4)
    private BigDecimal q;

    @Column(name = "suppress_factor", nullable = false, precision = 5, scale = 4)
    private BigDecimal suppressFactor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skill_def", nullable = false, columnDefinition = "jsonb")
    private JsonNode skillDef;

    @Column(name = "power_score", nullable = false, precision = 6, scale = 4)
    private BigDecimal powerScore;

    @Column(name = "copies_made", nullable = false)
    private Integer copiesMade;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CraftedTechniqueEntity() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameStatus() {
        return nameStatus;
    }

    public void setNameStatus(String nameStatus) {
        this.nameStatus = nameStatus;
    }

    public UUID getCreatorAccountId() {
        return creatorAccountId;
    }

    public void setCreatorAccountId(UUID creatorAccountId) {
        this.creatorAccountId = creatorAccountId;
    }

    public UUID getCreatorCharacterId() {
        return creatorCharacterId;
    }

    public void setCreatorCharacterId(UUID creatorCharacterId) {
        this.creatorCharacterId = creatorCharacterId;
    }

    public String getCreatorNameSnapshot() {
        return creatorNameSnapshot;
    }

    public void setCreatorNameSnapshot(String creatorNameSnapshot) {
        this.creatorNameSnapshot = creatorNameSnapshot;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getCoreId() {
        return coreId;
    }

    public void setCoreId(String coreId) {
        this.coreId = coreId;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public JsonNode getModifierIds() {
        return modifierIds;
    }

    public void setModifierIds(JsonNode modifierIds) {
        this.modifierIds = modifierIds;
    }

    public String getMutationModifierId() {
        return mutationModifierId;
    }

    public void setMutationModifierId(String mutationModifierId) {
        this.mutationModifierId = mutationModifierId;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public BigDecimal getQ() {
        return q;
    }

    public void setQ(BigDecimal q) {
        this.q = q;
    }

    public BigDecimal getSuppressFactor() {
        return suppressFactor;
    }

    public void setSuppressFactor(BigDecimal suppressFactor) {
        this.suppressFactor = suppressFactor;
    }

    public JsonNode getSkillDef() {
        return skillDef;
    }

    public void setSkillDef(JsonNode skillDef) {
        this.skillDef = skillDef;
    }

    public BigDecimal getPowerScore() {
        return powerScore;
    }

    public void setPowerScore(BigDecimal powerScore) {
        this.powerScore = powerScore;
    }

    public Integer getCopiesMade() {
        return copiesMade;
    }

    public void setCopiesMade(Integer copiesMade) {
        this.copiesMade = copiesMade;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
