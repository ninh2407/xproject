package vn.tienlo.api.social.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.block_entry}; DDL: docs/design/20-social.md. */
@Entity
@Table(name = "block_entry", schema = "game")
@IdClass(BlockEntryEntity.Key.class)
public class BlockEntryEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "blocked_account_id", nullable = false)
    @Id
    private UUID blockedAccountId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public BlockEntryEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getBlockedAccountId() {
        return blockedAccountId;
    }

    public void setBlockedAccountId(UUID blockedAccountId) {
        this.blockedAccountId = blockedAccountId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public UUID blockedAccountId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof BlockEntryEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(blockedAccountId, that.blockedAccountId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, blockedAccountId);
        }
    }
}
