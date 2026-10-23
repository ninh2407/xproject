# GD-15 — Nhiệm vụ

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-15 · Prefix ID: `QS` |
| Owner | Content Designer (nội dung) + Game Designer (hệ thống) + Server Dev (engine) |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 2 (engine, hướng dẫn, 15–20 nhiệm vụ chương 1), Phase 3 (hoàn thiện chương 1, nhiệm vụ hệ thống) |
| Quyết định nền | PLAN §4.1 (cốt truyện theo cảnh giới), §4.16 · D-03 (một mạng) |
| Liên quan | [PLAN §4.16](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-22 Cơ duyên](23-encounters.md) (dùng chung runtime Ink) · [GD-04 Tu luyện](10-cultivation.md) · [GD-07 Kỹ năng](13-skills.md) · [GD-09 Vật phẩm](15-items-equipment.md) · [GD-13 Kinh tế](19-economy.md) · [GD-14 Xã hội](20-social.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-27 Thiên phú](04-talents.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-21 Open World](22-open-world.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [GD-30 Động phủ & Sơn môn](28-caves-sect-sites.md) · LR-03 Story Outline |

> Đọc [docs/README.md §3](../README.md) và GD-22 §5.6 (Ink trên server, hàm ngoài, danh sách trắng) trước khi triển khai. Nội dung nhiệm vụ ở `shared/quests/` (§7).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-15 | Mục |
|--------------|-----------------|-----|
| GD-04 | `QC_TUTORIAL_QI` (PN1 → LK1), `QC_QUIET_HEART` (gỡ tâm ma); tu vi từ nhiệm vụ (`ExpSource.QUEST`) | §5.8, §6.1 |
| GD-07 | Chọn vũ khí trong hướng dẫn, học tâm pháp / kỹ năng khởi đầu | §5.8 |
| GD-14 | `QC_MAIN_V1_JOIN_SECT`, `QC_SECT_SERVANT`, nhiệm vụ Thanh Vân Tông hằng ngày | §5.6, §5.8 |
| GD-30 | Nhiệm vụ tông môn tự lập hằng tuần (Uy vọng, kinh nghiệm tông môn) | §5.7 |
| GD-26 | `QC_GREAT_LIMIT_*` khi đại hạn | §5.8 |
| GD-13 | Linh thạch thưởng nhiệm vụ | §6.1 |
| GD-22 | Điều kiện `questStates` | §3 |
| GD-27 | `Background.start.questChain` | §5.8 |

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện*, P5 *Thế giới đáng khám phá*.

- Nhiệm vụ chính **dẫn đường qua thế giới** (bản đồ, hệ thống, cốt truyện) nhưng không khóa cứng; người chơi được tự đi lang thang (GD-21).
- Nhiệm vụ **dạy hệ thống** đúng lúc: luyện đan trước khi cần Trúc Cơ Đan, hiểm địa có hướng dẫn trong lúc còn bảo hộ tân thủ.
- Kiếp sau **không bắt chơi lại** toàn bộ: lược truyện nhanh, vẫn giữ các mở khóa cần thiết.
- Nhiệm vụ hằng ngày **ngắn và không tích lũy**: bỏ một ngày không mất gì đáng kể (tránh nghĩa vụ hóa, PLAN §4.16).
- **Dữ liệu hoàn toàn:** content designer viết nhiệm vụ bằng YAML + Ink, không cần dev.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="qs-r-001"></a>**QS-R-001** | Tiến độ nhiệm vụ do **API Service** tính từ sự kiện domain do server phát (§5.3); client không gửi "đã hoàn thành mục tiêu". |
| <a id="qs-r-002"></a>**QS-R-002** | Phần thưởng và tác động của nhiệm vụ chỉ qua **danh sách trắng `QuestAction`** (§8), áp dụng trong transaction, idempotent, ghi ledger với `reasonCode = QUEST_REWARD` / `QUEST_TAKE`, `refId = questId`. Kịch bản Ink không chứa ID vật phẩm / số lượng (như GD-22 ENC-R-003). |
| <a id="qs-r-003"></a>**QS-R-003** | Nhiệm vụ **thuộc kiếp**: kiếp kết thúc thì toàn bộ tiến độ bị xóa (GD-26 bước 4); mỗi kiếp nhận thưởng một nhiệm vụ không lặp lại tối đa một lần. |
| <a id="qs-r-004"></a>**QS-R-004** | Không nhiệm vụ nào **bắt buộc chết thật** hoặc bắt buộc vào hiểm địa khi không còn bảo hộ tân thủ; mục tiêu hiểm địa luôn có đường thay thế hoặc là nhiệm vụ phụ. |
| <a id="qs-r-005"></a>**QS-R-005** | Nhiệm vụ hằng ngày / hằng tuần **không cộng dồn**: chưa làm thì hết hạn ở lần làm mới, không bù. |

---

## 2. Tổng quan

```
 LOẠI NHIỆM VỤ                   NGUỒN BẮT ĐẦU                          LẶP LẠI
 ─────────────                   ──────────────                         ───────
 TUTORIAL   hướng dẫn            tự động khi tạo nhân vật               mỗi kiếp (bỏ qua được từ kiếp 2)
 MAIN       cốt truyện chương    NPC / tự động theo cảnh giới            mỗi kiếp (lược truyện từ kiếp 2)
 SIDE       phụ, theo bản đồ     NPC, vật phẩm, khu vực                  mỗi kiếp
 BACKGROUND theo xuất thân       tự động (GD-27)                         mỗi kiếp
 SYSTEM     đại hạn, tâm ma...   tự động theo trạng thái nhân vật        khi điều kiện xảy ra lại
 SECT_DAILY Thanh Vân Tông       bảng nhiệm vụ tông môn                  hằng ngày (05:00)
 SECT_WEEKLY tông môn tự lập     bảng trú địa (Beta)                     hằng tuần (thứ Hai 05:00)

 QuestDef = điều kiện + các BƯỚC; mỗi bước = các MỤC TIÊU (giết, thu thập, tới nơi, luyện, đột phá...)
          + hội thoại Ink + hành động khi xong bước; xong bước cuối → PHẦN THƯỞNG (QuestAction)
 Tiến độ: sự kiện domain (monster.killed, craft.completed, ...) → engine nhiệm vụ → S2C_QuestProgress
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type QuestCategory = 'TUTORIAL' | 'MAIN' | 'SIDE' | 'BACKGROUND' | 'SYSTEM' | 'SECT_DAILY' | 'SECT_WEEKLY';
type QuestState = 'ACTIVE' | 'READY_TO_TURN_IN' | 'COMPLETED' | 'FAILED' | 'ABANDONED';
// GD-22 EncounterConditions.questStates: 'NOT_STARTED' (không có bản ghi) | 'ACTIVE' (ACTIVE hoặc READY_TO_TURN_IN) | 'DONE' (= COMPLETED)

interface QuestDef {
  id: string;                         // QC_* (nhiệm vụ / chuỗi), QD_* (mẫu hằng ngày), QW_* (mẫu hằng tuần)
  version: number;
  category: QuestCategory;
  chapter?: number;                   // MAIN: chương 1, 2, 3...
  titleKey: string; summaryKey: string;
  giverNpcId?: string;                // null = tự động bắt đầu
  autoStart?: QuestTrigger;
  prerequisites: QuestConditions;
  recommendedRealm: string;           // RealmCode → dải thưởng (§6.1)
  expectedMinutes: number;            // thời gian dự kiến → thưởng (§6.1)
  steps: QuestStep[];
  rewards: QuestActionRef[];          // khi hoàn thành
  repeat: 'NONE' | 'DAILY' | 'WEEKLY' | 'ON_CONDITION';
  cooldownRealSec?: number;           // với ON_CONDITION
  abandonable: boolean;
  timeLimit?: { realSec?: number; gameDays?: number };
  rebornSkip?: { allowed: boolean; rewardMult: number };   // lược truyện (§5.9)
  ink?: { file: string };             // shared/quests/ink/{file}.{lang}.ink
  actions: Record<string, QuestAction[]>;                    // khóa → hành động (danh sách trắng §8)
  tags: string[];
}

type QuestTrigger =
  | { on: 'CHARACTER_CREATED' } | { on: 'QUEST_COMPLETED'; questId: string }
  | { on: 'REALM_REACHED'; realmCode: string } | { on: 'WARNING_LEVEL'; level: WarningLevel }
  | { on: 'STATUS_GAINED'; statusId: string } | { on: 'AREA_ENTERED'; areaId: string }
  | { on: 'ITEM_ACQUIRED'; templateId: string };

interface QuestConditions {           // tất cả trường tùy chọn
  realmMin?: string; realmMax?: string; generationMin?: number;
  questsCompleted?: string[]; questsNotCompleted?: string[];
  backgrounds?: string[]; talentsAny?: string[];
  lifeFlagsAll?: string[]; lifeFlagsNone?: string[]; accountFlagsAll?: string[]; accountFlagsNone?: string[];
  npcSectRankMin?: NpcSectRank; playerSectMember?: boolean;
  statusAny?: string[];               // vd. ST_HEART_DEMON
  warningLevels?: WarningLevel[];
}

interface QuestStep {
  stepId: string;
  objectives: Objective[];
  objectiveMode: 'ALL' | 'ANY';       // ANY: một mục tiêu bất kỳ là xong bước (tạo nhánh)
  dialogueKnot?: string;              // knot Ink chạy khi bắt đầu bước (hoặc khi nói chuyện với NPC của bước)
  onComplete?: QuestActionRef[];      // hành động khi xong bước (trước khi sang bước sau)
  hintKey?: string;
  markers?: { mapId: string; position?: [number, number]; poiId?: string; radius?: number }[];
}

type Objective =
  | { type: 'TALK'; npcId: string }
  | { type: 'KILL'; monsterIds?: string[]; monsterTags?: string[]; ranks?: MonsterRank[]; mapIds?: string[]; count: number; partyShare: boolean }
  | { type: 'COLLECT'; templateId?: string; tag?: string; count: number }          // có trong BAG khi kiểm tra
  | { type: 'GATHER'; nodeIds?: string[]; templateId?: string; count: number }     // hái / khai thác
  | { type: 'DELIVER'; npcId: string; templateId: string; count: number }          // nộp cho NPC (tiêu vật phẩm)
  | { type: 'REACH'; poiId?: string; areaId?: string; mapId?: string }
  | { type: 'CRAFT'; kind: 'ALCHEMY' | 'FORGING'; recipeId?: string; recipeTag?: string; count: number; minQuality?: PillQuality; minGrade?: ItemGrade }
  | { type: 'USE_ITEM'; templateId: string; count: number }
  | { type: 'REALM'; realmCode: string }
  | { type: 'BREAKTHROUGH'; targetRealm: Realm }
  | { type: 'MEDITATE'; seconds: number; location?: MeditationLocation }
  | { type: 'SCAN'; count: number }
  | { type: 'DISCOVER'; poiKind?: PoiKind; mapId?: string; count: number }
  | { type: 'DUNGEON_CLEAR'; mapId: string; count: number }
  | { type: 'DANGER_EXTRACT'; dzId?: string; count: number }
  | { type: 'ENCOUNTER_RESOLVED'; encounterId?: string; minTier?: EncounterTier; count: number }
  | { type: 'CHOICE'; key: string };                                               // lựa chọn trong Ink gán nhánh
type QuestActionRef = string;         // khóa vào QuestDef.actions

interface QuestProgress {             // một bản ghi mỗi (nhân vật, nhiệm vụ, chu kỳ)
  characterId: string; questId: string; questVersion: number;
  state: QuestState; stepIndex: number;
  counters: Record<string, number>;   // "stepId:objectiveIndex" → số đã đạt
  branch: string | null;              // nhánh đã chọn (CHOICE / ANY)
  periodKey: string | null;           // "2026-10-22" (DAILY) / "2026-W43" (WEEKLY)
  acceptedAt: string; updatedAt: string; completedAt: string | null;
  tracked: boolean;
}

interface SectTaskBoard {             // Thanh Vân Tông hằng ngày
  characterId: string; day: string;   // theo giờ làm mới 05:00
  slots: { questId: string; state: QuestState }[];   // 3 ô
  rerollsUsed: number;
}
```

---

## 4. Máy trạng thái `QuestProgress`

| Từ | Đến | Điều kiện | Tác dụng phụ |
|----|-----|-----------|--------------|
| (chưa có) | `ACTIVE` | Nhận ở NPC hoặc `autoStart` xảy ra, thỏa `prerequisites`, sổ nhiệm vụ còn chỗ (QS-R-020) | Chạy `dialogueKnot` bước 1 (nếu có); cấp vật phẩm khởi đầu (hành động bước 0) |
| `ACTIVE` | `ACTIVE` (bước kế) | Đủ mục tiêu bước hiện tại | `onComplete` của bước; sang bước kế |
| `ACTIVE` | `READY_TO_TURN_IN` | Đủ mục tiêu bước cuối mà bước cuối là `TALK` / `DELIVER` với NPC trả | Hiện dấu "?" ở NPC |
| `ACTIVE` / `READY_TO_TURN_IN` | `COMPLETED` | Trả nhiệm vụ (hoặc tự hoàn thành nếu bước cuối không cần NPC) | Áp `rewards`; xóa vật phẩm nhiệm vụ còn lại (`QUEST_TAKE`); kích hoạt `QUEST_COMPLETED` |
| `ACTIVE` | `FAILED` | Hết `timeLimit` | Xóa vật phẩm nhiệm vụ; nhận lại được nếu lặp lại |
| `ACTIVE` | `ABANDONED` | Người chơi bỏ (chỉ khi `abandonable`) | Xóa vật phẩm nhiệm vụ; nhận lại được |

Nhiệm vụ `DAILY` / `WEEKLY` / `ON_CONDITION` có bản ghi mới cho mỗi chu kỳ (`periodKey`).

---

## 5. Quy tắc

### 5.1. Nhận và sổ nhiệm vụ
- <a id="qs-r-010"></a>**QS-R-010** Nhận nhiệm vụ: nói chuyện với `giverNpcId` (trong 4m, không chiến đấu) hoặc tự động khi `autoStart` xảy ra; luôn kiểm tra `prerequisites` tại server.
- <a id="qs-r-011"></a>**QS-R-011** NPC hiển thị dấu: "!" vàng (nhiệm vụ chính có thể nhận), "!" trắng (phụ), "?" (trả nhiệm vụ). Chỉ hiển thị cho người thỏa điều kiện.
- <a id="qs-r-020"></a>**QS-R-020** Sổ nhiệm vụ tối đa `params.maxActive` (25) nhiệm vụ đang làm, **không tính** `TUTORIAL`, `MAIN`, `SYSTEM`, `SECT_DAILY`, `SECT_WEEKLY`. Theo dõi trên HUD tối đa 5.

### 5.2. Mục tiêu và tiến độ
- <a id="qs-r-030"></a>**QS-R-030** Mỗi mục tiêu được cập nhật từ một nguồn sự kiện (§5.3). `COLLECT` kiểm tra số lượng hiện có trong `BAG` (tăng / giảm theo túi); các mục tiêu khác là bộ đếm chỉ tăng.
- <a id="qs-r-031"></a>**QS-R-031** **KILL có `partyShare = true`:** mọi người được **tính công** hạ quái (GD-12 EN-R-100) đang ở cùng bước nhiệm vụ đều +1.
- <a id="qs-r-032"></a>**QS-R-032** **Vật phẩm nhiệm vụ** (category `QUEST`, `noTrade`, `noVault`, `noSell`): chỉ rơi cho người đang có bước cần nó (rơi đồ cá nhân, GD-12); bị xóa khi nhiệm vụ kết thúc ở bất kỳ trạng thái nào.
- <a id="qs-r-033"></a>**QS-R-033** Tiến độ chỉ tính cho bước **hiện tại**; sự kiện trước khi nhận nhiệm vụ không tính (trừ `COLLECT`, `REALM` được kiểm tra trạng thái ngay khi bước bắt đầu).
- <a id="qs-r-034"></a>**QS-R-034** Mục tiêu trong hiểm địa (`DANGER_EXTRACT`, vật phẩm phải mang ra) chỉ tính khi **thoát thành công** (GD-28 DZ-R-042); chết / bị đẩy ra không tính.

### 5.3. Nguồn sự kiện của mục tiêu

| Mục tiêu | Sự kiện domain | Tài liệu |
|----------|----------------|----------|
| `TALK`, `DELIVER`, `CHOICE` | Phiên hội thoại NPC (§5.4) | GD-15 |
| `KILL` | `monster.killed` (`creditedCharacterIds`) | GD-12 §9.3 |
| `COLLECT` | `inventory` thay đổi (`S2C_InventoryDelta` phía server) | GD-09 |
| `GATHER` | `gather.completed { characterId, nodeId, templateId, qty }` | GD-21 |
| `REACH`, `DISCOVER` | `character.area_entered`, `discovery` (fog / POI) | GD-21 |
| `CRAFT` | `craft.completed` | GD-10, GD-11 |
| `USE_ITEM` | `inventory.item_used` | GD-09 |
| `REALM` | `cultivation.stage_up` | GD-04 |
| `BREAKTHROUGH` | `cultivation.breakthrough_succeeded` | GD-04 |
| `MEDITATE` | Báo cáo đả tọa đã được API Service chấp nhận | GD-04 §9.1 |
| `SCAN` | `scan.used` | GD-21 |
| `DUNGEON_CLEAR` | `boss.defeated` (`dungeonId`) | GD-12 |
| `DANGER_EXTRACT` | `danger.member_resolved` (`result = EXTRACTED`) | GD-28 |
| `ENCOUNTER_RESOLVED` | `encounter.resolved` | GD-22 |

### 5.4. Hội thoại NPC (Ink)
- <a id="qs-r-040"></a>**QS-R-040** Dùng **chung runtime Ink trên server** với GD-22 (GD-22 §5.6): biến chỉ đọc giống ENC-R-061 (thêm `npc_sect_rank`, `quest_branch`), file mỗi ngôn ngữ cùng cấu trúc (ENC-R-060).
- <a id="qs-r-041"></a>**QS-R-041** Hàm ngoài cho nhiệm vụ: `QUEST_ACTION(key)` (đưa `actions[key]` vào hàng chờ, áp khi xong bước / trả nhiệm vụ), `SET_BRANCH(key)` (hoàn thành mục tiêu `CHOICE` có cùng `key` và ghi `branch`), `HAS_ITEM`, `HAS_FLAG`, `HAS_TALENT`, `ROLL_CHECK` (như GD-22). Không có `START_COMBAT` trong hội thoại nhiệm vụ (chiến đấu dùng mục tiêu `KILL`).
- <a id="qs-r-042"></a>**QS-R-042** NPC có nhiều nhiệm vụ: menu liệt kê theo thứ tự `MAIN` → `SYSTEM` → `BACKGROUND` → `SIDE`, sau đó hội thoại thường (lore).

### 5.5. Trả nhiệm vụ và phần thưởng
- <a id="qs-r-050"></a>**QS-R-050** Phần thưởng (`rewards`) áp dụng **một lần** trong một transaction khi `COMPLETED` (khóa idempotent `characterId:questId:periodKey`). Túi đầy → vật phẩm vào `MAIL` (`overflow = MAIL`, GD-09).
- <a id="qs-r-051"></a>**QS-R-051** Tu vi và linh thạch thưởng theo §6.1 trừ khi nhiệm vụ ghi đè; tu vi qua GD-04 `cultivation/exp` với `source = QUEST`, **không** nhân `cultivationSpeed` (GD-04 CU-R-055), chặn bởi bình cảnh.
- <a id="qs-r-052"></a>**QS-R-052** Nhiệm vụ có nhánh (`CHOICE`, `objectiveMode = ANY`) có thể có `rewards` khác nhau theo `branch` (khóa `rewards` dạng `"branch:<key>"`).

### 5.6. Thanh Vân Tông: nhiệm vụ hằng ngày
- <a id="qs-r-060"></a>**QS-R-060** Nhân vật bậc ≥ `NGOAI_MON` (GD-14) có **bảng nhiệm vụ tông môn** 3 ô, làm mới lúc `params.dailyResetHour` (05:00 giờ server). Mỗi ô rút ngẫu nhiên một mẫu `QD_*` phù hợp cảnh giới (không trùng mẫu trong ngày).
- <a id="qs-r-061"></a>**QS-R-061** Mỗi nhiệm vụ hằng ngày: 10–15 phút, thưởng **100 cống hiến tông môn** + linh thạch / tu vi theo §6.1 (`expectedMinutes` của mẫu). 3 nhiệm vụ = 300 = trần cống hiến mỗi ngày (GD-13 EC-R-050).
- <a id="qs-r-062"></a>**QS-R-062** Đổi một ô: 1 lần miễn phí mỗi ngày, sau đó không đổi được. Nhiệm vụ chưa xong hết hạn lúc làm mới (QS-R-005).
- <a id="qs-r-063"></a>**QS-R-063** Nhóm mẫu hằng ngày: Diệt yêu (giết quái theo dải bản đồ), Thu thảo (hái linh thảo), Tuần sơn (tới 3 điểm trên bản đồ), Hiến đan (nộp đan tự luyện, phẩm chất ≥ Trung), Hiến khí (nộp trang bị tự rèn, Alpha có luyện khí), Tĩnh tu (đả tọa 10 phút ở linh mạch).

### 5.7. Tông môn tự lập: nhiệm vụ hằng tuần (Beta)
- <a id="qs-r-070"></a>**QS-R-070** Thành viên tông môn tự lập có 3 nhiệm vụ `QW_*` mỗi tuần (làm mới thứ Hai 05:00), dài hơn (20–40 phút, thường cần tổ đội: bí cảnh, tinh anh, hiểm địa có đường thay thế QS-R-004).
- <a id="qs-r-071"></a>**QS-R-071** Mỗi nhiệm vụ: kinh nghiệm tông môn +200 (GD-14 SO-R-055), Uy vọng +10 (GD-30 HM-R-080, trần 30 / tuần / thành viên), công trạng `merit` +50, linh thạch / tu vi §6.1.

### 5.8. Chuỗi nhiệm vụ được các tài liệu khác yêu cầu

| ID | Loại | Bắt đầu | Các bước chính | Kết quả bắt buộc | Tài liệu |
|----|------|---------|----------------|------------------|----------|
| `QC_TUTORIAL_QI` | TUTORIAL | `CHARACTER_CREATED` | (1) Nói chuyện Lão Đạo ở điểm xuất phát theo xuất thân · (2) Di chuyển, né, đánh: hạ 3 Trúc Thử ở bãi tập · (3) Đả tọa 60s (đủ 100 tu vi) · (4) Chọn vũ khí (`CHOICE`: kiếm / quạt) · (5) Dẫn khí nhập thể | Đạt điều kiện GD-04 CU-R-023 → `LK1`; học & trang bị `TEC_DAN_KHI`; học 2 kỹ năng theo vũ khí (GD-07 SK-R-033, SK-R-045); nhận vũ khí `LK1` phẩm Phàm | GD-04, GD-07 |
| `QC_MAIN_V1_JOIN_SECT` | MAIN (chương 1) | Sau `QC_TUTORIAL_QI` | (1) Tới Thanh Vân Trấn · (2) Thử thách nhập môn: hạ Trúc Vương Thử (tinh anh F01) hoặc vượt Trận Thử Tâm (`ANY`) · (3) **Chọn tông** (`SET_BRANCH`: `THANH_VAN` / `TAC_THIEN` / `HOP_HOAN`, GD-14 §5.4b) và qua thử thách gia nhập của tông đó — Thanh Vân: như bước (2); Tạc Thiên: đầu danh trạng (hạ boss BC01 hoặc nộp 3 vật phẩm hiểm địa); Hợp Hoan: Trận Mị Tâm (phiên tâm cảnh) — rồi bái sư tại tổng đàn | `JOIN_NPC_SECT {sectId}` + `SET_NPC_SECT_RANK NGOAI_MON`; cờ tài khoản `FLAG_ACC_JOINED_{SECT}` (Beta: nhánh Tạc Thiên / Hợp Hoan bật) | GD-14 SO-R-040 |
| `QC_SECT_SERVANT` | BACKGROUND | Tạo nhân vật xuất thân `BG_SECT_SERVANT` | (1) Gánh nước (nộp 5 Thủy Linh Thảo) · (2) Quét Tàng Kinh Các (`TALK` + `CHOICE`) · (3) Theo sư huynh hạ yêu (`KILL` 5 quái F01, `partyShare`) | `SET_NPC_SECT_RANK NGOAI_MON` (thay cho bước 2–3 của `QC_MAIN_V1_JOIN_SECT`, nhiệm vụ đó tự hoàn thành khi xong chuỗi này); +100 cống hiến | GD-14, GD-27 |
| `QC_TAC_THIEN_GANG` | BACKGROUND (Beta) | Tạo nhân vật xuất thân `BG_TAC_THIEN_GANG` | (1) Người liên lạc giao việc: thu "phí bảo kê" (`KILL` 5 Dã Trư F01) · (2) Đưa hàng lậu tới Tạc Thiên Trại khi đạt `LK3` (`REACH` POI tổng đàn, `ESCORT` tùy chọn) · (3) Đầu mục thử gan (`CHOICE` + `ROLL_CHECK` luck) | `JOIN_NPC_SECT SECT_TAC_THIEN` đã có từ lúc tạo; `SET_NPC_SECT_RANK NGOAI_MON` (thay nhánh Tạc Thiên của `QC_MAIN_V1_JOIN_SECT`); +100 cống hiến; `CHANGE_REPUTATION TA +100` | GD-14, GD-27 |
| `QC_HOP_HOAN_DISCIPLE` | BACKGROUND (Beta) | Tạo nhân vật xuất thân `BG_HOP_HOAN_DISCIPLE` | (1) Sứ giả dạy mị thuật cơ bản (`TALK`, dùng quạt hạ 3 quái F01) · (2) Hái 5 Đào Hoa Linh ở F01–F02 · (3) Tới Hợp Hoan Cốc khi đạt `LK3`, song tu cùng sư tỷ (`MEDITATE` 60s tại POI) | `SET_NPC_SECT_RANK NGOAI_MON` (thay nhánh Hợp Hoan); +100 cống hiến; `CHANGE_REPUTATION MA +100` | GD-14, GD-27 |
| `QC_QUIET_HEART` | SYSTEM | `STATUS_GAINED ST_HEART_DEMON` (nhận ở Tĩnh Tâm Thiền Sư, `HUB`) | (1) Đả tọa 1.800s tại `LEY_LINE` · (2) Hái 3 Nguyệt Quang Thảo (chỉ ban đêm) và nộp · (3) Đối thoại tự vấn (`CHOICE`, nội dung theo cờ kiếp, vd. `FLAG_KILLED_FOX`) | `REMOVE_STATUS ST_HEART_DEMON` (mọi tầng); `heartState` +10. `repeat = ON_CONDITION`, `cooldownRealSec` 259.200 (3 ngày) | GD-04 CU-R-095 |
| `QC_GREAT_LIMIT_SEEK` | SYSTEM | `WARNING_LEVEL GREAT_LIMIT` | (1) Gặp Thọ Lão ở `HUB` (gợi ý nơi có diên thọ theo cảnh giới) · (2) `ANY`: dùng 1 vật phẩm `LIFESPAN_EXTENSION` **hoặc** đột phá cảnh giới lớn kế tiếp | `heartState` +10, danh hiệu "Nghịch Mệnh" | GD-26 LC-R-050 |
| `QC_GREAT_LIMIT_LEGACY` | SYSTEM | `WARNING_LEVEL GREAT_LIMIT` (cùng lúc) | (1) Gặp Thọ Lão · (2) Gửi ít nhất 1 vật phẩm vào Tàng Bảo Các · (3) Viết trước lời trăn trối (`CHOICE` + nhập văn bản, lưu cho `LifeRecord.lastWords`, sửa được khi `ENDING`) | Cờ kiếp `FLAG_LEGACY_PREPARED`: khi tọa hóa nhận thêm 20 điểm Luân Hồi (GD-26 §6.4, mục `achievementPointsThisLife`) | GD-26 |

### 5.9. Lược truyện cho kiếp sau
- <a id="qs-r-090"></a>**QS-R-090** Từ kiếp 2 (`generation ≥ 2`), `QC_TUTORIAL_QI` có nút **"Bỏ qua"**: chọn vũ khí ngay rồi nhận toàn bộ kết quả bắt buộc (lên `LK1`, tâm pháp, kỹ năng, vũ khí); không nhận tu vi / linh thạch thưởng của các bước đã bỏ.
- <a id="qs-r-091"></a>**QS-R-091** Nhiệm vụ `MAIN` có `rebornSkip.allowed = true` có nút **"Lược truyện"** (khi đã đủ điều kiện nhận): hoàn thành ngay, áp **mọi hành động mở khóa** (gia nhập tông môn, cờ, học công thức...) nhưng thưởng tu vi / linh thạch / vật phẩm tiêu hao × `rebornSkip.rewardMult` (0,5). Không lược được các bước dạy hệ thống mà người chơi chưa từng hoàn thành ở **kiếp nào** của tài khoản (kiểm tra cờ tài khoản `FLAG_ACC_QUEST_DONE_<id>`).
- <a id="qs-r-092"></a>**QS-R-092** Nhiệm vụ `SIDE` không lược được (nội dung khám phá; có thể bỏ qua hoàn toàn).

### 5.10. Nhiệm vụ chính chương 1 (Vực 1 Thanh Vân)

| # | ID | Tên | Cảnh giới | Nơi | Mục đích / dạy hệ thống |
|:-:|----|-----|:---:|-----|--------------------------|
| 1 | `QC_TUTORIAL_QI` | Dẫn khí nhập thể | PN1 | Điểm xuất phát | Điều khiển, đả tọa, vũ khí |
| 2 | `QC_MAIN_V1_JOIN_SECT` | Thanh Vân bái sư | LK1 | F01 → HUB | Gia nhập Thanh Vân Tông |
| 3 | `QC_MAIN_V1_03` | Đan Phòng sơ thí | LK2 | HUB | Luyện Hồi Khí Đan đầu tiên (`CRAFT`) (GD-10) |
| 4 | `QC_MAIN_V1_04` | Bích Thủy dị văn | LK2 | F02 | Thần thức quét (`SCAN`), linh thảo theo thời tiết |
| 5 | `QC_MAIN_V1_05` | Bảng nhiệm vụ tông môn | LK3 | HUB | Giới thiệu nhiệm vụ hằng ngày, Bảo Khố |
| 6 | `QC_MAIN_V1_06` | Khoáng tặc hoành hành | LK3–4 | F03 | Chiến đấu nhóm quái, tinh anh |
| 7 | `QC_MAIN_V1_07` | Phế Khoáng Động | LK5 | BC01 | Bí cảnh solo đầu tiên (`DUNGEON_CLEAR`) |
| 8 | `QC_MAIN_V1_08` | Sương mù Vụ Ẩn | LK5 | F04 | Quái ẩn thân, Tị Độc Đan |
| 9 | `QC_MAIN_V1_09` | Vạn Độc Đầm: lần đầu | LK5 | `V1_HD01` | **Hiểm địa có hướng dẫn** (khi còn bảo hộ tân thủ, QS-R-004): thoát với 1 Ngưng Cơ Thảo; đường thay thế: nộp Bách Niên Linh Thảo ×3 nếu người chơi đã hết bảo hộ |
| 10 | `QC_MAIN_V1_10` | Linh hồ dạ hành | LK6 | F05 | Ban đêm, quái dạ hành |
| 11 | `QC_MAIN_V1_11` | Hồ Yêu Động | LK7 | BC02 | Bí cảnh tổ đội (có thể đi một mình, khó hơn) |
| 12 | `QC_MAIN_V1_12` | Cổ chiến nguyên | LK7–8 | F06 | Lore chiến tranh thượng cổ, bia đá (`DISCOVER`) |
| 13 | `QC_MAIN_V1_13` | Tàn Kiếm Trủng | LK9 | BC03 | Bí cảnh cuối chương, đan phương Trúc Cơ Đan (rơi) |
| 14 | `QC_MAIN_V1_14` | Trúc Cơ chi đạo | LK9 | HUB | Luyện Trúc Cơ Đan (`CRAFT`, mua đan phương ở Bảo Khố nếu chưa có) |
| 15 | `QC_MAIN_V1_15` | Phá cảnh | LK9 → TC1 | Linh mạch / HUB | Đột phá Trúc Cơ (`BREAKTHROUGH`), hộ pháp (gợi ý mời đồng đội) |

Mỗi bản đồ dã ngoại thêm 3–6 nhiệm vụ `SIDE` (chuẩn mật độ GD-21 §3.4), tổng ~25 nhiệm vụ phụ chương 1.

---

## 6. Công thức

### 6.1. Thưởng mặc định
```
band = band(recommendedRealm)                                  (GD-13 §6.1)
exp    = round(expectedMinutes / 60 × refExpPerHour[band] × expShare[category])
stones = round(IPH[band] × expectedMinutes / 60 × 0,8)          (GD-13 EC-R-022)
refExpPerHour: PN 300 · LK_EARLY 600 · LK_MID 600 · LK_LATE 600 · TC 1.200 · KD 2.400   (GD-04 §6.2)
expShare: MAIN 0,6 · SIDE 0,4 · TUTORIAL 0,5 · BACKGROUND 0,4 · SYSTEM 0 · SECT_DAILY 0,3 · SECT_WEEKLY 0,4
lược truyện: × rebornSkip.rewardMult (0,5)
```
Ví dụ `QC_MAIN_V1_07` (`LK5`, 20 phút, MAIN): `exp = round(20/60 × 600 × 0,6) = 120`; `stones = round(250 × 20/60 × 0,8) = 67`.
Nhiệm vụ hằng ngày `LK_MID` 12 phút: `exp = 36`, `stones = 40`, + 100 cống hiến.

### 6.2. Kiểm tra tỷ trọng tu vi (GD-04 §2: nhiệm vụ ~5%)
Chương 1 có ~15 nhiệm vụ chính (~5 giờ) + ~25 nhiệm vụ phụ (~4 giờ): tổng tu vi ≈ `5 × 600 × 0,6 + 4 × 600 × 0,4 = 2.760` ≈ 28% tổng tu vi Luyện Khí (9.880) **nếu làm hết**. Do bình cảnh và việc làm nhiệm vụ trải dài, tỷ trọng thực tế mục tiêu 5–15%; Balance Simulator kiểm chứng, nếu vượt 15% thì hạ `expShare`.

---

## 7. Dữ liệu nội dung

```
shared/quests/
├── defs/tutorial/QC_TUTORIAL_QI.yaml
├── defs/main/v1/QC_MAIN_V1_*.yaml
├── defs/side/v1/QC_SIDE_V1_<map>_*.yaml
├── defs/system/QC_QUIET_HEART.yaml, QC_GREAT_LIMIT_*.yaml
├── defs/background/QC_SECT_SERVANT.yaml
├── defs/daily/QD_*.yaml · defs/weekly/QW_*.yaml
└── ink/{file}.{vi,en}.ink
shared/gamedata/quests/params.json
```

```yaml
# shared/quests/defs/main/v1/QC_MAIN_V1_JOIN_SECT.yaml
id: QC_MAIN_V1_JOIN_SECT
version: 1
category: MAIN
chapter: 1
titleKey: q.main.v1.join_sect.title          # "Thanh Vân bái sư"
summaryKey: q.main.v1.join_sect.summary
giverNpcId: NPC_V1_LAO_DAO
autoStart: { on: QUEST_COMPLETED, questId: QC_TUTORIAL_QI }
prerequisites: { realmMin: LK1, lifeFlagsNone: [FLAG_JOINED_SECT] }
recommendedRealm: LK1
expectedMinutes: 15
repeat: NONE
abandonable: false
rebornSkip: { allowed: true, rewardMult: 0.5 }
ink: { file: main/v1/join_sect }
steps:
  - stepId: S1
    objectives: [ { type: REACH, mapId: V1_HUB } ]
    objectiveMode: ALL
    hintKey: q.main.v1.join_sect.s1
  - stepId: S2
    objectiveMode: ANY
    objectives:
      - { type: KILL, monsterIds: [MON_E_BAMBOO_KING], count: 1, partyShare: true }
      - { type: CHOICE, key: trial_of_heart }       # Trận Thử Tâm: chuỗi lựa chọn Ink
    dialogueKnot: join_sect_trial
  - stepId: S3
    objectives: [ { type: TALK, npcId: NPC_V1_SECT_ELDER } ]
    objectiveMode: ALL
    dialogueKnot: join_sect_ceremony
rewards: [ join, reward_basic ]
actions:
  join:
    - { type: SET_NPC_SECT_RANK, rank: NGOAI_MON }
    - { type: SET_FLAG, scope: LIFE, flag: FLAG_JOINED_SECT }
    - { type: SET_FLAG, scope: ACCOUNT, flag: FLAG_ACC_JOINED_SECT }
  reward_basic:
    - { type: GRANT_DEFAULT_REWARD }                # exp + linh thạch theo §6.1
    - { type: GRANT_ITEM, itemId: ITM_HOI_KHI_DAN, qty: 5, quality: TRUNG }
tags: [chapter1, sect]
```

```jsonc
// params.json
{ "maxActive": 25, "maxTracked": 5, "npcInteractRadius": 4,
  "dailyResetHour": 5, "weeklyResetDow": 1, "sectDailySlots": 3, "sectDailyFreeRerolls": 1,
  "sectDailyContribution": 100, "sectWeekly": { "slots": 3, "sectExp": 200, "prestige": 10, "merit": 50 },
  "refExpPerHour": { "PN": 300, "LK_EARLY": 600, "LK_MID": 600, "LK_LATE": 600, "TC": 1200, "KD": 2400 },
  "expShare": { "MAIN": 0.6, "SIDE": 0.4, "TUTORIAL": 0.5, "BACKGROUND": 0.4, "SYSTEM": 0, "SECT_DAILY": 0.3, "SECT_WEEKLY": 0.4 },
  "stoneFactor": 0.8, "rebornSkipRewardMult": 0.5,
  "quietHeartCooldownSec": 259200, "legacyPreparedRebirthPoints": 20 }
```

**Validator (CI):** ID đúng mẫu và duy nhất; mọi tham chiếu (NPC, quái, vật phẩm, bản đồ, POI, công thức, cơ duyên, trạng thái) tồn tại; mọi `QUEST_ACTION`/`SET_BRANCH` trong Ink khớp `actions` / `CHOICE`; mọi khóa `actions` được dùng; `QuestAction` thuộc danh sách trắng §8; nhiệm vụ có mục tiêu hiểm địa thỏa QS-R-004 (có `objectiveMode: ANY` với đường thay thế hoặc là `SIDE`); `SECT_DAILY` có `expectedMinutes` 10–15; chương MAIN tạo thành chuỗi liên tục (không có nhiệm vụ mồ côi); mỗi bản đồ `FIELD` Vực 1 có ≥ 3 `SIDE`.

---

## 8. Danh sách trắng `QuestAction`

| `type` | Tham số | Xử lý |
|--------|---------|-------|
| `GRANT_DEFAULT_REWARD` | — | Tu vi + linh thạch theo §6.1 |
| `GRANT_ITEM` | `itemId`, `qty`, `quality?`, `grade?` | GD-09 `inventory/grant` (`QUEST_REWARD`, `overflow = MAIL`) |
| `TAKE_ITEM` | `itemId`, `qty` | GD-09 `inventory/consume` (`QUEST_TAKE`); thiếu → bước không hoàn thành |
| `GRANT_CURRENCY` | `currency` (`SPIRIT_STONE` / `SECT_CONTRIBUTION`), `amount` | GD-09 `currency/change`; cống hiến chặn trần ngày (GD-13 EC-R-050) |
| `GRANT_CULTIVATION` | `exp` | GD-04 `cultivation/exp` (`QUEST`, không nhân tốc độ) |
| `LEARN_TECHNIQUE` | `refId` (`TEC_*` / `SKL_*`), `equipMain?`, `slot?` | GD-07 (không qua vật phẩm ngọc giản) |
| `LEARN_RECIPE` | `recipeId` | GD-10 / GD-11 |
| `SET_NPC_SECT_RANK` | `rank` | GD-14 (chỉ tăng bậc) |
| `ADVANCE_TUTORIAL_REALM` | — | Đánh dấu hoàn thành hướng dẫn cho GD-04 CU-R-023 (PN1 → LK1) |
| `REMOVE_STATUS` | `statusId`, `allStacks` | GD-06 |
| `CHANGE_HEART_STATE` | `delta` (−20..20) | GD-04 |
| `CHANGE_KARMA` | `delta` (−20..20) | GD-27 |
| `CHANGE_REPUTATION` | `faction: CHINH \| TA \| MA`, `delta` (−300..300) | GD-14 §5.4c |
| `SET_FLAG` | `scope` (`LIFE` / `ACCOUNT`), `flag` | Cờ |
| `START_QUEST` | `questId` | Nhận nhiệm vụ kế (bỏ qua điều kiện NPC) |
| `UNLOCK_CODEX` | `entryType`, `entryId` | GD-21 §3.6 |
| `GRANT_TITLE` | `titleId` | Danh hiệu |
| `SET_LAST_WORDS` | (lấy từ ô nhập trong hội thoại) | GD-26 `LifeRecord.lastWords` nháp |

Không có loại khác. Không có hành động tăng thọ nguyên, thiên phú, linh căn (những thứ đó thuộc cơ duyên / đột phá).

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/characters/{id}/quests` | Đang làm, đã xong (ID), có thể nhận ở NPC gần | |
| `POST /v1/characters/{id}/quests/{questId}/accept` | Nhận (Game Server xác nhận đứng gần NPC) | QS-R-010, 020 |
| `POST /v1/characters/{id}/quests/{questId}/abandon` | Bỏ | §4 |
| `POST /v1/characters/{id}/quests/{questId}/turn-in` | Trả | QS-R-050 |
| `POST /v1/characters/{id}/quests/{questId}/skip` | Bỏ qua hướng dẫn / lược truyện | QS-R-090, 091 |
| `POST /v1/characters/{id}/quests/{questId}/track` | `{ tracked }` | QS-R-020 |
| `GET /v1/characters/{id}/sect-tasks` · `POST .../sect-tasks/{slot}/reroll` | Bảng Thanh Vân Tông hằng ngày | §5.6 |

### 9.2. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `C2S_NpcInteract` | C→S | `{ npcId }` |
| `S2C_NpcMenu` | S→C | `{ npcId, quests: [{ questId, kind: 'ACCEPT' \| 'TURN_IN' \| 'STEP_TALK' }], hasLore }` |
| `S2C_DialogueStep` · `C2S_DialogueChoose` | S↔C | Như GD-22 `S2C_EncounterStep` / `C2S_EncounterChoose`, thêm `textInput?` cho `SET_LAST_WORDS` |
| `S2C_QuestProgress` | S→C | `{ questId, state, stepIndex, counters, markers }` |
| `S2C_QuestCompleted` | S→C | `{ questId, rewards: [...] }` |

### 9.3. Sự kiện domain

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `quest.completed` | `{ characterId, accountId, questId, category, branch, periodKey }` | Uy vọng (GD-30), kinh nghiệm tông môn (GD-14), telemetry, `autoStart` của nhiệm vụ khác |
| `quest.accepted` / `quest.abandoned` / `quest.failed` | `{ characterId, questId }` | Telemetry |
| Tiêu thụ: các sự kiện §5.3; `lifecycle.warning_level_changed`; `lifecycle.life_ending` (xóa tiến độ) | | |

---

## 10. Lưu trữ

```sql
CREATE TABLE quest_progress (                      -- thay bảng sơ bộ ở PLAN §5.4
  character_id UUID NOT NULL REFERENCES character(id),
  quest_id VARCHAR(64) NOT NULL,
  period_key VARCHAR(16) NOT NULL DEFAULT '',      -- '' với nhiệm vụ không lặp
  quest_version INT NOT NULL,
  state VARCHAR(20) NOT NULL,
  step_index SMALLINT NOT NULL DEFAULT 0,
  counters JSONB NOT NULL DEFAULT '{}',
  branch VARCHAR(32),
  tracked BOOLEAN NOT NULL DEFAULT FALSE,
  accepted_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at TIMESTAMPTZ,
  PRIMARY KEY (character_id, quest_id, period_key)
);
CREATE INDEX ix_quest_active ON quest_progress(character_id) WHERE state IN ('ACTIVE', 'READY_TO_TURN_IN');

CREATE TABLE sect_task_board (
  character_id UUID NOT NULL REFERENCES character(id), day DATE NOT NULL,
  slots JSONB NOT NULL, rerolls_used SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, day)
);

