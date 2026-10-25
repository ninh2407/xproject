# GD-11 — Luyện khí

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-11 · Prefix ID: `FG` |
| Owner | Game Designer + Client Dev (minigame) |
| Trạng thái | 🟡 Bản nháp v0.5 |
| Cần trước | Phase 4 / Alpha (chế tạo trang bị, phân giải); Beta (cường hóa, tẩy luyện, thăng phẩm, khắc trận văn) |
| Quyết định nền | PLAN D-17 · PLAN §4.11, §4.13 |
| Liên quan | [PLAN §4.13](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-10 Luyện đan](16-alchemy.md) (khung chế tác dùng chung) · [GD-09 Vật phẩm](15-items-equipment.md) · [GD-13 Kinh tế](19-economy.md) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-27 Thiên phú](04-talents.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-28 Hiểm địa](27-danger-zones.md) |

> Đọc [docs/README.md §3](../README.md) và **GD-10 §3–§5** (khung `RecipeDef`, `CraftSession`, phí, bảo hiểm, thuần thục) trước khi triển khai. Tài liệu này chỉ mô tả **phần khác** của luyện khí. Dữ liệu ở `shared/gamedata/forging/*.json` (§7).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-11 | Mục |
|--------------|-----------------|-----|
| GD-09 | Nguồn trang bị phẩm Linh / Bảo / Tiên có thuộc tính phụ; cường hóa (`enhanceLevel`), tẩy luyện, thăng phẩm, ô khắc, phân giải; dị tượng | §5 |
| GD-13 | Phí chế tác / cường hóa / tẩy luyện; phân giải là kênh tiêu hao trang bị | §5.6–5.9, §6 |
| GD-27 | `CRAFT_QUALITY_PCT` (FORGING), Thợ rèn tiểu tử (`RCP_THIET_KIEM`) | §5.4, §5.11 |
| GD-26 | Tiêu thọ `CRAFTING` với khí phổ cao cấp | §5.4 |

---

## 1. Mục tiêu

**Trụ cột:** P4 *Chế tác có chiều sâu*.

- Luyện khí là **nguồn chính của trang bị phẩm cao** (ngoài đồ bộ bí cảnh): người chơi tự rèn ra Linh / Bảo / Tiên khí.
- Có **đích nhắm**: dùng chất xúc tác để định hướng thuộc tính phụ, tẩy luyện để tinh chỉnh, không phụ thuộc may rủi hoàn toàn.
- **Không phá hủy vật phẩm** khi cường hóa thất bại: game một mạng đã đủ khắc nghiệt; thất bại chỉ tốn tài nguyên hoặc tụt một cấp.
- Phân giải biến đồ thừa thành nguyên liệu, giữ kinh tế lành mạnh (GD-13).

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="fg-r-001"></a>**FG-R-001** | Mọi kết quả (thành / bại, phẩm, thuộc tính phụ, dị tượng, cường hóa, tẩy luyện, thăng phẩm) do **API Service** tính bằng RNG server, ghi `rng_audit`. Minigame rèn được server **chấm lại** từ chuỗi input (như GD-10 AL-R-001). Seed kết quả độc lập với seed bài nhịp, sinh lúc chấm ([GD-29 RF-R-050](05-rng-fairness.md)). |
| <a id="fg-r-002"></a>**FG-R-002** | **Không thao tác nào phá hủy trang bị** ngoài phân giải do người chơi chủ động. Cường hóa thất bại tối đa tụt 1 cấp; thăng phẩm thất bại giữ nguyên vật phẩm. |
| <a id="fg-r-003"></a>**FG-R-003** | Mọi chi phí (nguyên liệu, phí, thọ nguyên) tiêu **trước** khi roll, trong cùng transaction với kết quả (GD-10 AL-R-002). |
| <a id="fg-r-004"></a>**FG-R-004** | Tỷ lệ thành công và phân phối phẩm / kết quả luôn hiển thị trước khi thực hiện (GD-29). |
| <a id="fg-r-005"></a>**FG-R-005** | Luyện khí là nghề mọi nhân vật có từ cấp 1; cấp và khí phổ thuộc kiếp (GD-10 AL-R-005). |

---

## 2. Tổng quan

```
 Tại Lò Rèn (POI FORGE_STATION, Thanh Vân Trấn)
 ┌──────────────────────────────────────────────────────────────────────────────────────┐
 │ CHẾ TẠO (MVP)   khí phổ + khoáng + vật liệu quái + búa + [xúc tác] → minigame "rèn"   │
 │                 → thành / bại → điểm Q → PHẨM (Phàm…Tiên) → thuộc tính phụ (có định  │
 │                   hướng) → [dị tượng: +1 dòng]                                       │
 │ PHÂN GIẢI (MVP) trang bị → Linh Kim Tiết (+ hoàn 50% đá cường hóa đã dùng)           │
 │ LUYỆN ĐÁ (MVP)  Linh Kim Tiết + khoáng → Cường Hóa Thạch                             │
 ├──────────────────────────────────────────────────────────────────────────────────────┤
 │ CƯỜNG HÓA (Beta)  +0 → +15, ×1,06 chỉ số cơ bản mỗi cấp; ≥ +8 thất bại tụt 1 cấp     │
 │ TẨY LUYỆN (Beta)  tẩy giá trị mọi dòng / tẩy 1 dòng; xem kết quả rồi chọn giữ cũ/mới │
 │ THĂNG PHẨM (Beta) Phàm → Linh → Bảo → Tiên, thêm 1 dòng thuộc tính phụ               │
 │ KHẮC TRẬN VĂN (Beta) gắn trận văn vào ô khắc (Bảo 1 ô, Tiên 2 ô)                     │
 └──────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Thực thể & kiểu dữ liệu

Dùng lại `RecipeDef` (với `kind = 'FORGING'`), `RecipeInput`, `CraftSession`, `KnownRecipe`, `CraftProfile` của GD-10 §3. Bổ sung:

```ts
interface ForgeRecipeExt {             // trường thêm của RecipeDef khi kind = FORGING
  hammerTierReq: number;               // thay cho furnaceTierReq
  maxGrade: ItemGrade;                 // trần phẩm có thể rèn ra (vd. LK1 tối đa BAO)
  rhythm: { bpm: number; beats: number[]; jitterPct: number };  // beats: vị trí nhịp (đơn vị phách) trong bài
}

