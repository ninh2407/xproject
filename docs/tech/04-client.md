# TD-05 — Client Architecture

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-05 · Prefix ID: `CL` |
| Owner | Tech Lead (Client) + Tech Artist |
| Trạng thái | 🟡 Bản nháp v0.4 |
| Cần trước | Phase 0 (spike camera 2.5D, nhân vật GLB, WASD, netcode), Phase 1 (khung client đầy đủ) |
| Quyết định nền | D-01 (2.5D, logic 2D), D-09 (camera cố định), D-10 (WASD mặc định), D-19 (TS dùng chung `shared/sim`), D-22 (3D phong cách hóa + Babylon.js, đa nền tảng) |
| Liên quan | [TD-01 TDD](00-tdd.md) (§17b đa nền tảng) · [TD-02 Network](01-network.md) · [TD-07 Gamedata](06-gamedata.md) · [TD-12 World & Map](11-world-maps.md) · [TD-13 Map Format](12-map-editor.md) · [TD-14 Geometry](13-geom2d.md) · [GD-29 RNG](../design/05-rng-fairness.md) (`ClientGamedata`) · [GD-07 Kỹ năng](../design/13-skills.md) (§4 `ActionState`) · [GD-26 Vòng đời](../design/03-lifecycle.md) (màn tổng kết) · [GD-21 Open World](../design/22-open-world.md) · [GD-24 Camera](../design/25-camera-controls.md) · [AR-03 Animation List](../art/animation-list.md) (§3, §5) · [AR-04 VFX Guidelines](../art/vfx-guidelines.md) (§4.2, §5, §6.5, §9–§11) · [TD-11](10-performance.md) §3, §3.2 · PLAN §5.2, §5.8, §6.1 |

> Client là **lớp hiển thị và dự đoán**: không quyết định kết quả (TD-01 TA-R-002). Mọi logic mô phỏng dùng `@tienlo/sim` (TD-01 TA-R-007), mọi số liệu đọc từ `ClientGamedata` (TD-07, GD-29). Tài liệu này quy định cấu trúc mã, vòng đời, mô hình dữ liệu, render, hoạt ảnh, UI, tải asset, chất lượng đồ họa, kết nối lại, debug và kiểm thử của `client/` (`@tienlo/client`).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-05 | Mục |
|--------------|-----------------|-----|
| TD-01 §17b | Cách hiện thực lớp đầu vào (`IntentFrame`) và lớp nền tảng | §2.3, §2.4 |
| TD-02 | Nơi chạy dự đoán / đối chiếu / nội suy trong client | §4.3 |
| TD-12 / TD-13 | Tải trước, stream chunk, màn chuyển bản đồ | §3, §8 |
| GD-24 | Adapter cảm ứng / hỗ trợ ngắm thuộc lớp đầu vào | §2.3 |
| GD-17 UI/UX | Khung React, store, quy tắc i18n, bố cục co giãn | §7 |
| AR-01 / AR-04 | Ràng buộc shader, VFX tham số, lớp vẽ, ngân sách | §5, §9, §13 |
| AR-03 | State machine hoạt ảnh, blend, co giãn, chuỗi dự phòng, lớp thân trên / dưới | §6, §13 |

---

## 1. Mục tiêu & bất biến

- Tải lần đầu **< 15 MB** để vào game; 60 FPS trên GPU tích hợp ở mức Trung (PLAN §5.2).
- Cảm giác tức thì ở ping ≤ 150ms nhờ dự đoán (TD-02 §5).
- **Một codebase** cho web PC, mobile web / PWA, app Capacitor (TD-01 TA-R-160).
- Cấu trúc rõ ràng để AI sinh mã đúng chỗ: mỗi thư mục một trách nhiệm, phụ thuộc một chiều.

| Mã | Bất biến |
|----|----------|
| <a id="cl-r-001"></a>**CL-R-001** | Client **không bao giờ** gửi vị trí, sát thương hay kết quả; chỉ gửi ý định (TD-02 NW-R-001). Kết quả hiển thị (số sát thương, nhận đồ, chết) chỉ xuất hiện khi có message server. |
| <a id="cl-r-002"></a>**CL-R-002** | Mô phỏng (di chuyển, va chạm, hình đòn, đạn) gọi **`@tienlo/sim`**, không viết lại trong `client/`. Lint chặn import trực tiếp các thuật toán hình học ngoài `@tienlo/sim`. |
| <a id="cl-r-003"></a>**CL-R-003** | Client chỉ import kiểu **`ClientGamedata`** (GD-29 RF-R-043); không có đường dẫn nào tới `bundle.server`. |
| <a id="cl-r-004"></a>**CL-R-004** | Code game không gọi trực tiếp API trình duyệt / plugin native cho lưu trữ, thanh toán, thông báo, vòng đời app — đi qua lớp `platform/` (TD-01 TA-R-162). |
| <a id="cl-r-005"></a>**CL-R-005** | Code combat / mạng không biết thiết bị đầu vào; chỉ đọc `IntentFrame` (TD-01 TA-R-161). |
| <a id="cl-r-006"></a>**CL-R-006** | Phụ thuộc giữa thư mục **một chiều** theo §2.2; vi phạm bị ESLint (`import/no-restricted-paths`) chặn trong CI. |

---

## 2. Cấu trúc mã

