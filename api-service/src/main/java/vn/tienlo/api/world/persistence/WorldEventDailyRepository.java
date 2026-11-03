package vn.tienlo.api.world.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.world.persistence.entity.WorldEventDailyEntity;

public interface WorldEventDailyRepository extends JpaRepository<WorldEventDailyEntity, WorldEventDailyEntity.Key> {}
