package vn.tienlo.api.lifecycle.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.lifecycle.persistence.entity.AccountVaultEntity;

public interface AccountVaultRepository extends JpaRepository<AccountVaultEntity, AccountVaultEntity.Key> {}
