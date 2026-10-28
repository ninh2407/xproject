# TD-12 — World & Map Architecture

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-12 · Prefix ID: `WM` |
| Owner | Server Dev (Game Server) + Tech Lead |
| Trạng thái | 🟡 Bản nháp v0.4 |
| Cần trước | Phase 0 (spike: 2 bản đồ + chuyển bản đồ), Phase 2 (kênh, instance, World Clock) |
| Quyết định nền | D-02 (open world chia bản đồ, chuyển < 2s, kênh 100–150 người), D-14 (fog giữ qua kiếp), D-19 (Colyseus), [TD-01](00-tdd.md) §2, §13, §14 |
| Liên quan | [GD-21 Open World](../design/22-open-world.md) · [TD-13 Map Format](12-map-editor.md) · [TD-02 Network](01-network.md) · [TD-14 Geometry](13-geom2d.md) · [TD-03 Database](02-database.md) · [GD-28 Hiểm địa](../design/27-danger-zones.md) · [GD-30 Động phủ](../design/28-caves-sect-sites.md) · [GD-22 Cơ duyên](../design/23-encounters.md) · [GD-04 Tu luyện](../design/10-cultivation.md) (thiên kiếp) · [GD-26](../design/03-lifecycle.md) (Tàn Hồn Cảnh) · [GD-32 PvP](../design/31-pvp-infamy.md) |

> GD-21 quy định **luật chơi** của thế giới (nối bản đồ, kênh, fog, ngày đêm, thời tiết). Tài liệu này quy định **cách hệ thống chạy**: bản đồ nào là room nào, room đặt ở tiến trình nào, chọn kênh, chuyển bản đồ, instance, đồng hồ thế giới, fog, khởi động lại / triển khai.

---

## 1. Thành phần

```
                 ┌──────────────── API Service ────────────────┐
                 │ module world:                                │
Client ──REST──► │  • World Directory (chọn room, cấp vé)      │──► PostgreSQL (fog, POI, snapshot)
                 │  • Transfer Coordinator (chuyển bản đồ)     │
                 │  • World Clock & Weather (1 leader)         │──► Valkey pub/sub world:*
                 └───────────────▲─────────────────────────────┘
                                 │ REST nội bộ + Valkey (wd:*, ctx:*)
     ┌───────────────────────────┴──────────────────────────────┐
     │ Game Server (N tiến trình Colyseus)                        │
     │  OpenMapRoom  (HUB, F)        — {mapId}#{channel}          │
     │  InstanceRoom (BC, HD, KN, DP, TD, TRIAL)                  │
     └────────────────────────────────────────────────────────────┘
```

| Thành phần | Chạy ở | Trách nhiệm |
|-----------|--------|-------------|
| **World Directory** | API (`world`) + Valkey `wd:*` | Danh sách room đang chạy, số người, kênh; chọn room khi vào / chuyển; cấp vé vào room |
| **Transfer Coordinator** | API (`world`) | Máy trạng thái chuyển bản đồ (§5), idempotent theo `transferId` |
| **World Clock & Weather** | API, **1 leader** (khóa Valkey `world:leader`) | Canh giờ, thời tiết từng vực — tất định từ epoch + seed (§7) |
| **OpenMapRoom** | Game Server | Một kênh của bản đồ mở; mô phỏng, AOI, fog, spawn |
| **InstanceRoom** | Game Server | Bản đồ riêng: bí cảnh, hiểm địa, động phủ cổ, động phủ cá nhân, trú địa, phòng thử thách (Tàn Hồn Cảnh, thủ vệ động phủ, thiên kiếp, Diễn Pháp Tràng) |

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="wm-r-001"></a>**WM-R-001** | Một nhân vật ở **đúng một room** tại một thời điểm (khóa `charlock`, TD-01 TA-R-120). Không bao giờ "lơ lửng" giữa hai bản đồ (GD-21 §3.3). |
| <a id="wm-r-002"></a>**WM-R-002** | Trạng thái bền của nhân vật **luôn ở API** trước khi rời room (snapshot, TD-01 TA-R-110). Room đích chỉ nạp từ API, không nhận trạng thái trực tiếp từ room nguồn. |
| <a id="wm-r-003"></a>**WM-R-003** | Canh giờ và thời tiết chỉ do World Clock tính; room không tự tính (GD-21 §7). |

