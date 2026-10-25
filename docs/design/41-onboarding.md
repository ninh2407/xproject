# GD-18 — Onboarding / Tutorial (30 phút đầu)

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-18 · Prefix ID: `OB` |
| Owner | Game Designer + UI/UX Designer + Content Designer |
| Trạng thái | 🟡 Bản nháp v0.4 |
| Cần trước | Phase 3 (onboarding đầy đủ), khung bước 1–5 từ Phase 1 |
| Quyết định nền | D-03 (một mạng), D-10 (WASD), D-11 (1 giờ = 1 năm), D-13 (bảo hộ tân thủ), D-21 (`V1_F01` không PvP), D-22 (đa nền tảng) |
| Liên quan | [GD-27 Thiên phú](04-talents.md) §5.1 · [GD-15 Nhiệm vụ](21-quests.md) §5.8–5.9 · [GD-04 Tu luyện](10-cultivation.md) CU-R-023 · [GD-07 Kỹ năng](13-skills.md) SK-R-045 · [GD-24 Camera & Điều khiển](25-camera-controls.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-14 Xã hội](20-social.md) §5.4b · [GD-21 Open World](22-open-world.md) · [GD-10 Luyện đan](16-alchemy.md) · [LR-01 World Bible](../lore/world-bible.md) §9 · [GD-20 Telemetry](51-analytics.md) · [TD-05 Client](../tech/04-client.md) |

> Tài liệu này **không thêm cơ chế gameplay mới**. Nó sắp xếp **thứ tự và cách giới thiệu** các hệ thống đã có trong 30 phút đầu và các mốc sau đó. Nội dung cụ thể (bước nhiệm vụ, phần thưởng) nằm ở GD-15; GD-18 quy định trải nghiệm và tiêu chí đo.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-18 | Mục |
|--------------|-----------------|-----|
| GD-15 | Nhịp và lời dẫn của `QC_TUTORIAL_QI`, `QC_MAIN_V1_JOIN_SECT` | §3 |
| GD-22 | Cơ duyên dàn dựng đầu tiên `ENC_V1_FIRST_OMEN` | §3 bước 8, OB-R-030 |
| GD-17 / TD-05 | Hệ gợi ý ngữ cảnh (tooltip lần đầu), mốc mở UI | §5 |
| GD-20 | Funnel FTUE, sự kiện đo | §8 |

---

## 1. Mục tiêu & bất biến

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện* (mở đầu có câu chuyện), P3 *Cái chết có ý nghĩa nhưng công bằng*.

Sau 30 phút, người chơi phải: (1) điều khiển và đánh được; (2) hiểu **vòng lặp cốt lõi** "khám phá → thu thập / cơ duyên → luyện đan → đột phá"; (3) biết mình có **thọ nguyên** và một mạng, nhưng **không sợ** (biết rằng ở vùng thường chỉ trọng thương, và kiếp đầu có bảo hộ); (4) có **mục tiêu tiếp theo rõ ràng** (gia nhập tông → Trúc Cơ Đan).

| Mã | Bất biến |
|----|----------|
| <a id="ob-r-001"></a>**OB-R-001** | **Học bằng làm:** mỗi hệ thống được giới thiệu bằng một hành động người chơi tự làm; không có màn hình giải thích dài quá **2 câu** trước khi được chơi. |
| <a id="ob-r-002"></a>**OB-R-002** | **Không tường chữ:** cutscene ≤ 20 giây mỗi đoạn, luôn bỏ qua được; tooltip ≤ 140 ký tự. |
| <a id="ob-r-003"></a>**OB-R-003** | Trong 30 phút đầu, nhân vật **không thể chết thật** và không mất gì không lấy lại được (khớp D-13 bảo hộ tân thủ, LC-R-001, `V1_F01` không PvP). |
| <a id="ob-r-004"></a>**OB-R-004** | **Kiếp 2+ bỏ qua được** phần hướng dẫn (GD-15 QS-R-090, lược truyện §5.9) mà không mất mở khóa bắt buộc. |
| <a id="ob-r-005"></a>**OB-R-005** | Mọi gợi ý ngữ cảnh **tắt được** (một nút tắt toàn bộ trong cài đặt) và không che vùng cảnh báo chiêu (GD-24 CAM-R-006). |

---

## 2. Tổng quan dòng chảy

