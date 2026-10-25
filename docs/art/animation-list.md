# AR-03 — Animation List

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | AR-03 · Prefix ID: `ANM` |
| Owner | Tech Artist + Animator (duyệt: Art Director, Client Dev) |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 0 (locomotion cho spike), Phase 1 (bộ kiếm + phản ứng + quái `QUADRUPED`), sau đó theo §6 |
| Quyết định nền | PLAN §6.2, §6.6, §6.7, D-22; [AR-02](asset-guidelines.md) §3 (tên `ANM_*`), §4 (bộ xương), §5 (hoạt ảnh, marker `hit`) — **AR-03 không được mâu thuẫn AR-02**; [TD-05](../tech/04-client.md) §6 (CL-R-070..073) |
| Liên quan | [AR-01 Art Bible](art-bible.md) · [AR-02 Asset Guidelines](asset-guidelines.md) · [GD-07](../design/13-skills.md) §3–§5 (`ActionState`, `SkillDef`, `WeaponDef`) · [GD-06](../design/12-stats-formulas.md) §5.9–5.10 (phản ứng, poise, né) · [GD-12](../design/18-enemies-ai.md) (`SkeletonGroup`, máy trạng thái AI, boss) · [GD-25](../design/26-spirit-beasts.md) (linh thú) · [GD-31](../design/29-sword-flight.md) (ngự kiếm) · [GD-04](../design/10-cultivation.md) (đả tọa, đột phá) · [GD-26](../design/03-lifecycle.md) (trọng thương, tọa hóa) · [GD-28](../design/27-danger-zones.md) (`DOWNED`, `DORMANT_GUARD`) · [GD-10](../design/16-alchemy.md) / [GD-11](../design/17-forging.md) (nghề) · [GD-19](../design/50-monetization.md) (`EMOTE`) · [TD-11](../tech/10-performance.md) · [TD-15](../tech/14-oss-register.md) §4.5 · [Glossary](../lore/glossary.md) |

**Tài liệu này cung cấp cho:**

| Bên dùng | Cần gì từ AR-03 | Mục |
|----------|-----------------|-----|
| Animator / AI script Blender | Danh mục clip phải làm: mã, bộ xương, vũ khí, frame, loop, marker, nguồn, ưu tiên, phase | §4 |
| TD-05 (`client/src/anim/`) | Ánh xạ `ActionState` → clip, `Shape` → clip, chuỗi dự phòng, gợi ý blend, công thức co giãn | §3, §5 |
| CI `pnpm art:check` | Catalogue máy đọc `tools/art/anim-list.json` (sinh từ tài liệu này) + tiêu chí `ANM-AC-*` | §2.5, §8 |
| PM / Asset Tracker (AR-05) | Số clip theo phase để ước lượng khối lượng | §6 |

> AR-03 là **danh mục sản xuất đầy đủ** mở rộng AR-02 §5.2–§5.3 (danh sách tối thiểu). Clip đánh dấu **(+)** là **bổ sung so với AR-02**, cần đồng bộ ngược vào AR-02 §5 (xem lịch sử thay đổi + câu hỏi mở). Mọi con số frame là **giá trị tham chiếu sản xuất**; thời lượng gameplay luôn lấy từ gamedata, client co giãn clip cho khớp (§5.3).

---

> **Ghi chú đồng bộ 2026-10-25:** các clip đánh dấu (+) đã được gộp vào AR-02 §5 (v0.3); AR-03 là danh mục chuẩn, AR-02 trỏ sang đây. ANM-Q-1 (combo `ATTACK_1..n`) và ANM-Q-5 (`ROOTED` → `IDLE_COMBAT`) đã giải quyết ở AR-02.

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="anm-r-001"></a>**ANM-R-001** | **Mỗi `ActionState` của GD-07 §4 có ít nhất một clip** (hoặc quy tắc dùng clip khác được ghi rõ ở §3.1) cho mọi bộ xương có thực thể chiến đấu; thiếu clip khi chạy → dùng chuỗi dự phòng §5.4, **không bao giờ T-pose** (TD-05 CL-R-073). |
| <a id="anm-r-002"></a>**ANM-R-002** | **30 fps gốc** cho mọi clip (AST-R-030). Số frame trong tài liệu tính ở 30 fps. |
| <a id="anm-r-003"></a>**ANM-R-003** | Clip tấn công / kỹ năng có **marker `hit`** (hoặc `hit_1..hit_n`) theo AST-R-031; client co giãn để `hit` trùng `windupSec + hit.atSec` của `SkillDef` / `ComboStep` (GD-07 SK-R-072, TD-05 CL-R-071). Cửa sổ hủy lấy từ dữ liệu, **không** từ clip (CL-R-072). |
| <a id="anm-r-004"></a>**ANM-R-004** | **Hoạt ảnh không quyết định gameplay:** vị trí, thời lượng trạng thái, va chạm do server quyết định (GD-07 SK-R-001, GD-12 EN-R-001). Mọi clip phát **tại chỗ (in-place)**; root motion chỉ có ở clip được phép (§2.4) để tham khảo khoảng cách, client bỏ khi phát (AST-R-030). |
| <a id="anm-r-005"></a>**ANM-R-005** | Một **bộ xương chung `HUMANOID`** cho người chơi, NPC, tàn hồn, quái hình người; quái theo `SkeletonGroup` (AST-R-004, CL-R-070). Không làm clip riêng cho từng trang phục / ngoại hình. |
| <a id="anm-r-006"></a>**ANM-R-006** | Clip chỉ được vào game khi **có trong catalogue** `tools/art/anim-list.json` (sinh từ §4). Muốn thêm clip → sửa AR-03 trước (tăng phiên bản), rồi mới sản xuất (docs/README §3 mục 7). |

---

## 2. Quy ước

### 2.1. Tên clip
- <a id="anm-r-010"></a>**ANM-R-010** Theo AR-02 §3 (`ANM_{SKELETON}_{HÀNH_ĐỘNG}`), chi tiết hóa thành bốn dạng:

| Dạng | Mẫu | Ví dụ | Dùng cho |
|------|-----|-------|----------|
| Chung của bộ xương | `ANM_{SKELETON}_{ACTION}` | `ANM_HUMANOID_DODGE_F`, `ANM_QUADRUPED_ATTACK_BITE` | Clip không phụ thuộc vũ khí; **biến thể gốc** của clip theo vũ khí (người chưa cầm vũ khí — Phàm Nhân trước bước chọn vũ khí của `QC_TUTORIAL_QI`, NPC) |
| Theo vũ khí | `ANM_HUMANOID_{WeaponType}_{ACTION}` | `ANM_HUMANOID_SWORD_ATTACK_1`, `ANM_HUMANOID_FAN_CAST_CONE` | Clip có cột "Theo vũ khí ✅" ở AR-02 §5.2; `WeaponType` đúng glossary (`SWORD`, `FAN`; Beta `DUAL_BLADE`, `FIST`) |
| Quái hình người theo archetype | `ANM_HUMANOID_{AiArchetype}_{ACTION}` | `ANM_HUMANOID_RANGED_KITER_ATTACK_1` | Đòn riêng của archetype (AR-02 §5.3 dòng `HUMANOID`) |
| Riêng một model (boss) | `ANM_{SKELETON}_{MODEL_KEY}_{ACTION}` | `ANM_QUADRUPED_FOX_MATRIARCH_CLAW_COMBO` | Chiêu riêng boss; `MODEL_KEY` = `modelId` bỏ tiền tố `MDL_MON_` (AR-02 §3) |

- <a id="anm-r-011"></a>**ANM-R-011** Chữ hoa + gạch dưới, ≤ 48 ký tự, **không đổi tên** sau khi được tham chiếu (AST-R-010). Tên NLA track trong Blender = mã clip (AR-02 §8 bước 3).
- <a id="anm-r-012"></a>**ANM-R-012** `art:check` **không phân tích tên** để suy ra loại clip (dạng 1 và 4 có thể trùng cấu trúc); mọi thuộc tính lấy từ catalogue (§2.5). Đoạn sau `ANM_HUMANOID_` chỉ được là `WeaponType` khi nó thuộc danh sách `WeaponType` của GD-07 §3.
- Hậu tố quy ước: `_LOOP` (lặp), `_ENTER` / `_EXIT` (vào / ra trạng thái lặp), `_F/_B/_L/_R` (hướng tương đối với hướng mặt), `_1.._n` (bước combo).

### 2.2. fps và thời lượng
- <a id="anm-r-020"></a>**ANM-R-020** Xuất 30 fps; `art:optimize` được resample / nén key nhưng không đổi thời lượng.
- <a id="anm-r-021"></a>**ANM-R-021** Clip có thời lượng gắn với tham số gameplay (cột "Khóa thời lượng" §4) được client co giãn cho khớp thời lượng trạng thái từ gamedata; animator làm đúng số frame tham chiếu để hệ số co giãn ≈ 1. `art:check` so thời lượng clip với tham số gamedata **tại thời điểm chạy CI** (không so với số trong tài liệu này).
- Clip lặp: frame đầu = frame cuối (AR-02 §5.1); clip một lần của trạng thái giữ (`DEATH`, `DOWNED_ENTER`, `KNOCKDOWN`) kết thúc ở tư thế giữ được.

### 2.3. Marker (timeline marker → `extras.events` của AnimationGroup, AST-R-031)

| Marker | Bắt buộc ở | Ý nghĩa |
|--------|------------|---------|
| `hit` | Clip có đúng 1 lượt trúng: `ATTACK_n`, `HEAVY_RELEASE`, `CAST_*` (trừ `CAST_CHANNEL_LOOP`), `DASH_STRIKE`, đòn quái, chiêu boss 1 nhát | Khung va chạm, dùng để co giãn (§5.3) |
| `hit_1..hit_n` | Clip riêng của chiêu có `n ≥ 2` `HitDef` (vd. chiêu boss liên kích) | Thay cho `hit`; client căn theo `hit_1`, các mốc sau dùng để kiểm tra (§5.3) |
| `cancel` | Tùy chọn | Mốc hủy sớm nhất **chỉ để xem trước trong tool**; luật hủy thật lấy `cancelAfterSec` (CL-R-072) |
| `vfx_spawn` | Tùy chọn; nên có ở `PARRY_PERFECT`, `CRAFT_FORGE_STRIKE`, `PHASE_TRANSITION` | Điểm sinh VFX / SFX không gắn lượt trúng |

