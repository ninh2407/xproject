# TD-15 — OSS Register & License Policy

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-15 · Prefix ID: `OSS` |
| Owner | Tech Lead (người duyệt license) + Producer (asset / dịch vụ) |
| Trạng thái | 🟡 Bản nháp v0.5 |
| Cần trước | Phase 0 (trước dependency đầu tiên và trước asset / mô hình AI đầu tiên) |
| Quyết định nền | PLAN §5.7.4 (chính sách license), D-16, D-19, D-22 (mỹ thuật ưu tiên AI, đa nền tảng) |
| Liên quan | [PLAN §5.7, §5.8, §6.6](../../PLAN.md) · [TD-01 TDD](00-tdd.md) §16 (CI) · [TD-07 Gamedata](06-gamedata.md) · [TD-03 Database](02-database.md) · [TD-13 Map Format](12-map-editor.md) |

> Mọi thư viện, công cụ, dịch vụ, asset và mô hình AI bên thứ ba phải có dòng trong **sổ đăng ký** (§4) **trước khi** được commit hoặc dùng để tạo asset phát hành. License trong bảng được ghi theo hiểu biết lúc viết; dòng ⚠️ **chưa được xác minh** — người duyệt phải đối chiếu file `LICENSE` của repo / điều khoản chính thức trước khi đổi thành ✅.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ TD-15 | Mục |
|--------------|-----------------|-----|
| Mọi TD / GD nêu thư viện | Trạng thái được phép dùng | §4 |
| TD-01 §16 (CI) | Danh sách license cấm / cần duyệt cho kiểm tra tự động | §2, §3.3 |
| PLAN §6.6 (mỹ thuật AI) | Điều kiện dùng mô hình AI, hồ sơ nguồn gốc asset | §2.4, §3.4 |
| Bản phát hành | File `THIRD_PARTY_NOTICES` | §3.5 |
| AR-02 / AR-05 (`pnpm art:check`) | Enum `source`, bảng §4.5 máy đọc được, cách tính `license` | §3.3, §3.4, §4.5, §4.7 |
| PM-05 (khảo sát người ngoài) | Điều kiện dùng dịch vụ SaaS thu dữ liệu | OSS-R-022, §4.4 |

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="oss-r-001"></a>**OSS-R-001** | Không thành phần bên thứ ba nào vào repo, bản build hay asset phát hành nếu chưa có dòng trong sổ đăng ký với trạng thái ✅. |
| <a id="oss-r-002"></a>**OSS-R-002** | **Không** nhúng mã GPL / AGPL / SSPL / RSAL vào client, Game Server hay API Service được phát hành hoặc vận hành cho người chơi (§2.1). Các license này chỉ được dùng cho **công cụ nội bộ** không phân phối. |
| <a id="oss-r-003"></a>**OSS-R-003** | **Không sao chép mã** từ dự án chỉ dùng để tham khảo (L2J, aCis, Veloren, và mọi dự án GPL): chỉ đọc ý tưởng (PLAN §5.7.3). |
| <a id="oss-r-004"></a>**OSS-R-004** | Asset phát hành chỉ dùng license cho phép thương mại; **cấm** asset "Non-Commercial" (CC BY-NC…), asset không rõ nguồn, và output của mô hình AI có license không cho thương mại. |
| <a id="oss-r-005"></a>**OSS-R-005** | Mỗi asset tạo bằng AI có **hồ sơ nguồn gốc** (§3.4); không có hồ sơ thì không được đưa vào bản phát hành. |

---

## 2. Chính sách license

### 2.1. Mã nguồn (thư viện, framework, runtime)

| Mức | License | Quy tắc |
|-----|---------|---------|
| ✅ **Tự do** | MIT, Apache-2.0, BSD-2/3-Clause, ISC, zlib, PostgreSQL License, Unlicense, 0BSD | Giữ thông báo bản quyền / NOTICE trong `THIRD_PARTY_NOTICES` (§3.5) |
| ✅ **Có điều kiện** | MPL-2.0, EPL-2.0 | Dùng được nguyên bản; nếu **sửa file** của thư viện thì phải công bố file đã sửa theo license. Ưu tiên không sửa, bọc qua interface |
| ⚠️ **Cần duyệt từng trường hợp** | LGPL-2.1 / 3.0 | Chỉ khi liên kết động / tách rời được; ghi rõ cách đáp ứng điều kiện trong cột Ghi chú |
| ⚠️ **Chỉ công cụ nội bộ** | GPL-2.0/3.0, AGPL-3.0 | Công cụ chạy trong nội bộ team, **không phân phối**, không là một phần dịch vụ cung cấp cho người chơi (vd. k6, Grafana, Loki, Metabase, ComfyUI, Blender). Không import vào code sản phẩm |
| ❌ **Cấm trong sản phẩm** | GPL / AGPL / SSPL / RSAL / BSL (Business Source) cho thành phần chạy trong sản phẩm; license "không thương mại" | |

- <a id="oss-r-010"></a>**OSS-R-010** Runtime có ngoại lệ liên kết (vd. OpenJDK GPLv2 + Classpath Exception) được coi như "Tự do" cho mục đích chạy ứng dụng.
- <a id="oss-r-011"></a>**OSS-R-011** Thành phần **dual / đổi license theo phiên bản** (vd. Redis, §4 ghi chú) phải ghi **phiên bản cụ thể** và license của đúng phiên bản đó; nâng phiên bản = duyệt lại.

### 2.2. Công cụ và dịch vụ
- <a id="oss-r-020"></a>**OSS-R-020** Công cụ dựng nội dung (Blender, ComfyUI…) dù GPL vẫn dùng được: **output** (model, ảnh) không bị ràng buộc bởi license của công cụ, trừ khi điều khoản công cụ nói khác.
- <a id="oss-r-021"></a>**OSS-R-021** Dịch vụ miễn phí có điều khoản riêng (Mixamo, AccuRIG, CDN, analytics SaaS…): ghi link điều khoản + ngày đọc; trạng thái ⚠️ cho tới khi người duyệt xác nhận dùng thương mại được.
- <a id="oss-r-022"></a>**OSS-R-022** Dịch vụ SaaS **thu dữ liệu của người ngoài team** (khảo sát playtest, biểu mẫu, ghi màn hình — PM-05 PT-Q-2) phải có dòng ở §4.4 **trước** lần dùng đầu tiên, ghi: điều khoản + chính sách dữ liệu (link, ngày đọc), nơi lưu dữ liệu, thời hạn giữ, có xóa theo yêu cầu không. Biểu mẫu **không** hỏi email / số điện thoại / tên tài khoản (PM-05 PT-R-002, TD-08 SEC-R-075); liên hệ mời lại giữ ngoài repo. Biểu mẫu tự host bằng phần mềm đã ✅ ở §4 không cần dòng riêng.

