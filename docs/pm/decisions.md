# PM-04 — Decision Log

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | PM-04 |
| Owner | Producer / Tech Lead |
| Trạng thái | 🟡 Bản nháp v0.4 |
| Liên quan | [PLAN §11](../../PLAN.md) · [TD-01 TA-R-140](../tech/00-tdd.md) (ADR) · [docs/README.md](../README.md) |

> Nguồn sự thật cho **mọi quyết định đã chốt**. PLAN §11.1 là bản tóm tắt; khi vênh, theo tài liệu này. Quyết định kiến trúc lớn (thêm thành phần, đổi ranh giới, đổi cơ chế vận chuyển) phải có **ADR** (§4) theo TD-01 TA-R-140.

**Quy tắc ghi:**
- Mỗi quyết định một mã `D-nn` tăng dần, **không xóa / không đánh số lại**. Quyết định bị thay → ghi "Thay bởi D-xx" ở cột Trạng thái, giữ dòng cũ.
- Ghi tài liệu bị ảnh hưởng để người sửa biết phải đồng bộ đâu.
- Người duyệt: điền tên khi có quy trình duyệt chính thức (hiện để "—").

---

## 1. Quyết định

| Mã | Ngày | Quyết định | Lý do ngắn | Tài liệu bị ảnh hưởng | Trạng thái | Người duyệt |
|----|------|-----------|-----------|----------------------|-----------|-------------|
| D-01 | 2026-10-19 | Đồ họa **2.5D**: 3D low-poly stylized, camera chéo 45–55°, logic trên mặt phẳng 2D | Giảm tải đồ họa / netcode cho web, tập trung trải nghiệm | PLAN §2.1, §6.1; TD-05, TD-14; GD-24 | ✅ (bổ sung bởi D-22) | — |
| D-02 | 2026-10-19 | **Open world chia bản đồ**: Đại lục → Vực → bản đồ 250–400m, chuyển < 2s, kênh 100–150 người | Web không gánh thế giới liền mạch; nội dung dễ kiểm soát | PLAN §2.2; GD-21; TD-12, TD-13 | ✅ | — |
| D-03 | 2026-10-19 | **Một mạng + thọ nguyên + luân hồi**; chết thật chỉ ở hiểm địa và thiên kiếp Nguyên Anh+, vùng thường là trọng thương | Trụ cột P1 "mỗi kiếp một câu chuyện"; cái chết có ý nghĩa nhưng công bằng | PLAN §4.2; GD-26, GD-28, GD-04 | ✅ (mở rộng bởi D-20, D-21) | — |
| D-04 | 2026-10-19 | **Thiên phú & xuất thân ngẫu nhiên**, roll lại có giới hạn, đổi thiên phú qua sự kiện | Mỗi kiếp khởi đầu khác nhau | PLAN §4.3; GD-27, GD-22 | ✅ | — |
| D-05 | 2026-10-19 | **Tu luyện dựa trên vật phẩm / đan dược**; tu vi đánh quái có giới hạn, bình cảnh, đan độc | Đẩy khám phá / chế tác thay cho cày quái | PLAN §4.4; GD-04, GD-10, GD-12 | ✅ | — |
| D-06 | 2026-10-19 | Kiến trúc: API Service + Game Server, PostgreSQL + Valkey; React cho UI | Tách nguồn sự thật và mô phỏng | PLAN §5; TD-01 | ✅ Sửa bởi D-19, D-24 | — |
| D-07 | 2026-10-19 | **Launch Việt Nam trước, i18n từ đầu** | Thị trường mục tiêu; không phải làm lại chuỗi | TD-07 §6; GD-19 | ✅ | — |
| D-08 | 2026-10-19 | MVP: **preset ngoại hình + đổi màu** | Giảm chi phí mỹ thuật MVP | GD-27; GD-19 | ✅ | — |
| D-09 | 2026-10-19 | **Camera cố định** ở MVP, cân nhắc xoay sau | Frustum nhỏ, dễ tối ưu, dễ đọc combat | GD-24; TD-05 | ✅ | — |
| D-10 | 2026-10-19 | **WASD mặc định**, click-to-move tùy chọn | Combat hành động | GD-24; TD-02 | ✅ | — |
| D-11 | 2026-10-19 (sửa 2026-10-25) | **1 giờ chơi ≈ 1 năm**; người chơi trung bình **đạt Trúc Cơ – Kim Đan trong 40–80 giờ** (độ dài cả kiếp có thể dài hơn) | Thọ nguyên có cảm giác thời gian thật; GD-03 C1 cho thấy kiếp đầu dài hơn mục tiêu cũ | GD-26; GD-03 | ✅ Đã chốt (GD-03 C1) | — |
| D-12 | 2026-10-19 | **1 nhân vật sống / tài khoản / server**, mở thêm ô bằng Luân Hồi | Giữ sức nặng của một mạng | GD-26; GD-19 (ô nhân vật chỉ ở server khác); TD-03 | ✅ | — |
| D-13 | 2026-10-19 | **Bảo hộ tân thủ** kiếp đầu: không chết thật trước Trúc Cơ | Không đuổi người mới | GD-26 | ✅ (PvP không miễn theo cảnh giới — D-21 v0.2) | — |
| D-14 | 2026-10-19 | **Fog of war giữ qua kiếp**, cơ duyên reset | Tiến trình tài khoản, giữ bất ngờ | GD-21, GD-22 | ✅ | — |
| D-15 | 2026-10-19 | Cho phép **tự đặt tên công pháp**, bộ lọc + báo cáo | Sở hữu sáng tạo | GD-08, GD-14 | ✅ | — |
| D-16 | 2026-10-19 | **Không dùng RPG-JS làm nền**; dùng thư viện OSS theo chiến lược §5.7 | RPG-JS 2D tile, buộc đảo ngược nhiều quyết định | PLAN §5.7; TD-15 | ✅ | — |
| D-17 | 2026-10-19 | **Không bán bằng tiền thật** thứ giảm rủi ro chết / kéo dài thọ; cosmetic gắn tài khoản | Công bằng của game một mạng | GD-19, GD-29, GD-13, GD-25 | ✅ | — |
| D-18 | 2026-10-19 | Team thêm **Content Designer**, 3D artist low-poly; roadmap ~15–17 tháng | Open world cần nhiều nội dung | PLAN §3, §7 | ✅ Roadmap giãn lên 21–22 tháng (PLAN v0.40) | — |
| D-19 | 2026-10-22 | **Kiến trúc lai**: client + Game Server bằng TypeScript (Colyseus), code mô phỏng dùng chung `shared/sim`; Spring Boot là nguồn sự thật | Viết mô phỏng một lần; giữ Java cho giao dịch / kiểm toán | PLAN §5; TD-01..TD-06, TD-14 | ✅ (fork T5C bỏ ở D-23) | — |
| D-20 | 2026-10-23 | **Chết không mất trắng**: đồ mang theo rớt; kho đồ / thư / đấu giá phong ấn vào Di Tàng, kiếp sau đánh bại tàn hồn để lấy lại; động phủ mở bằng thú canh cửa | Tránh mất động lực chơi lại | GD-26 §5.15, GD-09, GD-30, GD-25, GD-13; TD-03 §4 | ✅ | — |
| D-21 | 2026-10-23 | **PvP dã ngoại có kiểm soát** (thay nguyên tắc "không open-world PvP"): chế độ PK, sát nghiệp, Chấp Pháp, Thiên Đạo Hóa Thân; chết PvP là trọng thương, người vô tội không mất thọ | Tranh đoạt tài nguyên, giang hồ | GD-32, GD-26, GD-28, GD-14, GD-21; TD-02 | ✅ (v0.2: bỏ miễn theo cảnh giới; `V1_F01` không PvP) | — |
| D-22 | 2026-10-25 | **Giữ 3D phong cách hóa + Babylon.js**; mỹ thuật ưu tiên AI; đa nền tảng cùng codebase (web → PWA → Capacitor) | 3D rẻ hơn 2D cho trang bị / linh thú / ngoại hình; AI 3D nhất quán hơn | PLAN §5.8, §6.1, §6.6, §6.7; TD-01 §17b; TD-05; GD-24, GD-19 | ✅ | — |
| D-23 | 2026-10-25 | **Dựng mới, không fork T5C**; khung từ starter chính thức; T5C chỉ tham khảo | Kiến trúc T5C khác spec; code bằng AI theo spec | PLAN §5.7.6–5.7.8; TD-01, TD-08, TD-15 | ✅ | — |
| D-24 | 2026-10-25 | **Valkey thay Redis** | BSD-3, tương thích, không cần ngoại lệ license | PLAN §5; TD-01, TD-02, TD-03, TD-12, TD-15 | ✅ | — |
| D-25 | 2026-10-25 | **Trần TC4 cho Alpha và Beta 1; Kết Kim Đan ở Beta 2 cùng Vực 2** | Nguyên liệu Kết Kim Đan nằm ở Vực 2 (GD-10); Vực 2 thuộc Beta 2 (PLAN §7) — sửa ghi chú "Kim Đan ở Beta 1" của LOOP-Q-2 | PLAN §7; GD-03, GD-04, GD-10, LR-03 | ✅ | — |
| D-26 | 2026-10-25 | **Mobile lúc launch: chạy được, chưa tối ưu** | Tiết kiệm công UI/QA; nền tảng cảm ứng đã có từ đầu | PLAN §5.8; GD-17, GD-24, TD-05 | ✅ | — |
| D-27 | 2026-10-25 | **Mạch truyện lớn**: phản diện Đoạn Thiên Minh Chủ; Lão Mù là chân linh mang dấu ấn; Luân Hồi Đài do hai người đồng tâm dựng | Director duyệt đề xuất LR-02 / LR-03 | LR-01 WB-Q-3/4; LR-02 CHR-Q-13; LR-03 STY-Q-8 | ✅ | — |
| D-28 | 2026-10-25 | **Asset do team bổ sung; tài liệu chỉ liệt kê yêu cầu** | Không khóa dự án vào một công cụ AI; team chủ động nguồn asset | PLAN §6.6; TD-15 OSS-Q-5; AR-02..06 | ✅ | — |

