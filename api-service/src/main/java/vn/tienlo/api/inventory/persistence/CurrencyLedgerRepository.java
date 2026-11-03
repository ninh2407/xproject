package vn.tienlo.api.inventory.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import vn.tienlo.api.inventory.persistence.entity.CurrencyLedgerEntity;

@RepositoryDefinition(domainClass = CurrencyLedgerEntity.class, idClass = Long.class)
public interface CurrencyLedgerRepository extends Repository<CurrencyLedgerEntity, Long> {
    Optional<CurrencyLedgerEntity> findById(Long id);
    List<CurrencyLedgerEntity> findAll();
    long count();
}
