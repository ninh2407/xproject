# GD-30 — Động phủ & Sơn môn

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-30 · Prefix ID: `HM` |
| Owner | Game Designer + Content Designer (đặt vị trí) + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.3 |
| Cần trước | Beta (cùng tông môn tự lập) |
| Quyết định nền | PLAN D-01, D-02, D-03 · Quyết định 2026-10-22: **tầng 1 + 2** (vị trí trên bản đồ, bên trong là instance; không xây tự do trên bản đồ chung) |
| Liên quan | [PLAN §4.18](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-21 Open World](22-open-world.md) · [GD-14 Xã hội](20-social.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-10 Luyện đan](16-alchemy.md) · [GD-11 Luyện khí](17-forging.md) · [GD-13 Kinh tế](19-economy.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-22 Cơ duyên](23-encounters.md) · GD-15 Nhiệm vụ · GD-19 Monetization · GD-23 Sự kiện thế giới · TD-12, TD-13 |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Dữ liệu ở `shared/gamedata/housing/*.json` (§7).

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-30 | Mục |
|--------------|-----------------|-----|
| GD-21 | Loại POI `CAVE_GATE`, `SECT_SITE`; loại bản đồ `CAVE`, `SECT_GROUNDS`; quy tắc đặt vị trí; chuẩn mật độ | §5.1, §5.5 |
| GD-14 | Sơn môn của tông môn tự lập trên bản đồ, Uy vọng, Sơn Môn Đại Hội; lối vào trú địa | §5.5–5.9 |
| GD-04 / GD-26 | Địa điểm đả tọa / bế quan `CAVE`, `SECT_GROUNDS` và hệ số | §5.3, §6.1 |
| GD-10 / GD-11 | Trạm chế tác trong động phủ, thưởng | §5.3 |
| GD-13 | Phí chiếm chỗ, xây dựng, duy trì sơn môn (sink) | §5.4, §5.8, §6 |
| GD-22 / GD-23 | Phế tích tông môn (sự kiện + rương) | §5.9 |

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện*, P5 *Thế giới đáng khám phá*, P4 *Chế tác có chiều sâu*.

- Người chơi có **"nhà" gắn với một nơi cụ thể trên bản đồ**: chọn cửa động gần linh mạch hay gần hiểm địa là một quyết định.
- Tông môn tự lập có **sơn môn thấy được trên bản đồ chung**, lớn dần theo cấp: danh tiếng và cạnh tranh có kiểm soát.
- **Không phá thiết kế màn chơi**, không thiếu đất, không phá rối, nhẹ cho web: vị trí do designer đặt sẵn, bên trong là instance.
- Thế giới có "lịch sử": sơn môn bị mất trở thành **phế tích** cho người khác khám phá.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="hm-r-001"></a>**HM-R-001** | Người chơi **không** đặt / xây vật thể tự do trên bản đồ chung. Mọi vị trí (`CAVE_GATE`, `SECT_SITE`) do designer đặt trong map editor (TD-13). Xây tự do chỉ ở loại bản đồ riêng "Hoang Vực" (sau launch, ngoài phạm vi tài liệu này). |
| <a id="hm-r-002"></a>**HM-R-002** | Bên trong động phủ và trú địa là **instance riêng** (GD-21 `MapType` `CAVE`, `SECT_GROUNDS`); không có quái, không chiến đấu, tuổi trôi như thành trấn (×0,25, GD-26). |
| <a id="hm-r-003"></a>**HM-R-003** | Động phủ và mọi công trình trong đó thuộc **tài khoản**, giữ qua mọi kiếp (như Tàng Bảo Các, GD-26 LC-R-080). Động phủ **không có kho chứa đồ** (không lách giới hạn Tàng Bảo Các). |
| <a id="hm-r-004"></a>**HM-R-004** | Hệ số đả tọa trong động phủ / trú địa **luôn thấp hơn** linh mạch ngoài đồng (GD-04 `LEY_LINE` 1,5), để đánh đổi "tu nhanh nhưng già nhanh" (GD-04 CU-R-033) vẫn còn ý nghĩa. |
| <a id="hm-r-005"></a>**HM-R-005** | Cạnh tranh sơn môn **không có PvP**: giành / giữ bằng Uy vọng (hoạt động PvE của tông môn) và phí duy trì (§5.7). |
| <a id="hm-r-006"></a>**HM-R-006** | Mọi chi phí đi qua cổng tiền tệ / vật phẩm GD-09; mọi thay đổi chủ sở hữu ghi log. |

---

## 2. Tổng quan

