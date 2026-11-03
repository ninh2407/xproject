package vn.tienlo.api.danger.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.danger_run_member}; DDL: docs/design/27-danger-zones.md. */
@Entity
@Table(name = "danger_run_member", schema = "game")
@IdClass(DangerRunMemberEntity.Key.class)
public class DangerRunMemberEntity {
    @Column(name = "run_id", nullable = false)
    @Id
    private UUID runId;

    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "state", nullable = false, length = 24)
    private String state;

    @Column(name = "confirmed", nullable = false)
    private Boolean confirmed;

    @Column(name = "downed_count", nullable = false)
    private Integer downedCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "carried_snapshot", nullable = true, columnDefinition = "jsonb")
    private JsonNode carriedSnapshot;

    @Column(name = "entered_at", nullable = true)
    private Instant enteredAt;

    @Column(name = "resolved_at", nullable = true)
    private Instant resolvedAt;

    @Column(name = "needs_review", nullable = false)
    private Boolean needsReview;

    public DangerRunMemberEntity() {}

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Integer getDownedCount() {
        return downedCount;
    }

    public void setDownedCount(Integer downedCount) {
        this.downedCount = downedCount;
    }

    public JsonNode getCarriedSnapshot() {
        return carriedSnapshot;
    }

    public void setCarriedSnapshot(JsonNode carriedSnapshot) {
        this.carriedSnapshot = carriedSnapshot;
    }

    public Instant getEnteredAt() {
        return enteredAt;
    }

    public void setEnteredAt(Instant enteredAt) {
        this.enteredAt = enteredAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Boolean getNeedsReview() {
        return needsReview;
    }

    public void setNeedsReview(Boolean needsReview) {
        this.needsReview = needsReview;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID runId;
        public UUID characterId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof DangerRunMemberEntity.Key that)) return false;
            return Objects.equals(runId, that.runId) && Objects.equals(characterId, that.characterId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(runId, characterId);
        }
    }
}
