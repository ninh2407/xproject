# TD-08 — Bảo mật & Chống gian lận

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-08 · Prefix ID: `SEC` |
| Owner | Tech Lead (Server) + Security Champion (1 dev kiêm nhiệm) |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 1 (xác thực, phiên, kiểm tra message), Phase 2 (rate limit, phát hiện gian lận cơ bản), trước Closed Beta (pentest, phát hiện RMT / bot đầy đủ) |
| Quyết định nền | D-17 (không bán sức mạnh), D-19 (Colyseus + Spring Boot), D-21 (PvP), D-22 (đa nền tảng); [TD-01](00-tdd.md) TA-R-001..008, §6, §7, §11, §14; PLAN §5.3 |
| Liên quan | [TD-01 TDD](00-tdd.md) · [TD-02 Network](01-network.md) (§8.3 kiểm tra message, giới hạn tần suất, §7 bù trễ) · [TD-03 Database](02-database.md) (§13 dữ liệu cá nhân, `account_session`, `admin_audit`) · [TD-07 Gamedata](06-gamedata.md) (bundle client / server) · [GD-29 RNG](../design/05-rng-fairness.md) (seed, `serverOnly`) · [GD-13 Kinh tế](../design/19-economy.md) · [GD-14 Xã hội](../design/20-social.md) (lọc chat, báo cáo) · [GD-32 PvP](../design/31-pvp-infamy.md) · [GD-11 Luyện khí](../design/17-forging.md) (cờ bot minigame) · GD-19 Monetization · TD-09 Hạ tầng |

> Nguyên tắc gốc: **client là thù địch**. Mọi thứ client gửi có thể bị sửa, phát lại, tự động hóa. Bảo mật của game một mạng = **server quyết định mọi kết quả** (TD-01 TA-R-002) + **mọi thay đổi có giá trị có dấu vết** (ledger, `rng_audit`, `admin_audit`) + **phát hiện hành vi bất thường từ dữ liệu server**, không dựa vào phần mềm chống hack phía client.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-08 | Mục |
|--------------|-----------------|-----|
| TD-01 | Chi tiết xác thực, khóa ký JWT, service token, quyền GM | §3 |
| TD-02 | Danh sách kiểm tra chống gian lận phía Game Server, hậu quả vi phạm | §5.1, §5.2 |
| TD-03 | Mã hóa dữ liệu, quyền DB, bảng `security_flag`, `sanction`, `login_attempt` | §6, §10 |
| GD-13, GD-14, GD-32 | Phát hiện RMT, đa tài khoản, farm sát nghiệp; xử phạt | §5.3–§5.6 |
| GD-19 | Xác minh biên lai mua hàng | §8 |
| TD-09 | WAF, DDoS, bí mật, sao lưu mã hóa, ứng phó sự cố | §6, §9, §10 |

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="sec-r-001"></a>**SEC-R-001** | **Không tin client.** Client chỉ gửi ý định (TD-02 NW-R-001); mọi giá trị số (sát thương, vị trí, tiền, tỷ lệ) do server tính. Không có endpoint nào nhận "kết quả" từ client. |
| <a id="sec-r-002"></a>**SEC-R-002** | **Không có bí mật trong client.** Bundle client không chứa dữ liệu `serverOnly` (GD-29 RF-R-041), seed kết quả (RF-R-050), khóa API, khóa ký, URL nội bộ. |
| <a id="sec-r-003"></a>**SEC-R-003** | **Bí mật không bao giờ nằm trong repo.** Khóa ký, mật khẩu DB, token dịch vụ, khóa cổng thanh toán nằm trong kho bí mật (§6.3); CI chặn commit chứa bí mật. |
| <a id="sec-r-004"></a>**SEC-R-004** | **Mọi hành động của GM / admin có dấu vết** trong `admin_audit` (TD-03 §6), không ngoại lệ, không xóa. |
| <a id="sec-r-005"></a>**SEC-R-005** | **Phát hiện ≠ xử phạt tự động nặng.** Hệ thống tự động chỉ được cảnh cáo / hạn chế tạm; khóa vĩnh viễn luôn cần người duyệt (§5.7). Game một mạng: xử oan một tài khoản là thiệt hại không lấy lại được. |
| <a id="sec-r-006"></a>**SEC-R-006** | **Tối thiểu quyền:** mỗi thành phần chỉ có quyền nó cần (Game Server không có quyền DB, TA-R-001; tài khoản DB của API không có quyền DDL ở production). |

---

## 2. Mô hình đe dọa (STRIDE rút gọn)

| Thành phần | Đe dọa chính | Loại | Biện pháp |
|-----------|--------------|------|-----------|
| **Client** (trình duyệt / app) | Sửa code JS, đọc bộ nhớ, tự động hóa input (bot, macro), đọc dữ liệu ẩn | T, I, E | Server-authoritative (§5.1); không gửi dữ liệu ẩn (§5.4); phát hiện bot từ phía server (§5.2) |
| **Kênh WebSocket** | Phát lại / giả message, spam, flood kết nối, chiếm phiên | S, T, D | TLS; join ticket dùng một lần (TD-01 §14); kiểm tra schema + giới hạn tần suất (TD-02 §8.3); giới hạn kết nối (§9) |
| **REST công khai** | Dò mật khẩu, dùng lại token, phát lại request ghi, IDOR (sửa `characterId` của người khác), injection | S, T, E | Argon2id + khóa đăng nhập (§3.1); refresh xoay vòng (§3.2); idempotency (TD-01 §7); kiểm tra sở hữu tài nguyên (SEC-R-030); validate + truy vấn tham số hóa (§4) |
| **Game Server** | Bị chiếm → gọi API nội bộ tùy ý (tạo đồ) | E | Service token phạm vi hẹp + mạng nội bộ (§3.3); API kiểm tra hợp lý (giới hạn lô, trần giá trị) (SEC-R-032); ledger + đối soát (TD-01 TA-R-062) |
| **API Service** | Lỗ hổng phụ thuộc, lộ bí mật, logic sai cho phép nhân đồ | T, I, E | Chuỗi cung ứng (§7); bí mật trong vault (§6.3); idempotency + khóa hàng (TD-03 §10) + test |
| **PostgreSQL** | Truy cập trái phép, lộ backup | I, T | Mạng riêng, TLS, quyền tối thiểu (§6.4), mã hóa đĩa + backup (§6.2) |
| **Valkey** | Truy cập trái phép, giả `ctx:*` để vượt kiểm tra vị trí | T, E | Mạng riêng, AUTH + ACL, TLS; Valkey không phải nguồn sự thật cho giá trị (TD-03 DB-R-090); `ctx` chỉ Game Server ghi (ACL) |
| **CDN** | Giả mạo asset / bundle | T | Tên file theo hash nội dung; HTTPS; client kiểm `gamedataVersion` (TD-07 DT-R-091) |
| **GM panel** | Chiếm tài khoản GM → phát đồ, đổi dữ liệu | S, E, R | 2FA bắt buộc, mạng / VPN riêng, quyền theo vai trò, `admin_audit`, hành động nhạy cảm cần 2 người (§3.4) |
| **Người chơi** (ngoài kỹ thuật) | RMT, đa tài khoản chuyển đồ, farm sát nghiệp, lừa đảo giao dịch, quấy rối chat | — | Phát hiện từ ledger (§5.3–§5.6); lọc / báo cáo chat (GD-14); xác nhận giao dịch 2 bước (GD-13) |

