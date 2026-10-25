# GD-22 — Cơ duyên (Kỳ ngộ)

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-22 · Prefix ID: `ENC` |
| Owner | Content Designer (nội dung) + Game Designer (hệ thống) |
| Trạng thái | 🟡 Bản nháp v0.7 |
| Cần trước | Phase 1 (engine), Phase 2–3 (nội dung) |
| Quyết định nền | PLAN D-03, D-04, D-14, D-17 · Đánh giá Ink: Q23 |
| Liên quan | [PLAN §4.7](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-21 Open World](22-open-world.md) · [GD-26 Vòng đời](03-lifecycle.md) · GD-27 Thiên phú · GD-28 Hiểm địa · GD-29 RNG & Công bằng · GD-12 Quái · [AR-04 VFX Guidelines](../art/vfx-guidelines.md) §8.2, §11 (ID `presentation.vfx`) |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Tham số nằm ở `shared/gamedata/encounters/params.json`; nội dung cơ duyên ở `shared/encounters/` (§7).

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện*, P5 *Thế giới đáng khám phá*.

**Người chơi cần cảm thấy:**
- *"Tu tiên dựa vào cơ duyên."* Cùng một bản đồ, mỗi kiếp gặp những câu chuyện khác nhau.
- Lựa chọn có hậu quả: cứu hay giết, tham hay buông, đều để lại dấu vết.
- May rủi **công bằng**: thấy được tỷ lệ, có thể tác động bằng khí vận, thần thức, sự chuẩn bị.
- Có chuyện để kể: *"Kiếp này tôi gặp lão ngư ông dưới mưa..."*

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="enc-r-001"></a>**ENC-R-001** | Cơ duyên là **cá nhân**: chỉ người kích hoạt thấy tín hiệu và nội dung. Quái do cơ duyên sinh ra chỉ người đó tương tác được (MVP). |
| <a id="enc-r-002"></a>**ENC-R-002** | Toàn bộ việc đánh giá điều kiện, roll xác suất và chạy kịch bản diễn ra **trên server**. Client chỉ nhận văn bản, lựa chọn và kết quả. |
| <a id="enc-r-003"></a>**ENC-R-003** | Kịch bản (Ink) **không bao giờ chứa ID vật phẩm hay con số phần thưởng**. Kịch bản chỉ gọi `OUTCOME("khóa")`; khóa được định nghĩa trong file định nghĩa cơ duyên và kiểm tra theo danh sách trắng §8. |
| <a id="enc-r-004"></a>**ENC-R-004** | Mọi phần thưởng / hình phạt được áp dụng bởi **API Service trong transaction**, idempotent, ghi ledger với lý do `ENCOUNTER:<encounterId>`. |
| <a id="enc-r-005"></a>**ENC-R-005** | Lựa chọn có yếu tố may rủi phải **hiển thị tỷ lệ ước tính** trước khi chọn (GD-29). |
| <a id="enc-r-006"></a>**ENC-R-006** | Cơ duyên không được cấp Tiên Ngọc (`JADE`) hoặc bất kỳ thứ gì mua bằng tiền thật (D-17). |

---

## 2. Tổng quan

```
 Sự kiện gameplay (Game Server)                    API Service
 ─────────────────────────────                     ───────────
 đến điểm cơ duyên / hái thuốc / hạ quái /
 đả tọa / quét thần thức / vào khu / định kỳ
        │
        ▼
 [1] Cơ hội (Opportunity) theo loại trigger           (§5.1)
        ▼
 [2] Lọc ứng viên theo điều kiện cứng                 (§5.2)
        ▼  (rỗng → dừng, không tăng pity)
 [3] Roll "có xảy ra không" (pTrigger + pity)         (§5.3)
        ▼  (trượt → pity +1)
 [4] Chọn 1 cơ duyên theo trọng số (phẩm cấp × khí vận)
        ▼
 [5] Đặt chỗ phiên ────────────────────────────────► kiểm tra cooldown / độc nhất / giới hạn
        ◄──────────────────────────────── sessionId + seed (hoặc 409 → chọn lại tối đa 2 lần)
        ▼
 [6] OFFERED: hiện tín hiệu (NPC, ánh sáng, âm thanh) (§5.5)
        ▼  người chơi tương tác
 [7] ACTIVE: chạy Ink, lựa chọn, kiểm tra may rủi, chiến đấu
        ▼  mỗi điểm commit
 [8] Commit outcome ───────────────────────────────► kiểm tra danh sách trắng, áp dụng transaction
        ▼
 [9] RESOLVED ─────────────────────────────────────► lịch sử, Vạn Giới Lục, pity, thông báo
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type EncounterTier = 'PHAM' | 'LINH' | 'HUYEN' | 'DIA' | 'THIEN';
type EncounterType = 'DIALOGUE' | 'COMBAT' | 'PUZZLE' | 'INSTANCE' | 'CHAIN_STEP' | 'LEGACY_CAVE'
                   | 'HEART_TRIAL';   // thử thách tâm ma khi đột phá, chỉ do GD-04 mở (§5.12)
type TriggerType =
  | 'POI_PROXIMITY'   // đến gần POI kind ENCOUNTER_POINT (bán kính params.proximityRadius)
  | 'AREA_ENTER'      // vào một Area (GD-21)
  | 'GATHER'          // hái / khai thác thành công
  | 'KILL'            // hạ quái
  | 'MEDITATE'        // mỗi params.meditateTickSec khi đang đả tọa
  | 'SCAN'            // dùng thần thức quét (GD-21 §3.7)
  | 'TIME_TICK'       // mỗi params.timeTickSec online ở FIELD
  | 'CHAIN_STEP';     // bước kế tiếp của chuỗi (§5.8), không roll xác suất
type Uniqueness = 'NONE' | 'ONCE_PER_LIFE' | 'ONCE_PER_ACCOUNT' | 'SERVER_UNIQUE';
type SessionState = 'OFFERED' | 'ACTIVE' | 'RESOLVED' | 'EXPIRED' | 'ABORTED';
type CueKind = 'NPC' | 'GLOW' | 'SOUND' | 'VISION' | 'OBJECT';

interface EncounterDef {
  id: string;                         // ENC_{vực}_{TÊN}, vd. ENC_V1_NGU_ONG
  version: number;                    // tăng khi đổi nội dung; phiên đang chạy giữ version cũ
  tier: EncounterTier;
  type: EncounterType;
  titleKey: string;                   // i18n
  codexHintKey: string;               // gợi ý mờ hiển thị trong Vạn Giới Lục
  trigger: EncounterTrigger;
  conditions: EncounterConditions;
  weight: number;                     // > 0
  cooldown?: { perCharacterRealSec?: number; perCharacterGameDays?: number };
  uniqueness: Uniqueness;
  scripted?: boolean;                 // cơ duyên dàn dựng (GD-18 OB-R-030): bỏ qua roll xảy ra ENC-R-030, không tính pity / giới hạn tần suất ENC-R-011; kích hoạt theo điều kiện của GD sở hữu
  ink: { file: string; knot: string };            // shared/encounters/ink/{file}.{lang}.ink
  checks?: Record<string, CheckDef>;              // §6.3
  outcomes: Record<string, OutcomeSpec[]>;        // khóa → danh sách outcome (§8)
  spawnGroups?: Record<string, SpawnGroupRef>;    // cho START_COMBAT; SpawnGroupRef = { groupId: SG_* } trỏ tới SpawnGroupDef (GD-12 §3, §5.10)
  instanceMapId?: string;                         // cho OPEN_INSTANCE (type INSTANCE)
  presentation: { cue: CueKind; npcModel?: string; vfx?: string; offerTtlSec?: number };   // vfx: ID VfxEffectDef trong vfx/effects.json (V-15), vd. VFX_WLD_ENCOUNTER_GLOW
  chain?: { chainId: string; stepId: string };    // chỉ với type CHAIN_STEP
  broadcast?: 'AUTO' | 'NEVER' | 'ALWAYS';        // mặc định AUTO (§5.10)
}

interface EncounterTrigger {
  type: TriggerType;
  poiIds?: string[];                  // POI_PROXIMITY
  areaIds?: string[];                 // AREA_ENTER
  itemIds?: string[];                 // GATHER (vật phẩm thu được)
  monsterIds?: string[];              // KILL
  monsterRanks?: ('NORMAL' | 'ELITE' | 'BOSS')[];
}

interface EncounterConditions {       // mọi trường đều tùy chọn; có mặt = bắt buộc thỏa
  regions?: string[]; maps?: string[]; areas?: string[];
  realmMin?: string; realmMax?: string;           // RealmCode
  generationMin?: number;
  dayPhases?: DayPhase[]; shiChen?: ShiChen[]; weather?: Weather[];
  rootElementsAny?: Element[]; rootCountMax?: number;
  talentsAll?: string[]; talentsAny?: string[]; talentsNone?: string[];
  backgrounds?: string[];
  karmaMin?: number; karmaMax?: number;
  alignment?: Alignment[];             // phe của nhân vật (GD-14 SO-R-048)
  reputationMin?: Partial<Record<'CHINH' | 'TA' | 'MA', number>>;   // GD-14 §5.4c
  luckMin?: number; comprehensionMin?: number; spiritSenseMin?: number;
  itemsHeld?: { itemId: string; qty: number }[];
  lifeFlagsAll?: string[]; lifeFlagsNone?: string[];
  accountFlagsAll?: string[]; accountFlagsNone?: string[];
  questStates?: { questId: string; state: 'NOT_STARTED' | 'ACTIVE' | 'DONE' }[];   // ánh xạ sang QuestState: GD-15 §3
  warningLevels?: WarningLevel[];                 // vd. cơ duyên chỉ có khi đại hạn (GD-26)
  soloOnly?: boolean;
}

interface CheckDef {                  // kiểm tra may rủi (§6.3)
  stat: 'luck' | 'comprehension' | 'spiritSense' | 'constitution' | 'karma';
  base: number; perPoint: number; pivot?: number;  // pivot mặc định 50 (luck), 0 (khác)
  min: number; max: number;
}

interface EncounterSession {
  sessionId: string;
  characterId: string;
  encounterId: string; encounterVersion: number;
  state: SessionState;
  triggerType: TriggerType; triggerRef: string | null;   // poiId / itemId / monsterId...
  mapId: string; channel: number;
  rngSeed: string;                    // seed server cấp; mọi roll trong phiên dẫn xuất từ seed
  inkState: string | null;            // JSON trạng thái Ink (để khôi phục khi mất kết nối)
  committedOutcomeKeys: string[];
  checkResults: { checkId: string; p: number; roll: number; passed: boolean }[];
  createdAt: string; offeredUntil: string; resolvedAt: string | null;
}
```

