# GD-26 — Vòng đời nhân vật: Thọ nguyên · Cái chết · Luân hồi

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-26 · Prefix ID: `LC` |
| Owner | Director + Game Designer |
| Trạng thái | 🟡 Bản nháp v0.9 |
| Cần trước | Phase 0 |
| Quyết định nền | PLAN D-03, D-11, D-12, D-13, D-14, D-17 |
| Liên quan | [PLAN §4.2](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-21 Open World](22-open-world.md) · [GD-22 Cơ duyên](23-encounters.md) · GD-04 Tu luyện · GD-27 Thiên phú · GD-28 Hiểm địa · GD-13 Kinh tế · TD-03 Database |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Mọi con số là mặc định, nằm trong `shared/gamedata/lifecycle/*.json` (§7).

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện* (chính), P2 *Cảm giác tu tiên thật sự*.

**Người chơi cần cảm thấy:**
- Thời gian quý giá: mỗi quyết định tiêu thọ nguyên đều đáng cân nhắc.
- Cái chết đáng sợ nhưng **công bằng**: chỉ chết thật khi tự chọn mạo hiểm.
- Kết thúc một kiếp là **một chương khép lại**, không phải mất trắng: luôn có thứ mang sang kiếp sau.
- **Mất thứ đang mang, không mất thứ đã cất:** đồ trên người rớt; kho đồ và động phủ bị **phong ấn**, kiếp sau đánh bại **tàn hồn của chính mình** / **thú canh cửa** để lấy lại (§5.15) — vừa có cái giá, vừa có mục tiêu khi chơi lại.

**Bất biến của hệ thống (không bao giờ được vi phạm):**

| Mã | Bất biến |
|----|----------|
| <a id="lc-r-001"></a>**LC-R-001** | Nhân vật chỉ **chết thật** trong đúng 3 trường hợp: (1) chết trong `MapType.DANGER_ZONE`, (2) thất bại thiên kiếp có cờ `lethal = true`, (3) hết thọ nguyên, **bao gồm thất bại đột phá khi đang `LAST_LIGHT`** (GD-04 CU-R-083). Mọi trường hợp hết máu khác là **trọng thương** (§5.6). Chết do **người chơi** ở dã ngoại (PvP, [GD-32](31-pvp-infamy.md)) luôn là trọng thương; người vô tội (`PEACE` + `CLEAN`) bị giết **không mất thọ / tu vi** (PK-R-003). Ngoài ra nhân vật có thể **tự kết thúc kiếp bằng binh giải** khi đang độ thiên kiếp lethal (§5.16, PLAN Q21) — đây là lựa chọn chủ động, không phải "chết do roll". |
| <a id="lc-r-002"></a>**LC-R-002** | Tuổi **không trôi khi offline**, trừ khi người chơi chủ động bế quan offline (§5.13). |
| <a id="lc-r-003"></a>**LC-R-003** | Mọi thay đổi thọ nguyên (tiêu hoặc cộng) đều ghi `lifespan_ledger` với `reasonCode`, có khóa idempotency. |
| <a id="lc-r-004"></a>**LC-R-004** | Mọi quyết định về sống/chết, tuổi, điểm Luân Hồi do **API Service** thực hiện trong transaction DB. Game Server chỉ báo cáo sự kiện. |
| <a id="lc-r-005"></a>**LC-R-005** | Không vật phẩm / dịch vụ trả bằng tiền thật nào được giảm rủi ro chết hoặc tăng thọ nguyên (D-17). |
| <a id="lc-r-006"></a>**LC-R-006** | Mỗi tài khoản có tối đa **1 nhân vật `ALIVE` hoặc `ENDING`** trên một server (D-12). |

---

## 2. Tổng quan

```
 Tạo nhân vật (GD-27) ──► ALIVE ─────────────────────────────────────────────┐
                           │  tuổi trôi (§5.3), tiêu/cộng thọ (§5.4)          │
                           │  hết máu ngoài hiểm địa → TRỌNG THƯƠNG (§5.6)    │
                           │                                                  │
                           ├─ hết thọ → LAST_LIGHT 10' → tọa hóa ───────┐     │
                           ├─ chết trong hiểm địa ────────────────────┐  │     │
                           └─ thất bại thiên kiếp lethal ────────────┐│  │     │
                                    (kiểm tra bảo hộ tân thủ,        ││  │     │
                                     Thế Thân Phù: §5.7)             ▼▼  ▼     │
                                                              ENDING (≤ 24h)   │
                                     tổng kết, gửi Tàng Bảo Các, lời trăn trối │
                                                                     ▼         │
                                                              ENDED            │
                                     điểm Luân Hồi, Bia Vạn Cổ, hủy tài sản    │
                                                                     ▼         │
                                                       Luân hồi: cửa hàng ─────┘
                                                       Luân Hồi, tạo kiếp mới
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
// ===== Enum (định nghĩa chuẩn trong Glossary) =====
type LifeState = 'ALIVE' | 'ENDING' | 'ENDED';
type EndCause = 'LIFESPAN_EXHAUSTED' | 'SLAIN_DANGER_ZONE' | 'TRIBULATION_FAILED' | 'SELF_DISSOLUTION' | 'ASCENDED';   // SELF_DISSOLUTION: binh giải (§5.16)
type WarningLevel = 'NORMAL' | 'NOTICE' | 'GREAT_LIMIT' | 'CRITICAL' | 'LAST_LIGHT';
type TimeFlowState = 'ACTIVE' | 'TOWN' | 'AFK' | 'PAUSED';      // §5.3
type LifespanLedgerKind = 'USED' | 'EXTENSION';
type LifespanReason =
  | 'AGE_ACCRUAL' | 'SEVERE_INJURY' | 'BREAKTHROUGH_FAILED' | 'SECLUSION_ONLINE'
  | 'SECLUSION_OFFLINE' | 'CRAFTING' | 'FORBIDDEN_ART' | 'ENCOUNTER'
  | 'NEWBIE_PROTECTION_PENALTY' | 'TALENT_FUSION' | 'PILL_EXTENSION' | 'CRAFTING_REFUND' | 'GM_ADJUST';  // CRAFTING_REFUND: GD-10 AL-R-033

// ===== Phần vòng đời của nhân vật (lưu trong bảng character) =====
interface CharacterLifecycle {
  characterId: string;
  accountId: string;
  generation: number;                 // kiếp thứ mấy của tài khoản trên server, từ 1
  lifeState: LifeState;
  startAge: number;                   // = params.startAge (16) khi tạo
  lifespanUsedSec: number;            // int64, chỉ tăng (trừ GM_ADJUST)
  innateLifespanBonusYears: number;   // roll khi tạo nhân vật (GD-27), −20..+20
  lifespanExtensionYears: number;     // tổng năm cộng thêm từ đan dược / cơ duyên
  lastLightUntil: string | null;      // ISO time; khác null = đang LAST_LIGHT
  endingDeadline: string | null;      // hạn chót của trạng thái ENDING
  endCause: EndCause | null;
  diedAt: string | null;
  endedAt: string | null;
}

// ===== Giá trị dẫn xuất (tính khi đọc, KHÔNG lưu) =====
interface LifecycleView {
  ageYears: number;                   // §6.1
  lifespanMaxYears: number;           // §6.2
  lifespanRemainingYears: number;
  warningLevel: WarningLevel;         // §5.5
  newbieProtection: boolean;          // §5.7
  timeFlowMultiplier: number;         // §5.3
}

// ===== Cấp tài khoản =====
interface AccountRebirth {
  accountId: string;
  rebirthPoints: number;              // số dư hiện tại
  rebirthPointsTotal: number;         // tổng đã nhận (thống kê)
  unlocks: string[];                  // ID trong rebirth_unlocks.json
  vaultSlots: number;
  vaultSpiritStones: number;
  firstReachRealms: string[];         // RealmCode lớn đã đạt lần đầu (thưởng một lần)
}

interface AccountServerProgress {
  accountId: string;
  serverId: number;
  generationCount: number;            // số kiếp đã bắt đầu trên server này
}

interface LifeRecord {                // Bia Vạn Cổ
  recordId: string;
  accountId: string;
  characterId: string;
  characterName: string;
  generation: number;
  backgroundId: string;
  talentIds: string[];
  spiritRoot: { elements: string[]; purity: number };
  finalRealmCode: string;             // vd. "TC2"
  ageYears: number;
  endCause: EndCause;
  endMapId: string;
  uniqueEncounters: number;
  notableEvents: { at: string; type: string; refId: string }[]; // đột phá, cơ duyên Địa+, ...
  lastWords: string | null;           // ≤ 200 ký tự, qua bộ lọc từ ngữ
  rebirthPointsAwarded: number;
  endedAt: string;
}
```

---


```ts
type SealBand = 'LK' | 'TC' | 'KD' | 'NA';
interface LegacyStash {                     // Di Tàng (§5.15)
  accountId: string; sourceCharacterId: string; sealedAt: string;
  bands: Record<SealBand, { itemCount: number; topGrade: ItemGrade | null; released: boolean }>;
}
interface RemnantSnapshot {                 // chụp ở bước 6 (§5.8)
  characterId: string; name: string; appearance: object; weapon: WeaponType;
  mainTechniqueId: string; skillIds: string[];   // ≤ 5, không có tuyệt kỹ / bí pháp
  highestRealmCode: string; lastWords: string | null;
}
```

## 4. Máy trạng thái `LifeState`

| Từ | Đến | Điều kiện (guard) | Tác dụng phụ |
|----|-----|-------------------|--------------|
| — | `ALIVE` | Tạo nhân vật thành công (GD-27); tài khoản không có nhân vật `ALIVE`/`ENDING` trên server đã chọn (LC-R-006) | `generation = accountServerProgress(serverId).generationCount + 1`; `startAge = params.startAge`; ghi `lifecycle.life_started` |
| `ALIVE` | `ALIVE` (LAST_LIGHT) | `lifespanRemainingYears ≤ 0` và `lastLightUntil == null` | `lastLightUntil = now + params.lastLightRealSec`; thêm status `LAST_LIGHT` (§5.5) |
| `ALIVE` | `ENDING` | Chết thật hợp lệ (§5.7) **hoặc** `now ≥ lastLightUntil` (có trì hoãn chiến đấu, LC-R-033) | Quy trình §5.8 bước 1–6 |
| `ENDING` | `ENDED` | Người chơi xác nhận **hoặc** `now ≥ endingDeadline` | Quy trình §5.8 bước 7–12 |
| `ENDED` | — | Trạng thái cuối, không đổi nữa | |