---

## 2. Loại room

| `MapType` (GD-21) | Room | Khóa room | Sức chứa | Tạo / đóng |
|-------------------|------|-----------|----------|------------|
| `HUB`, `F` | `OpenMapRoom` | `{mapId}#{channel}` (vd. `V1_F01#3`) | `channel.capacity` (150, ghi đè theo bản đồ) | Kênh 1 luôn chạy; kênh khác theo §4 |
| `BC` (bí cảnh) | `InstanceRoom` | `{mapId}~{instanceId}` | 1–5 (tổ đội) | Tạo khi tổ vào; đóng khi trống 60s hoặc xong |
| `HD` (hiểm địa) | `InstanceRoom` | `{mapId}~{runId}` | theo GD-28 (`SOLO`/`PARTY`/`SHARED` ≤ 30) | Theo vòng đời lượt GD-28 |
| `KN` (động phủ cổ, cơ duyên) | `InstanceRoom` | `{mapId}~{sessionId}` | 1 (hoặc tổ theo GD-22) | Theo phiên cơ duyên |
| `DP` (động phủ cá nhân) | `InstanceRoom` | `CAVE~{caveId}` | chủ + khách (GD-30) | Tạo khi có người vào; đóng khi trống 120s |
| `TD` (trú địa tông môn) | `InstanceRoom` | `SECT~{sectId}` | thành viên + khách | Như `DP` |
| `TRIAL` (thử thách 1 người) | `InstanceRoom` | `TRIAL~{kind}~{id}` | 1 (thiên kiếp cũng 1 — GD-04 CU-R-073 không nhận hỗ trợ) | Tàn Hồn Cảnh (GD-26), thủ vệ động phủ (GD-30), thiên kiếp (GD-04), Diễn Pháp Tràng (GD-08); đóng khi kết thúc |

- <a id="wm-r-010"></a>**WM-R-010** Mọi room có metadata Colyseus `{ mapId, mapType, channel?, ownerKey?, players, capacity, processId, gamedataVersion }`, được đồng bộ sang Valkey `wd:{roomId}` mỗi 5s và khi số người đổi (World Directory đọc từ đây).
- <a id="wm-r-011"></a>**WM-R-011** Room nạp dữ liệu bản đồ từ `bundle.server` (TD-07, TD-13): va chạm, POI, spawn, vùng an toàn, cờ PvP; không đọc file lẻ lúc chạy.

---

## 3. Đặt room lên tiến trình
- <a id="wm-r-020"></a>**WM-R-020** Khi cần room mới, World Directory chọn tiến trình Game Server theo **điểm tải** = `0,6 × (tickP99 / 25ms) + 0,4 × (players / softCapPlayersPerProcess)`, thấp nhất trước; bỏ qua tiến trình đang **drain** (§9) hoặc tick p99 > 35ms. `softCapPlayersPerProcess` = 400, trần cứng 600 (TD-11, hiệu chỉnh sau load test).
- <a id="wm-r-021"></a>**WM-R-021** Tạo room qua `matchMaker.createRoom` của Colyseus nhắm tiến trình đã chọn (Colyseus Redis driver / presence (dùng với Valkey)). Nếu Colyseus không hỗ trợ nhắm tiến trình ở bản đang dùng → để Colyseus tự phân phối và World Directory chỉ chọn **room có sẵn** (WM-Q-1).
- <a id="wm-r-022"></a>**WM-R-022** Bản đồ thường đông (`HUB`) có thể **ghim** kênh 1 vào tiến trình riêng (cấu hình triển khai) để không bị bản đồ dã ngoại làm chậm.

---

