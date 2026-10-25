# TD-06 — Game Server Architecture

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-06 · Prefix ID: `GS` |
| Owner | Server Dev (Game Server) |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 0 (spike netcode), Phase 1 (combat + 1 bản đồ), Phase 2 (khung MMO) |
| Quyết định nền | D-19 (Colyseus / TypeScript, mô phỏng viết một lần trong `shared/sim`), [TD-01](00-tdd.md) §3, §10–§14, [TD-02](01-network.md), [TD-12](11-world-maps.md), [TD-14](13-geom2d.md) |
| Liên quan | [TD-03 Database](02-database.md) (Game Server không truy cập DB) · [TD-07 Gamedata](06-gamedata.md) (`bundle.server`) · [TD-13 Map Format](12-map-editor.md) · [GD-06](../design/12-stats-formulas.md) · [GD-07](../design/13-skills.md) §4–§5 · [GD-12](../design/18-enemies-ai.md) · [GD-21](../design/22-open-world.md) · [GD-22](../design/23-encounters.md) · [GD-25](../design/26-spirit-beasts.md) · [GD-28](../design/27-danger-zones.md) · [GD-32](../design/31-pvp-infamy.md) |

> TD-01 nói **Game Server được quyết định gì** (mô phỏng thời gian thực, không giữ dữ liệu có giá trị). TD-02 nói **giao tiếp với client thế nào**; TD-12 nói **room nào chạy ở đâu**. Tài liệu này nói **bên trong một tiến trình Game Server được tổ chức ra sao**: thư mục, mô hình thực thể, thứ tự hệ thống trong tick, vòng đời room, khuôn mẫu code để AI sinh nhất quán, gọi API, sự kiện, AI quái, cấu hình, quan sát, kiểm thử.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-06 | Mục |
|--------------|-----------------|-----|
| Mọi GD có phần "chạy ở Game Server" | Nơi đặt code (system nào), khuôn mẫu system / handler | §2, §5 |
| TD-02 | Hiện thực thứ tự tick NW-R-010, rewind, AOI | §4 |
| TD-12 | Hiện thực vòng đời room, handoff, drain | §6 |
| TD-01 | Hàng đợi thử lại, batching, phản ứng sự kiện | §7, §8 |
| TD-05 (client) | Phần `shared/sim` dùng chung | §3 |

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="gs-r-001"></a>**GS-R-001** | Game Server **không** kết nối PostgreSQL. Mọi dữ liệu bền đi qua API nội bộ (TD-01 TA-R-001); Valkey chỉ cho `ctx:*`, `charlock:*`, stream sự kiện, presence Colyseus, hàng đợi thử lại (TD-03 §12). |
| <a id="gs-r-002"></a>**GS-R-002** | Logic mô phỏng (va chạm, di chuyển, công thức, máy trạng thái hành động, AI ra quyết định, chạy Ink) nằm trong `@tienlo/sim`; `game-server` chỉ **điều phối** (gọi sim theo tick, I/O, mạng). Không viết lại công thức trong `game-server` (TA-R-007). |
| <a id="gs-r-003"></a>**GS-R-003** | Một room chạy trên **một luồng** (event loop Node.js), tick cố định 20Hz; không `await` I/O bên trong bước mô phỏng của tick (I/O chạy song song, kết quả áp vào tick sau qua hàng đợi). |
| <a id="gs-r-004"></a>**GS-R-004** | Mọi message C2S được kiểm tra bằng schema zod trước khi vào hàng đợi lệnh (TD-02 NW-R-063); handler không bao giờ sửa trạng thái thế giới trực tiếp — chỉ **xếp lệnh** cho tick xử lý. |
| <a id="gs-r-005"></a>**GS-R-005** | Mọi gọi API có tác dụng phụ mang khóa idempotency và đi qua `ApiClient` (hàng đợi thử lại bền trong Valkey, TA-R-054); không gọi `fetch` thô. |
| <a id="gs-r-006"></a>**GS-R-006** | Dữ liệu `@serverOnly` (bảng rơi, trọng số spawn, vị trí bí mật — GD-29) chỉ tồn tại trong RAM của Game Server; **không** đưa vào State hay message trừ khi đã đến lượt người chơi được biết (NW-R-042). |

---

## 2. Cấu trúc thư mục