---

## 4. Máy trạng thái `EncounterSession`

| Từ | Đến | Điều kiện | Tác dụng phụ |
|----|-----|-----------|--------------|
| — | `OFFERED` | API Service đặt chỗ thành công (ENC-R-040) | Áp cooldown ngay (ENC-R-043); pity reset (ENC-R-032); gửi `S2C_EncounterCue` |
| `OFFERED` | `ACTIVE` | Người chơi tương tác (`C2S_EncounterEngage`) trong TTL và trong bán kính `params.engageRadius` | Khởi chạy Ink với biến từ §5.6 |
| `OFFERED` | `EXPIRED` | Hết `offerTtlSec` hoặc người chơi rời bản đồ | Không outcome; cooldown giữ nguyên |
| `ACTIVE` | `RESOLVED` | Ink tới `END` hoặc `-> DONE` | Commit outcome còn lại; ghi lịch sử, Vạn Giới Lục; thông báo (§5.10) |
| `ACTIVE` | `ABORTED` | Nhân vật vào `ENDING` (GD-26), rời bản đồ, hoặc không phản hồi quá `params.activeIdleSec` | Outcome **đã commit** giữ nguyên; phần chưa commit bỏ |
| `ACTIVE` | `ACTIVE` | Mất kết nối ≤ `params.reconnectGraceSec` | Lưu `inkState`; khôi phục khi kết nối lại |

`RESOLVED`, `EXPIRED`, `ABORTED` là trạng thái cuối.

---

## 5. Quy tắc

### 5.1. Cơ hội kích hoạt
- <a id="enc-r-010"></a>**ENC-R-010** Mỗi sự kiện gameplay khớp `TriggerType` tạo **một cơ hội** cho nhân vật đó. Xác suất gốc `pTrigger` theo loại:

| `TriggerType` | `pTrigger` mặc định | Giới hạn phụ |
|---------------|:-------------------:|--------------|
| `POI_PROXIMITY` | 0,35 | Mỗi POI có cooldown cơ hội riêng theo nhân vật `params.pointCooldownRealSec` (1800s) |
| `AREA_ENTER` | 0,03 | Mỗi khu 1 cơ hội / 10 phút |
| `GATHER` | 0,02 | |
| `KILL` | NORMAL 0,005 · ELITE 0,05 · BOSS 0,15 | |
| `MEDITATE` | 0,05 mỗi 600s đả tọa | |
| `SCAN` | 0,08 | Theo cooldown của thần thức quét |
| `TIME_TICK` | 0,01 mỗi 600s | Chỉ ở `FIELD` |
| `CHAIN_STEP` | 1,0 | Không pity, không giới hạn tần suất toàn cục |

- <a id="enc-r-011"></a>**ENC-R-011** **Giới hạn tần suất toàn cục** theo nhân vật: tối thiểu `params.minIntervalRealSec` (300s) giữa hai phiên; tối đa `params.maxPerRealDay` (30) phiên mỗi 24 giờ thực. Cơ hội trong thời gian giới hạn bị bỏ qua (không roll, không tăng pity). Không áp dụng cho `CHAIN_STEP`.
- <a id="enc-r-012"></a>**ENC-R-012** Nhân vật đang có phiên `OFFERED` hoặc `ACTIVE` không nhận cơ hội mới.
- <a id="enc-r-013"></a>**ENC-R-013** Không có cơ duyên trong `DANGER_ZONE` ở MVP (hiểm địa có nội dung riêng, GD-28) và không có trong `DUNGEON`.

### 5.2. Lọc ứng viên
- <a id="enc-r-020"></a>**ENC-R-020** Ứng viên = mọi `EncounterDef` có `trigger` khớp sự kiện **và** thỏa toàn bộ `conditions` **và** không trong cooldown / không vi phạm `uniqueness` theo dữ liệu đệm của nhân vật (tải khi vào bản đồ, §9.1).
- <a id="enc-r-021"></a>**ENC-R-021** So sánh cảnh giới theo thứ tự `Realm` rồi `realmStage` (Glossary). `realmMin`/`realmMax` đều tính bao gồm.
- <a id="enc-r-022"></a>**ENC-R-022** Điều kiện thời gian / thời tiết lấy từ World Service tại thời điểm cơ hội (GD-21 §3.8–3.9).
- <a id="enc-r-023"></a>**ENC-R-023** Danh sách ứng viên rỗng thì dừng; **không** roll, **không** tăng pity.

### 5.3. Xác suất và chọn cơ duyên
- <a id="enc-r-030"></a>**ENC-R-030** Roll xảy ra: `p = min(params.pMax, pTrigger × (1 + pityMissCount × params.pityStep))`, xem §6.1.
- <a id="enc-r-031"></a>**ENC-R-031** Trượt: `pityMissCount += 1` (một bộ đếm chung cho nhân vật, trừ `CHAIN_STEP`).
- <a id="enc-r-032"></a>**ENC-R-032** Trúng và đặt chỗ thành công: `pityMissCount = 0`.
- <a id="enc-r-033"></a>**ENC-R-033** Chọn một ứng viên theo trọng số §6.2 (phẩm cấp × khí vận × bảo hiểm phẩm cấp).
- <a id="enc-r-034"></a>**ENC-R-034** **Bảo hiểm phẩm cấp:** nếu `sessionsSinceHuyenPlus ≥ params.rarityPityThreshold` (15), mọi ứng viên phẩm `HUYEN` trở lên nhân trọng số `params.rarityPityMult` (3). Bộ đếm reset khi nhận phiên phẩm `HUYEN`+.
- <a id="enc-r-035"></a>**ENC-R-035** Mọi roll dùng RNG an toàn phía server; mỗi lần roll ở bước [3] và [4] ghi `rng_audit` (kind `ENC_TRIGGER`, `ENC_PICK`).

### 5.4. Đặt chỗ, cooldown, độc nhất
- <a id="enc-r-040"></a>**ENC-R-040** Game Server gọi API đặt chỗ (§9.1) trước khi hiện tín hiệu. API kiểm tra **nguyên tử**: nhân vật `ALIVE`, cooldown, `uniqueness`, giới hạn ENC-R-011. Thất bại (409) thì Game Server loại cơ duyên đó và chọn lại trong danh sách còn lại, tối đa `params.reserveRetries` (2) lần; vẫn thất bại thì coi như trượt (không tăng pity).
- <a id="enc-r-041"></a>**ENC-R-041** `uniqueness`:
  - `ONCE_PER_LIFE`: tối đa 1 phiên cho mỗi nhân vật.
  - `ONCE_PER_ACCOUNT`: tối đa 1 phiên cho mỗi tài khoản trên server, qua mọi kiếp.
  - ~~`SERVER_UNIQUE`~~: **không dùng** (chốt ENC-Q-1 / PLAN Q14, 2026-10-23): mọi người chơi đều phải có cơ hội gặp mọi cơ duyên; validator báo lỗi nếu nội dung dùng.
