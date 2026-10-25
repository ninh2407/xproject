# TD-07 — Gamedata Format

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-07 · Prefix ID: `DT` |
| Owner | Tech Lead (Server) + Lead Designer |
| Trạng thái | 🟡 Bản nháp v0.11 |
| Cần trước | Phase 0 (trước khi nhập dữ liệu thật cho bất kỳ hệ thống nào) |
| Quyết định nền | PLAN §5.5, D-07 (i18n), D-19; [TD-01](00-tdd.md) TA-R-008, TA-R-011, TA-R-150 |
| Liên quan | [TD-01 TDD](00-tdd.md) · [docs/README.md](../README.md) · [Glossary](../lore/glossary.md) · mục "Gamedata" (§7) và "Validator (CI)" của mọi GD · TD-13 Map Format · TD-15 OSS Register · [AR-02](../art/asset-guidelines.md) §3 (ID asset) · [AR-04](../art/vfx-guidelines.md) §11 · [AR-06](../art/environment-kits.md) §6 |

> Mọi con số cân bằng, mọi danh sách nội dung (vật phẩm, kỹ năng, quái, công thức, cơ duyên…) nằm trong gamedata. Các GD định nghĩa **nội dung** từng file (§7 của GD); tài liệu này định nghĩa **luật chung**: thư mục, định dạng, ID, kiểu, i18n, pipeline build, validator, phiên bản, cách code nạp dữ liệu.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-07 | Mục |
|--------------|-----------------|-----|
| Mọi GD | Quy ước file, ID, đơn vị, i18n key; nơi đăng ký validator | §3–§6, §9 |
| TD-01 | `gamedataVersion` (TA-R-150), danh sách hàm hai ngôn ngữ (TA-R-011) | §8, §11 |
| TD-13 | Ranh giới `shared/maps` với `shared/gamedata` | §2 |
| Client / Game Server / API Service | Cách nạp và truy cập dữ liệu có kiểu | §10 |

---

## 1. Mục tiêu

- **Một nguồn sự thật:** cùng một bundle dữ liệu cho client, Game Server và API Service.
- **Sai thì vỡ build, không vỡ ở production:** kiểu, tham chiếu, ràng buộc cân bằng đều kiểm tra trong CI.
- Designer / Content Designer **tự thêm nội dung** mà không cần dev (soạn JSON / CSV với autocomplete, chạy một lệnh kiểm tra).
- **AI sinh dữ liệu đúng ngay lần đầu:** kiểu nằm ở một chỗ, ví dụ trong GD khớp schema.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="dt-r-001"></a>**DT-R-001** | Kiểu dữ liệu gamedata có **một nguồn duy nhất**: file TypeScript trong `shared/gamedata-types/` (chép từ §3 của GD). JSON Schema và lớp Java **sinh tự động** từ đó, không viết tay (§3). |
| <a id="dt-r-002"></a>**DT-R-002** | ID nội dung **không bao giờ đổi tên, không bao giờ tái sử dụng** sau khi đã phát hành tới môi trường có dữ liệu người chơi (Alpha trở đi). Ngừng dùng → `deprecated: true` (§4.4). |
| <a id="dt-r-003"></a>**DT-R-003** | Gamedata **không chứa văn bản hiển thị**: chỉ chứa i18n key (§6). |
| <a id="dt-r-004"></a>**DT-R-004** | Production chỉ đổi gamedata bằng **triển khai bundle mới** qua pipeline (§8). Không có công cụ sửa dữ liệu trực tiếp trên server đang chạy. |
| <a id="dt-r-005"></a>**DT-R-005** | Mọi file gamedata phải qua: kiểm tra schema → kiểm tra tham chiếu → validator nghiệp vụ của GD. Lỗi bất kỳ → build fail. |

---

## 2. Thư mục

```
shared/
├── gamedata-types/            # NGUỒN KIỂU (TS) — DT-R-001
│   └── {domain}.ts            # vd. items.ts, skills.ts, technique_forge.ts
├── gamedata/                  # dữ liệu cân bằng / nội dung có cấu trúc
│   └── {domain}/
│       ├── params.json        # tham số đơn (object)
│       ├── {collection}.json  # mảng thực thể có `id`
│       └── {collection}/*.json  # một bộ sưu tập chia nhiều file (gộp theo glob)
├── maps/{mapId}.json          # dữ liệu logic bản đồ (xuất từ map editor, TD-13)
├── encounters/                # cơ duyên: defs/*.yaml + ink/*.{lang}.ink (GD-22 §7)
├── quests/                    # nhiệm vụ: defs/*.yaml + ink/*.{lang}.ink (GD-15 §7)
├── i18n/{lang}/{ns}.json      # văn bản hiển thị (§6)
├── testvectors/{name}.json    # golden test hai ngôn ngữ (§11)
└── gamedata-build/            # (sinh ra, không commit) schema/*.json, java/, dist/
```

- <a id="dt-r-010"></a>**DT-R-010** Thư mục domain trong `shared/gamedata/` (đã dùng trong các GD): `alchemy`, `balance`, `beasts`, `cultivation`, `danger_zones`, `economy`, `fairness`, `encounters` (chỉ `params.json`), `forging`, `housing`, `items`, `lifecycle`, `monetization`, `monsters`, `onboarding`, `pvp`, `quests` (chỉ `params.json`), `skills`, `social`, `stats`, `talents`, `technique_forge`, `vfx`, `world`, `world_events`. Thêm domain mới phải thêm vào danh sách này và vào bảng đăng ký file (§9.1).
- <a id="dt-r-011"></a>**DT-R-011** **Dữ liệu theo bản đồ** (vị trí POI, vùng, spawn point, lưới đi được, cổng nối, thông số kênh, ghi đè thời tiết) nằm ở `shared/maps/{mapId}.json` (TD-13), **không** ở `shared/gamedata/maps/`. Bảng chung (bảng thời tiết theo vực, tham số fog…) ở `shared/gamedata/world/world.json`. (Sửa tham chiếu GD-21 §4.)
- <a id="dt-r-012"></a>**DT-R-012** Kịch bản (cơ duyên, nhiệm vụ) dùng YAML cho định nghĩa + Ink cho lời thoại vì designer viết dài và có nhánh; YAML **cùng chịu** schema, kiểm tra tham chiếu và i18n như JSON (YAML chỉ là cú pháp khác).

---

## 3. Kiểu dữ liệu: TS → JSON Schema → Java

```
shared/gamedata-types/skills.ts   (export interface SkillDef {...}  — chép từ GD-07 §3)
        │  ts-json-schema-generator (CI)
        ▼
gamedata-build/schema/skills/SkillDef.schema.json
        │  ├── Ajv: kiểm tra file JSON (build, editor autocomplete)
        │  └── jsonschema2pojo (Gradle): sinh record Java cho API Service
        ▼
client / game-server dùng thẳng type TS · api-service dùng record Java sinh ra
```

- <a id="dt-r-020"></a>**DT-R-020** Mỗi file gamedata khai báo kiểu gốc trong bảng đăng ký (§9.1), vd. `skills/skills.json` → `SkillDef[]`, `skills/params.json` → `SkillParams`.
- <a id="dt-r-021"></a>**DT-R-021** Chú thích JSDoc trong type TS được dùng làm ràng buộc schema:

| Chú thích | Nghĩa | Ví dụ |
|-----------|-------|-------|
| `@minimum` / `@maximum` | Khoảng số | `/** @minimum 0 @maximum 1 */ chance: number` |
| `@pattern` | Regex chuỗi | ID |
| `@ref ITM` | Trường là ID tham chiếu tới thực thể có tiền tố `ITM` (kiểm tra tồn tại, §5) | `/** @ref ITM */ outputTemplateId: string` |
| `@ref ST` (trên mảng) | Mỗi phần tử là ID tham chiếu | `/** @ref ST */ statusIds: string[]` |
| `@i18n` | Trường là i18n key (kiểm tra tồn tại, §6) | `/** @i18n */ nameKey: string` |
| `@unit sec` | Đơn vị (tài liệu + lint tên trường, §4.2) | |
| `@unit pp` | Điểm phần trăm 0–100 cho xác suất cộng dồn (ngoại lệ DT-R-041, GD-29 RF-R-090) | `/** @unit pp */ baseChance: number` |
| `@serverOnly` | Trường không có trong bundle client (§4.6, GD-29 §5.4) | `/** @serverOnly */ entries: LootEntry[]` |

- <a id="dt-r-022"></a>**DT-R-022** Union có phân biệt (`{ type: 'A'; ... } | { type: 'B'; ... }`) là cách duy nhất để mô tả dữ liệu đa hình (hiệu ứng thiên phú, `Shape`, `HitDef`, `PatchOp`, `QuestAction`, outcome cơ duyên…). Trường phân biệt luôn tên `type` (hoặc `op`/`kind` nếu GD đã đặt). Validator báo lỗi theo nhánh union, không chung chung.
- <a id="dt-r-023"></a>**DT-R-023** Đổi type TS trong GD → sửa `shared/gamedata-types` **trong cùng PR** với dữ liệu và GD; CI so sánh tên kiểu / trường giữa GD §3 và file TS (cảnh báo khi lệch, không chặn).

---

## 4. Quy ước dữ liệu

### 4.1. Định dạng file
- <a id="dt-r-030"></a>**DT-R-030** File nguồn là **JSONC** (JSON cho phép comment `//` và dấu phẩy cuối); build chuyển thành JSON chuẩn. Mã hóa UTF-8, không BOM, xuống dòng LF, thụt 2 dấu cách (Prettier trong CI).
- <a id="dt-r-031"></a>**DT-R-031** **Bộ sưu tập** là mảng các object có `id`, sắp theo `id` trong mỗi file (formatter tự sắp) để diff dễ đọc. **Tham số** là một object.
- <a id="dt-r-032"></a>**DT-R-032** **CSV** được phép cho bảng phẳng mà designer hay sửa bằng bảng tính (đăng ký ở §9.1 với cột `csv`): dòng đầu là tên trường theo type TS, ô rỗng = không có trường, số dùng dấu chấm thập phân, mảng ghi `a|b|c`. Build chuyển CSV thành JSON rồi kiểm tra như JSON. Ví dụ: `stats/base_stats.csv`, `cultivation/stages.csv`, `monsters/monsters.csv`.
- <a id="dt-r-033"></a>**DT-R-033** Một bộ sưu tập chia nhiều file (`items/templates/*.json`) được gộp; **ID trùng giữa các file là lỗi**.

### 4.2. Đơn vị và tên trường
- <a id="dt-r-040"></a>**DT-R-040** Tên trường `camelCase`, giá trị enum `UPPER_SNAKE` (glossary). Hậu tố tên trường quy định đơn vị:

| Hậu tố | Đơn vị | Ví dụ |
|--------|--------|-------|
| `Sec`, `Ms` | giây, mili giây | `cooldownSec`, `windupSec` |
| `Min`, `Hours`, `Days` | phút / giờ / ngày **thời gian thực** | `buybackMinutes` (dữ liệu cũ, chấp nhận), `playerMailExpireDays` |
| `Years` | năm **trong game** (thọ nguyên) | `lifespanYears` |
| `Deg` | độ | `angleDeg` |
| (không hậu tố) cho khoảng cách | mét | `range`, `radius`, `distance` |
| `Pct`, `Ratio`, `Mult`, `chance` | **tỷ lệ dạng phân số** (0,15 = 15%) | `npcSellRatio`, `mpPctOfBase`, `pctMaxHp` |
| `Weight(s)` | trọng số nguyên tương đối (không cần tổng 100) | `fragmentKindWeights` |

- <a id="dt-r-041"></a>**DT-R-041** **Mọi tỷ lệ là phân số** (0–1, hoặc > 1 với hệ số nhân), kể cả trường có hậu tố `Pct` (lý do lịch sử trong GD). **Ngoại lệ duy nhất:** trường `@unit pp` của các hệ xác suất cộng dồn đăng ký ở [GD-29 RF-R-090](../design/05-rng-fairness.md) (đột phá, luyện đan, luyện khí, cường hóa, nâng phẩm). Không lưu "15" để chỉ 15%. Validator chặn trường `Pct`/`chance` > 10 (dấu hiệu nhập phần trăm).
- <a id="dt-r-042"></a>**DT-R-042** Tiền tệ, số lượng, cấp, số lượt: số nguyên. Hệ số: số thực, tối đa 4 chữ số thập phân.
- <a id="dt-r-043"></a>**DT-R-043** Mã cảnh giới dạng chuỗi `PN1`, `LK5`, `TC1`… (GD-04); so sánh qua bảng thứ tự trong `cultivation/stages`, không so sánh chuỗi.

### 4.3. ID nội dung
- <a id="dt-r-050"></a>**DT-R-050** Mẫu chung: `^[A-Z][A-Z0-9]*(_[A-Z0-9]+)+$`, dài ≤ 48 ký tự, chữ không dấu. Phần sau tiền tố là tên tiếng Anh hoặc tiếng Việt không dấu đều được (glossary: tên riêng nội dung dùng tiếng Việt, vd. `ITM_TRUC_CO_DAN`).
- <a id="dt-r-051"></a>**DT-R-051** **Bảng đăng ký tiền tố** (danh sách đóng; thêm tiền tố mới phải thêm vào đây):

