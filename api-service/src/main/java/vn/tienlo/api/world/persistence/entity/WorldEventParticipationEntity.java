package vn.tienlo.api.world.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.world_event_participation}; DDL: docs/design/24-world-events.md. */
@Entity
@Table(name = "world_event_participation", schema = "game")
@IdClass(WorldEventParticipationEntity.Key.class)
public class WorldEventParticipationEntity {
    @Column(name = "instance_id", nullable = false, length = 64)
    @Id
    private String instanceId;

    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "presence_sec", nullable = false)
    private Integer presenceSec;

    @Column(name = "tier", nullable = true, length = 8)
    private String tier;

    @Column(name = "rewarded", nullable = false)
    private Boolean rewarded;

    public WorldEventParticipationEntity() {}

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getPresenceSec() {
        return presenceSec;
    }

    public void setPresenceSec(Integer presenceSec) {
        this.presenceSec = presenceSec;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public Boolean getRewarded() {
        return rewarded;
    }

    public void setRewarded(Boolean rewarded) {
        this.rewarded = rewarded;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public String instanceId;
        public UUID characterId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof WorldEventParticipationEntity.Key that)) return false;
            return Objects.equals(instanceId, that.instanceId) && Objects.equals(characterId, that.characterId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(instanceId, characterId);
        }
    }
}