- <a id="enc-r-042"></a>**ENC-R-042** Cooldown tính theo `perCharacterRealSec` hoặc `perCharacterGameDays` (1 ngày game = 120 phút thực, GD-21 §3.8); có cả hai thì lấy mốc **muộn hơn**.
- <a id="enc-r-043"></a>**ENC-R-043** Cooldown và `uniqueness` được ghi ngay khi phiên vào `OFFERED`. Phớt lờ hoặc từ chối cơ duyên **vẫn tính** là đã gặp, để người chơi không "lọc" cơ duyên bằng cách bỏ qua.

### 5.5. Trình bày và tương tác
- <a id="enc-r-050"></a>**ENC-R-050** Tín hiệu (`cue`) xuất hiện trong bán kính `params.cueSpawnRadius` (8–15m) quanh nhân vật, ở vị trí đi được, ưu tiên vị trí của POI kích hoạt nếu có.
- <a id="enc-r-051"></a>**ENC-R-051** `offerTtlSec` mặc định 120s thực. Minimap hiện biểu tượng cơ duyên trong thời gian này.
- <a id="enc-r-052"></a>**ENC-R-052** Trong `ACTIVE`, nhân vật **không bị quái tấn công** (quái mất mục tiêu, nhân vật không thể bị chọn làm mục tiêu) cho tới khi kịch bản gọi `START_COMBAT` hoặc phiên kết thúc. Nếu nhân vật đang trong chiến đấu thì không tương tác được.
- <a id="enc-r-053"></a>**ENC-R-053** Người chơi luôn có lựa chọn rời đi ở bước đầu tiên của mọi cơ duyên (validator kiểm tra: knot gốc phải có ít nhất một lựa chọn dẫn tới `END` mà không gọi `OUTCOME`). Ngoại lệ: `HEART_TRIAL` (§5.12).

### 5.6. Chạy kịch bản Ink
- <a id="enc-r-060"></a>**ENC-R-060** Mỗi ngôn ngữ một file Ink cùng cấu trúc: `shared/encounters/ink/{file}.vi.ink`, `{file}.en.ink`. Validator kiểm tra hai bản có cùng tập knot, stitch, số lựa chọn và lời gọi hàm ngoài.
- <a id="enc-r-061"></a>**ENC-R-061** **Biến chỉ đọc** server gán trước khi chạy (khai báo `VAR` trong Ink với giá trị mặc định để test trong Inky):

| Biến Ink | Nguồn | Kiểu |
|----------|-------|------|
| `realm_code` | RealmCode | string, vd. `"TC2"` |
| `realm_order` | 0 = PN, 1 = LK, 2 = TC, 3 = KD, 4 = NA, 5 = HT | int |
| `generation` | GD-26 | int |
| `luck`, `comprehension`, `spirit_sense`, `constitution`, `karma` | GD-27 | int |
| `root_count` | số nguyên tố linh căn | int |
| `day_phase`, `shi_chen`, `weather` | World Service | string (giá trị enum) |
| `background_id` | GD-27 | string |
| `warning_level` | GD-26 | string |
| `player_name` | tên nhân vật | string |

- <a id="enc-r-062"></a>**ENC-R-062** **Hàm ngoài** (external function) là cách duy nhất kịch bản tác động lên game:

| Hàm | Trả về | Ý nghĩa |
|-----|--------|---------|
| `HAS_TALENT(talentId)` | bool | Chỉ đọc |
| `HAS_ITEM(itemId, qty)` | bool | Chỉ đọc |
| `HAS_FLAG(flag)` | bool | Chỉ đọc, cờ phạm vi kiếp |
| `ROLL_CHECK(checkId)` | bool | Kiểm tra may rủi theo `checks[checkId]` (§6.3); ghi `checkResults` |
| `OUTCOME(key)` | void | Đưa `outcomes[key]` vào hàng chờ commit |
| `COMMIT()` | void | Commit ngay hàng chờ (dùng trước chiến đấu / trước đoạn rủi ro) |
| `START_COMBAT(spawnGroupKey)` | void | Tạm dừng Ink, sinh quái; khi xong gán biến `combat_result` = `"WIN"` / `"LOSE"` / `"FLED"` rồi chạy tiếp |
| `OPEN_INSTANCE()` | void | Đưa nhân vật vào `instanceMapId`; phiên tiếp tục trong instance (Beta) |

- <a id="enc-r-063"></a>**ENC-R-063** Ink **không** được dùng `RANDOM()`, `SEED_RANDOM()` hay mọi nguồn ngẫu nhiên khác ngoài `ROLL_CHECK` (validator chặn).
- <a id="enc-r-064"></a>**ENC-R-064** Tag trên lựa chọn:
  - `#check:<checkId>`: client hiện tỷ lệ ước tính của check đó cạnh lựa chọn (ENC-R-005).
  - `#cost_lifespan:<years>`: client hiện cảnh báo tiêu thọ nguyên (GD-26 LC-R-042).
  - `#locked:<i18nKey>`: hiện lựa chọn bị khóa (mờ) kèm lý do. Lựa chọn khóa dùng điều kiện Ink để không thể chọn.
- <a id="enc-r-065"></a>**ENC-R-065** Kết thúc chiến đấu `LOSE` nghĩa là nhân vật bị trọng thương (GD-26 §5.6). Phiên chuyển `RESOLVED` với outcome đã commit; phần còn lại của Ink không chạy.

### 5.7. Áp dụng outcome
- <a id="enc-r-070"></a>**ENC-R-070** Commit xảy ra khi Ink gọi `COMMIT()`, trước `START_COMBAT`, trước `OPEN_INSTANCE`, và khi phiên `RESOLVED`.
- <a id="enc-r-071"></a>**ENC-R-071** Request commit gồm `commitId` (idempotent) và danh sách **khóa outcome**. API Service kiểm tra mỗi khóa tồn tại trong `outcomes` của đúng `encounterId` + `encounterVersion` của phiên; khóa lạ thì từ chối **toàn bộ** commit và gắn cờ `contentError`.
- <a id="enc-r-072"></a>**ENC-R-072** Toàn bộ outcome trong một commit áp dụng trong **một transaction**; một outcome thất bại (vd. túi đầy) thì xử lý theo §8 cột "Khi không áp dụng được", không rollback outcome khác trừ khi ghi rõ.
- <a id="enc-r-073"></a>**ENC-R-073** Một khóa outcome chỉ được commit **một lần** mỗi phiên (chống lặp do vòng lặp Ink).

### 5.8. Chuỗi cơ duyên
- <a id="enc-r-080"></a>**ENC-R-080** Chuỗi bắt đầu bằng outcome `START_CHAIN` từ một cơ duyên thường. Các bước sau là `EncounterDef` có `type = CHAIN_STEP`, `trigger.type = CHAIN_STEP` + một trigger vị trí (POI / khu), và `chain.stepId`.
- <a id="enc-r-081"></a>**ENC-R-081** Bước chuỗi kích hoạt **xác định** (p = 1) khi nhân vật đang ở đúng bước và thỏa điều kiện vị trí.
- <a id="enc-r-082"></a>**ENC-R-082** Mỗi bước có hạn `expiresGameDays`; quá hạn thì chuỗi thất bại (`FAILED`), có thể kèm outcome an ủi định nghĩa trong chuỗi.
- <a id="enc-r-083"></a>**ENC-R-083** Tiến độ chuỗi thuộc **kiếp hiện tại**; kiếp kết thúc thì chuỗi bị hủy.
- <a id="enc-r-084"></a>**ENC-R-084** Sổ "Cơ Duyên Chi Lộ" (trong nhật ký nhiệm vụ) hiển thị gợi ý bước hiện tại (`hintKey` của bước).

### 5.9. Động phủ truyền thừa (Beta)
- <a id="enc-r-090"></a>**ENC-R-090** `LegacyCave` do GD-26 LC-R-140 tạo ra trở thành ứng viên ảo `ENC_LEGACY_CAVE` (phẩm `HUYEN`, trigger `POI_PROXIMITY` tại POI có tag `legacy_capable` thuộc vực tương ứng với cảnh giới cao nhất của người để lại).
- <a id="enc-r-091"></a>**ENC-R-091** Khi được chọn, API chọn ngẫu nhiên 1 động phủ **chưa được nhận**, của **tài khoản khác** (ngoại lệ: nhân vật có thiên phú `TAL_PAST_WISDOM` (Túc Tuệ) được ưu tiên nhận động phủ của chính tài khoản mình).
- <a id="enc-r-092"></a>**ENC-R-092** Nhận: vật phẩm chuyển `LEGACY → BAG` (item_log `LEGACY_CLAIMED`), hiển thị lời nhắn của người để lại. Tài khoản người để lại nhận thông báo và `params.legacyClaimRebirthPoints` (20) điểm Luân Hồi.

