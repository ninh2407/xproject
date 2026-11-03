package vn.tienlo.api.encounter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.encounter.persistence.entity.AccountFlagEntity;

public interface AccountFlagRepository extends JpaRepository<AccountFlagEntity, AccountFlagEntity.Key> {}
