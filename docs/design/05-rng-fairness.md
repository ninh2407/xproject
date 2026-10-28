# GD-29 — Ngẫu nhiên & Công bằng (RNG)

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-29 · Prefix ID: `RF` |
| Owner | Lead Designer + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 1 (trước khi làm hệ thống có roll đầu tiên) |
| Quyết định nền | PLAN §4.7 "Nguyên tắc ngẫu nhiên công bằng", D-03 (một mạng), D-17 (không bán may rủi giảm rủi ro chết), rủi ro R13; TD-07 DT-Q-3 (đã chốt: **tách dữ liệu bí mật khỏi client**) |
| Liên quan | [PLAN §4.7](../../PLAN.md) · [Glossary](../lore/glossary.md) · [TD-01 §9](../tech/00-tdd.md) (thuật toán RNG, `rng_audit`) · [TD-07](../tech/06-gamedata.md) (bundle client / server) · [GD-27 Thiên phú](04-talents.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-10 Luyện đan](16-alchemy.md) · [GD-11 Luyện khí](17-forging.md) · [GD-08 Chế công pháp](14-technique-crafting.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-09 Vật phẩm](15-items-equipment.md) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [GD-21 Open World](22-open-world.md) · GD-19 Monetization |

> TD-01 §9 quy định **cách** roll (thuật toán, seed, `rng_audit`). Tài liệu này quy định **chính sách**: roll nào phải công khai tỷ lệ và công khai thế nào, dữ liệu nào giữ bí mật ở server, pity chuẩn, khí vận được và không được ảnh hưởng gì, cách người chơi tự kiểm tra lịch sử may rủi của mình.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-29 | Mục |
|--------------|-----------------|-----|
| Mọi GD có roll | Phân loại roll → nghĩa vụ hiển thị / kiểm toán | §3, §5.1 |
| GD-04, GD-08, GD-10, GD-11, GD-22, GD-27 | Chuẩn `ChanceView` cho màn xem trước, làm tròn | §5.2, §9.1 |
| GD-04, GD-10, GD-11 | Quy ước **điểm phần trăm** cho xác suất cộng dồn | §6.1 |
| GD-10, GD-11 | Tách seed minigame và seed kết quả | §5.5 |
| TD-07 | Danh sách dữ liệu `serverOnly` (DT-Q-3) | §5.4 |
| GD-12, GD-22, GD-21 | Mức mô tả độ hiếm (không lộ tỷ lệ chính xác) | §5.3 |
| GD-19 | Luật cho may rủi trả tiền thật | §5.6 |

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện* (may rủi tạo chuyện), P3 *Cái chết có ý nghĩa* (không chết vì một con xúc xắc ẩn).

- Người chơi **tin** rằng may rủi là thật và công bằng: thấy tỷ lệ trước khi liều, xem lại được lịch sử, không bị "nhà cái" gian.
- May rủi **tạo câu chuyện**, không tạo cảm giác bị trừng phạt: xui liên tục có bảo hiểm; không có kết cục tệ nhất do một roll ẩn.
- **Giữ bí ẩn của khám phá**: tỷ lệ rơi đồ hiếm, cơ duyên, vị trí bí mật không bị datamine từ client.
- Chuẩn hóa để **mọi hệ thống làm giống nhau** (AI không tự nghĩ cách hiển thị / làm tròn riêng).

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="rf-r-001"></a>**RF-R-001** | **Không có cái chết thật nào do một roll mà người chơi không thấy tỷ lệ trước.** Roll có thể dẫn tới chết thật (đột phá trong hồi quang phản chiếu, thiên kiếp) phải hiển thị tỷ lệ và xác nhận rõ "có thể chết" (GD-04, GD-26 LC-R-042). |
| <a id="rf-r-002"></a>**RF-R-002** | Mọi roll **quyết định có rủi ro** (loại A, §3) hiển thị tỷ lệ **do server tính** trước khi xác nhận; con số hiển thị **là** con số được dùng để roll (cùng `ChanceView`, §5.2). |
| <a id="rf-r-003"></a>**RF-R-003** | Kết quả roll được **ghi bền vững trước khi trả về client** (cùng transaction, TD-01). Ngắt kết nối, đóng tab, F5 sau khi bấm xác nhận **không** hủy được kết quả (chống "save-scum"). |
| <a id="rf-r-004"></a>**RF-R-004** | Client **không bao giờ** biết trước seed hay dữ liệu đủ để đoán kết quả của roll chưa diễn ra (§5.5). |
| <a id="rf-r-005"></a>**RF-R-005** | Dữ liệu đánh dấu `serverOnly` (§5.4) **không có mặt** trong bundle gửi client (TD-07). |
| <a id="rf-r-006"></a>**RF-R-006** | Không bán bằng tiền thật bất kỳ may rủi nào ảnh hưởng sức mạnh, tuổi thọ hay rủi ro chết (D-17). |

---

## 2. Tổng quan

```
                          ┌──────────── loại A: Quyết định có rủi ro ────────────┐
 Người chơi chọn liều ───►│ xem trước (ChanceView, server tính) → xác nhận      │──► roll API, rng_audit,
                          │ → roll → ghi kết quả → hiện                          │    Thiên Cơ Lục (§5.7)
                          └──────────────────────────────────────────────────────┘
 Khám phá (loại B) ──────► roll phía server, tỷ lệ BÍ MẬT, chỉ hiện "mức độ hiếm" sau khi đã gặp (§5.3)
 Khởi đầu (loại C) ──────► bảng tỷ lệ CÔNG KHAI đầy đủ (nút "Thiên cơ")
 Chiến đấu (loại D) ─────► chỉ số công khai (critRate…), roll trong Game Server, không audit từng lần
 Tiền thật (loại E) ─────► chỉ cosmetic, tỷ lệ chính xác + bảo hiểm công khai (§5.6)
```

---

## 3. Phân loại roll

- <a id="rf-r-010"></a>**RF-R-010** Mọi roll trong game thuộc **đúng một** loại dưới đây. GD mới có roll phải ghi loại ngay cạnh quy tắc roll.

