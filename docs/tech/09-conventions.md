# TD-10 — Coding Conventions & Quy trình code bằng AI

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-10 · Prefix ID: `CC` |
| Owner | Tech Lead |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 0 (trước dòng code đầu tiên) |
| Quyết định nền | D-19 (TS cho client + Game Server, Java cho API Service), D-22, [docs/README.md §2–§3](../README.md) (quy ước spec, quy tắc cho AI), [TD-01](00-tdd.md), [TD-07](06-gamedata.md) |
| Liên quan | [TD-01 TDD](00-tdd.md) · [TD-02 Network](01-network.md) · [TD-03 Database](02-database.md) · TD-04 API · TD-05 Client · TD-06 Game Server · TD-08 Bảo mật · [TD-14 Geometry](13-geom2d.md) · TD-15 OSS · [Glossary](../lore/glossary.md) |

> Dự án dùng **AI để viết phần lớn code**. Tài liệu này là "hiến pháp" cho mọi code (người hay AI viết): cấu trúc, đặt tên, xử lý lỗi, kiểm thử, git, và **quy trình giao việc cho AI** để code sinh ra truy được về đúng quy tắc trong spec và đúng một kiểu.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-10 | Mục |
|--------------|-----------------|-----|
| TD-05, TD-06, API Service | Cấu trúc package, luật phụ thuộc, khung code mẫu | §2, §4, §13 |
| Mọi GD / TD | Cách hiện thực quy tắc `-R-` và kiểm thử `-AC-` | §9, §12 |
| CI (TD-01 TA-R-141) | Danh sách cổng kiểm tra | §11 |

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="cc-r-001"></a>**CC-R-001** | **Spec là nguồn sự thật.** Code hiện thực đúng quy tắc trong GD / TD; thiếu hoặc mâu thuẫn → dừng, ghi câu hỏi mở vào tài liệu (README §3), không tự đoán. |
| <a id="cc-r-002"></a>**CC-R-002** | **Truy vết hai chiều:** chỗ hiện thực một quy tắc có comment mã quy tắc (`// LC-R-021: …`); mỗi tiêu chí `-AC-` có ít nhất một test mang mã đó trong tên. |
| <a id="cc-r-003"></a>**CC-R-003** | **Không tự thêm** enum, mã lỗi, mã lý do ledger, kind RNG, loại POI, message, endpoint… ngoài danh sách đóng trong spec / `shared/contracts` / gamedata types. Cần thêm → sửa spec + contract trước, trong cùng PR. |
| <a id="cc-r-004"></a>**CC-R-004** | **Không hằng số gameplay trong code** (TA-R-008): mọi con số cân bằng đọc từ gamedata. |
| <a id="cc-r-005"></a>**CC-R-005** | CI xanh là điều kiện bắt buộc để merge (§11); không tắt kiểm tra để qua CI. |

---

## 2. Repo, gói, phiên bản

```
xproject/
├── client/            @tienlo/client         (Babylon.js, React, Vite)          — TD-05
├── game-server/       @tienlo/game-server    (Colyseus, Node)                   — TD-06
├── shared/
│   ├── sim/           @tienlo/sim            (TS thuần: geom2d, combat, AI, ink runner, worldClock)
│   ├── schema/        @tienlo/schema         (@colyseus/schema state + kiểu message)
│   ├── gamedata-types/ + gói @tienlo/gamedata (accessor có kiểu, ClientGamedata / ServerGamedata) — TD-07
│   ├── contracts/     OpenAPI (public / internal / admin), errors.yaml, rng-kinds.yaml → @tienlo/contracts (TS) + stub Java — TD-04
│   ├── events/        JSON Schema sự kiện domain — TD-01 §10
│   ├── gamedata/ maps/ encounters/ quests/ i18n/ testvectors/
├── api-service/       Gradle (Kotlin DSL), Java 21, Spring Boot 3 — gói gốc vn.tienlo.api
├── tools/             gamedata build / validators / generators, maps build, art (art:check, tracker, anim-list), docs (docs:check), scripts
├── infra/             docker-compose, CI, deploy
└── docs/
```

