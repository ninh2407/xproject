# PM-03 — Risk Register

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | PM-03 |
| Owner | Producer |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Chu kỳ rà soát | Mỗi sprint (2 tuần) và trước mỗi cột mốc phase |
| Liên quan | [PLAN §10](../../PLAN.md) · [PM-04 Decision Log](decisions.md) · [TD-08](../tech/07-security.md) · [TD-09](../tech/08-infra-ops.md) · [TD-11](../tech/10-performance.md) · [TD-15](../tech/14-oss-register.md) |

> Nguồn sự thật cho rủi ro dự án. PLAN §10 là tóm tắt; khi vênh, theo tài liệu này. Mức: 🔴 Cao · 🟠 Trung bình · 🟡 Thấp–TB. Trạng thái: **Mở** / **Đang giảm** / **Đóng**. Người phụ trách ghi theo vai trò cho tới khi có tên.

## 1. Bảng rủi ro

| Mã | Rủi ro | Mức | Giảm thiểu | Người phụ trách | Trạng thái | Rà soát |
|----|--------|:---:|-----------|-----------------|-----------|---------|
| R1 | **Phình phạm vi** — đã thêm linh thú, ba tông NPC, PvP + Chấp Pháp, Di Tàng, Ma Vực, ngự kiếm, sự kiện thế giới | 🔴 | MoSCoW; tính năng mới thay tính năng cũ hoặc vào "sau launch"; **roadmap đã giãn 21–22 tháng, Beta chia 5a / 5b**; ưu tiên hệ thống cốt lõi ở MVP | Producer | Đang giảm | 2026-10-25 |
| R2 | Combat qua mạng không mượt trên web | 🟠 | Spike netcode Phase 0; bù trễ TD-02 §7; va chạm 2D dùng chung TD-14; vùng cảnh báo | Tech Lead (Client) | Mở | 2026-10-25 |
| R3 | Hiệu năng web kém trên máy yếu / mobile | 🟡 | Ngân sách TD-11, preset chất lượng TD-05, đo mỗi sprint, cổng hiệu năng CI | Tech Lead (Client) | Mở | 2026-10-25 |
| R4 | Khối lượng asset & animation vượt sức team | 🟠 | Bộ xương chung, kit module, asset CC0, **quy trình mỹ thuật AI** (PLAN §6.6, D-22), linh thú dùng lại model quái | Art Director | Đang giảm | 2026-10-25 |
| R5 | Chế tạo công pháp phá cân bằng | 🟠 | Ngân sách sức mạnh GD-07 §6.5, áp chế GD-08, validator duyệt tổ hợp | Game Designer | Đang giảm | 2026-10-25 |
| R6 | Lạm phát / phá kinh tế / nhân bản đồ | 🟠 | Ledger + item log, idempotency TD-01, đối soát hằng ngày, dashboard GD-20; **hố tiền giảm do Di Tàng (D-20) → phí giải ấn (EC-Q-6), mô phỏng GD-03** | Game Designer (Kinh tế) | Mở | 2026-10-25 |
| R7 | Hack / cheat | 🟠 | Server-authoritative, TD-08 §5, rate limit TD-02, không gửi dữ liệu ẩn (GD-29 RF-R-040) | Server Dev | Mở | 2026-10-25 |
| R8 | Thiếu người / nghỉ việc ở vai trò then chốt | 🟠 | Tài liệu spec đầy đủ, code bằng AI theo spec (TD-10), review chéo | Producer | Mở | 2026-10-25 |
| R9 | Không có người chơi khi ra mắt | 🟠 | Cộng đồng từ Phase 1, devlog, alpha sớm | Producer / Marketing | Mở | 2026-10-25 |
| R10 | Bản quyền asset / tên gọi | 🟡 | Thế giới tự sáng tác (LR-01); OSS Register TD-15; **hồ sơ nguồn gốc asset AI** (`art/provenance/`) | Art Director | Mở | 2026-10-25 |
| R11 | Một mạng khiến người chơi bỏ game sau khi chết | 🔴 | Trọng thương ở vùng thường; **Di Tàng + tàn hồn (D-20)**, ký ức nghề (GD-10 §5.9); luân hồi giữ tiến trình; đo KPI quay lại 24h / 7 ngày (GD-20) | Game Designer | Đang giảm | 2026-10-25 |
| R12 | Chết vì lag / lỗi server | 🔴 | Death log + Death Review, quy tức, hoãn xác nhận hấp hối (TD-02 NW-R-051), khôi phục nhân vật duyệt hai người (TD-08), runbook TD-09 | Server Dev | Mở | 2026-10-25 |
| R13 | Ngẫu nhiên gây cảm giác bất công | 🟠 | GD-29: hiển thị tỷ lệ, pity, Thiên Cơ Lục, khí vận không chạm tỷ lệ chính | Game Designer | Đang giảm | 2026-10-25 |
| R14 | Cân bằng thời gian sống khó | 🟠 | Tham số hóa, Balance Simulator (GD-03 §7), telemetry; D-11 đã sửa theo GD-03 C1 (mục tiêu = thời gian đạt TC – KD); kiểm chứng bằng Balance Simulator Phase 3 | Game Designer | Mở | 2026-10-25 |
| R15 | Bot / AFK lợi dụng thời gian & bế quan | 🟡 | Tuổi chỉ trôi khi hoạt động; bế quan offline 1 lần / ngày (LC-R-132); phát hiện bot TD-08 | Server Dev | Đang giảm | 2026-10-25 |
| R16 | Phụ thuộc mã nguồn mở | 🟡 | TD-15, khóa phiên bản, kiểm tra license CI, SBOM | Tech Lead | Đang giảm | 2026-10-25 |
| R17 | Hai ngôn ngữ phía server (TS + Java) | 🟡 | Ranh giới TD-01; code chung tối thiểu + testvectors; contract-first TD-04 (rủi ro fork T5C đã loại ở D-23) | Tech Lead | Đang giảm | 2026-10-25 |
| R18 | **PvP mở gây toxic / quấy người mới** (D-21) | 🔴 | Người vô tội không mất thọ / tu vi; sát nghiệp ×3 khi giết người yếu hơn nhiều; `V1_F01` không PvP; Chấp Pháp; vùng an toàn; phát hiện lạm dụng PvP (TD-08); KPI GD-20 | Game Designer | Mở | 2026-10-25 |
| R19 | **Pháp lý phát hành tại Việt Nam** (giấy phép trò chơi trực tuyến G1, phê duyệt nội dung, thanh toán nội địa, quy định người chưa thành niên) | 🔴 | Tư vấn pháp lý trước Beta; GD-19 hạn mức chi tiêu; nội dung Hợp Hoan Tông theo phân loại độ tuổi (SO-R-04A) | Producer | Mở | 2026-10-25 |
| R20 | **Pháp lý & chất lượng asset AI** (khó bảo hộ bản quyền, license mô hình, chất lượng không đều) | 🟠 | Chỉ dùng mô hình license cho phép (TD-15 §4.5); hồ sơ prompt / nguồn; art director duyệt; khai báo AI khi nền tảng yêu cầu | Art Director | Mở | 2026-10-25 |
| R21 | **Thanh toán kho ứng dụng** (phí Apple / Google, chính sách nội dung, đồng bộ quyền sở hữu web ↔ app) | 🟠 | Xác minh biên lai phía server (TD-08 §8), quyền sở hữu ở API (TA-R-164), GD-19 | Producer / Server Dev | Mở | 2026-10-25 |
| R22 | **Chi phí hạ tầng vượt ngân sách** (Q10 chưa chốt) | 🟠 | Ước tính TD-09 §9, 400 người / tiến trình (TD-11), Valkey managed rẻ hơn, theo dõi chi phí hằng tháng | Tech Lead / Producer | Mở | 2026-10-25 |
| R23 | **Không có đường lên Kim Đan ở Alpha** → người chơi Alpha kẹt TC4 | 🟠 | **Đã quyết (D-25): TC4 là trần Alpha và Beta 1, Kim Đan ở Beta 2 cùng Vực 2**; thông báo rõ trong nội dung Alpha, Kim Đan + Thiên Kiếp ở Beta 1 | Game Designer | Đã xử lý | 2026-10-25 |

## 2. Quy trình
- Rủi ro mới: ai cũng được thêm (mã `R-nn` tiếp theo), Producer xác nhận mức và người phụ trách trong sprint đó.
- Rủi ro 🔴 có kế hoạch giảm thiểu cụ thể + chỉ số theo dõi; báo cáo mỗi cột mốc phase.
- Đóng rủi ro: ghi lý do + ngày; không xóa dòng.

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: chuyển R1..R17 từ PLAN §10, cập nhật theo D-20..D-24 và roadmap giãn; thêm R18 (PvP), R19 (pháp lý VN), R20 (asset AI), R21 (thanh toán kho ứng dụng), R22 (chi phí hạ tầng), R23 (trần Alpha TC4); cột người phụ trách / trạng thái / rà soát |
| v0.2 | 2026-10-25 | — | R14, R23 cập nhật theo quyết định GD-03 |
| v0.3 | 2026-10-25 | — | R23 theo D-25 |

