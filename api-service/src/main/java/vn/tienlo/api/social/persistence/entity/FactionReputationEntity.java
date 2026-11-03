package vn.tienlo.api.social.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.faction_reputation}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "faction_reputation", schema = "game")
public class FactionReputationEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "chinh", nullable = false)
    private Integer chinh;

    @Column(name = "ta", nullable = false)
    private Integer ta;

    @Column(name = "ma", nullable = false)
    private Integer ma;

    @Column(name = "daily_day", nullable = true)
    private LocalDate dailyDay;

    @Column(name = "daily_chinh", nullable = false)
    private Integer dailyChinh;

    @Column(name = "daily_ta", nullable = false)
    private Integer dailyTa;

    @Column(name = "daily_ma", nullable = false)
    private Integer dailyMa;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "best", nullable = false, columnDefinition = "jsonb")
    private JsonNode best;

    public FactionReputationEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public Integer getChinh() {
        return chinh;
    }

    public void setChinh(Integer chinh) {
        this.chinh = chinh;
    }

    public Integer getTa() {
        return ta;
    }

    public void setTa(Integer ta) {
        this.ta = ta;
    }

    public Integer getMa() {
        return ma;
    }

    public void setMa(Integer ma) {
        this.ma = ma;
    }

    public LocalDate getDailyDay() {
        return dailyDay;
    }

    public void setDailyDay(LocalDate dailyDay) {
        this.dailyDay = dailyDay;
    }

    public Integer getDailyChinh() {
        return dailyChinh;
    }

    public void setDailyChinh(Integer dailyChinh) {
        this.dailyChinh = dailyChinh;
    }

    public Integer getDailyTa() {
        return dailyTa;
    }

    public void setDailyTa(Integer dailyTa) {
        this.dailyTa = dailyTa;
    }

    public Integer getDailyMa() {
        return dailyMa;
    }

    public void setDailyMa(Integer dailyMa) {
        this.dailyMa = dailyMa;
    }

    public JsonNode getBest() {
        return best;
    }

    public void setBest(JsonNode best) {
        this.best = best;
    }

}