## 4. Kênh

- <a id="wm-r-030"></a>**WM-R-030** **Chọn kênh khi vào bản đồ mở** (GD-21 §3.3), trong các kênh chưa đầy:
  1. kênh có thành viên tổ đội;
  2. kênh có bạn bè / đồng môn tông tự lập;
  3. kênh có tỷ lệ đầy trong `[0,4; 0,8]`, chọn kênh đầy nhất trong khoảng;
  4. kênh ít người nhất.
  Mọi kênh ≥ 0,8 → **mở kênh mới** (số kênh nhỏ nhất còn trống).
- <a id="wm-r-031"></a>**WM-R-031** **Gộp kênh:** kênh (≠ 1) có < 10% sức chứa liên tục 10 phút và có kênh khác đủ chỗ → thông báo 60s *"Linh khí kênh này suy yếu, sắp hợp nhất"* rồi chuyển từng người sang kênh đích bằng luồng chuyển §5 (`reason = CHANNEL_MERGE`, bỏ qua khóa chiến đấu — người đang đánh được hoãn tới khi hết chiến đấu tối đa 60s), sau đó đóng kênh.
- <a id="wm-r-032"></a>**WM-R-032** **Đổi kênh thủ công:** hồi chiêu 60s, không khi đang chiến đấu (GD-21); dùng luồng chuyển §5 với `arrival = SAME_POSITION`. Sau đổi kênh: `ST_QI_UNSETTLED` 120s (GD-21 OW-R-105).
- <a id="wm-r-033"></a>**WM-R-033** Trạng thái tài nguyên chung theo **kênh** (linh thảo hiếm, Linh chủng, tinh anh): mỗi kênh độc lập. Riêng thông báo dị tượng (GD-21 OW-R-106) gửi mọi kênh của bản đồ.

---

## 5. Chuyển bản đồ (handoff)

### 5.1. Máy trạng thái
```
REQUESTED ─validate─► SOURCE_SAVING ─snapshot ok─► TICKET_ISSUED ─client vào room đích─► COMPLETED
    │ fail                  │ API lỗi / timeout 5s          │ hết TTL 60s / client không tới
    ▼                       ▼                               ▼
 REJECTED              ABORTED (ở lại room nguồn)      EXPIRED → nhân vật về vùng đệm an toàn của bản đồ nguồn
```

| Bước | Ai | Việc |
|------|----|------|
| 1 | Room nguồn | Phát hiện kích hoạt: chạm đoạn biên / tương tác cổng / truyền tống trận xong vận pháp / cửa instance / kết quả cơ duyên / chết → hồi sinh / gộp kênh. Gọi `POST /internal/v1/world/transfer` `{ transferId, characterId, from, trigger, target }` |
| 2 | API | **Kiểm tra** (WM-R-041), chọn room đích (WM-R-030 hoặc tạo instance), tính điểm xuất hiện (WM-R-042) |
| 3 | Room nguồn | Gửi snapshot cuối (vị trí đích ghi luôn vào snapshot), đánh dấu nhân vật `TRANSFERRING` (bất động, bất tử), nhận `{ joinTicket, endpoint, roomId, arrival }` |
| 4 | Client | Nhận `S2C_TransferTicket` (TD-02 NW-R-074), hiện màn mây thủy mặc, rời room nguồn, `joinById` room đích (bản đồ thường đã tải trước, WM-R-050) |
| 5 | Room đích | `onAuth` kiểm tra vé (TD-01 §14) → `session-load` từ API → đặt ở `arrival` → bất tử `transfer.spawnInvulnSec` (2s; 10s với PvP, GD-32) → `charlock` chuyển sang room đích → `COMPLETED` |

