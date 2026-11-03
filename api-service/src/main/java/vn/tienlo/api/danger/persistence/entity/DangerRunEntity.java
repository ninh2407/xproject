package vn.tienlo.api.danger.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.danger_run}; DDL: docs/design/27-danger-zones.md. */
@Entity
@Table(name = "danger_run", schema = "game")
public class DangerRunEntity {
    @Column(name = "run_id", nullable = false)
    @Id
    private UUID runId;

    @Column(name = "dz_id", nullable = false, length = 16)
    private String dzId;

    @Column(name = "layout_id", nullable = true, length = 24)
    private String layoutId;

    @Column(name = "instance_id", nullable = true, length = 64)
    private String instanceId;

    @Column(name = "party_id", nullable = true)
    private UUID partyId;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @Column(name = "started_at", nullable = true)
    private Instant startedAt;

    @Column(name = "ends_at", nullable = true)
    private Instant endsAt;

    @Column(name = "closed_at", nullable = true)
    private Instant closedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DangerRunEntity() {}

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public String getDzId() {
        return dzId;
    }

    public void setDzId(String dzId) {
        this.dzId = dzId;
    }

    public String getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(String layoutId) {
        this.layoutId = layoutId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public UUID getPartyId() {
        return partyId;
    }

    public void setPartyId(UUID partyId) {
        this.partyId = partyId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
