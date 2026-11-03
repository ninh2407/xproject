package vn.tienlo.api.craft.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.craft.persistence.entity.ProfessionEntity;

public interface ProfessionRepository extends JpaRepository<ProfessionEntity, ProfessionEntity.Key> {}
