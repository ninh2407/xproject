# TD-02 — Network Protocol

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-02 · Prefix ID: `NW` |
| Owner | Tech Lead (Client) + Server Dev (Game Server) |
| Trạng thái | 🟡 Bản nháp v0.7 |
| Cần trước | Phase 0 (spike netcode: 2 client di chuyển + bắn skillshot, lag 150ms) |
| Quyết định nền | D-19 (Colyseus, `@colyseus/schema`), D-10 (WASD mặc định), [TD-01](00-tdd.md) §2, §3, §14 |
| Liên quan | [TD-01 TDD](00-tdd.md) · [TD-14 Geometry 2D](13-geom2d.md) · [TD-07 Gamedata](06-gamedata.md) · TD-12 World & Map · TD-08 Bảo mật · [GD-06 Chỉ số](../design/12-stats-formulas.md) (SF-R-103) · [GD-07 Kỹ năng](../design/13-skills.md) (§4, §5.6, §9.3) · [GD-12 Quái](../design/18-enemies-ai.md) · [GD-21 Open World](../design/22-open-world.md) · [GD-28 Hiểm địa](../design/27-danger-zones.md) · [GD-14 Xã hội](../design/20-social.md) · mục "Message thời gian thực" của mọi GD |

> Phạm vi: kênh **Client ↔ Game Server** (WebSocket qua Colyseus). Kênh REST (Client ↔ API, Game Server ↔ API) theo TD-01 §6. Tài liệu này quy định: tick, đồng bộ trạng thái, input, dự đoán phía client, bù trễ, AOI, định dạng & danh mục message, kết nối lại, ngân sách băng thông.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-02 | Mục |
|--------------|-----------------|-----|
| GD-07 SK-R-072, GD-06 SF-R-103 | Cách tua lại (rewind) và phán định phòng thủ | §7 |
| Mọi GD có "Message thời gian thực" | Quy ước tên, envelope, kiểm tra, giới hạn tần suất; danh mục chung | §8, §9 |
| GD-21, GD-12 | AOI, lọc thực thể ẩn | §6 |
| GD-28, GD-26 | Mất kết nối, kết nối lại | §10 |
| GD-14 | Đường đi của chat (chốt TD-01 TA-Q-5) | §11 |
| TD-05 / TD-06 | Vòng lặp client / server | §3–§5 |

---

## 1. Mục tiêu

- Combat hành động **cảm giác tức thì** với ping ≤ 150ms (di chuyển dự đoán, kỹ năng phản hồi ngay), vẫn **server-authoritative** (TD-01 TA-R-002).
- **Công bằng cho người phòng thủ** trong PvE: né / đỡ đúng lúc trên màn hình thì được tính (trong giới hạn).
- 150 người / room trên một tiến trình Node.js (TD-01), băng thông thấp hợp với web.
- Một định dạng message nhất quán, có kiểu, sinh từ `shared/schema` để AI và người viết code không lệch.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="nw-r-001"></a>**NW-R-001** | Client chỉ gửi **ý định** (hướng di chuyển, hướng nhắm, lệnh). Không gửi vị trí, sát thương, kết quả. Server bỏ qua mọi trường không có trong schema message. |
| <a id="nw-r-002"></a>**NW-R-002** | **Một kết nối WebSocket** mỗi client tới Game Server (room hiện tại). Chat, thông báo, cập nhật từ API đều đi qua kết nối này (§11). |
| <a id="nw-r-003"></a>**NW-R-003** | Thời gian chuẩn là **thời gian server** tính bằng số tick (`tick`, 50ms) và `serverTimeMs`. `clientTimeMs` chỉ dùng ước lượng trễ, không bao giờ dùng để quyết định kết quả trực tiếp. |
| <a id="nw-r-004"></a>**NW-R-004** | Mô phỏng (di chuyển, va chạm, hitbox) ở client và server dùng **cùng code** `shared/sim` + TD-14, với cùng tham số gamedata. |

---

## 2. Tổng quan

```
CLIENT (60+ FPS render)                                 GAME SERVER (room = MapInstance, 20Hz)
 ┌───────────────────────────────┐   C2S_Input (20Hz)    ┌───────────────────────────────────┐
 │ input WASD + chuột            │ ─────────────────────►│ hàng đợi input theo người chơi     │
 │ dự đoán di chuyển bản thân    │   C2S_* lệnh (tức thì)│ tick: áp input → shared/sim       │
 │ (shared/sim)                  │ ─────────────────────►│   → AI → kỹ năng / va chạm (TD-14) │
 │ đối chiếu (reconcile) khi     │                        │   → lưu lịch sử vị trí (rewind)   │
 │ nhận ack                      │◄───────────────────── │ state patch (schema delta, 20Hz)  │
 │ nội suy thực thể khác (100ms) │   state + S2C_* sự kiện│ + S2C_* sự kiện (ngay trong tick) │
 └───────────────────────────────┘                        └───────────────────────────────────┘
```

---

## 3. Tick & thời gian

