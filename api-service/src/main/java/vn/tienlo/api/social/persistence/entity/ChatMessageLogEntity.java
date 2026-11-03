package vn.tienlo.api.social.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.chat_message_log}; DDL: docs/design/20-social.md. */
@Entity
@Table(name = "chat_message_log", schema = "game")
@IdClass(ChatMessageLogEntity.Key.class)
public class ChatMessageLogEntity {
    @Column(name = "message_id", nullable = false)
    @Id
    private UUID messageId;

    @Column(name = "channel", nullable = false, length = 8)
    private String channel;

    @Column(name = "scope_id", nullable = true, length = 80)
    private String scopeId;

    @Column(name = "from_character_id", nullable = true)
    private UUID fromCharacterId;

    @Column(name = "from_account_id", nullable = true)
    private UUID fromAccountId;

    @Column(name = "text_raw", nullable = false)
    private String textRaw;

    @Column(name = "text_filtered", nullable = false)
    private String textFiltered;

    @Column(name = "sent_at", nullable = false)
    @Id
    private Instant sentAt;

    public ChatMessageLogEntity() {}

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public UUID getFromCharacterId() {
        return fromCharacterId;
    }

    public void setFromCharacterId(UUID fromCharacterId) {
        this.fromCharacterId = fromCharacterId;
    }

    public UUID getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(UUID fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getTextRaw() {
        return textRaw;
    }

    public void setTextRaw(String textRaw) {
        this.textRaw = textRaw;
    }

    public String getTextFiltered() {
        return textFiltered;
    }

    public void setTextFiltered(String textFiltered) {
        this.textFiltered = textFiltered;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public Instant sentAt;
        public UUID messageId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ChatMessageLogEntity.Key that)) return false;
            return Objects.equals(sentAt, that.sentAt) && Objects.equals(messageId, that.messageId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sentAt, messageId);
        }
    }
}