interface HammerSpec {                 // ItemTemplate.hammer, category TOOL (GD-09)
  tier: number; successFlat: number; qualityFlat: number;
  windowBonusMs: number;               // nới cửa sổ chấm nhịp
}

interface CatalystSpec {               // ItemTemplate.catalyst, category MATERIAL, tiêu 1 mỗi lần rèn
  qualityFlat?: number;
  affixBias?: { affixId: string; weightMult: number }[];   // tăng trọng số thuộc tính phụ (§5.3)
}

interface RuneSpec {                   // ItemTemplate.rune, category RUNE (Beta)
  modifier: { stat: CombatStat; kind: 'FLAT' | 'PCT'; value: number };
  allowedKinds: EquipKind[];
}

interface ForgeItemState {             // trạng thái phụ của từng trang bị (bảng riêng)
  itemInstanceId: string;
  enhancePity: number; upgradePity: number;
  enhanceStonesInvested: Record<string, number>;   // templateId đá → số lượng (để hoàn khi phân giải)
}

interface PendingReroll {
  pendingId: string; itemInstanceId: string; characterId: string;
  mode: 'VALUES' | 'LINE'; lineIndex: number | null;
  oldAffixes: AffixRoll[]; newAffixes: AffixRoll[];
  createdAt: string; expiresAt: string;
}

