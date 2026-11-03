package vn.tienlo.api.lifecycle.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.lifecycle.persistence.entity.AgeAccrualReportEntity;

public interface AgeAccrualReportRepository extends JpaRepository<AgeAccrualReportEntity, String> {}