- <a id="wm-r-040"></a>**WM-R-040** `transferId` là khóa idempotency (TD-01 §7): gọi lại trả cùng vé. Mỗi nhân vật chỉ một chuyển đang mở; yêu cầu mới khi đang mở → trả chuyển cũ.
- <a id="wm-r-041"></a>**WM-R-041** **Kiểm tra** trước khi cấp vé: `ALIVE` (trừ hồi sinh); không chiến đấu 5s (`transfer.combatLockSec`, trừ `CHANNEL_MERGE`, hồi sinh, thoát hiểm địa); hồi chiêu cùng biên 3s; điều kiện cổng (cảnh giới cổng vực, xác nhận hiểm địa, quyền vào động phủ / trú địa); ác nhân không dùng truyền tống trận (GD-32 PK-R-033); room đích tồn tại hoặc tạo được.
- <a id="wm-r-042"></a>**WM-R-042** **Điểm xuất hiện** theo loại kết nối (dữ liệu TD-13):

| Kích hoạt | Điểm xuất hiện |
|-----------|----------------|
| Biên (seam) | Ánh xạ tỷ lệ dọc đoạn biên: `t = vị trí trên đoạn nguồn / độ dài` → điểm `t` trên đoạn đích, lùi vào trong 3m, giữ hướng chạy |
| Cổng / cổng vực | `arrival` của cổng đích |
| Truyền tống trận | Điểm quanh POI trận đích (vòng 2–4m, tránh chồng) |
| Cửa instance (vào) | `entrySpawn` của instance |
| Rời instance | `returnPoint` lưu lúc vào (bản đồ + vị trí cửa) |
| Hồi sinh (trọng thương) | Điểm hồi sinh gần nhất theo GD-26 / GD-32 (ác nhân: không phải thành trấn) |
| Đăng nhập | Snapshot cuối; nếu bản đồ snapshot là instance đã đóng → `returnPoint`, không có → điểm hồi sinh của vực |

- <a id="wm-r-043"></a>**WM-R-043** **Mất kết nối giữa chừng:** vé giữ 60s; quá hạn → `EXPIRED`: lần đăng nhập sau nhân vật ở vùng đệm an toàn bản đồ nguồn (GD-21). Đang ở hiểm địa → theo GD-28 (quy tức).
- <a id="wm-r-044"></a>**WM-R-044** Mục tiêu hiệu năng: từ kích hoạt tới điều khiển được ở room đích **p95 < 2s** khi bản đồ đích đã tải trước; đo `transfer_ms` theo từng bước.

### 5.2. Tải trước
- <a id="wm-r-050"></a>**WM-R-050** Room nguồn gửi `S2C_PreloadHint { mapId, assetManifestUrl, logicVersion }` khi người chơi tới gần đoạn biên / cổng trong `transfer.preloadDistance` (60m) hoặc khi bắt đầu vận pháp truyền tống; client tải nền gói bản đồ (TD-13 §6) vào bộ nhớ đệm IndexedDB. Instance và truyền tống trận tới bản đồ chưa tải: màn chuyển kéo dài tới khi tải xong (hiện tiến độ), vé gia hạn tối đa 120s.

---

## 6. Instance

- <a id="wm-r-060"></a>**WM-R-060** **Tạo:** API tạo bản ghi nghiệp vụ trước (vd. `danger_run`, `encounter_session`, phiên thử thách), rồi World Directory tạo `InstanceRoom` với `ownerKey` và danh sách người được vào; vé chỉ cấp cho người trong danh sách.
- <a id="wm-r-061"></a>**WM-R-061** **Đóng:** trống người quá thời hạn theo loại (§2) hoặc nghiệp vụ kết thúc (hết lượt hiểm địa, kết thúc thử thách). Trước khi đóng: báo API kết quả cuối (idempotent), chuyển người còn lại ra `returnPoint`.
- <a id="wm-r-062"></a>**WM-R-062** **Sập tiến trình chứa instance:** hiểm địa → GD-28 DZ-R-080 (`SERVER_RECOVERY`); bí cảnh / động phủ cổ → phiên hủy, không mất vật phẩm đã nhận (đã ghi API), người chơi về `returnPoint`; Tàn Hồn Cảnh / thủ vệ → tính **thua không phạt** và hoàn phí (GD-26 LC-R-165 + phí giải ấn); thiên kiếp → theo GD-04 (mất kết nối khi vận công).
- <a id="wm-r-063"></a>**WM-R-063** **Phasing động phủ** (GD-30 HM-R-012): mỗi động phủ một room `CAVE~{caveId}`; nhiều chủ dùng chung một cửa động ngoài đồng chỉ là một POI — tương tác cửa → chuyển vào room của động phủ tương ứng.

