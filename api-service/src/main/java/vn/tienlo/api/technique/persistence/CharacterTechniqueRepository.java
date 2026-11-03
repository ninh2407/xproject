package vn.tienlo.api.technique.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.technique.persistence.entity.CharacterTechniqueEntity;

public interface CharacterTechniqueRepository extends JpaRepository<CharacterTechniqueEntity, CharacterTechniqueEntity.Key> {}
