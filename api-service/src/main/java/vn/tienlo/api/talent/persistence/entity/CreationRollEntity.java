package vn.tienlo.api.talent.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.creation_roll}; DDL: docs/design/04-talents.md. */
@Entity
@Table(name = "creation_roll", schema = "game")
public class CreationRollEntity {
    @Column(name = "roll_id", nullable = false)
    @Id
    private UUID rollId;

    @Column(name = "attempt_id", nullable = false)
    private UUID attemptId;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @Column(name = "background_id", nullable = false, length = 32)
    private String backgroundId;

    @Column(name = "locked_line", nullable = true, length = 16)
    private String lockedLine;

    @Column(name = "seed", nullable = false, length = 64)
    private String seed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", nullable = false, columnDefinition = "jsonb")
    private JsonNode result;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public CreationRollEntity() {}

    public UUID getRollId() {
        return rollId;
    }

    public void setRollId(UUID rollId) {
        this.rollId = rollId;
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public String getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(String backgroundId) {
        this.backgroundId = backgroundId;
    }

    public String getLockedLine() {
        return lockedLine;
    }

    public void setLockedLine(String lockedLine) {
        this.lockedLine = lockedLine;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public JsonNode getResult() {
        return result;
    }

    public void setResult(JsonNode result) {
        this.result = result;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
