package vn.tienlo.api.technique.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.technique_forge_sandbox_use}; DDL: docs/design/14-technique-carfting.md. */
@Entity
@Table(name = "technique_forge_sandbox_use", schema = "game")
@IdClass(TechniqueForgeSandboxUseEntity.Key.class)
public class TechniqueForgeSandboxUseEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "day", nullable = false)
    @Id
    private LocalDate day;

    @Column(name = "runs", nullable = false)
    private Short runs;

    public TechniqueForgeSandboxUseEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public Short getRuns() {
        return runs;
    }

    public void setRuns(Short runs) {
        this.runs = runs;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public LocalDate day;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof TechniqueForgeSandboxUseEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(day, that.day);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, day);
        }
    }
}
