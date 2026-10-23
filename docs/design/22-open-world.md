# GD-21 — Open World & Khám phá

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-21 |
| Owner | Content Designer |
| Trạng thái | 🟡 Bản nháp v0.4 |
| Cần trước | Phase 1 |
| Liên quan | [PLAN.md](../../PLAN.md) §2.2, §4.6, §4.7 · GD-16 Level Design · GD-22 Cơ duyên · GD-23 Sự kiện thế giới · GD-26 Vòng đời nhân vật · GD-28 Hiểm địa · TD-12 World & Map Architecture · TD-13 Map Format |

> **Quy ước:** mọi con số trong tài liệu là **giá trị khởi điểm**, nằm trong gamedata (`shared/gamedata/world/*.json`) và sẽ được cân bằng qua playtest. Không hard-code trong code.

---

## 1. Mục tiêu

**Phục vụ trụ cột:** P5 *Thế giới đáng khám phá* (chính), P1 *Mỗi kiếp một câu chuyện*, P6 *Nhẹ — vào chơi trong 10 giây*.

**Người chơi cần cảm thấy:**
- *"Đi đâu cũng có thứ để tìm."* Không có đoạn đường trống, đi lang thang cũng có phần thưởng.
- *"Thế giới sống và thay đổi."* Giờ giấc, thời tiết, sự kiện làm cùng một bản đồ trở nên khác đi.
- *"Kiến thức là sức mạnh của người tu đạo lâu năm."* Kiếp sau mang theo hiểu biết về thế giới (bản đồ, Vạn Giới Lục), chứ không mang theo chỉ số.
- *"Thần thức của mình ngày càng mạnh."* Cảnh giới cao thì nhìn thấy những thứ người khác không thấy.

**Không phải mục tiêu:**
- Thế giới liền mạch không màn chuyển (đã loại ở PLAN §2.4).
- Leo trèo, bơi lặn, bay tự do theo trục cao độ (logic game là 2D).
- Bản đồ sinh ngẫu nhiên hoàn toàn (procedural). Bản đồ thủ công, chỉ **vị trí** một số nội dung được xáo trộn.

---

## 2. Tổng quan

```
ĐẠI LỤC (Thiên Huyền Giới)
 └── VỰC (region)          = 1 chương cốt truyện, 1 dải cảnh giới, 6–9 bản đồ
      └── BẢN ĐỒ (map)     = 1 MapInstance trên server (× N kênh), 250–400m
           └── KHU (area)  = 5–8 khu con theo chủ đề, mỗi khu ~50–100m
                └── ĐIỂM (POI) = quái, linh thảo, bí mật, NPC, cổng, di tích...
```

Bốn lớp hệ thống chạy trên khung đó:

| Lớp | Hệ thống | Lưu ở đâu |
|-----|----------|-----------|
| Không gian | Bản đồ, kết nối, kênh, truyền tống trận | Gamedata + MapInstance |
| Hiểu biết | Fog of war, Vạn Giới Lục | **Tài khoản** (giữ qua các kiếp, D-14) |
| Cảm tri | Thần thức quét | Thời gian thực (server lọc thực thể) |
| Môi trường | Ngày/đêm (World Clock), thời tiết | Toàn server, phát qua pub/sub |

---

## 3. Quy tắc chi tiết

### 3.1. Cấu trúc thế giới

**Quy ước ID** (dùng trong gamedata, code, log):

| Cấp | Mẫu ID | Ví dụ |
|-----|--------|-------|
| Vực | `V{n}` | `V1` |
| Bản đồ | `V{n}_{loại}{nn}` | `V1_HUB`, `V1_F01` (dã ngoại), `V1_BC01` (bí cảnh), `V1_HD01` (hiểm địa) |
| Khu | `{map}_A{nn}` | `V1_F01_A03` |
| POI | `{map}_{loại}_{nnn}` | `V1_F01_HERB_012`, `V1_F01_TP_001` (truyền tống trận) |

**Loại bản đồ và kích thước chuẩn:**

| Loại | Mã | Kích thước | Kênh | Ghi chú |
|------|----|-----------|------|---------|
| Thành trấn / Tông môn | `HUB` | 150–250m | 100–150 người / kênh | An toàn, tuổi trôi chậm ×0.25 (GD-26) |
| Dã ngoại | `F` | 250–400m (Q15) | 100–150 người / kênh | Nội dung chính của khám phá |
| Hiểm địa | `HD` | 150–300m | Instance theo nhóm hoặc kênh ≤ 30 người | Chết thật (GD-28); fog riêng mỗi lượt |
| Bí cảnh | `BC` | Tuyến tính, 10–15 phút | Instance 1–5 người | Không có fog |
| Động phủ cổ / Kỳ ngộ | `KN` | 30–80m | Instance solo | Sinh ra bởi cơ duyên (GD-22) |
| Động phủ cá nhân | `DP` | 30–60m | Instance riêng (vào từ `CAVE_GATE`) | Beta ([GD-30](28-caves-sect-sites.md)) |
| Trú địa tông môn tự lập | `TD` | 60–120m | Instance riêng (vào từ `SECT_SITE` hoặc cổng thành trấn) | Beta (GD-14, GD-30); `MapType.SECT_GROUNDS` |

**Lưới logic:** 1 ô = 0,5m cho vùng đi được / navmesh; 1 chunk hiển thị = 32×32m (gộp mesh, tải theo chunk); 1 ô fog = 8×8m (§3.5).

### 3.2. Vực 1 — Thanh Vân (phạm vi MVP)

**Dải cảnh giới:** Luyện Khí 1 → Trúc Cơ sơ kỳ. **Chủ đề:** núi rừng thanh tú, thôn trấn phàm nhân, dấu tích cổ chiến trường ở rìa phía bắc.

```
                 [F05 Lạc Hà Cốc]────────[F06 Cổ Chiến Nguyên]══► Cổng Vực 2 (Trúc Cơ)
                   │  ◆BC02                │  ◆BC03   ☠HD02
                   │                       │
 [F01 Thanh Trúc Lâm]──[HUB Thanh Vân Trấn]──[F02 Bích Thủy Hà Bạn]
                   │                       │
                   │                       │
                 [F03 Hoang Thạch Khoáng]──[F04 Vụ Ẩn Trạch]
                      ◆BC01                   ☠HD01

  ── biên nối      ══ cổng vực (khóa cảnh giới)      ◆ bí cảnh      ☠ hiểm địa
```

