# PM-06 — Kế hoạch coding (Phase 0 → Phase 2)

| Thuộc tính | Giá trị |
|---|---|
| Mã tài liệu | PM-06 · Prefix ID: `CP` |
| Trạng thái | 🟡 Bản nháp v0.1 |
| Ngày | 2026-10-25 |
| Đọc cho | Tech Lead, lập trình viên, **AI code** (mỗi việc trong backlog §7 là một đơn vị giao việc theo TD-10 CC-R-111) |
| Liên quan | [PLAN §7](../../PLAN.md) (roadmap) · [AGENTS.md](../../AGENTS.md) · [TD-01](../tech/00-tdd.md) §4, §19 · [TD-10](../tech/09-conventions.md) §11–§12 · [TD-04](../tech/03-api.md) · [TD-02](../tech/01-network.md) · [TD-05](../tech/04-client.md) · [TD-06](../tech/05-game-server.md) · [TD-07](../tech/06-gamedata.md) · [TD-14](../tech/13-geom2d.md) · [PM-05](playtests/README.md) |

---

## 1. Đánh giá mức sẵn sàng

**Kết luận: đủ để bắt đầu code Phase 0 – Phase 1 ngay; Phase 2 đủ về spec hệ thống.** Thứ còn thiếu không phải thiết kế mà là **tệp máy đọc được** sinh từ tài liệu — việc đầu tiên của Sprint 0 (§4, M0).

| Hạng mục | Tình trạng | Ảnh hưởng | Xử lý |
|---|---|---|---|
| Kiến trúc, quyền hạn, ledger, idempotency, RNG, sự kiện (TD-01) | ✅ đủ | — | — |
| Network, Game Server, Client, Geometry (TD-02/05/06/14) | ✅ đủ cho spike + combat | — | — |
| Spec hệ thống GD (combat, quái, kỹ năng, tu luyện, vòng đời, túi đồ, nhiệm vụ, cơ duyên…) | ✅ đủ cho Phase 1–2 | Số cân bằng một số chỗ là **tạm** (né 0,4s, `ST_CHARMED`…) | Code đọc từ gamedata; chỉnh số không cần sửa code |
| **`shared/contracts`** (OpenAPI `public/internal/admin.yaml`, `errors.yaml`, `rng-kinds.yaml`) | ❌ chưa có tệp — chỉ có bảng trong TD-04 | **Chặn** code API và client gọi REST | M0-SH-03..05: AI sinh YAML từ TD-04 §5–§8, người review |
| **Kiểu gamedata TS + JSON Schema + dữ liệu thật** | ❌ chưa có tệp — chỉ có trong GD (§7 mỗi GD) | Chặn mọi hệ thống đọc tham số | M0-SH-06..07, sau đó mỗi lát dọc tự thêm dữ liệu của mình |
| `shared/events` (JSON Schema sự kiện domain) | ❌ chưa có tệp | Chặn outbox / phản ứng GS (Phase 2) | M4-SH-01 |
| **Bản đồ `V1_F01`** (GD-16 Level Design) | 🟡 đã có bản nháp v0.1 ([V1_F01](../design/30-levels/V1_F01.md)); còn câu hỏi LV1F01-Q-* | Chặn bản đồ thật ở M3 | M1–M2 dùng **bản đồ test** 300×300m; viết GD-16 `V1_F01` song song trước M3 |
| Asset (nhân vật, quái, prop, VFX) | Team tự bổ sung (D-28) | Không chặn code | Client dùng **placeholder** (capsule / khối, màu theo AR-04) có cùng tên ID; thay asset không đổi code |
| Quyết định chờ spike | Q22 map editor, Q23 Ink, OSS-Q-1 Valkey + Colyseus, recast trên Node | Chặn M3 (editor, cơ duyên) | Spike ở M1 (§4) |

---

## 2. Chia luồng: BE và FE

**Có chia được BE / FE**, nhưng dự án có **ba khối chạy** + **một khối dùng chung**, nên chia thành 5 luồng (lane). Mỗi luồng có thể giao cho một người + AI.