### 2.1. Thư mục `client/src/`
```
client/src/
├── main.ts                    # điểm vào: tạo App, gắn React root + canvas
├── app/                       # vòng đời app, máy trạng thái màn hình (§3), DI container
│   ├── App.ts
│   ├── screenMachine.ts       # Boot → Login → CharacterSelect → MapLoading → InGame → Transfer → Ending
│   └── services.ts            # khởi tạo và tiêm các service (net, gamedata, platform...)
├── core/                      # tiện ích không phụ thuộc domain
│   ├── loop.ts                # vòng lặp khung hình: fixed-step mô phỏng 20Hz + render theo requestAnimationFrame
│   ├── time.ts                # serverOffset, serverTimeMs ước tính (TD-02 NW-R-012)
│   ├── events.ts              # GameEventBus có kiểu (§7.2)
│   ├── log.ts, assert.ts, pool.ts
├── platform/                  # TD-01 TA-R-162
│   ├── types.ts               # interface Storage, Purchase, Notifications, AppLifecycle, Share
│   ├── web/                   # IndexedDB, Service Worker, Page Visibility API
│   └── capacitor/             # plugin Capacitor tương ứng (nạp động khi chạy trong app)
├── input/                     # TD-01 TA-R-161
│   ├── IntentFrame.ts         # { seq?, move, aim, walk, actions[] }
│   ├── InputManager.ts        # chọn adapter hoạt động, gộp ý định mỗi khung
│   ├── keymap.ts              # sơ đồ phím mặc định + tùy biến (GD-24)
│   └── adapters/
│       ├── keyboardMouse.ts   # WASD + chuột (D-10), click-to-move (chuyển thành chuỗi move bằng navmesh)
│       ├── touch.ts           # joystick ảo, nút kỹ năng, hỗ trợ ngắm / tự chọn mục tiêu (GD-24)
│       └── gamepad.ts
├── net/                       # TD-02
│   ├── ApiClient.ts           # REST (@tienlo/contracts), JWT + refresh, X-Request-Id, traceparent
│   ├── RoomConnection.ts      # Colyseus client: join bằng ticket, reconnect, transfer
│   ├── messages.ts            # gửi / nhận message có kiểu (@tienlo/schema), kiểm tra zod khi DEV
│   ├── stateSync.ts           # ánh xạ State Colyseus → ECS (§4.4)
│   ├── inputSender.ts         # đóng gói C2S_Input 50ms, dư 4 khung (TD-02 §5.1)
│   └── ping.ts
├── gamedata/                  # nạp bundle.client theo version (TD-07 §10.1), cache IndexedDB
├── world/                     # TD-12, TD-13
│   ├── MapLoader.ts           # manifest, logic.client.json, navmesh r0.4 (click-to-move)
│   ├── ChunkStreamer.ts       # tải / hủy chunk 32×32m theo vị trí camera
│   ├── Preloader.ts           # S2C_PreloadHint → tải nền bản đồ kề
│   ├── fog.ts                 # bitmap fog (S2C_FogDelta) → texture minimap / bản đồ thế giới
│   ├── worldClock.ts          # dùng shared/sim/worldClock khi chưa có tin server (TD-12 WM-R-073)
│   └── weather.ts
├── render/
│   ├── Renderer.ts            # Engine / Scene Babylon, WebGL2 mặc định, thử WebGPU
│   ├── camera.ts              # ArcRotateCamera khóa góc 45–55°, zoom giới hạn (D-09)
│   ├── lighting.ts            # ánh sáng theo canh giờ, sương, mưa (§5.2)
│   ├── materials/             # toon, ink (thủy mặc), outline, terrain splat, foliage (gió)
│   ├── vfx/                   # VFX tham số AR-04 §6, §10: template × palette × overlay (GD-08 TF-R-071)
│   │   ├── resolveSkillVfx.ts # chọn { templateId, paletteId, overlayIds, flavor } (AR-04 VFX-R-055, GD-07 SK-R-083)
│   │   ├── templates/         # {templateId}.ts: VFX_BOLT, _SWEEP, _SMITE, _DASH, _NOVA, _FAMILIAR, _BLOOM, _AEGIS, _THRUST (+ VFX_BOSS_*)
│   │   ├── catalog.ts         # đọc vfx/templates, palettes, overlays, effects, bindings, params (ClientGamedata)
│   │   ├── layers.ts          # thứ tự lớp vẽ cố định (CL-R-069)
│   │   ├── vfxPool.ts         # pool theo mẫu trên core/pool.ts: làm ấm, trần, tái dùng (CL-R-069)
│   │   ├── vfxLod.ts          # LOD khoảng cách, ngoài khung nhìn, giảm khi đông (AR-04 §10.2–§10.3)
│   │   └── aestheticRng.ts    # PRNG thẩm mỹ seed = băm(casterId, serverStartMs, templateId) (AR-04 VFX-R-099)
│   ├── telegraph.ts           # vẽ vùng cảnh báo từ shapeOutline (TD-14 GEO-R-045)
│   ├── lod.ts, instancing.ts, occluderFade.ts
│   └── quality.ts             # preset thấp / trung / cao (§9)
├── entities/                  # ECS client (§4)
│   ├── components.ts
│   ├── world.ts               # tạo / hủy entity, truy vấn
│   └── systems/               # interpolationSystem, predictionSystem, animationSystem, renderSyncSystem, nameplateSystem, vfxSystem
├── combat/                    # dự đoán hành động (TD-02 NW-R-033), hiển thị số trúng, hitbox debug
├── anim/                      # bộ xương chung, AnimationGroup, state machine khớp ActionState (§6)
├── audio/                     # Babylon Audio Engine v2 / Howler (chọn ở AU-01 AU-Q-1; bọc sau interface AudioPort), quy tắc trộn AU-01 §6, nhạc theo bản đồ + canh giờ, SFX
├── features/                  # theo domain GD, mỗi thư mục: api.ts, store.ts, ui/*.tsx, handlers.ts
│   ├── inventory/  cultivation/  skills/  alchemy/  forging/  techniqueForge/
│   ├── encounter/  quest/  social/  sect/  danger/  lifecycle/  beasts/  pvp/  map/
├── ui/                        # khung React dùng chung
│   ├── root.tsx               # ReactRoot, router theo screenMachine
│   ├── layout/                # HUD, cửa sổ, bố cục PC / tablet / điện thoại
│   ├── components/            # Button, Tooltip, ChanceView (GD-29), ItemSlot, Modal, HoldToConfirm...
│   ├── store/                 # Zustand: session, character, settings, ui
│   └── i18n/                  # i18next, nạp i18n/{lang}.client.{version}.json
└── debug/                     # chỉ build DEV / cờ GM: hitbox F9, overlay mạng, bảng ECS, giả lập lag
```

### 2.2. Hướng phụ thuộc (CL-R-006)
```
app → features → ui, combat, world, render, entities, net, input, audio
features → gamedata, platform
combat, world, render, entities, anim → @tienlo/sim, gamedata, core
net → @tienlo/schema, @tienlo/contracts, core
input, platform, gamedata → core
@tienlo/sim, @tienlo/schema → (không phụ thuộc client)
```
- <a id="cl-r-010"></a>**CL-R-010** `render/` không import `net/` và ngược lại; dữ liệu đi qua ECS (§4). `ui/` không import Babylon; giao tiếp game qua store / event bus (§7.2).
- <a id="cl-r-011"></a>**CL-R-011** **Tính năng mới theo GD** đặt trong `features/{domain}/` với cùng bố cục `api.ts` (REST), `handlers.ts` (message S2C), `store.ts` (Zustand slice), `ui/` (React). AI sinh mã cho một GD chỉ tạo / sửa trong thư mục domain đó + đăng ký handler.

### 2.3. Lớp đầu vào
```ts
// input/IntentFrame.ts
export interface IntentFrame {
  move: [number, number];               // chuẩn hóa |v| ≤ 1
  aim: [number, number] | null;         // điểm nhắm trên mặt đất (mét); null = theo hướng mặt
  walk: boolean;
  actions: IntentAction[];              // lệnh phát sinh trong khung
}
export type IntentAction =
  | { kind: 'BASIC_ATTACK' } | { kind: 'HEAVY_START' } | { kind: 'HEAVY_RELEASE' }
  | { kind: 'CAST'; slot: SkillSlot; holdMs?: number }
  | { kind: 'DODGE'; dir: [number, number] } | { kind: 'BLOCK_START' } | { kind: 'BLOCK_END' }
  | { kind: 'INTERACT'; targetId?: number } | { kind: 'USE_QUICK_SLOT'; index: number }
  | { kind: 'BEAST_SUMMON' } | { kind: 'BEAST_COMMAND' } | { kind: 'CAPTURE_START'; targetId: number };
```
- <a id="cl-r-020"></a>**CL-R-020** Mỗi adapter cài `InputAdapter { enable(); disable(); poll(dt): Partial<IntentFrame> }`. `InputManager` chọn adapter theo thiết bị dùng gần nhất (đổi tự động khi chạm màn hình / nhấn phím / tay cầm), gộp thành một `IntentFrame` mỗi khung mô phỏng.
- <a id="cl-r-021"></a>**CL-R-021** `inputSender` đổi `IntentFrame` thành `C2S_Input` (20Hz) và lệnh C2S tương ứng (`C2S_CastSkill`, `C2S_Dodge`…) — bảng ánh xạ một nơi duy nhất (`net/inputSender.ts`).
- <a id="cl-r-022"></a>**CL-R-022** Hỗ trợ ngắm cho cảm ứng (GD-24) chạy **trong adapter**: chọn `aim` theo mục tiêu gần nhất trong hình nón phía trước / mục tiêu đang khóa; server không biết có hỗ trợ. Thông số hỗ trợ ngắm, sơ đồ phím, cảm ứng, tay cầm: [GD-24 §5–§8](../design/25-camera-controls.md); tham số camera ở `world/camera.json`, điều khiển ở `world/controls.json` (GD-24) — `world/client.json` chỉ giữ tham số không thuộc camera / điều khiển. Mất focus cửa sổ → thả mọi phím đang giữ.
- <a id="cl-r-023"></a>**CL-R-023** Bàn phím giữ phím bí pháp (`holdToCastSec`, GD-07 SK-R-110) và giữ xác nhận rủi ro chết (GD-29 RF-R-025) dùng chung component / tiện ích `HoldToConfirm`.

