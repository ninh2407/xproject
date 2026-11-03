package vn.tienlo.api.platform.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.platform.persistence.entity.IdempotencyRecordEntity;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecordEntity, String> {}
