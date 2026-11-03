package vn.tienlo.api.lifecycle.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.lifecycle.persistence.entity.LegacyStashEntity;

public interface LegacyStashRepository extends JpaRepository<LegacyStashEntity, UUID> {}
