# GD-28 — Hiểm địa ☠

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-28 · Prefix ID: `DZ` |
| Owner | Game Designer + Content Designer |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 1 (thử nghiệm vào–lấy–thoát, hấp hối, chưa chết thật), Phase 3 (HD01, HD02 chết thật) |
| Quyết định nền | PLAN D-03, D-13, D-17 · PLAN §4.5 |
| Liên quan | [PLAN §4.5](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-12 Quái & AI](18-enemies-ai.md) · [GD-21 Open World](22-open-world.md) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-07 Kỹ năng](13-skills.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-22 Cơ duyên](23-encounters.md) · GD-09 Vật phẩm · GD-10 Luyện đan · TD-12 World & Map |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Dữ liệu ở `shared/gamedata/danger_zones/*.json` (§7).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-28 | Mục |
|--------------|-----------------|-----|
| GD-26 | Khi nào gửi `LethalEvent(DANGER_ZONE)`; "vật phẩm mang theo vào hiểm địa" là gì; di hài (`CORPSE`); quy tức; bảo hộ tân thủ trong hiểm địa | §5.5–5.8 |
| GD-12 | Thời gian hấp hối; quy tức; xác suất kinh động khi quét; tăng sức quái theo pha | §5.4–5.6, §5.10 |
| GD-07 | Trạng thái `DOWNED`, `DORMANT_GUARD` | §5.5, §5.7 |
| GD-21 | Mô hình instance, fog mỗi lượt, lối thoát, cửa vào | §5.1–5.3 |
| GD-04 / GD-10 | Nguyên liệu chính đan đột phá, thiên tài địa bảo | §5.9, §7.1 |

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện* (chính), P2 *Cảm giác tu tiên*, P3 *Chiến đấu có kỹ năng*.

**Người chơi cần cảm thấy:**
- Tim đập nhanh: mỗi bước đi đều có giá, túi đồ càng nặng càng sợ chết.
- Quyết định thật: *"tham thêm hay về ngay?"*
- Đồng đội quan trọng: được kéo dậy khi ngã, được nhặt hộ di vật.
- Chết là **lỗi của mình** (tham, chủ quan), không phải do game bất công hay mạng lag.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="dz-r-001"></a>**DZ-R-001** | Trước khi vào, người chơi **luôn** thấy và xác nhận màn cảnh báo: cấp hiểm địa, yêu cầu cảnh giới, thời lượng, *"Sinh tử tự chịu"*, tình trạng bảo hộ tân thủ và Thế Thân Phù. |
| <a id="dz-r-002"></a>**DZ-R-002** | Hết máu trong hiểm địa → **hấp hối** (`DOWNED`), không chết ngay. Chỉ chết thật khi hết thời gian hấp hối mà không được cứu, hoặc khi hiểm địa sụp đổ (§5.3). |
| <a id="dz-r-003"></a>**DZ-R-003** | Vật phẩm nhặt trong hiểm địa nằm ở `DANGER_BAG` và **chỉ thuộc về người chơi khi thoát ra thành công**. |
| <a id="dz-r-004"></a>**DZ-R-004** | **Mất kết nối không bao giờ trực tiếp gây chết**: nhân vật vào quy tức (§5.7); hết quy tức thì bị đẩy ra ngoài và mất `DANGER_BAG`, nhưng sống. |
| <a id="dz-r-005"></a>**DZ-R-005** | Hiểm địa **không có PvP** dù server bật PvP dã ngoại (GD-32 PK-R-002): người chơi không gây sát thương lên nhau trong hiểm địa. Ma Vực (PvP, chết thật) là quyết định riêng (DZ-Q-5). |
| <a id="dz-r-006"></a>**DZ-R-006** | Mọi chuyển dịch vật phẩm (nhặt, thoát, chết, nhặt di hài) do API Service thực hiện trong transaction, ghi `item_log`, idempotent. |

---

## 2. Tổng quan

```
 BẢN ĐỒ NGOÀI (vd. V1_F04) ── cửa hiểm địa ──► MÀN CẢNH BÁO + phí vào (§5.1)
                                                 ▼ cả tổ đội xác nhận
 INSTANCE (bố cục ngẫu nhiên trong 2–3 biến thể, fog mới) ─────────────────────────────┐
   │ pha:  YÊN TĨNH ──(40%)──► BẤT AN ──(70%)──► BẠO LOẠN ──(100%)──► SỤP ĐỔ         │
   │       quái thường         quái +15%,         quái +30%, linh khí        sát thương│
   │                           lối thoát tạm      bạo loạn, một số lối       tăng dần  │
   │                           xuất hiện          thoát đóng                 → chết    │
   │                                                                                    │
   │ Nhặt đồ ─► DANGER_BAG      Hết máu ─► HẤP HỐI 25s ─► được cứu / CHẾT THẬT (GD-26) │
   │ Mất kết nối ─► QUY TỨC 60s ─► kết nối lại / bị đẩy ra (mất DANGER_BAG, sống)      │
   ▼                                                                                    │
 THOÁT: đứng ở lối thoát 8s hoặc Truyền Tống Phù 10s ─► DANGER_BAG → BAG ─────────────┘
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type DangerTier = 1 | 2 | 3 | 4 | 5;                       // Nhất … Ngũ giai
type DangerPhase = 'CALM' | 'UNREST' | 'STORM' | 'COLLAPSE';
type InstanceMode = 'PARTY' | 'SHARED';                    // MVP: PARTY
type HazardKind = 'POISON_FOG' | 'KILLING_INTENT' | 'QI_STORM';

interface DangerZoneDef {
  id: string;                         // V1_HD01
  nameKey: string;
  tier: DangerTier;
  realmMin: string;                   // RealmCode
  partySize: [number, number];        // [1, 5]
  instanceMode: InstanceMode;
  layouts: string[];                  // mapId các biến thể bố cục, vd. V1_HD01_L1
  entry: { outerMapId: string; gatePoiId: string; returnPoiId: string };
  entryFee: { spiritStones: number; itemId?: string };
  durationSec: number;                // tới khi SỤP ĐỔ
  phaseAtPct: { UNREST: number; STORM: number };          // 0,4 / 0,7
  monsterPhaseBonusPct: { UNREST: number; STORM: number }; // 0,15 / 0,30
  dangerBagSlots: number;
  exits: ExitDef[];                   // theo layout: ExitDef.layoutId
  tempExit?: { spawnAtPhase: DangerPhase; openSec: number; candidates: [number, number][] };
  hazards: HazardDef[];
  traps: TrapPlacement[];
  spawnTableIds: string[];            // GD-12, áp dụng trong instance
  roamerIds: string[];                // quái tinh anh / boss lang thang (alertOnScan)
  chests: ChestPlacement[];
  nodes: NodePlacement[];             // linh thảo / khoáng hiếm, có hiddenLevel
  scanAlertChance: number;            // EN-R-140
  partyElitesHpScaling: number;       // §6.4
}

interface ExitDef {
  id: string; layoutId: string;
  position: [number, number]; radius: number;
  openFrom: DangerPhase;              // mở từ pha này
  closeAt?: DangerPhase;              // đóng khi vào pha này (nếu có)
  channelSec: number;                 // 8
}
interface HazardDef {
  kind: HazardKind; areaIds?: string[];  // null = toàn bản đồ
  activeFrom: DangerPhase;
  statusId: string;                   // status áp định kỳ (GD-06)
  tickSec: number;
  counterStatusId?: string;           // status miễn nhiễm (vd. ST_ANTI_POISON từ Tị Độc Đan)
}
interface TrapDef {
  id: string;                         // TRP_*
  hiddenLevel: number; radius: number;
  effect: { damagePctMaxHp: number; statusId?: string; statusSec?: number };
  rearmSec: number | null;            // null = dùng một lần
}
interface TrapPlacement { layoutId: string; trapId: string; candidates: [number, number][]; count: number }
interface ChestPlacement { layoutId: string; chestTier: 'COMMON' | 'RARE' | 'LEGENDARY'; lootTableId: string;
                           candidates: [number, number][]; count: number; guardMonsterId?: string; openSec: number }
interface NodePlacement { layoutId: string; nodeId: string; candidates: [number, number][]; count: number; hiddenLevel: number }

interface DangerRun {
  runId: string; dzId: string; layoutId: string; instanceId: string;
  partyId: string | null; memberIds: string[];
  startedAt: string; endsAt: string; phase: DangerPhase;
  state: 'PENDING_CONFIRM' | 'ACTIVE' | 'CLOSED';
}
type MemberState = 'IN' | 'DOWNED' | 'DORMANT_GUARD' | 'EXTRACTED' | 'DIED' | 'PROTECTED_EXPELLED' | 'FORCE_EXTRACTED';
interface DangerRunMember {
  runId: string; characterId: string; state: MemberState;
  downedCount: number; enteredAt: string; resolvedAt: string | null;
  carriedSnapshot: string[];          // itemInstanceId BAG + EQUIP lúc vào (để đối chiếu)
}
interface Corpse {
  corpseId: string; runId: string; characterId: string;
  position: [number, number];
  itemInstanceIds: string[];          // ItemLocation.CORPSE
  expiresAt: string;
}
```

