# AR-02 — Asset Guidelines

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | AR-02 · Prefix ID: `AST` |
| Owner | Tech Artist |
| Trạng thái | 🟡 Bản nháp v0.4 |
| Cần trước | Phase 0 (pipeline asset + spike mỹ thuật AI) |
| Quyết định nền | D-22, PLAN §6.2, §6.6, §6.7; [TD-11](../tech/10-performance.md) §3, §3.1 (ngân sách asset — **nguồn số**), [TD-13](../tech/12-map-editor.md) §7, [TD-05](../tech/04-client.md) §6 (hoạt ảnh), [TD-15](../tech/14-oss-register.md) §3.4, §4.5 |
| Liên quan | [AR-01 Art Bible](art-bible.md) · [AR-03 Animation List](animation-list.md) (danh mục clip chuẩn) · [AR-04 VFX](vfx-guidelines.md) §11 · [AR-05 Asset Tracker](asset-tracker.md) · [AR-06 Environment Kits](environment-kits.md) · [GD-07](../design/13-skills.md) §4 (ActionState) · [GD-12](../design/18-enemies-ai.md) (`SkeletonGroup`, `modelId`) · [GD-25](../design/26-spirit-beasts.md) (thú dùng model quái) · [TD-14](../tech/13-geom2d.md) (va chạm) · [TD-07](../tech/06-gamedata.md) DT-R-053 |

**Tài liệu này cung cấp cho:** artist và **AI script Blender** (quy chuẩn kỹ thuật bắt buộc), CI validator asset `pnpm art:check` (TD-11 PERF-R-030, AR-05 §6), TD-05 (tên xương, event hoạt ảnh), TD-13 (`collisionTemplate`), AR-03 / AR-04 / AR-06 (mẫu tên, khóa `extras`), AR-05 (`kind`, `budgetClass`).

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="ast-r-001"></a>**AST-R-001** | **1 unit = 1 mét**; mặt đất y = 0; nhân vật cao chuẩn 1,7m (tỷ lệ theo AR-01 §3 sau khi chốt Q13). |
| <a id="ast-r-002"></a>**AST-R-002** | Mọi asset 3D vào game là **glTF 2.0 (GLB)** đã qua glTF-Transform (Meshopt + KTX2); không đưa FBX / OBJ / PNG thô vào `client`. Ngoại lệ có định dạng riêng ở §3: texture dùng chung nhiều GLB (`ENV_*`, `TER_*`) là **KTX2**; asset 2D (`ICO`, `UI`, `POR`, `ILL`, `WMAP`) là **webp**. |
| <a id="ast-r-003"></a>**AST-R-003** | Ngân sách tam giác / texture / xương / dung lượng theo **TD-11 §3, §3.1** — vượt là CI chặn (PERF-AC-003). |
| <a id="ast-r-004"></a>**AST-R-004** | Mọi nhân vật hình người dùng **một bộ xương chung** (§4); quái dùng bộ xương theo `SkeletonGroup`. |
| <a id="ast-r-005"></a>**AST-R-005** | Mọi asset có **hồ sơ nguồn gốc** `art/provenance/{assetId}.json` (TD-15 §3.4) — thiếu là CI chặn (OSS-AC-005). |

---

## 2. Trục, tỷ lệ, pivot

| Mục | Quy chuẩn |
|-----|-----------|
| Hệ trục trong Blender | Z lên, nhân vật nhìn về **−Y** (Front); xuất glTF với "+Y Up" → Babylon.js (left-handed, y lên) nhận đúng, nhân vật nhìn về **+Z** (Bắc, TD-14 §2.1) |
| Đơn vị Blender | Metric, Unit Scale 1,0 |
| Pivot nhân vật / quái | Giữa hai chân, tại mặt đất (0,0,0) |
| Pivot prop | Tâm đáy (chạm đất) — đặt được trực tiếp lên địa hình |
| Pivot vũ khí | Điểm cầm (trùng xương `hand_r` / `hand_l` khi gắn) |
| Biến đổi | Apply rotation + scale trước khi xuất (scale 1, rotation 0) |
| Snap | Kit môi trường theo lưới 0,5m, góc 15° (TD-13 E1) |

---

## 3. Đặt tên

Cột `kind` là giá trị cột `kind` của tracker (AR-05 §4). Dòng ghi **(đề xuất)** là mẫu mới ở v0.3, chưa có asset nào dùng.

