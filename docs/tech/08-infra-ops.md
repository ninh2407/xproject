# TD-09 — Hạ tầng & Vận hành

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-09 · Prefix ID: `OPS` |
| Owner | Tech Lead + DevOps / SRE (kiêm nhiệm ở team nhỏ) |
| Trạng thái | 🟡 Bản nháp v0.1 |
| Cần trước | Phase 0 (dev / CI), Phase 2 (staging), Phase 4 (prod Alpha, on-call), Phase 5 (load test 1.000+ CCU, pentest) |
| Quyết định nền | PLAN §5.6, Q10 (nhà cung cấp cloud — **chưa chốt**), D-19 / D-23 (Colyseus + Spring Boot), D-22 (đa nền tảng), **Valkey thay Redis** (2026-10-25, BSD-3, tương thích giao thức) |
| Liên quan | [TD-01](00-tdd.md) §13, §15, §17 · [TD-03](02-database.md) §3, §7, §12 · [TD-07](06-gamedata.md) §8 · [TD-08](07-security.md) · [TD-10](09-conventions.md) §11 · [TD-12](11-world-maps.md) §7, §9 · TD-11 Performance · [GD-26](../design/03-lifecycle.md) LC-R-150 · [TD-15](14-oss-register.md) |

> Tài liệu này quy định **môi trường, đóng gói, triển khai, quan sát, sao lưu, ứng phó sự cố, bảo trì và chi phí**. Trung lập nhà cung cấp cloud cho tới khi chốt Q10. Trong mọi tài liệu, "Valkey" là kho key-value / streams (trước đây ghi "Redis"); tên khóa và lệnh giữ nguyên (TD-03 §12).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-09 | Mục |
|--------------|-----------------|-----|
| TD-01 §13, §15, §17 | Cách triển khai, cảnh báo, SLO, runbook | §3, §4, §7 |
| TD-03 §3 | Quy trình backup / khôi phục / diễn tập | §5 |
| TD-08 | Kho bí mật, WAF / CDN, ứng phó sự cố bảo mật | §6, §7 |
| TD-12 §9 | Drain Game Server trong pipeline phát hành | §3.4 |
| PLAN Q10, roadmap | Tiêu chí chọn cloud, chi phí theo giai đoạn | §2.3, §9 |

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="ops-r-001"></a>**OPS-R-001** | **Mọi hạ tầng là mã** (IaC, cấu hình, dashboard, cảnh báo) trong repo `infra/`; không sửa tay môi trường prod ngoài quy trình sự cố có ghi biên bản. |
| <a id="ops-r-002"></a>**OPS-R-002** | **Một artifact chạy mọi môi trường**: image container build một lần, đẩy qua dev → staging → prod; khác nhau chỉ ở cấu hình / bí mật. |
| <a id="ops-r-003"></a>**OPS-R-003** | Mỗi bản phát hành gồm **cùng một** `gamedataVersion` cho client, Game Server, API (TD-01 TA-R-150, TD-07 DT-R-091). |
| <a id="ops-r-004"></a>**OPS-R-004** | Không có thay đổi prod nào **không rollback được** trong ≤ 15 phút (migration theo expand → contract, TD-03 DB-R-081). |
| <a id="ops-r-005"></a>**OPS-R-005** | Khôi phục PostgreSQL được **diễn tập mỗi tháng**; một bản backup chưa từng khôi phục thử coi như không có. |

---

## 2. Môi trường & topology

### 2.1. Các môi trường

| Môi trường | Mục đích | Dữ liệu | Triển khai |
|-----------|----------|---------|-----------|
| **dev (local)** | Lập trình | Giả | `docker compose` (PostgreSQL, Valkey, api-service, game-server ×1, client Vite) |
| **ci** | Test tích hợp tạm thời | Giả, xóa sau job | Testcontainers / compose trong runner |
| **staging** | Kiểm thử trước phát hành, playtest nội bộ | Giả + bản sao **đã ẩn danh** từ prod (khi có) | Tự động sau mỗi merge `main` |
| **loadtest** | Đo tải 150 / room, 1.000+ CCU | Bot | Dựng theo yêu cầu (IaC), tắt sau khi đo |
| **prod** | Người chơi thật | Thật | Theo lịch, có cổng duyệt (§3.3) |

