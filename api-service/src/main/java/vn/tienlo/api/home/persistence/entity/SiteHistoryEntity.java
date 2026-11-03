package vn.tienlo.api.home.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.site_history}; DDL: docs/design/28-caves-sect-sites.md. */
@Entity
@Table(name = "site_history", schema = "game")
public class SiteHistoryEntity {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_id", nullable = false, length = 32)
    private String siteId;

    @Column(name = "sect_id", nullable = false)
    private UUID sectId;

    @Column(name = "sect_name", nullable = false, length = 16)
    private String sectName;

    @Column(name = "held_from", nullable = false)
    private Instant heldFrom;

    @Column(name = "held_to", nullable = true)
    private Instant heldTo;

    @Column(name = "end_reason", nullable = true, length = 16)
    private String endReason;

    public SiteHistoryEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public UUID getSectId() {
        return sectId;
    }

    public void setSectId(UUID sectId) {
        this.sectId = sectId;
    }

    public String getSectName() {
        return sectName;
    }

    public void setSectName(String sectName) {
        this.sectName = sectName;
    }

    public Instant getHeldFrom() {
        return heldFrom;
    }

    public void setHeldFrom(Instant heldFrom) {
        this.heldFrom = heldFrom;
    }

    public Instant getHeldTo() {
        return heldTo;
    }

    public void setHeldTo(Instant heldTo) {
        this.heldTo = heldTo;
    }

    public String getEndReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }

}