| Tiền tố | Thực thể | File / nguồn | GD | Lưu trong DB? |
|---------|----------|--------------|----|:---:|
| `ITM_` | Mẫu vật phẩm (gồm `ITM_FRAG_*`, `ITM_SEED_*`, `ITM_SCROLL_CRAFTED_*`) | `items/templates/*` | GD-09 | ✅ |
| `AFX_` | Thuộc tính phụ | `items/affixes` | GD-09 | ✅ |
| `SET_` | Bộ trang bị | `items/sets` | GD-09 | ✅ |
| `RCP_` | Đan phương / khí phổ (`RCP_FORGE_*`) | `alchemy/recipes`, `forging/recipes` | GD-10, GD-11 | ✅ |
| `TAL_` | Thiên phú | `talents/talents` | GD-27 | ✅ |
| `BG_` | Xuất thân | `talents/backgrounds` | GD-27 | ✅ |
| `UNL_` | Mở khóa Luân Hồi | `lifecycle/rebirth_unlocks` | GD-26 | ✅ |
| `SKL_` | Kỹ năng (`SKL_MON_*` cho quái) | `skills/skills` | GD-07, GD-12 | ✅ |
| `TEC_` | Tâm pháp | `skills/main_techniques` | GD-07 | ✅ |
| `CORE_`, `ELEM_`, `MOD_` | Thành phần chế công pháp | `technique_forge/*` | GD-08 | ✅ |
| `BST_`, `BTR_` | Loài linh thú, đặc tính thú | `beasts/species`, `beasts/traits` | GD-25 | ✅ |
| `EDICT_` | Tông quy của tông môn tự lập | `social/sect_edicts` | GD-14 | ✅ |
| `COS_`, `OFR_`, `SS_` | Ngoại hình, ưu đãi cửa hàng, mùa Vấn Đạo Lệnh | `monetization/*` | GD-19 | ✅ (`COS_`) |
| `WE_` (`WEI_` cho bản ghi chạy, runtime) | Định nghĩa sự kiện thế giới | `world_events/*` | GD-23 | ✅ |
| `ST_` | Status | `stats/statuses` | GD-06 | ✅ (buff có thời hạn) |
| `MON_` | Quái (`MON_E_*` tinh anh, `MON_B_*` boss) | `monsters/monsters` | GD-12 | ✅ (codex) |
| `SG_` | Nhóm spawn do cơ duyên / sự kiện gọi (`SpawnGroupDef`; không gắn vào bản đồ — TD-13 MF-R-010) | `monsters/spawn_groups` | GD-12, GD-22, GD-23 | |
| `SP_` | Bảng spawn theo khu `SP_{mapId}_{areaId}` (`SpawnTable`; cùng `id` với vùng spawn `MapDef.spawnAreas[]`) | `monsters/spawn_tables` + `shared/maps` | GD-12, TD-13 | |
| `ESP_` | Tinh anh định kỳ `ESP_{mapId}_{TÊN}` (`EliteSpawnDef`) | `monsters/elite_spawns` | GD-12 | |
| `LT_` | Bảng rơi đồ | `monsters/loot_tables` | GD-12 | |
| `TRP_` | Bẫy hiểm địa | `danger_zones/traps` | GD-28 | |
| `SHOP_` | Cửa hàng NPC | `economy/shops` | GD-13 | |
| `NPC_` | NPC: `NPC_{ROLE}` (NPC duy nhất toàn thế giới), `NPC_{SECT}_{ROLE}` (thuộc tông), `NPC_V{n}_*` (gắn một vực) — giữ nguyên mã đã có | `shared/maps` + `social/npc_sects` | GD-14, GD-15, LR-02 | ✅ |
| `VFX_` | Mẫu VFX kỹ năng (`VFX_{MẪU}`), phần của mẫu (`VFX_{MẪU}_{PHẦN}`), VFX theo nhóm / boss (`VFX_ST_*`, `VFX_BOSS_*`…) | `vfx/templates.json`, `vfx/effects.json` | AR-04 §11.1 | |
| `VFXO_` | Lớp phủ biến thức | `vfx/overlays.json` | AR-04, GD-08 | |
| `VPAL_` | Bảng màu VFX theo nguyên tố (`VPAL_{ELEMENT}`, `VPAL_NONE`) | `vfx/palettes.json` | AR-04 §3 | |
| `TGPAL_` | Bảng màu vùng cảnh báo (`TGPAL_DEFAULT`, 3 bảng mù màu) | `vfx/telegraph_palettes.json` | AR-04 §11.3 | |
| `PAL_` | Bảng màu bản đồ `PAL_{VỰC}_{TÊN}` (khác `VPAL_` / `TGPAL_`) | `world/env_palettes.json` | AR-06 §6, TD-13 §3 | |
| `KIT_`, `PRP_` | Kit môi trường (`KIT_{VỰC}_{TÊN}`, `KIT_CM_{TÊN}`), mảnh kit — **ID asset** (AR-02 §3), không phải thực thể gamedata | `art/env/{kit}/kit.json` | AR-06 §3, AR-02 §3 | |
| `BIO_` | Mẫu biome cho cọ rải `BIO_{VỰC}_{TÊN}` — dữ liệu chỉ cho editor, không vào bundle | `tools/map-editor/biomes/{id}.json` | AR-06 §7, TD-13 §5.3 | |
| `ANM_` | Clip hoạt ảnh — **ID asset** (AR-02 §3, AR-03 §2.1) | `tools/art/anim-list.json` | AR-03 | |
| `ENC_` | Cơ duyên `ENC_{vực}_{TÊN}` | `shared/encounters/defs` | GD-22 | ✅ |
| `CHAIN_` | Chuỗi cơ duyên | `shared/encounters/defs` | GD-22 | ✅ |
| `QC_`, `QD_`, `QW_` | Nhiệm vụ chuỗi / mẫu hằng ngày / hằng tuần | `shared/quests/defs` | GD-15 | ✅ |
| `FLAG_` | Cờ kiếp / tài khoản (`FLAG_ACC_*`) | khai báo trong `quests/params`, `encounters/params` | GD-15, GD-22 | ✅ |
| `HINT_` | Gợi ý onboarding | `onboarding/hints.json` | GD-18 | ✅ |
| `CDX_` | Mục Vạn Giới Lục: `CDX_MON_{TÊN}` (yêu thú, `MonsterDef.codexId`), `CDX_LOC_{mapId}_{TÊN}` (bia / di tích, `STELE.props.codexId` — TD-13 §4) | chưa chốt (DT-Q-6) | GD-21 §3.6, GD-12 | ✅ (tiến độ theo tài khoản) |
| `LP_` | Hồ sơ ánh sáng bản đồ `LP_{TÊN}` (`MapDef.ambient.lightingProfileId`) | chưa chốt (DT-Q-6) | TD-05 §5, TD-13 §3 | |
| `BGM_` | Nhạc nền bản đồ `BGM_{VỰC}_{TÊN}` (`MapDef.ambient.musicId`) — ID asset âm thanh, không phải thực thể gamedata | danh mục AU-01 §3.1 | AU-01 | |
| `CHEST_` | Mẫu POI bí mật (`secretCandidates[].poiTemplate`, TD-13 §3): `CHEST_{CỠ}_{VỰC}` = Ẩn cấp 1, hậu tố `_H{n}` = Ẩn cấp n (vd. `CHEST_SMALL_V1`, `CHEST_SMALL_V1_H2`) | chưa chốt (DT-Q-6) | GD-21 §3.4, GD-09 | |
| `V{n}`, `V{n}_{HUB\|F\|HD\|BC\|KN\|DP}{nn}` | Vực, bản đồ | `shared/maps`, `world/world` | GD-21 | ✅ |
| `{mapId}_{KIND}_{nnn}`, `{mapId}_A{nn}` | POI, khu vực | `shared/maps` | GD-21 | ✅ (POI đã khám phá) |
| `CSK_` | Công pháp tự chế | **Không phải gamedata** (sinh lúc chạy, DB) | GD-08 | ✅ |

