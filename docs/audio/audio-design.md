# AU-01 — Audio Design

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | AU-01 · Prefix ID: `AU` |
| Owner | Audio Designer (hoặc Tech Artist kiêm) |
| Trạng thái | 🟡 Bản nháp v0.2 |
| Cần trước | Phase 1 (âm combat cơ bản), Phase 3 (nhạc Vực 1 đầy đủ) |
| Liên quan | [LR-01 World Bible](../lore/world-bible.md) · [GD-21](../design/22-open-world.md) (vực, ngày / đêm, thời tiết) · [GD-28](../design/27-danger-zones.md) (pha hiểm địa) · [GD-26](../design/03-lifecycle.md) (chết, tọa hóa, luân hồi) · [GD-06](../design/12-stats-formulas.md) / [GD-07](../design/13-skills.md) (combat) · [GD-10](../design/16-alchemy.md) / [GD-11](../design/17-forging.md) (minigame) · [GD-24](../design/25-camera-controls.md) (trợ năng) · [TD-05](../tech/04-client.md) · [TD-11](../tech/10-performance.md) · [TD-15](../tech/14-oss-register.md) §4.5 |

---

## 1. Mục tiêu & bất biến
- Âm thanh **cổ phong tiên hiệp**, làm nền cảm xúc cho "mỗi kiếp một câu chuyện" (P1); **âm báo gameplay rõ ràng** để combat hành động đọc được bằng tai.

| Mã | Bất biến |
|----|----------|
| <a id="au-r-001"></a>**AU-R-001** | Mọi **cue gameplay bắt buộc** (§5) có âm riêng, dễ phân biệt, **không bị nhạc át** (bus ưu tiên, §6) và có **phụ đề / biểu tượng thay thế** (§7). |
| <a id="au-r-002"></a>**AU-R-002** | Không có yếu tố âm nhạc / âm thanh hiện đại (nhạc điện tử, giọng hát tiếng nước ngoài) trừ khi phối cổ phong (LR-01 WB-R luật giọng văn). |
| <a id="au-r-003"></a>**AU-R-003** | Mọi asset âm thanh có nguồn gốc + license ghi trong TD-15 (§9). |

## 2. Phong cách nhạc
- **Nhạc cụ chủ đạo:** cổ cầm (guqin), tiêu / sáo trúc, tỳ bà, nhị hồ, đàn tranh, chuông đồng, khánh, trống lớn (chiến đấu / boss), tiếng gió / nước làm lớp nền.
- **Bảng cảm xúc:** thanh tĩnh (thành trấn, đả tọa) · phiêu lãng (dã ngoại ban ngày) · u huyền (đêm, sương) · hùng tráng (boss, sự kiện) · bi tráng (tọa hóa, thân tử đạo tiêu) · huyền bí (cơ duyên, Luân Hồi Đài).
- **Chủ đề (leitmotif):** chủ đề chính "Tiên Lộ" (màn hình đầu, luân hồi); chủ đề riêng mỗi đại tông (Thanh Vân: sáo trong trẻo; Tạc Thiên: trống + tỳ bà dồn dập; Hợp Hoan: đàn tranh + tiêu mềm, gợi cảm xúc, không gợi dục — SO-R-04A); chủ đề Thiên Đạo (khánh + bè trầm) cho thiên kiếp / Hóa Thân.

## 3. Nhạc theo ngữ cảnh

| Ngữ cảnh | Quy tắc | Nguồn |
|----------|---------|-------|
| Vực / bản đồ | Mỗi vực một bộ chủ đề; mỗi bản đồ một biến tấu (nhạc cụ / nhịp) | GD-21 §3.2 |
| Ngày / đêm | Lớp nhạc ngày ↔ đêm **trộn chéo 20s** theo canh giờ World Clock, không ngắt | GD-21 §3.8 |
| Thời tiết | Lớp ambience (mưa, gió, sấm, sương) chồng lên nhạc; Linh vũ có lớp chuông lấp lánh | GD-21 §3.9 |
| Chiến đấu | Vào chiến đấu → thêm lớp trống / dây sau 1s; ra chiến đấu 5s → rút dần 4s (nhạc phân lớp, không đổi bài đột ngột) | GD-07 |
| Boss | Bài riêng theo pha (đổi pha = đổi đoạn nhạc theo nhịp) | GD-12 |
| Hiểm địa | Căng dần theo **4 pha áp lực** (GD-28): mỗi pha thêm lớp / tăng nhịp; pha cuối có nhịp tim + trống dồn; hấp hối: lọc âm thấp (muffled) + nhịp tim | GD-28 |
| Cơ duyên | Cue mở đầu huyền bí + nhạc nền nhẹ, ưu tiên lời thoại | GD-22 |
| Thành trấn / động phủ | Thanh tĩnh, có tiếng chợ / chuông xa | GD-30 |
| Đả tọa / bế quan / đột phá | Đả tọa: drone trầm + tiêu; đột phá: dồn dập lên đỉnh, thành công = hợp âm sáng, thất bại = đứt âm | GD-04 |
| Chết / tọa hóa / luân hồi | Trọng thương: âm "đứt" ngắn; **thân tử đạo tiêu**: im lặng 1s rồi cổ cầm bi tráng; **tọa hóa**: khúc tiễn biệt (màn tổng kết kiếp); **luân hồi / kiếp mới**: chủ đề "Tiên Lộ" phiên bản sáng | GD-26 |

