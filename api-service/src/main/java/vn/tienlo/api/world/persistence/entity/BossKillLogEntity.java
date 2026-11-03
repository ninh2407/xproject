package vn.tienlo.api.world.persistence.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** DB-R-011 mapping for {@code game.boss_kill_log}; DDL: docs/design/18-enemies-ai.md. */
@Entity
@Table(name = "boss_kill_log", schema = "game")
public class BossKillLogEntity {
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monster_id", nullable = false, length = 48)
    private String monsterId;

    @Column(name = "map_id", nullable = false, length = 32)
    private String mapId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "party", nullable = false, columnDefinition = "jsonb")
    private JsonNode party;

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public BossKillLogEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(String monsterId) {
        this.monsterId = monsterId;
    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public JsonNode getParty() {
        return party;
    }

    public void setParty(JsonNode party) {
        this.party = party;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(Integer durationSec) {
        this.durationSec = durationSec;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