### 2.2. Topology prod

```
            Người chơi (web PC / PWA / app Capacitor)
                         │
   ┌─────────── CDN + WAF (asset GLB/KTX2, bundle client, gamedata, chống DDoS L3/L4/L7) ───────────┐
   │                                                                                               │
   ▼ HTTPS                                                                         WSS (sticky theo room)
 Load balancer L7 ──► API Service ×2–N (stateless) ─┐                ┌──► Game Server ×N tiến trình
                       + World leader + outbox relay │                │     (1 tiến trình / vCPU)
                                                     ▼                ▼
                                       PostgreSQL primary (+ replica)   Valkey (primary + replica / Sentinel)
                                       PITR, backup mã hóa             AOF bật
                         ▲
              Quan sát: Prometheus · Grafana · Loki · OTel collector · cảnh báo → on-call
```

- <a id="ops-r-010"></a>**OPS-R-010** Mạng tách 3 vùng: **công khai** (CDN, LB), **ứng dụng** (API, Game Server — Game Server nhận WSS qua LB / gateway, không lộ cổng nội bộ), **dữ liệu** (PostgreSQL, Valkey — chỉ ứng dụng vào được). Kênh nội bộ API ↔ Game Server theo TD-08 (service token + mTLS / mạng riêng).
- <a id="ops-r-011"></a>**OPS-R-011** Game Server cần **định tuyến tới đúng tiến trình** chứa room: endpoint từng tiến trình (`gs-{n}.…`) trả trong vé vào room (TD-01 §14, TD-12 §5), LB / gateway chuyển thẳng WSS tới tiến trình đó.

### 2.3. Kích thước ban đầu & mở rộng

| Giai đoạn | Mục tiêu | API | Game Server | PostgreSQL | Valkey |
|-----------|----------|-----|-------------|-----------|--------|
| Staging | Playtest nội bộ | 1 × 2 vCPU | 1 VM 4 vCPU (2–4 tiến trình) | 2 vCPU / 4GB | 1 nút 1GB |
| Alpha (≈ 300 người) | Phase 4 | 2 × 2 vCPU | 1 VM 8 vCPU | 4 vCPU / 16GB + PITR | Primary + replica 2GB |
| Beta (≈ 2.000 CCU) | Phase 5 | 3–4 × 4 vCPU | 2–3 VM 8 vCPU | 8 vCPU / 32GB + replica đọc | Primary + replica 4GB, Sentinel |
| Launch (≈ 5.000 CCU) | Phase 6 | 6+ × 4 vCPU (tự co giãn) | 5–7 VM 8 vCPU | 16 vCPU / 64GB + replica | 8GB, Sentinel / cluster dịch vụ quản lý |

- <a id="ops-r-012"></a>**OPS-R-012** Mở rộng **ngang** API theo CPU / p95; Game Server theo **số người / tiến trình** (`softCapPlayersPerProcess` 1.000, TD-12 WM-R-020) và tick p99 — thêm VM, World Directory tự đặt room mới. PostgreSQL mở rộng **dọc** + replica đọc; tách DB theo module chỉ khi có ADR (DB-Q-2).
- <a id="ops-r-013"></a>**OPS-R-013** Con số ở bảng trên là **giả định ban đầu**, phải hiệu chỉnh bằng load test (TD-01 §16, TD-02 NW-AC-009) trước mỗi giai đoạn.

### 2.4. Tiêu chí chọn cloud (cho PLAN Q10)

| Tiêu chí | Trọng số | Ghi chú |
|----------|:---:|---------|
| Vùng dữ liệu gần Việt Nam (Singapore / Việt Nam), độ trễ tới người chơi VN ≤ 50ms | Cao | Combat hành động (TD-02) |
| Có **PostgreSQL quản lý** (PITR, replica) và **Valkey quản lý** (hoặc VM tự vận hành dễ) | Cao | Giảm việc vận hành cho team nhỏ |
| Chi phí VM CPU-optimized + băng thông ra (egress) | Cao | Game Server và WSS tốn egress |
| CDN + WAF / chống DDoS tích hợp hoặc dùng nhà cung cấp CDN riêng | Trung bình | |
| Tuân thủ quy định lưu trữ dữ liệu tại VN (nếu áp dụng cho giấy phép trò chơi) | Cao | Kiểm tra pháp lý |
| Không khóa nhà cung cấp: dịch vụ chuẩn (VM, container, Postgres, Valkey) | Trung bình | IaC trung lập (§3.1) |

