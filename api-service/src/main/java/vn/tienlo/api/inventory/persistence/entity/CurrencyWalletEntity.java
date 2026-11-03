package vn.tienlo.api.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.currency_wallet}; DDL: docs/design/15-items-equipment.md. */
@Entity
@Table(name = "currency_wallet", schema = "game")
@IdClass(CurrencyWalletEntity.Key.class)
public class CurrencyWalletEntity {
    @Column(name = "owner_type", nullable = false, length = 16)
    @Id
    private String ownerType;

    @Column(name = "owner_id", nullable = false)
    @Id
    private UUID ownerId;

    @Column(name = "currency", nullable = false, length = 24)
    @Id
    private String currency;

    @Column(name = "balance", nullable = false)
    private Long balance;

    public CurrencyWalletEntity() {}

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public String ownerType;
        public UUID ownerId;
        public String currency;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CurrencyWalletEntity.Key that)) return false;
            return Objects.equals(ownerType, that.ownerType) && Objects.equals(ownerId, that.ownerId) && Objects.equals(currency, that.currency);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ownerType, ownerId, currency);
        }
    }
}
