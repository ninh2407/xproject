package vn.tienlo.api.security.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.purchase_receipt}; DDL: docs/tech/07-security.md. */
@Entity
@Table(name = "purchase_receipt", schema = "game")
public class PurchaseReceiptEntity {
    @Column(name = "transaction_id", nullable = false, length = 128)
    @Id
    private String transactionId;

    @Column(name = "store", nullable = false, length = 16)
    private String store;

    @Column(name = "product_id", nullable = false, length = 128)
    private String productId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "verified_at", nullable = true)
    private Instant verifiedAt;

    @Column(name = "refunded_at", nullable = true)
    private Instant refundedAt;

    public PurchaseReceiptEntity() {}

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(Instant refundedAt) {
        this.refundedAt = refundedAt;
    }

}
