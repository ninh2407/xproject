# GD-08 — Chế tạo công pháp

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-08 · Prefix ID: `TF` |
| Owner | Game Designer + Technical Artist (VFX tham số) + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.1 |
| Cần trước | Phase 4 / Alpha (bản đầu: phẩm Hoàng – Huyền, theo PLAN Phase 4), Beta (sao chép, giao dịch ngọc giản, phẩm Địa) |
| Quyết định nền | PLAN §4.10 (trụ cột P4, tính năng đặc trưng), D-15 (tự đặt tên + lọc + báo cáo), D-17 |
| Liên quan | [PLAN §4.10](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-07 Kỹ năng](13-skills.md) (khung `SkillDef`, ngân sách §6.5) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-09 Vật phẩm](15-items-equipment.md) · [GD-27 Thiên phú](04-talents.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-13 Kinh tế](19-economy.md) · [GD-14 Xã hội](20-social.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-28 Hiểm địa](27-danger-zones.md) |

> Đọc [docs/README.md §3](../README.md) và **GD-07 §3, §5.6, §6.5** trước khi triển khai. Kỹ năng chế tạo là một `SkillDef` bình thường (GD-07) sinh ra từ dữ liệu thành phần; engine chiến đấu **không phân biệt** kỹ năng chế tạo với kỹ năng tác giả viết. Dữ liệu ở `shared/gamedata/technique_forge/*.json` (§7).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-08 | Mục |
|--------------|-----------------|-----|
| GD-07 | Kỹ năng `CSK_*` (nguồn `CRAFTED`), quy tắc ghép `SkillDef`, bổ sung công thức `powerScore` | §5.3, §6.3 |
| GD-09 | Mảnh ngọc giản (`TECHNIQUE_FRAGMENT`), ngọc giản tự chế (`craftedRefId`) | §3, §5.6 |
| GD-12 / GD-22 / GD-28 | Bảng rơi mảnh ngọc giản theo phẩm (`LT_*_TECH_FRAGMENT_*`) | §5.8 |
| GD-27 | Ngộ tính, khí vận ảnh hưởng chất lượng & đột biến | §6.2 |
| GD-26 | Tiêu thọ `CRAFTING` khi diễn pháp phẩm cao | §5.4 |
| GD-13 | Giá trị ngọc giản theo phẩm, phí | §5.4, §6.4 |

---

## 1. Mục tiêu

**Trụ cột:** P4 *Sáng tạo & chế tác có chiều sâu* (chính), P1 *Mỗi kiếp một câu chuyện*.

- Người chơi **sáng tạo chiêu thức của riêng mình**, đặt tên, mang dấu ấn cá nhân ("Liệt Phong Kiếm Ý, do X sáng tạo").
- Lựa chọn **có ý nghĩa**: hạch tâm, nguyên tố, biến thức ghép lại cho ra lối chơi khác nhau; ngộ tính cao thì chiêu tốt hơn.
- **Không phá cân bằng:** mọi chiêu chế tạo nằm trong ngân sách sức mạnh theo phẩm (GD-07 SK-R-130); server tự "áp chế" khi vượt.
- **Không cần code / VFX riêng cho từng chiêu:** ghép từ thành phần dữ liệu và VFX tham số.
- Ngọc giản tự chế là **di sản và hàng hóa**: cất Tàng Bảo Các cho kiếp sau, hoặc bán (Beta).

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="tf-r-001"></a>**TF-R-001** | Client chỉ gửi **lựa chọn** (ID mảnh, loại sát thương, tên). Toàn bộ việc ghép, roll, áp chế ngân sách do **API Service** thực hiện, ghi `rng_audit`. |
| <a id="tf-r-002"></a>**TF-R-002** | Mọi kỹ năng chế tạo có `powerScore ≤ budget[tier]` (GD-07 §6.5) **sau khi roll** (§5.3 bước 6). Không có ngoại lệ, kể cả đột biến. |
| <a id="tf-r-003"></a>**TF-R-003** | Thành phần chỉ biến đổi `SkillDef` qua **phép vá dữ liệu** thuộc danh sách trắng (§3 `PatchOp`); không thành phần nào cần code riêng. |
| <a id="tf-r-004"></a>**TF-R-004** | Định nghĩa kỹ năng chế tạo (`CSK_*`) là **bất biến** sau khi tạo (trừ đổi tên do GM) và tồn tại vĩnh viễn trên server (người khác có thể đang giữ ngọc giản). |
| <a id="tf-r-005"></a>**TF-R-005** | Tên công pháp qua bộ lọc GD-14 (SO-R-021), báo cáo được; GM đổi tên vi phạm (D-15). |

---

## 2. Tổng quan