### 3.1. Danh mục nhạc nền bản đồ (`BGM_`)
Mã `BGM_{VỰC}_{TÊN}` (TD-07 DT-R-051) là giá trị của `MapDef.ambient.musicId` (TD-13 §3); mỗi mã là một bài phân lớp theo §3 (ngày ↔ đêm trộn chéo 20s, lớp chiến đấu thêm sau 1s). Thêm bản đồ mới: thêm dòng ở đây trước khi dùng trong `MapDef`.

| Mã | Bản đồ dùng | Cảm xúc (§2) | Ghi chú |
|----|-------------|--------------|---------|
| `BGM_V1_FIELD_CALM` | `V1_F01` Thanh Trúc Lâm (GD-16 `V1_F01` §12) | Thanh tĩnh / phiêu lãng ban ngày; đêm u huyền nhẹ | Biến tấu chủ đề Vực 1: sáo trúc + cổ cầm; cutscene mở đầu và cơ duyên Điềm Báo dùng cue cơ duyên (§3), không dùng bài này |

## 4. Danh sách SFX theo hệ thống (MVP)

| Hệ thống | SFX cần có |
|----------|-----------|
| Di chuyển | Bước chân theo mặt đất (đất, đá, cỏ, gỗ, nước); né lướt (vút); khinh công |
| Combat (GD-06 / 07) | Vung kiếm 3 nhịp combo, đánh mạnh tụ lực (tăng dần) + nhả, quạt pháp thuật; trúng thịt / giáp / phép; **chí mạng** (riêng); đỡ / **đỡ hoàn hảo** (riêng, sáng); phá thế; đẩy lùi / ngã; status (bỏng, độc, đóng băng, tê, khống chế); hồi máu; khiên |
| Kỹ năng | Bộ âm theo hệ ngũ hành (8 hệ) × dạng (đạn, vùng, quạt, lướt, triệu hồi) — **ghép tham số** như VFX (GD-08) |
| Quái (GD-12) | Mỗi nhóm bộ xương: gầm khi phát hiện, tấn công, bị đánh, chết; tinh anh / boss có âm riêng |
| Chế tác (GD-10 / 11) | Lửa đan lô (theo độ nóng), nhiễu hỏa khí, đan thành / tạc lô; búa rèn theo nhịp (**âm đánh dấu phách chuẩn**, §5), tôi luyện, dị tượng |
| UI | Mở / đóng bảng, chọn, xác nhận, lỗi, nhận vật phẩm theo phẩm (5 mức), lên cấp / tầng, thông báo hệ thống, tin nhắn |
| Cơ duyên (GD-22) | Tín hiệu cơ duyên (cue), lựa chọn, kết quả tốt / xấu |
| Linh thú (GD-25) | Gọi / thu, chiêu lệnh, ném phù (vận pháp), thu phục thành công / vỡ phù |
| Ngự kiếm (GD-31) | Cất cánh, gió khi bay (lặp), hạ cánh, rơi |
| Sự kiện thế giới (GD-23) | Báo động sự kiện (trống / chuông lớn), bắt đầu / kết thúc, world boss xuất hiện |
| Thế giới | Ambience theo biome; truyền tống trận; mở rương; hái thuốc / khai khoáng |
| PvP (GD-32) | Đổi chế độ PK, bị đánh dấu xám / đỏ, Chấp Pháp xuất hiện |

