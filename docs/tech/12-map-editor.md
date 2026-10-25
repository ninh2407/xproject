# TD-13 — Map Format & Map Editor

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-13 · Prefix ID: `MF` |
| Owner | Tech Lead (Client) + Level Designer + Tech Artist |
| Trạng thái | 🟡 Bản nháp v0.9 |
| Cần trước | Phase 0 (map editor bản thô, xuất GLB + JSON), Phase 1 (bản đồ `V1_F01` đầy đủ) |
| Quyết định nền | D-01 (2.5D, logic 2D), D-02, PLAN Q22 (mở rộng Babylon.js Editor hay tự viết — chốt sau spike), [TD-07](06-gamedata.md) DT-R-011 (dữ liệu bản đồ ở `shared/maps`), [TD-14](13-geom2d.md) §3.3 (lớp va chạm) |
| Liên quan | [GD-21 Open World](../design/22-open-world.md) §3.1–3.5, §7 · [TD-12 World & Map](11-world-maps.md) · [TD-14 Geometry](13-geom2d.md) · [TD-07 Gamedata](06-gamedata.md) · [GD-29 RNG](../design/05-rng-fairness.md) (`serverOnly`) · [GD-12](../design/18-enemies-ai.md) (spawn) · [GD-28](../design/27-danger-zones.md) (layout hiểm địa) · [GD-30](../design/28-caves-sect-sites.md) (cửa động, sơn môn) · [GD-32](../design/31-pvp-infamy.md) (cờ PvP) · AR-06 Environment Kits · PLAN §5.2 (ngân sách hiệu năng) |

> Một bản đồ gồm **hai phần tách bạch**: **hình ảnh** (mesh, texture, ánh sáng — chỉ client cần) và **logic** (va chạm, POI, khu vực, kết nối, spawn — server quyết định, client dùng để dự đoán / hiển thị). Tài liệu này định nghĩa định dạng cả hai, công cụ tạo ra chúng và quy trình xuất — không phụ thuộc việc chọn editor (Q22).

---

## 1. Tệp của một bản đồ

```
art-source/maps/V1_F01/                 # nguồn (Git LFS): scene editor, .blend kit
shared/maps/V1_F01.json                 # LOGIC — nguồn sự thật, xuất từ editor, review trong PR
shared/maps/V1_F01.preview.png          # ảnh chụp top-down (tự sinh) để review PR
build/maps/V1_F01/                      # (sinh ra, không commit)
  ├─ logic.server.json                  # đầy đủ (TD-07 bundle.server)
  ├─ logic.client.json                  # bỏ trường @serverOnly (GD-29 RF-R-041)
  ├─ navmesh.r040.bin / r100.bin / r200.bin   # recast theo cỡ thân (TD-14 §8)
  ├─ chunks/c_{cx}_{cz}.glb             # hình ảnh theo chunk 32×32m (Meshopt + KTX2)
  ├─ height.r16                         # heightmap 16-bit cho đặt y hiển thị (client)
  ├─ minimap.webp                       # nền bản đồ thế giới / minimap
  └─ manifest.json                      # { mapId, logicHash, assetHash, chunks[], sizes }
```

- <a id="mf-r-001"></a>**MF-R-001** **`shared/maps/{mapId}.json` là nguồn sự thật duy nhất của logic bản đồ.** Mọi thứ khác sinh ra từ nó + scene nguồn. Không sửa tay file trong `build/`.
- <a id="mf-r-002"></a>**MF-R-002** Logic bản đồ **thuộc bundle gamedata** (cùng `gamedataVersion`, chốt TD-07 DT-Q-4); **hình ảnh** version riêng theo `assetHash` (CDN, cache vĩnh viễn) — sửa cây cối không bắt tải lại gamedata, sửa va chạm thì có.

---

## 2. Hệ tọa độ & đơn vị
Theo TD-14 §2: mét, `(x, z)`, gốc góc Tây Nam, `+x` Đông, `+z` Bắc; bản đồ nằm trong `[0, width] × [0, depth]`, ≤ 400m (GD-21). Đa giác đỉnh **ngược chiều kim đồng hồ**. Độ cao `y` chỉ có trong heightmap / mesh, logic không dùng.

---

## 3. Định dạng logic (`MapDef`)

