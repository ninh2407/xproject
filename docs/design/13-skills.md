# GD-07 — Kỹ năng & Công pháp

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-07 · Prefix ID: `SK` |
| Owner | Game Designer + Client Dev |
| Trạng thái | 🟡 Bản nháp v0.2 |
| Cần trước | Phase 1 (kiếm, 4 kỹ năng, đánh thường, máy trạng thái hành động), Phase 2 (quạt, tâm pháp, thuần thục) |
| Quyết định nền | PLAN D-01 (logic 2D), D-10 (WASD), §4.9–4.10 |
| Liên quan | [PLAN §4.9–4.10](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-06 Chỉ số & Công thức](12-stats-formulas.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-27 Thiên phú](04-talents.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-08 Chế tạo công pháp](14-technique-crafting.md) · GD-09 Trang bị · TD-02 Network · TD-14 Geometry 2D |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Tham số và nội dung nằm ở `shared/gamedata/skills/*.json` (§7). Công thức sát thương, poise, thể lực ở **GD-06**; tài liệu này định nghĩa **kỹ năng làm gì và khi nào**.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-07 | Mục |
|--------------|-----------------|-----|
| GD-04 | `techniqueMultiplier` (bảng chuẩn), ô kỹ năng theo cảnh giới | §5.1, §5.3 |
| GD-06 | Dữ liệu đòn đánh (`coefficient`, `damageType`, `element`, `poiseDamage`, `HitReaction`), `armorLevel` theo trạng thái hành động, `elementAffinity` từ tâm pháp | §3, §4, §5.2 |
| GD-26 | Bí pháp liều mạng (`FORBIDDEN_ART`) | §5.10 |
| GD-27 | `skillTag` cho `MASTERY_SPEED_PCT`; hook `SWORD_PIERCE`; tương hợp hệ (TLN-R-044) trong thuần thục | §5.5, §5.11 |
| GD-08 | Khung `SkillDef` dạng thành phần + ngân sách sức mạnh theo phẩm | §3, §5.12 |

---

## 1. Mục tiêu

**Trụ cột:** P3 *Chiến đấu hành động có kỹ năng*, P4 *Chế tác có chiều sâu*.

- **Công pháp thay cho class:** build được tạo bởi tâm pháp chính + 4–5 kỹ năng + vũ khí, đổi được trong kiếp.
- Mỗi kỹ năng có **hình dạng đọc được trên mặt đất**; người chơi giỏi thắng nhờ ngắm, né, chọn thời điểm.
- Dùng nhiều thì giỏi lên (thuần thục), tạo gắn bó với bộ kỹ năng của mình.
- Kỹ năng **mô tả hoàn toàn bằng dữ liệu thành phần**, để chế tạo công pháp (GD-08) và nội dung mới không cần viết code mới.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="sk-r-001"></a>**SK-R-001** | Mọi lần thi triển do **Game Server** chấp nhận / từ chối và phán định trúng đòn. Client chỉ dự đoán (animation, hồi chiêu) và quay lui khi bị từ chối. |
| <a id="sk-r-002"></a>**SK-R-002** | Kỹ năng được ghép từ các **thành phần dữ liệu** định nghĩa ở §3 (hình dạng, lượt trúng, hiệu ứng). Không có code riêng cho từng kỹ năng; hành vi đặc biệt chỉ qua `perfection` / hook thuộc danh sách trắng §5.11. |
| <a id="sk-r-003"></a>**SK-R-003** | Không khóa công pháp / kỹ năng theo linh căn; khác hệ chỉ chậm thuần thục (GD-27 TLN-R-044). |
| <a id="sk-r-004"></a>**SK-R-004** | Công pháp đã học và độ thuần thục **thuộc kiếp hiện tại**; kiếp kết thúc thì mất (ngọc giản trong Tàng Bảo Các vẫn còn, GD-26). |
| <a id="sk-r-005"></a>**SK-R-005** | Mọi va chạm tính trên mặt phẳng 2D bằng thư viện hình học dùng chung client / server (TD-14). |

---

## 2. Tổng quan

