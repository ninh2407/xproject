# GD-25 — Linh thú

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-25 · Prefix ID: `SB` |
| Owner | Game Designer + Content Designer + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 5 / Beta (theo PLAN §4.17); dữ liệu loài + bảng rơi trứng có thể nhập từ Alpha (ẩn bằng `phase: BETA`, TD-07 DT-R-058) |
| Quyết định nền | PLAN §4.17, D-03 (một mạng), D-12, D-17 (không bán sức mạnh bằng tiền thật); TD-07, GD-29 |
| Liên quan | [PLAN §4.17](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-07 Kỹ năng](13-skills.md) (triệu hồi §5.8, ngân sách §6.5) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-09 Vật phẩm](15-items-equipment.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-27 Thiên phú](04-talents.md) · [GD-29 RNG](05-rng-fairness.md) · [GD-30 Động phủ](28-caves-sect-sites.md) · [GD-10 Luyện đan](16-alchemy.md) · [GD-13 Kinh tế](19-economy.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [TD-14 Geometry](../tech/13-geom2d.md) |

> Linh thú dùng lại tối đa thứ đã có: **model + bộ xương của quái** (GD-12) đổi màu / tỉ lệ, **khung triệu hồi** (GD-07 §5.8) cho hành vi chiến đấu, **`SkillDef` + ngân sách** (GD-07 §6.5) cho chiêu của thú, **hiệu ứng thiên phú** (GD-27 `TalentEffect`) cho đặc tính thú, **chuẩn RNG / pity** (GD-29). Không có hệ chiến đấu hay hệ AI riêng.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-25 | Mục |
|--------------|-----------------|-----|
| GD-12 | Trường `beastSpeciesId`, biến thể **Linh chủng** (spawn hiếm, bắt được), trứng trong bảng rơi | §5.1, §5.3 |
| GD-09 | Loại vật phẩm `BEAST_EGG`, `UseSpec` `CAPTURE_BEAST` / `HATCH`, vật phẩm thú ấn | §3, §5.2 |
| GD-29 | Roll mới (bắt, nở, lai tạo), pity mới, khí vận cho biến dị | §6, §5.8 |
| GD-30 | Công trình **Linh Thú Viên** (`THU_VIEN`) | §5.6 |
| GD-26 | Linh thú khi chủ nhân hết kiếp | §5.9 |
| GD-10 | Đan mới cho thú (thân mật, lai tạo) | §7.4 |
| GD-13 | Nguồn / hố tiền mới, giao dịch trứng | §5.10 |

---

## 0. Đánh giá

| Tiêu chí | Đánh giá |
|----------|----------|
| **Khớp trụ cột** | ✅ P2 *Khám phá* (săn Linh chủng hiếm, trứng từ quái), P4 *Chế tác có chiều sâu* (lai tạo như một "nghề"), P1 *Mỗi kiếp một câu chuyện* (thú có thọ nguyên, sống qua nhiều kiếp của chủ — "con hồ ly đã theo ta ba đời") |
| **Khớp bối cảnh** | ✅ Ngự thú, linh thú huyết mạch, biến dị huyết mạch là mô-típ tiên hiệp quen thuộc |
| **Chi phí làm** | 🟡 Vừa phải nhờ tái dùng: model quái + tint (không cần mỹ thuật mới cho 8 loài đầu), khung triệu hồi, `SkillDef`, `TalentEffect`. Phần mới: AI đi theo + lệnh, dữ liệu di truyền, UI chuồng / gia phả, công trình động phủ. Ước tính **~2,5–3 người-tháng** (1 designer + 1 server + 0,5 client + 0,25 tech art) |
| **Rủi ro cân bằng** | 🟠 Thú thêm sát thương → phải có trần. Giải pháp: sức mạnh thú **tính theo chỉ số chủ nhân** × hệ số chất lượng (§6.4), trần ~17% DPS chủ |
| **Rủi ro với game một mạng** | 🟠 Thú mạnh mang sang kiếp sau làm kiếp mới dễ quá, phá sự khan hiếm của Tàng Bảo Các (3–6 ô). Giải pháp: Linh Thú Viên **có giới hạn ô**, chọn thú giữ lại khi tọa hóa, kết khế cần cảnh giới, sức mạnh tự co theo chủ mới (§5.9) |
| **Rủi ro kinh tế** | 🟠 Trại lai tạo (bot) làm lạm phát trứng phẩm cao. Giải pháp: lai tạo **tiêu thọ nguyên của thú bố mẹ**, giới hạn số lần mỗi thú, chi phí linh thạch tăng theo phẩm (hố tiền) (§5.7) |
| **Rủi ro tiền thật** | Không bán trứng / thú / tăng tỷ lệ bằng `JADE` (D-17, GD-29 RF-R-006). Chỉ cosmetic (dị sắc mua được là ngoại hình riêng, không phải biến dị) |
| **Kết luận** | **Nên làm ở Beta** như PLAN, phạm vi §16: bắt Linh chủng + trứng + 1 thú xuất chiến + lai tạo cơ bản. Thú cưỡi, trang bị thú, thú chiến đấu PvP: sau launch |

---

## 1. Mục tiêu

**Trụ cột:** P2, P4, P1.

- Gặp **Linh chủng** là một khoảnh khắc "cơ duyên nhỏ": quái phát sáng hiếm hoi, có thể bắt nếu chuẩn bị.
- Thú là **bạn đồng hành có cá tính** (đặc tính, chiêu, tên), không phải con số thuần túy.
- **Lai tạo** là trò chơi dài hạn: chọn cặp, mong biến dị, xây dòng huyết mạch qua nhiều kiếp.
- Không thay thế vai trò của chủ nhân: thú **hỗ trợ**, không gánh trận.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="sb-r-001"></a>**SB-R-001** | Sức mạnh chiến đấu của thú **tính từ chỉ số chủ nhân** × hệ số chất lượng thú (§6.4). Thú không có trang bị / cấp độ riêng làm tăng sức mạnh vượt trần. |
| <a id="sb-r-002"></a>**SB-R-002** | Mọi roll (bắt, nở, lai tạo, biến dị) do **API Service** thực hiện, ghi `rng_audit` (TD-01 §9); loại roll theo GD-29 §3. |
| <a id="sb-r-003"></a>**SB-R-003** | Mọi thay đổi thú (sinh, đổi chủ, chết, lai tạo) đi qua **cổng beast** của API Service (`beasts/*`), idempotent, ghi `beast_log`. |
| <a id="sb-r-004"></a>**SB-R-004** | Thú **không chết vì chiến đấu**: bị đánh gục chỉ "trọng thương" (rút về, chờ hồi). Thú chỉ mất khi **hết thọ nguyên** hoặc **bị thả / không có chỗ giữ khi chủ tọa hóa** (§5.9). |
| <a id="sb-r-005"></a>**SB-R-005** | Không bán bằng tiền thật trứng, thú, vật phẩm bắt / lai tạo, hay bất cứ thứ gì tăng tỷ lệ (D-17). |

---

## 2. Tổng quan

```
 Quái thường (loài nuôi được) ──giết──► rơi TRỨNG (hiếm, loại B) ──┐
 LINH CHỦNG (spawn hiếm, phát sáng) ──đánh còn ≤30% HP──► Ngự Thú Phù (loại A, thấy tỷ lệ)
                                                     │ thành công          │
                                                     ▼                     ▼
 Cơ duyên / boss / hiểm địa ──► trứng phẩm cao    LINH THÚ (đã có tư chất) ◄── ẤP TRỨNG (roll tư chất, đặc tính)
 BOSS ĐẶC BIỆT ──HP ≤10% → KIỆT SỨC 8s──► Huyền Ngự Thú Phù (tỷ lệ rất thấp ≤ 15%, đổi lấy phần rơi đồ boss) ──► THÚ BOSS
                                                     │
                     ┌──────── KẾT KHẾ với nhân vật (tối đa 3) ─ XUẤT CHIẾN 1 con: đi theo, tự đánh, phím T = chiêu thú
                     │                              tăng THÂN MẬT (đan dược, chiến đấu cùng)
                     ▼
     LINH THÚ VIÊN (động phủ, cấp tài khoản, có giới hạn ô) ── LAI TẠO 2 con cùng nhóm ──► trứng con
                                  │                                   (di truyền + BIẾN DỊ theo độ hiếm)
          chủ tọa hóa: chọn thú giữ lại (theo ô) ─► kiếp sau kết khế lại
          thú hết thọ: hóa linh → Thú Hồn Tinh (nguyên liệu lai tạo / luyện đan)
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type BloodlineGrade = 'PHAM' | 'LINH' | 'HUYEN' | 'DIA' | 'THIEN';   // huyết mạch (cùng giá trị với phẩm cơ duyên GD-22)
type BeastRole = 'ASSAULT' | 'GUARDIAN' | 'MYSTIC' | 'FORAGER';       // tấn công · hộ vệ · pháp thuật / hỗ trợ · thu thập
type AptitudeKey = 'vigor' | 'might' | 'spirit' | 'agility';          // Cương · Lực · Linh · Tốc (tư chất 0–100)

interface BeastSpeciesDef {                  // shared/gamedata/beasts/species.json
  id: string;                                // BST_*
  nameKey: string;
  sourceMonsterId: string | null;            // MON_* dùng model / bộ xương; null với loài lai chỉ có từ lai tạo
  modelId: string; tint?: string; scale: number;
  breedGroup: string;                        // nhóm lai được với nhau, vd. "BEAST_QUADRUPED", "SERPENT", "SPIRIT"
  element: Element | 'NONE';
  role: BeastRole;
  size: 'S' | 'M' | 'L';                     // TD-14 bodyRadius (thú không có cỡ XL)
  minBloodline: BloodlineGrade; maxBloodline: BloodlineGrade;
  aptitudeBias: Record<AptitudeKey, number>; // cộng vào trung bình tư chất khi roll (−15..+15)
  basicSkillId: string;                      // SKL_BST_* đòn tự động
  commandSkillIds: Partial<Record<BloodlineGrade, string>>;   // chiêu lệnh (phím T), mạnh dần theo huyết mạch
  traitPool: string[];                       // BTR_* thường
  rareTraitPool: string[];                   // BTR_* chỉ có qua biến dị
  hybridOf?: [string, string];               // loài lai: hai loài gốc
  bossSpecies?: { sourceBossId: string; baseSpeciesId: string; signatureTraitId: string };
                                             // loài từ boss đặc biệt (§5.2b): con lai tạo không bao giờ ra loài này
  phase?: 'ALPHA' | 'BETA' | 'LAUNCH';
}

interface BeastTraitDef {                    // shared/gamedata/beasts/traits.json
  id: string;                                // BTR_*
  nameKey: string; descKey: string;
  rare: boolean;
  appliesTo: 'BEAST' | 'OWNER';              // tác dụng lên thú hay lên chủ (khi xuất chiến)
  effects: TalentEffect[];                   // dùng union của GD-27 §3 (chỉ các type được phép, §7.3)
}

interface Beast {                            // một con thú (bảng beast)
  id: string;                                // UUIDv7
  accountId: string;
  speciesId: string; name: string;           // tên người chơi đặt (lọc GD-14)
  bloodline: BloodlineGrade;
  aptitudes: Record<AptitudeKey, number>;    // 0–100, trần theo huyết mạch (§6.3)
  traitIds: string[];                        // ≤ traitSlots[bloodline]
  mutations: MutationMark[];                 // lịch sử biến dị (hiển thị gia phả)
  colorVariant: string | null;               // dị sắc (chỉ ngoại hình)
  ageYears: number; lifespanMaxYears: number;
  bond: number;                              // thân mật 1–10
  breedCount: number;                        // số lần đã lai tạo
  generation: number;                        // 1 = bắt / nở từ trứng hoang; ≥ 2 = từ lai tạo
  parentIds: [string, string] | null;
  state: BeastState;
  contractCharacterId: string | null;        // nhân vật đang kết khế
  injuredUntil: string | null;
  bornAt: string; origin: 'CAPTURE' | 'BOSS_CAPTURE' | 'WILD_EGG' | 'BREED' | 'ENCOUNTER' | 'GM';
}
type BeastState = 'CONTRACTED' | 'STABLED' | 'BREEDING' | 'SEALED' | 'DEPARTED';
//  CONTRACTED: đã kết khế với nhân vật sống · STABLED: trong Linh Thú Viên · BREEDING: đang lai tạo
//  SEALED: thành Thú Ấn (vật phẩm, để giao dịch, Beta) · DEPARTED: đã mất (hết thọ / thả / không ô)

interface MutationMark { at: string; kind: MutationKind; detail: string }
type MutationKind = 'BLOODLINE_UP' | 'RARE_TRAIT' | 'APTITUDE_SURGE' | 'COLOR_VARIANT';

interface BreedingSession {
  id: string; accountId: string; parentA: string; parentB: string;
  startedAt: string; readyAt: string;
  quote: BreedQuote;                         // ảnh chụp tỷ lệ lúc bắt đầu (TD-07 DT-R-095)
  state: 'RUNNING' | 'READY' | 'COLLECTED';
  resultEggItemInstanceId: string | null;
}

interface EggSpec {                          // ItemTemplate.egg (category BEAST_EGG, GD-09)
  speciesId: string | 'FROM_PARENTS';
  bloodline: BloodlineGrade;
  hatchMinutes: number;                      // thời gian thực
}
```

**Vật phẩm mới (GD-09):** category `BEAST_EGG` (trứng hoang: mẫu `ITM_EGG_{LOÀI}_{HUYẾT_MẠCH}`, sinh tự động; trứng lai tạo: `ITM_EGG_BRED` với `ItemInstance.attributes.breed` chứa kết quả đã roll); `UseSpec` mới `{ kind: 'CAPTURE_BEAST'; tier: BloodlineGrade; bonusPp: number }` (Ngự Thú Phù, category `TALISMAN`) và `{ kind: 'HATCH' }`; `ITM_BEAST_SEAL` (Thú Ấn, Beta).

---

## 4. Máy trạng thái

```
            bắt / nở / nhận
                 │
                 ▼
  ┌────────► STABLED ◄──────────── giải khế (tại Ngự Thú Sư / Thú Viên)
  │            │  ▲  │
  │   kết khế  │  │  │ lai tạo bắt đầu ──► BREEDING ──(xong)──► STABLED
  │            ▼  │  │
  │        CONTRACTED ── chủ tọa hóa ──► STABLED (nếu được chọn & còn ô) / DEPARTED
  │            │
  │            └── xuất chiến / thu về (không đổi state; trường runtime `summoned`)
  │
  └── mở Thú Ấn ◄── SEALED ◄── phong ấn (Beta)
  mọi state ── hết thọ ──► DEPARTED (sinh Thú Hồn Tinh)
```
Trọng thương (`injuredUntil > now`) là cờ, không phải state: không xuất chiến / lai tạo được tới khi hết.

---

## 5. Quy tắc

### 5.1. Loài nuôi được và Linh chủng
- <a id="sb-r-010"></a>**SB-R-010** Quái có `beastSpeciesId` (trường mới của `MonsterDef`, GD-12) là **loài nuôi được**. Quái hạng thường (`NORMAL`) thuộc họ `BEAST` / `SPIRIT` bắt được khi là Linh chủng (§5.2). Tinh anh **không** bắt được. Boss chỉ bắt được nếu là **boss đặc biệt** (có khối `capture` trong `MonsterDef`, §5.2b), tỷ lệ rất thấp; boss khác có thể rơi trứng.
- <a id="sb-r-011"></a>**SB-R-011** **Linh chủng:** khi spawn một quái nuôi được, Game Server roll `p = linhChungChance` (mặc định 0,015; loại B, `serverOnly`) → quái thành biến thể Linh chủng: hào quang theo huyết mạch, tên vàng "Linh chủng · {tên}", chỉ số ×1,3, **huyết mạch** roll theo `linhChungBloodlineWeights` (LINH 80 · HUYEN 18 · DIA 2, giới hạn bởi `maxBloodline` của loài). Mỗi kênh bản đồ tối đa `linhChungMaxPerMap` (2) cùng lúc.
- <a id="sb-r-012"></a>**SB-R-012** Linh chủng là thực thể **riêng của người phát hiện đầu tiên trong 60s** (người gây sát thương đầu tiên được quyền bắt; người khác vẫn đánh được nhưng không ném phù được), sau 60s mở cho mọi người. Tránh tranh giành vô nghĩa.
- <a id="sb-r-013"></a>**SB-R-013** Quét thần thức (GD-21) phát hiện Linh chủng trong bán kính quét kể cả khi bị che (biểu tượng riêng trên minimap 30s). Thiên phú `ENCOUNTER_WEIGHT_PCT` **không** ảnh hưởng Linh chủng (tránh chồng hiệu ứng); khí vận **có** (loại B, §6.1).

### 5.2. Bắt (thu phục)
- <a id="sb-r-020"></a>**SB-R-020** Điều kiện ném Ngự Thú Phù: mục tiêu là Linh chủng (SB-R-010/011), HP ≤ `captureHpThreshold` (30%), cách ≤ 6m, người chơi có quyền bắt (SB-R-012), phù có `tier` ≥ huyết mạch mục tiêu **trừ 1 bậc** (vd. Huyền chủng cần phù Linh trở lên).
- <a id="sb-r-021"></a>**SB-R-021** Ném phù: **vận pháp 2s** (`CHANNELING`, di chuyển hủy, bị đánh trúng hủy — phù **không** mất khi hủy). Xong vận pháp → API roll (loại A). Trước khi vận pháp, HUD hiện `ChanceView` (GD-29 §5.2) — cập nhật theo HP mục tiêu mỗi 0,5s; tỷ lệ dùng để roll là tỷ lệ **lúc kết thúc vận pháp** (báo giá tự động, không cần xác nhận lại vì hành động liên tục; thay đổi do HP giảm chỉ có lợi).
- <a id="sb-r-022"></a>**SB-R-022** Thành công: quái biến mất (hiệu ứng thu vào phù), sinh `Beast` (`origin = CAPTURE`, `generation = 1`, huyết mạch của Linh chủng, tư chất + đặc tính roll như nở trứng §6.3), vào Linh Thú Viên nếu còn ô, nếu không thì vào **ô tạm** của Ngự Thú Sư (giữ 7 ngày thực).
- <a id="sb-r-023"></a>**SB-R-023** Thất bại: phù mất, quái hồi 10% HP và thoát khỏi trạng thái khống chế; mỗi con Linh chủng có tối đa `captureAttemptsPerTarget` (3) lần, quá thì nó bỏ chạy và biến mất sau 5s. Pity theo §6.2.

### 5.2b. Bắt boss đặc biệt
- <a id="sb-r-025"></a>**SB-R-025** **Boss đặc biệt** là boss có khối `capture: { speciesId, bloodlineWeights }` trong `MonsterDef` (GD-12). Danh sách do designer chọn (boss có hình thú / linh vật, hợp lý khi thu phục); Vực 1: **Hồ Yêu Mẫu** (`MON_B_FOX_MATRIARCH`, BC02) → loài `BST_FOX_MATRIARCH` (Hồ Yêu). Kiếm Hồn, Khoáng Ma Bạo Chúa: không.
- <a id="sb-r-026"></a>**SB-R-026** **Cửa sổ Kiệt Sức:** lần đầu HP boss đặc biệt chạm ≤ `bossCaptureHpThreshold` (10%) trong một trận, boss vào trạng thái **Kiệt Sức** `bossExhaustedSec` (8s): đứng yên, không tấn công, không nhận sát thương (`S2C_BossPhase { phase: 'EXHAUSTED' }`, hào quang thu phục quanh boss). **Chỉ** trong cửa sổ này mới ném phù được. Hết cửa sổ mà chưa ai thu phục → boss hồi 5% HP và tiếp tục pha cuối như bình thường (đánh hạ để nhận thưởng). Mỗi trận chỉ có **một** cửa sổ.
- <a id="sb-r-027"></a>**SB-R-027** Điều kiện ném: người chơi được tính công (GD-12 EN-R-100), có **Ngự Thú Phù bậc ≥ HUYEN**, chưa dùng lượt của mình trong trận này, còn lượt trong ngày (`bossCaptureDailyAttempts` = 3 lượt / tài khoản / boss / ngày thực, reset 05:00). Mỗi người **tối đa 1 lần ném mỗi trận**; vận pháp `bossCaptureChannelSec` (3s, phải hoàn tất trong cửa sổ 8s). Tỷ lệ theo §6.2b (loại A, `ChanceView` hiện khi nhắm, như SB-R-021).
- <a id="sb-r-028"></a>**SB-R-028** Nhiều người ném trong cùng cửa sổ: server xử lý theo **thứ tự hoàn tất vận pháp**; người đầu tiên thành công thu phục, các lần ném đang vận pháp của người khác bị hủy và **hoàn phù** (không tính lượt ngày). Thất bại: phù mất, tính lượt ngày, pity tăng (§6.2b).
- <a id="sb-r-029"></a>**SB-R-029** **Đánh đổi phần thưởng:** thu phục thành công = boss **bị hạ** với mọi mục đích (nhiệm vụ, tiến độ bí cảnh, codex). Mọi người được tính công nhận phần rơi đồ cá nhân như bình thường (EN-R-101), **trừ người thu phục**: người đó nhận **thú boss thay cho** phần rơi đồ boss của mình (vẫn nhận tu vi). Quyết định ném phù là quyết định có giá.
- <a id="sb-r-02a"></a>**SB-R-02A** **Thú boss:** huyết mạch roll theo `bloodlineWeights` của boss (Hồ Yêu Mẫu: HUYEN 90 · DIA 10), tư chất theo §6.3 với `aptitudeBias` cao của loài, **luôn có đặc tính đặc trưng** `signatureTraitId` (vd. `BTR_NINE_TAILS_FLAME`) chiếm một ô riêng ngoài `traitSlots`, chiêu lệnh là phiên bản thu nhỏ chiêu của boss (vd. *Cửu Vĩ Hỏa Vũ* 3 vùng thay vì 5), vẫn trong ngân sách GD-07 §6.5 theo huyết mạch (SB-R-043). Model boss thu nhỏ (`scale` 0,55, cỡ `L`). Sức mạnh vẫn theo công thức chung §6.4 (không vượt trần).
- <a id="sb-r-02b"></a>**SB-R-02B** **Lai tạo thú boss:** được, nhưng con **không bao giờ** là loài boss: loài con = loài của thú còn lại hoặc `baseSpeciesId` (Hồ Yêu → Xích Hồ). Đặc tính đặc trưng kế thừa với xác suất `signatureInheritChance` (0,15) và khi kế thừa thì chiếm một ô thường. Loài boss vì vậy **chỉ có qua thu phục**, giữ độ hiếm.
- <a id="sb-r-02c"></a>**SB-R-02C** Thu phục boss phát thông báo toàn server ưu tiên 2 (GD-14 SO-R-060): *"{tên} đã thu phục Hồ Yêu Mẫu!"*; ghi vào Bia Vạn Cổ của nhân vật.

### 5.3. Trứng
- <a id="sb-r-030"></a>**SB-R-030** Trứng hoang rơi từ: quái thường nuôi được (bảng rơi `LT_*`, trứng PHAM ~0,5%, LINH ~0,05% — loại B, `serverOnly`), boss bí cảnh (trứng LINH/HUYEN của loài liên quan), rương hiểm địa nhị / tam giai (HUYEN/DIA, PLAN §4.6), cơ duyên (tới THIEN, GD-22 outcome `GRANT_ITEM`). Trứng **giao dịch được** (`bind = NONE`).
- <a id="sb-r-031"></a>**SB-R-031** **Ấp trứng** (`UseSpec HATCH`) tại Ngự Thú Sư (`NPC_BEAST_KEEPER`, `HUB`) hoặc Linh Thú Viên: thời gian thực `hatchMinutes` (PHAM 30 · LINH 60 · HUYEN 180 · DIA 480 · THIEN 720), phí linh thạch (§7.1). Ngự Thú Sư ấp **1** trứng một lúc và không ấp được trứng DIA trở lên; Linh Thú Viên cấp n ấp `n` trứng một lúc, mọi phẩm.
- <a id="sb-r-032"></a>**SB-R-032** Nở: roll tư chất + đặc tính (§6.3, loại A — xem trước hiện **phân phối** tư chất và xác suất số đặc tính). Trứng lai tạo đã chứa kết quả roll lúc lai tạo (§5.7); nở chỉ hiện ra.

### 5.4. Kết khế và xuất chiến
- <a id="sb-r-040"></a>**SB-R-040** Nhân vật kết khế tối đa `contractSlots` (3) thú; điều kiện cảnh giới theo huyết mạch: PHAM `LK1` · LINH `LK3` · HUYEN `LK7` · DIA `TC1` · THIEN `KD1`. Kết khế / giải khế tại Ngự Thú Sư hoặc Linh Thú Viên (kiểm tra `CharacterContext`, TD-01 §11).
- <a id="sb-r-041"></a>**SB-R-041** **Xuất chiến** tối đa 1 thú (phím `G` chọn / gọi / thu, ngoài chiến đấu; trong chiến đấu gọi được nhưng mất 1,5s vận pháp). Thú xuất chiến là thực thể thuộc chủ theo khung triệu hồi GD-07 §5.8, **không** hết hạn theo `durationSec`; biến mất khi chủ chuyển bản đồ (tự gọi lại ở bản đồ mới), vào `CHANNELING`, hoặc thú bị đánh gục.
- <a id="sb-r-042"></a>**SB-R-042** Hành vi: đi theo chủ (cách 2–4m); khi chủ trong chiến đấu thì đánh mục tiêu chủ đang đánh, nếu không có thì kẻ địch gần chủ nhất trong 8m (GD-07 SK-R-091). Quái **không ưu tiên** nhắm thú (trừ khi chỉ còn thú trong tầm). Vai `GUARDIAN` có chiêu khiêu khích trong `commandSkillIds` (ngoại lệ có chủ đích). Ở PvP dã ngoại, thú đánh người chơi theo chế độ PK của chủ (GD-32 PK-R-063).
- <a id="sb-r-043"></a>**SB-R-043** **Chiêu lệnh** (phím `T`): thú dùng `commandSkillIds[bloodline]` (một `SkillDef`, hồi chiêu ≥ 15s, thi triển từ vị trí thú hướng vào mục tiêu của chủ / điểm chuột). Chiêu thú tuân ngân sách GD-07 §6.5 theo bậc: PHAM/LINH = budget `HOANG`, HUYEN = `HUYEN`, DIA = `DIA`, THIEN = `THIEN`, với hệ số tính theo chỉ số thú (§6.4).
- <a id="sb-r-044"></a>**SB-R-044** **Trọng thương thú:** HP thú về 0 → rút về, `injuredUntil = now + 300s` (giảm 50% nếu đang trong thành trấn / động phủ). Không mất gì khác.
- <a id="sb-r-045"></a>**SB-R-045** Vai `FORAGER`: khi xuất chiến và chủ thu thập (hái thuốc, khai khoáng), có đặc tính thu thập (vd. `BTR_DIGGER`: +10% cơ hội thêm 1 nguyên liệu). Không tự thu thập thay chủ (tránh bot).

### 5.5. Thân mật
- <a id="sb-r-050"></a>**SB-R-050** Thân mật 1–10, tăng bằng: chiến đấu cùng (điểm theo quái hạ khi thú xuất chiến, trần mỗi ngày thực), cho ăn **Linh Thú Đan** (GD-10, §7.4). Mỗi bậc: +3% hệ số chất lượng (§6.4); bậc 5 mở đặc tính thứ hai (nếu có), bậc 10 có hoạt ảnh thân thiết.
- <a id="sb-r-051"></a>**SB-R-051** Thân mật **giữ nguyên** khi đổi chủ trong cùng tài khoản (qua kiếp); **reset về 1** khi thú được giao dịch sang tài khoản khác.

### 5.6. Linh Thú Viên
- <a id="sb-r-060"></a>**SB-R-060** Công trình động phủ mới `THU_VIEN` (GD-30 `CaveBuilding`), cấp 0–3, thuộc tài khoản:

| Cấp | Ô giữ thú | Ấp cùng lúc | Lai tạo | Chi phí nâng cấp (linh thạch + vật liệu) |
|:---:|:---:|:---:|:---:|------|
| 0 (chưa xây) | 2 (ô tại Ngự Thú Sư) | 1 (Ngự Thú Sư, ≤ HUYEN) | ❌ | — |
| 1 | 4 | 1 | ✅ (≤ HUYEN) | 3.000 + 10 Linh Mộc |
| 2 | 6 | 2 | ✅ (≤ DIA) | 12.000 + vật liệu TC |
| 3 | 8 | 3 | ✅ (mọi phẩm), +3 pp biến dị | 40.000 + vật liệu KD |

- <a id="sb-r-061"></a>**SB-R-061** Thú `CONTRACTED` **không** chiếm ô Thú Viên; `STABLED` và `BREEDING` chiếm ô. Mở thêm ô bằng Luân Hồi: unlock `UNL_BEAST_STABLE_SLOT` (+1, tối đa 2 lần, GD-26).

### 5.7. Lai tạo
- <a id="sb-r-070"></a>**SB-R-070** Điều kiện: hai thú `STABLED` của cùng tài khoản, **khác nhau**, cùng `breedGroup` (hoặc thuộc một công thức lai `hybridOf`), cả hai không trọng thương, `breedCount < breedLimit[bloodline]` (PHAM 5 · LINH 4 · HUYEN 3 · DIA 2 · THIEN 1), tuổi ≥ 10% thọ (trưởng thành), thọ còn ≥ chi phí thọ; huyết mạch cao nhất của cặp ≤ mức Thú Viên cho phép (SB-R-060). **Không** lai với bố / mẹ / anh chị em ruột (kiểm tra `parentIds` một đời) — giữ gia phả có ý nghĩa.
- <a id="sb-r-071"></a>**SB-R-071** **Xem trước** (`BreedQuote`, loại A, GD-29): loài con có thể ra + xác suất, huyết mạch gốc, tỷ lệ biến dị (pp, từng dòng), phân phối loại biến dị, khoảng tư chất con, xác suất kế thừa từng đặc tính, chi phí (linh thạch, Linh Phối Đan, thọ của bố mẹ). Bắt đầu cần `quoteId` (GD-29 RF-R-022).
- <a id="sb-r-072"></a>**SB-R-072** Bắt đầu: transaction tiêu chi phí, trừ thọ bố mẹ (`breedLifespanCost[bloodline]` năm mỗi con), `breedCount += 1`, cả hai `BREEDING` trong `breedHours` (thời gian thực, PHAM 1h … THIEN 6h); **roll toàn bộ kết quả ngay lúc bắt đầu** (seed kết quả lưu `rng_audit`, không gửi client — GD-29 RF-R-050) và lưu vào trứng sẽ nhận. Hết giờ: bố mẹ về `STABLED`, người chơi nhận `ITM_EGG_BRED` (ẩn kết quả tới khi nở, nhưng tooltip hiện loài + huyết mạch gốc).
- <a id="sb-r-073"></a>**SB-R-073** Kết quả lai tạo theo §6.5: loài, huyết mạch (có thể tăng nhờ biến dị), tư chất trung bình bố mẹ ± dao động, kế thừa đặc tính, biến dị theo độ hiếm. `generation = max(gen bố, gen mẹ) + 1`.

### 5.8. Biến dị
- <a id="sb-r-080"></a>**SB-R-080** Mỗi lần lai tạo roll **có biến dị không** (tỷ lệ §6.5, có pity §6.2); nếu có, roll **loại biến dị** theo bảng trọng số phụ thuộc **huyết mạch gốc** của con — huyết mạch càng cao thì **khó biến dị hơn nhưng biến dị mạnh hơn**:

| Loại biến dị | Hiệu ứng | Trọng số khi gốc PHAM | LINH | HUYEN | DIA |
|--------------|----------|:---:|:---:|:---:|:---:|
| `COLOR_VARIANT` | Dị sắc (ngoại hình hiếm, không tăng sức mạnh) | 40 | 30 | 20 | 10 |
| `APTITUDE_SURGE` | +12 mọi tư chất (vượt trần thường, tới trần biến dị §6.3) | 35 | 35 | 30 | 25 |
| `RARE_TRAIT` | Thêm 1 đặc tính từ `rareTraitPool` (ô thêm, vượt `traitSlots`) | 20 | 25 | 35 | 40 |
| `BLOODLINE_UP` | Huyết mạch +1 (không vượt `maxBloodline` loài; nếu đã trần → đổi thành `RARE_TRAIT`) | 5 | 10 | 15 | 25 |

  Gốc THIEN: tỷ lệ biến dị 0 (đỉnh huyết mạch), trừ dị sắc qua cơ duyên.
- <a id="sb-r-081"></a>**SB-R-081** Mỗi con có tối đa `maxPowerMutations` (2) biến dị **kiểu sức mạnh** (`APTITUDE_SURGE`, `RARE_TRAIT`) của **chính nó** — dấu biến dị của bố mẹ không tính, nhưng tư chất / đặc tính đã kế thừa thì giữ. Vượt giới hạn → biến dị đổi thành `COLOR_VARIANT`. Dị sắc và tăng huyết mạch không giới hạn số lần.
- <a id="sb-r-082"></a>**SB-R-082** **Lai loài** (`hybridOf`): khi cặp thuộc công thức lai, `hybridChance` (10% nếu cả hai ≥ LINH, 20% nếu cả hai ≥ HUYEN) con ra **loài lai** (vd. Xích Hồ × Ngạ Lang → `BST_FLAME_WOLF` Hỏa Lang). Loài lai có `rareTraitPool` riêng và `maxBloodline` cao hơn.

### 5.9. Khi chủ nhân hết kiếp
- <a id="sb-r-090"></a>**SB-R-090** Màn tổng kết (GD-26 `ENDING`) có bước **"Linh thú"**: mọi thú `CONTRACTED` được liệt kê; người chơi chọn thú **về Linh Thú Viên** trong giới hạn ô trống. Thú không được chọn → **trở về hoang dã** (`DEPARTED`, origin lịch sử "phóng sinh", hiện trong Bia Vạn Cổ). Thú `STABLED` vẫn ở nguyên. *(GD-26 v0.3:)* Thú về Linh Thú Viên bị **phong ấn cùng động phủ**; kiếp sau thắng thủ vệ (GD-30 HM-R-045, thú mạnh nhất làm thú canh cửa) mới dùng lại được.
- <a id="sb-r-091"></a>**SB-R-091** Chủ **chết thật** (hiểm địa / thiên kiếp): thú xuất chiến lúc đó "hộ chủ" — trừ thọ `guardDeathLifespanCost` (10 năm, không về dưới 1) rồi vào danh sách chọn như SB-R-090.
- <a id="sb-r-092"></a>**SB-R-092** Kiếp mới kết khế lại theo SB-R-040 (điều kiện cảnh giới). Sức mạnh thú **tự co theo chủ mới** vì tính từ chỉ số chủ (SB-R-001): một con Thiên chủng mang sang kiếp mới cho lợi thế **chất lượng** (~+17% DPS so với +6% của Phàm chủng), không cho sức mạnh thô của kiếp trước.

### 5.10. Thọ nguyên của thú, giao dịch
- <a id="sb-r-100"></a>**SB-R-100** Thú có thọ theo huyết mạch (`lifespanByBloodline`: PHAM 60 · LINH 100 · HUYEN 160 · DIA 250 · THIEN 400 năm). Thú **già cùng đồng hồ của nhân vật sống** trên tài khoản (GD-26: chỉ khi nhân vật online, cùng hệ số khu vực), ở mọi state trừ `SEALED`. Không có nhân vật sống → thú không già.
- <a id="sb-r-101"></a>**SB-R-101** Hết thọ → `DEPARTED` ("hóa linh"): người chơi nhận **Thú Hồn Tinh** (`ITM_BEAST_SOUL_{BLOODLINE}`) — dùng làm chất xúc tác lai tạo (+pp biến dị, §6.5) hoặc nguyên liệu luyện đan cao cấp. Thú vào "Linh Thú Phổ" (bảng tưởng niệm tài khoản).
- <a id="sb-r-102"></a>**SB-R-102** Không có cách kéo dài thọ thú ở MVP (giữ áp lực lai tạo thế hệ mới). (SB-Q-2.)
- <a id="sb-r-103"></a>**SB-R-103** **Giao dịch thú (Beta):** phong ấn thành **Thú Ấn** (`ITM_BEAST_SEAL`, phí 10% giá trị) — vật phẩm giao dịch / đấu giá được (GD-13), mở ấn để nhận thú; tuổi đóng băng trong lúc phong ấn; thân mật reset khi đổi tài khoản (SB-R-051). Trứng giao dịch tự do.

---

## 6. Công thức

### 6.1. Linh chủng và khí vận
```
P(linh chủng) = linhChungChance × clamp((luck / 50) ^ 1, 0,5, 2)        (loại B, GD-29 RF-R-082 bổ sung)
```

### 6.2. Tỷ lệ bắt (điểm phần trăm, GD-29 RF-R-090)
```
capturePct = clamp(
    baseByBloodline[target]                       // LINH 30 · HUYEN 15 · DIA 6
  + hpBonus      30 × (0,30 − hpPct) / 0,30        // 0 ở 30% HP, ~29 ở 1% HP
  + talisman     bonusPp của phù                   // Phàm Thú Phù 0 · Linh 10 · Huyền 20
  + sense        (spiritSense − 50) × 0,2          // thần thức (không dùng khí vận — GD-29 RF-R-081)
  + realm        clamp((stageIdx(chủ) − stageIdx(quái)) × 2, −20, +10)
  + control      +10 nếu mục tiêu đang STUNNED / FROZEN / ENTANGLE
  + pity         min(5 × captureFails[loài], 20)   // ADDITIVE_PP, reset khi bắt thành công loài đó
, 5, 90)
```
**Ví dụ:** Linh chủng Xích Hồ huyết mạch LINH, HP 12%, phù Linh (+10), thần thức 60, chủ `LK7` vs quái `LK6` (chênh 1 tiểu cảnh giới), đang `ENTANGLE`, đã trượt 1 lần với loài này:
`30 + 30 × 0,18/0,30 (= 18) + 10 + 2 + 2 + 10 + 5 = 77%`.

### 6.2b. Tỷ lệ bắt boss đặc biệt (điểm phần trăm)
```
bossCapturePct = clamp(
    bossCaptureBasePp                              // 3
  + talisman     HUYEN +3 · DIA +5                 // chỉ phù ≥ HUYEN
  + sense        (spiritSense − 50) × 0,05         // ±2,5
  + realm        clamp(stageIdx(chủ) − stageIdx(boss), −10, +3)
  + poise        +2 nếu boss đang POISE_BROKEN khi vào Kiệt Sức (thưởng cho phá thế trước đó)
  + pity         min(1 × bossCaptureFails[tài khoản × boss], 10)   // ADDITIVE_PP, reset khi thu phục boss đó
, 1, 15)
```
**Ví dụ:** chủ `LK9` (chênh boss `LK7` 2 tiểu cảnh giới), thần thức 60, phù Huyền, boss bị phá thế trước khi Kiệt Sức, chưa từng trượt: `3 + 3 + 0,5 + 2 + 2 + 0 = 10,5%`. Sau 10 lần trượt (≥ 4 ngày với 3 lượt / ngày): tối đa 15% (trần).

### 6.3. Tư chất và đặc tính khi nở / bắt
```
mean[bloodline] = PHAM 35 · LINH 50 · HUYEN 62 · DIA 74 · THIEN 85
cap[bloodline]  = PHAM 60 · LINH 75 · HUYEN 85 · DIA 95 · THIEN 100      (trần biến dị: cap + 10, tối đa 100)
aptitude_k = clamp(round(mean + aptitudeBias_k + N(0, 8)), 1, cap)      N: xấp xỉ bằng tổng 3 U(−8; 8) / 1,41 (tất định, TD-01)
traitSlots[bloodline] = PHAM 1 · LINH 1 · HUYEN 2 · DIA 2 · THIEN 3
traits: rút traitSlots đặc tính khác nhau từ traitPool (đều nhau)
```

### 6.4. Sức mạnh thú (SB-R-001)
```
quality  = gradeScale[bloodline] × (0,6 + 0,4 × avgAptitude / 100) × (1 + 0,03 × (bond − 1))
gradeScale: PHAM 0,12 · LINH 0,15 · HUYEN 0,19 · DIA 0,24 · THIEN 0,30

Chỉ số thú (chụp từ chủ khi gọi, cập nhật khi stats.inputs_changed):
  attack / spellPower = chủ × quality × (1 + (might | spirit − 50) / 200)
  maxHp               = chủ.maxHp × quality × roleHpMult × (1 + (vigor − 50) / 200)      roleHpMult: ASSAULT 1,5 · GUARDIAN 3 · MYSTIC 1,2 · FORAGER 1,2
  defense, spellResist = chủ × 0,8
  moveSpeed           = chủ.moveSpeed (luôn theo kịp)
  tốc đánh            = basicSkill.cooldown × (1 − (agility − 50) / 250)
Đòn tự động: basicSkill hệ số ~0,8 mỗi 1,2s.
```
**Kiểm tra trần:** DPS thú ≈ `0,8 / 1,2 × quality × attack(chủ)`, DPS chủ ≈ `1,5 × attack` → tỷ lệ ≈ `0,44 × quality`. Phàm chủng tư chất 35, thân mật 1: `quality = 0,12 × 0,74 = 0,089` → **~4%**. Thiên chủng tư chất 100, thân mật 10: `0,30 × 1,0 × 1,27 = 0,381` → **~17%**. Mục tiêu §8: ≤ 20%.

### 6.5. Lai tạo
```
Loài con:     công thức lai thỏa → P(lai) = hybridChance; còn lại 50 / 50 loài bố / mẹ
Huyết mạch gốc = min(bloodline A, bloodline B); nếu A == B: giữ nguyên
Tư chất con_k = clamp(round((A_k + B_k) / 2 + U(−6; +6)), 1, cap[gốc])
Kế thừa đặc tính: mỗi đặc tính (không trùng) của bố / mẹ kế thừa với xác suất 0,40 (đặc tính hiếm 0,25),
                  tối đa traitSlots[gốc]; ô trống còn lại rút từ traitPool như §6.3
mutationPct = clamp(
    baseMutation[gốc]                          // PHAM 30 · LINH 20 · HUYEN 12 · DIA 6 · THIEN 0
  + aptitudeBonus  floor((avgApt(A) + avgApt(B)) / 2 / 20)     // 0–5
  + stable         thuVienMutationBonus (cấp 3: +3)
  + catalyst       Thú Hồn Tinh: PHAM +2 · LINH +3 · HUYEN +5 · DIA +8 · THIEN +12 (tối đa 1)
  + luck           (luck − 50) × 0,05           // ±2,5 — khí vận được phép cho "kết quả thưởng thêm" (GD-29 RF-R-080)
  + pity           min(4 × breedNoMutation, 16) // ADDITIVE_PP theo tài khoản, reset khi có biến dị
, 0, 60)
Có biến dị → roll loại theo bảng SB-R-080 với gốc tương ứng.
```
**Ví dụ:** Xích Hồ LINH (tư chất TB 58) × Ngạ Lang LINH (TB 62), Thú Viên cấp 2, dùng Thú Hồn Tinh LINH, khí vận 60, đã lai 2 lần không biến dị:
- Loài: công thức Hỏa Lang, cả hai ≥ LINH → 10% Hỏa Lang, 45% Xích Hồ, 45% Ngạ Lang.
- `mutationPct = 20 + floor(60/20) (= 3) + 0 + 3 + 0,5 + 8 = 34,5%`.
- Nếu biến dị (gốc LINH): Dị sắc 30% · Tư chất +12 35% · Đặc tính hiếm 25% · Huyết mạch → HUYEN 10% ⇒ P(lên Huyền chủng) = 34,5% × 10% ≈ **3,5%**.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/beasts/params.json` | Tham số |
| `shared/gamedata/beasts/species.json` | `BeastSpeciesDef[]` |
| `shared/gamedata/beasts/traits.json` | `BeastTraitDef[]` |
| `shared/gamedata/skills/skills.json` | `SKL_BST_*` (đòn tự động + chiêu lệnh, GD-07 định dạng) |
| `shared/gamedata/items/templates/beasts_*.json` | Ngự Thú Phù, đan thú, Thú Hồn Tinh, Thú Ấn; trứng sinh tự động (`gen-eggs`, TD-07 §7) |
| `shared/gamedata/monsters/monsters.json` | Trường `beastSpeciesId` (GD-12) |

### 7.1. Tham số
```jsonc
// params.json
{
  "linhChungChance": 0.015, "linhChungMaxPerMap": 2, "linhChungStatMult": 1.3,
  "linhChungBloodlineWeights": { "LINH": 80, "HUYEN": 18, "DIA": 2 },        // @serverOnly
  "linhChungOwnerLockSec": 60,
  "captureHpThreshold": 0.30, "captureRange": 6, "captureChannelSec": 2, "captureAttemptsPerTarget": 3,
  "captureBasePp": { "LINH": 30, "HUYEN": 15, "DIA": 6 },                      // @unit pp
  "captureHpBonusMaxPp": 30, "captureSenseFactorPp": 0.2, "captureRealmStepPp": 2, "captureRealmMinPp": -20, "captureRealmMaxPp": 10,
  "captureControlBonusPp": 10, "capturePityStepPp": 5, "capturePityCapPp": 20, "captureMinPp": 5, "captureMaxPp": 90,
  "hatchMinutes": { "PHAM": 30, "LINH": 60, "HUYEN": 180, "DIA": 480, "THIEN": 720 },
  "hatchFee": { "PHAM": 50, "LINH": 200, "HUYEN": 800, "DIA": 3000, "THIEN": 10000 },
  "aptitudeMean": { "PHAM": 35, "LINH": 50, "HUYEN": 62, "DIA": 74, "THIEN": 85 },
  "aptitudeCap": { "PHAM": 60, "LINH": 75, "HUYEN": 85, "DIA": 95, "THIEN": 100 }, "mutationCapBonus": 10,
  "traitSlots": { "PHAM": 1, "LINH": 1, "HUYEN": 2, "DIA": 2, "THIEN": 3 },
  "contractSlots": 3,
  "contractRealm": { "PHAM": "LK1", "LINH": "LK3", "HUYEN": "LK7", "DIA": "TC1", "THIEN": "KD1" },
  "gradeScale": { "PHAM": 0.12, "LINH": 0.15, "HUYEN": 0.19, "DIA": 0.24, "THIEN": 0.30 },
  "bondStep": 0.03, "bondMax": 10,
  "roleHpMult": { "ASSAULT": 1.5, "GUARDIAN": 3.0, "MYSTIC": 1.2, "FORAGER": 1.2 },
  "injuredSec": 300, "summonInCombatChannelSec": 1.5,
  "lifespanByBloodline": { "PHAM": 60, "LINH": 100, "HUYEN": 160, "DIA": 250, "THIEN": 400 },
  "guardDeathLifespanCost": 10,
  "breedLimit": { "PHAM": 5, "LINH": 4, "HUYEN": 3, "DIA": 2, "THIEN": 1 },
  "breedHours": { "PHAM": 1, "LINH": 1.5, "HUYEN": 2, "DIA": 4, "THIEN": 6 },
  "breedLifespanCost": { "PHAM": 3, "LINH": 5, "HUYEN": 8, "DIA": 12, "THIEN": 20 },
  "breedFee": { "PHAM": 100, "LINH": 400, "HUYEN": 1500, "DIA": 5000, "THIEN": 15000 },
  "breedMinAgePct": 0.10,
  "baseMutationPp": { "PHAM": 30, "LINH": 20, "HUYEN": 12, "DIA": 6, "THIEN": 0 },
  "catalystPp": { "PHAM": 2, "LINH": 3, "HUYEN": 5, "DIA": 8, "THIEN": 12 },
  "mutationLuckFactorPp": 0.05, "mutationPityStepPp": 4, "mutationPityCapPp": 16, "mutationMaxPp": 60,
  "mutationTypeWeights": {
    "PHAM":  { "COLOR_VARIANT": 40, "APTITUDE_SURGE": 35, "RARE_TRAIT": 20, "BLOODLINE_UP": 5 },
    "LINH":  { "COLOR_VARIANT": 30, "APTITUDE_SURGE": 35, "RARE_TRAIT": 25, "BLOODLINE_UP": 10 },
    "HUYEN": { "COLOR_VARIANT": 20, "APTITUDE_SURGE": 30, "RARE_TRAIT": 35, "BLOODLINE_UP": 15 },
    "DIA":   { "COLOR_VARIANT": 10, "APTITUDE_SURGE": 25, "RARE_TRAIT": 40, "BLOODLINE_UP": 25 }
  },
  "aptitudeSurge": 12, "maxPowerMutations": 2,
  "traitInheritChance": 0.40, "rareTraitInheritChance": 0.25,
  "hybridChance": { "LINH": 0.10, "HUYEN": 0.20 },
  "stableSlots": [2, 4, 6, 8], "stableHatchSlots": [1, 1, 2, 3], "stableBreedMaxBloodline": [null, "HUYEN", "DIA", "THIEN"],
  "stableMutationBonusPp": [0, 0, 0, 3],
  "sealFeePct": 0.10, "tempHoldDays": 7,
  "bossCaptureHpThreshold": 0.10, "bossExhaustedSec": 8, "bossExhaustedRecoverHpPct": 0.05,
  "bossCaptureChannelSec": 3, "bossCaptureDailyAttempts": 3, "bossCaptureMinTalisman": "HUYEN",
  "bossCaptureBasePp": 3, "bossTalismanPp": { "HUYEN": 3, "DIA": 5 }, "bossSenseFactorPp": 0.05,
  "bossRealmMinPp": -10, "bossRealmMaxPp": 3, "bossPoiseBonusPp": 2,
  "bossPityStepPp": 1, "bossPityCapPp": 10, "bossCaptureMinPp": 1, "bossCaptureMaxPp": 15,
  "signatureInheritChance": 0.15
}
```

### 7.2. Loài Vực 1 (8 loài gốc + 1 lai)
| ID | Tên | Nguồn (quái) | Nhóm lai | Hệ | Vai | Huyết mạch | Chiêu lệnh (LINH) | Đặc tính thường |
|----|-----|--------------|----------|----|-----|:---:|-------------------|-----------------|
| `BST_BAMBOO_RAT` | Trúc Thử | `MON_BAMBOO_RAT` | BEAST_SMALL | MOC | FORAGER | PHAM–HUYEN | Đào hang (né + giảm aggro chủ 3s) | `BTR_DIGGER`, `BTR_HERB_NOSE` |
| `BST_GREEN_SNAKE` | Thanh Xà | `MON_GREEN_SNAKE` | SERPENT | MOC | ASSAULT | PHAM–DIA | Độc nha (`ST_POISON`) | `BTR_VENOM`, `BTR_AMBUSH` |
| `BST_WILD_BOAR` | Dã Trư | `MON_WILD_BOAR` | BEAST_QUADRUPED | THO | GUARDIAN | PHAM–HUYEN | Xung phong + khiêu khích | `BTR_THICK_HIDE`, `BTR_TAUNT` |
| `BST_RIVER_CRAB` | Giáp Giải | `MON_RIVER_CRAB` | SHELLED | THUY | GUARDIAN | PHAM–DIA | Mai giáp (khiên chủ 10%) | `BTR_SHELL`, `BTR_TIDE` |
| `BST_WATER_SPRITE` | Thủy Tinh Linh | `MON_WATER_SPRITE` | SPIRIT | THUY | MYSTIC | LINH–DIA | Cam lộ (hồi máu chủ theo thời gian) | `BTR_DEW`, `BTR_RAIN_BORN` |
| `BST_RED_FOX` | Xích Hồ | `MON_RED_FOX` | BEAST_QUADRUPED | HOA | ASSAULT | PHAM–DIA | Hồ hỏa (quạt lửa `ST_BURN`) | `BTR_SWIFT`, `BTR_FIRE_HEART` |
| `BST_HUNGRY_WOLF` | Ngạ Lang | `MON_HUNGRY_WOLF` | BEAST_QUADRUPED | KIM | ASSAULT | PHAM–DIA | Lang nha (chảy máu) | `BTR_PACK_HUNTER`, `BTR_FEROCITY` |
| `BST_NIGHT_OWL` | Dạ Kiêu | `MON_NIGHT_OWL` | BIRD | PHONG | MYSTIC | LINH–DIA | Dạ nhãn (lộ quái ẩn 10m, 8s) | `BTR_NIGHT_EYE`, `BTR_WIND_WING` |
| `BST_FLAME_WOLF` | **Hỏa Lang** (lai) | — (model sói + tint lửa) | BEAST_QUADRUPED | HOA | ASSAULT | LINH–THIEN | Liệt hỏa lang nha | thừa kế + `BTR_INFERNO_SOUL` (hiếm) |
| `BST_FOX_MATRIARCH` | **Hồ Yêu** (thú boss) | `MON_B_FOX_MATRIARCH` (scale 0,55) | BEAST_QUADRUPED | HOA | MYSTIC | HUYEN–DIA | Cửu Vĩ Hỏa Vũ (3 vùng) | đặc trưng `BTR_NINE_TAILS_FLAME`; `baseSpeciesId` = `BST_RED_FOX` |

### 7.3. Đặc tính
`effects` dùng union `TalentEffect` (GD-27 §3), giới hạn các type: `STAT_PCT`, `STAT_FLAT`, `GATHER_*`, `STATUS_CHANCE`, `CRIT_*`, `SCAN_RADIUS_FLAT` (với `appliesTo: OWNER` tối đa **+5%** mỗi đặc tính và chỉ khi thú xuất chiến). Validator chặn type khác (không có đặc tính thú chạm vào đột phá, thọ nguyên, tỷ lệ chế tác).

### 7.4. Vật phẩm
| ID | Tên | Loại | Nguồn | Công dụng |
|----|-----|------|-------|-----------|
| `ITM_TAME_TALISMAN_PHAM` / `_LINH` / `_HUYEN` | Ngự Thú Phù | `TALISMAN` (`CAPTURE_BEAST`) | Phàm: NPC Ngự Thú Sư; Linh / Huyền: luyện đan (GD-10, `RCP_TAME_TALISMAN_LINH` / `_HUYEN`) hoặc rơi đồ hiểm địa | §6.2 |
| `ITM_BEAST_PILL` | Linh Thú Đan | `PILL` | Luyện đan (GD-10, đan phương mới `RCP_BEAST_PILL`) | +thân mật |
| `ITM_BREED_PILL` | Linh Phối Đan | `MATERIAL` | Luyện đan (`RCP_BREED_PILL`) | Bắt buộc mỗi lần lai tạo |
| `ITM_BEAST_SOUL_{BLOODLINE}` | Thú Hồn Tinh | `MATERIAL` | Thú hóa linh | Xúc tác lai tạo, luyện đan |
| `ITM_EGG_{LOÀI}_{HUYẾT_MẠCH}` | Trứng {loài} | `BEAST_EGG` | Rơi đồ / cơ duyên (sinh tự động) | Ấp |
| `ITM_EGG_BRED` | Trứng lai | `BEAST_EGG` | Lai tạo | Ấp |
| `ITM_BEAST_SEAL` | Thú Ấn | `MATERIAL` (đặc biệt) | Phong ấn (Beta) | Giao dịch thú |

**Validator (CI):** (SB-V-01) `sourceMonsterId` tồn tại, quái `NORMAL` họ BEAST/SPIRIT; (SB-V-02) `commandSkillIds` mỗi bậc có `powerScore ≤ budget` tương ứng (SB-R-043); (SB-V-03) `hybridOf` hai loài cùng `breedGroup`, loài lai có `maxBloodline ≥` loài gốc; (SB-V-04) đặc tính chỉ dùng type được phép, `OWNER` ≤ 5%; (SB-V-05) trứng sinh đủ cho mọi loài × huyết mạch trong `[minBloodline, maxBloodline]`; (SB-V-06) không có trứng / phù / đan thú trong cửa hàng `JADE` (D-17); (SB-V-07) trọng số Linh chủng, tỷ lệ rơi trứng nằm trong file / trường `serverOnly` (GD-29 RF-R-041); (SB-V-08) khối `capture` chỉ có ở boss (`MON_B_*`), `speciesId` trỏ tới loài có `bossSpecies.sourceBossId` khớp; loài boss không xuất hiện trong `hybridOf`, bảng rơi trứng hay kết quả lai tạo.

---

## 8. Mục tiêu cân bằng

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| DPS thú / DPS chủ | Phàm ~4% · Huyền ~9% · Thiên tối đa ≤ 20% |
| Thời gian gặp Linh chủng đầu tiên (người chơi đi săn chủ động ở bản đồ có loài nuôi được) | 30–60 phút |
| Tỷ lệ bắt trung bình khi chuẩn bị tốt (phù đúng bậc, HP thấp, khống chế) | 60–80% |
| Số lần lai tạo để có con Huyền chủng từ hai con Linh chủng | trung bình 15–25 (nhiều tuần thực) |
| Số con Địa chủng mỗi 1.000 người chơi hoạt động / tháng | 5–15 (hiếm, có giá trên đấu giá) |

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai
| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/accounts/me/beasts` | Danh sách thú (mọi state trừ `DEPARTED`), ô Thú Viên | |
| `GET /v1/beasts/{id}` | Chi tiết + gia phả 3 đời + dấu biến dị | |
| `POST /v1/beasts/{id}/contract` · `/release-contract` | `{ characterId }` | SB-R-040; `CharacterContext` |
| `POST /v1/beasts/{id}/rename` | `{ name }` | Lọc GD-14 |
| `POST /v1/beasts/{id}/feed` | `{ itemInstanceId }` | SB-R-050 |
| `POST /v1/beasts/{id}/release` | Phóng sinh (xác nhận 2 bước) | → `DEPARTED` |
| `POST /v1/beasts/hatch/preview` · `/hatch` | `{ eggItemInstanceId }` → `ChanceView`-dạng phân phối / `{ beastId, readyAt }` | SB-R-031..032 |
| `POST /v1/beasts/breed/preview` | `{ parentA, parentB, catalystItemInstanceId? }` → `BreedQuote` | SB-R-071 |
| `POST /v1/beasts/breed` | `{ parentA, parentB, catalyst?, quoteId, requestId }` → `BreedingSession` | SB-R-072 |
| `POST /v1/beasts/breed/{sessionId}/collect` | Nhận trứng | |
| `POST /v1/beasts/{id}/seal` · `POST /v1/items/{id}/unseal` | Beta | SB-R-103 |

### 9.2. API nội bộ (Game Server → API)
| Endpoint | Request | Quy tắc |
|----------|---------|---------|
| `POST /internal/v1/beasts/capture` | `{ attemptId, characterId, monsterInstance: { speciesId, bloodline, hpPct, realmCode, controlled }, talismanItemInstanceId }` → `{ success, pct, beastId?, fleeing }` | §6.2; roll API, ghi `rng_audit` `BEAST_CAPTURE` |
| `POST /internal/v1/beasts/boss-capture` | `{ attemptId, characterId, bossMonsterId, fightId, realmCode, poiseBroken, talismanItemInstanceId }` → `{ success, pct, beastId?, attemptsLeftToday }` | §5.2b, §6.2b; idempotent theo `attemptId`; `fightId` bảo đảm một lượt / người / trận và một thú / trận; `rng_audit` `BEAST_BOSS_CAPTURE` |
| `POST /internal/v1/beasts/bond` | `{ batchId, beastId, points }` | SB-R-050, gom 60s |
| `POST /internal/v1/beasts/injure` | `{ beastId, eventId }` | SB-R-044 |
| `GET /internal/v1/characters/{id}/beast-loadout` | Thú kết khế + chỉ số tính sẵn (§6.4) | Khi vào bản đồ / `beast.changed` |

### 9.3. Message thời gian thực (TD-02)
| Message | Hướng | Payload |
|---------|-------|---------|
| `C2S_BeastSummon` | C→S | `{ seq, beastId \| null }` (null = thu về) |
| `C2S_BeastCommand` | C→S | `{ seq, aim }` (phím T) |
| `C2S_CaptureStart` / `C2S_CaptureCancel` | C→S | `{ seq, targetId, talismanItemInstanceId }` |
| `S2C_CaptureChance` | S→C | `ChanceView` (cập nhật 2 Hz khi đang nhắm) |
| `S2C_CaptureResult` | S→C | `{ success, beastId?, fleeing }` |
| `S2C_LinhChungSpotted` | S→C | `{ entityId, bloodline, speciesId, lockedToYou: boolean }` |

### 9.4. Sự kiện domain
| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `beast.obtained` | `{ accountId, beastId, origin, speciesId, bloodline }` | Nhiệm vụ, telemetry, thông báo (DIA+ toàn server ưu tiên 3) |
| `beast.mutated` | `{ accountId, beastId, kind }` | Thông báo (`BLOODLINE_UP` lên DIA/THIEN: toàn server) |
| `beast.changed` | `{ accountId, beastId, characterId? }` | Game Server làm mới loadout |
| `beast.departed` | `{ accountId, beastId, reason }` | Linh Thú Phổ, Bia Vạn Cổ |

---

## 10. Lưu trữ
```sql
CREATE TABLE beast (
  id UUID PRIMARY KEY, account_id UUID NOT NULL, species_id VARCHAR(48) NOT NULL, name VARCHAR(24) NOT NULL,
  bloodline VARCHAR(8) NOT NULL, aptitudes JSONB NOT NULL, trait_ids JSONB NOT NULL, mutations JSONB NOT NULL DEFAULT '[]',
  color_variant VARCHAR(32), age_years NUMERIC(7,2) NOT NULL DEFAULT 0, lifespan_max_years INT NOT NULL,
  bond SMALLINT NOT NULL DEFAULT 1, breed_count SMALLINT NOT NULL DEFAULT 0, generation SMALLINT NOT NULL DEFAULT 1,
  parent_a UUID, parent_b UUID, state VARCHAR(12) NOT NULL, contract_character_id UUID, injured_until TIMESTAMPTZ,
  origin VARCHAR(12) NOT NULL, born_at TIMESTAMPTZ NOT NULL DEFAULT now(), departed_at TIMESTAMPTZ, departed_reason VARCHAR(16),
  version INT NOT NULL DEFAULT 0
);
CREATE INDEX ix_beast_account ON beast(account_id, state);
CREATE INDEX ix_beast_contract ON beast(contract_character_id) WHERE contract_character_id IS NOT NULL;

CREATE TABLE beast_breeding (
  id UUID PRIMARY KEY, account_id UUID NOT NULL, parent_a UUID NOT NULL, parent_b UUID NOT NULL,
  quote JSONB NOT NULL, result JSONB NOT NULL,           -- kết quả đã roll (SB-R-072), không trả client trước khi nở
  started_at TIMESTAMPTZ NOT NULL, ready_at TIMESTAMPTZ NOT NULL, state VARCHAR(12) NOT NULL, egg_item_instance_id UUID
);
CREATE TABLE beast_pity (
  account_id UUID PRIMARY KEY, breed_no_mutation SMALLINT NOT NULL DEFAULT 0
);
CREATE TABLE beast_boss_capture (                        -- pity + lượt ngày theo tài khoản × boss (§5.2b)
  account_id UUID NOT NULL, boss_monster_id VARCHAR(48) NOT NULL,
  fails SMALLINT NOT NULL DEFAULT 0, attempts_day DATE, attempts_used SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (account_id, boss_monster_id)
);
CREATE TABLE beast_capture_pity (                         -- theo nhân vật × loài (§6.2)
  character_id UUID NOT NULL, species_id VARCHAR(48) NOT NULL, fails SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, species_id)
);
CREATE TABLE beast_log (                                  -- ledger thú (TD-01 §8)
  id BIGSERIAL PRIMARY KEY, beast_id UUID NOT NULL, account_id UUID NOT NULL, reason_code VARCHAR(32) NOT NULL,
  detail JSONB, ref_id VARCHAR(80), idempotency_key VARCHAR(80) NOT NULL UNIQUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```
`beast_log.reason_code`: `CAPTURED`, `BOSS_CAPTURED`, `HATCHED`, `BRED_OUT`, `CONTRACTED`, `CONTRACT_RELEASED`, `STABLED_ON_ENDING`, `RELEASED_WILD`, `GUARD_DEATH`, `AGED_OUT`, `SEALED`, `UNSEALED`, `RENAMED`, `FED`, `GM_ADJUST`.
`rng_audit.kind` mới: `BEAST_LINH_CHUNG` (GS), `BEAST_CAPTURE`, `BEAST_BOSS_CAPTURE`, `BEAST_HATCH`, `BEAST_BREED_SPECIES`, `BEAST_BREED_INHERIT`, `BEAST_MUTATION`, `BEAST_MUTATION_KIND`.
`item_log` mã mới: `BEAST_HATCH_CONSUMED`, `BEAST_SOUL_GRANTED`, `BEAST_SEALED`, `BEAST_UNSEALED`. `currency_ledger` (GD-13): `BEAST_HATCH_FEE`, `BEAST_BREED_FEE`, `BEAST_SEAL_FEE`, `STABLE_UPGRADE`.

---

## 11. UI/UX
- **Linh chủng:** hào quang + cột sáng mảnh theo màu huyết mạch, tiếng chuông nhẹ khi vào tầm 40m; khung tên vàng; biểu tượng khóa "của bạn 60s".
- **Ném phù:** giữ phím `F` khi nhắm Linh chủng (HP ≤ 30%): vòng tỷ lệ lớn quanh mục tiêu cập nhật theo HP; thanh vận pháp 2s; kết quả: phù sáng rồi thu thú (thành công) hoặc vỡ (thất bại).
- **Bảng Linh thú (phím `B`):** thẻ thú (model xoay, tên, huyết mạch dạng mây 1–5, tư chất 4 thanh, đặc tính, thọ còn lại, thân mật); nút xuất chiến / kết khế; **gia phả** dạng cây 3 đời với dấu biến dị.
- **Lai tạo (Linh Thú Viên):** hai ô chọn bố mẹ → khung xem trước: loài có thể ra (ảnh + %), thanh tỷ lệ biến dị và bảng đóng góp (GD-29), phân phối loại biến dị, khoảng tư chất con, chi phí + thọ bố mẹ; nút bắt đầu cần báo giá còn hạn.
- **Nở trứng:** trứng nứt dần theo thời gian; mở → khoảnh khắc lộ diện (dị sắc / biến dị có hiệu ứng riêng).
- **Màn tổng kết kiếp:** bước "Linh thú" với số ô còn trống, kéo thả thú vào Thú Viên.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-12 | `beastSpeciesId`, biến thể Linh chủng, trứng trong `LT_*` |
| GD-07 | Khung triệu hồi (không hết hạn), `SKL_BST_*`, ngân sách chiêu thú, phím `G`/`T` |
| GD-06 | Chỉ số thú tính từ chủ |
| GD-09 | `BEAST_EGG`, `CAPTURE_BEAST`, `HATCH`, Thú Ấn |
| GD-10 | Đan phương `RCP_BEAST_PILL`, `RCP_BREED_PILL`, (Linh/Huyền) Ngự Thú Phù |
| GD-13 | Phí ấp / lai tạo / phong ấn / nâng cấp Thú Viên (hố tiền); trứng, Thú Ấn trên đấu giá |
| GD-14 | Lọc tên thú; thông báo toàn server |
| GD-21 | Quét thần thức thấy Linh chủng |
| GD-22 | Outcome trứng phẩm cao, cơ duyên "linh thú bị thương" (PLAN §4.7) → thu phục không cần roll |
| GD-26 | Bước "Linh thú" khi tọa hóa, thú già theo đồng hồ nhân vật, unlock ô Thú Viên |
| GD-27 | Đặc tính dùng `TalentEffect`; thần thức (bắt), khí vận (Linh chủng, biến dị) |
| GD-28 | Trứng HUYEN/DIA trong rương hiểm địa; thú hộ chủ khi chết thật |
| GD-29 | Loại roll: bắt (A), nở (A), lai tạo (A), Linh chủng & trứng rơi (B); pity mới; khí vận được phép |
| GD-30 | Công trình `THU_VIEN` |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Hai người cùng đánh Linh chủng | Người gây sát thương đầu có quyền bắt 60s (SB-R-012) |
| Mất kết nối khi đang vận pháp ném phù vào boss | Hủy, hoàn phù, không tính lượt |
| Boss đặc biệt bị đánh trong cửa sổ Kiệt Sức | Không nhận sát thương (SB-R-026) |
| Linh chủng bị giết trước khi bắt | Rơi trứng cùng loài, huyết mạch thấp hơn 1 bậc (tối thiểu PHAM) với xác suất 30% |
| Mất kết nối khi đang vận pháp ném phù | Hủy vận pháp, phù không mất |
| Bắt thành công khi Thú Viên đầy | Vào ô tạm Ngự Thú Sư 7 ngày (SB-R-022); quá hạn → `DEPARTED` (cảnh báo qua thư 2 lần) |
| Thú già hết thọ khi đang `BREEDING` | Lai tạo vẫn hoàn thành (kết quả đã roll), thú hóa linh khi xong |
| Lai hai con cùng bố mẹ | Từ chối (SB-R-070) |
| Đổi chủ (Thú Ấn) khi thú còn kết khế | Phải giải khế trước |
| Thú xuất chiến vào hiểm địa, chủ thoát thành công | Không ảnh hưởng |
| GM cân bằng lại `gradeScale` | Áp ngay cho mọi thú (sức mạnh tính động); tư chất / đặc tính đã roll không đổi |

---

## 14. Telemetry
| Sự kiện | Trường | Câu hỏi |
|---------|--------|---------|
| `linh_chung_spawn` / `_seen` / `_captured` | map, bloodline, timeToFind | Tần suất có hợp lý (§8) |
| `capture_attempt` | pct, success, hpPct, talisman | Tỷ lệ thực tế, người chơi có chuẩn bị |
| `beast_breed` | grades, mutationPct, mutated, kind | Đường cong lai tạo |
| `beast_dps_share` | bloodline, avgApt, bond, share | Kiểm trần §8 |
| `beast_kept_on_ending` | slotsUsed, released | Áp lực chọn thú khi tọa hóa |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="sb-ac-001"></a>SB-AC-001 | Ví dụ §6.2 | Tính | `capturePct = 77` |
| SB-AC-002 | Linh chủng HUYEN, người chơi dùng phù Phàm | Ném | Từ chối (phù phải ≥ LINH) |
| SB-AC-003 | Linh chủng HP 35% | Ném | Từ chối (> 30%) |
| SB-AC-004 | Đang vận pháp ném phù, bị quái đánh trúng | Kết quả | Hủy, phù còn nguyên |
| SB-AC-005 | Thất bại lần 3 với cùng con | Sau đó | Con thú bỏ chạy, biến mất sau 5s |
| SB-AC-006 | Người B gây sát thương sau A trong 60s đầu | B ném phù | Từ chối (`NOT_OWNER`) |
| SB-AC-007 | Phàm chủng tư chất TB 35, thân mật 1; Thiên chủng TB 100, thân mật 10 | Đo DPS thú / chủ trong mô phỏng | ≈ 4% và ≈ 17% (± 1%) |
| SB-AC-008 | Thiên chủng mang sang kiếp mới, chủ `LK1` | Kết khế | Từ chối (cần `KD1`) |
| SB-AC-009 | Huyền chủng, chủ mới `LK7` | Xuất chiến | Chỉ số thú tính từ chỉ số chủ `LK7` |
| SB-AC-010 | Ví dụ §6.5 | Xem trước lai tạo | Hỏa Lang 10%, biến dị 34,5%, P(lên HUYEN) ≈ 3,5% |
| SB-AC-011 | Lai hai con có chung bố | Xem trước | Từ chối |
| SB-AC-012 | Bắt đầu lai tạo | Kiểm tra | Bố mẹ trừ thọ đúng `breedLifespanCost`, `breedCount + 1`, kết quả đã roll lưu trong `beast_breeding.result`, không có trong phản hồi |
| SB-AC-013 | Hai con THIEN | Xem trước | Tỷ lệ biến dị 0 (trừ dị sắc qua cơ duyên) |
| SB-AC-014 | Biến dị `BLOODLINE_UP` khi loài đã ở `maxBloodline` | Kết quả | Đổi thành `RARE_TRAIT` |
| SB-AC-015 | Thú đã có 2 biến dị sức mạnh | Biến dị `APTITUDE_SURGE` | Đổi thành `COLOR_VARIANT` |
| SB-AC-016 | 3 lần lai tạo liền không biến dị | Lần thứ 4 xem trước | Dòng pity +12 pp |
| SB-AC-017 | Khí vận 20 và 80, cùng cặp | Xem trước lai tạo | Chênh 3 pp (dòng `luck`); tỷ lệ **bắt** không đổi theo khí vận |
| SB-AC-018 | Nhân vật tọa hóa với 3 thú kết khế, Thú Viên còn 1 ô | Màn tổng kết | Chọn 1 thú giữ; 2 thú còn lại `DEPARTED` (`RELEASED_WILD`) |
| SB-AC-019 | Chết thật ở hiểm địa với thú xuất chiến | Sau đó | Thú −10 năm thọ, vào danh sách chọn |
| SB-AC-020 | Không có nhân vật sống trên tài khoản 3 ngày | Kiểm tra tuổi thú | Không đổi |
| SB-AC-021 | Thú hết thọ | Kết quả | `DEPARTED` (`AGED_OUT`), nhận `ITM_BEAST_SOUL_{bloodline}` |
| SB-AC-022 | Thú bị đánh gục | Gọi lại ngay | Từ chối tới khi hết `injuredSec` |
| SB-AC-023 | Cửa hàng `JADE` có `ITM_EGG_*` | Validator | Fail SB-V-06 |
| SB-AC-024 | Bundle client | Tìm `linhChungBloodlineWeights` | Không có (`serverOnly`) |
| SB-AC-025 | Mô phỏng 1 triệu lần spawn quái nuôi được, khí vận 50 | Đếm Linh chủng | 1,5% ± 0,03% |
| SB-AC-026 | Ví dụ §6.2b | Tính | `bossCapturePct = 10,5` |
| SB-AC-027 | Hồ Yêu Mẫu HP chạm 10% | Trận đấu | Kiệt Sức 8s, boss không đánh, không nhận sát thương; chỉ một lần mỗi trận |
| SB-AC-028 | Ném phù ngoài cửa sổ Kiệt Sức / phù bậc LINH | Ném | Từ chối |
| SB-AC-029 | Tổ 3 người, A và B cùng vận pháp, A xong trước và thành công | Kết quả | A nhận thú boss, không nhận đồ boss; B được hoàn phù, không tính lượt; B và C nhận đồ boss bình thường |
| SB-AC-030 | Cả tổ ném trượt | Hết cửa sổ | Boss hồi 5% HP, trận tiếp tục; mỗi người mất phù + 1 lượt ngày; pity tài khoản +1 |
| SB-AC-031 | Tài khoản đã dùng 3 lượt với Hồ Yêu Mẫu trong ngày | Ném lần 4 | Từ chối (`DAILY_LIMIT`) |
| SB-AC-032 | 10 lần trượt trước đó | Xem tỷ lệ | Dòng pity +10, tổng ≤ 15% |
| SB-AC-033 | Lai Hồ Yêu × Ngạ Lang | Xem trước | Loài con chỉ Xích Hồ / Ngạ Lang (không có Hồ Yêu); đặc trưng kế thừa 15% |
| SB-AC-034 | Thú boss | Kiểm tra | Có `BTR_NINE_TAILS_FLAME` ở ô riêng; `powerScore` chiêu lệnh ≤ budget theo huyết mạch |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Alpha | Beta | Sau launch |
|----------|:---:|:----:|:----------:|
| Dữ liệu loài + trứng rơi ẩn (`phase: BETA`), không bật | ✅ | | |
| Linh chủng, bắt, trứng, ấp tại Ngự Thú Sư | | ✅ | |
| Kết khế, xuất chiến 1 thú, chiêu lệnh, thân mật | | ✅ | |
| Linh Thú Viên (GD-30), lai tạo, biến dị, lai loài Hỏa Lang | | ✅ | |
| Bắt boss đặc biệt (Hồ Yêu Mẫu) | | ✅ | |
| Thêm boss đặc biệt ở Vực 2+, world boss | | | ✅ |
| Bước "Linh thú" khi tọa hóa, Linh Thú Phổ | | ✅ | |
| Thú Ấn + giao dịch thú | | ✅ (cuối Beta) | |
| Thú cưỡi, trang bị thú, tiến hóa ngoại hình theo thân mật, thú ở Vực 2+ | | | ✅ |
| Kéo dài thọ thú, đấu thú | | | ✅ |

---

## 17. Câu hỏi mở
- [ ] **SB-Q-1:** Có cho thú xuất chiến **2 con** ở Kim Đan+ (tăng chiều sâu, nhưng phải chia `quality`)?
- [ ] **SB-Q-2:** Có vật phẩm kéo dài thọ thú (hiếm, từ cơ duyên) không? Ảnh hưởng nhịp lai tạo thế hệ mới.
- [ ] **SB-Q-3:** Có bảng xếp hạng huyết mạch / "Thần Thú Bảng" (danh tiếng cho người lai tạo) như GD-08 TF-Q-4?
- [ ] **SB-Q-4:** Có nên cho tổ đội chia sẻ quyền bắt Linh chủng (người bắt được chọn bằng roll trong tổ)?
- [ ] **SB-Q-6:** World boss (GD-23) có nên là boss đặc biệt với cơ chế "đấu giá quyền ném phù" trong cửa sổ Kiệt Sức (người đóng góp cao nhất được ném) không?
- [ ] **SB-Q-5:** Linh chủng có nên có cơ chế "đánh động" (bỏ chạy khi thấy người chơi ở xa) để thêm kỹ năng tiếp cận (lén, khống chế)?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-23 | — | Bản nháp đầu (tách khỏi GD-25 cũ "Linh thú, Ngự kiếm"; Ngự kiếm chuyển sang GD-31): đánh giá, Linh chủng + bắt (loại A, thần thức, pity theo loài), trứng + ấp, kết khế / xuất chiến / chiêu lệnh / thân mật, sức mạnh tính từ chủ (trần ~17%), Linh Thú Viên, lai tạo + di truyền + biến dị theo độ hiếm + lai loài, thú khi chủ tọa hóa, thọ nguyên thú, Thú Ấn, 9 loài Vực 1, API, DB, nghiệm thu |
| v0.2 | 2026-10-23 | — | **Bắt boss đặc biệt** (§5.2b, §6.2b): cửa sổ Kiệt Sức 8s khi HP ≤ 10%, phù ≥ Huyền, tỷ lệ 1–15% + pity theo tài khoản, 1 lượt / người / trận và 3 lượt / ngày, **đổi lấy phần rơi đồ boss**, thú boss có đặc tính đặc trưng, con lai không bao giờ ra loài boss; loài `BST_FOX_MATRIARCH` |
| v0.3 | 2026-10-23 | — | SB-R-042: thú trong PvP (GD-32) |

