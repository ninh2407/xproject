# AR-05 — Asset Tracker

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | AR-05 · Prefix ID: `TRK` |
| Owner | Art Director (trạng thái, duyệt) + Tech Artist (schema, CI) |
| Trạng thái | 🟡 Bản nháp v0.4 |
| Ngày | 2026-10-25 |
| Cần trước | Phase 0 (theo dõi asset mẫu + spike pipeline AI); bắt buộc trước sản xuất hàng loạt Phase 1 |
| Quyết định nền | D-22 (mỹ thuật ưu tiên AI), PLAN §6.6, §6.7, §7 Phase 0, §8.4 |
| Liên quan | [AR-01 Art Bible](art-bible.md) §9 (duyệt), §10 (mẫu Phase 0) · [AR-02 Asset Guidelines](asset-guidelines.md) §3 (đặt tên), §6 (ngân sách), §8 (quy trình xuất, phạm vi `art:check`), §9 (checklist) · [AR-03 Animation List](animation-list.md) (`tools/art/anim-list.json`) · [AR-06 Environment Kits](environment-kits.md) · [TD-11 Performance](../tech/10-performance.md) §3 (**nguồn số ngân sách**) · [TD-15 OSS Register](../tech/14-oss-register.md) §2.3–§2.4, §3.4, §4.5 · [TD-08 Bảo mật](../tech/07-security.md) SEC-R-075 · [PM-03 Risk Register](../pm/risks.md) R4, R10, R20 · [PLAN §7](../../PLAN.md) |

> Tài liệu này mô tả **schema, máy trạng thái và quy trình** của danh sách asset. **Đề xuất (chưa duyệt):** nguồn sự thật là file máy đọc được `tools/art/asset-tracker.csv`, được `pnpm art:check` kiểm tra trong CI; bảng ở §7 chỉ là **ảnh chụp mẫu** để thống nhất cách ghi, không phải dữ liệu sống. Khi file CSV có, AI và người **chỉ sửa file CSV**; markdown này chỉ đổi khi đổi schema / quy trình (tăng phiên bản).

**Tài liệu này cung cấp cho:**

| Tài liệu / công cụ gọi | Cần gì từ AR-05 | Mục |
|------------------------|-----------------|-----|
| `pnpm art:check` (AR-02 §8 bước 5) | Schema cột, chuyển trạng thái hợp lệ, điều kiện cổng | §3, §4, §6 |
| Art Director / Producer | Tiến độ theo phase, asset bị chặn | §8 |
| AI cập nhật trạng thái tự động | Định dạng file, quy tắc sửa | §2, §5 |
| PM-02 Backlog | Liên kết task ↔ `assetId` | §4 cột `links` |

---

> **D-28:** tracker là danh sách asset **cần có**; team tự chọn nguồn (cột `source`) và bổ sung asset. Công cụ AI trong tài liệu chỉ là gợi ý.

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="trk-r-001"></a>**TRK-R-001** | Mỗi asset đi vào `art/` (kể cả mẫu Phase 0) có **đúng một dòng** trong tracker, khóa là `assetId` theo AR-02 §3 (AST-R-010). Không có dòng → không được commit file vào `art/`. |
| <a id="trk-r-002"></a>**TRK-R-002** | Tracker **không chứa con số ngân sách**; chỉ ghi `budgetClass` (§4). Giới hạn tam giác / texture / dung lượng đọc từ TD-11 §3 lúc chạy `art:check` (AST-R-003, AST-R-042). |
| <a id="trk-r-003"></a>**TRK-R-003** | Tracker **không thay** hồ sơ nguồn gốc. Hồ sơ chuẩn là `art/provenance/{assetId}.json` (TD-15 §3.4, AST-R-005); tracker chỉ trỏ tới và tóm tắt trạng thái license. |
| <a id="trk-r-004"></a>**TRK-R-004** | Người phụ trách / người duyệt ghi bằng **vai trò hoặc handle nội bộ**, không ghi email, số điện thoại (TD-08 SEC-R-075). |
| <a id="trk-r-005"></a>**TRK-R-005** | Không xóa dòng. Asset bị bỏ → trạng thái `DROPPED` kèm lý do; `assetId` không dùng lại (giống quy ước không đánh số lại của docs/README §2.1). |

---

## 2. Nguồn sự thật & định dạng (đề xuất)

| Mục | Đề xuất | Lý do |
|-----|---------|-------|
| File | `tools/art/asset-tracker.csv` (UTF-8, phân cách `,`, có dòng tiêu đề đúng thứ tự §4) | Diff theo dòng dễ review; AI sửa được bằng thao tác văn bản; mở được bằng Google Sheets (PLAN §8.4 "hoặc Sheet") |
| Trường nhiều giá trị | Ngăn bằng `;` trong cùng ô, không khoảng trắng thừa (vd. `TRELLIS.2;Blender;Mixamo`) | Không cần CSV lồng |
| Sắp xếp | Theo `phase`, rồi `assetId` tăng dần | Giảm xung đột merge |
| Schema | `tools/art/asset-tracker.schema.json` (JSON Schema cho từng dòng) — sinh từ §3–§4 tài liệu này | Một nguồn kiểm tra cho CI và cho AI |
| Kiểm tra | `pnpm art:check` đọc CSV + `art/` + `art/provenance/` + bảng TD-15 §4.5 + gamedata (`modelId`, `iconId`) | §6 |
| Dashboard | `art:check --report` ghi đè khối giữa hai marker trong §8 (hoặc một file sinh riêng) | §8 |

