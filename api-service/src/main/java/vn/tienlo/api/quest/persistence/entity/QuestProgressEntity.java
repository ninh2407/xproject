package vn.tienlo.api.quest.persistence.entity;

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

/** DB-R-011 mapping for {@code game.quest_progress}; DDL: docs/design/21-quests.md. */
@Entity
@Table(name = "quest_progress", schema = "game")
@IdClass(QuestProgressEntity.Key.class)
public class QuestProgressEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "quest_id", nullable = false, length = 64)
    @Id
    private String questId;

    @Column(name = "period_key", nullable = false, length = 16)
    @Id
    private String periodKey;

    @Column(name = "quest_version", nullable = false)
    private Integer questVersion;

    @Column(name = "state", nullable = false, length = 20)
    private String state;

    @Column(name = "step_index", nullable = false)
    private Short stepIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "counters", nullable = false, columnDefinition = "jsonb")
    private JsonNode counters;

    @Column(name = "branch", nullable = true, length = 32)
    private String branch;

    @Column(name = "tracked", nullable = false)
    private Boolean tracked;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at", nullable = true)
    private Instant completedAt;

    public QuestProgressEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public String getPeriodKey() {
        return periodKey;
    }

    public void setPeriodKey(String periodKey) {
        this.periodKey = periodKey;
    }

    public Integer getQuestVersion() {
        return questVersion;
    }

    public void setQuestVersion(Integer questVersion) {
        this.questVersion = questVersion;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Short getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(Short stepIndex) {
        this.stepIndex = stepIndex;
    }

    public JsonNode getCounters() {
        return counters;
    }

    public void setCounters(JsonNode counters) {
        this.counters = counters;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Boolean getTracked() {
        return tracked;
    }

    public void setTracked(Boolean tracked) {
        this.tracked = tracked;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String questId;
        public String periodKey;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof QuestProgressEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(questId, that.questId) && Objects.equals(periodKey, that.periodKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, questId, periodKey);
        }
    }
}