type ForgeAction = 'CRAFT' | 'ENHANCE' | 'REROLL' | 'UPGRADE' | 'DISMANTLE' | 'SOCKET' | 'UNSOCKET' | 'REFINE_STONE';
```

`ItemInstance` (GD-09) bổ sung trường `spiritBorn: boolean` (dị tượng, §5.3).

---

## 4. Máy trạng thái

- **Phiên chế tạo:** giống GD-10 §4 (`RUNNING` → `COMPLETED` / `CANCELLED`, ân hạn 3s, bị ngắt tính điểm tự động × 0,8).
- **Cường hóa, thăng phẩm, phân giải, khắc, luyện đá:** thao tác **tức thì** (một request, một transaction), không minigame.
- **Tẩy luyện:** `PendingReroll` được tạo → người chơi chọn `OLD` / `NEW` → áp dụng; hết `params.rerollPendingSec` (600s) mà chưa chọn thì **giữ cũ**. Trong khi có `PendingReroll`, trang bị không mặc / bán / phân giải / tẩy tiếp được.

---

## 5. Quy tắc

### 5.1. Nơi rèn và điều kiện chung
- <a id="fg-r-010"></a>**FG-R-010** Mọi thao tác cần đứng trong 3m quanh POI `FORGE_STATION`, không trong chiến đấu, không có phiên chế tác khác (GD-10 AL-R-030).
- <a id="fg-r-011"></a>**FG-R-011** Cấp luyện khí sư mở khóa thao tác:

| Thao tác | Cấp tối thiểu | Giai đoạn |
|----------|:---:|:---:|
| Chế tạo, phân giải, luyện đá | 1 (khí phổ có `levelReq` riêng) | MVP |
| Cường hóa tới +5 / +10 / +15 | 1 / 3 / 5 | Beta |
| Tẩy luyện | 2 | Beta |
| Thăng phẩm Phàm → Linh / Linh → Bảo / Bảo → Tiên | 2 / 3 / 5 | Beta |
| Khắc trận văn | 4 | Beta |

- <a id="fg-r-012"></a>**FG-R-012** Thao tác trên trang bị đang mặc được phép (tự tháo, xử lý, mặc lại); nếu thay đổi chỉ số thì phát `stats.inputs_changed` (GD-06).

### 5.2. Minigame "rèn" (nhịp búa)
- <a id="fg-r-020"></a>**FG-R-020** Bài nhịp tất định từ `rhythm` + seed: thời điểm phách `t_i = beats[i] × 60 / bpm`, mỗi đoạn 4 phách có dao động nhịp ±`jitterPct` (từ seed). Người chơi bấm chuột trái (búa) đúng phách.
- <a id="fg-r-021"></a>**FG-R-021** Chấm mỗi phách: lần bấm gần nhất chưa dùng trong cửa sổ `±(perfectMs + windowBonusMs)` → **Hoàn hảo** (1,0); trong `±(goodMs + windowBonusMs)` → **Tốt** (0,6); không có → **Trượt** (0). Lần bấm không thuộc phách nào → **đập lệch** (−0,2 vào tổng). Điểm theo §6.1.
- <a id="fg-r-022"></a>**FG-R-022** Kiểm tra input như GD-10 AL-R-042 (≤ 10 sự kiện/giây, tăng dần, trong thời lượng). Ngoài ra: nhân vật có tỷ lệ Hoàn hảo ≥ 98% trên ≥ 200 phách trong 24h → gắn cờ nghi dùng công cụ tự động (không chặn, để GM xem).
- <a id="fg-r-023"></a>**FG-R-023** Chế độ tự động: điểm 0,55 (GD-10 `autoScore`), xếp hàng tối đa 10.

### 5.3. Chế tạo: phẩm, thuộc tính phụ, dị tượng
- <a id="fg-r-030"></a>**FG-R-030** Thành / bại theo §6.2 (cùng dạng GD-10 §6.2, dùng búa thay đan lô). Thất bại: nhận 1 **Phế Phôi** (`ITM_PHE_PHOI`, phân giải được ra 1 Linh Kim Tiết); bảo hiểm +5.
- <a id="fg-r-031"></a>**FG-R-031** Thành công: tính `Q` (§6.3) → **phẩm** của trang bị: `Q < 40` Phàm · 40–69 Linh · 70–89 Bảo · ≥ 90 Tiên; chặn bởi `maxGrade` của khí phổ. Thần khí không rèn ra được (sau launch).
- <a id="fg-r-032"></a>**FG-R-032** Thuộc tính phụ roll theo GD-09 IT-R-041 (số dòng theo phẩm, không trùng `stat`). **Xúc tác** (`affixBias`) nhân trọng số của thuộc tính phụ chỉ định trong pool (vd. Hỏa Tinh Thạch: `AFX_ELEM_DMG_HOA` ×3).
- <a id="fg-r-033"></a>**FG-R-033** **Dị tượng:** khi `Q ≥ 95`, roll xác suất §6.4; trúng thì `spiritBorn = true`: trang bị có **thêm 1 dòng** thuộc tính phụ ngoài số dòng của phẩm, hiệu ứng "khí linh" (ánh sáng quanh vũ khí). Khí linh trưởng thành theo chủ: sau launch.
- <a id="fg-r-034"></a>**FG-R-034** Sản phẩm `bind = ON_EQUIP` (GD-09), `crafterCharacterId` = người rèn.

### 5.4. Khí phổ, búa, xúc tác, chi phí
- <a id="fg-r-040"></a>**FG-R-040** Khí phổ (`RECIPE`, `kind = FORGING`) cho từng ô và dải trang bị. Nguyên liệu theo dải (§7.2). Học qua vật phẩm (GD-10 AL-R-100); Thợ rèn tiểu tử khởi đầu biết `RCP_THIET_KIEM` (kiếm `LK1`), có `ITM_HAMMER_T1`, kinh nghiệm luyện khí 100 (GD-27 §5.2).
- <a id="fg-r-041"></a>**FG-R-041** Búa (`TOOL`) trong `BAG`, không tiêu hao. Xúc tác tiêu 1 mỗi lần rèn (không phụ thuộc số mẻ; luyện khí luôn 1 mẻ = 1 trang bị).
- <a id="fg-r-042"></a>**FG-R-042** **Phí chế tạo** (GD-13 EC-R-023): `round(0,05 × value(mẫu) × gradeValueMult[LINH])` (giá trị dự kiến ở phẩm Linh), tối thiểu 1, `CRAFT_FEE`.
- <a id="fg-r-043"></a>**FG-R-043** Khí phổ dải `TC1` trở lên có `lifespanYears = 1` (GD-26 `CRAFTING`), như đan đột phá.

### 5.5. Phân giải và luyện đá (MVP)
- <a id="fg-r-050"></a>**FG-R-050** **Phân giải** (chọn nhiều món cùng lúc): mỗi trang bị → `round(gradeValueMult[grade] × bandFactor[band(itemRealm)])` **Linh Kim Tiết** (`ITM_LINH_KIM_TIET`) + hoàn `floor(50%)` số Cường Hóa Thạch đã đầu tư (`enhanceStonesInvested`). Trang bị → `DESTROYED` (`DISMANTLE`). Được phân giải đồ ràng buộc; không phân giải đồ có `PendingReroll` hoặc đang ở `DANGER_BAG`.
- <a id="fg-r-051"></a>**FG-R-051** Xác nhận bắt buộc với trang bị phẩm Bảo trở lên hoặc cường hóa ≥ +5 (giữ nút 1s).
- <a id="fg-r-052"></a>**FG-R-052** **Luyện đá** (tức thì): `5 Linh Kim Tiết + 1 khoáng của dải → 1 Cường Hóa Thạch` của dải đó (`RCP_CUONG_HOA_THACH_{BAND}`), không minigame, không thất bại, có phí nhỏ (5% `value` đá).

### 5.6. Cường hóa (Beta)
- <a id="fg-r-060"></a>**FG-R-060** Nâng `enhanceLevel` từ L − 1 lên L (1 ≤ L ≤ 15). Chi phí: `ceil(L / 3)` **Cường Hóa Thạch** cùng dải `itemRealm` + phí §6.5. Chỉ số cơ bản ×`enhanceMult(L) = 1 + 0,06 × L` (GD-09 §6.3).
- <a id="fg-r-061"></a>**FG-R-061** Tỷ lệ thành công gốc theo L: +1..+5 100% · +6 90% · +7 80% · +8 70% · +9 60% · +10 50% · +11 40% · +12 35% · +13 30% · +14 25% · +15 20%; cộng bảo hiểm `min(5 × enhancePity, 25)`.
- <a id="fg-r-062"></a>**FG-R-062** Thất bại: `enhancePity += 1`; nếu L ≥ 8 thì **tụt về L − 2** (tức mất 1 cấp so với hiện tại), trừ khi dùng **Hộ Phù** (`ITM_HO_PHU`, tiêu khi thất bại, giữ nguyên cấp). L ≤ 7: không đổi cấp. Không bao giờ phá hủy (FG-R-002). Thành công: `enhancePity = 0`.
- <a id="fg-r-063"></a>**FG-R-063** Đá đã tiêu cộng vào `enhanceStonesInvested` (cả khi thất bại) để hoàn một phần khi phân giải.

### 5.7. Tẩy luyện (Beta)
- <a id="fg-r-070"></a>**FG-R-070** Hai chế độ:
  - `VALUES`: roll lại **giá trị** của mọi dòng (giữ nguyên `stat`). Chi phí 1 **Tẩy Luyện Thạch** + phí `round(0,05 × instanceValue)`.
  - `LINE`: roll lại **toàn bộ một dòng** (chọn `stat` mới từ pool, không trùng các dòng còn lại, và giá trị mới). Chi phí 2 Tẩy Luyện Thạch + phí ×2.
- <a id="fg-r-071"></a>**FG-R-071** Kết quả tạo `PendingReroll`; người chơi xem song song cũ / mới và chọn. Chi phí đã tiêu dù chọn bên nào.
- <a id="fg-r-072"></a>**FG-R-072** Dòng thêm từ dị tượng cũng tẩy được; xúc tác có `affixBias` dùng được khi tẩy `LINE` (tiêu 1).

### 5.8. Thăng phẩm (Beta)
- <a id="fg-r-080"></a>**FG-R-080** Nâng phẩm một bậc: Phàm → Linh (80%) · Linh → Bảo (50%) · Bảo → Tiên (25%), cộng `min(5 × (level − levelReqOfStep), 15)` và bảo hiểm `min(10 × upgradePity, 30)`. Chi phí: **Thăng Phẩm Thạch** 1 / 2 / 4 + phí `round(0,1 × instanceValue)`.
- <a id="fg-r-081"></a>**FG-R-081** Thành công: `grade += 1`, chỉ số cơ bản tính lại theo phẩm mới (GD-09 §6.1), roll **thêm 1 dòng** thuộc tính phụ, giữ nguyên các dòng cũ, cấp cường hóa, ô khắc (thêm ô theo phẩm mới). `upgradePity = 0`.
- <a id="fg-r-082"></a>**FG-R-082** Thất bại: mất nguyên liệu và phí, vật phẩm giữ nguyên, `upgradePity += 1`.

### 5.9. Khắc trận văn (Beta)
- <a id="fg-r-090"></a>**FG-R-090** Số ô khắc theo phẩm (GD-09: Bảo 1 · Tiên 2 · Thần 3). Gắn trận văn (`RUNE`) vào ô trống nếu `allowedKinds` khớp; không gắn hai trận văn cùng `stat` trên một món.
- <a id="fg-r-091"></a>**FG-R-091** Gỡ trận văn: mặc định **hủy** trận văn; trả phí `round(0,5 × value(trận văn))` hoặc dùng **Thoái Văn Phù** để lấy lại nguyên vẹn.
- <a id="fg-r-092"></a>**FG-R-092** Trận văn là `StatModifier` nguồn `EQUIPMENT` (GD-06). Trận văn được chế tạo bằng khí phổ khắc ấn (`RCP_RUNE_*`, minigame rèn, không có phẩm: luôn ra đúng trận văn nếu thành công).

### 5.10. Pháp bảo bản mệnh (sau Beta)
- <a id="fg-r-100"></a>**FG-R-100** Từ Kim Đan, chỉ định 1 pháp bảo làm bản mệnh (một lần mỗi kiếp; đổi lại tốn 2 năm thọ nguyên). "Ôn dưỡng" bằng linh thạch + nguyên liệu để lên cấp 1–10, mỗi cấp +5% bị động và hệ số kỹ năng chủ động. Đặc tả chi tiết ở phiên bản sau.

### 5.11. Cấp luyện khí sư và kinh nghiệm
- <a id="fg-r-110"></a>**FG-R-110** Cấp 1–9, ngưỡng kinh nghiệm như GD-10 §6.5. Kinh nghiệm theo §6.6. MVP tới cấp 4. Thuần thục khí phổ, bảo hiểm chế tạo: như GD-10 AL-R-091, AL-R-092.

---

### 5.12. Ký ức nghề & Khảo hạch
- <a id="fg-r-120"></a>**FG-R-120** Áp dụng nguyên cơ chế GD-10 §5.9 (AL-R-110..117) cho `kind = FORGING`: ký ức nghề cấp tài khoản, ba giai đoạn Sơ / Trung / Cao, khảo hạch tại Lò Rèn với trưởng lão `NPC_FORGE_ELDER`, khí phổ khảo hạch `RCP_TRIAL_FORGE_{SO|TRUNG|CAO}` (bài nhịp §5.2, nguyên liệu do trưởng lão cấp, không ra sản phẩm), cùng tham số `professionTiers`, `trialRealm`, `trialScore`, `trialFee`, `trialDailyAttempts` (đọc từ `alchemy/params.json`; ghi đè được trong `forging/params.json`). Đạt → khôi phục cấp + khí phổ trong ký ức (`source = PAST_LIFE`).

## 6. Công thức

### 6.1. Điểm minigame rèn
```
hit_i = 1,0 (|Δt| ≤ perfectMs + windowBonusMs) | 0,6 (|Δt| ≤ goodMs + windowBonusMs) | 0 (trượt)
score = clamp( (Σ hit_i − 0,2 × số lần đập lệch) / số phách , 0, 1 )
perfectMs = 60, goodMs = 120
AUTO: 0,55; bị ngắt: × 0,8
```

### 6.2. Tỷ lệ thành công chế tạo
```
success% = clamp( baseSuccess + min(10 × (level − levelReq), 20) + hammer.successFlat
                + 40 × (score − 0,5) + 3 × (proficiency − 1) + min(5 × pityFails, 25), 5, 98 )
