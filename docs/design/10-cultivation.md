# GD-04 — Hệ thống Tu luyện

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-04 · Prefix ID: `CU` |
| Owner | Game Designer |
| Trạng thái | 🟡 Bản nháp v0.6 |
| Cần trước | Phase 1 (tu vi, tầng nhỏ), Phase 2 (đả tọa, đan độc), Phase 3 (đột phá bằng vật phẩm, thiên kiếp) |
| Quyết định nền | PLAN D-03, D-05, D-11 |
| Liên quan | [PLAN §4.4](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-27 Thiên phú](04-talents.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-21 Open World](22-open-world.md) · GD-06 Chỉ số · GD-07 Kỹ năng · GD-10 Luyện đan · GD-12 Quái · GD-28 Hiểm địa |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Tham số nằm ở `shared/gamedata/cultivation/*.json` (§7).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-04 | Mục |
|--------------|-----------------|-----|
| GD-26 | `stageRequirementExp`, bình cảnh, thất bại đột phá (tiêu thọ), thiên kiếp lethal (`LethalEvent`), đột phá trong LAST_LIGHT, `cultivationSpeed` cho bế quan | §5.2, §5.6–5.8, §6.1 |
| GD-27 | Gọi thức tỉnh sau đột phá, chặn đột phá khi còn lựa chọn chờ, hiệu ứng `BREAKTHROUGH_CHANCE_FLAT`, `TRIBULATION_POWER_PCT`, `PILL_TOXICITY_PCT`, `CULTIVATION_SPEED_PCT`; định nghĩa chuẩn của tâm cảnh & tâm ma | §5.6, §5.9, §6 |
| GD-22 | `GRANT_CULTIVATION`, `CHANGE_HEART_STATE`; trigger `MEDITATE`; loại cơ duyên `HEART_TRIAL` và outcome `MODIFY_BREAKTHROUGH_CHANCE` | §5.3, §5.7 |
| GD-21 | Đả tọa tại linh mạch, giờ Tý / Ngọ, linh vũ | §5.3 |

---

## 1. Mục tiêu

**Trụ cột:** P2 *Cảm giác tu tiên thật sự* (chính), P1 *Mỗi kiếp một câu chuyện*, P4 *Chế tác có chiều sâu*.

**Triết lý:** *"Tu tiên là tranh đoạt tài nguyên với trời đất."* Đánh quái chỉ là một phần nhỏ; tiến bộ chủ yếu đến từ **tìm kiếm vật phẩm, luyện đan và chuẩn bị đột phá**.

**Người chơi cần cảm thấy:**
- Mỗi lần đột phá cảnh giới lớn là **khoảnh khắc lớn nhất của kiếp**: phải chuẩn bị, hồi hộp, có thể thất bại.
- Thời gian (thọ nguyên) và tài nguyên đều là đánh đổi: đả tọa chậm mà rẻ, bế quan nhanh mà tốn thọ, đan dược nhanh nhưng tích độc.
- Không thể "cày quái" lên cảnh giới cao, nhưng cũng không bị phạt nếu thích chiến đấu.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="cu-r-001"></a>**CU-R-001** | Mọi thay đổi tu vi đi qua **một cổng duy nhất** `cultivation/exp` (§9.1) trên API Service, idempotent, ghi `cultivation_ledger`. |
| <a id="cu-r-002"></a>**CU-R-002** | Tu vi **không vượt bình cảnh**. Phần vượt bị bỏ (ghi `wasted`), không cộng dồn chờ đột phá. |
| <a id="cu-r-003"></a>**CU-R-003** | Đột phá cảnh giới lớn **luôn** cần: tu vi viên mãn + đan đột phá. Không có cách nào khác (kể cả GM, trừ `GM_ADJUST` có log). |
| <a id="cu-r-004"></a>**CU-R-004** | Tỷ lệ thành công đột phá và **từng thành phần** cộng / trừ của nó luôn được hiển thị trước khi người chơi xác nhận. Định dạng: `ChanceView` + báo giá `quoteId` ([GD-29 §5.2](05-rng-fairness.md)). |
| <a id="cu-r-005"></a>**CU-R-005** | Chỉ thiên kiếp có `lethal = true` (Nguyên Anh trở lên) mới có thể gây chết thật (GD-26 LC-R-001). Thất bại ở mọi bước khác chỉ gây hậu quả §5.8. |

---

## 2. Tổng quan

```
                  NGUỒN TU VI (§5.3–5.5)                          tỷ trọng mục tiêu
   đan dược ────────────── × cultivationSpeed, × hiệu quả đan độc ── 30–35%
   đả tọa / bế quan ────── × cultivationSpeed, × địa điểm / giờ ──── 10–15%
   thiên tài địa bảo, cơ duyên ── % yêu cầu tầng ──────────────────── 15–20%
   đánh quái ───────────── chênh cảnh giới, mệt mỏi 24h ──────────── 20–25%
   nhiệm vụ ────────────── cố định ───────────────────────────────── ~5%
                          │
                          ▼
   TẦNG NHỎ: đủ tu vi → tự lên tầng, phần dư chuyển sang tầng sau (§5.2)
                          │
                          ▼
   BÌNH CẢNH (tầng cuối mỗi cảnh giới lớn): tu vi đầy thì dừng, phần dư bị bỏ
                          │
                          ▼
   ĐỘT PHÁ (§5.6): chuẩn bị → vận công 30s → [tâm ma] → roll tỷ lệ → [thiên kiếp] → kết quả
        thành công: cảnh giới mới, thọ nguyên mới (GD-26), thức tỉnh (GD-27), tâm cảnh +
        thất bại:   mất đan, tổn thọ, mất tu vi, tâm cảnh −, có thể nhận tâm ma
        chết:       chỉ khi thiên kiếp lethal (Nguyên Anh+)
```

---

> Tỷ trọng mục tiêu chỉnh lại 2026-10-25 theo đối chiếu GD-03 (C3–C5, LOOP-R-010); Balance Simulator kiểm chứng ở Phase 3.

## 3. Thực thể & kiểu dữ liệu

```ts
type Realm = 'PHAM_NHAN' | 'LUYEN_KHI' | 'TRUC_CO' | 'KIM_DAN' | 'NGUYEN_ANH' | 'HOA_THAN';
type ExpSource = 'MONSTER' | 'MEDITATION' | 'SECLUSION' | 'PILL' | 'TREASURE'
               | 'ENCOUNTER' | 'QUEST' | 'BREAKTHROUGH_FAILED' | 'SEVERE_INJURY' | 'GM_ADJUST';
type MeditationLocation = 'LEY_LINE' | 'HUB' | 'OTHER' | 'CAVE' | 'SECT_GROUNDS';   // CAVE, SECT_GROUNDS: GD-30
type PillQuality = 'HA' | 'TRUNG' | 'THUONG' | 'CUC';           // Hạ / Trung / Thượng / Cực phẩm (GD-10)

interface CultivationState {                // lưu trong bảng character
  characterId: string;
  realm: Realm; realmStage: number;         // RealmCode = viết tắt realm + stage
  cultivationExp: number;                   // tu vi trong tầng hiện tại, 0..requirementExp
  heartState: number;                       // tâm cảnh 0..heartStateCap (§5.9)
  pillToxicity: number;                     // đan độc 0..100 (§5.4)
  toxicityUpdatedAt: string;                // để tính suy giảm theo thời gian
  breakthroughFailures: Record<string, number>;  // theo cảnh giới đích, vd. { "TRUC_CO": 1 }
  lastBreakthroughFailAt: string | null;
}

interface StageDef {                        // stages.json
  realmCode: string;                        // "LK5"
  stageIndex: number;                       // chỉ số toàn cục (§6.2)
  requirementExp: number;
  isBottleneck: boolean;                    // tầng cuối của cảnh giới lớn
}

interface BreakthroughDef {                 // breakthroughs.json
  targetRealm: Realm;                       // cảnh giới đích
  fromRealmCode: string;                    // "LK9"
  pillItemIds: string[];                    // đan đột phá chấp nhận được
  baseChance: number;                       // điểm %
  heartTrial: 'NEVER' | 'ALWAYS';           // ngoài ra luôn bắt buộc khi đủ 3 tầng tâm ma (CU-R-064)
  tribulation: TribulationDef | null;
  failure: { lifespanYears: number; expLossPct: number; heartDelta: number;
             weakenedSec: number; heartDemonChance: number };
  success: { heartDelta: number; awaken: boolean; broadcast: boolean };
}

interface TribulationDef {
  mapId: string;                            // instance đấu trường, vd. "TRIB_TC"
  lethal: boolean;
  waves: number; strikesPerWave: number;
  strikeDamagePctMaxHp: number;             // sát thương mỗi tia sét theo % HP tối đa
  telegraphSec: number;                     // thời gian báo trước vòng sét
  maxHealingItems: number;
}

type BreakthroughState =
  | 'PREPARED' | 'CHANNELING' | 'HEART_TRIAL' | 'ROLLED' | 'TRIBULATION'
  | 'SUCCEEDED' | 'FAILED' | 'DIED' | 'ABORTED';

interface ChanceBreakdown {                 // hiển thị từng dòng (CU-R-004)
  base: number; pill: number; root: number; heart: number; support: number;
  location: number; guardians: number; talents: number; failurePity: number;
  heartDemon: number; toxicity: number; heartTrial: number; channelHits: number;
  total: number;                            // sau clamp
}

interface BreakthroughAttempt {
  attemptId: string; characterId: string;
  targetRealm: Realm; fromRealmCode: string;
  state: BreakthroughState;
  pillItemInstanceId: string; pillQuality: PillQuality;
  supportItemInstanceIds: string[];
  guardianCharacterIds: string[];
  location: { mapId: string; kind: MeditationLocation; poiId: string | null };
  chance: ChanceBreakdown;
  heartTrialSessionId: string | null;       // EncounterSession (GD-22)
  roll: number | null; rollPassed: boolean | null;
  tribulation: { survived: boolean; hpPctEnd: number; disconnected: boolean } | null;
  createdAt: string; resolvedAt: string | null;
}
```

