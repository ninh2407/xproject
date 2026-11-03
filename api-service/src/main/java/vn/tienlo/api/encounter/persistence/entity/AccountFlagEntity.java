package vn.tienlo.api.encounter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.account_flag}; DDL: docs/design/23-encounters.md. */
@Entity
@Table(name = "account_flag", schema = "game")
@IdClass(AccountFlagEntity.Key.class)
public class AccountFlagEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "flag", nullable = false, length = 64)
    @Id
    private String flag;

    @Column(name = "set_at", nullable = false)
    private Instant setAt;

    public AccountFlagEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Instant getSetAt() {
        return setAt;
    }

    public void setSetAt(Instant setAt) {
        this.setAt = setAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String flag;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof AccountFlagEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(flag, that.flag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, flag);
        }
    }
}