```

### 6.3. Điểm phẩm chất → phẩm
```
Q = 100 × score × (1 + Σ CRAFT_QUALITY_PCT[FORGING])        (GD-27: TAL_STEADY_HAND +0,05; TAL_FORGE_SOUL +0,15)
  + hammer.qualityFlat + catalyst.qualityFlat
  + 3 × (proficiency − 1) + 2 × min(level − levelReq, 5)
  + (constitution − 50) × 0,1                                (luyện khí dùng Căn cốt thay Thần thức)
  + (luck − 50) × 0,1
  + U(−8; 8)
grade = Q < 40 ? PHAM : Q < 70 ? LINH : Q < 90 ? BAO : TIEN,  sau đó min(grade, recipe.maxGrade)
```

### 6.4. Dị tượng
```
spiritChance = Q ≥ 95 ? clamp(0,05 + (luck − 50) × 0,002, 0, 0,15) : 0
```

### 6.5. Phí cường hóa / tẩy luyện / thăng phẩm
```
instanceValue = value(mẫu) × gradeValueMult[grade]            (GD-13 §6.2)
enhanceFee(L)  = round(0,02 × instanceValue × L)
rerollFee      = round(0,05 × instanceValue) × (mode == LINE ? 2 : 1)
upgradeFee     = round(0,10 × instanceValue)
```

### 6.6. Kinh nghiệm luyện khí sư
```
craft:   recipe.exp × (thành công ? gradeExpMult[grade] : 0,3)      gradeExpMult: PHAM 1 · LINH 1,2 · BAO 1,5 · TIEN 2
enhance: thành công 5 × L, thất bại 1     reroll: 10     upgrade: thành công 50, thất bại 10
dismantle: 2 mỗi món                      refine stone: 1 mỗi viên    rune: recipe.exp
```

### 6.7. Ví dụ
**Rèn kiếm `LK4`** (`RCP_FORGE_WEAPON_SWORD_LK4`: `baseSuccess` 75, `levelReq` 2, `maxGrade` TIEN), luyện khí sư cấp 2, búa T1, không xúc tác, thuần thục 1, chưa thất bại, căn cốt 50, khí vận 50, khử dao động:
- `score = 0,75` → thành công `75 + 0 + 0 + 10 = 85%`.
- `Q = 75` → **Bảo khí** (2 dòng thuộc tính phụ). Với Khí Hồn (+0,15): `86,25` → vẫn Bảo. Với `score = 0,6`: `Q = 60` → Linh khí.
- Phí: `value` kiếm `LK4` = 80 (GD-13 §6.2) → `round(0,05 × 80 × 2,5) = 10` linh thạch.

**Cường hóa kiếm đó (Bảo, `instanceValue` 480) lên +8:** phí `round(0,02 × 480 × 8) = 77`; đá `ceil(8 / 3) = 3` Cường Hóa Thạch `LK_MID`; tỷ lệ 70% (+ bảo hiểm); thất bại không Hộ Phù → về +6.

**Phân giải kiếm Bảo `LK4` đã dùng 9 đá:** `round(6 × 2) = 12` Linh Kim Tiết + `floor(9 × 0,5) = 4` Cường Hóa Thạch.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/forging/params.json` | Tham số |
| `shared/gamedata/forging/recipes.json` | `RecipeDef[]` với `kind: FORGING` + `ForgeRecipeExt` |
| `shared/gamedata/forging/enhance.json` | Bảng tỷ lệ cường hóa, số đá theo cấp |
| `shared/gamedata/items/templates/forging_*.json` | Búa (`hammer`), xúc tác (`catalyst`), khoáng, đá, phù, trận văn (`rune`), khí phổ (`RECIPE`) |