```
 Mảnh ngọc giản (rơi từ quái / bí cảnh / hiểm địa / cơ duyên)
   HẠCH TÂM ×1 (bắt buộc)   NGUYÊN TỐ ×0–1   BIẾN THỨC ×0–3 (theo phẩm)
          │                       │                   │
          ▼ tại Ngộ Đạo Thạch (Tàng Kinh Các Thanh Vân Tông, POI INSIGHT_STATION)
   [Diễn Pháp Tràng: thử miễn phí bản xem trước trên hình nhân]
          ▼
   GHÉP (tất định): hạch tâm → loại sát thương → nguyên tố → biến thức (thứ tự cố định)
   ROLL "ngộ đạo" q ∈ [0,1] (ngộ tính, khí vận) → hệ số, hồi chiêu, phẩm phụ Hạ / Trung / Thượng
   [ĐỘT BIẾN: thêm 1 biến thức hiếm]
   ÁP CHẾ NGÂN SÁCH: vượt budget[tier] → thu nhỏ hệ số cho vừa
          ▼
   Kỹ năng CSK_* (SkillDef) + 1 NGỌC GIẢN TỰ CHẾ (vật phẩm) mang tên người đặt
          → học (GD-07) · cất Tàng Bảo Các · (Beta) sao chép, bán · tán giản
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type ComponentKind = 'CORE' | 'ELEMENT' | 'MODIFIER';
type TechniqueTier = GradeTier;                         // HOANG | HUYEN | DIA | THIEN (GD-07)

interface FragmentSpec {                                 // ItemTemplate.fragment, category TECHNIQUE_FRAGMENT (GD-09)
  kind: ComponentKind; componentId: string; tier: TechniqueTier;
}
// Mẫu vật phẩm mảnh sinh tự động: ITM_FRAG_{componentId}_{TIER}, vd. ITM_FRAG_CORE_BOLT_HUYEN

interface CoreDef {
  id: string;                                            // CORE_*
  nameKey: string; vfxTemplateId: string;
  base: Omit<SkillDef, 'id' | 'nameKey' | 'descKey' | 'grade' | 'realmMin' | 'perfection'>;
  allowedDamageTypes: ('PHYSICAL' | 'SPELL')[];          // người chơi chọn một khi chế tạo
  shapeTags: string[];                                   // vd. "projectile", "area", "melee", "support"
}

interface ElementDef {
  id: string;                                            // ELEM_KIM ... ELEM_PHONG
  element: Element; statusId: string;                    // hiệu ứng hệ (GD-06 §5.12)
  baseStatusChance: number;                              // 0,3
  paletteId: string;                                     // bảng màu VFX
}

interface ModifierDef {
  id: string;                                            // MOD_*
  nameKey: string;
  rare: boolean;                                         // chỉ xuất hiện qua đột biến
  requires?: { shapeTypes?: Shape['type'][]; needsElement?: boolean; needsPersist?: boolean };
  excludes?: string[];                                   // không dùng chung với các biến thức này
  priority: number;                                      // thứ tự áp dụng (nhỏ trước)
  patches: PatchOp[];
  vfxOverlayId?: string;
}

type PatchOp =                                           // danh sách trắng (TF-R-003)
  | { op: 'MUL'; path: PatchPath; value: number }
  | { op: 'ADD'; path: PatchPath; value: number }
  | { op: 'SET'; path: PatchPath; value: number | string | boolean }
  | { op: 'REACTION_UP'; levels: number }                 // FLINCH → STAGGER → KNOCKBACK → KNOCKDOWN (trần)
  | { op: 'APPEND_HIT_ECHO'; delaySec: number; coefficientPct: number }   // nhân bản lượt trúng đầu
  | { op: 'APPEND_STATUS'; statusId: string | '$ELEMENT' | '$SECOND_ELEMENT'; chance: number }
  | { op: 'APPEND_SELF_SHIELD'; pctMaxHp: number; durationSec: number };
type PatchPath =                                          // trường số được phép vá
  | 'shape.speed' | 'shape.range' | 'shape.radius' | 'shape.angleDeg' | 'shape.maxRange' | 'shape.distance'
  | 'shape.pierce' | 'shape.count' | 'shape.spreadDeg' | 'shape.explodeRadius' | 'shape.persistSec' | 'shape.durationSec'
  | 'hits[*].damage.coefficient' | 'hits[*].heal.coefficient' | 'hits[*].poiseDamage' | 'hits[*].maxTargets'
  | 'hits[*].ticks.count' | 'hits[*].statuses[*].chance'
  | 'cooldownSec' | 'windupSec' | 'costs.mpPctOfBase' | 'armorDuringCast';

interface CraftedTechnique {                             // định nghĩa vĩnh viễn (TF-R-004)
  id: string;                                            // CSK_{base32 10 ký tự}
  name: string; nameStatus: 'OK' | 'GM_RENAMED';
  creatorAccountId: string; creatorCharacterId: string; creatorNameSnapshot: string;
  tier: TechniqueTier; sub: SubGrade;
  coreId: string; elementId: string | null; modifierIds: string[]; mutationModifierId: string | null;
  damageType: 'PHYSICAL' | 'SPELL';
  q: number;                                             // điểm ngộ đạo
  suppressFactor: number;                                // hệ số áp chế (1 = không áp chế)
  skillDef: SkillDef;                                    // kết quả cuối, engine dùng trực tiếp
  powerScore: number;
  copiesMade: number;                                    // gồm bản gốc
  createdAt: string;
}
```

`ItemInstance` (GD-09) bổ sung `craftedRefId: string | null` (ngọc giản tự chế trỏ tới `CSK_*`). Mẫu vật phẩm chung `ITM_SCROLL_CRAFTED_{TIER}` (category `TECHNIQUE_SCROLL`, `teaches` lấy từ `craftedRefId`).

---

## 4. Máy trạng thái
- Chế tạo là **thao tác tức thì** (một request, một transaction): xem trước → xác nhận → kết quả. Không có phiên kéo dài, không minigame.
- `CraftedTechnique`: tạo ra → tồn tại vĩnh viễn; trường `copiesMade` chỉ tăng; `nameStatus` đổi khi GM đổi tên.

---

## 5. Quy tắc

### 5.1. Nơi chế tạo và điều kiện
- <a id="tf-r-010"></a>**TF-R-010** Chế tạo tại POI `INSIGHT_STATION` (Ngộ Đạo Thạch ở Tàng Kinh Các, Thanh Vân Tông, `HUB`), đứng trong 3m, không chiến đấu, bậc Thanh Vân Tông ≥ `NGOAI_MON` (GD-14).
- <a id="tf-r-011"></a>**TF-R-011** Cảnh giới tối thiểu theo **phẩm** của công pháp tạo ra:

| Phẩm | Cảnh giới để chế tạo | `realmMin` của kỹ năng tạo ra (để học) | Số ô biến thức |
|------|:---:|:---:|:---:|
| Hoàng | `TC1` | `LK1` | 1 |
| Huyền | `TC1` | `LK5` | 2 |
| Địa | `KD1` | `TC1` | 3 |
| Thiên | `NA1` | `KD1` | 3 |

  (Sửa đổi GD-04 §5.1: chế công pháp mở từ **Trúc Cơ** với phẩm Hoàng – Huyền, thay vì Kim Đan.)
