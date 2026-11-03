package vn.tienlo.api.lifecycle.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.life_record}; DDL: docs/design/03-lifecycle.md. */
@Entity
@Table(name = "life_record", schema = "game")
public class LifeRecordEntity {
    @Column(name = "record_id", nullable = false)
    @Id
    private UUID recordId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary", nullable = false, columnDefinition = "jsonb")
    private JsonNode summary;

    @Column(name = "final_realm", nullable = false, length = 8)
    private String finalRealm;

    @Column(name = "age_years", nullable = false, precision = 8, scale = 2)
    private BigDecimal ageYears;

    @Column(name = "end_cause", nullable = false, length = 32)
    private String endCause;

    @Column(name = "rebirth_points", nullable = false)
    private Integer rebirthPoints;

    @Column(name = "ended_at", nullable = false)
    private Instant endedAt;

    public LifeRecordEntity() {}

    public UUID getRecordId() {
        return recordId;
    }

    public void setRecordId(UUID recordId) {
        this.recordId = recordId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public JsonNode getSummary() {
        return summary;
    }

    public void setSummary(JsonNode summary) {
        this.summary = summary;
    }

    public String getFinalRealm() {
        return finalRealm;
    }

    public void setFinalRealm(String finalRealm) {
        this.finalRealm = finalRealm;
    }

    public BigDecimal getAgeYears() {
        return ageYears;
    }

    public void setAgeYears(BigDecimal ageYears) {
        this.ageYears = ageYears;
    }

    public String getEndCause() {
        return endCause;
    }

    public void setEndCause(String endCause) {
        this.endCause = endCause;
    }

    public Integer getRebirthPoints() {
        return rebirthPoints;
    }

    public void setRebirthPoints(Integer rebirthPoints) {
        this.rebirthPoints = rebirthPoints;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

}
