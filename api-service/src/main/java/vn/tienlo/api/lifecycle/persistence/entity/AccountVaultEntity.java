package vn.tienlo.api.lifecycle.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.account_vault}; DDL: docs/design/03-lifecycle.md. */
@Entity
@Table(name = "account_vault", schema = "game")
@IdClass(AccountVaultEntity.Key.class)
public class AccountVaultEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "slot", nullable = false)
    @Id
    private Integer slot;

    @Column(name = "item_instance_id", nullable = false)
    private UUID itemInstanceId;

    public AccountVaultEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    public UUID getItemInstanceId() {
        return itemInstanceId;
    }

    public void setItemInstanceId(UUID itemInstanceId) {
        this.itemInstanceId = itemInstanceId;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public Integer slot;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof AccountVaultEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(slot, that.slot);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, slot);
        }
    }
}
