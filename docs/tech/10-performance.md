# TD-11 — Performance Budget

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-11 · Prefix ID: `PERF` |
| Owner | Tech Lead (Client) + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.4 |
| Cần trước | Phase 0 (đo spike), Phase 1 (cổng hiệu năng trong CI) |
| Quyết định nền | PLAN §5.2, D-01, D-02, D-19, D-22 (đa nền tảng); Valkey thay Redis (OSS-Q-1) |
| Liên quan | [TD-01](00-tdd.md) §16 · [TD-02](01-network.md) §12 · [TD-03](02-database.md) §9 · [TD-05](04-client.md) §5, §8, §9 · [TD-06](05-game-server.md) §4 · [TD-12](11-world-maps.md) · [TD-13](12-map-editor.md) §7 · [TD-14](13-geom2d.md) GEO-R-081 · AR-02 Asset Guidelines · [AR-03 Animation List](../art/animation-list.md) §2.5, §6 · [AR-04 VFX Guidelines](../art/vfx-guidelines.md) §9, §10 |

> **Nguồn duy nhất cho mọi con số hiệu năng.** Tài liệu khác chỉ trỏ về đây; khi vênh, **TD-11 thắng** và tài liệu kia phải sửa. Số liệu là mục tiêu ban đầu, hiệu chỉnh sau mỗi đợt đo (ghi vào lịch sử).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì | Mục |
|--------------|--------|-----|
| TD-05, TD-13, AR-02 | Ngân sách client, asset, bản đồ | §2, §3 |
| AR-03, AR-04 | Ngân sách GLB hoạt ảnh; ngân sách VFX (thời gian sống, boss, draw call, thời tiết, pool) | §3, §3.2 |
| TD-02, TD-06, TD-14 | Ngân sách mạng, tick, geometry | §4, §5 |
| TD-03, TD-04, TD-12 | Ngân sách API / DB / Valkey, chuyển bản đồ, đặt room | §5, §6 |
| CI (TD-10 CC-R-103) | Cổng hiệu năng | §8 |

---

## 0. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="perf-r-001"></a>**PERF-R-001** | Mọi ngân sách đo trên **thiết bị tham chiếu** (§1) với **kịch bản chuẩn** (§7.2); số đo trên máy dev không dùng để kết luận. |
| <a id="perf-r-002"></a>**PERF-R-002** | Ngân sách **có số đo đi kèm**: mỗi mục có metric / công cụ đo cụ thể (§7.1). Không có cách đo thì không phải ngân sách. |
| <a id="perf-r-003"></a>**PERF-R-003** | Vượt ngân sách loại **chặn** → không merge / không phát hành; loại **cảnh báo** → mở việc, phải xử lý trước mốc phase tiếp theo (§8). |
| <a id="perf-r-004"></a>**PERF-R-004** | Khi số trong tài liệu khác vênh với TD-11, sửa tài liệu kia (danh sách vênh hiện tại: §11). |

---

## 1. Thiết bị tham chiếu

| Mã | Loại | Cấu hình mô tả (chọn máy thật khớp mô tả, ghi model cụ thể vào phụ lục khi mua) | Preset mục tiêu |
|----|------|-------------------------------------------------------------|-----------------|
| `REF-PC-LOW` | PC / laptop văn phòng | CPU 4 nhân thế hệ ~2018 trở lên, **GPU tích hợp** lớp Intel UHD 620 – Iris Xe, RAM 8 GB, màn 1080p, Chrome bản ổn định | **Trung** (mặc định), 60 FPS |
| `REF-PC-MID` | Laptop / PC tầm trung | CPU 6 nhân, GPU rời phổ thông hoặc iGPU mạnh lớp Radeon 680M, RAM 16 GB, 1080p–1440p | Cao, 60 FPS |
| `REF-AND-MID` | Android tầm trung | SoC tầm trung ra mắt ~2–3 năm trước, RAM 6 GB, WebGL2, Chrome / WebView | Thấp, ≥ 30 FPS (mục tiêu 45) |
| `REF-IOS-OLD` | iPhone đời cũ nhất còn hỗ trợ | iPhone còn nhận bản iOS hiện hành, Safari / WKWebView có WebGL2 (chốt dòng máy ở PERF-Q-1) | Thấp, ≥ 30 FPS |