| ID | Tên | Cảnh giới | Chủ đề / Đặc sản | Nội dung đặc biệt |
|----|-----|-----------|------------------|-------------------|
| `V1_HUB` | Thanh Vân Trấn | — | Thị trấn chân núi, cổng Thanh Vân Tông | Đan phòng, lò rèn, sàn giao dịch, Khâm Thiên Đài (dự báo thời tiết), truyền tống trận trung tâm |
| `V1_F01` | Thanh Trúc Lâm | LK 1–3 | Rừng trúc; linh thảo cơ bản | Bản đồ hướng dẫn; mật độ quái thấp; 1 linh mạch nhỏ |
| `V1_F02` | Bích Thủy Hà Bạn | LK 2–4 | Bờ sông, làng chài; linh thảo hệ Thủy | Linh thảo theo thời tiết mưa; cơ duyên "ngư ông" |
| `V1_F03` | Hoang Thạch Khoáng | LK 3–5 | Mỏ bỏ hoang; khoáng thạch | Cửa **BC01 Phế Khoáng Động** (solo) |
| `V1_F04` | Vụ Ẩn Trạch | LK 4–6 | Đầm lầy **luôn có sương**; linh thảo độc | Cửa **HD01 Vạn Độc Đầm** (hiểm địa nhất giai, LK 5+) |
| `V1_F05` | Lạc Hà Cốc | LK 5–7 | Thung lũng ráng chiều, yêu thú | Cửa **BC02 Hồ Yêu Động** (tổ đội); quái dạ hành |
| `V1_F06` | Cổ Chiến Nguyên | LK 7–9 → TC | Rìa cổ chiến trường, oán linh | Cửa **BC03 Tàn Kiếm Trủng** (tổ đội), **HD02 Cổ Chiến Trường** (nhị giai, Trúc Cơ), **cổng Vực 2** |

**Vòng lặp chính của Vực 1:** HD01 cung cấp nguyên liệu chính cho Trúc Cơ Đan, nên người chơi phải mạo hiểm hiểm địa trước khi đột phá Trúc Cơ (PLAN §4.4). HD02 và cổng Vực 2 chỉ mở sau Trúc Cơ.

### 3.3. Quy tắc nối bản đồ

**Năm loại kết nối:**

| Loại | Mô tả | Ví dụ |
|------|-------|-------|
| **Biên (seam)** | Một đoạn mở 15–30m ở rìa bản đồ, phần còn lại của rìa bị chặn bằng địa hình tự nhiên (vách núi, sông sâu, rừng rậm) | F01 ↔ HUB |
| **Cổng (gate)** | Kết nối qua vật thể: hang động, cầu, sơn môn | F03 ↔ F04 qua hầm mỏ |
| **Cổng vực** | Cổng lớn sang vực khác, **khóa theo cảnh giới**, có cutscene camera ngắn lần đầu | F06 ══► Vực 2 |
| **Truyền tống trận** | Dịch chuyển nhanh giữa các trận đã kích hoạt (§3.5) | Mỗi bản đồ 1 trận |
| **Cửa instance** | Vào bí cảnh / hiểm địa / động phủ cổ; có màn xác nhận | Cửa HD01 |

**Quy tắc thiết kế đồ thị (bắt buộc với mọi vực):**
1. Mỗi bản đồ dã ngoại có **2–4 kết nối** với bản đồ khác (không tính truyền tống trận).
2. Đồ thị của vực có **ít nhất 1 vòng kín** để người chơi không phải quay lại đường cũ (Vực 1: HUB–F02–F04–F03 hoặc HUB–F05–F06–F02).
3. **Chênh lệch cảnh giới** giữa hai bản đồ kề nhau ≤ 2 tầng nhỏ, trừ cổng vực.
4. Từ **bất kỳ điểm đi được nào**, chạy tới truyền tống trận gần nhất mất ≤ 90 giây.
5. Mỗi điểm vào bản đồ có **vùng đệm an toàn bán kính 20m**: không spawn quái, quái không đuổi vào.
6. Biên nối dùng **địa hình báo trước**: con đường, cột mốc, biển chỉ đường, đổi bảng màu. Người chơi luôn biết mình sắp sang bản đồ khác.

**Luồng chuyển bản đồ (mục tiêu < 2 giây, p95):**
1. Client vào vùng **tải trước** (cách biên/cổng 60m), bắt đầu tải nền gói bản đồ kề (GLB + JSON logic).
2. Người chơi chạm vùng chuyển → server kiểm tra điều kiện (§ bên dưới) → handoff (TD-12) → cấp transfer ticket.
3. Client hiện màn chuyển ngắn (mây cuộn kiểu thủy mặc), kết nối `MapInstance` đích.
4. Nhân vật xuất hiện tại **vị trí tương ứng dọc biên đích**, giữ hướng chạy; được bất tử 2 giây hoặc tới khi tự hành động.

**Điều kiện chuyển bản đồ:**
- **Không chuyển khi đang trong trạng thái chiến đấu** (gây hoặc nhận sát thương trong 5 giây gần nhất). Hiện thông báo *"Sát khí chưa tan, không thể rời đi"*. Lý do: chặn việc lợi dụng chuyển bản đồ để thoát quái / thoát chết.
- Cooldown chuyển lại cùng một biên: 3 giây (chống giật qua lại).
- Cổng vực: kiểm tra cảnh giới. Cửa hiểm địa: kiểm tra cảnh giới + màn cảnh báo "Sinh tử tự chịu".

**Chọn kênh khi vào bản đồ (theo thứ tự ưu tiên):**
1. Kênh có thành viên tổ đội.
2. Kênh có bạn bè / đồng môn.
3. Kênh đầy 40–80% (cảm giác đông vui nhưng không chật).
4. Kênh ít người nhất.

Đổi kênh thủ công: cooldown 60 giây, không đổi được khi trong chiến đấu.

**Mất kết nối giữa lúc chuyển:** server giữ ticket 60 giây; quá hạn thì đưa nhân vật về **vùng đệm an toàn của bản đồ cũ**. Không bao giờ để nhân vật "lơ lửng" giữa hai bản đồ.

### 3.4. Chuẩn mật độ nội dung

**Quy tắc 60 giây:** từ bất kỳ điểm đi được nào trên bản đồ dã ngoại, đi theo một hướng bất kỳ trong 60 giây phải gặp **ít nhất 1 thứ tương tác được** (quái, điểm thu thập, NPC, bí mật, di tích, cảnh quan đáng chú ý). Kiểm chứng bằng heatmap telemetry (§9) và công cụ phân tích trong map editor.

