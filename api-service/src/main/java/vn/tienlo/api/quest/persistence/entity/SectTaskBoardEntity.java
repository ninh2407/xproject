package vn.tienlo.api.quest.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.sect_task_board}; DDL: docs/design/21-quests.md. */
@Entity
@Table(name = "sect_task_board", schema = "game")
@IdClass(SectTaskBoardEntity.Key.class)
public class SectTaskBoardEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "day", nullable = false)
    @Id
    private LocalDate day;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "slots", nullable = false, columnDefinition = "jsonb")
    private JsonNode slots;

    @Column(name = "rerolls_used", nullable = false)
    private Short rerollsUsed;

    public SectTaskBoardEntity() {}

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

    public JsonNode getSlots() {
        return slots;
    }

    public void setSlots(JsonNode slots) {
        this.slots = slots;
    }

    public Short getRerollsUsed() {
        return rerollsUsed;
    }

    public void setRerollsUsed(Short rerollsUsed) {
        this.rerollsUsed = rerollsUsed;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public LocalDate day;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof SectTaskBoardEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(day, that.day);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, day);
        }
    }
}