- <a id="perf-r-010"></a>**PERF-R-010** Mobile (`REF-AND-MID`, `REF-IOS-OLD`) chỉ là **cổng bắt buộc từ khi làm mobile** (sau launch, PLAN §5.8); trước đó đo định kỳ để không thiết kế ra thứ không thể chạy trên mobile.
- <a id="perf-r-011"></a>**PERF-R-011** Mạng tham chiếu: **20 Mbps, RTT 60ms** (bình thường) và **5 Mbps, RTT 150ms ± 20ms, mất gói 2%** (xấu — dùng cho kiểm thử mạng TD-02).

---

## 2. Ngân sách client

### 2.1. Theo preset (cảnh chuẩn "thành trấn đông 100 người", §7.2)

| Chỉ số | Thấp (`REF-AND-MID`) | Trung (`REF-PC-LOW`) | Cao (`REF-PC-MID`) |
|--------|:---:|:---:|:---:|
| FPS trung bình / 1% thấp | ≥ 30 / ≥ 24 | **≥ 60 / ≥ 45** | ≥ 60 / ≥ 50 |
| Thời gian khung (ms, p95) | ≤ 33 | ≤ 16,7 | ≤ 16,7 |
| — mô phỏng + mạng (`sim`, ECS, nội suy) | ≤ 4 | ≤ 3 | ≤ 3 |
| — render (CPU submit + GPU) | ≤ 24 | ≤ 11 | ≤ 11 |
| — UI React + khác | ≤ 4 | ≤ 2,5 | ≤ 2,5 |
| Draw call (toàn khung) | ≤ 100 | **≤ 200** | ≤ 250 |
| — trong đó môi trường | ≤ 80 | ≤ 150 | ≤ 180 |
| Tam giác trong khung nhìn (tổng) | ≤ 250k | **≤ 600k** | ≤ 900k |
| — môi trường | ≤ 150k | ≤ 300k | ≤ 400k |
| — nhân vật / quái (sau LOD) | ≤ 100k | ≤ 300k | ≤ 500k |
| Bộ nhớ GPU (texture + buffer) | ≤ 256 MB | ≤ 512 MB | ≤ 768 MB |
| JS heap sau 30 phút chơi | ≤ 300 MB | ≤ 400 MB | ≤ 500 MB |
| Hạt VFX đồng thời (gồm thời tiết + không khí, PERF-R-035) | ≤ 1.500 | ≤ 4.000 | ≤ 8.000 |

- <a id="perf-r-020"></a>**PERF-R-020** **LOD nhân vật bắt buộc** để đạt trần tam giác khi 100 nhân vật trong khung: LOD0 (≤ 5k) trong 15m, LOD1 (≤ 2k) 15–30m, LOD2 (≤ 800) xa hơn; người chơi khác ngoài 30m dùng LOD2 ở preset Trung.
- <a id="perf-r-021"></a>**PERF-R-021** Tự hạ preset (TD-05 CL-R-100): PC khi FPS trung bình < 45 trong 30s; mobile < 28 trong 30s.
- <a id="perf-r-022"></a>**PERF-R-022** Không rò rỉ: sau 10 lần chuyển bản đồ qua lại, số mesh / texture / entity / listener quay về mức ban đầu ± 5% (TD-05 CL-R-041).

### 2.2. Tải & khởi động

| Chỉ số | Ngân sách | Loại |
|--------|-----------|------|
| **Tổng tải lần đầu** tới khi điều khiển được ở `V1_F01` | **≤ 15 MB** (nén) | Chặn |
| — mã lõi (JS gzip) | ≤ 3 MB | Chặn |
| — gamedata (`bundle.client`) + i18n `vi` | ≤ 1,5 MB | Chặn |
| — bản đồ đầu + model / texture cần thiết | ≤ 10 MB | Chặn |
| Thời gian tới khi điều khiển được (mạng 20 Mbps, cache trống) | ≤ 20s trên `REF-PC-LOW` | Cảnh báo |
| Vào lại (cache ấm) | ≤ 6s | Cảnh báo |
| Tải một bản đồ chưa có trong cache (sau khi đã ở trong game) | ≤ 5s ở 20 Mbps | Cảnh báo |
| **Chuyển bản đồ đã tải trước**, từ kích hoạt tới điều khiển được | **p95 ≤ 2s** (TD-12 WM-R-044) | Chặn (đo ở load test) |
| Quota cache trình duyệt | web 500 MB · mobile 300 MB (LRU) | — |