```
Phút 0   Mở trang → đăng ký / đăng nhập nhanh
Phút 1   Tạo nhân vật: tên → roll linh căn + thiên phú (nút "Thiên cơ") → xuất thân
Phút 4   Cảnh mở đầu: miếu hoang Thanh Trúc Lâm, mưa, mảnh ngọc giản cổ (LR-01 §9) — mọi xuất thân (GD-27 TLN-R-032)
Phút 5   QC_TUTORIAL_QI: Lão Đạo → di chuyển / né / đánh (3 Trúc Thử) → đả tọa → chọn vũ khí → dẫn khí nhập thể (LK1)
Phút 12  Tự do ở V1_F01 (không PvP): nhặt đồ, trang bị, hái thuốc, quét thần thức
Phút 17  Cơ duyên dàn dựng đầu tiên "Điềm Báo" (chắc chắn xảy ra) → hiểu cơ duyên + lựa chọn
Phút 20  Giới thiệu thọ nguyên & một mạng (nhẹ nhàng)
Phút 22  Đến Thanh Vân Trấn: truyền tống trận, Tàng Bảo Các, Đan Phòng, lò rèn
Phút 27  Thử thách nhập môn → chọn tông (Alpha: Thanh Vân; Beta: thêm Tạc Thiên / Hợp Hoan)
Phút 30  Mục tiêu dài hạn hiện trên HUD: "Tích cống hiến → đan phương Trúc Cơ Đan → Trúc Cơ"
```

---

## 3. Từng bước (30 phút đầu)

| # | Phút | Bước | Hệ thống | Người chơi làm | Cách giới thiệu | Nguồn |
|:-:|:----:|------|----------|----------------|-----------------|-------|
| 1 | 0–1 | Đăng nhập | Tài khoản | Email / mạng xã hội; chơi ngay rồi gắn tài khoản sau (OB-Q-1) | 1 màn hình, 2 trường | TD-04 |
| 2 | 1–4 | Tạo nhân vật | Linh căn, thiên phú, xuất thân | Roll, roll lại (giới hạn), xem bảng **Thiên cơ** (tỷ lệ công khai), chọn xuất thân | Mỗi thiên phú có tooltip 1 câu; nút "Thiên cơ" nhấp nháy lần đầu; xuất thân mặc định gợi ý `BG_VILLAGE_YOUTH` cho người mới | GD-27 §5.1, TLN-R-064 |
| 3 | 4–5 | Cảnh mở đầu | Lore, mảnh ngọc giản | Xem (bỏ qua được) | Cutscene ≤ 20s: tránh mưa trong miếu, chạm ngọc giản, ánh sáng "dấu ấn Luân Hồi"; **mọi xuất thân** xuất hiện ở miếu hoang `V1_F01` | LR-01 §3.4–3.5, §9; GD-27 TLN-R-032 |
| 4 | 5–7 | Lão Đạo | Hội thoại, nhiệm vụ | Nói chuyện, nhận `QC_TUTORIAL_QI` | Hội thoại ≤ 4 câu; HUD nhiệm vụ hiện mục tiêu đầu | GD-15 §5.8 bước 1 |
| 5 | 7–9 | Di chuyển, né, đánh | Điều khiển, combat | WASD / joystick, chuột nhắm / chạm, Space né, đánh 3 Trúc Thử | Gợi ý phím hiện **tại chỗ** (icon phím trên đất), đổi theo thiết bị (PC / tay cầm / cảm ứng); Trúc Thử có vùng cảnh báo rõ để học né | GD-24 §5–§8, GD-06 né |
| 6 | 9–10 | Đả tọa | Tu vi | Phím đả tọa 60s (đủ 100 tu vi) | Thanh tu vi + hạt linh khí bay vào người; 1 câu: "Linh khí trời đất là gốc tu hành" | GD-04, GD-15 bước 3 |
| 7 | 10–12 | Chọn vũ khí → dẫn khí nhập thể | Vũ khí, tâm pháp, kỹ năng | `CHOICE` kiếm / quạt (xem demo 3s mỗi loại) → lên `LK1`, nhận tâm pháp + kỹ năng | Hiệu ứng đột phá nhỏ (khoảnh khắc "tu tiên" đầu tiên); ô kỹ năng Q sáng lên | GD-07 SK-R-045, GD-04 CU-R-023 |
| 8 | 12–17 | Tự do ở `V1_F01` | Rơi đồ, trang bị, thu thập, thần thức | Nhặt đồ, mặc trang bị, hái linh thảo, quét thần thức thấy rương ẩn; trên đường chính chạm truyền tống trận `V1_F01_TP_001` (kích hoạt lần đầu — GD-16 `V1_F01` LV1F01-R-002) | Tooltip lần đầu (§5): "Trang bị mới tốt hơn — bấm để mặc"; vòng sáng quét thần thức | GD-09, GD-21 §3.7 |
| 9 | 17–20 | Cơ duyên đầu tiên | Cơ duyên, lựa chọn, `ROLL_CHECK` | Gặp cơ duyên dàn dựng `ENC_V1_FIRST_OMEN` (OB-R-030) khi vào Gò Linh Mạch `V1_F01_A04` lần đầu, chọn 1 trong 2–3 lựa chọn, một lựa chọn có tỷ lệ | Hiện tỷ lệ "~75%" cạnh lựa chọn (GD-29); kết thúc với 1 câu "Cơ duyên mỗi kiếp mỗi khác" | GD-22, GD-29 |
| 10 | 20–22 | Thọ nguyên & một mạng | Vòng đời | Mở bảng nhân vật thấy **tuổi / thọ nguyên** (tự động hiện lần đầu) | Xem §4 | GD-26 |
| 11 | 22–27 | Thanh Vân Trấn | Truyền tống trận, Tàng Bảo Các, Đan Phòng, lò rèn, cửa hàng | Được Lâm Thanh Phong đón ở khu tông môn (LR-02 CHR-NPC-002); kích hoạt truyền tống trận (miễn phí kiếp đầu < LK4, GD-13 EC-R-023), ghé từng NPC có dấu "!" | Mỗi NPC giới thiệu 1 câu + 1 hành động thử (gửi 1 vật vào Tàng Bảo Các; xem đan phương). Xuất thân gắn tông (`BG_SECT_SERVANT` / `BG_TAC_THIEN_GANG` / `BG_HOP_HOAN_DISCIPLE`): đây là điểm đến sau hướng dẫn, gặp NPC đón của chuỗi xuất thân (GD-27 TLN-R-032, GD-15 §5.8) | GD-21 §3.5, GD-26 §5.11, GD-10 |
| 12 | 27–30 | Nhập môn → chọn tông | Tông NPC, phe | Thử thách nhập môn: **Trận Thử Tâm** (đường chính, **Lâm Thanh Phong chủ trì** ở trấn — GD-15 QS-R-056); hạ Trúc Vương Thử là tùy chọn (tổ đội / quay lại sau, vẫn được tính); chọn tông | Alpha: chỉ Thanh Vân; Beta: màn chọn 3 tông, mỗi tông 1 câu phong cách + phúc lợi chính (không phải bảng số) | GD-15 `QC_MAIN_V1_JOIN_SECT`, GD-14 §5.4b |
| 13 | 30 | Mục tiêu dài hạn | Cống hiến, Trúc Cơ Đan | — | HUD "Con đường" (Tiên Lộ) hiện 3 bước kế tiếp: làm nhiệm vụ tông → đổi đan phương Trúc Cơ Đan → chuẩn bị đột phá | GD-14 SO-R-044, GD-10 AL-R-071 |

