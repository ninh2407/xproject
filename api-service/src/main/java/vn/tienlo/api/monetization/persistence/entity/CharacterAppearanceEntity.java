package vn.tienlo.api.monetization.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.character_appearance}; DDL: docs/design/50-monetization.md. */
@Entity
@Table(name = "character_appearance", schema = "game")
public class CharacterAppearanceEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "slots", nullable = false, columnDefinition = "jsonb")
    private JsonNode slots;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CharacterAppearanceEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public JsonNode getSlots() {
        return slots;
    }

    public void setSlots(JsonNode slots) {
        this.slots = slots;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
