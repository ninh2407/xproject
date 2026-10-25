# GD-16 — Level Design (index)

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-16 · Prefix ID: `LV` (quy tắc chung); mỗi file bản đồ có prefix riêng `LV{n}{loại}{nn}` (LV-R-008) |
| Owner | Level Designer + Content Designer (duyệt: Game Designer, Tech Lead Client) |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 1: file `V1_F01` phải có trước khi dựng bản đồ thật ở M3 ([PM-06](../../pm/coding-plan.md)); mỗi bản đồ khác: trước khi blockout bản đồ đó |
| Quyết định nền | D-01 (logic 2D), D-02 (open world chia bản đồ), D-21 (`V1_F01` không PvP), D-22 (kit + cọ rải), PLAN Q15 (kích thước dã ngoại chưa chốt), PLAN §4.6 (checklist mật độ) |
| Liên quan | [GD-21 Open World](../22-open-world.md) §3.1–3.5 · [TD-13 Map Format](../../tech/12-map-editor.md) · [TD-12 World & Map](../../tech/11-world-maps.md) · [TD-14 Geometry](../../tech/13-geom2d.md) §2, §3.3 · [TD-11 Performance](../../tech/10-performance.md) §3.1 · [AR-06 Environment Kits](../../art/environment-kits.md) · [LR-01 World Bible](../../lore/world-bible.md) §2, §4.2, §7.2 · [GD-12](../18-enemies-ai.md) · [GD-15](../21-quests.md) · [GD-18](../41-onboarding.md) · [GD-22](../23-encounters.md) · [GD-30](../28-caves-sect-sites.md) · [GD-31](../29-sword-flight.md) · [GD-32](../31-pvp-infamy.md) · [PM-05 Playtest](../../pm/playtests/README.md) |

> GD-16 là **bố cục cụ thể từng bản đồ**: khu nào ở đâu, POI nào ở tọa độ nào, quái gì, linh thảo gì, người chơi đi thế nào và mất bao lâu. Luật chơi **không** nằm ở đây — mọi cơ chế lấy từ GD / TD nguồn. File level design là đầu vào cho (1) content designer dựng bản đồ trong map editor (TD-13 §5.2 bước 1), (2) AI sinh `MapDef` và gamedata spawn / POI, (3) QA kiểm checklist mật độ.

---

## 1. Phạm vi

| Có trong GD-16 | Không có trong GD-16 (tài liệu sở hữu) |
|----------------|-----------------------------------------|
| Bố cục khu, đường đi, khối chặn, cổng nối; tọa độ POI / vùng / spawn | Định dạng `MapDef`, danh mục `PoiKind`, validator (TD-13) |
| Chọn quái / linh thảo / NPC **có sẵn** cho từng chỗ; số lượng; thời gian hồi theo dải spec | Chỉ số quái, AI, bảng rơi (GD-12); vật phẩm (GD-09 / GD-10 / GD-11); hồ sơ NPC (LR-02) |
| Ánh xạ bước nhiệm vụ / onboarding → vị trí → thời lượng | Nội dung bước nhiệm vụ (GD-15), nhịp onboarding (GD-18) |
| Điểm kích hoạt cơ duyên, ứng viên bí mật | Nội dung cơ duyên (GD-22) |
| Đối chiếu checklist mật độ, ước ngân sách | Luật mật độ (GD-21 §3.4), số ngân sách (TD-11 §3.1, TD-13 §7) |
| Đề xuất tên địa danh nhỏ, mã mới *(đx)* | Đăng ký tên (LR-01 §7.2), đăng ký mã (tài liệu nguồn) |

---

## 2. Quy ước file

- Mỗi bản đồ mở (`HUB`, `F`) hoặc bí cảnh / hiểm địa có layout cố định (`BC`, `HD` — mỗi layout hiểm địa `V1_HD01_L1`… một file nếu khác biệt) có **một file** `docs/design/30-levels/{mapId}.md`; tên file = `MapDef.id` (GD-21 §3.1).
- Instance sinh động (`KN`, `DP`, `TD`, `TRIAL`) **không** có file riêng; nếu cần mẫu bố cục thì viết file theo mẫu `docs/design/30-levels/{TYPE}_TEMPLATE.md` (chưa có, LV-Q-7).
- File bản đồ dùng bảng thuộc tính đầu file và lịch sử thay đổi cuối file như mọi GD ([docs/README](../../README.md) §2), mã quy tắc / nghiệm thu / câu hỏi theo prefix của file (LV-R-008).
- Trạng thái từng file ghi ở bảng §5 của README này.

