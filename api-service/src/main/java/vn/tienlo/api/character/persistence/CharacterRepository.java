package vn.tienlo.api.character.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.character.persistence.entity.CharacterEntity;

public interface CharacterRepository extends JpaRepository<CharacterEntity, UUID> {}