Không có chuyển trạng thái nào khác. Phi thăng (`ASCENDED`) đi `ALIVE → ENDING` như chết thật, nhưng là sau launch.

---

## 5. Quy tắc

### 5.1. Khởi tạo kiếp
- <a id="lc-r-010"></a>**LC-R-010** Nhân vật mới bắt đầu ở `RealmCode` `PN1` (Phàm nhân), `startAge = 16`, `lifespanUsedSec = 0`, `lifespanExtensionYears = 0`.
- <a id="lc-r-011"></a>**LC-R-011** `innateLifespanBonusYears` lấy từ kết quả roll tạo nhân vật (GD-27); khoảng cho phép `[params.innateLifespanMin, params.innateLifespanMax]`.
- <a id="lc-r-012"></a>**LC-R-012** Áp dụng các unlock Luân Hồi có hiệu lực lúc tạo (vd. `UNL_PAST_LIFE_MEMORY`, §5.10) **trong cùng transaction** tạo nhân vật.
- <a id="lc-r-013"></a>**LC-R-013** Vật phẩm trong Tàng Bảo Các **không** tự chuyển vào túi; nhân vật mới rút ở NPC Tàng Bảo Các (§5.11).

### 5.2. Thọ nguyên tối đa
- <a id="lc-r-020"></a>**LC-R-020** `lifespanMaxYears` tính theo công thức §6.2, **không lưu**, tính lại khi đọc.
- <a id="lc-r-021"></a>**LC-R-021** Đột phá cảnh giới lớn đổi `realmLifespanYears` theo bảng `realm_lifespan.json`; tuổi hiện tại giữ nguyên.
- <a id="lc-r-022"></a>**LC-R-022** Nếu `lifespanMaxYears` giảm xuống ≤ `ageYears` (vd. mất thiên phú Trường Sinh Thể), áp dụng ngay LAST_LIGHT (§5.5).

### 5.3. Đồng hồ tuổi
- <a id="lc-r-030"></a>**LC-R-030** Tuổi tăng theo **giây hoạt động có hệ số**: mỗi giây thực, `lifespanUsedSec += timeFlowMultiplier`.

| `TimeFlowState` | Điều kiện | Hệ số mặc định |
|-----------------|-----------|:--------------:|
| `ACTIVE` | Online, ở bản đồ `FIELD`, `DANGER_ZONE`, `DUNGEON`, `ENCOUNTER_INSTANCE` | 1,0 |
| `TOWN` | Online, ở `HUB`, `CAVE` hoặc `SECT_GROUNDS` (GD-30) | 0,25 |
| `AFK` | Không có input ≥ `params.afkThresholdSec` (300s), mọi loại bản đồ | 0,25 |
| `PAUSED` | Đang chuyển bản đồ / loading, đang ở màn hình nhân vật, `lifeState ≠ ALIVE`, trong thiên kiếp (instance thời gian ngắn) | 0 |

- <a id="lc-r-031"></a>**LC-R-031** Ưu tiên khi trùng điều kiện: `PAUSED` > `AFK` > `TOWN` > `ACTIVE`. **Ngoại lệ:** đang đả tọa (GD-04 CU-R-033) thì không bao giờ tính là `AFK`; tuổi trôi theo loại bản đồ.
- <a id="lc-r-032"></a>**LC-R-032** Game Server cộng dồn theo từng `MapInstance` và gửi `AgeAccrualReport` (§8.1) mỗi `params.accrualReportSec` (60s) và khi nhân vật rời bản đồ / đăng xuất. API Service **từ chối** báo cáo nếu tổng giây thực của các bucket > độ dài cửa sổ + `params.accrualToleranceSec` (5s) hoặc cửa sổ chồng lấn báo cáo trước.
- <a id="lc-r-033"></a>**LC-R-033** Nếu LAST_LIGHT hết hạn khi nhân vật đang trong trạng thái chiến đấu, trì hoãn tọa hóa tối đa `params.lastLightCombatGraceSec` (60s) hoặc tới khi ra khỏi chiến đấu, lấy mốc nào đến trước.

### 5.4. Tiêu / cộng thọ nguyên trực tiếp
- <a id="lc-r-040"></a>**LC-R-040** Tiêu thọ nguyên (kind `USED`) tăng `lifespanUsedSec` thêm `years × params.secPerYear`. Các nguồn và giá trị nằm trong `lifespan_costs.json`:

| `reasonCode` | Nguồn | Mặc định |
|--------------|-------|----------|
| `SEVERE_INJURY` | Trọng thương | Theo cảnh giới: PN 0 · LK 1 · TC 2 · KD 3 · NA 5 năm |
| `BREAKTHROUGH_FAILED` | Thất bại đột phá (GD-04) | TC 5 · KD 15 · NA 30 năm |
| `SECLUSION_ONLINE` / `SECLUSION_OFFLINE` | Bế quan (§5.13) | Số năm người chơi chọn |
| `CRAFTING` | Luyện đan / khí cao cấp (GD-10, GD-11) | Theo công thức: 1–3 năm |
| `FORBIDDEN_ART` | Bí pháp liều mạng (GD-07) | Theo kỹ năng |
| `ENCOUNTER` | Kết quả cơ duyên (GD-22 outcome `CHANGE_LIFESPAN` âm) | Theo cơ duyên |
| `NEWBIE_PROTECTION_PENALTY` | Chết được bảo hộ tân thủ (§5.7) | 10 năm |
| `TALENT_FUSION` | Thất bại dung hợp dị hỏa / huyết mạch (GD-27 TLN-R-083) | Theo vật phẩm |

- <a id="lc-r-041"></a>**LC-R-041** Cộng thọ nguyên (kind `EXTENSION`) tăng `lifespanExtensionYears`. Hiệu lực thực tế tính theo §6.3 (giảm dần khi dùng lặp lại cùng một vật phẩm, có trần theo cảnh giới).
- <a id="lc-r-042"></a>**LC-R-042** Hành động tiêu thọ nguyên do người chơi chủ động (bế quan, chế tác, bí pháp) phải **hiển thị số năm sẽ mất và tuổi sau khi mất** trước khi xác nhận. Nếu số năm ≥ `lifespanRemainingYears`, hiện cảnh báo đỏ *"Hành động này sẽ dẫn tới đại hạn ngay lập tức"*.

### 5.5. Ngưỡng cảnh báo, Đại hạn, Hồi quang phản chiếu
- <a id="lc-r-050"></a>**LC-R-050** `warningLevel` tính theo tỷ lệ `r = lifespanRemainingYears / lifespanMaxYears`:

| Mức | Điều kiện | Hệ quả |
|-----|-----------|--------|
| `NORMAL` | r > 25% | — |
| `NOTICE` | 10% < r ≤ 25% | Thanh tuổi chuyển vàng; thông báo một lần |
| `GREAT_LIMIT` | r ≤ 10% | Status `GREAT_LIMIT`; mở hai nhiệm vụ `QC_GREAT_LIMIT_SEEK` (tìm diên thọ) và `QC_GREAT_LIMIT_LEGACY` (chuẩn bị hậu sự, +20 điểm Luân Hồi khi tọa hóa) ([GD-15 §5.8](21-quests.md)); thanh tuổi đỏ |
| `CRITICAL` | `lifespanRemainingYears ≤ max(2% × lifespanMaxYears, 1)` | Cảnh báo mỗi lần vào bản đồ; nhắc gửi đồ vào Tàng Bảo Các |
| `LAST_LIGHT` | `lifespanRemainingYears ≤ 0` | Xem LC-R-051 |

- <a id="lc-r-051"></a>**LC-R-051** **Hồi quang phản chiếu:** thời lượng `params.lastLightRealSec` (600s **thực**, không chịu hệ số), +20% mọi chỉ số chiến đấu. Nhân vật **vẫn được phép đột phá**; nếu thành công và `lifespanRemainingYears > 0` thì gỡ `LAST_LIGHT`, `lastLightUntil = null`. Hết thời gian thì tọa hóa (`EndCause.LIFESPAN_EXHAUSTED`). Trong `LAST_LIGHT` được đột phá ngay không cần cooldown; đột phá thất bại thì tọa hóa ngay; hết hạn khi đang đột phá thì hoãn tới khi có kết quả (GD-04 CU-R-083, CU-R-084).
- <a id="lc-r-052"></a>**LC-R-052** Khi đang LAST_LIGHT, tuổi vẫn được ghi nhận nhưng không gây hiệu ứng thêm.
- <a id="lc-r-053"></a>**LC-R-053** Mỗi lần `warningLevel` đổi, phát sự kiện `lifecycle.warning_level_changed` và gửi `S2C_LifecycleUpdate`.

### 5.6. Trọng thương (hết máu nhưng không chết thật)
- <a id="lc-r-060"></a>**LC-R-060** Áp dụng khi HP về 0 ở mọi nơi **không thuộc** LC-R-001 (1)–(2). Trọng thương do PvP: hình phạt theo trạng thái nạn nhân (GD-32 PK-R-040); API `severe-injury` nhận cờ `pvpVictimProtected`.
- <a id="lc-r-061"></a>**LC-R-061** Hậu quả, thực hiện tuần tự trong một transaction:
  1. Tiêu thọ nguyên theo bảng LC-R-040 (`SEVERE_INJURY`).
  2. Trừ tu vi `params.severeInjuryExpLossPct` (5%) của **yêu cầu tu vi tầng hiện tại**, không xuống dưới 0 của tầng (không bao giờ tụt tầng).
  3. Thêm status `WEAKENED` trong `params.weakenedSec` (600s thực): −20% công và phòng.
  4. Hồi sinh tại truyền tống trận đã kích hoạt gần nhất **trong cùng vực**. Không có: nếu nhân vật **chưa kích hoạt truyền tống trận nào** (tập truyền tống trận đã kích hoạt rỗng — GD-21 §3.5) thì hồi sinh tại điểm xuất hiện của xuất thân `Background.start` (`mapId` + `poiId`, GD-27 TLN-R-032; Vực 1: miếu hoang `V1_F01_LM_001`) để không đứt phần hướng dẫn (GD-18 OB-AC-003); còn lại về `HUB` của vực.
  5. Ghi `severe_injury_log`.
