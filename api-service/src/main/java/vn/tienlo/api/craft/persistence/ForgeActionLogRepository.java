package vn.tienlo.api.craft.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.craft.persistence.entity.ForgeActionLogEntity;

public interface ForgeActionLogRepository extends JpaRepository<ForgeActionLogEntity, String> {}
