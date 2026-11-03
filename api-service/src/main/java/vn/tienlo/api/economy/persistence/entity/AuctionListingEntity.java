package vn.tienlo.api.economy.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** DB-R-011 mapping for {@code game.auction_listing}; DDL: docs/design/19-economy.md. */
@Entity
@Table(name = "auction_listing", schema = "game")
public class AuctionListingEntity {
    @Column(name = "listing_id", nullable = false)
    @Id
    private UUID listingId;

    @Column(name = "seller_character_id", nullable = false)
    private UUID sellerCharacterId;

    @Column(name = "item_instance_id", nullable = false)
    private UUID itemInstanceId;

    @Column(name = "template_id", nullable = false, length = 64)
    private String templateId;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;

    @Column(name = "deposit", nullable = false)
    private Long deposit;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "buyer_character_id", nullable = true)
    private UUID buyerCharacterId;

    @Column(name = "sold_at", nullable = true)
    private Instant soldAt;

    public AuctionListingEntity() {}

    public UUID getListingId() {
        return listingId;
    }

    public void setListingId(UUID listingId) {
        this.listingId = listingId;
    }

    public UUID getSellerCharacterId() {
        return sellerCharacterId;
    }

    public void setSellerCharacterId(UUID sellerCharacterId) {
        this.sellerCharacterId = sellerCharacterId;
    }

    public UUID getItemInstanceId() {
        return itemInstanceId;
    }

    public void setItemInstanceId(UUID itemInstanceId) {
        this.itemInstanceId = itemInstanceId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getDeposit() {
        return deposit;
    }

    public void setDeposit(Long deposit) {
        this.deposit = deposit;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public UUID getBuyerCharacterId() {
        return buyerCharacterId;
    }

    public void setBuyerCharacterId(UUID buyerCharacterId) {
        this.buyerCharacterId = buyerCharacterId;
    }

    public Instant getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(Instant soldAt) {
        this.soldAt = soldAt;
    }

}
