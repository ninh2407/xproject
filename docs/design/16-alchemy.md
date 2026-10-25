# GD-10 — Luyện đan

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-10 · Prefix ID: `AL` |
| Owner | Game Designer + Client Dev (minigame) |
| Trạng thái | 🟡 Bản nháp v0.7 |
| Cần trước | Phase 3 (PLAN: "Luyện Đan đầy đủ"); bản thô minigame ở Phase 2 |
| Quyết định nền | PLAN D-05 (tu luyện dựa vào đan dược), D-17 · PLAN §4.12 |
| Liên quan | [PLAN §4.12](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-09 Vật phẩm](15-items-equipment.md) · [GD-13 Kinh tế](19-economy.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-27 Thiên phú](04-talents.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [GD-21 Open World](22-open-world.md) · [GD-06 Chỉ số](12-stats-formulas.md) · GD-11 Luyện khí |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Dữ liệu ở `shared/gamedata/alchemy/*.json` (§7).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-10 | Mục |
|--------------|-----------------|-----|
| GD-04 | Cách tạo đan tu luyện, Trúc Cơ Đan, Giải Độc Đan, Thanh Tâm Đan với `PillQuality` | §5.5, §7.2 |
| GD-09 | Sinh vật phẩm `CRAFT_OUTPUT` (có `quality`, đan văn), tiêu `CRAFT_INPUT`; mẫu đan lô, hỏa chủng | §5.6, §7 |
| GD-13 | Phí chế tác, hàng hóa Trung phẩm trở lên | §5.4 |
| GD-26 | Tiêu thọ nguyên `CRAFTING` khi luyện đan cao cấp; `UNL_PAST_LIFE_MEMORY` | §5.4, §5.8 |
| GD-27 | Hiệu ứng `CRAFT_QUALITY_PCT` (ALCHEMY), khí vận "bạo đan", thần thức | §6.2–6.4 |
| GD-28 / GD-06 | Tị Độc Đan → `ST_ANTI_POISON` | §7.2 |
| GD-11 | Khung chế tác dùng chung (công thức, phiên, minigame, phí, thuần thục) | §3, §5 |

---

## 1. Mục tiêu

**Trụ cột:** P4 *Chế tác có chiều sâu*, P2 *Cảm giác tu tiên*.

- Luyện đan là **con đường tiến bộ ngang hàng** với đánh quái (PLAN P4): đan là nguồn tu vi lớn nhất (GD-04: 35–40%) và là chìa khóa đột phá.
- **Kỹ năng người chơi có ý nghĩa** (minigame hỏa hầu), nhưng không bắt buộc lặp mãi (luyện tự động).
- May rủi **có kiểm soát** và minh bạch: thấy tỷ lệ, có bảo hiểm khi thất bại (PLAN §4.7).
- MVP chưa có giao dịch, nên **ai cũng phải tự luyện được** đan cần cho tiến trình chính (Trúc Cơ Đan).

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="al-r-001"></a>**AL-R-001** | Kết quả luyện (thành / bại, phẩm chất, đan văn, số lượng) do **API Service** tính. Minigame chạy trên client nhưng server **mô phỏng lại** từ chuỗi input và seed (§5.3) để ra điểm; không tin điểm client gửi. |
| <a id="al-r-002"></a>**AL-R-002** | Nguyên liệu, phí, thọ nguyên tiêu **khi bắt đầu** phiên (một transaction). Kết quả cấp khi phiên kết thúc. Không có cách hủy để giữ nguyên liệu sau thời gian ân hạn (AL-R-033). |
| <a id="al-r-003"></a>**AL-R-003** | Luyện đan là **nghề mọi nhân vật đều có** từ cấp 1; đan phương Trúc Cơ Đan luôn mua được ổn định ở Tông Môn Bảo Khố (AL-R-071). |
| <a id="al-r-004"></a>**AL-R-004** | Tỷ lệ thành công và phân phối phẩm chất dự kiến luôn hiển thị trước khi luyện (GD-29). |
| <a id="al-r-005"></a>**AL-R-005** | Cấp luyện đan sư, đan phương đã biết, độ thuần thục đan phương **thuộc kiếp hiện tại** (trừ `UNL_PAST_LIFE_MEMORY`, GD-26). |

---

## 2. Tổng quan

```
 Đan phương (đã học) + Nguyên liệu + Đan lô (trong túi) + [Hỏa chủng] + Phí linh thạch + [Thọ nguyên]
        │ tại Đan Phòng (POI ALCHEMY_STATION)
        ▼
 BẮT ĐẦU PHIÊN: tiêu nguyên liệu / phí / thọ (AL-R-002), server cấp seed
        ▼
 ┌─ MINIGAME "khống chế hỏa hầu" (20–40s) ───────────────┐      ┌─ LUYỆN TỰ ĐỘNG ─────────┐
 │ giữ nhiệt trong vùng mục tiêu di động                 │  hoặc │ điểm cố định 0,55       │
 │ cho nguyên liệu phụ vào đúng thời điểm                │      │ xếp hàng tối đa 10 lượt │
 │ client gửi chuỗi input → server mô phỏng lại → điểm   │      └─────────────────────────┘
 └───────────────────────────────────────────────────────┘
        ▼
 roll THÀNH / BẠI  (đan phương, cấp, đan lô, điểm, bảo hiểm, thuần thục)
   bại → Đan tra (phế đan), bảo hiểm +5
   thành → roll PHẨM CHẤT (Hạ · Trung · Thượng · Cực) → [đan văn] → [bạo đan +1] → số lượng
        ▼
 kinh nghiệm luyện đan sư, thuần thục đan phương
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type CraftMode = 'MINIGAME' | 'AUTO';
type RecipeKind = 'ALCHEMY' | 'FORGING';            // FORGING dùng ở GD-11
type InputRole = 'MAIN' | 'AUX' | 'CATALYST';

interface RecipeDef {
  id: string;                         // RCP_*
  kind: RecipeKind;                   // 'ALCHEMY'
  nameKey: string;
  outputTemplateId: string;           // ITM_* (đan)
  levelReq: number;                   // cấp luyện đan sư 1..9
  furnaceTierReq: number;             // 1..5
  inputs: RecipeInput[];
  yield: [number, number];            // số viên mỗi mẻ
  durationSec: number;                // thời lượng minigame / tự động
  heatProfile: { points: [number, number][]; bandWidth: number };  // [tỷ lệ thời gian 0..1, nhiệt mục tiêu 0..100]
  addStages: number[];                // thời điểm (tỷ lệ 0..1) cho từng đợt thêm nguyên liệu phụ
  baseSuccess: number;                // điểm %
  exp: number;                        // kinh nghiệm luyện đan sư mỗi mẻ
  lifespanYears?: number;             // tiêu thọ khi bắt đầu (GD-26 CRAFTING)
  tags: string[];                     // "healing", "cultivation", "breakthrough", "antidote"...
}

interface RecipeInput {
  templateId?: string;                // một trong hai: templateId cụ thể
  tag?: string; minGrade?: ItemGrade; //            hoặc nhóm nguyên liệu (vd. tag "herb_moc", phẩm ≥ LINH)
  qty: number;                        // mỗi mẻ
  role: InputRole;
  addStage: number;                   // 0 = cho vào lúc đầu; k ≥ 1 = đợt thêm thứ k (index vào addStages + 1)
}

interface FurnaceSpec {               // ItemTemplate.furnace (category EQUIPMENT? không: category 'TOOL', xem §7)
  tier: number;                       // 1..5
  successFlat: number; qualityFlat: number;
  maxBatches: number;                 // số mẻ tối đa trong một lần luyện
  stability: number;                  // 0..0,6: giảm biên độ nhiễu nhiệt
}
interface FireSpec {                  // ItemTemplate.fire — tiêu hao mỗi lần luyện
  qualityFlat: number; stabilityBonus: number;
}

interface CraftProfile {              // bảng profession (PLAN §5.4), type ALCHEMY
  characterId: string; kind: RecipeKind;
  level: number; exp: number;
}
interface KnownRecipe {
  characterId: string; recipeId: string;
  crafts: number;                     // số mẻ đã luyện thành (độ thuần thục, §5.7)
  pityFails: number;                  // "hỏa hầu tâm đắc"
  source: 'STARTER' | 'NPC' | 'ITEM' | 'ENCOUNTER' | 'PAST_LIFE';
}

interface MinigameEvent { tMs: number; type: 'FIRE_ON' | 'FIRE_OFF' | 'ADD' }

interface CraftSession {
  sessionId: string; characterId: string; recipeId: string;
  mode: CraftMode; batches: number;
  furnaceInstanceId: string; fireInstanceId: string | null;
  seed: string;
  startedAt: string; endsAt: string;
  state: 'RUNNING' | 'COMPLETED' | 'CANCELLED';
  score: number | null;
  result: { success: boolean; quality?: PillQuality; pattern?: boolean; qty: number; outputInstanceIds: string[] } | null;
}
```

---

## 4. Máy trạng thái `CraftSession`

| Từ | Đến | Điều kiện | Tác dụng phụ |
|----|-----|-----------|--------------|
| — | `RUNNING` | Điều kiện AL-R-030 thỏa | Transaction: tiêu nguyên liệu × mẻ, hỏa chủng, phí (GD-13), thọ nguyên (GD-26); cấp `seed`; nhân vật vào `CHANNELING` (GD-07) |
| `RUNNING` | `CANCELLED` | Người chơi hủy trong `params.cancelGraceSec` (3s) đầu | **Hoàn** toàn bộ: nguyên liệu, phí, hỏa chủng; thọ nguyên hoàn qua `lifespan-change` với `reasonCode = CRAFTING_REFUND` |
| `RUNNING` | `COMPLETED` | Minigame: client gửi input (hoặc hết `durationSec + params.submitGraceSec`); Tự động: hết `durationSec` | Tính kết quả (§6), cấp vật phẩm, kinh nghiệm |
| `RUNNING` | `COMPLETED` (điểm tự động) | Hủy sau thời gian ân hạn, mất kết nối, bị tấn công | Tính như `AUTO` (`score = autoScore × params.interruptedScoreMult (0,8)`) |

---

## 5. Quy tắc

### 5.1. Nơi luyện và điều kiện
- <a id="al-r-030"></a>**AL-R-030** Bắt đầu phiên khi:
  1. Nhân vật đứng trong bán kính 3m của POI `ALCHEMY_STATION` (Đan Phòng ở `HUB`; sau này: động phủ, tông môn), không trong chiến đấu, không có phiên chế tác khác.
  2. Đã biết đan phương; cấp luyện đan sư ≥ `levelReq`.
  3. Có đan lô trong `BAG` với `tier ≥ furnaceTierReq`; số mẻ ≤ `maxBatches` của đan lô.
  4. Đủ nguyên liệu × số mẻ, đủ phí, (nếu có) đủ hỏa chủng.
  5. Nếu `lifespanYears > 0`: người chơi đã xác nhận hộp thoại tiêu thọ (GD-26 LC-R-042).
- <a id="al-r-031"></a>**AL-R-031** Trong phiên, nhân vật ở `CHANNELING`: không di chuyển, quái không vào Đan Phòng (vùng an toàn `HUB`).
- <a id="al-r-032"></a>**AL-R-032** **Luyện tự động** có thể **xếp hàng** tối đa `params.autoQueueMax` (10) phiên liên tiếp cùng đan phương; mỗi phiên vẫn tiêu nguyên liệu khi bắt đầu phiên đó. Hàng dừng khi thiếu nguyên liệu hoặc người chơi di chuyển.
- <a id="al-r-033"></a>**AL-R-033** Hủy trong 3s đầu: hoàn hết (nhầm nút). Sau đó: hủy / mất kết nối / bị ngắt → tính điểm tự động × 0,8 (§4).

### 5.2. Minigame "khống chế hỏa hầu"
- <a id="al-r-040"></a>**AL-R-040** Mô phỏng tất định ở 20 tick/giây (50ms), dùng chung TS (client dự đoán) và server (chấm điểm):
  - Nhiệt độ `T` bắt đầu 20, trong [0, 100]. Giữ phím (FIRE_ON): `T += riseRate × dt` (30/s); thả (FIRE_OFF): `T −= fallRate × dt` (20/s).
  - **Nhiễu hỏa khí:** từ seed, cứ mỗi 3–6s một lần, cộng xung ±(8–15) × (1 − stability) trải đều trong 0,5s. `stability = furnace.stability + fire.stabilityBonus` (trần 0,6).
  - **Vùng mục tiêu:** tâm `c(t)` nội suy tuyến tính theo `heatProfile.points`; nửa bề rộng `w = bandWidth / 2`.
  - **Thêm nguyên liệu:** mỗi đợt k có thời điểm chuẩn `t_k = addStages[k] × durationSec`; người chơi bấm `ADD` (phím Space) trong ±1,0s.
  - **Tạc lô:** `T ≥ 98` cộng dồn ≥ 1,5s → phiên kết thúc ngay với **thất bại** (không hỏng đan lô).
- <a id="al-r-041"></a>**AL-R-041** Điểm (§6.1) trong [0, 1]: 70% thời gian trong vùng + 30% độ chính xác thêm nguyên liệu (không có đợt thêm thì 100% theo nhiệt).
- <a id="al-r-042"></a>**AL-R-042** Client gửi `MinigameEvent[]` một lần khi kết thúc. Server kiểm tra: thời gian tăng dần, `0 ≤ tMs ≤ durationSec × 1000`, ≤ 6 sự kiện/giây, số `ADD` ≤ số đợt. Vi phạm → điểm = `autoScore × 0,8` và log cảnh báo bảo mật.
- <a id="al-r-043"></a>**AL-R-043** Client hiển thị điểm dự đoán theo thời gian thực; kết quả cuối theo server (cùng hàm, cùng seed nên thường trùng khớp).

### 5.3. Thành / bại, phẩm chất, đan văn, bạo đan
- <a id="al-r-050"></a>**AL-R-050** Roll thành công theo §6.2 (RNG server, `rng_audit` kind `ALCHEMY_SUCCESS`). Tạc lô luôn là thất bại. Seed kết quả **độc lập** với seed minigame của phiên, sinh lúc chấm ([GD-29 RF-R-050](05-rng-fairness.md)).
- <a id="al-r-051"></a>**AL-R-051** Thất bại: mỗi mẻ cho 1 **Đan Tra** (`ITM_DAN_TRA`, phế đan, bán NPC 1 linh thạch, dùng làm nguyên liệu `CATALYST` cho một số đan phương); `pityFails += 1`; kinh nghiệm × 0,3.
- <a id="al-r-052"></a>**AL-R-052** Thành công: tính điểm phẩm chất `Q` (§6.3) → `PillQuality`: `Q < 40` Hạ · 40–69 Trung · 70–89 Thượng · ≥ 90 Cực. Mọi viên trong lần luyện cùng phẩm chất. `pityFails = 0`; `crafts += số mẻ`.
- <a id="al-r-053"></a>**AL-R-053** **Đan văn:** khi `Q ≥ 85`, roll xác suất §6.4; trúng thì mọi viên có thuộc tính `AFX_PILL_PATTERN` (GD-09 `affixes`): hiệu ứng chính của đan ×1,2 (tu vi, hồi máu, thời lượng status, lượng giải độc); đan tu luyện có đan văn cộng đan độc ×0,8. Viên có đan văn không cộng chồng với viên không có (GD-09 IT-R-011).
- <a id="al-r-054"></a>**AL-R-054** **Bạo đan:** roll xác suất §6.4; trúng thì **+1 viên mỗi mẻ**.

### 5.4. Chi phí
- <a id="al-r-060"></a>**AL-R-060** **Phí** (GD-13 EC-R-023): `round(0,05 × value(output) × yieldTrungBình × số mẻ)`, tối thiểu 1, `reasonCode = CRAFT_FEE`.
- <a id="al-r-061"></a>**AL-R-061** **Thọ nguyên:** đan phương có `lifespanYears` (đan đột phá, Diên Thọ Đan...) tiêu `lifespanYears × số mẻ` khi bắt đầu, qua GD-26 `lifespan-change` (`USED`, `CRAFTING`, ref `sessionId`). Thất bại không hoàn (thời gian đã tiêu).
- <a id="al-r-062"></a>**AL-R-062** Nguyên liệu tiêu qua `inventory/consume` (`CRAFT_INPUT`), nguyên liệu `MAIN` ưu tiên chồng phẩm thấp nhất thỏa `minGrade`. Sản phẩm qua `inventory/grant` (`CRAFT_OUTPUT`, `crafterCharacterId`, `overflow = MAIL`).

### 5.5. Loại đan
- <a id="al-r-070"></a>**AL-R-070** Sản phẩm là mẫu vật phẩm GD-09 với `UseSpec` tương ứng:

| Nhóm | `UseSpec.kind` | Ảnh hưởng của phẩm chất | Ví dụ |
|------|----------------|------------------------|-------|
| Hồi phục | `HEAL` | Lượng hồi × hệ số phẩm chất (GD-09 IT-R-073) | Hồi Khí Đan, Hồi Linh Đan |
| Buff chiến đấu | `STATUS` | Thời lượng × hệ số phẩm chất | Cầm Huyết Đan, Tị Độc Đan |
| Tu luyện | `CULTIVATION` | Tu vi × `qualityExpMult`, đan độc × `qualityToxMult` (GD-04 §6.5) | Tụ Khí Đan, Ngưng Khí Đan |
| Đột phá | (vật phẩm trong `pillItemIds`, GD-04) | Cộng tỷ lệ đột phá: Hạ 0 · Trung +10 · Thượng +20 · Cực +30 (GD-04 §6.6) | Trúc Cơ Đan, Kết Kim Đan |
| Giải độc / tâm | `DETOX`, `HEART_DEMON_CURE` | Lượng giải độc × hệ số; tâm ma: Cực phẩm gỡ thêm 1 tầng | Giải Độc Đan, Thanh Tâm Đan |
| Diên thọ | `LIFESPAN_EXTENSION` | `baseYears` × hệ số phẩm chất, rồi giảm dần theo GD-26 §6.3 | Diên Thọ Đan (Beta) |

- <a id="al-r-071"></a>**AL-R-071** **Đan phương Trúc Cơ Đan** (`RCP_TRUC_CO_DAN`) luôn mua được ở mọi con đường (GD-14 SO-R-04N): Bảo Khố cấp 1 của mọi đại tông NPC (600 cống hiến), Tông Khố tông tự lập cấp 2 (300 merit), Tạp Hóa `SHOP_V1_GENERAL` (2.000 linh thạch, `LK7`) và rơi ở BC03; nguyên liệu chính `ITM_NGUNG_CO_THAO` chỉ có ở hiểm địa `V1_HD01` (GD-28). Đây là vòng lặp "hiểm địa → luyện đan → đột phá" của Vực 1.

### 5.6. Đan lô và hỏa chủng
- <a id="al-r-080"></a>**AL-R-080** Đan lô là vật phẩm (category `TOOL`, GD-09) nằm trong `BAG`, không tiêu hao, không hỏng. Hỏa chủng (category `MATERIAL`, tag `fire`) tiêu 1 mỗi lần luyện (không phụ thuộc số mẻ).

| ID | Tên | Tier | `successFlat` | `qualityFlat` | `maxBatches` | `stability` | Nguồn |
|----|-----|:---:|:---:|:---:|:---:|:---:|-------|
| `ITM_FURNACE_T1` | Thanh Đồng Đan Lô | 1 | 0 | 0 | 1 | 0 | Dược Điếm (NPC), Dược đồng khởi đầu |
| `ITM_FURNACE_T2` | Tử Kim Đan Lô | 2 | +5 | +5 | 2 | 0,2 | Tông Môn Bảo Khố, BC02 |
| `ITM_FURNACE_T3` | Huyền Hỏa Đan Lô | 3 | +8 | +10 | 3 | 0,35 | BC03, rương hiểm địa Nhị giai |
| `ITM_FIRE_BEAST` | Thú Hỏa Tinh (hỏa chủng) | — | — | +8 | — | +0,1 | Tinh anh Vực 1 |

### 5.7. Cấp luyện đan sư và thuần thục đan phương
- <a id="al-r-090"></a>**AL-R-090** Cấp 1–9 (Nhất phẩm → Cửu phẩm luyện đan sư), kinh nghiệm theo §6.5. MVP tới cấp 4.
- <a id="al-r-091"></a>**AL-R-091** **Thuần thục đan phương** theo số mẻ luyện thành `crafts`: 0–9 → 1 · 10–29 → 2 · 30–79 → 3 · ≥ 80 → 4. Mỗi cấp trên 1: +3 điểm thành công, +3 điểm phẩm chất.
- <a id="al-r-092"></a>**AL-R-092** **Hỏa hầu tâm đắc** (bảo hiểm): +5 điểm thành công mỗi lần thất bại liên tiếp với cùng đan phương, trần +25; reset khi thành công.

### 5.8. Học đan phương
- <a id="al-r-100"></a>**AL-R-100** Học bằng vật phẩm `RECIPE` (`UseSpec.LEARN_RECIPE`, GD-09) mua ở cửa hàng, rơi đồ, cơ duyên. Đã biết thì không dùng được (không tiêu vật phẩm).
- <a id="al-r-101"></a>**AL-R-101** Xuất thân Dược đồng (GD-27) bắt đầu biết `RCP_HOI_KHI_DAN`, `RCP_CAM_HUYET_DAN`, có `ITM_FURNACE_T1`, kinh nghiệm luyện đan 100.
- <a id="al-r-102"></a>**AL-R-102** `UNL_PAST_LIFE_MEMORY` (GD-26): khi tạo kiếp mới, chọn 1 đan phương đã biết ở kiếp trước → biết ngay (`source = PAST_LIFE`, `crafts = 0`).

---

### 5.9. Ký ức nghề & Khảo hạch (qua kiếp)
- <a id="al-r-110"></a>**AL-R-110** Cấp nghề (`profession`, dùng chung luyện đan và luyện khí GD-11) **gắn với nhân vật**: kiếp mới bắt đầu ở cấp 1. Nhưng khi hết kiếp, API Service ghi **ký ức nghề** cấp tài khoản (`profession_memory`): `maxLevel = max(maxLevel cũ, cấp kiếp này)` và hợp (union) các đan phương / khí phổ đã biết. Ký ức **không tiêu tán** (khác Di Tàng GD-26 LC-R-166): đó là kỹ năng, không phải tài sản, và kiếp nào cũng phải khảo hạch lại để dùng.
- <a id="al-r-111"></a>**AL-R-111** Cấp nghề chia ba **giai đoạn**: **Sơ** (cấp 1–3) · **Trung** (4–6) · **Cao** (7–9). Mỗi giai đoạn mở lại bằng một **Khảo hạch** tại Đan Phòng (NPC `NPC_ALCHEMY_ELDER`, Đan Đạo Trưởng Lão; luyện khí: `NPC_FORGE_ELDER` tại Lò Rèn).
- <a id="al-r-112"></a>**AL-R-112** Điều kiện dự khảo hạch giai đoạn T:
  - ký ức nghề đã từng chạm giai đoạn T (`maxLevel ≥ tierBottom[T]`);
  - cảnh giới nhân vật ≥ `trialRealm[T]` (Sơ `LK3` · Trung `TC1` · Cao `KD1`);
  - đã qua giai đoạn trước trong kiếp này, hoặc cấp hiện tại ≥ `tierTop[T−1]`;
  - còn lượt trong ngày (`trialDailyAttempts` 3 / loại nghề, reset 05:00).
- <a id="al-r-113"></a>**AL-R-113** **Khảo hạch** là một phiên minigame (§5.2) với đan phương khảo hạch `RCP_TRIAL_ALCHEMY_{SO|TRUNG|CAO}`: nguyên liệu **do trưởng lão cấp** (không tiêu đồ của người chơi), có phí linh thạch (`trialFee`, hố tiền GD-13), **không** ra sản phẩm, không tiêu thọ nguyên. Đạt khi điểm minigame ≥ `trialScore[T]` (Sơ 0,70 · Trung 0,75 · Cao 0,80). Không có roll: kết quả hoàn toàn theo kỹ năng (server chấm lại như AL-R-001).
- <a id="al-r-114"></a>**AL-R-114** **Đạt:**
  - `level = max(level hiện tại, min(maxLevel ký ức, tierTop[T]))`;
  - `exp` = ngưỡng đầu của cấp đó (`levelExpThresholds`);
  - mọi đan phương trong ký ức có `levelReq ≤` cấp mới mà chưa biết → học ngay (`recipe_known.source = PAST_LIFE`);
  - thuần thục từng đan phương và pity (`pity_fails`) **không** khôi phục (bắt đầu lại 0);
  - lần đầu qua mỗi giai đoạn trong đời tài khoản: danh hiệu "Đan đạo tái hiện · {giai đoạn}".
- <a id="al-r-115"></a>**AL-R-115** **Trượt:** mất phí và lượt ngày; không ảnh hưởng gì khác. Có thể luyện bình thường để lên cấp thay vì khảo hạch (khảo hạch là **lối tắt**, không bắt buộc).
- <a id="al-r-116"></a>**AL-R-116** Đan lô, hỏa chủng, búa rèn là **vật phẩm** → theo Di Tàng (GD-26 §5.15), không thuộc ký ức nghề.
- <a id="al-r-117"></a>**AL-R-117** `UNL_PAST_LIFE_MEMORY` (AL-R-102) vẫn giữ: cho biết ngay 1 đan phương lúc tạo nhân vật (trước cả khảo hạch Sơ).

**Ví dụ:** kiếp trước đạt luyện đan cấp 6, biết `RCP_TRUC_CO_DAN` (cấp 3) và `RCP_DIEN_THO_DAN` (cấp 4). Kiếp mới đạt `LK3`, khảo hạch Sơ được 0,72 ≥ 0,70 → cấp 3, biết Trúc Cơ Đan. Đạt `TC1`, khảo hạch Trung → cấp 6 (= ký ức), biết Diên Thọ Đan. Không thể khảo hạch Cao (ký ức chưa từng chạm cấp 7).

## 6. Công thức

### 6.1. Điểm minigame
```
heatScore = số tick có |T − c(t)| ≤ w  /  tổng số tick
addScore_k = max(0, 1 − |tAdd_k − t_k| / 1,0s)      (không bấm: 0)
addScore = trung bình addScore_k
score = addStages rỗng ? heatScore : 0,7 × heatScore + 0,3 × addScore
AUTO: score = autoScore (0,55); bị ngắt: score × 0,8
```

### 6.2. Tỷ lệ thành công
```
success% = clamp(
    baseSuccess
  + min(10 × (level − levelReq), 20)
  + furnace.successFlat
  + 40 × (score − 0,5)
  + 3 × (proficiency − 1)
  + min(5 × pityFails, 25)
, 5, 98)
```

### 6.3. Điểm phẩm chất
```
Q = 100 × score × (1 + Σ CRAFT_QUALITY_PCT[ALCHEMY])        (GD-27: TAL_STEADY_HAND +0,05; TAL_PILL_HEART +0,15)
  + furnace.qualityFlat + fire.qualityFlat
  + 3 × (proficiency − 1)
  + 2 × min(level − levelReq, 5)
  + (spiritSense − 50) × 0,1
  + (luck − 50) × 0,1
  + U(−8; 8)                                                  (RNG server)
```

### 6.4. Đan văn và bạo đan
```
patternChance = Q ≥ 85 ? clamp(0,10 + (luck − 50) × 0,002, 0, 0,25) : 0
bonusYieldChance = clamp(0,05 + (luck − 50) × 0,002, 0, 0,20)
qty = Σ_{mẻ} ( randInt(yield[0], yield[1]) + (bạo đan ? 1 : 0) )
```

### 6.5. Kinh nghiệm luyện đan sư
```
craftExp = recipe.exp × số mẻ × (thành công ? qualityExpMult : 0,3)
qualityExpMult: HA 1,0 · TRUNG 1,2 · THUONG 1,5 · CUC 2,0
Ngưỡng tích lũy lên cấp: 2 → 200 · 3 → 800 · 4 → 2.500 · 5 → 7.000 · 6 → 18.000 · 7 → 45.000 · 8 → 110.000 · 9 → 260.000
```

### 6.6. Ví dụ đầy đủ: Trúc Cơ Đan
Luyện đan sư cấp 3, `RCP_TRUC_CO_DAN` (`baseSuccess` 45, `levelReq` 3), Tử Kim Đan Lô (+5 / +5), không hỏa chủng, thuần thục 1, chưa thất bại, minigame `score = 0,8`, thần thức 50, khí vận 50:
- Thành công: `45 + 0 + 5 + 40 × 0,3 + 0 + 0 = 62%`.
- Phẩm chất (khử dao động): `100 × 0,8 + 5 = 85` → **Thượng phẩm**; nếu có Đan Tâm (+0,15): `80 × 1,15 + 5 = 97` → **Cực phẩm**.
- Phí: `value` Trúc Cơ Đan 3.000 (GD-13), `yield` [1, 1] → `round(0,05 × 3.000) = 150` linh thạch. Thọ nguyên: 1 năm.
- Đem dùng đột phá: Thượng phẩm +20, Cực phẩm +30 điểm tỷ lệ (GD-04 §6.6).

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/alchemy/params.json` | Tham số |
| `shared/gamedata/alchemy/recipes.json` | `RecipeDef[]` (`kind: ALCHEMY`) |
| `shared/gamedata/items/templates/alchemy_*.json` | Đan, đan lô (`furnace`), hỏa chủng (`fire`), đan phương (`RECIPE`), linh thảo |

```jsonc
// params.json
{
  "tickHz": 20, "startTemp": 20, "riseRate": 30, "fallRate": 20,
  "disturbIntervalSec": [3, 6], "disturbAmp": [8, 15], "disturbSpreadSec": 0.5, "maxStability": 0.6,
  "addWindowSec": 1.0, "overheatTemp": 98, "overheatMaxSec": 1.5,
  "heatWeight": 0.7, "addWeight": 0.3,
  "autoScore": 0.55, "interruptedScoreMult": 0.8, "autoQueueMax": 10,
  "cancelGraceSec": 3, "submitGraceSec": 5, "maxEventsPerSec": 6,
  "stationRadius": 3,
  "levelSuccessPerLevel": 10, "levelSuccessCap": 20, "scoreSuccessFactor": 40,
  "proficiencyThresholds": [10, 30, 80], "proficiencyBonus": 3,
  "pityPerFail": 5, "pityCap": 25, "successMin": 5, "successMax": 98,
  "qualityThresholds": { "TRUNG": 40, "THUONG": 70, "CUC": 90 },
  "qualityVariance": 8, "qualityLevelBonus": 2, "qualityLevelBonusCap": 5,
  "qualitySenseFactor": 0.1, "qualityLuckFactor": 0.1,
  "patternMinQ": 85, "patternBase": 0.10, "patternLuckFactor": 0.002, "patternMax": 0.25, "patternEffectMult": 1.2, "patternToxMult": 0.8,
  "bonusYieldBase": 0.05, "bonusYieldLuckFactor": 0.002, "bonusYieldMax": 0.20,
  "failExpMult": 0.3, "qualityExpMult": { "HA": 1.0, "TRUNG": 1.2, "THUONG": 1.5, "CUC": 2.0 },
  "levelExpThresholds": [200, 800, 2500, 7000, 18000, 45000, 110000, 260000],
  "mvpMaxLevel": 4,
  "professionTiers": { "SO": [1, 3], "TRUNG": [4, 6], "CAO": [7, 9] },          // dùng chung GD-11
  "trialRealm": { "SO": "LK3", "TRUNG": "TC1", "CAO": "KD1" },
  "trialScore": { "SO": 0.70, "TRUNG": 0.75, "CAO": 0.80 },
  "trialFee": { "SO": 100, "TRUNG": 600, "CAO": 3000 },
  "trialDailyAttempts": 3
}
```

### 7.1. Linh thảo / nguyên liệu Vực 1

| ID | Tên | Phẩm | Tag | Nơi / điều kiện (GD-21) |
|----|-----|------|-----|-------------------------|
| `ITM_HERB_THANH_DIEP` | Thanh Diệp Thảo | PHAM | `herb`, `herb_moc` | F01, F02 |
| `ITM_HERB_HUYET_LINH` | Huyết Linh Chi | PHAM | `herb`, `herb_hoa` | F02, F05 |
| `ITM_HERB_NGUYET_QUANG` | Nguyệt Quang Thảo | LINH | `herb`, `herb_thuy` | F01 (vài điểm ven suối — GD-16 `V1_F01` §7), F02, F05; chỉ `NIGHT` |
| `ITM_HERB_DUONG_VIEM` | Dương Viêm Hoa | LINH | `herb`, `herb_hoa` | F03, F06; chỉ giờ Ngọ |
| `ITM_HERB_THUY_LINH` | Thủy Linh Thảo | LINH | `herb`, `herb_thuy` | F02, F04; chỉ `RAIN` |
| `ITM_HERB_DOC_TAM` | Độc Tâm Liên | LINH | `herb`, `herb_doc` | F04 |
| `ITM_HERB_BACH_NIEN` | Bách Niên Linh Thảo | BAO | `herb`, `herb_rare` | Ẩn cấp 1 ở F04–F06; nhiều ở `V1_HD01` |
| `ITM_NGUNG_CO_THAO` | Ngưng Cơ Thảo | BAO | `herb`, `dz_exclusive` | Chỉ `V1_HD01` (GD-28) |
| `ITM_LINH_THU_HUYET` | Linh Thú Huyết | BAO | `monster_part` | Ngạ Lang, Xích Hồ (hiếm, GD-12) |
| `ITM_THUY_LINH_CHAU` | Thủy Linh Châu | LINH | `catalyst` | Cơ duyên `ENC_V1_NGU_ONG` (GD-22) |
| `ITM_DAN_TRA` | Đan Tra (phế đan) | PHAM | `catalyst` | Luyện thất bại |

### 7.2. Đan phương MVP

| ID | Đan | Cấp | Lô | Nguyên liệu (mỗi mẻ) | Ra | Thời gian | Thành công gốc | Thọ | Nguồn đan phương |
|----|-----|:---:|:---:|----------------------|:---:|:---:|:---:|:---:|------------------|
| `RCP_HOI_KHI_DAN` | Hồi Khí Đan (hồi 25% HP / 3s) | 1 | 1 | 2 Thanh Diệp + 1 Huyết Linh Chi | 3–5 | 20s | 80 | — | Dược Điếm, Dược đồng |
| `RCP_HOI_LINH_DAN` | Hồi Linh Đan (hồi 25% MP / 3s) | 1 | 1 | 2 Thanh Diệp + 1 Nguyệt Quang | 3–5 | 20s | 80 | — | Dược Điếm |
| `RCP_CAM_HUYET_DAN` | Cầm Huyết Đan (`ST_STAUNCH` 10s) | 1 | 1 | 1 Huyết Linh Chi + 1 Thanh Diệp + 1 Đan Tra | 2–3 | 25s | 75 | — | Dược đồng, Dược Điếm |
| `RCP_TU_KHI_DAN` | Tụ Khí Đan (tu luyện LK, `exp` 150, độc 8) | 1 | 1 | 3 Thanh Diệp + 1 Huyết Linh Chi (+ đợt 1: 1 Nguyệt Quang) | 2–3 | 30s | 70 | — | Dược Điếm |
| `RCP_GIAI_DOC_DAN` | Giải Độc Đan (giải 30 đan độc) | 2 | 1 | 2 Thủy Linh + 1 Thanh Diệp | 2 | 30s | 65 | — | Tông Môn Bảo Khố |
| `RCP_TI_DOC_DAN` | Tị Độc Đan (`ST_ANTI_POISON` 300s) | 2 | 1 | 1 Độc Tâm Liên + 2 Thanh Diệp (+ đợt 1: 1 Thủy Linh) | 2–3 | 30s | 65 | — | Tông Môn Bảo Khố |
| `RCP_THANH_TAM_DAN` | Thanh Tâm Đan (gỡ 1 tầng tâm ma) | 3 | 2 | 2 Nguyệt Quang + 1 Bách Niên (+ đợt 1: 1 Thủy Linh Châu) | 1 | 35s | 55 | — | Cơ duyên, BC02 |
| `RCP_TRUC_CO_DAN` | **Trúc Cơ Đan** | 3 | 2 | 1 Ngưng Cơ Thảo + 2 Bách Niên (+ đợt 1: 1 Linh Thú Huyết, đợt 2: 1 Dương Viêm Hoa) | 1 | 40s | 45 | 1 năm | Tông Môn Bảo Khố (AL-R-071), BC03 |
| `RCP_NGUNG_KHI_DAN` | Ngưng Khí Đan (tu luyện TC, `exp` 900, độc 9) | 4 | 2 | (nguyên liệu Vực 2) | 2–3 | 35s | 60 | — | Vực 2 (Beta) |
| `RCP_DIEN_THO_DAN` | Diên Thọ Đan (+20 năm gốc) | 4 | 3 | (Diên Thọ Quả, Tam giai) | 1 | 45s | 40 | 3 năm | Beta |

```jsonc
// recipes.json (trích)
{ "id": "RCP_TRUC_CO_DAN", "kind": "ALCHEMY", "nameKey": "rcp.truc_co_dan", "outputTemplateId": "ITM_TRUC_CO_DAN",
  "levelReq": 3, "furnaceTierReq": 2,
  "inputs": [ { "templateId": "ITM_NGUNG_CO_THAO", "qty": 1, "role": "MAIN", "addStage": 0 },
              { "templateId": "ITM_HERB_BACH_NIEN", "qty": 2, "role": "MAIN", "addStage": 0 },
              { "templateId": "ITM_LINH_THU_HUYET", "qty": 1, "role": "AUX", "addStage": 1 },
              { "templateId": "ITM_HERB_DUONG_VIEM", "qty": 1, "role": "AUX", "addStage": 2 } ],
  "yield": [1, 1], "durationSec": 40,
  "heatProfile": { "points": [[0, 40], [0.3, 60], [0.6, 75], [0.85, 70], [1, 55]], "bandWidth": 14 },
  "addStages": [0.35, 0.7],
  "baseSuccess": 45, "exp": 150, "lifespanYears": 1, "tags": ["breakthrough"] }
```

**Validator (CI):** `RCP_*` duy nhất; `outputTemplateId`, `templateId` tồn tại; `tag` có ít nhất một vật phẩm khớp; mọi `addStage` ≤ `addStages.length`; `heatProfile.points` tăng dần theo thời gian, nhiệt trong [10, 95]; `bandWidth` trong [8, 30]; đan phương có tag `breakthrough` có `lifespanYears ≥ 1`; vật phẩm đầu ra không bán ở NPC trên Hạ phẩm (GD-13 EC-R-002).

---

## 8. Mục tiêu cân bằng ban đầu

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Tỷ lệ thành công Trúc Cơ Đan với chuẩn bị điển hình (cấp 3, lô T2, điểm 0,7) | 55–65% |
| Phân phối phẩm chất người chơi giỏi minigame (điểm 0,8–0,9) | Thượng phẩm chiếm đa số |
| Luyện tự động so với minigame trung bình | Thành công thấp hơn ~10 điểm, phẩm chất thường Trung |
| Thời gian từ đạt LK9 tới có Trúc Cơ Đan đầu tiên (gồm đi HD01) | 2–4 giờ chơi |
| Đan chiếm tỷ trọng tu vi (GD-04 §2) | 35–40% |

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai
> **Đường dẫn, method và khóa idempotency chuẩn ở [TD-04 §5](../tech/03-api.md)** (mọi thao tác của nhân vật nằm dưới `/v1/characters/{id}/…`, xem trước dùng `POST`, khóa idempotency qua header `Idempotency-Key`). Bảng dưới mô tả mục đích; khi vênh với TD-04, theo TD-04.

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/characters/{id}/alchemy` | Cấp, kinh nghiệm, đan phương đã biết (thuần thục, bảo hiểm) | |
| `GET /v1/characters/{id}/alchemy/preview` | `?recipeId&furnaceInstanceId&fireInstanceId&batches&mode` → `{ eligible, reasons[], fee, lifespanYears, successAt: { auto, score07, score09 }, qualityOddsAt: {...}, inputsAvailable }` | AL-R-004 |
| `POST /v1/characters/{id}/alchemy/sessions` | `{ recipeId, furnaceInstanceId, fireInstanceId?, batches, mode, queueCount?, confirmLifespan? }` → `{ sessionId, seed, durationSec, heatProfile, addStages, stability }` | AL-R-030..033, AL-R-002 |
| `GET /v1/characters/{id}/professions/{kind}/trials` | Giai đoạn có thể khảo hạch, lượt còn lại, ký ức nghề | §5.9 |
| `POST /v1/characters/{id}/professions/{kind}/trials` | `{ tier }` → phiên minigame khảo hạch (nộp kết quả như phiên thường) | AL-R-112..115; `CharacterContext` gần trưởng lão |
| `POST /v1/alchemy/sessions/{sessionId}/submit` | `{ events: MinigameEvent[] }` → `{ score, success, quality, pattern, qty, outputs }` | AL-R-040..043; idempotent theo `sessionId` |
| `POST /v1/alchemy/sessions/{sessionId}/cancel` | Hủy | AL-R-033 |

Game Server xác nhận vị trí trạm và trạng thái chiến đấu khi tạo phiên (API Service đọc `CharacterContext` do Game Server ghi, [TD-01 §11](../tech/00-tdd.md)).

### 9.2. Gọi sang hệ thống khác (trong transaction bắt đầu / kết thúc)
`inventory/consume` (`CRAFT_INPUT`), `currency/change` (`CRAFT_FEE`), `lifecycle/lifespan-change` (`CRAFTING` / `CRAFTING_REFUND`), `inventory/grant` (`CRAFT_OUTPUT`).

### 9.3. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `S2C_CraftState` | S→C (AOI) | `{ characterId, crafting: boolean, recipeId? }` (hiệu ứng lửa đan lô cho người xung quanh) |
| `S2C_CraftResult` | S→C | `{ sessionId, success, quality?, pattern?, qty, bonusYield, levelUp? }` |

### 9.4. Sự kiện domain

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `craft.completed` | `{ characterId, kind: 'ALCHEMY', recipeId, success, quality, qty, mode, score }` | Telemetry, nhiệm vụ (GD-15), Bia Vạn Cổ (Cực phẩm đan đột phá đầu tiên) |
| `craft.level_up` | `{ characterId, kind, level }` | Thông báo, danh hiệu |

---

## 10. Lưu trữ

```sql
CREATE TABLE profession (                         -- PLAN §5.4, dùng chung GD-11
  character_id UUID NOT NULL REFERENCES character(id),
  kind VARCHAR(16) NOT NULL,                      -- ALCHEMY | FORGING
  level SMALLINT NOT NULL DEFAULT 1,
  exp INT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, kind)
);

CREATE TABLE profession_memory (                  -- §5.9, cấp tài khoản, dùng chung GD-11
  account_id UUID NOT NULL, kind VARCHAR(16) NOT NULL,      -- ALCHEMY | FORGING
  max_level SMALLINT NOT NULL, recipe_ids JSONB NOT NULL, updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, kind)
);
CREATE TABLE profession_trial (                   -- lượt + kết quả khảo hạch
  id UUID PRIMARY KEY, character_id UUID NOT NULL, kind VARCHAR(16) NOT NULL, tier VARCHAR(8) NOT NULL,
  session_id UUID NOT NULL, score NUMERIC(5,4), passed BOOLEAN, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE recipe_known (                       -- PLAN §5.4
  character_id UUID NOT NULL REFERENCES character(id),
  recipe_id VARCHAR(48) NOT NULL,
  crafts INT NOT NULL DEFAULT 0,
  pity_fails INT NOT NULL DEFAULT 0,
  source VARCHAR(16) NOT NULL,
  learned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, recipe_id)
);

CREATE TABLE craft_session (
  session_id UUID PRIMARY KEY,
  character_id UUID NOT NULL REFERENCES character(id),
  recipe_id VARCHAR(48) NOT NULL,
  mode VARCHAR(16) NOT NULL, batches SMALLINT NOT NULL,
  furnace_instance_id UUID NOT NULL, fire_instance_id UUID,
  seed VARCHAR(64) NOT NULL,
  state VARCHAR(16) NOT NULL,
  events JSONB, score NUMERIC(6,4), result JSONB,
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(), ends_at TIMESTAMPTZ NOT NULL, completed_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX ux_craft_one_running ON craft_session(character_id) WHERE state = 'RUNNING';
```
Mã lý do mới: `item_log` `CRAFTING_REFUND`; `lifespan_ledger` `CRAFTING_REFUND` (GD-26 `LifespanReason` bổ sung); `rng_audit.kind` `ALCHEMY_SUCCESS`, `ALCHEMY_QUALITY`, `ALCHEMY_PATTERN`, `ALCHEMY_YIELD`, `ALCHEMY_DISTURB`.

---

## 11. UI/UX
- **Màn Đan Phòng:** trái: danh sách đan phương (lọc theo nhóm, mờ nếu thiếu cấp / nguyên liệu); giữa: đan lô 3D với ngọn lửa; phải: nguyên liệu cần (đủ / thiếu), chọn đan lô, hỏa chủng, số mẻ, chế độ, **bảng tỷ lệ** (thành công theo điểm 0,55 / 0,7 / 0,9; xác suất từng phẩm chất), phí, thọ nguyên (cảnh báo LC-R-042).
- **Minigame:** thanh nhiệt dọc cạnh đan lô; vùng mục tiêu xanh lục di chuyển; kim nhiệt độ; giữ chuột trái để thổi lửa; biểu tượng nguyên liệu phụ rơi xuống đúng lúc với vòng thời gian (bấm Space); lô rung và đỏ dần khi gần tạc lô. Điểm dự đoán góc màn hình.
- **Kết quả:** đan bay ra khỏi lô, màu ánh sáng theo phẩm chất; đan văn có hoa văn vàng; "Bạo đan!" khi được thêm viên.
- **Tooltip đan:** phẩm chất, "Do X luyện chế", đan văn, hiệu ứng đã nhân phẩm chất.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-04 | Đan tu luyện, đan đột phá (phẩm chất → tỷ lệ), Giải Độc Đan, Thanh Tâm Đan |
| GD-09 | Tiêu / cấp vật phẩm; đan lô `TOOL`, hỏa chủng; `AFX_PILL_PATTERN` |
| GD-13 | Phí chế tác; đan Trung phẩm+ là hàng hóa (Beta) |
| GD-26 | Tiêu thọ khi luyện đan cao cấp; ký ức tiền kiếp |
| GD-27 | `CRAFT_QUALITY_PCT`, khí vận, thần thức; Dược đồng |
| GD-21 | Linh thảo theo giờ / thời tiết; POI `ALCHEMY_STATION` |
| GD-28 | Ngưng Cơ Thảo, Bách Niên Linh Thảo; Tị Độc Đan cho Vạn Độc Đầm |
| GD-12 | Linh Thú Huyết, Thú Hỏa Tinh rơi từ quái |
| GD-22 | Thủy Linh Châu, đan phương từ cơ duyên |
| GD-11 | Dùng chung `RecipeDef`, `CraftSession`, minigame (biến thể "rèn") |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Gửi `submit` hai lần | Lần hai trả lại kết quả lần đầu |
| Không gửi `submit` (đóng game) | Hết `durationSec + 5s` → tính tự động × 0,8 |
| Nguyên liệu `tag` có nhiều phẩm khác nhau | Tiêu chồng phẩm thấp nhất thỏa `minGrade` (AL-R-062) |
| Túi đầy khi nhận đan | `overflow = MAIL` |
| Thọ nguyên còn ít hơn `lifespanYears` | Cho phép (cảnh báo đỏ), có thể vào `LAST_LIGHT` (GD-26) |
| Xếp hàng tự động khi đan lô bị gửi vào Vault giữa chừng | Phiên đang chạy hoàn tất; phiên kế tiếp không bắt đầu |
| Chuỗi input có tMs vượt `durationSec` | Vi phạm AL-R-042 → điểm tự động × 0,8 |
| Kiếp kết thúc khi đang luyện | Phiên bị hủy không hoàn (GD-26 bước 4 hủy mọi phiên) |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `craft_session` | recipeId, mode, score, success, quality, pattern, batches, level | Phân phối điểm, tỷ lệ thành công thực tế, chế độ phổ biến |
| `minigame_overheat` | recipeId, tAtExplode | Độ khó minigame |
| `pill_source` | quality, source (crafted / NPC / drop) | Đan tự luyện có thay được đan NPC |
| `breakthrough_pill_quality` | quality khi dùng đột phá | Người chơi có đầu tư phẩm chất trước đột phá |
| `alchemy_level_time` | level, playHours | Tốc độ lên cấp |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="al-ac-001"></a>AL-AC-001 | Ví dụ §6.6 | Tính | Thành công 62%; Q 85 (Thượng) / 97 với Đan Tâm (Cực); phí 150; thọ −1 năm |
| AL-AC-002 | Cùng seed + cùng chuỗi input | Mô phỏng client và server | Điểm giống hệt (sai số ≤ 1e-9) |
| AL-AC-003 | Chuỗi input 20 sự kiện trong 1s | Server | Điểm = 0,55 × 0,8 = 0,44, log cảnh báo |
| AL-AC-004 | Giữ lửa liên tục tới `T ≥ 98` quá 1,5s | Mô phỏng | Tạc lô → thất bại, đan lô còn nguyên |
| AL-AC-005 | Bắt đầu phiên | Kiểm tra DB | Nguyên liệu, phí, thọ nguyên đã tiêu trong cùng transaction trước khi minigame bắt đầu |
| AL-AC-006 | Hủy ở giây 2 | Hủy | Hoàn đủ nguyên liệu, phí, thọ nguyên; phiên `CANCELLED` |
| AL-AC-007 | Hủy ở giây 10 | Hủy | Phiên `COMPLETED` với điểm 0,44 |
| AL-AC-008 | Thất bại 3 lần liên tiếp | Lần 4 | +15 điểm thành công từ bảo hiểm; thành công thì reset về 0 |
| AL-AC-009 | Luyện thành 10 mẻ một đan phương | Lần tiếp theo | Thuần thục 2: +3 thành công, +3 phẩm chất |
| AL-AC-010 | Thất bại 2 mẻ | Kết quả | 2 Đan Tra; kinh nghiệm = `exp × 2 × 0,3` |
| AL-AC-011 | Q = 88, khí vận 75, 100.000 lần | Mô phỏng | Tỷ lệ đan văn ≈ 15% |
| AL-AC-012 | Khí vận 50, 100.000 lần | Mô phỏng | Tỷ lệ bạo đan ≈ 5% |
| AL-AC-013 | Tụ Khí Đan có đan văn, Trung phẩm | Dùng (GD-04) | Tu vi ×1,2; đan độc cộng ×0,8 |
| AL-AC-014 | Viên có đan văn và viên không có, cùng phẩm chất | Gộp chồng | Từ chối |
| AL-AC-015 | Cấp luyện đan 2, đan phương cấp 3 | Bắt đầu | Từ chối (`levelReq`) |
| AL-AC-016 | Đan lô T1, đan phương cần T2 | Bắt đầu | Từ chối |
| AL-AC-017 | Đan lô T2 (`maxBatches` 2), chọn 3 mẻ | Bắt đầu | Từ chối |
| AL-AC-018 | Xếp hàng tự động 10 lượt, nguyên liệu đủ 4 | Chạy | 4 phiên hoàn tất, hàng dừng, báo thiếu nguyên liệu |
| AL-AC-019 | Dược đồng mới tạo | Mở Đan Phòng | Biết `RCP_HOI_KHI_DAN`, `RCP_CAM_HUYET_DAN`; có đan lô T1; kinh nghiệm 100 |
| AL-AC-020 | Đứng cách Đan Phòng 5m | Bắt đầu | Từ chối (bán kính 3m) |
| AL-AC-021 | Đang trong chiến đấu | Bắt đầu | Từ chối |
| AL-AC-022 | Tông Môn Bảo Khố | Danh mục | Luôn có `RCP_TRUC_CO_DAN` (AL-R-071) |
| AL-AC-023 | Kiếp mới có `UNL_PAST_LIFE_MEMORY`, kiếp trước biết `RCP_TRUC_CO_DAN` | Tạo nhân vật, chọn đan phương đó | Biết ngay, `source = PAST_LIFE`, `crafts = 0` |
| AL-AC-024 | Ví dụ §5.9 (ký ức cấp 6) | Khảo hạch Sơ ở `LK3`, điểm 0,72 | Cấp 3, exp = 800, biết `RCP_TRUC_CO_DAN` (`PAST_LIFE`) |
| AL-AC-025 | Như trên, `LK9` | Khảo hạch Trung | Từ chối (cần `TC1`) |
| AL-AC-026 | Ký ức cấp 6 | Khảo hạch Cao | Từ chối (ký ức chưa chạm cấp 7) |
| AL-AC-027 | Điểm khảo hạch 0,69 | Kết quả | Trượt, mất phí + 1 lượt, cấp không đổi |
| AL-AC-028 | Cấp hiện tại 5 (tự luyện), ký ức 4 | Khảo hạch Trung | Cấp giữ 5 (không giảm) |
| AL-AC-029 | Kiếp này đạt cấp 8, ký ức cũ 6 | Hết kiếp | `profession_memory.max_level = 8`, đan phương hợp lại |
| AL-AC-030 | Khảo hạch | Kiểm tra túi | Không tiêu nguyên liệu của người chơi, không ra sản phẩm |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 2 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Minigame hỏa hầu (server mô phỏng lại), luyện tự động | ✅ (thô) | ✅ | | |
| Thành / bại, phẩm chất, bảo hiểm, thuần thục, cấp 1–4 | | ✅ | | |
| Đan văn, bạo đan, hỏa chủng | | ✅ | | |
| 8 đan phương Vực 1 (đến Trúc Cơ Đan) | | ✅ | | |
| Xếp hàng tự động | | ✅ | | |
| Ngưng Khí Đan, Diên Thọ Đan, Kết Kim Đan (Vực 2) | | | ✅ | |
| Cấp 5–9, Linh hỏa / dị hỏa ảnh hưởng luyện đan | | | ✅ | ✅ |
| Đan Phòng tông môn tự lập (buff luyện đan, GD-14 SO-R-056) | | | ✅ | |
| Bán đan qua Vạn Bảo Lâu (GD-13) | | | ✅ | |

---

## 17. Câu hỏi mở
- [x] **AL-Q-1:** *(Đã chốt 2026-10-23: **ký ức nghề + khảo hạch theo giai đoạn** Sơ / Trung / Cao — §5.9, áp dụng cả luyện khí.)* Cấp luyện đan sư mất khi hết kiếp có quá nặng với người chơi chuyên nghề?
- [ ] **AL-Q-2** (phím minigame là ngữ cảnh riêng — GD-24 CAM-R-020): Minigame dùng chuột trái giữ lửa có xung đột thao tác không? Phương án: phím W/S.
- [ ] **AL-Q-3:** MVP không có giao dịch: người không thích luyện đan vẫn bắt buộc luyện Trúc Cơ Đan. Có nên cho NPC "luyện thuê" (đưa nguyên liệu + phí cao, điểm tự động) không?
- [ ] **AL-Q-4:** Có hiển thị nhiễu hỏa khí trước 0,5s (gợi ý) để người chơi phản ứng kịp, hay giữ bất ngờ?
- [ ] **AL-Q-5:** Đan văn ×1,2 có làm đan Cực phẩm + đan văn quá mạnh với đan đột phá? (đan đột phá hiện chỉ dùng phẩm chất, đan văn không cộng thêm tỷ lệ).

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-21 | — | Bản nháp đầu: bất biến, khung `RecipeDef` / `CraftSession` dùng chung với GD-11, máy trạng thái phiên, minigame hỏa hầu tất định (server mô phỏng lại), thành / bại, phẩm chất, đan văn, bạo đan, chi phí (phí, thọ nguyên), loại đan, đan lô & hỏa chủng, cấp luyện đan sư & thuần thục, học đan phương, công thức + ví dụ Trúc Cơ Đan, 11 nguyên liệu, 10 đan phương, API, DB, nghiệm thu |
| v0.2 | 2026-10-23 | — | Chốt AL-Q-1: **ký ức nghề** cấp tài khoản + **khảo hạch theo giai đoạn** Sơ / Trung / Cao (§5.9) — khôi phục cấp và đan phương kiếp trước bằng minigame kỹ năng, không roll; bảng `profession_memory`, `profession_trial`; AL-AC-024..030 |
| v0.3 | 2026-10-25 | — | Trỏ đường dẫn API chuẩn sang TD-04 §5 |
| v0.4 | 2026-10-25 | — | AL-Q-2 trỏ GD-24 CAM-R-020 |
| v0.5 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
| v0.6 | 2026-10-25 | — | Giá Tạp Hóa 2.000 (GD-03 C6) |
| v0.7 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: `ITM_HERB_NGUYET_QUANG` mọc thêm ở F01 (vài điểm ven suối, chỉ `NIGHT`) |

