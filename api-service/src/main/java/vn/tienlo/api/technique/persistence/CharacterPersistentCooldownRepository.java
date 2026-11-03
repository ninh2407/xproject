package vn.tienlo.api.technique.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.technique.persistence.entity.CharacterPersistentCooldownEntity;

public interface CharacterPersistentCooldownRepository extends JpaRepository<CharacterPersistentCooldownEntity, CharacterPersistentCooldownEntity.Key> {}
