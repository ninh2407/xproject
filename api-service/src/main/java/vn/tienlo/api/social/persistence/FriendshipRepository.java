package vn.tienlo.api.social.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.social.persistence.entity.FriendshipEntity;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, FriendshipEntity.Key> {}