---

## 3. Ngân sách asset

| Asset | Tam giác | Texture | Khác |
|-------|---------|---------|------|
| Nhân vật người chơi / NPC hình người | LOD0 ≤ 5k · LOD1 ≤ 2k · LOD2 ≤ 800 | Atlas chung + 1 texture vẽ tay ≤ 1024² (KTX2) | Xương ≤ 60 bone (bộ xương chung); GLB ≤ 600 KB |
| Trang phục (mesh thay thế) | ≤ 3k (phần thay) | Dùng chung texture nhân vật / ≤ 512² | ≤ 300 KB |
| Quái thường | LOD0 ≤ 3k · LOD1 ≤ 1,2k | ≤ 512² | ≤ 400 KB |
| Tinh anh / thú lớn | ≤ 6k | ≤ 1024² | |
| Boss | ≤ 15k | ≤ 2048² (1 bộ) | |
| Prop môi trường | 500–1.500 (prop lớn / kiến trúc ≤ 4k) | Atlas gradient chung; đồ đặc biệt 256–512² | `collisionTemplate`, không va chạm theo mesh (TD-13) |
| VFX một kỹ năng | Hạt ≤ 300 (preset Cao), mesh VFX ≤ 500 tam giác | Atlas VFX chung ≤ 1024² | Thời gian sống ≤ 3s cho hiệu ứng một lần; hiệu ứng duy trì theo gameplay (`persistSec`, triệu hồi, status, hào quang) phải vòng lặp và tính vào trần hạt đồng thời (AR-04) |
| GLB hoạt ảnh (mỗi file, AR-03 §2.5) | — | — | ≤ 3 MB sau `art:optimize` (Meshopt, 30 fps); tách khỏi model (TD-05 CL-R-070); xem PERF-R-037 |
| Icon / chân dung (UI) | — | Icon 128² (webp), chân dung ≤ 512² | Gộp sprite sheet theo nhóm |

- <a id="perf-r-030"></a>**PERF-R-030** Mọi asset qua glTF-Transform (Meshopt + KTX2); CI kiểm tra số tam giác, kích thước texture, kích thước file theo bảng trên (AR-02 định nghĩa tên / thuộc tính đo).
- <a id="perf-r-031"></a>**PERF-R-031** **Âm thanh** (AU-01 §6): tối đa **32 kênh đồng thời** ở preset Trung (Thấp 16, Cao 48), ưu tiên theo khoảng cách + loại cue (cue gameplay bắt buộc AU-01 §5 không bị cắt); âm thanh trong tải lần đầu chỉ gồm gói SFX lõi + nhạc màn đầu, nằm trong ngân sách tải lần đầu §2.2; nhạc vực tải theo bản đồ (Opus / OGG ≤ 128 kbps).

- <a id="perf-r-037"></a>**PERF-R-037** **Hoạt ảnh:** mỗi GLB hoạt ảnh ≤ 3 MB (khởi điểm, hiệu chỉnh sau đo Phase 1); phần hoạt ảnh trong tải lần đầu (`HUMANOID` + nhóm quái của `V1_F01`) ≤ 2 MB, nằm trong 10 MB "bản đồ đầu + model" của §2.2. GLB nhóm chỉ tải khi bản đồ có thực thể dùng nhóm đó (TD-05 §8). Với AR-03 §6, `HUMANOID` (gồm quái hình người theo archetype) tới Alpha ≈ 80 clip, Beta 1 ≈ 125 clip (thêm `DUAL_BLADE` / `FIST` ước lượng) và clip riêng boss nằm chung GLB nhóm theo CL-R-070: CI đo kích thước mỗi GLB; vượt 3 MB thì **chặn** và phải tách file theo đề xuất AR-03 ANM-Q-3 (`HUMANOID_{WeaponType}.glb`, `{SKELETON}_{MODEL_KEY}.glb`) — việc tách cần sửa CL-R-070 và AR-02 §3 trước.