---

## 3. Cấu trúc bắt buộc của một file bản đồ (template)

```markdown
# GD-16 · {mapId} — {Tên bản đồ}

| Thuộc tính | Giá trị |   ← Mã tài liệu (GD-16 · {mapId} · Prefix ID), Owner, Trạng thái, Cần trước, Quyết định nền, Liên quan

## 1. Thông tin bản đồ
   mapId, type, tên (LR-01), vực, realmRange, phase, kích thước (ghi "tạm" nếu Q15 chưa chốt), chunks,
   số ô fog, sức chứa kênh (TD-12), cờ PvP / timeFlow / fog / weatherOverride, bay (GD-31),
   ambient (lightingProfileId, musicId, palette PAL_*), kit + mẫu biome (AR-06)
## 2. Vai trò thiết kế
   phục vụ hệ thống / nhiệm vụ / onboarding nào; cảm xúc mong muốn; câu chuyện môi trường (LR-01 §4.2)
## 3. Sơ đồ bố cục
   3.1 ASCII có lưới tọa độ mét (TD-13 §2) + chú giải
   3.2 Khối chặn (địa hình chặn, tỷ lệ đi được 55–70%)
   3.3 Đường chính / phụ (polyline tọa độ), lối tắt
   3.4 Quy tắc bố cục bắt buộc ({PREFIX}-R-nnn)
## 4. Khu vực con        areaId · tên · polygon CCW · realmHint · mô tả · kit / biome · mật độ prop
## 5. POI, vùng, kết nối, điểm hồi sinh
   bảng POI (poiId · kind TD-13 §4 · pos · bán kính · hiddenLevel · conditions · props · khu · liên kết)
   bảng ZoneDef · bảng ConnectionDef · respawnPoints · landmark
## 6. Spawn quái        SpawnTable / EliteSpawnDef / LootTable (GD-12) + spawnAreas (TD-13, cùng id SP_*): SP_*, ESP_*, LT_*, MON_*, vị trí, số lượng, hồi, ngày / đêm (SG_* chỉ cho cơ duyên / sự kiện)
## 7. Điểm thu thập     HERB / ORE: ID ITM_*, số lượng, hồi, điều kiện; linh thảo hiếm (GD-21 §3.10) nếu áp
## 8. Luồng người chơi  bước GD-18 / GD-15 → vị trí → thời lượng; tổng thời gian ở bản đồ
## 9. Cơ duyên & bí mật ENC_* áp dụng, điểm kích hoạt, ứng viên bí mật (@serverOnly), lối tắt
## 10. Đối chiếu checklist mật độ GD-21 §3.4 / PLAN §4.6   mục → chuẩn (quy đổi) → giá trị → đạt / chưa
## 11. Ngân sách         TD-11 §3.1 / TD-13 §7: tải, logic JSON, draw call, tam giác, va chạm — ước + cách đạt
## 12. Ánh sáng, ngày / đêm, thời tiết, âm thanh, VFX môi trường
## 13. Ví dụ MapDef rút gọn (JSON, đúng TD-13 §3)
## 14. Tiêu chí nghiệm thu {PREFIX}-AC-nnn (validator TD-13 hoặc playtest PM-05)
## 15. Danh sách đồng bộ  tên mới → LR-01; mã đx → GD / TD nguồn; asset → AR-05 / AR-06
## 16. Câu hỏi mở         {PREFIX}-Q-n

## Lịch sử thay đổi
```

Mục nào không áp dụng (vd. bí cảnh không có fog) vẫn giữ tiêu đề và ghi "Không áp dụng — lý do".

---

## 4. Quy tắc chung

