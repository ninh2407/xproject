package vn.tienlo.api.talent.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.talent.persistence.entity.TalentChangeLogEntity;

public interface TalentChangeLogRepository extends JpaRepository<TalentChangeLogEntity, String> {}
