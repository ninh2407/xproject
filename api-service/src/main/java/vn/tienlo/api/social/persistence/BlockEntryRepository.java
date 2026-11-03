package vn.tienlo.api.social.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.social.persistence.entity.BlockEntryEntity;

public interface BlockEntryRepository extends JpaRepository<BlockEntryEntity, BlockEntryEntity.Key> {}