---

## 4. Truyền đạt thọ nguyên & một mạng (không làm sợ)

- <a id="ob-r-020"></a>**OB-R-020** Giới thiệu **sau** khi người chơi đã có trải nghiệm vui (lên LK1, cơ duyên đầu), không ở màn tạo nhân vật.
- <a id="ob-r-021"></a>**OB-R-021** Thông điệp chính chỉ **3 ý**, theo thứ tự:
  1. "Tu sĩ cũng có thọ nguyên — thời gian của ngươi quý giá." (hiện tuổi / thọ còn lại, đồng hồ chỉ chạy khi online, LC-R-002)
  2. "Ở vùng thường, ngã xuống chỉ là trọng thương; chỉ ở **hiểm địa** và **thiên kiếp** mới có thể thân tử đạo tiêu — luôn được báo trước." (LC-R-001)
  3. "Kết thúc một kiếp không phải mất trắng: luân hồi giữ lại nhiều thứ." (Tàng Bảo Các, điểm Luân Hồi; chi tiết Di Tàng giới thiệu sau, §6)
- <a id="ob-r-022"></a>**OB-R-022** Không hiện con số tỷ lệ chết, không cutscene chết trong 30 phút đầu. Biểu tượng **bảo hộ tân thủ** (lá chắn nhỏ cạnh thanh máu) giải thích bằng tooltip "Kiếp đầu, trước Trúc Cơ, ngươi được che chở khỏi cái chết thật" (D-13).
- <a id="ob-r-023"></a>**OB-R-023** Lần đầu bị trọng thương (nếu có trong 30 phút): màn hồi sinh hiện rõ "mất X năm thọ nguyên — có thể kiếm lại bằng…" kèm 1 gợi ý (vd. Diên Thọ Đan ở Beta), không trách phạt.