- <a id="nw-r-010"></a>**NW-R-010** Room mô phỏng ở **20Hz** (`tickMs = 50`), bước cố định (fixed timestep). `patchRate` Colyseus = 50ms (gửi delta mỗi tick). Thứ tự trong một tick: Hiện thực và ngân sách từng bước: [TD-06 §4](05-game-server.md). Hiện thực và ngân sách từng bước: [TD-06 §4](05-game-server.md).
  1. Nhận input / lệnh đã đến (theo thứ tự nhận) → xác thực (§8.3).
  2. Áp di chuyển cho từng người chơi theo input (`shared/sim/movement`).
  3. Lệnh hành động (đánh, kỹ năng, né, đỡ) → máy trạng thái GD-07 §4.
  4. AI quái (GD-12) → lệnh hành động của quái.
  5. Cập nhật đạn, vùng, status, lượt trúng đến hạn (GD-07 SK-R-072..074) dùng rewind (§7).
  6. Ghi lịch sử vị trí tick này (§7.1); cập nhật AOI (§6); ghi `CharacterContext` nếu đổi (TD-01 §11).
  7. Đẩy sự kiện `S2C_*` phát sinh; schema patch được gửi ở cuối tick.
- <a id="nw-r-011"></a>**NW-R-011** Tick chậm (> 50ms) không được "đuổi" quá 2 tick một lần; trễ hơn thì bỏ bước và ghi metric (TD-01 TA-R-132). Ngân sách: p99 < 25ms.
- <a id="nw-r-012"></a>**NW-R-012** **Đồng bộ đồng hồ:** client gửi `C2S_Ping { clientTimeMs }` mỗi 2s; server trả `S2C_Ping { clientTimeMs, serverTimeMs, tick }`. Client giữ `RTT` (trung bình trượt 8 mẫu, bỏ mẫu lệch > 2× trung vị) và `serverOffset`. Server cũng giữ `rttMs` mỗi kết nối (trung bình trượt) để dùng ở §7.

---

## 4. Đồng bộ trạng thái (server → client)

### 4.1. Schema và sự kiện
- <a id="nw-r-020"></a>**NW-R-020** Có hai cơ chế, chọn theo bản chất dữ liệu:

| Cơ chế | Dùng cho | Ví dụ |
|--------|----------|-------|
| **State** (`@colyseus/schema`, delta mỗi tick, lọc theo AOI) | Trạng thái **liên tục** cần cho người mới vào tầm nhìn | Vị trí, hướng, HP%, trạng thái hành động, status hiển thị, tên, cảnh giới |
| **Message** (`S2C_*`, gửi một lần) | **Sự kiện** tức thời hoặc dữ liệu riêng tư | `S2C_HitResults`, `S2C_SkillCast`, `S2C_ProjectileSpawn`, `S2C_InventoryDelta`, `S2C_LootDrop` |

  Quy tắc: nếu người vào tầm nhìn muộn **vẫn cần thấy** → State; nếu bỏ lỡ là xong → Message. Dữ liệu **riêng của một người** (túi đồ, tiền, tu vi, cooldown) không bao giờ đặt trong State chung.

### 4.2. Cấu trúc State
```ts
// shared/schema/map-state.ts (minh họa; @colyseus/schema)
class EntityState extends Schema {
  @type('uint32') id: number;                 // ID runtime trong room (TD-01 §6.4)
  @type('uint8')  kind: number;               // PLAYER | MONSTER | NPC | SUMMON | LOOT | CORPSE | GATE ...
  @type('uint16') x: number;                  // centimet (0–655,35m) — NW-R-022
  @type('uint16') z: number;
  @type('uint8')  facing: number;             // 0–255 ↔ 0–360°
  @type('uint8')  action: number;             // ActionState GD-07 §4 (mã số)
  @type('uint8')  hpPct: number;              // 0–200 (0,5% mỗi bước)
  @type('uint16') actionStartTick: number;    // tick bắt đầu trạng thái (vòng 16 bit)
  @type('string') templateId: string;         // MON_* / NPC_* / ITM_* ... (người chơi: rỗng)
  @type([ 'uint16' ]) statusIcons;            // mã status hiển thị (bảng tra từ gamedata)
}
class PlayerPublic extends Schema {           // phần công khai của người chơi
  @type('string') name; @type('string') realmCode; @type('uint8') weapon;
  @type('string') sectTag; @type('uint8') flags;   // tổ đội, hấp hối, quy tức, an toàn...
  @type('uint8') elementAffinity;             // Element (glossary) của tâm pháp chính, mã số; 0 = NONE (vô hệ / chưa có tâm pháp) — NW-R-025
}
class SelfState extends Schema {              // chỉ gửi cho chính người chơi
  @type('uint32') lastInputSeq: number;       // ack cho dự đoán (§5.2)
  @type('uint32') hp; @type('uint32') maxHp; @type('uint32') mp; @type('uint32') maxMp;
  @type('uint16') stamina; @type('uint8') energy; @type('uint16') poisePct;
}
class MapState extends Schema {
  @type({ map: EntityState }) entities;       // lọc theo AOI cho từng client (NW-R-021)
  @type({ map: PlayerPublic }) players;
  @type('uint8') phase; @type('uint8') weather; // canh giờ, thời tiết (GD-21) — phát cho cả room
}
```
- <a id="nw-r-021"></a>**NW-R-021** Lọc theo người xem dùng **StateView** của Colyseus (bản có hỗ trợ). Nếu spike Phase 0 cho thấy StateView không đáp ứng 150 người / room trong ngân sách CPU, phương án dự phòng: State chỉ giữ dữ liệu toàn room (`phase`, `weather`), thực thể gửi qua message `S2C_Snapshot` tự đóng gói nhị phân theo AOI (TD-02-Q-1). Hợp đồng ở §4.2 (trường, lượng tử hóa) **giữ nguyên** trong cả hai phương án.
- <a id="nw-r-022"></a>**NW-R-022** **Lượng tử hóa:** vị trí `uint16` centimet (bản đồ ≤ 400m, GD-21 D-02); hướng `uint8` (~1,4°); HP `hpPct` cho người khác, số tuyệt đối chỉ trong `SelfState`; thời gian theo tick. Client giải lượng tử trước khi đưa vào `shared/sim`; server **mô phỏng bằng số thực** và chỉ lượng tử hóa khi gửi.
- <a id="nw-r-025"></a>**NW-R-025** **Hệ tâm pháp của người thi triển** (cho bảng màu VFX của kỹ năng `INHERIT`, AR-04 VFX-R-045 / VFX-R-046, GD-07 SK-R-083): gửi **một lần** qua State `PlayerPublic.elementAffinity` (giá trị `elementAffinity` của GD-06 SF-R-060 = `element` của tâm pháp chính, `NONE` nếu vô hệ / không có), **không** thêm trường vào `S2C_SkillCast` (chốt AR-04 VFX-Q-5). Bảng mã số `uint8` ↔ `Element | 'NONE'` sinh tự động từ thứ tự enum trong `shared/schema` (`NONE` = 0, rồi `KIM`, `MOC`, `THUY`, `HOA`, `THO`, `LOI`, `BANG`, `PHONG` theo glossary), ổn định sau phát hành như mã message (NW-R-061). Trường chỉ đổi khi đổi tâm pháp chính (ngoài chiến đấu, GD-07 SK-R-032) → delta State thông thường; quái dùng `MonsterDef.elementAffinity` từ gamedata qua `templateId`, không cần trường này.