| Thành phần | Phiên bản / công cụ |
|-----------|---------------------|
| Node.js | 22 LTS (khóa trong `.nvmrc`, `engines`) |
| Package manager | **pnpm** workspaces (khóa phiên bản bằng `packageManager`) |
| TypeScript | 5.x, `strict`, `noUncheckedIndexedAccess`, `exactOptionalPropertyTypes`, `noImplicitOverride`, ESM (`"type": "module"`) |
| Lint / format TS | ESLint flat config + `typescript-eslint` (strict-type-checked) + Prettier |
| Test TS | Vitest; Playwright (trình duyệt) |
| Validate runtime | zod (message, cấu hình, payload ngoài) |
| Java | 21; Spring Boot 3.x; Gradle Kotlin DSL; Spotless (google-java-format) |
| Test Java | JUnit 5, AssertJ, Testcontainers, ArchUnit |
| DB | Flyway, jOOQ (truy vấn nóng), Spring Data JPA (CRUD đơn giản) — TD-03 |

- <a id="cc-r-010"></a>**CC-R-010** Phiên bản phụ thuộc **khóa chính xác** (lockfile commit; không `^` cho thư viện lõi như Babylon.js, Colyseus); nâng cấp qua PR riêng có ghi chú.
- <a id="cc-r-011"></a>**CC-R-011** Thêm thư viện / công cụ / asset / mô hình AI mới → đăng ký TD-15 trước (license).

---

## 3. Đặt tên

| Đối tượng | Quy tắc | Ví dụ |
|-----------|---------|-------|
| Thuật ngữ nghiệp vụ | **Chỉ dùng định danh ở Glossary** (tiếng Anh); không tên biến tiếng Việt không dấu | `lifespanUsedSec`, không `thoNguyen` |
| TS biến / hàm | `camelCase`; hàm động từ trước | `computeStats`, `applyIntent` |
| TS kiểu / class / enum-like | `PascalCase`; union chuỗi `'UPPER_SNAKE'` | `type LifeState = 'ALIVE' \| 'ENDING' \| 'ENDED'` |
| TS file | `kebab-case.ts`; một khái niệm chính mỗi file; test `*.test.ts` cạnh file | `action-state.ts`, `action-state.test.ts` |
| Java class | `PascalCase`; hậu tố vai trò: `…Controller`, `…Service`, `…Repository`, `…Gate`, `…Event`, `…Command`, `…View` | `InventoryGate`, `LifespanChangeCommand` |
| Java package | `vn.tienlo.api.{module}.{layer}` (§4.2) | `vn.tienlo.api.inventory.app` |
| SQL | TD-03 §2 (`snake_case`, số ít) | `item_instance` |
| Sự kiện domain | `{module}.{động_từ_quá_khứ}` | `inventory.item_granted` |
| Message mạng | `C2S_PascalCase` / `S2C_PascalCase` (TD-02) | `C2S_CastSkill` |
| ID gamedata | TD-07 §4.3 | `ITM_TRUC_CO_DAN` |
| Biến môi trường | `TL_{THÀNH_PHẦN}_{TÊN}` | `TL_API_DB_URL`, `TL_GS_PROCESS_ID` |
| Feature flag | `ff.{khu_vực}.{tên}` trong cấu hình | `ff.pvp.enabled` |

- <a id="cc-r-020"></a>**CC-R-020** Tên đơn vị trong tên biến theo TD-07 §4.2 (`…Sec`, `…Ms`, `…Years`, `…Deg`, tỷ lệ là phân số; `…Pp` / `@unit pp` cho điểm phần trăm GD-29).

---

## 4. Cấu trúc & luật phụ thuộc

### 4.1. TypeScript
- <a id="cc-r-030"></a>**CC-R-030** Luật import (kiểm bằng `eslint-plugin-boundaries` hoặc `dependency-cruiser` trong CI):

