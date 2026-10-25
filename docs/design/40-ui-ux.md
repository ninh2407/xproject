# GD-17 — UI/UX Spec

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-17 · Prefix ID: `UX` |
| Owner | Game Designer + UI Artist + Client Dev |
| Trạng thái | 🟡 Bản nháp v0.2 |
| Cần trước | Phase 1 (HUD chiến đấu, túi đồ, kỹ năng), Phase 2–3 (các màn hệ thống), Beta (màn mở rộng) |
| Quyết định nền | D-01 / D-09 (camera chéo cố định), D-07 (i18n từ đầu), D-10 (WASD), D-17 / D-22 (không ép mua; đa nền tảng), PLAN §6.1 (cổ phong / thủy mặc) |
| Liên quan | [GD-24 Camera & Điều khiển](25-camera-controls.md) (sơ đồ phím chuẩn §5, trợ năng §9) · [TD-05 Client](../tech/04-client.md) §7 · [LR-01 World Bible](../lore/world-bible.md) (giọng văn) · [GD-29 RNG](05-rng-fairness.md) (`ChanceView`) · [GD-19 Monetization](50-monetization.md) · mục "UI/UX" của mọi GD |

> Tài liệu này **gom và chuẩn hóa** mục "UI/UX" rải trong các GD thành một danh mục màn hình, một hệ thống thiết kế và các luồng chính. Chi tiết nội dung từng màn vẫn do GD sở hữu quy định; khi GD và GD-17 vênh về **bố cục / thành phần / phím**, theo GD-17 (phím theo GD-24 §5).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-17 | Mục |
|--------------|-----------------|-----|
| TD-05 | Danh mục màn hình, thành phần dùng chung, slice store mỗi màn | §3, §6, §7 |
| Mọi GD | Mẫu thành phần (tooltip, hộp xác nhận rủi ro, ChanceView, thông báo) | §6 |
| AR-01 Art Bible | Hướng mỹ thuật UI, bảng màu, chữ | §2, §6.1 |
| GD-18 Onboarding | Luồng người dùng, thứ tự mở UI | §5 |

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="ux-r-001"></a>**UX-R-001** | **Không chữ viết cứng:** mọi chuỗi qua i18n key (TD-05 CL-R-084, TD-07 §6); bố cục chịu được chuỗi dài hơn 40% (tiếng Anh / ngôn ngữ khác). |
| <a id="ux-r-002"></a>**UX-R-002** | **Vùng cảnh báo và thông tin sinh tử không bao giờ bị UI che** (GD-24 CAM-R-006): HUD không phủ vùng giữa màn hình; cửa sổ mở khi đang chiến đấu thu nhỏ / trong suốt. |
| <a id="ux-r-003"></a>**UX-R-003** | **Mọi hành động có rủi ro chết thật hoặc tiêu thọ nguyên** dùng **hộp xác nhận rủi ro** chuẩn (§6.4): hiện số năm thọ mất / tỷ lệ thất bại, giữ nút để xác nhận (GD-26 LC-R-042, GD-29 RF-R-025, GD-24 CAM-R-022). |
| <a id="ux-r-004"></a>**UX-R-004** | **Không ép mua** (GD-19): không quảng cáo cửa hàng sau khi chết / ở màn tổng kết kiếp / khi thất bại đột phá; không đồng hồ đếm ngược giả; giá tiền thật luôn hiện cạnh giá Tiên Ngọc. |
| <a id="ux-r-005"></a>**UX-R-005** | **Mỗi số liệu ngẫu nhiên loại A hiện bằng `ChanceView`** (GD-29 §5.2) — cùng một thành phần ở mọi màn (đột phá, chế tác, dung hợp, bắt thú…). |
| <a id="ux-r-006"></a>**UX-R-006** | Mọi màn dùng được bằng **bàn phím-chuột, tay cầm và cảm ứng** (D-22); vùng chạm ≥ 44px ở bố cục điện thoại (TD-05 CL-R-083). |

---

## 2. Nguyên tắc

