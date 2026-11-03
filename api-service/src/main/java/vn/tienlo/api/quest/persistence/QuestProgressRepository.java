package vn.tienlo.api.quest.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.quest.persistence.entity.QuestProgressEntity;

public interface QuestProgressRepository extends JpaRepository<QuestProgressEntity, QuestProgressEntity.Key> {}