## 5. Cue gameplay bắt buộc
| Cue | Yêu cầu | Nguồn |
|-----|---------|-------|
| **Cảnh báo đòn quái / boss** | Âm khởi chiêu đồng bộ với vùng cảnh báo; đòn **không thể đỡ** có âm riêng; theo vị trí 2D (pan trái / phải) | GD-12 EN-R-070, GD-06 SF-R-102 |
| **Linh chủng xuất hiện** trong 40m | Chuông nhẹ đặc trưng | GD-25 |
| **Thiên phạt** / Thiên Đạo Hóa Thân sắp tới | Sấm rền tăng dần trong 15s cảnh báo | GD-32 PK-R-055 |
| Chấp Pháp truy lùng | Trống cảnh báo 10s | GD-32 |
| Phách minigame rèn / hỏa hầu | Âm phách sạch, tấn công ngắn; **bù độ trễ âm thanh** theo cài đặt hiệu chỉnh (FG-Q-3) | GD-11, GD-10 |
| Hấp hối / quy tức trong hiểm địa | Nhịp tim + lọc âm | GD-28 |
| Thọ nguyên cảnh báo (Đại hạn, Hồi quang phản chiếu) | Chuông chậm | GD-26 |
| Đổi pha hiểm địa | Hiệu ứng chuyển pha | GD-28 |

## 6. Trộn âm & kỹ thuật
- **Bus:** Master → Nhạc · Ambience · SFX (Combat / Thế giới) · UI · Lời thoại · **Cue ưu tiên** (không bị duck; các bus khác duck −6 dB khi cue ưu tiên phát).
- **Thư viện:** Babylon.js Audio Engine v2 cho âm không gian theo thực thể; Howler.js cho nhạc / UI nếu cần (PLAN §5.2) — chọn một đường chính ở TD-05, tránh hai engine cùng điều khiển một bus.
- **Định dạng:** Opus trong container Ogg/WebM (ưu tiên), fallback AAC/M4A cho Safari cũ nếu cần; nhạc stream, SFX nạp vào bộ nhớ theo nhóm (preload theo bản đồ).
- **Ngân sách** (số chuẩn ở TD-11): âm thanh nằm trong ngân sách tải lần đầu (≤ 15MB tổng) — chỉ gói SFX lõi + nhạc màn đầu; nhạc vực tải theo bản đồ; số kênh đồng thời tối đa 32 (preset Trung), ưu tiên theo khoảng cách + loại.
- **Không gian:** âm theo vị trí 2D (x, z) quanh người nghe = nhân vật; suy giảm theo khoảng cách AOI (TD-02).

## 7. Trợ năng
- Thanh trượt âm lượng từng bus; **phụ đề / biểu tượng** cho mọi cue §5 (vd. biểu tượng sấm khi Thiên phạt); chỉ báo hướng âm (mũi tên rìa màn hình) cho cảnh báo đòn ngoài màn hình; tùy chọn mono; giảm âm chớp / tần số chói. Đồng bộ với GD-24 §trợ năng.

## 8. Lời thoại
- MVP: **không lồng tiếng toàn bộ**; chỉ tiếng hô / tiếng cảm thán ngắn (nhân vật, boss, NPC chính). Lời thoại hiển thị chữ (i18n TD-07).
- Sau launch: cân nhắc lồng tiếng tiếng Việt cho cảnh chính.

## 9. Nguồn âm thanh & license
- Ưu tiên **CC0** và thư viện có license thương mại rõ ràng; ghi mọi asset vào TD-15.
- **Nhạc / SFX do AI tạo:** chỉ dùng công cụ / mô hình có điều khoản cho phép dùng thương mại output (đánh giá theo TD-15 §4.5 trước khi dùng — không giả định); lưu hồ sơ nguồn gốc (prompt, công cụ, phiên bản) như asset hình ảnh (`art/provenance/`); người duyệt nghe lại toàn bộ trước khi đưa vào game.
- Nhạc chủ đề chính và chủ đề tông môn: ưu tiên thuê nhạc sĩ / mua quyền để có bản quyền rõ.

## 10. Câu hỏi mở
- [ ] **AU-Q-1:** Babylon audio hay Howler.js làm đường chính?
- [ ] **AU-Q-2:** Ngân sách thuê nhạc sĩ cho chủ đề chính + 3 chủ đề tông môn?
- [ ] **AU-Q-3:** Có nhạc riêng khi ngự kiếm / khi PvP (tên đỏ) không?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến, phong cách cổ phong + leitmotif tông môn, nhạc theo ngữ cảnh (vực, ngày / đêm, thời tiết, chiến đấu phân lớp, boss, hiểm địa theo pha, chết / tọa hóa / luân hồi), danh sách SFX theo hệ thống, cue gameplay bắt buộc, trộn âm & ngân sách, trợ năng, lời thoại, nguồn & license |
| v0.2 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: §3.1 danh mục nhạc nền bản đồ `BGM_{VỰC}_{TÊN}` (TD-07 DT-R-051), đăng ký `BGM_V1_FIELD_CALM` |