| Loại | `kind` | Mẫu `assetId` | Ví dụ | File |
|------|--------|---------------|-------|------|
| Nhân vật gốc | `CHR` | `CHR_{LOẠI}_{TÊN}` | `CHR_PLAYER_BASE_M` | `art/characters/{assetId}.glb` |
| Biến thể mẫu phong cách (chỉ `releaseIntent = SAMPLE`, AR-05 TRK-R-012) | (theo loại gốc) | `{mẫu của loại}` + hậu tố `_SAMPLE_{A..Z}` | `CHR_PLAYER_SAMPLE_A` | như loại gốc |
| Trang phục (mesh thay) | `OUT` | `OUT_{TÔNG\|BỘ}_{PHẦN}_{nn}` | `OUT_THANHVAN_BODY_01` | `art/outfits/{assetId}.glb` |
| Ngoại hình bán (GD-19) | `COS` | khớp `COS_*` | `COS_OUT_SPRING_BLOSSOM` | `art/cosmetics/{assetId}.glb` |
| Vũ khí | `WPN` | `WPN_{WeaponType}_{TÊN}` | `WPN_SWORD_LK4` | `art/weapons/{assetId}.glb` |
| Quái / boss | `MDL_MON` | `MDL_MON_{TÊN}` = giá trị `MonsterDef.modelId` (GD-12; chốt AR-05 TRK-Q-6). Mặc định `{TÊN}` = phần sau tiền tố của `MON_*` / `MON_E_*` / `MON_B_*` (bỏ `E_` / `B_`); nhiều quái (biến thể, linh thú GD-25) được dùng chung một model | `MDL_MON_RED_FOX`, `MDL_MON_FOX_MATRIARCH` (`MON_B_FOX_MATRIARCH`) | `art/monsters/{assetId}.glb` |
| Kit theo vực | `KIT` | `KIT_{VỰC}_{TÊN}` | `KIT_V1_BAMBOO` | `art/env/{kit}/kit.json` (catalogue, AR-06 §3.3) |
| Kit dùng chung mọi vực (đề xuất, AR-06) | `KIT` | `KIT_CM_{TÊN}` | `KIT_CM_POI` | như trên |
| Mảnh kit | `PRP` | `PRP_{KIT_NGẮN}_{TÊN}_{nn}` | `PRP_BAMBOO_STALK_03` | `art/env/{kit}/{assetId}.glb` |
| Mảnh module (đề xuất, AR-06) | `PRP` | `PRP_{KIT_NGẮN}_{TÊN}_{DÀI}M[_H{CAO}]` | `PRP_ROCK_CLIFF_8M_H6` | như trên |
| Mảnh theo cấp công trình (đề xuất, AR-06) | `PRP` | `PRP_{KIT_NGẮN}_{CÔNG_TRÌNH}_L{n}` | `PRP_CAVE_TINHTHAT_L2` | như trên |
| Texture môi trường dùng chung nhiều kit (đề xuất, AR-06 KIT-R-030) | `ENV` | `ENV_{TÊN}` → texture `ENV_{TÊN}_ATLAS` | `ENV_GRADIENT` → `ENV_GRADIENT_ATLAS` | `art/env/{assetId}_ATLAS.ktx2` |
| Texture địa hình (lớp splat, đề xuất, AR-06) | `TER` | `TER_{VỰC}_{TÊN}` | `TER_V1_BAMBOO_LITTER` | `art/terrain/{assetId}.ktx2` |
| Mẫu VFX kỹ năng | `VFX` | `VFX_{MẪU}` | `VFX_BOLT` | `art/vfx/{assetId}/…` |
| Phần / pha của mẫu VFX | `VFX` | `VFX_{MẪU}_{PHẦN}` | `VFX_BOLT_CORE` | `art/vfx/{MẪU}/…` |
| VFX theo nhóm (đề xuất, AR-04 §11.1) | `VFX` | `VFX_{NHÓM}_{TÊN}`, `NHÓM` ∈ `TG` · `HIT` · `CB` · `ST` · `ENV` · `AMB` · `POI` · `WLD` · `BT` · `TRIB` · `FLY` · `EVT` · `DZ` · `PK` | `VFX_TG_ENEMY`, `VFX_ST_BURN` | `art/vfx/{assetId}/…` |
| VFX chiêu boss (đề xuất, AR-04) | `VFX` | `VFX_BOSS_{MODEL_KEY}_{CHIÊU}` (`MODEL_KEY` = `modelId` bỏ `MDL_MON_`) | `VFX_BOSS_FOX_MATRIARCH_FIRE_BURST` | `art/vfx/{assetId}/…` |
| Lớp phủ biến thức (GD-08) | `VFX` | `VFXO_{TÊN}` | `VFXO_MULTI` | `art/vfx/{assetId}/…` |
| Icon | `ICO` | `ICO_{ID gamedata}` | `ICO_ITM_TRUC_CO_DAN` | `art/icons/{assetId}.webp` (128²) |
| Nhạc nền | `BGM` | `BGM_{VỰC}_{NGỮ_CẢNH}` (AU-01 §3.1) | `BGM_V1_FIELD_CALM` | `art/audio/bgm/{assetId}.ogg` |
| Khung / màn UI (đề xuất, TRK-Q-3) | `UI` | `UI_{MÀN}_{TÊN}` — `MÀN` = tên màn GD-17 §3 viết tiếng Anh chữ hoa (GD-17 chưa có mã màn — AST-Q-5) | `UI_INVENTORY_FRAME` | `art/ui/{assetId}.webp` |
| Chân dung NPC (đề xuất, TRK-Q-3) | `POR` | `POR_{npcId}` (giữ nguyên `NPC_*`, TD-07 DT-R-051) | `POR_NPC_OLD_FISHERMAN` | `art/portraits/{assetId}.webp` |
| Minh họa cơ duyên / cutscene (đề xuất, TRK-Q-3) | `ILL` | `ILL_{ID gamedata}_{nn}` (ID nội dung được minh họa, vd. `ENC_*`, `QC_*`) | `ILL_ENC_V1_NGU_ONG_01` | `art/illustrations/{assetId}.webp` |
| Bản đồ thế giới / minimap vẽ tay (đề xuất, TRK-Q-3) | `WMAP` | `WMAP_{VỰC}` hoặc `WMAP_WORLD` | `WMAP_V1` | `art/worldmap/{assetId}.webp` |
| Hoạt ảnh — clip chung của bộ xương | `ANM` | `ANM_{SKELETON}_{ACTION}` | `ANM_HUMANOID_DODGE_F`, `ANM_QUADRUPED_ATTACK_BITE` | trong `art/animations/{SKELETON}.glb` |
| Hoạt ảnh — theo vũ khí | `ANM` | `ANM_HUMANOID_{WeaponType}_{ACTION}` | `ANM_HUMANOID_SWORD_ATTACK_1` | như trên |
| Hoạt ảnh — quái hình người theo archetype | `ANM` | `ANM_HUMANOID_{AiArchetype}_{ACTION}` | `ANM_HUMANOID_RANGED_KITER_ATTACK_1` | như trên |
| Hoạt ảnh — riêng một model (boss) | `ANM` | `ANM_{SKELETON}_{MODEL_KEY}_{ACTION}` | `ANM_QUADRUPED_FOX_MATRIARCH_CLAW_COMBO` | như trên |
| GLB hoạt ảnh của một `SkeletonGroup` (CL-R-070, AR-03 ANM-R-050) | — (không có dòng tracker riêng; tracker theo clip, TRK-Q-4) | `{SKELETON}` | `HUMANOID` | `art/animations/{SKELETON}.glb` |
| Texture | — (thuộc asset chủ) | `{assetId}_{map}` với map ∈ `BC` (base color), `ATLAS` | `CHR_PLAYER_BASE_M_BC` | trong GLB của asset |

