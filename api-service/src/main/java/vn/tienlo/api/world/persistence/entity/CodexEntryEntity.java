package vn.tienlo.api.world.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.codex_entry}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "codex_entry", schema = "game")
@IdClass(CodexEntryEntity.Key.class)
public class CodexEntryEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "entry_type", nullable = false, length = 16)
    @Id
    private String entryType;

    @Column(name = "entry_id", nullable = false, length = 48)
    @Id
    private String entryId;

    @Column(name = "level", nullable = false)
    private Short level;

    @Column(name = "progress", nullable = false)
    private Integer progress;

    @Column(name = "unlocked_at", nullable = true)
    private Instant unlockedAt;

    public CodexEntryEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public Short getLevel() {
        return level;
    }

    public void setLevel(Short level) {
        this.level = level;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Instant getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(Instant unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String entryType;
        public String entryId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CodexEntryEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(entryType, that.entryType) && Objects.equals(entryId, that.entryId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, entryType, entryId);
        }
    }
}
