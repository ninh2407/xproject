# GD-09 — Trang bị & Vật phẩm

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-09 · Prefix ID: `IT` |
| Owner | Game Designer + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.5 |
| Cần trước | Phase 1 (túi đồ, trang bị cơ bản, vũ khí), Phase 2 (thuộc tính phụ, kho, thư, cửa hàng NPC, ô nhanh) |
| Quyết định nền | PLAN D-01 (gắn trang bị vào bone), D-12, D-17 · PLAN §4.11, §5.4 |
| Liên quan | [PLAN §4.11](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-06 Chỉ số](12-stats-formulas.md) · [GD-07 Kỹ năng](13-skills.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-27 Thiên phú](04-talents.md) · GD-10 Luyện đan · GD-11 Luyện khí · GD-13 Kinh tế · GD-19 Monetization |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Dữ liệu ở `shared/gamedata/items/*.json` (§7). **Mọi hệ thống khác thay đổi vật phẩm / tiền tệ đều đi qua API của tài liệu này** (§9.1).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-09 | Mục |
|--------------|-----------------|-----|
| Mọi tài liệu | Cổng `inventory/grant`, `consume`, `move`, `currency/change`; danh mục **mã lý do `item_log` hợp nhất** | §5.10, §9.1 |
| GD-06 | Chỉ số `FLAT`/`PCT` từ trang bị, hiệu ứng bộ; định nghĩa "trang bị chuẩn" cho nhân vật tham chiếu | §5.3–5.5, §6.1 |
| GD-07 | Vũ khí → `WeaponType`; vật phẩm `TECHNIQUE_SCROLL` | §5.2, §5.7 |
| GD-12 | Phẩm vật phẩm ↔ độ hiếm rơi đồ; trang bị rơi ra roll phẩm theo độ hiếm | §5.6 |
| GD-22 | Trần phẩm vật phẩm theo phẩm cơ duyên (GD-22 §8.2) | §5.6 |
| GD-26 / GD-28 | Các vị trí `VAULT`, `DANGER_BAG`, `CORPSE`, `LEGACY`, `DESTROYED`; chuyển hàng loạt | §5.8, §9.1 |
| GD-04 / GD-10 | Phẩm chất đan `PillQuality` trên instance; trường `cultivation` | §3, §5.7 |
| GD-11 | Trường `enhanceLevel`, thuộc tính phụ, ô khắc (cường hóa / tẩy luyện chi tiết ở GD-11) | §5.4, §6.3 |

---

## 1. Mục tiêu

**Trụ cột:** P2 *Cảm giác tu tiên*, P4 *Chế tác có chiều sâu*, P6 *Nhẹ*.

- Trang bị là **phần thưởng hữu hình** (đổi ngoại hình trên nhân vật) và **lựa chọn build** (thuộc tính phụ), nhưng không lấn át kỹ năng người chơi.
- Một mạng nghĩa là đồ đạc **có thể mất**: hệ thống phải minh bạch tuyệt đối (đồ đang ở đâu, có rủi ro không).
- Mọi vật phẩm **truy vết được** từ lúc sinh ra tới lúc mất, để điều tra dupe / hack (PLAN R6).
- Team nhỏ làm đủ nội dung: trang bị theo **mẫu + phẩm + roll**, không vẽ tay từng món.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="it-r-001"></a>**IT-R-001** | Mọi thay đổi vật phẩm / tiền tệ (sinh ra, chuyển vị trí, đổi chủ, tiêu hao, hủy) đi qua **cổng inventory** của API Service (§9.1), trong transaction, idempotent. Không hệ thống nào ghi thẳng bảng `item_instance`. |
| <a id="it-r-002"></a>**IT-R-002** | Mọi thay đổi vị trí khác chủ / khác loại container, đổi chủ, tạo, hủy đều ghi `item_log` với `reasonCode` thuộc danh mục §5.10. |
| <a id="it-r-003"></a>**IT-R-003** | Mẫu vật phẩm (`ItemTemplate`) nằm trong gamedata; DB chỉ lưu instance. |
| <a id="it-r-004"></a>**IT-R-004** | Roll ngẫu nhiên (phẩm, thuộc tính phụ, mở rương) chạy trên server, seed ghi `rng_audit`. | Tên kind chuẩn: `ITEM_GRADE`, `ITEM_AFFIX`, `CONTAINER_OPEN` ([TD-01 §9.3](../tech/00-tdd.md)).
| <a id="it-r-005"></a>**IT-R-005** | Tiên Ngọc (`JADE`) chỉ mua được ngoại hình và tiện ích không ảnh hưởng sức mạnh hay rủi ro chết (D-17). Không mẫu vật phẩm sức mạnh nào có giá `JADE`. Danh mục đóng được phép / cấm bán: [GD-19 MZ-R-010](50-monetization.md). |
| <a id="it-r-006"></a>**IT-R-006** | Không có độ bền / sửa đồ. Trang bị không hỏng theo thời gian. |

---

## 2. Tổng quan

