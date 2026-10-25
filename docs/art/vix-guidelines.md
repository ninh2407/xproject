# AR-04 — VFX Guidelines

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | AR-04 · Prefix ID: `VFX` |
| Owner | Tech Artist (VFX) + Tech Lead (Client) |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 0 (mẫu VFX Hỏa + vùng cảnh báo + bảng mù màu, AR-01 §10 mẫu #5), Phase 1 (hệ VFX tham số cho 4 kỹ năng kiếm + quái / boss), Phase 4 (VFX ghép cho công pháp tự chế GD-08) |
| Quyết định nền | D-01 (logic 2D), D-09 (camera cố định), D-22 (3D phong cách hóa, mỹ thuật ưu tiên AI); PLAN §6.1, §6.6, §6.7 (mục 6: VFX bằng shader / particle có tham số); [AR-01](art-bible.md) §4, §6 (ART-R-002, ART-R-020, ART-R-040..042); [TD-11](../tech/10-performance.md) §2.1, §3 (**nguồn số ngân sách**); [TD-05](../tech/04-client.md) §5.5, §9 (CL-R-067, CL-R-068) |
| Liên quan | [AR-02 Asset Guidelines](asset-guidelines.md) §3, §4, §5.1, §6 · [AR-06 Environment Kits](environment-kits.md) · [GD-08 Chế công pháp](../design/14-technique-crafting.md) §3, §5.7, §7 · [GD-07 Kỹ năng](../design/13-skills.md) §3, §5.7, §11 · [GD-06 Chỉ số](../design/12-stats-formulas.md) §5.6, §5.12 · [GD-12 Quái](../design/18-enemies-ai.md) §5.6 · [GD-24 Camera](../design/25-camera-controls.md) CAM-R-006, CAM-R-013, CAM-R-014, CAM-R-060, CAM-R-061 · [GD-04 Tu luyện](../design/10-cultivation.md) §5.7, §11 · [GD-31 Ngự kiếm](../design/29-sword-flight.md) §11 · [GD-23 Sự kiện thế giới](../design/24-world-events.md) · [GD-28 Hiểm địa](../design/27-danger-zones.md) §5.3, §5.10 · [GD-21 Open World](../design/22-open-world.md) §3.7–3.9 · [GD-22 Cơ duyên](../design/23-encounters.md) (`presentation.vfx`) · [GD-25 Linh thú](../design/26-spirit-beasts.md) · [GD-32 PvP](../design/31-pvp-infamy.md) · [TD-02](../tech/01-network.md) NW-R-024 · [TD-07](../tech/06-gamedata.md) · [TD-14](../tech/13-geom2d.md) GEO-R-045 · [TD-15](../tech/14-oss-register.md) §4.5 |

**Tài liệu này cung cấp cho:** Tech Artist / AI viết code VFX (`client/src/render/vfx/`, `render/telegraph.ts`, `entities/systems/vfxSystem`), validator gamedata (TD-07) và asset (`pnpm art:check`), GD-08 (danh sách `vfxTemplateId`, `paletteId`, `vfxOverlayId`), GD-22 (ID cho `presentation.vfx`), GD-07 / GD-12 (cách kỹ năng tác giả viết và chiêu quái nhận VFX), AR-05 (danh sách asset VFX).

> **Quy ước "đề xuất":** mọi mã / file / kiểu dữ liệu đánh dấu **(đề xuất)** chưa có trong GD / TD nào. Code **chưa được dùng** cho tới khi mục tương ứng ở §15 được đồng bộ vào tài liệu chủ quản (TD-07, GD-07, GD-08, TD-02…). Con số ghi "khởi điểm" là giá trị ban đầu cho gamedata, không hard-code (AGENTS §4 luật 3).

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="vfx-r-001"></a>**VFX-R-001** | **Gameplay thắng thẩm mỹ.** Vùng cảnh báo (telegraph) luôn đọc rõ trên mặt đất: vẽ ở lớp trên cùng của thế giới, **không kiểm tra độ sâu**, không VFX / sương / ánh sáng / prop nào che được (ART-R-002, ART-R-041, CAM-R-006, CAM-R-013). |
| <a id="vfx-r-002"></a>**VFX-R-002** | **Màu theo nguyên tố, luôn kèm hình dạng** riêng (motif) — đúng 8 hệ của GD-06 / LR-01 WB-R-005, bảng màu lấy từ AR-01 §4.2 (§3). Không VFX nào dùng màu hệ thống (AR-01 §4.4) cho trang trí. |
| <a id="vfx-r-003"></a>**VFX-R-003** | **Phân biệt được của địch và của mình**: VFX của thực thể thù địch, của bản thân và của đồng minh khác nhau về độ đậm, viền và lớp vẽ (§4.3); vùng cảnh báo kẻ địch và đồng minh khác màu và khác dạng (§5). |
| <a id="vfx-r-004"></a>**VFX-R-004** | **Không VFX nào che vùng cảnh báo**: VFX mặt đất không có viền liền sắc nét + nền đặc phẳng (ngôn ngữ hình dạng dành riêng cho telegraph); thứ tự vẽ cố định §4.2. |
| <a id="vfx-r-005"></a>**VFX-R-005** | **VFX ghép tham số, không làm VFX riêng cho từng chiêu** (ART-R-040, TF-R-071, CL-R-067): mẫu hình dạng × bảng màu × lớp phủ; mọi kỹ năng — tác giả viết, tự chế, quái — đi qua cùng hệ (§6). Ngoại lệ duy nhất: chiêu đặc trưng boss (§6.6). |
| <a id="vfx-r-006"></a>**VFX-R-006** | **VFX chỉ là hiển thị**: không ảnh hưởng va chạm / kết quả; vị trí, thời điểm, bán kính lấy từ `SkillDef` và message server (TD-02), không suy ra từ hạt. Ngẫu nhiên thẩm mỹ dùng PRNG có seed (§10.5), không `Math.random` (AGENTS §4 luật 7). |
| <a id="vfx-r-007"></a>**VFX-R-007** | **Ngân sách theo TD-11** (hạt đồng thời theo preset, hạt / mesh / atlas / thời gian sống của một kỹ năng). Tài liệu này **không** đặt số khác; chỗ TD-11 chưa có số được ghi là đề xuất chờ TD-11 duyệt (§9). |
| <a id="vfx-r-008"></a>**VFX-R-008** | Mọi asset VFX (texture, mesh, flipbook) có **hồ sơ nguồn gốc** (AST-R-005, TD-15 §4.5) và qua `pnpm art:check`. |

---

## 2. Phạm vi và thuật ngữ

| Thuật ngữ | Nghĩa trong tài liệu |
|-----------|----------------------|
| **Telegraph / vùng cảnh báo** | Hình trên mặt đất báo trước một lượt trúng, vẽ từ `shapeOutline` của `@tienlo/sim` (GEO-R-045, CL-R-068) |
| **Mẫu VFX (template)** | Hiệu ứng theo **hình dạng** (`Shape.type` GD-07), nhận tham số số học từ `SkillDef`; GD-08 gọi là `vfxTemplateId` |
| **Bảng màu VFX (palette)** | Bộ màu + motif của một nguyên tố; GD-08 gọi là `paletteId` |
| **Lớp phủ (overlay)** | Thành phần cộng thêm do biến thức (vệt sáng, nổ, nhân bản…); GD-08 gọi là `vfxOverlayId`, mẫu tên `VFXO_*` (AR-02 §3) |
| **Pha VFX** | `CAST` (tụ lực / anticipation) → `RELEASE` (phóng / di chuyển) → `IMPACT` (bùng sáng) → `LINGER` (vùng tồn tại, lặp) → `FADE` (tàn quang / follow-through) — ART-R-042 |
| **Ràng buộc thời gian** | Pha khớp `castTimeSec`, `windupSec`, `hits[].atSec`, `shape.delaySec` / `persistSec` / `durationSec` của `SkillDef`; marker clip `hit`, `vfx_spawn` (AR-02 AST-R-031) |

Không thuộc phạm vi: số sát thương bay, nameplate, thanh máu (Babylon GUI, CL-R-082 / GD-17); màn chuyển bản đồ mây thủy mặc (UI, GD-21 §6); ánh sáng ngày / đêm (TD-05 CL-R-061).

---

## 3. Bảng màu VFX theo nguyên tố

### 3.1. Tám bảng màu nguyên tố
Tên màu, mã hex và motif lấy **nguyên văn AR-01 §4.2** (đã đồng bộ ở AR-01 v0.2 — AR-01 là nguồn màu); hex là **giá trị khởi điểm** để sinh gamedata, hiệu chỉnh bằng mẫu Phase 0 (AR-01 §10 mẫu #5) thì sửa AR-01 trước.

| Hệ (`Element`) | `paletteId` (GD-08 §7.2) | Màu chính (AR-01) | Màu lõi (AR-01) | Hex chính / lõi / viền tối (AR-01, khởi điểm) | Motif (hình dạng nhận diện, AR-01) | Status hệ (GD-08 §7.2) |
|----|----|----|----|----|----|----|
| `KIM` | `VPAL_KIM` | Vàng kim nhạt | Trắng ngà | `#E8D28A` / `#FFF6E0` / `#8F7535` | Mảnh kim sắc cạnh, vệt chém thẳng | `ST_ARMOR_BREAK` |
| `MOC` | `VPAL_MOC` | Lục lá | Lục non | `#4FA34A` / `#B8F07A` / `#2B5A28` | Lá non, dây leo xoắn | `ST_ENTANGLE` |
| `THUY` | `VPAL_THUY` | Lam nước | Lam nhạt | `#3C8FD6` / `#A9DBFF` / `#1D4A75` | Giọt nước, gợn sóng tròn | `ST_FROST` |
| `HOA` | `VPAL_HOA` | Đỏ cam | Vàng lửa | `#E8602A` / `#FFD65C` / `#7E2610` | **Lưỡi lửa** (ART-R-020) | `ST_BURN` |
| `THO` | `VPAL_THO` | Nâu hổ phách | Vàng đất | `#B07A32` / `#E8C474` / `#5A3B16` | Đá vụn, bụi cuộn | `ST_HEAVY` |
| `LOI` | `VPAL_LOI` | Tím điện | Trắng tím | `#8A4DFF` / `#E6D9FF` / `#3A1D7A` | **Tia gấp khúc** (ART-R-020) | `ST_SHOCK` |
| `BANG` | `VPAL_BANG` | Lam băng | Trắng xanh | `#6FD3F0` / `#E8FBFF` / `#2A7590` | **Tinh thể sáu cạnh** (ART-R-020) | `ST_FROST` |
| `PHONG` | `VPAL_PHONG` | Lục ngọc nhạt | Trắng xanh lục | `#7FD9B5` / `#E3FFF4` / `#2D755C` | Dải gió xoắn, vệt cong mảnh | `ST_WIND_CUT` |

- <a id="vfx-r-010"></a>**VFX-R-010** Mỗi bảng màu gồm 3 màu (chính, lõi, viền tối) + 1 motif. Lõi dùng cho tâm sáng / bloom; viền tối dùng để tách hiệu ứng khỏi nền sáng (ban ngày, mây trắng). Không dùng đen tuyền.
- <a id="vfx-r-011"></a>**VFX-R-011** **Motif bắt buộc** ở pha `RELEASE` và `IMPACT` của mọi kỹ năng có nguyên tố (người mù màu vẫn phân biệt hệ, ART-R-020, GD-24 trợ năng). Validator dữ liệu kiểm mọi `VPAL_*` có `motif`.
- <a id="vfx-r-012"></a>**VFX-R-012** **Vô hệ / `INHERIT` không có hệ:** kỹ năng có `element = NONE`, hoặc `INHERIT` khi tâm pháp chính vô hệ (vd. `TEC_DAN_KHI`, GD-07 §7.1), dùng **`VPAL_NONE`**: bạc trắng linh quang `#D8DEE8` / `#FFFFFF` / `#6B7380`, motif "khí tròn mảnh" (đã ghi vào AR-01 §4.2, ART-R-022; VFX-Q-1 đã chốt).
- <a id="vfx-r-013"></a>**VFX-R-013** **Hỗ trợ (hồi máu / khiên):** GD-08 TF-R-030 bước 3 — hạch tâm `heal` / `shield` chỉ đổi bảng màu theo nguyên tố, không thêm status; khi không có nguyên tố thì `VPAL_NONE`. Không có "màu hồi máu" riêng (tránh thêm màu mang nghĩa ngoài AR-01).

### 3.2. Màu hệ thống dùng trong VFX (nguồn AR-01 §4.4, GD-24 CAM-R-061)

| Nghĩa | Màu (AR-01 / GD-24) | Bảng thay thế mù màu | Dùng ở |
|-------|---------------------|----------------------|--------|
| Vùng cảnh báo kẻ địch | Đỏ cam bán trong suốt, viền sáng, độ đậm 0,6–1 | `DEUTERANOPIA` · `PROTANOPIA` · `TRITANOPIA` + sọc chéo (CAM-R-061) | `VFX_TG_ENEMY` |
| Đòn không thể đỡ | Viền đỏ đậm + sọc (CAM-R-061, SF-R-102) + lóe đỏ trên quái (EN-R-071) | Như trên, sọc luôn bật | `VFX_TG_UNBLOCKABLE`, `VFX_CB_UNBLOCKABLE_FLASH` |
| Vùng cảnh báo đồng minh / kỹ năng mình | Lam trắng mờ (AR-01) / xanh lam (CAM-R-061) | Như trên | `VFX_TG_ALLY`, `VFX_TG_AIM` |
| Vùng an toàn | Viền lục nhạt mặt đất khi vào / ra | — | `VFX_WLD_SAFE_EDGE` |
| Phẩm vật phẩm / huyết mạch | AR-01 §4.3 (5 bậc) | Kèm số vạch / hình mây (ART-R-021) | Cột sáng rơi đồ, hào quang Linh chủng |

- <a id="vfx-r-014"></a>**VFX-R-014** Giá trị hex của bốn bảng telegraph (`DEFAULT` + 3 bảng mù màu) ở **GD-24 CAM-R-061** (nguồn số; §11.3 chỉ là trích mẫu JSON); khởi điểm, hiệu chỉnh bằng mẫu Phase 0 #5 thì sửa GD-24 trước (VFX-Q-2 đã chốt).
- <a id="vfx-r-015"></a>**VFX-R-015** **Xung đột Hỏa ↔ cảnh báo:** `VPAL_HOA` (đỏ cam) trùng tông với vùng cảnh báo kẻ địch. Bắt buộc: (1) VFX Hỏa trên mặt đất **không** có viền liền + nền phẳng (dùng nhiễu, lưỡi lửa, hạt); (2) telegraph vẽ unlit, độ đậm ≥ 0,6, có viền sáng liên tục và **lấp dần theo thời gian** (GD-12 §11); (3) kiểm bằng ảnh chụp đúng camera game: "telegraph quái nằm trong vùng lửa đồng đội" là ca duyệt bắt buộc (AR-01 §8). Tương tự Thủy / Băng ↔ cảnh báo đồng minh lam, Phong ↔ viền an toàn lục nhạt (VFX-Q-3).

---

## 4. Phân loại, lớp vẽ, phân biệt địch / ta

### 4.1. Mười nhóm VFX

| Nhóm (`VfxCategory`, đề xuất) | Tiền tố ID | Nội dung | Nguồn dữ liệu kích hoạt |
|------|-----|----------|----------|
| `TELEGRAPH` | `VFX_TG_` | Vùng cảnh báo, xem trước ngắm, vòng sét thiên kiếp | `S2C_SkillCast.telegraph` (GD-07 §9.3), `S2C_TribulationWave` (GD-04 §9.3) |
| `CAST` | `VFX_{MẪU}_CAST` | Tụ lực, niệm, vận công kỹ năng | `ActionState` `CASTING` / `HEAVY_CHARGE` (GD-07 §4) |
| `PROJECTILE` | `VFX_{MẪU}_CORE` / `_TRAIL` | Đạn, sóng kiếm, vật bay | `S2C_ProjectileSpawn` (TD-02) |
| `HIT` | `VFX_HIT_` | Trúng, chí mạng, đỡ, đỡ hoàn hảo, phá thế | `S2C_HitResults` (GD-06 §9.3) |
| `STATUS` | `VFX_ST_` | Hiệu ứng trạng thái trên thân | `statusIcons` / `S2C_StatusUpdate` (GD-06 §5.12) |
| `ENVIRONMENT` | `VFX_ENV_`, `VFX_AMB_`, `VFX_DZ_`, `VFX_POI_` | Thời tiết, không khí theo bản đồ, nguy hiểm hiểm địa, POI | World Clock / thời tiết (GD-21 §3.8–3.9), `MapDef`, `S2C_DangerPhase` (GD-28) |
| `WORLD_UI` | `VFX_WLD_` | Quét thần thức, cột sáng rơi đồ, lên tầng, tín hiệu cơ duyên, hào quang, vùng an toàn | Message tương ứng của từng GD |
| `BREAKTHROUGH` | `VFX_BT_`, `VFX_TRIB_` | Vận công đột phá, thành công / thất bại, thiên kiếp, binh giải | `S2C_BreakthroughPhase`, `S2C_TribulationWave`, `S2C_BreakthroughResult` (GD-04 §9.3) |
| `FLIGHT` | `VFX_FLY_` | Cất cánh, vệt bay, bóng, đáp, rơi | `FlightState` (GD-31 §3) |
| `EVENT` | `VFX_EVT_` | Sự kiện thế giới: linh khí bùng phát, cột sáng dị bảo, viền vùng sự kiện… | `EventState` / `EventKind` (GD-23 §3) |

Nhóm phụ `VFX_CB_` (combat feedback: né, giáp, hào quang tinh anh / boss, xuất hiện / chết của quái) xếp trong `HIT`.

### 4.2. Thứ tự lớp vẽ
- <a id="vfx-r-020"></a>**VFX-R-020** Thứ tự (dưới → trên), cố định trong `render/vfx`:

| Lớp | Nội dung | Kiểm tra độ sâu |
|:---:|----------|:---:|
| 1 | Địa hình, decal mặt đất trang trí (AR-06) | Có |
| 2 | VFX mặt đất của kỹ năng / môi trường (vùng tồn tại, vết cháy, sương hiểm địa) | Có |
| 3 | Thực thể, VFX thân (status, hào quang, cast), đạn, VFX không khí | Có |
| 4 | Thời tiết, hạt không khí | Có |
| 5 | **Telegraph** (kẻ địch trên đồng minh), vòng chân nhân vật mình, viền vật được quét | **Không** (CAM-R-006) |
| 6 | Nameplate, số sát thương (Babylon GUI) | Không |

- <a id="vfx-r-021"></a>**VFX-R-021** Telegraph **không chịu** bloom, sương (scene fog), hậu xử lý thủy mặc, ánh sáng ngày / đêm (vẽ unlit) — đêm không được làm telegraph tối đi (GD-21 §3.8, CL-R-061).
- <a id="vfx-r-022"></a>**VFX-R-022** Bloom có trần (ART-R-041): tổng độ sáng vượt ngưỡng bị nén (tone-map) để màn hình không trắng xóa khi 20 người cùng thi triển (`SCN-BOSS-20`, TD-11 §7.2).

### 4.3. Phân biệt địch / ta (VFX-R-003)
- <a id="vfx-r-025"></a>**VFX-R-025** Quan hệ người xem ↔ nguồn VFX quyết định cách hiển thị:

| Quan hệ (với người xem) | Độ đậm hạt | Viền | Telegraph |
|-------------------------|-----------|------|-----------|
| **Bản thân** | 100% (theo preset) | Không | `VFX_TG_AIM` khi giữ để ngắm (GD-07 §11) |
| Tổ đội / đồng minh | Theo dòng "VFX người chơi khác" TD-05 §9 | Không | `VFX_TG_ALLY` (tắt được, GD-07 §11) |
| Người chơi khác không thù địch | Như trên | Không | Không |
| **Thù địch** (quái, người chơi đang PvP với mình — GD-32) | 100% (theo preset), **không** bị giảm bởi dòng "VFX người chơi khác" | **Viền thù địch**: vành mảnh màu cảnh báo kẻ địch quanh lõi đạn / vật bay | `VFX_TG_ENEMY` / `VFX_TG_UNBLOCKABLE` |

- <a id="vfx-r-026"></a>**VFX-R-026** Đạn / vật bay **thù địch** là thông tin gameplay (để né): ở mọi preset, mọi LOD, mọi mức giảm khi đông (§10) vẫn vẽ ít nhất **hình dạng tối giản** (lõi + viền thù địch), không bao giờ bị loại.
- <a id="vfx-r-027"></a>**VFX-R-027** Tùy chọn "giảm độ đậm VFX người khác" (ART-R-041) **chỉ** có ở preset (TD-05 §9); GD-24 `AccessibilitySettings` không có trường riêng (GD-24 CAM-R-064; VFX-Q-4 đã chốt).

---

## 5. Vùng cảnh báo (telegraph)

- <a id="vfx-r-030"></a>**VFX-R-030** Hình học lấy từ `shapeOutline(shape, params)` (GEO-R-045): **không** cộng bán kính thân mục tiêu. Đếm ngược theo `serverStartMs` quy đổi **không trễ nội suy** (NW-R-024, CL-R-068); thời gian hiển thị = `telegraphSec − RTT/2`.
- <a id="vfx-r-031"></a>**VFX-R-031** Dạng vẽ chuẩn `VFX_TG_ENEMY`: nền màu bảng hiện hành, độ đậm = `telegraphOpacity` (0,6–1, GD-24); **viền sáng liên tục** dày khởi điểm 0,08m; **phần lấp dần** từ tâm ra (vùng tròn / quạt) hoặc từ gốc ra (đường thẳng / lướt) theo tỷ lệ thời gian đã trôi; lóe viền 0,1s lúc chạm 100%. Sọc chéo khi `telegraphPattern = true`.
- <a id="vfx-r-032"></a>**VFX-R-032** `VFX_TG_UNBLOCKABLE`: như trên + viền đỏ đậm gấp đôi + **sọc luôn bật** (kể cả khi `telegraphPattern = false`) + `VFX_CB_UNBLOCKABLE_FLASH` trên thân quái lúc bắt đầu windup (EN-R-071).
- <a id="vfx-r-033"></a>**VFX-R-033** `VFX_TG_ALLY` (lam trắng mờ): chỉ viền + nền rất nhạt, **không** lấp dần; người chơi tắt được (GD-07 §11). Không bao giờ vẽ đè lên `VFX_TG_ENEMY` (lớp 5 vẽ địch sau cùng).
- <a id="vfx-r-034"></a>**VFX-R-034** Quái tấn công cơ bản có cảnh báo dạng "rung vũ khí" bằng hoạt ảnh (EN-R-072) — không cần decal; chiêu khác của quái luôn có decal (`telegraphForOthers = true`, EN-R-070). `GROUND_CIRCLE` có `delaySec > 0` của người chơi hiện cho mục tiêu theo SK-R-081 (với người chơi PvP thù địch dùng `VFX_TG_ENEMY`).
- <a id="vfx-r-035"></a>**VFX-R-035** Telegraph dùng **một mesh động gộp** (batch) cho mọi vùng cảnh báo đang hiện trong khung để giữ draw call thấp; tối đa 64 vùng đồng thời (khởi điểm, `vfx/params.json → telegraph.maxActive`; TD-05 CL-R-068, TD-11 PERF-R-034).
- <a id="vfx-r-036"></a>**VFX-R-036** Vòng sét thiên kiếp (GD-04 CU-R-072, `S2C_TribulationWave.strikes[]`) và bẫy hiểm địa đã lộ (GD-28 DZ-R-101) dùng dạng `VFX_TG_ENEMY` (nguy hiểm môi trường được coi như kẻ địch).

---

## 6. Hệ VFX tham số (GD-08 TF-R-071, CL-R-067)

### 6.1. Ba thành phần
```
VFX của một lần thi triển = Template(Shape.type, tham số SkillDef)
                          × Palette(nguyên tố)
                          × Overlay[](biến thức theo priority; gồm đột biến)
                          × Flavor(damageType)  × Quan hệ người xem (§4.3) × Preset / LOD (§9–§10)
```

### 6.2. Mẫu VFX (`vfxTemplateId`) — 8 mẫu của GD-08 + `VFX_THRUST`
GD-08 §5.7 quy định "8 mẫu" (một mẫu cho mỗi hạch tâm); tên theo mẫu `VFX_{MẪU}` của AR-02 §3, đã điền vào `CoreDef.vfxTemplateId` (GD-08 §7.1) và quy tắc suy theo `shape.type` (GD-07 SK-R-083).

| `vfxTemplateId` | Hạch tâm GD-08 | `Shape.type` nhận | Pha / phần (ID con `VFX_{MẪU}_{PHẦN}`) | Tham số SkillDef mẫu đọc (đúng tên trường GD-07 / GD-08 `PatchPath`) |
|-----------------|----------------|-------------------|-----------------------------------|------------------------|
| `VFX_BOLT` | `CORE_BOLT` Phi Xạ | `PROJECTILE` | `_CAST` · `_CORE` · `_TRAIL` · `_HIT` | `shape.speed`, `shape.range`, `shape.radius` (cỡ lõi), `shape.pierce` (vệt xuyên), `shape.count` + `shape.spreadDeg` (số luồng, góc tỏa), `shape.explodeRadius` (nổ ở `_HIT`), `windupSec` |
| `VFX_SWEEP` | `CORE_SWEEP` Hoành Tảo | `CONE` | `_CAST` · `_ARC` · `_HIT` | `shape.radius`, `shape.angleDeg`, `windupSec`, `hits[0].atSec` |
| `VFX_SMITE` | `CORE_SMITE` Lạc Địa | `GROUND_CIRCLE` (sát thương) | `_CAST` · `_FALL` · `_IMPACT` · `_LOOP` | `shape.radius`, `shape.maxRange`, `shape.delaySec` (thời gian rơi = telegraph), `shape.persistSec` + `hits[*].ticks.count` / `intervalSec` (pha `LINGER`) |
| `VFX_DASH` | `CORE_DASH` Thuấn Trảm | `DASH` | `_CAST` · `_STREAK` · `_AFTERIMAGE` · `_HIT` | `shape.distance`, `shape.width`, `shape.iframe` (dư ảnh khi i-frame) |
| `VFX_NOVA` | `CORE_NOVA` Hộ Thể Bạo | `SELF_CIRCLE` | `_CAST` · `_BURST` · `_HIT` | `shape.radius`, `windupSec` |
| `VFX_FAMILIAR` | `CORE_FAMILIAR` Ngự Vật | `SUMMON` | `_SPAWN` · `_LOOP` · `_ATTACK` · `_DESPAWN` | `shape.durationSec`, `shape.maxActive`; model vật triệu hồi lấy từ `summons.json` (GD-07 SK-R-090) |
| `VFX_BLOOM` | `CORE_BLOOM` Hồi Xuân | `GROUND_CIRCLE` có `heal`, `target = ALLIES` | `_CAST` · `_FIELD` · `_TICK` | `shape.radius`, `shape.persistSec`, `hits[*].ticks` |
| `VFX_AEGIS` | `CORE_AEGIS` Kim Thân | `SELF` | `_CAST` · `_SHELL` · `_BREAK` | `hits[*].shield.durationSec`, `selfStatuses[*].durationSec` |
| `VFX_THRUST` **(ngoài GD-08; GD-07 SK-R-083, VFX-Q-13 đã chốt)** | — (không có hạch tâm) | `LINE` | `_CAST` · `_STAB` · `_HIT` | `shape.length`, `shape.width` |

- <a id="vfx-r-040"></a>**VFX-R-040** Mẫu **co giãn theo tham số**, không theo ID kỹ năng: `shape.radius ×1,3` (`MOD_WIDEN`) → vòng VFX ×1,3; `shape.count +2` (`MOD_SPLIT`) → 3 luồng tỏa `spreadDeg`; `shape.persistSec +2` (`MOD_LINGER`) → pha `LINGER` dài thêm 2s. Kích thước hiển thị = kích thước logic (vùng VFX khớp vùng trúng ±5%) để người chơi học được tầm thật.
- <a id="vfx-r-041"></a>**VFX-R-041** **Trần hiển thị** khớp giới hạn cứng GD-08 (`hardLimits`: `maxProjectiles` 7, `maxRadius` 8m): mẫu phải được kiểm ở giá trị trần (VFX-AC-007).
- <a id="vfx-r-042"></a>**VFX-R-042** **Flavor theo `damageType`:** `PHYSICAL` = "kiếm khí" (vệt lưỡi liềm sắc, lõi mảnh, vệt chuyển động); `SPELL` = "linh quang" (lõi tròn phát sáng, vòng phù văn). Cùng mẫu, đổi texture / hình hạt qua tham số `flavor` (không tách mẫu).
- <a id="vfx-r-043"></a>**VFX-R-043** Pha khớp thời gian kỹ năng: `CAST` kéo dài `castTimeSec + windupSec` (giữ vòng khi `CAST_CHANNEL_LOOP`); `IMPACT` bắt đầu đúng `hits[i].atSec` (sau windup) hoặc marker `vfx_spawn` của clip (AST-R-031); `FADE` ≤ 0,5s (khởi điểm). Điểm phát lấy từ socket `socket_weapon_r` / `socket_chest_fx` (AR-02 §4).

### 6.3. Bảng màu (`paletteId`)
- <a id="vfx-r-045"></a>**VFX-R-045** `ElementDef.paletteId` (GD-08 §3) = `VPAL_{ELEMENT}` (§3.1). Thứ tự xác định bảng màu của một lần thi triển:
  1. Kỹ năng tự chế có nguyên tố → `ElementDef.paletteId`.
  2. `damage.element` của lượt trúng đầu là một `Element` → `VPAL_{element}`.
  3. `INHERIT` → hệ tâm pháp chính **của người thi triển lúc thi triển** (TF-R-071, SK-R-030) → `VPAL_{element}`; tâm pháp vô hệ → `VPAL_NONE`.
  4. `NONE` hoặc không có `damage` (hồi / khiên không nguyên tố, `SELF`) → `VPAL_NONE`.
- <a id="vfx-r-046"></a>**VFX-R-046** Client của người xem khác đọc hệ tâm pháp của người thi triển từ **`PlayerPublic.elementAffinity`** (TD-02 §4.2, NW-R-025 — gửi một lần qua State, không qua `S2C_SkillCast`; VFX-Q-5 đã chốt). Chưa nhận State của người đó thì tạm dùng `VPAL_NONE`.
- <a id="vfx-r-047"></a>**VFX-R-047** Bảng màu đổi qua **uniform / gradient texture** của vật liệu hạt; không tạo material mới mỗi lần thi triển, không biến thể shader theo màu (tránh biên dịch shader giữa trận). Biến thể shader chỉ theo preset (CL-R-063), biên dịch sẵn ở `MapLoading`.

### 6.4. Lớp phủ (`vfxOverlayId`)
GD-08 TF-R-071 nêu ba loại lớp phủ (vệt sáng, nổ, nhân bản); toàn bộ ID dưới đây đã điền vào `ModifierDef.vfxOverlayId` (GD-08 §7.3). Biến thức chỉ đổi số liệu mà mẫu đã đọc (§6.2) thì **không** cần lớp phủ.

| `vfxOverlayId` | Loại (GD-08) | Biến thức GD-08 §7.3 | Hiệu ứng | Gắn vào pha |
|----------------|---------------|----------------------|----------|-------------|
| `VFXO_MULTI` (GD-08) | Nhân bản | `MOD_SPLIT`, `MOD_GREAT_SPLIT` (dùng chung, GD-08 §7.3) | Vệt tách luồng ở miệng phóng, các luồng phụ nhỏ hơn 15% | `CAST`, `RELEASE` |
| `VFXO_TRAIL` | Vệt sáng | `MOD_PIERCE` | Vệt sáng dài hơn + tia lóe khi xuyên qua mục tiêu | `RELEASE`, `IMPACT` |
| `VFXO_BURST` | Nổ | `MOD_BURST` | Vòng nổ bán kính `shape.explodeRadius` theo motif nguyên tố | `IMPACT` |
| `VFXO_ECHO` | Nhân bản | `MOD_ECHO` | Dư ảnh lặp lại lượt trúng sau `delaySec` của `APPEND_HIT_ECHO` | `IMPACT` (lần 2) |
| `VFXO_EMPOWER` | Vệt sáng | `MOD_EMPOWER` | Lõi sáng hơn, tụ lực có vành sáng | `CAST`, `RELEASE` |
| `VFXO_SHOCKWAVE` | Nổ | `MOD_CONCUSS` | Sóng chấn mặt đất mảnh khi trúng | `IMPACT` |
| `VFXO_WARD` | — | `MOD_WARD` | Vỏ khiên mỏng quanh người thi triển (`APPEND_SELF_SHIELD`) | `CAST` → hết khiên |
| `VFXO_STEADFAST` | — | `MOD_STEADFAST` | Viền giáp `HEAVY` khi niệm (dùng chung `VFX_CB_ARMOR_HEAVY`) | `CAST` |
| `VFXO_TWIN` | Vệt sáng | `MOD_TWIN_ELEMENT` (hiếm) | Vệt phụ màu **hệ thứ hai** (hệ khắc chế, GD-06 SF-R-061) xoắn quanh lõi | `RELEASE`, `IMPACT` |
| — (không lớp phủ) | | `MOD_WIDEN`, `MOD_REACH`, `MOD_SWIFT`, `MOD_SURGE`, `MOD_LINGER`, `MOD_THRIFT`, `MOD_QUICKCAST`, `MOD_VOID_COST` | Mẫu tự co giãn theo tham số / không có thay đổi hình ảnh | |

- <a id="vfx-r-050"></a>**VFX-R-050** Lớp phủ áp **theo đúng thứ tự `priority`** của biến thức (TF-R-030 bước 4, gồm đột biến); trùng ID (vd. `MOD_SPLIT` + đột biến `MOD_GREAT_SPLIT`) chỉ áp một lần.
- <a id="vfx-r-051"></a>**VFX-R-051** Tối đa 4 lớp phủ một lần thi triển (3 ô biến thức + 1 đột biến, TF-R-011). Mọi lớp phủ **chia chung** ngân sách hạt của một kỹ năng (§9.2): mẫu ≤ 70%, tổng lớp phủ ≤ 30% (khởi điểm `vfx/params.json → skillBudgetSplit`, đề xuất — chờ TD-07 đăng ký domain `vfx`).
- <a id="vfx-r-052"></a>**VFX-R-052** Hiệu ứng Viên Mãn (`PerfectionEffect`, GD-07 §3) dùng lại cùng cơ chế: `EXTRA_PROJECTILES` → `VFXO_MULTI` theo `count`; `EXTRA_HIT` → `VFXO_ECHO`; `ON_EXPIRE_EXPLODE` → `VFXO_BURST`; `RADIUS_ADD` / `DURATION_ADD` → mẫu tự co giãn; `SHIELD_PCT_ADD` → `VFX_AEGIS`. Không cần ID mới.

### 6.5. Thuật toán chọn VFX (client, tất định)
- <a id="vfx-r-055"></a>**VFX-R-055** `resolveSkillVfx(skillRef, skillDef, caster) → { templateId, paletteId, overlayIds[], flavor }`:
  1. **Ghi đè dữ liệu:** nếu `SkillDef` có trường `vfx` (GD-07 §3: `vfx?: { templateId?: string; overlayIds?: string[] }`) → dùng giá trị đó.
  2. **Kỹ năng tự chế** (`skillRef` = `CSK_*`): `templateId = CoreDef[coreId].vfxTemplateId`; `overlayIds` = `vfxOverlayId` của `modifierIds` + `mutationModifierId` sắp theo `priority`. Client cần `coreId`, `elementId`, `modifierIds`, `mutationModifierId` của `CraftedTechnique` → lấy từ `GET /v1/crafted-techniques/{id}` (GD-08 §9.1, TF-R-072 — trả các trường này) và cache (bất biến, TF-R-004; VFX-Q-6 đã chốt: REST + cache).
  3. **Còn lại** (kỹ năng tác giả viết, đánh thường / đánh mạnh `ComboStep`, chiêu quái): suy từ `shape.type`: `PROJECTILE` → `VFX_BOLT` · `CONE` → `VFX_SWEEP` · `GROUND_CIRCLE` có `heal` + `ALLIES` → `VFX_BLOOM`, còn lại → `VFX_SMITE` · `DASH` → `VFX_DASH` · `SELF_CIRCLE` → `VFX_NOVA` · `SUMMON` → `VFX_FAMILIAR` · `SELF` → `VFX_AEGIS` · `LINE` → `VFX_THRUST`. Lớp phủ suy từ `PerfectionEffect` đang hiệu lực (VFX-R-052) và `shape.explodeRadius > 0` → `VFXO_BURST`.
  4. `paletteId` theo VFX-R-045; `flavor` = `damageType` của lượt trúng đầu (không có thì `SPELL`).
- <a id="vfx-r-056"></a>**VFX-R-056** Kết quả phụ thuộc **chỉ** vào dữ liệu (gamedata + `CraftedTechnique` + hệ tâm pháp) → hai client thấy cùng VFX; có test bảng (VFX-AC-005, 006).

### 6.6. Chiêu đặc trưng boss
- <a id="vfx-r-060"></a>**VFX-R-060** Boss (`MON_B_*`, GD-12 §7.1) được phép **mẫu riêng** `VFX_BOSS_{TÊN}_{CHIÊU}` (vd. Hồ Hỏa Bạo của Hồ Yêu Mẫu — GD-12 §7 ví dụ boss) qua trường ghi đè `SkillDef.vfx` (VFX-R-055 bước 1; GD-12 EN-R-095), trong ngân sách boss (§9.2, TD-11 PERF-R-033). Vẫn bắt buộc telegraph chuẩn §5 — VFX riêng **không** thay telegraph.
- <a id="vfx-r-061"></a>**VFX-R-061** Pha boss (`S2C_BossPhase`) có `VFX_CB_BOSS_PHASE` (bùng khí + đổi màu nhấn của hào quang boss, AR-01 §5.2); cửa sổ Kiệt Sức thu phục có `VFX_WLD_CAPTURE_WINDOW` (GD-25 SB-R-026).

---

## 7. Trúng, phản hồi chiến đấu, trạng thái

### 7.1. Trúng và phản hồi
| ID (đề xuất) | Khi nào | Mô tả |
|--------------|---------|-------|
| `VFX_HIT_PHYS` / `VFX_HIT_SPELL` | Lượt trúng thường | Tia lóe theo flavor + motif nguyên tố; cỡ theo `reaction` (`FLINCH` nhỏ → `KNOCKDOWN` lớn) |
| `VFX_HIT_CRIT` | Chí mạng | Như trên ×1,5 cỡ + vòng sáng vàng ngắn (số vàng to do UI, GD-06 §11) |
| `VFX_HIT_BLOCK` / `VFX_HIT_PARRY` | Đỡ / đỡ hoàn hảo (GD-06) | Tia lửa kim loại / vòng sáng trắng + rung có giới hạn (CAM-R-014) |
| `VFX_HIT_POISE_BREAK` | Vỡ thế (`POISE_BROKEN`) | Mảnh vỡ trắng quanh thân, chữ "Phá thế" do UI |
| `VFX_HIT_IMMUNE` | Mục tiêu `ST_INVULNERABLE` | Gợn sáng mờ, không tia lóe (GD-06 §13) |
| `VFX_CB_DODGE` | Né lướt có i-frame | Dư ảnh mờ 2–3 bóng theo hướng lướt |
| `VFX_CB_ARMOR_HEAVY` | Thực thể đang giáp `HEAVY` / `IMMUNE` | Viền sáng khối quanh thân (đọc được "không bị ngắt") |
| `VFX_CB_UNBLOCKABLE_FLASH` | Quái bắt đầu chiêu không thể đỡ | Lóe đỏ trên thân (EN-R-071) |
| `VFX_CB_ELITE_AURA` / `VFX_CB_BOSS_AURA` | Tinh anh / boss | Hào quang nhẹ / màu nhấn riêng (AR-01 §5.2) |
| `VFX_CB_SPAWN` / `VFX_CB_DESPAWN` | Quái xuất hiện / chết | Khói tụ / tan thành linh khí — **không** máu me (ART-R-004, WB-R-009) |
| `VFX_CB_DOWNED` | Hấp hối (`DOWNED`) | Vòng tối mờ dưới thân; đồng đội thấy rõ (GD-28 §11) |

- <a id="vfx-r-065"></a>**VFX-R-065** Hit-stop và rung màn hình thuộc hoạt ảnh / camera (CAM-R-014: biên độ ≤ 0,3m, ≤ 0,4s, nhân `screenShake`); VFX trúng chỉ phát **một** tia lóe mỗi mục tiêu mỗi tick (gộp theo `S2C_HitResults`, NW-R-090).

### 7.2. Trạng thái (`VFX_ST_*`)
- <a id="vfx-r-070"></a>**VFX-R-070** Mỗi `StatusDef` (GD-06 §5.12) có **một binding** VFX hoặc khai báo rõ `none` (chỉ biểu tượng UI). Cường độ theo số tầng (`STACK`); `onMaxStacks` đổi binding (vd. `ST_FROST` 3 tầng → `ST_FROZEN`).
- <a id="vfx-r-071"></a>**VFX-R-071** Tối đa 2 VFX trạng thái trên thân một thực thể cùng lúc (khởi điểm `vfx/params.json → status.maxBodyVfx`, đề xuất), ưu tiên `CC` > `DEBUFF` có DoT > `BUFF` > còn lại; phần dư chỉ hiện biểu tượng.

| Status (GD-06) | Binding (đề xuất) | Hình ảnh |
|----------------|-------------------|----------|
| `ST_BURN` | `VFX_ST_BURN` | Lưỡi lửa nhỏ trên thân, 1–3 cụm theo tầng (`VPAL_HOA`) |
| `ST_FROST` | `VFX_ST_FROST` | Tinh thể sáu cạnh bám chân, 1–3 lớp (`VPAL_BANG`; VFX-Q-7: nguồn Thủy dùng `VPAL_THUY`?) |
| `ST_FROZEN` | `VFX_ST_FROZEN` | Khối băng bọc thân 1,5s, vỡ khi hết |
| `ST_SHOCK` | `VFX_ST_SHOCK` | Tia gấp khúc nhảy quanh thân theo tầng (`VPAL_LOI`) |
| `ST_ARMOR_BREAK` | `VFX_ST_ARMOR_BREAK` | Mảnh kim vỡ rơi quanh vai (`VPAL_KIM`) |
| `ST_ENTANGLE` | `VFX_ST_ENTANGLE` | Dây leo trói chân (`VPAL_MOC`) |
| `ST_HEAVY` | `VFX_ST_HEAVY` | Vòng bụi đá nặng dưới chân (`VPAL_THO`) |
| `ST_WIND_CUT` | `VFX_ST_WIND_CUT` | Vệt gió cắt xoay, 1–5 vệt (`VPAL_PHONG`) |
| `ST_POISON` | `VFX_ST_POISON` | Bọt khí lục độc bốc lên (màu độc, không trùng `VPAL_MOC`: lục vàng `#A8D43A`, đề xuất) |
| `ST_CHARMED` | `VFX_ST_CHARMED` | Cánh hoa hồng phấn xoay quanh đầu + ánh mắt hồng (GD-06 SF-R-124; màu ngoài 8 hệ) |
| `ST_STUN` | `VFX_ST_STUN` | Vòng phù văn xoay trên đầu |
| `ST_SILENCE` | `VFX_ST_SILENCE` | Phù ấn niêm phong trước ngực |
| `ST_INVULNERABLE` | `VFX_ST_INVULNERABLE` | Lớp kim quang mờ toàn thân |
| `ST_BURNING_LIFE` | `VFX_ST_BURNING_LIFE` | Lửa đỏ thẫm + tro bay (thọ nguyên đang đốt) |
| `ST_CHILLED` | `VFX_ST_CHILLED` | Hơi lạnh nhẹ ở chân |
| `ST_LAST_LIGHT` | `VFX_ST_LAST_LIGHT` | Ánh vàng ấm chập chờn (hồi quang) — trang nghiêm |
| `ST_QI_SURGE` | `VFX_ST_QI_SURGE` | Hạt linh khí bay lên khi đả tọa |
| `ST_HEAVEN_PUNISHMENT` | `VFX_ST_HEAVEN_PUNISHMENT` | Dấu lôi ấn trên đầu, thấy được bởi người khác (GD-32) |
| `ST_DORMANT_GUARD` | `VFX_ST_DORMANT_GUARD` | Tĩnh tọa phát sáng (khớp clip `DORMANT_LOOP`, AR-02 §5.2) |
| `ST_DZ_UNREST` / `ST_DZ_STORM` | `VFX_ST_DZ_EMPOWER` | Khí đỏ đen quanh quái (đậm hơn ở `STORM`) |
| `ST_KILLING_INTENT` | `VFX_ST_KILLING_INTENT` | Sát khí đỏ thẫm quanh chân, đậm dần theo 10 tầng |
| `ST_ANTI_POISON` | `VFX_ST_ANTI_POISON` | Vòng lục nhạt mảnh quanh thân |
| `ST_STAUNCH` | `VFX_ST_STAUNCH` | Hạt ấm đỏ nhạt hồi máu |
| `ST_NIGHT_EMPOWER` | `VFX_ST_NIGHT_EMPOWER` | Mắt quái phát sáng (AR-01 §5.2 `DEMON`) |
| `ST_WEAKENED`, `ST_GREAT_LIMIT`, `ST_HEART_DEMON`, `ST_QI_UNSETTLED`, `ST_QI_STORM`, `ST_COLLAPSE` | `none` (biểu tượng UI) — `ST_QI_STORM` / `ST_COLLAPSE` thể hiện bằng VFX môi trường hiểm địa (§8.4) | |

---

## 8. Môi trường, thế giới, đột phá, ngự kiếm, sự kiện

### 8.1. Thời tiết và không khí (GD-21 §3.8–3.9, CL-R-062)
| `Weather` (glossary) | VFX (đề xuất) | Ghi chú |
|----------------------|---------------|---------|
| `CLEAR` / `CLOUDY` | — / bóng mây trôi trên mặt đất | Chỉ ánh sáng (TD-05) |
| `RAIN` | `VFX_ENV_RAIN` | Hạt GPU + gợn nước mặt đất; preset Thấp tắt hạt (GD-21 §3.9) |
| `STORM` | `VFX_ENV_RAIN` + `VFX_ENV_LIGHTNING` | Chớp xa theo `flashReduction` (CAM-R-060) |
| `FOG` | `VFX_ENV_FOG` | Scene fog + vệt sương thấp; **không** áp lên telegraph (VFX-R-021) |
| `SPIRIT_RAIN` (Linh vũ) | `VFX_ENV_SPIRIT_RAIN` | Hạt phát sáng riêng (CL-R-062) — tên vênh với GD-23 `LINH_VU` (§15) |
| `SNOW` / `SANDSTORM` | `VFX_ENV_SNOW` / `VFX_ENV_SANDSTORM` | Vực 2+ (sau MVP) |

Không khí theo bản đồ Vực 1 (theo điểm nhấn AR-01 §4.1 / AR-06): `VFX_AMB_BAMBOO_LEAF` (lá trúc rơi, F01), `VFX_AMB_WATER_GLINT` (ánh nước, F02), `VFX_AMB_DUST` (bụi mỏ, F03), `VFX_AMB_MIST` + `VFX_AMB_POISON_GLOW` (sương, lục độc phát sáng, F04), `VFX_AMB_BLOSSOM` (hoa đào bay, F05), `VFX_AMB_WRAITH` (oán khí lam, F06), `VFX_AMB_FIREFLY` (đêm, mọi bản đồ dã ngoại), `VFX_AMB_LANTERN` (đèn lồng đêm, HUB). Đặt bằng điểm phát trong scene (AR-06 §8) hoặc gắn prop.

- <a id="vfx-r-075"></a>**VFX-R-075** VFX môi trường dùng **bảng màu bản đồ** (AR-06 §6), không dùng `VPAL_*` nguyên tố hay màu hệ thống (ART-R-003).

### 8.2. POI và thế giới (`VFX_POI_*`, `VFX_WLD_*`)
| ID (đề xuất) | Nguồn | Mô tả |
|--------------|-------|-------|
| `VFX_POI_TELEPORT_IDLE` / `_ACTIVATE` / `_CAST` | GD-21 §3.5 | Trận văn mờ / bùng sáng lần đầu chạm / vòng sáng niệm 3s |
| `VFX_POI_LEY_LINE` | `LEY_LINE` (TD-13 §4) | Cột linh khí mảnh — có thể là điểm mốc (GD-21 §3.4) |
| `VFX_POI_INSIGHT_STONE` | `INSIGHT_STATION` (GD-08) | Đạo văn cộng hưởng quanh Ngộ Đạo Thạch |
| `VFX_POI_EXIT_OPEN` | `EXIT` (GD-28) | Cột sáng lối thoát đang mở; tắt khi đóng |
| `VFX_POI_CAVE_GATE` | `CAVE_GATE` (GD-30) | Màn sương mờ ở cửa động (Beta) |
| `VFX_WLD_SCAN_WAVE` / `VFX_WLD_SCAN_HIGHLIGHT` | GD-21 §3.7 | Sóng tròn lan ra / viền sáng vật được phát hiện 10s (lớp 5) |
| `VFX_WLD_RARE_HERB` | GD-21 OW-R-106 | Linh khí hội tụ quanh linh thảo hiếm (chỉ khi đã phát hiện, OW-R-101) |
| `VFX_WLD_LOOT_PILLAR` | GD-09 §11 | Cột sáng màu phẩm (AR-01 §4.3) + số vạch |
| `VFX_WLD_STAGE_UP` / `VFX_WLD_MASTERY_UP` / `VFX_WLD_PERFECTION` | GD-04, GD-07 §11 | Vòng sáng lên tầng / thuần thục / Viên Mãn (lớn) |
| `VFX_WLD_MEDITATE` | GD-04 §5.3 | Linh khí xoáy chậm khi đả tọa |
| `VFX_WLD_ENCOUNTER_GLOW` | GD-22 `CueKind.GLOW` | Cột sáng mờ; ID dùng trong `presentation.vfx` |
| `VFX_WLD_ENCOUNTER_TRACE` | GD-21 §3.7 | Luồng khí tức mờ chỉ hướng cơ duyên khi quét |
| `VFX_WLD_LINHCHUNG_AURA` | GD-25 SB-R-011, §11 | Hào quang + cột sáng mảnh màu huyết mạch (AR-01 §4.3) |
| `VFX_WLD_CAPTURE_WINDOW` | GD-25 SB-R-026 | Hào quang thu phục quanh boss Kiệt Sức |
| `VFX_WLD_TITLE_TACTHIEN_1..3` | GD-32 PK-R-056 | Hào quang sấm sét đậm dần: Tôn Giả → Chí Tôn → Đại Đế (`socket_head_fx`) |
| `VFX_WLD_SAFE_EDGE` | AR-01 §4.4 | Viền lục nhạt khi vào / ra vùng an toàn |
| `VFX_WLD_NOFLY_EDGE` | GD-31 §11 | Viền mây ở mép vùng `NO_FLY` khi đang bay |
| `VFX_WLD_INSIGHT_MOMENT` | GD-08 §11 | Chữ cổ bay quanh nhân vật tĩnh tọa; đột biến thêm sấm "Đốn ngộ!" |
| `VFX_WLD_ALCHEMY_RESULT` / `VFX_WLD_FORGE_RESULT` / `VFX_WLD_SPIRIT_BORN` | GD-10 §11, GD-11 §11, FG-R-033 | Đan bay ra màu theo phẩm chất / trang bị nguội dần / hư ảnh khí linh |
| `VFX_WLD_REMNANT` | GD-26 §5.15 | Vật liệu bán trong suốt lam lạnh cho tàn hồn (`SPIRIT`, AR-01 §5.2) |
| `VFX_WLD_CORPSE_CANDLE` | GD-28 §11 | Ánh nến trên bia gỗ di hài |
| `VFX_PK_HEAVEN_AVATAR` | LR-01 §8.1, GD-32 | Vật liệu "người bằng lôi quang, không mặt" cho `MON_B_HEAVEN_AVATAR` |

### 8.3. Đột phá và thiên kiếp (GD-04 §5.7, §11)
| ID (đề xuất) | Pha | Mô tả |
|--------------|-----|-------|
| `VFX_BT_CHANNEL` | `CHANNELING` 30s | Linh khí xoáy quanh nhân vật **theo màu linh căn** (`SpiritRoot.elements` → `VPAL_*`, nhiều hệ thì xoay vòng màu); viền đỏ khi trúng đòn |
| `VFX_BT_SUCCESS` | `SUCCEEDED` | Cột sáng + tên cảnh giới thư pháp (chữ do UI) |
| `VFX_BT_FAIL` | `FAILED` | Linh khí tán loạn, tắt dần — không máu me |
| `VFX_TRIB_CLOUD` | `TRIBULATION` | Mây đen tụ trên đấu trường tròn 30m |
| `VFX_TG_TRIBULATION` | Mỗi tia | Vòng sét dạng `VFX_TG_ENEMY` + motif tia gấp khúc, thời gian `telegraphSec` (0,8 / 0,7s, `breakthroughs.json`) |
| `VFX_TRIB_STRIKE` | Mỗi tia | Thiên lôi (biến thể sáng của `VPAL_LOI`); bắt buộc tuân `flashReduction` |
| `VFX_TRIB_CLEANSE` | CU-R-074 | Thiên lôi tẩy thể: điện trắng chảy dọc thân rồi tan |
| `VFX_TRIB_DISSOLVE` | Binh giải (GD-26 §5.16) | Nhục thân tan thành ánh sáng giữa lôi kiếp — trang nghiêm (WB-R-009) |

- <a id="vfx-r-080"></a>**VFX-R-080** VFX chớp mạnh (thiên lôi, nổ lớn) khi `flashReduction = true`: giảm độ sáng đỉnh ≥ 50% và **không quá 3 lần chớp / giây** (ngưỡng an toàn quang mẫn thông dụng; đã ghi vào GD-24 CAM-R-060, khóa `controls.json → accessibility.flashReduction`).

### 8.4. Hiểm địa (GD-28)
| ID (đề xuất) | Nguồn | Mô tả |
|--------------|-------|-------|
| `VFX_DZ_POISON_FOG` | `HazardKind.POISON_FOG` | Thể tích sương lục độc theo `areaIds` — ranh giới đọc được (người chơi phải biết đang đứng trong) |
| `VFX_DZ_POISON_VENT` | `TRP_POISON_VENT` | Lỗ phun độc: lóe báo 0,5s (`VFX_DZ_TRAP_ARM`) rồi phun |
| `VFX_DZ_TRAP_REVEAL` / `VFX_DZ_TRAP_ARM` | DZ-R-101 | Trận văn bẫy hiện khi quét đủ cấp / lóe 0,5s trước khi kích hoạt |
| `VFX_DZ_SOUL_ALTAR` | `KILLING_INTENT` (Trấn Hồn Đài) | Vầng sáng đài; tắt với người đã dùng |
| `VFX_DZ_PHASE_UNREST` / `_STORM` / `_COLLAPSE` | DZ-R-030 | Ám màu bầu trời + hạt linh khí bạo loạn (`STORM`) + vết nứt, bụi rơi (`COLLAPSE`) — màu HUD theo GD-28 §11 |

### 8.5. Ngự kiếm (GD-31 §11, Beta 2)
| ID (đề xuất) | `FlightState` | Mô tả |
|--------------|---------------|-------|
| `VFX_FLY_TAKEOFF` / `VFX_FLY_LAND` | `TAKING_OFF` / `LANDING` | Vòng gió tụ dưới chân (`socket_feet_fx`) |
| `VFX_FLY_TRAIL` | `FLYING` | Vệt kiếm khí sau phi kiếm; biến thể theo ngoại hình `COS_FLIGHT_*` (chỉ hình ảnh, FLY-R-005) |
| `VFX_FLY_SHADOW` | `FLYING` | Bóng tròn mặt đất **đúng vị trí logic** (bắt buộc — người chơi khác nhắm theo bóng, FLY-R-051) |
| `VFX_FLY_FALL` | `FALLING` | Tia lóe khi bị đánh rơi + vệt rơi 0,8s |

### 8.6. Sự kiện thế giới (GD-23 §7.1)
| ID (đề xuất) | Sự kiện | Mô tả | Phase |
|--------------|---------|-------|-------|
| `VFX_EVT_QI_SURGE` | `WE_V1_QI_SURGE` | Hạt linh khí lơ lửng khắp bản đồ (thưa, preset Thấp tắt) | Alpha |
| `VFX_EVT_TREASURE_PILLAR` | `WE_V1_TREASURE` | Cột sáng dị bảo thấy từ xa — **chỉ khi `ACTIVE`** (GD-23 §11) | Alpha |
| `VFX_EVT_TREASURE_HOLD` | `WE_V1_TREASURE` | Vòng tiến độ giữ dị bảo 10s quanh người giữ | Alpha |
| `VFX_EVT_ZONE_EDGE` | Mọi sự kiện có vùng | Viền vùng sự kiện trên mặt đất (GD-23 §11) — **không** dùng màu telegraph | Alpha |
| `VFX_ENV_SPIRIT_RAIN` | `WE_V1_LINH_VU` | Dùng lại VFX thời tiết | Alpha |
| `VFX_EVT_TIDE_LANTERN` | `WE_V1_BEAST_TIDE` | 3 đèn trấn yêu: sáng / chập chờn / tắt | Beta 2 |
| `VFX_EVT_INVASION_MIASMA` / `VFX_EVT_ARRAY_EYE` | `WE_V1_INVASION` | Ma khí phủ bản đồ / trận nhãn | Beta 2 |
| `VFX_BOSS_BLOOD_DRAGON_*` | `WE_V1_WORLD_BOSS` | Chiêu world boss (VFX-R-060) | Beta 2 |

---

## 9. Ngân sách theo preset (nguồn: TD-11, TD-05 §9)

### 9.1. Số đã có — chỉ trích, **không** đặt số khác
| Hạng mục | Thấp | Trung | Cao | Nguồn |
|----------|:---:|:---:|:---:|-------|
| Hạt VFX đồng thời (cảnh `SCN-TOWN-100`) | ≤ 1.500 | ≤ 4.000 | ≤ 8.000 | TD-11 §2.1 |
| Hạt của một kỹ năng | 30% × 300 = **90** | 70% × 300 = **210** | **300** | TD-11 §3 (≤ 300 ở Cao) × TD-05 §9 "Hạt VFX tối đa / hiệu ứng" (suy ra) |
| Mesh VFX một kỹ năng | ≤ 500 tam giác | ≤ 500 | ≤ 500 | TD-11 §3 |
| Atlas VFX | 1 atlas chung ≤ 1024² | | | TD-11 §3, AR-02 §6 |
| Thời gian sống | ≤ 3s | | | TD-11 §3 (xem VFX-R-086) |
| VFX người chơi khác | Chỉ hình dạng tối giản | Giảm 50% | Đầy đủ | TD-05 §9 |
| Particle thời tiết | 20% | 60% | 100% | TD-05 §9 |
| Bloom / thủy mặc | Tắt | Bloom nhẹ | Bloom + thủy mặc | TD-05 §9, ART-Q-2 |
| Draw call toàn khung / môi trường | ≤ 100 / ≤ 80 | ≤ 200 / ≤ 150 | ≤ 250 / ≤ 180 | TD-11 §2.1 |

### 9.2. Quy tắc áp ngân sách
- <a id="vfx-r-085"></a>**VFX-R-085** Ngân sách hạt "một kỹ năng" là **tổng một lần thi triển** gồm mọi luồng đạn (`shape.count` tới 7), mọi lớp phủ và mọi tick; mẫu tự chia hạt theo số luồng (vd. 7 luồng ở Cao: mỗi luồng ≤ 300 × 0,7 / 7 = 30 hạt).
- <a id="vfx-r-086"></a>**VFX-R-086** "Thời gian sống ≤ 3s" (TD-11 §3) áp cho **từng hạt** và cho hiệu ứng một lần (`CAST`, `IMPACT`, `FADE`). Hiệu ứng có thời lượng do gameplay dài hơn 3s (`persistSec` tới 6s với `MOD_LINGER`, `CORE_BLOOM` 4s, `SKL_HEALING_RAIN` 5s, triệu hồi 8s, status, hào quang) chạy ở pha `LINGER` dạng **phát lặp** với số hạt ổn định trong ngân sách — **TD-11 PERF-R-032 đã xác nhận** (VFX-Q-8 đã chốt).
- <a id="vfx-r-087"></a>**VFX-R-087** **Boss / world boss:** dùng ngân sách "một kỹ năng" cho mỗi chiêu, kiểm bằng `SCN-BOSS-20` (TD-11 PERF-R-033; VFX-Q-9 đã chốt).
- <a id="vfx-r-088"></a>**VFX-R-088** **Draw call VFX:** VFX tính trong phần draw call ngoài môi trường của TD-11 §2.1 (TD-11 PERF-R-034); ràng buộc thiết kế: mỗi mẫu dùng 1 hệ hạt + tối đa 1 mesh, chung atlas → mọi hiệu ứng cùng mẫu gộp được; telegraph 1 draw call (VFX-R-035). Trần riêng cho VFX chưa có (TD-11 PERF-Q-4). Đo bằng `SceneInstrumentation` (TD-11 §7.1).
- <a id="vfx-r-089"></a>**VFX-R-089** Hạt thời tiết / không khí **nằm trong** trần "hạt VFX đồng thời", dành tối đa 25% trần cho thời tiết + không khí (`vfx/params.json → reserve.ambientPct`; TD-11 PERF-R-035, VFX-Q-10 đã chốt).

---

## 10. Pool, LOD, giảm khi đông

### 10.1. Pool (CL-R-067, `core/pool.ts`)
- <a id="vfx-r-090"></a>**VFX-R-090** Mỗi mẫu có **pool** hệ hạt + mesh dùng lại; bảng màu / tham số gán khi lấy ra (VFX-R-047). Không tạo / hủy `ParticleSystem` trong trận.
- <a id="vfx-r-091"></a>**VFX-R-091** **Làm ấm khi `MapLoading`:** tạo sẵn pool cho (a) mẫu của kỹ năng trong loadout bản thân, (b) mẫu chiêu của quái trong `spawnAreas` của bản đồ, (c) VFX môi trường / POI của bản đồ, (d) nhóm `HIT` và `STATUS` lõi. Số tạo sẵn khởi điểm: 4 / mẫu, đạn 16 (`vfx/params.json → pool.prewarm`; TD-11 PERF-R-036).
- <a id="vfx-r-092"></a>**VFX-R-092** Pool có trần (`pool.max`, khởi điểm 32 / mẫu; TD-11 PERF-R-036); hết chỗ thì **tái dùng hiệu ứng cũ nhất có ưu tiên thấp nhất** (§10.3), không cấp phát thêm.
- <a id="vfx-r-093"></a>**VFX-R-093** Rời bản đồ: trả toàn bộ về pool rồi giải phóng pool chỉ dùng cho bản đồ cũ — số mesh / texture / hệ hạt quay về mức ban đầu ± 5% sau 10 lần chuyển (PERF-R-022, CL-R-041).

### 10.2. LOD theo khoảng cách
- <a id="vfx-r-095"></a>**VFX-R-095** Khoảng cách đo trên mặt phẳng 2D từ **tâm camera** (nhân vật mình) tới gốc hiệu ứng; dùng lại mốc LOD nhân vật PERF-R-020:

| Mức | Khoảng cách | Hiển thị |
|-----|-------------|----------|
| `NEAR` | ≤ 15m | Đầy đủ theo preset |
| `MID` | 15–30m | 50% hạt; bỏ sub-emitter, vệt mesh, ánh sáng động |
| `FAR` | > 30m | Hình dạng tối giản (lõi + motif); hiệu ứng không thù địch, không phải của tổ đội thì bỏ |

- <a id="vfx-r-096"></a>**VFX-R-096** Hiệu ứng ngoài khung nhìn (frustum + lề 4m): **dừng cập nhật hạt** nhưng giữ đồng hồ pha, để khi vào khung vẫn đúng thời điểm. Telegraph không áp LOD.

### 10.3. Giảm khi đông
- <a id="vfx-r-097"></a>**VFX-R-097** Thứ tự ưu tiên (cao → thấp), dùng khi tổng hạt sắp vượt trần preset (TD-11 §2.1) hoặc pool đầy:

| Bậc | Nội dung | Có bị cắt |
|:---:|----------|-----------|
| 0 | Telegraph, vòng chân mình, viền quét | **Không bao giờ** |
| 1 | Đạn / chiêu **thù địch** nhắm vào mình hoặc tổ đội | Không (tối thiểu hình dạng tối giản, VFX-R-026) |
| 2 | Kỹ năng + trúng + status của **bản thân** | Chỉ giảm hạt, không bỏ |
| 3 | Boss / tinh anh (hào quang, pha) | Giảm hạt |
| 4 | Tổ đội | Giảm → tối giản |
| 5 | Người chơi khác, status trên người khác | Tối giản → bỏ |
| 6 | Thời tiết, không khí, trang trí | Giảm → bỏ |

- <a id="vfx-r-098"></a>**VFX-R-098** Ngoài dòng "VFX người chơi khác" theo preset (TD-05 §9): khi số người chơi khác **đang thi triển** trong khung > `crowd.otherCastersThreshold` (khởi điểm 15, đề xuất), VFX bậc 5 chuyển sang tối giản tới khi số giảm dưới ngưỡng − 3 (trễ để không nhấp nháy).

### 10.4. Tự hạ preset
Khi client tự hạ preset (CL-R-100, PERF-R-021), các mức % §9.1 đổi **ngay cho hiệu ứng mới**; hiệu ứng đang chạy giữ tới khi kết thúc.

### 10.5. Ngẫu nhiên thẩm mỹ
- <a id="vfx-r-099"></a>**VFX-R-099** Biến thiên hạt (góc, kích thước, thời điểm) dùng PRNG thẩm mỹ của client, seed = băm(`casterId`, `serverStartMs`, `templateId`) — tái lập được cho test ảnh chụp (TD-05 §12) và không dùng `Math.random`. PRNG này **không** thuộc dịch vụ RNG của TD-01 §9 (không ảnh hưởng kết quả, không ghi `rng_audit`).

---

## 11. Đặt tên và định dạng dữ liệu

### 11.1. Quy ước ID
| Loại | Mẫu | Ví dụ | Trạng thái |
|------|-----|-------|-----------|
| Mẫu kỹ năng | `VFX_{MẪU}` | `VFX_BOLT` | GD-07 SK-R-083, GD-08 §7.1, AR-02 §3 |
| Phần / pha của mẫu | `VFX_{MẪU}_{PHẦN}` | `VFX_BOLT_CORE` (khớp ví dụ AR-02 §3) | AR-02 |
| Lớp phủ | `VFXO_{TÊN}` | `VFXO_MULTI` | AR-02 / GD-08 §7.3 |
| Bảng màu nguyên tố | `VPAL_{ELEMENT}` / `VPAL_NONE` | `VPAL_HOA` | AR-01 §4.2, GD-08 §7.2 |
| Bảng màu telegraph | `TGPAL_{DEFAULT\|DEUTERANOPIA\|PROTANOPIA\|TRITANOPIA}` | `TGPAL_DEFAULT` | GD-24 CAM-R-061 (giá trị khớp `telegraphPalette`) |
| VFX theo nhóm | `VFX_{TG\|HIT\|CB\|ST\|ENV\|AMB\|POI\|WLD\|BT\|TRIB\|FLY\|EVT\|DZ\|PK}_{TÊN}` | `VFX_ST_BURN` | Đề xuất |
| VFX boss | `VFX_BOSS_{TÊN}_{CHIÊU}` | `VFX_BOSS_FOX_MATRIARCH_FIRE_BURST` | GD-12 EN-R-095 |
| Asset | `art/vfx/{id}/…` (texture `{id}_ATLAS`, mesh GLB) | `art/vfx/VFX_BOLT/` | AR-02 §3 |

- <a id="vfx-r-100"></a>**VFX-R-100** ID chữ hoa + gạch dưới, ≤ 48 ký tự, **không đổi** sau khi được tham chiếu trong gamedata (AST-R-010, DT-R-002) — vì `CoreDef`, `ElementDef`, `ModifierDef`, `EncounterDef.presentation.vfx` trỏ tới.

### 11.2. Thư mục gamedata (domain `vfx`, `visibility: client` — TD-07 DT-R-101; có trong `bundle.server` hay không: DT-Q-5)
| File | Kiểu | Nội dung |
|------|------|----------|
| `shared/gamedata/vfx/templates.json` | `VfxTemplateDef[]` | 9 mẫu kỹ năng + mẫu boss |
| `shared/gamedata/vfx/palettes.json` | `VfxPaletteDef[]` | 9 bảng màu nguyên tố |
| `shared/gamedata/vfx/telegraph_palettes.json` | `TelegraphPaletteDef[]` | 4 bảng telegraph |
| `shared/gamedata/vfx/overlays.json` | `VfxOverlayDef[]` | Lớp phủ |
| `shared/gamedata/vfx/effects.json` | `VfxEffectDef[]` | VFX không thuộc kỹ năng (§7–§8) |
| `shared/gamedata/vfx/bindings.json` | `VfxBindings` | `statusId → vfxId \| "none"`, trúng theo `reaction` |
| `shared/gamedata/vfx/params.json` | `VfxParams` | Pool, LOD, giảm khi đông, chia ngân sách |

DT-R-010 chưa có domain `vfx` → phải thêm vào TD-07 (DT-R-010, bảng đăng ký §9.1) trước khi tạo file (§15).

### 11.3. Kiểu dữ liệu (đề xuất)
```ts
// shared/gamedata-types/vfx.ts  (đề xuất — chờ TD-07)
type VfxCategory = 'TELEGRAPH' | 'CAST' | 'PROJECTILE' | 'HIT' | 'STATUS'
                 | 'ENVIRONMENT' | 'WORLD_UI' | 'BREAKTHROUGH' | 'FLIGHT' | 'EVENT';
type VfxPhase = 'CAST' | 'RELEASE' | 'IMPACT' | 'LINGER' | 'FADE';
type VfxMotif = 'BLADE_SHARD' | 'LEAF_VINE' | 'WATER_RIPPLE' | 'FLAME_TONGUE' | 'ROCK_DUST'
              | 'ZIGZAG_BOLT' | 'HEX_CRYSTAL' | 'WIND_RIBBON' | 'PLAIN_QI';
type VfxFlavor = 'PHYSICAL' | 'SPELL';

interface VfxPaletteDef {
  id: string;                              // VPAL_HOA
  element: Element | 'NONE';
  primary: string; core: string; edge: string;   // '#RRGGBB'
  motif: VfxMotif;
}
interface TelegraphPaletteDef {
  id: string;                              // TGPAL_DEFAULT
  enemyFill: string; enemyRim: string; unblockableRim: string;
  allyFill: string; allyRim: string;
}
interface VfxTemplateDef {
  id: string;                              // VFX_BOLT
  shapeTypes: Shape['type'][];             // GD-07 §3
  parts: { id: string; phase: VfxPhase; asset: string; particleShare: number }[];  // Σ particleShare = 1
  readsParams: string[];                   // đường dẫn SkillDef, vd. "shape.radius" (⊂ PatchPath ∪ trường Shape)
  flavors: VfxFlavor[];
  budget: { meshTris: number };            // ≤ 500 (TD-11 §3)
}
interface VfxOverlayDef {
  id: string;                              // VFXO_MULTI
  phases: VfxPhase[];
  particleShare: number;                   // phần của 30% dành cho lớp phủ
  asset: string;
}
interface VfxEffectDef {
  id: string;                              // VFX_ST_BURN, VFX_WLD_LOOT_PILLAR…
  category: VfxCategory;
  asset: string;
  loop: boolean;                           // true → pha LINGER (VFX-R-086)
  attach: 'GROUND' | 'BODY' | 'SOCKET_HEAD' | 'SOCKET_CHEST' | 'SOCKET_FEET' | 'WEAPON' | 'WORLD';
  maxParticlesHigh: number;                // ở preset Cao; preset khác nhân theo TD-05 §9
  priorityTier: 0 | 1 | 2 | 3 | 4 | 5 | 6; // §10.3
  hostileRim?: boolean;
  respectsFlashReduction?: boolean;        // VFX-R-080
  phase?: 'ALPHA' | 'BETA' | 'LAUNCH';     // TD-07 DT-R-058
}
interface VfxBindings {
  status: Record<string, string>;          // statusId → VfxEffectDef.id | "none"
  hitByReaction: Record<HitReaction, string>;
}
interface VfxParams {
  skillBudgetSplit: { template: number; overlays: number };   // 0,7 / 0,3
  lod: { nearM: number; midM: number; midParticleMult: number };
  pool: { prewarm: number; prewarmProjectile: number; max: number };
  crowd: { otherCastersThreshold: number; hysteresis: number };
  status: { maxBodyVfx: number };
  telegraph: { maxActive: number; rimWidthM: number; finishFlashSec: number };
  reserve: { ambientPct: number };
  fadeSecMax: number;
}
```

```jsonc
// shared/gamedata/vfx/templates.json (trích, đề xuất)
[
  { "id": "VFX_BOLT", "shapeTypes": ["PROJECTILE"], "flavors": ["PHYSICAL", "SPELL"],
    "parts": [ { "id": "VFX_BOLT_CAST",  "phase": "CAST",    "asset": "art/vfx/VFX_BOLT/cast.json",  "particleShare": 0.15 },
               { "id": "VFX_BOLT_CORE",  "phase": "RELEASE", "asset": "art/vfx/VFX_BOLT/core.json",  "particleShare": 0.35 },
               { "id": "VFX_BOLT_TRAIL", "phase": "RELEASE", "asset": "art/vfx/VFX_BOLT/trail.json", "particleShare": 0.25 },
               { "id": "VFX_BOLT_HIT",   "phase": "IMPACT",  "asset": "art/vfx/VFX_BOLT/hit.json",   "particleShare": 0.25 } ],
    "readsParams": ["shape.speed", "shape.range", "shape.radius", "shape.pierce", "shape.count",
                    "shape.spreadDeg", "shape.explodeRadius", "windupSec"],
    "budget": { "meshTris": 300 } }
]
// shared/gamedata/vfx/palettes.json (trích)
[ { "id": "VPAL_HOA", "element": "HOA", "primary": "#E8602A", "core": "#FFD65C", "edge": "#7E2610", "motif": "FLAME_TONGUE" } ]
// shared/gamedata/vfx/telegraph_palettes.json (khởi điểm — nguồn số GD-24 CAM-R-061)
[ { "id": "TGPAL_DEFAULT",      "enemyFill": "#FF4A1C", "enemyRim": "#FFE2B8", "unblockableRim": "#8C0A0A", "allyFill": "#CFE6FF", "allyRim": "#FFFFFF" },
  { "id": "TGPAL_DEUTERANOPIA", "enemyFill": "#FF9F1C", "enemyRim": "#FFF1C2", "unblockableRim": "#5A2A00", "allyFill": "#3D7EFF", "allyRim": "#DDE8FF" },
  { "id": "TGPAL_PROTANOPIA",   "enemyFill": "#FFB000", "enemyRim": "#FFF4CC", "unblockableRim": "#4A3300", "allyFill": "#3D7EFF", "allyRim": "#DDE8FF" },
  { "id": "TGPAL_TRITANOPIA",   "enemyFill": "#E8175D", "enemyRim": "#FFD1E0", "unblockableRim": "#5C0022", "allyFill": "#2EC4B6", "allyRim": "#D6FFFA" } ]
// shared/gamedata/vfx/params.json (khởi điểm)
{ "skillBudgetSplit": { "template": 0.7, "overlays": 0.3 },
  "lod": { "nearM": 15, "midM": 30, "midParticleMult": 0.5 },
  "pool": { "prewarm": 4, "prewarmProjectile": 16, "max": 32 },
  "crowd": { "otherCastersThreshold": 15, "hysteresis": 3 },
  "status": { "maxBodyVfx": 2 },
  "telegraph": { "maxActive": 64, "rimWidthM": 0.08, "finishFlashSec": 0.1 },
  "reserve": { "ambientPct": 0.25 },
  "fadeSecMax": 0.5 }
```
Ngân sách hạt của hiệu ứng **không** ghi lại số TD-11 trong `params.json` (tránh hai nguồn số): client đọc trần từ `world/client.json → vfxBudget` (TD-05 §13, TD-11 §3.2; VFX-Q-11 đã chốt).

### 11.4. Validator (thêm vào `pnpm gamedata:check` và `pnpm art:check`, không tạo lệnh mới)
- <a id="vfx-r-105"></a>**VFX-R-105** Dữ liệu: mọi `CoreDef.vfxTemplateId` ∈ `templates.json`; mọi `ElementDef.paletteId` ∈ `palettes.json`; mọi `ModifierDef.vfxOverlayId` ∈ `overlays.json`; mọi `EncounterDef.presentation.vfx` ∈ `effects.json`; mọi `StatusDef.id` có trong `bindings.status`; mỗi `Element` có đúng một `VPAL_*`, có `motif`; `Σ particleShare = 1`; `readsParams` chỉ chứa trường tồn tại trong `Shape` / `SkillDef`; `shapeTypes` của 9 mẫu phủ đủ union `Shape` (GD-07 §3).
- <a id="vfx-r-106"></a>**VFX-R-106** Asset: mesh VFX ≤ 500 tam giác, atlas ≤ 1024² lũy thừa 2, KTX2 (AST-R-040), có hồ sơ nguồn gốc (AST-R-005); mô phỏng mỗi mẫu ở giá trị trần (`count` 7, `radius` 8, 4 lớp phủ) không vượt 300 hạt (Cao).

---

## 12. Danh sách VFX MVP (ưu tiên, phase)

Ưu tiên: **P0** chặn mốc phase · **P1** cần trong phase · **P2** có thể dời. Phase theo PLAN §7.

| # | ID | Nhóm | Ưu tiên | Phase | Ghi chú |
|:-:|----|------|:---:|:---:|---------|
| 1 | `VFX_TG_ENEMY`, `VFX_TG_UNBLOCKABLE`, `TGPAL_*` (4) | TELEGRAPH | P0 | 0 | Mẫu Phase 0 #5 (AR-01 §10) |
| 2 | `VFX_BOLT` + `VPAL_HOA` | PROJECTILE | P0 | 0 | "1 VFX kỹ năng (Hỏa)" — chốt độ sáng, bloom |
| 3 | `VFX_TG_ALLY`, `VFX_TG_AIM` | TELEGRAPH | P0 | 1 | |
| 4 | `VFX_BOLT`, `VFX_SWEEP`, `VFX_SMITE`, `VFX_DASH` | Mẫu | P0 | 1 | 4 kỹ năng kiếm Phase 1 (phóng, quạt, vùng, lướt): `SKL_SWORD_QI_SLASH`, `SKL_SWEEPING_BLADE`, `SKL_SWORD_RAIN`, `SKL_FLASH_STEP` |
| 5 | `VFX_THRUST` | Mẫu | P0 | 1 | Đánh mạnh kiếm mức 1 (`LINE` 4m × 1m, GD-07 §5.4) |
| 6 | `VPAL_NONE` + 8 `VPAL_*` | Palette | P0 | 1 | Kiếm `INHERIT` cần mọi hệ tâm pháp MVP (GD-07 §7.1) |
| 7 | `VFX_HIT_PHYS`, `_SPELL`, `_CRIT`, `_BLOCK`, `_PARRY`, `_POISE_BREAK` | HIT | P0 | 1 | "Game feel" Phase 1 |
| 8 | `VFX_CB_DODGE`, `_ARMOR_HEAVY`, `_UNBLOCKABLE_FLASH`, `_ELITE_AURA`, `_BOSS_AURA`, `_BOSS_PHASE`, `_SPAWN`, `_DESPAWN` | HIT | P0 | 1 | 1 tinh anh + 1 boss 3–4 chiêu |
| 9 | `VFX_CB_DOWNED`, `VFX_DZ_TRAP_ARM`, `VFX_POI_EXIT_OPEN`, `VFX_DZ_PHASE_*` | DZ | P1 | 1 | Hiểm địa thử nghiệm Phase 1 |
| 10 | `VFX_AMB_BAMBOO_LEAF`, `VFX_AMB_FIREFLY` | ENV | P1 | 1 | `V1_F01` + ngày / đêm cơ bản |
| 11 | `VFX_NOVA`, `VFX_FAMILIAR`, `VFX_BLOOM`, `VFX_AEGIS` | Mẫu | P0 | 2 | Vũ khí thứ 2 (quạt) + đủ 14 kỹ năng MVP (GD-07 §7.2) |
| 12 | `VFX_ST_*` cho 8 status hệ + `ST_POISON`, `ST_STUN`, `ST_FROZEN`, `ST_INVULNERABLE`, `ST_BURNING_LIFE` | STATUS | P0 | 2 | |
| 13 | `VFX_POI_TELEPORT_*`, `VFX_WLD_SCAN_WAVE`, `_SCAN_HIGHLIGHT`, `_LOOT_PILLAR`, `_STAGE_UP`, `_MASTERY_UP`, `_MEDITATE`, `_SAFE_EDGE`, `_ENCOUNTER_GLOW`, `_ENCOUNTER_TRACE` | WORLD_UI | P0 | 2 | Truyền tống, fog, thần thức, cơ duyên |
| 14 | `VFX_ENV_RAIN`, `VFX_ENV_FOG`, `VFX_AMB_*` của HUB + 3 dã ngoại Phase 2 | ENV | P1 | 2 | |
| 15 | `VFX_WLD_REMNANT`, `VFX_POI_INSIGHT_STONE`, `VFX_WLD_ALCHEMY_RESULT` | WORLD_UI | P1 | 2–3 | Tàn hồn, luyện đan |
| 16 | `VFX_ENV_LIGHTNING`, `VFX_ENV_SPIRIT_RAIN`, `VFX_AMB_*` còn lại của Vực 1 | ENV | P1 | 3 | Thời tiết đầy đủ |
| 17 | `VFX_DZ_POISON_FOG`, `_POISON_VENT`, `_SOUL_ALTAR`, `_TRAP_REVEAL`, `VFX_WLD_CORPSE_CANDLE`, `VFX_ST_KILLING_INTENT`, `VFX_ST_DZ_EMPOWER`, `VFX_ST_ANTI_POISON` | DZ | P0 | 3 | HD01, HD02 chết thật |
| 18 | `VFX_BT_CHANNEL`, `_SUCCESS`, `_FAIL`, `VFX_TRIB_CLOUD`, `VFX_TG_TRIBULATION`, `VFX_TRIB_STRIKE`, `_CLEANSE` | BREAKTHROUGH | P0 | 3 | Đột phá Trúc Cơ có `TRIB_TC` (GD-04 §7) — xem vênh PLAN ở §15 |
| 19 | `VFX_ST_LAST_LIGHT`, `VFX_ST_STAUNCH`, `VFX_ST_CHILLED`, `VFX_ST_SILENCE`, `VFX_ST_DORMANT_GUARD` | STATUS | P1 | 3 | |
| 20 | 9 lớp phủ `VFXO_*` | Overlay | P0 | 4 | Chế công pháp Alpha (GD-08 §16) |
| 21 | `VFX_WLD_INSIGHT_MOMENT`, `VFX_WLD_FORGE_RESULT`, `_SPIRIT_BORN` | WORLD_UI | P1 | 4 | |
| 22 | `VFX_EVT_QI_SURGE`, `_TREASURE_PILLAR`, `_TREASURE_HOLD`, `_ZONE_EDGE`, `VFX_ST_QI_SURGE` | EVENT | P0 | 4 | Sự kiện thế giới Alpha (GD-23 §16) |
| 23 | `VFX_TRIB_DISSOLVE`, `VFX_PK_HEAVEN_AVATAR`, `VFX_ST_HEAVEN_PUNISHMENT`, `VFX_WLD_TITLE_TACTHIEN_1..3`, `VFX_POI_CAVE_GATE` | Khác | P1 | 5a | Thiên kiếp KĐ + binh giải, PvP, động phủ |
| 24 | `VFX_FLY_*`, `VFX_WLD_NOFLY_EDGE` | FLIGHT | P0 | 5b | Ngự kiếm Beta 2 |
| 25 | `VFX_WLD_LINHCHUNG_AURA`, `VFX_WLD_CAPTURE_WINDOW` | WORLD_UI | P0 | 5b | Linh thú Beta 2 |
| 26 | `VFX_EVT_TIDE_LANTERN`, `_INVASION_MIASMA`, `_ARRAY_EYE`, `VFX_BOSS_BLOOD_DRAGON_*` | EVENT | P1 | 5b | |
| 27 | `VFX_ENV_SNOW`, `VFX_ENV_SANDSTORM`, VFX theo bảng màu Vực 2 | ENV | P2 | 5b | Vực 2 |

Ước lượng: Phase 0–1 ≈ 35 hiệu ứng; tới Alpha (Phase 4) ≈ 110; tới Beta 2 ≈ 150 (chưa gồm biến thể ngoại hình `COS_*`).

---

## 13. Quy trình làm VFX bằng AI / công cụ

VFX chủ yếu là **hệ hạt + shader có tham số** (PLAN §6.7 mục 6), không phải model 3D — TRELLIS.2 chỉ dùng cho mesh VFX hiếm khi cần (vd. phi kiếm ngoại hình). Chỉ dùng công cụ đã đăng ký TD-15; công cụ VFX mới (phần mềm mô phỏng hạt, mô hình AI khác) phải đăng ký TD-15 trước (AGENTS §4 luật 9).

```
1. Brief (người)        : chọn ID ở §12, mẫu / nhóm, bảng màu, motif, pha + thời gian từ SkillDef mẫu
2. Texture / flipbook   : mô hình vẽ ảnh đã duyệt license (TD-15 §4.5: SDXL / FLUX.1 schnell qua ComfyUI)
                          prompt theo motif (§3.1), nền trong suốt, đen-trắng (màu gán bằng palette ở shader)
                          → chỉnh tay → đóng vào atlas VFX chung ≤ 1024² (KTX2)
3. Hệ hạt / shader      : AI viết code ParticleSystem / NodeParticle + Node Material (TD-05 CL-R-067) trong
                          client/src/render/vfx/templates/{id}.ts, đọc tham số qua readsParams; không hằng số gameplay
4. Xem trước            : cảnh chuẩn AR-01 §9 (Thanh Trúc Lâm, giờ Ngọ + giờ Tý), đúng camera game,
                          chạy ở 3 preset + 3 bảng mù màu; ca duyệt bắt buộc: telegraph quái trong vùng lửa đồng đội
5. Đo                   : SceneInstrumentation — số hạt / draw call ở giá trị trần (VFX-R-106)
6. Hồ sơ nguồn gốc      : art/provenance/{id}.json (công cụ, mô hình + license, prompt, seed, người duyệt)
7. CI                   : pnpm art:check + pnpm gamedata:check (VFX-R-105, VFX-R-106) → PR có ảnh chụp
```

- <a id="vfx-r-110"></a>**VFX-R-110** Texture VFX vẽ **đơn sắc** (độ sáng + alpha); màu chỉ đến từ palette → một texture dùng cho cả 9 bảng màu (tiết kiệm atlas, đúng ART-R-040).
- <a id="vfx-r-111"></a>**VFX-R-111** Không prompt theo tên họa sĩ còn sống / tên game khác, không dùng ảnh bên thứ ba không có quyền (ART-R-050).

---

## 14. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="vfx-ac-001"></a>VFX-AC-001 | Quái thi triển `GROUND_CIRCLE` trong vùng `VFX_SMITE` Hỏa đang `LINGER` của đồng đội | Render | Telegraph vẽ ở lớp 5, không kiểm độ sâu; ảnh chụp: pixel viền telegraph không bị VFX phủ |
| <a id="vfx-ac-002"></a>VFX-AC-002 | `telegraphOpacity = 0,4` trong cài đặt | Nạp cài đặt | Bị kẹp về 0,6 (CAM-R-006) |
| <a id="vfx-ac-003"></a>VFX-AC-003 | `telegraphPalette = TRITANOPIA`, `telegraphPattern = true` | Telegraph kẻ địch | Dùng màu `TGPAL_TRITANOPIA` + sọc chéo |
| <a id="vfx-ac-004"></a>VFX-AC-004 | Chiêu quái `unblockable = true`, `telegraphPattern = false` | Bắt đầu windup | `VFX_TG_UNBLOCKABLE` có sọc + `VFX_CB_UNBLOCKABLE_FLASH` trên thân quái |
| <a id="vfx-ac-005"></a>VFX-AC-005 | `CraftedTechnique` `CORE_BOLT` + `ELEM_HOA` + `MOD_BURST` + `MOD_SPLIT` | `resolveSkillVfx` | `{ VFX_BOLT, VPAL_HOA, [lớp phủ theo priority của MOD_SPLIT / MOD_BURST], flavor theo damageType }` |
| <a id="vfx-ac-006"></a>VFX-AC-006 | `SKL_SWORD_QI_SLASH` (`INHERIT`), tâm pháp `TEC_LIET_HOA_KIEM` / `TEC_DAN_KHI` | `resolveSkillVfx` | `VPAL_HOA` / `VPAL_NONE` |
| <a id="vfx-ac-007"></a>VFX-AC-007 | Mẫu `VFX_BOLT`, `shape.count = 7`, 4 lớp phủ, preset Cao | Mô phỏng một lần thi triển | Tổng hạt ≤ 300; mesh ≤ 500 tam giác |
| <a id="vfx-ac-008"></a>VFX-AC-008 | Preset Thấp / Trung | Thi triển `VFX_SWEEP` | Hạt ≤ 90 / ≤ 210 (30% / 70% của 300) |
| <a id="vfx-ac-009"></a>VFX-AC-009 | `SCN-TOWN-100`, preset Trung | 3 phút | Hạt VFX đồng thời ≤ 4.000 (TD-11 §2.1) |
| <a id="vfx-ac-010"></a>VFX-AC-010 | Preset Trung, người chơi khác (không thù địch) thi triển | Render | Hạt = 50% của bản thân cùng kỹ năng; ở preset Thấp chỉ hình dạng tối giản; VFX của bản thân và telegraph không đổi |
| <a id="vfx-ac-011"></a>VFX-AC-011 | Quái bắn đạn vào mình khi tổng hạt đã chạm trần | Render | Đạn vẫn hiện tối thiểu lõi + viền thù địch (VFX-R-026) |
| <a id="vfx-ac-012"></a>VFX-AC-012 | Đã làm ấm pool, 200 lần thi triển liên tiếp | Đo | Không tạo `ParticleSystem` / material mới sau lần làm ấm |
| <a id="vfx-ac-013"></a>VFX-AC-013 | 10 lần chuyển bản đồ qua lại | Đo | Số hệ hạt / mesh / texture VFX về mức ban đầu ± 5% (PERF-R-022) |
| <a id="vfx-ac-014"></a>VFX-AC-014 | `ModifierDef.vfxOverlayId = "VFXO_XYZ"` không có trong `overlays.json` | `gamedata:check` | Fail (VFX-R-105) |
| <a id="vfx-ac-015"></a>VFX-AC-015 | `statuses.json` có status mới không có trong `bindings.status` | `gamedata:check` | Fail (VFX-R-070) |
| <a id="vfx-ac-016"></a>VFX-AC-016 | Mesh VFX 620 tam giác / atlas VFX 2048² | `art:check` | Fail (TD-11 §3) |
| <a id="vfx-ac-017"></a>VFX-AC-017 | `flashReduction = true`, thiên kiếp 3 tia trong 1s | Render `VFX_TRIB_STRIKE` | Độ sáng đỉnh giảm ≥ 50%, ≤ 3 lần chớp / giây |
| <a id="vfx-ac-018"></a>VFX-AC-018 | Kỹ năng tự chế có `MOD_BURST` (`shape.explodeRadius = 2`) | Trúng | Vòng nổ VFX bán kính 2m ± 5% (khớp logic, VFX-R-040) |
| <a id="vfx-ac-019"></a>VFX-AC-019 | Cùng `casterId`, `serverStartMs`, kỹ năng | Phát lại 2 lần trong test ảnh | Hai ảnh chụp giống hệt (PRNG seed, VFX-R-099) |
| <a id="vfx-ac-020"></a>VFX-AC-020 | Canh giờ Tý (đêm), sương mù | Telegraph kẻ địch | Màu / độ đậm telegraph giống giờ Ngọ (unlit, không fog — VFX-R-021) |
| <a id="vfx-ac-021"></a>VFX-AC-021 | Hiệu ứng `MID` (22m) / `FAR` (35m) không thù địch | Render | 50% hạt, không sub-emitter / tối giản hoặc bỏ |
| <a id="vfx-ac-022"></a>VFX-AC-022 | Hiệu ứng ra ngoài khung nhìn 2s rồi quay lại | Render | Pha đúng thời điểm thật (không phát lại từ đầu) |
| <a id="vfx-ac-023"></a>VFX-AC-023 | `EncounterDef.presentation.vfx = "VFX_WLD_ENCOUNTER_GLOW"` | `gamedata:check` | Pass; ID không tồn tại → Fail |
| <a id="vfx-ac-024"></a>VFX-AC-024 | Nhân vật đang `FLYING` | Người chơi khác nhìn | `VFX_FLY_SHADOW` ở đúng `(x, z)` logic (sai lệch ≤ 0,05m sau nội suy) |
| <a id="vfx-ac-025"></a>VFX-AC-025 | 4 status trên một quái (`ST_FROZEN`, `ST_BURN`, `ST_SHOCK`, `ST_HEAVY`) | Render | Chỉ 2 VFX thân: `ST_FROZEN` (CC) + một DoT; phần còn lại chỉ biểu tượng |

---

## 15. Danh sách đồng bộ (cần sửa ở tài liệu khác)

| Tài liệu | Việc cần đồng bộ | Tình trạng (đợt rà 2026-10-25) |
|----------|------------------|------------------|
| `docs/README.md` §1 | Thêm dòng AR-04 VFX Guidelines, prefix `VFX`, 🟡 v0.1 | ⏳ ngoài phạm vi đợt rà (README đã có dòng AR-04 🟡 v0.1; cần cập nhật phiên bản) |
| PLAN §8 | Đánh dấu AR-04 đã có bản nháp | ⏳ ngoài phạm vi đợt rà |
| **TD-07** | Domain mới `vfx` (DT-R-010) + 7 file §11.2 vào `registry.json` (DT-R-100), chỉ bundle client; kiểu §11.3; validator VFX-R-105; mẫu ID `VFX_*`, `VFXO_*`, `VPAL_*`, `TGPAL_*` vào bảng ID §4.3 | ⏳ chưa làm — chặn việc tạo file `vfx/*.json` |
| **GD-08** | Điền `CoreDef.vfxTemplateId` (8 mẫu §6.2), `ElementDef.paletteId` (`VPAL_*`), `ModifierDef.vfxOverlayId` (§6.4); `GET /v1/crafted-techniques/{id}` trả `coreId`, `elementId`, `modifierIds`, `mutationModifierId` (VFX-Q-6) | ✅ GD-08 v0.4 (§7.1–§7.3, TF-R-071, TF-R-072) |
| **GD-07** | Trường tùy chọn `SkillDef.vfx` (VFX-R-055); quy tắc suy mẫu từ `shape.type`; mẫu `VFX_THRUST` cho `LINE`; màu vùng cảnh báo quái ghi "đỏ" ở §11 → thống nhất "đỏ cam" (AR-01, CAM-R-061) | ✅ GD-07 v0.6 (§3 `SkillDef.vfx`, SK-R-083, §11) |
| GD-12 | Chiêu boss dùng `SkillDef.vfx` = `VFX_BOSS_*` (VFX-R-060) | ✅ GD-12 v0.4 (EN-R-095) |
| **TD-02** | Hệ tâm pháp của người thi triển (cho `INHERIT`) trong `S2C_SkillCast` hoặc `PlayerPublic` (VFX-R-046) | ✅ TD-02 v0.7 (`PlayerPublic.elementAffinity`, NW-R-025) |
| **TD-11** | Xác nhận VFX-R-086 (vòng lặp > 3s), ngân sách boss (VFX-R-087), phần draw call VFX (VFX-R-088), hạt thời tiết trong trần (VFX-R-089), số hiệu ứng / pool (VFX-R-091..092) | ✅ TD-11 v0.4 (§3.2 PERF-R-032..036; trần draw call riêng VFX còn PERF-Q-4) |
| TD-05 | `render/vfx` theo §6.5, §10; thứ tự lớp §4.2; tham chiếu `vfx/params.json` | ✅ TD-05 v0.4 (§2.1, CL-R-067..069, §13 `vfxBudget`) |
| GD-24 | Giá trị 4 bảng telegraph (VFX-R-014); ngưỡng `flashReduction` (VFX-R-080); cân nhắc tùy chọn riêng "độ đậm VFX người khác" (VFX-Q-4) | ✅ GD-24 v0.3 (CAM-R-060, CAM-R-061, CAM-R-064) |
| AR-01 | Hex + motif 5 hệ còn lại (§3.1); `VPAL_NONE` (VFX-Q-1) | ✅ AR-01 v0.2 (§4.2, ART-R-020, ART-R-022, ART-R-041) |
| AR-02 | Bổ sung mẫu ID §11.1 vào bảng §3 | ⏳ ngoài phạm vi đợt rà |
| GD-22 | `presentation.vfx` chỉ nhận ID trong `vfx/effects.json` | ✅ GD-22 v0.6 (validator V-15) |
| Glossary | `VfxCategory`, `VfxPhase`, `VfxMotif` (khi được duyệt); thống nhất `Weather.SPIRIT_RAIN` ↔ `LINH_VU` của GD-23 §7.1 | ⏳ ngoài phạm vi đợt rà |
| TD-15 | Không thêm công cụ mới ở v0.1; mọi công cụ VFX bổ sung phải đăng ký trước | — không cần ở v0.2 |
---

## 16. Câu hỏi mở
- [x] **VFX-Q-1:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: đã ghi vào AR-01 §4.2 + ART-R-022.)* `VPAL_NONE` (bạc trắng linh quang) cho vô hệ / `INHERIT` vô hệ có được AR-01 chấp nhận? Mặc định: dùng `VPAL_NONE` như §3.1.
- [x] **VFX-Q-2:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: giá trị §11.3 ghi vào GD-24 CAM-R-061; hiệu chỉnh bằng mẫu Phase 0 #5.)* Hex 4 bảng telegraph (§11.3) — chốt bằng mẫu Phase 0 #5 với người mù màu thật? Mặc định: giá trị §11.3.
- [x] **VFX-Q-3:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: giữ AR-01, phân biệt bằng hình dạng — AR-01 ART-R-020.)* Có cần đổi sắc độ `VPAL_HOA` lệch vàng hơn để xa màu cảnh báo, hoặc `VPAL_PHONG` xa viền an toàn lục nhạt? Mặc định: giữ AR-01, phân biệt bằng hình dạng (VFX-R-015).
- [x] **VFX-Q-4:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: chỉ theo preset — GD-24 CAM-R-064, AR-01 ART-R-041.)* GD-24 có thêm tùy chọn riêng "độ đậm VFX người khác" (ART-R-041) ngoài preset? Mặc định: chỉ theo preset TD-05 §9.
- [x] **VFX-Q-5:** *(Đã chốt — đợt rà nhất quán 2026-10-25: `PlayerPublic` gửi một lần, trường `elementAffinity` — TD-02 NW-R-025.)* Gửi hệ tâm pháp của người thi triển qua `S2C_SkillCast` (mỗi lần) hay `PlayerPublic` (một lần)? Mặc định tạm: `VPAL_NONE` cho `INHERIT` của người khác.
- [x] **VFX-Q-6:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: REST + cache — GD-08 TF-R-072.)* Client lấy thành phần của `CSK_*` qua `GET /v1/crafted-techniques/{id}` hay Game Server đính kèm khi phát `S2C_SkillCast`? Mặc định: REST + cache.
- [x] **VFX-Q-7:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: một VFX `VPAL_BANG`.)* `ST_FROST` do Thủy và Băng cùng gây — một VFX (`VPAL_BANG`) hay theo hệ nguồn? Mặc định: `VPAL_BANG`.
- [x] **VFX-Q-8:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: TD-11 PERF-R-032.)* TD-11 "thời gian sống ≤ 3s" có áp cho hiệu ứng vòng lặp do gameplay dài hơn (vùng tồn tại, triệu hồi, status)? Mặc định: VFX-R-086.
- [x] **VFX-Q-9:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: như một kỹ năng — TD-11 PERF-R-033.)* Ngân sách riêng cho chiêu boss / world boss? Mặc định: như một kỹ năng.
- [x] **VFX-Q-10:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: có, ≤ 25% — TD-11 PERF-R-035.)* Hạt thời tiết / không khí có tính vào trần hạt đồng thời TD-11 không, phần dành bao nhiêu? Mặc định: có, ≤ 25%.
- [x] **VFX-Q-11:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: `world/client.json → vfxBudget` — TD-05 §13.)* Trần hạt theo preset đọc từ file nào (TD-05 `world/client.json` chưa có khóa)? Mặc định: thêm khóa vào `world/client.json` do TD-05 sở hữu.
- [x] **VFX-Q-12:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: không.)* Có tăng "độ rực" VFX theo phẩm công pháp (Hoàng → Thiên) / phẩm phụ không? GD-08 chưa nói. Mặc định: không.
- [x] **VFX-Q-13:** *(Đã chốt theo mặc định — đợt rà nhất quán 2026-10-25: mẫu riêng — GD-07 SK-R-083.)* `VFX_THRUST` cho `LINE` là mẫu thứ 9 ngoài 8 mẫu GD-08 — chấp nhận, hay gộp vào `VFX_DASH`? Mặc định: mẫu riêng (chỉ dùng cho kỹ năng tác giả viết).

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến (telegraph trên cùng, màu + motif nguyên tố, phân biệt địch / ta, không che cảnh báo, ghép tham số, PRNG thẩm mỹ), bảng màu 8 hệ + `VPAL_NONE` (đề xuất hex / motif), màu hệ thống, 10 nhóm VFX + lớp vẽ, telegraph chi tiết, hệ VFX tham số GD-08 (8 mẫu + `VFX_THRUST`, bảng màu, lớp phủ theo biến thức, thuật toán chọn VFX, Viên Mãn, boss), trúng / status, môi trường / thế giới / đột phá / thiên kiếp / ngự kiếm / sự kiện / hiểm địa, ngân sách trích TD-11 / TD-05, pool / LOD / giảm khi đông, đặt tên + kiểu dữ liệu + JSON (đề xuất domain `vfx`), validator, danh sách MVP theo phase, quy trình AI, 25 tiêu chí nghiệm thu, danh sách đồng bộ, 13 câu hỏi mở |
| v0.2 | 2026-10-25 | — | Đợt rà nhất quán: đánh dấu đã chốt VFX-Q-1..13 (VFX-Q-5 chọn `PlayerPublic.elementAffinity`); bỏ "đề xuất" ở chỗ đã đồng bộ (bảng màu / motif → AR-01, `vfxTemplateId` / `paletteId` / `vfxOverlayId` → GD-08, `SkillDef.vfx` + suy mẫu → GD-07, VFX boss → GD-12, hệ tâm pháp → TD-02, ngân sách → TD-11 §3.2, `vfxBudget` / lớp vẽ → TD-05, telegraph hex + `flashReduction` → GD-24, `presentation.vfx` → GD-22); §15 thêm cột tình trạng. Còn chờ: TD-07 (domain `vfx`), AR-02, glossary, README / PLAN |
| v0.3 | 2026-10-25 | — | Binding `ST_CHARMED`; domain `vfx` đã đăng ký TD-07; mẫu tên theo AR-02 §3 |

