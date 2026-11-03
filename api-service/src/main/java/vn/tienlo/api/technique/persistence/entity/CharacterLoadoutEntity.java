package vn.tienlo.api.technique.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.character_loadout}; DDL: docs/design/13-skills.md. */
@Entity
@Table(name = "character_loadout", schema = "game")
@IdClass(CharacterLoadoutEntity.Key.class)
public class CharacterLoadoutEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "slot", nullable = false, length = 1)
    @Id
    private String slot;

    @Column(name = "skill_ref", nullable = false, length = 64)
    private String skillRef;

    public CharacterLoadoutEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getSkillRef() {
        return skillRef;
    }

    public void setSkillRef(String skillRef) {
        this.skillRef = skillRef;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String slot;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CharacterLoadoutEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(slot, that.slot);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, slot);
        }
    }
}
