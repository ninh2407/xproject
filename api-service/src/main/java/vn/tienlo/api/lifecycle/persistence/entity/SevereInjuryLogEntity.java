package vn.tienlo.api.lifecycle.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.severe_injury_log}; DDL: docs/design/03-lifecycle.md. */
@Entity
@Table(name = "severe_injury_log", schema = "game")
public class SevereInjuryLogEntity {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "map_id", nullable = false, length = 32)
    private String mapId;

    @Column(name = "cause", nullable = false, length = 32)
    private String cause;

    @Column(name = "lifespan_lost_years", nullable = false, precision = 6, scale = 2)
    private BigDecimal lifespanLostYears;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public SevereInjuryLogEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public BigDecimal getLifespanLostYears() {
        return lifespanLostYears;
    }

    public void setLifespanLostYears(BigDecimal lifespanLostYears) {
        this.lifespanLostYears = lifespanLostYears;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
