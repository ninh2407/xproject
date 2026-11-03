package vn.tienlo.api.world.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.world_event_daily}; DDL: docs/design/24-world-events.md. */
@Entity
@Table(name = "world_event_daily", schema = "game")
@IdClass(WorldEventDailyEntity.Key.class)
public class WorldEventDailyEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "day", nullable = false)
    @Id
    private LocalDate day;

    @Column(name = "full_rewards", nullable = false)
    private Short fullRewards;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "world_boss_claims", nullable = false, columnDefinition = "jsonb")
    private JsonNode worldBossClaims;

    public WorldEventDailyEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public Short getFullRewards() {
        return fullRewards;
    }

    public void setFullRewards(Short fullRewards) {
        this.fullRewards = fullRewards;
    }

    public JsonNode getWorldBossClaims() {
        return worldBossClaims;
    }

    public void setWorldBossClaims(JsonNode worldBossClaims) {
        this.worldBossClaims = worldBossClaims;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public LocalDate day;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof WorldEventDailyEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(day, that.day);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, day);
        }
    }
}