(S = giả danh, T = sửa đổi, R = chối bỏ, I = lộ thông tin, D = từ chối dịch vụ, E = leo thang quyền.)

---

## 3. Xác thực, phiên, phân quyền

### 3.1. Đăng nhập
- <a id="sec-r-010"></a>**SEC-R-010** Mật khẩu băm **Argon2id** (tham số khởi điểm: memory 64 MiB, iterations 3, parallelism 1; hiệu chỉnh để băm ≈ 200–300ms trên máy chủ API), muối ngẫu nhiên mỗi tài khoản. Độ dài tối thiểu 10 ký tự, kiểm tra với danh sách mật khẩu phổ biến / đã lộ (danh sách offline, không gửi mật khẩu ra ngoài).
- <a id="sec-r-011"></a>**SEC-R-011** **Chống dò mật khẩu:** đếm thất bại theo tài khoản và theo IP (bảng `login_attempt` + Valkey): ≥ 5 lần / 15 phút theo tài khoản → yêu cầu CAPTCHA; ≥ 10 → khóa đăng nhập tài khoản 15 phút (tăng dần, tối đa 24h) và gửi email cảnh báo; ≥ 50 lần / giờ theo IP → chặn IP 1 giờ. Thông báo lỗi **không** tiết lộ tài khoản có tồn tại hay không.
- `login_attempt` chỉ lưu account ID (nếu xác định được), IP và kết quả; không lưu email hay mật khẩu. Valkey giữ bộ đếm ngắn hạn; bảng này phục vụ kiểm toán và lưu 90 ngày.

DDL đề xuất:

```sql
CREATE TABLE login_attempt (
  id BIGSERIAL PRIMARY KEY,
  account_id UUID REFERENCES account(id),
  ip INET NOT NULL,
  succeeded BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_login_attempt_account_time ON login_attempt(account_id, created_at DESC);
CREATE INDEX ix_login_attempt_ip_time ON login_attempt(ip, created_at DESC);
```

- <a id="sec-r-012"></a>**SEC-R-012** **2FA (TOTP)** tùy chọn cho người chơi (khuyến khích bằng phần thưởng cosmetic nhỏ); **bắt buộc** cho tài khoản có vai trò GM / ADMIN (§3.4). Đổi email / mật khẩu / tắt 2FA cần xác nhận qua email đã đăng ký và nhập lại mật khẩu.

### 3.2. Token & phiên
- <a id="sec-r-013"></a>**SEC-R-013** **Access token:** JWT ký **bất đối xứng** (Ed25519 hoặc ES256), sống 15 phút, `aud = public`, chứa `sub` (accountId), `sid` (session id), `roles`, `kid`. Các dịch vụ xác minh bằng khóa công khai (JWKS nội bộ).
- <a id="sec-r-014"></a>**SEC-R-014** **Xoay khóa ký** mỗi 30 ngày: khóa mới ký token mới, khóa cũ vẫn xác minh tới khi token cuối hết hạn (giữ ≥ 2 khóa trong JWKS); khóa bị nghi lộ → thu hồi ngay, mọi phiên phải đăng nhập lại.
- <a id="sec-r-015"></a>**SEC-R-015** **Refresh token:** chuỗi ngẫu nhiên 256 bit, lưu **băm** trong `account_session.refresh_token_hash` (TD-03 §6), sống 30 ngày, **xoay vòng mỗi lần dùng** (cấp mới, vô hiệu cũ). **Phát hiện tái dùng:** một refresh token đã bị xoay mà được dùng lại → coi như bị đánh cắp: thu hồi **toàn bộ chuỗi phiên** đó, gửi email cảnh báo.
- <a id="sec-r-016"></a>**SEC-R-016** Trình duyệt: refresh token để trong cookie `HttpOnly; Secure; SameSite=Strict; Path=/v1/auth`; access token chỉ giữ trong bộ nhớ (không `localStorage`). App Capacitor: refresh token trong kho bảo mật của hệ điều hành (Keychain / Keystore) qua plugin.
- <a id="sec-r-017"></a>**SEC-R-017** **Join ticket** vào Game Server (TD-01 §14): JWT ký riêng (`aud = game`), TTL 60s, `jti` dùng một lần (`SET NX ticket:{jti}`), gắn `characterId` + `roomId`; Game Server từ chối vé sai room hoặc đã dùng. Đăng nhập lần hai → phiên cũ bị ngắt (TA-R-120).
- <a id="sec-r-018"></a>**SEC-R-018** Đăng xuất / đổi mật khẩu / khóa tài khoản → thu hồi refresh token, đưa `sid` vào danh sách thu hồi trong Valkey tới khi access token hết hạn, ngắt kết nối Game Server của tài khoản (sự kiện `account.sessions_revoked`).