| Loại | Định nghĩa | Hiển thị tỷ lệ | Kiểm toán | Pity |
|------|-----------|----------------|-----------|------|
| **A — Quyết định có rủi ro** | Người chơi chủ động chọn, có tiêu hao hoặc rủi ro | **Bắt buộc**, chính xác, trước khi xác nhận | `rng_audit` + Thiên Cơ Lục | **Bắt buộc** nếu tỷ lệ gốc < 50% và lặp lại được (§7) |
| **B — Phần thưởng khám phá** | Xảy ra khi chơi, người chơi không chọn "liều" | **Không** công khai số; mô tả mức độ hiếm sau khi gặp (§5.3) | `rng_audit` | Khuyến nghị cho phần thưởng hiếm (§7) |
| **C — Khởi đầu** | Tạo nhân vật, thức tỉnh đề xuất | **Công khai toàn bộ bảng** | `rng_audit` | Theo GD-27 |
| **D — Chiến đấu** | Chí mạng, né, xác suất status, AI chọn chiêu | Chỉ số liên quan hiển thị trong bảng chỉ số | Death log (TD-01 §12) | Không |
| **E — Tiền thật** | Bất kỳ roll nào mua bằng `JADE` | Chính xác + bảo hiểm cứng | `rng_audit` | **Bắt buộc**, đảm bảo (§5.6) |

### 3.1. Danh mục roll hiện có

| Roll | Loại | GD | `rng_audit.kind` |
|------|:---:|----|------------------|
| Tạo nhân vật: linh căn, chỉ số bẩm sinh, thiên phú | C | GD-27 | `CREATION_*` |
| Thức tỉnh: đề xuất thiên phú | C | GD-27 | `AWAKEN_OFFER` |
| Dung hợp thiên phú | A | GD-27 | `FUSION` |
| Đột phá cảnh giới | A | GD-04 | `BREAKTHROUGH` |
| Tâm ma (lựa chọn trong thử thách) | A (qua `ROLL_CHECK`) | GD-04, GD-22 | `HEART_DEMON_ROLL`, `HEART_TRIAL_PICK` |
| Luyện đan: thành công | A | GD-10 | `ALCHEMY_SUCCESS` |
| Luyện đan: phẩm chất, đan văn, bạo đan, nhiễu | A (phân phối hiển thị) | GD-10 | `ALCHEMY_QUALITY`, `_PATTERN`, `_YIELD`, `_DISTURB` |
| Luyện khí: thành công, phẩm, thuộc tính phụ, dị tượng | A | GD-11 | `FORGE_*` |
| Cường hóa, tẩy luyện, nâng phẩm | A | GD-11 | `ENHANCE`, `REROLL`, `UPGRADE` |
| Chế công pháp: điểm ngộ đạo, đột biến | A | GD-08 | `TECHNIQUE_*` |
| Lựa chọn may rủi trong cơ duyên (`ROLL_CHECK`) | A | GD-22 | `ROLL_CHECK` (trong `ENC_PICK` session) |
| Cơ duyên: có xảy ra, chọn cơ duyên nào | B | GD-22 | `ENC_TRIGGER`, `ENC_PICK` |
| Rơi đồ quái / boss | B | GD-12 | `MONSTER_LOOT` |
| Mở rương, rương hiểm địa | B | GD-09, GD-28 | `CONTAINER_OPEN` |
| Linh chủng xuất hiện, trứng rơi | B | GD-25 | `BEAST_LINH_CHUNG`, `MONSTER_LOOT` |
| Bắt linh thú (Ngự Thú Phù) | A (ChanceView liên tục, không `quoteId` — GD-25 SB-R-021) | GD-25 | `BEAST_CAPTURE` |
| Thu phục boss đặc biệt | A (ChanceView trong cửa sổ Kiệt Sức) | GD-25 | `BEAST_BOSS_CAPTURE` |
| Nở trứng: tư chất, đặc tính | A (hiện phân phối) | GD-25 | `BEAST_HATCH` |
| Lai tạo: loài, kế thừa, biến dị | A | GD-25 | `BEAST_BREED_*`, `BEAST_MUTATION*` |
| Phẩm / thuộc tính phụ khi vật phẩm sinh ra (rơi, thưởng) | B | GD-09 | `ITEM_GRADE`, `ITEM_AFFIX` |
| Vị trí bí mật mỗi kiếp | B | GD-21 | (không audit — tái lập từ seed nhân vật) |
| Chí mạng, né, status, AI | D | GD-06, GD-12 | — |

---

## 4. Thực thể & kiểu dữ liệu

```ts
type RollClass = 'A' | 'B' | 'C' | 'D' | 'E';

interface ChanceLine {                       // một dòng đóng góp vào tỷ lệ
  source: string;                            // "base" | "pill" | "pity" | "talent:TAL_X" | "furnace" | ...
  labelKey: string;                          // i18n
  value: number;                             // điểm phần trăm (pp) có dấu, vd. +10, −5
}

interface ChanceView {                       // chuẩn cho mọi màn xem trước loại A (RF-R-020)
  kind: string;                              // rng_audit.kind chính
  totalPct: number;                          // tỷ lệ sẽ dùng, đã clamp, 0–100, tối đa 1 chữ số thập phân
  rawPct: number;                            // trước clamp
  clamp: { min: number; max: number };
  lines: ChanceLine[];                       // Σ lines.value = rawPct
  unknownPart?: { labelKey: string; minPct: number; maxPct: number };   // phần chưa biết (vd. điểm minigame)
  outcomes?: OutcomeDistribution[];          // phân phối kết quả phụ (phẩm chất, phẩm phụ...)
  pity?: { counter: number; nextBonusPct: number; capPct: number };
  deathRisk: boolean;                        // RF-R-001
  quoteId: string;                           // RF-R-022
  expiresAt: string;
}

interface OutcomeDistribution {
  labelKey: string;                          // vd. "pill.quality.thuong"
  pct: number;                               // xác suất ước tính (mô phỏng hoặc giải tích), làm tròn RF-R-021
}

type RarityBand = 'COMMON' | 'UNCOMMON' | 'RARE' | 'VERY_RARE' | 'LEGENDARY';   // mô tả loại B (§5.3)

interface FortuneLogEntry {                  // Thiên Cơ Lục (§5.7)
  at: string; kind: string; labelKey: string; refId: string;
  shownPct: number | null; succeeded: boolean | null; outcomeLabelKey: string;
}
```