```
 ItemTemplate (gamedata)           ItemInstance (DB)                       Container (ItemLocation)
 ─────────────────────             ─────────────────                       ────────────────────────
 ITM_EQ_SWORD_LK4 ──── roll ────►  { grade: BAO, affixes: [...],  ──────►  BAG · EQUIP · WAREHOUSE · MAIL
 ITM_TU_KHI_DAN        phẩm,         enhanceLevel: 0, quality... }          VAULT (tài khoản, qua kiếp)
 ITM_NGUNG_CO_THAO     thuộc tính                                           DANGER_BAG · CORPSE · LEGACY
 ...                   phụ                                                  AUCTION (Beta) · DESTROYED
                                       ▲
                                       │ mọi thay đổi
                       cổng inventory (grant · consume · move · currency) ── item_log / currency_ledger
                                       ▲
     GD-04 · GD-07 · GD-10 · GD-11 · GD-12 · GD-22 · GD-26 · GD-27 · GD-28 · cửa hàng NPC · GM
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type ItemCategory =
  | 'EQUIPMENT' | 'ARTIFACT'                       // trang bị thường, pháp bảo
  | 'PILL' | 'TREASURE' | 'BREAKTHROUGH_AID' | 'TALISMAN' | 'CONTAINER'
  | 'MATERIAL' | 'TECHNIQUE_SCROLL' | 'TECHNIQUE_FRAGMENT' | 'RECIPE'
  | 'RUNE' | 'QUEST' | 'COSMETIC_TOKEN'
  | 'TOOL'                                          // đan lô (GD-10), búa rèn / lò rèn (GD-11)
  | 'BEAST_EGG';                                    // trứng linh thú (GD-25)
type ItemGrade = 'PHAM' | 'LINH' | 'BAO' | 'TIEN' | 'THAN';  // Phàm · Linh · Bảo · Tiên · Thần khí
type EquipSlot = 'WEAPON' | 'HEAD' | 'BODY' | 'BELT' | 'BOOTS' | 'RING_1' | 'RING_2' | 'PENDANT' | 'ARTIFACT';
type EquipKind = 'WEAPON' | 'HEAD' | 'BODY' | 'BELT' | 'BOOTS' | 'RING' | 'PENDANT' | 'ARTIFACT';  // RING vào RING_1 hoặc RING_2
type BindRule = 'NONE' | 'ON_PICKUP' | 'ON_EQUIP' | 'ACCOUNT';
type ItemLocation = 'BAG' | 'EQUIP' | 'WAREHOUSE' | 'MAIL' | 'AUCTION' | 'VAULT'
                  | 'DANGER_BAG' | 'CORPSE' | 'LEGACY' | 'SEALED' | 'DESTROYED';   // SEALED: Di Tàng (GD-26 §5.15)
type Currency = 'SPIRIT_STONE' | 'JADE' | 'SECT_CONTRIBUTION';

interface ItemTemplate {
  id: string;                         // ITM_*
  nameKey: string; descKey: string; iconId: string; modelId?: string;
  category: ItemCategory;
  grade?: ItemGrade;                  // cố định với vật phẩm không roll phẩm; trang bị: null (roll khi sinh ra)
  stackMax: number;                   // 1 cho trang bị / ngọc giản
  bind: BindRule;
  flags: { noVault?: boolean; noTrade?: boolean; noSell?: boolean; noDestroy?: boolean; dangerZoneExclusive?: boolean };
  requiredRealm?: string;             // RealmCode để dùng / trang bị
  value: number;                      // giá trị tham chiếu (linh thạch), dùng cho giá bán NPC và ước tính DANGER_BAG
  equipment?: EquipmentSpec;
  artifact?: ArtifactSpec;
  use?: UseSpec;                      // vật phẩm dùng được
  furnace?: FurnaceSpec;              // category TOOL: đan lô (GD-10 §3)
  fire?: FireSpec;                    // hỏa chủng, tiêu hao khi luyện (GD-10 §3)
  hammer?: HammerSpec;                // category TOOL: búa rèn (GD-11 §3)
  catalyst?: CatalystSpec;            // xúc tác luyện khí (GD-11 §3)
  rune?: RuneSpec;                    // category RUNE: trận văn (GD-11 §3, Beta)
  material?: { tags: string[] };      // vd. "herb", "ore", "monster_part", "dz_exclusive"
}

interface EquipmentSpec {
  kind: EquipKind;
  weaponType?: WeaponType;            // GD-07; bắt buộc khi kind = WEAPON
  itemRealm: string;                  // RealmCode của "cấp đồ" (= requiredRealm)
  baseProfile: string;                // khóa vào base_profiles.json (§6.1)
  affixPool: string;                  // khóa vào affix_pools.json (§5.4)
  setId?: string;
  attachBone: string;                 // điểm gắn model (PLAN §6.2)
}

interface ArtifactSpec {
  activeSkillId: string;              // SKL_AF_* (định dạng GD-07)
  passive?: { stat: CombatStat; kind: 'FLAT' | 'PCT'; byGrade: Record<ItemGrade, number> };
  itemRealm: string;
}

type UseSpec =
  | { kind: 'CULTIVATION' }                         // trường `cultivation` (GD-04 §7)
  | { kind: 'HEAL'; hpPctOfMax?: number; mpPctOfMax?: number; overSec: number; cooldownGroup: string }
  | { kind: 'STATUS'; statusId: string; durationSec: number; cooldownGroup: string }
  | { kind: 'DETOX'; amount: number }               // GD-04 CU-R-045
  | { kind: 'TEACH'; teaches: string }              // GD-07 SK-R-044
  | { kind: 'LEARN_RECIPE'; recipeId: string }      // GD-10 / GD-11
  | { kind: 'LIFESPAN_EXTENSION'; baseYears: number }   // GD-26 §6.3
  | { kind: 'FUSION' }                              // trường `fusion` (GD-27 TLN-R-083)
  | { kind: 'MODIFY_ROOT'; operation: 'PURIFY' | 'REMOVE_ELEMENT' | 'MUTATE'; amount?: number; element?: Element }
  | { kind: 'HEART_DEMON_CURE'; stacks: number }    // GD-04 CU-R-095
  | { kind: 'ESCAPE' }                              // Truyền Tống Phù (GD-28)
  | { kind: 'DEATH_WARD' }                          // thụ động, GD-26 LC-R-071 (không "dùng" chủ động)
  | { kind: 'OPEN_CONTAINER'; lootTableId: string } // rương, túi quà
  | { kind: 'COSMETIC_UNLOCK'; cosmeticId: string }
  | { kind: 'CAPTURE_BEAST'; tier: BloodlineGrade; bonusPp: number }   // Ngự Thú Phù (GD-25 §6.2)
  | { kind: 'HATCH' };                              // ấp trứng (GD-25 §5.3)

interface AffixRoll { affixId: string; stat: CombatStat; kind: 'FLAT' | 'PCT'; value: number }

interface ItemInstance {
  id: string;                         // UUID
  templateId: string;
  qty: number;
  location: ItemLocation;
  ownerCharacterId: string | null;    // null với VAULT / LEGACY
  ownerAccountId: string;
  slotIndex: number | null;           // vị trí trong container; EQUIP dùng thứ tự EquipSlot
  equipSlot: EquipSlot | null;
  bound: boolean;
  grade: ItemGrade | null;            // trang bị / pháp bảo: phẩm đã roll
  quality: PillQuality | null;        // đan dược (GD-10)
  affixes: AffixRoll[];
  enhanceLevel: number;               // 0..15 (GD-11)
  sockets: (string | null)[];         // ID trận văn (RUNE), Beta
  spiritBorn: boolean;                // dị tượng: +1 dòng thuộc tính phụ (GD-11 FG-R-033)
  originCharacterId: string | null;   // di vật (GD-28 DZ-R-063)
  dangerRunId: string | null;         // khi DANGER_BAG
  corpseId: string | null;            // khi CORPSE
  crafterCharacterId: string | null;  // GD-10 / GD-11 (hiển thị "Do X luyện chế")
  createdReason: string; createdAt: string;
}
```

---

## 4. Máy trạng thái vị trí vật phẩm

Chuyển vị trí hợp lệ (mọi chuyển khác bị từ chối):

| Từ \ Đến | BAG | EQUIP | WAREHOUSE | MAIL | VAULT | DANGER_BAG | CORPSE | LEGACY | AUCTION | DESTROYED |
|----------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| (tạo mới) | ✅ | | | ✅ | | ✅ | | | | |
| BAG | ↔ | ✅ | ✅ | ✅ gửi | ✅ (HUB) | | ✅ chết | ✅ di sản | Beta | ✅ |
| EQUIP | ✅ | | | | ✅ (ENDING) | | ✅ chết | ✅ di sản | | ✅ hết kiếp |
| WAREHOUSE | ✅ | | ↔ | | | | | | | ✅ hết kiếp |
| MAIL | ✅ nhận | | | | | | | | | ✅ hết hạn / hết kiếp |
| VAULT | ✅ (HUB) | | | | ↔ | | | | | ✅ (người chơi hủy) |
| DANGER_BAG | ✅ thoát | | | ✅ thoát (tràn) | | ↔ | ✅ chết | | | ✅ bỏ / bảo hộ |
| CORPSE | | | | | | ✅ nhặt | | | | ✅ hết hạn |
| LEGACY | ✅ nhận | | | | | | | | | ✅ |
| AUCTION | ✅ hủy tin | | | ✅ bán được | | | | | | ✅ hết kiếp (`noVault`) |

**Di Tàng (`SEALED`, GD-26 §5.15):** `WAREHOUSE` / `MAIL` / `AUCTION` → `SEALED` khi hết kiếp (`LIFE_ENDED_SEALED`); `SEALED` → `WAREHOUSE` (tràn → `MAIL`) khi thắng tàn hồn (`SEAL_RELEASED`); `SEALED` → `DESTROYED` khi kiếp sau kết thúc mà chưa mở (`SEAL_DISSIPATED`). Ô `WAREHOUSE` / `MAIL` → `DESTROYED` ở bảng trên chỉ còn áp dụng cho vật phẩm `noVault`.

`DESTROYED` là trạng thái cuối (giữ dòng DB cho kiểm toán, có thể lưu trữ sang bảng lịch sử sau 90 ngày).

---

## 5. Quy tắc

### 5.1. Container và sức chứa
- <a id="it-r-010"></a>**IT-R-010** Sức chứa:

| Container | Phạm vi | Sức chứa | Ghi chú |
|-----------|---------|----------|---------|
| `BAG` | Nhân vật | 40 ô + mở rộng tài khoản (tối đa +40) | Mở rộng túi là tiện ích cấp tài khoản, giữ qua kiếp (IT-R-005: tiện ích, không sức mạnh) |
| `EQUIP` | Nhân vật | 9 ô (`EquipSlot`) | |
| `WAREHOUSE` | Nhân vật | 60 ô | Chỉ truy cập tại NPC kho ở `HUB`; mất khi hết kiếp (GD-26) |
| `MAIL` | Nhân vật | 50 thư, mỗi thư ≤ 5 vật phẩm | Thư hệ thống hết hạn 14 ngày; vật phẩm trong thư hết hạn → `DESTROYED` |
| `VAULT` | Tài khoản | 3–6 ô (GD-26 §5.11) | |
| `DANGER_BAG` | Nhân vật, theo lượt | 12 / 16 / 20 (GD-28) | |
| `CORPSE`, `LEGACY` | Theo bản ghi | Không giới hạn | |

- <a id="it-r-011"></a>**IT-R-011** **Cộng chồng:** hai instance chỉ gộp được khi cùng `templateId`, cùng `bound`, cùng `quality`, không có `affixes`/`enhanceLevel`/`originCharacterId`, và tổng ≤ `stackMax`. Mặc định `stackMax`: nguyên liệu 99 · đan 20 · phù 10 · thiên tài địa bảo 10 · trang bị / ngọc giản / pháp bảo 1.
- <a id="it-r-014"></a>**IT-R-014** Đan dược có thể mang `affixes = [{ affixId: 'AFX_PILL_PATTERN' }]` (đan văn, GD-10 AL-R-053): hiệu ứng chính ×1,2. Đây là thuộc tính phụ duy nhất được phép trên vật phẩm không phải trang bị.
- <a id="it-r-012"></a>**IT-R-012** Nhận vật phẩm khi container đầy: hành vi theo tham số `overflow` của lời gọi: `MAIL` (gửi thư hệ thống), `REJECT` (thất bại cả lời gọi), `LEAVE` (để lại nguồn, vd. túi đồ quái / di hài).
- <a id="it-r-013"></a>**IT-R-013** Không có vật phẩm "trên mặt đất" ngoài túi đồ quái (GD-12), rương, di hài. Người chơi không thả đồ xuống đất; muốn bỏ thì **hủy** (xác nhận, không với `noDestroy`).