- <a id="lc-r-062"></a>**LC-R-062** Nếu có ≥ `params.heartDemonInjuryCount` (3) lần trọng thương trong `params.heartDemonWindowSec` (3600s thực), roll `params.heartDemonChance` (30%) nhận status `HEART_DEMON` (hiệu ứng định nghĩa ở GD-04 / GD-27).
- <a id="lc-r-063"></a>**LC-R-063** Vật phẩm trong túi và trang bị **không mất** khi trọng thương.

### 5.7. Chết thật và các lớp bảo vệ
Khi Game Server phát hiện tình huống chết thật (LC-R-001 (1) hoặc (2)), nó gửi `LethalEvent` (§8.1). API Service xử lý theo **thứ tự ưu tiên**:

- <a id="lc-r-070"></a>**LC-R-070** **(1) Bảo hộ tân thủ** (D-13): nếu `generation == 1` **và** RealmCode < `TC1` thì **không chết**:
  - Ở hiểm địa: đưa nhân vật về lối vào hiểm địa (bản đồ ngoài); toàn bộ vật phẩm `DANGER_BAG` chuyển `DESTROYED`; trang bị và túi thường giữ nguyên.
  - Tiêu thọ nguyên `NEWBIE_PROTECTION_PENALTY` (10 năm), status `WEAKENED` `params.newbieWeakenedSec` (1800s).
  - Trả kết quả `PROTECTED_NEWBIE`. **Không tiêu Thế Thân Phù.**
- <a id="lc-r-071"></a>**LC-R-071** **(2) Thế Thân Phù:** nếu trong `BAG` (không tính Vault) có vật phẩm tag `DEATH_WARD` và nhân vật không trong cooldown `params.deathWardCooldownSec` (86400s thực) thì:
  - Tiêu 1 Thế Thân Phù (item_log `DEATH_WARD_CONSUMED`); hồi sinh tại chỗ 30% HP, bất tử 3s.
  - Trả kết quả `WARD_CONSUMED`. Thế Thân Phù **không** cứu được `LIFESPAN_EXHAUSTED`.
- <a id="lc-r-072"></a>**LC-R-072** **(3) Chết thật:** chuyển `ALIVE → ENDING` theo §5.8; trả kết quả `DIED`.
- <a id="lc-r-073"></a>**LC-R-073** `LethalEvent` **idempotent** theo `eventId`: gửi lại cùng `eventId` trả lại đúng kết quả lần đầu, không xử lý lần hai.
- <a id="lc-r-074"></a>**LC-R-074** Mỗi `LethalEvent` kèm `deathLogId` (60 giây sự kiện cuối, PLAN §5.3). Thiếu death log thì API Service vẫn xử lý nhưng gắn cờ `needsReview = true` cho Death Review.
- <a id="lc-r-075"></a>**LC-R-075** Mất kết nối trong hiểm địa: trạng thái *quy tức* do GD-28 định nghĩa. Nếu chết khi đang quy tức, xử lý như chết thật nhưng **luôn** gắn `needsReview = true`.

### 5.8. Quy trình kết thúc kiếp
Bước 1–6 chạy trong **một transaction** khi vào `ENDING`; bước 7–12 chạy trong **một transaction** khi vào `ENDED`.

| Bước | Hành động |
|:----:|-----------|
| 1 | Khóa dòng `character` (`SELECT … FOR UPDATE`); kiểm tra `lifeState == ALIVE` |
| 2 | Ghi `endCause`, `diedAt = now`, `lifeState = ENDING`, `endingDeadline = now + params.endingWindowSec` (86400s) |
| 3 | Nếu chết trong hiểm địa: toàn bộ vật phẩm ở `DANGER_BAG`, `BAG`, `EQUIP` chuyển `CORPSE` (GD-28 DZ-R-015, DZ-R-060), GD-28 tạo di hài |
| 4 | Hủy mọi phiên đang mở của nhân vật: cơ duyên (GD-22 → `ABORTED`), giao dịch, tổ đội |
| 5 | Phát `lifecycle.life_ending`; Game Server gỡ thực thể khỏi bản đồ, gửi `S2C_LifeEnded` |
| 6 | Chụp nhanh dữ liệu cho màn tổng kết (snapshot `LifeRecord` nháp) |
| 7 | Áp dụng lựa chọn của người chơi (chỉ hợp lệ trong `ENDING`): gửi vật phẩm vào Tàng Bảo Các (LC-R-081), lời trăn trối, di sản (Beta, LC-R-140) |
| 8 | Linh thạch: `LIFESPAN_EXHAUSTED` chuyển `params.stoneCarryoverPct` (20%) số dư vào `vaultSpiritStones` (không vượt `params.vaultStoneCap`); các `EndCause` khác chuyển 0% |
| 9 | (a) Vật phẩm còn `SEALED` của Di Tàng **kiếp trước** → `DESTROYED` (`SEAL_DISSIPATED`, LC-R-166). (b) Vật phẩm còn lại ở `BAG`, `EQUIP` → `DESTROYED` (đồ mang theo rớt). (c) `WAREHOUSE`, `MAIL` chưa nhận, tin đấu giá đang treo (hủy tin) → **`SEALED`** vào Di Tàng mới (LC-R-160), trừ vật phẩm `noVault` → `DESTROYED`. (d) Động phủ → `caveSealed = true` (GD-30 HM-R-044). Vật phẩm `CORPSE` giữ nguyên tới khi di hài hết hạn (GD-28) |
| 10 | Tính điểm Luân Hồi (§6.4), cộng vào `AccountRebirth`; cập nhật **ký ức nghề** `profession_memory` (GD-10 AL-R-110) |
| 11 | Ghi `LifeRecord` chính thức (Bia Vạn Cổ); `lifeState = ENDED`, `endedAt = now` |
| 12 | Phát `lifecycle.life_ended` |

- <a id="lc-r-080"></a>**LC-R-080** Dữ liệu cấp tài khoản **không bị ảnh hưởng**: Tiên Ngọc (`JADE`), cosmetic, danh hiệu, Vạn Giới Lục, fog of war, truyền tống trận đã kích hoạt (GD-21 §3.5), bạn bè, tông môn (nhân vật kiếp sau vào lại với trạng thái "chuyển thế đệ tử").
- <a id="lc-r-081"></a>**LC-R-081** Trong `ENDING` (chỉ khi `endCause == LIFESPAN_EXHAUSTED`): người chơi được chọn vật phẩm từ `BAG`/`EQUIP` gửi vào Tàng Bảo Các, tối đa bằng số ô trống. Vật phẩm `noVault` không được chọn. Với `EndCause` khác, chỉ những gì **đã nằm sẵn** trong Tàng Bảo Các được giữ. Bước chọn linh thú giữ lại: [GD-25 SB-R-090](26-spirit-beasts.md); unlock `UNL_BEAST_STABLE_SLOT` (+1 ô Linh Thú Viên, tối đa 2). Kho đồ (`WAREHOUSE`) **không** cần chọn: tự động phong ấn (§5.15).
- <a id="lc-r-082"></a>**LC-R-082** Hết `endingDeadline` mà chưa xác nhận: tự kết thúc với lựa chọn rỗng (không gửi thêm gì vào Vault, không di sản).

### 5.9. Điểm Luân Hồi
- <a id="lc-r-090"></a>**LC-R-090** Tính một lần ở bước 10, công thức §6.4.
- <a id="lc-r-091"></a>**LC-R-091** Thưởng **lần đầu đạt cảnh giới lớn** (`firstRealmBonus`) chỉ một lần mỗi tài khoản trên server, không nhân hệ số kết cục.
- <a id="lc-r-092"></a>**LC-R-092** Ghi `rebirth_ledger` (reason `LIFE_ENDED`, `UNLOCK_PURCHASE`, `GM_ADJUST`).

### 5.10. Cửa hàng Luân Hồi
- <a id="lc-r-100"></a>**LC-R-100** Danh mục trong `rebirth_unlocks.json`. Mỗi unlock có: `id`, `cost`, `requires` (điều kiện trên metadata tài khoản và/hoặc tiến trình server theo LC-R-164), `stackMax`, `effect`.
- <a id="lc-r-101"></a>**LC-R-101** Mua được bất cứ lúc nào (kể cả khi đang có nhân vật `ALIVE`); hiệu lực của unlock loại "lúc tạo nhân vật" chỉ áp dụng cho kiếp **tiếp theo**.
- <a id="lc-r-102"></a>**LC-R-102** Không hoàn điểm. Không quy đổi qua lại với Tiên Ngọc (LC-R-005).
- <a id="lc-r-164"></a>**LC-R-164** `generationCount` được lưu và tăng độc lập theo `(accountId, serverId)`, chỉ tăng cùng transaction confirm tạo nhân vật thành công. Với điều kiện generation của unlock cấp account, dùng giá trị lớn nhất trong các server của tài khoản; unlock đã mua vẫn dùng chung trên mọi server.

Danh mục MVP:

| ID | Hiệu ứng | Giá | Điều kiện | Tối đa |
|----|----------|:---:|-----------|:------:|
| `UNL_REROLL` | +1 lượt roll lại khi tạo nhân vật | 50 × (số đã mua + 1) | — | 5 |
| `UNL_LOCK_LINE` | Được khóa 1 dòng khi roll lại | 300 | Đã mua ≥ 2 `UNL_REROLL` | 1 |
| `UNL_BG_NOBLE` | Mở xuất thân Thế gia công tử | 200 | Từng đạt `TC1` | 1 |
| `UNL_BG_ORPHAN` | Mở xuất thân Cô nhi lưu lạc | 200 | — | 1 |
| `UNL_BG_SECT_SERVANT` | Mở xuất thân Tông môn tạp dịch | 250 | Từng vào Thanh Vân Tông (`FLAG_ACC_JOINED_SECT`) | 1 |
| `UNL_BG_TAC_THIEN_GANG` | Mở xuất thân Bang chúng Tạc Thiên | 250 | Từng vào Tạc Thiên Bang (`FLAG_ACC_JOINED_TAC_THIEN`) | 1 |
| `UNL_BG_HOP_HOAN_DISCIPLE` | Mở xuất thân Hợp Hoan đệ tử | 250 | Từng vào Hợp Hoan Tông (`FLAG_ACC_JOINED_HOP_HOAN`) | 1 |
| `UNL_BAG_SLOTS` | +5 ô túi (nguồn trong game của mở rộng túi, GD-19) | 150 × (số đã mua + 1) | — | 4 (= 20 ô) |
| `UNL_BG_OUTCAST` | Mở xuất thân Phế vật bị trục xuất | 500 | `max(generationCount across servers) ≥ 3` | 1 |
| `UNL_VAULT_SLOT` | +1 ô Tàng Bảo Các | 150 / 300 / 600 | — | 3 (tối đa 6 ô) |
| `UNL_PAST_LIFE_MEMORY` | Kiếp mới chọn 1 đan phương / khí phổ đã biết ở kiếp trước, bắt đầu với nó ở mức thuần thục 1 | 300 | — | 1 |
| `UNL_TALENT_POOL_*` | Thêm thiên phú vào pool roll (GD-27) | 100–400 | Theo từng thiên phú | 1 mỗi loại |

### 5.11. Tàng Bảo Các (Vault)
- <a id="lc-r-110"></a>**LC-R-110** Số ô mặc định `params.vaultBaseSlots` (3), tối đa `params.vaultMaxSlots` (6 ở MVP). Một ô chứa một vật phẩm hoặc một chồng (stack).
- <a id="lc-r-111"></a>**LC-R-111** Gửi / rút chỉ tại NPC `NPC_VAULT_KEEPER` ở `HUB`, khi `lifeState == ALIVE` (ngoại lệ: LC-R-081).
- <a id="lc-r-112"></a>**LC-R-112** Không gửi được vật phẩm có cờ `noVault` (vật phẩm nhiệm vụ, vật phẩm gắn kiếp hiện tại).
- <a id="lc-r-113"></a>**LC-R-113** Vật phẩm rút ra vẫn giữ `requiredRealm`: nhân vật kiếp mới **cầm được nhưng không dùng / trang bị được** khi chưa đủ cảnh giới.
- <a id="lc-r-114"></a>**LC-R-114** Kho linh thạch riêng `vaultSpiritStones`, trần `params.vaultStoneCap` (5.000, theo GD-13 EC-R-060). Gửi / rút như vật phẩm.
- <a id="lc-r-115"></a>**LC-R-115** Mọi thao tác ghi `item_log` (`VAULT_DEPOSIT`, `VAULT_WITHDRAW`, `ENDING_VAULT_DEPOSIT`).

### 5.12. Bia Vạn Cổ
- <a id="lc-r-120"></a>**LC-R-120** Mỗi kiếp kết thúc tạo đúng 1 `LifeRecord` (bước 11).
- <a id="lc-r-121"></a>**LC-R-121** Bảng xếp hạng: cảnh giới cao nhất, tuổi thọ dài nhất, nhiều cơ duyên nhất; phạm vi toàn thời gian và theo mùa.
- <a id="lc-r-122"></a>**LC-R-122** `notableEvents` ghi tự động: đột phá cảnh giới lớn, cơ duyên phẩm `DIA`/`THIEN`, lần đầu vào hiểm địa mỗi cấp, thay đổi thiên phú.
- <a id="lc-r-123"></a>**LC-R-123** Trang tiểu sử có link chia sẻ công khai (không lộ thông tin tài khoản).

### 5.13. Bế quan
- <a id="lc-r-130"></a>**LC-R-130** **Bế quan online:** tại POI `LEY_LINE`, điểm đả tọa ở `HUB`, Tĩnh Thất động phủ (`CAVE`) hoặc Linh Mạch Điện (`SECT_GROUNDS`) (GD-30), chọn số năm `Y ∈ [1, params.seclusionMaxYears]` (10). Tiêu ngay `Y` năm (`SECLUSION_ONLINE`), nhận tu vi theo §6.5, cutscene 5 giây. Không dùng được khi `warningLevel == LAST_LIGHT`.
- <a id="lc-r-131"></a>**LC-R-131** Tu vi nhận được bị chặn ở bình cảnh (GD-04). Nếu phần vượt bình cảnh > 0, UI cảnh báo *"Tu vi sẽ lãng phí"* trước khi xác nhận.
- <a id="lc-r-132"></a>**LC-R-132** **Bế quan offline** (chốt LC-Q-1 / PLAN Q6): chọn `Y ∈ [1, params.seclusionMaxYears]` (10) năm tại `LEY_LINE`, Tĩnh Thất động phủ hoặc Linh Mạch Điện, rồi đăng xuất. Lần đăng nhập sau: `yearsConsumed = min(Y, floor(offlineRealSec / params.offlineSecPerYear))` với `offlineSecPerYear = 1800` (2 năm / giờ offline — nhanh gấp đôi đồng hồ online D-11, vì người chơi không có mặt), tiêu `yearsConsumed` (`SECLUSION_OFFLINE`), nhận tu vi §6.5 × `offlineSeclusionEfficiency` (0,8). **Tối đa 1 lần bế quan offline mỗi ngày thực** (reset 05:00) — bế quan offline là tiện ích cho người bận, không phải cách tu tối ưu.
- <a id="lc-r-133"></a>**LC-R-133** Bế quan offline không bao giờ khiến nhân vật tọa hóa khi offline: `yearsConsumed` bị chặn để `lifespanRemainingYears ≥ 1`.

### 5.14. Di sản / Động phủ truyền thừa (Beta)
- <a id="lc-r-140"></a>**LC-R-140** Chỉ khi `endCause == LIFESPAN_EXHAUSTED`: chọn tối đa `params.legacyMaxItems` (3) vật phẩm + 1 lời nhắn → tạo `LegacyCave`. Vật phẩm chuyển `LEGACY`. Cơ chế xuất hiện và nhận thưởng do GD-22 §5.9 định nghĩa.

---

### 5.15. Di Tàng phong ấn & Tàn hồn
- <a id="lc-r-160"></a>**LC-R-160** Khi kiếp kết thúc (**mọi** `EndCause`), tài sản chia bốn nhóm: Ma trận kỹ thuật đầy đủ (bảng nào, phạm vi nào, xử lý gì): [TD-03 §4](../tech/02-database.md).

| Nhóm | Gồm | Số phận |
|------|-----|---------|
| **Mang theo** | `BAG`, `EQUIP`, `DANGER_BAG`, ví linh thạch | **Rớt.** Chết trong hiểm địa → di hài (GD-28, người khác nhặt được). Khác → hủy. Riêng tọa hóa: vẫn chọn gửi Tàng Bảo Các (LC-R-081) và 20% linh thạch (bước 8) |
| **Cất giữ** | `WAREHOUSE`, `MAIL` chưa nhận, vật phẩm đang treo đấu giá | **Phong ấn** vào **Di Tàng** (`ItemLocation.SEALED`, thuộc tài khoản). Kiếp sau đánh bại **Tàn hồn** để mở (LC-R-162..165) |
| **Động phủ** | Công trình, Linh Điền, Linh Thú Viên + linh thú (GD-25) | **Phong ấn** (`caveSealed`). Kiếp sau đánh bại **thú canh cửa / Thạch Hộ Pháp** để mở (GD-30 HM-R-045) |
| **Cấp tài khoản** | Tàng Bảo Các, `JADE`, cosmetic, Vạn Giới Lục… (LC-R-080) | Giữ nguyên, dùng ngay |

- <a id="lc-r-161"></a>**LC-R-161** **Di Tàng:** mỗi tài khoản trên server có tối đa **một** Di Tàng — chứa đồ cất giữ của **kiếp vừa kết thúc**. Mỗi vật phẩm được gán **tầng ấn** `sealBand` theo cảnh giới lớn của vật phẩm: `requiredRealm` (trang bị) → nếu không có thì `itemRealm` (GD-09) → nếu không có thì `LK`. Bốn tầng: `LK` (gồm `PN`) · `TC` · `KD` · `NA` (gồm cảnh giới cao hơn).
- <a id="lc-r-162"></a>**LC-R-162** **Mở ấn:** tại POI `REMNANT_ALTAR` (**Luân Hồi Đài**, ngoại ô Thanh Vân Trấn, GD-21), chọn một tầng còn vật phẩm. Điều kiện: nhân vật `ALIVE`, cảnh giới ≥ `bandEntryRealm` (LK: `LK4` · TC: `TC1` · KD: `KD1` · NA: `NA1`), không có tầng nào khác đang mở dở. Vào instance một người **Tàn Hồn Cảnh** (`MapType.INSTANCE`, **không phải** hiểm địa — thua không chết thật, LC-R-001). **Phí giải ấn** (chốt GD-13 EC-Q-6): nộp khi vào Tàn Hồn Cảnh, không hoàn khi thua — tầng LK 100 · TC 600 · KD 2.500 · NA 10.000 linh thạch (`REMNANT_TRIAL_FEE`, hố tiền bù cho phần hủy đồ đã giảm).
- <a id="lc-r-163"></a>**LC-R-163** **Tàn hồn** là bóng ma của nhân vật kiếp trước, dựng từ `RemnantSnapshot` chụp ở bước 6: tên, ngoại hình, vũ khí, tâm pháp chính, tối đa 5 kỹ năng đã trang bị (không có tuyệt kỹ, không có bí pháp tiêu thọ), lời trăn trối. Chỉ số = bảng cơ bản (GD-06) ở `remnantRealm[band]` (LK: `LK6` · TC: `TC2` · KD: `KD2` · NA: `NA2`), **không vượt** cảnh giới cao nhất kiếp trước, máu ×3, dùng mẫu quái `MON_B_REMNANT` (GD-12): AI theo vũ khí, mọi chiêu có vùng cảnh báo đạt mức tối thiểu của boss (GD-12 EN-R-070). Hai pha: ở 50% máu vào "Chấp niệm" (thêm 1 kỹ năng, nói lời trăn trối).
- <a id="lc-r-164"></a>**LC-R-164** **Thắng:** mọi vật phẩm của tầng đó chuyển `SEALED → WAREHOUSE` của nhân vật hiện tại (tràn → `MAIL` theo GD-09 IT-R-012), ghi `SEAL_RELEASED`. Ràng buộc (`bind`) giữ nguyên và coi như thuộc nhân vật hiện tại. Lần đầu thắng tàn hồn mỗi tầng trong đời tài khoản: danh hiệu "Siêu thoát chấp niệm · {tầng}".
- <a id="lc-r-165"></a>**LC-R-165** **Thua / rời instance:** không mất vật phẩm, không tiêu thọ; hồi chiêu tầng đó `remnantRetryCooldownSec` (1800s thực). Mất kết nối trong Tàn Hồn Cảnh = thua.
- <a id="lc-r-166"></a>**LC-R-166** **Tàn hồn tiêu tán:** khi kiếp **hiện tại** kết thúc, mọi vật phẩm còn `SEALED` của Di Tàng cũ bị hủy (`SEAL_DISSIPATED`, bước 9a) trước khi Di Tàng mới được tạo. Màn tổng kết và HUD (từ cảnh báo Đại hạn) nhắc: *"Di Tàng kiếp trước còn N vật phẩm chưa lấy lại."* Hệ quả có chủ đích: đồ của tầng cao hơn cảnh giới mà kiếp này đạt được sẽ mất nếu kiếp này cũng kết thúc sớm.
- <a id="lc-r-167"></a>**LC-R-167** Vật phẩm `noVault` (nhiệm vụ, gắn kiếp) không được phong ấn (hủy). Linh thạch **không** phong ấn (nằm trong ví, thuộc nhóm "mang theo").
- <a id="lc-r-168"></a>**LC-R-168** Di Tàng **không** giao dịch, không xem được nội dung chi tiết của tầng chưa mở (chỉ thấy số vật phẩm và phẩm cao nhất mỗi tầng — để biết đáng đánh tới đâu).