### 2.3. Asset
| Mức | License | Quy tắc |
|-----|---------|---------|
| ✅ Ưu tiên | CC0 / Public Domain | Không bắt buộc ghi công; vẫn ghi nguồn trong sổ |
| ✅ Được dùng | CC-BY-4.0, asset mua có license thương mại | **Bắt buộc ghi công** trong màn Credits + `THIRD_PARTY_NOTICES`; lưu hóa đơn / file license cạnh asset |
| ⚠️ Cần duyệt | CC-BY-SA (chia sẻ tương tự) | Chỉ khi không làm "lây" sang asset khác; thường **không dùng** |
| ❌ Cấm | CC-BY-NC, CC-BY-ND (nếu cần sửa), "free for personal use", không rõ nguồn | |

### 2.4. Mô hình AI và output AI (D-22, PLAN §6.6)
- <a id="oss-r-030"></a>**OSS-R-030** Mỗi **mô hình** (weights) là một dòng riêng trong sổ, ghi: license weights, **có cho thương mại không**, giới hạn khu vực / số người dùng / doanh thu, license của **output**, license của code chạy mô hình.
- <a id="oss-r-031"></a>**OSS-R-031** Chỉ dùng cho **asset phát hành** các mô hình có license cho phép thương mại và không có giới hạn khu vực / người dùng xung đột với kế hoạch phát hành (Việt Nam trước, D-07). Mô hình không đạt (vd. FLUX.1 **dev**, Hunyuan3D bản mở chưa rà license) chỉ được dùng cho **concept nội bộ không đưa vào game**, hoặc không dùng.
- <a id="oss-r-032"></a>**OSS-R-032** Asset do AI tạo **không** chắc được bảo hộ bản quyền; vì vậy hồ sơ nguồn gốc (§3.4) là bắt buộc để chứng minh quy trình và tránh tranh chấp. Không dùng ảnh tham chiếu có bản quyền của bên thứ ba làm đầu vào (img2img / image-to-3D) nếu không có quyền.
- <a id="oss-r-033"></a>**OSS-R-033** Khai báo việc dùng nội dung AI khi nền tảng phát hành yêu cầu (vd. Steam, cửa hàng ứng dụng).

---

## 3. Quy trình

### 3.1. Thêm một thành phần
```
1. Người đề xuất (dev / artist) mở PR thêm dòng vào §4: tên, phiên bản, loại, license, dùng ở đâu, phân phối?, lý do, phương án thay thế
2. Người duyệt (Tech Lead cho mã / dịch vụ kỹ thuật; Producer + Art Director cho asset / mô hình AI) đối chiếu LICENSE chính thức → đổi ⚠️ thành ✅ / ❌, ghi tên + ngày
3. Chỉ sau khi dòng ✅ được merge mới thêm dependency / dùng tạo asset
4. Thành phần quan trọng (Ink runtime, recast, Colyseus) được bọc qua interface nội bộ để thay được (PLAN §5.7.4)
```
- <a id="oss-r-040"></a>**OSS-R-040** Đổi **phiên bản chính** (major) của thành phần ✅ cũng phải duyệt lại dòng đó (license có thể đổi — OSS-R-011).

### 3.2. Gỡ bỏ
- <a id="oss-r-041"></a>**OSS-R-041** Thành phần không còn dùng → chuyển trạng thái `🗄 Ngừng dùng` (giữ dòng để truy vết), xóa khỏi `THIRD_PARTY_NOTICES` ở bản phát hành kế tiếp.

### 3.3. Kiểm tra tự động trong CI
| Phía | Công cụ | Hành vi |
|------|---------|---------|
| pnpm workspace (client, game-server, shared, tools) | `license-checker` (hoặc tương đương) với danh sách cho phép từ §2.1 | Build **fail** nếu gặp license cấm hoặc license chưa có trong danh sách cho phép |
| Gradle (api-service) | Plugin kiểm tra license Gradle | Như trên |
| SBOM | Sinh SBOM chuẩn **CycloneDX** cho mỗi bản phát hành (client + server) | Lưu cùng artifact; dùng để rà lỗ hổng và license |
| Đối chiếu sổ | Script so dependency trực tiếp trong `package.json` / `build.gradle` với bảng §4 | **Fail** nếu dependency trực tiếp không có dòng ✅ |
| Asset | Validator asset `pnpm art:check` (AR-02 §8, AR-05 §6): mọi file trong `art/` phát hành có hồ sơ nguồn gốc (§3.4); mọi tên trong `tools` / `models` của hồ sơ và cột `tools` của tracker `tools/art/asset-tracker.csv` khớp cột "Tên" ở §4.5; cột `license` của tracker tính từ cột "Trạng thái" §4.5 | **Fail** nếu thiếu hồ sơ, tên không có dòng, hoặc asset `RELEASE` dùng dòng chưa ✅ |

- <a id="oss-r-050"></a>**OSS-R-050** Phụ thuộc **gián tiếp** (transitive) được kiểm tra license tự động nhưng không cần dòng riêng trong §4, trừ khi license thuộc nhóm "Có điều kiện" / "Cần duyệt".
- <a id="oss-r-051"></a>**OSS-R-051** `art:check` **đọc trực tiếp** bảng §4.5 (cột "Tên" và "Trạng thái") — không đổi tên cột, không gộp nhiều thành phần vào một dòng. Mọi trạng thái bắt đầu bằng ⚠️ (kể cả "⚠️ chờ duyệt") tính là `PENDING`; ❌ / không có dòng tính là `BLOCKED`; chỉ ✅ là `OK` (AR-05 §4 cột `license`).
- <a id="oss-r-052"></a>**OSS-R-052** Với mô hình AI / công cụ sinh asset, "phụ thuộc" gồm cả **mô hình phụ và thư viện bắt buộc chạy lúc sinh** (vd. mô hình tách nền, bộ mã hóa ảnh, thư viện render / bake trong pipeline TRELLIS.2): mỗi cái có dòng riêng ở §4.5, vì license **hạn chế việc sử dụng** (vd. "chỉ phi thương mại") áp lên cả quy trình tạo asset, dù output không phải tác phẩm phái sinh của thư viện. Thành phần chính chỉ ✅ khi mọi phụ thuộc bắt buộc của **cấu hình pipeline đang dùng** đã ✅ hoặc đã được thay thế (ghi cấu hình ở cột Ghi chú).

