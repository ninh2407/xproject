package vn.tienlo.api.monetization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.monetization.persistence.entity.SeasonPassProgressEntity;

public interface SeasonPassProgressRepository extends JpaRepository<SeasonPassProgressEntity, SeasonPassProgressEntity.Key> {}
