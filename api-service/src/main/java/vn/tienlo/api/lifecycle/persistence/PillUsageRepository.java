package vn.tienlo.api.lifecycle.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.lifecycle.persistence.entity.PillUsageEntity;

public interface PillUsageRepository extends JpaRepository<PillUsageEntity, PillUsageEntity.Key> {}