**Quy tắc điểm mốc:** mỗi bản đồ có **1 điểm mốc lớn nhìn thấy từ hầu hết vị trí** (tháp cổ, cây cổ thụ, thác nước, cột sáng linh mạch) để người chơi tự định hướng mà không cần mở bản đồ.

**Cấu trúc khu:** mỗi bản đồ dã ngoại chia **5–8 khu**, mỗi khu có tên, chủ đề và độ khó riêng; độ khó tăng dần theo hướng rời xa thành trấn. **55–70% diện tích** là vùng đi được; còn lại là địa hình chặn tạo hành lang và điểm nhìn.

**Checklist mật độ — bản đồ dã ngoại (300×300m chuẩn, quy đổi theo diện tích):**

| Thành phần | Số lượng | Hồi sinh / Làm mới | Ghi chú |
|------------|:--------:|--------------------|---------|
| Khu quái thường | 4–6 | Quái hồi sinh 60–120s | Mỗi khu 1 chủ đề quái |
| Tinh anh / tiểu boss | 1–2 | 20–40 phút, báo trên minimap 1 phút trước | Rơi đồ tốt, tu vi cao |
| Điểm thu thập (linh thảo, khoáng mạch) | 15–30 | 5–15 phút; loại hiếm theo giờ / thời tiết | ≥ 30% số điểm có điều kiện giờ hoặc thời tiết |
| Điểm cơ duyên tiềm năng | 3–5 | Theo GD-22 | Vị trí cố định, nội dung ngẫu nhiên |
| Bí mật (rương ẩn, hang ẩn, câu đố nhỏ) | 2–4 | **Reset mỗi kiếp**; vị trí rút từ tập 6–10 điểm ứng viên | Ẩn cấp 1–3 (§3.7) |
| Bia đá / di tích | 1–2 | Không làm mới | Mở mục Vạn Giới Lục |
| NPC / nhiệm vụ phụ | 3–6 | Theo nhiệm vụ | Có ít nhất 1 NPC chỉ xuất hiện ban đêm |
| Linh mạch | 1 | Cố định | Đả tọa tăng tốc tu luyện |
| Cửa động (`CAVE_GATE`) | 2–4 | Cố định | Lối vào động phủ cá nhân, phẩm địa mạch theo khoảng cách tới linh mạch (GD-30 HM-R-010..011) |
| Địa điểm sơn môn (`SECT_SITE`) | 0–1 | Cố định | Mặt tiền ~40 × 40m, là điểm mốc; không ở bản đồ tân thủ đầu vực (GD-30 HM-R-050..051) |
| Truyền tống trận | 1 | Cố định | Kích hoạt khi chạm vào |
| Cửa bí cảnh / hiểm địa / động phủ cổ | 0–1 | Cố định | |
| Cảnh quan "đáng chụp" | 2–3 | — | Điểm dừng chân, góc nhìn đẹp: phục vụ chia sẻ mạng xã hội |

**Checklist — thành trấn:** truyền tống trận trung tâm, đan phòng, lò rèn, cửa hàng, sàn giao dịch, bảng nhiệm vụ, Khâm Thiên Đài, khu đả tọa, ≥ 2 NPC có cơ duyên, ≥ 1 bí mật.

**Checklist — hiểm địa:** xem [GD-28](27-danger-zones.md) (luôn có ≥ 1 lối thoát mở ở mọi pha, 4 pha áp lực thời gian, bẫy trận pháp, điểm thu thập ẩn, quái lang thang `alertOnScan`).

**Ngân sách quy hoạch Vực 1:** 1 thành trấn + 6 dã ngoại = khoảng **6–8 giờ khám phá lần đầu** (không tính bí cảnh, hiểm địa, cày).

### 3.5. Fog of war & dịch chuyển nhanh

**Cơ chế:**
- Mỗi bản đồ dã ngoại / thành trấn chia lưới **8×8m**. Bản đồ 400m có 50×50 = 2.500 ô, lưu dạng bitmap ≈ 313 byte.
- Ô được mở khi nằm trong **bán kính khám phá** quanh nhân vật: 24m mặc định, cộng thêm khi dùng thần thức quét (mở toàn bộ bán kính quét).
- Server tính từ vị trí thực của nhân vật (client không tự báo ô đã mở), cập nhật mỗi 2 giây, lưu DB mỗi 60 giây và khi chuyển bản đồ.

**Giữ / reset qua các kiếp (D-14):**

| Dữ liệu | Phạm vi | Qua kiếp |
|---------|---------|----------|
| Địa hình đã khám phá (fog) | Tài khoản | ✅ Giữ |
| Truyền tống trận đã kích hoạt | Tài khoản | ✅ Giữ (tiện lợi, không phải sức mạnh) |
| Di tích đã đọc (mục Vạn Giới Lục) | Tài khoản | ✅ Giữ; phần thưởng ngộ tính nhận lại mỗi kiếp (§3.6) |
| Bí mật đã tìm (rương ẩn...) | Nhân vật | ❌ Reset; vị trí rút lại từ tập ứng viên |
| Cơ duyên đã gặp | Nhân vật | ❌ Reset (lịch sử vẫn lưu trong Vạn Giới Lục) |

**Ngoại lệ:** hiểm địa có fog **riêng cho mỗi lượt vào**, không lưu (một số hiểm địa thay đổi bố cục). Bí cảnh không có fog.

**Mốc khám phá (thưởng một lần mỗi tài khoản):**

| Mốc | Thưởng |
|-----|--------|
| 50% một bản đồ | Linh thạch `0,5 × IPH(dải bản đồ)` (GD-13 EC-R-022) + điểm Luân Hồi nhỏ |
| 100% một bản đồ | Linh thạch `1,0 × IPH` + danh hiệu bản đồ + điểm Luân Hồi |
| 100% toàn vực | Danh hiệu vực + mở 1 xuất thân / thiên phú vào pool (GD-27) |

**Truyền tống trận:**
- Kích hoạt bằng cách chạm vào lần đầu. Dịch chuyển tới trận đã kích hoạt bất kỳ trong **cùng vực** (khác vực phải qua trận trung tâm ở thành trấn).
- Phí linh thạch mỗi lần theo dải cảnh giới, khác vực ×2; kiếp đầu dưới `LK4` miễn phí ([GD-13 EC-R-023](19-economy.md)). Sink nhỏ.
- Niệm 3 giây, bị ngắt khi nhận sát thương; không dùng được khi đang chiến đấu hoặc trong hiểm địa / bí cảnh.

