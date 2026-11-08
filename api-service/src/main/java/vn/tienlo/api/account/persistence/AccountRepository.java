package vn.tienlo.api.account.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tienlo.api.account.persistence.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from AccountEntity account where account.id = :accountId")
    Optional<AccountEntity> findByIdForUpdate(@Param("accountId") UUID accountId);
}
