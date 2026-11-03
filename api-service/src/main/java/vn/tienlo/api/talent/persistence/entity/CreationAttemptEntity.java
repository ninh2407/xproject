package vn.tienlo.api.talent.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.creation_attempt}; DDL: docs/design/04-talents.md. */
@Entity
@Table(name = "creation_attempt", schema = "game")
public class CreationAttemptEntity {
    @Column(name = "attempt_id", nullable = false)
    @Id
    private UUID attemptId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "generation", nullable = false)
    private Integer generation;

    @Column(name = "rolls_allowed", nullable = false)
    private Integer rollsAllowed;

    @Column(name = "rolls_used", nullable = false)
    private Integer rollsUsed;

    @Column(name = "lock_available", nullable = false)
    private Boolean lockAvailable;

    @Column(name = "no_rare_plus_streak", nullable = false)
    private Integer noRarePlusStreak;

    @Column(name = "confirmed_roll_id", nullable = true)
    private UUID confirmedRollId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CreationAttemptEntity() {}

    public UUID getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    public Integer getRollsAllowed() {
        return rollsAllowed;
    }

    public void setRollsAllowed(Integer rollsAllowed) {
        this.rollsAllowed = rollsAllowed;
    }

    public Integer getRollsUsed() {
        return rollsUsed;
    }

    public void setRollsUsed(Integer rollsUsed) {
        this.rollsUsed = rollsUsed;
    }

    public Boolean getLockAvailable() {
        return lockAvailable;
    }

    public void setLockAvailable(Boolean lockAvailable) {
        this.lockAvailable = lockAvailable;
    }

    public Integer getNoRarePlusStreak() {
        return noRarePlusStreak;
    }

    public void setNoRarePlusStreak(Integer noRarePlusStreak) {
        this.noRarePlusStreak = noRarePlusStreak;
    }

    public UUID getConfirmedRollId() {
        return confirmedRollId;
    }

    public void setConfirmedRollId(UUID confirmedRollId) {
        this.confirmedRollId = confirmedRollId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
