package vn.tienlo.api.world.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.world.persistence.entity.CharacterSecretEntity;

public interface CharacterSecretRepository extends JpaRepository<CharacterSecretEntity, CharacterSecretEntity.Key> {}
