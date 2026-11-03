package vn.tienlo.api.platform.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.platform.persistence.entity.RngAuditEntity;

public interface RngAuditRepository extends JpaRepository<RngAuditEntity, RngAuditEntity.Key> {}
