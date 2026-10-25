# GD-23 — Sự kiện thế giới

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-23 · Prefix ID: `WE` |
| Owner | Game Designer + Content Designer + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.4 |
| Cần trước | Phase 4 / Alpha (khung lập lịch + 2 sự kiện bản đầu: Linh khí bùng phát, Dị bảo xuất thế), Beta 2 (Thú triều, Ma tu xâm lấn, world boss, Thượng Cổ Tiên Phủ theo lịch) |
| Quyết định nền | PLAN §4.8, D-03 (chỉ chết thật ở hiểm địa / thiên kiếp / hết thọ), D-21 (PvP dã ngoại có kiểm soát), D-24 (Valkey) |
| Liên quan | [PLAN §4.8](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-21 Open World](22-open-world.md) (World Clock, thời tiết, Linh vũ) · [GD-12 Quái](18-enemies-ai.md) (tính công EN-R-100, boss) · [GD-13 Kinh tế](19-economy.md) · [GD-14 Xã hội](20-social.md) (thông báo SO-R-060) · [GD-22 Cơ duyên](23-encounters.md) · [GD-25 Linh thú](26-spirit-beasts.md) (SB-Q-6) · [GD-28 Hiểm địa](27-danger-zones.md) (DZ-R-110 Thượng Cổ Tiên Phủ) · [GD-30 Động phủ & Sơn môn](28-caves-sect-sites.md) (Sơn Môn Đại Hội, Phế Tích) · [GD-32 PvP](31-pvp-infamy.md) · [GD-29 RNG](05-rng-fairness.md) · [TD-12 World & Map](../tech/11-world-maps.md) §7 · [TD-03 Database](../tech/02-database.md) · [TD-09 Vận hành](../tech/08-infra-ops.md) §6 (cờ) |

> Sự kiện thế giới là **lớp nội dung theo thời gian** chồng lên bản đồ có sẵn: dùng lại quái (GD-12), bảng rơi (GD-12 §6.5), World Clock (TD-12 §7), thông báo (GD-14), không cần bản đồ mới. Mọi sự kiện mô tả bằng **dữ liệu** (`EventDef`) và chạy bởi **một bộ lập lịch tất định** trên World Service leader.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-23 | Mục |
|--------------|-----------------|-----|
| GD-12 | Cách sự kiện sinh quái / boss bằng `MonsterDef` / `SpawnGroupDef`; world boss | §5.3, §5.6 |
| GD-13 | Phần thưởng sự kiện (linh thạch, vật phẩm) và trần | §5.5, §6.2 |
| GD-14 | Ưu tiên thông báo sự kiện (SO-R-060) | §5.2 |
| GD-21 | Linh vũ là sự kiện thời tiết; sự kiện dùng bản đồ / kênh làm phạm vi | §5.1, §7 |
| GD-25 | Trả lời SB-Q-6 (world boss có bắt được không) | §5.6 |
| GD-28 | Lịch mở Thượng Cổ Tiên Phủ / Ma Uyên | §5.7 |
| GD-30 | Sơn Môn Đại Hội, sự kiện Phế Tích chạy trên cùng bộ lập lịch | §5.7 |
| GD-32 | PvP trong vùng sự kiện | §5.8 |
| TD-03 | DDL `world_event_log` + bảng phụ | §10 |
| TD-12 | Lịch sự kiện trên World Service leader | §5.1 |

---

## 1. Mục tiêu

**Trụ cột:** P2 *Khám phá & bất ngờ* (thế giới "sống", có chuyện xảy ra), P1 *Mỗi kiếp một câu chuyện* ("ta đã ở đó khi Dị bảo xuất thế"), xã hội (lý do tụ tập, hợp tác tự nhiên).

- Thế giới thay đổi theo thời gian: cột sáng, thú triều, ma khí… kéo người chơi tới cùng một chỗ.
- **Hợp tác tự nhiên không cần tổ đội**: ai đóng góp đủ đều được thưởng trọn phần của mình.
- **Không bắt buộc, không FOMO độc hại:** bỏ lỡ một sự kiện không làm tụt hậu tiến trình (phần thưởng là "gia vị", không phải đường duy nhất).
- Nội dung rẻ cho team nhỏ: lắp ghép từ quái, bảng rơi, bản đồ, hiệu ứng có sẵn.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="we-r-001"></a>**WE-R-001** | Sự kiện ở **vùng thường** (không phải hiểm địa) **không bao giờ gây chết thật**: hết máu là trọng thương (GD-26 LC-R-001). Sự kiện trong hiểm địa / Ma Vực theo luật hiểm địa (GD-28). |
| <a id="we-r-002"></a>**WE-R-002** | Lịch sự kiện **tất định**: tính từ `worldEpoch` + `worldSeed` + `EventDef` (giống thời tiết TD-12 WM-R-072). Mọi bản API tính ra cùng lịch; đổi leader không đổi lịch; sự kiện ngẫu nhiên vẫn tái lập được. |
| <a id="we-r-003"></a>**WE-R-003** | Phần thưởng theo **đóng góp cá nhân**: mỗi người đạt ngưỡng nhận **trọn phần của mình**, không chia nhỏ theo số người (tinh thần GD-12 EN-R-100). Có trần mỗi người / mỗi sự kiện / mỗi ngày. |
| <a id="we-r-004"></a>**WE-R-004** | Sự kiện chỉ dùng **outcome / hành động trong danh sách trắng** (§3 `EventAction`); không sự kiện nào cần code riêng. |
| <a id="we-r-005"></a>**WE-R-005** | Không sự kiện nào bán bằng tiền thật hay thưởng thứ giảm rủi ro chết / tăng thọ ngoài khung đã có (D-17, GD-19). |

