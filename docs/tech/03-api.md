# TD-04 — API Spec

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-04 · Prefix ID: `API` |
| Owner | Tech Lead (Server) |
| Trạng thái | 🟡 Bản nháp v0.5 |
| Cần trước | Phase 1 (trước khi viết controller đầu tiên); danh mục endpoint cập nhật theo từng GD |
| Quyết định nền | [TD-01](00-tdd.md) §6 (bề mặt API, định dạng, lỗi), §7 (idempotency), §11 (`CharacterContext`), §14 (phiên chơi); [TD-03](02-database.md) (`account`, `account_session`); D-19 |
| Liên quan | Mục "Hợp đồng giao tiếp" / "API" của mọi GD · [TD-02 Network](01-network.md) (kênh realtime) · [TD-12 World](11-world-maps.md) · [TD-07 Gamedata](06-gamedata.md) · TD-08 Bảo mật |

> TD-01 §6 đã định nghĩa ba bề mặt API (`/v1`, `/internal/v1`, `/admin/v1`), định dạng JSON, lỗi, ID; tài liệu này **không lặp lại** mà bổ sung: cấu trúc `shared/contracts`, quy trình contract-first, xác thực, rate limit, quy ước đặt tên, **danh mục endpoint hợp nhất** từ mọi GD / TD, **danh mục mã lỗi** chuẩn cho `errors.yaml`. Khi GD và TD-04 vênh về **đường dẫn / tên** → theo TD-04 (§7 liệt kê các chỗ đã chuẩn hóa); về **nghiệp vụ** (trường, quy tắc) → theo GD.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-04 | Mục |
|--------------|-----------------|-----|
| Mọi GD có API | Quy ước đường dẫn, operationId, idempotency, đánh dấu cần `CharacterContext` | §3, §5 |
| `shared/contracts` | Cấu trúc file, nội dung `errors.yaml`, nguồn `rng-kinds.yaml` | §2, §6, §8 |
| Client (`@tienlo/client`), Game Server | Client TS sinh từ OpenAPI (`@tienlo/contracts`) | §2 |
| API Service (`vn.tienlo.api`) | Stub Java sinh ra, controller theo module | §2 |
| TD-08 | Luồng xác thực, token, rate limit | §4 |

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="api-r-001"></a>**API-R-001** | **Contract-first:** mọi endpoint tồn tại trong `shared/contracts/*.yaml` **trước** khi có code; CI chặn controller không có trong hợp đồng và hợp đồng không có controller (TD-01 TA-R-020). |
| <a id="api-r-002"></a>**API-R-002** | Mọi thao tác ghi có tác dụng phụ là `POST` / `PUT` / `DELETE` và **idempotent** (TD-01 §7): công khai dùng header `Idempotency-Key`, nội bộ dùng trường khóa trong body theo GD (`grantId`, `batchId`, …). |
| <a id="api-r-003"></a>**API-R-003** | Mã lỗi chỉ lấy từ `errors.yaml` (§6); response lỗi đúng định dạng TD-01 TA-R-043. |
| <a id="api-r-004"></a>**API-R-004** | Endpoint công khai thao tác trên nhân vật luôn có `{characterId}` trong path và API kiểm tra nhân vật thuộc tài khoản trong JWT (`403 FORBIDDEN` nếu không). |

---

## 2. `shared/contracts` & sinh code

```
shared/contracts/
├── public.yaml          # OpenAPI 3.1 — /v1/**  (client)
├── internal.yaml        # OpenAPI 3.1 — /internal/v1/**  (Game Server, World Service)
├── admin.yaml           # OpenAPI 3.1 — /admin/v1/**  (GM panel)
├── components/          # schema dùng chung: Error, Page, ChanceView, ItemInstance, ...  ($ref từ 3 file trên)
│   └── {module}.yaml    # một file component mỗi module TD-01 §5.1 (account, character, lifecycle, ..., beast, pvp)
├── errors.yaml          # danh sách mã lỗi đóng (§6)
└── rng-kinds.yaml       # danh sách rng_audit.kind đóng (§8)
```

- <a id="api-r-010"></a>**API-R-010** Mỗi path gắn **một tag = tên module** (`inventory`, `craft`, …) — dùng để sinh controller interface theo package `vn.tienlo.api.{module}.web` và chia nhóm trong client TS.
- <a id="api-r-011"></a>**API-R-011** **Sinh code** (CI, `pnpm contracts:gen` + task Gradle `openApiGenerate`):
  - TS: gói `@tienlo/contracts` — kiểu request / response + client fetch có kiểu + **schema zod** (validate response ở client dev, validate request ở Game Server khi gọi API nội bộ);
  - Java: interface controller (`*Api`) + DTO record trong `vn.tienlo.api.{module}.web.generated`; controller viết tay `implements` interface sinh ra; không sửa file sinh ra.
- <a id="api-r-012"></a>**API-R-012** Kiểu dữ liệu TS trong GD (§3 mỗi GD) là nguồn cho `components/*.yaml`; tên schema giữ nguyên tên kiểu TS (`ChanceView`, `BreedQuote`, `LifecycleView`…).
- <a id="api-r-013"></a>**API-R-013** **Contract test:** chạy test sinh tự động từ OpenAPI (Schemathesis hoặc tương đương) với API thật + Testcontainers: mọi response khớp schema, mọi mã lỗi trả về có trong `errors.yaml`, endpoint ghi gửi lặp cùng khóa trả cùng kết quả.
- <a id="api-r-014"></a>**API-R-014** **Thay đổi hợp đồng:** thêm trường tùy chọn / endpoint mới = tương thích; đổi kiểu, bỏ trường, đổi nghĩa = **không tương thích** → endpoint mới hoặc `/v2` (chỉ khi thật cần); CI so sánh với bản phát hành trước (oasdiff) và chặn thay đổi phá vỡ trên `/v1`.

---

## 3. Quy ước

### 3.1. Đường dẫn
| Loại tài nguyên | Mẫu | Ví dụ |
|-----------------|-----|-------|
| Thuộc nhân vật (kiếp) | `/v1/characters/{characterId}/…` | `/v1/characters/{id}/inventory/equip` |
| Thuộc tài khoản | `/v1/accounts/me/…` | `/v1/accounts/me/vault`, `/v1/accounts/me/beasts` |
| Danh mục / công khai, toàn server | `/v1/{tài-nguyên}` | `/v1/shops/{shopId}`, `/v1/crafted-techniques/{id}`, `/v1/life-records/{id}` |
| Hành động không phải CRUD | động từ cuối path, `POST` | `/…/ending/confirm`, `/…/forging/enhance` |
| Nội bộ | `/internal/v1/{module}/…` | `/internal/v1/inventory/grant` |
| Quản trị | `/admin/v1/{module}/…` | `/admin/v1/economy/alerts` |

