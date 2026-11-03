package vn.tienlo.api.monetization.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.spend_limit}; DDL: docs/design/50-monetization.md. */
@Entity
@Table(name = "spend_limit", schema = "game")
public class SpendLimitEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "monthly_cap_vnd", nullable = true)
    private Long monthlyCapVnd;

    @Column(name = "is_minor", nullable = false)
    private Boolean isMinor;

    @Column(name = "updated_at", nullable = true)
    private Instant updatedAt;

    public SpendLimitEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Long getMonthlyCapVnd() {
        return monthlyCapVnd;
    }

    public void setMonthlyCapVnd(Long monthlyCapVnd) {
        this.monthlyCapVnd = monthlyCapVnd;
    }

    public Boolean getIsMinor() {
        return isMinor;
    }

    public void setIsMinor(Boolean isMinor) {
        this.isMinor = isMinor;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