- <a id="dt-r-052"></a>**DT-R-052** ID **duy nhất toàn cục** (không chỉ trong file) để thông báo lỗi và tham chiếu không mơ hồ. Giá trị enum (`SET_FLAG`, `NPC_BUY`…) không phải ID nên không xung đột, nhưng validator cảnh báo khi một ID trùng tên với giá trị enum đã đăng ký.
- <a id="dt-r-053"></a>**DT-R-053** **ID asset** (danh sách đóng ở AR-02 §3, AST-R-011: `CHR_`, `OUT_`, `WPN_`, `MDL_MON_`, `KIT_`, `PRP_`, `ENV_`, `TER_`, `VFX_`, `VFXO_`, `ICO_`, `UI_`, `POR_`, `ILL_`, `WMAP_`, `ANM_`; `COS_` vừa là ID gamedata vừa là `assetId`) do AR-02 sở hữu, không phải thực thể gamedata; gamedata chỉ **tham chiếu** (vd. `MonsterDef.modelId`, `iconId`) và `art:check` kiểm tồn tại trong tracker AR-05. Bảng DT-R-051 chỉ liệt kê tiền tố asset nào **cũng là khóa** trong file gamedata / bản đồ / catalogue công cụ, để kiểm trùng toàn cục (DT-R-052). Cột "Lưu trong DB?" để trống với ID asset và ID VFX: không đổi tên vẫn bắt buộc theo AST-R-010 / VFX-R-100, nhưng không cần migration DB.

### 4.4. Ngừng dùng và bí danh
- <a id="dt-r-055"></a>**DT-R-055** Thực thể ngừng dùng: giữ nguyên, thêm `"deprecated": true`. Hiệu lực: không xuất hiện trong bảng rơi đồ / cửa hàng / pool mới (validator chặn tham chiếu **mới** tới thực thể deprecated), nhưng dữ liệu người chơi đang giữ vẫn đọc được.
- <a id="dt-r-056"></a>**DT-R-056** Bí danh tên cũ (vd. GD-06 SF-R-120: `WEAKENED` = `ST_WEAKENED`) khai báo ở `shared/gamedata/aliases.json` `{ "WEAKENED": "ST_WEAKENED" }`. **Chỉ dùng cho tài liệu và công cụ nhập liệu**; code và dữ liệu đã build chỉ chứa ID chuẩn (build thay bí danh bằng ID chuẩn và cảnh báo).

### 4.5. Gắn phase
- <a id="dt-r-058"></a>**DT-R-058** Thực thể bất kỳ có thể có `"phase": "ALPHA" | "BETA" | "LAUNCH" | "POST_LAUNCH"` (mặc định `ALPHA`). Build theo **hồ sơ** (`--profile alpha|beta|launch`) loại bỏ thực thể có phase muộn hơn; tham chiếu từ thực thể còn lại tới thực thể bị loại là lỗi. Dùng để hiện thực bảng "Phạm vi MVP" của các GD mà không tách nhánh dữ liệu.

---

### 4.6. Dữ liệu chỉ ở server
- <a id="dt-r-059"></a>**DT-R-059** (Chốt DT-Q-3.) Build xuất **hai bundle**: `bundle.server` (đầy đủ, cho Game Server và API Service) và `bundle.client` (loại bỏ mọi trường `@serverOnly`, mọi file có `"visibility": "server"` trong registry, và i18n namespace server-only). Danh sách dữ liệu bắt buộc server-only và lý do: [GD-29 RF-R-041](../design/05-rng-fairness.md). Gói TS sinh hai kiểu `ServerGamedata` / `ClientGamedata`; client chỉ import được `ClientGamedata` (lỗi biên dịch nếu đọc trường server-only).
- <a id="dt-r-057"></a>**DT-R-057** File có `"visibility": "client"` trong registry (dữ liệu **chỉ client dùng**: domain `vfx`, `world/env_palettes.json`) vẫn nằm trong `bundle.server` để `gamedataVersion` (DT-R-090) phủ mọi thay đổi và validator kiểm được tham chiếu chéo (VFX-R-105, MF-V-14), nhưng `ServerGamedata` **không** sinh accessor cho chúng (Game Server / API Service không đọc). Giá trị `visibility`: `"all"` (mặc định) · `"server"` (DT-R-059) · `"client"`. Có loại khỏi `bundle.server` để giảm dung lượng hay không: DT-Q-5.

## 5. Tham chiếu

- <a id="dt-r-060"></a>**DT-R-060** Trường có `@ref PREFIX` phải trỏ tới ID tồn tại thuộc đúng tiền tố, trong bundle sau khi đã lọc phase. Báo lỗi dạng:
```
[REF] skills/skills.json  SKL_FIRE_ORB.hits[0].statuses[0].statusId = "ST_BURNN" → không tồn tại (gần nhất: ST_BURN)
```
- <a id="dt-r-061"></a>**DT-R-061** Tham chiếu từ **kịch bản** (YAML, Ink) cũng được kiểm tra: Ink chỉ được gọi hàm ngoài thuộc danh sách trắng của GD-22 / GD-15, đối số là ID hợp lệ (GD-22 §7.4 V-01..V-13, GD-15 §7 validator).
- <a id="dt-r-062"></a>**DT-R-062** Validator cảnh báo **thực thể mồ côi** (không ai tham chiếu, không phải điểm vào như cửa hàng / cơ duyên / bản đồ): vd. vật phẩm không có nguồn nào, status không ai gây ra.

---

## 6. Văn bản & i18n

- <a id="dt-r-070"></a>**DT-R-070** Văn bản hiển thị nằm ở `shared/i18n/{lang}/{ns}.json` dạng object phẳng `{ "tal.fire_body.name": "Hỏa Linh Thể" }`. `ns` = đoạn đầu của key (`tal`, `skl`, `itm`, `mod`, `st`, `mon`, `enc`, `qst`, `ui`, `err`…).
- <a id="dt-r-071"></a>**DT-R-071** Mẫu key: `{ns}.{id_bỏ_tiền_tố_viết_thường}.{field}`, vd. `TAL_FIRE_BODY` → `tal.fire_body.name` / `tal.fire_body.desc`. Key nội dung có thể rút gọn nếu GD đã đặt (vd. `mod.split`, `itm.eq.sword_lk`); validator chỉ yêu cầu key **tồn tại**, không bắt theo mẫu.
- <a id="dt-r-072"></a>**DT-R-072** `vi` là ngôn ngữ gốc, **phải đủ 100%** (thiếu key = build fail). Ngôn ngữ khác (`en` …) thiếu key → fallback `vi` và báo cáo độ phủ (D-07).
- <a id="dt-r-073"></a>**DT-R-073** Tham số trong chuỗi dùng ICU MessageFormat `{name}`, `{count, plural, ...}` (i18next ICU). Số trong mô tả kỹ năng / vật phẩm **lấy từ gamedata lúc hiển thị** (`"Gây {coef} lần sát thương"`), không chép số vào chuỗi.
- <a id="dt-r-074"></a>**DT-R-074** Tên do người chơi đặt (công pháp, tông môn, nhân vật) là dữ liệu runtime, không qua i18n.

---

## 7. Dữ liệu sinh tự động

- <a id="dt-r-080"></a>**DT-R-080** Một số file được **sinh** từ dữ liệu khác bởi script trong `tools/gamedata/generators/`; file sinh ra **không** sửa tay, có dòng đầu `// GENERATED by {generator} — do not edit`, được commit để review diff.

