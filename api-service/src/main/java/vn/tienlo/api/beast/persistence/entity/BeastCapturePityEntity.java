package vn.tienlo.api.beast.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.beast_capture_pity}; DDL: docs/design/26-spirit-beasts.md. */
@Entity
@Table(name = "beast_capture_pity", schema = "game")
@IdClass(BeastCapturePityEntity.Key.class)
public class BeastCapturePityEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "species_id", nullable = false, length = 48)
    @Id
    private String speciesId;

    @Column(name = "fails", nullable = false)
    private Short fails;

    public BeastCapturePityEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public Short getFails() {
        return fails;
    }

    public void setFails(Short fails) {
        this.fails = fails;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String speciesId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof BeastCapturePityEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(speciesId, that.speciesId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, speciesId);
        }
    }
}
