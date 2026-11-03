package vn.tienlo.api.encounter.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.encounter_account_history}; DDL: docs/design/23-encounters.md. */
@Entity
@Table(name = "encounter_account_history", schema = "game")
@IdClass(EncounterAccountHistoryEntity.Key.class)
public class EncounterAccountHistoryEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "encounter_id", nullable = false, length = 64)
    @Id
    private String encounterId;

    @Column(name = "times", nullable = false)
    private Integer times;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "endings_seen", nullable = false, columnDefinition = "jsonb")
    private JsonNode endingsSeen;

    public EncounterAccountHistoryEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public JsonNode getEndingsSeen() {
        return endingsSeen;
    }

    public void setEndingsSeen(JsonNode endingsSeen) {
        this.endingsSeen = endingsSeen;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String encounterId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof EncounterAccountHistoryEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(encounterId, that.encounterId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, encounterId);
        }
    }
}
