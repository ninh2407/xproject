package vn.tienlo.api.home.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.home.persistence.entity.SiteHistoryEntity;

public interface SiteHistoryRepository extends JpaRepository<SiteHistoryEntity, Long> {}