- <a id="lv-r-001"></a>**LV-R-001** **File level design là nguồn sự thật của bố cục**; `shared/maps/{mapId}.json` (TD-13 MF-R-001) là nguồn sự thật của **dữ liệu logic** xuất ra từ editor và phải khớp các bảng trong file (ID, `kind`, tọa độ ± 0,5m, `props`, số lượng). Khi blockout / playtest buộc đổi bố cục: sửa file GD-16 trước (tăng phiên bản, ghi lịch sử), rồi sửa bản đồ ([docs/README](../../README.md) §3 mục 7).
- <a id="lv-r-002"></a>**LV-R-002** **Hệ tọa độ** theo TD-13 §2 / TD-14 §2: mét, cặp `[x, z]`, gốc góc **Tây Nam**, `+x` Đông, `+z` Bắc, bản đồ trong `[0, width] × [0, depth]`; đa giác (khu, vùng, vùng spawn) liệt kê đỉnh **ngược chiều kim đồng hồ**; tọa độ làm tròn tới **0,5m** (lưới logic GD-21 §3.1). Sơ đồ ASCII chỉ minh họa; số trong bảng là số chuẩn.
- <a id="lv-r-003"></a>**LV-R-003** **Kích thước tạm khi Q15 chưa chốt:** bản đồ dã ngoại dùng **300 × 300m** — giá trị của ví dụ chuẩn TD-13 §8 và kích thước chuẩn của checklist GD-21 §3.4 — và ghi rõ "tạm (Q15)". Thành trấn dùng giá trị trong dải GD-21 §3.1 (150–250m) do file bản đồ đó chọn. Khi Q15 chốt kích thước khác: nhân mọi tọa độ với `s = kích thước mới / 300` làm bản phác, rồi bố cục lại, chạy lại validator, cập nhật §10 / §11; **không đổi ID POI** nếu bản đồ đã phát hành (MF-R-021).
- <a id="lv-r-004"></a>**LV-R-004** **Mật độ quy đổi theo diện tích:** mỗi dòng số lượng của checklist GD-21 §3.4 (chuẩn 300 × 300m = 90.000 m²) quy đổi `n' = round(n × W × D / 90.000)` cho cả cận dưới và cận trên; dòng "1" cố định (linh mạch, truyền tống trận, điểm mốc) không quy đổi. Mỗi file có bảng đối chiếu §10; mục kiểm được bằng máy thì ghi mã validator (MF-V-08, MF-V-09…), mục còn lại kiểm bằng playtest PM-05 / telemetry GD-21 §9.
- <a id="lv-r-005"></a>**LV-R-005** **Validator là cổng chất lượng:** file bản đồ chỉ chuyển 🟢 khi `pnpm maps:build {mapId}` xanh với MF-V-01..17 (TD-13 §6) và mọi `-AC-` loại "validator" của file có test. Mục mật độ không có trong MF-V-08 (bí mật, NPC, cảnh quan…) kiểm bằng lớp phủ mật độ E8 của editor và playtest.
- <a id="lv-r-006"></a>**LV-R-006** **Không tự thêm danh sách đóng:** `MapDef.type`, `PoiKind` (TD-13 §4, MF-R-020), `ZoneDef.kind`, `ConnectionDef.type`, `role` của POI `NPC`; và các mã `MON_*`, `SG_*`, `ITM_*`, `NPC_*`, `ENC_*`, `QC_*`, `PAL_*`, `BIO_*`, `PRP_*` chỉ dùng mã có trong tài liệu nguồn. Thiếu → ghi mã / loại đề xuất kèm nhãn **(đx)**, đưa vào §15 (đồng bộ) và §16 (câu hỏi); mục (đx) về loại / trường **không** đưa vào ví dụ `MapDef` §13 cho tới khi TD-13 có.
- <a id="lv-r-007"></a>**LV-R-007** **Không hằng số gameplay mới:** số lượng, bán kính, thời gian hồi trong file là **giá trị khởi điểm** nằm trong dải của spec nguồn (GD-21 §3.4, GD-12 §5.2, GD-30 HM-R-010…), đi vào gamedata / `shared/maps`, không hard-code.
- <a id="lv-r-008"></a>**LV-R-008** **Định danh:** khu `{mapId}_A{nn}`, POI `{mapId}_{KIND rút gọn}_{nnn}` (GD-21 §3.1, MF-R-021), vùng spawn `SP_{mapId}_{areaId rút gọn}` (TD-07 DT-R-051). Mã cục bộ trong một `MapDef` theo mẫu của ví dụ TD-13 §8 (chưa thành quy tắc — LV-Q-4): vùng `z_{tên}`, kết nối `{n|e|s|w}_{đích viết thường}` (vd. `e_hub`, `w_f01`), điểm hồi sinh `rp_{n}`. **Prefix mã tài liệu của file bản đồ** = `LV` + mapId bỏ `V` và `_` đầu: `V1_F01` → `LV1F01`, `V1_HUB` → `LV1HUB`, `V1_BC01` → `LV1BC01` (anchor chữ thường: `lv1f01-r-001`; tên test `LV1F01_AC_001_…`).
- <a id="lv-r-009"></a>**LV-R-009** **Tên địa danh mới** (khu, điểm nhỏ, bia) theo quy tắc đặt tên LR-01 §2 (2–4 chữ Hán Việt hoặc mô tả thuần Việt kiểu "miếu hoang", "bãi tập" như LR-01 §7.2), không trùng tên trong LR-01 / LR-02, không dùng từ của tôn giáo có thật (WB-R-008). Mọi tên mới liệt kê ở §15 của file để đăng ký vào LR-01 §7.2 **trước khi** dùng trong `nameKey` / Ink (WB-R-010).
- <a id="lv-r-010"></a>**LV-R-010** **Dữ liệu `@serverOnly`** (`secretCandidates`, `rareHerbGroups`, `props.shape` của `TRAP`, `lootTableId` của `CHEST` — GD-29 RF-R-041) chỉ ghi trong mục có nhãn `@serverOnly` của file; không vẽ lên ảnh minimap / preview công khai và không đặt prop trang trí trong 0,5m quanh các vị trí đó (TD-13 MF-R-036, MF-V-16).
- <a id="lv-r-011"></a>**LV-R-011** **Phase:** POI / spawn / NPC thuộc phase muộn hơn Alpha ghi rõ phase (TD-07 DT-R-058, LR-02 CHR-AC-011) để build hồ sơ `alpha` loại ra; vị trí của chúng vẫn được giữ chỗ trong bố cục từ đầu (vd. `CAVE_GATE` — GD-30 §16, AR-06 §4.12).
- <a id="lv-r-012"></a>**LV-R-012** Bản đồ chứa bước onboarding (GD-18) hoặc bước nhiệm vụ chính (GD-15 §5.10) phải có **bảng luồng §8** ánh xạ từng bước → vị trí (POI / khu / marker) → thời lượng, và thời lượng cộng dồn phải khớp mục tiêu của GD-18 (vd. OB-AC-001).