```
 BUILD CHIẾN ĐẤU = Vũ khí (GD-09) + Tâm pháp chính (1) + Ô kỹ năng (4–5) + [Tuyệt kỹ từ Kim Đan]

 Vũ khí ──────── quyết định ĐÁNH THƯỜNG (combo chuột trái) và ĐÁNH MẠNH (giữ chuột phải)
                 và kỹ năng nào dùng được (weaponReq)
 Tâm pháp chính ─ hệ (elementAffinity), chỉ số bị động theo thuần thục, techniqueMultiplier (tu luyện)
                  hệ của đánh thường và kỹ năng "INHERIT"
 Kỹ năng ─────── Q · E · R · F (Luyện Khí) · V (Trúc Cơ) · T tuyệt kỹ (Kim Đan, cần năng lượng)
                  mỗi kỹ năng = hình dạng + lượt trúng + hiệu ứng + chi phí + hồi chiêu
 Thuần thục ──── Nhập Môn → Tiểu Thành → Đại Thành → Viên Mãn (mỗi kỹ năng và tâm pháp)
 Nguồn học ───── ngọc giản / bí tịch (item TECHNIQUE_SCROLL): cửa hàng tông môn, rơi đồ, cơ duyên, chế tạo (GD-08)
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type WeaponType = 'SWORD' | 'FAN';                 // MVP; Beta thêm 'DUAL_BLADE' | 'FIST'; sau launch 'ZITHER'
type GradeTier = 'HOANG' | 'HUYEN' | 'DIA' | 'THIEN';
type SubGrade = 'HA' | 'TRUNG' | 'THUONG';
interface Grade { tier: GradeTier; sub: SubGrade }  // gradeIndex = tierIndex × 3 + subIndex (0..11)
type MasteryLevel = 1 | 2 | 3 | 4;                  // Nhập Môn · Tiểu Thành · Đại Thành · Viên Mãn
type SkillSlot = 'Q' | 'E' | 'R' | 'F' | 'V' | 'T';
type SkillElement = Element | 'NONE' | 'INHERIT';  // INHERIT = hệ của tâm pháp chính
type HitReaction = 'NONE' | 'FLINCH' | 'STAGGER' | 'KNOCKBACK' | 'KNOCKDOWN';   // GD-06 SF-R-093
type ArmorLevel = 'NONE' | 'LIGHT' | 'HEAVY' | 'IMMUNE';                         // GD-06 SF-R-094
type RoleTag = 'BALANCED' | 'ATTACK' | 'DEFENSE' | 'CONTROL' | 'SUPPORT';

// ===== Hình dạng (mặt phẳng 2D, đơn vị mét, góc độ) =====
type Shape =
  | { type: 'PROJECTILE'; speed: number; range: number; radius: number; pierce: number;
      count?: number; spreadDeg?: number; explodeRadius?: number }
  | { type: 'CONE'; radius: number; angleDeg: number }
  | { type: 'LINE'; length: number; width: number }                      // đâm thẳng tức thì
  | { type: 'GROUND_CIRCLE'; radius: number; maxRange: number; delaySec: number; persistSec?: number }
  | { type: 'DASH'; distance: number; width: number; iframe: boolean }   // di chuyển nhân vật, trúng dọc đường
  | { type: 'SELF_CIRCLE'; radius: number }
  | { type: 'SELF' }                                                      // chỉ tác dụng lên bản thân
  | { type: 'SUMMON'; summonId: string; durationSec: number; maxActive: number };

interface HitDef {                    // một "lượt trúng"
  atSec: number;                      // thời điểm (tính từ khi hết windup) xảy ra lượt trúng
  ticks?: { count: number; intervalSec: number };  // trúng lặp lại (vùng tồn tại)
  target: 'ENEMIES' | 'ALLIES' | 'SELF';
  maxTargets: number;
  damage?: { damageType: 'PHYSICAL' | 'SPELL'; element: SkillElement; coefficient: number; flat?: number };
  heal?: { coefficient: number };
  shield?: { pctMaxHp: number; durationSec: number };
  poiseDamage: number;
  reaction: HitReaction; knockbackDistance?: number;
  statuses?: { statusId: string; chance: number; durationSec?: number }[];   // statusId thuộc GD-06 §5.12
  energyGain?: number;
  unblockable?: boolean;
}

interface SkillDef {
  id: string;                         // SKL_*
  nameKey: string; descKey: string;
  grade: Grade;
  weaponReq: WeaponType | 'ANY';
  isUltimate: boolean;
  realmMin: string;                   // RealmCode để học
  tags: string[];                     // vd. "sword", "fire", "element_hoa", "movement" (dùng cho GD-27 MASTERY_SPEED_PCT)
  shape: Shape;
  castTimeSec: number;                // thời gian niệm (0 = tức thì), có thể di chuyển chậm nếu movableCast
  windupSec: number;                  // animation trước lượt trúng đầu
  recoverySec: number;
  cancelAfterSec: number;             // từ thời điểm này được hủy sang né / kỹ năng khác
  movableCast: boolean;
  armorDuringCast: ArmorLevel;
  hits: HitDef[];
  selfStatuses?: { statusId: string; durationSec: number }[];
  costs: { mpPctOfBase: number; stamina?: number; lifespanYears?: number; energy?: number };
  cooldownSec: number;
  holdToCastSec?: number;             // bắt buộc giữ phím (bí pháp liều mạng)
  perfection: PerfectionEffect;       // hiệu ứng khi đạt Viên Mãn (§5.5)
  telegraphForOthers: boolean;        // hiện vùng cảnh báo cho người chơi khác / quái (luôn true với quái)
}

type PerfectionEffect =
  | { type: 'COEFFICIENT_PCT'; value: number }
  | { type: 'RADIUS_ADD'; value: number }
  | { type: 'EXTRA_PROJECTILES'; count: number; spreadDeg: number }
  | { type: 'DURATION_ADD'; value: number }
  | { type: 'ADD_STATUS'; hitIndex: number; statusId: string; chance: number }
  | { type: 'SHIELD_PCT_ADD'; value: number }
  | { type: 'RESET_COOLDOWN_ON_KILL' }
  | { type: 'EXTRA_HIT'; delaySec: number; coefficientPct: number }
  | { type: 'EXTRA_SUMMON'; count: number }
  | { type: 'COST_REDUCE'; lifespanYears?: number; mpPct?: number }
  | { type: 'ON_EXPIRE_EXPLODE'; coefficient: number; radius: number }
  | { type: 'DISPEL_ALLY'; count: number };

interface MainTechniqueDef {
  id: string;                         // TEC_*
  nameKey: string; descKey: string;
  grade: Grade;
  element: Element | 'NONE';          // → elementAffinity (GD-06 SF-R-060)
  role: RoleTag;
  realmMin: string;
  passives: TechniquePassive[];       // giá trị theo từng mức thuần thục
}
type TechniquePassive =
  | { type: 'STAT'; stat: CombatStat; kind: 'FLAT' | 'PCT'; byMastery: [number, number, number, number] }
  | { type: 'STATUS_CHANCE_BONUS'; skillTag?: string; element?: Element; statusId: string;
      byMastery: [number, number, number, number] };

interface WeaponDef {
  type: WeaponType;
  basicCombo: ComboStep[];
  heavy: { levels: { chargeSec: number; stamina: number; skill: Omit<SkillDef, 'id' | 'grade' | 'cooldownSec' | 'perfection'> }[];
           armorWhileCharging: ArmorLevel };
  aimAssist: { angleDeg: number; extraRange: number };
}
interface ComboStep {
  shape: Shape; hit: HitDef;          // element thường là 'INHERIT'
  windupSec: number; activeSec: number; recoverySec: number;
  nextInputWindowSec: number;         // bấm tiếp trong khoảng này để sang bước kế
  cancelAfterSec: number;
}

interface CharacterTechnique {        // thuộc kiếp
  characterId: string; refId: string; kind: 'MAIN' | 'SKILL';   // refId = TEC_* | SKL_* | id kỹ năng chế tạo
  masteryLevel: MasteryLevel; masteryPoints: number;
  source: 'STARTER' | 'SCROLL' | 'ENCOUNTER' | 'CRAFTED';
  learnedAt: string;
}
interface Loadout { characterId: string; mainTechniqueId: string | null;
                    slots: Partial<Record<SkillSlot, string>>; mainChangedAt: string | null }
```

---

## 4. Máy trạng thái hành động (mỗi thực thể chiến đấu)

| Trạng thái | `armorLevel` | Di chuyển | Chuyển ra khi | Ghi chú |
|------------|:---:|:---:|---------------|---------|
| `IDLE` / `MOVING` | NONE | ✅ | Mọi lệnh hợp lệ | |
| `BASIC_ATTACK` (bước n) | NONE | Chậm (30%) | Hết `recoverySec`; bấm tiếp trong `nextInputWindowSec` → bước n+1; sau `cancelAfterSec` được né / kỹ năng | |
| `HEAVY_CHARGE` | Theo vũ khí (LIGHT) | Chậm (30%) | Thả chuột phải → `CASTING` đòn mạnh theo mức đã tụ | Tiêu thể lực khi thả |
| `CASTING` (niệm + windup) | `armorDuringCast` | Nếu `movableCast` | Hết windup → `ACTIVE`; bị ngắt → `IDLE` (SK-R-064) | |
| `ACTIVE` | `armorDuringCast` | Không (trừ DASH) | Hết lượt trúng cuối → `RECOVERY` | |
| `RECOVERY` | NONE | Chậm | Hết `recoverySec`; từ `cancelAfterSec` được hủy | |
| `DODGING` | IMMUNE trong i-frame | Lướt | Hết lướt | GD-06 SF-R-100 |
| `BLOCKING` | NONE | Chậm (40%) | Thả Shift | Đỡ / đỡ hoàn hảo GD-06 |
| `STAGGERED` / `KNOCKED_BACK` / `KNOCKED_DOWN` | NONE | ❌ | Hết thời gian phản ứng | GD-06 SF-R-093 |
| `STUNNED` / `ROOTED` / `SILENCED` | NONE | ❌ / ❌ / ✅ | Hết status | SILENCE vẫn đánh thường / né |
| `POISE_BROKEN` | NONE | ❌ | Hết choáng phá thế | GD-06 SF-R-091 |
| `CHANNELING` | NONE | ❌ | Đả tọa / đột phá / tương tác kết thúc | GD-04 |
| `DOWNED` | — | Bò chậm (1 m/s) | Được cứu / hết giờ | Hấp hối trong hiểm địa (GD-28 §5.5) |
| `DORMANT_GUARD` | NONE | ❌ | Kết nối lại / hết thời hạn | Quy tức khi mất kết nối trong hiểm địa (GD-28 §5.7); có `ST_DORMANT_GUARD` |
| `DEAD` | — | ❌ | — | |

- <a id="sk-r-010"></a>**SK-R-010** Lệnh nhận khi trạng thái không cho phép thì **đệm** tối đa `params.inputBufferSec` (0,2s) rồi thực hiện ngay khi được phép; quá hạn thì bỏ.

---

## 5. Quy tắc

### 5.1. Phẩm cấp
- <a id="sk-r-020"></a>**SK-R-020** Phẩm: Hoàng → Huyền → Địa → Thiên, mỗi phẩm chia Hạ / Trung / Thượng (12 bậc, `gradeIndex` 0..11).
- <a id="sk-r-021"></a>**SK-R-021** **Hệ số tu luyện của tâm pháp** (`techniqueMultiplier`, dùng ở GD-04 §6.1, **đây là bảng chuẩn**):
```
techniqueMultiplier = tierBase[tier] + subBonus[sub] + masteryBonus[level]
tierBase: HOANG 1,00 · HUYEN 1,15 · DIA 1,30 · THIEN 1,50
subBonus: HA 0 · TRUNG 0,025 · THUONG 0,05
masteryBonus: 1 → 0 · 2 → 0,05 · 3 → 0,10 · 4 → 0,15
```
  Không có tâm pháp chính → 0,8.
- <a id="sk-r-022"></a>**SK-R-022** **Hệ số sức mạnh kỹ năng theo phẩm:** `gradeCoefMult = tierCoef[tier] + subCoef[sub]` với `tierCoef`: HOANG 1,00 · HUYEN 1,20 · DIA 1,45 · THIEN 1,75; `subCoef`: HA 0 · TRUNG 0,05 · THUONG 0,10. Áp lên `coefficient` của mọi `damage`/`heal` và `shield.pctMaxHp`.

