package vn.tienlo.api.monetization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.monetization.persistence.entity.GiftLogEntity;

public interface GiftLogRepository extends JpaRepository<GiftLogEntity, Long> {}
