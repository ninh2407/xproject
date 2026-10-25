# GD-13 — Kinh tế

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-13 · Prefix ID: `EC` |
| Owner | Game Designer (kinh tế) + Server Dev (dashboard) |
| Trạng thái | 🟡 Bản nháp v0.7 |
| Cần trước | Phase 2 (linh thạch, cửa hàng NPC, phí), Phase 4 (dashboard trước Alpha), Beta (giao dịch, Vạn Bảo Lâu) |
| Quyết định nền | PLAN D-03, D-17 · PLAN §4.14 |
| Liên quan | [PLAN §4.14](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-09 Vật phẩm](15-items-equipment.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-21 Open World](22-open-world.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-27 Thiên phú](04-talents.md) · GD-10 Luyện đan · GD-11 Luyện khí · [GD-14 Xã hội](20-social.md) · GD-15 Nhiệm vụ · GD-19 Monetization |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Tham số ở `shared/gamedata/economy/*.json` (§7). Mọi giao dịch tiền / vật phẩm đi qua cổng inventory của GD-09.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-13 | Mục |
|--------------|-----------------|-----|
| GD-09 | Công thức `value` của vật phẩm, hệ số giá trị theo phẩm, giá cửa hàng NPC, danh mục cửa hàng | §5.3–5.4, §6.2 |
| GD-12 | Linh thạch rơi từ quái | §6.3 |
| GD-21 | Phí truyền tống, thưởng linh thạch mốc khám phá | §5.2 |
| GD-22 | Trần linh thạch của `GRANT_CURRENCY` theo phẩm cơ duyên | §5.2 |
| GD-26 | Tỷ lệ / trần linh thạch chuyển kiếp (trả lời LC-Q-3) | §5.6 |
| GD-28 | Phí vào hiểm địa theo cấp | §5.2 |
| GD-10 / GD-11 | Phí chế tác, định giá sản phẩm theo giờ công | §5.3 |
| GD-14 | Giao dịch trực tiếp, cống hiến tông môn, `rankMin` của Bảo Khố | §5.4, §5.7, §5.8 |

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện*, P4 *Chế tác có chiều sâu*.

- Linh thạch **luôn có giá trị**: không lạm phát vô hạn, người mới không bị bỏ xa vì giá cả.
- Người chơi chuyên nghề (luyện đan sư, người đi hiểm địa) **có vai trò kinh tế thật** khi giao dịch mở (Beta).
- Một mạng là **kênh tiêu hao tự nhiên**: chết / tọa hóa xóa tài sản, giữ kinh tế lành mạnh (PLAN §4.14).
- Tiền thật **không mua được sức mạnh**, kể cả gián tiếp qua đổi tiền tệ (D-17).

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="ec-r-001"></a>**EC-R-001** | Không có quy đổi `JADE` ↔ `SPIRIT_STONE` dưới bất kỳ hình thức nào (kể cả qua vật phẩm trung gian bán được). Vật phẩm mua bằng `JADE` luôn `bind = ACCOUNT` và không bán NPC được. Mã lý do ví `JADE`: danh mục riêng ở [GD-19 §10](50-monetization.md) (`JADE_*`). |
| <a id="ec-r-002"></a>**EC-R-002** | NPC **không bán** vật phẩm độc quyền hiểm địa, đan đột phá, Diên Thọ Đan, Thế Thân Phù, Truyền Tống Phù, đan phẩm chất trên `HA`, trang bị trên phẩm `PHAM`. Những thứ này chỉ đến từ khám phá, chế tác, rơi đồ (và giao dịch người chơi ở Beta). |
| <a id="ec-r-003"></a>**EC-R-003** | Mọi nguồn sinh linh thạch (faucet) và mọi nơi tiêu (sink) có `reasonCode` riêng trong `currency_ledger`, để dashboard đo được (§5.9). |
| <a id="ec-r-004"></a>**EC-R-004** | Mọi giá, phí, tỷ lệ rơi linh thạch là tham số gamedata, chỉnh được không cần deploy code (đòn bẩy điều tiết, §5.10). |
| <a id="ec-r-005"></a>**EC-R-005** | Giá trị vật phẩm (`value`) được định theo **giờ công** (§5.3), có công thức / bảng tham chiếu; không đặt tùy ý. |

---

## 2. Tổng quan

