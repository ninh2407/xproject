package vn.tienlo.api.beast.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.beast.persistence.entity.BeastBreedingEntity;

public interface BeastBreedingRepository extends JpaRepository<BeastBreedingEntity, UUID> {}