---

## 3. Đóng gói, IaC, CI/CD

### 3.1. Đóng gói & IaC
- <a id="ops-r-020"></a>**OPS-R-020** Container OCI cho `api-service` (JRE 21 distroless), `game-server` (Node 22 slim / distroless), công cụ (relay outbox chạy trong API). Client là **tệp tĩnh** trên CDN (không container). Image có nhãn `version`, `gitCommit`, `gamedataVersion`.
- <a id="ops-r-021"></a>**OPS-R-021** IaC bằng **Terraform / OpenTofu** (hạ tầng: mạng, VM / container service, DB, Valkey, CDN, DNS) + **Ansible** hoặc cloud-init (cấu hình VM khi tự vận hành). Mô-đun tách theo môi trường; trạng thái IaC lưu từ xa, có khóa.
- <a id="ops-r-022"></a>**OPS-R-022** Chạy container: **Docker Compose / systemd trên VM** cho Alpha (đơn giản); chuyển sang **Kubernetes** (hoặc container service quản lý) chỉ khi cần tự co giãn nhiều nút (PLAN §5.6) — ghi ADR.

### 3.2. Pipeline CI
```
PR:   lint → type-check → test (unit / tích hợp / hợp đồng / testvectors) → validator gamedata + bản đồ
      → kiểm tra ranh giới, sổ bảng, schema sự kiện → license + SBOM (TD-15) → quét bí mật / lỗ hổng / SAST (TD-08)
      → báo cáo độ phủ AC (TD-10 CC-R-080)
main: build image (1 lần) → ký image (cosign) + SBOM đính kèm → đẩy registry
      → build bundle gamedata + asset bản đồ (TD-07, TD-13) → CDN (đường dẫn theo hash)
      → triển khai staging tự động → smoke test + bot luồng chính (TD-06)
```

### 3.3. Phát hành prod
- <a id="ops-r-030"></a>**OPS-R-030** Lịch phát hành cố định (vd. **thứ Tư 05:00–06:00**, trùng giờ reset 05:00 của GD-15 / GD-25) + bản vá khẩn khi P1 / P2. Cổng duyệt: Tech Lead + 1 người; checklist: staging xanh ≥ 24h, migration đã thử trên bản sao dữ liệu, ghi chú phát hành, kế hoạch rollback.
- <a id="ops-r-031"></a>**OPS-R-031** **Thứ tự triển khai một bản phát hành:**
  1. Đẩy asset + bundle gamedata mới lên CDN (đường dẫn theo hash, bản cũ vẫn còn).
  2. Migration **expand** (thêm cột / bảng; tương thích bản cũ).
  3. **API Service rolling** (từng bản một, health check `/actuator/health/readiness`).
  4. **Game Server drain** theo TD-12 WM-R-090: tiến trình mới khởi động với `gamedataVersion` mới; tiến trình cũ ngừng nhận room, thông báo 5 phút, chuyển người chơi bản đồ mở sang tiến trình mới, instance chờ kết thúc ≤ 30 phút.
  5. Client nhận `gamedataVersion` mới qua `/v1/meta` / `S2C_Welcome` → tải lại bundle (TD-02 NW-AC-015).
  6. Migration **contract** (bỏ cột cũ) ở **bản phát hành sau**.
