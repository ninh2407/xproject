package vn.tienlo.api.home.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.sect_site}; DDL: docs/design/28-caves-sect-sites.md. */
@Entity
@Table(name = "sect_site", schema = "game")
public class SectSiteEntity {
    @Column(name = "site_id", nullable = false, length = 32)
    @Id
    private String siteId;

    @Column(name = "region_id", nullable = false, length = 8)
    private String regionId;

    @Column(name = "map_id", nullable = false, length = 32)
    private String mapId;

    @Column(name = "grade", nullable = false, length = 1)
    private String grade;

    @Column(name = "state", nullable = false, length = 8)
    private String state;

    @Column(name = "sect_id", nullable = true)
    private UUID sectId;

    @Column(name = "held_since", nullable = true)
    private Instant heldSince;

    @Column(name = "upkeep_due_at", nullable = true)
    private Instant upkeepDueAt;

    @Column(name = "unpaid_weeks", nullable = false)
    private Short unpaidWeeks;

    @Column(name = "ruins_until", nullable = true)
    private Instant ruinsUntil;

    @Column(name = "ruins_former_sect_level", nullable = true)
    private Short ruinsFormerSectLevel;

    @Column(name = "ruins_former_sect_name", nullable = true, length = 16)
    private String ruinsFormerSectName;

    public SectSiteEntity() {}

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UUID getSectId() {
        return sectId;
    }

    public void setSectId(UUID sectId) {
        this.sectId = sectId;
    }

    public Instant getHeldSince() {
        return heldSince;
    }

    public void setHeldSince(Instant heldSince) {
        this.heldSince = heldSince;
    }

    public Instant getUpkeepDueAt() {
        return upkeepDueAt;
    }

    public void setUpkeepDueAt(Instant upkeepDueAt) {
        this.upkeepDueAt = upkeepDueAt;
    }

    public Short getUnpaidWeeks() {
        return unpaidWeeks;
    }

    public void setUnpaidWeeks(Short unpaidWeeks) {
        this.unpaidWeeks = unpaidWeeks;
    }

    public Instant getRuinsUntil() {
        return ruinsUntil;
    }

    public void setRuinsUntil(Instant ruinsUntil) {
        this.ruinsUntil = ruinsUntil;
    }

    public Short getRuinsFormerSectLevel() {
        return ruinsFormerSectLevel;
    }

    public void setRuinsFormerSectLevel(Short ruinsFormerSectLevel) {
        this.ruinsFormerSectLevel = ruinsFormerSectLevel;
    }

    public String getRuinsFormerSectName() {
        return ruinsFormerSectName;
    }

    public void setRuinsFormerSectName(String ruinsFormerSectName) {
        this.ruinsFormerSectName = ruinsFormerSectName;
    }

}