---

## 4. Máy trạng thái

### 4.1. Lượt hiểm địa (`DangerRun`)
| Từ | Đến | Điều kiện | Tác dụng phụ |
|----|-----|-----------|--------------|
| — | `PENDING_CONFIRM` | Đội trưởng mở lượt tại cửa (§5.1) | Gửi màn cảnh báo cho mọi thành viên |
| `PENDING_CONFIRM` | `ACTIVE` | Mọi thành viên xác nhận trong 30s và đủ điều kiện | Thu phí; chọn bố cục; tạo instance; chuyển cả đội vào (GD-21 handoff) |
| `PENDING_CONFIRM` | `CLOSED` | Có người từ chối / hết 30s | Không thu phí |
| `ACTIVE` | `ACTIVE` | Đổi pha theo thời gian (§5.3) | Phát `S2C_DangerPhase` |
| `ACTIVE` | `CLOSED` | Mọi thành viên đã có trạng thái kết thúc (`EXTRACTED`, `DIED`, `PROTECTED_EXPELLED`, `FORCE_EXTRACTED`) | Sau 60s hủy instance; di hài còn lại hết hạn (§5.6) |

### 4.2. Thành viên (`DangerRunMember`)
| Từ | Đến | Điều kiện |
|----|-----|-----------|
| `IN` | `DOWNED` | HP về 0 (§5.5) |
| `DOWNED` | `IN` | Được cứu (§5.5) hoặc Thế Thân Phù kích hoạt (GD-26 LC-R-071) |
| `DOWNED` | `DIED` / `PROTECTED_EXPELLED` | Hết thời gian hấp hối → `LethalEvent` → GD-26 trả `DIED` / `PROTECTED_NEWBIE` |
| `IN` / `DOWNED` | `DORMANT_GUARD` | Mất kết nối (§5.7); nếu đang `DOWNED` thì thời gian hấp hối tiếp tục chạy |
| `DORMANT_GUARD` | `IN` | Kết nối lại trong thời hạn |
| `DORMANT_GUARD` | `FORCE_EXTRACTED` | Hết thời hạn quy tức (§5.7) |
| `IN` | `EXTRACTED` | Thoát thành công (§5.4) |
| `IN` / `DOWNED` / `DORMANT_GUARD` | `DIED` | Pha `COLLAPSE` làm HP về 0 khi đang `DOWNED` / hết hấp hối (§5.3) |

---

## 5. Quy tắc

### 5.1. Vào hiểm địa
- <a id="dz-r-010"></a>**DZ-R-010** Mở lượt tại POI cửa (`gatePoiId`) trên bản đồ ngoài. Tổ đội 1–5 người (`partySize`); mọi thành viên phải ở trong 15m quanh cửa, `lifeState == ALIVE`, cảnh giới ≥ `realmMin`, không trong chiến đấu, không có lượt hiểm địa khác đang mở.
- <a id="dz-r-011"></a>**DZ-R-011** Mỗi thành viên tự xác nhận màn cảnh báo (DZ-R-001) và trả **phí vào** riêng (`entryFee`, sink linh thạch). Ai thiếu phí thì cả lượt không mở được.
- <a id="dz-r-012"></a>**DZ-R-012** Không vào thêm giữa lượt. Thành viên rời tổ đội khi đang trong hiểm địa vẫn ở trong instance tới khi kết thúc lượt của mình.
- <a id="dz-r-013"></a>**DZ-R-013** Instance `PARTY`: mỗi lượt một instance riêng (MVP). `SHARED` (nhiều tổ đội, ≤ 30 người, Beta: Thượng Cổ Tiên Phủ) theo §5.11.
- <a id="dz-r-014"></a>**DZ-R-014** Mỗi lượt chọn ngẫu nhiên một `layoutId` trong `layouts`; vị trí rương, bẫy, điểm thu thập rút ngẫu nhiên từ `candidates` (RNG server). Fog of war riêng của lượt, không lưu (GD-21 §3.5).
- <a id="dz-r-015"></a>**DZ-R-015** **Mọi vật phẩm người chơi mang vào** (`BAG` + `EQUIP` tại thời điểm vào) đều **có thể mất nếu chết thật**. `carriedSnapshot` ghi lại để đối chiếu và telemetry. Tàng Bảo Các, kho, thư không bị ảnh hưởng.
- <a id="dz-r-016"></a>**DZ-R-016** Tuổi trôi ×1,0 (`ACTIVE`, GD-26). Không đả tọa, không bế quan, không đột phá trong hiểm địa (GD-04 CU-R-030, CU-R-060).

### 5.2. Túi hiểm địa
- <a id="dz-r-020"></a>**DZ-R-020** Mọi vật phẩm nhặt trong hiểm địa (quái, rương, điểm thu thập, di hài) vào `DANGER_BAG`, sức chứa `dangerBagSlots` (Nhất giai 12 · Nhị giai 16 · Tam giai 20). Đầy thì không nhặt được (vẫn để dưới đất tới khi túi đồ / rương hết hạn).
- <a id="dz-r-021"></a>**DZ-R-021** Vật phẩm trong `DANGER_BAG` **dùng được ngay** (vd. đan hồi phục nhặt được), nhưng không chuyển được sang `BAG` hay cho người khác khi còn trong hiểm địa.
- <a id="dz-r-022"></a>**DZ-R-022** `DANGER_BAG` lưu DB (vị trí `DANGER_BAG`, gắn `runId`) để không mất khi Game Server khởi động lại (§5.8).

