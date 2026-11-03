package vn.tienlo.api.social.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import vn.tienlo.api.social.persistence.entity.PlayerSectLedgerEntity;

@RepositoryDefinition(domainClass = PlayerSectLedgerEntity.class, idClass = Long.class)
public interface PlayerSectLedgerRepository extends Repository<PlayerSectLedgerEntity, Long> {
    Optional<PlayerSectLedgerEntity> findById(Long id);
    List<PlayerSectLedgerEntity> findAll();
    long count();
}
