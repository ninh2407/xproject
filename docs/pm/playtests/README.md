# PM-05 — Playtest Reports

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | PM-05 · Prefix ID: `PT` |
| Owner | Producer (lịch, quy trình) + Game Designer (mục tiêu, phân tích) |
| Trạng thái | 🟡 Bản nháp v0.2 |
| Ngày | 2026-10-25 |
| Chu kỳ | Nội bộ hằng tuần từ Phase 1 (PLAN §9); buổi lớn theo cột mốc phase (§3) |
| Liên quan | [PLAN §7, §9, §11.3](../../../PLAN.md) · [GD-20 Telemetry & KPI](../../design/51-analytics.md) §4.8, §7, §8 · [GD-18 Onboarding](../../design/41-onboarding.md) §6, §8 · [PM-03 Risk Register](../risks.md) · [PM-04 Decision Log](../decisions.md) · [TD-08 Bảo mật](../../tech/07-security.md) SEC-R-075 · [TD-09 Hạ tầng](../../tech/08-infra-ops.md) (môi trường staging) · [docs/README.md](../../README.md) §2 · [Mẫu báo cáo](_template.md) |

> Thư mục này chứa **một file báo cáo cho mỗi buổi playtest** (§8) và tệp quy trình này. Báo cáo là **bằng chứng**, không phải nơi ra quyết định: quyết định ghi ở PM-04, thay đổi hành vi ghi ở GD / TD tương ứng (docs/README §3.7). AI được phép tạo báo cáo từ ghi chép + số liệu, và đề xuất cập nhật câu hỏi mở theo §7, nhưng không tự đóng câu hỏi mở.

---

## 1. Bất biến

| Mã | Bất biến |
|----|----------|
| <a id="pt-r-001"></a>**PT-R-001** | Mỗi buổi một file theo §8, tạo từ [`_template.md`](_template.md), hoàn thành **trong 2 ngày làm việc** sau buổi. Không gộp nhiều buổi vào một file. |
| <a id="pt-r-002"></a>**PT-R-002** | **Ẩn danh:** người tham gia ghi bằng mã `P01`, `P02`…; không ghi email, số điện thoại, tên tài khoản / nhân vật thật, IP (TD-08 SEC-R-075; GD-20 TM-R-041, TM-R-045). Danh sách liên hệ người ngoài (nếu cần mời lại) giữ ngoài repo, do Producer quản lý. |
| <a id="pt-r-003"></a>**PT-R-003** | Số liệu định lượng dùng **đúng định nghĩa KPI / sự kiện của GD-20** (§7, §4) và funnel GD-18 §8; không tự định nghĩa chỉ số mới trong báo cáo. Cần chỉ số mới → đề xuất `TM-Q-*` ở GD-20. |
| <a id="pt-r-004"></a>**PT-R-004** | Mỗi phát hiện có **mức độ** (§6.1) và **hành động** trỏ tới mã: task backlog (PM-02), câu hỏi `{PREFIX}-Q-{n}`, quy tắc `-R-`, rủi ro `R-nn`, hoặc quyết định `D-nn`. Phát hiện không có hành động phải ghi rõ "chỉ theo dõi". |
| <a id="pt-r-005"></a>**PT-R-005** | Ghi đúng **build + `gamedataVersion`** (TD-01 TA-R-150; glossary) của buổi để so sánh giữa các buổi và tái hiện lỗi. |
| <a id="pt-r-006"></a>**PT-R-006** | Mẫu nhỏ (< 30 người) chỉ cho **tín hiệu định tính**; không dùng để kết luận đạt / trượt mục tiêu KPI GD-20 §7 (các mục tiêu đó dành cho Closed Alpha trở đi). Báo cáo ghi rõ `n` cạnh mọi tỷ lệ. |

---

## 2. Loại buổi

```ts
// Giá trị dùng trong thuộc tính "Loại buổi" của báo cáo (§8, _template.md)
type PlaytestType =
  | 'PAPER'          // prototype giấy vòng đời (Phase 0) — thẻ bài + spreadsheet
  | 'STYLE_REVIEW'   // xem mẫu phong cách / asset đúng camera game (Phase 0, AR-01 §10)
  | 'INTERNAL'       // nội bộ hằng tuần, cả team (PLAN §9)
  | 'FOCUSED'        // nội bộ theo chủ đề: 1 hệ thống / 1 câu hỏi mở (vd. DZ-Q-4)
  | 'FRIENDS'        // người ngoài team (bạn bè, cộng đồng nhỏ), có người quan sát
  | 'FTUE_LAB'       // quan sát 1–1 người chưa từng chơi, think-aloud, 30–45 phút đầu (GD-18)
  | 'CLOSED_ALPHA'   // Phase 4, 100–300 người, telemetry đầy đủ
  | 'CLOSED_BETA'    // Phase 5a / 5b
  | 'OPEN_BETA';     // Phase 6 — báo cáo định kỳ (tuần), không theo buổi
```

