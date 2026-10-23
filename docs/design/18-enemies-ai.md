# GD-12 — Quái vật & AI

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-12 · Prefix ID: `EN` |
| Owner | Game Designer + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.1 |
| Cần trước | Phase 1 (3 quái thường + 1 tinh anh + 1 boss, AI cơ bản), Phase 2–3 (bestiary Vực 1, spawn theo giờ / thời tiết, bảng rơi đồ) |
| Liên quan | [PLAN §4.9, §4.13](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-07 Kỹ năng](13-skills.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-21 Open World](22-open-world.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-26 Vòng đời](03-lifecycle.md) · GD-09 Vật phẩm · GD-16 Level Design · GD-23 Sự kiện thế giới · GD-28 Hiểm địa · TD-06 Game Server |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Dữ liệu nằm ở `shared/gamedata/monsters/*.json` (§7). Quái **dùng chung** công thức của GD-06 và khung kỹ năng của GD-07; tài liệu này định nghĩa **quái là gì, xuất hiện ở đâu, nghĩ gì, rơi gì**.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-12 | Mục |
|--------------|-----------------|-----|
| GD-04 | `monsterRealmCode`, `baseExp`, `rank`; quy tắc ai được tính công hạ quái | §3, §5.9 |
| GD-06 | Hệ số riêng của loài (`statMult`), `elementAffinity`, `maxPoise` | §3, §5.1 |
| GD-07 | Quái dùng `SkillDef`; quái né vùng cảnh báo của người chơi | §5.5, §5.6 |
| GD-21 | Bảng spawn theo khu, tinh anh định kỳ, quái theo giờ / thời tiết, quái ẩn thân, vùng đệm an toàn | §5.2, §5.11, §5.12 |
| GD-22 | `spawnGroups` cho `START_COMBAT`, trigger `KILL`, quái cá nhân | §5.10 |
| GD-28 | Quái phản ứng thần thức quét, quái không đánh người hấp hối, quy tức | §5.3, §5.13 |
| GD-21 §3.6 | Tiến độ Vạn Giới Lục mục Yêu thú | §5.9 |

---

## 1. Mục tiêu

**Trụ cột:** P3 *Chiến đấu hành động có kỹ năng*, P5 *Thế giới đáng khám phá*.

- Mỗi loại quái **đọc được**: nhìn dáng và chiêu là biết cách đánh (né, đỡ, áp sát, giữ khoảng cách).
- Chết vì quái phải là **lỗi của người chơi**, không phải vì chiêu không báo trước, bị bao vây vô lý hay độ trễ mạng.
- Quái làm thế giới sống động: theo giờ, theo thời tiết, phục kích, bầy đàn.
- Team nhỏ làm được nhiều loại quái: **tái sử dụng** khung xương, model đổi màu, chiêu ghép từ dữ liệu.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="en-r-001"></a>**EN-R-001** | AI chạy **hoàn toàn trên Game Server**. Client chỉ hiển thị trạng thái và animation. |
| <a id="en-r-002"></a>**EN-R-002** | Mọi chiêu gây sát thương của quái có **vùng cảnh báo trên mặt đất** với thời gian tối thiểu theo hạng (§5.6). Validator chặn chiêu vi phạm. |
| <a id="en-r-003"></a>**EN-R-003** | Quái dùng đúng công thức GD-06 và khung `SkillDef` GD-07; không có sát thương "đặc biệt" ngoài dữ liệu. |
| <a id="en-r-004"></a>**EN-R-004** | Rơi đồ **cá nhân**: mỗi người chơi đủ điều kiện có lượt roll và túi đồ riêng; không có tranh giành / cướp quái (kill-steal). |
| <a id="en-r-005"></a>**EN-R-005** | Quái không bao giờ đi vào vùng đệm an toàn (GD-21 §3.3) và không spawn trong đó. |
| <a id="en-r-006"></a>**EN-R-006** | Hành vi quái mô tả bằng dữ liệu: archetype + tham số + danh sách chiêu có trọng số + kịch bản pha (boss). Không viết code riêng cho từng loài. |

---

## 2. Tổng quan

```
 Bảng spawn theo khu (GD-21) ──► SPAWN (ngoài tầm nhìn người chơi, ngoài vùng đệm)
                                   ▼
                         IDLE / PATROL quanh điểm neo
                                   ▼ thấy / nghe người chơi (hoặc bị đánh)
                         ALERT (0,5s, biểu tượng "!")  ──► gọi đồng loại gần (social)
                                   ▼
                         COMBAT: chọn mục tiêu theo threat → chọn chiêu theo điểm hữu dụng
                                 → áp sát / giữ khoảng cách → thi triển (GD-07) → [né vùng cảnh báo]
                                   ▼ mục tiêu ra ngoài dây xích / mất mục tiêu
                         RETURNING (bất tử, chạy về, hồi đầy)
                                   ▼ HP về 0
                         DEAD → tu vi (GD-04) · rơi đồ cá nhân · trigger KILL (GD-22) · Vạn Giới Lục
                                   ▼
                         chờ hồi sinh theo bảng spawn
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type MonsterRank = 'NORMAL' | 'ELITE' | 'BOSS';
type MonsterFamily = 'BEAST' | 'HUMANOID' | 'SPIRIT' | 'PLANT' | 'INSECT' | 'CONSTRUCT' | 'UNDEAD' | 'DEMON';
type SkeletonGroup = 'HUMANOID' | 'QUADRUPED' | 'BIRD' | 'SERPENT' | 'NONE';   // chia sẻ animation (PLAN §6.2)
type AiArchetype =
  | 'MELEE_BRUISER' | 'MELEE_SKIRMISHER' | 'RANGED_KITER' | 'CASTER'
  | 'SWARM' | 'AMBUSHER' | 'TANK' | 'PASSIVE' | 'BOSS_SCRIPTED';
type ActiveTime = 'ALWAYS' | 'DAY' | 'NIGHT';

interface MonsterDef {
  id: string;                         // MON_* (thường), MON_E_* (tinh anh), MON_B_* (boss)
  nameKey: string; codexId: string;
  family: MonsterFamily; skeleton: SkeletonGroup; modelId: string; tint?: string;
  rank: MonsterRank;
  realmCode: string;                  // RealmCode → base_stats (GD-06 §5.2)
  elementAffinity: Element | 'NONE';
  statMult: Partial<Record<'maxHp' | 'attack' | 'spellPower' | 'defense' | 'spellResist' | 'moveSpeed', number>>; // mặc định 1
  maxPoiseOverride?: number;
  baseExp: number;                    // GD-04 §6.7
  archetype: AiArchetype;
  aiOverrides?: Partial<ArchetypeParams>;
  perception: { sightRadius: number; fovDeg: number; hearingRadius: number; alertOnScan: boolean };
  aggressive: boolean;                // false = chỉ đánh khi bị tấn công
  social: { assistRadius: number; assistFamilies?: MonsterFamily[] };
  basicSkillId: string;               // SKL_* dùng làm đòn cơ bản
  skills: MonsterSkillRef[];
  leashRadius: number;
  size: 'S' | 'M' | 'L' | 'XL';       // bán kính thân, TD-14 GEO-R-020
  beastSpeciesId?: string;            // BST_* — loài nuôi được, có biến thể Linh chủng (GD-25 §5.1)
  capture?: { speciesId: string; bloodlineWeights: Partial<Record<BloodlineGrade, number>> };   // chỉ boss đặc biệt (GD-25 §5.2b)
  hiddenLevel: number;                // 0 = luôn thấy; > 0 = ẩn thân (GD-21 §3.7)
  activeTime: ActiveTime;
  nightStatBonusPct?: number;         // GD-21 §3.8 (vd. 0,15)
  weatherActive?: Weather[];          // chỉ xuất hiện khi thời tiết này
  lootTableId: string | null;
  tags: string[];                     // vd. "pack", "poison", "undead" (dùng cho điều kiện cơ duyên / nhiệm vụ)
}

interface MonsterSkillRef {
  skillId: string;                    // SKL_* (GD-07); quái dùng chung định dạng
  weight: number;
  minRange: number; maxRange: number;
  cooldownSecOverride?: number;
  conditions?: { hpBelowPct?: number; hpAbovePct?: number; phaseIds?: string[]; targetsInRangeMin?: number };
}

interface ArchetypeParams {
  preferredRange: [number, number];   // khoảng cách muốn giữ với mục tiêu
  attackIntervalSec: [number, number];// nghỉ giữa hai hành động (random đều trong khoảng)
  repositionChance: number;           // sau mỗi hành động, xác suất đổi vị trí
  dodgeChance: number;                // né khi đứng trong vùng cảnh báo của người chơi
  retreatBelowHpPct?: number;         // PASSIVE / RANGED_KITER
  meleeToken: boolean;                // cần "suất cận chiến" để đánh (§5.7)
}

interface SpawnTable {
  id: string;                         // SP_{mapId}_{areaId}
  mapId: string; areaId: string;
  maxAlive: number;
  respawnSec: [number, number];
  entries: { monsterId: string; weight: number; pack: [number, number];
             conditions?: { dayPhases?: DayPhase[]; shiChen?: ShiChen[]; weather?: Weather[] } }[];
  spawnPoints: [number, number][];    // xuất từ map editor (TD-13)
}

interface EliteSpawnDef {
  id: string; mapId: string; monsterId: string;
  positions: [number, number][];
  intervalMin: [number, number];      // GD-21 §3.4: 20–40 phút
  announceSecBefore: number;          // 60
  conditions?: { dayPhases?: DayPhase[]; weather?: Weather[] };
}

interface SpawnGroupDef {             // GD-22 START_COMBAT
  id: string;                         // SG_*
  entries: { monsterId: string; count: number }[];
  pattern: 'AROUND_PLAYER' | 'AT_CUE';
  radius: number;
  ownerOnly: boolean;                 // true: chỉ chủ phiên tương tác (ENC-R-001)
  grantExp: boolean; grantLoot: boolean;
}

interface BossDef {                   // bổ sung cho MonsterDef có rank BOSS
  monsterId: string;
  arena: { center: [number, number]; radius: number };
  phases: BossPhase[];
  enrage: { afterSec: number; damageBonusPerStep: number; stepSec: number };
  partyHpScaling: number;             // §6.3
}
interface BossPhase {
  id: string;                         // P1, P2...
  enterAtHpPct: number;               // 100 cho pha đầu
  onEnter: BossAction[];
  skills: MonsterSkillRef[];
  rotation: 'WEIGHTED' | 'SEQUENCE';
}
type BossAction =
  | { type: 'CAST'; skillId: string }
  | { type: 'SUMMON'; monsterId: string; count: number; radius: number; linkPoisePct?: number }
  | { type: 'SHIELD'; pctMaxHp: number; breakByPoise: boolean }
  | { type: 'INVULNERABLE_UNTIL_ADDS_DEAD' }
  | { type: 'MOVE_TO'; point: [number, number] }
  | { type: 'ANNOUNCE'; textKey: string };

interface LootTable {
  id: string;                         // LT_*
  rolls: number;
  nothingWeight: number;              // không bị khí vận ảnh hưởng
  entries: LootEntry[];
  guaranteed?: LootEntry[];           // luôn rơi (vd. linh thạch)
}
interface LootEntry {
  itemId?: string; lootTableId?: string;  // một trong hai (bảng lồng nhau)
  currency?: 'SPIRIT_STONE';
  weight: number;
  qty: [number, number];
  rarity: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';   // ↔ ItemGrade PHAM..THAN (GD-09 IT-R-060); với trang bị: quyết định phẩm sinh ra
  conditions?: { dayPhases?: DayPhase[]; weather?: Weather[]; realmMax?: string };
}
```

