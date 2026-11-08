# GD-27 — Thiên phú & Xuất thân

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-27 · Prefix ID: `TLN` |
| Owner | Game Designer |
| Trạng thái | 🟡 Bản nháp v0.5 |
| Cần trước | Phase 1 (bản thô 10–15 thiên phú), Phase 2 (đầy đủ) |
| Quyết định nền | PLAN D-04, D-12, D-17 |
| Liên quan | [PLAN §4.3](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-21 Open World](22-open-world.md) · GD-04 Tu luyện · GD-06 Chỉ số & Công thức · GD-07 Kỹ năng · GD-29 RNG |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Tham số nằm ở `shared/gamedata/talents/*.json` (§7).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-27 | Mục |
|--------------|-----------------|-----|
| GD-26 | `innateLifespanBonusYears`, `talentLifespanPct`, xuất thân / pool / lượt roll mở bằng Luân Hồi, `rootMultiplier` cho bế quan | §5.4, §5.6, §6.2 |
| GD-22 | Điều kiện (linh căn, thiên phú, xuất thân, chỉ số bẩm sinh); outcome `ADD_TALENT`, `REMOVE_TALENT`, `MODIFY_ROOT`, `CHANGE_INNATE_STAT`; `TAL_PAST_WISDOM` | §5.8, §9.1 |
| GD-21 | Thần thức → bán kính quét, cấp thần thức | §5.5, §6.4 |
| GD-04 | `rootMultiplier`, thức tỉnh khi đột phá, status `HEART_DEMON` | §5.8, §5.10, §6.2 |

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện*.

**Người chơi cần cảm thấy:**
- Tạo nhân vật là **lần roll cơ duyên đầu tiên**: hồi hộp, mong chờ, muốn chụp màn hình khoe.
- **Không có roll "rác":** kết quả nào cũng chơi được và có hướng build riêng.
- Thiên phú **mở ra câu chuyện**: có những cơ duyên chỉ người mang thiên phú / xuất thân đó mới gặp.
- Thiên phú **có thể thay đổi** qua những khoảnh khắc lớn (đột phá, tẩy tủy, dung hợp), nên kiếp nào cũng có cơ hội lật ngược.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="tln-r-001"></a>**TLN-R-001** | Mọi roll (tạo nhân vật, thức tỉnh, dung hợp) do **API Service** thực hiện bằng RNG an toàn, lưu seed và kết quả vào `rng_audit`. Client không bao giờ tự roll. |
| <a id="tln-r-002"></a>**TLN-R-002** | Không bán bằng tiền thật: lượt roll, khóa dòng, thiên phú, xuất thân, vật phẩm tẩy tủy / dung hợp (D-17, GD-26 LC-R-005). |
| <a id="tln-r-003"></a>**TLN-R-003** | Tổng 4 chỉ số bẩm sinh khi roll luôn bằng `innateStatTotal` trước khi cộng xuất thân (§6.1). Không có bộ chỉ số "toàn thấp". |
| <a id="tln-r-004"></a>**TLN-R-004** | Pool thiên phú lúc tạo nhân vật **không chứa thiên phú tiêu cực thuần** (`polarity = NEGATIVE`). Thiên phú có mặt trái luôn kèm mặt lợi (`DUAL`). |
| <a id="tln-r-005"></a>**TLN-R-005** | Hiệu ứng thiên phú được mô tả **hoàn toàn bằng dữ liệu** (§3, `TalentEffect`). Logic đặc biệt chỉ qua `HOOK` thuộc danh sách trắng §5.9. |
| <a id="tln-r-006"></a>**TLN-R-006** | Mọi thay đổi thiên phú / linh căn / chỉ số bẩm sinh sau khi tạo nhân vật đi qua **một cổng duy nhất** `talents/change` (§9.1), idempotent, ghi `talent_change_log`. |

---

## 2. Tổng quan

```
 Màn Luân hồi (GD-26) ─► CHỌN XUẤT THÂN ─► ROLL ───────────────────────────────┐
                                           │ Dòng 1  Linh căn   (hệ + độ tinh khiết)
                                           │ Dòng 2  Bẩm sinh   (4 chỉ số + thọ nguyên gốc)
                                           │ Dòng 3–5 Thiên phú ×3
                                           ▼
                              ROLL LẠI (giới hạn lượt, có thể khóa 1 dòng)
                              lịch sử mọi lần roll: được chọn lại bất kỳ lần nào
                                           ▼
                              ĐẶT TÊN, CHỌN NGOẠI HÌNH (preset + màu, D-08)
                                           ▼
                              XÁC NHẬN → tạo nhân vật PN1 (GD-26 §5.1) ──► hướng dẫn đầu game

 Trong kiếp:  thức tỉnh khi đột phá lớn (chọn 1/3) · cơ duyên (thêm / bớt thiên phú, tẩy tủy)
              dung hợp dị hỏa / huyết mạch (có rủi ro) · tâm ma (status tiêu cực)
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type InnateStatKey = 'constitution' | 'comprehension' | 'luck' | 'spiritSense';
type InnateStats = Record<InnateStatKey, number>;          // int

type Element = 'KIM' | 'MOC' | 'THUY' | 'HOA' | 'THO' | 'LOI' | 'BANG' | 'PHONG';
type BaseElement = 'KIM' | 'MOC' | 'THUY' | 'HOA' | 'THO';
type RootGrade = 'HEAVENLY' | 'DUAL' | 'TRIPLE' | 'QUAD' | 'PENTA';  // 1–5 hệ

interface SpiritRoot {
  elements: Element[];          // 1–5 phần tử, không trùng
  purity: number;               // int 1–100
  mutated: boolean;             // có ít nhất một hệ biến dị (LOI/BANG/PHONG)
}
// grade dẫn xuất từ elements.length: 1 HEAVENLY · 2 DUAL · 3 TRIPLE · 4 QUAD · 5 PENTA

type TalentRarity = 'COMMON' | 'RARE' | 'EPIC' | 'LEGENDARY' | 'MYTHIC';
type TalentPolarity = 'POSITIVE' | 'DUAL' | 'NEGATIVE';
type TalentSource = 'CREATION' | 'AWAKEN' | 'EVENT' | 'FUSION';

interface TalentDef {
  id: string;                   // TAL_*
  nameKey: string; descKey: string;
  rarity: TalentRarity;
  polarity: TalentPolarity;
  sources: TalentSource[];      // được xuất hiện từ nguồn nào
  poolWeight: number;           // trọng số trong cùng độ hiếm, mặc định 100
  exclusiveGroup?: string;      // tối đa 1 thiên phú mỗi nhóm trên một nhân vật
  requires?: { rootElementsAny?: Element[]; backgrounds?: string[] };
  unlockId?: string | null;     // cần unlock Luân Hồi / tài khoản (GD-26 §5.10) mới vào pool
  hiddenAtCreation?: boolean;   // hiện "???" tới khi lộ (§5.6)
  revealOn?: 'TC1' | 'KD1' | 'NA1' | 'FLAG';
  tags: string[];               // dùng cho điều kiện cơ duyên, vd. "sword", "alchemy", "demonic"
  effects: TalentEffect[];
}

type CombatStat =               // tập con; danh sách chuẩn ở GD-06 §3 (12-stats-formulas.md)
  | 'maxHp' | 'maxMp' | 'attack' | 'spellPower' | 'defense' | 'spellResist'
  | 'moveSpeed' | 'staminaRegen' | 'critRate' | 'critDamage' | 'poiseDamageTaken'
  | `elementDamage.${Element}` | 'poisonResist';

type TalentEffect =
  | { type: 'STAT_FLAT'; stat: CombatStat | InnateStatKey; value: number; when?: EffectCondition }
  | { type: 'STAT_PCT'; stat: CombatStat | InnateStatKey; value: number; when?: EffectCondition }
  | { type: 'LIFESPAN_PCT'; value: number }                                // → GD-26 talentLifespanPct
  | { type: 'CULTIVATION_SPEED_PCT'; value: number }
  | { type: 'MASTERY_SPEED_PCT'; value: number; skillTag?: string }        // vd. skillTag "sword"
  | { type: 'CRAFT_QUALITY_PCT'; craft: 'ALCHEMY' | 'FORGING' | 'TECHNIQUE'; value: number }
  | { type: 'GATHER_YIELD_PCT'; nodeKind: 'HERB' | 'ORE'; value: number }
  | { type: 'ENCOUNTER_WEIGHT_PCT'; minTier: EncounterTier; value: number } // nhân vào w_i ở GD-22 §6.2
  | { type: 'SENSE_LEVEL_FLAT'; value: number }                            // GD-21 §3.7
  | { type: 'SCAN_RADIUS_FLAT'; value: number }                            // mét
  | { type: 'BREAKTHROUGH_CHANCE_FLAT'; value: number }                    // điểm %, GD-04
  | { type: 'TRIBULATION_POWER_PCT'; value: number }                       // GD-04
  | { type: 'PILL_TOXICITY_PCT'; value: number }                           // GD-04
  | { type: 'PRICE_PCT'; scope: 'NPC_SHOP' | 'TELEPORT'; value: number }
  | { type: 'PARTY_LUCK_FLAT'; value: number }                             // áp lên đồng đội
  | { type: 'HOOK'; hookId: HookId };                                      // §5.9

interface EffectCondition {
  soloOnly?: boolean; dayPhases?: DayPhase[]; weather?: Weather[]; hpBelowPct?: number;
}

type HookId = 'PAST_WISDOM' | 'DEMON_SEED' | 'SWORD_PIERCE' | 'CHAOS_ROOT';

interface Background {
  id: string;                   // BG_*
  nameKey: string; descKey: string;
  unlockId: string | null;      // null = mở sẵn; khác null = UNL_BG_* (GD-26 §5.10)
  statOffsets: Partial<InnateStats>;
  rootRules?: { gradeWeightsOverride?: Partial<Record<RootGrade, number>> };
  talentRules?: { guaranteedHidden?: { minRarity: TalentRarity; revealOn: 'TC1' } };
  start: {
    mapId: string; poiId: string;   // điểm xuất hiện: mọi xuất thân = { mapId: "V1_F01", poiId: "V1_F01_LM_001" } (miếu hoang, TLN-R-032)
    items: { itemId: string; qty: number }[];
    spiritStones: number;
    knownRecipes: string[];     // RCP_* (đan phương / khí phổ)
    lifeFlags: string[];        // FLAG_* (dùng cho cơ duyên / nhiệm vụ)
    karma: number;
    questChain?: string;        // QC_*
  };
  encounterTags: string[];
}

interface CharacterTalent {
  characterId: string; talentId: string;
  source: TalentSource; slot: number;          // 1..maxTalents
  revealed: boolean;
  acquiredAt: string; refId: string | null;    // vd. sessionId cơ duyên
}

interface CreationRollResult {
  rollId: string; attemptId: string; index: number;   // index 0 = lần roll đầu
  backgroundId: string;
  spiritRoot: SpiritRoot;
  innateStats: InnateStats;                    // đã cộng statOffsets của xuất thân
  innateLifespanBonusYears: number;
  talentIds: [string, string, string];
  lockedLine: CreationLine | null;
  seed: string; createdAt: string;
}
type CreationLine = 'ROOT' | 'INNATE' | 'TALENT_1' | 'TALENT_2' | 'TALENT_3';

interface CreationAttempt {                    // một lần "chuyển thế" của tài khoản trên server
  attemptId: string; accountId: string; serverId: number; generation: number;
  rollsAllowed: number; rollsUsed: number;     // rollsUsed tính cả lần roll đầu
  lockAvailable: boolean;
  noRarePlusStreak: number;                    // §5.6 bảo hiểm
  confirmedRollId: string | null;
}

interface PendingTalentChoice {                // chờ người chơi quyết định
  pendingId: string; characterId: string;
  kind: 'AWAKEN' | 'REPLACE';
  offers: string[];                            // talentIds đề xuất (AWAKEN: 3; REPLACE: 1)
  context: { source: TalentSource; refId: string | null; realmCode?: string };
  createdAt: string; resolvedAt: string | null;
}
```

