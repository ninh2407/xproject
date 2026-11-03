package vn.tienlo.api.technique.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.technique.persistence.entity.CharacterLoadoutEntity;

public interface CharacterLoadoutRepository extends JpaRepository<CharacterLoadoutEntity, CharacterLoadoutEntity.Key> {}
