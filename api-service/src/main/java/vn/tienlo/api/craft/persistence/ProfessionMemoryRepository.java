package vn.tienlo.api.craft.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.craft.persistence.entity.ProfessionMemoryEntity;

public interface ProfessionMemoryRepository extends JpaRepository<ProfessionMemoryEntity, ProfessionMemoryEntity.Key> {}
