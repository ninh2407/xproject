# GD-06 — Chỉ số & Công thức chiến đấu

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-06 · Prefix ID: `SF` |
| Owner | Game Designer + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 1 (công thức sát thương, poise, thể lực), Phase 2 (bảng chỉ số đầy đủ, status) |
| Liên quan | [PLAN §4.9](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-07 Kỹ năng](13-skills.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-27 Thiên phú](04-talents.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-22 Cơ duyên](23-encounters.md) · GD-09 Trang bị · GD-12 Quái |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Tham số nằm ở `shared/gamedata/stats/*.json` (§7). **Toàn bộ công thức trong tài liệu này là code dùng chung client/server** (client dùng để dự đoán và hiển thị, server là nguồn sự thật).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-06 | Mục |
|--------------|-----------------|-----|
| GD-04 | Chỉ số cơ bản theo `RealmCode` (`base_stats.json`) | §5.2 |
| GD-27 | Danh sách `CombatStat` chuẩn; thứ tự gộp hiệu ứng; quy đổi chỉ số bẩm sinh; công thức chiến lực (để chấm điểm thiên phú, GD-27 §8) | §3, §5.3–5.4, §6.8 |
| GD-07 | Công thức sát thương / hồi máu, ngũ hành, poise, phản ứng trúng đòn, thể lực, linh lực, năng lượng tuyệt kỹ, khung status | §5.5–5.11 |
| GD-26 / GD-04 / GD-22 | Định nghĩa chuẩn các status `ST_WEAKENED`, `ST_LAST_LIGHT`, `ST_HEART_DEMON`, `ST_CHILLED`... | §5.12 |
| GD-12 | Hệ số quái theo hạng | §5.13 |

---

## 1. Mục tiêu

**Trụ cột:** P3 *Chiến đấu hành động có kỹ năng*.

- Kỹ năng người chơi (né, đỡ, ngắm) quan trọng ngang chỉ số: chênh lệch chỉ số vừa phải phải **thắng được bằng kỹ năng**.
- Công thức **đơn giản, dự đoán được, hiển thị được**: mọi con số trên bảng nhân vật đều giải thích được nguồn gốc.
- Không có cơ chế né / trượt ngẫu nhiên: trúng hay không quyết định bởi **hình học và i-frame**, không bởi xúc xắc.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="sf-r-001"></a>**SF-R-001** | Mọi chỉ số cuối tính bằng **một hàm duy nhất** `computeStats(snapshot)` (§6.1), dùng chung client / server, thuần (không I/O, không ngẫu nhiên). |
| <a id="sf-r-002"></a>**SF-R-002** | Sát thương / hồi máu cuối do **Game Server** tính; client chỉ hiển thị dự đoán. Yếu tố ngẫu nhiên duy nhất là chí mạng và dao động ±5%, lấy từ RNG server. |
| <a id="sf-r-003"></a>**SF-R-003** | Mọi hệ số phần trăm cùng loại **cộng với nhau** (additive), không nhân chồng, trừ các hệ số được liệt kê rõ là nhân độc lập trong §6.3. |
| <a id="sf-r-004"></a>**SF-R-004** | Mọi chỉ số có trần / sàn (§5.14) được chặn **sau** khi gộp; bảng nhân vật hiển thị cả giá trị trước chặn. |
| <a id="sf-r-005"></a>**SF-R-005** | Không có tỷ lệ trượt / né ngẫu nhiên. Đòn đánh trúng khi hình va chạm giao nhau và mục tiêu không có i-frame / bất khả xâm phạm. |

---

## 2. Tổng quan

```
 base_stats[RealmCode] (§5.2)
   + quy đổi chỉ số bẩm sinh (§5.3)          GD-27
   + Σ FLAT (trang bị GD-09, thiên phú GD-27, tâm pháp GD-07, status)
   × (1 + Σ PCT)                              cùng các nguồn
   → chặn trần/sàn (§5.14)                    = CHỈ SỐ CUỐI (bảng nhân vật)

 Một đòn đánh (Game Server):
   va chạm 2D hợp lệ → sát thương thô (hệ số kỹ năng × công/pháp công)
   × (1 + tăng sát thương) × (1 − giảm thương phòng ngự) × ngũ hành × (1 − kháng hệ)
   × chí mạng × dao động → khiên → HP → poise → phản ứng trúng đòn → status → năng lượng
```

---

## 3. Danh sách chỉ số chuẩn

```ts
type CombatStat =
  // Tài nguyên
  | 'maxHp' | 'maxMp' | 'maxStamina' | 'hpRegen' | 'mpRegen' | 'staminaRegen'
  // Tấn công
  | 'attack' | 'spellPower' | 'critRate' | 'critDamage' | 'damageBonus' | 'healingBonus'
  | 'cooldownReduction' | 'lifesteal' | 'poiseDamage'
  // Phòng thủ
  | 'defense' | 'spellResist' | 'maxPoise' | 'poiseDamageTaken' | 'damageTaken' | 'poisonResist'
  // Di chuyển
  | 'moveSpeed'
  // Theo hệ (8 hệ, xem Glossary `Element`)
  | `elementDamage.${Element}` | `elementResist.${Element}`;

type StatModifier = { stat: CombatStat | InnateStatKey; kind: 'FLAT' | 'PCT'; value: number;
                      source: StatSource; sourceId: string; when?: EffectCondition };
type StatSource = 'BASE' | 'INNATE' | 'EQUIPMENT' | 'TALENT' | 'TECHNIQUE' | 'STATUS' | 'SET_BONUS' | 'GM';

interface StatSheet {                 // kết quả computeStats
  version: number;                    // tăng mỗi khi đầu vào đổi
  final: Record<CombatStat, number>;  // sau chặn
  uncapped: Partial<Record<CombatStat, number>>;
  breakdown: Record<CombatStat, { base: number; flat: number; pct: number; capped: boolean;
                                  contributions: { source: StatSource; sourceId: string; kind: 'FLAT' | 'PCT'; value: number }[] }>;
  combatPower: number;                // §6.8
}
```

