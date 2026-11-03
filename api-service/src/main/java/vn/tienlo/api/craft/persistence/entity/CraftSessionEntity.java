package vn.tienlo.api.craft.persistence.entity;

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

/** DB-R-011 mapping for {@code game.craft_session}; DDL: docs/design/16-alchemy.md. */
@Entity
@Table(name = "craft_session", schema = "game")
public class CraftSessionEntity {
    @Column(name = "session_id", nullable = false)
    @Id
    private UUID sessionId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "recipe_id", nullable = false, length = 48)
    private String recipeId;

    @Column(name = "mode", nullable = false, length = 16)
    private String mode;

    @Column(name = "batches", nullable = false)
    private Short batches;

    @Column(name = "furnace_instance_id", nullable = false)
    private UUID furnaceInstanceId;

    @Column(name = "fire_instance_id", nullable = true)
    private UUID fireInstanceId;

    @Column(name = "seed", nullable = false, length = 64)
    private String seed;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "events", nullable = true, columnDefinition = "jsonb")
    private JsonNode events;

    @Column(name = "score", nullable = true, precision = 6, scale = 4)
    private BigDecimal score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", nullable = true, columnDefinition = "jsonb")
    private JsonNode result;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "completed_at", nullable = true)
    private Instant completedAt;

    public CraftSessionEntity() {}

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

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Short getBatches() {
        return batches;
    }

    public void setBatches(Short batches) {
        this.batches = batches;
    }

    public UUID getFurnaceInstanceId() {
        return furnaceInstanceId;
    }

    public void setFurnaceInstanceId(UUID furnaceInstanceId) {
        this.furnaceInstanceId = furnaceInstanceId;
    }

    public UUID getFireInstanceId() {
        return fireInstanceId;
    }

    public void setFireInstanceId(UUID fireInstanceId) {
        this.fireInstanceId = fireInstanceId;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public JsonNode getEvents() {
        return events;
    }

    public void setEvents(JsonNode events) {
        this.events = events;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public JsonNode getResult() {
        return result;
    }

    public void setResult(JsonNode result) {
        this.result = result;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

}