- <a id="tf-r-012"></a>**TF-R-012** **Phẩm** của công pháp = phẩm **thấp nhất** trong các mảnh đem dùng (hạch tâm, nguyên tố, biến thức). Muốn công pháp Huyền phẩm thì mọi mảnh phải từ Huyền trở lên.

### 5.2. Chọn thành phần
- <a id="tf-r-020"></a>**TF-R-020** Bắt buộc 1 mảnh **hạch tâm**; tùy chọn 1 mảnh **nguyên tố**; tùy chọn tới N mảnh **biến thức** (N theo phẩm, TF-R-011), không trùng biến thức, thỏa `requires` / `excludes`. Biến thức `rare` không chọn được (chỉ qua đột biến).
- <a id="tf-r-021"></a>**TF-R-021** Chọn **loại sát thương** trong `allowedDamageTypes` của hạch tâm (vd. Phi Xạ cho phép cả `PHYSICAL` lẫn `SPELL`: kiếm thủ chọn võ kỹ, người dùng quạt chọn pháp thuật). Không có nguyên tố → `element = INHERIT` (hệ tâm pháp chính, GD-07 SK-R-030).
- <a id="tf-r-022"></a>**TF-R-022** **Xem trước** (miễn phí, không tiêu gì) trả về: `SkillDef` ở `q = 0,5`, khoảng giá trị khi `q` từ 0 tới 1, `powerScore` nhỏ nhất / lớn nhất, ngân sách, có bị áp chế không, tỷ lệ đột biến, phí, thọ nguyên. Tổ hợp mà ngay cả `q = 0` cũng cần áp chế `suppressFactor < params.minSuppress` (0,5) bị đánh dấu **"Thiên đạo bất dung"**: không chế tạo được (§5.3 bước 6).

### 5.3. Thuật toán ghép (tất định trừ bước 4–5)
- <a id="tf-r-030"></a>**TF-R-030** Server thực hiện đúng thứ tự:
  1. Sao chép `CoreDef.base` thành `SkillDef` nháp; `weaponReq = ANY`; `tags = ["crafted", ...shapeTags]`; `realmMin` theo TF-R-011; `perfection = { type: COEFFICIENT_PCT, value: 0,15 }` (§5.7).
  2. Gán `damageType` đã chọn cho mọi `HitDef` có `damage`.
  3. **Nguyên tố:** gán `element` cho mọi `damage`; thêm `statuses += { statusId: ElementDef.statusId, chance: baseStatusChance }` vào lượt trúng đầu. Hạch tâm hỗ trợ (`heal`, `shield`) chỉ đổi bảng màu, không thêm status.
  4. **Biến thức** (gồm đột biến nếu có, xem bước 5): sắp theo `priority` tăng dần (bằng nhau thì theo `id`), áp từng `PatchOp`. Đường dẫn không tồn tại trên hình dạng hiện tại → bỏ qua phép đó (validator đã chặn biến thức không tương thích, `requires`).
  5. **Ngộ đạo:** roll `q` (§6.2); nhân mọi `damage.coefficient`, `heal.coefficient`, `shield.pctMaxHp` với `(0,9 + 0,2 × q)`; nhân `cooldownSec` với `(1,1 − 0,2 × q)`; phẩm phụ: `q < 0,4` Hạ · `< 0,8` Trung · còn lại Thượng. **Đột biến** roll trước bước 4 (để biến thức hiếm được áp cùng thứ tự).
  6. **Áp chế ngân sách:** tính `powerScore` (GD-07 §6.5 đã bổ sung §6.3); nếu > `budget[tier]` thì nhân mọi hệ số (damage / heal / shield) với `suppressFactor` (§6.3) sao cho bằng ngân sách. `suppressFactor < 0,5` → **hủy chế tạo**, hoàn toàn bộ chi phí (trường hợp này đã được xem trước cảnh báo, chỉ xảy ra nếu dữ liệu đổi giữa hai lần gọi).
  7. Làm tròn: hệ số 3 chữ số, thời gian 2 chữ số, khoảng cách 1 chữ số. Lưu `CraftedTechnique`.
- <a id="tf-r-031"></a>**TF-R-031** Validator CI chạy bước 1–4, 6 với `q ∈ {0; 0,5; 1}` cho **mọi tổ hợp hợp lệ** (hạch tâm × nguyên tố × tập biến thức tới 3) để phát hiện tổ hợp "Thiên đạo bất dung" hoặc vi phạm giới hạn cứng (hồi chiêu < 0,5s, số đạn > 7, bán kính > 8m).

### 5.4. Chi phí
- <a id="tf-r-040"></a>**TF-R-040** Mọi mảnh đã chọn bị tiêu (`CRAFT_INPUT`); phí linh thạch `round(0,05 × scrollValue[tier])` (`CRAFT_FEE`, GD-13); thọ nguyên theo phẩm: Hoàng 0 · Huyền 1 · Địa 2 · Thiên 3 năm (GD-26 `CRAFTING`, xác nhận LC-R-042).
- <a id="tf-r-041"></a>**TF-R-041** Không có thất bại: luôn ra công pháp (chất lượng theo `q`). Rủi ro nằm ở chất lượng, không ở mất trắng.

### 5.5. Kết quả: ngọc giản tự chế
- <a id="tf-r-050"></a>**TF-R-050** Kết quả là **1 ngọc giản** `ITM_SCROLL_CRAFTED_{TIER}` với `craftedRefId = CSK_*` vào `BAG` (`CRAFT_OUTPUT`, `bind = NONE`). Người tạo **không** tự động biết kỹ năng; phải dùng ngọc giản để học (GD-07 SK-R-044) như mọi ngọc giản khác.
- <a id="tf-r-051"></a>**TF-R-051** Ngọc giản tự chế có thể cất Tàng Bảo Các (GD-26): **kiếp sau học lại công pháp của chính mình**; hoặc tặng / bán khi giao dịch mở (Beta, GD-13).
- <a id="tf-r-052"></a>**TF-R-052** Kỹ năng `CSK_*` đã học hiển thị tên + "do {creatorNameSnapshot} sáng tạo"; thuần thục, ô kỹ năng, quy trình thi triển theo GD-07 (`gradeFactor` theo phẩm, `skillTag` gồm `crafted` và tag hình dạng).

