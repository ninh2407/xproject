package vn.tienlo.api.social.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.social.persistence.entity.ChatMuteEntity;

public interface ChatMuteRepository extends JpaRepository<ChatMuteEntity, ChatMuteEntity.Key> {}