| Gói | Được import | Cấm import |
|-----|-------------|-----------|
| `@tienlo/sim` | `@tienlo/gamedata` (kiểu), thư viện thuần TS đã duyệt | Babylon.js, Colyseus, DOM, Node API, `Math.random`, `Date.now` (GEO-R-003) |
| `@tienlo/schema` | `@colyseus/schema` | client, game-server |
| `@tienlo/client` | sim, schema, gamedata (Client), contracts | game-server, `ServerGamedata` |
| `@tienlo/game-server` | sim, schema, gamedata (Server), contracts | client, Babylon.js, React |

- <a id="cc-r-031"></a>**CC-R-031** Trong một gói: thư mục theo **domain** (`combat/`, `inventory/`…), mỗi domain có `index.ts` xuất API công khai; domain khác chỉ import qua `index.ts`.
- <a id="cc-r-032"></a>**CC-R-032** Hàm mô phỏng trong `sim` là **thuần**: nhận trạng thái + thời gian + gamedata, trả kết quả; không I/O, không biến toàn cục, không cấp phát trong vòng lặp nóng (GEO-R-080).

### 4.2. Java (API Service)
```
vn.tienlo.api.{module}
 ├─ api/          interface + DTO công khai cho module khác (InventoryGate, ...)   ← module khác CHỈ dùng gói này
 ├─ web/          @RestController, map request ↔ command, không chứa nghiệp vụ
 ├─ app/          use case / service (transaction ở đây), gọi domain + persistence + gate module khác
 ├─ domain/       quy tắc nghiệp vụ thuần (không Spring, không DB) — nơi đặt comment mã -R-
 ├─ persistence/  jOOQ / JPA repository, mapping bảng (TD-03)
 └─ events/       publish outbox, consumer Valkey Streams
```
- <a id="cc-r-033"></a>**CC-R-033** Hướng phụ thuộc: `web → app → domain`; `app → persistence`, `app → {module khác}.api`; `domain` không phụ thuộc Spring / DB. ArchUnit kiểm tra (TA-AC-019, DB-AC-002).
- <a id="cc-r-034"></a>**CC-R-034** Transaction đặt ở `app` (`@Transactional`), một use case một transaction; tuân thứ tự khóa TD-03 DB-R-071; không gọi mạng trong transaction (DB-R-073).

---

## 5. Lỗi & kết quả

- <a id="cc-r-040"></a>**CC-R-040** **Lỗi nghiệp vụ là kết quả, không phải ngoại lệ lạ:**
  - TS (`sim`, game-server): hàm trả `Result<T, E>` với `E` là union mã lỗi có trong `errors.yaml` hoặc lý do từ chối có trong spec (vd. `CastRejectReason` GD-07); ngoại lệ chỉ cho lỗi lập trình / bất biến bị vi phạm.
  - Java: domain ném `BusinessException(ErrorCode)` với `ErrorCode` **sinh từ `errors.yaml`**; `@ControllerAdvice` map sang định dạng lỗi TD-01 TA-R-043 + lưu idempotency (TA-R-052).
- <a id="cc-r-041"></a>**CC-R-041** Không nuốt lỗi (`catch {}` rỗng bị lint chặn); lỗi hạ tầng (DB / Valkey / mạng) → log + metric + trả `503 UNAVAILABLE` hoặc đưa vào hàng đợi thử lại (TA-R-054).
- <a id="cc-r-042"></a>**CC-R-042** Mọi đầu vào từ ngoài (HTTP body, message WS, file cấu hình, gamedata lúc nạp) validate bằng schema (zod / Bean Validation / JSON Schema) **ở biên**, trước khi vào domain.

---

## 6. Số, thời gian, ngẫu nhiên