```ts
// shared/gamedata-types/maps.ts  (TD-07: kiểu TS là nguồn → JSON Schema)
interface MapDef {
  id: string;                               // V1_F01 (GD-21 §3.1)
  type: 'HUB' | 'F' | 'HD' | 'BC' | 'KN' | 'DP' | 'TD' | 'TRIAL';
  region: string;                           // V1
  nameKey: string;                          // i18n
  size: [number, number];                   // [width, depth] mét
  realmRange: [string, string];             // ["LK1","LK3"]
  channelCapacity?: number;                 // ghi đè TD-12 (mặc định 150)
  flags: {
    pvpDisabled?: boolean;                  // GD-32 (V1_F01 = true)
    timeFlow?: 'NORMAL' | 'SLOW';           // HUB tuổi trôi ×0,25 (GD-26)
    fog?: boolean;                          // HUB / F: true; HD: per-run; BC: false
    weatherOverride?: string | null;        // vd. F04 "luôn có sương"
  };
  ambient: { lightingProfileId: string; musicId: string; palette: string };   // AR-01 / AU-01; palette = PAL_* trong world/env_palettes.json (AR-06 §6, MF-V-14)
  collision: CollisionLayer;                // TD-14 §3.3 (đa giác lồi / tròn, cờ move/projectile/sight)
  areas: AreaDef[];                         // khu vực
  zones: ZoneDef[];                         // vùng chức năng (an toàn, không Chấp Pháp, ...)
  connections: ConnectionDef[];
  pois: PoiDef[];
  spawnAreas: SpawnAreaDef[];               // SP_* (GD-12)
  respawnPoints: { id: string; pos: [number, number] }[];
  entrySpawn?: [number, number];            // instance
  /** @serverOnly */ secretCandidates: { group: string; positions: [number, number][]; poiTemplate: string }[];
  /** @serverOnly */ rareHerbGroups: { group: string; herbId: string; positions: [number, number][] }[];   // GD-21 OW-R-104
  landmark: { pos: [number, number] };
  chunks: { cell: 32; count: [number, number] };
}

interface AreaDef { id: string; nameKey: string; polygon: [number, number][]; realmHint?: string }   // {map}_A{nn}
interface ZoneDef {
  id: string; kind: 'SAFE' | 'NO_ENFORCER' | 'NO_PVP' | 'DORMANT_SAFE' | 'NO_FLY';   // NO_FLY: GD-31
  shape: { circle?: { c: [number, number]; r: number }; polygon?: [number, number][] };
}
type ConnectionDef =
  | { type: 'SEAM'; id: string; to: string; toConnectionId: string; segment: [[number, number], [number, number]] }
  | { type: 'GATE'; id: string; to: string; toConnectionId: string; trigger: [number, number]; radius: number; arrival: [number, number] }
  | { type: 'REGION_GATE'; id: string; to: string; toConnectionId: string; trigger: [number, number]; radius: number; arrival: [number, number]; realmMin: string; cutsceneId?: string }
  | { type: 'INSTANCE_DOOR'; id: string; instanceMapId: string; trigger: [number, number]; radius: number; confirmKey: string; realmMin?: string };
interface PoiDef {
  id: string;                               // {map}_{KIND}_{nnn}
  kind: PoiKind;                            // §4
  pos: [number, number];
  interactRadius?: number;                  // mặc định theo kind (§4)
  hiddenLevel?: number;                     // 0–5 (GD-21 §3.7)
  conditions?: { phase?: string[]; weather?: string[] };
  props?: Record<string, unknown>;          // theo kind (§4), có schema riêng từng kind
}
interface SpawnAreaDef {                     // vùng spawn quái theo khu (GD-12 §3, EN-R-030..032)
  id: string;                               // = SpawnTable.id `SP_{mapId}_{areaId rút gọn}` (GD-12); quái, trọng số, `pack`, `maxAlive`, `respawnSec` nằm ở SpawnTable, không ở bản đồ
  polygon: [number, number][];              // CCW, không giao vùng SAFE (MF-V-05)
  spawnPoints: [number, number][];          // điểm spawn đặt trong editor, nằm trong polygon; build chép sang SpawnTable.spawnPoints (GD-12 §3)
}
```

- <a id="mf-r-010"></a>**MF-R-010** Tham chiếu chéo bằng ID và được kiểm tra (TD-07 §5): `connections[].to` / `toConnectionId` tồn tại và **trỏ ngược lại** (biên hai chiều); mỗi `spawnAreas[].id` là một `SpawnTable.id` (`SP_*`) có trong `monsters/spawn_tables.json` với `mapId` trùng bản đồ, mọi `spawnPoints` nằm trong `polygon` (GD-12 EN-R-030); `SpawnGroupDef` (`SG_*`) **không** gắn vào bản đồ — chỉ do cơ duyên / sự kiện gọi (GD-12 EN-R-110, GD-23 `SPAWN_GROUP`); `props.npcId`, `props.shopId`, `props.instanceMapId`… tồn tại.
- <a id="mf-r-011"></a>**MF-R-011** **Biên (SEAM):** hai đoạn biên của một cặp nối có **cùng độ dài ± 1m** (ánh xạ tỷ lệ TD-12 WM-R-042); đoạn nằm trên rìa bản đồ.

