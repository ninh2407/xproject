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

/** DB-R-011 mapping for {@code game.encounter_chain_progress}; DDL: docs/design/23-encounters.md. */
@Entity
@Table(name = "encounter_chain_progress", schema = "game")
@IdClass(EncounterChainProgressEntity.Key.class)
public class EncounterChainProgressEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "chain_id", nullable = false, length = 64)
    @Id
    private String chainId;

    @Column(name = "step_id", nullable = false, length = 16)
    private String stepId;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @Column(name = "step_expires_at", nullable = true)
    private Instant stepExpiresAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EncounterChainProgressEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Instant getStepExpiresAt() {
        return stepExpiresAt;
    }

    public void setStepExpiresAt(Instant stepExpiresAt) {
        this.stepExpiresAt = stepExpiresAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String chainId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof EncounterChainProgressEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(chainId, that.chainId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, chainId);
        }
    }
}