---

## 4. Máy trạng thái AI

| Trạng thái | Hành vi | Ra khi | Ghi chú |
|------------|---------|--------|---------|
| `SPAWNING` | Hiện dần 0,8s (bất tử, không hành động) | Hết thời gian | |
| `IDLE` | Đứng / đi lang thang trong bán kính 5m quanh điểm neo | Phát hiện người chơi (§5.3) hoặc bị đánh | Quái `PASSIVE` ăn cỏ, quái `AMBUSHER` nằm phục |
| `PATROL` | Đi theo đường tuần tra (nếu map editor có) | Như trên | |
| `ALERT` | Quay về phía mục tiêu, biểu tượng "!" 0,5s; gọi đồng loại | Hết 0,5s → `COMBAT` | Bị đánh thì bỏ qua `ALERT` |
| `COMBAT` | Chọn mục tiêu (§5.3), chọn chiêu (§5.5), di chuyển, thi triển theo máy trạng thái hành động GD-07 §4 | Mục tiêu chết / mất / ra ngoài dây xích → chọn mục tiêu khác; không còn ai → `RETURNING` | |
| `RETURNING` | Chạy về điểm neo, `ST_INVULNERABLE`, xóa status | Về tới nơi → hồi đầy HP / poise → `IDLE` | Bị đánh khi đang về: không phản ứng |
| `FLEEING` | Chạy xa mục tiêu (quái `PASSIVE` / máu thấp) | 6s hoặc ra ngoài dây xích | |
| `DEAD` | Xác nằm 3s rồi tan | — | Rơi đồ, tu vi, trigger tại thời điểm chết |

- <a id="en-r-010"></a>**EN-R-010** Quyết định AI chạy ở `params.aiDecisionHz` (5Hz) cho quái **hoạt động**; di chuyển theo tick server (20Hz). Quái **ngủ** (không người chơi nào trong `params.dormantRadius` 80m) không tick AI; spawn vẫn chạy.

---

## 5. Quy tắc

### 5.1. Hạng và chỉ số
- <a id="en-r-020"></a>**EN-R-020** `stat = base_stats[realmCode][stat] × rankMult[rank][stat] (GD-06 SF-R-130) × statMult[stat]`, làm tròn nửa lên; rồi đi qua `computeStats` (GD-06) như người chơi (status vẫn áp được). `maxPoise` theo GD-06 trừ khi có `maxPoiseOverride`.
- <a id="en-r-021"></a>**EN-R-021** Quái có `nightStatBonusPct`: khi `DayPhase = NIGHT` nhận modifier `PCT` +giá trị lên `attack`, `spellPower`, `defense`, `spellResist` (nguồn `STATUS`, ID `ST_NIGHT_EMPOWER`).
- <a id="en-r-022"></a>**EN-R-022** Khống chế lên tinh anh ×0,5 thời lượng; boss miễn khống chế trừ khi phá thế (GD-06 SF-R-122).

### 5.2. Spawn
- <a id="en-r-030"></a>**EN-R-030** Mỗi khu (`Area`, GD-21) có tối đa 1 `SpawnTable`; số quái sống trong khu ≤ `maxAlive`. Spawn riêng theo **từng kênh**.
- <a id="en-r-031"></a>**EN-R-031** Khi số quái sống < `maxAlive`, sau thời gian random trong `respawnSec` (GD-21: 60–120s), chọn một mục theo `weight` trong các mục thỏa `conditions` (giờ / thời tiết hiện tại), spawn một bầy cỡ random trong `pack`.
- <a id="en-r-032"></a>**EN-R-032** Điểm spawn: chọn ngẫu nhiên trong `spawnPoints` sao cho cách mọi người chơi ≥ `params.spawnMinPlayerDistance` (15m) và **ngoài vùng đệm an toàn**; không có điểm hợp lệ thì thử lại sau 5s.
- <a id="en-r-033"></a>**EN-R-033** Quái `activeTime = NIGHT` / `DAY` hoặc có `weatherActive`: chỉ spawn khi thỏa; khi hết điều kiện, quái **không trong chiến đấu** tan biến (hiệu ứng tan vào sương / đất) trong 10s; quái đang chiến đấu ở lại tới khi chết hoặc reset.
- <a id="en-r-034"></a>**EN-R-034** **Tinh anh định kỳ** (`EliteSpawnDef`): mỗi lần chết hẹn giờ ngẫu nhiên trong `intervalMin`; trước `announceSecBefore` (60s) phát `S2C_EliteAnnounce` cho mọi người chơi trong bản đồ (kênh đó) và hiện biểu tượng trên minimap. Tối đa 1 bản thể mỗi `EliteSpawnDef` mỗi kênh.
- <a id="en-r-035"></a>**EN-R-035** **Giờ Tý** (GD-21 §3.8): bảng spawn có thể chứa mục với `shiChen: [TY]` cho quái hiếm; mục này chỉ tham gia roll trong giờ Tý.