### 4.3. Nội suy thực thể khác
- <a id="nw-r-023"></a>**NW-R-023** Client hiển thị thực thể khác **trễ `interpDelayMs = 100`** (2 tick) so với thời gian server ước tính, nội suy tuyến tính giữa hai mẫu; thiếu mẫu tới 250ms thì ngoại suy theo vận tốc cuối, quá nữa thì đứng yên. Hoạt ảnh tấn công bắt đầu từ `actionStartTick` (đã biết trước) để khớp vùng cảnh báo.
- <a id="nw-r-024"></a>**NW-R-024** **Vùng cảnh báo** (telegraph, GD-12 EN-R-070) vẽ theo `S2C_SkillCast.serverStartMs` quy đổi sang thời gian client **không trễ nội suy** (vùng là hình tĩnh, cần đúng thời điểm thật): người chơi thấy đúng thời gian còn lại để né. Thời gian cảnh báo hiển thị = `telegraphSec − RTT/2`; GD-12 đặt mức tối thiểu đã tính dư cho ping 150ms.

---

## 5. Input & dự đoán phía client

### 5.1. Input di chuyển
```ts
interface C2S_Input {                 // gửi mỗi 50ms khi có thay đổi, và tối thiểu mỗi 250ms khi đang giữ phím
  seq: number;                        // tăng dần
  inputs: Array<{                     // tối đa 4 khung (bù gói trễ / mất), cũ → mới
    seq: number; move: [number, number];   // vector hướng đã chuẩn hóa (−1..1), [0,0] = đứng
    aim: [number, number];             // hướng nhắm (đơn vị), dùng cho xoay mặt / ngắm
    walk: boolean;                     // đi chậm (Alt)
  }>;
}
```
- <a id="nw-r-030"></a>**NW-R-030** Server áp **mỗi khung input đúng một tick** theo thứ tự `seq`, bỏ khung đã áp (trùng). Hàng đợi tối đa 6 khung; dư thì bỏ khung cũ (chống tăng tốc). Không có input mới trong 250ms → coi như `move = [0,0]`.
- <a id="nw-r-031"></a>**NW-R-031** Tốc độ do server tính (`moveSpeed` GD-06 × trạng thái GD-07 §4 × status); client không gửi tốc độ. Click-to-move (tùy chọn D-10) chạy **ở client**: client tìm đường (recast) rồi chuyển thành chuỗi `move` như WASD.

### 5.2. Dự đoán & đối chiếu
- <a id="nw-r-032"></a>**NW-R-032** Client mô phỏng ngay di chuyển của bản thân bằng `shared/sim` (cùng va chạm TD-14), lưu bộ đệm `{seq, input, predictedPos}`. Khi nhận `SelfState.lastInputSeq = n` và vị trí server: bỏ các mục ≤ n, đặt vị trí = server, **áp lại** các input > n. Sai lệch sau áp lại: Phía client: [TD-05 §4](04-client.md). Phía client: [TD-05 §4](04-client.md).
  - < 0,05m: bỏ qua;
  - 0,05–1,5m: kéo mượt trong 100ms;
  - > 1,5m (bị đẩy lùi, dịch chuyển): đặt ngay.
- <a id="nw-r-033"></a>**NW-R-033** **Dự đoán hành động** (chỉ hiển thị, không có hiệu lực): khi bấm đánh / kỹ năng / né, client chạy ngay hoạt ảnh + dự đoán hồi chiêu nếu kiểm tra cục bộ (GD-07 SK-R-070 bước 1–5) qua. Nếu nhận `S2C_CastRejected` thì hủy hoạt ảnh, khôi phục hồi chiêu, hiện lý do. **Không** dự đoán sát thương / số trúng: số chỉ hiện khi có `S2C_HitResults`.
- <a id="nw-r-034"></a>**NW-R-034** Né lướt (`DODGING`) được dự đoán như di chuyển (vị trí); đòn của kẻ địch trúng hay không do server phán định theo §7.2.

