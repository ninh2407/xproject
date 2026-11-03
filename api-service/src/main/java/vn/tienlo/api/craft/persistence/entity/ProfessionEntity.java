package vn.tienlo.api.craft.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.profession}; DDL: docs/design/16-alchemy.md. */
@Entity
@Table(name = "profession", schema = "game")
@IdClass(ProfessionEntity.Key.class)
public class ProfessionEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "kind", nullable = false, length = 16)
    @Id
    private String kind;

    @Column(name = "level", nullable = false)
    private Short level;

    @Column(name = "exp", nullable = false)
    private Integer exp;

    public ProfessionEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Short getLevel() {
        return level;
    }

    public void setLevel(Short level) {
        this.level = level;
    }

    public Integer getExp() {
        return exp;
    }

    public void setExp(Integer exp) {
        this.exp = exp;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String kind;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ProfessionEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(kind, that.kind);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, kind);
        }
    }
}
