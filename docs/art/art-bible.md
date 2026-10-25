# AR-01 — Art Bible

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | AR-01 · Prefix ID: `ART` |
| Owner | Art Director (+ Tech Artist) |
| Trạng thái | 🟡 Bản nháp v0.2 |
| Cần trước | Phase 0 (mẫu phong cách, chốt Q13) — bắt buộc trước sản xuất hàng loạt |
| Quyết định nền | D-01 (2.5D: 3D nhìn chéo, logic 2D), D-09 (camera cố định), **D-22** (3D phong cách hóa + Babylon.js, mỹ thuật ưu tiên AI), PLAN §6.1, §6.6, §6.7 |
| Liên quan | [AR-02 Asset Guidelines](asset-guidelines.md) · [GD-24 Camera](../design/25-camera-controls.md) · [GD-21 Open World](../design/22-open-world.md) §3.2 · [LR-01 World Bible](../lore/world-bible.md) · [GD-06](../design/12-stats-formulas.md) (nguyên tố) · [GD-09](../design/15-items-equipment.md) (phẩm) · [GD-25](../design/26-spirit-beasts.md) (huyết mạch) · [GD-14](../design/20-social.md) §5.4b (ba tông) · [GD-12](../design/18-enemies-ai.md) (quái) · [TD-05](../tech/04-client.md) (shader, preset) · [TD-11](../tech/10-performance.md) · [TD-15](../tech/14-oss-register.md) §4.5 · [AR-04 VFX Guidelines](vfx-guidelines.md) §3 (bảng màu VFX `VPAL_*`) |

**Tài liệu này cung cấp cho:** artist / AI sinh ảnh & model (phong cách, bảng màu, ngôn ngữ hình dạng), TD-05 (tham số shader, bảng màu dùng trong code), GD-24 (màu vùng cảnh báo), AR-02 (quy chuẩn kỹ thuật dựa trên phong cách này).

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="art-r-001"></a>**ART-R-001** | **Đọc được ở góc nhìn game trước khi đẹp ở ảnh tĩnh.** Mọi asset duyệt bằng ảnh chụp **đúng camera game** (nghiêng 50°, khoảng cách 20m, GD-24), không duyệt bằng ảnh cận cảnh. |
| <a id="art-r-002"></a>**ART-R-002** | **Thông tin gameplay thắng thẩm mỹ:** vùng cảnh báo, màu phẩm / nguyên tố / phe, tên đỏ ác nhân luôn dễ nhận hơn mọi trang trí (không VFX, ánh sáng, sương nào được che vùng cảnh báo). |
| <a id="art-r-003"></a>**ART-R-003** | Một **bảng màu toàn cục** (§4) — màu mang nghĩa gameplay (nguyên tố, phẩm, phe, cảnh báo) không được dùng tùy tiện cho trang trí. |
| <a id="art-r-004"></a>**ART-R-004** | Không nội dung gợi dục, máu me chi tiết, biểu tượng tôn giáo / chính trị có thật; trang phục Hợp Hoan Tông theo SO-R-04A. Phân loại độ tuổi mục tiêu: 12+ (chốt khi xin giấy phép). |
| <a id="art-r-005"></a>**ART-R-005** | Mọi asset phát hành có **hồ sơ nguồn gốc** (TD-15 §4.5) và được người duyệt ký (§9). |

---

## 2. Định hướng phong cách (D-22)

**Tiên hiệp cổ phong, 3D phong cách hóa nhìn từ trên cao.** Cảm giác "tranh minh họa sống động", không hiện thực.

