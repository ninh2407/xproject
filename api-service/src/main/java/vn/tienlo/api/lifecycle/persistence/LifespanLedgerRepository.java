package vn.tienlo.api.lifecycle.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import vn.tienlo.api.lifecycle.persistence.entity.LifespanLedgerEntity;

@RepositoryDefinition(domainClass = LifespanLedgerEntity.class, idClass = Long.class)
public interface LifespanLedgerRepository extends Repository<LifespanLedgerEntity, Long> {
    Optional<LifespanLedgerEntity> findById(Long id);
    List<LifespanLedgerEntity> findAll();
    long count();
}
