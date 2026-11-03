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

/** DB-R-011 mapping for {@code game.shop_purchase_limit}; DDL: docs/design/19-economy.md. */
@Entity
@Table(name = "shop_purchase_limit", schema = "game")
@IdClass(ShopPurchaseLimitEntity.Key.class)
public class ShopPurchaseLimitEntity {
    @Column(name = "character_id", nullable = false)
    @Id
    private UUID characterId;

    @Column(name = "shop_id", nullable = false, length = 32)
    @Id
    private String shopId;

    @Column(name = "template_id", nullable = false, length = 64)
    @Id
    private String templateId;

    @Column(name = "day", nullable = false)
    @Id
    private LocalDate day;

    @Column(name = "count", nullable = false)
    private Integer count;

    public ShopPurchaseLimitEntity() {}

    public UUID getCharacterId() {
        return characterId;
    }

    public void setCharacterId(UUID characterId) {
        this.characterId = characterId;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;

        public UUID characterId;
        public String shopId;
        public String templateId;
        public LocalDate day;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ShopPurchaseLimitEntity.Key that)) return false;
            return Objects.equals(characterId, that.characterId) && Objects.equals(shopId, that.shopId) && Objects.equals(templateId, that.templateId) && Objects.equals(day, that.day);
        }

        @Override
        public int hashCode() {
            return Objects.hash(characterId, shopId, templateId, day);
        }
    }
}