---

## 5. Danh sách bản đồ Vực 1 — Thanh Vân Vực

Nguồn: GD-21 §3.2, LR-01 §4.2. Kích thước theo GD-21 §3.1 (dã ngoại: tạm 300 × 300m theo LV-R-003).

| `mapId` | Tên | Loại | Cảnh giới | Kích thước | Nội dung đặc biệt (GD-21) | File GD-16 | Trạng thái |
|---------|-----|------|-----------|-----------|---------------------------|-----------|------------|
| `V1_HUB` | Thanh Vân Trấn | `HUB` | — | 150–250m (chưa chọn) | Đan Phòng, lò rèn, sàn giao dịch, Khâm Thiên Đài, truyền tống trận trung tâm, Luân Hồi Đài | — | ⬜ chưa viết |
| `V1_F01` | Thanh Trúc Lâm | `F` | LK1–3 | 300 × 300m (tạm) | Bản đồ hướng dẫn, không PvP, 1 linh mạch nhỏ | [V1_F01.md](V1_F01.md) | 🟡 v0.2 |
| `V1_F02` | Bích Thủy Hà Bạn | `F` | LK2–4 | 300 × 300m (tạm) | Linh thảo mưa, cơ duyên ngư ông | — | ⬜ |
| `V1_F03` | Hoang Thạch Khoáng | `F` | LK3–5 | 300 × 300m (tạm) | Cửa BC01, Tạc Thiên Trại | — | ⬜ |
| `V1_F04` | Vụ Ẩn Trạch | `F` | LK4–6 | 300 × 300m (tạm) | Luôn có sương, cửa HD01 | — | ⬜ |
| `V1_F05` | Lạc Hà Cốc | `F` | LK5–7 | 300 × 300m (tạm) | Cửa BC02, quái dạ hành, Hợp Hoan Cốc | — | ⬜ |
| `V1_F06` | Cổ Chiến Nguyên | `F` | LK7–9 → TC | 300 × 300m (tạm) | Cửa BC03, HD02, cổng Vực 2 | — | ⬜ |
| `V1_BC01` | Phế Khoáng Động | `BC` (solo) | LK5 | Tuyến tính 10–15 phút | Boss `MON_B_MINE_TYRANT` | — | ⬜ |
| `V1_BC02` | Hồ Yêu Động | `BC` (tổ đội) | LK7 | Tuyến tính 10–15 phút | Boss `MON_B_FOX_MATRIARCH` | — | ⬜ |
| `V1_BC03` | Tàn Kiếm Trủng | `BC` (tổ đội) | LK9 | Tuyến tính 10–15 phút | Boss `MON_B_SWORD_SPIRIT` | — | ⬜ |
| `V1_HD01` | Vạn Độc Đầm | `HD` | LK5+ | 150–300m, nhiều layout | Ngưng Cơ Thảo (GD-28) | — | ⬜ |
| `V1_HD02` | Cổ Chiến Trường | `HD` | TC | 150–300m, nhiều layout | Trấn Hồn Đài (GD-28) | — | ⬜ |

