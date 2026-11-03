package vn.tienlo.api.monetization.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.season_pass_progress}; DDL: docs/design/50-monetization.md. */
@Entity
@Table(name = "season_pass_progress", schema = "game")
@IdClass(SeasonPassProgressEntity.Key.class)
public class SeasonPassProgressEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "season_id", nullable = false, length = 16)
    @Id
    private String seasonId;

    @Column(name = "premium", nullable = false)
    private Boolean premium;

    @Column(name = "xp", nullable = false)
    private Integer xp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "claimed", nullable = false, columnDefinition = "jsonb")
    private JsonNode claimed;

    public SeasonPassProgressEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public Boolean getPremium() {
        return premium;
    }

    public void setPremium(Boolean premium) {
        this.premium = premium;
    }

    public Integer getXp() {
        return xp;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    public JsonNode getClaimed() {
        return claimed;
    }

    public void setClaimed(JsonNode claimed) {
        this.claimed = claimed;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String seasonId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof SeasonPassProgressEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(seasonId, that.seasonId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, seasonId);
        }
    }
}