CREATE TABLE quest_reward_log (                    -- idempotency phần thưởng
  idempotency_key VARCHAR(120) PRIMARY KEY,        -- characterId:questId:periodKey
  character_id UUID NOT NULL, quest_id VARCHAR(64) NOT NULL, result JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

---

## 11. UI/UX
- **Sổ nhiệm vụ** (phím `L`): tab Chính · Phụ · Tông môn · Hệ thống; mỗi nhiệm vụ có tóm tắt, bước hiện tại, mục tiêu (x/y), gợi ý, phần thưởng; nút Theo dõi / Bỏ / Lược truyện.
- **HUD theo dõi:** tối đa 5 nhiệm vụ cạnh minimap; mục tiêu đổi màu khi xong.
- **Bản đồ / minimap:** dấu vị trí mục tiêu (`markers`); khu vực mục tiêu là vòng tròn mờ thay vì điểm chính xác với mục tiêu khám phá.
- **Hội thoại:** khung giấy cổ phong như cơ duyên (GD-22 §11), chân dung NPC.
- **Bảng tông môn:** 3 thẻ nhiệm vụ, đồng hồ tới lần làm mới, nút đổi (còn 1 lần).
- **Lược truyện:** hộp thoại liệt kê những gì sẽ nhận (mở khóa đầy đủ, thưởng × 0,5).

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-22 | Runtime Ink, biến, `ROLL_CHECK`; điều kiện `questStates` |
| GD-04 | Hướng dẫn PN1 → LK1; tu vi `QUEST`; `QC_QUIET_HEART`; mục tiêu đả tọa / đột phá |
| GD-07 | Học tâm pháp / kỹ năng khởi đầu |
| GD-09 | Vật phẩm thưởng / nhiệm vụ, `QUEST_REWARD` / `QUEST_TAKE` |
| GD-10 / GD-11 | Mục tiêu `CRAFT`, nhiệm vụ hằng ngày nộp đan / khí |
| GD-12 | Mục tiêu `KILL`, `DUNGEON_CLEAR`; rơi vật phẩm nhiệm vụ |
| GD-13 | Linh thạch thưởng, cống hiến |
| GD-14 | Gia nhập Thanh Vân Tông, bảng hằng ngày; kinh nghiệm tông môn tự lập |
| GD-21 | Mục tiêu `REACH`, `DISCOVER`, `GATHER`, `SCAN`; dấu bản đồ |
| GD-26 | Đại hạn, lời trăn trối, xóa tiến độ khi hết kiếp |
| GD-27 | Chuỗi theo xuất thân |
| GD-28 | Mục tiêu `DANGER_EXTRACT`, hiểm địa có hướng dẫn |
| GD-30 | Uy vọng từ nhiệm vụ hằng tuần |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Vật phẩm `COLLECT` bị bán / dùng trước khi trả | Bộ đếm giảm, bước chưa xong |
| Đang có bước `KILL` thì đồng đội (không cùng nhiệm vụ) hạ quái | Không tính cho người không được tính công (QS-R-031) |
| Nội dung nhiệm vụ đổi phiên bản khi đang làm | Giữ `questVersion` cũ tới khi xong; nếu bước bị xóa → nhảy tới bước tương ứng gần nhất theo bảng ánh xạ phiên bản (content designer cung cấp) |
| Đã đạt cảnh giới cao hơn khi nhận nhiệm vụ `REALM` | Hoàn thành ngay (QS-R-033) |
| Bảng hằng ngày làm mới khi đang làm dở | Nhiệm vụ dở `FAILED`, bảng mới 3 ô |
| Hết kiếp khi đang làm chuỗi đại hạn | Bị xóa; `FLAG_LEGACY_PREPARED` (nếu đã đặt) được dùng ở bước tính điểm Luân Hồi trước khi xóa |
| Người chơi bỏ qua hướng dẫn nhưng chưa từng làm ở kiếp nào | Từ kiếp 2 trở đi luôn có nút bỏ qua (người chơi kiếp 2 đã chơi kiếp 1) |
| Mất kết nối giữa hội thoại | Như GD-22 (giữ `inkState` 120s) |

---

## 14. Telemetry

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `quest_funnel` | questId, stepIndex, minutesInStep | **Bước nào người chơi bỏ cuộc** (chương 1) |
| `quest_completed` | questId, minutes, generation, skipped | Thời gian thực tế so với `expectedMinutes`; tỷ lệ lược truyện |
| `quest_abandoned` | questId, stepIndex | Nhiệm vụ phụ kém hấp dẫn |
| `sect_daily` | completedCount / 3, rerolled | Mức độ "nghĩa vụ hóa" |
| `quest_exp_share` | % tu vi từ nhiệm vụ theo dải | Kiểm chứng §6.2 |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="qs-ac-001"></a>QS-AC-001 | Nhân vật mới | Tạo nhân vật | `QC_TUTORIAL_QI` `ACTIVE` tự động |
| QS-AC-002 | Hoàn thành `QC_TUTORIAL_QI`, chọn quạt | Kết thúc | `LK1`, trang bị `TEC_DAN_KHI`, biết `SKL_FIRE_ORB`, `SKL_FROST_FIELD`, có quạt `LK1` Phàm |
| QS-AC-003 | Kiếp 2 | Bấm "Bỏ qua" hướng dẫn, chọn kiếm | Như QS-AC-002 (kiếm) nhưng không nhận tu vi / linh thạch thưởng |
| QS-AC-004 | Hoàn thành `QC_MAIN_V1_JOIN_SECT` | Kết thúc | Bậc `NGOAI_MON`; cờ `FLAG_ACC_JOINED_SECT` |
| QS-AC-005 | Xuất thân `BG_SECT_SERVANT` | Xong `QC_SECT_SERVANT` | `NGOAI_MON`, +100 cống hiến; `QC_MAIN_V1_JOIN_SECT` tự hoàn thành |
| QS-AC-006 | Bước `KILL` 5 quái `partyShare`, tổ đội 3 người cùng nhiệm vụ | Hạ 1 quái, cả ba được tính công | Cả ba +1 |
| QS-AC-007 | Bước `COLLECT` 3 Thủy Linh Thảo, có 3 rồi bán 1 | Kiểm tra | 2/3, chưa xong |
| QS-AC-008 | Ink gọi `QUEST_ACTION("lạ")` không có trong `actions` | Validator CI | Fail |
| QS-AC-009 | Trả nhiệm vụ hai lần (gửi lặp) | API | Thưởng một lần (`quest_reward_log`) |
| QS-AC-010 | Ví dụ §6.1 `QC_MAIN_V1_07` | Trả nhiệm vụ | +120 tu vi (không nhân `cultivationSpeed`), +67 linh thạch |
| QS-AC-011 | Nhân vật ở bình cảnh viên mãn | Nhận thưởng tu vi nhiệm vụ | Tu vi lãng phí theo GD-04 CU-R-002 |
| QS-AC-012 | Bậc `NGOAI_MON` | 05:00 | Bảng tông môn có 3 nhiệm vụ khác mẫu, phù hợp cảnh giới |
| QS-AC-013 | Hoàn thành 3 nhiệm vụ hằng ngày | Tổng cống hiến hôm đó | 300 |
| QS-AC-014 | Đổi ô lần 2 trong ngày | Đổi | Từ chối |
| QS-AC-015 | Nhiệm vụ hằng ngày làm dở lúc 05:00 | Làm mới | `FAILED`, không bù |
| QS-AC-016 | Nhận `ST_HEART_DEMON` | Theo dõi | `QC_QUIET_HEART` khả dụng ở Tĩnh Tâm Thiền Sư; xong → gỡ mọi tầng, tâm cảnh +10; làm lại trong 3 ngày bị chặn |
| QS-AC-017 | Thọ còn ≤ 10% | `warning_level_changed` → `GREAT_LIMIT` | `QC_GREAT_LIMIT_SEEK` và `QC_GREAT_LIMIT_LEGACY` cùng `ACTIVE` |
| QS-AC-018 | Hoàn thành `QC_GREAT_LIMIT_LEGACY` rồi tọa hóa | GD-26 tính điểm | +20 điểm Luân Hồi |
| QS-AC-019 | `QC_MAIN_V1_09` khi nhân vật kiếp 2 (hết bảo hộ) | Xem bước | Có đường thay thế nộp Bách Niên Linh Thảo ×3 (QS-R-004) |
| QS-AC-020 | Mục tiêu `DANGER_EXTRACT`, chết trong hiểm địa | Tiến độ | Không tính |
| QS-AC-021 | Kiếp 2, `QC_MAIN_V1_03` | Lược truyện | Hoàn thành, mở khóa đầy đủ, thưởng × 0,5 |
| QS-AC-022 | Nhiệm vụ `SIDE` | Thử lược truyện | Không có nút |
| QS-AC-023 | Kết thúc kiếp | GD-26 bước 4 | Mọi `quest_progress` của nhân vật bị đóng; vật phẩm `QUEST` bị hủy cùng túi |
| QS-AC-024 | (Beta) Hoàn thành 4 nhiệm vụ `QW_*` trong tuần | Uy vọng | +30 (trần), kinh nghiệm tông môn +600 cho 3 nhiệm vụ đầu |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 2 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Engine nhiệm vụ (mục tiêu, sự kiện, thưởng, Ink) | ✅ | | | |
| `QC_TUTORIAL_QI`, `QC_MAIN_V1_JOIN_SECT`, 15–20 nhiệm vụ chương 1 | ✅ | | | |
| Bảng Thanh Vân Tông hằng ngày | | ✅ | | |
| `QC_QUIET_HEART`, `QC_GREAT_LIMIT_*`, `QC_SECT_SERVANT` | | ✅ | | |
| Lược truyện / bỏ qua hướng dẫn | | ✅ | | |
| ~25 nhiệm vụ phụ Vực 1 | | ✅ | | |
| Nhiệm vụ tông môn tự lập hằng tuần | | | ✅ | |
| Chương 2 (Vực 2) | | | ✅ | |
| Tháp thí luyện (PLAN §4.16) | | | | ✅ (tài liệu riêng) |

---

## 17. Câu hỏi mở
- [ ] **QS-Q-1:** Lược truyện thưởng × 0,5 có làm người chơi kiếp sau lên Luyện Khí quá nhanh? Đo thời gian tới `LK5` của kiếp ≥ 2.
- [ ] **QS-Q-2:** Có cho nhiệm vụ phụ "ẩn" chỉ xuất hiện với xuất thân / thiên phú cụ thể (giống cơ duyên) không? *Mặc định: có, qua `prerequisites.backgrounds` / `talentsAny`.*
- [ ] **QS-Q-3:** Nhiệm vụ hằng ngày có nên cho làm bù tối đa 1 ngày (bận một hôm) không? Mâu thuẫn QS-R-005. *Mặc định: không.*
- [ ] **QS-Q-4:** Có lồng tiếng / thoại có âm thanh cho nhiệm vụ chính không (chi phí lớn)? *Mặc định: không, chỉ chữ + hiệu ứng âm thanh.*
- [ ] **QS-Q-5:** `QC_MAIN_V1_09` (hiểm địa có hướng dẫn) có nên đặt sớm hơn (LK4) để người chơi trải nghiệm trước khi bảo hộ có thể kết thúc?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-22 | — | Bản nháp đầu: bất biến, kiểu dữ liệu (nhiệm vụ / bước / 17 loại mục tiêu), máy trạng thái, nguồn sự kiện, hội thoại Ink dùng chung GD-22, thưởng, Thanh Vân Tông hằng ngày, tông môn tự lập hằng tuần, 6 chuỗi được tài liệu khác yêu cầu, lược truyện cho kiếp sau, 15 nhiệm vụ chính chương 1, công thức thưởng + kiểm tra tỷ trọng, định dạng YAML, danh sách trắng hành động, API, DB, nghiệm thu |
| v0.2 | 2026-10-23 | — | `QC_MAIN_V1_JOIN_SECT` có 3 nhánh tông (GD-14 §5.4b); hành động `JOIN_NPC_SECT` |
| v0.3 | 2026-10-23 | — | Chuỗi xuất thân `QC_TAC_THIEN_GANG`, `QC_HOP_HOAN_DISCIPLE`; hành động `CHANGE_REPUTATION` (GD-14 §5.4c) |