Phương án thay thế: `tools/art/asset-tracker.yaml` (đọc dễ hơn với trường nhiều giá trị, diff kém hơn khi sửa hàng loạt) — xem TRK-Q-1.

---

## 3. Máy trạng thái

### 3.1. Trạng thái

```ts
// tools/art (đề xuất) — chỉ dùng trong công cụ mỹ thuật, không vào shared/contracts
type AssetState =
  | 'PROPOSED'         // đề xuất: có dòng, có mô tả + budgetClass + owner; chưa sinh gì
  | 'AI_GEN'           // đang sinh AI: concept (ComfyUI) → art director chọn → ảnh → model (TRELLIS.2) / ảnh 2D
  | 'BLENDER_CLEANUP'  // làm sạch Blender: decimate, UV / atlas, pivot, scale, LOD, extras (AR-02 §8 bước 1–3)
  | 'RIG_ANIM'         // gắn bộ xương chung / SkeletonGroup + hoạt ảnh, marker hit (AR-02 §4–§5)
  | 'ART_CHECK'        // đã art:optimize, đang chạy / sửa cho art:check xanh (AR-02 §8 bước 4–5)
  | 'ART_REVIEW'       // duyệt mỹ thuật: ảnh đúng camera game, so AR-01 §1/§4/§5, ký reviewedBy (AR-01 §9 bước 3–4)
  | 'IN_GAME'          // trong game: đã merge vào main, được tham chiếu (gamedata / bản đồ / cảnh mẫu)
  | 'REJECTED'         // bị trả lại: kèm returnTo + rejectReason
  | 'DROPPED';         // bỏ khỏi phạm vi (TRK-R-005)
```

```
PROPOSED ─► AI_GEN ─► BLENDER_CLEANUP ─► RIG_ANIM ─► ART_CHECK ─► ART_REVIEW ─► IN_GAME
   │          │              │               │            │             │           │
   │          └──────────────┴───────────────┴────────────┴─────────────┴───────────┴─► REJECTED ─(returnTo)─► bước đã ghi
   └──────────────────────────────────────── (bất kỳ trạng thái nào) ──────────────────► DROPPED
```

### 3.2. Đối chiếu quy trình duyệt

| Trạng thái | AR-01 §9 | AR-02 §8 / §9 | Điều kiện **ra khỏi** trạng thái (cổng) |
|------------|----------|---------------|------------------------------------------|
| `PROPOSED` | — | §9.1 (`assetId` đúng mẫu, chưa dùng) | `assetId` hợp lệ, `kind`, `budgetClass`, `source`, `owner`, `phase`, `priority` đủ |
| `AI_GEN` | Bước 1 Concept → art director chọn; bước 2 phần TRELLIS.2 | — | Có concept được chọn (link trong `links`); mọi mô hình trong `tools` có dòng TD-15 §4.5 (✅, hoặc ⚠️ nếu `releaseIntent = SAMPLE`, TRK-R-012) |
| `BLENDER_CLEANUP` | Bước 2 (→ Blender theo AR-02) | §8 bước 1–3; §9.2, §9.5, §9.6 | Đơn vị / pivot / transform đúng AR-02 §2; LOD nếu nhân vật / quái; `collisionTemplate` nếu prop |
| `RIG_ANIM` | — | §8 bước 1 (tên xương, marker); §9.4 | Đúng bộ xương (AST-R-004, AST-R-020); clip tấn công có `hit` (AST-R-031) |
| `ART_CHECK` | Bước 2 (kiểm ngân sách CI) | §8 bước 4–5; §9.3, §9.7 | `pnpm art:optimize` + `pnpm art:check` xanh; có `art/provenance/{assetId}.json` (AST-AC-005) |
| `ART_REVIEW` | Bước 3 (chụp đúng camera) + bước 4 (ký duyệt) | §8 bước 6; §9.8, §9.9 | `reviewedBy` có trong hồ sơ nguồn gốc; với `RELEASE`: mọi dòng TD-15 liên quan là ✅ |
| `IN_GAME` | "mới được merge" | — | (trạng thái cuối) |

### 3.3. Bước áp dụng theo loại và nguồn
Bước không áp dụng được **bỏ qua** (chuyển thẳng sang bước kế tiếp áp dụng được); CI coi bước bỏ qua là hợp lệ theo bảng dưới.

| `kind` (tiền tố AR-02 §3) | `AI_GEN` | `BLENDER_CLEANUP` | `RIG_ANIM` |
|---------------------------|:---:|:---:|:---:|
| `CHR`, `MDL_MON` | ✅ nếu `source = AI` | ✅ | ✅ |
| `OUT`, `COS` (mesh trang phục) | ✅ nếu `source = AI` | ✅ | ✅ (skin vào bộ xương chung) |
| `WPN`, `PRP` | ✅ nếu `source = AI` | ✅ | — |
| `KIT` (gói nhiều prop) | — (theo từng `PRP`) | ✅ (atlas chung, snap lưới) | — |
| `ANM` | — | — | ✅ (bắt buộc) |
| `VFX` | ✅ nếu texture / atlas AI | ✅ nếu có mesh | — |
| `ICO`, `UI`, `POR`, `ILL`, `WMAP` (asset 2D) | ✅ nếu `source = AI` | — | — |
| `ENV`, `TER` (texture môi trường / địa hình) | ✅ nếu `source = AI` | ✅ (bake / lượng tử về `MaterialGroup`, AR-06 §6.1) | — |

