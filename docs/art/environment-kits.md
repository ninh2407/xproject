# AR-06 — Environment Kits

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | AR-06 · Prefix ID: `KIT` |
| Owner | Tech Artist + Environment Artist (duyệt: Art Director, Level Designer) |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 0 (atlas gradient v0 + góc Thanh Trúc Lâm 40×30m, AR-01 §10 mẫu #3), Phase 1 (`V1_F01` hoàn chỉnh), Phase 3 (đủ Vực 1) |
| Quyết định nền | D-01 (logic 2D), D-22 (3D phong cách hóa, mỹ thuật ưu tiên AI); PLAN §6.2 (kit module 50–150 prop / kit), §6.3, §6.6, §6.7 (mục 2–4); [AR-01](art-bible.md) §2, §4.1, §5.1; [AR-02](asset-guidelines.md) §2, §3, §6, §7, §8; [TD-11](../tech/10-performance.md) §3, §3.1 (**nguồn số ngân sách**); [TD-13](../tech/12-map-editor.md) §5.1 (E1, E3, E11), §7 |
| Liên quan | [LR-01 World Bible](../lore/world-bible.md) §4, §6, §8 · [GD-21 Open World](../design/22-open-world.md) §3.1–3.4, §3.9 · [GD-28 Hiểm địa](../design/27-danger-zones.md) · [GD-30 Động phủ & Sơn môn](../design/28-caves-sect-sites.md) · [GD-14 Xã hội](../design/20-social.md) §5.4b, SO-R-056 · [GD-31 Ngự kiếm](../design/29-sword-flight.md) §5.3 · [GD-04](../design/10-cultivation.md) CU-R-072 · [GD-08](../design/14-technique-crafting.md) §11 · [GD-24](../design/25-camera-controls.md) CAM-R-013 · [TD-05](../tech/04-client.md) §5.3, §5.4, §9 · [TD-14](../tech/13-geom2d.md) §3.3 · [TD-15](../tech/14-oss-register.md) §4.5 · [AR-04 VFX](vfx-guidelines.md) §8.1 |

**Tài liệu này cung cấp cho:** Environment Artist và **AI script Blender** (quy chuẩn lắp ghép, danh sách mảnh, `collisionTemplate`), map editor (TD-13 E1 snap / đổi bảng màu, E3 tự sinh va chạm, E11 mẫu biome), Level Designer (kit theo bản đồ), validator asset `pnpm art:check` và validator bản đồ `pnpm maps:build`, AR-05 (danh sách asset).

> **Quy ước "đề xuất":** mọi mã / file / kiểu dữ liệu đánh dấu **(đề xuất)** chưa có trong GD / TD nào; code chưa dùng cho tới khi đồng bộ theo §13. Tên kit và mảnh (`KIT_*`, `PRP_*`) theo mẫu AR-02 §3 — là **ID asset** mới, được tạo trong tài liệu này. **Không** có địa danh, thế lực, vực mới: mọi nơi chốn lấy từ LR-01 / GD-21; nơi chưa có tên ghi vào §15.

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="kit-r-001"></a>**KIT-R-001** | **Grid snap:** mọi mảnh kit đặt theo lưới **0,5m** và góc **15°** (TD-13 E1, AR-02 §2); kích thước mảnh module là bội số của 0,5m. |
| <a id="kit-r-002"></a>**KIT-R-002** | **Mảnh module, không mesh bản đồ đúc liền:** bản đồ ghép từ mảnh kit (+ 2–3 điểm nhấn vẽ riêng mỗi bản đồ, AR-01 §8, PLAN §6.7); chunk 32×32m gộp mesh khi build (TD-13 §6, CL-R-064). |
| <a id="kit-r-003"></a>**KIT-R-003** | **Một atlas gradient chung** cho phần lớn môi trường (AR-01 §2, PLAN §6.7 mục 2); màu theo vực đến từ **bảng màu bản đồ** áp lúc render (§6), không nhân bản texture theo vực. Texture riêng chỉ cho điểm nhấn (256–512², TD-11 §3). |
| <a id="kit-r-004"></a>**KIT-R-004** | Mọi mảnh có **`collisionTemplate`** đúng AST-R-050 hoặc khai báo rõ "không va chạm"; vật cao chặn khi bay có `fly: true` (GD-31 FLY-R-030). Hình ảnh **luôn khớp** va chạm (không đặt prop có va chạm vào lớp rải bị giảm theo preset, KIT-R-031). |
| <a id="kit-r-005"></a>**KIT-R-005** | **Đọc được ở camera game** (ART-R-001): duyệt bằng ảnh chụp nghiêng 50°, 20m; mặt đất và lối đi rõ; không dùng màu mang nghĩa gameplay (AR-01 §4.4, nguyên tố §4.2) cho trang trí lớn (ART-R-003). |
| <a id="kit-r-006"></a>**KIT-R-006** | Ngân sách tam giác / texture / bản đồ theo **TD-11 §3, §3.1** — tài liệu này không đặt số khác (§9). |
| <a id="kit-r-007"></a>**KIT-R-007** | Mọi mảnh có **hồ sơ nguồn gốc** `art/provenance/{assetId}.json` (AST-R-005, TD-15 §4.5). |

---

## 2. Quy chuẩn lắp ghép

### 2.1. Lưới, kích thước, trục
| Mục | Quy chuẩn | Nguồn |
|-----|-----------|-------|
| Đơn vị / trục | 1 unit = 1m; Blender Z lên, xuất glTF +Y Up; mặt đất y = 0 | AST-R-001, AR-02 §2 |
| Lưới đặt | 0,5m; xoay theo bước 15° | TD-13 E1 |
| **Đơn vị module** (đề xuất) | `M = 2m`: chiều dài cạnh nối ∈ {2, 4, 8}m; sàn / nền 2×2, 4×4m | — |
| Bậc cao hiển thị (đề xuất) | Tường thôn 3m · vách thấp 3m · vách cao 6m · vách núi 12m · hầm 4m (rộng) × 4m (cao) | — |
| Mảnh cong | Theo bước 15° / 30° để khép vòng bằng xoay snap | TD-13 E1 |
| Pivot | **Tâm đáy** (chạm đất) cho mọi prop / module | AR-02 §2 |
| Biến đổi | Apply rotation + scale trước khi xuất (scale 1, rotation 0) | AR-02 §2 |
| Tỷ lệ khi đặt | Mảnh **có va chạm**: scale cố định 1 (va chạm khai báo ở scale 1). Mảnh rải **không** va chạm: scale đồng đều ±15% (đề xuất) | — |

- <a id="kit-r-010"></a>**KIT-R-010** Kích thước khung bao (`footprint` x × z) của mảnh module là bội số 0,5m (sai số ≤ 0,01m); mảnh tự do (đá, cây) không bắt buộc nhưng pivot vẫn rơi đúng điểm lưới khi đặt.
- <a id="kit-r-011"></a>**KIT-R-011** Hai mảnh module đặt kề nhau theo lưới **không hở > 0,01m** và không chồng mặt (z-fighting) ở cạnh nối; kiểm tự động bằng script Blender (§12) và test KIT-AC-002.

### 2.2. Cạnh nối (edge profile — editor: TD-13 MF-R-032)
Mỗi cạnh nối của mảnh module mang một **hồ sơ cạnh**; hai mảnh ghép được khi cạnh đối diện cùng hồ sơ. Editor hiển thị gợi ý snap theo cạnh (E1).

| Hồ sơ | Mặt cắt | Dùng cho |
|-------|---------|----------|
| `WALL_05` | Tường dày 0,5m, cao 3m | Tường thôn trấn, tường điện |
| `CLIFF_H3` / `CLIFF_H6` / `CLIFF_H12` | Vách đá cao 3 / 6 / 12m, chân lượn trong 1m | Vách, ranh giới bản đồ |
| `BANK` | Bờ dốc đất / đá xuống mặt nước (−0,5m) | Bờ sông, bờ đầm, ao |
| `TUNNEL_4` | Hầm rộng 4m, cao 4m (mở trần để camera nhìn từ trên) | Hầm mỏ, bí cảnh hang |
| `BOARD_2` | Ván / cầu rộng 2m | Đường ván đầm lầy, cầu tàu |
| `FENCE` | Rào cao ≤ 1,2m | Rào trúc, rào gỗ, lan can |
| `STAIR_4` | Bậc thang rộng 4m | Bậc đá Thanh Vân Sơn |

- <a id="kit-r-012"></a>**KIT-R-012** Mảnh mở đầu / kết thúc (`_END`) và góc (`_CORNER_OUT`, `_CORNER_IN`) phải có cho mọi hồ sơ cạnh dùng trong kit — không để mép hở thấy được ở camera game.
- <a id="kit-r-013"></a>**KIT-R-013** Mái nhà là **node riêng** gắn cờ `roofHide` (`extras`, AR-02 AST-R-043) để client ẩn mái khi nhân vật vào trong (CAM-R-013).

### 2.3. Va chạm theo loại mảnh
`collisionTemplate` theo AST-R-050 (`CIRCLE` / `POLYGON` lồi, CCW, **3–12 đỉnh** — TD-14 §3.3, MF-V-01), cờ theo bảng TD-14 §3.3 và GD-31 FLY-R-030. Ký hiệu cột "Va chạm" ở §4–§5: `CIRCLE r0,4 · M P` = hình tròn bán kính 0,4m, cờ `move` + `projectile`; `S` = `sight`; `—` = không va chạm.

| Loại mảnh | Mặc định | Cờ | `fly` |
|-----------|----------|----|:---:|
| Thân cây thường, cột, cọc | `CIRCLE` | M P | ❌ |
| Cây cổ thụ lớn, tháp, lầu | `CIRCLE` / `POLYGON` | M P S | ✅ |
| Nhà thấp (1 tầng), tường thôn, đá lớn | `POLYGON` | M P S | ❌ |
| Vách cao ≥ 6m, vách núi, thác | `POLYGON` | M P S | ✅ |
| Hàng rào thấp, lan can | `POLYGON` | M | ❌ |
| Bụi rậm cao, lau sậy cao | `POLYGON` | S | ❌ |
| Cỏ, hoa, lá rụng, sỏi, decal | — | — | — |
| Mặt nước sâu, vực | Vẽ ở lớp va chạm của editor (E3), không phải prop | M | ❌ |

- <a id="kit-r-015"></a>**KIT-R-015** **Ngưỡng `fly` (đề xuất):** vật chặn `move` có chiều cao hiển thị **≥ 6m** (bằng `visualHeightM` khi bay, GD-31 §11) **và** khung bao ≥ 2m → `fly: true`; thân mảnh (trúc, cột) cao hơn 6m vẫn `fly: false` (người bay đi qua, client làm mờ vật dưới chân — GD-31 §11). Chờ GD-31 xác nhận (KIT-Q-5).
- <a id="kit-r-016"></a>**KIT-R-016** Va chạm phủ khung bao chân mảnh với sai số ≤ 0,25m; mảnh có chân lõm (nhà chữ L) **tách thành nhiều mảnh** mỗi mảnh một đa giác lồi, vì AST-R-050 chỉ cho một `collisionTemplate` mỗi node (KIT-Q-6).
- <a id="kit-r-017"></a>**KIT-R-017** **Prop động không nằm trong chunk:** vật có vị trí bí mật / theo lượt (bẫy `TrapPlacement`, rương từ `secretCandidates`, lối thoát tạm `tempExit`, di hài — GD-28, GD-21) **không** được nướng vào GLB chunk (lộ vị trí `@serverOnly`, GD-29 RF-R-041); client sinh chúng như thực thể khi server gửi. Danh mục `DYNAMIC` ở §3.

---

## 3. Phân loại mảnh, đặt tên, dữ liệu

### 3.1. Danh mục mảnh (đề xuất)
| `category` | Nghĩa | Gộp chunk | Giảm theo preset |
|------------|-------|:---:|:---:|
| `MODULE` | Mảnh ghép có hồ sơ cạnh (tường, vách, hầm, mái, ván) | ✅ | ❌ |
| `STRUCTURE` | Vật đứng riêng có va chạm (nhà dựng sẵn, cây, đá lớn) | ✅ | ❌ |
| `SCATTER` | Vật rải không va chạm (cỏ, hoa, lá, sỏi) — thin instance | Thin instance | ✅ (`droppable`) |
| `ACCENT` | Điểm nhấn vẽ riêng (texture riêng), ≤ 3 mỗi bản đồ | ✅ | ❌ |
| `POI` | Hình ảnh của POI cố định (TD-13 §4): truyền tống trận, bia, trạm… | ✅ | ❌ |
| `DYNAMIC` | Vật do server sinh (KIT-R-017) | ❌ (thực thể) | ❌ |

### 3.2. Đặt tên (AR-02 §3)
| Loại | Mẫu | Ví dụ |
|------|-----|-------|
| Kit theo vực | `KIT_{VỰC}_{TÊN}` | `KIT_V1_BAMBOO` (AR-02) |
| Kit dùng chung mọi vực (đề xuất) | `KIT_CM_{TÊN}` | `KIT_CM_POI` |
| Mảnh | `PRP_{TÊN_KIT_NGẮN}_{TÊN}_{nn}` | `PRP_BAMBOO_STALK_03` (AR-02) |
| Mảnh module | `PRP_{KIT}_{TÊN}_{DÀI}M[_H{CAO}]` | `PRP_ROCK_CLIFF_8M_H6` |
| Mảnh theo cấp công trình | `PRP_{KIT}_{CÔNG_TRÌNH}_L{n}` | `PRP_CAVE_TINHTHAT_L2` |
| Bảng màu bản đồ (đề xuất) | `PAL_{VỰC}_{TÊN}` | `PAL_V1_GREEN` (ví dụ TD-13 §8) |
| Mẫu biome (đề xuất) | `BIO_{VỰC}_{TÊN}` | `BIO_V1_BAMBOO_GROVE` |
| Atlas gradient chung (đề xuất) | `ENV_GRADIENT` → texture `ENV_GRADIENT_ATLAS` | AR-02 §3 (`{assetId}_ATLAS`) |

File: `art/env/{kit}/{assetId}.glb` (AR-02 §3); `assetId` ≤ 48 ký tự, không đổi tên sau khi được tham chiếu (AST-R-010).

### 3.3. Dữ liệu mảnh (đề xuất)
```ts
// art/env/{kit}/kit.json — sinh bởi script Blender (§12), đọc bởi map editor và art:check
type KitCategory = 'MODULE' | 'STRUCTURE' | 'SCATTER' | 'ACCENT' | 'POI' | 'DYNAMIC';
type EdgeProfile = 'WALL_05' | 'CLIFF_H3' | 'CLIFF_H6' | 'CLIFF_H12' | 'BANK' | 'TUNNEL_4' | 'BOARD_2' | 'FENCE' | 'STAIR_4';
type MaterialGroup = /* §6.1 */ string;

interface KitDef {
  id: string;                         // KIT_V1_BAMBOO
  shortName: string;                  // BAMBOO (dùng trong PRP_*)
  region: string;                     // V1 | CM
  phase: 'ALPHA' | 'BETA' | 'LAUNCH'; // TD-07 DT-R-058
  pieces: KitPieceDef[];
}
interface KitPieceDef {
  assetId: string;                    // PRP_BAMBOO_STALK_01
  category: KitCategory;
  footprint: [number, number];        // mét (x, z), bội số 0,5 với MODULE
  heightM: number;
  tris: number;                       // LOD0
  materialGroups: MaterialGroup[];
  collisionTemplate: { kind: 'CIRCLE'; r: number; flags: CollisionFlags }
                   | { kind: 'POLYGON'; verts: [number, number][]; flags: CollisionFlags }
                   | null;            // AST-R-050; null = không va chạm (khai báo rõ)
  edges?: Partial<Record<'N' | 'E' | 'S' | 'W', EdgeProfile>>;
  droppable?: boolean;                // SCATTER: được giảm theo preset (KIT-R-031)
  roofHide?: boolean;                 // KIT-R-013
  specialTexture?: string;            // ACCENT: texture riêng 256–512²
}
interface CollisionFlags { move: boolean; projectile: boolean; sight: boolean; fly: boolean }
```
GLB mang các khóa tương ứng trong `extras` của node gốc (`collisionTemplate` theo AST-R-050; `kit`, `category`, `edges`, `droppable`, `roofHide` — danh sách đóng ở AR-02 AST-R-043).

---

## 4. Kit Vực 1 — Thanh Vân Vực (MVP, chi tiết)

### 4.0. Kit nào dùng ở bản đồ nào
| Bản đồ (GD-21 §3.2, LR-01 §4.2) | Kit chính | Kit phụ | Bảng màu (§6.2) | Mẫu biome (§7) |
|------|------|------|------|------|
| `V1_HUB` Thanh Vân Trấn | `VILLAGE`, `THANHVAN` | `RUINS` (Luân Hồi Đài, Bia Vạn Cổ), `CM_POI`, `BAMBOO` (rìa) | `PAL_V1_TOWN` | `BIO_V1_VILLAGE_EDGE` |
| `V1_F01` Thanh Trúc Lâm | `BAMBOO` | `VILLAGE` (thôn trồng trúc), `RUINS` (miếu hoang), `ROCK`, `CM_POI`, `MINE` (chỉ `PRP_MINE_ORE_NODE_*` cho POI `ORE` — GD-16 `V1_F01` §7) | `PAL_V1_GREEN` | `BIO_V1_BAMBOO_GROVE`, `BIO_V1_VILLAGE_EDGE` |
| `V1_F02` Bích Thủy Hà Bạn | `RIVER` | `VILLAGE` (làng chài), `ROCK`, `CM_POI` | `PAL_V1_RIVER` | `BIO_V1_RIVERBANK` |
| `V1_F03` Hoang Thạch Khoáng | `MINE`, `ROCK` | `TACTHIEN` (cờ, cổng trại), `CM_POI` | `PAL_V1_MINE` | `BIO_V1_ROCKY_SLOPE`, `BIO_V1_MINE_YARD` |
| `V1_F04` Vụ Ẩn Trạch | `SWAMP` | `ROCK`, `RUINS`, `CM_POI` | `PAL_V1_SWAMP` | `BIO_V1_MARSH` |
| `V1_F05` Lạc Hà Cốc | `BLOSSOM` | `ROCK`, `HOPHOAN` (cửa cốc), `CM_POI` | `PAL_V1_DUSK` | `BIO_V1_BLOSSOM_VALLEY` |
| `V1_F06` Cổ Chiến Nguyên | `BATTLE` | `RUINS`, `ROCK`, `CM_POI` (cổng Vực 2) | `PAL_V1_ASH` | `BIO_V1_ASH_FIELD` |
| `V1_BC01` Phế Khoáng Động | `MINE` (hầm) | `ROCK` | `PAL_V1_BC01` | — |
| `V1_BC02` Hồ Yêu Động | `BLOSSOM` (hang) | `ROCK` | `PAL_V1_BC02` | — |
| `V1_BC03` Tàn Kiếm Trủng | `BATTLE` (mộ kiếm) | `RUINS` | `PAL_V1_BC03` | — |
| `V1_HD01` Vạn Độc Đầm | `SWAMP` + `HAZARD` | `CM_POI` (lối thoát) | `PAL_V1_HD01` | `BIO_V1_MARSH` |
| `V1_HD02` Cổ Chiến Trường | `BATTLE` + `HAZARD` | `CM_POI` | `PAL_V1_HD02` | `BIO_V1_ASH_FIELD` |

Ưu tiên mảnh: **P0** chặn mốc phase · **P1** cần trong phase · **P2** dời được. "Ph" = phase PLAN §7 cần mảnh lần đầu. Số tam giác là **mục tiêu LOD0** trong trần TD-11 §3 (prop ≤ 1.500; lớn / kiến trúc ≤ 4.000). Hậu tố `_01..04` = số biến thể.

### 4.1. `KIT_V1_BAMBOO` — Rừng trúc (F01, rìa HUB)
| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_BAMBOO_STALK_01..04` | Cụm 1–3 thân trúc cao 6–8m | 600–900 | `CIRCLE r0,3–0,6 · M P` | ❌ | P0 · 0 |
| `PRP_BAMBOO_STALK_FAR_01..02` | Cụm thân trúc lấp **lõi** khối rừng chặn, **không va chạm** (`SCATTER`, droppable — KIT-R-048) | 300–500 | — | — | P0 · 1 |
| `PRP_BAMBOO_CLUMP_01..03` | Bụi trúc dày Ø ~3m | ≤ 1.500 | `POLYGON` 8 đỉnh `· M P S` | ❌ | P0 · 0 |
| `PRP_BAMBOO_GROVE_WALL_4M_01..02` | Mảng rừng trúc rậm làm hành lang / biên (module 4×2m) | ≤ 2.500 | `POLYGON 4×2 · M P S` | ❌ | P0 · 1 |
| `PRP_BAMBOO_FENCE_2M` / `_4M` / `_END` | Rào trúc cao 1m (`FENCE`) | 300 / 500 / 150 | `POLYGON · M` | ❌ | P0 · 1 |
| `PRP_BAMBOO_STUMP_01..02` | Gốc trúc cụt | 200 | `CIRCLE r0,3 · M` | ❌ | P1 · 1 |
| `PRP_BAMBOO_SHOOT_01..03` | Măng, trúc non thấp | 150–300 | — | — | P1 · 1 |
| `PRP_BAMBOO_GRASS_01..04` | Cỏ, dương xỉ (`SCATTER`, droppable) | 50–200 | — | — | P0 · 0 |
| `PRP_BAMBOO_LEAFLITTER_01..03` | Thảm lá trúc rụng (mesh phẳng) | ≤ 100 | — | — | P0 · 0 |
| `PRP_BAMBOO_STREAM_ROCK_01..03` | Đá suối nhỏ | 300 | `CIRCLE r0,5 · M P` | ❌ | P1 · 1 |
| `PRP_BAMBOO_BRIDGE_4M` | Cầu trúc qua suối (`BOARD_2`); khe đi được do lớp nước chừa ra | 800 | lan can `POLYGON · M` | ❌ | P1 · 1 |

- <a id="kit-r-048"></a>**KIT-R-048** **Lõi khối rừng chặn không sinh va chạm:** khối rừng / bụi chặn (vd. M1–M3, M7 của GD-16 `V1_F01` §3.2) chỉ có va chạm ở **viền** (`PRP_BAMBOO_GROVE_WALL_4M_*`, `PRP_BAMBOO_CLUMP_*`); phần lõi người chơi không tới được lấp bằng `PRP_BAMBOO_STALK_FAR_*` (không `collisionTemplate`, `category: SCATTER`, `droppable`), đặt cách viền có va chạm ≥ 2m vào trong, để giữ trần đa giác va chạm (TD-13 §7) và giảm tam giác ở preset Thấp (KIT-R-036). Validator / lớp phủ editor: mảnh `STALK_FAR` không được nằm trên vùng đi được của navmesh (MF-V-02).

### 4.2. `KIT_V1_VILLAGE` — Thôn trấn phàm nhân (HUB, thôn F01, làng chài F02)
Gỗ ấm, mái ngói xám xanh, trắng vôi, đèn lồng đỏ (AR-01 §4.1 `V1_HUB`). HUB phủ `NO_FLY` toàn bộ (FLY-R-032) nên `fly` chỉ có ý nghĩa với nhà ở bản đồ dã ngoại.

| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_VILLAGE_WALL_2M` / `_4M` | Tường gỗ – vôi (`WALL_05`) cao 3m | 200 / 350 | `POLYGON · M P S` | ❌ | P0 · 1 |
| `PRP_VILLAGE_WALL_CORNER_OUT` / `_IN` / `_END` | Góc ngoài / trong / đầu tường | 250 | `POLYGON · M P S` | ❌ | P0 · 1 |
| `PRP_VILLAGE_WALL_DOOR_2M` / `_WINDOW_2M` | Mảng tường có cửa / cửa sổ | 400 | 2 mảnh `POLYGON` hai bên cửa | ❌ | P0 · 1 |
| `PRP_VILLAGE_ROOF_4X4` / `_4X8` / `_EDGE` / `_RIDGE` / `_CORNER` | Mái ngói module, `roofHide` | 300–800 | — | — | P0 · 1 |
| `PRP_VILLAGE_HOUSE_S_01..03` | Nhà 1 tầng dựng sẵn 6×6m (ghép từ module, gộp) | ≤ 4.000 | `POLYGON 6×6 · M P S` | ❌ | P0 · 1 |
| `PRP_VILLAGE_HOUSE_M_01..02` | Nhà 2 tầng 8×8m (HUB) | ≤ 4.000 | `POLYGON 8×8 · M P S` | ❌ | P0 · 2 |
| `PRP_VILLAGE_GATE_TOWN` | Cổng trấn (2 cột + mái), rộng 8m | ≤ 4.000 | 2 × `CIRCLE r0,5 · M P S` | ❌ | P0 · 2 |
| `PRP_VILLAGE_STALL_01..03` | Sạp chợ / sàn giao dịch | 600 | `POLYGON · M P` | ❌ | P0 · 2 |
| `PRP_VILLAGE_LANTERN_POLE` / `_HANG` | Cột đèn lồng đỏ / đèn treo (phát sáng đêm) | 300 / 150 | `CIRCLE r0,2 · M` / — | ❌ | P0 · 1 |
| `PRP_VILLAGE_BANNER_THANHVAN` | Cờ xanh Thanh Vân (điểm nhấn HUB) | 300 | `CIRCLE r0,15 · M` | ❌ | P1 · 2 |
| `PRP_VILLAGE_CRATE_01..02` / `_BARREL` / `_JAR_01..03` | Thùng, thùng tròn, chum | 150–400 | `CIRCLE r0,4 · M P` (nhỏ < 0,5m: —) | ❌ | P0 · 1 |
| `PRP_VILLAGE_FENCE_WOOD_2M` / `_END` | Rào gỗ (`FENCE`) | 300 / 120 | `POLYGON · M` | ❌ | P0 · 1 |
| `PRP_VILLAGE_WELL` | Giếng | 800 | `CIRCLE r0,9 · M P` | ❌ | P1 · 2 |
| `PRP_VILLAGE_CART` | Xe kéo | 900 | `POLYGON · M P` | ❌ | P1 · 2 |
| `PRP_VILLAGE_OBSERVATORY` | **Khâm Thiên Đài** (LR-01 §4.2) — `ACCENT` | ≤ 4.000 + texture 512² | `POLYGON · M P S` | ✅ | P1 · 2 |
| `PRP_VILLAGE_FIELD_PLOT_4X4` | Luống vườn / ruộng (mesh phẳng) | 200 | — | — | P2 · 2 |

### 4.3. `KIT_V1_THANHVAN` — Kiến trúc Thanh Vân Tông (HUB, Thanh Vân Sơn)
Thẳng, cân đối, đường dọc thanh thoát; xanh lam + trắng; lụa, ngọc; mây và kiếm (AR-01 §5.1).

| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_THANHVAN_GATE` | Sơn môn — cổng vào Thanh Vân Tông (`ACCENT`) | ≤ 4.000 + 512² | 2 × `POLYGON · M P S` | ✅ | P0 · 2 |
| `PRP_THANHVAN_STAIR_4M` / `_LANDING` | Bậc đá trắng lên núi (`STAIR_4`) / chiếu nghỉ | 400 / 300 | — (đi được) | — | P0 · 2 |
| `PRP_THANHVAN_BALUSTRADE_2M` / `_END` | Lan can đá trắng (`FENCE`) | 300 / 150 | `POLYGON · M` | ❌ | P0 · 2 |
| `PRP_THANHVAN_HALL_WALL_4M` / `_PILLAR` | Tường gỗ sơn điện (`WALL_05`) / cột tròn | 400 / 300 | `POLYGON · M P S` / `CIRCLE r0,35 · M P S` | ❌ | P0 · 2 |
| `PRP_THANHVAN_ROOF_*` (6 mảnh) | Mái cong lam, đầu đao mây, `roofHide` | 400–900 | — | — | P0 · 2 |
| `PRP_THANHVAN_LIBRARY` | **Tàng Kinh Các** (lầu 3 tầng) — `ACCENT`, chứa Ngộ Đạo Thạch (GD-08) | ≤ 4.000 + 512² | `POLYGON · M P S` | ✅ | P0 · 2 |
| `PRP_THANHVAN_PAVILION` | Đình nghỉ mái cong | ≤ 2.500 | 4 × `CIRCLE r0,25 · M P` | ❌ | P1 · 2 |
| `PRP_THANHVAN_SWORD_STELE` | Bia khắc mây – kiếm (biểu tượng tông) | 600 | `POLYGON · M P S` | ❌ | P1 · 2 |
| `PRP_THANHVAN_CLOUD_LAMP` | Đèn đá hoa văn mây | 400 | `CIRCLE r0,3 · M P` | ❌ | P1 · 2 |
| `PRP_THANHVAN_BANNER` | Cờ lam – trắng | 300 | `CIRCLE r0,15 · M` | ❌ | P1 · 2 |
| `PRP_THANHVAN_PINE_01..02` | Tùng cổ trên núi (cây cổ thụ lớn) | ≤ 2.500 | `CIRCLE r0,8 · M P S` | ✅ | P1 · 2 |

### 4.4. `KIT_V1_RIVER` — Bờ sông, làng chài (F02)
| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_RIVER_BANK_4M` / `_8M` / `_CURVE15` / `_CURVE30` / `_END` | Mảnh bờ sông (`BANK`); va chạm nước vẽ ở lớp editor (M, không P, không S — TD-14) | 600–1.200 | — | — | P0 · 2 |
| `PRP_RIVER_REED_01..04` | Lau sậy thấp (`SCATTER`) | 100–300 | — | — | P0 · 2 |
| `PRP_RIVER_REED_TALL_01..02` | Lau sậy cao (che tầm nhìn quái) | 400 | `POLYGON · S` | ❌ | P1 · 2 |
| `PRP_RIVER_ROCK_S` / `_M` / `_L` | Đá sông | 300 / 700 / 1.400 | `CIRCLE · M P` / `POLYGON · M P S` (L) | ❌ | P0 · 2 |
| `PRP_RIVER_PIER_4M` / `_END` | Cầu tàu gỗ (`BOARD_2`) | 500 / 300 | — (lớp nước chừa khe) | — | P0 · 2 |
| `PRP_RIVER_BRIDGE_8M` / `_12M` | Cầu gỗ (kết nối dạng "cầu", GD-21 §3.3) | 1.500 / 2.500 | lan can `POLYGON · M` | ❌ | P0 · 2 |
| `PRP_RIVER_BOAT_01..02` | Thuyền chài | 900 | `POLYGON · M P` | ❌ | P1 · 2 |
| `PRP_RIVER_FISH_RACK` | Giàn phơi cá | 500 | `POLYGON · M` | ❌ | P1 · 2 |
| `PRP_RIVER_STILT_HOUSE_01..02` | Nhà sàn làng chài | ≤ 4.000 | `POLYGON · M P S` | ❌ | P1 · 2 |
| `PRP_RIVER_WILLOW_01..02` | Cây ven sông | ≤ 1.500 | `CIRCLE r0,5 · M P` | ❌ | P1 · 2 |
| `PRP_RIVER_LILY_01..03` | Bèo, lá sen nổi (`SCATTER`) | ≤ 100 | — | — | P2 · 3 |
| `PRP_RIVER_GUARD_LANTERN` | Đèn trấn yêu (sự kiện Thú triều, GD-23 `WE_V1_BEAST_TIDE`) — `DYNAMIC` | 600 | `CIRCLE r0,5 · M P` | ❌ | P1 · 5b |

### 4.5. `KIT_V1_ROCK` — Núi đá, vách, ranh giới (dùng chung mọi bản đồ, đổi màu theo bảng)
| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_ROCK_CLIFF_4M_H3` / `_8M_H3` | Vách thấp 3m (`CLIFF_H3`) | 800 / 1.400 | `POLYGON · M P S` | ❌ | P0 · 0 |
| `PRP_ROCK_CLIFF_4M_H6` / `_8M_H6` | Vách cao 6m (`CLIFF_H6`) | 1.200 / 2.500 | `POLYGON · M P S` | ✅ | P0 · 1 |
| `PRP_ROCK_CLIFF_8M_H12` | Vách núi 12m — biên bản đồ (GD-21 §3.3) | ≤ 4.000 | `POLYGON · M P S` | ✅ | P0 · 1 |
| `PRP_ROCK_CLIFF_CORNER_OUT_H{3,6,12}` / `_IN_H{3,6,12}` / `_END_H{3,6}` | Góc / đầu vách | 600–2.500 | `POLYGON · M P S` | theo cao | P0 · 1 |
| `PRP_ROCK_CLIFF_CAP_01..02` | Mỏm đỉnh vách | 600 | — | — | P1 · 2 |
| `PRP_ROCK_BOULDER_S_01..03` / `_M_01..03` / `_L_01..03` | Đá tảng | 300 / 800 / 1.500 | `CIRCLE · M P` (S, M) / `POLYGON · M P S` (L) | ❌ | P0 · 0 |
| `PRP_ROCK_PEBBLE_01..04` | Sỏi (`SCATTER`) | ≤ 100 | — | — | P0 · 0 |
| `PRP_ROCK_SLOPE_4M` | Dốc đá nối hai cao độ hiển thị | 600 | — | — | P1 · 1 |
| `PRP_ROCK_CAVE_MOUTH_01..02` | Miệng hang (cửa bí cảnh / cửa động, có khe đi vào) | ≤ 3.000 | 2 × `POLYGON · M P S` | ✅ | P0 · 1 |
| `PRP_ROCK_WATERFALL_H6` | Thác nước (mesh + shader nước; "sau thác" cho cửa động HM-R-010) | ≤ 3.000 | `POLYGON · M P S` | ✅ | P1 · 3 |
| `PRP_ROCK_TREE_ANCIENT_01..02` | Cây cổ thụ lớn chung (đổi màu theo bảng; "gốc cây cổ thụ" HM-R-010) | ≤ 4.000 | `CIRCLE r1,2 · M P S` | ✅ | P1 · 2 |

### 4.6. `KIT_V1_MINE` — Hầm mỏ (F03, BC01)
Xám đá, nâu đất, gỗ giàn mỏ cũ, lam tinh thể khoáng, bụi (AR-01 §4.1).

| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_MINE_SCAFFOLD_4M` / `_END` | Giàn gỗ mỏ cũ | 900 / 400 | chân `POLYGON · M P` | ❌ | P0 · 2 |
| `PRP_MINE_SUPPORT_BEAM` | Cột chống gỗ | 300 | `CIRCLE r0,25 · M P` | ❌ | P0 · 2 |
| `PRP_MINE_TUNNEL_4M` / `_CORNER` / `_T` / `_END` | Hầm module (`TUNNEL_4`), mở trần | ≤ 2.500 | `POLYGON · M P S` hai vách | — (instance / trong hầm) | P0 · 2 |
| `PRP_MINE_TUNNEL_COLLAPSED` | Đoạn hầm sập (vụ sập hầm, LR-01 §4.2) | ≤ 3.000 | `POLYGON · M P S` | ✅ | P1 · 2 |
| `PRP_MINE_ORE_NODE_01..03` | Mạch khoáng (hình ảnh POI `ORE`; dùng cả ở `V1_F01` — §4.0) | 500 | `CIRCLE r0,6 · M P` | ❌ | P0 · 1 |
| `PRP_MINE_CRYSTAL_01..03` | Tinh thể khoáng phát sáng lam (trang trí) | 300 | — | — | P1 · 2 |
| `PRP_MINE_CART` / `_RAIL_4M` / `_RAIL_CURVE15` | Xe goòng / đường ray | 700 / 200 / 250 | `POLYGON · M P` / — | ❌ | P1 · 2 |
| `PRP_MINE_SPOIL_HEAP_01..02` | Đống đá thải | 800 | `POLYGON · M P S` | ❌ | P1 · 2 |
| `PRP_MINE_TENT` / `_CAMPFIRE` | Lều Khoáng Tặc / lửa trại | 800 / 400 | `POLYGON · M P S` / `CIRCLE r0,5 · M` | ❌ | P1 · 2 |
| `PRP_MINE_WINCH` | Tời kéo gỗ | 900 | `POLYGON · M P` | ❌ | P2 · 3 |

### 4.7. `KIT_V1_SWAMP` — Đầm lầy sương (F04, HD01)
Lục xám rêu, xám tím sương (luôn có), lục độc phát sáng (AR-01 §4.1). Đá: **dùng lại** `PRP_ROCK_BOULDER_*` với `PAL_V1_SWAMP` (không làm đá riêng).

| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_SWAMP_MANGROVE_01..03` | Cây đầm rễ chống | ≤ 1.500 | `CIRCLE r0,6 · M P` | ❌ | P0 · 3 |
| `PRP_SWAMP_DEADTREE_01..02` | Cây chết | 800 | `CIRCLE r0,4 · M P` | ❌ | P1 · 3 |
| `PRP_SWAMP_BOARDWALK_4M` / `_CURVE15` / `_END` | Đường ván gỗ trên đầm (`BOARD_2`) | 400 | — (lớp nước chừa khe) | — | P0 · 3 |
| `PRP_SWAMP_REED_TALL_01..02` | Cỏ lác cao (chỗ phục kích Vụ Quỷ, GD-12) | 300 | `POLYGON · S` | ❌ | P1 · 3 |
| `PRP_SWAMP_MUD_POOL_01..02` | Vũng bùn (chỉ hình ảnh) | 300 | — | — | P1 · 3 |
| `PRP_SWAMP_TOXIC_SHROOM_01..03` | Nấm độc phát sáng lục | 200 | — | — | P1 · 3 |
| `PRP_SWAMP_ALGAE_01..03` | Rêu tảo nổi (`SCATTER`) | ≤ 100 | — | — | P1 · 3 |
| `PRP_SWAMP_HUT_RUIN` | Chòi bỏ hoang | ≤ 1.500 | `POLYGON · M P S` | ❌ | P2 · 3 |

### 4.8. `KIT_V1_BLOSSOM` — Thung lũng hoa đào (F05, BC02)
Hồng cam ráng chiều, tím hoa đào (AR-01 §4.1). Lửa hồ ly đỏ cam là **VFX** (AR-04 §8.1), không phải prop.

| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_BLOSSOM_PEACH_TREE_01..03` | Đào linh (cây thường) | ≤ 1.500 | `CIRCLE r0,4 · M P` | ❌ | P0 · 3 |
| `PRP_BLOSSOM_PEACH_ANCIENT` | Đào cổ thụ | ≤ 4.000 | `CIRCLE r1,0 · M P S` | ✅ | P1 · 3 |
| `PRP_BLOSSOM_PETAL_CARPET_01..03` | Thảm cánh hoa (`SCATTER`) | ≤ 100 | — | — | P0 · 3 |
| `PRP_BLOSSOM_FLOWER_01..04` | Hoa cỏ (`SCATTER`) | ≤ 200 | — | — | P0 · 3 |
| `PRP_BLOSSOM_CAVE_WALL_4M` / `_CORNER` / `_END` | Vách hang BC02 (`TUNNEL_4`), rễ đào bám | ≤ 2.500 | `POLYGON · M P S` | — | P1 · 3 |
| `PRP_BLOSSOM_STREAM_BRIDGE` | Cầu đá vòm nhỏ | ≤ 1.500 | lan can `POLYGON · M` | ❌ | P2 · 3 |

### 4.9. `KIT_V1_BATTLE` — Cổ chiến trường (F06, HD02, BC03)
Xám tro, đỏ sét, kim loại rỉ, lam lạnh oán linh; "kiếm gãy cắm đầy đất" (LR-01 §4.2). Không xương / máu me chi tiết (ART-R-004, WB-R-009).

| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_BATTLE_BROKEN_SWORD_01..05` | Kiếm gãy cắm đất (nhỏ: `SCATTER`; lớn: có va chạm) | 150–400 | — / `CIRCLE r0,2 · M` | ❌ | P0 · 3 |
| `PRP_BATTLE_WAR_WALL_4M_H3` / `_4M_H6` / `_END` | Tường thành đổ nát (`CLIFF_H3`/`H6` profile tường) | 800 / 1.600 / 500 | `POLYGON · M P S` | theo cao | P0 · 3 |
| `PRP_BATTLE_ARMOR_PILE_01..02` | Giáp rỉ chất đống | 600 | `CIRCLE r0,6 · M P` | ❌ | P1 · 3 |
| `PRP_BATTLE_BANNER_TORN_01..02` | Cờ rách cổ | 300 | `CIRCLE r0,15 · M` | ❌ | P1 · 3 |
| `PRP_BATTLE_CRATER_01..02` | Hố trận (vành mesh + decal) | 800 | — | — | P1 · 3 |
| `PRP_BATTLE_ASH_TREE_01..02` | Cây cháy trơ | 800 | `CIRCLE r0,4 · M P` | ❌ | P1 · 3 |
| `PRP_BATTLE_GRAVE_MOUND_01..02` | Gò mộ (Tàn Kiếm Trủng) | ≤ 1.500 | `POLYGON · M P S` | ❌ | P1 · 3 |
| `PRP_BATTLE_TOMB_WALL_4M` / `_CORNER` / `_END` | Vách mộ kiếm BC03 (`TUNNEL_4`) | ≤ 2.500 | `POLYGON · M P S` | — | P1 · 3 |

### 4.10. `KIT_V1_RUINS` — Phế tích, di tích Thái Sơ (mọi bản đồ)
| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_RUINS_STELE_01..03` | Bia đá (hình ảnh POI `STELE`, GD-21 §3.6) | 400–800 | `POLYGON · M P S` | ❌ | P0 · 1 |
| `PRP_RUINS_PILLAR_BROKEN_01..03` | Cột đá vỡ | 400–900 | `CIRCLE r0,5 · M P S` | ❌ | P0 · 1 |
| `PRP_RUINS_WALL_FRAG_2M` / `_4M` | Tường đổ (`WALL_05`) | 300 / 600 | `POLYGON · M P S` | ❌ | P0 · 1 |
| `PRP_RUINS_FLOOR_4X4_01..02` | Nền đá cổ khắc trận văn | 200 | — | — | P1 · 1 |
| `PRP_RUINS_SHRINE_ABANDONED` | **Miếu hoang Thanh Trúc Lâm** — cảnh mở đầu (LR-01 §9, GD-18) — `ACCENT` | ≤ 4.000 + 512² | `POLYGON · M P S` | ❌ | **P0 · 1** |
| `PRP_RUINS_REINCARNATION_TERRACE` | **Luân Hồi Đài** — đá đen khắc trận văn, ngoại ô tây HUB (POI `REMNANT_ALTAR`) — `ACCENT` | ≤ 4.000 + 512² | `POLYGON · M P S` | ✅ | P0 · 2 |
| `PRP_RUINS_TABLET_COURT_01..02` | **Bia Vạn Cổ** trong sân Luân Hồi Đài | 800 | `POLYGON · M P S` | ❌ | P1 · 2 |
| `PRP_RUINS_ARRAY_RING` | Vòng trận văn đá trang trí | 600 | — | — | P2 · 3 |

### 4.11. `KIT_V1_HAZARD` — Đạo cụ hiểm địa (HD01, HD02)
| Mã | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|-------|:---:|---------|:---:|:---:|
| `PRP_HAZARD_POISON_VENT_01..02` | Lỗ phun độc `TRP_POISON_VENT` — **`DYNAMIC`** (KIT-R-017) | 500 | — (bẫy do server) | — | P0 · 3 |
| `PRP_HAZARD_STONE_TRAP` | Bẫy trận đá HD02 — `DYNAMIC` | 600 | — | — | P0 · 3 |
| `PRP_HAZARD_SOUL_ALTAR` | **Trấn Hồn Đài** (GD-28 DZ-R-100) — cố định theo layout | ≤ 1.500 | `POLYGON · M P S` | ❌ | P0 · 3 |
| `PRP_HAZARD_POISON_POOL_01..02` | Ao độc trang trí (vùng độc thật vẽ bằng `areaIds`, AR-04 `VFX_DZ_POISON_FOG`) | 400 | — | — | P1 · 3 |
| `PRP_HAZARD_TEST_*` (≤ 5) | Đạo cụ cho hiểm địa thử nghiệm Phase 1 (dùng lại `ROCK` / `RUINS`) | — | — | — | P0 · 1 |

### 4.12. `KIT_CM_POI` — Hình ảnh POI dùng chung mọi vực (đề xuất tiền tố `CM`)
| Mã | `PoiKind` (TD-13 §4) | Mô tả | Tam giác | Va chạm | `fly` | Ưu tiên · Ph |
|----|------|-------|:---:|---------|:---:|:---:|
| `PRP_POI_TELEPORT_ARRAY` | `TELEPORT_ARRAY` | Đài đá + trận văn (VFX AR-04 `VFX_POI_TELEPORT_*`); bệ đi lên được | ≤ 3.000 | 4 cột viền `CIRCLE r0,3 · M P` | ❌ | P0 · 1 |
| `PRP_POI_LEY_STONE` | `LEY_LINE` | Đá linh mạch (VFX cột linh khí) | ≤ 1.500 | `CIRCLE r0,8 · M P S` | ❌ | P0 · 1 |
| `PRP_POI_MEDITATION_MAT` | `MEDITATION_SPOT` | Bồ đoàn / đài đả tọa HUB | 300 | — | — | P1 · 2 |
| `PRP_POI_HERB_BASE_01..03` | `HERB` | Bụi linh thảo chung (mô hình riêng từng `herbId` thuộc AR-05) | 300 | — | — | P0 · 1 |
| `PRP_POI_CHEST_COMMON` / `_RARE` / `_LEGENDARY` | `CHEST` | Rương 3 bậc (`chestTier` GD-28) — `DYNAMIC` | 600–1.200 | `CIRCLE r0,5 · M P` | ❌ | P0 · 1 |
| `PRP_POI_STATION_ALCHEMY` | `ALCHEMY_STATION` | Đan lô | ≤ 1.500 | `CIRCLE r0,8 · M P` | ❌ | P0 · 2 |
| `PRP_POI_STATION_FORGE` | `FORGE_STATION` | Lò rèn + đe | ≤ 1.500 | `POLYGON · M P` | ❌ | P0 · 3 |
| `PRP_POI_INSIGHT_STONE` | `INSIGHT_STATION` | **Ngộ Đạo Thạch** (trong Tàng Kinh Các) — `ACCENT` | ≤ 3.000 + 512² | `POLYGON · M P S` | ❌ | P0 · 4 |
| `PRP_POI_INSTANCE_DOOR_01..02` | `INSTANCE_DOOR` (kết nối) | Cửa bí cảnh / hiểm địa dạng trận môn (kiểu hang dùng `PRP_ROCK_CAVE_MOUTH_*`) | ≤ 3.000 | 2 × `POLYGON · M P S` | ✅ | P0 · 1 |
| `PRP_POI_REGION_GATE` | `REGION_GATE` (kết nối) | Cổng Vực 2 ở F06 — `ACCENT` | ≤ 4.000 + 512² | 2 × `POLYGON · M P S` | ✅ | P1 · 3 |
| `PRP_POI_BORDER_MARKER_01..02` | — | Cột mốc / biển chỉ đường ở biên nối (GD-21 §3.3 quy tắc 6) | 400 | `CIRCLE r0,3 · M P` | ❌ | P0 · 1 |
| `PRP_POI_QUEST_BOARD` | `NPC` (`BOUNTY_BOARD`) | Bảng nhiệm vụ / bảng truy nã | 600 | `POLYGON · M P S` | ❌ | P1 · 2 |
| `PRP_POI_EXIT_PORTAL` | `EXIT` | Vòng đá lối thoát hiểm địa (lối cố định; lối tạm là `DYNAMIC`) | ≤ 1.500 | — (bước vào) | — | P0 · 1 |
| `PRP_POI_CORPSE_TABLET` | (di hài, GD-28 §5.6) | Bia gỗ + nến — `DYNAMIC` | 400 | `CIRCLE r0,3 · M` | ❌ | P1 · 1 |
| `PRP_POI_CAVE_GATE_CLIFF` / `_TREE` / `_WATERFALL` | `CAVE_GATE` | Cửa động ở vách núi / gốc cổ thụ / sau thác (HM-R-010); MVP đặt sẵn, chưa kích hoạt (GD-30 §16) | ≤ 2.500 | `POLYGON · M P S` | ✅ | P1 · 3 |

---

## 5. Kit thôn trấn, tông môn, hiểm địa, động phủ

### 5.1. Thôn trấn / thành
- Vực 1 chỉ có **một thị trấn** (`V1_HUB` Thanh Vân Trấn, 150–250m, GD-21 §3.1) và các thôn nhỏ trong dã ngoại (thôn trồng trúc F01, làng chài F02 — LR-01 §4.2). Không có "thành" ở Vực 1; "thành cổ chìm trong băng" thuộc Vực 3 (LR-01 §4.1).
- <a id="kit-r-020"></a>**KIT-R-020** Thành trấn: phủ `NO_FLY` (FLY-R-032) nên không phụ thuộc cờ `fly`; mái `roofHide`; đèn lồng / cửa sổ phát sáng về đêm (GD-21 §3.8) qua nhóm vật liệu `EMISSIVE_WARM` (§6.1); ngân sách thành trấn ≤ 6 MB tải, tam giác môi trường theo TD-11 §3.1 (xem vênh §13).
- Checklist hình ảnh HUB (GD-21 §3.4 + LR-01 §4.2): truyền tống trận trung tâm, Đan Phòng, Lò Rèn, cửa hàng, sàn giao dịch, bảng nhiệm vụ, **Khâm Thiên Đài**, khu đả tọa, cổng Thanh Vân Tông + **Tàng Kinh Các** (Ngộ Đạo Thạch), Ngự Thú Sư, thủ kho Tàng Bảo Các, **Luân Hồi Đài** ở ngoại ô phía tây. Đan Phòng / Lò Rèn / cửa hàng là `PRP_VILLAGE_HOUSE_*` + trạm `CM_POI` + biển hiệu (không cần kit riêng).

### 5.2. Ba đại tông (GD-14 §5.4b, AR-01 §5.1, LR-01 §6.1)
| Tông | Kit | Tổng đàn | Ngôn ngữ hình dạng | Mảnh chính (sơ bộ, chi tiết hóa khi vào Beta) | Phase |
|------|-----|----------|--------------------|-----------------------------------------------|:---:|
| **Thanh Vân Tông** | `KIT_V1_THANHVAN` (§4.3) | Thanh Vân Trấn / Thanh Vân Sơn (`V1_HUB`) | Thẳng, cân đối; lam + trắng; mây và kiếm | §4.3 | 2 (MVP) |
| **Tạc Thiên Bang** | `KIT_V1_TACTHIEN` | **Tạc Thiên Trại**, hầm sâu Hoang Thạch Khoáng (`V1_F03`) | Góc cạnh, bất đối xứng; đỏ sẫm + đen + đồng; da, sắt rèn thô, xích; búa đục núi / vết nứt trời | `PRP_TACTHIEN_BANNER` (cờ đỏ — điểm nhấn F03 đã có ở MVP, P1 · 3) · `_PALISADE_4M` (tường cọc + sắt vá, 700, `POLYGON · M P S`) · `_GATE` (cổng treo xích, ≤ 4.000, ✅ `fly`) · `_HALL_WALL_4M` / `_ROOF_*` (đại sảnh hang đá) · `_BLACK_MARKET_STALL` (sạp **Hắc Thị**, 800, `POLYGON · M P`) · `_BRAZIER` (chậu lửa sắt, 400, `CIRCLE r0,4 · M P`) · `_CHAIN_HANG` (—) · `_HAMMER_MONUMENT` (tượng búa đục núi, `ACCENT`, ✅) | 5a |
| **Hợp Hoan Tông** | `KIT_V1_HOPHOAN` | **Hợp Hoan Cốc**, nhánh cốc của Lạc Hà Cốc (`V1_F05`) | Đường cong mềm, lụa bay nhiều lớp **kín đáo**; hồng đào + tím + vàng nhạt; hoa đào song sinh, quạt | `PRP_HOPHOAN_GATE` (cổng cốc, ≤ 3.000) · `_PAVILION` (thủy tạ mái cong rủ lụa, ≤ 4.000, cột `CIRCLE`) · `_SILK_CURTAIN` (rèm lụa động bằng vertex shader, —) · `_TWIN_PEACH` (cây đào song sinh, `ACCENT`, ✅) · `_FAN_SCREEN` (bình phong quạt, `POLYGON · M P S`) · `_SILK_LANTERN` (đèn lụa, —) · `_LOTUS_POND_*` (ao, va chạm nước ở editor) | 5a |

- <a id="kit-r-022"></a>**KIT-R-022** Hợp Hoan Tông: gợi "mị" bằng màu, lụa chuyển động, hoa — **không** hình ảnh gợi dục ở tượng, tranh, đồ trang trí (ART-R-030, SO-R-04A, WB-R-007). Tạc Thiên Bang: không biểu tượng tội phạm ngoài đời thực. Mọi mảnh hai kit này qua checklist nội dung khi duyệt (AR-01 §9).
- <a id="kit-r-023"></a>**KIT-R-023** Tổng đàn NPC (`SECT_HQ`) là vùng an toàn có `NO_FLY` (FLY-R-032) — dựng bằng kit tông + `ROCK` / `MINE` / `BLOSSOM` của bản đồ chứa nó, đổi bảng màu bản đồ.

### 5.3. Sơn môn tông môn tự lập — `KIT_CM_SITE` (GD-30 §5.5–5.6, Beta 2)
| Mã | Theo GD-30 HM-R-061 | Ghi chú |
|----|---------------------|---------|
| `PRP_SITE_FACADE_L1` … `_L5` | `facadePrefabs[0..4]`: cổng → tường → chính điện → lầu tháp → linh quang | Mỗi cấp là **nhóm mảnh** (mỗi mảnh ≤ 4.000 tam giác) gộp sẵn ít draw call (HM-R-062); vừa vùng mặt tiền ~40 × 40m (HM-R-051) |
| `PRP_SITE_RUINS` | `ruinsPrefab` | Trạng thái `RUINS` |
| `PRP_SITE_VACANT_STELE` | `VACANT` | Đất trống + bia đá "Vô chủ" |
| `PRP_SITE_BANNER_EMBLEM` | Cờ hiệu in huy hiệu tông | Huy hiệu là decal động, không texture riêng mỗi tông |

- Phong cách trung tính (không lấy màu ba đại tông); 2–3 phong cách mặt tiền là HM-Q-5 (chưa chốt). Vị trí `SECT_SITE` và `footprintId` đặt sẵn từ MVP (GD-30 §16) → Phase 3 chỉ cần `PRP_SITE_VACANT_STELE`.

### 5.4. Trú địa — `KIT_CM_GROUNDS` (GD-14 SO-R-056, instance `SECT_GROUNDS` 60–120m, Beta 1)
`PRP_GROUNDS_DANPHONG_L0..L5`, `PRP_GROUNDS_LUYENKHICAC_L0..L5`, `PRP_GROUNDS_LINHMACHDIEN_L0..L5` (cấp 0 = nền móng mờ chưa xây), `PRP_GROUNDS_PLAZA_4X4` (sân lát đá, `MODULE`), `PRP_GROUNDS_GATE_COMMON` (cổng chung ở `HUB` khi tông chưa giữ sơn môn). Va chạm công trình `POLYGON · M P S`; trú địa không bay trừ khi GD-30 bật (FLY-R-062).

### 5.5. Động phủ — `KIT_CM_CAVE` (GD-30 §5.3, instance `CAVE` 30–60m, Beta 1)
| Mã | Theo GD-30 | Ghi chú |
|----|-----------|---------|
| `PRP_CAVE_SHELL_4M` / `_CORNER` / `_END` | Không gian hang cổ phong | `TUNNEL_4` mở trần; `POLYGON · M P S` |
| `PRP_CAVE_TINHTHAT_L0..L3` | `TINH_THAT` | L0 hiển thị mờ "chưa xây" (GD-30 §11) |
| `PRP_CAVE_LINHDIEN_L0..L3` | `LINH_DIEN` 2 / 4 / 6 ô | Cây trồng theo giai đoạn thuộc asset linh thảo (AR-05) |
| `PRP_CAVE_DANTHAT_L0..L3` | `DAN_THAT` (trạm `ALCHEMY_STATION`) | Dùng lại `PRP_POI_STATION_ALCHEMY` ở cấp ≥ 1 |
| `PRP_CAVE_KHITHAT_L0..L3` | `KHI_THAT` (trạm `FORGE_STATION`) | Dùng lại `PRP_POI_STATION_FORGE` |
| `PRP_CAVE_THUVIEN_L0..L3` | `THU_VIEN` (Linh Thú Viên, GD-25 §5.6) | Có trong `CaveBuilding` nhưng không trong bảng HM-R-030 (§13) — P2 |
| `PRP_CAVE_DECOR_SLOT` | Ô trang trí (HM-R-034) | Vật trang trí là cosmetic GD-19 (`COS_*`), không thuộc kit |

### 5.6. Thử thách / ảo cảnh — `KIT_CM_TRIAL`
| Mã | Nơi | Ghi chú | Ph |
|----|-----|---------|:---:|
| `PRP_TRIAL_TRIB_ARENA` | Đấu trường thiên kiếp tròn 30m (`TRIB_TC`, GD-04 CU-R-072) | Nền + vành đá; mây đen là VFX (AR-04 `VFX_TRIB_CLOUD`) | 3 |
| `PRP_TRIAL_REMNANT_ISLE_*` | Tàn Hồn Cảnh — ảo cảnh trong Luân Hồi Đài (LR-01 §3.4, GD-26 §5.15) | Đảo đá lơ lửng trong sương, cùng ngôn ngữ đá đen Luân Hồi Đài | 2–3 |
| `PRP_TRIAL_SANDBOX_HALL` | Diễn Pháp Tràng (GD-08 §11) | Phòng có hình nhân (hình nhân là thực thể mục tiêu, model theo AR-02) | 4 |

### 5.7. Hiểm địa
- <a id="kit-r-025"></a>**KIT-R-025** Hiểm địa dùng kit của bản đồ cửa vào (`SWAMP` cho HD01, `BATTLE` cho HD02) + `KIT_V1_HAZARD`; bảng màu **dẫn xuất**: giảm bão hòa của bảng gốc + đen + màu nguy hiểm (AR-01 §4.1) — `PAL_V1_HD01`, `PAL_V1_HD02` (§6.2).
- <a id="kit-r-026"></a>**KIT-R-026** Mỗi layout (`V1_HD01_L1`, `_L2`…) là một `MapDef` riêng (GD-28 §7); kit phải đủ để dựng ≥ 2 layout khác biệt cho mỗi hiểm địa mà không cần mảnh mới (thay vị trí, không thay asset).
- <a id="kit-r-027"></a>**KIT-R-027** Không đặt prop trang trí trùng hình dạng / màu với bẫy hoặc lối thoát (người chơi không được nhầm trang trí là bẫy, hay bẫy là trang trí).

---

## 6. Bảng màu từng vực

### 6.1. Atlas gradient chung và nhóm vật liệu (đề xuất)
- <a id="kit-r-030"></a>**KIT-R-030** `ENV_GRADIENT_ATLAS` 512² (ETC1S, AST-R-040): **16 hàng** = 16 nhóm vật liệu × **4 cột** biến thể; mỗi ô 128×32 chứa **dải độ sáng** tối → sáng + chi tiết vẽ nhẹ (vân gỗ, ngói, đá). Màu thật do bảng màu bản đồ cấp theo nhóm (ramp 3 điểm tối / giữa / sáng) — shader toon (TD-05 `ToonMaterial`) tra nhóm theo tọa độ `v` của UV. Đổi bảng màu = đổi uniform, **không** tải texture mới (TD-13 E1 "đổi màu theo bảng màu vực"). Cần TD-05 xác nhận cách hiện thực (KIT-Q-8).

| # | `MaterialGroup` (đề xuất) | Dùng cho |
|:-:|----|----|
| 0 | `WOOD_WARM` | Gỗ nhà, giàn, cầu |
| 1 | `WOOD_DARK` | Cột, khung, gỗ cũ |
| 2 | `ROOF_TILE` | Ngói |
| 3 | `PLASTER` | Vôi, tường trắng |
| 4 | `STONE` | Đá, vách |
| 5 | `STONE_DARK` | Đá đen (Luân Hồi Đài), đá ướt |
| 6 | `EARTH` | Đất, bùn |
| 7 | `SAND` | Cát, sỏi |
| 8 | `FOLIAGE` | Lá, cỏ |
| 9 | `FOLIAGE_ALT` | Trúc, hoa, lá đổi màu |
| 10 | `BARK` | Vỏ cây |
| 11 | `METAL` | Sắt, đồng, rỉ |
| 12 | `CLOTH` | Cờ, lụa, mái che |
| 13 | `ACCENT_RED` | Đèn lồng, sơn son (điểm nhấn) |
| 14 | `EMISSIVE_WARM` | Đèn lồng / cửa sổ đêm, lửa trại |
| 15 | `EMISSIVE_COOL` | Tinh thể khoáng, độc phát sáng, oán khí |

- <a id="kit-r-031"></a>**KIT-R-031** Nhóm phát sáng (`EMISSIVE_*`) không được trùng màu vùng cảnh báo / vùng an toàn (AR-01 §4.4) và cách ≥ 30° sắc độ so với `VPAL_*` của nguyên tố gần nhất (AR-04 §3.1), trừ khi AR-01 đã chỉ định (vd. lam tinh thể khoáng F03). Validator so sắc độ trong `env_palettes.json`.

### 6.2. Vực 1 (tên màu theo AR-01 §4.1; hex là khởi điểm đề xuất)
| Bản đồ | `paletteId` (đề xuất) | Chủ đạo | Phụ | Điểm nhấn | Không khí / sương | Hex khởi điểm (chủ đạo · phụ · nhấn · sương) |
|--------|----|----|----|----|----|----|
| `V1_HUB` | `PAL_V1_TOWN` | Gỗ ấm, mái ngói xám xanh | Trắng vôi | Đèn lồng đỏ, cờ xanh Thanh Vân | Ấm, an toàn | `#A9774A` · `#E9E3D6` · `#C8352B` · `#F2E6D0` |
| `V1_F01` | `PAL_V1_GREEN` (TD-13 §8) | Xanh trúc tươi | Vàng nắng xuyên lá | Trắng sương nhẹ | Yên bình | `#6FAF4E` · `#E6CF7A` · `#F4F6EE` · `#DDEBD5` |
| `V1_F02` | `PAL_V1_RIVER` | Xanh ngọc nước | Cát nhạt, lau sậy | Cam chiều trên mặt nước | Thoáng, mưa nhiều | `#3FA7A0` · `#D9C9A0` · `#E8934A` · `#CFE3E2` |
| `V1_F03` | `PAL_V1_MINE` | Xám đá, nâu đất | Gỗ giàn mỏ cũ | Lam tinh thể khoáng; đỏ cờ Tạc Thiên Trại | Khô, bụi | `#857C72` · `#8A6440` · `#4FA3D9` · `#CDBFA8` |
| `V1_F04` | `PAL_V1_SWAMP` | Lục xám rêu | Xám tím sương (luôn có) | Lục độc phát sáng | Ngột ngạt, mờ | `#5E6B4C` · `#8C8396` · `#A6E04A` · `#9C96A6` |
| `V1_F05` | `PAL_V1_DUSK` | Hồng cam ráng chiều | Tím hoa đào | Lửa hồ ly đỏ cam (VFX) | Mơ màng | `#E59A7A` · `#B98AC4` · `#F06A2E` · `#F3D3C4` |
| `V1_F06` | `PAL_V1_ASH` | Xám tro, đỏ sét | Kim loại rỉ | Lam lạnh oán linh | Hoang tàn, nặng nề | `#77706A` · `#8E5A3E` · `#6FA8D6` · `#B5ADA6` |
| `V1_HD01` | `PAL_V1_HD01` | Dẫn xuất `PAL_V1_SWAMP`: bão hòa ×0,6, tối hơn | Đen | Lục độc | "Sinh tử tự chịu" | (sinh tự động) |
| `V1_HD02` | `PAL_V1_HD02` | Dẫn xuất `PAL_V1_ASH`: bão hòa ×0,6 | Đen | Đỏ sát khí | "Sinh tử tự chịu" | (sinh tự động) |
| `V1_BC01..03` | `PAL_V1_BC01..03` | Dẫn xuất từ bảng của bản đồ cửa vào (F03 / F05 / F06), tối hơn (trong hang / mộ) | | | | (sinh tự động) |

- <a id="kit-r-032"></a>**KIT-R-032** Hai bản đồ kề nhau phải **khác bảng màu nhận ra được** ở biên nối (GD-21 §3.3 quy tắc 6: "đổi bảng màu" báo sắp sang bản đồ khác); dải chuyển 30–60m trước biên trộn hai bảng (đề xuất).
- Đỏ cờ Tạc Thiên Trại dùng nhóm `ACCENT_RED`; lục độc F04 và lam oán linh F06 dùng `EMISSIVE_COOL` — đều phải qua KIT-R-031 (lục độc F04 gần `VPAL_MOC`, lam F06 gần `VPAL_THUY` → KIT-Q-9).

```jsonc
// shared/gamedata/world/env_palettes.json (TD-07 registry, `"visibility": "client"` — trích)
[
  { "id": "PAL_V1_GREEN", "region": "V1",
    "groups": {
      "FOLIAGE":     ["#2F5A2A", "#6FAF4E", "#B9DE86"],
      "FOLIAGE_ALT": ["#3E6B2E", "#86BF5A", "#D5EE9E"],
      "EARTH":       ["#4A3A28", "#7A6045", "#A88B66"],
      "STONE":       ["#5C615A", "#8C918A", "#C3C7BE"],
      "WOOD_WARM":   ["#5A3B22", "#9A6A40", "#D2A06C"]
      /* … đủ 16 nhóm */
    },
    "fogTint": "#DDEBD5", "sunTint": "#FFF1C9",
    "splatLayers": ["TER_V1_DAMP_SOIL", "TER_V1_BAMBOO_LITTER", "TER_V1_GRASS", "TER_V1_PATH_STONE"] },
  { "id": "PAL_V1_HD01", "region": "V1", "derivedFrom": "PAL_V1_SWAMP", "saturationMul": 0.6, "valueMul": 0.85 }
]
```
`MapDef.ambient.palette` (TD-13 §3) trỏ tới `id` ở file này; `splatLayers` là 4 lớp `TerrainSplat` (TD-05 §5.3) — tên texture địa hình theo mẫu `TER_{VỰC}_{TÊN}` của AR-02 §3.

### 6.3. Vực 2–4 (sơ bộ, theo LR-01 §4.1 — tên vực **tạm**, WB-Q-1)
| Vực | Tên (LR-01, đề xuất) | Chủ đề (LR-01) | Bảng màu sơ bộ | Kit dự kiến (sơ bộ) | Phase |
|-----|------|------|------|------|:---:|
| V2 | *Xích Viêm Vực* | Hoang mạc đá đỏ, núi lửa ngủ, tông môn tu hỏa | Đỏ gạch, cam đất, đen bazan; nhấn cam nham nguội (tránh trùng `VPAL_HOA` — dùng nham **tắt**, ít phát sáng) | `KIT_V2_REDROCK` (vách đá đỏ, cột đá phong hóa, cát), `KIT_V2_VOLCANO` (bazan đen, dòng nham nguội, khe hơi nóng, tro), kit tông môn tu hỏa (tên tông chưa có — KIT-Q-3); dùng lại `ROCK`, `VILLAGE`, `RUINS`, `CM_*` đổi bảng màu | 5b |
| V3 | *Hàn Băng Vực* | Băng nguyên phương bắc, thành cổ chìm trong băng | Trắng xanh, xám đá lạnh; nhấn ấm (đèn) để tách khỏi `VPAL_BANG` | `KIT_V3_ICEFIELD`, `KIT_V3_FROZEN_CITY` | Sau launch |
| V4 | *Vân Hải Thiên Khuyết* | Quần đảo lơ lửng trên biển mây, gần "tiên lộ" | Trắng mây, lam trời nhạt, vàng nhạt; tránh vàng kim `VPAL_KIM` cho mảng lớn | `KIT_V4_SKYISLE` (mép đảo, rễ treo, cầu xích), `KIT_V4_CLOUDSEA` (mặt mây shader); mép đảo = vực (`move`, `fly: false` — bay qua được, FLY-R-030) | Sau launch |

- <a id="kit-r-033"></a>**KIT-R-033** Vực mới ưu tiên **dùng lại** kit cũ + bảng màu mới (PLAN §6.7 mục 4); chỉ làm kit mới cho hình dạng không có (đá đỏ phong hóa, băng, đảo lơ lửng). Mục tiêu: ≥ 40% mảnh của một vực mới là mảnh tái dùng (đề xuất).

---

## 7. Mẫu biome cho cọ rải (TD-13 E11)

### 7.1. Quy tắc
- <a id="kit-r-035"></a>**KIT-R-035** Cọ rải chỉ đặt mảnh **có trong kit** được phép của biome; vị trí snap 0,5m / 15° (KIT-R-001); **không** rải trong vùng va chạm, trên lối đi (khoảng cách tối thiểu), quanh POI và trong vùng an toàn điểm vào (MF-V-05) theo tham số biome; mảnh có `collisionTemplate` rải ra được editor **tự sinh va chạm** (E3) và tính vào ngân sách đa giác va chạm (TD-11 §3.1).
- <a id="kit-r-036"></a>**KIT-R-036** Giảm mật độ theo preset (TD-05 §9 "Mật độ cỏ / prop rải" 30 / 70 / 100%) chỉ áp cho mảnh `SCATTER` `droppable`; chọn mảnh giữ lại **tất định** theo băm vị trí (mọi client cùng preset thấy giống nhau). Mảnh có va chạm không bao giờ bị giảm (KIT-R-004).
- <a id="kit-r-037"></a>**KIT-R-037** Kết quả rải là **dữ liệu scene** (hình ảnh); chỉ va chạm sinh ra mới đi vào `shared/maps/{mapId}.json`. Rải lại một vùng không được đổi ID POI (MF-R-021).

### 7.2. Mẫu biome Vực 1 (đề xuất)
| `biomeId` | Bản đồ | Mảnh chính (trọng số) | Mật độ khởi điểm (mảnh / 100m²) | Ràng buộc | Lớp splat (4) |
|-----------|--------|-----------------------|:---:|-----------|------|
| `BIO_V1_BAMBOO_GROVE` | F01, rìa HUB | `BAMBOO_STALK_*` 35 · `BAMBOO_CLUMP_*` 10 · `BAMBOO_GRASS_*` 30 · `BAMBOO_LEAFLITTER_*` 20 · `BAMBOO_SHOOT_*` 5 | 18 | Dốc ≤ 30° · cách lối đi ≥ 2m · cách POI ≥ 3m | Đất ẩm · lá trúc mục · cỏ · đá lối mòn |
| `BIO_V1_VILLAGE_EDGE` | HUB, thôn F01 / F02 | `VILLAGE_JAR_*` 10 · `VILLAGE_CRATE_*` 10 · `VILLAGE_FENCE_WOOD_2M` 10 · `BAMBOO_GRASS_*` 50 · `ROCK_PEBBLE_*` 20 | 8 | Dốc ≤ 15° · cách lối đi ≥ 1m | Đất nện · cỏ thưa · đá lát · bùn |
| `BIO_V1_RIVERBANK` | F02 | `RIVER_REED_*` 40 · `RIVER_ROCK_S/M` 15 · `ROCK_PEBBLE_*` 25 · `RIVER_WILLOW_*` 5 · `BAMBOO_GRASS_*` 15 | 14 | Trong 0–8m từ mép nước · không trên mặt nước | Cát ướt · cát khô · cỏ ven sông · đá cuội |
| `BIO_V1_ROCKY_SLOPE` | F03, vách chung | `ROCK_BOULDER_S/M` 25 · `ROCK_PEBBLE_*` 45 · `BAMBOO_GRASS_*` (cỏ khô) 20 · `MINE_SPOIL_HEAP_*` 10 | 10 | Dốc 10–45° | Đá xám · đất nâu · sỏi · cỏ khô |
| `BIO_V1_MINE_YARD` | F03 | `MINE_SUPPORT_BEAM` 10 · `MINE_CART` 5 · `MINE_CRYSTAL_*` 10 · `ROCK_PEBBLE_*` 50 · `VILLAGE_CRATE_*` 25 | 6 | Dốc ≤ 15° · cách lối đi ≥ 1,5m | Đất mỏ · sỏi đen · ván gỗ · bụi đá |
| `BIO_V1_MARSH` | F04, HD01 | `SWAMP_MANGROVE_*` 15 · `SWAMP_DEADTREE_*` 10 · `SWAMP_REED_TALL_*` 15 · `SWAMP_ALGAE_*` 30 · `SWAMP_TOXIC_SHROOM_*` 10 · `ROCK_BOULDER_S` 20 | 12 | Không trên đường ván · cách POI ≥ 3m | Bùn · rêu · nước nông · lá mục |
| `BIO_V1_BLOSSOM_VALLEY` | F05 | `BLOSSOM_PEACH_TREE_*` 15 · `BLOSSOM_FLOWER_*` 35 · `BLOSSOM_PETAL_CARPET_*` 30 · `ROCK_BOULDER_S` 10 · `BAMBOO_GRASS_*` 10 | 15 | Dốc ≤ 25° · cách lối đi ≥ 2m | Cỏ hồng · cánh hoa · đất đỏ nhạt · đá lối |
| `BIO_V1_ASH_FIELD` | F06, HD02 | `BATTLE_BROKEN_SWORD_*` 25 · `BATTLE_ARMOR_PILE_*` 5 · `BATTLE_ASH_TREE_*` 5 · `ROCK_PEBBLE_*` 40 · `BATTLE_CRATER_*` 5 · `BATTLE_BANNER_TORN_*` 5 · `ROCK_BOULDER_S` 15 | 9 | Dốc ≤ 30° · cách lối đi ≥ 1,5m | Tro · đất đỏ sét · đá vỡ · cỏ cháy |

```jsonc
// tools/map-editor/biomes/BIO_V1_BAMBOO_GROVE.json (kiểu BiomeTemplate — TD-13 MF-R-033; dữ liệu chỉ cho editor, không vào bundle runtime)
{ "id": "BIO_V1_BAMBOO_GROVE", "region": "V1", "palette": "PAL_V1_GREEN",
  "allowedKits": ["KIT_V1_BAMBOO", "KIT_V1_ROCK"],
  "pieces": [ { "assetId": "PRP_BAMBOO_STALK_01", "weight": 9 }, { "assetId": "PRP_BAMBOO_STALK_02", "weight": 9 },
              { "assetId": "PRP_BAMBOO_CLUMP_01", "weight": 5 }, { "assetId": "PRP_BAMBOO_GRASS_01", "weight": 15 } /* … */ ],
  "densityPer100m2": 18, "slopeMaxDeg": 30, "minDistToPathM": 2, "minDistToPoiM": 3,
  "scaleJitter": 0.15, "rotationStepDeg": 15,
  "splatLayers": ["TER_V1_DAMP_SOIL", "TER_V1_BAMBOO_LITTER", "TER_V1_GRASS", "TER_V1_PATH_STONE"] }
```

---

## 8. Điểm nhấn và điểm mốc
- Mỗi bản đồ: **1 điểm mốc lớn** nhìn thấy từ hầu hết vị trí (GD-21 §3.4) + tối đa 2–3 mảnh `ACCENT` vẽ riêng (AR-01 §8). Địa điểm sơn môn tính là điểm mốc (HM-R-051).
- Điểm nhấn đã xác định từ lore: miếu hoang (F01), Khâm Thiên Đài, cổng Thanh Vân Tông, Tàng Kinh Các, Luân Hồi Đài (HUB), cổng Vực 2 (F06), Trấn Hồn Đài (HD02).
- **`V1_F01`:** điểm mốc = cột linh khí của linh mạch `V1_F01_LEY_001` tại `[150, 160]` — `PRP_POI_LEY_STONE` + `VFX_POI_LEY_LINE` (GD-16 `V1_F01` §1, không texture riêng).
- **Điểm mốc các bản đồ dã ngoại khác chưa được GD-16 / GD-21 chốt** — GD-21 chỉ nêu loại (tháp cổ, cây cổ thụ, thác nước, cột sáng linh mạch). Tài liệu này **không** tự chọn; kit đã có sẵn ứng viên (`PRP_ROCK_TREE_ANCIENT_*`, `PRP_ROCK_WATERFALL_H6`, `PRP_BLOSSOM_PEACH_ANCIENT`, `PRP_POI_LEY_STONE` + VFX) — KIT-Q-1.
- Điểm phát VFX không khí (AR-04 §8.1) đặt trong scene như node rỗng tên `vfx:{VFX_ID}` (AR-02 AST-R-043), không phải prop.

---

## 9. Ngân sách (trích TD-11 — nguồn số là TD-11)
| Hạng mục | Ngân sách | Nguồn |
|----------|-----------|-------|
| Prop môi trường | 500–1.500 tam giác (lớn / kiến trúc ≤ 4.000); atlas gradient chung; đồ đặc biệt 256–512² | TD-11 §3 |
| Bản đồ dã ngoại / thành trấn / instance — tổng tải | ≤ 5 / ≤ 6 / ≤ 2 MB | TD-11 §3.1 |
| Draw call môi trường trong khung (Trung) | ≤ 150 (dã ngoại, thành trấn), ≤ 100 (instance) | TD-11 §3.1 |
| Tam giác môi trường trong khung | ≤ 300k (dã ngoại, thành trấn), ≤ 200k (instance) | TD-11 §3.1 |
| Đa giác va chạm | ≤ 2.000 / ≤ 2.500 / ≤ 800 | TD-11 §3.1 |
| Theo preset | Draw call môi trường Thấp ≤ 80 · Cao ≤ 180; tam giác môi trường Thấp ≤ 150k · Cao ≤ 400k | TD-11 §2.1 |

- <a id="kit-r-040"></a>**KIT-R-040** Để giữ draw call: mỗi chunk dùng **≤ 6 vật liệu** (atlas chung, foliage, nước, địa hình, ≤ 2 texture điểm nhấn — đề xuất); mọi mảnh `MODULE` / `STRUCTURE` dùng atlas chung được gộp vào một mesh / chunk (CL-R-064); `SCATTER` dùng thin instance.
- <a id="kit-r-041"></a>**KIT-R-041** Mảnh `SCATTER` dưới 500 tam giác là **hợp lệ**: 500–1.500 của TD-11 được hiểu là khoảng điển hình với trần 1.500, không phải sàn (KIT-Q-7).

---

## 10. Ước lượng số mảnh theo phase
Số **mảnh duy nhất** (tính cả biến thể `_nn`), cộng dồn; PLAN §6.2 đặt 50–150 prop / kit.

| Phase (PLAN §7) | Nội dung bản đồ | Kit / phần mới | Mảnh mới | Cộng dồn |
|-----------------|-----------------|----------------|:---:|:---:|
| 0 | Góc Thanh Trúc Lâm 40×30m (AR-01 §10 #3), spike pipeline AI 5 prop | `BAMBOO` lõi, `ROCK` lõi, atlas v0, `PAL_V1_GREEN` | ~25 | ~25 |
| 1 | `V1_F01` hoàn chỉnh + 1 bí cảnh nhỏ + hiểm địa thử nghiệm | `BAMBOO` đủ (gồm `STALK_FAR`), `VILLAGE` (thôn), `ROCK` đủ, `RUINS` (miếu hoang, bia), `CM_POI` lõi, `MINE_ORE_NODE` (quặng F01), `HAZARD_TEST`, phần hầm của bí cảnh (KIT-Q-4) | ~115 | ~140 |
| 2 | HUB + 3 dã ngoại (KIT-Q-4) | `VILLAGE` đủ, `THANHVAN`, `RIVER`, `RUINS` (Luân Hồi Đài, Bia Vạn Cổ), `TRIAL` (Tàn Hồn Cảnh), bảng màu 4 bản đồ | ~190 | ~330 |
| 3 | Đủ Vực 1: 6 dã ngoại + 3 bí cảnh + HD01, HD02 | `MINE` đủ, `SWAMP`, `BLOSSOM`, `BATTLE`, `HAZARD`, `CM_POI` (cửa động đặt sẵn, cổng Vực 2, lò rèn), `TRIAL_TRIB_ARENA`, `SITE_VACANT_STELE`, `TACTHIEN_BANNER` | ~230 | ~560 |
| 4 (Alpha) | Chế công pháp, Diễn Pháp Tràng | `POI_INSIGHT_STONE`, `TRIAL_SANDBOX_HALL`, sửa theo telemetry | ~20 | ~580 |
| 5a (Beta 1) | Tạc Thiên Bang, Hợp Hoan Tông, động phủ, trú địa | `TACTHIEN`, `HOPHOAN`, `CM_CAVE`, `CM_GROUNDS` | ~140 | ~720 |
| 5b (Beta 2) | Vực 2 (≥ 4 bản đồ + 2 bí cảnh), sơn môn, sự kiện | `CM_SITE`, kit V2 (~60% tái dùng), `RIVER_GUARD_LANTERN` | ~180 | ~900 |

---

## 11. Thôn trấn ban đêm, thời tiết, ngự kiếm — ràng buộc kit
- <a id="kit-r-045"></a>**KIT-R-045** Mảnh có nguồn sáng (đèn lồng, cửa sổ, lửa trại) dùng nhóm `EMISSIVE_WARM` bật theo pha `NIGHT` / `DUSK` (`DayPhase`, glossary) qua uniform — không thêm đèn động cho từng prop (TD-05 CL-R-061: một `DirectionalLight` + `HemisphericLight`).
- <a id="kit-r-046"></a>**KIT-R-046** Mặt đất / mái có biến thể "ướt" bằng tham số shader khi `RAIN` / `STORM` (không mảnh riêng).
- <a id="kit-r-047"></a>**KIT-R-047** Vùng bay được ở bản đồ `F` phải có điểm đi được trong 25m (FLY-V-02): khi dựng mảng rừng rậm / đầm sâu lớn hơn 50m bằng kit, level designer khai báo `NO_FLY` hoặc chừa khoảng trống — `pnpm maps:build` kiểm (MF-V-13).

---

## 12. Quy trình sinh bằng AI: TRELLIS.2 → Blender script

Chỉ dùng công cụ / mô hình đã đăng ký TD-15 §4.5 (TRELLIS.2, SDXL / FLUX.1 schnell qua ComfyUI, Blender, glTF-Transform; Kenney / Quaternius / KayKit CC0 cho bản thử). Công cụ mới phải đăng ký trước (AGENTS §4 luật 9).

| Loại mảnh | Cách làm chính |
|-----------|----------------|
| Hữu cơ / điêu khắc (đá, cây, gốc, tượng, bia, miếu hoang, điểm nhấn) | Ảnh concept → **TRELLIS.2** → Blender script |
| Module kiến trúc chính xác lưới (tường, mái, bậc, hầm, ván, vách module) | **Script Blender do AI viết** (bmesh / Geometry Nodes) sinh hình học theo kích thước chuẩn §2 — TRELLIS.2 chỉ cho chi tiết trang trí gắn thêm |
| Rải nhỏ (cỏ, lá, sỏi, hoa) | Script Blender (thẻ / mesh thấp) hoặc gói CC0 đã đăng ký → chuẩn hóa |
| Bản thử (blockout) | Gói CC0 (Kenney / Quaternius / KayKit — xác minh từng gói KayKit, TD-15) |

```
1. Brief          : assetId, kit, category, footprint / heightM, tam giác mục tiêu, MaterialGroup, hồ sơ cạnh (§4–§5)
2. Concept        : mô hình vẽ ảnh (TD-15) + LoRA phong cách dự án; prompt mẫu:
                    "stylized low-poly ancient Chinese {vật}, 3/4 top-down view, flat gradient colors, toon, isolated"
                    — không tên họa sĩ còn sống / tên game khác (ART-R-050)
3. Ảnh → 3D       : TRELLIS.2 → GLB (PBR) — chỉ loại mảnh "hữu cơ"
4. Blender script : tools/art/blender_export.py (AR-02 §8) + bước kit tools/art/kit_conform.py (AR-02 §8 bước 2b):
   4.1 nhập, đặt Z lên, scale về heightM; xóa mảnh rời / mặt trong; weld
   4.2 decimate về tam giác mục tiêu (≤ trần TD-11 §3)
   4.3 bake màu → lượng tử hóa về MaterialGroup gần nhất + bậc sáng → remap UV vào ô atlas ENV_GRADIENT (§6.1); sửa tay nếu sai nhóm
   4.4 pivot tâm đáy; MODULE: ép footprint về bội số 0,5m (co giãn trục ≤ 5%, quá thì từ chối) và kiểm KIT-R-011
   4.5 sinh collisionTemplate mặc định theo bảng §2.3: CIRCLE r = bán kính mặt cắt thân ở độ cao 0,5m;
       POLYGON = bao lồi mặt cắt chân, đơn giản hóa ≤ 12 đỉnh, CCW; cờ theo loại; fly theo KIT-R-015
   4.6 ghi extras (collisionTemplate, kit, category, edges, droppable, roofHide); tách node mái
   4.7 xuất GLB (+Y Up) và cập nhật art/env/{kit}/kit.json
5. pnpm art:optimize  : glTF-Transform (Meshopt, KTX2 ETC1S cho atlas — AST-R-040)
6. pnpm art:check     : ngân sách TD-11, tên, pivot / scale, collisionTemplate hợp lệ, kiểm kit (§14), hồ sơ nguồn gốc
7. Xem trước          : ảnh đúng camera game trong cảnh chuẩn (AR-01 §9: Thanh Trúc Lâm, giờ Ngọ + giờ Tý)
                        + ảnh top-down có lớp va chạm phủ lên + ảnh ở 2 bảng màu khác nhau → art director duyệt
8. Hồ sơ nguồn gốc    : art/provenance/{assetId}.json (công cụ, mô hình + license, prompt, seed, đầu vào, người sửa, người duyệt)
```

- <a id="kit-r-050"></a>**KIT-R-050** Mảnh từ TRELLIS.2 **luôn** qua bước 4.3 (về atlas chung) — không giữ texture PBR của TRELLIS.2 trong game (AST-R-040: không normal / roughness riêng cho toon), trừ `ACCENT` được duyệt.
- <a id="kit-r-051"></a>**KIT-R-051** Va chạm sinh tự động ở 4.5 là **đề xuất**; người duyệt xem ảnh top-down và sửa trước khi merge. `collisionTemplate` là một phần hợp đồng logic bản đồ (đổi va chạm ⇒ đổi `logicHash`, MF-AC-007).

---

## 13. Danh sách đồng bộ (cần sửa ở tài liệu khác)

| Tài liệu | Việc cần đồng bộ |
|----------|------------------|
| `docs/README.md` §1 | Thêm dòng AR-06 Environment Kits, prefix `KIT`, 🟡 v0.1 |
| PLAN §8 | Đánh dấu AR-06 đã có bản nháp; PLAN §2.2 dùng tên Vực 2 / 3 là "Vạn Thú Sơn", "Huyết Sa", vênh LR-01 §4.1 (Xích Viêm Vực, Hàn Băng Vực) |
| **TD-07** ✅ *(đã đồng bộ TD-07 v0.9)* | File `world/env_palettes.json` (kiểu `EnvPaletteDef`, vào registry DT-R-100, bundle client); validator: `MapDef.ambient.palette` tồn tại; mẫu ID `PAL_*`, `BIO_*`, `KIT_*`, `PRP_*` (bảng §4.3) → registry `"visibility": "client"` (DT-R-057, DT-R-101), validator `ar06` (KIT-R-031), bảng tiền tố DT-R-051; tồn tại `palette` kiểm ở TD-13 MF-V-14 |
| **TD-13** ✅ *(đã đồng bộ TD-13 v0.7)* | E1: hồ sơ cạnh + đơn vị module 2m; E11: định dạng mẫu biome §7 và nơi lưu (`tools/map-editor/biomes/`); quy tắc giảm rải tất định KIT-R-036; prop `DYNAMIC` không vào chunk (KIT-R-017); kiểm `NO_FLY` cho mảng rừng rậm (KIT-R-047) → MF-R-032..036, MF-V-13..17. Vênh tam giác thành trấn 350k / 300k đã sửa ở TD-13 v0.6 (≤ 300k) |
| **AR-02** ✅ *(đã đồng bộ AR-02 v0.3)* | Khóa `extras` mới (`kit`, `category`, `edges`, `droppable`, `roofHide`, `vfx:*`); mẫu tên `KIT_CM_*`, texture địa hình `TER_*`, atlas `ENV_GRADIENT_ATLAS`; script `tools/art/kit_conform.py`; cho phép nhiều `collisionTemplate` / node hay không (KIT-Q-6) → AST-R-043, §3 (`KIT_CM_*`, `ENV_*`, `TER_*`), §8 bước 2b; một `collisionTemplate` / node |
| TD-05 | Cách shader `ToonMaterial` tra nhóm vật liệu + bảng màu (KIT-R-030); `roofHide`; nhóm phát sáng theo `DayPhase` |
| GD-31 | Ngưỡng `fly` KIT-R-015 (≥ 6m và khung ≥ 2m) |
| TD-14 | Không đổi (dùng đúng `CollisionLayer`, 3–12 đỉnh) |
| GD-30 | `CaveBuilding` có `THU_VIEN` nhưng HM-R-030 ghi "bốn công trình" — thống nhất; tên mảnh `facadePrefabs` / `ruinsPrefab` = `PRP_SITE_*` |
| GD-21 | Ví dụ JSON §7 dùng `"type": "seam"` (chữ thường), `safeZones`, `weatherOverride` ở gốc — vênh `MapDef` TD-13 (chỉ ghi nhận; TD-13 thắng); "Huyết Sa" ở §3.9 không có trong LR-01 |
| LR-01 | Tên tông môn tu hỏa Vực 2; xác nhận vật trang trí ở §4 (thuyền, giếng, giàn mỏ…) không cần đăng ký tên |
| AR-01 | Hex bảng màu Vực 1 §6.2; nhóm vật liệu §6.1 |
| Glossary | (khi duyệt) `MaterialGroup`, `KitCategory`, `EdgeProfile` |

---

## 14. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="kit-ac-001"></a>KIT-AC-001 | Mảnh `MODULE` có footprint 2,13 × 0,5m | `art:check` | Fail (không phải bội số 0,5m, KIT-R-010) |
| <a id="kit-ac-002"></a>KIT-AC-002 | Hai `PRP_VILLAGE_WALL_2M` đặt kề theo lưới | Script kiểm ghép | Khe hở ≤ 0,01m, không mặt chồng (KIT-R-011) |
| <a id="kit-ac-003"></a>KIT-AC-003 | Prop có pivot ở góc thay vì tâm đáy | `art:check` | Fail (AR-02 §2) |
| <a id="kit-ac-004"></a>KIT-AC-004 | Mảnh `STRUCTURE` dùng texture riêng, không thuộc `ACCENT` | `art:check` | Fail (KIT-R-003) |
| <a id="kit-ac-005"></a>KIT-AC-005 | `PRP_ROCK_CLIFF_8M_H6` có `fly: false` | `art:check` | Fail (vách ≥ 6m phải `fly: true`, KIT-R-015) |
| <a id="kit-ac-006"></a>KIT-AC-006 | `collisionTemplate` `POLYGON` 14 đỉnh / không lồi / CW | `art:check` | Fail (3–12 đỉnh, lồi, CCW — TD-14 §3.3) |
| <a id="kit-ac-007"></a>KIT-AC-007 | Prop thường 1.800 tam giác / kiến trúc 4.500 tam giác | `art:check` | Fail (TD-11 §3) |
| <a id="kit-ac-008"></a>KIT-AC-008 | Mảnh cây không có `collisionTemplate` | `art:check` | Cảnh báo + gán mặc định `CIRCLE` (khớp AST-AC-004) |
| <a id="kit-ac-009"></a>KIT-AC-009 | Cùng chunk GLB, đổi `MapDef.ambient.palette` `PAL_V1_GREEN` → `PAL_V1_ASH` | Nạp bản đồ | Màu đổi theo bảng; không tải thêm texture nào (KIT-R-030) |
| <a id="kit-ac-010"></a>KIT-AC-010 | `MapDef.ambient.palette = "PAL_V1_XYZ"` không có trong `env_palettes.json` | `maps:build` | Fail |
| <a id="kit-ac-011"></a>KIT-AC-011 | Mẫu biome tham chiếu `PRP_*` không tồn tại hoặc ngoài `allowedKits` | Validator editor / CI | Fail |
| <a id="kit-ac-012"></a>KIT-AC-012 | Cọ rải `BIO_V1_BAMBOO_GROVE` trên vùng có đa giác va chạm và lối đi | Rải | Không mảnh nào nằm trong va chạm hay cách lối đi < 2m; mảnh có va chạm sinh shape trong lớp va chạm |
| <a id="kit-ac-013"></a>KIT-AC-013 | Preset Thấp | Nạp `V1_F01` | Số mảnh `SCATTER` `droppable` ≈ 30% (± 2%), mảnh có va chạm giữ 100%; hai client preset Thấp giữ cùng tập mảnh |
| <a id="kit-ac-014"></a>KIT-AC-014 | Bẫy `TRP_POISON_VENT` có trong GLB chunk của `V1_HD01_L1` | `maps:build` | Fail (KIT-R-017) |
| <a id="kit-ac-015"></a>KIT-AC-015 | `SECT_SITE` thiếu `PRP_SITE_FACADE_L4` | Validator GD-30 | Fail (đủ 5 `facadePrefabs` + `ruinsPrefab`) |
| <a id="kit-ac-016"></a>KIT-AC-016 | Mảnh kit không có `art/provenance/{assetId}.json` | CI | Fail (AST-R-005) |
| <a id="kit-ac-017"></a>KIT-AC-017 | Góc Thanh Trúc Lâm mẫu Phase 0, preset Trung, `REF-PC-LOW` | Đo trong khung | Draw call môi trường ≤ 150, tam giác môi trường ≤ 300k (TD-11) |
| <a id="kit-ac-018"></a>KIT-AC-018 | Mảnh có node mái `roofHide` | Nhân vật đi vào trong nhà | Mái ẩn (CAM-R-013), va chạm không đổi |
| <a id="kit-ac-019"></a>KIT-AC-019 | `env_palettes.json` có nhóm `EMISSIVE_COOL` lệch < 30° sắc độ so với `VPAL_LOI` mà AR-01 không chỉ định | `gamedata:check` | Cảnh báo (KIT-R-031) |
| <a id="kit-ac-020"></a>KIT-AC-020 | Mảnh có va chạm được đặt với scale 1,2 | Editor / `maps:build` | Từ chối (scale cố định 1, §2.1) |

---

## 15. Câu hỏi mở
- [ ] **KIT-Q-1:** Điểm mốc lớn của từng bản đồ dã ngoại Vực 1 (GD-21 §3.4) là gì? Chờ GD-16 level design. Mặc định: dùng ứng viên §8, level designer chọn khi blockout. *(`V1_F01` đã chốt 2026-10-25: `PRP_POI_LEY_STONE` + `VFX_POI_LEY_LINE` — GD-16 `V1_F01` §1; còn F02–F06.)*
- [ ] **KIT-Q-2:** Tên chính thức Vực 2–4 (LR-01 WB-Q-1) vs PLAN §2.2 ("Vạn Thú Sơn", "Huyết Sa") — kit V2+ đặt theo LR-01 tới khi chốt.
- [ ] **KIT-Q-3:** Tông môn tu hỏa ở Vực 2 (LR-01 §4.1) chưa có tên / ngôn ngữ hình dạng → chưa lên danh sách mảnh.
- [ ] **KIT-Q-4:** Bí cảnh nhỏ của Phase 1 là bí cảnh nào, và "3 bản đồ dã ngoại" của Phase 2 là những bản đồ nào (PLAN §7)? Ảnh hưởng thứ tự làm `MINE` / `RIVER` / `BLOSSOM`. Mặc định: F01–F03, bí cảnh Phase 1 dựng bằng `ROCK` + `RUINS`.
- [ ] **KIT-Q-5:** Ngưỡng `fly` (≥ 6m và khung ≥ 2m) có đúng ý GD-31? Rừng trúc cao 6–8m để `fly: false`? Mặc định: KIT-R-015.
- [x] **KIT-Q-6:** Cho một node có **nhiều** `collisionTemplate` (nhà chữ L) hay bắt buộc tách mảnh? Mặc định: tách mảnh (KIT-R-016). *(Đã chốt 2026-10-25 theo mặc định: AR-02 v0.3 AST-R-043 — tối đa một `collisionTemplate` mỗi node, mảnh chân lõm tách nhiều mảnh.)*
- [ ] **KIT-Q-7:** "500–1.500 tam giác" của TD-11 có phải sàn? Mặc định: chỉ trần (KIT-R-041).
- [ ] **KIT-Q-8:** Atlas độ sáng + bảng màu ramp theo nhóm (KIT-R-030) hay atlas màu thật + LUT theo vực? Chốt ở spike Phase 0 với TD-05. Mặc định: KIT-R-030.
- [ ] **KIT-Q-9:** Lục độc F04 / lam oán linh F06 (AR-01) gần màu nguyên tố Mộc / Thủy — chấp nhận vì AR-01 chỉ định, hay lệch sắc độ? Mặc định: giữ AR-01, KIT-R-031 chỉ cảnh báo.
- [ ] **KIT-Q-10:** Prefab mặt tiền sơn môn cấp 5 gồm nhiều mảnh vượt 4.000 tam giác tổng — tính ngân sách theo mảnh hay theo prefab (HM-R-062)? Mặc định: theo mảnh, prefab chịu ngân sách bản đồ.
- [ ] **KIT-Q-11:** Quỷ Thị (chợ đêm, GD-21 §3.8, Beta) đặt ở bản đồ nào — cần kit sạp đêm riêng không? Mặc định: dùng `PRP_VILLAGE_STALL_*` + `EMISSIVE_WARM`.
- [x] **KIT-Q-12:** Mẫu biome lưu ở `tools/map-editor/biomes/` (chỉ editor) hay trong `shared/` để CI kiểm chung với bản đồ? Mặc định: `tools/map-editor/biomes/`, CI vẫn chạy validator KIT-AC-011. *(Đã chốt 2026-10-25: TD-13 v0.7 MF-R-033 — `tools/map-editor/biomes/{biomeId}.json`, kiểu `BiomeTemplate`, CI kiểm bằng MF-V-15.)*
- [ ] **KIT-Q-13:** LOD cho prop lớn / kiến trúc (AST-Q-3) — áp cho `ACCENT` ≥ 3.000 tam giác? Mặc định: không LOD ở camera cố định, đo lại ở Phase 1.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến (grid snap, mảnh module, atlas gradient chung, va chạm + `fly`, đọc được ở camera game, ngân sách TD-11, hồ sơ nguồn gốc), quy chuẩn lắp ghép (lưới 0,5m / 15°, module 2m, pivot, hồ sơ cạnh, va chạm theo loại mảnh, prop động không vào chunk), danh mục / đặt tên / dữ liệu mảnh, 13 kit Vực 1 + 5 kit dùng chung với danh sách mảnh (mã, mô tả, tam giác, `collisionTemplate`, `fly`, ưu tiên, phase), thôn trấn, ba đại tông, sơn môn, trú địa, động phủ, thử thách, hiểm địa, atlas + 16 nhóm vật liệu, bảng màu Vực 1 + sơ bộ Vực 2–4, 8 mẫu biome cho cọ rải E11, điểm nhấn, ngân sách, ước lượng số mảnh theo phase, quy trình TRELLIS.2 → Blender script, danh sách đồng bộ, 20 tiêu chí nghiệm thu, 13 câu hỏi mở |
| v0.2 | 2026-10-25 | — | Đánh dấu đã đồng bộ §13 với TD-07 v0.9 (registry `env_palettes.json`, tiền tố), TD-13 v0.7 (hồ sơ cạnh, `BiomeTemplate`, giảm rải tất định, không nướng prop động / vị trí `@serverOnly` vào chunk) và AR-02 v0.3 (khóa `extras`, `KIT_CM_*`, `ENV_*`, `TER_*`, `kit_conform.py`); bỏ nhãn "đề xuất" ở các mục đã có tài liệu chủ quản; đóng KIT-Q-6, KIT-Q-12 |
| v0.3 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: `PRP_BAMBOO_STALK_FAR_01..02` (trúc lấp lõi khối chặn, không va chạm) + KIT-R-048; `V1_F01` dùng `PRP_MINE_ORE_NODE_*` (kit phụ `MINE`, mảnh dời sang Phase 1); §8 / KIT-Q-1 chốt điểm mốc `V1_F01` = `PRP_POI_LEY_STONE` + `VFX_POI_LEY_LINE` |

