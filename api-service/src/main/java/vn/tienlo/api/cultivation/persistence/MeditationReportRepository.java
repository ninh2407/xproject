package vn.tienlo.api.cultivation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.cultivation.persistence.entity.MeditationReportEntity;

public interface MeditationReportRepository extends JpaRepository<MeditationReportEntity, String> {}