---

## 7. World Clock & Weather

- <a id="wm-r-070"></a>**WM-R-070** **Leader:** một bản API giữ khóa `world:leader` (Valkey `SET NX PX 15000`, gia hạn 5s). Mất khóa → bản khác tiếp quản; vì kết quả tất định nên không lệch.
- <a id="wm-r-071"></a>**WM-R-071** **Canh giờ:** `gameMinutes = floor((now − worldEpoch) / realMsPerGameMinute)`; `realMinutesPerDay = 120` (GD-21 §4) → 1 canh giờ = 10 phút thực; pha ngày / đêm / giao thời suy ra từ canh giờ.
- <a id="wm-r-072"></a>**WM-R-072** **Thời tiết:** mỗi vực chia thời gian thành các **ô thời tiết** độ dài thay đổi; ô thứ `k` có `duration` và `weather` được rút bằng xoshiro (TD-01 §9) với seed `hash(worldSeed, regionId, k)` theo bảng trọng số `weather.table` (GD-21, `bundle.server`). Mọi bản API tính ra cùng kết quả; dự báo 1–2 ô tới (NPC Khâm Thiên Đài) là tính trước, không phải đoán.
- <a id="wm-r-073"></a>**WM-R-073** Leader phát `PUBLISH world:tick { gameMinutes, phase, weatherByRegion }` mỗi 10s và `world:changed` ngay khi pha / thời tiết đổi; room áp vào `MapState.phase/weather` (TD-02 §4.2). Room khởi động hoặc mất tin > 30s → tự tính lại bằng cùng công thức (dùng chung code TS trong `shared/sim/worldClock`) cho tới khi nhận tin.
- <a id="wm-r-074"></a>**WM-R-074** Sự kiện thời tiết hiếm (Linh vũ, GD-21) và dị tượng gửi `broadcast.requested` qua outbox (TD-01 §10).

---

## 8. Fog, POI, bí mật

- <a id="wm-r-080"></a>**WM-R-080** **Fog:** room giữ bitmap fog (ô 8m, GD-21) của từng người chơi có mặt trong RAM; cập nhật mỗi `fog.serverUpdateSec` (2s) từ vị trí server (bán kính 24m, quét thần thức mở thêm); gửi phần thay đổi cho client (`S2C_FogDelta { mapId, cells: bitmap-diff }`).
- <a id="wm-r-081"></a>**WM-R-081** Ghi fog về API mỗi `fog.persistSec` (60s), khi rời bản đồ và khi đăng xuất: `POST /internal/v1/world/fog { batchId, accountId, mapId, bitmap }`; API **gộp OR** với bitmap trong `map_discovery` (TD-03 §6) — không bao giờ làm mất ô đã mở; tính `discovered_pct`, kích hoạt mốc khám phá (GD-21 §3.5) một lần mỗi tài khoản.
- <a id="wm-r-082"></a>**WM-R-082** POI bền vững (truyền tống trận, di tích) ghi `discovered_poi` qua API khi kích hoạt; bí mật ghi `character_secret.found_at`. Vị trí bí mật mỗi kiếp rút **lúc tạo nhân vật** (API, seed nhân vật lưu server — GD-29 RF-R-051) và nạp trong `session-load`; room chỉ gửi thực thể bí mật cho người đã phát hiện (TD-02 NW-R-042).
- <a id="wm-r-083"></a>**WM-R-083** Hiểm địa: fog riêng mỗi lượt, chỉ trong RAM, không ghi DB (GD-21 §3.5).

---

