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

/** DB-R-011 mapping for {@code game.chat_mute}; DDL: docs/design/20-social.md. */
@Entity
@Table(name = "chat_mute", schema = "game")
@IdClass(ChatMuteEntity.Key.class)
public class ChatMuteEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "until_at", nullable = false)
    private Instant untilAt;

    @Column(name = "reason", nullable = true, length = 48)
    private String reason;

    @Column(name = "by_gm", nullable = true)
    private UUID byGm;

    @Column(name = "created_at", nullable = false)
    @Id
    private Instant createdAt;

    public ChatMuteEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Instant getUntilAt() {
        return untilAt;
    }

    public void setUntilAt(Instant untilAt) {
        this.untilAt = untilAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getByGm() {
        return byGm;
    }

    public void setByGm(UUID byGm) {
        this.byGm = byGm;
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
        public Instant createdAt;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ChatMuteEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(createdAt, that.createdAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, createdAt);
        }
    }
}