---

## 4. Máy trạng thái

### 4.1. `CreationAttempt`
| Từ | Đến | Điều kiện | Tác dụng phụ |
|----|-----|-----------|--------------|
| — | `OPEN` | Tài khoản không có nhân vật `ALIVE`/`ENDING` trên server đã chọn (GD-26 LC-R-006) | Tạo attempt, tính `rollsAllowed` (TLN-R-020); chưa roll |
| `OPEN` | `OPEN` | Roll lần đầu hoặc roll lại, `rollsUsed < rollsAllowed` | Thêm `CreationRollResult` vào lịch sử |
| `OPEN` | `CONFIRMED` | Xác nhận một `rollId` trong lịch sử + tên hợp lệ | Tạo nhân vật (GD-26 §5.1) trong cùng transaction |

Attempt **không hết hạn**: người chơi thoát game giữa chừng, lần sau vào lại đúng lịch sử roll cũ (không hoàn lượt, không cho roll lại từ đầu).

### 4.2. `PendingTalentChoice`
`PENDING` → `RESOLVED` (chọn / bỏ qua). Mỗi nhân vật tối đa 1 lựa chọn `AWAKEN` đang chờ; lựa chọn `REPLACE` xếp hàng.

---

## 5. Quy tắc

### 5.1. Phiên tạo nhân vật
- <a id="tln-r-020"></a>**TLN-R-020** `rollsAllowed = 1 + params.baseRerolls (3) + số UNL_REROLL đã mua (≤ 5)`. `lockAvailable = có UNL_LOCK_LINE`.
- <a id="tln-r-065"></a>**TLN-R-065** Mở attempt chỉ tạo hoặc resume metadata và lịch sử hiện có; không tự roll. Attempt mới có `rollsUsed = 0`, lịch sử rỗng. Roll đầu tiên được gửi tường minh qua endpoint roll với `backgroundId`, được tính là lượt đầu (`idx = 0`). Mở lại attempt trả cùng attempt và lịch sử, không tiêu lượt.
- <a id="tln-r-021"></a>**TLN-R-021** Mỗi lần roll gắn với **một xuất thân** do người chơi chọn ở lần roll đó; đổi xuất thân giữa các lần roll được phép, không tốn thêm lượt. Xác nhận dùng xuất thân của roll được chọn.
- <a id="tln-r-022"></a>**TLN-R-022** Người chơi được **xác nhận bất kỳ roll nào trong lịch sử** của attempt, không chỉ roll mới nhất.
- <a id="tln-r-023"></a>**TLN-R-023** **Khóa dòng:** nếu `lockAvailable`, mỗi lần roll lại được khóa tối đa 1 `CreationLine`, sao chép từ roll **đang hiển thị**. Khóa `INNATE` giữ cả 4 chỉ số bẩm sinh lẫn thọ nguyên gốc. Khóa `ROOT` mà xuất thân mới có `rootRules` khác thì vẫn giữ linh căn đã khóa.
- <a id="tln-r-024"></a>**TLN-R-024** Thứ tự roll trong một lần: **ROOT → INNATE → TALENT_1..3**. Dòng bị khóa bỏ qua bước roll nhưng vẫn tham gia ràng buộc của các bước sau (vd. `requires.rootElementsAny`).
- <a id="tln-r-025"></a>**TLN-R-025** Tên nhân vật: 2–12 ký tự, chữ cái tiếng Việt có dấu và khoảng trắng đơn, qua bộ lọc từ ngữ, duy nhất trên server **trong số nhân vật `ALIVE`/`ENDING`** (tên của nhân vật đã `ENDED` được dùng lại).
- <a id="tln-r-026"></a>**TLN-R-026** Trước khi kiểm tra tên, API chuẩn hóa Unicode NFC; từ chối khoảng trắng đầu/cuối, nhiều khoảng trắng liên tiếp, ký tự ngoài bảng chữ cái tiếng Việt và khoảng trắng ASCII. Đếm độ dài theo Unicode code point sau chuẩn hóa; giới hạn 2–12. Từ cấm lấy từ danh sách versioned phía server; không chấp nhận tạo nhân vật nếu danh sách chưa nạp được.
- <a id="tln-r-027"></a>**TLN-R-027** Ngoại hình nền gồm preset và màu lấy từ appearance gamedata đã nạp. Mỗi preset khai báo các slot màu hợp lệ và màu mặc định; client chỉ gửi các override theo cặp `colorSlotId` → `paletteColorId`. API từ chối preset, slot hoặc màu không có trong bundle hiện hành. Màu không gửi dùng default của preset.

### 5.2. Xuất thân