`ChanceBreakdown` của GD-04 §3 là một dạng cụ thể của `ChanceView.lines` (mỗi trường thành một dòng); API trả `ChanceView`, GD-04 giữ tên trường cho nội bộ.

---

## 5. Quy tắc

### 5.1. Nghĩa vụ theo loại
- <a id="rf-r-015"></a>**RF-R-015** Bảng §3 là bắt buộc. Roll loại A thiếu màn xem trước, loại C không có bảng công khai, loại E không có bảo hiểm: **không được phát hành** (checklist QA, §12).

### 5.2. Hiển thị tỷ lệ loại A
- <a id="rf-r-020"></a>**RF-R-020** Mỗi hành động loại A có endpoint `.../preview` (hoặc trường `chance` trong phản hồi xem trước sẵn có) trả `ChanceView`. Client **hiển thị đúng** `totalPct` và từng dòng `lines`; client có thể tính để hiển thị tạm khi người chơi thay đổi lựa chọn, nhưng **nút xác nhận chỉ mở khi đã có `ChanceView` mới nhất từ server**.
- <a id="rf-r-021"></a>**RF-R-021** **Làm tròn hiển thị:** `totalPct` hiển thị tới 1 chữ số thập phân nếu < 10%, số nguyên nếu ≥ 10% (vd. "4,5%", "62%"); làm tròn half-up (98,6 → "99%", 0,35 → "0,4%") nhưng **không bao giờ** hiển thị 100% khi tỷ lệ < 100 (99,7 → "99%") hay 0% khi tỷ lệ > 0 (0,04 → "< 0,1%"). Tỷ lệ dùng để roll là `totalPct` **không làm tròn hiển thị**. Ngoại lệ GD-22 `ROLL_CHECK`: hiển thị "~" làm tròn tới 5% (đặc trưng kể chuyện, ENC-R-005), giữ nguyên.
- <a id="rf-r-022"></a>**RF-R-022** **Báo giá (quote):** `ChanceView.quoteId` có hạn 60s. Request xác nhận gửi kèm `quoteId`; nếu trạng thái đã đổi khiến tỷ lệ khác (tiêu thêm đan, hết buff, đổi vị trí) → `409 CHANCE_CHANGED` kèm `ChanceView` mới, người chơi xác nhận lại. Không bao giờ roll bằng tỷ lệ **thấp hơn** tỷ lệ đã hiển thị mà không hỏi lại.
- <a id="rf-r-023"></a>**RF-R-023** Phần **chưa biết lúc xem trước** (vd. điểm minigame luyện đan / rèn chiếm `40 × (score − 0,5)` điểm %) hiển thị qua `unknownPart` dạng khoảng ("Hỏa hầu: −20% … +20%"). Sau minigame, trước khi roll thành công, client hiển thị tỷ lệ cuối (server tính từ điểm đã chấm) trong khoảnh khắc "khai lô".
- <a id="rf-r-024"></a>**RF-R-024** Phân phối kết quả phụ (`outcomes`: phẩm chất đan, phẩm phụ công pháp, phẩm vật phẩm rèn) hiển thị dạng thanh xác suất, làm tròn theo RF-R-021, tổng hiển thị có thể lệch 100% ±1 do làm tròn (ghi chú nhỏ).
- <a id="rf-r-025"></a>**RF-R-025** Nếu `deathRisk = true`: hộp xác nhận đỏ, ghi rõ "Thất bại sẽ **thân tử đạo tiêu**", hiển thị tỷ lệ thất bại (`100 − totalPct`), yêu cầu **giữ nút 2 giây** (không phải bấm nhanh).

### 5.3. Loại B: mô tả độ hiếm, không lộ số
- <a id="rf-r-030"></a>**RF-R-030** Tỷ lệ loại B **không** hiển thị dạng số. Sau khi người chơi đã **gặp** phần thưởng / nguồn (đã nhặt vật phẩm, đã đạt cấp Vạn Giới Lục của quái — GD-12, GD-21 §3.6), giao diện hiện **mức độ hiếm** theo bảng:

| `RarityBand` | Tên hiển thị | Khoảng xác suất thực (mỗi lần cơ hội) |
|--------------|--------------|---------------------------------------|
| `COMMON` | Thường gặp | ≥ 20% |
| `UNCOMMON` | Thỉnh thoảng | 5% – 20% |
| `RARE` | Hiếm | 1% – 5% |
| `VERY_RARE` | Rất hiếm | 0,1% – 1% |
| `LEGENDARY` | Cơ duyên trời định | < 0,1% |

  Band tính **từ dữ liệu ở khí vận 50** khi build (TD-07 generator), gửi xuống client thay cho tỷ lệ; khí vận không đổi band hiển thị.
- <a id="rf-r-031"></a>**RF-R-031** Khí vận và thiên phú ảnh hưởng loại B được **mô tả định tính** trong tooltip chỉ số ("Khí vận cao: dễ gặp cơ duyên phẩm cao và vật phẩm hiếm hơn") và **định lượng tương đối** ở màn chỉ số: "Cơ hội vật phẩm Hiếm: ×1,20 so với người thường" (tính từ công thức GD-12 §6.5 với band `RARE`), không lộ tỷ lệ gốc.
- <a id="rf-r-032"></a>**RF-R-032** Pity loại B (cơ duyên GD-22 ENC-R-030..034) được **giới thiệu cơ chế** trong game ("Lâu không gặp cơ duyên, thiên cơ sẽ dần ưu ái") nhưng **không** hiển thị bộ đếm.

### 5.4. Dữ liệu bí mật (`serverOnly`) — chốt TD-07 DT-Q-3
- <a id="rf-r-040"></a>**RF-R-040** Build gamedata xuất **hai bundle**: `bundle.server` (đầy đủ) và `bundle.client` (đã loại trường / file / thực thể `serverOnly`). Cách đánh dấu theo TD-07 §4.6: chú thích `@serverOnly` trên trường trong type TS, hoặc `"visibility": "server"` cho cả file trong `registry.json`.
- <a id="rf-r-041"></a>**RF-R-041** Danh sách **bắt buộc** `serverOnly`:

| Dữ liệu | Vị trí | Lý do | Client nhận gì thay thế |
|---------|--------|-------|-------------------------|
| Định nghĩa cơ duyên (điều kiện, `pTrigger`, trọng số, outcome) | `shared/encounters/defs/**` (cả file) | Không datamine cơ duyên | Nội dung qua message khi phiên diễn ra (GD-22) |
| Kịch bản Ink cơ duyên + i18n `enc.*` | `shared/encounters/ink/**`, `i18n/*/enc.json` | Lộ cốt truyện, outcome | Dòng thoại do Game Server gửi |
| Tham số pity / tần suất cơ duyên | `encounters/params.json` | | Mô tả định tính (RF-R-032) |
| Bảng rơi đồ (`entries`, trọng số) | `monsters/loot_tables.json` | Loại B | `RarityBand` theo vật phẩm × nguồn (sinh lúc build) |
| Bảng spawn, tinh anh xuất hiện (trọng số, điều kiện giờ / thời tiết) | `monsters/spawn_tables.json`, `elite_spawns.json` | Khám phá | — |
| Ứng viên vị trí bí mật, POI ẩn (`hiddenLevel > 0`), vị trí bẫy | `shared/maps/*.json` trường `secretCandidates`, `pois[hiddenLevel>0]`, `traps` | Bí mật / bẫy | Gửi qua interest management sau khi phát hiện (GD-21) |
| Bố cục hiểm địa (layout, rương, lối thoát theo pha) | `danger_zones/zones.json` trường `layouts` | Khám phá | Gửi khi vào lượt |
| Định nghĩa nhiệm vụ: nhánh ẩn, phần thưởng ẩn | `shared/quests/defs/**` trường đánh `@serverOnly` | Spoiler | API trả mục tiêu hiện tại |
| Bộ đếm pity, seed | (runtime, DB) | RF-R-004 | — |

- <a id="rf-r-042"></a>**RF-R-042** Dữ liệu **công khai** (giữ trong client): mọi tỷ lệ loại A và C (công thức, tham số), mẫu vật phẩm, kỹ năng, quái (chỉ số, chiêu), cửa hàng, đan phương / khí phổ (sau khi học), bảng tạo nhân vật (TLN-R-064). Nguyên tắc: **cái người chơi cần để quyết định thì công khai; cái để khám phá thì bí mật.**
- <a id="rf-r-043"></a>**RF-R-043** Validator TD-07: bundle client không chứa key nào thuộc danh sách RF-R-041; type `ClientGamedata` sinh ra **không có** các trường `serverOnly` (code client tham chiếu tới là lỗi biên dịch); i18n namespace server-only không có trong `i18n/{lang}.client.json`.

### 5.5. Seed và chống đoán trước
- <a id="rf-r-050"></a>**RF-R-050** **Seed minigame** (công khai, client cần để chạy minigame tất định: nhiễu hỏa khí GD-10, bài nhịp GD-11) và **seed kết quả** (bí mật, dùng cho roll thành công / phẩm / thuộc tính phụ / đan văn) là **hai seed độc lập**. Seed kết quả sinh **lúc chấm** (sau khi nhận chuỗi input), không lưu trước trong phiên. (Bổ sung GD-10 AL-R-050, GD-11 FG-R-001.)
- <a id="rf-r-051"></a>**RF-R-051** Không dùng seed theo nhân vật / theo ngày có thể suy ra để roll loại A, B, E (mỗi thao tác một seed mới, TD-01 TA-R-070). Ngoại lệ có chủ đích: **vị trí bí mật mỗi kiếp** (GD-21) dùng seed nhân vật lưu ở server (không gửi client).
- <a id="rf-r-052"></a>**RF-R-052** Game Server dùng seed phiên cơ duyên do API cấp (GD-22) — seed này **không** gửi client.

### 5.6. Tiền thật (loại E)
- <a id="rf-r-060"></a>**RF-R-060** Chỉ áp dụng cho **cosmetic** (ngoại hình, hiệu ứng, trang trí động phủ). Mọi thứ khác bị cấm (RF-R-006).
- <a id="rf-r-061"></a>**RF-R-061** Hiển thị **tỷ lệ chính xác** từng vật phẩm (2 chữ số thập phân), **bảo hiểm cứng** (vd. chắc chắn nhận vật phẩm hiếm nhất trong ≤ N lần, N công khai, bộ đếm hiển thị), không có vật phẩm trùng vô dụng (trùng → đổi thành tiền tệ cosmetic). Bộ đếm theo **tài khoản**, không mất khi nhân vật chết.
- <a id="rf-r-062"></a>**RF-R-062** Tuân thủ quy định pháp lý của thị trường phát hành (Việt Nam trước, D-07); chi tiết và danh sách vật phẩm: GD-19. Có thể quyết định **không có loại E** ở launch (GD-19). **Chốt ở GD-19 MZ-R-060: loại E tắt ở launch** (mở sau phải có ADR).

### 5.7. Thiên Cơ Lục: lịch sử may rủi của người chơi
- <a id="rf-r-070"></a>**RF-R-070** Người chơi xem **50 roll loại A gần nhất** của nhân vật hiện tại: thời điểm, hành động, tỷ lệ đã hiển thị, kết quả (`FortuneLogEntry`), và thống kê "Kỳ vọng: 3,2 lần thành công · Thực tế: 3". Lấy từ `rng_audit` (TD-01 §9) lọc theo kind loại A.
- <a id="rf-r-071"></a>**RF-R-071** Từ Thiên Cơ Lục, người chơi bấm **"Khiếu nại thiên đạo"** trên một mục → tạo ticket hỗ trợ kèm `refId`; GM dùng `rng-replay` (TD-01 TA-R-074) để xác minh.
- <a id="rf-r-072"></a>**RF-R-072** Roll loại B **không** hiện trong Thiên Cơ Lục (tránh lộ tỷ lệ), trừ dòng tóm tắt "Đã gặp N cơ duyên trong kiếp này".