- <a id="anm-r-030"></a>**ANM-R-030** Chỉ dùng bốn loại marker trên (danh sách của AR-02 §5.1). Cần loại mới (vd. bước chân cho AU-01) → câu hỏi mở ANM-Q-9, không tự thêm.
- <a id="anm-r-031"></a>**ANM-R-031** Marker phải nằm trong `(0, thời lượng clip)`; `hit` của clip combo / kỹ năng nằm trong **40% đầu** clip để còn follow-through (PLAN §6.2, AR-01 ART-R-042: anticipation → impact → follow-through).

### 2.4. Root motion
- <a id="anm-r-040"></a>**ANM-R-040** Xương `root` **chỉ** được mang tịnh tiến (x, z) ở: `DODGE_F/B/L/R`, `{W}_DASH_STRIKE` (người), `ATTACK_LUNGE` (`QUADRUPED`), `ATTACK_DIVE` (`BIRD`), chiêu lao của boss (cột "Root" = ✅ ở §4) — đúng phạm vi "lướt / né" của AST-R-030. Khoảng cách root motion bằng khoảng cách gameplay tham chiếu (vd. né 4m — `stats/params.json → dodge.distance`). Client luôn bỏ root motion khi phát.
- <a id="anm-r-041"></a>**ANM-R-041** Mọi clip khác: tịnh tiến (x, z) của `root` = 0 trên toàn clip (dung sai trong `anim-list.json → checks`); `hips` được dao động tự nhiên. Không clip nào xoay `root` quanh trục đứng (hướng mặt do server / client quyết định).
- <a id="anm-r-042"></a>**ANM-R-042** Không key xương phụ `cloth_*`, `sleeve_*`, `hair_*` (spring bone ở client, AR-02 §4) cho tới khi chốt AST-Q-2; không hoạt ảnh khuôn mặt (AR-01 ART-R-010). Xương socket (`socket_*`) không key riêng — đi theo xương cha.

### 2.5. Gói GLB và catalogue
- <a id="anm-r-050"></a>**ANM-R-050** Theo CL-R-070: **một GLB hoạt ảnh mỗi `SkeletonGroup`**, tách khỏi model; đường dẫn `art/animations/{SKELETON}.glb` (vd. `art/animations/HUMANOID.glb`; TD-05 CL-R-076) — còn phải ghi vào AR-02 §3. Ngân sách mỗi file: TD-11 PERF-R-037 (≤ 3 MB); tách theo vũ khí / boss: ANM-Q-3.
- <a id="anm-r-051"></a>**ANM-R-051** Catalogue máy đọc sinh từ §4 (giống `tools/art/skeletons/*.json` sinh từ AR-02):

```ts
// tools/art/anim-list.json — sinh từ AR-03 §4; art:check và client đọc
type AnimPhase = 'PHASE_0' | 'PHASE_1' | 'PHASE_2' | 'PHASE_3' | 'ALPHA' | 'BETA_1' | 'BETA_2' | 'POST_LAUNCH';
type AnimMarker = 'hit' | `hit_${number}` | 'cancel' | 'vfx_spawn';
interface AnimClipSpec {
  id: string;                         // ANM_* (ANM-R-010)
  skeleton: SkeletonGroup;            // GD-12 §3
  weapon: WeaponType | null;          // null = clip chung / biến thể gốc
  archetype?: AiArchetype;            // chỉ quái HUMANOID theo archetype
  modelKey?: string;                  // chỉ clip riêng model (boss)
  actionStates: string[];             // ActionState GD-07 §4 / trạng thái AI GD-12 §4 / FlightState GD-31 dùng clip này
  frames: number;                     // tham chiếu ở 30 fps
  loop: boolean;
  markers: AnimMarker[];              // marker bắt buộc
  rootMotion: boolean;                // ANM-R-040
  durationParam?: string;             // tham số gamedata khóa thời lượng (ANM-R-021), vd. "world/flight.json → takeOffSec"
  source: 'MIXAMO' | 'AI' | 'HAND';
  priority: 'P0' | 'P1' | 'P2';
  phase: AnimPhase;
  fallback: string[];                 // chuỗi dự phòng §5.4 (mã clip)
  additionsToAst02?: boolean;         // true = clip (+) chưa có ở AR-02 §5
}
// checks: { rootDriftMaxM, loopPoseMaxDeg, rateMin, rateMax, durationTolPct } — ngưỡng của art:check (ANM-Q-6 đã chốt); rateMin / rateMax lấy từ world/client.json → anim (TD-05 §13)
```

- **Ưu tiên:** `P0` = thiếu thì không chơi được / chặn mốc phase; `P1` = cần cho chất lượng mốc phase, có dự phòng chấp nhận được; `P2` = đánh bóng, có thể lùi phase sau.
- **Nguồn:** `MIXAMO` (thư viện Mixamo, retarget qua `tools/art/retarget-map.json`); `AI` (sinh chuyển động — **chưa có mô hình nào đăng ký ở TD-15 §4.5**, nên tới khi đăng ký thì dòng `AI` làm bằng tay); `HAND` (key tay trong Blender). Mixamo không có thú 4 chân / chim / rắn → quái phi người luôn `HAND` (hoặc `AI` khi được đăng ký).

---

## 3. Ánh xạ trạng thái → clip

### 3.1. `ActionState` (GD-07 §4) → clip `HUMANOID`
`{W}` = `WeaponType` đang cầm; không cầm vũ khí → biến thể gốc (bỏ `{W}_`).

| `ActionState` | Clip | Ghi chú |
|---------------|------|---------|
| `IDLE` | `{W}_IDLE` ngoài chiến đấu · `{W}_IDLE_COMBAT` trong chiến đấu (`combatTimeoutSec`, GD-06) | Đổi bằng blend, không có clip rút / cất vũ khí (ANM-Q-4) |
| `MOVING` | Blend tree theo tốc độ: `{W}_IDLE_COMBAT` → `{W}_WALK` → `{W}_RUN`; hướng: `{W}_RUN_STRAFE_L/R`, `{W}_RUN_BACK` (phụ thuộc AST-Q-1) | §5.2 |
| `BASIC_ATTACK` (bước n) | `{W}_ATTACK_n`, `n = 1..len(WeaponDef.basicCombo)` | Kiếm 4 bước, quạt 3 bước (GD-07 §5.4) — ANM-R-060 |
| `HEAVY_CHARGE` | `{W}_HEAVY_CHARGE_LOOP` | Mức 1 "chạm" không tụ → vào thẳng `HEAVY_RELEASE` |
| `CASTING` / `ACTIVE` (kỹ năng) | Niệm (`castTimeSec > 0`) hoặc giữ phím bí pháp (`holdToCastSec`) → `{W}_CAST_CHANNEL_LOOP`; windup + active → clip theo `Shape` (§3.2) | Đánh mạnh khi thả: `{W}_HEAVY_RELEASE` |
| `RECOVERY` | Đuôi của clip tấn công / cast; hết clip mà chưa hết `recoverySec` → `{W}_IDLE_COMBAT` | Co giãn đuôi §5.3 |
| `DODGING` | `DODGE_F/B/L/R` theo `dir` so với hướng mặt; né lần 2 (khinh công Trúc Cơ, GD-06 `dodge.doubleWindowSec`) phát lại clip từ đầu | Không theo vũ khí |
| `BLOCKING` | `{W}_BLOCK_LOOP`; đỡ trúng → `{W}_BLOCK_HIT`; đỡ hoàn hảo → `{W}_PARRY_PERFECT` | |
| `STAGGERED` | `FLINCH` → `HIT_LIGHT` (**additive**, không đổi trạng thái — FLINCH không ngắt hành động, GD-06 SF-R-093); `STAGGER` → `HIT_HEAVY` | |
| `KNOCKED_BACK` | `KNOCKBACK` (tại chỗ; server đẩy vị trí) | |
| `KNOCKED_DOWN` | `KNOCKDOWN` → `GETUP` (tổng = thời gian ngã) | `stats/params.json → reactionSec.KNOCKDOWN` gồm cả `GETUP` (GD-06 SF-R-093) |
| `STUNNED` | `STUN_LOOP` | Cũng dùng cho choáng sau `FALLING` (GD-31 `fallStunSec`) |
| `ROOTED` | `{W}_IDLE_COMBAT` + VFX trói (AR-04) | ROOTED vẫn thi triển được (GD-07 SK-R-070 không chặn) — AR-02 gộp vào `STUN_LOOP`, xem ANM-Q-5 |
| `SILENCED` | Không clip riêng: clip của hành động đang làm + VFX status | Vẫn di chuyển / đánh thường / né (GD-07 §4) |
| `POISE_BROKEN` | `POISE_BREAK` (một lần, khóa theo `stats/params.json → poiseBreakStunSec.PLAYER`) | |
| `CHANNELING` | Đả tọa / đột phá (vận công) → `MEDITATE_ENTER` → `MEDITATE_LOOP` → `MEDITATE_EXIT`; hái → `GATHER_LOOP`; tương tác giữ (cứu đồng đội 4s GD-28 DZ-R-053, mở / đọc) → `INTERACT`; luyện đan → `CRAFT_ALCHEMY_LOOP`; luyện khí → `CRAFT_FORGE_LOOP` + `CRAFT_FORGE_STRIKE` mỗi nhịp búa | Phiên nghề ở `CHANNELING` (GD-10 AL-R-031) |
| `DOWNED` | `DOWNED_ENTER` → `DOWNED_IDLE` / `DOWNED_CRAWL` (blend theo tốc độ, bò 1 m/s) | GD-28 §5.5 |
| `DORMANT_GUARD` | `DORMANT_LOOP` | GD-28 §5.7 |
| `DEAD` | `DEATH` (giữ frame cuối) | Dùng cho trọng thương lẫn chết thật; tọa hóa / binh giải: ANM-Q-8 |
| *(ngoài `ActionState`)* `FlightState` GD-31 | `TAKING_OFF` → `FLIGHT_TAKEOFF`; `FLYING` → `FLIGHT_LOOP`; `LANDING` → `FLIGHT_LAND`; `FALLING` → `FLIGHT_FALL`; `GROUNDED` → theo `ActionState` | Bay chỉ từ `IDLE` / `MOVING` (GD-31 §4) |
| *(ngoài `ActionState`)* Biểu cảm `EMOTE` (GD-19) | `EMOTE_*` | Chỉ khi `IDLE`; hủy khi có lệnh khác |