### 5.6. Sao chép và tán giản (Beta)
- <a id="tf-r-060"></a>**TF-R-060** **Sao chép:** chỉ **nhân vật đã tạo ra** công pháp, khi còn `ALIVE`, tại Ngộ Đạo Thạch: tiêu 1 **Ngọc Giản Trống** (`ITM_BLANK_JADE`, NPC bán) + phí `round(0,1 × scrollValue[tier])` → 1 ngọc giản mới. Tổng số bản (gồm bản gốc) ≤ `params.maxCopies` (20). Người tạo hết kiếp thì không ai sao chép được nữa: số bản đang lưu hành trở thành **cố định** (độ hiếm tự nhiên).
- <a id="tf-r-061"></a>**TF-R-061** **Tán giản:** tiêu 1 ngọc giản tự chế → nhận lại **1 mảnh hạch tâm** cùng phẩm (`DISMANTLE`). Không trả lại nguyên tố / biến thức.

### 5.7. Hiệu ứng Viên Mãn và VFX tham số
- <a id="tf-r-070"></a>**TF-R-070** Kỹ năng chế tạo có Viên Mãn chung `COEFFICIENT_PCT +15%` (không tính vào `powerScore`, giống kỹ năng tác giả viết, GD-07 §6.5).
- <a id="tf-r-071"></a>**TF-R-071** **VFX ghép tham số:** `vfxTemplateId` của hạch tâm (8 mẫu) × `paletteId` của nguyên tố (8 bảng màu; `INHERIT` dùng bảng màu theo hệ tâm pháp khi thi triển) × `vfxOverlayId` của biến thức (vệt sáng, nổ, nhân bản). Không làm VFX riêng cho từng công pháp.

### 5.8. Nguồn mảnh ngọc giản
- <a id="tf-r-080"></a>**TF-R-080**

| Phẩm mảnh | Nguồn | Bảng rơi đồ |
|-----------|-------|-------------|
| Hoàng | Tinh anh Vực 1 (GD-12), Tông Môn Bảo Khố (hạch tâm thường, bậc `NOI_MON`), rương hiểm địa Nhất giai | `LT_V1_TECH_FRAGMENT_HOANG` |
| Huyền | `V1_HD02` Cổ Chiến Trường (GD-28 DZ-R-092), boss BC03, cơ duyên phẩm HUYEN+ (vd. `ENC_V1_NGU_HANH_TRAN`) | `LT_V1_TECH_FRAGMENT_HUYEN` |
| Địa | Hiểm địa Tam giai, boss Vực 2 (Beta) | `LT_V2_TECH_FRAGMENT_DIA` |
| Thiên | Cơ duyên phẩm THIEN, sự kiện thế giới lớn (sau launch) | — |

- <a id="tf-r-081"></a>**TF-R-081** Trong một bảng rơi mảnh, tỷ lệ loại: hạch tâm 30% · nguyên tố 30% · biến thức 40%; trong mỗi loại chia đều các thành phần (trừ biến thức `rare`: không bao giờ rơi).

---

## 6. Công thức

### 6.1. Phẩm
```
tier = min(tier của mọi mảnh đã chọn)            (thứ tự HOANG < HUYEN < DIA < THIEN)
```

### 6.2. Điểm ngộ đạo và đột biến
```
q = clamp( U(0, 1) + (comprehension − 50) × 0,005 + (luck − 50) × 0,002 + Σ CRAFT_QUALITY_PCT[TECHNIQUE] + insightItemBonus, 0, 1 )
mutationChance = clamp( 0,05 + (luck − 50) × 0,001 + (comprehension − 50) × 0,001, 0, 0,12 )
insightItemBonus: Ngộ Đạo Trà (đan dược, GD-10, Beta) +0,10
CRAFT_QUALITY_PCT[TECHNIQUE]: hiệu ứng thiên phú GD-27 (cộng thẳng vào q; hiện chưa có thiên phú nào dùng, dành cho mở rộng)
```
Ví dụ: ngộ tính 80, khí vận 60, roll U = 0,50 → `q = 0,50 + 0,15 + 0,02 = 0,67` → phẩm phụ Trung; đột biến `0,05 + 0,01 + 0,03 = 9%`.

### 6.3. Điểm sức mạnh (bổ sung GD-07 §6.5) và áp chế
```
Bổ sung vào damageValue của GD-07 §6.5:
  projectileFactor = 1 + 0,5 × (shape.count − 1)                      (PROJECTILE có count; mặc định count = 1)
  targetFactor khi maxTargets > 1 mà không phải vùng: min(1 + 0,3 × (maxTargets − 1), 2,0)
  shieldValue như GD-07 (pctMaxHp × 10)
Áp chế:
  nếu powerScore > budget[tier]:
    scalable    = phần damageValue + healValue + shieldValue
    nonScalable = ccValue + statusValue + mobility
    suppressFactor = (budget × (cooldownSec + 1) − nonScalable) / scalable
    nhân mọi hệ số damage / heal / shield với suppressFactor
```