```
 BẢN ĐỒ CHUNG (mọi kênh thấy giống nhau)
 ───────────────────────────────────────
  ⛰ Cửa động (CAVE_GATE)  ── nhiều người cùng chọn một cửa ──►  mỗi người vào ĐỘNG PHỦ riêng (instance CAVE)
     phẩm địa mạch 1–3                                            Tĩnh Thất · Linh Điền · Đan Thất · Khí Thất · trang trí
     (gần linh mạch = phẩm cao)                                   thuộc tài khoản, giữ qua kiếp

  🏯 Địa điểm sơn môn (SECT_SITE)  ── một tông môn giữ một địa điểm ──►  TRÚ ĐỊA tông môn (instance SECT_GROUNDS)
     phẩm A / B / C; mặt tiền lớn dần theo cấp tông môn, cờ hiệu             Đan Phòng · Luyện Khí Các · Linh Mạch Điện (GD-14)
     VACANT ─ chiếm / Đại Hội ─► HELD ─ mất (Uy vọng, nợ phí, giải tán) ─► RUINS (7 ngày: sự kiện Phế Tích) ─► VACANT

  Tông môn không có sơn môn: vào trú địa qua cổng chung ở thành trấn (GD-14), không có thưởng phẩm địa điểm
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type CaveBuilding = 'TINH_THAT' | 'LINH_DIEN' | 'DAN_THAT' | 'KHI_THAT' | 'THU_VIEN';   // THU_VIEN: Linh Thú Viên (GD-25 §5.6)
type VisitPolicy = 'PRIVATE' | 'FRIENDS' | 'PARTY' | 'SECT';
type SiteGrade = 'A' | 'B' | 'C';
type SiteState = 'VACANT' | 'HELD' | 'RUINS';

interface CaveGateDef {                 // trong dữ liệu bản đồ (TD-13), POI kind CAVE_GATE
  poiId: string;                        // V1_F02_CAVE_001
  mapId: string; regionId: string;
  position: [number, number];
  gateGrade: 1 | 2 | 3;                 // phẩm địa mạch: designer đặt (gần linh mạch → 3)
  realmMin: string;                     // cảnh giới tối thiểu để chọn cửa này
}

interface Cave {                        // một tài khoản một động phủ trên server
  accountId: string;
  gatePoiId: string;
  buildings: Record<CaveBuilding, number>;   // cấp 0..3
  visitPolicy: VisitPolicy;
  decorations: { slotId: string; cosmeticId: string }[];   // GD-19
  claimedAt: string; relocatedAt: string | null;
  archived: boolean;                    // ẩn do tài khoản không hoạt động (§5.4)
}

interface SpiritPlot {                  // ô Linh Điền
  accountId: string; plotIndex: number;
  seedTemplateId: string | null; plantedAt: string | null; readyAt: string | null;
}

interface SeedDef {                     // ItemTemplate.seed (category MATERIAL, tag "seed")
  harvestTemplateId: string;            // linh thảo thu được
  growHours: number; yield: [number, number];
  linhDienLevelMin: number;             // cấp Linh Điền tối thiểu
}

interface SectSiteDef {                 // trong dữ liệu bản đồ, POI kind SECT_SITE
  siteId: string;                       // V1_SITE_03
  mapId: string; regionId: string; grade: SiteGrade;
  gatePosition: [number, number];
  footprintId: string;                  // vùng mặt tiền trong map editor
  facadePrefabs: string[];              // 5 biến thể theo cấp tông môn 1..5
  ruinsPrefab: string;
  safeRadius: number;                   // vùng an toàn quanh cổng
}

interface SectSite {
  siteId: string; state: SiteState;
  sectId: string | null;                // GD-14 PlayerSect
  heldSince: string | null;
  upkeepDueAt: string | null; unpaidWeeks: number;
  ruinsUntil: string | null; ruinsFormerSectLevel: number | null;
}

interface SectPrestigeWeek {            // Uy vọng theo tuần
  sectId: string; weekStart: string;
  points: number; breakdown: Record<string, number>;
}
```

---

## 4. Máy trạng thái

### 4.1. Động phủ
`(chưa có)` → `ACTIVE` (chiếm cửa động, HM-R-040) → `ACTIVE` (dời cửa, HM-R-042) · `ACTIVE` ↔ `ARCHIVED` (không hoạt động 60 ngày / đăng nhập lại). Không bao giờ bị xóa tự động.

### 4.2. Địa điểm sơn môn
| Từ | Đến | Điều kiện | Tác dụng phụ |
|----|-----|-----------|--------------|
| `VACANT` | `HELD` | Tông môn chiếm (HM-R-070) hoặc được phân ở Sơn Môn Đại Hội (HM-R-073) | Thu phí chiếm; mặt tiền theo cấp tông môn; lối vào trú địa chuyển sang cổng địa điểm |
| `HELD` | `RUINS` | Thua ở Đại Hội; nợ phí duy trì 2 tuần; tông môn giải tán / tự bỏ | Mặt tiền đổi sang `ruinsPrefab`; bắt đầu sự kiện Phế Tích (§5.9); trú địa của tông môn **không mất**, lối vào về cổng chung ở thành trấn |
| `RUINS` | `VACANT` | Hết 7 ngày | Mặt tiền trống ("đất trống" với bia đá) |

---

## 5. Quy tắc

### 5.1. Cửa động: đặt vị trí (designer)
- <a id="hm-r-010"></a>**HM-R-010** Mỗi bản đồ `FIELD` có **2–4 cửa động** đặt ở vách núi / gốc cây cổ thụ / sau thác, không chắn đường chính, cách POI khác ≥ 10m, nằm trong vùng đệm an toàn bán kính 8m (quái không vào, GD-12 EN-R-005).
- <a id="hm-r-011"></a>**HM-R-011** `gateGrade` theo khoảng cách tới linh mạch gần nhất của bản đồ: < 40m → 3 · < 100m → 2 · còn lại → 1. `realmMin` theo dải bản đồ (vd. F01–F02 `LK3`, F05–F06 `LK6`).
- <a id="hm-r-012"></a>**HM-R-012** **Không giới hạn số chủ** trên một cửa động: mỗi chủ vào động phủ của mình (phasing). Cửa động hiển thị số "đạo hữu đang ẩn cư tại đây" và danh sách động phủ được mở cho khách.

### 5.2. Vào động phủ, khách thăm
- <a id="hm-r-020"></a>**HM-R-020** Tương tác với cửa động (không trong chiến đấu) → "Vào động phủ của ta" (nếu cửa này là của mình) / "Thăm động phủ đạo hữu" (danh sách động phủ tại cửa này cho phép mình vào). Chuyển vào instance `CAVE` như chuyển bản đồ (GD-21 §3.3), ra thì về đúng cửa động.
- <a id="hm-r-021"></a>**HM-R-021** `visitPolicy`: `PRIVATE` · `FRIENDS` (bạn bè, GD-14) · `PARTY` (đồng đội hiện tại) · `SECT` (cùng tông môn tự lập). Khách chỉ xem, trò chuyện, chụp ảnh; không dùng trạm chế tác, Linh Điền, không nhận thưởng đả tọa của chủ (đả tọa của khách tính như `HUB`).
- <a id="hm-r-022"></a>**HM-R-022** Chủ có thể "Truyền tống về động phủ" từ bất kỳ đâu (không trong chiến đấu, không trong hiểm địa / bí cảnh), vận 5s, hồi chiêu 30 phút, miễn phí; ra khỏi động phủ thì xuất hiện ở cửa động.