### 5.2. Ô trang bị và vũ khí
- <a id="it-r-020"></a>**IT-R-020** 9 ô: `WEAPON`, `HEAD` (Quan), `BODY` (Bào), `BELT` (Đai), `BOOTS` (Hài), `RING_1`, `RING_2` (Nhẫn), `PENDANT` (Ngọc bội), `ARTIFACT` (Pháp bảo). `EquipKind.RING` vào ô nhẫn trống đầu tiên (hoặc ô người chơi chọn).
- <a id="it-r-021"></a>**IT-R-021** Trang bị cần `requiredRealm ≤ RealmCode` nhân vật (so theo `stageIndex`, GD-04 §6.2). Vật phẩm rút từ Tàng Bảo Các vẫn chịu điều kiện này (GD-26 LC-R-113).
- <a id="it-r-022"></a>**IT-R-022** Đổi trang bị chỉ khi **không trong chiến đấu** (GD-06 SF-R-010) và không `DOWNED`/`CHANNELING`. Đổi xong phát `stats.inputs_changed` (GD-06) và `S2C_EquipmentChanged` cho AOI (đổi model trên nhân vật).
- <a id="it-r-023"></a>**IT-R-023** Ô `WEAPON` quyết định `WeaponType` (GD-07 SK-R-050). Không có vũ khí → đánh tay không: dùng combo của `SWORD` với hệ số ×0,5, không dùng được kỹ năng `weaponReq` cụ thể.
- <a id="it-r-024"></a>**IT-R-024** **Pháp bảo** (`ARTIFACT`): kỹ năng chủ động `activeSkillId` thi triển bằng phím **Tab** (hồi chiêu riêng, quy trình GD-07 §5.6); bị động theo phẩm. Pháp bảo bản mệnh (Kim Đan, ôn dưỡng lên cấp) ở Beta (GD-11).

### 5.3. Chỉ số cơ bản của trang bị
- <a id="it-r-030"></a>**IT-R-030** Chỉ số cơ bản tính theo §6.1 từ `baseProfile`, `itemRealm`, `grade`, `enhanceLevel`, làm tròn **1 chữ số thập phân**, áp lên nhân vật dạng `FLAT` (nguồn `EQUIPMENT`, GD-06 §3).
- <a id="it-r-031"></a>**IT-R-031** **Trang bị chuẩn** (định nghĩa dùng cho cân bằng GD-06 §6.8, GD-12 §6.6): đủ 8 ô (trừ pháp bảo) phẩm `PHAM`, `itemRealm` = cảnh giới nhân vật, `enhanceLevel` 0, không thuộc tính phụ. Bộ chuẩn cho ≈ **+30%** `attack` (hoặc `spellPower` với quạt), `maxHp`, `defense`, `spellResist` so với chỉ số cơ bản cùng cảnh giới (§6.1).

### 5.4. Phẩm và thuộc tính phụ
- <a id="it-r-040"></a>**IT-R-040** Phẩm trang bị / pháp bảo được roll khi sinh ra (rơi đồ, chế tạo, phần thưởng), lưu trên instance.

| Phẩm | Màu | Hệ số chỉ số cơ bản | Số thuộc tính phụ | Hệ số thuộc tính phụ | Ô khắc (Beta) |
|------|-----|:---:|:---:|:---:|:---:|
| `PHAM` (Phàm khí) | Trắng | 1,00 | 0 | — | 0 |
| `LINH` (Linh khí) | Xanh lá | 1,15 | 1 | 1,00 | 0 |
| `BAO` (Bảo khí) | Xanh dương | 1,30 | 2 | 1,15 | 1 |
| `TIEN` (Tiên khí) | Tím | 1,50 | 3 | 1,30 | 2 |
| `THAN` (Thần khí, sau launch) | Vàng cam | 1,75 | 4 | 1,50 | 3 |

- <a id="it-r-041"></a>**IT-R-041** Thuộc tính phụ chọn ngẫu nhiên **không trùng `stat`** từ `affixPool` của mẫu (có trọng số), giá trị roll đều trong `[min, max] × affixGradeMult`, lưu 3 chữ số thập phân. Trang bị `spiritBorn = true` (dị tượng, GD-11) có thêm 1 dòng ngoài số dòng của phẩm.
- <a id="it-r-042"></a>**IT-R-042** Danh mục thuộc tính phụ MVP (`affixes.json`):

| ID | Chỉ số | Kiểu | Khoảng (Linh) | Ô được phép |
|----|--------|------|:---:|-------------|
| `AFX_ATK_PCT` | `attack` | PCT | 3–6% | Vũ khí, nhẫn, ngọc bội |
| `AFX_SP_PCT` | `spellPower` | PCT | 3–6% | Vũ khí, nhẫn, ngọc bội |
| `AFX_CRIT_RATE` | `critRate` | FLAT (điểm %) | 1–3 | Vũ khí, nhẫn, mũ |
| `AFX_CRIT_DMG` | `critDamage` | FLAT (điểm %) | 4–10 | Vũ khí, nhẫn |
| `AFX_HP_PCT` | `maxHp` | PCT | 4–8% | Áo, mũ, đai, ngọc bội |
| `AFX_DEF_PCT` | `defense` | PCT | 4–8% | Áo, mũ, giày |
| `AFX_RES_PCT` | `spellResist` | PCT | 4–8% | Áo, đai, ngọc bội |
| `AFX_ELEM_DMG_{X}` | `elementDamage.X` | FLAT (điểm %) | 4–8 | Vũ khí, ngọc bội |
| `AFX_ELEM_RES_{X}` | `elementResist.X` | FLAT (điểm %) | 5–10 | Áo, đai, giày |
| `AFX_CDR` | `cooldownReduction` | FLAT (điểm %) | 2–4 | Mũ, ngọc bội |
| `AFX_MOVE` | `moveSpeed` | PCT | 3–5% | Chỉ giày |
| `AFX_LIFESTEAL` | `lifesteal` | FLAT (điểm %) | 1–2 | Vũ khí |
| `AFX_HEAL` | `healingBonus` | FLAT (điểm %) | 4–8 | Nhẫn, ngọc bội |
| `AFX_POISE_DMG` | `poiseDamage` | FLAT (điểm %) | 5–10 | Vũ khí |
| `AFX_STAMINA` | `maxStamina` | FLAT | 5–10 | Giày, đai |

### 5.5. Hiệu ứng bộ
- <a id="it-r-050"></a>**IT-R-050** Trang bị có `setId` cộng dồn theo số món khác nhau cùng bộ đang mặc; ngưỡng 2 / 4 món. Hiệu ứng là `StatModifier` (có thể có `when`, GD-27 `EffectCondition`), nguồn `SET_BONUS` (GD-06).
- <a id="it-r-051"></a>**IT-R-051** Bộ MVP (đồ bí cảnh, `ON_PICKUP`):

| ID | Tên | Nguồn | 2 món | 4 món |
|----|-----|-------|-------|-------|
| `SET_KHOANG_MA` | Khoáng Ma | BC01 (LK5) | `defense` +8% | `maxHp` +10%; `poiseDamageTaken` −15% |
| `SET_HO_MI` | Hồ Mị | BC02 (LK7) | `critRate` +4 điểm % | `damageBonus` +12 điểm % khi `NIGHT` |
| `SET_TAN_KIEM` | Tàn Kiếm | BC03 (LK9) | `attack` +8% | `elementDamage.KIM` +15 điểm %; `cooldownReduction` +5 điểm % |

### 5.6. Phẩm vật phẩm, độ hiếm rơi đồ, trần theo cơ duyên
- <a id="it-r-060"></a>**IT-R-060** **Ánh xạ chuẩn** giữa độ hiếm rơi đồ (GD-12 `LootEntry.rarity`) và phẩm vật phẩm:

| Độ hiếm rơi đồ | `COMMON` | `UNCOMMON` | `RARE` | `EPIC` | `LEGENDARY` |
|----------------|:---:|:---:|:---:|:---:|:---:|
| Phẩm vật phẩm | `PHAM` | `LINH` | `BAO` | `TIEN` | `THAN` |

  - Mục rơi đồ là **trang bị / pháp bảo**: `rarity` của mục **quyết định phẩm** của instance được sinh ra.
  - Mục là vật phẩm có `grade` cố định: validator yêu cầu `rarity` khớp `grade` theo bảng trên.
