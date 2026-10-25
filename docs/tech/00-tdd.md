# TD-01 — Technical Design Doc (TDD): Kiến trúc tổng

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-01 · Prefix ID: `TA` |
| Owner | Tech Lead (Server) + Tech Lead (Client) |
| Trạng thái | 🟡 Bản nháp v0.8 |
| Cần trước | Phase 0 (trước mọi spike và trước khi viết code hệ thống) |
| Quyết định nền | PLAN D-06 (đã sửa bởi D-19), **D-19** kiến trúc lai, D-16 (không dùng framework game làm nền), D-07 (i18n) |
| Liên quan | [PLAN §5](../../PLAN.md) · [docs/README.md](../README.md) · [Glossary](../lore/glossary.md) · TD-02 Network · TD-03 Database · TD-04 API Spec · TD-05 Client · TD-06 Game Server · TD-07 Gamedata · TD-08 Bảo mật · TD-12 World & Map · TD-14 Geometry 2D · TD-15 OSS Register · toàn bộ GD (mục "Hợp đồng giao tiếp" và "Lưu trữ") |

> Tài liệu này là **luật chung** cho mọi hệ thống. Khi một tài liệu GD nói "idempotent", "ghi `rng_audit`", "sự kiện domain", "Game Server xác nhận vị trí", "API nội bộ"…, cách làm cụ thể nằm ở đây. GD và TD-01 mâu thuẫn → dừng lại, ghi câu hỏi mở, không tự chọn (docs/README.md §3).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-01 | Mục |
|--------------|-----------------|-----|
| Mọi GD | Ranh giới quyền hạn: hệ thống nào chạy ở thành phần nào | §3 |
| Mọi GD có API | Quy ước API, lỗi, idempotency | §6, §7 |
| Mọi GD có ledger / `rng_audit` | Chuẩn ledger, chuẩn RNG + bảng `rng_audit` | §8, §9 |
| Mọi GD có "Sự kiện domain" | Envelope, outbox, Valkey Streams, danh mục sự kiện | §10 |
| GD-09, GD-10, GD-11, GD-13, GD-15, GD-26 | Kiểm tra vị trí / trạng thái chiến đấu cho request REST (`CharacterContext`) | §11 |
| GD-26, GD-28 | Death log, phục hồi sự cố | §12, §13 |
| TD-02 … TD-15 | Khung tổng để chi tiết hóa | toàn bộ |

---

## 1. Mục tiêu

- Một kiến trúc **đủ đơn giản cho team < 10 người** và **đủ chặt cho game một mạng** (mất đồ, chết oan, nhân bản vật phẩm là lỗi nghiêm trọng nhất).
- **Viết code mô phỏng một lần** (TypeScript, `shared/sim`), dùng chung client và Game Server.
- **Một nguồn sự thật** cho mọi dữ liệu có giá trị lâu dài: API Service + PostgreSQL.
- Quy ước đủ cụ thể để **AI sinh code nhất quán** giữa 17+ hệ thống.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="ta-r-001"></a>**TA-R-001** | **API Service là nguồn sự thật duy nhất** cho dữ liệu bền vững. Chỉ API Service ghi PostgreSQL. Game Server, World Service, công cụ đều đi qua API. |
| <a id="ta-r-002"></a>**TA-R-002** | **Client không có quyền quyết định.** Client chỉ gửi ý định (input, lựa chọn, ID). Mọi kết quả do server tính (PLAN §5.3). |
| <a id="ta-r-003"></a>**TA-R-003** | Mọi thao tác ghi có tác dụng phụ (vật phẩm, tiền, tu vi, thọ nguyên, thiên phú, chết) **idempotent** theo khóa do bên gọi cung cấp (§7). |
| <a id="ta-r-004"></a>**TA-R-004** | Mọi thay đổi vật phẩm / tiền tệ / thọ nguyên / tu vi **ghi ledger trong cùng transaction** với thay đổi số dư (§8). |
| <a id="ta-r-005"></a>**TA-R-005** | Mọi roll ngẫu nhiên có giá trị lâu dài **tái lập được** từ seed đã lưu trong `rng_audit` (§9). |
| <a id="ta-r-006"></a>**TA-R-006** | Mỗi miền dữ liệu có **một cổng ghi duy nhất** (§5.2). Không module nào ghi thẳng bảng của module khác. |
| <a id="ta-r-007"></a>**TA-R-007** | Logic mô phỏng (va chạm, công thức chiến đấu, AI, tìm đường, chạy Ink) nằm trong `shared/sim`, **không** viết lại ở client hay Game Server. |
| <a id="ta-r-008"></a>**TA-R-008** | Mọi con số cân bằng nằm trong `shared/gamedata` (TD-07). Code không chứa hằng số gameplay. |

---

## 2. Tổng quan thành phần

```
                         ┌────────────── CDN: asset GLB/KTX2/ogg, gamedata bundle ──────────────┐
                         ▼                                                                       │
┌──────────────────────────────────────────────────────────────┐                                 │
│ CLIENT (trình duyệt) · Babylon.js · TS · Vite · React UI      │                                 │
│  uses shared/sim (dự đoán di chuyển, hitbox debug, Ink xem trước)                               │
└──────┬──────────────────────────────────────┬────────────────┘                                 │
       │ REST/JSON + JWT                      │ WebSocket (Colyseus) + join ticket               │
       ▼                                      ▼                                                  │
┌───────────────────────────┐   REST nội bộ   ┌─────────────────────────────────────┐            │
│ API SERVICE (Spring Boot)  │◄───────────────│ GAME SERVER (Colyseus · Node.js)     │            │
│ modular monolith, §5       │  service token │ N tiến trình; room = MapInstance     │            │
│ - nguồn sự thật, ledger    │                │ uses shared/sim; trạng thái trong RAM│            │
│ - RNG quan trọng, audit    │                │ - ghi CharacterContext (§11)          │            │
│ - outbox → Valkey Streams   │                │ - đọc sự kiện domain (§10)            │            │
└──────┬─────────────┬──────┘                 └──────────┬───────────────┬──────────┘            │
       │             │ XADD (relay outbox)               │ XREADGROUP     │ SET ctx:* / presence  │
       │             ▼                                    ▼                ▼                       │
       │      ┌──────────────────── REDIS 7+ ─────────────────────────────────┐                   │
       │      │ streams evt:* · ctx:{characterId} · session · ticket jti ·    │                   │
       │      │ Colyseus presence/driver · rate limit · cache                 │                   │
       │      └───────────────────────────────────────────────────────────────┘                   │
       ▼                                                                                          │
┌──────────────────┐   ┌───────────────────────────────┐   ┌──────────────────────────┐          │
│ PostgreSQL 16+   │   │ WORLD SERVICE (module của API) │   │ Admin / GM (Refine)      │──────────┘
│ chỉ API ghi      │   │ World Clock, thời tiết, lịch   │   │ gọi API admin            │
└──────────────────┘   └───────────────────────────────┘   └──────────────────────────┘
```