### 3.1. Bản đồ (thống nhất với TD-13 §7)

| Hạng mục | Dã ngoại 300–400m | Thành trấn | Instance | Loại |
|----------|------------------|-----------|----------|------|
| Tổng tải (chunks + texture riêng) | ≤ 5 MB | ≤ 6 MB | ≤ 2 MB | Chặn |
| Logic JSON client (gzip) | ≤ 150 KB | ≤ 150 KB | ≤ 50 KB | Chặn |
| Draw call môi trường trong khung | ≤ 150 | ≤ 150 | ≤ 100 | Cảnh báo |
| Tam giác môi trường trong khung | ≤ 300k | ≤ 300k | ≤ 200k | Cảnh báo |
| Đa giác va chạm | ≤ 2.000 | ≤ 2.500 | ≤ 800 | Chặn |


### 3.2. VFX (xác nhận AR-04 §9–§10)

| Mã | Quy tắc |
|----|---------|
| <a id="perf-r-032"></a>**PERF-R-032** | **Thời gian sống ≤ 3s** (§3) áp cho **từng hạt** và cho hiệu ứng một lần (pha `CAST`, `IMPACT`, `FADE`). Hiệu ứng có thời lượng do gameplay dài hơn 3s (`persistSec` tới 6s với `MOD_LINGER`, `CORE_BLOOM` 4s, `SKL_HEALING_RAIN` 5s, triệu hồi 8s, status, hào quang) chạy pha `LINGER` dạng **phát lặp** với số hạt ổn định, nằm trong ngân sách "VFX một kỹ năng" và tính vào trần hạt đồng thời §2.1 (xác nhận AR-04 VFX-R-086). |
| <a id="perf-r-033"></a>**PERF-R-033** | **Boss / world boss:** mỗi chiêu (kể cả mẫu riêng `VFX_BOSS_*`) dùng ngân sách "VFX một kỹ năng" của §3 (hạt ≤ 300 ở Cao, mesh ≤ 500 tam giác); không có ngân sách riêng. Kiểm bằng `SCN-BOSS-20`: tổng hạt đồng thời vẫn ≤ trần preset §2.1 (xác nhận AR-04 VFX-R-087). |
| <a id="perf-r-034"></a>**PERF-R-034** | **Draw call VFX:** VFX (gồm vùng cảnh báo) tính trong phần draw call **ngoài môi trường** của §2.1 (Thấp 20 · Trung 50 · Cao 70 = tổng − môi trường), dùng chung với nhân vật / quái. Ràng buộc thiết kế: mỗi mẫu VFX = 1 hệ hạt + tối đa 1 mesh, chung atlas VFX → các hiệu ứng cùng mẫu gộp được; mọi vùng cảnh báo trong khung gộp **1 draw call** (AR-04 VFX-R-035, VFX-R-088). Chưa tách trần riêng cho VFX (PERF-Q-4); đo bằng `SceneInstrumentation` ở `SCN-BOSS-20`. |
| <a id="perf-r-035"></a>**PERF-R-035** | **Thời tiết + không khí** tính vào trần hạt đồng thời §2.1, dành tối đa **25%** trần cho chúng (`vfx/params.json → reserve.ambientPct` 0,25); hệ số theo preset (20% / 60% / 100%) của TD-05 §9 áp trước khi so trần (xác nhận AR-04 VFX-R-089). |
| <a id="perf-r-036"></a>**PERF-R-036** | **Pool VFX:** không tạo / hủy `ParticleSystem` hay material trong trận; làm ấm khi `MapLoading` 4 hệ / mẫu, 16 cho đạn; trần 32 / mẫu, hết chỗ thì tái dùng hiệu ứng cũ nhất có ưu tiên thấp nhất (`vfx/params.json → pool`, AR-04 VFX-R-090..092). Rời bản đồ trả về mức ban đầu ± 5% (PERF-R-022). |