### 5.2. Tâm pháp chính
- <a id="sk-r-030"></a>**SK-R-030** Mỗi nhân vật trang bị **tối đa 1** tâm pháp chính. `elementAffinity` của nhân vật = `element` của tâm pháp (GD-06 SF-R-060). Hệ `INHERIT` của đánh thường và kỹ năng lấy từ đây.
- <a id="sk-r-031"></a>**SK-R-031** Bị động (`passives`) áp dụng theo mức thuần thục hiện tại của tâm pháp, dưới dạng `StatModifier` nguồn `TECHNIQUE` (GD-06 §3).
- <a id="sk-r-032"></a>**SK-R-032** Đổi tâm pháp chính: chỉ khi đang **đả tọa** (GD-04 CU-R-030), không trong chiến đấu; cooldown `params.mainTechniqueSwapCooldownSec` (1800s thực). Độ thuần thục của từng tâm pháp được giữ riêng.
- <a id="sk-r-033"></a>**SK-R-033** Tâm pháp khởi đầu: khi lên `LK1` (GD-04 CU-R-023), nhân vật được học và trang bị `TEC_DAN_KHI`.

### 5.3. Kỹ năng và ô kỹ năng
- <a id="sk-r-040"></a>**SK-R-040** Số ô theo cảnh giới: `PN` 0 · `LK` 4 (Q, E, R, F) · `TC` 5 (+V) · `KD`+ 5 + ô tuyệt kỹ (T). Kiểm tra bằng `realmAtLeast` (GD-04 CU-R-011).
- <a id="sk-r-041"></a>**SK-R-041** Ô `T` chỉ nhận kỹ năng `isUltimate = true`; ô Q–V không nhận tuyệt kỹ.
- <a id="sk-r-042"></a>**SK-R-042** Đổi kỹ năng trong ô: không trong chiến đấu (GD-06 SF-R-010); kỹ năng vừa đưa vào ô có hồi chiêu `max(còn lại, params.slotSwapCooldownSec (3s))`.
- <a id="sk-r-043"></a>**SK-R-043** Kỹ năng `weaponReq ≠ ANY` mà vũ khí đang cầm không khớp thì hiện mờ, không thi triển được (vẫn giữ trong ô).
- <a id="sk-r-044"></a>**SK-R-044** Học kỹ năng / tâm pháp: dùng vật phẩm `TECHNIQUE_SCROLL` (trường `teaches: refId`), cần `realmMin`; vật phẩm bị tiêu; đã biết thì không dùng được (UI chặn trước). Không giới hạn số kỹ năng đã biết.
- <a id="sk-r-045"></a>**SK-R-045** Kỹ năng khởi đầu khi lên `LK1` theo lựa chọn vũ khí ở nhiệm vụ hướng dẫn: kiếm → `SKL_SWORD_QI_SLASH`, `SKL_FLASH_STEP`; quạt → `SKL_FIRE_ORB`, `SKL_FROST_FIELD`. Vũ khí còn lại học sau ở cửa hàng tông môn. (Bước chọn vũ khí của `QC_TUTORIAL_QI`, GD-15 §5.8.)

### 5.4. Vũ khí, đánh thường, đánh mạnh
- <a id="sk-r-050"></a>**SK-R-050** Vũ khí đang trang bị (GD-09) quyết định `WeaponDef`. Đổi vũ khí chỉ ngoài chiến đấu.
- <a id="sk-r-051"></a>**SK-R-051** Đánh thường không tốn linh lực, không hồi chiêu; hệ `INHERIT`. Bỏ lỡ `nextInputWindowSec` thì combo về bước 1.
- <a id="sk-r-052"></a>**SK-R-052** **Hỗ trợ ngắm** chỉ áp dụng cho đánh thường: hướng đòn hút về kẻ địch gần nhất trong góc `aimAssist.angleDeg` (25°) quanh hướng con trỏ và trong tầm + `extraRange` (1m). Kỹ năng đi chính xác theo con trỏ.
- <a id="sk-r-053"></a>**SK-R-053** Đánh mạnh: giữ chuột phải để tụ; thả ra thi triển mức cao nhất đã đạt (`chargeSec`); tiêu thể lực của mức đó; không đủ thể lực thì dùng mức thấp hơn đủ thể lực, không mức nào đủ thì hủy.

| Vũ khí | Đánh thường | Đánh mạnh |
|--------|-------------|-----------|
| **Kiếm** (`SWORD`) | 4 bước, hình quạt 2,2m / 100° (bước 4: 2,8m / 140°); hệ số 0,6 · 0,6 · 0,8 · 1,2 (`PHYSICAL`); poise 10 · 10 · 15 · 30; phản ứng FLINCH ×3, STAGGER ở bước 4 | Mức 1 (chạm, 15 thể lực): đâm thẳng `LINE` 4m × 1m, hệ số 1,0. Mức 2 (tụ 1,0s, 25 thể lực): hệ số 2,2, KNOCKBACK 2m, poise 45. Giáp `LIGHT` khi tụ |
| **Quạt** (`FAN`) | 3 bước, đạn `PROJECTILE` tốc độ 18 m/s, tầm 12m, bán kính 0,4m; hệ số 0,45 · 0,45 · 0,9 (`SPELL`); poise 5 · 5 · 15 | Mức 1 (chạm, 12 thể lực): quạt `CONE` 4m / 60°, hệ số 0,9, KNOCKBACK 2m. Mức 2 (tụ 0,8s, 20 thể lực): `CONE` 6m / 60°, hệ số 1,6, KNOCKBACK 3m |

### 5.5. Thuần thục
- <a id="sk-r-060"></a>**SK-R-060** **Điểm thuần thục thô:**
  - Kỹ năng: +1 mỗi lần thi triển trúng ít nhất 1 kẻ địch (kỹ năng hồi máu / khiên / bản thân: mỗi lần thi triển có tác dụng); +3 nếu trúng tinh anh / boss.
  - Tâm pháp chính: +1 mỗi 10 tu vi nhận từ `MEDITATION`, `SECLUSION`, `PILL` (GD-04 `ExpSource`); +1 mỗi 60s trong chiến đấu.
- <a id="sk-r-061"></a>**SK-R-061** Điểm thực nhận = thô × `masterySpeed` (§6.3). Game Server gom điểm thô mỗi `params.masteryBatchSec` (30s) gửi API (§9.1); API áp hệ số và lên cấp.
- <a id="sk-r-062"></a>**SK-R-062** Ngưỡng lên cấp (điểm tích lũy) = `baseThreshold[level] × gradeFactor[tier]`:

| | Lên Tiểu Thành | Lên Đại Thành | Lên Viên Mãn |
|---|:---:|:---:|:---:|
| Kỹ năng | 200 | 600 | 1.500 |
| Tâm pháp | 1.000 | 3.000 | 8.000 |

  `gradeFactor`: HOANG 1,0 · HUYEN 1,5 · DIA 2,2 · THIEN 3,0.
- <a id="sk-r-063"></a>**SK-R-063** Hiệu ứng thuần thục của kỹ năng: Tiểu Thành → hệ số ×1,10; Đại Thành → hồi chiêu ×0,85; Viên Mãn → thêm `perfection` của kỹ năng. Tâm pháp: bị động theo cột tương ứng trong `byMastery`.
- <a id="sk-r-064"></a>**SK-R-064** **Bị ngắt khi đang niệm / windup** (STAGGER trở lên vượt `armorDuringCast`, STUN, SILENCE): kỹ năng bị hủy, **hoàn 100% linh lực**, hồi chiêu đặt `params.interruptCooldownSec` (1s). Bí pháp liều mạng bị ngắt **trước** khi tiêu thọ nguyên thì không tiêu.

### 5.6. Quy trình thi triển trên server
- <a id="sk-r-070"></a>**SK-R-070** Client gửi `C2S_CastSkill { seq, slot, aim: [x, z], clientTimeMs }`. Server kiểm tra theo thứ tự, trượt điều nào thì trả `S2C_CastRejected { seq, reason }`:

| # | Kiểm tra | `reason` |
|:-:|----------|----------|
| 1 | Còn sống, không `STUNNED`/`SILENCED`/`POISE_BROKEN`/`DOWNED`/`CHANNELING` | `STATE` |
| 2 | Trạng thái hành động cho phép (hoặc đệm SK-R-010) | `BUSY` |
| 3 | Ô có kỹ năng, đúng vũ khí | `NO_SKILL` / `WEAPON` |
| 4 | Hết hồi chiêu (dung sai `params.cooldownToleranceMs` 100ms) | `COOLDOWN` |
| 5 | Đủ linh lực / thể lực / năng lượng | `RESOURCE` |
| 6 | Bí pháp: đã giữ phím đủ `holdToCastSec` | `HOLD` |

- <a id="sk-r-071"></a>**SK-R-071** Chấp nhận: trừ tài nguyên, bắt đầu hồi chiêu, phát `S2C_SkillCast` cho mọi người chơi trong AOI (kèm vùng cảnh báo nếu `telegraphForOthers`). `aim` bị kẹp vào `maxRange` / `range`.
- <a id="sk-r-072"></a>**SK-R-072** Lượt trúng xảy ra tại `windupSec + hit.atSec` theo thời gian server. Vị trí mục tiêu được **tua lại** `min(RTT/2 + interpDelay, params.maxRewindMs (200ms))` để bù trễ (TD-02). Chi tiết: [TD-02 §7.2](../tech/01-network.md); hình học và quy tắc trúng theo thân: [TD-14 §5](../tech/13-geom2d.md).
- <a id="sk-r-073"></a>**SK-R-073** Đạn bay (`PROJECTILE`) do server mô phỏng theo tick (20Hz) bằng hình tròn quét (swept circle); va chạm địa hình chặn đạn. Client vẽ đạn theo `S2C_ProjectileSpawn`.
- <a id="sk-r-074"></a>**SK-R-074** Mỗi `HitDef` trúng một mục tiêu **tối đa một lần mỗi tick** của nó; `maxTargets` chọn theo khoảng cách tăng dần từ tâm hình. Mặc định: đạn 1 (+`pierce`), vùng 8.
- <a id="sk-r-075"></a>**SK-R-075** Không có sát thương lên đồng minh. `target: ALLIES` gồm bản thân và thành viên tổ đội trong hình. `target: ENEMIES` gồm quái **và** người chơi mà chế độ PK cho phép tấn công (GD-32 PK-R-021), áp hệ số PvP (PK-R-060).
- <a id="sk-r-076"></a>**SK-R-076** Kết quả từng lượt trúng tính theo GD-06 §6.3 và gửi `S2C_HitResults` (GD-06 §9.3).

### 5.7. Hình dạng
- <a id="sk-r-080"></a>**SK-R-080** Quy ước hình học (TD-14): `CONE` có đỉnh tại tâm nhân vật, hướng theo `aim`; `LINE` là hình chữ nhật xoay bắt đầu từ mép nhân vật; `GROUND_CIRCLE` đặt tại `aim` (đã kẹp); `DASH` di chuyển nhân vật theo hướng `aim`, dừng trước chướng ngại, trúng mọi mục tiêu giao với hình chữ nhật đã đi qua. Định nghĩa hình học chính xác: [TD-14 §5.2](../tech/13-geom2d.md).
- <a id="sk-r-081"></a>**SK-R-081** `GROUND_CIRCLE` có `delaySec > 0` luôn hiện vùng cảnh báo trên mặt đất cho mục tiêu (người chơi thấy vùng của quái; quái AI "thấy" vùng của người chơi và có thể né theo GD-12).
- <a id="sk-r-082"></a>**SK-R-082** `PROJECTILE` có `explodeRadius`: khi chạm mục tiêu / địa hình / hết tầm thì nổ, áp lượt trúng cho mọi mục tiêu trong bán kính (bao gồm mục tiêu bị chạm).

### 5.8. Triệu hồi
- <a id="sk-r-090"></a>**SK-R-090** Vật triệu hồi (`summons.json`) là thực thể thuộc chủ, chụp chỉ số của chủ lúc gọi, sống `durationSec`, tối đa `maxActive` mỗi kỹ năng (gọi lại thì thay cái cũ nhất). Biến mất khi chủ chuyển bản đồ, chết hoặc vào `CHANNELING`.
- <a id="sk-r-091"></a>**SK-R-091** Vật triệu hồi tấn công tự động kẻ địch gần nhất trong `aggroRange` (8m) của chủ; không bị quái nhắm làm mục tiêu ưu tiên (trừ tường chắn `SKL_EARTH_WALL` chặn đường / đạn).

### 5.9. Tuyệt kỹ
- <a id="sk-r-100"></a>**SK-R-100** Tuyệt kỹ (`isUltimate`) cần Kim Đan trở lên, tiêu 100 năng lượng (GD-06 SF-R-111), không có hồi chiêu ngoài năng lượng; giáp `HEAVY` trong windup. MVP chưa có tuyệt kỹ (Kim Đan giới hạn); nội dung ở Beta (§7.3).

### 5.10. Bí pháp liều mạng
- <a id="sk-r-110"></a>**SK-R-110** Kỹ năng có `costs.lifespanYears > 0` là bí pháp: phải **giữ phím** `holdToCastSec` (1s); lần đầu mỗi kiếp hiện hộp thoại xác nhận theo GD-26 LC-R-042 (tuổi trước → sau).
- <a id="sk-r-111"></a>**SK-R-111** Thọ nguyên bị tiêu khi lượt trúng / hiệu ứng đầu tiên xảy ra (sau windup), qua GD-26 `lifespan-change` (`USED`, `FORBIDDEN_ART`, ref `skillId:castSeq`).
- <a id="sk-r-112"></a>**SK-R-112** Không bị chặn khi thọ còn ít; nếu tiêu xong thọ về 0 thì GD-26 áp `LAST_LIGHT`.

### 5.11. Hook và tương tác với thiên phú
- <a id="sk-r-120"></a>**SK-R-120** Hook `SWORD_PIERCE` (GD-27 §5.9): kỹ năng có tag `sword` và `shape.type = PROJECTILE` được `pierce += 1`, `maxTargets` tối đa 2.
- <a id="sk-r-121"></a>**SK-R-121** Hiệu ứng `MASTERY_SPEED_PCT` có `skillTag` áp lên kỹ năng có tag đó; không có `skillTag` thì áp lên mọi kỹ năng và tâm pháp.
- <a id="sk-r-122"></a>**SK-R-122** Hook `CHAOS_ROOT`: mọi kỹ năng / tâm pháp được coi là cùng hệ khi tính `masterySpeed`.
- <a id="sk-r-123"></a>**SK-R-123** Tag hệ của kỹ năng tự sinh: kỹ năng có `damage.element = X` (khác `INHERIT`/`NONE`) có tag `element_x` (vd. `element_hoa`), dùng cho thiên phú `ELEMENT_BODY`.

### 5.12. Ngân sách sức mạnh (dùng cho kiểm tra nội dung và GD-08)
- <a id="sk-r-130"></a>**SK-R-130** Mỗi kỹ năng (không tính đánh thường, tuyệt kỹ) có `powerScore` theo §6.5, phải ≤ `budget[tier]` của phẩm. Validator kiểm tra mọi kỹ năng tác giả viết; GD-08 dùng cùng hàm cho kỹ năng chế tạo, có bổ sung `projectileFactor` và `targetFactor` cho đòn đa mục tiêu không phải vùng ([GD-08 §6.3](14-technique-crafting.md)).

---

## 6. Công thức

### 6.1. Hệ số hiệu lực
```
effectiveCoefficient = coefficient × gradeCoefMult (SK-R-022) × (masteryLevel ≥ 2 ? 1,10 : 1) × perfectionCoef
effectiveCooldown    = max(0,5, cooldownSec × (1 − cooldownReduction) × (masteryLevel ≥ 3 ? 0,85 : 1))
manaCost             = mpPctOfBase / 100 × baseStats[realmCode].maxMp            (GD-06 §5.2)
```
Ví dụ: `SKL_SWORD_RAIN` (Huyền Trung, hệ số mỗi tick 0,6), Tiểu Thành → `0,6 × 1,25 × 1,10 = 0,825` mỗi tick. Hồi chiêu 14s, `cooldownReduction` 10%, Đại Thành → `14 × 0,9 × 0,85 = 10,71s`. Ở `LK5`, chi phí 25% → `0,25 × 146 = 36,5` linh lực.