| Chỉ số | Kiểu | Mặc định cơ bản | Ý nghĩa |
|--------|------|:---:|---------|
| `maxHp` | số | bảng §5.2 | Khí huyết |
| `maxMp` | số | bảng §5.2 | Linh lực, dùng cho kỹ năng |
| `maxStamina` | số | 100 | Thể lực: né, đỡ, đánh mạnh |
| `hpRegen` | %/s của `maxHp` | 0 trong chiến đấu · 2 ngoài chiến đấu | Hồi máu |
| `mpRegen` | %/s của `maxMp` | 1 trong · 3 ngoài chiến đấu | Hồi linh lực |
| `staminaRegen` | điểm/s | 25 | Hồi thể lực (sau 0,8s không tiêu) |
| `attack` | số | bảng §5.2 | Công kích (vật lý) |
| `spellPower` | số | bảng §5.2 | Pháp công |
| `critRate` | % | 5 | Tỷ lệ chí mạng |
| `critDamage` | % | 150 | Hệ số chí mạng |
| `damageBonus` | % | 0 | Tăng mọi sát thương gây ra |
| `healingBonus` | % | 0 | Tăng hồi máu gây ra |
| `cooldownReduction` | % | 0 | Giảm hồi chiêu |
| `lifesteal` | % | 0 | Hút máu theo sát thương gây ra |
| `poiseDamage` | % | 0 | Tăng sát thương lên poise |
| `defense` | số | bảng §5.2 | Phòng ngự (giảm sát thương vật lý) |
| `spellResist` | số | bảng §5.2 | Pháp kháng (giảm sát thương pháp thuật) |
| `maxPoise` | số | người chơi 100 · quái §5.13 | Thanh kiên định |
| `poiseDamageTaken` | % | 0 | Tăng/giảm poise mất khi trúng đòn |
| `damageTaken` | % | 0 | Tăng/giảm mọi sát thương nhận |
| `poisonResist` | % | 0 | Giảm sát thương và thời gian độc |
| `moveSpeed` | m/s | 6,0 | Tốc độ chạy |
| `elementDamage.X` | % | 0 | Tăng sát thương hệ X |
| `elementResist.X` | % | 0 | Kháng hệ X |

Danh sách tạm `CombatStat` trong GD-27 §3 được thay bằng bảng này (GD-27 dùng tập con).

---

## 4. (Không có máy trạng thái riêng)
Trạng thái chiến đấu của thực thể (đang đánh, đang né, choáng...) định nghĩa ở GD-07 §4.

---

## 5. Quy tắc

### 5.1. Trạng thái chiến đấu
- <a id="sf-r-010"></a>**SF-R-010** Thực thể **đang chiến đấu** khi gây hoặc nhận sát thương trong `params.combatTimeoutSec` (5s) gần nhất. Dùng cho: hồi máu / linh lực, chuyển bản đồ (GD-21), đả tọa (GD-04), đổi kỹ năng (GD-07).

### 5.2. Chỉ số cơ bản theo cảnh giới
- <a id="sf-r-020"></a>**SF-R-020** Chỉ số cơ bản của người chơi theo `RealmCode`, sinh từ công thức §6.2 và lưu sẵn trong `base_stats.json` (bảng đã làm tròn là nguồn chuẩn; script sinh bảng nằm ở `tools/gen-base-stats`).

Mốc kiểm tra (làm tròn nửa lên):

| RealmCode | maxHp | maxMp | attack | spellPower | defense | spellResist |
|-----------|---:|---:|---:|---:|---:|---:|
| PN1 | 120 | 60 | 12 | 12 | 6 | 6 |
| LK1 | 200 | 100 | 20 | 20 | 10 | 10 |
| LK5 | 293 | 146 | 29 | 29 | 15 | 15 |
| LK9 | 429 | 214 | 43 | 43 | 21 | 21 |
| TC1 | 755 | 377 | 75 | 75 | 38 | 38 |
| TC4 | 1.060 | 530 | 106 | 106 | 53 | 53 |
| KD1 | 2.137 | 1.069 | 214 | 214 | 107 | 107 |
| KD4 | 3.166 | 1.583 | 317 | 317 | 158 | 158 |
| NA1 *(sau launch)* | 7.219 | 3.610 | 722 | 722 | 361 | 361 |
| HT1 *(sau launch)* | 28.756 | 14.378 | 2.876 | 2.876 | 1.438 | 1.438 |

- <a id="sf-r-021"></a>**SF-R-021** `maxStamina`, `moveSpeed`, `critRate`, `critDamage`, `maxPoise` (người chơi) **không** tăng theo cảnh giới; chỉ thay đổi qua trang bị / thiên phú / tâm pháp / status.

### 5.3. Quy đổi chỉ số bẩm sinh (GD-27)
- <a id="sf-r-030"></a>**SF-R-030** Chỉ số bẩm sinh cuối (sau thiên phú) quy đổi thành `PCT` với `source = INNATE`:

| Bẩm sinh | Chỉ số chiến đấu | Quy đổi | Giới hạn |
|----------|------------------|---------|----------|
| `constitution` | `maxHp` | +1% mỗi điểm trên / dưới 50 | ±40% |
| `constitution` | `defense` | +0,5% mỗi điểm | ±20% |
| `spiritSense` | `maxMp` | +1% mỗi điểm | ±40% |
| `spiritSense` | `spellResist` | +0,5% mỗi điểm | ±20% |
| `luck` | `critRate` | +0,05 điểm % mỗi điểm | ±2,5 điểm % |
| `comprehension` | — | Không ảnh hưởng chỉ số chiến đấu (ảnh hưởng thuần thục, GD-07) | |

Ví dụ: căn cốt 70 → `maxHp` +20%, `defense` +10%.

### 5.4. Gộp hiệu ứng
- <a id="sf-r-040"></a>**SF-R-040** `final = clamp((base + Σ FLAT) × (1 + Σ PCT))` theo SF-R-003, với mọi nguồn trong `StatSource`. Hiệu ứng có `when` (điều kiện thời gian thực) chỉ cộng khi thỏa; Game Server đánh giá lại khi điều kiện đổi (ngày/đêm, thời tiết, máu, có tổ đội...).
- <a id="sf-r-041"></a>**SF-R-041** Thay đổi đầu vào (trang bị, thiên phú, cảnh giới, tâm pháp, status) tăng `StatSheet.version` và gửi `S2C_StatsUpdate`. Tài nguyên hiện tại (HP/MP) giữ **tỷ lệ %** khi `maxHp`/`maxMp` đổi (trừ khi hồi đầy theo quy tắc khác, vd. đột phá).

### 5.5. Sát thương
- <a id="sf-r-050"></a>**SF-R-050** Mỗi lần trúng đòn có `damageType`: `PHYSICAL` (dùng `attack`, bị giảm bởi `defense`) hoặc `SPELL` (dùng `spellPower`, giảm bởi `spellResist`), và `element` (một trong 8 hệ hoặc `NONE`). Công thức §6.3.
- <a id="sf-r-051"></a>**SF-R-051** **Giảm thương phòng ngự** tính theo cảnh giới **người tấn công** (§6.4), nên phòng ngự không vô hiệu hóa được kẻ địch cảnh giới cao hơn.
- <a id="sf-r-052"></a>**SF-R-052** Sát thương cuối làm tròn xuống, tối thiểu 1 (trừ khi bất khả xâm phạm / đỡ hoàn hảo).
- <a id="sf-r-053"></a>**SF-R-053** **Khiên** (shield, từ kỹ năng / pháp bảo) hấp thụ sát thương **trước** HP; nhiều khiên thì khiên hết hạn sớm nhất bị trừ trước.
- <a id="sf-r-054"></a>**SF-R-054** **Hút máu:** hồi `lifesteal% × sát thương cuối gây ra` (sau khiên của mục tiêu); không áp dụng với sát thương theo thời gian (DoT).