Trần hạt theo preset được sao vào gamedata ở `world/client.json → vfxBudget` (TD-05 §13, chốt AR-04 VFX-Q-11) để client đọc; **TD-11 là nguồn số**, khi đổi số phải sửa TD-11 trước rồi tới file dữ liệu. `vfx/params.json` không chứa lại số của TD-11 (AR-04 §11.3).

---

## 4. Ngân sách mạng (thống nhất với TD-02 §12)

| Tình huống | Downstream / client | Upstream / client |
|-----------|---------------------|-------------------|
| Dã ngoại, ~20 thực thể trong tầm | ≤ 6 KB/s | ≤ 1,5 KB/s |
| Thành trấn đông, ~100 thực thể | ≤ 20 KB/s | ≤ 1,5 KB/s |
| Boss, 20 người + hiệu ứng | ≤ 25 KB/s (đỉnh 40) | ≤ 2 KB/s |
| Kích thước message C2S | ≤ 2 KB (chat ≤ 1 KB văn bản) | |
| Kết nối lại (state đầy đủ trong view) | ≤ 60 KB một lần | |

- <a id="perf-r-040"></a>**PERF-R-040** Cảm giác điều khiển: phản hồi hoạt ảnh của bản thân ≤ 1 khung (dự đoán); sai lệch dự đoán khi đứng yên < 0,05m; không giật > 1,5m ở mạng xấu (TD-02 NW-AC-001).

---

## 5. Ngân sách Game Server

| Chỉ số | Ngân sách | Nguồn |
|--------|-----------|-------|
| Tick | 20Hz (50ms) | TD-02 NW-R-010 |
| **Tick p99 một room** (150 người + 200 quái, đánh liên tục) | **< 25ms** | TD-06 §4 |
| — geometry (di chuyển + lượt trúng / đạn) | ≤ 8ms | TD-14 GEO-R-081 |
| — AI quái (200 quái) | ≤ 4ms | TD-06 GS-R-083 |
| — từng bước tick | theo bảng TD-06 §4 (tổng 24ms) | TD-06 |
| Cảnh báo vận hành | tick p99 > 40ms trong 1 phút | TD-01 TA-R-133 |
| Bộ nhớ một room mở đầy | ≤ 150 MB | — |
| Bộ nhớ một tiến trình | ≤ 1,5 GB (giới hạn `--max-old-space-size` 2 GB) | — |
| **Người chơi / tiến trình** (đặt room — TD-12 WM-R-020) | **mềm 400**, cứng 600 | §11 (thay số 1.000 cũ) |
| Tiến trình / máy | 1 tiến trình / vCPU (trừ 1 vCPU cho hệ thống) | TD-01 §17 |
| CCU mục tiêu / máy 8 vCPU | ~2.000–2.500 (xác nhận bằng load test Alpha) | §11 (thay "500–1.000" của PLAN §5.6) |
| Thời gian nạp room (onCreate, bản đồ + spawn) | ≤ 1s | — |
| `session-load` + vào room | ≤ 300ms p95 | — |

- <a id="perf-r-050"></a>**PERF-R-050** Cơ sở tính "400 người / tiến trình": một room đầy đánh liên tục dùng tới ~50% một lõi (25ms / 50ms); phần lớn room không ở đỉnh cùng lúc. Số chính thức chốt sau load test Alpha (§9) và cập nhật tham số `softCapPlayersPerProcess` (TD-12).

---

## 6. Ngân sách API Service, PostgreSQL, Valkey

| Chỉ số | Ngân sách | Loại |
|--------|-----------|------|
| API công khai — đọc | p95 ≤ 80ms, p99 ≤ 200ms | Cảnh báo |
| API công khai — ghi (không gồm chấm minigame) | p95 ≤ 150ms, p99 ≤ 400ms | Cảnh báo |
| API nội bộ — gom lô (kill / mastery / tuổi / fog) | p95 ≤ 100ms | Cảnh báo |
| Cấp vé chuyển bản đồ (`world/transfer`) | p95 ≤ 150ms (phần của 2s chuyển bản đồ) | Chặn ở load test |
| Truy vấn DB | p95 < 20ms, cảnh báo log khi > 100ms | TD-03 DB-R-060 |
| Kết thúc kiếp (bước 9, nhân vật 200 vật phẩm) | < 200ms | TD-03 DB-R-020 |
| Throughput mục tiêu ở 2.500 CCU | ~1.500 request ghi nội bộ / s (gom lô + snapshot) + ~300 request công khai / s | Đo load test |
| Kết nối PostgreSQL (qua PgBouncer) | API dùng ≤ 80% `max_connections` | TD-03 §3 |
| **Valkey** — lệnh thường (`GET` / `SET` / `XADD`) | p99 ≤ 2ms | Cảnh báo |
| Valkey — độ trễ relay outbox (ghi DB → có trên stream) | p95 ≤ 1s, cảnh báo khi chưa phát > 30s | TD-01 TA-R-133 |
| `CharacterContext` mới | tuổi ≤ 2s khi kiểm tra (TD-01 TA-R-091) | — |

