package vn.tienlo.api.talent.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.talent.persistence.entity.CreationAttemptEntity;

public interface CreationAttemptRepository extends JpaRepository<CreationAttemptEntity, UUID> {}