---

## 2. Tổng quan

```
 EventDef (gamedata)  ──►  World Service leader: SCHEDULER (tất định từ epoch + seed)
                                │   ANNOUNCED (báo trước)  →  ACTIVE  →  RESOLVING  →  ENDED
                                │ outbox: world_event.announced / started / ended
            ┌───────────────────┴───────────────────┐
            ▼                                       ▼
   Game Server (room các bản đồ phạm vi)      Client: bản đồ thế giới hiện biểu tượng + đếm ngược,
   - áp hiệu ứng (buff vùng, spawn quái/boss)  thông báo SO-R-060, HUD tiến độ sự kiện
   - đếm ĐÓNG GÓP từng người (§6.1)
   - kết thúc → POST /internal/v1/world-events/{id}/results
            ▼
   API Service: phát thưởng theo đóng góp (inventory gate, rng_audit), ghi world_event_log
```

**Phạm vi** (`scope`): `MAP` (một bản đồ, mọi kênh hoặc một kênh) · `REGION` (cả vực) · `SERVER` (toàn server).
**Kích hoạt** (`trigger`): `SCHEDULED` (giờ cố định) · `RANDOM` (cửa sổ + xác suất, tất định từ seed) · `WEATHER` (gắn thời tiết, vd. Linh vũ) · `PLAYER` (người chơi kích hoạt, vd. đốt Hương Triệu Thú) · `SYSTEM` (hệ thống khác gọi: Phế Tích GD-30, hiểm địa theo lịch GD-28).

---

## 3. Thực thể & kiểu dữ liệu

```ts
type EventScope = 'MAP' | 'REGION' | 'SERVER';
type EventTrigger = 'SCHEDULED' | 'RANDOM' | 'WEATHER' | 'PLAYER' | 'SYSTEM';
type EventState = 'ANNOUNCED' | 'ACTIVE' | 'RESOLVING' | 'ENDED' | 'CANCELLED';
type EventKind = 'BUFF_ZONE' | 'TREASURE' | 'BEAST_TIDE' | 'INVASION' | 'WORLD_BOSS' | 'SCHEDULED_INSTANCE' | 'SECT_CONCLAVE' | 'RUINS';

interface EventDef {                       // shared/gamedata/world_events/events.json
  id: string;                              // WE_*
  nameKey: string; descKey: string;
  kind: EventKind;
  scope: EventScope;
  targets: { regionId?: string; mapIds?: string[]; areaIds?: string[]; channelMode: 'ALL' | 'ONE_RANDOM' | 'BUSIEST' };
  trigger: EventTrigger;
  schedule?: { cron: string; tz: 'SERVER' };                    // SCHEDULED: "0 20 * * SAT"
  random?: { windowHours: [number, number]; chancePerWindow: number; minGapHours: number };  // RANDOM
  weather?: Weather;                                            // WEATHER: giá trị enum `Weather` (glossary), vd. "SPIRIT_RAIN"
  announceLeadMin: number;                                      // báo trước
  durationMin: number;
  realmRange?: [string, string];                                // gợi ý / điều kiện tham gia
  scaling?: { basis: 'PLAYERS_IN_SCOPE'; min: number; max: number; hpPerPlayerPct: number; spawnPerPlayer: number };
  phases?: EventPhaseDef[];                                     // tùy kind (thú triều nhiều đợt, boss nhiều pha)
  contribution: ContributionRule;                               // §6.1
  rewards: RewardTier[];                                        // §5.5
  actions: EventAction[];                                       // hiệu ứng khi bắt đầu / theo pha / kết thúc
  pvp: 'INHERIT' | 'DISABLED_IN_ZONE';                          // §5.8
  broadcastPriority: 2 | 3 | 4;                                 // GD-14 SO-R-060
  phase?: 'ALPHA' | 'BETA' | 'LAUNCH';                          // TD-07 DT-R-058
}

interface EventPhaseDef { id: string; startAtPct?: number; untilCondition?: 'WAVE_CLEARED' | 'BOSS_HP_BELOW' | 'TIMER'; value?: number; actions: EventAction[] }

type EventAction =                                              // danh sách trắng (WE-R-004)
  | { type: 'ZONE_BUFF'; area: 'MAP' | string; statusId: string }            // ST_* (GD-06), vd. tu luyện ×2
  | { type: 'SPAWN_GROUP'; spawnGroupId: string; at: 'AREA' | 'POI' | 'EDGES'; ref?: string; count?: number }
  | { type: 'SPAWN_BOSS'; monsterId: string; at: string }
  | { type: 'SPAWN_POI'; poiTemplate: string; at: 'RANDOM_IN_AREA' | string; guardSpawnGroupId?: string }   // dị bảo (TD-13 PoiKind)
  | { type: 'DISABLE_TELEPORT'; mapId: string }                               // ma tu chiếm truyền tống trận
  | { type: 'REPLACE_SPAWNS'; mapId: string; spawnTableId: string }           // đổi quái bản đồ
  | { type: 'OPEN_INSTANCE'; dangerZoneId: string }                           // GD-28 instance theo lịch
  | { type: 'BROADCAST'; messageKey: string; priority: 2 | 3 | 4 };

interface ContributionRule {
  sources: Partial<Record<'DAMAGE' | 'HEAL' | 'KILL' | 'OBJECTIVE' | 'DEFEND' | 'GATHER', number>>;   // trọng số
  minPointsToQualify: number;              // ngưỡng nhận thưởng
  presenceMinSec: number;                  // phải có mặt trong phạm vi tối thiểu (chống ăn bám)
  perPlayerCap: number;                    // trần điểm mỗi người (chống một người "gánh" tạo chênh lệch quá lớn)
}
interface RewardTier { minRankPct?: number; minPoints?: number; lootTableId: string; spiritStones?: [number, number]; exp?: { pctOfStage: number }; titleId?: string }

interface WorldEventInstance {             // bản ghi chạy
  id: string;                              // WEI_{eventId}_{epochSlot}
  eventId: string; state: EventState;
  scopeResolved: { mapIds: string[]; channels: number[] | 'ALL' };
  announcedAt: string; startsAt: string; endsAt: string; endedAt: string | null;
  outcome: 'SUCCESS' | 'FAILED' | 'TIMEOUT' | 'CANCELLED' | null;
  seed: string;                            // từ worldSeed + eventId + slot (WE-R-002)
}
```

