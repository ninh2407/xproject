package vn.tienlo.api.monetization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.monetization.persistence.entity.PaymentOrderEntity;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, UUID> {}
