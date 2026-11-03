package vn.tienlo.api.home.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.home.persistence.entity.CavePlotEntity;

public interface CavePlotRepository extends JpaRepository<CavePlotEntity, CavePlotEntity.Key> {}