### 6.2. Sát thương của lượt trúng
Dùng GD-06 §6.3 với `coefficient = effectiveCoefficient`, `element` = hệ của lượt trúng (`INHERIT` → `elementAffinity` của người thi triển).

### 6.3. Tốc độ thuần thục
```
masterySpeed = clamp(1 + (comprehension − 50) × 0,01, 0,5, 2,0)
             × (sameElement ? 1 : 0,7)                       (GD-27 TLN-R-044; NONE / INHERIT luôn coi là cùng hệ)
             × (1 + Σ MASTERY_SPEED_PCT phù hợp)              (GD-27)
sameElement  = element của kỹ năng / tâm pháp ∈ linh căn nhân vật (hoặc có hook CHAOS_ROOT)
```
Ví dụ: ngộ tính 70, kỹ năng hệ Hỏa, linh căn không có Hỏa, có `TAL_SWORD_AFFINITY` (+0,5 cho tag `sword`, kỹ năng có tag `sword`) → `1,2 × 0,7 × 1,5 = 1,26`.

### 6.4. Hệ số tu luyện tâm pháp
SK-R-021. Ví dụ `TEC_THANH_MOC` (Huyền Trung) Đại Thành → `1,15 + 0,025 + 0,10 = 1,275`.

### 6.5. Điểm sức mạnh kỹ năng
```
damageValue = Σ_hits ( effectiveCoefficient × tickCount × targetFactor )        (gradeCoefMult = 1, mastery = 1)
targetFactor: maxTargets = 1 → 1,0 ; vùng bán kính / độ dài ≤ 3m → 1,5 ; > 3m → 2,0
healValue   = Σ heal coefficient × tickCount × 1,5 (tối đa 5 đồng minh)
shieldValue = pctMaxHp × 10
ccValue     = STUN / FROZEN / ENTANGLE: 0,6 mỗi giây khống chế ; KNOCKDOWN 0,4 ; KNOCKBACK 0,2 ; STAGGER 0,1
statusValue = Σ chance × statusWeight (DoT / debuff: 0,3 ; buff bản thân: 0,5 mỗi 10% chỉ số)
mobility    = DASH: 0,4 ; iframe: +0,3
powerScore  = (damageValue + healValue + shieldValue + ccValue + statusValue + mobility) / (cooldownSec + 1)
budget[tier]: HOANG 0,45 · HUYEN 0,55 · DIA 0,68 · THIEN 0,85
```
Ví dụ `SKL_THUNDER_STRIKE`: damage 3,0 × 1 × 1,5 = 4,5; STUN 0,8s → 0,48; `ST_SHOCK` 100% → 0,3; tổng 5,28 / 11 = 0,48 ≤ 0,55 (Huyền) ✓.

---

## 7. Gamedata và nội dung

| File | Nội dung |
|------|----------|
| `shared/gamedata/skills/params.json` | Tham số (dưới) |
| `shared/gamedata/skills/weapons.json` | `WeaponDef` cho `SWORD`, `FAN` |
| `shared/gamedata/skills/main_techniques.json` | `MainTechniqueDef` (§7.1) |
| `shared/gamedata/skills/skills.json` | `SkillDef` (§7.2, §7.3) |
| `shared/gamedata/skills/summons.json` | Vật triệu hồi |
| `shared/gamedata/items/*.json` | `TECHNIQUE_SCROLL` với `teaches` |

```jsonc
// params.json
{
  "inputBufferSec": 0.2,
  "slotsByRealm": { "PHAM_NHAN": [], "LUYEN_KHI": ["Q","E","R","F"], "TRUC_CO": ["Q","E","R","F","V"],
                    "KIM_DAN": ["Q","E","R","F","V","T"] },
  "slotSwapCooldownSec": 3, "mainTechniqueSwapCooldownSec": 1800, "interruptCooldownSec": 1,
  "cooldownToleranceMs": 100, "maxRewindMs": 200 /* tham khảo; nguồn: world/network.json (TD-02 §13) */, "minCooldownSec": 0.5,
  "techniqueTierBase": { "HOANG": 1.00, "HUYEN": 1.15, "DIA": 1.30, "THIEN": 1.50 },
  "techniqueSubBonus": { "HA": 0, "TRUNG": 0.025, "THUONG": 0.05 },
  "techniqueMasteryBonus": [0, 0.05, 0.10, 0.15], "noMainTechniqueMultiplier": 0.8,
  "skillTierCoef": { "HOANG": 1.00, "HUYEN": 1.20, "DIA": 1.45, "THIEN": 1.75 },
  "skillSubCoef": { "HA": 0, "TRUNG": 0.05, "THUONG": 0.10 },
  "masteryCoefBonus": 0.10, "masteryCooldownMult": 0.85,
  "masteryThresholds": { "SKILL": [200, 600, 1500], "MAIN": [1000, 3000, 8000] },
  "masteryGradeFactor": { "HOANG": 1.0, "HUYEN": 1.5, "DIA": 2.2, "THIEN": 3.0 },
  "masteryBatchSec": 30, "masteryEliteBonus": 3, "masteryMainExpPerPoint": 10, "masteryMainCombatSecPerPoint": 60,
  "masteryComprehensionFactor": 0.01, "masterySpeedMin": 0.5, "masterySpeedMax": 2.0, "offElementMasteryMult": 0.7,
  "aimAssist": { "angleDeg": 25, "extraRange": 1 },
  "defaultMaxTargets": { "PROJECTILE": 1, "AREA": 8 },
  "forbiddenArtHoldSec": 1.0, "persistCooldownMinSec": 60,
  "powerBudget": { "HOANG": 0.45, "HUYEN": 0.55, "DIA": 0.68, "THIEN": 0.85 }
}
```

### 7.1. Tâm pháp MVP (7)

| ID | Tên | Phẩm | Hệ | Vai trò | Bị động (Nhập Môn → Viên Mãn) | Nguồn |
|----|-----|------|----|---------|-------------------------------|-------|
| `TEC_DAN_KHI` | Dẫn Khí Quyết | Hoàng Hạ | NONE | BALANCED | `maxMp` +5% → +10% | Khởi đầu |
| `TEC_HAU_THO` | Hậu Thổ Quyết | Hoàng Thượng | THO | DEFENSE | `defense` +8% → +15%; `maxPoise` +10 → +30 | Cửa hàng tông môn |
| `TEC_HUYEN_THUY` | Huyền Thủy Chân Kinh | Huyền Hạ | THUY | CONTROL | `spellResist` +10% → +20%; kỹ năng hệ THUY: xác suất `ST_FROST` +10 → +25 điểm % | Cửa hàng tông môn |
| `TEC_THANH_MOC` | Thanh Mộc Trường Sinh Quyết | Huyền Trung | MOC | SUPPORT | `healingBonus` +10 → +25 điểm %; `hpRegen` (kể cả khi chiến đấu) +0,5 → +1,0 điểm %/s | Bí cảnh BC02 |
| `TEC_LIET_HOA_KIEM` | Liệt Hỏa Kiếm Điển | Huyền Trung | HOA | ATTACK | `elementDamage.HOA` +10 → +20 điểm %; kỹ năng tag `sword`: xác suất `ST_BURN` +15 → +30 điểm % | Bí cảnh BC03 |
| `TEC_CANH_KIM` | Canh Kim Bất Diệt Thể | Huyền Thượng | KIM | DEFENSE | `maxHp` +10% → +20%; `defense` +10% → +20%; `moveSpeed` −5% | Cơ duyên |
| `TEC_LOI_DINH` | Lôi Đình Kiếm Quyết | Địa Hạ | LOI | ATTACK | `critRate` +5 → +10 điểm %; `elementDamage.LOI` +15 → +30 điểm % | Chuỗi cơ duyên `CHAIN_V1_KIEM_TRUNG` |