Bí cảnh nhỏ của Phase 1 (PLAN §7) chưa xác định (AR-06 KIT-Q-4) — khi chốt, thêm dòng và file.

---

## 6. Quan hệ với GD-21, TD-13, AR-06, PM-05

```
GD-21 (luật thế giới, checklist §3.4) ─┐
GD-12/10/15/18/22/LR-01/LR-02 (nội dung)├─► GD-16 {mapId}.md ──► map editor (TD-13 §5.2: blockout → kit / biome AR-06)
TD-13 (MapDef, PoiKind, validator) ────┘          │                       │
                                                  │                       ▼
                                                  │        pnpm maps:build {mapId} (MF-V-01..17, ngân sách §7)
                                                  │                       │
                                                  └──── playtest PM-05 ◄──┘  (PT-S-07/08, heatmap GD-21 §9)
```

- **GD-21 §3.4** cho luật mật độ; GD-16 §10 của từng file là bảng đối chiếu. Mục MF-V-08 tự động hóa (điểm thu thập 15–30, ≥ 30% có điều kiện…) được ghi mã validator; mục không tự động hóa (bí mật, NPC, cảnh quan "đáng chụp", quy tắc 60 giây) dùng lớp phủ E8 và telemetry `position_sample`.
- **TD-13 validator** là cổng chặn merge (MF-R-050); `-AC-` của file bản đồ trích mã MF-V tương ứng.
- **AR-06** cung cấp kit / bảng màu / biome; GD-16 chốt **điểm mốc** của từng bản đồ (trả lời AR-06 KIT-Q-1 cho bản đồ đó).
- **PM-05** đo trải nghiệm (thời gian băng qua, độ phủ, cảm nhận mật độ) — dữ liệu chốt Q15.

---

## 7. Câu hỏi mở

