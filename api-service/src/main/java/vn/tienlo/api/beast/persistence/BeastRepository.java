package vn.tienlo.api.beast.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.beast.persistence.entity.BeastEntity;

public interface BeastRepository extends JpaRepository<BeastEntity, UUID> {}
