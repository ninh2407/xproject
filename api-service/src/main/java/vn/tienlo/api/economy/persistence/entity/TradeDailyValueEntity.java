package vn.tienlo.api.economy.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.trade_daily_value}; DDL: docs/design/19-economy.md. */
@Entity
@Table(name = "trade_daily_value", schema = "game")
@IdClass(TradeDailyValueEntity.Key.class)
public class TradeDailyValueEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "day", nullable = false)
    @Id
    private LocalDate day;

    @Column(name = "received_value", nullable = false)
    private Long receivedValue;

    public TradeDailyValueEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public Long getReceivedValue() {
        return receivedValue;
    }

    public void setReceivedValue(Long receivedValue) {
        this.receivedValue = receivedValue;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public LocalDate day;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof TradeDailyValueEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(day, that.day);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, day);
        }
    }
}