### 6.4. Ví dụ đầy đủ
Huyền phẩm, hạch tâm **Phi Xạ** (`CORE_BOLT`: đạn 18 m/s, 11m, r 0,5, hệ số 1,6, hồi 5s, STAGGER), chọn `SPELL`, nguyên tố **Hỏa** (`ST_BURN` 30%), biến thức **Xuyên Thấu** (`pierce +1`, `maxTargets +1`) + **Bạo Liệt** (`explodeRadius = 2`), `q = 1,0` (ngộ tính rất cao):
- Bước 5: hệ số `1,6 × 1,1 = 1,76`; hồi chiêu `5 × 0,9 = 4,5`s.
- `powerScore`: vùng nổ r 2 ≤ 3 → `targetFactor` 1,5 → `1,76 × 1,5 = 2,64`; STAGGER 0,1; status `0,3 × 0,3 = 0,09` → `2,83 / 5,5 = 0,515` ≤ 0,55 (Huyền) → **không áp chế**; phẩm phụ Thượng.
- Cùng tổ hợp thêm **Cường Hóa** (×1,25 hệ số, chỉ có ở phẩm Địa vì Huyền chỉ có 2 ô): Địa phẩm, `q = 1`: `2,2 × 1,5 = 3,3 + 0,19 = 3,49 / 5,5 = 0,635` ≤ 0,68 → không áp chế.
- Phí: `round(0,05 × 2.000) = 100` linh thạch; thọ nguyên 1 năm.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/technique_forge/params.json` | Tham số |
| `shared/gamedata/technique_forge/cores.json` | `CoreDef[]` |
| `shared/gamedata/technique_forge/elements.json` | `ElementDef[]` |
| `shared/gamedata/technique_forge/modifiers.json` | `ModifierDef[]` |
| `shared/gamedata/items/templates/fragments_*.json` | Sinh tự động bởi `tools/gen-fragments` từ 3 file trên × 4 phẩm |

```jsonc
// params.json
{
  "stationRadius": 3, "npcSectRankMin": "NGOAI_MON",
  "tierCraftRealm": { "HOANG": "TC1", "HUYEN": "TC1", "DIA": "KD1", "THIEN": "NA1" },
  "tierLearnRealm": { "HOANG": "LK1", "HUYEN": "LK5", "DIA": "TC1", "THIEN": "KD1" },
  "modifierSlots": { "HOANG": 1, "HUYEN": 2, "DIA": 3, "THIEN": 3 },
  "qComprehensionFactor": 0.005, "qLuckFactor": 0.002,
  "coefRange": [0.9, 1.1], "cooldownRange": [1.1, 0.9], "subThresholds": { "TRUNG": 0.4, "THUONG": 0.8 },
  "mutationBase": 0.05, "mutationLuckFactor": 0.001, "mutationComprehensionFactor": 0.001, "mutationMax": 0.12,
  "minSuppress": 0.5,
  "hardLimits": { "minCooldownSec": 0.5, "maxProjectiles": 7, "maxRadius": 8 },
  "scrollValue": { "HOANG": 500, "HUYEN": 2000, "DIA": 8000, "THIEN": 30000 },
  "craftFeePct": 0.05, "copyFeePct": 0.10, "maxCopies": 20,
  "lifespanYears": { "HOANG": 0, "HUYEN": 1, "DIA": 2, "THIEN": 3 },
  "perfection": { "type": "COEFFICIENT_PCT", "value": 0.15 },
  "fragmentKindWeights": { "CORE": 30, "ELEMENT": 30, "MODIFIER": 40 },
  "sandbox": { "dailyRuns": 5, "durationSec": 60 },
  "nameMin": 2, "nameMax": 10
}
```

### 7.1. Hạch tâm (8)
Giá trị gốc ở mức Hoàng (trước `gradeCoefMult`, GD-07 SK-R-022). Tất cả nằm trong ngân sách Hoàng ở `q = 0,5`.

| ID | Tên | Hình dạng | Đòn | Hệ số | MP | CD | Phản ứng | Loại sát thương |
|----|-----|-----------|-----|:---:|:---:|:---:|----------|:---:|
| `CORE_BOLT` | Phi Xạ | Đạn 18 m/s, 11m, r 0,5 | 1 lượt | 1,6 | 12 | 5s | STAGGER, poise 20 | PHYS / SPELL |
| `CORE_SWEEP` | Hoành Tảo | Quạt 3m / 120° | 1 lượt | 1,8 | 12 | 6s | KNOCKBACK 1,5m, poise 30 | PHYS / SPELL |
| `CORE_SMITE` | Lạc Địa | Vùng r 2,5, tầm 10, trễ 0,7s | 1 lượt | 2,4 | 18 | 9s | FLINCH, poise 25 | SPELL / PHYS |
| `CORE_DASH` | Thuấn Trảm | Lướt 6m × 1,2m, i-frame | 1 lượt | 1,3 | 12 | 9s | STAGGER | PHYS |
| `CORE_NOVA` | Hộ Thể Bạo | Quanh thân r 3 | 1 lượt | 1,0 | 15 | 10s | KNOCKBACK 2m | PHYS / SPELL |
| `CORE_FAMILIAR` | Ngự Vật | Triệu hồi 8s, tấn công mỗi 1s | — | 0,45 / đòn | 25 | 20s | FLINCH | PHYS / SPELL |
| `CORE_BLOOM` | Hồi Xuân | Vùng r 4, tồn tại 4s, tick 1s, đồng minh | Hồi | 0,3 / tick | 28 | 18s | — | (hồi máu) |
| `CORE_AEGIS` | Kim Thân | Bản thân | Khiên 12% `maxHp` 5s | — | 15 | 18s | — | (khiên) |

### 7.2. Nguyên tố (8)
`ELEM_KIM` (`ST_ARMOR_BREAK`) · `ELEM_MOC` (`ST_ENTANGLE`) · `ELEM_THUY` (`ST_FROST`) · `ELEM_HOA` (`ST_BURN`) · `ELEM_THO` (`ST_HEAVY`) · `ELEM_LOI` (`ST_SHOCK`) · `ELEM_BANG` (`ST_FROST`) · `ELEM_PHONG` (`ST_WIND_CUT`); `baseStatusChance` 0,3; mảnh Lôi / Băng / Phong chỉ từ phẩm Huyền trở lên.

### 7.3. Biến thức
| ID | Tên | Yêu cầu | Phép vá | Hiếm |
|----|-----|---------|---------|:---:|
| `MOD_SPLIT` | Phân Hóa | `PROJECTILE` | `shape.count +2`, `shape.spreadDeg = 20`, hệ số ×0,7 | |
| `MOD_PIERCE` | Xuyên Thấu | `PROJECTILE` | `shape.pierce +1`, `maxTargets +1` | |
| `MOD_BURST` | Bạo Liệt | `PROJECTILE` | `shape.explodeRadius = 2` (dùng chung được với `MOD_PIERCE`, xem ví dụ §6.4) | |
| `MOD_WIDEN` | Khoáng Đại | `CONE`, `GROUND_CIRCLE`, `SELF_CIRCLE` | `radius ×1,3`, `angleDeg ×1,3` | |
| `MOD_REACH` | Viễn Trình | `PROJECTILE`, `GROUND_CIRCLE`, `DASH` | `range ×1,4`, `maxRange ×1,4`, `distance ×1,3` | |
| `MOD_SWIFT` | Tốc Phát | — | `cooldownSec ×0,8`, hệ số ×0,9 | |
| `MOD_EMPOWER` | Cường Hóa | — | hệ số ×1,25, `mpPctOfBase ×1,3` | |
| `MOD_ECHO` | Liên Kích | có `damage` | `APPEND_HIT_ECHO` trễ 0,5s, 50% hệ số | |
| `MOD_SURGE` | Nguyên Tố Thịnh | có nguyên tố | `APPEND_STATUS $ELEMENT +0,3` (cộng vào xác suất) | |
| `MOD_CONCUSS` | Chấn Động | có `damage` | `REACTION_UP 1`, `poiseDamage ×1,5` | |
| `MOD_LINGER` | Trì Lưu | `GROUND_CIRCLE` có `persistSec` | `persistSec +2`, `ticks.count +2` | |
| `MOD_WARD` | Hộ Thuẫn | — | `APPEND_SELF_SHIELD` 8% `maxHp` 4s | |
| `MOD_THRIFT` | Tiết Linh | — | `mpPctOfBase ×0,6` | |
| `MOD_STEADFAST` | Bất Khuất | — | `armorDuringCast = HEAVY`, `cooldownSec +1` | |
| `MOD_QUICKCAST` | Thuấn Phát | — | `windupSec ×0,6`, `cooldownSec +1` | |
| `MOD_TWIN_ELEMENT` | Song Hành | có nguyên tố | `APPEND_STATUS $SECOND_ELEMENT 0,3` (hệ khắc chế của nguyên tố chính) | ✅ |
| `MOD_GREAT_SPLIT` | Vạn Hóa | `PROJECTILE` | `shape.count +4`, `spreadDeg = 40`, hệ số ×0,6 | ✅ |
| `MOD_VOID_COST` | Hư Không | — | `mpPctOfBase = 0`, `cooldownSec ×1,2` | ✅ |

```jsonc
// modifiers.json (trích)
{ "id": "MOD_SPLIT", "nameKey": "mod.split", "rare": false, "priority": 20,
  "requires": { "shapeTypes": ["PROJECTILE"] }, "excludes": ["MOD_GREAT_SPLIT"],
  "patches": [ { "op": "ADD", "path": "shape.count", "value": 2 },
               { "op": "SET", "path": "shape.spreadDeg", "value": 20 },
               { "op": "MUL", "path": "hits[*].damage.coefficient", "value": 0.7 } ],
  "vfxOverlayId": "VFXO_MULTI" }