- <a id="ops-r-032"></a>**OPS-R-032** Trong lúc rolling, API bản cũ và mới **cùng chạy**: hợp đồng API / sự kiện phải tương thích ngược một bản (TD-04 chặn thay đổi phá vỡ; TD-01 TA-R-086 phát song song hai phiên bản sự kiện).
- <a id="ops-r-033"></a>**OPS-R-033** **Rollback:** API → triển khai lại image trước; Game Server → drain ngược sang image trước; gamedata → trỏ `gamedataVersion` về bản trước (bundle cũ còn trên CDN). Migration expand không cần rollback (tương thích). Mục tiêu ≤ 15 phút (OPS-R-004).
- <a id="ops-r-034"></a>**OPS-R-034** Bản cập nhật app iOS / Android (Capacitor, D-22) chỉ khi đổi phần native; nội dung game tải qua web bundle — server giữ tương thích với **ít nhất 2 phiên bản app** gần nhất, ép cập nhật bằng `426 CLIENT_OUTDATED` (TD-04).

---

## 4. Quan sát

### 4.1. Ngăn xếp
Prometheus (metric) · Grafana (dashboard, cảnh báo) · Loki (log) · OpenTelemetry Collector (trace, Tempo hoặc tương đương) — Grafana / Loki / Tempo là **AGPL, chỉ nội bộ** (TD-15). Sentry hoặc tương đương (tự host) cho lỗi client (tùy chọn).

### 4.2. Dashboard bắt buộc
| Dashboard | Nội dung |
|-----------|----------|
| Tổng quan dịch vụ | CCU, người / room, số room, tỉ lệ lỗi API theo mã, p95 / p99 theo endpoint |
| Game Server | Tick p50 / p99 theo room, thời gian từng bước tick (TD-06), hàng đợi thử lại, người / tiến trình, băng thông / người |
| Chuyển bản đồ | `transfer_ms` từng bước (TD-12 WM-R-044), tỉ lệ vé hết hạn |
| Dữ liệu | PostgreSQL (kết nối, khóa chờ, truy vấn chậm, replica lag, dung lượng, phân vùng tương lai), Valkey (bộ nhớ, độ trễ streams, `evt:dlq`) |
| Nền tảng sự kiện | Độ trễ relay outbox, số chưa phát, consumer lag |
| Kinh tế | Từ GD-13 (dòng tiền, faucet / sink, giá) |
| Một mạng | Tử vong theo nguyên nhân, `needsReview`, cái chết / giờ (phát hiện bất thường) |
| Bảo mật | Từ TD-08 (đăng nhập thất bại, `security_flag`, rate limit) |

### 4.3. Cảnh báo
| Mức | Điều kiện | Gửi tới |
|-----|-----------|---------|
| P1 | API tỉ lệ 5xx > 5% trong 5 phút · không có tiến trình Game Server khỏe · PostgreSQL primary mất · **số cái chết thật / 10 phút > 5× trung bình** (dấu hiệu lỗi) | Gọi on-call ngay |
| P2 | Tick p99 > 40ms trong 1 phút (TA-R-133) · outbox chưa phát > 30s · replica lag > 60s · Valkey mất · `transfer_ms` p95 > 5s | On-call (giờ hành chính gọi, ngoài giờ tin nhắn) |
| P3 | Hàng đợi thử lại GS > 1.000 · `evt:dlq` có phần tử · đối soát ledger lệch (TA-R-062) · phân vùng tương lai < 2 (DB-R-040) · chứng chỉ TLS < 14 ngày | Kênh vận hành |
| P4 | Dung lượng đĩa > 70% · chi phí vượt ngân sách tháng 10% | Kênh vận hành |

### 4.4. SLO
| SLO | Mục tiêu (30 ngày) |
|-----|-------------------|
| Tỉ lệ request API công khai thành công (không tính 4xx nghiệp vụ) | ≥ 99,5% |
| Tick p99 < 25ms | ≥ 99% số phút |
| Chuyển bản đồ p95 < 2s | ≥ 99% |
| Thời gian khả dụng đăng nhập + vào game | ≥ 99,5% (trừ bảo trì báo trước) |
| Không mất dữ liệu đã xác nhận (vật phẩm / tiền / tu vi) | 100% (RPO 5 phút chỉ cho thảm họa) |

- <a id="ops-r-040"></a>**OPS-R-040** Ngân sách lỗi SLO cạn → đóng băng phát hành tính năng, ưu tiên ổn định tới khi phục hồi.

---