### 3.3. Dịch vụ nội bộ
- <a id="sec-r-019"></a>**SEC-R-019** **Service token:** JWT ký bằng khóa riêng của từng dịch vụ (`iss = game-server-{processId}` / `world-service`), `aud = internal`, TTL 1 giờ, chứa **phạm vi** (`scopes`: vd. `inventory.grant`, `lifecycle.event`, `world.transfer`). API kiểm tra phạm vi cho từng endpoint `/internal/v1/*`.
- <a id="sec-r-020"></a>**SEC-R-020** `/internal/*` và `/admin/*` **không** mở ra internet: chỉ nhận từ mạng riêng; production bật **mTLS** giữa Game Server ↔ API (chứng chỉ ngắn hạn, xoay tự động). Load balancer công khai chặn mọi đường dẫn `/internal` / `/admin`.

### 3.4. GM & quản trị
- <a id="sec-r-021"></a>**SEC-R-021** Vai trò: `GM_SUPPORT` (xem, mute chat, xử lý báo cáo), `GM` (thêm: khóa tạm, đổi tên vi phạm, Death Review), `GM_SENIOR` (thêm: khôi phục nhân vật, phát / thu vật phẩm), `ADMIN` (cấu hình, quản lý vai trò). Quyền theo vai trò kiểm tra ở API, không ở UI.
- <a id="sec-r-022"></a>**SEC-R-022** GM panel chỉ truy cập qua VPN / mạng nội bộ, **2FA bắt buộc**, phiên 8 giờ, khóa sau 15 phút không hoạt động.
- <a id="sec-r-023"></a>**SEC-R-023** **Hai người duyệt** (4-eyes) cho: phát vật phẩm / tiền giá trị > ngưỡng (`gmGrantApprovalValue`), khôi phục nhân vật đã chết (GD-26 LC-R-150), khóa vĩnh viễn, đổi vai trò. Yêu cầu tạo bởi người A, duyệt bởi người B (B ≠ A), cả hai ghi `admin_audit`.
- <a id="sec-r-024"></a>**SEC-R-024** Mọi hành động GM ghi `admin_audit { actor, action, target, before, after, reason }` (bắt buộc nhập lý do); xem dữ liệu nhạy cảm (email, IP) cũng ghi. Báo cáo hằng tuần: GM nào phát bao nhiêu giá trị — phát hiện GM gian lận.

---

## 4. Bảo vệ API

- <a id="sec-r-030"></a>**SEC-R-030** **Kiểm tra sở hữu tài nguyên (chống IDOR)** ở mọi endpoint có `characterId` / `itemInstanceId` / `beastId`…: thuộc `accountId` trong token và (nếu cần) nhân vật `ALIVE`. Viết một lần ở tầng chung (`@OwnedBy` annotation + interceptor trong `vn.tienlo.api.platform.security`), có test cho từng endpoint.
- <a id="sec-r-031"></a>**SEC-R-031** **Validate đầu vào** bằng schema sinh từ `@tienlo/contracts` (OpenAPI → Bean Validation): kiểu, độ dài, khoảng, enum; từ chối trường thừa. Truy vấn DB **chỉ** qua tham số hóa (JPA / jOOQ), không nối chuỗi SQL. Chuỗi người chơi nhập (tên, chat, mô tả) qua bộ lọc GD-14 và được **escape khi hiển thị** (React mặc định; cấm `dangerouslySetInnerHTML` với dữ liệu người dùng — lint).
- <a id="sec-r-032"></a>**SEC-R-032** **Kiểm tra hợp lý ở API nội bộ** (phòng Game Server bị chiếm hoặc lỗi): giới hạn mỗi lô (vd. `loot/grant` ≤ 20 dòng, `monster-kills` ≤ 200 kill / 30s / nhân vật, `cultivation/exp` theo trần nguồn GD-04), tổng giá trị nhận / giờ / nhân vật vượt `p99.9` theo cảnh giới → chấp nhận nhưng gắn `security_flag` (§5.3), vượt 10× → từ chối + cảnh báo.
- <a id="sec-r-033"></a>**SEC-R-033** **Giới hạn tần suất** (token bucket, Valkey `rl:*`, TD-03 §12) theo ba trục — IP, tài khoản, endpoint:

| Nhóm endpoint | Theo tài khoản | Theo IP |
|---------------|----------------|---------|
| `/v1/auth/login`, `/register`, `/password/*` | xem SEC-R-011 | 20 / phút |
| Ghi nghiệp vụ (`POST`, `PUT`) | 30 / phút mỗi endpoint, 300 / phút tổng | 600 / phút |
| Đọc (`GET`) | 120 / phút | 1.200 / phút |
| Giao dịch / đấu giá / thư | 10 / phút | 100 / phút |
| Báo cáo người chơi | 10 / giờ | 30 / giờ |

  Vượt → `429 RATE_LIMITED` kèm `Retry-After`. Vượt liên tục → `security_flag` (`RATE_ABUSE`).
- <a id="sec-r-034"></a>**SEC-R-034** **Chống phát lại:** mọi request ghi có khóa idempotency (TD-01 §7); token có `exp` ngắn; join ticket dùng một lần. Thao tác giá trị cao (giao dịch, đấu giá mua, xác nhận phân phối) cần **báo giá / phiên bản** hiện tại (vd. `PRICE_CHANGED`, `quoteId` GD-29) để không bị phát lại với điều kiện cũ.
- <a id="sec-r-035"></a>**SEC-R-035** **HTTP:** chỉ HTTPS (HSTS 1 năm, `includeSubDomains`); CORS chỉ cho origin của game (web, `capacitor://localhost`, `https://localhost` của app) — không `*`; header bảo mật: `Content-Security-Policy` (chỉ cho phép script / style từ origin + CDN của game, `connect-src` tới API / Game Server, không `unsafe-eval` ở production), `X-Content-Type-Options: nosniff`, `Referrer-Policy: strict-origin-when-cross-origin`, `Permissions-Policy` tối thiểu, `frame-ancestors 'none'`.
- <a id="sec-r-036"></a>**SEC-R-036** Lỗi trả về theo TD-01 §6.3, **không** lộ stack trace, câu SQL, đường dẫn nội bộ. Log đầy đủ ở server với `requestId`.

---

## 5. Chống gian lận gameplay