### 5.3. Pha và áp lực thời gian
- <a id="dz-r-030"></a>**DZ-R-030** Pha theo tỷ lệ thời gian đã trôi `t / durationSec`: `CALM` [0; 0,4) · `UNREST` [0,4; 0,7) · `STORM` [0,7; 1,0) · `COLLAPSE` từ 1,0.

| Pha | Quái | Môi trường | Lối thoát |
|-----|------|-----------|-----------|
| `CALM` | Bình thường | Nguy hiểm riêng của hiểm địa (§5.10) | Lối thoát `openFrom = CALM` mở |
| `UNREST` | +15% `attack`/`spellPower`/`defense`/`spellResist` (`ST_DZ_UNREST`); bảng spawn hồi nhanh ×1,5 | — | Lối thoát tạm xuất hiện ở vị trí ngẫu nhiên trong `openSec` (180s), hiện trên minimap khi quét thần thức |
| `STORM` | +30% (`ST_DZ_STORM`, thay `ST_DZ_UNREST`) | **Linh khí bạo loạn:** mọi người chơi nhận `ST_QI_STORM` (DoT `TRUE` 1% `maxHp` mỗi 5s) | Lối thoát có `closeAt = STORM` đóng; lối thoát `openFrom = STORM` mở |
| `COLLAPSE` | Như `STORM` | **Sụp đổ:** `ST_COLLAPSE` DoT `TRUE` bắt đầu 3% `maxHp`/s, +1%/s mỗi 10s | Mọi lối thoát còn mở vẫn dùng được |

- <a id="dz-r-031"></a>**DZ-R-031** Cảnh báo pha: thông báo + âm thanh 60s và 10s trước mỗi lần đổi pha; đồng hồ đếm ngược luôn hiện trên HUD.
- <a id="dz-r-032"></a>**DZ-R-032** Người chơi `DOWNED` vẫn nhận DoT của `STORM`/`COLLAPSE` dưới dạng **trừ thời gian hấp hối** (DZ-R-052), không phải HP.

### 5.4. Thoát
- <a id="dz-r-040"></a>**DZ-R-040** **Lối thoát:** đứng trong bán kính lối thoát đang mở và vận `channelSec` (8s). Di chuyển ra ngoài hoặc nhận sát thương thì **đặt lại** về 0. Nhiều người có thể vận cùng lúc.
- <a id="dz-r-041"></a>**DZ-R-041** **Truyền Tống Phù** (`ITM_TRUYEN_TONG_PHU`): vận 10s ở bất kỳ đâu (đứng yên), nhận sát thương thì hủy (phù **không** bị tiêu nếu bị hủy). Thành công thì phù bị tiêu. Hiếm: từ cơ duyên / chế tạo / rương hiếm; không bán bằng tiền thật (D-17).
- <a id="dz-r-042"></a>**DZ-R-042** Thoát thành công (transaction, `extractId` idempotent): toàn bộ `DANGER_BAG` → `BAG` (thiếu chỗ thì phần dư gửi `MAIL`), item_log `DANGER_EXTRACTED`; nhân vật xuất hiện tại `returnPoiId` trên bản đồ ngoài với `ST_INVULNERABLE` 3s. Trạng thái thành viên `EXTRACTED`.
- <a id="dz-r-043"></a>**DZ-R-043** Thoát là **cá nhân**: đồng đội ở lại không bị ảnh hưởng.

### 5.5. Hấp hối và cứu
- <a id="dz-r-050"></a>**DZ-R-050** HP về 0 trong hiểm địa (bất kỳ nguồn nào) → `DOWNED` (GD-07 §4): không tấn công / dùng kỹ năng / vật phẩm; bò 1 m/s; hủy vận thoát. Quái không tấn công người `DOWNED` (GD-12 EN-R-141).
- <a id="dz-r-051"></a>**DZ-R-051** Thời gian hấp hối: lần 1 trong lượt `params.downedSec` (25s); mỗi lần tiếp theo **giảm một nửa** (12,5s · 6s · tối thiểu 3s).
- <a id="dz-r-052"></a>**DZ-R-052** Sát thương môi trường (`ST_QI_STORM`, `ST_COLLAPSE`, hazard, bẫy) lên người `DOWNED` trừ 2s thời gian hấp hối mỗi tick.
- <a id="dz-r-053"></a>**DZ-R-053** **Cứu:** đồng đội (không `DOWNED`) giữ tương tác 4s trong bán kính 2m; người cứu nhận sát thương thì đặt lại. Được cứu: 30% HP, `ST_WEAKENED` 60s, `ST_INVULNERABLE` 2s.
- <a id="dz-r-054"></a>**DZ-R-054** Hết thời gian hấp hối → Game Server gửi `LethalEvent(cause = DANGER_ZONE, deathLogId)` tới GD-26 và xử lý theo kết quả:
  - `WARD_CONSUMED`: hồi sinh tại chỗ (GD-26 LC-R-071), tiếp tục lượt, `downedCount` giữ nguyên.
  - `PROTECTED_NEWBIE`: đưa ra `returnPoiId`, `DANGER_BAG` bị hủy (GD-26 LC-R-070), trạng thái `PROTECTED_EXPELLED`. Không tạo di hài.
  - `DIED`: tạo di hài (§5.6), trạng thái `DIED`.
- <a id="dz-r-055"></a>**DZ-R-055** Solo vẫn hấp hối như bình thường (có thể được Thế Thân Phù cứu ở cuối thời gian), nhưng không ai cứu được.

### 5.6. Di hài
- <a id="dz-r-060"></a>**DZ-R-060** Khi chết thật trong hiểm địa, GD-26 bước 3 chuyển toàn bộ `DANGER_BAG` + `BAG` + `EQUIP` của nhân vật sang `CORPSE`; GD-28 tạo `Corpse` tại vị trí chết, tồn tại tới khi instance đóng (§4.1) hoặc `params.corpseSec` (600s), lấy mốc sớm hơn.
- <a id="dz-r-061"></a>**DZ-R-061** Đồng đội trong cùng lượt thấy di hài và nhặt từng vật phẩm (vào `DANGER_BAG` của người nhặt, chiếm chỗ; item_log `CORPSE_LOOTED`). Người ngoài lượt không thấy (MVP).
- <a id="dz-r-062"></a>**DZ-R-062** Di hài hết hạn: vật phẩm còn lại chuyển `DESTROYED` (item_log `CORPSE_EXPIRED`).
- <a id="dz-r-063"></a>**DZ-R-063** Vật phẩm nhặt từ di hài giữ thuộc tính `originCharacterId` để hiển thị *"Di vật của {tên}"*; sau khi người nhặt thoát ra thì là vật phẩm bình thường của họ. (Gửi trả di vật về tài khoản người chết: Beta, DZ-Q-2.)