- [ ] **LV-Q-1** (= PLAN Q15, GD-21 §11, TD-13 MF-Q-2): kích thước dã ngoại chuẩn 250m hay 400m. *Mặc định:* 300 × 300m tạm (LV-R-003); chốt sau playtest `V1_F01` Phase 1.
- [x] **LV-Q-2:** *(Đã chốt 2026-10-25: chấp nhận mặc định `LV{n}{loại}{nn}` — LV-R-008.)* Prefix mã tài liệu theo bản đồ `LV{n}{loại}{nn}` (LV-R-008) có được duyệt không, hay dùng một prefix `LV` chung + số theo dải (vd. `LV-R-100..199` cho `V1_F01`)? *Mặc định:* `LV{n}{loại}{nn}`.
- [x] **LV-Q-3:** *(Đã chốt 2026-10-25: theo GD-12 — `SpawnTable` `SP_*` là nguồn hành vi; `MapDef.spawnAreas[]` cùng `id`, chỉ có `polygon` + `spawnPoints`, bỏ `spawnGroupIds`; `SG_*` không gắn bản đồ — TD-13 v0.9 §3 / MF-R-010, GD-12 v0.5.)* Mô hình spawn vùng **vênh** giữa TD-13 và GD-12: `MapDef.spawnAreas[].spawnGroupIds` trỏ `SG_*` (TD-13 §3, ví dụ `SG_V1_BAMBOO_RATS`), trong khi GD-12 dùng `SpawnTable` `SP_{mapId}_{areaId}` (`entries.monsterId`, `pack`, `maxAlive`, `respawnSec` — EN-R-030..031) cho quái theo khu và để `SpawnGroupDef` (`SG_*`) cho quái do cơ duyên sinh (EN-R-110). `SG_V1_BAMBOO_RATS` không có trong GD-12. *Mặc định cho file bản đồ:* ghi đủ cả hai — `SpawnTable` theo GD-12 là nguồn hành vi spawn (id = `SpawnAreaDef.id`), `spawnGroupIds` điền mã `SG_*` *(đx)* tương ứng để qua MF-R-010 — chờ TD-13 / GD-12 thống nhất.
- [ ] **LV-Q-4:** Quy ước mã cục bộ trong `MapDef` (`z_*`, `e_hub` / `w_f01`, `rp_*`) mới chỉ có trong ví dụ TD-13 §8, chưa thành quy tắc. *Mặc định:* dùng như LV-R-008; đề xuất TD-13 ghi thành MF-R.
- [x] **LV-Q-5:** *(Đã chốt 2026-10-25: TD-13 v0.9 §4 thêm `PoiKind` `ENCOUNTER_POINT` (props tùy chọn `tags`) + mã rút gọn `ENC` (MF-R-021); điểm cơ duyên được đưa vào `MapDef`.)* GD-22 dùng POI `ENCOUNTER_POINT` (trigger `POI_PROXIMITY`, ID mẫu `V1_F02_ENC_001`; glossary cũng liệt kê) nhưng **danh sách đóng `PoiKind` của TD-13 §4 không có** loại này, cũng không có mã rút gọn `ENC`. Checklist GD-21 §3.4 yêu cầu 3–5 điểm cơ duyên mỗi dã ngoại. *Mặc định:* file bản đồ ghi vị trí điểm cơ duyên với `kind: ENCOUNTER_POINT` *(đx)*, không đưa vào ví dụ `MapDef`; cơ duyên bắt buộc của bản đồ dùng trigger không cần POI (`AREA_ENTER`) khi có thể.
- [ ] **LV-Q-6:** Cảnh quan "đáng chụp" (GD-21 §3.4) không có `PoiKind` riêng — chỉ là bố cục, hay đặt POI `LANDMARK` (không `props`) để hiện trên bản đồ thế giới? *Mặc định:* chỉ bố cục (không POI), kiểm bằng playtest.
- [ ] **LV-Q-7:** Có cần file mẫu bố cục cho instance sinh động (`KN` động phủ cổ, `DP`, `TD`, `TRIAL`) không? *Mặc định:* chưa, viết khi thiết kế instance đầu tiên của loại đó.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: phạm vi GD-16, quy ước một file mỗi bản đồ, template 16 mục bắt buộc, quy tắc chung LV-R-001..012 (nguồn sự thật bố cục, hệ tọa độ TD-13, kích thước tạm khi Q15 chưa chốt, quy đổi mật độ, validator, không thêm danh sách đóng, định danh + prefix theo bản đồ, tên địa danh, `@serverOnly`, phase, bảng luồng), danh sách bản đồ Vực 1 (chỉ `V1_F01` có file), quan hệ GD-21 / TD-13 / AR-06 / PM-05, câu hỏi mở LV-Q-1..7 |
| v0.2 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01` v0.2: chốt LV-Q-2 (prefix `LV{n}{loại}{nn}`), LV-Q-3 (spawn theo GD-12, TD-13 v0.9), LV-Q-5 (`ENCOUNTER_POINT` trong TD-13 §4); template §6 bỏ `SG_*`, thêm `ESP_*`, `LT_*`; bảng §5: `V1_F01` 🟡 v0.2 |
| v0.3 | 2026-10-25 | — | Kết nối Vực 1 đã chốt toàn bộ (GD-21 OW-6) |