| Luồng | Thư mục | Ngôn ngữ | Thuộc | Tài liệu chính |
|---|---|---|---|---|
| **SH — Dùng chung / hợp đồng** | `shared/sim`, `shared/schema`, `shared/contracts`, `shared/gamedata`, `shared/events`, `shared/testvectors` | TS (+ YAML / JSON Schema) | Cả hai — **đường găng** | TD-14, TD-02 §8–§9, TD-04, TD-07, TD-01 §9–§10 |
| **GS — Game Server (BE thời gian thực)** | `game-server/` | TS · Colyseus | BE | TD-06, TD-02, TD-12, GD-06/07/12 |
| **API — API Service (BE lưu trữ, nguồn sự thật)** | `api-service/` | Java 21 · Spring Boot 3 | BE | TD-01 §5–§11, TD-03, TD-04, GD từng module |
| **CL — Client (FE)** | `client/` | TS · Babylon.js · React | FE | TD-05, GD-17, GD-24, AR-01..04 |
| **TO — Công cụ & hạ tầng** | `tools/`, `infra/` | TS · Docker · CI | Hỗ trợ | TD-10 §11, TD-07 §8–§9, TD-13, TD-09, AR-02 §8 |

### 2.1. Phụ thuộc giữa các luồng

```mermaid
flowchart LR
  SH[SH: contracts · schema · sim · gamedata types] --> GS[GS: Game Server]
  SH --> API[API: Java]
  SH --> CL[CL: Client]
  TO[TO: CI · docker · validator] --> SH
  API -. internal.yaml .-> GS
  GS -. message TD-02 .-> CL
  API -. public.yaml .-> CL
```

- <a id="cp-r-001"></a>**CP-R-001** **SH làm trước, các luồng khác song song sau đó.** Không luồng nào tự định nghĩa message, endpoint, enum, kiểu gamedata — phải có trong `shared/*` trước (TA-R-020, AGENTS §4 luật 2). Đổi hợp đồng = **PR spec / contract trước**, rồi mới PR code (CC-R-113).
- <a id="cp-r-002"></a>**CP-R-002** **Làm song song bằng giả lập theo hợp đồng:**
  - GS không chờ Java: dùng `ApiClient` **giả (fake)** cài theo `internal.yaml` (trả dữ liệu cố định / lưu trong bộ nhớ) — TD-06 §7.
  - CL không chờ Java: gọi REST qua client TS sinh từ `public.yaml`, chạy với **mock server sinh từ OpenAPI** (công cụ chọn ở M0-TO-04, đăng ký TD-15 trước).
  - CL chạy với **GS thật** (cùng TS, khởi động nhanh) ngay từ M1.
  - API test bằng contract test, không cần GS.
- <a id="cp-r-003"></a>**CP-R-003** **`shared/sim` là code chung client + GS** (va chạm, di chuyển, hình đòn, công thức). Một người sở hữu (thường người luồng GS), client chỉ dùng; mọi thay đổi có test trong `shared/testvectors` chạy ở cả hai phía (TA-R-011).

### 2.2. Cái gì làm trước, cái gì song song

| Giai đoạn | Tuần tự bắt buộc | Song song được |
|---|---|---|
| M0 (2–3 tuần) | TO dựng repo + CI → SH sinh contracts / types | TO docker-compose ‖ SH `shared/sim` geom2d ‖ CL dựng khung Vite + Babylon trống ‖ API dựng khung Spring + ArchUnit |
| M1 spike | SH message `C2S_Input` / state (TD-02) **trước** GS và CL | GS room + tick ‖ CL camera + di chuyển dự đoán ‖ API `play/join` + vé ‖ TO spike Valkey / Ink / recast / editor |
| M2 combat | SH công thức + hình đòn trong `sim` **trước** | GS AI quái + xác thực trúng ‖ CL game feel, telegraph, HUD ‖ API account / character / gamedata version |
| M3 bản đồ & khám phá | TD-13 định dạng + `maps:build` **trước** bản đồ thật; GD-16 `V1_F01` trước khi dựng bản đồ | GS POI / thu thập / hiểm địa thử ‖ CL fog, minimap, UI cơ duyên ‖ API encounter / talent ‖ content dựng bản đồ |
| M4 khung MMO | API module `platform` (idempotency, ledger, outbox, RNG) **trước** mọi module ghi | Từng module API (inventory, cultivation, lifecycle, quest…) song song ‖ GS handoff / World Directory ‖ CL từng màn GD-17 |

---

## 3. Tổ chức đề xuất (team < 10 người + AI)

