package vn.tienlo.api.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.item_log}; DDL: docs/design/15-items-equipment.md. */
@Entity
@Table(name = "item_log", schema = "game")
public class ItemLogEntity {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_instance_id", nullable = false)
    private UUID itemInstanceId;

    @Column(name = "template_id", nullable = false, length = 64)
    private String templateId;

    @Column(name = "qty_delta", nullable = false)
    private Integer qtyDelta;

    @Column(name = "from_location", nullable = true, length = 16)
    private String fromLocation;

    @Column(name = "to_location", nullable = true, length = 16)
    private String toLocation;

    @Column(name = "from_owner", nullable = true)
    private UUID fromOwner;

    @Column(name = "to_owner", nullable = true)
    private UUID toOwner;

    @Column(name = "reason_code", nullable = false, length = 32)
    private String reasonCode;

    @Column(name = "ref_id", nullable = true, length = 80)
    private String refId;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ItemLogEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getItemInstanceId() {
        return itemInstanceId;
    }

    public void setItemInstanceId(UUID itemInstanceId) {
        this.itemInstanceId = itemInstanceId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Integer getQtyDelta() {
        return qtyDelta;
    }

    public void setQtyDelta(Integer qtyDelta) {
        this.qtyDelta = qtyDelta;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    public UUID getFromOwner() {
        return fromOwner;
    }

    public void setFromOwner(UUID fromOwner) {
        this.fromOwner = fromOwner;
    }

    public UUID getToOwner() {
        return toOwner;
    }

    public void setToOwner(UUID toOwner) {
        this.toOwner = toOwner;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
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