---

## 4. Máy trạng thái `BreakthroughAttempt`

| Từ | Đến | Điều kiện | Tác dụng phụ |
|----|-----|-----------|--------------|
| — | `PREPARED` | Điều kiện CU-R-060 thỏa | Tính `chance` (chưa gồm tâm ma / trúng đòn) |
| `PREPARED` | `ABORTED` | Người chơi hủy | Không tiêu gì |
| `PREPARED` | `CHANNELING` | Người chơi xác nhận (lethal: xác nhận hai lần, CU-R-071) | **Tiêu đan đột phá + vật phẩm phụ trợ** (item_log `BREAKTHROUGH_CONSUMED`); khóa di chuyển 30s |
| `CHANNELING` | `HEART_TRIAL` | Hết `channelSec` và cần thử thách tâm ma (CU-R-064) | Mở `EncounterSession` loại `HEART_TRIAL` (GD-22) |
| `CHANNELING` | `ROLLED` | Hết `channelSec`, không cần tâm ma | Roll tỷ lệ |
| `CHANNELING` | `FAILED` | HP về 0 trong lúc vận công | Hậu quả thất bại §5.8 (không thêm trọng thương) |
| `HEART_TRIAL` | `ROLLED` | Phiên tâm ma `RESOLVED` / `ABORTED` | Cộng `heartTrial` vào `chance`, rồi roll |
| `ROLLED` | `TRIBULATION` | Roll đạt và `tribulation ≠ null` | Đưa vào instance thiên kiếp |
| `ROLLED` | `SUCCEEDED` | Roll đạt và `tribulation == null` | §5.7 |
| `ROLLED` | `FAILED` | Roll trượt | §5.8 |
| `CHANNELING` / `ROLLED` / `TRIBULATION` | `DIED` | Thất bại (bất kỳ lý do nào) khi đang `LAST_LIGHT` | CU-R-083: GD-26 kết thúc kiếp với `LIFESPAN_EXHAUSTED` |
| `TRIBULATION` | `SUCCEEDED` | Sống sót hết các đợt sét | §5.7 |
| `TRIBULATION` | `FAILED` | HP về 0, `lethal = false`; hoặc mất kết nối quá hạn (CU-R-076) | §5.8 |
| `TRIBULATION` | `DIED` | HP về 0, `lethal = true` và GD-26 trả `DIED` | GD-26 §5.8 |

`SUCCEEDED`, `FAILED`, `DIED`, `ABORTED` là trạng thái cuối. Nếu GD-26 trả `WARD_CONSUMED` khi chết trong thiên kiếp lethal, attempt chuyển `FAILED` (sống sót nhưng đột phá thất bại).

---

## 5. Quy tắc

### 5.1. Cảnh giới và tầng

