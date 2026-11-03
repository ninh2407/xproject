package vn.tienlo.api.admin.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.admin.persistence.entity.FortuneDisputeEntity;

public interface FortuneDisputeRepository extends JpaRepository<FortuneDisputeEntity, UUID> {}
