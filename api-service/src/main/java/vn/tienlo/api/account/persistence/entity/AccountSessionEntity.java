package vn.tienlo.api.account.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.account_session}; DDL: docs/tech/02-database.md. */
@Entity
@Table(name = "account_session", schema = "game")
public class AccountSessionEntity {
    @Column(name = "id", nullable = false)
    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "refresh_token_hash", nullable = false)
    private byte[] refreshTokenHash;

    @Column(name = "device_hash", nullable = true)
    private byte[] deviceHash;

    @Column(name = "device", nullable = true, length = 128)
    private String device;

    @Column(name = "ip", nullable = true, columnDefinition = "inet")
    private InetAddress ip;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = true)
    private Instant revokedAt;

    public AccountSessionEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public byte[] getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(byte[] refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public byte[] getDeviceHash() {
        return deviceHash;
    }

    public void setDeviceHash(byte[] deviceHash) {
        this.deviceHash = deviceHash;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

}