Mã **không phải asset** nhưng liên quan, sở hữu ở tài liệu khác (không tạo file trong `art/`): bảng màu VFX `VPAL_*`, `TGPAL_*` (AR-04, `vfx/*.json`); bảng màu bản đồ `PAL_{VỰC}_{TÊN}` (AR-06, `world/env_palettes.json`); mẫu biome `BIO_{VỰC}_{TÊN}` (AR-06, TD-13 MF-R-033) — TD-07 DT-R-051.

- <a id="ast-r-010"></a>**AST-R-010** `assetId` chữ hoa + gạch dưới, ≤ 48 ký tự, **không đổi tên** sau khi được tham chiếu trong gamedata (giống DT-R-002).
- <a id="ast-r-011"></a>**AST-R-011** Bảng trên là **danh sách đóng** tiền tố asset (TD-07 DT-R-053): tiền tố / mẫu mới phải thêm vào đây (tăng phiên bản) trước khi tạo asset hoặc dòng tracker. `{kit}` trong đường dẫn = `KitDef.shortName` viết thường (vd. `art/env/bamboo/`); `{SKELETON}` = giá trị `SkeletonGroup` viết hoa (`HUMANOID`, `QUADRUPED`, `BIRD`, `SERPENT`, `NONE`). Tên clip chi tiết theo AR-03 ANM-R-010.
- <a id="ast-r-012"></a>**AST-R-012** Hậu tố `_SAMPLE_{X}` chỉ dùng cho asset `releaseIntent = SAMPLE`; gamedata phát hành không được tham chiếu `assetId` có hậu tố này (AR-05 TRK-R-012). Asset thật làm lại từ mẫu dùng `assetId` mới không có hậu tố.

---

## 4. Bộ xương người chung (`HUMANOID`)

- <a id="ast-r-020"></a>**AST-R-020** Danh sách xương **cố định** (≤ 60 bone, TD-11), tên chữ thường gạch dưới; tương thích ánh xạ Mixamo / AccuRIG (bảng ánh xạ trong `tools/art/retarget-map.json`).

| Nhóm | Xương |
|------|-------|
| Gốc | `root` (mặt đất, mang chuyển động gốc), `hips` |
| Thân | `spine_01`, `spine_02`, `spine_03`, `neck`, `head`, `head_end` |
| Tay trái | `clavicle_l`, `upperarm_l`, `lowerarm_l`, `hand_l`, `thumb_01_l`, `thumb_02_l`, `index_01_l`, `index_02_l`, `middle_01_l`, `middle_02_l`, `ring_01_l`, `pinky_01_l` |
| Tay phải | (đối xứng hậu tố `_r`) |
| Chân trái | `thigh_l`, `calf_l`, `foot_l`, `ball_l` |
| Chân phải | `thigh_r`, `calf_r`, `foot_r`, `ball_r` |
| Điểm gắn (socket) | `socket_weapon_r`, `socket_weapon_l`, `socket_back` (vũ khí đeo lưng), `socket_head_fx` (hào quang danh hiệu / Linh chủng), `socket_feet_fx` (ngự kiếm GD-31), `socket_chest_fx` |
| Phụ (tùy chọn, tính vào 60) | `cloth_front_01..02`, `cloth_back_01..02`, `sleeve_l_01`, `sleeve_r_01`, `hair_01..02` (lụa / tóc bay — spring bone ở client) |

- **Quái theo nhóm** (`SkeletonGroup`, GD-12): `QUADRUPED` (root, hips, spine_01–03, neck, head, jaw, tail_01–04, 4 chân × 4 đốt, socket_head_fx), `BIRD` (thêm wing_l/r × 3 đốt), `SERPENT` (root + chuỗi spine_01–16 + head + jaw), `NONE` (không xương — quái tĩnh / construct một khối, hoạt ảnh bằng transform). Danh sách tên đầy đủ ở `tools/art/skeletons/*.json` (sinh từ tài liệu này).