### 5.1. Server-authoritative — danh sách kiểm tra bắt buộc ở Game Server
- <a id="sec-r-040"></a>**SEC-R-040** Game Server kiểm tra (đã quy định ở TD-02 / GD, gom lại để triển khai chống gian lận một chỗ trong `@tienlo/game-server/src/anticheat/`):

| Kiểm tra | Cách | Vi phạm → |
|----------|------|-----------|
| Tốc độ di chuyển | Server tính vị trí từ input (TD-02 NW-R-030/031), không nhận vị trí; hàng đợi input tối đa 6 khung | Bỏ khung dư; đếm `INPUT_FLOOD` |
| Hồi chiêu, tài nguyên | GD-07 SK-R-070 (dung sai 100ms) | `S2C_CastRejected`; đếm `COOLDOWN_SPAM` nếu > 20 lần / phút |
| Tầm, góc, chướng ngại | `aim` kẹp vào tầm; va chạm theo TD-14 | Kẹp, không lỗi |
| Tương tác (nhặt, NPC, trạm, hái) | Khoảng cách server (TD-14 GEO-R-061), `CharacterContext` (TD-01 §11) | Từ chối; đếm `INTERACT_RANGE` |
| Tốc độ đồng hồ client (speedhack) | So `clientTimeMs` trôi với thời gian server qua `C2S_Ping` (TD-02 NW-R-012): lệch > 5% trong 60s | Không ảnh hưởng mô phỏng (server dùng đồng hồ của mình); đếm `CLOCK_DRIFT` |
| Message sai schema / NaN / vector chưa chuẩn hóa | TD-02 NW-R-063 | Bỏ; đếm `MALFORMED` |
| Tần suất message | TD-02 NW-R-064 | Bỏ → cảnh báo → ngắt kết nối |
| Bù trễ bị lạm dụng (giả ping cao) | `rewindMs` có trần (200ms PvE / 100ms PvP), `rttMs` server đo, không tin client (TD-02 §7) | Trần cố định |

- <a id="sec-r-041"></a>**SEC-R-041** Bộ đếm vi phạm theo nhân vật (cửa sổ trượt 10 phút) gửi về API mỗi 60s (`POST /internal/v1/security/signals`, gom lô); vượt ngưỡng → `security_flag` (§5.6). Vi phạm kỹ thuật thuần (vd. `MALFORMED` từ client sửa đổi) mức cao → ngắt kết nối ngay.

### 5.2. Bot và macro
- <a id="sec-r-042"></a>**SEC-R-042** **Không** dùng phần mềm chống hack cài trên máy người chơi (không khả thi trên web). Phát hiện bot **từ dữ liệu server**:

| Tín hiệu | Mô tả | Nguồn |
|----------|-------|-------|
| Thời gian chơi | Online liên tục > 16 giờ / ngày nhiều ngày, không có khoảng nghỉ | Phiên |
| Đều đặn input | Phương sai khoảng cách giữa các lệnh quá thấp (người thật có dao động); lặp chuỗi đường đi giống hệt | `C2S_Input`, lệnh hành động |
| Minigame chế tác | Điểm hoàn hảo bất thường (GD-11 `botFlagPerfectRatio` 0,98 trên ≥ 200 phách, tương tự GD-10) | Chấm lại phía server |
| Tuyến farm | Lặp cùng vòng điểm thu thập / quái hàng giờ, không tương tác xã hội, không đọc hội thoại | Telemetry |
| Phản ứng sự kiện | Không phản ứng với thứ con người sẽ phản ứng (thông báo, NPC nói chuyện, tin nhắn GM) | Kiểm tra chủ động của GM |
| Nhiều tài khoản cùng mẫu | Cùng thiết bị / IP, cùng tuyến, chạy đồng thời | §5.3 |

- <a id="sec-r-043"></a>**SEC-R-043** Mỗi tín hiệu cho điểm; tổng điểm `botScore` theo ngày. Vượt ngưỡng 1 → **thử thách nhẹ** trong game (vd. "Thiên cơ vấn" — câu đố lựa chọn ngẫu nhiên khi mở rương / hái thuốc, sai 3 lần → khóa thu thập 30 phút); vượt ngưỡng 2 → `security_flag` cho GM xem lại (không tự khóa — SEC-R-005).
- <a id="sec-r-044"></a>**SEC-R-044** Thiết kế đã giảm lợi ích của bot: trần tu vi đánh quái (GD-04), trần cống hiến / danh vọng ngày, linh thảo hiếm tranh đoạt + chống đổi kênh (GD-21 OW-R-105), thú không tự thu thập (GD-25 SB-R-045). Macro hợp lệ (giữ phím, gán nút) không bị cấm; tự động hóa không cần người là vi phạm (Điều khoản dịch vụ).

### 5.3. Đa tài khoản, chuyển đồ, RMT
- <a id="sec-r-050"></a>**SEC-R-050** **Liên kết tài khoản:** API lưu dấu vết đăng nhập (`ip`, `deviceHash` — băm từ thông tin thiết bị không nhạy cảm, không fingerprint xâm phạm) trong `account_session`; đồ thị tài khoản liên quan (cùng thiết bị / IP / phương thức thanh toán). Nhiều tài khoản **không** bị cấm (D-12 cho 1 nhân vật / tài khoản / server), nhưng là đầu vào cho phát hiện chuyển đồ.
- <a id="sec-r-051"></a>**SEC-R-051** **Phát hiện chuyển đồ / RMT** từ ledger (`item_log`, `currency_ledger`, `trade_session`, `auction_listing` — GD-09, GD-13):
  - giao dịch / đấu giá **lệch giá** (bán < 20% hoặc mua > 500% giá trung vị 7 ngày) — đặc biệt giữa tài khoản liên quan;
  - **dòng chảy một chiều**: tài khoản A liên tục chuyển giá trị cho B, A không nhận lại gì;
  - tài khoản mới (< 7 ngày) nhận giá trị lớn;
  - tài khoản "mỏ" (farm rồi chuyển hết, gần như không tiêu dùng);
  - chat chứa mẫu quảng cáo bán linh thạch (GD-14 SO-R-021 đã chặn URL / số điện thoại ở kênh công khai).
  Mỗi mẫu tạo `security_flag` (`RMT_SUSPECT`) với điểm và bằng chứng (ID ledger).