- <a id="anm-r-060"></a>**ANM-R-060** Số clip `{W}_ATTACK_n` của mỗi vũ khí **bằng** `len(WeaponDef.basicCombo)` trong `skills/weapons.json` (GD-07 §3). `art:check` đối chiếu với gamedata. Đồng bộ GD-07 SK-R-054. (AR-02 §5.2 ghi `ATTACK_1..3` — kiếm 4 bước nên cần `ATTACK_4`; AR-02 còn phải sửa, ANM-Q-1.)

### 3.2. `Shape` (GD-07 §3) → clip kỹ năng chung
AR-02 §5.2: clip cast **dùng chung cho mọi kỹ năng theo `Shape`**; kỹ năng không có clip riêng (SK-R-002 — dữ liệu thành phần).

| `Shape.type` | Clip `{W}_…` | Kỹ năng MVP dùng (GD-07 §7.2) |
|--------------|--------------|-------------------------------|
| `PROJECTILE` | `CAST_PROJECTILE` | `SKL_SWORD_QI_SLASH`, `SKL_FIRE_ORB` |
| `CONE` | `CAST_CONE` | `SKL_SWEEPING_BLADE`, `SKL_WIND_BLADE` |
| `LINE` | `CAST_CONE` (mặc định tạm) | Chỉ đánh mạnh kiếm mức 1 (dùng `HEAVY_RELEASE`) — ANM-Q-2 |
| `GROUND_CIRCLE` | `CAST_GROUND` | `SKL_SWORD_RAIN`, `SKL_FROST_FIELD`, `SKL_THUNDER_STRIKE`, `SKL_HEALING_RAIN` |
| `DASH` | `DASH_STRIKE` | `SKL_FLASH_STEP` |
| `SELF_CIRCLE` | `CAST_SELF` | `SKL_SWORD_NET` |
| `SELF` | `CAST_SELF` | `SKL_IRON_BODY`, `SKL_BURN_LIFE` (sau `CAST_CHANNEL_LOOP` giữ phím 1s) |
| `SUMMON` | `CAST_SELF` (mặc định tạm) | `SKL_FLYING_SWORD`, `SKL_EARTH_WALL` — ANM-Q-2 |

- <a id="anm-r-061"></a>**ANM-R-061** Kỹ năng `weaponReq = ANY` dùng clip của vũ khí đang cầm. Tuyệt kỹ (Beta, `isUltimate`) dùng bảng trên theo `Shape` cho tới khi có quyết định khác (ANM-Q-2).

### 3.3. Trạng thái quái (GD-12 §4) → clip
| Trạng thái AI | Clip | Ghi chú |
|---------------|------|---------|
| `SPAWNING` | `IDLE` + VFX hiện dần | Không clip riêng |
| `IDLE` / `PATROL` | `IDLE` / `WALK` (`BIRD`: `FLY_IDLE` / `FLY_MOVE`; `SERPENT`: `IDLE` / `MOVE`; `NONE`: `IDLE_BOB` / `MOVE_BOB`) | |
| `ALERT` | `ALERT` (0,5s; nhóm không có → `IDLE`) | Boss `ANNOUNCE` cũng dùng `ALERT` |
| `COMBAT` | Theo `ActionState` (GD-07 §4, quái dùng cùng máy trạng thái hành động); đòn chọn theo §3.4 | |
| `RETURNING` / `FLEEING` | `RUN` (`BIRD` `FLY_MOVE`; `SERPENT` `MOVE`; `NONE` `MOVE_BOB`) | |
| `DEAD` | `DEATH` (giữ tư thế 3s rồi tan — VFX) | |
| Boss đổi pha (EN-R-090) | `{MODEL_KEY}_PHASE_TRANSITION` trong lúc chạy `onEnter` | |
| Boss Kiệt Sức (SB-R-026, `EXHAUSTED`) | `{MODEL_KEY}_EXHAUSTED_LOOP` | Beta 2 |

### 3.4. Đòn quái → clip
- <a id="anm-r-062"></a>**ANM-R-062** Mặc định chọn clip đòn quái theo `Shape` của `SkillDef` (quái dùng chung định dạng, GD-12 `MonsterSkillRef`):

| Nhóm | `DASH` | `PROJECTILE` | `GROUND_CIRCLE` / `SELF_CIRCLE` / `SUMMON` | `CONE` / `LINE` / khác |
|------|--------|--------------|--------------------------------------------|------------------------|
| `QUADRUPED` | `ATTACK_LUNGE` | `ATTACK_BITE` | `ALERT` | `ATTACK_BITE` |
| `BIRD` | `ATTACK_DIVE` | `ATTACK_WING` | `ATTACK_WING` | `ATTACK_WING` |
| `SERPENT` | `ATTACK_STRIKE` | `ATTACK_SPIT` | `ATTACK_SPIT` | `ATTACK_STRIKE` |
| `NONE` | `ATTACK_SLAM` | `ATTACK_PULSE` | `ATTACK_PULSE` | `ATTACK_SLAM` |
| `HUMANOID` (quái) | `{ARCHETYPE}_ATTACK_2` | `{ARCHETYPE}_ATTACK_1` | `{ARCHETYPE}_ATTACK_2` | `basicSkillId` → `{ARCHETYPE}_ATTACK_1`, chiêu khác → `{ARCHETYPE}_ATTACK_2` |

- <a id="anm-r-063"></a>**ANM-R-063** Chiêu boss có clip riêng (§4.9) cần **ánh xạ `skillId` → clip**. `SkillDef` / `MonsterSkillRef` hiện **không có trường hoạt ảnh** → không tự thêm trường; ánh xạ nằm trong catalogue `anim-list.json → skillClips` (client đọc, TD-05 CL-R-074; GD-12 EN-R-095), thiếu thì dùng ANM-R-062 (ANM-Q-10 đã chốt).

---

## 4. Bảng clip

Cột: **Mã** (bỏ tiền tố `ANM_`), **Frame** (30 fps), **Loop**, **Marker** bắt buộc, **Root** (ANM-R-040), **Khóa thời lượng** (ANM-R-021), **Nguồn**, **Ưu tiên**, **Phase**. (+) = bổ sung so với AR-02.

### 4.1. `HUMANOID` — clip chung (không theo vũ khí)

| Mã (`ANM_HUMANOID_…`) | Mô tả | Frame | Loop | Marker | Root | Khóa thời lượng | Nguồn | Ưu tiên | Phase |
|---|---|:---:|:---:|---|:---:|---|---|:---:|---|
| `IDLE` | Biến thể gốc: đứng thở (Phàm Nhân, NPC, dự phòng) | 60 | ✅ | — | | | Mixamo | P0 | Phase 0 |
| `WALK` | Biến thể gốc: đi chậm (`IntentFrame.walk`) | 32 | ✅ | — | | | Mixamo | P0 | Phase 0 |
| `RUN` | Biến thể gốc: chạy (tham chiếu `moveSpeed` 6 m/s) | 20 | ✅ | — | | | Mixamo | P0 | Phase 0 |
| `DODGE_F` | Lướt tới, thân thấp, tàn ảnh | 12 | | — | ✅ 4m | `stats/params.json → dodge.durationSec` (GD-06 SF-R-104) | Mixamo + tay | P0 | Phase 1 |
| `DODGE_B` | Lướt lùi | 12 | | — | ✅ 4m | như trên | Mixamo + tay | P1 | Phase 1 |
| `DODGE_L` | Lướt trái | 12 | | — | ✅ 4m | như trên | Mixamo + tay | P1 | Phase 1 |
| `DODGE_R` | Lướt phải | 12 | | — | ✅ 4m | như trên | Mixamo + tay | P1 | Phase 1 |
| `HIT_LIGHT` | Giật nhẹ thân trên (FLINCH, phát additive) | 9 | | — | | | Mixamo | P0 | Phase 1 |
| `HIT_HEAVY` | Choáng lùi nửa bước (STAGGER 0,4s) | 12 | | — | | `stats/params.json → reactionSec.STAGGER` | Mixamo | P0 | Phase 1 |
| `KNOCKBACK` | Bị đẩy, trượt chân (0,5s; tại chỗ) | 15 | | — | | `reactionSec.KNOCKBACK` | Mixamo | P0 | Phase 1 |
| `KNOCKDOWN` | Ngã ngửa, nằm | 18 | | — | | `reactionSec.KNOCKDOWN` (tổng cùng `GETUP`) | Mixamo | P0 | Phase 1 |
| `GETUP` | Bật dậy | 18 | | — | | như trên | Mixamo | P0 | Phase 1 |
| `STUN_LOOP` | Lảo đảo, sao quay (VFX) | 30 | ✅ | — | | | Mixamo | P1 | Phase 1 |
| `POISE_BREAK` | Phá thế: khuỵu gối, thủ thế vỡ | 30 | | — | | `stats/params.json → poiseBreakStunSec.PLAYER` | Tay | P0 | Phase 1 |
| `DEATH` | Gục xuống, giữ tư thế | 40 | | — | | | Mixamo | P0 | Phase 1 |
| `DOWNED_ENTER` | Quỵ xuống hấp hối | 20 | | — | | | Mixamo | P1 | Phase 1 |
| `DOWNED_IDLE` (+) | Nằm thở dốc, chống tay | 60 | ✅ | — | | | Tay | P1 | Phase 1 |
| `DOWNED_CRAWL` | Bò chậm (tham chiếu 1 m/s) | 40 | ✅ | — | | | Mixamo | P1 | Phase 1 |
| `GATHER_LOOP` | Cúi hái linh thảo (cũng dùng cho khoáng mạch tới khi chốt ANM-Q-11) | 45 | ✅ | — | | | Mixamo | P1 | Phase 1 |
| `INTERACT` | Tương tác giữ: đưa tay vận khí (cứu đồng đội, mở rương, đọc bia) | 40 | ✅ | — | | | Mixamo | P1 | Phase 1 |
| `MEDITATE_ENTER` | Ngồi xuống xếp bằng | 30 | | — | | | Mixamo + tay | P1 | Phase 2 |
| `MEDITATE_LOOP` | Đả tọa / vận công đột phá: thở, linh khí quanh thân (VFX) | 90 | ✅ | — | | | Tay | P1 | Phase 2 |
| `MEDITATE_EXIT` (+) | Đứng dậy khi dừng đả tọa (CU-R-030) | 20 | | — | | | Mixamo + tay | P2 | Phase 2 |
| `DORMANT_LOOP` | Quy tức: tĩnh tọa phát sáng (biến thể `MEDITATE_LOOP`) | 90 | ✅ | — | | | Tay | P2 | Phase 3 |
| `CRAFT_ALCHEMY_LOOP` (+) | Đứng trước đan lô, tay thúc hỏa (GD-10 minigame hỏa hầu) | 60 | ✅ | — | | | AI / tay | P1 | Phase 3 |
| `CRAFT_FORGE_LOOP` (+) | Đứng bên đe, cầm búa chờ nhịp | 60 | ✅ | — | | | Tay | P2 | Alpha |
| `CRAFT_FORGE_STRIKE` (+) | Một nhát búa, phát theo mỗi input nhịp (GD-11 FG-R-020) | 12 | | `vfx_spawn` | | | Tay | P1 | Alpha |
| `FLIGHT_TAKEOFF` | Triệu phi kiếm, nhảy lên (đứng trên vật bay tại `socket_feet_fx`) | 30 | | — | | `world/flight.json → takeOffSec` | Tay | P1 | Beta 2 |
| `FLIGHT_LOOP` | Đứng trên phi kiếm, áo bay | 60 | ✅ | — | | | Tay | P1 | Beta 2 |
| `FLIGHT_LAND` | Hạ cánh, bước xuống | 15 | | — | | `world/flight.json → landSec` | Tay | P1 | Beta 2 |
| `FLIGHT_FALL` | Bị bắn rơi, chới với | 24 | | — | | `world/flight.json → fallSec` | Tay | P1 | Beta 2 |
| `EMOTE_SALUTE` (+) | *Đề xuất:* ôm quyền thi lễ | 60 | | — | | | Mixamo + tay | P2 | Beta 2 |
| `EMOTE_BOW` (+) | *Đề xuất:* cúi chào | 60 | | — | | | Mixamo | P2 | Beta 2 |
| `EMOTE_WAVE` (+) | *Đề xuất:* vẫy tay | 60 | | — | | | Mixamo | P2 | Beta 2 |