- <a id="trk-r-010"></a>**TRK-R-010** Chuyển trạng thái chỉ **tiến một bước áp dụng được** mỗi lần cập nhật (không nhảy `PROPOSED → IN_GAME`), trừ `REJECTED` và `DROPPED` vào được từ mọi trạng thái.
- <a id="trk-r-011"></a>**TRK-R-011** `REJECTED` bắt buộc có `returnTo` (một trong các trạng thái từ `AI_GEN` tới `ART_CHECK`) và `rejectReason` (một câu, trích mã quy tắc nếu có, vd. `ART-R-050`, `AST-R-041`, "silhouette lẫn nền"). Cập nhật kế tiếp phải chuyển sang đúng `returnTo`. Ai trả lại: CI (ở `ART_CHECK`) hoặc Art Director (ở `ART_REVIEW`).
- <a id="trk-r-012"></a>**TRK-R-012** `releaseIntent = SAMPLE` (mẫu phong cách Phase 0, AR-01 §10) được dùng mô hình ⚠️ trong TD-15 vì là nội bộ (OSS-R-031), nhưng **không** được tham chiếu từ gamedata phát hành; muốn dùng thật phải tạo dòng `RELEASE` mới (có thể cùng nguồn) và đi lại từ `ART_CHECK` với license ✅.
- <a id="trk-r-013"></a>**TRK-R-013** Đổi file của asset đã `IN_GAME` (sửa lỗi, tối ưu) → chuyển về `ART_CHECK` (sửa kỹ thuật) hoặc `ART_REVIEW` (đổi hình ảnh); `assetId` giữ nguyên (AST-R-010).

---

## 4. Cột bắt buộc (schema một dòng)

Thứ tự cột trong CSV đúng như bảng. Cột **(CI)** do `art:check` điền / ghi đè, người không sửa tay.

| # | Cột | Kiểu / giá trị | Bắt buộc | Ghi chú |
|:-:|-----|----------------|:---:|---------|
| 1 | `assetId` | Chuỗi theo mẫu AR-02 §3, `^[A-Z0-9_]{1,48}$` | ✅ | Khóa, duy nhất (AST-R-010) |
| 2 | `kind` | Cột `kind` của AR-02 §3 (danh sách đóng, AST-R-011): `CHR` · `OUT` · `COS` · `WPN` · `MDL_MON` · `KIT` · `PRP` · `ENV` · `TER` · `VFX` · `ICO` · `UI` · `POR` · `ILL` · `WMAP` · `ANM` | ✅ | Phải khớp tiền tố của `assetId` (`VFXO_*` có `kind = VFX`) |
| 3 | `title` | Tên mô tả tiếng Việt ngắn | ✅ | vd. "Trúc Thử" |
| 4 | `region` | Mã vực: `V1` … (GD-21 §3.1); `—` nếu dùng chung | ✅ | |
| 5 | `kit` | `KIT_*` mà prop thuộc về; `—` nếu không | khi `kind = PRP` | AR-06 khi có |
| 6 | `budgetClass` | Cột `budgetClass` của AR-02 §6 (AST-R-042): `CHARACTER` · `OUTFIT` · `MONSTER_NORMAL` · `MONSTER_ELITE` · `BOSS` · `PROP` · `PROP_LARGE` · `VFX_SKILL` · `ICON` · `PORTRAIT` · `MAP` · `—` | ✅ (`—` cho `ANM`, `ENV`, `TER`, `UI`, `ILL`, `WMAP` — AST-Q-4) | Đã có ở AR-02 v0.3 (chốt TRK-Q-5); số đọc từ TD-11 §3 |
| 7 | `measured` **(CI)** | `tris=…;tex=…;kb=…;bones=…` | ở `ART_CHECK`+ | Số đo thật của LOD0 sau `art:optimize` |
| 8 | `source` | Đúng enum `source` của hồ sơ nguồn gốc TD-15 §3.4: `AI` · `CC0` · `CC_BY` · `PURCHASED` · `IN_HOUSE` · `SERVICE` | ✅ | "Tay" = `IN_HOUSE`; "mua" = `PURCHASED`; dịch vụ miễn phí có điều khoản riêng (Mixamo, AccuRIG) = `SERVICE` (TD-15 OSS-R-034, chốt TRK-Q-2) |
| 9 | `tools` | Tên mô hình AI / dịch vụ / gói, đúng tên cột "Tên" ở TD-15 §4.5, ngăn `;` | ✅ khi `source ≠ IN_HOUSE` thuần tay | vd. `FLUX.1 schnell;TRELLIS.2;Blender;Mixamo` |
| 10 | `license` **(CI)** | `OK` (mọi dòng TD-15 liên quan ✅) · `PENDING` (có ⚠️, kể cả "⚠️ chờ duyệt" / "⚠️ rủi ro") · `BLOCKED` (có ❌ / không có dòng) | — | Tính từ `tools` + TD-15 §4.5 theo TD-15 OSS-R-051; asset `CC_BY` thêm kiểm tra `attribution` |
| 11 | `provenance` | Đường dẫn `art/provenance/{assetId}.json` | ở `ART_CHECK`+ | TRK-R-003 |
| 12 | `owner` | Vai trò / handle (TRK-R-004) | ✅ | vd. `3D Artist`, `Tech Artist` |
| 13 | `reviewer` | Vai trò / handle người duyệt | ở `ART_REVIEW`+ | Khớp `reviewedBy` của hồ sơ |
| 14 | `phase` | `PH0` · `PH1` · `PH2` · `PH3` · `ALPHA` · `BETA1` · `BETA2` · `OB` (PLAN §7: Phase 0–3, 4, 5a, 5b, 6) | ✅ | Phase cần có asset |
| 15 | `priority` | `P0` chặn tiêu chí hoàn thành phase · `P1` cần trong phase · `P2` có thì tốt | ✅ | |
| 16 | `state` | `AssetState` §3.1 | ✅ | |
| 17 | `returnTo` | `AssetState` | khi `REJECTED` | TRK-R-011 |
| 18 | `rejectReason` | Một câu | khi `REJECTED` / `DROPPED` | |
| 19 | `releaseIntent` | `RELEASE` · `SAMPLE` | ✅ | TRK-R-012 |
| 20 | `effortHours` | Số giờ công cộng dồn (số thực 0,5) | — | Phase 0 đo giờ công pipeline AI (PLAN §7 Phase 0) |
| 21 | `links` | `concept=…;pr=…;preview=…;task=…` | — | Ảnh concept, PR, ảnh xem trước đúng camera, task PM-02 |
| 22 | `updated` | `YYYY-MM-DD` | ✅ | Ngày đổi trạng thái gần nhất |