---

## 5. Hoạt ảnh

### 5.1. Quy tắc chung
- <a id="ast-r-030"></a>**AST-R-030** 30 fps gốc; chuyển động gốc (root motion) **không** dùng cho di chuyển thường (server điều khiển vị trí) — chỉ có trên clip lướt / né để tham khảo khoảng cách, client bỏ root motion khi phát.
- <a id="ast-r-031"></a>**AST-R-031** Clip tấn công / kỹ năng có **marker `hit`** (Blender timeline marker → glTF `extras` của AnimationGroup: `{ "events": [{ "name": "hit", "t": 0.30 }] }`); client co giãn tốc độ clip để `hit` trùng thời điểm trúng trong `SkillDef` (TD-05 CL-AC-012). Clip nhiều nhát có `hit_1`, `hit_2`…; tùy chọn marker `cancel` (mốc hủy sớm nhất) và `vfx_spawn`.
- Clip lặp (`idle`, `run`) khớp vòng (frame đầu = cuối).

> **Danh mục chuẩn là [AR-03](animation-list.md) §4** (catalogue máy đọc `tools/art/anim-list.json`, ANM-R-006): mã clip, frame, marker, root, nguồn, ưu tiên, phase. §5.2–§5.3 dưới đây là **danh sách tối thiểu** để đối chiếu; khi lệch, AR-03 thắng về danh sách clip, AR-02 thắng về quy ước (tên, bộ xương, marker, fps). Clip đánh dấu **(+)** là clip AR-03 bổ sung, đã đồng bộ vào AR-02 v0.3.

### 5.2. Danh sách tối thiểu — người chơi (khớp `ActionState` GD-07 §4)

| ActionState | Clip (`ANM_HUMANOID_…`) | Theo vũ khí |
|-------------|------------------------|:---:|
| `IDLE` | `IDLE`, `IDLE_COMBAT` | ✅ |
| `MOVING` | `WALK`, `RUN` (blend theo tốc độ; 8 hướng nếu đánh khi di chuyển chậm: `RUN_STRAFE_L/R`, `RUN_BACK`) | ✅ |
| `BASIC_ATTACK` | `ATTACK_1` … `ATTACK_n`, n = `len(WeaponDef.basicCombo)` (GD-07; kiếm 4 bước → có `SWORD_ATTACK_4` (+), quạt 3 bước — AR-03 ANM-R-060) | ✅ |
| `HEAVY_CHARGE` → đòn mạnh | `HEAVY_CHARGE_LOOP`, `HEAVY_RELEASE` | ✅ |
| `CASTING` / `ACTIVE` | `CAST_PROJECTILE`, `CAST_CONE`, `CAST_GROUND`, `CAST_SELF`, `CAST_CHANNEL_LOOP`, `DASH_STRIKE` (dùng chung cho mọi kỹ năng theo `Shape`) | ✅ |
| `RECOVERY` | (đuôi của clip tấn công / cast) | |
| `DODGING` | `DODGE_F`, `DODGE_B`, `DODGE_L`, `DODGE_R` | |
| `BLOCKING` | `BLOCK_LOOP`, `BLOCK_HIT`, `PARRY_PERFECT` | ✅ |
| `STAGGERED` / `KNOCKED_BACK` / `KNOCKED_DOWN` | `HIT_LIGHT`, `HIT_HEAVY`, `KNOCKBACK`, `KNOCKDOWN`, `GETUP` | |
| `STUNNED` / `POISE_BROKEN` | `STUN_LOOP`, `POISE_BREAK` | |
| `ROOTED` | `IDLE_COMBAT` (vẫn thi triển được — GD-07 SK-R-070) | |
| `CHANNELING` | `MEDITATE_ENTER`, `MEDITATE_LOOP` (đả tọa, đột phá), `MEDITATE_EXIT` (+), `GATHER_LOOP` (hái), `INTERACT`; chế tác: `CRAFT_ALCHEMY_LOOP` (+), `CRAFT_FORGE_LOOP` (+), `CRAFT_FORGE_STRIKE` (+, marker `vfx_spawn`) | |
| `DOWNED` | `DOWNED_ENTER`, `DOWNED_IDLE` (+), `DOWNED_CRAWL` | |
| `DORMANT_GUARD` | `DORMANT_LOOP` (tĩnh tọa phát sáng) | |
| `DEAD` | `DEATH` | |
| Khác | `FLIGHT_TAKEOFF`, `FLIGHT_LOOP`, `FLIGHT_LAND`, `FLIGHT_FALL` (GD-31); `EMOTE_*` (cosmetic GD-19; AR-03 có 3 clip **đề xuất** `EMOTE_SALUTE`, `EMOTE_BOW`, `EMOTE_WAVE` (+) chờ danh sách `COS_*` — ANM-Q-12) | |

Vũ khí MVP: kiếm (`SWORD`), quạt (`FAN`) → mỗi clip "theo vũ khí" có hai biến thể (`ANM_HUMANOID_SWORD_ATTACK_1`, `ANM_HUMANOID_FAN_ATTACK_1`). Ước tính ~45 clip / vũ khí.

