package vn.tienlo.api.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.inventory.persistence.entity.ItemLogEntity;

public interface ItemLogRepository extends JpaRepository<ItemLogEntity, Long> {}
