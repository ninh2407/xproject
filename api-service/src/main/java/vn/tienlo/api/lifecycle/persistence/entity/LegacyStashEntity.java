package vn.tienlo.api.lifecycle.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.legacy_stash}; DDL: docs/design/03-lifecycle.md. */
@Entity
@Table(name = "legacy_stash", schema = "game")
public class LegacyStashEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "source_character_id", nullable = false)
    private UUID sourceCharacterId;

    @Column(name = "sealed_at", nullable = false)
    private Instant sealedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "remnant_snapshot", nullable = false, columnDefinition = "jsonb")
    private JsonNode remnantSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bands", nullable = false, columnDefinition = "jsonb")
    private JsonNode bands;

    public LegacyStashEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getSourceCharacterId() {
        return sourceCharacterId;
    }

    public void setSourceCharacterId(UUID sourceCharacterId) {
        this.sourceCharacterId = sourceCharacterId;
    }

    public Instant getSealedAt() {
        return sealedAt;
    }

    public void setSealedAt(Instant sealedAt) {
        this.sealedAt = sealedAt;
    }

    public JsonNode getRemnantSnapshot() {
        return remnantSnapshot;
    }

    public void setRemnantSnapshot(JsonNode remnantSnapshot) {
        this.remnantSnapshot = remnantSnapshot;
    }

    public JsonNode getBands() {
        return bands;
    }

    public void setBands(JsonNode bands) {
        this.bands = bands;
    }

}