### 5.3. Công trình trong động phủ
- <a id="hm-r-030"></a>**HM-R-030** Năm công trình (khớp enum `CaveBuilding` §3), cấp 0–3, xây / nâng cấp bằng linh thạch + nguyên liệu (§6.2; Linh Thú Viên theo GD-25 §5.6):

| Công trình | Cấp 1 | Cấp 2 | Cấp 3 | Yêu cầu cảnh giới nhân vật để dùng |
|------------|-------|-------|-------|-----------------------------------|
| **Tĩnh Thất** (`TINH_THAT`) | Điểm đả tọa / bế quan `CAVE` (§6.1) | +0,05 hệ số | +0,05 hệ số | LK1 / LK4 / LK7 |
| **Linh Điền** (`LINH_DIEN`) | 2 ô trồng | 4 ô | 6 ô; trồng được Bách Niên Linh Thảo | LK1 / LK4 / LK7 |
| **Đan Thất** (`DAN_THAT`) | Trạm `ALCHEMY_STATION` (GD-10) | + thành công +2, phẩm chất +2 | +4 / +4 | — |
| **Khí Thất** (`KHI_THAT`) | Trạm `FORGE_STATION` (GD-11) | +2 / +2 | +4 / +4 | — |
| **Linh Thú Viên** (`THU_VIEN`, Beta — cùng linh thú, GD-25 §16) | 4 ô giữ thú, ấp 1, lai tạo ≤ HUYEN | 6 ô, ấp 2, lai tạo ≤ DIA | 8 ô, ấp 3, lai tạo mọi phẩm | Theo GD-25 SB-R-060 (nguồn sự thật của công trình này) |

- <a id="hm-r-031"></a>**HM-R-031** Thưởng của công trình chỉ áp dụng khi nhân vật hiện tại của chủ đạt yêu cầu cảnh giới của **cấp đó**; chưa đạt thì dùng hiệu ứng của cấp cao nhất đã đạt yêu cầu. (Kiếp mới không hưởng ngay toàn bộ lợi thế của kiếp trước.)
- <a id="hm-r-032"></a>**HM-R-032** **Linh Điền:** gieo hạt giống (`SeedDef`) vào ô trống; cây lớn theo **thời gian thực** (kể cả offline); `readyAt = plantedAt + growHours`. Thu hoạch: `randInt(yield)` linh thảo vào `BAG` (`HARVEST`); cây chín không héo. Vật phẩm `dangerZoneExclusive` (Ngưng Cơ Thảo...) **không có hạt giống** (giữ giá trị hiểm địa, GD-28).
- <a id="hm-r-033"></a>**HM-R-033** **Hạt giống** rơi khi hái linh thảo ngoài đồng: xác suất 10% (+ `GATHER_YIELD_PCT` của thiên phú, GD-27) ra 1 hạt giống tương ứng; Tạp Hóa (NPC) bán hạt Thanh Diệp Thảo.
- <a id="hm-r-034"></a>**HM-R-034** **Trang trí:** các ô trang trí đặt vật phẩm ngoại hình (cấp tài khoản, GD-19). Trang trí **không** ảnh hưởng chỉ số.

### 5.4. Chiếm, dời, lưu trữ động phủ
- <a id="hm-r-040"></a>**HM-R-040** **Chiếm cửa động:** nhân vật `ALIVE`, cảnh giới ≥ `realmMin` của cửa, tài khoản chưa có động phủ; phí `params.caveClaimFee` (500 linh thạch, sink `CAVE_CLAIM_FEE`). Công trình bắt đầu ở cấp 0.
- <a id="hm-r-041"></a>**HM-R-041** Một tài khoản một động phủ trên server.
- <a id="hm-r-042"></a>**HM-R-042** **Dời cửa động:** chọn cửa động khác đủ `realmMin`; phí 2.000 (`CAVE_RELOCATE_FEE`); hồi chiêu 7 ngày; công trình và cây đang trồng giữ nguyên.
- <a id="hm-r-043"></a>**HM-R-043** **Lưu trữ:** tài khoản không đăng nhập 60 ngày → `archived = true` (ẩn khỏi danh sách tại cửa động); đăng nhập lại → khôi phục nguyên trạng. Không xóa.
- <a id="hm-r-044"></a>**HM-R-044** Hết kiếp (GD-26): động phủ, công trình, cây trồng, linh thú **giữ nguyên nhưng bị phong ấn** (`caveSealed = true`, GD-26 LC-R-160); cây trồng **ngừng lớn** trong lúc phong ấn (`readyAt` dời theo). Nhân vật kiếp mới đạt `realmMin` của cửa động thì được thách đấu thủ vệ (HM-R-045); thắng mới vào được.
- <a id="hm-r-045"></a>**HM-R-045** **Thủ vệ động phủ:** tương tác cửa động khi `caveSealed` → instance một người tại cửa động. Thủ vệ = linh thú `STABLED` có huyết mạch cao nhất (bằng nhau → tư chất TB cao nhất) làm **thú canh cửa** — dùng đòn và chiêu lệnh của loài, chỉ số bảng cơ bản (GD-06) ở `guardianRealm` × máu ×4; không có thú → **Thạch Hộ Pháp** (`MON_CAVE_WARDEN`, model Thạch Khôi). `guardianRealm` = `realmMin` của cửa động + 2 tiểu cảnh giới (tối thiểu `LK4`). Thắng → `caveSealed = false`, thú canh cửa "nhận lại chủ" (+1 thân mật, GD-25). Thua → hồi chiêu 1800s, không mất gì.

### 5.5. Địa điểm sơn môn: đặt vị trí (designer)
- <a id="hm-r-050"></a>**HM-R-050** Mỗi vực có **6–8 địa điểm sơn môn**, mỗi bản đồ `FIELD` tối đa 1 (không đặt ở bản đồ tân thủ đầu tiên của vực). Vực 1: 5 địa điểm (F02–F06) + 1 ở ngoại vi thành trấn.
- <a id="hm-r-051"></a>**HM-R-051** Mỗi địa điểm có vùng mặt tiền (`footprintId`, khoảng 40 × 40m) dựng sẵn trong map editor; không chắn lối nối bản đồ, không đè POI; tính là **điểm mốc** của bản đồ (GD-21 §3.4). Vùng an toàn bán kính `safeRadius` (25m) quanh cổng.
- <a id="hm-r-052"></a>**HM-R-052** Phẩm địa điểm: `A` (cạnh linh mạch lớn hoặc cửa hiểm địa) · `B` · `C`. Phẩm ảnh hưởng thưởng Linh Mạch Điện (§6.1) và phí duy trì (§6.3).