- <a id="cc-r-050"></a>**CC-R-050** Tiền / số lượng: số nguyên (`number` an toàn ≤ 2^53 ở TS cho linh thạch; `long` ở Java). Không dùng `float` / `double` cho giá trị lưu DB có ý nghĩa kinh tế; hệ số dùng `BigDecimal` ở Java khi lưu, `number` khi mô phỏng.
- <a id="cc-r-051"></a>**CC-R-051** Làm tròn bằng hàm chung `round(x, digits)` half-up (TD-07 DT-R-132), có ở cả TS và Java + testvector.
- <a id="cc-r-052"></a>**CC-R-052** Thời gian: server dùng **đồng hồ inject** (`Clock` ở Java, `TimeSource` ở TS) để test được; trong `sim` thời gian là tham số (tick / ms), không gọi đồng hồ hệ thống. Lưu / truyền UTC ISO-8601 hoặc epoch ms.
- <a id="cc-r-053"></a>**CC-R-053** Ngẫu nhiên: **chỉ** qua `RngService` / `rng.xoshiro128ss` (TD-01 §9) với seed rõ ràng; `Math.random()`, `new Random()`, `ThreadLocalRandom` bị lint / ArchUnit chặn ngoài module RNG.

---

## 7. Log, metric, cấu hình

- <a id="cc-r-060"></a>**CC-R-060** Log JSON có cấu trúc theo TD-01 TA-R-130 (pino ở TS, SLF4J + Logback JSON ở Java); trường `event` là chuỗi cố định dạng `domain.action` (vd. `transfer.ticket_issued`); không log token, mật khẩu, email, nội dung chat riêng.
- <a id="cc-r-061"></a>**CC-R-061** Metric đặt tên `tl_{thành_phần}_{đo}_{đơn_vị}` (vd. `tl_gs_tick_duration_ms`), label hữu hạn (không dùng `characterId` làm label).
- <a id="cc-r-062"></a>**CC-R-062** Cấu hình qua biến môi trường `TL_*` + file mặc định trong repo; bí mật chỉ qua secret manager (TD-08), không commit; cấu hình validate lúc khởi động (fail-fast).

---

## 8. API, sự kiện, dữ liệu
- <a id="cc-r-070"></a>**CC-R-070** Endpoint: sửa `shared/contracts/*.yaml` trước → sinh code → hiện thực (TA-R-020). Controller không viết tay DTO trùng với DTO sinh ra.
- <a id="cc-r-071"></a>**CC-R-071** Thao tác ghi có tác dụng phụ nhận khóa idempotency theo TD-01 §7; dùng helper chung `IdempotencyGuard` (Java) — không tự viết lại logic.
- <a id="cc-r-072"></a>**CC-R-072** Phát sự kiện domain chỉ qua `OutboxPublisher.publish(type, payload)` trong transaction (TA-R-080); payload có JSON Schema ở `shared/events`.
- <a id="cc-r-073"></a>**CC-R-073** Migration DB theo TD-03 §11; mỗi PR đổi schema kèm migration + cập nhật sổ đăng ký TD-03 §5 nếu thêm bảng.

---

## 9. Kiểm thử

| Lớp | Bắt buộc | Quy ước |
|-----|----------|---------|
| Đơn vị (`sim`, `domain`) | Mọi công thức / quy tắc có ví dụ trong spec | Không I/O; dữ liệu từ **bundle gamedata test** (`shared/gamedata/test-fixtures`) |
| Tiêu chí nghiệm thu | **Mỗi `-AC-` ≥ 1 test** (CC-R-002) | Tên test: `{MÃ_AC}_{mô_tả_ngắn}` — TS: `it('LC_AC_003 offline does not age', …)`; Java: `void LC_AC_003_offlineDoesNotAge()` |
| Tích hợp API | Mỗi endpoint: thành công, lỗi nghiệp vụ chính, idempotency lặp lại | Testcontainers (PostgreSQL, Valkey thật) |
| Hợp đồng | Mỗi endpoint khớp OpenAPI | Sinh từ `shared/contracts` |
| Hai ngôn ngữ | Hàm ở TD-07 §11 | `shared/testvectors` chạy ở JUnit + Vitest |
| Game Server | System + message handler với room giả; bot cho luồng chính | TD-06 |
| Tất định | Geometry / RNG | TD-14 GEO-R-091, TA-AC-009 |

