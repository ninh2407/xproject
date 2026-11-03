package vn.tienlo.api.economy.persistence;

import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import vn.tienlo.api.economy.persistence.entity.AuctionListingEntity;

@Repository
public class AuctionListingSearchJooqRepository {
    private static final Table<?> AUCTION_LISTING = DSL.table(DSL.name("game", "auction_listing"));
    private static final Field<String> TEMPLATE_ID = DSL.field(DSL.name("template_id"), String.class);
    private static final Field<Long> UNIT_PRICE = DSL.field(DSL.name("unit_price"), Long.class);
    private static final Field<String> STATE = DSL.field(DSL.name("state"), String.class);
    private static final Field<UUID> LISTING_ID = DSL.field(DSL.name("listing_id"), UUID.class);

    private final DSLContext dsl;

    public AuctionListingSearchJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** DB-R-060: search active listings through the documented template/price index. */
    public List<AuctionListingEntity> findActiveByTemplateAndMaxPrice(
            String templateId,
            long maximumUnitPrice,
            int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        return dsl.selectFrom(AUCTION_LISTING)
                .where(TEMPLATE_ID.eq(templateId))
                .and(UNIT_PRICE.le(maximumUnitPrice))
                .and(STATE.eq("ACTIVE"))
                .orderBy(UNIT_PRICE.asc(), LISTING_ID.asc())
                .limit(limit)
                .fetchInto(AuctionListingEntity.class);
    }
}
