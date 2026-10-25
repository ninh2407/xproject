# TD-14 — Geometry 2D & Collision

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-14 · Prefix ID: `GEO` |
| Owner | Server Dev (Game Server) + Tech Lead (Client) |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 0 (spike: thư viện va chạm trong `shared/sim`, cùng bộ test ở client và Game Server) |
| Quyết định nền | D-01 (2.5D, logic trên mặt phẳng 2D), D-19 (code mô phỏng viết một lần bằng TS), [TD-01](00-tdd.md) TA-R-007, TA-R-021 |
| Liên quan | [TD-02 Network](01-network.md) (rewind, dự đoán) · [TD-07 Gamedata](06-gamedata.md) · TD-13 Map Format & Editor · TD-12 World & Map · [GD-07 Kỹ năng](../design/13-skills.md) (§3 `Shape`, §5.6–5.7) · [GD-06 Chỉ số](../design/12-stats-formulas.md) (né, đẩy lùi) · [GD-12 Quái](../design/18-enemies-ai.md) (nhận thức, suất cận chiến) · [GD-21 Open World](../design/22-open-world.md) · [GD-28 Hiểm địa](../design/27-danger-zones.md) (bẫy) |

> Mọi logic không gian của gameplay (di chuyển, chướng ngại, hitbox kỹ năng, đạn, tầm nhìn, khoảng cách tương tác) chạy trên **mặt phẳng (x, z)** bằng thư viện `shared/sim/geom2d`, **cùng code** ở client (dự đoán, vẽ vùng cảnh báo, công cụ debug) và Game Server (quyết định). Độ cao `y` chỉ để hiển thị (heightmap). Không dùng engine vật lý (Havok) cho gameplay.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-14 | Mục |
|--------------|-----------------|-----|
| GD-07 SK-R-005, SK-R-080 | Hình học chính xác của từng `Shape`, quy tắc "trúng" | §5, §6 |
| GD-07 SK-R-073, SK-R-082 | Đạn: hình tròn quét, nổ | §6.2 |
| GD-06 | Né lướt, đẩy lùi, va chạm địa hình | §4 |
| GD-12 | Bán kính thân theo cỡ, tầm nhìn (LOS), tách quái, tìm đường | §3, §7, §8 |
| TD-02 | Hàm `positionAt` cho rewind, va chạm cho dự đoán | §4, §6 |
| TD-13 | Lớp va chạm trong dữ liệu bản đồ | §3.3 |

---

## 1. Mục tiêu

- **Một nguồn hình học** cho client và server → dự đoán khớp, vùng cảnh báo vẽ đúng vùng sẽ trúng.
- Quy tắc trúng **trực quan và công bằng**: cái người chơi thấy chạm vào thân là trúng.
- **Tất định** trên mọi trình duyệt và Node.js.
- Đủ nhanh cho 150 người + ~200 quái / room ở 20Hz (TD-02).

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="geo-r-001"></a>**GEO-R-001** | `shared/sim/geom2d` là **thư viện duy nhất** cho va chạm gameplay. Client không dùng Babylon picking / physics để quyết định gameplay (chỉ để chọn đối tượng bằng chuột trong UI). |
| <a id="geo-r-002"></a>**GEO-R-002** | Đơn vị **mét**, số thực **float64** (number của JS). Không lượng tử hóa trong mô phỏng (chỉ khi gửi mạng, TD-02 NW-R-022). |
| <a id="geo-r-003"></a>**GEO-R-003** | Code mô phỏng **không dùng** `Math.sin`, `Math.cos`, `Math.tan`, `Math.atan2`, `Math.exp`, `Math.pow` với số mũ không nguyên, `Math.random` (kết quả có thể khác giữa engine JS). Dùng phép toán vector (`+ − × ÷`, `Math.sqrt`, `Math.abs`, `Math.min/max`, `Math.floor`) — các phép này là IEEE-754 chính xác. Góc từ dữ liệu (vd. `angleDeg`) đổi sang **cos nửa góc** bằng bảng tra sinh lúc build (§2.3). Lint chặn import các hàm cấm trong `shared/sim`. |
| <a id="geo-r-004"></a>**GEO-R-004** | Mục tiêu bị trúng khi **hình tròn thân** của nó **giao** với hình của đòn (không yêu cầu tâm nằm trong hình) (§5.1). |

