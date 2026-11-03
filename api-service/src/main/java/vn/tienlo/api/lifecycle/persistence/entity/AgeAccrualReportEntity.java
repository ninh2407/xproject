package vn.tienlo.api.lifecycle.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.age_accrual_report}; DDL: docs/design/03-lifecycle.md. */
@Entity
@Table(name = "age_accrual_report", schema = "game")
public class AgeAccrualReportEntity {
    @Column(name = "report_id", nullable = false, length = 64)
    @Id
    private String reportId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @Column(name = "window_end", nullable = false)
    private Instant windowEnd;

    @Column(name = "effective_sec", nullable = false, precision = 12, scale = 2)
    private BigDecimal effectiveSec;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public AgeAccrualReportEntity() {}

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public Instant getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(Instant windowStart) {
        this.windowStart = windowStart;
    }

    public Instant getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(Instant windowEnd) {
        this.windowEnd = windowEnd;
    }

    public BigDecimal getEffectiveSec() {
        return effectiveSec;
    }

    public void setEffectiveSec(BigDecimal effectiveSec) {
        this.effectiveSec = effectiveSec;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
