package vn.tienlo.api.danger.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.danger.persistence.entity.DzDormantStrikeEntity;

public interface DzDormantStrikeRepository extends JpaRepository<DzDormantStrikeEntity, DzDormantStrikeEntity.Key> {}