```
game-server/                              # @tienlo/game-server
├── package.json · tsconfig.json (strict, ESM, "moduleResolution": "bundler")
├── src/
│   ├── main.ts                           # bootstrap: config → logger → telemetry → gamedata → redis → colyseus server
│   ├── config/
│   │   ├── env.ts                        # zod parse process.env (GS-R-090)
│   │   └── flags.ts                      # feature flag (pvpEnabled, beastsEnabled, ...) đọc từ API meta + env
│   ├── colyseus/
│   │   ├── server.ts                     # Server + RedisPresence (dùng với Valkey) + RedisDriver (dùng với Valkey), đăng ký room
│   │   └── auth.ts                       # verify joinTicket (JWT) + SET NX ticket:{jti} (TD-01 §14)
│   ├── rooms/
│   │   ├── BaseGameRoom.ts               # khung chung: tick loop, hàng đợi lệnh, thứ tự system, snapshot, ctx, deathlog
│   │   ├── OpenMapRoom.ts                # HUB / F theo kênh (TD-12 §2)
│   │   ├── InstanceRoom.ts               # cơ sở cho instance
│   │   ├── instances/
│   │   │   ├── SecretRealmRoom.ts        # bí cảnh (BC)
│   │   │   ├── DangerZoneRoom.ts         # hiểm địa (HD, GD-28) — pha, hấp hối, di hài, lối thoát
│   │   │   ├── EncounterCaveRoom.ts      # động phủ cổ (KN, GD-22)
│   │   │   ├── CaveRoom.ts               # động phủ cá nhân (DP, GD-30) — không chiến đấu
│   │   │   ├── SectGroundsRoom.ts        # trú địa (TD)
│   │   │   └── TrialRoom.ts              # TRIAL: tàn hồn, thủ vệ, thiên kiếp, Diễn Pháp Tràng (variant theo kind)
│   │   └── registry.ts                   # mapType → class room
│   ├── world/
│   │   ├── World.ts                      # kho thực thể + chỉ mục không gian của một room (§3)
│   │   ├── components.ts                 # định nghĩa component (re-export kiểu từ @tienlo/sim)
│   │   ├── commandQueue.ts               # lệnh đã validate, theo thứ tự nhận
│   │   └── eventBus.ts                   # sự kiện trong-room (HitEvent, DeathEvent...) cho các system sau
│   ├── systems/                          # mỗi file một system (§5.1), tên = thứ tự trong §4
│   │   ├── input.system.ts               # áp C2S_Input (NW-R-030)
│   │   ├── movement.system.ts            # sim.move, dodge, knockback (TD-14 §4)
│   │   ├── actionState.system.ts         # máy trạng thái hành động GD-07 §4 + đệm lệnh SK-R-010
│   │   ├── skills.system.ts              # CastSkill: kiểm tra SK-R-070, windup, lượt trúng (SK-R-072..074)
│   │   ├── projectiles.system.ts         # sim.stepProjectile (TD-14 §6)
│   │   ├── hitResolution.system.ts       # tính sát thương GD-06 §6.3, xét lại phòng thủ (NW-R-050..051)
│   │   ├── statuses.system.ts            # status GD-06 (tick DoT, hết hạn)
│   │   ├── ai.system.ts                  # quyết định quái (GD-12), tần suất riêng (§9)
│   │   ├── summons.system.ts             # vật triệu hồi + linh thú xuất chiến (GD-07 §5.8, GD-25)
│   │   ├── spawn.system.ts               # sinh / hồi quái, Linh chủng (GD-25 SB-R-011), Chấp Pháp / Hóa Thân (GD-32)
│   │   ├── loot.system.ts                # roll rơi đồ (PRNG room, seed gửi kèm loot/grant), túi đồ trên đất
│   │   ├── gather.system.ts              # hái / khai khoáng, khóa vận hái (GD-21 OW-R-102)
│   │   ├── encounter.system.ts           # kích hoạt cơ duyên + chạy Ink qua sim/ink (GD-22)
│   │   ├── danger.system.ts              # pha hiểm địa, hấp hối, quy tức (GD-28) — chỉ DangerZoneRoom
│   │   ├── pvp.system.ts                 # chế độ PK, cờ xám, sát nghiệp báo API (GD-32)
│   │   ├── fog.system.ts                 # fog 2s, gom ghi 60s (TD-12 §8)
│   │   ├── aoi.system.ts                 # lưới AOI 16m, view từng client (NW-R-040..044)
│   │   ├── contextWriter.system.ts       # ghi ctx:{characterId} (TD-01 §11)
│   │   ├── snapshot.system.ts            # snapshot nhân vật 30s (TA-R-110)
│   │   ├── deathLog.system.ts            # bộ đệm vòng 60s (TD-01 §12)
│   │   └── history.system.ts             # lịch sử vị trí 20 tick cho rewind (NW-R-050)
│   ├── handlers/                         # message handler C2S (§5.2) — một file mỗi domain
│   │   ├── combat.handlers.ts · movement.handlers.ts · inventory.handlers.ts
│   │   ├── interaction.handlers.ts · encounter.handlers.ts · danger.handlers.ts
│   │   ├── social.handlers.ts (chat, TD-02 §11) · beast.handlers.ts · pvp.handlers.ts
│   │   └── index.ts                      # bảng message → { schema, rateLimitGroup, handler }
│   ├── net/
│   │   ├── state/                        # cập nhật @colyseus/schema từ World (§4 bước 9)
│   │   ├── views.ts                      # StateView theo AOI (hoặc S2C_Snapshot dự phòng, NW-R-021)
│   │   ├── rateLimit.ts                  # token bucket theo nhóm (NW-R-064)
│   │   └── send.ts                       # gửi S2C có kiểu, gom theo tick
│   ├── adapters/
│   │   ├── api/ApiClient.ts              # client sinh từ @tienlo/contracts + idempotency + retry queue (§7)
│   │   ├── api/batchers.ts               # gom kill / mastery / age / fog / codex / bond (§7.2)
│   │   ├── redis/streams.ts              # consumer group gs.{processId} (TD-01 TA-R-083)
│   │   ├── redis/context.ts · redis/charlock.ts · redis/chat.ts
│   │   └── world/directory.ts            # heartbeat room lên API (TD-12 WM-R-010)
│   ├── events/                           # phản ứng sự kiện domain (§8)
│   │   └── handlers/*.ts
│   ├── observability/ (logger.ts pino · metrics.ts prom-client · tracing.ts OpenTelemetry)
│   └── testing/                          # room giả, đồng hồ giả, ApiClient giả (§12)
└── test/ (unit, integration, bots/, load/)
```