| Generator | Đầu vào | Đầu ra | GD |
|-----------|---------|--------|----|
| `gen-fragments` | `technique_forge/{cores,elements,modifiers}.json` × 4 phẩm | `items/templates/fragments_*.json` | GD-08 §7 |
| `gen-base-stats` | công thức GD-06 §6.2 + `stats/params.json` | `stats/base_stats.json` (kiểm tra ngược ±1) | GD-06 |
| `gen-i18n-stubs` | ID mới chưa có key | chèn key `TODO:` vào `i18n/vi/*.json` (build vẫn fail tới khi điền) | §6 |

- <a id="dt-r-081"></a>**DT-R-081** CI chạy lại generator và so sánh: file sinh ra lệch với file đã commit → fail ("chạy `pnpm gamedata:gen`").

---

## 8. Pipeline build & phiên bản

```
pnpm gamedata:build --profile alpha
  1. Nạp: JSONC / CSV / YAML → object; thay bí danh (§4.4)
  2. Gộp bộ sưu tập nhiều file; kiểm tra ID trùng toàn cục
  3. Lọc theo phase (§4.5)
  4. Schema (Ajv, sinh từ TS)                       → lỗi [SCHEMA]
  5. Tham chiếu @ref, i18n @i18n                    → lỗi [REF] [I18N]
  6. Validator nghiệp vụ của từng GD (§9.2)         → lỗi [RULE:{mã}]
  7. Generator kiểm tra lệch (§7)                   → lỗi [GEN]
  8. Xuất gamedata-build/dist/:
       bundle.server.{version}.json (toàn bộ, đã chuẩn hóa, sắp xếp khóa)
       bundle.client.{version}.json (bỏ dữ liệu server-only, §4.6; thêm RarityBand sinh lúc build, GD-29 RF-R-030)
       manifest.json                { version, profile, gitCommit, builtAt, files: {path: sha256} }
       i18n/{lang}.{version}.json
```

- <a id="dt-r-090"></a>**DT-R-090** `gamedataVersion` = 12 ký tự hex đầu của SHA-256 trên `bundle` đã chuẩn hóa (khóa sắp xếp, không khoảng trắng) + profile. Cùng nội dung ⇒ cùng version, ở mọi máy. `gamedataVersion` tính trên `bundle.server`; `bundle.client` mang cùng version.
- <a id="dt-r-091"></a>**DT-R-091** Một bản phát hành gồm **cùng một** `gamedataVersion` cho client (CDN, tên file có version → cache vĩnh viễn), Game Server và API Service (đóng gói vào artifact). Kiểm tra lúc khởi động theo TD-01 TA-R-150.
- <a id="dt-r-092"></a>**DT-R-092** Lỗi build in dạng `[LOẠI] đường dẫn file  ID.đường.dẫn.trường = giá trị → mô tả`, có số dòng khi nguồn là JSONC / CSV. Exit code ≠ 0. Có `--json` để công cụ khác đọc.
- <a id="dt-r-093"></a>**DT-R-093** Thời gian build mục tiêu < 10s cho toàn bộ dữ liệu Vực 1 (để designer chạy liên tục). Validator nặng (duyệt tổ hợp GD-08 TF-R-031) chạy ở CI và lệnh riêng `pnpm gamedata:check --full`.

### 8.1. Thay đổi dữ liệu đã phát hành

| Loại thay đổi | Cho phép? | Điều kiện |
|---------------|:---:|-----------|
| Thêm thực thể mới | ✅ | |
| Đổi số cân bằng | ✅ | Ghi changelog cân bằng; phiên đang chạy dùng ảnh chụp lúc bắt đầu (DT-R-095) |
| Ngừng dùng thực thể | ✅ | `deprecated: true` (DT-R-055) |
| Đổi tên / xóa ID có cột "Lưu trong DB" ✅ | ❌ | Cấm (DT-R-002). Trường hợp bắt buộc: ADR + migration DB + bí danh |
| Đổi kiểu trường (vd. số → object) | ⚠️ | ADR; dữ liệu DB lưu theo kiểu cũ phải có migration |
| Thêm trường bắt buộc vào type | ⚠️ | Phải điền cho mọi thực thể trong cùng PR (build tự chặn) |

- <a id="dt-r-095"></a>**DT-R-095** Thao tác kéo dài qua nhiều request (phiên luyện đan, lượt hiểm địa, phiên cơ duyên, lần đột phá) **lưu `gamedataVersion` và ảnh chụp các giá trị cần thiết** (vd. công thức, tỷ lệ) lúc bắt đầu; kết thúc phiên dùng ảnh chụp đó, không dùng dữ liệu mới. Định nghĩa vĩnh viễn (vd. `crafted_technique.skill_def`, GD-08 TF-R-004) lưu bản đầy đủ.
- <a id="dt-r-096"></a>**DT-R-096** Hotfix dữ liệu (chỉ đổi số, không đổi code) đi **cùng pipeline**, triển khai API rolling + Game Server drain (TD-01 §13). Không có đường tắt.

---

## 9. Đăng ký file & validator

### 9.1. Bảng đăng ký file
- <a id="dt-r-100"></a>**DT-R-100** `shared/gamedata/registry.json` liệt kê **mọi** file / glob gamedata với kiểu gốc. File không có trong registry → build fail (tránh file "mồ côi" không được nạp).

```jsonc
// registry.json (trích)
{
  "files": [
    { "path": "items/params.json",          "type": "ItemParams" },
    { "path": "items/templates/*.json",     "type": "ItemTemplate[]", "collection": "ITM" },
    { "path": "items/affixes.json",         "type": "AffixDef[]",     "collection": "AFX" },
    { "path": "skills/skills.json",         "type": "SkillDef[]",     "collection": "SKL" },
    { "path": "world/client.json",          "type": "ClientParams" },          // TD-05 §13 (không chứa số camera — GD-24 `world/camera.json`; có anim.blend, rateMin/rateMax, vfxBudget, hệ số mật độ rải theo preset)
    { "path": "world/lighting_curves.json", "type": "LightingCurve[]" },       // TD-05 §5
    { "path": "world/camera.json",          "type": "CameraParams" },          // GD-24
    { "path": "world/controls.json",        "type": "ControlParams" },         // GD-24 (sơ đồ phím mặc định, ActionId)
    { "path": "world/flight.json",          "type": "FlightParams" },          // GD-31
    { "path": "world/env_palettes.json",    "type": "EnvPaletteDef[]",  "collection": "PAL",   "visibility": "client" },  // AR-06 §6
    { "path": "vfx/templates.json",         "type": "VfxTemplateDef[]", "collection": "VFX",   "visibility": "client" },  // AR-04 §11.2–11.3
    { "path": "vfx/palettes.json",          "type": "VfxPaletteDef[]",  "collection": "VPAL",  "visibility": "client" },
    { "path": "vfx/telegraph_palettes.json","type": "TelegraphPaletteDef[]", "collection": "TGPAL", "visibility": "client" },
    { "path": "vfx/overlays.json",          "type": "VfxOverlayDef[]",  "collection": "VFXO",  "visibility": "client" },
    { "path": "vfx/effects.json",           "type": "VfxEffectDef[]",   "collection": "VFX",   "visibility": "client" },
    { "path": "vfx/bindings.json",          "type": "VfxBindings",      "visibility": "client" },
    { "path": "vfx/params.json",            "type": "VfxParams",        "visibility": "client" },
    { "path": "world_events/*.json",        "type": "(GD-23 §7)" },
    { "path": "balance/*.json",             "type": "(GD-03 §8)" },
    { "path": "monetization/*.json",        "type": "(GD-19 §7)" },
    { "path": "onboarding/hints.json",      "type": "HintDef[]",      "collection": "HINT" },   // GD-18
    { "path": "onboarding/params.json",     "type": "(GD-18 §7)" },
    { "path": "stats/base_stats.csv",       "type": "BaseStatRow[]",  "csv": true },
    { "path": "technique_forge/modifiers.json", "type": "ModifierDef[]", "collection": "MOD" },
    { "path": "../encounters/defs/**/*.yaml",   "type": "EncounterDef",  "collection": "ENC" }
  ]
}
```

