package vn.tienlo.api.monetization.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.payment_order}; DDL: docs/design/50-monetization.md. */
@Entity
@Table(name = "payment_order", schema = "game")
public class PaymentOrderEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "pack_id", nullable = false, length = 32)
    private String packId;

    @Column(name = "provider", nullable = false, length = 24)
    private String provider;

    @Column(name = "amount_vnd", nullable = false)
    private Long amountVnd;

    @Column(name = "status", nullable = false, length = 12)
    private String status;

    @Column(name = "provider_ref", nullable = true, length = 128)
    private String providerRef;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "paid_at", nullable = true)
    private Instant paidAt;

    public PaymentOrderEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getPackId() {
        return packId;
    }

    public void setPackId(String packId) {
        this.packId = packId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Long getAmountVnd() {
        return amountVnd;
    }

    public void setAmountVnd(Long amountVnd) {
        this.amountVnd = amountVnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProviderRef() {
        return providerRef;
    }

    public void setProviderRef(String providerRef) {
        this.providerRef = providerRef;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

}
