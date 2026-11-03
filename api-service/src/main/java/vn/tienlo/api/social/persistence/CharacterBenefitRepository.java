package vn.tienlo.api.social.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.social.persistence.entity.CharacterBenefitEntity;

public interface CharacterBenefitRepository extends JpaRepository<CharacterBenefitEntity, UUID> {}
