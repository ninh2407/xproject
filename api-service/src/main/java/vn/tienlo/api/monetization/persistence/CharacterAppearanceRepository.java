package vn.tienlo.api.monetization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.monetization.persistence.entity.CharacterAppearanceEntity;

public interface CharacterAppearanceRepository extends JpaRepository<CharacterAppearanceEntity, UUID> {}