---

## 4. Danh mục loại POI

| `PoiKind` | Bán kính tương tác mặc định | `props` bắt buộc | Nguồn |
|-----------|:---:|------------------|-------|
| `TELEPORT_ARRAY` | 3 | — (mỗi bản đồ mở 1, GD-21 quy tắc 4) | GD-21 §3.5 |
| `LEY_LINE` | 6 | `coef` (hệ số đả tọa) | GD-04, GD-21 |
| `MEDITATION_SPOT` | 3 | `coef` | GD-04 (điểm đả tọa ở HUB) |
| `HERB` | 1,5 | `herbId`, `tier` | GD-21, GD-10 |
| `ORE` | 1,5 | `oreId`, `tier` | GD-11 |
| `NPC` | 3 | `npcId`, `role` (`SHOP` / `QUEST` / `VAULT_KEEPER` / `BEAST_KEEPER` / `ELDER` / `FORECAST` / `BOUNTY_BOARD` / ...) , `shopId?` | GD-13, GD-14, GD-15, GD-25, GD-26, GD-32 |
| `ALCHEMY_STATION` · `FORGE_STATION` · `INSIGHT_STATION` | 3 | — | GD-10, GD-11, GD-08 |
| `REMNANT_ALTAR` | 3 | — | GD-26 §5.15 |
| `CAVE_GATE` | 3 | `gateGrade` (tự tính theo khoảng cách linh mạch, HM-R-011 — editor hiển thị) | GD-30 |
| `SECT_SITE` | 8 | `siteGrade`, `footprint` (đa giác mặt tiền) | GD-30 |
| `STELE` (bia / di tích) | 2 | `codexId` | GD-21 §3.6 |
| `CHEST` | 1,5 | `lootTableId` (`@serverOnly`) | GD-09 |
| `SECRET` | 1,5 | (sinh từ `secretCandidates`, không đặt tay) | GD-21 |
| `TRAP` | — | `trapId`, `shape` (`@serverOnly`) | GD-28 |
| `EXIT` (lối thoát hiểm địa) | 3 | `openFrom` (pha) | GD-28 |
| `SECT_HQ` | 20 (vùng) | `sectId` (tổng đàn tông NPC: an toàn, Hắc Thị, ...) | GD-14 §5.4b |
| `ENCOUNTER_POINT` (điểm cơ duyên) | — (kích hoạt theo `encounters/params.proximityRadius`, GD-22 §3 `POI_PROXIMITY`) | không bắt buộc; tùy chọn `tags?: string[]` (vd. `legacy_capable` — GD-22 ENC-R-090) | GD-22, GD-21 §3.4 (3–5 điểm mỗi dã ngoại) |
| `LANDMARK` | — | — | GD-21 |

- <a id="mf-r-020"></a>**MF-R-020** `PoiKind` là **danh sách đóng** (giá trị `UPPER_SNAKE`); GD mới cần loại POI mới phải thêm vào bảng này kèm schema `props`.
- <a id="mf-r-021"></a>**MF-R-021** ID POI theo mẫu `{mapId}_{KIND_RÚT_GỌN}_{nnn}` (GD-21): `TP`, `LEY`, `HERB`, `ORE`, `NPC`, `STN` (trạm), `ALTAR`, `GATE` (cửa động), `SITE`, `STELE`, `CHEST`, `TRAP`, `EXIT`, `HQ`, `LM`, `ENC` (điểm cơ duyên). **Không đổi ID** sau khi phát hành (lưu trong `discovered_poi`, TD-07 DT-R-002).

---

## 5. Map editor

### 5.1. Yêu cầu chức năng (độc lập lựa chọn Q22)