### 5.6. Mặt tiền và hiển thị
- <a id="hm-r-060"></a>**HM-R-060** Trạng thái mọi địa điểm của bản đồ gửi cho client khi vào bản đồ (`S2C_SectSiteStates`) và khi đổi; mọi kênh của bản đồ hiển thị giống nhau.
- <a id="hm-r-061"></a>**HM-R-061** `HELD`: mặt tiền dùng `facadePrefabs[sectLevel − 1]` (cổng → tường → chính điện → lầu tháp → linh quang), cờ hiệu in huy hiệu tông môn, bia đá ghi tên tông môn. `RUINS`: `ruinsPrefab`. `VACANT`: đất trống + bia đá "Vô chủ".
- <a id="hm-r-062"></a>**HM-R-062** Mặt tiền là **mô hình tĩnh gộp sẵn** (một vài draw call); nằm trong ngân sách hiệu năng bản đồ (PLAN §5.2). Không có vật thể do người chơi đặt.
- <a id="hm-r-063"></a>**HM-R-063** Bản đồ thế giới hiển thị tên tông môn đang giữ từng địa điểm.

### 5.7. Chiếm và giữ sơn môn
- <a id="hm-r-070"></a>**HM-R-070** **Chiếm địa điểm `VACANT`** (bất kỳ lúc nào giữa hai kỳ Đại Hội): tông môn cấp ≥ 2, ≥ 10 thành viên, chưa giữ địa điểm nào; Chưởng môn hoặc Trưởng lão xác nhận; phí `params.siteClaimFee` (20.000 linh thạch từ tông khố, sink `SITE_CLAIM_FEE`). Ai xác nhận trước được trước.
- <a id="hm-r-071"></a>**HM-R-071** Mỗi tông môn giữ **tối đa 1** địa điểm trên server.
- <a id="hm-r-072"></a>**HM-R-072** **Phí duy trì** hằng tuần theo phẩm (§6.3), trừ từ tông khố cùng lúc với phí công trình (GD-14 SO-R-057). Không đủ: `unpaidWeeks += 1`; đủ 2 tuần liên tiếp → `RUINS`.
- <a id="hm-r-073"></a>**HM-R-073** **Sơn Môn Đại Hội** (ngày 1 hằng tháng, 20:00 giờ server, sự kiện GD-23): với từng vực có N địa điểm, xếp hạng các tông môn **đủ điều kiện** (HM-R-070, kể cả tông môn đang giữ địa điểm ở vực đó) theo điểm Đại Hội (§6.4). N tông môn đứng đầu được giữ / nhận địa điểm:
  1. Tông môn đang giữ và nằm trong top N: giữ nguyên địa điểm của mình.
  2. Tông môn mới vào top N: lần lượt theo điểm từ cao xuống chọn trong các địa điểm còn trống (`VACANT`) hoặc vừa bị tông môn rớt hạng để lại (không cần chờ `RUINS`); không chọn trong 10 phút thì hệ thống chọn địa điểm phẩm cao nhất còn lại.
  3. Tông môn đang giữ nhưng rớt khỏi top N: địa điểm cũ chuyển `RUINS` ngay, **trừ khi** có tông môn mới chọn nó: khi đó chuyển thẳng sang tông môn mới (không có phế tích).
  Tông môn mới nhận địa điểm qua Đại Hội **không trả phí chiếm**.
- <a id="hm-r-074"></a>**HM-R-074** Tông môn có thể tự bỏ địa điểm (Chưởng môn, xác nhận lại sau 24h) → `RUINS`.

### 5.8. Uy vọng tông môn
- <a id="hm-r-080"></a>**HM-R-080** Uy vọng tích lũy theo tuần từ hoạt động PvE của thành viên (không có điểm từ đánh nhau giữa người chơi, HM-R-005):

| Nguồn | Điểm | Trần mỗi tuần mỗi thành viên |
|-------|:---:|:---:|
| Hoàn thành nhiệm vụ tông môn hằng tuần (GD-15) | 10 | 30 |
| Hạ boss bí cảnh (lần đầu mỗi boss mỗi tuần) | 5 | 15 |
| Thoát hiểm địa thành công (GD-28) | 3 | 15 |
| Đột phá Trúc Cơ / Kim Đan (GD-04) | 100 / 300 | — |
| Quyên góp tông khố | 1 mỗi 1.000 linh thạch | 20 |

- <a id="hm-r-081"></a>**HM-R-081** Điểm Đại Hội dùng **tổng Uy vọng 4 tuần gần nhất** (§6.4), nên một tông môn không thể "mua" địa điểm chỉ bằng một đợt cày ngắn.

### 5.9. Phế tích tông môn
- <a id="hm-r-090"></a>**HM-R-090** Khi địa điểm vào `RUINS` (7 ngày): mặt tiền đổi sang phế tích; sinh sự kiện **Phế Tích** cho **mọi người chơi** trên bản đồ đó: 3 rương (bảng rơi đồ theo cấp cũ của tông môn, `LT_SITE_RUINS_L{n}`), 1 tinh anh canh giữ (GD-12), cơ hội cơ duyên `POI_PROXIMITY` riêng của phế tích (GD-22, tag `ruins`). Rương làm mới mỗi ngày trong 7 ngày, rơi đồ cá nhân (GD-12 EN-R-004).
- <a id="hm-r-091"></a>**HM-R-091** Bia đá phế tích ghi tên tông môn cũ và thời gian giữ sơn môn (lưu vào Vạn Giới Lục mục Địa danh, GD-21 §3.6): "lịch sử" do người chơi tạo ra.
- <a id="hm-r-092"></a>**HM-R-092** Trú địa của tông môn cũ **không bị ảnh hưởng** (công trình giữ nguyên); chỉ lối vào chuyển về cổng chung ở thành trấn và mất thưởng phẩm địa điểm.