| ID | Tên | Mở khóa | Chỉ số | Khởi đầu | Đặc biệt |
|----|-----|---------|--------|----------|----------|
| `BG_VILLAGE_YOUTH` | Sơn thôn thiếu niên | Mặc định | — | 100 linh thạch, đồ vải, kiếm gỗ | Cân bằng, dễ chơi |
| `BG_HERB_BOY` | Dược đồng | Mặc định | spiritSense +5, constitution −5 | 50 linh thạch, 2 đan phương (`RCP_HOI_KHI_DAN`, `RCP_CAM_HUYET_DAN`), đan lô sơ cấp | Tag `herbalist` |
| `BG_SMITH_APPRENTICE` | Thợ rèn tiểu tử | Mặc định | constitution +5, comprehension −5 | 50 linh thạch, 1 khí phổ (`RCP_THIET_KIEM`), búa rèn | Tag `smith` |
| `BG_NOBLE` | Thế gia công tử | `UNL_BG_NOBLE` | comprehension +5, luck −10 | 1.000 linh thạch, kiếm phẩm Linh (`requiredRealm` LK3), áo gấm | Cờ `FLAG_FAMILY_ENEMY` (cơ duyên ám sát / tranh đoạt gia tộc) |
| `BG_ORPHAN` | Cô nhi lưu lạc | `UNL_BG_ORPHAN` | luck +15, constitution −5, comprehension −5 | 0 linh thạch, áo rách | karma +10; tag `orphan` |
| `BG_SECT_SERVANT` | Tông môn tạp dịch | `UNL_BG_SECT_SERVANT` | constitution +5, comprehension −5 | Thành viên ngoại môn Thanh Vân Tông, 30 linh thạch | Sau hướng dẫn ở `V1_F01`, đến khu tông môn trong `V1_HUB` (TLN-R-032); chuỗi `QC_SECT_SERVANT` |
| `BG_TAC_THIEN_GANG` | Bang chúng Tạc Thiên | `UNL_BG_TAC_THIEN_GANG` | luck +5, comprehension −5 | Lâu la (`TAP_DICH`) của Tạc Thiên Bang, 30 linh thạch, 1 Truyền Tống Phù sơ cấp | Sau hướng dẫn ở `V1_F01`, đến `V1_HUB` gặp người liên lạc của bang (TLN-R-032); karma −5; tag `outlaw`; chuỗi `QC_TAC_THIEN_GANG` (Beta) |
| `BG_HOP_HOAN_DISCIPLE` | Hợp Hoan đệ tử | `UNL_BG_HOP_HOAN_DISCIPLE` | spiritSense +5, constitution −5 | Tạp dịch (`TAP_DICH`) của Hợp Hoan Tông, 30 linh thạch, quạt gỗ (`FAN`) | Sau hướng dẫn ở `V1_F01`, đến `V1_HUB` gặp sứ giả của tông (TLN-R-032); tag `charm`; chuỗi `QC_HOP_HOAN_DISCIPLE` (Beta) |
| `BG_OUTCAST` | Phế vật bị trục xuất | `UNL_BG_OUTCAST` | — | 0 linh thạch | Linh căn chỉ ra `QUAD`/`PENTA`; **đảm bảo 1 thiên phú ẩn ≥ LEGENDARY**, lộ khi lên `TC1` (TLN-R-062) |

- <a id="tln-r-030"></a>**TLN-R-030** `statOffsets` cộng **sau** khi roll chỉ số (§6.1); kết quả chặn trong `[params.innateCreationMin (5), params.innateCreationMax (95)]`.
- <a id="tln-r-031"></a>**TLN-R-031** Vật phẩm khởi đầu vào `BAG`; đan phương / khí phổ ghi vào `recipe_known`; cờ ghi `character_flag`; tất cả trong transaction tạo nhân vật.
- <a id="tln-r-032"></a>**TLN-R-032** Mọi xuất thân bắt đầu ở `PN1`, **xuất hiện ở miếu hoang `V1_F01`** — `start.mapId = "V1_F01"`, `start.poiId = "V1_F01_LM_001"` (POI `LANDMARK` miếu hoang, GD-16 `V1_F01` LV1F01-R-001) cho **mọi** xuất thân (cảnh mở đầu, GD-18 §3) và đi qua phần hướng dẫn chung `QC_TUTORIAL_QI` (Lão Đạo, bãi tập Trúc Thử, cơ duyên `ENC_V1_FIRST_OMEN` — đều ở `V1_F01`); xuất thân chỉ đổi **điểm đến sau hướng dẫn** (NPC đón ở bước đầu của `start.questChain`, vd. người liên lạc / sứ giả ở `V1_HUB`), hội thoại mở đầu và các cờ.

### 5.3. Linh căn
- <a id="tln-r-040"></a>**TLN-R-040** Roll **số hệ** theo `rootGradeWeights` (xuất thân có thể ghi đè):

| Grade | Số hệ | Tỷ lệ mặc định |
|-------|:-----:|:--------------:|
| `HEAVENLY` (Thiên linh căn) | 1 | 1% |
| `DUAL` (Song linh căn) | 2 | 9% |
| `TRIPLE` (Tam linh căn) | 3 | 45% |
| `QUAD` (Tứ linh căn) | 4 | 30% |
| `PENTA` (Ngũ linh căn) | 5 | 15% |

- <a id="tln-r-041"></a>**TLN-R-041** Chọn các hệ **ngẫu nhiên đều, không trùng** trong 5 hệ cơ bản.
- <a id="tln-r-042"></a>**TLN-R-042** **Biến dị:** độc lập, xác suất `params.mutationChance` (3%) thay **một** hệ ngẫu nhiên bằng `LOI`/`BANG`/`PHONG` (đều nhau); `mutated = true`. Không áp dụng cho `PENTA`.
- <a id="tln-r-043"></a>**TLN-R-043** **Độ tinh khiết:** `purity = clamp(round(N(50, 15)), 10, 90)` khi tạo; trong kiếp có thể lên tới 100 (tẩy tủy).
- <a id="tln-r-044"></a>**TLN-R-044** **Tương hợp công pháp:** công pháp có hệ thuộc linh căn → +10% sát thương kỹ năng hệ đó, thuần thục tốc độ ×1,0; công pháp khác hệ → **vẫn dùng được**, thuần thục ×`params.offElementMasteryMult` (0,7). Không bao giờ khóa công pháp theo linh căn (PLAN §4.3).
- <a id="tln-r-045"></a>**TLN-R-045** `rootMultiplier` (tốc độ tu luyện, dùng ở GD-04 và GD-26 §6.5) theo §6.2.

### 5.4. Chỉ số bẩm sinh và thọ nguyên gốc
- <a id="tln-r-050"></a>**TLN-R-050** Roll 4 chỉ số theo §6.1 (tổng cố định, mỗi chỉ số trong `[20, 80]` trước offset).
- <a id="tln-r-051"></a>**TLN-R-051** `innateLifespanBonusYears` roll phân phối tam giác nguyên trong `[−20, +20]`, đỉnh 0 (GD-26 LC-R-011). Thuộc dòng `INNATE`.
- <a id="tln-r-052"></a>**TLN-R-052** Ý nghĩa chỉ số (công thức chi tiết ở tài liệu được dẫn):

| Chỉ số | Ảnh hưởng chính | Tài liệu |
|--------|-----------------|----------|
| `constitution` (Căn cốt) | Khí huyết, phòng ngự; thời gian `WEAKENED` −0,5% mỗi điểm trên 50 (tối đa −25%); tỷ lệ dung hợp (§6.5) | GD-06, GD-26 |
| `comprehension` (Ngộ tính) | Tốc độ thuần thục công pháp; chất lượng chế công pháp; `ROLL_CHECK` | GD-07, GD-22 |
| `luck` (Khí vận) | `luckFactor` chọn cơ duyên; tỷ lệ rơi đồ hiếm; "bạo" khi chế tác | GD-22 §6.2, GD-09, GD-10 |
| `spiritSense` (Thần thức) | Bán kính & hồi chiêu thần thức quét; thành công luyện đan / khí; +1 cấp thần thức khi ≥ 80 | GD-21 §3.7, §6.4 |

- <a id="tln-r-053"></a>**TLN-R-053** Trong kiếp, chỉ số bẩm sinh thay đổi qua `CHANGE_INNATE_STAT` hoặc thiên phú `STAT_FLAT`/`STAT_PCT`. Thay đổi **cơ bản** (không tính thiên phú) bị chặn ±`params.innateLifeCap` (15) so với giá trị lúc tạo, và giá trị tuyệt đối trong `[1, 150]`. Phần vượt trần bị bỏ và báo cho người chơi.

### 5.5. Thần thức và cấp thần thức
- <a id="tln-r-055"></a>**TLN-R-055** `senseLevel = realmOrder + (spiritSense ≥ params.senseBonusThreshold (80) ? 1 : 0) + Σ SENSE_LEVEL_FLAT`. Dùng để so với `hiddenLevel` (GD-21 §3.7). Ví dụ: Trúc Cơ (2), thần thức 82, có `TAL_SPIRIT_EYES` (+1) → `senseLevel = 4`.

### 5.6. Roll thiên phú lúc tạo nhân vật
- <a id="tln-r-060"></a>**TLN-R-060** Mỗi ô (TALENT_1..3) roll **độ hiếm** theo `creationRarityWeights`: COMMON 55 · RARE 28 · EPIC 12 · LEGENDARY 4,5 · MYTHIC 0,5. Khí vận **không** ảnh hưởng roll tạo nhân vật.
- <a id="tln-r-061"></a>**TLN-R-061** Trong độ hiếm đã roll, chọn theo `poolWeight` từ pool hợp lệ:
  - có `CREATION` trong `sources`, `polarity ≠ NEGATIVE` (TLN-R-004);
  - `unlockId == null` **hoặc** tài khoản đã có unlock đó;
  - thỏa `requires` với linh căn / xuất thân của lần roll này;
  - không trùng thiên phú đã có trong lần roll (kể cả dòng khóa), không trùng `exclusiveGroup`.
  Pool rỗng ở độ hiếm đó thì hạ dần độ hiếm cho tới khi có ứng viên.