Danh sách file theo domain: xem mục "Gamedata" (§7) của GD sở hữu; registry phải khớp (validator so sánh registry với các bảng "File | Nội dung" trong GD và cảnh báo khi lệch).

- <a id="dt-r-101"></a>**DT-R-101** Domain `vfx` (7 file, nguồn kiểu: AR-04 §11.3 → `shared/gamedata-types/vfx.ts`) và `world/env_palettes.json` (kiểu `EnvPaletteDef`, AR-06 §6.2 → `shared/gamedata-types/world.ts`) là dữ liệu **chỉ client dùng** (`"visibility": "client"`, DT-R-057). `vfx/templates.json` và `vfx/effects.json` cùng tiền tố `VFX_` nhưng khác kiểu: ID không được trùng giữa hai file (DT-R-052); trường tham chiếu chỉ định đúng file (vd. `CoreDef.vfxTemplateId` chỉ nhận ID trong `templates.json`, `EncounterDef.presentation.vfx` chỉ nhận ID trong `effects.json` — VFX-R-105). Ngân sách hạt **không** chép số TD-11 vào `vfx/params.json` (AR-04 §11.3).

### 9.2. Validator nghiệp vụ
- <a id="dt-r-110"></a>**DT-R-110** Mỗi đoạn "**Validator (CI)**" trong GD được hiện thực thành **một module** `tools/gamedata/validators/{mã tài liệu}.ts` (vd. `gd07.ts`; tài liệu mỹ thuật dùng `arNN`, vd. `ar04.ts`), mỗi kiểm tra có mã `{PREFIX}-V-nn` theo thứ tự xuất hiện trong GD (GD-22 đã dùng `V-01..V-13`, giữ nguyên dạng `ENC-V-01`). Kiểm tra cũng có thể trỏ tới quy tắc `-R-` (vd. `SK-R-130`).
- <a id="dt-r-111"></a>**DT-R-111** Validator chỉ **đọc** bundle và trả danh sách lỗi; viết bằng TS và dùng **chung hàm với `shared/sim`** khi kiểm tra công thức (vd. `powerScore` GD-07 §6.5), không viết lại công thức.
- <a id="dt-r-112"></a>**DT-R-112** Danh sách validator hiện có (từ các GD):

| Module | Kiểm tra chính |
|--------|----------------|
| `gd04` | `stages` liên tục, một bình cảnh mỗi cảnh giới lớn, đột phá có vật phẩm tồn tại |
| `gd06` | `base_stats` khớp công thức; status có modifier hợp lệ; quan hệ ngũ hành đủ 8 × 8 |
| `gd07` | union `Shape`/`HitDef`/`PerfectionEffect`; `powerScore ≤ budget` (SK-R-130); tuyệt kỹ ⇔ năng lượng 100; bí pháp có `holdToCastSec`; `teaches` tồn tại |
| `gd08` | `PatchPath`/`op` thuộc danh sách trắng; hạch tâm trong ngân sách Hoàng; duyệt mọi tổ hợp (TF-R-031, chỉ `--full`); biến thức hiếm không rơi |
| `gd09` | `itemRealm`; pool thuộc tính phụ đúng ô; bộ chuẩn +30% ± 2%; không bán sức mạnh bằng `JADE` |
| `gd10` / `gd11` | `RCP_*` có đầu ra tồn tại; khí phổ có `rhythm` hợp lệ |
| `gd12` | ID khớp `rank`; chiêu quái có cảnh báo ≥ mức tối thiểu (EN-R-070); bảng rơi tham chiếu hợp lệ |
| `gd13` | cửa hàng không bán vật phẩm cấm (EC-R-001/002); trần thưởng cơ duyên theo phẩm |
| `gd15` / `gd22` | ID, tham chiếu, Ink chỉ gọi hàm trắng, không `RANDOM` trong Ink, trần outcome theo phẩm (V-01..V-13) |
| `gd21` / `gd30` | bản đồ: cổng nối hai chiều, mật độ POI, 2–4 `CAVE_GATE` mỗi `FIELD`, `SECT_SITE` không giao lối nối |
| `gd26` / `gd27` | ID `TAL_`/`BG_`; `effects[].type` thuộc union; `hookId` hợp lệ; tổng chỉ số bẩm sinh |
| `gd28` | mỗi layout luôn có ≥ 1 lối thoát mở ở mọi pha |
| `ar04` | **VFX-R-105**: mọi `CoreDef.vfxTemplateId` ∈ `vfx/templates.json`; mọi `ElementDef.paletteId` ∈ `vfx/palettes.json`; mọi `ModifierDef.vfxOverlayId` ∈ `vfx/overlays.json`; mọi `EncounterDef.presentation.vfx` ∈ `vfx/effects.json`; mọi `StatusDef.id` có khóa trong `bindings.status`; mỗi `Element` có đúng một `VPAL_*` có `motif`; `Σ parts[].particleShare = 1` mỗi mẫu; `readsParams` chỉ chứa đường dẫn tồn tại trong `Shape` / `SkillDef`; `shapeTypes` của các mẫu phủ đủ union `Shape` (GD-07 §3). Kiểm tra trên trường chưa có trong type của GD-08 / GD-22 chỉ bật khi GD đó đã thêm trường (AR-04 §15) |
| `ar06` | `env_palettes.json`: đủ 16 `MaterialGroup` (hoặc `derivedFrom` trỏ `PAL_*` tồn tại); hex `#RRGGBB`; **cảnh báo** nhóm `EMISSIVE_*` lệch < 30° sắc độ so với `VPAL_*` gần nhất (KIT-R-031, KIT-AC-019). Tồn tại `MapDef.ambient.palette` kiểm ở validator bản đồ (TD-13 MF-V-14) |

---

## 10. Nạp dữ liệu trong code