### 5.8. Khí vận: được và không được
- <a id="rf-r-080"></a>**RF-R-080** Khí vận (`luck`, giá trị **cuối** GD-27 §6.3) **được** ảnh hưởng:
  - phân phối loại B (cơ duyên phẩm cao, rơi đồ hiếm);
  - **kết quả thưởng thêm** của loại A (đan văn, bạo đan, dị tượng, đột biến công pháp, một phần điểm phẩm chất);
  - chí mạng nhẹ (GD-06, trần ±2,5 điểm %).
- <a id="rf-r-081"></a>**RF-R-081** Khí vận **không được** ảnh hưởng:
  - **tỷ lệ thành công chính** của loại A (đột phá, luyện đan / rèn thành công, cường hóa, nâng phẩm, dung hợp);
  - bất kỳ roll nào dẫn tới **chết thật** hay mất thọ nguyên;
  - roll loại C (tạo nhân vật, thức tỉnh — giữ GD-27 TLN-Q-1 "mặc định: không");
  - loại E.
  *Lý do:* thành bại chính và sinh tử phải do **chuẩn bị** (đan, địa điểm, hộ pháp, tâm cảnh) quyết định để người chơi thấy mình kiểm soát được; khí vận làm "gia vị".
- <a id="rf-r-082"></a>**RF-R-082** **Bảng đăng ký ảnh hưởng của khí vận** (danh sách đóng; GD mới thêm phải thêm vào đây):

| Hệ thống | Công thức | Trần | GD |
|----------|-----------|------|----|
| Chọn cơ duyên | `luckFactor = clamp((luck/50)^tierLuckExp, 0,25, 4)` | ×0,25 … ×4 | GD-22 §6.2 |
| Rơi đồ | `clamp((luck/50)^rarityExp, 0,5, 2,5)` trên trọng số | ×0,5 … ×2,5 | GD-12 §6.5 |
| Điểm phẩm chất đan / khí | `+ (luck − 50) × 0,1` | ±5 điểm Q (luck 0–100) | GD-10 §6.3, GD-11 §6.3 |
| Đan văn | `clamp(0,10 + (luck − 50) × 0,002, 0, 0,25)` | 25% | GD-10 §6.4 |
| Bạo đan | `clamp(0,05 + (luck − 50) × 0,002, 0, 0,20)` | 20% | GD-10 §6.4 |
| Dị tượng | `clamp(0,05 + (luck − 50) × 0,002, 0, 0,15)` | 15% | GD-11 §6.3 |
| Ngộ đạo công pháp `q` | `+ (luck − 50) × 0,002` | ±0,1 (luck 0–100) | GD-08 §6.2 |
| Đột biến công pháp | `+ (luck − 50) × 0,001` | 12% | GD-08 §6.2 |
| Chí mạng | `+0,05 điểm % mỗi điểm` (quanh 50) | ±2,5 điểm % | GD-06 |
| Linh chủng xuất hiện | `× clamp(luck/50, 0,5, 2)` | ×0,5 … ×2 | GD-25 §6.1 |
| Biến dị linh thú | `+ (luck − 50) × 0,05` pp | ±2,5 pp | GD-25 §6.5 |

- <a id="rf-r-083"></a>**RF-R-083** Mọi công thức khí vận **neo tại 50** (khí vận 50 = người thường = giá trị thiết kế gốc) và có **trần trên và dưới**. Khí vận thấp làm phần thưởng ít hơn nhưng **không** tạo kết cục xấu mới.

---

## 6. Công thức

### 6.1. Xác suất cộng dồn theo điểm phần trăm
- <a id="rf-r-090"></a>**RF-R-090** Các hệ **xác suất cộng dồn** (nhiều nguồn cộng / trừ vào một tỷ lệ) tính bằng **điểm phần trăm (pp)**, thang 0–100, rồi clamp:
```
rawPct   = Σ lines.value                          (pp, có thể lẻ thập phân)
totalPct = clamp(rawPct, min, max)                (min/max theo GD)
success  = rng.nextFloat() × 100 < totalPct       (TD-01 TA-R-070)
```
  Đây là **ngoại lệ có đăng ký** của TD-07 DT-R-041 ("mọi tỷ lệ là phân số"): chỉ các tham số trong bảng dưới được lưu dạng pp. Hệ khác dùng phân số.

| Hệ | Tham số lưu dạng pp | Clamp | GD |
|----|---------------------|-------|----|
| Đột phá | `baseChance`, mọi tham số đóng góp §6.6 (`rootChanceMin/Max`, `locationChanceBonus`, `guardianBonus(Cap)`, `supportBonusCap`, `failurePityPerFail/Cap`, `heartDemonChancePenalty`, `channelHitPenalty(Cap)`…), hiệu ứng `BREAKTHROUGH_CHANCE_FLAT` (GD-27), `breakthroughBonus` (vật phẩm) | 5–95 | GD-04 §6.6 |
| Luyện đan thành công | `baseSuccess`, `pityPerFail`, `pityCap`, `successMin`, `successMax`, `furnace.successFlat` | 5–98 | GD-10 §6.2 |
| Luyện khí thành công | `baseSuccess`, `hammer.successFlat`; hằng số trong công thức §6.2 (pity +5 / trần 25) — nên đưa ra `params.json` (RF-Q-3) | 5–98 | GD-11 §6.2 |
| Cường hóa | `enhanceChance[]`, `enhancePityStep`, `enhancePityCap` | 0–100 | GD-11 §5.6 |
| Nâng phẩm | `upgradeChance{}`, `upgradeLevelBonus(Cap)`, `upgradePityStep`, `upgradePityCap` | 0–100 | GD-11 §5.8 |
| Bắt linh thú, biến dị linh thú | các tham số `*Pp` trong `beasts/params.json` | 5–90 / 0–60 | GD-25 §6.2, §6.5 |

  Trường pp được đánh dấu bằng chú thích **`@unit pp`** trong type TS (TD-07 DT-R-021); validator dùng chú thích này (giá trị 0–100 với tỷ lệ, có dấu với hạng mục đóng góp) và chặn `@unit pp` ở hệ không có trong bảng. Tên tham số pp mới **không** dùng hậu tố `Pct`/`Ratio` (tránh nhầm với phân số).

