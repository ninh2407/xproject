package vn.tienlo.api.craft.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.craft.persistence.entity.CraftSessionEntity;

public interface CraftSessionRepository extends JpaRepository<CraftSessionEntity, UUID> {}
