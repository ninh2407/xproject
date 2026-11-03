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

/** DB-R-011 mapping for {@code game.character_technique}; DDL: docs/design/13-skills.md. */
@Entity
@Table(name = "character_technique", schema = "game")
@IdClass(CharacterTechniqueEntity.Key.class)
public class CharacterTechniqueEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "ref_id", nullable = false, length = 64)
    @Id
    private String refId;

    @Column(name = "kind", nullable = false, length = 8)
    private String kind;

    @Column(name = "mastery_level", nullable = false)
    private Short masteryLevel;

    @Column(name = "mastery_points", nullable = false)
    private Integer masteryPoints;

    @Column(name = "source", nullable = false, length = 16)
    private String source;

    @Column(name = "learned_at", nullable = false)
    private Instant learnedAt;

    public CharacterTechniqueEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Short getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(Short masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    public Integer getMasteryPoints() {
        return masteryPoints;
    }

    public void setMasteryPoints(Integer masteryPoints) {
        this.masteryPoints = masteryPoints;
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
        public String refId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CharacterTechniqueEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(refId, that.refId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, refId);
        }
    }
}