### 2.4. Lớp nền tảng
```ts
// platform/types.ts
export interface Platform {
  kind: 'web' | 'capacitor';
  storage: KeyValueStore & BlobCache;      // IndexedDB (web) / Filesystem + Preferences (capacitor)
  purchase: PurchaseProvider;              // web: chuyển trang thanh toán; capacitor: IAP — biên lai gửi API (TD-01 TA-R-164)
  notifications: NotificationProvider;
  lifecycle: AppLifecycle;                 // onBackground / onForeground → net reconnect (§10)
  share: ShareProvider;
  device: { touch: boolean; lowPowerHint: boolean; safeArea: Insets };
}
```
- <a id="cl-r-030"></a>**CL-R-030** `platform/index.ts` chọn bản `capacitor` khi `window.Capacitor?.isNativePlatform()`; plugin Capacitor **nạp động** để bản web không tải mã native.

---

## 3. Vòng đời app & máy trạng thái màn hình

```
Boot ──► Login ──► CharacterSelect ──► MapLoading ──► InGame ◄──► Transfer
  │        ▲            │   ▲                            │
  │        └── hết phiên ┘   └──── tạo nhân vật mới ◄─── Ending (GD-26 ENDING)
  └─► Maintenance / VersionMismatch (tải lại)
```

| Màn | Vào khi | Việc chính | Ra khi |
|-----|---------|-----------|--------|
| `Boot` | Mở app | Nạp cấu hình, kiểm tra WebGL2, `GET /v1/meta` (gamedataVersion, assetBase), nạp `bundle.client` + i18n (cache theo version), chọn preset chất lượng (§9) | Có refresh token hợp lệ → `CharacterSelect`; ngược lại `Login` |
| `Login` | Chưa đăng nhập | Form + REST auth | Thành công |
| `CharacterSelect` | Đăng nhập | Danh sách nhân vật (một `ALIVE`, D-12), tạo nhân vật (GD-27 roll thiên phú), Tàng Bảo Các / Di Tàng xem trước | `POST /v1/play/join` thành công → `MapLoading` |
| `MapLoading` | Có vé vào room | Tải manifest + logic bản đồ, chunk quanh điểm xuất hiện, navmesh, kết nối room (`joinById` + ticket), chờ `S2C_Welcome` + State đầu | State đầu đã áp vào ECS → `InGame` |
| `InGame` | — | Vòng lặp mô phỏng + render, HUD | `S2C_TransferTicket` → `Transfer`; `S2C_LifeEnded` → `Ending`; mất kết nối quá cửa sổ → `CharacterSelect` |
| `Transfer` | Có vé chuyển (TD-12 §5) | Màn mây thủy mặc, rời room cũ, chờ tải xong (tiến độ), vào room đích | Như `MapLoading` |
| `Ending` | `S2C_LifeEnded` / `lifeState = ENDING` | Màn tổng kết kiếp (GD-26 §5.8), chọn gửi Tàng Bảo Các / linh thú giữ lại / lời trăn trối, Bia Vạn Cổ | Xác nhận → `CharacterSelect` |

- <a id="cl-r-040"></a>**CL-R-040** Máy trạng thái màn hình là **một nguồn duy nhất** (`app/screenMachine.ts`); React router hiển thị theo trạng thái máy, không tự điều hướng.
- <a id="cl-r-041"></a>**CL-R-041** Rời `InGame` (chuyển bản đồ, ending) giải phóng: entity ECS, mesh chunk không dùng ở bản đồ đích, listener room; kiểm tra rò rỉ bằng bộ đếm tài nguyên ở `debug/` (§11).

---

## 4. Mô hình dữ liệu client

### 4.1. Lựa chọn ECS
- <a id="cl-r-050"></a>**CL-R-050** Dùng **bitECS** (MPL-2.0, PLAN §5.7.2): component dạng mảng số (SoA) hợp với dữ liệu nóng mỗi khung (vị trí, vận tốc, hướng, trạng thái hành động) cho ~200 thực thể hiển thị; dữ liệu ít đổi, kiểu chuỗi (tên, templateId, statusIcons) giữ ở **bảng phụ** `Map<eid, …>` trong `entities/world.ts`. Không đặt object Babylon trong component; liên kết qua `RenderRef` (eid → mesh handle).

### 4.2. Component
| Component | Trường | Nguồn |
|-----------|--------|-------|
| `NetId` | `serverId: u32` | State |
| `Kind` | `kind: u8` (PLAYER, MONSTER, NPC, SUMMON, LOOT, CORPSE, GATE…) | State |
| `Transform2D` | `x, z, facing` (mét, rad) | Nội suy / dự đoán |
| `NetSamples` | vòng 4 mẫu `{tick, x, z, facing}` | State |
| `Predicted` | cờ — thực thể của chính người chơi | Client |
| `Action` | `state: u8` (ActionState GD-07 §4), `startTick: u16` | State / dự đoán |
| `Health` | `hpPct: u8` (người khác) | State; số tuyệt đối ở store `character` |
| `Body` | `radius` (TD-14 GEO-R-020) | Gamedata |
| `RenderRef` | `handle: u32` | Client |
| `AnimRef` | `handle: u32` | Client |
| `Nameplate` | cờ hiển thị, màu PK (GD-32) | State |
| `Hidden` | `discovered: bool` | Server chỉ gửi khi đã phát hiện (TD-02 NW-R-042) |

### 4.3. Nguồn sự thật
| Dữ liệu | Nguồn sự thật | Client làm gì |
|---------|---------------|---------------|
| Vị trí bản thân | Server (`SelfState.lastInputSeq` + vị trí) | **Dự đoán** bằng `@tienlo/sim/movement`, đối chiếu theo TD-02 NW-R-032 |
| Vị trí thực thể khác | Server (State) | **Nội suy** trễ 100ms (TD-02 NW-R-023), ngoại suy ≤ 250ms |
| Trạng thái hành động bản thân | Server | Dự đoán hoạt ảnh + hồi chiêu; hủy khi `S2C_CastRejected` (TD-02 NW-R-033) |
| HP / MP / tài nguyên bản thân | Server (`SelfState`) | Hiển thị; không trừ trước |
| Sát thương, chết, rơi đồ | Server (message) | Chỉ hiện khi nhận (CL-R-001) |
| Túi đồ, tiền, tu vi, nhiệm vụ | API Service (REST) + message `S2C_*Delta` | Store Zustand; làm mới khi nhận sự kiện |
| Canh giờ / thời tiết | World Clock (`MapState.phase/weather`) | Áp vào lighting; tự tính khi mất tin (TD-12 WM-R-073) |