### 5.3. Danh sách tối thiểu — quái theo nhóm
| `SkeletonGroup` | Clip tối thiểu |
|-----------------|---------------|
| `QUADRUPED` | `IDLE`, `WALK`, `RUN`, `ATTACK_BITE`, `ATTACK_LUNGE`, `HIT`, `STUN_LOOP` (+), `KNOCKDOWN`, `GETUP`, `DEATH`, `ALERT` (gầm) |
| `BIRD` | `FLY_IDLE`, `FLY_MOVE`, `ATTACK_DIVE`, `ATTACK_WING`, `HIT`, `STUN_LOOP` (+), `DEATH` |
| `SERPENT` | `IDLE`, `MOVE`, `ATTACK_STRIKE`, `ATTACK_SPIT`, `HIT`, `STUN_LOOP` (+), `DEATH` |
| `HUMANOID` (quái người) | Dùng biến thể gốc của thư viện người chơi + `ANM_HUMANOID_{AiArchetype}_ATTACK_n` riêng của archetype (§3) |
| `NONE` | Hoạt ảnh transform: `IDLE_BOB`, `MOVE_BOB` (+), `ATTACK_SLAM`, `ATTACK_PULSE` (+, marker `hit`), `HIT_SQUASH` (+), `DEATH_CRUMBLE` |
| Boss | Bộ clip nhóm của `skeleton` + clip riêng mỗi chiêu `ANM_{SKELETON}_{MODEL_KEY}_{CHIÊU}` (cảnh báo dài, có marker `hit`; tách `_CHARGE` / `_RELEASE` khi cần — AR-03 ANM-R-070) + `RECOVER_LOOP` (đứng hở, ANM-R-071) + `PHASE_TRANSITION` |

Linh thú (GD-25) **dùng lại** clip của quái cùng nhóm, thêm `FOLLOW_IDLE`, `HAPPY` (thân mật cao).

---

## 6. Ngân sách (trích TD-11 §3, §3.1 — nguồn số là TD-11)

Cột `budgetClass` là mã hàng mà tracker (AR-05 §4 cột 6) và `art:check` dùng để tra ngân sách (chốt AR-05 TRK-Q-5). **Số trong bảng chỉ là trích dẫn**: `art:check` lấy số từ TD-11 theo mã hàng; TD-11 đổi số thì bảng này phải sửa theo, không ngược lại.

| `budgetClass` | Asset (`kind`) | Tam giác | Texture | Xương / khác |
|---------------|----------------|---------|---------|--------------|
| `CHARACTER` | Nhân vật hình người (`CHR`) | LOD0 ≤ 5k · LOD1 ≤ 2k · LOD2 ≤ 800 (LOD bắt buộc, PERF-R-020) | Atlas chung + 1 texture vẽ tay ≤ 1024² (KTX2) | ≤ 60 bone; GLB ≤ 600 KB |
| `OUTFIT` | Trang phục (`OUT`, `COS` mesh) | ≤ 3k phần thay | Dùng chung / ≤ 512² | ≤ 300 KB |
| `MONSTER_NORMAL` | Quái thường (`MDL_MON`) | LOD0 ≤ 3k · LOD1 ≤ 1,2k | ≤ 512² | ≤ 400 KB |
| `MONSTER_ELITE` | Tinh anh / thú lớn (`MDL_MON`) | ≤ 6k | ≤ 1024² | |
| `BOSS` | Boss (`MDL_MON`) | ≤ 15k | ≤ 2048² | |
| `PROP` | Prop (`PRP`, `WPN`) | 500–1.500 (AR-06 KIT-R-041 hiểu 1.500 là trần, dưới 500 hợp lệ — chờ TD-11 xác nhận, KIT-Q-7) | Atlas gradient chung; đặc biệt 256–512² | `collisionTemplate` (§7) |
| `PROP_LARGE` | Prop lớn / kiến trúc (`PRP`) | ≤ 4k | Atlas gradient chung; đặc biệt 256–512² | `collisionTemplate` (§7) |
| `VFX_SKILL` | VFX một kỹ năng (`VFX`) | Hạt ≤ 300 (preset Cao), mesh ≤ 500 | Atlas VFX ≤ 1024² | Sống ≤ 3s (hiệu ứng một lần) |
| `ICON` | Icon (`ICO`) | — | 128² webp | Gộp sprite sheet |
| `PORTRAIT` | Chân dung (`POR`) | — | ≤ 512² | Gộp sprite sheet |
| `MAP` | Kit / bản đồ (`KIT`; đo trên bản đồ dùng kit, TD-13 §7) | Draw call môi trường ≤ 150 trong khung (trong tổng ≤ 200) | Tổng tải dã ngoại ≤ 5MB · thành trấn ≤ 6MB · instance ≤ 2MB | Đa giác va chạm ≤ 2.000 |
| `—` | Hoạt ảnh (`ANM`, ngân sách theo GLB nhóm — ANM-Q-3); `ENV`, `TER`, `UI`, `ILL`, `WMAP` (TD-11 chưa có hàng — AST-Q-4) | — | — | `art:check` chỉ kiểm lũy thừa 2 / định dạng (AST-R-040), không chặn theo số |

- <a id="ast-r-042"></a>**AST-R-042** `budgetClass` là **danh sách đóng** theo bảng trên; thêm mã mới chỉ khi TD-11 §3 có hàng tương ứng. Asset `PRP` chọn `PROP` hay `PROP_LARGE` theo mô tả trong kit (AR-06 §4: "lớn / kiến trúc"); `MDL_MON` theo `rank` của `MonsterDef` (thường / tinh anh / boss).