**Hiển thị:** vùng chưa khám phá phủ **mây thủy mặc**; vùng đã khám phá hiện địa hình, đường đi, POI cố định. Icon bí mật chỉ hiện sau khi tìm thấy trong kiếp hiện tại.

### 3.6. Vạn Giới Lục (codex)

Bộ bách khoa về thế giới, **gắn với tài khoản và giữ qua mọi kiếp**: kiến thức tích lũy của "người tu đạo nhiều đời". Vạn Giới Lục cho **thông tin và lựa chọn**, không cộng sức mạnh thô (nguyên tắc 3 của PLAN §4.2).

| Mục | Mở khóa theo cấp | Thông tin nhận được |
|-----|------------------|---------------------|
| **Yêu thú** | Gặp → Hạ 10 → Hạ 50 | Cấp 1: hình, tên, nơi sống · Cấp 2: chỉ số, **điểm yếu ngũ hành**, chiêu thức nguy hiểm · Cấp 3: bảng rơi đồ |
| **Linh thảo & khoáng vật** | Thu thập 1 → 20 | Cấp 1: công dụng · Cấp 2: **điều kiện xuất hiện** (giờ, thời tiết, khu) |
| **Địa danh & di tích** | Đặt chân / đọc bia | Lore, lịch sử vùng đất. **Lần đầu đọc mỗi bia trong mỗi kiếp:** +1 ngộ tính hoặc +tâm cảnh nhỏ (có trần mỗi vực) |
| **Cơ duyên** | Gặp lần đầu | Tên, mô tả mờ về điều kiện (*"tương truyền vào đêm mưa..."*), kết cục đã chọn ở các kiếp trước |
| **Nhân vật** | Gặp / hoàn thành chuỗi nhiệm vụ | Tiểu sử NPC, quan hệ thế lực |
| **Tiền kiếp** | Mỗi kiếp kết thúc | Tiểu sử các kiếp của chính mình (liên kết Bia Vạn Cổ, GD-26) |

**Phần thưởng bộ sưu tập** (một lần mỗi tài khoản): hoàn thành một nhóm (vd. "Toàn bộ yêu thú Vực 1 cấp 2") thì nhận điểm Luân Hồi, danh hiệu, cosmetic, hoặc mở thiên phú / xuất thân vào pool.

### 3.7. Thần thức quét

| Thuộc tính | Giá trị khởi điểm |
|-----------|-------------------|
| Phím | G |
| Thời gian niệm | 0,5 giây (đứng yên hoặc đi chậm) |
| Hồi chiêu | 20 giây, giảm theo chỉ số Thần thức (tối thiểu 10 giây) |
| Tiêu hao | 10% Linh lực tối đa |
| Bán kính | Luyện Khí 25m · Trúc Cơ 40m · Kim Đan 60m · +Thần thức × 0,2m |
| Hiệu ứng hình | Sóng tròn lan ra từ nhân vật; vật được phát hiện viền sáng 10 giây |

**Phát hiện được gì:**
- Điểm thu thập trong bán kính, kể cả loại không hiện bằng mắt thường.
- Bí mật (rương ẩn, lối đi ẩn, cơ quan câu đố).
- Quái ẩn thân, phục kích.
- Bẫy trận pháp (trong hiểm địa).
- **Dấu vết cơ duyên:** chỉ hiện một luồng khí tức mờ chỉ hướng, không chỉ chính xác vị trí.

