package vn.tienlo.api.lifecycle.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import vn.tienlo.api.lifecycle.persistence.entity.RebirthLedgerEntity;

@RepositoryDefinition(domainClass = RebirthLedgerEntity.class, idClass = Long.class)
public interface RebirthLedgerRepository extends Repository<RebirthLedgerEntity, Long> {
    Optional<RebirthLedgerEntity> findById(Long id);
    List<RebirthLedgerEntity> findAll();
    long count();
}
