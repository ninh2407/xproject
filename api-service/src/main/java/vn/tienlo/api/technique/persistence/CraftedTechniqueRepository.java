package vn.tienlo.api.technique.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.technique.persistence.entity.CraftedTechniqueEntity;

public interface CraftedTechniqueRepository extends JpaRepository<CraftedTechniqueEntity, String> {}
