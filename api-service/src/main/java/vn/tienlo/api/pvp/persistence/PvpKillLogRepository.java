package vn.tienlo.api.pvp.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.pvp.persistence.entity.PvpKillLogEntity;

public interface PvpKillLogRepository extends JpaRepository<PvpKillLogEntity, PvpKillLogEntity.Key> {}
