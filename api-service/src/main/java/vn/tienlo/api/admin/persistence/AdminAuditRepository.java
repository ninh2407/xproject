package vn.tienlo.api.admin.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.admin.persistence.entity.AdminAuditEntity;

public interface AdminAuditRepository extends JpaRepository<AdminAuditEntity, Long> {}
