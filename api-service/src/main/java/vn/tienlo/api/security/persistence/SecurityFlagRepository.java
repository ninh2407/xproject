package vn.tienlo.api.security.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.security.persistence.entity.SecurityFlagEntity;

public interface SecurityFlagRepository extends JpaRepository<SecurityFlagEntity, UUID> {}