- <a id="sec-r-052"></a>**SEC-R-052** Biện pháp thiết kế đã có: trần giao dịch trực tiếp theo IPH (GD-13), thuế đấu giá (hố tiền), vật phẩm mạnh ràng buộc, Tiên Ngọc không chuyển được (EC-R-001). Xử lý RMT: thu hồi vật phẩm / tiền bất hợp pháp (bút toán bù `GM_REMOVE`, TA-R-061), khóa theo §5.7; tài khoản bán chịu nặng hơn tài khoản mua.

### 5.4. Map hack & lộ thông tin
- <a id="sec-r-053"></a>**SEC-R-053** Server **không gửi** thực thể ẩn chưa phát hiện (TD-02 NW-R-042), dữ liệu `serverOnly` (GD-29), vị trí bí mật mỗi kiếp, tỷ lệ rơi, nội dung cơ duyên chưa diễn ra, vị trí người chơi ngoài AOI (trừ tổ đội 1Hz). Hack đọc bộ nhớ client không có gì để đọc.
- <a id="sec-r-054"></a>**SEC-R-054** Thông tin về người chơi khác qua API công khai giới hạn ở thông tin hiển thị (GD-14 SO-R-080); không trả vị trí chính xác, không trả thọ nguyên tối đa, không trả túi đồ.

### 5.5. Lạm dụng PvP và sát nghiệp (GD-32)
- <a id="sec-r-055"></a>**SEC-R-055** **Farm sát nghiệp / danh vọng / tiền thưởng giữa tài khoản liên quan:** cặp (kẻ giết, nạn nhân) lặp lại, tài khoản liên quan (SEC-R-050), nạn nhân cố ý bật `SLAUGHTER` rồi đứng yên, đại ác nhân bị cùng một người hạ nhiều lần để chuyển tiền thưởng truy nã (PK-R-052) → `security_flag` (`PVP_COLLUSION`); tiền thưởng giữa tài khoản liên quan **không trả** (giữ lại tới khi GM duyệt).
- <a id="sec-r-056"></a>**SEC-R-056** **Quấy rối người mới:** một người giết nhiều nạn nhân vô tội thấp hơn ≥ 1 cảnh giới lớn trong 24h (vượt ngưỡng) → ngoài sát nghiệp ×3 (PK-AC-009), gắn cờ cho GM; tái phạm có thể bị hạn chế PvP (khóa chế độ `SLAUGHTER` 7 ngày) theo §5.7.
- <a id="sec-r-057"></a>**SEC-R-057** Cố ý mất kết nối để tránh chết (PvP / hiểm địa): đã được xử lý bằng quy tức và bất tử có giới hạn (GD-28, TD-02 NW-R-072); tỷ lệ mất kết nối lúc máu thấp bất thường (so với trung bình) → `security_flag` (`DISCONNECT_ABUSE`).

### 5.6. Cờ bảo mật
```sql
CREATE TABLE security_flag (
  id UUID PRIMARY KEY, account_id UUID NOT NULL, character_id UUID,
  kind VARCHAR(32) NOT NULL,            -- BOT_SUSPECT, RMT_SUSPECT, PVP_COLLUSION, RATE_ABUSE, CLIENT_TAMPER, DISCONNECT_ABUSE, GM_ABUSE, ...
  score INT NOT NULL, evidence JSONB NOT NULL,   -- ID ledger, thống kê, mẫu input (không chứa PII thô)
  status VARCHAR(12) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','REVIEWING','CONFIRMED','DISMISSED')),
  reviewer_id UUID, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), resolved_at TIMESTAMPTZ
);
CREATE INDEX ix_security_flag_open ON security_flag(status, score DESC) WHERE status IN ('OPEN','REVIEWING');
```
- <a id="sec-r-058"></a>**SEC-R-058** Job phân tích chạy theo lô (mỗi giờ / mỗi ngày) trên **bản sao đọc** (TD-03 §3), không làm chậm DB chính. Cờ hiện trong GM panel theo điểm giảm dần, kèm bằng chứng có thể bấm vào ledger.

### 5.7. Xử phạt & kháng nghị
- <a id="sec-r-060"></a>**SEC-R-060** **Thang xử phạt** (bảng `sanction`):

| Mức | Biện pháp | Ai quyết định |
|-----|-----------|---------------|
| 1 | Cảnh cáo (thư hệ thống) | Tự động hoặc GM |
| 2 | Hạn chế chức năng tạm (mute chat, khóa giao dịch, khóa PvP `SLAUGHTER`, khóa thu thập) 1–7 ngày | Tự động (ngưỡng rõ ràng) hoặc GM |
| 3 | Khóa tài khoản tạm 1–30 ngày + thu hồi lợi ích bất hợp pháp | GM |
| 4 | Khóa vĩnh viễn | GM_SENIOR + người duyệt thứ hai (SEC-R-023) |

- <a id="sec-r-061"></a>**SEC-R-061** **Khóa tài khoản không làm nhân vật già đi:** tuổi không trôi khi không online (GD-26 LC-R-002) nên khóa tạm không lấy thọ nguyên của người bị xử oan. Khi khóa: ngắt kết nối, nhân vật ở hiểm địa → xử lý như `SERVER_RECOVERY` (thoát an toàn, GD-28 DZ-R-080) để không biến xử phạt thành cái chết.
- <a id="sec-r-062"></a>**SEC-R-062** **Kháng nghị:** người bị xử phạt mức ≥ 2 gửi kháng nghị trong 30 ngày (`POST /v1/support/appeals`); người xem xét **khác** người ra quyết định; trả lời trong 7 ngày. Xử oan → gỡ phạt, khôi phục (bao gồm khôi phục vật phẩm bị thu từ ledger), bồi thường cosmetic.
- <a id="sec-r-063"></a>**SEC-R-063** Mọi xử phạt ghi `sanction { accountId, level, kind, reason, evidenceFlagIds, actorId, startsAt, endsAt }` và `admin_audit`; người chơi xem được lý do và thời hạn.

DDL đề xuất cho xử phạt và kháng nghị:

```sql
CREATE TABLE sanction (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES account(id),
  level SMALLINT NOT NULL CHECK (level BETWEEN 1 AND 4),
  kind VARCHAR(32) NOT NULL,
  reason TEXT NOT NULL,
  evidence_flag_ids JSONB NOT NULL DEFAULT '[]',
  actor_id UUID NOT NULL,
  approved_by UUID,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ,
  revoked_at TIMESTAMPTZ,
  revoked_by UUID,
  revoke_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (ends_at IS NULL OR ends_at > starts_at),
  CHECK (level NOT IN (2, 3) OR
         (ends_at IS NOT NULL AND ends_at >= starts_at + INTERVAL '1 day')),
  CHECK (level <> 2 OR ends_at <= starts_at + INTERVAL '7 days'),
  CHECK (level <> 3 OR ends_at <= starts_at + INTERVAL '30 days'),
  CHECK (level <> 4 OR
         (ends_at IS NULL AND approved_by IS NOT NULL AND approved_by <> actor_id))
);
CREATE INDEX ix_sanction_account_time ON sanction(account_id, created_at DESC);

CREATE TABLE appeal (
  id UUID PRIMARY KEY,
  sanction_id UUID NOT NULL REFERENCES sanction(id),
  account_id UUID NOT NULL REFERENCES account(id),
  body TEXT NOT NULL,
  reviewer_id UUID,
  response TEXT,
  upheld BOOLEAN,
  submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  resolved_at TIMESTAMPTZ
);
CREATE INDEX ix_appeal_sanction_time ON appeal(sanction_id, submitted_at DESC);
CREATE INDEX ix_appeal_pending ON appeal(submitted_at) WHERE resolved_at IS NULL;
```

`appeal.reviewer_id` phải khác `sanction.actor_id`; `upheld = false` yêu cầu thu hồi sanction và khôi phục quyền lợi theo SEC-R-062. Đây là kiểm tra nghiệp vụ trong cùng transaction xử lý kháng nghị.

---

## 6. Bảo vệ dữ liệu

### 6.1. Khi truyền
- <a id="sec-r-070"></a>**SEC-R-070** TLS 1.2+ (ưu tiên 1.3) cho mọi kết nối công khai (HTTPS, WSS) và nội bộ (API ↔ PostgreSQL, Valkey, Game Server; mTLS ở SEC-R-020). Chứng chỉ tự gia hạn (ACME).

### 6.2. Khi lưu
- <a id="sec-r-071"></a>**SEC-R-071** Mã hóa đĩa (volume) cho PostgreSQL, Valkey (AOF), log; **backup mã hóa** (khóa riêng, lưu khác nơi với backup), diễn tập khôi phục hằng tháng (TD-03 §3). Trường đặc biệt nhạy cảm (email) có thể mã hóa cấp ứng dụng nếu cần tra cứu bằng băm — quyết định ở SEC-Q-2.

### 6.3. Bí mật
- <a id="sec-r-072"></a>**SEC-R-072** Bí mật (khóa ký JWT, mật khẩu DB / Valkey, khóa thanh toán, khóa API email) nằm trong **kho bí mật** (Vault / secret manager của nhà cung cấp cloud — TD-09), nạp vào dịch vụ lúc chạy; môi trường dev dùng bí mật riêng, không dùng chung với production.
- <a id="sec-r-073"></a>**SEC-R-073** CI chạy quét bí mật (gitleaks hoặc tương đương) ở mỗi PR và trên toàn lịch sử; phát hiện → chặn merge, xoay bí mật bị lộ ngay (coi như đã lộ dù đã xóa commit).

### 6.4. Quyền & log
- <a id="sec-r-074"></a>**SEC-R-074** Tài khoản DB: `app_rw` (API, chỉ DML trên schema `game`), `app_migrator` (chỉ Flyway, chỉ trong pipeline triển khai), `analytics_ro` (bản sao đọc, không thấy cột PII — view che), `gm_ro` không tồn tại (GM đi qua API). Không ai dùng `postgres` superuser ngoài DBA khẩn cấp (ghi nhật ký).
- <a id="sec-r-075"></a>**SEC-R-075** **Log không chứa:** mật khẩu, token, refresh token, nội dung chat riêng (`WHISPER`), email đầy đủ (che `n***@domain`), IP đầy đủ ở log ứng dụng (che octet cuối; IP đầy đủ chỉ ở `account_session` / `login_attempt` với hạn lưu 90 ngày). Logger có bộ lọc trường cấm (TD-01 TA-R-130) và test.
- <a id="sec-r-076"></a>**SEC-R-076** Dữ liệu cá nhân và xóa tài khoản: TD-03 DB-R-100 (Nghị định 13/2023/NĐ-CP).

---

## 7. Chuỗi cung ứng phần mềm
- <a id="sec-r-080"></a>**SEC-R-080** **Khóa phiên bản:** `pnpm-lock.yaml` và Gradle lock / version catalog được commit; CI cài đặt bằng `--frozen-lockfile`; không dùng phiên bản dải (`^`, `latest`) cho phụ thuộc runtime ở production build.
- <a id="sec-r-081"></a>**SEC-R-081** **Quét lỗ hổng:** Dependabot (hoặc Renovate) + `osv-scanner` trong CI; lỗ hổng mức Critical / High ở phụ thuộc runtime → chặn phát hành, phải nâng cấp hoặc ghi ngoại lệ có hạn trong OSS Register (TD-15).
- <a id="sec-r-082"></a>**SEC-R-082** **SBOM** (CycloneDX) sinh cho client, game-server, api-service mỗi bản phát hành, lưu cùng artifact.
- <a id="sec-r-083"></a>**SEC-R-083** **Ký artifact:** image container ký (cosign), triển khai chỉ chấp nhận image đã ký từ CI; bundle client / gamedata gắn hash trong manifest (TD-07 DT-R-090).
- <a id="sec-r-084"></a>**SEC-R-084** Phụ thuộc mới phải qua OSS Register (TD-15): license, mức độ duy trì, số maintainer; ưu tiên ít phụ thuộc. Mã mượn từ dự án khác (vd. đoạn tham khảo từ T5C, D-23): rà soát trước khi đưa vào (script cài đặt, phụ thuộc lạ) và ghi công theo TD-15.

---

