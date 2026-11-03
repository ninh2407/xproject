package vn.tienlo.api.craft.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.craft.persistence.entity.ForgePendingRerollEntity;

public interface ForgePendingRerollRepository extends JpaRepository<ForgePendingRerollEntity, UUID> {}
