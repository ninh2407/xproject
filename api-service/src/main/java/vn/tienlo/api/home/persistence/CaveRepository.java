package vn.tienlo.api.home.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.home.persistence.entity.CaveEntity;

public interface CaveRepository extends JpaRepository<CaveEntity, UUID> {}