### 4.4. Ánh xạ State Colyseus → ECS
- <a id="cl-r-051"></a>**CL-R-051** `net/stateSync.ts` đăng ký callback `onAdd / onChange / onRemove` của `MapState.entities`: `onAdd` → tạo entity, `NetId`, `Kind`, bảng phụ; mỗi `onChange` vị trí → **đẩy mẫu** vào `NetSamples` (giải lượng tử: cm → m, `facing` u8 → rad bằng bảng tra TD-14 GEO-R-011), **không** ghi thẳng `Transform2D`; `onRemove` → đánh dấu hủy, hệ render làm mờ 0,2s rồi xóa.
- <a id="cl-r-052"></a>**CL-R-052** Nếu dùng phương án dự phòng `S2C_Snapshot` (TD-02 NW-R-021), cùng hàm `applyEntityUpdate(serverId, fields)` được gọi — ECS không biết nguồn là State hay Snapshot.

### 4.5. Vòng lặp khung hình
```
requestAnimationFrame(now):
  acc += dt
  while acc ≥ 50ms:                      # fixed-step 20Hz, khớp tick server
     input.poll → IntentFrame
     predictionSystem (bản thân, @tienlo/sim) ; inputSender.queue
     acc −= 50ms
  interpolationSystem(renderTime = serverTimeEst − 100ms)
  animationSystem ; vfxSystem ; renderSyncSystem (Transform2D → mesh, y từ heightmap)
  scene.render()
```
- <a id="cl-r-053"></a>**CL-R-053** Render nội suy giữa bước mô phỏng trước và sau cho bản thân (`alpha = acc / 50ms`) để mượt ở 60–144Hz.

---

## 5. Render

### 5.1. Camera
- <a id="cl-r-060"></a>**CL-R-060** `ArcRotateCamera`: thông số (nghiêng 45–55°, mặc định 50°; hướng cố định D-09; zoom 14–26m; `followLerp`; vùng nhìn ~40×30m) theo **GD-24 §4**, đọc từ `world/camera.json` (GD-24 §11) — TD-05 không giữ bản sao số camera. Vật che giữa camera và người chơi làm mờ (`occluderFade.ts`, ray 2D trên lớp va chạm `sight` + bounding box mesh).

### 5.2. Ánh sáng, ngày / đêm, thời tiết
- <a id="cl-r-061"></a>**CL-R-061** Một `DirectionalLight` + `HemisphericLight`; màu / cường độ / hướng nội suy theo `gameMinutes` từ đường cong trong gamedata (`world/lighting_curves`); không nhảy khi đổi pha (GD-21). Đêm không tối tới mức mất khả năng đọc gameplay (GD-21 §3.8).
- <a id="cl-r-062"></a>**CL-R-062** Thời tiết: mưa / tuyết là particle GPU giới hạn theo preset; sương là scene fog + post-process nhẹ; Linh vũ có hạt phát sáng riêng.

### 5.3. Chất liệu & phong cách (PLAN §6.1)
| Chất liệu | Dùng cho | Ghi chú |
|-----------|----------|---------|
| `ToonMaterial` (Node Material) | Nhân vật, quái, prop | 2–3 dải sáng (ramp), viền rim nhẹ, nhận atlas gradient hoặc texture vẽ tay |
| `OutlinePass` | Nhân vật, NPC, quái tinh anh / boss | Viền dày theo hạng; tắt ở preset Thấp |
| `InkWashPost` (tùy chọn) | Toàn cảnh / bầu trời / màn chuyển | Post-process thủy mặc: viền loang, giấy, mây; bật theo art direction |
| `TerrainSplat` | Mặt đất | 4 lớp texture + decal; mặt đất chiếm phần lớn màn hình (PLAN §6.1) |
| `Foliage` | Cỏ, lá | Thin instance + gió bằng vertex shader |

- <a id="cl-r-063"></a>**CL-R-063** Shader viết bằng Node Material / ShaderMaterial trong `render/materials/`; mỗi chất liệu có biến thể theo preset (§9) qua `#define`. Không dùng PBR nặng cho nhân vật.

### 5.4. Hình học, instancing, chunk
- <a id="cl-r-064"></a>**CL-R-064** Môi trường tĩnh theo chunk 32×32m (TD-13): mỗi chunk là một GLB đã gộp mesh; `ChunkStreamer` giữ chunk trong bán kính `chunkLoadRadius` (2 chunk quanh camera) và giải phóng ngoài `chunkUnloadRadius` (3 chunk). Prop lặp dùng **thin instances**.
- <a id="cl-r-065"></a>**CL-R-065** Nhân vật / quái cùng model dùng `InstancedMesh` khi không có hoạt ảnh riêng biệt; có hoạt ảnh thì dùng clone dùng chung skeleton + animation baking (vertex animation texture) cho quái thường ở preset Thấp.
- <a id="cl-r-066"></a>**CL-R-066** **Ngân sách** (PLAN §5.2): draw call < 200 ở thành trấn đông, nhân vật ≤ 5k tam giác, quái ≤ 3k, prop 500–1.500; ~100 nhân vật trên màn hình; người chơi khác ở xa giảm VFX. Trần tổng tam giác ≤ 600k ở preset Trung + LOD nhân vật bắt buộc: [TD-11 PERF-R-020](10-performance.md).