- <a id="cc-r-080"></a>**CC-R-080** CI tạo báo cáo **độ phủ AC**: quét mã `-AC-` trong `docs/` và tên test; AC của hệ thống đang làm mà chưa có test → cảnh báo (Phase 1), → lỗi chặn merge (từ Alpha).
- <a id="cc-r-081"></a>**CC-R-081** Test không phụ thuộc thứ tự, không ngủ (`sleep`) chờ thời gian — dùng đồng hồ giả (CC-R-052); test ngẫu nhiên dùng seed cố định.

---

## 10. Comment & tài liệu trong code
- <a id="cc-r-090"></a>**CC-R-090** Comment giải thích **vì sao**, không lặp lại code. Bắt buộc comment mã quy tắc ở chỗ hiện thực (`// SK-R-072: tua lại vị trí mục tiêu …`); nhiều quy tắc → liệt kê.
- <a id="cc-r-091"></a>**CC-R-091** API công khai của gói / module (`index.ts`, gói `api` Java) có JSDoc / Javadoc ngắn trỏ mục spec.
- <a id="cc-r-092"></a>**CC-R-092** Mỗi gói / module có `README.md` ≤ 1 trang: làm gì, spec liên quan, điểm vào chính — giúp AI đọc ít mà hiểu đủ (PLAN §5.7.9 tiết kiệm token).

---

## 11. Git, PR, CI

- <a id="cc-r-100"></a>**CC-R-100** **Trunk-based**: nhánh ngắn từ `main` (`feat/…`, `fix/…`, `chore/…`, `docs/…`), merge bằng squash; `main` luôn triển khai được.
- <a id="cc-r-101"></a>**CC-R-101** Commit theo **Conventional Commits** kèm mã spec: `feat(inventory): grant gate idempotency (IT-R-001, TA-R-051)`.
- <a id="cc-r-102"></a>**CC-R-102** **Mẫu PR** (`.github/pull_request_template.md`) bắt buộc điền:
  - Spec hiện thực: danh sách mã `-R-` và `-AC-`;
  - Đổi hợp đồng? (OpenAPI / events / schema) · Đổi DB? (migration) · Đổi gamedata types?;
  - Test đã thêm (mã AC);
  - Câu hỏi mở phát sinh (đã ghi vào tài liệu nào);
  - Code do AI sinh? (có / không) — nếu có: đã tự review theo checklist §12.4.
- <a id="cc-r-103"></a>**CC-R-103** **Cổng CI** (chặn merge): build + type-check; lint + format; test đơn vị / tích hợp / hợp đồng / testvectors; validator gamedata (TD-07) + bản đồ (TD-13) + asset `art:check` (AR-02 §8.1) + tài liệu playtest `docs:check` (PM-05); kiểm tra ranh giới (CC-R-030, CC-R-033); schema sự kiện (TA-R-086); sổ đăng ký bảng (DB-R-011); license (TD-15); quét lỗ hổng phụ thuộc (TD-08); độ phủ AC (CC-R-080).
- <a id="cc-r-104"></a>**CC-R-104** Review: ít nhất 1 người duyệt cho mọi PR; PR chạm **luật kinh tế, vòng đời, RNG, bảo mật, migration** cần 2 người hoặc Tech Lead.
- <a id="cc-r-105"></a>**CC-R-105** **Lệnh chuẩn** (script gốc `package.json`; CI gọi đúng các lệnh này, không gọi script con trực tiếp). Danh sách này là nguồn cho mục "Lệnh" của `AGENTS.md` (CC-R-110) — thêm / đổi lệnh phải sửa cả hai:

| Lệnh | Việc | Đọc | Spec |
|------|------|-----|------|
| `pnpm install` · `pnpm -r build` · `pnpm -r test` | Cài, build, test toàn workspace TS | — | §2, §9 |
| `pnpm gamedata:build --profile …` · `pnpm gamedata:check [--full]` · `pnpm gamedata:gen` · `pnpm gamedata:explain <id>` · `pnpm gamedata:diff <a> <b>` | Build bundle, validator gamedata (gồm `ar04` / `ar06`), generator, tra cứu | `shared/gamedata/**`, `shared/encounters`, `shared/quests`, `shared/i18n` | TD-07 §8, §12 |
| `pnpm maps:build <mapId\|all>` | Build + validator bản đồ (MF-V-01..17, gồm mẫu biome) | `shared/maps/**`, `art-source/maps/**`, `tools/map-editor/biomes/**` | TD-13 §6 |
| `pnpm art:optimize <file>` | glTF-Transform (Meshopt, KTX2) | file asset | AR-02 §8 |
| `pnpm art:check [--report] [--write] [--json]` | Validator asset: ngân sách TD-11, tên / bộ xương / marker / `extras`, **tracker** `tools/art/asset-tracker.csv`, **catalogue hoạt ảnh** `tools/art/anim-list.json`, **license** (TD-15 §4.5, hồ sơ nguồn gốc), kit môi trường, VFX; `--report` ghi dashboard AR-05 §8.2 | `art/**`, `art/provenance/**`, `tools/art/**`, `art/env/*/kit.json`, `docs/tech/14-oss-register.md` §4.5, bundle gamedata | AR-02 §8.1, AST-R-060; AR-05 §6; AR-03 ANM-R-006; TD-15 OSS-R-051 |
| `pnpm docs:check` | Kiểm tài liệu playtest `docs/pm/playtests/*.md` (bỏ file bắt đầu bằng `_`): tên file theo PM-05 §8; quét chuỗi dạng email / số điện thoại; báo cáo trạng thái `Hoàn thành` phải có build + `gamedataVersion`; phát hiện S1 / S2 có hành động trỏ mã. Script `tools/docs/check-playtests.ts` | `docs/pm/playtests/**` | PM-05 PT-AC-001..004 |
| `cd api-service && ./gradlew check` | Build + test + ArchUnit + Spotless (Java) | `api-service/**` | §4.2 |
| `docker compose -f infra/docker-compose.yml up` | PostgreSQL, Valkey, api, game-server cho dev | `infra/**` | TD-09 |

---

## 12. Quy trình code bằng AI

### 12.1. Tệp ngữ cảnh cho AI
- <a id="cc-r-110"></a>**CC-R-110** Gốc repo có **`AGENTS.md`** (và bản sao / liên kết `CLAUDE.md` nếu công cụ cần) ≤ 2 trang: tóm tắt TD-10 (cấu trúc, lệnh build / test, luật không được vi phạm), thứ tự đọc tài liệu, nơi đặt spec. Mỗi gói có `README.md` (CC-R-092). AI luôn được trỏ tới các tệp này trước.

### 12.2. Thứ tự đọc khi nhận việc
`AGENTS.md` → Glossary → TD-01 (luật chung) → TD-10 → TD liên quan (TD-02 / 03 / 05 / 06 / 07 / 14…) → **GD của hệ thống** → các GD được trỏ trong "Liên quan".