## 5. Sao lưu, khôi phục, DR
- <a id="ops-r-050"></a>**OPS-R-050** PostgreSQL: WAL archiving liên tục + base backup hằng ngày, **mã hóa**, lưu ở vùng / tài khoản lưu trữ khác; giữ 35 ngày PITR + bản tháng 12 tháng. **RPO ≤ 5 phút, RTO ≤ 1 giờ** (TD-03 §3).
- <a id="ops-r-051"></a>**OPS-R-051** Valkey: AOF + snapshot; mất dữ liệu Valkey **chấp nhận được** (TD-03 DB-R-090) — không phải đường khôi phục chính.
- <a id="ops-r-052"></a>**OPS-R-052** **Diễn tập khôi phục hằng tháng** trên môi trường riêng: khôi phục tới mốc thời gian, chạy job đối soát ledger (TA-R-062), so tổng tiền / vật phẩm, ghi thời gian thực tế (DB-AC-011).
- <a id="ops-r-053"></a>**OPS-R-053** **DR vùng:** Alpha / Beta chấp nhận khôi phục từ backup sang vùng khác (RTO vài giờ); từ Launch cân nhắc replica khác vùng (ADR theo chi phí).
- <a id="ops-r-054"></a>**OPS-R-054** Asset / bundle trên CDN có bản gốc ở object storage có versioning; mã nguồn và IaC trong Git (bản sao ngoài).

---

## 6. Bí mật, cấu hình, cờ
- <a id="ops-r-060"></a>**OPS-R-060** Bí mật (mật khẩu DB, khóa ký JWT, khóa service token, khóa API thanh toán) trong **kho bí mật** (Vault hoặc dịch vụ quản lý của cloud), cấp cho dịch vụ lúc chạy, xoay vòng theo lịch TD-08; không nằm trong image / repo (TD-08, gitleaks trong CI).
- <a id="ops-r-061"></a>**OPS-R-061** Cấu hình không bí mật: biến môi trường `TL_*` (TD-10 §3) + file mặc định trong repo; thay đổi qua PR.
- <a id="ops-r-062"></a>**OPS-R-062** **Cờ tính năng / kill switch** (`ff.*`: `pvp.enabled`, `beasts.enabled`, `auction.enabled`, `maVuc.enabled`, `worldEvents.enabled`… — TD-06, TD-08) đặt ở API, phát cho Game Server qua `/internal/v1/meta` + sự kiện; bật / tắt bởi người có vai trò vận hành, ghi `admin_audit`, có hiệu lực ≤ 60s mà không cần triển khai.

---

## 7. On-call & ứng phó sự cố

### 7.1. Mức độ
| Mức | Định nghĩa | Phản hồi | Ví dụ |
|-----|-----------|----------|-------|
| **P1** | Không chơi được / mất dữ liệu / chết hàng loạt do lỗi / rò rỉ dữ liệu | ≤ 15 phút, mọi lúc | API sập, DB primary mất |
| **P2** | Suy giảm lớn, một phần người chơi | ≤ 1 giờ | Một tiến trình GS sập liên tục, chuyển bản đồ chậm |
| **P3** | Lỗi có cách tránh, ảnh hưởng nhỏ | Giờ làm việc | Một cửa hàng sai giá |
| **P4** | Thẩm mỹ, tối ưu | Lên kế hoạch | |

- <a id="ops-r-070"></a>**OPS-R-070** On-call luân phiên tuần (team nhỏ: 2–3 người), có người dự phòng; công cụ gọi (PagerDuty / Opsgenie / tương đương hoặc bot chat có leo thang).
- <a id="ops-r-071"></a>**OPS-R-071** Quy trình: **phát hiện → xác nhận mức → chỉ định chỉ huy sự cố → giảm thiểu (kill switch / rollback / bảo trì khẩn) → thông báo người chơi (status page, kênh cộng đồng) → khắc phục → biên bản sau sự cố trong 5 ngày** (không đổ lỗi, có hành động cụ thể).

