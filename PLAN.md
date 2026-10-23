# KẾ HOẠCH TỔNG QUAN — MMORPG WEB 3D TIÊN HIỆP

> **Tên game (tạm):** **Tiên Lộ** · Thế giới: **Huyền Hoang Giới** (tạm, Q8) · Mã dự án: XProject
> **Thể loại:** MMORPG nhập vai hành động **một mạng (permadeath) kết hợp roguelite**, thế giới mở chia bản đồ, bối cảnh tiên hiệp
> **Đồ họa:** 2.5D — model 3D low-poly stylized, camera nhìn chéo từ trên xuống (isometric/top-down nghiêng)
> **Nền tảng:** Trình duyệt web desktop (Chrome/Edge/Firefox), mobile web để sau (2.5D giúp khả thi hơn)
> **Công nghệ:** Babylon.js (TypeScript) · Spring Boot (Java 21) · PostgreSQL · Redis
> **Quy mô team:** ≤ 10 người
> **Trạng thái tài liệu:** v0.5 — đề xuất v0.1–v0.3 đã duyệt (2026-10-19); khảo sát dự án nền tảng mã nguồn mở, **D-19 (kiến trúc lai: Colyseus/TS cho game server, Spring Boot cho API) đã duyệt 2026-10-22**; cập nhật liên tục

---

## Mục lục