| Cảnh giới | Mã | Tầng | Thọ nguyên (GD-26) | Mở khóa gameplay |
|-----------|----|:----:|:------------------:|------------------|
| Phàm nhân | `PN` | 1 | 80 | Hướng dẫn; chưa có kỹ năng công pháp |
| Luyện Khí | `LK` | 9 | 120 | Combat cơ bản, 4 ô kỹ năng, hiểm địa nhất giai |
| Trúc Cơ | `TC` | 4 (Sơ · Trung · Hậu · Viên Mãn) | 250 | Ô kỹ năng thứ 5, khinh công (né lướt 2 lần), luyện đan cấp 2, chế công pháp Hoàng – Huyền ([GD-08 TF-R-011](14-technique-crafting.md#tf-r-011)), ngự kiếm (Beta, + nhiệm vụ `QC_SWORD_FLIGHT` — GD-31), hiểm địa nhị giai |
| Kim Đan | `KD` | 4 | 500 | Pháp bảo bản mệnh, chế công pháp phẩm Địa, ô tuyệt kỹ, hiểm địa tam giai |
| Nguyên Anh | `NA` | 4 | 1.000 | Nguyên Anh xuất khiếu (sau launch) |
| Hóa Thần | `HT` | 4 | 2.000 | Hướng tới Phi Thăng (sau launch) |

- <a id="cu-r-010"></a>**CU-R-010** Chỉ số cơ bản theo từng `RealmCode` do GD-06 định nghĩa (`base_stats.json`). GD-04 chỉ quyết định khi nào `RealmCode` thay đổi.
- <a id="cu-r-011"></a>**CU-R-011** Các mở khóa gameplay ở bảng trên được kiểm tra bằng hàm `realmAtLeast(realmCode)` dùng chung (so sánh theo `stageIndex`, §6.2).

### 5.2. Tu vi, tầng nhỏ và bình cảnh
- <a id="cu-r-020"></a>**CU-R-020** Khi `cultivationExp ≥ requirementExp` của tầng hiện tại và tầng đó **không phải** bình cảnh: lên tầng kế, `cultivationExp −= requirementExp`, lặp lại (một lần cộng có thể lên nhiều tầng).
- <a id="cu-r-021"></a>**CU-R-021** Bình cảnh: `LK9`, `TC4`, `KD4`, `NA4`, `HT4`. Tại bình cảnh, `cultivationExp` bị chặn ở `requirementExp`; phần dư ghi `wasted` (CU-R-002).
- <a id="cu-r-022"></a>**CU-R-022** Trước khi người chơi **chủ động** dùng nguồn tu vi (dùng đan, bế quan, dùng thiên tài địa bảo), API trả bản xem trước gồm tu vi dự kiến và phần sẽ lãng phí; UI cảnh báo nếu `wasted > 0`.
- <a id="cu-r-023"></a>**CU-R-023** `PN1 → LK1` (dẫn khí nhập thể) là bước hướng dẫn: khi `cultivationExp ≥ 100` và hoàn thành nhiệm vụ hướng dẫn `QC_TUTORIAL_QI`, chuyển `LK1` với tỷ lệ 100%, không cần đan, không thiên kiếp. Chuỗi hướng dẫn: [GD-15 §5.8](21-quests.md) (`QC_TUTORIAL_QI`, hành động `ADVANCE_TUTORIAL_REALM`).
- <a id="cu-r-024"></a>**CU-R-024** Mất tu vi (thất bại đột phá, trọng thương GD-26 LC-R-061) không bao giờ làm tụt tầng: `cultivationExp = max(0, cultivationExp − loss)`.

### 5.3. Đả tọa
- <a id="cu-r-030"></a>**CU-R-030** Bắt đầu đả tọa (phím `Z`) khi: không trong chiến đấu, đứng yên, bản đồ không phải `DANGER_ZONE`/`DUNGEON`. Dừng khi di chuyển, tấn công, nhận sát thương, hoặc mở giao dịch.
- <a id="cu-r-031"></a>**CU-R-031** Địa điểm:
  - `LEY_LINE`: trong bán kính `params.leyLineRadius` (6m) của POI `LEY_LINE` (GD-21) → hệ số 1,5.
  - `HUB`: trong khu đả tọa được đánh dấu ở thành trấn → hệ số 1,0.
  - `CAVE`: Tĩnh Thất trong động phủ của chính mình → `min(1,0 + 0,05 × (L − 1) + 0,05 × (gateGrade − 1), 1,25)` (GD-30 §6.1).
  - `SECT_GROUNDS`: Linh Mạch Điện trong trú địa tông môn → `min(1,0 + 0,05 × L + siteBonus, 1,35)` (GD-30 §6.1).
  - `OTHER`: mọi nơi khác → hệ số 0,7.
  - Mọi địa điểm trong khu an toàn (`HUB`, `CAVE`, `SECT_GROUNDS`) có hệ số **< 1,5** của `LEY_LINE` vì tuổi chỉ trôi ×0,25 ở đó (GD-30 HM-R-004).
- <a id="cu-r-032"></a>**CU-R-032** Tu vi đả tọa theo §6.3. Giờ Tý / Ngọ ×1,10 (GD-21 §3.8); linh vũ ×1,25 (GD-21 §3.9); nhân với nhau.
- <a id="cu-r-033"></a>**CU-R-033** **Thời gian khi đả tọa:** đả tọa **không bị tính là AFK** (sửa đổi GD-26 LC-R-031): tuổi trôi theo loại bản đồ (`FIELD` ×1,0; `HUB` ×0,25). Như vậy linh mạch ngoài đồng tu nhanh hơn nhưng già nhanh hơn thành trấn, một đánh đổi có chủ đích.
- <a id="cu-r-034"></a>**CU-R-034** Sau `params.meditationPromptSec` (1800s) đả tọa liên tục, hiện hộp thoại "Tiếp tục tĩnh tọa?"; không phản hồi trong 60s thì dừng (chống bot).
- <a id="cu-r-035"></a>**CU-R-035** Đả tọa tại `LEY_LINE`: +1 tâm cảnh mỗi 3600s đả tọa, chỉ khi `heartState < params.meditationHeartCap` (70).
- <a id="cu-r-036"></a>**CU-R-036** Game Server báo cáo đả tọa theo cửa sổ (§9.1) giống báo cáo tuổi của GD-26 (kiểm tra cửa sổ không chồng lấn, tổng giây ≤ độ dài cửa sổ + 5s). Mỗi 600s đả tọa là một cơ hội `MEDITATE` cho GD-22.
- <a id="cu-r-037"></a>**CU-R-037** **Bế quan** (đổi thọ nguyên lấy tu vi tức thì / offline) do GD-26 §5.13 định nghĩa; tu vi tính theo §6.4 dùng `cultivationSpeed` của tài liệu này.

### 5.4. Đan dược và đan độc
- <a id="cu-r-040"></a>**CU-R-040** Đan tu luyện (item category `PILL`, có trường `cultivation`) cho tu vi theo §6.5; yêu cầu `realmMin ≤ RealmCode ≤ realmMax` của đan.
- <a id="cu-r-041"></a>**CU-R-041** Mỗi viên cộng đan độc `toxicity × qualityToxMult × (1 + Σ PILL_TOXICITY_PCT)`; `pillToxicity` chặn trong `[0, 100]`.
- <a id="cu-r-042"></a>**CU-R-042** Hiệu quả của đan tu luyện giảm theo đan độc **trước khi** dùng viên đó: `toxEfficiency = 1 − params.toxEfficiencyLoss (0,6) × pillToxicity / 100`.
- <a id="cu-r-043"></a>**CU-R-043** Không dùng được đan tu luyện khi `pillToxicity ≥ params.toxHardCap` (100). Đan hồi phục (HP/MP) và đan giải độc không bị chặn.
- <a id="cu-r-044"></a>**CU-R-044** Đan độc giảm `params.toxDecayPerRealHour` (4) mỗi giờ **thực** (kể cả offline), tính lười khi đọc / ghi từ `toxicityUpdatedAt`.
- <a id="cu-r-045"></a>**CU-R-045** Giải Độc Đan (`ITM_GIAI_DOC_DAN`) giảm 30 đan độc.
- <a id="cu-r-046"></a>**CU-R-046** Đan độc làm giảm tỷ lệ đột phá: `−floor(pillToxicity / 10)` điểm % (§6.6).
- <a id="cu-r-047"></a>**CU-R-047** Đan đột phá không cộng đan độc.

### 5.5. Đánh quái, thiên tài địa bảo, cơ duyên, nhiệm vụ
- <a id="cu-r-050"></a>**CU-R-050** Tu vi từ quái tính ở **API Service** theo §6.7 từ báo cáo hạ quái của Game Server (gom mỗi `params.monsterBatchSec` (10s)). Quái thấp hơn ≥ 3 tầng cho 0 tu vi. Ai được tính công (mỗi người nhận trọn phần, không chia) theo [GD-12 EN-R-100](18-enemies-ai.md#en-r-100).
- <a id="cu-r-051"></a>**CU-R-051** **Mệt mỏi 24h** theo tổng tu vi từ quái trong 24 giờ thực gần nhất (cửa sổ trượt), so với `requirementExp` tầng hiện tại (§6.7).
- <a id="cu-r-052"></a>**CU-R-052** Tu vi từ quái **không** nhân `cultivationSpeed` (chiến đấu công bằng giữa các linh căn); vẫn bị chặn bởi bình cảnh.
- <a id="cu-r-053"></a>**CU-R-053** Thiên tài địa bảo (vật phẩm có `cultivation.expPctOfStage`): tu vi = `expPctOfStage × requirementExp` tầng hiện tại, **không** cộng đan độc, có `realmMin`/`realmMax`.
- <a id="cu-r-054"></a>**CU-R-054** Outcome `GRANT_CULTIVATION` của GD-22: như CU-R-053 với `pctOfStage`; không nhân `cultivationSpeed`.
- <a id="cu-r-055"></a>**CU-R-055** Nhiệm vụ cho tu vi cố định ghi trong dữ liệu nhiệm vụ (GD-15); không nhân `cultivationSpeed`. Công thức mặc định: [GD-15 §6.1](21-quests.md).

### 5.6. Đột phá cảnh giới lớn: điều kiện và tỷ lệ
- <a id="cu-r-060"></a>**CU-R-060** Điều kiện bắt đầu (`PREPARED`):
  1. Đang ở bình cảnh và `cultivationExp == requirementExp` (viên mãn).
  2. Có ít nhất một đan trong `pillItemIds` của `BreakthroughDef` trong `BAG`.
  3. Không có `PendingTalentChoice(AWAKEN)` chưa giải quyết (GD-27 TLN-R-081).
  4. Không trong chiến đấu; bản đồ không phải `DANGER_ZONE`, `DUNGEON`.
  5. Đã qua `params.breakthroughFailCooldownSec` (900s thực) kể từ lần thất bại trước, **trừ khi đang `LAST_LIGHT`**: khi đó được thử lại ngay (CU-R-083).
  6. `lifeState == ALIVE` (được phép khi đang `LAST_LIGHT`, GD-26 LC-R-051).
- <a id="cu-r-061"></a>**CU-R-061** Tỷ lệ thành công tính theo §6.6, chặn trong `[params.minChance (5), params.maxChance (95)]`. Mọi thành phần trả về trong `ChanceBreakdown`.
- <a id="cu-r-062"></a>**CU-R-062** **Vật phẩm phụ trợ:** tối đa 3, mỗi vật phẩm có `breakthroughBonus` (điểm %); tổng tối đa `params.supportBonusCap` (25).
- <a id="cu-r-063"></a>**CU-R-063** **Hộ pháp:** thành viên tổ đội trong bán kính 15m, đang `ALIVE`, không trong chiến đấu lúc xác nhận: +3 mỗi người, tối đa +9. Trong lúc vận công, quái tấn công người đột phá; hộ pháp có nhiệm vụ chặn.
- <a id="cu-r-064"></a>**CU-R-064** **Thử thách tâm ma** bắt buộc khi `BreakthroughDef.heartTrial == ALWAYS` **hoặc** nhân vật có đủ 3 tầng `HEART_DEMON`.
- <a id="cu-r-065"></a>**CU-R-065** **Trúng đòn khi vận công:** mỗi lần nhận sát thương trong `CHANNELING` trừ 2 điểm, tối đa −20 (`channelHits`). HP về 0 thì thất bại ngay (§4).

### 5.7. Đột phá: thử thách tâm ma, thiên kiếp, thành công
- <a id="cu-r-070"></a>**CU-R-070** Thử thách tâm ma dùng engine cơ duyên (GD-22) với loại `HEART_TRIAL`:
  - Chọn ngẫu nhiên có trọng số một `EncounterDef` loại `HEART_TRIAL` thỏa điều kiện (cảnh giới đích, cờ kiếp, thiên phú...). Ví dụ: cờ `FLAG_KILLED_FOX` mở tâm ma "Hồ yêu oán hận".
  - Kịch bản chỉ được dùng outcome `MODIFY_BREAKTHROUGH_CHANCE` (−20..+15) và `CHANGE_HEART_STATE`.
  - Không có giới hạn tần suất, không pity, không tính cooldown cơ duyên thường.
  - Mất kết nối quá `params.heartTrialTimeoutSec` (300s) → coi như chọn lựa chọn mặc định (outcome 0).
- <a id="cu-r-071"></a>**CU-R-071** Đột phá có thiên kiếp `lethal = true`: màn xác nhận ghi rõ *"Độ kiếp thất bại sẽ thân tử đạo tiêu"*, hiện tình trạng Thế Thân Phù, yêu cầu xác nhận hai lần (`confirmLethal = true`). Trong instance thiên kiếp lethal có nút **Binh giải** (GD-26 §5.16): tự kết thúc kiếp với ưu đãi như tọa hóa, thay vì liều tới cùng.
- <a id="cu-r-072"></a>**CU-R-072** **Thiên kiếp:** instance solo `TribulationDef.mapId` (đấu trường tròn 30m); thời gian tuổi `PAUSED` (GD-26). Mỗi đợt có `strikesPerWave` tia sét; mỗi tia báo trước bằng vòng tròn trên mặt đất `telegraphSec` (0,8s), sát thương `strikeDamagePctMaxHp × maxHp × (1 + Σ TRIBULATION_POWER_PCT)`.
- <a id="cu-r-073"></a>**CU-R-073** Trong thiên kiếp được: né, dùng pháp bảo hộ thân (khiên hấp thụ), dùng tối đa `maxHealingItems` (3) đan hồi phục. Không nhận hỗ trợ từ người khác.
- <a id="cu-r-074"></a>**CU-R-074** **Thiên lôi tẩy thể:** vượt thiên kiếp với HP ≥ 50% → `CHANGE_INNATE_STAT constitution +1` qua GD-27 (nguồn `EVENT`, ref `attemptId`).
- <a id="cu-r-075"></a>**CU-R-075** HP về 0 trong thiên kiếp: `lethal = false` → `FAILED`; `lethal = true` → gửi `LethalEvent(cause = TRIBULATION)` tới GD-26 và theo kết quả trả về (§4).
- <a id="cu-r-076"></a>**CU-R-076** Mất kết nối trong thiên kiếp: tia sét **dừng** (tạm dừng instance) tối đa `params.tribulationPauseSec` (120s). Kết nối lại thì tiếp tục từ đợt đang dở. Quá hạn → `FAILED` (không chết, kể cả lethal), thêm 1 tầng `HEART_DEMON`, gắn `needsReview`. Tài khoản có ≥ 2 lần quá hạn trong 30 ngày thì lần sau **không** được tạm dừng.
- <a id="cu-r-077"></a>**CU-R-077** **Thành công** (trong một transaction):
  1. `realm = targetRealm`, `realmStage = 1`, `cultivationExp = 0`.
  2. `heartState += success.heartDelta` (+10), chặn theo `heartStateCap`.
  3. Hồi đầy HP / MP; `breakthroughFailures[targetRealm] = 0`.
  4. GD-26: `lifespanMaxYears` tự tính lại theo cảnh giới mới (LC-R-021); nếu đang `LAST_LIGHT` và còn thọ thì gỡ (LC-R-051).
  5. GD-27: nếu `success.awaken`, gọi `talents/awakening`; lộ thiên phú ẩn có `revealOn` = RealmCode mới (TLN-R-062, TLN-R-082).
  6. Ghi `notableEvents` (Bia Vạn Cổ); phát `cultivation.breakthrough_succeeded`; `success.broadcast` (Kim Đan trở lên) phát tin toàn server.

### 5.8. Hậu quả thất bại

| Cảnh giới đích | Thọ nguyên (`BREAKTHROUGH_FAILED`, GD-26) | Mất tu vi (% yêu cầu bình cảnh) | Tâm cảnh | `WEAKENED` | Xác suất nhận 1 tầng tâm ma |
|----------------|:---:|:---:|:---:|:---:|:---:|
| Trúc Cơ | 5 năm | 10% | −10 | 1800s | 20% |
| Kim Đan | 15 năm | 20% | −15 | 3600s | 35% |
| Nguyên Anh | 30 năm | 30% | −20 | 7200s | 50% |
| Hóa Thần | 60 năm | 30% | −25 | 7200s | 60% |

- <a id="cu-r-080"></a>**CU-R-080** Đan đột phá và vật phẩm phụ trợ **đã bị tiêu** từ lúc `CHANNELING`; thất bại không hoàn.
- <a id="cu-r-081"></a>**CU-R-081** `breakthroughFailures[targetRealm] += 1` (dùng cho `failurePity` §6.6); `lastBreakthroughFailAt = now`.
- <a id="cu-r-082"></a>**CU-R-082** Tiêu thọ nguyên gọi GD-26 `lifespan-change` (`USED`, `BREAKTHROUGH_FAILED`, ref `attemptId`). Nếu việc này làm thọ về 0, áp `LAST_LIGHT` theo GD-26.
- <a id="cu-r-083"></a>**CU-R-083** **Đột phá trong hồi quang phản chiếu (lần cuối):**
  - Khi đang `LAST_LIGHT`, bỏ qua cooldown thất bại; vẫn cần đủ các điều kiện khác của CU-R-060 (viên mãn, có đan đột phá...).
  - Thất bại ở **bất kỳ bước nào** (vận công HP về 0, roll trượt, thiên kiếp không lethal, mất kết nối quá hạn) khi đang `LAST_LIGHT` → **chết ngay**: không áp bảng hậu quả §5.8, gọi GD-26 `last-light-expired` với `reason = BREAKTHROUGH_FAILED`, `EndCause = LIFESPAN_EXHAUSTED` (vẫn là tọa hóa: được chọn gửi Tàng Bảo Các). Thế Thân Phù không cứu được (GD-26 LC-R-071).
  - Thất bại vì mất kết nối quá hạn trong trường hợp này gắn `needsReview = true` (Death Review có thể khôi phục, GD-26 LC-R-150).
  - Ví dụ: thất bại đột phá Trúc Cơ làm thọ về 0 → vào `LAST_LIGHT` 10 phút → nếu còn Trúc Cơ Đan có thể thử lại ngay; thành công thì sống tiếp với thọ 250, thất bại thì tọa hóa.
- <a id="cu-r-084"></a>**CU-R-084** `LAST_LIGHT` hết hạn trong khi attempt đang ở `CHANNELING`, `HEART_TRIAL`, `ROLLED` hoặc `TRIBULATION` thì **hoãn** tọa hóa tới khi attempt kết thúc; thành công thì sống, thất bại thì chết theo CU-R-083.

### 5.9. Tâm cảnh và tâm ma (định nghĩa chuẩn)
- <a id="cu-r-090"></a>**CU-R-090** `heartState` khởi đầu 50, trong `[0, heartStateCap]`, với `heartStateCap = 100 − 10 × số tầng HEART_DEMON`.
- <a id="cu-r-091"></a>**CU-R-091** Hiển thị cho người chơi dạng **mô tả**, không hiện số (trừ trong `ChanceBreakdown` của đột phá):

| Khoảng | Mô tả |
|--------|-------|
| 0–19 | Tâm loạn như ma |
| 20–39 | Tâm thần bất an |
| 40–59 | Tâm cảnh bình ổn |
| 60–79 | Tâm như chỉ thủy |
| 80–100 | Minh kính chỉ thủy |

- <a id="cu-r-092"></a>**CU-R-092** Nguồn thay đổi tâm cảnh: đột phá thành công (+10) / thất bại (§5.8), bỏ qua thức tỉnh (+5, GD-27), đả tọa ở linh mạch (CU-R-035), đọc bia di tích (GD-21 §3.6), outcome `CHANGE_HEART_STATE` (GD-22).
- <a id="cu-r-093"></a>**CU-R-093** **Tâm ma** (status `HEART_DEMON`, 1–3 tầng). Mỗi tầng: tỷ lệ đột phá −5 điểm %; `heartStateCap` −10 (nếu `heartState` đang vượt cap mới thì hạ xuống bằng cap).
- <a id="cu-r-094"></a>**CU-R-094** Nguồn tâm ma: thất bại đột phá (§5.8), trọng thương liên tục (GD-26 LC-R-062), mất kết nối quá hạn trong thiên kiếp (CU-R-076), outcome `ADD_STATUS` (GD-22).
- <a id="cu-r-095"></a>**CU-R-095** Gỡ tâm ma: `ITM_THANH_TAM_DAN` gỡ 1 tầng; hoàn thành chuỗi nhiệm vụ `QC_QUIET_HEART` gỡ tất cả; đột phá thành công **có** thử thách tâm ma gỡ 1 tầng.
- <a id="cu-r-096"></a>**CU-R-096** GD-27 TLN-R-084 trỏ về mục này; khi có mâu thuẫn, GD-04 là chuẩn.

---

## 6. Công thức

### 6.1. Tốc độ tu luyện
```
cultivationSpeed = rootMultiplier × techniqueMultiplier × (1 + Σ CULTIVATION_SPEED_PCT)
```
- `rootMultiplier`: GD-27 §6.2.
- `techniqueMultiplier`: theo tâm pháp chính, **bảng chuẩn ở [GD-07 SK-R-021](13-skills.md#sk-r-021)** (thêm bậc Hạ / Trung / Thượng: +0 / +0,025 / +0,05; không có tâm pháp: 0,8). Tóm tắt:

| Phẩm tâm pháp | Hoàng | Huyền | Địa | Thiên |
|---------------|:-----:|:-----:|:---:|:-----:|
| Hệ số | 1,00 | 1,15 | 1,30 | 1,50 |

  Thuần thục: Nhập Môn +0 · Tiểu Thành +0,05 · Đại Thành +0,10 · Viên Mãn +0,15 (cộng vào hệ số).
- `CULTIVATION_SPEED_PCT`: hiệu ứng thiên phú (vd. `TAL_EARLY_RISER` +0,15 khi `DAWN`; `TAL_CHAOS_BODY` +0,30).
- `cultivationSpeed` áp dụng cho: **đả tọa, bế quan, đan tu luyện**. Không áp dụng cho: quái, thiên tài địa bảo, cơ duyên, nhiệm vụ.

Ví dụ: Tam linh căn tinh khiết 50 (1,0), tâm pháp Huyền phẩm Tiểu Thành (1,20), có Hỗn Độn Thể (+0,30) → `1,0 × 1,20 × 1,30 = 1,56`.

### 6.2. Chỉ số tầng toàn cục và yêu cầu tu vi

| RealmCode | stageIndex | requirementExp | | RealmCode | stageIndex | requirementExp |
|-----------|:---:|---:|---|-----------|:---:|---:|
| PN1 | 0 | 100 | | TC1 | 10 | 6.000 |
| LK1 | 1 | 300 | | TC2 | 11 | 8.000 |
| LK2 | 2 | 400 | | TC3 | 12 | 10.500 |
| LK3 | 3 | 520 | | TC4 ⛔ | 13 | 14.000 |
| LK4 | 4 | 680 | | KD1 | 14 | 30.000 |
| LK5 | 5 | 880 | | KD2 | 15 | 40.000 |
| LK6 | 6 | 1.150 | | KD3 | 16 | 52.000 |
| LK7 | 7 | 1.500 | | KD4 ⛔ | 17 | 68.000 |
| LK8 | 8 | 1.950 | | NA1..4 | 18–21 | (sau launch) |
| LK9 ⛔ | 9 | 2.500 | | HT1..4 | 22–25 | (sau launch) |

⛔ = bình cảnh. Tổng tu vi: Luyện Khí 9.880 · Trúc Cơ 38.500 · Kim Đan 190.000.

**Tốc độ tham chiếu** (người chơi điển hình, `cultivationSpeed = 1,0`, đủ mọi nguồn theo tỷ trọng mục tiêu): LK ~600/giờ chơi · TC ~1.200 · KD ~2.400. Từ đó: Luyện Khí ≈ 16,5 giờ, Trúc Cơ ≈ 32 giờ, Kim Đan ≈ 79 giờ, khớp mục tiêu kiếp đầu 40–80 giờ, đạt Trúc Cơ – Kim Đan (PLAN D-11). Balance Simulator (PLAN §6.5) phải kiểm chứng các con số này.

### 6.3. Đả tọa
```
expPerHour = meditationExpPctPerHour × requirementExp × cultivationSpeed × locationMult × timeMult × weatherMult
meditationExpPctPerHour = 0,03
locationMult: LEY_LINE 1,5 · HUB 1,0 · OTHER 0,7
timeMult: TY, NGO 1,10 · khác 1,0          weatherMult: SPIRIT_RAIN 1,25 · khác 1,0
```
Ví dụ: LK5 (880), `cultivationSpeed` 1,0, linh mạch, giờ Tý → `0,03 × 880 × 1,5 × 1,1 = 43,56`/giờ.

### 6.4. Bế quan (dùng bởi GD-26 §6.5)
```
expGain = Y × seclusionExpPctPerYear (0,02) × requirementExp × cultivationSpeed × locationMult
```
`locationMult` như §6.3 (bế quan chỉ cho phép tại `LEY_LINE` hoặc `HUB`, GD-26 LC-R-130).

### 6.5. Đan tu luyện
```
qualityExpMult: HA 0,8 · TRUNG 1,0 · THUONG 1,25 · CUC 1,6
qualityToxMult: HA 1,2 · TRUNG 1,0 · THUONG 0,8 · CUC 0,5
toxEfficiency = 1 − 0,6 × pillToxicity / 100                 (đan độc trước khi uống)
expGain   = pill.exp × qualityExpMult × cultivationSpeed × toxEfficiency
toxAdded  = pill.toxicity × qualityToxMult × (1 + Σ PILL_TOXICITY_PCT)
```
Ví dụ: Tụ Khí Đan (`exp` 150, `toxicity` 8), Trung phẩm, `cultivationSpeed` 1,0, đan độc 40 → `expGain = 150 × 0,76 = 114`; đan độc sau = 48. Cùng viên với Vạn Đan Thể (−0,5) → đan độc sau = 44.

### 6.6. Tỷ lệ đột phá
```
total = clamp(
    base                                   // BreakthroughDef.baseChance
  + pill        (HA 0 · TRUNG +10 · THUONG +20 · CUC +30)
  + root        clamp((rootMultiplier − 1) × 10, −5, +15)
  + heart       (heartState − 50) × 0,3
  + support     min(Σ breakthroughBonus, 25)
  + location    (LEY_LINE +5 · HUB 0 · OTHER −5)
  + guardians   min(3 × số hộ pháp, 9)
  + talents     Σ BREAKTHROUGH_CHANCE_FLAT
  + failurePity min(5 × breakthroughFailures[targetRealm], 20)
  − heartDemon  5 × số tầng HEART_DEMON
  − toxicity    floor(pillToxicity / 10)
  + heartTrial  tổng MODIFY_BREAKTHROUGH_CHANCE từ thử thách tâm ma (−20..+15)
  − channelHits min(2 × số lần trúng đòn khi vận công, 20)
, 5, 95)
```
`baseChance`: Trúc Cơ 40 · Kim Đan 25 · Nguyên Anh 15 · Hóa Thần 10.

Ví dụ (Trúc Cơ): đan Thượng phẩm (+20), Tam linh căn tinh khiết 50 (0), tâm cảnh 60 (+3), 1 vật phẩm phụ trợ +10, tại linh mạch (+5), 1 hộ pháp (+3), không tâm ma, đan độc 15 (−1), không trúng đòn → `40 + 20 + 0 + 3 + 10 + 5 + 3 − 1 = 80`.

### 6.7. Tu vi từ quái
```
diff = stageIndex(nhân vật) − stageIndex(quái)
gapMult = diff ≤ 0 ? min(1 + 0,1 × (−diff), 1,3) : { 1: 0,7 · 2: 0,3 · ≥3: 0 }
rankMult: NORMAL 1 · ELITE 5 · BOSS 20
E = tổng tu vi từ quái trong 24h trượt; R = requirementExp tầng hiện tại
fatigueMult = E < 0,3R ? 1,0 : (E < 0,6R ? 0,4 : 0,1)
exp = monster.baseExp × rankMult × gapMult × fatigueMult
```
Ví dụ: nhân vật LK5 (R = 880) hạ quái tinh anh LK6 (`baseExp` 6), E hiện tại 200 → `6 × 5 × 1,1 × 1,0 = 33`. Khi E vượt 264 (0,3R) thì hệ số còn 0,4.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/cultivation/params.json` | Tham số toàn cục |
| `shared/gamedata/cultivation/stages.json` | Mảng `StageDef` (§6.2) |
| `shared/gamedata/cultivation/breakthroughs.json` | Mảng `BreakthroughDef` |
| `shared/gamedata/skills/params.json` (GD-07) | `techniqueTierBase`, `techniqueSubBonus`, `techniqueMasteryBonus` cho §6.1 |
| `shared/gamedata/items/*.json` | Trường `cultivation` của đan / thiên tài địa bảo, `breakthroughBonus` của vật phẩm phụ trợ |
| `shared/encounters/defs/heart_trials/*.yaml` | Thử thách tâm ma (`type: HEART_TRIAL`, GD-22) |

```jsonc
// params.json
{
  "meditationExpPctPerHour": 0.03,
  "meditationLocationMult": { "LEY_LINE": 1.5, "HUB": 1.0, "OTHER": 0.7 },
  "meditationTimeMult": { "TY": 1.10, "NGO": 1.10 },
  "meditationWeatherMult": { "SPIRIT_RAIN": 1.25 },
  "leyLineRadius": 6, "meditationPromptSec": 1800, "meditationHeartCap": 70, "meditationHeartPerSec": 3600,
  "seclusionExpPctPerYear": 0.02,
  "pillQualityExpMult": { "HA": 0.8, "TRUNG": 1.0, "THUONG": 1.25, "CUC": 1.6 },
  "pillQualityToxMult": { "HA": 1.2, "TRUNG": 1.0, "THUONG": 0.8, "CUC": 0.5 },
  "toxEfficiencyLoss": 0.6, "toxHardCap": 100, "toxDecayPerRealHour": 4, "detoxPillAmount": 30,
  "monsterBatchSec": 10,
  "monsterRankMult": { "NORMAL": 1, "ELITE": 5, "BOSS": 20 },
  "monsterGapMult": { "1": 0.7, "2": 0.3 }, "monsterGapBonusPerStage": 0.1, "monsterGapBonusMax": 1.3,
  "monsterFatigue": [ { "belowPct": 0.3, "mult": 1.0 }, { "belowPct": 0.6, "mult": 0.4 }, { "belowPct": null, "mult": 0.1 } ],
  "minChance": 5, "maxChance": 95,
  "pillChanceBonus": { "HA": 0, "TRUNG": 10, "THUONG": 20, "CUC": 30 },
  "rootChanceFactor": 10, "rootChanceMin": -5, "rootChanceMax": 15,
  "heartChanceFactor": 0.3,
  "supportBonusCap": 25, "maxSupportItems": 3,
  "locationChanceBonus": { "LEY_LINE": 5, "HUB": 0, "OTHER": -5 },
  "guardianBonus": 3, "guardianBonusCap": 9, "guardianRadius": 15,
  "failurePityPerFail": 5, "failurePityCap": 20,
  "heartDemonChancePenalty": 5, "toxicityChanceDivisor": 10,
  "channelSec": 30, "channelHitPenalty": 2, "channelHitPenaltyCap": 20,
  "breakthroughFailCooldownSec": 900,
  "heartTrialTimeoutSec": 300, "heartTrialChanceMin": -20, "heartTrialChanceMax": 15,
  "tribulationPauseSec": 120, "tribulationAbuseWindowDays": 30, "tribulationAbuseLimit": 2,
  "tribulationCleanseHpPct": 0.5,
  "heartStateStart": 50, "heartDemonCapPenalty": 10, "heartDemonMaxStacks": 3
}
```

```jsonc
// breakthroughs.json (trích)
[
  { "targetRealm": "TRUC_CO", "fromRealmCode": "LK9", "pillItemIds": ["ITM_TRUC_CO_DAN"], "baseChance": 40,
    "heartTrial": "NEVER",
    "tribulation": { "mapId": "TRIB_TC", "lethal": false, "waves": 3, "strikesPerWave": 3,
                     "strikeDamagePctMaxHp": 0.12, "telegraphSec": 0.8, "maxHealingItems": 3 },
    "failure": { "lifespanYears": 5, "expLossPct": 0.10, "heartDelta": -10, "weakenedSec": 1800, "heartDemonChance": 0.20 },
    "success": { "heartDelta": 10, "awaken": true, "broadcast": false } },
  { "targetRealm": "KIM_DAN", "fromRealmCode": "TC4", "pillItemIds": ["ITM_KET_KIM_DAN"], "baseChance": 25,
    "heartTrial": "ALWAYS",
    "tribulation": { "mapId": "TRIB_KD", "lethal": false, "waves": 6, "strikesPerWave": 3,
                     "strikeDamagePctMaxHp": 0.18, "telegraphSec": 0.8, "maxHealingItems": 3 },
    "failure": { "lifespanYears": 15, "expLossPct": 0.20, "heartDelta": -15, "weakenedSec": 3600, "heartDemonChance": 0.35 },
    "success": { "heartDelta": 10, "awaken": true, "broadcast": true } },
  { "targetRealm": "NGUYEN_ANH", "fromRealmCode": "KD4", "pillItemIds": ["ITM_HOA_ANH_DAN"], "baseChance": 15,
    "heartTrial": "ALWAYS",
    "tribulation": { "mapId": "TRIB_NA", "lethal": true, "waves": 9, "strikesPerWave": 3,
                     "strikeDamagePctMaxHp": 0.25, "telegraphSec": 0.7, "maxHealingItems": 3 },
    "failure": { "lifespanYears": 30, "expLossPct": 0.30, "heartDelta": -20, "weakenedSec": 7200, "heartDemonChance": 0.50 },
    "success": { "heartDelta": 10, "awaken": true, "broadcast": true } }
]
```

```jsonc
// Trường cultivation trong item (trích)
{ "id": "ITM_TU_KHI_DAN", "category": "PILL",
  "cultivation": { "kind": "CULTIVATION_PILL", "exp": 150, "toxicity": 8, "realmMin": "LK1", "realmMax": "LK9" } }
{ "id": "ITM_BACH_NIEN_LINH_QUA", "category": "TREASURE",
  "cultivation": { "kind": "TREASURE", "expPctOfStage": 0.10, "realmMin": "LK1", "realmMax": "TC4" } }
{ "id": "ITM_TU_LINH_TRAN_KY", "category": "BREAKTHROUGH_AID", "breakthroughBonus": 10 }
```

**Validator (CI):** `stages.json` liên tục `stageIndex` 0..n, đúng một bình cảnh mỗi cảnh giới lớn (trừ `PN`); mỗi `BreakthroughDef.fromRealmCode` là bình cảnh; `pillItemIds` tồn tại; `lethal = true` chỉ với `targetRealm ≥ NGUYEN_ANH` (CU-R-005); vật phẩm `cultivation` có `realmMin ≤ realmMax`; mọi cơ duyên `HEART_TRIAL` chỉ dùng outcome được phép (CU-R-070).

---

## 8. (Dành chỗ) Mô phỏng cân bằng
Balance Simulator (PLAN §6.5) chạy các hồ sơ người chơi (chỉ đánh quái / cân bằng / chuyên luyện đan / chuyên bế quan) để kiểm chứng: thời gian qua từng cảnh giới (§6.2), tỷ trọng nguồn tu vi (§2), tuổi khi đạt Trúc Cơ / Kim Đan, phân phối số lần thất bại đột phá. Kết quả mục tiêu ghi ở v0.2.

---

## 9. Hợp đồng giao tiếp

### 9.1. API nội bộ (Game Server / dịch vụ khác → API Service)

| Endpoint | Request | Response | Quy tắc |
|----------|---------|----------|---------|
| `POST /internal/v1/cultivation/exp` | `{ grantId, characterId, source: ExpSource, amount? , pctOfStage?, applySpeed: boolean, refId }` | `{ applied, wasted, realmCode, stageUps: string[] }` | CU-R-001, 002, 020; idempotent theo `grantId`. Dùng cho `ENCOUNTER`, `QUEST`, `TREASURE` (qua item), `GM_ADJUST` |
| `POST /internal/v1/cultivation/monster-kills` | `{ batchId, characterId, kills: [{ monsterId, monsterRealmCode, baseExp, rank }] }` | `{ applied, wasted, fatigueMult }` | CU-R-050..052; idempotent theo `batchId` |
| `POST /internal/v1/cultivation/meditation` | `{ reportId, characterId, windowStart, windowEnd, segments: [{ realSec, location: MeditationLocation, poiId?, shiChen, weather }] }` | `{ applied, wasted, heartGained }` | CU-R-031..036; kiểm tra cửa sổ như GD-26 LC-R-032 |
| `POST /internal/v1/breakthrough/{attemptId}/channel-complete` | `{ hitsTaken, hpZero: boolean }` | `{ next: 'HEART_TRIAL' \| 'ROLL' \| 'FAILED', heartTrialEncounterId? }` | §4, CU-R-064, 065 |
| `POST /internal/v1/breakthrough/{attemptId}/roll` | `{ heartTrialSessionId? }` | `{ chance: ChanceBreakdown, roll, passed, next: 'TRIBULATION' \| 'SUCCEEDED' \| 'FAILED' }` | CU-R-061; roll bằng RNG server, ghi `rng_audit` kind `BREAKTHROUGH` |
| `POST /internal/v1/breakthrough/{attemptId}/tribulation-result` | `{ survived, hpPctEnd, timedOutDisconnect: boolean }` | `{ state }` | CU-R-074..077 |

### 9.2. API công khai (Client → API Service)
> **Đường dẫn, method và khóa idempotency chuẩn ở [TD-04 §5](../tech/03-api.md)** (mọi thao tác của nhân vật nằm dưới `/v1/characters/{id}/…`, xem trước dùng `POST`, khóa idempotency qua header `Idempotency-Key`). Bảng dưới mô tả mục đích; khi vênh với TD-04, theo TD-04.

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/characters/{id}/cultivation` | Cảnh giới, tu vi / yêu cầu, mô tả tâm cảnh, đan độc, tốc độ tu luyện (phân rã), tâm ma | CU-R-091 |
| `POST /v1/characters/{id}/cultivation/consume` | `{ itemInstanceId, preview: boolean }` → preview: `{ expGain, wasted, toxicityAfter }`; thực hiện: áp dụng | CU-R-022, 040..047, 053 |
| `GET /v1/characters/{id}/breakthrough/preview` | `?pillItemInstanceId&supportItemInstanceIds` → `{ eligible, reasons[], chance: ChanceBreakdown, tribulation: { lethal, waves }, failure, deathWard: boolean }` | CU-R-004, 060..063 |
| `POST /v1/characters/{id}/breakthrough` | `{ pillItemInstanceId, supportItemInstanceIds, confirmLethal? }` → `{ attemptId }` | §4 `PREPARED → CHANNELING`; Game Server điều phối các bước sau |

### 9.3. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `C2S_MeditateStart` / `C2S_MeditateStop` | C→S | `{}` |
| `S2C_MeditationState` | S→C | `{ active, location, expPerHour }` |
| `S2C_CultivationUpdate` | S→C | `{ realmCode, cultivationExp, requirementExp, heartStateLabel, pillToxicity }` |
| `S2C_StageUp` | S→C | `{ fromRealmCode, toRealmCode }` |
| `S2C_BreakthroughPhase` | S→C | `{ attemptId, phase: BreakthroughState, chance?, roll?, secondsLeft? }` |
| `S2C_TribulationWave` | S→C | `{ attemptId, wave, strikes: [{ x, z, radius, atMs }] }` |
| `S2C_BreakthroughResult` | S→C | `{ attemptId, result: 'SUCCEEDED' \| 'FAILED' \| 'DIED', consequences }` |

### 9.4. Sự kiện domain (topic `cultivation.*`)

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `cultivation.stage_up` | `{ characterId, fromRealmCode, toRealmCode }` | Telemetry, mở khóa gameplay |
| `cultivation.breakthrough_succeeded` | `{ characterId, targetRealm, attemptId, chance }` | GD-26 (thọ), GD-27 (thức tỉnh), Bia Vạn Cổ, tin toàn server |
| `cultivation.breakthrough_failed` | `{ characterId, targetRealm, attemptId, reason: 'ROLL' \| 'TRIBULATION' \| 'CHANNEL' \| 'DISCONNECT' }` | Telemetry, Bia Vạn Cổ |
| `cultivation.heart_demon_changed` | `{ characterId, stacks }` | Game Server, UI |

---

## 10. Lưu trữ

```sql
-- Bổ sung cột cho character
ALTER TABLE character ADD COLUMN realm                  VARCHAR(16) NOT NULL DEFAULT 'PHAM_NHAN';
ALTER TABLE character ADD COLUMN realm_stage            INT NOT NULL DEFAULT 1;
ALTER TABLE character ADD COLUMN cultivation_exp        BIGINT NOT NULL DEFAULT 0;
ALTER TABLE character ADD COLUMN heart_state            INT NOT NULL DEFAULT 50;
ALTER TABLE character ADD COLUMN pill_toxicity          NUMERIC(6,2) NOT NULL DEFAULT 0;
ALTER TABLE character ADD COLUMN toxicity_updated_at    TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE character ADD COLUMN breakthrough_failures  JSONB NOT NULL DEFAULT '{}';
ALTER TABLE character ADD COLUMN last_breakthrough_fail_at TIMESTAMPTZ;

CREATE TABLE cultivation_ledger (
  id              BIGSERIAL PRIMARY KEY,
  character_id    UUID NOT NULL REFERENCES character(id),
  source          VARCHAR(24) NOT NULL,          -- ExpSource
  requested       NUMERIC(14,2) NOT NULL,
  applied         NUMERIC(14,2) NOT NULL,
  wasted          NUMERIC(14,2) NOT NULL,
  realm_code_after VARCHAR(8) NOT NULL,
  ref_id          VARCHAR(80),
  idempotency_key VARCHAR(80) NOT NULL UNIQUE,   -- grantId / batchId / reportId
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_cul_ledger_monster_window ON cultivation_ledger(character_id, created_at) WHERE source = 'MONSTER';

CREATE TABLE meditation_report (
  report_id    VARCHAR(64) PRIMARY KEY,
  character_id UUID NOT NULL,
  window_start TIMESTAMPTZ NOT NULL,
  window_end   TIMESTAMPTZ NOT NULL,
  real_sec     INT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE breakthrough_attempt (
  attempt_id      UUID PRIMARY KEY,
  character_id    UUID NOT NULL REFERENCES character(id),
  target_realm    VARCHAR(16) NOT NULL,
  from_realm_code VARCHAR(8) NOT NULL,
  state           VARCHAR(16) NOT NULL,
  pill_item_instance_id UUID NOT NULL,
  pill_quality    VARCHAR(8) NOT NULL,
  support_item_ids JSONB NOT NULL DEFAULT '[]',
  guardian_ids    JSONB NOT NULL DEFAULT '[]',
  location        JSONB NOT NULL,
  chance          JSONB NOT NULL,                -- ChanceBreakdown
  heart_trial_session_id UUID,
  roll            NUMERIC(8,6),
  roll_passed     BOOLEAN,
  tribulation     JSONB,
  needs_review    BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  resolved_at     TIMESTAMPTZ
);
-- tối đa 1 attempt đang chạy mỗi nhân vật
CREATE UNIQUE INDEX ux_bt_one_open ON breakthrough_attempt(character_id)
  WHERE state IN ('PREPARED', 'CHANNELING', 'HEART_TRIAL', 'ROLLED', 'TRIBULATION');
```

**Mã lý do item_log mới:** `BREAKTHROUGH_CONSUMED`, `PILL_CONSUMED`, `TREASURE_CONSUMED`. `rng_audit.kind` mới: `BREAKTHROUGH`, `HEART_DEMON_ROLL`, `HEART_TRIAL_PICK`.

---

## 11. UI/UX

- **Thanh tu vi** (dưới HUD): `Luyện Khí tầng 5 · 640 / 880`; ở bình cảnh đổi thành `Viên mãn, chờ đột phá` (nhấp nháy nhẹ).
- **Bảng tu luyện** (phím `C` → tab Tu luyện): cảnh giới, tốc độ tu luyện **phân rã** (linh căn × tâm pháp × thiên phú), mô tả tâm cảnh, thanh đan độc (xanh → đỏ, mốc 100 khóa đan), tâm ma (biểu tượng 1–3 tầng).
- **Xem trước khi dùng đan / thiên tài địa bảo:** tu vi nhận, phần lãng phí (đỏ), đan độc sau khi dùng.
- **Màn chuẩn bị đột phá:** chọn đan (hiện phẩm chất), tối đa 3 vật phẩm phụ trợ, danh sách hộ pháp trong bán kính, **bảng tỷ lệ từng dòng** (CU-R-004), hậu quả thất bại, cảnh báo lethal + Thế Thân Phù (CU-R-071). Khi đang `LAST_LIGHT`: khung đỏ *"Thất bại lần này sẽ tọa hóa ngay"* thay cho bảng hậu quả (CU-R-083).
- **Vận công:** camera hạ thấp, linh khí xoáy quanh nhân vật theo màu linh căn; đếm ngược 30s; viền đỏ khi trúng đòn.
- **Roll:** thanh tỷ lệ với kim quay dừng ở kết quả (minh bạch, không giả lập "suýt trúng" gây hiểu nhầm).
- **Thiên kiếp:** mây đen tụ, vòng sét báo trước trên mặt đất, đếm đợt `Đợt 2/3`; thành công thì cột sáng + tên cảnh giới mới bằng thư pháp.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-26 Vòng đời | `stageRequirementExp` cho trọng thương / bế quan; tiêu thọ khi thất bại; `LethalEvent(TRIBULATION)`; đột phá trong `LAST_LIGHT`; đả tọa không tính AFK (CU-R-033); tuổi `PAUSED` trong thiên kiếp |
| GD-27 Thiên phú | `rootMultiplier`; hiệu ứng thiên phú trong §6; thức tỉnh sau đột phá; chặn đột phá khi còn lựa chọn chờ; Thiên lôi tẩy thể dùng `CHANGE_INNATE_STAT` |
| GD-22 Cơ duyên | `GRANT_CULTIVATION`, `CHANGE_HEART_STATE`; trigger `MEDITATE`; loại `HEART_TRIAL` + outcome `MODIFY_BREAKTHROUGH_CHANCE` |
| GD-21 Open World | Linh mạch, khu đả tọa thành trấn; canh giờ; thời tiết |
| GD-10 Luyện đan | Đan tu luyện, đan đột phá, phẩm chất `PillQuality`, Giải Độc Đan, Thanh Tâm Đan |
| GD-28 Hiểm địa | Nguyên liệu chính của đan đột phá; thiên tài địa bảo |
| GD-12 Quái | `monsterRealmCode`, `baseExp`, `rank` |
| GD-06 Chỉ số | Chỉ số cơ bản theo `RealmCode` |
| GD-07 Kỹ năng | `techniqueMultiplier`, mở khóa ô kỹ năng theo cảnh giới |
| GD-14 Xã hội | Hộ pháp (tổ đội), phần thưởng hộ pháp (SO-R-070) |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Một lần cộng tu vi vượt nhiều tầng | Lên lần lượt từng tầng (CU-R-020), phát `stage_up` cho từng tầng |
| Uống đan khi ở bình cảnh viên mãn | Preview báo lãng phí 100%; người chơi vẫn có thể xác nhận (đan độc vẫn cộng) |
| Đột phá khi hộ pháp rời tổ đội giữa chừng | Bonus hộ pháp chốt lúc `PREPARED → CHANNELING`, không đổi sau đó |
| Đang vận công thì bị đẩy ra khỏi bản đồ (sự kiện server) | `FAILED` với lý do `CHANNEL`, `needsReview`; GM có thể hoàn đan |
| Server sập giữa `TRIBULATION` | Khởi động lại: attempt `TRIBULATION` quá hạn được xử lý như CU-R-076 (không chết), `needsReview` |
| Thọ về 0 do thất bại đột phá | GD-26 áp `LAST_LIGHT` (10 phút); được thử lại ngay không cần chờ cooldown nếu còn đan; thất bại lần này thì tọa hóa (CU-R-083) |
| `LAST_LIGHT` còn 20 giây khi bắt đầu đột phá | Tọa hóa được hoãn tới khi attempt kết thúc (CU-R-084) |
| Thiên phú thay đổi giữa preview và xác nhận | Tính lại tỷ lệ khi xác nhận; nếu khác preview, trả lỗi `CHANCE_CHANGED` kèm tỷ lệ mới để người chơi xác nhận lại |
| Báo cáo đả tọa trùng cửa sổ | Từ chối (như GD-26 LC-AC-005) |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `exp_gain` (gộp theo giờ) | source, applied, wasted, realmCode | **Tỷ trọng nguồn tu vi thực tế** so với mục tiêu §2 |
| `stage_up` | realmCode, playHours, ageYears | Thời gian qua từng tầng (so với §6.2) |
| `pill_consumed` | itemId, quality, toxBefore, expGain | Người chơi có quản lý đan độc không |
| `meditation_session` | location, durationSec, expGain | Linh mạch có được dùng như dự kiến |
| `breakthrough_preview` | targetRealm, total, components | Người chơi chuẩn bị tới mức nào trước khi thử |
| `breakthrough_result` | targetRealm, total, roll, result, reason, attemptNo | Tỷ lệ thành công thực tế, số lần thử trung bình |
| `tribulation_result` | targetRealm, survived, hpPctEnd, healingUsed, disconnected | Độ khó thiên kiếp |
| `heart_demon` | stacks, source | Tâm ma có quá phổ biến không |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="cu-ac-001"></a>CU-AC-001 | LK3 với 500/520 tu vi | Nhận 400 tu vi | Lên LK4 với 380/680 (CU-R-020) |
| CU-AC-002 | LK8 với 1.900/1.950 | Nhận 3.000 | LK9, tu vi 2.500/2.500, `wasted = 2.450` (CU-R-021) |
| CU-AC-003 | Gửi cùng `grantId` / `batchId` / `reportId` hai lần | API xử lý | Áp dụng một lần |
| CU-AC-004 | Ví dụ §6.1 | Tính `cultivationSpeed` | 1,56 |
| CU-AC-005 | Ví dụ §6.3 | Tính tu vi đả tọa | 43,56/giờ |
| CU-AC-006 | Ví dụ §6.5 | Uống đan | `expGain` 114, đan độc 48 |
| CU-AC-007 | Đan độc 100 | Uống đan tu luyện | Bị từ chối; Giải Độc Đan vẫn dùng được (CU-R-043) |
| CU-AC-008 | Đan độc 60, offline 5 giờ | Đăng nhập | Đan độc 40 (CU-R-044) |
| CU-AC-009 | Ví dụ §6.7 | Hạ quái | 33 tu vi; sau khi E ≥ 264, hệ số còn 0,4 |
| CU-AC-010 | Nhân vật LK5 hạ quái LK2 | Hạ quái | 0 tu vi (CU-R-050) |
| CU-AC-011 | Nhân vật có Thiên linh căn và Tam linh căn hạ cùng một quái | So sánh | Cùng tu vi (CU-R-052) |
| CU-AC-012 | Đả tọa ở `FIELD` 3600s, không input | Báo cáo tuổi (GD-26) | Tuổi +1,0 (không bị hạ thành AFK ×0,25) (CU-R-033) |
| CU-AC-013 | Ví dụ §6.6 | Preview đột phá | `total = 80`, từng dòng đúng như ví dụ |
| CU-AC-014 | LK9 chưa viên mãn hoặc không có Trúc Cơ Đan | Preview | `eligible = false`, `reasons` liệt kê đúng điều kiện thiếu |
| CU-AC-015 | Còn `PendingTalentChoice(AWAKEN)` | Preview đột phá | `eligible = false` (CU-R-060 mục 3) |
| CU-AC-016 | Xác nhận đột phá | Vào `CHANNELING` | Đan + vật phẩm phụ trợ bị tiêu ngay; hủy sau đó không hoàn |
| CU-AC-017 | Trúng đòn 12 lần khi vận công | Roll | `channelHits = −20` (chặn) |
| CU-AC-018 | Đột phá Trúc Cơ, roll đạt, sống sót thiên kiếp với HP 60% | Kết thúc | `TC1`, tu vi 0, tâm cảnh +10, thọ tối đa theo 250, có `PendingTalentChoice(AWAKEN)`, căn cốt +1 |
| CU-AC-019 | Đột phá Trúc Cơ, roll trượt | Kết thúc | Thọ −5 năm, tu vi = 90% của 2.500, tâm cảnh −10, `WEAKENED` 1800s, `breakthroughFailures.TRUC_CO = 1`; lần sau `failurePity +5` |
| CU-AC-020 | Thất bại lúc 10:00, không `LAST_LIGHT` | Thử lại lúc 10:10; rồi lúc 10:15 | 10:10 bị chặn bởi cooldown; 10:15 được phép (CU-R-060 mục 5) |
| CU-AC-021 | Thiên kiếp Trúc Cơ, HP về 0 | Kết thúc | `FAILED` (không chết) (CU-R-005) |
| CU-AC-022 | Thiên kiếp Nguyên Anh, HP về 0, không Thế Thân Phù | Kết thúc | GD-26 nhận `LethalEvent(TRIBULATION)`; attempt `DIED` |
| CU-AC-023 | Thiên kiếp Nguyên Anh, mất kết nối 150s | Quá hạn | `FAILED` không chết, +1 tầng tâm ma, `needsReview = true` (CU-R-076) |
| CU-AC-024 | Có 2 tầng tâm ma, `heartState` 95 | Nhận tầng thứ 3 | `heartStateCap = 70`, `heartState` hạ xuống 70; đột phá tiếp theo bắt buộc thử thách tâm ma |
| CU-AC-025 | `validator` gặp `BreakthroughDef` Trúc Cơ có `lethal: true` | CI | Fail (CU-R-005) |
| CU-AC-026 | Balance Simulator, hồ sơ "cân bằng" | Chạy | Thời gian qua Luyện Khí 14–19 giờ, Trúc Cơ 27–37 giờ (±15% so với §6.2) |
| CU-AC-027 | Thất bại đột phá Trúc Cơ làm thọ về 0, còn 1 Trúc Cơ Đan | Thử lại ngay trong `LAST_LIGHT` | Được phép (không cooldown) |
| CU-AC-028 | Đang `LAST_LIGHT`, đột phá roll trượt | Kết thúc attempt | `DIED`; GD-26 kết thúc kiếp với `LIFESPAN_EXHAUSTED`; không áp bảng hậu quả §5.8; Thế Thân Phù không bị tiêu |
| CU-AC-029 | Đang `LAST_LIGHT`, đột phá thành công lên Trúc Cơ | Kết thúc attempt | `SUCCEEDED`, gỡ `LAST_LIGHT`, thọ tối đa theo 250 |
| CU-AC-030 | `LAST_LIGHT` hết hạn khi attempt đang `TRIBULATION` | Thiên kiếp tiếp diễn | Không tọa hóa giữa chừng; kết quả theo attempt (CU-R-084) |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 1 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Tu vi, tầng nhỏ, bình cảnh, PN1 → LK1 | ✅ | | | |
| Tu vi từ quái + mệt mỏi 24h | ✅ | | | |
| Đả tọa (địa điểm, canh giờ, chống bot) | | ✅ | | |
| Đan tu luyện + đan độc + Giải Độc Đan | | ✅ | | |
| Thiên tài địa bảo, `GRANT_CULTIVATION` | | ✅ | | |
| Đột phá Trúc Cơ (đan, tỷ lệ, vận công, thiên kiếp không lethal) | | ✅ | | |
| Đột phá Kim Đan (thử thách tâm ma, thiên kiếp không lethal) | | ✅ (giới hạn) | ✅ | |
| Tâm cảnh, tâm ma, Thanh Tâm Đan, `QC_QUIET_HEART` | | ✅ | | |
| Hộ pháp | | ✅ | | |
| Nguyên Anh, thiên kiếp lethal | | | | ✅ |
| Linh mạch tranh chấp | | | | ✅ |

---

## 17. Câu hỏi mở

- [ ] **CU-Q-1:** Có giới hạn số người đả tọa trên một linh mạch mỗi kênh (vd. 8) để tạo tranh chấp nhẹ không? *Mặc định: không giới hạn ở MVP.*
- [ ] **CU-Q-2:** Tỷ lệ cơ bản Trúc Cơ 40% (thường đạt 70–85% khi chuẩn bị tốt) có làm Trúc Cơ quá dễ, mất cảm giác "khoảnh khắc lớn"? Chốt sau playtest Phase 3.
- [ ] **CU-Q-3:** Thất bại đột phá có nên rơi Trúc Cơ Đan "tàn đan" (bán được / dùng làm nguyên liệu) để giảm cảm giác mất trắng?
- [x] **CU-Q-4:** Hộ pháp có nhận phần thưởng gì (điểm sư đồ, tu vi nhỏ) để khuyến khích giúp người khác đột phá? **→ Đã trả lời ở GD-14 SO-R-070: +2 thiện ác, tiến độ danh hiệu, tối đa 5 lần / ngày, không thưởng tu vi.**
- [ ] **CU-Q-5:** Thời gian vận công 30 giây có quá ngắn để hộ pháp có ý nghĩa? Cân nhắc 60s cho Kim Đan trở lên.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-20 | — | Bản nháp đầu: bất biến, cảnh giới & tầng, tu vi & bình cảnh, đả tọa, đan độc, tu vi từ quái, đột phá (điều kiện, tỷ lệ, vận công, tâm ma, thiên kiếp, thành công / thất bại), tâm cảnh & tâm ma (chuẩn), công thức + ví dụ, gamedata, API, DB, nghiệm thu |
| v0.2 | 2026-10-20 | — | Cooldown sau thất bại 3600s → **900s**; thêm CU-R-083 (trong hồi quang phản chiếu được thử lại ngay, thất bại thì tọa hóa) và CU-R-084 (hoãn tọa hóa khi đang đột phá); CU-AC-027..030 |
| v0.3 | 2026-10-23 | — | CU-R-071: binh giải trong thiên kiếp lethal (GD-26 §5.16) |
| v0.4 | 2026-10-25 | — | Trỏ đường dẫn API chuẩn sang TD-04 §5 |
| v0.5 | 2026-10-25 | — | Ngự kiếm cần `QC_SWORD_FLIGHT`; Trỏ telemetry chuẩn GD-20 |
| v0.6 | 2026-10-25 | — | Tỷ trọng nguồn tu vi chỉnh theo GD-03 (đan 30–35%, đả tọa 10–15%, quái 20–25%) |

