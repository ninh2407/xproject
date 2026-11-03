package vn.tienlo.api.beast.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.beast.persistence.entity.BeastBossCaptureEntity;

public interface BeastBossCaptureRepository extends JpaRepository<BeastBossCaptureEntity, BeastBossCaptureEntity.Key> {}