- <a id="api-r-020"></a>**API-R-020** Tên path `kebab-case`, danh từ số nhiều cho tập hợp; ID trong path là `{characterId}`, `{itemInstanceId}`… (trong bảng §5 viết tắt `{id}` cho nhân vật).
- <a id="api-r-021"></a>**API-R-021** Phiên chế tác / lai tạo / thử thách thuộc nhân vật → nằm dưới `/v1/characters/{id}/…` kể cả các bước `submit` / `cancel` (§7 chuẩn hóa GD-10, GD-11).

### 3.2. operationId
- <a id="api-r-022"></a>**API-R-022** `{module}.{verb}{Object}` theo camelCase, động từ: `get`, `list`, `create`, `update`, `delete` hoặc động từ nghiệp vụ (`equip`, `craft`, `breed`, `confirm`…). Ví dụ: `inventory.equipItem`, `craft.submitAlchemySession`, `lifecycle.confirmEnding`. operationId là tên hàm trong client TS và tên method trong interface Java — **không đổi** sau phát hành.

### 3.3. Mẫu request / response
```http
POST /v1/characters/018f…/inventory/equip
Authorization: Bearer <JWT>
Idempotency-Key: equip:018f3c…            # TD-01 TA-R-050
X-Request-Id: 7c1e…                         # TA-R-042
traceparent: 00-…
Content-Type: application/json

{ "itemInstanceId": "018f…", "slot": "WEAPON" }
```
```json
200 OK
{ "equipment": { "WEAPON": { "itemInstanceId": "018f…", "templateId": "ITM_EQ_SWORD_LK4" } }, "stats": { "...": "..." } }
```
- <a id="api-r-023"></a>**API-R-023** Response thành công trả **trạng thái mới cần cho UI** (không chỉ `204`) để client không phải gọi lại; danh sách dùng `{ items, nextCursor }` (TA-R-041).
- <a id="api-r-024"></a>**API-R-024** Thao tác xem trước có tỷ lệ (loại A, GD-29) là `POST …/preview` (body có thể dài) trả `ChanceView` + `quoteId`; thao tác xác nhận gửi `quoteId` (RF-R-022).

---

## 4. Xác thực, phân quyền, rate limit

### 4.1. Tài khoản người chơi
| Endpoint | Mục đích | Ghi chú |
|----------|----------|---------|
| `POST /v1/auth/register` | `{ email, password, displayHandle?, locale, captchaToken }` → `201 { accountId }` | Email chuẩn hóa chữ thường; mật khẩu Argon2id (TD-03 §6); chống tạo hàng loạt: rate limit + CAPTCHA (TD-08) |
| `POST /v1/auth/login` | `{ email, password, device, captchaToken? }` → `{ accessToken, expiresIn: 900 }` + refresh token trong cookie HttpOnly | Tạo `account_session` (TD-03); sai → `401 INVALID_CREDENTIALS` (không phân biệt email sai / mật khẩu sai); CAPTCHA từ lần sai thứ 6 |
| `POST /v1/auth/refresh` | Refresh token lấy từ cookie HttpOnly → access token mới + cookie refresh token đã xoay | **Xoay vòng:** refresh cũ bị thu hồi ngay; dùng lại refresh đã thu hồi → thu hồi **toàn bộ** phiên của tài khoản (dấu hiệu bị đánh cắp) + `401 TOKEN_REVOKED` |
| `POST /v1/auth/logout` | Bearer access token + refresh cookie | Thu hồi phiên này và xóa cookie |
| `POST /v1/auth/logout-all` | Bearer access token | Thu hồi mọi phiên và xóa cookie |
| `GET /v1/accounts/me` | Hồ sơ tài khoản, cài đặt, thông tin Luân Hồi tóm tắt | |
| `PATCH /v1/accounts/me/settings` | `{ hideNameInBroadcast?, locale?, … }` | GD-14 SO-R-061 |
| `GET /v1/accounts/me/characters` | Nhân vật `ALIVE` / `ENDING` hiện tại + danh sách kiếp đã kết thúc (tóm tắt) | |
| `POST /v1/accounts/me/deletion-requests` | Yêu cầu xóa tài khoản | TD-03 DB-R-100 |

- <a id="api-r-030"></a>**API-R-030** **Access token** JWT ký **EdDSA (Ed25519)**, TTL 15 phút, claims: `sub = accountId`, `sid = sessionId`, `roles`, `iat`, `exp`, `aud = public`. **Refresh token** ngẫu nhiên 256-bit (không phải JWT), lưu **băm** trong `account_session.refresh_token_hash`, TTL 30 ngày, xoay vòng mỗi lần dùng; chỉ truyền qua cookie `__Secure-xproject_refresh` với `HttpOnly; Secure; SameSite=Strict; Path=/v1/auth`. Không đưa refresh token vào JSON response. Browser client dùng `credentials: include`; API chỉ cho phép các origin cấu hình chính xác qua `TL_CORS_ALLOWED_ORIGINS` và không bật wildcard khi dùng credentials. Response auth lưu trong `idempotency_record` phải được mã hóa AES-GCM; không lưu token thô trong DB.
- <a id="api-r-031"></a>**API-R-031** Tài khoản `SUSPENDED` / `BANNED`: login / refresh trả `403 ACCOUNT_SUSPENDED` kèm `details.until`, mọi phiên bị thu hồi khi trạng thái đổi.

### 4.2. Nội bộ và quản trị
- <a id="api-r-032"></a>**API-R-032** **Service token** (TD-01 §6.1): JWT EdDSA `aud = internal`, `sub = game-server:{processId}` hoặc `world-service`, TTL 1 giờ, cấp bởi API Service qua `POST /internal/v1/auth/service-token` với client credentials (secret nạp từ biến môi trường / secret manager); `/internal/**` chỉ nghe trên mạng nội bộ (cổng riêng hoặc ingress nội bộ).
- <a id="api-r-033"></a>**API-R-033** **Quản trị:** JWT người chơi có `roles ∋ GM | ADMIN` + yêu cầu **TOTP** khi đăng nhập GM panel; mọi `/admin/**` ghi `admin_audit` (TD-03 §6) với `before` / `after` / `reason` bắt buộc cho thao tác ghi.
- <a id="api-r-034"></a>**API-R-034** **Vé vào room** (`joinTicket`, TD-01 §14) do API cấp trong `POST /v1/play/join` và `POST /internal/v1/world/transfer`, không phải access token.

