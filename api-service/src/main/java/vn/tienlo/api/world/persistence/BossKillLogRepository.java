package vn.tienlo.api.world.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.world.persistence.entity.BossKillLogEntity;

public interface BossKillLogRepository extends JpaRepository<BossKillLogEntity, Long> {}