### 5.6. Ngũ hành tương khắc
- <a id="sf-r-060"></a>**SF-R-060** Mỗi thực thể có `elementAffinity`: quái theo dữ liệu (GD-12); người chơi theo **hệ của tâm pháp chính** (GD-07), `NONE` nếu tâm pháp vô hệ.
- <a id="sf-r-061"></a>**SF-R-061** Quan hệ giữa hệ đòn đánh và `elementAffinity` mục tiêu (`element_relations.json`):

| Hệ đòn | Khắc (×1,25) | Bị khắc (×0,8) |
|--------|--------------|----------------|
| `KIM` | `MOC`, `PHONG` | `HOA` |
| `MOC` | `THO` | `KIM` |
| `THO` | `THUY`, `LOI` | `MOC` |
| `THUY` | `HOA` | `THO`, `LOI` |
| `HOA` | `KIM`, `BANG` | `THUY` |
| `LOI` | `THUY` | `THO`, `PHONG` |
| `BANG` | `PHONG` | `HOA` |
| `PHONG` | `LOI` | `KIM`, `BANG` |

Cột "Bị khắc" là chiều ngược của cột "Khắc" (A khắc B ⇔ đòn hệ B đánh vào mục tiêu hệ A bị ×0,8). `NONE` không khắc và không bị khắc.

- <a id="sf-r-062"></a>**SF-R-062** **Hiệu ứng hệ:** đòn có `element ≠ NONE` có xác suất gắn status hệ (§5.12) theo dữ liệu kỹ năng (GD-07); không có xác suất mặc định ngầm.

### 5.7. Chí mạng
- <a id="sf-r-070"></a>**SF-R-070** Mỗi lần trúng đòn trực tiếp roll chí mạng với `critRate` (sau chặn 60%); DoT và hồi máu theo thời gian không chí mạng. Hồi máu trực tiếp **có** chí mạng.

### 5.8. Hồi máu
- <a id="sf-r-080"></a>**SF-R-080** `heal = coefficient × spellPower × (1 + healingBonus)` (× chí mạng); không vượt `maxHp`; ghi nhận hồi máu thừa cho telemetry.

### 5.9. Poise (kiên định) và phản ứng trúng đòn
- <a id="sf-r-090"></a>**SF-R-090** Mỗi đòn có `poiseDamage` (dữ liệu kỹ năng). Poise mất = `poiseDamage × (1 + attacker.poiseDamage) × (1 + target.poiseDamageTaken)`.
- <a id="sf-r-091"></a>**SF-R-091** Poise về 0 → **Phá thế** (`POISE_BREAK`): choáng `params.poiseBreakStunSec` (người chơi 1,0s · quái thường 1,5s · tinh anh 2,0s · boss 3,0s); trong thời gian phá thế mục tiêu nhận +20% sát thương; hết phá thế thì poise hồi đầy.
- <a id="sf-r-092"></a>**SF-R-092** Poise hồi `params.poiseRegenPctPerSec` (20% `maxPoise`/s) sau `params.poiseRegenDelaySec` (3s) không mất poise.
- <a id="sf-r-093"></a>**SF-R-093** **Phản ứng trúng đòn** (`HitReaction`, theo dữ liệu kỹ năng): `NONE` < `FLINCH` (giật nhẹ, không ngắt hành động) < `STAGGER` (ngắt hành động 0,4s) < `KNOCKBACK` (đẩy lùi `distance` m, ngắt 0,5s) < `KNOCKDOWN` (ngã 1,2s).
- <a id="sf-r-094"></a>**SF-R-094** **Giáp hành động** (`armorLevel`, theo trạng thái hiện tại của mục tiêu, GD-07): `NONE` · `LIGHT` (bỏ qua `FLINCH`) · `HEAVY` (bỏ qua tới `KNOCKBACK`) · `IMMUNE`. Phản ứng chỉ áp dụng nếu **cao hơn** giáp hành động; phá thế luôn bỏ qua giáp (trừ `IMMUNE`).
- <a id="sf-r-095"></a>**SF-R-095** Sau `KNOCKDOWN`, mục tiêu miễn `KNOCKDOWN` và `KNOCKBACK` trong `params.knockdownImmunitySec` (2s), chống khóa liên hoàn.

### 5.10. Thể lực, né, đỡ
- <a id="sf-r-100"></a>**SF-R-100** Thể lực: tối đa `maxStamina` (100); hồi `staminaRegen`/s sau `params.staminaRegenDelaySec` (0,8s) không tiêu. Không đủ thể lực thì không thực hiện được hành động tiêu thể lực.

| Hành động | Tiêu thể lực | Ghi chú |
|-----------|:---:|---------|
| Né lướt (Space) | 25 | Lướt 4m, i-frame 0,25s, hồi 0,3s; Trúc Cơ+ được né lần 2 trong 0,6s (khinh công, GD-04) |
| Đỡ (giữ Shift) | 15 mỗi lần chặn đòn | Giảm 50% sát thương trực diện |
| Đỡ hoàn hảo | 0 | Bấm Shift trong `params.perfectParryWindowSec` (0,2s) trước khi đòn chạm |
| Đánh mạnh / tụ lực | 15–30 | Theo vũ khí (GD-07) |

- <a id="sf-r-101"></a>**SF-R-101** **Đỡ hoàn hảo:** không nhận sát thương; kẻ tấn công cận chiến mất 30% `maxPoise` và bị `STAGGER`; nhận +15 năng lượng tuyệt kỹ. Đạn bay bị chặn (không phản lại).
- <a id="sf-r-102"></a>**SF-R-102** Đòn **không thể đỡ** (`unblockable = true`, viền đỏ) bỏ qua đỡ / đỡ hoàn hảo; chỉ né được.
- <a id="sf-r-103"></a>**SF-R-103** Server phán định cửa sổ đỡ / i-frame theo thời điểm server nhận lệnh, có bù trễ: tối đa `params.defenseLagCompMs` (100ms) lùi về theo RTT (chi tiết TD-02). Quy tắc cụ thể (thời điểm hiệu lực, xét lại lượt trúng, hoãn xác nhận hấp hối): [TD-02 §7.2](../tech/01-network.md).

### 5.11. Linh lực và năng lượng tuyệt kỹ
- <a id="sf-r-110"></a>**SF-R-110** Kỹ năng tiêu linh lực theo dữ liệu (GD-07); không đủ thì không thi triển.
- <a id="sf-r-111"></a>**SF-R-111** Năng lượng tuyệt kỹ (0–100), chỉ có từ Kim Đan (GD-04): +`energyGain` của kỹ năng khi trúng, +3 mỗi lần nhận sát thương ≥ 5% `maxHp`, +15 đỡ hoàn hảo. Giảm 5/s sau 10s ngoài chiến đấu.