---

## 4. Máy trạng thái

```
(lịch tính trước) ──announceLeadMin──► ANNOUNCED ──startsAt──► ACTIVE ──hết giờ / mục tiêu xong / thất bại──► RESOLVING ──thưởng xong──► ENDED
        └──────── cờ ff.worldEvents.enabled = false / sự cố ────────────────────────────────────────────────► CANCELLED
```
- `ANNOUNCED`: hiện trên bản đồ thế giới + thông báo; chưa có hiệu ứng.
- `ACTIVE`: Game Server áp `actions`, đếm đóng góp.
- `RESOLVING`: Game Server gửi bảng đóng góp cuối; API phát thưởng (idempotent theo `eventInstanceId`).
- `CANCELLED`: không phát thưởng; nếu đã `ACTIVE` > 50% thời lượng thì phát thưởng an ủi bậc thấp nhất cho người đủ ngưỡng (WE-R-062).

---

## 5. Quy tắc

### 5.1. Lập lịch
- <a id="we-r-010"></a>**WE-R-010** **Bộ lập lịch** chạy trên **World Service leader** (TD-12 WM-R-070). Lịch 48 giờ tới được tính trước từ `EventDef` và cache trong Valkey (`world:events:upcoming`); mọi bản API tính ra cùng kết quả (WE-R-002).
- <a id="we-r-011"></a>**WE-R-011** `RANDOM`: chia thời gian thành các cửa sổ `windowHours`; cửa sổ thứ `k` có sự kiện nếu `xoshiro(seed = hash(worldSeed, eventId, k)).nextFloat() < chancePerWindow` và cách lần trước ≥ `minGapHours`; giờ bắt đầu rút trong cửa sổ bằng cùng luồng. Kênh (`ONE_RANDOM`) rút lúc `ANNOUNCED` theo cùng seed.
- <a id="we-r-012"></a>**WE-R-012** `WEATHER`: sự kiện gắn thời tiết (Linh vũ, GD-21) bắt đầu khi ô thời tiết tương ứng bắt đầu (TD-12 WM-R-072) — thời tiết là nguồn, sự kiện chỉ thêm hiệu ứng / thông báo.
- <a id="we-r-013"></a>**WE-R-013** **Không chồng**: cùng một bản đồ không có hai sự kiện `ACTIVE` cùng `kind` thuộc nhóm chiến đấu (`BEAST_TIDE`, `INVASION`, `WORLD_BOSS`); trùng → sự kiện có ưu tiên thấp hơn dời sang cửa sổ kế tiếp (tất định).
- <a id="we-r-014"></a>**WE-R-014** `PLAYER`: người chơi dùng vật phẩm kích hoạt (vd. `ITM_BEAST_LURE_INCENSE`) tại khu cho phép → sự kiện `MAP` một kênh, báo trước 2 phút; mỗi khu hồi chiêu 2 giờ; không trong 30 phút trước sự kiện `SCHEDULED` cùng bản đồ.
- <a id="we-r-015"></a>**WE-R-015** **Bản đồ tân thủ không có sự kiện chiến đấu:** `V1_F01` (bản đồ hướng dẫn, không PvP — D-13, D-21, GD-18) **không** được chọn làm bản đồ của sự kiện có `kind` `TREASURE` (thủ hộ tinh anh), `BEAST_TIDE`, `INVASION`, `WORLD_BOSS` hoặc có hành động `SPAWN_GROUP`, `SPAWN_BOSS`, `REPLACE_SPAWNS`, `DISABLE_TELEPORT`: không ghi trong `targets.mapIds`, và bộ chọn bản đồ ngẫu nhiên (`scope` `MAP` / `REGION`) loại nó khỏi tập ứng viên. Sự kiện không chiến đấu (`WE_V1_QI_SURGE`, `WE_V1_LINH_VU`) vẫn áp dụng ở `V1_F01`.