| Vai trò | Người | Luồng | Ghi chú |
|---|:-:|---|---|
| Tech Lead | 1 | SH + review mọi PR hợp đồng | Duyệt contract / schema; giữ AGENTS.md, TD-10 |
| Server Dev (TS) | 1–2 | GS (+ sở hữu `shared/sim`) | |
| Backend Dev (Java) | 1–2 | API | Module theo TD-01 §5.1 |
| Client Dev | 2 | CL | 1 người render / combat feel, 1 người UI (React) |
| Tools / DevOps | 1 (có thể kiêm) | TO | CI, docker, validator, map editor |
| Designer / Content | 1–2 | gamedata, bản đồ, Ink | Không code; điền dữ liệu theo schema |

- <a id="cp-r-010"></a>**CP-R-010** Mỗi người điều phối **1–3 phiên AI song song** trên các việc **không chung file** (cùng một luồng nhưng khác thư mục / module). Hai việc cùng sửa `shared/*` không chạy song song.

---

## 4. Mốc (milestone) và tiêu chí xong

| Mốc | Thời gian (PLAN §7) | Mục tiêu | Xong khi |
|---|---|---|---|
| **M0** Nền móng | Tháng 1, tuần 1–3 | Repo, CI, docker, contracts / types sinh từ tài liệu | `pnpm -r build && test`, `./gradlew check`, `gamedata:check` xanh trên CI; contracts lint sạch |
| **M1** Spike kỹ thuật | Tháng 1–2 | Rủi ro lớn nhất: netcode, render, va chạm chung, Valkey, Ink, editor | Các spike PLAN Phase 0 đạt; quyết định Q22, Q23, OSS-Q-1 ghi PM-04 |
| **M2** Combat slice | Tháng 3–4 | Combat top-down vui qua mạng (PLAN Phase 1 phần combat) | 1 kiếm + 4 kỹ năng + 3 quái + 1 tinh anh + 1 boss ổn định ở 150ms; playtest nội bộ |
| **M3** Bản đồ & khám phá | Tháng 4–5 | 1 bản đồ dã ngoại + 1 bí cảnh, thu thập, 3–5 cơ duyên, hiểm địa thử, roll thiên phú | Tiêu chí hoàn thành Phase 1 (PLAN §7) |
| **M4** Khung MMO | Tháng 6–9 | Tài khoản → nhân vật → 3 bản đồ nối nhau, tu luyện, vòng đời, túi đồ, nhiệm vụ, cơ duyên, lưu DB đầy đủ | Tiêu chí hoàn thành Phase 2 (PLAN §7) |

Phase 3 trở đi lập kế hoạch chi tiết ở bản sau của tài liệu này (cuối M3), dựa trên kết quả playtest (PM-05).

---

## 5. Nội dung từng mốc theo luồng

### M0 — Nền móng
| Luồng | Việc | Spec |
|---|---|---|
| TO | Monorepo pnpm (`client`, `game-server`, `shared/*`, `tools`), Gradle `api-service`; Prettier / ESLint / Spotless / ArchUnit; CI chạy build + test + lint; `docker-compose` PostgreSQL + Valkey | TD-10 §2, §4, §11; TD-01 §4; TD-09 §3 |
| SH | `shared/contracts`: `public.yaml`, `internal.yaml`, `admin.yaml` từ TD-04 §5; `errors.yaml` từ TD-04 §6; `rng-kinds.yaml` từ TD-01 §9.3 / TD-04 §8; sinh client TS + stub Java | TD-04 §2–§8, TA-R-020 |
| SH | `shared/gamedata`: kiểu TS → JSON Schema → Java, registry, validator `gamedata:check`, bundle client / server (`@serverOnly`) | TD-07 §3, §8–§10 |
| SH | `shared/sim/geom2d` + `shared/testvectors` | TD-14 §2–§9, §12 |
| API | Khung Spring Boot: module rỗng theo TD-01 §5.1, Flyway, cấu trúc `api/web/app/domain/persistence/events` | TD-10 §4.2; TD-03 §11 |
| CL | Khung Vite + React + Babylon; máy trạng thái màn hình rỗng | TD-05 §2–§3 |