### 10.1. TypeScript (client, Game Server, validator)
```ts
import { loadGamedata, type Gamedata } from '@tienlo/gamedata';
const gd: Gamedata = await loadGamedata(bundleUrlOrPath);   // kiểm tra version, đóng băng (Object.freeze sâu)
gd.version;                          // "a1b2c3d4e5f6"
gd.items.get('ITM_TRUC_CO_DAN');     // ItemTemplate | undefined   (Map theo id)
gd.items.require('ITM_TRUC_CO_DAN'); // ném lỗi nếu thiếu
gd.skills.params.inputBufferSec;     // tham số có kiểu
```
- <a id="dt-r-120"></a>**DT-R-120** Gói `@tienlo/gamedata` (trong `shared/`) sinh **accessor có kiểu** từ registry: mỗi bộ sưu tập có `get`, `require`, `all()`, `byTag()` khi type có `tags`. Dữ liệu chỉ đọc (frozen); code không được sửa object gamedata.
- <a id="dt-r-121"></a>**DT-R-121** Client tải bundle từ CDN sau khi đăng nhập (`/v1/meta` trả `gamedataVersion` + URL); cache IndexedDB theo version. Client có thể nạp **bundle con** theo nhóm (vd. không tải dữ liệu quái Vực 2 khi đang ở Vực 1) — tối ưu sau, không bắt buộc MVP. Client chỉ tải `bundle.client` (DT-R-059).

### 10.2. Java (API Service)
```java
@Component class GamedataRegistry {             // nạp một lần lúc khởi động từ artifact
  String version();
  ItemTemplate item(String id);                  // ném UnknownGamedataIdException
  Optional<ItemTemplate> findItem(String id);
  SkillParams skillParams();
}
```
- <a id="dt-r-122"></a>**DT-R-122** API Service nạp bundle **lúc khởi động** (fail-fast nếu thiếu / sai version); record Java sinh từ schema là bất biến. Không đọc file gamedata rải rác lúc chạy.
- <a id="dt-r-123"></a>**DT-R-123** Môi trường **dev**: client và Game Server theo dõi thay đổi file và nạp lại nóng; API Service nạp lại qua endpoint `POST /admin/v1/gamedata/reload` **chỉ bật ở dev**.

---

## 11. Hàm hai ngôn ngữ (TA-R-011)

- <a id="dt-r-130"></a>**DT-R-130** Hàm tất định phải chạy giống hệt ở Java (API Service, bản quyết định) và TS (`shared/sim`, xem trước / validator). Mỗi hàm có file `shared/testvectors/{name}.json`: `[{ "input": {...}, "expected": {...} }]`, chạy ở JUnit và Vitest; lệch một vector = CI fail.

| Hàm | Java (quyết định) | TS (dùng cho) | Vector |
|-----|-------------------|---------------|--------|
| `rng.xoshiro128ss` (+ `nextFloat`, `nextInt`) | API: mọi roll | GS (roll quái), client (không) | `rng_xoshiro.json` (TA-AC-009) |
| `alchemy.scoreMinigame` | Chấm lại phiên luyện đan (GD-10 §6) | Client: hiển thị điểm trực tiếp | `alchemy_score.json` |
| `forging.scoreRhythm` | Chấm lại nhịp rèn (GD-11) | Client | `forging_rhythm.json` |
| `techniqueForge.compose` + `skills.powerScore` | Chế công pháp (GD-08 §5.3) | Client xem trước; validator `gd07`/`gd08` | `technique_compose.json`, `power_score.json` |

- <a id="dt-r-131"></a>**DT-R-131** Thêm hàm vào bảng này cần lý do trong PR (TD-01 khuyến nghị giữ danh sách ngắn). Vector phải phủ: ví dụ trong GD (mã `-AC-`), biên (0, max, làm tròn), và ít nhất 50 trường hợp ngẫu nhiên sinh từ bản TS rồi xác nhận bằng bản Java.
- <a id="dt-r-132"></a>**DT-R-132** Làm tròn: dùng **half-up** tới số chữ số GD quy định, hàm `round(x, digits)` chung trong vector; tránh so sánh số thực bằng `==` (vector dùng sai số `1e-9` trước khi làm tròn).

---

## 12. Công cụ cho designer

- `pnpm gamedata:check` — build nhanh + validator (bỏ `--full`), in lỗi dễ đọc.
- VS Code: `.vscode/settings.json` gắn JSON Schema sinh ra cho từng glob trong registry → autocomplete, kiểm tra kiểu khi gõ, gợi ý ID cho trường `@ref` (qua extension nội bộ đơn giản hoặc `enum` sinh vào schema dev).
- `pnpm gamedata:explain SKL_FIRE_ORB` — in thực thể, ai tham chiếu nó, giá trị tính toán (vd. `powerScore`).
- `pnpm gamedata:diff <gitRefA> <gitRefB>` — liệt kê thay đổi theo ID (thêm / sửa / deprecated), dùng viết changelog cân bằng.
- GM panel (Refine) có trang **xem** gamedata của bản đang chạy (chỉ đọc, DT-R-004).

---

## 13. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="dt-ac-001"></a>DT-AC-001 | `skills.json` có `statusId: "ST_BURNN"` | build | `[REF]` fail, gợi ý `ST_BURN`, có số dòng |
| DT-AC-002 | Hai file `items/templates/a.json`, `b.json` cùng có `ITM_X` | build | Fail ID trùng |
| DT-AC-003 | Thêm file `items/new.json` không có trong registry | build | Fail file chưa đăng ký |
| DT-AC-004 | Trường `chance: 30` | build | Fail (DT-R-041) |
| DT-AC-005 | ID `TAL_fire_body` | build | Fail mẫu ID |
| DT-AC-006 | Thiếu key `tal.fire_body.name` trong `vi` | build | Fail `[I18N]`; thiếu trong `en` chỉ cảnh báo |
| DT-AC-007 | Cùng nội dung build trên hai máy | so sánh | `gamedataVersion` giống nhau |
| DT-AC-008 | Thực thể `phase: BETA` bị cửa hàng (phase ALPHA) tham chiếu | build `--profile alpha` | Fail |
| DT-AC-009 | Sửa tay `fragments_huyen.json` | CI | Fail `[GEN]` |
| DT-AC-010 | Xóa `ITM_TRUC_CO_DAN` khỏi dữ liệu đã phát hành | CI (so với bản phát hành trước) | Fail (DT-R-002) |
| DT-AC-011 | Bảng rơi đồ mới tham chiếu vật phẩm `deprecated` | build | Fail; bảng cũ đã có từ trước chỉ cảnh báo |
| DT-AC-012 | `aliases.json` có `WEAKENED`; dữ liệu dùng `WEAKENED` | build | Bundle chứa `ST_WEAKENED`, cảnh báo |
| DT-AC-013 | Vector `power_score.json` | JUnit + Vitest | Cả hai pass, kết quả giống nhau |
| DT-AC-014 | API Service khởi động với bundle version khác Game Server | khởi động | Từ chối (TA-AC-020) |
| DT-AC-015 | Phiên luyện đan bắt đầu, sau đó triển khai bundle đổi tỷ lệ thành công | hoàn tất phiên | Dùng tỷ lệ trong ảnh chụp lúc bắt đầu (DT-R-095) |
| DT-AC-016 | `stats/base_stats.csv` có ô rỗng ở trường bắt buộc | build | Fail `[SCHEMA]` có dòng / cột |
| DT-AC-017 | Ink gọi `QUEST_ACTION("lạ")` | build | Fail (GD-15 QS-AC-008) |
| DT-AC-018 | Code client gán `gd.items.get(id).value = 0` | chạy test | Lỗi (object frozen) |
| <a id="dt-ac-019"></a>DT-AC-019 | Chỉ sửa `vfx/params.json` | build | `gamedataVersion` đổi; `ServerGamedata` không có accessor `vfx` (lỗi biên dịch nếu Game Server đọc) — DT-R-057 |
| <a id="dt-ac-020"></a>DT-AC-020 | `ModifierDef.vfxOverlayId = "VFXO_XYZ"` không có trong `vfx/overlays.json` | build | Fail `[RULE:VFX-R-105]` (= AR-04 VFX-AC-014) |
| <a id="dt-ac-021"></a>DT-AC-021 | `vfx/effects.json` có ID trùng một ID trong `vfx/templates.json` | build | Fail ID trùng toàn cục (DT-R-052, DT-R-101) |