---

## 6. AOI (vùng quan tâm)

- <a id="nw-r-040"></a>**NW-R-040** Lưới AOI ô **16m**. Thực thể **vào** tầm nhìn khi cách người xem ≤ `aoiEnter = 40m`, **ra** khi > `aoiLeave = 48m` (trễ để không nhấp nháy). Camera 2.5D thấy ~40×30m (PLAN §5.3).
- <a id="nw-r-041"></a>**NW-R-041** Ưu tiên khi quá tải (> 120 thực thể trong tầm): giữ bản thân, tổ đội, kẻ đang đánh mình / bị mình đánh, quái tinh anh / boss, rồi gần nhất; phần còn lại gửi mỗi 2 tick (10Hz).
- <a id="nw-r-042"></a>**NW-R-042** **Thực thể ẩn** (POI `hiddenLevel > 0`, bẫy, rương bí mật — GD-21, GD-28) **không** vào State của người chưa phát hiện (kể cả trong tầm). Phát hiện (mắt / quét thần thức) → thêm vào view của riêng người đó. Dữ liệu đó vốn chỉ có ở server (GD-29 RF-R-041).
- <a id="nw-r-043"></a>**NW-R-043** Thành viên tổ đội ngoài tầm nhìn: vị trí gửi bằng `S2C_PartyPositions` 1Hz (minimap), không qua State.
- <a id="nw-r-044"></a>**NW-R-044** Message phạm vi "AOI" (vd. `S2C_SkillCast`, `S2C_HitResults`, `S2C_ProjectileSpawn`) gửi cho mọi client có **người gây ra hoặc mục tiêu** trong view của họ.

---

## 7. Bù trễ

### 7.1. Lịch sử vị trí
- <a id="nw-r-050"></a>**NW-R-050** Server lưu vị trí + hình va chạm (TD-14) của mọi thực thể chiến đấu cho **20 tick gần nhất** (1s), bộ đệm vòng. Hàm `positionAt(entityId, tickFloat)` nội suy giữa hai tick.

### 7.2. Quy tắc tua lại

| Tình huống | Quy tắc | Giới hạn | Nguồn |
|-----------|---------|----------|-------|
| **Người chơi tấn công quái** (lượt trúng của kỹ năng / đánh thường / đạn khi bắn ra) | Vị trí **quái** được tua lại `rewindMs = min(rttMs/2 + interpDelayMs, maxRewindMs)` khi tính va chạm của lượt trúng **tức thời** (`CONE`, `LINE`, `SELF_CIRCLE`, `DASH`). Đạn bay mô phỏng theo thời gian server, **không** tua (đã có thời gian bay). `GROUND_CIRCLE` có `delaySec` không tua (là cảnh báo cho cả hai phía). | `maxRewindMs = 200` | GD-07 SK-R-072 |
| **Quái tấn công người chơi** | Tính ở thời gian server hiện tại (không tua vị trí người chơi). Đổi lại, **phòng thủ** của người chơi được lùi thời điểm (dòng dưới). | — | |
| **Né / đỡ / đỡ hoàn hảo của người chơi** | Thời điểm hiệu lực của lệnh = `tickNhận − min(rttMs/2, defenseLagCompMs)` (quy ra tick, làm tròn xuống). Lượt trúng đã xử lý ở tick nằm **trong** khoảng lùi đó được **xét lại**: nếu lệnh phòng thủ hợp lệ tại thời điểm đó thì hủy / giảm sát thương đã gây (gửi `S2C_HitResults` sửa với `corrected: true`). | `defenseLagCompMs = 100` | GD-06 SF-R-103 |
| **Người chơi tấn công người chơi** (PvP, GD-32) | Tua lại vị trí mục tiêu tối đa `pvpMaxRewindMs`; phòng thủ của mục tiêu lùi tối đa `defenseLagCompMs` như dòng trên — hai phía cân bằng | `pvpMaxRewindMs = 100` | GD-32 PK-R-062 |
| **Người chơi hỗ trợ đồng minh** (hồi máu vùng) | Như tấn công (tua vị trí đồng minh) | 200ms | |

- <a id="nw-r-051"></a>**NW-R-051** "Xét lại" (dòng 3) chỉ áp dụng cho lượt trúng **không gây chết thật**. Lượt trúng đưa nhân vật vào `DOWNED` / chết được **hoãn xác nhận** `defenseLagCompMs` (2 tick) trước khi chuyển trạng thái, để lệnh phòng thủ đến trễ vẫn kịp tính. Đảm bảo không có cái chết bị "hồi sinh" sau khi đã thông báo.
- <a id="nw-r-052"></a>**NW-R-052** Người chơi có `rttMs > 300` không được bù thêm (dùng trần), và client hiện cảnh báo "Mạng chậm — phán định có thể bất lợi". Ping cao nằm trong death log (TD-01 §12).

### 7.3. Ví dụ
Người chơi RTT 120ms chém `CONE` vào quái đang chạy 4 m/s: `rewindMs = min(60 + 100, 200) = 160ms` → dùng vị trí quái ở 3,2 tick trước (nội suy), tức lùi 0,64m dọc hướng chạy — khớp với vị trí người chơi thấy trên màn hình.
Quái tung đòn đập trúng ở tick T; lệnh né của người chơi (RTT 160ms) đến ở tick T+1: thời điểm hiệu lực = T+1 − min(80, 100)ms → T − 0,6 tick → làm tròn xuống tick T−1 < T: né được tính, lượt trúng được xét lại thành "né" (i-frame).

