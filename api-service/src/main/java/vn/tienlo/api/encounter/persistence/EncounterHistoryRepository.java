package vn.tienlo.api.encounter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.encounter.persistence.entity.EncounterHistoryEntity;

public interface EncounterHistoryRepository extends JpaRepository<EncounterHistoryEntity, EncounterHistoryEntity.Key> {}
