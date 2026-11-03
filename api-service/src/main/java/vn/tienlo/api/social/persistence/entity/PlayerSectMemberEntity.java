package vn.tienlo.api.social.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.player_sect_member}; DDL: docs/design/20-social.md. */
@Entity
@Table(name = "player_sect_member", schema = "game")
public class PlayerSectMemberEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "sect_id", nullable = false)
    private UUID sectId;

    @Column(name = "role", nullable = false, length = 16)
    private String role;

    @Column(name = "merit", nullable = false)
    private Long merit;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    public PlayerSectMemberEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getSectId() {
        return sectId;
    }

    public void setSectId(UUID sectId) {
        this.sectId = sectId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getMerit() {
        return merit;
    }

    public void setMerit(Long merit) {
        this.merit = merit;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

}