---

## 2. Câu hỏi đã chốt quan trọng

| Câu hỏi | Kết luận | Nguồn |
|---------|----------|-------|
| Q6 Bế quan offline | 10 năm / lần, 2 năm / giờ offline, hiệu suất 0,8, 1 lần / ngày | GD-26 LC-R-132 |
| Q8 Tên game / thế giới | Tạm: **Tiên Lộ** / **Huyền Hoang Giới** | PLAN §11; LR-01 |
| Q9 Ma Vực | Có, Beta 2 | GD-28 §5.12 |
| Q14 Cơ duyên độc nhất toàn server | Không | GD-22 ENC-Q-1 |
| Q20 Server theo mùa / hòa bình song song | Không ở launch | GD-32 PK-Q-1 |
| Q21 Binh giải | Có, chỉ trong thiên kiếp lethal, ×0,9 | GD-26 §5.16 |
| OSS-Q-1 Redis hay Valkey | Valkey (D-24) | TD-15 |
| TA-Q-1 Fork T5C | Dựng mới (D-23) | TD-01 |
| DT-Q-3 Tách dữ liệu bí mật | Có: `bundle.server` / `bundle.client`, `@serverOnly` | TD-07 DT-R-059, GD-29 §5.4 |
| DT-Q-4 Bản đồ chung `gamedataVersion` | Logic chung, hình ảnh version riêng | TD-13 MF-R-002 |
| SO-Q-3 Thêm tông NPC | Tạc Thiên Bang, Hợp Hoan Tông | GD-14 §5.4b |
| SO-Q-5 Thông báo tọa hóa | Chỉ top 100 Thiên Bảng Chiến Lực | GD-14 §5.4d |
| SO-Q-6 Xuất thân gắn tông | `BG_TAC_THIEN_GANG`, `BG_HOP_HOAN_DISCIPLE` | GD-14 SO-R-04B, GD-27 |
| SO-Q-7 Danh vọng phe | 3 thanh, chính ↔ ma đối nghịch, tà trung lập | GD-14 §5.4c |
| AL-Q-1 Cấp nghề qua kiếp | Ký ức nghề + khảo hạch Sơ / Trung / Cao | GD-10 §5.9 |
| SF-Q-1 Hệ số tăng trưởng cảnh giới | `g`, `J` tăng dần theo cảnh giới | GD-06 §6.2 |
| OW-5 Linh thảo hiếm | Hiển thị chung, tranh đoạt | GD-21 §3.10 |
| PK-Q-1 Server hòa bình | Không | GD-32 |
| PK-Q-4 Thưởng Tạc Thiên Bang hạ Hóa Thân | Chỉ biểu tượng (Tạc Thiên Tôn Giả / Chí Tôn / Đại Đế) | GD-32 PK-R-056 |
| EC-Q-6 Phí giải ấn Di Tàng | Có, theo tầng | GD-13, GD-26 LC-R-162 |
| SB-Q-6 World boss thu phục | Có, top 3 đóng góp / kênh, Beta 2 | GD-23 WE-R-062 |
| CAM-Q-3 Phím ngự kiếm | `N`, camera 30m khi bay | GD-31, GD-24 |

