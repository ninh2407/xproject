package vn.tienlo.api.talent.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.talent.persistence.entity.AccountTalentSeenEntity;

public interface AccountTalentSeenRepository extends JpaRepository<AccountTalentSeenEntity, AccountTalentSeenEntity.Key> {}