## 8. Client mobile / Capacitor và thanh toán
- <a id="sec-r-090"></a>**SEC-R-090** **Biên lai mua hàng** (App Store / Google Play) **luôn xác minh phía server** với API của cửa hàng (App Store Server API / Google Play Developer API) trước khi cấp quyền sở hữu (TD-01 TA-R-164); lưu `purchase_receipt { store, transactionId UNIQUE, productId, accountId, status }` chống dùng lại biên lai; xử lý thông báo hoàn tiền (server-to-server) → thu hồi cosmetic tương ứng.

DDL đề xuất:

```sql
CREATE TABLE purchase_receipt (
  transaction_id VARCHAR(128) PRIMARY KEY,
  store VARCHAR(16) NOT NULL,
  product_id VARCHAR(128) NOT NULL,
  account_id UUID NOT NULL REFERENCES account(id),
  status VARCHAR(16) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  verified_at TIMESTAMPTZ,
  refunded_at TIMESTAMPTZ
);
```

Chỉ lưu định danh và trạng thái cần cho chống dùng lại / xử lý hoàn tiền; không lưu payload biên lai thô.
- <a id="sec-r-091"></a>**SEC-R-091** Thanh toán web qua cổng thanh toán: chỉ tin **webhook đã xác minh chữ ký** từ cổng, không tin tham số trả về trình duyệt.
- <a id="sec-r-092"></a>**SEC-R-092** App Capacitor: tắt gỡ lỗi WebView ở bản phát hành, giới hạn điều hướng WebView về origin của game, không nạp mã từ xa ngoài bundle đã ký (trừ asset / gamedata có hash), lưu token ở kho bảo mật hệ điều hành (SEC-R-016).

---

## 9. DDoS & flood
- <a id="sec-r-100"></a>**SEC-R-100** Mọi lưu lượng công khai đi qua **CDN / WAF** có chống DDoS (lớp 3/4 và lớp 7): API REST và cổng WebSocket của Game Server không lộ IP gốc; tường lửa chỉ nhận từ dải IP của CDN / load balancer.
- <a id="sec-r-101"></a>**SEC-R-101** **Giới hạn kết nối WebSocket:** tối đa 3 kết nối đồng thời / tài khoản (đăng nhập mới ngắt cũ), 20 / IP (nới cho IP có nhiều tài khoản hợp lệ qua danh sách cho phép, vd. quán net); kết nối không gửi join hợp lệ trong 5s → đóng; kích thước khung tối đa 2 KB (TD-02 NW-R-065).
- <a id="sec-r-102"></a>**SEC-R-102** WAF chặn mẫu tấn công phổ biến ở REST (OWASP CRS), giới hạn kích thước body (64 KB, trừ endpoint tải lên nếu có), giới hạn tần suất ở biên (bổ sung SEC-R-033).
- <a id="sec-r-103"></a>**SEC-R-103** Khi bị tấn công: bật chế độ "đăng nhập chậm" (hàng đợi đăng nhập), ưu tiên phiên đang chơi; Game Server tiếp tục chạy với người đã kết nối.

---

## 10. Ứng phó sự cố & kiểm thử bảo mật

### 10.1. Runbook ngắn
| Sự cố | Bước ngay | Sau đó |
|-------|-----------|--------|
| **Lộ khóa ký JWT / service token** | Thu hồi khóa trong JWKS, phát khóa mới, buộc mọi phiên đăng nhập lại | Điều tra nguồn lộ, rà `admin_audit` / ledger trong khoảng lộ |
| **Lộ mật khẩu DB / bí mật khác** | Xoay bí mật, khởi động lại dịch vụ với bí mật mới | Rà truy cập DB, kiểm tra dữ liệu bị đọc |
| **Lỗi nhân bản vật phẩm / tiền (dupe)** | Bật cờ tính năng tắt chức năng liên quan (giao dịch, chế tác…), đóng băng tài khoản nghi lợi dụng | Truy ledger tìm mọi bản sao, thu hồi bằng bút toán bù, vá, test hồi quy, thông báo |
| **Tài khoản GM bị chiếm** | Khóa tài khoản GM, thu hồi phiên | Rà `admin_audit`, hoàn tác hành động trái phép |
| **Rò rỉ dữ liệu cá nhân** | Cô lập, chặn nguồn | Đánh giá phạm vi, thông báo người dùng và cơ quan chức năng theo quy định (Nghị định 13/2023), báo cáo sau sự cố |
| **DDoS** | SEC-R-103, liên hệ nhà cung cấp CDN | Báo cáo, điều chỉnh WAF |

- <a id="sec-r-110"></a>**SEC-R-110** **Cờ tính năng khẩn cấp** (kill switch) cho từng hệ thống có giá trị (giao dịch, đấu giá, chế tác từng loại, thư, PvP, mở rương): bật / tắt qua admin không cần triển khai, ghi `admin_audit`. Đây là công cụ chính khi phát hiện dupe.
- <a id="sec-r-111"></a>**SEC-R-111** Mỗi sự cố bảo mật có báo cáo sau sự cố (không đổ lỗi cá nhân) trong 7 ngày, ghi vào Decision Log nếu dẫn tới thay đổi kiến trúc.

### 10.2. Kiểm thử
- <a id="sec-r-112"></a>**SEC-R-112** **Test tự động:** mỗi endpoint có test IDOR (gọi bằng tài khoản khác → 403), test idempotency (TA-AC-001..004), test rate limit; fuzz message WebSocket (payload ngẫu nhiên / sai schema không làm sập room).
- <a id="sec-r-113"></a>**SEC-R-113** **SAST** (Semgrep / CodeQL) trong CI; **DAST** cơ bản (OWASP ZAP) trên staging trước mỗi bản Beta.
- <a id="sec-r-114"></a>**SEC-R-114** **Pentest bởi bên ngoài** trước Closed Beta 1 (PLAN Phase 5a) và trước Open Beta, phạm vi: xác thực, API, WebSocket / Game Server, logic kinh tế (nhân đồ, giao dịch), GM panel, thanh toán.

---