| Lớp | Cách làm | Ghi chú |
|-----|----------|---------|
| Môi trường | Low-poly, **atlas màu gradient chung** (một texture cho phần lớn prop), shading toon 2–3 bậc | Rẻ, đồng nhất, tải nhỏ (TD-11) |
| Nhân vật, quái lớn, điểm nhấn (landmark) | **Texture vẽ tay** (≤ 1024², AR-02) + toon + viền | Nơi đầu tư chi tiết |
| Viền | Viền ngoài mảnh, màu tối hơn màu nền (không đen tuyền), độ dày theo khoảng cách | Tách nhân vật khỏi nền |
| Bầu trời, nền xa, mây | **Thủy mặc**: loang mực, giấy dó, mây cuộn | Màn chuyển bản đồ dùng mây thủy mặc (GD-21) |
| Tùy chọn shader thủy mặc toàn cảnh | Hậu xử lý: viền loang, nhiễu giấy, giảm bão hòa vùng xa | Bật ở preset Cao, mặc định tắt ở Thấp (TD-05) |
| VFX | **Phát sáng** (kiếm khí, linh quang, trận pháp) — điểm nhấn thị giác chính của game | Bloom có trần (§6) |

Tham khảo tinh thần: tranh thủy mặc + minh họa tiên hiệp, game nhìn chéo phong cách hóa (vd. Albion Online về khả năng đọc), bầu trời kiểu Sky: Children of the Light. **Không** sao chép asset / nhân vật của game khác.

---

## 3. Thiết kế cho góc nhìn trên cao

- <a id="art-r-010"></a>**ART-R-010** Chi tiết dồn vào **đỉnh đầu, vai, vũ khí, bóng dáng và mặt đất** — những gì camera 50° thấy; mặt nhỏ, không đầu tư chi tiết khuôn mặt (không hoạt ảnh khuôn mặt, PLAN §6.7).
- <a id="art-r-011"></a>**ART-R-011** **Silhouette** nhận được ở 20m: người chơi phân biệt được kiếm / quạt, tông môn (màu chủ đạo + hình mũ / vai), quái thường / tinh anh / boss (kích thước + hào quang).
- <a id="art-r-012"></a>**ART-R-012** Mặt đất chiếm phần lớn màn hình → texture / decal mặt đất phong phú (lối mòn, lá rụng, đá vụn), đổi theo khu.
- **Tỷ lệ nhân vật (Q13 — chốt bằng mẫu ở Phase 0):**

| Phương án | Tỷ lệ đầu / thân | Ưu | Nhược |
|-----------|:---:|-----|------|
| **A. Hơi chibi** (PLAN §6.1) | 1/5 | Dễ nhận diện ở xa, dễ thương, trang phục rõ | Kém "tiên phong đạo cốt" khi cận cảnh |
| **B. Chuẩn phong cách hóa** | 1/6 – 1/6,5 | Uy nghi, hợp boss / tàn hồn | Nhỏ ở camera xa, phải phóng đại vũ khí / vai |

  Mẫu Phase 0 làm cả hai cho cùng một nhân vật, chụp đúng camera game để chọn.

---

## 4. Bảng màu

### 4.1. Theo vực / bản đồ (Vực 1 — Thanh Vân, khớp GD-21 §3.2, LR-01)

| Bản đồ | Chủ đạo | Phụ | Điểm nhấn | Không khí |
|--------|---------|-----|-----------|-----------|
| `V1_HUB` Thanh Vân Trấn | Gỗ ấm, mái ngói xám xanh | Trắng vôi | Đèn lồng đỏ, cờ xanh Thanh Vân | Ấm, an toàn |
| `V1_F01` Thanh Trúc Lâm | Xanh trúc tươi | Vàng nắng xuyên lá | Trắng sương nhẹ | Yên bình, hướng dẫn |
| `V1_F02` Bích Thủy Hà Bạn | Xanh ngọc nước | Cát nhạt, lau sậy | Cam chiều trên mặt nước | Thoáng, mưa nhiều |
| `V1_F03` Hoang Thạch Khoáng | Xám đá, nâu đất | Gỗ giàn mỏ cũ | Lam tinh thể khoáng; đỏ cờ Tạc Thiên Trại | Khô, bụi |
| `V1_F04` Vụ Ẩn Trạch | Lục xám rêu | Xám tím sương (luôn có) | Lục độc phát sáng | Ngột ngạt, mờ |
| `V1_F05` Lạc Hà Cốc | Hồng cam ráng chiều | Tím hoa đào (Hợp Hoan Cốc) | Lửa hồ ly đỏ cam | Mơ màng, nguy hiểm ngầm |
| `V1_F06` Cổ Chiến Nguyên | Xám tro, đỏ sét | Kim loại rỉ | Lam lạnh oán linh | Hoang tàn, nặng nề |
| Hiểm địa (HD01, HD02) | Giảm bão hòa của bản đồ gốc | Đen | Màu nguy hiểm của hiểm địa | "Sinh tử tự chịu" |

