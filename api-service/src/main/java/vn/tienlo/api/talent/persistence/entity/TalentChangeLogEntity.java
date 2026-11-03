package vn.tienlo.api.talent.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.talent_change_log}; DDL: docs/design/04-talents.md. */
@Entity
@Table(name = "talent_change_log", schema = "game")
public class TalentChangeLogEntity {
    @Column(name = "change_id", nullable = false, length = 80)
    @Id
    private String changeId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "op", nullable = false, length = 24)
    private String op;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params", nullable = false, columnDefinition = "jsonb")
    private JsonNode params;

    @Column(name = "source", nullable = false, length = 16)
    private String source;

    @Column(name = "ref_id", nullable = true, length = 64)
    private String refId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", nullable = false, columnDefinition = "jsonb")
    private JsonNode result;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public TalentChangeLogEntity() {}

    public String getChangeId() {
        return changeId;
    }

    public void setChangeId(String changeId) {
        this.changeId = changeId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public JsonNode getParams() {
        return params;
    }

    public void setParams(JsonNode params) {
        this.params = params;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public JsonNode getResult() {
        return result;
    }

    public void setResult(JsonNode result) {
        this.result = result;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