`EMOTE_*`: GD-19 mới có `CosmeticCategory = 'EMOTE'`, chưa có danh sách `COS_*` — ba clip trên chỉ là đề xuất để ước lượng (ANM-Q-12).

### 4.2. `HUMANOID` — clip theo vũ khí (mẫu cho mỗi `WeaponType`)
Mỗi dòng sinh một clip `ANM_HUMANOID_{W}_{ACTION}`. Frame / marker là tham chiếu; thời điểm thật do dữ liệu (§5.3).

| Hành động (`{W}_…`) | Mô tả | Frame | Loop | Marker | Root | Nguồn | Ưu tiên |
|---|---|:---:|:---:|---|:---:|---|:---:|
| `IDLE` | Đứng cầm vũ khí, ngoài chiến đấu | 60 | ✅ | — | | Mixamo + tay | P0 |
| `IDLE_COMBAT` | Thủ thế | 40 | ✅ | — | | Tay | P0 |
| `WALK` | Đi chậm cầm vũ khí | 32 | ✅ | — | | Mixamo | P1 |
| `RUN` | Chạy cầm vũ khí (6 m/s) | 20 | ✅ | — | | Mixamo | P0 |
| `RUN_STRAFE_L` | Chạy ngang trái, thân hướng con trỏ | 20 | ✅ | — | | Mixamo | P2 (AST-Q-1) |
| `RUN_STRAFE_R` | Chạy ngang phải | 20 | ✅ | — | | Mixamo | P2 (AST-Q-1) |
| `RUN_BACK` | Chạy lùi | 20 | ✅ | — | | Mixamo | P2 (AST-Q-1) |
| `ATTACK_1` … `ATTACK_n` | Combo đánh thường, `n = len(basicCombo)`; bước cuối là đòn kết (to, rõ) | 20 (bước cuối 26) | | `hit` (`cancel` tùy chọn) | | Tay | P0 |
| `HEAVY_CHARGE_LOOP` | Tụ lực đánh mạnh (giáp `LIGHT`) | 30 | ✅ | — | | Tay | P0 |
| `HEAVY_RELEASE` | Thả đòn mạnh (kiếm: đâm `LINE`; quạt: phẩy `CONE`) | 24 | | `hit` | | Tay | P0 |
| `CAST_PROJECTILE` | Phóng (kiếm khí / hỏa cầu) | 21 | | `hit` | | Tay | P0 |
| `CAST_CONE` | Quét hình quạt | 24 | | `hit` | | Tay | P0 |
| `CAST_GROUND` | Chỉ tay / kiếm lên trời, giáng xuống vùng | 30 | | `hit` | | Tay | P0 |
| `CAST_SELF` | Vận công quanh thân / hộ thể | 24 | | `hit` | | Tay | P1 |
| `CAST_CHANNEL_LOOP` | Niệm / giữ phím bí pháp | 30 | ✅ | — | | Tay | P1 |
| `DASH_STRIKE` | Lướt chém (`DASH`, i-frame) | 18 | | `hit` (đầu đoạn lướt) | ✅ | Tay | P0 |
| `BLOCK_LOOP` | Hộ thể / đỡ | 30 | ✅ | — | | Tay | P0 |
| `BLOCK_HIT` | Đỡ trúng đòn | 9 | | — | | Tay | P1 |
| `PARRY_PERFECT` | Đỡ hoàn hảo, bật đòn | 15 | | `vfx_spawn` | | Tay | P1 |

**Số clip mỗi vũ khí = 18 + n.** Áp dụng:

| `WeaponType` | `n` (GD-07 §5.4) | Số clip | Phase | Ghi chú |
|--------------|:---:|:---:|---|---|
| `SWORD` | 4 | 22 | Phase 1 (20 clip); `CAST_SELF`, `CAST_CHANNEL_LOOP` ở Phase 2 | Phase 1 chỉ có 4 kỹ năng: phóng, vùng, quạt, lướt (PLAN §7 Phase 1) |
| `FAN` | 3 | 21 | Phase 2 | GD-07 §16: quạt ở MVP (PLAN Phase 2 "vũ khí thứ 2") |
| `DUAL_BLADE` | ? (chưa có `WeaponDef`) | ~22 (giả định n = 4) | Beta 1 (giả định) | GD-07 §16 "Beta" — ANM-Q-13 |
| `FIST` | ? | ~22 (giả định n = 4) | Beta 1 (giả định) | Như trên |
| `ZITHER` | ? | — | Sau launch | GD-07 §3 chú thích; chưa thuộc glossary |

> AR-02 §5.2 ước "~45 clip / vũ khí": con số đó tương ứng **cả bộ người chơi cầm một vũ khí** (clip theo vũ khí ~22 + clip chung ~25 tới MVP), khớp với §6.

### 4.3. `HUMANOID` — quái hình người theo archetype
Dùng biến thể gốc của §4.1 cho di chuyển, phản ứng, chết (AR-02 §5.3); thêm đòn theo archetype. Vũ khí cầm (cuốc mỏ, cung — AR-01 §5.2) là mesh gắn `socket_weapon_r/l`, không phải `WeaponType`.

| Mã (`ANM_HUMANOID_…`) | Quái V1 dùng (GD-12 §7.1) | Frame | Marker | Nguồn | Ưu tiên | Phase |
|---|---|:---:|---|---|:---:|---|
| `MELEE_SKIRMISHER_ATTACK_1` / `_ATTACK_2` | `MON_BANDIT_MINER` | 20 / 24 | `hit` | Mixamo + tay | P1 | Phase 2 |
| `MELEE_BRUISER_ATTACK_1` / `_ATTACK_2` | `MON_RESENTFUL_SOLDIER`, tinh anh `MON_E_FALLEN_GENERAL` (giả định) | 24 / 30 | `hit` | Mixamo + tay | P1 | Phase 2 |
| `TANK_ATTACK_1` / `_ATTACK_2` | `MON_STONE_PUPPET`, `MON_E_STONE_KING` | 26 / 30 | `hit` | Tay | P1 | Phase 2 |
| `AMBUSHER_ATTACK_1` / `_ATTACK_2` | `MON_FOG_WRAITH` | 18 / 24 | `hit` | Tay | P1 | Phase 2 |
| `RANGED_KITER_ATTACK_1` / `_ATTACK_2` | `MON_GHOST_ARCHER` (bắn cung) | 24 / 30 | `hit` | Mixamo + tay | P1 | Phase 2 |

`ATTACK_1` = đòn cơ bản (`basicSkillId`, có "rung vũ khí" cảnh báo — EN-R-072); `ATTACK_2` = chiêu kỹ năng. Archetype `CASTER`, `SWARM`, `PASSIVE` hình người chưa có quái V1 → làm khi có nội dung. Chấp Pháp `MON_E_ENFORCER*` (Beta 1): ANM-Q-14.

### 4.4. `QUADRUPED`

| Mã (`ANM_QUADRUPED_…`) | Mô tả | Frame | Loop | Marker | Root | Nguồn | Ưu tiên | Phase |
|---|---|:---:|:---:|---|:---:|---|:---:|---|
| `IDLE` | Đứng, vẫy đuôi | 60 | ✅ | — | | Tay | P0 | Phase 1 |
| `WALK` | Đi | 32 | ✅ | — | | Tay | P0 | Phase 1 |
| `RUN` | Chạy | 16 | ✅ | — | | Tay | P0 | Phase 1 |
| `ALERT` | Gầm / hú (cũng dùng gọi bầy) | 15 | | — | | Tay | P1 | Phase 1 |
| `ATTACK_BITE` | Cắn / vồ ngắn | 20 | | `hit` | | Tay | P0 | Phase 1 |
| `ATTACK_LUNGE` | Lao / vồ xa (`DASH`) | 24 | | `hit` | ✅ | Tay | P0 | Phase 1 |
| `HIT` | Giật lùi | 9 | | — | | Tay | P0 | Phase 1 |
| `KNOCKDOWN` | Ngã nghiêng | 18 | | — | | Tay | P1 | Phase 1 |
| `GETUP` | Lồm cồm dậy | 18 | | — | | Tay | P1 | Phase 1 |
| `STUN_LOOP` (+) | Choáng / phá thế (EN-R-094: cửa sổ sát thương chính) | 30 | ✅ | — | | Tay | P1 | Phase 1 |
| `DEATH` | Ngã, giữ tư thế 3s | 36 | | — | | Tay | P0 | Phase 1 |

Quái V1: `MON_BAMBOO_RAT`, `MON_WILD_BOAR`, `MON_RED_FOX`, `MON_HUNGRY_WOLF`, tinh anh `MON_E_BAMBOO_KING`, `MON_E_BLOOD_WOLF`. Cùng một bộ clip cho mọi thân hình 4 chân (chuột → heo rừng); khác tỷ lệ bằng scale model. Phase 1 giả định dùng quái F01 (ANM-Q-15).