| Loại | Người tham gia | Thời lượng | Môi trường | Thu thập chính | Ai chủ trì |
|------|----------------|-----------|------------|----------------|-----------|
| `PAPER` | Team (4–8) | 60–90' | Bàn + spreadsheet | Ghi chép, bảng mô phỏng kiếp | Game Designer |
| `STYLE_REVIEW` | Team + 3–5 người ngoài | 30' | Build client đứng một mình / cảnh mẫu | Chấm điểm A/B, khảo sát §5.3 | Art Director |
| `INTERNAL` | Cả team | 30–60' (PLAN §9) | staging (TD-09) | Ghi chép, khảo sát ngắn §5.1, telemetry nếu có | Producer (luân phiên) |
| `FOCUSED` | 3–6 người team | 30–60' | staging / dev | Số liệu đo đúng câu hỏi | Owner của GD liên quan |
| `FRIENDS` | 5–15 người ngoài team | 60–90' | staging, tài khoản test | Quan sát, khảo sát §5.1 + §5.2, telemetry | Game Designer + 1 người ghi |
| `FTUE_LAB` | 3–6 người mới hoàn toàn, từng người | 45' | staging, quay màn hình (có đồng ý) | Funnel GD-18 §8, thời gian từng bước, điểm bối rối | UI/UX Designer |
| `CLOSED_ALPHA` / `CLOSED_BETA` | 100–300 / 500+ | Nhiều tuần | Môi trường test riêng | Dashboard GD-20 TM-R-050, khảo sát trong game + biểu mẫu | Producer + Game Designer |
| `OPEN_BETA` | Công khai | Liên tục | prod | Dashboard + cảnh báo TM-R-051, khảo sát định kỳ | Producer |

- <a id="pt-r-010"></a>**PT-R-010** Người ngoài team (`FRIENDS`, `FTUE_LAB`, Alpha / Beta): đồng ý thu thập dữ liệu (TM-R-042) và, nếu quay màn hình / ghi âm, đồng ý riêng bằng văn bản; bản ghi không đưa vào repo, giữ tối đa 90 ngày.
- <a id="pt-r-011"></a>**PT-R-011** Người tham gia `FTUE_LAB` không được là người đã xem build hoặc tài liệu thiết kế; mỗi người chỉ tham gia `FTUE_LAB` một lần cho cùng một phiên bản onboarding.
- <a id="pt-r-012"></a>**PT-R-012** Khảo sát / biểu mẫu cho người ngoài team chỉ dùng công cụ có dòng ✅ ở [TD-15](../../tech/14-oss-register.md) §4.4 (OSS-R-022); hiện dòng "Công cụ khảo sát playtest" đang **chờ chọn** → buổi `FRIENDS` / `FTUE_LAB` dùng biểu mẫu giấy hoặc khảo sát do người chủ trì đọc và ghi theo mã `P01…`. Biểu mẫu không hỏi email / số điện thoại / tên tài khoản (PT-R-002).

---

## 3. Lịch playtest theo phase (PLAN §7)