### 4.2. Nguyên tố (GD-06) — dùng cho VFX, icon, viền kỹ năng

| Hệ | `paletteId` | Màu chính | Màu sáng (VFX lõi) | Hex chính / lõi / viền tối (khởi điểm) | Motif (hình dạng nhận diện, `VfxMotif`) |
|----|----|-----------|---------------------|----|----|
| Kim `KIM` | `VPAL_KIM` | Vàng kim nhạt | Trắng ngà | `#E8D28A` / `#FFF6E0` / `#8F7535` | Mảnh kim sắc cạnh, vệt chém thẳng (`BLADE_SHARD`) |
| Mộc `MOC` | `VPAL_MOC` | Lục lá | Lục non | `#4FA34A` / `#B8F07A` / `#2B5A28` | Lá non, dây leo xoắn (`LEAF_VINE`) |
| Thủy `THUY` | `VPAL_THUY` | Lam nước | Lam nhạt | `#3C8FD6` / `#A9DBFF` / `#1D4A75` | Giọt nước, gợn sóng tròn (`WATER_RIPPLE`) |
| Hỏa `HOA` | `VPAL_HOA` | Đỏ cam | Vàng lửa | `#E8602A` / `#FFD65C` / `#7E2610` | Lưỡi lửa (`FLAME_TONGUE`) |
| Thổ `THO` | `VPAL_THO` | Nâu hổ phách | Vàng đất | `#B07A32` / `#E8C474` / `#5A3B16` | Đá vụn, bụi cuộn (`ROCK_DUST`) |
| Lôi `LOI` | `VPAL_LOI` | Tím điện | Trắng tím | `#8A4DFF` / `#E6D9FF` / `#3A1D7A` | Tia gấp khúc (`ZIGZAG_BOLT`) |
| Băng `BANG` | `VPAL_BANG` | Lam băng | Trắng xanh | `#6FD3F0` / `#E8FBFF` / `#2A7590` | Tinh thể sáu cạnh (`HEX_CRYSTAL`) |
| Phong `PHONG` | `VPAL_PHONG` | Lục ngọc nhạt | Trắng xanh lục | `#7FD9B5` / `#E3FFF4` / `#2D755C` | Dải gió xoắn, vệt cong mảnh (`WIND_RIBBON`) |
| Vô hệ (`NONE`) | `VPAL_NONE` | Bạc trắng linh quang | Trắng | `#D8DEE8` / `#FFFFFF` / `#6B7380` | Khí tròn mảnh (`PLAIN_QI`) |

Hex là **giá trị khởi điểm** để sinh `shared/gamedata/vfx/palettes.json` (AR-04 §3.1, §11.3); hiệu chỉnh bằng mẫu Phase 0 #5 (§10) thì sửa bảng này trước (tăng phiên bản), rồi tới dữ liệu.

- <a id="art-r-020"></a>**ART-R-020** Màu nguyên tố **luôn kèm hình dạng / hoa văn** riêng (motif ở bảng trên — Hỏa: lưỡi lửa; Lôi: tia gấp khúc; Băng: tinh thể sáu cạnh…) để người mù màu vẫn phân biệt (GD-24 trợ năng). Màu nguyên tố **giữ nguyên sắc độ** của bảng trên kể cả khi gần màu hệ thống (Hỏa ↔ cảnh báo đỏ cam, Thủy / Băng ↔ cảnh báo đồng minh lam, Phong ↔ viền an toàn lục nhạt): phân biệt bằng **hình dạng** — VFX nguyên tố trên mặt đất không dùng viền liền + nền phẳng (dành cho vùng cảnh báo), vùng cảnh báo unlit, có viền sáng liên tục và lấp dần (AR-04 VFX-R-015; chốt VFX-Q-3 theo mặc định).
- <a id="art-r-022"></a>**ART-R-022** **Vô hệ** (`VPAL_NONE`, bạc trắng linh quang) chỉ dùng cho kỹ năng `element = NONE`, `INHERIT` khi tâm pháp chính vô hệ (vd. `TEC_DAN_KHI`) và hỗ trợ (hồi máu / khiên) không nguyên tố; **không** có "màu hồi máu" riêng (AR-04 VFX-R-012, VFX-R-013; chốt VFX-Q-1 theo mặc định).