### 5.10. Lợi ích của sơn môn cho thành viên
- <a id="hm-r-100"></a>**HM-R-100** Lối vào trú địa ngay tại địa điểm; "Truyền tống về sơn môn" miễn phí cho thành viên (như HM-R-022, hồi chiêu 30 phút).
- <a id="hm-r-101"></a>**HM-R-101** Thưởng Linh Mạch Điện theo phẩm địa điểm (§6.1).
- <a id="hm-r-102"></a>**HM-R-102** Danh tiếng: tên trên bản đồ thế giới, mặt tiền, thông báo hệ thống khi nhận sơn môn ở Đại Hội (GD-14 §5.6, ưu tiên 2).

---

## 6. Công thức

### 6.1. Hệ số đả tọa / bế quan (bổ sung GD-04 §6.3, §6.4)
```
CAVE (Tĩnh Thất cấp L hiệu lực, L ≥ 1):
  locationMult = min(1,00 + 0,05 × (L − 1) + 0,05 × (gateGrade − 1), 1,25)
SECT_GROUNDS (Linh Mạch Điện cấp L hiệu lực, GD-14):
  locationMult = min(1,00 + 0,05 × L + siteBonus, 1,35)     siteBonus: A 0,10 · B 0,05 · C 0 · không có sơn môn 0
So sánh: LEY_LINE 1,5 (tuổi ×1,0) > SECT_GROUNDS ≤ 1,35 · CAVE ≤ 1,25 (tuổi ×0,25) ≥ HUB 1,0 (tuổi ×0,25)
```
Ví dụ: Tĩnh Thất cấp 3, cửa động phẩm 3 → `min(1 + 0,10 + 0,10, 1,25) = 1,20`. Linh Mạch Điện cấp 5, sơn môn phẩm A → `min(1 + 0,25 + 0,10, 1,35) = 1,35`.

### 6.2. Chi phí công trình động phủ (cấp L)
| Công trình | Cấp 1 | Cấp 2 | Cấp 3 |
|------------|------:|------:|------:|
| Tĩnh Thất | 2.000 | 8.000 + 10 Xích Đồng | 25.000 + 10 Huyền Thiết |
| Linh Điền | 3.000 | 10.000 + 20 Thanh Diệp Thảo | 30.000 + 5 Bách Niên Linh Thảo |
| Đan Thất / Khí Thất | 5.000 | 15.000 + 10 Huyền Thiết | 40.000 + 2 Tinh Kim |
| Linh Thú Viên | 3.000 + 10 Linh Mộc | 12.000 + vật liệu TC | 40.000 + vật liệu KD |

Sink `CAVE_BUILD`. Phá bỏ công trình không hoàn tiền.

### 6.3. Phí sơn môn
```
siteClaimFee = 20.000 (một lần, khi chiếm VACANT ngoài Đại Hội)
siteUpkeepWeekly: A 6.000 · B 4.000 · C 2.500     (cộng thêm phí công trình GD-14 SO-R-057)
```

### 6.4. Điểm Sơn Môn Đại Hội
```
prestige4w = Σ SectPrestigeWeek.points của 4 tuần gần nhất
score = prestige4w × (đang giữ địa điểm ở vực này ? 1,15 : 1,00)
hòa điểm: tông môn lập sớm hơn đứng trước
```
Ví dụ (vực có 2 địa điểm): A (đang giữ) 3.000 → 3.450; B (đang giữ) 2.000 → 2.300; C (chưa giữ) 2.500. Xếp hạng: A, C, B → A giữ; C nhận địa điểm của B (chuyển thẳng, không phế tích); B mất sơn môn.