| Thành phần | Công nghệ | Trạng thái | Scale |
|------------|-----------|-----------|-------|
| Client | Babylon.js (bản ổn định mới nhất lúc bắt đầu, ≥ 7), TypeScript, Vite, React + Zustand, i18next | — | — |
| API Service | Java 21, Spring Boot 3.x, Spring Data JPA + jOOQ, Flyway | Stateless | Ngang (nhiều bản sao sau load balancer) |
| Game Server | Node.js LTS, TypeScript, Colyseus (dựng mới theo TD-06 — D-23) | Stateful (RAM) | Nhiều tiến trình, Colyseus Redis driver/presence (dùng với Valkey) |
| World Service | Module trong API Service, chạy **một bản leader** (khóa Valkey) | Tính từ epoch, không cần lưu | 1 leader |
| PostgreSQL | 16+ | Nguồn sự thật | 1 primary + replica đọc (khi cần) |
| Valkey | 7+ | Tạm thời, khôi phục được | 1 (MVP) → Sentinel |

---

## 3. Ranh giới quyền hạn

### 3.1. Ma trận quyết định
- <a id="ta-r-010"></a>**TA-R-010** Mọi tính năng phải chạy đúng thành phần theo bảng dưới. Tính năng mới không khớp hàng nào → ghi ADR (§16).

| Loại việc | Ai quyết định | Ví dụ |
|-----------|---------------|-------|
| Mô phỏng thời gian thực (vị trí, va chạm, hitbox, sát thương, status, AI, aggro) | **Game Server** | Combat GD-06/07, AI GD-12, hiểm địa GD-28 trong bản đồ |
| Roll chiến đấu tức thời (chí mạng, né, xác suất status) | **Game Server** (PRNG theo room, không ghi `rng_audit`; kết quả vào death log) | GD-06 |
| Roll rơi đồ quái | **Game Server** (PRNG ghi seed, gửi kèm `loot/grant`, API ghi `rng_audit` với `source = GAME_SERVER`) | GD-12 |
| Roll có giá trị lâu dài (thiên phú, đột phá, chế tác, cơ duyên, phẩm / thuộc tính phụ vật phẩm) | **API Service** | GD-27, GD-04, GD-08, GD-10, GD-11, GD-09, GD-22 |
| Thay đổi vật phẩm, tiền tệ, tu vi, thọ nguyên, thiên phú, kỹ năng, cái chết | **API Service** qua cổng (§5.2) | Mọi GD |
| Đồng hồ thế giới, thời tiết, lịch sự kiện | **World Service** | GD-21, GD-23 |
| Chạy kịch bản Ink của cơ duyên | **Game Server** (inkjs, seed từ API) → commit outcome qua API | GD-22 |
| Chấm lại minigame chế tác, ghép công pháp | **API Service** (bản Java tham chiếu, cùng golden test với bản TS xem trước, TA-R-011) | GD-10, GD-11, GD-08 |
| Kiểm tra "đứng gần NPC / trạm, không chiến đấu" cho request REST | **API Service** đọc `CharacterContext` do Game Server ghi (§11) | GD-09, GD-10, GD-11, GD-15, GD-26 |
| Hiển thị, dự đoán, nội suy, xem trước | **Client** (không có hiệu lực) | |

- <a id="ta-r-011"></a>**TA-R-011** **Code tất định cần chạy ở cả API Service (Java) và TS** (chấm minigame GD-10/11, ghép công pháp GD-08, công thức thuần thục GD-07): viết **một bản tham chiếu bằng Java trong API Service** (nơi quyết định) + bản TS trong `shared/sim` cho client xem trước; cả hai chạy **cùng bộ golden test** `shared/testvectors/*.json` (đầu vào → đầu ra mong đợi) trong CI. Danh sách hàm thuộc diện này ghi ở [TD-07 §11](06-gamedata.md); mục tiêu giữ danh sách **ngắn** (ưu tiên đặt logic ở đúng một phía).
- <a id="ta-r-012"></a>**TA-R-012** Game Server **không** cache dữ liệu có giá trị (túi đồ, tiền) để tự quyết định. Game Server được giữ **bản sao chỉ đọc** cần cho mô phỏng (chỉ số đã tính, kỹ năng đã trang bị, `CSK_*` skillDef) và làm mới khi nhận sự kiện (§10).

### 3.2. Luồng điển hình

**A. Đánh quái → tu vi + rơi đồ**
```
GS: quái chết (shared/sim) → roll rơi đồ (PRNG room, seed s) → hiện vật phẩm trên đất
GS: người chơi nhặt → POST /internal/v1/loot/grant {grantId, ..., rng:{seed:s, kind:'MONSTER_LOOT'}}
API: inventory gate → item_instance + item_log + rng_audit(source=GAME_SERVER) → outbox inventory.item_granted
GS: gom kill 30s → POST /internal/v1/cultivation/monster-kills {batchId}
```

**B. Request REST cần vị trí (luyện đan)**
```
Client → API POST /v1/characters/{id}/alchemy/sessions        (đường dẫn chuẩn: TD-04)
API: đọc ctx:{characterId} từ Valkey (§11) → kiểm tra POI ALCHEMY_STATION ≤ 3m, !inCombat, tuổi ctx ≤ 2s
API: tạo phiên (transaction), trả seed minigame → Client chơi → Client gửi chuỗi input → API chấm lại
```

**C. Cái chết thật**
```
GS: hấp hối hết giờ → lưu death log (§12) → POST /internal/v1/lifecycle/lethal-event {eventId, deathLogId}
API: lifecycle gate → DIED → outbox lifecycle.life_ending
GS: nhận sự kiện → gỡ thực thể, tạo di hài (POST /internal/v1/danger/corpses)
```

---

## 4. Cấu trúc repo & module

```
xproject/
├── docs/
├── client/                    # Babylon.js + React (pnpm workspace)
├── game-server/               # Colyseus · Node.js · TS (pnpm workspace)
│   └── src/ (chi tiết: TD-06 §2)
├── api-service/               # Spring Boot (Gradle)
│   └── src/main/java/.../{module}/   # §5
├── shared/
│   ├── sim/                   # TS dùng chung client + GS (TA-R-007)
│   ├── schema/                # @colyseus/schema state, kiểu message (TD-02)
│   ├── contracts/             # OpenAPI: public.yaml, internal.yaml, admin.yaml (sinh client TS + stub Java)
│   ├── events/                # JSON Schema cho từng sự kiện domain (§10.4)
│   ├── gamedata/              # JSON + JSON Schema (TD-07)
│   ├── maps/                  # dữ liệu logic bản đồ (TD-13)
│   ├── encounters/            # Ink + YAML cơ duyên (GD-22)
│   └── testvectors/           # golden test hai ngôn ngữ (TA-R-011)
├── tools/                     # validator gamedata, gen-fragments, map editor
└── infra/                     # docker-compose, CI, deploy
```

- <a id="ta-r-020"></a>**TA-R-020** `shared/contracts` là **nguồn sự thật của API**: đổi endpoint = sửa YAML trước, sinh lại code, rồi mới viết logic. CI chặn code lệch hợp đồng (contract test).
- <a id="ta-r-021"></a>**TA-R-021** `shared/sim` **không** import Babylon.js, Colyseus, DOM hay Node API: chỉ TS thuần + gamedata, chạy được trong trình duyệt, Node.js và test.

---

