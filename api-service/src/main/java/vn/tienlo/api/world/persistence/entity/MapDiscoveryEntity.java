package vn.tienlo.api.world.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.map_discovery}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "map_discovery", schema = "game")
@IdClass(MapDiscoveryEntity.Key.class)
public class MapDiscoveryEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "map_id", nullable = false, length = 24)
    @Id
    private String mapId;

    @Column(name = "fog_bitmap", nullable = false)
    private byte[] fogBitmap;

    @Column(name = "discovered_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal discoveredPct;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MapDiscoveryEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public byte[] getFogBitmap() {
        return fogBitmap;
    }

    public void setFogBitmap(byte[] fogBitmap) {
        this.fogBitmap = fogBitmap;
    }

    public BigDecimal getDiscoveredPct() {
        return discoveredPct;
    }

    public void setDiscoveredPct(BigDecimal discoveredPct) {
        this.discoveredPct = discoveredPct;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public String mapId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof MapDiscoveryEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(mapId, that.mapId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, mapId);
        }
    }
}
