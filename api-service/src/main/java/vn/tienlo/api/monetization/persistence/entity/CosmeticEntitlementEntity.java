package vn.tienlo.api.monetization.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.cosmetic_entitlement}; DDL: docs/design/50-monetization.md. */
@Entity
@Table(name = "cosmetic_entitlement", schema = "game")
@IdClass(CosmeticEntitlementEntity.Key.class)
public class CosmeticEntitlementEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "cosmetic_id", nullable = false, length = 48)
    @Id
    private String cosmeticId;

    @Column(name = "source", nullable = false, length = 48)
    private String source;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    @Column(name = "revoked_at", nullable = true)
    private Instant revokedAt;

    public CosmeticEntitlementEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getCosmeticId() {
        return cosmeticId;
    }

    public void setCosmeticId(String cosmeticId) {
        this.cosmeticId = cosmeticId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(Instant grantedAt) {
        this.grantedAt = grantedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String cosmeticId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CosmeticEntitlementEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(cosmeticId, that.cosmeticId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, cosmeticId);
        }
    }
}