## 5. API Service: modular monolith

### 5.1. Module

| Module | GD sở hữu | Bảng chính |
|--------|-----------|-----------|
| `account` | GD-14 (tài khoản), auth | `account`, `session` |
| `character` | GD-27 (tạo nhân vật), GD-06 (stat-inputs) | `character` |
| `lifecycle` | GD-26 | `lifespan_ledger`, `life_record`, `vault_*` |
| `talent` | GD-27 | `character_talent`, `talent_change_log` |
| `cultivation` | GD-04 | `cultivation_ledger`, `breakthrough_attempt` |
| `technique` | GD-07, GD-08 | `character_technique`, `crafted_technique` |
| `inventory` | GD-09 (+ tiền tệ GD-13) | `item_instance`, `item_log`, `currency_wallet`, `currency_ledger`, `mail` |
| `craft` | GD-10, GD-11 | `craft_session`, `profession`, `recipe_known` |
| `economy` | GD-13 | shop, auction |
| `social` | GD-14 | sect, friendship, report |
| `quest` | GD-15 | `quest_progress` |
| `encounter` | GD-22 | `encounter_session`, cờ |
| `danger` | GD-28 | `danger_run`, corpse |
| `world` | GD-21, World Service | `world_directory` (Valkey), `discovered_poi`, fog |
| `home` | GD-30 | cave, sect site |
| `beast` | GD-25 | `beast`, `beast_breeding`, `beast_log` |
| `pvp` | GD-32 | `pk_state`, `pvp_kill_log` |
| `platform` | TD-01 | `idempotency_record`, `outbox_event`, `rng_audit`, `death_log` (DDL: TD-03 §6) |
| `security` | TD-08 | `security_flag`, `sanction`, `login_attempt`, `purchase_receipt`, `appeal` |
| `monetization` | GD-19 | `cosmetic_entitlement`, `character_appearance`, `season_pass_progress`, `payment_order`, `spend_limit`, `gift_log` |
| `admin` | GM, Death Review | (gọi các module khác) |

### 5.2. Cổng ghi (TA-R-006)

| Cổng | Module | Định nghĩa |
|------|--------|-----------|
| Inventory gate (`grant`, `consume`, `move`, `currency/change`) | `inventory` | GD-09 IT-R-001 |
| Cultivation gate (`cultivation/exp`) | `cultivation` | GD-04 CU-R-001 |
| Lifecycle gate (`lifespan-change`, `lethal-event`) | `lifecycle` | GD-26 |
| Talent gate (`talents/change`) | `talent` | GD-27 TLN-R-006 |
| Technique gate (`learn`, `mastery`) | `technique` | GD-07 |
| Quest gate (`QuestAction`) | `quest` | GD-15 |

- <a id="ta-r-030"></a>**TA-R-030** Module gọi module khác qua **interface Java công khai** của module đó (package `api`), trong **cùng transaction** khi cần nguyên tử (vd. chế tác = tiêu nguyên liệu + phí + thọ nguyên + tạo vật phẩm trong 1 transaction). Không truy cập repository / bảng của module khác. Kiểm tra bằng ArchUnit (hoặc Spring Modulith) trong CI.
- <a id="ta-r-031"></a>**TA-R-031** Phản ứng **không cần nguyên tử** (cập nhật nhiệm vụ khi nhặt đồ, thông báo, telemetry) đi qua **sự kiện domain** (§10), không gọi trực tiếp.

---

## 6. Quy ước API

### 6.1. Ba bề mặt

| Bề mặt | Prefix | Người gọi | Xác thực |
|--------|--------|-----------|----------|
| Công khai | `/v1/...` | Client | JWT truy cập (15 phút) + refresh token (30 ngày, xoay vòng); `characterId` trong path phải thuộc tài khoản |
| Nội bộ | `/internal/v1/...` | Game Server, World Service | Service token (JWT ký bằng khóa riêng, `aud = internal`, 1 giờ) + mạng nội bộ; **không** mở ra internet |
| Quản trị | `/admin/v1/...` | GM panel | JWT có vai trò `GM`/`ADMIN` + ghi `admin_audit` |

### 6.2. Định dạng
- <a id="ta-r-040"></a>**TA-R-040** JSON, `camelCase`; thời gian ISO-8601 UTC (`2026-10-22T05:00:00Z`); tiền tệ và số lượng là số nguyên (`long`); hệ số / tỷ lệ là số thực đã làm tròn theo GD; ID gamedata là chuỗi (`ITM_*`, `TAL_*`); ID thực thể là UUIDv7 (§6.4).
- <a id="ta-r-041"></a>**TA-R-041** Phân trang: `?cursor=&limit=` (≤ 100), trả `{ items, nextCursor }`.
- <a id="ta-r-042"></a>**TA-R-042** Header bắt buộc: `X-Request-Id` (UUID, client sinh; dùng truy vết, khác khóa idempotency), `traceparent` (W3C, OpenTelemetry). Game Server chuyển tiếp `X-Request-Id` của hành động gốc khi có.

### 6.3. Lỗi
- <a id="ta-r-043"></a>**TA-R-043** Mọi lỗi trả:
```json
{ "error": { "code": "INSUFFICIENT", "message": "Không đủ linh thạch", "messageKey": "err.insufficient", "details": { "currency": "SPIRIT_STONE", "need": 100, "have": 40 } }, "requestId": "..." }
```

| HTTP | Khi nào | Mã chung |
|------|---------|----------|
| 400 | Sai cú pháp / thiếu trường | `VALIDATION` |
| 401 / 403 | Chưa xác thực / không có quyền / nhân vật không thuộc tài khoản | `UNAUTHENTICATED`, `FORBIDDEN` |
| 404 | Không tồn tại | `NOT_FOUND` |
| 409 | Vi phạm trạng thái nghiệp vụ | `INSUFFICIENT`, `ALREADY_KNOWN`, `NO_SPACE`, `TAKEN`, `SOLD`, `PRICE_CHANGED`, `NAME_TAKEN`, `NO_ROLLS_LEFT`, `ALREADY_HAS_TALENT`, `INVALID_STATE`, `NOT_NEAR`, `IN_COMBAT`, `CONTEXT_STALE`, `CHANCE_CHANGED` (GD-29 RF-R-022) |
| 422 | Cùng khóa idempotency nhưng nội dung khác | `IDEMPOTENCY_MISMATCH` |
| 423 | Nhân vật đang bị khóa (đang xử lý chết, đang chuyển bản đồ, `ENDING`) | `CHARACTER_LOCKED` |
| 429 | Rate limit | `RATE_LIMITED` |
| 503 | Phụ thuộc lỗi tạm thời, thử lại được | `UNAVAILABLE` |

- <a id="ta-r-044"></a>**TA-R-044** Mã lỗi nghiệp vụ là **danh sách đóng** trong `shared/contracts/errors.yaml` (danh mục đầy đủ: **TD-04 §6** — bảng ở trên chỉ là ví dụ); GD thêm mã mới phải thêm vào đây. Client hiển thị qua `messageKey` (i18n), không dùng `message`.