### 6.2. Kỳ vọng trong Thiên Cơ Lục
```
expectedSuccesses = Σ (shownPct_i / 100)          (các roll có tỷ lệ thành / bại)
```
Ví dụ: 5 lần luyện đan tỷ lệ 62, 62, 67, 72, 57 → kỳ vọng `3,2`.

### 6.3. Pity cộng dồn chuẩn
```
pityBonus = min(step × failsSinceLastSuccess, cap)     (pp)
```
Bộ đếm reset về 0 khi thành công. Thông số theo §7.

### 6.4. Giám sát lệch tỷ lệ (vận hành)
```
với mỗi kind loại A, cửa sổ 24h:  n lần roll, kỳ vọng E = Σ p_i, phương sai V = Σ p_i (1 − p_i)
z = (thực tế − E) / √V ;  |z| > 4 với n ≥ 500  → cảnh báo "RNG lệch" (khả năng lỗi code / dữ liệu)
```

---

## 7. Bảo hiểm (pity)

- <a id="rf-r-100"></a>**RF-R-100** Roll loại A có tỷ lệ gốc < 50% **và** người chơi có thể lặp lại phải có pity. Loại B phần thưởng phẩm cao nên có pity. Mọi pity phải thuộc **một trong bốn dạng chuẩn**:

| Dạng | Mô tả | Ví dụ |
|------|-------|-------|
| `ADDITIVE_PP` | Cộng `step` pp mỗi lần thất bại, trần `cap`, reset khi thành công | Đột phá, luyện đan, luyện khí, cường hóa, nâng phẩm |
| `MULTIPLICATIVE` | Nhân tỷ lệ `× (1 + n × step)`, trần `pMax` | Cơ duyên xảy ra (GD-22 ENC-R-030) |
| `WEIGHT_BOOST` | Sau N lần không đạt phẩm X, nhân trọng số nhóm phẩm ≥ X | Cơ duyên phẩm Huyền+ (ENC-R-034) |
| `GUARANTEE` | Sau N lần không đạt, lần kế **chắc chắn** đạt | Tạo nhân vật (TLN-R-063), loại E |

- <a id="rf-r-101"></a>**RF-R-101** **Bảng đăng ký pity** (danh sách đóng):

| Pity | Dạng | Bộ đếm | Phạm vi | Thông số | Reset | Mất khi chết? |
|------|------|--------|---------|----------|-------|:---:|
| Đột phá | `ADDITIVE_PP` | `breakthroughFailures[targetRealm]` | nhân vật × cảnh giới đích | +5 / lần, trần 20 | Đột phá thành công | ✅ (theo kiếp) |
| Luyện đan | `ADDITIVE_PP` | `recipe_known.pity_fails` | nhân vật × đan phương | +5, trần 25 | Mẻ thành công | ✅ |
| Luyện khí (rèn) | `ADDITIVE_PP` | `recipe_known.pity_fails` | nhân vật × khí phổ | +5, trần 25 | Thành công | ✅ |
| Cường hóa | `ADDITIVE_PP` | `forge_item_state.enhance_pity` | **vật phẩm** | +5, trần 25 | Cường hóa thành công | ❌ (đi theo vật phẩm, kể cả khi giao dịch / Tàng Bảo Các) |
| Nâng phẩm | `ADDITIVE_PP` | `forge_item_state.upgrade_pity` | **vật phẩm** | +10, trần 30 | Thành công | ❌ (như trên) |
| Cơ duyên xảy ra | `MULTIPLICATIVE` | `encounter_pity.pity_miss_count` | nhân vật | ×(1 + 0,15n), trần 0,9 | Đặt chỗ phiên thành công | ✅ |
| Cơ duyên phẩm cao | `WEIGHT_BOOST` | `sessions_since_huyen_plus` | nhân vật | sau 15 phiên, ×3 | Gặp phẩm Huyền+ | ✅ |
| Tạo nhân vật | `GUARANTEE` | `noRarePlusStreak` | phiên tạo nhân vật | sau 3 roll | Có RARE+ | (không áp dụng) |
| Bắt linh thú | `ADDITIVE_PP` | `beast_capture_pity.fails` | nhân vật × loài | +5, trần 20 | Bắt thành công loài đó | ✅ |
| Thu phục boss đặc biệt | `ADDITIVE_PP` | `beast_boss_capture.fails` | **tài khoản** × boss | +1, trần 10 (tổng ≤ 15%) | Thu phục boss đó | ❌ |
| Biến dị linh thú | `ADDITIVE_PP` | `beast_pity.breed_no_mutation` | **tài khoản** | +4, trần 16 | Có biến dị | ❌ |

- <a id="rf-r-102"></a>**RF-R-102** Pity **không** áp dụng cho: roll chiến đấu (D), `ROLL_CHECK` một lần trong cơ duyên, dung hợp thiên phú (rủi ro có chủ đích, mỗi vật phẩm dùng một lần), thử thách tâm ma (GD-04 CU-R-070 chỉ định).
- <a id="rf-r-103"></a>**RF-R-103** Pity loại A **hiển thị** trong `ChanceView.pity` và dòng `lines` có `source = "pity"` ("Hỏa hầu tâm đắc +10%").
- <a id="rf-r-104"></a>**RF-R-104** Pity theo vật phẩm (cường hóa, nâng phẩm) hiển thị trong tooltip vật phẩm khi > 0 ("Khí linh tích tụ: +10%") để người mua biết giá trị.

---

## 8. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/fairness/params.json` | Band độ hiếm, làm tròn, quote, giám sát |

```jsonc
// params.json
{
  "rarityBands": [
    { "band": "COMMON",    "minP": 0.20 },
    { "band": "UNCOMMON",  "minP": 0.05 },
    { "band": "RARE",      "minP": 0.01 },
    { "band": "VERY_RARE", "minP": 0.001 },
    { "band": "LEGENDARY", "minP": 0 }
  ],
  "displayDecimalsBelowPct": 10,
  "quoteTtlSec": 60,
  "deathRiskHoldSec": 2,
  "fortuneLogSize": 50,
  "monitor": { "windowHours": 24, "minSamples": 500, "zAlert": 4 }
}
```