---

## 3. Đang chờ quyết định (theo dõi)
- ~~**LOOP-Q-1..4** (GD-03)~~ — đã chốt 2026-10-25: D-11 sửa; trần Alpha = TC4 (Kim Đan ở Beta 1); tỷ trọng tu vi chỉnh; đan phương Trúc Cơ 2.000.
- **Q10** nhà cung cấp cloud / ngân sách (TD-09 §2.4, §9).
- **Q13, Q15, Q22, Q23** chốt bằng mẫu / spike Phase 0.

---

## 4. Mẫu ADR (`docs/pm/adr/NNNN-ten-ngan.md`)

```markdown
# ADR-NNNN — <Tiêu đề>

| Thuộc tính | Giá trị |
|------------|---------|
| Ngày | YYYY-MM-DD |
| Trạng thái | Đề xuất / Đã duyệt / Thay bởi ADR-xxxx |
| Quyết định liên quan | D-nn |
| Người đề xuất / duyệt | — |

## Bối cảnh
<vấn đề, ràng buộc, số liệu>

## Các phương án
| Phương án | Ưu | Nhược |
|-----------|----|-------|

## Quyết định
<chọn gì, phạm vi>

## Hệ quả
<tài liệu phải sửa, việc phát sinh, rủi ro mới>
```
- Đánh số `NNNN` tăng dần từ `0001`; mỗi ADR liên kết từ dòng `D-nn` tương ứng ở §1.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: chuyển D-01..D-24 từ PLAN §11.1 thành log (ngày, lý do, tài liệu ảnh hưởng, trạng thái), danh sách câu hỏi đã chốt, mục chờ quyết định, mẫu ADR |
| v0.2 | 2026-10-25 | — | D-11 đã chốt theo GD-03 C1; LOOP-Q-1..4 đóng |
| v0.3 | 2026-10-25 | — | D-25 trần cảnh giới theo phase, D-26 mobile lúc launch, D-27 mạch truyện lớn |
| v0.4 | 2026-10-25 | — | D-28 asset do team bổ sung |

