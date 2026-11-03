package vn.tienlo.api.lifecycle.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.pill_usage}; DDL: docs/design/03-lifecycle.md. */
@Entity
@Table(name = "pill_usage", schema = "game")
@IdClass(PillUsageEntity.Key.class)
public class PillUsageEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "item_id", nullable = false, length = 64)
    @Id
    private String itemId;

    @Column(name = "uses", nullable = false)
    private Integer uses;

    public PillUsageEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getUses() {
        return uses;
    }

    public void setUses(Integer uses) {
        this.uses = uses;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String itemId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof PillUsageEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(itemId, that.itemId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, itemId);
        }
    }
}
