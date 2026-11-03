package vn.tienlo.api.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.inventory.persistence.entity.MailEntity;

public interface MailRepository extends JpaRepository<MailEntity, UUID> {}