---

## 7. Phương pháp đo

### 7.1. Công cụ
| Lớp | Công cụ | Đo gì |
|-----|---------|-------|
| Client | Babylon.js Inspector + `SceneInstrumentation` / `EngineInstrumentation`; overlay F11 (TD-05) | FPS, thời gian khung, draw call, tam giác, texture |
| Client | Chrome Performance / tracing; `performance.measure` quanh từng pass | Thời gian theo pass §2.1 |
| Client (CI) | **Playwright** chạy cảnh chuẩn trên máy tham chiếu tự host (runner riêng), xuất JSON | FPS, draw call, heap, kích thước gói |
| Gói | `rollup-plugin-visualizer` / size-limit | Kích thước JS, gamedata, chunk bản đồ |
| Game Server | `gs_tick_duration_ms`, `gs_system_duration_ms{system}` (prom-client, TD-06 §11); `--cpu-prof` khi điều tra | Tick, từng system |
| Tải | **Bot Colyseus headless** (dùng lại `@tienlo/sim` + adapter đầu vào giả) + **k6** (REST / WebSocket) | CCU, băng thông, chuyển bản đồ, API |
| DB | `pg_stat_statements`, `EXPLAIN (ANALYZE, BUFFERS)` | Truy vấn chậm |
| Valkey | `LATENCY` / `INFO commandstats`, `XINFO` | Độ trễ lệnh, độ dài stream |

### 7.2. Kịch bản chuẩn
| Mã | Kịch bản | Thiết lập |
|----|----------|-----------|
| `SCN-TOWN-100` | Thành trấn đông | `V1_HUB`, 100 bot đi lại + chat + 20% dùng kỹ năng nhẹ, camera theo một bot, 3 phút |
| `SCN-BOSS-20` | Boss | Bí cảnh BC02, 20 bot đánh Hồ Yêu Mẫu đủ 3 pha, VFX đầy đủ |
| `SCN-FIELD` | Dã ngoại | `V1_F05`, 1 người chơi + 20 thực thể, đi hết vòng tuần tra 5 phút, chuyển 2 bản đồ |
| `SCN-ROOM-FULL` | Room đầy (server) | 150 bot + 200 quái đánh liên tục 10 phút (TD-06 GS-AC-001) |
| `SCN-SOAK` | Ngâm | 1 client chơi 2 giờ (chuyển bản đồ, chế tác, đánh) — đo rò rỉ |

---

## 8. Cổng hiệu năng trong CI

| Cổng | Khi nào chạy | Loại |
|------|--------------|------|
| Kích thước gói (mã lõi, gamedata, bản đồ, asset theo §3) | Mỗi PR | **Chặn** |
| Số tam giác / texture / bone của asset mới | Mỗi PR có asset | **Chặn** |
| Đa giác va chạm, logic JSON bản đồ | Mỗi PR có bản đồ | **Chặn** |
| Microbenchmark `@tienlo/sim` (geometry, AI quyết định) so với mốc | Mỗi PR chạm `sim` | Cảnh báo (> 10% chậm hơn) |
| `SCN-TOWN-100` trên `REF-PC-LOW` | Hằng đêm | Cảnh báo; **chặn phát hành** nếu dưới ngân sách 3 đêm liên tiếp |
| `SCN-ROOM-FULL` tick p99 | Hằng đêm (máy chủ staging) | Cảnh báo; chặn phát hành như trên |
| Load test CCU (§9) | Trước mỗi mốc phase | Chặn mốc phase |

