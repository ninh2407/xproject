package vn.tienlo.api.cultivation.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.meditation_report}; DDL: docs/design/10-cultivation.md. */
@Entity
@Table(name = "meditation_report", schema = "game")
public class MeditationReportEntity {
    @Column(name = "report_id", nullable = false, length = 64)
    @Id
    private String reportId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @Column(name = "window_end", nullable = false)
    private Instant windowEnd;

    @Column(name = "real_sec", nullable = false)
    private Integer realSec;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public MeditationReportEntity() {}

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

    public Integer getRealSec() {
        return realSec;
    }

    public void setRealSec(Integer realSec) {
        this.realSec = realSec;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
