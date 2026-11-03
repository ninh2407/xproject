package vn.tienlo.api.encounter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.encounter.persistence.entity.CharacterFlagEntity;

public interface CharacterFlagRepository extends JpaRepository<CharacterFlagEntity, CharacterFlagEntity.Key> {}
