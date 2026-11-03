package vn.tienlo.api.social.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.social.persistence.entity.NpcSectMembershipEntity;

public interface NpcSectMembershipRepository extends JpaRepository<NpcSectMembershipEntity, NpcSectMembershipEntity.Key> {}
