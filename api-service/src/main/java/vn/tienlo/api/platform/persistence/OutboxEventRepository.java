package vn.tienlo.api.platform.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.platform.persistence.entity.OutboxEventEntity;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {}