---

## 8. Message

### 8.1. Quy ước
- <a id="nw-r-060"></a>**NW-R-060** Tên: `C2S_PascalCase` (client → server), `S2C_PascalCase` (server → client). Kiểu payload định nghĩa **một lần** trong `shared/schema/messages/*.ts` (gom theo domain: `combat.ts`, `inventory.ts`, `encounter.ts`…), GD chỉ mô tả trường. Thêm message = thêm vào file này + danh mục §9.
- <a id="nw-r-061"></a>**NW-R-061** Mã hóa: Colyseus message type là **số nguyên** (bảng ánh xạ sinh tự động từ danh mục, ổn định — không đổi số khi đã phát hành), payload msgpack (mặc định của Colyseus). Vị trí trong message dùng mét số thực `[x, z]` làm tròn 2 chữ số (cm), hướng là vector đơn vị làm tròn 3 chữ số.
- <a id="nw-r-062"></a>**NW-R-062** Mọi lệnh hành động C2S có `seq` (tăng dần theo kết nối) và `clientTimeMs`. Phản hồi từ chối luôn mang lại `seq` (vd. `S2C_CastRejected { seq, reason }`).

### 8.2. Envelope sự kiện chung
```ts
interface S2C_Notice { code: string; messageKey: string; params?: Record<string, string | number>; level: 'INFO' | 'WARN' | 'ERROR' }
interface S2C_Error  { seq?: number; code: string; messageKey: string }   // mã theo TD-01 errors.yaml
```

### 8.3. Kiểm tra & giới hạn
- <a id="nw-r-063"></a>**NW-R-063** Server kiểm tra **mọi** payload C2S bằng schema (zod hoặc tương đương sinh từ `shared/schema`): sai kiểu / thừa trường / số NaN / vector chưa chuẩn hóa (|v| > 1,01) → bỏ message, tăng bộ đếm vi phạm.
- <a id="nw-r-064"></a>**NW-R-064** Giới hạn tần suất theo loại (token bucket mỗi kết nối):

| Nhóm | Ví dụ | Giới hạn |
|------|-------|----------|
| Input di chuyển | `C2S_Input` | 25 / s |
| Lệnh chiến đấu | `C2S_BasicAttack`, `C2S_CastSkill`, `C2S_Dodge`, `C2S_Block*`, `C2S_Heavy*` | 20 / s tổng |
| Tương tác | `C2S_NpcInteract`, `C2S_PickupLoot`, `C2S_GatherStart`, `C2S_LootCorpse`, `C2S_DialogueChoose`, `C2S_Encounter*` | 10 / s |
| Chat | `C2S_ChatSend` | theo GD-14 (kênh), tối đa 2 / s |
| Khác | `C2S_Ping`, `C2S_Set*` | 5 / s |

  Vượt giới hạn: bỏ message; vượt liên tục 10s → `S2C_Notice` cảnh báo; 60s → ngắt kết nối + ghi nhật ký chống gian lận (TD-08).
- <a id="nw-r-065"></a>**NW-R-065** Kích thước tối đa một message C2S: 2 KB (chat: 1 KB văn bản).

---

## 9. Danh mục message

Danh mục đầy đủ sinh từ `shared/schema/messages`; bảng dưới gom các message đã định nghĩa trong GD (payload chi tiết ở GD sở hữu) và các message chung mới của TD-02.

