package vn.tienlo.api.economy.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import vn.tienlo.api.economy.persistence.entity.EconomySnapshotEntity;

public interface EconomySnapshotRepository extends JpaRepository<EconomySnapshotEntity, LocalDate> {}
