package vn.tienlo.api.world.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.world.persistence.entity.DiscoveredPoiEntity;

public interface DiscoveredPoiRepository extends JpaRepository<DiscoveredPoiEntity, DiscoveredPoiEntity.Key> {}
