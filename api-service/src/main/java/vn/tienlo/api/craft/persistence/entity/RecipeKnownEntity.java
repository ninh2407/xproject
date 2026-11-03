package vn.tienlo.api.craft.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.recipe_known}; DDL: docs/design/16-alchemy.md. */
@Entity
@Table(name = "recipe_known", schema = "game")
@IdClass(RecipeKnownEntity.Key.class)
public class RecipeKnownEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "recipe_id", nullable = false, length = 48)
    @Id
    private String recipeId;

    @Column(name = "crafts", nullable = false)
    private Integer crafts;

    @Column(name = "pity_fails", nullable = false)
    private Integer pityFails;

    @Column(name = "source", nullable = false, length = 16)
    private String source;

    @Column(name = "learned_at", nullable = false)
    private Instant learnedAt;

    public RecipeKnownEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public Integer getCrafts() {
        return crafts;
    }

    public void setCrafts(Integer crafts) {
        this.crafts = crafts;
    }

    public Integer getPityFails() {
        return pityFails;
    }

    public void setPityFails(Integer pityFails) {
        this.pityFails = pityFails;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getLearnedAt() {
        return learnedAt;
    }

    public void setLearnedAt(Instant learnedAt) {
        this.learnedAt = learnedAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String recipeId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof RecipeKnownEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(recipeId, that.recipeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, recipeId);
        }
    }
}
