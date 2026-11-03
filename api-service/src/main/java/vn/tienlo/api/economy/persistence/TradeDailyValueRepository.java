package vn.tienlo.api.economy.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.economy.persistence.entity.TradeDailyValueEntity;

public interface TradeDailyValueRepository extends JpaRepository<TradeDailyValueEntity, TradeDailyValueEntity.Key> {}