| # | Chức năng | Ghi chú |
|---|-----------|---------|
| E1 | Đặt prop từ **kit môi trường** (AR-06), snap lưới 0,5m / 15°, **gợi ý snap theo hồ sơ cạnh** (đơn vị module 2m, MF-R-032), đổi màu theo bảng màu bản đồ (`PAL_*`) | Hình ảnh |
| E2 | Sơn địa hình (heightmap + lớp texture splat) | Hình ảnh |
| E3 | **Lớp va chạm**: vẽ đa giác / hình tròn với cờ `move` / `projectile` / `sight` / `fly`; **tự sinh va chạm** từ prop có `collisionTemplate` (`CIRCLE` / `POLYGON` + cờ, định dạng chuẩn [AR-02 §7](../art/asset-guidelines.md) AST-R-050); tự tách đa giác lõm thành lồi | TD-14 GEO-R-023 |
| E4 | Đặt POI theo danh mục §4 với form `props` sinh từ schema | |
| E5 | Vẽ khu vực, vùng chức năng, vùng spawn; đặt điểm hồi sinh | |
| E6 | Kết nối: vẽ đoạn biên (tự bắt rìa), đặt cổng; chọn bản đồ đích và kết nối đích từ danh sách | MF-R-010, MF-R-011 |
| E7 | **Xem trước logic**: tô vùng đi được (sinh navmesh nhanh), vùng an toàn, vùng PvP, tầm 90s tới truyền tống trận (GD-21 quy tắc 4) dạng heatmap | |
| E8 | **Lớp phủ mật độ**: đếm POI / quái / bí mật theo diện tích, so với checklist GD-21 §3.4, đánh dấu vùng "trống" | |
| E9 | Chế độ **thử chạy**: thả nhân vật vào bản đồ với `shared/sim` (va chạm thật), camera game | |
| E10 | **Xuất** (§6) và chạy validator ngay trong editor, lỗi bấm vào nhảy tới vị trí | |
| E11 | **Cọ rải (scatter brush) theo quy tắc** (mật độ, độ dốc, khoảng cách tới đường đi / POI, không rải trong vùng va chạm) + **mẫu biome** `BIO_*` (rừng trúc, bờ sông, đầm lầy, hầm mỏ…: tập prop + tỷ lệ + bảng màu; định dạng và nơi lưu §5.3) để phủ bản đồ rộng nhanh; prop rải tự gắn `collisionTemplate` | PLAN §6.7 (D-22), AR-06 §7 |

- <a id="mf-r-030"></a>**MF-R-030** Editor **không** chứa logic gameplay riêng: va chạm, navmesh, xem trước dùng đúng `shared/sim` (TD-14) và tool build như CI.
- <a id="mf-r-031"></a>**MF-R-031** Spike Phase 0 (Q22): thử mở rộng **Babylon.js Editor** bằng plugin cho E3–E10 trong ≤ 5 ngày; nếu plugin API không đủ (đặc biệt E3, E7, E10) → tự viết editor web nhẹ (Babylon.js + React) chỉ cho phần logic, dùng Blender / Babylon.js Editor cho phần hình ảnh. Kết quả ghi Decision Log.

### 5.2. Quy trình làm một bản đồ
```
1. Level designer phác layout (giấy / editor): khu, đường, kết nối, POI chính — theo GD-16 (file level design)
2. Blockout trong editor: địa hình thô + va chạm + POI + kết nối → xuất → chơi thử (E9) → playtest nội bộ
3. Environment artist dựng hình ảnh bằng kit (E1, E2), giữ nguyên lớp logic (khóa lớp)
4. Validator xanh + ngân sách hiệu năng đạt (§7) → PR: diff logic JSON + ảnh preview
5. Merge → CI build chunks, navmesh, bundle → staging
```

### 5.3. Kit, hồ sơ cạnh, mẫu biome, rải (đồng bộ AR-06 §2, §3, §7)

- <a id="mf-r-032"></a>**MF-R-032** **Hồ sơ cạnh (E1):** editor đọc catalogue kit `art/env/{kit}/kit.json` (`KitDef` / `KitPieceDef`, AR-06 §3.3; sinh bởi script Blender, không sửa tay). Mảnh `MODULE` mang `edges` theo hướng `N`/`E`/`S`/`W` với giá trị `EdgeProfile` (danh sách đóng ở AR-06 §2.2 / §3.3: `WALL_05`, `CLIFF_H3`, `CLIFF_H6`, `CLIFF_H12`, `BANK`, `TUNNEL_4`, `BOARD_2`, `FENCE`, `STAIR_4`). Khi kéo mảnh tới gần mảnh đã đặt, editor gợi ý snap sao cho hai cạnh đối diện **cùng hồ sơ** và cùng chiều dài (bội số đơn vị module 2m: 2 / 4 / 8m); cạnh khác hồ sơ vẫn đặt được (không chặn) nhưng hiện cảnh báo. Hồ sơ cạnh là dữ liệu **hình ảnh**, không vào `MapDef`.
- <a id="mf-r-033"></a>**MF-R-033** **Mẫu biome (E11)** lưu ở `tools/map-editor/biomes/{biomeId}.json`, một file mỗi mẫu, tên file = `id` (`BIO_{VỰC}_{TÊN}`, TD-07 DT-R-051). Dữ liệu **chỉ cho editor**: không vào bundle gamedata, không vào `shared/maps`; CI vẫn kiểm (MF-V-15). Kiểu nguồn ở `tools/map-editor/src/biome.ts` (không thuộc `shared/gamedata-types` vì không phải gamedata runtime):

