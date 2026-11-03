package vn.tienlo.api.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.inventory.persistence.entity.CharacterQuickSlotEntity;

public interface CharacterQuickSlotRepository extends JpaRepository<CharacterQuickSlotEntity, CharacterQuickSlotEntity.Key> {}