---

## 2. Hệ tọa độ & kiểu cơ bản

### 2.1. Hệ tọa độ
```
      +z (Bắc)
       ▲
       │        Babylon.js: x phải, y lên, z vào trong màn hình (left-handed).
       │        Gameplay dùng (x, z); y = heightmap(x, z) chỉ khi hiển thị.
       └──────► +x (Đông)
  gốc (0,0) = góc Tây Nam của bản đồ; bản đồ nằm trong [0, width] × [0, depth] (≤ 400m, GD-21)
```
- <a id="geo-r-010"></a>**GEO-R-010** Hướng biểu diễn bằng **vector đơn vị** `[dx, dz]`. Góc chỉ dùng cho hiển thị và lượng tử hóa mạng (`facing` uint8: 0 = +x, tăng ngược chiều kim đồng hồ khi nhìn từ trên xuống, 64 = +z). Chuyển đổi góc ↔ vector ở biên (client render, mã hóa mạng) bằng bảng tra 256 phần tử (§2.3).

### 2.2. Kiểu
```ts
type Vec2 = { x: number; z: number };          // hoặc tuple [x, z] trong gamedata / message
interface Circle  { c: Vec2; r: number }
interface Segment { a: Vec2; b: Vec2 }
interface Capsule { a: Vec2; b: Vec2; r: number }          // hình tròn quét
interface OBB     { c: Vec2; axis: Vec2; halfLen: number; halfWid: number }   // axis = hướng chiều dài (đơn vị)
interface Sector  { apex: Vec2; dir: Vec2; radius: number; cosHalf: number }  // hình quạt
interface ConvexPolygon { verts: Vec2[] }                   // ngược chiều kim đồng hồ, 3–12 đỉnh
```

### 2.3. Bảng tra
- <a id="geo-r-011"></a>**GEO-R-011** `tools/gamedata/generators/gen-trig` sinh `shared/sim/geom2d/trig-table.ts`: 256 vector đơn vị cho `facing` và `cosHalf` cho mọi `angleDeg` xuất hiện trong gamedata (tính bằng `Math.cos` **lúc build**, ghi hằng số 17 chữ số). Gamedata dùng `angleDeg` không có trong bảng → build fail (TD-07 `[GEN]`).

---

## 3. Thực thể & thế giới tĩnh

### 3.1. Thân thực thể
- <a id="geo-r-020"></a>**GEO-R-020** Mọi thực thể chiến đấu có **thân hình tròn** `bodyRadius`:

| Loại | `bodyRadius` (m) |
|------|:---:|
| Người chơi, NPC hình người | 0,4 |
| Quái cỡ `S` (chuột, rắn) | 0,35 |
| Quái cỡ `M` (sói, người) | 0,5 |
| Quái cỡ `L` (gấu, tinh anh lớn) | 1,0 |
| Quái cỡ `XL` (boss) | 2,0 |
| Vật triệu hồi | theo `summons.json` (mặc định 0,4) |

  `MonsterDef` (GD-12) bổ sung trường `size: 'S' | 'M' | 'L' | 'XL'`; bảng bán kính ở `monsters/params.json` → `bodyRadiusBySize`.

### 3.2. Va chạm giữa thực thể
- <a id="geo-r-021"></a>**GEO-R-021** **Người chơi đi xuyên qua** người chơi, NPC, quái và vật triệu hồi (không chặn đường nhau — tránh chặn cửa, kẹt ở thành trấn). Ngoại lệ: thực thể có cờ `solid` (vd. tường đất `SKL_EARTH_WALL`, GD-07 SK-R-091) là **chướng ngại động** dạng OBB, chặn di chuyển và đạn như chướng ngại tĩnh.
- <a id="geo-r-022"></a>**GEO-R-022** **Quái tách nhau mềm:** mỗi tick, cặp quái chồng lấn bị đẩy ra theo đường nối tâm, mỗi bên tối đa `separationSpeed` (2 m/s × dt), không đẩy vào chướng ngại. Quái **không** tách khỏi người chơi (đi xuyên) nhưng AI giữ khoảng cách bằng suất cận chiến (GD-12).