### 3.4. Hồ sơ nguồn gốc asset
Mỗi asset phát hành có bản ghi trong `art/provenance/{assetId}.json`:
```jsonc
{
  "assetId": "CHR_PLAYER_BASE_M", "type": "model",
  "source": "AI" ,                                   // AI | CC0 | CC_BY | PURCHASED | IN_HOUSE | SERVICE
  "tools": [ { "name": "TRELLIS.2", "version": "…" }, { "name": "Blender", "version": "…" } ],
  "models": [ { "name": "FLUX.1 schnell", "license": "Apache-2.0" } ],
  "prompts": ["…"], "seeds": [123456],
  "inputs": [ { "file": "concept/chr_player_m_v3.png", "origin": "AI (FLUX.1 schnell)" } ],
  "editedBy": "artist@…", "reviewedBy": "art-director@…", "date": "2026-10-25",
  "attribution": null                                // chuỗi ghi công nếu CC-BY
}
```

| `source` | Nghĩa | Điều kiện |
|----------|-------|-----------|
| `AI` | Phần chính do mô hình AI sinh (concept, ảnh → 3D, texture) | `models` không rỗng; mỗi mô hình có dòng §4.5 |
| `CC0` / `CC_BY` | Lấy từ gói asset có license đó | `tools` chứa tên gói đúng cột "Tên" §4.5; `CC_BY` bắt buộc `attribution` |
| `PURCHASED` | Asset mua có license thương mại | Lưu hóa đơn / file license cạnh asset (§2.3) |
| `IN_HOUSE` | Team tự làm bằng công cụ (Blender…), không lấy nội dung bên ngoài | — |
| `SERVICE` | Nội dung (nhân vật, rig, **hoạt ảnh**) lấy từ **dịch vụ miễn phí có điều khoản riêng** (OSS-R-021: Mixamo, AccuRIG…), kể cả khi team chỉnh sửa sau đó (chốt AR-05 TRK-Q-2) | `tools` chứa tên dịch vụ đúng cột "Tên" §4.5; license của asset = trạng thái dòng dịch vụ đó; ràng buộc phân phối của dịch vụ (vd. không phân phối file thô) ghi ở §4.7 áp cho asset |

- <a id="oss-r-034"></a>**OSS-R-034** Một asset chỉ có **một** `source` theo phần nội dung chính; phần lấy từ nguồn khác ghi trong `inputs[].origin` (vd. model `AI` gắn hoạt ảnh Mixamo: asset model là `AI`, mỗi clip hoạt ảnh là asset riêng `SERVICE`).

### 3.5. Ghi công
- <a id="oss-r-060"></a>**OSS-R-060** Mỗi bản phát hành kèm `THIRD_PARTY_NOTICES` (sinh từ SBOM + sổ đăng ký): tên, phiên bản, license, thông báo bản quyền; client hiển thị trong màn **Thông tin / Credits**. Asset CC-BY có dòng ghi công đúng yêu cầu tác giả.

---

## 4. Sổ đăng ký

Chú thích: **Loại** — TV (thư viện), CC (công cụ), DV (dịch vụ / phần mềm chạy riêng), AS (asset), AI (mô hình AI). **Phân phối?** — có nằm trong bản build / được vận hành cho người chơi không. **Trạng thái** — ✅ được phép · ⚠️ cần xác minh (đối chiếu LICENSE repo / điều khoản) · **⚠️ chờ duyệt** = đã kiểm điều khoản từ nguồn chính thức (link + ngày kiểm ở §4.7), đủ điều kiện duyệt theo §2 và quy trình §3.1 bước 2, **chờ người duyệt ký** — vẫn tính là ⚠️ (OSS-R-001, OSS-R-051) · **⚠️ rủi ro** = đã kiểm, có điểm chặn dùng cho asset phát hành (ghi ở §4.7), chưa đủ điều kiện duyệt · ❌ không dùng. Cột người duyệt / ngày duyệt điền khi đổi trạng thái.

### 4.1. Client & mã dùng chung
| Tên | Loại | License | Dùng ở đâu | Phân phối? | Trạng thái | Ghi chú |
|-----|------|---------|------------|:---:|:---:|---------|
| Babylon.js | TV | Apache-2.0 | Client (render) | Có | ✅ | |
| Babylon.js Editor | CC | Apache-2.0 | Công cụ (map / scene, đang đánh giá Q22) | Không | ✅ | Đã kiểm tra license ở PLAN v0.5 |
| T5C | Tham khảo (mượn từng phần) | MIT (theo index) | Đọc tham khảo; đoạn mã mượn (nếu có) vào client / Game Server | Chỉ phần mượn | ⚠️ | D-23: **không fork**; xác minh LICENSE trước khi chép bất kỳ đoạn nào, ghi công trong `THIRD_PARTY_NOTICES` |
| React | TV | MIT | Client UI | Có | ✅ | |
| Zustand | TV | MIT | Client state | Có | ✅ | |
| i18next | TV | MIT | Client i18n | Có | ✅ | |
| bitECS | TV | MPL-2.0 | Client ECS | Có | ✅ | Có điều kiện: không sửa file thư viện |
| recast-navigation-js | TV | MIT | Client, editor, Game Server | Có | ⚠️ | Bản thân MIT; lõi Recast/Detour C++ dùng **zlib** — xác minh cả hai, ghi vào notices |
| inkjs | TV | MIT | Game Server, client xem trước | Có | ✅ | |
| Ink / Inky | CC | MIT | Công cụ viết kịch bản | Không | ✅ | |
| Howler.js | TV | MIT | Client âm thanh | Có | ✅ | |
| zod | TV | MIT | Kiểm tra payload (TD-02) | Có | ✅ | |
| Colyseus client SDK | TV | MIT | Client | Có | ✅ | |
| `@colyseus/schema` | TV | MIT | Client + Game Server | Có | ✅ | |
| Capacitor | TV | MIT | Bọc app iOS / Android (PLAN §5.8) | Có | ✅ | Plugin bên thứ ba duyệt từng cái |
| mistreevous | TV | MIT (theo PLAN) | Tham khảo AI behavior tree | Có (nếu dùng) | ⚠️ | Hiện chỉ **tham khảo**; nếu import thì xác minh LICENSE |
| TypeScript | CC | Apache-2.0 | Toàn bộ mã TS | Không | ✅ | |
| Vite | CC | MIT | Build client | Không | ✅ | |
| @vitejs/plugin-react | CC | MIT | JSX transform cho Vite client | Không | ✅ | npm metadata checked 2026-10-28; pin 4.4.1 |
| @types/node | CC | MIT | TypeScript Node.js declarations | Không | ✅ | npm metadata checked 2026-10-28; pin 22.14.1 |
| @types/react | CC | MIT | TypeScript React declarations | Không | ✅ | npm metadata checked 2026-10-28; pin 19.0.12 |
| @types/react-dom | CC | MIT | TypeScript React DOM declarations | Không | ✅ | npm metadata checked 2026-10-28; pin 19.0.4 |
| ESLint | CC | MIT | TypeScript lint | Không | ✅ | npm metadata checked 2026-10-28; pin 10.13.0 |
| Prettier | CC | MIT | TypeScript formatter | Không | ✅ | npm metadata checked 2026-10-28; pin 3.9.10 |
| typescript-eslint | CC | MIT | Typed ESLint rules | Không | ✅ | npm metadata checked 2026-10-28; pin 8.72.1 |
| React DOM | TV | MIT | Client UI renderer | Có | ✅ | npm metadata checked 2026-10-28; pin 19.0.0 |