```ts
// tools/map-editor/src/biome.ts — đồng bộ AR-06 §7.2 (ví dụ JSON ở đó)
interface BiomeTemplate {
  id: string;                     // BIO_V1_BAMBOO_GROVE
  region: string;                 // V1 | CM
  palette: string;                // PAL_* — tồn tại trong world/env_palettes.json
  allowedKits: string[];          // KIT_* — mọi pieces[].assetId thuộc một trong các kit này
  pieces: { assetId: string; weight: number }[];   // PRP_*; weight nguyên > 0 (TD-07 DT-R-040 "Weight")
  densityPer100m2: number;        // mảnh / 100 m²
  slopeMaxDeg?: number; slopeMinDeg?: number;
  minDistToPathM?: number; minDistToPoiM?: number;
  waterBandM?: [number, number];  // chỉ rải trong khoảng cách tới mép nước (vd. bờ sông 0–8m)
  scaleJitter: number;            // tỷ lệ ±, chỉ áp cho mảnh không va chạm (AR-06 §2.1)
  rotationStepDeg: 15;            // KIT-R-001
  splatLayers: [string, string, string, string];   // 4 lớp TerrainSplat (TD-05 §5.3), tên TER_* (AR-02 §3)
}
```
- <a id="mf-r-034"></a>**MF-R-034** **Rải trong editor** là tất định theo **seed lưu trong scene** (mỗi nét cọ một seed): xuất lại cùng scene cho cùng kết quả (không dùng `Math.random`). Kết quả rải là dữ liệu hình ảnh; chỉ shape va chạm sinh từ mảnh có `collisionTemplate` đi vào `MapDef.collision` (KIT-R-037). Mảnh có va chạm luôn đặt với scale 1 (AR-06 §2.1; `scaleJitter` chỉ cho mảnh không va chạm). Rải lại không đổi ID POI (MF-R-021).
- <a id="mf-r-035"></a>**MF-R-035** **Giảm rải theo preset (runtime, KIT-R-036):** chỉ mảnh `SCATTER` có `droppable: true` bị giảm, theo hệ số mật độ của preset (TD-05 §9 "Mật độ cỏ / prop rải", đọc từ gamedata, không hard-code). Mảnh được giữ khi `hash32(key) / 2³² < densityMult`, với `key = "{mapId}:{round(x·2)}:{round(z·2)}:{assetId}"` (tọa độ mét của pivot, `round` half-up như TD-07 DT-R-132) và `hash32` = **FNV-1a 32-bit** trên UTF-8 của `key`. Hệ quả: mọi client cùng preset giữ **cùng tập** mảnh; tập của preset thấp là **tập con** của preset cao. Mảnh có va chạm, `MODULE`, `STRUCTURE`, `ACCENT`, `POI` không bao giờ bị giảm (KIT-R-004).
- <a id="mf-r-036"></a>**MF-R-036** **Không nướng vào chunk:** bước cắt chunk (§6 bước 4) **loại** (a) mọi node có `category = DYNAMIC` (AR-06 §3.1, KIT-R-017: bẫy, rương từ `secretCandidates`, lối thoát tạm, di hài…), và (b) mọi prop đặt trong bán kính 0,5m quanh một vị trí thuộc dữ liệu `@serverOnly` của `MapDef` (`secretCandidates[].positions`, `rareHerbGroups[].positions`, `props.shape` của `TRAP`). Các vật này được client sinh như thực thể khi server gửi; GLB chunk và `minimap.webp` không được làm lộ vị trí `@serverOnly` (GD-29 RF-R-041). Vi phạm → MF-V-16.

---

## 6. Xuất & build

```
editor export
  ├─ shared/maps/{mapId}.json (logic, sắp xếp khóa ổn định để diff đẹp)
  └─ art-source/maps/{mapId}/scene.*  (hình ảnh)
CI: pnpm maps:build {mapId|all}
  1. Validate MapDef (schema TD-07 + MF-V-01..17)
  2. Tách logic.server / logic.client theo @serverOnly
  3. Sinh navmesh theo bán kính (recast-navigation-js, TD-14 §8) từ lớp va chạm `move`
  4. Cắt scene thành chunk 32×32m (loại prop theo MF-R-036) → glTF-Transform: gộp mesh tĩnh, Meshopt, texture KTX2 (PLAN §5.2); SCATTER giữ dạng thin instance kèm cờ droppable (MF-R-035)
  5. Heightmap, minimap, ảnh preview
  6. manifest.json (logicHash, assetHash) → gộp logic vào bundle gamedata (TD-07 §8), đẩy asset lên CDN
```

- <a id="mf-r-040"></a>**MF-R-040** **Validator bản đồ** (chạy ở editor và CI, mã `MF-V-nn`):

