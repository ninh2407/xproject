package vn.tienlo.api.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.character_quick_slot}; DDL: docs/design/15-items-equipment.md. */
@Entity
@Table(name = "character_quick_slot", schema = "game")
@IdClass(CharacterQuickSlotEntity.Key.class)
public class CharacterQuickSlotEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "slot", nullable = false)
    @Id
    private Short slot;

    @Column(name = "template_id", nullable = true, length = 64)
    private String templateId;

    public CharacterQuickSlotEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public Short getSlot() {
        return slot;
    }

    public void setSlot(Short slot) {
        this.slot = slot;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public Short slot;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CharacterQuickSlotEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(slot, that.slot);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, slot);
        }
    }
}