```
                FAUCET (sinh linh thạch)                         SINK (xóa linh thạch)
  quái rơi ~45% ─┐                                       ┌─ mua NPC (đan Hạ phẩm, đồ Phàm, nguyên liệu cơ bản)
  nhiệm vụ ~20% ─┤                                       ├─ phí truyền tống
  bán đồ NPC ~25% ┼──►  VÍ NHÂN VẬT (SPIRIT_STONE) ──────┼─ phí vào hiểm địa
  khám phá, cơ   │       │                               ├─ phí chế tác, học đan phương (GD-10/11)
  duyên ~10% ────┘       │                               ├─ cường hóa (GD-11, Beta)
                         │                               ├─ thuế + phí đăng Vạn Bảo Lâu (Beta)
                         ▼                               └─ ★ HẾT KIẾP: toàn bộ ví bị xóa (trừ 20% khi tọa hóa → Vault, có trần)
             Tàng Bảo Các (vaultSpiritStones, tài khoản)
                                                         Vật phẩm: chết / tọa hóa / di hài hết hạn / bán NPC / tiêu hao
 Mục tiêu: tổng sink / tổng faucet mỗi tuần 85–100% (§6.6)
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type RealmBand = 'PN' | 'LK_EARLY' | 'LK_MID' | 'LK_LATE' | 'TC' | 'KD' | 'NA';   // §6.1
type CurrencyReason =                                    // currency_ledger.reason_code (bổ sung danh mục GD-09 IT-R-100)
  // faucet
  | 'MONSTER_DROP' | 'BOSS_DROP' | 'QUEST_REWARD' | 'NPC_SELL' | 'ENCOUNTER' | 'EXPLORATION_MILESTONE'
  | 'CHARACTER_CREATION' | 'VAULT_WITHDRAW' | 'AUCTION_SOLD' | 'TRADE_IN' | 'GM_GRANT'
  // sink
  | 'NPC_BUY' | 'REMNANT_TRIAL_FEE' | 'TELEPORT_FEE' | 'DANGER_ENTRY_FEE' | 'CRAFT_FEE' | 'RECIPE_LEARN_FEE' | 'ENHANCE_FEE'
  | 'CAVE_CLAIM_FEE' | 'CAVE_RELOCATE_FEE' | 'CAVE_BUILD' | 'SITE_CLAIM_FEE' | 'SITE_UPKEEP'   // GD-30
  | 'SECT_CREATE_FEE' | 'SECT_DONATE' | 'SECT_UPGRADE' | 'SECT_UPKEEP' | 'SECT_DISBAND'   // GD-14 §10
  | 'AUCTION_DEPOSIT' | 'AUCTION_TAX' | 'TRADE_OUT' | 'VAULT_DEPOSIT' | 'LIFE_ENDED_DESTROYED' | 'GM_REMOVE'
  // chuyển kiếp
  | 'ENDING_CARRYOVER';

interface ShopDef {
  id: string;                         // SHOP_*
  npcId: string; mapId: string;
  currency: 'SPIRIT_STONE' | 'SECT_CONTRIBUTION';
  entries: ShopEntry[];
}
interface ShopEntry {
  templateId: string;
  priceMult?: number;                 // giá = value × priceMult (mặc định params.npcBuyMult)
  priceOverride?: number;             // dùng khi không theo value (vd. cửa hàng tông môn)
  quality?: PillQuality;              // chỉ 'HA' (EC-R-002)
  grade?: ItemGrade;                  // chỉ 'PHAM' (EC-R-002)
  realmMin?: string;
  dailyLimit?: number;                // giới hạn mua mỗi nhân vật mỗi ngày thực
  rankMin?: NpcSectRank;              // chỉ cửa hàng Thanh Vân Tông (GD-14 SO-R-044)
}

interface EconomySnapshot {           // tổng hợp hằng ngày (job)
  date: string;
  moneySupply: { characterWallets: number; vaultStones: number; total: number };
  faucetByReason: Record<string, number>;
  sinkByReason: Record<string, number>;
  walletPercentilesByBand: Record<RealmBand, { p10: number; p50: number; p90: number; p99: number }>;
  itemInflow: Record<string, number>;     // templateId quan trọng → số lượng sinh ra
  itemOutflow: Record<string, number>;    // → số lượng bị hủy / tiêu hao
  activeCharactersByBand: Record<RealmBand, number>;
}

// ===== Beta =====
interface AuctionListing {
  listingId: string; sellerCharacterId: string; itemInstanceId: string;
  qty: number; unitPrice: number; deposit: number;
  createdAt: string; expiresAt: string; state: 'ACTIVE' | 'SOLD' | 'CANCELLED' | 'EXPIRED';
}
interface TradeSession {
  tradeId: string; aCharacterId: string; bCharacterId: string;
  aOffer: { itemInstanceIds: string[]; spiritStones: number };
  bOffer: { itemInstanceIds: string[]; spiritStones: number };
  state: 'OPEN' | 'LOCKED' | 'CONFIRMED' | 'CANCELLED'; lockedAt: string | null;
}
```

---

## 4. Máy trạng thái (Beta)

**`TradeSession`:** `OPEN` (hai bên thêm / bớt) → `LOCKED` (cả hai bấm khóa; mọi thay đổi đưa về `OPEN`) → sau `params.tradeConfirmDelaySec` (5s) cả hai xác nhận → `CONFIRMED` (một transaction hoán đổi) · bất kỳ lúc nào → `CANCELLED`.

**`AuctionListing`:** `ACTIVE` → `SOLD` (có người mua) · `CANCELLED` (người bán hủy, mất phí đăng) · `EXPIRED` (hết hạn, vật phẩm về `MAIL` người bán). Hết kiếp người bán: `CANCELLED` và vật phẩm `DESTROYED` (GD-09 IT-R-083).

---

## 5. Quy tắc

### 5.1. Linh thạch
- <a id="ec-r-010"></a>**EC-R-010** Linh thạch là **một đơn vị nguyên** (`SPIRIT_STONE`). Hạ / Trung / Thượng phẩm chỉ là **cách hiển thị**: 1 Trung phẩm = 100 Hạ phẩm, 1 Thượng phẩm = 10.000 Hạ phẩm (vd. `12.345` hiển thị "1 Thượng · 23 Trung · 45 Hạ").
- <a id="ec-r-011"></a>**EC-R-011** Linh thạch thuộc **nhân vật** (mất khi hết kiếp, GD-09 IT-R-090), trừ phần trong Tàng Bảo Các (`vaultSpiritStones`, tài khoản).

### 5.2. Faucet và sink: số liệu chuẩn
- <a id="ec-r-020"></a>**EC-R-020** **Thu nhập mỗi giờ chơi mục tiêu** (`IPH`, người chơi điển hình, chơi hỗn hợp): là thước đo gốc cho mọi giá và phí.

| Dải | Cảnh giới | IPH (linh thạch / giờ chơi) |
|-----|-----------|:---:|
| `PN` | PN1 | 20 |
| `LK_EARLY` | LK1–3 | 100 |
| `LK_MID` | LK4–6 | 250 |
| `LK_LATE` | LK7–9 | 450 |
| `TC` | TC1–4 | 1.200 |
| `KD` | KD1–4 | 3.500 |
| `NA` | NA1–4 | 10.000 (sau launch) |

