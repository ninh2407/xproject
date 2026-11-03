package vn.tienlo.api.world.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.world.persistence.entity.CodexEntryEntity;

public interface CodexEntryRepository extends JpaRepository<CodexEntryEntity, CodexEntryEntity.Key> {}