---

## 5. Quy trình cập nhật

1. **Thêm asset:** người đề xuất (artist / designer / AI theo yêu cầu) thêm dòng `PROPOSED` trong cùng PR với task; Art Director xác nhận `priority` và `owner`.
2. **Đổi trạng thái:** người phụ trách sửa `state` + `updated` trong **cùng PR** với file tạo ra ở bước đó (file `.blend` trong `art-source/`, GLB trong `art/`, hồ sơ nguồn gốc). Không có PR riêng chỉ để đổi trạng thái, trừ `REJECTED` / `DROPPED`.
3. **AI cập nhật tự động** (được phép): điền cột (CI); chuyển `ART_CHECK → REJECTED` (`returnTo` = bước gây lỗi, `rejectReason` = thông điệp `art:check`); đề xuất chuyển `ART_CHECK → ART_REVIEW` khi xanh. **Không được:** chuyển sang `IN_GAME`, điền `reviewer`, đổi `priority` / `phase` — các việc đó cần người (AR-01 §9 "AI không thay được bước duyệt", PLAN §6.6).
4. **Duyệt:** Art Director xem ảnh xem trước (AR-02 §8 bước 6) → đạt: ghi `reviewedBy` vào hồ sơ + `reviewer` + `ART_REVIEW → IN_GAME` khi merge; không đạt: `REJECTED` + `returnTo`.
5. **Rà soát:** mỗi sprint (PLAN §9) Art Director xem §8; asset `P0` đứng yên một trạng thái > 1 sprint được nêu trong demo / retro và cân nhắc ghi PM-03 R4.

---

## 6. Kiểm tra trong CI (`art:check`, đề xuất mở rộng AR-02 §8 bước 5)

| Kiểm tra | Kết quả khi sai |
|----------|-----------------|
| CSV khớp schema §4 (cột, enum, mẫu `assetId`, `kind` khớp tiền tố) | Fail |
| `assetId` trùng | Fail |
| File trong `art/` không có dòng tracker (TRK-R-001) | Fail |
| Dòng `IN_GAME` mà không có file trong `art/` | Fail |
| Chuyển trạng thái so với nhánh `main` vi phạm TRK-R-010 / TRK-R-011 | Fail |
| `state ≥ ART_CHECK` mà thiếu hồ sơ nguồn gốc | Fail (AST-AC-005, OSS-AC-005) |
| `RELEASE` ở `ART_REVIEW` / `IN_GAME` có `license ≠ OK` | Fail (OSS-R-031, OSS-AC-006) |
| Gamedata (`modelId`, `iconId`) trỏ tới asset `SAMPLE` hoặc chưa `IN_GAME` | Fail (TRK-R-012) |
| Gamedata trỏ tới `assetId` không có dòng tracker | Fail |
| `owner` / `reviewer` chứa ký tự `@` | Fail (TRK-R-004) |
| `measured` vượt ngân sách `budgetClass` theo TD-11 | Fail (AST-R-003) — `art:check` tự đặt `REJECTED` khi chạy cục bộ với cờ ghi |

---

## 7. Bảng mẫu — asset P0 của Phase 0