- <a id="gs-r-010"></a>**GS-R-010** Tên file theo mẫu `*.system.ts`, `*.handlers.ts`, `*.event.ts`; mỗi system export đúng một đối tượng `System` (§5.1). Code domain mới (GD mới) thêm system / handler theo đúng khuôn, không đặt logic trong class room.

---

## 3. Mô hình thực thể

- <a id="gs-r-020"></a>**GS-R-020** Dùng **component store kiểu ECS nhẹ tự viết** (mảng / Map theo `entityId` cho từng component), **không** dùng thư viện ECS ngoài phía server. Lý do: số thực thể mỗi room nhỏ (≤ ~400), cần kiểm soát thứ tự system và sự kiện; tránh phụ thuộc. Client có thể dùng bitECS cho render (TD-05) nhưng **kiểu component** dùng chung từ `@tienlo/sim`.
- <a id="gs-r-021"></a>**GS-R-021** Component cốt lõi (kiểu định nghĩa trong `@tienlo/sim/components`):

| Component | Trường chính | Ai dùng |
|-----------|--------------|---------|
| `Transform` | `pos: Vec2`, `facing: Vec2` | movement, aoi, history |
| `Body` | `radius`, `solid` | TD-14 |
| `Mover` | `speed`, `intent: IntentFrame` (người chơi) / `path` (quái) | movement |
| `Combatant` | `stats: CombatStats` (GD-06, tính bởi `sim.computeStats`), `hp`, `mp`, `stamina`, `energy`, `poise` | combat |
| `ActionState` | `state`, `stateStartTick`, `buffered`, `castSkillRef`, `armorLevel` | actionState (GD-07 §4) |
| `Cooldowns` | `bySkillRef: Map<string, tick>` | skills |
| `Statuses` | danh sách `{ id, stacks, expiresTick, sourceId }` | statuses |
| `PlayerLink` | `characterId`, `accountId`, `sessionId`, `rttMs`, `lastInputSeq`, `pkState`, `flags` | handlers, net |
| `Monster` | `monsterId`, `rank`, `aiState`, `threat`, `spawnRef`, `leashCenter`, `variant` (Linh chủng) | ai, spawn, loot |
| `Projectile` | TD-14 `ProjectileState` | projectiles |
| `Interactable` | `poiId`, `kind`, `radius`, `lock` | gather, interaction |
| `Hidden` | `hiddenLevel`, `discoveredBy: Set<characterId>` | aoi (NW-R-042) |
| `Owner` | `ownerEntityId` (vật triệu hồi, linh thú) | summons |

- <a id="gs-r-022"></a>**GS-R-022** Phần dùng chung với client qua `@tienlo/sim`: kiểu component, hàm thuần (`move`, `shapeGeometry`, `queryTargets`, `stepProjectile`, `computeStats`, `resolveHit`, `actionStateReducer`, `aiDecide`, `worldClock`, `inkRunner`). Client dùng một tập con (dự đoán di chuyển / hoạt ảnh, vẽ vùng cảnh báo, xem trước). **Không** dùng chung: AOI, rơi đồ, spawn, AI chạy thật, I/O.

