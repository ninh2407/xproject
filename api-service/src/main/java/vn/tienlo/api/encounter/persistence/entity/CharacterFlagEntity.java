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

/** DB-R-011 mapping for {@code game.character_flag}; DDL: docs/design/23-encounters.md. */
@Entity
@Table(name = "character_flag", schema = "game")
@IdClass(CharacterFlagEntity.Key.class)
public class CharacterFlagEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "flag", nullable = false, length = 64)
    @Id
    private String flag;

    @Column(name = "set_at", nullable = false)
    private Instant setAt;

    public CharacterFlagEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Instant getSetAt() {
        return setAt;
    }

    public void setSetAt(Instant setAt) {
        this.setAt = setAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String flag;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CharacterFlagEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(flag, that.flag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, flag);
        }
    }
}
