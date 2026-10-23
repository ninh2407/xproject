# Tài liệu dự án XProject

Thư mục này chứa toàn bộ tài liệu thiết kế. Kế hoạch tổng nằm ở [PLAN.md](../PLAN.md); danh mục đầy đủ các tài liệu cần viết ở PLAN §8.

## 1. Danh mục tài liệu đã có

| Mã | Tài liệu | Prefix ID | Trạng thái |
|----|----------|-----------|-----------|
| LR-05 | [Thuật ngữ & tên định danh trong code](lore/glossary.md) | — | 🟡 v0.1 |
| GD-04 | [Hệ thống Tu luyện](design/10-cultivation.md) | `CU` | 🟡 v0.3 |
| GD-06 | [Chỉ số & Công thức chiến đấu](design/12-stats-formulas.md) | `SF` | 🟡 v0.3 |
| GD-07 | [Kỹ năng & Công pháp](design/13-skills.md) | `SK` | 🟡 v0.2 |
| GD-08 | [Chế tạo công pháp](design/14-technique-crafting.md) | `TF` | 🟡 v0.1 |
| GD-09 | [Trang bị & Vật phẩm](design/15-items-equipment.md) | `IT` | 🟡 v0.2 |
| GD-10 | [Luyện đan](design/16-alchemy.md) | `AL` | 🟡 v0.2 |
| GD-11 | [Luyện khí](design/17-forging.md) | `FG` | 🟡 v0.2 |
| GD-12 | [Quái vật & AI](design/18-enemies-ai.md) | `EN` | 🟡 v0.1 |
| GD-13 | [Kinh tế](design/19-economy.md) | `EC` | 🟡 v0.3 |
| GD-14 | [Xã hội](design/20-social.md) | `SO` | 🟡 v0.7 |
| GD-15 | [Nhiệm vụ](design/21-quests.md) | `QS` | 🟡 v0.3 |
| GD-28 | [Hiểm địa](design/27-danger-zones.md) | `DZ` | 🟡 v0.3 |
| GD-29 | [Ngẫu nhiên & Công bằng](design/05-rng-fairness.md) | `RF` | 🟡 v0.1 |
| GD-30 | [Động phủ & Sơn môn](design/28-caves-sect-sites.md) | `HM` | 🟡 v0.1 |
| GD-21 | [Open World & Khám phá](design/22-open-world.md) | `OW` | 🟡 v0.4 |
| GD-22 | [Cơ duyên (Kỳ ngộ)](design/23-encounters.md) | `ENC` | 🟡 v0.3 |
| GD-26 | [Vòng đời nhân vật: Thọ nguyên · Cái chết · Luân hồi](design/03-lifecycle.md) | `LC` | 🟡 v0.6 |
| GD-25 | [Linh thú](design/26-spirit-beasts.md) | `SB` | 🟡 v0.3 |
| GD-32 | [PvP dã ngoại & Sát nghiệp](design/31-pvp-infamy.md) | `PK` | 🟡 v0.4 |
| GD-27 | [Thiên phú & Xuất thân](design/04-talents.md) | `TLN` | 🟡 v0.2 |
| TD-01 | [Technical Design Doc: kiến trúc tổng](tech/00-tdd.md) | `TA` | 🟡 v0.2 |
| TD-02 | [Network Protocol](tech/01-network.md) | `NW` | 🟡 v0.3 |
| TD-03 | [Database Schema](tech/02-database.md) | `DB` | 🟡 v0.1 |
| TD-07 | [Gamedata Format](tech/06-gamedata.md) | `DT` | 🟡 v0.3 |
| TD-12 | [World & Map Architecture](tech/11-world-maps.md) | `WM` | 🟡 v0.1 |
| TD-13 | [Map Format & Map Editor](tech/12-map-editor.md) | `MF` | 🟡 v0.1 |
| TD-14 | [Geometry 2D & Collision](tech/13-geom2d.md) | `GEO` | 🟡 v0.1 |

Trạng thái: ⬜ chưa viết · 🟡 bản nháp · 🟢 đã duyệt · 🔵 đã triển khai.

## 2. Quy ước viết spec (bắt buộc với mọi tài liệu GD/TD)

Các tài liệu sẽ được **AI đọc để sinh code**, nên phải cụ thể, không mơ hồ và liên kết được với nhau.

### 2.1. Mã định danh trong tài liệu