### 5.2. Báo trước & thông báo
- <a id="we-r-020"></a>**WE-R-020** `ANNOUNCED` hiện biểu tượng + đếm ngược trên bản đồ thế giới và minimap của mọi người trong vực; thông báo qua hàng đợi chung GD-14 SO-R-060 với `broadcastPriority` (mặc định 4 — chỉ trong bản đồ / vực; world boss & Ma tu xâm lấn: 3 toàn server; sự kiện đặc biệt hiếm: 2).
- <a id="we-r-021"></a>**WE-R-021** Người chơi đăng ký "nhắc tôi" cho sự kiện theo lịch (thông báo trong game / thông báo đẩy trên app — TD-01 TA-R-162).

### 5.3. Quy mô theo số người
- <a id="we-r-030"></a>**WE-R-030** Khi `ACTIVE`, Game Server đếm `N` = số người chơi đủ điều kiện (`realmRange` nếu có, còn sống, không `DORMANT_GUARD`) trong phạm vi; quái / boss sự kiện co giãn: `HP = baseHp × (1 + hpPerPlayerPct × max(0, N − 1))` (trần `scaling.max` người), số quái mỗi đợt `= base + spawnPerPlayer × N` (trần theo ngân sách thực thể TD-11). Tính lại mỗi 30s, **không giảm** HP đã mất.
- <a id="we-r-031"></a>**WE-R-031** Sự kiện `ALL` kênh: mỗi kênh có thực thể riêng, co giãn riêng (TD-12 WM-R-033 — tài nguyên theo kênh).

### 5.4. Đóng góp
- <a id="we-r-040"></a>**WE-R-040** Game Server tính **điểm đóng góp** từng người theo `ContributionRule` (§6.1); chỉ hành động **trong phạm vi sự kiện** và **lên thực thể / mục tiêu của sự kiện** mới tính.
- <a id="we-r-041"></a>**WE-R-041** **Chống ăn bám:** phải có mặt trong phạm vi ≥ `presenceMinSec`; điểm từ `HEAL` chỉ tính khi hồi cho người đang có điểm; `DEFEND` (đứng trong vùng phòng thủ) có trần thấp; AFK (không input 120s) không cộng điểm thời gian.
- <a id="we-r-042"></a>**WE-R-042** Bảng xếp hạng đóng góp hiển thị trực tiếp (top 10 + vị trí của mình) trong HUD sự kiện.

### 5.5. Phần thưởng
- <a id="we-r-050"></a>**WE-R-050** Kết thúc: mọi người đạt `minPointsToQualify` nhận thưởng theo **bậc** (`RewardTier`: theo điểm tuyệt đối hoặc theo hạng phần trăm `minRankPct`), mỗi người **roll bảng rơi riêng** (loại B, GD-29) qua inventory gate (`EVENT_REWARD`), linh thạch / tu vi theo bậc. Bậc cao không lấy bớt của bậc thấp (WE-R-003).
- <a id="we-r-051"></a>**WE-R-051** **Trần:** mỗi tài khoản nhận thưởng đầy đủ tối đa `dailyFullRewards` (3) sự kiện mỗi ngày; vượt thì chỉ nhận bậc thấp nhất (vẫn vui để tham gia, không cày được). World boss: thưởng chính **1 lần / ngày / tài khoản** cho mỗi boss.
- <a id="we-r-052"></a>**WE-R-052** Giá trị thưởng theo **IPH** của dải cảnh giới bản đồ (GD-13 EC-R-022): một sự kiện 20–30 phút thưởng ≈ 1,0–1,5 × IPH cho bậc trung bình — tương đương chơi bình thường cộng chút hấp dẫn, không phá kinh tế.
- <a id="we-r-053"></a>**WE-R-053** Vật phẩm đặc thù sự kiện (vd. **Dị Bảo**, **Thú Hồn Tinh** từ thú triều) theo bảng rơi `serverOnly` (GD-29 RF-R-041).

### 5.6. World boss
- <a id="we-r-060"></a>**WE-R-060** World boss là `MonsterDef` hạng `BOSS` (GD-12) xuất hiện ở bản đồ mở theo lịch (`WORLD_BOSS`), **mọi kênh của bản đồ** (`channelMode: ALL`) để không dồn một kênh; HP co giãn WE-R-030; 3–4 pha, chiêu theo chuẩn cảnh báo boss (GD-12 EN-R-070); dây xích lớn (60m); hết `durationMin` chưa hạ → boss rút lui (`TIMEOUT`, thưởng an ủi cho người đủ ngưỡng).
- <a id="we-r-061"></a>**WE-R-061** Hạ boss: tính công theo đóng góp (§6.1, trọng số `DAMAGE` + `HEAL` + `OBJECTIVE` cơ chế), không theo "đòn cuối".
- <a id="we-r-062"></a>**WE-R-062** **Trả lời GD-25 SB-Q-6 — world boss có thu phục được không:** **có, cho một số world boss được chọn**, theo khung "boss đặc biệt" GD-25 §5.2b nhưng **thay cơ chế ném phù tự do** bằng **quyền ném phù cho top đóng góp**: khi boss vào Kiệt Sức (≤ 10% HP), **top 3 đóng góp** của **mỗi kênh** (đã có Huyền Ngự Thú Phù trở lên, còn lượt ngày) được ném; tỷ lệ theo GD-25 §6.2b; người thu phục đổi thưởng chính của mình lấy thú (SB-R-029). Lý do: tránh 100 người cùng ném (tỷ lệ cộng dồn quá cao), thưởng cho người đóng góp thật, không bán quyền (không "đấu giá" bằng tiền). Vực 1 MVP: **chưa có** world boss thu phục được (Beta 2).

