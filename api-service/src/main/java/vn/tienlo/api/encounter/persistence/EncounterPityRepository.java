package vn.tienlo.api.encounter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.encounter.persistence.entity.EncounterPityEntity;

public interface EncounterPityRepository extends JpaRepository<EncounterPityEntity, UUID> {}
