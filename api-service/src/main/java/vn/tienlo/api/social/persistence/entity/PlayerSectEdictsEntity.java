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

/** DB-R-011 mapping for {@code game.player_sect_edicts}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "player_sect_edicts", schema = "game")
public class PlayerSectEdictsEntity {
    @Column(name = "sect_id", nullable = false)
    @Id
    private UUID sectId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "edict_ids", nullable = false, columnDefinition = "jsonb")
    private JsonNode edictIds;

    @Column(name = "changed_at", nullable = true)
    private Instant changedAt;

    @Column(name = "effective_from", nullable = true)
    private Instant effectiveFrom;

    public PlayerSectEdictsEntity() {}

    public UUID getSectId() {
        return sectId;
    }

    public void setSectId(UUID sectId) {
        this.sectId = sectId;
    }

    public JsonNode getEdictIds() {
        return edictIds;
    }

    public void setEdictIds(JsonNode edictIds) {
        this.edictIds = edictIds;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

}