- <a id="tln-r-062"></a>**TLN-R-062** **Thiên phú ẩn:** thiên phú `hiddenAtCreation` hiện `??? (độ hiếm)` lúc tạo; lộ khi đạt mốc `revealOn`. Xuất thân `BG_OUTCAST` thay ô TALENT_3 bằng một thiên phú ẩn độ hiếm ≥ `LEGENDARY` (chọn từ pool ẩn / LEGENDARY+), lộ ở `TC1`; ô này không khóa / không roll lại được.
- <a id="tln-r-063"></a>**TLN-R-063** **Bảo hiểm:** nếu `noRarePlusStreak ≥ params.creationPityRolls` (3), lần roll tiếp theo đảm bảo TALENT_1 có độ hiếm ≥ `RARE`. `noRarePlusStreak` reset khi một roll có ít nhất một thiên phú RARE+.
- <a id="tln-r-064"></a>**TLN-R-064** Bảng xác suất (độ hiếm, linh căn, phân phối chỉ số) **công khai** trong màn tạo nhân vật (nút "Thiên cơ", GD-29).

### 5.7. Danh mục thiên phú MVP (34)

Giá trị % là cộng dồn kiểu cộng (additive) với các nguồn khác, xem §6.3.

| ID | Tên | Độ hiếm | Cực | Hiệu ứng | Ghi chú |
|----|-----|---------|-----|----------|---------|
| `TAL_IRON_SKIN` | Thiết Bì | COMMON | + | `defense` +8% | |
| `TAL_QUICK_FEET` | Khinh Thân | COMMON | + | `moveSpeed` +5%, `staminaRegen` +10% | |
| `TAL_HERB_SENSE` | Thảo Mộc Tri | COMMON | + | Sản lượng hái linh thảo +15% | Tag `herbalist` |
| `TAL_ORE_SENSE` | Khoáng Mạch Tri | COMMON | + | Sản lượng khai khoáng +15% | |
| `TAL_STEADY_HAND` | Ổn Thủ | COMMON | + | Chất lượng luyện đan +5%, luyện khí +5% | |
| `TAL_DEEP_BREATH` | Thâm Tức | COMMON | + | `maxMp` +10% | |
| `TAL_THICK_BLOOD` | Hậu Huyết | COMMON | + | `maxHp` +10% | |
| `TAL_EARLY_RISER` | Kê Minh | COMMON | + | Tốc độ tu luyện +15% khi `DAWN` | |
| `TAL_NIGHT_OWL` | Dạ Miêu | COMMON | + | `attack`, `spellPower` +8% khi `NIGHT` | |
| `TAL_THRIFTY` | Cần Kiệm | COMMON | + | Giá cửa hàng NPC −10%, phí truyền tống −10% | |
| `TAL_PILL_HEART` | Đan Tâm | RARE | + | Chất lượng luyện đan +15% | Tag `alchemy` |
| `TAL_FORGE_SOUL` | Khí Hồn | RARE | + | Chất lượng luyện khí +15% | Tag `forging` |
| `TAL_SWORD_AFFINITY` | Kiếm Duyên | RARE | + | Thuần thục kỹ năng tag `sword` +50% | Tag `sword` |
| `TAL_SPIRIT_EYES` | Linh Nhãn | RARE | + | `SENSE_LEVEL_FLAT` +1, bán kính quét +10m | |
| `TAL_POISON_IMMUNE` | Kháng Độc Thể | RARE | + | `poisonResist` +50% | |
| `TAL_FORTUNATE` | Phúc Tinh | RARE | + | `luck` +10 | |
| `TAL_PAST_WISDOM` | Túc Tuệ | RARE | + | `HOOK PAST_WISDOM` | Unlock `UNL_TALENT_POOL_PAST_WISDOM` |
| `TAL_METAL_BODY` | Canh Kim Thể | RARE | + | `elementDamage.KIM` +20%, thuần thục kỹ năng hệ Kim +10% | Cần hệ KIM; nhóm `ELEMENT_BODY` |
| `TAL_WOOD_BODY` | Ất Mộc Thể | RARE | + | Như trên, hệ MOC | Cần MOC; `ELEMENT_BODY` |
| `TAL_WATER_BODY` | Huyền Thủy Thể | RARE | + | Như trên, hệ THUY | Cần THUY; `ELEMENT_BODY` |
| `TAL_FIRE_BODY` | Ly Hỏa Thể | RARE | + | Như trên, hệ HOA | Cần HOA; `ELEMENT_BODY` |
| `TAL_EARTH_BODY` | Hậu Thổ Thể | RARE | + | Như trên, hệ THO | Cần THO; `ELEMENT_BODY` |
| `TAL_LONGEVITY_BODY` | Trường Sinh Thể | EPIC | + | `LIFESPAN_PCT` +0,30 | Nhóm `LIFESPAN` |
| `TAL_SHORT_LIVED_GENIUS` | Đoản Mệnh Kỳ Tài | EPIC | ± | `LIFESPAN_PCT` −0,30; `comprehension` +40% | Nhóm `LIFESPAN` |
| `TAL_BATTLE_MANIAC` | Chiến Cuồng | EPIC | ± | `attack`, `spellPower` +15%; `defense` −10% | |
| `TAL_HEAVEN_SENSE` | Thiên Cơ Cảm | EPIC | + | `ENCOUNTER_WEIGHT_PCT` HUYEN+ +25% | |
| `TAL_IMMOVABLE` | Bất Động Như Sơn | EPIC | + | `poiseDamageTaken` −30%, `defense` +5% | |
| `TAL_DEMON_SEED` | Ma Chủng | EPIC | ± | `HOOK DEMON_SEED`; `attack` +10% khi `NIGHT` | `hiddenAtCreation`, lộ bằng cờ `FLAG_DEMON_AWAKENED`; tag `demonic` |
| `TAL_SWORD_HEART` | Kiếm Tâm Thông Minh | LEGENDARY | + | Thuần thục `sword` +100%; `HOOK SWORD_PIERCE` | Tag `sword` |
| `TAL_LONE_STAR` | Thiên Sát Cô Tinh | LEGENDARY | ± | `attack`, `spellPower` +30% khi `soloOnly`; `PARTY_LUCK_FLAT` −10 | |
| `TAL_TEN_THOUSAND_PILLS` | Vạn Đan Thể | LEGENDARY | + | `PILL_TOXICITY_PCT` −50% | |
| `TAL_GREAT_FORTUNE` | Đại Khí Vận | LEGENDARY | + | `luck` +20 | |
| `TAL_HEAVEN_FAVORED` | Thiên Đạo Sủng Nhi | MYTHIC | ± | `luck` +40; `TRIBULATION_POWER_PCT` +100% | Unlock bằng mốc 100% Vực 1 (GD-21 §3.5) |
| `TAL_CHAOS_BODY` | Hỗn Độn Thể | MYTHIC | ± | `HOOK CHAOS_ROOT`; tốc độ tu luyện +30%; `TRIBULATION_POWER_PCT` +50% | Unlock `UNL_TALENT_POOL_CHAOS_BODY` (cần `max(generationCount across servers) ≥ 3`, LC-R-164) |

**Thiên phú tiêu cực** (`polarity = NEGATIVE`, chỉ đến từ sự kiện, §5.10): `TAL_SHATTERED_MERIDIAN` (Kinh Mạch Tổn Thương: tốc độ tu luyện −20%), `TAL_HAUNTED` (Oán Linh Bám Thân: `maxMp` −15%, tâm cảnh tối đa −10).

### 5.8. Thay đổi thiên phú trong kiếp

Mọi thay đổi đi qua `POST /internal/v1/talents/change` (TLN-R-006). Các thao tác:

| Thao tác (`op`) | Nguồn gọi | Quy tắc |
|-----------------|-----------|---------|
| `ADD_TALENT` | Cơ duyên (GD-22), thức tỉnh, dung hợp | TLN-R-070..072 |
| `REMOVE_TALENT` | Cơ duyên, GM | TLN-R-073 |
| `MODIFY_ROOT` | Cơ duyên, vật phẩm tẩy tủy | TLN-R-074 |
| `CHANGE_INNATE_STAT` | Cơ duyên, đan dược | TLN-R-053 |
| `REVEAL_TALENT` | Đạt `revealOn`, set cờ | TLN-R-062 |