- <a id="ux-r-010"></a>**UX-R-010** **Phong cách:** cổ phong tối giản — nền giấy dó / lụa mờ, viền mực loang, họa tiết mây cuộn ở góc; **nội dung trên hết**: khung trang trí mỏng, không lấn chỗ dữ liệu. Điểm nhấn bằng màu ngũ hành và vàng nhạt (phẩm cao).
- <a id="ux-r-011"></a>**UX-R-011** **Giọng văn** theo LR-01 §2: Hán Việt, trang nhã; nhãn ngắn ("Bế quan", "Đột phá", "Tàng Bảo Các"); thông báo hệ thống kiểu "thiên cơ" nhưng thông tin phải rõ (số liệu cụ thể, không chỉ ẩn dụ).
- <a id="ux-r-012"></a>**UX-R-012** **Đọc được trên mọi màn hình:** cỡ chữ cơ sở 14px PC / 15px điện thoại, chỉnh 80–150% (GD-24 §9); tương phản chữ ≥ 4,5:1; không dùng màu là kênh duy nhất (thêm icon / hoa văn — mù màu GD-24 CAM-R-061).
- <a id="ux-r-013"></a>**UX-R-013** **Ít cửa sổ chồng:** tối đa 2 cửa sổ nội dung mở cùng lúc (vd. túi đồ + cửa hàng); mở cái thứ ba thì đóng cái cũ nhất. **Màn con mở từ một cửa sổ cha** (vd. đột phá hay Thiên Cơ Lục mở từ `C`) **tính chung là một cửa sổ** với cha. `Esc` đóng lần lượt (con trước, cha sau).
- <a id="ux-r-014"></a>**UX-R-014** **Phản hồi tức thì:** mọi nút có trạng thái nhấn / chờ server (spinner nhỏ ≤ 300ms trước khi hiện); lỗi hiện tại chỗ bằng `messageKey` (TD-01 TA-R-043), không chỉ toast.

---

## 3. Danh mục màn hình & panel

Cột **Phím** theo GD-24 §5 ("—" = mở từ menu `Esc`, từ nút HUD hoặc bằng tương tác `X` với NPC / trạm). **Nguồn** = API REST (TD-04) hoặc State / message realtime (TD-02).

### 3.1. HUD (luôn hiện khi trong bản đồ)

| Thành phần | Nội dung | Nguồn | GD |
|-----------|----------|-------|----|
| Khung nhân vật | Máu, linh lực, thể lực, năng lượng tuyệt kỹ (từ KD), cảnh giới + tầng, **tuổi / thọ còn lại** (đổi màu theo `warningLevel`) | `SelfState`, `S2C_LifecycleUpdate` | GD-06, GD-26 |
| Thanh kỹ năng | Q E R F (V từ TC), T tuyệt kỹ, Tab pháp bảo; hồi chiêu, thiếu tài nguyên mờ | `character_loadout`, `S2C_CooldownSync` | GD-07, GD-09 |
| Ô nhanh | 1–4 | `character_quick_slot` | GD-09 |
| Buff / debuff | Icon + thời gian; status nguy hiểm viền đỏ; `ST_HEAVEN_PUNISHMENT`, `ST_QI_UNSETTLED` có chú thích | `S2C_StatusUpdate` | GD-06, GD-21, GD-32 |
| Minimap | Fog, POI đã biết, hướng truyền tống gần nhất, **vòng 12 canh giờ**, icon thời tiết, tên bản đồ + kênh, biểu tượng vùng PvP / an toàn / cấm bay | State `phase`/`weather`, fog | GD-21, GD-32, GD-31 |
| Nhiệm vụ theo dõi | 1–3 nhiệm vụ, bước hiện tại | `quest_progress` | GD-15 |
| Tổ đội | Khung thành viên (máu, cảnh giới, biểu tượng hấp hối / quy tức) | `S2C_PartyUpdate` | GD-14, GD-28 |
| Chat | Kênh tab, thu gọn được | `S2C_ChatMessage` | GD-14 |
| Thông báo hệ thống | Dải trên cùng (toàn server / bản đồ), hàng đợi theo ưu tiên SO-R-060 | `S2C_Notice`, `broadcast.requested` | GD-14 |
| Chế độ PK + sát nghiệp | Nút chế độ (sen trắng / cờ / kiếm đỏ), màu tên, Huyết thù | `S2C_PkState` | GD-32 |
| Linh thú | Chân dung thú xuất chiến, máu, hồi chiêu chiêu lệnh (Y) | `beast-loadout` | GD-25 |
| Cue cơ duyên | Biểu tượng "thiên cơ dị động" gần nhân vật | `S2C_EncounterCue` | GD-22 |
| Hiểm địa | Pha, đồng hồ, lối thoát gần nhất, túi hiểm địa | `S2C_DangerRunState` | GD-28 |
| Sự kiện thế giới | Thanh sự kiện + đóng góp (Đồng / Bạc / Vàng) | `S2C_WorldEvent*` | GD-23 |
| Ngự kiếm | Linh lực còn khi bay, cảnh báo sắp tự đáp | `S2C_FlightState` | GD-31 |