### 5.7. Quy tức (mất kết nối)
- <a id="dz-r-070"></a>**DZ-R-070** Mất kết nối khi đang trong hiểm địa (không `DOWNED`): nhân vật đứng yên, vào `DORMANT_GUARD` (GD-07 §4) với `ST_DORMANT_GUARD` (`damageTaken` −80%); threat giảm về 0 trong 10s (GD-12 EN-R-142). Thời hạn `params.dormantSec` (60s).
- <a id="dz-r-071"></a>**DZ-R-071** Kết nối lại trong thời hạn → về `IN` tại chỗ.
- <a id="dz-r-072"></a>**DZ-R-072** Hết thời hạn → `FORCE_EXTRACTED`: đưa ra `returnPoiId`, **`DANGER_BAG` bị hủy** (item_log `DANGER_FORFEIT`), vật phẩm mang vào giữ nguyên, nhân vật sống. Không có lợi gì khi cố ý rút mạng (mất toàn bộ chiến lợi phẩm).
- <a id="dz-r-073"></a>**DZ-R-073** Mất kết nối khi đang `DOWNED`: thời gian hấp hối tiếp tục chạy (không quy tức); đồng đội vẫn cứu được. Hết giờ → `LethalEvent` với `needsReview = true` (GD-26 LC-R-075).
- <a id="dz-r-074"></a>**DZ-R-074** HP về 0 khi đang quy tức → `DOWNED` như bình thường; chết sau đó gắn `needsReview = true`.
- <a id="dz-r-075"></a>**DZ-R-075** Tài khoản có ≥ `params.dormantAbuseLimit` (3) lần `FORCE_EXTRACTED` trong 7 ngày: thời hạn quy tức của lần sau giảm còn 20s và `damageTaken` chỉ −50%.

### 5.8. Sự cố server
- <a id="dz-r-080"></a>**DZ-R-080** Game Server mất instance (sập, khởi động lại) khi lượt đang `ACTIVE`: mọi thành viên còn `IN`/`DOWNED`/`DORMANT_GUARD` được xử lý như **thoát thành công** (giữ `DANGER_BAG`), item_log `DANGER_SERVER_RECOVERY`, gắn `needsReview`. Người chơi không bị phạt vì lỗi của server. Khi **API Service** mất kết nối > 10s, Game Server tạm dừng đếm hấp hối và không phát sinh chết thật mới ([TD-01 §13](../tech/00-tdd.md)).
- <a id="dz-r-081"></a>**DZ-R-081** Job khởi động quét `danger_run_member` trạng thái mở thuộc instance không còn tồn tại và áp DZ-R-080.

### 5.9. Phần thưởng
- <a id="dz-r-090"></a>**DZ-R-090** Nguồn chiến lợi phẩm: quái (GD-12, `toLocation = DANGER_BAG`), rương, điểm thu thập ẩn (`hiddenLevel` 1–3, tìm bằng thần thức), di hài.
- <a id="dz-r-091"></a>**DZ-R-091** **Rương:** mở bằng tương tác `openSec` (Thường 2s · Hiếm 4s · Truyền thuyết 6s, bị ngắt khi nhận sát thương). Rương Truyền thuyết luôn có quái canh (`guardMonsterId`, tinh anh). Mỗi người chơi trong lượt roll riêng khi mở (rơi đồ cá nhân, GD-12 EN-R-004) nếu ở trong 15m lúc rương mở.
- <a id="dz-r-092"></a>**DZ-R-092** Vật phẩm **độc quyền hiểm địa** (chỉ rơi ở đây) theo cấp (§7.1): nguyên liệu chính đan đột phá (GD-04 CU-R-060), linh dịch tẩy tủy (GD-27 `MODIFY_ROOT`), mảnh ngọc giản phẩm cao (GD-08), Diên Thọ Quả (GD-26 §6.3), Truyền Tống Phù, Thế Thân Phù (rất hiếm).
- <a id="dz-r-093"></a>**DZ-R-093** Giá trị kỳ vọng tăng theo thời gian ở lại: một phần rương / điểm thu thập chỉ xuất hiện từ `UNREST` hoặc `STORM` (`activeFrom` trong placement), tạo động lực "tham thêm".

### 5.10. Nguy hiểm riêng, bẫy, kinh động
- <a id="dz-r-100"></a>**DZ-R-100** **Nguy hiểm môi trường** (`HazardDef`):

| Loại | Hiểm địa | Hiệu ứng | Khắc chế |
|------|----------|----------|----------|
| `POISON_FOG` | Vạn Độc Đầm | Trong vùng sương độc: `ST_POISON` +1 tầng mỗi 3s | `ST_ANTI_POISON` (Tị Độc Đan, GD-10) miễn nhiễm 300s; `poisonResist` |
| `KILLING_INTENT` | Cổ Chiến Trường | Mỗi 30s ở trong hiểm địa: +1 tầng `ST_KILLING_INTENT` (`damageTaken` +3%/tầng, tối đa 10) | Đứng ở **Trấn Hồn Đài** 5s xóa toàn bộ (mỗi đài dùng một lần mỗi người) |
| `QI_STORM` | Mọi hiểm địa, từ `STORM` | DZ-R-030 | — |

- <a id="dz-r-101"></a>**DZ-R-101** **Bẫy trận pháp** (`TrapDef`): ẩn (`hiddenLevel` 1–3), lộ khi thần thức quét đủ cấp (GD-21 §3.7) hoặc khi tới gần 2m (lóe sáng 0,5s trước khi kích hoạt). Kích hoạt: sát thương % `maxHp` + status; `rearmSec` hoặc một lần.
- <a id="dz-r-102"></a>**DZ-R-102** **Kinh động khi quét** (GD-12 EN-R-140): mỗi quái `alertOnScan` trong `scanRadius × 1,5` roll `scanAlertChance` (Nhất giai 0,25 · Nhị giai 0,40 · Tam giai 0,60); trúng thì quái `ALERT` về vị trí người quét. Người quét nhận cảnh báo *"Có thứ gì đó đã cảm nhận được thần thức của ngươi"*.

### 5.12. Ma Vực — hiểm địa PvP (Beta 2, chốt PLAN Q9)
- <a id="dz-r-120"></a>**DZ-R-120** **Ma Vực** là loại hiểm địa riêng (`dangerKind = MA_VUC`) nơi **PvP bật và chết vì người chơi là chết thật** (đúng luật hiểm địa, LC-R-001). Vào tự nguyện, có màn xác nhận đỏ. Ngoài Ma Vực, mọi hiểm địa khác giữ DZ-R-005 (không PvP).
- <a id="dz-r-121"></a>**DZ-R-121** Nguyên tắc định trước (chi tiết viết ở bản GD-28 sau): phần thưởng cao hơn hiểm địa cùng giai; **không** tính sát nghiệp (GD-32) bên trong; di hài người chết rơi toàn bộ túi hiểm địa cho kẻ khác nhặt; quy tức khi mất kết nối vẫn áp dụng; ghép kênh theo dải cảnh giới để hạn chế chênh lệch; cảnh báo vị trí kẻ địch qua thần thức.

### 5.11. Instance chung (Beta)
- <a id="dz-r-110"></a>**DZ-R-110** `SHARED`: nhiều tổ đội cùng một instance (≤ 30 người), mở theo lịch (Thượng Cổ Tiên Phủ: 2 giờ mỗi tuần, GD-23). Vẫn PvE (DZ-R-005); rương và tinh anh là tài nguyên chung theo luật rơi đồ cá nhân; di hài chỉ tổ đội của người chết thấy.

