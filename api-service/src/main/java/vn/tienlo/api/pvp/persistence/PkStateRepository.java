package vn.tienlo.api.pvp.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.pvp.persistence.entity.PkStateEntity;

public interface PkStateRepository extends JpaRepository<PkStateEntity, UUID> {}