**Validator (CI):** (RF-V-01) mỗi bảng rơi đồ / spawn / cơ duyên nằm trong file `serverOnly`; (RF-V-02) `bundle.client` không chứa key cấm (RF-R-043); (RF-V-03) trường `@unit pp` chỉ có ở các hệ trong bảng RF-R-090, tỷ lệ trong 0–100, giá trị tỷ lệ < 1 bị cảnh báo (nghi nhập phân số); (RF-V-04) mỗi `rng_audit.kind` trong `rng-kinds.yaml` có loại (A–E) khai báo ở bảng §3.1; (RF-V-05) công thức có `luck` chỉ ở các hệ trong RF-R-082 (kiểm tra bằng thẻ `@luck` trong type TS / gamedata); (RF-V-06) loại E không có vật phẩm phi cosmetic.


---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| (mọi hành động loại A) `.../preview` | Trả `ChanceView` | RF-R-020..025 |
| (mọi hành động loại A) xác nhận | Nhận thêm `quoteId`; lỗi `409 CHANCE_CHANGED` + `ChanceView` mới | RF-R-022 |
| `GET /v1/characters/{id}/fortune-log` | Thiên Cơ Lục: `{ entries: FortuneLogEntry[], expected, actual }` | RF-R-070 |
| `POST /v1/support/fortune-disputes` | `{ characterId, refId, note }` → ticket | RF-R-071 |
| `GET /v1/meta/rarity-bands` | Bảng band (đã có trong bundle client, endpoint cho web / wiki) | RF-R-030 |

Hành động loại A hiện có và endpoint xem trước: đột phá (GD-04 §9), dung hợp (GD-27), luyện đan (GD-10 tạo phiên), luyện khí / cường hóa / tẩy luyện / nâng phẩm (GD-11), chế công pháp (GD-08 `technique-forge/preview`). Lựa chọn `ROLL_CHECK` trong cơ duyên dùng tag `#check` (GD-22), không qua `quoteId`.

### 9.2. Lỗi mới (TD-01 §6.3)
`409 CHANCE_CHANGED` — thêm vào `shared/contracts/errors.yaml`.

### 9.3. Sự kiện / telemetry
| Sự kiện | Trường | Dùng cho |
|---------|--------|----------|
| `rng_roll` (telemetry, từ `rng_audit`) | kind, class, shownPct, success | Giám sát §6.4, kiểm chứng mô phỏng |
| `fortune_log_viewed` | | Người chơi có quan tâm minh bạch không |
| `fortune_dispute_created` | kind | Tỷ lệ khiếu nại (dấu hiệu cảm giác bất công) |
| `chance_changed_rejected` | kind, oldPct, newPct | UX báo giá |

---

## 10. Lưu trữ
Không có bảng mới: dùng `rng_audit` (TD-01 §9.2) và các bộ đếm pity trong bảng của GD sở hữu (RF-R-101). Thiên Cơ Lục truy vấn `rng_audit` theo `character_id` + kind loại A, chỉ trong kiếp hiện tại (`created_at ≥ character.created_at`).

Bảng `fortune_dispute (id, account_id, character_id, ref_id, note, status, gm_note, created_at, resolved_at)` thuộc module `admin`.

---