### 4.3. Rate limit (token bucket, Valkey `rl:*`, TD-03 §12)
| Nhóm | Phạm vi | Giới hạn | Vượt |
|------|---------|----------|------|
| `auth` (register, login, refresh) | IP + email | 10 / phút / IP; 5 login sai / 15 phút / email → CAPTCHA; 10 sai → khóa tăng dần (15 phút–24 giờ); 50 sai / giờ / IP → chặn 1 giờ | `429 RATE_LIMITED` |
| `read` (`GET /v1/**`) | tài khoản | 60 / giây, burst 120 | `429` |
| `write` (thao tác ghi công khai) | tài khoản | 10 / giây, burst 20 | `429` |
| `preview` (`…/preview`) | tài khoản | 5 / giây | `429` |
| `market` (đấu giá, giao dịch) | tài khoản | 2 / giây | `429` |
| `report` (báo cáo, khiếu nại) | tài khoản | 10 / giờ | `429` |
| `internal` | tiến trình | không giới hạn cứng; cảnh báo khi > 2.000 / giây / tiến trình | — |

- <a id="api-r-035"></a>**API-R-035** Response `429` có header `Retry-After` (giây). Rate limit tính **trước** idempotency (gửi lặp cũng tính).
- Auth CAPTCHA dùng gateway provider-neutral theo SEC-R-011: API gửi `{ secret, response, remoteIp }` đến `TL_CAPTCHA_VERIFICATION_URI`, nhận `{ "success": boolean }`. Thiếu cấu hình hoặc gateway lỗi trả `503 UNAVAILABLE`; không bỏ qua bước xác minh.

---

## 5. Danh mục endpoint

Ký hiệu: **Idem** — `IK` = header `Idempotency-Key`; tên trường = khóa trong body (nội bộ); `—` = chỉ đọc. **Ctx** — ✅ = API gọi `requireContext` (TD-01 §11: vị trí / không chiến đấu); `⚔` = chỉ kiểm tra không chiến đấu. **Nguồn** = tài liệu sở hữu nghiệp vụ (GD / TD + mục hợp đồng giao tiếp). Đường dẫn ở đây là **chuẩn**; những chỗ khác GD được đánh dấu ⚙ và giải thích ở §7.

### 5.1. Nền tảng, phiên chơi, thế giới
| Method + path | Mục đích | Nguồn | Idem | Ctx |
|---------------|----------|-------|------|-----|
| `GET /v1/meta` | `gamedataVersion`, URL bundle client, phiên bản client tối thiểu | TD-07 DT-R-121 | — | |
| `GET /v1/meta/rarity-bands` | Band độ hiếm | GD-29 §9.1 | — | |
| `POST /v1/play/join` | Vào game → `{ endpoint, roomId, joinTicket }` | TD-01 §14, TD-12 §10 | IK | |
| `POST /v1/characters/{id}/channel` | Đổi kênh | TD-12 WM-R-032 | IK | ⚔ |
| `GET /v1/world/maps/{mapId}/channels` | Danh sách kênh | TD-12 §10 | — | |
| `GET /internal/v1/meta` | `gamedataVersion` của API (Game Server so khi khởi động) | TD-01 TA-R-150 | — | |
| `POST /internal/v1/auth/service-token` | Cấp service token | TD-04 API-R-032 | — | |
| `GET /internal/v1/characters/{id}/session-load` | Nạp trạng thái nhân vật khi vào room | TD-01 §14 | — | |
| `POST /internal/v1/characters/{id}/snapshot` | Ảnh chụp vị trí / HP / cooldown dài / buff (+ `combatPower`, §7) | TD-01 TA-R-110 | `seq` | |
| `POST /internal/v1/death-logs` | Lưu death log | TD-01 TA-R-101 | `deathLogId` | |
| `POST /internal/v1/world/transfer` | Chuyển bản đồ, cấp vé | TD-12 §5 | `transferId` | |
| `POST /internal/v1/world/rooms/heartbeat` | Tiến trình báo room + tải | TD-12 §10 | — | |
| `POST /internal/v1/world/instances` | Tạo instance | TD-12 WM-R-060 | `instanceRequestId` | |
| `POST /internal/v1/world/fog` | Ghi fog (gộp OR) | TD-12 WM-R-081 | `batchId` | |
| `POST /internal/v1/codex/progress` | Tiến độ Vạn Giới Lục | GD-12 §9.1 | `batchId` | |
| `POST /internal/v1/chat/log` | Nhật ký chat theo lô | TD-02 NW-R-080 | `batchId` | |
| `POST /admin/v1/gamedata/reload` | Nạp lại gamedata (chỉ dev) | TD-07 DT-R-123 | — | |

### 5.2. Tài khoản, tạo nhân vật, thiên phú
| Method + path | Mục đích | Nguồn | Idem | Ctx |
|---------------|----------|-------|------|-----|
| (§4.1) `/v1/auth/*`, `/v1/accounts/me`, `/v1/accounts/me/characters`, `/v1/accounts/me/settings`, `/v1/accounts/me/deletion-requests` | Tài khoản | TD-04 §4.1 | IK (ghi) | |
| `GET /v1/creation/odds` | Bảng tỷ lệ "Thiên cơ" | GD-27 §9 | — | |
| `GET /v1/servers` | Danh mục server mở / bảo trì để chọn | GD-27 §9, TD-03 DB-Q-2 | — | |
| `GET /v1/backgrounds` · `GET /v1/talents` | Danh mục + trạng thái mở khóa | GD-27 §9 | — | |
| `POST /v1/creation/attempts` | Bắt đầu tạo nhân vật | GD-27 §9 | IK | |
| `POST /v1/creation/attempts/{attemptId}/rolls` | Roll / roll lại | GD-27 §9 | IK | |
| `POST /v1/creation/attempts/{attemptId}/confirm` | Xác nhận, tạo nhân vật | GD-27 §9 | IK | |
| `GET /v1/characters/{id}/talents` | Thiên phú hiện có | GD-27 §9 | — | |
| `POST /v1/characters/{id}/talent-choices/{pendingId}` | Chọn thiên phú đề xuất | GD-27 §9 | IK | |
| `GET /internal/v1/characters/{id}/modifiers` | Hiệu ứng thiên phú cho mô phỏng | GD-27 §9 | — | |
| `POST /internal/v1/talents/change` | Cổng thay đổi thiên phú | GD-27 TLN-R-006 | `changeId` | |
| `POST /internal/v1/talents/awakening` | Thức tỉnh | GD-27 §9 | `awakeningId` | |
| `POST /internal/v1/talents/fusion` | Dung hợp | GD-27 TLN-R-083 | `attemptId` | |

