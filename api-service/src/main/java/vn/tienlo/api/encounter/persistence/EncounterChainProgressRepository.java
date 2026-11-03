package vn.tienlo.api.encounter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.encounter.persistence.entity.EncounterChainProgressEntity;

public interface EncounterChainProgressRepository extends JpaRepository<EncounterChainProgressEntity, EncounterChainProgressEntity.Key> {}