### 6.4. Định danh
| Loại | Dạng | Sinh ở |
|------|------|--------|
| Thực thể bền vững (account, character, item_instance, session, run…) | UUIDv7 | API Service |
| ID gamedata | `PREFIX_TEN` (TD-07) | Designer |
| ID công khai ngắn (`CSK_*`, mã tông môn) | Base32 Crockford, 10 ký tự | API Service |
| Thực thể runtime trong room (quái, đạn, vật phẩm rơi chưa nhặt) | Số nguyên 32-bit theo room | Game Server |
| Sự kiện domain, khóa idempotency | UUIDv7 (khóa có tiền tố, §7) | Bên phát / bên gọi |

---

## 7. Idempotency

- <a id="ta-r-050"></a>**TA-R-050** Khóa idempotency do **bên gọi** sinh, dạng `{loại}:{uuidv7}` (≤ 80 ký tự), vd. `grant:018f…`, `kill:018f…`, `craft:018f…`. Tên trường theo GD (`grantId`, `batchId`, `changeId`, `eventId`, `requestId`…) — đều là khóa idempotency. Ở **API công khai**, khóa gửi qua header `Idempotency-Key` (TD-04); ở API nội bộ là trường trong body theo GD (`grantId`, `batchId`…).
- <a id="ta-r-051"></a>**TA-R-051** Xử lý trên API Service:
  1. Bắt đầu transaction; `INSERT INTO idempotency_record (key, endpoint, request_hash, status='IN_PROGRESS')` — trùng khóa:
     - đã `DONE` và `request_hash` khớp → trả lại `response` đã lưu (HTTP gốc), **không** xử lý lại;
     - `request_hash` khác → `422 IDEMPOTENCY_MISMATCH`;
     - `IN_PROGRESS` (request song song) → chờ khóa hàng (`SELECT … FOR UPDATE`) rồi áp dụng quy tắc trên.
  2. Thực hiện nghiệp vụ, ghi ledger (ledger cũng có ràng buộc unique theo khóa, lớp bảo vệ thứ hai).
  3. Lưu `response`, `status = DONE`, commit.
- <a id="ta-r-052"></a>**TA-R-052** Lỗi nghiệp vụ 4xx **cũng được lưu** (gửi lại trả đúng lỗi đó). Lỗi 5xx / timeout **không** lưu: transaction rollback, bên gọi thử lại cùng khóa.
- <a id="ta-r-053"></a>**TA-R-053** Giữ `idempotency_record` 7 ngày (job dọn). Bên gọi không được dùng lại khóa sau 7 ngày.
- <a id="ta-r-054"></a>**TA-R-054** **Bên gọi phải thử lại** với cùng khóa khi gặp timeout / 503: Game Server dùng hàng đợi thử lại (backoff 0,5s → 30s, tối đa 24h cho thao tác quan trọng, lưu trong Valkey `gs:retry:{processId}` để sống qua khởi động lại tiến trình). Client thử lại tối đa 3 lần rồi báo lỗi.
- <a id="ta-r-055"></a>**TA-R-055** Request chỉ đọc (`GET`) và xem trước (`preview`) không cần khóa.

---

## 8. Chuẩn ledger

- <a id="ta-r-060"></a>**TA-R-060** Mọi bảng ledger có các cột chung:

| Cột | Ý nghĩa |
|-----|---------|
| `id BIGSERIAL` | |
| chủ thể (`character_id` / `owner_type`+`owner_id` / `item_instance_id`) | |
| `delta` (hoặc `qty_delta`, `years`) | Có dấu |
| `balance_after` (khi có số dư) | Kiểm tra nhất quán |
| `reason_code VARCHAR(32)` | Danh sách đóng của ledger đó (vd. GD-09 IT-R-100) |
| `ref_id VARCHAR(80)` | ID nghiệp vụ liên quan (sessionId, runId, encounterId…) |
| `idempotency_key VARCHAR(80)` | Unique (cùng chủ thể nếu một khóa chạm nhiều chủ thể) |
| `created_at TIMESTAMPTZ` | |

| Ledger | Định nghĩa | Mã lý do |
|--------|-----------|----------|
| `item_log`, `currency_ledger` | GD-09 §10 | GD-09 IT-R-100 (`currency_ledger` dùng GD-13 `CurrencyReason`) |
| `lifespan_ledger` | GD-26 §9 | GD-26 `LifespanReason` |
| `cultivation_ledger` | GD-04 §10 | GD-04 `ExpSource` |
| `talent_change_log` | GD-27 §10 | GD-27 `TalentSource` |

- <a id="ta-r-061"></a>**TA-R-061** Ledger **chỉ thêm, không sửa / xóa**. Sửa sai bằng bút toán bù (`GM_ADJUST`, `GM_REMOVE`). Partition theo tháng cho bảng lớn (`item_log`, `currency_ledger`); giữ ≥ 12 tháng online.
- <a id="ta-r-062"></a>**TA-R-062** Job đối soát hằng ngày: `Σ delta = số dư` cho mỗi ví; `item_instance.quantity` khớp tổng `item_log`; lệch → cảnh báo (không tự sửa).

---

## 9. Chuẩn RNG & `rng_audit`

### 9.1. Thuật toán
- <a id="ta-r-070"></a>**TA-R-070** Mỗi **thao tác** cần ngẫu nhiên mở một luồng RNG:
  - `seed` 128-bit lấy từ `SecureRandom` (API) / `crypto.randomBytes` (GS);
  - luồng số từ **xoshiro128\*\*** khởi tạo bằng seed (qua SplitMix64) — cài đặt giống hệt ở Java và TS, có golden test (TA-R-011);
  - `nextFloat()` = 24 bit cao / 2²⁴ ∈ [0, 1); `nextInt(n)` dùng rejection sampling (không dùng `% n`).
- <a id="ta-r-071"></a>**TA-R-071** Thứ tự rút số trong một thao tác **cố định theo spec** (GD mô tả thứ tự bước). Cùng seed + cùng đầu vào + cùng phiên bản gamedata ⇒ cùng kết quả (vd. GD-08 TF-AC-010, GD-10 chấm lại minigame).
- <a id="ta-r-072"></a>**TA-R-072** Roll chiến đấu ở Game Server dùng một luồng PRNG mỗi room (seed lúc tạo room), **không** ghi `rng_audit` (quá nhiều); death log ghi kết quả (§12).

### 9.2. Bảng `rng_audit`
```sql
CREATE TABLE rng_audit (
  id              BIGSERIAL,
  kind            VARCHAR(40) NOT NULL,          -- danh mục §9.3
  source          VARCHAR(16) NOT NULL,          -- API_SERVICE | GAME_SERVER
  account_id      UUID, character_id UUID,
  ref_id          VARCHAR(80),                   -- attemptId, sessionId, grantId...
  seed            BYTEA NOT NULL,                -- 16 byte
  gamedata_version VARCHAR(40) NOT NULL,         -- TD-07
  inputs          JSONB NOT NULL,                -- xác suất / trọng số / chỉ số đầu vào đã dùng
  outcome         JSONB NOT NULL,                -- kết quả
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);
CREATE INDEX ix_rng_char ON rng_audit(character_id, created_at);
CREATE INDEX ix_rng_ref ON rng_audit(ref_id);
```
- <a id="ta-r-073"></a>**TA-R-073** Một thao tác ghi **một hàng cho mỗi `kind`** (một thao tác có thể có nhiều kind, vd. chế tạo công pháp: `TECHNIQUE_Q` + `TECHNIQUE_MUTATION`), cùng `ref_id`, ghi trong cùng transaction với kết quả. Giữ 18 tháng (đủ cho Death Review, khiếu nại).
- <a id="ta-r-074"></a>**TA-R-074** Công cụ GM `rng-replay`: nhập `ref_id` → chạy lại bằng seed + gamedata version → so khớp `outcome`.