### 3.2. Màn hình và cửa sổ

| Màn | Phím | Mục đích & thành phần chính | Trạng thái rỗng / lỗi / tải | Nguồn | GD |
|-----|:---:|-----------------------------|------------------------------|-------|----|
| Đăng nhập / chọn server | — | Tài khoản, server, ngôn ngữ | Lỗi mạng: thử lại; bảo trì: thông báo + giờ dự kiến | REST auth | TD-04, TD-08 |
| **Tạo nhân vật** | — | Xuất thân, roll linh căn + chỉ số + thiên phú, lượt roll lại, khóa dòng, nút **"Thiên cơ"** (bảng tỷ lệ công khai), ngoại hình preset + màu (D-08) | Hết lượt roll: nút mờ + lý do | REST talents | GD-27, GD-29 |
| Chọn nhân vật / kiếp | — | Nhân vật sống (1 / server), Bia Vạn Cổ các kiếp trước, nút "Kiếp mới" | Chưa có nhân vật: vào thẳng tạo | REST | GD-26 |
| **Túi đồ & trang bị** | `I` | Lưới túi (40 + mở rộng), 9 ô trang bị, so sánh tooltip, sắp xếp, ví (linh thạch / cống hiến / Tiên Ngọc tách riêng) | Túi đầy: cảnh báo vàng trên HUD | REST inventory + `S2C_InventoryDelta` | GD-09, GD-13 |
| Kho đồ | `X` NPC kho | Kho 60 ô | Chỉ khi đứng gần NPC (`CharacterContext`) | REST | GD-09 |
| **Nhân vật / tu luyện** | `C` | Chỉ số (rê chuột: breakdown), linh căn, thiên phú (ẩn → "???"), tu vi / bình cảnh, đan độc, tâm cảnh, **Thiên Cơ Lục** (tab), danh vọng phe, danh hiệu | — | REST stat-inputs, lifecycle | GD-04, GD-06, GD-27, GD-29, GD-14 |
| **Đột phá** | từ `C` | Điều kiện, vật phẩm hỗ trợ, **ChanceView** (từng dòng đóng góp), hộ pháp, cảnh báo thiên kiếp / LAST_LIGHT (**hộp xác nhận rủi ro**), nút **Binh giải** trong thiên kiếp | Thiếu điều kiện: liệt kê từng điều | REST breakthrough | GD-04, GD-26, GD-29 |
| **Công pháp** | `K` | Tâm pháp chính, kỹ năng đã học, thuần thục, gán ô Q–V, xem trước (vùng trúng vẽ trên lưới) | Chưa học: gợi ý nguồn | REST techniques | GD-07 |
| **Đan phòng** | `X` trạm | Đan phương, nguyên liệu đủ / thiếu, đan lô / hỏa chủng, số mẻ, **ChanceView**, minigame hỏa hầu (ngữ cảnh phím riêng CAM-R-020), kết quả + đan văn | Thiếu cấp / nguyên liệu: mờ + lý do | REST alchemy | GD-10 |
| **Lò rèn** | `X` trạm | Khí phổ, búa, minigame nhịp, cường hóa / tẩy luyện / thăng phẩm (Beta), pity hiện trên tooltip | — | REST forging | GD-11 |
| **Ngộ Đạo Thạch** | `X` trạm | Ba khay hạch tâm / nguyên tố / biến thức, hình chiêu trên mặt đất, thanh ngân sách, tỷ lệ đột biến, đặt tên, Diễn Pháp Tràng | Thiên đạo bất dung: thanh đỏ | REST technique-forge | GD-08 |
| Khảo hạch nghề | `X` trưởng lão | Giai đoạn mở được, lượt còn, minigame khảo hạch | — | REST professions | GD-10 §5.9 |
| **Bản đồ thế giới** | `M` | Vực / bản đồ, fog thủy mặc, truyền tống trận đã mở (nhấp để dịch chuyển, phí), thời tiết hiện tại + dự báo, Huyết thù (vị trí vực), Linh chủng đã quét (30s) | Bản đồ chưa đến: mây phủ | State + REST | GD-21, GD-32, GD-25 |
| **Vạn Giới Lục** | `J` | Yêu thú (band độ hiếm sau khi gặp — GD-29 §5.3), linh thảo (điều kiện xuất hiện), địa danh, Linh Thú Phổ | Mục chưa gặp: hình bóng | REST codex | GD-21, GD-12, GD-25 |
| **Sổ nhiệm vụ** | `L` | Chính / phụ / xuất thân / tông môn hằng ngày, lược truyện kiếp sau | Rỗng: gợi ý NPC | REST quests | GD-15 |
| **Bạn bè & xã hội** | `O` | Bạn bè, chặn, Chiêu Mộ, **tab Tông môn** (tông NPC: bậc, cống hiến, Bảo Khố; tông tự lập: thành viên, tông quy, tông khố), gói phúc lợi, Thiên Bảng | — | REST social | GD-14 |
| Hội thoại NPC / cơ duyên | `X` | Khung thoại Ink, lựa chọn có tỷ lệ "~%" (ROLL_CHECK), cảnh báo tiêu thọ | Phiên hết hạn: đóng nhẹ | `S2C_DialogueStep`, `S2C_EncounterStep` | GD-15, GD-22 |
| Cửa hàng NPC / Hắc Thị | `X` NPC | Mua / bán / mua lại; ác nhân giá +50% | Không đủ tiền: mờ | REST shops | GD-13, GD-32 |
| Đấu giá (Vạn Bảo Lâu) | `X` NPC | Tìm, lọc, treo bán, thuế | — | REST auction | GD-13 |
| Thư | — (biểu tượng HUD khi có thư) | Thư hệ thống / người chơi, nhận đính kèm | Hết hạn: xám | REST mail | GD-09 |
| **Linh thú** | `B` | Thẻ thú (tư chất, huyết mạch, đặc tính, thọ, thân mật), gia phả 3 đời, kết khế / xuất chiến | Chưa có thú: hướng dẫn Linh chủng | REST beasts | GD-25 |
| Linh Thú Viên (lai tạo) | `X` trong động phủ | Hai ô bố mẹ, **ChanceView** biến dị, phân phối loại biến dị, chi phí thọ | — | REST beasts/breed | GD-25 |
| **Động phủ** | `H` (trong động phủ) | Công trình, Linh Điền, quyền thăm, trang trí | Phong ấn: nút thách đấu thủ vệ | REST cave | GD-30, GD-26 |
| Hiểm địa — vào / kết quả | `X` cửa | Màn xác nhận "Sinh tử tự chịu", kết quả lượt (mang ra / mất) | — | REST danger | GD-28 |
| **Màn tổng kết kiếp** | tự mở | Tiểu sử (Bia Vạn Cổ), chọn gửi Tàng Bảo Các (tọa hóa / binh giải), chọn linh thú giữ lại, cảnh báo **Di Tàng kiếp trước còn N vật phẩm**, điểm Luân Hồi, lời trăn trối | Hết `endingDeadline`: tự kết thúc | REST ending | GD-26, GD-25 |
| **Luân Hồi** | từ màn tổng kết / chọn nhân vật | Điểm Luân Hồi, cửa hàng unlock, ký ức nghề | — | REST rebirth | GD-26, GD-10 |
| Luân Hồi Đài (Di Tàng) | `X` POI | Tầng ấn (số vật phẩm, phẩm cao nhất), phí giải ấn, vào Tàn Hồn Cảnh | Tầng chưa đủ cảnh giới: khóa + điều kiện | REST legacy-stash | GD-26 §5.15 |
| Tàng Bảo Các | `X` NPC | Ô kho tài khoản | — | REST vault | GD-26 |
| Thiên Cơ Lục | tab trong `C` | 50 roll loại A gần nhất, kỳ vọng / thực tế, khiếu nại | Kiếp mới: rỗng | REST fortune-log | GD-29 |
| Chế độ PK | `P` | Chọn chế độ, sát nghiệp, Huyết thù, Thiên phạt còn lại | Đang PvP: không về Hòa bình | `S2C_PkState` | GD-32 |
| Bảng truy nã | `X` bảng ở thành | Đại ác nhân, vực hiện tại | — | REST bounties | GD-32 |
| Sự kiện thế giới | — (nút HUD) | Lịch sắp tới, nhắc lịch, đóng góp | — | REST world-events | GD-23 |
| **Cửa hàng ngoại hình** | — (nút HUD / menu) | Ngoại hình, Vấn Đạo Lệnh, gói Tiên Ngọc (giá tiền thật đi kèm), hạn mức chi tiêu | Cửa hàng bị khóa (số dư âm): giải thích | REST shop / payments | GD-19 |
| Vấn Đạo Lệnh | — (nút HUD) | 50 cấp, nhánh miễn phí / trả phí, nhiệm vụ mùa | Hết mùa: đếm tới mùa sau | REST season-pass | GD-19 |
| **Cài đặt** | `Esc` → Cài đặt | Đồ họa (preset TD-05), âm thanh, phím (đổi phím GD-24), điều khiển (click-to-move, hỗ trợ ngắm), trợ năng, ngôn ngữ, quyền riêng tư (tắt telemetry không thiết yếu GD-20), ẩn tên trong thông báo | — | `account.settings` | GD-24, GD-20, GD-14 |