| Domain | C2S | S2C | GD |
|--------|-----|-----|----|
| PvP (GD-32) | `C2S_SetPkMode` | `S2C_PkState`, `S2C_PvpKilled`, `S2C_EnforcerWarning`, `S2C_HeavenPunishment` | GD-32 |
| Linh thú (GD-25) | `C2S_BeastSummon`, `C2S_BeastCommand`, `C2S_CaptureStart`, `C2S_CaptureCancel` | `S2C_CaptureChance`, `S2C_CaptureResult`, `S2C_LinhChungSpotted` | GD-25 |
| Chỉ dev | — | `S2C_DebugHit` (TD-14 GEO-R-092) | TD-14 |
| Thế giới (TD-12) | — | `S2C_TransferTicket`, `S2C_PreloadHint`, `S2C_FogDelta`, `S2C_ChannelMergeNotice` | TD-12 |
| Sự kiện thế giới (GD-23) | (theo GD-23 §9) | `S2C_WorldEvent*`, `S2C_EventContribution` | GD-23 |
| Ngự kiếm (GD-31) | `C2S_FlightToggle` | `S2C_FlightState`, `S2C_FlightRejected` (+ bit `FLYING` trong `PlayerPublic.flags`; kiểm tra tốc độ dùng `flySpeed` khi bay) | GD-31 |
| **Chung (TD-02)** | `C2S_Input`, `C2S_Ping` | `S2C_Ping`, `S2C_Welcome`, `S2C_Notice`, `S2C_Error`, `S2C_Kick`, `S2C_TransferTicket`, `S2C_Snapshot` (dự phòng NW-R-021), `S2C_PartyPositions` | TD-02 |
| Chiến đấu | `C2S_BasicAttack`, `C2S_HeavyStart`, `C2S_HeavyRelease`, `C2S_CastSkill`, `C2S_Dodge`, `C2S_BlockStart`, `C2S_BlockEnd`, `C2S_SetLoadout`, `C2S_SetMainTechnique` | `S2C_SkillCast`, `S2C_CastRejected`, `S2C_ProjectileSpawn`, `S2C_CooldownSync`, `S2C_HitResults`, `S2C_StatusUpdate`, `S2C_StatsUpdate`, `S2C_ResourceUpdate`, `S2C_MasteryUp` | GD-06, GD-07 |
| Thực thể / quái | — | `S2C_EntitySpawn`, `S2C_EntityDespawn`, `S2C_EntityState`, `S2C_EliteAnnounce`, `S2C_BossPhase`, `S2C_LootDrop` | GD-12 |
| Vật phẩm | `C2S_PickupLoot`, `C2S_UseQuickSlot`, `C2S_SetQuickSlot`, `C2S_UseArtifact` | `S2C_InventoryDelta`, `S2C_EquipmentChanged`, `S2C_CurrencyUpdate` | GD-09, GD-13 |
| Tu luyện | `C2S_MeditateStart`, `C2S_MeditateStop` | `S2C_MeditationState`, `S2C_CultivationUpdate`, `S2C_StageUp`, `S2C_BreakthroughPhase`, `S2C_BreakthroughResult`, `S2C_TribulationWave`, `S2C_ChannelProgress` | GD-04 |
| Vòng đời | — | `S2C_LifecycleUpdate`, `S2C_SevereInjury`, `S2C_DeathProtected`, `S2C_LifeEnded` | GD-26 |
| Thiên phú | — | `S2C_TalentChanged`, `S2C_TalentRevealed`, `S2C_TalentChoicePending` | GD-27 |
| Chế tác | — | `S2C_CraftState`, `S2C_CraftResult` | GD-10, GD-11 |
| Cơ duyên / hội thoại | `C2S_EncounterEngage`, `C2S_EncounterChoose`, `C2S_DialogueChoose`, `C2S_NpcInteract` | `S2C_EncounterCue`, `S2C_EncounterStep`, `S2C_EncounterOutcome`, `S2C_EncounterCombat`, `S2C_EncounterEnd`, `S2C_DialogueStep`, `S2C_NpcMenu` | GD-22, GD-15 |
| Nhiệm vụ | — | `S2C_QuestProgress`, `S2C_QuestCompleted` | GD-15 |
| Hiểm địa | `C2S_ExtractStart`, `C2S_ExtractCancel`, `C2S_ReviveStart`, `C2S_ReviveCancel`, `C2S_LootCorpse`, `C2S_UseEscapeTalisman` | `S2C_DangerRunState`, `S2C_DangerPhase`, `S2C_Downed`, `S2C_CorpseSpawn`, `S2C_RunResult` | GD-28 |
| Open world | (quét thần thức theo GD-21), `C2S_GatherStart`, `C2S_GatherCancel` | `S2C_ScanAlerted`, `S2C_GatherState`, `S2C_GatherResult`, `S2C_GatherRejected` | GD-21 |
| Xã hội | `C2S_ChatSend` | `S2C_ChatMessage`, `S2C_ChatRejected`, `S2C_PartyInvite`, `S2C_PartyUpdate`, `S2C_FriendNotify` | GD-14 |
| Động phủ / sơn môn | `C2S_EnterCave`, `C2S_HomeTeleport` | `S2C_SectSiteStates` | GD-30 |

```ts
interface S2C_Welcome {               // ngay sau khi vào room
  characterId: string; entityId: number; mapId: string; channel: number;
  tick: number; serverTimeMs: number; tickMs: 50;
  gamedataVersion: string;            // phải khớp client (TD-07 DT-R-091), lệch → client tải lại
  rewind: { maxRewindMs: 200; defenseLagCompMs: 100; interpDelayMs: 100 };
}
interface S2C_Kick { reason: 'DUPLICATE_LOGIN' | 'MAINTENANCE' | 'RATE_LIMIT' | 'GM' | 'VERSION'; messageKey: string }
```

---

## 10. Kết nối, mất kết nối, kết nối lại

- <a id="nw-r-070"></a>**NW-R-070** Vào room theo TD-01 §14 (join ticket dùng một lần). Sau `onJoin`, server gửi `S2C_Welcome` rồi State đầy đủ trong view.
- <a id="nw-r-071"></a>**NW-R-071** **Phát hiện mất kết nối:** WebSocket đóng, hoặc không nhận gói nào trong 5s (client gửi `C2S_Ping` 2s/lần).
- <a id="nw-r-072"></a>**NW-R-072** **Cửa sổ kết nối lại** 60s (`allowReconnection` của Colyseus) ở mọi nơi. Trong cửa sổ, nhân vật:

| Nơi | Hành vi |
|-----|---------|
| Hiểm địa | `DORMANT_GUARD` theo GD-28 §5.7 (quy tức) |
| Vùng thường, đang chiến đấu | Đứng yên tại chỗ, vẫn có thể bị đánh (trọng thương, không chết thật — D-03); sau 10s không kết nối lại → kéo ra khỏi chiến đấu (quái mất mục tiêu) |
| Vùng thường, không chiến đấu / thành trấn | Đứng yên, hiển thị biểu tượng "mất kết nối" |
| Đột phá đang vận công | Theo GD-04 (CU-R mất kết nối) |

  Hết 60s → lưu snapshot (TD-01 TA-R-110), gỡ khỏi room, như đăng xuất.