### 5.5. VFX và vùng cảnh báo
- <a id="cl-r-067"></a>**CL-R-067** VFX tham số (GD-08 TF-R-071, [AR-04 §6](../art/vfx-guidelines.md)): `VfxTemplate` (9 mẫu theo `Shape.type`, AR-04 §6.2) × `Palette` (`VPAL_*`, AR-04 §3.1) × `Overlay` (`VFXO_*`, AR-04 §6.4). Mọi kỹ năng — tác giả viết, chế tạo, quái — đi qua `render/vfx/resolveSkillVfx.ts` theo đúng thứ tự AR-04 VFX-R-055 / GD-07 SK-R-083 (ghi đè `SkillDef.vfx` → thành phần `CSK_*` lấy từ `GET /v1/crafted-techniques/{id}` có cache (GD-08 TF-R-072) → suy từ `shape.type`); bảng màu `INHERIT` của người khác đọc `PlayerPublic.elementAffinity` (TD-02 NW-R-025). Template là ParticleSystem / NodeParticle + tối đa 1 mesh, chung atlas VFX; bảng màu đổi bằng uniform / gradient texture, không tạo material mới mỗi lần thi triển (AR-04 VFX-R-047). Dữ liệu ở `shared/gamedata/vfx/*.json` (AR-04 §11.2 — domain `vfx` chờ TD-07 đăng ký); tham số pool / LOD / giảm khi đông ở `vfx/params.json`, trần hạt theo preset ở `world/client.json → vfxBudget` (§13, TD-11 §3.2).
- <a id="cl-r-068"></a>**CL-R-068** Vùng cảnh báo vẽ bằng `shapeOutline` của `@tienlo/sim` (TD-14 GEO-R-045) làm decal mặt đất, đếm ngược theo `serverStartMs` quy đổi không trễ nội suy (TD-02 NW-R-024); vẽ theo AR-04 §5 (`VFX_TG_ENEMY` / `VFX_TG_UNBLOCKABLE` / `VFX_TG_ALLY` / `VFX_TG_AIM`), màu từ bảng `TGPAL_*` của `telegraphPalette` (GD-24 CAM-R-061), độ đậm `telegraphOpacity` ≥ 0,6; mọi vùng cảnh báo trong khung gộp **một mesh động / 1 draw call**, tối đa `vfx/params.json → telegraph.maxActive` (TD-11 PERF-R-034).
- <a id="cl-r-069"></a>**CL-R-069** **Thứ tự lớp vẽ cố định** (`render/vfx/layers.ts`, AR-04 §4.2 VFX-R-020), dưới → trên: (1) địa hình, decal trang trí · (2) VFX mặt đất kỹ năng / môi trường · (3) thực thể, VFX thân, đạn, VFX không khí · (4) thời tiết, hạt không khí — các lớp 1–4 **có** kiểm tra độ sâu; (5) vùng cảnh báo (kẻ địch vẽ sau đồng minh), vòng chân nhân vật mình, viền vật được quét — **không** kiểm tra độ sâu, unlit, không chịu bloom / sương / thủy mặc / ánh sáng ngày đêm (CAM-R-006, VFX-R-021); (6) nameplate, số sát thương (Babylon GUI, CL-R-082). **Pool, LOD, giảm khi đông** theo AR-04 §10: không tạo / hủy `ParticleSystem` trong trận, làm ấm ở `MapLoading` (TD-11 PERF-R-036); LOD theo khoảng cách 2D từ tâm camera (`NEAR` / `MID` / `FAR`), ngoài khung nhìn dừng cập nhật hạt nhưng giữ đồng hồ pha; khi sắp vượt trần hạt cắt theo thứ tự ưu tiên VFX-R-097 — vùng cảnh báo và đạn / chiêu thù địch nhắm vào mình / tổ đội **không bao giờ** bị loại (tối thiểu hình dạng tối giản, VFX-R-026).

---

## 6. Hoạt ảnh

- <a id="cl-r-070"></a>**CL-R-070** **Bộ xương chung** cho mọi nhân vật hình người (người chơi, NPC, tàn hồn); quái theo nhóm bộ xương (`SkeletonGroup`, GD-12). Thư viện hoạt ảnh tách khỏi model: một GLB hoạt ảnh mỗi nhóm bộ xương, gắn lại bằng `AnimationGroup` retarget theo tên xương chuẩn ([AR-02 §4](../art/asset-guidelines.md)).
- <a id="cl-r-071"></a>**CL-R-071** State machine hoạt ảnh **khớp `ActionState` GD-07 §4**: mỗi trạng thái → clip theo bảng ánh xạ [AR-03 §3](../art/animation-list.md) (hoặc blend tree cho `MOVING` theo tốc độ / hướng tương đối); số bước `ATTACK_n` theo `len(WeaponDef.basicCombo)` (GD-07 SK-R-054). Thời điểm bắt đầu theo `actionStartTick` (bản thân: dự đoán), tốc độ phát chỉnh để **khung va chạm của clip trùng `windupSec + hit.atSec`** của `SkillDef` (marker `hit` / `hit_1..n`; quy ước marker: [AR-02 §5.1](../art/asset-guidelines.md), AR-03 §2.3); công thức co giãn hai đoạn `r_pre` / `r_post` theo AR-03 ANM-R-091, mỗi hệ số kẹp trong [`anim.rateMin`, `anim.rateMax`] (§13). Clip khóa thời lượng (phản ứng, né, ngự kiếm) co giãn theo tham số gamedata tương ứng (`stats/params.json → reactionSec`, `dodge.durationSec`, GD-06; AR-03 ANM-R-021).
- <a id="cl-r-072"></a>**CL-R-072** Cửa sổ hủy (`cancelAfterSec`, `nextInputWindowSec`, GD-07) lấy từ `SkillDef` / `WeaponDef`, không từ clip; hoạt ảnh bị cắt bằng blend `anim.cancelBlendSec` (0,08s) khi chuyển trạng thái sớm. Thời gian blend các chuyển tiếp khác đọc từ `anim.blend` (§13, giá trị theo bảng gợi ý AR-03 §5.1); di chuyển khi đang đả tọa cắt `MEDITATE_LOOP` bằng `cancelBlendSec` (ưu tiên phản hồi, PERF-R-040), `MEDITATE_EXIT` chỉ phát khi dừng đả tọa không do di chuyển.
- <a id="cl-r-073"></a>**CL-R-073** Phản ứng (`STAGGERED`, `KNOCKED_BACK`, `KNOCKED_DOWN`) và `DOWNED`, `DORMANT_GUARD`, `CHANNELING` có clip riêng; thiếu clip → **chuỗi dự phòng** của AR-03 ANM-R-092 (không bao giờ T-pose): `ANM_HUMANOID_{W}_{A}` → `ANM_HUMANOID_{A}` (biến thể gốc) → bảng dự phòng AR-03 §5.4 (vd. `DOWNED_*` → `KNOCKDOWN` giữ frame cuối; `KNOCKBACK` / `KNOCKDOWN` / `GETUP` / `POISE_BREAK` / `STUN_LOOP` → `HIT_HEAVY` giữ frame cuối; `CAST_*` → `{W}_ATTACK_1`) → `IDLE` của nhóm. Chuỗi được sinh sẵn vào `tools/art/anim-list.json → fallback` và client chỉ đọc catalogue, không tự suy từ tên clip (ANM-R-012).
- <a id="cl-r-074"></a>**CL-R-074** **Clip riêng boss:** ánh xạ `skillId` → clip đọc từ catalogue `tools/art/anim-list.json → skillClips` (AR-03 ANM-R-063, chốt ANM-Q-10 theo mặc định: không thêm trường hoạt ảnh vào `SkillDef` / `MonsterSkillRef`); không có ánh xạ → clip nhóm theo `Shape` của chiêu (ANM-R-062). Chiêu cảnh báo dài cần tốc độ < `anim.rateMin` dùng cặp clip `{CHIÊU}_CHARGE` (lặp) + `{CHIÊU}_RELEASE` (ANM-R-070). Boss đổi pha phát `{MODEL_KEY}_PHASE_TRANSITION` trong lúc chạy `onEnter`.
- <a id="cl-r-075"></a>**CL-R-075** **Lớp thân trên / thân dưới** (mask từ xương `spine_01`, AR-02 §4): (a) `FLINCH` → `HIT_LIGHT` phát **additive** trên clip hiện tại, chỉ thân trên, không đổi `ActionState` (ANM-R-090); (b) khi đang `BASIC_ATTACK`, `HEAVY_CHARGE`, `RECOVERY`, `BLOCKING` mà tốc độ > 0 (đi chậm 30% / 40%, GD-07 §4): thân trên = clip hành động, thân dưới = `{W}_WALK` theo tốc độ — tránh trượt chân (chốt ANM-Q-6 theo mặc định). Blend tree `MOVING` 1D theo tốc độ thực, tốc độ phát `RUN` = `|v|` / tốc độ tham chiếu lưu trong catalogue (AR-03 §5.2).
- <a id="cl-r-076"></a>**CL-R-076** **Gói GLB hoạt ảnh:** một GLB mỗi `SkeletonGroup` (CL-R-070, `art/animations/{SKELETON}.glb`), tải khi bản đồ có thực thể dùng nhóm đó; kích thước mỗi file theo TD-11 PERF-R-037 (vượt thì tách theo AR-03 ANM-Q-3, phải sửa CL-R-070 trước).