- <a id="ec-r-021"></a>**EC-R-021** **Tỷ trọng faucet mục tiêu:** quái ~45% · nhiệm vụ ~20% · bán đồ NPC ~25% · khám phá + cơ duyên ~10%.
- <a id="ec-r-022"></a>**EC-R-022** **Faucet cụ thể:**

| Nguồn | Quy tắc | Tài liệu |
|-------|---------|----------|
| Quái | Công thức §6.3 (theo cảnh giới quái, hạng) | GD-12 |
| Nhiệm vụ | `stones = round(IPH(dải nhiệm vụ) × phút dự kiến / 60 × 0,8)` | GD-15 |
| Bán NPC | `floor(value × 0,25 × hệ số phẩm / phẩm chất)` (GD-09 §6.4) | GD-09 |
| Mốc khám phá | 50% bản đồ: `0,5 × IPH(dải bản đồ)`; 100%: `1,0 × IPH` (một lần mỗi tài khoản) | GD-21 §3.5 |
| Cơ duyên `GRANT_CURRENCY` | Trần theo phẩm: `IPH(dải của realmMax) × giờ`: PHAM 0,25h · LINH 0,5h · HUYEN 1h · DIA 2h · THIEN 4h (validator) | GD-22 §8 |
| Xuất thân | 0–1.000 khi tạo nhân vật | GD-27 §5.2 |

- <a id="ec-r-023"></a>**EC-R-023** **Sink cụ thể:**

| Sink | Giá | Tài liệu |
|------|-----|----------|
| Truyền tống | Mỗi lần: `PN`/`LK_EARLY` 5 · `LK_MID` 10 · `LK_LATE` 15 · `TC` 40 · `KD` 120; khác vực ×2. Kiếp đầu và cảnh giới < `LK4`: **miễn phí** (trả lời GD-21 OW-4) | GD-21 §3.5 |
| Phí vào hiểm địa | Nhất giai 50 · Nhị giai 300 · Tam giai 1.000 (trả lời GD-28 DZ-Q-3: không miễn phí, phí ≈ 10–15 phút thu nhập) | GD-28 DZ-R-011 |
| Mua NPC | §5.4 | GD-09 |
| Phí chế tác | `round(0,05 × value(sản phẩm dự kiến))` mỗi lần luyện, tối thiểu 1 | GD-10 / GD-11 |
| Học đan phương / khí phổ ở NPC | `value(công thức)` | GD-10 / GD-11 |
| Động phủ (Beta) | Chiếm 500 · dời 2.000 · công trình 2.000–40.000 / cấp | GD-30 §6.2 |
| Sơn môn (Beta) | Chiếm 20.000 · duy trì 2.500–6.000 / tuần (tông khố) | GD-30 §6.3 |
| Cường hóa (Beta) | GD-11 | GD-11 |
| Vạn Bảo Lâu (Beta) | Phí đăng 2% (không hoàn) + thuế bán 8% | §5.8 |
| **Hết kiếp** | Toàn bộ ví nhân vật bị xóa (trừ chuyển kiếp §5.6) | GD-26 |

### 5.3. Định giá vật phẩm theo giờ công
- <a id="ec-r-030"></a>**EC-R-030** `value` (giá trị tham chiếu, linh thạch) của vật phẩm **không phải trang bị** đặt theo:
```
value ≈ giờ_công_để_có × IPH(dải sử dụng) × hệ số khan hiếm
hệ số khan hiếm: phổ biến 0,5 · thường 1,0 · hiếm 1,5 · độc quyền hiểm địa 2,0
```
  `giờ_công` = thời gian trung bình người chơi điển hình cần để kiếm được 1 đơn vị (đo bằng Balance Simulator / telemetry `item_granted`).
- <a id="ec-r-031"></a>**EC-R-031** `value` trang bị / pháp bảo theo công thức §6.2 (không đặt tay).
- <a id="ec-r-032"></a>**EC-R-032** Bảng giá trị tham chiếu MVP (`value`):

| Vật phẩm | Dải | `value` | Ghi chú |
|----------|-----|---:|---------|
| Hồi Khí Đan / Hồi Linh Đan (Trung phẩm) | LK | 8 | NPC bán Hạ phẩm 12 |
| Tụ Khí Đan (đan tu luyện LK) | LK | 40 | |
| Giải Độc Đan | LK | 60 | |
| Tị Độc Đan | LK_MID | 50 | Cho Vạn Độc Đầm |
| Thanh Tâm Đan (gỡ 1 tầng tâm ma) | LK–TC | 400 | |
| Bách Niên Linh Quả (thiên tài địa bảo) | LK–TC | 800 | |
| **Ngưng Cơ Thảo** (độc quyền HD01) | LK_MID | 900 | ≈ 1,8h × 250 × 2,0 |
| **Trúc Cơ Đan** (Trung phẩm) | LK_LATE | 3.000 | Sản phẩm luyện đan |
| Truyền Tống Phù | LK–TC | 600 | |
| Tẩy Tủy Linh Dịch (độc quyền HD02) | TC | 5.000 | |
| Diên Thọ Đan | mọi dải | 2.000 | "Thọ nguyên là hàng hóa quý nhất" (PLAN §4.14) |
| Thế Thân Phù | mọi dải | 20.000 | |

### 5.4. Cửa hàng NPC
- <a id="ec-r-040"></a>**EC-R-040** Giá mua từ NPC = `ceil(value × priceMult)`, mặc định `npcBuyMult = 1,0`; **đan dược Hạ phẩm** `priceMult = 1,5` (để đan do người chơi luyện, Trung phẩm trở lên, có chỗ đứng). Thiên phú `TAL_THRIFTY` giảm 10% (GD-27).
- <a id="ec-r-041"></a>**EC-R-041** Giá bán cho NPC: GD-09 IT-R-092, với hệ số theo phẩm là `gradeValueMult` (§6.2), **không** phải hệ số chỉ số.
- <a id="ec-r-042"></a>**EC-R-042** Cửa hàng MVP (Thanh Vân Trấn):