```jsonc
// params.json
{
  "perfectMs": 60, "goodMs": 120, "perfectValue": 1.0, "goodValue": 0.6, "strayPenalty": 0.2,
  "maxEventsPerSec": 10, "botFlagPerfectRatio": 0.98, "botFlagMinBeats": 200,
  "qualityThresholds": { "LINH": 40, "BAO": 70, "TIEN": 90 },
  "qualityConstitutionFactor": 0.1, "qualityLuckFactor": 0.1, "qualityVariance": 8,
  "spiritMinQ": 95, "spiritBase": 0.05, "spiritLuckFactor": 0.002, "spiritMax": 0.15,
  "craftFeePct": 0.05, "craftFeeGradeBasis": "LINH",
  "dismantleBandFactor": { "PN": 1, "LK_EARLY": 1, "LK_MID": 2, "LK_LATE": 3, "TC": 6, "KD": 15 },
  "dismantleStoneRefundPct": 0.5,
  "refineStone": { "fragments": 5, "ore": 1, "feePct": 0.05 },
  "enhanceLevelReq": { "5": 1, "10": 3, "15": 5 },
  "enhanceChance": [100, 100, 100, 100, 100, 90, 80, 70, 60, 50, 40, 35, 30, 25, 20],
  "enhanceDowngradeFrom": 8, "enhancePityStep": 5, "enhancePityCap": 25, "enhanceFeePct": 0.02,
  "rerollLevelReq": 2, "rerollFeePct": 0.05, "rerollLineMult": 2, "rerollPendingSec": 600,
  "upgradeChance": { "PHAM": 80, "LINH": 50, "BAO": 25 }, "upgradeLevelReq": { "PHAM": 2, "LINH": 3, "BAO": 5 },
  "upgradeStones": { "PHAM": 1, "LINH": 2, "BAO": 4 }, "upgradeFeePct": 0.10,
  "upgradeLevelBonus": 5, "upgradeLevelBonusCap": 15, "upgradePityStep": 10, "upgradePityCap": 30,
  "socketLevelReq": 4, "unsocketFeePct": 0.5,
  "gradeExpMult": { "PHAM": 1, "LINH": 1.2, "BAO": 1.5, "TIEN": 2 },
  "mvpMaxLevel": 4
}
```

### 7.1. Nguyên liệu, công cụ Vực 1

| ID | Tên | Phẩm | Loại | Nguồn |
|----|-----|------|------|-------|
| `ITM_ORE_HAN_THIET` | Hàn Thiết | PHAM | Khoáng (`LK_EARLY`) | Khoáng mạch F01–F03; Tạp Hóa (NPC) |
| `ITM_ORE_XICH_DONG` | Xích Đồng | PHAM | Khoáng (`LK_MID`) | F03, F04 |
| `ITM_ORE_HUYEN_THIET` | Huyền Thiết | LINH | Khoáng (`LK_LATE`) | Ẩn cấp 1 ở F03, F06 |
| `ITM_ORE_TINH_KIM` | Tinh Kim | BAO | Khoáng (`TC`) | `V1_HD02`, rương hiểm địa |
| `ITM_WOLF_PELT` · `ITM_BOAR_HIDE` · `ITM_CRAB_SHELL` | Da sói · Da lợn rừng · Mai cua | PHAM | Vật liệu quái | GD-12 |
| `ITM_ELEM_STONE_{X}` | Kim / Mộc / Thủy / Hỏa / Thổ Tinh Thạch | LINH | Xúc tác: `affixBias` ×3 cho `AFX_ELEM_DMG_{X}` và `AFX_ELEM_RES_{X}`, `qualityFlat` +3 | Tinh anh, cơ duyên |
| `ITM_LINH_KIM_TIET` | Linh Kim Tiết | PHAM | Mảnh phân giải | Phân giải |
| `ITM_CUONG_HOA_THACH_{BAND}` | Cường Hóa Thạch (theo dải) | LINH | Đá cường hóa | Luyện đá, rơi đồ tinh anh / boss |
| `ITM_TAY_LUYEN_THACH` | Tẩy Luyện Thạch | BAO | Tẩy luyện | Boss bí cảnh, hiểm địa |
| `ITM_THANG_PHAM_THACH` | Thăng Phẩm Thạch | BAO | Thăng phẩm | Hiểm địa Nhị giai, boss |
| `ITM_HO_PHU` | Hộ Phù | BAO | Chống tụt cấp cường hóa | Hiếm: cơ duyên, rương Truyền thuyết |
| `ITM_THOAI_VAN_PHU` | Thoái Văn Phù | LINH | Gỡ trận văn nguyên vẹn | Tông Môn Bảo Khố |
| `ITM_PHE_PHOI` | Phế Phôi | PHAM | Rèn hỏng | Chế tạo thất bại |
| `ITM_HAMMER_T1` | Thiết Chùy | — | Búa: tier 1, +0 / +0, cửa sổ +0ms | Binh Khí Phường, Thợ rèn tiểu tử |
| `ITM_HAMMER_T2` | Huyền Thiết Chùy | — | Búa: tier 2, thành công +5, phẩm chất +5, cửa sổ +15ms | Tông Môn Bảo Khố, BC02 |
| `ITM_HAMMER_T3` | Lôi Văn Chùy | — | Búa: tier 3, +8 / +10, cửa sổ +25ms | BC03, rương Nhị giai |

### 7.2. Khí phổ MVP
Một khí phổ cho mỗi ô (kiếm, quạt, áo, mũ, đai, giày, nhẫn, ngọc bội) × dải; đầu ra là mẫu trang bị GD-09 cùng ô / dải.