### 9.3. Danh mục `kind`
Danh sách đóng trong `shared/contracts/rng-kinds.yaml`; GD thêm kind phải thêm vào đây.

| Nhóm | Kind | GD |
|------|------|----|
| Tạo nhân vật / thiên phú | `CREATION_ROOT`, `CREATION_INNATE`, `CREATION_TALENT`, `AWAKEN_OFFER`, `FUSION` | GD-27 |
| Tu luyện | `BREAKTHROUGH`, `HEART_DEMON_ROLL`, `HEART_TRIAL_PICK` | GD-04 |
| Cơ duyên | `ENC_TRIGGER`, `ENC_PICK` | GD-22 |
| Vật phẩm | `ITEM_GRADE`, `ITEM_AFFIX`, `CONTAINER_OPEN`, `MONSTER_LOOT` | GD-09, GD-12 |
| Luyện đan | `ALCHEMY_SUCCESS`, `ALCHEMY_QUALITY`, `ALCHEMY_PATTERN`, `ALCHEMY_YIELD`, `ALCHEMY_DISTURB` | GD-10 |
| Luyện khí | `FORGE_SUCCESS`, `FORGE_QUALITY`, `FORGE_AFFIX`, `FORGE_SPIRIT`, `FORGE_RHYTHM`, `ENHANCE`, `REROLL`, `UPGRADE` | GD-11 |
| Chế công pháp | `TECHNIQUE_Q`, `TECHNIQUE_MUTATION`, `TECHNIQUE_MUTATION_PICK` | GD-08 |
| Linh thú | `BEAST_LINH_CHUNG` (GS), `BEAST_CAPTURE`, `BEAST_BOSS_CAPTURE`, `BEAST_HATCH`, `BEAST_BREED_SPECIES`, `BEAST_BREED_INHERIT`, `BEAST_MUTATION`, `BEAST_MUTATION_KIND` | GD-25 |
| Sự kiện thế giới | `WE_SCHEDULE`, `WE_REWARD` | GD-23 |

(`ITEM_GRADE`, `ITEM_AFFIX`, `CONTAINER_OPEN`, `MONSTER_LOOT` là tên chuẩn hóa cho các roll GD-09 IT-R-004 / GD-12 chưa đặt tên kind.)

---

## 10. Sự kiện domain

### 10.1. Envelope
```ts
interface DomainEvent<T> {
  eventId: string;            // UUIDv7
  type: string;               // "inventory.item_granted" — {module}.{động_từ_quá_khứ}
  version: number;            // phiên bản schema payload, bắt đầu 1
  occurredAt: string;         // ISO UTC
  producer: 'API_SERVICE' | 'GAME_SERVER' | 'WORLD_SERVICE';
  accountId?: string; characterId?: string;   // để lọc nhanh
  causationId?: string;       // khóa idempotency / eventId gây ra
  payload: T;                 // theo GD (mục "Sự kiện domain")
}
```

### 10.2. Vận chuyển
- <a id="ta-r-080"></a>**TA-R-080** **API Service dùng transactional outbox:** sự kiện được `INSERT INTO outbox_event` **trong cùng transaction** với thay đổi nghiệp vụ. Relay (job trong API Service, 1 leader) đọc outbox theo thứ tự `id`, `XADD` vào Valkey Stream, đánh dấu `published_at`. Đảm bảo **ít nhất một lần**.
- <a id="ta-r-081"></a>**TA-R-081** Stream theo module: `evt:{module}` (vd. `evt:inventory`, `evt:lifecycle`), `MAXLEN ~ 1.000.000`. Outbox giữ 7 ngày để phát lại.
- <a id="ta-r-082"></a>**TA-R-082** **Game Server phát sự kiện trực tiếp** (`XADD`, không có outbox) **chỉ cho dữ liệu chấp nhận mất khi tiến trình sập**: `monster.killed`, `monster.elite_spawned`, `boss.defeated`, telemetry. Mọi thứ có giá trị (tu vi, vật phẩm, chết) đi qua **API nội bộ đồng bộ** (§3), không qua sự kiện.
- <a id="ta-r-083"></a>**TA-R-083** **Người nhận:**
  - module trong API Service: consumer group `api.{module}` (chia tải giữa các bản sao);
  - mỗi tiến trình Game Server: consumer group riêng `gs.{processId}` (nhận **tất cả**, tự lọc theo nhân vật / bản đồ đang chứa). Đủ cho MVP; khi > 20 tiến trình xem lại (ADR).
- <a id="ta-r-084"></a>**TA-R-084** Người nhận **phải idempotent** theo `eventId` (bảng `consumed_event(consumer, event_id)` giữ 7 ngày, hoặc thao tác tự nhiên idempotent), và **không giả định thứ tự giữa các stream**. Trong một stream, thứ tự theo `id` được giữ cho cùng `characterId`.
- <a id="ta-r-085"></a>**TA-R-085** Xử lý lỗi ở người nhận: thử lại 5 lần (`XPENDING` + `XCLAIM`), sau đó chuyển `evt:dlq` + cảnh báo.

### 10.3. Phản ứng bắt buộc của Game Server

| Sự kiện | Game Server làm gì |
|---------|-------------------|
| `stats.inputs_changed`, `inventory.equipment_changed`, `talent.changed`, `technique.learned`, `technique.main_changed`, `cultivation.stage_up` | Gọi `GET /internal/v1/characters/{id}/stat-inputs` (GD-06), tính lại chỉ số trong `shared/sim` |
| `lifecycle.last_light_started`, `lifecycle.life_ending` | Áp buff / gỡ thực thể, chuyển client sang màn tổng kết |
| `cultivation.breakthrough_succeeded` | Hiệu ứng, cập nhật cảnh giới hiển thị |
| `broadcast.requested` | Phát thông báo toàn server tới client trong room (GD-14 SO-R-060) |
| `site.state_changed` | Cập nhật bản đồ có sơn môn (GD-30) |

### 10.4. Danh mục
- <a id="ta-r-086"></a>**TA-R-086** Mỗi `type` có JSON Schema ở `shared/events/{type}.v{version}.json`; CI kiểm tra mọi sự kiện được phát đều có schema, và mọi `type` được nêu trong GD đều có schema. Đổi payload không tương thích → tăng `version`, phát song song hai phiên bản trong một bản phát hành.

Danh mục hiện có (từ các GD): `lifecycle.*` (5), `talent.*` (3), `cultivation.*` (4), `stats.inputs_changed`, `technique.*` (4), `inventory.*` (2), `currency.changed`, `craft.*` (2), `forge.item_changed`, `monster.*` / `boss.defeated` (3), `party.changed`, `broadcast.requested`, `quest.completed`, `encounter.*` (3), `danger.*` (3), `site.state_changed`, `conclave.completed`, `prestige.awarded`. Tên đầy đủ: mục "Sự kiện domain" của từng GD.