### 4.3. Phẩm vật phẩm (GD-09) và huyết mạch linh thú (GD-25)

| Bậc | Phẩm vật phẩm | Huyết mạch | Màu viền / tên |
|:---:|---------------|-----------|----------------|
| 1 | Phàm `PHAM` | Phàm `PHAM` | Xám trắng |
| 2 | Linh `LINH` | Linh `LINH` | Lục |
| 3 | Bảo `BAO` | Huyền `HUYEN` | Lam |
| 4 | Tiên `TIEN` | Địa `DIA` | Tím |
| 5 | Thần `THAN` | Thiên `THIEN` | Vàng kim (có lấp lánh) |

- <a id="art-r-021"></a>**ART-R-021** Màu phẩm kèm **số vạch / hình mây** (1–5) ở icon và tooltip — không chỉ dựa vào màu.

### 4.4. Màu mang nghĩa hệ thống (cấm dùng trang trí)
| Nghĩa | Màu |
|-------|-----|
| Vùng cảnh báo quái (GD-12) | Đỏ cam bán trong suốt, viền sáng, độ đậm ≥ 0,6; bảng thay thế cho 3 loại mù màu + **sọc chéo** (GD-24 CAM-R-061 — giá trị hex 4 bảng `TGPAL_*` ở đó) |
| Vùng cảnh báo đồng minh / kỹ năng mình | Lam trắng mờ |
| Tên ác nhân / đại ác nhân (GD-32) | Đỏ / đỏ thẫm |
| Linh chủng (GD-25) | Hào quang theo màu huyết mạch §4.3 |
| Vùng an toàn | Viền lục nhạt ở mặt đất (khi vào / ra) |

---

## 5. Ngôn ngữ hình dạng

### 5.1. Ba đại tông (GD-14 §5.4b, LR-01)
| Tông | Hình dạng | Màu | Chất liệu | Biểu tượng |
|------|-----------|-----|-----------|-----------|
| **Thanh Vân Tông** (chính) | Thẳng, cân đối, đường dọc thanh thoát; áo dài, tay rộng | Xanh lam + trắng | Lụa, ngọc | Mây và kiếm |
| **Tạc Thiên Bang** (tà, giang hồ) | Góc cạnh, bất đối xứng, khoác lông thú, giáp vá | Đỏ sẫm + đen + đồng | Da, sắt rèn thô, xích | Búa đục núi / vết nứt trời |
| **Hợp Hoan Tông** (ma, mị thuật) | Đường cong mềm, lụa bay, nhiều lớp **kín đáo** | Hồng đào + tím + vàng nhạt | Lụa mỏng nhiều lớp (không hở), hoa | Hoa đào song sinh, quạt |

- <a id="art-r-030"></a>**ART-R-030** Hợp Hoan Tông: gợi "mị" bằng **màu, chuyển động lụa, VFX hoa** — **không** bằng hở da, tư thế gợi dục (SO-R-04A, ART-R-004). Tạc Thiên Bang: bất kham, không biểu tượng tội phạm ngoài đời thực.
- Danh hiệu NPC lãnh đạo (Tạc Thiên Đại Đế…, GD-32 PK-R-056): hào quang danh hiệu cùng kiểu với người chơi.