---

## 6. Công thức

### 6.1. Pha theo thời gian
```
p = (now − startedAt) / durationSec
phase = p < 0,4 ? CALM : p < 0,7 ? UNREST : p < 1,0 ? STORM : COLLAPSE
```
Ví dụ HD01 (900s): `UNREST` lúc 360s, `STORM` lúc 630s, `COLLAPSE` lúc 900s.

### 6.2. Thời gian hấp hối
```
downedSec(k) = max(params.downedMinSec (3), params.downedSec (25) / 2^(k − 1)),  k = lần hấp hối thứ k trong lượt
```
k = 1: 25s · k = 2: 12,5s · k = 3: 6,25s · k = 4: 3,125s · k ≥ 5: 3s.

### 6.3. Sát thương sụp đổ
```
collapseDps% = 3 + floor((now − collapseStart) / 10)        (% maxHp mỗi giây, TRUE)
```
Ví dụ sau 25s sụp đổ: 5% `maxHp`/s.

### 6.4. HP tinh anh / boss lang thang theo số người
```
maxHp = base × (1 + partyElitesHpScaling × (n − 1)),   partyElitesHpScaling mặc định 0,5,  n = số người vào lượt
```
Quái thường không đổi theo số người.

### 6.5. Kỳ vọng phần thưởng (mục tiêu cân bằng, kiểm chứng bằng Balance Simulator)
```
EV(lượt) = Σ P(nhặt được i) × giá trị(i) × P(thoát | thời gian ở lại)
```
Mục tiêu tỷ lệ thoát (người chơi đúng cảnh giới, trang bị chuẩn, tổ đội 3): Nhất giai 85% · Nhị giai 70% · Tam giai 55%. Tỷ lệ chết thật mỗi lượt (không tính bảo hộ): Nhất giai 3–5% · Nhị giai 8–12% · Tam giai 15–20%.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/danger_zones/params.json` | Tham số |
| `shared/gamedata/danger_zones/zones.json` | `DangerZoneDef[]` |
| `shared/gamedata/danger_zones/traps.json` | `TrapDef[]` |
| `shared/maps/V1_HD01_L*.json` … | Bố cục (map editor, TD-13): khu, lối thoát, ứng viên rương / bẫy / điểm thu thập |
| `shared/gamedata/monsters/*` | Quái, bảng spawn, bảng rơi đồ dùng trong hiểm địa (GD-12) |

```jsonc
// params.json
{
  "gateRadius": 15, "confirmTimeoutSec": 30,
  "downedSec": 25, "downedMinSec": 3, "downedEnvTickPenaltySec": 2, "crawlSpeed": 1.0,
  "reviveHoldSec": 4, "reviveRadius": 2, "revivedHpPct": 0.30, "revivedWeakenedSec": 60, "revivedInvulnSec": 2,
  "exitChannelSec": 8, "escapeTalismanChannelSec": 10, "extractInvulnSec": 3,
  "corpseSec": 600, "instanceCloseDelaySec": 60,
  "dormantSec": 60, "dormantDamageTakenPct": -0.8,
  "dormantAbuseLimit": 3, "dormantAbuseWindowDays": 7, "dormantAbuseSec": 20, "dormantAbuseDamageTakenPct": -0.5,
  "phaseWarningSec": [60, 10],
  "qiStorm": { "tickSec": 5, "pctMaxHp": 0.01 },
  "collapse": { "startPctPerSec": 0.03, "stepPctPerSec": 0.01, "stepSec": 10 },
  "chestOpenSec": { "COMMON": 2, "RARE": 4, "LEGENDARY": 6 }, "chestShareRadius": 15,
  "trapRevealDistance": 2, "trapRevealWarningSec": 0.5
}
```

```jsonc
// zones.json (trích)
[
  { "id": "V1_HD01", "nameKey": "dz.v1.van_doc_dam", "tier": 1, "realmMin": "LK5",
    "partySize": [1, 5], "instanceMode": "PARTY",
    "layouts": ["V1_HD01_L1", "V1_HD01_L2"],
    "entry": { "outerMapId": "V1_F04", "gatePoiId": "V1_F04_GATE_HD01", "returnPoiId": "V1_F04_GATE_HD01" },
    "entryFee": { "spiritStones": 50 },
    "durationSec": 900, "phaseAtPct": { "UNREST": 0.4, "STORM": 0.7 },
    "monsterPhaseBonusPct": { "UNREST": 0.15, "STORM": 0.30 },
    "dangerBagSlots": 12,
    "exits": [
      { "id": "EX1", "layoutId": "V1_HD01_L1", "position": [20, 30], "radius": 3, "openFrom": "CALM", "channelSec": 8 },
      { "id": "EX2", "layoutId": "V1_HD01_L1", "position": [210, 180], "radius": 3, "openFrom": "CALM", "closeAt": "STORM", "channelSec": 8 },
      { "id": "EX3", "layoutId": "V1_HD01_L1", "position": [120, 240], "radius": 3, "openFrom": "STORM", "channelSec": 8 }
    ],
    "tempExit": { "spawnAtPhase": "UNREST", "openSec": 180, "candidates": [[80, 90], [160, 60], [60, 200]] },
    "hazards": [ { "kind": "POISON_FOG", "areaIds": ["V1_HD01_L1_A02", "V1_HD01_L1_A04"], "activeFrom": "CALM",
                   "statusId": "ST_POISON", "tickSec": 3, "counterStatusId": "ST_ANTI_POISON" } ],
    "traps": [ { "layoutId": "V1_HD01_L1", "trapId": "TRP_POISON_VENT", "candidates": [[50,50],[70,120],[150,140],[190,90]], "count": 3 } ],
    "spawnTableIds": ["SP_V1_HD01_A01", "SP_V1_HD01_A02", "SP_V1_HD01_A03"],
    "roamerIds": ["MON_E_POISON_DRAKE"],
    "chests": [ { "layoutId": "V1_HD01_L1", "chestTier": "LEGENDARY", "lootTableId": "LT_V1_HD01_CHEST_LEG",
                  "candidates": [[170, 210], [40, 160]], "count": 1, "guardMonsterId": "MON_E_POISON_DRAKE", "openSec": 6 } ],
    "nodes": [ { "layoutId": "V1_HD01_L1", "nodeId": "NODE_NGUNG_CO_THAO", "candidates": [[30,80],[90,150],[140,40],[180,220],[60,230]],
                 "count": 3, "hiddenLevel": 1 } ],
    "scanAlertChance": 0.25, "partyElitesHpScaling": 0.5 }
]
```

### 7.1. Hiểm địa MVP

| ID | Tên | Cấp | Cửa (bản đồ ngoài) | Yêu cầu | Thời lượng | Nguy hiểm riêng | Quái lang thang | Độc quyền |
|----|-----|:---:|--------------------|---------|:---:|-----------------|-----------------|-----------|
| `V1_HD01` | Vạn Độc Đầm | Nhất giai | `V1_F04` Vụ Ẩn Trạch | `LK5` | 15' | Sương độc, lỗ phun độc | `MON_E_POISON_DRAKE` Độc Giao (tinh anh, `alertOnScan`) | **Ngưng Cơ Thảo** (`ITM_NGUNG_CO_THAO`, nguyên liệu chính Trúc Cơ Đan), Bách Niên Linh Thảo, Truyền Tống Phù (hiếm) |
| `V1_HD02` | Cổ Chiến Trường | Nhị giai | `V1_F06` Cổ Chiến Nguyên | `TC1` | 20' | Sát khí (Trấn Hồn Đài), bẫy trận đá | `MON_B_GHOST_GENERAL` Tàn Hồn Tướng Quân (boss lang thang, `alertOnScan`) | **Tẩy Tủy Linh Dịch** (`MODIFY_ROOT PURIFY`), mảnh ngọc giản Huyền phẩm, **Huyết Ngọc Tủy** (nguyên liệu phụ Kết Kim Đan), Thế Thân Phù (rất hiếm) |

**Validator (CI):** mỗi layout có ≥ 2 lối thoát `openFrom = CALM` hoặc ít nhất 1 `CALM` + 1 `STORM` sao cho **luôn có ≥ 1 lối thoát mở ở mọi pha** (GD-21 checklist); mọi `statusId`, `trapId`, `lootTableId`, `nodeId`, `monsterId` tồn tại; `realmMin` khớp cấp (Nhất giai ≥ LK5, Nhị giai ≥ TC1, Tam giai ≥ KD1); không ứng viên nào nằm trong tường / ngoài vùng đi được; `roamerIds` có `alertOnScan = true`.

---

## 8. (Dành chỗ) Mô phỏng
Balance Simulator mô phỏng lượt hiểm địa với các hồ sơ (an toàn: thoát ở `CALM`; cân bằng: thoát ở `UNREST`; tham: ở tới `STORM`) để kiểm chứng §6.5 và giá trị nguyên liệu đột phá. Kết quả ghi ở v0.2.

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai (Client → API Service)

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/danger-zones` | Danh sách, yêu cầu, phí, tình trạng bảo hộ tân thủ của nhân vật | |
| `POST /v1/danger-zones/{dzId}/runs` | Đội trưởng mở lượt `{ characterId }` → `{ runId, warning }` | DZ-R-010 |
| `POST /v1/danger-runs/{runId}/confirm` | `{ characterId, accept: boolean }` | DZ-R-001, 011 |

### 9.2. API nội bộ (Game Server → API Service)

| Endpoint | Request | Response | Quy tắc |
|----------|---------|----------|---------|
| `POST /internal/v1/danger/runs/{runId}/activate` | `{ instanceId, layoutId }` | `{ endsAt }` | Thu phí, ghi `carriedSnapshot` |
| `POST /internal/v1/danger/runs/{runId}/extract` | `{ characterId, extractId, via: 'EXIT' \| 'TALISMAN', talismanItemId? }` | `{ moved, mailed }` | DZ-R-042; idempotent |
| `POST /internal/v1/danger/runs/{runId}/force-extract` | `{ characterId, eventId, reason: 'DORMANT_TIMEOUT' \| 'SERVER_RECOVERY' }` | `{ forfeited }` | DZ-R-072, DZ-R-080 |
| `POST /internal/v1/lifecycle/lethal-event` | (GD-26 §8.1) `cause = DANGER_ZONE` | | DZ-R-054 |
| `POST /internal/v1/danger/corpses` | `{ runId, characterId, position }` | `{ corpseId, itemInstanceIds }` | Sau khi GD-26 trả `DIED` |
| `POST /internal/v1/danger/corpses/{corpseId}/loot` | `{ lootId, looterCharacterId, itemInstanceIds }` | `{ moved, rejected }` | DZ-R-061; idempotent |
| `POST /internal/v1/loot/grant` | (GD-12 §9.1) `toLocation = DANGER_BAG`, `runId` | | DZ-R-020 |

### 9.3. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `S2C_DangerRunState` | S→C | `{ runId, phase, endsAt, nextPhaseAt, exits: [{ id, open, position }], tempExit? }` |
| `S2C_DangerPhase` | S→C | `{ runId, phase, warningSec? }` |
| `C2S_ExtractStart` / `C2S_ExtractCancel` | C→S | `{ exitId }` |
| `C2S_UseEscapeTalisman` | C→S | `{ itemInstanceId }` |
| `S2C_ChannelProgress` | S→C | `{ kind: 'EXIT' \| 'TALISMAN' \| 'REVIVE' \| 'CHEST', progress, reset? }` |
| `S2C_Downed` | S→C (đội) | `{ characterId, downedUntil, downedCount }` |
| `C2S_ReviveStart` / `C2S_ReviveCancel` | C→S | `{ targetCharacterId }` |
| `S2C_CorpseSpawn` | S→C (đội) | `{ corpseId, characterId, position, items: [{ itemInstanceId, itemId, qty }] }` |
| `C2S_LootCorpse` | C→S | `{ corpseId, itemInstanceIds }` |
| `S2C_ScanAlerted` | S→C | `{ count }` (DZ-R-102) |
| `S2C_RunResult` | S→C | `{ runId, result: MemberState, items: [...], durationSec }` |

### 9.4. Sự kiện domain (topic `danger.*`)

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `danger.run_started` | `{ runId, dzId, memberIds, layoutId }` | Telemetry |
| `danger.member_resolved` | `{ runId, characterId, result, bagValue, durationSec }` | Telemetry, Bia Vạn Cổ (lần đầu thoát mỗi cấp), GD-21 Vạn Giới Lục |
| `danger.run_closed` | `{ runId }` | Dọn di hài, instance |
| Nhận `lifecycle.life_ending` (GD-26) | | Tạo di hài nếu `mapId` thuộc hiểm địa |

---

## 10. Lưu trữ

```sql
CREATE TABLE danger_run (
  run_id      UUID PRIMARY KEY,
  dz_id       VARCHAR(16) NOT NULL,
  layout_id   VARCHAR(24),
  instance_id VARCHAR(64),
  party_id    UUID,
  state       VARCHAR(16) NOT NULL,              -- PENDING_CONFIRM | ACTIVE | CLOSED
  started_at  TIMESTAMPTZ, ends_at TIMESTAMPTZ, closed_at TIMESTAMPTZ,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE danger_run_member (
  run_id        UUID NOT NULL REFERENCES danger_run(run_id),
  character_id  UUID NOT NULL REFERENCES character(id),
  state         VARCHAR(24) NOT NULL,            -- MemberState
  confirmed     BOOLEAN NOT NULL DEFAULT FALSE,
  downed_count  INT NOT NULL DEFAULT 0,
  carried_snapshot JSONB,
  entered_at    TIMESTAMPTZ, resolved_at TIMESTAMPTZ,
  needs_review  BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (run_id, character_id)
);
-- một nhân vật tối đa một lượt mở
CREATE UNIQUE INDEX ux_dz_member_open ON danger_run_member(character_id)
  WHERE state IN ('IN', 'DOWNED', 'DORMANT_GUARD');

CREATE TABLE corpse (
  corpse_id    UUID PRIMARY KEY,
  run_id       UUID NOT NULL REFERENCES danger_run(run_id),
  character_id UUID NOT NULL,
  position     JSONB NOT NULL,
  expires_at   TIMESTAMPTZ NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE dz_dormant_strike (                   -- DZ-R-075
  account_id UUID NOT NULL, at TIMESTAMPTZ NOT NULL, run_id UUID NOT NULL,
  PRIMARY KEY (account_id, at)
);

-- item_instance (PLAN §5.4) bổ sung:
ALTER TABLE item_instance ADD COLUMN danger_run_id UUID;           -- khi location = DANGER_BAG
ALTER TABLE item_instance ADD COLUMN corpse_id UUID;               -- khi location = CORPSE
ALTER TABLE item_instance ADD COLUMN origin_character_id UUID;     -- di vật (DZ-R-063)
```

**Mã lý do item_log mới:** `DANGER_PICKUP`, `DANGER_EXTRACTED`, `DANGER_FORFEIT`, `DANGER_SERVER_RECOVERY`, `CORPSE_LOOTED`, `CORPSE_EXPIRED`, `ESCAPE_TALISMAN_CONSUMED`. Status mới (GD-06): `ST_DORMANT_GUARD`, `ST_DZ_UNREST`, `ST_DZ_STORM`, `ST_QI_STORM`, `ST_COLLAPSE`, `ST_KILLING_INTENT`, `ST_ANTI_POISON`.

---

## 11. UI/UX
- **Màn cảnh báo** (DZ-R-001): nền đỏ sẫm, chữ thư pháp *"Sinh tử tự chịu"*; cấp, thời lượng, độc quyền nổi bật, danh sách đồ đang mang (*"Toàn bộ có thể mất"*), dòng xanh *"Được bảo hộ tân thủ"* hoặc dòng vàng *"Thế Thân Phù: 1"*; nút xác nhận phải giữ 1,5s.
- **HUD trong hiểm địa:** đồng hồ đếm ngược + tên pha (màu đổi dần xanh → vàng → đỏ → đen), giá trị ước tính `DANGER_BAG`, hướng tới lối thoát gần nhất đang mở, biểu tượng tầng sát khí / độc.
- **Túi hiểm địa:** khung riêng bên cạnh túi thường, viền đỏ, chú thích *"Chưa thuộc về ngươi"*.
- **Hấp hối:** màn hình đen trắng, vòng đếm ngược lớn; đồng đội thấy biểu tượng đầu lâu đỏ trên minimap + vạch chỉ hướng.
- **Thoát:** vòng tiến độ tại lối thoát; thoát thành công có màn tổng kết "Thoát hiểm" liệt kê chiến lợi phẩm.
- **Di hài:** bia gỗ có tên người chết, ánh nến; mở ra như rương.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-26 | `LethalEvent(DANGER_ZONE)`; bảo hộ tân thủ; Thế Thân Phù; chuyển đồ sang `CORPSE`; `needsReview` |
| GD-12 | Quái hiểm địa, quái lang thang `alertOnScan`, không đánh người hấp hối, quy tức, tăng sức theo pha |
| GD-21 | Cửa ở bản đồ ngoài, instance, fog mỗi lượt, thần thức quét (bẫy, điểm thu thập ẩn, lối thoát tạm) |
| GD-06 | Status mới §10; DoT `TRUE` |
| GD-07 | Trạng thái `DOWNED`, `DORMANT_GUARD` |
| GD-04 / GD-10 | Nguyên liệu đột phá; Tị Độc Đan |
| GD-27 | Tẩy Tủy Linh Dịch (`MODIFY_ROOT`) |
| GD-22 | Cơ duyên trong hiểm địa (Beta) |
| GD-23 | Thượng Cổ Tiên Phủ (instance chung theo lịch, Beta) |
| GD-13 | Phí vào là sink linh thạch; chết là kênh tiêu hao vật phẩm lớn |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Cả tổ đội cùng hấp hối | Mỗi người hết thời gian thì chết theo DZ-R-054 (không chết đồng loạt ngay) |
| Đang vận thoát thì bị quái đánh | Đặt lại tiến độ (DZ-R-040) |
| Vận Truyền Tống Phù khi đang bị độc (DoT) | DoT là sát thương → hủy vận (DZ-R-041); phải giải độc trước |
| Hấp hối đúng lúc `COLLAPSE` bắt đầu | Mỗi tick sụp đổ trừ 2s thời gian hấp hối (DZ-R-052) |
| Người cứu đang trong vùng sương độc | DoT lên người cứu làm đặt lại tiến độ cứu |
| `DANGER_BAG` đầy khi nhặt di hài | Từ chối phần không vừa; để lại trong di hài |
| Hết thọ (LAST_LIGHT hết hạn) trong hiểm địa | GD-26: tọa hóa tại chỗ; `DANGER_BAG` chuyển `CORPSE` nhưng `endCause = LIFESPAN_EXHAUSTED`; tạo di hài như DZ-R-060 |
| Người chơi bị kick vì lý do khác (GM, cập nhật) | Xử lý như `SERVER_RECOVERY` (DZ-R-080) |
| Đội trưởng mở lượt khi một thành viên đang chiến đấu | Không mở được; báo tên thành viên chưa đủ điều kiện |
| Instance còn người `DORMANT_GUARD` lúc mọi người khác đã ra | Instance chờ tới khi người đó kết thúc (quy tức tối đa 60s) |

---

## 14. Telemetry

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `dz_run` | dzId, partySize, layoutId | Tần suất, cỡ đội phổ biến |
| `dz_member_result` | result, phaseAtResolve, durationSec, bagValue, downedCount | Tỷ lệ thoát / chết theo pha (so với §6.5) |
| `dz_downed` | cause (monster / trap / hazard / storm), revived, solo | Nguyên nhân ngã, tỷ lệ được cứu |
| `dz_death` | cause, phase, ping, needsReview | Chết có công bằng không (đối chiếu Death Review) |
| `dz_extract_choice` | phase, bagValue | Người chơi "tham" tới mức nào |
| `dz_scan` | alerted, found (bẫy / node) | Cân bằng rủi ro thần thức |
| `dz_corpse_loot` | lootedPct, byTeammate | Văn hóa nhặt hộ di vật |
| `dz_dormant` | reconnected, forceExtracted | Mức độ ảnh hưởng của mất kết nối |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="dz-ac-001"></a>DZ-AC-001 | Tổ đội 3 người, một người `LK4` | Mở lượt `V1_HD01` (`LK5`) | Bị từ chối, nêu tên thành viên chưa đủ cảnh giới |
| DZ-AC-002 | Tổ đội 2 người đủ điều kiện, một người từ chối cảnh báo | Xác nhận | Lượt `CLOSED`, không ai bị thu phí |
| DZ-AC-003 | Lượt `V1_HD01` bắt đầu lúc T | T + 360s / 630s / 900s | Pha lần lượt `UNREST` / `STORM` / `COLLAPSE`; cảnh báo 60s và 10s trước mỗi lần |
| DZ-AC-004 | Quái thường ở `STORM` | `computeStats` | Có `ST_DZ_STORM` +30% (không cộng thêm `ST_DZ_UNREST`) |
| DZ-AC-005 | Nhặt vật phẩm từ quái trong hiểm địa | Kiểm tra vị trí | `DANGER_BAG`, gắn `danger_run_id` |
| DZ-AC-006 | Đứng ở lối thoát 6s thì trúng đòn | Tiếp tục đứng | Tiến độ về 0; cần đủ 8s liên tục |
| DZ-AC-007 | Thoát thành công với 14 vật phẩm, `BAG` còn 10 ô | Thoát | 10 vào `BAG`, 4 vào `MAIL`; item_log `DANGER_EXTRACTED` |
| DZ-AC-008 | Gửi `extract` cùng `extractId` hai lần | API | Áp dụng một lần |
| DZ-AC-009 | HP về 0 lần 1 | Theo dõi | `DOWNED` 25s; quái chuyển mục tiêu; lần 2 trong cùng lượt: 12,5s |
| DZ-AC-010 | Đồng đội giữ cứu 4s không bị đánh | Hoàn tất | Người hấp hối về `IN` với 30% HP, `ST_WEAKENED` 60s |
| DZ-AC-011 | Hấp hối trong `STORM` (tick mỗi 5s) | 10s trôi qua | Thời gian hấp hối còn 25 − 10 − 4 = 11s |
| DZ-AC-012 | Kiếp 2, `TC1`, không Thế Thân Phù, hết hấp hối | `LethalEvent` | `DIED`; di hài chứa `DANGER_BAG` + `BAG` + `EQUIP`; đồng đội thấy `S2C_CorpseSpawn` |
| DZ-AC-013 | Kiếp 1, `LK6`, hết hấp hối ở `V1_HD01` | `LethalEvent` | `PROTECTED_EXPELLED`: về cửa, `DANGER_BAG` bị hủy, đồ mang vào giữ nguyên, không di hài |
| DZ-AC-014 | Có Thế Thân Phù trong `BAG`, hết hấp hối | `LethalEvent` | Hồi sinh tại chỗ, tiếp tục lượt |
| DZ-AC-015 | Đồng đội nhặt 3 món từ di hài rồi thoát | Thoát | 3 món vào `BAG` người nhặt; còn lại trong di hài bị `CORPSE_EXPIRED` khi instance đóng |
| DZ-AC-016 | Mất kết nối khi `IN`, kết nối lại sau 40s | Theo dõi | Về `IN` tại chỗ, `DANGER_BAG` còn nguyên |
| DZ-AC-017 | Mất kết nối khi `IN`, không kết nối lại | Sau 60s | `FORCE_EXTRACTED`: về cửa, `DANGER_BAG` mất (`DANGER_FORFEIT`), nhân vật sống |
| DZ-AC-018 | Đã có 3 lần `FORCE_EXTRACTED` trong 7 ngày | Mất kết nối lần 4 | Quy tức chỉ 20s, `damageTaken` −50% |
| DZ-AC-019 | Mất kết nối khi đang `DOWNED`, không ai cứu | Hết giờ | `LethalEvent` với `needsReview = true` |
| DZ-AC-020 | Game Server sập khi lượt đang `ACTIVE` | Khởi động lại | Mọi thành viên chưa kết thúc được tính thoát thành công, giữ `DANGER_BAG`, `needsReview` |
| DZ-AC-021 | Quét thần thức gần Độc Giao (`alertOnScan`), Nhất giai | 10.000 lần mô phỏng | Tỷ lệ kinh động ≈ 25% |
| DZ-AC-022 | Bẫy `hiddenLevel = 2`, `senseLevel = 1` | Đi tới 2m | Bẫy lóe sáng 0,5s rồi kích hoạt nếu vẫn đứng trong vùng |
| DZ-AC-023 | Đứng trong sương độc không có `ST_ANTI_POISON` 9s | Theo dõi | `ST_POISON` 3 tầng |
| DZ-AC-024 | Ở `COLLAPSE` 25s | Tính DoT | 5% `maxHp` mỗi giây (§6.3) |
| DZ-AC-025 | Mọi layout trong dữ liệu | Validator | Luôn có ≥ 1 lối thoát mở ở mọi pha |
| DZ-AC-026 | Tổ đội 3 người vào lượt | Tinh anh lang thang spawn | `maxHp = base × 2,0` (§6.4) |
| DZ-AC-027 | Vận Truyền Tống Phù 7s thì trúng đòn | Theo dõi | Hủy vận, phù không bị tiêu |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 1 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Vào – lấy – thoát, `DANGER_BAG`, lối thoát, pha thời gian (chưa chết thật) | ✅ | | | |
| Hấp hối, cứu | ✅ | | | |
| Chết thật, di hài, bảo hộ tân thủ, Thế Thân Phù | | ✅ | | |
| Quy tức, sự cố server | | ✅ | | |
| `V1_HD01` Vạn Độc Đầm, `V1_HD02` Cổ Chiến Trường | | ✅ | | |
| Bẫy, nguy hiểm môi trường, kinh động khi quét | | ✅ | | |
| Lối thoát tạm, Truyền Tống Phù | | ✅ | | |
| Gửi trả di vật về tài khoản người chết | | | ✅ (DZ-Q-2) | |
| Ma Uyên (nhiều tầng), Thượng Cổ Tiên Phủ (instance chung) | | | ✅ | |
| Cơ duyên trong hiểm địa | | | ✅ | |
| Ma Vực (hiểm địa PvP, §5.12) | | | ✅ (Beta 2) | |

---

## 17. Câu hỏi mở
- [ ] **DZ-Q-1:** Có nên có một ô "túi an toàn" (vd. 1 ô) giữ được vật phẩm khi chết? Mâu thuẫn với GD-26 (chết thật chỉ giữ Tàng Bảo Các). *Mặc định: không.*
- [ ] **DZ-Q-2:** Cho phép đồng đội gửi trả di vật về Tàng Bảo Các / thư tài khoản của người chết? Tạo khoảnh khắc cảm xúc nhưng làm giảm hậu quả cái chết. *Mặc định: Beta, giới hạn 1 món mỗi di hài.*
- [x] **DZ-Q-3:** Phí vào (50 / 300 linh thạch) có cản người mới không? Có nên miễn phí lần đầu mỗi ngày? **→ Đã trả lời ở GD-13 EC-R-023: giữ phí (≈ 10–15 phút thu nhập), không miễn phí.**
- [ ] **DZ-Q-4:** Thời lượng 15' / 20' có đủ để vừa khám phá vừa thoát? Chốt sau playtest Phase 1.
- [x] **DZ-Q-5** (= PLAN Q9): *(Đã chốt 2026-10-23: **có**, ở Beta 2 — §5.12.)* Ma Vực PvP có làm trong Beta không?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-20 | — | Bản nháp đầu: bất biến, kiểu dữ liệu, máy trạng thái lượt / thành viên, vào hiểm địa, túi hiểm địa, 4 pha thời gian, thoát (lối thoát, Truyền Tống Phù), hấp hối & cứu, di hài, quy tức, sự cố server, phần thưởng & vật phẩm độc quyền, nguy hiểm riêng / bẫy / kinh động, instance chung (Beta), công thức, HD01 + HD02, API, DB, nghiệm thu |
| v0.2 | 2026-10-23 | — | DZ-R-005: hiểm địa không có PvP kể cả khi bật PvP dã ngoại (GD-32) |
| v0.3 | 2026-10-23 | — | Chốt DZ-Q-5: **Ma Vực** hiểm địa PvP chết thật ở Beta 2 (§5.12, nguyên tắc định trước) |