- <a id="tln-r-070"></a>**TLN-R-070** Số thiên phú tối đa `params.maxTalents` (6) = 3 lúc tạo + tối đa 3 lần thức tỉnh / sự kiện. Thiên phú tiêu cực **không tính** vào giới hạn.
- <a id="tln-r-071"></a>**TLN-R-071** Thêm thiên phú khi đã đủ 6, hoặc trùng `exclusiveGroup`, thì tạo `PendingTalentChoice(kind = REPLACE)`: người chơi chọn thiên phú bị thay (trong nhóm bị trùng nếu là xung đột nhóm) hoặc **từ chối** (thiên phú mới bị bỏ). API trả `PENDING_CHOICE`.
- <a id="tln-r-072"></a>**TLN-R-072** Thêm thiên phú đã có thì trả `REJECTED(DUPLICATE)`, không lỗi nội dung; nguồn gọi (vd. GD-22) có thể dùng outcome dự phòng.
- <a id="tln-r-073"></a>**TLN-R-073** Bỏ thiên phú ảnh hưởng thọ nguyên thì GD-26 tính lại `lifespanMaxYears`; nếu ≤ tuổi thì áp LC-R-022.
- <a id="tln-r-074"></a>**TLN-R-074** `MODIFY_ROOT`:
  - `PURIFY(amount)`: `purity = min(100, purity + amount)`.
  - `REMOVE_ELEMENT`: người chơi chọn một hệ để loại (tạo `PendingTalentChoice` dạng chọn hệ); grade tăng một bậc (vd. QUAD → TRIPLE). Không áp dụng khi chỉ còn 1 hệ.
  - `MUTATE(element ∈ LOI/BANG/PHONG)`: người chơi chọn hệ cơ bản bị thay; `mutated = true`. Không áp dụng nếu đã có hệ đó.
  - Thiên phú có `requires.rootElementsAny` không còn thỏa sau thay đổi thì **giữ nguyên nhưng vô hiệu** (hiện mờ) tới khi thỏa lại.

### 5.9. Hook (danh sách trắng)

| `HookId` | Thiên phú | Hệ thống hiện thực | Hành vi |
|----------|-----------|--------------------|---------|
| `PAST_WISDOM` | Túc Tuệ | GD-22 §5.9, GD-21 §3.6 | Ưu tiên nhận động phủ truyền thừa của chính tài khoản; gợi ý cơ duyên trong Vạn Giới Lục hiện rõ hơn một mức |
| `DEMON_SEED` | Ma Chủng | GD-22 | Thêm tag `demonic` vào điều kiện cơ duyên; mở nhánh lựa chọn ma đạo; lộ khi có cờ `FLAG_DEMON_AWAKENED` |
| `SWORD_PIERCE` | Kiếm Tâm Thông Minh | GD-07 | Kỹ năng `sword` dạng phóng (projectile) xuyên qua mục tiêu đầu tiên (tối đa 2 mục tiêu) |
| `CHAOS_ROOT` | Hỗn Độn Thể | §5.3 | Linh căn được tính là tương hợp với **mọi** hệ (TLN-R-044); `rootMultiplier` dùng grade `TRIPLE` nếu grade thực thấp hơn |

Thêm hook mới phải sửa bảng này trước (docs/README §3).

### 5.10. Thức tỉnh, dung hợp, tâm ma
- <a id="tln-r-080"></a>**TLN-R-080** **Thức tỉnh:** đột phá thành công lên `TC1`, `KD1`, `NA1` → API roll 3 thiên phú (nguồn `AWAKEN`, không trùng, tuân thủ ràng buộc TLN-R-061) theo `awakeningRarityWeights[realm]`, tạo `PendingTalentChoice(kind = AWAKEN)`. Người chơi chọn 1 hoặc **bỏ qua** (bỏ qua: `heartState` +5).

| Mốc | COMMON | RARE | EPIC | LEGENDARY | MYTHIC |
|-----|:------:|:----:|:----:|:---------:|:------:|
| `TC1` | 40 | 40 | 17 | 3 | 0 |
| `KD1` | 0 | 45 | 40 | 13 | 2 |
| `NA1` | 0 | 0 | 50 | 42 | 8 |

- <a id="tln-r-081"></a>**TLN-R-081** Lựa chọn `AWAKEN` không hết hạn, nhưng **phải giải quyết trước lần đột phá lớn tiếp theo** (GD-04 chặn đột phá khi còn lựa chọn chờ).
- <a id="tln-r-082"></a>**TLN-R-082** Thiên phú ẩn của `BG_OUTCAST` lộ ở `TC1` **cùng lúc** với thức tỉnh `TC1` (không thay thế lượt thức tỉnh).
- <a id="tln-r-083"></a>**TLN-R-083** **Dung hợp** (dị hỏa, huyết mạch yêu thú): vật phẩm có `fusion = { talentId, baseChance, failure: { lifespanYears, statusId, statusSec } }`. Tỷ lệ thành công theo §6.5, **luôn hiển thị trước khi dùng**. Thành công → `ADD_TALENT` (nguồn `FUSION`); thất bại → tiêu thọ qua GD-26 (`reasonCode = TALENT_FUSION`) và nhận status. Vật phẩm bị tiêu cả khi thất bại.
- <a id="tln-r-084"></a>**TLN-R-084** **Tâm ma** (status `HEART_DEMON`, tối đa 3 tầng). Mỗi tầng: tỷ lệ đột phá −5 điểm %, `heartState` tối đa −10. Nguồn: GD-26 LC-R-062, thất bại đột phá (GD-04), outcome `ADD_STATUS`. Gỡ: `ITM_THANH_TAM_DAN` gỡ 1 tầng; chuỗi nhiệm vụ `QC_QUIET_HEART` gỡ toàn bộ. Đủ 3 tầng: lần đột phá lớn tiếp theo bắt buộc qua thử thách tâm ma. **Định nghĩa chuẩn ở [GD-04 §5.9](10-cultivation.md) (CU-R-090..096)**; khi mâu thuẫn, GD-04 thắng.
- <a id="tln-r-085"></a>**TLN-R-085** Thiên phú tiêu cực (`NEGATIVE`) chỉ được thêm bởi outcome cơ duyên phẩm bất kỳ hoặc hậu quả thất bại dung hợp; gỡ bằng outcome `REMOVE_TALENT` hoặc vật phẩm chuyên dụng.

### 5.11. Thiên Phú Lục (mục Vạn Giới Lục)
- <a id="tln-r-090"></a>**TLN-R-090** Mỗi thiên phú từng sở hữu (kể cả ẩn đã lộ) ghi vào `account_talent_seen`; hiển thị trong Vạn Giới Lục mục "Thiên Phú" (GD-21 §3.6). Thiên phú chưa từng có hiện bóng mờ + độ hiếm.
- <a id="tln-r-091"></a>**TLN-R-091** Sưu tầm đủ nhóm (vd. 5 thiên phú `ELEMENT_BODY`) thưởng điểm Luân Hồi / danh hiệu (một lần mỗi tài khoản).

---

## 6. Công thức

### 6.1. Roll chỉ số bẩm sinh (tổng cố định)
```
total = innateStatTotal (200); floor = 20; cap = 80; keys = [constitution, comprehension, luck, spiritSense]
pool = total − 4 × floor                       // 120
w_k  = −ln(u_k), u_k ~ U(0,1) từ PRNG(seed)    // tương đương Dirichlet(1,1,1,1)
raw_k = floor + pool × w_k / Σw
làm tròn: lấy phần nguyên, phân phần dư theo phần thập phân lớn nhất (largest remainder)
vượt cap: lấy phần vượt chia đều cho các chỉ số chưa chạm cap, lặp tới khi hết
cộng statOffsets của xuất thân, chặn [5, 95] (TLN-R-030)
```
Ví dụ: `w = [0,5; 1,2; 0,3; 1,0]`, Σ = 3,0 → raw = `[40; 68; 32; 60]`, tổng 200. Xuất thân Cô nhi (luck +15, constitution −5, comprehension −5) → `[35; 63; 47; 60]`.

### 6.2. Hệ số tu luyện theo linh căn
```
rootMultiplier = gradeBase[grade] × (0,75 + purity / 200) + (mutated ? 0,1 : 0)
```
`gradeBase`: HEAVENLY 2,0 · DUAL 1,5 · TRIPLE 1,0 · QUAD 0,8 · PENTA 0,65.

Ví dụ: Tam linh căn, tinh khiết 50 → `1,0 × 1,0 = 1,0`. Thiên linh căn biến dị, tinh khiết 80 → `2,0 × 1,15 + 0,1 = 2,4`. Ngũ linh căn tinh khiết 30 → `0,65 × 0,9 = 0,585`.

Tốc độ tu luyện cuối (GD-04): `× rootMultiplier × (1 + Σ CULTIVATION_SPEED_PCT)`.

### 6.3. Gộp hiệu ứng thiên phú vào chỉ số
```
final = (base + Σ STAT_FLAT) × (1 + Σ STAT_PCT)
```
- Các `STAT_PCT` **cộng** với nhau (không nhân chồng). Hiệu ứng có `when` chỉ tính khi điều kiện thỏa (Game Server đánh giá theo thời gian thực).
- Hiệu ứng của thiên phú bị vô hiệu (TLN-R-074) không tính.
- Công thức gộp chuẩn cho mọi nguồn (trang bị, tâm pháp, status...) là [GD-06 §6.1 / SF-R-040](12-stats-formulas.md); công thức trên là trường hợp riêng của nó.

