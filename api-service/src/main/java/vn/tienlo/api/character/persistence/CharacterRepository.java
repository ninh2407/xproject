package vn.tienlo.api.character.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import vn.tienlo.api.character.persistence.entity.CharacterEntity;

public interface CharacterRepository extends JpaRepository<CharacterEntity, UUID> {
    List<CharacterEntity> findAllByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
