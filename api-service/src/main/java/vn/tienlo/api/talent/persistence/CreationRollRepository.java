package vn.tienlo.api.talent.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.talent.persistence.entity.CreationRollEntity;

public interface CreationRollRepository extends JpaRepository<CreationRollEntity, UUID> {}
