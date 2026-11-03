package vn.tienlo.api.beast.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.beast.persistence.entity.BeastCapturePityEntity;

public interface BeastCapturePityRepository extends JpaRepository<BeastCapturePityEntity, BeastCapturePityEntity.Key> {}
