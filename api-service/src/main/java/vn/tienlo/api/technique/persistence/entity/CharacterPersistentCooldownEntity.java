package vn.tienlo.api.technique.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.character_persistent_cooldown}; DDL: docs/design/13-skills.md. */
@Entity
@Table(name = "character_persistent_cooldown", schema = "game")
@IdClass(CharacterPersistentCooldownEntity.Key.class)
public class CharacterPersistentCooldownEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "skill_ref", nullable = false, length = 64)
    @Id
    private String skillRef;

    @Column(name = "ready_at", nullable = false)
    private Instant readyAt;

    public CharacterPersistentCooldownEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getSkillRef() {
        return skillRef;
    }

    public void setSkillRef(String skillRef) {
        this.skillRef = skillRef;
    }

    public Instant getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(Instant readyAt) {
        this.readyAt = readyAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String skillRef;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CharacterPersistentCooldownEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(skillRef, that.skillRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, skillRef);
        }
    }
}