### 4.2. Game Server & API Service
| Tên | Loại | License | Dùng ở đâu | Phân phối? | Trạng thái | Ghi chú |
|-----|------|---------|------------|:---:|:---:|---------|
| Node.js | DV (runtime) | MIT (+ thành phần khác, xem LICENSE) | Game Server | Có (vận hành) | ✅ | |
| Colyseus (server, Redis driver / presence) | TV | MIT | Game Server | Có | ✅ | |
| pino | TV | MIT | Log Game Server | Có | ✅ | |
| prom-client | TV | Apache-2.0 | Metric Game Server | Có | ⚠️ | Xác minh LICENSE repo |
| OpenTelemetry (JS + Java SDK) | TV | Apache-2.0 | Trace (TD-01 §15) | Có | ✅ | |
| OpenJDK (vd. Eclipse Temurin) | DV (runtime) | GPLv2 + Classpath Exception | API Service | Có (vận hành) | ✅ | OSS-R-010 |
| Spring Boot (+ Spring Framework) | TV | Apache-2.0 | API Service | Có | ✅ | |
| Spring Security | TV | Apache-2.0 | Authentication and authorization for API Service (TD-04 API-R-030, TD-08 SEC-R-013) | Có | ✅ | Version managed by Spring Boot 3.4.5 |
| Spring Boot Validation (Hibernate Validator) | TV | Apache-2.0 | Bean validation for API contracts | Có | ✅ | Version managed by Spring Boot 3.4.5 |
| Spring Data JPA (+ Hibernate ORM) | TV | Apache-2.0 | Simple CRUD persistence for API Service (TD-10 CC-R-065) | Có | ✅ | Version managed by Spring Boot 3.4.5 |
| Spring Data Redis (Lettuce) | TV | Apache-2.0 | Valkey-backed API rate limits and revoked-session keys (TD-08 SEC-R-011, SEC-R-018) | Có | ✅ | Version managed by Spring Boot 3.4.5; Valkey uses the Redis-compatible RESP protocol |
| jOOQ (Open Source Edition) | TV | Apache-2.0 khi dùng với DB mã nguồn mở | Hot queries for API Service (TD-10 CC-R-065) | Có | ✅ | jOOQ 3.19.22 managed by Spring Boot 3.4.5; PostgreSQL qualifies for the OSS edition |
| Nimbus JOSE+JWT | TV | Apache-2.0 | Ed25519 JWT signing and verification (TD-04 API-R-030) | Có | ✅ | `nimbus-jose-jwt` 9.37.3; version managed by Spring Boot 3.4.5 and pinned as a direct dependency |
| Bouncy Castle | TV | Bouncy Castle Licence | Argon2id password hashing (TD-08 SEC-R-010) | Có | ✅ | `bcprov-jdk18on` 1.86; Maven Central POM checked 2026-11-03 |
| Tink (Java) | TV | Apache-2.0 | Ed25519 JWT signing dependency for Nimbus JOSE+JWT (TD-04 API-R-030) | Có | ✅ | Version 1.24.0; Maven Central metadata and POM checked 2026-11-03 |
| Flyway (Community) | TV | Apache-2.0 | Migration (TD-03 §11) | Có | ✅ | Community 10.20.1; Apache-2.0 verified from official Maven Central parent POM on 2026-10-28; review again on upgrade |
| Caffeine | TV | Apache-2.0 | Cache API | Có | ✅ | |
| Micrometer | TV | Apache-2.0 | Metric API | Có | ✅ | |
| OpenAPI Generator | CC | Apache-2.0 | Sinh client / stub từ `shared/contracts` | Không (output có) | ✅ | Kiểm tra license template của generator dùng |
| @openapitools/openapi-generator-cli | CC | Apache-2.0 | Wrapper CLI cho OpenAPI Generator | Không | ✅ | npm metadata checked 2026-10-28; pin 2.42.0; generator pin 7.12.0 |
| Prism CLI | CC | Apache-2.0 | Mock server cho OpenAPI contracts | Không | ✅ | npm metadata checked 2026-10-28; pin `@stoplight/prism-cli` 5.16.0 |
| ts-json-schema-generator | CC | MIT | TS → JSON Schema (TD-07) | Không | ⚠️ | Xác minh LICENSE repo |
| jsonschema2pojo | CC | Apache-2.0 | JSON Schema → Java (TD-07, DT-Q-2) | Không (output có) | ⚠️ | Xác minh LICENSE repo |
| Ajv | TV | MIT | Validator gamedata, payload | Có (tools) | ✅ | |

### 4.3. Dữ liệu & hạ tầng
| Tên | Loại | License | Dùng ở đâu | Phân phối? | Trạng thái | Ghi chú |
|-----|------|---------|------------|:---:|:---:|---------|
| PostgreSQL | DV | PostgreSQL License | Nguồn sự thật (TD-03) | Có (vận hành) | ✅ | |
| pg_stat_statements | DV (extension) | PostgreSQL License (contrib) | Theo dõi truy vấn | Có (vận hành) | ✅ | Đi kèm PostgreSQL |
| pg_partman | DV (extension) | PostgreSQL License (theo hiểu biết) | Phân vùng (TD-03 §7) | Có (vận hành) | ⚠️ | Xác minh LICENSE repo |
| PgBouncer | DV | ISC | Pool kết nối | Có (vận hành) | ✅ | |
| **Valkey** | Dịch vụ dữ liệu tạm (cache, Streams, pub/sub, khóa) | BSD-3-Clause | Server (hạ tầng) | Không (chạy nội bộ) | ✅ | **Chốt OSS-Q-1 (2026-10-25): dùng Valkey** thay Redis — tương thích giao thức Redis (RESP), giữ license BSD; tên khóa / lệnh không đổi (TD-03 §12); xác minh Colyseus driver / presence + Streams ở spike Phase 0 |
| Redis (≥ 7.4) | — | RSALv2 / SSPLv1; Redis 8 thêm AGPLv3 | — | — | ❌ không dùng | Thay bằng Valkey (OSS-Q-1); không cần ngoại lệ license |
| Docker / docker-compose | CC | Apache-2.0 (Engine / Compose) | Dev, triển khai | Không | ⚠️ | Docker Desktop có điều khoản thương mại riêng cho doanh nghiệp — xác minh |