Giá trị Tiểu Thành / Đại Thành nội suy tuyến tính giữa Nhập Môn và Viên Mãn (làm tròn tới 0,5).

### 7.2. Kỹ năng MVP (14)

Hệ số là giá trị **gốc** (trước `gradeCoefMult` và thuần thục). MP = % linh lực cơ bản của cảnh giới.

| ID | Tên | Vũ khí | Phẩm | Hình dạng | Đòn | Hệ số | MP | CD | Hiệu ứng | Viên Mãn |
|----|-----|--------|------|-----------|-----|:---:|:---:|:---:|----------|----------|
| `SKL_SWORD_QI_SLASH` | Kiếm Khí Trảm | Kiếm | Hoàng Hạ | Đạn 20 m/s, 10m, r 0,5 | PHYS / INHERIT | 1,6 | 10 | 4s | Poise 20, STAGGER | +2 luồng, quạt 15° |
| `SKL_SWEEPING_BLADE` | Hoành Tảo Thiên Quân | Kiếm | Hoàng Thượng | Quạt 3m / 160° | PHYS / INHERIT | 2,2 | 15 | 7s | Poise 35, KNOCKBACK 2m | `ST_ARMOR_BREAK` 100% |
| `SKL_FLASH_STEP` | Thuấn Ảnh Kiếm | Kiếm | Huyền Hạ | Lướt 7m × 1,2m, i-frame | PHYS / INHERIT | 1,4 | 12 | 9s | STAGGER | Hồi chiêu reset khi hạ địch |
| `SKL_SWORD_RAIN` | Vạn Kiếm Quy Tông | Kiếm | Huyền Trung | Vùng r 4, tầm 10, trễ 0,6s, 5 tick / 0,3s | PHYS / INHERIT | 0,6 / tick | 25 | 14s | FLINCH | Bán kính +1m |
| `SKL_SWORD_NET` | Kiếm Võng Hộ Thân | Kiếm | Huyền Hạ | Quanh thân r 3 | PHYS / INHERIT | 0,8 | 20 | 16s | Khiên 15% HP 4s; giáp HEAVY 0,5s | Khiên +10% |
| `SKL_FLYING_SWORD` | Phi Kiếm Truy Hồn | Kiếm | Huyền Trung | Triệu hồi 8s | PHYS / INHERIT | 0,5 / đòn (1s) | 25 | 20s | Tấn công tự động | +1 phi kiếm |
| `SKL_FIRE_ORB` | Hỏa Cầu Thuật | Quạt | Hoàng Hạ | Đạn 14 m/s, 12m, r 0,6, nổ r 2 | SPELL / HOA | 1,4 | 12 | 4s | `ST_BURN` 40% | Nổ r 3 |
| `SKL_WIND_BLADE` | Phong Nhận | Quạt | Hoàng Trung | Quạt 6m / 50° | SPELL / PHONG | 1,5 | 12 | 7s | `ST_WIND_CUT` 60%, KNOCKBACK 3m | Thêm 1 đòn sau 0,4s (60%) |
| `SKL_FROST_FIELD` | Hàn Băng Trận | Quạt | Hoàng Thượng | Vùng r 4, tầm 10, trễ 0,3s, tồn tại 4s, tick 0,5s | SPELL / THUY | 0,25 / tick | 20 | 12s | `ST_FROST` 50% mỗi tick | Tồn tại +2s |
| `SKL_THUNDER_STRIKE` | Lôi Phạt | Quạt | Huyền Hạ | Vùng r 2,5, tầm 10, trễ 0,8s | SPELL / LOI | 3,0 | 22 | 10s | `ST_STUN` 0,8s, `ST_SHOCK` 100% | Đòn thứ 2 sau 0,5s (50%) |
| `SKL_HEALING_RAIN` | Cam Lộ Thuật | Quạt | Huyền Trung | Vùng r 5, tầm 10, tồn tại 5s, tick 1s, đồng minh | Hồi / MOC | 0,35 / tick | 30 | 18s | Hồi máu | Gỡ 1 debuff mỗi đồng minh |
| `SKL_EARTH_WALL` | Thổ Tường Thuật | Quạt | Huyền Hạ | Triệu hồi tường 6m × 1m, 6s | — / THO | — | 20 | 16s | Chặn di chuyển và đạn của địch | Khi hết hạn nổ: hệ số 1,2, r 3 |
| `SKL_IRON_BODY` | Kim Cương Hộ Thể | Bất kỳ | Hoàng Thượng | Bản thân | — | — | 15 | 20s | `damageTaken` −30%, giáp HEAVY 4s | +`ST_INVULNERABLE` 0,5s đầu |
| `SKL_BURN_LIFE` | Nhiên Thọ Bí Pháp | Bất kỳ | Huyền Thượng | Bản thân | — | — | 0 | 600s | **Tiêu 5 năm thọ**; `ST_BURNING_LIFE`: `damageBonus` +50 điểm % trong 20s; giữ phím 1s | Chỉ tiêu 4 năm |

**Ví dụ dữ liệu đầy đủ:**
```jsonc
{
  "id": "SKL_SWORD_QI_SLASH", "nameKey": "skl.sword_qi_slash.name", "descKey": "skl.sword_qi_slash.desc",
  "grade": { "tier": "HOANG", "sub": "HA" }, "weaponReq": "SWORD", "isUltimate": false, "realmMin": "LK1",
  "tags": ["sword", "projectile"],
  "shape": { "type": "PROJECTILE", "speed": 20, "range": 10, "radius": 0.5, "pierce": 0 },
  "castTimeSec": 0, "windupSec": 0.25, "recoverySec": 0.35, "cancelAfterSec": 0.3,
  "movableCast": false, "armorDuringCast": "NONE",
  "hits": [ { "atSec": 0, "target": "ENEMIES", "maxTargets": 1,
              "damage": { "damageType": "PHYSICAL", "element": "INHERIT", "coefficient": 1.6 },
              "poiseDamage": 20, "reaction": "STAGGER", "energyGain": 5 } ],
  "costs": { "mpPctOfBase": 10 }, "cooldownSec": 4,
  "perfection": { "type": "EXTRA_PROJECTILES", "count": 2, "spreadDeg": 15 },
  "telegraphForOthers": false
}
```
```jsonc
{
  "id": "SKL_BURN_LIFE", "nameKey": "skl.burn_life.name", "descKey": "skl.burn_life.desc",
  "grade": { "tier": "HUYEN", "sub": "THUONG" }, "weaponReq": "ANY", "isUltimate": false, "realmMin": "LK5",
  "tags": ["forbidden"], "shape": { "type": "SELF" },
  "castTimeSec": 0, "windupSec": 0.5, "recoverySec": 0.3, "cancelAfterSec": 0.5,
  "movableCast": false, "armorDuringCast": "HEAVY",
  "hits": [], "selfStatuses": [ { "statusId": "ST_BURNING_LIFE", "durationSec": 20 } ],
  "costs": { "mpPctOfBase": 0, "lifespanYears": 5 }, "cooldownSec": 600, "holdToCastSec": 1.0,
  "perfection": { "type": "COST_REDUCE", "lifespanYears": 1 },
  "telegraphForOthers": false
}
```

### 7.3. Tuyệt kỹ (Beta, Kim Đan)
| ID | Tên | Vũ khí | Mô tả |
|----|-----|--------|-------|
| `SKL_ULT_SWORD_DOMAIN` | Kiếm Vực | Kiếm | Vùng r 6 quanh thân 6s: mọi kẻ địch trong vùng trúng 0,5 mỗi 0,5s; bản thân giáp HEAVY |
| `SKL_ULT_FIVE_ELEMENTS` | Ngũ Hành Đại Trận | Quạt | Vùng r 7 tại con trỏ, 5 đợt mỗi đợt một hệ (Kim → Thủy → Mộc → Hỏa → Thổ), mỗi đợt 1,5; áp status hệ tương ứng |

**Validator (CI):** ID `SKL_*` / `TEC_*` duy nhất; `shape`/`HitDef`/`PerfectionEffect` thuộc union §3; `statusId` tồn tại trong `statuses.json` (GD-06); `powerScore ≤ budget[tier]` (SK-R-130); `isUltimate` ⇔ có `costs.energy = 100`; `costs.lifespanYears > 0` ⇒ có `holdToCastSec ≥ 1` và tag `forbidden`; mọi `TECHNIQUE_SCROLL.teaches` trỏ tới ID tồn tại.

