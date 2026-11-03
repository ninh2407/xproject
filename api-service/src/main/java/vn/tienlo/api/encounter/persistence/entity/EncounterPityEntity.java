package vn.tienlo.api.encounter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.encounter_pity}; DDL: docs/design/23-encounters.md. */
@Entity
@Table(name = "encounter_pity", schema = "game")
public class EncounterPityEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "pity_miss_count", nullable = false)
    private Integer pityMissCount;

    @Column(name = "sessions_since_huyen_plus", nullable = false)
    private Integer sessionsSinceHuyenPlus;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EncounterPityEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public Integer getPityMissCount() {
        return pityMissCount;
    }

    public void setPityMissCount(Integer pityMissCount) {
        this.pityMissCount = pityMissCount;
    }

    public Integer getSessionsSinceHuyenPlus() {
        return sessionsSinceHuyenPlus;
    }

    public void setSessionsSinceHuyenPlus(Integer sessionsSinceHuyenPlus) {
        this.sessionsSinceHuyenPlus = sessionsSinceHuyenPlus;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
