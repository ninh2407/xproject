package vn.tienlo.api.beast.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.beast_breeding}; DDL: docs/design/26-spirit-beasts.md. */
@Entity
@Table(name = "beast_breeding", schema = "game")
public class BeastBreedingEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "parent_a", nullable = false)
    private UUID parentA;

    @Column(name = "parent_b", nullable = false)
    private UUID parentB;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quote", nullable = false, columnDefinition = "jsonb")
    private JsonNode quote;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", nullable = false, columnDefinition = "jsonb")
    private JsonNode result;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ready_at", nullable = false)
    private Instant readyAt;

    @Column(name = "state", nullable = false, length = 12)
    private String state;

    @Column(name = "egg_item_instance_id", nullable = true)
    private UUID eggItemInstanceId;

    public BeastBreedingEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getParentA() {
        return parentA;
    }

    public void setParentA(UUID parentA) {
        this.parentA = parentA;
    }

    public UUID getParentB() {
        return parentB;
    }

    public void setParentB(UUID parentB) {
        this.parentB = parentB;
    }

    public JsonNode getQuote() {
        return quote;
    }

    public void setQuote(JsonNode quote) {
        this.quote = quote;
    }

    public JsonNode getResult() {
        return result;
    }

    public void setResult(JsonNode result) {
        this.result = result;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(Instant readyAt) {
        this.readyAt = readyAt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UUID getEggItemInstanceId() {
        return eggItemInstanceId;
    }

    public void setEggItemInstanceId(UUID eggItemInstanceId) {
        this.eggItemInstanceId = eggItemInstanceId;
    }

}