| Phase | Buổi bắt buộc | Mục tiêu chính | Cổng (tiêu chí hoàn thành PLAN §7) | Câu hỏi cần chốt (§9) |
|-------|---------------|----------------|-------------------------------------|------------------------|
| **Phase 0** — Tiền sản xuất (T1–2) | ≥ 1 `PAPER` (vòng đời một mạng); 1 `STYLE_REVIEW` (mẫu AR-01 §10) | Vòng đời một kiếp có hấp dẫn trên giấy; chọn tỷ lệ nhân vật | "Phong cách mỹ thuật được chốt; team đồng thuận GDD" | Q13 (ART-Q-1), ART-Q-2 (đo, không phải playtest); Q22, Q23 chốt bằng **spike**, ghi PM-04 |
| **Phase 1** — Prototype combat + 1 bản đồ (T3–5) | `INTERNAL` hằng tuần; ≥ 1 `FRIENDS` (PLAN: "1 lần playtest với người ngoài team"); `FOCUSED` cho hiểm địa, cơ duyên | Combat vui ở 150ms; khám phá `V1_F01`; "tham hay về" ở hiểm địa thử; roll thiên phú có hào hứng | "Người test chơi 20 phút và muốn chơi tiếp, tự nói về điều họ 'tìm thấy'; combat ổn định ở 150ms ping; hiểm địa tạo được cảm giác 'tham hay về'" | Q15, ENC-Q-3, DZ-Q-4, GEO-Q-3; mục tiêu GD-06 §8 (chênh 1 tầng nhỏ) |
| **Phase 2** — Khung MMO (T6–9) | `INTERNAL` hằng tuần; `FRIENDS` mỗi cuối tháng; `FTUE_LAB` cho bước 1–5 GD-18 | 3–4 giờ đầu liền mạch, chuyển bản đồ; vòng đời (trọng thương, chết, luân hồi) dễ hiểu | "Người chơi mới có thể chơi liền mạch 3–4 giờ đầu game, di chuyển giữa các bản đồ không lỗi" | OB-Q-1 (ứng viên); KPI chơi lại sau chết thử nghiệm định tính |
| **Phase 3** — Chế tác & Vực 1 (T10–13) | `INTERNAL` hằng tuần; `FTUE_LAB` mỗi lần đổi onboarding (≥ 2 vòng); `FRIENDS` 15–20 giờ chơi (nhiều buổi, cùng nhóm) | Onboarding đầy đủ (OB-AC-001); 15–20 giờ nội dung; ≥ 30% thời gian khám phá / chế tác | "Đủ nội dung cho ~15–20 giờ chơi, trong đó ≥ 30% thời gian là khám phá / chế tác" | CU-Q-2, OB-AC-001, OB-Q-2, OB-Q-3 |
| **Phase 4** — Closed Alpha (T14–15) | `CLOSED_ALPHA` (báo cáo tuần + báo cáo tổng kết); `INTERNAL` tiếp tục | **KPI một mạng** GD-20 §7.2: tỷ lệ chơi lại 24h / 7 ngày, công bằng khi chết; funnel FTUE; heatmap | Dashboard TM-R-050 (1)–(8) chạy; trần Alpha TC4 (GD-03 C2) được truyền đạt | TM-Q-2, TLN-Q-2, NW-Q-3, SK-Q-5, EN-Q-4 |
| **Phase 5a** — Closed Beta 1 (T16–18) | `CLOSED_BETA` (tuần); `FOCUSED` PvP, thiên kiếp, đấu giá | PvP dã ngoại không đuổi người chơi hòa bình; kinh tế hố / nguồn | Dashboard (9)–(10); cảnh báo "giết người vô tội tăng vọt" yên | (theo câu hỏi mở GD-32, GD-13 khi có) |
| **Phase 5b** — Closed Beta 2 (T19–21) | `CLOSED_BETA` (tuần); `FOCUSED` linh thú, ngự kiếm, Ma Vực | Nội dung mở rộng; cosmetic shop không P2W (GD-20 §7.6) | Load test 1000+ CCU (không phải playtest) | (theo GD-25, GD-31, GD-28) |
| **Phase 6** — Open Beta (T22+) | `OPEN_BETA` báo cáo tuần | Giữ chân D1 / D7 / D30; chơi lại sau chết | — | — |

---

## 4. Quy trình một buổi (gắn với sprint PLAN §9)

```
T−3 ngày  Chủ trì tạo file từ _template.md (trạng thái "Kế hoạch"): mục tiêu, kịch bản, câu hỏi -Q- nhắm tới,
          số liệu cần (sự kiện GD-20), build dự kiến. Producer xếp lịch trong sprint.
T−1 ngày  Chốt build trên staging; ghi build + gamedataVersion; kiểm tra telemetry ghi đúng `serverId` / nhãn buổi.
T0        Giới thiệu ≤ 5' (không giải thích hệ thống trừ khi kịch bản yêu cầu) → chơi → khảo sát §5 ngay sau → thảo luận ≤ 15'.
          Người ghi chép ghi phát hiện thô theo người tham gia ẩn danh (P01…), kèm mốc thời gian.
T+1..2    Chủ trì hoàn thành báo cáo: số liệu, phát hiện có mức độ (§6), hành động (§7). Trạng thái "Hoàn thành".
          Lỗi → backlog PM-02 (link trong báo cáo). Câu hỏi mở → cập nhật GD theo §7.
Cuối sprint  Demo + retro (PLAN §9) điểm qua các phát hiện S1 / S2 và hành động đang mở.
```

- <a id="pt-r-020"></a>**PT-R-020** Trạng thái file báo cáo: `Kế hoạch` → `Hoàn thành` → (`Đã xử lý` khi mọi hành động S1 / S2 đã đóng). Producer rà mỗi sprint các báo cáo chưa `Đã xử lý`.

---

## 5. Mục tiêu đo & khảo sát chuẩn

### 5.1. Chỉ số theo loại buổi