| Dải | ID mẫu | Cấp | Búa | `maxGrade` | Nguyên liệu (vũ khí / nhẫn / ngọc bội) | Nguyên liệu (giáp: áo, mũ, đai, giày) | Thành công gốc | Nguồn khí phổ |
|-----|--------|:---:|:---:|:---:|----------------------------------------|---------------------------------------|:---:|---------------|
| `LK1` | `RCP_FORGE_{KIND}_LK1` (kiếm = `RCP_THIET_KIEM`) | 1 | 1 | BAO | 3 Hàn Thiết + 1 Linh Kim Tiết | 2 Hàn Thiết + 2 Da sói | 80 | Binh Khí Phường; Thợ rèn tiểu tử |
| `LK4` | `RCP_FORGE_{KIND}_LK4` | 2 | 1 | TIEN | 2 Hàn Thiết + 2 Xích Đồng + 1 Linh Kim Tiết | 2 Xích Đồng + 2 Da lợn rừng | 75 | Binh Khí Phường |
| `LK7` | `RCP_FORGE_{KIND}_LK7` | 3 | 2 | TIEN | 3 Xích Đồng + 1 Huyền Thiết + 2 Linh Kim Tiết | 2 Huyền Thiết + 2 Mai cua | 65 | Tông Môn Bảo Khố |
| `TC1` (Beta) | `RCP_FORGE_{KIND}_TC1` | 4 | 3 | TIEN | 2 Huyền Thiết + 1 Tinh Kim + … | … | 55 | Hiểm địa Nhị giai; tiêu 1 năm thọ |

```jsonc
// recipes.json (trích)
{ "id": "RCP_THIET_KIEM", "kind": "FORGING", "nameKey": "rcp.thiet_kiem", "outputTemplateId": "ITM_EQ_WEAPON_SWORD_LK1",
  "levelReq": 1, "hammerTierReq": 1, "maxGrade": "BAO",
  "inputs": [ { "templateId": "ITM_ORE_HAN_THIET", "qty": 3, "role": "MAIN", "addStage": 0 },
              { "templateId": "ITM_LINH_KIM_TIET", "qty": 1, "role": "AUX", "addStage": 0 } ],
  "yield": [1, 1], "durationSec": 20,
  "rhythm": { "bpm": 100, "beats": [0, 1, 2, 3, 4, 5, 6, 6.5, 7, 8, 9, 10, 10.5, 11, 12, 13, 14, 15], "jitterPct": 0.03 },
  "baseSuccess": 80, "exp": 12, "tags": ["weapon", "sword"] }
```

**Validator (CI):** khí phổ `FORGING` có `hammerTierReq`, `maxGrade`, `rhythm` (phách tăng dần, ≥ 8 phách, `bpm` trong [70, 160]); `outputTemplateId` là trang bị cùng dải với tên khí phổ; xúc tác `affixBias.affixId` tồn tại; `enhanceChance` đủ 15 phần tử giảm dần không tăng; trận văn không trùng `stat` với thuộc tính phụ bị cấm ở ô (GD-09 §5.4).

---

## 8. Mục tiêu cân bằng ban đầu

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Phẩm phổ biến khi rèn với điểm minigame 0,7 | Linh – Bảo |
| Tỷ lệ ra Tiên khí (điểm 0,9+, lô / búa tốt, thiên phú) | 10–25% |
| Thời gian để có đủ bộ Linh khí tự rèn mỗi dải | 2–3 giờ (gồm khai khoáng) |
| Cường hóa +10 trung bình (Beta) | ~25–35 lần thử, ~1 tuần chơi |
| Chiến lực bộ Bảo +10 so với bộ chuẩn | +45–55% (vẫn trong trần chênh lệch GD-09 §8 khi không tính cường hóa) |

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai
> **Đường dẫn, method và khóa idempotency chuẩn ở [TD-04 §5](../tech/03-api.md)** (mọi thao tác của nhân vật nằm dưới `/v1/characters/{id}/…`, xem trước dùng `POST`, khóa idempotency qua header `Idempotency-Key`). Bảng dưới mô tả mục đích; khi vênh với TD-04, theo TD-04.

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/characters/{id}/forging` | Cấp, kinh nghiệm, khí phổ đã biết | |
| `GET /v1/characters/{id}/forging/preview` | `?action&recipeId&itemInstanceId&hammerInstanceId&catalystInstanceId&targetLevel&mode` → tỷ lệ, phân phối phẩm, chi phí, kết quả nếu thất bại | FG-R-004 |
| `POST /v1/characters/{id}/forging/sessions` · `POST /v1/forging/sessions/{id}/submit` · `/cancel` | Chế tạo (giống GD-10 §9.1; `events` là các lần đập búa `{ tMs, type: 'HIT' }`) | §5.2–5.4 |
| `POST /v1/characters/{id}/forging/dismantle` | `{ itemInstanceIds: string[], confirm }` → nguyên liệu nhận | FG-R-050, 051 |
| `POST /v1/characters/{id}/forging/refine-stone` | `{ recipeId, count }` | FG-R-052 |
| `POST /v1/characters/{id}/forging/enhance` | `{ itemInstanceId, useProtection }` → `{ success, level, pity }` (Beta) | §5.6 |
| `POST /v1/characters/{id}/forging/reroll` | `{ itemInstanceId, mode, lineIndex?, catalystInstanceId? }` → `{ pendingId, oldAffixes, newAffixes }` (Beta) | §5.7 |
| `POST /v1/forging/rerolls/{pendingId}/decide` | `{ keep: 'OLD' \| 'NEW' }` | FG-R-071 |
| `POST /v1/characters/{id}/forging/upgrade` | `{ itemInstanceId }` → `{ success, grade, newAffix? }` (Beta) | §5.8 |
| `POST /v1/characters/{id}/forging/socket` · `/unsocket` | `{ itemInstanceId, socketIndex, runeInstanceId? , useScroll? }` (Beta) | §5.9 |

Mọi request thao tác đều có `requestId` idempotent; Game Server xác nhận vị trí Lò Rèn và trạng thái chiến đấu.

### 9.2. Gọi sang hệ thống khác
`inventory/consume` (`CRAFT_INPUT`, `ENHANCE`, `REROLL`, `UPGRADE`), `inventory/grant` (`CRAFT_OUTPUT`, `DISMANTLE` nguyên liệu), `inventory/move` → `DESTROYED` (`DISMANTLE`), `currency/change` (`CRAFT_FEE`, `ENHANCE_FEE`), `lifecycle/lifespan-change` (`CRAFTING`).

### 9.3. Sự kiện domain

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `craft.completed` | (GD-10) `kind: 'FORGING'`, `grade`, `spiritBorn` | Telemetry, thông báo đồ hiếm (Tiên khí, dị tượng: tin toàn server) |
| `forge.item_changed` | `{ characterId, itemInstanceId, action: ForgeAction, before, after }` | GD-06 (nếu đang mặc), telemetry |

---

## 10. Lưu trữ

```sql
ALTER TABLE item_instance ADD COLUMN spirit_born BOOLEAN NOT NULL DEFAULT FALSE;   -- GD-09

