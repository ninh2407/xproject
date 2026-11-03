package vn.tienlo.api.security.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.security.persistence.entity.PurchaseReceiptEntity;

public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceiptEntity, String> {}