### 3.3. Lớp va chạm tĩnh của bản đồ
```ts
interface CollisionLayer {                 // trong shared/maps/{mapId}.json, xuất từ map editor (TD-13)
  bounds: { width: number; depth: number };
  shapes: Array<{
    id: string;
    kind: 'POLYGON' | 'CIRCLE';
    verts?: [number, number][];            // POLYGON lồi, CCW, 3–12 đỉnh
    c?: [number, number]; r?: number;      // CIRCLE (cây, cột)
    flags: { move: boolean; projectile: boolean; sight: boolean; fly?: boolean };   // fly: chặn cả khi đang bay (vật cao, biên bản đồ) — GD-31
  }>;
}
```

| Loại chướng ngại | `move` | `projectile` | `sight` | Ví dụ |
|------------------|:---:|:---:|:---:|-------|
| Tường, đá lớn, nhà | ✅ | ✅ | ✅ | |
| Hàng rào thấp, lan can | ✅ | ❌ | ❌ | Bắn qua được, nhìn qua được |
| Nước sâu, vực | ✅ | ❌ | ❌ | |
| Bụi rậm cao | ❌ | ❌ | ✅ | Che tầm nhìn quái (GD-12) |
| Biên bản đồ | ✅ | ✅ | ❌ | Sinh tự động từ `bounds` |

- <a id="geo-r-023"></a>**GEO-R-023** Đa giác lõm phải được map editor **tách thành đa giác lồi** khi xuất (TD-13). Validator bản đồ: đa giác lồi, CCW, không tự cắt, ≥ 3 đỉnh, cạnh ≥ 0,1m; các vùng đi được liên thông tới mọi cổng nối và POI (kiểm bằng navmesh, §8).
- <a id="geo-r-024"></a>**GEO-R-024** Broadphase: **lưới đều 4m** cho chướng ngại tĩnh (dựng lúc nạp bản đồ) và **lưới 4m cập nhật mỗi tick** cho thực thể động. Mọi truy vấn đi qua broadphase.

---

## 4. Di chuyển

- <a id="geo-r-030"></a>**GEO-R-030** **Collide-and-slide** cho hình tròn thân với chướng ngại `move`:
```
move(pos, r, delta):
  steps = ceil(|delta| / (0,5 × r))              // bước con để không xuyên chướng ngại mỏng
  for each sub-step d = delta / steps:
    for iter in 1..3:
      hit = sweepCircleFirstHit(pos, r, d, obstacles[move])   // thời điểm t ∈ [0,1], pháp tuyến n
      if !hit: pos += d; break
      pos += d × max(0, hit.t − ε_t)              // dừng sát chướng ngại (ε_t sao cho cách ≥ SKIN = 0,01m)
      d = (d × (1 − hit.t)) − n × dot(d × (1 − hit.t), n)   // trượt theo tường
  depenetrate(pos, r)                             // đẩy ra nếu vẫn chồng lấn (sai số)
```
- <a id="geo-r-031"></a>**GEO-R-031** Khi nhân vật **đang bay** (GD-31), `move()` dùng mặt nạ cờ `fly` thay cho `move` (bay qua vật thấp, nước; vẫn chặn vật cao và biên bản đồ); đang cất cánh vẫn dùng `move`. Áp cho: di chuyển WASD (TD-02 §5), né lướt (`DODGING`: `dodge.distance` GD-06, chia đều theo tick trong thời gian lướt do GD-07 §4 / GD-06 quy định), đẩy lùi (`KNOCKBACK` GD-06: đi theo hướng từ nguồn → mục tiêu, dừng ở tường, **không** gây sát thương va tường ở MVP), lướt kỹ năng (`DASH`, §5.6).
- <a id="geo-r-032"></a>**GEO-R-032** Hằng số: `SKIN = 0,01m`, `ε = 1e-9`. Tie-break khi hai va chạm cùng `t`: chọn chướng ngại có `id` nhỏ hơn (tất định).

