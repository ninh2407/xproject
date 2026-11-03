package vn.tienlo.api.beast.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.beast_boss_capture}; DDL: docs/design/26-spirit-beasts.md. */
@Entity
@Table(name = "beast_boss_capture", schema = "game")
@IdClass(BeastBossCaptureEntity.Key.class)
public class BeastBossCaptureEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "boss_monster_id", nullable = false, length = 48)
    @Id
    private String bossMonsterId;

    @Column(name = "fails", nullable = false)
    private Short fails;

    @Column(name = "attempts_day", nullable = true)
    private LocalDate attemptsDay;

    @Column(name = "attempts_used", nullable = false)
    private Short attemptsUsed;

    public BeastBossCaptureEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getBossMonsterId() {
        return bossMonsterId;
    }

    public void setBossMonsterId(String bossMonsterId) {
        this.bossMonsterId = bossMonsterId;
    }

    public Short getFails() {
        return fails;
    }

    public void setFails(Short fails) {
        this.fails = fails;
    }

    public LocalDate getAttemptsDay() {
        return attemptsDay;
    }

    public void setAttemptsDay(LocalDate attemptsDay) {
        this.attemptsDay = attemptsDay;
    }

    public Short getAttemptsUsed() {
        return attemptsUsed;
    }

    public void setAttemptsUsed(Short attemptsUsed) {
        this.attemptsUsed = attemptsUsed;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String bossMonsterId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof BeastBossCaptureEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(bossMonsterId, that.bossMonsterId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, bossMonsterId);
        }
    }
}