CREATE TABLE forge_item_state (
  item_instance_id UUID PRIMARY KEY REFERENCES item_instance(id),
  enhance_pity SMALLINT NOT NULL DEFAULT 0,
  upgrade_pity SMALLINT NOT NULL DEFAULT 0,
  enhance_stones_invested JSONB NOT NULL DEFAULT '{}'
);

CREATE TABLE forge_pending_reroll (
  pending_id UUID PRIMARY KEY,
  item_instance_id UUID NOT NULL UNIQUE REFERENCES item_instance(id),
  character_id UUID NOT NULL,
  mode VARCHAR(8) NOT NULL, line_index SMALLINT,
  old_affixes JSONB NOT NULL, new_affixes JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE forge_action_log (                    -- idempotency + kiểm toán thao tác tức thì
  request_id VARCHAR(80) PRIMARY KEY,
  character_id UUID NOT NULL, item_instance_id UUID, action VARCHAR(16) NOT NULL,
  input JSONB NOT NULL, result JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```
Dùng lại `profession` (`kind = FORGING`), `recipe_known`, `craft_session` của GD-10 §10. `rng_audit.kind` mới: `FORGE_SUCCESS`, `FORGE_QUALITY`, `FORGE_AFFIX`, `FORGE_SPIRIT`, `FORGE_RHYTHM`, `ENHANCE`, `REROLL`, `UPGRADE`.

---

## 11. UI/UX
- **Màn Lò Rèn:** các tab Chế tạo · Phân giải · Luyện đá · (Beta) Cường hóa · Tẩy luyện · Thăng phẩm · Khắc. Mỗi tab có bảng tỷ lệ và chi phí (FG-R-004).
- **Minigame rèn:** phôi đỏ rực trên đe; vòng nhịp thu nhỏ về tâm báo phách; đập đúng lóe tia lửa (Hoàn hảo: tia vàng + âm "keng" trong); trượt: âm đục. Nhạc nền theo `bpm` của khí phổ.
- **Kết quả chế tạo:** trang bị nguội dần, màu ánh sáng theo phẩm; dị tượng: hư ảnh khí linh hiện quanh vật phẩm, tin toàn server.
- **Cường hóa (Beta):** búa đập lên vật phẩm; hiện rõ "Thất bại sẽ về +6" khi L ≥ 8; ô tick dùng Hộ Phù.
- **Tẩy luyện (Beta):** hai cột Cũ / Mới, nút "Giữ cũ" / "Lấy mới", đồng hồ 10 phút.
- **Phân giải:** chọn nhiều, tổng nguyên liệu nhận được ở dưới, cảnh báo món Bảo khí trở lên.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-10 | Khung `RecipeDef` / `CraftSession` / bảo hiểm / thuần thục / cấp nghề dùng chung |
| GD-09 | Tạo trang bị có phẩm và thuộc tính phụ; `enhanceLevel`, `sockets`, `spiritBorn`; `DISMANTLE` |
| GD-06 | Cường hóa, trận văn thay đổi chỉ số |
| GD-13 | Phí, phân giải là sink trang bị, Tiên khí là hàng hóa (Beta) |
| GD-27 | `CRAFT_QUALITY_PCT`, căn cốt, khí vận; Thợ rèn tiểu tử |
| GD-12 | Vật liệu quái, đá cường hóa từ tinh anh / boss |
| GD-28 | Tinh Kim, Thăng Phẩm Thạch từ hiểm địa |
| GD-21 | Khoáng mạch, POI `FORGE_STATION` |
| GD-26 | Tiêu thọ khi rèn khí phổ cao cấp; bản mệnh |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Pool thuộc tính phụ không đủ dòng khác nhau cho dị tượng | Bỏ dòng thêm, giữ `spiritBorn = true` (chỉ hiệu ứng hình ảnh); validator cảnh báo pool quá nhỏ |
| Cường hóa trang bị đang mặc rồi thất bại tụt cấp | Chỉ số cập nhật ngay (`stats.inputs_changed`) |
| Phân giải món đang có `PendingReroll` | Từ chối |
| Đăng xuất khi có `PendingReroll` | Hết hạn 10 phút → giữ cũ |
| Thăng phẩm thành công khi pool không còn thuộc tính khác | Thăng phẩm vẫn thành công, không thêm dòng; báo cho người chơi |
| Gửi `enhance` hai lần cùng `requestId` | Trả kết quả lần đầu |
| Hết kiếp khi có `PendingReroll` | Bỏ (vật phẩm bị hủy theo GD-26) |
| Đá cường hóa sai dải | Từ chối; UI chỉ hiện đá đúng dải |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `forge_craft` | recipeId, score, success, grade, spiritBorn, catalyst | Phân phối phẩm theo điểm, giá trị của xúc tác |
| `forge_rhythm` | perfectRatio, goodRatio, missRatio, bpm | Độ khó minigame, nghi công cụ tự động |
| `forge_enhance` | targetLevel, success, protectionUsed, pity | Đường cong cường hóa thực tế |
| `forge_reroll` | mode, kept (OLD / NEW) | Tẩy luyện có hữu ích |
| `forge_upgrade` | fromGrade, success | |
| `forge_dismantle` | grade, enhanceLevel, count | Kênh tiêu hao trang bị |
| `gear_source` | grade, source (crafted / drop / NPC / set) | Luyện khí có thật sự là nguồn đồ phẩm cao chính |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="fg-ac-001"></a>FG-AC-001 | Ví dụ rèn kiếm §6.7 | Tính | Thành công 85%; Q 75 → Bảo (2 dòng); `score` 0,6 → Linh; phí 10 |
| FG-AC-002 | Khí phổ `LK1` (`maxGrade` BAO), Q = 93 | Kết quả | Phẩm Bảo (bị chặn), không Tiên |
| FG-AC-003 | Cùng seed + cùng chuỗi đập búa | Chấm client / server | Điểm giống hệt |
| FG-AC-004 | 18 phách, 12 Hoàn hảo, 4 Tốt, 2 trượt, 1 đập lệch | Chấm | `(12 + 2,4 − 0,2) / 18 = 0,789` |
| FG-AC-005 | Xúc tác Hỏa Tinh Thạch, kiếm phẩm Bảo, 100.000 lần | Mô phỏng | Tỷ lệ có `AFX_ELEM_DMG_HOA` cao hơn rõ rệt so với không xúc tác (trọng số ×3) |
| FG-AC-006 | Q = 97, khí vận 50, 100.000 lần | Mô phỏng | Tỷ lệ dị tượng ≈ 5%; món dị tượng phẩm Tiên có 4 dòng |
| FG-AC-007 | Rèn thất bại | Kết quả | 1 Phế Phôi; bảo hiểm +5 |
| FG-AC-008 | Thợ rèn tiểu tử mới tạo | Mở Lò Rèn | Biết `RCP_THIET_KIEM`; có `ITM_HAMMER_T1`; kinh nghiệm 100 |
| FG-AC-009 | Phân giải ví dụ §6.7 | Phân giải | 12 Linh Kim Tiết + 4 Cường Hóa Thạch; kiếm `DESTROYED` (`DISMANTLE`) |
| FG-AC-010 | 10 Linh Kim Tiết + 2 Hàn Thiết | Luyện đá × 2 | 2 `ITM_CUONG_HOA_THACH_LK_EARLY`, không thất bại |
| FG-AC-011 | (Beta) Cường hóa ví dụ §6.7 lên +8 | Thực hiện | Phí 77, tiêu 3 đá; tỷ lệ 70% |
| FG-AC-012 | (Beta) Thất bại khi lên +8 không Hộ Phù | Kết quả | Về +6; bảo hiểm +1; đá cộng vào `enhanceStonesInvested` |
| FG-AC-013 | (Beta) Thất bại khi lên +8 có Hộ Phù | Kết quả | Giữ +7; Hộ Phù bị tiêu |
| FG-AC-014 | (Beta) Thất bại khi lên +6 | Kết quả | Giữ +5; Hộ Phù (nếu chọn) **không** bị tiêu |
| FG-AC-015 | (Beta) Mọi thao tác cường hóa / thăng phẩm thất bại | Kiểm tra | Không vật phẩm nào bị `DESTROYED` (FG-R-002) |
| FG-AC-016 | (Beta) Tẩy `VALUES`, chọn giữ cũ | Hoàn tất | Thuộc tính phụ không đổi; Tẩy Luyện Thạch và phí đã tiêu |
| FG-AC-017 | (Beta) Tẩy `LINE` dòng 2 | Kết quả mới | `stat` mới không trùng các dòng còn lại |
| FG-AC-018 | (Beta) `PendingReroll` hết 10 phút | Job | Giữ cũ, mở khóa vật phẩm |
| FG-AC-019 | (Beta) Thăng phẩm Linh → Bảo thành công | Kết quả | Phẩm Bảo, chỉ số cơ bản ×1,30 / 1,15, thêm 1 dòng, thêm 1 ô khắc, giữ `enhanceLevel` |
| FG-AC-020 | (Beta) Gắn hai trận văn cùng `stat` | Khắc | Từ chối |
| FG-AC-021 | (Beta) Gỡ trận văn không phù, không phí | Gỡ | Trận văn bị hủy |
| FG-AC-022 | Luyện khí sư cấp 2, khí phổ `LK7` (cấp 3) | Bắt đầu | Từ chối |
| FG-AC-023 | Đồ đang ở `DANGER_BAG` | Phân giải | Từ chối |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Alpha (MVP) | Beta | Sau launch |
|----------|:---:|:----:|:----------:|
| Minigame rèn (server chấm lại), chế tạo tự động | ✅ | | |
| Chế tạo dải `LK1` / `LK4` / `LK7`, xúc tác định hướng, dị tượng | ✅ | | |
| Phân giải, luyện đá | ✅ | | |
| Cấp luyện khí 1–4 | ✅ | | |
| Cường hóa +1 → +15, Hộ Phù | | ✅ | |
| Tẩy luyện, thăng phẩm | | ✅ | |
| Khắc trận văn, khí phổ khắc ấn | | ✅ | |
| Dải `TC1`, cấp 5–9 | | ✅ | |
| Pháp bảo bản mệnh, khí linh trưởng thành | | | ✅ |
| Thần khí | | | ✅ |

---

## 17. Câu hỏi mở
- [ ] **FG-Q-1:** Cường hóa ≥ +8 thất bại tụt 1 cấp có quá gắt? Phương án mềm: không tụt cấp, chỉ mất tài nguyên.
- [ ] **FG-Q-2:** Trang bị tự rèn `ON_EQUIP` có làm thợ rèn khó bán hàng ở Beta? (Bán được trước khi mặc, nên vẫn ổn.)
- [ ] **FG-Q-3:** Minigame nhịp cần hiệu chỉnh độ trễ âm thanh / màn hình cho từng máy (bước "căn chỉnh độ trễ" trong tùy chọn)? Xem cue phách và bù độ trễ ở [AU-01 §5](../audio/audio-design.md).
- [ ] **FG-Q-4:** Dị tượng có nên được "đặt tên" như công pháp tự tạo (GD-08) để tăng cảm giác sở hữu? (GD-08 v0.1 đã có quy trình đặt tên + lọc + GM đổi tên, TF-R-005; có thể tái dùng.)
- [ ] **FG-Q-5:** Đưa cường hóa +1 → +5 vào MVP (an toàn, không thất bại) để Thợ rèn tiểu tử có thêm việc làm?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-21 | — | Bản nháp đầu: dùng chung khung GD-10, minigame rèn theo nhịp (server chấm lại), chế tạo (phẩm theo Q, xúc tác định hướng, dị tượng), phân giải, luyện đá, cường hóa (không phá hủy), tẩy luyện (giữ cũ / mới), thăng phẩm, khắc trận văn, cấp luyện khí sư, công thức + ví dụ, nguyên liệu & khí phổ Vực 1, API, DB, nghiệm thu; chia phạm vi Alpha / Beta |
| v0.2 | 2026-10-23 | — | Ký ức nghề & khảo hạch theo giai đoạn (§5.12, theo GD-10 §5.9) |
| v0.3 | 2026-10-25 | — | Trỏ đường dẫn API chuẩn sang TD-04 §5 |
| v0.4 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
| v0.5 | 2026-10-25 | — | FG-Q-3 liên kết AU-01 §5 |