- <a id="it-r-061"></a>**IT-R-061** **Trần phẩm theo phẩm cơ duyên** (bổ sung GD-22 §8.2): `PHAM` → tối đa `PHAM` · `LINH` → `LINH` · `HUYEN` → `BAO` · `DIA` → `TIEN` · `THIEN` → `THAN`. Áp cho `GRANT_ITEM` và bảng rơi đồ của `GRANT_FROM_TABLE`.

### 5.7. Dùng vật phẩm
- <a id="it-r-070"></a>**IT-R-070** Dùng vật phẩm đi qua `use` (§9.2 / ô nhanh §9.3); server định tuyến theo `UseSpec.kind`:

| `kind` | Xử lý bởi | Dùng khi chiến đấu | Ghi chú |
|--------|-----------|:---:|---------|
| `HEAL`, `STATUS` | Game Server (GD-06) | ✅ (ô nhanh 1–4) | Hồi chiêu theo `cooldownGroup` (vd. `HEAL_PILL` 10s) |
| `CULTIVATION` | GD-04 `cultivation/consume` | ❌ | Có xem trước (CU-R-022) |
| `DETOX`, `HEART_DEMON_CURE` | GD-04 | ❌ | |
| `TEACH` | GD-07 `techniques/learn` | ❌ | |
| `LEARN_RECIPE` | GD-10 / GD-11 | ❌ | |
| `LIFESPAN_EXTENSION` | GD-26 `lifespan-change` (`EXTENSION`, `PILL_EXTENSION`) | ❌ | Giảm dần theo `pill_usage` |
| `FUSION` | GD-27 `talents/fusion` | ❌ | Hiện tỷ lệ trước khi dùng |
| `MODIFY_ROOT` | GD-27 `talents/change` | ❌ | |
| `ESCAPE` | GD-28 | ✅ (vận 10s) | Chỉ trong hiểm địa |
| `DEATH_WARD` | GD-26 (thụ động) | — | Không có nút "dùng" |
| `OPEN_CONTAINER` | GD-09 (roll `lootTableId`, IT-R-004) | ❌ | Nội dung vào `BAG` (`overflow = REJECT` nếu không đủ chỗ) |
| `COSMETIC_UNLOCK` | GD-19 | ❌ | Mở khóa ngoại hình cấp tài khoản |

- <a id="it-r-071"></a>**IT-R-071** Tiêu vật phẩm do dùng ghi `item_log` với mã của hệ thống xử lý (vd. `PILL_CONSUMED`, `SCROLL_CONSUMED`, `FUSION_CONSUMED`). Nếu hệ thống xử lý từ chối (vd. đã biết công pháp), **không tiêu** vật phẩm (cùng transaction).
- <a id="it-r-072"></a>**IT-R-072** **Ô nhanh** 1–4: gán vật phẩm `HEAL`/`STATUS`/`ESCAPE`; ô trỏ tới `templateId` (tự lấy chồng tiếp theo trong `BAG` hoặc `DANGER_BAG`).
- <a id="it-r-073"></a>**IT-R-073** Đan hồi phục chịu hệ số phẩm chất đan: `HA` 0,8 · `TRUNG` 1,0 · `THUONG` 1,25 · `CUC` 1,6 (cùng bảng GD-04 `qualityExpMult`).

### 5.8. Ràng buộc, giao dịch, hết kiếp
- <a id="it-r-080"></a>**IT-R-080** `bind`: `NONE` (tự do) · `ON_PICKUP` (ràng buộc khi vào túi lần đầu) · `ON_EQUIP` (ràng buộc khi mặc lần đầu) · `ACCOUNT` (thuộc tài khoản, chỉ ngoại hình / tiện ích). Vật phẩm đã ràng buộc không giao dịch, không đấu giá, không gửi thư cho người khác; **vẫn gửi được vào Tàng Bảo Các** của chính tài khoản (trừ `noVault`).
- <a id="it-r-081"></a>**IT-R-081** MVP **không có giao dịch giữa người chơi** (PLAN §2.3: Beta). Ngoại lệ duy nhất: nhặt di hài trong hiểm địa (GD-28) và nhận động phủ truyền thừa (GD-22, Beta).
- <a id="it-r-082"></a>**IT-R-082** Vật phẩm độc quyền hiểm địa (`dangerZoneExclusive`) **không ràng buộc** (`bind = NONE`) để tạo kinh tế (Diên Thọ Quả, nguyên liệu đột phá có giá trị trao đổi khi giao dịch mở ở Beta).
- <a id="it-r-083"></a>**IT-R-083** Hết kiếp: theo GD-26 §5.8 (bước 9): `BAG`, `EQUIP`, `WAREHOUSE`, `MAIL`, `AUCTION` → `DESTROYED`; `VAULT`, ngoại hình tài khoản, `JADE`, mở rộng túi giữ nguyên. *(Sửa bởi GD-26 v0.3:* `WAREHOUSE`, `MAIL`, `AUCTION` → `SEALED` (Di Tàng), chỉ `BAG`, `EQUIP` → `DESTROYED`.)

### 5.9. Tiền tệ
- <a id="it-r-090"></a>**IT-R-090**

| Tiền tệ | Phạm vi | Nguồn | Tiêu | Hết kiếp |
|---------|---------|-------|------|----------|
| `SPIRIT_STONE` (Linh thạch) | Nhân vật | Quái, nhiệm vụ, bán đồ NPC, cơ duyên | Mua NPC, phí truyền tống, phí hiểm địa, chế tác, cường hóa | Mất (trừ phần chuyển vào Vault khi tọa hóa, GD-26) |
| `JADE` (Tiên Ngọc) | Tài khoản | Nạp tiền | Ngoại hình, tiện ích (IT-R-005) | Giữ |
| `SECT_CONTRIBUTION` (Cống hiến tông môn) | Nhân vật | Nhiệm vụ tông môn | Cửa hàng tông môn | Mất (thuộc kiếp, GD-14 SO-R-043) |

- <a id="it-r-091"></a>**IT-R-091** Số dư không bao giờ âm; mọi thay đổi qua `currency/change` (§9.1), ghi `currency_ledger`.
- <a id="it-r-092"></a>**IT-R-092** **Cửa hàng NPC:** giá mua theo dữ liệu cửa hàng (GD-13); giá bán cho NPC = `floor(value × params.npcSellRatio (0,25) × gradeValueMult)` (hệ số giá trị theo phẩm ở [GD-13 §6.2](19-economy.md); không dùng hệ số chỉ số), đan dược theo phẩm chất × hệ số IT-R-073. Mua lại (buyback) 10 món gần nhất trong 30 phút với đúng giá đã bán.

### 5.10. Danh mục mã lý do `item_log` (hợp nhất)
- <a id="it-r-100"></a>**IT-R-100** `reasonCode` chỉ được lấy từ bảng dưới. Thêm mã mới phải thêm vào đây và tài liệu chủ sở hữu.

| Nhóm | Mã | Tài liệu sở hữu |
|------|----|-----------------|
| Tạo / nhận | `CHARACTER_CREATION`, `MONSTER_DROP`, `BOSS_DROP`, `ENCOUNTER`, `QUEST_REWARD`, `NPC_BUY`, `CONTAINER_OPENED`, `CRAFT_OUTPUT`, `MAIL_CLAIM`, `GM_GRANT`, `LEGACY_CLAIMED`, `BEAST_SOUL_GRANTED`, `BEAST_UNSEALED`, `GATHERED` (hái / khai khoáng, GD-21 / TD-04), `EVENT_REWARD` (GD-23) | GD-27, GD-12, GD-22, GD-15, GD-09, GD-10/11, GD-26, GD-25 |
| Tiêu hao | `PILL_CONSUMED`, `TREASURE_CONSUMED`, `BREAKTHROUGH_CONSUMED`, `SCROLL_CONSUMED`, `FUSION_CONSUMED`, `DEATH_WARD_CONSUMED`, `ESCAPE_TALISMAN_CONSUMED`, `CRAFT_INPUT`, `DISMANTLE`, `QUEST_TAKE`, `ITEM_USED`, `BEAST_HATCH_CONSUMED`, `BEAST_SEALED` | GD-04, GD-07, GD-27, GD-26, GD-28, GD-10/11, GD-08, GD-15, GD-09, GD-25 |
| Chuyển vị trí | `MOVE` (BAG ↔ WAREHOUSE), `VAULT_DEPOSIT`, `VAULT_WITHDRAW`, `ENDING_VAULT_DEPOSIT`, `MAIL_SEND`, `MAIL_OVERFLOW`, `DANGER_PICKUP`, `DANGER_EXTRACTED`, `CORPSE_LOOTED`, `MOVED_TO_CORPSE`, `MOVED_TO_LEGACY`, `AUCTION_LIST`, `AUCTION_CANCEL`, `AUCTION_SOLD`, `LIFE_ENDED_SEALED`, `SEAL_RELEASED` | GD-09, GD-26, GD-28, GD-13 |
| Hoàn | `CRAFTING_REFUND` (GD-10 hủy phiên trong thời gian ân hạn) | GD-10 |
| Hủy | `PLAYER_DESTROY`, `NPC_SELL`, `MAIL_EXPIRED`, `LIFE_ENDED_DESTROYED`, `NEWBIE_PROTECTION_DESTROYED`, `DANGER_FORFEIT`, `CORPSE_EXPIRED`, `GM_REMOVE`, `SEAL_DISSIPATED` | GD-09, GD-26, GD-28 |
| Khác | `DANGER_SERVER_RECOVERY`, `BIND` (khi ràng buộc), `ENHANCE`, `REROLL`, `UPGRADE`, `DISMANTLE` | GD-28, GD-09, GD-11 |