### 4.5. `BIRD`

| Mã (`ANM_BIRD_…`) | Mô tả | Frame | Loop | Marker | Root | Nguồn | Ưu tiên | Phase |
|---|---|:---:|:---:|---|:---:|---|:---:|---|
| `FLY_IDLE` | Vỗ cánh tại chỗ | 24 | ✅ | — | | Tay | P0 | Phase 2 |
| `FLY_MOVE` | Bay tới | 20 | ✅ | — | | Tay | P0 | Phase 2 |
| `ATTACK_DIVE` | Bổ nhào (`DASH`) | 24 | | `hit` | ✅ | Tay | P0 | Phase 2 |
| `ATTACK_WING` | Quạt cánh / phóng lông | 20 | | `hit` | | Tay | P0 | Phase 2 |
| `HIT` | Giật trên không | 9 | | — | | Tay | P0 | Phase 2 |
| `STUN_LOOP` (+) | Choáng, chao đảo | 24 | ✅ | — | | Tay | P2 | Phase 2 |
| `DEATH` | Rơi xuống đất, nằm | 30 | | — | | Tay | P0 | Phase 2 |

Quái V1: `MON_MINE_BAT`, `MON_NIGHT_OWL`, `MON_BLOOD_CROW`.

### 4.6. `SERPENT`

| Mã (`ANM_SERPENT_…`) | Mô tả | Frame | Loop | Marker | Root | Nguồn | Ưu tiên | Phase |
|---|---|:---:|:---:|---|:---:|---|:---:|---|
| `IDLE` | Cuộn, lưỡi thè | 60 | ✅ | — | | Tay | P0 | Phase 1 |
| `MOVE` | Trườn (sóng spine_01–16) | 30 | ✅ | — | | Tay | P0 | Phase 1 |
| `ATTACK_STRIKE` | Mổ | 18 | | `hit` | | Tay | P0 | Phase 1 |
| `ATTACK_SPIT` | Phun độc | 24 | | `hit` | | Tay | P1 | Phase 1 |
| `HIT` | Giật | 9 | | — | | Tay | P0 | Phase 1 |
| `STUN_LOOP` (+) | Choáng | 30 | ✅ | — | | Tay | P2 | Phase 1 |
| `DEATH` | Duỗi, nằm | 30 | | — | | Tay | P0 | Phase 1 |

Quái V1: `MON_GREEN_SNAKE` (F01).

### 4.7. `NONE` (không xương — hoạt ảnh transform của node gốc, AR-02 §4)

| Mã (`ANM_NONE_…`) | Mô tả | Frame | Loop | Marker | Nguồn | Ưu tiên | Phase |
|---|---|:---:|:---:|---|---|:---:|---|
| `IDLE_BOB` | Nhấp nhô / thở (scale y) | 60 | ✅ | — | Tay | P0 | Phase 2 |
| `MOVE_BOB` (+) | Nhảy cóc / bò ngang khi di chuyển | 20 | ✅ | — | Tay | P0 | Phase 2 |
| `ATTACK_SLAM` | Nhún rồi đập | 24 | | `hit` | Tay | P0 | Phase 2 |
| `ATTACK_PULSE` (+) | Phình rồi phóng (phun độc, pháp thuật) | 24 | | `hit` | Tay | P0 | Phase 2 |
| `HIT_SQUASH` (+) | Nén – giãn khi trúng | 8 | | — | Tay | P1 | Phase 2 |
| `DEATH_CRUMBLE` | Sụp / tan (kèm VFX) | 30 | | — | Tay | P0 | Phase 2 |

Quái V1: `MON_RIVER_CRAB` (`TANK`), `MON_WATER_SPRITE` (`CASTER`), `MON_POISON_TOAD` (`RANGED_KITER`) — cả ba **di chuyển và có đòn tầm xa**, nên AR-02 §5.3 (chỉ `IDLE_BOB`, `ATTACK_SLAM`, `DEATH_CRUMBLE`) không đủ; ba clip (+) cần đồng bộ ngược. Khiên mai cua (`TANK`) dùng `ATTACK_PULSE` + VFX.

### 4.8. Boss — clip riêng theo model
- <a id="anm-r-070"></a>**ANM-R-070** Boss dùng **bộ clip nhóm** của `skeleton` (di chuyển, `HIT`, `STUN_LOOP`, `DEATH`) + **clip riêng mỗi chiêu** (AR-02 §5.3) + `PHASE_TRANSITION`. Chiêu có cảnh báo dài (EN-R-070: tới 1,0s; cơ chế toàn đấu trường 2,0s): nếu cần phát chậm hơn `rateMin` (§5.3), tách thành `{CHIÊU}_CHARGE` (lặp, giữ tư thế anticipation đọc được) + `{CHIÊU}_RELEASE` (có `hit`), không kéo giãn một clip ngắn.
- <a id="anm-r-071"></a>**ANM-R-071** Mỗi pha boss có chiêu "đứng hở" ≥ 1,5s (EN-R-094) → boss có `RECOVER_LOOP` (thở dốc, hạ thủ) để đuôi chiêu không bị đứng cứng.

**`MON_B_FOX_MATRIARCH`** (Hồ Yêu Mẫu, `QUADRUPED` — theo GD-25 §7.2 nhóm lai `BEAST_QUADRUPED`; chiêu theo GD-12 §7.2):

| Mã (`ANM_QUADRUPED_FOX_MATRIARCH_…`) | Chiêu / dùng cho | Frame | Loop | Marker | Root | Nguồn | Ưu tiên | Phase |
|---|---|:---:|:---:|---|:---:|---|:---:|---|
| `CLAW_COMBO` | Hồ Trảo liên kích (quạt 3m, **3 nhát**) | 36 | | `hit_1`, `hit_2`, `hit_3` | | Tay | P0 | Phase 3 |
| `FIREBALL` | Hồ Hỏa Cầu (3 đạn) | 27 | | `hit` | | Tay | P0 | Phase 3 |
| `PHANTOM_DASH` | Mị Ảnh Lướt (lao 10m, KNOCKDOWN) | 30 | | `hit` | ✅ | Tay | P0 | Phase 3 |
| `RECOVER_LOOP` | Đứng hở 1,8s sau Mị Ảnh Lướt (ANM-R-071) | 30 | ✅ | — | | Tay | P0 | Phase 3 |
| `NINE_TAIL_RAIN` | Cửu Vĩ Hỏa Vũ (5 vùng, cảnh báo 1,0s); thú boss dùng lại (3 vùng) | 36 | | `hit` | | Tay | P0 | Phase 3 |
| `FIRE_BURST_CHARGE` | Hồ Hỏa Bạo — tụ 2,0s ở tâm đấu trường (không thể đỡ) | 30 | ✅ | — | | Tay | P0 | Phase 3 |
| `FIRE_BURST_RELEASE` | Hồ Hỏa Bạo — bùng nổ | 24 | | `hit` | | Tay | P0 | Phase 3 |
| `PHASE_TRANSITION` | Đổi pha P2 (gọi Hồ Ảnh) / P3 | 45 | | `vfx_spawn` | | Tay | P1 | Phase 3 |
| `EXHAUSTED_LOOP` | Kiệt Sức 8s (thu phục, SB-R-026) | 60 | ✅ | — | | Tay | P1 | Beta 2 |

`MON_FOX_ILLUSION` (Hồ Ảnh) dùng bộ `QUADRUPED` chung. `ANNOUNCE` → `ALERT`; `MOVE_TO` → `RUN`.

**Boss khác** — chưa đủ dữ liệu (GD-12 không ghi `skeleton` và chiêu cụ thể); ước lượng để lập kế hoạch, mã clip đặt khi GD-12 bổ sung (ANM-Q-15):

| Boss | Bối cảnh | Bộ xương | Ước số clip riêng | Phase (giả định) |
|------|----------|----------|:---:|---|
| Boss thử nghiệm Phase 1 ("3–4 chiêu có telegraph", PLAN §7) | Chưa chốt — giả định `MON_B_MINE_TYRANT` (BC01, 2 pha) | ? | 6 | Phase 1 |
| `MON_B_SWORD_SPIRIT` | BC03, 3 pha | ? (gợi ý `HUMANOID` + kiếm) | 8 | Phase 3 |
| `MON_B_REMNANT` | Tàn Hồn Cảnh (GD-26 §5.15) | `HUMANOID` (CL-R-070: tàn hồn dùng bộ người) | 0 — dùng thư viện người chơi theo vũ khí | Theo GD-26 |
| `MON_CAVE_WARDEN` | Cửa động phủ (GD-30), 1 pha | ? | 5 | Beta 1 |
| `MON_B_HEAVEN_AVATAR` | Thiên Đạo Hóa Thân (GD-32), 3 pha | ? | 8 | Beta 1 |
| World boss (GD-23) | Thú triều / world boss | ? | ? | Beta 2 — chưa ước lượng |

Tinh anh: dùng bộ clip nhóm, model to 1,3× (AR-01 §5.2); không clip riêng trừ khi GD-12 thêm chiêu đặc thù.

### 4.9. Linh thú (GD-25)
- <a id="anm-r-080"></a>**ANM-R-080** Linh thú **dùng lại toàn bộ clip** của quái cùng `SkeletonGroup` (model + bộ xương của `sourceMonsterId`, GD-25 §1; đòn tự động / chiêu lệnh chọn clip theo ANM-R-062). Thú boss `BST_FOX_MATRIARCH` dùng clip của `MON_B_FOX_MATRIARCH` (scale 0,55; chiêu lệnh Cửu Vĩ Hỏa Vũ → `NINE_TAIL_RAIN`). Chỉ thêm hai clip mỗi nhóm (AR-02 §5.3):

| Mã | Mô tả | Frame | Loop | Nguồn | Ưu tiên | Phase |
|---|---|:---:|:---:|---|:---:|---|
| `ANM_QUADRUPED_FOLLOW_IDLE` | Ngồi / nằm chờ cạnh chủ (SB-R-042: cách 2–4m) | 60 | ✅ | Tay | P1 | Beta 2 |
| `ANM_QUADRUPED_HAPPY` | Thân mật bậc 10 — "hoạt ảnh thân thiết" (SB-R-050) | 45 | | Tay | P2 | Beta 2 |
| `ANM_SERPENT_FOLLOW_IDLE` | Cuộn chờ | 60 | ✅ | Tay | P1 | Beta 2 |
| `ANM_SERPENT_HAPPY` | Ngóc đầu, lắc | 45 | | Tay | P2 | Beta 2 |
| `ANM_BIRD_FOLLOW_IDLE` | Lượn chậm quanh chủ | 48 | ✅ | Tay | P1 | Beta 2 |
| `ANM_BIRD_HAPPY` | Nhào lộn | 45 | | Tay | P2 | Beta 2 |
| `ANM_NONE_FOLLOW_IDLE` | Nhấp nhô chậm (cua, tinh linh nước) | 60 | ✅ | Tay | P1 | Beta 2 |
| `ANM_NONE_HAPPY` | Nảy vui | 30 | | Tay | P2 | Beta 2 |