| Loại | Mẫu | Ví dụ | Dùng để |
|------|-----|-------|---------|
| Yêu cầu (rule) | `{PREFIX}-R-{nnn}` | `LC-R-012` | Mỗi quy tắc nghiệp vụ một mã; code và test trích dẫn mã này |
| Tiêu chí nghiệm thu | `{PREFIX}-AC-{nnn}` | `ENC-AC-004` | Mỗi tiêu chí viết thành **ít nhất một test tự động** |
| Câu hỏi mở | `{PREFIX}-Q-{n}` | `LC-Q-2` | Chỗ chưa chốt; code phải dùng giá trị mặc định ghi kèm |
| Tham số | `{file}.{khóa}` | `lifecycle/params.json → secPerYear` | Mọi con số đều nằm trong gamedata, **không hard-code** |

Đánh số không dùng lại: khi xóa một yêu cầu, ghi `~~LC-R-007~~ (bỏ ở v0.2)` thay vì đánh số lại.

### 2.2. Tham chiếu chéo
- Giữa tài liệu: `[GD-26 LC-R-012](design/03-lifecycle.md#lc-r-012)` hoặc ngắn gọn `GD-26 LC-R-012`.
- Thuật ngữ: dùng đúng **tên định danh** trong [glossary](lore/glossary.md) (vd. `lifespanMaxYears`, `EndCause.SLAIN_DANGER_ZONE`).
- Tài liệu chưa viết vẫn được tham chiếu bằng mã (vd. GD-27, GD-28); ghi rõ giả định đang dùng.

### 2.3. Cấu trúc một tài liệu hệ thống
Theo template PLAN §8.6, bổ sung các mục bắt buộc cho AI:
1. **Thực thể & kiểu dữ liệu** (dạng TypeScript type: dùng trực tiếp cho client / Game Server, chuyển sang Java cho API Service)
2. **Máy trạng thái** (nếu có)
3. **Quy tắc** đánh mã `-R-`
4. **Công thức** có ví dụ tính cụ thể
5. **Gamedata**: đường dẫn file + JSON mẫu
6. **Hợp đồng giao tiếp**: API nội bộ, message client ↔ server, sự kiện domain
7. **Lưu trữ**: bảng DB, mã lý do ledger
8. **Tiêu chí nghiệm thu** `-AC-` dạng Given / When / Then

### 2.4. Thành phần logic (D-19 đã duyệt)
D-19 (PLAN §5.7.7) đã duyệt: kiến trúc lai. Spec vẫn gọi **tên thành phần logic**; ánh xạ sang công nghệ như bảng dưới:

| Tên logic | Vai trò | Ghi chú |
|-----------|---------|---------|
| **API Service** | Nguồn sự thật cho dữ liệu bền vững: tài khoản, nhân vật, vật phẩm, ledger, vòng đời, RNG quan trọng | Spring Boot (D-06, giữ nguyên trong mọi phương án) |
| **Game Server** | Mô phỏng thời gian thực, chứa các `MapInstance` | Colyseus · Node.js · TypeScript; mỗi room = 1 `MapInstance`; không truy cập DB trực tiếp, gọi API nội bộ của API Service |
| **World Service** | World Clock, thời tiết, lịch sự kiện; phát qua pub/sub | Module của API Service hoặc service riêng |
| **Client** | Babylon.js | Dùng chung `shared/sim` (va chạm 2D, công thức, Ink) với Game Server |

## 3. Quy tắc cho AI khi triển khai từ tài liệu
1. Trước khi code một hệ thống, đọc: [glossary](lore/glossary.md) → [TD-01 TDD](tech/00-tdd.md) (luật chung: quyền hạn, API, idempotency, ledger, RNG, sự kiện) → tài liệu GD của hệ thống → các tài liệu được tham chiếu trong mục "Liên quan".
2. **Không tự thêm** loại dữ liệu, enum, mã lý do, outcome... ngoài những gì spec định nghĩa. Thiếu thì dừng lại và ghi vào mục câu hỏi mở của tài liệu.
3. Mỗi tiêu chí `-AC-` thành ít nhất một test. Tên test chứa mã, vd. `LC_AC_003_offline_does_not_age`.
4. Comment ở chỗ hiện thực một quy tắc phải trích mã, vd. `// LC-R-021: tuổi không trôi khi offline`.
5. Mọi con số đọc từ gamedata. Giá trị mặc định trong spec chỉ để tạo file gamedata ban đầu.
6. Khi spec và code mâu thuẫn: **spec thắng**, trừ khi đã có ADR trong Decision Log ghi khác.
7. Thay đổi hành vi phải sửa spec trước (tăng phiên bản, ghi lịch sử thay đổi), rồi mới sửa code.