---

## 5. Gợi ý ngữ cảnh (tooltip lần đầu)

```ts
interface HintDef {                       // shared/gamedata/onboarding/hints.json
  id: string;                             // HINT_*
  trigger: { event: string; condition?: string };   // vd. event "item.picked_up_better_gear"
  textKey: string;                        // i18n, ≤ 140 ký tự (OB-R-002)
  anchor: 'HUD' | 'WORLD' | 'PANEL';      // chỗ hiện
  once: 'ACCOUNT' | 'CHARACTER';          // hiện một lần mỗi tài khoản / mỗi kiếp
  minGeneration?: number;                 // chỉ hiện từ kiếp n (vd. gợi ý Di Tàng)
  priority: number;                       // nhiều gợi ý cùng lúc → hiện cái cao nhất, xếp hàng phần còn lại
}
```
- <a id="ob-r-010"></a>**OB-R-010** Tối đa **1 gợi ý** trên màn hình cùng lúc; tự ẩn sau 8s hoặc khi làm hành động; không hiện khi đang chiến đấu (trừ gợi ý né lần đầu).
- <a id="ob-r-011"></a>**OB-R-011** Trạng thái đã xem lưu trong `account.settings.hintsSeen` (TD-03) — không làm phiền lại kiếp sau với gợi ý `once: ACCOUNT`.
- <a id="ob-r-012"></a>**OB-R-012** Gợi ý chỉ do client hiển thị (không ảnh hưởng gameplay); nội dung i18n, chọn theo thiết bị (phím PC / nút tay cầm / cử chỉ cảm ứng — GD-24).

---

## 6. Mốc giới thiệu hệ thống theo cảnh giới (sau 30 phút)

| Hệ thống | Khi nào mở | Cách giới thiệu | Nguồn |
|----------|-----------|-----------------|-------|
| Luyện đan | `LK2` hoặc khi có đan phương đầu | Nhiệm vụ phụ Đan Phòng: luyện 1 mẻ Hồi Khí Đan có hướng dẫn minigame | GD-10 |
| Luyện khí | `LK3` | Nhiệm vụ lò rèn: rèn 1 vũ khí | GD-11 |
| Bí cảnh | `LK4`–`LK5` | Nhiệm vụ chính dẫn tới BC01, gợi ý tổ đội | GD-12, GD-14 |
| **Hiểm địa** | `LK5` (HD01) | Nhiệm vụ cảnh báo riêng: màn "Sinh tử tự chịu", giải thích túi hiểm địa + lối thoát; **lần đầu vào có bản "luyện tập" an toàn?** (OB-Q-2) | GD-28 |
| Bình cảnh, đan độc, đột phá | Trước `LK9` / TC | Nhiệm vụ chuẩn bị đột phá; bảng tỷ lệ `ChanceView` có chú thích từng dòng | GD-04, GD-29 |
| Chế công pháp | `TC1` | Ngộ Đạo Thạch + Diễn Pháp Tràng thử miễn phí | GD-08 |
| Động phủ | `realmMin` cửa động | Nhiệm vụ "Tìm chốn tu hành" | GD-30 |
| Ngự kiếm | `TC1` + `QC_SWORD_FLIGHT` (Beta) | Nhiệm vụ riêng | GD-31 |
| Linh thú | Beta, gặp Linh chủng đầu tiên | Gợi ý khi thấy Linh chủng + nhiệm vụ Ngự Thú Sư | GD-25 |
| PvP dã ngoại | Rời `V1_F01` lần đầu | Dải chữ "Giang hồ hiểm ác" + tooltip chế độ PK (mặc định Hòa bình) | GD-32 |

---

## 7. Onboarding kiếp 2+ và mobile

