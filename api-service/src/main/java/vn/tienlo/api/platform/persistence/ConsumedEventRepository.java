package vn.tienlo.api.platform.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.platform.persistence.entity.ConsumedEventEntity;

public interface ConsumedEventRepository extends JpaRepository<ConsumedEventEntity, ConsumedEventEntity.Key> {}
