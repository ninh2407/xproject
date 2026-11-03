package vn.tienlo.api.craft.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.profession_trial}; DDL: docs/design/16-alchemy.md. */
@Entity
@Table(name = "profession_trial", schema = "game")
public class ProfessionTrialEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "kind", nullable = false, length = 16)
    private String kind;

    @Column(name = "tier", nullable = false, length = 8)
    private String tier;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "score", nullable = true, precision = 5, scale = 4)
    private BigDecimal score;

    @Column(name = "passed", nullable = true)
    private Boolean passed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ProfessionTrialEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