### 5.3. Nhận biết, threat, mục tiêu
- <a id="en-r-040"></a>**EN-R-040** Quái `aggressive` phát hiện người chơi khi: trong `sightRadius` **và** trong góc `fovDeg`, **hoặc** trong `hearingRadius` khi người chơi đang chiến đấu / thi triển kỹ năng. Quái không `aggressive` chỉ vào chiến đấu khi bị đánh.
- <a id="en-r-041"></a>**EN-R-041** Không phát hiện / bỏ mục tiêu khi người chơi: `ST_INVULNERABLE`, đang trong phiên cơ duyên `ACTIVE` (GD-22 ENC-R-052), trong vùng đệm an toàn, `DOWNED` (§5.13), đã chuyển bản đồ, hoặc `lifeState ≠ ALIVE`.
- <a id="en-r-042"></a>**EN-R-042** **Threat:** mỗi quái giữ bảng threat theo người chơi: +1 mỗi điểm sát thương gây lên quái; +0,5 × lượng hồi máu cho đồng minh đang trong bảng threat của quái (chia đều cho các quái đó); +50 khi bị người chơi đỡ hoàn hảo. Người phát hiện đầu tiên nhận threat khởi điểm 10.
- <a id="en-r-043"></a>**EN-R-043** Mục tiêu = threat cao nhất; chỉ đổi mục tiêu khi người mới vượt **110%** threat mục tiêu hiện tại (cận chiến) hoặc **130%** (đánh xa). Threat giảm 5%/s với người chơi ngoài `sightRadius`.
- <a id="en-r-044"></a>**EN-R-044** **Gọi đồng loại:** khi vào `ALERT`/`COMBAT`, quái cùng `family` (hoặc trong `assistFamilies`) trong `assistRadius` và đang `IDLE`/`PATROL` cũng vào chiến đấu với cùng mục tiêu.

### 5.4. Dây xích và reset
- <a id="en-r-050"></a>**EN-R-050** Mỗi quái có điểm neo (điểm spawn). Nếu mục tiêu ra ngoài `leashRadius` tính từ điểm neo (mặc định thường 30m · tinh anh 40m · boss = bán kính đấu trường) **hoặc** không có mục tiêu hợp lệ trong `params.noTargetResetSec` (8s) → `RETURNING`.
- <a id="en-r-051"></a>**EN-R-051** Về tới điểm neo: hồi đầy HP, poise; xóa bảng threat. Không ai nhận tu vi / rơi đồ từ quái đã reset.
- <a id="en-r-052"></a>**EN-R-052** Boss reset khi mọi người chơi trong đấu trường chết / trọng thương / rời đấu trường: về pha đầu, hồi đầy, hủy vật triệu hồi.

### 5.5. Hành vi chiến đấu
- <a id="en-r-060"></a>**EN-R-060** Mỗi lần ra quyết định khi rảnh (đã qua `attackIntervalSec`):
  1. Tính điểm hữu dụng mỗi chiêu (§6.4); chọn ngẫu nhiên có trọng số trong các chiêu điểm > 0 (RNG seed theo `MapInstance`).
  2. Không chiêu nào khả dụng ở khoảng cách hiện tại → di chuyển tới khoảng cách của chiêu có trọng số cao nhất đã hết hồi chiêu; không có → dùng `basicSkillId` nếu trong tầm, ngược lại tiến lại gần.
  3. Sau hành động, với xác suất `repositionChance` di chuyển tới vị trí mới trong `preferredRange` (đi vòng sang bên, không lùi thẳng).
- <a id="en-r-061"></a>**EN-R-061** Tham số archetype mặc định (`ai_archetypes.json`):

| Archetype | `preferredRange` | `attackIntervalSec` | `repositionChance` | `dodgeChance` | Đặc điểm | `meleeToken` |
|-----------|:---:|:---:|:---:|:---:|----------|:---:|
| `MELEE_BRUISER` | 0–2,5m | 1,4–2,2 | 0,1 | 0 | Áp sát, đòn nặng, giáp `LIGHT` khi tụ chiêu | ✅ |
| `MELEE_SKIRMISHER` | 0–3m | 0,9–1,5 | 0,5 | 0,15 | Đánh rồi lùi sang bên, lướt vào | ✅ |
| `RANGED_KITER` | 7–11m | 1,2–1,8 | 0,4 | 0,1 | Lùi khi người chơi < 5m (`retreatBelowHpPct` không áp) | ❌ |
| `CASTER` | 8–12m | 1,8–2,6 | 0,3 | 0 | Chiêu vùng chậm, cảnh báo dài | ❌ |
| `SWARM` | 0–2m | 1,6–2,4 | 0,2 | 0 | Máu thấp, đi thành bầy, tản quanh mục tiêu | ✅ |
| `AMBUSHER` | 0–2,5m | 1,0–1,6 | 0,3 | 0,1 | Ẩn thân tới khi người chơi < 4m hoặc bị quét; đòn mở màn có cảnh báo 0,6s | ✅ |
| `TANK` | 0–2,5m | 1,8–2,6 | 0 | 0 | Chậm, poise ×2, khiên định kỳ | ✅ |
| `PASSIVE` | — | 2,0–3,0 | 0 | 0 | Không chủ động; bị đánh thì chạy (`retreatBelowHpPct` 100) hoặc phản đòn yếu | ❌ |
| `BOSS_SCRIPTED` | theo pha | theo pha | 0,2 | 0 | Kịch bản pha §5.8 | ❌ |

  Tinh anh: `attackIntervalSec` ×0,8, `dodgeChance` +0,15 (trần 0,3). Boss không né.
- <a id="en-r-062"></a>**EN-R-062** **Né vùng cảnh báo của người chơi** (GD-07 SK-R-081): khi một `GROUND_CIRCLE` của người chơi phủ lên quái, quái roll `dodgeChance` một lần cho vùng đó; trúng thì đi ra khỏi vùng theo hướng gần nhất (không i-frame).

### 5.6. Cảnh báo chiêu (telegraph)
- <a id="en-r-070"></a>**EN-R-070** Mọi chiêu của quái có `telegraphForOthers = true`. Thời gian cảnh báo = `windupSec` (+ `delaySec` của `GROUND_CIRCLE`) phải ≥:

| Hạng | Chiêu thường | Chiêu mạnh (hệ số ≥ 2,0 hoặc gây `KNOCKDOWN` / `STUN`) | Chiêu không thể đỡ |
|------|:---:|:---:|:---:|
| `NORMAL` | 0,4s | 0,6s | 0,8s |
| `ELITE` | 0,5s | 0,7s | 0,8s |
| `BOSS` | 0,5s | 0,8s | 1,0s (cơ chế toàn đấu trường: 2,0s) |

- <a id="en-r-071"></a>**EN-R-071** Chiêu không thể đỡ (`unblockable`) có thêm hiệu ứng lóe đỏ trên người quái lúc bắt đầu windup.
- <a id="en-r-072"></a>**EN-R-072** Đòn cơ bản (`basicSkillId`) của quái có cảnh báo dạng "rung vũ khí" (animation), vẫn tuân thủ thời gian tối thiểu "Chiêu thường".

### 5.7. Bao vây công bằng
- <a id="en-r-080"></a>**EN-R-080** **Suất cận chiến:** mỗi người chơi có `params.meleeTokensPerPlayer` (3) suất. Quái có `meleeToken = true` phải giữ một suất để được tấn công cận chiến người đó; quái không có suất đi vòng quanh ở 3–5m, chờ suất trống. Boss và vật triệu hồi của boss không cần suất.
- <a id="en-r-081"></a>**EN-R-081** Tổng số quái đồng thời **đang windup** chiêu nhắm vào một người chơi ≤ `params.maxSimultaneousWindupsPerTarget` (2) đối với quái thường; quái thứ 3 trở đi chờ.