### 5.3. Vòng đời, Luân Hồi, Di Tàng
| Method + path | Mục đích | Nguồn | Idem | Ctx |
|---------------|----------|-------|------|-----|
| `GET /v1/characters/{id}/lifecycle` | `LifecycleView` | GD-26 §8.2 | — | |
| `GET /v1/characters/{id}/ending` · `POST …/ending/confirm` | Màn tổng kết, xác nhận (gửi Vault, linh thú, di sản) | GD-26 §8.2 | IK | |
| `GET /v1/accounts/me/rebirth` · `POST /v1/accounts/me/rebirth/unlocks` | Luân Hồi, mua unlock | GD-26 §8.2 | IK | |
| `GET /v1/accounts/me/vault` | Tàng Bảo Các | GD-26 §8.2 | — | |
| `POST /v1/accounts/me/vault/deposit` · `/withdraw` | Gửi / rút | GD-26 §8.2 | IK | ✅ `NPC_VAULT_KEEPER` |
| `POST /v1/characters/{id}/seclusion` | Bế quan | GD-26 §8.2 | IK | ✅ `LEY_LINE` / Tĩnh Thất |
| `GET /v1/life-records/{recordId}` | Bia Vạn Cổ công khai | GD-26 §8.2 | — | |
| `GET /v1/accounts/me/legacy-stash` | Di Tàng | GD-26 §9 | — | |
| `POST /v1/characters/{id}/remnant-trials` | Vào Tàn Hồn Cảnh (thu phí giải ấn) | GD-26 §9 | IK | ✅ `REMNANT_ALTAR` |
| `POST /internal/v1/lifecycle/age-accrual` | Báo tuổi theo lô | GD-26 §8.1 | `reportId` | |
| `POST /internal/v1/lifecycle/lifespan-change` | Cổng tiêu / cộng thọ | GD-26 §8.1 | `changeId` | |
| `POST /internal/v1/lifecycle/severe-injury` | Trọng thương (cờ `pvpVictimProtected`) | GD-26 §8.1, GD-32 | `eventId` | |
| `POST /internal/v1/lifecycle/lethal-event` | Sự kiện chết thật | GD-26 §8.1 | `eventId` | |
| `POST /internal/v1/lifecycle/last-light-expired` | Hết hồi quang phản chiếu | GD-26 §8.1 | `eventId` | |
| `POST /internal/v1/lifecycle/remnant-result` | Kết quả Tàn Hồn Cảnh | GD-26 §9 | `trialId` | |

### 5.4. Tu luyện, chỉ số, kỹ năng, chế công pháp
| Method + path | Mục đích | Nguồn | Idem | Ctx |
|---------------|----------|-------|------|-----|
| `GET /v1/characters/{id}/cultivation` | Tu vi, cảnh giới, đan độc | GD-04 §9 | — | |
| `POST /v1/characters/{id}/cultivation/consume` | Dùng vật phẩm tu luyện | GD-04 §9 | IK | |
| `POST /v1/characters/{id}/breakthrough/preview` ⚙ | `ChanceView` đột phá | GD-04 §9 | — | |
| `POST /v1/characters/{id}/breakthrough` | Bắt đầu đột phá (`quoteId`) | GD-04 §9 | IK | ✅ |
| `POST /internal/v1/breakthrough/{attemptId}/channel-complete` | Xong vận công | GD-04 §9.1 | `attemptId` | |
| `POST /internal/v1/breakthrough/{attemptId}/roll` | Roll đột phá | GD-04 §9.1 | `attemptId` | |
| `POST /internal/v1/breakthrough/{attemptId}/tribulation-result` | Kết quả thiên kiếp (gồm binh giải) | GD-04 §9.1, GD-26 §5.16 | `attemptId` | |
| `POST /internal/v1/cultivation/exp` | Cổng tu vi | GD-04 CU-R-001 | `grantId` | |
| `POST /internal/v1/cultivation/monster-kills` | Tu vi đánh quái theo lô | GD-04 §9.1 | `batchId` | |
| `POST /internal/v1/cultivation/meditation` | Đả tọa theo lô | GD-04 §9.1 | `batchId` | |
| `GET /v1/characters/{id}/stats` | Chỉ số + breakdown | GD-06 §9 | — | |
| `GET /internal/v1/characters/{id}/stat-inputs` | Đầu vào tính chỉ số | GD-06 §9 | — | |
| `GET /v1/characters/{id}/techniques` | Công pháp / kỹ năng đã học | GD-07 §9 | — | |
| `POST /v1/characters/{id}/techniques/learn` | Học ngọc giản | GD-07 §9 | IK | ⚔ |
| `GET /v1/characters/{id}/skills/{skillId}/preview` ⚙ | Xem chi tiết kỹ năng theo nhân vật | GD-07 §9 | — | |
| `PUT /internal/v1/characters/{id}/loadout` | Ghi ô kỹ năng (từ `C2S_SetLoadout`) | GD-07 §9 | `requestId` | |
| `PUT /internal/v1/characters/{id}/main-technique` | Đổi tâm pháp chính | GD-07 §9 | `requestId` | |
| `POST /internal/v1/techniques/mastery` | Thuần thục theo lô | GD-07 §9 | `batchId` | |
| `GET /v1/technique-forge/catalog` | Danh mục thành phần | GD-08 §9.1 | — | |
| `POST /v1/characters/{id}/technique-forge/preview` | Xem trước công pháp | GD-08 §9.1 | — | ✅ `INSIGHT_STATION` |
| `POST /v1/characters/{id}/technique-forge/craft` ⚙ | Chế tạo | GD-08 §9.1 | IK | ✅ |
| `POST /v1/characters/{id}/technique-forge/sandbox` | Diễn Pháp Tràng | GD-08 §9.1 | IK | ✅ |
| `POST /v1/characters/{id}/technique-forge/copy` ⚙ · `/dismantle` | Sao chép, tán giản (Beta) | GD-08 §9.1 | IK | ✅ |
| `GET /v1/crafted-techniques/{id}` | Thông tin công khai, **gồm thành phần** `coreId`, `elementId`, `modifierIds`, `mutationModifierId` (client chọn VFX, cache theo id) | GD-08 §9.1, TF-R-072 | — | |
| `GET /internal/v1/crafted-skills/{csk}` | `SkillDef` của `CSK_*` | GD-08 §9.2 | — | |
| `POST /admin/v1/crafted-techniques/{id}/rename` | GM đổi tên | GD-08 §9.2 | — | |

