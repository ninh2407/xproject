package vn.tienlo.api.cultivation.persistence.entity;

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

/** DB-R-011 mapping for {@code game.breakthrough_attempt}; DDL: docs/design/10-cultivation.md. */
@Entity
@Table(name = "breakthrough_attempt", schema = "game")
public class BreakthroughAttemptEntity {
    @Column(name = "attempt_id", nullable = false)
    @Id
    private UUID attemptId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "target_realm", nullable = false, length = 16)
    private String targetRealm;

    @Column(name = "from_realm_code", nullable = false, length = 8)
    private String fromRealmCode;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @Column(name = "pill_item_instance_id", nullable = false)
    private UUID pillItemInstanceId;

    @Column(name = "pill_quality", nullable = false, length = 8)
    private String pillQuality;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "support_item_ids", nullable = false, columnDefinition = "jsonb")
    private JsonNode supportItemIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "guardian_ids", nullable = false, columnDefinition = "jsonb")
    private JsonNode guardianIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "location", nullable = false, columnDefinition = "jsonb")
    private JsonNode location;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chance", nullable = false, columnDefinition = "jsonb")
    private JsonNode chance;

    @Column(name = "heart_trial_session_id", nullable = true)
    private UUID heartTrialSessionId;

    @Column(name = "roll", nullable = true, precision = 8, scale = 6)
    private BigDecimal roll;

    @Column(name = "roll_passed", nullable = true)
    private Boolean rollPassed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tribulation", nullable = true, columnDefinition = "jsonb")
    private JsonNode tribulation;

    @Column(name = "needs_review", nullable = false)
    private Boolean needsReview;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at", nullable = true)
    private Instant resolvedAt;

    public BreakthroughAttemptEntity() {}

    public UUID getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getTargetRealm() {
        return targetRealm;
    }

    public void setTargetRealm(String targetRealm) {
        this.targetRealm = targetRealm;
    }

    public String getFromRealmCode() {
        return fromRealmCode;
    }

    public void setFromRealmCode(String fromRealmCode) {
        this.fromRealmCode = fromRealmCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UUID getPillItemInstanceId() {
        return pillItemInstanceId;
    }

    public void setPillItemInstanceId(UUID pillItemInstanceId) {
        this.pillItemInstanceId = pillItemInstanceId;
    }

    public String getPillQuality() {
        return pillQuality;
    }

    public void setPillQuality(String pillQuality) {
        this.pillQuality = pillQuality;
    }

    public JsonNode getSupportItemIds() {
        return supportItemIds;
    }

    public void setSupportItemIds(JsonNode supportItemIds) {
        this.supportItemIds = supportItemIds;
    }

    public JsonNode getGuardianIds() {
        return guardianIds;
    }

    public void setGuardianIds(JsonNode guardianIds) {
        this.guardianIds = guardianIds;
    }

    public JsonNode getLocation() {
        return location;
    }

    public void setLocation(JsonNode location) {
        this.location = location;
    }

    public JsonNode getChance() {
        return chance;
    }

    public void setChance(JsonNode chance) {
        this.chance = chance;
    }

    public UUID getHeartTrialSessionId() {
        return heartTrialSessionId;
    }

    public void setHeartTrialSessionId(UUID heartTrialSessionId) {
        this.heartTrialSessionId = heartTrialSessionId;
    }

    public BigDecimal getRoll() {
        return roll;
    }

    public void setRoll(BigDecimal roll) {
        this.roll = roll;
    }

    public Boolean getRollPassed() {
        return rollPassed;
    }

    public void setRollPassed(Boolean rollPassed) {
        this.rollPassed = rollPassed;
    }

    public JsonNode getTribulation() {
        return tribulation;
    }

    public void setTribulation(JsonNode tribulation) {
        this.tribulation = tribulation;
    }

    public Boolean getNeedsReview() {
        return needsReview;
    }

    public void setNeedsReview(Boolean needsReview) {
        this.needsReview = needsReview;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

}