Khi chủ cất cánh, thú tạm thu (GD-31 FLY-R-072) → không cần clip bay theo chủ.

---

## 5. Blend / transition (gợi ý cho state machine TD-05 §6)

> **Nguồn số là TD-05 §13 (`anim`).** TD-05 đã có `cancelBlendSec`, `anim.blend.*`, `rateMin` / `rateMax` theo bảng dưới (ANM-Q-6 đã chốt) — client không hard-code; khi vênh, theo TD-05.

### 5.1. Bảng chuyển tiếp

| Từ → Đến | Blend (TD-05 `anim.blend`) | Ghi chú |
|----------|:---:|---------|
| Bất kỳ → `BASIC_ATTACK` / `CASTING` / `DODGING` / `BLOCKING` (bản thân, dự đoán) | 0,05s | Phản hồi ≤ 1 khung (TD-11 PERF-R-040): clip mới bắt đầu ngay, blend chỉ làm mềm tư thế |
| `ATTACK_n` → `ATTACK_n+1` (bấm trong `nextInputWindowSec`) | 0,05s | Bắt đầu từ frame 0 của bước kế |
| Hủy sớm sau `cancelAfterSec` | `cancelBlendSec` (0,08s) | CL-R-072 |
| Đuôi `RECOVERY` → `IDLE_COMBAT` / `MOVING` | 0,15s (`recoveryExitSec`) | |
| `IDLE` ↔ `IDLE_COMBAT` | 0,3s | Theo cờ chiến đấu |
| Trong blend tree `MOVING` | Tham số liên tục | §5.2 |
| Bất kỳ → phản ứng (`HIT_HEAVY`, `KNOCKBACK`, `KNOCKDOWN`, `POISE_BREAK`, `STUN_LOOP`) | 0,05s | Phản ứng phải đọc được ngay |
| `KNOCKDOWN` → `GETUP` | 0 (nối tiếp) | |
| Bất kỳ → `DEATH` / `DOWNED_ENTER` | 0,1s | |
| `*_ENTER` → `*_LOOP` | 0 (nối tiếp) | `MEDITATE`, `DOWNED` |
| `MEDITATE_LOOP` → `MEDITATE_EXIT` → `IDLE` | 0,1s / 0,2s | Đả tọa dừng khi di chuyển: TD-05 CL-R-072 chọn ưu tiên phản hồi: di chuyển khi đả tọa cắt bằng `cancelBlendSec`; `MEDITATE_EXIT` chỉ phát khi dừng đả tọa không do di chuyển |
| `FLIGHT_TAKEOFF` → `FLIGHT_LOOP` → `FLIGHT_LAND` | 0 / 0,1s | Theo `S2C_FlightState` |
| Bất kỳ → `FLIGHT_FALL` | 0,05s | |

### 5.2. Lớp và additive
- <a id="anm-r-090"></a>**ANM-R-090** `HIT_LIGHT` (FLINCH) phát **additive** trên clip hiện tại, trọng số thân trên (từ `spine_01`); không đổi `ActionState`.
- **Blend tree `MOVING`:** 1D theo tốc độ thực (`|v|` nội suy từ server): 0 → `IDLE_COMBAT`, tốc độ `WALK` tham chiếu → `WALK`, 6 m/s → `RUN`; tốc độ phát `RUN` = `|v| / 6` (tốc độ tham chiếu lưu trong catalogue, `moveSpeed` đổi bởi trang bị / status GD-06 3–9 m/s). Hướng: nếu AST-Q-1 chọn clip strafe → blend 2D theo góc giữa hướng chạy và hướng con trỏ; nếu chọn additive thân trên → chỉ `RUN` + xoay `spine_01..03`.
- **Đi chậm khi đánh / đỡ** (`BASIC_ATTACK`, `HEAVY_CHARGE`, `RECOVERY` 30%; `BLOCKING` 40%, GD-07 §4): mask hai lớp — thân trên = clip hành động, thân dưới = `WALK` khi tốc độ > 0 — để tránh trượt chân (TD-05 CL-R-075).

### 5.3. Co giãn tốc độ theo dữ liệu
- <a id="anm-r-091"></a>**ANM-R-091** Với clip có `hit` tại `t_hit` (giây trong clip), thời lượng `D`, lượt trúng gameplay tại `T_hit = windupSec + hits[0].atSec` (hoặc `ComboStep.windupSec + hit.atSec`), kết thúc hồi `T_end = windupSec + hits[last].atSec + recoverySec`:

```
r_pre  = t_hit / T_hit                       // tốc độ phát đoạn [0, t_hit]
r_post = (D − t_hit) / (T_end − T_hit)       // tốc độ phát đoạn [t_hit, D]
mỗi r kẹp trong [checks.rateMin, checks.rateMax]; đoạn đầu ưu tiên chính xác (hit phải trùng)
```
  Với clip có `castTimeSec > 0`: `CAST_CHANNEL_LOOP` phủ `castTimeSec`, clip `Shape` bắt đầu sau đó và căn như trên (cách tính mốc trúng khi có `castTimeSec`: ANM-Q-2).

**Ví dụ 1** — `SKL_SWORD_QI_SLASH` (`windupSec` 0,25, `atSec` 0, `recoverySec` 0,35) với `ANM_HUMANOID_SWORD_CAST_PROJECTILE` 21 frame (`D` = 0,70s), `hit` ở frame 9 (`t_hit` = 0,30s): `r_pre = 0,30 / 0,25 = 1,20`; `T_end = 0,60` → `r_post = 0,40 / 0,35 ≈ 1,14`.
**Ví dụ 2** — AST-AC-007 / CL-AC-012: `t_hit` 0,30s, `T_hit` 0,45s → `r_pre ≈ 0,667`.
**Ví dụ 3** — `SKL_MON_WOLF_POUNCE` (`windupSec` 0,6, `atSec` 0) với `ANM_QUADRUPED_ATTACK_LUNGE` 24 frame, `hit` frame 15 (0,50s): `r_pre ≈ 0,833`.

- Clip `hit_1..hit_n`: căn theo `hit_1`; `art:check` cảnh báo nếu `(t_hit_k − t_hit_1) × r_pre` lệch `hits[k].atSec − hits[0].atSec` quá `checks.durationTolPct`.
- Clip khóa thời lượng (ANM-R-021): tốc độ = `D / thời lượng trạng thái` (vd. `FLIGHT_TAKEOFF` 30 frame = 1,0s với `takeOffSec` 1,0 → 1,0).

### 5.4. Chuỗi dự phòng (CL-R-073)
- <a id="anm-r-092"></a>**ANM-R-092** Thứ tự tra: `ANM_HUMANOID_{W}_{A}` → `ANM_HUMANOID_{A}` (biến thể gốc) → dự phòng theo bảng → `IDLE` của nhóm. Clip riêng boss → clip nhóm theo ANM-R-062.

| Thiếu | Dùng | Thiếu | Dùng |
|-------|------|-------|------|
| `DODGE_B/L/R` | `DODGE_F` (xoay thân theo `dir`) | `RUN_STRAFE_*`, `RUN_BACK` | `RUN` |
| `KNOCKBACK`, `KNOCKDOWN`, `GETUP`, `POISE_BREAK`, `STUN_LOOP` | `HIT_HEAVY` (giữ frame cuối tới hết trạng thái) | `HIT_LIGHT` | bỏ qua (chỉ VFX) |
| `DOWNED_*` | `KNOCKDOWN` (giữ frame cuối) | `DORMANT_LOOP` | `MEDITATE_LOOP` |
| `MEDITATE_ENTER/EXIT` | cắt thẳng (blend `cancelBlendSec`) | `CRAFT_*`, `GATHER_LOOP` | `INTERACT` |
| `CAST_*`, `DASH_STRIKE` | `{W}_ATTACK_1` | `CAST_CHANNEL_LOOP` | `{W}_HEAVY_CHARGE_LOOP` |
| `BLOCK_HIT`, `PARRY_PERFECT` | `BLOCK_LOOP` | `FLIGHT_*` | `IDLE` (đứng trên phi kiếm) |
| `EMOTE_*`, `FOLLOW_IDLE`, `HAPPY` | `IDLE` / `FLY_IDLE` / `IDLE_BOB` | Quái `ALERT` | `IDLE` |
| Quái `STUN_LOOP`, `KNOCKDOWN`, `GETUP` | `HIT` (giữ) / `HIT_SQUASH` | `MOVE_BOB` | `IDLE_BOB` |

---

## 6. Tổng số clip theo phase

Đếm theo §4 (mỗi vũ khí = 18 + n; boss chưa có dữ liệu dùng ước lượng §4.8). Dùng để ước khối lượng; cập nhật khi trả lời câu hỏi mở.

| Nhóm | Phase 0 | Phase 1 | Phase 2 | Phase 3 | Alpha | Beta 1 | Beta 2 | Tổng |
|------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| `HUMANOID` chung (§4.1) | 3 | 17 | 3 | 2 | 2 | — | 7 | 34 |
| `HUMANOID` `SWORD` | — | 20 | 2 | — | — | — | — | 22 |
| `HUMANOID` `FAN` | — | — | 21 | — | — | — | — | 21 |
| `HUMANOID` `DUAL_BLADE` + `FIST` (ước) | — | — | — | — | — | 44 | — | 44 |
| Quái `HUMANOID` theo archetype | — | — | 10 | — | — | — | — | 10 |
| `QUADRUPED` | — | 11 | — | — | — | — | — | 11 |
| `SERPENT` | — | 7 | — | — | — | — | — | 7 |
| `BIRD` | — | — | 7 | — | — | — | — | 7 |
| `NONE` | — | — | 6 | — | — | — | — | 6 |
| Boss (Hồ Yêu Mẫu 9; khác ước) | — | 6 | — | 16 | — | 13 | 1 | 36 |
| Linh thú | — | — | — | — | — | — | 8 | 8 |
| **Tổng theo phase** | **3** | **61** | **49** | **18** | **2** | **57** | **16** | **206** |
| Lũy kế | 3 | 64 | 113 | 131 | 133 | 190 | 206 | |