### 7.2. Runbook
| Tình huống | Dấu hiệu | Hành động |
|-----------|----------|-----------|
| **API Service down** | 5xx tăng, health fail | Kiểm tra DB / Valkey / bản phát hành gần nhất → rollback nếu do phát hành; tăng bản sao nếu quá tải. Game Server tự vào chế độ suy giảm (TD-01 §13: chặn hành động cần kết quả ngay, dừng đếm hấp hối sau 10s) — **không cần xử lý thêm ở GS** |
| **Tiến trình Game Server sập** | Mất heartbeat 10s (TD-12 WM-R-091) | Xác nhận World Directory đã xóa room; tiến trình tự khởi động lại (systemd / orchestrator). Hiểm địa của tiến trình đó xử lý `SERVER_RECOVERY` (GD-28 DZ-R-080) — kiểm tra job đã chạy; sập lặp lại → rollback image, thu death log / core dump |
| **Valkey mất** | Kết nối lỗi, ctx stale tăng | Failover replica (Sentinel / dịch vụ quản lý). Hệ quả chấp nhận được (TD-01 §13): phiên Colyseus kết nối lại, `CONTEXT_STALE` tạm thời; outbox giữ sự kiện trong PostgreSQL và phát lại khi Valkey về — kiểm tra relay hết tồn đọng |
| **PostgreSQL failover** | Primary mất | Thăng replica (dịch vụ quản lý tự làm / thủ công theo hướng dẫn), cập nhật endpoint, kiểm tra API kết nối lại; chạy đối soát ledger; nếu mất dữ liệu sau mốc replica → PITR + công bố cửa sổ ảnh hưởng |
| **DDoS** | Lưu lượng bất thường ở CDN / LB, kết nối WS tăng vọt | Bật chế độ thử thách / rate limit chặt ở WAF; giới hạn kết nối WS theo IP / tài khoản (TD-08); chặn ASN / quốc gia tạm thời nếu cần; thông báo bảo trì nếu không giữ được |
| **Gamedata lỗi sau phát hành** | Lỗi nạp, cân bằng vỡ (vd. vật phẩm rơi gấp 100 lần) | Kill switch tính năng liên quan (OPS-R-062) **ngay**; rollback `gamedataVersion` (OPS-R-033); đánh giá thiệt hại kinh tế qua ledger; bù trừ / thu hồi bằng bút toán GM có duyệt hai người (TD-08) |
| **Chết hàng loạt do lỗi** | Cảnh báo P1 "cái chết thật / 10 phút > 5×" | Kill switch hiểm địa / thiên kiếp / PvP liên quan; lấy danh sách cái chết trong cửa sổ từ `death_log` + `needsReview`; **khôi phục nhân vật theo GD-26 LC-R-150 với duyệt hai người (TD-08 SEC-R-023)**; thông báo minh bạch, kèm Bia Vạn Cổ được sửa |
| **Rò rỉ dữ liệu** | Phát hiện truy cập trái phép / dữ liệu lộ | Theo runbook bảo mật TD-08 §10: cô lập, thu hồi khóa / phiên, xoay bí mật, điều tra, thông báo người dùng và cơ quan theo luật (Nghị định 13/2023) |

---

## 8. Bảo trì & thông báo
- <a id="ops-r-080"></a>**OPS-R-080** Bảo trì định kỳ (nếu cần dừng): báo trước **≥ 24 giờ** trong game (`S2C_Notice`, banner đăng nhập) và kênh cộng đồng; trong bảo trì đồng hồ tuổi không chạy (người chơi offline — GD-26 LC-R-002).
- <a id="ops-r-081"></a>**OPS-R-081** **Status page** công khai (tách hạ tầng khỏi game): trạng thái đăng nhập / game / cửa hàng, lịch bảo trì, sự cố đang xử lý.
- <a id="ops-r-082"></a>**OPS-R-082** Bảo trì khẩn có dừng hiểm địa: người đang trong lượt được xử lý `SERVER_RECOVERY` (không phạt, GD-28 DZ-R-080); thiên kiếp đang diễn ra được hoãn / hoàn tác theo GD-04.

---

## 9. Ước tính chi phí hạ tầng (giả định)