### 5.7. Sự kiện chạy trên cùng bộ lập lịch
- <a id="we-r-070"></a>**WE-R-070** **Thượng Cổ Tiên Phủ** (GD-28 DZ-R-110, instance chung theo lịch): `SCHEDULED_INSTANCE` với `OPEN_INSTANCE` — 2 giờ mỗi tuần; **Ma Uyên** (GD-28, nhiều tầng) nếu có lịch mở thì dùng cùng kiểu. Luật bên trong theo GD-28 (chết thật), không theo WE-R-001.
- <a id="we-r-071"></a>**WE-R-071** **Sơn Môn Đại Hội** (GD-30 HM-R-073, ngày 1 hằng tháng 20:00) và **sự kiện Phế Tích** (GD-30 `site.state_changed → RUINS`, 7 ngày) là `SECT_CONCLAVE` / `RUINS` (`trigger: SYSTEM`) — GD-30 giữ luật, GD-23 chỉ cung cấp lịch, thông báo, HUD.

### 5.8. PvP trong vùng sự kiện
- <a id="we-r-080"></a>**WE-R-080** Mặc định `pvp: INHERIT` — theo luật bản đồ (GD-32). Sự kiện **hợp tác** (Thú triều phòng thủ thôn, Ma tu xâm lấn) đặt `DISABLED_IN_ZONE`: trong vùng sự kiện không PvP (vùng `NO_PVP` tạm thời, TD-13 `ZoneDef`). Sự kiện **tranh đoạt** (Dị bảo xuất thế) để `INHERIT` — đúng tinh thần tranh đoạt OW-5, chịu luật sát nghiệp.
- <a id="we-r-081"></a>**WE-R-081** Điểm đóng góp **không** tính từ việc giết người chơi; đánh người chơi khác trong sự kiện không cộng điểm.

---

## 6. Công thức

### 6.1. Điểm đóng góp
```
points(p) = min( perPlayerCap,
    w_DAMAGE   × damageToEventEntities(p) / eventEntitiesTotalHp × 1000
  + w_HEAL     × effectiveHealOnContributors(p) / eventEntitiesTotalHp × 1000
  + w_KILL     × killsOfEventEntities(p)
  + w_OBJECTIVE× objectivesCompleted(p)              // phá trận nhãn, mang dị bảo về, đốt đèn phòng thủ...
  + w_DEFEND   × min(secondsInDefendZone(p), defendCapSec) / 60
  + w_GATHER   × eventGathers(p) )
qualify(p) ⇔ points(p) ≥ minPointsToQualify  ∧  presenceSec(p) ≥ presenceMinSec
```
**Ví dụ — Thú triều** (`w_DAMAGE 1, w_HEAL 0,8, w_KILL 2, w_OBJECTIVE 15, w_DEFEND 1`, `minPoints 40`, `presenceMin 180s`, `perPlayerCap 400`), tổng HP quái sự kiện 200.000: người A gây 12.000 sát thương (60 điểm), hạ 8 quái (16), giữ đèn 1 lần (15), đứng vùng phòng thủ 5 phút (5) → **96 điểm** → đủ ngưỡng; người B chỉ đứng vùng phòng thủ 10 phút (trần `defendCapSec` 300s → 5 điểm) → **không** đủ ngưỡng.

### 6.2. Bậc thưởng (mặc định)
| Bậc | Điều kiện | Thưởng |
|-----|-----------|--------|
| Đồng | Đủ ngưỡng | Bảng rơi bậc 1, linh thạch 0,6 × IPH |
| Bạc | Top 50% | Bảng rơi bậc 2, 1,0 × IPH |
| Vàng | Top 10% (tối thiểu 1 người / kênh) | Bảng rơi bậc 3, 1,5 × IPH, danh hiệu sự kiện (lần đầu) |

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/world_events/params.json` | Tham số chung |
| `shared/gamedata/world_events/events.json` | `EventDef[]` (ID `WE_*`) |
| `shared/gamedata/monsters/loot_tables.json` | `LT_WE_*` (`serverOnly`) |

```jsonc
// params.json
{ "upcomingHorizonHours": 48, "dailyFullRewards": 3, "worldBossDailyMain": 1,
  "afkNoInputSec": 120, "defendCapSec": 300, "cancelConsolationAfterPct": 0.5,
  "playerTriggerCooldownMin": 120, "playerTriggerBlockBeforeScheduledMin": 30,
  "rescaleEverySec": 30, "worldBossCaptureTopPerChannel": 3 }
