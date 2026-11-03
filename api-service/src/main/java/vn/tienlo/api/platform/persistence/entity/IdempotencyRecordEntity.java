package vn.tienlo.api.platform.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.idempotency_record}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "idempotency_record", schema = "game")
public class IdempotencyRecordEntity {
    @Column(name = "key", nullable = false, length = 80)
    @Id
    private String key;

    @Column(name = "endpoint", nullable = false, length = 96)
    private String endpoint;

    @Column(name = "request_hash", nullable = false)
    private byte[] requestHash;

    @Column(name = "status", nullable = false, length = 12)
    private String status;

    @Column(name = "http_status", nullable = true)
    private Short httpStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response", nullable = true, columnDefinition = "jsonb")
    private JsonNode response;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public IdempotencyRecordEntity() {}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public byte[] getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(byte[] requestHash) {
        this.requestHash = requestHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Short getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Short httpStatus) {
        this.httpStatus = httpStatus;
    }

    public JsonNode getResponse() {
        return response;
    }

    public void setResponse(JsonNode response) {
        this.response = response;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
