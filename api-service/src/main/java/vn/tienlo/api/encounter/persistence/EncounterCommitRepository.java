package vn.tienlo.api.encounter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.encounter.persistence.entity.EncounterCommitEntity;

public interface EncounterCommitRepository extends JpaRepository<EncounterCommitEntity, String> {}