- <a id="perf-r-060"></a>**PERF-R-060** **Quy trình khi vượt:** (1) CI ghi số đo + so với mốc; (2) mở việc gắn nhãn `perf` cho người sở hữu hệ thống; (3) ưu tiên sửa nguyên nhân (gộp mesh, LOD, giảm tần suất AI…) trước khi nới ngân sách; (4) nới ngân sách chỉ bằng PR sửa TD-11 có số đo trên thiết bị tham chiếu và Tech Lead duyệt.

---

## 9. Kế hoạch load test theo phase

| Phase | Mục tiêu | Kịch bản |
|-------|----------|----------|
| 0 (spike) | 2 client + skillshot ở mạng xấu; 1 room 50 bot | Đo khung dự đoán / bù trễ (TD-02), tick |
| 1 | 1 room 150 bot + 200 quái | `SCN-ROOM-FULL` đạt < 25ms |
| 2 | 1 tiến trình 400 người (3 room) + chuyển bản đồ liên tục | Xác nhận `softCapPlayersPerProcess`, chuyển bản đồ p95 < 2s |
| 3 | 1 máy 8 vCPU, 1.500 CCU, API + DB + Valkey thật | Throughput §6 |
| 4 (Alpha) | **2.500 CCU** trên cấu hình triển khai Alpha (TD-01 §17) trong 1 giờ | Toàn bộ §5–§6; chốt số CCU / máy |
| 5a / 5b (Beta) | 500+ rồi **1.000+ CCU thật** + bot bù tới 5.000 | Hành vi người thật + PvP + sự kiện thế giới |

---

## 10. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="perf-ac-001"></a>PERF-AC-001 | `REF-PC-LOW`, preset Trung | `SCN-TOWN-100` 3 phút | FPS trung bình ≥ 60, 1% thấp ≥ 45, draw call ≤ 200, tam giác ≤ 600k |
| PERF-AC-002 | Cache trống, mạng 20 Mbps | Mở game tới `V1_F01` | Tổng tải ≤ 15 MB (mã ≤ 3, gamedata ≤ 1,5, bản đồ + model ≤ 10); ≤ 20s |
| PERF-AC-003 | PR thêm model nhân vật 6.500 tam giác LOD0 | CI | Fail (≤ 5k) |
| PERF-AC-004 | PR làm gói JS lõi lên 3,2 MB gzip | CI | Fail |
| PERF-AC-005 | `SCN-ROOM-FULL` | 10 phút | Tick p99 < 25ms; geometry ≤ 8ms; AI ≤ 4ms |
| PERF-AC-006 | Tiến trình đạt 400 người | Có người mới vào bản đồ | World Directory đặt room mới ở tiến trình khác (WM-R-020) |
| PERF-AC-007 | Bản đồ đã tải trước, load test 400 người chuyển liên tục | Đo | Chuyển bản đồ p95 ≤ 2s |
| PERF-AC-008 | `SCN-SOAK` 2 giờ | Đo heap | ≤ 400 MB, số mesh / texture không tăng dần (PERF-R-022) |
| PERF-AC-009 | Load test Alpha 2.500 CCU | 1 giờ | API đọc p95 ≤ 80ms, DB p95 < 20ms, Valkey p99 ≤ 2ms, tick p99 < 25ms |
| PERF-AC-010 | `REF-AND-MID`, preset Thấp | `SCN-FIELD` | FPS trung bình ≥ 30 (chỉ cổng bắt buộc từ khi làm mobile, PERF-R-010) |
| <a id="perf-ac-011"></a>PERF-AC-011 | `REF-PC-LOW`, preset Trung | `SCN-BOSS-20` đủ 3 pha | Hạt VFX đồng thời ≤ 4.000; mỗi chiêu boss ≤ 300 × 70% = 210 hạt (PERF-R-033); draw call ≤ 200 |
| <a id="perf-ac-012"></a>PERF-AC-012 | PR làm `art/animations/HUMANOID.glb` lên 3,4 MB | CI | Fail (PERF-R-037) |
| <a id="perf-ac-013"></a>PERF-AC-013 | Vùng `SKL_HEALING_RAIN` 5s ở preset Cao | Đo | Không hạt nào sống > 3s; số hạt pha `LINGER` ổn định ≤ 300 (PERF-R-032) |
| <a id="perf-ac-014"></a>PERF-AC-014 | Thời tiết `STORM` ở preset Trung, `SCN-TOWN-100` | Đo | Hạt thời tiết + không khí ≤ 1.000 (25% × 4.000, PERF-R-035) |