### 5.10. Thông báo toàn server
- <a id="enc-r-100"></a>**ENC-R-100** `broadcast: AUTO`: phiên phẩm `DIA` và `THIEN` khi `RESOLVED` phát tin *"Đạo hữu {name} gặp cơ duyên {title}"* lên kênh thế giới.
- <a id="enc-r-101"></a>**ENC-R-101** Người chơi có tùy chọn `hideNameInBroadcast` (hiện "Một vị đạo hữu").
- <a id="enc-r-102"></a>**ENC-R-102** Hàng đợi tin tối đa 1 tin / `params.broadcastMinIntervalSec` (60s) mỗi server; tin dồn quá 10 phút thì bỏ. *(Thay bằng hàng đợi chung của [GD-14 SO-R-060](20-social.md#so-r-060); cơ duyên Địa = ưu tiên 2, Thiên = ưu tiên 1.)*

### 5.11. Lịch sử và Vạn Giới Lục
- <a id="enc-r-110"></a>**ENC-R-110** Mỗi phiên `OFFERED` cập nhật `encounter_history` (theo nhân vật) và `encounter_account_history` (theo tài khoản, dùng cho `ONCE_PER_ACCOUNT`).
- <a id="enc-r-111"></a>**ENC-R-111** Phiên `RESOLVED` lần đầu của một `encounterId` trên tài khoản mở mục Vạn Giới Lục `CodexEntry(type = ENCOUNTER)` (GD-21 §3.6), lưu danh sách **nhánh kết thúc đã thấy** (khóa outcome cuối cùng).

### 5.12. Thử thách tâm ma (`HEART_TRIAL`)
- <a id="enc-r-120"></a>**ENC-R-120** Phiên `HEART_TRIAL` **chỉ** được mở bởi hệ thống đột phá (GD-04 CU-R-070), không qua bước cơ hội / pity / giới hạn tần suất / cooldown của §5.1–5.4.
- <a id="enc-r-121"></a>**ENC-R-121** Chọn định nghĩa: lọc mọi `EncounterDef` loại `HEART_TRIAL` thỏa `conditions` (thêm điều kiện `breakthroughTargets?: Realm[]`), chọn theo `weight` (không nhân phẩm cấp / khí vận). Không có ứng viên thì dùng `ENC_HT_DEFAULT`.
- <a id="enc-r-122"></a>**ENC-R-122** Phiên gắn `attemptId` của `BreakthroughAttempt`; khi `RESOLVED`/`ABORTED`, API Service cộng tổng `MODIFY_BREAKTHROUGH_CHANCE` đã commit vào attempt (chặn [−20, +15]).
- <a id="enc-r-123"></a>**ENC-R-123** Không có lựa chọn "rời đi" bắt buộc; mất kết nối quá `heartTrialTimeoutSec` (GD-04) thì kết thúc với outcome đã commit (thường là 0).

---

## 6. Công thức

### 6.1. Xác suất xảy ra
```
p = min(pMax, pTrigger × (1 + pityMissCount × pityStep))
pMax = 0,9   pityStep = 0,15
```
Ví dụ `GATHER` (0,02) sau 20 lần trượt: `0,02 × (1 + 20 × 0,15) = 0,08`.

### 6.2. Trọng số chọn
```
w_i = weight_i × tierWeight[tier_i] × luckFactor(tier_i) × rarityPity_i × (1 + Σ ENCOUNTER_WEIGHT_PCT)
luckFactor(tier) = clamp((luck / 50) ^ tierLuckExp[tier], 0,25, 4)
rarityPity_i = rarityPityMult nếu ENC-R-034 thỏa và tier_i ≥ HUYEN, ngược lại 1
P(chọn i) = w_i / Σ w
```
- `luck` là giá trị **cuối** sau thiên phú (GD-27 §6.3). `ENCOUNTER_WEIGHT_PCT` là hiệu ứng thiên phú (vd. `TAL_HEAVEN_SENSE` +0,25) áp lên ứng viên có `tier ≥ minTier` của hiệu ứng.
- Điều kiện `luckMin`, `comprehensionMin`, `spiritSenseMin` (§3) cũng so với giá trị cuối.

| Phẩm | `tierWeight` | `tierLuckExp` |
|------|:------------:|:-------------:|
| `PHAM` | 100 | 0 |
| `LINH` | 40 | 0,5 |
| `HUYEN` | 12 | 1 |
| `DIA` | 3 | 1,5 |
| `THIEN` | 0,5 | 2 |

Ví dụ: ba ứng viên `weight` đều 100, phẩm PHAM / HUYEN / DIA, khí vận 75:
- PHAM: 100 × 100 × 1 = 10.000
- HUYEN: 100 × 12 × 1,5 = 1.800
- DIA: 100 × 3 × 1,5^1,5 ≈ 551,1
- Tổng ≈ 12.351 → P(DIA) ≈ 4,46%. Với khí vận 50: 300 / 11.500 ≈ 2,61%.

### 6.3. Kiểm tra may rủi (`ROLL_CHECK`)
```
p = clamp(base + (statValue − pivot) × perPoint, min, max)
passed = roll < p,  roll ∈ [0, 1) lấy từ PRNG(seed phiên, thứ tự check)
```
Ví dụ check `patience` = `{ stat: comprehension, base: 0,30, perPoint: 0,01, min: 0,10, max: 0,90 }`, ngộ tính 45 → `p = 0,75`. Client hiện *"~75%"* (làm tròn tới 5%).

---

## 7. Định dạng nội dung

### 7.1. Thư mục
```
shared/encounters/
├── defs/v1/ENC_V1_NGU_ONG.yaml        # một file định nghĩa / cơ duyên
├── defs/v1/chains/CHAIN_V1_KIEM_TRUNG.yaml
├── ink/v1/ngu_ong.vi.ink              # kịch bản tiếng Việt
├── ink/v1/ngu_ong.en.ink              # kịch bản tiếng Anh (cùng cấu trúc)
└── ink/common/functions.ink           # khai báo EXTERNAL + hàm giả lập để test trong Inky
shared/gamedata/encounters/params.json
```

### 7.2. Ví dụ đầy đủ: `ENC_V1_NGU_ONG` (Lão ngư ông dưới mưa)

```yaml
# shared/encounters/defs/v1/ENC_V1_NGU_ONG.yaml
id: ENC_V1_NGU_ONG
version: 1
tier: LINH
type: DIALOGUE
titleKey: enc.v1.ngu_ong.title            # "Lão ngư ông dưới mưa"
codexHintKey: enc.v1.ngu_ong.hint         # "Tương truyền bên sông Bích Thủy, những ngày mưa..."
trigger:
  type: POI_PROXIMITY
  poiIds: [V1_F02_ENC_001, V1_F02_ENC_002]
conditions:
  realmMin: LK1
  realmMax: TC4
  weather: [RAIN]
  dayPhases: [DAWN, DAY]
weight: 100
cooldown: { perCharacterGameDays: 3 }
uniqueness: NONE
ink: { file: v1/ngu_ong, knot: enc_v1_ngu_ong }
checks:
  patience: { stat: comprehension, base: 0.30, perPoint: 0.01, min: 0.10, max: 0.90 }
outcomes:
  reward_pearl:  [ { type: GRANT_ITEM, itemId: ITM_THUY_LINH_CHAU, qty: 1 } ]
  reward_recipe: [ { type: GRANT_ITEM, itemId: ITM_RECIPE_THANH_TAM_DAN, qty: 1 } ]
  insight:       [ { type: CHANGE_INNATE_STAT, stat: comprehension, delta: 1 } ]
  chilled:       [ { type: ADD_STATUS, statusId: ST_CHILLED, durationSec: 300 } ]
presentation: { cue: NPC, npcModel: NPC_OLD_FISHERMAN, offerTtlSec: 120 }
```

```ink
// shared/encounters/ink/v1/ngu_ong.vi.ink
INCLUDE ../common/functions.ink

=== enc_v1_ngu_ong ===
Bên bờ sông, một lão ngư ông đội nón lá ngồi câu giữa màn mưa. Lưỡi câu của ông không có mồi.
* [Ngồi xuống câu cùng lão] #check:patience
    -> fish
* {comprehension >= 30} [Hỏi: "Câu không mồi, lão trượng câu gì?"]
    -> ask
* [Lặng lẽ rời đi]
    -> END

= fish
Hai người ngồi im lặng rất lâu. Chỉ có tiếng mưa rơi trên mặt sông.
{ ROLL_CHECK("patience"):
    Cần câu của bạn chợt trĩu xuống. Một viên châu xanh biếc mắc vào lưỡi câu.
    ~ OUTCOME("reward_pearl")
- else:
    Mưa lạnh thấm vào người. Lão cười khẽ: "Tâm chưa tĩnh, cá sao cắn câu."
    ~ OUTCOME("chilled")
}
-> END

= ask
Lão ngư ông liếc nhìn bạn: "Ta câu người có duyên." Ông rút từ trong áo ra một cuộn giấy cũ.
~ OUTCOME("reward_recipe")
~ OUTCOME("insight")
-> END
```

> Cú pháp tag trên lựa chọn (`#check:...`) cần ink ≥ 1.1; xác nhận trong đánh giá Ink ở Phase 0 (PLAN Q23).

### 7.3. Ví dụ rút gọn (định nghĩa)

**`ENC_V1_THUONG_HO` (Linh hồ bị thương): phẩm HUYEN, chiến đấu + lựa chọn đạo đức**
```yaml
id: ENC_V1_THUONG_HO
tier: HUYEN
type: COMBAT
trigger: { type: AREA_ENTER, areaIds: [V1_F05_A03, V1_F05_A05] }
conditions: { realmMin: LK4, dayPhases: [NIGHT], soloOnly: true }
weight: 80
uniqueness: ONCE_PER_LIFE
ink: { file: v1/thuong_ho, knot: enc_v1_thuong_ho }
spawnGroups: { wolves: { groupId: SG_V1_HUNGRY_WOLVES } }
outcomes:
  spare_fox:  [ { type: CHANGE_KARMA, delta: 10 },
                { type: SET_FLAG, scope: LIFE, flag: FLAG_SAVED_FOX },
                { type: START_CHAIN, chainId: CHAIN_V1_HO_TIEN } ]
  take_core:  [ { type: GRANT_ITEM, itemId: ITM_HO_YEU_DAN, qty: 1 },
                { type: CHANGE_KARMA, delta: -15 },
                { type: SET_FLAG, scope: LIFE, flag: FLAG_KILLED_FOX } ]
```
Luồng Ink: gặp linh hồ bị lang yêu truy đuổi → chọn cứu (`START_COMBAT("wolves")`) hoặc bỏ đi → nếu `combat_result == "WIN"`: chọn thả hồ (`spare_fox`) hoặc lấy nội đan (`take_core`).

**`ENC_V1_NGU_HANH_TRAN` (Trận pháp ngũ hành): phẩm HUYEN, câu đố bằng lựa chọn Ink**
```yaml
id: ENC_V1_NGU_HANH_TRAN
tier: HUYEN
type: PUZZLE
trigger: { type: SCAN, }                 # phát hiện bằng thần thức quét
conditions: { maps: [V1_F06], spiritSenseMin: 20 }
uniqueness: ONCE_PER_LIFE
outcomes:
  solved: [ { type: GRANT_FROM_TABLE, lootTableId: LT_V1_TECH_FRAGMENT_HUYEN, rolls: 1 } ]
  failed: [ { type: ADD_STATUS, statusId: ST_WEAKENED, durationSec: 300 } ]
```
Luồng Ink: 5 cột đá, chọn thứ tự kích hoạt theo vòng tương sinh, bắt đầu từ cột đang sáng (Mộc → Hỏa → Thổ → Kim → Thủy). Sai 2 lần thì `failed`.

**`ENC_V1_FIRST_OMEN` (Điềm Báo): phẩm PHAM, cơ duyên dàn dựng của onboarding (GD-18 OB-R-030, LR-03 PP-0.03)**
```yaml
id: ENC_V1_FIRST_OMEN
tier: PHAM
type: DIALOGUE
scripted: true                                   # GD-18 OB-R-030: bỏ qua roll ENC-R-030, không tính pity / giới hạn tần suất
trigger: { type: AREA_ENTER, areaIds: [V1_F01_A04] }        # vào Gò Linh Mạch — GD-16 V1_F01 §9.1 (mọi lộ trình ra Cửa rừng đều qua khu này)
conditions: { maps: [V1_F01], realmMin: LK1, questStates: [ { questId: QC_TUTORIAL_QI, state: DONE } ] }
weight: 1
uniqueness: ONCE_PER_ACCOUNT
presentation: { cue: OBJECT, vfx: VFX_WLD_ENCOUNTER_GLOW }  # mảnh đá hoa văn giống ngọc giản phát sáng (LR-03 PP-0.03)
# ink, checks, outcomes: CHỜ VIẾT — yêu cầu: 2–3 lựa chọn, đúng 1 lựa chọn ROLL_CHECK (OB-R-030); mở mục Vạn Giới Lục đầu tiên của mạch ngọc giản (LR-03 PP-0.03)
```

**Điểm cơ duyên `V1_F01_ENC_001..004`** (POI `ENCOUNTER_POINT`, vị trí ở GD-16 `V1_F01` §9.1): cơ duyên `POI_PROXIMITY` cho các điểm này **chờ viết**; V-13 cảnh báo tới khi có.

**`CHAIN_V1_KIEM_TRUNG` (Tàn kiếm cổ mộ): chuỗi phẩm DIA, 3 bước**
```yaml
id: CHAIN_V1_KIEM_TRUNG
steps:
  - { stepId: S1, encounterId: ENC_V1_KT_S1, expiresGameDays: 5, hintKey: chain.kt.s1 }  # mảnh bia ở F06
  - { stepId: S2, encounterId: ENC_V1_KT_S2, expiresGameDays: 5, hintKey: chain.kt.s2 }  # đêm giông ở F03
  - { stepId: S3, encounterId: ENC_V1_KT_S3, expiresGameDays: 3, hintKey: chain.kt.s3 }  # mộ kiếm, chiến đấu
onFail: [ { type: GRANT_CURRENCY, currency: SPIRIT_STONE, amount: 200 } ]
```

### 7.4. Validator (chạy trong CI, build fail khi lỗi)

| Mã | Kiểm tra |
|----|----------|
| V-01 | `id` đúng mẫu `ENC_{vực}_{TÊN}`, duy nhất; tên file = `id` |
| V-02 | Mọi ID tham chiếu tồn tại: `poiIds`, `areaIds`, `maps`, `itemId`, `lootTableId`, `talentId`, `statusId`, `groupId`, `chainId` |
| V-03 | Mọi `OUTCOME("k")` trong Ink có `k` trong `outcomes`; mọi khóa trong `outcomes` được dùng ít nhất một lần |
| V-04 | Mọi `ROLL_CHECK("c")` có `c` trong `checks`; mọi `#check:c` cũng vậy |
| V-05 | Mọi `START_COMBAT("g")` có `g` trong `spawnGroups` |
| V-06 | Outcome tuân thủ danh sách trắng và **trần theo phẩm cấp** §8.2 |
| V-07 | Knot gốc có lựa chọn rời đi không outcome (ENC-R-053), trừ `HEART_TRIAL` |
| V-14 | `HEART_TRIAL` chỉ dùng outcome `MODIFY_BREAKTHROUGH_CHANCE`, `CHANGE_HEART_STATE`; tổng `MODIFY_BREAKTHROUGH_CHANCE` trên mọi nhánh trong [−20, +15]; outcome `MODIFY_BREAKTHROUGH_CHANCE` không xuất hiện ở loại khác |
| V-08 | Không dùng `RANDOM`, `SEED_RANDOM` (ENC-R-063) |
| V-09 | Các bản ngôn ngữ cùng cấu trúc (ENC-R-060) |
| V-10 | `uniqueness: SERVER_UNIQUE` bị cấm (ENC-R-041, ENC-Q-1) |
| V-11 | `weight > 0`; `realmMin ≤ realmMax`; enum hợp lệ |
| V-12 | Mọi `titleKey`, `codexHintKey`, `hintKey` có bản dịch `vi` |
| V-13 | Mỗi bản đồ `FIELD` có ít nhất 1 cơ duyên `POI_PROXIMITY` cho mỗi `ENCOUNTER_POINT` (cảnh báo, không fail) |
| V-15 | `presentation.vfx` (nếu có) là `id` của một `VfxEffectDef` trong `shared/gamedata/vfx/effects.json` (AR-04 VFX-R-105; domain `vfx` chờ TD-07 đăng ký); không nhận mẫu kỹ năng `VFX_{MẪU}`, lớp phủ `VFXO_*` hay bảng màu |

---

## 8. Danh sách trắng outcome

### 8.1. Các loại outcome

| `type` | Tham số | Xử lý (API Service) | Khi không áp dụng được |
|--------|---------|---------------------|------------------------|
| `GRANT_ITEM` | `itemId`, `qty` | Vào `BAG`; item_log `ENCOUNTER` | Túi đầy → gửi thư (`MAIL`) |
| `GRANT_FROM_TABLE` | `lootTableId`, `rolls` | Roll bảng rơi đồ (RNG server, `rng_audit`) → như `GRANT_ITEM` | Như trên |
| `GRANT_CURRENCY` | `currency` (**chỉ** `SPIRIT_STONE`), `amount` | currency_ledger `ENCOUNTER` | — |
| `GRANT_CULTIVATION` | `pctOfStage` (0–1) | Cộng tu vi = pct × yêu cầu tầng hiện tại, chặn ở bình cảnh (GD-04) | Phần vượt bị bỏ, thông báo |
| `CHANGE_LIFESPAN` | `kind` (`USED`/`EXTENSION`), `years` | Gọi GD-26 `lifespan-change`, `reasonCode = ENCOUNTER`; `EXTENSION` dùng khóa giảm dần `ENC:<encounterId>` (GD-26 §6.3) | — |
| `CHANGE_KARMA` | `delta` (−20..20) | Chặn trong −100..100 | — |
| `CHANGE_REPUTATION` | `faction`, `delta` (trần theo phẩm: PHAM 50 · LINH 100 · HUYEN 200 · DIA 300) | GD-14 §5.4c (đối nghịch, hệ số thành viên) | — |
| `CHANGE_INNATE_STAT` | `stat` (`comprehension`/`constitution`/`spiritSense`/`luck`), `delta` (−5..5) | Qua GD-27; có trần tổng mỗi kiếp | Vượt trần → bỏ, thông báo |
| `ADD_TALENT` | `talentId` | Qua sự kiện thay đổi thiên phú GD-27 | Đã đủ số thiên phú tối đa → người chơi chọn thay thế hoặc từ chối |
| `REMOVE_TALENT` | `talentId` | Qua GD-27 | Không có → bỏ qua |
| `MODIFY_ROOT` | `operation` (`PURIFY`/`REMOVE_ELEMENT`/`MUTATE`), `element?`, `amount?` | Qua GD-27 (tẩy tủy) | Không hợp lệ → bỏ qua |
| `ADD_STATUS` | `statusId`, `durationSec` | Status trên nhân vật | — |
| `CHANGE_HEART_STATE` | `delta` (−20..20) | GD-04 | — |
| `SET_FLAG` | `scope` (`LIFE`/`ACCOUNT`), `flag` | Cờ dùng cho điều kiện | — |
| `START_CHAIN` | `chainId` | Tạo tiến độ chuỗi bước đầu | Đang có chuỗi này → bỏ qua |
| `ADVANCE_CHAIN` | `chainId`, `stepId` | Sang bước kế | Sai bước → `contentError` |
| `UNLOCK_CODEX` | `entryType`, `entryId` | GD-21 §3.6 | Đã mở → bỏ qua |
| `MODIFY_BREAKTHROUGH_CHANCE` | `delta` (−20..15) | **Chỉ trong `HEART_TRIAL`**: cộng vào dòng `heartTrial` của `BreakthroughAttempt` (GD-04 §6.6) | Phiên không gắn attempt → `contentError` |

Không có loại outcome nào khác. Muốn thêm loại mới phải sửa tài liệu này trước (docs/README §3).

### 8.2. Trần sức mạnh theo phẩm cấp (validator V-06)

| Phẩm | Thiên phú tối đa (`ADD_TALENT`) | Thọ `EXTENSION` tối đa | `GRANT_CULTIVATION` tối đa | `CHANGE_INNATE_STAT` tối đa | `MODIFY_ROOT` |
|------|:---:|:---:|:---:|:---:|:---:|
| `PHAM` | — | 0 | 0,05 | 0 | ❌ |
| `LINH` | `COMMON` | 5 năm | 0,10 | ±1 | ❌ |
| `HUYEN` | `RARE` | 15 năm | 0,25 | ±2 | `PURIFY` |
| `DIA` | `EPIC` | 40 năm | 0,50 | ±3 | ✅ |
| `THIEN` | `LEGENDARY` | 100 năm | 1,00 | ±5 | ✅ |

Trần linh thạch của `GRANT_CURRENCY` theo phẩm: [GD-13 §6.5](19-economy.md) (`IPH × 0,25 / 0,5 / 1 / 2 / 4 giờ`). Phẩm của vật phẩm trong `GRANT_ITEM` / bảng rơi đồ cũng bị giới hạn theo [GD-09 IT-R-061](15-items-equipment.md#it-r-061): `PHAM` → Phàm · `LINH` → Linh · `HUYEN` → Bảo · `DIA` → Tiên · `THIEN` → Thần khí.

---

## 9. Hợp đồng giao tiếp

### 9.1. API nội bộ (Game Server → API Service)

| Endpoint | Request | Response | Quy tắc |
|----------|---------|----------|---------|
| `GET /internal/v1/encounters/context/{characterId}` | — | `{ cooldowns: {encounterId: until}[], lifeOnce: string[], accountOnce: string[], lifeFlags, accountFlags, pityMissCount, sessionsSinceHuyenPlus, recentSessions: { at }[], chains: ChainProgress[] }` | Tải khi vào bản đồ; Game Server đệm và cập nhật cục bộ |
| `POST /internal/v1/encounters/sessions` | `{ characterId, encounterId, encounterVersion, triggerType, triggerRef, mapId, channel }` | `201 { sessionId, rngSeed, offeredUntil }` · `409 { reason: 'COOLDOWN' \| 'UNIQUE_TAKEN' \| 'RATE_LIMIT' \| 'NOT_ALIVE' \| 'HAS_OPEN_SESSION' }` | ENC-R-040..043 |
| `POST /internal/v1/encounters/sessions/{id}/state` | `{ state: 'ACTIVE' \| 'EXPIRED' \| 'ABORTED', inkState? }` | `200` | §4 |
| `POST /internal/v1/encounters/sessions/{id}/commit` | `{ commitId, outcomeKeys: string[], checkResults: [...] }` | `{ results: [{ key, type, applied: bool, detail }] }` | ENC-R-070..073; idempotent theo `commitId` |
| `POST /internal/v1/encounters/sessions/{id}/resolve` | `{ finalOutcomeKey?: string, inkState }` | `{ broadcastQueued: bool }` | ENC-R-100..111 |
| `POST /internal/v1/encounters/pity` | `{ characterId, pityMissCount, sessionsSinceHuyenPlus }` | `200` | Ghi khi rời bản đồ / mỗi 60s |

### 9.2. Message thời gian thực (Game Server ↔ Client)

| Message | Hướng | Payload |
|---------|-------|---------|
| `S2C_EncounterCue` | S→C | `{ sessionId, cue: CueKind, position: [x, z], npcModel?, vfx?, ttlSec }` |
| `C2S_EncounterEngage` | C→S | `{ sessionId }` |
| `S2C_EncounterStep` | S→C | `{ sessionId, lines: string[], choices: [{ index, text, checkPct?, costLifespanYears?, lockedReason? }] }` |
| `C2S_EncounterChoose` | C→S | `{ sessionId, index }` |
| `S2C_EncounterCombat` | S→C | `{ sessionId, spawnGroupKey }` (client hiện viền chiến đấu riêng) |
| `S2C_EncounterOutcome` | S→C | `{ sessionId, results: [{ type, displayKey, params }] }` |
| `S2C_EncounterEnd` | S→C | `{ sessionId, state }` |

Server kiểm tra `index` thuộc danh sách lựa chọn hợp lệ hiện tại; sai thì bỏ qua và log.

### 9.3. Sự kiện domain (topic `encounter.*`)

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `encounter.offered` | `{ characterId, encounterId, tier, triggerType }` | Telemetry |
| `encounter.resolved` | `{ characterId, accountId, encounterId, tier, finalOutcomeKey }` | Bia Vạn Cổ (`notableEvents` nếu DIA+), Vạn Giới Lục, telemetry |
| `encounter.broadcast` | `{ displayName, titleKey, tier }` | Kênh chat thế giới |
| Nhận `lifecycle.life_ending` (GD-26) | | Hủy phiên đang mở → `ABORTED`; hủy chuỗi |

---

## 10. Lưu trữ

```sql
CREATE TABLE encounter_session (
  session_id        UUID PRIMARY KEY,
  character_id      UUID NOT NULL REFERENCES character(id),
  encounter_id      VARCHAR(64) NOT NULL,
  encounter_version INT NOT NULL,
  state             VARCHAR(16) NOT NULL,
  trigger_type      VARCHAR(24) NOT NULL,
  trigger_ref       VARCHAR(64),
  map_id            VARCHAR(32) NOT NULL,
  rng_seed          VARCHAR(64) NOT NULL,
  ink_state         JSONB,
  committed_keys    JSONB NOT NULL DEFAULT '[]',
  check_results     JSONB NOT NULL DEFAULT '[]',
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  offered_until     TIMESTAMPTZ NOT NULL,
  resolved_at       TIMESTAMPTZ
);
-- ENC-R-012: tối đa 1 phiên mở mỗi nhân vật
CREATE UNIQUE INDEX ux_enc_one_open ON encounter_session(character_id) WHERE state IN ('OFFERED', 'ACTIVE');

CREATE TABLE encounter_commit (
  commit_id   VARCHAR(80) PRIMARY KEY,           -- idempotency
  session_id  UUID NOT NULL REFERENCES encounter_session(session_id),
  outcome_keys JSONB NOT NULL,
  results     JSONB NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE encounter_history (                   -- theo nhân vật (cooldown, ONCE_PER_LIFE)
  character_id UUID NOT NULL, encounter_id VARCHAR(64) NOT NULL,
  times INT NOT NULL DEFAULT 0, last_offered_at TIMESTAMPTZ NOT NULL, cooldown_until TIMESTAMPTZ,
  PRIMARY KEY (character_id, encounter_id)
);

CREATE TABLE encounter_account_history (           -- theo tài khoản (ONCE_PER_ACCOUNT, nhánh đã thấy)
  account_id UUID NOT NULL, encounter_id VARCHAR(64) NOT NULL,
  times INT NOT NULL DEFAULT 0, endings_seen JSONB NOT NULL DEFAULT '[]',
  PRIMARY KEY (account_id, encounter_id)
);

CREATE TABLE encounter_pity (
  character_id UUID PRIMARY KEY, pity_miss_count INT NOT NULL DEFAULT 0,
  sessions_since_huyen_plus INT NOT NULL DEFAULT 0, updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE encounter_chain_progress (
  character_id UUID NOT NULL, chain_id VARCHAR(64) NOT NULL,
  step_id VARCHAR(16) NOT NULL, state VARCHAR(16) NOT NULL,   -- ACTIVE | DONE | FAILED
  step_expires_at TIMESTAMPTZ, updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, chain_id)
);

CREATE TABLE character_flag (
  character_id UUID NOT NULL, flag VARCHAR(64) NOT NULL, set_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, flag)
);
CREATE TABLE account_flag (
  account_id UUID NOT NULL, flag VARCHAR(64) NOT NULL, set_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, flag)
);

CREATE TABLE legacy_cave (                          -- Beta, tạo bởi GD-26 LC-R-140
  id UUID PRIMARY KEY, from_account_id UUID NOT NULL, from_character_id UUID NOT NULL,
  region_id VARCHAR(8) NOT NULL, message TEXT, item_ids JSONB NOT NULL,
  claimed_by UUID, claimed_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_legacy_unclaimed ON legacy_cave(region_id) WHERE claimed_by IS NULL;
```

**Mã lý do ledger:** `item_log`, `currency_ledger`, `lifespan_ledger` dùng `reason_code = 'ENCOUNTER'`, `ref_id = '<encounterId>:<sessionId>'`. Thêm `item_log` `LEGACY_CLAIMED`.

---

## 11. UI/UX

- **Tín hiệu:** NPC có biểu tượng mây lành nhỏ trên đầu; `GLOW` là cột sáng mờ; `SOUND` có âm thanh định hướng + gợn sóng trên minimap. Âm thanh "cơ duyên" riêng, dễ nhận ra.
- **Hộp thoại cơ duyên:** khung giấy cổ phong; tên cơ duyên + phẩm (màu theo phẩm); lựa chọn hiện `~75%` (tag `#check`), biểu tượng đồng hồ cát kèm số năm (tag `#cost_lifespan`), lựa chọn khóa mờ kèm lý do.
- **Kết quả:** thẻ phần thưởng / hình phạt xuất hiện lần lượt; phẩm `DIA`+ có hiệu ứng màn hình riêng.
- **Vạn Giới Lục → Cơ duyên:** danh sách đã gặp (tên, phẩm, gợi ý điều kiện mờ, nhánh kết thúc đã thấy x/y); cơ duyên chưa gặp hiện "???" kèm số lượng còn lại của vực.
- **Cơ Duyên Chi Lộ:** mục riêng trong nhật ký, hiện chuỗi đang theo và gợi ý bước kế.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-21 Open World | `ENCOUNTER_POINT`, khu, canh giờ, thời tiết; `SCAN` là trigger; Vạn Giới Lục |
| GD-26 Vòng đời | Outcome `CHANGE_LIFESPAN`; điều kiện `warningLevels`, `generation`; hủy phiên khi `life_ending`; động phủ truyền thừa |
| GD-27 Thiên phú | Điều kiện thiên phú / linh căn / xuất thân / khí vận; outcome `ADD_TALENT`, `MODIFY_ROOT`, `CHANGE_INNATE_STAT` |
| GD-04 Tu luyện | `GRANT_CULTIVATION`, `CHANGE_HEART_STATE`; tâm ma khi đột phá có thể dùng cùng engine (trigger riêng, ngoài phạm vi v0.1) |
| GD-12 Quái | `spawnGroups` cho `START_COMBAT`; trigger `KILL` |
| GD-09 Vật phẩm | Item, bảng rơi đồ, ánh xạ phẩm vật phẩm ↔ phẩm cơ duyên |
| GD-29 RNG | Hiển thị tỷ lệ, pity, audit |
| GD-14 Xã hội | Hàng đợi thông báo toàn server chung (SO-R-060), ẩn tên |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Hai cơ hội cùng lúc (hái thuốc gần điểm cơ duyên) | Xử lý tuần tự; cơ hội đầu đặt chỗ thành công thì cơ hội sau bị ENC-R-012 chặn |
| Mất kết nối giữa hộp thoại | Giữ `ACTIVE` trong `reconnectGraceSec` (120s), lưu `inkState`; quá hạn thì `ABORTED` (outcome đã commit giữ nguyên) |
| Game Server sập giữa phiên | Phiên `ACTIVE` quá hạn được job dọn về `ABORTED`; commit đã ghi vẫn còn |
| Nhân vật chết / trọng thương do nguồn khác trong khi `OFFERED` | `EXPIRED` |
| Đổi nội dung (version mới) khi phiên đang chạy | Phiên giữ version cũ tới khi kết thúc; API giữ được định nghĩa các version gần nhất |
| Người chơi cố gửi `C2S_EncounterChoose` với index ngoài phạm vi / phiên của người khác | Bỏ qua, log cảnh báo bảo mật |
| Ink vòng lặp gọi cùng `OUTCOME` nhiều lần | ENC-R-073: chỉ commit một lần |
| Túi đầy khi nhận vật phẩm | Gửi thư (§8.1) |
| Cơ duyên chiến đấu: người chơi chạy khỏi vùng | Quá `params.combatLeashRadius` (40m) → `combat_result = "FLED"` |
| Tổ đội đứng cạnh khi một người gặp cơ duyên | Người khác không thấy gì (ENC-R-001); quái chiến đấu hiện mờ với họ, không tương tác được |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `enc_opportunity` | triggerType, candidates, p, hit | Tỷ lệ trúng thực tế theo loại trigger |
| `enc_offered` | encounterId, tier, luck, pity | Phân bố phẩm, tác động khí vận |
| `enc_engaged` / `enc_expired` | encounterId, giây tới khi tương tác | Tín hiệu có dễ thấy không |
| `enc_choice` | encounterId, knot, choiceIndex, checkPct | Lựa chọn phổ biến, độ hấp dẫn của rủi ro |
| `enc_check` | checkId, p, passed | Kiểm chứng tỷ lệ thực tế khớp lý thuyết |
| `enc_resolved` | encounterId, finalOutcomeKey, durationSec | Nhánh kết thúc, độ dài |
| `enc_per_hour` | số phiên / giờ chơi / nhân vật | Mục tiêu MVP: 2–4 phiên / giờ chơi ở `FIELD` |
| `enc_chain` | chainId, stepId, state | Tỷ lệ hoàn thành / thất bại chuỗi |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="enc-ac-001"></a>ENC-AC-001 | Nhân vật LK3, trời mưa, ban ngày, đến gần `V1_F02_ENC_001` | Cơ hội `POI_PROXIMITY` | `ENC_V1_NGU_ONG` nằm trong danh sách ứng viên |
| ENC-AC-002 | Như trên nhưng trời quang | Cơ hội | `ENC_V1_NGU_ONG` **không** là ứng viên |
| ENC-AC-003 | Danh sách ứng viên rỗng | Cơ hội | Không roll; `pityMissCount` không đổi (ENC-R-023) |
| ENC-AC-004 | `pTrigger = 0,02`, `pityMissCount = 20` | Tính p | p = 0,08 (§6.1) |
| ENC-AC-005 | Ví dụ §6.2 với khí vận 75 | Tính xác suất chọn | P(DIA) ≈ 4,46% (±0,01%) |
| ENC-AC-006 | 10.000 lần roll mô phỏng với seed cố định | Chạy trọng số §6.2 | Tần suất từng ứng viên lệch ≤ 1% so với lý thuyết |
| ENC-AC-007 | Phiên vừa `OFFERED`, người chơi phớt lờ tới hết TTL | Hết hạn | `EXPIRED`; cooldown vẫn áp dụng; `pityMissCount = 0` (ENC-R-043) |
| ENC-AC-008 | Phiên kết thúc cách đây 100s | Cơ hội mới | Bị bỏ qua do `minIntervalRealSec` (ENC-R-011), không tăng pity |
| ENC-AC-009 | `uniqueness: ONCE_PER_ACCOUNT`, đã gặp ở kiếp 1 | Kiếp 2 thỏa điều kiện | Không là ứng viên; nếu Game Server gửi đặt chỗ thì API trả 409 `UNIQUE_TAKEN` |
| ENC-AC-010 | Ink gọi `OUTCOME("reward_pearl")` | Resolve | Nhân vật nhận đúng `ITM_THUY_LINH_CHAU` ×1; `item_log` có `reason_code = ENCOUNTER`, `ref_id` đúng |
| ENC-AC-011 | Commit chứa khóa không tồn tại trong định nghĩa | API xử lý | Từ chối toàn bộ commit, cờ `contentError`, không áp dụng gì (ENC-R-071) |
| ENC-AC-012 | Gửi cùng `commitId` hai lần | API xử lý | Áp dụng một lần, response giống nhau |
| ENC-AC-013 | Ink lặp gọi `OUTCOME("insight")` 3 lần | Resolve | Ngộ tính +1 (một lần) (ENC-R-073) |
| ENC-AC-014 | Check `patience`, ngộ tính 45 | Hiển thị lựa chọn | Client nhận `checkPct = 75` |
| ENC-AC-015 | Cùng seed phiên, cùng chuỗi lựa chọn | Chạy lại phiên (replay audit) | Kết quả mọi `ROLL_CHECK` giống hệt |
| ENC-AC-016 | Phiên `ACTIVE`, nhân vật vào `ENDING` | Nhận `lifecycle.life_ending` | Phiên `ABORTED`; outcome đã commit giữ nguyên |
| ENC-AC-017 | Cơ duyên chiến đấu, người chơi thua | Trọng thương | Phiên `RESOLVED`, chỉ outcome đã commit được áp dụng (ENC-R-065) |
| ENC-AC-018 | Phiên `ACTIVE` | Quái xung quanh | Quái không tấn công / không chọn được nhân vật làm mục tiêu (ENC-R-052) |
| ENC-AC-019 | Người chơi thứ hai đứng cạnh | Người thứ nhất nhận phiên | Người thứ hai không nhận `S2C_EncounterCue` (ENC-R-001) |
| ENC-AC-020 | Phiên `DIA` resolve, `hideNameInBroadcast = true` | Thông báo | Tin thế giới dùng "Một vị đạo hữu" |
| ENC-AC-021 | File nội dung có `OUTCOME` với `ADD_TALENT` EPIC ở cơ duyên phẩm `LINH` | CI | Validator V-06 fail |
| ENC-AC-022 | File Ink dùng `RANDOM(1, 10)` | CI | Validator V-08 fail |
| ENC-AC-023 | 15 phiên liên tiếp không có phẩm HUYEN+ | Cơ hội thứ 16 có ứng viên HUYEN | Trọng số HUYEN+ ×3 (ENC-R-034) |
| ENC-AC-024 | Bước chuỗi quá `expiresGameDays` | Job kiểm tra | Chuỗi `FAILED`, outcome `onFail` được áp dụng một lần |
| <a id="enc-ac-025"></a>ENC-AC-025 | `presentation.vfx = "VFX_WLD_ENCOUNTER_GLOW"` / `"VFX_XYZ"` (không có trong `vfx/effects.json`) | CI | Pass / Validator V-15 fail (AR-04 VFX-AC-023) |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | MVP | Beta | Sau launch |
|----------|:---:|:----:|:----------:|
| Engine: cơ hội, lọc, pity, trọng số, đặt chỗ, phiên | ✅ | | |
| Ink chạy trên server, biến, hàm ngoài, tag lựa chọn | ✅ | | |
| Loại `DIALOGUE`, `COMBAT`, `PUZZLE` (bằng lựa chọn Ink) | ✅ | | |
| 20–30 cơ duyên Vực 1, 2 chuỗi | ✅ | | |
| Validator V-01..V-15 trong CI | ✅ | | |
| Thông báo toàn server, Vạn Giới Lục mục Cơ duyên | ✅ (cơ bản) | ✅ (nhánh đã thấy) | |
| Encounter Previewer (chạy thử trong game, PLAN §6.5) | ✅ | | |
| Loại `INSTANCE` (động phủ cổ, map `KN`) | | ✅ | |
| Động phủ truyền thừa (`LEGACY_CAVE`) | | ✅ | |
| Cơ duyên chia sẻ cho tổ đội | | ? (ENC-Q-2) | |
| Cơ duyên trong hiểm địa | | ✅ (GD-28) | |
| `SERVER_UNIQUE` | ❌ | ❌ | ❌ (ENC-Q-1: không làm) |
| 100+ cơ duyên | | | ✅ |

---

## 17. Câu hỏi mở

- [x] **ENC-Q-1** (= PLAN Q14): *(Đã chốt 2026-10-23: **không** — ai cũng có cơ hội.)* Có dùng cơ duyên độc nhất toàn server không?
- [ ] **ENC-Q-2**: Cơ duyên chia sẻ cho tổ đội: một phiên chung (đội trưởng chọn) hay mỗi người một phiên? *Mặc định: không chia sẻ.*
- [ ] **ENC-Q-3**: Mục tiêu 2–4 phiên / giờ chơi có quá dày (mất cảm giác hiếm) hay quá thưa? Chốt bằng playtest Phase 1 với 3–5 cơ duyên mẫu.
- [ ] **ENC-Q-4** (= PLAN Q23): Ink + inkjs chạy trên Game Server (D-19 đã duyệt) có hỗ trợ đầy đủ tag trên lựa chọn và lưu / khôi phục trạng thái không?
- [ ] **ENC-Q-5**: Có nên cho người chơi "ghi nhớ" một cơ duyên đã gặp để chủ động tìm lại ở kiếp sau (đổi điểm Luân Hồi) không?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-20 | — | Bản nháp đầu: bất biến, pipeline, kiểu dữ liệu, máy trạng thái phiên, cơ hội / lọc / pity / trọng số, đặt chỗ & cooldown, Ink (biến, hàm ngoài, tag), outcome & danh sách trắng & trần phẩm cấp, chuỗi, động phủ truyền thừa, định dạng nội dung + ví dụ, validator, API, DB, nghiệm thu |
| v0.2 | 2026-10-23 | — | Điều kiện `alignment`, `reputationMin`; outcome `CHANGE_REPUTATION` (GD-14 §5.4c) |
| v0.3 | 2026-10-23 | — | Chốt ENC-Q-1: không có cơ duyên độc nhất toàn server |
| v0.4 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
| v0.5 | 2026-10-25 | — | Cờ `scripted` cho cơ duyên dàn dựng; `ENC_V1_FIRST_OMEN` thuộc GD-18 OB-R-030 |
| v0.6 | 2026-10-25 | — | Đợt rà nhất quán (AR-04): `presentation.vfx` chỉ nhận ID trong `vfx/effects.json` (validator V-15), ENC-AC-025 |
| v0.7 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: §7.3 định nghĩa khung `ENC_V1_FIRST_OMEN` (`scripted`, trigger `AREA_ENTER V1_F01_A04`, điều kiện `QC_TUTORIAL_QI` `DONE`; Ink / outcome chờ viết); điểm `V1_F01_ENC_001..004` (`ENCOUNTER_POINT` đã có ở TD-13 §4) chờ nội dung |

