package vn.tienlo.api.monetization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.monetization.persistence.entity.CosmeticEntitlementEntity;

public interface CosmeticEntitlementRepository extends JpaRepository<CosmeticEntitlementEntity, CosmeticEntitlementEntity.Key> {}
