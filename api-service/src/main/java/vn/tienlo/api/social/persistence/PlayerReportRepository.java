package vn.tienlo.api.social.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.social.persistence.entity.PlayerReportEntity;

public interface PlayerReportRepository extends JpaRepository<PlayerReportEntity, UUID> {}