- <a id="nw-r-073"></a>**NW-R-073** Kết nối lại thành công → server gửi lại State đầy đủ trong view + `S2C_CooldownSync` + `S2C_ResourceUpdate`; client xóa bộ đệm dự đoán, đặt `seq` tiếp theo lớn hơn `lastInputSeq`.
- <a id="nw-r-074"></a>**NW-R-074** **Chuyển bản đồ** không phải mất kết nối và không dùng `S2C_Kick`: Game Server gửi `S2C_TransferTicket { mapId, endpoint, roomId, joinTicket }`, client rời room cũ và vào room mới (TD-12); tải trước bản đồ bắt đầu từ `transfer.preloadDistance` (GD-21).

---

## 11. Chat qua Game Server (chốt TD-01 TA-Q-5)

- <a id="nw-r-080"></a>**NW-R-080** Chat đi qua **kết nối Game Server hiện có** (NW-R-002), không có dịch vụ WebSocket riêng ở MVP:
  1. Client gửi `C2S_ChatSend` → Game Server kiểm tra tần suất, độ dài, bộ lọc (GD-14 SO-R-021, dùng `shared/sim/chatFilter` + danh sách từ cấm trong gamedata server).
  2. Kênh `MAP`: phát trong room. Kênh `WORLD`, `SECT`, `PARTY`, `WHISPER`: `PUBLISH chat:{channel}:{scope}` lên Valkey; mọi tiến trình Game Server đăng ký các kênh liên quan tới người chơi đang chứa và chuyển tiếp `S2C_ChatMessage` (lọc danh sách chặn SO-R-023 ở bước này).
  3. Game Server gom nhật ký chat gửi API `POST /internal/v1/chat/log` mỗi 5s (bảng `chat_message_log`, GD-14), vì chỉ API ghi DB (TD-01 TA-R-001).
- <a id="nw-r-081"></a>**NW-R-081** Liên kết vật phẩm trong chat (SO-R-022): Game Server gọi API lấy ảnh chụp instance, nhúng `itemLinkId` vào tin; tooltip tải qua REST.
- <a id="nw-r-082"></a>**NW-R-082** Thông báo toàn server (`broadcast.requested`, GD-14 SO-R-060) đến từ sự kiện domain (TD-01 §10.3), phát bằng `S2C_Notice` / kênh `SYSTEM`.

---

## 12. Ngân sách băng thông

| Tình huống | Downstream / client | Upstream / client |
|-----------|---------------------|-------------------|
| Dã ngoại, ~20 thực thể trong tầm | ≤ 6 KB/s | ≤ 1,5 KB/s |
| Thành trấn đông, ~100 thực thể | ≤ 20 KB/s | ≤ 1,5 KB/s |
| Boss, 20 người + hiệu ứng | ≤ 25 KB/s (đỉnh 40) | ≤ 2 KB/s |

- <a id="nw-r-090"></a>**NW-R-090** Đo bằng bot trong load test (TD-01 §16). Vượt ngân sách → giảm tần suất thực thể xa (NW-R-041), gộp `S2C_HitResults` theo tick (một message nhiều sự kiện, đã là thiết kế của GD-06).
- <a id="nw-r-091"></a>**NW-R-091** Bật nén permessage-deflate cho WebSocket **chỉ** khi đo thấy lợi (CPU server là tài nguyên khan hơn băng thông); mặc định tắt.

---

## 13. Tham số

```jsonc
// shared/gamedata/world/network.json  (đăng ký domain world, TD-07)
{
  "tickMs": 50, "maxCatchUpTicks": 2,
  "interpDelayMs": 100, "extrapolateMaxMs": 250,
  "maxRewindMs": 200, "pvpMaxRewindMs": 100, "defenseLagCompMs": 100, "highPingMs": 300,
  "historyTicks": 20,
  "inputSendMs": 50, "inputIdleResendMs": 250, "inputRedundancy": 4, "inputQueueMax": 6, "inputTimeoutMs": 250,
  "reconcile": { "ignoreM": 0.05, "smoothMaxM": 1.5, "smoothMs": 100 },
  "aoi": { "cellM": 16, "enterM": 40, "leaveM": 48, "softCapEntities": 120 },
  "pingIntervalMs": 2000, "disconnectSilenceMs": 5000, "reconnectWindowSec": 60, "combatDisengageSec": 10,
  "partyPositionsHz": 1,
  "chatLogFlushMs": 5000
}
```
(Các giá trị trùng với GD-06 `defenseLagCompMs`, GD-07 `maxRewindMs`: file này là **nguồn duy nhất**; GD-06 / GD-07 tham chiếu.)

---