### 6.5. Linh Điền
```
readyAt = plantedAt + growHours ;  thu hoạch = randInt(yield[0], yield[1])
seedDropChance = 0,10 × (1 + Σ GATHER_YIELD_PCT[HERB])
```

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/housing/params.json` | Tham số |
| `shared/gamedata/housing/cave_buildings.json` | Công trình động phủ, chi phí, hiệu ứng theo cấp, yêu cầu cảnh giới |
| `shared/gamedata/housing/seeds.json` | `SeedDef` (hoặc trường `seed` trong mẫu vật phẩm GD-09) |
| `shared/gamedata/housing/prestige.json` | Bảng Uy vọng §5.8 |
| `shared/maps/*.json` | `CAVE_GATE` (`gateGrade`, `realmMin`), `SECT_SITE` (`grade`, `footprintId`, `facadePrefabs`, `ruinsPrefab`) |

```jsonc
// params.json
{
  "caveClaimFee": 500, "caveRelocateFee": 2000, "caveRelocateCooldownDays": 7,
  "caveArchiveAfterDays": 60, "caveGatesPerFieldMap": [2, 4], "caveGateSafeRadius": 8,
  "homeTeleport": { "channelSec": 5, "cooldownMin": 30 },
  "caveMeditation": { "base": 1.0, "perLevel": 0.05, "perGateGrade": 0.05, "cap": 1.25 },
  "sectMeditation": { "base": 1.0, "perLevel": 0.05, "siteBonus": { "A": 0.10, "B": 0.05, "C": 0 }, "cap": 1.35 },
  "seedDropChance": 0.10,
  "sitesPerRegion": [6, 8], "siteSafeRadius": 25,
  "siteClaim": { "sectLevelMin": 2, "membersMin": 10, "fee": 20000 },
  "siteUpkeepWeekly": { "A": 6000, "B": 4000, "C": 2500 }, "siteUnpaidWeeksToRuins": 2,
  "conclave": { "dayOfMonth": 1, "hour": 20, "incumbentMult": 1.15, "prestigeWeeks": 4, "pickTimeoutMin": 10 },
  "ruinsDays": 7, "ruinsChests": 3, "abandonConfirmHours": 24
}
```

```jsonc
// seeds.json (trích)
[
  { "id": "ITM_SEED_THANH_DIEP", "harvestTemplateId": "ITM_HERB_THANH_DIEP", "growHours": 2, "yield": [3, 5], "linhDienLevelMin": 1 },
  { "id": "ITM_SEED_NGUYET_QUANG", "harvestTemplateId": "ITM_HERB_NGUYET_QUANG", "growHours": 6, "yield": [2, 3], "linhDienLevelMin": 2 },
  { "id": "ITM_SEED_BACH_NIEN", "harvestTemplateId": "ITM_HERB_BACH_NIEN", "growHours": 24, "yield": [1, 2], "linhDienLevelMin": 3 }
]
```

**Validator (CI):** mỗi bản đồ `FIELD` có 2–4 `CAVE_GATE`; `gateGrade` khớp quy tắc HM-R-011 theo khoảng cách tới `LEY_LINE`; mỗi vực có 6–8 `SECT_SITE`, mỗi bản đồ ≤ 1; vùng mặt tiền không giao lối nối bản đồ / POI / điểm spawn (GD-12); đủ 5 `facadePrefabs` + `ruinsPrefab`; không `SeedDef` nào có `harvestTemplateId` mang cờ `dangerZoneExclusive`; hệ số trần §6.1 < 1,5.

---

## 8. Mục tiêu cân bằng
| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Tỷ lệ tài khoản có động phủ sau 10 giờ chơi | ≥ 60% |
| Linh Điền: tỷ trọng linh thảo thường từ trồng trọt | ≤ 30% (phần còn lại vẫn từ khám phá) |
| Số tông môn tranh mỗi địa điểm ở Đại Hội | 1,5–3 (cạnh tranh có thật nhưng không tuyệt vọng) |
| Phế tích mỗi tháng mỗi vực | 1–3 |

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/caves/gates/{poiId}` | Thông tin cửa động: phẩm, `realmMin`, số chủ, động phủ mở cho mình | HM-R-012, 021 |
| `POST /v1/accounts/me/cave` | `{ characterId, gatePoiId }` chiếm | HM-R-040 |
| `POST /v1/accounts/me/cave/relocate` | `{ characterId, gatePoiId }` | HM-R-042 |
| `GET /v1/accounts/me/cave` · `PUT .../visit-policy` · `POST .../buildings/{b}/upgrade` | Quản lý động phủ | HM-R-021, 030 |
| `POST /v1/accounts/me/cave/plots/{i}/plant` · `/harvest` | Linh Điền | HM-R-032 |
| `GET /v1/sect-sites?regionId` | Danh sách địa điểm, trạng thái, tông môn giữ, phẩm | |
| `POST /v1/player-sects/{id}/site/claim` · `/abandon` | Chiếm / bỏ sơn môn | HM-R-070, 074 |
| `GET /v1/player-sects/{id}/prestige` | Uy vọng 4 tuần, dự kiến xếp hạng Đại Hội | HM-R-080, 081 |
| `POST /v1/conclave/{regionId}/pick` | Chọn địa điểm trong lượt chọn Đại Hội | HM-R-073 |

### 9.2. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `S2C_SectSiteStates` | S→C | `{ mapId, sites: [{ siteId, state, sectId?, sectName?, emblemId?, sectLevel? }] }` |
| `C2S_EnterCave` | C→S | `{ gatePoiId, ownerAccountId }` |
| `C2S_HomeTeleport` | C→S | `{ target: 'CAVE' \| 'SECT_SITE' }` |

### 9.3. Sự kiện domain

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `site.state_changed` | `{ siteId, from, to, sectId }` | Game Server (đổi mặt tiền mọi kênh), thông báo (GD-14), GD-23 (sự kiện Phế Tích) |
| `conclave.completed` | `{ regionId, results: [{ siteId, sectId }] }` | Thông báo hệ thống, telemetry |
| `prestige.awarded` | `{ sectId, accountId, source, points }` | Tổng hợp tuần |
| Tiêu thụ `cultivation.breakthrough_succeeded`, `danger.member_resolved`, nhiệm vụ GD-15, `boss.defeated` | | Tính Uy vọng |

---

## 10. Lưu trữ

```sql
CREATE TABLE cave (
  account_id UUID PRIMARY KEY,
  gate_poi_id VARCHAR(48) NOT NULL,
  buildings JSONB NOT NULL DEFAULT '{}',
  visit_policy VARCHAR(8) NOT NULL DEFAULT 'FRIENDS',
  decorations JSONB NOT NULL DEFAULT '[]',
  claimed_at TIMESTAMPTZ NOT NULL DEFAULT now(), relocated_at TIMESTAMPTZ,
  archived BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX ix_cave_gate ON cave(gate_poi_id) WHERE archived = FALSE;

CREATE TABLE cave_plot (
  account_id UUID NOT NULL REFERENCES cave(account_id), plot_index SMALLINT NOT NULL,
  seed_template_id VARCHAR(64), planted_at TIMESTAMPTZ, ready_at TIMESTAMPTZ,
  PRIMARY KEY (account_id, plot_index)
);

CREATE TABLE sect_site (
  site_id VARCHAR(32) PRIMARY KEY, region_id VARCHAR(8) NOT NULL, map_id VARCHAR(32) NOT NULL,
  grade CHAR(1) NOT NULL, state VARCHAR(8) NOT NULL DEFAULT 'VACANT',
  sect_id UUID REFERENCES player_sect(sect_id), held_since TIMESTAMPTZ,
  upkeep_due_at TIMESTAMPTZ, unpaid_weeks SMALLINT NOT NULL DEFAULT 0,
  ruins_until TIMESTAMPTZ, ruins_former_sect_level SMALLINT, ruins_former_sect_name VARCHAR(16)
);
CREATE UNIQUE INDEX ux_site_one_per_sect ON sect_site(sect_id) WHERE sect_id IS NOT NULL;

CREATE TABLE sect_prestige_week (
  sect_id UUID NOT NULL, week_start DATE NOT NULL, points INT NOT NULL DEFAULT 0, breakdown JSONB NOT NULL DEFAULT '{}',
  PRIMARY KEY (sect_id, week_start)
);
CREATE TABLE sect_prestige_member_week (           -- áp trần theo thành viên
  sect_id UUID NOT NULL, account_id UUID NOT NULL, week_start DATE NOT NULL, source VARCHAR(24) NOT NULL, points INT NOT NULL,
  PRIMARY KEY (sect_id, account_id, week_start, source)
);
CREATE TABLE site_history (                        -- bia đá phế tích, Vạn Giới Lục
  id BIGSERIAL PRIMARY KEY, site_id VARCHAR(32) NOT NULL, sect_id UUID NOT NULL, sect_name VARCHAR(16) NOT NULL,
  held_from TIMESTAMPTZ NOT NULL, held_to TIMESTAMPTZ, end_reason VARCHAR(16)
);
```
Mã lý do tiền tệ mới (GD-13): `CAVE_CLAIM_FEE`, `CAVE_RELOCATE_FEE`, `CAVE_BUILD`, `SITE_CLAIM_FEE`, `SITE_UPKEEP`. `item_log`: `HARVEST`, `SEED_PLANTED`, `CAVE_BUILD_INPUT`.

---

## 11. UI/UX
- **Cửa động:** khi lại gần hiện tên cửa, phẩm địa mạch (1–3 ngôi sao), số đạo hữu ẩn cư; menu vào động / thăm động.
- **Màn chọn cửa động:** bản đồ thế giới đánh dấu mọi cửa đủ điều kiện với phẩm và khoảng cách tới linh mạch, hiểm địa.
- **Trong động phủ:** không gian hang động phong cách cổ phong, các khu công trình (mờ nếu chưa xây), bảng quản lý (phím `H`), Linh Điền hiện đồng hồ đếm ngược trên từng ô.
- **Sơn môn trên bản đồ:** mặt tiền theo cấp + cờ hiệu; lại gần hiện tên tông môn, cấp, phẩm địa điểm. Phế tích: bia đá kể tên tông môn cũ.
- **Sơn Môn Đại Hội:** màn hình xếp hạng theo vực (điểm, ai giữ / ai lên), lượt chọn địa điểm có đồng hồ 10 phút; thông báo toàn server khi kết thúc.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-21 | POI `CAVE_GATE`, `SECT_SITE`; `MapType` `CAVE`, `SECT_GROUNDS`; điểm mốc; vùng an toàn |
| GD-14 | Tông môn tự lập giữ sơn môn; trú địa và công trình; tông khố trả phí |
| GD-04 / GD-26 | Địa điểm đả tọa / bế quan `CAVE`, `SECT_GROUNDS`; tuổi ×0,25; động phủ qua kiếp |
| GD-10 / GD-11 | Đan Thất / Khí Thất là trạm chế tác có thưởng |
| GD-13 | Sink: phí chiếm, dời, xây, duy trì |
| GD-12 | Tinh anh canh phế tích; quái không vào vùng an toàn |
| GD-22 / GD-23 | Cơ duyên phế tích; Sơn Môn Đại Hội, sự kiện Phế Tích |
| GD-27 | `GATHER_YIELD_PCT` tăng tỷ lệ hạt giống |
| GD-19 | Trang trí động phủ (ngoại hình, cấp tài khoản) |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Hai tông môn cùng chiếm một địa điểm trống trong cùng giây | Khóa dòng `sect_site`; tông môn thứ hai nhận `409 TAKEN` |
| Tông môn đang giữ sơn môn bị giải tán | Địa điểm → `RUINS` (HM-R-074 không cần chờ 24h vì giải tán đã có xác nhận riêng) |
| Thành viên đang trong trú địa khi sơn môn mất | Không bị đẩy ra; khi ra sẽ xuất hiện ở cổng chung thành trấn |
| Khách đang thăm động phủ khi chủ đổi `visitPolicy` sang `PRIVATE` | Khách được đưa ra cửa động sau 10s |
| Chủ dời cửa động khi có khách | Khách được đưa ra cửa động cũ |
| Dữ liệu bản đồ cập nhật làm mất một `CAVE_GATE` | Động phủ ở cửa đó được dời miễn phí sang cửa gần nhất cùng bản đồ, thông báo cho chủ |
| Server khởi động lại giữa Đại Hội | Đại Hội chạy lại từ bước tính điểm với dữ liệu đã chốt lúc 20:00 (lưu snapshot điểm) |
| Tông môn không chọn địa điểm trong 10 phút | Hệ thống chọn địa điểm phẩm cao nhất còn lại (HM-R-073) |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `cave_claimed` / `cave_relocated` | gatePoiId, gateGrade, realmCode | Cửa động nào được ưa chuộng; phẩm có quá quyết định |
| `cave_building_upgraded` | building, level | Công trình nào được ưu tiên |
| `plot_harvest` | seed, hoursLate | Linh Điền có thành "việc nhà" bắt buộc không |
| `meditation_session` (GD-04) theo location | CAVE / SECT_GROUNDS / LEY_LINE | Linh mạch ngoài đồng còn được dùng không (HM-R-004) |
| `site_state` | siteId, from, to, reason | Tần suất đổi chủ, phế tích |
| `conclave_result` | regionId, contenders, changes | Cạnh tranh có lành mạnh |
| `ruins_event` | siteId, players, chestsOpened | Phế tích có hấp dẫn người chơi |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="hm-ac-001"></a>HM-AC-001 | 50 tài khoản chọn cùng cửa động | Mỗi người vào | Mỗi người vào instance động phủ riêng của mình |
| HM-AC-002 | Nhân vật `LK2`, cửa động `realmMin` `LK3` | Chiếm | Từ chối |
| HM-AC-003 | Tài khoản đã có động phủ | Chiếm cửa khác | Từ chối (dùng dời cửa) |
| HM-AC-004 | Dời cửa lần 2 trong 7 ngày | Dời | Từ chối (hồi chiêu) |
| HM-AC-005 | Ví dụ §6.1 | Tính hệ số | Động phủ 1,20; Linh Mạch Điện 1,35; cả hai < 1,5 |
| HM-AC-006 | Đả tọa trong động phủ 3600s | Báo cáo tuổi (GD-26) | Tuổi +0,25 |
| HM-AC-007 | Tĩnh Thất cấp 3, nhân vật kiếp mới `LK5` | Đả tọa | Dùng hiệu ứng cấp 2 (yêu cầu `LK4`), chưa dùng cấp 3 (`LK7`) |
| HM-AC-008 | Gieo Thanh Diệp lúc T, đăng xuất 3 giờ | Đăng nhập, thu hoạch | Được (2h), nhận 3–5 Thanh Diệp Thảo |
| HM-AC-009 | Dữ liệu `seeds.json` có hạt Ngưng Cơ Thảo | Validator | Fail |
| HM-AC-010 | Khách (bạn bè) trong động phủ `FRIENDS` | Dùng Đan Thất | Từ chối; đả tọa tính như `HUB` |
| HM-AC-011 | Nhân vật tọa hóa | Kiếp mới đạt `realmMin`, thắng thủ vệ (HM-R-045) | Vào được động phủ cũ, công trình nguyên vẹn, cây trồng dời `readyAt` đúng thời gian phong ấn |
| HM-AC-011b | Linh Thú Viên có Huyền chủng và Linh chủng | Kiếp mới thách đấu | Thú canh cửa là Huyền chủng; thắng → +1 thân mật |
| HM-AC-012 | Tài khoản không đăng nhập 61 ngày | Job | `archived = true`; đăng nhập lại → khôi phục |
| HM-AC-013 | Tông môn cấp 1 | Chiếm địa điểm | Từ chối (`sectLevelMin` 2) |
| HM-AC-014 | Tông môn đủ điều kiện, địa điểm `VACANT`, tông khố 25.000 | Chiếm | `HELD`, tông khố còn 5.000; mọi kênh của bản đồ thấy mặt tiền theo cấp tông môn |
| HM-AC-015 | Tông môn đã giữ một địa điểm | Chiếm địa điểm thứ 2 | Từ chối |
| HM-AC-016 | Nợ phí duy trì 2 tuần liên tiếp | Job tuần | Địa điểm → `RUINS`, trú địa giữ nguyên, lối vào về cổng thành trấn |
| HM-AC-017 | Ví dụ Đại Hội §6.4 | Chạy Đại Hội | A giữ; C nhận địa điểm của B (không phế tích); B mất sơn môn |
| HM-AC-018 | Tông môn giữ địa điểm rớt hạng, không ai chọn địa điểm đó | Kết thúc lượt chọn | Địa điểm → `RUINS` 7 ngày, sự kiện Phế Tích bắt đầu |
| HM-AC-019 | Địa điểm `RUINS` | 7 ngày sau | `VACANT`, bia đá ghi lịch sử, Vạn Giới Lục mục Địa danh có mục phế tích |
| HM-AC-020 | Phế tích, người chơi không thuộc tông môn nào | Mở rương | Được, rơi đồ cá nhân |
| HM-AC-021 | Thành viên làm 4 nhiệm vụ tông môn tuần | Uy vọng | Tối đa 30 (trần 3 lần × 10) |
| HM-AC-022 | Quái đuổi người chơi tới cổng sơn môn | Theo dõi | Quái dừng ở mép vùng an toàn 25m |
| HM-AC-023 | Validator bản đồ có `SECT_SITE` giao lối nối bản đồ | CI | Fail |
| HM-AC-024 | Truyền tống về động phủ khi đang trong hiểm địa | Lệnh | Từ chối |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | MVP | Beta | Sau launch |
|----------|:---:|:----:|:----------:|
| Đặt sẵn `CAVE_GATE`, `SECT_SITE` trong dữ liệu bản đồ Vực 1 (chưa kích hoạt) | ✅ | | |
| Động phủ: chiếm, dời, vào / thăm, truyền tống về nhà | | ✅ | |
| Tĩnh Thất, Linh Điền, Đan Thất, Khí Thất | | ✅ | |
| Sơn môn: chiếm, mặt tiền 5 cấp, phí duy trì | | ✅ | |
| Uy vọng, Sơn Môn Đại Hội, phế tích | | ✅ | |
| Trang trí động phủ (ngoại hình, GD-19) | | ✅ | |
| Hoang Vực: xây tự do theo lô | | | ✅ |
| Linh mạch tranh chấp, sơn môn nhiều vực | | | ✅ |

> Đặt sẵn vị trí ngay từ MVP để không phải sửa lại bố cục bản đồ khi mở tính năng ở Beta.

---

## 17. Câu hỏi mở
- [ ] **HM-Q-1:** Có cho thành viên tông môn tự lập "ẩn cư" động phủ quanh sơn môn của mình (cửa động đặc biệt cạnh sơn môn) không?
- [ ] **HM-Q-2:** Đại Hội 1 lần / tháng có quá thưa / quá dày? Cân nhắc 2 tuần một lần khi server đông.
- [ ] **HM-Q-3:** Linh Điền có cần sự kiện ngẫu nhiên (sâu hại, linh vũ tưới) để thú vị hơn hay giữ đơn giản để tránh "việc nhà"?
- [ ] **HM-Q-4:** Phí chiếm cửa động 500 có quá thấp khiến ai cũng chiếm "cho vui"? (Không thiếu đất nên chấp nhận được.)
- [ ] **HM-Q-5:** Có nên cho tông môn chọn **phong cách mặt tiền** (2–3 kit kiến trúc) để đỡ trùng lặp khi nhiều sơn môn cùng cấp?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-22 | — | Bản nháp đầu theo quyết định "tầng 1 + 2": cửa động trên bản đồ + động phủ instance (phasing, thuộc tài khoản, 4 công trình, Linh Điền), địa điểm sơn môn trên bản đồ + mặt tiền theo cấp, chiếm / duy trì, Uy vọng, Sơn Môn Đại Hội, phế tích; hệ số đả tọa có trần dưới linh mạch; công thức + ví dụ, API, DB, nghiệm thu |
| v0.2 | 2026-10-25 | — | Trỏ telemetry chuẩn GD-20 |
| v0.3 | 2026-10-25 | — | Đợt rà nhất quán: HM-R-030 thành **năm** công trình khớp enum `CaveBuilding` (thêm Linh Thú Viên `THU_VIEN` vào bảng §5.3 và §6.2, số liệu theo GD-25 SB-R-060) |