- <a id="ast-r-040"></a>**AST-R-040** Texture lũy thừa của 2; KTX2 (UASTC cho texture vẽ tay nhân vật, ETC1S cho atlas / prop); mipmap bật; không texture normal / roughness riêng cho phong cách toon (trừ boss / điểm nhấn được duyệt).
- <a id="ast-r-041"></a>**AST-R-041** Vật liệu: một material / asset (nhân vật tối đa 2: thân + tóc / lụa trong suốt); shader do client gán (toon / viền / thủy mặc, TD-05) — glTF chỉ mang base color + tham số `extras` (vd. `"toonRamp": "RAMP_SKIN"`).
- <a id="ast-r-043"></a>**AST-R-043** Khóa `extras` trong GLB là **danh sách đóng** (`art:check` cảnh báo khóa lạ, fail khi sai kiểu):

| Khóa / quy ước | Gắn ở | Kiểu / giá trị | Nguồn |
|----------------|-------|----------------|-------|
| `toonRamp` | material | chuỗi `RAMP_*` (TD-05) | AST-R-041 |
| `collisionTemplate` | node gốc prop | `{ kind: 'CIRCLE', r, flags } \| { kind: 'POLYGON', verts, flags }` — **tối đa một** mỗi node; mảnh chân lõm tách nhiều mảnh (AR-06 KIT-R-016, mặc định KIT-Q-6) | AST-R-050 |
| `kit` | node gốc mảnh kit | `KIT_*` | AR-06 §3.3 |
| `category` | node gốc mảnh kit | `MODULE` · `STRUCTURE` · `SCATTER` · `ACCENT` · `POI` · `DYNAMIC` (`KitCategory`) | AR-06 §3.1 |
| `edges` | node gốc mảnh `MODULE` | `{ N?, E?, S?, W? }` mỗi giá trị một `EdgeProfile` (AR-06 §2.2) | AR-06 §3.3, TD-13 MF-R-032 |
| `droppable` | node gốc mảnh `SCATTER` | boolean — được giảm theo preset | AR-06 KIT-R-036, TD-13 MF-R-035 |
| `roofHide` | node **mái** (node con riêng) | boolean — client ẩn khi nhân vật vào trong | AR-06 KIT-R-013, GD-24 CAM-R-013 |
| `vfx:{VFX_ID}` | **tên** node rỗng trong scene bản đồ (không phải khóa `extras`) | `VFX_ID` tồn tại trong `vfx/effects.json` | AR-06 §8, AR-04 §8.1 |
| `events` | `extras` của AnimationGroup | `[{ name: 'hit' \| 'hit_n' \| 'cancel' \| 'vfx_spawn', t }]` | AST-R-031 |

---

## 7. `collisionTemplate` cho prop (TD-13, TD-14)
- <a id="ast-r-050"></a>**AST-R-050** Prop có va chạm khai báo trong `extras` của node gốc, **không** dùng va chạm theo mesh:
```jsonc
{ "collisionTemplate": { "kind": "CIRCLE", "r": 0.6,
                         "flags": { "move": true, "projectile": true, "sight": false, "fly": false } } }
// hoặc { "kind": "POLYGON", "verts": [[-1,-0.5],[1,-0.5],[1,0.5],[-1,0.5]], "flags": {...} }  — tọa độ cục bộ (x, z), lồi, CCW
```
  Mặc định theo loại kit: cây / cột → `CIRCLE`; nhà / đá lớn → `POLYGON`; cỏ / hoa → không va chạm; vật cao (vách núi, tháp) → `fly: true` (GD-31). Editor tự sinh va chạm khi đặt prop (TD-13 E3).

---

## 8. Quy trình xuất
```
Blender (nguồn .blend trong art-source/, Git LFS)
  1. Kiểm tra: scale / rotation đã apply, pivot đúng §2, tên xương §4, marker hit §5.1
  2. Script AI-viết tools/art/blender_export.py: decimate về ngân sách (nếu từ TRELLIS.2), LOD1/LOD2, UV atlas / bake, gán extras (AST-R-043)
     2b. Mảnh kit môi trường: thêm tools/art/kit_conform.py (AR-06 §12 bước 4): lượng tử màu về MaterialGroup + UV vào ENV_GRADIENT_ATLAS,
         ép footprint bội số 0,5m (MODULE), sinh collisionTemplate mặc định, ghi extras kit / category / edges / droppable / roofHide,
         tách node mái, cập nhật art/env/{kit}/kit.json
  3. Export glTF 2.0 (GLB): +Y Up, chỉ selected; hoạt ảnh theo NLA track (mỗi track = 1 clip, tên = mã clip AR-03) → art/animations/{SKELETON}.glb
glTF-Transform (pnpm art:optimize <file>)
  4. dedup, prune, weld, Meshopt, KTX2 (UASTC / ETC1S theo AST-R-040), resize texture theo ngân sách
CI (pnpm art:check)
  5. Kiểm tra (§8.1) — fail chặn merge
  6. Ảnh xem trước tự động đúng camera game (AR-01 §9) đính kèm PR
pnpm art:check --report
  7. Ghi đè khối dashboard của AR-05 §8.2 (giữa marker TRK:DASHBOARD) — chạy ở CI nhánh main hoặc cục bộ
```