---

## 5. Hình của đòn (GD-07 `Shape`)

### 5.1. Quy tắc trúng
- <a id="geo-r-040"></a>**GEO-R-040** Mục tiêu trúng nếu `intersects(shapeGeometry, Circle(target.pos, target.bodyRadius))` (GEO-R-004), xét ở vị trí mục tiêu tại thời điểm phán định (sau rewind, TD-02 §7).
- <a id="geo-r-041"></a>**GEO-R-041** Chọn tối đa `maxTargets` (GD-07 SK-R-074): sắp các mục tiêu trúng theo **khoảng cách từ điểm neo của hình** (bảng §5.2) tới **mép thân** mục tiêu (`dist − bodyRadius`), tăng dần; bằng nhau → theo `entityId` tăng dần.
- <a id="geo-r-042"></a>**GEO-R-042** **Tường không chặn** đòn vùng / cận chiến tức thời (`CONE`, `LINE`, `GROUND_CIRCLE`, `SELF_CIRCLE`) — PvE, đơn giản, dễ đọc. **Tường chặn**: `PROJECTILE` (§6.2), quãng lướt của `DASH` (§5.6), và tầm nhìn quái (§7). (Designer muốn đòn vùng bị tường chặn → GD-07 thêm trường, không có ở MVP.)

### 5.2. Bảng hình học

| `Shape` (GD-07 §3) | Hình học | Điểm neo (sắp mục tiêu) | Ghi chú |
|--------------------|----------|-------------------------|---------|
| `CONE { radius, angleDeg }` | `Sector(apex = caster.pos, dir = aim, radius, cosHalf(angleDeg))` | `apex` | Đỉnh tại **tâm** người thi triển (SK-R-080); `radius` tính từ tâm |
| `LINE { length, width }` | `OBB` bắt đầu ở **mép thân** người thi triển: tâm = `caster.pos + aim × (bodyRadius + length/2)`, `halfLen = length/2`, `halfWid = width/2` | `caster.pos` | |
| `GROUND_CIRCLE { radius, maxRange, delaySec }` | `Circle(aimPoint, radius)`; `aimPoint` = `caster.pos + clamp(|aim|, 0, maxRange)` theo hướng aim | `aimPoint` | Vị trí chốt lúc **bắt đầu windup** (vùng cảnh báo không đi theo mục tiêu); có thể đặt lên chướng ngại |
| `SELF_CIRCLE { radius }` | `Circle(caster.pos, radius)` | `caster.pos` | Vị trí lúc lượt trúng |
| `DASH { distance, width }` | Xem §5.6 | điểm xuất phát | |
| `PROJECTILE {...}` | Xem §6.2 | điểm chạm | |
| `SELF` / `SUMMON` | Không có hình trúng | — | |

- <a id="geo-r-043"></a>**GEO-R-043** `aim` client gửi là **điểm** (GD-07 `C2S_CastSkill.aim: [x, z]`). Hướng = chuẩn hóa(`aim − caster.pos`); nếu `|aim − caster.pos| < 0,01` dùng hướng mặt hiện tại.