- <a id="ob-r-040"></a>**OB-R-040** **Kiếp 2+:** sau tạo nhân vật hiện **"Ký ức tiền kiếp"** 1 màn: Bia Vạn Cổ của kiếp trước (1 dòng), những gì còn lại (Tàng Bảo Các, Di Tàng, động phủ, linh thú, ký ức nghề, fog). Hướng dẫn bỏ qua được (OB-R-004); HUD "Con đường" có thêm mục **"Giải phong ấn Di Tàng"** (Luân Hồi Đài, tầng LK khi đạt `LK4` — GD-26 §5.15), **"Khảo hạch nghề"** (GD-10 §5.9), **"Thủ vệ động phủ"** (GD-30 HM-R-045).
- <a id="ob-r-041"></a>**OB-R-041** Gợi ý chỉ dành cho kiếp 2+ dùng `minGeneration: 2` (§5): Di Tàng, tàn hồn, ký ức nghề, xuất thân mới mở bằng Luân Hồi.
- <a id="ob-r-042"></a>**OB-R-042** **Mobile:** bước 5 dùng joystick nổi + nút né / đánh có hướng dẫn cử chỉ ("kéo nút kỹ năng để ngắm"); bật **hỗ trợ ngắm mặc định** (GD-24); cutscene tối ưu dọc / ngang theo màn hình; tooltip neo gần ngón tay cái.

---

## 8. Đo lường (FTUE)

Funnel chuẩn (tên sự kiện theo [GD-20 §4](51-analytics.md), nhóm `ob_*`):

| Bước funnel | Sự kiện | Mục tiêu giữ lại (từ bước 1) |
|-------------|---------|:---:|
| 1 Mở trang / client tải xong | `ob_client_ready` | 100% |
| 2 Tạo nhân vật xong | `ob_character_created` | ≥ 80% |
| 3 Hết cutscene mở đầu | `ob_intro_done` (`skipped`) | ≥ 75% |
| 4 Đạt `LK1` | `ob_tutorial_qi_done` (`ms`) | ≥ 65% |
| 5 Xong cơ duyên đầu | `ob_first_encounter_done` | ≥ 55% |
| 6 Đến Thanh Vân Trấn | `ob_hub_reached` | ≥ 50% |
| 7 Gia nhập tông | `ob_sect_joined` | ≥ 45% |
| 8 Quay lại hôm sau | (KPI D1, GD-20) | ≥ 40% |

- Đo thêm: thời gian mỗi bước (p50 / p90), điểm rời bỏ (bước cuối cùng trước khi thoát > 24h), tỷ lệ bỏ qua cutscene / hướng dẫn, số lần chết / trọng thương trong 30 phút (mục tiêu ~0), số lần mở bảng "Thiên cơ".

---

## 9. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/onboarding/hints.json` | `HintDef[]` (`HINT_*`) |
| `shared/gamedata/onboarding/params.json` | Thời lượng tooltip, hàng đợi, bật hỗ trợ ngắm mặc định trên mobile, bước HUD "Con đường" |

**Validator:** `textKey` tồn tại và ≤ 140 ký tự (bản `vi`); `trigger.event` thuộc danh mục sự kiện client đã đăng ký; không có hai gợi ý cùng `trigger` + `priority`.

---

## 10. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="ob-ac-001"></a>OB-AC-001 | Người chơi mới | Từ mở trang tới `LK1` | p50 ≤ 12 phút trong playtest (§3) |
| OB-AC-002 | Mọi cutscene trong 30 phút đầu | Bấm bỏ qua | Bỏ qua được, tiến trình không mất |
| OB-AC-003 | Kiếp 1, trong 30 phút đầu | Bị hạ ở `V1_F01` | Trọng thương, không chết thật, màn hồi sinh có gợi ý (OB-R-023); nếu chưa kích hoạt truyền tống trận nào thì hồi sinh tại miếu hoang `V1_F01_LM_001`, không bị đưa về `V1_HUB` (GD-26 LC-R-061) |
| OB-AC-004 | Người chơi ở bước 9 | Kích hoạt cơ duyên | Luôn gặp `ENC_V1_FIRST_OMEN` (OB-R-030) đúng 1 lần mỗi tài khoản |
| OB-AC-005 | Hai gợi ý kích hoạt cùng lúc | Hiển thị | Chỉ gợi ý `priority` cao hiện; cái kia xếp hàng |
| OB-AC-006 | Đang chiến đấu | Gợi ý trang bị kích hoạt | Hoãn tới khi hết chiến đấu |
| OB-AC-007 | Tắt "gợi ý" trong cài đặt | Chơi tiếp | Không gợi ý nào hiện |
| OB-AC-008 | Kiếp 2 | `QC_TUTORIAL_QI` | Có nút bỏ qua (QS-R-090); màn "Ký ức tiền kiếp" hiện; HUD có mục Di Tàng |
| OB-AC-009 | Thiết bị cảm ứng | Bước 5 | Hướng dẫn cử chỉ (không hiện phím bàn phím); hỗ trợ ngắm bật |
| OB-AC-010 | Gợi ý `once: ACCOUNT` đã xem ở kiếp 1 | Kiếp 2 | Không hiện lại |
| OB-AC-011 | Tooltip `vi` dài 160 ký tự | Validator | Fail |

