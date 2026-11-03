package vn.tienlo.api.world.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.world.persistence.entity.WorldEventParticipationEntity;

public interface WorldEventParticipationRepository extends JpaRepository<WorldEventParticipationEntity, WorldEventParticipationEntity.Key> {}
