# GD-24 — Camera & Điều khiển

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-24 · Prefix ID: `CAM` |
| Owner | Game Designer + Client Dev |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 0 (spike camera 2.5D + WASD + chuột nhắm), Phase 1 (bản đầy đủ PC), sau launch (cảm ứng, PLAN §5.8) |
| Quyết định nền | D-01 (camera chéo 45–55°, logic 2D), D-09 (camera cố định ở MVP), D-10 (WASD mặc định, click-to-move tùy chọn), D-22 (đa nền tảng cùng codebase) |
| Liên quan | [PLAN §4 (sơ đồ phím ban đầu), §5.2, §5.8](../../PLAN.md) · [TD-05 Client](../tech/04-client.md) (`IntentFrame`, adapter, CL-R-022) · [TD-01 TA-R-161](../tech/00-tdd.md) · [TD-02 Network](../tech/01-network.md) (`C2S_Input`, aim là điểm) · [TD-14 Geometry](../tech/13-geom2d.md) (GEO-R-043) · [GD-07 Kỹ năng](13-skills.md) · [GD-06 Chỉ số](12-stats-formulas.md) (né, đỡ) · [GD-12 Quái](18-enemies-ai.md) (vùng cảnh báo) · [GD-21 Open World](22-open-world.md) · [GD-25 Linh thú](26-spirit-beasts.md) · [GD-09 Vật phẩm](15-items-equipment.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-32 PvP](31-pvp-infamy.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [AR-01 Art Bible](../art/art-bible.md) §4.4 (màu hệ thống) · [AR-04 VFX Guidelines](../art/vfx-guidelines.md) §5, §8.3 (telegraph, chớp) |

> Tài liệu này là **nguồn chuẩn duy nhất** cho: thông số camera, **sơ đồ phím mặc định**, hỗ trợ ngắm, tay cầm, cảm ứng, trợ năng. Mọi thiết bị đầu vào đều quy về `IntentFrame` (TD-05 §2.3) — server không biết người chơi dùng thiết bị gì (TA-R-161). Phím nhắc trong GD khác mà vênh với §5 thì theo GD-24.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-24 | Mục |
|--------------|-----------------|-----|
| TD-05 | Tham số camera, adapter bàn phím-chuột / tay cầm / cảm ứng, hỗ trợ ngắm | §4, §5–§8 |
| Mọi GD có UI / phím | Sơ đồ phím chuẩn | §5 |
| GD-12 | Vùng cảnh báo luôn thấy (không bị che), bảng màu trợ năng | CAM-R-006, §9 |
| GD-19 (sau) | Không bán lợi thế điều khiển (vd. tự ngắm tốt hơn) | CAM-R-005 |

---

## 1. Mục tiêu & bất biến

- Combat hành động **đọc được**: luôn thấy nhân vật, kẻ địch gần, vùng cảnh báo; nhắm 360° chính xác.
- Điều khiển **quen tay** với người chơi ARPG (WASD + chuột) và **chơi được** trên tay cầm / cảm ứng mà không cần server đổi gì.
- Không có cài đặt nào cho lợi thế thi đấu không công bằng (đặc biệt khi có PvP, GD-32).

| Mã | Bất biến |
|----|----------|
| <a id="cam-r-001"></a>**CAM-R-001** | Mọi thiết bị đầu vào sinh **cùng `IntentFrame`** (TD-05 §2.3): `move` (vector ≤ 1), `aim` (điểm trên mặt phẳng `(x, z)`, TD-02 / TD-14 GEO-R-043), `walk`, `actions[]`. Server và combat không phân biệt thiết bị. |
| <a id="cam-r-002"></a>**CAM-R-002** | Camera **không ảnh hưởng gameplay** phía server: tầm đánh, tầm nhìn quái, AOI (TD-02 40 / 48m) độc lập với zoom. Zoom ra không làm thấy thêm thực thể ngoài AOI hay vật ẩn chưa phát hiện. |
| <a id="cam-r-003"></a>**CAM-R-003** | **Hỗ trợ ngắm chỉ chạy ở client** và chỉ đổi điểm `aim` gửi đi; server vẫn kiểm tra tầm / hình như mọi lệnh (GD-07 SK-R-070..071). |
| <a id="cam-r-004"></a>**CAM-R-004** | **Một sơ đồ phím mặc định chuẩn** (§5); mọi phím đổi được (trừ `Esc` và phím debug). Không hai hành động cùng ngữ cảnh trùng một phím. |
| <a id="cam-r-005"></a>**CAM-R-005** | Mức hỗ trợ ngắm theo thiết bị cố định trong gamedata, **không** bán / mở khóa bằng tiền hay tiến trình (D-17 tinh thần công bằng). |
| <a id="cam-r-006"></a>**CAM-R-006** | **Vùng cảnh báo** (telegraph, GD-12 EN-R-070) và **vòng chân nhân vật mình** luôn hiển thị phía trên vật che / hiệu ứng; không cài đặt nào tắt được vùng cảnh báo của kẻ địch. |

---

## 2. Tổng quan

```
 Bàn phím + chuột ─┐
 Tay cầm ──────────┼─► adapter (TD-05) ─► hỗ trợ ngắm (client, CAM-R-003) ─► IntentFrame ─► dự đoán (shared/sim) + C2S_Input / lệnh (TD-02)
 Cảm ứng ──────────┘                                                                  ▲
                                                                     camera (theo nhân vật, zoom, làm mờ vật che) ─┘ (chỉ hiển thị)
```

---

## 3. Kiểu dữ liệu

```ts
interface CameraParams {                    // shared/gamedata/world/camera.json
  pitchDeg: number;                          // góc nghiêng mặc định (50)
  pitchRangeDeg: [number, number];           // cho phép chỉnh trong cài đặt (45–55, D-01)
  yawDeg: number;                            // cố định MVP (0 = nhìn về +z / Bắc ở trên màn hình, khớp minimap)
  fovDeg: number;                            // góc nhìn dọc
  distance: { default: number; min: number; max: number; step: number };   // mét, cuộn chuột / chụm ngón
  followLerp: number;                        // độ mượt theo nhân vật (0–1 mỗi khung 60fps)
  aimLookAhead: { enabled: boolean; ratio: number; maxM: number };         // dịch tâm camera về phía con trỏ
  occluderFade: { radiusM: number; minAlpha: number; fadeSec: number };
  shake: { maxAmplitudeM: number; maxDurationSec: number };
  boundsInsetM: number;                      // không cho tâm camera ra sát rìa bản đồ
}

type InputDevice = 'KBM' | 'GAMEPAD' | 'TOUCH';
type ActionId =                               // danh sách đóng (CAM-R-004)
  | 'MOVE_UP' | 'MOVE_DOWN' | 'MOVE_LEFT' | 'MOVE_RIGHT' | 'WALK_HOLD'
  | 'BASIC_ATTACK' | 'HEAVY_ATTACK' | 'DODGE' | 'BLOCK'
  | 'SKILL_Q' | 'SKILL_E' | 'SKILL_R' | 'SKILL_F' | 'SKILL_V' | 'ULTIMATE'
  | 'ARTIFACT' | 'QUICK_1' | 'QUICK_2' | 'QUICK_3' | 'QUICK_4'
  | 'INTERACT' | 'SCAN' | 'MEDITATE'
  | 'BEAST_COMMAND' | 'BEAST_SUMMON'
  | 'TARGET_LOCK' | 'SHOW_NAMES'
  | 'UI_BAG' | 'UI_CHARACTER' | 'UI_TECHNIQUES' | 'UI_QUESTS' | 'UI_MAP' | 'UI_CODEX'
  | 'UI_FRIENDS' | 'UI_BEASTS' | 'UI_CAVE' | 'UI_PK_MODE' | 'UI_CHAT' | 'UI_MENU'
  | 'CAMERA_ZOOM_IN' | 'CAMERA_ZOOM_OUT';

interface KeyBinding { action: ActionId; device: InputDevice; primary: string; secondary?: string }

interface ControlSettings {                  // lưu theo tài khoản (CAM-R-090)
  version: number;
  bindings: KeyBinding[];                    // chỉ phần khác mặc định
  clickToMove: boolean;                      // D-10 (mặc định false)
  holdToAim: boolean;                        // giữ phím kỹ năng để hiện hình rồi thả để thi triển (GD-07 §11)
  aimLookAhead: boolean;
  cameraPitchDeg: number;
  toggleInsteadOfHold: { block: boolean; walk: boolean };
  gamepad: { deadzone: number; aimAssist: 'STANDARD' | 'LOW' | 'OFF'; vibration: boolean };
  touch: TouchLayout;
  accessibility: AccessibilitySettings;
}

interface TouchLayout {
  joystick: 'FLOATING' | 'FIXED'; leftHanded: boolean; buttonScale: number;   // 0,8–1,4
  smartCast: boolean;                        // chạm nhanh = tự chọn mục tiêu (§8)
  positions?: Record<string, [number, number]>;   // tùy chỉnh vị trí nút (tỷ lệ màn hình)
}

interface AccessibilitySettings {
  screenShake: number;                       // 0–1 (mặc định 0,7)
  flashReduction: boolean;                   // ngưỡng: CAM-R-060
  telegraphPalette: 'DEFAULT' | 'DEUTERANOPIA' | 'PROTANOPIA' | 'TRITANOPIA';   // ↔ TGPAL_{giá trị} (CAM-R-061)
  telegraphPattern: boolean;                 // thêm sọc chéo cho vùng cảnh báo
  telegraphOpacity: number;                  // 0,6–1 (không thấp hơn 0,6 — CAM-R-006)
  uiScale: number;                           // 0,8–1,5
  fontScale: number;                         // 0,9–1,4
  subtitles: boolean;
}                                            // không có trường riêng "độ đậm VFX người khác" — CAM-R-064
```

---

## 4. Camera

- <a id="cam-r-010"></a>**CAM-R-010** **Camera chéo cố định** (D-01, D-09): `ArcRotateCamera` giới hạn, nghiêng `pitchDeg` 50° (người chơi chỉnh trong 45–55°), **hướng cố định** `yawDeg = 0` — Bắc (`+z`, TD-14) ở phía trên màn hình, khớp minimap / bản đồ thế giới. Không xoay camera ở MVP (CAM-Q-1).
- <a id="cam-r-011"></a>**CAM-R-011** **Khoảng cách / zoom:** mặc định 20m, tối thiểu 14m, tối đa 26m, bước 1,5m (cuộn chuột, chụm 2 ngón, D-pad tay cầm); ở mặc định khung nhìn mặt đất ≈ **40 × 30m** (PLAN §5.2) — kiểm chứng ở spike Phase 0 và hiệu chỉnh `fovDeg` / `distance` cho khớp. Zoom tối đa không vượt AOI (CAM-R-002).
- <a id="cam-r-012"></a>**CAM-R-012** **Theo nhân vật:** tâm camera nội suy về vị trí **dự đoán** của nhân vật (TD-05) với `followLerp`; khi bật `aimLookAhead`, tâm dịch về phía điểm nhắm `ratio` 15%, tối đa 3m (giúp nhắm đòn xa; tắt được). Dịch chuyển tức thời (chuyển bản đồ, đẩy lùi > 1,5m) → camera nhảy theo, không trượt dài.
- <a id="cam-r-013"></a>**CAM-R-013** **Làm mờ vật che:** vật thể nằm giữa camera và nhân vật mình (hình trụ bán kính 1,5m dọc tia nhìn) **mờ dần** tới alpha 0,25 trong 0,15s (dither shader, TD-05); mái nhà trong thành trấn ẩn hẳn khi nhân vật vào trong. Vùng cảnh báo, vòng chân, tên mục tiêu **không bao giờ** bị che (CAM-R-006: vẽ lớp phủ, không kiểm tra độ sâu).
- <a id="cam-r-014"></a>**CAM-R-014** **Rung màn hình** chỉ cho sự kiện lớn (đòn mạnh trúng mình, boss dậm, thiên lôi, nổ): biên độ ≤ 0,3m, ≤ 0,4s, nhân với `accessibility.screenShake`; không rung khi đang ngắm kỹ năng `holdToAim`.
- <a id="cam-r-015"></a>**CAM-R-015** **Ranh giới:** tâm camera bị kẹp cách rìa bản đồ `boundsInsetM` (8m) để không lộ khoảng trống ngoài bản đồ; nhân vật ở sát rìa thì lệch khỏi tâm màn hình (chấp nhận).
- <a id="cam-r-016"></a>**CAM-R-016** **Cảnh ngắn (cutscene):** cổng vực lần đầu, Kiệt Sức của boss đặc biệt (GD-25), màn tổng kết kiếp (GD-26), cảnh đối ẩm (GD-32 lưu ý nội dung): camera chạy theo đường dựng sẵn ≤ 8s, khóa đầu vào gameplay, **luôn bỏ qua được** (`Esc` / chạm); **không** có cutscene khi đang trong vùng có thể chết thật (hiểm địa, PvP đang chiến) — chỉ dùng hiệu ứng nhẹ.

---

## 5. Sơ đồ phím mặc định (PC — nguồn chuẩn)

| Nhóm | Hành động (`ActionId`) | Phím mặc định | Ghi chú / nguồn |
|------|------------------------|---------------|-----------------|
| Di chuyển | `MOVE_*` | `W` `A` `S` `D` | D-10 |
| | `WALK_HOLD` | giữ `Ctrl` | Đi chậm (`IntentFrame.walk`, TD-05) |
| | Click-to-move (tùy chọn) | chuột phải giữ / nhấp *(chỉ khi bật `clickToMove`; khi đó Đánh mạnh chuyển sang `Shift` + chuột trái)* | D-10, CAM-R-021 |
| Nhắm | — | **con trỏ chuột** = điểm `aim` | GD-07, TD-02 |
| Chiến đấu | `BASIC_ATTACK` | chuột trái | Combo (GD-07 §5.4) |
| | `HEAVY_ATTACK` | giữ chuột phải, thả để đánh | GD-07 SK-R-053 |
| | `DODGE` | `Space` | Né lướt (GD-06) — hướng theo phím di chuyển, đứng yên thì theo con trỏ |
| | `BLOCK` | giữ `Shift` | Đỡ / đỡ hoàn hảo (GD-06) |
| | `SKILL_Q/E/R/F` · `SKILL_V` | `Q` `E` `R` `F` · `V` (từ Trúc Cơ) | GD-07 SK-R-040 |
| | `ULTIMATE` | `T` | Ô tuyệt kỹ (Kim Đan, GD-07 SK-R-041) |
| | `ARTIFACT` | `Tab` | Kỹ năng pháp bảo (GD-09 IT-R-024) |
| | `QUICK_1..4` | `1` `2` `3` `4` | Ô nhanh (GD-09) |
| | `BEAST_COMMAND` | `Y` | Chiêu lệnh linh thú (GD-25 SB-R-043) — **đổi từ `T`** (trùng tuyệt kỹ) |
| | `BEAST_SUMMON` | `U` | Gọi / thu linh thú (GD-25 SB-R-041) — **đổi từ `G`** (trùng thần thức quét) |
| | `TARGET_LOCK` | chuột giữa | Khóa mục tiêu mềm (CAM-R-031) |
| Thế giới | `INTERACT` | `X` | Nói chuyện, nhặt, hái (GD-21 OW-R-102), tương tác trạm / cổng, cứu đồng đội (giữ, GD-28), thoát hiểm địa, **thu phục Linh chủng / boss** (giữ khi đủ điều kiện, GD-25) |
| | `SCAN` | `G` | Thần thức quét (GD-21 §3.7) |
| | `MEDITATE` | `Z` | Đả tọa (GD-04 CU-R-030) |
| | `SHOW_NAMES` | giữ `Alt` | Hiện tên vật phẩm rơi / người chơi |
| Giao diện | `UI_BAG` | `I` | Túi đồ (GD-09) |
| | `UI_CHARACTER` | `C` | Nhân vật / tu luyện (GD-04) |
| | `UI_TECHNIQUES` | `K` | Công pháp (GD-07) |
| | `UI_QUESTS` | `L` | Sổ nhiệm vụ (GD-15) |
| | `UI_MAP` | `M` | Bản đồ thế giới (GD-21) |
| | `UI_CODEX` | `J` | Vạn Giới Lục (GD-21) |
| | `UI_FRIENDS` | `O` | Bạn bè (GD-14) |
| | `UI_BEASTS` | `B` | Bảng linh thú (GD-25) |
| | `UI_CAVE` | `H` | Quản lý động phủ (chỉ trong động phủ, GD-30) |
| | `UI_PK_MODE` | `P` | Bảng chế độ PK (GD-32) |
| | `UI_CHAT` | `Enter` | Mở / gửi chat |
| | `UI_MENU` | `Esc` | Menu, hủy, bỏ qua cảnh ngắn (không đổi được) |
| Camera | `CAMERA_ZOOM_IN/OUT` | cuộn chuột | CAM-R-011 |
| Dev | Debug hitbox / mạng / gamedata | `F9` / `F10` / `F11` | Chỉ bản dev (TD-05) |

- <a id="cam-r-020"></a>**CAM-R-020** **Ngữ cảnh riêng:** trong minigame chế tác (GD-10 / GD-11), phím do minigame quy định (vd. giữ chuột trái / `W` giữ lửa, `Space` thêm nguyên liệu); trong cơ duyên / hội thoại: chuột hoặc `1`–`9` chọn lựa chọn. Ngoài các ngữ cảnh này, bảng §5 áp dụng.
- <a id="cam-r-021"></a>**CAM-R-021** **Click-to-move** (tùy chọn, D-10): chuột phải đặt điểm đến; client tìm đường (recast, TD-14 §8) rồi sinh chuỗi `move` như WASD (TD-02 NW-R-031); WASD bất kỳ lúc nào hủy đường đi. Khi bật, Đánh mạnh chuyển sang `Shift` + chuột trái và Đỡ chuyển sang `Caps Lock` (giữ) — bảng đổi phím hiển thị rõ.
- <a id="cam-r-022"></a>**CAM-R-022** **Bí pháp tiêu thọ** (GD-07 SK-R-110) và **binh giải** (GD-26 §5.16) luôn yêu cầu **giữ phím** (1s / 3s) — không có tùy chọn bỏ giữ (an toàn, chống bấm nhầm).
- <a id="cam-r-023"></a>**CAM-R-023** **Đổi phím:** mọi `ActionId` (trừ `UI_MENU`, debug) gán lại được (2 phím: chính + phụ); phát hiện trùng trong cùng ngữ cảnh → yêu cầu xử lý trước khi lưu; nút "Khôi phục mặc định".

---

## 6. Nhắm & hỗ trợ ngắm

- <a id="cam-r-030"></a>**CAM-R-030** **Điểm nhắm (PC):** giao điểm tia con trỏ với mặt phẳng `y = độ cao nhân vật` (không chọn trúng mesh), quy về `(x, z)`; `GROUND_CIRCLE` kẹp vào `maxRange` (TD-14 §5.2). `holdToAim` bật: giữ phím kỹ năng hiện hình dạng (quạt / vùng / đường đạn, `shapeOutline` TD-14), thả để thi triển; tắt (mặc định): thi triển ngay khi bấm.
- <a id="cam-r-031"></a>**CAM-R-031** **Khóa mục tiêu mềm** (`TARGET_LOCK`): chọn kẻ địch gần con trỏ nhất trong 15m; khi khóa, đánh thường và kỹ năng **không** có hình vùng sẽ lấy điểm nhắm = vị trí mục tiêu (hiển thị) nếu con trỏ ở trong 60° quanh hướng tới mục tiêu; vòng khóa trên đầu mục tiêu; mất khóa khi mục tiêu chết / ra ngoài 25m / bấm lại. Kỹ năng vùng (`GROUND_CIRCLE`) vẫn theo con trỏ.
- <a id="cam-r-032"></a>**CAM-R-032** **Mức hỗ trợ ngắm theo thiết bị** (CAM-R-003, CAM-R-005):

| Thiết bị | Đánh thường | Kỹ năng hướng (quạt, đường, đạn) | Kỹ năng vùng |
|----------|-------------|----------------------------------|--------------|
| Bàn phím + chuột | Hút về kẻ địch trong 25° quanh con trỏ (GD-07 SK-R-052) | Không | Không |
| Tay cầm | 35°, ưu tiên mục tiêu khóa | 20° quanh hướng cần phải | Điểm = mục tiêu khóa, hoặc cách 6m theo hướng cần phải |
| Cảm ứng | Tự chọn mục tiêu (§8) | Kéo nút để ngắm; chạm nhanh = tự chọn (smart cast) | Kéo để đặt; chạm nhanh = đặt lên mục tiêu tự chọn |

  Trong **PvP** (người chơi là mục tiêu): góc hút giảm một nửa cho mọi thiết bị (giảm chênh lệch giữa thiết bị).

---

## 7. Tay cầm

- <a id="cam-r-040"></a>**CAM-R-040** Sơ đồ mặc định (Xbox / PS): cần trái di chuyển (vùng chết 0,15); **cần phải nhắm** (điểm nhắm cách nhân vật 6m theo hướng cần, không đẩy cần → theo hướng mặt); `RT` đánh thường · `LT` giữ đánh mạnh · `A`/✕ né · `RB` giữ đỡ · `LB` giữ + `X`/`Y`/`B`/`A` = kỹ năng Q/E/R/F, `LB` + `RT` = V · `LB` + `RB` = tuyệt kỹ · `X`/□ tương tác · `Y`/△ pháp bảo · `B`/○ thần thức quét · D-pad = ô nhanh 1–4 (giữ D-pad xuống = đả tọa) · `R3` khóa mục tiêu · `L3` gọi / thu linh thú, `LB` + `L3` chiêu lệnh linh thú · `Menu` menu · `View` bản đồ. Đổi phím được (CAM-R-023).
- <a id="cam-r-041"></a>**CAM-R-041** Rung tay cầm cho trúng đòn mạnh / đỡ hoàn hảo / bị đánh gục; tắt được.
- <a id="cam-r-042"></a>**CAM-R-042** Giao diện chuyển chế độ "tiêu điểm" (điều hướng bằng D-pad) khi phát hiện tay cầm; biểu tượng phím trên UI đổi theo thiết bị đang dùng gần nhất.

---

## 8. Cảm ứng (mobile — sau launch, PLAN §5.8)

```
 ┌──────────────────────────────────────────────────────────────┐
 │ [minimap]                                       [menu][chat] │
 │                                                               │
 │                                       (Q)  (E)                │
 │   ◯ joystick                       (R)  ( ĐÁNH )  (né)        │
 │  (nổi, nửa trái)                     (F)   (V)    (đỡ)        │
 │                                      [1][2][3][4]  (T)(X)(G)  │
 └──────────────────────────────────────────────────────────────┘
```

- <a id="cam-r-050"></a>**CAM-R-050** **Joystick ảo** nửa trái màn hình: nổi (xuất hiện tại chỗ chạm) mặc định hoặc cố định; đẩy nhẹ (< 40% bán kính) = đi chậm (`walk`).
- <a id="cam-r-051"></a>**CAM-R-051** **Nút kỹ năng kéo để ngắm:** chạm giữ nút → hiện hình dạng; kéo ngón → hướng / điểm (bán kính kéo ánh xạ tới `maxRange`); thả để thi triển; kéo vào vùng **Hủy** (biểu tượng X phía trên cụm nút) để hủy. **Chạm nhanh** (< 150ms, `smartCast` bật) = thi triển về mục tiêu tự chọn.
- <a id="cam-r-052"></a>**CAM-R-052** **Tự chọn mục tiêu** (thứ tự ưu tiên): mục tiêu đang khóa → kẻ địch vừa đánh mình (3s) → kẻ địch gần nhất trong hình nón 60° theo hướng joystick / hướng mặt trong tầm kỹ năng → kẻ địch gần nhất trong tầm; không có → theo hướng mặt. Chạm vào kẻ địch trên màn hình = khóa mục tiêu.
- <a id="cam-r-053"></a>**CAM-R-053** **Né:** hướng theo joystick, đứng yên thì theo hướng mặt; **Đỡ:** giữ nút (hoặc bật / tắt nếu `toggleInsteadOfHold.block`).
- <a id="cam-r-054"></a>**CAM-R-054** Nút **ngữ cảnh** `X` (tương tác) chỉ hiện khi có đối tượng tương tác trong tầm, đổi biểu tượng theo việc (nhặt, hái, nói chuyện, thu phục, cứu…); giữ nút cho thao tác cần giữ.
- <a id="cam-r-055"></a>**CAM-R-055** Vùng chạm ≥ 44 pt (TD-01 TA-R-163); bố cục đổi được vị trí / cỡ (0,8–1,4), có chế độ tay trái; chụm 2 ngón để zoom.

---

## 9. Trợ năng

- <a id="cam-r-060"></a>**CAM-R-060** **Rung / chớp:** thanh `screenShake` 0–100% (mặc định 70%); `flashReduction` giảm độ sáng và tần số chớp của VFX (thiên lôi, nổ) xuống dưới ngưỡng an toàn với người nhạy cảm ánh sáng. **Ngưỡng** khi `flashReduction = true`: độ sáng đỉnh của VFX chớp mạnh giảm **≥ 50%** và **không quá 3 lần chớp / giây** (`controls.json → accessibility.flashReduction`: `peakBrightnessMult` 0,5, `maxFlashesPerSec` 3); áp cho mọi VFX có `respectsFlashReduction` (AR-04 VFX-R-080: thiên lôi, nổ lớn, chớp thời tiết).
- <a id="cam-r-061"></a>**CAM-R-061** **Vùng cảnh báo cho người mù màu:** bảng màu thay thế theo loại (`DEUTERANOPIA`, `PROTANOPIA`, `TRITANOPIA`), tùy chọn **sọc chéo** để phân biệt không cần màu; độ đậm 0,6–1 (không thấp hơn — CAM-R-006). Màu mặc định (nghĩa màu theo AR-01 §4.4): vùng của kẻ địch đỏ cam, đòn không thể đỡ viền đỏ đậm + sọc luôn bật (GD-06 SF-R-102), vùng của đồng minh / kỹ năng mình lam trắng mờ. Giá trị bốn bảng (`TGPAL_{telegraphPalette}`, dữ liệu ở `shared/gamedata/vfx/telegraph_palettes.json`, kiểu `TelegraphPaletteDef` — AR-04 §11.2–§11.3; domain `vfx` chờ TD-07 đăng ký):

| `id` | `enemyFill` | `enemyRim` | `unblockableRim` | `allyFill` | `allyRim` |
|------|-------------|------------|------------------|------------|-----------|
| `TGPAL_DEFAULT` | `#FF4A1C` | `#FFE2B8` | `#8C0A0A` | `#CFE6FF` | `#FFFFFF` |
| `TGPAL_DEUTERANOPIA` | `#FF9F1C` | `#FFF1C2` | `#5A2A00` | `#3D7EFF` | `#DDE8FF` |
| `TGPAL_PROTANOPIA` | `#FFB000` | `#FFF4CC` | `#4A3300` | `#3D7EFF` | `#DDE8FF` |
| `TGPAL_TRITANOPIA` | `#E8175D` | `#FFD1E0` | `#5C0022` | `#2EC4B6` | `#D6FFFA` |

  Giá trị là **khởi điểm** (AR-04 VFX-R-014, VFX-Q-2 đã chốt theo mặc định); hiệu chỉnh bằng mẫu Phase 0 #5 (AR-01 §10) thì sửa bảng này (tăng phiên bản) và file dữ liệu cùng lúc. Vùng cảnh báo vẽ unlit, không chịu sương / bloom / ngày đêm (AR-04 VFX-R-021).
- <a id="cam-r-062"></a>**CAM-R-062** **Cỡ giao diện** 80–150%, **cỡ chữ** 90–140%, phụ đề cho lời thoại có giọng.
- <a id="cam-r-063"></a>**CAM-R-063** **Giữ / bật-tắt:** `BLOCK` và `WALK_HOLD` có chế độ bật-tắt; đánh mạnh (tụ lực) và các thao tác an toàn (CAM-R-022) vẫn là giữ.
- <a id="cam-r-064"></a>**CAM-R-064** **Độ đậm VFX của người khác** (AR-01 ART-R-041) **chỉ** điều chỉnh qua preset đồ họa (TD-05 §9, dòng "VFX người chơi khác"); `AccessibilitySettings` **không** có tùy chọn riêng (chốt AR-04 VFX-Q-4 theo mặc định). VFX thù địch và vùng cảnh báo không bị giảm (AR-04 VFX-R-025, VFX-R-026).

---

## 10. Lưu cài đặt

- <a id="cam-r-090"></a>**CAM-R-090** `ControlSettings` lưu **theo tài khoản** trong `account.settings` (TD-03 §6) qua `GET / PUT /v1/accounts/me/settings` (TD-04), đồng bộ giữa máy / thiết bị; bản sao cục bộ (IndexedDB / Platform.Storage, TD-05) để dùng ngay khi chưa tải. Phần **riêng thiết bị** (tay cầm, bố cục cảm ứng, chất lượng đồ họa) lưu theo cặp tài khoản × loại thiết bị.
- <a id="cam-r-091"></a>**CAM-R-091** `version` tăng khi đổi sơ đồ mặc định; client chuyển đổi cài đặt cũ, phím xung đột với mặc định mới được báo để người chơi chọn.

---

## 11. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/world/camera.json` | `CameraParams` (§3) |
| `shared/gamedata/world/controls.json` | Sơ đồ phím mặc định theo thiết bị (§5, §7, §8), tham số hỗ trợ ngắm (§6), bố cục cảm ứng mặc định, ngưỡng trợ năng (`accessibility`, §9) |
| `shared/gamedata/vfx/telegraph_palettes.json` | 4 bảng màu vùng cảnh báo `TGPAL_*` (CAM-R-061; file thuộc domain `vfx` của AR-04, chờ TD-07 đăng ký) |

```jsonc
// camera.json
{
  "pitchDeg": 50, "pitchRangeDeg": [45, 55], "yawDeg": 0, "fovDeg": 40,
  "distance": { "default": 20, "min": 14, "max": 26, "step": 1.5 },
  "followLerp": 0.18,
  "aimLookAhead": { "enabled": true, "ratio": 0.15, "maxM": 3 },
  "occluderFade": { "radiusM": 1.5, "minAlpha": 0.25, "fadeSec": 0.15 },
  "shake": { "maxAmplitudeM": 0.3, "maxDurationSec": 0.4 },
  "boundsInsetM": 8
}
// controls.json (trích)
{
  "aimAssist": {
    "KBM":     { "basicDeg": 25, "directionalDeg": 0 },
    "GAMEPAD": { "basicDeg": 35, "directionalDeg": 20, "aimDistanceM": 6 },
    "TOUCH":   { "smartCastTapMs": 150, "autoTargetConeDeg": 60, "dragCancelZone": true },
    "pvpMultiplier": 0.5
  },
  "targetLock": { "pickRadiusM": 15, "keepRadiusM": 25, "coneDeg": 60 },
  "holdSafety": { "forbiddenArtSec": 1, "selfDissolutionSec": 3 },
  "accessibility": { "flashReduction": { "peakBrightnessMult": 0.5, "maxFlashesPerSec": 3 }, "telegraphOpacityMin": 0.6 },
  "defaults": { "KBM": [ { "action": "SKILL_Q", "primary": "KeyQ" }, { "action": "BEAST_COMMAND", "primary": "KeyY" } ] }
}
```

**Validator:** mọi `ActionId` có phím mặc định cho `KBM`; không trùng phím trong cùng ngữ cảnh; `telegraphOpacity` tối thiểu 0,6; `pitchRangeDeg` nằm trong 45–55 (D-01); `telegraph_palettes.json` có đúng 4 bản ghi `TGPAL_{giá trị telegraphPalette}`, mọi màu dạng `#RRGGBB`; `flashReduction.peakBrightnessMult ≤ 0,5`, `maxFlashesPerSec ≤ 3`.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| TD-05 | Adapter, `IntentFrame`, camera, làm mờ vật che, lớp phủ vùng cảnh báo |
| TD-02 / TD-14 | `aim` là điểm, `walk`, click-to-move sinh `move` |
| GD-07 / GD-06 | Phím chiến đấu, hỗ trợ ngắm đánh thường, giữ phím bí pháp |
| GD-12 | Vùng cảnh báo luôn thấy, bảng màu trợ năng |
| GD-21 / GD-25 / GD-28 / GD-30 / GD-32 | Phím thế giới (quét, tương tác, thu phục, cứu, động phủ, PK) |
| TD-03 / TD-04 | Lưu `ControlSettings` theo tài khoản |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Đổi thiết bị giữa trận (cắm tay cầm khi đang dùng chuột) | Adapter mới nhất có đầu vào thắng; UI đổi biểu tượng; không mất lệnh đang đệm (GD-07 SK-R-010) |
| Con trỏ ra ngoài cửa sổ trình duyệt | Giữ điểm nhắm cuối; mất focus cửa sổ → thả mọi phím đang giữ (tránh "kẹt" đỡ / chạy) |
| Phím trình duyệt trùng (`Ctrl+W`, `F5`, `Tab` rời trang) | Chặn mặc định trình duyệt trong khi đang chơi cho các phím của game; `Ctrl` + phím hệ thống không gán được |
| Nhân vật đứng sau nhà, kẻ địch đang ra chiêu | Nhà mờ (CAM-R-013), vùng cảnh báo vẫn vẽ trên cùng |
| Click-to-move vào chỗ không đi được | Đi tới điểm đi được gần nhất trên navmesh |
| Màn hình rất rộng (21:9) | Không tăng khung nhìn quá 48m chiều ngang (giữ trong AOI); thêm viền mờ hai bên nếu cần |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).
| Sự kiện | Trường | Câu hỏi |
|---------|--------|---------|
| `input_device_session` | device, thời lượng | Tỷ lệ chuột / tay cầm / cảm ứng |
| `rebind` | action, phím cũ → mới | Phím mặc định nào bị đổi nhiều (sơ đồ chưa tốt) |
| `settings_changed` | khóa cài đặt | Click-to-move, holdToAim, trợ năng được dùng bao nhiêu |
| `camera_zoom_avg` | khoảng cách trung bình | Hiệu chỉnh `distance.default` |
| `skill_cast_hit_rate` | device, skill | Chênh lệch trúng đòn giữa thiết bị (cân hỗ trợ ngắm) |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="cam-ac-001"></a>CAM-AC-001 | Camera mặc định trên màn hình 16:9 | Đo khung nhìn mặt đất | ≈ 40 × 30m (± 10%) |
| CAM-AC-002 | Zoom tối đa | Có quái cách 45m | Quái không hiện (ngoài AOI, CAM-R-002) |
| CAM-AC-003 | Nhân vật đứng sau nhà, boss ra vùng cảnh báo | Hiển thị | Nhà mờ ≤ 0,25 alpha trong 0,15s; vùng cảnh báo thấy đủ |
| CAM-AC-004 | Bấm `T` khi ô tuyệt kỹ chưa mở và có linh thú xuất chiến | Kết quả | Không gọi chiêu lệnh thú (chiêu lệnh là `Y`) |
| CAM-AC-005 | Bấm `G` | Kết quả | Thần thức quét; linh thú không bị gọi / thu |
| CAM-AC-006 | Đổi `SKILL_Q` sang `E` | Lưu | Báo trùng với `SKILL_E`, chưa lưu tới khi xử lý |
| CAM-AC-007 | Cùng một đòn `CONE` | Bắn bằng chuột / tay cầm / cảm ứng | Server nhận cùng định dạng `C2S_CastSkill { aim: [x, z] }` |
| CAM-AC-008 | Tay cầm, mục tiêu lệch 30° so với hướng cần phải | Đánh thường | Hút trúng (35°); trong PvP với mục tiêu người chơi lệch 30° → không hút (17,5°) |
| CAM-AC-009 | Cảm ứng, chạm nhanh nút `Q` có kẻ địch trong nón 60° | Thi triển | Hướng về kẻ địch gần nhất trong nón |
| CAM-AC-010 | Cảm ứng, kéo nút kỹ năng vào vùng Hủy | Thả | Không thi triển, không tốn tài nguyên |
| CAM-AC-011 | Bí pháp tiêu thọ | Bấm nhanh (< 1s) | Không thi triển (CAM-R-022) |
| CAM-AC-012 | `telegraphPalette = DEUTERANOPIA`, `telegraphPattern = true` | Vùng cảnh báo | Đổi bảng màu + có sọc; độ đậm ≥ 0,6 |
| CAM-AC-013 | Đổi phím trên PC | Đăng nhập trên máy khác | Phím đã đổi được áp dụng (CAM-R-090) |
| CAM-AC-014 | Mất focus cửa sổ khi đang giữ `Shift` (đỡ) | Quay lại | Không còn đỡ (đã thả) |
| CAM-AC-015 | Đang trong hiểm địa, kích hoạt sự kiện có cảnh ngắn | Kết quả | Không chạy cutscene khóa đầu vào (CAM-R-016) |
| <a id="cam-ac-016"></a>CAM-AC-016 | `flashReduction = true`, thiên kiếp 3 tia trong 1s | Render `VFX_TRIB_STRIKE` | Độ sáng đỉnh ≤ 50% bản gốc, ≤ 3 lần chớp / giây (CAM-R-060, AR-04 VFX-AC-017) |
| <a id="cam-ac-017"></a>CAM-AC-017 | `telegraphPalette = TRITANOPIA` | Vùng cảnh báo kẻ địch | Nền `#E8175D`, viền `#FFD1E0` (`TGPAL_TRITANOPIA`, CAM-R-061) |
| <a id="cam-ac-018"></a>CAM-AC-018 | Lưu `ControlSettings` có trường `accessibility.otherVfxOpacity` | Kiểm tra schema | Bị loại (trường không tồn tại, CAM-R-064) |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 0–1 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:---:|:---:|
| Camera cố định, zoom, theo nhân vật, làm mờ vật che, lớp phủ vùng cảnh báo | ✅ | | | |
| Sơ đồ phím PC, đổi phím, click-to-move, holdToAim, khóa mục tiêu mềm | | ✅ | | |
| Trợ năng (rung, chớp, mù màu, cỡ chữ, bật-tắt), lưu theo tài khoản | | ✅ | | |
| Tay cầm | | | ✅ | |
| Cảm ứng (PWA / Capacitor) | | | | ✅ |
| Xoay camera theo bước 90° (CAM-Q-1) | | | | ? |