### 12.3. Đơn vị giao việc
- <a id="cc-r-111"></a>**CC-R-111** Mỗi việc giao cho AI = **một lát dọc nhỏ** gắn với một nhóm mã spec cụ thể (vd. "GD-09 IT-R-011 + IT-AC-014..015: gộp chồng trong inventory gate"), không giao "làm cả hệ thống luyện đan".
- <a id="cc-r-112"></a>**CC-R-112** Mẫu yêu cầu chuẩn:
```
Nhiệm vụ: <mô tả 1–2 câu>
Spec: <tài liệu + danh sách mã -R- / -AC- / mục>
Phạm vi file: <thư mục / gói được sửa>
Không được: tự thêm enum / mã lỗi / endpoint / bảng ngoài spec; sửa file ngoài phạm vi
Hoàn thành khi: mỗi -AC- liệt kê có test đặt tên theo mã; CI xanh; comment mã -R- ở chỗ hiện thực
Nếu spec thiếu / mâu thuẫn: dừng, liệt kê câu hỏi, đề xuất ghi vào mục "Câu hỏi mở" của tài liệu
```
- <a id="cc-r-113"></a>**CC-R-113** Thay đổi chéo nhiều module (vd. thêm mã lý do ledger mới) làm thành **PR spec trước** (sửa GD / TD + contracts), rồi mới PR code.

### 12.4. Checklist tự review code AI
1. Mọi mã `-R-` trong yêu cầu có chỗ hiện thực kèm comment; không hiện thực quy tắc nào **không** có trong spec.
2. Không có enum / mã / hằng số gameplay mới ngoài spec / gamedata.
3. Đúng ranh giới gói / module (CC-R-030, CC-R-033); không import chéo trái luật.
4. Thao tác ghi: idempotency, ledger, outbox, transaction đúng chỗ.
5. Không `Math.random` / đồng hồ hệ thống / số thực cho tiền.
6. Test: mỗi `-AC-` có test đúng tên; có test lỗi nghiệp vụ và lặp lại idempotency.
7. Log không chứa dữ liệu nhạy cảm; lỗi không bị nuốt.
8. Không thêm phụ thuộc chưa đăng ký TD-15.

---

## 13. Khung code mẫu

### 13.1. Java — endpoint ghi có idempotency, gate, ledger, outbox
```java
// vn.tienlo.api.lifecycle.web.VaultController
@RestController
@RequestMapping("/v1/accounts/me/vault")
class VaultController implements VaultApi {                      // VaultApi sinh từ public.yaml (CC-R-070)
  private final VaultService service;
  @Override
  public ResponseEntity<VaultView> deposit(String idemKey, VaultDepositRequest req, AuthPrincipal me) {
    return ResponseEntity.ok(service.deposit(me.accountId(), req.characterId(), req.itemInstanceId(), idemKey));
  }
}

// vn.tienlo.api.lifecycle.app.VaultService
@Service
class VaultService {
  private final IdempotencyGuard idem; private final InventoryGate inventory;   // inventory.api (CC-R-033)
  private final ContextChecker context; private final VaultRepository vaults; private final OutboxPublisher outbox;

  @Transactional
  VaultView deposit(UUID accountId, UUID characterId, UUID itemId, String idemKey) {
    return idem.run(idemKey, "vault.deposit", () -> {
      context.require(characterId, ContextRequirement.nearNpcRole("VAULT_KEEPER"));     // TA-R-091, LC-R-111
      var slot = vaults.firstFreeSlot(accountId)
          .orElseThrow(() -> new BusinessException(ErrorCode.NO_SPACE));             // LC-R-110
      inventory.move(MoveCommand.toVault(itemId, accountId, slot, ReasonCode.VAULT_DEPOSIT, idemKey));  // IT-R-001, LC-R-115
      outbox.publish("inventory.item_moved", Map.of("itemInstanceId", itemId, "to", "VAULT"));          // TA-R-080
      return vaults.view(accountId);
    });
  }
}
```

### 13.2. TypeScript — hàm mô phỏng thuần + test theo mã AC
```ts
// shared/sim/src/lifecycle/age.ts
/** Tuổi hiển thị từ thời gian sống đã dùng — GD-26 §6.1. */
export function ageYears(startAge: number, lifespanUsedSec: number, secPerYear: number): number {
  // LC-R-030: tuổi tính từ thời gian sống đã dùng (đã nhân hệ số), không từ đồng hồ thực
  return startAge + lifespanUsedSec / secPerYear;
}

// shared/sim/src/lifecycle/age.test.ts
import { describe, it, expect } from 'vitest';
import { ageYears } from './age';
describe('ageYears', () => {
  it('LC_AC_002 one hour of play equals one year', () => {
    expect(ageYears(16, 3600, 3600)).toBe(17);
  });
});
```