---

> **Bổ sung GD-31:** system `flight` (máy trạng thái bay, tiêu linh lực, hạ cánh / rơi) chạy **trước** `movement` trong thứ tự tick.

## 4. Thứ tự hệ thống trong một tick

Tick 50ms (TD-02 NW-R-010). Ngân sách cho một room 150 người + 200 quái, p99 tổng < 25ms:

| # | Bước | System | Ngân sách p99 |
|:-:|------|--------|:---:|
| 0 | Lấy lệnh đã validate từ `commandQueue` (theo thứ tự nhận) + kết quả I/O đã về (phản hồi API, sự kiện domain) | `BaseGameRoom.drain()` | 0,5ms |
| 1 | Áp input di chuyển | `input` | 1ms |
| 2 | Di chuyển, né, đẩy lùi, tách quái | `movement` | 3ms |
| 3 | Lệnh hành động → máy trạng thái (đánh, kỹ năng, né, đỡ, tụ lực) | `actionState`, `skills` (giai đoạn bắt đầu) | 2ms |
| 4 | AI quái (theo nhóm tần suất §9) → lệnh hành động của quái | `ai`, rồi `actionState` cho quái | 4ms |
| 5 | Đạn, vùng, lượt trúng đến hạn (rewind từ `history`), xét lại phòng thủ | `skills` (lượt trúng), `projectiles`, `hitResolution` | 5ms |
| 6 | Status, hồi tài nguyên, chết / hấp hối / hồi sinh | `statuses`, `danger`, `pvp` | 1,5ms |
| 7 | Thế giới: spawn, rơi đồ, hái, cơ duyên, triệu hồi | `spawn`, `loot`, `gather`, `encounter`, `summons` | 2ms |
| 8 | Ghi lịch sử vị trí; AOI; fog; `ctx`; death log | `history`, `aoi`, `fog`, `contextWriter`, `deathLog` | 3ms |
| 9 | Ghi State (schema) + gửi S2C đã gom; xếp I/O ra ngoài (API batch, snapshot) | `net/state`, `send`, `snapshot` | 2ms |

- <a id="gs-r-030"></a>**GS-R-030** Thứ tự trên là **cố định** và khai báo một lần trong `BaseGameRoom.systems` (mảng có thứ tự). Room biến thể chỉ được **bật / tắt** system (vd. `CaveRoom` tắt combat / ai / spawn), không đổi thứ tự.
- <a id="gs-r-031"></a>**GS-R-031** System trao đổi qua `World` (component) và `eventBus` trong tick (vd. `hitResolution` phát `DamageEvent` → `statuses`, `deathLog`, `pvp`, `loot` đọc ở bước sau cùng tick). `eventBus` xóa cuối tick.
- <a id="gs-r-032"></a>**GS-R-032** Đo thời gian từng system mỗi tick (histogram §11); tick vượt 50ms không "đuổi" quá 2 tick (NW-R-011).

---

## 5. Khuôn mẫu code (để AI sinh nhất quán)

### 5.1. System
```ts
// src/systems/_template.system.ts
import type { System, TickContext } from '../world/types';

export const exampleSystem: System = {
  name: 'example',
  // tần suất: chạy mỗi n tick (1 = mọi tick). AI dùng nhóm tần suất riêng (§9).
  everyTicks: 1,
  // room nào bật system này (mặc định mọi room mở + instance có combat)
  enabledIn: (room) => room.features.combat,
  run(ctx: TickContext): void {
    const { world, tick, gd, events, rng, log } = ctx;
    for (const id of world.query('Combatant', 'Statuses')) {
      // chỉ gọi hàm thuần của @tienlo/sim, không tự viết công thức (GS-R-002)
      // không await, không I/O (GS-R-003); cần I/O → ctx.io.enqueue(...)
    }
  },
};

// src/world/types.ts (trích)
export interface TickContext {
  readonly tick: number; readonly nowMs: number;          // đồng hồ server
  readonly world: World; readonly gd: ServerGamedata;     // dữ liệu chỉ đọc
  readonly events: EventBus;                               // sự kiện trong tick
  readonly rng: RoomRng;                                    // xoshiro128** seed theo room (TA-R-072)
  readonly io: IoQueue;                                     // xếp gọi API / Valkey, chạy sau tick
  readonly send: Sender;                                    // xếp S2C, gửi ở bước 9
  readonly log: Logger;
}
```