## 11. Tham số
```jsonc
// cấu hình API Service (không phải gamedata; nằm trong cấu hình triển khai)
{
  "auth": { "argon2": { "memoryKiB": 65536, "iterations": 3, "parallelism": 1 },
            "accessTtlMin": 15, "refreshTtlDays": 30, "signingKeyRotationDays": 30,
            "loginCaptchaAfter": 5, "loginLockAfter": 10, "loginWindowMin": 15, "ipBlockPerHour": 50 },
  "gm": { "sessionHours": 8, "idleLockMin": 15, "gmGrantApprovalValue": 5000 },
  "ws": { "maxConnPerAccount": 3, "maxConnPerIp": 20, "joinTimeoutSec": 5 },
  "anticheat": { "clockDriftPct": 0.05, "botScoreChallenge": 60, "botScoreFlag": 100,
                 "rmtPriceLowPct": 0.20, "rmtPriceHighPct": 5.0, "newAccountDays": 7 },
  "retention": { "loginAttemptDays": 90, "sessionIpDays": 90 }
}
```

---

## 12. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="sec-ac-001"></a>SEC-AC-001 | Tài khoản A | Gọi `POST /v1/characters/{id của B}/inventory/equip` | `403 FORBIDDEN`, không thay đổi dữ liệu |
| SEC-AC-002 | 10 lần đăng nhập sai trong 15 phút | Lần 11 (đúng mật khẩu) | Từ chối, tài khoản khóa 15 phút, email cảnh báo được gửi |
| SEC-AC-003 | Refresh token R1 đã xoay thành R2 | Dùng lại R1 | Thu hồi toàn bộ chuỗi phiên (R2 cũng hết hiệu lực), email cảnh báo |
| SEC-AC-004 | Join ticket đã dùng | Dùng lại | Game Server từ chối |
| SEC-AC-005 | Service token không có scope `inventory.grant` | Gọi `/internal/v1/inventory/grant` | `403` |
| SEC-AC-006 | Request tới `/internal/*` từ internet | Qua load balancer công khai | Bị chặn ở biên |
| SEC-AC-007 | GM tạo yêu cầu phát 10.000 linh thạch | Cùng GM tự duyệt | Từ chối (cần người thứ hai) |
| SEC-AC-008 | `loot/grant` gửi 50 dòng | API | Từ chối (> 20); gắn cờ |
| SEC-AC-009 | Bundle client production | Quét chuỗi | Không có khóa, URL nội bộ, trường `serverOnly` |
| SEC-AC-010 | Commit chứa chuỗi giống khóa bí mật | CI | Chặn merge |
| SEC-AC-011 | Client gửi `clientTimeMs` trôi nhanh 20% | 60s | Mô phỏng không đổi tốc độ; tín hiệu `CLOCK_DRIFT` ghi nhận |
| SEC-AC-012 | Hai tài khoản cùng thiết bị: A bán vật phẩm giá trị 10.000 cho B với giá 100 | Job RMT | `security_flag RMT_SUSPECT` có bằng chứng `trade_session` |
| SEC-AC-013 | A (đại ác nhân) bị B (tài khoản liên quan) hạ 3 lần trong ngày | Truy nã | Tiền thưởng giữ lại, cờ `PVP_COLLUSION` |
| SEC-AC-014 | Tài khoản bị khóa tạm khi đang ở hiểm địa | Khóa | Nhân vật thoát an toàn (không chết), tuổi không trôi trong thời gian khóa |
| SEC-AC-015 | Biên lai App Store dùng lần hai trên tài khoản khác | Xác minh | Từ chối (`transactionId` đã dùng) |
| SEC-AC-016 | Thông báo hoàn tiền từ cửa hàng | Server nhận | Thu hồi cosmetic, ghi ledger |
| SEC-AC-017 | Log ứng dụng sau 1.000 request đăng nhập / chat riêng | Quét log | Không có mật khẩu, token, nội dung `WHISPER`, email đầy đủ |
| SEC-AC-018 | Payload WebSocket ngẫu nhiên 100.000 lần (fuzz) | Gửi vào room | Room không sập, tick p99 trong ngân sách |
| SEC-AC-019 | Phát hiện lỗi nhân đồ ở giao dịch | Admin bật kill switch giao dịch | Mọi request giao dịch trả `503`/mã tắt tính năng trong ≤ 10s, `admin_audit` có bản ghi |
| SEC-AC-020 | Kháng nghị xử phạt | GM xử lý | Người xử lý khác người ra quyết định ban đầu |

---

## 13. Câu hỏi mở
- [ ] **SEC-Q-1:** Có yêu cầu xác minh số điện thoại khi đăng ký (giảm tài khoản rác / RMT, liên quan GD-14 SO-Q-1) hay chỉ ở các chức năng nhạy cảm (giao dịch, kênh thế giới)?
- [ ] **SEC-Q-2:** Mã hóa email cấp ứng dụng (tra cứu bằng băm) có cần ngay từ MVP không?
- [ ] **SEC-Q-3:** Công cụ phát hiện bot dùng quy tắc (MVP) hay mô hình học máy sau Beta khi đủ dữ liệu telemetry?
- [ ] **SEC-Q-4:** Kho bí mật và WAF / CDN chọn nhà cung cấp nào (phụ thuộc PLAN Q10)?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến, mô hình đe dọa STRIDE, xác thực (Argon2id, khóa đăng nhập, 2FA), token (JWT bất đối xứng + xoay khóa, refresh xoay vòng + phát hiện tái dùng, join ticket), service token có phạm vi + mTLS, vai trò GM + 2FA + 4-eyes + `admin_audit`, bảo vệ API (IDOR, validate, kiểm tra hợp lý API nội bộ, rate limit 3 trục, chống phát lại, CORS / CSP / header), chống gian lận (kiểm tra Game Server, bot, đa tài khoản / RMT, map hack, lạm dụng PvP), `security_flag`, thang xử phạt + kháng nghị, bảo vệ dữ liệu, chuỗi cung ứng, mobile / biên lai, DDoS, runbook + kill switch, kiểm thử, nghiệm thu |
| v0.2 | 2026-10-25 | — | SEC-R-084: bỏ giả định fork T5C (D-23) |
| v0.3 | 2026-10-25 | — | D-24: Valkey thay Redis (tên khóa / lệnh không đổi) |