---

## 14. Phạm vi theo phase

| Hạng mục | Phase 0 | Phase 1–2 | Alpha | Beta |
|----------|:---:|:---:|:---:|:---:|
| Thư mục, registry, JSONC, schema từ TS, kiểm tra tham chiếu, i18n `vi` | ✅ | | | |
| Gói `@tienlo/gamedata` (TS) + `GamedataRegistry` (Java sinh record) | ✅ | | | |
| Validator nghiệp vụ theo GD | | ✅ (theo hệ thống được làm) | | |
| CSV, generator, profile phase, testvectors | | ✅ | | |
| Chặn xóa / đổi tên ID đã phát hành (so với bản trước) | | | ✅ | |
| Bundle con theo vùng, công cụ `explain` / `diff` | | | | ✅ |

---

## 15. Câu hỏi mở
- [ ] **DT-Q-1:** Có cần công cụ bảng tính (Google Sheets → CSV tự động) cho designer, hay sửa CSV trong repo là đủ? Rủi ro: hai nguồn sự thật.
- [ ] **DT-Q-2:** `jsonschema2pojo` có xử lý tốt union có phân biệt không? Nếu không, sinh Java từ TS bằng generator tự viết (sealed interface + record). Chốt ở spike Phase 0.
- [x] **DT-Q-3:** *(Đã chốt 2026-10-23: **có tách** — DT-R-059, GD-29 §5.4.)* Có tách bundle dữ liệu "bí mật" (tỷ lệ cơ duyên, vị trí bí mật, bảng rơi đồ hiếm) khỏi bundle gửi client không? Đề xuất: **có** — build xuất `bundle.client` loại bỏ các trường đánh dấu `@serverOnly` (GD-29 sẽ quy định tỷ lệ nào được công khai).
- [x] **DT-Q-4:** *(Đã chốt 2026-10-23: **logic** bản đồ đi chung `gamedataVersion`; **hình ảnh** version riêng theo `assetHash` — TD-13 MF-R-002.)* Dữ liệu bản đồ có đi chung `gamedataVersion` không?
- [ ] **DT-Q-5:** File `"visibility": "client"` (domain `vfx`, `world/env_palettes.json`) có nên **loại khỏi** `bundle.server` để giảm dung lượng artifact server không? Khi đó DT-R-090 phải tính `gamedataVersion` trên cả hai bundle. *Mặc định: giữ trong `bundle.server`, không sinh accessor server (DT-R-057).* AR-04 §11.2 ghi "chỉ bundle client" — hiểu là "chỉ client dùng".
- [ ] **DT-Q-6:** Tiền tố `CDX_` (mục Vạn Giới Lục), `LP_` (hồ sơ ánh sáng), `CHEST_` (mẫu POI bí mật: Ẩn cấp, `lootTableId` `@serverOnly`) đã đăng ký ở DT-R-051 nhưng chưa có file / kiểu trong registry DT-R-100. *Mặc định:* tiền tố có hiệu lực để kiểm trùng (DT-R-052); validator tham chiếu (DT-R-060) với ba tiền tố này chỉ cảnh báo, chưa fail, tới khi GD-21 §3.6 / TD-05 §5 / GD-09 định nghĩa file.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-23 | — | Bản nháp đầu: thư mục, kiểu TS → JSON Schema → Java, quy ước file / đơn vị / tỷ lệ phân số, bảng đăng ký tiền tố ID, ngừng dùng & bí danh, gắn phase, tham chiếu, i18n, generator, pipeline build + `gamedataVersion`, quy tắc thay đổi dữ liệu đã phát hành + ảnh chụp phiên, registry & validator theo GD, nạp dữ liệu TS / Java, hàm hai ngôn ngữ + testvectors, công cụ designer, nghiệm thu |
| v0.2 | 2026-10-23 | — | Chốt DT-Q-3: hai bundle server / client, `@serverOnly` (§4.6, DT-R-059); chú thích `@unit pp` + ngoại lệ điểm phần trăm (DT-R-041, theo GD-29); thêm domain `fairness` |
| v0.3 | 2026-10-23 | — | Chốt DT-Q-4 theo TD-13 MF-R-002 |
| v0.4 | 2026-10-25 | — | Đổi tên gói `@xproject/gamedata` → `@tienlo/gamedata` (TD-10); thêm `world/client.json`, `world/lighting_curves.json` vào registry (TD-05) |
| v0.5 | 2026-10-25 | — | Domain `monetization`; registry camera / controls; tiền tố COS_ / OFR_ / SS_ |
| v0.6 | 2026-10-25 | — | Domain `balance`, `world_events`; Registry flight / world_events / balance; Tiền tố WE_ |
| v0.7 | 2026-10-25 | — | Domain `onboarding`, tiền tố `HINT_`, registry `onboarding/*` (GD-18) |
| v0.8 | 2026-10-25 | — | Domain `vfx`; quy ước mã `NPC_` (LR-02); tiền tố `VFX_`, `VFXO_`, `VPAL_`, `TGPAL_`, `KIT_`, `PRP_`, `PAL_`, `BIO_`, `ANM_` (AR-03/04/06) |
| v0.9 | 2026-10-25 | — | Registry: 7 file `vfx/*` (AR-04 §11.2–11.3) + `world/env_palettes.json` (AR-06) với `"visibility": "client"` (DT-R-057, DT-R-101); validator `ar04` (VFX-R-105) và `ar06` (KIT-R-031); bảng tiền tố tách mỗi tiền tố một dòng, sửa nguồn (`KIT_`/`PRP_` → `art/env/{kit}/kit.json`, `BIO_` → `tools/map-editor/biomes/`), bỏ ✅ "Lưu trong DB" của ID asset / VFX; DT-R-053 (ID asset do AR-02 sở hữu); module validator đặt tên theo mã tài liệu; DT-AC-019..021; DT-Q-5 |
| v0.10 | 2026-10-25 | — | `ClientParams` theo TD-05 v0.4 (bỏ camera, thêm anim / vfxBudget / mật độ rải) |
| v0.11 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: DT-R-051 thêm tiền tố `ESP_`, `CDX_`, `LP_`, `BGM_`, `CHEST_` (mẫu `CHEST_SMALL_V1`, `CHEST_SMALL_V1_H2`); `SP_` là `SpawnTable` (cùng `id` với `MapDef.spawnAreas`), `SG_` không gắn bản đồ (TD-13 v0.9, GD-12 v0.5); DT-Q-6 |

