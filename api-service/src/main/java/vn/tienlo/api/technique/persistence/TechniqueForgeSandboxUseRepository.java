package vn.tienlo.api.technique.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tienlo.api.technique.persistence.entity.TechniqueForgeSandboxUseEntity;

public interface TechniqueForgeSandboxUseRepository extends JpaRepository<TechniqueForgeSandboxUseEntity, TechniqueForgeSandboxUseEntity.Key> {}