---

## 11. Phạm vi

| Hạng mục | Phase 1 | Alpha | Beta |
|----------|:---:|:---:|:---:|
| Bước 1–7 (tạo nhân vật → LK1) | ✅ (thô) | ✅ | |
| Bước 8–13, gợi ý ngữ cảnh, HUD "Con đường" | | ✅ | |
| Cơ duyên dàn dựng đầu tiên | | ✅ | |
| Onboarding kiếp 2+ (Di Tàng, khảo hạch) | | ✅ | |
| Màn chọn 3 tông, mốc linh thú / ngự kiếm / PvP | | | ✅ |
| Onboarding mobile đầy đủ | | | sau launch (theo D-22) |

---

## 12. Quy tắc bổ sung
- <a id="ob-r-030"></a>**OB-R-030** **Cơ duyên dàn dựng đầu tiên** `ENC_V1_FIRST_OMEN` ("Điềm Báo"): phẩm `PHAM`, `uniqueness: ONCE_PER_ACCOUNT`, kích hoạt chắc chắn (bỏ qua roll xảy ra của GD-22 ENC-R-030) khi người chơi đã đạt `LK1` và ở `V1_F01` lần đầu (cụ thể: vào khu Gò Linh Mạch `V1_F01_A04` sau khi xong `QC_TUTORIAL_QI` — trigger `AREA_ENTER`, GD-22 §7.3; mọi lộ trình ra Cửa rừng đều qua khu này — GD-16 `V1_F01` LV1F01-R-003); có 1 lựa chọn `ROLL_CHECK` để dạy hiển thị tỷ lệ; không tính vào pity / giới hạn tần suất. (Cần GD-22 thêm cờ `scripted: true` cho cơ duyên dàn dựng — xem đồng bộ.)
- <a id="ob-r-031"></a>**OB-R-031** HUD **"Con đường"** (Tiên Lộ): luôn hiện tối đa 3 mục tiêu kế tiếp có thể làm ngay, lấy từ nhiệm vụ đang mở + mốc §6; thu gọn được.

---

## 13. Câu hỏi mở
- [ ] **OB-Q-1:** Cho chơi ngay không cần đăng ký (tài khoản khách, gắn email sau)? Lợi cho FTUE, rủi ro mất tiến trình / lạm dụng.
- [ ] **OB-Q-2:** Lần đầu vào hiểm địa có nên là bản "luyện tập" không chết thật (instance riêng) để dạy cơ chế thoát?
- [ ] **OB-Q-3:** Cơ duyên dàn dựng đầu tiên có nên khác nhau theo xuất thân (3–4 biến thể) để tăng cảm giác "mỗi kiếp một chuyện" ngay từ đầu?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến (học bằng làm, không tường chữ, không chết thật trong 30 phút đầu, bỏ qua được kiếp 2+), dòng chảy 30 phút / 13 bước, cách truyền đạt thọ nguyên & một mạng, hệ gợi ý ngữ cảnh `HintDef`, mốc giới thiệu hệ thống theo cảnh giới, onboarding kiếp 2+ và mobile, funnel FTUE, cơ duyên dàn dựng đầu tiên, HUD "Con đường", nghiệm thu |
| v0.2 | 2026-10-25 | — | Đợt rà nhất quán: ghi rõ mọi xuất thân xuất hiện và qua hướng dẫn ở `V1_F01`; xuất thân gắn tông gặp NPC đón ở Thanh Vân Trấn sau hướng dẫn (khớp GD-27 TLN-R-032) |
| v0.3 | 2026-10-25 | — | Đồng bộ GD-16 `V1_F01`: bước 8 kích hoạt truyền tống trận `V1_F01_TP_001` trên đường chính; bước 9 / OB-R-030 trigger `AREA_ENTER V1_F01_A04`; bước 11 Lâm Thanh Phong đón ở `V1_HUB` (một vị trí cố định, CHR-R-045); bước 12 Trận Thử Tâm là đường chính, Trúc Vương Thử tùy chọn; OB-AC-003 hồi sinh ở miếu hoang khi chưa kích hoạt truyền tống trận (GD-26 LC-R-061) |
| v0.4 | 2026-10-25 | — | Trận Thử Tâm do Lâm Thanh Phong chủ trì |