---

## 7. UI

### 7.1. Cấu trúc
- React 18+ render vào `#ui-root` phủ trên canvas; Zustand cho trạng thái; i18next (ICU) cho văn bản.
- Store chia slice: `session` (tài khoản, token), `character` (SelfState, ví, cảnh giới, thọ nguyên — dữ liệu riêng tư), `inventory`, `skills`, `social`, `ui` (cửa sổ mở, tooltip), `settings` (phím, chất lượng, âm thanh, ngôn ngữ).
- Mỗi `features/{domain}/ui` chứa cửa sổ của domain đó; `ui/layout/Hud.tsx` ghép các phần HUD.

### 7.2. Giao tiếp UI ↔ game
- <a id="cl-r-080"></a>**CL-R-080** **Game → UI:** handler message / hệ ECS cập nhật **store Zustand** (dữ liệu bền như HP, túi đồ) hoặc phát **`GameEventBus`** (sự kiện tức thời: số sát thương, thông báo, cue cơ duyên). UI chỉ đọc store và nghe bus.
- <a id="cl-r-081"></a>**CL-R-081** **UI → game:** UI gọi **action** có kiểu (`features/*/api.ts` cho REST, `game.commands.*` cho lệnh realtime), không chạm ECS / Babylon. Lệnh realtime đi vào `InputManager` như một `IntentAction` (vd. bấm nút kỹ năng trên HUD = `CAST`).
- <a id="cl-r-082"></a>**CL-R-082** Nameplate, thanh máu trên đầu, số sát thương bay: dùng **Babylon GUI** / sprite trong không gian 3D (hiệu năng), không dùng React DOM.

### 7.3. Bố cục co giãn
- <a id="cl-r-083"></a>**CL-R-083** Điểm ngắt: **PC** ≥ 1280px, **tablet** 768–1279px, **điện thoại** < 768px hoặc `device.touch` ở hướng ngang. Bố cục điện thoại: HUD thu gọn, joystick trái, cụm nút kỹ năng phải, vùng chạm ≥ 44px, tôn trọng `safeArea` (TD-01 TA-R-163). Tooltip rê chuột → chạm giữ trên thiết bị cảm ứng.

### 7.4. i18n
- <a id="cl-r-084"></a>**CL-R-084** Mọi chuỗi hiển thị qua `t(key, params)`; key theo TD-07 §6; số liệu trong mô tả lấy từ gamedata khi hiển thị (DT-R-073). ESLint chặn chuỗi tiếng Việt / Anh viết cứng trong JSX (trừ `debug/`). Lỗi API hiển thị bằng `messageKey` (TD-01 TA-R-044).

---

## 8. Tải asset & dữ liệu

| Loại | Nguồn | Cache | Khi nào tải |
|------|-------|-------|-------------|
| Mã client (JS) | CDN, tên file có hash (Vite) | HTTP cache + Service Worker | Boot; tách gói theo màn (`Login`, `InGame`, các cửa sổ lớn) |
| `bundle.client.{version}.json` + `i18n/{lang}.client.{version}.json` | CDN | IndexedDB theo version | Boot (lần đầu hoặc version đổi) |
| `manifest.json` bản đồ | CDN | IndexedDB theo `assetHash` | Trước khi vào bản đồ / tải trước |
| Chunk GLB, texture KTX2 | CDN | IndexedDB / Cache Storage theo hash | Theo vị trí camera (§5.4) |
| Model nhân vật / quái / hoạt ảnh | CDN | Như trên | Lần đầu xuất hiện thực thể; tải trước loại phổ biến của bản đồ (theo `spawnAreas`) |
| Âm thanh | CDN | Như trên | Nhạc theo bản đồ (stream), SFX nhóm theo bản đồ |

- <a id="cl-r-090"></a>**CL-R-090** **Ngân sách tải lần đầu < 15 MB** tới khi vào bản đồ đầu tiên: mã lõi ≤ 3 MB (gzip), gamedata + i18n ≤ 1,5 MB, bản đồ đầu + model cần ≤ 10 MB. CI đo kích thước gói và chặn khi vượt.
- <a id="cl-r-091"></a>**CL-R-091** **Bundle con** (TD-07 DT-R-121) cho gamedata theo vực: chỉ bắt buộc ở Beta 2 khi có Vực 2; giao diện `gamedata.require(id)` không đổi.
- <a id="cl-r-092"></a>**CL-R-092** Asset thiếu / lỗi tải → hiển thị placeholder (khối màu theo kích thước thân) và thử lại nền; không chặn chơi.
- <a id="cl-r-093"></a>**CL-R-093** Xóa cache cũ theo LRU khi vượt `cacheQuotaMb` (web: 500 MB; mobile: 300 MB); gamedata version cũ xóa ngay khi version mới nạp xong.

---

## 9. Mức chất lượng đồ họa (TD-01 TA-R-163)

| Thiết lập | Thấp | Trung (mặc định PC tích hợp) | Cao |
|-----------|------|------------------------------|-----|
| Tỷ lệ phân giải render | 0,7 (mobile: 0,6) | 1,0 | 1,0 (hỗ trợ DPR tới 2) |
| Bóng đổ | Blob dưới chân | Shadow map 1024, chỉ nhân vật | Shadow map 2048, nhân vật + prop lớn |
| Viền (`OutlinePass`) | Tắt (chỉ boss) | Nhân vật + quái | Tất cả |
| `InkWashPost` / bloom | Tắt | Bloom nhẹ | Bloom + thủy mặc (nếu bật art direction) |
| Mật độ cỏ / prop rải | 30% | 70% | 100% |
| Hạt VFX tối đa / hiệu ứng | 30% | 70% | 100% |
| VFX người chơi khác | Chỉ hình dạng tối giản | Giảm 50% | Đầy đủ |
| Hoạt ảnh quái thường | Vertex animation texture, 15 FPS ở xa | Skeleton, 30 FPS ở xa | Skeleton đầy đủ |
| Bán kính chunk | 1 | 2 | 2 |
| Particle thời tiết | 20% | 60% | 100% |

- <a id="cl-r-100"></a>**CL-R-100** Chọn preset tự động ở `Boot` bằng bài đo 3s (render cảnh mẫu), lưu vào `settings`; **tự hạ một mức** nếu FPS trung bình < 45 trong 30s (PC) / < 28 (mobile), báo người chơi; người chơi có thể ghi đè.

---

## 10. Lỗi & kết nối lại