---

## 14. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="cc-ac-001"></a>CC-AC-001 | File trong `shared/sim` import `@babylonjs/core` | CI | Fail (CC-R-030) |
| CC-AC-002 | Gói `web` Java gọi trực tiếp repository | CI | Fail ArchUnit (CC-R-033) |
| CC-AC-003 | Code dùng `Math.random()` ngoài module RNG | Lint | Fail (CC-R-053) |
| CC-AC-004 | PR thêm mã lỗi mới chỉ trong code Java | CI | Fail: `ErrorCode` sinh từ `errors.yaml` không có mã đó (CC-R-003) |
| CC-AC-005 | Hệ thống `inventory` có `IT-AC-004` chưa có test | Báo cáo độ phủ AC | Liệt kê `IT-AC-004` thiếu |
| CC-AC-006 | Commit không theo Conventional Commits | Hook / CI | Cảnh báo (chặn ở nhánh `main`) |
| CC-AC-007 | `catch (e) {}` rỗng | Lint | Fail (CC-R-041) |
| CC-AC-008 | PR không điền mục "Spec hiện thực" trong mẫu PR | CI kiểm tra mô tả PR | Fail |
| CC-AC-009 | Thư viện mới chưa có trong TD-15 | CI license | Fail (CC-R-011) |
| CC-AC-010 | Biến tên `thoNguyen` | Lint (từ điển glossary) | Cảnh báo (CC-R-020, §3) |
| <a id="cc-ac-011"></a>CC-AC-011 | PR thêm `docs/pm/playtests/2027-3-15-phase1-Combat.md` | CI `pnpm docs:check` | Fail (CC-R-105, PM-05 PT-AC-001) |
| <a id="cc-ac-012"></a>CC-AC-012 | PR thêm `art/monsters/MDL_MON_X.glb` không có dòng trong `tools/art/asset-tracker.csv` | CI `pnpm art:check` | Fail (CC-R-105, AR-05 TRK-R-001) |

---

## 15. Câu hỏi mở
- [ ] **CC-Q-1:** Java dùng Spring Modulith hay chỉ ArchUnit cho ranh giới module (= TD-01 TA-Q-4)?
- [ ] **CC-Q-2:** Ngưỡng độ phủ dòng code tối thiểu (vd. 70% cho `sim` / `domain`) có cần ngoài độ phủ AC không?
- [ ] **CC-Q-3:** Công cụ AI chính (và giới hạn dữ liệu được gửi cho dịch vụ AI bên ngoài — mã nguồn, gamedata bí mật `serverOnly`) — cần chính sách riêng ở TD-08.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến truy vết spec, repo + phiên bản, đặt tên, luật phụ thuộc TS / Java, lỗi & kết quả, số / thời gian / RNG, log / metric / cấu hình, API / sự kiện / DB, kiểm thử theo mã AC + báo cáo độ phủ AC, comment, git / PR / CI, **quy trình code bằng AI** (AGENTS.md, thứ tự đọc, đơn vị giao việc, mẫu yêu cầu, checklist), khung code mẫu Java / TS, nghiệm thu |
| v0.2 | 2026-10-25 | — | D-24: Valkey thay Redis (tên khóa / lệnh không đổi) |
| v0.3 | 2026-10-25 | — | CC-R-105 bảng lệnh chuẩn (nguồn cho `AGENTS.md` §5): thêm `pnpm art:check` mở rộng (tracker, anim-list, license, `--report`) và `pnpm docs:check` (kiểm tài liệu playtest, chốt PM-05 PT-Q-4); cổng CI CC-R-103 thêm `art:check`, `docs:check`; CC-AC-011, CC-AC-012 |

