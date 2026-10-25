<!--
Mẫu báo cáo một buổi playtest — PM-05 (docs/pm/playtests/README.md).
Cách dùng: sao chép thành docs/pm/playtests/YYYY-MM-DD-{phase}-{chủ đề}.md (README §8), thay mọi <…>, xóa dòng hướng dẫn trong ngoặc nhọn.
Luật: ẩn danh P01, P02… — KHÔNG ghi email, số điện thoại, tên tài khoản / nhân vật thật, IP (PT-R-002, TD-08 SEC-R-075).
Mọi tỷ lệ ghi kèm n (PT-R-006). Chỉ số theo định nghĩa GD-20 §7 / GD-18 §8 (PT-R-003).
-->

# PT-<YYYYMMDD> — <Tiêu đề ngắn của buổi>

| Thuộc tính | Giá trị |
|------------|---------|
| Mã báo cáo | PT-<YYYYMMDD>[-n] |
| Trạng thái | Kế hoạch / Hoàn thành / Đã xử lý (PT-R-020) |
| Ngày · giờ (UTC+7) | <YYYY-MM-DD> · <hh:mm>–<hh:mm> |
| Phase | <ph0 / ph1 / ph2 / ph3 / alpha / beta1 / beta2 / ob> (PLAN §7) |
| Loại buổi | <PAPER / STYLE_REVIEW / INTERNAL / FOCUSED / FRIENDS / FTUE_LAB / CLOSED_ALPHA / CLOSED_BETA / OPEN_BETA> (README §2) |
| Chủ trì · ghi chép | <vai trò> · <vai trò> |
| Môi trường | <staging / dev / môi trường Alpha / bàn giấy> (TD-09) |
| Build | <tag hoặc commit ngắn, client + game-server + api-service> |
| `gamedataVersion` | <12 hex> (TD-01 TA-R-150) |
| Bản đồ / nội dung | <vd. `V1_F01`, HD thử, `ENC_V1_FIRST_OMEN`> |
| Thiết bị | <số máy theo `platform` × `deviceClass`, trình duyệt; preset chất lượng> |
| Báo cáo trước liên quan | <PT-… hoặc —> |

---

## 1. Người tham gia (ẩn danh)

| Mã | Nhóm | Kinh nghiệm MMORPG / tiên hiệp | Đã chơi build trước? | Thiết bị | Ghi chú |
|----|------|-------------------------------|:---:|----------|---------|
| P01 | <Team / Ngoài team / Mới hoàn toàn> | <Ít / Vừa / Nhiều> | <Có / Không> | <PC Chrome, laptop văn phòng…> | |
| P02 | | | | | |

- Tổng `n` = <…> (team <…>, ngoài team <…>). Đồng ý thu thập dữ liệu / quay màn hình: <đã có / không áp dụng> (PT-R-010).

---

## 2. Mục tiêu & tiêu chí chốt (đặt **trước** buổi — PT-R-042)

| # | Mục tiêu / câu hỏi | Mã liên quan | Tiêu chí chốt / đạt | Đo bằng |
|:-:|--------------------|--------------|---------------------|---------|
| 1 | <vd. Thời lượng hiểm địa 15' có đủ để vừa khám phá vừa thoát> | <DZ-Q-4> | <≥ 70% người thoát kịp ít nhất 1 lần và PT-S-10 "vừa" ≥ 60%> | <PT-S-09, PT-S-10; quan sát> |
| 2 | <vd. Người chơi muốn chơi tiếp sau 20 phút> | <Cổng Phase 1, PLAN §7> | <PT-S-01 trung vị ≥ 4> | <PT-S-01, PT-S-02> |

---

## 3. Kịch bản

| Bước | Phút | Người chơi làm | Người quan sát chú ý | Gợi ý được phép |
|:----:|:----:|----------------|----------------------|-----------------|
| 1 | 0–5 | <Giới thiệu ≤ 5', không giải thích hệ thống> | | Không |
| 2 | 5–<…> | <…> | <…> | <…> |
| 3 | <…> | Khảo sát (README §5.2, các câu: <PT-S-…>) | | — |
| 4 | <…> | Thảo luận ≤ 15' | | — |

Thay đổi so với kế hoạch: <… hoặc "không">.

---

## 4. Số liệu

### 4.1. Telemetry (định nghĩa GD-20 §7, funnel GD-18 §8)

