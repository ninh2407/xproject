package vn.tienlo.api.encounter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.encounter.persistence.entity.EncounterAccountHistoryEntity;

public interface EncounterAccountHistoryRepository extends JpaRepository<EncounterAccountHistoryEntity, EncounterAccountHistoryEntity.Key> {}
