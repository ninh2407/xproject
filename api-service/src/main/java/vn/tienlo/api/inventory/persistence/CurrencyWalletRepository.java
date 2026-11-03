package vn.tienlo.api.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.inventory.persistence.entity.CurrencyWalletEntity;

public interface CurrencyWalletRepository extends JpaRepository<CurrencyWalletEntity, CurrencyWalletEntity.Key> {}
