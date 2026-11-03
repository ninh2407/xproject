package vn.tienlo.api.character.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.character_status}; DDL: docs/design/12-stats-formulas.md. */
@Entity
@Table(name = "character_status", schema = "game")
@IdClass(CharacterStatusEntity.Key.class)
public class CharacterStatusEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "status_id", nullable = false, length = 48)
    @Id
    private String statusId;

    @Column(name = "stacks", nullable = false)
    private Integer stacks;

    @Column(name = "expires_at", nullable = true)
    private Instant expiresAt;

    @Column(name = "source_ref", nullable = true, length = 80)
    private String sourceRef;

    public CharacterStatusEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public Integer getStacks() {
        return stacks;
    }

    public void setStacks(Integer stacks) {
        this.stacks = stacks;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String statusId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CharacterStatusEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(statusId, that.statusId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, statusId);
        }
    }
}