### 5.2. Message handler
```ts
// src/handlers/combat.handlers.ts (trích)
import { z } from 'zod';
import { defineHandler } from './define';

export const castSkill = defineHandler({
  type: 'C2S_CastSkill',                          // số message ổn định sinh từ danh mục (NW-R-061)
  schema: z.object({
    seq: z.number().int().nonnegative(),
    slot: z.enum(['Q', 'E', 'R', 'F', 'V']),
    aim: z.tuple([z.number().finite(), z.number().finite()]),
    clientTimeMs: z.number().int(),
    holdMs: z.number().int().min(0).max(5000).optional(),
  }).strict(),                                    // thừa trường → bỏ (NW-R-001)
  rateLimitGroup: 'combat',                       // NW-R-064
  // chỉ chuyển thành lệnh; KHÔNG sửa World ở đây (GS-R-004)
  toCommand: (msg, player) => ({ kind: 'CAST_SKILL', entityId: player.entityId, ...msg, receivedTick: player.room.tick }),
});
```

- <a id="gs-r-040"></a>**GS-R-040** `defineHandler` tự gắn: kiểm tra schema, rate limit, đếm vi phạm (NW-R-063..065), log có `characterId` + `seq`, đẩy lệnh vào `commandQueue`. Handler từ chối sớm chỉ khi lỗi định dạng; kiểm tra nghiệp vụ (hồi chiêu, tài nguyên…) nằm trong system và trả `S2C_CastRejected` / `S2C_Error` có `seq`.

### 5.3. Phản ứng sự kiện domain
```ts
// src/events/handlers/statsInputsChanged.event.ts
export const onStatsInputsChanged = defineEventHandler({
  type: 'stats.inputs_changed', version: 1,
  appliesTo: (evt, process) => process.hostsCharacter(evt.characterId),   // TA-R-083: tự lọc
  async handle(evt, { api, rooms }) {
    const inputs = await api.characters.getStatInputs(evt.characterId);   // I/O ngoài tick
    rooms.forCharacter(evt.characterId)?.enqueueExternal({ kind: 'STAT_INPUTS', characterId: evt.characterId, inputs });
  },
});
```

---

## 6. Vòng đời room

| Hook | Việc | Lỗi |
|------|------|-----|
| `onCreate(options)` | Nạp `MapDef` từ `bundle.server` (TD-13), dựng `CollisionWorld` + chỉ mục, navmesh theo cỡ thân (TD-14 §8), spawn ban đầu, seed `RoomRng` (lưu log), đăng ký heartbeat World Directory (TD-12 WM-R-010), subscribe `world:tick` | Map không tồn tại / `gamedataVersion` lệch → không tạo, báo API |
| `onAuth(client, { ticket })` | `auth.verifyTicket`: chữ ký, `roomKey`, `exp`, `SET NX ticket:{jti}` (TD-01 §14) | `UNAUTHORIZED` |
| `onJoin(client, auth)` | `GET /internal/v1/characters/{id}/session-load` → tạo entity + component; đặt ở `arrival` (TD-12 WM-R-042); bất tử spawn; lấy `charlock`; gửi `S2C_Welcome` + State trong view | session-load lỗi → `S2C_Kick` + trả vé lại để thử |
| `onLeave(client, consented)` | Nếu không chủ động: `allowReconnection(client, 60)` (TD-02 NW-R-072), nhân vật xử lý theo nơi đứng (đứng yên / quy tức hiểm địa); hết hạn → snapshot cuối + nhả `charlock` + gỡ entity | — |
| Chuyển bản đồ | `transfer` (TD-12 §5): khóa nhân vật `TRANSFERRING`, snapshot cuối, gửi `S2C_TransferTicket`, chờ client rời | Timeout → `ABORTED`, mở khóa |
| `onDispose()` | Gửi batch còn tồn (§7.2), kết quả instance cuối (WM-R-061), hủy subscribe, xóa `wd:*` | Lỗi gửi → đẩy vào hàng đợi thử lại bền |
| Drain (TD-12 WM-R-090) | Không nhận người mới; chuyển người chơi sang room cùng khóa ở tiến trình khác | — |

- <a id="gs-r-050"></a>**GS-R-050** `session-load` trả **toàn bộ** dữ liệu mô phỏng cần cho nhân vật (chỉ số đầu vào, loadout kỹ năng, `CSK_*` skillDef, thú kết khế + chỉ số tính sẵn, status bền, `pkState`, cờ, bí mật đã rút, fog bản đồ). Room không gọi thêm API trong lúc nhân vật đang chơi trừ khi nhận sự kiện (§8).

---

## 7. Gọi API Service