### 5.3. Phép giao (chuẩn)
```
Circle–Circle:   |c1 − c2|² ≤ (r1 + r2)²
Sector–Circle:   d = target.c − apex; dist = |d|
                 nếu dist > radius + r            → không
                 nếu dist ≤ r                      → trúng (thân phủ đỉnh)
                 nếu dot(d, dir) ≥ dist × cosHalf  → trúng (tâm trong góc)
                 ngược lại: khoảng cách từ target.c tới hai cạnh biên (đoạn apex→apex+edge_i×radius) ≤ r → trúng
                 (edge_i = dir xoay ±halfAngle, lấy từ bảng tra GEO-R-011)
OBB–Circle:      chuyển target.c sang hệ trục OBB; kẹp vào [−halfLen, halfLen] × [−halfWid, halfWid];
                 khoảng cách tới điểm kẹp ≤ r
Capsule–Circle:  khoảng cách điểm–đoạn(a, b) ≤ rCapsule + r
Polygon–Circle:  SAT với các pháp tuyến cạnh + trục từ đỉnh gần nhất tới tâm
Segment–Polygon: cắt từng cạnh (dùng cho LOS)
```
- <a id="geo-r-044"></a>**GEO-R-044** Mọi phép so sánh dùng **bình phương khoảng cách** khi được (tránh `sqrt`); biên tính là **trúng** (`≤`).

### 5.4. Ví dụ
Người chơi tại (10, 10), `CONE { radius 3, angleDeg 120 }`, aim hướng +x. Quái cỡ M (r 0,5) tại (13,3; 10): `dist = 3,3 ≤ 3 + 0,5`, `dot = 3,3 ≥ 3,3 × cos60° = 1,65` → **trúng** (dù tâm ở ngoài bán kính 3m, thân chạm mép quạt). Quái tại (10, 13,4): `dist = 3,4`, `dot = 0 < 1,7` → xét cạnh biên: cạnh trên đi từ (10,10) tới (10 + 3 cos60°, 10 + 3 sin60°) = (11,5; 12,598); chiếu của (0; 3,4) lên hướng cạnh (0,5; 0,866) = 2,944 ∈ [0, 3], khoảng cách vuông góc = |0 × 0,866 − 3,4 × 0,5| = 1,7 > 0,5 → **trượt**.

### 5.5. Vùng cảnh báo
- <a id="geo-r-045"></a>**GEO-R-045** Client vẽ vùng cảnh báo từ **cùng hình học** §5.2 (hàm `shapeOutline(shape, params) → polygon/arc` trong `shared/sim`). Vùng vẽ **không** cộng bán kính thân mục tiêu (người chơi tự hiểu "chạm vào là trúng"); tooltip kỹ năng ghi chú điều này.

### 5.6. DASH
- <a id="geo-r-046"></a>**GEO-R-046** `DASH { distance, width, iframe }`: người thi triển di chuyển theo hướng aim bằng `move()` (§4) tối đa `distance` trong thời gian lướt; quãng đi được thực tế `L` (dừng trước tường). Hình trúng = `OBB` từ vị trí đầu tới vị trí cuối, rộng `width` (cộng 2 × bodyRadius người thi triển theo chiều dài để phủ thân lúc đầu và cuối). Mỗi mục tiêu trúng tối đa một lần mỗi lần lướt.

---

## 6. Đạn

### 6.1. Mô phỏng
- <a id="geo-r-050"></a>**GEO-R-050** Đạn là thực thể server có `pos`, `dir`, `speed`, `radius`, `rangeLeft`, `pierceLeft`, `hitSet`. Mỗi tick: quãng `s = min(speed × dt, rangeLeft)`, quét `Capsule(pos, pos + dir × s, radius)`:
  1. tìm va chạm **chướng ngại `projectile`** sớm nhất `t_wall`;
  2. tìm mọi **mục tiêu** (chưa trong `hitSet`) giao capsule, tính thời điểm chạm `t_i` (giao đầu tiên của hình tròn quét với thân);
  3. xử lý mục tiêu theo `t_i` tăng dần (bằng nhau → `entityId`) với `t_i < t_wall`: trúng, thêm `hitSet`, `pierceLeft −= 1`; khi `pierceLeft < 0` → dừng tại `t_i`;
  4. chạm tường → dừng tại `t_wall`;
  5. hết `rangeLeft` → dừng.
