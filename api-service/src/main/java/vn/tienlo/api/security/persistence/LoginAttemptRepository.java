package vn.tienlo.api.security.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.security.persistence.entity.LoginAttemptEntity;

public interface LoginAttemptRepository extends JpaRepository<LoginAttemptEntity, Long> {}
