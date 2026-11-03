package vn.tienlo.api.pvp.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.pk_state}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "pk_state", schema = "game")
public class PkStateEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "mode", nullable = false, length = 10)
    private String mode;

    @Column(name = "mode_changed_at", nullable = true)
    private Instant modeChangedAt;

    @Column(name = "infamy", nullable = false)
    private Integer infamy;

    @Column(name = "infamy_peak", nullable = false)
    private Integer infamyPeak;

    @Column(name = "grey_until", nullable = true)
    private Instant greyUntil;

    @Column(name = "last_pvp_combat_at", nullable = true)
    private Instant lastPvpCombatAt;

    @Column(name = "heaven_punishment_field_sec", nullable = true)
    private Integer heavenPunishmentFieldSec;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feuds", nullable = false, columnDefinition = "jsonb")
    private JsonNode feuds;

    @Column(name = "version", nullable = false)
    @Version
    private Integer version;

    public PkStateEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Instant getModeChangedAt() {
        return modeChangedAt;
    }

    public void setModeChangedAt(Instant modeChangedAt) {
        this.modeChangedAt = modeChangedAt;
    }

    public Integer getInfamy() {
        return infamy;
    }

    public void setInfamy(Integer infamy) {
        this.infamy = infamy;
    }

    public Integer getInfamyPeak() {
        return infamyPeak;
    }

    public void setInfamyPeak(Integer infamyPeak) {
        this.infamyPeak = infamyPeak;
    }

    public Instant getGreyUntil() {
        return greyUntil;
    }

    public void setGreyUntil(Instant greyUntil) {
        this.greyUntil = greyUntil;
    }

    public Instant getLastPvpCombatAt() {
        return lastPvpCombatAt;
    }

    public void setLastPvpCombatAt(Instant lastPvpCombatAt) {
        this.lastPvpCombatAt = lastPvpCombatAt;
    }

    public Integer getHeavenPunishmentFieldSec() {
        return heavenPunishmentFieldSec;
    }

    public void setHeavenPunishmentFieldSec(Integer heavenPunishmentFieldSec) {
        this.heavenPunishmentFieldSec = heavenPunishmentFieldSec;
    }

    public JsonNode getFeuds() {
        return feuds;
    }

    public void setFeuds(JsonNode feuds) {
        this.feuds = feuds;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}