---

## 8. Mục tiêu cân bằng ban đầu

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Tỷ trọng sát thương đánh thường / kỹ năng (người chơi kiếm) | 40% / 60% |
| Thời gian giữa hai lần dùng kỹ năng bất kỳ (vòng xoay 4 kỹ năng) | ~1,5–2,5s |
| Linh lực đủ cho | ~8–12 kỹ năng liên tục trước khi cạn |
| Kiếm vs quạt (DPS đơn mục tiêu, cùng cảnh giới / trang bị) | Chênh ≤ 10%; quạt mạnh hơn vùng, kiếm mạnh hơn đơn mục tiêu |

---

## 9. Hợp đồng giao tiếp

### 9.1. API nội bộ (Game Server → API Service)

| Endpoint | Request | Response | Quy tắc |
|----------|---------|----------|---------|
| `POST /internal/v1/techniques/mastery` | `{ batchId, characterId, gains: [{ refId, rawPoints }] }` | `{ results: [{ refId, masteryLevel, masteryPoints, leveledUp }] }` | SK-R-060..063; idempotent theo `batchId` |
| `PUT /internal/v1/characters/{id}/loadout` | `{ slots, changeId }` | `Loadout` | SK-R-040..043; Game Server đã kiểm tra trạng thái chiến đấu |
| `PUT /internal/v1/characters/{id}/main-technique` | `{ techniqueId, changeId }` | `Loadout` | SK-R-032; phát `stats.inputs_changed` (GD-06) |

### 9.2. API công khai (Client → API Service)

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/characters/{id}/techniques` | Tâm pháp, kỹ năng đã biết (thuần thục, tiến độ), `Loadout`, ô khả dụng | |
| `POST /v1/characters/{id}/techniques/learn` | `{ itemInstanceId }` | SK-R-044 |
| `GET /v1/skills/{skillId}/preview?characterId=` | Hệ số hiệu lực, hồi chiêu, chi phí, sát thương ước tính lên mục tiêu chuẩn | §6.1 |

### 9.3. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `C2S_BasicAttack` | C→S | `{ seq, aim, clientTimeMs }` |
| `C2S_HeavyStart` / `C2S_HeavyRelease` | C→S | `{ seq, aim, clientTimeMs }` |
| `C2S_CastSkill` | C→S | `{ seq, slot, aim, clientTimeMs, holdMs? }` |
| `C2S_Dodge` | C→S | `{ seq, dir: [x, z], clientTimeMs }` |
| `C2S_BlockStart` / `C2S_BlockEnd` | C→S | `{ seq, clientTimeMs }` |
| `C2S_SetLoadout` | C→S | `{ slots }` |
| `C2S_SetMainTechnique` | C→S | `{ techniqueId }` |
| `S2C_SkillCast` | S→C (AOI) | `{ casterId, skillRef, aim, serverStartMs, telegraph? }` |
| `S2C_CastRejected` | S→C | `{ seq, reason }` |
| `S2C_ProjectileSpawn` | S→C (AOI) | `{ projectileId, ownerId, from, dir, speed, range, radius }` |
| `S2C_CooldownSync` | S→C | `{ cooldowns: { [slot]: remainingMs } }` (khi lệch dự đoán) |
| `S2C_MasteryUp` | S→C | `{ refId, masteryLevel }` |

### 9.4. Sự kiện domain (topic `technique.*`)

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `technique.learned` | `{ characterId, refId, source }` | Telemetry, Vạn Giới Lục (sau này) |
| `technique.mastery_up` | `{ characterId, refId, masteryLevel }` | `stats.inputs_changed` nếu là tâm pháp chính; Bia Vạn Cổ khi Viên Mãn tâm pháp Địa+ |
| `technique.main_changed` | `{ characterId, from, to }` | GD-06, GD-04 (`techniqueMultiplier`) |

---

## 10. Lưu trữ

```sql
ALTER TABLE character ADD COLUMN main_technique_id VARCHAR(48);
ALTER TABLE character ADD COLUMN main_technique_changed_at TIMESTAMPTZ;

CREATE TABLE character_technique (
  character_id   UUID NOT NULL REFERENCES character(id),
  ref_id         VARCHAR(64) NOT NULL,          -- TEC_* | SKL_* | CSK_* (kỹ năng chế tạo, GD-08 §3)
  kind           VARCHAR(8)  NOT NULL,          -- MAIN | SKILL
  mastery_level  SMALLINT NOT NULL DEFAULT 1,
  mastery_points INT NOT NULL DEFAULT 0,
  source         VARCHAR(16) NOT NULL,          -- STARTER | SCROLL | ENCOUNTER | CRAFTED
  learned_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, ref_id)
);

CREATE TABLE character_loadout (
  character_id UUID NOT NULL REFERENCES character(id),
  slot         CHAR(1) NOT NULL,                -- Q E R F V T
  skill_ref    VARCHAR(64) NOT NULL,
  PRIMARY KEY (character_id, slot)
);

