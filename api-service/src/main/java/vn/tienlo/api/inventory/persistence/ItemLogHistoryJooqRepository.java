package vn.tienlo.api.inventory.persistence;

import java.util.List;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import vn.tienlo.api.inventory.persistence.entity.ItemLogEntity;

@Repository
public class ItemLogHistoryJooqRepository {
    private static final Table<?> ITEM_LOG = DSL.table(DSL.name("game", "item_log"));
    private static final Field<UUID> ITEM_INSTANCE_ID = DSL.field(DSL.name("item_instance_id"), UUID.class);
    private static final Field<java.time.OffsetDateTime> CREATED_AT =
            DSL.field(DSL.name("created_at"), java.time.OffsetDateTime.class);

    private final DSLContext dsl;

    public ItemLogHistoryJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** DB-R-060: use the item history index for support/audit lookups. */
    public List<ItemLogEntity> findRecentByItemInstanceId(UUID itemInstanceId, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        return dsl.selectFrom(ITEM_LOG)
                .where(ITEM_INSTANCE_ID.eq(itemInstanceId))
                .orderBy(CREATED_AT.desc())
                .limit(limit)
                .fetchInto(ItemLogEntity.class);
    }
}
