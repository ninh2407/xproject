package vn.tienlo.api.social.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.social.persistence.entity.ChatMessageLogEntity;

public interface ChatMessageLogRepository extends JpaRepository<ChatMessageLogEntity, ChatMessageLogEntity.Key> {}
