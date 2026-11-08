package vn.tienlo.api.server.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.server.persistence.entity.ServerRegistryEntity;

public interface ServerRegistryRepository extends JpaRepository<ServerRegistryEntity, Short> {
    List<ServerRegistryEntity> findAllByStatusInOrderByServerIdAsc(List<String> statuses);
}