1. [Tầm nhìn & trụ cột thiết kế](#1-tầm-nhìn--trụ-cột-thiết-kế)
2. [Phạm vi dự án — làm gì và KHÔNG làm gì](#2-phạm-vi-dự-án--làm-gì-và-không-làm-gì)
3. [Cơ cấu team](#3-cơ-cấu-team)
4. [Tổng quan Game Design](#4-tổng-quan-game-design)
5. [Kiến trúc kỹ thuật](#5-kiến-trúc-kỹ-thuật)
6. [Pipeline nội dung & asset](#6-pipeline-nội-dung--asset)
7. [Lộ trình phát triển (Roadmap)](#7-lộ-trình-phát-triển-roadmap)
8. [Danh mục tài liệu thiết kế cần viết](#8-danh-mục-tài-liệu-thiết-kế-cần-viết)
9. [Quy trình làm việc](#9-quy-trình-làm-việc)
10. [Rủi ro & phương án giảm thiểu](#10-rủi-ro--phương-án-giảm-thiểu)
11. [Câu hỏi mở / Quyết định chờ chốt](#11-câu-hỏi-mở--quyết-định-chờ-chốt)
12. [Lịch sử thay đổi](#12-lịch-sử-thay-đổi)

---

## 1. Tầm nhìn & trụ cột thiết kế

### 1.1. Elevator pitch
> Một MMORPG tiên hiệp **một mạng** chơi ngay trên trình duyệt. Mỗi nhân vật là **một kiếp tu tiên** với thiên phú ngẫu nhiên và **thọ nguyên có hạn**. Người chơi tranh đoạt cơ duyên, luyện đan, mạo hiểm vào **hiểm địa sinh tử** tìm bảo vật để đột phá cảnh giới trước khi đại hạn ập đến. Chết hoặc tọa hóa thì kiếp này kết thúc, nhưng đạo không mất: **luân hồi chuyển thế**, mang theo di sản và kinh nghiệm sang kiếp sau.
>
> Đồ họa 2.5D nhẹ nhàng nhưng có phong cách riêng, để dồn sức vào **độ phong phú của trải nghiệm**: cơ duyên, khám phá, chế tác, những câu chuyện sinh tử.

### 1.2. Trụ cột thiết kế (Design Pillars)
Mọi quyết định tính năng phải phục vụ ít nhất 1 trụ cột. Không phục vụ → cắt.

| # | Trụ cột | Ý nghĩa |
|---|---------|---------|
| P1 | **Mỗi kiếp một câu chuyện** | Một mạng, thọ nguyên có hạn, thiên phú và cơ duyên ngẫu nhiên: mỗi nhân vật là một hành trình riêng, có mở đầu, cao trào, kết thúc, và đáng để kể lại. |
| P2 | **Cảm giác "tu tiên" thật sự** | Đột phá cảnh giới là khoảnh khắc lớn, là thành quả của việc tìm kiếm, luyện đan, mạo hiểm, chứ không phải cày quái. |
| P3 | **Chiến đấu hành động có kỹ năng** | Né, đỡ, combo, đọc chiêu boss. Người chơi giỏi thắng được người chơi có chỉ số cao hơn một chút. |
| P4 | **Sáng tạo & chế tác có chiều sâu** | Luyện đan / luyện khí / chế công pháp là con đường tiến bộ ngang hàng với đánh quái, không phải tính năng phụ. |
| P5 | **Thế giới đáng khám phá** | Mỗi bản đồ có bí mật, cơ duyên, sự kiện động. Đi lang thang cũng có phần thưởng và câu chuyện. |
| P6 | **Nhẹ — vào chơi trong 10 giây** | Web-first, 2.5D: tải nhanh, chạy mượt trên máy văn phòng, hướng tới cả mobile web. |

> **Nguyên tắc đánh đổi:** khi phải chọn giữa *đồ họa đẹp hơn* và *thêm nội dung/hệ thống thú vị*, ưu tiên nội dung — miễn là giữ được phong cách mỹ thuật nhất quán.

### 1.3. Đối tượng người chơi
- 18–35 tuổi, đọc/xem truyện tiên hiệp (Phàm Nhân Tu Tiên, Tiên Nghịch, Đấu Phá...).
- Đã chơi MMO/ARPG (Genshin, Blade & Soul, Tru Tiên, Nghịch Thủy Hàn...).
- Phiên chơi 30–90 phút/ngày, thích cả nội dung một mình lẫn nhóm.
- Người thích roguelite / hardcore (Hades, Tale of Immortal, chế độ Hardcore của Diablo/PoE, game extraction): chấp nhận rủi ro để đổi lấy cảm xúc mạnh.

### 1.4. Game tham khảo
| Khía cạnh | Tham khảo |
|-----------|-----------|
| Góc nhìn & phong cách 2.5D | Albion Online, Lost Ark, Diablo III/IV, Torchlight, Hades |
| Combat top-down hành động | Hades (cảm giác đòn đánh), Lost Ark (kỹ năng định hướng, né), Path of Exile (build) |
| Thế giới mở & khám phá | Albion Online (bản đồ nối nhau), Genshin (phần thưởng khám phá), Zelda BotW (tò mò dẫn đường) |
| Tu luyện / cơ duyên | Tale of Immortal (Quỷ Cốc Bát Hoang — kỳ ngộ, sự kiện ngẫu nhiên), Tru Tiên, Nghịch Thủy Hàn |
| Một mạng, luân hồi, di sản | Tale of Immortal (luân hồi mang điểm sang kiếp sau), Rogue Legacy (di sản), Hades (meta progression), Diablo/PoE Hardcore |
| Hiểm địa vào–lấy–thoát | Dark and Darker, Escape from Tarkov (extraction), Albion Online (vùng đen) |
| Chế tác & kinh tế | Tale of Immortal (luyện đan), Albion Online (kinh tế do người chơi điều khiển) |
| Web MMO | RuneScape (tinh thần), Hordes.io, Mope/Taming.io (khả thi kỹ thuật) |

---

## 2. Phạm vi dự án — làm gì và KHÔNG làm gì

> Team < 10 người **không thể** làm một MMO open-world 3D góc nhìn thứ ba kiểu WoW/Genshin. Giải pháp: **đơn giản hóa đồ họa (2.5D) và chia thế giới thành nhiều bản đồ nối nhau**, dồn nguồn lực vào nội dung.

### 2.1. Quyết định hướng đồ họa: 2.5D

**So sánh các phương án:**

| Tiêu chí | 3D góc nhìn thứ 3 (v0.1) | **2.5D: 3D low-poly + camera trên cao (đề xuất)** | HD-2D: sprite 2D trong cảnh 3D | 2D thuần (top-down) |
|----------|:---:|:---:|:---:|:---:|
| Khối lượng asset môi trường | Rất lớn (phải đẹp ở mọi góc, đường chân trời) | **Vừa** (chỉ thấy mặt trên/một phía, không cần chân trời) | Vừa | Nhỏ–vừa |
| Animation nhân vật | Nhiều, phải chi tiết | **Vừa** (dùng chung skeleton, camera xa nên giấu được chi tiết) | Rất lớn (vẽ 4–8 hướng cho mỗi động tác) | Rất lớn (giống HD-2D) |
| Đổi trang bị hiển thị trên người | Dễ | **Dễ** (gắn mesh vào bone) | Khó (vẽ lại sprite) | Khó |
| Hiệu năng web / mobile | Nặng | **Nhẹ** | Nhẹ | Rất nhẹ |
| Netcode combat | Khó (3D, aim tự do) | **Dễ hơn nhiều** (logic trên mặt phẳng 2D) | Dễ | Dễ |
| Cảm giác "tiên hiệp" (bay, kiếm khí, VFX) | Tốt nhất | **Tốt** (VFX 3D đẹp nhìn từ trên) | Khá | Hạn chế |
| Phù hợp Babylon.js | Tốt | **Tốt** | Được | Kém (nên dùng Phaser/Pixi) |

**→ Quyết định: 2.5D: model 3D low-poly stylized, render bằng Babylon.js, camera nhìn chéo 45–55° từ trên xuống.**
- Camera: cố định góc, **zoom in/out** được; có thể xoay theo bước 90° (cân nhắc, xem Q11).
- **Logic game chạy trên mặt phẳng 2D (XZ)**; độ cao chỉ để hiển thị (cầu, bậc thang, vách đá xử lý bằng navmesh/vùng đi được). Đây là yếu tố giảm phức tạp lớn nhất cho cả client lẫn server.
- Khi cần cảm giác "điện ảnh" (đột phá cảnh giới, boss xuất hiện, thiên kiếp): camera tạm hạ thấp / zoom cận trong vài giây.
- Giữ Babylon.js nên vẫn có thể nâng dần chất lượng đồ họa (ánh sáng, shader) sau launch mà không phải đổi engine.

### 2.2. Mô hình thế giới: "Open world chia bản đồ"

```
                         ┌────────── ĐẠI LỤC THIÊN HUYỀN ──────────┐
   VỰC 1: Thanh Vân        VỰC 2: Vạn Thú Sơn       VỰC 3: Huyết Sa
  ┌─────┬─────┬─────┐     ┌─────┬─────┬─────┐     ┌─────┬─────┐
  │Rừng │Thanh│Bờ   │────►│Cửa  │Thú  │Đỉnh │────►│ ... │ ... │
  │Trúc │Vân  │Sông │     │Núi  │Cốc  │Mây  │     │     │     │
  ├─────┼─Trấn┼─────┤     ├─────┼─────┼─────┤     └─────┴─────┘
  │Mỏ   │(hub)│Đầm  │     │Hắc  │Tông │Hiểm │
  │Hoang│     │Lầy  │     │Lâm  │Môn  │Địa  │
  └─────┴─────┴─────┘     └─────┴─────┴─────┘
  ◆ Bí cảnh, động phủ cổ: instance, cửa vào nằm rải rác trên bản đồ
  ✦ Truyền tống trận: dịch chuyển nhanh giữa các điểm đã khám phá
```

- **Cấu trúc:** Đại lục → nhiều **Vực** (region, mỗi vực 1 chương cốt truyện, 1 dải cảnh giới) → mỗi vực 6–9 **bản đồ** nối nhau.
- **Kích thước bản đồ:** ~250×250m đến 400×400m. Nhân vật chạy ~6m/s, đi xuyên một bản đồ mất khoảng 1 phút; bản đồ đủ dày đặc nội dung, không trống trải.
- **Chuyển bản đồ:** đi tới rìa/cổng → màn chuyển ngắn < 2 giây (client tải trước các bản đồ kề cạnh), nhân vật xuất hiện ở rìa tương ứng của bản đồ mới, giữ hướng chạy → cảm giác gần liền mạch.
- **Kênh (channel):** mỗi bản đồ có nhiều kênh, ~100–150 người/kênh (2.5D hiển thị nhiều người nhẹ hơn 3D). Tự động mở thêm kênh khi đông.
- **Phân loại bản đồ:**

| Loại | Mô tả | Instance? |
|------|-------|:---------:|
| **Thành trấn / Tông môn** | An toàn, giao dịch, chế tác, nhiệm vụ | Chung (channel) |
| **Dã ngoại** | Quái, tài nguyên, cơ duyên, sự kiện thế giới | Chung (channel) |
| **Hiểm địa** ☠ | **Vùng chết thật**: quái mạnh, vật phẩm hiếm để đột phá; vào–lấy–thoát (xem 4.5) | Instance theo nhóm / kênh giới hạn người |
| **Bí cảnh** | Dungeon 1–5 người, boss có cơ chế | Riêng |
| **Động phủ cổ / Kỳ ngộ** | Nội dung cơ duyên, giải đố, solo | Riêng |
| **Động phủ cá nhân** | Nhà riêng (vào từ cửa động trên bản đồ): đả tọa, linh điền, đan thất / khí thất | Riêng (GD-30) |
| **Trú địa tông môn** | Vào từ sơn môn trên bản đồ (hoặc cổng thành trấn): công trình tông môn | Riêng (GD-14, GD-30) |

- **Bản đồ thế giới + sương mù chiến tranh (fog of war):** mở dần khi khám phá, đánh dấu điểm đã tìm thấy.
- **Ngày/đêm & thời tiết** chạy đồng bộ toàn server, ảnh hưởng quái, linh thảo, sự kiện.

### 2.3. Phân loại tính năng theo mức ưu tiên (MoSCoW)

| Tính năng | MVP (Alpha) | Beta | Sau launch |
|-----------|:-----------:|:----:|:----------:|
| Đăng ký / đăng nhập / tạo nhân vật | ✅ | | |
| **Thiên phú & xuất thân ngẫu nhiên khi tạo nhân vật** | ✅ | | |
| **Thọ nguyên, chết thật, luân hồi (tiến trình tài khoản), Tàng Bảo Các** | ✅ | | |
| **Đột phá dựa trên vật phẩm + đan dược, tu vi đánh quái có giới hạn** | ✅ | | |
| **Hiểm địa vào–lấy–thoát: 2 hiểm địa (nhất giai, nhị giai)** | ✅ | | |
| Di chuyển, camera 2.5D, combat cận chiến + pháp thuật | ✅ | | |
| Hệ thống tu luyện (3 cảnh giới đầu) | ✅ | | |
| Trang bị cơ bản + túi đồ | ✅ | | |
| Kỹ năng / công pháp cơ bản | ✅ | | |
| Luyện đan + thu thập (hái thuốc, khai khoáng) | ✅ | | |
| **Vực 1: 1 thành trấn + 6 bản đồ dã ngoại + 3 bí cảnh** | ✅ | | |
| **Chuyển bản đồ, truyền tống trận, bản đồ thế giới + fog of war** | ✅ | | |
| **Cơ duyên (kỳ ngộ ngẫu nhiên): 20–30 sự kiện** | ✅ | | |
| **Ngày/đêm, thời tiết** | ✅ | | |
| Party (tổ đội), chat | ✅ | | |
| **Di Tàng phong ấn + tàn hồn (tầng LK, TC)**, ký ức nghề + khảo hạch (D-20, GD-10 §5.9) | ✅ | | |
| Linh thảo hiếm tranh đoạt (GD-21 §3.10), Thiên Bảng Chiến Lực (GD-14 §5.4d) | ✅ | | |
| **Ba đại tông NPC** (Tạc Thiên Bang, Hợp Hoan Tông — GD-14 §5.4b), gói phúc lợi môn phái | | ✅ | |
| **Sự kiện thế giới động** (thú triều, dị bảo xuất thế...) | | ✅ | |
| **Vực 2** | | ✅ | |
| Luyện khí: chế tạo trang bị (rèn) + phân giải (GD-11) | ✅ (Alpha) | | |
| Luyện khí: cường hóa, tẩy luyện, thăng phẩm, khắc trận văn (GD-11) | | ✅ | |
| Chế tạo công pháp | | ✅ | |
| Tông môn (guild), giao dịch giữa người chơi | | ✅ | |
| Thiên kiếp (sự kiện đột phá) | | ✅ | |
| **Thức tỉnh / thay đổi thiên phú qua sự kiện** | | ✅ | |
| **Di sản: động phủ truyền thừa trở thành cơ duyên cho người khác** | | ✅ | |
| **Ma Uyên nhiều tầng, Thượng Cổ Tiên Phủ mở theo lịch** | | ✅ | |
| World boss | | ✅ | |
| **Linh thú / sủng vật** | | ✅ | |
| **Ngự kiếm phi hành** (2.5D làm dễ hơn: di chuyển nhanh trên bản đồ) | | ✅ | |
| **Động phủ cá nhân** qua cửa động trên bản đồ (GD-30) | | ✅ | |
| **Sơn môn tông môn trên bản đồ**, Uy vọng, Sơn Môn Đại Hội, phế tích (GD-30) | | ✅ | |
| Xây tự do theo lô ở bản đồ riêng "Hoang Vực" | | | ✅ |
| **Danh vọng phe phái / chính – tà – ma** (GD-14 §5.4c) | | ✅ | |
| **PvP dã ngoại có kiểm soát** (chế độ PK, sát nghiệp, Chấp Pháp, D-21) | | ✅ | |
| **Ma Vực** (hiểm địa PvP, chết thật — GD-28 §5.12) | | ✅ (Beta 2) | |
| PvP đấu trường (Luận Võ Đài) | | | ✅ |
| Shop / monetization | | ✅ (cơ bản) | ✅ |
| Mobile web (cảm ứng) | | | ✅ |

### 2.4. Các thứ cố ý KHÔNG làm (ít nhất đến sau launch)
- Không làm thế giới liền mạch thật sự (seamless streaming không màn chuyển). Bản đồ chia ô + chuyển nhanh là đủ.
- Không làm góc nhìn thứ ba tự do, không leo trèo/bơi lặn tự do (logic 2D).
- ~~Không làm open-world PvP tự do~~ → **thay bằng D-21 (2026-10-23):** PvP dã ngoại có kiểm soát (Beta, cờ server): chết do PvP chỉ là trọng thương, người vô tội không mất thọ, sát nghiệp → NPC Chấp Pháp truy sát ([GD-32](docs/design/31-pvp-infamy.md)). Hiểm địa vẫn không PvP; Ma Vực / Luận Võ Đài sau launch (Q9).
- Không tự viết engine vật lý / renderer: dùng Babylon.js; vật lý va chạm 2D tự viết đơn giản (không cần Havok cho logic).
- Không làm voice chat.
- **Không bán bằng tiền thật** bất cứ thứ gì giảm rủi ro cái chết hoặc kéo dài thọ nguyên (hồi sinh, thế thân phù, diên thọ đan, lượt roll thiên phú). Một mạng chỉ có ý nghĩa khi công bằng.
- Không làm nhiều class cứng ngay từ đầu: dùng **hệ thống công pháp linh hoạt** thay cho class (xem 4.10).
- Không làm cutscene điện ảnh phức tạp: dùng hội thoại + camera zoom đơn giản.

---

## 3. Cơ cấu team

### 3.1. Đề xuất team 8–10 người

| Vai trò | Số lượng | Trách nhiệm chính |
|---------|:--------:|-------------------|
| **Producer / Game Director** | 1 | Tầm nhìn, ưu tiên backlog, lịch trình, chốt quyết định. Có thể kiêm Game Designer chính. |
| **Game Designer (hệ thống)** | 1 | Tu luyện, combat, chế tác, kinh tế, cân bằng số liệu. |
| **Content Designer / Writer** | 1 | Cốt truyện, nhiệm vụ, **cơ duyên**, sự kiện thế giới, level design bản đồ. Vai trò quan trọng vì định hướng là "trải nghiệm phong phú". |
| **Client Developer (Babylon.js/TS)** | 2 | Rendering 2.5D, combat client, UI, netcode phía client, **map editor**, tối ưu hiệu năng. |
| **Server Developer (Java/Spring)** | 2 | Game server realtime, API, DB, bảo mật, chống hack. 1 người kiêm DevOps. |
| **3D Artist (low-poly)** | 1 | Nhân vật, quái, **bộ kit môi trường dạng module**, trang bị (Blender → glTF). |
| **Technical Artist / Animator / VFX** | 1 | Animation, hiệu ứng kỹ năng (VFX là "điểm sáng" của đồ họa 2.5D), shader toon, tối ưu asset. |
| **2D/UI Artist** | 0.5–1 | UI/UX, icon (rất nhiều: vật phẩm, đan dược, công pháp), concept art. Có thể thuê ngoài. |
| **QA** | 0–1 | Giai đoạn đầu dev tự test; từ Alpha cần 1 QA. |

> So với v0.1: bớt 1 vị trí 3D Artist nhờ chuyển sang 2.5D low-poly, thêm 1 **Content Designer**. Khối lượng nội dung (bản đồ, nhiệm vụ, cơ duyên) mới là thứ quyết định "độ phong phú".

### 3.2. Thuê ngoài / dùng asset có sẵn
- **Âm thanh & nhạc:** thuê ngoài hoặc mua bản quyền (không cần full-time).
- **Asset 3D:** phong cách low-poly có rất nhiều asset pack chất lượng (Synty POLYGON, KayKit, Quaternius, Kenney: nhiều gói miễn phí/CC0), mua về rồi chỉnh màu/texture cho đồng nhất phong cách cổ phong. Kiểm tra kỹ license dùng cho web game thương mại.
- **Animation:** Mixamo cho prototype; animation combat chính phải tự làm hoặc mocap thuê ngoài.
- **AI tool:** dùng hỗ trợ concept art, texture, viết nội dung nhiệm vụ — luôn qua bước biên tập của người.

---

## 4. Tổng quan Game Design

> Phần này là tóm tắt. Chi tiết từng hệ thống sẽ nằm trong các tài liệu riêng (xem mục 8).

### 4.1. Thế giới & cốt truyện
- **Tên thế giới (tạm):** *Thiên Huyền Giới*.
- **Bối cảnh:** Thiên địa linh khí suy kiệt sau một trận đại chiến thượng cổ giữa các tiên nhân. Các tông môn tranh giành những mạch linh khí cuối cùng. Phong ấn cổ đang yếu dần, ma tộc trỗi dậy.
- **Nhân vật chính:** người chơi xuất thân phàm nhân, vô tình nhận được một *mảnh ngọc giản cổ* (cơ chế cốt truyện giải thích vì sao người chơi có thể "chế tạo công pháp" — thiên phú đặc biệt).
- **Cấu trúc cốt truyện:** chia theo cảnh giới. Mỗi cảnh giới = 1 chương, mở ra vùng đất mới.

| Chương | Cảnh giới | Vùng đất | Chủ đề |
|--------|-----------|----------|--------|
| 1 | Luyện Khí | Thanh Vân Trấn & núi rừng phụ cận | Bước vào tiên đồ, gia nhập tông môn |
| 2 | Trúc Cơ | Vạn Thú Sơn Mạch | Thí luyện tông môn, bí mật ngọc giản |
| 3 | Kim Đan | Huyết Sa Hoang Mạc | Ma tộc xuất hiện, tông môn nội đấu |
| 4+ | Nguyên Anh... | (mở rộng sau launch) | |

**Tài liệu cần viết:** World Bible (địa lý, lịch sử, thế lực, nhân vật), Story Outline theo chương, Quest Design Doc.

### 4.2. Lối chơi cốt lõi: Một mạng · Thọ nguyên · Luân hồi (trụ cột P1)

> *"Mỗi nhân vật là một kiếp tu tiên. Chết là hết, nhưng đạo không mất."*

**Vòng lặp cốt lõi của một kiếp:**
```
 ┌─► TẠO NHÂN VẬT: roll thiên phú, linh căn, chọn xuất thân  (cơ duyên đầu tiên)
 │        ▼
 │   THÁM HIỂM & TÌM KIẾM: cơ duyên, linh thảo, khoáng thạch, đánh quái (tu vi có giới hạn)
 │        ▼
 │   CHẾ TÁC: luyện đan, luyện khí, chế công pháp (kết quả ngẫu nhiên có kiểm soát)
 │        ▼
 │   MẠO HIỂM HIỂM ĐỊA ☠: vật phẩm hiếm để đột phá  ⇄  nguy cơ chết thật
 │        ▼
 │   ĐỘT PHÁ CẢNH GIỚI: tăng thọ nguyên, mở vùng đất mới, thức tỉnh thiên phú
 │        ▼  (lặp lại cho tới khi...)
 │   KẾT THÚC KIẾP: tọa hóa (hết thọ) / tử vong (hiểm địa, thiên kiếp) / phi thăng
 │        ▼
 └── LUÂN HỒI: tổng kết kiếp → điểm Luân Hồi + di sản → kiếp mới có thêm LỰA CHỌN
```

**Nguyên tắc thiết kế:**
1. **Cái chết phải có ý nghĩa nhưng không vô lý:** người chơi luôn *biết* khi nào mình đang đặt cược mạng sống (hiểm địa, đột phá cảnh giới cao, đại hạn sắp tới). Không bao giờ chết thật vì một con quái thường ở vùng an toàn.
2. **Mất nhân vật không có nghĩa là mất tất cả:** tiến trình tài khoản (luân hồi, kho, cosmetic, codex, tông môn) được giữ lại. Kho đồ và động phủ của kiếp trước bị **phong ấn** chứ không mất: kiếp sau đánh bại **tàn hồn của chính mình** (theo từng tầng cảnh giới) hoặc **thú canh cửa** để lấy lại — chỉ đồ đang mang trên người là rớt (D-20, [GD-26 §5.15](docs/design/03-lifecycle.md)).
3. **Kiếp sau mở thêm *lựa chọn*, không cộng thêm *sức mạnh thô*:** thêm xuất thân, thiên phú, công thức, thay vì cộng chỉ số. Người chơi mới không bị bỏ xa quá mức.
4. **Thời gian là tài nguyên:** thọ nguyên vừa là "đồng hồ đếm ngược" tạo động lực, vừa là "tiền tệ" để đổi lấy sức mạnh (bế quan, bí pháp đốt thọ).

**Ba cách kết thúc một kiếp:**

| Kết cục | Nguyên nhân | Hệ quả |
|---------|-------------|--------|
| **Tọa hóa** | Hết thọ nguyên | Kết thúc êm đẹp: nhận đủ điểm Luân Hồi, được chọn di sản đầy đủ, có thời gian chuẩn bị ("đại hạn sắp tới") |
| **Thân tử đạo tiêu** | Chết trong hiểm địa, thất bại thiên kiếp cấp cao | Đồ mang theo rơi tại chỗ, điểm Luân Hồi giảm; kho đồ / động phủ **phong ấn**, kiếp sau đánh bại tàn hồn / thú canh cửa để lấy lại (D-20) |
| **Phi thăng** *(end-game, sau launch)* | Đạt cảnh giới tối đa và vượt Phi Thăng Kiếp | Vinh danh vĩnh viễn trên Tiên Bảng, phần thưởng luân hồi lớn nhất |

**Chết ở vùng thường không phải chết thật mà là "trọng thương":** hồi sinh tại điểm an toàn gần nhất, **mất 1–5 năm thọ nguyên** (theo cảnh giới), giảm nhẹ tu vi, suy yếu 10 phút. Như vậy cái chết nào cũng có giá, nhưng chỉ hiểm địa và các thử thách được báo trước mới lấy mạng nhân vật.

#### Thọ nguyên

| Cảnh giới | Thọ nguyên tối đa (năm) |
|-----------|:-----------------------:|
| Phàm nhân (bắt đầu ở tuổi 16) | 80 |
| Luyện Khí | 120 |
| Trúc Cơ | 250 |
| Kim Đan | 500 |
| Nguyên Anh | 1.000 |
| Hóa Thần | 2.000 |

**Thời gian trôi (tham số, cần cân bằng qua playtest):**
- Tuổi nhân vật chạy theo **đồng hồ riêng**, tách khỏi chu kỳ ngày/đêm của thế giới.
- **Chỉ trôi khi online và hoạt động:** đề xuất 1 giờ chơi ≈ 1 năm tuổi. Ở thành trấn hoặc AFK lâu, thời gian trôi chậm (×0.25) để không phạt việc giao lưu, giao dịch.
- **Offline không già đi**, để không phạt người bận rộn. Ngoại lệ: người chơi *chủ động* chọn **bế quan offline** để đổi thọ nguyên lấy tu vi.
- **Hành động tiêu thọ nguyên trực tiếp:** bế quan (đổi năm lấy tu vi), luyện đan/khí cao cấp (1–3 năm), trọng thương, thất bại đột phá, **bí pháp liều mạng** (đốt thọ nguyên lấy sức mạnh tạm thời).
- **Kéo dài thọ nguyên:** đột phá cảnh giới (nguồn chính), Diên Thọ Đan và linh quả (hiệu quả giảm dần mỗi lần dùng), cơ duyên, thiên phú.
- **Tiết tấu mục tiêu:** kiếp đầu tiên của người chơi trung bình kéo dài **40–80 giờ chơi** và đạt Trúc Cơ – Kim Đan. Người chơi giỏi/may mắn có thể sống nhiều tháng.
- **Đại hạn:** khi thọ nguyên còn < 10%, nhân vật nhận trạng thái "Đại hạn sắp tới" và mở chuỗi nhiệm vụ tìm diên thọ / chuẩn bị di sản. Đây là chương cuối kịch tính của mỗi kiếp.

#### Luân hồi: tiến trình cấp tài khoản

- **Điểm Luân Hồi** = f(cảnh giới đạt được, số năm đã sống, cơ duyên đã gặp, thành tựu, cách kết thúc kiếp).
- **Dùng điểm Luân Hồi để mở:** thêm lượt roll thiên phú, khóa dòng khi roll, xuất thân mới, thiên phú mới vào pool, ô Tàng Bảo Các, **Ký ức tiền kiếp** (bắt đầu kiếp mới với 1 đan phương / khí phổ đã thông thạo).
- **Giữ vĩnh viễn qua các kiếp:** cosmetic, danh hiệu, Vạn Giới Lục (codex), bạn bè, tư cách thành viên tông môn.
- **Tàng Bảo Các (kho tài khoản):** ít ô (3–6, mở rộng bằng điểm Luân Hồi), đồ gửi vào được giữ qua các kiếp. Đồ có **yêu cầu cảnh giới** khi dùng, nên không phá cân bằng đầu game.
- **Di sản / Động phủ truyền thừa (Beta):** khi tọa hóa, người chơi để lại một động phủ chứa 1–3 vật phẩm/công pháp tự chọn kèm lời nhắn. Động phủ này **trở thành cơ duyên cho người chơi khác** tìm thấy ngẫu nhiên trên thế giới (hoặc cho chính đời sau, nếu có thiên phú "Túc Tuệ"). Đây là nội dung do người chơi tạo ra, rất hợp với team nhỏ.
- **Bia Vạn Cổ:** lưu danh mọi kiếp (tên, xuất thân, thiên phú, cảnh giới, tuổi thọ, cách kết thúc). Có bảng xếp hạng và trang "tiểu sử kiếp này" chia sẻ được lên mạng xã hội.

> **Vì sao lối chơi này hợp với team nhỏ:** roguelite tăng mạnh khả năng chơi lại. Cùng một nội dung nhưng chơi lại với thiên phú, xuất thân và cơ duyên khác, nên nội dung team làm ra "dùng được lâu hơn". Một mạng cũng là kênh tiêu hao vật phẩm lớn nhất, giúp kinh tế MMO lành mạnh.

### 4.3. Thiên phú & Xuất thân

> Tạo nhân vật chính là **lần roll cơ duyên đầu tiên**, và cũng là khoảnh khắc người chơi hào hứng nhất.

**Quy trình tạo nhân vật:**
1. **Chọn xuất thân** (mở dần bằng Luân Hồi). Xuất thân quyết định điểm bắt đầu, đồ khởi đầu và một số cơ duyên riêng.

| Xuất thân | Đặc điểm | Mở khóa |
|-----------|----------|---------|
| Sơn thôn thiếu niên | Cân bằng, dễ chơi | Mặc định |
| Dược đồng | Biết sẵn 2 đan phương, hiểu linh thảo | Mặc định |
| Thợ rèn tiểu tử | Bonus luyện khí, căn cốt tốt | Mặc định |
| Thế gia công tử | Nhiều linh thạch & trang bị khởi đầu, khí vận thấp, có kẻ thù gia tộc | Luân Hồi |
| Cô nhi lưu lạc | Khởi đầu nghèo, khí vận rất cao | Luân Hồi |
| Tông môn tạp dịch | Bắt đầu trong tông môn, có nhiệm vụ nội môn | Luân Hồi |
| Phế vật bị trục xuất | Linh căn kém nhưng mang 1 thiên phú ẩn cấp cao | Luân Hồi (hiếm) |

2. **Roll thuộc tính bẩm sinh:**

| Thuộc tính | Ảnh hưởng |
|------------|-----------|
| **Linh căn** (số hệ + độ tinh khiết) | Thiên linh căn (1 hệ, cực hiếm, tu luyện ×2) · Song linh căn · Tam linh căn (phổ biến) · Tứ/Ngũ linh căn (chậm nhưng dùng được mọi hệ) · Biến dị: Lôi, Băng, Phong (cực hiếm) |
| **Căn cốt** | Khí huyết, phòng ngự, tốc độ hồi phục trọng thương |
| **Ngộ tính** | Tốc độ lĩnh ngộ công pháp, chất lượng chế công pháp |
| **Khí vận** | Tỷ lệ gặp cơ duyên, vật phẩm hiếm, may mắn khi chế tác |
| **Thần thức** | Tầm quét, tỷ lệ thành công luyện đan/khí |
| **Thọ nguyên gốc** | ± vài chục năm so với chuẩn |

3. **Roll 3 thiên phú đặc biệt** từ pool theo độ hiếm: Phổ thông → Hiếm → Sử thi → Truyền thuyết → Nghịch thiên. Có cả **thiên phú hai mặt** (lợi kèm hại), tạo build thú vị.

| Thiên phú (ví dụ) | Độ hiếm | Hiệu ứng |
|-------------------|---------|----------|
| Đan Tâm | Hiếm | +15% tỷ lệ đan thượng phẩm |
| Trường Sinh Thể | Sử thi | +30% thọ nguyên tối đa |
| Đoản Mệnh Kỳ Tài | Sử thi (hai mặt) | −30% thọ nguyên, +40% ngộ tính |
| Kiếm Tâm Thông Minh | Truyền thuyết | Kiếm pháp thuần thục ×2, kiếm khí xuyên thấu |
| Thiên Sát Cô Tinh | Truyền thuyết (hai mặt) | +30% sát thương khi đi một mình, đồng đội bị −khí vận |
| Thiên Đạo Sủng Nhi | Nghịch thiên | Khí vận cực cao, nhưng thiên kiếp mạnh gấp đôi |
| Túc Tuệ | Hiếm | Giữ lại 1 ký ức tiền kiếp; có thể tìm thấy động phủ truyền thừa của chính mình |
| Ma Chủng | Sử thi (ẩn) | Mở nhánh cơ duyên ma đạo |

4. **Roll lại có giới hạn:** mặc định 3 lượt, nhận thêm từ Luân Hồi; có thể **khóa 1 dòng** khi roll lại (mở bằng Luân Hồi). **Không bán lượt roll bằng tiền thật.**

**Thay đổi thiên phú trong kiếp (qua sự kiện đặc biệt):**

| Sự kiện | Ảnh hưởng |
|---------|-----------|
| **Tẩy tủy phạt cốt** (linh dịch, thiên tài địa bảo từ cơ duyên/hiểm địa) | Nâng cấp linh căn (vd. Tứ → Tam linh căn), tăng căn cốt |
| **Thức tỉnh khi đột phá cảnh giới lớn** | Chọn 1 trong 3 thiên phú mới (kiểu roguelike) |
| **Dung hợp dị hỏa / huyết mạch yêu thú** | Nhận thiên phú mạnh, có tỷ lệ thất bại → trọng thương / tổn thọ |
| **Tâm ma** (thất bại đột phá, trọng thương nhiều lần) | Nhận thiên phú tiêu cực; gỡ bằng đan dược / nhiệm vụ tĩnh tâm |
| **Đọa ma** (lựa chọn trong cơ duyên, sau launch) | Đổi linh căn thành ma căn: sức mạnh lớn, NPC chính phái thù địch |

- **Thiên phú mở nội dung:** một số cơ duyên chỉ xuất hiện với thiên phú / xuất thân nhất định, nên mỗi kiếp thấy những câu chuyện khác nhau.

### 4.4. Hệ thống tu luyện (Cảnh giới)

> **Triết lý:** *"Tu tiên là tranh đoạt tài nguyên với trời đất."* Đánh quái chỉ là phần nhỏ. Tiến bộ chủ yếu đến từ **tìm kiếm vật phẩm, luyện đan và chuẩn bị đột phá** (trụ cột P2).

**Cấu trúc:** Cảnh giới lớn → Tầng nhỏ.

| Cảnh giới | Số tầng | Thọ nguyên tối đa | Mở khóa gameplay |
|-----------|:-------:|:-----------------:|------------------|
| Luyện Khí | 9 tầng | 120 | Combat cơ bản, 4 ô kỹ năng, hiểm địa nhất giai |
| Trúc Cơ | Sơ / Trung / Hậu / Viên Mãn | 250 | Luyện đan cấp 2, ô kỹ năng thứ 5, khinh công (né lướt 2 lần), chế công pháp Hoàng – Huyền (GD-08), ngự kiếm phi hành (Beta), hiểm địa nhị giai |
| Kim Đan | Sơ / Trung / Hậu / Viên Mãn | 500 | Pháp bảo bản mệnh, chế công pháp phẩm Địa, ô tuyệt kỹ, hiểm địa tam giai |
| Nguyên Anh | ... | 1.000 | Nguyên Anh xuất khiếu (phân thân hỗ trợ chiến đấu), mở Vực 4 (sau launch) |
| Hóa Thần+ | ... | 2.000+ | Mở rộng, hướng tới Phi Thăng |

**Nguồn tu vi và tỷ trọng mục tiêu:**

| Nguồn | Tỷ trọng | Giới hạn / Đặc điểm |
|-------|:--------:|---------------------|
| **Đan dược** | ~35–40% | Nguồn chính. Tích **đan độc**: dùng nhiều một loại thì hiệu quả giảm, phải dùng đan phẩm cao hơn / đa dạng hoặc giải độc |
| **Đả tọa / Bế quan** | ~20–25% | Tiêu **thời gian = thọ nguyên**. Hiệu quả theo linh căn, công pháp, nơi đả tọa (linh mạch, động phủ) |
| **Thiên tài địa bảo, cơ duyên** | ~15–20% | Ngẫu nhiên, phần lớn từ hiểm địa |
| **Đánh quái** | ~15% | **Có giới hạn:** chỉ quái ngang hoặc trên cảnh giới mới cho tu vi; giảm dần theo số lượng trong ngày; **không phá được bình cảnh**. Giá trị chính của đánh quái là vật liệu, linh thạch, thuần thục công pháp |
| Nhiệm vụ | ~5% | Chủ yếu nhiệm vụ chính |

**Bình cảnh:** tầng cuối mỗi cảnh giới lớn có bình cảnh. Tu vi đã đầy sẽ không tăng nữa cho đến khi đột phá. Muốn qua bình cảnh phải có vật phẩm.

**Đột phá cảnh giới lớn (khoảnh khắc lớn nhất của mỗi kiếp):**
1. **Tu vi viên mãn** (đã chạm bình cảnh).
2. **Đan đột phá** (Trúc Cơ Đan, Kết Kim Đan, Hóa Anh Đan...): nguyên liệu chính **chỉ có ở hiểm địa hoặc cơ duyên**. Tự luyện hoặc mua của luyện đan sư.
3. **Vật phẩm phụ trợ** (tùy chọn): linh dịch, pháp bảo hộ thân, trận pháp tụ linh, **hộ pháp** (người chơi khác canh giữ).
4. **Tỷ lệ thành công luôn hiển thị trước** = f(phẩm chất đan, linh căn, ngộ tính, tâm cảnh, vật phẩm phụ trợ, địa điểm, thiên phú).
5. **Thử thách:** *Tâm ma* (cơ duyên lựa chọn, đúng/sai ảnh hưởng tỷ lệ) và *Thiên Kiếp* ở cảnh giới lớn (instance solo né sét theo pattern, có thể dùng pháp bảo đỡ kiếp).

**Hậu quả thất bại (tăng dần theo cảnh giới, luôn cảnh báo trước):**
| Cảnh giới đột phá | Thất bại |
|-------------------|----------|
| Trúc Cơ | Mất đan, tổn 5 năm thọ nguyên, suy yếu |
| Kim Đan | Trọng thương, tổn 10–20 năm thọ nguyên, có thể nhận tâm ma |
| Nguyên Anh trở lên | Thiên kiếp có thể **chết thật**. Người chơi chọn đánh cược hay chuẩn bị thêm |

- Tầng nhỏ (không phải bình cảnh) tự động lên khi đủ tu vi.
- **Tâm cảnh** (chỉ số ẩn hiển thị dạng mô tả): giảm khi thất bại, trọng thương, tâm ma; tăng khi đả tọa, ngộ đạo ở di tích. Ảnh hưởng tỷ lệ đột phá.

**Chỉ số nhân vật (sơ bộ):**
| Chỉ số | Ý nghĩa |
|--------|---------|
| Khí Huyết (HP) | Máu |
| Linh Lực (MP) | Năng lượng dùng kỹ năng |
| Thể Lực (Stamina) | Dùng cho né, chạy nhanh, đỡ đòn, tự hồi nhanh |
| Công kích / Pháp công | Sát thương vật lý / pháp thuật |
| Phòng ngự / Pháp kháng | Giảm sát thương |
| Thân pháp | Tốc độ di chuyển, tốc độ hồi thể lực |
| Bạo kích / Bạo thương | Tỷ lệ / hệ số chí mạng |
| Thần thức | Tầm quét, tỷ lệ thành công chế tác |
| Ngộ tính | Tốc độ lĩnh ngộ công pháp, chất lượng chế công pháp |
| Căn cốt | Khí huyết, hồi phục trọng thương |
| Khí vận | Cơ duyên, rơi đồ hiếm, may mắn chế tác |
| Tuổi / Thọ nguyên | Tuổi hiện tại / tối đa |

### 4.5. Hiểm địa ☠ (vùng chết thật)

> Nơi duy nhất (cùng với thiên kiếp cấp cao) có thể **mất nhân vật**, và cũng là nơi duy nhất có **vật phẩm hiếm để nâng cao cảnh giới**.

**Vòng lặp "vào – lấy – thoát" (extraction):**
1. **Vào:** qua cửa hiểm địa trên bản đồ thường; màn cảnh báo rõ ràng (*"Sinh tử tự chịu"*), hiển thị cấp độ nguy hiểm và yêu cầu cảnh giới. Người chơi tự chọn mang theo gì; chết là rơi hết đồ đang mang.
2. **Lấy:** vật phẩm nhặt được nằm trong **túi hiểm địa** và **chưa thực sự thuộc về bạn**.
3. **Thoát:** đi tới **lối thoát** (truyền tống trận cố định hoặc xuất hiện ngẫu nhiên) hoặc dùng **Truyền Tống Phù** (hiếm, kích hoạt 10 giây, bị ngắt khi trúng đòn). Chỉ khi thoát ra, đồ mới được chuyển vào túi thường.
4. **Áp lực tăng dần:** linh khí bạo loạn, cấm chế thu hẹp, quái mạnh dần theo thời gian, buộc người chơi chọn giữa *"tham thêm"* và *"về ngay"*.

**Phân cấp:**
| Cấp | Yêu cầu | Đặc trưng | Phần thưởng tiêu biểu |
|-----|---------|-----------|------------------------|
| Nhất giai | Luyện Khí 5+ | Ngắn (10–15 phút), lối thoát dễ tìm | Nguyên liệu Trúc Cơ Đan, linh thảo trăm năm |
| Nhị giai | Trúc Cơ | Quái tinh anh, bẫy trận pháp | Mảnh ngọc giản Huyền phẩm, khoáng thạch hiếm, linh dịch tẩy tủy |
| Tam giai | Kim Đan | Boss lang thang, lối thoát ngẫu nhiên | Nguyên liệu Kết Kim Đan, Diên Thọ Quả, trứng linh thú |
| Tứ – Ngũ giai | Nguyên Anh+ (sau launch) | | |

**Các loại hiểm địa (ví dụ):**
- **Cổ Chiến Trường:** oán linh, binh khí cổ, mảnh công pháp thất truyền.
- **Vạn Độc Đầm:** độc khí làm mất máu liên tục, linh thảo độc quý.
- **Ma Uyên** (Beta): nhiều tầng, càng xuống sâu càng hiếm, mỗi tầng có lối thoát riêng.
- **Thượng Cổ Tiên Phủ** (Beta): mở theo lịch mỗi tuần trong 2 giờ, nhiều nhóm cùng vào tranh bảo vật.

**Cơ chế giảm chết vô lý và tạo tinh thần đồng đội:**
- **Hấp hối:** hết máu thì ngã xuống 20–30 giây, đồng đội có thể cứu. Hết giờ mới chết thật. Đi solo vẫn được nhưng rủi ro cao hơn hẳn.
- **Di hài:** người chết để lại di hài chứa đồ tại chỗ. Đồng đội nhặt giúp (để trả lại hoặc giữ), sau một thời gian di hài tiêu tan.
- **Thế Thân Phù:** vật phẩm cực hiếm (từ cơ duyên / chế tạo cao cấp), đỡ 1 lần chết. **Không bán bằng tiền thật.**
- **Mất kết nối:** nhân vật vào trạng thái *quy tức* 60 giây (bất động, phòng ngự rất cao, quái mất mục tiêu dần). Xem thêm chính sách server ở 5.3.
- **MVP chỉ có PvE:** người chơi không thể tấn công nhau trong hiểm địa. **PvP hiểm địa (Ma Vực)** để sau launch và là lựa chọn riêng (Q9).
### 4.6. Thế giới mở & khám phá (trụ cột P5)

> Mục tiêu: mỗi bản đồ có **mật độ nội dung cao**. Người chơi đi bất kỳ hướng nào trong 1–2 phút đều gặp điều gì đó đáng chú ý.

**Mật độ nội dung chuẩn cho 1 bản đồ dã ngoại (để content designer làm checklist):**

| Thành phần | Số lượng / bản đồ | Ghi chú |
|------------|:-----------------:|---------|
| Khu quái thường | 4–6 | Mỗi khu 1 chủ đề, cấp độ tăng dần theo hướng đi |
| Tinh anh (elite) / tiểu boss | 1–2 | Spawn theo giờ, rơi đồ tốt |
| Điểm thu thập (linh thảo, khoáng mạch) | 15–30 | Một số chỉ xuất hiện ban đêm / khi mưa |
| Điểm cơ duyên tiềm năng | 3–5 | Nơi có thể kích hoạt kỳ ngộ (xem 4.7) |
| Bí mật (rương ẩn, hang ẩn, câu đố nhỏ) | 2–4 | Thưởng cho người tò mò |
| Bia đá / di tích (lore) | 1–2 | Mở nhật ký thế giới, cộng ngộ tính |
| NPC / nhiệm vụ phụ | 3–6 | |
| Linh mạch (điểm đả tọa tăng tốc tu luyện) | 1 | Có thể tranh chấp (sau launch) |
| Cửa vào bí cảnh / động phủ cổ | 0–1 | |
| Truyền tống trận | 1 | Kích hoạt khi tìm thấy |

**Hệ thống phục vụ khám phá:**
- **Bản đồ thế giới + fog of war:** mở dần, % khám phá mỗi bản đồ có phần thưởng mốc (50% / 100%).
- **Thần thức quét:** kỹ năng nhấn phím để "quét" xung quanh, làm nổi bật linh thảo/bí mật trong bán kính theo chỉ số Thần thức (cơ chế radar tiên hiệp, có hồi chiêu).
- **Nhật ký thế giới (Vạn Giới Lục):** sưu tầm quái, linh thảo, di tích, cơ duyên đã gặp → phần thưởng hoàn thành bộ sưu tập.
- **Ngày/đêm (1 ngày game = 2 giờ thực) & thời tiết:** quái ma đạo mạnh hơn về đêm, linh thảo hệ Thủy mọc khi mưa, sự kiện Lôi kiếp khi giông bão...
- **Ngự kiếm phi hành (Beta):** chế độ di chuyển nhanh trong bản đồ, bay qua địa hình và quái; camera zoom ra; không thể tương tác khi bay. 2.5D chỉ cần đổi độ cao hiển thị + bỏ va chạm địa hình → rẻ hơn nhiều so với 3D.

### 4.7. Cơ duyên (Kỳ ngộ) — tính năng đặc trưng

> Tinh thần tiên hiệp: *"Tu tiên dựa vào cơ duyên"*. Cùng một bản đồ, mỗi người chơi có trải nghiệm khác nhau.

- **Định nghĩa:** các sự kiện ngẫu nhiên, ngắn (30 giây – 5 phút), kích hoạt khi người chơi thỏa điều kiện tại một địa điểm.
- **Điều kiện kích hoạt (data-driven, kết hợp được):** vị trí, cảnh giới, linh căn, thời gian (ngày/đêm), thời tiết, vật phẩm đang mang, nhiệm vụ đã làm, thiện/ác, may mắn (ẩn) + tỷ lệ ngẫu nhiên + hồi chiêu.
- **Dạng nội dung:**
  - *Hội thoại lựa chọn:* gặp lão giả bí ẩn, chọn đáp án → nhận/mất thứ gì đó (phong cách Tale of Immortal).
  - *Chiến đấu:* linh thú bị thương đang bị truy sát → cứu hoặc đoạt nội đan.
  - *Giải đố nhỏ:* trận pháp cổ, sắp xếp đúng thứ tự ngũ hành.
  - *Instance nhỏ:* lạc vào động phủ cổ của tiền bối tọa hóa.
  - *Chuỗi cơ duyên:* nhiều bước rải trên nhiều bản đồ, dẫn tới công pháp/pháp bảo hiếm.
- **Phần thưởng:** mảnh ngọc giản (nguyên liệu chế công pháp), đan phương hiếm, linh căn biến dị, danh hiệu, linh thú.
- **Chia sẻ:** thông báo "Đạo hữu X vừa gặp cơ duyên Y" lên kênh thế giới (tùy độ hiếm) → tạo câu chuyện cộng đồng, kích thích khám phá.
- **Công cụ:** viết cơ duyên bằng **Ink** (soạn trong Inky) cho phần kể chuyện / lựa chọn, kèm YAML/JSON cho điều kiện kích hoạt và phần thưởng. Content designer tự thêm mà không cần dev; server chạy kịch bản bằng inkjs (xem 5.7, D-19). MVP: 20–30 cơ duyên; mục tiêu sau launch: 100+.

#### Cơ duyên xuyên suốt mọi lớp gameplay

Ngẫu nhiên là **trải nghiệm cốt lõi**, nhưng người chơi luôn có cách tác động lên xác suất:

| Lớp | Yếu tố ngẫu nhiên | Người chơi kiểm soát |
|-----|-------------------|----------------------|
| Tạo nhân vật | Linh căn, thuộc tính, thiên phú | Chọn xuất thân, số lượt roll, khóa dòng |
| Thám hiểm | Cơ duyên, linh thảo hiếm, sự kiện | Khí vận, thần thức quét, chọn nơi / giờ / thời tiết |
| Luyện đan | Phẩm chất, đan văn | Minigame hỏa hầu, đan lô, hỏa chủng, nguyên liệu tốt |
| Luyện khí | Phẩm cấp, thuộc tính phụ, dị tượng | Minigame rèn, khoáng thạch, khóa thuộc tính khi tẩy luyện |
| Chế công pháp | Hệ số, đột biến biến thức | Chọn mảnh ngọc giản, ngộ tính |
| Đột phá | Tỷ lệ thành công | Chuẩn bị đan, vật phẩm phụ trợ, hộ pháp, địa điểm |
| Thay đổi thiên phú | Thiên phú được đề xuất khi thức tỉnh | Chọn 1 trong 3, quyết định có mạo hiểm dung hợp hay không |

**Nguyên tắc ngẫu nhiên công bằng:**
- **Luôn hiển thị tỷ lệ** (hoặc mức mô tả rõ ràng) trước khi người chơi quyết định.
- **Cơ chế bảo hiểm (pity):** thất bại tích lũy kinh nghiệm (vd. luyện đan hỏng thì tăng "hỏa hầu tâm đắc", lần sau tỷ lệ cao hơn).
- Kỹ năng và sự chuẩn bị **dịch chuyển phân phối xác suất** nhưng không xóa bỏ may rủi.
- **RNG chạy trên server**, ghi log để kiểm toán; client không bao giờ quyết định kết quả.
- Chi tiết chính sách (phân loại roll, hiển thị tỷ lệ, pity chuẩn, khí vận, dữ liệu bí mật, Thiên Cơ Lục): [GD-29](docs/design/05-rng-fairness.md).

### 4.8. Sự kiện thế giới động (Beta)

| Sự kiện | Phạm vi | Mô tả |
|---------|---------|-------|
| **Dị bảo xuất thế** | 1 bản đồ, theo lịch + ngẫu nhiên | Cột sáng xuất hiện, người chơi tranh nhau tới, đánh bảo vật thú canh giữ |
| **Thú triều** | 1 bản đồ | Đợt quái tràn vào thôn làng, người chơi cùng phòng thủ, thưởng theo đóng góp |
| **Ma tu xâm lấn** | 1 vực | Ma tu chiếm bản đồ, đổi quái, cần đánh lui để mở lại truyền tống trận |
| **Linh khí bùng phát** | 1 bản đồ, 30 phút | Tốc độ tu luyện ×2 tại bản đồ đó, thu hút người chơi |
| **World boss** | Theo lịch | Boss lớn, cần nhiều tổ đội |

- Sự kiện thông báo trước trên bản đồ thế giới để người chơi kịp tập trung.
- Server có **bộ lập lịch sự kiện** (event scheduler) điều khiển bằng data.

### 4.9. Hệ thống chiến đấu

**Triết lý:** Action combat top-down hiện đại (kiểu Hades / Lost Ark / Diablo IV), server-authoritative, chịu được độ trễ mạng 50–150ms. Góc nhìn trên cao giúp người chơi **thấy rõ vòng cảnh báo trên mặt đất**, rất hợp với combat né chiêu.

**Điều khiển (PC: bàn phím + chuột):**
| Phím | Hành động |
|------|-----------|
| WASD | Di chuyển (tùy chọn: click chuột phải để di chuyển cho người quen MOBA/Diablo) |
| Con trỏ chuột | **Hướng nhắm**: nhân vật quay mặt và ra chiêu về phía con trỏ |
| Chuột trái | Đánh thường (combo 3–4 đòn) |
| Chuột phải | Đánh mạnh / tụ lực (giữ) |
| Space | Né lướt (dash, có i-frame ~0.25s, tốn thể lực) |
| Shift | Hộ thể / Phản đòn (parry: đỡ đúng thời điểm → choáng địch) |
| Q / E / R / F | Kỹ năng công pháp (thêm ô **V** từ Trúc Cơ, GD-07 SK-R-040) |
| T | Tuyệt kỹ (nạp bằng năng lượng chiến đấu) |
| 1–4 | Đan dược / vật phẩm nhanh |
| Tab | Kỹ năng chủ động của pháp bảo (GD-09 IT-R-024) |
| G | Thần thức quét (xem 4.6) |
| X | Tương tác, nhặt đồ |
| Con lăn | Zoom camera |

**Các dạng kỹ năng (hình học 2D đơn giản: dễ cho server tính toán):**
| Dạng | Hình vùng | Ví dụ |
|------|-----------|-------|
| Phóng (projectile) | Đường thẳng / hình tròn bay | Kiếm khí, hỏa cầu |
| Vùng tại con trỏ (ground AoE) | Hình tròn | Lôi phạt, băng trận |
| Quạt (cone) | Hình quạt trước mặt | Chém quét, phun lửa |
| Lướt (dash) | Đoạn thẳng | Kiếm độn, thuấn bộ |
| Quanh thân (self AoE) | Hình tròn quanh nhân vật | Hộ thể cương khí |
| Triệu hồi / đặt vật | Điểm | Phi kiếm tự động, trận kỳ |

**Cơ chế chính:**
- **Nhắm bằng chuột (skillshot) + hỗ trợ ngắm nhẹ:** đòn đánh thường tự hút nhẹ về địch gần hướng con trỏ; kỹ năng đi chính xác theo con trỏ.
- **Combo & animation cancel:** đánh thường có thể hủy vào né/kỹ năng tại các frame cho phép (định nghĩa trong data).
- **Hit reaction:** stagger (giật lùi), knockback (đẩy lùi: rất hợp góc nhìn trên cao), knockdown, stun. Quái có **thanh kiên định (Poise)**: đánh vỡ để gây choáng lớn.
- **Telegraph:** chiêu của boss/quái mạnh có vòng/hình cảnh báo đỏ trên mặt đất, thời gian báo trước ≥ 0.4s, giúp bù độ trễ mạng.
- **Game feel:** hit stop (khựng 50–80ms), rung camera, số sát thương bay lên, tia sáng khi trúng đòn. Ở góc nhìn xa, các yếu tố này quyết định cảm giác đã tay.
- **Hấp hối (trong hiểm địa):** hết máu thì ngã xuống 20–30 giây, đồng đội có thể cứu (xem 4.5).
- **Bí pháp liều mạng:** một số công pháp cho phép **đốt thọ nguyên** để bùng nổ sức mạnh tạm thời (vd. đốt 5 năm → +50% sát thương trong 20 giây). Lựa chọn thoát hiểm đậm chất tiên hiệp.
- **Nguyên tố tương khắc:** Ngũ hành tương sinh tương khắc (Kim khắc Mộc, Mộc khắc Thổ...) → bonus/penalty sát thương, combo nguyên tố đơn giản.
- **Vai trò trong nhóm:** không có class cứng, nhưng công pháp nghiêng về Công / Thủ / Hỗ trợ.

**Vũ khí (MVP chọn 2–3 loại để kiểm soát khối lượng animation):**
| Vũ khí | Phong cách | MVP? |
|--------|-----------|:----:|
| Kiếm | Cân bằng, combo nhanh | ✅ |
| Pháp trượng / Phiến (quạt) | Tầm xa, pháp thuật | ✅ |
| Song đao / Quyền | Cận chiến, tốc độ cao | Beta |
| Cầm (đàn) | Hỗ trợ / buff | Sau launch |

**Kỹ thuật mạng cho combat (chi tiết trong TDD):**
- **Mọi va chạm tính trên mặt phẳng 2D** (hình tròn, hình chữ nhật xoay, hình quạt, đoạn thẳng). Đơn giản, nhanh, client và server dùng chung công thức.
- Di chuyển: client prediction + server reconciliation.
- Tấn công: client phát animation ngay (cảm giác mượt), server xác thực hit với **lag compensation** (tua lại vị trí mục tiêu theo RTT, giới hạn ~200ms).
- Quái AI chạy hoàn toàn trên server; tìm đường bằng lưới/navmesh 2D.

### 4.10. Kỹ năng & Công pháp

**Công pháp** thay thế cho hệ thống class:
- **Công pháp tâm pháp (chính):** 1 cái, quyết định chỉ số tăng trưởng + nội tại (passive) chính. Ví dụ: *Thanh Mộc Trường Sinh Quyết* (hồi máu, hỗ trợ), *Liệt Hỏa Kiếm Điển* (sát thương bùng nổ).
- **Võ kỹ / Pháp thuật:** các chiêu chủ động gắn vào 4 ô kỹ năng + 1 tuyệt kỹ.
- **Phẩm cấp công pháp:** Hoàng → Huyền → Địa → Thiên (mỗi cấp chia Hạ/Trung/Thượng phẩm).
- **Độ thuần thục:** dùng nhiều → lên cấp thuần thục (Nhập Môn → Tiểu Thành → Đại Thành → Viên Mãn) → mở hiệu ứng phụ.

**Chế tạo công pháp (tính năng đặc trưng, trụ cột P4):**
- Ý tưởng: hệ thống **module hóa kỹ năng**.
  - **Hạch tâm (Core):** dạng chiêu — phóng ra (projectile), vùng (AoE), lướt chém (dash), triệu hồi, buff.
  - **Thuộc tính (Element):** Kim/Mộc/Thủy/Hỏa/Thổ/Lôi... → hiệu ứng đi kèm (bỏng, đóng băng, tê liệt...).
  - **Biến thức (Modifier, 1–3 cái):** nhiều mục tiêu, nổ lan, xuyên thấu, hút máu, giảm hồi chiêu...
- Nguyên liệu: **mảnh ngọc giản** rơi từ quái/bí cảnh, mỗi mảnh chứa 1 hạch tâm / thuộc tính / biến thức.
- Kết quả: kỹ năng mới với chỉ số tính theo công thức + yếu tố ngẫu nhiên (ảnh hưởng bởi Ngộ tính, Khí vận); xác suất nhỏ **đột biến** ra biến thức hiếm không có trong nguyên liệu. Người chơi tự đặt tên công pháp → có thể chia sẻ / bán ngọc giản cho người khác.
- **Rủi ro cân bằng cao** → cần sandbox + giới hạn ngân sách sức mạnh (power budget) cho mỗi công pháp. VFX dùng hệ thống ghép tham số (màu theo nguyên tố, hình dạng theo hạch tâm) thay vì làm riêng từng chiêu.

### 4.11. Trang bị

**Ô trang bị:** Vũ khí, Mũ (Quan), Áo (Bào), Đai, Giày (Hài), Nhẫn ×2, Ngọc bội, **Pháp bảo** (ô đặc biệt — có kỹ năng chủ động riêng).

**Phẩm cấp:** Phàm khí → Linh khí → Bảo khí → Tiên khí → (Thần khí — sau launch). Mỗi phẩm có màu riêng (trắng → xanh lá → xanh dương → tím → vàng/cam).

**Thuộc tính trang bị:**
- Chỉ số cơ bản (cố định theo loại).
- Thuộc tính phụ ngẫu nhiên (1–4 dòng theo phẩm cấp).
- **Hiệu ứng bộ (set)** cho đồ bí cảnh.
- **Khắc trận văn (socket)** — gắn linh thạch/trận văn.

**Tiến triển trang bị (liên kết với Luyện Khí):** cường hóa (+1 → +15), tẩy luyện (re-roll thuộc tính phụ), thăng phẩm, phân giải lấy nguyên liệu.

**Ngoại hình (cosmetic):** tách biệt khỏi chỉ số — nguồn monetization chính, không pay-to-win.

### 4.12. Luyện Đan (Alchemy)
- **Đầu vào:** Đan phương (công thức, học từ NPC/rơi ra) + Linh thảo (thu thập trên bản đồ dã ngoại, có loại chỉ mọc theo giờ/thời tiết) + Đan lô (lò luyện — vật phẩm có cấp) + Hỏa chủng (lửa, tùy chọn).
- **Quá trình:** minigame ngắn 20–40 giây — **khống chế hỏa hầu**: giữ nhiệt độ trong vùng tối ưu (thanh trượt dao động), cho nguyên liệu vào đúng thời điểm. Có tùy chọn **luyện tự động** (tỷ lệ thành công thấp hơn) để không bắt người chơi lặp minigame mãi.
- **Đầu ra:** Đan dược với phẩm chất (Hạ/Trung/Thượng/Cực phẩm) + có thể có **đan văn** (chỉ số thưởng). Thất bại → phế đan (vẫn có giá trị nhỏ).
- **Loại đan:** hồi phục (HP/MP), tăng tu vi, đột phá, buff chiến đấu tạm thời, giải độc, tăng chỉ số vĩnh viễn (giới hạn số lần dùng).
- **Cấp Luyện Đan Sư:** nhất phẩm → cửu phẩm, lên cấp theo kinh nghiệm luyện.
- **Tiêu tốn thời gian:** đan cao cấp (đan đột phá, diên thọ đan) cần luyện lâu → **tiêu 1–3 năm thọ nguyên**. Người chơi phải cân nhắc tự luyện hay mua của luyện đan sư khác, tạo ra vai trò kinh tế cho nghề luyện đan.
- **Pity:** luyện hỏng tích "hỏa hầu tâm đắc", tăng tỷ lệ thành công lần sau với cùng đan phương.

### 4.13. Luyện Khí (Forging)
- **Chế tạo:** Bản vẽ (khí phổ) + Khoáng thạch + Vật liệu quái → trang bị. Minigame **rèn** (đập búa theo nhịp) hoặc tự động.
- **Ngẫu nhiên có kiểm soát:** phẩm cấp và thuộc tính phụ ra ngẫu nhiên; minigame tốt + khoáng thạch tốt + khí vận đẩy xác suất lên phẩm cao. Xác suất nhỏ xuất hiện **dị tượng**: trang bị sinh khí linh, có thuộc tính đặc biệt và trưởng thành theo chủ.
- **Cường hóa / Tẩy luyện / Khắc trận văn / Thăng phẩm** như mục 4.11.
- **Luyện chế pháp bảo:** pháp bảo bản mệnh (Kim Đan trở lên) có thể "ôn dưỡng" lên cấp theo người chơi.
- **Cấp Luyện Khí Sư** tương tự luyện đan.

### 4.14. Kinh tế
- **Tiền tệ:**
  - *Linh Thạch* — tiền chính kiếm trong game, giao dịch được. Hạ / Trung / Thượng phẩm chỉ là cách hiển thị (1 Trung = 100 Hạ, 1 Thượng = 10.000 Hạ; GD-13 EC-R-010).
  - *Tông môn cống hiến* — đổi đồ trong tông môn, không giao dịch.
  - *Tiên Ngọc* — tiền nạp (premium), chỉ dùng cho cosmetic / tiện ích.
- **Faucet (nguồn tiền vào):** quái, nhiệm vụ, bán đồ NPC.
- **Sink (nguồn tiền ra):** phí cường hóa, phí học đan phương, sửa đồ, phí giao dịch (thuế 5–10% sàn đấu giá), phí dịch chuyển.
- **Giao dịch:** sàn đấu giá (Vạn Bảo Lâu) + giao dịch trực tiếp. Đồ ràng buộc (bound) với đồ cao cấp nhất để giữ giá trị.
- **Theo dõi kinh tế:** dashboard thống kê tổng linh thạch trong hệ thống, giá trung bình — bắt buộc trước khi mở Beta.
- **Một mạng là kênh tiêu hao lớn:** đồ **mang theo** của nhân vật chết rơi tại chỗ (hiểm địa) hoặc tiêu tan; kho đồ bị **phong ấn** (Di Tàng, D-20) và chỉ lấy lại được khi kiếp sau đánh bại tàn hồn + nộp phí giải ấn, phần chưa lấy tiêu tán khi kiếp sau kết thúc. Theo dõi tỷ lệ hố tiền ở Alpha (GD-13).
- **Thọ nguyên là hàng hóa quý nhất:** Diên Thọ Đan, linh quả, nguyên liệu đan đột phá sẽ là mặt hàng giá trị nhất trên sàn. Luyện đan sư và người chuyên đi hiểm địa có vai trò kinh tế rõ ràng.
- **Chống lách luật chuyển đồ** (dùng tài khoản phụ chuyển đồ cho nhân vật mới): chấp nhận ở mức độ nhất định vì đồ có yêu cầu cảnh giới; theo dõi giao dịch bất thường.

### 4.15. Xã hội
- **Tổ đội:** tối đa 5 người; rơi đồ **cá nhân** nên không có luật chia đồ (GD-12, GD-14).
- **Hai loại tông môn:** *Thanh Vân Tông* (tông môn NPC, mọi người chơi gia nhập ở chương 1, có bậc và Bảo Khố) và *tông môn tự lập* do người chơi lập (Beta) (GD-14).
- **Tông môn (Guild):** tạo khi đạt Trúc Cơ; cấp tông môn, kho chung, nhiệm vụ tông môn, công trình (Đan Phòng, Luyện Khí Các → buff chế tác cho thành viên).
- **Chat:** kênh thế giới, khu vực, tổ đội, tông môn, riêng tư. Lọc từ ngữ.
- **Hộ pháp:** khi đột phá trong thế giới, người chơi khác đứng hộ pháp (chặn quái, tăng tỷ lệ thành công). Đây là lý do tự nhiên để có bạn tu.
- **Tông môn và bạn bè gắn với tài khoản:** nhân vật kiếp mới vẫn ở lại tông môn dưới dạng "chuyển thế đệ tử".
- **Sư đồ** (sau launch): người chơi kỳ cựu (kể cả đang ở kiếp mới) dẫn tân thủ, nhận điểm Luân Hồi.
- **Tin tọa hóa & Bia Vạn Cổ:** thông báo khi một tu sĩ nổi danh tọa hóa / tử vong, xem được tiểu sử kiếp của họ.

### 4.16. Nội dung PvE
- **Nhiệm vụ chính** theo chương, dẫn người chơi đi qua các bản đồ theo thứ tự hợp lý nhưng không khóa cứng.
- **Nhiệm vụ phụ** gắn với NPC trên từng bản đồ; **nhiệm vụ hằng ngày** (giới hạn để tránh nghĩa vụ hóa).
- **Bí cảnh:** 3 bí cảnh cho MVP (1 solo, 2 tổ đội), mỗi cái 10–15 phút, 2–3 boss có cơ chế.
- **World boss, sự kiện thế giới:** xem 4.8.
- **Hiểm địa:** xem 4.5.
- **Tháp thí luyện:** leo tầng, độ khó tăng dần (tái sử dụng content tốt cho team nhỏ).

### 4.17. Linh thú (Beta, [GD-25](docs/design/26-spirit-beasts.md))
- **Thu phục:** quái thường thuộc loài nuôi được có xác suất nhỏ xuất hiện thành **Linh chủng** (phát sáng, huyết mạch Linh / Huyền / Địa); đánh còn ≤ 30% HP rồi ném **Ngự Thú Phù** (thấy tỷ lệ trước, có bảo hiểm). Quái thường rơi **trứng** hiếm; boss, hiểm địa, cơ duyên rơi trứng phẩm cao. **Boss đặc biệt** (vd. Hồ Yêu Mẫu) cũng thu phục được trong cửa sổ "Kiệt Sức" khi còn ≤ 10% HP, tỷ lệ rất thấp (1–15%, có bảo hiểm), người thu phục đổi phần rơi đồ boss của mình lấy thú boss.
- **Đồng hành:** kết khế tối đa 3, xuất chiến 1; thú tự đánh + 1 chiêu lệnh (phím T) + đặc tính. **Sức mạnh tính theo chỉ số chủ nhân** × chất lượng thú (trần ~17% DPS chủ). Thú không chết vì chiến đấu, chỉ trọng thương.
- **Lai tạo** ở Linh Thú Viên (động phủ): di truyền tư chất / đặc tính, **biến dị theo độ hiếm** (huyết mạch càng cao càng khó biến dị nhưng biến dị càng mạnh), lai loài (vd. Hỏa Lang). Lai tạo tiêu thọ nguyên của bố mẹ, giới hạn số lần.
- **Một mạng:** thú có thọ riêng (60–400 năm), già cùng đồng hồ nhân vật; khi tọa hóa chọn thú giữ lại theo số ô Linh Thú Viên — thú Thiên chủng có thể theo người chơi qua nhiều kiếp.
- 2.5D giúp làm linh thú rẻ: tái sử dụng model quái + đổi màu/kích thước.

### 4.18. Động phủ & Sơn môn (Beta, [GD-30](docs/design/28-caves-sect-sites.md))
- **Nguyên tắc (quyết định 2026-10-22):** vị trí nằm trên bản đồ chung (do designer đặt sẵn), bên trong là instance. **Không xây tự do trên bản đồ chung** (tránh thiếu đất, phá thiết kế màn chơi, phá rối, nặng cho web).
- **Động phủ cá nhân:** chọn một cửa động trên vách núi (nhiều người dùng chung một cửa, mỗi người vào động riêng); thuộc tài khoản, giữ qua kiếp; Tĩnh Thất, Linh Điền, Đan Thất, Khí Thất; trang trí là nguồn bán ngoại hình.
- **Sơn môn tông môn:** mỗi vực 6–8 địa điểm; mặt tiền hiện trên bản đồ chung, lớn dần theo cấp tông môn; giữ bằng Uy vọng (PvE) + phí duy trì; Sơn Môn Đại Hội hằng tháng; mất sơn môn → **phế tích** thành nội dung cho người chơi khác.
- Hệ số đả tọa trong động phủ / trú địa luôn thấp hơn linh mạch ngoài đồng.

### 4.19. Monetization (định hướng)
- Free-to-play, **không pay-to-win**.
- Vật phẩm mua bằng tiền thật **gắn với tài khoản, giữ qua mọi kiếp** (ngoại hình, danh hiệu, hiệu ứng): mua một lần, dùng mãi. Trong game một mạng, đây là lý do mua rất rõ ràng.
- **Tuyệt đối không bán:** hồi sinh, thọ nguyên, thế thân phù, lượt roll thiên phú, ô Tàng Bảo Các.
- Bán: ngoại hình (trang phục, hiệu ứng kiếm, tọa kỵ), Vấn Đạo Lệnh (battle pass theo mùa — đổi tên vì "Tiên Lộ" là tên game), tiện ích (mở rộng túi, ô nhân vật).
- Chi tiết trong Monetization Design Doc — chốt trước Beta.

---

## 5. Kiến trúc kỹ thuật

### 5.1. Sơ đồ tổng quan

```
                    ┌──────────────────────────────┐
                    │   CDN (asset: GLB, KTX2, ogg)│
                    └──────────────┬───────────────┘
                                   │ HTTPS
┌──────────────────────────────────┴──────────────────────────────────┐
│                  CLIENT — Trình duyệt                                │
│  Babylon.js 7+ · TypeScript · Vite · Render 2.5D · Logic/va chạm 2D  │
│  UI HTML/React overlay · Web Worker cho xử lý mạng/giải nén          │
└───────────┬───────────────────────────────────────┬─────────────────┘
            │ HTTPS (REST/JSON)                     │ WebSocket (binary)
            ▼                                       ▼
┌──────────────────────────┐          ┌──────────────────────────────────┐
│  API Service             │          │  Game Server (Zone Server)       │
│  Spring Boot (MVC)       │          │  Colyseus · Node.js · TypeScript │
│  - Auth (JWT)            │          │  - Room = MapInstance, tick 20Hz │
│  - Tài khoản, nhân vật   │          │  - Di chuyển, combat, AI quái    │
│  - Túi đồ, chế tác       │          │  - Mỗi tiến trình chạy nhiều     │
│  - Sàn đấu giá, shop     │          │    bản đồ×kênh + instance        │
│  - Tông môn, bạn bè      │          │  - AOI, cơ duyên, world event    │
│  - World Directory       │          │  - Chuyển bản đồ (handoff)       │
└──────────┬───────────────┘          └──────────┬───────────────────────┘
           │                                     │
           │        ┌───────────────────┐        │
           ├───────►│ Redis             │◄───────┤
           │        │ - Session, online │        │
           │        │ - Pub/Sub (chat,  │        │
           │        │   sự kiện chéo)   │        │
           │        │ - Cache, rate-lim │        │
           │        └───────────────────┘        │
           │        ┌───────────────────┐        │
           └───────►│ PostgreSQL 16+    │◄───────┘
                    │ - Dữ liệu bền vững│  (game server ghi định kỳ /
                    └───────────────────┘   khi chuyển bản đồ, qua batch)
```

**Lý do tách 2 service:**
- API Service: request/response, dễ scale ngang, dùng Spring tiêu chuẩn (Spring Data JPA, Spring Security).
- Game Server (D-19): stateful, vòng lặp tick cố định, độ trễ thấp → **Colyseus (Node.js / TypeScript)**, mỗi room là một `MapInstance`. Code mô phỏng (va chạm 2D, công thức combat, AI, tìm đường, Ink) viết **một lần bằng TypeScript** trong `shared/sim`, client và game server dùng chung.
- **Ranh giới quyền hạn:** Game Server chỉ giữ trạng thái realtime trong RAM; mọi thao tác có giá trị lâu dài (vật phẩm, ledger, chết thật, đột phá, chế tác, giao dịch, RNG quan trọng, đồng hồ tuổi) gọi **API nội bộ của API Service** (REST/JSON, idempotent theo `requestId`).
- MVP chạy cả 2 trong **1 monorepo** (Gradle cho API Service, pnpm workspace cho client / game server / shared), triển khai riêng.

**Mô hình bản đồ trên server (open world chia bản đồ):**
- Mỗi **(bản đồ, kênh)** là một đối tượng `MapInstance` độc lập có tick loop riêng; một tiến trình game-server chứa nhiều `MapInstance` (vd. 20–50 cái), chạy nhiều tiến trình Node.js để dùng hết CPU (Colyseus + Redis presence/driver).
- **World Directory** (trong API service + Redis): biết `MapInstance` nào đang chạy ở tiến trình nào, còn bao nhiêu chỗ → điều phối khi người chơi vào game / chuyển bản đồ / mở kênh mới.
- **Chuyển bản đồ (handoff):** người chơi chạm cổng → server hiện tại lưu trạng thái nhân vật (Redis + đánh dấu ghi DB) → cấp *transfer ticket* → client kết nối tới `MapInstance` đích (cùng hoặc khác tiến trình) → server đích nạp trạng thái. Mục tiêu < 2 giây kể cả tải bản đồ (client đã tải trước).
- **Trạng thái toàn cục** (giờ trong game, thời tiết, lịch sự kiện thế giới) do một **World Clock / Event Scheduler** phát qua Redis pub/sub cho mọi `MapInstance`.
- Bản đồ ít người → tự động gộp kênh; bản đồ trống → tạm ngưng tick để tiết kiệm CPU.

### 5.2. Client (Babylon.js)

| Hạng mục | Lựa chọn | Ghi chú |
|----------|----------|---------|
| Ngôn ngữ / build | TypeScript + Vite | Hot reload nhanh, code splitting |
| Engine | Babylon.js 7+ (WebGL2, thử WebGPU) | WebGL2 là mặc định cho tương thích |
| Camera | ArcRotateCamera giới hạn góc (45–55°), zoom giới hạn | Camera cố định → frustum nhỏ, dễ tối ưu |
| Va chạm / logic | **Thư viện hình học 2D tự viết** (circle, OBB, cone, segment) + lưới đi được (walkable grid) | Viết một lần bằng TS trong `shared/sim`, client và game server dùng chung nên kết quả giống nhau (D-19). Không cần Havok |
| Tìm đường | **recast-navigation-js** (navmesh) | Click-to-move phía client; game server dùng **cùng thư viện** (D-19) |
| Shader / ánh sáng | Toon shader + gradient ramp, lightmap/AO bake sẵn, 1 directional light + bóng đổ đơn giản | Đẹp, rẻ, đồng nhất phong cách |
| UI | **HTML/CSS overlay (React hoặc SolidJS)** | Nhanh làm, dễ thuê dev; Babylon GUI chỉ cho UI trong không gian 3D (tên, thanh máu trên đầu) |
| State | Kiến trúc ECS nhẹ (vd. bitECS) hoặc component tự viết | Tách logic khỏi render để chia sẻ code mô phỏng |
| Mạng | **Colyseus client** (WebSocket): đồng bộ state bằng `@colyseus/schema` (delta, binary); message input / sự kiện có kiểu TS dùng chung | Thay Protobuf ở kênh realtime (D-19) |
| Asset | glTF/GLB + Draco/Meshopt, texture KTX2 (Basis) | Giảm dung lượng tải 3–5 lần |
| Animation | Babylon AnimationGroup + blending, state machine tự viết | Định nghĩa combo/cancel window trong data JSON |
| Âm thanh | Babylon Audio Engine v2 / Howler.js | |

**Ngân sách hiệu năng (mục tiêu):**
- 60 FPS trên GPU tích hợp (Intel UHD/Iris Xe) ở chất lượng Trung bình; mục tiêu sau launch: 30–60 FPS trên điện thoại tầm trung.
- **Lần tải đầu < 15MB** để vào game; mỗi bản đồ thêm 2–5MB, tải trước bản đồ kề cạnh trong nền; cache bằng Service Worker / IndexedDB.
- Draw call < 200 trong thành trấn đông người; dùng instancing, thin instances, gộp mesh tĩnh theo ô (chunk merge), texture atlas.
- Nhân vật người chơi: ≤ 5k tris; quái thường ≤ 3k tris; prop môi trường ≤ 500–1.500 tris.
- Texture: phần lớn dùng **1 atlas màu gradient chung** (kiểu Synty/low-poly), chỉ 256–512px cho đồ đặc biệt.
- Hiển thị tối đa ~100 nhân vật trên màn hình; người chơi khác ở xa có thể ẩn bớt hiệu ứng kỹ năng (tùy chọn).

### 5.3. Server (API Service: Spring Boot · Game Server: Colyseus)

| Hạng mục | Lựa chọn |
|----------|----------|
| Ngôn ngữ | Java 21 (API Service, virtual threads) · TypeScript trên Node.js LTS (Game Server) |
| Framework | Spring Boot 3.x (API Service) · Colyseus (Game Server, bản ổn định mới nhất lúc fork T5C) |
| Realtime | Colyseus room (WebSocket) trong Game Server; mỗi room = 1 `MapInstance` |
| Serialization | Client ↔ Game Server: `@colyseus/schema` + message TS · Game Server ↔ API Service: REST/JSON nội bộ (OpenAPI, sinh client TS) · Client ↔ API Service: REST/JSON |
| ORM | Spring Data JPA (API) + jOOQ/JDBC thuần cho truy vấn nóng. Game Server **không** truy cập PostgreSQL trực tiếp |
| Migration DB | Flyway |
| Auth | JWT ngắn hạn + refresh token; game server xác thực ticket một lần khi kết nối WS |
| Cache / pub-sub | Redis |
| Monitoring | Spring Actuator + Micrometer (API) · prom-client (Game Server) + Prometheus + Grafana; log tập trung (Loki hoặc ELK) |
| Test | JUnit 5, Testcontainers (API Service) · Vitest (Game Server, `shared/sim`); test tích hợp chạy cả hai với PostgreSQL/Redis thật |

**Nguyên tắc server:**
- **Server-authoritative tuyệt đối:** client chỉ gửi *input/ý định* (di chuyển hướng X, dùng kỹ năng Y vào mục tiêu Z), server quyết định kết quả. Mọi giao dịch đồ, chế tác, rơi đồ đều do server tính.
- **Tick loop:** 20Hz (50ms) cho mỗi `MapInstance`; gửi snapshot trạng thái có delta compression; interest management theo lưới (grid AOI): camera 2.5D chỉ thấy ~40×30m nên chỉ cần gửi thực thể trong bán kính ~35–45m. Băng thông thấp hơn nhiều so với 3D góc nhìn thứ ba.
- **Mô phỏng 2D:** vị trí `(x, z)` + hướng; độ cao `y` client tự lấy từ heightmap khi hiển thị.
- **Chế tác (luyện đan/khí):** minigame chạy trên client nhưng server nhận chuỗi input + timestamp và tính lại kết quả (hoặc server chỉ dùng điểm minigame như một hệ số có giới hạn + kiểm tra hợp lý).
- **Lưu trạng thái:** trạng thái nhân vật trong `MapInstance` giữ trên RAM, lưu DB mỗi 30–60 giây + khi chuyển bản đồ/đăng xuất. Giao dịch vật phẩm quan trọng (giao dịch, đấu giá, chế tác) ghi DB ngay trong transaction.
- **Chống hack:** kiểm tra tốc độ di chuyển, tần suất kỹ năng (cooldown phía server), rate-limit packet, log bất thường.
- **Cái chết do server quyết định và có hồ sơ:** mỗi cái chết thật lưu *death log* (60 giây sự kiện cuối: sát thương nhận, vị trí, ping, trạng thái kết nối) để xử lý khiếu nại và phát hiện lỗi.
- **Bảo vệ khi mất kết nối:** mất kết nối trong hiểm địa thì nhân vật vào *quy tức* 60 giây. Server sự cố diện rộng thì áp dụng chính sách khôi phục nhân vật chết trong khoảng thời gian sự cố.
- **RNG phía server:** mọi kết quả ngẫu nhiên (roll thiên phú, cơ duyên, chế tác, đột phá) do server tính bằng RNG an toàn, ghi log kiểm toán.
- **Đồng hồ tuổi:** server tính tuổi theo thời gian hoạt động thực tế (có hệ số theo khu vực / AFK), chống gian lận đồng hồ client.

### 5.4. Cơ sở dữ liệu (PostgreSQL)

> **Nguồn chuẩn: [TD-03 Database Schema](docs/tech/02-database.md)** — quy ước, sổ đăng ký ~120 bảng theo module, DDL bảng lõi, ma trận dữ liệu qua kiếp, phân vùng / hạn lưu, migration. DDL chi tiết từng hệ thống nằm ở mục "Lưu trữ" của GD sở hữu. Bản phác schema cũ ở đây đã bị thay (DB-R-050).

**Nguyên tắc:**
- Dùng **JSONB** cho thuộc tính linh hoạt (thuộc tính phụ trang bị, stats), cột thường cho thứ cần index/truy vấn.
- **Bảng log/ledger** cho tiền tệ và vật phẩm là bắt buộc — để điều tra dupe/hack và rollback.
- Partition bảng log theo tháng.
### 5.5. Cấu trúc repo đề xuất (monorepo)

```
xproject/
├── docs/                    # Toàn bộ tài liệu thiết kế (mục 8)
├── client/                  # Babylon.js + TS + Vite
│   ├── src/
│   │   ├── core/            # game loop, ECS, input, camera
│   │   ├── net/             # websocket, protobuf, prediction
│   │   ├── combat/          # state machine, hitbox debug, VFX trigger
│   │   ├── world/           # load bản đồ, preload bản đồ kề, ngày/đêm, thời tiết
│   │   ├── ui/              # React/Solid overlay
│   │   └── systems/         # crafting UI, inventory...
│   └── public/
├── server/
│   ├── api-service/         # Spring Boot REST
│   ├── game-server/         # Colyseus · Node.js · TypeScript (fork T5C, D-19)
│   └── common/              # entity, DTO phía Java
├── shared/
│   ├── sim/                 # TS dùng chung client + game server: geom2d, công thức combat, AI, Ink runner
│   ├── schema/              # @colyseus/schema state + kiểu message; OpenAPI API nội bộ
│   ├── gamedata/            # JSON/CSV: item, skill, quái, đan phương... (single source of truth)
│   ├── maps/                # dữ liệu bản đồ: lưới đi được, spawn, POI, cổng nối (xuất từ map editor)
│   └── encounters/          # kịch bản cơ duyên, sự kiện thế giới (YAML/JSON)
├── tools/
│   ├── map-editor/          # Map editor chạy trên web (Babylon.js)
│   └── ...                  # script export data, validate data, asset pipeline
├── art-source/              # (Git LFS hoặc repo riêng) file .blend, PSD
└── infra/                   # docker-compose, k8s/helm, CI config
```

### 5.6. Hạ tầng & triển khai
- **Dev:** docker-compose (PostgreSQL, Redis, api, game-server).
- **CI/CD:** GitHub Actions / GitLab CI — build, test, lint, validate gamedata, deploy staging tự động.
- **Staging + Production:** bắt đầu bằng VPS/cloud VM (2–3 máy) + Docker; chỉ lên Kubernetes khi thực sự cần scale.
- **CDN** cho asset (Cloudflare/BunnyCDN), version hóa theo hash để cache vĩnh viễn.
- **Backup:** PostgreSQL PITR (WAL archiving), backup hằng ngày, **diễn tập khôi phục** định kỳ.
- **Ước lượng ban đầu:** 1 game-server (8 vCPU/16GB) chịu ~500–1000 CCU tùy mức độ combat — cần load test xác nhận.

### 5.7. Chiến lược sử dụng mã nguồn mở

> **Nguyên tắc: "Dùng thư viện, không lấy framework game làm nền."** Dùng mã nguồn mở để tiết kiệm thời gian ở những phần **không tạo khác biệt** (render, tìm đường, serialization, công cụ hội thoại, admin UI). **Tự làm** những phần là **bản sắc của game** (vòng đời một mạng, cơ duyên, combat hành động, chế tác, kinh tế).

#### 5.7.1. Đánh giá RPG-JS (github.com/RSamaium/RPG-JS)

RPG-JS là framework TypeScript (license MIT) để làm RPG / MMORPG **2D** trên trình duyệt: client PixiJS, server Node.js, bản đồ soạn bằng Tiled, cùng một codebase chạy được cả chế độ chơi đơn lẫn MMO.

| Tiêu chí | RPG-JS | Yêu cầu dự án | Đánh giá |
|----------|--------|---------------|:--------:|
| Đồ họa | 2D tile-based (PixiJS), bản đồ Tiled | 2.5D Babylon.js (D-01) | ❌ Phải bỏ quyết định 2.5D |
| Server | Node.js / TypeScript | Spring Boot / Java (D-06) | ❌ Phải bỏ backend Java hoặc vận hành 2 backend |
| Mô hình gameplay | Kiểu RPG Maker: di chuyển theo tile, event, hội thoại | Action combat top-down, skillshot, hitbox 2D, lag compensation | ⚠️ Phần combat vẫn phải tự viết gần hết |
| Độ trưởng thành | v4 ổn định; **v5 vẫn đang beta** (5.0.0-beta.x) và là nhánh mặc định | Nền tảng ổn định cho 2–3 năm phát triển + vận hành | ⚠️ Rủi ro breaking change khi v5 ra bản chính thức |
| Cộng đồng | ~1,6k sao GitHub, phụ thuộc nhiều vào 1 maintainer chính | | ⚠️ Rủi ro "bus factor" |
| MMO & dữ liệu bền vững | Có room đồng bộ, client prediction, save/load | Nhiều tiến trình, handoff bản đồ, transaction, ledger, death log, RNG audit cho game một mạng | ❌ Phần quan trọng nhất vẫn phải tự xây |
| License | MIT | | ✅ |

**Kết luận: KHÔNG dùng RPG-JS làm nền tảng (D-16).**
- Dùng RPG-JS buộc phải đảo ngược 3 quyết định đã duyệt: 2.5D, Spring Boot, action combat.
- Phần tiết kiệm được (render tile, đồng bộ room cơ bản, GUI) lại là phần *dễ*. Phần *khó nhất* của dự án vẫn phải tự làm: combat hành động qua mạng, vòng đời một mạng an toàn, chuyển bản đồ, kinh tế, kiểm toán.
- **Nên đọc mã nguồn RPG-JS để học:** cách tổ chức client prediction, đồng bộ room, hệ thống event/hook cho nội dung.
- **Khi nào xem lại:** nếu team quyết định chuyển sang **2D tile + backend Node.js** (vd. không tuyển được dev Java, hoặc cần bản chơi thử trong 3 tháng), RPG-JS sẽ là ứng viên hàng đầu.
- **Không làm prototype "vứt đi" bằng RPG-JS:** thời gian học framework xấp xỉ thời gian làm prototype. Vòng lặp một mạng / thiên phú / cơ duyên nên kiểm chứng bằng **prototype giấy + spreadsheet + Ink** ở Phase 0, vì rẻ và nhanh hơn.

#### 5.7.2. Bộ thư viện mã nguồn mở đề xuất

| Lĩnh vực | Thư viện | License | Dùng cho | Thay thế phần tự viết nào |
|----------|----------|---------|----------|---------------------------|
| Render | **Babylon.js** | Apache-2.0 | Render 2.5D, animation, glTF loader | (đã chọn) |
| Map editor | **Babylon.js Editor** (v5.5.x, còn hoạt động) | Apache-2.0 | Đánh giá làm nền cho map editor (scene, đặt prop, plugin) | Giảm 1–2 tháng tự viết editor nếu phù hợp |
| Tối ưu asset | **glTF-Transform** | MIT | CLI/script nén Meshopt, KTX2, gộp mesh | Pipeline asset |
| Tìm đường | **recast-navigation-js** (client, editor, game server) | MIT | Một thư viện dùng ở mọi phía (D-19) | Lưới đi được + A* tự viết |
| ECS | **bitECS** | MPL-2.0 | Quản lý thực thể phía client | ECS tự viết |
| Protocol | **Colyseus** + `@colyseus/schema` (realtime); **OpenAPI Generator** (API nội bộ) | MIT / Apache-2.0 | Đồng bộ state delta, message có kiểu; client TS cho API Spring Boot | Netcode nền tự viết |
| UI | **React** + **Zustand** + **i18next** | MIT | UI overlay, state, đa ngôn ngữ (D-07) | |
| Âm thanh | **Howler.js** | MIT | Nhạc nền, SFX | |
| **Cơ duyên / hội thoại** | **Ink** (inkle): soạn bằng **Inky**, runtime **inkjs** (client preview và game server) | MIT / Apache-2.0 | Content designer viết cơ duyên, hội thoại lựa chọn, nhánh truyện; **server chạy kịch bản** nên kết quả do server quyết định | Encounter editor + ngôn ngữ kịch bản tự định nghĩa (tiết kiệm ~1–1,5 tháng) |
| AI quái | Behavior tree / FSM TS tự viết nhẹ trong `shared/sim` (tham khảo **mistreevous**, MIT) | MIT | AI quái, boss pattern phía server | |
| Server | Spring Boot, Flyway, jOOQ, Caffeine, Micrometer (API Service); Colyseus, Node.js (Game Server) | Apache-2.0 / MIT | Nền tảng server | |
| Test | **Testcontainers**, JUnit 5 | MIT / EPL-2.0 | Test với PostgreSQL / Redis thật | |
| Validate data | **JSON Schema + Ajv** | MIT | Kiểm tra gamedata trong CI | Validator tự viết |
| Admin / GM panel | **Refine** hoặc **react-admin** | MIT | CRUD, tra cứu người chơi, Death Review UI | Giảm ~0,5 tháng |
| Monitoring | Prometheus, Grafana, Loki | Apache-2.0 / AGPL (công cụ nội bộ) | Giám sát, log | |
| Load test | **k6** (WebSocket) + bot client dùng lại Colyseus client | AGPL (công cụ nội bộ) | Load test | |
| Analytics | **PostHog** (self-host) hoặc bảng sự kiện + **Metabase** | MIT / AGPL (nội bộ) | Telemetry, KPI một mạng | |
| Asset | **Kenney, Quaternius, KayKit** (CC0), Mixamo (miễn phí, không phải OSS: đọc điều khoản) | CC0 | Asset prototype & bổ sung | Giảm khối lượng 3D |

> Mọi license phải được kiểm tra lại trên file LICENSE của repo chính thức khi đưa vào **OSS Register** (TD-15).
> **D-19 đã duyệt** (5.7.7): game server dùng TypeScript nên **không dùng recast4j, blade-ink, gdx-ai, Netty, protobuf-java**.

#### 5.7.3. Dự án để học (đọc ý tưởng, không copy mã)
- **Colyseus** (MIT, Node.js): đồng bộ state theo room, delta patch. Tham khảo khi thiết kế `MapInstance`.
- **Nakama** (Apache-2.0, Go): mô hình social / guild / leaderboard / chat. Tham khảo thiết kế API.
- **RPG-JS** (MIT): client prediction, event hook cho nội dung.
- **T5C**: nền fork theo D-19 (xem 5.7.7).
- **Reldens** (Phaser + Colyseus): admin panel quản lý NPC / quái / vật phẩm / kỹ năng, mô hình dữ liệu RPG.
- **L2J / aCis** (GPL, Java): kiến trúc server MMO Java lâu năm. **Chỉ đọc tham khảo, tuyệt đối không copy mã** vì license GPL.
- **Veloren** (GPL-3.0, Rust): open world MMO, mô phỏng thế giới, netcode.

#### 5.7.4. Chính sách license
| Mức | License | Quy tắc |
|-----|---------|---------|
| ✅ Dùng thoải mái | MIT, Apache-2.0, BSD, ISC, zlib, CC0 (asset) | Giữ file NOTICE / ghi công theo yêu cầu |
| ✅ Có điều kiện | MPL-2.0, EPL-2.0 | Dùng được; nếu sửa file của thư viện thì phải công bố file đã sửa |
| ⚠️ Chỉ làm công cụ nội bộ | GPL, AGPL (k6, Metabase, Grafana/Loki, Tiled editor) | Không nhúng vào client / server phát hành |
| ❌ Cấm nhúng vào sản phẩm | GPL / AGPL / SSPL library, asset "Non-Commercial" (CC BY-NC), asset không rõ nguồn | |

- Mọi dependency ghi vào **OSS Register** (`docs/tech/14-oss-register.md`): tên, version, license, mục đích, người duyệt, ngày duyệt.
- CI tự kiểm tra license: `license-checker` (npm) và plugin license cho Maven/Gradle; build fail khi gặp license bị cấm.
- Asset mua / miễn phí: lưu bằng chứng license (file license, hóa đơn) cùng asset.
- Thư viện quan trọng (Ink runtime, recast) được **bọc qua interface nội bộ** để có thể thay thế khi thư viện ngừng phát triển.

#### 5.7.5. Ước tính thời gian tiết kiệm
| Hạng mục | Tiết kiệm ước tính (người-tháng) |
|----------|:--------------------------------:|
| Ink + blade-ink cho cơ duyên / hội thoại | 1–1,5 |
| recast-navigation-js + recast4j cho tìm đường | ~1 |
| Babylon.js Editor làm nền map editor (nếu đánh giá đạt) | 1–2 |
| Refine / react-admin cho GM panel | ~0,5 |
| Asset CC0 cho prototype & bổ sung | 1–2 (artist) |
| **Tổng** | **~4,5–7 người-tháng** |

> Ước tính sơ bộ, xác nhận lại sau các đánh giá ở Phase 0. Thời gian tiết kiệm nên dồn vào **nội dung** (bản đồ, cơ duyên): đây là rủi ro R4b.

#### 5.7.6. Ứng viên làm nền tảng (fork rồi phát triển tiếp)

Kết quả khảo sát (2026-10-19): **không có dự án nào khớp trọn plan** (2.5D Babylon.js + MMO + tiên hiệp một mạng). Các ứng viên đáng xét:

| Dự án | Stack | License | Độ khớp | Nhận xét |
|-------|-------|---------|:-------:|----------|
| **T5C: The 5th Continent** (`orion3dgames/t5c`) | Babylon.js + Colyseus (TypeScript / Node.js), MySQL/SQLite, Vite | MIT (theo index bên thứ ba, **phải xác minh LICENSE**) | ⭐⭐⭐⭐ | RPG 3D **top-down** nhiều người chơi trên web. Có sẵn: di chuyển server-authoritative + client prediction/reconciliation, đăng nhập / chọn nhân vật, nhiều map + dịch chuyển (dungeon), chat toàn cục, AI quái (idle/patrol/chase/attack), loot, kỹ năng, buff, AoE, cooldown, đánh tầm xa, túi đồ, trang bị, lên cấp, nhiệm vụ, trainer, vendor, spatial grid lọc thực thể theo khoảng cách; đã thử ~100 người chơi giả lập. **Hạn chế:** dự án nhỏ, 1 tác giả chính; combat kiểu chọn mục tiêu + click-to-move (plan dùng WASD + skillshot) |
| Reldens | Phaser (2D) + Colyseus, Node.js | xác minh | ⭐⭐ | Nền tảng MMORPG 2D có admin panel quản lý nội dung. 2D nên không hợp D-01; chỉ tham khảo |
| RPG-JS | PixiJS (2D) + Node.js | MIT | ⭐ | Xem 5.7.1 |
| Leseptum/xianxia_server | Godot 4 (.NET) + SpacetimeDB | xác minh | ⭐ | MMORPG 2D chủ đề tu tiên, khác stack hoàn toàn; chỉ tham khảo ý tưởng |
| 4thfever/cultivation-world-simulator | Python + Vue, AI agent | xác minh | ⭐ (nội dung) | Mô phỏng thế giới tu tiên bằng AI agent; tham khảo thiết kế hệ thống / nội dung, không dùng code |
| CivGenesis / jforgame / ioGame | Java + Netty (+ Spring Boot) | Apache-2.0 / Apache-2.0 / **AGPL-3.0** | ⭐⭐ (server) | Framework server game Java, không có gameplay. ioGame AGPL nên không dùng; CivGenesis mới, cần kiểm tra độ hoạt động |

> **Phát hiện quan trọng:** T5C chạy game server bằng **Colyseus (TypeScript)**. Nếu dùng làm nền, **logic mô phỏng chỉ viết một lần bằng TypeScript và dùng chung cho client và server**. Plan hiện tại (D-06) phải viết **hai lần** (TS cho client prediction, Java cho server): thư viện va chạm 2D (TD-14), navmesh (recast-navigation-js + recast4j), Ink (inkjs + blade-ink), công thức combat. Đây là nguồn tốn thời gian, tốn token AI và dễ gây lỗi lệch kết quả giữa hai phía.

#### 5.7.7. D-19 (đã duyệt 2026-10-22): kiến trúc lai, fork T5C

| | Phương án A: giữ nguyên D-06 | **Phương án B: lai (đề xuất)** | Phương án C: toàn bộ Node.js |
|---|---|---|---|
| Client | Babylon.js tự xây | **Fork T5C** (Babylon.js) rồi phát triển | Fork T5C |
| Game server realtime | Spring Boot + Netty tự xây | **Colyseus (TypeScript) từ T5C**: mỗi room = 1 `MapInstance` | Colyseus |
| API / dữ liệu bền vững | Spring Boot | **Spring Boot giữ nguyên**: tài khoản, nhân vật, vật phẩm, kinh tế, ledger, luân hồi, đấu giá, GM, death log | Node.js |
| Database | PostgreSQL + Redis | PostgreSQL + Redis (thay MySQL của T5C) | PostgreSQL + Redis |
| Code mô phỏng dùng chung client/server | ❌ Viết 2 lần (TS + Java) | ✅ Viết 1 lần (TS) | ✅ |
| Tận dụng được | Chỉ thư viện | Nền client + server realtime chạy được ngay | Tối đa |
| Tiết kiệm ước tính (Phase 0–2) | 0 | **~3–5 người-tháng** | ~4–6 người-tháng |
| Rủi ro chính | Tự làm nhiều nhất | Hai ngôn ngữ phía server; chất lượng code T5C cần đánh giá; phải đổi combat sang WASD + skillshot | Bỏ Spring Boot; Node.js cho kinh tế / kiểm toán đòi hỏi kỷ luật cao |

**Vì sao đề xuất B:**
- Giữ Spring Boot ở nơi cần nhất: **giao dịch, kinh tế, kiểm toán, cái chết thật** (transaction, ledger, death log, RNG audit).
- Phần realtime chuyển sang TypeScript dùng chung với client, bỏ được việc port Java ↔ TS, nên giảm cả thời gian dev lẫn token AI.
- Colyseus (MIT) có sẵn room, đồng bộ state dạng delta (schema), matchmaking, mở rộng nhiều tiến trình qua Redis. Khớp mô hình `MapInstance` + kênh của plan.
- Node.js một luồng mỗi tiến trình đủ cho room 20Hz, 100–150 người, góc nhìn top-down; mở rộng bằng cách chạy nhiều tiến trình.

**Ranh giới quyền hạn nếu chọn B:**
- **Game server (Colyseus):** mô phỏng thời gian thực (di chuyển, combat, AI, hiểm địa, cơ duyên trong bản đồ), giữ trạng thái trong RAM.
- **API Service (Spring Boot) là nguồn sự thật** cho mọi thứ có giá trị lâu dài. Game server gọi API nội bộ cho các thao tác quan trọng (mang đồ ra khỏi hiểm địa, chết thật, đột phá, chế tác, giao dịch); mọi thao tác đều ghi ledger.
- RNG quan trọng (roll thiên phú, đột phá, kết quả chế tác) và đồng hồ tuổi được tính ở API Service.

**Việc phải làm khi fork T5C:**
1. Xác minh LICENSE, đánh giá chất lượng code, test, phiên bản Babylon.js / Colyseus (3–5 ngày, Phase 0).
2. Thay MySQL/SQLite bằng lời gọi API Spring Boot + PostgreSQL.
3. Đổi camera sang 2.5D cố định; đổi combat sang WASD + skillshot + né lướt (giữ hệ kỹ năng / buff / cooldown).
4. Thêm handoff bản đồ qua World Directory, death log, trạng thái quy tức khi mất kết nối.
5. Dọn bỏ phần không dùng, viết tài liệu module (giảm token khi AI đọc code).
6. Giữ ghi công MIT; fork độc lập (theo dõi upstream nhưng không phụ thuộc).

**Phương án dự phòng (cổng Phase 0):** kiến trúc B là cố định. Đánh giá T5C (3–5 ngày) chỉ quyết định **có fork T5C hay không**: nếu LICENSE không rõ ràng hoặc chất lượng code kém thì vẫn dùng Colyseus + TypeScript nhưng dựng game server / client từ đầu, T5C chỉ để tham khảo. Ghi kết quả vào Decision Log.

#### 5.7.8. Bản đồ tận dụng mã nguồn mở theo module

| Module trong plan | Nguồn mở đề xuất | Mức tận dụng | Phần phải tự làm |
|-------------------|------------------|:------------:|------------------|
| Client nền (scene, login, chọn nhân vật, đồng bộ thực thể, animation) | T5C | Cao | Camera 2.5D, UI phong cách riêng |
| Netcode realtime | Colyseus + prediction của T5C | Cao | Lag compensation cho skillshot |
| Va chạm 2D | detect-collisions (JS, xác minh license) hoặc tự viết nhẹ | Cao | Hình quạt, quy tắc hitbox |
| Tìm đường | recast-navigation-js | Cao | Sinh navmesh từ map editor |
| AI quái | FSM của T5C, Yuka (MIT, ít cập nhật) | Trung bình | Boss pattern, telegraph |
| Map editor | Babylon.js Editor (Apache-2.0) | Trung bình – cao | Plugin xuất dữ liệu logic (spawn, POI, cổng, hiểm địa) |
| Cơ duyên / hội thoại | Ink + inkjs (MIT) | Cao (phần kể chuyện) | Engine điều kiện kích hoạt, phần thưởng, khí vận |
| Dữ liệu game & validate | JSON Schema + Ajv, json-schema-to-typescript | Cao | Schema dữ liệu game |
| API Service | Spring Boot, Spring Security, Flyway, OpenAPI Generator (sinh client TS) | Cao | Logic nghiệp vụ |
| GM panel | Refine / react-admin, @colyseus/monitor | Cao | Death Review |
| Load test | @colyseus/loadtest, k6 | Cao | Kịch bản bot |
| Telemetry | PostHog | Cao | Định nghĩa sự kiện, KPI một mạng |
| Asset | Kenney, Quaternius, KayKit, Poly Haven (CC0), Mixamo | Trung bình | Nhân vật & trang phục cổ phong, VFX |
| Thanh toán | Không có OSS phù hợp: SDK cổng thanh toán VN | — | Tích hợp |

**Tự làm hoàn toàn (bản sắc của game, không có nguồn mở thay thế):** vòng đời một mạng & thọ nguyên, luân hồi & di sản, roll thiên phú, engine cơ duyên, tu luyện & đột phá, luyện đan / luyện khí / chế công pháp (minigame + RNG có pity), hiểm địa vào–lấy–thoát, combat top-down đặc trưng, kinh tế & ledger, World Clock & sự kiện thế giới, Death Review.

#### 5.7.9. Tiết kiệm token khi dùng AI hỗ trợ phát triển
1. **Bắt đầu từ nền chạy được** (fork) thay vì để AI sinh boilerplate từ đầu. Ngay sau khi fork: **dọn code thừa** và viết `CLAUDE.md` mô tả bản đồ module, để AI không phải đọc toàn bộ repo mỗi lần.
2. **Một ngôn ngữ cho code mô phỏng** (TypeScript dùng chung client/server): viết một lần, không port.
3. **Sinh code bằng công cụ thay vì AI:** OpenAPI Generator (client TS từ Spring), Colyseus schema, JSON Schema → type TS/Java, migration Flyway theo mẫu.
4. **Data-driven:** designer sửa bảng tính / JSON / Ink thay vì sửa code, nên cần ít lượt AI hơn.
5. **Dùng thư viện phổ biến** (Babylon.js, Colyseus, Spring, React): AI hiểu rõ, ít vòng sửa lỗi.
6. **Module nhỏ, interface rõ, có test:** mỗi lần AI chỉ cần ngữ cảnh của một module; test tự động bắt lỗi mà không cần AI đọc lại nhiều.
7. **Chọn model theo việc:** model nhỏ / rẻ cho CRUD, test, boilerplate; model mạnh cho kiến trúc, netcode, cân bằng.
8. **Tài liệu ngắn gọn, ổn định** (plan, ADR, `CLAUDE.md`) để tận dụng prompt caching và tránh lặp lại ngữ cảnh.

---

## 6. Pipeline nội dung & asset

### 6.1. Phong cách mỹ thuật
- **Định hướng:** **low-poly stylized cổ phong**: khối hình đơn giản, màu phẳng/gradient, toon shading, viền nhẹ; điểm nhấn là **VFX phát sáng** (kiếm khí, linh quang, trận pháp) và **bảng màu theo vùng**. Tham khảo: Albion Online, Synty POLYGON, Sky: Children of the Light, tranh thủy mặc cho bầu trời/nền.
- Nhân vật hơi "chibi" nhẹ (đầu to ~1/5–1/6 thân) để dễ nhận diện từ góc nhìn trên cao; silhouette và màu trang phục phải phân biệt được ở khoảng cách xa.
- Thiết kế cho góc nhìn trên cao: chi tiết dồn vào **đỉnh đầu, vai, vũ khí, mặt đất**; mặt đất có texture/decal phong phú vì chiếm phần lớn màn hình.
- **Tài liệu:** Art Bible (bắt buộc trước khi sản xuất hàng loạt) có ảnh mẫu chụp đúng góc camera game.

### 6.2. Pipeline 3D
```
Concept (2D) → Blockout (Blender) → Model low-poly + UV vào atlas màu chung
→ Rig (skeleton chung) + Animation (Blender) → Export glTF
→ Tối ưu (gltf-transform: Meshopt, KTX2) → Kiểm tra trong viewer nội bộ (đúng góc camera game)
→ Commit vào asset repo → CDN
```
- **Quy chuẩn đặt tên, tỷ lệ (1 unit = 1m), trục, pivot**: viết trong Asset Guidelines.
- **Skeleton dùng chung** cho mọi nhân vật người chơi; quái chia vài nhóm skeleton (người, thú 4 chân, chim, rắn) để chia sẻ animation.
- **Bộ kit môi trường dạng module** cho từng vực (vd. kit "Rừng trúc", kit "Núi đá", kit "Kiến trúc tông môn") với 50–150 prop/kit, lắp ghép trong map editor.
- Animation: camera xa nên có thể ít frame hơn, nhưng **đòn đánh phải có anticipation & follow-through rõ** (quan trọng cho game feel).
- Script tự động hóa tối ưu asset trong `tools/`.

### 6.3. Pipeline bản đồ
```
Phác thảo layout 2D (giấy/Miro) → Blockout trong Map Editor (địa hình + vùng đi được)
→ Playtest đường đi, khoảng cách, mật độ → Trang trí bằng kit module + tô texture địa hình
→ Đặt quái, NPC, POI, cơ duyên, cổng nối → Bake ánh sáng/AO → Xuất:
   (1) GLB hiển thị (gộp mesh theo chunk)  (2) JSON logic: lưới đi được, spawn, POI, trigger
```
- Map editor sinh **cả dữ liệu hiển thị lẫn dữ liệu logic** từ một nguồn để client và server không bao giờ lệch nhau.
- Công cụ **tán prop tự động** (cỏ, đá, cây theo vùng tô) để làm nhanh bản đồ lớn.

### 6.4. Dữ liệu game (data-driven)
- Toàn bộ số liệu (item, kỹ năng, quái, đan phương, rơi đồ, cảnh giới, nhiệm vụ) do **Game Designer quản lý trên Google Sheets/Excel** → script export ra JSON trong `shared/gamedata/` → cả client và server đọc.
- Có **validator** chạy trong CI (ID trùng, tham chiếu không tồn tại, giá trị ngoài phạm vi).
- **Cơ duyên & hội thoại** viết bằng **Ink** (kể chuyện, lựa chọn, nhánh) + YAML/JSON (điều kiện kích hoạt, phần thưởng, sự kiện thế giới). Server chạy Ink bằng runtime Java nên mọi kết quả do server quyết định; validator kiểm tra cả hai phần trong CI.
- Server hỗ trợ **hot reload** gamedata ở môi trường dev/staging để designer chỉnh và thử ngay.

### 6.5. Công cụ nội bộ cần làm
| Công cụ | Mục đích | Giai đoạn |
|---------|----------|-----------|
| **Map Editor (web, Babylon.js)**: đánh giá mở rộng Babylon.js Editor trước khi tự viết | Địa hình heightmap + tô texture, đặt prop từ kit, tán prop tự động, vẽ vùng đi được, đặt spawn/NPC/POI/cổng, xuất GLB + JSON. **Công cụ quan trọng nhất** cho open world | Phase 0–1 (bản thô), hoàn thiện Phase 2 |
| **Encounter Previewer** | Viết cơ duyên / hội thoại bằng **Ink** (Inky), nạp nóng và chạy thử ngay trong game (chế độ dev) | Phase 2–3 |
| Combat debug overlay | Hiển thị hitbox 2D, frame data, trạng thái mạng | Phase 1 |
| **Death Review** | GM xem death log, phát lại 60 giây cuối, quyết định khôi phục nhân vật nếu chết do lỗi | Phase 2 |
| **Balance Simulator** | Mô phỏng hàng nghìn kiếp: tuổi thọ, cảnh giới đạt được, phân phối RNG, dòng tài nguyên | Phase 2–3 |
| Admin/GM web panel | Tra cứu người chơi, cấp đồ, ban, xem log | Phase 2 |
| Dashboard kinh tế | Theo dõi tiền tệ, giá đấu giá | Phase 4 |
| Bot load test | Giả lập hàng trăm client kết nối | Phase 2–4 |

---

## 7. Lộ trình phát triển (Roadmap)

> Tổng thời gian ước tính: **~21–22 tháng** đến Open Beta với team 8–10 người *(giãn từ 15–17 tháng ngày 2026-10-23 do phạm vi Beta tăng: linh thú, ba tông môn, PvP dã ngoại, Ma Vực, Di Tàng…; Beta chia 2 giai đoạn)*. Chuyển sang 2.5D giảm tải phần đồ họa/netcode, nhưng thời gian tiết kiệm được **dồn vào nội dung open world** (bản đồ, cơ duyên, sự kiện). Ước lượng sẽ được hiệu chỉnh sau mỗi phase.

### Phase 0 — Tiền sản xuất (Pre-production) · Tháng 1–2
**Mục tiêu:** chốt hướng đi, giảm rủi ro kỹ thuật lớn nhất.
- [ ] Viết Game Concept Doc + GDD v0.1 (các hệ thống ở mức tổng quan).
- [ ] Viết World Bible v0.1 (bối cảnh, bản đồ đại lục, 3 vực đầu).
- [ ] Art Bible v0.1: **thử nghiệm phong cách low-poly ở đúng góc camera game**: 1 nhân vật, 1 quái, 1 góc môi trường, VFX 1 kỹ năng. Chốt phong cách.
- [ ] TDD v0.1: kiến trúc, protocol, schema DB, mô hình `MapInstance` + chuyển bản đồ.
- [ ] **Spike kỹ thuật:**
  - [ ] Babylon.js: camera 2.5D, nhân vật GLB có animation, di chuyển WASD + chuột nhắm hướng, 1 bản đồ test 300×300m với ~2.000 prop → đo FPS trên laptop văn phòng.
  - [ ] Netcode: 2 client di chuyển + bắn skillshot đồng bộ qua game server Colyseus (prediction + reconciliation), giả lập lag 150ms.
  - [ ] Thư viện va chạm 2D trong `shared/sim`, chạy cùng một bộ test ở client và game server.
  - [ ] Map editor bản thô: đặt prop, vẽ vùng đi được, xuất GLB + JSON.
  - [ ] Asset pipeline: Blender → glTF → Meshopt/KTX2 tự động (glTF-Transform).
  - [ ] **Đánh giá mã nguồn mở** (mỗi hạng mục ≤ 3 ngày, ghi kết quả vào Decision Log):
    - [ ] Babylon.js Editor: có đủ làm nền map editor không (plugin, xuất dữ liệu logic)?
    - [ ] Ink + inkjs: viết 3 cơ duyên mẫu, chạy trên game server, preview trên client; lưu / khôi phục trạng thái qua API Service.
    - [ ] recast-navigation-js chạy trên Node.js (game server) với navmesh xuất từ map editor.
    - [ ] Lập OSS Register + kiểm tra license trong CI.
  - [ ] **Đánh giá T5C (3–5 ngày):** fork, chạy local, đọc kiến trúc, thử 100 bot, xác minh LICENSE → quyết định **fork hay dựng từ đầu** (D-19, cổng 5.7.7).
- [ ] **Prototype giấy vòng đời một mạng:** thẻ bài + spreadsheet mô phỏng 1 kiếp (roll thiên phú, cơ duyên, luyện đan, đột phá, hiểm địa, chết / tọa hóa); chơi thử nội bộ trước khi code.
- [ ] Thiết lập repo, CI, docker-compose, quy trình làm việc.

**Tiêu chí hoàn thành:** spike chạy được; phong cách mỹ thuật được chốt; team đồng thuận GDD tổng quan & phạm vi MVP.

### Phase 1 — Prototype combat + 1 bản đồ (Vertical Slice nhỏ) · Tháng 3–5
**Mục tiêu:** chứng minh **combat top-down vui** và **đi khám phá 1 bản đồ thấy thú vị** khi chơi qua mạng. Đây là cột mốc quan trọng nhất: nếu chưa vui thì dừng lại sửa trước khi làm gì khác.
- [ ] Nhân vật với 1 vũ khí (kiếm): combo đánh thường, đánh mạnh, né lướt, hộ thể/phản đòn, 4 kỹ năng (đủ các dạng: phóng, vùng, quạt, lướt).
- [ ] 3 loại quái thường + 1 tinh anh + 1 boss có 3–4 chiêu có telegraph.
- [ ] Server: AI quái, xác thực hit 2D, lag compensation, sát thương, chết/hồi sinh.
- [ ] **1 bản đồ dã ngoại hoàn chỉnh theo checklist mật độ (4.6)** + 1 bí cảnh nhỏ 1–3 người.
- [ ] Thu thập linh thảo cơ bản, **3–5 cơ duyên mẫu** để kiểm chứng cảm giác.
- [ ] **Hiểm địa thử nghiệm:** vòng lặp vào–lấy–thoát, hấp hối, rơi đồ khi chết (chưa cần chết thật), để kiểm chứng độ căng thẳng.
- [ ] **Màn tạo nhân vật roll thiên phú** bản thô (10–15 thiên phú) để kiểm chứng độ hào hứng.
- [ ] Ngày/đêm cơ bản.
- [ ] VFX & SFX đòn đánh (hit stop, camera shake, tia sáng khi trúng): "game feel".
- [ ] Combat debug overlay.
- [ ] **Playtest nội bộ hằng tuần** + 1 lần playtest với người ngoài team.

**Tiêu chí hoàn thành:** người test chơi 20 phút và muốn chơi tiếp, tự nói về điều họ "tìm thấy"; combat ổn định ở 150ms ping; hiểm địa tạo được cảm giác "tham hay về".

### Phase 2 — Khung MMO & Open world cốt lõi · Tháng 6–9
**Mục tiêu:** từ prototype thành thế giới nhiều bản đồ có vòng lặp tiến triển.
- [ ] Tài khoản, tạo nhân vật (preset ngoại hình + đổi màu, chọn linh căn).
- [ ] **Thành trấn + 3 bản đồ dã ngoại nối nhau**, chuyển bản đồ (handoff), kênh (channel), World Directory.
- [ ] Truyền tống trận, bản đồ thế giới + fog of war, Vạn Giới Lục (bản đầu).
- [ ] Hệ thống tu luyện: tu vi, cảnh giới Luyện Khí → Trúc Cơ, đả tọa, linh mạch; tu vi đánh quái có giới hạn, bình cảnh, đan độc.
- [ ] **Vòng đời nhân vật:** thọ nguyên & đồng hồ tuổi, trọng thương, chết thật, tổng kết kiếp, luân hồi cơ bản, Tàng Bảo Các, Bia Vạn Cổ.
- [ ] **Tạo nhân vật:** 3–4 xuất thân, pool ~30 thiên phú, roll lại / khóa dòng.
- [ ] Túi đồ, trang bị (hiển thị trên nhân vật), rơi đồ, NPC cửa hàng.
- [ ] Hệ thống nhiệm vụ (data-driven) + 15–20 nhiệm vụ chương 1.
- [ ] Hệ thống cơ duyên (engine + encounter editor) + 10 cơ duyên.
- [ ] Vũ khí thứ 2 (pháp trượng/quạt).
- [ ] Chat, tổ đội.
- [ ] Lưu trữ DB đầy đủ, ledger/log.
- [ ] Map editor hoàn thiện (tán prop tự động, bake ánh sáng).
- [ ] Admin panel v1 + Death Review.
- [ ] Load test: 300 bot trong 1 thành trấn, 50 bản đồ×kênh trên 1 tiến trình.

**Tiêu chí hoàn thành:** người chơi mới có thể chơi liền mạch 3–4 giờ đầu game, di chuyển qua lại giữa các bản đồ không lỗi.

### Phase 3 — Chế tác & lấp đầy Vực 1 · Tháng 10–13
- [ ] Luyện Đan đầy đủ (minigame + tự động, cấp nghề), khai khoáng.
- [ ] **Hoàn thiện Vực 1: 6 bản đồ dã ngoại + 3 bí cảnh**, thời tiết.
- [ ] Cơ duyên: đạt 20–30 cái, có ít nhất 2 chuỗi cơ duyên nhiều bước.
- [ ] **Đột phá dựa trên vật phẩm:** đan đột phá Trúc Cơ / Kim Đan, tỷ lệ thành công, tâm ma; Kim Đan (bản giới hạn) + **Thiên Kiếp** v1.
- [ ] **2 hiểm địa chết thật** (nhất giai, nhị giai), cân bằng phần thưởng – rủi ro.
- [ ] Balance Simulator: mô phỏng tuổi thọ & tiến trình của nhiều kiểu người chơi.
- [ ] Hoàn thiện chương 1, khởi động chương 2.
- [ ] UI/UX polish toàn bộ, tutorial / onboarding.
- [ ] Tối ưu hiệu năng (đạt ngân sách 5.2).

**Tiêu chí hoàn thành:** đủ nội dung cho ~15–20 giờ chơi, trong đó ≥ 30% thời gian là khám phá/chế tác (không chỉ đánh quái).

### Phase 4 — Closed Alpha · Tháng 14–15
- [ ] Mời 100–300 người chơi test (cộng đồng tiên hiệp, Discord/Facebook group).
- [ ] Thu thập telemetry: tỷ lệ rời bỏ theo từng bước, **heatmap di chuyển trên bản đồ** (vùng nào bị bỏ qua), tỷ lệ gặp cơ duyên, thời gian đột phá, phân bố tiền tệ.
- [ ] **KPI riêng của game một mạng:** thời gian sống trung bình mỗi kiếp, phân bố nguyên nhân chết, **tỷ lệ người chơi bắt đầu kiếp mới trong 24h / 7 ngày sau khi chết** (chỉ số sống còn), khảo sát "cái chết có công bằng không".
- [ ] Sửa lỗi, cân bằng, tối ưu server.
- [ ] Bắt đầu Luyện Khí + Chế tạo công pháp (bản đầu), sự kiện thế giới (bản đầu).

### Phase 5a — Closed Beta 1 · Tháng 16–18 *(hệ thống chiều sâu + xã hội)*
- [ ] Luyện Khí đầy đủ (cường hóa, tẩy luyện, thăng phẩm), Chế tạo công pháp đầy đủ, Sàn đấu giá + giao dịch.
- [ ] Tông môn tự lập + Tông quy, **Tạc Thiên Bang & Hợp Hoan Tông** (tổng đàn, nhánh gia nhập, xuất thân), danh vọng phe.
- [ ] **PvP dã ngoại có kiểm soát** (GD-32): chế độ PK, sát nghiệp, Chấp Pháp, Thiên Đạo Hóa Thân.
- [ ] Thiên kiếp + binh giải, thức tỉnh / thay đổi thiên phú, động phủ cá nhân.
- [ ] Chống hack, bảo mật, pentest cơ bản; load test 500+ CCU.

### Phase 5b — Closed Beta 2 · Tháng 19–21 *(nội dung mở rộng)*
- [ ] **Linh thú** (bắt Linh chủng / boss, trứng, lai tạo), Ngự kiếm phi hành.
- [ ] **Vực 2** (ít nhất 4 bản đồ + 2 bí cảnh), cửa hàng phe Vực 2, tầng KD / NA của Di Tàng.
- [ ] Sự kiện thế giới động + World boss, Ma Uyên, Thượng Cổ Tiên Phủ, **Ma Vực**.
- [ ] Sơn môn tông môn trên bản đồ, di sản – động phủ truyền thừa.
- [ ] Monetization cơ bản (cosmetic shop) + thanh toán.
- [ ] Load test 1000+ CCU.
- [ ] Wipe dữ liệu trước Open Beta (thông báo trước).

### Phase 6 — Open Beta / Launch · Tháng 22+
- [ ] Marketing, landing page, trailer (góc nhìn 2.5D + VFX rất hợp làm clip ngắn TikTok/Shorts).
- [ ] Vận hành live: on-call, hotfix, sự kiện.
- [ ] Lộ trình sau launch: Luận Võ Đài, Vực 3–4, xây tự do Hoang Vực, phẩm Thiên của chế công pháp, mobile web (cảm ứng).

### Tổng quan dạng timeline
```
Tháng:    1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22
Phase 0  ██████
Phase 1        █████████
Phase 2                 ████████████
Phase 3                             ████████████
Phase 4                                         ██████
Phase 5a                                              █████████
Phase 5b                                                       █████████
Phase 6                                                                 ███→
```

---

## 8. Danh mục tài liệu thiết kế cần viết

> Tất cả đặt trong `docs/`, viết bằng Markdown, quản lý version cùng code. Mỗi tài liệu có owner, trạng thái, và lịch sử thay đổi ở cuối file.
### 8.1. Tài liệu Game Design

| # | Tài liệu | File | Nội dung chính | Owner | Cần trước | Trạng thái |
|---|----------|------|----------------|-------|-----------|-----------|
| GD-01 | **Game Concept Doc** | `docs/design/00-concept.md` | Pitch, pillars, đối tượng, USP, tham khảo (1–3 trang) | Director | Phase 0 | ⬜ |
| GD-02 | **GDD tổng (index)** | `docs/design/01-gdd.md` | Tổng quan, liên kết tới các doc hệ thống | Designer | Phase 0 | ⬜ |
| GD-03 | Core Loop & Progression | `docs/design/02-core-loop.md` | Vòng lặp phút/giờ/ngày/tuần, đường cong tiến triển, thời gian lên cấp | Designer | Phase 0 | ⬜ |
| GD-04 | **Hệ thống Tu luyện** | `docs/design/10-cultivation.md` | Cảnh giới, nguồn tu vi & tỷ trọng, bình cảnh, đan độc, đột phá dựa trên vật phẩm, tâm ma, thiên kiếp | Designer | Phase 1 | 🟡 [v0.1](docs/design/10-cultivation.md) |
| GD-05 | **Combat Design** | `docs/design/11-combat.md` | Điều khiển, combo, frame data, hit reaction, poise, công thức sát thương, ngũ hành | Designer + Client Dev | Phase 1 | ➖ Gộp vào [GD-06](docs/design/12-stats-formulas.md) + [GD-07](docs/design/13-skills.md) (2026-10-23) |
| GD-06 | Chỉ số & Công thức | `docs/design/12-stats-formulas.md` | Toàn bộ chỉ số, công thức tính, bảng tăng trưởng | Designer | Phase 1 | 🟡 [v0.1](docs/design/12-stats-formulas.md) |
| GD-07 | Kỹ năng & Công pháp | `docs/design/13-skills.md` | Danh sách công pháp, kỹ năng, thuần thục | Designer | Phase 1 | 🟡 [v0.1](docs/design/13-skills.md) |
| GD-08 | **Chế tạo Công pháp** | `docs/design/14-technique-crafting.md` | Hạch tâm/thuộc tính/biến thức, power budget, VFX tham số | Designer + Tech Artist | Phase 3 | 🟡 [v0.1](docs/design/14-technique-crafting.md) |
| GD-09 | Trang bị & Vật phẩm | `docs/design/15-items-equipment.md` | Ô trang bị, phẩm cấp, thuộc tính, set, ràng buộc | Designer | Phase 2 | 🟡 [v0.1](docs/design/15-items-equipment.md) |
| GD-10 | **Luyện Đan** | `docs/design/16-alchemy.md` | Đan phương, linh thảo, minigame, phẩm chất, cấp nghề | Designer | Phase 3 | 🟡 [v0.1](docs/design/16-alchemy.md) |
| GD-11 | **Luyện Khí** | `docs/design/17-forging.md` | Chế tạo, cường hóa, tẩy luyện, pháp bảo | Designer | Phase 4 | 🟡 [v0.1](docs/design/17-forging.md) |
| GD-12 | Quái vật & AI | `docs/design/18-enemies-ai.md` | Phân loại quái, hành vi, boss pattern, bảng rơi đồ | Designer | Phase 1 | 🟡 [v0.1](docs/design/18-enemies-ai.md) |
| GD-13 | **Kinh tế** | `docs/design/19-economy.md` | Tiền tệ, faucet/sink, mô phỏng dòng tiền, đấu giá | Designer | Phase 3 | 🟡 [v0.1](docs/design/19-economy.md) |
| GD-14 | Xã hội | `docs/design/20-social.md` | Tổ đội, tông môn, chat, giao dịch, bạn bè | Designer | Phase 2 | 🟡 [v0.1](docs/design/20-social.md) |
| GD-15 | Nhiệm vụ | `docs/design/21-quests.md` | Cấu trúc nhiệm vụ, loại nhiệm vụ, phần thưởng, format data | Designer | Phase 2 | 🟡 [v0.1](docs/design/21-quests.md) |
| GD-16 | Level Design | `docs/design/30-levels/*.md` | Mỗi bản đồ/bí cảnh 1 file: layout, luồng, khu quái, POI, cơ duyên, checklist mật độ | Content Designer | Mỗi bản đồ | ⬜ |
| GD-21 | **Open World & Khám phá** | `docs/design/22-open-world.md` | Cấu trúc đại lục/vực/bản đồ, quy tắc nối bản đồ, chuẩn mật độ nội dung, fog of war, Vạn Giới Lục, thần thức quét, ngày/đêm, thời tiết | Content Designer | Phase 1 | 🟡 [v0.1](docs/design/22-open-world.md) |
| GD-22 | **Cơ duyên (Kỳ ngộ)** | `docs/design/23-encounters.md` | Loại cơ duyên, điều kiện kích hoạt, tỷ lệ, phần thưởng, format kịch bản, danh sách cơ duyên | Content Designer | Phase 1 | 🟡 [v0.1](docs/design/23-encounters.md) |
| GD-23 | Sự kiện thế giới | `docs/design/24-world-events.md` | Loại sự kiện, lịch, quy mô, thưởng theo đóng góp | Designer | Phase 4 | ⬜ |
| GD-24 | Camera & Điều khiển | `docs/design/25-camera-controls.md` | Góc camera, zoom, sơ đồ phím, click-to-move, hỗ trợ ngắm, gamepad/cảm ứng sau này | Designer + Client Dev | Phase 0 | ⬜ |
| GD-25 | **Linh thú** | `docs/design/26-spirit-beasts.md` | Linh chủng & thu phục (cả boss đặc biệt), trứng, đồng hành, lai tạo & biến dị, thú qua các kiếp | Designer + Content | Phase 4 | 🟡 [v0.2](docs/design/26-spirit-beasts.md) |
| GD-31 | Ngự kiếm phi hành | `docs/design/29-sword-flight.md` | Di chuyển nhanh trên bản đồ (tách khỏi GD-25 cũ) | Designer | Phase 4 | ⬜ |
| GD-32 | **PvP dã ngoại & Sát nghiệp** | `docs/design/31-pvp-infamy.md` | Chế độ PK, sát nghiệp, rơi đồ PvP, Huyết thù, Chấp Pháp, truy nã, hệ số PvP | Designer + Server | Phase 4 | 🟡 [v0.3](docs/design/31-pvp-infamy.md) |
| GD-30 | **Động phủ & Sơn môn** | `docs/design/28-caves-sect-sites.md` | Cửa động, động phủ instance, công trình, Linh Điền; địa điểm sơn môn, mặt tiền, Uy vọng, Sơn Môn Đại Hội, phế tích | Designer + Content | Beta | 🟡 [v0.1](docs/design/28-caves-sect-sites.md) |
| GD-26 | **Vòng đời nhân vật: Thọ nguyên · Cái chết · Luân hồi** | `docs/design/03-lifecycle.md` | Đồng hồ tuổi, bảng thọ nguyên, trọng thương / chết thật, tổng kết kiếp, điểm Luân Hồi, Tàng Bảo Các, di sản, Bia Vạn Cổ | Director + Designer | **Phase 0** | 🟡 [v0.1](docs/design/03-lifecycle.md) |
| GD-27 | **Thiên phú & Xuất thân** | `docs/design/04-talents.md` | Quy trình tạo nhân vật, linh căn, pool thiên phú & độ hiếm, roll lại, sự kiện thay đổi thiên phú | Designer | Phase 1 | 🟡 [v0.1](docs/design/04-talents.md) |
| GD-28 | **Hiểm địa** | `docs/design/27-danger-zones.md` | Phân cấp, vào–lấy–thoát, hấp hối, di hài, bảng phần thưởng, quy tắc công bằng | Designer + Content | Phase 1 | 🟡 [v0.1](docs/design/27-danger-zones.md) |
| GD-29 | Ngẫu nhiên & Công bằng (RNG) | `docs/design/05-rng-fairness.md` | Nguyên tắc ngẫu nhiên, hiển thị tỷ lệ, pity, khí vận, kiểm toán | Designer | Phase 1 | 🟡 [v0.1](docs/design/05-rng-fairness.md) |
| GD-17 | UI/UX Spec | `docs/design/40-ui-ux.md` | Wireframe các màn hình, luồng người dùng, HUD | Designer + UI Artist | Phase 1 | ⬜ |
| GD-18 | Onboarding/Tutorial | `docs/design/41-onboarding.md` | 30 phút đầu tiên của người chơi | Designer | Phase 3 | ⬜ |
| GD-19 | Monetization | `docs/design/50-monetization.md` | Mô hình kiếm tiền, shop, battle pass, nguyên tắc chống P2W | Director | Phase 4 | ⬜ |
| GD-20 | Telemetry & KPI | `docs/design/51-analytics.md` | Sự kiện cần tracking, KPI (D1/D7/D30 retention, ARPDAU) | Director + Server Dev | Phase 4 | ⬜ |

### 8.2. Tài liệu Cốt truyện & Thế giới

| # | Tài liệu | File | Nội dung chính | Cần trước |
|---|----------|------|----------------|-----------|
| LR-01 | **World Bible** | `docs/lore/world-bible.md` | Lịch sử, địa lý, hệ thống tu luyện trong lore, thế lực, tông môn | Phase 0 |
| LR-02 | Nhân vật | `docs/lore/characters.md` | NPC chính, phản diện, tính cách, vai trò | Phase 1 |
| LR-03 | Story Outline | `docs/lore/story-outline.md` | Tóm tắt từng chương, plot point chính | Phase 1 |
| LR-04 | Script/Hội thoại | `docs/lore/scripts/*.md` | Kịch bản hội thoại theo chương | Phase 2+ |
| LR-05 | Từ điển thuật ngữ 🟡 [v0.1](docs/lore/glossary.md) | `docs/lore/glossary.md` | Thuật ngữ tiên hiệp thống nhất (VN/EN/ID trong code) | Phase 0 |

### 8.3. Tài liệu Kỹ thuật

| # | Tài liệu | File | Nội dung chính | Cần trước |
|---|----------|------|----------------|-----------|
| TD-01 | **Technical Design Doc (TDD)** 🟡 [v0.1](docs/tech/00-tdd.md) | `docs/tech/00-tdd.md` | Kiến trúc tổng, lựa chọn công nghệ, lý do | Phase 0 |
| TD-02 | **Network Protocol** 🟡 [v0.1](docs/tech/01-network.md) | `docs/tech/01-network.md` | Message, tick, snapshot, prediction, lag compensation | Phase 0 |
| TD-03 | Database Schema 🟡 [v0.1](docs/tech/02-database.md) | `docs/tech/02-database.md` | ERD, bảng, index, chiến lược lưu trạng thái | Phase 1 |
| TD-04 | API Spec | `docs/tech/03-api.md` (+ OpenAPI) | Endpoint REST | Phase 2 |
| TD-05 | Client Architecture | `docs/tech/04-client.md` | ECS, scene management, asset loading, UI | Phase 1 |
| TD-06 | Game Server Architecture | `docs/tech/05-game-server.md` | Tick loop, `MapInstance`/instance, AOI, AI, persistence | Phase 1 |
| TD-07 | Gamedata Format 🟡 [v0.1](docs/tech/06-gamedata.md) | `docs/tech/06-gamedata.md` | Schema JSON của data, quy trình export/validate | Phase 1 |
| TD-08 | Bảo mật & Chống hack | `docs/tech/07-security.md` | Mô hình đe dọa, biện pháp | Phase 2 |
| TD-09 | Hạ tầng & Vận hành | `docs/tech/08-infra-ops.md` | Deploy, monitoring, backup, runbook sự cố | Phase 4 |
| TD-10 | Coding Conventions | `docs/tech/09-conventions.md` | Quy tắc code TS/Java, git flow, review | Phase 0 |
| TD-11 | Performance Budget | `docs/tech/10-performance.md` | Ngân sách FPS, draw call, bộ nhớ, dung lượng tải | Phase 1 |
| TD-12 | **World & Map Architecture** 🟡 [v0.1](docs/tech/11-world-maps.md) | `docs/tech/11-world-maps.md` | `MapInstance`, kênh, World Directory, handoff chuyển bản đồ, World Clock, preload phía client | Phase 0 |
| TD-13 | **Map Format & Map Editor** 🟡 [v0.1](docs/tech/12-map-editor.md) | `docs/tech/12-map-editor.md` | Định dạng dữ liệu bản đồ (hiển thị + logic), tính năng editor, quy trình xuất | Phase 1 |
| TD-14 | Geometry 2D & Collision 🟡 [v0.1](docs/tech/13-geom2d.md) | `docs/tech/13-geom2d.md` | Hình học dùng chung client/server, lưới đi được, tìm đường, bộ test chung | Phase 0 |
| TD-15 | **OSS Register & License Policy** | `docs/tech/14-oss-register.md` | Danh sách thư viện / asset bên thứ ba, license, người duyệt; chính sách 5.7.4 | Phase 0 |

### 8.4. Tài liệu Mỹ thuật & Âm thanh

| # | Tài liệu | File | Nội dung chính | Cần trước |
|---|----------|------|----------------|-----------|
| AR-01 | **Art Bible** | `docs/art/art-bible.md` | Phong cách, bảng màu, tỷ lệ, ví dụ đúng/sai | Phase 0 |
| AR-02 | Asset Guidelines | `docs/art/asset-guidelines.md` | Quy chuẩn kỹ thuật: poly, texture, đặt tên, export | Phase 1 |
| AR-03 | Animation List | `docs/art/animation-list.md` | Danh sách animation cần làm, frame data, ưu tiên | Phase 1 |
| AR-04 | VFX Guidelines | `docs/art/vfx-guidelines.md` | Màu theo nguyên tố, ngân sách particle | Phase 1 |
| AR-05 | Asset Tracker | `docs/art/asset-tracker.md` (hoặc Sheet) | Danh sách toàn bộ asset + trạng thái | Phase 1 |
| AR-06 | **Environment Kits** | `docs/art/environment-kits.md` | Danh sách kit module theo vực, quy chuẩn lắp ghép (grid snap), bảng màu từng vực | Phase 1 |
| AU-01 | Audio Design | `docs/audio/audio-design.md` | Phong cách nhạc (cổ phong), danh sách SFX | Phase 2 |

### 8.5. Tài liệu Quản lý dự án

| # | Tài liệu | File | Nội dung chính |
|---|----------|------|----------------|
| PM-01 | **Plan tổng (file này)** | `PLAN.md` | Tổng quan, roadmap |
| PM-02 | Milestone & Backlog | Jira/Linear/GitHub Projects | Task chi tiết theo sprint |
| PM-03 | Risk Register | `docs/pm/risks.md` | Danh sách rủi ro cập nhật (mở rộng từ mục 10) |
| PM-04 | Decision Log | `docs/pm/decisions.md` | Ghi lại các quyết định quan trọng (ADR): ngày, bối cảnh, lựa chọn, lý do |
| PM-05 | Playtest Reports | `docs/pm/playtests/*.md` | Kết quả mỗi buổi playtest |

### 8.6. Mẫu (template) cho tài liệu hệ thống
Mỗi doc hệ thống (GD-04 → GD-15) nên theo cấu trúc:
```markdown
# [Tên hệ thống]
## 1. Mục tiêu (phục vụ pillar nào, người chơi cảm thấy gì)
## 2. Tổng quan (mô tả 1 đoạn + sơ đồ)
## 3. Quy tắc chi tiết
## 4. Công thức & số liệu (link tới sheet)
## 5. Tương tác với hệ thống khác
## 6. UI/UX liên quan
## 7. Yêu cầu kỹ thuật (client / server / data)
## 8. Trường hợp biên & chống lạm dụng
## 9. Telemetry cần ghi
## 10. Phạm vi MVP vs sau này
## 11. Câu hỏi mở
## Lịch sử thay đổi
```

---

## 9. Quy trình làm việc

- **Sprint 2 tuần:** planning đầu sprint, demo + retro cuối sprint. Demo luôn là **build chơi được**.
- **Playtest nội bộ hằng tuần** từ Phase 1 (30–60 phút, cả team chơi, ghi feedback vào PM-05).
- **Git:** trunk-based (nhánh ngắn hạn + PR + review bắt buộc), Git LFS cho asset nhị phân.
- **Build:** mỗi merge vào main → deploy tự động lên môi trường dev; staging cập nhật theo sprint.
- **Definition of Done:** code review xong, có test cho logic server quan trọng, data validate qua CI, đã test trên build staging, tài liệu liên quan đã cập nhật.
- **Spec cho AI triển khai:** mọi tài liệu GD/TD viết theo quy ước [docs/README.md](docs/README.md): mã yêu cầu `-R-`, tiêu chí nghiệm thu `-AC-` (mỗi tiêu chí là một test tự động), thuật ngữ theo [glossary](docs/lore/glossary.md), tham số nằm trong gamedata.
- **Quản lý quyết định:** quyết định lớn ghi vào Decision Log (PM-04) để người mới hiểu "tại sao".
- **Công cụ:** Git (GitHub/GitLab), Jira/Linear, Discord/Slack, Figma (UI), Google Sheets (gamedata), Miro (sơ đồ).

---

## 10. Rủi ro & phương án giảm thiểu

| # | Rủi ro | Mức độ | Phương án |
|---|--------|:------:|-----------|
| R1 | **Phình phạm vi (scope creep)** — tiên hiệp có vô số tính năng hấp dẫn | 🔴 Cao | Bám pillars & bảng MoSCoW; mọi tính năng mới phải thay thế 1 tính năng cũ hoặc vào backlog "sau launch" |
| R2 | **Combat qua mạng không mượt** trên web | 🟠 TB (giảm nhờ logic 2D) | Spike netcode ngay Phase 0; telegraph trên mặt đất; client-side animation ngay lập tức; va chạm 2D dùng chung |
| R3 | **Hiệu năng web** kém trên máy yếu | 🟡 Thấp-TB (giảm nhờ 2.5D) | Ngân sách hiệu năng từ đầu, đo trên máy yếu mỗi sprint, gộp mesh theo chunk, atlas texture |
| R4 | **Khối lượng asset & animation** vượt sức team | 🟠 TB | Low-poly + kit module, skeleton chung, mua asset pack, giới hạn số vũ khí ở MVP, tái sử dụng quái (đổi màu/kích thước) |
| R4b | **Khối lượng nội dung open world** (bản đồ, cơ duyên, nhiệm vụ): thế giới rộng nhưng trống | 🔴 Cao | Checklist mật độ cho từng bản đồ; map editor + tán prop tự động; cơ duyên data-driven có thể tổ hợp; ưu tiên ít bản đồ nhưng dày đặc hơn nhiều bản đồ trống |
| R4c | **2.5D bị đánh giá là "rẻ tiền"** so với game 3D | 🟠 TB | Art direction mạnh và nhất quán, đầu tư VFX + ánh sáng + game feel; chốt phong cách sớm ở Phase 0 bằng mẫu thật |
| R4d | **Lỗi chuyển bản đồ** (mất đồ, nhân bản, kẹt nhân vật) | 🟠 TB | Handoff có transaction + ticket một lần; test tự động chuyển bản đồ liên tục bằng bot |
| R5 | **Chế tạo công pháp phá vỡ cân bằng** | 🟠 TB | Power budget, test bằng mô phỏng, giới hạn modifier, có thể điều chỉnh sau bằng data |
| R6 | **Lạm phát / phá kinh tế, dupe đồ** | 🟠 TB | Ledger + item log, transaction DB, dashboard kinh tế, sink rõ ràng |
| R7 | **Hack/cheat** (client JS dễ bị đọc) | 🟠 TB | Server-authoritative 100%, không tin client, rate-limit, phát hiện bất thường |
| R8 | **Thiếu người/nghỉ việc** ở vai trò then chốt | 🟠 TB | Tài liệu đầy đủ, code review chéo, không để 1 người duy nhất hiểu 1 hệ thống |
| R9 | **Không có người chơi** khi ra mắt | 🟠 TB | Xây cộng đồng từ Phase 1 (devlog, Discord, TikTok/YouTube), alpha test sớm |
| R10 | **Bản quyền asset / tên gọi** (đạo nhái truyện nổi tiếng) | 🟡 Thấp-TB | Thế giới và tên gọi tự sáng tác; kiểm tra license mọi asset mua/miễn phí |
| R11 | **Một mạng khiến người chơi bỏ game** sau khi chết | 🔴 Cao | Chỉ chết thật khi chủ động mạo hiểm (hiểm địa / đột phá cao); trọng thương thay cho chết ở vùng thường; luân hồi giữ tiến trình tài khoản; màn tổng kết kiếp cảm xúc; tạo kiếp mới nhanh và hào hứng; đo KPI "quay lại sau khi chết" từ Alpha |
| R12 | **Chết vì lag / lỗi server**: mất uy tín ngay | 🔴 Cao | Death log + Death Review, trạng thái quy tức khi mất kết nối, chính sách khôi phục khi sự cố, ưu tiên ổn định server hơn tính năng |
| R13 | **Ngẫu nhiên gây cảm giác bất công** (roll thiên phú tệ, luyện đan hỏng liên tục) | 🟠 TB | Hiển thị tỷ lệ, pity, thiên phú hai mặt (không có roll "vô dụng"), roll lại, giới hạn chênh lệch sức mạnh giữa thiên phú |
| R14 | **Cân bằng thời gian sống khó** (quá ngắn thì nản, quá dài thì mất căng thẳng) | 🟠 TB | Tham số hóa toàn bộ, Balance Simulator, điều chỉnh qua telemetry, playtest dài hạn từ Phase 3 |
| R15 | **Bot / AFK lợi dụng** cơ chế thời gian & bế quan | 🟡 Thấp-TB | Tuổi chỉ trôi khi hoạt động, bế quan offline có giới hạn, phát hiện hành vi bot |
| R16 | **Phụ thuộc mã nguồn mở** (thư viện ngừng phát triển, breaking change, vi phạm license) | 🟡 Thấp-TB | Chọn thư viện có cộng đồng; pin version; bọc qua interface nội bộ; OSS Register + kiểm tra license trong CI; không dùng framework game làm nền (D-16) |
| R17 | **Phụ thuộc fork T5C** (D-19) và **hai ngôn ngữ phía server**: 1 tác giả, chất lượng code chưa rõ | 🟠 TB | Đánh giá kỹ ở Phase 0; fork độc lập; viết test cho phần giữ lại; sẵn sàng viết lại từng module; không phụ thuộc upstream |

---

## 11. Quyết định đã chốt & Câu hỏi mở

### 11.1. Quyết định đã chốt (duyệt ngày 2026-10-19)

> Toàn bộ đề xuất trong plan v0.1–v0.3 đã được duyệt. Khi tạo `docs/pm/decisions.md` (PM-04), chuyển bảng này sang đó, mỗi quyết định một mục ADR.

| # | Quyết định | Nguồn |
|---|-----------|-------|
| D-01 | Đồ họa **2.5D**: 3D low-poly stylized, camera nhìn chéo 45–55°, logic game trên mặt phẳng 2D | 2.1 |
| D-02 | **Open world chia bản đồ**: Đại lục → Vực → bản đồ 250–400m, chuyển bản đồ < 2s, kênh 100–150 người | 2.2 |
| D-03 | **Một mạng + thọ nguyên + luân hồi**; chết thật chỉ ở hiểm địa và thiên kiếp Nguyên Anh+, vùng thường là trọng thương | 4.2 |
| D-04 | **Thiên phú & xuất thân ngẫu nhiên**, roll lại có giới hạn, thay đổi thiên phú qua sự kiện | 4.3 |
| D-05 | **Tu luyện dựa trên vật phẩm / đan dược**; tu vi đánh quái có giới hạn, bình cảnh, đan độc | 4.4 |
| D-06 | Kiến trúc: API Service (Spring MVC) + Game Server, PostgreSQL + Redis; **React** cho UI (Q2). *Sửa bởi D-19:* Game Server dùng Colyseus/TS thay Spring Boot + Netty; Protobuf (Q3) thay bằng `@colyseus/schema` ở kênh realtime | 5 |
| D-07 | **Launch Việt Nam trước, i18n từ đầu** (Q1) | |
| D-08 | MVP: **preset ngoại hình + đổi màu** (Q5) | |
| D-09 | **Camera cố định** ở MVP, cân nhắc xoay sau (Q11) | |
| D-10 | **WASD mặc định**, click-to-move là tùy chọn (Q12) | |
| D-11 | **1 giờ chơi ≈ 1 năm** (tham số); kiếp đầu mục tiêu **40–80 giờ** (Q16) | 4.2 |
| D-12 | **1 nhân vật sống / tài khoản / server**, mở thêm ô bằng Luân Hồi (Q17) | |
| D-13 | **Bảo hộ tân thủ** kiếp đầu: không chết thật trước Trúc Cơ (Q18) | |
| D-14 | **Fog of war giữ qua kiếp**, cơ duyên reset (Q19) | |
| D-15 | Cho phép **tự đặt tên công pháp**, có bộ lọc từ ngữ + báo cáo vi phạm (Q4) | |
| D-16 | **Không dùng RPG-JS làm nền tảng**; dùng thư viện mã nguồn mở theo chiến lược 5.7 | 5.7 |
| D-17 | **Không bán bằng tiền thật** bất cứ thứ gì giảm rủi ro chết / kéo dài thọ nguyên; cosmetic gắn tài khoản | 2.4, 4.19 |
| D-18 | Team: thêm **Content Designer**, 3D artist low-poly; roadmap ~15–17 tháng tới Open Beta | 3, 7 |
| D-19 | **Kiến trúc lai** (duyệt 2026-10-22): client + Game Server realtime bằng TypeScript (Colyseus, fork T5C nếu qua đánh giá Phase 0), code mô phỏng dùng chung `shared/sim`; **Spring Boot là nguồn sự thật** (kinh tế, ledger, một mạng, RNG quan trọng); PostgreSQL + Redis | 5.1–5.5, 5.7.7 |
| D-20 | **Chết không mất trắng** (2026-10-23): đồ mang theo rớt; kho đồ / thư / đấu giá **phong ấn** vào Di Tàng theo tầng cảnh giới, kiếp sau đánh bại **tàn hồn của chính mình** để lấy lại (tiêu tán nếu kiếp sau cũng kết thúc trước khi lấy); động phủ phong ấn, mở bằng **thú canh cửa** | 4.2, GD-26 §5.15 |
| D-21 | **PvP dã ngoại có kiểm soát** (2026-10-23, thay nguyên tắc "không open-world PvP" §2.4): chế độ PK (Hòa bình / Tông môn / Sát lục), **không miễn theo cảnh giới** (v0.2), vùng an toàn + hiểm địa không PvP, giết người yếu hơn nhiều mang sát nghiệp ×3; chết PvP = trọng thương, người vô tội không mất thọ / tu vi; sát nghiệp → tên đỏ → NPC Chấp Pháp truy sát (đánh bại được nhưng rất khó, có thưởng; giết Chấp Pháp → Đại ác nhân + Thiên Đạo Hóa Thân truy sát), chết giảm sát nghiệp; Huyết thù; truy nã; cờ server `pvpEnabled` | 2.4, GD-32 |

### 11.2. Đề xuất chờ duyệt
*(Không còn đề xuất chờ duyệt.)*

### 11.3. Chốt bằng mẫu / playtest
- [ ] **Q13:** Tỷ lệ nhân vật (chuẩn hay chibi nhẹ): chốt bằng mẫu phong cách ở Phase 0.
- [ ] **Q15:** Kích thước bản đồ chuẩn (250m hay 400m): chốt sau playtest bản đồ đầu tiên ở Phase 1.
- [ ] **Q22:** Map editor: mở rộng Babylon.js Editor hay tự viết: chốt sau đánh giá Phase 0 (tiêu chí spike: TD-13 MF-R-031; yêu cầu editor TD-13 §5.1).
- [ ] **Q23:** Ink + inkjs chạy trên game server có đáp ứng cơ duyên (tag trên lựa chọn, lưu / khôi phục trạng thái) không: chốt sau đánh giá Phase 0.

### 11.4. Câu hỏi còn mở
- [x] **Q6:** *(Đã chốt: 10 năm / lần, 2 năm / giờ offline, hiệu suất 0,8, 1 lần / ngày — GD-26 LC-R-132.)* Bế quan offline.
- [ ] **Q7:** Hỗ trợ tay cầm (gamepad) ở MVP không?
- [x] **Q8:** *(Tạm: game **Tiên Lộ**, thế giới **Huyền Hoang Giới** — 2026-10-23; chốt chính thức trước marketing.)* Tên chính thức của game và thế giới.
- [x] **Q9:** *(Đã chốt: **có**, Beta 2 — GD-28 §5.12.)* Ma Vực (hiểm địa PvP, một mạng).
- [ ] **Q10:** Nhà cung cấp cloud / ngân sách hạ tầng hằng tháng.
- [x] **Q14:** *(Đã chốt: **không** giới hạn — ai cũng có cơ hội; GD-22 ENC-Q-1.)* Cơ duyên hiếm có giới hạn toàn server không?
- [x] **Q20:** *(Đã chốt: **không** mở server hòa bình / theo mùa song song ở launch — GD-32 PK-Q-1.)* Có server theo mùa song song không?
- [x] **Q21:** *(Đã chốt: **có** — chỉ trong thiên kiếp lethal, ×0,9, ưu đãi như tọa hóa; GD-26 §5.16.)* Binh giải.

---

## 12. Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-16 | — | Bản nháp đầu tiên: tầm nhìn, phạm vi, team, GDD tổng quan, kiến trúc, roadmap, danh mục tài liệu |
| v0.2 | 2026-10-16 | — | **Đổi hướng: đồ họa 2.5D (3D low-poly, camera trên cao) + open world chia bản đồ.** Thêm pillar Khám phá; thêm mục Thế giới mở, Cơ duyên, Sự kiện thế giới, Linh thú, Động phủ; viết lại combat theo kiểu top-down; kiến trúc `MapInstance`/handoff; logic va chạm 2D; pipeline bản đồ + map editor; cập nhật team (thêm Content Designer), roadmap, tài liệu, rủi ro, câu hỏi mở |
| v0.3 | 2026-10-16 | — | **Thêm lối chơi cốt lõi: một mạng + thọ nguyên + luân hồi.** Thêm pillar P1 "Mỗi kiếp một câu chuyện"; thêm mục Vòng đời nhân vật, Thiên phú & Xuất thân, Hiểm địa (vào–lấy–thoát); tu luyện chuyển sang dựa trên vật phẩm/đan dược (đánh quái có giới hạn, bình cảnh, đan độc); ngẫu nhiên xuyên suốt và nguyên tắc công bằng; cập nhật kinh tế, xã hội, monetization, DB, server (death log, RNG server), roadmap, tài liệu, rủi ro, câu hỏi |
| v0.4 | 2026-10-19 | — | **Duyệt toàn bộ đề xuất v0.1–v0.3** → mục 11 thành bảng quyết định D-01…D-18. Thêm **5.7 Chiến lược mã nguồn mở**: đánh giá RPG-JS (không dùng làm nền), bộ thư viện đề xuất (Ink/blade-ink, recast, Babylon.js Editor, Refine...), chính sách license, ước tính tiết kiệm. Cập nhật Phase 0 (đánh giá OSS, prototype giấy), tài liệu TD-15, rủi ro R16 |
| v0.5 | 2026-10-19 | — | Khảo sát dự án nền tảng mã nguồn mở (5.7.6): **T5C** (Babylon.js + Colyseus) khớp nhất; **đề xuất D-19 kiến trúc lai** (chờ duyệt); bản đồ tận dụng OSS theo module (5.7.8); mẹo tiết kiệm token AI (5.7.9); xác minh license Babylon.js Editor (Apache-2.0), blade-ink (Apache-2.0); Phase 0 thêm đánh giá T5C; rủi ro R17 |
| v0.6 | 2026-10-19 | — | Viết **GD-21 Open World & Khám phá** v0.1 (`docs/design/22-open-world.md`); schema 5.4: fog, truyền tống trận, Vạn Giới Lục chuyển sang theo tài khoản, thêm `character_secret` |
| v0.7 | 2026-10-20 | — | Viết **GD-26 Vòng đời nhân vật** v0.1, **GD-22 Cơ duyên** v0.1, **LR-05 Glossary** v0.1 và **docs/README.md** (quy ước spec cho AI triển khai: mã `-R-`/`-AC-`, kiến trúc trung lập chờ D-19); thêm cảnh giới Phàm nhân (`PN1`) làm điểm bắt đầu |
| v0.8 | 2026-10-20 | — | Viết **GD-27 Thiên phú & Xuất thân** v0.1 (`docs/design/04-talents.md`); đồng bộ GD-21 (cấp thần thức), GD-22 (thiên phú trong trọng số cơ duyên), GD-26 (lý do `TALENT_FUSION`, `rootMultiplier`), glossary |
| v0.9 | 2026-10-20 | — | Viết **GD-04 Hệ thống Tu luyện** v0.1 (`docs/design/10-cultivation.md`): bảng yêu cầu tu vi theo tầng khớp mục tiêu kiếp 40–80 giờ; đồng bộ GD-26 (đả tọa không tính AFK, công thức bế quan), GD-27 (tâm ma chuẩn ở GD-04), GD-22 (loại `HEART_TRIAL`, outcome `MODIFY_BREAKTHROUGH_CHANCE`), glossary |
| v0.10 | 2026-10-20 | — | GD-04 v0.2 + GD-26 v0.2: cooldown sau thất bại đột phá còn 15 phút; trong hồi quang phản chiếu được thử lại ngay, thất bại thì tọa hóa |
| v0.11 | 2026-10-20 | — | Viết **GD-06 Chỉ số & Công thức** v0.1 (danh sách chỉ số chuẩn, bảng cơ bản theo cảnh giới, sát thương, ngũ hành 8 hệ, poise, né / đỡ, status `ST_*`) và **GD-07 Kỹ năng & Công pháp** v0.1 (kỹ năng dạng thành phần, máy trạng thái hành động, 7 tâm pháp, 14 kỹ năng, thuần thục, ngân sách sức mạnh); thêm ô kỹ năng V; đồng bộ GD-04, GD-27, glossary |
| v0.12 | 2026-10-20 | — | Viết **GD-12 Quái vật & AI** v0.1 (`docs/design/18-enemies-ai.md`): máy trạng thái AI, 9 archetype, spawn theo khu / giờ / thời tiết, threat, cảnh báo tối thiểu theo hạng, suất cận chiến, boss theo pha, rơi đồ cá nhân + khí vận, bestiary Vực 1 (16 quái, 4 tinh anh, 3 boss); đồng bộ GD-04, GD-06 (`ST_NIGHT_EMPOWER`), GD-22, glossary |
| v0.13 | 2026-10-20 | — | Viết **GD-28 Hiểm địa** v0.1 (`docs/design/27-danger-zones.md`): vào–lấy–thoát, 4 pha áp lực thời gian, hấp hối & cứu, di hài, quy tức (mất kết nối không gây chết), sự cố server tính là thoát, bẫy / nguy hiểm môi trường / kinh động, HD01 Vạn Độc Đầm + HD02 Cổ Chiến Trường; đồng bộ GD-06 (7 status), GD-07 (`DORMANT_GUARD`), GD-26, GD-21, glossary |
| v0.14 | 2026-10-20 | — | Viết **GD-09 Trang bị & Vật phẩm** v0.1 (`docs/design/15-items-equipment.md`): cổng inventory duy nhất, bảng chuyển vị trí hợp lệ, 9 ô trang bị (thêm phím Tab cho pháp bảo), trang bị chuẩn +30%, phẩm Phàm → Thần + 15 thuộc tính phụ, 3 bộ, ánh xạ độ hiếm ↔ phẩm, 14 cách dùng vật phẩm, danh mục mã lý do `item_log` hợp nhất; đồng bộ GD-06, GD-12, GD-22, glossary |
| v0.15 | 2026-10-21 | — | Viết **GD-13 Kinh tế** v0.1 (`docs/design/19-economy.md`): IPH theo dải làm thước đo gốc, faucet / sink cụ thể, định giá theo giờ công, giá trị trang bị theo phẩm, cửa hàng NPC, chuyển kiếp hạ trần 5.000, giao dịch + Vạn Bảo Lâu (Beta), dashboard & cảnh báo; đồng bộ GD-09 (giá bán theo `gradeValueMult`), GD-12, GD-26, GD-21, GD-22, GD-28; trả lời LC-Q-3, OW-4, DZ-Q-3 |
| v0.16 | 2026-10-21 | — | Viết **GD-10 Luyện đan** v0.1 (`docs/design/16-alchemy.md`): minigame hỏa hầu tất định (server mô phỏng lại từ input + seed), thành / bại, phẩm chất, đan văn, bạo đan, bảo hiểm, đan lô & hỏa chủng, cấp luyện đan sư, 10 đan phương (Trúc Cơ Đan luôn mua được ở Tông Môn Bảo Khố); đồng bộ GD-09 (`TOOL`, đan văn), GD-26 (`CRAFTING_REFUND`), GD-06 (`ST_STAUNCH`), glossary |
| v0.17 | 2026-10-21 | — | Viết **GD-11 Luyện khí** v0.1 (`docs/design/17-forging.md`): dùng chung khung GD-10, minigame rèn theo nhịp, phẩm theo điểm Q + xúc tác định hướng + dị tượng, phân giải & luyện đá, cường hóa không phá hủy (≥ +8 tụt 1 cấp, Hộ Phù), tẩy luyện chọn giữ cũ / mới, thăng phẩm, khắc trận văn. **Đổi phạm vi:** chế tạo trang bị + phân giải vào MVP (Alpha) vì đã có Thợ rèn tiểu tử / Khí Hồn; phần còn lại Beta. Đồng bộ GD-09 (`spiritBorn`, búa / xúc tác / trận văn), glossary |
| v0.18 | 2026-10-21 | — | Viết **GD-14 Xã hội** v0.1 (`docs/design/20-social.md`): tổ đội & Chiêu Mộ, chat 6 kênh + lọc + kiểm duyệt, bạn bè / chặn theo tài khoản, **tách Thanh Vân Tông (NPC, thuộc kiếp) và tông môn tự lập (tài khoản, Beta)**, hàng đợi thông báo chung, phần thưởng hộ pháp. **Sửa lỗi đánh số:** các tham chiếu "GD-15 Xã hội" trong GD-04, GD-09, GD-10, GD-13, GD-22, GD-26 → GD-14 (GD-15 là Nhiệm vụ). Trả lời CU-Q-4, LC-Q-5, OW-1 |
| v0.19 | 2026-10-22 | — | **Quyết định: động phủ & sơn môn theo mô hình "vị trí trên bản đồ, bên trong là instance" (tầng 1 + 2), không xây tự do trên bản đồ chung.** Viết **GD-30 Động phủ & Sơn môn** v0.1; động phủ cá nhân chuyển từ sau launch lên **Beta**; xây tự do (Hoang Vực) để sau launch. Sửa cân bằng: Linh Mạch Điện (GD-14) hạ trần 1,35. Đồng bộ GD-04, GD-13, GD-14, GD-21, GD-26, glossary |
| v0.20 | 2026-10-22 | — | Viết **GD-15 Nhiệm vụ** v0.1 (`docs/design/21-quests.md`): engine nhiệm vụ data-driven (17 loại mục tiêu, nguồn sự kiện domain, Ink dùng chung GD-22), danh sách trắng hành động, Thanh Vân Tông hằng ngày (3 × 100 cống hiến), tông môn tự lập hằng tuần, các chuỗi `QC_TUTORIAL_QI` / `QC_MAIN_V1_JOIN_SECT` / `QC_SECT_SERVANT` / `QC_QUIET_HEART` / `QC_GREAT_LIMIT_*`, **lược truyện cho kiếp sau**, 15 nhiệm vụ chính chương 1; đồng bộ GD-04, GD-07, GD-22, GD-26, glossary |
| v0.21 | 2026-10-22 | — | Viết **GD-08 Chế tạo công pháp** v0.1 (`docs/design/14-technique-crafting.md`): 8 hạch tâm, 8 nguyên tố, 15 biến thức + 3 hiếm dạng phép vá dữ liệu, thuật toán ghép tất định + điểm ngộ đạo `q` + đột biến + **áp chế ngân sách**, Ngộ Đạo Thạch + Diễn Pháp Tràng, ngọc giản tự chế `CSK_*` (học, Tàng Bảo Các, sao chép ≤ 20 bản khi người tạo còn sống, tán giản); chế công pháp mở từ **Trúc Cơ** (Hoàng – Huyền), Địa ở Kim Đan; đồng bộ GD-04, GD-07, GD-09, GD-11, glossary |
| v0.22 | 2026-10-22 | — | **Duyệt D-19 kiến trúc lai**: Game Server = Colyseus / TypeScript (fork T5C nếu qua đánh giá Phase 0, nếu không thì dựng từ đầu trên Colyseus), code mô phỏng dùng chung `shared/sim`; Spring Boot giữ vai trò nguồn sự thật; sửa D-06 (bỏ Netty; Protobuf ở kênh realtime thay bằng `@colyseus/schema`), §5.1–5.3, §5.5, §5.7.2 (bỏ recast4j, blade-ink, gdx-ai), Phase 0, Q23, R17; README §2.4, GD-22 ENC-Q-4 |
| v0.23 | 2026-10-22 | — | Viết **TD-01 TDD** v0.1 (`docs/tech/00-tdd.md`): ma trận quyền hạn API Service / Game Server / World Service, API Service dạng modular monolith với cổng ghi, quy ước API + lỗi + ID, idempotency (`idempotency_record`), chuẩn ledger, RNG xoshiro128** + bảng `rng_audit`, sự kiện domain (outbox → Redis Streams), **`CharacterContext`** cho kiểm tra vị trí (chốt câu hỏi GD-10), death log, sự cố & phục hồi, join ticket; đồng bộ GD-09, GD-10, GD-12, GD-28, README, glossary |
| v0.24 | 2026-10-23 | — | Viết **TD-07 Gamedata Format** v0.1 (`docs/tech/06-gamedata.md`): kiểu TS là nguồn duy nhất → JSON Schema + Java sinh tự động, JSONC / CSV, đơn vị theo hậu tố, **mọi tỷ lệ là phân số**, bảng đăng ký tiền tố ID, cấm đổi tên / tái dùng ID đã phát hành, gắn phase theo hồ sơ build, i18n `vi` bắt buộc đủ, generator, pipeline + `gamedataVersion`, ảnh chụp dữ liệu cho phiên dài, validator theo GD, hàm hai ngôn ngữ + testvectors; dữ liệu bản đồ chuyển về `shared/maps`; sửa GD-21 (`scan.manaCostPct` 0,10), TD-01, README, glossary |
| v0.25 | 2026-10-23 | — | Viết **GD-29 Ngẫu nhiên & Công bằng** v0.1 (`docs/design/05-rng-fairness.md`): 5 loại roll, `ChanceView` + báo giá + chống save-scum, band độ hiếm cho phần thưởng khám phá, **tách dữ liệu bí mật khỏi client** (chốt TD-07 DT-Q-3: hai bundle, `@serverOnly`), tách seed minigame / kết quả, khí vận **không** ảnh hưởng tỷ lệ thành công chính và sinh tử, quy ước điểm phần trăm (`@unit pp`), 4 dạng pity + bảng đăng ký, Thiên Cơ Lục; đồng bộ TD-07 v0.2, TD-01, GD-04, GD-10, GD-11, README, glossary |
| v0.26 | 2026-10-23 | — | Viết **TD-02 Network Protocol** v0.1 (`docs/tech/01-network.md`): tick 20Hz, State (`@colyseus/schema`, lượng tử hóa) / Message, **`C2S_Input`** + dự đoán / đối chiếu, nội suy 100ms, AOI 40/48m + thực thể ẩn, bù trễ (rewind tấn công ≤ 200ms, xét lại phòng thủ ≤ 100ms, hoãn xác nhận hấp hối), danh mục message, giới hạn tần suất, kết nối lại 60s, **chat qua Game Server + Redis** (chốt TA-Q-5), ngân sách băng thông; và **TD-14 Geometry 2D** v0.1 (`docs/tech/13-geom2d.md`): tất định (cấm hàm lượng giác, bảng tra), thân tròn theo cỡ, đi xuyên thực thể, lớp va chạm có cờ, collide-and-slide, hình học từng `Shape` + **trúng theo thân**, đạn quét / xuyên / nổ, LOS, navmesh theo cỡ; đồng bộ GD-06, GD-07, GD-12 (`size`), TD-01, README, glossary |
| v0.27 | 2026-10-23 | — | Viết **GD-25 Linh thú** v0.1 (`docs/design/26-spirit-beasts.md`, tách Ngự kiếm sang **GD-31**): đánh giá, **Linh chủng** + bắt bằng Ngự Thú Phù (thấy tỷ lệ, thần thức, pity theo loài), trứng + ấp, kết khế / xuất chiến / chiêu lệnh / thân mật, **sức mạnh tính từ chủ** (trần ~17% DPS), Linh Thú Viên, **lai tạo + biến dị theo độ hiếm** + lai loài, thú qua các kiếp (chọn giữ theo ô), thọ thú, Thú Ấn; PLAN §4.17 viết lại; đồng bộ GD-09 (`BEAST_EGG`, `CAPTURE_BEAST`, `HATCH`), GD-12 (`beastSpeciesId`), GD-26, GD-29, GD-30 (`THU_VIEN`), TD-01, TD-07, README, glossary |
| v0.28 | 2026-10-23 | — | **GD-25 v0.2: bắt boss đặc biệt** — cửa sổ Kiệt Sức 8s ở 10% HP, phù ≥ Huyền, tỷ lệ 1–15% + pity tài khoản, 1 lượt / người / trận, 3 lượt / ngày, đổi phần rơi đồ boss lấy thú boss (đặc tính đặc trưng, không lai ra được); đồng bộ GD-12, GD-29, TD-01 |
| v0.29 | 2026-10-23 | — | **D-20 Di Tàng phong ấn & Tàn hồn** (GD-26 v0.3): đồ mang theo rớt, kho đồ phong ấn 4 tầng mở bằng đánh bại tàn hồn kiếp trước ở Luân Hồi Đài, động phủ mở bằng thú canh cửa; đồng bộ GD-09 (`SEALED`), GD-12, GD-13, GD-25, GD-30, glossary |
| v0.30 | 2026-10-23 | — | Chốt **SF-Q-1** (GD-06 v0.2): hệ số tăng trưởng **tăng dần theo cảnh giới** (`g` 1,10→1,18 mỗi tầng, `J` 1,6→2,2 mỗi cảnh giới lớn) để chênh lệch cảnh giới cao rõ rệt; LK – TC1 không đổi; đồng bộ GD-09 (`realmScale` dùng hàm chung), GD-13 (`majorScale` cho linh thạch rơi) |
| v0.31 | 2026-10-23 | — | Chốt **AL-Q-1**: **ký ức nghề** (cấp tài khoản) + **khảo hạch theo giai đoạn** Sơ / Trung / Cao để khôi phục cấp luyện đan / luyện khí và công thức kiếp trước (GD-10 v0.2 §5.9, GD-11 v0.2 §5.12); GD-26 bước 10 ghi ký ức nghề |
| v0.32 | 2026-10-23 | — | Chốt **SO-Q-3** (GD-14 v0.3 §5.4b): ba đại tông NPC — Thanh Vân Tông (chính), **Tạc Thiên Bang** (tà, hiểm địa / hắc thị), **Hợp Hoan Tông** (ma, song tu / mị thuật); chọn khi bái sư chương 1 (nhánh mới ở Beta), một tông mỗi kiếp, phản môn, phe làm điều kiện cơ duyên; đồng bộ GD-15, GD-10 (Trúc Cơ Đan ở mọi Bảo Khố), glossary |
| v0.33 | 2026-10-23 | — | Duyệt **SO-Q-6** (xuất thân Bang chúng Tạc Thiên, Hợp Hoan đệ tử — GD-27 v0.2, GD-15, GD-26 unlock), **SO-Q-7** (danh vọng phe chính / tà / ma — GD-14 §5.4c, GD-22 / GD-15 `CHANGE_REPUTATION`), chốt **SO-Q-5** (Thiên Bảng Chiến Lực top 100 — chỉ top 100 được thông báo tử vong toàn server, GD-14 §5.4d) |
| v0.34 | 2026-10-23 | — | **Cân bằng tông NPC / tông tự lập** (GD-14 v0.5 §5.4e): một gói phúc lợi (sư môn NPC · Tông quy · Tán tu tự tại), đổi mỗi 7 ngày; công trình trú địa và Bảo Khố không bị loại trừ; Tông quy 8 lựa chọn theo cấp tông; Trúc Cơ Đan mở ở mọi con đường (Tông Khố 300 merit, Tạp Hóa 3.000 linh thạch — GD-13 v0.2, GD-10) |
| v0.35 | 2026-10-23 | — | Chốt **OW-5** (GD-21 v0.2 §3.10): linh thảo hiếm **hiển thị chung, tranh đoạt** — khóa vận hái 4s, thủ hộ linh thú, làm mới đổi vị trí, chống đổi kênh (`ST_QI_UNSETTLED`), thông báo dị tượng trong bản đồ; đồng bộ GD-06, GD-12, TD-02 |
| v0.36 | 2026-10-23 | — | **D-21 PvP dã ngoại có kiểm soát**: viết **GD-32** v0.1 (`docs/design/31-pvp-infamy.md`); thay nguyên tắc "không open-world PvP"; đồng bộ GD-06 (chốt SF-Q-4), GD-07, GD-14, GD-21, GD-25, GD-26, GD-28, TD-02, TD-07, glossary |
| v0.37 | 2026-10-23 | — | **GD-32 v0.2:** bỏ miễn PvP dưới LK5 và giới hạn chênh cảnh giới (chống lợi dụng nhân vật phụ), thay bằng sát nghiệp ×3 khi giết người thấp hơn ≥ 1 cảnh giới lớn; Chấp Pháp đánh bại được (rất khó, thưởng Chấp Pháp Lệnh); giết Chấp Pháp → Đại ác nhân + Thiên phạt, Thiên Đạo Hóa Thân truy sát; đồng bộ GD-06, GD-12 |
| v0.38 | 2026-10-23 | — | Chốt **PK-Q-4** (GD-32 v0.3): thành viên Tạc Thiên Bang hạ Thiên Đạo Hóa Thân nhận thưởng **chỉ biểu tượng** (danh hiệu theo bậc Tạc Thiên Tôn Giả → Chí Tôn → Đại Đế, hào quang, Bia Vạn Cổ, lời thoại; danh vọng trong trần ngày) |
| v0.39 | 2026-10-23 | — | Lưu ý nội dung: NPC Bang chủ / Trưởng lão Tạc Thiên Bang mang sẵn danh hiệu Tạc Thiên Đại Đế / Chí Tôn / Tôn Giả, lời thoại phản ứng theo danh hiệu người chơi (GD-32 PK-R-056, GD-14 SO-R-045B) |
| v0.40 | 2026-10-23 | — | **Chốt loạt câu hỏi tổng quan:** giãn roadmap 15–17 → **21–22 tháng**, Beta chia 5a / 5b; tên tạm **Tiên Lộ** / thế giới **Huyền Hoang Giới** (battle pass đổi tên Vấn Đạo Lệnh); không server hòa bình; `V1_F01` không PvP; **Ma Vực** ở Beta 2; không cơ duyên độc nhất toàn server; **binh giải** (GD-26 §5.16); bế quan offline 10 năm / lần, 1 lần / ngày; phí giải ấn Di Tàng; **GD-05 gộp vào GD-06/07**; đồng bộ bảng cảnh giới (chế công pháp ở Trúc Cơ), §4.13 kinh tế (Di Tàng), bảng phạm vi (thêm Di Tàng, ký ức nghề, ba tông, danh vọng phe, Ma Vực) |
| v0.41 | 2026-10-23 | — | Viết **TD-03 Database Schema** v0.1 (`docs/tech/02-database.md`): quy ước, phạm vi dữ liệu + **ma trận giữ qua kiếp**, sổ đăng ký ~120 bảng, DDL bảng lõi (account, `character` gộp cột, platform, world, danh vọng, gói phúc lợi, PvP), phân vùng / hạn lưu, thứ tự khóa, migration, khóa Redis; PLAN §5.4 thay bằng tham chiếu; sửa khóa `npc_sect_membership` (GD-14), thêm `seal_band` (GD-09), module `beast` / `pvp` (TD-01) |
| v0.42 | 2026-10-23 | — | Viết **TD-12 World & Map Architecture** v0.1 (room mở / instance, đặt room theo tải, chọn / mở / gộp kênh, máy trạng thái chuyển bản đồ < 2s, instance, World Clock & Weather tất định, fog gộp OR, drain) và **TD-13 Map Format & Map Editor** v0.1 (`MapDef`, 18 loại POI, yêu cầu editor độc lập Q22, pipeline build, 12 validator, ngân sách bản đồ; chốt DT-Q-4); đồng bộ GD-21, TD-02, TD-07, glossary |





---

### Bước tiếp theo (ngay tuần này)
1. Tạo cấu trúc `docs/`, chuyển bảng quyết định 11.1 vào **Decision Log** (PM-04), lập **OSS Register** (TD-15).
2. Viết song song: **GD-26 Vòng đời nhân vật** (nền tảng nhất), **GD-01 Concept Doc**, **GD-24 Camera & Điều khiển**, **LR-01 World Bible** (kèm bản đồ đại lục), **TD-01 TDD**, **TD-12 World & Map Architecture**, **AR-01 Art Bible**.
3. Làm **prototype giấy vòng đời một mạng** để kiểm chứng sớm cảm giác roll thiên phú, cơ duyên, đột phá, cái chết.
4. Artist làm **mẫu phong cách** (1 nhân vật + 1 góc môi trường + 1 VFX) ở đúng góc camera, để chốt Q13.
5. Dev **đánh giá T5C trước** (3–5 ngày) để quyết định fork hay dựng từ đầu trên Colyseus (D-19), vì kết quả quyết định cách làm các spike còn lại (camera 2.5D, netcode skillshot, va chạm 2D, Babylon.js Editor, Ink, recast).
6. Trả lời các câu hỏi còn mở ở 11.4, ưu tiên **Q10** (ngân sách hạ tầng).