- Trong đó **P0** tới hết Phase 1: 37 clip + clip P0 của boss Phase 1 (11 chung: locomotion gốc, `DODGE_F`, phản ứng chính, `POISE_BREAK`, `DEATH`; 14 kiếm: trừ strafe / `WALK` / `CAST_SELF` / `CAST_CHANNEL_LOOP` / `BLOCK_HIT` / `PARRY_PERFECT`; 7 `QUADRUPED`; 5 `SERPENT`).
- Clip (+) bổ sung so với AR-02 (ngoài clip riêng boss): 15 — `DOWNED_IDLE`, `MEDITATE_EXIT`, 3 `CRAFT_*`, 3 `EMOTE_*` đề xuất, 3 `STUN_LOOP` quái, `MOVE_BOB`, `ATTACK_PULSE`, `HIT_SQUASH`, `SWORD_ATTACK_4`.
- Strafe (3 clip × mỗi vũ khí) đếm trong bảng; nếu AST-Q-1 chọn additive thì trừ 3 / vũ khí (−6 tới MVP).

---

## 7. Quy trình sản xuất (PLAN §6.6) và checklist

### 7.1. Quy trình
```
0. Chọn clip từ catalogue (§4 → tools/art/anim-list.json): mã, frame, marker, root, khóa thời lượng
1. Nguồn
   - MIXAMO: tải clip "In Place" (trừ clip có Root ✅), 30 fps, không skin
   - AI (sinh chuyển động): CHỈ khi mô hình đã đăng ký ✅ ở TD-15 §4.5; ngược lại làm HAND
   - HAND: key trong Blender trên rig chuẩn (art-source/animations/{SKELETON}.blend, Git LFS)
2. Retarget về bộ xương chuẩn AR-02 §4 bằng tools/art/retarget-map.json (Mixamo / AccuRIG → HUMANOID)
3. Làm sạch: khóa chân chạm đất, khớp vòng (đầu = cuối), xóa tịnh tiến root (ANM-R-041),
   không key cloth/sleeve/hair (ANM-R-042), chỉnh tư thế cầm vũ khí theo socket_weapon_r/l
4. Phong cách: anticipation → impact → follow-through rõ (PLAN §6.2, ART-R-042); tư thế đọc được ở camera 50°/20m (ART-R-001);
   tuân AR-01 ART-R-004 / ART-R-030 (không tư thế gợi dục)
5. Marker trên timeline (hit / hit_n / cancel / vfx_spawn) đúng frame tham chiếu
6. Mỗi clip = 1 NLA track tên = mã clip; script tools/art/blender_export.py xuất art/animations/{SKELETON}.glb
7. pnpm art:optimize (resample, nén key Meshopt)  →  pnpm art:check (ANM-AC-*, AST-AC-*)
8. Ảnh / video xem trước đúng camera game (AR-01 §9): mỗi clip phát trên model chuẩn + clip đòn phát cùng vùng cảnh báo của một SkillDef mẫu
9. Hồ sơ nguồn gốc art/provenance/{assetId}.json (TD-15 §4.5): Mixamo → tên clip gốc + link điều khoản + ngày đọc (OSS-R-021)
10. Art director duyệt (reviewedBy) → merge
```

### 7.2. Checklist mỗi clip (trước khi commit)
1. Mã clip có trong catalogue, đúng mẫu ANM-R-010, ≤ 48 ký tự.
2. 30 fps; số frame trong dung sai so với catalogue; clip khóa thời lượng khớp tham số gamedata (ANM-R-021).
3. Loop: đầu = cuối; không loop: kết ở tư thế chuyển tiếp được (hoặc tư thế giữ cho `DEATH` / `DOWNED_ENTER`).
4. Marker bắt buộc đủ, trong 40% đầu với clip đòn (ANM-R-031); chiêu nhiều nhát có `hit_1..hit_n`.
5. Root: chỉ clip Root ✅ có tịnh tiến; khoảng cách khớp tham chiếu.
6. Chỉ key xương có trong `tools/art/skeletons/{SKELETON}.json`; không key xương phụ / socket.
7. Xem trước đúng camera: đọc được hướng đòn, đọc được cảnh báo (AR-01 ART-R-002); không xuyên mesh vũ khí / tay áo ở tỷ lệ nhân vật đã chốt (ART-Q-1).
8. `art:optimize` + `art:check` xanh; hồ sơ nguồn gốc đủ; người duyệt ký.

---

## 8. Tiêu chí nghiệm thu

Trừ khi ghi khác, kiểm bằng `pnpm art:check` với tham số phase mục tiêu (lọc clip theo cột Phase của catalogue).

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="anm-ac-001"></a>ANM-AC-001 | `HUMANOID.glb` thiếu `ANM_HUMANOID_DODGE_F`, phase mục tiêu Phase 1 | `art:check` | Fail: `ActionState DODGING` không có clip (ANM-R-001) |
| <a id="anm-ac-002"></a>ANM-AC-002 | Clip `ANM_HUMANOID_SWORD_ATTACK_4` thiếu, `skills/weapons.json` có `SWORD.basicCombo` 4 bước | `art:check` | Fail (ANM-R-060) |
| <a id="anm-ac-003"></a>ANM-AC-003 | Clip xuất ở 24 fps | `art:check` | Fail (ANM-R-002) |
| <a id="anm-ac-004"></a>ANM-AC-004 | GLB chứa clip `ANM_HUMANOID_SWORD_SPIN` không có trong catalogue | `art:check` | Fail: clip chưa đăng ký (ANM-R-006) |
| <a id="anm-ac-005"></a>ANM-AC-005 | Tên clip dài 52 ký tự | `art:check` | Fail (ANM-R-011) |
| <a id="anm-ac-006"></a>ANM-AC-006 | `ANM_HUMANOID_FAN_CAST_CONE` không có marker `hit` | `art:check` | Fail (ANM-R-003, cùng AST-AC-003) |
| <a id="anm-ac-007"></a>ANM-AC-007 | `ANM_QUADRUPED_FOX_MATRIARCH_CLAW_COMBO` chỉ có `hit` (catalogue yêu cầu `hit_1..hit_3`) | `art:check` | Fail |
| <a id="anm-ac-008"></a>ANM-AC-008 | Marker `hit` ở 0,9s trong clip dài 0,8s | `art:check` | Fail (ANM-R-031) |
| <a id="anm-ac-009"></a>ANM-AC-009 | `ANM_HUMANOID_SWORD_RUN`: `root` tịnh tiến 1,2m theo z | `art:check` | Fail (ANM-R-041) |
| <a id="anm-ac-010"></a>ANM-AC-010 | `ANM_HUMANOID_DODGE_F` có root motion 4m | `art:check` | Đạt (clip Root ✅, ANM-R-040) |
| <a id="anm-ac-011"></a>ANM-AC-011 | Clip loop `ANM_HUMANOID_IDLE`, tư thế frame cuối lệch frame đầu vượt `checks.loopPoseMaxDeg` | `art:check` | Fail |
| <a id="anm-ac-012"></a>ANM-AC-012 | Clip key xương `tail_01` trong `HUMANOID.glb` | `art:check` | Fail: xương ngoài bộ xương chuẩn (AST-R-020) |
| <a id="anm-ac-013"></a>ANM-AC-013 | Clip key `sleeve_l_01` khi AST-Q-2 chưa chốt | `art:check` | Cảnh báo (ANM-R-042) |
| <a id="anm-ac-014"></a>ANM-AC-014 | `SKL_SWORD_QI_SLASH` (windup 0,25, atSec 0, recovery 0,35), clip `CAST_PROJECTILE` 21 frame, `hit` frame 9 | Tính tốc độ (unit test `client/src/anim`) | `r_pre = 1,20`, `r_post ≈ 1,14` (§5.3 ví dụ 1) |
| <a id="anm-ac-015"></a>ANM-AC-015 | Một `SkillDef` cần `r_pre` < `checks.rateMin` với clip theo `Shape` | `art:check` (đối chiếu `skills.json`) | Cảnh báo: cần clip `_CHARGE` / `_RELEASE` (ANM-R-070) hoặc sửa frame `hit` |
| <a id="anm-ac-016"></a>ANM-AC-016 | `FLIGHT_TAKEOFF` 30 frame, `world/flight.json → takeOffSec` đổi thành 1,5 | `art:check` | Cảnh báo: hệ số co giãn 0,67 ngoài `[rateMin, rateMax]` (ANM-R-021) |
| <a id="anm-ac-017"></a>ANM-AC-017 | `MonsterDef` `skeleton = SERPENT` thuộc phase mục tiêu, GLB `SERPENT` thiếu `ANM_SERPENT_DEATH` | `art:check` (đối chiếu `monsters.json`) | Fail |
| <a id="anm-ac-018"></a>ANM-AC-018 | Client: nhân vật vào `DOWNED`, GLB không có `DOWNED_*` | Phát (test client) | Dùng `KNOCKDOWN` giữ frame cuối; không T-pose (ANM-R-092, CL-R-073) |
| <a id="anm-ac-019"></a>ANM-AC-019 | Client: trúng đòn `FLINCH` khi đang `ATTACK_2` | Phát | `HIT_LIGHT` additive, `ATTACK_2` tiếp tục, `ActionState` không đổi (ANM-R-090) |
| <a id="anm-ac-020"></a>ANM-AC-020 | Clip nguồn `AI` ghi mô hình chưa có dòng ✅ ở TD-15 §4.5 | CI | Fail (OSS-R-021 / AR-02 AST-R-005) |
| <a id="anm-ac-021"></a>ANM-AC-021 | Linh thú loài `sourceMonsterId` nhóm `QUADRUPED`, phase Beta 2, GLB thiếu `ANM_QUADRUPED_FOLLOW_IDLE` | `art:check` | Fail (ANM-R-080) |
| <a id="anm-ac-022"></a>ANM-AC-022 | Client: thi triển `SKL_FIRE_ORB` khi cầm quạt | Phát | Clip `ANM_HUMANOID_FAN_CAST_PROJECTILE` (§3.2); chưa có → `ANM_HUMANOID_FAN_ATTACK_1` |

---

