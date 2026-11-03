package vn.tienlo.api.technique.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.technique.persistence.entity.MasteryBatchEntity;

public interface MasteryBatchRepository extends JpaRepository<MasteryBatchEntity, String> {}
