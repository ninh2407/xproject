package vn.tienlo.api.cultivation.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import vn.tienlo.api.cultivation.persistence.entity.CultivationLedgerEntity;

@RepositoryDefinition(domainClass = CultivationLedgerEntity.class, idClass = Long.class)
public interface CultivationLedgerRepository extends Repository<CultivationLedgerEntity, Long> {
    Optional<CultivationLedgerEntity> findById(Long id);
    List<CultivationLedgerEntity> findAll();
    long count();
}