## 14. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="nw-ac-001"></a>NW-AC-001 | 2 client, lag giả lập 150ms ± 20ms, mất gói 2% | Di chuyển 60s | Vị trí dự đoán và server lệch < 0,05m khi đứng yên; không có giật > 1,5m |
| NW-AC-002 | Client gửi `C2S_Input` 40 lần/giây | Đo tốc độ trên server | Không vượt `moveSpeed` (bỏ khung dư) |
| NW-AC-003 | Client gửi trường `pos` trong `C2S_Input` | Server | Bỏ qua trường, không ảnh hưởng vị trí |
| NW-AC-004 | Ví dụ §7.3 (RTT 120ms, quái chạy 4 m/s) | Chém `CONE` trúng trên màn hình | Server tính trúng (rewind 160ms) |
| NW-AC-005 | RTT 400ms | Chém như trên | Rewind giới hạn 200ms; client hiện cảnh báo mạng chậm |
| NW-AC-006 | Ví dụ §7.3 (né đến trễ 1 tick) | Server xử lý | Lượt trúng được xét lại thành né; `S2C_HitResults.corrected = true` |
| NW-AC-007 | Lượt trúng khiến người chơi `DOWNED`, lệnh né đến trong 100ms | Server | Không vào `DOWNED` (NW-R-051) |
| NW-AC-008 | Người chơi chưa phát hiện rương bí mật đứng cạnh nó | Kiểm tra State nhận được | Không có thực thể rương |
| NW-AC-009 | 150 bot trong một room, mỗi bot di chuyển + đánh | Đo | Tick p99 < 25ms; downstream trung bình ≤ 20 KB/s / bot ở thành trấn |
| NW-AC-010 | Mất kết nối 30s trong vùng thường | Kết nối lại | Cùng room, cùng vị trí, State đầy đủ, hồi chiêu đúng |
| NW-AC-011 | Mất kết nối 61s | Kết nối lại | Phải vào lại qua `play/join`; vị trí theo snapshot |
| NW-AC-012 | Gửi chat kênh `WORLD` ở tiến trình A | Người chơi ở tiến trình B | Nhận `S2C_ChatMessage` (qua Valkey); người đã chặn người gửi không nhận |
| NW-AC-013 | `C2S_CastSkill` khi đang hồi chiêu | Server | `S2C_CastRejected { seq, reason: COOLDOWN }`; client hủy hoạt ảnh dự đoán |
| NW-AC-014 | Payload `aim = [NaN, 1]` | Server | Bỏ message, tăng bộ đếm vi phạm |
| NW-AC-015 | Client có `gamedataVersion` khác `S2C_Welcome` | Vào room | Client tải lại bundle trước khi chơi |
| <a id="nw-ac-016"></a>NW-AC-016 | Người chơi A trang bị `TEC_LIET_HOA_KIEM` (HOA), B trong AOI | B nhận State | `PlayerPublic[A].elementAffinity` = mã của `HOA`; A đổi sang `TEC_DAN_KHI` khi đả tọa → B nhận delta `NONE` (0); `S2C_SkillCast` không có trường hệ (NW-R-025) |

---

## 15. Phạm vi theo phase

| Hạng mục | Phase 0 (spike) | Phase 1 | Alpha | Beta |
|----------|:---:|:---:|:---:|:---:|
| Tick 20Hz, `C2S_Input`, dự đoán + đối chiếu, nội suy | ✅ | | | |
| Rewind tấn công, xét lại phòng thủ | ✅ (thô) | ✅ | | |
| AOI + StateView (hoặc dự phòng) | | ✅ | | |
| Kiểm tra schema, giới hạn tần suất | | ✅ | | |
| Kết nối lại 60s, chat qua Valkey | | ✅ | | |
| Load test 150 / room, tối ưu băng thông | | | ✅ | |

---

## 16. Câu hỏi mở
- [ ] **NW-Q-1:** StateView của Colyseus có đủ hiệu năng cho 150 người / room với AOI không, hay dùng `S2C_Snapshot` tự đóng gói (NW-R-021)? Chốt ở spike Phase 0.
- [ ] **NW-Q-2:** Có cần WebTransport / UDP-like cho trình duyệt hỗ trợ (giảm head-of-line blocking khi mất gói) ở sau launch?
- [ ] **NW-Q-3:** `defenseLagCompMs` 100ms có đủ cho người chơi ở xa (ping 150–200ms) mà không làm quái "đánh trúng rồi mới trượt" quá thường xuyên? Đo bằng telemetry `corrected` ở Alpha.
- [ ] **NW-Q-4:** Kênh chat `WORLD` có cần tách sang dịch vụ riêng khi > 5.000 CCU (fan-out Valkey tới mọi tiến trình)?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-23 | — | Bản nháp đầu: tick 20Hz + thứ tự xử lý, đồng bộ đồng hồ, State (schema, lượng tử hóa) và Message, nội suy, `C2S_Input` + dự đoán / đối chiếu, AOI + thực thể ẩn, bù trễ (rewind tấn công, xét lại phòng thủ, hoãn xác nhận hấp hối), quy ước & danh mục message, kiểm tra & giới hạn tần suất, kết nối lại 60s, chat qua Game Server + Redis (chốt TA-Q-5), ngân sách băng thông, tham số, nghiệm thu |
| v0.2 | 2026-10-23 | — | Bù trễ PvP (`pvpMaxRewindMs` 100), message PvP (GD-32) |
| v0.3 | 2026-10-23 | — | Danh mục message thế giới (TD-12) |
| v0.4 | 2026-10-25 | — | Danh mục thêm message linh thú, `S2C_DebugHit`; trỏ TD-06 §4, TD-05 §4 |
| v0.5 | 2026-10-25 | — | D-24: Valkey thay Redis (tên khóa / lệnh không đổi) |
| v0.6 | 2026-10-25 | — | Message sự kiện thế giới, ngự kiếm |
| v0.7 | 2026-10-25 | — | Đợt rà nhất quán (AR-04 VFX-R-046, VFX-Q-5): thêm `PlayerPublic.elementAffinity` (gửi một lần qua State, không qua `S2C_SkillCast`), NW-R-025, NW-AC-016 |

