package vn.tienlo.api.lifecycle.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.lifecycle.persistence.entity.SevereInjuryLogEntity;

public interface SevereInjuryLogRepository extends JpaRepository<SevereInjuryLogEntity, Long> {}