## 11. UI/UX
- **Màn xem trước loại A:** con số lớn ở giữa (`totalPct`), bên dưới **bảng đóng góp** (dòng xanh +, đỏ −), dòng pity riêng có biểu tượng, phần chưa biết dạng khoảng, thanh phân phối kết quả phụ. Nút xác nhận mờ khi `ChanceView` đang tải / hết hạn.
- **Rủi ro chết:** viền đỏ, chữ "Thân tử đạo tiêu", giữ nút 2 giây với vòng tiến trình.
- **Khoảnh khắc roll:** hiệu ứng kéo dài ≤ 2s; kết quả đã được ghi trước khi hiệu ứng chạy (RF-R-003); bỏ qua hiệu ứng được.
- **Thiên Cơ Lục:** trong bảng nhân vật, danh sách 50 mục + dòng "Kỳ vọng / Thực tế", nút khiếu nại trên mỗi mục.
- **Tooltip độ hiếm:** biểu tượng band (1–5 vạch mây) + tên band; không có số.
- **Bảng "Thiên cơ"** ở tạo nhân vật (GD-27 TLN-R-064): bảng tỷ lệ đầy đủ.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| TD-01 | Thuật toán, `rng_audit`, `rng-replay` |
| TD-07 | Bundle client / server, `@serverOnly`, trường pp, band sinh lúc build |
| GD-04, GD-08, GD-10, GD-11, GD-27 | `ChanceView`, pity chuẩn, quote |
| GD-12, GD-09, GD-28 | Loại B, band độ hiếm |
| GD-22 | Loại A (`ROLL_CHECK`) + loại B (xảy ra / chọn), pity ẩn |
| GD-21 | Bí mật, POI ẩn: `serverOnly` |
| GD-06 | Loại D |
| GD-19 | Loại E |
| QA | Checklist phát hành: mỗi roll mới có loại, `ChanceView` (A), bảng công khai (C), bảo hiểm (E), mô phỏng 100.000 lần khớp công thức |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Mất kết nối ngay sau khi bấm xác nhận | Kết quả đã ghi; vào lại thấy kết quả + thông báo (RF-R-003) |
| Hai tab cùng xác nhận một `quoteId` | Idempotency (TD-01 §7): một lần roll, tab kia nhận cùng kết quả |
| Buff hết hạn giữa xem trước và xác nhận | `409 CHANCE_CHANGED` (RF-R-022) |
| Tỷ lệ tăng giữa xem trước và xác nhận (vd. vừa thêm hộ pháp) | Vẫn `CHANCE_CHANGED` để hiển thị đúng con số sẽ dùng |
| `rawPct` > `max` | Hiển thị "95% (tối đa)" và dòng ghi chú bị chặn trần |
| Datamine client tìm bảng rơi đồ | Không có trong bundle (RF-R-040); chỉ có band |
| Người chơi thống kê từ cộng đồng ra tỷ lệ rơi | Chấp nhận (một phần văn hóa game); band vẫn đúng |
| Khiếu nại "đột phá 90% mà thất bại 2 lần" | Thiên Cơ Lục cho thấy tỷ lệ; GM `rng-replay` xác nhận; xác suất 1% là có thật |
| Pity vật phẩm cường hóa khi vật phẩm bị bán | Đi theo vật phẩm (RF-R-101) |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).
Xem §9.3. KPI: tỷ lệ khiếu nại / 1.000 roll loại A < 0,5; câu khảo sát "May rủi trong game có công bằng không" ≥ 70% đồng ý (khảo sát Alpha, PLAN Phase 4).

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="rf-ac-001"></a>RF-AC-001 | Bundle client đã build | Tìm key `loot_tables`, `encounters/defs`, `secretCandidates` | Không có (RF-R-040) |
| RF-AC-002 | Code client truy cập `gd.monsters.lootTables` | Biên dịch | Lỗi kiểu (`ClientGamedata` không có trường) |
| RF-AC-003 | Xem trước đột phá có 3 nguồn đóng góp | `preview` | `Σ lines.value = rawPct`; `totalPct = clamp(rawPct, 5, 95)` |
| RF-AC-004 | Xem trước 62,4% | Hiển thị | "62%"; roll dùng 62,4 |
| RF-AC-005 | Tỷ lệ 0,35%; 0,04%; 99,7% | Hiển thị | "0,4%"; "< 0,1%"; "99%" |
| RF-AC-006 | Quote hết hạn (> 60s) | Xác nhận | `409 CHANCE_CHANGED` + `ChanceView` mới |
| RF-AC-007 | Buff hết giữa xem trước và xác nhận | Xác nhận | `409 CHANCE_CHANGED` |
| RF-AC-008 | Bấm xác nhận rồi đóng tab ngay | Mở lại | Kết quả đã ghi, vật phẩm / tu vi đã thay đổi tương ứng |
| RF-AC-009 | Đột phá trong hồi quang phản chiếu | Xem trước | `deathRisk = true`; client yêu cầu giữ nút 2s |
| RF-AC-010 | Phiên luyện đan | Client nhận phản hồi tạo phiên | Chỉ có seed minigame; seed kết quả sinh lúc chấm và chỉ có trong `rng_audit` |
| RF-AC-011 | Hai phiên với cùng seed minigame, cùng input | Kết quả thành công | Có thể khác nhau (seed kết quả độc lập) |
| RF-AC-012 | Khí vận 20 và 80, cùng đột phá | `preview` | `totalPct` giống nhau (RF-R-081) |
| RF-AC-013 | Khí vận 80 | Tooltip vật phẩm Hiếm từ quái | Band "Hiếm" như khí vận 50; màn chỉ số hiện hệ số tương đối |
| RF-AC-014 | 5 lần luyện đan 62/62/67/72/57%, thành công 3 | Thiên Cơ Lục | Kỳ vọng 3,2 · Thực tế 3 |
| RF-AC-015 | Kiếp mới | Thiên Cơ Lục | Trống (chỉ kiếp hiện tại) |
| RF-AC-016 | Vật phẩm `enhance_pity = 2` được bán cho người khác | Người mua xem trước cường hóa | Dòng pity +10% |
| RF-AC-017 | Mô phỏng 1 triệu roll đột phá 40% với RNG chuẩn | Thống kê | 40% ± 0,2% |
| RF-AC-018 | Dữ liệu đặt `pityPerFail` (`@unit pp`) thành `0.05` | Validator | Cảnh báo RF-V-03 (nghi nhập phân số) |
| RF-AC-019 | Thêm công thức dùng `luck` vào tỷ lệ thành công đột phá | Validator / review | Fail RF-V-05 |
| RF-AC-020 | Giả lập lỗi khiến tỷ lệ thực tế 30% thay vì 40% (n = 1.000) | Giám sát §6.4 | Cảnh báo `|z| > 4` |
| RF-AC-021 | Người chơi gửi khiếu nại từ Thiên Cơ Lục | GM mở ticket | Có `refId`, `rng-replay` khớp |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 1–2 | Alpha | Beta |
|----------|:---:|:---:|:---:|
| Phân loại roll, `ChanceView`, pp, pity chuẩn | ✅ | | |
| Bundle client / server, `@serverOnly`, validator RF-V-01..06 | ✅ | | |
| Tách seed minigame / kết quả | ✅ | | |
| Band độ hiếm loại B | | ✅ | |
| Thiên Cơ Lục + khiếu nại | | ✅ | |
| Giám sát lệch tỷ lệ | | ✅ | |
| Loại E (nếu GD-19 có) | | | ✅ |

---

## 17. Câu hỏi mở
- [ ] **RF-Q-1:** Có công khai tỷ lệ rơi đồ chính xác **sau khi** người chơi đạt cấp tối đa Vạn Giới Lục của quái (phần thưởng cho người cày codex)? Đề xuất: có, ở Beta.
- [ ] **RF-Q-2:** Thiên Cơ Lục có nên xem được các kiếp trước (qua Bia Vạn Cổ) để kể chuyện "kiếp trước xui tận mạng"?
- [ ] **RF-Q-3:** Đưa hằng số pity luyện khí (+5 / trần 25, GD-11 §6.2) ra `forging/params.json` như luyện đan; có nên thêm hậu tố `Pp` cho tên trường pp (vd. `baseSuccessPp`) trước khi nhập dữ liệu thật?
- [ ] **RF-Q-4:** Có cần "chế độ minh bạch" cho streamer (hiện seed hash trước, seed sau — commit–reveal) không? Chi phí thấp, tăng niềm tin cộng đồng.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-23 | — | Bản nháp đầu: 5 loại roll + danh mục roll hiện có, `ChanceView` + làm tròn + báo giá, chống save-scum, band độ hiếm cho loại B, **dữ liệu `serverOnly` / hai bundle (chốt TD-07 DT-Q-3)**, tách seed minigame / kết quả, luật loại E, Thiên Cơ Lục + khiếu nại, khí vận được / không được + bảng đăng ký, quy ước điểm phần trăm cho xác suất cộng dồn, 4 dạng pity + bảng đăng ký, giám sát lệch tỷ lệ, nghiệm thu |
| v0.2 | 2026-10-25 | — | Loại E tắt ở launch (GD-19) |
| v0.3 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