- <a id="it-r-101"></a>**IT-R-101** Không ghi `item_log` cho thao tác nội bộ cùng container của cùng chủ (sắp xếp, đổi ô) và `EQUIP`/`UNEQUIP` (ghi riêng vào `equip_history` cho telemetry nếu cần).

---

## 6. Công thức

### 6.1. Chỉ số cơ bản trang bị
```
statBase(template, grade, enhanceLevel)[stat] =
    round1( slotBase[baseProfile][stat] × realmScale(itemRealm) × gradeMult[grade] × enhanceMult(enhanceLevel) )
realmScale(realmCode) = hàm chung của GD-06 §6.2 (g, J tăng dần theo cảnh giới; PN1 = 0,6) — không định nghĩa lại ở đây
gradeMult: PHAM 1,00 · LINH 1,15 · BAO 1,30 · TIEN 1,50 · THAN 1,75
round1 = làm tròn 1 chữ số thập phân (nửa lên)
```

**`slotBase` ở `LK1`, phẩm `PHAM`** (`base_profiles.json`):

| Profile | attack | spellPower | maxHp | maxMp | defense | spellResist |
|---------|:---:|:---:|:---:|:---:|:---:|:---:|
| `WEAPON_SWORD` | 4,5 | 1,5 | | | | |
| `WEAPON_FAN` | 1,5 | 4,5 | | | | |
| `RING` (mỗi chiếc) | 0,75 | 0,75 | | | | |
| `HEAD` | | | 20 | | 0,75 | |
| `BODY` | | | 30 | | 1,5 | 0,75 |
| `BELT` | | | 10 | | | 0,75 |
| `BOOTS` | | | | | 0,75 | 0,75 |
| `PENDANT` | | | | 15 | | 0,75 |

Tổng bộ chuẩn (kiếm, `LK1`): `attack` 6 (30% của 20) · `maxHp` 60 (30% của 200) · `defense` 3 (30%) · `spellResist` 3 (30%) · `maxMp` 15 (15%).

Ví dụ `LK6` (`realmScale` = 1,1^5 = 1,61051), kiếm phẩm `PHAM`: `attack` = round1(4,5 × 1,61051) = 7,2; mỗi nhẫn round1(0,75 × 1,61051) = 1,2 → tổng `attack` 9,6 (≈ +30% so với 32 cơ bản). Cùng kiếm phẩm `LINH`: round1(7,247 × 1,15) = 8,3.

### 6.2. Thuộc tính phụ
```
value = round3( U(min, max) × affixGradeMult[grade] )     U từ PRNG(seed instance)
affixGradeMult: LINH 1,00 · BAO 1,15 · TIEN 1,30 · THAN 1,50
```

### 6.3. Cường hóa (chi phí / tỷ lệ ở [GD-11 §5.6](17-forging.md))
```
enhanceMult(level) = 1 + 0,06 × level        (0 ≤ level ≤ 15; +15 → ×1,9)
```
Chỉ áp lên chỉ số cơ bản, không áp lên thuộc tính phụ.