CREATE TABLE mastery_batch (
  batch_id     VARCHAR(80) PRIMARY KEY,         -- idempotency
  character_id UUID NOT NULL,
  result       JSONB NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
```
Hồi chiêu thường không lưu DB (chỉ trong RAM Game Server; mất khi đăng xuất là chấp nhận được). Kỹ năng có `cooldownSec ≥ params.persistCooldownMinSec` (60s, vd. bí pháp) lưu vào bảng dưới để không lách hồi chiêu bằng cách đăng xuất:

```sql
CREATE TABLE character_persistent_cooldown (
  character_id UUID NOT NULL REFERENCES character(id),
  skill_ref    VARCHAR(64) NOT NULL,
  ready_at     TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (character_id, skill_ref)
);
```

---

## 11. UI/UX
- **Thanh kỹ năng:** Q · E · R · F · (V) · (T), biểu tượng hồi chiêu dạng quét; kỹ năng sai vũ khí mờ; bí pháp viền đỏ, có vòng "giữ phím".
- **Ngắm:** khi giữ phím kỹ năng (tùy chọn "giữ để ngắm"), hiện hình dạng trên mặt đất (quạt / vùng / đường đạn); mặc định thi triển ngay khi bấm theo con trỏ.
- **Bảng Công pháp** (phím `K`): tab Tâm pháp (đang dùng, danh sách, thuần thục, `techniqueMultiplier`), tab Kỹ năng (kéo thả vào ô, thẻ chi tiết hiện hình dạng, hệ số hiệu lực, sát thương ước tính, điểm thuần thục, hiệu ứng Viên Mãn).
- **Lên thuần thục:** thông báo nhỏ + hiệu ứng trên biểu tượng; Viên Mãn có hiệu ứng lớn.
- **Vùng cảnh báo của quái:** đỏ; của đồng đội: xanh mờ (tắt được).

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-06 | Toàn bộ công thức sát thương / hồi máu / poise / thể lực; status; `armorLevel` |
| GD-04 | `techniqueMultiplier`; ô kỹ năng theo cảnh giới; thuần thục tâm pháp từ tu vi hấp thụ; đổi tâm pháp khi đả tọa |
| GD-27 | Thuần thục theo ngộ tính / linh căn / thiên phú; hook `SWORD_PIERCE`, `CHAOS_ROOT`; tag `element_*` |
| GD-26 | Bí pháp tiêu thọ; công pháp thuộc kiếp |
| GD-08 | Kỹ năng chế tạo dùng `SkillDef` + ngân sách §6.5 |
| GD-09 | Vũ khí; ngọc giản là vật phẩm |
| GD-12 | Quái dùng cùng `SkillDef` (luôn `telegraphForOthers = true`) |
| GD-22 | Cơ duyên tặng ngọc giản (`GRANT_ITEM`) |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Gói `C2S_CastSkill` đến khi hồi chiêu còn 80ms | Chấp nhận (dung sai 100ms) |
| Hai lệnh kỹ năng liên tiếp trong windup | Lệnh sau được đệm 0,2s (SK-R-010) |
| `DASH` vào tường | Dừng trước tường; lượt trúng tính theo quãng đã đi |
| Mục tiêu chết trước khi đạn tới | Đạn bay tiếp tới tầm tối đa / chạm mục tiêu khác |
| Đổi vũ khí khi kỹ năng đang hồi | Hồi chiêu giữ nguyên |
| Học ngọc giản của tâm pháp đang biết | UI chặn; API trả `409 ALREADY_KNOWN`, không tiêu vật phẩm |
| Bí pháp: thả phím trước 1s | Không thi triển, không tiêu gì |
| Mất kết nối giữa windup | Server vẫn hoàn tất lượt trúng (đã chấp nhận) |
| Đăng xuất khi bí pháp đang hồi chiêu | Hồi chiêu tiếp tục khi offline (§10) |

---

## 14. Telemetry

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `skill_cast` (gộp) | skillRef, hitRate, avgTargets, damage | Kỹ năng nào hiệu quả, nào bị bỏ |
| `cast_rejected` | reason | Lệch dự đoán client / server, độ trễ |
| `loadout_snapshot` | weapon, mainTechnique, slots, realm | Build phổ biến |
| `damage_share` | basic / skill / summon / dot | So với §8 |
| `mastery_up` | refId, level, playHours | Tốc độ thuần thục |
| `forbidden_art_used` | skillRef, lifespanBefore, inDangerZone | Người chơi có dùng "liều mạng" như ý đồ |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="sk-ac-001"></a>SK-AC-001 | Nhân vật lên `LK1` qua hướng dẫn, chọn kiếm | Sau sự kiện | Biết `TEC_DAN_KHI` (đang trang bị), `SKL_SWORD_QI_SLASH`, `SKL_FLASH_STEP`; ô Q, E, R, F khả dụng |
| SK-AC-002 | Nhân vật `LK9` | Đặt kỹ năng vào ô V | Bị từ chối; lên `TC1` thì được |
| SK-AC-003 | Đặt tuyệt kỹ vào ô Q | Lưu loadout | Bị từ chối (SK-R-041) |
| SK-AC-004 | Ví dụ §6.1 | Tính | Hệ số 0,825 / tick; hồi chiêu 10,71s; chi phí 36,5 MP |
| SK-AC-005 | Ví dụ §6.3 | Tính `masterySpeed` | 1,26 |
| SK-AC-006 | Ví dụ §6.4 | Tính `techniqueMultiplier` | 1,275 |
| SK-AC-007 | Ví dụ §6.5 | Tính `powerScore` của `SKL_THUNDER_STRIKE` | ≈ 0,48 và ≤ 0,55 |
| SK-AC-008 | Mọi kỹ năng trong `skills.json` | Validator | `powerScore ≤ budget[tier]` |
| SK-AC-009 | Kỹ năng Huyền (gradeFactor 1,5) có 290 điểm | Nhận thêm 10 điểm thực | Lên Tiểu Thành (ngưỡng 300) |
| SK-AC-010 | Kỹ năng Đại Thành, CD gốc 10s, `cooldownReduction` 0 | Thi triển | Hồi chiêu 8,5s |
| SK-AC-011 | Đang niệm kỹ năng `armorDuringCast = NONE`, trúng STAGGER | Bị ngắt | Hoàn 100% linh lực, hồi chiêu 1s |
| SK-AC-012 | Gửi `C2S_CastSkill` khi đang `STUNNED` | Server | `S2C_CastRejected { reason: STATE }` |
| SK-AC-013 | Đổi kỹ năng trong ô khi đang chiến đấu | Server | Từ chối |
| SK-AC-014 | Đổi tâm pháp khi không đả tọa | Server | Từ chối; khi đả tọa thì được; đổi lần nữa trong 30 phút bị từ chối |
| SK-AC-015 | Tâm pháp `TEC_LIET_HOA_KIEM`, đánh thường kiếm vào quái hệ KIM | Trúng | `relation = 1,25` (đánh thường hệ INHERIT = HOA) |
| SK-AC-016 | Có `TAL_SWORD_HEART` (`SWORD_PIERCE`), dùng `SKL_SWORD_QI_SLASH` vào 3 quái thẳng hàng | Trúng | Trúng 2 quái đầu |
| SK-AC-017 | `SKL_BURN_LIFE`, giữ phím 1s | Thi triển | Thọ −5 năm (`FORBIDDEN_ART`), có `ST_BURNING_LIFE` 20s; thả phím ở 0,6s thì không có gì xảy ra |
| SK-AC-018 | `SKL_BURN_LIFE` vừa dùng, đăng xuất 5 phút | Đăng nhập | Hồi chiêu còn ~295s |
| SK-AC-019 | `SKL_FIRE_ORB` bắn vào nhóm 3 quái đứng sát nhau | Trúng quái đầu | Nổ r 2 trúng cả 3 |
| SK-AC-020 | Mục tiêu di chuyển, người chơi RTT 160ms | Lượt trúng | Server tua lại 80ms + interp (≤ 200ms) |
| SK-AC-021 | `SKL_HEALING_RAIN` dưới chân tổ đội 3 người | 5 tick | Mỗi người hồi 5 lần; quái không được hồi |
| SK-AC-022 | Học ngọc giản kỹ năng đã biết | API | `409 ALREADY_KNOWN`, vật phẩm không bị tiêu |
| SK-AC-023 | Combo kiếm, bấm bước 2 sau khi hết `nextInputWindowSec` | Server | Thực hiện lại bước 1 |
| SK-AC-024 | Kiếp kết thúc | Kiếp mới | Không còn công pháp / thuần thục của kiếp trước (SK-R-004) |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 1 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Máy trạng thái hành động, đánh thường / đánh mạnh kiếm, 4 kỹ năng kiếm | ✅ | | | |
| Quy trình thi triển server, bù trễ, đạn mô phỏng server | ✅ | | | |
| Quạt + 6 kỹ năng quạt, 2 kỹ năng chung | | ✅ | | |
| Tâm pháp (7), đổi tâm pháp, thuần thục, Viên Mãn | | ✅ | | |
| Ô V (Trúc Cơ) | | ✅ | | |
| Bí pháp liều mạng | | ✅ | | |
| Tuyệt kỹ + năng lượng | | | ✅ | |
| Vũ khí Song đao / Quyền | | | ✅ | |
| Cầm (đàn), vai trò hỗ trợ chuyên sâu | | | | ✅ |
| Kỹ năng chế tạo (GD-08) | | | ✅ | |

---

## 17. Câu hỏi mở
- [ ] **SK-Q-1:** Có cho phép "giữ để ngắm" (hiện hình dạng rồi thả để thi triển) làm mặc định không? *Mặc định: bấm là thi triển ngay, tùy chọn giữ để ngắm.*
- [ ] **SK-Q-2:** Hồi chiêu đổi tâm pháp 30 phút có quá cứng với người muốn đổi vai trò khi vào bí cảnh tổ đội?
- [ ] **SK-Q-3:** Có cho học kỹ năng từ người chơi khác (sư phụ truyền công) thay vì chỉ ngọc giản? (liên quan sư đồ, sau launch)
- [ ] **SK-Q-4:** Thuần thục có nên chuyển một phần sang kiếp sau (vd. qua `UNL_PAST_LIFE_MEMORY`) không? *Mặc định: không (SK-R-004).*
- [ ] **SK-Q-5:** Quạt bắn đạn cho đánh thường có làm người chơi quạt quá an toàn so với kiếm? Theo dõi `damage_taken_sample` theo vũ khí.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-20 | — | Bản nháp đầu: bất biến, kiểu dữ liệu thành phần, máy trạng thái hành động, phẩm cấp (bảng `techniqueMultiplier` chuẩn), tâm pháp, ô kỹ năng, vũ khí kiếm / quạt, thuần thục, quy trình thi triển server, hình dạng, triệu hồi, tuyệt kỹ, bí pháp, hook, ngân sách sức mạnh, 7 tâm pháp, 14 kỹ năng, API, DB, nghiệm thu |
| v0.2 | 2026-10-23 | — | SK-R-075: `ENEMIES` gồm người chơi theo chế độ PK (GD-32) |