### 5.16. Binh giải (chốt PLAN Q21)
- <a id="lc-r-170"></a>**LC-R-170** **Binh giải** (*tự giải thể nhục thân, giữ chân linh*): chỉ dùng được **trong instance thiên kiếp lethal** (GD-04, Nguyên Anh trở lên), từ khi sóng đầu tiên bắt đầu tới trước khi HP về 0. Giữ phím 3s, xác nhận "Binh giải — kết thúc kiếp này". Không dùng được ở nơi khác (không có "tự sát để lấy điểm").
- <a id="lc-r-171"></a>**LC-R-171** Kết quả: `endCause = SELF_DISSOLUTION`, kiếp kết thúc qua quy trình §5.8 với các ưu đãi **như tọa hóa**: được chọn gửi Tàng Bảo Các (LC-R-081), chọn linh thú giữ lại, di sản (LC-R-140); linh thạch chuyển `stoneCarryoverPct` × 0,5 (10%); `endMultiplier = 0,9` (giữa tọa hóa 1,0 và độ kiếp thất bại 0,8).
- <a id="lc-r-172"></a>**LC-R-172** Ý nghĩa thiết kế: người chơi thấy thiên kiếp quá nguy hiểm có lối thoát **có giá** (mất kiếp, nhưng giữ được nhiều hơn so với chết trong kiếp), thay vì "đánh cược tất cả". Bia Vạn Cổ ghi *"Binh giải giữa lôi kiếp"*; thông báo như tọa hóa (SO-R-04J).

## 6. Công thức

### 6.1. Tuổi
```
ageYears = startAge + lifespanUsedSec / secPerYear
```
Ví dụ: `startAge = 16`, `lifespanUsedSec = 252.000`, `secPerYear = 3600` → `ageYears = 16 + 70 = 86`.

### 6.2. Thọ nguyên tối đa
```
lifespanMaxYears = (realmLifespanYears[realm] + innateLifespanBonusYears)
                   × (1 + Σ talentLifespanPct)
                   + effectiveExtensionYears
```
- `realmLifespanYears`: PN 80 · LK 120 · TC 250 · KD 500 · NA 1000 · HT 2000.
- `talentLifespanPct`: từ thiên phú (vd. `TAL_LONGEVITY_BODY` +0,30, `TAL_SHORT_LIVED_GENIUS` −0,30), tổng bị chặn trong `[−0,5; +1,0]`.
- `effectiveExtensionYears = min(lifespanExtensionYears, extensionCapPct × realmLifespanYears[realm])`, `extensionCapPct = 0,2`.

Ví dụ: Trúc Cơ, innate +10, có Trường Sinh Thể (+0,30), đã cộng 70 năm từ đan:
- Trần extension = 0,2 × 250 = 50 → hiệu lực 50.
- `(250 + 10) × 1,3 + 50 = 388` năm.

### 6.3. Cộng thọ từ đan dược / vật phẩm (giảm dần)
```
gainYears = baseYears × extensionDecay ^ n
```
- `n` = số lần **đã dùng cùng `itemId`** trong kiếp này (bảng `pill_usage`), `extensionDecay = 0,5`.
- Ví dụ Diên Thọ Đan (`baseYears = 20`): lần 1 +20, lần 2 +10, lần 3 +5.
- Phần vượt trần §6.2 vẫn được ghi vào `lifespanExtensionYears` nhưng không có hiệu lực cho tới khi lên cảnh giới cao hơn (trần tăng). UI phải báo *"Thọ nguyên đã chạm giới hạn của cảnh giới hiện tại"*.

### 6.4. Điểm Luân Hồi
```
base  = realmScore[finalRealmCode]
      + floor(ageYears − startAge) × 0,5
      + uniqueEncounters × 5
      + newCodexEntriesThisLife × 1
      + achievementPointsThisLife
points = floor(base × endMultiplier[endCause]) + Σ firstRealmBonus (chưa từng nhận)
```

| `realmScore` | Giá trị |
|--------------|---------|
| `PN1` | 0 |
| `LK{n}` | 5 × n (LK9 = 45) |
| `TC1..4` | 60 · 75 · 90 · 105 |
| `KD1..4` | 150 · 180 · 210 · 240 |
| `NA1..4` | 400 · 450 · 500 · 550 |

| `endMultiplier` | |
|-----------------|---|
| `LIFESPAN_EXHAUSTED` | 1,0 |
| `SELF_DISSOLUTION` (binh giải, §5.16) | 0,9 |
| `TRIBULATION_FAILED` | 0,8 |
| `SLAIN_DANGER_ZONE` | 0,6 |
| `ASCENDED` | 2,0 |

`firstRealmBonus`: TC 100 · KD 300 · NA 800 (mỗi tài khoản / server một lần).

Ví dụ: chết trong hiểm địa ở `TC2`, 140 tuổi, 12 cơ duyên khác nhau, 30 mục Vạn Giới Lục mới, 20 điểm thành tựu, lần đầu đạt Trúc Cơ:
- `base = 75 + 62 + 60 + 30 + 20 = 247`
- `points = floor(247 × 0,6) + 100 = 148 + 100 = 248`.

