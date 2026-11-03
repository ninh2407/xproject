package vn.tienlo.api.account.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.account.persistence.entity.AccountSessionEntity;

public interface AccountSessionRepository extends JpaRepository<AccountSessionEntity, UUID> {}
