package vn.tienlo.api.account.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.account.persistence.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {}
