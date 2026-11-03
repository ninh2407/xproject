package vn.tienlo.api.cultivation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.cultivation.persistence.entity.BreakthroughAttemptEntity;

public interface BreakthroughAttemptRepository extends JpaRepository<BreakthroughAttemptEntity, UUID> {}