### 8.1. Phạm vi `pnpm art:check`
Một lệnh duy nhất (không tạo lệnh mới cho từng tài liệu — AR-04 §11.4); đọc `art/`, `art/provenance/`, `tools/art/asset-tracker.csv`, `tools/art/anim-list.json`, `tools/art/skeletons/*.json`, `art/env/*/kit.json`, bảng TD-15 §4.5 và bundle gamedata.

| Nhóm | Kiểm tra | Nguồn quy tắc |
|------|----------|---------------|
| Kỹ thuật asset | Ngân sách theo `budgetClass` (TD-11); tên / ID đúng §3; scale / pivot; tên xương; marker `hit`; `collisionTemplate` hợp lệ; khóa `extras` (AST-R-043); texture lũy thừa 2 / KTX2 | AST-R-002..050, AST-AC-001..008 |
| Tracker | `tools/art/asset-tracker.csv` đúng schema, chuyển trạng thái hợp lệ so với `main`, file trong `art/` có dòng, `owner` / `reviewer` không chứa `@`, `measured` (CI điền) | AR-05 §4, §6, TRK-AC-001..015 |
| Catalogue hoạt ảnh | Mọi clip trong `art/animations/{SKELETON}.glb` có trong `tools/art/anim-list.json` (và ngược lại với clip `IN_GAME`); frame, loop, marker, root drift, khóa thời lượng theo `checks`; số clip combo = `len(WeaponDef.basicCombo)` | AR-03 ANM-R-006, ANM-R-060, ANM-AC-* |
| License | Mọi tên trong `tools` của tracker và `tools` / `models` của hồ sơ nguồn gốc có dòng TD-15 §4.5; tính cột `license` (`OK` / `PENDING` / `BLOCKED`, TD-15 OSS-R-051); `RELEASE` ở `ART_REVIEW` / `IN_GAME` phải `OK`; `source` đúng enum TD-15 §3.4 | TD-15 OSS-R-005, OSS-R-031, OSS-R-051; AR-05 TRK-R-012 |
| Kit môi trường | Footprint bội số 0,5m, khe ghép ≤ 0,01m, ngưỡng `fly`, texture riêng chỉ cho `ACCENT`, `kit.json` khớp GLB | AR-06 KIT-AC-001..008, 016 |
| VFX | Mesh ≤ 500 tam giác, atlas ≤ 1024², hồ sơ nguồn gốc, mô phỏng ở giá trị trần | AR-04 VFX-R-106 |
| Tham chiếu gamedata | `modelId`, `iconId` trỏ asset có dòng tracker, đã `IN_GAME`, không `SAMPLE` | AR-05 TRK-R-012, TRK-AC-010 |

- <a id="ast-r-060"></a>**AST-R-060** Cờ của lệnh: `--report` (ghi dashboard AR-05 §8.2), `--write` (cho phép `art:check` tự ghi cột (CI) và đặt `REJECTED` khi chạy cục bộ — AR-05 §6), `--json` (cho công cụ khác đọc). Không cờ: chỉ đọc, in lỗi dạng `[LOẠI] đường dẫn  assetId → mô tả` như TD-07 DT-R-092.

---

## 9. Checklist trước khi commit
1. `assetId` đúng mẫu §3, chưa từng dùng.
2. Đơn vị m, pivot đúng, transform đã apply.
3. Tam giác / texture / bone / dung lượng ≤ TD-11; có LOD nếu là nhân vật / quái.
4. Nhân vật / quái: đúng bộ xương; clip tấn công có `hit`.
5. Prop: `collisionTemplate` đúng loại; vật cao có `fly: true`.
6. Material / `extras` theo AST-R-041.
7. Đã chạy `pnpm art:optimize` + `pnpm art:check` xanh.
8. **Hồ sơ nguồn gốc** đầy đủ (công cụ, mô hình + license, prompt, seed, đầu vào, người sửa, người duyệt) — TD-15 §3.4.
9. Ảnh xem trước đúng camera game được art director duyệt (AR-01 §9).

---

## 10. Hồ sơ nguồn gốc
Định dạng chuẩn ở **TD-15 §3.4** (`art/provenance/{assetId}.json`: `source` ∈ `AI` · `CC0` · `CC_BY` · `PURCHASED` · `IN_HOUSE` · `SERVICE`, `tools`, `models` + license, `prompts`, `seeds`, `inputs`, `editedBy`, `reviewedBy`, `date`, `attribution`). Hoạt ảnh lấy từ Mixamo / AccuRIG ghi `source = SERVICE` (TD-15 OSS-R-034). Mô hình bị cấm cho asset phát hành (vd. FLUX.1 dev, Hunyuan3D, thành phần ❌ của pipeline TRELLIS.2) → CI chặn (OSS-AC-006).

---

