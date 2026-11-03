package vn.tienlo.api.quest.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.quest.persistence.entity.QuestRewardLogEntity;

public interface QuestRewardLogRepository extends JpaRepository<QuestRewardLogEntity, String> {}