- <a id="ux-r-020"></a>**UX-R-020** Màn nào mở bằng `X` (trạm / NPC) đóng tự động khi nhân vật rời bán kính tương tác (CharacterContext, TD-01 §11) hoặc vào chiến đấu (trừ màn xem).
- <a id="ux-r-021"></a>**UX-R-021** Trong **hiểm địa** và **thiên kiếp**, các cửa sổ không liên quan (cửa hàng ngoại hình, Vấn Đạo Lệnh, đấu giá, Luân Hồi) không mở được.

---

## 4. Wireframe

### 4.1. HUD PC (1920×1080)
```
┌───────────────────────────────────────────────────────────────────────────────────────┐
│ [Thông báo hệ thống — dải mỏng, tự ẩn]                                    [Minimap ◐ ]│
│ ┌Chân dung┐ Lâm Tiểu Phàm  Luyện Khí 7                                    │canh Ngọ ☀│
│ │  (HP)   │ ████████████░░ HP   ▓▓▓▓▓▓░ Linh lực   ▒▒▒▒ Thể lực          │V1_F05 #2 │
│ └─────────┘ Tuổi 41 / thọ 120 (còn 79)   [buff][buff][debuff]             └──────────┘│
│ ┌Tổ đội──┐                                                         ┌Nhiệm vụ────────┐│
│ │A ████  │                                                         │▸ Bái sư (2/3)  ││
│ │B ███░  │                  (vùng giữa: KHÔNG có UI — UX-R-002)     │▸ Gánh nước 3/5 ││
│ └────────┘                                                         └────────────────┘│
│                                                                                       │
│ ┌Chat (thu gọn)──────────┐                     [Linh thú ◉ Y:4s]   [⚔ PK: Hòa bình]  │
│ │[Thế giới] ...          │   [1][2][3][4]   [Q][E][R][F][V]  [T]  [Tab]   [🏪][📜]    │
│ └────────────────────────┘                    hồi chiêu dạng quạt trên ô              │
└───────────────────────────────────────────────────────────────────────────────────────┘
```

