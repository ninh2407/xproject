package vn.tienlo.api.world.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.discovered_poi}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "discovered_poi", schema = "game")
@IdClass(DiscoveredPoiEntity.Key.class)
public class DiscoveredPoiEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "poi_id", nullable = false, length = 48)
    @Id
    private String poiId;

    @Column(name = "discovered_at", nullable = false)
    private Instant discoveredAt;

    public DiscoveredPoiEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getPoiId() {
        return poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public Instant getDiscoveredAt() {
        return discoveredAt;
    }

    public void setDiscoveredAt(Instant discoveredAt) {
        this.discoveredAt = discoveredAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String poiId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof DiscoveredPoiEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(poiId, that.poiId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, poiId);
        }
    }
}