| Nhóm | Chỉ số (định nghĩa ở nguồn) | Nguồn | Từ loại buổi |
|------|-----------------------------|-------|--------------|
| Một mạng | Tỷ lệ chơi lại 24h / 7 ngày sau `life_ended`; khoảng nghỉ trước kiếp mới; **cảm nhận công bằng khi chết** (`life_death_survey`, % điểm ≥ 4); phân bố nguyên nhân kết thúc kiếp; độ dài kiếp | GD-20 §7.2 | `PAPER` (mô phỏng), `FRIENDS` (định tính), `CLOSED_ALPHA`+ (đo thật, mục tiêu ≥ 50% / ≥ 70%, ≥ 70%) |
| Giữ chân | Retention D1 / D7 / D30, độ dài phiên | GD-20 §7.1 | `CLOSED_ALPHA`+ |
| FTUE | Funnel 8 bước `ob_*` (giữ lại từ bước 1: 100% → ≥ 80% → … → ≥ 45%, D1 ≥ 40%); p50 / p90 thời gian mỗi bước; tỷ lệ bỏ qua cutscene; số lần trọng thương trong 30' (~0); số lần mở "Thiên cơ"; **p50 từ mở trang tới `LK1` ≤ 12'** (OB-AC-001) | GD-18 §8, GD-20 §4.8 | `FTUE_LAB`, `FRIENDS`, `CLOSED_ALPHA`+ |
| Mốc sau 30' | Thời điểm (cảnh giới, giờ chơi) người chơi gặp từng mốc GD-18 §6 (luyện đan `LK2`, luyện khí `LK3`, bí cảnh `LK4–5`, hiểm địa `LK5`, chế công pháp `TC1`…), có hiểu cơ chế không | GD-18 §6 | `FRIENDS` (Phase 2–3), `CLOSED_ALPHA` |
| Tiến trình | Thời gian lên cảnh giới, tu vi lãng phí ở bình cảnh, tỷ lệ đột phá, cơ duyên / giờ, phễu nhiệm vụ, độ phủ bản đồ | GD-20 §7.3 | `FOCUSED`, `CLOSED_ALPHA`+ |
| Chiến đấu | TTK (`combat_ttk`), trọng thương / giờ theo bản đồ; mục tiêu GD-06 §8 | GD-20 §7.3, GD-06 §8 | `INTERNAL`, `FOCUSED` (Phase 1+) |
| Kinh tế / RNG | Hố / nguồn, chỉ số giá; lệch tỷ lệ `z` | GD-20 §7.4–§7.5 | `CLOSED_ALPHA`+ |
| Hiệu năng | FPS p50 / p5 theo preset × `deviceClass`; thời gian tải, chuyển bản đồ | GD-20 §7.7, TD-11 | Mọi buổi có build |

### 5.2. Câu hỏi khảo sát chuẩn
Mã `PT-S-nn` **cố định** — không đổi nghĩa, không dùng lại số; câu mới thêm số tiếp theo (để so sánh giữa các buổi). Thang Likert 1–5 (1 = hoàn toàn không đồng ý, 5 = hoàn toàn đồng ý) trừ khi ghi khác. Buổi chọn tập con phù hợp; **bộ lõi** (★) hỏi ở mọi buổi có người chơi thật từ Phase 1.

