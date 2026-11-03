package vn.tienlo.api.inventory.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.item_instance}; DDL: docs/design/15-items-equipment.md. */
@Entity
@Table(name = "item_instance", schema = "game")
public class ItemInstanceEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "template_id", nullable = false, length = 64)
    private String templateId;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "location", nullable = false, length = 16)
    private String location;

    @Column(name = "owner_character_id", nullable = true)
    private UUID ownerCharacterId;

    @Column(name = "owner_account_id", nullable = false)
    private UUID ownerAccountId;

    @Column(name = "slot_index", nullable = true)
    private Integer slotIndex;

    @Column(name = "equip_slot", nullable = true, length = 16)
    private String equipSlot;

    @Column(name = "bound", nullable = false)
    private Boolean bound;

    @Column(name = "grade", nullable = true, length = 8)
    private String grade;

    @Column(name = "quality", nullable = true, length = 8)
    private String quality;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "affixes", nullable = false, columnDefinition = "jsonb")
    private JsonNode affixes;

    @Column(name = "enhance_level", nullable = false)
    private Short enhanceLevel;

    @Column(name = "seal_band", nullable = true, length = 4)
    private String sealBand;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sockets", nullable = false, columnDefinition = "jsonb")
    private JsonNode sockets;

    @Column(name = "origin_character_id", nullable = true)
    private UUID originCharacterId;

    @Column(name = "danger_run_id", nullable = true)
    private UUID dangerRunId;

    @Column(name = "corpse_id", nullable = true)
    private UUID corpseId;

    @Column(name = "legacy_id", nullable = true)
    private UUID legacyId;

    @Column(name = "mail_id", nullable = true)
    private UUID mailId;

    @Column(name = "crafter_character_id", nullable = true)
    private UUID crafterCharacterId;

    @Column(name = "created_reason", nullable = false, length = 32)
    private String createdReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "version", nullable = false)
    @Version
    private Integer version;

    @Column(name = "crafted_ref_id", nullable = true, length = 16)
    private String craftedRefId;

    @Column(name = "spirit_born", nullable = false)
    private Boolean spiritBorn;

    public ItemInstanceEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public UUID getOwnerCharacterId() {
        return ownerCharacterId;
    }

    public void setOwnerCharacterId(UUID ownerCharacterId) {
        this.ownerCharacterId = ownerCharacterId;
    }

    public UUID getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(UUID ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    public Integer getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(Integer slotIndex) {
        this.slotIndex = slotIndex;
    }

    public String getEquipSlot() {
        return equipSlot;
    }

    public void setEquipSlot(String equipSlot) {
        this.equipSlot = equipSlot;
    }

    public Boolean getBound() {
        return bound;
    }

    public void setBound(Boolean bound) {
        this.bound = bound;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public JsonNode getAffixes() {
        return affixes;
    }

    public void setAffixes(JsonNode affixes) {
        this.affixes = affixes;
    }

    public Short getEnhanceLevel() {
        return enhanceLevel;
    }

    public void setEnhanceLevel(Short enhanceLevel) {
        this.enhanceLevel = enhanceLevel;
    }

    public String getSealBand() {
        return sealBand;
    }

    public void setSealBand(String sealBand) {
        this.sealBand = sealBand;
    }

    public JsonNode getSockets() {
        return sockets;
    }

    public void setSockets(JsonNode sockets) {
        this.sockets = sockets;
    }

    public UUID getOriginCharacterId() {
        return originCharacterId;
    }

    public void setOriginCharacterId(UUID originCharacterId) {
        this.originCharacterId = originCharacterId;
    }

    public UUID getDangerRunId() {
        return dangerRunId;
    }

    public void setDangerRunId(UUID dangerRunId) {
        this.dangerRunId = dangerRunId;
    }

    public UUID getCorpseId() {
        return corpseId;
    }

    public void setCorpseId(UUID corpseId) {
        this.corpseId = corpseId;
    }

    public UUID getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(UUID legacyId) {
        this.legacyId = legacyId;
    }

    public UUID getMailId() {
        return mailId;
    }

    public void setMailId(UUID mailId) {
        this.mailId = mailId;
    }

    public UUID getCrafterCharacterId() {
        return crafterCharacterId;
    }

    public void setCrafterCharacterId(UUID crafterCharacterId) {
        this.crafterCharacterId = crafterCharacterId;
    }

    public String getCreatedReason() {
        return createdReason;
    }

    public void setCreatedReason(String createdReason) {
        this.createdReason = createdReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCraftedRefId() {
        return craftedRefId;
    }

    public void setCraftedRefId(String craftedRefId) {
        this.craftedRefId = craftedRefId;
    }

    public Boolean getSpiritBorn() {
        return spiritBorn;
    }

    public void setSpiritBorn(Boolean spiritBorn) {
        this.spiritBorn = spiritBorn;
    }

}