### 5.12. Status (buff / debuff / khống chế)
- <a id="sf-r-120"></a>**SF-R-120** Mọi status định nghĩa trong `statuses.json`, ID dạng `ST_*`. **Tên trong các tài liệu cũ là bí danh của ID chuẩn:** `WEAKENED` = `ST_WEAKENED`, `HEART_DEMON` = `ST_HEART_DEMON`, `LAST_LIGHT` (status) = `ST_LAST_LIGHT`, `GREAT_LIMIT` = `ST_GREAT_LIMIT`. Code chỉ dùng ID chuẩn.

```ts
interface StatusDef {
  id: string;                                   // ST_*
  kind: 'BUFF' | 'DEBUFF' | 'CC' | 'MARKER';
  durationSec: number | null;                   // null = tới khi gỡ
  stacking: 'REFRESH' | 'STACK' | 'IGNORE';     // áp lại khi đang có
  maxStacks: number;
  modifiers: { stat: CombatStat; kind: 'FLAT' | 'PCT'; valuePerStack: number }[];
  tick?: { intervalSec: number; damageType: 'PHYSICAL' | 'SPELL' | 'TRUE'; element: Element | 'NONE';
           coefficientPerStack: number; scalingStat: 'attack' | 'spellPower' | 'maxHpOfTarget' };
  cc?: 'STUN' | 'ROOT' | 'SILENCE';             // SLOW dùng modifier moveSpeed âm
  onMaxStacks?: { applyStatusId: string; clearSelf: boolean };
  dispellable: boolean;
  persistOffline: boolean;                      // true: tiếp tục đếm khi offline (vd. ST_WEAKENED)
}
```

- <a id="sf-r-121"></a>**SF-R-121** DoT tính theo chỉ số **của người gây ra lúc áp status** (chụp nhanh), không chí mạng, không hút máu, bỏ qua phòng ngự nếu `damageType = TRUE`.
- <a id="sf-r-122"></a>**SF-R-122** Boss miễn `STUN`/`ROOT`/`SILENCE` trừ khi đang phá thế; tinh anh chịu thời lượng khống chế ×0,5.
- <a id="sf-r-123"></a>**SF-R-123** Danh mục status lõi MVP:

| ID | Loại | Thời lượng | Cộng dồn | Hiệu ứng | Nguồn tham chiếu |
|----|------|:---:|:---:|----------|------------------|
| `ST_WEAKENED` | DEBUFF | theo nguồn | REFRESH | `attack`, `spellPower`, `defense`, `spellResist` −20%; `persistOffline` | GD-26 §5.6, GD-04 §5.8 |
| `ST_LAST_LIGHT` | BUFF | theo GD-26 | IGNORE | `attack`, `spellPower`, `defense`, `spellResist`, `maxHp` +20% | GD-26 LC-R-051 |
| `ST_GREAT_LIMIT` | MARKER | — | IGNORE | Không chỉ số; dùng cho điều kiện / UI | GD-26 LC-R-050 |
| `ST_HEART_DEMON` | MARKER | — | STACK (3) | Không chỉ số chiến đấu; hiệu ứng ở GD-04 CU-R-093 | GD-04 §5.9 |
| `ST_HEAVEN_PUNISHMENT` | DEBUFF (hiển thị) | tới khi hết (GD-32) | REFRESH | Bị Thiên Đạo Hóa Thân truy sát; không ảnh hưởng chỉ số | GD-32 |
| `ST_QI_UNSETTLED` | DEBUFF (hiển thị) | 120s | REFRESH | Không hái được linh thảo hiếm (GD-21 OW-R-105); không ảnh hưởng chỉ số | GD-21 |
| `ST_CHILLED` | DEBUFF | theo nguồn | REFRESH | `moveSpeed` −15% | GD-22 ví dụ |
| `ST_BURN` | DEBUFF | 3s | STACK (3), refresh thời lượng | DoT hệ HOA: 0,15 × `spellPower` mỗi giây mỗi tầng | Hiệu ứng hệ Hỏa |
| `ST_FROST` | DEBUFF | 3s | STACK (3) | `moveSpeed` −10% mỗi tầng; đủ 3 tầng → `ST_FROZEN`, xóa `ST_FROST` | Hiệu ứng hệ Thủy / Băng |
| `ST_FROZEN` | CC | 1,5s | IGNORE | `ROOT` | |
| `ST_SHOCK` | DEBUFF | 4s | STACK (3) | `damageTaken` +5% mỗi tầng | Hiệu ứng hệ Lôi |
| `ST_ARMOR_BREAK` | DEBUFF | 5s | STACK (2) | `defense` −15% mỗi tầng | Hiệu ứng hệ Kim |
| `ST_ENTANGLE` | CC | 1,0s | IGNORE | `ROOT` | Hiệu ứng hệ Mộc |
| `ST_HEAVY` | DEBUFF | 4s | REFRESH | `poiseDamageTaken` +30% | Hiệu ứng hệ Thổ |
| `ST_WIND_CUT` | DEBUFF | 4s | STACK (5) | DoT hệ PHONG: 0,05 × `attack` mỗi giây mỗi tầng | Hiệu ứng hệ Phong |
| `ST_POISON` | DEBUFF | 6s | STACK (5) | DoT `TRUE`: 0,5% `maxHp` mục tiêu mỗi giây mỗi tầng, giảm theo `poisonResist` | Quái, bẫy |
| `ST_STUN` | CC | theo nguồn | REFRESH | `STUN` | Kỹ năng |
| `ST_SILENCE` | CC | theo nguồn | REFRESH | `SILENCE` (không dùng kỹ năng, vẫn đánh thường / né) | Kỹ năng |
| `ST_INVULNERABLE` | BUFF | theo nguồn | REFRESH | Bỏ qua mọi sát thương và phản ứng | Hồi sinh, chuyển bản đồ (GD-21), Thế Thân Phù (GD-26) |
| `ST_BURNING_LIFE` | BUFF | 20s | REFRESH | `damageBonus` +50 điểm % | Bí pháp `SKL_BURN_LIFE` (GD-07) |
| `ST_NIGHT_EMPOWER` | BUFF | — (khi `NIGHT`) | IGNORE | `attack`, `spellPower`, `defense`, `spellResist` +X% với X = `nightStatBonusPct` của quái (mặc định 0,15) | Quái dạ hành (GD-12 EN-R-021) |
| `ST_DORMANT_GUARD` | BUFF | theo nguồn | REFRESH | `damageTaken` −80% (−50% khi bị giới hạn lạm dụng) | Quy tức (GD-28 DZ-R-070) |
| `ST_DZ_UNREST` | BUFF | — (pha `UNREST`) | IGNORE | `attack`, `spellPower`, `defense`, `spellResist` +15% | Quái trong hiểm địa (GD-28 §5.3) |
| `ST_DZ_STORM` | BUFF | — (pha `STORM`+) | IGNORE | Như trên +30% (thay `ST_DZ_UNREST`) | Quái trong hiểm địa |
| `ST_QI_STORM` | DEBUFF | — (pha `STORM`+) | IGNORE | DoT `TRUE` 1% `maxHp` mục tiêu mỗi 5s | Linh khí bạo loạn (GD-28) |
| `ST_COLLAPSE` | DEBUFF | — (pha `COLLAPSE`) | IGNORE | DoT `TRUE` 3% `maxHp`/s, +1%/s mỗi 10s (giá trị do nguồn cập nhật) | Hiểm địa sụp đổ (GD-28 §6.3) |
| `ST_KILLING_INTENT` | DEBUFF | — (tới khi xóa) | STACK (10) | `damageTaken` +3% mỗi tầng | Sát khí Cổ Chiến Trường (GD-28 DZ-R-100) |
| `ST_ANTI_POISON` | BUFF | 300s | REFRESH | `poisonResist` +80 điểm %; nguồn `POISON_FOG` không áp `ST_POISON` khi có status này | Tị Độc Đan (GD-10) |
| `ST_STAUNCH` | BUFF | 10s (× phẩm chất đan) | REFRESH | `hpRegen` +1 điểm %/s, có hiệu lực cả khi đang chiến đấu | Cầm Huyết Đan (GD-10) |

