package vn.tienlo.api.economy.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import vn.tienlo.api.economy.persistence.entity.AuctionListingEntity;

public interface AuctionListingRepository extends JpaRepository<AuctionListingEntity, UUID> {}
