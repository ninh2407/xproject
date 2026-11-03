package vn.tienlo.api.encounter.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.encounter_commit}; DDL: docs/design/23-encounters.md. */
@Entity
@Table(name = "encounter_commit", schema = "game")
public class EncounterCommitEntity {
    @Column(name = "commit_id", nullable = false, length = 80)
    @Id
    private String commitId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "outcome_keys", nullable = false, columnDefinition = "jsonb")
    private JsonNode outcomeKeys;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "results", nullable = false, columnDefinition = "jsonb")
    private JsonNode results;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public EncounterCommitEntity() {}

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public JsonNode getOutcomeKeys() {
        return outcomeKeys;
    }

    public void setOutcomeKeys(JsonNode outcomeKeys) {
        this.outcomeKeys = outcomeKeys;
    }

    public JsonNode getResults() {
        return results;
    }

    public void setResults(JsonNode results) {
        this.results = results;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