```

**Validator (CI):** mọi `PatchOp.path` thuộc `PatchPath`, `op` thuộc danh sách trắng; `statusId` tồn tại (GD-06); mọi `CoreDef.base` đạt `powerScore ≤ budget.HOANG` ở `q = 0,5`; TF-R-031 (duyệt mọi tổ hợp); biến thức `rare` không có trong bảng rơi đồ; mẫu mảnh sinh đủ cho mọi thành phần × phẩm hợp lệ.

---

## 8. Mục tiêu cân bằng

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Tỷ lệ công pháp bị áp chế | 20–40% (đủ để ngộ tính cao có ý nghĩa nhưng không phá trần) |
| DPS công pháp chế tạo tốt nhất so với kỹ năng tác giả viết cùng phẩm | ±10% (chế tạo linh hoạt, tác giả viết có Viên Mãn độc đáo) |
| Tỷ lệ người chơi Trúc Cơ có ít nhất 1 công pháp tự chế trong ô | 40–60% |
| Số tổ hợp "Thiên đạo bất dung" | 0 (validator) |

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/technique-forge/catalog` | Danh mục hạch tâm, nguyên tố, biến thức (tên, mô tả, yêu cầu) | |
| `POST /v1/characters/{id}/technique-forge/preview` | `{ coreFragmentTemplateId, elementFragmentTemplateId?, modifierFragmentTemplateIds[], damageType }` → `{ tier, valid, reasonIfInvalid, skillDefAtHalf, ranges, powerScoreRange, budget, suppressRange, mutationChance, fee, lifespanYears }` | TF-R-022 |
| `POST /v1/characters/{id}/technique-forge/craft` | Như preview + `{ name, confirmLifespan, requestId }` → `{ craftedTechniqueId, scrollItemInstanceId, q, sub, mutationModifierId, suppressFactor }` | §5.3–5.5; idempotent theo `requestId` |
| `POST /v1/characters/{id}/technique-forge/sandbox` | `{ preview params }` → vào Diễn Pháp Tràng 60s với kỹ năng ở `q = 0,5` | TF-R-022; 5 lần / ngày |
| `POST /v1/characters/{id}/technique-forge/copy` | `{ craftedTechniqueId, requestId }` (Beta) | TF-R-060 |
| `POST /v1/characters/{id}/technique-forge/dismantle` | `{ scrollItemInstanceId }` (Beta) | TF-R-061 |
| `GET /v1/crafted-techniques/{id}` | Thông tin công khai (tên, người tạo, phẩm, chỉ số) | Tooltip ngọc giản |
| `POST /v1/reports` (GD-14) | Báo cáo tên công pháp | TF-R-005 |

### 9.2. API nội bộ
| Endpoint | Mục đích |
|----------|----------|
| `GET /internal/v1/crafted-skills/{csk}` | Game Server tải `SkillDef` của `CSK_*` khi nhân vật vào bản đồ (cache, bất biến) |
| `POST /admin/v1/crafted-techniques/{id}/rename` | GM đổi tên (`nameStatus = GM_RENAMED`) |