### 5.5. Vật phẩm, tiền tệ, kinh tế
| Method + path | Mục đích | Nguồn | Idem | Ctx |
|---------------|----------|-------|------|-----|
| `GET /v1/characters/{id}/inventory` | Túi, đồ mặc, kho (khi gần NPC), ô nhanh, ví | GD-09 §9.2 | — | |
| `POST /v1/characters/{id}/inventory/equip` · `/unequip` | Mặc / tháo | GD-09 §9.2 | IK | ⚔ |
| `POST /v1/characters/{id}/inventory/move` | Chuyển BAG ↔ WAREHOUSE, tách / gộp | GD-09 §9.2 | IK | ✅ (khi đụng `WAREHOUSE`) |
| `POST /v1/characters/{id}/items/{itemInstanceId}/use` | Dùng vật phẩm | GD-09 §9.2 | IK | ⚔ (tùy `UseSpec`) |
| `POST /v1/characters/{id}/items/{itemInstanceId}/destroy` | Hủy | GD-09 §9.2 | IK | |
| `POST /v1/characters/{id}/items/{itemInstanceId}/unseal` ⚙ | Mở Thú Ấn | GD-25 §9.1 | IK | |
| `GET /v1/characters/{id}/mail` · `POST …/mail/{mailId}/claim` | Thư | GD-09 §9.2 | IK | |
| `GET /v1/shops/{shopId}` | Cửa hàng NPC (kèm giới hạn mua hôm nay) | GD-13 §9 | — | |
| `POST /v1/characters/{id}/shops/{shopId}/buy` ⚙ · `/sell` · `/buyback` | Mua / bán | GD-09 §9.2, GD-13 §9 | IK | ✅ NPC cửa hàng |
| `POST /v1/characters/{id}/trades` · `PUT …/trades/{tradeId}/offer` · `POST …/lock` · `/confirm` · `/cancel` ⚙ | Giao dịch trực tiếp (Beta) | GD-13 §9 | IK | ⚔ |
| `GET /v1/auction/search` · `GET /v1/auction/price-history/{templateId}` | Vạn Bảo Lâu | GD-13 §9 | — | |
| `POST /v1/characters/{id}/auction/listings` ⚙ · `POST …/listings/{listingId}/buy` · `DELETE …/listings/{listingId}` | Treo / mua / hủy (Beta) | GD-13 §9 | IK | |
| `GET /v1/characters/{id}/fortune-log` | Thiên Cơ Lục | GD-29 §9.1 | — | |
| `POST /v1/support/fortune-disputes` | Khiếu nại thiên đạo | GD-29 §9.1 | IK | |
| `POST /internal/v1/inventory/grant` | Cổng sinh vật phẩm | GD-09 §9.1 | `grantId` | |
| `POST /internal/v1/inventory/consume` | Cổng tiêu hao | GD-09 §9.1 | `consumeId` | |
| `POST /internal/v1/inventory/move` | Cổng chuyển vị trí | GD-09 §9.1 | `moveId` | |
| `POST /internal/v1/currency/change` | Cổng tiền tệ | GD-09 §9.1 | `changeId` | |
| `POST /internal/v1/loot/grant` | Rơi đồ (quái, boss, thu thập, rương — §7) | GD-12 §9.1 | `grantId` | |
| `GET /admin/v1/economy/snapshots` · `/flows` · `/alerts` · `/characters/{id}/ledger` | Dashboard kinh tế | GD-13 §9 | — | |

### 5.6. Chế tác
| Method + path | Mục đích | Nguồn | Idem | Ctx |
|---------------|----------|-------|------|-----|
| `GET /v1/characters/{id}/alchemy` · `POST …/alchemy/preview` ⚙ | Trạng thái nghề, xem trước | GD-10 §9.1 | — | ✅ (preview) |
| `POST /v1/characters/{id}/alchemy/sessions` | Bắt đầu phiên luyện đan | GD-10 §9.1 | IK | ✅ `ALCHEMY_STATION` |
| `POST /v1/characters/{id}/alchemy/sessions/{sessionId}/submit` ⚙ · `/cancel` ⚙ | Nộp minigame / hủy | GD-10 §9.1 | `sessionId` | |
| `GET /v1/characters/{id}/forging` · `POST …/forging/preview` ⚙ | Trạng thái, xem trước | GD-11 §9.1 | — | ✅ (preview) |
| `POST /v1/characters/{id}/forging/sessions` · `…/forging/sessions/{sessionId}/submit` ⚙ · `/cancel` ⚙ | Rèn | GD-11 §9.1 | IK / `sessionId` | ✅ `FORGE_STATION` |
| `POST /v1/characters/{id}/forging/enhance` · `/reroll` · `/upgrade` · `/socket` · `/unsocket` · `/dismantle` · `/refine-stone` | Các thao tác luyện khí | GD-11 §9.1 | IK ⚙ | ✅ |
| `POST /v1/characters/{id}/forging/rerolls/{pendingId}/decide` ⚙ | Chọn kết quả tẩy luyện | GD-11 §9.1 | IK | |
| `GET /v1/characters/{id}/professions/{kind}/trials` · `POST …/trials` | Khảo hạch nghề | GD-10 §5.9 | IK | ✅ trưởng lão |

### 5.7. Thế giới mở, quái, cơ duyên, hiểm địa, nhiệm vụ
| Method + path | Mục đích | Nguồn | Idem | Ctx |
|---------------|----------|-------|------|-----|
| `GET /internal/v1/encounters/context/{characterId}` | Ngữ cảnh chọn cơ duyên | GD-22 §9.1 | — | |
| `POST /internal/v1/encounters/sessions` | Đặt chỗ phiên cơ duyên | GD-22 §9.1 | `sessionRequestId` | |
| `POST /internal/v1/encounters/sessions/{id}/state` · `/commit` · `/resolve` | Vòng đời phiên | GD-22 §9.1 | `commitId` (commit) | |
| `POST /internal/v1/encounters/pity` | Ghi bộ đếm pity | GD-22 §9.1 | `batchId` | |
| `GET /v1/danger-zones` | Danh sách hiểm địa | GD-28 §9.1 | — | |
| `POST /v1/characters/{id}/danger-zones/{dzId}/runs` ⚙ · `POST …/danger-runs/{runId}/confirm` ⚙ | Đăng ký / xác nhận lượt | GD-28 §9.1 | IK | ✅ cửa hiểm địa |
| `POST /internal/v1/danger/runs/{runId}/activate` · `/extract` · `/force-extract` | Vòng đời lượt | GD-28 §9.2 | `extractId` / `eventId` | |
| `POST /internal/v1/danger/corpses` · `/corpses/{corpseId}/loot` | Di hài | GD-28 §9.2 | `lootId` | |
| `GET /v1/characters/{id}/quests` | Nhiệm vụ | GD-15 §9 | — | |
| `POST /v1/characters/{id}/quests/{questId}/accept` · `/turn-in` · `/abandon` · `/track` · `/skip` | Thao tác nhiệm vụ | GD-15 §9 | IK | ✅ NPC (accept / turn-in) |
| `GET /v1/characters/{id}/sect-tasks` · `POST …/sect-tasks/{slot}/reroll` | Nhiệm vụ tông môn hằng ngày | GD-15 §9 | IK | |