- <a id="cl-r-110"></a>**CL-R-110** Mất kết nối WebSocket (TD-02 NW-R-071) → overlay "Đang kết nối lại…", thử `reconnect` của Colyseus với backoff 0,5 → 1 → 2 → 4s trong cửa sổ 60s (TD-02 NW-R-072); thành công → xóa bộ đệm dự đoán, đặt `seq` > `lastInputSeq`, áp State đầy đủ (NW-R-073). Quá 60s → về `CharacterSelect` với thông báo.
- <a id="cl-r-111"></a>**CL-R-111** `platform.lifecycle.onBackground` (app mobile xuống nền / tab ẩn): ngừng render, giữ kết nối tối đa 10s rồi chủ động đóng; `onForeground` → luồng kết nối lại như trên. Ở hiểm địa hiển thị cảnh báo trước "Rời ứng dụng sẽ vào trạng thái quy tức" (GD-28).
- <a id="cl-r-112"></a>**CL-R-112** Lỗi REST theo TD-01 §6.3: `401` → làm mới token một lần rồi về `Login`; `409 CONTEXT_STALE` → thử lại sau 0,5s (TD-01 TA-R-091); `409 CHANCE_CHANGED` → hiển thị `ChanceView` mới (GD-29); `429` → chờ theo header; `5xx` / timeout → thử lại tối đa 3 lần với cùng khóa idempotency (TD-01 TA-R-054).
- <a id="cl-r-113"></a>**CL-R-113** `gamedataVersion` của `S2C_Welcome` khác bản đang nạp → tải lại bundle trước khi vào room (TD-02 NW-AC-015).
- <a id="cl-r-114"></a>**CL-R-114** Lỗi không bắt được trong client gửi về endpoint thu thập lỗi (kèm `requestId`, phiên bản, preset, thiết bị) — không gửi dữ liệu cá nhân (TD-03 DB-R-100).

---

## 11. Công cụ debug (chỉ build DEV hoặc tài khoản GM)

| Công cụ | Phím | Nội dung |
|---------|------|----------|
| Hitbox | `F9` | Hình đòn client tính (xanh) chồng hình server gửi `S2C_DebugHit` (đỏ) — TD-14 GEO-R-092; thân thực thể, lớp va chạm, navmesh |
| Overlay mạng | `F10` | RTT, `serverOffset`, mất gói, kB/s lên / xuống, `lastInputSeq` vs dự đoán, sai lệch đối chiếu, số thực thể trong AOI |
| Hiệu năng | `F11` | FPS, thời gian khung, draw call, tam giác, bộ nhớ texture, số chunk / entity / mesh (phát hiện rò rỉ CL-R-041) |
| Giả lập mạng | bảng DEV | Thêm trễ / dao động / mất gói phía client |
| Bảng ECS | bảng DEV | Danh sách entity, component, theo dõi một entity |
| Thời gian thế giới | bảng DEV | Ép canh giờ / thời tiết hiển thị (chỉ client) |

- <a id="cl-r-120"></a>**CL-R-120** Mã `debug/` bị loại khỏi build production bằng cờ `import.meta.env.DEV` + tree-shaking; tài khoản GM chỉ mở được Hitbox / Overlay mạng ở production (không giả lập, không ép thời gian).

---

## 12. Kiểm thử

| Lớp | Công cụ | Phạm vi |
|-----|---------|---------|
| Đơn vị | Vitest (jsdom khi cần) | `input/` (adapter → IntentFrame), `net/stateSync`, `inputSender`, store Zustand, reducer UI, hàm tiện ích |
| Dự đoán / đối chiếu | Vitest + `@tienlo/sim` | Kịch bản input + phản hồi server giả lập trễ / mất gói → sai lệch trong ngưỡng TD-02 NW-R-032 |
| Thành phần UI | Vitest + Testing Library | Cửa sổ theo domain, `ChanceView`, `HoldToConfirm`, i18n không thiếu key |
| E2E trình duyệt | Playwright (Chromium, Firefox, WebKit) | Boot → login → vào bản đồ test → di chuyển → dùng kỹ năng → chuyển bản đồ; chạy với Game Server + API trong docker-compose (TD-01 §16) |
| Tất định chéo trình duyệt | Playwright + `@tienlo/sim` | Kịch bản 10.000 tick, băm vị trí giống nhau trên mọi engine (TD-14 GEO-R-091) |
| Hình ảnh | Playwright screenshot so sánh | Màn hình chính ở 3 preset; ngưỡng sai khác nhỏ |
| Hiệu năng | Playwright + bài đo cố định | FPS / draw call cảnh thành trấn 100 bot trên máy tham chiếu; kích thước gói (CL-R-090) |

- <a id="cl-r-130"></a>**CL-R-130** Mỗi tiêu chí `-AC-` của GD có phần client (UI hiển thị, phản hồi) có ít nhất một test Vitest hoặc Playwright, tên chứa mã (docs/README.md §3).

---

## 13. Tham số

```jsonc
// shared/gamedata/world/client.json (đọc qua ClientGamedata)
{
  // camera: không đặt ở đây — nguồn duy nhất world/camera.json (GD-24 §11, CL-R-060)
  "chunks": { "loadRadius": 2, "unloadRadius": 3 },
  "anim": {
    "cancelBlendSec": 0.08, "despawnFadeSec": 0.2,
    "blend": { "actionStartSec": 0.05, "comboNextSec": 0.05, "recoveryExitSec": 0.15, "idleCombatSwapSec": 0.3,
               "toReactionSec": 0.05, "toDeathSec": 0.1, "loopEnterSec": 0, "meditateExitSec": 0.2,
               "flightSec": 0.1, "flightFallSec": 0.05 },          // AR-03 §5.1 (CL-R-072)
    "rateMin": 0.6, "rateMax": 1.6                                    // kẹp hệ số co giãn (CL-R-071); art:check checks.rateMin/rateMax dùng cùng giá trị
  },
  "vfxBudget": {                                                      // sao số TD-11 §2.1, §3, TD-05 §9 — mảng theo preset [Thấp, Trung, Cao]; TD-11 là nguồn số
    "concurrentParticles": [1500, 4000, 8000],
    "skillParticlesHigh": 300, "skillParticlePct": [0.3, 0.7, 1.0],
    "otherPlayersParticlePct": [0, 0.5, 1.0],                          // 0 = chỉ hình dạng tối giản
    "weatherParticlePct": [0.2, 0.6, 1.0],
    "skillMeshTrisMax": 500, "lifetimeSecMax": 3, "atlasMaxPx": 1024
  },
  "quality": { "autoDowngradeFpsPc": 45, "autoDowngradeFpsMobile": 28, "autoDowngradeWindowSec": 30, "benchSec": 3 },
  "cache": { "quotaMbWeb": 500, "quotaMbMobile": 300 },
  "reconnect": { "backoffMs": [500, 1000, 2000, 4000], "backgroundKeepAliveSec": 10 },
  "ui": { "breakpoints": { "tablet": 768, "pc": 1280 }, "minTouchTargetPx": 44 },
  "bundleBudgetMb": { "firstLoad": 15, "coreJsGzip": 3, "gamedataI18n": 1.5 }
}
```

---

