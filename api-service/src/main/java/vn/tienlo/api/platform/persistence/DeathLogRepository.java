package vn.tienlo.api.platform.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.platform.persistence.entity.DeathLogEntity;

public interface DeathLogRepository extends JpaRepository<DeathLogEntity, UUID> {}