### M1 — Spike
| Luồng | Việc | Spec |
|---|---|---|
| SH | `shared/schema`: `PlayerPublic`, `C2S_Input`, snapshot, message nhóm kết nối + di chuyển + kỹ năng | TD-02 §3–§5, §8–§9 |
| GS | Room = `MapInstance`, tick 20Hz, thứ tự hệ thống, nhận vé vào room, di chuyển server-authoritative, AOI | TD-06 §3–§6; TD-02 §3, §6; TD-12 §2 |
| GS | Skillshot + bù trễ (lag compensation) | TD-02 §7; TD-14 §5–§6 |
| CL | Camera 2.5D, nhân vật placeholder, WASD + chuột, dự đoán + đối chiếu (reconciliation), giả lập lag 150ms, đo FPS bản đồ 2.000 prop | TD-05 §5, §9; TD-02 §5; GD-24 |
| API | `POST /v1/play/join` cấp `joinTicket` (tài khoản dev) | TD-04 API-R-034; TD-01 §14 |
| TO | Spike: Valkey + Colyseus presence 2 tiến trình (OSS-Q-1); Ink + inkjs trên GS (Q23); recast-navigation-js trên Node; Babylon.js Editor làm nền map editor (Q22); glTF-Transform pipeline | PLAN Phase 0; TD-13 MF-R-031; TD-15 |

### M2 — Combat slice
| Luồng | Việc | Spec |
|---|---|---|
| SH | `sim`: công thức sát thương, poise, phản ứng (`reactionSec`), né; `ActionState`; hình đòn | GD-06; GD-07 §4–§5; TD-14 §5 |
| SH | gamedata: kiếm `WeaponDef`, 4 kỹ năng, 3 quái + tinh anh + boss Hồ Yêu Mẫu, status | GD-07 §7; GD-12 §7; GD-06 §5.12 |
| GS | Hệ thống kỹ năng, xác thực trúng, sát thương, chết / hồi sinh (chưa chết thật), AI quái (máy trạng thái, telegraph, boss pattern) | TD-06 §4, §9; GD-12 |
| CL | Combat feel (hit stop, rung camera), telegraph theo AR-04, state machine hoạt ảnh (placeholder), HUD tối thiểu, combat debug overlay | TD-05 §5–§7, §11; AR-04; GD-17 HUD |
| API | `account`, `character` tối thiểu; kiểm `gamedataVersion` (426) | TD-04 §5; API-AC-011 |

### M3 — Bản đồ & khám phá
| Luồng | Việc | Spec |
|---|---|---|
| TO | Map editor bản thô + `maps:build` + validator bản đồ | TD-13 §1–§7 |
| Content | **Viết GD-16 `V1_F01`** rồi dựng bản đồ + 1 bí cảnh | GD-21 checklist mật độ; TD-13 |
| GS | POI, điểm thu thập, ngày / đêm, cơ duyên (Ink runtime), hiểm địa thử (vào–lấy–thoát, hấp hối, rơi đồ) | GD-21; GD-22; GD-28; TD-12 §7–§8 |
| CL | Minimap, fog, UI cơ duyên + `ChanceView`, màn tạo nhân vật roll thiên phú | GD-17; GD-27; GD-29 |
| API | `talent` (roll, khóa dòng), `encounter` (đặt chỗ, kết quả), `world` (POI đã phát hiện) | GD-27; GD-22 §9; GD-21 |

### M4 — Khung MMO
| Luồng | Việc | Spec |
|---|---|---|
| API | **`platform` trước:** idempotency, ledger chuẩn, outbox → Valkey Streams, RNG + `rng_audit`, `CharacterContext`, death log | TD-01 §7–§12; TD-03 §6 |
| SH | `shared/events` JSON Schema; golden test RNG hai ngôn ngữ | TD-01 §9–§10; TA-R-011 |
| API | `inventory` → `cultivation` → `lifecycle` → `quest` → `economy` (shop NPC) → `social` (chat, tổ đội, tông NPC) | GD-09, GD-13, GD-04, GD-26, GD-15, GD-14 |
| GS | Handoff chuyển bản đồ, kênh, World Directory, World Clock; phản ứng sự kiện domain | TD-12 §4–§7; TD-06 §8 |
| CL | Màn GD-17: túi đồ, nhân vật (`C`), đột phá + `RiskConfirm`, nhiệm vụ, bản đồ thế giới, chat, tổ đội, tổng kết kiếp; onboarding GD-18 | GD-17; GD-18; TD-05 §7 |
| TO | Admin panel v1 + Death Review; load test 300 bot | TD-08; TD-11 §9; TD-09 |

---

## 6. Quy trình giao việc cho AI

