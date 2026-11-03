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

/** DB-R-011 mapping for {@code game.friendship}; DDL: docs/design/20-social.md. */
@Entity
@Table(name = "friendship", schema = "game")
@IdClass(FriendshipEntity.Key.class)
public class FriendshipEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "friend_account_id", nullable = false)
    @Id
    private UUID friendAccountId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public FriendshipEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getFriendAccountId() {
        return friendAccountId;
    }

    public void setFriendAccountId(UUID friendAccountId) {
        this.friendAccountId = friendAccountId;
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
        public UUID friendAccountId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof FriendshipEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(friendAccountId, that.friendAccountId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, friendAccountId);
        }
    }
}