**Độ ẩn (Ẩn cấp 0–5):** mỗi vật thể có một Ẩn cấp. Thần thức quét chỉ phát hiện được nếu **cấp thần thức ≥ Ẩn cấp**. Cấp thần thức (`senseLevel`) định nghĩa ở [GD-27 TLN-R-055](04-talents.md#tln-r-055): `realmOrder` (LK = 1, TC = 2, KĐ = 3...) + 1 nếu Thần thức ≥ 80 + thưởng từ thiên phú (`SENSE_LEVEL_FLAT`). Ví dụ: linh thảo trăm năm Ẩn cấp 1, linh thảo nghìn năm Ẩn cấp 3. Như vậy cảnh giới cao thực sự "nhìn thấy nhiều hơn", và cùng một bản đồ có thêm nội dung khi quay lại ở cảnh giới cao.

**Đánh đổi trong hiểm địa:** quét thần thức có xác suất **kinh động** quái có cảm tri (quái tinh anh / boss lang thang trong bán kính ×1,5 nhận biết vị trí người chơi). Tạo quyết định: quét để tìm bảo vật hay giữ im lặng.

**Chống gian lận:** server **không gửi** thông tin vật thể ẩn xuống client trước khi được phát hiện (bằng mắt thường hoặc thần thức). Client có bị can thiệp cũng không "nhìn xuyên" được.

### 3.8. Ngày/đêm (World Clock)

**1 ngày game = 120 phút thực = 12 canh giờ**, mỗi canh giờ 10 phút thực. Đồng bộ toàn server; mọi bản đồ cùng giờ.

| Canh giờ | Giờ tương ứng | Pha | Thời lượng thực |
|----------|--------------|-----|:---------------:|
| Mão | 5–7h | Bình minh | 10' |
| Thìn, Tỵ, Ngọ, Mùi, Thân | 7–17h | Ngày | 50' |
| Dậu | 17–19h | Hoàng hôn | 10' |
| Tuất, Hợi, Tý, Sửu, Dần | 19–5h | Đêm | 50' |

> **Đồng hồ thế giới tách biệt với tuổi nhân vật** (GD-26: 1 giờ chơi ≈ 1 năm tuổi). Ngày/đêm chỉ ảnh hưởng môi trường.

**Ảnh hưởng gameplay:**

| Hệ thống | Ban ngày | Ban đêm | Giờ đặc biệt |
|----------|----------|---------|--------------|
| Quái | Bình thường | Quái dạ hành (oán linh, ma tu, yêu thú đêm) xuất hiện; một số quái +15% chỉ số | Giờ Tý: âm khí cực thịnh, xuất hiện quái hiếm |
| Linh thảo | Loại dương (Dương Viêm Hoa: chỉ giờ Ngọ) | Loại âm (Nguyệt Quang Thảo) | Bình minh / hoàng hôn: một số loại "giao thời" |
| Cơ duyên | — | Nhiều cơ duyên chỉ có ban đêm | Điều kiện theo canh giờ cụ thể (GD-22) |
| Tu luyện | — | — | **Giờ Tý và giờ Ngọ** (âm dương giao hội): đả tọa +10% hiệu quả |
| NPC | Cửa hàng, dịch vụ luôn mở | Một số NPC chỉ xuất hiện ban đêm (Quỷ Thị: chợ đêm bán hàng hiếm) | |

**Hiển thị:** ánh sáng có hướng + màu ambient đổi theo canh giờ; đèn lồng / cửa sổ sáng về đêm. **Đêm không được tối tới mức khó đọc gameplay:** telegraph, viền quái, vật phẩm vẫn rõ ràng; không giảm tầm nhìn của người chơi.

### 3.9. Thời tiết

**Cơ chế:** mỗi vực có **bảng xác suất thời tiết**; trạng thái đổi mỗi 20–40 phút thực, chuyển tiếp 1–2 phút. Một bản đồ có thể **ghi đè** (vd. Vụ Ẩn Trạch luôn có sương). Thời tiết đồng bộ trong vực, mọi kênh như nhau.

| Thời tiết | Xác suất (Vực 1) | Ảnh hưởng gameplay |
|-----------|:----------------:|--------------------|
| Quang đãng | 45% | Cơ sở |
| Nhiều mây | 20% | Không ảnh hưởng (biến thể hình ảnh) |
| Mưa | 18% | Linh thảo hệ Thủy xuất hiện; công pháp Thủy +10%, Hỏa −10% |
| Giông bão (lôi bạo) | 7% | Công pháp Lôi +15%; cơ duyên hệ Lôi; quái hệ Lôi hoạt động |
| Sương mù | 8% | Bán kính thần thức −30%; quái phục kích; cơ duyên "lạc lối" |
| **Linh vũ** (mưa linh khí) | 2% | Hiếm. Toàn vực: đả tọa +25%, linh thảo làm mới nhanh ×2. Thông báo toàn server |
| Tuyết / Bão cát | 0% | Dành cho các vực sau (Vực 2+, Huyết Sa) |

**Nguyên tắc:** mức ảnh hưởng nhỏ (±10–15%), đủ để người chơi có lý do lên kế hoạch nhưng không khiến ai thấy bị phạt vì thời tiết.

**Dự báo:** NPC **Khâm Thiên Đài** ở thành trấn cho biết thời tiết 1–2 lượt tiếp theo của từng vực; bản đồ thế giới hiện icon thời tiết hiện tại. Người chơi có thể *lên kế hoạch* đi hái linh thảo mưa hay chờ linh vũ.

**Ngân sách hình ảnh:** mưa / tuyết dùng particle GPU, giới hạn số hạt theo mức đồ họa; sương dùng scene fog. Mức đồ họa thấp: chỉ đổi màu ánh sáng + âm thanh, tắt particle.

---

### 3.10. Linh thảo hiếm: tài nguyên chung, tranh đoạt (chốt OW-5)
Linh thảo **thường** (Ẩn cấp 0–1) như cũ: chung, làm mới 5–15 phút. Linh thảo **hiếm** (Ẩn cấp ≥ 2, hoặc phẩm ≥ `BAO`) là **tài nguyên chung của kênh bản đồ** — ai phát hiện và hái trước thì được. "Tranh" bằng **phát hiện, tốc độ, chuẩn bị và sức chiến đấu** (thủ hộ); nếu server bật PvP dã ngoại thì còn bằng đánh nhau, theo luật sát nghiệp ([GD-32](31-pvp-infamy.md)).

- <a id="ow-r-100"></a>**OW-R-100** **Một phiên bản chung mỗi kênh:** mỗi điểm linh thảo hiếm tồn tại một lần trong `MapInstance` (bản đồ × kênh). Hái xong → biến mất với **mọi người** trong kênh đó. (Bỏ quy tắc cũ "hiển thị riêng theo người", §8.)
- <a id="ow-r-101"></a>**OW-R-101** **Thấy được = đã phát hiện:** linh thảo hiếm vẫn theo Ẩn cấp (§3.7): chỉ người đã phát hiện (mắt trong tầm, hoặc quét với `senseLevel ≥` Ẩn cấp) mới nhận thực thể (TD-02 NW-R-042). Thần thức cao = thấy trước người khác — lợi thế chính đáng của build thần thức.
- <a id="ow-r-102"></a>**OW-R-102** **Hái là vận pháp có khóa:** hái linh thảo hiếm vận `rareGatherSec` (4s; thường 1,5s), di chuyển / bị đánh trúng thì hủy. Trong lúc một người đang vận, điểm đó **bị khóa** với người khác (thanh tiến độ hiển thị cho mọi người thấy điểm đó). Hủy → mở khóa ngay; người đó phải chờ `rareGatherRetrySec` (2s) mới thử lại (người khác có thể chen vào — đó là "tranh đoạt"). Nếu server bật PvP (GD-32), người khác có thể đánh ngắt người đang hái — đó là một cách tranh đoạt (theo luật sát nghiệp).
- <a id="ow-r-103"></a>**OW-R-103** **Thủ hộ:** linh thảo Ẩn cấp ≥ 3 hoặc phẩm ≥ `TIEN` có **thủ hộ linh thú** (nhóm spawn `SG_HERB_GUARDIAN_*`, GD-12): xuất hiện khi người chơi đầu tiên tới trong 8m; điểm **không hái được** khi thủ hộ còn sống trong 15m. Ai cũng đánh được thủ hộ (tính công GD-12 EN-R-100, mỗi người nhận phần thưởng quái của mình). Thủ hộ chết → mở hái cho tất cả; ai vận xong trước thì được.
- <a id="ow-r-104"></a>**OW-R-104** **Làm mới và di chuyển:** hái xong, điểm đó tắt `rareRespawnMin` (30–90 phút thực, theo phẩm); khi làm mới, server rút **vị trí mới** trong tập ứng viên của nhóm (`rareHerbGroups`, `serverOnly`, GD-29 RF-R-041) và vẫn phải thỏa điều kiện giờ / thời tiết. Không thể "cắm" một chỗ cố định.
- <a id="ow-r-105"></a>**OW-R-105** **Chống đổi kênh săn linh thảo:** sau khi đổi kênh (thủ công) hoặc vào bản đồ, nhân vật có trạng thái `ST_QI_UNSETTLED` (*Linh khí chưa ổn*) `channelHopGatherLockSec` (120s): **không hái** được linh thảo hiếm (vẫn thấy, vẫn đánh thủ hộ). Cộng với hồi chiêu đổi kênh thủ công 60s, đổi kênh liên tục không còn lợi.
- <a id="ow-r-106"></a>**OW-R-106** **Thiên địa dị tượng:** linh thảo phẩm `TIEN` trở lên khi xuất hiện phát thông báo **trong bản đồ** (mọi kênh, ưu tiên 4, SO-R-060): *"Linh khí hội tụ tại {khu vực}…"* — chỉ tên khu (`{map}_A{nn}`), không lộ tọa độ. Tạo khoảnh khắc nhiều người cùng đổ về tìm — nội dung xã hội tự nhiên.
- <a id="ow-r-107"></a>**OW-R-107** Vật phẩm hái được vào túi người hái (không chia tổ đội, không khóa nhặt). Linh thảo hiếm `bind = NONE` → giao dịch được (GD-13), là hàng hóa có giá vì cung bị giới hạn bởi tranh đoạt.
- <a id="ow-r-108"></a>**OW-R-108** Hiểm địa và bí cảnh **không** áp dụng mục này: linh thảo trong đó theo instance của lượt / tổ (GD-28).

**Message (TD-02):** `C2S_GatherStart { seq, poiId }` · `C2S_GatherCancel { seq }` · `S2C_GatherState { poiId, lockedBy: entityId | null, progress, endsAtMs }` (gửi cho mọi người thấy điểm) · `S2C_GatherResult { poiId, items }` · `S2C_GatherRejected { seq, reason: 'LOCKED' | 'GUARDED' | 'QI_UNSETTLED' | 'GONE' | 'RETRY_WAIT' }`.

**Tiêu chí nghiệm thu:**

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="ow-ac-100"></a>OW-AC-100 | A và B cùng thấy một linh thảo hiếm | A hái xong | Điểm biến mất với cả B; B nhận `S2C_GatherState` báo hết |
| OW-AC-101 | A đang vận hái (2s) | B bắt đầu hái | `S2C_GatherRejected LOCKED` |
| OW-AC-102 | A đang vận, bị quái đánh trúng | Sau đó | Hủy, mở khóa; B hái được ngay; A chờ 2s |
| OW-AC-103 | Linh thảo Ẩn cấp 3 có thủ hộ còn sống | Hái | `GUARDED` |
| OW-AC-104 | Người chơi vừa đổi kênh 30s | Hái linh thảo hiếm | `QI_UNSETTLED` |
| OW-AC-105 | Linh thảo hiếm được hái | Làm mới sau `rareRespawnMin` | Xuất hiện ở vị trí ứng viên (có thể khác chỗ cũ), thỏa điều kiện giờ / thời tiết |
| OW-AC-106 | Người chơi có `senseLevel` 1, linh thảo Ẩn cấp 2 | Đứng cạnh, không quét | Không nhận thực thể |
| OW-AC-107 | Linh thảo phẩm `TIEN` xuất hiện | Thông báo | Mọi kênh của bản đồ nhận tin có tên khu, không có tọa độ |

## 4. Công thức & số liệu

Tất cả tham số nằm trong `shared/gamedata/world/world.json` (toàn cục) và `shared/maps/{mapId}.json` (từng bản đồ, TD-07 DT-R-011).

| Tham số | Giá trị | File |
|---------|---------|------|
| `fog.cellSize` | 8 (m) | world.json |
| `fog.revealRadius` | 24 (m) | world.json |
| `fog.serverUpdateSec` / `persistSec` | 2 / 60 | world.json |
| `transfer.preloadDistance` | 60 (m) | world.json |
| `transfer.combatLockSec` | 5 | world.json |
| `transfer.reenterCooldownSec` | 3 | world.json |
| `transfer.spawnInvulnSec` | 2 | world.json |
| `transfer.ticketTtlSec` | 60 | world.json |
| `safeZone.radius` | 20 (m) | world.json |
| `channel.capacity` | 150 | maps/*.json (ghi đè được) |
| `channel.preferredFill` | 0,4–0,8 | world.json |
| `channel.manualSwitchCooldownSec` | 60 | world.json |
| `gather.rareGatherSec` / `normalGatherSec` | 4 / 1,5 | world.json |
| `gather.rareGatherRetrySec` | 2 | world.json |
| `gather.channelHopGatherLockSec` | 120 | world.json |
| `gather.rareRespawnMin` | theo phẩm: BAO 30 · TIEN 60 · THAN 90 | world.json |
| `gather.guardianTriggerRadius` / `guardianBlockRadius` | 8 / 15 (m) | world.json |
| `rareHerbGroups` (vị trí ứng viên) | theo bản đồ, `serverOnly` | maps/*.json |
| `teleport.castSec` | 3 | world.json |
| `teleportFee` (theo dải), `teleportCrossRegionMult` | GD-13 §7 | economy/params.json |
| `scan.cooldownSec` / `minCooldownSec` | 20 / 10 | world.json |
| `scan.manaCostPct` | 0,10 (phân số, TD-07 DT-R-041) | world.json |
| `scan.radiusByRealm` | LK 25 · TC 40 · KĐ 60 | world.json |
| `scan.radiusPerSense` | 0,2 (m / điểm Thần thức) | world.json |
| `scan.alertRadiusMult` (hiểm địa) | 1,5 | world.json |
| `clock.realMinutesPerDay` | 120 | world.json |
| `weather.durationMin` | 20–40 | world.json |
| `weather.table` | theo vực | world.json |

**Bán kính quét:** `R = radiusByRealm[cảnh giới] + Thần thức × radiusPerSense + Σ SCAN_RADIUS_FLAT` (thiên phú, GD-27 §6.4), nhân 0,7 khi sương mù.

**Hồi chiêu quét:** `CD = max(minCooldownSec, cooldownSec × 100 / (100 + Thần thức))`.

---

## 5. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-26 Vòng đời nhân vật | Tuổi trôi chậm ×0.25 ở `HUB`; fog / truyền tống trận / Vạn Giới Lục giữ qua kiếp; bảo hộ tân thủ khi vào HD01 ở kiếp đầu |
| GD-22 Cơ duyên | Dùng điểm cơ duyên tiềm năng, điều kiện canh giờ / thời tiết / khu; dấu vết cơ duyên hiện qua thần thức |
| GD-23 Sự kiện thế giới | Chạy trên World Clock; dùng bản đồ & kênh làm phạm vi; linh vũ là một sự kiện thời tiết |
| GD-28 Hiểm địa | Cửa hiểm địa, fog riêng mỗi lượt, thần thức kinh động quái |
| GD-10 Luyện đan | Linh thảo theo giờ / thời tiết là nguồn nguyên liệu; Vạn Giới Lục chỉ điều kiện xuất hiện |
| GD-12 Quái & AI | Bảng spawn theo khu và canh giờ; quái dạ hành |
| GD-04 Tu luyện | Linh mạch; đả tọa giờ Tý / Ngọ; linh vũ |
| GD-13 Kinh tế | Phí truyền tống là sink; linh thảo hiếm theo thời tiết tạo biến động giá |
| GD-27 Thiên phú | Thiên phú tăng thần thức / bán kính khám phá; mốc 100% vực mở thiên phú vào pool |
| GD-16 Level Design | Mỗi bản đồ có file riêng theo checklist §3.4 |

---

## 6. UI/UX liên quan

- **Minimap** (góc trên phải): fog, POI đã biết, hướng tới truyền tống trận gần nhất, **đồng hồ canh giờ** (vòng 12 canh, có biểu tượng mặt trời / trăng), icon thời tiết, tên bản đồ + kênh.
- **Bản đồ thế giới** (phím M): 3 mức zoom: Đại lục → Vực → Bản đồ. Hiện % khám phá, truyền tống trận (nhấn để dịch chuyển), thời tiết từng vực, sự kiện thế giới đang diễn ra, đánh dấu tự đặt (tối đa 20).
- **Vạn Giới Lục** (phím J): sổ tay phong cách thư tịch cổ, các tab theo §3.6, thanh tiến độ bộ sưu tập.
- **Thần thức quét:** icon trên thanh kỹ năng có đồng hồ hồi chiêu; danh sách "Phát hiện" ngắn hiện 3 giây bên cạnh minimap.
- **Thông báo:** *"Đã phát hiện: Nguyệt Quang Thảo"*, *"Khám phá Thanh Trúc Lâm 50%"*, *"Linh vũ giáng xuống Vực Thanh Vân!"* (toàn server).
- **Màn chuyển bản đồ:** mây thủy mặc cuộn + tên bản đồ đích bằng thư pháp.

---

## 7. Yêu cầu kỹ thuật

**Dữ liệu bản đồ** (`shared/maps/{mapId}.json`, xuất từ map editor — định dạng chuẩn ở [TD-13](../tech/12-map-editor.md); vận hành room / kênh / chuyển bản đồ ở [TD-12](../tech/11-world-maps.md); bảng DB ở [TD-03](../tech/02-database.md) §6). Ví dụ rút gọn:
```jsonc
{
  "id": "V1_F01",
  "type": "F",
  "region": "V1",
  "size": [300, 300],
  "realmRange": ["LK1", "LK3"],
  "areas": [{ "id": "V1_F01_A01", "name": "Trúc Ảnh Kính", "polygon": [...], "spawnTable": "..." }],
  "connections": [
    { "type": "seam", "to": "V1_HUB", "segment": [[300, 120], [300, 145]], "arrival": "V1_HUB:west" }
  ],
  "safeZones": [{ "center": [290, 132], "radius": 20 }],
  "pois": [{ "id": "V1_F01_HERB_012", "kind": "HERB", "pos": [55, 210], "hiddenLevel": 1,
             "conditions": { "phase": ["NIGHT"], "weather": ["RAIN"] } }],      // định dạng đầy đủ: TD-13 §3
  "secretCandidates": [{ "group": "chest_a", "positions": [[...], [...]] }],
  "landmark": { "pos": [150, 160] },
  "weatherOverride": null
}
```

**Server:**
- **World Clock & Weather Service:** một nguồn duy nhất tính canh giờ và thời tiết từng vực, phát qua Redis pub/sub; `MapInstance` chỉ nhận, không tự tính. Khởi động lại server không làm lệch giờ (tính từ epoch + tham số).
- **Fog:** tính phía server từ vị trí nhân vật; bitmap giữ trong RAM của `MapInstance`, ghi DB theo `persistSec`.
- **Lọc thực thể ẩn** trong interest management: chỉ gửi vật thể có `hiddenLevel` > 0 cho người chơi đã phát hiện.
- **Handoff:** theo TD-12; ticket một lần, TTL 60 giây; kiểm tra combat lock trước khi cấp ticket.
- **Xáo trộn bí mật:** khi nhân vật mới được tạo (kiếp mới), rút vị trí bí mật cho từng nhóm ứng viên bằng RNG server, lưu theo nhân vật.

**Client:**
- Tải trước bản đồ kề theo `preloadDistance`; cache trong IndexedDB theo hash phiên bản.
- Ánh sáng / ambient nội suy theo canh giờ (không giật khi đổi pha).

**Database: thay đổi so với PLAN §5.4:**
- `map_discovery` chuyển sang **theo tài khoản**: `(account_id, map_id, fog_bitmap, discovered_pct, updated_at)`.
- `discovered_poi` tách hai phạm vi: POI bền vững (truyền tống trận, di tích) theo `account_id`; bí mật theo `character_id`.
- `codex_entry` theo tài khoản và có tiến độ: `(account_id, entry_type, entry_id, level, progress, unlocked_at)`.
- Thêm `character_secret_layout (character_id, group_id, position_index)`.

---

## 8. Trường hợp biên & chống lạm dụng

| Tình huống | Xử lý |
|-----------|-------|
| Chuyển bản đồ để thoát quái / thoát chết | Combat lock 5 giây (§3.3) |
| Giật qua lại ở biên | Cooldown 3 giây; vùng đệm an toàn ngăn quái "chờ" ở biên |
| Đổi kênh liên tục để hái linh thảo hiếm | *(Sửa v0.2, OW-5)* Linh thảo hiếm là **tài nguyên chung tranh đoạt** (§3.10); đổi kênh / vào bản đồ bị `ST_QI_UNSETTLED` 120s không hái được (OW-R-105) + hồi chiêu đổi kênh 60s |
| Đứng chờ người khác giết thủ hộ rồi "hớt tay trên" | Cho phép (tranh đoạt), nhưng người đánh thủ hộ đứng gần nhất thường vận hái trước; vận 4s và bị đánh là hủy |
| Linh thảo thường bị người khác hái mất | Chấp nhận (tài nguyên chung) nhưng làm mới nhanh 5–15 phút |
| Bot đi khám phá để lấy thưởng fog | Thưởng mốc chỉ một lần mỗi tài khoản; phát hiện chuyển động kiểu bot |
| Map hack (xem vật ẩn) | Server không gửi vật ẩn chưa phát hiện (§3.7) |
| Farm trong vùng đệm an toàn | Quái không vào vùng đệm, người chơi trong vùng đệm không nhận tu vi từ quái bị kéo tới |
| Mất kết nối giữa lúc chuyển | Ticket TTL 60 giây, quá hạn về vùng đệm bản đồ cũ |
| Kẹt địa hình | Lệnh "Thoát kẹt" (cooldown 10 phút) đưa về vùng đệm gần nhất; log để sửa navmesh |
| Thời tiết hiếm (linh vũ) gây dồn người vào một vực | Mở thêm kênh tự động; phần thưởng linh vũ không độc quyền |

---

## 9. Telemetry cần ghi

| Sự kiện | Trường chính | Dùng để |
|---------|-------------|---------|
| `map_enter` / `map_exit` | map, kênh, từ đâu, cách vào (biên/cổng/truyền tống), thời gian ở lại | Luồng di chuyển, bản đồ bị bỏ qua |
| `map_transfer_time` | ms từ chạm biên tới vào được bản đồ mới | Mục tiêu p95 < 2.000 ms |
| `position_sample` | map, x, z mỗi 10 giây | **Heatmap**: vùng chết (không ai đến), kiểm chứng quy tắc 60 giây |
| `fog_milestone` | map, % | Tốc độ khám phá |
| `scan_used` | map, số vật phát hiện, trong hiểm địa? kinh động? | Cân bằng thần thức |
| `discovery` | loại POI, Ẩn cấp, cách phát hiện (mắt / quét) | Độ khó tìm bí mật |
| `gather` | loại linh thảo, canh giờ, thời tiết | Cân bằng điều kiện xuất hiện |
| `codex_unlock` | mục, cấp | Mức độ tương tác với Vạn Giới Lục |
| `teleport_used` | từ, đến, phí | Kinh tế, điểm nóng |
| `channel_population` | map, kênh, số người (mỗi phút) | Cấu hình sức chứa kênh |
| `stuck_escape` | map, vị trí | Sửa navmesh |

---

## 10. Phạm vi MVP vs sau này

| Hạng mục | MVP (Alpha) | Beta | Sau launch |
|----------|:-----------:|:----:|:----------:|
| Vực 1: 1 thành trấn + 6 dã ngoại, kết nối biên/cổng | ✅ | | |
| Kênh, chọn kênh theo tổ đội / bạn bè | ✅ | | |
| Truyền tống trận, bản đồ thế giới, minimap | ✅ | | |
| Fog of war giữ qua kiếp, mốc khám phá | ✅ | | |
| Vạn Giới Lục: Yêu thú, Linh thảo, Địa danh | ✅ | | |
| Vạn Giới Lục: Cơ duyên, Nhân vật, Tiền kiếp | | ✅ | |
| Thần thức quét + Ẩn cấp | ✅ | | |
| Kinh động quái khi quét trong hiểm địa | | ✅ | |
| Ngày/đêm 12 canh giờ + ảnh hưởng quái / linh thảo / đả tọa | ✅ | | |
| Quỷ Thị (chợ đêm) | | ✅ | |
| Thời tiết: quang đãng, mây, mưa, sương | ✅ | | |
| Thời tiết: giông bão, linh vũ, dự báo Khâm Thiên Đài | | ✅ | |
| Vực 2, cổng vực | | ✅ | |
| Tuyết, bão cát, vực 3+ | | | ✅ |
| Ngự kiếm phi hành trên bản đồ | | ✅ | |

---

## 11. Câu hỏi mở

- [ ] **Q15 (PLAN):** Kích thước dã ngoại chuẩn 250m hay 400m: chốt sau playtest `V1_F01` ở Phase 1. Checklist §3.4 quy đổi theo diện tích.
- [x] **OW-1:** Có cho đổi kênh khi đang ở tổ đội mà các thành viên khác kênh không (tự gom về một kênh)? **→ Đã trả lời ở GD-14 SO-R-014: không tự gom; có nút "Tới kênh đồng đội".**
- [ ] **OW-2:** Phần thưởng ngộ tính khi đọc bia mỗi kiếp: trần bao nhiêu mỗi vực để không thành "nghĩa vụ đi đọc bia" mỗi đầu kiếp?
- [ ] **OW-3:** Ban đêm có nên giảm nhẹ tầm nhìn (bán kính khám phá fog) để tăng không khí hay giữ nguyên vì khả năng đọc gameplay?
- [x] **OW-4:** Truyền tống trận có nên miễn phí trong kiếp đầu (bảo hộ tân thủ) không? **→ Đã trả lời ở GD-13 EC-R-023: miễn phí ở kiếp đầu khi cảnh giới < LK4.**
- [x] **OW-5:** *(Đã chốt 2026-10-23: linh thảo hiếm **hiển thị chung, tranh đoạt** — §3.10: khóa khi đang vận hái, thủ hộ, làm mới đổi vị trí, chống đổi kênh, dị tượng thông báo trong bản đồ.)* Linh thảo hiếm hiển thị riêng theo người có làm giảm cảm giác "thế giới chung" không?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.4 | 2026-10-23 | — | Ví dụ §7 theo định dạng TD-13 (`kind` chữ hoa); trỏ TD-12 / TD-13 / TD-03 |
| v0.3 | 2026-10-23 | — | OW-R-102 / §3.10: tương tác với PvP dã ngoại (GD-32); vùng an toàn quanh truyền tống trận / lối nối dùng cho PvP |
| v0.2 | 2026-10-23 | — | Chốt OW-5: **linh thảo hiếm là tài nguyên chung tranh đoạt** (§3.10, OW-R-100..108): khóa vận hái 4s, thủ hộ linh thú, làm mới đổi vị trí, `ST_QI_UNSETTLED` chống đổi kênh, thông báo dị tượng trong bản đồ; message hái; OW-AC-100..107 |
| v0.1 | 2026-10-19 | — | Bản nháp đầu: cấu trúc thế giới, Vực 1, quy tắc nối bản đồ, chuẩn mật độ, fog of war, Vạn Giới Lục, thần thức quét, ngày/đêm, thời tiết, tham số, kỹ thuật, telemetry, phạm vi |

