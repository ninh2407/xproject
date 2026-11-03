package vn.tienlo.api.home.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.cave}; DDL: docs/design/28-caves-sect-sites.md. */
@Entity
@Table(name = "cave", schema = "game")
public class CaveEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "gate_poi_id", nullable = false, length = 48)
    private String gatePoiId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "buildings", nullable = false, columnDefinition = "jsonb")
    private JsonNode buildings;

    @Column(name = "visit_policy", nullable = false, length = 8)
    private String visitPolicy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "decorations", nullable = false, columnDefinition = "jsonb")
    private JsonNode decorations;

    @Column(name = "claimed_at", nullable = false)
    private Instant claimedAt;

    @Column(name = "relocated_at", nullable = true)
    private Instant relocatedAt;

    @Column(name = "archived", nullable = false)
    private Boolean archived;

    public CaveEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getGatePoiId() {
        return gatePoiId;
    }

    public void setGatePoiId(String gatePoiId) {
        this.gatePoiId = gatePoiId;
    }

    public JsonNode getBuildings() {
        return buildings;
    }

    public void setBuildings(JsonNode buildings) {
        this.buildings = buildings;
    }

    public String getVisitPolicy() {
        return visitPolicy;
    }

    public void setVisitPolicy(String visitPolicy) {
        this.visitPolicy = visitPolicy;
    }

    public JsonNode getDecorations() {
        return decorations;
    }

    public void setDecorations(JsonNode decorations) {
        this.decorations = decorations;
    }

    public Instant getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(Instant claimedAt) {
        this.claimedAt = claimedAt;
    }

    public Instant getRelocatedAt() {
        return relocatedAt;
    }

    public void setRelocatedAt(Instant relocatedAt) {
        this.relocatedAt = relocatedAt;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

}