| ID | NPC | Tiền tệ | Bán gì |
|----|-----|---------|--------|
| `SHOP_V1_PHARMACY` | Dược Điếm | Linh thạch | Đan hồi phục Hạ phẩm, Giải Độc Đan, Tị Độc Đan (`dailyLimit` 5), đan lô sơ cấp, đan phương cơ bản |
| `SHOP_V1_ARMORY` | Binh Khí Phường | Linh thạch | Trang bị phẩm `PHAM` mọi ô, các dải `LK1`, `LK4`, `LK7`, `TC1` (theo `realmMin`) |
| `SHOP_V1_GENERAL` | Tạp Hóa | Linh thạch | Nguyên liệu phổ biến (linh thảo thường, khoáng thường), búa rèn, ngọc giản Hoàng phẩm Hạ; **đan phương `RCP_TRUC_CO_DAN` 2.000 linh thạch, yêu cầu `LK7`** (hạ từ 3.000 theo GD-03 C6) (đường cho tán tu, GD-14 SO-R-04N) |
| `SHOP_V1_SECT_TREASURY` | Tông Môn Bảo Khố | Cống hiến tông môn | Ngọc giản tâm pháp / kỹ năng Hoàng – Huyền phẩm (GD-07), `ITM_AF_HO_THAN_KINH`, đan phương trung cấp |

- <a id="ec-r-043"></a>**EC-R-043** Validator chặn mọi `ShopEntry` vi phạm EC-R-002 và EC-R-001.

### 5.5. Cống hiến tông môn
- <a id="ec-r-050"></a>**EC-R-050** `SECT_CONTRIBUTION` chỉ kiếm từ nhiệm vụ / sự kiện Thanh Vân Tông (bậc & quy tắc: GD-14 §5.4; nội dung nhiệm vụ: GD-15), trần `params.sectContributionDailyCap` (300) mỗi nhân vật mỗi ngày; không giao dịch, không quy đổi.

### 5.6. Chuyển kiếp (trả lời GD-26 LC-Q-3)
- <a id="ec-r-060"></a>**EC-R-060** Giữ tỷ lệ 20% khi tọa hóa nhưng **hạ trần** `vaultStoneCap` từ 10.000 xuống **5.000**. Lý do: 5.000 ≈ 20 giờ thu nhập `LK_MID`; đủ mua một bộ trang bị Phàm cho kiếp mới (tạo cảm giác "di sản"), không đủ làm đầu kiếp quá dễ.
- <a id="ec-r-061"></a>**EC-R-061** Linh thạch trong Tàng Bảo Các rút ra là faucet `VAULT_WITHDRAW` với nhân vật nhưng không sinh tiền mới cho tổng cung (dashboard tính riêng).

### 5.7. Giao dịch trực tiếp (Beta)
- <a id="ec-r-070"></a>**EC-R-070** Hai người chơi đứng gần nhau (≤ 5m), không chiến đấu, không trong hiểm địa. Tối đa 8 vật phẩm + linh thạch mỗi bên. Vật phẩm `bound` không giao dịch được (GD-09 IT-R-080).
- <a id="ec-r-071"></a>**EC-R-071** Quy trình `OPEN → LOCKED → CONFIRMED` (§4); mọi thay đổi sau khi khóa đưa về `OPEN`; xác nhận cuối cần chờ 5s sau khóa (chống lừa đổi đồ phút chót). Hoán đổi trong một transaction qua cổng inventory (`TRADE_IN` / `TRADE_OUT`).
- <a id="ec-r-072"></a>**EC-R-072** Không thuế giao dịch trực tiếp; đổi lại có giới hạn: tổng giá trị (theo `value`) nhận qua giao dịch trực tiếp mỗi ngày ≤ `params.tradeDailyValueCap` = `20 × IPH(dải người nhận)` (chống chuyển tài sản / RMT quy mô lớn).

### 5.8. Vạn Bảo Lâu: sàn đấu giá (Beta)
- <a id="ec-r-080"></a>**EC-R-080** Đăng bán: `unitPrice ≥ npcSellPrice` của vật phẩm (sàn giá, chống xả hàng); phí đăng `max(1, floor(2% × tổng giá))` trả ngay, không hoàn; thời hạn 24h hoặc 48h; tối đa 20 tin mỗi nhân vật.
- <a id="ec-r-081"></a>**EC-R-081** Bán được: người mua trả đủ, người bán nhận `tổng giá × (1 − 8%)` qua `MAIL`; 8% là sink `AUCTION_TAX`.
- <a id="ec-r-082"></a>**EC-R-082** Mua theo tin (giá cố định), không có cơ chế trả giá ở MVP Beta. Tìm kiếm theo loại, phẩm, cảnh giới yêu cầu; hiện giá trung bình 7 ngày của vật phẩm.
- <a id="ec-r-083"></a>**EC-R-083** Vật phẩm đang đăng ở `AUCTION` (GD-09); hết kiếp người bán → `CANCELLED`, vật phẩm `DESTROYED` (rủi ro được báo khi đăng).

### 5.9. Giám sát
- <a id="ec-r-090"></a>**EC-R-090** Job hằng ngày tạo `EconomySnapshot` từ `currency_ledger`, `item_log` (GD-09). **Bắt buộc có trước Closed Alpha** (PLAN Phase 4).
- <a id="ec-r-091"></a>**EC-R-091** **Cảnh báo tự động:**