### 4.4. Vận hành, kiểm thử, công cụ nội bộ
| Tên | Loại | License | Dùng ở đâu | Phân phối? | Trạng thái | Ghi chú |
|-----|------|---------|------------|:---:|:---:|---------|
| Prometheus | DV | Apache-2.0 | Giám sát | Không | ✅ | |
| Grafana | DV | AGPL-3.0 | Dashboard nội bộ | Không | ✅ | **Chỉ nội bộ** (OSS-R-002); không nhúng / không mở cho người chơi |
| Loki | DV | AGPL-3.0 | Log tập trung | Không | ✅ | Chỉ nội bộ |
| k6 | CC | AGPL-3.0 | Load test | Không | ✅ | Chỉ nội bộ; script k6 của team không phân phối |
| Metabase (OSS) | DV | AGPL-3.0 | Phân tích nội bộ | Không | ✅ | Chỉ nội bộ |
| PostHog (self-host) | DV | MIT cho phần lõi; thư mục `ee` có license riêng | Telemetry (GD-20) | Không | ⚠️ | Xác minh phần dùng không nằm trong `ee` |
| Refine | TV | MIT | GM panel | Không (nội bộ) | ⚠️ | Xác minh LICENSE repo |
| react-admin | TV | MIT (bản community) | GM panel (phương án thay) | Không (nội bộ) | ⚠️ | Bản Enterprise có điều khoản riêng — chỉ dùng community |
| Testcontainers | TV | MIT | Test API | Không | ✅ | |
| JUnit 5 | TV | EPL-2.0 | Test API | Không | ✅ | |
| ArchUnit | TV | Apache-2.0 | Kiểm tra ranh giới module (TD-01, TD-03) | Không | ✅ | |
| Vitest | TV | MIT | Test TS | Không | ✅ | |
| Gradle | CC | Apache-2.0 | Build Java API (wrapper 8.13) | Không | ✅ | |
| Playwright | TV | Apache-2.0 | Test chéo trình duyệt (TD-14) | Không | ✅ | |
| glTF-Transform | CC | MIT | Tối ưu asset | Không | ✅ | |
| Công cụ khảo sát playtest (**chờ chọn**: biểu mẫu tự host hoặc dịch vụ SaaS) | DV | — | Khảo sát người ngoài team (PM-05 §5.2, PT-Q-2) | Không | ⚠️ chờ chọn | Chưa được dùng cho tới khi có tên dịch vụ cụ thể + điều khoản / chính sách dữ liệu đã đọc (OSS-R-022). Buổi nội bộ (`PAPER`, `INTERNAL`) dùng biểu mẫu giấy / file trong repo, không cần dịch vụ |
| Tiled | CC | GPL (editor) | (Không dùng — đã chọn hướng khác, PLAN §5.7.4 có nhắc) | Không | 🗄 | Giữ để truy vết |

