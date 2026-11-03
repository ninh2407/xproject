package vn.tienlo.api.craft.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.craft.persistence.entity.RecipeKnownEntity;

public interface RecipeKnownRepository extends JpaRepository<RecipeKnownEntity, RecipeKnownEntity.Key> {}
