package vn.tienlo.api.technique.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.mastery_batch}; DDL: docs/design/13-skills.md. */
@Entity
@Table(name = "mastery_batch", schema = "game")
public class MasteryBatchEntity {
    @Column(name = "batch_id", nullable = false, length = 80)
    @Id
    private String batchId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", nullable = false, columnDefinition = "jsonb")
    private JsonNode result;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public MasteryBatchEntity() {}

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
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
