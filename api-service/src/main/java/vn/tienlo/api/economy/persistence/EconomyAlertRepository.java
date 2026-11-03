package vn.tienlo.api.economy.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.economy.persistence.entity.EconomyAlertEntity;

public interface EconomyAlertRepository extends JpaRepository<EconomyAlertEntity, Long> {}
