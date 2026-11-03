package vn.tienlo.api.social.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.player_sect}; DDL: docs/design/20-social.md. */
@Entity
@Table(name = "player_sect", schema = "game")
public class PlayerSectEntity {
    @Column(name = "sect_id", nullable = false)
    @Id
    private UUID sectId;

    @Column(name = "name", nullable = false, length = 16)
    private String name;

    @Column(name = "emblem_id", nullable = false, length = 32)
    private String emblemId;

    @Column(name = "level", nullable = false)
    private Short level;

    @Column(name = "exp", nullable = false)
    private Integer exp;

    @Column(name = "leader_account_id", nullable = false)
    private UUID leaderAccountId;

    @Column(name = "treasury_stones", nullable = false)
    private Long treasuryStones;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "buildings", nullable = false, columnDefinition = "jsonb")
    private JsonNode buildings;

    @Column(name = "upkeep_due_at", nullable = false)
    private Instant upkeepDueAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "disbanded_at", nullable = true)
    private Instant disbandedAt;

    public PlayerSectEntity() {}

    public UUID getSectId() {
        return sectId;
    }

    public void setSectId(UUID sectId) {
        this.sectId = sectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmblemId() {
        return emblemId;
    }

    public void setEmblemId(String emblemId) {
        this.emblemId = emblemId;
    }

    public Short getLevel() {
        return level;
    }

    public void setLevel(Short level) {
        this.level = level;
    }

    public Integer getExp() {
        return exp;
    }

    public void setExp(Integer exp) {
        this.exp = exp;
    }

    public UUID getLeaderAccountId() {
        return leaderAccountId;
    }

    public void setLeaderAccountId(UUID leaderAccountId) {
        this.leaderAccountId = leaderAccountId;
    }

    public Long getTreasuryStones() {
        return treasuryStones;
    }

    public void setTreasuryStones(Long treasuryStones) {
        this.treasuryStones = treasuryStones;
    }

    public JsonNode getBuildings() {
        return buildings;
    }

    public void setBuildings(JsonNode buildings) {
        this.buildings = buildings;
    }

    public Instant getUpkeepDueAt() {
        return upkeepDueAt;
    }

    public void setUpkeepDueAt(Instant upkeepDueAt) {
        this.upkeepDueAt = upkeepDueAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDisbandedAt() {
        return disbandedAt;
    }

    public void setDisbandedAt(Instant disbandedAt) {
        this.disbandedAt = disbandedAt;
    }

}