### 5.2. Quái theo họ (GD-12 `MonsterFamily`)
| Họ | Hình dạng | Đọc nhanh |
|----|-----------|-----------|
| `BEAST` | Hữu cơ, tròn khối, lông / vảy | Màu theo môi trường sống |
| `HUMANOID` | Như người, đồ nghề rõ (cuốc mỏ, cung) | Phe qua trang phục |
| `SPIRIT` | Bán trong suốt, phát sáng, viền mờ | Màu nguyên tố |
| `PLANT` | Rễ, dây leo, hoa | Lục + màu độc |
| `INSECT` | Khớp nhọn, nhiều chân | Vỏ bóng |
| `CONSTRUCT` | Khối đá / gỗ, khắc phù văn | Phù văn phát sáng = điểm yếu |
| `UNDEAD` | Xương, giáp rỉ, khí lam lạnh | Oán khí lam |
| `DEMON` | Sừng, góc nhọn, khí đỏ đen | Mắt phát sáng |

- **Tinh anh:** to hơn 1,3×, hào quang nhẹ. **Boss:** to 2–3×, điểm nhấn màu riêng, pha có đổi ngoại hình / VFX. **Linh chủng:** hào quang + cột sáng mảnh theo huyết mạch.

---

## 6. VFX

- <a id="art-r-040"></a>**ART-R-040** VFX **ghép tham số** (GD-08): mẫu theo hình dạng (đạn, quạt, vùng tròn, lướt, quanh thân) × bảng màu nguyên tố × lớp phủ biến thức; không làm VFX riêng cho từng chiêu chế tạo.
- <a id="art-r-041"></a>**ART-R-041** Độ sáng có trần: bloom không làm trắng xóa màn hình; VFX của **người khác** (không thù địch) giảm độ đậm **theo preset đồ họa** (TD-05 §9, dòng "VFX người chơi khác") — không có tùy chọn riêng trong GD-24 (GD-24 CAM-R-064, chốt AR-04 VFX-Q-4); VFX không bao giờ phủ lên vùng cảnh báo quái (vẽ cảnh báo ở lớp trên).
- <a id="art-r-042"></a>**ART-R-042** Mỗi kỹ năng: **anticipation** (tụ lực) → **impact** (bùng sáng ngắn, rung có giới hạn GD-24) → **follow-through** (tàn quang) — quan trọng cho cảm giác đánh.
- Ngân sách hạt / mesh VFX: TD-11 / AR-02.

---

## 7. UI cổ phong
- Khung giấy dó / lụa, viền mực, góc bo kiểu hoa văn mây; chữ tiêu đề kiểu thư pháp (chỉ tiêu đề), chữ nội dung **dễ đọc** (sans hỗ trợ tiếng Việt đầy đủ dấu).
- Icon: nét viền rõ, nền theo màu phẩm (§4.3), đọc được ở 32px.
- Thông báo quan trọng (đại hạn, thiên phạt, chết thật): mực đỏ, ấn triện.
- Bố cục co giãn PC / mobile (TD-05, PLAN §5.8); vùng chạm ≥ 44px ở mobile.

---

## 8. Ví dụ đúng / sai (mô tả)

| Chủ đề | ✅ Đúng | ❌ Sai |
|--------|--------|-------|
| Nhân vật ở camera game | Bóng dáng + vũ khí + màu tông nhận ra ngay ở 20m | Chi tiết thêu áo đẹp nhưng nhìn từ trên chỉ thấy một khối màu |
| Môi trường | 2–3 điểm nhấn vẽ riêng, còn lại kit + atlas, đổi màu theo vực | Mỗi cây một texture riêng, bản đồ nặng và loạn |
| Vùng cảnh báo | Đỏ cam đậm, viền sáng, có sọc khi bật chế độ mù màu, nằm trên mọi VFX | Đỏ nhạt chìm trong VFX lửa của đồng đội |
| Màu nguyên tố | Hỏa đỏ cam + lưỡi lửa | Dùng màu tím Lôi cho trang trí cột nhà (gây hiểu nhầm) |
| Hợp Hoan Tông | Lụa nhiều lớp bay theo chuyển động, hoa đào, ánh hồng | Trang phục hở hang, tư thế gợi dục |
| Boss | To, màu nhấn riêng, đổi VFX theo pha, cảnh báo rõ | Boss cùng tông màu nền, chiêu không có cảnh báo |
| Ánh sáng đêm (GD-21) | Tối hơn nhưng vẫn đọc được đường đi, quái, vùng cảnh báo | Tối đen tới mức phải tăng gamma mới chơi được |

