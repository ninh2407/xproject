package vn.tienlo.api.beast.persistence.entity;

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

/** DB-R-011 mapping for {@code game.beast}; DDL: docs/design/26-spirit-beasts.md. */
@Entity
@Table(name = "beast", schema = "game")
public class BeastEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "species_id", nullable = false, length = 48)
    private String speciesId;

    @Column(name = "name", nullable = false, length = 24)
    private String name;

    @Column(name = "bloodline", nullable = false, length = 8)
    private String bloodline;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "aptitudes", nullable = false, columnDefinition = "jsonb")
    private JsonNode aptitudes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "trait_ids", nullable = false, columnDefinition = "jsonb")
    private JsonNode traitIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mutations", nullable = false, columnDefinition = "jsonb")
    private JsonNode mutations;

    @Column(name = "color_variant", nullable = true, length = 32)
    private String colorVariant;

    @Column(name = "age_years", nullable = false, precision = 7, scale = 2)
    private BigDecimal ageYears;

    @Column(name = "lifespan_max_years", nullable = false)
    private Integer lifespanMaxYears;

    @Column(name = "bond", nullable = false)
    private Short bond;

    @Column(name = "breed_count", nullable = false)
    private Short breedCount;

    @Column(name = "generation", nullable = false)
    private Short generation;

    @Column(name = "parent_a", nullable = true)
    private UUID parentA;

    @Column(name = "parent_b", nullable = true)
    private UUID parentB;

    @Column(name = "state", nullable = false, length = 12)
    private String state;

    @Column(name = "contract_character_id", nullable = true)
    private UUID contractCharacterId;

    @Column(name = "injured_until", nullable = true)
    private Instant injuredUntil;

    @Column(name = "origin", nullable = false, length = 12)
    private String origin;

    @Column(name = "born_at", nullable = false)
    private Instant bornAt;

    @Column(name = "departed_at", nullable = true)
    private Instant departedAt;

    @Column(name = "departed_reason", nullable = true, length = 16)
    private String departedReason;

    @Column(name = "version", nullable = false)
    @Version
    private Integer version;

    public BeastEntity() {}

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

    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBloodline() {
        return bloodline;
    }

    public void setBloodline(String bloodline) {
        this.bloodline = bloodline;
    }

    public JsonNode getAptitudes() {
        return aptitudes;
    }

    public void setAptitudes(JsonNode aptitudes) {
        this.aptitudes = aptitudes;
    }

    public JsonNode getTraitIds() {
        return traitIds;
    }

    public void setTraitIds(JsonNode traitIds) {
        this.traitIds = traitIds;
    }

    public JsonNode getMutations() {
        return mutations;
    }

    public void setMutations(JsonNode mutations) {
        this.mutations = mutations;
    }

    public String getColorVariant() {
        return colorVariant;
    }

    public void setColorVariant(String colorVariant) {
        this.colorVariant = colorVariant;
    }

    public BigDecimal getAgeYears() {
        return ageYears;
    }

    public void setAgeYears(BigDecimal ageYears) {
        this.ageYears = ageYears;
    }

    public Integer getLifespanMaxYears() {
        return lifespanMaxYears;
    }

    public void setLifespanMaxYears(Integer lifespanMaxYears) {
        this.lifespanMaxYears = lifespanMaxYears;
    }

    public Short getBond() {
        return bond;
    }

    public void setBond(Short bond) {
        this.bond = bond;
    }

    public Short getBreedCount() {
        return breedCount;
    }

    public void setBreedCount(Short breedCount) {
        this.breedCount = breedCount;
    }

    public Short getGeneration() {
        return generation;
    }

    public void setGeneration(Short generation) {
        this.generation = generation;
    }

    public UUID getParentA() {
        return parentA;
    }

    public void setParentA(UUID parentA) {
        this.parentA = parentA;
    }

    public UUID getParentB() {
        return parentB;
    }

    public void setParentB(UUID parentB) {
        this.parentB = parentB;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UUID getContractCharacterId() {
        return contractCharacterId;
    }

    public void setContractCharacterId(UUID contractCharacterId) {
        this.contractCharacterId = contractCharacterId;
    }

    public Instant getInjuredUntil() {
        return injuredUntil;
    }

    public void setInjuredUntil(Instant injuredUntil) {
        this.injuredUntil = injuredUntil;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Instant getBornAt() {
        return bornAt;
    }

    public void setBornAt(Instant bornAt) {
        this.bornAt = bornAt;
    }

    public Instant getDepartedAt() {
        return departedAt;
    }

    public void setDepartedAt(Instant departedAt) {
        this.departedAt = departedAt;
    }

    public String getDepartedReason() {
        return departedReason;
    }

    public void setDepartedReason(String departedReason) {
        this.departedReason = departedReason;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}