---

## 11. `CharacterContext`: kiểm tra vị trí cho request REST

Nhiều GD yêu cầu "Game Server xác nhận đứng gần NPC / trạm, không chiến đấu" cho request đi thẳng từ client tới API Service. Cách làm chuẩn (không gọi đồng bộ API → Game Server):

```ts
interface CharacterContext {              // Valkey key ctx:{characterId}, TTL 10s
  characterId: string;
  processId: string; roomId: string; mapId: string; channel: number;
  pos: [number, number];                  // (x, z) mét
  inCombat: boolean; alive: boolean; dangerRunId: string | null;
  nearPoiIds: string[];                   // POI trong bán kính tương tác (≤ 5m), tính bằng shared/sim
  updatedAt: number;                      // epoch ms (đồng hồ server)
  seq: number;                            // tăng dần
}
```
- <a id="ta-r-090"></a>**TA-R-090** Game Server ghi `ctx:{characterId}` **khi có thay đổi** của `mapId`, `inCombat`, `alive`, `dangerRunId`, `nearPoiIds` (tối đa 4 lần / giây) và **heartbeat mỗi 3s**.
- <a id="ta-r-091"></a>**TA-R-091** API Service kiểm tra bằng hàm chung `requireContext(characterId, { poiKind?, poiId?, radius?, notInCombat?, notInDanger? })`:
  - không có ctx hoặc `now − updatedAt > 2000ms` → `409 CONTEXT_STALE` (client thử lại sau 0,5s);
  - POI yêu cầu không có trong `nearPoiIds` (hoặc khoảng cách `pos` tới POI theo `shared/maps` > `radius`) → `409 NOT_NEAR`;
  - `inCombat` khi cấm → `409 IN_COMBAT`.
- <a id="ta-r-092"></a>**TA-R-092** Áp dụng cho: GD-09 (trang bị, cửa hàng), GD-10/11 (trạm), GD-08 (`INSIGHT_STATION`), GD-15 (nhận / trả nhiệm vụ NPC), GD-26 (Tàng Bảo Các, bế quan), GD-13 (đấu giá tại NPC nếu có). GD mới cần kiểm tra vị trí dùng hàm này, không tự nghĩ cách khác.
- <a id="ta-r-093"></a>**TA-R-093** Rủi ro chấp nhận: ctx trễ ≤ 2s có thể cho phép thao tác ngay sau khi vào chiến đấu; các thao tác này không ảnh hưởng an toàn tính mạng nên chấp nhận được. Thao tác **ảnh hưởng tính mạng** (đột phá, bí pháp tiêu thọ) do Game Server khởi tạo qua API nội bộ (GD-04), không qua `CharacterContext`.

---

## 12. Death log

- <a id="ta-r-100"></a>**TA-R-100** Game Server giữ **bộ đệm vòng 60s** cho mỗi nhân vật đang ở vùng có thể chết thật (hiểm địa, thiên kiếp): sát thương nhận (nguồn, kỹ năng, số, chí mạng), status, vị trí mỗi 250ms, input nhận được, RTT, trạng thái kết nối, tick server.
- <a id="ta-r-101"></a>**TA-R-101** Khi phát sinh `LethalEvent` (GD-26): Game Server nén (gzip JSON) và gửi `POST /internal/v1/death-logs` → `{ deathLogId }` **trước** khi gửi `lethal-event`. Gửi thất bại sau 3 lần → vẫn gửi `lethal-event` với `deathLogId = null` (GD-26 LC-R-074 gắn `needsReview`).
- <a id="ta-r-102"></a>**TA-R-102** Bảng `death_log (id UUID, character_id, map_id, cause, payload BYTEA, size_bytes, created_at)`; giữ 18 tháng. Death Review UI (admin) phát lại timeline.

---

## 13. Sự cố & phục hồi

| Sự cố | Hành vi |
|-------|---------|
| **API Service không phản hồi** (với Game Server) | Game Server tiếp tục mô phỏng; thao tác quan trọng vào hàng đợi thử lại (TA-R-054); **chặn** hành động cần kết quả ngay (mở rương, chuyển bản đồ, vào hiểm địa) với thông báo "Thiên địa hỗn loạn, chờ giây lát"; không phát sinh chết thật mới khi API mất > 10s (GS tạm dừng đếm hấp hối, xem GD-28 sự cố server) |
| **Tiến trình Game Server sập** | Nhân vật trong các room đó: lần đăng nhập sau nạp từ API (vị trí cuối đã lưu, tối đa 60s trước). Hiểm địa: GD-28 "sự cố server tính là thoát" (`force-extract SERVER_RECOVERY`). Sự kiện GS-only trong khoảng đó có thể mất (TA-R-082) |
| **Valkey mất** | Mất ctx (request REST bị `CONTEXT_STALE` tới khi GS ghi lại), mất phiên Colyseus (client kết nối lại), tổ đội khôi phục theo GD-14. Outbox giữ sự kiện trong PostgreSQL → relay phát lại khi Valkey về |
| **PostgreSQL mất** | API trả 503; GS như dòng "API không phản hồi" |
| **Triển khai phiên bản mới** | API: rolling. GS: "drain" — tiến trình cũ không nhận room mới, thông báo bảo trì 5 phút, lưu toàn bộ trạng thái, chuyển người chơi sang tiến trình mới (handoff TD-12) |

- <a id="ta-r-110"></a>**TA-R-110** Game Server lưu trạng thái nhân vật (vị trí, HP/MP, cooldown dài, fog, buff có thời hạn) về API qua `POST /internal/v1/characters/{id}/snapshot` **mỗi 30s**, khi rời bản đồ, khi đăng xuất, khi drain. Snapshot có `seq` tăng dần; API bỏ qua snapshot cũ hơn. Snapshot gồm cả `combatPower` (cache cho Thiên Bảng, GD-14 SO-R-04I).
- <a id="ta-r-111"></a>**TA-R-111** Dữ liệu gom theo lô (kill tu vi, thuần thục, tuổi, codex, pity) gửi mỗi 30–60s theo GD tương ứng, idempotent theo `batchId`/`reportId`; khi sập tối đa mất một lô (chấp nhận được, trừ tuổi: GD-26 tính lại từ `lastAgeReportAt`).

---

## 14. Kết nối & phiên chơi

```
1. Client đăng nhập → JWT
2. Client POST /v1/play/join { characterId } → API kiểm tra nhân vật ALIVE, không bị khóa
   → World Directory chọn MapInstance (TD-12) → trả { endpoint, roomId, joinTicket }
3. joinTicket = JWT ký bởi API (aud = game, sub = characterId, roomId, jti, exp = 60s — GD-21 ticketTtlSec)
4. Client kết nối Colyseus joinById(roomId, { ticket }) → room.onAuth: kiểm tra chữ ký, roomId,
   SET NX ticket:{jti} (dùng một lần) → GET /internal/v1/characters/{id}/session-load → vào room
5. Chuyển bản đồ: GS hiện tại lưu snapshot → xin ticket mới qua /internal/v1/world/transfer → client sang room đích
```
- <a id="ta-r-120"></a>**TA-R-120** Một nhân vật chỉ ở **một room** tại một thời điểm: khóa Valkey `charlock:{characterId}` (chủ = roomId, gia hạn theo heartbeat). Đăng nhập lần hai → phiên cũ bị ngắt.
- Chi tiết message, tick, prediction: **TD-02**. Directory, handoff, kênh: **TD-12**. Cấu trúc client: **TD-05**; cấu trúc Game Server và thứ tự system trong tick: **TD-06**.