| Chỉ số | Nguồn định nghĩa | Giá trị | `n` | Mục tiêu tham chiếu | Ghi chú |
|--------|------------------|---------|:---:|---------------------|---------|
| <vd. p50 mở trang → `LK1` (`ob_tutorial_qi_done.ms`)> | GD-18 §8, OB-AC-001 | <… phút> | <…> | ≤ 12' | |
| <vd. Funnel bước 4 / bước 1> | GD-18 §8 | <…%> | <…> | ≥ 65% | |
| <vd. Tỷ lệ chơi lại 24h sau `life_ended`> | GD-20 §7.2 | <…%> | <…> | ≥ 50% (mục tiêu Alpha — chỉ tham khảo, PT-R-006) | |
| <vd. FPS p50 / p5 theo preset> | GD-20 §7.7 | <… / …> | <…> | TD-11 | |

Dashboard / truy vấn: <link Metabase / PostHog hoặc "thủ công">. Khung thời gian lọc: <hh:mm–hh:mm>.

### 4.2. Khảo sát (README §5.2)

| Mã câu | Trung vị | Phân bố (1 / 2 / 3 / 4 / 5) hoặc lựa chọn | `n` |
|--------|:---:|-------------------------------------------|:---:|
| PT-S-01 | <…> | <… / … / … / … / …> | <…> |
| PT-S-04 | <…> (0–10) | <…> | <…> |

### 4.3. Quan sát định lượng thủ công
<vd. số lần chết / trọng thương, số lần thoát hiểm địa kịp, thời gian băng qua bản đồ — ghi theo P01…>

---

## 5. Phát hiện

Mức độ S1–S4 và loại theo README §6. Mã `PT-<YYYYMMDD>-F<nn>`.

| Mã | Mức | Loại | Mô tả (hiện tượng, không phải giải pháp) | Tần suất (`x / n`) | Bằng chứng | Hành động (mã) |
|----|:---:|------|------------------------------------------|:---:|-----------|----------------|
| PT-<YYYYMMDD>-F01 | <S1–S4> | <BUG / FEEL / BALANCE / UX / PERF / CONTENT / ART / LORE> | <…> | <…/…> | <mốc ghi chép, ảnh, truy vấn> | <task PM-02 #… / DZ-Q-4 / R11 / `assetId` AR-05 / chỉ theo dõi> |
| PT-<YYYYMMDD>-F02 | | | | | | |

---

## 6. Trích dẫn

> "<lời người chơi, nguyên văn, đã bỏ mọi thông tin định danh>" — P0<n>, <bối cảnh: bước / phút>

> "<…>" — P0<n>, <…>

---

## 7. Quyết định & hành động

| # | Hành động | Loại | Trỏ tới | Người phụ trách (vai trò) | Hạn | Trạng thái |
|:-:|-----------|------|---------|---------------------------|-----|-----------|
| 1 | <vd. Tăng thời lượng HD01 lên 20' trong gamedata để thử buổi sau> | Gamedata | <`danger-zones/…json` → khóa; DZ-Q-4> | <Game Designer> | <sprint n> | <Mở / Xong> |
| 2 | <vd. Sửa lỗi treo khi chuyển bản đồ> | Bug | <task PM-02 #…> | <Server Dev> | | |
| 3 | <vd. Đề xuất câu hỏi mở mới> | Tài liệu | <GD-xx {PREFIX}-Q-{n+1}> | | | |
| 4 | <vd. Cập nhật mức rủi ro> | Rủi ro | <PM-03 R-nn> | <Producer> | | |

Quyết định đã ghi PM-04: <D-nn / dòng §2 "Câu hỏi đã chốt" / "không có">. Báo cáo không tự sửa `-R-` hay đóng `-Q-` (PT-R-040).

---

## 8. Câu hỏi mở

### 8.1. Đề xuất chốt (cần Producer / owner tài liệu duyệt — README §7)
| Câu hỏi | Kết luận đề xuất | Tiêu chí (mục 2) đạt? | `n` · loại buổi | Tài liệu cần sửa |
|---------|------------------|:---:|-----------------|------------------|
| <vd. DZ-Q-4> | <20' cho nhị giai, giữ 15' cho nhất giai> | <Có / Không / Chưa đủ dữ liệu> | <8 · FOCUSED> | <GD-28 §…; PM-04 §2; gamedata> |

### 8.2. Bằng chứng bổ sung (chưa đủ chốt)
| Câu hỏi | Bằng chứng ghi thêm vào dòng `-Q-` của tài liệu nguồn |
|---------|--------------------------------------------------------|
| <vd. ENC-Q-3> | <"PT-<YYYYMMDD> (n=6): 4/6 thấy cơ duyên hơi thưa ở 2/giờ"> |

### 8.3. Câu hỏi mới phát sinh
- [ ] <Đề xuất `{PREFIX}-Q-{n}` cho GD / TD nào, mặc định đang dùng>

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | <YYYY-MM-DD> | <vai trò> | Kế hoạch buổi |
| v1.0 | <YYYY-MM-DD> | <vai trò> | Hoàn thành báo cáo |