| Mã | Câu hỏi | Loại | Gắn với |
|----|---------|------|---------|
| PT-S-01 ★ | Tôi muốn chơi tiếp ngay sau buổi này. | Likert | Cổng Phase 1 |
| PT-S-02 ★ | Điều thú vị nhất tôi **tìm thấy** hôm nay là gì? | Mở | Cổng Phase 1, trụ cột P5 |
| PT-S-03 ★ | Điều làm tôi khó chịu / bối rối nhất là gì? | Mở | — |
| PT-S-04 ★ | Tôi sẵn lòng giới thiệu game cho bạn chơi tiên hiệp. (0–10) | Thang 0–10 | Theo dõi xu hướng |
| PT-S-05 | Đánh nhau có cảm giác "đã tay" (phản hồi trúng, né, đỡ). | Likert | GD-06, GD-07; AST-Q-1, SK-Q-1 |
| PT-S-06 | Tôi đọc được đòn đánh của quái và có thời gian né. | Likert | GD-12, GD-24 (vùng cảnh báo) |
| PT-S-07 | Tôi thấy bản đồ có nhiều thứ đáng khám phá, không trống. | Likert | GD-21 §3.4; Q15 |
| PT-S-08 | Bản đồ có kích thước vừa phải (quá nhỏ / vừa / quá lớn). | Chọn 1 | Q15 |
| PT-S-09 | Ở hiểm địa, tôi phải cân nhắc "tham thêm hay rút về". | Likert | Cổng Phase 1; GD-28; DZ-Q-4 |
| PT-S-10 | Thời gian trong hiểm địa đủ để vừa khám phá vừa thoát (thiếu / vừa / thừa). | Chọn 1 | DZ-Q-4 |
| PT-S-11 | Cơ duyên xuất hiện (quá thưa / vừa / quá dày). | Chọn 1 | ENC-Q-3 |
| PT-S-12 | Khi kết thúc kiếp, tôi muốn bắt đầu kiếp mới. | Likert | GD-20 §7.2 (chơi lại sau chết) |
| PT-S-13 | Cái chết / trọng thương vừa rồi là công bằng. | Likert (= `life_death_survey.score`) | GD-20 §7.2; TM-Q-2; PM-03 R11, R12 |
| PT-S-14 | Tôi hiểu thọ nguyên và chỗ nào có thể chết thật. | Likert | GD-18 §4 (OB-R-021) |
| PT-S-15 | Tôi hiểu tỷ lệ hiển thị (đột phá, cơ duyên) và thấy ngẫu nhiên là công bằng. | Likert | GD-29; PM-03 R13 |
| PT-S-16 | Màn roll thiên phú làm tôi hào hứng với kiếp này. | Likert | GD-27; PLAN Phase 1 |
| PT-S-17 | Hướng dẫn 30 phút đầu rõ ràng, không quá nhiều chữ. | Likert | GD-18; OB-AC-001 |
| PT-S-18 | Đột phá Trúc Cơ là một khoảnh khắc lớn. | Likert | CU-Q-2 |
| PT-S-19 | Game chạy mượt trên máy của tôi. (+ thiết bị / trình duyệt) | Likert + mở | TD-11 |
| PT-S-20 | (Chỉ `STYLE_REVIEW`) Tỷ lệ nhân vật A / B nào dễ nhận ra hơn ở góc camera game; cái nào hợp tiên hiệp hơn? | Chọn 1 × 2 + mở | Q13 (ART-Q-1) |

### 5.3. Bộ câu hỏi theo phase (tối thiểu)
| Phase | Câu |
|-------|-----|
| 0 | `PAPER`: PT-S-01, 03, 12, 16 (hỏi trên giấy) · `STYLE_REVIEW`: PT-S-20, 03 |
| 1 | Bộ lõi + PT-S-05, 06, 07, 08, 09, 10, 11, 16 |
| 2 | Bộ lõi + PT-S-12, 13, 14, 17, 19 |
| 3 | Bộ lõi + PT-S-12, 13, 14, 15, 17, 18 |
| Alpha / Beta | Bộ lõi + PT-S-12, 13 (trong game qua `life_death_survey`), 15, 19; biểu mẫu tuần |

---

## 6. Ghi lỗi & phát hiện

### 6.1. Mức độ
| Mức | Nghĩa | Ví dụ | Xử lý |
|-----|-------|-------|-------|
| **S1 Chặn** | Không chơi tiếp được, mất dữ liệu / tiến trình, **chết thật do lỗi / lag**, khai thác kinh tế | Treo khi chuyển bản đồ; chết thật vì desync | Task trong sprint hiện tại; chết do lỗi → Death Review (PM-03 R12); khai thác → TD-08 |
| **S2 Nặng** | Làm hỏng mục tiêu buổi / trụ cột thiết kế | Combat không né được ở 150ms; không ai hiểu thọ nguyên | Task ưu tiên cao; nếu chạm câu hỏi mở → §7 |
| **S3 Vừa** | Khó chịu rõ, có cách lách | Gợi ý che vùng cảnh báo | Backlog |
| **S4 Nhẹ** | Hình thức, chữ, đề xuất nhỏ | Sai chính tả, màu icon | Backlog, gộp |

### 6.2. Phân loại & mã
- Loại: `BUG` · `FEEL` (cảm giác điều khiển / combat) · `BALANCE` · `UX` · `PERF` · `CONTENT` · `ART` · `LORE`.
- Mã phát hiện: `PT-{YYYYMMDD}-F{nn}` (vd. `PT-20270315-F03`), duy nhất trong thư mục; dùng để trích dẫn từ GD / PM-04.
- <a id="pt-r-030"></a>**PT-R-030** `BUG` luôn có task ở PM-02 (link trong báo cáo) với: build, `gamedataVersion`, bước tái hiện, kỳ vọng / thực tế, mã người tham gia (không định danh thật). Báo cáo không thay task.
- <a id="pt-r-031"></a>**PT-R-031** Phát hiện về asset (`ART`) trỏ `assetId` và, nếu cần làm lại, đặt dòng tương ứng ở [AR-05](../../art/asset-tracker.md) về `REJECTED` với `rejectReason` trích mã phát hiện.

---