## 9. Câu hỏi mở
- [x] **ANM-Q-1:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: theo GD-07 SK-R-054; AR-02 §5.2 vẫn phải sửa.)* AR-02 §5.2 ghi combo `ATTACK_1..3` chung, trong khi GD-07 §5.4 kiếm 4 bước, quạt 3 bước. *Mặc định: theo GD-07 (ANM-R-060), cần sửa AR-02.*
- [ ] **ANM-Q-2:** *(Đợt rà 2026-10-25: phần clip `LINE` / `SUMMON` / tuyệt kỹ vẫn dùng mặc định tạm; phần mốc trúng khi `castTimeSec > 0` cần GD-07 chốt SK-R-072 — chưa đóng.)* Clip cho `Shape` `LINE` và `SUMMON` (AR-02 không có); tuyệt kỹ có clip riêng mỗi vũ khí không; khi `castTimeSec > 0`, mốc trúng là `windupSec + atSec` tính từ lúc nào (GD-07 SK-R-072 chưa nêu). *Mặc định: `LINE` → `CAST_CONE`, `SUMMON` → `CAST_SELF`, tuyệt kỹ theo `Shape`; `castTimeSec` đứng trước windup.*
- [ ] **ANM-Q-3:** *(Đợt rà 2026-10-25: TD-11 PERF-R-037 đặt ngân sách ≤ 3 MB / GLB, vượt thì tách theo đề xuất này; việc tách còn chờ AR-02 / TD-05 CL-R-070.)* Một GLB / nhóm (CL-R-070) sẽ chứa ~125 clip `HUMANOID` ở Beta 1 và clip riêng mọi boss trong GLB nhóm — vượt ngân sách tải? TD-11 chưa có ngân sách cho GLB hoạt ảnh. *Đề xuất: tách `HUMANOID_{W}.glb` theo vũ khí và `{SKELETON}_{MODEL_KEY}.glb` cho boss; cần TD-05 / TD-11 / AR-02 đồng ý. Mặc định: theo CL-R-070.*
- [x] **ANM-Q-4:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: luôn cầm, không có clip rút / cất.)* Vũ khí luôn cầm tay hay đeo lưng ngoài chiến đấu (`socket_back` có sẵn) — nếu đeo lưng cần clip rút / cất (`DRAW`, `SHEATHE`) × mỗi vũ khí. *Mặc định: luôn cầm, không có clip rút / cất.*
- [x] **ANM-Q-5:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: `IDLE_COMBAT` + VFX; AR-02 vẫn phải sửa.)* `ROOTED`: AR-02 gộp vào `STUN_LOOP`, nhưng ROOTED vẫn thi triển được (GD-07 SK-R-070). *Mặc định: `IDLE_COMBAT` + VFX; cần sửa AR-02.*
- [x] **ANM-Q-6:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: TD-05 §13 `anim.blend`, `rateMin` 0,6 / `rateMax` 1,6, CL-R-075 mask hai lớp; ngưỡng `checks` còn lại theo mặc định.)* Blend gợi ý §5.1, mask hai lớp khi đánh / đỡ lúc di chuyển (§5.2), và ngưỡng `checks` của `art:check`. *Mặc định đề xuất: `rateMin` 0,6 · `rateMax` 1,6 · `durationTolPct` 10% · `rootDriftMaxM` 0,05 · `loopPoseMaxDeg` 2°; TD-05 §13 nên thêm khóa blend vào `anim`.*
- [x] **ANM-Q-7:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: GD-06 v0.5 thêm `reactionSec` và `dodge.durationSec` (0,4s tạm, SF-Q-5); KNOCKDOWN gồm cả `GETUP`.)* Thời lượng phản ứng STAGGER 0,4s · KNOCKBACK 0,5s · KNOCKDOWN 1,2s (GD-06 SF-R-093) và **thời gian lướt của né** chỉ có trong văn bản, **không có khóa trong `stats/params.json`** (chỉ có `dodge.distance`, `iframeSec`, `cooldownSec`) — trái luật "không hằng số trong code". *Đề xuất GD-06 thêm khóa; tạm khóa thời lượng clip theo số trong văn bản.* KNOCKDOWN 1,2s gồm cả đứng dậy hay `GETUP` phát sau? *Mặc định: gồm cả `GETUP`.*
- [x] **ANM-Q-8:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: không clip mới.)* Tọa hóa (hết `LAST_LIGHT`), binh giải (GD-26 §5.16), đột phá thành công / thất bại, bế quan online (cutscene 5s, LC-R-130) có clip riêng không? *Mặc định: tọa hóa / binh giải / bế quan = `MEDITATE_LOOP` + VFX; đột phá = `MEDITATE_LOOP` rồi VFX kết quả; không clip mới.*
- [x] **ANM-Q-9:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: không thêm marker.)* Marker bước chân (AU-01: SFX bước chân theo mặt đất) — thêm loại marker `foot_l` / `foot_r` vào AR-02 §5.1 hay client tự suy từ pha clip? *Mặc định: không thêm marker; client phát theo chu kỳ clip.*
- [x] **ANM-Q-10:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: catalogue art `skillClips` — TD-05 CL-R-074, GD-12 EN-R-095.)* Ánh xạ `skillId` → clip riêng cho chiêu boss: thêm trường vào `SkillDef` / `MonsterSkillRef` (GD-07 / GD-12 / TD-07) hay giữ trong catalogue art (`anim-list.json → skillClips`)? *Mặc định: catalogue art.*
- [x] **ANM-Q-11:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: `GATHER_LOOP`; quét / dùng đan / ném phù = VFX.)* Khai khoáng (`PoiKind.ORE`, PLAN Phase 3) dùng `GATHER_LOOP` hay clip đào riêng (`MINE_LOOP`)? Thần thức quét (niệm 0,5s, GD-21), dùng đan / ô nhanh, ném Ngự Thú Phù (GD-25) có clip không? *Mặc định: `GATHER_LOOP`; quét / dùng đan / ném phù = VFX, không clip.*
- [ ] **ANM-Q-12:** Danh sách biểu cảm `COS_*` (`EMOTE`) và thời điểm có (GD-19 §16, PLAN Beta 2) — cần GD-19 chốt để thay ba clip đề xuất.
- [ ] **ANM-Q-13:** *(Đợt rà 2026-10-25: chuyển thành GD-07 SK-Q-6, cần designer quyết.)* `WeaponDef` cho `DUAL_BLADE`, `FIST` (số bước combo, đánh mạnh) chưa có; "Beta" của GD-07 §16 là Beta 1 hay Beta 2? *Mặc định ước lượng: n = 4, Beta 1.*
- [x] **ANM-Q-14:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: tàn hồn dùng `{W}_*`, quái khác dùng bộ archetype, cung là mesh gắn socket.)* Quái hình người có cầm vũ khí người chơi (vd. Chấp Pháp, tàn hồn) dùng bộ `{W}_*` của người chơi hay bộ archetype? `MON_GHOST_ARCHER` bắn cung — không có `WeaponType` cung. *Mặc định: tàn hồn dùng `{W}_*`; quái khác dùng bộ archetype; cung là mesh gắn socket.*
- [ ] **ANM-Q-15:** *(Đợt rà 2026-10-25: chuyển thành GD-12 EN-Q-6.)* GD-12 chưa ghi: bộ quái / boss của Phase 1 ("3 quái thường + 1 tinh anh + 1 boss"), `skeleton` của `MON_B_MINE_TYRANT`, `MON_B_SWORD_SPIRIT`, `MON_CAVE_WARDEN`, `MON_B_HEAVEN_AVATAR`, world boss, và mã chiêu boss (chỉ có tên tiếng Việt ở §7.2). *Mặc định ước lượng §4.8; Phase 1 giả định quái F01 (`QUADRUPED` + `SERPENT`).*
- [x] **ANM-Q-16:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: quy tắc tạm tới khi chốt ART-Q-1.)* Tỷ lệ nhân vật chưa chốt (AR-01 ART-Q-1 / PLAN Q13): đầu to 1/5 làm clip Mixamo xuyên đầu / tay áo. *Mặc định: chỉ làm `IDLE`/`WALK`/`RUN` gốc (Phase 0) trước khi chốt; P0 Phase 1 làm sau khi chốt.*
- [x] **ANM-Q-17:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: một `FLIGHT_LOOP` + nghiêng `spine` bằng code.)* Ngự kiếm: có trạng thái bay đứng yên (không di chuyển) không, và có cần nghiêng người theo tốc độ (`FLIGHT_LOOP` tách `FLIGHT_HOVER` / `FLIGHT_MOVE`)? GD-31 không nêu. *Mặc định: một `FLIGHT_LOOP` + nghiêng `spine` bằng code.*
- [ ] **ANM-Q-18:** Mixamo đang ⚠️ ở TD-15 §4.5 (chưa xác nhận dùng thương mại). Nếu không được duyệt, ~40% clip `HUMANOID` phải làm tay / AI → ảnh hưởng lớn khối lượng §6. Cần chốt trước Phase 1.

---

## Lịch sử thay đổi
| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến (mỗi `ActionState` có clip, 30 fps, marker `hit` khớp `windupSec + hit.atSec`, in-place), quy ước tên 4 dạng mở rộng AR-02 §3, marker, root motion, gói GLB + catalogue `tools/art/anim-list.json`; ánh xạ `ActionState` / `Shape` / trạng thái AI / đòn quái → clip; bảng clip `HUMANOID` chung, theo vũ khí (`SWORD` 4 bước, `FAN` 3 bước, `DUAL_BLADE` / `FIST` ước lượng), quái hình người theo archetype, `QUADRUPED`, `BIRD`, `SERPENT`, `NONE`, boss (Hồ Yêu Mẫu đầy đủ, boss khác ước lượng), linh thú; blend / transition gợi ý, co giãn tốc độ có ví dụ, chuỗi dự phòng; tổng 206 clip theo phase; quy trình sản xuất AI / Mixamo + checklist; 22 tiêu chí nghiệm thu; 18 câu hỏi mở. Clip (+) bổ sung so với AR-02 §5 cần đồng bộ ngược |
| v0.2 | 2026-10-25 | — | Đợt rà nhất quán: đánh dấu đã chốt ANM-Q-1, 4–11, 14, 16, 17 (theo mặc định / theo GD-06 v0.5, GD-07 v0.6, GD-12 v0.4, TD-05 v0.4, TD-11 v0.4); bỏ "đề xuất" / "gợi ý" ở chỗ đã đồng bộ (đường dẫn GLB, khóa thời lượng `reactionSec` / `dodge.durationSec`, blend `anim.blend`, mask thân trên / dưới, `skillClips`); ghi chú tình trạng ANM-Q-2, 3, 13, 15 |
| v0.3 | 2026-10-25 | — | Ghi chú: clip (+) đã gộp vào AR-02 v0.3 |

