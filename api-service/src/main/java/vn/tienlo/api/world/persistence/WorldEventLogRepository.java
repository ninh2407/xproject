package vn.tienlo.api.world.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.world.persistence.entity.WorldEventLogEntity;

public interface WorldEventLogRepository extends JpaRepository<WorldEventLogEntity, String> {}