### 4.2. HUD điện thoại (ngang)
```
┌───────────────────────────────────────────────────────────────┐
│[HP▓▓▓▓░][LL▓▓░] LK7 · 41/120          [thông báo]   [map ◐]   │
│[buff…]                                                        │
│                                                               │
│   ( joystick nổi —                              (Q)  (E)      │
│     vùng trái 40% )                          (R)  [ ⚔ ]  (F)  │
│                                               (né)  (đỡ)      │
│ [💬] [1][2]                          [X tương tác ngữ cảnh]   │
└───────────────────────────────────────────────────────────────┘
  vùng chạm ≥ 44px · safe area · nút kỹ năng kéo để ngắm (GD-24 §8)
```

### 4.3. Màn đột phá
```
┌ Đột phá: Luyện Khí 9 → Trúc Cơ ──────────────────────────────┐
│ Điều kiện: ✓ Tu vi đầy  ✓ Trúc Cơ Đan (Thượng)  ✓ Linh mạch  │
│ ┌ ChanceView ─────────────────┐   Hộ pháp: [ + ] [ + ] [ + ]  │
│ │  Tỷ lệ thành công   62%     │   Vật phẩm hỗ trợ: [ô][ô][ô]  │
│ │  Cơ bản         +40         │                               │
│ │  Đan Thượng     +20         │   Thất bại: −5 năm thọ,       │
│ │  Tâm cảnh       +3          │   tu vi còn 90%, Suy yếu 30'  │
│ │  Linh mạch      +5          │                               │
│ │  Hỏa hầu tâm đắc +5 (pity)  │   [  Vận công 30s  ]          │
│ │  Đan độc        −11         │                               │
│ └─────────────────────────────┘   (báo giá còn 54s)           │
└───────────────────────────────────────────────────────────────┘
```