### 5.8. Xã hội, tông môn, động phủ, linh thú, PvP
| Method + path | Mục đích | Nguồn | Idem | Ctx |
|---------------|----------|-------|------|-----|
| `POST /v1/parties/invites` · `…/invites/{id}/accept` · `/decline` · `POST /v1/parties/{id}/kick` · `/leave` · `/transfer` | Tổ đội | GD-14 §9 | IK | |
| `GET /v1/lfg` · `POST /v1/lfg` · `…/lfg/{id}/apply` · `/accept` | Chiêu Mộ | GD-14 §9 | IK | |
| `GET /v1/accounts/me/friends` · `POST …/friend-requests` · `…/friend-requests/{id}/accept` · `DELETE …/friends/{accountId}` | Bạn bè | GD-14 §9 | IK | |
| `POST /v1/accounts/me/blocks` · `DELETE …/blocks/{accountId}` | Chặn | GD-14 §9 | IK | |
| `POST /v1/reports` | Báo cáo (chat, tên công pháp / thú / tông) | GD-14 §9 (dùng chung GD-08, GD-25) | IK | |
| `GET /v1/characters/{id}/profile-public` | Xem người khác | GD-14 §9 | — | |
| `GET /v1/characters/{id}/npc-sect` | Tông NPC, bậc, cống hiến | GD-14 §9 | — | |
| `POST /v1/characters/{id}/npc-sect/leave` (mới) | Phản môn | GD-14 SO-R-047 | IK | ✅ tổng đàn |
| `GET /v1/characters/{id}/reputation` | Danh vọng phe | GD-14 §5.4c | — | |
| `POST /v1/characters/{id}/benefit` | Chọn gói phúc lợi | GD-14 SO-R-04K | IK | |
| `GET /v1/rankings/combat-power` | Thiên Bảng Chiến Lực | GD-14 SO-R-04I | — | |
| `POST /v1/player-sects` · `GET /v1/player-sects/{id}` · `…/applications` · `…/members/{accountId}/role` · `…/donate` · `…/buildings/{b}/upgrade` · `…/disband` | Tông môn tự lập | GD-14 §9 | IK | |
| `PUT /v1/player-sects/{id}/edicts` ⚙ | Đổi tông quy | GD-14 SO-R-05A | IK | |
| `GET /v1/player-sects/{id}/prestige` · `GET /v1/sect-sites` · `POST /v1/player-sects/{id}/site/claim` · `/abandon` · `POST /v1/conclave/{regionId}/pick` | Sơn môn | GD-30 §9 | IK | |
| `GET /v1/accounts/me/cave` · `POST /v1/accounts/me/cave` · `PUT …/cave/visit-policy` · `POST …/cave/buildings/{b}/upgrade` · `POST …/cave/relocate` · `POST …/cave/plots/{i}/plant` · `/harvest` · `GET /v1/caves/gates/{poiId}` | Động phủ | GD-30 §9 | IK | ✅ (`claim` / `relocate` tại cửa động) |
| `GET /v1/accounts/me/beasts` ⚙ · `GET /v1/beasts/{beastId}` | Danh sách thú; xem chi tiết / gia phả (công khai) | GD-25 §9.1 | — | |
| `POST /v1/accounts/me/beasts/{beastId}/contract` ⚙ · `/release-contract` · `/rename` · `/feed` · `/release` · `/seal` | Thao tác thú | GD-25 §9.1 | IK | ✅ (contract / release-contract) |
| `POST /v1/accounts/me/beasts/hatch/preview` ⚙ · `/hatch` | Ấp trứng | GD-25 §9.1 | IK | ✅ |
| `POST /v1/accounts/me/beasts/breed/preview` ⚙ · `/breed` · `/breed/{sessionId}/collect` | Lai tạo | GD-25 §9.1 | IK | ✅ Linh Thú Viên |
| `GET /internal/v1/characters/{id}/beast-loadout` | Thú kết khế + chỉ số | GD-25 §9.2 | — | |
| `POST /internal/v1/beasts/capture` · `/boss-capture` · `/bond` · `/injure` | Bắt thú, thân mật, trọng thương | GD-25 §9.2 | `attemptId` / `batchId` / `eventId` | |
| `GET /v1/bounties` | Bảng truy nã | GD-32 §4 | — | |
| `POST /internal/v1/pvp/kill` | Xử lý một lần giết PvP | GD-32 §4 | `eventId` | |
| `PUT /internal/v1/characters/{id}/pk-state` (mới) | Đổi chế độ PK / cờ xám / Thiên phạt (từ Game Server) | GD-32 §4 (§7) | `requestId` | |

---

> **Bổ sung:** sự kiện thế giới (`/v1/world-events/upcoming`, nhắc lịch, endpoint nội bộ kết quả / kích hoạt — [GD-23 §9](../design/24-world-events.md)); telemetry (`POST /v1/telemetry`, `/internal/v1/telemetry` — [GD-20](../design/51-analytics.md)); ngự kiếm không có REST (chỉ message, GD-31).
>
> **Bổ sung (GD-19):** nhóm endpoint monetization — cửa hàng ngoại hình, thanh toán web / IAP + webhook, Vấn Đạo Lệnh, quà tặng, hạn mức chi tiêu — định nghĩa ở [GD-19 §9](../design/50-monetization.md); gộp vào bảng §5 ở bản TD-04 sau. Thiết lập điều khiển (GD-24) dùng `PATCH /v1/accounts/me/settings` đã có.

## 6. Danh mục mã lỗi (`errors.yaml`)

`messageKey` mặc định = `err.{code_viết_thường}`. Cột **Nguồn**: tài liệu đầu tiên dùng mã; *mới (TD-04)* = bổ sung ở đây.