## 14. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="cl-ac-001"></a>CL-AC-001 | Client sạch cache, mạng 20 Mbps | Mở game tới khi điều khiển được ở `V1_F01` | Tổng tải ≤ 15 MB; thời gian ≤ 20s |
| CL-AC-002 | Laptop GPU tích hợp tham chiếu, preset Trung | Thành trấn 100 bot | ≥ 60 FPS trung bình, draw call < 200 |
| CL-AC-003 | Adapter cảm ứng giả lập | Di chuyển + dùng kỹ năng | Server nhận `C2S_Input` / `C2S_CastSkill` cùng định dạng như bàn phím-chuột (TA-AC-022) |
| CL-AC-004 | Module trong `ui/` import `@babylonjs/core` | CI lint | Fail (CL-R-010) |
| CL-AC-005 | Code client import `ServerGamedata` | Biên dịch | Lỗi kiểu (CL-R-003) |
| CL-AC-006 | Nhấn kỹ năng đang hồi chiêu phía server | Nhận `S2C_CastRejected` | Hoạt ảnh dự đoán bị hủy trong ≤ 1 khung, hồi chiêu khôi phục, hiện lý do |
| CL-AC-007 | Lag giả lập 150ms ± 20ms | Di chuyển 60s | Không giật > 1,5m; sai lệch đứng yên < 0,05m (NW-AC-001) |
| CL-AC-008 | Chuyển F01 → HUB (đã tải trước) | Chạy qua biên | Màn chuyển ≤ 2s; không rò rỉ: số mesh / entity sau 20 lần chuyển qua lại không tăng |
| CL-AC-009 | FPS trung bình 38 trong 30s ở preset Cao (PC) | Tự hạ | Chuyển Trung, báo người chơi |
| CL-AC-010 | App Capacitor xuống nền 30s | Quay lại | Kết nối lại cùng room, trạng thái đúng |
| CL-AC-011 | Mất kết nối 70s | Kết nối lại | Về `CharacterSelect` với thông báo |
| CL-AC-012 | Clip đánh chém có event `hit` ở 0,3s, `SkillDef` hit tại 0,45s | Phát | Tốc độ clip điều chỉnh để event trùng 0,45s |
| CL-AC-013 | Chuỗi tiếng Việt viết cứng trong JSX | CI lint | Fail (CL-R-084) |
| CL-AC-014 | Build production | Tìm mã `debug/` trong bundle | Không có (trừ Hitbox / Overlay mạng cho GM) |
| CL-AC-015 | Kịch bản tất định 10.000 tick | Chromium / Firefox / WebKit | Băm vị trí giống hệt |
| <a id="cl-ac-016"></a>CL-AC-016 | Quái thi triển `GROUND_CIRCLE` trong vùng `VFX_SMITE` Hỏa đang `LINGER` của đồng đội, nhân vật đứng sau nhà | Render | Vùng cảnh báo ở lớp 5, không kiểm độ sâu, không bị VFX / nhà che (CL-R-069, AR-04 VFX-AC-001) |
| <a id="cl-ac-017"></a>CL-AC-017 | `SkillDef` cần `r_pre` = 0,4 với clip theo `Shape` | Phát | Hệ số kẹp về `anim.rateMin` 0,6; marker `hit` lệch được ghi cảnh báo DEV (CL-R-071) |
| <a id="cl-ac-018"></a>CL-AC-018 | Nhân vật vào `DOWNED`, GLB không có `DOWNED_*` | Phát | Dùng `KNOCKDOWN` giữ frame cuối; không T-pose (CL-R-073, AR-03 ANM-AC-018) |
| <a id="cl-ac-019"></a>CL-AC-019 | Chiêu boss không có mục trong `anim-list.json → skillClips` | Phát | Dùng clip nhóm theo `Shape` (CL-R-074) |
| <a id="cl-ac-020"></a>CL-AC-020 | Trúng `FLINCH` khi đang `ATTACK_2` và đang đi chậm | Phát | `HIT_LIGHT` additive thân trên, `ATTACK_2` tiếp tục, thân dưới `WALK`, `ActionState` không đổi (CL-R-075) |
| <a id="cl-ac-021"></a>CL-AC-021 | `world/client.json → vfxBudget.concurrentParticles[1]` = 4.000, preset Trung | Tổng hạt sắp vượt trần | Cắt theo thứ tự VFX-R-097; vùng cảnh báo và đạn thù địch vẫn hiện (CL-R-069) |

---

## 15. Câu hỏi mở
- [ ] **CL-Q-1:** WebGPU làm mặc định khi trình duyệt hỗ trợ (hiệu năng tốt hơn) hay giữ WebGL2 mặc định tới khi WebGPU phổ biến trên mobile? Đề xuất: WebGL2 mặc định, WebGPU thử nghiệm qua cờ.
- [ ] **CL-Q-2:** `InkWashPost` (thủy mặc) bật cho toàn cảnh hay chỉ màn chuyển / cutscene? Chốt bằng mẫu Art Bible (AR-01, PLAN Q13).
- [ ] **CL-Q-3:** Có dùng vertex animation texture cho mọi quái thường ngay từ đầu (đơn giản hóa pipeline) hay chỉ ở preset Thấp?
- [ ] **CL-Q-4:** React 18 hay chuyển SolidJS cho HUD (cập nhật nhiều mỗi giây) nếu đo thấy React tốn CPU? Đề xuất: React + giới hạn tần suất cập nhật store HUD (10Hz).

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến, cấu trúc `client/src/` theo trách nhiệm + hướng phụ thuộc, lớp đầu vào (`IntentFrame`, adapter) và lớp nền tảng, máy trạng thái màn hình, ECS bitECS + nguồn sự thật + ánh xạ State → ECS, vòng lặp fixed-step, render (camera, ánh sáng theo canh giờ, toon / thủy mặc, chunk, instancing, ngân sách, VFX tham số, vùng cảnh báo), hoạt ảnh khớp `ActionState`, UI React + store + bus + co giãn + i18n, tải asset & cache, 3 preset chất lượng, kết nối lại / lỗi, debug, kiểm thử, tham số, nghiệm thu |
| v0.2 | 2026-10-25 | — | Trỏ GD-24 (điều khiển, camera.json / controls.json) và TD-11 (trần tam giác) |
| v0.3 | 2026-10-25 | — | Trỏ AU-01 (âm thanh, AU-Q-1), AR-02 §4 / §5.1 (xương, clip, marker `hit`) |
| v0.4 | 2026-10-25 | — | Đợt rà nhất quán (AR-03 / AR-04): cấu trúc `render/vfx/` theo AR-04 §6.5 / §10; CL-R-067 thuật toán chọn VFX + nguồn dữ liệu; CL-R-068 telegraph `TGPAL_*` gộp 1 draw call; CL-R-069 thứ tự lớp vẽ §4.2 + pool / LOD / giảm khi đông; CL-R-071..076 hoạt ảnh (co giãn kẹp `rateMin` / `rateMax`, blend `anim.blend`, chuỗi dự phòng, clip riêng boss qua `skillClips`, lớp thân trên / dưới, gói GLB); §13 khóa `anim.blend`, `anim.rateMin` / `rateMax`, `vfxBudget` (VFX-Q-11); bỏ bản sao số camera (theo GD-24, sửa vênh zoom 24 → 26m); CL-AC-016..021 |