### 4.4. Túi đồ & trang bị
```
┌ Túi đồ ────────────────────────┬ Trang bị ────────────────────┐
│ [ô][ô][ô][ô][ô][ô][ô][ô]       │      [Đầu]                   │
│ [ô][ô][ô][ô][ô][ô][ô][ô]  40/60│ [Vũ khí] [Áo] [Pháp bảo]     │
│ ...                            │      [Đai]  [Nhẫn][Nhẫn]     │
│ Linh thạch 1.930 · Cống hiến 340│     [Giày]  [Ngọc bội]      │
│ Tiên Ngọc 120 (tách riêng)     │ Chiến lực 4.812              │
│ [Sắp xếp] [Lọc: tất cả ▾]      │                              │
└────────────────────────────────┴──────────────────────────────┘
 tooltip: tên + phẩm (màu + hoa văn), yêu cầu cảnh giới, chỉ số cơ bản, thuộc tính phụ, bộ, so sánh với đồ đang mặc (▲▼)
```

### 4.5. Màn tổng kết kiếp
```
┌ Tọa hóa ─────────────────────────────────────────────────────┐
│  Lâm Tiểu Phàm (kiếp 3) · Trúc Cơ Hậu kỳ · hưởng thọ 182     │
│  "Một đời cầu đạo, chưa thấy Kim Đan…"   [lời trăn trối ✎]   │
│  Dấu mốc: Trúc Cơ năm 64 · 23 cơ duyên · Vạn Độc Đầm ×4      │
│ ┌ Gửi Tàng Bảo Các (3/4 ô trống) ┐ ┌ Linh thú giữ lại (1 ô) ┐ │
│ │ [chọn từ túi / trang bị]       │ │ [Xích Hồ ★★] [Thanh Xà] │ │
│ └────────────────────────────────┘ └─────────────────────────┘ │
│  ⚠ Di Tàng kiếp trước còn 12 vật phẩm (tầng TC) sẽ tiêu tán   │
│  Kho đồ kiếp này → phong ấn vào Di Tàng (tầng LK 18, TC 9)    │
│  Điểm Luân Hồi: +248                     [ Khép lại kiếp này ]│
└───────────────────────────────────────────────────────────────┘
 (không có quảng cáo / nút cửa hàng — UX-R-004)
```

---

## 5. Luồng người dùng chính

```
Đăng nhập → Tạo nhân vật (xuất thân, roll, "Thiên cơ") → cutscene ngắn
  → QC_TUTORIAL_QI ở Thanh Trúc Lâm (không PvP): di chuyển → đánh → né → nhặt → hái → đả tọa
  → Phàm nhân → Luyện Khí 1 (mở thanh kỹ năng 4 ô, mở K)
  → QC_MAIN_V1_JOIN_SECT: chọn tông (Thanh Vân / Tạc Thiên* / Hợp Hoan*) → mở O / tab Tông môn
  → Luyện đan lần đầu (Đan phòng, minigame, ChanceView) → hiểm địa nhất giai (màn "Sinh tử tự chịu")
  → Đột phá Trúc Cơ (màn §4.3) → thành công: cutscene + mở ô V / khinh công
                               → thất bại: hiện mất gì, pity tăng, hồi chiêu 15 phút
  → … chết thật (hiểm địa) / tọa hóa → Màn tổng kết (§4.5) → Luân Hồi (unlock, ký ức nghề)
  → Kiếp mới: lược truyện, chuyển thế đệ tử, Luân Hồi Đài (Di Tàng) khi đủ cảnh giới
   (* Beta)
```

- <a id="ux-r-030"></a>**UX-R-030** **Mở UI theo tiến trình:** phím / nút của hệ thống chưa mở hiển thị khóa + điều kiện (không ẩn hẳn), trừ hệ thống Beta chưa bật (ẩn). Thứ tự mở khớp GD-18 Onboarding.
- <a id="ux-r-031"></a>**UX-R-031** **Sau cái chết thật:** màn đen ngắn → màn tổng kết; không toast, không thông báo khác chen vào; nút duy nhất là tiếp tục quy trình kiếp.

---

## 6. Hệ thống thiết kế

### 6.1. Màu & chữ
| Vai trò | Giá trị gợi ý (AR-01 chốt) |
|---------|----------------------------|
| Nền panel | Giấy dó ngà `#EFE6D2` 92% (sáng) / mực đêm `#1E1B18` 88% (tối, mặc định trong game) |
| Chữ chính | `#2B2620` trên nền sáng / `#EDE6D6` trên nền tối |
| Nhấn | Vàng kim nhạt `#C9A45C` |
| Nguy hiểm / rủi ro chết | Son đỏ `#B23A2E` + hoa văn sóng (không chỉ dựa vào màu) |
| Phẩm vật phẩm | Phàm xám · Linh lục · Bảo lam · Tiên tím · Thần vàng (kèm số vạch hoa văn 1–5) |
| Ngũ hành | Kim · Mộc · Thủy · Hỏa · Thổ · Lôi · Băng · Phong (bảng màu [AR-01](../art/art-bible.md)) |
| Chữ | Tiêu đề: font thư pháp có dấu tiếng Việt (chỉ tiêu đề lớn); nội dung: sans-serif rõ, hỗ trợ đầy đủ dấu tiếng Việt |

