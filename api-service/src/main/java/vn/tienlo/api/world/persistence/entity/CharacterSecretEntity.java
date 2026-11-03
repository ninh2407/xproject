package vn.tienlo.api.world.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.character_secret}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "character_secret", schema = "game")
@IdClass(CharacterSecretEntity.Key.class)
public class CharacterSecretEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "group_id", nullable = false, length = 48)
    @Id
    private String groupId;

    @Column(name = "position_index", nullable = false)
    private Short positionIndex;

    @Column(name = "found_at", nullable = true)
    private Instant foundAt;

    public CharacterSecretEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Short getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(Short positionIndex) {
        this.positionIndex = positionIndex;
    }

    public Instant getFoundAt() {
        return foundAt;
    }

    public void setFoundAt(Instant foundAt) {
        this.foundAt = foundAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String groupId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CharacterSecretEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(groupId, that.groupId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, groupId);
        }
    }
}