| Mã | Kiểm tra | Nguồn quy tắc |
|----|----------|---------------|
| MF-V-01 | Va chạm: đa giác lồi, CCW, 3–12 đỉnh, cạnh ≥ 0,1m, trong biên bản đồ | TD-14 GEO-R-023 |
| MF-V-02 | Vùng đi được **liên thông** tới mọi kết nối, POI tương tác, điểm hồi sinh (navmesh r0,4) | TD-14 |
| MF-V-03 | Kết nối hai chiều; đoạn biên hai phía cùng độ dài ± 1m | MF-R-010, MF-R-011 |
| MF-V-04 | Bản đồ dã ngoại có 2–4 kết nối (không tính truyền tống trận) | GD-21 §3.3 quy tắc 1 |
| MF-V-05 | Mỗi điểm vào có vùng `SAFE` bán kính ≥ 20m; không vùng spawn giao vùng an toàn | GD-21 quy tắc 5 |
| MF-V-06 | Mọi điểm đi được tới truyền tống trận ≤ 90s chạy bộ (`moveSpeed` 6 m/s theo navmesh) | GD-21 quy tắc 4 |
| MF-V-07 | Chênh `realmRange` với bản đồ kề ≤ 2 tầng (trừ `REGION_GATE`) | GD-21 quy tắc 3 |
| MF-V-08 | Mật độ nội dung đạt checklist (điểm thu thập 15–30, ≥ 30% có điều kiện giờ / thời tiết, …) | GD-21 §3.4 |
| MF-V-09 | `FIELD` có 2–4 `CAVE_GATE`; `SECT_SITE` không giao đường nối / vùng an toàn | GD-30 HM-R-010, HM-AC-023 |
| MF-V-10 | Hiểm địa: mỗi layout luôn có ≥ 1 `EXIT` mở ở mọi pha | GD-28 DZ-AC-025 |
| MF-V-11 | `pvpDisabled` cho `V1_F01`; vùng `NO_ENFORCER` quanh Tạc Thiên Trại (`V1_F03`); vùng `SAFE` quanh mọi `TELEPORT_ARRAY` và kết nối | GD-32 |
| MF-V-12 | Trường `@serverOnly` không có trong `logic.client.json`; ID POI đúng mẫu, không trùng, không đổi so với bản phát hành trước | GD-29, MF-R-021 |
| MF-V-13 | Ngự kiếm (GD-31 FLY-V-02..04): thành trấn phủ `NO_FLY`; POI cổng / trạm / truyền tống / cửa instance nằm trong `NO_FLY`; biên bản đồ có `fly = true`; mọi điểm bay tới có điểm đáp đi được trong 25m (gồm mảng rừng rậm / đầm sâu > 50m dựng bằng kit — AR-06 KIT-R-047) | GD-31 |
| MF-V-14 | `ambient.palette` tồn tại trong `world/env_palettes.json` | AR-06 KIT-AC-010 |
| MF-V-15 | Mẫu biome `tools/map-editor/biomes/*.json`: đúng kiểu `BiomeTemplate`, `id` = tên file, mẫu `BIO_*`; `palette` tồn tại; mọi `pieces[].assetId` có trong `kit.json` của một kit thuộc `allowedKits`; `splatLayers` đủ 4 (chạy với `pnpm maps:build all` và khi đổi file biome) | MF-R-033, AR-06 KIT-AC-011 |
| MF-V-16 | Chunk GLB không chứa node `DYNAMIC` hay prop trong 0,5m quanh vị trí `@serverOnly` | MF-R-036, AR-06 KIT-AC-014 |
| MF-V-17 | Prop có `collisionTemplate` đặt với scale 1; shape va chạm sinh ra (E3) khớp `collisionTemplate` của mảnh sau xoay / tịnh tiến | AR-06 §2.1, KIT-AC-020 |

---

## 7. Ngân sách một bản đồ (PLAN §5.2)

| Hạng mục | Dã ngoại 300–400m | Thành trấn | Instance |
|----------|------------------|-----------|----------|
| Tổng tải xuống (chunks + texture riêng) | ≤ 5 MB | ≤ 6 MB | ≤ 2 MB |
| Logic JSON (client, gzip) | ≤ 150 KB | ≤ 150 KB | ≤ 50 KB |
| Draw call trong khung nhìn (**riêng môi trường**, nằm trong tổng ≤ 200 — TD-11) | ≤ 150 | ≤ 150 | ≤ 100 |
| Tam giác trong khung nhìn | ≤ 300k | ≤ 300k | ≤ 200k |
| Đa giác va chạm | ≤ 2.000 | ≤ 2.500 | ≤ 800 |
| Prop có va chạm | dùng `collisionTemplate`, không va chạm theo mesh | | |

- <a id="mf-r-050"></a>**MF-R-050** CI báo vượt ngân sách là **lỗi** (chặn merge) với tổng tải và va chạm; là **cảnh báo** với draw call / tam giác (đo lại trên máy tham chiếu).

---