### 5.13. Hệ số quái (tham chiếu cho GD-12)
- <a id="sf-r-130"></a>**SF-R-130** Chỉ số quái = `base_stats[monsterRealmCode]` × hệ số hạng × hệ số riêng của loài (GD-12):

| Hạng | maxHp | attack / spellPower | defense / spellResist | maxPoise |
|------|:---:|:---:|:---:|:---:|
| `NORMAL` | ×0,6 | ×0,8 | ×0,8 | 40 |
| `ELITE` | ×3,0 | ×1,2 | ×1,2 | 150 |
| `BOSS` | ×20,0 | ×1,5 | ×1,5 | 400 |

### 5.14. Trần và sàn

| Chỉ số | Sàn | Trần |
|--------|:---:|:---:|
| `critRate` | 0% | 60% |
| `critDamage` | 100% | 300% |
| `cooldownReduction` | 0% | 40% |
| `lifesteal` | 0% | 20% |
| `damageTaken` | −50% | +100% |
| `elementResist.X` | −50% | 60% |
| `poisonResist` | 0% | 80% |
| `moveSpeed` | 3,0 m/s | 9,0 m/s |
| Giảm thương phòng ngự (§6.4) | 0 | 75% |
| `maxHp`, `maxMp`, `maxStamina`, `maxPoise` | 1 | — |

---

## 6. Công thức

### 6.1. `computeStats`
```
for each stat s:
  base  = baseStats[realmCode][s]        (người chơi)  | monster formula (§5.13)
  flat  = Σ FLAT modifiers of s (điều kiện when thỏa)
  pct   = Σ PCT  modifiers of s (gồm INNATE §5.3)
  value = (base + flat) × (1 + pct)
  final = clamp(value, floor[s], cap[s])
```
Các stat dạng % (`critRate`, `damageBonus`...) có `base` = giá trị mặc định §3 và chỉ nhận `FLAT` (đơn vị điểm %); `PCT` lên chúng bị validator cấm.

### 6.2. Bảng chỉ số cơ bản
```
value(stat, realmCode) = V_LK1[stat] × realmScale(realmCode)
value(stat, PN1)       = 0,6 × V_LK1[stat]

realmScale(LK1) = 1
realmScale(R, tầng s > 1)    = realmScale(R, s − 1) × g[R]                    (lên tầng trong cùng cảnh giới)
realmScale(R mới, tầng 1)    = realmScale(R cũ, tầng cuối) × g[R cũ] × J[R mới]   (đột phá cảnh giới lớn)

g (mỗi tầng, tăng theo cảnh giới):  LK 1,10 · TC 1,12 · KD 1,14 · NA 1,16 · HT 1,18
J (vào cảnh giới lớn, tăng dần):     TC 1,6  · KD 1,8  · NA 2,0  · HT 2,2
majorScale(R) = Π J từ TC tới R  (TC 1,6 · KD 2,88 · NA 5,76 · HT 12,672)   — dùng cho kinh tế (GD-13)
V_LK1: maxHp 200 · maxMp 100 · attack 20 · spellPower 20 · defense 10 · spellResist 10
làm tròn nửa lên → base_stats.json
```
Ví dụ `TC1`: `200 × 1,1^8 (LK9) × 1,1 × 1,6 = 754,5 → 755` (không đổi so với v0.1). `KD1`: `realmScale(TC4) = 3,7727 × 1,12³ = 5,3004` → `× 1,12 × 1,8 = 10,6856` → `200 × 10,6856 = 2.137`.

**Khoảng cách giữa các cảnh giới (SF-Q-1, tầng đầu so với tầng đầu cảnh giới trước):**

| Đột phá | Tỷ lệ chỉ số | Trong cảnh giới (tầng cuối / tầng đầu) |
|---------|:---:|:---:|
| LK1 → TC1 | ×3,77 | LK ×2,14 (9 tầng) |
| TC1 → KD1 | ×2,83 *(v0.1: ×2,34)* | TC ×1,40 |
| KD1 → NA1 | ×3,38 | KD ×1,48 |
| NA1 → HT1 | ×3,98 | NA ×1,56 · HT ×1,64 |

Cảm nhận trong chiến đấu (bảng cơ bản, cùng hệ số kỹ năng, giảm thương §6.4): `KD1` đánh `TC4` bị giảm thương 9,0%, `TC4` đánh `KD1` bị giảm 28,8% → thời gian `TC4` cần để hạ `KD1` gấp **≈ 5,2×** thời gian `KD1` hạ `TC4` (v0.1: ≈ 3,8×). Vượt cấp ở cảnh giới cao gần như không thể; ở LK vẫn có thể bù bằng kỹ năng và trang bị.

### 6.3. Sát thương
```
raw        = coefficient × (damageType == PHYSICAL ? attack : spellPower) + flatDamage
bonus      = 1 + attacker.damageBonus + attacker.elementDamage[element]          (cộng, SF-R-003)
mitigation = §6.4 (theo defense hoặc spellResist của mục tiêu)
relation   = 1,25 | 0,8 | 1,0   (§5.6)
resist     = 1 − target.elementResist[element]
taken      = 1 + target.damageTaken
crit       = roll < critRate ? critDamage : 1
variance   = U(0,95; 1,05)  (RNG server)
special    = phá thế ×1,2 · đỡ ×0,5 · đỡ hoàn hảo ×0
damage     = max(1, floor(raw × bonus × (1 − mitigation) × relation × resist × taken × crit × variance × special))
```
Các thừa số `relation`, `resist`, `taken`, `crit`, `variance`, `special` là **nhân độc lập** (ngoại lệ của SF-R-003).