## 7. Từ phát hiện tới câu hỏi mở / thay đổi tài liệu

```
Phát hiện (PT-…-Fnn)
 ├─ BUG ───────────────────────────────► task PM-02 (PT-R-030)
 ├─ Chạm câu hỏi mở đã có ({PREFIX}-Q-n) ─► (1) thêm bằng chứng vào dòng Q trong GD/TD:
 │                                            "Bằng chứng: PT-20270315 (n=8): 6/8 thấy 15' thiếu…"
 │                                       └► (2) đủ để chốt? → Producer / owner GD quyết
 │                                               → PM-04 §2 "Câu hỏi đã chốt" (hoặc D-nn mới nếu là quyết định lớn, ADR nếu kiến trúc)
 │                                               → sửa spec: đánh dấu [x] + kết luận, tăng phiên bản, lịch sử thay đổi
 │                                               → mới sửa code / gamedata (docs/README §3.7)
 ├─ Vấn đề mới, spec chưa có câu hỏi ───► đề xuất {PREFIX}-Q-{n+1} trong mục "Câu hỏi mở" của GD/TD liên quan, ghi giá trị mặc định đang dùng
 ├─ Chỉ cần chỉnh số ───────────────────► sửa gamedata (tham số ghi trong spec) qua PR thường, trích mã phát hiện; không đổi -R-
 └─ Rủi ro dự án ───────────────────────► thêm / cập nhật R-nn ở PM-03
```

- <a id="pt-r-040"></a>**PT-R-040** Báo cáo **không** sửa trực tiếp `-R-` hay đóng `-Q-`; nó chỉ liệt kê "Đề xuất cập nhật tài liệu". Việc sửa spec là PR riêng trích mã phát hiện và mã báo cáo.
- <a id="pt-r-041"></a>**PT-R-041** Câu hỏi PLAN §11.3 (Q13, Q15, Q22, Q23) khi chốt phải cập nhật **cả** PLAN §11.3 (đánh dấu `[x]` + kết luận), PM-04 §2 / §3 và tài liệu nguồn (vd. ART-Q-1, GD-21 Q15).
- <a id="pt-r-042"></a>**PT-R-042** Một câu hỏi chỉ được đóng bằng playtest khi báo cáo ghi rõ `n`, loại buổi và tiêu chí chốt đã đặt **trước** buổi (trong mục "Mục tiêu" của báo cáo).

---

## 8. Đặt tên file & thuộc tính

- Đường dẫn: `docs/pm/playtests/YYYY-MM-DD-{phase}-{chủ đề}.md`
  - `YYYY-MM-DD`: ngày diễn ra buổi (giờ UTC+7).
  - `{phase}` ∈ `ph0` · `ph1` · `ph2` · `ph3` · `alpha` · `beta1` · `beta2` · `ob` (cùng tập mã phase với AR-05 cột `phase`, viết thường).
  - `{chủ đề}`: chữ thường không dấu, gạch nối, ≤ 40 ký tự (vd. `paper-lifecycle`, `combat-v1-f01`, `ftue-buoc-1-5`, `danger-zone-hd01`).
  - Ví dụ: `2026-12-10-ph0-paper-lifecycle.md`, `2027-03-15-ph1-friends-combat-v1-f01.md`, `2027-12-20-alpha-tuan-02.md`.
  - Hai buổi cùng ngày cùng chủ đề: thêm hậu tố `-2`.
- Mã báo cáo (trích dẫn): `PT-{YYYYMMDD}` hoặc `PT-{YYYYMMDD}-{n}` nếu có hậu tố.
- File bắt đầu bằng `_` (vd. `_template.md`) không phải báo cáo.

### 8.1. Mục lục báo cáo
Thêm một dòng khi tạo báo cáo (mới nhất ở trên).

| Mã | Ngày | Phase | Loại | Chủ đề | Câu hỏi nhắm tới | Trạng thái |
|----|------|-------|------|--------|------------------|-----------|
| — | — | — | — | *(chưa có buổi nào)* | — | — |

---

## 9. Câu hỏi đang chờ playtest / mẫu chốt

Nguồn: PLAN §11.3 và grep "playtest" / "đo" trong `docs/design`, `docs/tech`, `docs/art` (2026-10-25). Cột "Cách chốt": **PT** = playtest, **MẪU** = mẫu phong cách / asset, **SPIKE** = đánh giá kỹ thuật (không phải playtest, ghi PM-04), **TM** = telemetry Alpha+. Dòng "ứng viên" chưa được tài liệu nguồn ghi là chốt bằng playtest — đề xuất bổ sung.

