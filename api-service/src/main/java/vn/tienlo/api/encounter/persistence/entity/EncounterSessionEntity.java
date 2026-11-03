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

/** DB-R-011 mapping for {@code game.encounter_session}; DDL: docs/design/23-encounters.md. */
@Entity
@Table(name = "encounter_session", schema = "game")
public class EncounterSessionEntity {
    @Column(name = "session_id", nullable = false)
    @Id
    private UUID sessionId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "encounter_id", nullable = false, length = 64)
    private String encounterId;

    @Column(name = "encounter_version", nullable = false)
    private Integer encounterVersion;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @Column(name = "trigger_type", nullable = false, length = 24)
    private String triggerType;

    @Column(name = "trigger_ref", nullable = true, length = 64)
    private String triggerRef;

    @Column(name = "map_id", nullable = false, length = 32)
    private String mapId;

    @Column(name = "rng_seed", nullable = false, length = 64)
    private String rngSeed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ink_state", nullable = true, columnDefinition = "jsonb")
    private JsonNode inkState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "committed_keys", nullable = false, columnDefinition = "jsonb")
    private JsonNode committedKeys;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "check_results", nullable = false, columnDefinition = "jsonb")
    private JsonNode checkResults;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "offered_until", nullable = false)
    private Instant offeredUntil;

    @Column(name = "resolved_at", nullable = true)
    private Instant resolvedAt;

    public EncounterSessionEntity() {}

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public Integer getEncounterVersion() {
        return encounterVersion;
    }

    public void setEncounterVersion(Integer encounterVersion) {
        this.encounterVersion = encounterVersion;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerRef() {
        return triggerRef;
    }

    public void setTriggerRef(String triggerRef) {
        this.triggerRef = triggerRef;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getRngSeed() {
        return rngSeed;
    }

    public void setRngSeed(String rngSeed) {
        this.rngSeed = rngSeed;
    }

    public JsonNode getInkState() {
        return inkState;
    }

    public void setInkState(JsonNode inkState) {
        this.inkState = inkState;
    }

    public JsonNode getCommittedKeys() {
        return committedKeys;
    }

    public void setCommittedKeys(JsonNode committedKeys) {
        this.committedKeys = committedKeys;
    }

    public JsonNode getCheckResults() {
        return checkResults;
    }

    public void setCheckResults(JsonNode checkResults) {
        this.checkResults = checkResults;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getOfferedUntil() {
        return offeredUntil;
    }

    public void setOfferedUntil(Instant offeredUntil) {
        this.offeredUntil = offeredUntil;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

}