| Code | HTTP | Ý nghĩa | Nguồn |
|------|:----:|---------|-------|
| `VALIDATION` | 400 | Sai cú pháp / thiếu trường / vi phạm schema | TD-01 |
| `UNAUTHENTICATED` | 401 | Thiếu / sai access token | TD-01 |
| `INVALID_CREDENTIALS` | 401 | Sai email hoặc mật khẩu | mới (TD-04) |
| `TOKEN_EXPIRED` | 401 | Access token hết hạn (client gọi refresh) | mới (TD-04) |
| `TOKEN_REVOKED` | 401 | Refresh token đã thu hồi / dùng lại | mới (TD-04) |
| `FORBIDDEN` | 403 | Không có quyền / nhân vật không thuộc tài khoản | TD-01 |
| `ACCOUNT_SUSPENDED` | 403 | Tài khoản bị khóa (`details.until`) | mới (TD-04) |
| `LOCK_NOT_AVAILABLE` | 403 | Chưa mở khóa tính năng khóa dòng khi roll | GD-27 |
| `NOT_FOUND` | 404 | Không tồn tại | TD-01 |
| `INSUFFICIENT` | 409 | Không đủ tiền / vật phẩm / tài nguyên (`details.need`, `have`) | TD-01, GD-09 |
| `ALREADY_KNOWN` | 409 | Đã biết công pháp / công thức | GD-07, GD-08 |
| `ALREADY_HAS_TALENT` | 409 | Đã có thiên phú này | GD-27 |
| `NO_SPACE` | 409 | Túi / kho / ô đầy | GD-09 |
| `NO_ROLLS_LEFT` | 409 | Hết lượt roll | GD-27 |
| `NAME_TAKEN` | 409 | Tên đã dùng | GD-27 |
| `NAME_FILTERED` | 409 | Tên vi phạm bộ lọc (nhân vật, công pháp, thú, tông) | mới (TD-04; GD-08 TF-AC-014, GD-14 SO-R-021) |
| `TAKEN` | 409 | Tài nguyên đã bị người khác chiếm (sơn môn, cửa động…) | GD-30 |
| `UNIQUE_TAKEN` | 409 | Vi phạm `uniqueness` cơ duyên | GD-22 |
| `SOLD` | 409 | Tin đấu giá đã bán | GD-13 |
| `PRICE_CHANGED` | 409 | Giá đổi so với lúc xem | GD-13 |
| `CHANCE_CHANGED` | 409 | Tỷ lệ đổi / báo giá hết hạn (`details.chanceView`) | GD-29 |
| `INVALID_STATE` | 409 | Trạng thái không cho phép thao tác / xung đột khóa lạc quan | TD-01, TD-03 |
| `REQUIREMENT_NOT_MET` | 409 | Chưa đủ điều kiện (cảnh giới, cấp nghề, bậc tông…; `details.requirement`) | mới (TD-04) |
| `REALM_LOCKED` | 409 | Cảnh giới chưa đủ cho cổng / nội dung (trường hợp riêng của `REQUIREMENT_NOT_MET` dùng cho chuyển bản đồ) | TD-12 |
| `COOLDOWN` | 409 | Đang hồi chiêu (`details.availableAt`) | TD-12 |
| `DAILY_LIMIT` | 409 | Hết lượt trong ngày (`details.resetAt`) | GD-25 |
| `NOT_OWNER` | 409 | Không có quyền trên đối tượng tạm thời (Linh chủng, túi chiến lợi phẩm…) | GD-25 |
| `NOT_ALLOWED` | 409 | Luật chặn (ác nhân dùng truyền tống, kênh chat chưa đủ điều kiện…) | TD-12, GD-14 |
| `NOT_NEAR` | 409 | Không ở gần POI yêu cầu | TD-01 |
| `IN_COMBAT` | 409 | Đang chiến đấu | TD-01 |
| `CONTEXT_STALE` | 409 | `CharacterContext` quá cũ, thử lại sau 0,5s | TD-01 |
| `IDEMPOTENCY_MISMATCH` | 422 | Cùng khóa, nội dung khác | TD-01 |
| `CHARACTER_LOCKED` | 423 | Nhân vật đang được xử lý (chết, chuyển bản đồ, `ENDING`) | TD-01 |
| `CLIENT_OUTDATED` | 426 | Phiên bản client / gamedata cũ hơn tối thiểu — tải lại | mới (TD-04; TA-R-150) |
| `SPEND_LIMIT_REACHED` | 409 | Vượt hạn mức chi tiêu đã đặt | mới (GD-19) |
| `STORE_LOCKED` | 423 | Cửa hàng bị khóa do số dư Tiên Ngọc âm sau hoàn tiền | mới (GD-19) |
| `RATE_LIMITED` | 429 | Vượt giới hạn (`Retry-After`) | TD-01 |
| `UNAVAILABLE` | 503 | Lỗi tạm thời, thử lại cùng khóa | TD-01 |
| `INTERNAL_ERROR` | 500 | Lỗi không dự kiến; response không để lộ chi tiết nội bộ | TD-01 |

- <a id="api-r-040"></a>**API-R-040** Lý do từ chối **realtime** (`S2C_CastRejected.reason`, `S2C_GatherRejected.reason`, `S2C_ChatRejected.reason`…) **không** thuộc `errors.yaml`; chúng là enum trong `shared/schema` (TD-02). Kết quả nghiệp vụ không phải lỗi (`DIED`, `DEFERRED`, `PENDING_CHOICE`, `WARD_CONSUMED`…) là trường trong response 200, không phải mã lỗi.
- <a id="api-r-041"></a>**API-R-041** Ưu tiên mã cụ thể; `INVALID_STATE` chỉ dùng khi không có mã cụ thể hơn. Thêm mã: sửa `errors.yaml` + bảng này + i18n `err.*` (`vi` bắt buộc, TD-07 DT-R-072) trong cùng PR.

```yaml
# shared/contracts/errors.yaml (trích)
- code: INSUFFICIENT
  http: 409
  messageKey: err.insufficient
  details: { need: integer, have: integer, resource: string }
- code: CHANCE_CHANGED
  http: 409
  messageKey: err.chance_changed
  details: { chanceView: { $ref: components/rng.yaml#/ChanceView } }
```

---

## 7. Chuẩn hóa so với GD (⚙ ở §5)