Ví dụ (khử ngẫu nhiên: `crit = 1`, `variance = 1`): người tấn công `LK5`, `attack` 50, kỹ năng hệ HOA vật lý `coefficient` 1,8, `flatDamage` 10, `damageBonus` +20%; mục tiêu `defense` 30, `elementAffinity = KIM`, kháng hệ 0:
- `raw = 1,8 × 50 + 10 = 100`; `bonus = 1,2`
- `mitigation = 30 / (30 + 5 × 15) = 0,2857` (15 = `defense` cơ bản của `LK5`)
- `relation = 1,25` (Hỏa khắc Kim)
- `damage = floor(100 × 1,2 × 0,7143 × 1,25) = floor(107,14) = 107`

### 6.4. Giảm thương phòng ngự
```
mitigation = min(0,75, D / (D + K))
D = target.defense (PHYSICAL) | target.spellResist (SPELL)
K = params.mitigationK (5) × baseStats[attacker.realmCode].defense (hoặc .spellResist)
```
Phòng ngự bằng mức cơ bản của người tấn công → giảm 16,7%; gấp 3 lần → 37,5%.

### 6.5. Hồi tài nguyên
```
hpPerSec      = maxHp × hpRegen%        (hpRegen = 0 khi đang chiến đấu, trừ nguồn FLAT từ kỹ năng / tâm pháp)
mpPerSec      = maxMp × mpRegen%
staminaPerSec = staminaRegen            (sau 0,8s không tiêu)
```

### 6.6. Sát thương theo thời gian (DoT)
```
tickDamage = coefficientPerStack × stacks × snapshot(scalingStat) × (1 − mitigation nếu không TRUE) × relation × resist × taken
ST_POISON: tickDamage = 0,005 × stacks × target.maxHp × (1 − poisonResist)
```

### 6.7. Thời gian hiệu lực khống chế
```
ccDuration = baseDuration × (rank == ELITE ? 0,5 : 1) ;  BOSS: 0 nếu không phá thế
```

