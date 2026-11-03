package vn.tienlo.api.character.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.character.persistence.entity.CharacterStatusEntity;

public interface CharacterStatusRepository extends JpaRepository<CharacterStatusEntity, CharacterStatusEntity.Key> {}