## 9. Triển khai, drain, phục hồi
- <a id="wm-r-090"></a>**WM-R-090** **Drain tiến trình** (triển khai mới, TD-01 §13): đánh dấu `draining` (World Directory không đặt room mới) → thông báo 5 phút → với **room mở**: tạo room cùng `{mapId}#{channel}` trên tiến trình mới và chuyển từng người theo §5 (`reason = DRAIN`, `arrival = SAME_POSITION`, bỏ khóa chiến đấu, giữ trạng thái chiến đấu qua snapshot) → với **instance**: chờ kết thúc tự nhiên tối đa 30 phút, quá hạn áp WM-R-062 → tắt tiến trình.
- <a id="wm-r-091"></a>**WM-R-091** **Sập tiến trình:** World Directory phát hiện mất heartbeat 10s → xóa `wd:*` của tiến trình; người chơi kết nối lại sẽ được đặt vào room mới theo snapshot (TD-01 §13). Quái, vật phẩm rơi chưa nhặt, linh thảo đang có trên bản đồ: **mất** (sinh lại theo bảng spawn) — chấp nhận.
- <a id="wm-r-092"></a>**WM-R-092** Kênh 1 của mỗi bản đồ mở được World Directory **đảm bảo luôn tồn tại** (tự tạo lại nếu mất).

---

## 10. API & message

| Loại | Tên | Mục đích |
|------|-----|----------|
| Nội bộ | `POST /internal/v1/world/transfer` | §5; trả `{ joinTicket, endpoint, roomId, arrival }` hoặc lỗi `IN_COMBAT` / `COOLDOWN` / `REALM_LOCKED` / `NOT_ALLOWED` |
| Nội bộ | `POST /internal/v1/world/rooms/heartbeat` | Tiến trình báo danh sách room + tải (5s) |
| Nội bộ | `POST /internal/v1/world/fog` | WM-R-081 |
| Nội bộ | `POST /internal/v1/world/instances` | Tạo instance (WM-R-060) |
| Công khai | `POST /v1/play/join` | Vào game (TD-01 §14), dùng §5.1 bước 2 với `trigger = LOGIN` |
| Công khai | `POST /v1/characters/{id}/channel` | `{ channel }` đổi kênh (WM-R-032) |
| Công khai | `GET /v1/world/maps/{mapId}/channels` | Danh sách kênh + độ đông (UI đổi kênh) |
| S2C | `S2C_TransferTicket`, `S2C_PreloadHint`, `S2C_FogDelta`, `S2C_ChannelMergeNotice` | TD-02 danh mục |

---

## 11. Tham số

```jsonc
// shared/gamedata/world/world.json (bổ sung phần vận hành; luật chơi ở GD-21 §4)
{
  "transfer": { "preloadDistance": 60, "combatLockSec": 5, "reenterCooldownSec": 3, "spawnInvulnSec": 2,
                "ticketTtlSec": 60, "ticketTtlLoadingMaxSec": 120, "sourceSaveTimeoutSec": 5, "seamInsetM": 3 },
  "channel": { "capacity": 150, "preferredFill": [0.4, 0.8], "openNewAt": 0.8,
               "mergeBelow": 0.10, "mergeAfterMin": 10, "mergeNoticeSec": 60, "manualSwitchCooldownSec": 60 },
  "instance": { "emptyCloseSec": { "BC": 60, "KN": 60, "DP": 120, "TD": 120, "TRIAL": 0 }, "drainWaitMaxMin": 30 },
  "placement": { "softCapPlayersPerProcess": 400, "hardCapPlayersPerProcess": 600, "maxTickP99Ms": 35, "weightTick": 0.6, "weightPlayers": 0.4 },
  "clock": { "realMinutesPerDay": 120, "tickBroadcastSec": 10, "selfComputeAfterSilenceSec": 30 },
  "fog": { "cellSize": 8, "revealRadius": 24, "serverUpdateSec": 2, "persistSec": 60 },
  "directory": { "roomHeartbeatSec": 5, "processDeadAfterSec": 10 }
}
```

