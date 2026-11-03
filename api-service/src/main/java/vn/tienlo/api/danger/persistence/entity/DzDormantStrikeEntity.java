package vn.tienlo.api.danger.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.dz_dormant_strike}; DDL: docs/design/27-danger-zones.md. */
@Entity
@Table(name = "dz_dormant_strike", schema = "game")
@IdClass(DzDormantStrikeEntity.Key.class)
public class DzDormantStrikeEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "at", nullable = false)
    @Id
    private Instant at;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    public DzDormantStrikeEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Instant getAt() {
        return at;
    }

    public void setAt(Instant at) {
        this.at = at;
    }

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public Instant at;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof DzDormantStrikeEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(at, that.at);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, at);
        }
    }
}