### 6.5. Tu vi từ bế quan
```
expGain = Y × seclusionExpPctPerYear × stageRequirementExp(currentRealmCode) × cultivationSpeed × locationMultiplier
```
- `seclusionExpPctPerYear = 0,02` (2% yêu cầu tầng hiện tại mỗi năm).
- `locationMultiplier`: `LEY_LINE` 1,5 · `HUB` 1,0 · `CAVE` ≤ 1,25 · `SECT_GROUNDS` ≤ 1,35 (GD-30 §6.1). `cultivationSpeed` và `stageRequirementExp` theo [GD-04 §6.1–6.2](10-cultivation.md) (công thức chuẩn ở GD-04 §6.4).
- Ví dụ: bế quan 10 năm tại linh mạch, efficiency 1,5 → nhận 30% yêu cầu tầng hiện tại.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/lifecycle/params.json` | Tham số toàn cục (bên dưới) |
| `shared/gamedata/lifecycle/realm_lifespan.json` | `realmLifespanYears` theo `Realm` |
| `shared/gamedata/lifecycle/lifespan_costs.json` | Bảng LC-R-040 |
| `shared/gamedata/lifecycle/rebirth_scoring.json` | `realmScore`, `endMultiplier`, `firstRealmBonus`, hệ số §6.4 |
| `shared/gamedata/lifecycle/rebirth_unlocks.json` | Danh mục §5.10 |

```jsonc
// params.json
{
  "startAge": 16,
  "secPerYear": 3600,
  "innateLifespanMin": -20, "innateLifespanMax": 20,
  "timeFlow": { "ACTIVE": 1.0, "TOWN": 0.25, "AFK": 0.25, "PAUSED": 0 },
  "afkThresholdSec": 300,
  "accrualReportSec": 60, "accrualToleranceSec": 5,
  "warning": { "noticePct": 0.25, "greatLimitPct": 0.10, "criticalPct": 0.02, "criticalMinYears": 1 },
  "lastLightRealSec": 600, "lastLightStatBonusPct": 0.20, "lastLightCombatGraceSec": 60,
  "severeInjuryExpLossPct": 0.05, "weakenedSec": 600,
  "heartDemonInjuryCount": 3, "heartDemonWindowSec": 3600, "heartDemonChance": 0.30,
  "newbieMaxRealmExclusive": "TC1", "newbieWeakenedSec": 1800,
  "deathWardCooldownSec": 86400, "deathWardReviveHpPct": 0.30, "deathWardInvulnSec": 3,
  "endingWindowSec": 86400,
  "stoneCarryoverPct": 0.20,
  "vaultBaseSlots": 3, "vaultMaxSlots": 6, "vaultStoneCap": 5000,
  "extensionDecay": 0.5, "extensionCapPct": 0.20, "talentLifespanPctMin": -0.5, "talentLifespanPctMax": 1.0,
  "seclusionMaxYears": 10, "seclusionExpPctPerYear": 0.02,
  "offlineSecPerYear": 1800, "offlineSeclusionEfficiency": 0.8,
  "legacyMaxItems": 3, "lastWordsMaxLength": 200,
  "gmRestoreWindowSec": 604800
}
```

```jsonc
// lifespan_costs.json
{
  "SEVERE_INJURY": { "PN": 0, "LK": 1, "TC": 2, "KD": 3, "NA": 5, "HT": 8 },
  "BREAKTHROUGH_FAILED": { "TC": 5, "KD": 15, "NA": 30, "HT": 60 },
  "NEWBIE_PROTECTION_PENALTY": 10
}
```

---

## 8. Hợp đồng giao tiếp

### 8.1. API nội bộ (Game Server → API Service)
Xác thực bằng service token; mọi request có `requestId` để truy vết.

| Endpoint | Request | Response | Quy tắc |
|----------|---------|----------|---------|
| `POST /internal/v1/lifecycle/age-accrual` | `{ characterId, reportId, windowStart, windowEnd, buckets: [{ state: TimeFlowState, realSec }] }` | `{ ageYears, lifespanMaxYears, warningLevel, lastLightUntil }` | LC-R-030..032; idempotent theo `reportId` |
| `POST /internal/v1/lifecycle/lifespan-change` | `{ characterId, changeId, kind: LifespanLedgerKind, years, reasonCode, refId, itemId? }` | `LifecycleView` | LC-R-040/041; idempotent theo `changeId`; `itemId` dùng cho §6.3 |
| `POST /internal/v1/lifecycle/severe-injury` | `{ characterId, eventId, mapId, cause }` | `{ lifespanLostYears, expLost, respawn: { mapId, poiId } }` | LC-R-060..063; idempotent theo `eventId` |
| `POST /internal/v1/lifecycle/lethal-event` | `{ eventId, characterId, cause: 'DANGER_ZONE' \| 'TRIBULATION', mapId, deathLogId \| null }` | `{ result: 'DIED' \| 'PROTECTED_NEWBIE' \| 'WARD_CONSUMED', respawn? }` | LC-R-070..075 |
| `POST /internal/v1/lifecycle/last-light-expired` | `{ characterId, eventId, reason: 'TIMEOUT' \| 'BREAKTHROUGH_FAILED', attemptId? }` | `{ result: 'DIED' \| 'DEFERRED' }` | LC-R-033, LC-R-051; `TIMEOUT` trả `DEFERRED` nếu đang chiến đấu hoặc có `BreakthroughAttempt` chưa kết thúc; `BREAKTHROUGH_FAILED` luôn `DIED` |

### 8.2. API công khai (Client → API Service)

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/characters/{id}/lifecycle` | Lấy `LifecycleView` | |
| `GET /v1/characters/{id}/ending` | Dữ liệu màn tổng kết + lựa chọn hợp lệ | Chỉ khi `ENDING` |
| `POST /v1/characters/{id}/ending/confirm` | `{ vaultDepositItemIds: string[], lastWords?: string, legacy?: { itemIds, message } }` | LC-R-081/082/140 |
| `GET /v1/accounts/me/rebirth` | `AccountRebirth` + danh mục unlock khả dụng | |
| `POST /v1/accounts/me/rebirth/unlocks` | `{ unlockId }` | LC-R-100..102 |
| `GET /v1/accounts/me/vault` | Danh sách ô | |
| `POST /v1/accounts/me/vault/deposit` · `/withdraw` | `{ characterId, itemInstanceId? , spiritStones? }` | LC-R-110..115; Game Server xác nhận nhân vật đang đứng gần `NPC_VAULT_KEEPER` |
| `POST /v1/characters/{id}/seclusion` | `{ mode: 'ONLINE' \| 'OFFLINE', years, poiId }` | LC-R-130..133 |
| `GET /v1/life-records/{recordId}` | Trang tiểu sử công khai | LC-R-123 |
| `GET /v1/accounts/me/legacy-stash` | `LegacyStash` (số vật phẩm, phẩm cao nhất mỗi tầng) | LC-R-161, LC-R-168 |
| `POST /v1/characters/{id}/remnant-trials` | `{ band }` → `{ instanceTicket }` | LC-R-162; `CharacterContext` gần `REMNANT_ALTAR` |

### 8.3. Message thời gian thực (Game Server ↔ Client)

| Message | Hướng | Payload | Khi nào |
|---------|-------|---------|---------|
| `S2C_LifecycleUpdate` | S→C | `{ ageYears, lifespanMaxYears, warningLevel, timeFlowState, lastLightUntil }` | Khi vào bản đồ, khi `warningLevel`/`timeFlowState` đổi, và mỗi 60s |
| `S2C_SevereInjury` | S→C | `{ lifespanLostYears, expLost, respawnMapId }` | Sau trọng thương |
| `S2C_DeathProtected` | S→C | `{ kind: 'NEWBIE' \| 'WARD', message }` | Sau LC-R-070/071 |
| `S2C_LifeEnded` | S→C | `{ endCause, characterId }` | Client chuyển sang màn tổng kết |

### 8.4. Sự kiện domain (pub/sub, topic `lifecycle.*`)

| Sự kiện | Payload | Bên nhận tiêu biểu |
|---------|---------|---------------------|
| `lifecycle.life_started` | `{ accountId, characterId, generation }` | Telemetry, GD-27 |
| `lifecycle.warning_level_changed` | `{ characterId, from, to }` | Nhiệm vụ `QC_GREAT_LIMIT_*`, thông báo |
| `lifecycle.last_light_started` | `{ characterId, until }` | Game Server (buff), thông báo |
| `lifecycle.life_ending` | `{ characterId, endCause, mapId }` | Game Server (gỡ thực thể), GD-22 (hủy phiên), GD-28 (di hài) |
| `lifecycle.life_ended` | `{ accountId, characterId, endCause, generation, finalRealmCode, ageYears, rebirthPoints }` | Bia Vạn Cổ, tin toàn server, telemetry, tông môn |

---

## 9. Lưu trữ

```sql
-- Bổ sung cột cho bảng character (PLAN §5.4)
ALTER TABLE character ADD COLUMN life_state            VARCHAR(16) NOT NULL DEFAULT 'ALIVE';
ALTER TABLE character ADD COLUMN generation            INT         NOT NULL;
ALTER TABLE character ADD COLUMN start_age             INT         NOT NULL DEFAULT 16;
ALTER TABLE character ADD COLUMN lifespan_used_sec     BIGINT      NOT NULL DEFAULT 0;
ALTER TABLE character ADD COLUMN innate_lifespan_bonus_years INT   NOT NULL DEFAULT 0;
ALTER TABLE character ADD COLUMN lifespan_extension_years   NUMERIC(10,2) NOT NULL DEFAULT 0;
ALTER TABLE character ADD COLUMN last_light_until      TIMESTAMPTZ;
ALTER TABLE character ADD COLUMN ending_deadline       TIMESTAMPTZ;
ALTER TABLE character ADD COLUMN end_cause             VARCHAR(32);
ALTER TABLE character ADD COLUMN died_at               TIMESTAMPTZ;
ALTER TABLE character ADD COLUMN ended_at              TIMESTAMPTZ;
-- LC-R-006: tối đa 1 nhân vật ALIVE/ENDING mỗi tài khoản (mỗi server = 1 schema/DB)
CREATE UNIQUE INDEX ux_character_one_active ON character(account_id)
  WHERE life_state IN ('ALIVE', 'ENDING');

CREATE TABLE lifespan_ledger (
  id              BIGSERIAL PRIMARY KEY,
  character_id    UUID        NOT NULL REFERENCES character(id),
  kind            VARCHAR(16) NOT NULL,            -- USED | EXTENSION
  delta_years     NUMERIC(10,4) NOT NULL,
  reason_code     VARCHAR(32) NOT NULL,            -- LifespanReason
  ref_id          VARCHAR(64),
  idempotency_key VARCHAR(80) NOT NULL UNIQUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE age_accrual_report (
  report_id       VARCHAR(64) PRIMARY KEY,
  character_id    UUID        NOT NULL REFERENCES character(id),
  window_start    TIMESTAMPTZ NOT NULL,
  window_end      TIMESTAMPTZ NOT NULL,
  effective_sec   NUMERIC(12,2) NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_accrual_char_window ON age_accrual_report(character_id, window_end);

CREATE TABLE pill_usage (
  character_id UUID NOT NULL REFERENCES character(id),
  item_id      VARCHAR(64) NOT NULL,
  uses         INT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, item_id)
);

CREATE TABLE severe_injury_log (
  id BIGSERIAL PRIMARY KEY, character_id UUID NOT NULL, map_id VARCHAR(32) NOT NULL,
  cause VARCHAR(32) NOT NULL, lifespan_lost_years NUMERIC(6,2) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE account_meta (
  account_id            UUID PRIMARY KEY,
  rebirth_points        INT NOT NULL DEFAULT 0,
  rebirth_points_total  INT NOT NULL DEFAULT 0,
  unlocks               JSONB NOT NULL DEFAULT '[]',
  vault_slots           INT NOT NULL DEFAULT 3,
  vault_spirit_stones   BIGINT NOT NULL DEFAULT 0,
  first_reach_realms    JSONB NOT NULL DEFAULT '[]'
);

CREATE TABLE account_server_meta (
  account_id       UUID NOT NULL REFERENCES account(id),
  server_id        SMALLINT NOT NULL REFERENCES server_registry(server_id),
  generation_count INT NOT NULL DEFAULT 0 CHECK (generation_count >= 0),
  PRIMARY KEY (account_id, server_id)
);

CREATE TABLE rebirth_ledger (
  id BIGSERIAL PRIMARY KEY, account_id UUID NOT NULL, delta INT NOT NULL,
  reason_code VARCHAR(32) NOT NULL, ref_id VARCHAR(64), created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE account_vault (
  account_id       UUID NOT NULL,
  slot             INT  NOT NULL,
  item_instance_id UUID NOT NULL UNIQUE,
  PRIMARY KEY (account_id, slot)
);

CREATE TABLE life_record (
  record_id     UUID PRIMARY KEY,
  account_id    UUID NOT NULL,
  character_id  UUID NOT NULL UNIQUE,
  summary       JSONB NOT NULL,                     -- toàn bộ LifeRecord
  final_realm   VARCHAR(8) NOT NULL,
  age_years     NUMERIC(8,2) NOT NULL,
  end_cause     VARCHAR(32) NOT NULL,
  rebirth_points INT NOT NULL,
  ended_at      TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_life_record_rank_realm ON life_record(final_realm, age_years DESC);
```

**Mã lý do `item_log` mới:** `LIFE_ENDED_DESTROYED`, `VAULT_DEPOSIT`, `VAULT_WITHDRAW`, `ENDING_VAULT_DEPOSIT`, `DEATH_WARD_CONSUMED`, `NEWBIE_PROTECTION_DESTROYED`, `MOVED_TO_CORPSE`, `MOVED_TO_LEGACY`.