## 8. Ví dụ (rút gọn)
```jsonc
{
  "id": "V1_F01", "type": "F", "region": "V1", "nameKey": "map.v1_f01", "size": [300, 300],
  "realmRange": ["LK1", "LK3"],
  "flags": { "pvpDisabled": true, "fog": true },
  "ambient": { "lightingProfileId": "LP_BAMBOO", "musicId": "BGM_V1_FIELD_CALM", "palette": "PAL_V1_GREEN" },
  "collision": { "bounds": { "width": 300, "depth": 300 }, "shapes": [
    { "id": "c001", "kind": "CIRCLE", "c": [52.5, 210], "r": 0.6, "flags": { "move": true, "projectile": true, "sight": false } } ] },
  "areas": [{ "id": "V1_F01_A01", "nameKey": "area.v1_f01_a01", "polygon": [[200,0],[300,0],[300,100],[200,100]] }],
  "zones": [{ "id": "z_entry_hub", "kind": "SAFE", "shape": { "circle": { "c": [290, 132], "r": 20 } } }],
  "connections": [{ "type": "SEAM", "id": "e_hub", "to": "V1_HUB", "toConnectionId": "w_f01", "segment": [[300, 120], [300, 145]] },
                  { "type": "SEAM", "id": "s_f03", "to": "V1_F03", "toConnectionId": "n_f01", "segment": [[30, 0], [55, 0]] }],
  "pois": [
    { "id": "V1_F01_TP_001", "kind": "TELEPORT_ARRAY", "pos": [160, 150] },
    { "id": "V1_F01_HERB_012", "kind": "HERB", "pos": [55, 210], "hiddenLevel": 1,
      "conditions": { "phase": ["NIGHT"] }, "props": { "herbId": "ITM_HERB_NGUYET_QUANG", "tier": "LINH" } },
    { "id": "V1_F01_ENC_001", "kind": "ENCOUNTER_POINT", "pos": [182, 112] }
  ],
  "spawnAreas": [{ "id": "SP_V1_F01_A01", "polygon": [[240,22],[280,22],[280,58],[240,58]],
                   "spawnPoints": [[245,27],[251,27],[257,27],[263,27],[269,27],[275,27],[245,40],[275,40]] }],   // trích; SpawnTable SP_V1_F01_A01 (GD-12 §7.1), đủ điểm ở GD-16 V1_F01 §6.1
  "respawnPoints": [{ "id": "rp_1", "pos": [280, 132] }],
  "secretCandidates": [{ "group": "chest_a", "positions": [[228, 92], [190, 118], [140, 70]], "poiTemplate": "CHEST_SMALL_V1" }],
  "rareHerbGroups": [],
  "landmark": { "pos": [150, 160] },
  "chunks": { "cell": 32, "count": [10, 10] }
}
```

---

## 9. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="mf-ac-001"></a>MF-AC-001 | Va chạm có đa giác lõm | Xuất | Editor tự tách lồi; validator xanh |
| MF-AC-002 | Biên F01→HUB dài 25m, HUB→F01 dài 30m | Validator | Fail MF-V-03 |
| MF-AC-003 | Một khu bị tường bao kín, có `HERB` | Validator | Fail MF-V-02 (không liên thông) |
| MF-AC-004 | Điểm xa nhất cách truyền tống trận 110s chạy | Validator | Fail MF-V-06, editor tô đỏ vùng đó |
| MF-AC-005 | `secretCandidates` có trong `logic.client.json` | CI | Fail MF-V-12 |
| MF-AC-006 | Đổi ID `V1_F01_TP_001` sau phát hành | CI | Fail MF-V-12 |
| MF-AC-007 | Chỉ sửa vị trí cây (hình ảnh), không đổi va chạm | Build | `assetHash` đổi, `logicHash` / `gamedataVersion` không đổi |
| MF-AC-008 | Dã ngoại tổng tải 6,2 MB | CI | Fail ngân sách |
| MF-AC-009 | Thử chạy trong editor | Đi vào tường | Va chạm giống hệt Game Server (cùng `shared/sim`) |
| MF-AC-010 | POI `kind: "herb"` (chữ thường) | Validator | Fail schema (phải `HERB`) |
| <a id="mf-ac-011"></a>MF-AC-011 | `ambient.palette = "PAL_V1_XYZ"` không có trong `env_palettes.json` | `maps:build` | Fail MF-V-14 |
| <a id="mf-ac-012"></a>MF-AC-012 | `BIO_V1_BAMBOO_GROVE.json` có `PRP_SWAMP_REED_TALL_01` (kit ngoài `allowedKits`) | `maps:build all` | Fail MF-V-15 |
| <a id="mf-ac-013"></a>MF-AC-013 | Bẫy `TRP_POISON_VENT` có trong GLB chunk của `V1_HD01_L1` | `maps:build` | Fail MF-V-16 |
| <a id="mf-ac-014"></a>MF-AC-014 | Cùng bản đồ, hai client preset Thấp; một client preset Trung | Nạp bản đồ | Hai client Thấp giữ cùng tập `SCATTER` `droppable`; tập Thấp ⊂ tập Trung; mảnh có va chạm giữ 100% (MF-R-035) |
| <a id="mf-ac-015"></a>MF-AC-015 | Xuất lại cùng scene không chỉnh gì | Build | `assetHash` và `logicHash` không đổi (MF-R-034) |
| <a id="mf-ac-016"></a>MF-AC-016 | Hai `PRP_VILLAGE_WALL_2M` cạnh `WALL_05` kéo lại gần nhau | Editor | Gợi ý snap khớp cạnh; đặt cạnh `WALL_05` cạnh `FENCE` → cảnh báo, không chặn (MF-R-032) |