Lấy từ AR-01 §10 (mẫu #1, #2, #3, #5, #7) và spike pipeline AI ở PLAN §7 Phase 0 ("1 nhân vật + 1 quái + 5 prop"). Mẫu #4 (ánh sáng ngày / đêm / thời tiết) và #6 (shader thủy mặc) là cấu hình TD-05 / gamedata, **không** là asset — không có dòng. `assetId` dưới đây **đề xuất** theo mẫu AR-02 §3, chưa có trong gamedata. Trạng thái là giả định ngày 2026-10-25 (đầu Phase 0).

| assetId | kind | title | region / kit | budgetClass | source / tools | license | owner | phase | prio | state | intent | AR-01 §10 |
|---------|------|-------|--------------|-------------|----------------|:---:|-------|:---:|:---:|-------|:---:|:---:|
| `CHR_PLAYER_SAMPLE_A` | CHR | Nhân vật kiếm, tỷ lệ A (1/5) | V1 / — | CHARACTER | AI / `FLUX.1 schnell;TRELLIS.2;Blender;Mixamo` | PENDING | 3D Artist | PH0 | P0 | AI_GEN | SAMPLE | #1 |
| `CHR_PLAYER_SAMPLE_B` | CHR | Nhân vật kiếm, tỷ lệ B (1/6–1/6,5) | V1 / — | CHARACTER | AI / `FLUX.1 schnell;TRELLIS.2;Blender;Mixamo` | PENDING | 3D Artist | PH0 | P0 | AI_GEN | SAMPLE | #1 |
| `OUT_THANHVAN_BODY_01` | OUT | Trang phục Thanh Vân Tông | V1 / — | OUTFIT | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH0 | P0 | PROPOSED | SAMPLE | #1 |
| `OUT_TACTHIEN_BODY_01` | OUT | Trang phục Tạc Thiên Bang | V1 / — | OUTFIT | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH0 | P0 | PROPOSED | SAMPLE | #1 |
| `OUT_HOPHOAN_BODY_01` | OUT | Trang phục Hợp Hoan Tông | V1 / — | OUTFIT | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH0 | P0 | PROPOSED | SAMPLE | #1 |
| `WPN_SWORD_LK1` | WPN | Kiếm khởi đầu | — / — | PROP | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH0 | P0 | PROPOSED | SAMPLE | #1 |
| `ANM_HUMANOID_SWORD_ATTACK_1` | ANM | Kiếm — đánh thường nhát 1 (có `hit`) | — / — | — | SERVICE / `Mixamo;Blender` | PENDING | Tech Artist | PH0 | P0 | PROPOSED | SAMPLE | #1 |
| `MDL_MON_BAMBOO_RAT` | MDL_MON | Trúc Thử (`MON_BAMBOO_RAT`, QUADRUPED) | V1 / — | MONSTER_NORMAL | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH0 | P0 | BLENDER_CLEANUP | SAMPLE | #2 |
| `MDL_MON_FOX_MATRIARCH` | MDL_MON | Hồ Yêu Mẫu (`MON_B_FOX_MATRIARCH`, boss) | V1 / — | BOSS | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH0 | P0 | PROPOSED | SAMPLE | #2 |
| `KIT_V1_BAMBOO` | KIT | Kit Thanh Trúc Lâm (`V1_F01`), atlas gradient | V1 / — | MAP | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH0 | P0 | AI_GEN | SAMPLE | #3 |
| `PRP_BAMBOO_STALK_01` | PRP | Cụm trúc (`CIRCLE`) | V1 / `KIT_V1_BAMBOO` | PROP | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH0 | P0 | ART_CHECK | SAMPLE | #3 |
| `PRP_ROCK_BOULDER_L_01` | PRP | Đá tảng lớn phủ rêu (`POLYGON`) | V1 / `KIT_V1_ROCK` | PROP | CC0 / `Quaternius assets;Blender` | OK | 3D Artist | PH0 | P0 | REJECTED → `BLENDER_CLEANUP` | SAMPLE | #3 |
| `PRP_RUINS_SHRINE_ABANDONED` | PRP | Miếu hoang Thanh Trúc Lâm (`ACCENT`, LR-01 §3.4) | V1 / `KIT_V1_RUINS` | PROP_LARGE | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH0 | P0 | PROPOSED | SAMPLE | #3 |
| `VFX_BOLT` | VFX | Mẫu đạn bay — Hỏa Cầu Thuật (`SKL_FIRE_ORB`) với `VPAL_HOA` (AR-04 §6.2) | — / — | VFX_SKILL | IN_HOUSE / `Blender` | OK | Tech Artist | PH0 | P0 | PROPOSED | SAMPLE | #5 |
| `VFX_TG_ENEMY` | VFX | Vùng cảnh báo kẻ địch + 4 bảng `TGPAL_*` (AR-04 §5) | — / — | VFX_SKILL | IN_HOUSE / `Blender` | OK | Tech Artist | PH0 | P0 | PROPOSED | SAMPLE | #5 |
| `ICO_ITM_TRUC_CO_DAN` | ICO | Icon Trúc Cơ Đan (màn túi đồ + tooltip phẩm) | — / — | ICON | AI / `FLUX.1 schnell` | PENDING | 2D/UI Artist | PH0 | P0 | PROPOSED | SAMPLE | #7 |

Ghi chú bảng mẫu:
- Dòng `PRP_ROCK_BOULDER_L_01` minh họa trạng thái bị trả lại: `returnTo = BLENDER_CLEANUP`, `rejectReason = "màu gói CC0 lệch bảng màu Thanh Trúc Lâm (AR-01 §4.1); chuyển sang atlas gradient chung"`.
- `assetId` của prop / VFX theo danh sách mảnh AR-06 §4 và mẫu AR-04 (v0.2 đổi từ `PRP_BAMBOO_ROCK_01`, `PRP_BAMBOO_SHRINE_01`, `VFX_ORB_HOA`, `VFX_TELEGRAPH_CIRCLE` — chưa có file nào dùng tên cũ). Bảng màu nguyên tố áp lúc chạy (`VPAL_*`), không tạo asset VFX riêng theo hệ.
- `license = PENDING` vì mọi mô hình AI và Mixamo ở TD-15 §4.5 hiện là ⚠️ (FLUX.1 schnell, Mixamo: "⚠️ chờ duyệt"; TRELLIS.2: "⚠️ rủi ro" — pipeline mặc định có phụ thuộc phi thương mại, TD-15 §4.7, OSS-Q-5) — được phép với `SAMPLE` (TRK-R-012), chặn với `RELEASE`.
- Bộ hoạt ảnh đầy đủ (~45 clip / vũ khí, AR-02 §5.2, danh mục AR-03 §4) chưa liệt kê; mức chi tiết theo clip hay theo bộ: TRK-Q-4.
- Màn UI mẫu #7 (khung túi đồ, tooltip) dùng tiền tố `UI_{MÀN}_*` (AR-02 v0.3, vd. `UI_INVENTORY_FRAME`); thêm dòng khi có brief UI.

Dòng CSV tương ứng (ví dụ hai dòng đầu và dòng bị trả lại):
```csv
assetId,kind,title,region,kit,budgetClass,measured,source,tools,license,provenance,owner,reviewer,phase,priority,state,returnTo,rejectReason,releaseIntent,effortHours,links,updated
CHR_PLAYER_SAMPLE_A,CHR,"Nhân vật kiếm, tỷ lệ A (1/5)",V1,—,CHARACTER,,AI,FLUX.1 schnell;TRELLIS.2;Blender;Mixamo,PENDING,,3D Artist,,PH0,P0,AI_GEN,,,SAMPLE,3,concept=art-source/concept/chr_sample_a_v1.png,2026-10-25
PRP_ROCK_BOULDER_L_01,PRP,Đá tảng lớn phủ rêu,V1,KIT_V1_ROCK,PROP,tris=620;tex=0;kb=48;bones=0,CC0,Quaternius assets;Blender,OK,art/provenance/PRP_ROCK_BOULDER_L_01.json,3D Artist,,PH0,P0,REJECTED,BLENDER_CLEANUP,"màu gói CC0 lệch bảng màu Thanh Trúc Lâm (AR-01 §4.1)",SAMPLE,1.5,,2026-10-25
```

### 7.1. Asset riêng của `V1_F01` (Phase 1 — GD-16 `V1_F01` §15.4)

Chỉ liệt kê asset `V1_F01` cần mà chưa có dòng ở §7; các mảnh kit khác lấy theo danh sách AR-06 §4 khi kit được dựng. `assetId` **đề xuất**; `releaseIntent = SAMPLE` cho tới khi `license = OK` (TRK-R-012).

| assetId | kind | title | region / kit | budgetClass | source / tools | license | owner | phase | prio | state | intent |
|---------|------|-------|--------------|-------------|----------------|:---:|-------|:---:|:---:|-------|:---:|
| `PRP_BAMBOO_STALK_FAR_01` | PRP | Trúc lấp lõi khối chặn, không va chạm (AR-06 KIT-R-048) | V1 / `KIT_V1_BAMBOO` | PROP | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH1 | P0 | PROPOSED | SAMPLE |
| `PRP_BAMBOO_STALK_FAR_02` | PRP | Như trên, biến thể 2 | V1 / `KIT_V1_BAMBOO` | PROP | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH1 | P0 | PROPOSED | SAMPLE |
| `PRP_MINE_ORE_NODE_01` | PRP | Mạch khoáng Hàn Thiết (`V1_F01_ORE_001..002`) | V1 / `KIT_V1_MINE` | PROP | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH1 | P0 | PROPOSED | SAMPLE |
| `MDL_MON_GREEN_SNAKE` | MDL_MON | Thanh Xà (`MON_GREEN_SNAKE`, SERPENT) | V1 / — | MONSTER_NORMAL | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH1 | P0 | PROPOSED | SAMPLE |
| `MDL_MON_WILD_BOAR` | MDL_MON | Dã Trư (`MON_WILD_BOAR`, QUADRUPED) | V1 / — | MONSTER_NORMAL | AI / `FLUX.1 schnell;TRELLIS.2;Blender` | PENDING | 3D Artist | PH1 | P0 | PROPOSED | SAMPLE |

Mô hình tinh anh `MON_E_BAMBOO_KING` (có thể dùng `tint` trên `MDL_MON_BAMBOO_RAT`) và mô hình NPC `V1_F01` thêm dòng khi GD-12 / LR-02 chốt `modelId`.

---

## 8. Dashboard & chỉ số

### 8.1. Công thức
- **Tỷ lệ hoàn thành** của một phase = số dòng `IN_GAME` / (tổng dòng của phase − `DROPPED`). Tính riêng cho `P0` và cho tất cả.
- **Tiến độ có trọng số** = trung bình trọng số trạng thái của các dòng (trừ `DROPPED`); `REJECTED` lấy trọng số của `returnTo`; bước bỏ qua theo §3.3 không làm giảm trọng số.

| Trạng thái | `PROPOSED` | `AI_GEN` | `BLENDER_CLEANUP` | `RIG_ANIM` | `ART_CHECK` | `ART_REVIEW` | `IN_GAME` |
|-----------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Trọng số | 0 | 0,15 | 0,35 | 0,55 | 0,70 | 0,85 | 1,00 |

- **Chỉ số phụ:** số dòng bị chặn license (`RELEASE` có `license ≠ OK`); số lần `REJECTED` / asset (tỷ lệ trả lại theo lý do); thời gian trung vị ở mỗi trạng thái (từ lịch sử git của `updated`); tổng `effortHours` theo `kind` và `source` (so AI với CC0 / tay — đầu vào cho đánh giá spike Phase 0 và PM-03 R4).

Ví dụ với bảng §7 (16 dòng P0 của `PH0`): hoàn thành 0 / 16 = **0%**; trọng số = (10 × 0 [`PROPOSED`] + 3 × 0,15 [`AI_GEN`] + 2 × 0,35 [`MDL_MON_BAMBOO_RAT` + `PRP_ROCK_BOULDER_L_01` qua `returnTo`] + 1 × 0,70 [`ART_CHECK`]) / 16 = 1,85 / 16 = **11,6%**.

### 8.2. Khối dashboard sinh tự động
`art:check --report` (đề xuất) ghi đè nội dung giữa hai marker; người không sửa tay khối này.

<!-- TRK:DASHBOARD:BEGIN -->
| Phase | Tổng | P0 | IN_GAME | % hoàn thành (P0 / tất cả) | Tiến độ trọng số | REJECTED | Chặn license (RELEASE) | Cập nhật |
|-------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|----------|
| PH0 | 16 | 16 | 0 | 0% / 0% | 11,6% | 1 | 0 | 2026-10-25 (mẫu tay, chưa có CSV) |
| PH1 | 0 | 0 | 0 | — | — | 0 | 0 | — |
| PH2 | 0 | 0 | 0 | — | — | 0 | 0 | — |
| PH3 | 0 | 0 | 0 | — | — | 0 | 0 | — |
| ALPHA | 0 | 0 | 0 | — | — | 0 | 0 | — |
| BETA1 | 0 | 0 | 0 | — | — | 0 | 0 | — |
| BETA2 | 0 | 0 | 0 | — | — | 0 | 0 | — |
| OB | 0 | 0 | 0 | — | — | 0 | 0 | — |
<!-- TRK:DASHBOARD:END -->

- <a id="trk-r-020"></a>**TRK-R-020** Cổng phase (PLAN §7 "Tiêu chí hoàn thành"): phase không được đóng khi còn asset `P0` của phase đó chưa `IN_GAME` hoặc `DROPPED` (bỏ phải có lý do được Producer đồng ý).

---

## 9. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="trk-ac-001"></a>TRK-AC-001 | Dòng có `assetId = chr_player_a` (chữ thường) | `art:check` | Fail (mẫu AR-02 §3, AST-R-010) |
| <a id="trk-ac-002"></a>TRK-AC-002 | Hai dòng cùng `assetId` | `art:check` | Fail (trùng khóa) |
| <a id="trk-ac-003"></a>TRK-AC-003 | File `art/env/bamboo/PRP_BAMBOO_STALK_02.glb` không có dòng tracker | CI | Fail (TRK-R-001) |
| <a id="trk-ac-004"></a>TRK-AC-004 | Dòng ở `main` là `PROPOSED`, PR đổi thành `IN_GAME` | CI | Fail (TRK-R-010) |
| <a id="trk-ac-005"></a>TRK-AC-005 | Dòng `REJECTED` không có `returnTo` | `art:check` | Fail (TRK-R-011) |
| <a id="trk-ac-006"></a>TRK-AC-006 | Dòng `ART_CHECK`, không có `art/provenance/{assetId}.json` | CI | Fail (AST-AC-005) |
| <a id="trk-ac-007"></a>TRK-AC-007 | Dòng `RELEASE`, `tools` có `TRELLIS.2` đang ⚠️ ở TD-15 §4.5 | Chuyển sang `ART_REVIEW` | Fail, `license = PENDING` (OSS-R-031) |
| <a id="trk-ac-008"></a>TRK-AC-008 | Dòng `SAMPLE`, `tools` có mô hình ⚠️ | Chuyển `AI_GEN → BLENDER_CLEANUP` | Đạt (TRK-R-012) |
| <a id="trk-ac-009"></a>TRK-AC-009 | `tools` có `FLUX.1 dev` (❌) | `art:check` | Fail, `license = BLOCKED` (OSS-AC-006) với cả `SAMPLE` lẫn `RELEASE` khi asset vào `art/` |
| <a id="trk-ac-010"></a>TRK-AC-010 | `MonsterDef.modelId = MDL_MON_BAMBOO_RAT`, dòng tracker `releaseIntent = SAMPLE` | `pnpm gamedata:check` + `art:check` | Fail (TRK-R-012) |
| <a id="trk-ac-011"></a>TRK-AC-011 | `owner = "artist@studio.vn"` | `art:check` | Fail (TRK-R-004) |
| <a id="trk-ac-012"></a>TRK-AC-012 | Dòng `MDL_MON_*` đi `BLENDER_CLEANUP → ART_CHECK` (bỏ `RIG_ANIM`) | CI | Fail (`MDL_MON` bắt buộc `RIG_ANIM`, §3.3) |
| <a id="trk-ac-013"></a>TRK-AC-013 | Dòng `ICO_*` đi `AI_GEN → ART_CHECK` | CI | Đạt (bước không áp dụng được bỏ qua, §3.3) |
| <a id="trk-ac-014"></a>TRK-AC-014 | Bảng §7 (16 dòng PH0, trạng thái như trên) | `art:check --report` | Dòng PH0 của §8.2: 0% hoàn thành, tiến độ trọng số 11,6% |
| <a id="trk-ac-015"></a>TRK-AC-015 | Asset `IN_GAME` có `measured.tris` vượt hàng `CHARACTER` của TD-11 sau khi sửa file | `art:check` | Fail (AST-R-003); dòng phải về `ART_CHECK` (TRK-R-013) |

---

## 10. Câu hỏi mở
- [ ] **TRK-Q-1:** Nguồn sự thật là CSV (đề xuất), YAML, hay Google Sheet đồng bộ về repo (PLAN §8.4 ghi "hoặc Sheet")? Sheet tiện cho người không dùng git nhưng mất review qua PR. *Mặc định: CSV trong repo.*
- [x] **TRK-Q-2:** Hoạt ảnh lấy từ thư viện Mixamo ghi `source` gì? Enum TD-15 §3.4 (`AI | CC0 | CC_BY | PURCHASED | IN_HOUSE`) không có giá trị cho "dịch vụ miễn phí có điều khoản riêng" (OSS-R-021). *(Đã chốt 2026-10-25: TD-15 v0.4 thêm `source = SERVICE` — OSS-R-034; `tools` chứa `Mixamo`, license theo dòng Mixamo của TD-15 §4.5.)*
- [x] **TRK-Q-3:** AR-02 §3 chưa có tiền tố cho **khung / màn UI**, **chân dung NPC**, **minh họa cơ duyên**, **bản đồ thế giới** (PLAN §6.6 phần 2D) và cho **biến thể mẫu** (`CHR_PLAYER_SAMPLE_A/B` ở §7 là đề xuất). *(Đã chốt 2026-10-25: AR-02 v0.3 §3 thêm `UI_`, `POR_`, `ILL_`, `WMAP_` và hậu tố `_SAMPLE_{X}` (AST-R-012); tiền tố còn chờ Art Director duyệt — AST-Q-5.)*
- [ ] **TRK-Q-4:** Hoạt ảnh theo dõi **mỗi clip một dòng** (đúng `assetId` AR-02, ~45 dòng / vũ khí) hay **một dòng mỗi bộ GLB hoạt ảnh**? Phụ thuộc AR-03 Animation List. *Mặc định: mỗi clip một dòng.*
- [x] **TRK-Q-5:** Mã hàng ngân sách `budgetClass` (§4 cột 6) chưa có trong AR-02 §6 / TD-11 — cần thêm cột mã vào bảng ngân sách để `art:check` tra được. *(Đã chốt 2026-10-25: AR-02 v0.3 §6 có cột `budgetClass` (AST-R-042), thêm `PORTRAIT`; số vẫn lấy từ TD-11 §3. Asset chưa có hàng ở TD-11 dùng `—` — AST-Q-4.)*
- [x] **TRK-Q-6:** GD-12 ví dụ `modelId: "MDL_WOLF"` (§5 JSON mẫu) lệch mẫu `MDL_MON_{TÊN}` của AR-02 §3 — sửa GD-12 hay AR-02? *(Đã chốt 2026-10-25: theo AR-02 — `modelId` = `MDL_MON_{TÊN}`; GD-12 sửa ví dụ theo mẫu này.)*
- [ ] **TRK-Q-7:** Asset âm thanh (AU-01) có dùng chung tracker này (thêm `kind`) hay tracker riêng?
- [ ] **TRK-Q-8:** Phase 0 mọi mô hình AI ở TD-15 §4.5 còn ⚠️: có cần duyệt ✅ ít nhất `TRELLIS.2` và `FLUX.1 schnell` trước khi kết thúc spike để mẫu chuyển được thành `RELEASE` ở Phase 1 không? (liên quan OSS-Q-3, OSS-Q-4) *Cập nhật 2026-10-25 (TD-15 v0.4 §4.7): `FLUX.1 schnell`, `SDXL`, `Mixamo` đã kiểm, "⚠️ chờ duyệt" — chỉ cần người duyệt ký; `TRELLIS.2` "⚠️ rủi ro" (phụ thuộc NVIDIA / RMBG-2.0 phi thương mại) — chờ OSS-Q-5, không duyệt được chỉ bằng chữ ký.*

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến, đề xuất nguồn sự thật `tools/art/asset-tracker.csv` kiểm bởi `art:check`, máy trạng thái 9 trạng thái đối chiếu AR-01 §9 / AR-02 §8, bước áp dụng theo loại, schema 22 cột, quy trình cập nhật (phần AI được / không được làm), kiểm tra CI, bảng mẫu 16 asset P0 Phase 0, dashboard theo phase, TRK-AC-001..015, câu hỏi mở |
| v0.2 | 2026-10-25 | — | Đồng bộ AR-02 v0.3 / TD-15 v0.4: `kind` thêm `ENV`, `TER`, `UI`, `POR`, `ILL`, `WMAP` (+ bước áp dụng §3.3); `budgetClass` theo AR-02 §6 (+ `PORTRAIT`, `—`); `source` thêm `SERVICE`; `license` tính theo OSS-R-051; trỏ TD-11 §3 (thay §4). Bảng mẫu: ANM dùng `SERVICE`; đổi `assetId` theo AR-06 / AR-04 (`PRP_ROCK_BOULDER_L_01`, `PRP_RUINS_SHRINE_ABANDONED`, `VFX_BOLT`, `VFX_TG_ENEMY`), số liệu dashboard không đổi. Đóng TRK-Q-2, TRK-Q-3, TRK-Q-5, TRK-Q-6; cập nhật TRK-Q-8 theo kết quả kiểm license |
| v0.3 | 2026-10-25 | — | Ghi chú D-28: team tự chọn nguồn asset |
| v0.4 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: §7.1 dòng tracker cho asset riêng của `V1_F01` (`PRP_BAMBOO_STALK_FAR_01..02`, `PRP_MINE_ORE_NODE_01`, `MDL_MON_GREEN_SNAKE`, `MDL_MON_WILD_BOAR`) |