---

## 15. Quan sát & vận hành

- <a id="ta-r-130"></a>**TA-R-130** Log JSON có cấu trúc: `ts, level, service, processId, requestId, traceId, accountId?, characterId?, event, msg`. Không log token, mật khẩu, nội dung chat riêng.
- <a id="ta-r-131"></a>**TA-R-131** Trace OpenTelemetry xuyên Client → API, GS → API (header `traceparent`).
- <a id="ta-r-132"></a>**TA-R-132** Metric tối thiểu: API (p95 theo endpoint, lỗi theo mã, độ trễ relay outbox, kích thước outbox chưa phát); GS (thời gian tick p99 theo room — ngân sách 50ms, mục tiêu p99 < 25ms; số người / room; độ dài hàng đợi thử lại; ctx ghi / giây); Valkey (bộ nhớ, độ trễ stream); PostgreSQL (kết nối, khóa chờ).
- <a id="ta-r-133"></a>**TA-R-133** Cảnh báo bắt buộc: tick p99 > 40ms trong 1 phút; outbox chưa phát > 30s; hàng đợi thử lại GS > 1.000; `evt:dlq` có phần tử; job đối soát ledger lệch. Danh mục cảnh báo đầy đủ + SLO: [TD-09 §4](08-infra-ops.md).
- Telemetry gameplay (các mục "Telemetry" trong GD) gửi về PostHog / bảng sự kiện theo GD-20, **tách** khỏi sự kiện domain.

---

## 16. Kiểm thử & quy trình

| Lớp | Công cụ | Bắt buộc |
|-----|---------|----------|
| `shared/sim` | Vitest | Mọi công thức GD có ví dụ → test (mã `-AC-`) |
| Hai ngôn ngữ | `shared/testvectors` chạy ở JUnit + Vitest | TA-R-011, RNG (TA-R-070) |
| API Service | JUnit 5 + Testcontainers (PostgreSQL, Valkey thật) | Mỗi `-AC-` của GD → ≥ 1 test, tên chứa mã (`TF_AC_008_...`) |
| Hợp đồng | Sinh từ `shared/contracts`; test hợp đồng (Schemathesis hoặc tương đương) | Mỗi endpoint |
| Game Server | Vitest + room Colyseus test; bot headless | Luồng §3.2 |
| Tích hợp | docker-compose (API + GS + PG + Valkey) + bot | Trước mỗi bản phát hành |
| Tải | k6 + bot Colyseus | 150 người / room; **400 người / tiến trình (mềm), 600 (cứng)** — số chuẩn ở [TD-11](10-performance.md) |

- <a id="ta-r-140"></a>**TA-R-140** Thay đổi kiến trúc (thêm thành phần, đổi ranh giới §3, đổi cơ chế vận chuyển) phải có **ADR** ở `docs/pm/adr/NNNN-ten.md` (bối cảnh, quyết định, hệ quả), liên kết từ Decision Log (PM-04).
- <a id="ta-r-141"></a>**TA-R-141** CI chặn merge khi: test đỏ; validator gamedata lỗi (TD-07); code lệch `shared/contracts`; thiếu schema sự kiện; vi phạm ranh giới module (TA-R-030); license chưa đăng ký (TD-15). Danh sách cổng đầy đủ: TD-10 CC-R-103; kiểm tra license + SBOM: TD-15 §3; quét bí mật / lỗ hổng / SAST: TD-08 §7, §10.

---

## 17. Triển khai

> Chi tiết môi trường, pipeline phát hành, rollback, quan sát, backup / DR, runbook: [TD-09 Hạ tầng & Vận hành](08-infra-ops.md). Bảng dưới là tóm tắt.

| Môi trường | Cấu hình |
|-----------|----------|
| Dev | docker-compose: PostgreSQL, Valkey, api-service, game-server ×1, client Vite |
| Staging | 1 VM: API ×1, GS ×2 tiến trình, PG, Valkey; dữ liệu giả + bot |
| Production (Alpha) | VM1: API ×2 + World leader + relay; VM2: GS (1 tiến trình / vCPU); VM3: PostgreSQL (PITR) ; Valkey trên VM1 (AOF bật) ; CDN cho asset + gamedata bundle |

- <a id="ta-r-150"></a>**TA-R-150** Client, API, GS cùng một **phiên bản gamedata** (`gamedataVersion` = hash nội dung). Client cũ hơn → buộc tải lại; GS và API khác phiên bản → GS từ chối khởi động (`/internal/v1/meta` trả version). **Danh mục cờ tính năng / kill switch `ff.*`** (nguồn duy nhất): [TD-09 §6](08-infra-ops.md).

---

## 17b. Đa nền tảng (D-22)
- <a id="ta-r-160"></a>**TA-R-160** **Một codebase client** (TypeScript / Babylon.js / React) cho web PC, mobile web / PWA và app iOS / Android bọc bằng Capacitor (PLAN §5.8). Không nhánh code riêng theo nền tảng ngoài các adapter dưới đây.
- <a id="ta-r-161"></a>**TA-R-161** **Lớp đầu vào trừu tượng** (`client/src/input/`): bàn phím + chuột, cảm ứng, tay cầm là các *adapter* sinh cùng một luồng `IntentFrame { move, aim, actions[] }`; mạng (TD-02 `C2S_Input`, lệnh hành động) và combat chỉ đọc `IntentFrame`. Hỗ trợ ngắm / tự chọn mục tiêu cho cảm ứng là phần của adapter cảm ứng (thiết kế ở GD-24), server không phân biệt. Định nghĩa đầy đủ `IntentFrame` (gồm `walk`) và các adapter: [TD-05 §2.3](04-client.md).
- <a id="ta-r-162"></a>**TA-R-162** **Lớp nền tảng** (`client/src/platform/`): interface `Storage`, `Purchase`, `Notifications`, `AppLifecycle` (vào nền / quay lại → TD-02 kết nối lại), `Share`; bản `web` và bản `capacitor`. Code game không gọi trực tiếp API trình duyệt / plugin native.
- <a id="ta-r-163"></a>**TA-R-163** **UI co giãn** từ đầu (điểm ngắt PC / tablet / điện thoại, vùng chạm ≥ 44px ở bố cục mobile) và **mức chất lượng đồ họa** (thấp / trung / cao: độ phân giải render, bóng, hậu xử lý, mật độ cỏ, số hạt VFX) có sẵn trong client.
- <a id="ta-r-164"></a>**TA-R-164** Quyền sở hữu vật phẩm mua bằng tiền thật **thuộc tài khoản ở API Service**, dù mua qua web hay qua cửa hàng ứng dụng (xác minh biên lai phía server); chi tiết ở GD-19.

