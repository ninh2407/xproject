package vn.tienlo.api.home.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.cave_plot}; DDL: docs/design/28-caves-sect-sites.md. */
@Entity
@Table(name = "cave_plot", schema = "game")
@IdClass(CavePlotEntity.Key.class)
public class CavePlotEntity {
    @Column(name = "account_id", nullable = false)
    @Id
    private UUID accountId;

    @Column(name = "plot_index", nullable = false)
    @Id
    private Short plotIndex;

    @Column(name = "seed_template_id", nullable = true, length = 64)
    private String seedTemplateId;

    @Column(name = "planted_at", nullable = true)
    private Instant plantedAt;

    @Column(name = "ready_at", nullable = true)
    private Instant readyAt;

    public CavePlotEntity() {}

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Short getPlotIndex() {
        return plotIndex;
    }

    public void setPlotIndex(Short plotIndex) {
        this.plotIndex = plotIndex;
    }

    public String getSeedTemplateId() {
        return seedTemplateId;
    }

    public void setSeedTemplateId(String seedTemplateId) {
        this.seedTemplateId = seedTemplateId;
    }

    public Instant getPlantedAt() {
        return plantedAt;
    }

    public void setPlantedAt(Instant plantedAt) {
        this.plantedAt = plantedAt;
    }

    public Instant getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(Instant readyAt) {
        this.readyAt = readyAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID accountId;
        public Short plotIndex;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CavePlotEntity.Key that)) return false;
            return Objects.equals(accountId, that.accountId) && Objects.equals(plotIndex, that.plotIndex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, plotIndex);
        }
    }
}