| Chỉ số | Ngưỡng cảnh báo |
|--------|-----------------|
| Tỷ lệ sink / faucet 7 ngày | < 75% hoặc > 110% |
| Ví p50 theo dải so với `IPH × 10` | lệch > ±50% |
| Một nhân vật nhận > `50 × IPH` trong 24h | Gắn cờ điều tra (dupe / bot / RMT) |
| Lượng Diên Thọ Đan / Ngưng Cơ Thảo sinh ra mỗi ngày | lệch > ±40% so với mô phỏng |
| Giao dịch trực tiếp giữa tài khoản cùng thiết bị / IP chạm trần ≥ 3 ngày liên tiếp | Gắn cờ (chuyển tài sản qua tài khoản phụ, PLAN §4.14) |

### 5.10. Đòn bẩy điều tiết
- <a id="ec-r-100"></a>**EC-R-100** Khi dashboard lệch mục tiêu, chỉnh theo thứ tự ưu tiên (không cần deploy, EC-R-004): (1) phí truyền tống / hiểm địa / chế tác, (2) thuế Vạn Bảo Lâu, (3) tỷ lệ rơi linh thạch từ quái (`monsterStoneMult`), (4) giá NPC. **Không** giảm thưởng đã công bố của nhiệm vụ / mốc khám phá (gây cảm giác bị cắt xén).
- <a id="ec-r-101"></a>**EC-R-101** Mọi thay đổi tham số kinh tế ghi Decision Log (PM-04) kèm số liệu dashboard lý do.

---

## 6. Công thức

### 6.1. Dải kinh tế từ cảnh giới
```
band(realmCode) = PN1 → PN · LK1–3 → LK_EARLY · LK4–6 → LK_MID · LK7–9 → LK_LATE · TC → TC · KD → KD · NA → NA
```

### 6.2. Giá trị trang bị / pháp bảo
```
value = round( eqValueBase[kind] × realmScale(itemRealm) × (setId ? 1,5 : 1) )         (giá trị mẫu, phẩm PHAM)
instanceValue = value × gradeValueMult[grade]
eqValueBase: WEAPON 60 · BODY 45 · HEAD 30 · BELT 30 · BOOTS 30 · RING 35 · PENDANT 35 · ARTIFACT 150
gradeValueMult: PHAM 1 · LINH 2,5 · BAO 6 · TIEN 15 · THAN 40
realmScale: hàm chung GD-06 §6.2 (g, J tăng dần theo cảnh giới)
```
Ví dụ kiếm `LK4`: `round(60 × 1,1^3) = round(79,86) = 80`. Phẩm `BAO`: giá trị bản thể 480; bán NPC `floor(80 × 0,25 × 6) = 120`.
Áo bộ Khoáng Ma `LK5`: `round(45 × 1,4641 × 1,5) = round(98,83) = 99`.

### 6.3. Linh thạch rơi từ quái
```
stoneBase(realmCode) = (1 + 0,3 × stageIndex_trong_cảnh_giới_LK_quy_đổi) × majorScale(realm)      (majorScale: GD-06 §6.2)
  với: LK n → 1 + 0,3 × n ; TC n → (1 + 0,3 × (9 + n)) × 1,6 ; KD n → (1 + 0,3 × (13 + n)) × 2,88 ; PN1 → 0,5
stones = round( stoneBase × rankStoneMult[rank] × monsterStoneMult × U(0,7; 1,3) )
rankStoneMult: NORMAL 1 · ELITE 8 · BOSS 40          monsterStoneMult (đòn bẩy, mặc định 1,0)
```
Ví dụ quái thường `LK6`: `stoneBase = 1 + 1,8 = 2,8` → 2–4 linh thạch (trung bình 2,8). Tinh anh `LK6`: ~22. Quái thường `TC1`: `(1 + 3,0) × 1,6 = 6,4`.
Quái có tag `humanoid` (người) ×1,5 (vd. Khoáng Tặc, GD-12).

### 6.4. Kiểm tra IPH (đối chiếu)
Người chơi `LK_MID` đánh quái thường `LK6` liên tục ~80 con / giờ → ~225 linh thạch / giờ (chỉ quái). Chơi hỗn hợp (45% thời gian đánh quái, phần còn lại nhiệm vụ / khám phá / bán đồ) đạt IPH 250 ± 20%. Balance Simulator kiểm chứng (§8).

### 6.5. Trần linh thạch cơ duyên
```
maxGrantCurrency(encounter) = IPH(band(realmMax của cơ duyên)) × hoursByTier[tier]
hoursByTier: PHAM 0,25 · LINH 0,5 · HUYEN 1 · DIA 2 · THIEN 4
```
Ví dụ chuỗi `CHAIN_V1_KIEM_TRUNG` (phẩm DIA, Luyện Khí cuối): trần `450 × 2 = 900`; outcome `onFail` 200 ✓.