## 11. Tiêu chí nghiệm thu
| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="ast-ac-001"></a>AST-AC-001 | GLB nhân vật 6.500 tam giác LOD0 | `art:check` | Fail (≤ 5k) |
| AST-AC-002 | Nhân vật thiếu xương `socket_weapon_r` | `art:check` | Fail (bộ xương chung) |
| AST-AC-003 | Clip `ATTACK_1` không có marker `hit` | `art:check` | Fail |
| AST-AC-004 | Prop cây không có `collisionTemplate` | `art:check` | Cảnh báo (kit `BAMBOO` mặc định `CIRCLE`) |
| AST-AC-005 | Asset không có `art/provenance/{assetId}.json` | CI | Fail |
| AST-AC-006 | Nhân vật xuất với scale 100 | Nạp vào client | Bị chặn ở `art:check` (scale ≠ 1) |
| AST-AC-007 | Clip có `hit` ở 0,30s, `SkillDef` trúng ở 0,45s | Phát trong game | Clip co giãn để `hit` = 0,45s (TD-05 CL-AC-012) |
| AST-AC-008 | Texture 1000×1000 | `art:optimize` | Đổi về 1024² hoặc báo lỗi kích thước không lũy thừa 2 |
| <a id="ast-ac-009"></a>AST-AC-009 | File `art/props/XYZ_TREE_01.glb` (tiền tố không có ở §3) | `art:check` | Fail (AST-R-011) |
| <a id="ast-ac-010"></a>AST-AC-010 | Node có `extras.fooBar` | `art:check` | Cảnh báo khóa lạ (AST-R-043) |
| <a id="ast-ac-011"></a>AST-AC-011 | Dòng tracker `budgetClass = PROP_HUGE` | `art:check` | Fail (AST-R-042) |
| <a id="ast-ac-012"></a>AST-AC-012 | `MonsterDef.modelId = "CHR_PLAYER_SAMPLE_A"` trong gamedata phát hành | `art:check` | Fail (AST-R-012) |

---

## 12. Câu hỏi mở
- [ ] **AST-Q-1:** Đánh khi di chuyển có cần clip strafe 8 hướng (combat WASD + nhắm chuột) hay chỉ xoay thân trên (additive) để tiết kiệm clip?
- [ ] **AST-Q-2:** Spring bone cho lụa / tóc chạy ở client (chi phí CPU mobile) hay bake vào clip?
- [ ] **AST-Q-3:** Quy chuẩn LOD cho prop lớn (kiến trúc ≤ 4k) có cần LOD1 không ở camera cố định?
- [ ] **AST-Q-4:** TD-11 §3 chưa có ngân sách cho khung UI (`UI`), minh họa (`ILL`), bản đồ thế giới (`WMAP`), texture môi trường dùng chung (`ENV`), texture địa hình (`TER`) và GLB hoạt ảnh (ANM-Q-3). *Mặc định: `budgetClass = —`, `art:check` chỉ kiểm định dạng / lũy thừa 2.* Cần TD-11 thêm hàng rồi thêm mã vào §6.
- [ ] **AST-Q-5:** Các tiền tố đề xuất ở v0.3 (`KIT_CM_`, `ENV_`, `TER_`, `UI_`, `POR_`, `ILL_`, `WMAP_`, hậu tố `_SAMPLE_{X}`, nhóm `VFX_{NHÓM}_`) cần Art Director + Tech Artist duyệt; GD-17 chưa có mã màn cho `UI_{MÀN}_*`. *Mặc định: dùng như §3; không đổi sau khi asset đầu tiên dùng.*

---

## Lịch sử thay đổi
| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến, trục / tỷ lệ / pivot, đặt tên asset, bộ xương người chung (tên xương + socket) và bộ xương quái, quy tắc hoạt ảnh + marker `hit`, danh sách clip người chơi theo `ActionState` và quái theo nhóm, ngân sách (trích TD-11), `collisionTemplate`, quy trình xuất, checklist, hồ sơ nguồn gốc, nghiệm thu |
| v0.2 | 2026-10-25 | — | Hồ sơ nguồn gốc trỏ TD-15 §3.4; `ROOTED` dùng `IDLE_COMBAT`; combo `ATTACK_1..n`; danh mục clip đầy đủ ở AR-03 |
| v0.3 | 2026-10-25 | — | Trỏ TD-11 §3 / §3.1 (thay §4 sai mục); §3 viết lại thành danh sách đóng có cột `kind` (AST-R-011): 4 dạng tên clip AR-03 + `art/animations/{SKELETON}.glb`, mẫu VFX AR-04 (`VFX_{MẪU}`, `VFX_{MẪU}_{PHẦN}`, `VFX_{NHÓM}_*`, `VFX_BOSS_*`, `VFXO_*`), mẫu AR-06 (`KIT_CM_*`, mảnh module / cấp công trình, `ENV_*`, `TER_*`), tiền tố 2D `UI_`, `POR_`, `ILL_`, `WMAP_` và hậu tố `_SAMPLE_{X}` (AST-R-012) — chốt AR-05 TRK-Q-3; `MDL_MON_{TÊN}` là mẫu `modelId` (chốt TRK-Q-6); AST-R-002 ngoại lệ KTX2 / webp; §5 trỏ AR-03 là danh mục chuẩn + đồng bộ clip (+); §6 thêm cột `budgetClass` (AST-R-042, chốt TRK-Q-5), tách `PROP` / `PROP_LARGE`, `ICON` / `PORTRAIT`, số giữ nguyên; AST-R-043 khóa `extras` (AR-06); §8 bước `kit_conform.py`, §8.1 phạm vi `art:check` (tracker, anim-list, license, `--report`), AST-R-060 cờ lệnh; AST-Q-4, AST-Q-5 |
| v0.4 | 2026-10-25 | — | Tiền tố `BGM_` (AU-01 §3.1) |

