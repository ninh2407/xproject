package vn.tienlo.api.character.persistence;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
public class CharacterSnapshotJooqRepository {
    private static final Table<?> CHARACTER = DSL.table(DSL.name("game", "character"));
    private static final Field<UUID> ID = DSL.field(DSL.name("id"), UUID.class);
    private static final Field<String> MAP_ID = DSL.field(DSL.name("map_id"), String.class);
    private static final Field<Short> CHANNEL = DSL.field(DSL.name("channel"), Short.class);
    private static final Field<BigDecimal> POS_X = DSL.field(DSL.name("pos_x"), BigDecimal.class);
    private static final Field<BigDecimal> POS_Z = DSL.field(DSL.name("pos_z"), BigDecimal.class);
    private static final Field<Short> FACING = DSL.field(DSL.name("facing"), Short.class);
    private static final Field<Integer> HP = DSL.field(DSL.name("hp"), Integer.class);
    private static final Field<Integer> MP = DSL.field(DSL.name("mp"), Integer.class);
    private static final Field<Long> SNAPSHOT_SEQ = DSL.field(DSL.name("snapshot_seq"), Long.class);
    private static final Field<java.time.OffsetDateTime> SNAPSHOT_AT =
            DSL.field(DSL.name("snapshot_at"), java.time.OffsetDateTime.class);

    private final DSLContext dsl;

    public CharacterSnapshotJooqRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** DB-R-060: apply a snapshot only if its sequence is newer than the stored snapshot. */
    public int updateIfNewer(CharacterSnapshot snapshot) {
        return dsl.update(CHARACTER)
                .set(MAP_ID, snapshot.mapId())
                .set(CHANNEL, snapshot.channel())
                .set(POS_X, snapshot.posX())
                .set(POS_Z, snapshot.posZ())
                .set(FACING, snapshot.facing())
                .set(HP, snapshot.hp())
                .set(MP, snapshot.mp())
                .set(SNAPSHOT_SEQ, snapshot.sequence())
                .set(SNAPSHOT_AT, snapshot.capturedAt().atOffset(ZoneOffset.UTC))
                .where(ID.eq(snapshot.characterId()))
                .and(SNAPSHOT_SEQ.lt(snapshot.sequence()))
                .execute();
    }
}