Ví dụ: `luck` cơ bản 47, có `TAL_FORTUNATE` (+10) và `TAL_GREAT_FORTUNE` (+20) → 77.

### 6.4. Bán kính và cấp thần thức (bổ sung GD-21 §4)
```
scanRadius = radiusByRealm[realm] + spiritSense × 0,2 + Σ SCAN_RADIUS_FLAT
senseLevel = TLN-R-055
```

### 6.5. Tỷ lệ dung hợp
```
p = clamp(baseChance + (constitution − 50) × 0,005 + (heartState − 50) × 0,002, 0,05, 0,95)
```
Ví dụ: `baseChance = 0,5`, căn cốt 70, tâm cảnh 60 → `0,5 + 0,10 + 0,02 = 0,62`.

### 6.6. Xác suất tham khảo (để kiểm tra mô phỏng)
- Ít nhất 1 thiên phú LEGENDARY+ trong một lần roll 3 ô: `1 − (1 − 0,05)^3 ≈ 14,3%`.
- Thiên linh căn biến dị: `1% × 3% = 0,03%`.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/talents/params.json` | Tham số (bên dưới) |
| `shared/gamedata/talents/talents.json` | Mảng `TalentDef` (§5.7) |
| `shared/gamedata/talents/backgrounds.json` | Mảng `Background` (§5.2) |
| `shared/gamedata/talents/root.json` | `rootGradeWeights`, `gradeBase`, biến dị, tinh khiết |
| `shared/gamedata/talents/awakening.json` | `awakeningRarityWeights` |
| `shared/gamedata/items/*.json` | Trường `fusion` của vật phẩm dung hợp |

```jsonc
// params.json
{
  "baseRerolls": 3,
  "innateStatTotal": 200, "innateStatFloor": 20, "innateStatCap": 80,
  "innateCreationMin": 5, "innateCreationMax": 95,
  "innateLifeCap": 15, "innateAbsMin": 1, "innateAbsMax": 150,
  "innateLifespanMin": -20, "innateLifespanMax": 20,
  "creationRarityWeights": { "COMMON": 55, "RARE": 28, "EPIC": 12, "LEGENDARY": 4.5, "MYTHIC": 0.5 },
  "creationPityRolls": 3,
  "maxTalents": 6,
  "mutationChance": 0.03, "purityMean": 50, "puritySd": 15, "purityCreationMin": 10, "purityCreationMax": 90,
  "offElementMasteryMult": 0.7, "onElementDamageBonus": 0.10,
  "senseBonusThreshold": 80,
  "awakenSkipHeartState": 5,
  "heartDemon": { "maxStacks": 3, "breakthroughPenaltyPerStack": 5, "heartStateCapPenaltyPerStack": 10 },
  "nameMinLength": 2, "nameMaxLength": 12
}
```

```jsonc
// talents.json (trích)
[
  { "id": "TAL_LONGEVITY_BODY", "nameKey": "tal.longevity_body.name", "descKey": "tal.longevity_body.desc",
    "rarity": "EPIC", "polarity": "POSITIVE", "sources": ["CREATION", "AWAKEN", "EVENT"],
    "poolWeight": 100, "exclusiveGroup": "LIFESPAN", "unlockId": null, "tags": ["longevity"],
    "effects": [ { "type": "LIFESPAN_PCT", "value": 0.30 } ] },
  { "id": "TAL_FIRE_BODY", "nameKey": "tal.fire_body.name", "descKey": "tal.fire_body.desc",
    "rarity": "RARE", "polarity": "POSITIVE", "sources": ["CREATION", "AWAKEN", "EVENT"],
    "poolWeight": 100, "exclusiveGroup": "ELEMENT_BODY", "requires": { "rootElementsAny": ["HOA"] },
    "unlockId": null, "tags": ["fire"],
    "effects": [ { "type": "STAT_PCT", "stat": "elementDamage.HOA", "value": 0.20 },
                 { "type": "MASTERY_SPEED_PCT", "value": 0.10, "skillTag": "element_hoa" } ] },
  { "id": "TAL_LONE_STAR", "nameKey": "tal.lone_star.name", "descKey": "tal.lone_star.desc",
    "rarity": "LEGENDARY", "polarity": "DUAL", "sources": ["CREATION", "AWAKEN"],
    "poolWeight": 100, "unlockId": null, "tags": ["solo"],
    "effects": [ { "type": "STAT_PCT", "stat": "attack", "value": 0.30, "when": { "soloOnly": true } },
                 { "type": "STAT_PCT", "stat": "spellPower", "value": 0.30, "when": { "soloOnly": true } },
                 { "type": "PARTY_LUCK_FLAT", "value": -10 } ] }
]
```

**Validator (CI):** ID đúng mẫu `TAL_*` / `BG_*` và duy nhất; `effects[].type` thuộc union §3; `hookId` thuộc §5.9; `unlockId` tồn tại trong `rebirth_unlocks.json` (GD-26) hoặc danh sách unlock tài khoản; mỗi độ hiếm có ≥ 1 thiên phú nguồn `CREATION`; không thiên phú `NEGATIVE` nào có `CREATION` trong `sources`; mọi `nameKey`/`descKey` có bản `vi`.

---

## 8. (Dành chỗ) Cân bằng sức mạnh
Bảng "điểm sức mạnh" cho từng thiên phú (ΔCP% theo [GD-06 §6.8](12-stats-formulas.md)) sẽ bổ sung ở v0.2. Trong lúc chờ, nguyên tắc: thiên phú cùng độ hiếm có giá trị **tương đương ±20%** theo đánh giá của designer; thiên phú `DUAL` được phép mạnh hơn một bậc ở mặt lợi.

---

## 9. Hợp đồng giao tiếp

### 9.1. API nội bộ (Game Server / dịch vụ khác → API Service)

| Endpoint | Request | Response | Quy tắc |
|----------|---------|----------|---------|
| `POST /internal/v1/talents/change` | `{ changeId, characterId, op: 'ADD_TALENT' \| 'REMOVE_TALENT' \| 'MODIFY_ROOT' \| 'CHANGE_INNATE_STAT' \| 'REVEAL_TALENT', params, source: TalentSource, refId }` | `{ result: 'APPLIED' \| 'PENDING_CHOICE' \| 'REJECTED', pendingId?, reason?, modifiersVersion }` | TLN-R-006, §5.8; idempotent theo `changeId` |
| `GET /internal/v1/characters/{id}/modifiers` | — | `{ version, innateStats, spiritRoot, rootMultiplier, senseLevel, talents: [{ talentId, active }], aggregated: { flat, pct, conditional[] , hooks[] } }` | Game Server tải khi vào bản đồ và khi nhận `talent.changed` |
| `POST /internal/v1/talents/awakening` | `{ characterId, realmCode, eventId }` | `{ pendingId }` | TLN-R-080; GD-04 gọi sau đột phá |
| `POST /internal/v1/talents/fusion` | `{ characterId, itemInstanceId, attemptId }` | `{ success, p, talentId?, failure? }` | TLN-R-083; idempotent theo `attemptId` |

`params` theo `op`:
- `ADD_TALENT`: `{ talentId }` · `REMOVE_TALENT`: `{ talentId }` · `REVEAL_TALENT`: `{ talentId }`
- `MODIFY_ROOT`: `{ operation: 'PURIFY' | 'REMOVE_ELEMENT' | 'MUTATE', amount?, element? }`
- `CHANGE_INNATE_STAT`: `{ stat: InnateStatKey, delta }`

### 9.2. API công khai (Client → API Service)

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/servers` | Danh mục server có thể chọn | Trạng thái server theo TD-03 |
| `POST /v1/creation/attempts` | Mở hoặc lấy lại attempt hiện tại (kèm lịch sử roll) | §4.1 |
| `POST /v1/creation/attempts/{id}/rolls` | `{ backgroundId, lockLine?: CreationLine }` → `CreationRollResult` | TLN-R-020..024, 060..063 |
| `POST /v1/creation/attempts/{id}/confirm` | `{ rollId, name, appearancePresetId, colors }` → nhân vật | TLN-R-022, 025; GD-26 §5.1 |
| `GET /v1/creation/odds` | Bảng xác suất công khai | TLN-R-064 |
| `GET /v1/backgrounds` · `GET /v1/talents` | Danh mục + trạng thái mở khóa của tài khoản | |
| `GET /v1/characters/{id}/talents` | Thiên phú, linh căn, chỉ số bẩm sinh, lựa chọn đang chờ | |
| `POST /v1/characters/{id}/talent-choices/{pendingId}` | `{ pick: talentId \| null, replaceTalentId?: string, element?: Element }` | TLN-R-071, 074, 080 |

`POST /v1/creation/attempts` nhận `{ serverId }` và tạo/resume attempt chưa roll (TLN-R-065). Client chọn xuất thân khi gọi endpoint roll lần đầu. `GET /v1/servers` trả server `OPEN` và `MAINTENANCE`; `OPEN` có thể chọn, `MAINTENANCE` chỉ hiển thị và không thể mở attempt, `CLOSED` không xuất hiện. Attempt lưu `serverId`; roll và confirm dùng server đã lưu, không nhận lại server từ client.

### 9.3. Message thời gian thực

| Message | Hướng | Payload | Khi nào |
|---------|-------|---------|---------|
| `S2C_TalentChoicePending` | S→C | `{ pendingId, kind, offers: TalentDef[] }` | Có lựa chọn mới |
| `S2C_TalentChanged` | S→C | `{ op, talentId?, spiritRoot?, innateStats? }` | Sau mỗi thay đổi áp dụng |
| `S2C_TalentRevealed` | S→C | `{ talentId }` | Thiên phú ẩn lộ (hiệu ứng lớn) |

### 9.4. Sự kiện domain (topic `talent.*`)

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `talent.creation_confirmed` | `{ accountId, characterId, backgroundId, talentIds, rootGrade, rollsUsed }` | Telemetry, GD-26 (`life_started`) |
| `talent.changed` | `{ characterId, op, talentId?, source, refId, modifiersVersion }` | Game Server (tải lại modifiers), Bia Vạn Cổ (`notableEvents`), GD-26 (tính lại thọ) |
| `talent.revealed` | `{ characterId, talentId }` | Thông báo, Bia Vạn Cổ |

---

## 10. Lưu trữ

```sql
CREATE TABLE creation_attempt (
  attempt_id      UUID PRIMARY KEY,
  account_id      UUID NOT NULL,
  server_id       SMALLINT NOT NULL REFERENCES server_registry(server_id),
  generation      INT  NOT NULL,
  rolls_allowed   INT  NOT NULL,
  rolls_used      INT  NOT NULL DEFAULT 0,
  lock_available  BOOLEAN NOT NULL,
  no_rare_plus_streak INT NOT NULL DEFAULT 0,
  confirmed_roll_id UUID,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_attempt_open ON creation_attempt(account_id, server_id) WHERE confirmed_roll_id IS NULL;

CREATE TABLE creation_roll (
  roll_id     UUID PRIMARY KEY,
  attempt_id  UUID NOT NULL REFERENCES creation_attempt(attempt_id),
  idx         INT  NOT NULL,
  background_id VARCHAR(32) NOT NULL,
  locked_line VARCHAR(16),
  seed        VARCHAR(64) NOT NULL,
  result      JSONB NOT NULL,                 -- CreationRollResult
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (attempt_id, idx)
);

-- Bổ sung cột cho character
ALTER TABLE character ADD COLUMN background_id          VARCHAR(32) NOT NULL;
ALTER TABLE character ADD COLUMN spirit_root            JSONB NOT NULL;   -- SpiritRoot
ALTER TABLE character ADD COLUMN innate_stats           JSONB NOT NULL;   -- giá trị cơ bản hiện tại
ALTER TABLE character ADD COLUMN innate_stats_creation  JSONB NOT NULL;   -- để áp trần TLN-R-053
ALTER TABLE character ADD COLUMN modifiers_version      INT NOT NULL DEFAULT 1;

CREATE TABLE character_talent (
  character_id UUID NOT NULL REFERENCES character(id),
  talent_id    VARCHAR(48) NOT NULL,
  source       VARCHAR(16) NOT NULL,
  slot         INT NOT NULL,
  revealed     BOOLEAN NOT NULL DEFAULT TRUE,
  ref_id       VARCHAR(64),
  acquired_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, talent_id)
);

CREATE TABLE talent_pending_choice (
  pending_id   UUID PRIMARY KEY,
  character_id UUID NOT NULL REFERENCES character(id),
  kind         VARCHAR(16) NOT NULL,          -- AWAKEN | REPLACE | ROOT_ELEMENT
  offers       JSONB NOT NULL,
  context      JSONB NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  resolved_at  TIMESTAMPTZ,
  resolution   JSONB
);
CREATE UNIQUE INDEX ux_one_awaken_pending ON talent_pending_choice(character_id)
  WHERE kind = 'AWAKEN' AND resolved_at IS NULL;

CREATE TABLE talent_change_log (
  change_id    VARCHAR(80) PRIMARY KEY,       -- idempotency
  character_id UUID NOT NULL,
  op           VARCHAR(24) NOT NULL,
  params       JSONB NOT NULL,
  source       VARCHAR(16) NOT NULL,
  ref_id       VARCHAR(64),
  result       JSONB NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE account_talent_seen (
  account_id UUID NOT NULL, talent_id VARCHAR(48) NOT NULL, first_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, talent_id)
);
```

`rng_audit.kind` mới: `CREATION_ROOT`, `CREATION_INNATE`, `CREATION_TALENT`, `AWAKEN_OFFER`, `FUSION`.

---

## 11. UI/UX

- **Màn tạo nhân vật** (bố cục 3 cột):
  - Trái: thẻ xuất thân (thẻ khóa hiện điều kiện mở bằng Luân Hồi).
  - Giữa: nhân vật preset xoay được; quanh người có linh quang theo màu các hệ linh căn (biến dị có tia sét / băng / gió).
  - Phải: 5 dòng kết quả, mỗi dòng có biểu tượng ổ khóa (nếu có `UNL_LOCK_LINE`); thanh "Còn N lượt"; nút **Nghịch Thiên Cải Mệnh** (roll lại).
  - Dưới: dải lịch sử các lần roll (thẻ nhỏ), bấm để xem lại / chọn.
- **Hiệu ứng lộ kết quả:** các dòng lật lần lượt; thiên phú EPIC+ có ánh sáng màu theo độ hiếm; MYTHIC có hiệu ứng toàn màn hình. Không có bất kỳ nút mua / nạp nào trên màn này (TLN-R-002).
- **Nút "Thiên cơ":** mở bảng xác suất (TLN-R-064).
- **Thức tỉnh:** sau cutscene đột phá, 3 thẻ thiên phú úp → lật; nút "Thuận theo tự nhiên" để bỏ qua.
- **Bảng nhân vật → Căn cơ:** linh căn (hệ, tinh khiết, `rootMultiplier` dạng "Tốc độ tu luyện ×1,15"), 4 chỉ số bẩm sinh kèm giải thích, danh sách thiên phú (thiên phú vô hiệu hiện mờ kèm lý do).

---

## 12. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Gửi request roll khi `rollsUsed == rollsAllowed` | `409 NO_ROLLS_LEFT` |
| Khóa dòng khi không có `UNL_LOCK_LINE` | `403 LOCK_NOT_AVAILABLE` |
| Mua `UNL_REROLL` giữa attempt | `rollsAllowed` của attempt đang mở tăng ngay (GD-26 §12) |
| Hai tab cùng gửi roll | Khóa dòng `creation_attempt`; request thứ hai nhận kết quả theo `idx` tiếp theo hoặc `409` nếu hết lượt |
| Tên trùng nhân vật đang sống | `409 NAME_TAKEN`, attempt giữ nguyên |
| Pool ở độ hiếm đã roll rỗng (do ràng buộc) | Hạ độ hiếm (TLN-R-061); ghi telemetry `pool_fallback` |
| Thức tỉnh khi 3 thiên phú `AWAKEN` khả dụng < 3 | Đề xuất ít hơn 3; nếu 0 thì cho `heartState` +5 và ghi log |
| `REMOVE_ELEMENT` khi người chơi không phản hồi | Lựa chọn chờ không hết hạn; tẩy tủy chỉ áp dụng khi người chơi chọn |
| Dung hợp khi thiên phú đích đã có | Chặn trước khi dùng vật phẩm (UI báo, API `409 ALREADY_HAS_TALENT`) |
| Thiên phú bị xóa khỏi gamedata khi nhân vật đang có | Giữ bản ghi, hiệu ứng = rỗng, hiển thị "Thiên phú đã thất truyền"; validator cảnh báo |

---

## 13. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `creation_roll` | idx, backgroundId, rootGrade, rarities[], locked | Phân bố thực tế khớp lý thuyết? |
| `creation_confirm` | rollsUsed, chosenIdx, backgroundId, talentIds | Người chơi có hay chọn lại roll cũ? Dùng hết lượt không? |
| `creation_abandon` | rollsUsed, thời gian ở màn tạo | Màn tạo nhân vật có gây nản không |
| `talent_change` | op, talentId, source | Nguồn thay đổi thiên phú phổ biến |
| `awakening_choice` | realmCode, offers, pick | Thiên phú nào được ưa chuộng / bị bỏ qua |
| `fusion_attempt` | itemId, p, success | Tỷ lệ thực tế, mức chấp nhận rủi ro |
| `talent_vs_outcome` | talentIds, ageYears khi kết thúc, finalRealm | Thiên phú nào ảnh hưởng mạnh tới độ dài / thành tựu kiếp (cân bằng) |

---

## 14. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="tln-ac-001"></a>TLN-AC-001 | Tài khoản mới, không mua unlock | Mở attempt | `rollsAllowed = 4`, `rollsUsed = 0`, lịch sử rỗng, `lockAvailable = false` |
| TLN-AC-002 | Đã mua 2 `UNL_REROLL` | Mở attempt | `rollsAllowed = 6` |
| TLN-AC-003 | 100.000 roll chỉ số với seed ngẫu nhiên | Kiểm tra | Mọi roll có tổng = 200 trước offset; mọi chỉ số trong [20, 80] |
| TLN-AC-004 | Ví dụ §6.1 | Tính | `[35; 63; 47; 60]` |
| TLN-AC-005 | 100.000 roll linh căn | Kiểm tra | Tỷ lệ grade lệch ≤ 0,5 điểm % so với bảng TLN-R-040; không có hệ trùng; `PENTA` không biến dị |
| TLN-AC-006 | Ví dụ §6.2 | Tính `rootMultiplier` | 1,0 · 2,4 · 0,585 |
| TLN-AC-007 | Linh căn không có HOA | Roll thiên phú | Không bao giờ ra `TAL_FIRE_BODY` |
| TLN-AC-008 | Một roll | Kiểm tra | Không có 2 thiên phú cùng `exclusiveGroup`; không có thiên phú `NEGATIVE` |
| TLN-AC-009 | Tài khoản chưa mở `UNL_TALENT_POOL_CHAOS_BODY` | 100.000 roll | Không bao giờ ra `TAL_CHAOS_BODY` |
| TLN-AC-010 | Có `UNL_LOCK_LINE`, khóa `TALENT_2` | Roll lại | `TALENT_2` giữ nguyên; các ô khác không trùng nó |
| TLN-AC-011 | 3 roll liên tiếp không có RARE+ | Roll thứ 4 | `TALENT_1` có độ hiếm ≥ RARE (TLN-R-063) |
| TLN-AC-012 | Attempt có 4 roll | Xác nhận roll `idx = 1` | Nhân vật tạo theo đúng kết quả roll 1, kể cả xuất thân của roll 1 |
| TLN-AC-013 | Xuất thân `BG_OUTCAST` | Roll | Linh căn chỉ `QUAD`/`PENTA`; ô 3 là `??? (LEGENDARY+)`, không khóa được |
| TLN-AC-014 | Nhân vật `BG_OUTCAST` đột phá lên `TC1` | Sau đột phá | Thiên phú ẩn lộ (`S2C_TalentRevealed`) **và** có `PendingTalentChoice(AWAKEN)` |
| TLN-AC-015 | Nhân vật đủ 6 thiên phú | Cơ duyên gọi `ADD_TALENT` | Trả `PENDING_CHOICE`; người chơi từ chối → không đổi gì; chọn thay → thiên phú cũ bị gỡ, thiên phú mới thêm |
| TLN-AC-016 | Có `TAL_SHORT_LIVED_GENIUS`, cơ duyên thêm `TAL_LONGEVITY_BODY` | `ADD_TALENT` | `PENDING_CHOICE` (xung đột nhóm `LIFESPAN`) |
| TLN-AC-017 | Gỡ `TAL_LONGEVITY_BODY` khi tuổi > thọ mới | `REMOVE_TALENT` | GD-26 kích hoạt `LAST_LIGHT` (LC-R-022) |
| TLN-AC-018 | Tứ linh căn (KIM, MOC, THUY, THO) | `MODIFY_ROOT REMOVE_ELEMENT`, chọn THO | Tam linh căn (KIM, MOC, THUY); `rootMultiplier` tính theo TRIPLE |
| TLN-AC-019 | Có `TAL_FIRE_BODY`, linh căn bị `REMOVE_ELEMENT` mất HOA | Sau thay đổi | `TAL_FIRE_BODY` vẫn còn nhưng `active = false` trong modifiers |
| TLN-AC-020 | Ngộ tính lúc tạo 40, đã +15 | `CHANGE_INNATE_STAT +3` | Không đổi (vượt trần TLN-R-053), báo người chơi |
| TLN-AC-021 | Ví dụ §6.5 | Tính tỷ lệ dung hợp | 0,62 |
| TLN-AC-022 | Gửi cùng `changeId` hai lần | API xử lý | Áp dụng một lần |
| TLN-AC-023 | Ví dụ TLN-R-055 | Tính `senseLevel` | 4 |
| TLN-AC-024 | Còn `PendingTalentChoice(AWAKEN)` chưa giải quyết | Thử đột phá lớn tiếp theo | Bị chặn với lý do rõ ràng (TLN-R-081) |
| TLN-AC-025 | Attempt mới, chưa có roll | Gửi roll đầu với `backgroundId` hợp lệ | Tạo roll `idx = 0`, `rollsUsed = 1`; mở lại attempt không tạo thêm roll |

---

## 15. Phạm vi MVP vs sau này

| Hạng mục | Phase 1 (thô) | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Roll linh căn + chỉ số + 3 thiên phú, roll lại, lịch sử | ✅ | | | |
| 10–15 thiên phú, 2–3 xuất thân mặc định | ✅ | | | |
| Đủ 34 thiên phú, 7 xuất thân, khóa dòng, bảo hiểm, bảng xác suất | | ✅ | | |
| Thức tỉnh `TC1` | | ✅ | | |
| Thức tỉnh `KD1` / `NA1` | | | ✅ | ✅ |
| `ADD/REMOVE_TALENT`, `MODIFY_ROOT`, `CHANGE_INNATE_STAT` qua cơ duyên | | ✅ | | |
| Tâm ma | | ✅ | | |
| Dung hợp dị hỏa / huyết mạch | | | ✅ | |
| Thiên phú ẩn `TAL_DEMON_SEED` + nhánh ma đạo | | | ✅ (phần thiên phú) | ✅ (đọa ma, chính / ma đạo) |
| Thiên Phú Lục trong Vạn Giới Lục | | ✅ | | |
| Bảng điểm sức mạnh thiên phú (§8) | | | ✅ | |

---

## 16. Câu hỏi mở

- [ ] **TLN-Q-1:** Có cho khí vận ảnh hưởng roll **thức tỉnh** (không phải roll tạo nhân vật) không? *Mặc định: không.*
- [ ] **TLN-Q-2:** Tỷ lệ Thiên linh căn 1% có quá hiếm với trải nghiệm "khoe"? Cân nhắc 2% sau khi đo telemetry Phase 4.
- [ ] **TLN-Q-3:** Có thêm ô thiên phú thứ 4 lúc tạo như một unlock Luân Hồi đắt không? Rủi ro chênh lệch người cũ / mới (nguyên tắc 3 PLAN §4.2). *Mặc định: không.*
- [ ] **TLN-Q-4:** Ngoại hình có nên phản ánh linh căn / thiên phú (màu mắt, hoa văn) để người khác nhận ra không? *Mặc định: chỉ linh quang khi đả tọa / đột phá.*
- [ ] **TLN-Q-5:** Giới hạn 6 thiên phú có làm người chơi kiếp dài thấy "hết chỗ" quá sớm? Theo dõi `awakening_choice` và tỷ lệ từ chối thay thế.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-20 | — | Bản nháp đầu: bất biến, quy trình tạo nhân vật, xuất thân, linh căn, chỉ số bẩm sinh tổng cố định, thần thức, roll thiên phú + bảo hiểm, 34 thiên phú, thay đổi trong kiếp, hook, thức tỉnh, dung hợp, tâm ma, công thức, gamedata, API, DB, nghiệm thu |
| v0.2 | 2026-10-23 | — | Thêm xuất thân `BG_TAC_THIEN_GANG` (Bang chúng Tạc Thiên) và `BG_HOP_HOAN_DISCIPLE` (Hợp Hoan đệ tử), theo GD-14 SO-Q-6 |
| v0.3 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
| v0.4 | 2026-10-25 | — | Đợt rà nhất quán: TLN-R-032 — mọi xuất thân xuất hiện ở miếu hoang `V1_F01` và qua hướng dẫn chung; ba xuất thân gắn tông chỉ đổi điểm đến sau hướng dẫn (`V1_HUB`), khớp GD-18 / GD-15 `QC_TUTORIAL_QI` |
| v0.5 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: TLN-R-032 ghi rõ `start = { mapId: "V1_F01", poiId: "V1_F01_LM_001" }` cho mọi xuất thân; điểm này cũng là nơi hồi sinh khi chưa kích hoạt truyền tống trận (GD-26 LC-R-061) |
| v0.6 | 2026-11-03 | — | Tách mở/resume attempt khỏi roll đầu; người chơi chọn `backgroundId` trong lần gọi roll đầu tiên; TLN-R-065, TLN-AC-001, 025 |