---

## 17. Câu hỏi mở
- [ ] **CAM-Q-1:** Có cho xoay camera theo bước 90° (D-09 "cân nhắc xoay sau")? Ảnh hưởng: minimap phải xoay theo, làm mờ vật che phức tạp hơn, map design phải đẹp ở 4 hướng.
- [ ] **CAM-Q-2:** Khóa mục tiêu mềm có nên tắt trong PvP (chỉ nhắm tay) để giữ kỹ năng cá nhân?
- [x] **CAM-Q-3:** *(Đã trả lời ở GD-31: phím **`N`** (`ActionId` `FLIGHT_TOGGLE`), tay cầm `LB` + `A` giữ 0,5s, nút cảm ứng riêng; camera zoom ra 30m khi bay.)* Ngự kiếm phi hành (GD-31) dùng phím nào (đề xuất giữ `Space` 0,5s khi ngoài chiến đấu, hoặc phím riêng `N`) và camera zoom ra bao nhiêu khi bay?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến (IntentFrame chung, camera không ảnh hưởng server, hỗ trợ ngắm chỉ ở client, vùng cảnh báo luôn thấy), thông số camera, **sơ đồ phím chuẩn** (giải quyết trùng `G` / `T` / `F` với GD-25), click-to-move, nhắm & khóa mục tiêu mềm, hỗ trợ ngắm theo thiết bị (giảm trong PvP), tay cầm, cảm ứng (joystick ảo, kéo để ngắm, smart cast, tự chọn mục tiêu), trợ năng, lưu cài đặt theo tài khoản, gamedata, nghiệm thu |
| v0.2 | 2026-10-25 | — | CAM-Q-3: phím bay `N` (GD-31); Trỏ telemetry chuẩn GD-20 |
| v0.3 | 2026-10-25 | — | Đợt rà nhất quán (AR-04): giá trị hex 4 bảng vùng cảnh báo `TGPAL_*` (CAM-R-061, VFX-R-014); màu đồng minh "lam trắng mờ" theo AR-01; ngưỡng `flashReduction` (≥ 50% độ sáng, ≤ 3 chớp/s, khóa `controls.json → accessibility`); CAM-R-064 độ đậm VFX người khác chỉ theo preset (VFX-Q-4); CAM-AC-016..018 |