- <a id="geo-r-051"></a>**GEO-R-051** Vị trí xuất phát = `caster.pos + dir × (bodyRadius + radius)`; nếu điểm đó nằm trong chướng ngại → đạn dừng ngay (nổ nếu có `explodeRadius`).
- <a id="geo-r-052"></a>**GEO-R-052** `count > 1` (GD-07 / GD-08 `MOD_SPLIT`): các đạn trải đều trong `spreadDeg`, hướng tính từ bảng tra; mỗi đạn có `hitSet` riêng; một mục tiêu bị nhiều đạn trúng nhận nhiều lượt (đúng thiết kế `projectileFactor` GD-08 §6.3).
- <a id="geo-r-053"></a>**GEO-R-053** Đạn **không** tua lại vị trí mục tiêu (TD-02 §7.2): đạn có thời gian bay, mục tiêu né được bằng di chuyển. Client vẽ đạn từ `S2C_ProjectileSpawn` và **tự mô phỏng** quỹ đạo (cùng `shared/sim`), chỉ dừng khi nhận kết quả server.

### 6.2. Nổ
- <a id="geo-r-054"></a>**GEO-R-054** `explodeRadius` (SK-R-082): khi đạn dừng vì bất kỳ lý do nào, áp lượt trúng `Circle(stopPoint, explodeRadius)` cho mọi mục tiêu giao (gồm mục tiêu bị chạm, nhưng mục tiêu đó **không** nhận thêm lượt trúng trực tiếp lần hai — một đạn = tối đa một lượt mỗi mục tiêu). Nổ không bị tường chặn (GEO-R-042).

---

## 7. Tầm nhìn & khoảng cách tương tác

- <a id="geo-r-060"></a>**GEO-R-060** **LOS** (line of sight) = `Segment(a, b)` không cắt chướng ngại có `sight = true`. Dùng cho: nhận thức quái (`sightRadius`, `fovDeg` — GD-12, `fovDeg` qua bảng tra), phát hiện bí mật bằng mắt (GD-21). **Không** dùng cho kỹ năng người chơi (GEO-R-042) và quét thần thức (xuyên tường, GD-21).
- <a id="geo-r-061"></a>**GEO-R-061** **Khoảng cách tương tác** (NPC, trạm, rương, xác, cổng) = khoảng cách từ tâm nhân vật tới **điểm gần nhất** của hình tương tác của đối tượng (hình tròn mặc định r 0,5m) ≤ bán kính tương tác (thường 3m, theo GD). `CharacterContext.nearPoiIds` (TD-01 §11) tính bằng hàm này, bán kính 5m.

---

## 8. Tìm đường

- <a id="geo-r-070"></a>**GEO-R-070** Navmesh dựng bằng **recast-navigation-js** từ vùng đi được trừ chướng ngại `move`, **mỗi cỡ thân một navmesh** (agent radius 0,4 / 1,0 / 2,0 — cỡ `S`, `M` dùng 0,4 và chấp nhận chồng nhẹ). Dựng **lúc build bản đồ** (TD-13), lưu cùng dữ liệu bản đồ server; client có navmesh 0,4 cho click-to-move.
- <a id="geo-r-071"></a>**GEO-R-071** Đường đi navmesh chỉ là **gợi ý hướng**; chuyển động thực tế luôn qua `move()` (§4), nên lệch nhỏ giữa navmesh và đa giác va chạm không gây xuyên tường.
- <a id="geo-r-072"></a>**GEO-R-072** Chướng ngại động (`solid`) không cập nhật navmesh ở MVP; AI gặp `solid` chắn đường thì đi vòng bằng tránh cục bộ (steering) hoặc tấn công nó (GD-12).

---

## 9. API thư viện

> `@tienlo/sim` còn các hàm thuần ngoài hình học (`actionStateReducer`, `computeStats`, `resolveHit`, `aiDecide`, `worldClock`, `inkRunner`…) — danh mục ở [TD-06 §3](05-game-server.md) và [TD-05 §4](04-client.md).

