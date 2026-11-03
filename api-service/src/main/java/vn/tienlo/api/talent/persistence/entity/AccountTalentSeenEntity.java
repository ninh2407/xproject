package vn.tienlo.api.talent.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.account_talent_seen}; DDL: docs/design/04-talents.md. */
@Entity
@Table(name = "account_talent_seen", schema = "game")
@IdClass(AccountTalentSeenEntity.Key.class)
public class AccountTalentSeenEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "talent_id", nullable = false, length = 48)
    @Id
    private String talentId;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    public AccountTalentSeenEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getTalentId() {
        return talentId;
    }

    public void setTalentId(String talentId) {
        this.talentId = talentId;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(Instant firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String talentId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof AccountTalentSeenEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(talentId, that.talentId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, talentId);
        }
    }
}
