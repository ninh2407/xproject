package vn.tienlo.api.encounter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.encounter.persistence.entity.LegacyCaveEntity;

public interface LegacyCaveRepository extends JpaRepository<LegacyCaveEntity, UUID> {}