```

### 7.1. Sự kiện mẫu Vực 1
| ID | Tên | Kind / phạm vi | Kích hoạt | Mô tả | Pha |
|----|-----|----------------|-----------|-------|-----|
| `WE_V1_QI_SURGE` | **Linh khí bùng phát** | `BUFF_ZONE` · `MAP` (1 bản đồ dã ngoại ngẫu nhiên, gồm cả `V1_F01`; mọi kênh) | `RANDOM` (cửa sổ 6h, 50%) | 30 phút: đả tọa ở bản đồ ×2 (`ST_QI_SURGE`), linh thảo làm mới ×1,5; không thưởng điểm — thưởng là tốc độ tu luyện | Alpha |
| `WE_V1_TREASURE` | **Dị bảo xuất thế** | `TREASURE` · `MAP` (1 kênh `BUSIEST`; trừ `V1_F01` — WE-R-015) | `SCHEDULED` 3 lần / ngày + `RANDOM` | Cột sáng ở khu ngẫu nhiên; thủ hộ tinh anh; ai **phá thủ hộ và giữ dị bảo 10s** được rương Dị Bảo; người góp sức đánh thủ hộ nhận bậc thưởng theo đóng góp; PvP theo bản đồ (tranh đoạt) | Alpha |
| `WE_V1_LINH_VU` | **Linh vũ** | `BUFF_ZONE` · `REGION` | `WEATHER` (`SPIRIT_RAIN`, GD-21) | Đả tọa +25%, linh thảo ×2, thông báo toàn server — thời tiết là nguồn | Alpha |
| `WE_V1_BEAST_TIDE` | **Thú triều** | `BEAST_TIDE` · `MAP` (`V1_F02` làng chài, mọi kênh) | `SCHEDULED` tối thứ 3 / 6, 20:00 | 4 đợt quái tràn vào làng, giữ 3 đèn trấn yêu; đợt cuối tinh anh; thất bại nếu cả 3 đèn tắt; `DISABLED_IN_ZONE` | Beta 2 |
| `WE_V1_INVASION` | **Ma tu xâm lấn** | `INVASION` · `REGION` (2 bản đồ ngẫu nhiên, trừ `V1_F01` — WE-R-015) | `RANDOM` (cửa sổ 24h, 30%) | Ma tu chiếm bản đồ: `REPLACE_SPAWNS`, `DISABLE_TELEPORT`; đánh lui 3 trận nhãn để mở lại; danh vọng `CHINH` + (GD-14) | Beta 2 |
| `WE_V1_WORLD_BOSS` | **Huyết Giao Vương** (world boss) | `WORLD_BOSS` · `MAP` (`V1_F04` đầm lầy, mọi kênh) | `SCHEDULED` thứ 7, 21:00 | Boss 4 pha, HP co giãn, thưởng 1 lần / ngày; có thể là boss thu phục được (WE-R-062, Beta 2) | Beta 2 |
| `WE_V1_TIEN_PHU` | **Thượng Cổ Tiên Phủ mở cửa** | `SCHEDULED_INSTANCE` · `SERVER` | `SCHEDULED` chủ nhật 20:00, 2 giờ | `OPEN_INSTANCE` hiểm địa chung GD-28 (chết thật bên trong) | Beta 2 |

**Validator (CI):** (WE-V-01) `actions[].type` thuộc danh sách trắng; `statusId`, `spawnGroupId`, `monsterId`, `lootTableId`, `poiTemplate`, `dangerZoneId` tồn tại; (WE-V-02) `cron` hợp lệ, `durationMin` > 0, `announceLeadMin` ≥ 5 cho `SCHEDULED`; (WE-V-03) mô phỏng lịch 30 ngày không có hai sự kiện chiến đấu `ACTIVE` chồng trên một bản đồ (WE-R-013); (WE-V-04) sự kiện ở `FIELD` / `HUB` không có hành động gây chết thật (WE-R-001); (WE-V-05) tổng giá trị thưởng kỳ vọng một bậc ≤ trần GD-13 theo dải; (WE-V-06) bảng rơi sự kiện `serverOnly`; (WE-V-07) sự kiện chiến đấu theo WE-R-015 không có `V1_F01` trong `targets.mapIds`.

---

## 8. Mục tiêu cân bằng

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Số sự kiện người chơi gặp mỗi giờ online (Vực 1) | 0,5–1 |
| Tỷ lệ người tham gia đạt ngưỡng | 70–85% |
| Thưởng sự kiện / thu nhập của người chơi | ≤ 15% tổng thu nhập linh thạch tuần |
| Tỷ lệ thú triều thành công | 60–80% |

---

## 9. Hợp đồng giao tiếp

### 9.1. API
| Endpoint | Mục đích |
|----------|----------|
| `GET /v1/world-events/upcoming?regionId=` | Lịch 48h tới (đã báo trước) + đang diễn ra |
| `POST /v1/accounts/me/event-reminders` | `{ eventId }` nhắc tôi (WE-R-021) |
| `POST /internal/v1/world-events/{instanceId}/results` | `{ contributions: [{ characterId, points, presenceSec }] , outcome }` → phát thưởng; idempotent theo `instanceId` |
| `POST /internal/v1/world-events/player-trigger` | `{ requestId, characterId, itemInstanceId, areaId }` (WE-R-014) |
| `GET /internal/v1/world-events/active?mapId=` | Game Server lấy sự kiện đang chạy khi room khởi động |

### 9.2. Message (TD-02)
`S2C_WorldEventAnnounce { instanceId, eventId, startsAtMs, mapIds }` · `S2C_WorldEventState { instanceId, state, phase, progress, endsAtMs }` · `S2C_EventContribution { instanceId, myPoints, myRank, top: [{ name, points }] }` · `S2C_WorldEventResult { instanceId, outcome, tier, rewards }`.

### 9.3. Sự kiện domain
| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `world_event.announced` | `{ instanceId, eventId, startsAt, scope }` | Game Server (biểu tượng), thông báo GD-14, push nhắc |
| `world_event.started` / `world_event.ended` | `{ instanceId, eventId, outcome? }` | Game Server áp / gỡ hiệu ứng; telemetry; GD-15 (nhiệm vụ "tham gia sự kiện") |

---

## 10. Lưu trữ
```sql
CREATE TABLE world_event_log (                      -- TD-03 §5.6 (placeholder → chuẩn ở đây)
  id VARCHAR(64) PRIMARY KEY,                       -- WEI_{eventId}_{slot}
  event_id VARCHAR(48) NOT NULL, state VARCHAR(12) NOT NULL,
  scope_resolved JSONB NOT NULL, seed BYTEA NOT NULL,
  announced_at TIMESTAMPTZ NOT NULL, starts_at TIMESTAMPTZ NOT NULL, ends_at TIMESTAMPTZ NOT NULL, ended_at TIMESTAMPTZ,
  outcome VARCHAR(12), participants INT, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE world_event_participation (
  instance_id VARCHAR(64) NOT NULL, character_id UUID NOT NULL, account_id UUID NOT NULL,
  points INT NOT NULL, presence_sec INT NOT NULL, tier VARCHAR(8), rewarded BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (instance_id, character_id)
);
CREATE TABLE world_event_daily (account_id UUID NOT NULL, day DATE NOT NULL, full_rewards SMALLINT NOT NULL DEFAULT 0,
  world_boss_claims JSONB NOT NULL DEFAULT '[]', PRIMARY KEY (account_id, day));
CREATE TABLE event_reminder (account_id UUID NOT NULL, event_id VARCHAR(48) NOT NULL, PRIMARY KEY (account_id, event_id));
```
`item_log` mã mới: `EVENT_REWARD`. `rng_audit.kind` mới: `WE_SCHEDULE` (tái lập lịch ngẫu nhiên), `WE_REWARD` (roll bảng rơi sự kiện).

---

## 11. UI/UX
- **Bản đồ thế giới / minimap:** biểu tượng sự kiện + đếm ngược; bảng "Sắp diễn ra" (48h) với nút "Nhắc tôi" và "Tới đó" (đường tới truyền tống trận gần nhất).
- **HUD sự kiện** (góc phải): tên, pha, thanh tiến độ (đợt / HP boss / đèn), điểm của tôi, hạng, top 3; viền vùng sự kiện trên mặt đất.
- **Kết thúc:** bảng tổng kết (bậc, phần thưởng), danh hiệu lần đầu.
- **Cột sáng dị bảo** nhìn thấy từ xa trong bản đồ (hiệu ứng ánh sáng, không lộ khi chưa `ACTIVE`).

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| TD-12 | Lịch tất định trên World Service leader; room nhận `world_event.*`; kênh |
| GD-21 | Linh vũ, thời tiết; dị tượng thông báo |
| GD-12 | Quái, boss, bảng rơi, tính công |
| GD-13 | Thưởng theo IPH, trần |
| GD-14 | Thông báo SO-R-060, danh vọng phe từ sự kiện |
| GD-15 | Nhiệm vụ "tham gia sự kiện", nhiệm vụ mùa |
| GD-25 | World boss thu phục được (WE-R-062) |
| GD-28 | Tiên Phủ / Ma Uyên mở theo lịch |
| GD-30 | Sơn Môn Đại Hội, Phế Tích |
| GD-32 | PvP trong vùng sự kiện |
| TD-09 | Cờ `ff.worldEvents.enabled` (kill switch) |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Leader World Service đổi giữa lúc sự kiện chạy | Lịch tất định, bản mới tiếp tục; trạng thái `world_event_log` là nguồn |
| Tiến trình Game Server chứa kênh sự kiện sập | Kênh đó: đóng góp đã báo cáo (gửi định kỳ 60s) được giữ; nếu > 50% thời lượng → thưởng an ủi |
| Người chơi đổi kênh giữa sự kiện `ALL` kênh | Điểm theo `instanceId` gộp chung (cùng sự kiện), nhưng `presenceSec` tính lại từ đầu ở kênh mới (chống nhảy kênh) |
| Người chơi trọng thương trong sự kiện | Giữ điểm; hồi sinh có thể quay lại |
| Kill switch tắt giữa sự kiện | `CANCELLED`, thưởng an ủi theo WE-R-062 / §4 |
| Hai sự kiện trùng bản đồ | WE-R-013 dời sự kiện ưu tiên thấp |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).
| Sự kiện | Trường | Câu hỏi |
|---------|--------|---------|
| `world_event_run` | eventId, participants, outcome, durationSec | Sự kiện nào đông / thất bại nhiều |
| `world_event_contribution` | eventId, pointsDistribution, qualifyRate | Ngưỡng có hợp lý, có ăn bám không |
| `world_event_travel` | eventId, timeToArriveSec | Báo trước có đủ |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="we-ac-001"></a>WE-AC-001 | Cùng `worldSeed`, hai bản API | Tính lịch 48h | Giống hệt (WE-R-002) |
| WE-AC-002 | Leader đổi giữa chừng | Sau đó | Lịch không đổi, sự kiện đang chạy không bị lặp / mất |
| WE-AC-003 | Ví dụ §6.1 người A / B | Kết thúc thú triều | A đủ ngưỡng (96), B không (5) |
| WE-AC-004 | 40 người đủ ngưỡng | Phát thưởng | Mỗi người roll bảng rơi riêng, không chia phần |
| WE-AC-005 | Tài khoản đã nhận 3 thưởng đầy đủ hôm nay | Sự kiện thứ 4 | Chỉ bậc thấp nhất |
| WE-AC-006 | World boss, đã nhận thưởng chính hôm nay | Hạ lần nữa | Không nhận thưởng chính |
| WE-AC-007 | Thú triều (`DISABLED_IN_ZONE`) | Hai người `SLAUGHTER` trong vùng | Không gây sát thương nhau |
| WE-AC-008 | Dị bảo (`INHERIT`) ở bản đồ có PvP | Tranh dị bảo | PvP theo GD-32, sát nghiệp áp dụng |
| WE-AC-009 | Sự kiện ở `FIELD`, người chơi hết máu | Kết quả | Trọng thương, không chết thật (WE-R-001) |
| WE-AC-010 | 30 người trong phạm vi | Quái thú triều đợt 2 | HP / số quái theo WE-R-030, không vượt ngân sách thực thể |
| WE-AC-011 | Validator: hai sự kiện chiến đấu chồng lịch | CI | Fail WE-V-03 |
| WE-AC-012 | World boss thu phục được, Kiệt Sức | Người hạng 5 kênh ném phù | Từ chối (chỉ top 3 / kênh) |
| WE-AC-013 | Đứng AFK trong vùng phòng thủ 10 phút | Điểm | Không cộng sau 120s không input |
| WE-AC-014 | Kill switch tắt khi sự kiện đã chạy 60% | Kết quả | `CANCELLED`, thưởng an ủi bậc thấp nhất cho người đủ ngưỡng |
| <a id="we-ac-015"></a>WE-AC-015 | Lịch 30 ngày mô phỏng với `worldSeed` bất kỳ | Bộ chọn bản đồ ngẫu nhiên | `WE_V1_TREASURE`, `WE_V1_INVASION` không bao giờ chọn `V1_F01`; `WE_V1_QI_SURGE` có thể chọn `V1_F01`; `EventDef` chiến đấu ghi `V1_F01` trong `targets.mapIds` → fail WE-V-07 (WE-R-015) |

---

## 16. Phạm vi

| Hạng mục | Alpha | Beta 1 | Beta 2 | Sau launch |
|----------|:---:|:---:|:---:|:---:|
| Bộ lập lịch tất định, `ANNOUNCED → ENDED`, HUD, thông báo | ✅ | | | |
| Linh khí bùng phát, Dị bảo xuất thế, Linh vũ | ✅ | | | |
| Đóng góp + bậc thưởng + trần ngày | ✅ | | | |
| Sự kiện do người chơi kích hoạt | | ✅ | | |
| Thú triều, Ma tu xâm lấn, world boss | | | ✅ | |
| Tiên Phủ / Ma Uyên theo lịch, Sơn Môn Đại Hội | | | ✅ | |
| World boss thu phục được | | | ✅ | |
| Sự kiện theo mùa / lễ hội (trang trí bản đồ MF-Q-3) | | | | ✅ |

---

## 17. Câu hỏi mở
- [ ] **WE-Q-1:** Giờ sự kiện cố định theo giờ Việt Nam có bất lợi cho người chơi lệch giờ không — có cần lặp world boss 2 khung giờ / ngày?
- [ ] **WE-Q-2:** Có cho tông môn tự lập "đăng ký" sự kiện để nhận thêm Uy vọng (GD-30) không?
- [ ] **WE-Q-3:** Ma tu xâm lấn khóa truyền tống trận có gây khó chịu quá cho người chỉ đi ngang qua vực?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: phạm vi / kích hoạt, `EventDef` + danh sách trắng hành động, bộ lập lịch tất định trên World Service leader, quy mô theo số người, đóng góp chống ăn bám + bậc thưởng + trần ngày, world boss (thưởng 1 lần / ngày, **trả lời SB-Q-6: thu phục được, quyền ném phù cho top 3 đóng góp mỗi kênh**), Tiên Phủ / Ma Uyên / Sơn Môn Đại Hội / Phế Tích trên cùng lịch, PvP trong vùng sự kiện, 7 sự kiện mẫu Vực 1, API, DDL, nghiệm thu |
| v0.2 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
| v0.3 | 2026-10-25 | — | Đợt rà nhất quán: thời tiết của `WE_V1_LINH_VU` dùng `Weather.SPIRIT_RAIN` theo glossary (thay `LINH_VU`); `EventDef.weather` kiểu `Weather`. Mã sự kiện `WE_V1_LINH_VU` giữ nguyên |
| v0.4 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: WE-R-015 loại `V1_F01` khỏi sự kiện chiến đấu (`TREASURE`, `BEAST_TIDE`, `INVASION`, `WORLD_BOSS`, hành động spawn / khóa truyền tống), vẫn nhận `WE_V1_QI_SURGE` / `WE_V1_LINH_VU`; validator WE-V-07; WE-AC-015 |