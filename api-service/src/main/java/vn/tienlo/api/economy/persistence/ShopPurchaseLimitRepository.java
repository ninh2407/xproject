package vn.tienlo.api.economy.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.economy.persistence.entity.ShopPurchaseLimitEntity;

public interface ShopPurchaseLimitRepository extends JpaRepository<ShopPurchaseLimitEntity, ShopPurchaseLimitEntity.Key> {}