### 5.8. Boss
- <a id="en-r-090"></a>**EN-R-090** Boss có 2–3 pha theo HP (`enterAtHpPct`). Khi HP chạm ngưỡng: hủy chiêu đang windup (nếu chưa tới lượt trúng), thực hiện `onEnter` theo thứ tự, đổi danh sách chiêu. Phát `S2C_BossPhase`.
- <a id="en-r-091"></a>**EN-R-091** Danh mục cơ chế boss dùng được (dữ liệu, không code riêng): vùng mặt đất, lao thẳng, quét hình quạt, mưa đạn, gọi quái phụ (`SUMMON`, có thể nối poise: quái phụ chết làm boss mất `linkPoisePct` poise), khiên phá bằng poise (`SHIELD.breakByPoise`), bất tử tới khi quái phụ chết, vùng an toàn (chiêu `SELF_CIRCLE` toàn đấu trường + `SUMMON` vật đánh dấu "vùng an toàn" miễn sát thương).
- <a id="en-r-092"></a>**EN-R-092** **Cuồng nộ:** sau `enrage.afterSec` (mặc định 300s), mỗi `stepSec` (30s) boss +`damageBonusPerStep` (10 điểm % `damageBonus`). Không có "chết ngay lập tức".
- <a id="en-r-093"></a>**EN-R-093** **Theo số người:** khi boss vào chiến đấu, đếm `n` = số người chơi trong đấu trường (tối đa 5); `maxHp` nhân theo §6.3; sát thương boss không đổi. `n` chốt tại thời điểm vào chiến đấu.
- <a id="en-r-094"></a>**EN-R-094** Phá thế boss là "cửa sổ sát thương" chính: thiết kế mỗi pha có ít nhất một chiêu sau đó boss **đứng hở** ≥ 1,5s (recovery dài) để người chơi dồn poise.

### 5.9. Hạ quái: tính công, tu vi, rơi đồ, Vạn Giới Lục
- <a id="en-r-100"></a>**EN-R-100** **Người được tính công** (mỗi người nhận **trọn** phần của mình, không chia): người chơi gây ≥ `params.creditMinDamagePct` (5%) `maxHp` của quái, **hoặc** đã hồi máu cho người được tính công trong lúc đánh quái, **hoặc** là thành viên tổ đội của người được tính công và ở trong 40m lúc quái chết. Người chơi `lifeState ≠ ALIVE` không được tính.
- <a id="en-r-101"></a>**EN-R-101** Với mỗi người được tính công, Game Server:
  1. Thêm vào lô báo cáo `monster-kills` (GD-04 CU-R-050) với `monsterRealmCode`, `baseExp`, `rank`.
  2. Roll bảng rơi đồ riêng (§6.5) và tạo **túi đồ cá nhân** tại xác quái, chỉ người đó thấy, tồn tại `params.lootBagSec` (120s); tùy chọn tự nhặt.
  3. Phát cơ hội `KILL` cho GD-22.
  4. Cộng tiến độ Vạn Giới Lục mục Yêu thú (`codexId`, theo tài khoản; mốc 1 / 10 / 50, GD-21 §3.6).
- <a id="en-r-102"></a>**EN-R-102** Nhặt túi đồ: vật phẩm vào `BAG` (ngoài hiểm địa) hoặc `DANGER_BAG` (trong hiểm địa, GD-28); túi đầy thì phần dư ở lại túi đồ tới khi hết hạn. Ghi `item_log` lý do `MONSTER_DROP`.
- <a id="en-r-103"></a>**EN-R-103** Quái spawn bởi `SpawnGroupDef` có `grantExp = false` / `grantLoot = false` thì bỏ bước tương ứng.

### 5.10. Quái do cơ duyên sinh ra (GD-22)
- <a id="en-r-110"></a>**EN-R-110** `START_COMBAT(spawnGroupKey)` tạo quái theo `SpawnGroupDef`. `ownerOnly = true`: quái chỉ nhắm / bị nhắm bởi chủ phiên; người chơi khác thấy quái mờ, không gây sát thương, không bị tấn công.
- <a id="en-r-111"></a>**EN-R-111** Dây xích của nhóm quái này là `params.encounterLeashRadius` (40m) tính từ vị trí tín hiệu; chủ phiên vượt quá → quái tan biến, GD-22 nhận `combat_result = "FLED"`. Mọi quái trong nhóm chết → `"WIN"`; chủ phiên trọng thương → `"LOSE"`.

### 5.11. Môi trường: giờ, thời tiết
- <a id="en-r-120"></a>**EN-R-120** Ban đêm: quái `nightStatBonusPct` mạnh lên (EN-R-021); bảng spawn ưu tiên mục `dayPhases: [NIGHT]` (quái dạ hành, GD-21 §3.8).
- <a id="en-r-121"></a>**EN-R-121** Giông bão: quái có `weatherActive: [STORM]` (vd. hệ Lôi) xuất hiện. Sương mù: quái `AMBUSHER` được +1 `hiddenLevel` (trần 5).

### 5.12. Quái ẩn thân
- <a id="en-r-130"></a>**EN-R-130** Quái có `hiddenLevel > 0` **không được gửi xuống client** của người chơi chưa phát hiện (GD-21 §3.7). Được phát hiện khi: thần thức quét với `senseLevel ≥ hiddenLevel`, người chơi tới gần ≤ 4m, hoặc quái tấn công / bị trúng đòn.
- <a id="en-r-131"></a>**EN-R-131** Quái ẩn thân khi lộ diện tấn công vẫn phải tuân thủ cảnh báo (EN-R-070), với đòn mở màn ≥ 0,6s.

### 5.13. Hiểm địa, hấp hối, quy tức (tham chiếu GD-28)
- <a id="en-r-140"></a>**EN-R-140** Quái có `alertOnScan = true` (thường là tinh anh / boss lang thang trong hiểm địa): khi người chơi dùng thần thức quét trong `scanRadius × alertRadiusMult` (GD-21: 1,5), roll xác suất kinh động (GD-28); trúng thì quái chuyển `ALERT` hướng về vị trí người quét.
- <a id="en-r-141"></a>**EN-R-141** Quái **không tấn công người chơi `DOWNED`** (hấp hối); chuyển sang mục tiêu khác hoặc đứng canh. Thời gian hấp hối do GD-28 quyết định.
- <a id="en-r-142"></a>**EN-R-142** Người chơi ở trạng thái quy tức (mất kết nối, GD-28): threat của họ giảm về 0 trong `params.dormantThreatDecaySec` (10s); quái chỉ tấn công nếu không còn mục tiêu nào khác và người đó đang trong tầm đánh cơ bản.

---

## 6. Công thức

### 6.1. Chỉ số quái
```
stat = round(base_stats[realmCode][stat] × rankMult[rank][stat] × statMult[stat])
```
Ví dụ `MON_HUNGRY_WOLF` (`LK6`, `NORMAL`, `statMult` mặc định): `maxHp = round(322 × 0,6) = 193`, `attack = round(32 × 0,8) = 26` (322, 32 là cơ bản `LK6` theo GD-06 §6.2).

### 6.2. Thời gian cảnh báo
```
telegraphSec = windupSec + (shape.type == GROUND_CIRCLE ? delaySec : 0)
```

### 6.3. HP boss theo số người
```
maxHp_boss = maxHp × (1 + partyHpScaling × (n − 1)),   partyHpScaling mặc định 0,6,  n ∈ [1, 5]
```
Ví dụ `MON_B_FOX_MATRIARCH` (`LK7`, cơ bản 354): `354 × 20 = 7.080`; tổ đội 3 người → `7.080 × 2,2 = 15.576`.

### 6.4. Điểm hữu dụng của chiêu
```
utility = weight
        × (minRange ≤ d ≤ maxRange ? 1 : 0)
        × (hết hồi chiêu ? 1 : 0)
        × (điều kiện thỏa ? 1 : 0)
        × (targetsInRangeMin thỏa ? 1 : 0)
d = khoảng cách tới mục tiêu
```