### 6.4. Giá bán NPC và giá trị túi hiểm địa
```
npcSellPrice = floor(value × 0,25 × (quality ? qualityMult[quality] : 1) × (grade ? gradeValueMult[grade] : 1))
gradeValueMult (GD-13 §6.2): PHAM 1 · LINH 2,5 · BAO 6 · TIEN 15 · THAN 40
value của mẫu trang bị / pháp bảo tính theo GD-13 §6.2 (không đặt tay)
dangerBagEstimate = Σ value × qty (hiển thị, GD-28 HUD)
```

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/items/params.json` | Tham số |
| `shared/gamedata/items/templates/*.json` | `ItemTemplate[]` chia theo nhóm (equipment, pills, materials, scrolls, talismans, artifacts...) |
| `shared/gamedata/items/base_profiles.json` | `slotBase` (§6.1) |
| `shared/gamedata/items/affixes.json` · `affix_pools.json` | Thuộc tính phụ (§5.4), pool theo ô |
| `shared/gamedata/items/sets.json` | Hiệu ứng bộ (§5.5) |
| `shared/gamedata/items/grade.json` | Bảng phẩm (§5.4), ánh xạ độ hiếm (§5.6), trần theo cơ duyên |
| `shared/gamedata/items/cooldown_groups.json` | Nhóm hồi chiêu vật phẩm |

```jsonc
// params.json
{
  "bagBaseSlots": 40, "bagMaxExpansion": 40,   /* 20 mua bằng Tiên Ngọc + 20 đạt trong game (GD-19 MZ-R-030, GD-26 UNL_BAG_SLOTS) */ "warehouseSlots": 60,
  "mailMaxCount": 50, "mailMaxItems": 5, "systemMailExpireDays": 14, "playerMailExpireDays": 30,
  "defaultStackMax": { "MATERIAL": 99, "PILL": 20, "TALISMAN": 10, "TREASURE": 10, "EQUIPMENT": 1, "ARTIFACT": 1, "TECHNIQUE_SCROLL": 1 },
  "quickSlots": 4,
  "npcSellRatio": 0.25, "buybackCount": 10, "buybackMinutes": 30,
  "unarmedCoefficientMult": 0.5,
  "enhanceStep": 0.06, "enhanceMax": 15,
  "gradeMult": { "PHAM": 1.0, "LINH": 1.15, "BAO": 1.3, "TIEN": 1.5, "THAN": 1.75 },
  "affixCount": { "PHAM": 0, "LINH": 1, "BAO": 2, "TIEN": 3, "THAN": 4 },
  "affixGradeMult": { "LINH": 1.0, "BAO": 1.15, "TIEN": 1.3, "THAN": 1.5 },
  "socketCount": { "PHAM": 0, "LINH": 0, "BAO": 1, "TIEN": 2, "THAN": 3 },
  "pillQualityEffectMult": { "HA": 0.8, "TRUNG": 1.0, "THUONG": 1.25, "CUC": 1.6 }
}
```

```jsonc
// templates/equipment_v1.json (trích) — quy ước ID: ITM_EQ_{KIND}[_{WEAPONTYPE}]_{BAND}
[
  { "id": "ITM_EQ_WEAPON_SWORD_LK4", "nameKey": "itm.eq.sword_lk4", "descKey": "", "iconId": "ICO_SWORD_02",
    "modelId": "MDL_SWORD_02", "category": "EQUIPMENT", "stackMax": 1, "bind": "ON_EQUIP", "flags": {},
    "requiredRealm": "LK4", "value": 80,
    "equipment": { "kind": "WEAPON", "weaponType": "SWORD", "itemRealm": "LK4",
                   "baseProfile": "WEAPON_SWORD", "affixPool": "POOL_WEAPON", "attachBone": "hand_r" } },
  { "id": "ITM_EQ_BODY_KHOANG_MA", "nameKey": "itm.eq.body_khoang_ma", "descKey": "", "iconId": "ICO_BODY_KM",
    "modelId": "MDL_BODY_KM", "category": "EQUIPMENT", "stackMax": 1, "bind": "ON_PICKUP", "flags": {},
    "requiredRealm": "LK5", "value": 99,
    "equipment": { "kind": "BODY", "itemRealm": "LK5", "baseProfile": "BODY", "affixPool": "POOL_ARMOR",
                   "setId": "SET_KHOANG_MA", "attachBone": "spine" } }
]
```

```jsonc
// templates/consumables_v1.json (trích)
[
  { "id": "ITM_HOI_KHI_DAN", "category": "PILL", "stackMax": 20, "bind": "NONE", "flags": {}, "value": 8,
    "nameKey": "itm.hoi_khi_dan", "descKey": "", "iconId": "ICO_PILL_RED",
    "use": { "kind": "HEAL", "hpPctOfMax": 0.25, "overSec": 3, "cooldownGroup": "HEAL_PILL" } },
  { "id": "ITM_DIEN_THO_DAN", "category": "PILL", "stackMax": 20, "bind": "NONE", "flags": {}, "value": 2000,
    "nameKey": "itm.dien_tho_dan", "descKey": "", "iconId": "ICO_PILL_GOLD", "requiredRealm": "LK1",
    "use": { "kind": "LIFESPAN_EXTENSION", "baseYears": 20 } },
  { "id": "ITM_TRUYEN_TONG_PHU", "category": "TALISMAN", "stackMax": 10, "bind": "NONE", "flags": {}, "value": 600,
    "nameKey": "itm.truyen_tong_phu", "descKey": "", "iconId": "ICO_TALISMAN_BLUE", "use": { "kind": "ESCAPE" } },
  { "id": "ITM_THE_THAN_PHU", "category": "TALISMAN", "stackMax": 10, "bind": "NONE", "flags": {}, "value": 20000,
    "nameKey": "itm.the_than_phu", "descKey": "", "iconId": "ICO_TALISMAN_GOLD", "use": { "kind": "DEATH_WARD" } },
  { "id": "ITM_NGUNG_CO_THAO", "category": "MATERIAL", "stackMax": 99, "bind": "NONE",
    "flags": { "dangerZoneExclusive": true }, "value": 900, "grade": "BAO",
    "nameKey": "itm.ngung_co_thao", "descKey": "", "iconId": "ICO_HERB_JADE", "material": { "tags": ["herb", "dz_exclusive"] } }
]
```

### 7.1. Pháp bảo MVP
| ID | Tên | Kỹ năng chủ động (Tab) | Bị động (theo phẩm) | Nguồn |
|----|-----|------------------------|---------------------|-------|
| `ITM_AF_HO_THAN_KINH` | Hộ Thân Kính | Khiên 20% `maxHp` 4s, hồi 30s | `defense` +4% → +10% | Cửa hàng tông môn |
| `ITM_AF_TRUY_HON_CHAM` | Truy Hồn Châm | Đạn 20 m/s, 12m, hệ số 2,0, hồi 15s | `critRate` +1 → +4 điểm % | BC02 |
| `ITM_AF_TINH_TAM_CHUNG` | Tĩnh Tâm Chung | Gỡ mọi debuff có thể gỡ + giáp `HEAVY` 2s, hồi 45s | `maxMp` +5% → +15% | Cơ duyên |

### 7.2. Dải trang bị Vực 1
Mỗi dải có đủ 9 ô (vũ khí 2 loại): `LK1` · `LK4` · `LK7` · `TC1` → 4 × 10 = **40 mẫu trang bị thường** + 12 mẫu đồ bộ (3 bộ × 4 món: Áo, Mũ, Đai, Giày) + 3 pháp bảo.

**Validator (CI):** ID `ITM_*` duy nhất; `equipment.itemRealm == requiredRealm`; `weaponType` có khi `kind = WEAPON`; `affixPool` chỉ chứa thuộc tính được phép ở ô (§5.4); `UseSpec` thuộc union; mọi ID tham chiếu (`statusId`, `teaches`, `lootTableId`, `activeSkillId`, `setId`) tồn tại; vật phẩm có `grade` cố định xuất hiện trong bảng rơi đồ với `rarity` khớp IT-R-060; **không mẫu nào có giá `JADE` mà ảnh hưởng chỉ số / rủi ro** (IT-R-005); `dangerZoneExclusive` ⇒ `bind = NONE`; bộ chuẩn tính từ `base_profiles.json` cho +30% ± 2% (IT-R-031).

---

## 8. Mục tiêu cân bằng ban đầu

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Bộ chuẩn (Phàm, đúng cảnh giới) | +30% công / máu / phòng (IT-R-031) |
| Bộ Bảo khí đủ dòng phụ tốt, đúng cảnh giới | ≈ +55–65% chiến lực so với không trang bị |
| Chênh chiến lực giữa trang bị kém nhất và tốt nhất cùng cảnh giới (không cường hóa) | ≤ 40% (kỹ năng vẫn quyết định, P3) |
| Thời gian kiếm đủ bộ chuẩn mỗi dải | 1–2 giờ chơi |

---

## 9. Hợp đồng giao tiếp

### 9.1. Cổng inventory nội bộ (mọi dịch vị → API Service)

| Endpoint | Request | Response | Quy tắc |
|----------|---------|----------|---------|
| `POST /internal/v1/inventory/grant` | `{ grantId, characterId?, accountId, items: [{ templateId, qty, grade?, quality?, bound? }], toLocation: 'BAG' \| 'DANGER_BAG' \| 'MAIL' \| 'VAULT' \| 'LEGACY', overflow: 'MAIL' \| 'REJECT' \| 'LEAVE', reasonCode, refId, extra?: { dangerRunId?, crafterCharacterId? } }` | `{ granted: [{ instanceId, templateId, qty, location }], overflowed: [...], mailed: [...] }` | IT-R-001, 004, 011, 012; roll phẩm / thuộc tính phụ khi cần; idempotent theo `grantId` |
| `POST /internal/v1/inventory/consume` | `{ consumeId, characterId, items: [{ instanceId?, templateId?, qty, fromLocations? }], reasonCode, refId }` | `{ consumed: [...] }` hoặc `409 INSUFFICIENT` | Nguyên tử: đủ hết hoặc không tiêu gì |
| `POST /internal/v1/inventory/move` | `{ moveId, characterId?, accountId, instanceIds \| selector: { locations: ItemLocation[] }, toLocation, reasonCode, refId, extra?: { corpseId?, dangerRunId?, legacyId? } }` | `{ moved: n }` | Chuyển hàng loạt (GD-26 hết kiếp / di hài, GD-28 thoát / bỏ); kiểm tra bảng §4 |
| `POST /internal/v1/currency/change` | `{ changeId, ownerType: 'CHARACTER' \| 'ACCOUNT', ownerId, currency, delta, reasonCode, refId }` | `{ balance }` hoặc `409 INSUFFICIENT` | IT-R-091; idempotent |
| `POST /internal/v1/loot/grant` | (GD-12 §9.1) | | **Bí danh** của `inventory/grant` với `reasonCode = MONSTER_DROP \| BOSS_DROP`, giữ để tương thích GD-12 / GD-28 |

### 9.2. API công khai (Client → API Service)
> **Đường dẫn, method và khóa idempotency chuẩn ở [TD-04 §5](../tech/03-api.md)** (mọi thao tác của nhân vật nằm dưới `/v1/characters/{id}/…`, xem trước dùng `POST`, khóa idempotency qua header `Idempotency-Key`). Bảng dưới mô tả mục đích; khi vênh với TD-04, theo TD-04.

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/characters/{id}/inventory` | `BAG`, `EQUIP`, `WAREHOUSE` (nếu đang ở NPC kho), `DANGER_BAG` (nếu trong hiểm địa), ô nhanh, ví | |
| `POST /v1/characters/{id}/inventory/equip` | `{ instanceId, slot? }` | IT-R-020..023; Game Server xác nhận không chiến đấu |
| `POST /v1/characters/{id}/inventory/unequip` | `{ slot }` | Cần chỗ trống trong `BAG` |
| `POST /v1/characters/{id}/inventory/move` | `{ instanceId, toLocation: 'BAG' \| 'WAREHOUSE', toSlotIndex?, qty? }` | Tách / gộp chồng (IT-R-011); `WAREHOUSE` cần đứng gần NPC kho |
| `POST /v1/characters/{id}/items/{instanceId}/use` | `{ targetCharacterId?, preview?: boolean }` | IT-R-070, 071 |
| `POST /v1/characters/{id}/items/{instanceId}/destroy` | `{ qty, confirm: true }` | IT-R-013 |
| `GET /v1/characters/{id}/mail` · `POST /v1/characters/{id}/mail/{mailId}/claim` | Thư | IT-R-010, 012 |
| `GET /v1/shops/{shopId}` · `POST /v1/shops/{shopId}/buy` · `/sell` · `/buyback` | Cửa hàng NPC | IT-R-092; Game Server xác nhận đứng gần NPC |

### 9.3. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `C2S_UseQuickSlot` | C→S | `{ slot: 1..4 }` |
| `C2S_SetQuickSlot` | C→S | `{ slot, templateId \| null }` |
| `C2S_UseArtifact` | C→S | `{ seq, aim, clientTimeMs }` (phím Tab, quy trình GD-07 §5.6) |
| `S2C_InventoryDelta` | S→C | `{ changes: [{ instanceId, templateId, location, slotIndex, qty, removed? }] }` |
| `S2C_CurrencyUpdate` | S→C | `{ currency, balance }` |
| `S2C_EquipmentChanged` | S→C (AOI) | `{ entityId, slot, modelId \| null, grade, appearanceOverride? }` |