```ts
// shared/sim/geom2d/index.ts — chữ ký chính (TS thuần, TA-R-021)
export function move(world: CollisionWorld, pos: Vec2, r: number, delta: Vec2): Vec2;
export function sweepCircleFirstHit(world: CollisionWorld, pos: Vec2, r: number, delta: Vec2, mask: Flag): Hit | null;
export function shapeGeometry(shape: Shape, ctx: { casterPos: Vec2; casterRadius: number; aimPoint: Vec2; facing: Vec2 }): HitGeometry;
export function queryTargets(index: EntityIndex, geom: HitGeometry, filter: TargetFilter, maxTargets: number): EntityId[];
export function stepProjectile(world: CollisionWorld, index: EntityIndex, p: ProjectileState, dt: number): ProjectileStepResult;
export function hasLineOfSight(world: CollisionWorld, a: Vec2, b: Vec2): boolean;
export function interactDistance(pos: Vec2, target: InteractShape): number;
export function shapeOutline(shape: Shape, ctx: ...): Outline;    // vẽ vùng cảnh báo (client)
export function positionAt(history: PositionHistory, entityId: EntityId, tick: number): Vec2;   // TD-02 §7.1
```
- <a id="geo-r-080"></a>**GEO-R-080** Hàm **thuần** (không trạng thái ẩn, không I/O); `CollisionWorld` và `EntityIndex` truyền vào. Không cấp phát trong vòng lặp nóng (dùng pool `Vec2` / mảng tái sử dụng) — hiệu năng Node.js.
- <a id="geo-r-081"></a>**GEO-R-081** Ngân sách (một room 150 người + 200 quái, Node.js trên 1 vCPU): toàn bộ va chạm + truy vấn trong một tick ≤ 8ms p99 (phần của 25ms TD-02 NW-R-011).

---

## 10. Kiểm thử

- <a id="geo-r-090"></a>**GEO-R-090** `shared/testvectors/geom2d/*.json` (dù chỉ có TS, vẫn dùng vector để khóa hành vi khi refactor): giao từng cặp hình (≥ 30 trường hợp mỗi phép, gồm tiếp xúc biên, thân phủ đỉnh quạt, mục tiêu sau lưng), `move` trượt tường / góc nhọn / hành lang hẹp bằng đúng `2r`, đạn xuyên + tường + nổ, LOS qua góc tường.
- <a id="geo-r-091"></a>**GEO-R-091** Test **tất định chéo môi trường**: chạy cùng kịch bản 10.000 tick di chuyển + va chạm trên Node.js, Chrome, Firefox, Safari (Playwright) → băm vị trí cuối phải giống hệt.
- <a id="geo-r-092"></a>**GEO-R-092** Công cụ debug: client có lớp vẽ hitbox (phím F9, chỉ build dev) hiện hình trúng do **server** gửi (`S2C_DebugHit`, chỉ dev) chồng lên hình client tính, để soát lệch.

---

## 11. Tham số

```jsonc
// shared/gamedata/world/geometry.json
{
  "skinM": 0.01, "epsilon": 1e-9,
  "moveSubstepFactor": 0.5, "slideIterations": 3,
  "broadphaseCellM": 4,
  "playerBodyRadius": 0.4, "npcBodyRadius": 0.4,
  "monsterSeparationSpeed": 2.0,
  "defaultInteractShapeRadius": 0.5, "contextNearPoiRadius": 5,
  "navAgentRadii": [0.4, 1.0, 2.0]
}
// shared/gamedata/monsters/params.json (bổ sung)
{ "bodyRadiusBySize": { "S": 0.35, "M": 0.5, "L": 1.0, "XL": 2.0 } }
```

---