### 7.1. ApiClient
- <a id="gs-r-060"></a>**GS-R-060** `ApiClient` sinh từ `@tienlo/contracts` (OpenAPI `internal.yaml`, TD-01 TA-R-020), gắn service token, `X-Request-Id`, `traceparent`; mọi thao tác ghi nhận `idempotencyKey` dạng `{loại}:{uuidv7}` (TA-R-050) **sinh khi xếp**, không sinh lại khi thử lại.
- <a id="gs-r-061"></a>**GS-R-061** **Hàng đợi thử lại bền** `gs:retry:{processId}` (Valkey list): thao tác quan trọng (`loot/grant`, `cultivation/*`, `lifecycle/*`, `danger/*`, `pvp/kill`, `beasts/*`, snapshot cuối) gặp timeout / 503 → backoff 0,5s → 30s, tối đa 24h; tiến trình khởi động lại đọc tiếp hàng đợi. Lỗi 4xx nghiệp vụ không thử lại, log + metric.
- <a id="gs-r-062"></a>**GS-R-062** Kết quả API trả về được áp vào room qua `enqueueExternal` ở bước 0 của tick sau (GS-R-003).

### 7.2. Gom lô

| Lô | Endpoint | Chu kỳ | Khóa |
|----|----------|--------|------|
| Tu vi đánh quái | `cultivation/monster-kills` (GD-04) | 30s / nhân vật, hoặc khi rời room | `batchId` |
| Thuần thục | `techniques/mastery` (GD-07) | 30s | `batchId` |
| Tuổi | `lifecycle/age-accrual` (GD-26) | 60s | `reportId` |
| Fog | `world/fog` (TD-12 WM-R-081) | 60s | `batchId` |
| Codex | `codex/progress` (GD-12) | 60s | `batchId` |
| Thân mật thú | `beasts/bond` (GD-25) | 60s | `batchId` |
| Snapshot | `characters/{id}/snapshot` (TA-R-110) | 30s + sự kiện | `seq` |
| Pity cơ duyên | `encounters/pity` (GD-22) | 60s / rời bản đồ | — |

- <a id="gs-r-063"></a>**GS-R-063** Batcher giữ dữ liệu trong RAM; mất khi tiến trình sập là chấp nhận được (TA-R-111). Lô **bắt buộc gửi** trước khi chuyển bản đồ / đăng xuất / drain.

### 7.3. Khi API mất (TD-01 §13)
- <a id="gs-r-064"></a>**GS-R-064** `ApiHealth` theo dõi tỷ lệ lỗi; API lỗi > 10s → room vào chế độ **suy giảm**: tiếp tục mô phỏng; chặn hành động cần kết quả ngay (mở rương, chuyển bản đồ, vào hiểm địa, ném phù bắt thú, chế tác) bằng `S2C_Notice` *"Thiên địa hỗn loạn"*; **tạm dừng đồng hồ hấp hối** và không phát sinh chết thật mới (GD-28 DZ-R-080 bổ sung); lô và thao tác quan trọng nằm trong hàng đợi thử lại.

---

## 8. Sự kiện domain (TD-01 §10)

- <a id="gs-r-070"></a>**GS-R-070** Mỗi tiến trình là consumer group `gs.{processId}` trên các stream cần thiết, đọc **tất cả** rồi lọc theo nhân vật / bản đồ đang chứa (TA-R-083); idempotent theo `eventId` bằng LRU 10.000 mục trong RAM (sự kiện cũ hơn 10 phút bỏ qua).

| Sự kiện | Phản ứng (file `events/handlers/`) |
|---------|------------------------------------|
| `stats.inputs_changed`, `inventory.equipment_changed`, `talent.changed`, `technique.learned`, `technique.main_changed`, `cultivation.stage_up` | Lấy `stat-inputs`, `sim.computeStats`, cập nhật `Combatant` |
| `beast.changed` | Lấy `beast-loadout`, gọi lại / thu thú |
| `lifecycle.last_light_started` | Thêm `ST_LAST_LIGHT` |
| `lifecycle.life_ending` | Gỡ entity, `S2C_LifeEnded`, nhả `charlock` |
| `cultivation.breakthrough_succeeded` | Hiệu ứng + cảnh giới hiển thị |
| `broadcast.requested` | `S2C_Notice` / kênh `SYSTEM` cho người trong room (lọc `hideNameInBroadcast` đã xử lý ở API) |
| `site.state_changed` | Cập nhật mặt tiền sơn môn (GD-30) |
| `pvp.tier_changed` | Cập nhật cờ tên, kích hoạt lịch Chấp Pháp (GD-32) |

- <a id="gs-r-071"></a>**GS-R-071** Game Server **phát** trực tiếp (không outbox, TA-R-082): `monster.killed`, `monster.elite_spawned`, `boss.defeated`, telemetry. Mọi kết quả có giá trị đi qua API đồng bộ.

