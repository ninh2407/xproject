package vn.tienlo.api.character.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.character}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "character", schema = "game")
public class CharacterEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "server_id", nullable = false)
    private Short serverId;

    @Column(name = "name", nullable = false, length = 12)
    private String name;

    @Column(name = "generation", nullable = false)
    private Integer generation;

    @Column(name = "background_id", nullable = false, length = 48)
    private String backgroundId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "base_appearance", nullable = false, columnDefinition = "jsonb")
    private JsonNode baseAppearance;

    @Column(name = "life_state", nullable = false, length = 8)
    private String lifeState;

    @Column(name = "start_age", nullable = false)
    private Integer startAge;

    @Column(name = "lifespan_used_sec", nullable = false)
    private Long lifespanUsedSec;

    @Column(name = "innate_lifespan_bonus_years", nullable = false)
    private Integer innateLifespanBonusYears;

    @Column(name = "lifespan_extension_years", nullable = false, precision = 8, scale = 2)
    private BigDecimal lifespanExtensionYears;

    @Column(name = "last_light_until", nullable = true)
    private Instant lastLightUntil;

    @Column(name = "end_cause", nullable = true, length = 24)
    private String endCause;

    @Column(name = "died_at", nullable = true)
    private Instant diedAt;

    @Column(name = "ending_deadline", nullable = true)
    private Instant endingDeadline;

    @Column(name = "ended_at", nullable = true)
    private Instant endedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spirit_root", nullable = false, columnDefinition = "jsonb")
    private JsonNode spiritRoot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "innate_stats", nullable = false, columnDefinition = "jsonb")
    private JsonNode innateStats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "innate_stats_creation", nullable = false, columnDefinition = "jsonb")
    private JsonNode innateStatsCreation;

    @Column(name = "modifiers_version", nullable = false)
    private Integer modifiersVersion;

    @Column(name = "realm", nullable = false, length = 4)
    private String realm;

    @Column(name = "realm_stage", nullable = false)
    private Integer realmStage;

    @Column(name = "cultivation_exp", nullable = false)
    private Long cultivationExp;

    @Column(name = "pill_toxicity", nullable = false, precision = 6, scale = 2)
    private BigDecimal pillToxicity;

    @Column(name = "toxicity_updated_at", nullable = false)
    private Instant toxicityUpdatedAt;

    @Column(name = "heart_state", nullable = false)
    private Integer heartState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "breakthrough_failures", nullable = false, columnDefinition = "jsonb")
    private JsonNode breakthroughFailures;

    @Column(name = "last_breakthrough_fail_at", nullable = true)
    private Instant lastBreakthroughFailAt;

    @Column(name = "main_technique_id", nullable = true, length = 48)
    private String mainTechniqueId;

    @Column(name = "main_technique_changed_at", nullable = true)
    private Instant mainTechniqueChangedAt;

    @Column(name = "map_id", nullable = true, length = 24)
    private String mapId;

    @Column(name = "channel", nullable = true)
    private Short channel;

    @Column(name = "pos_x", nullable = true, precision = 7, scale = 2)
    private BigDecimal posX;

    @Column(name = "pos_z", nullable = true, precision = 7, scale = 2)
    private BigDecimal posZ;

    @Column(name = "facing", nullable = true)
    private Short facing;

    @Column(name = "hp", nullable = true)
    private Integer hp;

    @Column(name = "mp", nullable = true)
    private Integer mp;

    @Column(name = "snapshot_seq", nullable = false)
    private Long snapshotSeq;

    @Column(name = "snapshot_at", nullable = true)
    private Instant snapshotAt;

    @Column(name = "combat_power", nullable = true)
    private Integer combatPower;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_login_at", nullable = true)
    private Instant lastLoginAt;

    @Column(name = "version", nullable = false)
    @Version
    private Integer version;

    public CharacterEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Short getServerId() {
        return serverId;
    }

    public void setServerId(Short serverId) {
        this.serverId = serverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGeneration() {
        return generation;
    }

    public void setGeneration(Integer generation) {
        this.generation = generation;
    }

    public String getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(String backgroundId) {
        this.backgroundId = backgroundId;
    }

    public JsonNode getBaseAppearance() {
        return baseAppearance;
    }

    public void setBaseAppearance(JsonNode baseAppearance) {
        this.baseAppearance = baseAppearance;
    }

    public String getLifeState() {
        return lifeState;
    }

    public void setLifeState(String lifeState) {
        this.lifeState = lifeState;
    }

    public Integer getStartAge() {
        return startAge;
    }

    public void setStartAge(Integer startAge) {
        this.startAge = startAge;
    }

    public Long getLifespanUsedSec() {
        return lifespanUsedSec;
    }

    public void setLifespanUsedSec(Long lifespanUsedSec) {
        this.lifespanUsedSec = lifespanUsedSec;
    }

    public Integer getInnateLifespanBonusYears() {
        return innateLifespanBonusYears;
    }

    public void setInnateLifespanBonusYears(Integer innateLifespanBonusYears) {
        this.innateLifespanBonusYears = innateLifespanBonusYears;
    }

    public BigDecimal getLifespanExtensionYears() {
        return lifespanExtensionYears;
    }

    public void setLifespanExtensionYears(BigDecimal lifespanExtensionYears) {
        this.lifespanExtensionYears = lifespanExtensionYears;
    }

    public Instant getLastLightUntil() {
        return lastLightUntil;
    }

    public void setLastLightUntil(Instant lastLightUntil) {
        this.lastLightUntil = lastLightUntil;
    }

    public String getEndCause() {
        return endCause;
    }

    public void setEndCause(String endCause) {
        this.endCause = endCause;
    }

    public Instant getDiedAt() {
        return diedAt;
    }

    public void setDiedAt(Instant diedAt) {
        this.diedAt = diedAt;
    }

    public Instant getEndingDeadline() {
        return endingDeadline;
    }

    public void setEndingDeadline(Instant endingDeadline) {
        this.endingDeadline = endingDeadline;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public JsonNode getSpiritRoot() {
        return spiritRoot;
    }

    public void setSpiritRoot(JsonNode spiritRoot) {
        this.spiritRoot = spiritRoot;
    }

    public JsonNode getInnateStats() {
        return innateStats;
    }

    public void setInnateStats(JsonNode innateStats) {
        this.innateStats = innateStats;
    }

    public JsonNode getInnateStatsCreation() {
        return innateStatsCreation;
    }

    public void setInnateStatsCreation(JsonNode innateStatsCreation) {
        this.innateStatsCreation = innateStatsCreation;
    }

    public Integer getModifiersVersion() {
        return modifiersVersion;
    }

    public void setModifiersVersion(Integer modifiersVersion) {
        this.modifiersVersion = modifiersVersion;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public Integer getRealmStage() {
        return realmStage;
    }

    public void setRealmStage(Integer realmStage) {
        this.realmStage = realmStage;
    }

    public Long getCultivationExp() {
        return cultivationExp;
    }

    public void setCultivationExp(Long cultivationExp) {
        this.cultivationExp = cultivationExp;
    }

    public BigDecimal getPillToxicity() {
        return pillToxicity;
    }

    public void setPillToxicity(BigDecimal pillToxicity) {
        this.pillToxicity = pillToxicity;
    }

    public Instant getToxicityUpdatedAt() {
        return toxicityUpdatedAt;
    }

    public void setToxicityUpdatedAt(Instant toxicityUpdatedAt) {
        this.toxicityUpdatedAt = toxicityUpdatedAt;
    }

    public Integer getHeartState() {
        return heartState;
    }

    public void setHeartState(Integer heartState) {
        this.heartState = heartState;
    }

    public JsonNode getBreakthroughFailures() {
        return breakthroughFailures;
    }

    public void setBreakthroughFailures(JsonNode breakthroughFailures) {
        this.breakthroughFailures = breakthroughFailures;
    }

    public Instant getLastBreakthroughFailAt() {
        return lastBreakthroughFailAt;
    }

    public void setLastBreakthroughFailAt(Instant lastBreakthroughFailAt) {
        this.lastBreakthroughFailAt = lastBreakthroughFailAt;
    }

    public String getMainTechniqueId() {
        return mainTechniqueId;
    }

    public void setMainTechniqueId(String mainTechniqueId) {
        this.mainTechniqueId = mainTechniqueId;
    }

    public Instant getMainTechniqueChangedAt() {
        return mainTechniqueChangedAt;
    }

    public void setMainTechniqueChangedAt(Instant mainTechniqueChangedAt) {
        this.mainTechniqueChangedAt = mainTechniqueChangedAt;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public Short getChannel() {
        return channel;
    }

    public void setChannel(Short channel) {
        this.channel = channel;
    }

    public BigDecimal getPosX() {
        return posX;
    }

    public void setPosX(BigDecimal posX) {
        this.posX = posX;
    }

    public BigDecimal getPosZ() {
        return posZ;
    }

    public void setPosZ(BigDecimal posZ) {
        this.posZ = posZ;
    }

    public Short getFacing() {
        return facing;
    }

    public void setFacing(Short facing) {
        this.facing = facing;
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Integer getMp() {
        return mp;
    }

    public void setMp(Integer mp) {
        this.mp = mp;
    }

    public Long getSnapshotSeq() {
        return snapshotSeq;
    }

    public void setSnapshotSeq(Long snapshotSeq) {
        this.snapshotSeq = snapshotSeq;
    }

    public Instant getSnapshotAt() {
        return snapshotAt;
    }

    public void setSnapshotAt(Instant snapshotAt) {
        this.snapshotAt = snapshotAt;
    }

    public Integer getCombatPower() {
        return combatPower;
    }

    public void setCombatPower(Integer combatPower) {
        this.combatPower = combatPower;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}
