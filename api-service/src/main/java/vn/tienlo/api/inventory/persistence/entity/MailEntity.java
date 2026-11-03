package vn.tienlo.api.inventory.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.mail}; DDL: docs/design/15-items-equipment.md. */
@Entity
@Table(name = "mail", schema = "game")
public class MailEntity {
    @Column(name = "mail_id", nullable = false)
    @Id
    private UUID mailId;

    @Column(name = "character_id", nullable = false)
    private UUID characterId;

    @Column(name = "sender_kind", nullable = false, length = 16)
    private String senderKind;

    @Column(name = "title_key", nullable = true, length = 64)
    private String titleKey;

    @Column(name = "body", nullable = true)
    private String body;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "claimed_at", nullable = true)
    private Instant claimedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public MailEntity() {}

    public UUID getMailId() {
        return mailId;
    }

    public void setMailId(UUID mailId) {
        this.mailId = mailId;
    }

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getSenderKind() {
        return senderKind;
    }

    public void setSenderKind(String senderKind) {
        this.senderKind = senderKind;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(Instant claimedAt) {
        this.claimedAt = claimedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