## 18. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="ta-ac-001"></a>TA-AC-001 | `POST /internal/v1/inventory/grant` thành công với `grantId = g` | Gửi lại y hệt | Cùng response; không thêm `item_log` |
| TA-AC-002 | Như trên | Gửi lại `grantId = g` nhưng khác `items` | `422 IDEMPOTENCY_MISMATCH` |
| TA-AC-003 | Hai request cùng khóa gửi song song | Xử lý | Đúng một lần ghi; request kia nhận cùng response |
| TA-AC-004 | Request trả `409 INSUFFICIENT` | Gửi lại cùng khóa sau khi đã nạp tiền | Vẫn `409` (lỗi đã lưu, TA-R-052) |
| TA-AC-005 | Transaction nghiệp vụ rollback | Kiểm tra | Không có `outbox_event`, không có `rng_audit` |
| TA-AC-006 | Valkey tắt 60s trong lúc chế tác | Valkey bật lại | Mọi sự kiện trong outbox được phát, đúng thứ tự theo nhân vật |
| TA-AC-007 | Người nhận xử lý cùng `eventId` hai lần | Kiểm tra | Tác dụng một lần |
| TA-AC-008 | `rng_audit` của một lần chế tạo công pháp | `rng-replay` | `outcome` khớp |
| TA-AC-009 | Vector xoshiro128** chuẩn (seed cố định) | Chạy Java và TS | 1.000 số đầu giống nhau |
| TA-AC-010 | ctx cập nhật cách 3s | Gọi API cần vị trí | `409 CONTEXT_STALE` |
| TA-AC-011 | Nhân vật cách Đan Phòng 6m | `POST /v1/alchemy/sessions` | `409 NOT_NEAR` |
| TA-AC-012 | Nhân vật đang chiến đấu | Trang bị | `409 IN_COMBAT` |
| TA-AC-013 | API ngừng 2 phút | Người chơi đánh quái | Sau khi API về, tu vi và vật phẩm nhặt được ghi đủ một lần (hàng đợi thử lại) |
| TA-AC-014 | Hấp hối trong hiểm địa, API mất 15s | Hết thời gian hấp hối | Không chết trong lúc API mất (tạm dừng), xử lý tiếp khi API về |
| TA-AC-015 | Chết thật | Kiểm tra | Có `death_log` với 60s dữ liệu, `lethal-event` mang `deathLogId` |
| TA-AC-016 | Cùng nhân vật đăng nhập trên máy thứ hai | Vào game | Phiên cũ bị ngắt; nhân vật chỉ ở một room |
| TA-AC-017 | Dùng lại `joinTicket` | Kết nối | Từ chối |
| TA-AC-018 | GS sập giữa chừng | Người chơi đăng nhập lại | Vị trí / HP từ snapshot ≤ 30s trước; không nhân bản vật phẩm |
| TA-AC-019 | Module `craft` gọi thẳng repository của `inventory` | CI | ArchUnit chặn |
| TA-AC-020 | GS khởi động với gamedata khác API | Khởi động | Từ chối, log lỗi rõ ràng |
| TA-AC-022 | Client chạy với adapter cảm ứng giả lập | Di chuyển + dùng kỹ năng | Server nhận cùng định dạng `C2S_Input` / lệnh như bàn phím-chuột |
| TA-AC-021 | Ledger bị sửa tay khiến số dư lệch | Job đối soát | Cảnh báo |

---

## 19. Phạm vi theo phase

| Hạng mục | Phase 0 (spike) | Phase 1–2 | Alpha | Beta |
|----------|:---:|:---:|:---:|:---:|
| Monorepo, contracts, CI cơ bản | ✅ | | | |
| Join ticket, room = MapInstance, `shared/sim` | ✅ | | | |
| Idempotency, ledger, cổng ghi | | ✅ | | |
| RNG chuẩn + `rng_audit` + golden test | | ✅ | | |
| Outbox + Valkey Streams | | ✅ | | |
| `CharacterContext` | | ✅ | | |
| Death log + Death Review UI | | ✅ (log) | ✅ (UI) | |
| Đối soát ledger, `rng-replay` | | | ✅ | |
| Drain GS khi triển khai, Valkey Sentinel, replica PG | | | | ✅ |

---

## 20. Câu hỏi mở
- [x] **TA-Q-1:** *(Đã chốt 2026-10-25, D-23: **dựng mới** bằng starter chính thức theo TD-05 / 06 / 10; T5C chỉ tham khảo.)* Fork T5C hay dựng từ đầu trên Colyseus?
- [ ] **TA-Q-2:** Danh sách "hàm hai ngôn ngữ" (TA-R-011): có nên chuyển chấm minigame GD-10/11 sang Game Server (TS, một ngôn ngữ) rồi gửi kết quả đã chấm cho API? Đổi lại API phải tin GS. Đề xuất: giữ ở API cho MVP.
- [ ] **TA-Q-3:** Fan-out sự kiện tới mọi tiến trình GS (TA-R-083) đủ tới bao nhiêu tiến trình? Đo ở load test Alpha.
- [ ] **TA-Q-4:** Có cần Spring Modulith (sự kiện nội bộ + kiểm tra module) hay ArchUnit là đủ?
- [x] **TA-Q-5:** *(Đã chốt: qua kết nối Game Server + Valkey pub/sub, [TD-02 §11](01-network.md).)* Chat (GD-14) chạy qua Game Server (room) hay một dịch vụ WebSocket riêng? Ảnh hưởng TD-02.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-22 | — | Bản nháp đầu theo D-19: thành phần, ma trận quyền hạn, repo & module API (modular monolith, cổng ghi), quy ước API / lỗi / ID, idempotency, chuẩn ledger, RNG xoshiro128** + bảng `rng_audit` + danh mục kind, sự kiện domain (outbox + Redis Streams), `CharacterContext` cho kiểm tra vị trí, death log, sự cố & phục hồi, phiên chơi, quan sát, kiểm thử, triển khai, nghiệm thu |
| v0.2 | 2026-10-23 | — | Thêm module `beast`, `pvp`; DDL bảng platform ở TD-03 |
| v0.3 | 2026-10-25 | — | §17b Đa nền tảng (D-22): một codebase, lớp đầu vào trừu tượng, lớp nền tảng, UI co giãn + mức đồ họa, quyền sở hữu mua hàng ở API |
| v0.4 | 2026-10-25 | — | Đồng bộ TD-04/05/06/08/10/15: module `security`, khóa idempotency qua header ở API công khai, `combatPower` trong snapshot, cổng CI, `IntentFrame`, trỏ danh mục mã lỗi sang TD-04 §6 |
| v0.5 | 2026-10-25 | — | Chốt TA-Q-1: dựng mới, không fork T5C (D-23) |
| v0.6 | 2026-10-25 | — | D-24: Valkey thay Redis (tên khóa / lệnh không đổi) |
| v0.7 | 2026-10-25 | — | Trỏ TD-09 / TD-11; module `monetization`; tải 400 người / tiến trình |
| v0.8 | 2026-10-25 | — | Kind RNG sự kiện thế giới (GD-23) |