### 4.5. Mỹ thuật, mô hình AI, asset
| Tên | Loại | License | Dùng ở đâu | Phân phối? | Trạng thái | Ghi chú |
|-----|------|---------|------------|:---:|:---:|---------|
| TRELLIS.2 (Microsoft) | AI | MIT — code **và** weights `TRELLIS.2-4B` (đã kiểm 2026-10-25, §4.7) | Ảnh → model 3D (PLAN §6.6) | Output có | ⚠️ rủi ro | Bản thân MIT, nhưng **pipeline mặc định** cần phụ thuộc có license phi thương mại: `nvdiffrast`, `nvdiffrec` (NVIDIA, chỉ phi thương mại), `RMBG-2.0` (CC BY-NC 4.0) và `DINOv3` (license riêng) — OSS-R-052. Chỉ ✅ khi có cấu hình pipeline không dùng thành phần ❌ (OSS-Q-5). Phase 0 `SAMPLE` (đánh giá nội bộ, TRK-R-012) được dùng |
| nvdiffrast (NVIDIA) | CC (thư viện chạy lúc sinh) | NVIDIA Source Code License (1-Way Commercial) — §3.3 "chỉ phi thương mại: nghiên cứu / đánh giá" (đã kiểm 2026-10-25) | Phụ thuộc TRELLIS.2: rasterize / bake khi xuất GLB | Không | ❌ cho asset phát hành | Đánh giá Phase 0 (`SAMPLE`) chỉ như một phần của pipeline mặc định ghi dưới tên `TRELLIS.2` — không ghi riêng vào `tools` / `models`. Dùng sản xuất cần license thương mại của NVIDIA hoặc thay thế (OSS-Q-5) |
| nvdiffrec (NVIDIA) | CC (thư viện chạy lúc sinh) | NVIDIA Source Code License for nvdiffrec — §3.3 "chỉ phi thương mại" (đã kiểm 2026-10-25) | Phụ thuộc TRELLIS.2: renderer PBR split-sum | Không | ❌ cho asset phát hành | Như `nvdiffrast` |
| RMBG-2.0 (BRIA) | AI | CC BY-NC 4.0; thương mại cần thỏa thuận riêng với BRIA (nguồn thứ cấp, §4.7) | Tách nền ảnh đầu vào — mô hình mặc định trong cấu hình pipeline `TRELLIS.2-4B` | Không | ❌ | Thay bằng `BiRefNet` (sửa `model_name` trong bản sao cấu hình pipeline) hoặc đưa ảnh đã tách nền (có alpha) vào TRELLIS.2 — xác minh ở spike Phase 0 |
| BiRefNet (`ZhengPeng7/BiRefNet`) | AI | MIT (nguồn thứ cấp; chưa đọc trực tiếp trang mô hình) | Tách nền — thay `RMBG-2.0` (lớp `BiRefNet` của TRELLIS.2 mặc định trỏ mô hình này) | Không | ⚠️ | Đọc LICENSE trên trang mô hình / repo gốc trước khi duyệt |
| DINOv3 (Meta) | AI | DINOv3 License (giấy phép riêng của Meta, mô hình gated) — chưa đọc toàn văn | Mã hóa ảnh điều kiện của TRELLIS.2 (`facebook/dinov3-vitl16-pretrain-lvd1689m`) | Không | ⚠️ | Đọc toàn văn: có cho thương mại, giới hạn sử dụng / khu vực không (OSS-R-030) |
| CuMesh | CC (thư viện chạy lúc sinh) | MIT (LICENSE repo GitHub `JeffreyXiang/CuMesh`, đã kiểm 2026-10-25) | Phụ thuộc TRELLIS.2 (xử lý mesh) | Không | ⚠️ chờ duyệt | Phụ thuộc Python khác (PyTorch, flash-attn, xformers…) rà khi dựng môi trường spike |
| FlexGEMM | CC (thư viện chạy lúc sinh) | MIT (LICENSE repo GitHub `JeffreyXiang/FlexGEMM`, đã kiểm 2026-10-25) | Phụ thuộc TRELLIS.2 (tích chập thưa) | Không | ⚠️ chờ duyệt | |
| O-Voxel | CC (thư viện chạy lúc sinh) | MIT (nằm trong repo TRELLIS.2) | Phụ thuộc TRELLIS.2 (mesh ↔ O-Voxel) | Không | ⚠️ chờ duyệt | |
| SDXL | AI | CreativeML Open RAIL++-M (đã kiểm 2026-10-25, §4.7) | Concept, icon, texture | Output có | ⚠️ chờ duyệt | Cho thương mại; **hạn chế sử dụng** ở Attachment A (phải tuân thủ, không phải giới hạn khu vực / người dùng); licensor không giữ quyền với output. Đủ điều kiện theo OSS-R-031 |
| FLUX.1 schnell | AI | Apache-2.0 (đã kiểm 2026-10-25, §4.7) | Concept, icon, texture | Output có | ⚠️ chờ duyệt | Đủ điều kiện theo OSS-R-031. Bộ mã hóa văn bản đi kèm (T5-XXL, CLIP-L) xác minh license khi dựng workflow ComfyUI |
| FLUX.1 dev | AI | FLUX.1-dev **Non-Commercial License** (đã kiểm 2026-10-25) | — | — | ❌ | Không dùng cho asset phát hành (OSS-R-031); gồm mọi biến thể `[dev]` (Fill, Canny, Depth, Redux, Kontext, Krea) |
| Hunyuan3D (bản mở) | AI | Tencent Hunyuan 3D 2.1 Community License (đã kiểm 2026-10-25) | — | — | ❌ | **Không áp dụng tại EU, Anh, Hàn Quốc**; cấm dùng / hiển thị **output** ngoài lãnh thổ đó; > 1 triệu MAU phải xin license — xung đột game web truy cập toàn cầu (OSS-R-031) |
| ComfyUI | CC | GPL-3.0 | Chạy pipeline vẽ ảnh nội bộ | Không | ✅ | Chỉ công cụ; output không bị ràng buộc GPL (OSS-R-020) |
| Blender | CC | GPL-2.0-or-later | Model, UV, bake, rig, xuất glTF | Không | ✅ | Script Python do team viết chạy trong Blender không phân phối |
| Mixamo (Adobe) | DV | Điều khoản Adobe (miễn phí, cần Adobe ID) — đã kiểm 2026-10-25, §4.7 | Gắn xương, thư viện hoạt ảnh (`source = SERVICE`, §3.4) | Output có | ⚠️ chờ duyệt | Cho dùng nhân vật / hoạt ảnh **miễn phí bản quyền trong game thương mại**; **cấm phân phối file thô** (nhân vật / hoạt ảnh dạng file riêng, gói asset); không dùng huấn luyện mô hình ML; không dùng được với Adobe ID **Enterprise / Federated** (OSS-Q-6). Điều kiện dùng: chỉ phân phối sau retarget, gộp trong GLB `art/animations/{SKELETON}.glb` của game |
| AccuRIG (Reallusion) | CC | Điều khoản Reallusion (miễn phí; xuất cần tài khoản ActorCore) — chỉ có nguồn diễn đàn, chưa đọc EULA chính thức | Gắn xương | Output có | ⚠️ | Theo diễn đàn: rig nhân vật **của mình** dùng thương mại được; nội dung / chuyển động của Reallusion có license riêng. Đọc EULA chính thức trước khi duyệt |
| Kenney assets | AS | CC0 | Prototype, đồ phụ | Có | ✅ | Ghi nguồn theo từng gói |
| Quaternius assets | AS | CC0 | Prototype, đồ phụ | Có | ✅ | Ghi nguồn theo từng gói |
| KayKit assets | AS | CC0 theo trang itch.io từng gói (đã kiểm gói *Adventurers* 2026-10-25) | Prototype, đồ phụ | Có | ⚠️ chờ duyệt (theo gói) | Duyệt **từng gói** (ghi tên gói vào Ghi chú khi duyệt); bản trả phí có thể kèm nội dung khác — kiểm trang gói; tác giả **yêu cầu** (không phải điều khoản CC0) không bán lại bản chưa sửa |

### 4.6. Dự án chỉ tham khảo (không lấy mã)
| Tên | License | Ghi chú |
|-----|---------|---------|
| RPG-JS | MIT | Tham khảo prediction / event hook (D-16: không làm nền) |
| Nakama | Apache-2.0 | Tham khảo thiết kế social / API |
| Reldens | ⚠️ cần xác minh | Tham khảo admin panel RPG |
| L2J / aCis | GPL | **Chỉ đọc, cấm sao chép** (OSS-R-003) |
| Veloren | GPL-3.0 | **Chỉ đọc, cấm sao chép** |
| recast4j, blade-ink, gdx-ai, Netty, protobuf-java | (nhiều loại) | 🗄 Không dùng sau D-19 |

### 4.7. Kết quả kiểm license mô hình AI / dịch vụ asset (2026-10-25)

Cách kiểm: file `LICENSE` tải trực tiếp từ repo GitHub chính thức; trang Hugging Face và Adobe Help **không truy cập trực tiếp được** từ mạng nội bộ ngày kiểm (HTTP 503 / bị chặn) → dùng kết quả tìm kiếm trỏ tới các trang đó, đánh dấu "(thứ cấp)". Người duyệt (§3.1 bước 2) phải mở trực tiếp các link, lưu bản PDF / ảnh chụp điều khoản tại nơi Producer chỉ định (ghi đường dẫn vào cột Ghi chú khi duyệt) rồi mới đổi sang ✅.

