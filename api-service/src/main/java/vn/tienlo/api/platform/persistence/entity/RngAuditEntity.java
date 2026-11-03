package vn.tienlo.api.platform.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.rng_audit}; DDL: docs/tech/00-tdd.md. */
@Entity
@Table(name = "rng_audit", schema = "game")
@IdClass(RngAuditEntity.Key.class)
public class RngAuditEntity {
    @Column(name = "id", nullable = false)
    @Id
    private Long id;

    @Column(name = "kind", nullable = false, length = 40)
    private String kind;

    @Column(name = "source", nullable = false, length = 16)
    private String source;

    @Column(name = "account_id", nullable = true)
    private UUID accountId;

    @Column(name = "character_id", nullable = true)
    private UUID characterId;

    @Column(name = "ref_id", nullable = true, length = 80)
    private String refId;

    @Column(name = "seed", nullable = false)
    private byte[] seed;

    @Column(name = "gamedata_version", nullable = false, length = 40)
    private String gamedataVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "inputs", nullable = false, columnDefinition = "jsonb")
    private JsonNode inputs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "outcome", nullable = false, columnDefinition = "jsonb")
    private JsonNode outcome;

    @Column(name = "created_at", nullable = false)
    @Id
    private Instant createdAt;

    public RngAuditEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setSeed(byte[] seed) {
        this.seed = seed;
    }

    public String getGamedataVersion() {
        return gamedataVersion;
    }

    public void setGamedataVersion(String gamedataVersion) {
        this.gamedataVersion = gamedataVersion;
    }

    public JsonNode getInputs() {
        return inputs;
    }

    public void setInputs(JsonNode inputs) {
        this.inputs = inputs;
    }

    public JsonNode getOutcome() {
        return outcome;
    }

    public void setOutcome(JsonNode outcome) {
        this.outcome = outcome;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public Long id;
        public Instant createdAt;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof RngAuditEntity.Key that)) return false;
            return Objects.equals(id, that.id) && Objects.equals(createdAt, that.createdAt);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, createdAt);
        }
    }
}