### 6.5. Roll rơi đồ
```
for i in 1..rolls:
  w_nothing = nothingWeight
  w_e = weight_e × clamp((luck / 50) ^ rarityExp[rarity_e], 0,5, 2,5)     cho mỗi entry thỏa điều kiện
  chọn theo trọng số; entry là bảng lồng thì roll đệ quy 1 lần
  qty = randInt(qty[0], qty[1])
cộng các entry guaranteed
rarityExp: COMMON 0 · UNCOMMON 0,25 · RARE 0,5 · EPIC 0,75 · LEGENDARY 1,0
```
`luck` là khí vận **cuối** của người chơi (GD-27). Ví dụ `LT_V1_WOLF` (nothing 40, COMMON 45, UNCOMMON 12, RARE 3), khí vận 75:
- UNCOMMON: `12 × 1,5^0,25 = 13,28`; RARE: `3 × 1,5^0,5 = 3,67`
- Tổng `40 + 45 + 13,28 + 3,67 = 101,95` → P(RARE) ≈ 3,60% (khí vận 50: 3,00%).

### 6.6. Kiểm tra nhanh thời gian hạ quái (đối chiếu GD-06 §8)
Người chơi `LK6`, `attack` 32 (không trang bị), đánh thường kiếm (hệ số trung bình 0,8) vào `MON_HUNGRY_WOLF` (`defense` = round(16 × 0,8) = 13):
`mitigation = 13 / (13 + 5 × 16) = 0,14` → `0,8 × 32 × 0,86 ≈ 22`/đòn → ~9 đòn đánh thường, ~4–5 đòn khi xen kỹ năng (hệ số 1,6–2,2). Với trang bị chuẩn (+30% công) đạt mục tiêu 3–6 đòn.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/monsters/params.json` | Tham số |
| `shared/gamedata/monsters/monsters.json` | `MonsterDef[]` |
| `shared/gamedata/monsters/bosses.json` | `BossDef[]` |
| `shared/gamedata/monsters/ai_archetypes.json` | `ArchetypeParams` theo archetype (§5.5) |
| `shared/gamedata/monsters/spawn_tables.json` | `SpawnTable[]` (`spawnPoints` lấy từ dữ liệu bản đồ, TD-13) |
| `shared/gamedata/monsters/elite_spawns.json` | `EliteSpawnDef[]` |
| `shared/gamedata/monsters/spawn_groups.json` | `SpawnGroupDef[]` (GD-22) |
| `shared/gamedata/monsters/loot_tables.json` | `LootTable[]` |
| `shared/gamedata/skills/skills.json` | Chiêu của quái (ID `SKL_MON_*`), cùng định dạng GD-07 |

```jsonc
// params.json
{
  "aiDecisionHz": 5, "dormantRadius": 80, "maxActiveMonstersPerInstance": 300,
  "spawnMinPlayerDistance": 15, "spawnRetrySec": 5, "spawnFadeInSec": 0.8, "despawnFadeSec": 10,
  "alertSec": 0.5, "noTargetResetSec": 8,
  "defaultLeash": { "NORMAL": 30, "ELITE": 40 },
  "threat": { "perDamage": 1, "perHealing": 0.5, "perfectParry": 50, "initial": 10,
              "switchMeleePct": 1.10, "switchRangedPct": 1.30, "decayOutOfSightPctPerSec": 0.05 },
  "eliteIntervalMult": 0.8, "eliteDodgeBonus": 0.15, "eliteDodgeCap": 0.3,
  "telegraphMin": {
    "NORMAL": { "normal": 0.4, "heavy": 0.6, "unblockable": 0.8 },
    "ELITE":  { "normal": 0.5, "heavy": 0.7, "unblockable": 0.8 },
    "BOSS":   { "normal": 0.5, "heavy": 0.8, "unblockable": 1.0, "arenaWide": 2.0 } },
  "heavyCoefficientThreshold": 2.0,
  "meleeTokensPerPlayer": 3, "maxSimultaneousWindupsPerTarget": 2,
  "ambusherRevealDistance": 4, "ambusherOpenerTelegraph": 0.6,
  "bossPartyHpScaling": 0.6, "bossMaxPartyCount": 5,
  "enrage": { "afterSec": 300, "stepSec": 30, "damageBonusPerStep": 10 },
  "creditMinDamagePct": 0.05, "creditPartyRadius": 40,
  "lootBagSec": 120, "corpseSec": 3,
  "lootRarityExp": { "COMMON": 0, "UNCOMMON": 0.25, "RARE": 0.5, "EPIC": 0.75, "LEGENDARY": 1.0 },
  "lootLuckMin": 0.5, "lootLuckMax": 2.5,
  "encounterLeashRadius": 40, "dormantThreatDecaySec": 10
}
```

```jsonc
// monsters.json (trích)
[
  { "id": "MON_HUNGRY_WOLF", "nameKey": "mon.hungry_wolf", "codexId": "CDX_MON_HUNGRY_WOLF",
    "family": "BEAST", "skeleton": "QUADRUPED", "modelId": "MDL_WOLF", "tint": "#7a6a58",
    "rank": "NORMAL", "realmCode": "LK6", "elementAffinity": "KIM", "statMult": {},
    "baseExp": 6, "archetype": "MELEE_SKIRMISHER",
    "perception": { "sightRadius": 14, "fovDeg": 140, "hearingRadius": 10, "alertOnScan": false },
    "aggressive": true, "social": { "assistRadius": 12 },
    "basicSkillId": "SKL_MON_WOLF_BITE",
    "skills": [ { "skillId": "SKL_MON_WOLF_POUNCE", "weight": 40, "minRange": 3, "maxRange": 7 } ],
    "leashRadius": 30, "hiddenLevel": 0, "activeTime": "ALWAYS", "nightStatBonusPct": 0.15,
    "lootTableId": "LT_V1_WOLF", "tags": ["pack", "beast"] }
]
```

```jsonc
// skills.json — chiêu quái (trích)
{ "id": "SKL_MON_WOLF_POUNCE", "nameKey": "skl.mon.wolf_pounce", "descKey": "",
  "grade": { "tier": "HOANG", "sub": "HA" }, "weaponReq": "ANY", "isUltimate": false, "realmMin": "PN1",
  "tags": ["monster"], "shape": { "type": "DASH", "distance": 6, "width": 1.2, "iframe": false },
  "castTimeSec": 0, "windupSec": 0.6, "recoverySec": 0.8, "cancelAfterSec": 0.8,
  "movableCast": false, "armorDuringCast": "LIGHT",
  "hits": [ { "atSec": 0, "target": "ENEMIES", "maxTargets": 1,
              "damage": { "damageType": "PHYSICAL", "element": "KIM", "coefficient": 1.6 },
              "poiseDamage": 25, "reaction": "KNOCKBACK", "knockbackDistance": 1.5 } ],
  "costs": { "mpPctOfBase": 0 }, "cooldownSec": 6, "perfection": { "type": "COEFFICIENT_PCT", "value": 0 },
  "telegraphForOthers": true }
```

```jsonc
// loot_tables.json (trích)
{ "id": "LT_V1_WOLF", "rolls": 1, "nothingWeight": 40,
  "entries": [ { "itemId": "ITM_WOLF_PELT", "weight": 45, "qty": [1, 2], "rarity": "COMMON" },
               { "itemId": "ITM_WOLF_FANG", "weight": 12, "qty": [1, 1], "rarity": "UNCOMMON" },
               { "itemId": "ITM_LINH_THU_HUYET", "weight": 3, "qty": [1, 1], "rarity": "RARE" } ],
  "guaranteed": [ { "currency": "SPIRIT_STONE", "weight": 1, "qty": [2, 4], "rarity": "COMMON" } ] }   // theo GD-13 §6.3 (LK6 thường: trung bình 2,8)
```

```jsonc
// spawn_groups.json — dùng bởi ENC_V1_THUONG_HO (GD-22 §7.3)
{ "id": "SG_V1_HUNGRY_WOLVES", "entries": [ { "monsterId": "MON_HUNGRY_WOLF", "count": 3 } ],
  "pattern": "AT_CUE", "radius": 6, "ownerOnly": true, "grantExp": true, "grantLoot": false }