### 6.8. Chiến lực (combat power)
Chỉ để hiển thị, so sánh, và chấm điểm sức mạnh thiên phú / trang bị. **Không** dùng trong công thức sát thương. Dùng cho **Thiên Bảng Chiến Lực** (GD-14 SO-R-04I): Game Server tính và báo về API, không nhận từ client.
```
mainPower = max(attack, spellPower)
DPS_idx   = mainPower × (1 + critRate × (critDamage − 1)) × (1 + damageBonus)
EHP_idx   = maxHp / ((1 − m_def) × 0,5 + (1 − m_res) × 0,5)
            với m_def, m_res = mitigation §6.4 khi người tấn công cùng RealmCode
combatPower = round(10 × sqrt(DPS_idx × EHP_idx))
```
**Điểm sức mạnh của một hiệu ứng** (dùng cho GD-27 §8, GD-09): `ΔCP% = combatPower(nhân vật tham chiếu + hiệu ứng) / combatPower(nhân vật tham chiếu) − 1`. Nhân vật tham chiếu: `LK5`, chỉ số bẩm sinh 50 đều, **trang bị chuẩn** ([GD-09 IT-R-031](15-items-equipment.md#it-r-031): 8 ô phẩm Phàm đúng cảnh giới, ≈ +30% công / máu / phòng), không thiên phú.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/stats/params.json` | Tham số |
| `shared/gamedata/stats/base_stats.json` | Bảng `RealmCode → chỉ số cơ bản` (sinh bởi `tools/gen-base-stats` từ §6.2) |
| `shared/gamedata/stats/stat_defs.json` | Mặc định, đơn vị, trần / sàn mỗi `CombatStat` (§3, §5.14) |
| `shared/gamedata/stats/innate_conversion.json` | Bảng §5.3 |
| `shared/gamedata/stats/element_relations.json` | Bảng §5.6 |
| `shared/gamedata/stats/statuses.json` | Mảng `StatusDef` (§5.12) |
| `shared/gamedata/stats/monster_rank.json` | Bảng §5.13 |

```jsonc
// params.json
{
  "combatTimeoutSec": 5,
  "baseGrowthPerStage": 1.10, "baseGrowthPerRealm": 1.60, "phamNhanFactor": 0.6,
  "mitigationK": 5, "mitigationCap": 0.75,
  "elementStrongMult": 1.25, "elementWeakMult": 0.8,
  "varianceMin": 0.95, "varianceMax": 1.05,
  "poiseBreakStunSec": { "PLAYER": 1.0, "NORMAL": 1.5, "ELITE": 2.0, "BOSS": 3.0 },
  "poiseBreakDamageTakenBonus": 0.20, "poiseRegenPctPerSec": 0.20, "poiseRegenDelaySec": 3,
  "knockdownImmunitySec": 2,
  "staminaRegenDelaySec": 0.8,
  "dodge": { "staminaCost": 25, "distance": 4, "iframeSec": 0.25, "cooldownSec": 0.3, "doubleWindowSec": 0.6 },
  "block": { "staminaCostPerHit": 15, "damageMult": 0.5 },
  "perfectParryWindowSec": 0.2, "perfectParryPoisePct": 0.30, "perfectParryEnergy": 15,
  "defenseLagCompMs": 100,            // bản sao tham khảo — nguồn duy nhất: world/network.json (TD-02 §13)
  "energy": { "max": 100, "onBigHitTaken": 3, "bigHitPctMaxHp": 0.05, "decayPerSec": 5, "decayDelaySec": 10 },
  "eliteCcMult": 0.5
}
```

```jsonc
// statuses.json (trích)
[
  { "id": "ST_WEAKENED", "kind": "DEBUFF", "durationSec": null, "stacking": "REFRESH", "maxStacks": 1,
    "modifiers": [ { "stat": "attack", "kind": "PCT", "valuePerStack": -0.2 },
                   { "stat": "spellPower", "kind": "PCT", "valuePerStack": -0.2 },
                   { "stat": "defense", "kind": "PCT", "valuePerStack": -0.2 },
                   { "stat": "spellResist", "kind": "PCT", "valuePerStack": -0.2 } ],
    "dispellable": false, "persistOffline": true },
  { "id": "ST_BURN", "kind": "DEBUFF", "durationSec": 3, "stacking": "STACK", "maxStacks": 3, "modifiers": [],
    "tick": { "intervalSec": 1, "damageType": "SPELL", "element": "HOA", "coefficientPerStack": 0.15, "scalingStat": "spellPower" },
    "dispellable": true, "persistOffline": false },
  { "id": "ST_FROST", "kind": "DEBUFF", "durationSec": 3, "stacking": "STACK", "maxStacks": 3,
    "modifiers": [ { "stat": "moveSpeed", "kind": "PCT", "valuePerStack": -0.10 } ],
    "onMaxStacks": { "applyStatusId": "ST_FROZEN", "clearSelf": true },
    "dispellable": true, "persistOffline": false }
]
```

`durationSec: null` với `ST_WEAKENED` vì thời lượng do nguồn áp truyền vào (GD-26: 600s / 1800s; GD-04: theo bảng thất bại).

**Validator (CI):** `base_stats.json` khớp công thức §6.2 (sai lệch ≤ 1 do làm tròn); mọi `StatusDef.modifiers[].stat` thuộc `CombatStat`; không có `PCT` lên stat dạng % (§6.1); `onMaxStacks.applyStatusId` tồn tại; bảng ngũ hành đối xứng (A khắc B ⇔ B bị A khắc).

---

## 8. Lưu ý cân bằng (mục tiêu ban đầu)

| Chỉ tiêu | Mục tiêu | Kiểm chứng |
|----------|----------|------------|
| Thời gian hạ quái thường cùng cảnh giới | 3–6 đòn (2–4 giây) | Balance Simulator + playtest |
| Thời gian hạ tinh anh | 20–40 giây solo | |
| Boss bí cảnh (3 người) | 2–4 phút | |
| Quái thường cùng cảnh giới đánh trúng người chơi | 5–8% `maxHp` mỗi đòn | |
| Chênh 1 tầng nhỏ | Thắng được bằng kỹ năng | Playtest |
| Chênh 1 cảnh giới lớn | Gần như không thể thắng | |

---

## 9. Hợp đồng giao tiếp

### 9.1. API nội bộ

| Endpoint | Request | Response | Ghi chú |
|----------|---------|----------|---------|
| `GET /internal/v1/characters/{id}/stat-inputs` | — | `{ realmCode, innateStats, modifiers: StatModifier[], elementAffinity, version }` | Game Server gọi khi vào bản đồ và khi nhận `stats.inputs_changed`; tự chạy `computeStats` |

### 9.2. API công khai

| Endpoint | Mục đích |
|----------|----------|
| `GET /v1/characters/{id}/stats` | `StatSheet` đầy đủ kèm `breakdown` (tooltip bảng nhân vật) |

### 9.3. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `S2C_StatsUpdate` | S→C | `{ version, final: Partial<Record<CombatStat, number>> }` (chỉ trường đổi) |
| `S2C_ResourceUpdate` | S→C | `{ entityId, hp, maxHp, mp?, stamina?, poise?, energy?, shields? }` |
| `S2C_HitResults` | S→C | `{ events: [{ attackerId, targetId, skillRef, damage, crit, relation: 'STRONG' \| 'WEAK' \| 'NEUTRAL', blocked: 'NONE' \| 'BLOCK' \| 'PERFECT', reaction, poiseBroken, statusesApplied: string[], killed }] }` |
| `S2C_StatusUpdate` | S→C | `{ entityId, statuses: [{ id, stacks, remainingSec }] }` |

### 9.4. Sự kiện domain

| Sự kiện | Payload | Phát bởi |
|---------|---------|----------|
| `stats.inputs_changed` | `{ characterId, reason: 'REALM' \| 'TALENT' \| 'EQUIPMENT' \| 'TECHNIQUE' \| 'STATUS_PERSISTENT' }` | API Service khi các hệ thống khác thay đổi đầu vào |

---

## 10. Lưu trữ
Không có bảng riêng: chỉ số cuối **không lưu**, luôn tính lại. Status bền vững (`persistOffline = true`) lưu ở:

```sql
CREATE TABLE character_status (
  character_id UUID NOT NULL REFERENCES character(id),
  status_id    VARCHAR(48) NOT NULL,
  stacks       INT NOT NULL DEFAULT 1,
  expires_at   TIMESTAMPTZ,                 -- null = tới khi gỡ
  source_ref   VARCHAR(80),
  PRIMARY KEY (character_id, status_id)
);
```
Status ngắn (chiến đấu) chỉ sống trong RAM của `MapInstance`.

---

## 11. UI/UX
- **Bảng nhân vật → Chỉ số:** nhóm Tài nguyên / Tấn công / Phòng thủ / Hệ; rê chuột hiện `breakdown` (cơ bản, từng nguồn cộng / nhân, bị chặn hay không); chiến lực ở đầu bảng.
- **Số sát thương:** trắng thường, vàng to chí mạng, viền xanh lá "Khắc" / xám "Bị khắc", chữ "Phá thế" khi poise vỡ, "Hoàn hảo" khi đỡ hoàn hảo.
- **Thanh poise** chỉ hiện với tinh anh / boss (dưới thanh máu); người chơi thấy poise của mình qua viền nhân vật khi < 30%.
- **Thanh thể lực** quanh con trỏ hoặc dưới nhân vật (tùy chọn), mờ đi khi đầy.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-07 Kỹ năng | Dùng §6.3–6.7; cung cấp `coefficient`, `damageType`, `element`, `poiseDamage`, `HitReaction`, `armorLevel`, status áp lên |
| GD-04 Tu luyện | `RealmCode` → `base_stats`; khinh công né 2 lần từ Trúc Cơ |
| GD-27 Thiên phú | Hiệu ứng `STAT_FLAT`/`STAT_PCT`; quy đổi bẩm sinh §5.3; chấm điểm thiên phú bằng §6.8 |
| GD-26 Vòng đời | `ST_WEAKENED`, `ST_LAST_LIGHT`, `ST_GREAT_LIMIT`; HP về 0 → trọng thương / chết |
| GD-09 Trang bị | `FLAT`/`PCT` từ trang bị, hiệu ứng bộ |
| GD-12 Quái | Hệ số hạng §5.13, `elementAffinity` |
| GD-21 Open World | `ST_INVULNERABLE` khi vào bản đồ; trạng thái chiến đấu chặn chuyển bản đồ |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Hai đòn trúng cùng tick server | Xử lý theo thứ tự nhận lệnh; nếu đòn đầu giết mục tiêu, đòn sau bỏ qua |
| `maxHp` giảm khi đang ở 100% | Giữ tỷ lệ (SF-R-041) → vẫn 100% |
| Status `REFRESH` áp với thời lượng ngắn hơn phần còn lại | Giữ thời lượng dài hơn |
| Đỡ đúng lúc nhưng gói tin tới trễ 150ms | Chỉ bù tối đa 100ms (SF-R-103); vượt thì tính là trúng |
| Mục tiêu đang `ST_INVULNERABLE` | Không sát thương, không phản ứng, không status, vẫn hiện số "Miễn" |
| Tổng `PCT` âm dưới −100% | `value` bị chặn bởi sàn của stat (§5.14), không âm |

---

## 14. Telemetry

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `combat_ttk` | attackerRealm, targetId, rank, seconds, hits | Thời gian hạ quái so với §8 |
| `damage_taken_sample` | realm, sourceRank, pctMaxHp | Quái có quá mạnh / yếu |
| `parry_result` | perfect / block / fail, skillRef | Cửa sổ đỡ có quá khó |
| `dodge_iframe_hit` | số đòn né được nhờ i-frame | Né có hữu ích |
| `poise_break` | targetRank, timeToBreak | Poise boss có quá dày |
| `combat_power` | realmCode, cp | Phân phối chiến lực |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="sf-ac-001"></a>SF-AC-001 | Bảng §5.2 | Chạy `tools/gen-base-stats` | Khớp đúng mọi mốc kiểm tra |
| SF-AC-002 | Ví dụ §6.3 (khử ngẫu nhiên) | Tính sát thương | 107 |
| SF-AC-003 | Phòng ngự = 3 × phòng ngự cơ bản của người tấn công | Tính `mitigation` | 0,375 |
| SF-AC-004 | Phòng ngự rất lớn | Tính `mitigation` | Không vượt 0,75 |
| SF-AC-005 | Căn cốt 70 | `computeStats` | `maxHp` có đóng góp `INNATE` +20%, `defense` +10% |
| SF-AC-006 | `TAL_FORTUNATE` + `TAL_GREAT_FORTUNE`, luck cơ bản 47 | `computeStats` | luck 77, `critRate` 5 + 1,35 = 6,35% |
| SF-AC-007 | Hai thiên phú `attack` +15% và +30% | `computeStats` | Tổng PCT +45% (cộng, không 1,15 × 1,30) |
| SF-AC-008 | `critRate` cộng tới 75% | `computeStats` | `final.critRate = 60`, `uncapped = 75` |
| SF-AC-009 | Đòn hệ THUY đánh mục tiêu `LOI` | Tính `relation` | 0,8 (Lôi khắc Thủy) |
| SF-AC-010 | 3 lần áp `ST_FROST` | Sau lần 3 | Có `ST_FROZEN` 1,5s, không còn `ST_FROST` |
| SF-AC-011 | Boss không phá thế | Áp `ST_STUN` | Không có hiệu lực (SF-R-122) |
| SF-AC-012 | Mục tiêu vừa bị `KNOCKDOWN` | Trúng đòn `KNOCKBACK` sau 1s | Không bị đẩy (SF-R-095) |
| SF-AC-013 | Mục tiêu `armorLevel = HEAVY` | Trúng đòn `STAGGER` | Không bị ngắt; nếu đòn đó làm vỡ poise → phá thế |
| SF-AC-014 | Bấm đỡ 0,15s trước khi đòn chạm (đòn có thể đỡ) | Server xử lý | Đỡ hoàn hảo: 0 sát thương, kẻ tấn công mất 30% poise, +15 năng lượng (nếu đã Kim Đan) |
| SF-AC-015 | Đòn `unblockable` | Người chơi giữ đỡ | Nhận đủ sát thương |
| SF-AC-016 | Thể lực 20 | Bấm né | Không né được |
| SF-AC-017 | `ST_WEAKENED` áp 600s rồi đăng xuất 5 phút | Đăng nhập | Còn 300s (`persistOffline`) |
| SF-AC-018 | Cùng snapshot đầu vào | Chạy `computeStats` ở client (TS) và server | Kết quả giống hệt (test dùng chung bộ dữ liệu) |
| SF-AC-019 | `maxHp` tăng từ 400 lên 500 khi HP 200 | Sau cập nhật | HP 250 (giữ 50%) |
| SF-AC-020 | Quái thường `LK5` | `computeStats` | `maxHp = round(293 × 0,6) = 176`, `maxPoise = 40` |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 1 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| `computeStats`, bảng cơ bản LK, công thức sát thương, chí mạng | ✅ | | | |
| Né, đỡ, đỡ hoàn hảo, thể lực, poise, phản ứng trúng đòn | ✅ | | | |
| Ngũ hành 5 hệ cơ bản | ✅ | | | |
| Hệ biến dị (Lôi, Băng, Phong) | | ✅ | | |
| Status lõi §5.12 | | ✅ | | |
| Năng lượng tuyệt kỹ | | ✅ (Kim Đan) | | |
| Chiến lực + chấm điểm hiệu ứng | | ✅ | | |
| Hệ số PvP (GD-32 PK-R-060) | | | ✅ | |

---

## 17. Câu hỏi mở
- [x] **SF-Q-1:** *(Đã chốt 2026-10-23: hệ số **tăng dần theo cảnh giới** — `g` 1,10 → 1,18 mỗi tầng, `J` 1,6 → 2,2 mỗi cảnh giới lớn; LK – TC1 giữ nguyên số cũ; §6.2.)* Hệ số tăng trưởng mỗi cảnh giới lớn `J = 1,6` có tạo đủ cảm giác "lên cảnh giới mạnh hẳn" không?
- [ ] **SF-Q-2:** Có hiển thị số sát thương của người chơi khác trong tổ đội không (rối mắt)? *Mặc định: chỉ hiện của mình + tổng đội ở boss.*
- [ ] **SF-Q-3:** Đỡ hoàn hảo có nên phản lại đạn bay (reflect) cho vũ khí kiếm ở mức Viên Mãn kỹ năng không?
- [x] **SF-Q-4:** *(Đã chốt 2026-10-23: hệ số chung — sát thương ×0,5, khống chế ×0,6 + kháng khống chế, hồi máu sau trúng ×0,7; GD-32 PK-R-060.)* PvP dùng hệ số giảm sát thương chung hay bộ công thức riêng?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-20 | — | Bản nháp đầu: danh sách chỉ số chuẩn, bảng cơ bản theo cảnh giới, quy đổi bẩm sinh, gộp hiệu ứng, sát thương, ngũ hành 8 hệ, chí mạng, hồi máu, poise & phản ứng, thể lực / né / đỡ, năng lượng, khung status + 17 status lõi (chuẩn hóa ID `ST_*`), hệ số quái, trần / sàn, chiến lực, nghiệm thu |
| v0.2 | 2026-10-23 | — | Chốt SF-Q-1: `realmScale` với `g` / `J` **tăng dần theo cảnh giới** (g: 1,10 · 1,12 · 1,14 · 1,16 · 1,18; J: 1,6 · 1,8 · 2,0 · 2,2), thêm `majorScale`; LK – TC1 không đổi, TC2+ tăng (TC4 +5,6%, KD1 +21%, KD4 +35%); bảng mốc SF-R-020 cập nhật; ví dụ chênh lệch chiến đấu KD1 / TC4 |
| v0.3 | 2026-10-23 | — | Chốt SF-Q-4: hệ số PvP chung (GD-32 PK-R-060) |