| Chỗ trong GD / TD | Chuẩn TD-04 | Lý do |
|-------------------|-------------|-------|
| TD-01 §3.2 `POST /v1/alchemy/sessions` | `POST /v1/characters/{id}/alchemy/sessions` (như GD-10) | API-R-004 |
| GD-10 `POST /v1/alchemy/sessions/{sessionId}/submit` · `/cancel` | `/v1/characters/{id}/alchemy/sessions/{sessionId}/submit` · `/cancel` | API-R-021 |
| GD-11 `POST /v1/forging/sessions/{id}/submit` · `/cancel`, `/v1/forging/rerolls/{pendingId}/decide` | Thêm tiền tố `/v1/characters/{id}/forging/…` | API-R-021 |
| GD-04, GD-10, GD-11 `GET …/preview` | `POST …/preview` (body có lựa chọn, trả `quoteId`) | API-R-024 |
| GD-07 `GET /v1/skills/{skillId}/preview?characterId=` | `GET /v1/characters/{id}/skills/{skillId}/preview` | API-R-004 |
| GD-09 / GD-13 `POST /v1/shops/{shopId}/buy` · `/sell` · `/buyback` | `POST /v1/characters/{id}/shops/{shopId}/buy` · … (đọc danh mục vẫn `GET /v1/shops/{shopId}`) | Thao tác của nhân vật, cần `CharacterContext` |
| GD-13 `/v1/trades/*`, `/v1/auction/listings` (ghi) | `/v1/characters/{id}/trades/*`, `/v1/characters/{id}/auction/listings…` (tìm kiếm vẫn `/v1/auction/search`) | API-R-004 |
| GD-28 `POST /v1/danger-zones/{dzId}/runs`, `/v1/danger-runs/{runId}/confirm` | `/v1/characters/{id}/danger-zones/{dzId}/runs`, `/v1/characters/{id}/danger-runs/{runId}/confirm` | API-R-004 |
| GD-14 `PUT /v1/sects/{id}/edicts` | `PUT /v1/player-sects/{id}/edicts` | Đồng nhất tên tài nguyên tông tự lập |
| GD-25 `/v1/beasts/…` (ghi), `GET /v1/accounts/me/beasts` | Ghi: `/v1/accounts/me/beasts/…`; đọc công khai: `GET /v1/beasts/{beastId}` | Thú thuộc tài khoản |
| GD-25 `POST /v1/items/{id}/unseal` | `POST /v1/characters/{id}/items/{itemInstanceId}/unseal` | Như dùng vật phẩm GD-09 |
| GD-08, GD-11, GD-25 trường `requestId` trong body (công khai) | Header `Idempotency-Key` | API-R-002 |
| GD-14 SO-R-04I (Game Server báo `combatPower`, chưa có endpoint) | Trường `combatPower` trong `POST /internal/v1/characters/{id}/snapshot` | Không thêm endpoint |
| GD-32 chế độ PK / Thiên phạt (chưa có endpoint ghi) | `PUT /internal/v1/characters/{id}/pk-state` | `pk_state` do API ghi (TD-03) |
| GD-14 phản môn (SO-R-047, chưa có endpoint) | `POST /v1/characters/{id}/npc-sect/leave` | |
| GD-12 `loot/grant.source: 'MONSTER' \| 'BOSS'` | Mở rộng `'MONSTER' \| 'BOSS' \| 'GATHER' \| 'CHEST' \| 'PVP_BAG'` | Thu thập linh thảo / rương / túi chiến lợi phẩm PvP đi qua cùng cổng |

---

## 8. `rng-kinds.yaml`
Danh sách đóng = TD-01 §9.3 (đã gồm các bổ sung của GD-08, GD-25: `TECHNIQUE_*`, `BEAST_*`). Mỗi mục: `kind`, `class` (A–E, GD-29 §3), `source` (`API_SERVICE` / `GAME_SERVER`), `owner` (GD). Validator RF-V-04 dùng file này.

---

## 9. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="api-ac-001"></a>API-AC-001 | Controller có path không có trong `public.yaml` | CI | Fail (API-R-001) |
| API-AC-002 | Endpoint trả mã lỗi không có trong `errors.yaml` | Contract test | Fail |
| API-AC-003 | Đổi kiểu trường trong `/v1` so với bản phát hành trước | CI (oasdiff) | Fail |
| API-AC-004 | Đăng nhập sai mật khẩu 5 lần / 15 phút | Lần 6 | `429 RATE_LIMITED`, `Retry-After` |
| API-AC-005 | Refresh token R1 đã đổi lấy R2 | Dùng lại R1 | `401 TOKEN_REVOKED`; mọi phiên của tài khoản bị thu hồi |
| API-AC-006 | Access token hết hạn | Gọi API | `401 TOKEN_EXPIRED`; client gọi refresh rồi thử lại thành công |
| API-AC-007 | JWT tài khoản A, `characterId` của tài khoản B | Gọi `/v1/characters/{id}/inventory` | `403 FORBIDDEN` |
| API-AC-008 | Service token `aud = internal` | Gọi `/v1/**` | `401` (sai audience) |
| API-AC-009 | Gửi `POST …/inventory/equip` hai lần cùng `Idempotency-Key` | Lần 2 | Cùng response, không đổi trạng thái lần hai |
| API-AC-010 | GM đổi tên công pháp không kèm `reason` | `/admin/v1/…` | `400 VALIDATION`; có `reason` → ghi `admin_audit` có `before` / `after` |
| API-AC-011 | Client có `gamedataVersion` cũ hơn tối thiểu | `POST /v1/play/join` | `426 CLIENT_OUTDATED` |
| API-AC-012 | Client TS sinh từ OpenAPI | Gọi `craft.submitAlchemySession` | Kiểu request / response khớp; zod chặn response sai schema ở môi trường dev |
| API-AC-013 | Lỗi validation hoặc runtime tại API auth/account/server đã triển khai | Gọi API | Trả error envelope TA-R-043; lỗi runtime dùng `500 INTERNAL_ERROR`, không lộ chi tiết nội bộ |

---

## 10. Câu hỏi mở
- [ ] **API-Q-1:** Đăng nhập bằng tài khoản mạng xã hội / số điện thoại (OTP) cho thị trường Việt Nam ở launch không? Ảnh hưởng §4.1, TD-03 `account`.
- [ ] **API-Q-2:** Có cần GraphQL / BFF gộp nhiều GET cho màn hình nặng (bảng nhân vật) không, hay đủ với endpoint tổng hợp REST? Đề xuất: REST + vài endpoint tổng hợp.
- [ ] **API-Q-3:** Sinh client TS bằng công cụ nào (openapi-typescript + openapi-fetch, hay orval có sẵn zod)? Chốt ở Phase 1.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến contract-first, cấu trúc `shared/contracts` + sinh code TS / Java + contract test + kiểm thay đổi phá vỡ, quy ước đường dẫn / operationId / mẫu request, xác thực (JWT EdDSA 15 phút, refresh xoay vòng 30 ngày, service token, GM + TOTP), rate limit theo nhóm, **danh mục ~190 endpoint** theo 8 nhóm module, **36 mã lỗi** cho `errors.yaml`, bảng chuẩn hóa 16 chỗ vênh với GD, nghiệm thu |
| v0.2 | 2026-10-25 | — | D-24: Valkey thay Redis (tên khóa / lệnh không đổi) |
| v0.3 | 2026-10-25 | — | Bổ sung mã lỗi `SPEND_LIMIT_REACHED`, `STORE_LOCKED`; trỏ endpoint GD-19 |
| v0.4 | 2026-10-25 | — | Trỏ endpoint GD-23, GD-20 |
| v0.5 | 2026-10-25 | — | `GET /v1/crafted-techniques/{id}` trả thành phần (GD-08 TF-R-072, AR-04) |
| v0.6 | 2026-11-08 | — | Account controllers implement generated APIs (API-R-011); global error envelope, `INTERNAL_ERROR` |