---

## 9. Quy trình duyệt khi dùng AI (PLAN §6.6)
1. **Concept** (mô hình vẽ ảnh được phép theo TD-15 §4.5, LoRA phong cách dự án) → art director chọn.
2. **Model** (TRELLIS.2 → Blender theo AR-02) → kiểm tra ngân sách tự động (CI, TD-11).
3. **Chụp đúng camera game** (cảnh xem trước chuẩn: nền Thanh Trúc Lâm, ánh sáng giờ Ngọ và giờ Tý) → so với bảng màu / ngôn ngữ hình dạng / bất biến §1.
4. **Ký duyệt** trong hồ sơ nguồn gốc (`reviewedBy`, TD-15 §4.5) → mới được merge.
- <a id="art-r-050"></a>**ART-R-050** Không dùng ảnh / model của bên thứ ba làm đầu vào AI nếu không có quyền (ghi `inputs` trong hồ sơ); không prompt theo tên họa sĩ còn sống / tên game khác.

---

## 10. Ảnh mẫu cần làm ở Phase 0
| # | Mẫu | Mục đích |
|---|-----|----------|
| 1 | 1 nhân vật người chơi (kiếm) ở **cả hai tỷ lệ** A / B, 3 bộ trang phục tông | Chốt Q13, kiểm silhouette |
| 2 | 1 quái thường (Trúc Thử) + 1 boss (Hồ Yêu Mẫu) | Quy mô, hào quang tinh anh / boss |
| 3 | 1 góc Thanh Trúc Lâm 40×30m (kit + atlas + 2 điểm nhấn) | Phong cách môi trường, mật độ |
| 4 | Cùng góc đó: ngày / hoàng hôn / đêm / mưa / sương | Ánh sáng, khả năng đọc ban đêm |
| 5 | 1 VFX kỹ năng (Hỏa) + 1 vùng cảnh báo + bảng mù màu | Độ sáng, ưu tiên hiển thị |
| 6 | Bật / tắt shader thủy mặc | Quyết định mặc định theo preset |
| 7 | 1 màn UI (túi đồ + tooltip phẩm) PC và mobile | Phong cách UI, độ đọc |

---

## 11. Câu hỏi mở
- [ ] **ART-Q-1** (= PLAN Q13): Tỷ lệ nhân vật A (1/5) hay B (1/6–1/6,5)?
- [ ] **ART-Q-2:** Shader thủy mặc toàn cảnh mặc định bật ở preset Trung không (chi phí GPU — đo ở mẫu #6)?
- [ ] **ART-Q-3:** Phân loại độ tuổi mục tiêu (12+ hay 16+) — ảnh hưởng mức máu me, Tạc Thiên Bang, Hợp Hoan Tông.

---

## Lịch sử thay đổi
| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến, định hướng D-22, thiết kế góc nhìn trên cao + 2 phương án tỷ lệ, bảng màu theo bản đồ Vực 1 / nguyên tố / phẩm / huyết mạch / màu hệ thống, ngôn ngữ hình dạng ba tông và họ quái, VFX, UI, ví dụ đúng / sai, quy trình duyệt AI, ảnh mẫu Phase 0 |
| v0.2 | 2026-10-25 | — | Đợt rà nhất quán (AR-04): §4.2 thêm `paletteId`, hex khởi điểm và motif cho 8 hệ + vô hệ `VPAL_NONE` (VFX-Q-1); ART-R-020 giữ sắc độ, phân biệt bằng hình dạng (VFX-Q-3); ART-R-022 vô hệ; ART-R-041 độ đậm VFX người khác chỉ theo preset (VFX-Q-4); §4.4 trỏ hex `TGPAL_*` ở GD-24 |

