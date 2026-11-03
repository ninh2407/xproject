package vn.tienlo.api.social.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.character_benefit}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "character_benefit", schema = "game")
public class CharacterBenefitEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "source", nullable = false, length = 12)
    private String source;

    @Column(name = "switched_at", nullable = false)
    private Instant switchedAt;

    public CharacterBenefitEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getSwitchedAt() {
        return switchedAt;
    }

    public void setSwitchedAt(Instant switchedAt) {
        this.switchedAt = switchedAt;
    }

}