### 9.3. Sự kiện domain
| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `technique.crafted` | `{ characterId, craftedTechniqueId, tier, sub, mutated }` | Thông báo toàn server (phẩm Thiên hoặc Địa có đột biến: ưu tiên 2, GD-14), Bia Vạn Cổ, telemetry |

---

## 10. Lưu trữ

```sql
CREATE TABLE crafted_technique (                   -- thay bảng sơ bộ ở PLAN §5.4
  id VARCHAR(16) PRIMARY KEY,                      -- CSK_xxxxxxxxxx
  name VARCHAR(32) NOT NULL, name_status VARCHAR(16) NOT NULL DEFAULT 'OK',
  creator_account_id UUID NOT NULL, creator_character_id UUID NOT NULL, creator_name_snapshot VARCHAR(24) NOT NULL,
  tier VARCHAR(8) NOT NULL, sub VARCHAR(8) NOT NULL,
  core_id VARCHAR(32) NOT NULL, element_id VARCHAR(32), modifier_ids JSONB NOT NULL, mutation_modifier_id VARCHAR(32),
  damage_type VARCHAR(8) NOT NULL, q NUMERIC(5,4) NOT NULL, suppress_factor NUMERIC(5,4) NOT NULL,
  skill_def JSONB NOT NULL, power_score NUMERIC(6,4) NOT NULL,
  copies_made INT NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_crafted_creator ON crafted_technique(creator_character_id);

ALTER TABLE item_instance ADD COLUMN crafted_ref_id VARCHAR(16);   -- GD-09: ngọc giản tự chế

CREATE TABLE technique_forge_sandbox_use (
  character_id UUID NOT NULL, day DATE NOT NULL, runs SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, day)
);
```
`rng_audit.kind` mới: `TECHNIQUE_Q`, `TECHNIQUE_MUTATION`, `TECHNIQUE_MUTATION_PICK`. `character_technique.ref_id` (GD-07) chứa `CSK_*` với `source = CRAFTED`.

---

## 11. UI/UX
- **Ngộ Đạo Thạch:** ba khay (Hạch tâm · Nguyên tố · Biến thức, ô khóa theo phẩm) kéo mảnh từ túi; khung giữa hiển thị **hình dạng chiêu trên mặt đất** và VFX xem trước; bên phải: chỉ số ở `q` 0 / 0,5 / 1, thanh ngân sách (xanh → vàng khi có thể bị áp chế → đỏ "Thiên đạo bất dung"), tỷ lệ đột biến, phí, thọ nguyên; ô đặt tên.
- **Diễn Pháp Tràng:** phòng có hình nhân, đồng hồ 60s, bảng DPS / số mục tiêu trúng.
- **Khoảnh khắc ngộ đạo:** cutscene ngắn (nhân vật tĩnh tọa, chữ cổ bay quanh), thanh `q` chạy và dừng; đột biến có tiếng sấm và chữ "Đốn ngộ!".
- **Ngọc giản tự chế (tooltip):** tên do người chơi đặt, "do X sáng tạo", phẩm + phẩm phụ, hình dạng, chỉ số, số bản lưu hành / tối đa.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-07 | Sinh `SkillDef`; ngân sách §6.5 (đã bổ sung); học, thuần thục, ô kỹ năng, thi triển |
| GD-06 | Status nguyên tố, phản ứng, khiên |
| GD-09 | Mảnh (`TECHNIQUE_FRAGMENT`), ngọc giản tự chế (`craftedRefId`), `CRAFT_INPUT` / `CRAFT_OUTPUT` / `DISMANTLE` |
| GD-27 | Ngộ tính, khí vận |
| GD-26 | Tiêu thọ; ngọc giản trong Tàng Bảo Các cho kiếp sau |
| GD-04 | Mở khóa chế công pháp từ Trúc Cơ (sửa đổi §5.1) |
| GD-12 / GD-22 / GD-28 | Nguồn mảnh |
| GD-13 | Giá trị ngọc giản, phí, hàng hóa (Beta) |
| GD-14 | Lọc / báo cáo tên; thông báo toàn server; điều kiện bậc Thanh Vân Tông |
| GD-10 | Ngộ Đạo Trà (Beta) |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Dữ liệu thành phần đổi giữa xem trước và chế tạo | Chế tạo dùng dữ liệu hiện tại; nếu kết quả "Thiên đạo bất dung" → hủy, hoàn chi phí (TF-R-030 bước 6) |
| Cân bằng lại thành phần sau khi đã có công pháp | Công pháp đã tạo **không đổi** (TF-R-004); chỉ ảnh hưởng chế tạo mới. Trường hợp khẩn cấp (lỗi nghiêm trọng): GM "phong ấn" một `CSK_*` (tạm vô hiệu) qua ADR |
| Tên trùng công pháp khác | Cho phép (định danh bằng `CSK_*` + người tạo) |
| Người tạo đổi tên nhân vật / hết kiếp | Hiển thị `creatorNameSnapshot` lúc tạo |
| Học ngọc giản tự chế đã biết | `409 ALREADY_KNOWN`, không tiêu (GD-07) |
| Sao chép khi đã đạt 20 bản | Từ chối |
| Mảnh nguyên tố Lôi phẩm Hoàng | Không tồn tại (validator, §7.2) |
| Biến thức đột biến không tương thích với hình dạng | Rút lại trong tập biến thức hiếm tương thích; không có → không đột biến |

---