---

## 10. Câu hỏi mở
- [ ] **MF-Q-1** (= PLAN Q22): Mở rộng Babylon.js Editor hay tự viết editor logic? Chốt bằng spike MF-R-031.
- [ ] **MF-Q-2** (= PLAN Q15): Kích thước dã ngoại chuẩn 250m hay 400m — ảnh hưởng ngân sách §7 và số chunk.
- [ ] **MF-Q-3:** Có cần lớp "trang trí động theo mùa / sự kiện" (GD-23) trong định dạng bản đồ (prop bật / tắt theo cờ) ngay từ đầu không?
- [ ] **MF-Q-4:** `respawnPoints` dùng vào việc gì? GD-26 LC-R-061 hồi sinh tại truyền tống trận đã kích hoạt / `Background.start.poiId` / `HUB`, không đọc `respawnPoints`. *Mặc định:* giữ trường (không luật nào đọc), validator chỉ kiểm điểm nằm trên vùng đi được (MF-V-02); chốt khi GD-26 / TD-12 cần điểm hồi sinh theo bản đồ.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-23 | — | Bản nháp đầu: tách hình ảnh / logic, tệp của một bản đồ, logic thuộc bundle gamedata (chốt DT-Q-4), `MapDef` + kết nối + vùng + POI, danh mục 18 loại POI, yêu cầu editor (độc lập Q22), quy trình làm bản đồ, pipeline xuất / build, 12 validator bản đồ, ngân sách hiệu năng, ví dụ, nghiệm thu |
| v0.2 | 2026-10-25 | — | E11 cọ rải theo quy tắc + mẫu biome (PLAN §6.7, D-22) |
| v0.3 | 2026-10-25 | — | Ghi rõ draw call môi trường nằm trong tổng 200 (TD-11) |
| v0.4 | 2026-10-25 | — | Vùng `NO_FLY`, cờ `fly` (GD-31); Validator ngự kiếm |
| v0.5 | 2026-10-25 | — | Thống nhất `collisionTemplate` với AR-02 §7 (CIRCLE / POLYGON, cờ `fly`) |
| v0.6 | 2026-10-25 | — | Tam giác thành trấn ≤ 300k (theo TD-11 §3.1) |
| v0.7 | 2026-10-25 | — | Đồng bộ AR-06 §13: §5.3 mới — hồ sơ cạnh + đơn vị module 2m (MF-R-032), định dạng `BiomeTemplate` và nơi lưu `tools/map-editor/biomes/` (MF-R-033, chốt AR-06 KIT-Q-12), rải tất định theo seed (MF-R-034), giảm rải theo preset bằng FNV-1a trên vị trí (MF-R-035), prop `DYNAMIC` / vị trí `@serverOnly` không nướng vào chunk (MF-R-036); MF-V-14..17; MF-V-13 nhắc mảng rừng rậm (KIT-R-047); MF-AC-011..016 |
| v0.8 | 2026-10-25 | — | Bỏ ghi chú GD-21 `herb` (GD-21 v0.8 đã sửa) |
| v0.9 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: `SpawnAreaDef` theo GD-12 (`id` = `SpawnTable.id` `SP_*`, thêm `spawnPoints`, bỏ `spawnGroupIds` / `weights`; MF-R-010 kiểm `SP_*`, `SG_*` không gắn bản đồ — đóng GD-16 LV-Q-3); thêm `PoiKind` `ENCOUNTER_POINT` + mã rút gọn `ENC` (GD-22; đóng LV-Q-5); ví dụ §8: khu `V1_F01_A01`, `HERB_012` `ITM_HERB_NGUYET_QUANG` chỉ `NIGHT`, kết nối `s_f03`, `SP_V1_F01_A01` = bãi tập, `chest_a`, điểm `V1_F01_ENC_001`; MF-Q-4 (`respawnPoints`) |