| Câu hỏi | Nội dung ngắn | Phase | Loại buổi / cách chốt | Đo bằng | Mặc định đang dùng |
|---------|---------------|:---:|-----------------------|---------|--------------------|
| **Q13** = [ART-Q-1](../../art/art-bible.md) | Tỷ lệ nhân vật A (1/5) hay B (1/6–1/6,5) | 0 | MẫU + `STYLE_REVIEW` | PT-S-20; silhouette ở camera game (AR-01 §10 #1) | — |
| [ART-Q-2](../../art/art-bible.md) | Shader thủy mặc bật mặc định ở preset Trung | 0 | MẫU (đo GPU mẫu #6) + `STYLE_REVIEW` | FPS TD-11; sở thích | — |
| **Q15** ([GD-21](../../design/22-open-world.md) §11) | Dã ngoại chuẩn 250m hay 400m | 1 | PT `INTERNAL` + `FRIENDS` trên `V1_F01` | PT-S-07, 08; độ phủ bản đồ (GD-20 §7.3); thời gian băng qua | Checklist GD-21 §3.4 quy đổi theo diện tích |
| **Q22** | Map editor: mở rộng Babylon.js Editor hay tự viết | 0 | SPIKE (TD-13 MF-R-031, §5.1) | Tiêu chí spike | — |
| **Q23** | Ink + inkjs trên Game Server đáp ứng cơ duyên | 0 | SPIKE (3 cơ duyên mẫu) | Tag lựa chọn, lưu / khôi phục | — |
| [ENC-Q-3](../../design/23-encounters.md) | 2–4 phiên cơ duyên / giờ quá dày / thưa | 1 | PT `FOCUSED` với 3–5 cơ duyên mẫu | PT-S-11; `enc_resolved` / giờ | 2–4 / giờ |
| [DZ-Q-4](../../design/27-danger-zones.md) | Thời lượng hiểm địa 15' / 20' đủ không | 1 | PT `FOCUSED` (hiểm địa thử) | PT-S-09, 10; tỷ lệ thoát kịp | 15' / 20' |
| [GEO-Q-3](../../tech/13-geom2d.md) | Xuyên qua quái làm combat "lỏng" | 1 | PT `INTERNAL` | PT-S-05; quan sát | Không chặn; phương án `L`/`XL` `solid` |
| [GD-06 §8](../../design/12-stats-formulas.md) | TTK quái thường 3–6 đòn; chênh 1 tầng nhỏ thắng bằng kỹ năng | 1–3 | PT `FOCUSED` + Balance Simulator | `combat_ttk`, tỷ lệ thắng | Như bảng GD-06 §8 |
| [CU-Q-2](../../design/10-cultivation.md) | Trúc Cơ 40% cơ bản có quá dễ | 3 | PT `FRIENDS` dài | PT-S-18; tỷ lệ đột phá GD-20 §7.3 | 40% |
| [OB-AC-001](../../design/41-onboarding.md) | p50 mở trang → `LK1` ≤ 12' | 1 (khung) / 3 | PT `FTUE_LAB` | `ob_tutorial_qi_done.ms` | — (là AC) |
| [OB-Q-2](../../design/41-onboarding.md) *(ứng viên)* | Hiểm địa lần đầu bản "luyện tập" | 3 | PT `FTUE_LAB` / `FRIENDS` | PT-S-09, 14; tỷ lệ chết lần đầu | Chưa có |
| [OB-Q-3](../../design/41-onboarding.md) *(ứng viên)* | Cơ duyên dàn dựng khác theo xuất thân | 3 | PT `FRIENDS` (A/B nhỏ) | PT-S-02, 16 | Một biến thể |
| [AST-Q-1](../../art/asset-guidelines.md) *(ứng viên)* | Strafe 8 hướng hay xoay thân trên | 1 | PT `INTERNAL` | PT-S-05 | — |
| [SK-Q-1](../../design/13-skills.md) *(ứng viên)* | "Giữ để ngắm" làm mặc định | 1 | PT `INTERNAL` | PT-S-05 | Bấm là thi triển |
| [TM-Q-2](../../design/51-analytics.md) | Khảo sát công bằng khi chết: ngay hay lần đăng nhập sau | Alpha | TM (A/B GD-20 §10) | Tỷ lệ phản hồi `life_death_survey` | — |
| [TLN-Q-2](../../design/04-talents.md) | Thiên linh căn 1% quá hiếm | Alpha | TM | Phân bố roll, PT-S-16 | 1% |
| [NW-Q-3](../../tech/01-network.md) | `defenseLagCompMs` 100ms đủ | Alpha | TM (`corrected`) | Telemetry | 100ms |
| [SK-Q-5](../../design/13-skills.md) | Quạt quá an toàn so với kiếm | Alpha | TM | `damage_taken_sample` theo vũ khí | — |
| [EN-Q-4](../../design/18-enemies-ai.md) | `partyHpScaling` 0,6 | Alpha | TM | `boss_attempt` theo `partySize` | 0,6 |

---

## 10. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="pt-ac-001"></a>PT-AC-001 | File `docs/pm/playtests/2027-3-15-phase1-Combat.md` | `pnpm docs:check` (TD-10 CC-R-105) | Fail (sai mẫu §8) |
| <a id="pt-ac-002"></a>PT-AC-002 | Báo cáo chứa chuỗi dạng email `a@b.c` hoặc số điện thoại | `pnpm docs:check` | Fail (PT-R-002) |
| <a id="pt-ac-003"></a>PT-AC-003 | Báo cáo trạng thái `Hoàn thành` thiếu build hoặc `gamedataVersion` | `pnpm docs:check` | Fail (PT-R-005) |
| <a id="pt-ac-004"></a>PT-AC-004 | Phát hiện mức S1 / S2 không có cột hành động trỏ mã (task / `-Q-` / `R-nn` / `D-nn`) | `pnpm docs:check` | Fail (PT-R-004) |
| <a id="pt-ac-005"></a>PT-AC-005 | Báo cáo đánh dấu một `-Q-` "đã chốt" | Review | Có dòng tương ứng ở PM-04 §2 và tài liệu nguồn đã `[x]` trong cùng hoặc PR kế tiếp (PT-R-040, PT-R-041) |
| <a id="pt-ac-006"></a>PT-AC-006 | Tỷ lệ trong báo cáo không ghi `n` | Review | Bị trả lại (PT-R-006) |
| <a id="pt-ac-007"></a>PT-AC-007 | Báo cáo mới được thêm | Review | Có dòng ở §8.1 |

---

## 11. Câu hỏi mở
- [ ] **PT-Q-1:** Mã câu khảo sát `PT-S-nn` là loại mã mới ngoài bảng docs/README §2.1 (`-R-`, `-AC-`, `-Q-`) — có bổ sung vào quy ước chung không? *Mặc định: chỉ dùng trong PM-05.*
- [ ] **PT-Q-2:** Công cụ khảo sát cho người ngoài (biểu mẫu tự host hay dịch vụ SaaS)? Dịch vụ SaaS phải đăng ký TD-15 (OSS-R-021) và rà dữ liệu cá nhân (TD-03 §13). *Cập nhật 2026-10-25: chính sách đã có — TD-15 v0.4 OSS-R-022 + dòng "Công cụ khảo sát playtest" trạng thái **chờ chọn** ở §4.4; PT-R-012 quy định cách làm tới khi chọn. Còn mở: chọn công cụ cụ thể (Producer).*
- [ ] **PT-Q-3:** Có gắn nhãn buổi playtest vào envelope telemetry (vd. một `serverId` riêng cho môi trường playtest) để lọc số liệu, hay dùng khung thời gian? GD-20 §5 chưa có trường này — không tự thêm.
- [x] **PT-Q-4:** Kiểm tra tự động PT-AC-001..004 chạy ở CI docs (script nào, lệnh nào)? Hiện chưa có lệnh trong AGENTS.md §5. *(Đã chốt 2026-10-25: TD-10 v0.3 CC-R-105 — lệnh `pnpm docs:check`, script `tools/docs/check-playtests.ts`: tên file theo §8, quét email / số điện thoại, báo cáo `Hoàn thành` phải có build + `gamedataVersion`, phát hiện S1 / S2 có hành động trỏ mã. AGENTS.md §5 cần thêm lệnh — việc của chủ AGENTS.md.)*
- [ ] **PT-Q-5:** Phase 2 có cần một đợt `FRIENDS` lớn (20–50 người) trước Closed Alpha để thử KPI chơi lại sau chết sớm (PM-03 R11) không?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến ẩn danh / số liệu, 9 loại buổi, lịch theo Phase 0–6 đối chiếu PLAN §7 / §9, chỉ số theo GD-20 §7 và GD-18 §6 / §8, bộ khảo sát chuẩn PT-S-01..20, mức độ S1–S4, luồng phát hiện → `-Q-` → PM-04 → spec, quy ước tên file, danh sách câu hỏi chờ playtest (PLAN §11.3 + GD / TD), nghiệm thu, câu hỏi mở |
| v0.2 | 2026-10-25 | — | PT-R-012 (khảo sát người ngoài chỉ qua công cụ ✅ ở TD-15, theo OSS-R-022); PT-AC-001..004 chạy bằng `pnpm docs:check` (TD-10 CC-R-105); đóng PT-Q-4; cập nhật PT-Q-2 (chính sách đã có, chờ chọn công cụ) |

