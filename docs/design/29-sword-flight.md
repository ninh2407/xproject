# GD-31 — Ngự kiếm phi hành

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-31 · Prefix ID: `FLY` |
| Owner | Game Designer + Client Dev + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 5b / Beta 2 (PLAN §7); cờ dữ liệu va chạm `fly` và vùng `NO_FLY` nên có trong định dạng bản đồ từ Phase 1 (TD-13) để không phải làm lại bản đồ |
| Quyết định nền | PLAN §4 (ngự kiếm Beta: di chuyển nhanh trong bản đồ, bay qua địa hình và quái, camera zoom ra, không tương tác khi bay), D-01 (logic 2D, độ cao chỉ hiển thị), D-17 / GD-19 (ngoại hình phi kiếm chỉ là cosmetic) |
| Liên quan | [PLAN §4](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-07 Kỹ năng](13-skills.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-19 Monetization](50-monetization.md) · [GD-21 Open World](22-open-world.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-24 Camera & Điều khiển](25-camera-controls.md) · [GD-25 Linh thú](26-spirit-beasts.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [GD-32 PvP](31-pvp-infamy.md) · [TD-02 Network](../tech/01-network.md) · [TD-12 World & Map](../tech/11-world-maps.md) · [TD-13 Map Format](../tech/12-map-editor.md) · [TD-14 Geometry](../tech/13-geom2d.md) · [TD-11 Performance](../tech/10-performance.md) |

> Bay **không** thêm trục độ cao vào logic: nhân vật vẫn là một hình tròn trên mặt phẳng `(x, z)` (TD-14), chỉ đổi **mặt nạ va chạm** (bỏ qua vật thấp, vẫn chặn vật cao / biên bản đồ), **tốc độ** và **những gì được phép làm**. Độ cao chỉ là hiển thị ở client.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-31 | Mục |
|--------------|-----------------|-----|
| TD-14 | Cờ va chạm mới `fly` (vật cao chặn cả khi bay) và mặt nạ va chạm khi bay | §5.3 |
| TD-13 | Cờ `flags.fly` trên shape, vùng `NO_FLY`, validator | §5.3, §7 |
| TD-02 / TD-06 | Trạng thái bay trong State, message, kiểm tra tốc độ | §9 |
| TD-12 | Chuyển bản đồ khi đang bay (tự hạ cánh) | §5.6 |
| GD-24 | Phím `N`, camera zoom khi bay (trả lời CAM-Q-3) | §5.1, §11 |
| GD-12 / GD-32 | Ai được tấn công người đang bay, rơi xuống khi trúng đòn | §5.5 |
| GD-19 | Ngoại hình phi kiếm (`COS_FLIGHT_*`) chỉ hình ảnh | §5.8 |

---

## 1. Mục tiêu

**Trụ cột:** P2 *Khám phá* (di chuyển nhanh, nhìn bao quát), P5 *Cảm giác tu tiên* (khoảnh khắc "ngự kiếm" là biểu tượng của Trúc Cơ).

- Đạt Trúc Cơ là **cột mốc cảm xúc**: lần đầu bay vượt rừng trúc.
- Giảm thời gian đi lại trong bản đồ rộng (GD-21), nhưng **không** thay truyền tống trận, **không** là cách thoát chiến đấu, **không** bỏ qua nội dung (hiểm địa, cơ duyên, thu thập vẫn phải đáp xuống).
- Không đổi kiến trúc logic (vẫn 2D) và không đổi ngân sách mạng / tick.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="fly-r-001"></a>**FLY-R-001** | Bay là **chế độ di chuyển**, không phải trục độ cao trong logic: vị trí server vẫn `(x, z)`; độ cao chỉ hiển thị (D-01, TD-14). |
| <a id="fly-r-002"></a>**FLY-R-002** | Khi bay **không** tấn công, dùng kỹ năng, dùng ô nhanh, tương tác, hái, thu phục, nhặt đồ, vào cổng / cửa instance (PLAN §4). Chỉ được: di chuyển, quét thần thức, chat, mở giao diện xem. |
| <a id="fly-r-003"></a>**FLY-R-003** | **Không cất cánh khi đang chiến đấu** (cùng khóa chiến đấu 5s của chuyển bản đồ, TD-12 WM-R-041); bay không phải cách thoát giao tranh. |
| <a id="fly-r-004"></a>**FLY-R-004** | Mọi trạng thái bay do **server** quyết định; client chỉ gửi ý định bật / tắt (TD-01 TA-R-002). |
| <a id="fly-r-005"></a>**FLY-R-005** | Ngoại hình phi kiếm **chỉ là hình ảnh**: không đổi tốc độ, linh lực tiêu hao, hay bất kỳ chỉ số nào (D-17, GD-19). |
| <a id="fly-r-006"></a>**FLY-R-006** | Không bay ở vùng / bản đồ có thể chết thật (hiểm địa, thiên kiếp) và ở instance (TD-12 §2). |

---

## 2. Tổng quan

```
 GROUNDED ──[N, ngoài chiến đấu, đủ linh lực, nơi cho phép]──► TAKING_OFF (1,0s, bị đánh trúng → hủy)
    ▲                                                                 │
    │                                                                 ▼
 LANDING (0,5s) ◄──[N / sắp hết linh lực / tới biên / vào NO_FLY]── FLYING (14–18 m/s, tiêu linh lực)
    ▲                                                                 │ trúng đòn (quái chống bay / PvP)
    │                                                                 ▼
    └──────────────────────────────────────────────────────── FALLING (rơi 0,8s, không sát thương rơi, choáng ngắn)
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type FlightState = 'GROUNDED' | 'TAKING_OFF' | 'FLYING' | 'LANDING' | 'FALLING';
type ForcedLandReason = 'PLAYER' | 'MP_EMPTY' | 'NO_FLY_ZONE' | 'MAP_EDGE' | 'HIT' | 'TRANSFER' | 'DISCONNECT' | 'STATUS';

interface FlightRuntime {                // trong room, không lưu DB (§10)
  state: FlightState;
  stateUntilMs: number;                  // thời điểm kết thúc TAKING_OFF / LANDING / FALLING
  landingTarget: [number, number] | null;   // điểm đáp đi được gần nhất (§5.4)
  lastReason: ForcedLandReason | null;
}

interface FlightParams {                 // shared/gamedata/world/flight.json
  unlockRealm: string;                   // "TC1"
  takeOffSec: number; landSec: number; fallSec: number; fallStunSec: number;
  speedByMajorRealm: Record<string, number>;   // TC 14 · KD 16 · NA 18 (m/s)
  takeOffMpPctOfMax: number;             // 0.05
  drainMpPctOfMaxPerSec: number;         // 0.01
  autoLandAtMpPct: number;               // 0.03
  combatLockSec: number;                 // 5 (= transfer.combatLockSec)
  bodyRadius: number;                    // 0.4 (giữ như mặt đất)
  landingSearchRadius: number;           // 25 (m) tìm điểm đáp đi được
  fogRevealMult: number;                 // 1.5 (nhìn bao quát hơn)
  cameraDistance: number;                // 30 (m, GD-24)
  visualHeightM: number;                 // 6 (chỉ hiển thị)
}
```

Mở khóa lưu bằng cờ nhân vật `FLAG_FLIGHT_UNLOCKED` (GD-22 / GD-15 `character_flag`). Ngoại hình đang dùng nằm trong `character_appearance` (GD-19).

---

## 4. Máy trạng thái

| Từ | Sang | Điều kiện | Ghi chú |
|----|------|-----------|---------|
| `GROUNDED` | `TAKING_OFF` | `N`; có `FLAG_FLIGHT_UNLOCKED`; cảnh giới ≥ `unlockRealm`; không chiến đấu ≥ `combatLockSec`; linh lực ≥ `takeOffMpPctOfMax`; vị trí không trong `NO_FLY`; trạng thái hành động `IDLE` / `MOVING` (GD-07 §4) | Trừ linh lực cất cánh khi vào `FLYING` |
| `TAKING_OFF` | `FLYING` | Hết `takeOffSec` | Hoạt ảnh triệu phi kiếm |
| `TAKING_OFF` | `GROUNDED` | Bị trúng đòn / bị khống chế / di chuyển? **Không** — di chuyển được trong lúc cất cánh; chỉ trúng đòn mới hủy | Không trừ linh lực |
| `FLYING` | `LANDING` | `N`; linh lực ≤ `autoLandAtMpPct`; vào `NO_FLY`; chạm biên / cổng (§5.6) | Đáp tại `landingTarget` (§5.4) |
| `FLYING` | `FALLING` | Trúng đòn hợp lệ (§5.5); bị khống chế (`STUN`/`ROOT`/`FROZEN`/`ENTANGLE`, GD-06) | Không sát thương rơi |
| `LANDING` / `FALLING` | `GROUNDED` | Hết thời gian | `FALLING` kèm choáng `fallStunSec` (0,5s), không tích kháng khống chế PvP |

---

## 5. Quy tắc

### 5.1. Mở khóa và phím
- <a id="fly-r-010"></a>**FLY-R-010** Mở khóa khi đột phá **Trúc Cơ** (`TC1`) **và** hoàn thành nhiệm vụ ngắn `QC_SWORD_FLIGHT` (sư phụ / trưởng lão tông môn NPC dạy Ngự Kiếm Quyết; tán tu nhận từ NPC ẩn sĩ ở Thanh Vân Trấn — GD-15) → đặt `FLAG_FLIGHT_UNLOCKED`. Bổ sung mục "ngự kiếm" vào bảng mở khóa GD-04 §5.1 (đã ghi "(Beta)").
- <a id="fly-r-011"></a>**FLY-R-011** Phím mặc định **`N`** (`ActionId` mới `FLIGHT_TOGGLE`, đổi được — GD-24 CAM-R-023). Không dùng "giữ `Space`" vì `Space` là né lướt, giữ lâu dễ gây bay nhầm giữa giao tranh. Tay cầm: giữ `LB` + `A` 0,5s; cảm ứng: nút phi kiếm cạnh joystick (chỉ hiện khi đủ điều kiện).
- <a id="fly-r-012"></a>**FLY-R-012** Ngự kiếm **không chiếm ô trang bị** (không dùng ô `ARTIFACT` của GD-09): phi kiếm là năng lực của nhân vật, hình dạng do ngoại hình quyết định (§5.8).

### 5.2. Tốc độ và linh lực
- <a id="fly-r-020"></a>**FLY-R-020** Tốc độ bay cố định theo cảnh giới lớn (`speedByMajorRealm`: TC 14 · KD 16 · NA 18 m/s), **không** cộng `moveSpeed` từ trang bị / thiên phú / status (tránh chồng chỉ số làm vỡ kiểm tra tốc độ và AOI); `SLOW` / `ST_CHILLED` vẫn giảm 50% hiệu lực lên tốc độ bay.
- <a id="fly-r-021"></a>**FLY-R-021** Cất cánh tiêu `takeOffMpPctOfMax` (5%) `maxMp`; khi bay tiêu `drainMpPctOfMaxPerSec` (1% / giây) và **ngừng hồi linh lực**; linh lực ≤ 3% → tự hạ cánh (`MP_EMPTY`). Thọ nguyên, tu vi, thể lực **không** đổi.
- <a id="fly-r-022"></a>**FLY-R-022** Đả tọa / đan hồi linh lực không dùng được khi bay (FLY-R-002).

### 5.3. Va chạm khi bay (TD-14)
- <a id="fly-r-030"></a>**FLY-R-030** Lớp va chạm bản đồ (TD-14 §3.3) thêm cờ **`fly`** trên mỗi shape: `true` = vật **cao** chặn cả khi bay (vách núi, tháp, tường thành, cây cổ thụ lớn); `false` (mặc định) = vật thấp (đá, bụi, hàng rào, nhà thấp, nước, vực) — **bay qua**. Biên bản đồ luôn `fly = true`.
- <a id="fly-r-031"></a>**FLY-R-031** Khi `FLYING` / `LANDING`, `move()` (TD-14 §4) dùng mặt nạ `fly` thay cho `move`. Khi `TAKING_OFF` vẫn dùng mặt nạ `move` (còn trên mặt đất).
- <a id="fly-r-032"></a>**FLY-R-032** Vùng **`NO_FLY`** (`ZoneDef.kind` mới, TD-13): mặc định phủ toàn bộ bản đồ `HUB`, vùng an toàn quanh truyền tống trận / lối nối / cửa instance (bán kính `safeZone.radius`), tổng đàn tông NPC, quanh `SECT_SITE`. Bay vào → tự hạ cánh tại mép vùng (`NO_FLY_ZONE`).
- <a id="fly-r-033"></a>**FLY-R-033** Thực thể khác (người chơi, quái, NPC) không chặn người bay (như mặt đất, GEO-R-021). Chướng ngại động `solid` (tường đất GD-07) chặn người bay chỉ khi có cờ `fly`.

### 5.4. Điểm đáp
- <a id="fly-r-040"></a>**FLY-R-040** Khi chuyển sang `LANDING` / `FALLING`, server tìm **điểm đi được gần nhất** trên navmesh r0,4 (TD-14 §8) trong `landingSearchRadius` (25m) quanh vị trí hiện tại; nhân vật **lướt** tới điểm đó trong thời gian đáp (tối đa `flySpeed × landSec`, quá xa thì tiếp tục lướt ở trạng thái `LANDING` tới khi tới nơi, bất tử và không điều khiển được). Không tìm thấy trong 25m (hiếm, vd. giữa hồ lớn) → lướt về **điểm cất cánh gần nhất** đã ghi.
- <a id="fly-r-041"></a>**FLY-R-041** Không bao giờ đáp vào trong shape có cờ `move` (kẹt trong đá / nước sâu); validator bản đồ bảo đảm mọi vùng bay được có điểm đi được trong 25m hoặc có đường lướt về (MF-V mới, §7).

### 5.5. Bị tấn công khi bay
- <a id="fly-r-050"></a>**FLY-R-050** Quái thường **không nhắm** người đang bay (mất mục tiêu, trở về). Ngoại lệ: quái có tag `anti_air` (vd. họ BIRD / DEMON cánh, boss chỉ định — GD-12) và **đạn / vùng** của quái đã bắn từ trước vẫn trúng nếu chạm hình tròn thân (TD-14 GEO-R-040).
- <a id="fly-r-051"></a>**FLY-R-051** **PvP** (GD-32): ở bản đồ có PvP, người đang bay **có thể bị** người chơi khác tấn công bằng kỹ năng `PROJECTILE` và `GROUND_CIRCLE` (không phải cận chiến `CONE` / `LINE` / `DASH` — người dưới đất "với không tới"). Trúng đòn → `FALLING`, nhận sát thương bình thường (hệ số PvP).
- <a id="fly-r-052"></a>**FLY-R-052** Bất kỳ lượt trúng gây sát thương hoặc khống chế hợp lệ → `FALLING` (rơi 0,8s, choáng 0,5s sau khi chạm đất), **không** có sát thương rơi. Bị kéo xuống tính là vào chiến đấu (không cất cánh lại trong 5s).

### 5.6. Chuyển bản đồ, cổng, instance
- <a id="fly-r-060"></a>**FLY-R-060** Chạm **đoạn biên** (`SEAM`) khi đang bay: tự `LANDING` (`TRANSFER`) rồi chuyển bản đồ theo TD-12 §5; xuất hiện ở bản đồ đích **trên mặt đất**. (Bay qua biên không được giữ trạng thái bay — giữ luồng chuyển bản đồ đơn giản và kiểm tra điều kiện đầy đủ.)
- <a id="fly-r-061"></a>**FLY-R-061** Cổng, cổng vực, cửa instance, truyền tống trận cần **tương tác trên mặt đất** (FLY-R-002); chúng nằm trong `NO_FLY` (FLY-R-032).
- <a id="fly-r-062"></a>**FLY-R-062** Không bay ở: `HD` (hiểm địa), `BC`, `KN`, `DP`, `TRIAL` và thiên kiếp (FLY-R-006); trú địa tông môn `TD` cho phép bay nếu cấp tông ≥ 3 (cảnh quan riêng, không có nguy hiểm) — tùy chọn của GD-30.

### 5.7. Khám phá khi bay
- <a id="fly-r-070"></a>**FLY-R-070** Bán kính mở fog khi bay = `fog.revealRadius × fogRevealMult` (24 × 1,5 = 36m) — nhìn bao quát hơn từ trên cao (GD-21 §3.5). Quét thần thức (`G`) dùng được, giữ nguyên công thức.
- <a id="fly-r-071"></a>**FLY-R-071** Khi bay **không** kích hoạt cơ duyên theo di chuyển / khu vực, không phát hiện bí mật bằng mắt (chỉ bằng quét), không có Linh chủng "đánh động" (GD-25). Cơ duyên "trên mây" riêng là nội dung sau launch (FLY-Q-2).
- <a id="fly-r-072"></a>**FLY-R-072** Linh thú xuất chiến (GD-25) **tạm thu** khi cất cánh và tự gọi lại khi đáp (không tính hồi chiêu gọi).

### 5.8. Ngoại hình
- <a id="fly-r-080"></a>**FLY-R-080** Mặc định: phi kiếm theo hình vũ khí đang cầm (kiếm → phi kiếm; quạt → quạt lớn bay). Ngoại hình khác (`COS_FLIGHT_*`: kiếm ngọc, hồ lô, mây lành, hạc…) là cosmetic tài khoản (GD-19), có nguồn trong game (thành tựu, Vấn Đạo Lệnh nhánh miễn phí) và nguồn mua; **không** đổi tốc độ / linh lực / va chạm (FLY-R-005).

---

## 6. Công thức

```
flySpeed     = speedByMajorRealm[majorRealm] × (1 − 0,5 × slowFraction)     (slowFraction từ status SLOW, chặn 0..0,6)
mpTakeOff    = ceil(takeOffMpPctOfMax × maxMp)
mpDrain/s    = drainMpPctOfMaxPerSec × maxMp
flyTimeMax   = (mpHiệnTại − mpTakeOff − autoLandAtMpPct × maxMp) / mpDrain/s
distanceMax  = flyTimeMax × flySpeed
```
**Ví dụ:** `TC1`, `maxMp` 377 (bảng cơ bản GD-06 SF-R-020, chưa tính trang bị), linh lực đầy: cất cánh tiêu 19; tiêu 3,77 / giây; tự hạ ở 11 → bay tối đa `(377 − 19 − 11) / 3,77 ≈ 92s`, ở 14 m/s ≈ **1.290m** (khoảng 3–4 lần chiều ngang một bản đồ dã ngoại 300–400m) — đủ băng bản đồ, không đủ thay truyền tống trận đi xa.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/world/flight.json` | `FlightParams` |
| `shared/maps/{mapId}.json` | Cờ `flags.fly` trên shape va chạm, `ZoneDef` kind `NO_FLY` (TD-13) |
| `shared/gamedata/monetization/cosmetics.json` | `COS_FLIGHT_*` (GD-19) |
| `shared/quests/defs/...QC_SWORD_FLIGHT.yaml` | Nhiệm vụ mở khóa (GD-15) |

```jsonc
// world/flight.json
{
  "unlockRealm": "TC1",
  "takeOffSec": 1.0, "landSec": 0.5, "fallSec": 0.8, "fallStunSec": 0.5,
  "speedByMajorRealm": { "TC": 14, "KD": 16, "NA": 18, "HT": 20 },
  "takeOffMpPctOfMax": 0.05, "drainMpPctOfMaxPerSec": 0.01, "autoLandAtMpPct": 0.03,
  "combatLockSec": 5, "bodyRadius": 0.4, "landingSearchRadius": 25,
  "fogRevealMult": 1.5, "cameraDistance": 30, "visualHeightM": 6,
  "slowEffectivenessMult": 0.5,
  "allowedMapTypes": ["F"], "sectGroundsMinLevel": 3
}
```

**Validator (CI):**
- (FLY-V-01) `speedByMajorRealm × tickMs` ≤ một nửa ô broadphase TD-14 (4m) — không xuyên qua vật `fly` mỏng trong một tick (kết hợp bước con TD-14 GEO-R-030);
- (FLY-V-02) bản đồ `F`: mọi điểm trong vùng bay được có điểm đi được trong `landingSearchRadius` (lấy mẫu lưới 8m) — nếu không, phải khai báo `NO_FLY` (FLY-R-041);
- (FLY-V-03) `HUB` phủ `NO_FLY` toàn bộ; mọi `TELEPORT_ARRAY`, kết nối, `INSTANCE_DOOR`, `SECT_HQ`, `SECT_SITE` nằm trong `NO_FLY`;
- (FLY-V-04) biên bản đồ có `fly = true`;
- (FLY-V-05) `COS_FLIGHT_*` không có trường chỉ số (GD-19).

---

## 8. Mục tiêu cân bằng

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Thời gian băng một bản đồ dã ngoại 400m (bay / chạy) | ~30s / ~70s |
| Tỷ lệ người chơi Trúc Cơ+ dùng bay ≥ 1 lần / phiên | ≥ 70% |
| Tỷ lệ chuyến bay kết thúc vì bị bắn rơi (PvP, map có PvP) | < 10% |
| Thời gian trung bình mỗi chuyến bay | 15–45s (dùng để băng bản đồ, không "sống trên trời") |

---

## 9. Hợp đồng giao tiếp

| Loại | Tên | Payload / ghi chú |
|------|-----|-------------------|
| C2S | `C2S_FlightToggle` | `{ seq }` — bật / tắt (FLY-R-011); rate limit nhóm "Khác" TD-02 |
| S2C | `S2C_FlightRejected` | `{ seq, reason: 'LOCKED' \| 'IN_COMBAT' \| 'NO_MP' \| 'NO_FLY_ZONE' \| 'BUSY' }` |
| S2C | `S2C_FlightState` | `{ entityId, state, untilMs, landingTarget?, reason? }` (AOI) |
| State | `PlayerPublic.flags` bit `FLYING` (TD-02 §4.2) | Người xem vẽ nhân vật trên cao, bóng dưới đất |
| Input | `C2S_Input` không đổi; server dùng `flySpeed` khi `FLYING` để kiểm tra tốc độ (TD-02 NW-R-030 / NW-R-031) | Dự đoán client dùng cùng hàm `shared/sim/flight` (mặt nạ `fly`) |
| Snapshot | `TA-R-110`: **không** lưu trạng thái bay; snapshot lúc bay ghi `landingTarget` làm vị trí | §10 |

Không có endpoint REST mới (mở khóa đi qua nhiệm vụ GD-15 `SET_FLAG`).

---

## 10. Lưu trữ
- Không bảng mới. `FLAG_FLIGHT_UNLOCKED` trong `character_flag` (GD-22 §10, TD-03); ngoại hình trong `character_appearance` (GD-19).
- <a id="fly-r-090"></a>**FLY-R-090** Trạng thái bay **không bền**: đăng xuất / mất kết nối / tiến trình sập khi đang bay → vị trí lưu là `landingTarget` (điểm đi được gần nhất, FLY-R-040); vào lại luôn ở mặt đất.

---

## 11. UI/UX
- **Lần đầu bay** (sau `QC_SWORD_FLIGHT`): cảnh ngắn 4s phi kiếm bay lên qua rừng trúc (bỏ qua được, GD-24), mở khóa thành tựu.
- **Camera:** khi `FLYING` zoom ra mượt tới `cameraDistance` 30m trong 0,6s (vẫn trong AOI, GD-24 CAM-R-002 — khung nhìn ≈ 60×45m, nửa đường chéo 37,5m < 40m); hạ cánh zoom về mức người chơi đặt. Trả lời GD-24 CAM-Q-3.
- **Hiển thị:** nhân vật nâng `visualHeightM` 6m + dao động nhẹ, vệt kiếm khí, bóng tròn trên mặt đất đúng vị trí logic; vật thấp dưới chân mờ nhẹ.
- **HUD:** thanh linh lực đổi màu và hiện "thời gian bay còn lại" (công thức §6); cảnh báo khi sắp vào `NO_FLY` (viền mây ở mép vùng).
- **Minimap:** hiện vùng `NO_FLY` (gạch chéo) khi đang bay.
- **Cảm ứng / tay cầm:** nút phi kiếm chỉ hiện khi đủ điều kiện; xám kèm lý do khi không (đang chiến đấu, thiếu linh lực, vùng cấm).

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-04 | Mở ở Trúc Cơ (bảng mở khóa) |
| GD-06 | Linh lực tiêu hao, ngừng hồi; `SLOW` giảm nửa hiệu lực |
| GD-07 | Không dùng kỹ năng khi bay; trạng thái hành động `IDLE` / `MOVING` mới cất cánh |
| GD-12 | Quái thường bỏ qua người bay; tag `anti_air` |
| GD-15 | `QC_SWORD_FLIGHT`, cờ mở khóa |
| GD-19 | `COS_FLIGHT_*` chỉ hình ảnh |
| GD-21 | Fog mở rộng ×1,5, quét được, không cơ duyên di chuyển khi bay |
| GD-24 | Phím `N`, camera 30m, nút cảm ứng |
| GD-25 | Thú tạm thu khi bay |
| GD-28 / GD-04 (thiên kiếp) | Cấm bay |
| GD-32 | Bị bắn rơi bằng `PROJECTILE` / `GROUND_CIRCLE` ở bản đồ PvP |
| TD-02 / TD-06 | State bit `FLYING`, message, kiểm tra tốc độ, system `flight` |
| TD-12 | Chạm biên khi bay → đáp rồi chuyển bản đồ |
| TD-13 / TD-14 | Cờ `fly`, vùng `NO_FLY`, mặt nạ va chạm, validator |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Hết linh lực giữa hồ lớn | Tự `LANDING`, lướt tới điểm đi được gần nhất (FLY-R-040); không rơi xuống nước |
| Bị bắn rơi trên vực | `FALLING` rồi lướt tới điểm đi được gần nhất, không chết, không sát thương rơi |
| Mất kết nối khi đang bay | Server đáp ngay tại `landingTarget`, nhân vật đứng yên theo TD-02 NW-R-072 |
| Cất cánh ngay khi bị quái nhắm (chưa trúng đòn) | Được nếu chưa vào trạng thái chiến đấu; quái mất mục tiêu (không phải `anti_air`) |
| Bay vào vùng an toàn quanh truyền tống trận | Tự đáp ở mép vùng |
| Bay tới đoạn biên khi bản đồ đích đang đầy | Đáp; luồng chuyển bản đồ xử lý chọn kênh như thường (TD-12) |
| Ác nhân (GD-32) bay để trốn Chấp Pháp | Chấp Pháp có tag `anti_air`; bay vẫn tiêu linh lực và bị bắn rơi |
| Client gửi `C2S_Input` vượt `flySpeed` | Bỏ khung dư như mặt đất (NW-R-030), ghi tín hiệu chống gian lận (TD-08) |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện | Trường | Câu hỏi |
|---------|--------|---------|
| `flight_start` / `flight_end` | mapId, durationSec, distanceM, endReason | Bay dùng để làm gì, kết thúc vì sao (§8) |
| `flight_rejected` | reason | Điều kiện có gây khó chịu |
| `flight_shot_down` | mapId, byMonster / byPlayer, pvp | Tỷ lệ bị bắn rơi |
| `flight_landing_glide` | glideDistanceM | Có vùng thiếu điểm đáp (sửa bản đồ) |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="fly-ac-001"></a>FLY-AC-001 | `TC1` chưa làm `QC_SWORD_FLIGHT` | Bấm `N` | `S2C_FlightRejected LOCKED` |
| FLY-AC-002 | Đủ điều kiện, vừa trúng đòn 3s trước | Bấm `N` | `IN_COMBAT` |
| FLY-AC-003 | Đang `TAKING_OFF` | Trúng đòn | Về `GROUNDED`, không trừ linh lực |
| FLY-AC-004 | `TC1`, `maxMp` 377, linh lực đầy | Bay liên tục | Tự hạ cánh sau ≈ 92s (±1s) — ví dụ §6 |
| FLY-AC-005 | Đang bay | Đi qua đá thấp / hàng rào (`fly = false`) | Bay qua |
| FLY-AC-006 | Đang bay | Gặp vách núi (`fly = true`) / biên bản đồ | Bị chặn, trượt dọc (collide-and-slide) |
| FLY-AC-007 | Đang bay | Tới vùng an toàn quanh truyền tống trận | Tự đáp ở mép vùng (`NO_FLY_ZONE`) |
| FLY-AC-008 | Đang bay | Bấm tấn công / kỹ năng / `X` | Bỏ qua (không gửi hoặc server từ chối) |
| FLY-AC-009 | Đang bay qua bầy sói thường | Quái phát hiện | Không nhắm, không đuổi |
| FLY-AC-010 | Bản đồ có PvP, người A bắn `PROJECTILE` trúng người B đang bay | Kết quả | B `FALLING`, nhận sát thương ×0,5 (PvP), không sát thương rơi, vào chiến đấu 5s |
| FLY-AC-011 | Cận chiến `CONE` vào người đang bay | Kết quả | Không trúng (FLY-R-051) |
| FLY-AC-012 | Hết linh lực giữa hồ | Hạ cánh | Lướt tới điểm đi được gần nhất ≤ 25m, không kẹt trong nước |
| FLY-AC-013 | Chạm đoạn biên khi bay | Chuyển bản đồ | Đáp trước, xuất hiện ở bản đồ đích trên mặt đất |
| FLY-AC-014 | Mất kết nối khi bay rồi vào lại | Vị trí | Ở `landingTarget`, trạng thái `GROUNDED` |
| FLY-AC-015 | Bật ngoại hình `COS_FLIGHT_HAC` | So tốc độ / linh lực | Giống hệt phi kiếm mặc định |
| FLY-AC-016 | Bấm `N` trong hiểm địa / thành trấn | Kết quả | `NO_FLY_ZONE` |
| FLY-AC-017 | Bay vào fog chưa mở | Mở fog | Bán kính 36m |
| FLY-AC-018 | Bản đồ có vùng bay được không có điểm đi được trong 25m | Validator | Fail FLY-V-02 |

---

## 16. Phạm vi

| Hạng mục | Phase 1–3 | Alpha | Beta 2 | Sau launch |
|----------|:---:|:---:|:---:|:---:|
| Cờ `fly` + vùng `NO_FLY` trong định dạng bản đồ (TD-13), validator | ✅ | | | |
| Ngự kiếm: cất / hạ cánh, tốc độ, linh lực, va chạm `fly`, điểm đáp | | | ✅ | |
| Quái `anti_air`, bắn rơi PvP, `QC_SWORD_FLIGHT`, camera 30m | | | ✅ | |
| Ngoại hình `COS_FLIGHT_*` | | | ✅ | |
| Bay qua biên giữ trạng thái bay, cơ duyên trên mây, chiến đấu trên không, thú cưỡi bay | | | | ✅ |

---

## 17. Câu hỏi mở
- [ ] **FLY-Q-1:** Có cho bay **qua biên bản đồ giữ trạng thái bay** (mượt hơn) ở bản sau không? Cần TD-12 hỗ trợ chuyển trạng thái bay qua snapshot.
- [ ] **FLY-Q-2:** Nội dung "trên mây" (cơ duyên, bí mật chỉ thấy khi bay, hạc tiên) có đáng làm sau launch không?
- [ ] **FLY-Q-3:** Tốc độ 14 m/s có làm bản đồ 250m (nếu Q15 chọn 250m) quá nhỏ khi bay? Cân nhắc giảm còn 11–12 m/s nếu Q15 = 250m.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến (bay là chế độ di chuyển 2D, không tương tác / chiến đấu, không cất cánh khi chiến đấu), máy trạng thái 5 trạng thái, mở khóa Trúc Cơ + `QC_SWORD_FLIGHT`, phím `N`, tốc độ theo cảnh giới, linh lực, cờ va chạm `fly` + vùng `NO_FLY`, điểm đáp, bị bắn rơi (quái `anti_air`, PvP tầm xa), chuyển bản đồ phải đáp, fog ×1,5, ngoại hình cosmetic, công thức + ví dụ, gamedata + validator, message, nghiệm thu |
| v0.2 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
| v0.3 | 2026-10-25 | — | Thống nhất tên cờ va chạm `fly` |