- <a id="cp-r-020"></a>**CP-R-020** Mỗi việc dùng **mẫu TD-10 CC-R-112** (AGENTS.md §6). Mã việc: `M{mốc}-{LUỒNG}-{nn}` (vd. `M2-GS-03`); ghi mã việc trong tiêu đề PR.
- <a id="cp-r-021"></a>**CP-R-021** Một việc = một **lát dọc nhỏ** gắn danh sách mã `-R-` / `-AC-` cụ thể, sửa trong **một luồng**. Việc cần đổi `shared/*` tách thành việc SH riêng, làm trước.
- <a id="cp-r-022"></a>**CP-R-022** **Xong** khi: mọi `-AC-` liệt kê có test đúng tên; CI xanh; comment mã `-R-` ở chỗ hiện thực; checklist TD-10 §12.4 đạt; người review của luồng duyệt (PR đổi hợp đồng: Tech Lead duyệt).
- <a id="cp-r-023"></a>**CP-R-023** AI gặp spec thiếu / mâu thuẫn → **dừng**, ghi câu hỏi vào PR; người phụ trách sửa tài liệu (thêm `-Q-` hoặc chốt) trước khi tiếp tục. Không để AI tự đoán.
- <a id="cp-r-024"></a>**CP-R-024** Số cân bằng tạm (đánh dấu "tạm" trong GD) **không** chặn code: code đọc từ gamedata, designer chỉnh sau.

---

## 7. Backlog khởi động (M0 – M1)

> Thứ tự trong cùng một luồng là thứ tự làm; các luồng khác nhau chạy song song (trừ khi có cột "Chờ").

| Mã | Luồng | Việc | Spec | Chờ |
|---|---|---|---|---|
| M0-TO-01 | TO | Monorepo pnpm + cấu trúc thư mục + `README.md` mỗi gói | TD-01 §4; TD-10 §2, CC-R-092 | — |
| M0-TO-02 | TO | ESLint / Prettier / tsconfig strict; luật ranh giới gói (`shared/sim` không import client / server) | TD-10 §4.1; TA-R-021 | M0-TO-01 |
| M0-TO-03 | TO | CI: build + test + lint TS và Java; kiểm license phụ thuộc | TD-10 §11; TD-15 §3.3 | M0-TO-01 |
| M0-TO-04 | TO | `infra/docker-compose.yml`: PostgreSQL, Valkey, api, game-server; chọn + đăng ký mock server OpenAPI vào TD-15 | TD-09 §2–§3; TD-15 §3.1 | M0-TO-01 |
| M0-SH-01 | SH | `shared/sim/geom2d`: kiểu cơ bản (float64, không hàm lượng giác của `Math` — GEO-R-002/003), va chạm tròn–đa giác, `move()` | TD-14 §2–§4, GEO-R-003 | M0-TO-02 |
| M0-SH-02 | SH | `shared/testvectors` + bộ chạy test chung client / GS | TD-14 §10; TA-R-011 | M0-SH-01 |
| M0-SH-03 | SH | `errors.yaml` + `rng-kinds.yaml` | TD-04 §6, §8; TD-01 §9.3 | M0-TO-01 |
| M0-SH-04 | SH | `public.yaml` (nhóm play / account / character trước), `internal.yaml` (world / transfer) | TD-04 §2–§5 | M0-SH-03 |
| M0-SH-05 | SH | Sinh client TS + stub Java từ OpenAPI; contract test mẫu | TD-04 §2; TA-R-020 | M0-SH-04 |
| M0-SH-06 | SH | `shared/gamedata`: pipeline TS → JSON Schema → Java, registry, `gamedata:check`, bundle client / server | TD-07 §3, §8–§9 | M0-TO-02 |
| M0-SH-07 | SH | Dữ liệu tối thiểu: `world/client.json`, `world/camera.json`, `world/controls.json`, `stats/params.json` | TD-07 §9; TD-05 §13; GD-24; GD-06 | M0-SH-06 |
| M0-API-01 | API | Khung Spring Boot, module rỗng theo TD-01 §5.1, ArchUnit luật `web → app → domain`, Flyway baseline | TD-10 §4.2; TD-03 §11 | M0-TO-01 |
| M0-CL-01 | CL | Khung Vite + React + Babylon, máy trạng thái màn hình, nạp gamedata bundle client | TD-05 §2–§3, §8 | M0-TO-01 |
| M1-SH-01 | SH | `shared/schema`: state + message kết nối, di chuyển, kỹ năng | TD-02 §4–§5, §8–§9 | M0-SH-01 |
| M1-GS-01 | GS | Room `MapInstance` tick 20Hz, nhận `joinTicket`, `ApiClient` giả | TD-06 §3–§7; TD-12 §2 | M1-SH-01 |
| M1-GS-02 | GS | Di chuyển server-authoritative dùng `sim.move()`, AOI | TD-02 §5–§6; TD-14 §4 | M1-GS-01 |
| M1-GS-03 | GS | Skillshot + bù trễ | TD-02 §7; TD-14 §5–§6 | M1-GS-02 |
| M1-CL-01 | CL | Camera 2.5D + nhân vật placeholder + WASD / chuột | TD-05 §5; GD-24 | M0-CL-01 |
| M1-CL-02 | CL | Kết nối GS, dự đoán + đối chiếu, nội suy thực thể khác, giả lập lag | TD-02 §5; TD-05 §10 | M1-SH-01, M1-CL-01 |
| M1-CL-03 | CL | Bản đồ test 300×300m, 2.000 prop placeholder, đo FPS theo TD-11 | TD-11 §1–§2, §7 | M1-CL-01 |
| M1-API-01 | API | `POST /v1/play/join` cấp `joinTicket` (tài khoản dev, chưa auth thật) | TD-04 API-R-034 | M0-SH-05, M0-API-01 |
| M1-TO-01 | TO | Spike Valkey + Colyseus presence 2 tiến trình → PM-04 (OSS-Q-1) | TD-15; TD-12 §3 | M0-TO-04 |
| M1-TO-02 | TO | Spike Ink + inkjs trên GS: 3 cơ duyên mẫu, lưu / khôi phục → PM-04 (Q23) | GD-22 §5.6 | M1-GS-01 |
| M1-TO-03 | TO | Spike recast-navigation-js trên Node | TD-14 §8 | M0-TO-01 |
| M1-TO-04 | TO | Spike Babylon.js Editor làm nền map editor → PM-04 (Q22) | TD-13 MF-R-031, §5.1 | M0-TO-01 |