---

## 11. Chỗ vênh đã thống nhất (cần sửa ở tài liệu nguồn)

| Tài liệu | Số cũ | Số chuẩn (TD-11) |
|----------|-------|------------------|
| TD-01 §16 "1.000 CCU / tiến trình"; TD-12 `placement.softCapPlayersPerProcess: 1000` | 1.000 người / tiến trình | **400 mềm / 600 cứng** (§5) |
| PLAN §5.6 "1 game-server 8 vCPU/16GB ~500–1000 CCU" | 500–1.000 / máy | **~2.000–2.500 / máy 8 vCPU** (xác nhận Alpha) |
| PLAN §5.2, TD-05 CL-R-066: chỉ có trần draw call < 200 và tam giác từng model, **không có trần tổng tam giác** (100 nhân vật × 5k = 500k + môi trường 300k vượt khả năng GPU tích hợp) | — | Tổng ≤ 600k ở Trung + **LOD nhân vật bắt buộc** (PERF-R-020) |
| PLAN §5.2 "mỗi bản đồ thêm 2–5MB" | 2–5 MB | Dã ngoại ≤ 5, **thành trấn ≤ 6**, instance ≤ 2 (TD-13 §7) |
| TD-13 §7 draw call ≤ 150 "trong khung nhìn" | Không rõ là tổng hay môi trường | **Môi trường** ≤ 150 trong tổng ≤ 200 |

---

## 12. Câu hỏi mở
- [ ] **PERF-Q-1:** Chốt dòng iPhone / Android cụ thể làm thiết bị tham chiếu và mua máy cho runner CI (liên quan thời điểm làm mobile).
- [ ] **PERF-Q-2:** Runner CI đo hiệu năng tự host (máy văn phòng thật) hay dùng dịch vụ đám mây có GPU tích hợp? Tự host cho số ổn định hơn.
- [ ] **PERF-Q-3:** Có nhắm 120 / 144 FPS cho màn tần số cao ở preset Cao (cảm giác combat) không, hay khóa 60?
- [ ] **PERF-Q-4:** Có cần trần draw call riêng cho VFX trong phần "ngoài môi trường" (PERF-R-034) không? *Mặc định: không tách; đo ở `SCN-BOSS-20` Phase 1 rồi quyết.*

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: thiết bị tham chiếu, ngân sách client theo preset (FPS, thời gian khung theo pass, draw call, **trần tổng tam giác + LOD bắt buộc**, bộ nhớ, tải / khởi động / chuyển bản đồ), asset, bản đồ, mạng, Game Server (**400 người / tiến trình**), API / DB / Valkey, công cụ + kịch bản đo, cổng CI, kế hoạch load test theo phase, nghiệm thu, bảng vênh đã thống nhất |
| v0.2 | 2026-10-25 | — | PERF-R-031 ngân sách âm thanh (AU-01) |
| v0.3 | 2026-10-25 | — | Làm rõ thời gian sống VFX duy trì (AR-04) |
| v0.4 | 2026-10-25 | — | Đợt rà nhất quán (AR-03 / AR-04): §3.2 xác nhận VFX-R-086..092 — PERF-R-032 (vòng lặp > 3s), PERF-R-033 (boss dùng ngân sách một kỹ năng, VFX-Q-9), PERF-R-034 (draw call VFX trong phần ngoài môi trường, PERF-Q-4), PERF-R-035 (thời tiết ≤ 25% trần, VFX-Q-10), PERF-R-036 (pool); PERF-R-037 + dòng §3 ngân sách GLB hoạt ảnh ≤ 3 MB / file; khóa trần hạt ở `world/client.json → vfxBudget` (VFX-Q-11); PERF-AC-011..014 |

