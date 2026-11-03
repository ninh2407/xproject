package vn.tienlo.api.home.persistence.entity;

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

/** DB-R-011 mapping for {@code game.sect_prestige_week}; DDL: docs/design/28-caves-sect-sites.md. */
@Entity
@Table(name = "sect_prestige_week", schema = "game")
@IdClass(SectPrestigeWeekEntity.Key.class)
public class SectPrestigeWeekEntity {
    @Column(name = "sect_id", nullable = false)
    @Id
    private UUID sectId;

    @Column(name = "week_start", nullable = false)
    @Id
    private LocalDate weekStart;

    @Column(name = "points", nullable = false)
    private Integer points;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "breakdown", nullable = false, columnDefinition = "jsonb")
    private JsonNode breakdown;

    public SectPrestigeWeekEntity() {}

    public UUID getSectId() {
        return sectId;
    }

    public void setSectId(UUID sectId) {
        this.sectId = sectId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public JsonNode getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(JsonNode breakdown) {
        this.breakdown = breakdown;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID sectId;
        public LocalDate weekStart;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof SectPrestigeWeekEntity.Key that)) return false;
            return Objects.equals(sectId, that.sectId) && Objects.equals(weekStart, that.weekStart);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sectId, weekStart);
        }
    }
}
