package vn.tienlo.api.talent.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.character_talent}; DDL: docs/design/04-talents.md. */
@Entity
@Table(name = "character_talent", schema = "game")
@IdClass(CharacterTalentEntity.Key.class)
public class CharacterTalentEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "talent_id", nullable = false, length = 48)
    @Id
    private String talentId;

    @Column(name = "source", nullable = false, length = 16)
    private String source;

    @Column(name = "slot", nullable = false)
    private Integer slot;

    @Column(name = "revealed", nullable = false)
    private Boolean revealed;

    @Column(name = "ref_id", nullable = true, length = 64)
    private String refId;

    @Column(name = "acquired_at", nullable = false)
    private Instant acquiredAt;

    public CharacterTalentEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getTalentId() {
        return talentId;
    }

    public void setTalentId(String talentId) {
        this.talentId = talentId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    public Boolean getRevealed() {
        return revealed;
    }

    public void setRevealed(Boolean revealed) {
        this.revealed = revealed;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public Instant getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(Instant acquiredAt) {
        this.acquiredAt = acquiredAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String talentId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CharacterTalentEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(talentId, that.talentId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, talentId);
        }
    }
}