```

### 7.1. Bestiary Vực 1 (MVP)

| ID | Tên | Bản đồ | Cảnh giới | Họ / Khung | Archetype | Hệ | Đặc điểm |
|----|-----|--------|-----------|------------|-----------|----|----------|
| `MON_BAMBOO_RAT` | Trúc Thử | F01 | LK1 | BEAST / QUADRUPED | SWARM | MOC | Bầy 3–5 |
| `MON_GREEN_SNAKE` | Thanh Xà | F01 | LK2 | BEAST / SERPENT | AMBUSHER (ẩn 1) | MOC | `ST_POISON` |
| `MON_WILD_BOAR` | Dã Trư | F01 | LK3 | BEAST / QUADRUPED | MELEE_BRUISER | THO | Lao thẳng KNOCKDOWN |
| `MON_RIVER_CRAB` | Giáp Giải | F02 | LK3 | BEAST / NONE | TANK | THUY | Khiên mai cua |
| `MON_WATER_SPRITE` | Thủy Tinh Linh | F02 | LK4 | SPIRIT / NONE | CASTER | THUY | Chỉ khi `RAIN` |
| `MON_MINE_BAT` | Khoáng Dơi | F03 | LK4 | BEAST / BIRD | SWARM | PHONG | Bay thành đàn, chỉ trong hầm |
| `MON_BANDIT_MINER` | Khoáng Tặc | F03 | LK5 | HUMANOID / HUMANOID | MELEE_SKIRMISHER | KIM | Người, rơi linh thạch nhiều hơn |
| `MON_STONE_PUPPET` | Thạch Khôi | F03 | LK5 | CONSTRUCT / HUMANOID | TANK | THO | Poise ×2 |
| `MON_POISON_TOAD` | Độc Thiềm | F04 | LK5 | BEAST / NONE | RANGED_KITER | MOC | Phun độc tầm xa |
| `MON_FOG_WRAITH` | Vụ Quỷ | F04 | LK6 | SPIRIT / HUMANOID | AMBUSHER (ẩn 2) | THUY | Mạnh hơn khi sương |
| `MON_RED_FOX` | Xích Hồ | F05 | LK6 | BEAST / QUADRUPED | MELEE_SKIRMISHER | HOA | Né nhiều |
| `MON_HUNGRY_WOLF` | Ngạ Lang | F05 | LK6 | BEAST / QUADRUPED | MELEE_SKIRMISHER | KIM | Bầy 2–4; dùng cho cơ duyên |
| `MON_NIGHT_OWL` | Dạ Kiêu Yêu | F05 | LK7 | DEMON / BIRD | RANGED_KITER | PHONG | Chỉ `NIGHT` |
| `MON_BLOOD_CROW` | Huyết Nha | F06 | LK7 | DEMON / BIRD | SWARM | HOA | |
| `MON_RESENTFUL_SOLDIER` | Oán Binh | F06 | LK8 | UNDEAD / HUMANOID | MELEE_BRUISER | KIM | +15% ban đêm |
| `MON_GHOST_ARCHER` | Quỷ Cung Thủ | F06 | LK8 | UNDEAD / HUMANOID | RANGED_KITER | KIM | Tên xuyên 1 mục tiêu |

**Tinh anh định kỳ:**

| ID | Tên | Bản đồ | Cảnh giới | Ghi chú |
|----|-----|--------|-----------|---------|
| `MON_E_BAMBOO_KING` | Trúc Vương Thử | F01 | LK3 | Gọi bầy Trúc Thử |
| `MON_E_STONE_KING` | Thạch Khôi Vương | F03 | LK6 | Khiên phá bằng poise |
| `MON_E_BLOOD_WOLF` | Huyết Lang Vương | F05 | LK7 | Chỉ ban đêm; hú gọi Ngạ Lang |
| `MON_E_FALLEN_GENERAL` | Tàn Tướng | F06 | LK9 | Rơi nguyên liệu phụ của Trúc Cơ Đan |

**Boss bí cảnh:**

| ID | Tên | Bí cảnh | Cảnh giới | Số người | Pha |
|----|-----|---------|-----------|:---:|-----|
| `MON_B_MINE_TYRANT` | Khoáng Ma Bạo Chúa | BC01 Phế Khoáng Động | LK5 | 1 | 2 |
| `MON_B_FOX_MATRIARCH` | Hồ Yêu Mẫu | BC02 Hồ Yêu Động | LK7 | 1–5 | 3 |
| `MON_B_SWORD_SPIRIT` | Kiếm Hồn | BC03 Tàn Kiếm Trủng | LK9 | 1–5 | 3 |
| `MON_B_REMNANT` | Tàn hồn (mẫu động) | Tàn Hồn Cảnh (GD-26 §5.15) | theo tầng | 1 | 2 |
| `MON_CAVE_WARDEN` | Thạch Hộ Pháp | Cửa động phủ (GD-30 HM-R-045) | theo cửa động | 1 | 1 |
| `MON_B_HEAVEN_AVATAR` | Thiên Đạo Hóa Thân | Dã ngoại, truy sát người mang Thiên phạt (GD-32 PK-R-055) | người bị phạt +1 cảnh giới lớn | 1–5 | 3 |

**Chấp Pháp** (GD-32 PK-R-051): `MON_E_ENFORCER` (tinh anh, HUMANOID, cảnh giới động = ác nhân +2 tầng, máu ×3, công ×1,5, poise ×2) và `MON_E_ENFORCER_ELDER` (+1 cảnh giới lớn, máu ×5, chiêu không thể đỡ); chỉ nhắm ác nhân và người giúp ác nhân; rơi `LT_ENFORCER*`.

**Thủ hộ linh thảo** (GD-21 OW-R-103): nhóm spawn `SG_HERB_GUARDIAN_{TIER}` gồm 1 quái thường cùng họ với vùng, chỉ số ×2,5, `leashRadius` 15m quanh điểm linh thảo, không rơi linh thảo (phần thưởng là quyền hái).

### 7.2. Ví dụ boss đầy đủ: `MON_B_FOX_MATRIARCH`

| Pha | Ngưỡng | Vào pha | Chiêu (trọng số) |
|-----|:---:|---------|------------------|
| P1 | 100% | `ANNOUNCE` "Kẻ nào dám xông vào hang ta?" | Hồ Trảo liên kích: quạt 3m / 120°, 3 nhát, cảnh báo 0,5s (40) · Hồ Hỏa Cầu: 3 đạn quạt 30°, cảnh báo 0,6s (35) · Mị Ảnh Lướt: lao 10m, rộng 1,5m, KNOCKDOWN, cảnh báo 0,8s, sau đó đứng hở 1,8s (25) |
| P2 | 60% | `SUMMON` 2 Hồ Ảnh (`MON_FOX_ILLUSION`, mỗi con chết làm boss mất 50% poise) · `ANNOUNCE` | Như P1 + Cửu Vĩ Hỏa Vũ: 5 vùng tròn r 2,5 ngẫu nhiên quanh người chơi, cảnh báo 1,0s, `ST_BURN` (30) |
| P3 | 25% | `MOVE_TO` tâm đấu trường · `CAST` Hồ Hỏa Bạo | Hồ Hỏa Bạo: toàn đấu trường trừ 3 vòng an toàn, **không thể đỡ**, cảnh báo 2,0s, mỗi 20s (bắt buộc) · các chiêu P2 với khoảng nghỉ ×0,8 |

**Boss đặc biệt (thu phục được, GD-25 §5.2b):** lần đầu HP ≤ 10% → **Kiệt Sức** 8s (đứng yên, không đánh, không nhận sát thương, `S2C_BossPhase { phase: 'EXHAUSTED' }`) để người chơi ném Ngự Thú Phù; không ai thu phục → hồi 5% HP, tiếp tục P3.

Cuồng nộ sau 300s; `partyHpScaling` 0,6. Mọi chiêu có thời gian cảnh báo đạt EN-R-070.

**Validator (CI):** ID đúng mẫu `MON_*` / `MON_E_*` / `MON_B_*` khớp `rank`; mọi `skillId`, `lootTableId`, `codexId`, `monsterId` trong spawn / boss tồn tại; **cảnh báo tối thiểu EN-R-070** cho mọi chiêu quái (§6.2); `telegraphForOthers = true` với mọi chiêu quái; `SpawnTable.areaId` tồn tại trong dữ liệu bản đồ; không `spawnPoints` nào nằm trong vùng đệm an toàn; mỗi pha boss có ít nhất một chiêu `recoverySec ≥ 1,5` (EN-R-094); bảng rơi đồ lồng nhau không tạo vòng lặp.

---

## 8. Mục tiêu cân bằng
Theo GD-06 §8 (thời gian hạ quái thường 3–6 đòn, tinh anh 20–40s solo, boss bí cảnh 2–4 phút, quái thường đánh trúng 5–8% HP). Bổ sung:

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Tỷ lệ chết (trọng thương) mỗi giờ ở bản đồ đúng cảnh giới | 0,3–0,8 lần |
| Tỷ lệ hạ boss bí cảnh ở lần thử đầu (đúng cảnh giới, trang bị chuẩn) | 40–60% |
| Số quái đánh cùng lúc trong tình huống bình thường | 1–3 |

---

## 9. Hợp đồng giao tiếp

### 9.1. API nội bộ (Game Server → API Service)

| Endpoint | Request | Response | Ghi chú |
|----------|---------|----------|---------|
| `POST /internal/v1/cultivation/monster-kills` | (GD-04 §9.1) | | Tu vi |
| `POST /internal/v1/loot/grant` | `{ grantId, characterId, source: 'MONSTER' \| 'BOSS', refId, rng: { seed, kind: 'MONSTER_LOOT' } (TD-01 §9), items: [{ itemId, qty }], currency: [{ code, amount }], toLocation: 'BAG' \| 'DANGER_BAG' }` | `{ granted, overflow }` | Khi người chơi nhặt túi đồ; idempotent theo `grantId`; inventory chi tiết ở GD-09 |
| `POST /internal/v1/codex/progress` | `{ batchId, accountId, entries: [{ codexId, kills }] }` | `{ unlocked: [{ codexId, level }] }` | GD-21 §3.6; gom mỗi 60s |

### 9.2. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `S2C_EntitySpawn` | S→C | `{ entityId, kind: 'MONSTER', monsterId, rank, realmCode, position, facing, hpPct, ownerOnlyFor? }` |
| `S2C_EntityState` | S→C | `{ entityId, aiState: 'IDLE' \| 'ALERT' \| 'COMBAT' \| 'RETURNING' \| 'FLEEING', targetId? }` |
| `S2C_EntityDespawn` | S→C | `{ entityId, reason: 'DEAD' \| 'FADE' \| 'OUT_OF_AOI' }` |
| `S2C_LootDrop` | S→C (chỉ chủ túi) | `{ lootId, position, items: [{ itemId, qty, rarity }], expiresInSec }` |
| `C2S_PickupLoot` | C→S | `{ lootId, itemIndexes?: number[] }` |
| `S2C_BossPhase` | S→C (đấu trường) | `{ entityId, phaseId, announceKey? }` |
| `S2C_EliteAnnounce` | S→C (bản đồ) | `{ eliteSpawnId, monsterId, position, inSec }` |

Chiêu và cảnh báo của quái dùng `S2C_SkillCast` (GD-07 §9.3); sát thương dùng `S2C_HitResults` (GD-06 §9.3).

### 9.3. Sự kiện domain

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `monster.killed` | `{ monsterId, rank, mapId, channel, creditedCharacterIds }` | Telemetry, nhiệm vụ (GD-15), sự kiện thế giới (GD-23) |
| `monster.elite_spawned` | `{ eliteSpawnId, mapId, channel }` | Telemetry |
| `boss.defeated` | `{ monsterId, dungeonId, partyCharacterIds, durationSec, attempt }` | Telemetry, Bia Vạn Cổ (lần đầu hạ boss) |

---

## 10. Lưu trữ
Quái không lưu DB (trạng thái nằm trong RAM `MapInstance`; khởi động lại thì spawn lại từ đầu, hẹn giờ tinh anh random lại). Lưu:
- `item_log` lý do `MONSTER_DROP` / `BOSS_DROP` (khi nhặt).
- Tiến độ Vạn Giới Lục ở `codex_entry` (GD-21 §7).
- Nhật ký hạ boss (telemetry, không cần cho gameplay MVP):

```sql
CREATE TABLE boss_kill_log (
  id BIGSERIAL PRIMARY KEY, monster_id VARCHAR(48) NOT NULL, map_id VARCHAR(32) NOT NULL,
  party JSONB NOT NULL, duration_sec INT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

---

## 11. UI/UX
- **Bảng tên quái:** tên + cảnh giới; màu theo chênh lệch `stageIndex` (GD-04 §6.2): xám (thấp hơn ≥ 3: không cho tu vi), trắng (±0), vàng (cao hơn 1–2), đỏ (cao hơn ≥ 3), đỏ đậm + "??" (cao hơn một cảnh giới lớn).
- **Khung hạng:** tinh anh viền bạc + thanh poise; boss thanh máu lớn ở trên màn hình + thanh poise + tên pha.
- **Biểu tượng "!"** khi quái phát hiện; **"?"** khi mất dấu (bắt đầu đếm reset).
- **Cảnh báo:** vùng đỏ lấp dần theo thời gian cảnh báo; không thể đỡ: viền đỏ đậm + lóe đỏ trên quái.
- **Túi đồ:** cột sáng theo độ hiếm cao nhất (trắng / xanh lá / xanh dương / tím / vàng); chỉ người sở hữu thấy.
- **Tinh anh sắp xuất hiện:** biểu tượng đếm ngược trên minimap 60s.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-06 | Chỉ số, sát thương, poise, status, hệ số hạng |
| GD-07 | Chiêu quái là `SkillDef`; quái né vùng cảnh báo; máy trạng thái hành động |
| GD-04 | Tu vi theo `baseExp`, `rank`, chênh cảnh giới, mệt mỏi |
| GD-21 | Spawn theo khu, giờ, thời tiết; vùng đệm; ẩn thân; Vạn Giới Lục |
| GD-22 | `spawnGroups`, trigger `KILL`, quái cá nhân, `combat_result` |
| GD-26 | Hết máu do quái → trọng thương (ngoài hiểm địa) / chết thật (trong hiểm địa) |
| GD-27 | Khí vận ảnh hưởng rơi đồ hiếm |
| GD-28 | Quái hiểm địa, kinh động khi quét, hấp hối, quy tức, `DANGER_BAG` |
| GD-09 | Vật phẩm rơi, inventory |
| GD-23 | Thú triều, world boss dùng cùng `MonsterDef` / `BossDef` |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Người chơi chạy vào vùng đệm an toàn khi bị đuổi | Quái dừng ở mép vùng đệm, mất mục tiêu sau 3s, `RETURNING` nếu không còn ai |
| Kéo quái về đông người cho người khác đánh | Người khác được tính công nếu đạt EN-R-100; không ai mất phần (rơi đồ cá nhân) |
| Hai tổ đội cùng đánh một tinh anh | Mỗi người đạt điều kiện đều được tính công và roll riêng |
| Quái bị kẹt địa hình không tới được mục tiêu | Sau 3s không tiến được: dùng chiêu tầm xa nếu có; không có → `RETURNING` (chống "đánh quái kẹt" lấy tu vi miễn phí) |
| Mục tiêu đứng trên chỗ quái không đi tới (mép vách) | Như trên |
| Boss đổi pha giữa windup | Hủy chiêu nếu lượt trúng chưa xảy ra (EN-R-090) |
| Người chơi đăng xuất giữa trận boss | Mất khỏi đấu trường; `n` không giảm (đã chốt); nếu không còn ai → reset |
| Người chơi trọng thương rồi quay lại đấu trường boss | Được vào lại nếu boss chưa reset (tức còn người khác đang đánh) |
| Quái dạ hành đang đánh lúc bình minh | Ở lại tới khi chết / reset rồi mới tan (EN-R-033) |
| Túi đồ hết hạn khi túi đồ người chơi đầy | Vật phẩm mất (đã cảnh báo khi nhặt) |

---

## 14. Telemetry

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `monster_kill` | monsterId, rank, ttkSec, hitsTaken, playerRealm | So với mục tiêu §8 |
| `severe_injury_by_monster` | monsterId, skillId, telegraphSec, playerPing | Chiêu nào gây chết nhiều, có do trễ mạng không |
| `boss_attempt` | monsterId, partySize, result, durationSec, phaseReached | Độ khó boss |
| `loot_drop` | lootTableId, rarity, luck | Kiểm chứng tỷ lệ rơi (§6.5) |
| `elite_contest` | eliteSpawnId, creditedPlayers | Tinh anh có đông người tụ tập như kỳ vọng |
| `monster_reset` | monsterId, reason | Dây xích có quá ngắn / dài, quái kẹt |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="en-ac-001"></a>EN-AC-001 | `MON_HUNGRY_WOLF` | Spawn | `maxHp = 193`, `attack = 26`, `maxPoise = 40` (§6.1) |
| EN-AC-002 | Boss `MON_B_FOX_MATRIARCH`, 3 người trong đấu trường | Vào chiến đấu | `maxHp = 15.576` (§6.3) |
| EN-AC-003 | Ví dụ §6.5, khí vận 75, 100.000 lần roll | Mô phỏng | P(RARE) = 3,60% ± 0,15% |
| EN-AC-004 | Mọi chiêu quái trong dữ liệu | Validator | Thời gian cảnh báo ≥ mức tối thiểu EN-R-070 |
| EN-AC-005 | Người chơi đứng cách điểm spawn 10m | Đến lượt spawn | Không spawn tại điểm đó (EN-R-032) |
| EN-AC-006 | Quái đuổi người chơi vào vùng đệm an toàn | Theo dõi | Quái không bước vào vùng đệm (EN-R-005) |
| EN-AC-007 | Người chơi kéo quái xa điểm neo 31m (quái thường) | Theo dõi | Quái `RETURNING`, bất tử, về tới nơi hồi đầy; không ai nhận tu vi / đồ |
| EN-AC-008 | 6 quái cận chiến tấn công một người chơi | Theo dõi | Tối đa 3 quái đánh cận chiến cùng lúc, tối đa 2 quái windup cùng lúc (EN-R-080, 081) |
| EN-AC-009 | Người chơi A gây 70% sát thương, B gây 4%, C (tổ đội của A, cách 30m) không đánh | Quái chết | A và C được tính công; B không (EN-R-100) |
| EN-AC-010 | A và B (khác tổ đội) đều gây ≥ 5% | Quái chết | Mỗi người có túi đồ riêng, chỉ mình thấy; cả hai nhận tu vi đầy đủ |
| EN-AC-011 | Quái ẩn thân `hiddenLevel = 2`, người chơi `senseLevel = 1` cách 8m | Gói tin AOI | Client không nhận `S2C_EntitySpawn` của quái; tới 4m hoặc quái tấn công thì nhận |
| EN-AC-012 | `MON_NIGHT_OWL` (`NIGHT`) | Trời sáng, quái không chiến đấu | Tan biến trong 10s; quái đang chiến đấu ở lại |
| EN-AC-013 | `MON_RESENTFUL_SOLDIER` ban đêm | `computeStats` | `attack` có modifier `ST_NIGHT_EMPOWER` +15% |
| EN-AC-014 | Phiên cơ duyên `ENC_V1_THUONG_HO` gọi `START_COMBAT("wolves")` | Spawn | 3 `MON_HUNGRY_WOLF` chỉ chủ phiên tương tác; người khác không gây sát thương được; chết hết → `combat_result = "WIN"`, không túi đồ (`grantLoot = false`) |
| EN-AC-015 | Như trên, chủ phiên chạy xa 41m | Theo dõi | Quái tan biến, `combat_result = "FLED"` |
| EN-AC-016 | Người chơi `DOWNED` trong hiểm địa, đồng đội còn sống | Quái đang đánh người `DOWNED` | Quái chuyển sang đồng đội (EN-R-141) |
| EN-AC-017 | Boss HP xuống 59% khi đang windup chiêu | Đổi pha | Chiêu bị hủy nếu chưa tới lượt trúng; `onEnter` P2 chạy (gọi 2 Hồ Ảnh) |
| EN-AC-018 | Giết 1 Hồ Ảnh ở P2 | Theo dõi | Boss mất 50% poise |
| EN-AC-019 | Trận boss kéo dài 360s | Theo dõi | Boss có `damageBonus` +20 điểm % (2 bước cuồng nộ) |
| EN-AC-020 | Tinh anh `MON_E_BLOOD_WOLF` sắp xuất hiện | 60s trước | Mọi người chơi trong bản đồ (kênh đó) nhận `S2C_EliteAnnounce` |
| EN-AC-021 | Người chơi gây sát thương, threat 100; người khác hồi máu 300 cho người đó | Bảng threat của quái | Người hồi máu có threat 150; quái cận chiến chỉ đổi mục tiêu khi vượt 110 |
| EN-AC-022 | Quái kẹt địa hình 3s không tới được mục tiêu, không có chiêu tầm xa | Theo dõi | `RETURNING` |
| EN-AC-023 | 10 phút ở bản đồ không có người chơi | Theo dõi | Quái không tick AI (ngủ); số quái vẫn được bổ sung đủ `maxAlive` |
| EN-AC-024 | Hạ 10 Ngạ Lang (tổng qua nhiều kiếp cùng tài khoản) | Vạn Giới Lục | Mục `CDX_MON_HUNGRY_WOLF` lên cấp 2 (hiện chỉ số, điểm yếu ngũ hành) |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 1 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Máy trạng thái AI, threat, dây xích, spawn theo khu | ✅ | | | |
| 3 quái thường + 1 tinh anh + 1 boss (thử nghiệm) | ✅ | | | |
| Suất cận chiến, giới hạn windup | ✅ | | | |
| Bestiary Vực 1 (16 + 4 tinh anh + 3 boss) | | ✅ | | |
| Spawn theo giờ / thời tiết, quái dạ hành, giờ Tý | | ✅ | | |
| Quái ẩn thân + thần thức | | ✅ | | |
| Rơi đồ cá nhân + khí vận, Vạn Giới Lục Yêu thú | | ✅ | | |
| Quái cơ duyên (`spawnGroups`) | | ✅ | | |
| Kinh động khi quét (hiểm địa) | | | ✅ | |
| World boss, thú triều (GD-23) | | | ✅ | |
| Vực 2 bestiary | | | ✅ | |
| Quái PvP-aware (Ma Vực) | | | | ✅ |

---

## 17. Câu hỏi mở
- [ ] **EN-Q-1:** Có cho tinh anh định kỳ rơi đồ **chung** (một lần cho người đóng góp nhiều nhất) để tạo tranh đua, hay giữ hoàn toàn cá nhân? *Mặc định: cá nhân.*
- [ ] **EN-Q-2:** Quái có nên "đứng canh" người hấp hối (gây áp lực cho đồng đội đến cứu) hay bỏ đi hẳn? *Mặc định: chuyển mục tiêu / đứng canh trong tầm.*
- [ ] **EN-Q-3:** Boss bí cảnh có khóa phần thưởng theo ngày (mỗi ngày nhận đồ boss một lần) để tránh cày lặp không? Liên quan kinh tế (GD-13).
- [ ] **EN-Q-4:** `partyHpScaling` 0,6 có làm đi 5 người quá dễ / quá khó? Đo `boss_attempt` theo `partySize`.
- [ ] **EN-Q-5:** Quái thường có cần chiêu "không thể đỡ" không, hay chỉ dành cho tinh anh / boss để giữ độ dễ chịu của dã ngoại?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-20 | — | Bản nháp đầu: bất biến, kiểu dữ liệu (quái, archetype, spawn, tinh anh, nhóm cơ duyên, boss, bảng rơi đồ), máy trạng thái AI, chỉ số, spawn, nhận biết & threat, dây xích, hành vi chiến đấu, cảnh báo tối thiểu, bao vây công bằng, boss, tính công & rơi đồ cá nhân, quái cơ duyên, môi trường, ẩn thân, hiểm địa, công thức + ví dụ, bestiary Vực 1, ví dụ boss, API, nghiệm thu |

