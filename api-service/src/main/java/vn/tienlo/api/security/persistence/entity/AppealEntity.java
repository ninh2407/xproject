package vn.tienlo.api.security.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.appeal}; DDL: docs/tech/07-security.md. */
@Entity
@Table(name = "appeal", schema = "game")
public class AppealEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "sanction_id", nullable = false)
    private UUID sanctionId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "reviewer_id", nullable = true)
    private UUID reviewerId;

    @Column(name = "response", nullable = true)
    private String response;

    @Column(name = "upheld", nullable = true)
    private Boolean upheld;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "resolved_at", nullable = true)
    private Instant resolvedAt;

    public AppealEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSanctionId() {
        return sanctionId;
    }

    public void setSanctionId(UUID sanctionId) {
        this.sanctionId = sanctionId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(UUID reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Boolean getUpheld() {
        return upheld;
    }

    public void setUpheld(Boolean upheld) {
        this.upheld = upheld;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

}
