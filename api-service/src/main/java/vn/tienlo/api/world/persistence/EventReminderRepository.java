package vn.tienlo.api.world.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.world.persistence.entity.EventReminderEntity;

public interface EventReminderRepository extends JpaRepository<EventReminderEntity, EventReminderEntity.Key> {}
