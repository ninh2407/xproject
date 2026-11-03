package vn.tienlo.api.beast.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.beast.persistence.entity.BeastLogEntity;

public interface BeastLogRepository extends JpaRepository<BeastLogEntity, Long> {}
