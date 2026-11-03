package vn.tienlo.api.quest.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.quest.persistence.entity.SectTaskBoardEntity;

public interface SectTaskBoardRepository extends JpaRepository<SectTaskBoardEntity, SectTaskBoardEntity.Key> {}