### 6.2. Thành phần dùng chung (thư viện `ui/components`)
`Button` (chính / phụ / nguy hiểm / giữ-để-xác-nhận) · `Tabs` · `Window` (kéo, đóng, ghim) · `ItemSlot` + `ItemTooltip` · `StatBreakdown` · **`ChanceView`** (GD-29: số lớn, dòng đóng góp, khoảng chưa biết, pity, hạn báo giá) · **`RiskConfirm`** (§6.4) · `CurrencyLabel` (linh thạch / cống hiến / Tiên Ngọc khác icon) · `CountdownTimer` · `RarityBand` (1–5 vạch mây) · `Toast` · `NoticeBar` · `ProgressRing` (vận pháp / hồi chiêu).

- <a id="ux-r-040"></a>**UX-R-040** Màn mới **phải** dùng thành phần dùng chung; thành phần mới cần duyệt UI trước khi dùng ở nhiều nơi.

### 6.3. Tooltip vật phẩm (thứ tự cố định)
Tên + phẩm (màu + vạch) → loại + ô → yêu cầu cảnh giới (đỏ nếu chưa đủ) → chỉ số cơ bản → thuộc tính phụ → bộ → dị tượng / đan văn → pity cường hóa / nâng phẩm (nếu > 0, GD-29 RF-R-104) → ràng buộc → giá trị bán → so sánh (▲▼) với đồ đang mặc.

### 6.4. Hộp xác nhận rủi ro (`RiskConfirm`)
Viền son đỏ; tiêu đề nêu hậu quả ("Thất bại sẽ thân tử đạo tiêu" / "Tiêu 5 năm thọ nguyên"); hiện **tuổi trước → sau** và thọ còn lại (LC-R-042); tỷ lệ thất bại (nếu có roll); **giữ nút 2s** (UX-AC-004, GD-29) (vòng tiến trình); hủy bằng `Esc` / nút phụ; không có tùy chọn "không hỏi lại" cho rủi ro chết thật.

### 6.5. Thông báo
- <a id="ux-r-050"></a>**UX-R-050** Bốn kênh: **NoticeBar** (toàn server / bản đồ, hàng đợi SO-R-060), **Toast** (kết quả hành động của mình, ≤ 3 cùng lúc, tự ẩn 4s), **dòng chat hệ thống** (lưu lịch sử), **cửa sổ** (chỉ cho việc cần quyết định). Lỗi hành động hiện tại chỗ (UX-R-014).
- <a id="ux-r-051"></a>**UX-R-051** Thông báo không che vùng giữa; trong chiến đấu toast thu nhỏ; tôn trọng `hideNameInBroadcast` (SO-R-061).

### 6.6. Phản hồi âm thanh & rung
Mỗi loại tương tác có SFX ngắn (AU-01): nhấp, mở / đóng cửa sổ, nhận vật phẩm (theo phẩm), lỗi, cảnh báo rủi ro (trầm), thành công đột phá (chuông + sấm). Rung (mobile / tay cầm) chỉ khi: trúng đòn mạnh, hấp hối, thành / bại đột phá; tắt được (GD-24 §9).

---

## 7. Kiến trúc UI ↔ game (theo TD-05 §7)

| Màn / nhóm | Slice store (Zustand) | Lấy dữ liệu | Gửi lệnh |
|------------|----------------------|-------------|----------|
| HUD | `character`, `skills`, `social`, `ui` | State + message (handler cập nhật store) | `game.commands.*` → `IntentAction` |
| Túi đồ, cửa hàng, đấu giá, thư | `inventory` | REST `features/inventory/api.ts` + `S2C_InventoryDelta` | REST có `Idempotency-Key` |
| Tu luyện, đột phá, công pháp, chế tác | `character`, `skills` + slice theo feature | REST preview (`ChanceView`) | REST xác nhận + `quoteId` |
| Xã hội, tông môn, PK | `social` | REST + `S2C_PkState`, chat | REST / message |
| Cài đặt | `settings` | `account.settings` | `PATCH /v1/accounts/me/settings` |

