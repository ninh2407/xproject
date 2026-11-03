package vn.tienlo.api.encounter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.encounter.persistence.entity.EncounterSessionEntity;

public interface EncounterSessionRepository extends JpaRepository<EncounterSessionEntity, UUID> {}