---

## 12. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="wm-ac-001"></a>WM-AC-001 | Bản đồ đích đã tải trước | Chạy qua biên F01 → HUB | Điều khiển được ở HUB p95 < 2s; vị trí ánh xạ đúng tỷ lệ dọc biên, lùi vào 3m |
| WM-AC-002 | Vừa trúng đòn 3s trước | Chạm biên | `IN_COMBAT`, ở lại |
| WM-AC-003 | Gửi `transfer` hai lần cùng `transferId` | API | Cùng vé |
| WM-AC-004 | Nhận vé rồi đóng tab 90s | Đăng nhập lại | Ở vùng đệm an toàn bản đồ nguồn |
| WM-AC-005 | Kênh 1, 2 đều ≥ 80% | Người mới vào bản đồ | Mở kênh 3 |
| WM-AC-006 | Bạn tổ đội ở kênh 2 (60%), kênh 1 70% | Vào bản đồ | Kênh 2 |
| WM-AC-007 | Kênh 4 có 10 người liên tục 10 phút | Gộp kênh | Thông báo 60s, mọi người sang kênh khác, kênh 4 đóng; người đang chiến đấu được hoãn tối đa 60s |
| WM-AC-008 | Hai bản API cùng tính thời tiết V1 ô thứ k | So sánh | Giống hệt; leader đổi giữa chừng không làm đổi thời tiết |
| WM-AC-009 | Room mất tin `world:tick` 40s | Pha ngày đổi | Room tự tính đúng pha |
| WM-AC-010 | Fog ô A mở ở kênh 1, ô B ở kênh 2 (hai lần ghi) | API gộp | Bitmap có cả A và B |
| WM-AC-011 | Drain tiến trình chứa `V1_F02#1` có 80 người | Triển khai | Mọi người sang room mới cùng kênh, cùng vị trí, không mất vật phẩm / trạng thái chiến đấu |
| WM-AC-012 | Tiến trình chứa Tàn Hồn Cảnh sập | Sau đó | Thử thách tính thua không phạt, hoàn phí giải ấn |
| WM-AC-013 | Ác nhân `RED` dùng truyền tống trận | Kích hoạt | Từ chối `NOT_ALLOWED` |
| WM-AC-014 | Người lạ (không trong danh sách) cố vào `CAVE~{id}` riêng tư | Xin vé | Từ chối (GD-30 `visitPolicy`) |

---

## 13. Câu hỏi mở
- [ ] **WM-Q-1:** Colyseus (bản dùng khi fork / dựng) có cho tạo room trên tiến trình chỉ định không? Nếu không, chấp nhận phân phối mặc định của Colyseus + World Directory chỉ chọn room có sẵn. Chốt ở spike Phase 0.
- [ ] **WM-Q-2:** Gộp kênh có nên tắt hẳn vào giờ cao điểm (tránh chuyển người chơi đang tranh đoạt) không?
- [ ] **WM-Q-3:** Cần "kênh tổ đội" riêng (instance mini cho tổ đông người ở bản đồ mở) không, hay đủ với ưu tiên kênh?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-23 | — | Bản nháp đầu: thành phần, loại room + khóa room, đặt room theo tải, chọn / mở / gộp / đổi kênh, máy trạng thái chuyển bản đồ + điểm xuất hiện + tải trước, instance (tạo, đóng, sập), phasing động phủ, World Clock & Weather tất định + leader, fog gộp OR, bí mật, drain / phục hồi, API, tham số, nghiệm thu |
| v0.2 | 2026-10-25 | — | D-24: Valkey thay Redis (tên khóa / lệnh không đổi) |
| v0.3 | 2026-10-25 | — | Sức chứa tiến trình 400 mềm / 600 cứng (TD-11) |
| v0.4 | 2026-10-25 | — | Thiên kiếp sức chứa 1 theo GD-04 CU-R-073 (bỏ "hộ pháp") |