### 6.6. Tỷ lệ sink / faucet
```
ratio_7d = Σ sink (7 ngày, trừ VAULT_DEPOSIT) / Σ faucet (7 ngày, trừ VAULT_WITHDRAW)
mục tiêu 0,85–1,00; LIFE_ENDED_DESTROYED tính là sink (từ GD-26 v0.3: chỉ đồ mang theo; kho đồ phong ấn — `SEAL_DISSIPATED` cũng là sink; theo dõi tỷ lệ sink giảm ở Alpha; bù bằng phí giải ấn `REMNANT_TRIAL_FEE`, EC-Q-6)
```

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/economy/params.json` | Tham số (dưới) |
| `shared/gamedata/economy/shops.json` | `ShopDef[]` (§5.4) |
| `shared/gamedata/items/templates/*.json` | Trường `value` (GD-09) theo §5.3, §6.2 |

```jsonc
// params.json
{
  "iphByBand": { "PN": 20, "LK_EARLY": 100, "LK_MID": 250, "LK_LATE": 450, "TC": 1200, "KD": 3500, "NA": 10000 },
  "displayUnits": { "TRUNG": 100, "THUONG": 10000 },
  "npcBuyMult": 1.0, "npcLowPillMult": 1.5, "npcSellRatio": 0.25,
  "gradeValueMult": { "PHAM": 1, "LINH": 2.5, "BAO": 6, "TIEN": 15, "THAN": 40 },
  "eqValueBase": { "WEAPON": 60, "BODY": 45, "HEAD": 30, "BELT": 30, "BOOTS": 30, "RING": 35, "PENDANT": 35, "ARTIFACT": 150 },
  "setValueMult": 1.5,
  "teleportFee": { "PN": 5, "LK_EARLY": 5, "LK_MID": 10, "LK_LATE": 15, "TC": 40, "KD": 120, "NA": 300 },
  "teleportCrossRegionMult": 2, "teleportFreeFirstLifeBelow": "LK4",
  "dangerEntryFee": { "1": 50, "2": 300, "3": 1000, "4": 3000, "5": 8000 },
  "craftFeePct": 0.05,
  "monsterStoneMult": 1.0, "rankStoneMult": { "NORMAL": 1, "ELITE": 8, "BOSS": 40 }, "humanoidStoneMult": 1.5,
  "stoneVarianceMin": 0.7, "stoneVarianceMax": 1.3,
  "questStoneFactor": 0.8,
  "explorationMilestoneHours": { "50": 0.5, "100": 1.0 },
  "encounterCurrencyHours": { "PHAM": 0.25, "LINH": 0.5, "HUYEN": 1, "DIA": 2, "THIEN": 4 },
  "sectContributionDailyCap": 300,
  "stoneCarryoverPct": 0.20, "vaultStoneCap": 5000,
  "trade": { "maxItems": 8, "radius": 5, "confirmDelaySec": 5, "dailyValueCapIphMult": 20 },
  "auction": { "depositPct": 0.02, "taxPct": 0.08, "durationsHours": [24, 48], "maxListings": 20 },
  "alerts": { "ratioMin": 0.75, "ratioMax": 1.10, "walletIphMult": 10, "walletDeviation": 0.5,
              "personalDailyIphMult": 50, "keyItemDeviation": 0.4 }
}
```

**Validator (CI):** `ShopEntry` không bán vật phẩm thuộc EC-R-002 và không dùng `JADE` (EC-R-001); `value` trang bị khớp §6.2; vật phẩm không phải trang bị có `value > 0`; `GRANT_CURRENCY` của mọi cơ duyên ≤ §6.5; linh thạch trong bảng rơi đồ quái khớp §6.3 ±30%; mọi `reason_code` dùng cho tiền tệ thuộc `CurrencyReason`.

---

## 8. Mô phỏng kinh tế
Balance Simulator (PLAN §6.5) bổ sung mô-đun kinh tế: 1.000 nhân vật ảo theo 4 hồ sơ (cày quái, nhiệm vụ, chuyên hiểm địa, luyện đan) qua 30 ngày, gồm chết / tọa hóa theo phân phối GD-26. Đầu ra: IPH theo dải (§5.2), tỷ lệ sink / faucet (§6.6), phân phối ví, lượng vật phẩm khóa (Ngưng Cơ Thảo, Diên Thọ Đan) lưu hành. Kết quả ghi v0.2.

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai
> **Đường dẫn, method và khóa idempotency chuẩn ở [TD-04 §5](../tech/03-api.md)** (mọi thao tác của nhân vật nằm dưới `/v1/characters/{id}/…`, xem trước dùng `POST`, khóa idempotency qua header `Idempotency-Key`). Bảng dưới mô tả mục đích; khi vênh với TD-04, theo TD-04.

| Endpoint | Mục đích | Ghi chú |
|----------|----------|---------|
| `GET /v1/shops/{shopId}` · `POST .../buy` · `/sell` · `/buyback` | Cửa hàng NPC | GD-09 §9.2; giá theo §5.4, kèm số còn được mua hôm nay |
| `POST /v1/trades` · `PUT /v1/trades/{id}/offer` · `POST /v1/trades/{id}/lock` · `/confirm` · `/cancel` | Giao dịch trực tiếp (Beta) | §5.7 |
| `GET /v1/auction/search` · `POST /v1/auction/listings` · `POST /v1/auction/listings/{id}/buy` · `DELETE /v1/auction/listings/{id}` | Vạn Bảo Lâu (Beta) | §5.8 |
| `GET /v1/auction/price-history/{templateId}` | Giá trung bình 7 ngày (Beta) | |

### 9.2. API quản trị (GM / designer)

| Endpoint | Mục đích |
|----------|----------|
| `GET /admin/v1/economy/snapshots?from&to` | `EconomySnapshot[]` |
| `GET /admin/v1/economy/flows?from&to&groupBy=reason\|band` | Faucet / sink chi tiết |
| `GET /admin/v1/economy/alerts` | Cảnh báo §5.9 đang mở |
| `GET /admin/v1/economy/characters/{id}/ledger` | Truy vết dòng tiền một nhân vật |

### 9.3. Sự kiện domain
Tiêu thụ `currency.changed`, `inventory.item_granted`, `inventory.item_destroyed` (GD-09), `lifecycle.life_ended` (GD-26) để tổng hợp. Phát `economy.alert_raised { kind, value, threshold }`.

---

## 10. Lưu trữ

```sql
CREATE TABLE shop_purchase_limit (
  character_id UUID NOT NULL, shop_id VARCHAR(32) NOT NULL, template_id VARCHAR(64) NOT NULL,
  day DATE NOT NULL, count INT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, shop_id, template_id, day)
);

CREATE TABLE economy_snapshot (
  day DATE PRIMARY KEY, data JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE economy_alert (
  id BIGSERIAL PRIMARY KEY, kind VARCHAR(48) NOT NULL, subject VARCHAR(80), value NUMERIC NOT NULL,
  threshold NUMERIC NOT NULL, status VARCHAR(16) NOT NULL DEFAULT 'OPEN', created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Beta
CREATE TABLE trade_session (
  trade_id UUID PRIMARY KEY, a_character_id UUID NOT NULL, b_character_id UUID NOT NULL,
  a_offer JSONB NOT NULL, b_offer JSONB NOT NULL, state VARCHAR(16) NOT NULL,
  locked_at TIMESTAMPTZ, completed_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE trade_daily_value (
  character_id UUID NOT NULL, day DATE NOT NULL, received_value BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, day)
);
CREATE TABLE auction_listing (
  listing_id UUID PRIMARY KEY, seller_character_id UUID NOT NULL, item_instance_id UUID NOT NULL UNIQUE,
  template_id VARCHAR(64) NOT NULL, qty INT NOT NULL, unit_price BIGINT NOT NULL, deposit BIGINT NOT NULL,
  state VARCHAR(16) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), expires_at TIMESTAMPTZ NOT NULL,
  buyer_character_id UUID, sold_at TIMESTAMPTZ
);
CREATE INDEX ix_auction_search ON auction_listing(template_id, unit_price) WHERE state = 'ACTIVE';
```
Bảng `auction_listing` ở PLAN §5.4 được thay bằng định nghĩa trên.

---

## 11. UI/UX
- **Hiển thị linh thạch:** dạng Thượng · Trung · Hạ phẩm với biểu tượng ba màu; tooltip hiện số nguyên.
- **Cửa hàng NPC:** tab Mua / Bán / Mua lại; vật phẩm vượt cảnh giới hiện mờ; giới hạn ngày "còn 3/5".
- **Xác nhận phí:** truyền tống và hiểm địa hiện phí trước khi xác nhận; số dư sau giao dịch.
- **Giao dịch trực tiếp (Beta):** hai cột, nút Khóa → Xác nhận (đếm ngược 5s sau khi cả hai khóa), cảnh báo đỏ nếu đối phương đổi đồ sau khi khóa.
- **Vạn Bảo Lâu (Beta):** tìm kiếm, lọc, giá trung bình 7 ngày, cảnh báo "vật phẩm sẽ mất nếu nhân vật hết kiếp khi chưa bán".
- **Dashboard kinh tế** (admin web, PLAN §6.5): biểu đồ cung tiền, faucet / sink theo lý do, phân phối ví theo dải, cảnh báo.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-09 | Cổng inventory / tiền tệ; `value`, giá NPC, `AUCTION` |
| GD-26 | Hết kiếp xóa ví; chuyển kiếp 20% / trần 5.000 |
| GD-28 | Phí vào; chết là sink vật phẩm; nguyên liệu độc quyền là hàng giá trị |
| GD-12 | Linh thạch rơi từ quái |
| GD-21 | Phí truyền tống, thưởng mốc khám phá |
| GD-22 | Trần `GRANT_CURRENCY` |
| GD-10 / GD-11 | Phí chế tác, học công thức; sản phẩm Trung phẩm+ là hàng hóa |
| GD-14 | Cống hiến tông môn, Bảo Khố theo bậc, tông khố tông môn tự lập, chặn giao dịch theo danh sách chặn |
| GD-19 | `JADE` tách biệt hoàn toàn (EC-R-001) |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Hai người mua cùng một tin đấu giá cùng lúc | Khóa dòng tin; người sau nhận `409 SOLD` |
| Người bán hết kiếp khi tin đang `ACTIVE` | `CANCELLED`, vật phẩm `DESTROYED`, phí đăng không hoàn |
| Người nhận giao dịch chạm trần giá trị ngày | Không khóa được giao dịch; hiện số còn được nhận |
| Bán cho NPC rồi mua lại sau khi hết kiếp | Danh sách mua lại thuộc nhân vật, mất khi hết kiếp |
| Giá NPC thay đổi khi người chơi đang mở cửa hàng | Giá chốt khi gửi lệnh mua; lệch thì trả `409 PRICE_CHANGED` kèm giá mới |
| Rút linh thạch Vault khi ví gần tràn số | `BIGINT`, không có giới hạn thực tế |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện / chỉ số | Câu hỏi trả lời |
|------------------|-----------------|
| `currency_flow` (GD-09) theo `reasonCode` × dải | Tỷ trọng faucet thực tế so với EC-R-021 |
| IPH thực tế theo dải | So với bảng EC-R-020 |
| Ví khi chết / tọa hóa | Bao nhiêu tiền bị xóa mỗi ngày (sink lớn nhất) |
| Chi tiêu đầu kiếp (24h đầu) của kiếp ≥ 2 | Chuyển kiếp 5.000 có làm đầu kiếp quá dễ |
| Tỷ lệ dùng truyền tống / hiểm địa sau khi đổi phí | Độ nhạy của phí |
| Giá Vạn Bảo Lâu của vật phẩm khóa (Beta) | Thọ nguyên / nguyên liệu đột phá có thực sự là hàng quý |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="ec-ac-001"></a>EC-AC-001 | 12.345 linh thạch | Hiển thị | "1 Thượng · 23 Trung · 45 Hạ" |
| EC-AC-002 | Ví dụ §6.2 | Tính `value` | Kiếm `LK4`: 80; bản thể `BAO`: 480; bán NPC 120; áo Khoáng Ma: 99 |
| EC-AC-003 | Quái thường `LK6`, 100.000 lần | Mô phỏng rơi linh thạch | Trung bình 2,8 ± 0,05; mọi giá trị trong [2, 4] |
| EC-AC-004 | `SHOP_V1_PHARMACY` bán `ITM_HOI_KHI_DAN` Hạ phẩm (`value` 8) | Giá mua | 12 (× 1,5) |
| EC-AC-005 | Có `TAL_THRIFTY` | Mua cùng món | 11 (ceil(12 × 0,9)) |
| EC-AC-006 | Dữ liệu cửa hàng chứa `ITM_DIEN_THO_DAN` hoặc trang bị phẩm `LINH` | Validator | Fail (EC-R-002) |
| EC-AC-007 | Bất kỳ mục nào có giá `JADE` mà không `bind = ACCOUNT` | Validator | Fail (EC-R-001) |
| EC-AC-008 | Kiếp 1, `LK3`, truyền tống | Phí | 0 |
| EC-AC-009 | Kiếp 2, `LK5`, truyền tống khác vực | Phí | 20 (10 × 2) |
| EC-AC-010 | Mua Tị Độc Đan lần thứ 6 trong ngày | Mua | Từ chối (`dailyLimit` 5); ngày thực sau mua lại được |
| EC-AC-011 | Cơ duyên phẩm HUYEN, `realmMax = LK6`, `GRANT_CURRENCY` 300 | Validator | Fail (trần 250) |
| EC-AC-012 | Tọa hóa với 30.000 linh thạch, Vault đang có 1.000 | GD-26 bước 8 | Vault = min(1.000 + 6.000, 5.000) = 5.000 |
| EC-AC-013 | Dữ liệu 7 ngày: faucet 1.000.000, sink 700.000 | Job cảnh báo | Tạo `economy_alert` `RATIO_LOW` |
| EC-AC-014 | Một nhân vật `LK_MID` nhận 13.000 linh thạch trong 24h | Job cảnh báo | Gắn cờ (> 50 × 250) |
| EC-AC-015 | (Beta) Đăng bán giá dưới `npcSellPrice` | API | Từ chối |
| EC-AC-016 | (Beta) Bán được 1.000 | Hoàn tất | Người bán nhận 920 qua thư; ledger có `AUCTION_TAX` 80; phí đăng 20 đã trừ lúc đăng |
| EC-AC-017 | (Beta) Đổi vật phẩm sau khi cả hai đã khóa | Giao dịch | Về `OPEN`, cả hai phải khóa lại |
| EC-AC-018 | (Beta) Người nhận `LK_MID` đã nhận giá trị 4.900 trong ngày, đề nghị thêm 200 | Khóa | Từ chối (trần 5.000) |
| EC-AC-019 | Mọi `reason_code` của `currency_ledger` trong code | Test tĩnh | Thuộc `CurrencyReason` |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 2 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Linh thạch, hiển thị Thượng / Trung / Hạ, cửa hàng NPC, phí truyền tống / hiểm địa | ✅ | | | |
| Định giá `value`, bảng giá tham chiếu, validator kinh tế | | ✅ | | |
| Cống hiến tông môn + Tông Môn Bảo Khố | | ✅ | | |
| `EconomySnapshot`, dashboard, cảnh báo | | ✅ (trước Alpha) | | |
| Giao dịch trực tiếp, Vạn Bảo Lâu | | | ✅ | |
| Giá trung bình, chống RMT nâng cao | | | ✅ | ✅ |
| Kinh tế theo mùa (server season, PLAN Q20) | | | | ? |

---

## 17. Câu hỏi mở
- [ ] **EC-Q-1:** Có cho đổi cống hiến tông môn sang linh thạch (một chiều, tỷ lệ xấu) không? *Mặc định: không.*
- [ ] **EC-Q-2:** Vạn Bảo Lâu có cần đấu giá (trả giá) cho vật phẩm cực hiếm (Thế Thân Phù) thay vì giá cố định?
- [ ] **EC-Q-3:** Thuế 8% có quá cao với vật phẩm giá trị lớn? Cân nhắc thuế bậc thang.
- [ ] **EC-Q-4:** Trần giao dịch trực tiếp 20 × IPH có cản hợp tác chính đáng (đồng đội đưa nguyên liệu đột phá)? Có ngoại lệ cho thành viên cùng tông môn?
- [x] **EC-Q-6:** *(Đã chốt 2026-10-23: **có phí giải ấn** nộp khi vào Tàn Hồn Cảnh — LK 100 · TC 600 · KD 2.500 · NA 10.000, GD-26 LC-R-162.)* Từ GD-26 v0.3 (D-20) kho đồ không còn bị hủy khi hết kiếp mà phong ấn → hố tiền `LIFE_ENDED_DESTROYED` nhỏ đi. Có cần **phí giải ấn** (linh thạch theo tầng) ở Luân Hồi Đài làm hố tiền bù không? Quyết định sau khi đo dòng tiền Alpha.
- [ ] **EC-Q-5** (= GD-12 EN-Q-3): Khóa phần thưởng boss bí cảnh theo ngày để kiểm soát faucet trang bị Bảo khí?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-21 | — | Bản nháp đầu: bất biến (không đổi Tiên Ngọc ↔ linh thạch, NPC không bán hàng khóa), IPH theo dải, faucet / sink cụ thể, định giá theo giờ công, giá trị trang bị theo phẩm, cửa hàng NPC, cống hiến tông môn, chuyển kiếp (trần 5.000), giao dịch trực tiếp + Vạn Bảo Lâu (Beta), giám sát & đòn bẩy, công thức + ví dụ, API, DB, nghiệm thu |
| v0.2 | 2026-10-23 | — | Tạp Hóa bán đan phương Trúc Cơ Đan 3.000 linh thạch (`LK7`) cho tán tu (GD-14 SO-R-04N) |
| v0.3 | 2026-10-23 | — | Đánh số lại câu hỏi phí giải ấn thành EC-Q-6 (trùng EC-Q-5) và chốt: phí theo tầng, `REMNANT_TRIAL_FEE` |
| v0.4 | 2026-10-25 | — | Trỏ đường dẫn API chuẩn sang TD-04 §5 |
| v0.5 | 2026-10-25 | — | Mã lý do ví JADE ở GD-19 §10 |
| v0.6 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
| v0.7 | 2026-10-25 | — | Đan phương Trúc Cơ ở Tạp Hóa 2.000 linh thạch (GD-03 C6) |