package vn.tienlo.api.social.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.npc_sect_membership}; DDL: docs/design/20-social.md. */
@Entity
@Table(name = "npc_sect_membership", schema = "game")
@IdClass(NpcSectMembershipEntity.Key.class)
public class NpcSectMembershipEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "sect_id", nullable = false, length = 32)
    @Id
    private String sectId;

    @Column(name = "rank", nullable = false, length = 16)
    private String rank;

    @Column(name = "contribution_total", nullable = false)
    private Integer contributionTotal;

    @Column(name = "reborn_disciple", nullable = false)
    private Boolean rebornDisciple;

    @Column(name = "state", nullable = false, length = 8)
    private String state;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at", nullable = true)
    private Instant leftAt;

    public NpcSectMembershipEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getSectId() {
        return sectId;
    }

    public void setSectId(String sectId) {
        this.sectId = sectId;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Integer getContributionTotal() {
        return contributionTotal;
    }

    public void setContributionTotal(Integer contributionTotal) {
        this.contributionTotal = contributionTotal;
    }

    public Boolean getRebornDisciple() {
        return rebornDisciple;
    }

    public void setRebornDisciple(Boolean rebornDisciple) {
        this.rebornDisciple = rebornDisciple;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String sectId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof NpcSectMembershipEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(sectId, that.sectId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, sectId);
        }
    }
}