---


```sql
CREATE TABLE legacy_stash (                          -- §5.15
  account_id UUID PRIMARY KEY, source_character_id UUID NOT NULL, sealed_at TIMESTAMPTZ NOT NULL,
  remnant_snapshot JSONB NOT NULL, bands JSONB NOT NULL      -- { LK: { released, cooldownUntil }, ... }
);
-- item_instance: location = 'SEALED', owner = account, cột seal_band VARCHAR(4) (GD-09)
```
Mã `item_log` mới: `LIFE_ENDED_SEALED`, `SEAL_RELEASED`, `SEAL_DISSIPATED`. Nội bộ: `POST /internal/v1/lifecycle/remnant-result` `{ trialId, characterId, band, won }` (idempotent theo `trialId`).

## 10. UI/UX

- **Thanh tuổi** (HUD, cạnh thanh máu): `Tuổi 86 / 388`, màu theo `warningLevel`; tooltip hiện hệ số thời gian hiện tại (*"Ở thành trấn: thời gian trôi chậm"*).
- **Xác nhận tiêu thọ nguyên** (LC-R-042): hộp thoại có tuổi trước → sau và thọ còn lại.
- **Hồi quang phản chiếu:** viền màn hình vàng nhạt, đồng hồ đếm ngược 10:00, nút tắt "Thử đột phá lần cuối" nếu đủ điều kiện.
- **Màn tổng kết kiếp** (ENDING): tranh thủy mặc vẽ hành trình (cảnh giới qua các mốc, cơ duyên nổi bật), số điểm Luân Hồi, chọn gửi Tàng Bảo Các (chỉ khi tọa hóa), ô lời trăn trối, nút xác nhận. Không được có nút "hồi sinh trả tiền" dưới bất kỳ hình thức nào.
- **Màn Luân hồi:** số dư điểm, cửa hàng unlock, nút "Chuyển thế" dẫn tới tạo nhân vật (GD-27).

---

## 11. Tương tác với hệ thống khác

| Hệ thống | Tương tác | Hướng |
|----------|-----------|-------|
| GD-27 Thiên phú | Cung cấp `innateLifespanBonusYears`, `talentLifespanPct`; nhận unlock xuất thân / pool / roll từ §5.10 | Hai chiều |
| GD-04 Tu luyện | Đột phá đổi `realmLifespanYears`; thất bại tiêu thọ; `stageRequirementExp` cho §6.5; status `HEART_DEMON` | Hai chiều |
| GD-28 Hiểm địa | Gửi `LethalEvent`; nhận vật phẩm `CORPSE`; định nghĩa quy tức, hấp hối | GD-28 → GD-26 |
| GD-22 Cơ duyên | Outcome `CHANGE_LIFESPAN` gọi `lifespan-change`; nhận `life_ending` để hủy phiên; `LegacyCave` | Hai chiều |
| GD-21 Open World | Loại bản đồ quyết định `TimeFlowState`; điểm hồi sinh là truyền tống trận | GD-21 → GD-26 |
| GD-10 / GD-11 Chế tác | Gọi `lifespan-change` với `CRAFTING` | → GD-26 |
| GD-13 Kinh tế | Linh thạch chuyển kiếp (bước 8), vật phẩm bị hủy là sink | Hai chiều |
| GD-14 Xã hội | Bạn bè, tông môn tự lập giữ thành viên cấp tài khoản; Thanh Vân Tông "chuyển thế đệ tử"; tin tọa hóa | GD-26 → GD-14 |
| Death Review (PLAN §6.5) | Hồ sơ `needsReview`; GM có thể đảo ngược cái chết (LC-R-150) | |

- <a id="lc-r-150"></a>**LC-R-150** **GM khôi phục nhân vật** (chết do lỗi): chỉ khi `lifeState == ENDING` hoặc `ENDED` chưa quá `params.gmRestoreWindowSec` (7 ngày); đảo ngược bước 3–11 bằng các bản ghi ledger/item_log tương ứng, ghi `GM_ADJUST`. Yêu cầu 2 GM xác nhận. Khôi phục nhân vật cần **duyệt hai người** (TD-08 SEC-R-023); khóa tài khoản tạm thời không làm nhân vật già / chết (SEC-R-061).

---

## 12. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Hai `LethalEvent` gần như cùng lúc (2 nguồn sát thương) | Khóa dòng + idempotency; event thứ hai thấy `lifeState ≠ ALIVE` → trả kết quả của event đầu |
| `LethalEvent` đến sau khi nhân vật đã chuyển bản đồ | API Service vẫn xử lý theo `mapId` trong event (Game Server đã khóa nhân vật trong khi chờ kết quả) |
| Game Server sập trước khi gửi accrual | Mất tối đa 60s tuổi; chấp nhận được (có lợi cho người chơi) |
| Server sập giữa transaction kết thúc kiếp | Transaction rollback toàn bộ; Game Server gửi lại `LethalEvent` cùng `eventId` khi khởi động |
| Người chơi đăng xuất khi đang `ENDING` | Đăng nhập lại vào thẳng màn tổng kết; hết hạn thì LC-R-082 |
| Đồng hồ client bị chỉnh | Không ảnh hưởng: tuổi tính bằng thời gian server (LC-R-032) |
| Hết thọ khi đang trong hiểm địa | LAST_LIGHT vẫn áp dụng; hết giờ thì tọa hóa tại chỗ; vật phẩm `DANGER_BAG` chuyển `CORPSE` như chết trong hiểm địa nhưng `endCause = LIFESPAN_EXHAUSTED` |
| Hết thọ trong bí cảnh có tổ đội | Tọa hóa tại chỗ; tổ đội nhận thông báo; không ảnh hưởng tiến trình của người khác |
| Bế quan offline nhưng tài khoản bị khóa | Khi mở khóa, áp LC-R-132 với `offlineRealSec` thực tế (vẫn chặn bởi LC-R-133) |
| Mua `UNL_REROLL` khi đang tạo nhân vật dở | Màn tạo nhân vật đọc lại số lượt roll mỗi lần roll |

---

## 13. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `life_started` | generation, backgroundId, talentIds | Phân bố lựa chọn đầu kiếp |
| `lifespan_spent` | reasonCode, years, realmCode, ageYears | Người chơi tiêu thọ vào đâu nhiều nhất |
| `severe_injury` | mapId, realmCode, cause | Bản đồ / quái gây trọng thương nhiều |
| `warning_level` | level, ageYears, realmCode | Bao nhiêu % người chơi chạm đại hạn |
| `last_light_breakthrough` | success | Tỷ lệ "đột phá phút cuối" |
| `death_protected` | kind (NEWBIE/WARD) | Tần suất bảo vệ kích hoạt |
| `life_ended` | endCause, realmCode, ageYears, playHours, generation | **Thời gian sống trung bình, nguyên nhân chết** (KPI PLAN Phase 4) |
| `rebirth_started` | gapHoursSinceEnd | **Tỷ lệ quay lại sau khi chết trong 24h / 7 ngày** (KPI sống còn) |
| `rebirth_unlock_purchased` | unlockId, cost | Giá trị cảm nhận của các unlock |

---

