package vn.tienlo.api.beast.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.beast.persistence.entity.BeastPityEntity;

public interface BeastPityRepository extends JpaRepository<BeastPityEntity, UUID> {}