---

## 9. AI quái (GD-12)

- <a id="gs-r-080"></a>**GS-R-080** AI chia hai tầng: **quyết định** (`sim.aiDecide(monster, perception, archetypeParams) → AiIntent`: chọn trạng thái `IDLE/ALERT/COMBAT/RETURNING/FLEEING`, mục tiêu, chiêu) và **thực thi** (lệnh hành động qua cùng `actionState` / `skills` như người chơi, cùng `SkillDef`). Khung quyết định là **FSM theo archetype** (GD-12 §5) + bảng trọng số chiêu; boss thêm lớp **pha** (ngưỡng HP → kịch bản). Không dùng thư viện behavior tree ở MVP.
- <a id="gs-r-081"></a>**GS-R-081** **Nhóm tần suất:** quái đang `COMBAT` quyết định mỗi 2 tick (10Hz); `ALERT` / `RETURNING` mỗi 4 tick; `IDLE` mỗi 10 tick hoặc khi có người vào `sightRadius` (do `aoi` báo); quái ngoài mọi view **ngủ** (không quyết định, không di chuyển) sau 5s. Thực thi (di chuyển, lượt trúng) vẫn mỗi tick.
- <a id="gs-r-082"></a>**GS-R-082** Nhận thức: LOS qua TD-14 GEO-R-060, lọc ứng viên bằng lưới AOI; tìm đường bằng navmesh theo cỡ thân, đường đi được cache 1s / mục tiêu.
- <a id="gs-r-083"></a>**GS-R-083** Ngân sách AI ≤ 4ms p99 / tick cho 200 quái; vượt → tăng `everyTicks` của nhóm `ALERT`/`IDLE` (tự điều chỉnh) và ghi metric.
- <a id="gs-r-084"></a>**GS-R-084** NPC Chấp Pháp, Thiên Đạo Hóa Thân (GD-32), thủ vệ linh thảo (GD-21), tàn hồn (GD-26) là **quái** dùng cùng khung, khác archetype / bộ lọc mục tiêu (chỉ nhắm ác nhân, người mang Thiên phạt, người tranh hái, chủ thử thách).

---

## 10. Cấu hình & feature flag

- <a id="gs-r-090"></a>**GS-R-090** Biến môi trường parse bằng zod lúc khởi động (thiếu / sai → dừng): `PROCESS_ID`, `API_BASE_URL`, `SERVICE_TOKEN_*`, `REDIS_URL`, `GAMEDATA_BUNDLE_PATH`, `PUBLIC_ENDPOINT`, `OTEL_*`, `LOG_LEVEL`.
- <a id="gs-r-091"></a>**GS-R-091** Feature flag đến từ `GET /internal/v1/meta` (cùng `gamedataVersion`, TA-R-150), làm mới 60s: `pvpEnabled` (GD-32), `beastsEnabled` (GD-25), `maVucEnabled` (GD-28), `worldEventsEnabled` (GD-23), `debugHitShapes` (chỉ dev, TD-14 GEO-R-092). Tắt flag giữa chừng → tính năng ngừng nhận lệnh mới, thực thể đang tồn tại xử lý nốt (vd. thú thu về).
- <a id="gs-r-092"></a>**GS-R-092** Tham số gameplay **không** nằm trong env: đọc từ gamedata (`world/network.json`, `world/geometry.json`, `pvp/params.json`…, TD-07).

---

## 11. Quan sát

| Loại | Bắt buộc |
|------|----------|
| Metric (prom-client) | `gs_tick_duration_ms` (histogram, nhãn `room_type`), `gs_system_duration_ms{system}`, `gs_room_players`, `gs_room_entities`, `gs_rooms_total{type}`, `gs_commands_rejected_total{reason}`, `gs_rate_limited_total{group}`, `gs_api_latency_ms{endpoint}`, `gs_api_errors_total{endpoint,code}`, `gs_retry_queue_length`, `gs_events_consumed_total{type}`, `gs_ws_bytes_out_total`, `gs_ai_decisions_total{tier}`, `gs_transfer_ms{step}` |
| Log (pino JSON, TA-R-130) | Mỗi dòng có `processId`, `roomKey`, `tick`; sự kiện quan trọng: tạo / đóng room, vào / rời, chuyển bản đồ, chết / hấp hối, lỗi API, vi phạm schema / rate limit |
| Trace (OpenTelemetry) | Span cho `onJoin`, `transfer`, mọi gọi API (truyền `traceparent`), xử lý sự kiện domain; **không** trace từng tick (chỉ metric) |
| Cảnh báo | Theo TA-R-133 (tick p99 > 40ms trong 1 phút, hàng đợi thử lại > 1.000) |

