package vn.tienlo.api.danger.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.danger.persistence.entity.DangerRunEntity;

public interface DangerRunRepository extends JpaRepository<DangerRunEntity, UUID> {}