## 14. Telemetry

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `technique_crafted` | tier, core, element, modifiers, q, sub, mutated, suppressFactor | Tổ hợp phổ biến, tỷ lệ áp chế, giá trị ngộ tính |
| `technique_preview` | số lần xem trước / chế tạo | Người chơi thử nghiệm nhiều không |
| `crafted_skill_usage` | cskId, casts, damageShare | Công pháp tự chế có được dùng thật trong chiến đấu |
| `crafted_vs_authored_dps` | tier, dps | So với §8 |
| `scroll_copy` / `scroll_vault` | | Ngọc giản có thành di sản / hàng hóa |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="tf-ac-001"></a>TF-AC-001 | Ví dụ §6.4 (Huyền, `q = 1`) | Chế tạo | Hệ số 1,76, hồi chiêu 4,5s, `powerScore ≈ 0,515`, không áp chế, phẩm phụ Thượng, phí 100, thọ −1 năm |
| TF-AC-002 | Ví dụ §6.2 | Tính | `q = 0,67` → Trung; đột biến 9% |
| TF-AC-003 | Mảnh hạch tâm Huyền + biến thức Hoàng | Xem trước | `tier = HOANG`, chỉ 1 ô biến thức |
| TF-AC-004 | Nhân vật `LK9` | Chế tạo phẩm Hoàng | Từ chối (`TC1`) |
| TF-AC-005 | Chọn 3 biến thức cho phẩm Huyền | Xem trước | Từ chối (tối đa 2) |
| TF-AC-006 | Chọn `MOD_PIERCE` với `CORE_SWEEP` | Xem trước | Từ chối (`requires` PROJECTILE) |
| TF-AC-007 | Chọn biến thức `rare` trực tiếp | API | Từ chối |
| TF-AC-008 | Tổ hợp vượt ngân sách sau roll | Chế tạo | `powerScore` cuối = ngân sách (± làm tròn); `suppressFactor < 1` được lưu |
| TF-AC-009 | Mọi tổ hợp hợp lệ trong dữ liệu | Validator TF-R-031 | Không có tổ hợp `suppressFactor < 0,5` ở `q = 0` |
| TF-AC-010 | Cùng seed, cùng lựa chọn | Chạy lại thuật toán (kiểm toán) | `SkillDef` giống hệt |
| TF-AC-011 | Chế tạo xong | Kiểm tra | Mảnh bị tiêu; 1 `ITM_SCROLL_CRAFTED_HUYEN` có `craftedRefId`; người tạo **chưa** biết kỹ năng |
| TF-AC-012 | Dùng ngọc giản tự chế | Học | `character_technique` có `CSK_*`, `source = CRAFTED`; thi triển như kỹ năng thường |
| TF-AC-013 | Ngọc giản tự chế trong Tàng Bảo Các, nhân vật tọa hóa | Kiếp mới (đủ `realmMin`) | Rút và học được |
| TF-AC-014 | Tên chứa từ cấm | Chế tạo | Từ chối, báo lý do |
| TF-AC-015 | GM đổi tên | Sau đó | Mọi ngọc giản / người đã học hiển thị tên mới, `nameStatus = GM_RENAMED` |
| TF-AC-016 | Thay đổi `modifiers.json` | Công pháp đã tạo trước đó | `skill_def` không đổi |
| TF-AC-017 | (Beta) Người tạo còn sống sao chép lần thứ 20 | Sao chép | Được; lần 21 bị từ chối |
| TF-AC-018 | (Beta) Người tạo đã hết kiếp | Người khác cầm ngọc giản thử sao chép | Từ chối (chỉ người tạo còn sống) |
| TF-AC-019 | (Beta) Tán giản ngọc giản Huyền | Kết quả | 1 mảnh hạch tâm Huyền cùng loại |
| TF-AC-020 | Diễn Pháp Tràng lần thứ 6 trong ngày | Bắt đầu | Từ chối |
| TF-AC-021 | Ngộ tính 30, khí vận 30 | Tính trần dưới của `q` khi U = 0 | `q = max(0, −0,1 − 0,04) = 0` (chặn) |
| TF-AC-022 | Phẩm Thiên có đột biến | Sự kiện | Thông báo toàn server ưu tiên 2 |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Alpha (MVP) | Beta | Sau launch |
|----------|:---:|:----:|:----------:|
| Ngộ Đạo Thạch, xem trước, Diễn Pháp Tràng | ✅ | | |
| Phẩm Hoàng – Huyền, 8 hạch tâm, 8 nguyên tố, 15 biến thức + 3 hiếm | ✅ | | |
| Áp chế ngân sách, validator tổ hợp | ✅ | | |
| Ngọc giản tự chế, học, cất Tàng Bảo Các | ✅ | | |
| Sao chép, tán giản, bán qua giao dịch | | ✅ | |
| Phẩm Địa, Ngộ Đạo Trà | | ✅ | |
| Phẩm Thiên | | | ✅ |
| Chế tạo tâm pháp chính, tuyệt kỹ | | | ✅ |

---

## 17. Câu hỏi mở
- [ ] **TF-Q-1:** Có nên cho người chơi "đặt tên + mô tả ngắn" (≤ 60 ký tự) cho công pháp để tăng cảm giác sở hữu? Rủi ro kiểm duyệt.
- [ ] **TF-Q-2:** Trần 20 bản sao có quá thấp / cao với công pháp nổi tiếng? Cân nhắc trần theo phẩm (Hoàng 50, Thiên 5).
- [ ] **TF-Q-3:** Có cho tổ đội cùng "hợp lực ngộ đạo" (nhiều người góp mảnh, chia ngọc giản) không?
- [ ] **TF-Q-4:** Bảng xếp hạng "Công Pháp Bảng" theo số người học / dùng (danh tiếng cho người tạo)?
- [ ] **TF-Q-5:** Chế công pháp ở động phủ (GD-30) với Tĩnh Thất cấp 3 thay vì phải về Tàng Kinh Các?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-22 | — | Bản nháp đầu: bất biến, thành phần (8 hạch tâm, 8 nguyên tố, 18 biến thức) dạng phép vá dữ liệu, thuật toán ghép tất định + ngộ đạo + đột biến + áp chế ngân sách, chi phí, ngọc giản tự chế (học, Tàng Bảo Các, sao chép có trần, tán giản), VFX tham số, nguồn mảnh, công thức + ví dụ, API, DB, nghiệm thu; mở khóa từ Trúc Cơ; bổ sung công thức `powerScore` cho GD-07 |