---

## 12. Kiểm thử

| Lớp | Cách làm |
|-----|----------|
| Unit system | `testing/FakeRoom` dựng `World` + `TickContext` giả (đồng hồ, `RoomRng` seed cố định, `IoQueue` ghi lại); chạy `system.run` qua N tick, so trạng thái. Mỗi quy tắc `-R-` của GD hiện thực ở Game Server có test tên chứa mã (vd. `SK_R_074_hit_once_per_tick`) |
| Handler | Gửi payload hợp lệ / sai kiểu / thừa trường / vượt rate → kiểm lệnh và vi phạm |
| Tích hợp room | Room Colyseus thật trong tiến trình test + `ApiClient` giả có kịch bản lỗi (timeout, 503, 409) |
| Bot headless | `test/bots/`: client Colyseus dùng `@tienlo/sim` dự đoán như client thật; kịch bản: đi dạo, đánh quái, chuyển bản đồ, hiểm địa, PvP |
| Tải | 150 bot / room × nhiều room / tiến trình, đo NW-AC-009, GEO-AC-015, GS-AC-001 |
| Tất định | Phát lại log lệnh của một room với cùng seed → cùng kết quả (dùng để tái hiện lỗi) |

---

## 13. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="gs-ac-001"></a>GS-AC-001 | Room 150 bot + 200 quái, mọi bot đánh liên tục | Đo 10 phút | Tick p99 < 25ms; từng system trong ngân sách §4 |
| GS-AC-002 | Handler nhận `C2S_CastSkill` thừa trường `damage` | Xử lý | Bỏ message, tăng vi phạm, không có lệnh |
| GS-AC-003 | API trả 503 cho `loot/grant` 2 phút | Sau khi API về | Vật phẩm được ghi đúng một lần (khóa idempotency giữ nguyên qua các lần thử) |
| GS-AC-004 | Tiến trình khởi động lại khi còn 50 mục trong `gs:retry:{processId}` | Khởi động | Tiếp tục gửi, không mất mục |
| GS-AC-005 | Nhận `stats.inputs_changed` cho nhân vật ở tiến trình khác | Consumer | Bỏ qua (không gọi API) |
| GS-AC-006 | Nhận cùng `eventId` hai lần | Consumer | Phản ứng một lần |
| GS-AC-007 | API mất 15s khi có người đang hấp hối ở hiểm địa | Hết thời gian hấp hối | Không chết; đồng hồ hấp hối tạm dừng tới khi API về |
| GS-AC-008 | 200 quái `IDLE` ngoài mọi view | Đo | Không có quyết định AI nào cho chúng (ngủ) |
| GS-AC-009 | Phát lại log lệnh 5 phút của một room với seed đã lưu | So sánh | Trạng thái cuối giống hệt |
| GS-AC-010 | `CaveRoom` | Gửi `C2S_CastSkill` | Từ chối (system combat tắt) |
| GS-AC-011 | Room bị dispose khi lô tu vi còn dữ liệu | Dispose | Lô được gửi hoặc nằm trong hàng đợi thử lại |
| GS-AC-012 | Flag `pvpEnabled` tắt khi đang có hai người `SLAUGHTER` | Sau làm mới flag | Không gây sát thương PvP mới |

---

## 14. Câu hỏi mở
- [ ] **GS-Q-1:** Có cần chạy AI quái trên **worker thread** riêng (song song tick) khi vượt ngân sách, hay đủ với nhóm tần suất? Đo ở load test Alpha.
- [ ] **GS-Q-2:** Ghi log lệnh để phát lại (GS-AC-009) cho **mọi** room ở production (tốn I/O) hay chỉ khi bật cờ điều tra?
- [ ] **GS-Q-3:** Instance nhẹ (động phủ, Diễn Pháp Tràng) có nên gom nhiều instance vào **một room Colyseus đa phòng** để giảm overhead khi số lượng lớn?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến, cấu trúc `game-server/src`, component store tự viết + phần dùng chung `@tienlo/sim`, thứ tự 10 bước tick có ngân sách, khuôn mẫu system / handler / phản ứng sự kiện, vòng đời room, ApiClient + hàng đợi thử lại + gom lô + chế độ suy giảm, consumer sự kiện, AI 2 tầng + nhóm tần suất, cấu hình / flag, metric / log / trace, kiểm thử, nghiệm thu |
| v0.2 | 2026-10-25 | — | D-24: Valkey thay Redis (tên khóa / lệnh không đổi) |
| v0.3 | 2026-10-25 | — | System `flight` trước movement (GD-31) |