### 9.4. Sự kiện domain (topic `inventory.*`)

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `inventory.equipment_changed` | `{ characterId, slot }` | GD-06 (`stats.inputs_changed`), GD-07 (vũ khí) |
| `inventory.item_granted` | `{ characterId, templateId, qty, grade, reasonCode }` | Telemetry, nhiệm vụ (GD-15), thông báo đồ hiếm |
| `currency.changed` | `{ ownerType, ownerId, currency, delta, reasonCode }` | Dashboard kinh tế (GD-13) |

---

## 10. Lưu trữ

```sql
CREATE TABLE item_instance (
  id                  UUID PRIMARY KEY,
  template_id         VARCHAR(64) NOT NULL,
  qty                 INT NOT NULL CHECK (qty > 0),
  location            VARCHAR(16) NOT NULL,          -- ItemLocation
  owner_character_id  UUID,
  owner_account_id    UUID NOT NULL,
  slot_index          INT,
  equip_slot          VARCHAR(16),
  bound               BOOLEAN NOT NULL DEFAULT FALSE,
  grade               VARCHAR(8),
  quality             VARCHAR(8),
  affixes             JSONB NOT NULL DEFAULT '[]',
  enhance_level       SMALLINT NOT NULL DEFAULT 0,
  seal_band           VARCHAR(4),                     -- chỉ khi location = 'SEALED' (Di Tàng, GD-26 §5.15, TD-03 §8)
  sockets             JSONB NOT NULL DEFAULT '[]',
  origin_character_id UUID,
  danger_run_id       UUID,
  corpse_id           UUID,
  legacy_id           UUID,
  mail_id             UUID,
  crafter_character_id UUID,
  created_reason      VARCHAR(32) NOT NULL,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  version             INT NOT NULL DEFAULT 1           -- khóa lạc quan
);
CREATE INDEX ix_item_owner_loc ON item_instance(owner_character_id, location) WHERE location <> 'DESTROYED';
CREATE INDEX ix_item_account_vault ON item_instance(owner_account_id) WHERE location = 'VAULT';
CREATE UNIQUE INDEX ux_item_equip_slot ON item_instance(owner_character_id, equip_slot) WHERE location = 'EQUIP';

CREATE TABLE item_log (
  id              BIGSERIAL PRIMARY KEY,
  item_instance_id UUID NOT NULL,
  template_id     VARCHAR(64) NOT NULL,
  qty_delta       INT NOT NULL,
  from_location   VARCHAR(16), to_location VARCHAR(16),
  from_owner      UUID, to_owner UUID,
  reason_code     VARCHAR(32) NOT NULL,              -- IT-R-100
  ref_id          VARCHAR(80),
  idempotency_key VARCHAR(80) NOT NULL,              -- grantId / consumeId / moveId ...
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);  -- partition theo tháng (PLAN §5.4)
CREATE INDEX ix_item_log_item ON item_log(item_instance_id);
CREATE UNIQUE INDEX ux_item_log_idem ON item_log(idempotency_key, item_instance_id);

CREATE TABLE currency_wallet (
  owner_type VARCHAR(16) NOT NULL, owner_id UUID NOT NULL, currency VARCHAR(24) NOT NULL,
  balance BIGINT NOT NULL CHECK (balance >= 0),
  PRIMARY KEY (owner_type, owner_id, currency)
);
CREATE TABLE currency_ledger (
  id BIGSERIAL PRIMARY KEY, owner_type VARCHAR(16) NOT NULL, owner_id UUID NOT NULL, currency VARCHAR(24) NOT NULL,
  delta BIGINT NOT NULL, balance_after BIGINT NOT NULL, reason_code VARCHAR(32) NOT NULL, ref_id VARCHAR(80),
  idempotency_key VARCHAR(80) NOT NULL UNIQUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE character_quick_slot (
  character_id UUID NOT NULL, slot SMALLINT NOT NULL, template_id VARCHAR(64),
  PRIMARY KEY (character_id, slot)
);

CREATE TABLE mail (
  mail_id UUID PRIMARY KEY, character_id UUID NOT NULL, sender_kind VARCHAR(16) NOT NULL,  -- SYSTEM | PLAYER
  title_key VARCHAR(64), body TEXT, expires_at TIMESTAMPTZ NOT NULL, claimed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE account_meta ADD COLUMN bag_expansion INT NOT NULL DEFAULT 0;
```
Bảng `item_instance`, `item_log` ở PLAN §5.4 được thay bằng định nghĩa trên.

---

