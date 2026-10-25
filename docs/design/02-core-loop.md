# GD-03 — Core Loop & Progression (vòng lặp & đường cong tiến trình)

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-03 · Prefix ID: `LOOP` |
| Owner | Lead Designer + Economy / Balance Designer |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Phase 1 (khung), Phase 3 (Balance Simulator chạy thật — PLAN Phase 3) |
| Quyết định nền | PLAN §4.1–4.2, **D-11** (1 giờ chơi ≈ 1 năm; kiếp đầu 40–80 giờ), D-05 (tu vi dựa trên vật phẩm, đánh quái có giới hạn), D-13, D-20, roadmap 21–22 tháng (Beta 1 / 2) |
| Liên quan | [GD-04 Tu luyện](10-cultivation.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-13 Kinh tế](19-economy.md) · [GD-10 Luyện đan](16-alchemy.md) · [GD-11 Luyện khí](17-forging.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [GD-15 Nhiệm vụ](21-quests.md) · [GD-14 Xã hội](20-social.md) · [GD-08](14-technique-crafting.md) / [GD-25](26-spirit-beasts.md) / [GD-30](28-caves-sect-sites.md) (Beta) · GD-20 Telemetry |

> Tài liệu này **không thêm cơ chế mới**. Nó (1) mô tả vòng lặp ở mọi thang thời gian, (2) chốt **đường cong tiến trình mục tiêu** làm chuẩn cho mọi con số ở các GD, (3) **đối chiếu chéo số liệu** đã viết và liệt kê mâu thuẫn (§7), (4) đặc tả **Balance Simulator** để kiểm chứng. Khi GD khác đổi số liệu tiến trình, phải chạy lại simulator theo §8.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-03 | Mục |
|--------------|-----------------|-----|
| GD-04, GD-26 | Đường cong mục tiêu (giờ chơi ↔ cảnh giới ↔ tuổi), tỷ trọng nguồn tu vi | §4, §5 |
| GD-13 | Thu nhập / chi phí theo thời gian chơi | §6 |
| GD-10, GD-12, GD-22, GD-28 | Ngân sách đóng góp tu vi / thời gian của từng hoạt động | §5 |
| GD-20 | KPI tiến trình cần đo | §9 |
| PLAN Phase 3 | Đặc tả Balance Simulator | §8 |

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện* (kiếp có độ dài và nhịp hợp lý), P2 *Cảm giác tu tiên* (mỗi cảnh giới là một cột mốc lớn), P5 *Thời gian quý giá* (thọ nguyên là áp lực có thật nhưng không phải bản án).

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="loop-r-001"></a>**LOOP-R-001** | **Bảng đường cong mục tiêu §4 là chuẩn.** Mọi tham số tiến trình ở GD khác (yêu cầu tu vi, tốc độ nguồn, thọ nguyên, giá, phí) phải cho kết quả nằm trong dung sai của bảng này khi chạy Balance Simulator (§8). |
| <a id="loop-r-002"></a>**LOOP-R-002** | Không hoạt động đơn lẻ nào được chiếm > 45% tu vi của người chơi điển hình (đa dạng hoạt động, D-05). |
| <a id="loop-r-003"></a>**LOOP-R-003** | Người chơi điển hình **không** chết vì hết thọ trước khi chạm bình cảnh của cảnh giới lớn hiện tại, nếu chơi bình thường (không bế quan quá mức, không liều mạng liên tục). Cái chết sớm phải đến từ **lựa chọn rủi ro** (hiểm địa, đột phá trong hồi quang, PvP khi là ác nhân). |
| <a id="loop-r-004"></a>**LOOP-R-004** | Mọi thay đổi số liệu tiến trình ở GD khác phải kèm báo cáo simulator (§8) trong PR (TD-10 mẫu PR). |

---

## 2. Vòng lặp theo thang thời gian

```
PHÚT (combat · khám phá)
  di chuyển ─ phát hiện (mắt / thần thức) ─ đánh quái / hái thuốc / tranh đoạt ─ nhặt ─ cơ duyên bất chợt
        │
PHIÊN 1–2 GIỜ (một "chuyến đi")
  nhận nhiệm vụ tông môn ─ ra dã ngoại theo canh giờ / thời tiết ─ đầy túi ─ về thành / động phủ:
  luyện đan · rèn · bán · đả tọa · (Beta) lai thú ─ (tùy chọn) 1 lượt hiểm địa / bí cảnh
        │
NGÀY
  3 nhiệm vụ tông môn (300 cống hiến) ─ trần mệt mỏi tu vi quái 24h ─ 1 bế quan offline (tùy chọn) ─ Linh Điền thu hoạch ─ khảo hạch nghề (3 lượt)
        │
TUẦN
  nhiệm vụ tông tự lập (merit, uy vọng) ─ phát hành nội dung thứ Tư ─ (Beta) Sơn Môn Đại Hội, sự kiện thế giới
        │
KIẾP (40–80 giờ chơi tới bình cảnh TC4 / Kim Đan — xem §4, §7 C1)
  tạo nhân vật (thiên phú) ─ Luyện Khí ─ chuẩn bị Trúc Cơ (hiểm địa nhất giai + luyện đan) ─ đột phá ─ Trúc Cơ
  ─ chuẩn bị Kim Đan (nhị / tam giai, Beta) ─ … ─ kết thúc: tọa hóa / chết thật / binh giải
        │
TÀI KHOẢN (nhiều kiếp)
  điểm Luân Hồi → unlock ─ Tàng Bảo Các ─ Di Tàng + tàn hồn ─ ký ức nghề ─ động phủ / linh thú qua kiếp ─ fog / codex ─ Bia Vạn Cổ
```

---

## 3. Mô hình thời gian

```
năm tuổi tiêu mỗi giờ phiên  y_h = shareActive × 1,0 + shareTown × 0,25          (GD-26 LC-R-030, TimeFlowState)
giả định phiên điển hình:    shareActive = 0,75 · shareTown = 0,25  →  y_h ≈ 0,81 năm / giờ
tuổi(t) = 16 + 0,81 × t + Σ chi phí thọ (trọng thương, đột phá thất bại, chế tác, bế quan, bí pháp)
thọ còn lại = lifespanMax(realm) − tuổi
```
Giả định "người chơi điển hình": `cultivationSpeed = 1,0`, không thiên phú thọ, chơi 2–3 giờ / ngày. Thông số này là **đầu vào chuẩn** của simulator.

---

## 4. Đường cong tiến trình mục tiêu (chuẩn — LOOP-R-001)

Tốc độ tu vi tham chiếu lấy từ GD-04 §6.2 (LK ~600 / giờ · TC ~1.200 · KD ~2.400) và tổng yêu cầu (LK 9.880 · TC 38.500 · KD 190.000). Thời gian **chuẩn bị đột phá** (gom nguyên liệu, luyện đan, cống hiến) là giả định, simulator phải xác nhận.

| Mốc | Giờ chơi tích lũy (điển hình) | Chăm (×0,8) | Thư thả (×1,3) | Tuổi ước tính (điển hình) | Thọ tối đa | Thọ còn lại |
|-----|:---:|:---:|:---:|:---:|:---:|:---:|
| LK9 (bình cảnh) | ~16,5 | ~13 | ~21 | ~33 (29 + ~4 năm chi phí: 2–3 trọng thương, chế tác) | 120 | ~87 |
| Chuẩn bị Trúc Cơ (HD01 + luyện đan cấp 3 / mua đan phương) | +4 → ~20,5 | ~17 | ~27 | ~37 | 120 | ~83 |
| TC1 | ~21 (kỳ vọng ~1,6 lần đột phá, +~3 năm thất bại) | ~17 | ~28 | ~40 | 250 | ~210 |
| TC4 (bình cảnh) | ~53 | ~42 | ~69 | ~68 | 250 | ~182 |
| KD1 (cần Kết Kim Đan — Vực 2 / tam giai, **Beta**) | ~61 | ~49 | ~80 | ~80 (+~15–25 năm thất bại / chuẩn bị) | 500 | ~420 |
| KD4 (bình cảnh) | ~140 | ~112 | ~182 | ~145 | 500 | ~355 |

**Đọc bảng:**
- **"Kiếp đầu 40–80 giờ, đạt Trúc Cơ – Kim Đan" (D-11) đúng nếu hiểu là *thời gian để đạt mốc*:** người chơi điển hình tới TC4 khoảng 53 giờ, vào KD1 khoảng 61 giờ.
- **Không đúng nếu hiểu là *độ dài kiếp*:** với thọ Trúc Cơ 250 năm, người chơi không gặp rủi ro có thể sống thêm ~180 năm ≈ **~225 giờ chơi** sau TC4 (§7 C1).

---

## 5. Phân bổ nguồn tu vi

### 5.1. Tỷ trọng mục tiêu hiện tại (GD-04 §2) và khả năng đạt được

| Nguồn | GD-04 mục tiêu | Ước tính theo công thức hiện có (LK, người chơi điển hình) | Nhận xét |
|-------|:---:|:---:|----------|
| Đan dược | 35–40% | ~25–30% sau 8 giờ (giảm vì đan độc) | §7 C3 |
| Đả tọa / bế quan | 20–25% | **~5–8%** | §7 C4 |
| Thiên tài địa bảo, cơ duyên | 15–20% | 15–20% (phụ thuộc tần suất GD-22) | Chưa kiểm được, cần simulator |
| Đánh quái | ~15% | **~25–40%** với người chơi 2–3 giờ / ngày (trần mệt mỏi 24h ít chạm) | §7 C5 |
| Nhiệm vụ | ~5% | ~5–10% | Ổn |

### 5.2. Tỷ trọng đề xuất (chờ duyệt, xem §7)
- <a id="loop-r-010"></a>**LOOP-R-010** Tỷ trọng mục tiêu đề xuất cho người chơi điển hình ở **Luyện Khí**: đan dược 30–35% · đánh quái 20–25% · thiên tài địa bảo + cơ duyên 15–20% · nhiệm vụ 10% · đả tọa / bế quan 10–15%. Ở **Trúc Cơ / Kim Đan**: đan dược tăng lên 35–40% (đan đột phá, nguyên liệu hiểm địa), đánh quái giảm còn 15%. Mọi nguồn ≤ 45% (LOOP-R-002).

### 5.3. Ví dụ tính (Luyện Khí, 16,5 giờ, 9.880 tu vi)
- **Đan:** Tụ Khí Đan Trung phẩm `exp` 150, độc 8 / viên (GD-04 §6.5). Đạt 35% (~3.460 tu vi) cần ~23 viên trong 16,5 giờ ≈ 1,4 viên / giờ → đan độc +11,2 / giờ, phân rã −4 / giờ (`toxDecayPerRealHour`) → **tăng ròng +7,2 / giờ**. Sau 8 giờ chơi liên tục đan độc ≈ 58 → hiệu quả còn `1 − 0,6 × 0,58 = 0,65`. Muốn giữ 35% phải dùng Giải Độc Đan hoặc giãn giờ chơi qua ngày (phân rã tính theo giờ thực, kể cả offline).
- **Đả tọa:** 3% yêu cầu tầng / giờ × linh mạch 1,5 = 4,5% / giờ đả tọa. LK5 (880): 40 tu vi / giờ đả tọa ≈ 7% của 600. Đạt 20% cần đả tọa **~3 giờ mỗi giờ chơi** — không khả thi.
- **Bế quan:** 10 năm tại linh mạch = 30% yêu cầu tầng (LK6 1.150 → 345 tu vi), đổi 10 năm thọ ≈ 12 giờ chơi tương đương. Hợp lý như **tiện ích cho người bận**, không phải nguồn chính.

---

## 6. Kinh tế theo thời gian

| Dải | IPH (GD-13) | Chi phí lớn đặc trưng | Giờ thu nhập tương đương |
|-----|:---:|-----------------------|:---:|
| LK_EARLY (LK1–3) | 100 | Bộ trang bị Phàm đầu tiên ~ (giá trị GD-13 §6.2, ~5 món) | ~3–4 giờ |
| LK_MID (LK4–6) | 250 | Ngưng Cơ Thảo (HD01) 900; phí vào hiểm địa | ~3,6 giờ / cây |
| LK_LATE (LK7–9) | 450 | **Trúc Cơ Đan** 3.000 (giá trị) **hoặc** đan phương ở Tạp Hóa 3.000 (`LK7`) **hoặc** 600 cống hiến (2 ngày nhiệm vụ) | ~6,7 giờ |
| TC | 1.200 | Đan lô / búa T2, ngọc giản Huyền, phí chế công pháp Huyền 100 | — |
| KD | 3.500 | Kết Kim Đan, Diên Thọ Đan (2.000 giá trị) | — |

- <a id="loop-r-020"></a>**LOOP-R-020** Người chơi điển hình phải **tự đủ** chi phí đột phá Trúc Cơ (đan phương + nguyên liệu + phí luyện) trong thời gian chuẩn bị ~4 giờ + thu nhập tích lũy từ LK. Kiểm chứng: ví cuối LK ước tính ≈ Σ(IPH × giờ) × (1 − tỷ lệ chi tiêu 0,6) ≈ (100×4 + 250×6 + 450×6,5) × 0,4 ≈ **~1.930** linh thạch — **chưa đủ** mua đan phương 3.000 ở Tạp Hóa; đường hợp lý là **cống hiến tông NPC (600)** hoặc **tự luyện** (cần luyện đan cấp 3). Xem §7 C6.
- Trần linh thạch chuyển kiếp 5.000 = 20 giờ IPH LK_MID (GD-13 EC-R-060) — khớp.

---

## 7. Mâu thuẫn & điểm cần sửa (phần quan trọng nhất)

| Mã | Vấn đề | Số liệu & nguồn | Đề xuất |
|----|--------|-----------------|---------|
| **C1** | **"Kiếp đầu kéo dài 40–80 giờ" mâu thuẫn với thọ nguyên.** Người chơi tới TC4 ở ~53 giờ, tuổi ~68, thọ Trúc Cơ 250 → còn ~182 năm ≈ ~225 giờ nếu không chết vì rủi ro. | PLAN §4.2 dòng "kiếp đầu… kéo dài 40–80 giờ", D-11; GD-26 `realmLifespanYears` TC 250 / KD 500; GD-26 timeflow | **Đề xuất A (khuyến nghị):** diễn đạt lại D-11 thành *"đạt Trúc Cơ – Kim Đan trong 40–80 giờ"*; độ dài kiếp phụ thuộc rủi ro người chơi chọn (LOOP-R-003). **Phương án B:** giảm thọ (TC 150, KD 300) hoặc tăng `y_h` — làm áp lực thọ rõ hơn nhưng ép kiếp ngắn. Cần quyết định. |
| **C2** | **Khoảng trống nội dung ở TC4 trong MVP / Alpha.** Kết Kim Đan cần nguyên liệu Vực 2 / hiểm địa tam giai (**Beta**), nhưng PLAN Phase 3 ghi "Kim Đan (bản giới hạn)" và người chơi Alpha chạm TC4 ở ~53 giờ, còn ~180 năm thọ mà **không có đường lên**. | GD-04 `breakthroughs.json` (`ITM_KET_KIM_DAN`), GD-10 §16 (Kết Kim Đan Vực 2 Beta), GD-28 hiểm địa tam giai, PLAN Phase 3 | Hoặc (a) đưa 1 nguồn Kết Kim Đan giới hạn vào Vực 1 cho Alpha (HD02 nhị giai, tỷ lệ thấp), hoặc (b) chấp nhận TC4 là trần Alpha và bổ sung nội dung "trần" (khảo hạch, chế công pháp Huyền, linh thú Beta…) + ghi rõ ở PLAN. |
| **C3** | **Tỷ trọng đan dược 35–40% khó giữ vì đan độc** ở người chơi chơi dài liên tục. | GD-04 §2 (35–40%), §6.5 (Tụ Khí Đan `exp` 150, độc 8), `toxDecayPerRealHour` 4 | Simulator kiểm; phương án: `toxDecayPerRealHour` 6–8, **hoặc** thêm Tụ Khí Đan cao cấp cho LK7–9 (`exp` theo tầng), **hoặc** hạ mục tiêu đan còn 30–35% (LOOP-R-010). |
| **C4** | **Tỷ trọng đả tọa / bế quan 20–25% không đạt được**: đả tọa ~7%/h, bế quan đổi thọ kém hiệu quả. | GD-04 §2, §6.3 (`meditationExpPctPerHour` 0,03), §6.4 (`seclusionExpPctPerYear` 0,02) | **Khuyến nghị:** hạ mục tiêu còn 10–15% (đả tọa là hoạt động "nghỉ" giữa chuyến đi), **hoặc** tăng `meditationExpPctPerHour` lên ~0,06 tại linh mạch (cẩn thận AFK-farm — GD-26 AFK). |
| **C5** | **Đánh quái vượt 15%** với người chơi 2–3 giờ / ngày: trần mệt mỏi 24h (0,3R / 0,6R) ít khi chạm trong phiên ngắn. | GD-04 §6.7 (`monsterFatigue`), GD-12 `baseExp` (vd. LK6 = 6), GD-13 §6.4 (~80 quái / giờ) | Chấp nhận 20–25% ở LK (LOOP-R-010); hoặc tính mệt mỏi theo **giờ chơi** thay vì 24h thực. Simulator quyết. |
| **C6** *(đã chốt: hạ về 2.000)* | **Đường mua đan phương Trúc Cơ bằng linh thạch quá đắt** so với ví cuối LK. | GD-13 Tạp Hóa 3.000 (`LK7`), §6 ví ~1.930 | Hạ giá Tạp Hóa về ~2.000, hoặc ghi rõ đường chính là cống hiến (600) / tự luyện; đường linh thạch là đường dự phòng chậm. |
| **C7** | GD-04 "tốc độ tham chiếu LK ~600 / giờ" giả định đủ mọi nguồn theo tỷ trọng §2 — nhưng C3–C5 cho thấy tỷ trọng thực khác. Tổng có thể vẫn ~600 nhưng **cơ cấu khác**. | GD-04 §6.2 | Simulator xuất cơ cấu thực; cập nhật GD-04 §2 theo LOOP-R-010 sau khi duyệt. |
| **C8** | GD-26 §6.5 lặp lại công thức bế quan của GD-04 §6.4 (đã ghi "chuẩn ở GD-04") — **khớp**, không phải mâu thuẫn; chỉ nhắc giữ một nguồn. | GD-26 §6.5, GD-04 §6.4 | Giữ nguyên tham chiếu. |
| **C9** | Linh thạch quái Kim Đan dùng `majorScale` 2,88 (GD-13, GD-06 v0.2) còn IPH KD 3.500 / TC 1.200 = 2,92 — **khớp**. | GD-06 §6.2, GD-13 §6.3 | — |

**Câu hỏi cần quyết (gom từ §7):** C1 (định nghĩa D-11), C2 (trần Alpha), C4 (tỷ trọng đả tọa). C3, C5, C6 chốt bằng simulator.

---

## 8. Balance Simulator (PLAN Phase 3)

### 8.1. Phạm vi
Mô phỏng **một kiếp** của nhân vật ảo theo bước 1 phút chơi (và bước 1 giờ thực cho phân rã đan độc / cây Linh Điền / reset ngày), dùng **đúng gamedata** (bundle server, TD-07) và **đúng công thức** trong `@tienlo/sim` / bản Java tham chiếu (không viết lại công thức — TD-07 DT-R-111).

### 8.2. Đầu vào
| Nhóm | Tham số |
|------|---------|
| Hồ sơ người chơi | giờ / ngày (1,5 · 3 · 6), số ngày / tuần, tỷ lệ thời gian theo hoạt động (quái / thu thập / nhiệm vụ / chế tác / hiểm địa / đả tọa), độ chịu rủi ro (số lượt hiểm địa, đột phá khi tỷ lệ ≥ X%), kỹ năng (tỷ lệ né đòn → trọng thương, điểm minigame trung bình) |
| Nhân vật | linh căn, thiên phú (bảng GD-27), xuất thân, tông môn / gói phúc lợi |
| Thế giới | tần suất cơ duyên (GD-22), bảng rơi (GD-12), EV + xác suất chết hiểm địa (GD-28), giá NPC / đấu giá giả lập (GD-13) |
| Hạt giống | seed RNG (tái lập — TD-01 §9) |

### 8.3. Đầu ra (mỗi hồ sơ, Monte Carlo N = 1.000 kiếp)
- Giờ chơi + tuổi tại từng mốc §4 (p10 / p50 / p90), so dung sai ±20% (LOOP-R-001).
- Cơ cấu nguồn tu vi thực (so LOOP-R-010), tu vi lãng phí ở bình cảnh, thời gian kẹt bình cảnh.
- Đan độc theo thời gian; số Giải Độc Đan dùng.
- Ví linh thạch theo thời gian, chi tiêu theo hạng mục, có tự đủ đột phá không (LOOP-R-020).
- Phân bố kết thúc kiếp: tọa hóa / chết hiểm địa / binh giải / chết PvP (chỉ trọng thương) — % chết trước TC1, trước TC4.
- Điểm Luân Hồi kiếm được mỗi kiếp.
- Cảnh báo vi phạm bất biến (LOOP-R-002, -003).

### 8.4. Vận hành
- <a id="loop-r-030"></a>**LOOP-R-030** Simulator chạy trong CI **mỗi đêm** và **bắt buộc** cho PR đổi gamedata thuộc các domain `cultivation`, `lifecycle`, `alchemy`, `forging`, `economy`, `monsters`, `encounters`, `danger_zones`; xuất báo cáo HTML + JSON; vượt dung sai → PR bị đánh dấu "cần duyệt cân bằng".
- Mục tiêu và dung sai đọc từ `shared/gamedata/balance/targets.json` (§10).

---

## 9. KPI tiến trình (cho GD-20)

| KPI | Mục tiêu (Alpha) |
|-----|------------------|
| Giờ chơi tới LK9 / TC1 / TC4 (p50) | theo §4, lệch ≤ ±25% |
| Tỷ lệ nhân vật kẹt bình cảnh > 5 giờ chơi | < 30% |
| Cơ cấu nguồn tu vi thực | theo LOOP-R-010, mỗi nguồn ±10 điểm % |
| % kiếp kết thúc trước TC1 (không tính chủ động bỏ) | 20–40% (rủi ro có thật nhưng không tàn nhẫn) |
| Thọ còn lại khi chạm TC4 (p50) | ≥ 100 năm |
| % người chơi bắt đầu kiếp mới trong 24h sau khi chết | ≥ 50% (PLAN Phase 4 KPI sống còn) |
| Ví p50 theo dải so với `IPH × 10` | trong ±50% (GD-13) |

---

## 10. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/balance/targets.json` | Đường cong mục tiêu §4, tỷ trọng LOOP-R-010, KPI §9, dung sai — **chỉ** dùng cho simulator và dashboard, không dùng trong gameplay |
| `shared/gamedata/balance/profiles.json` | Hồ sơ người chơi §8.2 |

```jsonc
// targets.json (trích)
{
  "timeModel": { "shareActive": 0.75, "shareTown": 0.25 },
  "milestones": [
    { "realm": "LK9", "hoursP50": 16.5, "ageP50": 33, "tolerance": 0.2 },
    { "realm": "TC1", "hoursP50": 21,   "ageP50": 40, "tolerance": 0.2 },
    { "realm": "TC4", "hoursP50": 53,   "ageP50": 68, "tolerance": 0.2 },
    { "realm": "KD1", "hoursP50": 61,   "ageP50": 80, "tolerance": 0.25, "phase": "BETA" }
  ],
  "sourceShare": {
    "LK": { "PILL": [0.30, 0.35], "MONSTER": [0.20, 0.25], "TREASURE_ENCOUNTER": [0.15, 0.20], "QUEST": [0.08, 0.12], "MEDITATION_SECLUSION": [0.10, 0.15] }
  },
  "maxSingleSourceShare": 0.45,
  "kpi": { "stuckBottleneckHoursPct": 0.30, "deathBeforeTC1": [0.20, 0.40], "lifespanLeftAtTC4MinYears": 100 }
}
```

---

## 11. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="loop-ac-001"></a>LOOP-AC-001 | Gamedata hiện tại, hồ sơ "điển hình" | Simulator 1.000 kiếp | p50 giờ tới TC4 trong 53 ± 20% |
| LOOP-AC-002 | Như trên | Simulator | Không nguồn tu vi nào > 45% (LOOP-R-002) |
| LOOP-AC-003 | Hồ sơ điển hình, không vào hiểm địa sau TC1 | Simulator | Không kiếp nào tọa hóa trước khi chạm TC4 (LOOP-R-003) |
| LOOP-AC-004 | PR đổi `cultivation/params.json` | CI | Simulator chạy, báo cáo đính kèm PR (LOOP-R-030) |
| LOOP-AC-005 | Cùng seed, cùng gamedata | Chạy simulator hai lần | Kết quả giống hệt |
| LOOP-AC-006 | Hồ sơ điển hình đến LK9 | Simulator | Ví đủ cho ít nhất một đường đột phá Trúc Cơ (cống hiến / tự luyện / mua) trong ≤ 6 giờ chuẩn bị (LOOP-R-020) |
| LOOP-AC-007 | Dữ liệu telemetry Alpha | Dashboard GD-20 | Hiển thị đủ KPI §9 so mục tiêu |

---

## 12. Câu hỏi mở
- [x] **LOOP-Q-1** (= §7 C1): D-11 "kiếp đầu 40–80 giờ" là *thời gian đạt Trúc Cơ – Kim Đan* (đề xuất) hay *độ dài kiếp* (phải giảm thọ / tăng tốc trôi tuổi)? *(Đã chốt: sửa D-11 thành thời gian đạt Trúc Cơ – Kim Đan, giữ thọ nguyên GD-26 — 2026-10-25.)*
- [x] **LOOP-Q-2** (= §7 C2): Alpha có nguồn Kết Kim Đan giới hạn ở Vực 1 không, hay TC4 là trần Alpha? *(Đã chốt: **TC4 là trần Alpha**; ~~Kim Đan ở Beta 1~~ → **Kim Đan ở Beta 2 cùng Vực 2** (D-25) — 2026-10-25.)*
- [x] **LOOP-Q-3** (= §7 C4): Hạ tỷ trọng đả tọa / bế quan còn 10–15% hay tăng tốc đả tọa? *(Đã chốt: hạ mục tiêu đả tọa / bế quan còn 10–15% (chỉnh mục tiêu theo thực tế) — 2026-10-25.)*
- [x] **LOOP-Q-4:** Mệt mỏi tu vi quái tính theo 24 giờ thực (hiện tại) hay theo giờ chơi (công bằng hơn với người chơi ít giờ)? *(Đã chốt: giữ mệt mỏi 24h; chấp nhận đánh quái 20–25% trong mục tiêu — 2026-10-25.)*

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: vòng lặp theo 6 thang thời gian, mô hình thời gian (0,81 năm / giờ phiên), **đường cong tiến trình mục tiêu** (LK9 ~16,5h → TC4 ~53h → KD4 ~140h) kèm tuổi / thọ còn lại, phân bổ nguồn tu vi + ví dụ tính, kinh tế theo thời gian, **9 mục đối chiếu (6 mâu thuẫn cần xử lý)**, đặc tả Balance Simulator, KPI cho GD-20, `balance/targets.json`, nghiệm thu |
| v0.2 | 2026-10-25 | — | Chốt LOOP-Q-1..4 và C6 theo quyết định 2026-10-25 |
| v0.3 | 2026-10-25 | — | LOOP-Q-2: Kim Đan ở Beta 2 cùng Vực 2 (D-25) |