package vn.tienlo.api.talent.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.talent.persistence.entity.CharacterTalentEntity;

public interface CharacterTalentRepository extends JpaRepository<CharacterTalentEntity, CharacterTalentEntity.Key> {}