- <a id="ux-r-060"></a>**UX-R-060** Thành phần React chỉ đọc store và gọi action (TD-05 CL-R-080 / 081); nameplate, số sát thương, vùng cảnh báo vẽ bằng Babylon GUI / mesh (CL-R-082), không DOM.
- <a id="ux-r-061"></a>**UX-R-061** Mỗi cửa sổ là một `features/{domain}/ui/*Window.tsx`, đăng ký vào `ui` slice với `windowId`, phím mở lấy từ `ActionId` (GD-24) — không gán phím trong component.

---

## 8. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="ux-ac-001"></a>UX-AC-001 | Chuỗi tiếng Việt viết cứng trong JSX | Lint | Fail (UX-R-001) |
| UX-AC-002 | Ngôn ngữ giả lập dài hơn 40% | Mở mọi cửa sổ | Không tràn / cắt chữ |
| UX-AC-003 | Quái tung vùng cảnh báo giữa màn hình khi túi đồ đang mở | Chiến đấu | Cửa sổ thu nhỏ / trong suốt, vùng cảnh báo thấy rõ |
| UX-AC-004 | Đột phá trong LAST_LIGHT | Bấm xác nhận | `RiskConfirm` đỏ, giữ nút 2s, hiện tuổi trước → sau |
| UX-AC-005 | Nhân vật vừa chết thật | Màn tổng kết | Không có nút / banner cửa hàng (UX-R-004) |
| UX-AC-006 | Gói Tiên Ngọc | Hiển thị | Có giá tiền thật tương đương |
| UX-AC-007 | Mọi màn có roll loại A | Mở | Dùng thành phần `ChanceView` chung |
| UX-AC-008 | Bố cục điện thoại | Đo vùng chạm | Mọi nút ≥ 44px |
| UX-AC-009 | Đang ở Đan phòng, đi ra ngoài 4m | — | Cửa sổ Đan phòng tự đóng (UX-R-020) |
| UX-AC-010 | Mở cửa sổ thứ 3 | — | Cửa sổ cũ nhất đóng (UX-R-013) |
| UX-AC-011 | Trong hiểm địa | Mở cửa hàng ngoại hình | Không mở được (UX-R-021) |
| UX-AC-012 | Hệ thống chưa mở (vd. ô V ở LK) | Nhấn phím | Khóa + điều kiện hiển thị (UX-R-030) |
| UX-AC-013 | Đổi phím `UI_BAG` sang `N` khi `N` là ngự kiếm | Đổi phím | Cảnh báo trùng (GD-24 CAM-R-023) |

---

## 9. Phạm vi

| Hạng mục | Phase 1 | Phase 2–3 / Alpha | Beta |
|----------|:---:|:---:|:---:|
| HUD chiến đấu, túi đồ, công pháp, cài đặt cơ bản, thành phần dùng chung | ✅ | | |
| Tạo nhân vật, tu luyện / đột phá, đan phòng, lò rèn, bản đồ, Vạn Giới Lục, nhiệm vụ, xã hội, màn tổng kết, Luân Hồi, Di Tàng, Thiên Cơ Lục | | ✅ | |
| Ngộ Đạo Thạch, linh thú, động phủ, PK, sự kiện thế giới, ngự kiếm, cửa hàng ngoại hình, Vấn Đạo Lệnh | | | ✅ |
| Bố cục điện thoại đầy đủ | | | sau launch ([PLAN §5.8](../../PLAN.md), D-26: lúc launch chỉ "chạy được") |

---

## 10. Câu hỏi mở
- [ ] **UX-Q-1:** Chủ đề sáng (giấy dó) có cần ở trong game, hay chỉ dùng cho màn ngoài game (đăng nhập, Bia Vạn Cổ)?
- [ ] **UX-Q-2:** Tông môn có cần phím riêng (đề xuất hiện tại: tab trong `O`)?
- [ ] **UX-Q-3:** Có cho người chơi tùy chỉnh vị trí phần tử HUD (kéo thả) ở MVP không?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến (i18n, không che vùng cảnh báo, xác nhận rủi ro, không ép mua, ChanceView chung, đa thiết bị), nguyên tắc phong cách, danh mục HUD + ~40 màn gom từ mọi GD kèm phím GD-24, wireframe HUD PC / điện thoại / đột phá / túi đồ / tổng kết kiếp, luồng người dùng chính, hệ thống thiết kế (màu, chữ, thành phần, tooltip, RiskConfirm, thông báo, phản hồi), kiến trúc UI ↔ game, nghiệm thu |
| v0.2 | 2026-10-25 | — | Mobile lúc launch theo D-26; quy tắc cửa sổ con UX-R-013; sửa giữ nút 2s và tham chiếu |

