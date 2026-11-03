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

/** DB-R-011 mapping for {@code game.encounter_history}; DDL: docs/design/23-encounters.md. */
@Entity
@Table(name = "encounter_history", schema = "game")
@IdClass(EncounterHistoryEntity.Key.class)
public class EncounterHistoryEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "encounter_id", nullable = false, length = 64)
    @Id
    private String encounterId;

    @Column(name = "times", nullable = false)
    private Integer times;

    @Column(name = "last_offered_at", nullable = false)
    private Instant lastOfferedAt;

    @Column(name = "cooldown_until", nullable = true)
    private Instant cooldownUntil;

    public EncounterHistoryEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public Instant getLastOfferedAt() {
        return lastOfferedAt;
    }

    public void setLastOfferedAt(Instant lastOfferedAt) {
        this.lastOfferedAt = lastOfferedAt;
    }

    public Instant getCooldownUntil() {
        return cooldownUntil;
    }

    public void setCooldownUntil(Instant cooldownUntil) {
        this.cooldownUntil = cooldownUntil;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String encounterId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof EncounterHistoryEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(encounterId, that.encounterId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, encounterId);
        }
    }
}