## 12. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="geo-ac-001"></a>GEO-AC-001 | Ví dụ §5.4 quái tại (13,3; 10) | `queryTargets` | Trúng |
| GEO-AC-002 | Ví dụ §5.4 quái tại (10; 13,4) | `queryTargets` | Không trúng |
| GEO-AC-003 | Quái cỡ M đứng **sau lưng** người chơi, chồng thân lên đỉnh quạt (cách 0,3m) | `CONE` | Trúng (thân phủ đỉnh) |
| GEO-AC-004 | Hành lang rộng đúng 0,8m, người chơi r 0,4 | Đi dọc hành lang | Đi qua được, không kẹt |
| GEO-AC-005 | Đi chéo 45° vào tường | `move` | Trượt dọc tường, không xuyên, cách tường ≥ 0,01m |
| GEO-AC-006 | Né lướt 4m về phía tường cách 2m | Né | Dừng sát tường sau ~2m |
| GEO-AC-007 | Đạn xuyên 1 (`pierce 1`) bay qua 3 quái thẳng hàng | Mô phỏng | Trúng 2 quái gần nhất theo thời gian chạm |
| GEO-AC-008 | Đạn có `explodeRadius 2` chạm quái A, quái B cách A 1,5m | Mô phỏng | A nhận đúng 1 lượt, B nhận 1 lượt nổ |
| GEO-AC-009 | Đạn bắn vào hàng rào thấp (`projectile = false`) | Mô phỏng | Bay qua |
| GEO-AC-010 | `CONE` đánh quái đứng sau tường | `queryTargets` | Trúng (GEO-R-042) |
| GEO-AC-011 | Quái ở sau bụi rậm (`sight = true`) | Kiểm tra nhận thức | Không thấy người chơi |
| GEO-AC-012 | Kịch bản 10.000 tick | Node + 3 trình duyệt | Hash vị trí giống hệt (GEO-R-091) |
| GEO-AC-013 | File `shared/sim` import `Math.sin` | Lint | Fail (GEO-R-003) |
| GEO-AC-014 | Gamedata có `angleDeg: 37` chưa có bảng tra | Build | Generator thêm vào bảng; file sinh lệch → `[GEN]` fail tới khi commit |
| GEO-AC-015 | 150 người + 200 quái, mọi người đánh liên tục | Đo tick | Phần geometry ≤ 8ms p99 |
| GEO-AC-016 | Bản đồ có đa giác lõm | Validator bản đồ | Fail (phải tách lồi) |
| GEO-AC-017 | `aim` trùng vị trí người thi triển | `CONE` | Dùng hướng mặt hiện tại |

---

## 13. Phạm vi theo phase

| Hạng mục | Phase 0 | Phase 1 | Alpha |
|----------|:---:|:---:|:---:|
| Kiểu, phép giao, `move`, `CONE`/`LINE`/`GROUND_CIRCLE`/`PROJECTILE`, bảng tra, lint hàm cấm | ✅ | | |
| `DASH`, nổ, `solid`, LOS, tách quái | | ✅ | |
| Navmesh theo cỡ thân, validator bản đồ | | ✅ | |
| Test tất định chéo trình duyệt, tối ưu ngân sách | | | ✅ |

---

## 14. Câu hỏi mở
- [ ] **GEO-Q-1:** Có cho phép đòn vùng bị tường chặn (cờ `blockedByWalls` trong `SkillDef`) cho một số chiêu boss để tạo lối chơi "nấp sau cột"? Cần thêm LOS từ tâm vùng tới mục tiêu.
- [ ] **GEO-Q-2:** Đẩy lùi vào tường có gây sát thương / choáng thêm (lối chơi đặt vị trí) ở Beta không?
- [ ] **GEO-Q-3:** Người chơi xuyên qua quái có làm combat "lỏng" (không thể chặn quái bảo vệ đồng đội)? Đo cảm giác ở playtest Phase 1; phương án: quái `L`/`XL` có thân `solid` với người chơi.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-23 | — | Bản nháp đầu: hệ tọa độ, kiểu, tất định (cấm hàm lượng giác, bảng tra), thân tròn theo cỡ, đi xuyên thực thể, lớp va chạm tĩnh có cờ, collide-and-slide, hình học từng `Shape` + quy tắc trúng theo thân + sắp mục tiêu, đạn quét + xuyên + nổ, LOS, khoảng cách tương tác, navmesh theo cỡ, API thư viện, kiểm thử, tham số, nghiệm thu |
| v0.2 | 2026-10-25 | — | Trỏ danh mục hàm thuần khác của `@tienlo/sim` sang TD-05 / TD-06 |
| v0.3 | 2026-10-25 | — | Cờ va chạm `fly` (GD-31) |

