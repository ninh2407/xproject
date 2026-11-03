package vn.tienlo.api.home.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.sect_prestige_member_week}; DDL: docs/design/28-caves-sect-sites.md. */
@Entity
@Table(name = "sect_prestige_member_week", schema = "game")
@IdClass(SectPrestigeMemberWeekEntity.Key.class)
public class SectPrestigeMemberWeekEntity {
    @Column(name = "sect_id", nullable = false)
    @Id
    private UUID sectId;

    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "week_start", nullable = false)
    @Id
    private LocalDate weekStart;

    @Column(name = "source", nullable = false, length = 24)
    @Id
    private String source;

    @Column(name = "points", nullable = false)
    private Integer points;

    public SectPrestigeMemberWeekEntity() {}

    public UUID getSectId() {
        return sectId;
    }

    public void setSectId(UUID sectId) {
        this.sectId = sectId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID sectId;
        public UUID accountId;
        public LocalDate weekStart;
        public String source;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof SectPrestigeMemberWeekEntity.Key that)) return false;
            return Objects.equals(sectId, that.sectId) && Objects.equals(accountId, that.accountId) && Objects.equals(weekStart, that.weekStart) && Objects.equals(source, that.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sectId, accountId, weekStart, source);
        }
    }
}
