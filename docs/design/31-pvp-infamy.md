# GD-32 — PvP dã ngoại & Sát nghiệp

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-32 · Prefix ID: `PK` |
| Owner | Game Designer + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.6 |
| Cần trước | Phase 5 / Beta (bật PvP sau khi vòng lặp PvE ổn định; dữ liệu + cờ server có thể làm từ Alpha, mặc định tắt) |
| Quyết định nền | **D-21** (2026-10-23, thay thế nguyên tắc "không open-world PvP" ở PLAN §2.4), D-03 / LC-R-001 (chỉ chết thật ở hiểm địa / thiên kiếp / hết thọ), D-13 (bảo hộ tân thủ), D-17 |
| Liên quan | [PLAN](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-26 Vòng đời](03-lifecycle.md) (trọng thương §5.6) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-07 Kỹ năng](13-skills.md) · [GD-21 Open World](22-open-world.md) · [GD-14 Xã hội](20-social.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [GD-25 Linh thú](26-spirit-beasts.md) · [GD-13 Kinh tế](19-economy.md) · [GD-12 Quái](18-enemies-ai.md) · [TD-02 Network](../tech/01-network.md) |

---

## 0. Đánh giá

| Tiêu chí | Đánh giá |
|----------|----------|
| **Giá trị** | ✅ Tranh đoạt tài nguyên thật (linh thảo hiếm OW-R-100, Linh chủng, thủ hộ), cảm giác giang hồ hiểm ác, câu chuyện ân oán — hợp tiên hiệp và hợp phe Tạc Thiên Bang / Hợp Hoan Tông |
| **Rủi ro lớn nhất** | 🔴 Game **một mạng + thọ nguyên**: người chơi PvE rất ghét bị giết đi giết lại (mất thọ, mất đồ). Nếu không có lớp bảo vệ, PvP tự do sẽ đuổi phần lớn người chơi |
| **Cách giảm rủi ro** | (1) Chết do PvP **không bao giờ là chết thật** (D-21). (2) **Người bị hại** (đang Hòa bình, không phải ác nhân) **không mất thọ, không mất tu vi**, chỉ rơi tối đa 1 chồng nguyên liệu vừa thu thập. (3) *(v0.2: bỏ miễn PvP theo cảnh giới — dễ bị lợi dụng bằng nhân vật phụ cấp thấp để tranh đồ)*; thay bằng **sát nghiệp nhân mạnh** khi giết người yếu hơn nhiều. (4) Vùng an toàn: thành trấn, vùng đệm, động phủ, trú địa, bí cảnh, **hiểm địa**. (5) Sát nghiệp → tên đỏ → NPC Chấp Pháp truy sát + bị chặn tiện ích + rơi nhiều đồ khi chết. (6) Huyết thù cho phép trả thù không mang nghiệp |
| **Chi phí** | 🟡 Vừa: chế độ PK + cờ thù địch, sát nghiệp, NPC Chấp Pháp (dùng lại AI quái), hệ số PvP, rơi đồ PvP, bù trễ PvP. ~1,5–2 người-tháng |
| **Kết luận** | Làm ở **Beta**, có **cờ server** `pvpEnabled` (dùng cho môi trường test; **không** mở server hòa bình riêng — PK-Q-1) |

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="pk-r-001"></a>**PK-R-001** | Chết do người chơi khác ở dã ngoại là **trọng thương**, không bao giờ chết thật (GD-26 LC-R-001 giữ nguyên). |
| <a id="pk-r-002"></a>**PK-R-002** | **Hiểm địa, bí cảnh, thành trấn, vùng đệm, động phủ, trú địa** không có PvP. (Ma Vực — hiểm địa PvP — vẫn là quyết định riêng PLAN Q9.) |
| <a id="pk-r-003"></a>**PK-R-003** | Người chơi đang **Hòa bình** và không phải ác nhân không bao giờ mất thọ nguyên / tu vi vì bị người khác giết. |
| <a id="pk-r-004"></a>**PK-R-004** | Mọi phán định PvP (trúng, chết, sát nghiệp, rơi đồ) do server; rơi đồ PvP **tất định** (không roll), ghi `item_log`. |

---

## 1. Thực thể & kiểu dữ liệu

```ts
type PkMode = 'PEACE' | 'GUILD' | 'SLAUGHTER';
//  PEACE: không tấn công người chơi (vẫn có thể bị ác nhân tấn công)
//  GUILD: tấn công người ngoài tổ đội và ngoài tông môn tự lập của mình
//  SLAUGHTER: tấn công mọi người ngoài tổ đội
type InfamyTier = 'CLEAN' | 'GREY' | 'RED' | 'CRIMSON';
//  CLEAN 0 · GREY (tạm thời, đã tấn công người Hòa bình 60s) · RED ≥ 100 (Ác nhân) · CRIMSON ≥ 300 (Đại ác nhân, bị truy nã)

interface PkState {                       // thuộc kiếp
  characterId: string;
  mode: PkMode; modeChangedAt: string;
  infamy: number;                         // Sát nghiệp ≥ 0
  greyUntil: string | null;               // cờ "xám": tấn công người vô tội gần đây
  lastPvpCombatAt: string | null;
  feuds: { characterId: string; until: string }[];   // Huyết thù: được giết những người này không mang nghiệp
}
interface PvpKillRecord {                 // nhật ký
  id: string; killerId: string; victimId: string; mapId: string;
  victimMode: PkMode; victimTier: InfamyTier; infamyDelta: number; droppedItemIds: string[]; at: string;
}
```

---

## 2. Quy tắc

### 2.1. Nơi có PvP và miễn trừ
- <a id="pk-r-010"></a>**PK-R-010** PvP bật ở mọi bản đồ `FIELD` (dã ngoại) khi server có `pvpEnabled = true`, **trừ** vùng an toàn: bán kính `safeZone.radius` quanh truyền tống trận và lối nối bản đồ (GD-21 vùng đệm), quanh tổng đàn tông NPC, và các `MapType` ở PK-R-002. **Bản đồ hướng dẫn `V1_F01` (Thanh Trúc Lâm) không có PvP trên toàn bản đồ** (cờ bản đồ `pvpDisabled = true`, áp dụng cho mọi người — không phải miễn theo cảnh giới nên không bị lợi dụng; F01 chỉ có tài nguyên cơ bản).
- <a id="pk-r-011"></a>**PK-R-011** **Miễn PvP** chỉ trong các trường hợp kỹ thuật: 10s sau khi hồi sinh / vào bản đồ (`respawnImmunitySec`); đang `DORMANT_GUARD` / mất kết nối. **Không** miễn theo cảnh giới hay kiếp đầu (v0.2) — nhân vật cấp thấp ở dã ngoại cũng tham gia tranh đoạt, đổi lại người vô tội không mất thọ / tu vi (PK-R-003) và kẻ giết người yếu hơn mang sát nghiệp nặng (PK-R-030).
- <a id="pk-r-012"></a>**PK-R-012** *(Bỏ ở v0.2.)* Không còn giới hạn chênh cảnh giới khi tấn công. Thay bằng hệ số sát nghiệp theo chênh lệch (PK-R-030).

### 2.2. Chế độ PK và cờ thù địch
- <a id="pk-r-020"></a>**PK-R-020** Mặc định `PEACE`. Đổi sang `GUILD` / `SLAUGHTER`: tức thì. Đổi **về** `PEACE`: phải ngoài chiến đấu PvP ≥ `peaceSwitchSec` (300s). Hồi chiêu đổi chế độ 60s.
- <a id="pk-r-021"></a>**PK-R-021** Ai được đánh ai (A tấn công B):

| B \ A | A `PEACE` | A `GUILD` | A `SLAUGHTER` |
|-------|:---:|:---:|:---:|
| B cùng tổ đội | ❌ | ❌ | ❌ |
| B cùng tông tự lập | ❌ | ❌ | ✅ |
| B `PEACE`, `CLEAN` | ❌ | ✅ (A thành `GREY`) | ✅ (A thành `GREY`) |
| B `GUILD` / `SLAUGHTER` hoặc `GREY` | ❌ (trừ tự vệ PK-R-022) | ✅ | ✅ |
| B `RED` / `CRIMSON` | ✅ (mọi chế độ, không mang nghiệp) | ✅ | ✅ |

- <a id="pk-r-022"></a>**PK-R-022** **Tự vệ:** B bị A tấn công trước thì trong 60s B (và tổ đội B) đánh trả A được ở mọi chế độ, **không** mang nghiệp.
- <a id="pk-r-023"></a>**PK-R-023** **Cờ xám** (`GREY`, 60s, làm mới mỗi lần tái phạm): A tấn công một người `PEACE` + `CLEAN`. Trong lúc xám, ai cũng đánh A được không mang nghiệp. Tên A hiện màu xám.

### 2.3. Sát nghiệp
- <a id="pk-r-030"></a>**PK-R-030** Giết một người chơi (đòn kết liễu; tính cho người gây sát thương cuối) cộng sát nghiệp:

| Nạn nhân | Sát nghiệp cộng |
|----------|:---:|
| `PEACE` + `CLEAN` (người vô tội) | **+50** |
| `GUILD` / `SLAUGHTER`, `CLEAN` | +10 |
| `GREY` | +5 |
| `RED` / `CRIMSON`, hoặc trong Huyết thù, hoặc tự vệ | 0 |

  Hệ số (nhân dồn): nạn nhân thấp hơn ≥ 2 tiểu cảnh giới ×1,5; thấp hơn **≥ 1 cảnh giới lớn** ×3 (thay cho ×1,5); giết lại **cùng nạn nhân** trong 30 phút ×2 (và nạn nhân không rơi đồ, PK-R-043). Vd. `KD1` giết `TC2` vô tội: 50 × 3 = **150** → thành ác nhân ngay.
- <a id="pk-r-031"></a>**PK-R-031** **Giảm sát nghiệp:** khi ác nhân **chết** — bị người chơi giết −`infamyDeathByPlayer` (10), bị NPC Chấp Pháp giết −`infamyDeathByEnforcer` (30); theo thời gian online không giết người −1 mỗi 6 phút (−10 / giờ); làm nhiệm vụ "Sám hối" ở Chấp Pháp Đường (Beta; NPC Phân đường chủ Tiêu Chính Ngôn `NPC_V1_ENFORCER_CHIEF` tại phân đường ở `V1_HUB` — LR-02 CHR-NPC-035, GD-21) −20 mỗi lần, tối đa 3 / ngày.
- <a id="pk-r-032"></a>**PK-R-032** **Bậc:** `RED` (Ác nhân, ≥ 100): tên đỏ. `CRIMSON` (Đại ác nhân, ≥ 300): tên đỏ thẫm, bị **truy nã** (PK-R-052). Sát nghiệp thuộc kiếp; Bia Vạn Cổ ghi sát nghiệp cao nhất ("Sát thần" nếu ≥ 1.000).
- <a id="pk-r-033"></a>**PK-R-033** **Hậu quả của ác nhân** (`RED` trở lên):
  - không vào được thành trấn: lính gác tấn công ở cổng; NPC trong vùng an toàn không phục vụ;
  - không dùng truyền tống trận;
  - cửa hàng NPC thường giá +50%;
  - Thanh Vân Tông: không nhận nhiệm vụ hằng ngày; ở `CRIMSON` bị trục xuất (như phản môn SO-R-047);
  - **Tạc Thiên Trại** vẫn tiếp ác nhân (vùng an toàn của trại, Hắc Thị phục vụ) — đất dung thân của phe tà.

### 2.4. Chết do PvP và rơi đồ
- <a id="pk-r-040"></a>**PK-R-040** Hết máu do người chơi → **trọng thương** (GD-26 §5.6), hồi sinh ở điểm hồi sinh gần nhất (ác nhân: hồi sinh ở Tạc Thiên Trại hoặc điểm hồi sinh hoang dã gần nhất, không phải thành trấn). Hình phạt theo trạng thái **nạn nhân**:

| Nạn nhân lúc chết | Thọ nguyên / tu vi (GD-26 §5.6) | Rơi đồ (PK-R-041) |
|-------------------|------------------|-------------------|
| `PEACE` + `CLEAN` | **Không mất** (PK-R-003); chỉ `ST_WEAKENED` 5 phút | Tối đa **1** chồng "chiến lợi phẩm gần đây" |
| `GUILD` / `SLAUGHTER` / `GREY` | Như trọng thương thường | Tối đa **2** chồng nguyên liệu không ràng buộc |
| `RED` | Như trọng thương thường | Tối đa **3** chồng nguyên liệu không ràng buộc |
| `CRIMSON` | Như trọng thương thường, `ST_WEAKENED` ×2 | 3 chồng + **1 trang bị / pháp bảo không ràng buộc trong túi** (không phải đồ đang mặc) có giá trị cao nhất |

- <a id="pk-r-041"></a>**PK-R-041** **Chọn đồ rơi (tất định):**
  - "Chiến lợi phẩm gần đây" = vật phẩm `MATERIAL` / `TREASURE` không ràng buộc vào túi trong **30 phút** gần nhất (hái, nhặt, rơi quái);
  - "Nguyên liệu không ràng buộc" = `MATERIAL` / `TREASURE` / `PILL` không ràng buộc trong túi;
  - chọn theo **giá trị (GD-13 `value`) giảm dần**, mỗi chồng rơi **một nửa** số lượng (làm tròn lên);
  - **không bao giờ rơi:** đồ đang mặc, vật phẩm ràng buộc, vật phẩm nhiệm vụ, linh thạch trong ví, Tiên Ngọc.
- <a id="pk-r-042"></a>**PK-R-042** Đồ rơi thành **túi chiến lợi phẩm** tại chỗ: 30s đầu chỉ kẻ kết liễu (và tổ đội) nhặt được, sau đó ai cũng nhặt; tồn tại 3 phút. `item_log` `PVP_DROPPED` / `PVP_LOOTED`.
- <a id="pk-r-043"></a>**PK-R-043** Bị cùng một người giết lại trong 30 phút: **không rơi đồ** lần sau (chống "nuôi" nạn nhân).

### 2.5. Huyết thù, Chấp Pháp, truy nã
- <a id="pk-r-050"></a>**PK-R-050** **Huyết thù:** người vô tội (`PEACE` + `CLEAN`) bị giết → kẻ giết vào danh sách Huyết thù của nạn nhân **24 giờ thực**: nạn nhân (không phải tổ đội) tấn công được kẻ đó ở mọi chế độ, không mang nghiệp. Danh sách hiển thị vị trí bản đồ (không tọa độ) của kẻ thù khi cùng vực.
- <a id="pk-r-051"></a>**PK-R-051** **NPC Chấp Pháp** — truy sát ác nhân, **đánh bại được nhưng rất khó**: khi một nhân vật `RED`+ ở bản đồ dã ngoại thuộc vực có Chấp Pháp Đường (Vực 1: mọi bản đồ trừ vùng quanh Tạc Thiên Trại ở `V1_F03`), mỗi `enforcerIntervalMin` (8 phút) server sinh **một tốp truy sát** gần ác nhân (ngoài tầm nhìn 30m), cảnh báo *"Chấp Pháp Đường đang truy lùng ngươi"* 10s trước; dây xích 60m, biến mất sau 3 phút nếu mất dấu.


| Ác nhân | Tốp truy sát | Sức mạnh |
|---------|-------------|----------|
| `RED` | 2 Chấp Pháp (`MON_E_ENFORCER`) | Cảnh giới = ác nhân **+2 tiểu cảnh giới**; máu ×3, công ×1,5, poise ×2 so với tinh anh cùng cảnh giới; khóa chân / phá đỡ; cảnh báo chiêu theo mức tinh anh (GD-12 EN-R-070) |
| `CRIMSON` | 3 Chấp Pháp + 1 **Chấp Pháp Trưởng Lão** (`MON_E_ENFORCER_ELDER`) | Như trên, Trưởng Lão +1 cảnh giới lớn so với ác nhân (trần `KD4` ở Vực 1), máu ×5, có chiêu `unblockable` |

  Mục tiêu thiết kế: một ác nhân đơn độc gần như không thắng được tốp truy sát; một tổ đội 3–5 người chuẩn bị kỹ thì thắng được.
- <a id="pk-r-054"></a>**PK-R-054** **Giết Chấp Pháp:** mọi người được tính công (GD-12 EN-R-100) khi hạ một Chấp Pháp / Trưởng Lão:
  - nhận **phần thưởng** riêng (bảng rơi `LT_ENFORCER` / `LT_ENFORCER_ELDER`): **Chấp Pháp Lệnh** (`ITM_ENFORCER_TOKEN`, không ràng buộc — đổi đồ hiếm ở Hắc Thị Tạc Thiên Trại hoặc bán), linh thạch theo hạng tinh anh ×3, tu vi như tinh anh;
  - **lập tức thành Đại ác nhân:** `infamy = max(infamy, 300) + 50` mỗi Chấp Pháp (Trưởng Lão +100) — kể cả người chỉ "giúp" một ác nhân;
  - nhận trạng thái **Thiên phạt** (`ST_HEAVEN_PUNISHMENT`, PK-R-055).
- <a id="pk-r-055"></a>**PK-R-055** **Thiên phạt & Thiên Đạo Hóa Thân:** người mang Thiên phạt bị **Thiên Đạo Hóa Thân** (`MON_B_HEAVEN_AVATAR`) truy sát:
  - xuất hiện sau `avatarFirstDelayMin` (5 phút) rồi mỗi `avatarIntervalMin` (15 phút), ở **mọi bản đồ dã ngoại** (kể cả quanh Tạc Thiên Trại — Hắc Thị không che được thiên phạt); báo trước bằng mây sấm tụ trên đầu 15s;
  - sức mạnh cấp boss: cảnh giới = người bị phạt **+1 cảnh giới lớn**, máu ×20 (nhân thêm theo số người mang Thiên phạt trong 30m), chiêu thiên lôi dùng lại sóng thiên kiếp (GD-04), có giai đoạn, mọi chiêu có cảnh báo theo mức boss;
  - **không vào** vùng an toàn / instance; Thiên phạt chỉ đếm ngược khi ở dã ngoại (trốn trong thành / trại không làm hết phạt);
  - Thiên phạt hết khi: bị Hóa Thân hạ (trọng thương, **thọ nguyên mất ×2** so với trọng thương thường, sát nghiệp −50, rơi đồ theo `CRIMSON`), **hoặc** đánh bại Hóa Thân, **hoặc** sống sót ở dã ngoại đủ `heavenPunishmentFieldMin` (60 phút cộng dồn);
  - **đánh bại Hóa Thân** (cực khó, thiết kế cho tổ đội mạnh): mọi người được tính công nhận **Thiên Đạo Tàn Ấn** (`ITM_HEAVEN_REMNANT_SEAL`, nguyên liệu hiếm cho chế công pháp phẩm Địa / luyện khí cao), danh hiệu **"Nghịch Thiên"**, thông báo toàn server ưu tiên 1; Thiên phạt xóa, nhưng sát nghiệp **không** giảm. Người tấn công Hóa Thân cũng nhận Thiên phạt (đã nghịch thiên thì cùng chịu);
  - chết vì Hóa Thân vẫn là **trọng thương**, không phải chết thật (PK-R-001).
- <a id="pk-r-056"></a>**PK-R-056** **Tạc Thiên Bang và Hóa Thân (chỉ biểu tượng, chốt PK-Q-4):** thành viên `ACTIVE` của Tạc Thiên Bang được tính công khi hạ Thiên Đạo Hóa Thân nhận thêm — (1) **danh hiệu theo bậc** (đếm số lần hạ Hóa Thân trong đời tài khoản, khi đang là thành viên Tạc Thiên Bang): lần 1 **"Tạc Thiên Tôn Giả"** · lần 3 **"Tạc Thiên Chí Tôn"** · lần 10 **"Tạc Thiên Đại Đế"** (bậc cao thay bậc thấp; hào quang sấm sét đậm dần theo bậc; bậc Đại Đế có thông báo toàn server khi đạt); (2) huy hiệu lưu trong Bia Vạn Cổ; (3) lời thoại riêng của NPC trong Tạc Thiên Trại và thông báo trong bản đồ của trại; (4) danh vọng `TA` +50 **tính trong trần ngày** (SO-R-04D). **Không** thêm vật phẩm, linh thạch, cống hiến, chỉ số hay tỷ lệ — phần thưởng sức mạnh giống hệt người khác (PK-R-055).
  > **Lưu ý nội dung (cho Content Designer / LR-02 Nhân vật):** NPC cấp cao của Tạc Thiên Bang **mang sẵn các danh hiệu này** để người chơi thấy mục tiêu và cảm giác thành tựu khi đạt được: **Bang chủ** (`NPC_TAC_THIEN_CHIEF`) — *"Tạc Thiên Đại Đế"*; **các Trưởng lão / Đường chủ** (`NPC_TAC_THIEN_ELDER_*`) — *"Tạc Thiên Chí Tôn"* hoặc *"Tạc Thiên Tôn Giả"*. Hiển thị danh hiệu trên đầu NPC cùng kiểu hào quang như của người chơi. Lời thoại phản ứng theo danh hiệu của người chơi: chưa có → coi thường; ngang bậc một Trưởng lão → gọi "đạo hữu"; đạt **Tạc Thiên Đại Đế** → Bang chủ đối xử **ngang hàng** (lời thoại + cảnh ngắn "đối ẩm" tại Tạc Thiên Trại, chỉ biểu tượng).
  > **Ghép NPC (LR-02 §3.3, CHR-R-036):** Bang chủ `NPC_TAC_THIEN_CHIEF` — Tạc Thiên Đại Đế (truyền thuyết của bang: đã hạ **mười** Hóa Thân, khớp ngưỡng lần 10; LR-01 §7); Đường chủ Chiến Đường Tần Ngạo `NPC_TAC_THIEN_ELDER_TAN_NGAO` — Tạc Thiên Chí Tôn; Đường chủ Hắc Thị Liễu Tam Nương `NPC_TAC_THIEN_ELDER_LIEU` (đổi Chấp Pháp Lệnh, PK-R-054) và Đường chủ Tiếp Dẫn Cung Bất Hối `NPC_TAC_THIEN_ELDER_CUNG` — Tạc Thiên Tôn Giả. Mã Đường chủ là đề xuất của LR-02 theo mẫu `NPC_TAC_THIEN_ELDER_*`, cần đăng ký glossary trước khi dùng.
- <a id="pk-r-052"></a>**PK-R-052** **Truy nã** (`CRIMSON`): hiện trên Bảng Truy Nã ở mọi thành trấn (tên, cảnh giới, vực hiện tại). Người hạ đại ác nhân nhận **tiền thưởng** = 10% linh thạch trong ví của đại ác nhân (trần 2.000; chuyển từ ví ác nhân, không sinh tiền mới) + danh vọng `CHINH` +50 (GD-14 §5.4c). Tiền thưởng truy nã bị **giữ lại chờ kiểm tra** khi nghi thông đồng giữa hai tài khoản (TD-08 SEC-R-055); hạn chế dùng chế độ `SLAUGHTER` có thể là một mức xử phạt (TD-08 §5).

### 2.6. Cân bằng chiến đấu PvP
- <a id="pk-r-060"></a>**PK-R-060** Sát thương người chơi → người chơi ×`pvpDamageMult` (0,5); hồi máu nhận được trong 5s sau khi trúng đòn PvP ×0,7; thời gian khống chế (STUN / ROOT / FROZEN / ENTANGLE) lên người chơi ×0,6, và **kháng khống chế** 8s sau mỗi lần bị khống chế (lần sau ×0,5, lần ba miễn). Chí mạng như PvE. (Chốt GD-06 SF-Q-4: hệ số chung, không bộ công thức riêng.)
- <a id="pk-r-061"></a>**PK-R-061** Bí pháp tiêu thọ (GD-07 SK-R-110) dùng được trong PvP. Tuyệt kỹ ×0,7 thêm trong PvP.
- <a id="pk-r-062"></a>**PK-R-062** **Bù trễ PvP** (TD-02 §7.2): tấn công người chơi tua lại tối đa `pvpMaxRewindMs` (100ms, thấp hơn 200ms của PvE); phòng thủ lùi tối đa 100ms như PvE — hai phía cân bằng.
- <a id="pk-r-063"></a>**PK-R-063** Linh thú xuất chiến (GD-25) đánh người chơi theo chế độ PK của chủ, chịu `pvpDamageMult`; sát nghiệp tính cho chủ.

---

## 3. Gamedata
```jsonc
// shared/gamedata/pvp/params.json
{
  "pvpEnabledDefault": false,
  "respawnImmunitySec": 10,
  "peaceSwitchSec": 300, "modeChangeCooldownSec": 60,
  "greySec": 60, "selfDefenseSec": 60,
  "infamyGain": { "PEACE_CLEAN": 50, "FLAGGED": 10, "GREY": 5, "RED": 0 },
  "infamyLowerVictimMult": 1.5, "infamyMajorRealmLowerMult": 3, "infamyRepeatKillMult": 2, "repeatKillWindowMin": 30,
  "infamyTiers": { "RED": 100, "CRIMSON": 300, "SLAYER_TITLE": 1000 },
  "infamyDeathByPlayer": 10, "infamyDeathByEnforcer": 30, "infamyDecayPerHourOnline": 10,
  "penanceQuest": { "reduce": 20, "dailyMax": 3 },
  "redShopPriceMult": 1.5,
  "drop": { "recentWindowMin": 30, "stacks": { "PEACE_CLEAN": 1, "FLAGGED": 2, "RED": 3, "CRIMSON": 3 }, "crimsonEquipment": 1,
            "stackFraction": 0.5, "killerLockSec": 30, "bagTtlSec": 180 },
  "feudHours": 24,
  "enforcer": { "intervalMin": 8, "warnSec": 10, "redCount": 2, "crimsonCount": 3, "crimsonElder": 1, "leashM": 60, "lostTrackSec": 180,
                "stageOffset": 2, "hpMult": 3, "atkMult": 1.5, "poiseMult": 2, "elderMajorOffset": 1, "elderHpMult": 5,
                "killInfamyFloor": 300, "killInfamyPer": 50, "killInfamyElder": 100 },
  "heavenAvatar": { "firstDelayMin": 5, "intervalMin": 15, "warnSec": 15, "majorOffset": 1, "hpMult": 20, "hpMultPerExtraTarget": 0.5,
                    "punishmentFieldMin": 60, "deathLifespanMult": 2, "deathInfamy": 50 },
  "bounty": { "walletPct": 0.10, "cap": 2000, "repChinh": 50 },
  "pvpDamageMult": 0.5, "pvpHealAfterHitMult": 0.7, "pvpCcDurationMult": 0.6, "ccDrSec": 8, "pvpUltimateMult": 0.7,
  "pvpMaxRewindMs": 100
}
```
**Validator:** bản đồ `FIELD` khai báo vùng an toàn quanh mọi truyền tống trận / lối nối; `V1_F03` khai báo vùng không có Chấp Pháp quanh Tạc Thiên Trại; `MON_E_ENFORCER`, `MON_E_ENFORCER_ELDER`, `MON_B_HEAVEN_AVATAR`, `LT_ENFORCER*`, `LT_HEAVEN_AVATAR` tồn tại; mọi chiêu của Chấp Pháp / Hóa Thân đạt cảnh báo tối thiểu GD-12.

---

## 4. Hợp đồng giao tiếp

| Loại | Tên | Ghi chú |
|------|-----|---------|
| C2S | `C2S_SetPkMode { seq, mode }` | PK-R-020 |
| S2C | `S2C_PkState { mode, infamy, tier, greyUntil, feuds }` | Gửi khi đổi |
| S2C | `S2C_PvpKilled { killerName, droppedCount, feudAdded }` | Cho nạn nhân |
| S2C | `S2C_EnforcerWarning { secondsLeft }` | PK-R-051 |
| S2C | `S2C_HeavenPunishment { active, fieldMinutesLeft, avatarInSec }` | PK-R-055 |
| State | `PlayerPublic.flags` thêm bit `PK_SLAUGHTER`, `GREY`, `RED`, `CRIMSON` (TD-02 §4.2) | Màu tên |
| API nội bộ | `POST /internal/v1/pvp/kill { eventId, killerId, victimId, mapId, victimMode, victimTier }` → `{ infamyDelta, drops: ItemInstanceId[], feudAdded }` | Idempotent theo `eventId`; gọi GD-26 `severe-injury` với cờ `pvpVictimProtected` |
| API công khai | `GET /v1/bounties` | Bảng truy nã |
| Sự kiện | `pvp.killed`, `pvp.tier_changed` | Telemetry, Chấp Pháp, thông báo bản đồ khi có `CRIMSON` mới |

**Lưu trữ:** `pk_state (character_id PK, mode, infamy, grey_until, last_pvp_combat_at, feuds JSONB)`; `pvp_kill_log (…)` partition theo tháng. `item_log` mới: `PVP_DROPPED`, `PVP_LOOTED`. `currency_ledger`: `BOUNTY_PAID`, `BOUNTY_RECEIVED`.

---

## 5. UI/UX
- Nút chế độ PK cạnh thanh máu (biểu tượng: hoa sen trắng / cờ tông / kiếm đỏ); đổi sang Sát lục cần xác nhận lần đầu mỗi kiếp.
- Màu tên: trắng (sạch) · cam (đang Sát lục / Tông môn) · xám (cờ xám) · đỏ (Ác nhân) · đỏ thẫm + biểu tượng truy nã.
- Vào vùng có PvP: dải chữ *"Giang hồ hiểm ác — có thể bị tấn công"*; vào vùng an toàn: *"An toàn"*.
- Bảng Huyết thù (danh sách kẻ thù, thời gian còn lại, vực hiện tại).
- Màn hồi sinh sau PvP: ai giết, mất gì (hoặc "Không mất thọ nguyên — ngươi là người vô tội").

---

## 6. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="pk-ac-001"></a>PK-AC-001 | A `SLAUGHTER`, B `PEACE` `CLEAN` | A giết B | B trọng thương **không mất thọ / tu vi**; A +50 sát nghiệp, thành `GREY`; B có Huyết thù với A 24h |
| PK-AC-002 | Như trên, B có 3 chồng nguyên liệu, 1 chồng hái 10 phút trước | Rơi đồ | Chỉ nửa chồng hái 10 phút trước rơi; khóa nhặt 30s cho A |
| PK-AC-003 | A giết lại B trong 30 phút | Kết quả | Sát nghiệp ×2 (+100); B không rơi đồ |
| PK-AC-004 | A `RED` (sát nghiệp 120) | Tới cổng Thanh Vân Trấn | Lính gác tấn công; NPC không phục vụ |
| PK-AC-005 | A `RED` ở `V1_F01` | 8 phút | Tốp 2 Chấp Pháp xuất hiện với cảnh báo 10s |
| PK-AC-006 | A `RED` bị Chấp Pháp hạ | Kết quả | Trọng thương thường, sát nghiệp −30 |
| PK-AC-007 | A `CRIMSON` (ví 15.000 linh thạch) bị C hạ | Kết quả | C nhận 1.500 linh thạch (từ ví A) + `CHINH` +50; A rơi 3 chồng + 1 trang bị không ràng buộc giá trị cao nhất trong túi |
| PK-AC-008 | Nhân vật `LK3` `PEACE` `CLEAN` bị `LK9` tấn công | Bị giết | Được phép; nạn nhân không mất thọ / tu vi; kẻ giết +50 × 1,5 = 75 sát nghiệp (cùng cảnh giới lớn, chênh ≥ 2 tầng) |
| PK-AC-009 | `KD1` giết `TC2` `PEACE` `CLEAN` | Kết quả | Được phép; +150 sát nghiệp (×3) → `RED` ngay |
| PK-AC-010 | Ở hiểm địa, cả hai `SLAUGHTER` | Tấn công nhau | Không gây sát thương (PK-R-002) |
| PK-AC-011 | A đánh B (B `PEACE`) 1 nhát | B đánh trả | Được (tự vệ), B không mang nghiệp |
| PK-AC-012 | B vừa bị STUN 2s | Bị STUN lần nữa trong 8s | 0,5 × 0,6 × 2 = 0,6s; lần ba miễn |
| PK-AC-013 | Đòn PvE gây 1.000 | Cùng đòn lên người chơi | 500 (`pvpDamageMult`) |
| PK-AC-014 | A đang chiến đấu PvP | Đổi về `PEACE` | Từ chối tới khi ngoài PvP 300s |
| PK-AC-015 | Server `pvpEnabled = false` | Bất kỳ | Không ai tấn công được người chơi |
| PK-AC-016b | Hai người `SLAUGHTER` ở `V1_F01` | Tấn công nhau | Không gây sát thương (`pvpDisabled`) |
| PK-AC-016 | Ác nhân online 3 giờ không giết ai | Sát nghiệp 120 | Còn 90 |
| PK-AC-017 | Ác nhân `RED` `TC2` | Tốp truy sát | 2 Chấp Pháp ở `TC4`, máu ×3, công ×1,5 so với tinh anh `TC4` |
| PK-AC-018 | Tổ 4 người (1 ác nhân 150, 3 người `CLEAN`) hạ 1 Chấp Pháp | Kết quả | Cả 4 nhận phần thưởng `LT_ENFORCER`; người `CLEAN` thành 350 (`CRIMSON`), ác nhân thành 350; cả 4 có Thiên phạt |
| PK-AC-019 | Mang Thiên phạt, ở dã ngoại | Sau 5 phút | Thiên Đạo Hóa Thân xuất hiện (cảnh báo mây sấm 15s), cảnh giới +1 cảnh giới lớn |
| PK-AC-020 | Mang Thiên phạt, trốn trong Tạc Thiên Trại 2 giờ | Ra dã ngoại | Thiên phạt chưa hết (chỉ đếm ở dã ngoại) |
| PK-AC-021 | Bị Hóa Thân hạ | Kết quả | Trọng thương, mất thọ ×2, sát nghiệp −50, rơi đồ `CRIMSON`; Thiên phạt hết; **không** chết thật |
| PK-AC-023 | Tổ có 1 thành viên Tạc Thiên Bang hạ Hóa Thân | Kết quả | Người đó thêm danh hiệu "Tạc Thiên Tôn Giả" (lần đầu) + hào quang + `TA` +50 (trong trần ngày); vật phẩm / linh thạch giống hệt đồng đội |
| PK-AC-022 | Tổ đội hạ Hóa Thân | Kết quả | Mỗi người nhận Thiên Đạo Tàn Ấn + danh hiệu Nghịch Thiên; thông báo toàn server ưu tiên 1; sát nghiệp giữ nguyên |

---

## 6b. Telemetry
Danh mục chuẩn ở [GD-20 §4](51-analytics.md): `pvp_kill`, `pvp_mode_change`, `pvp_tier_change`, `pvp_enforcer_fight`, `pvp_heaven_avatar` — trả lời: PvP có quấy người mới không, tỷ lệ ác nhân, Chấp Pháp có quá dễ / khó, Thiên phạt có được đánh bại.

## 7. Phạm vi
| Hạng mục | Alpha | Beta | Sau launch |
|----------|:---:|:---:|:---:|
| Cờ `pvpEnabled`, dữ liệu, chế độ PK (tắt mặc định) | ✅ | | |
| PvP dã ngoại, sát nghiệp, rơi đồ, Huyết thù, Chấp Pháp | | ✅ | |
| Truy nã + tiền thưởng, nhiệm vụ Sám hối | | ✅ | |
| Ma Vực (hiểm địa PvP, chết thật), Luận Võ Đài | | | ✅ (PLAN Q9) |

---

## 8. Câu hỏi mở
- [x] **PK-Q-1:** *(Đã chốt 2026-10-23: **không** mở server hòa bình.)* Có server "Hòa bình" (`pvpEnabled = false`) song song server PvP ngay từ Beta không (liên quan PLAN Q20)?
- [ ] **PK-Q-2:** Giết người khác phe (chính ↔ ma) có giảm sát nghiệp cộng (vd. ×0,5) và cộng danh vọng phe mình không? Có thể tạo xung đột phe tự nhiên, nhưng dễ thành cớ PK.
- [x] **PK-Q-4:** *(Đã chốt 2026-10-23: có, nhưng **chỉ biểu tượng** — danh hiệu theo bậc Tạc Thiên Tôn Giả → Chí Tôn → Đại Đế, hào quang, Bia Vạn Cổ, lời thoại; danh vọng trong trần ngày — PK-R-056.)* Thành viên Tạc Thiên Bang có nên nhận thêm phần thưởng khi hạ Thiên Đạo Hóa Thân?
- [ ] **PK-Q-3:** Người chơi `PEACE` có nên được chọn "Tị thế" (không bao giờ bị tấn công) đổi lại −20% phần thưởng thu thập ở dã ngoại?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.6 | 2026-10-25 | — | Đợt rà nhất quán: ghép NPC Tạc Thiên Bang với danh hiệu (PK-R-056) và NPC nhiệm vụ Sám hối (PK-R-031) theo LR-02 |
| v0.5 | 2026-10-25 | — | Thêm mục Telemetry (GD-20) |
| v0.4 | 2026-10-23 | — | `V1_F01` không PvP toàn bản đồ; không mở server hòa bình (PK-Q-1) |
| v0.3 | 2026-10-23 | — | Chốt PK-Q-4: thưởng **biểu tượng** — danh hiệu theo bậc Tạc Thiên Tôn Giả (1) / Chí Tôn (3) / Đại Đế (10) cho thành viên Tạc Thiên Bang khi hạ Thiên Đạo Hóa Thân (PK-R-056) |
| v0.2 | 2026-10-23 | — | **Bỏ miễn PvP theo cảnh giới (LK5) và giới hạn chênh cảnh giới** (dễ bị lợi dụng tranh đồ) → sát nghiệp ×3 khi giết người thấp hơn ≥ 1 cảnh giới lớn; **Chấp Pháp đánh bại được nhưng rất khó** (PK-R-051), có thưởng Chấp Pháp Lệnh (PK-R-054); giết Chấp Pháp → Đại ác nhân + **Thiên phạt**, bị **Thiên Đạo Hóa Thân** truy sát (PK-R-055); PK-AC-008/009 sửa, PK-AC-017..022 |
| v0.1 | 2026-10-23 | — | Bản nháp đầu (D-21): đánh giá, PvP dã ngoại có cờ server, vùng an toàn + miễn PvP, 3 chế độ PK + cờ xám + tự vệ, sát nghiệp (cộng / giảm khi chết / theo thời gian), ác nhân & đại ác nhân, chết PvP là trọng thương — người vô tội không mất thọ, rơi đồ tất định, Huyết thù, NPC Chấp Pháp truy sát, truy nã, hệ số PvP, bù trễ PvP, nghiệm thu |

