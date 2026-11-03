package vn.tienlo.api.craft.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.forge_action_log}; DDL: docs/design/17-forging.md. */
@Entity
@Table(name = "forge_action_log", schema = "game")
public class ForgeActionLogEntity {
    @Column(name = "request_id", nullable = false, length = 80)
    @Id
    private String requestId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "item_instance_id", nullable = true)
    private UUID itemInstanceId;

    @Column(name = "action", nullable = false, length = 16)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input", nullable = false, columnDefinition = "jsonb")
    private JsonNode input;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", nullable = false, columnDefinition = "jsonb")
    private JsonNode result;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ForgeActionLogEntity() {}

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public UUID getItemInstanceId() {
        return itemInstanceId;
    }

    public void setItemInstanceId(UUID itemInstanceId) {
        this.itemInstanceId = itemInstanceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public JsonNode getInput() {
        return input;
    }

    public void setInput(JsonNode input) {
        this.input = input;
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