| Thành phần | Nguồn chính thức (ngày kiểm 2026-10-25) | Kết quả | Điểm chặn / rủi ro | Khuyến nghị |
|------------|------------------------------------------|---------|--------------------|-------------|
| TRELLIS.2 | [LICENSE](https://github.com/microsoft/TRELLIS.2/blob/main/LICENSE) · [README §License](https://github.com/microsoft/TRELLIS.2#%EF%B8%8F-license) · [model card TRELLIS.2-4B](https://huggingface.co/microsoft/TRELLIS.2-4B) · [texturing_pipeline.json](https://huggingface.co/microsoft/TRELLIS.2-4B/blob/main/texturing_pipeline.json) (thứ cấp) | Code + weights **MIT**. README ghi rõ `nvdiffrast`, `nvdiffrec` "có license riêng"; cấu hình pipeline trên Hugging Face dùng `briaai/RMBG-2.0` (tách nền) và `facebook/dinov3-vitl16-pretrain-lvd1689m` (mã hóa ảnh) | Thư viện NVIDIA **chỉ phi thương mại** (giới hạn *sử dụng*, áp cả khi output không phải tác phẩm phái sinh — OSS-R-052); RMBG-2.0 phi thương mại | **Chưa duyệt.** Phase 0 dùng cho `SAMPLE` (đánh giá). Trước Phase 1: spike thử (a) ảnh vào đã có alpha / BiRefNet thay RMBG-2.0, (b) đường xuất chỉ hình học không qua `nvdiffrast` / `nvdiffrec` (KIT-R-050 vốn bỏ texture PBR, bake lại trong Blender), hoặc (c) mua license thương mại NVIDIA — OSS-Q-5 |
| nvdiffrast / nvdiffrec | [nvdiffrast LICENSE.txt](https://github.com/NVlabs/nvdiffrast/blob/main/LICENSE.txt) · [nvdiffrec LICENSE.txt](https://github.com/NVlabs/nvdiffrec/blob/main/LICENSE.txt) | §3.3 Use Limitation: chỉ "non-commercially" = nghiên cứu / đánh giá, không vì lợi ích tiền tệ trực tiếp hay gián tiếp | Sản xuất asset cho game thương mại = sử dụng thương mại | **Không duyệt** cho asset phát hành |
| RMBG-2.0 | [model card](https://huggingface.co/briaai/RMBG-2.0) · [repo](https://github.com/Bria-AI/RMBG-2.0) (thứ cấp) | CC BY-NC 4.0; thương mại cần thỏa thuận BRIA | Phi thương mại | **Không duyệt**; thay bằng BiRefNet (MIT, thứ cấp) hoặc ảnh có alpha |
| DINOv3 | [model card](https://huggingface.co/facebook/dinov3-vitl16-pretrain-lvd1689m) (thứ cấp) | License riêng của Meta, gated | Chưa đọc toàn văn | Đọc toàn văn; quyết định cùng OSS-Q-5 |
| FLUX.1 schnell | [README bảng mô hình](https://github.com/black-forest-labs/flux#models) · [LICENSE-FLUX1-schnell](https://github.com/black-forest-labs/flux/blob/main/model_licenses/LICENSE-FLUX1-schnell) · [model card](https://huggingface.co/black-forest-labs/FLUX.1-schnell) | **Apache-2.0** cho weights schnell + autoencoder; mọi biến thể `[dev]` là Non-Commercial | Không có giới hạn khu vực / người dùng. Mục "Licensing models for commercial use" của BFL áp cho mô hình `[dev]` | **Đủ điều kiện duyệt** theo OSS-R-031, chờ Producer + Art Director ký |
| SDXL 1.0 | [LICENSE-SDXL1.0](https://github.com/Stability-AI/generative-models/blob/main/model_licenses/LICENSE-SDXL1.0) | CreativeML Open RAIL++-M: cho thương mại; licensor không giữ quyền với output; **hạn chế sử dụng** Attachment A (truyền xuống bản phái sinh) | Hạn chế sử dụng chỉ là danh sách mục đích cấm, không xung đột kế hoạch phát hành | **Đủ điều kiện duyệt**, chờ ký; LoRA phong cách do team huấn luyện kế thừa cùng điều khoản |
| Hunyuan3D 2.1 | [LICENSE](https://github.com/Tencent-Hunyuan/Hunyuan3D-2.1/blob/main/LICENSE) | Lãnh thổ loại trừ EU / Anh / Hàn Quốc; cấm dùng / hiển thị output ngoài lãnh thổ; > 1 triệu MAU phải xin phép | Game web truy cập từ EU | **Giữ ❌** |
| Mixamo | [Mixamo FAQ (Adobe Help)](https://helpx.adobe.com/creative-cloud/faq/mixamo-faq.html) (thứ cấp) · [Mixamo FAQ: Licensing, Royalties, Ownership, EULA and TOS (Adobe Community)](https://community.adobe.com/questions-696/mixamo-faq-licensing-royalties-ownership-eula-and-tos-589400) (thứ cấp) | Nhân vật + hoạt ảnh dùng **royalty-free** cho dự án cá nhân, thương mại, phi lợi nhuận, kể cả **video game**; miễn phí với Adobe ID, **không** cho Enterprise / Federated ID | **Cấm phân phối file nhân vật / hoạt ảnh thô** (kể cả gói asset cho engine); không dùng huấn luyện ML. Game web tải GLB về trình duyệt: hoạt ảnh phải đã retarget và **gộp trong GLB của game**, không có đường tải riêng file clip | **Đủ điều kiện duyệt có điều kiện** (ghi ở cột Ghi chú §4.5), chờ ký; người duyệt đọc trực tiếp Adobe General Terms + FAQ hiện hành |
| AccuRIG | [Reallusion Discussions: AccuRIG 2 EULA](https://discussions.reallusion.com/t/question-about-accurig-2s-eula/15034) (diễn đàn, không phải điều khoản) | Nhân viên Reallusion trả lời: dùng thương mại được với nhân vật của mình; nội dung / chuyển động Reallusion có license riêng | Chưa có EULA chính thức | **Chưa duyệt**; đọc EULA chính thức |
| KayKit | [KayKit Adventurers (itch.io)](https://kaylousberg.itch.io/kaykit-adventurers) | CC0 1.0 trên trang gói | License theo **từng gói** | **Đủ điều kiện duyệt theo từng gói**, chờ ký |

---

## 5. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="oss-ac-001"></a>OSS-AC-001 | PR thêm dependency AGPL vào `game-server/package.json` | CI | Fail (OSS-R-002) |
| OSS-AC-002 | PR thêm dependency MIT chưa có dòng ✅ trong §4 | CI | Fail (chưa đăng ký) |
| OSS-AC-003 | Dependency gián tiếp mới có license MPL-2.0 | CI | Cảnh báo, yêu cầu dòng trong §4 (OSS-R-050) |
| OSS-AC-004 | Nâng Valkey lên một phiên bản lớn mới | Review | Bắt buộc kiểm tra lại license của phiên bản đó (OSS-R-011) |
| OSS-AC-005 | Model nhân vật trong `art/` không có `art/provenance/{assetId}.json` | Validator asset | Fail (OSS-R-005) |
| OSS-AC-006 | Hồ sơ nguồn gốc ghi mô hình FLUX.1 dev | Validator asset | Fail (OSS-R-031) |
| OSS-AC-007 | Asset CC-BY | Build phát hành | Có dòng ghi công trong Credits + `THIRD_PARTY_NOTICES` |
| OSS-AC-008 | Build phát hành | CI | Có SBOM CycloneDX cho client và server |
| OSS-AC-009 | Grafana dashboard | Kiểm tra triển khai | Chỉ truy cập trong mạng nội bộ / VPN |
| <a id="oss-ac-010"></a>OSS-AC-010 | Hồ sơ nguồn gốc `source = SERVICE`, `tools` không có tên dịch vụ nào ở §4.5 | `art:check` | Fail (OSS-R-034) |
| <a id="oss-ac-011"></a>OSS-AC-011 | Hồ sơ nguồn gốc có `models` chứa `RMBG-2.0` (kể cả asset `SAMPLE`) | `art:check` | Fail, `license = BLOCKED` (OSS-R-052; như AR-05 TRK-AC-009) |
| <a id="oss-ac-012"></a>OSS-AC-012 | Dòng §4.5 trạng thái "⚠️ chờ duyệt" | `art:check` tính `license` | `PENDING` (OSS-R-051) |

---

## 6. Câu hỏi mở
- [x] **OSS-Q-1:** Redis hay **Valkey**? Đề xuất Valkey (BSD-3) để tránh RSAL / SSPL / AGPL; cần spike xác nhận Colyseus Redis driver / presence và Streams chạy tốt trên Valkey. Ảnh hưởng TD-01, TD-03 §12, PLAN §5.3. *(Đã chốt 2026-10-25: **Valkey**.)*
- [ ] **OSS-Q-2:** License T5C (MIT theo index) — xác minh trước khi chép bất kỳ đoạn mã nào (D-23: không fork).
- [ ] **OSS-Q-3:** Có tự host mô hình AI (GPU ~16GB) hay dùng dịch vụ cloud? Dịch vụ cloud thêm điều khoản cần rà (quyền với output, lưu dữ liệu đầu vào).
- [ ] **OSS-Q-4:** Ai là người duyệt license chính thức (Tech Lead kiêm, hay thuê tư vấn pháp lý trước Open Beta)?
- [x] **OSS-Q-5:** *(Đã chốt 2026-10-25, D-28: tài liệu **không chốt công cụ sinh asset**; team tự chọn nguồn (kể cả bản dùng thử TRELLIS.2) và bổ sung asset theo danh sách yêu cầu AR-02 / AR-03 / AR-04 / AR-06, theo dõi ở AR-05. Bảng §4.5 / §4.7 giữ làm tham khảo. Ràng buộc còn hiệu lực: asset vào **bản phát hành** phải có hồ sơ nguồn gốc §3.4 và nguồn cho phép thương mại — điều khoản bản dùng thử / phi thương mại không đủ.)* TRELLIS.2 cho asset phát hành: pipeline mặc định cần `nvdiffrast` / `nvdiffrec` (NVIDIA, chỉ phi thương mại) và `RMBG-2.0` (CC BY-NC). Chọn: (a) cấu hình không dùng chúng (ảnh có alpha / BiRefNet; xuất chỉ hình học rồi bake trong Blender — KIT-R-050), (b) mua license thương mại NVIDIA / BRIA, hay (c) mô hình ảnh → 3D khác? Chốt bằng spike Phase 0 + người duyệt (OSS-Q-4). *Mặc định: TRELLIS.2 chỉ cho `SAMPLE` (AR-05 TRK-R-012); asset `RELEASE` hữu cơ làm bằng Blender / gói CC0 tới khi chốt.* Ảnh hưởng AR-05 TRK-Q-8, AR-06 §12.
- [ ] **OSS-Q-6:** Mixamo không dùng được với Adobe ID **Enterprise / Federated**: tài khoản nào (pháp nhân / cá nhân) đăng nhập Mixamo cho dự án, và ghi gì vào hồ sơ nguồn gốc? *Mặc định: một Adobe ID riêng của dự án do Producer giữ; hồ sơ ghi tên dịch vụ, không ghi email (TD-08 SEC-R-075).*

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến, chính sách license (mã, công cụ / dịch vụ, asset, mô hình AI), quy trình thêm / gỡ, kiểm tra CI + SBOM, hồ sơ nguồn gốc asset AI, ghi công, sổ đăng ký ~70 thành phần (nhiều dòng ⚠️ chờ xác minh), nghiệm thu, câu hỏi mở (Redis vs Valkey, T5C) |
| v0.2 | 2026-10-25 | — | Dòng T5C: không fork, chỉ tham khảo / mượn có ghi công (D-23) |
| v0.3 | 2026-10-25 | — | D-24: Valkey thay Redis (tên khóa / lệnh không đổi) |
| v0.4 | 2026-10-25 | — | Kiểm license mô hình AI / dịch vụ asset từ nguồn chính thức (§4.7, link + ngày kiểm): TRELLIS.2 MIT nhưng phụ thuộc NVIDIA / RMBG-2.0 phi thương mại → ⚠️ rủi ro; thêm dòng `nvdiffrast`, `nvdiffrec`, `RMBG-2.0` (❌), `BiRefNet`, `DINOv3`, CuMesh / FlexGEMM / O-Voxel; FLUX.1 schnell, SDXL, Mixamo, KayKit → "⚠️ chờ duyệt"; Hunyuan3D giữ ❌ (lãnh thổ). Trạng thái "⚠️ chờ duyệt" / "⚠️ rủi ro"; OSS-R-022 (SaaS thu dữ liệu người ngoài), OSS-R-051 (`art:check` đọc §4.5), OSS-R-052 (phụ thuộc lúc sinh của mô hình); `source = SERVICE` + bảng nghĩa `source`, OSS-R-034 (chốt AR-05 TRK-Q-2); dòng công cụ khảo sát playtest "chờ chọn" (PM-05 PT-Q-2); OSS-Q-5, OSS-Q-6 |
| v0.5 | 2026-10-25 | — | Chốt OSS-Q-5 (D-28): không chốt công cụ sinh asset; team tự bổ sung asset theo danh sách yêu cầu |