## 14. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="lc-ac-001"></a>LC-AC-001 | Nhân vật mới tạo | Đọc `LifecycleView` | `ageYears = 16`, `realmCode = PN1`, `lifespanMaxYears = 80 + innate` |
| LC-AC-002 | Nhân vật `ACTIVE` trên bản đồ `FIELD` 3600s thực | Gửi đủ các báo cáo accrual | `ageYears` tăng đúng 1,0 (±0,01) |
| LC-AC-003 | Nhân vật offline 8 giờ, không bế quan | Đăng nhập lại | `ageYears` không đổi (LC-R-002) |
| LC-AC-004 | Nhân vật ở `HUB` 3600s | Accrual | `ageYears` tăng 0,25 |
| LC-AC-005 | Báo cáo accrual có tổng `realSec` = cửa sổ + 30s | Gửi lên API | Bị từ chối, không thay đổi tuổi (LC-R-032) |
| LC-AC-006 | Gửi cùng `reportId` / `changeId` / `eventId` hai lần | API xử lý | Chỉ áp dụng một lần, response giống nhau (LC-R-003, LC-R-073) |
| LC-AC-007 | Nhân vật `LK5` hết máu trên `FIELD` | Game Server báo | Trọng thương: −1 năm, −5% yêu cầu tầng (không tụt tầng), `WEAKENED` 600s, hồi sinh ở truyền tống trận gần nhất cùng vực, **không mất vật phẩm** |
| LC-AC-008 | Kiếp 1, `LK7`, chết trong hiểm địa | `LethalEvent` | `PROTECTED_NEWBIE`: không chết, `DANGER_BAG` bị hủy, −10 năm, Thế Thân Phù **không** bị tiêu |
| LC-AC-009 | Kiếp 2, `TC1`, có Thế Thân Phù trong túi, chết trong hiểm địa | `LethalEvent` | `WARD_CONSUMED`: hồi sinh tại chỗ 30% HP; gọi lại trong 24h thì `DIED` |
| LC-AC-010 | Kiếp 2, `TC1`, không có Thế Thân Phù, chết trong hiểm địa | `LethalEvent` | `lifeState = ENDING`, vật phẩm mang vào hiểm địa chuyển `CORPSE`, client nhận `S2C_LifeEnded` |
| LC-AC-011 | `lifespanRemainingYears` về 0 | Accrual tiếp theo | `LAST_LIGHT` 600s, +20% chỉ số; hết giờ → `ENDING` với `LIFESPAN_EXHAUSTED` |
| LC-AC-012 | Đang LAST_LIGHT, đột phá thành công lên cảnh giới có thọ lớn hơn tuổi | Đột phá xong | Gỡ `LAST_LIGHT`, nhân vật tiếp tục sống |
| LC-AC-013 | LAST_LIGHT hết giờ khi đang chiến đấu | Tiếp tục đánh | Trì hoãn tối đa 60s rồi tọa hóa (LC-R-033) |
| LC-AC-014 | Tọa hóa, 3 ô Vault trống, chọn 4 vật phẩm | Confirm | Bị từ chối (vượt số ô); chọn 3 thì thành công |
| LC-AC-015 | Chết trong hiểm địa (`SLAIN_DANGER_ZONE`) | Mở màn tổng kết | Không có lựa chọn gửi Vault; linh thạch chuyển kiếp = 0 |
| LC-AC-016 | Tọa hóa với 30.000 linh thạch, Vault đang có 1.000 | Kết thúc | Vault = min(1.000 + 6.000, 5.000) = 5.000 |
| LC-AC-017 | Kết thúc kiếp | Bước 9 | Mọi vật phẩm `BAG/EQUIP/WAREHOUSE/MAIL` = `DESTROYED`, có `item_log` tương ứng; `JADE` của tài khoản không đổi |
| LC-AC-018 | Ví dụ §6.4 | Tính điểm | Kết quả = 248 |
| LC-AC-019 | Ví dụ §6.2 | Tính thọ | Kết quả = 388 |
| LC-AC-020 | Dùng Diên Thọ Đan (20 năm) 3 lần | Sau mỗi lần | Cộng lần lượt 20, 10, 5 (trước khi áp trần) |
| LC-AC-021 | Tài khoản có nhân vật `ENDING` | Tạo nhân vật mới | Bị từ chối (LC-R-006); sau khi `ENDED` thì được |
| LC-AC-022 | `ENDING` quá 24h không xác nhận | Job định kỳ | Tự `ENDED` với lựa chọn rỗng (LC-R-082) |
| LC-AC-023 | Bế quan offline chọn 10 năm, offline 3 giờ | Đăng nhập | Tiêu 6 năm (`3h × 3600 / 1800`), tu vi = 6 × 2% × yêu cầu tầng × efficiency × 0,8 |
| LC-AC-024 | Bế quan offline khi thọ còn 4 năm, chọn 10 năm, offline 24h | Đăng nhập | Chỉ tiêu 3 năm, nhân vật còn sống (LC-R-133) |
| LC-AC-025 | `LethalEvent` không có `deathLogId` | API xử lý | Xử lý bình thường và `needsReview = true` (LC-R-074) |
| LC-AC-026 | Đang `LAST_LIGHT`, đột phá thất bại | GD-04 gọi `last-light-expired` với `reason = BREAKTHROUGH_FAILED` | `lifeState = ENDING`, `endCause = LIFESPAN_EXHAUSTED`, được chọn gửi Tàng Bảo Các |
| LC-AC-027 | Tọa hóa với 20 vật phẩm trong `WAREHOUSE`, 5 trong `BAG` | Bước 9 | 20 vật phẩm `SEALED` (đúng `sealBand`), 5 vật phẩm `BAG` `DESTROYED` (trừ phần gửi Vault) |
| LC-AC-028 | Chết trong hiểm địa | Bước 3, 9 | Đồ mang theo vào di hài; `WAREHOUSE` vẫn được phong ấn |
| LC-AC-029 | Kiếp mới `LK3` | Mở tầng LK | Từ chối (cần `LK4`) |
| LC-AC-030 | Kiếp trước đạt `TC2`; kiếp mới `LK5` đánh thắng tàn hồn tầng LK | Kết quả | Vật phẩm tầng LK về `WAREHOUSE`; tầng TC vẫn `SEALED` |
| LC-AC-031 | Kiếp trước chỉ đạt `LK7` | Tàn hồn tầng LK | Cảnh giới tàn hồn = `LK6` (≤ cao nhất kiếp trước) |
| LC-AC-032 | Thua tàn hồn | Thử lại ngay | Từ chối tới hết 1800s; không mất vật phẩm |
| LC-AC-033 | Kiếp mới kết thúc khi tầng TC của Di Tàng cũ chưa mở | Bước 9 | Vật phẩm tầng TC cũ `DESTROYED` (`SEAL_DISSIPATED`); Di Tàng mới thay thế |
| LC-AC-034 | Vật phẩm nhiệm vụ (`noVault`) trong kho | Hết kiếp | Hủy, không phong ấn |
| LC-AC-035 | Đang độ thiên kiếp lethal ở `NA1`, sóng 2 | Binh giải | Kiếp kết thúc `SELF_DISSOLUTION`; được gửi Tàng Bảo Các; 10% linh thạch; `endMultiplier` 0,9 |
| LC-AC-036 | Ngoài thiên kiếp | Tìm nút binh giải | Không có |
| LC-AC-037 | Đã bế quan offline hôm nay | Bế quan offline lần 2 | Từ chối tới 05:00 |
| LC-AC-038 | Vào Tàn Hồn Cảnh tầng TC | Phí | Trừ 600 linh thạch (không hoàn khi thua) |
| <a id="lc-ac-039"></a>LC-AC-039 | Nhân vật kiếp 1 `LK1` ở `V1_F01`, chưa kích hoạt truyền tống trận nào | Hết máu (trọng thương) | `severe-injury` trả `respawn = { mapId: "V1_F01", poiId: "V1_F01_LM_001" }` (`Background.start`), không về `V1_HUB` (LC-R-061 bước 4) |
| LC-AC-040 | Tài khoản có generation 3 trên server A, generation 0 trên server B | Kiểm tra điều kiện và mua `UNL_BG_OUTCAST`, sau đó tạo nhân vật trên B | Đủ điều kiện mua theo generation cao nhất; unlock dùng được trên B và generation B chỉ tăng khi confirm thành công |

---

## 15. Phạm vi MVP vs sau này

| Hạng mục | MVP | Beta | Sau launch |
|----------|:---:|:----:|:----------:|
| Đồng hồ tuổi, thọ nguyên, cảnh báo, LAST_LIGHT | ✅ | | |
| Trọng thương, chết thật, bảo hộ tân thủ | ✅ | | |
| Thế Thân Phù | ✅ | | |
| ENDING / ENDED, màn tổng kết, Bia Vạn Cổ cơ bản | ✅ | | |
| Điểm Luân Hồi + cửa hàng (danh mục §5.10) | ✅ | | |
| Tàng Bảo Các | ✅ | | |
| Bế quan online | ✅ | | |
| Bế quan offline | | ✅ | |
| Di sản / động phủ truyền thừa | | ✅ | |
| Di Tàng phong ấn + Tàn hồn (tầng LK, TC) | ✅ | | |
| Tầng KD, NA của Di Tàng | | ✅ | |
| Trang tiểu sử chia sẻ công khai, xếp hạng theo mùa | | ✅ | |
| Phi thăng | | | ✅ |
| Binh giải (Q21) | | | ? |

---

## 16. Câu hỏi mở

- [x] **LC-Q-1** (= PLAN Q6): *(Đã chốt 2026-10-23: 10 năm / lần, 2 năm / giờ offline, hiệu suất 0,8, tối đa 1 lần / ngày — LC-R-132.)* Bế quan offline: `offlineSecPerYear = 1800` (2 năm / giờ offline) và hiệu suất 0,8 có hợp lý? Có giới hạn số lần mỗi ngày không? *Mặc định tạm: như §7, không giới hạn số lần.*
- [ ] **LC-Q-2**: Trọng thương ở bí cảnh tổ đội có nên hồi sinh tại lối vào bí cảnh (để quay lại cứu đồng đội) thay vì truyền tống trận? *Mặc định tạm: lối vào bí cảnh.*
- [x] **LC-Q-3**: Tỷ lệ linh thạch chuyển kiếp 20% và trần 10.000 có làm "kiếp sau quá dễ" không? Chờ dữ liệu kinh tế (GD-13). **→ Đã trả lời ở GD-13 EC-R-060: giữ 20%, hạ trần xuống 5.000.**
- [ ] **LC-Q-4** (= PLAN Q21): Binh giải.
- [x] **LC-Q-5**: Có hiển thị công khai tuổi / thọ nguyên của người chơi khác (khi soi thông tin) không? *Mặc định tạm: chỉ hiện tuổi, không hiện thọ tối đa.* **→ Đã trả lời ở GD-14 SO-R-080: chỉ hiện tuổi, không hiện thọ tối đa.**

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-20 | — | Bản nháp đầu: bất biến, máy trạng thái, đồng hồ tuổi, tiêu/cộng thọ, cảnh báo & LAST_LIGHT, trọng thương, chết thật & bảo vệ, quy trình kết thúc kiếp, Luân Hồi, Vault, Bia Vạn Cổ, bế quan, công thức, gamedata, API, DB, nghiệm thu |
| v0.3 | 2026-10-23 | — | **Di Tàng phong ấn & Tàn hồn** (§5.15, D-20): đồ mang theo rớt; kho đồ / thư / đấu giá phong ấn theo 4 tầng cảnh giới, kiếp sau đánh bại tàn hồn của chính mình tại Luân Hồi Đài để lấy lại; động phủ phong ấn, mở bằng thú canh cửa (GD-30); Di Tàng cũ tiêu tán khi kiếp tiếp theo kết thúc; bước 9 viết lại; LC-AC-027..034 |
| v0.2 | 2026-10-20 | — | Đồng bộ GD-04 v0.2: thất bại đột phá trong `LAST_LIGHT` là tọa hóa; `last-light-expired` thêm `reason`; LC-AC-026 |
| v0.4 | 2026-10-23 | — | D-21: chết do PvP là trọng thương, người vô tội không mất thọ / tu vi (LC-R-001, LC-R-060) |
| v0.5 | 2026-10-23 | — | Chốt Q6 (bế quan offline 10 năm / lần, 1 lần / ngày), **Q21 binh giải** (§5.16, `SELF_DISSOLUTION`, ×0,9, ưu đãi như tọa hóa), phí giải ấn Di Tàng theo tầng (EC-Q-6) |
| v0.6 | 2026-10-23 | — | Trỏ ma trận dữ liệu qua kiếp sang TD-03 §4 |
| v0.7 | 2026-10-25 | — | Unlock `UNL_BAG_SLOTS` (GD-19) |
| v0.8 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
| v0.9 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: LC-R-061 bước 4 — nhân vật chưa kích hoạt truyền tống trận nào hồi sinh tại `Background.start` (Vực 1: miếu hoang `V1_F01_LM_001`) thay vì `HUB`; LC-AC-039 |
| v0.10 | 2026-11-03 | — | Tách generation theo server; điều kiện generation của unlock cấp account dùng giá trị cao nhất giữa các server; LC-R-164, LC-AC-040 |