Backlog M2 – M4 tách việc chi tiết khi kết thúc M1 (theo bảng §5), cập nhật vào tài liệu này.

---

## 8. Rủi ro & điểm chặn

| Rủi ro | Ảnh hưởng | Giảm thiểu |
|---|---|---|
| Contracts sinh từ TD-04 sai / thiếu | Lệch giữa ba khối, sửa lan | Tech Lead review từng YAML; contract test trong CI từ M0 |
| AI tự thêm enum / endpoint | Lệch spec, khó gộp | AGENTS §4; review checklist TD-10 §12.4; CI kiểm enum so với contracts / gamedata |
| Hai phiên AI sửa cùng `shared/*` | Xung đột, hỏng hợp đồng | CP-R-010; chỉ một việc SH chạy tại một thời điểm |
| Netcode không mượt ở 150ms | Hỏng cột mốc Phase 1 | Spike M1 trước mọi thứ; telegraph dài hơn độ trễ |
| GD-16 `V1_F01` chưa viết khi tới M3 | Chặn bản đồ thật | Viết GD-16 trong M1–M2 (song song, không cần code) |
| Spike Ink / editor thất bại | Đổi hướng cơ duyên / editor | Spike nêu phương án B nếu thất bại (vd. tự viết editor tối giản; định dạng hội thoại khác Ink) và ghi PM-04 |

---

## 9. Câu hỏi mở
- [ ] **CP-Q-1:** Công cụ quản lý backlog (GitHub Projects / Linear / Jira — PLAN PM-02)? Mặc định: GitHub Projects, mã việc theo CP-R-020.
- [ ] **CP-Q-2:** Mock server OpenAPI cụ thể (M0-TO-04) — chọn và đăng ký TD-15.
- [ ] **CP-Q-3:** Số người thực tế mỗi luồng ở Phase 0 — điều chỉnh §3 và thời gian M0–M1.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: đánh giá sẵn sàng, 5 luồng (SH / GS / API / CL / TO), phụ thuộc + song song, tổ chức team, mốc M0–M4, nội dung theo luồng, quy trình giao việc AI, backlog M0–M1, rủi ro |