> **Giả định:** VM cloud vùng Singapore, giá niêm yết tham khảo (chưa giảm giá cam kết), băng thông WSS ~10–20 KB/s / người chơi (TD-02 §12), asset qua CDN; chưa gồm nhân sự, thanh toán, pháp lý. **Chỉ để lập ngân sách, phải tính lại khi chốt Q10.**

| Giai đoạn | Quy mô | Ước tính / tháng (USD) | Thành phần chính |
|-----------|--------|-----------------------|------------------|
| Phase 0–3 (dev + staging) | Nội bộ | 150 – 400 | 2–3 VM nhỏ, Postgres nhỏ, CI runner |
| Alpha | ≈ 300 người | 400 – 900 | API ×2, GS 1 VM, Postgres có PITR, Valkey có replica, CDN, quan sát |
| Beta | ≈ 2.000 CCU | 1.500 – 3.500 | GS 2–3 VM, Postgres lớn + replica, egress WSS tăng |
| Launch | ≈ 5.000 CCU | 3.500 – 8.000 | GS 5–7 VM, tự co giãn API, DR, WAF trả phí |

- <a id="ops-r-090"></a>**OPS-R-090** Theo dõi chi phí theo dịch vụ / môi trường (tag tài nguyên); tắt môi trường loadtest sau khi dùng; báo cáo chi phí / CCU mỗi tháng.

---

## 10. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="ops-ac-001"></a>OPS-AC-001 | Môi trường staging bị xóa | Chạy IaC | Dựng lại đầy đủ, không thao tác tay |
| OPS-AC-002 | Merge vào `main` | Pipeline | Image ký + SBOM, staging tự cập nhật, smoke test xanh |
| OPS-AC-003 | Phát hành có migration expand | Rolling API | Bản cũ và mới cùng chạy không lỗi |
| OPS-AC-004 | Phát hành lỗi | Rollback | Dịch vụ về bản trước ≤ 15 phút, không mất dữ liệu |
| OPS-AC-005 | Phát hành mới | Game Server drain | Người chơi bản đồ mở chuyển sang tiến trình mới không mất vật phẩm / vị trí (TD-12 WM-AC-011) |
| OPS-AC-006 | Diễn tập khôi phục tháng | Khôi phục PITR | ≤ 1 giờ, đối soát ledger sạch |
| OPS-AC-007 | Tắt `ff.pvp.enabled` | Trong 60s | Không ai gây sát thương PvP được; `admin_audit` có dòng ghi |
| OPS-AC-008 | Giả lập 50 cái chết thật trong 10 phút (gấp 5× trung bình) | Cảnh báo | P1 gửi on-call |
| OPS-AC-009 | Tắt Valkey primary | Failover | Người chơi kết nối lại; outbox phát hết tồn đọng; không mất dữ liệu bền |
| OPS-AC-010 | Bí mật bị commit nhầm | CI | gitleaks chặn merge |
| OPS-AC-011 | Bảo trì báo trước | 24h trước | Banner + `S2C_Notice` + status page cập nhật |

---

## 11. Câu hỏi mở
- [ ] **OPS-Q-1** (= PLAN Q10): Nhà cung cấp cloud và ngân sách tháng — chấm theo §2.4.
- [ ] **OPS-Q-2:** Tự vận hành Valkey / PostgreSQL trên VM (rẻ hơn) hay dùng dịch vụ quản lý (ít việc hơn) ở Alpha?
- [ ] **OPS-Q-3:** Có cần máy chủ đặt tại Việt Nam (yêu cầu lưu trữ dữ liệu của giấy phép trò chơi) ngoài vùng Singapore không? Cần tư vấn pháp lý.
- [ ] **OPS-Q-4:** Khi nào chuyển từ Compose / systemd sang Kubernetes — ngưỡng số VM Game Server hoặc nhu cầu tự co giãn?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến, môi trường + topology prod, kích thước theo giai đoạn, tiêu chí chọn cloud (Q10), container + IaC, pipeline CI, quy trình phát hành (expand → rolling API → drain Game Server → contract), rollback, quan sát (dashboard, cảnh báo P1–P4, SLO), backup / DR, bí mật + kill switch, on-call + 8 runbook, bảo trì + status page, ước tính chi phí, nghiệm thu; dùng Valkey thay Redis |

