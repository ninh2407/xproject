package vn.tienlo.api.danger.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.danger.persistence.entity.DangerRunMemberEntity;

public interface DangerRunMemberRepository extends JpaRepository<DangerRunMemberEntity, DangerRunMemberEntity.Key> {}