## 11. UI/UX
- **Túi đồ:** lưới ô, viền màu theo phẩm; biểu tượng khóa với vật phẩm ràng buộc; biểu tượng ⚠ "Có thể mất khi chết" ở góc túi khi đang trong hiểm địa.
- **Tooltip trang bị:** tên + phẩm (màu), yêu cầu cảnh giới (đỏ nếu chưa đủ), chỉ số cơ bản, thuộc tính phụ (giá trị + khoảng có thể roll, mờ), hiệu ứng bộ (sáng các ngưỡng đã đạt), so sánh với món đang mặc (mũi tên xanh / đỏ, Δ chiến lực), "Do X luyện chế" nếu có.
- **Bảng nhân vật:** búp bê với 9 ô quanh model 3D xoay được; ô pháp bảo có biểu tượng phím Tab.
- **Ô nhanh 1–4** cạnh thanh kỹ năng, hiển thị số lượng còn lại và hồi chiêu nhóm.
- **Đồ hiếm rơi:** cột sáng màu phẩm (GD-12), thông báo nhỏ "Nhận được [Tên] (Bảo khí)".
- **Hủy vật phẩm:** hộp thoại nhập số lượng + giữ nút 1s với phẩm `BAO` trở lên.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-06 | `FLAT` từ chỉ số cơ bản, thuộc tính phụ, bộ; trang bị chuẩn |
| GD-07 | Vũ khí → combo / kỹ năng; ngọc giản; pháp bảo dùng `SkillDef` |
| GD-04 | Đan tu luyện, đan đột phá, thiên tài địa bảo, Giải Độc Đan, Thanh Tâm Đan |
| GD-10 / GD-11 | Tạo vật phẩm (`CRAFT_OUTPUT`) với `quality` / phẩm; tiêu nguyên liệu; cường hóa, tẩy luyện, khắc |
| GD-12 | Rơi đồ, ánh xạ độ hiếm ↔ phẩm |
| GD-22 | `GRANT_ITEM`, `GRANT_FROM_TABLE`, trần phẩm |
| GD-26 | Vault, hết kiếp, Diên Thọ Đan, Thế Thân Phù |
| GD-27 | Vật phẩm dung hợp, tẩy tủy, vật phẩm khởi đầu theo xuất thân |
| GD-28 | `DANGER_BAG`, di hài, Truyền Tống Phù, vật phẩm độc quyền |
| GD-13 | Giá cửa hàng, dashboard tiền tệ, đấu giá (Beta) |
| GD-19 | Ngoại hình (`COSMETIC_UNLOCK`), mở rộng túi |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Hai yêu cầu cùng lúc dùng chồng đan cuối cùng | Khóa lạc quan (`version`); yêu cầu thứ hai `409` |
| `grant` với `overflow = REJECT` khi túi thiếu 1 ô | Không cấp gì (nguyên tử), trả `409 NO_SPACE` |
| Gỡ trang bị khi túi đầy | Từ chối, báo "Túi đầy" |
| Nhẫn vào khi cả hai ô nhẫn đã có | Thay ô người chơi chọn; mặc định thay `RING_1` |
| Cảnh giới giảm? | Không xảy ra (GD-04 CU-R-024 không tụt tầng); không có trường hợp phải tháo đồ vì mất cảnh giới |
| Thư hệ thống hết hạn khi chứa vật phẩm từ hiểm địa | `MAIL_EXPIRED`; thông báo trước 3 ngày và 1 ngày |
| Ô nhanh trỏ tới đan đã hết | Ô hiện mờ, giữ cấu hình để tự dùng khi có lại |
| Mẫu vật phẩm bị gỡ khỏi gamedata nhưng còn instance | Giữ instance, hiển thị "Vật phẩm thất truyền", không dùng được; validator cảnh báo |
| Dùng vật phẩm `OPEN_CONTAINER` khi túi không đủ chỗ cho nội dung | Từ chối trước khi roll (kiểm tra số ô tối đa có thể sinh ra) |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `item_granted` (gộp) | templateId, grade, reasonCode, realmCode | Phân bố phẩm thực tế theo nguồn |
| `equip_snapshot` (mỗi giờ) | realmCode, gradeBySlot, setPieces, combatPower | Người chơi có đủ bộ chuẩn đúng tiến độ (§8) |
| `item_destroyed` | templateId, grade, reasonCode | Kênh tiêu hao (chết, bán, hủy) |
| `currency_flow` | currency, reasonCode, sum | Faucet / sink (GD-13) |
| `bag_full_event` | context | Túi có quá chật |
| `quick_slot_use` | templateId, inCombat, inDangerZone | Đan hồi phục có được dùng như dự kiến |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="it-ac-001"></a>IT-AC-001 | Bộ chuẩn kiếm `LK1` (8 ô Phàm) | `computeStats` | `attack` +6,1 (kiếm 4,5 + nhẫn 0,8 × 2), `maxHp` +60, `defense` +3,1, `spellResist` +3,2 (do `round1` 0,75 → 0,8), tất cả trong +30% ± 2 điểm |
| IT-AC-002 | Kiếm `LK6` phẩm `PHAM` / `LINH` | Tính chỉ số | `attack` 7,2 / 8,3 (§6.1) |
| IT-AC-003 | `base_profiles.json` | Validator | Bộ chuẩn ở mọi cảnh giới cho +30% ± 2% (IT-R-031) |
| IT-AC-004 | Rơi trang bị từ mục `rarity = RARE` | Sinh instance | `grade = BAO`, đúng 2 thuộc tính phụ không trùng `stat`, giá trị trong khoảng × 1,15 |
| IT-AC-005 | Cùng seed instance | Roll lại thuộc tính phụ (kiểm toán) | Kết quả giống hệt |
| IT-AC-006 | Thêm `AFX_MOVE` vào pool của vũ khí | Validator | Fail (chỉ giày) |
| IT-AC-007 | Mặc 4 món `SET_KHOANG_MA` | `computeStats` | Có đóng góp `SET_BONUS`: `defense` +8%, `maxHp` +10%, `poiseDamageTaken` −15% |
| IT-AC-008 | Nhân vật `LK3` mặc đồ `LK4` | Equip | Bị từ chối (IT-R-021) |
| IT-AC-009 | Đổi vũ khí khi đang chiến đấu | Equip | Bị từ chối (IT-R-022) |
| IT-AC-010 | Đổi kiếm sang quạt | Sau khi equip | `WeaponType = FAN`, kỹ năng kiếm hiện mờ (GD-07 SK-R-043), AOI nhận `S2C_EquipmentChanged` |
| IT-AC-011 | `grant` 3 món với `overflow = MAIL`, túi còn 1 ô | Cấp | 1 vào `BAG`, 2 vào `MAIL`; `item_log` đủ 3 dòng |
| IT-AC-012 | `grant` cùng `grantId` hai lần | API | Áp dụng một lần |
| IT-AC-013 | `consume` 5 Ngưng Cơ Thảo khi chỉ có 4 | API | `409 INSUFFICIENT`, không tiêu gì |
| IT-AC-014 | Hai chồng `ITM_HOI_KHI_DAN` phẩm `TRUNG` (12 + 10) | Gộp | 20 + 2 (`stackMax` 20) |
| IT-AC-015 | Chồng `TRUNG` và chồng `CUC` | Gộp | Từ chối (khác `quality`) |
| IT-AC-016 | Đồ `ON_PICKUP` | Gửi vào Tàng Bảo Các | Được phép (cùng tài khoản); gửi thư cho người khác bị từ chối |
| IT-AC-017 | Dùng ngọc giản kỹ năng đã biết | `use` | GD-07 từ chối → vật phẩm **không** bị tiêu (IT-R-071) |
| IT-AC-018 | Đan hồi phục `CUC`, `hpPctOfMax` 0,25 | Dùng khi chiến đấu (ô nhanh) | Hồi 40% `maxHp` trong 3s; cả nhóm `HEAL_PILL` hồi chiêu 10s |
| IT-AC-019 | `currency/change` −100 khi số dư 50 | API | `409 INSUFFICIENT`, số dư giữ 50 |
| IT-AC-020 | Bán `ITM_EQ_WEAPON_SWORD_LK4` (`value` 80) phẩm `BAO` | Bán NPC | Nhận `floor(80 × 0,25 × 6) = 120` linh thạch; mua lại trong 30 phút với 120 |
| IT-AC-021 | Kết thúc kiếp | GD-26 bước 9 | Mọi vật phẩm `BAG/EQUIP/WAREHOUSE/MAIL` → `DESTROYED` qua `inventory/move`; `VAULT` và `bag_expansion` không đổi |
| IT-AC-022 | Chuyển `CORPSE → BAG` trực tiếp | `inventory/move` | Bị từ chối (không có trong bảng §4) |
| IT-AC-023 | Mọi `reasonCode` xuất hiện trong code | Test tĩnh | Thuộc danh mục IT-R-100 |
| IT-AC-024 | Mẫu vật phẩm sức mạnh có giá `JADE` trong dữ liệu cửa hàng | Validator | Fail (IT-R-005) |
| IT-AC-025 | Mở rương `OPEN_CONTAINER` có thể sinh tối đa 3 món khi túi còn 2 ô | `use` | Từ chối trước khi roll |
| IT-AC-026 | Không vũ khí | Đánh thường | Combo kiếm với hệ số ×0,5; kỹ năng có `weaponReq` cụ thể không dùng được |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 1 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Cổng inventory, `item_log`, túi, trang bị 8 ô, vũ khí 2 loại | ✅ | | | |
| Phẩm + thuộc tính phụ, bộ chuẩn | | ✅ | | |
| Kho, thư, ô nhanh, cửa hàng NPC, mua lại | | ✅ | | |
| Pháp bảo (3) + phím Tab | | ✅ | | |
| Hiệu ứng bộ (3 bộ bí cảnh) | | ✅ | | |
| 40 mẫu trang bị Vực 1 + vật phẩm tiêu hao | | ✅ | | |
| Mở rộng túi (tài khoản) | | ✅ | | |
| Chế tạo trang bị (rèn), phân giải (GD-11) | | ✅ (Alpha) | | |
| Cường hóa, tẩy luyện, thăng phẩm (GD-11) | | | ✅ | |
| Ô khắc trận văn | | | ✅ | |
| Giao dịch, đấu giá (GD-13) | | | ✅ | |
| Pháp bảo bản mệnh | | | ✅ | |
| Thần khí | | | | ✅ |

---

## 17. Câu hỏi mở
- [x] **IT-Q-1:** *(Đã giải quyết bởi GD-26 v0.3: kho đồ không mất mà bị phong ấn, kiếp sau lấy lại qua tàn hồn — kho có ý nghĩa riêng so với Tàng Bảo Các.)* Kho nhân vật (`WAREHOUSE`) mất khi hết kiếp có làm người chơi thấy vô nghĩa so với Tàng Bảo Các? Cân nhắc bỏ kho, tăng túi. *Mặc định: giữ kho 60 ô.*
- [ ] **IT-Q-2:** Đồ bộ bí cảnh `ON_PICKUP` có làm kinh tế Beta kém sôi động? Cân nhắc `ON_EQUIP` cho một số món.
- [ ] **IT-Q-3:** Có cho tách ngoại hình khỏi trang bị (transmog) bằng `JADE` ngay MVP không? (GD-19)
- [ ] **IT-Q-4:** Phím Tab cho pháp bảo có xung đột thói quen (Tab = chọn mục tiêu ở game khác)?
- [ ] **IT-Q-5:** Thư hệ thống hết hạn 14 ngày có quá gắt khi kiếp nhân vật có thể kéo dài nhiều tuần?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-20 | — | Bản nháp đầu: bất biến, kiểu dữ liệu (mẫu, instance, container), bảng chuyển vị trí hợp lệ, sức chứa & cộng chồng, 9 ô trang bị, chỉ số cơ bản & trang bị chuẩn +30%, phẩm & 15 thuộc tính phụ, 3 bộ, ánh xạ độ hiếm ↔ phẩm & trần theo cơ duyên, dùng vật phẩm (13 loại), ràng buộc, tiền tệ, **danh mục mã lý do `item_log` hợp nhất**, công thức, pháp bảo, API cổng inventory, DB, nghiệm thu |
| v0.2 | 2026-10-23 | — | Cột `item_instance.seal_band` cho Di Tàng (TD-03 §8) |
| v0.3 | 2026-10-25 | — | Trỏ đường dẫn API chuẩn sang TD-04 §5 |
| v0.4 | 2026-10-25 | — | Mở rộng túi 20 Tiên Ngọc + 20 trong game; trỏ GD-19 |
| v0.5 | 2026-10-25 | — | Mã lý do `EVENT_REWARD` (GD-23); Trỏ telemetry chuẩn GD-20 |