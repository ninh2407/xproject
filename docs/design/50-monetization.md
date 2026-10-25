# GD-19 — Monetization

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-19 · Prefix ID: `MZ` |
| Owner | Director + Game Designer + Server Dev (thanh toán) |
| Trạng thái | 🟡 Bản nháp v0.2 |
| Cần trước | Phase 5b / Beta 2 (cửa hàng cơ bản + thanh toán, PLAN Phase 5b); dữ liệu ngoại hình có thể nhập từ Alpha (`phase: BETA`) |
| Quyết định nền | PLAN §4.19, **D-17** (không bán bằng tiền thật thứ giảm rủi ro chết / kéo dài thọ), D-12 (1 nhân vật sống / tài khoản / server), D-22 (đa nền tảng §5.8) |
| Liên quan | [PLAN §4.19, §5.8](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-13 Kinh tế](19-economy.md) (EC-R-001) · [GD-09 Vật phẩm](15-items-equipment.md) (IT-R-005, `COSMETIC_UNLOCK`, `bag_expansion`) · [GD-29 RNG](05-rng-fairness.md) (loại E) · [GD-25 Linh thú](26-spirit-beasts.md) (SB-R-005) · [GD-30 Động phủ](28-caves-sect-sites.md) (trang trí) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-14 Xã hội](20-social.md) · [GD-15 Nhiệm vụ](21-quests.md) · [TD-01](../tech/00-tdd.md) (TA-R-164) · [TD-08 Bảo mật](../tech/07-security.md) §8 · [TD-04 API](../tech/03-api.md) · [TD-03 Database](../tech/02-database.md) |

> Mô hình **free-to-play, không pay-to-win**. Tiền thật chỉ mua **ngoại hình** và **tiện ích không ảnh hưởng sức mạnh, tuổi thọ hay rủi ro chết**. Vật phẩm mua bằng tiền thật **gắn với tài khoản, giữ qua mọi kiếp** — trong game một mạng, đây là lý do mua rõ ràng nhất ("mua một lần, theo ngươi qua mọi kiếp").

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-19 | Mục |
|--------------|-----------------|-----|
| GD-09 | Danh mục `COSMETIC_UNLOCK`, mở rộng túi bằng Tiên Ngọc (trần) | §3, §5.2 |
| GD-13 | Ranh giới tuyệt đối Tiên Ngọc ↔ linh thạch; `JADE` ledger | §5.1, §10 |
| GD-29 | Có / không có loại E (cosmetic ngẫu nhiên) ở launch | §5.6 |
| GD-30 | Trang trí động phủ là cosmetic tài khoản | §5.2 |
| GD-15 | Nhiệm vụ mùa của Vấn Đạo Lệnh | §5.4 |
| TD-04 / TD-08 | API cửa hàng, biên lai, hoàn tiền | §9, §10 |

---

## 1. Mục tiêu

**Trụ cột:** không làm mất P1 *Mỗi kiếp một câu chuyện* và P3 *Cái chết có ý nghĩa*: tiền thật không được mua được một kiếp dễ hơn.

- Doanh thu bền vững từ **ngoại hình + Vấn Đạo Lệnh** — đủ nuôi team nhỏ vận hành lâu dài (PLAN §6.7: mỗi trang phục là 1 mesh trên bộ xương chung → rẻ để làm).
- Người không trả tiền **chơi được trọn vẹn mọi nội dung**; người trả tiền chỉ "đẹp hơn / tiện hơn một chút", không mạnh hơn.
- Minh bạch, không thao túng: không ép mua bằng thời hạn giả, không tỷ lệ mù mờ, có giới hạn chi tiêu.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="mz-r-001"></a>**MZ-R-001** | **Không bán bằng tiền thật / Tiên Ngọc** bất cứ thứ gì ảnh hưởng: chỉ số, sát thương, tốc độ tu luyện, tỷ lệ (đột phá, chế tác, bắt thú, rơi đồ, cơ duyên), thọ nguyên, rủi ro chết, lượt roll thiên phú, ô Tàng Bảo Các, ô Linh Thú Viên, trứng / linh thú, nguyên liệu, đan dược, linh thạch (D-17, IT-R-005, SB-R-005). |
| <a id="mz-r-002"></a>**MZ-R-002** | **Tiên Ngọc (`JADE`) tách biệt hoàn toàn** khỏi kinh tế linh thạch: không đổi qua lại dưới mọi hình thức, không giao dịch, không đấu giá; vật phẩm mua bằng `JADE` luôn `bind = ACCOUNT` (GD-13 EC-R-001). |
| <a id="mz-r-003"></a>**MZ-R-003** | Mọi giao dịch tiền thật được **xác minh phía server** (biên lai cửa hàng / webhook có chữ ký) trước khi cấp quyền; quyền sở hữu nằm ở **API Service**, đồng bộ giữa web và app (TA-R-164, SEC-R-090..091). |
| <a id="mz-r-004"></a>**MZ-R-004** | Mọi thay đổi `JADE` và quyền sở hữu cosmetic ghi ledger trong cùng transaction, idempotent (TD-01 §7, §8). |
| <a id="mz-r-005"></a>**MZ-R-005** | Vật phẩm ngẫu nhiên mua bằng tiền thật (nếu có) tuân **GD-29 §5.6 loại E**: tỷ lệ chính xác công khai, bảo hiểm cứng theo tài khoản, không trùng vô dụng. |

---

## 2. Tổng quan

```
 Tiền thật ──(web: cổng thanh toán · app: Apple / Google IAP)──► API xác minh ──► Tiên Ngọc (JADE, tài khoản)
                                                                                  │
               ┌──────────────────────────────────────────────────────────────────┼─────────────────────┐
               ▼                                  ▼                               ▼                     ▼
       Vạn Tượng Các (cửa hàng ngoại hình)   Vấn Đạo Lệnh (mùa)            Tiện ích được phép       Quà tặng bạn bè
       trang phục · hiệu ứng kiếm / VFX      nhánh miễn phí + trả phí       mở rộng túi (có trần)   (cosmetic, có hạn chế)
       ngự kiếm skin · động phủ · khung chat  chỉ cosmetic + tiện ích       đổi tên, đổi ngoại hình
       biểu cảm · danh hiệu trang trí          nhiệm vụ mùa không ép cày    ô nhân vật ở server khác
               └──────────────────────────────────► Cosmetic tài khoản (giữ qua mọi kiếp, mọi nhân vật)
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
type CosmeticCategory =
  | 'OUTFIT'            // trang phục toàn thân (mesh thay thế, PLAN §6.7)
  | 'OUTFIT_DYE'        // bảng màu cho trang phục
  | 'WEAPON_SKIN'       // ngoại hình vũ khí theo WeaponType
  | 'SKILL_VFX'         // đổi màu / hình VFX kỹ năng — chỉ hình ảnh, KHÔNG đổi kích thước vùng cảnh báo / hitbox
  | 'FLIGHT_SKIN'       // ngoại hình ngự kiếm (GD-31) — cùng tốc độ
  | 'BEAST_SKIN'        // ngoại hình linh thú (khác dị sắc biến dị GD-25)
  | 'CAVE_DECOR'        // trang trí động phủ (GD-30 HM-R-034)
  | 'CHAT_FRAME' | 'NAMEPLATE' | 'EMOTE' | 'TITLE_DECOR';

interface CosmeticDef {                    // shared/gamedata/monetization/cosmetics.json
  id: string;                              // COS_*
  category: CosmeticCategory;
  nameKey: string; descKey: string;
  rarityLabel: 'THUONG' | 'HIEM' | 'TRUYEN_THUYET';   // chỉ để hiển thị khung, không ảnh hưởng gameplay
  assetRef: string;                        // mesh / VFX / decor id
  weaponType?: WeaponType;
  source: CosmeticSource[];                // nguồn có được (một cosmetic có thể có nhiều nguồn)
  phase?: 'ALPHA' | 'BETA' | 'LAUNCH';
}
type CosmeticSource =
  | { kind: 'SHOP'; offerId: string }
  | { kind: 'PASS'; seasonId: string; track: 'FREE' | 'PREMIUM'; level: number }
  | { kind: 'ACHIEVEMENT'; achievementId: string }     // nhiều cosmetic có nguồn trong game (MZ-R-020)
  | { kind: 'EVENT'; eventId: string };

interface ShopOffer {                      // shared/gamedata/monetization/offers.json
  id: string;                              // OFR_*
  grants: { cosmeticIds?: string[]; utility?: UtilityGrant };
  priceJade: number;
  availability: { from?: string; until?: string; rotation?: 'PERMANENT' | 'WEEKLY' | 'SEASONAL' };
  limitPerAccount?: number;
}
type UtilityGrant =
  | { kind: 'BAG_EXPANSION'; slots: number }          // trần MZ-R-031
  | { kind: 'RENAME_CHARACTER' } | { kind: 'APPEARANCE_CHANGE' }
  | { kind: 'EXTRA_SERVER_SLOT' };                    // ô nhân vật ở server khác (D-12 giữ nguyên)

interface JadePack {                       // shared/gamedata/monetization/jade_packs.json
  id: string;                              // JADE_*
  jade: number; bonusJade: number;         // bonus chỉ ở lần mua đầu (MZ-R-041)
  priceByChannel: { WEB_VND: number; APPLE_TIER: string; GOOGLE_SKU: string };
}

interface SeasonPassDef {                  // Vấn Đạo Lệnh
  seasonId: string;                        // SS_2027_01
  startsAt: string; endsAt: string;        // 8–10 tuần
  levels: number;                          // 50
  xpPerLevel: number;
  premiumPriceJade: number;
  rewards: { level: number; track: 'FREE' | 'PREMIUM'; cosmeticId?: string; jade?: number; utility?: UtilityGrant }[];
  questPoolId: string;                     // nhiệm vụ mùa (GD-15)
}

interface Entitlement {                    // quyền sở hữu cosmetic / tiện ích (tài khoản)
  accountId: string; cosmeticId: string; source: string; grantedAt: string; revokedAt: string | null;
}
```

---

## 4. Ví (Tiên Ngọc)
- Ví `JADE` thuộc **tài khoản** (`currency_wallet` owner `ACCOUNT`, GD-09), dùng cho mọi nhân vật / mọi kiếp.
- Tiên Ngọc chia hai phần để xử lý hoàn tiền / chính sách cửa hàng: **mua** (`paid`) và **tặng** (`bonus`, từ Vấn Đạo Lệnh, sự kiện, bù đắp). Khi tiêu: trừ `bonus` trước (MZ-R-042).

---

## 5. Quy tắc

### 5.1. Ranh giới tuyệt đối
- <a id="mz-r-010"></a>**MZ-R-010** **Bảng được phép / cấm** (danh sách đóng; thêm mục mới cần ADR và review Director): Ngoại hình phi kiếm: nhóm `COS_FLIGHT_*`, không có trường chỉ số (GD-31).

| Hạng mục | Bán bằng Tiên Ngọc? | Lý do |
|----------|:---:|-------|
| Trang phục, nhuộm màu, ngoại hình vũ khí | ✅ | Chỉ hình ảnh |
| VFX kỹ năng (màu / hình) | ✅ | Không đổi hitbox / vùng cảnh báo (GD-12 EN-R-070 vẫn đúng) |
| Ngoại hình ngự kiếm, ngoại hình linh thú | ✅ | Cùng tốc độ / chỉ số |
| Trang trí động phủ, khung chat, bảng tên, biểu cảm, trang trí danh hiệu | ✅ | Chỉ hình ảnh |
| Mở rộng túi (có trần, cũng có trong game) | ✅ có điều kiện | Tiện lợi; trần MZ-R-031; túi hiểm địa không đổi |
| Đổi tên nhân vật, đổi ngoại hình cơ thể | ✅ | Tiện ích |
| Ô nhân vật ở **server khác** | ✅ | Không phá D-12 (vẫn 1 nhân vật sống mỗi server) |
| Vấn Đạo Lệnh (nhánh trả phí) | ✅ | Chỉ cosmetic + Tiên Ngọc tặng + tiện ích ở trên |
| Ô nhân vật **thêm trên cùng server** | ❌ | Phá D-12 |
| Hồi sinh, thọ nguyên, Diên Thọ Đan, Thế Thân Phù, Truyền Tống Phù | ❌ | D-17 |
| Lượt roll thiên phú, khóa dòng roll, xuất thân | ❌ | D-17 (chỉ qua Luân Hồi) |
| Ô Tàng Bảo Các, ô Linh Thú Viên, ô Di Tàng | ❌ | Di sản qua kiếp phải giành bằng chơi |
| Linh thạch, nguyên liệu, đan, trang bị, trứng, linh thú, mảnh ngọc giản | ❌ | Sức mạnh / kinh tế (EC-R-001, SB-R-005) |
| Tăng tốc tu luyện, tăng EXP, tăng tỷ lệ rơi / chế tác / bắt thú / cơ duyên | ❌ | Sức mạnh |
| Bỏ qua hồi chiêu, mua lượt (Tàn Hồn, khảo hạch, bắt boss, hiểm địa) | ❌ | Sức mạnh / rủi ro |
| Giảm sát nghiệp, xóa Thiên phạt, miễn PvP | ❌ | Phá luật PvP (GD-32) |
| Tăng sức chứa tông môn / trú địa | ❌ | Lợi thế xã hội–sức mạnh |

- <a id="mz-r-011"></a>**MZ-R-011** Validator (MZ-V-01) chặn mọi `ShopOffer` / phần thưởng Vấn Đạo Lệnh cấp thứ ngoài các dòng ✅; `SKILL_VFX` không được có trường thay đổi kích thước / thời gian / hình dạng logic.

### 5.2. Cosmetic
- <a id="mz-r-020"></a>**MZ-R-020** **Không độc quyền tiền thật cho mọi thứ:** ít nhất 30% cosmetic mỗi mùa có nguồn **trong game** (thành tựu, sự kiện, Vấn Đạo Lệnh nhánh miễn phí); danh hiệu gắn thành tích (Tạc Thiên Tôn Giả, Nghịch Thiên, Bia Vạn Cổ…) **không bao giờ bán**.
- <a id="mz-r-021"></a>**MZ-R-021** Cosmetic là **quyền sở hữu cấp tài khoản** (`entitlement`), dùng cho mọi nhân vật, giữ qua mọi kiếp (GD-26 LC-R-080); không rơi khi chết, không vào Di Tàng, không giao dịch.
- <a id="mz-r-022"></a>**MZ-R-022** Trang phục áp **lên** trang bị (lớp hiển thị); có tùy chọn "hiện trang bị thật" khi người khác xem chi tiết. Hiển thị cảnh giới / tông môn trên bảng tên không bị cosmetic che.
- <a id="mz-r-023"></a>**MZ-R-023** Cosmetic không được làm khó đọc gameplay: không trong suốt / tàng hình, không che vùng cảnh báo, kích thước trong giới hạn bộ xương chuẩn (validator kích thước bounding box).

### 5.3. Tiện ích
- <a id="mz-r-030"></a>**MZ-R-030** Tiện ích chỉ là **rút ngắn công sức không ảnh hưởng cân bằng**; mọi tiện ích bán đều có đường đạt được trong game hoặc không mang lợi thế (đổi tên).
- <a id="mz-r-031"></a>**MZ-R-031** **Mở rộng túi:** tổng tối đa `bagMaxExpansion` (40, GD-09) = tối đa **20** ô mua bằng Tiên Ngọc + 20 ô đạt được trong game (thành tựu / Luân Hồi — bổ sung GD-26). Túi hiểm địa (`DANGER_BAG`) và kho không mở rộng bằng tiền.

### 5.4. Vấn Đạo Lệnh (battle pass theo mùa)
- <a id="mz-r-040"></a>**MZ-R-040** Mùa dài 8–10 tuần, 50 cấp. **Nhánh miễn phí** cho mọi người; **nhánh trả phí** mua bằng Tiên Ngọc. Phần thưởng chỉ gồm: cosmetic, Tiên Ngọc tặng (hoàn lại một phần giá — tổng ≤ 80% giá nhánh trả phí), tiện ích hợp lệ (MZ-R-010).
- <a id="mz-r-041"></a>**MZ-R-041** **Điểm mùa** từ **nhiệm vụ mùa** (GD-15, nhóm `QW_SEASON_*`): đa dạng hoạt động (khám phá, chế tác, cơ duyên, tông môn, PvP tùy chọn), **không có nhiệm vụ bắt buộc PvP hay hiểm địa**; mỗi tuần mở một lô, **tích dồn** (không mất nếu bỏ lỡ một tuần). Mục tiêu: hoàn thành 50 cấp với ~5–6 giờ chơi / tuần.
- <a id="mz-r-042"></a>**MZ-R-042** Không bán cấp Vấn Đạo Lệnh bằng tiền thật ở launch (tránh "trả tiền bỏ qua chơi"); cân nhắc ở 2 tuần cuối mùa (MZ-Q-1).
- <a id="mz-r-043"></a>**MZ-R-043** Cosmetic Vấn Đạo Lệnh có thể quay lại cửa hàng sau ≥ 1 năm (không "độc quyền vĩnh viễn" tạo FOMO quá mức), trừ cosmetic gắn kỷ niệm mùa (khung, danh hiệu trang trí mùa).

### 5.5. Gói Tiên Ngọc, giá
- <a id="mz-r-050"></a>**MZ-R-050** Gói Tiên Ngọc 5–7 mức; **thưởng lần đầu** mỗi mức (`bonusJade`) một lần mỗi tài khoản. Không có gói "giảm giá chỉ còn 10 phút" (không thao túng thời gian).
- <a id="mz-r-051"></a>**MZ-R-051** Giá hiển thị **bằng tiền thật tương đương** cạnh giá Tiên Ngọc trong cửa hàng (người chơi biết mình tiêu bao nhiêu); số Tiên Ngọc trong gói khớp được các giá vật phẩm phổ biến (tránh "thừa lẻ" cố ý).
- <a id="mz-r-052"></a>**MZ-R-052** Giá theo kênh: web (VND qua cổng thanh toán nội địa) và app (bậc giá Apple / SKU Google, đã gồm phí nền tảng). Một gói có thể có số Tiên Ngọc **như nhau** trên mọi kênh; chênh lệch giá do phí nền tảng chấp nhận theo chính sách cửa hàng ứng dụng (MZ-Q-2).

### 5.6. Cosmetic ngẫu nhiên (loại E, GD-29)
- <a id="mz-r-060"></a>**MZ-R-060** **Đề xuất: không có cosmetic ngẫu nhiên ở launch.** Mọi cosmetic bán trực tiếp giá rõ ràng. Lý do: rủi ro pháp lý / nhận thức "cờ bạc", trái tinh thần minh bạch GD-29, và team nhỏ không cần.
- <a id="mz-r-061"></a>**MZ-R-061** Nếu sau launch mở (cần ADR): tuân GD-29 RF-R-060..062 — tỷ lệ chính xác 2 chữ số thập phân, bảo hiểm cứng hiển thị bộ đếm (theo tài khoản), trùng → đổi thành tiền tệ cosmetic, chỉ cosmetic, kiểm tra quy định thị trường.

### 5.7. Bảo vệ người chơi
- <a id="mz-r-070"></a>**MZ-R-070** **Giới hạn chi tiêu:** người chơi tự đặt hạn mức tháng; mặc định cho tài khoản khai báo **dưới 18 tuổi** là hạn mức thấp (tham số) và cần xác nhận lại sau mỗi lần mua. Áp dụng các quy định hiện hành về người chơi chưa thành niên (thời gian chơi, chi tiêu) — **cần tư vấn pháp lý** (MZ-Q-3).
- <a id="mz-r-071"></a>**MZ-R-071** Không có nút "mua nhanh một chạm" cho gói > mức nhỏ; mỗi giao dịch có màn xác nhận ghi số tiền thật.
- <a id="mz-r-072"></a>**MZ-R-072** Không quảng cáo cửa hàng chen vào khoảnh khắc nhạy cảm (sau khi chết, màn tổng kết kiếp, khi đang hấp hối…).

### 5.8. Mua hàng & quyền sở hữu
- <a id="mz-r-080"></a>**MZ-R-080** **Web:** cổng thanh toán nội địa (thẻ ngân hàng / QR chuyển khoản, ví điện tử; thẻ cào nhà mạng nếu đối tác hỗ trợ) — chỉ cấp Tiên Ngọc khi nhận **webhook đã xác minh chữ ký** (SEC-R-091). Danh sách đối tác cụ thể chốt khi có giấy phép phát hành và hợp đồng (MZ-Q-4).
- <a id="mz-r-081"></a>**MZ-R-081** **App (Capacitor):** mua Tiên Ngọc **bắt buộc qua Apple / Google IAP** cho vật phẩm số (chính sách cửa hàng ứng dụng); server xác minh biên lai (SEC-R-090), lưu `purchase_receipt`. Không dẫn người dùng app sang thanh toán web theo cách trái chính sách cửa hàng.
- <a id="mz-r-082"></a>**MZ-R-082** **Đồng bộ:** Tiên Ngọc và cosmetic thuộc **tài khoản** ở API Service — mua ở web dùng ở app và ngược lại (trong phạm vi chính sách cửa hàng ứng dụng cho phép; MZ-Q-2).
- <a id="mz-r-083"></a>**MZ-R-083** **Hoàn tiền:** nhận thông báo hoàn tiền / chargeback (Apple / Google server notification, cổng web) → trừ Tiên Ngọc tương ứng (có thể âm); nếu đã tiêu: thu hồi cosmetic mua sau giao dịch đó hoặc khóa cửa hàng tới khi cân bằng; lặp lại / gian lận → TD-08 thang xử phạt. Không bao giờ thu hồi thứ thuộc gameplay (vì không có thứ gameplay nào mua bằng tiền).
- <a id="mz-r-084"></a>**MZ-R-084** **Quà tặng:** tặng cosmetic / Vấn Đạo Lệnh cho **bạn bè đã kết bạn ≥ 7 ngày**, người tặng có lịch sử nạp ≥ 7 ngày; hạn mức tặng / ngày; không tặng Tiên Ngọc (tránh RMT / rửa tiền).

---

## 6. Ví dụ
Vấn Đạo Lệnh mùa 1: nhánh trả phí 1.000 Tiên Ngọc; 50 cấp; tổng Tiên Ngọc tặng trong nhánh trả phí 700 (70% ≤ 80%, MZ-R-040) → người hoàn thành mùa mua mùa sau chỉ cần thêm 300. Nhánh miễn phí: 12 cosmetic nhỏ + 1 trang phục cấp 50.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/monetization/params.json` | Tham số |
| `shared/gamedata/monetization/cosmetics.json` | `CosmeticDef[]` (`COS_*`) |
| `shared/gamedata/monetization/offers.json` | `ShopOffer[]` (`OFR_*`) |
| `shared/gamedata/monetization/jade_packs.json` | `JadePack[]` (`JADE_*`) — `@serverOnly` các trường mã sản phẩm cửa hàng |
| `shared/gamedata/monetization/seasons/{seasonId}.json` | `SeasonPassDef` |

```jsonc
// params.json
{
  "bagExpansionJadeMax": 20,
  "passRefundJadeMaxRatio": 0.80,
  "passLevels": 50, "passWeeksMin": 8, "passWeeksMax": 10,
  "inGameCosmeticShareMin": 0.30,
  "minorMonthlySpendCapVnd": 500000,          // tham số tạm — chốt theo tư vấn pháp lý (MZ-Q-3)
  "giftFriendMinDays": 7, "giftSenderMinPayAgeDays": 7, "giftDailyMax": 3,
  "randomCosmeticsEnabled": false             // MZ-R-060
}
```

**Validator (CI):** (MZ-V-01) offer / phần thưởng chỉ cấp loại ✅ ở MZ-R-010; (MZ-V-02) `SKILL_VFX` không đổi thông số logic; (MZ-V-03) tổng `jade` tặng trong nhánh trả phí ≤ 80% giá; (MZ-V-04) ≥ 30% cosmetic của mùa có nguồn trong game; (MZ-V-05) không cosmetic nào là danh hiệu thành tích; (MZ-V-06) `randomCosmeticsEnabled = true` yêu cầu có bảng tỷ lệ + bảo hiểm hợp lệ (GD-29); (MZ-V-07) mọi vật phẩm giá `JADE` có `bind = ACCOUNT` (EC-AC-007).

---

## 8. Mục tiêu cân bằng / kinh doanh

| Chỉ tiêu | Mục tiêu |
|----------|----------|
| Tỷ lệ người chơi trả tiền (sau 30 ngày) | 3–6% (tham chiếu F2P cosmetic-only) |
| Tỷ lệ người chơi miễn phí hoàn thành ≥ 30 cấp Vấn Đạo Lệnh nhánh miễn phí | ≥ 40% người chơi hoạt động |
| Khảo sát "game có pay-to-win không" | ≥ 85% trả lời "không" |
| Tỷ lệ hoàn tiền / chargeback | < 1% giao dịch |

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai
| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `GET /v1/shop/catalog` | Danh mục offer đang bán + giá tiền thật tương đương theo kênh | MZ-R-051 |
| `GET /v1/accounts/me/wallet/jade` | Số dư `paid` / `bonus` | §4 |
| `POST /v1/shop/purchase` | `{ offerId }` + header `Idempotency-Key` → `{ entitlements, jade }` | MZ-R-010, MZ-R-004 |
| `GET /v1/accounts/me/entitlements` | Cosmetic / tiện ích đã sở hữu | MZ-R-021 |
| `PUT /v1/characters/{id}/appearance` | Chọn cosmetic đang mặc (kiểm tra sở hữu) | MZ-R-022 |
| `POST /v1/payments/web/orders` | Tạo đơn thanh toán web cho `JadePack` → URL cổng | MZ-R-080 |
| `POST /v1/payments/iap/verify` | `{ store, receipt }` từ app → xác minh, cấp Tiên Ngọc | MZ-R-081, SEC-R-090 |
| `GET /v1/season-pass/current` · `POST /v1/season-pass/premium` · `POST /v1/season-pass/claim` | Vấn Đạo Lệnh | §5.4 |
| `POST /v1/gifts` | Tặng quà | MZ-R-084 |
| `PUT /v1/accounts/me/spend-limit` | Hạn mức chi tiêu | MZ-R-070 |

### 9.2. Webhook / nội bộ
| Endpoint | Nguồn | Mục đích |
|----------|-------|----------|
| `POST /webhooks/payments/{provider}` | Cổng thanh toán web | Xác nhận thanh toán (xác minh chữ ký) |
| `POST /webhooks/apple/notifications` · `/webhooks/google/rtdn` | Apple / Google | Gia hạn / hoàn tiền / thu hồi |
| `POST /admin/v1/jade/adjust` | GM | Bù đắp (`bonus`), ghi `admin_audit` |

### 9.3. Sự kiện domain
| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `monetization.purchase_completed` | `{ accountId, offerId \| packId, channel }` | Telemetry, nhiệm vụ "lần đầu" |
| `monetization.entitlement_granted` / `_revoked` | `{ accountId, cosmeticId, source }` | Game Server làm mới ngoại hình |
| `monetization.refund_processed` | `{ accountId, receiptId, jadeDelta }` | TD-08 |

---

## 10. Lưu trữ
```sql
CREATE TABLE cosmetic_entitlement (
  account_id UUID NOT NULL, cosmetic_id VARCHAR(48) NOT NULL, source VARCHAR(48) NOT NULL,
  granted_at TIMESTAMPTZ NOT NULL DEFAULT now(), revoked_at TIMESTAMPTZ,
  PRIMARY KEY (account_id, cosmetic_id)
);
CREATE TABLE character_appearance (
  character_id UUID PRIMARY KEY, slots JSONB NOT NULL DEFAULT '{}', updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE season_pass_progress (
  account_id UUID NOT NULL, season_id VARCHAR(16) NOT NULL, premium BOOLEAN NOT NULL DEFAULT FALSE,
  xp INT NOT NULL DEFAULT 0, claimed JSONB NOT NULL DEFAULT '[]',
  PRIMARY KEY (account_id, season_id)
);
CREATE TABLE payment_order (                         -- đơn web
  id UUID PRIMARY KEY, account_id UUID NOT NULL, pack_id VARCHAR(32) NOT NULL, provider VARCHAR(24) NOT NULL,
  amount_vnd BIGINT NOT NULL, status VARCHAR(12) NOT NULL, provider_ref VARCHAR(128),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), paid_at TIMESTAMPTZ
);
CREATE TABLE spend_limit (account_id UUID PRIMARY KEY, monthly_cap_vnd BIGINT, is_minor BOOLEAN NOT NULL DEFAULT FALSE, updated_at TIMESTAMPTZ);
CREATE TABLE gift_log (id BIGSERIAL PRIMARY KEY, from_account UUID NOT NULL, to_account UUID NOT NULL, offer_id VARCHAR(32) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now());
-- purchase_receipt (biên lai app): TD-08 §8 / TD-03 module security
```
`currency_ledger` (owner `ACCOUNT`, currency `JADE`) mã lý do mới: `JADE_PURCHASE_WEB`, `JADE_PURCHASE_IAP`, `JADE_FIRST_BONUS`, `JADE_PASS_REWARD`, `JADE_SPEND_SHOP`, `JADE_SPEND_PASS`, `JADE_REFUND_CLAWBACK`, `JADE_GM_COMPENSATION`. Module sở hữu: `economy` (cửa hàng) + `security` (biên lai) — đề xuất module mới `monetization` (TD-01 §5.1).

---

## 11. UI/UX
- **Vạn Tượng Các** (cửa hàng): xem thử trên nhân vật của mình (xoay, đổi vũ khí, xem VFX), giá Tiên Ngọc + tiền thật tương đương, tag "Có thể đạt trong game" khi có nguồn khác.
- **Vấn Đạo Lệnh:** thanh cấp, hai hàng thưởng, nhiệm vụ mùa theo tuần (tích dồn), không đếm ngược gây áp lực ngoài ngày kết thúc mùa.
- **Tủ đồ ngoại hình:** áp dụng cho từng nhân vật; ghi chú "Giữ qua mọi kiếp".
- **Cài đặt chi tiêu:** hạn mức tháng, lịch sử giao dịch, yêu cầu hỗ trợ hoàn tiền.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-13 | `JADE` tách biệt (EC-R-001), không vào dashboard linh thạch |
| GD-09 | `COSMETIC_UNLOCK`, mở rộng túi (trần) |
| GD-26 | Cosmetic giữ qua kiếp; thêm nguồn trong game cho 20 ô túi (unlock / thành tựu) |
| GD-29 | Loại E tắt ở launch |
| GD-25 / GD-31 | Ngoại hình linh thú / ngự kiếm chỉ hình ảnh |
| GD-30 | Trang trí động phủ |
| GD-15 | Nhiệm vụ mùa |
| GD-14 | Danh hiệu thành tích không bán; quà tặng cần bạn bè |
| TD-04 / TD-08 / TD-03 | API, biên lai, hoàn tiền, bảng |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Webhook đến trước khi người dùng quay lại trang | Cấp Tiên Ngọc theo webhook (idempotent theo `provider_ref`) |
| Webhook đến 2 lần | Idempotent, cấp một lần |
| Hoàn tiền sau khi đã tiêu hết Tiên Ngọc | Số dư âm; cửa hàng khóa tới khi cân bằng; lặp lại → TD-08 |
| Cosmetic bị gỡ khỏi bán | Người đã sở hữu giữ nguyên |
| Mua trên app, đăng nhập web | Thấy Tiên Ngọc / cosmetic (MZ-R-082) |
| Nhân vật chết | Cosmetic không mất (MZ-R-021) |
| Tài khoản bị khóa vì gian lận thanh toán | Tiên Ngọc / cosmetic đóng băng theo TD-08 |

---

## 14. Telemetry

> **Tên sự kiện chuẩn, trường, lấy mẫu: [GD-20 §4](51-analytics.md)** — bảng dưới giữ cột "câu hỏi trả lời"; khi tên vênh, theo GD-20 (TM-R-020).
| Sự kiện | Trường | Câu hỏi |
|---------|--------|---------|
| `shop_view` / `shop_preview` / `shop_purchase` | offerId, category, channel | Cái gì bán chạy, kênh nào |
| `pass_progress` | seasonId, level, premium, hoursPlayed | Có ép cày không (§8) |
| `refund` | channel, reason | Tỷ lệ hoàn tiền |
| `p2w_survey` | answer | Nhận thức pay-to-win |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="mz-ac-001"></a>MZ-AC-001 | Offer cấp `ITM_DIEN_THO_DAN` | Validator | Fail (MZ-V-01) |
| MZ-AC-002 | Offer cấp 25 ô túi | Validator | Fail (trần 20, MZ-R-031) |
| MZ-AC-003 | Mua offer cosmetic với `Idempotency-Key` gửi 2 lần | API | Trừ Tiên Ngọc một lần, `entitlement` một dòng |
| MZ-AC-004 | Webhook thanh toán chữ ký sai | API | Từ chối, không cấp Tiên Ngọc |
| MZ-AC-005 | Biên lai IAP giả | `iap/verify` | Từ chối sau khi xác minh với cửa hàng thất bại |
| MZ-AC-006 | Mua cosmetic ở app | Đăng nhập web | Có cosmetic |
| MZ-AC-007 | Hoàn tiền 500 Tiên Ngọc khi số dư 200 | Xử lý | Số dư −300, cửa hàng khóa |
| MZ-AC-008 | Nhân vật tọa hóa | Kiếp mới | Cosmetic + Tiên Ngọc giữ nguyên |
| MZ-AC-009 | Mùa có 20 cosmetic, 5 có nguồn trong game | Validator | Fail (< 30%, MZ-V-04) |
| MZ-AC-010 | Tiêu 300 Tiên Ngọc khi có 100 bonus + 500 paid | Mua | bonus 0, paid 300 |
| MZ-AC-011 | Tài khoản dưới 18 đã đạt hạn mức tháng | Mua | Từ chối, hiện hạn mức |
| MZ-AC-012 | Tặng cosmetic cho người mới kết bạn 2 ngày | `gifts` | Từ chối |
| MZ-AC-013 | `SKILL_VFX` có trường đổi `radius` | Validator | Fail (MZ-V-02) |
| MZ-AC-014 | Bất kỳ giao dịch nào đổi `JADE` → linh thạch | Kiểm tra hợp đồng / code | Không có endpoint / mã lý do nào cho phép (EC-R-001) |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Alpha | Beta 2 | Launch | Sau launch |
|----------|:---:|:---:|:---:|:---:|
| Dữ liệu cosmetic + tủ đồ ngoại hình (nguồn trong game) | ✅ | | | |
| Cửa hàng Tiên Ngọc, thanh toán web, xác minh webhook | | ✅ | | |
| Vấn Đạo Lệnh mùa 1 | | ✅ (thử nghiệm) | ✅ | |
| IAP Apple / Google (khi có app Capacitor) | | | | ✅ |
| Quà tặng, hạn mức chi tiêu | | ✅ | | |
| Cosmetic ngẫu nhiên (loại E) | | | ❌ | Chỉ sau ADR |

---

## 17. Câu hỏi mở
- [ ] **MZ-Q-1:** Có bán cấp Vấn Đạo Lệnh ở 2 tuần cuối mùa không?
- [ ] **MZ-Q-2:** Chính sách giá / đồng bộ giữa web và app theo quy định hiện hành của Apple / Google (phí nền tảng, quy định chống dẫn sang thanh toán ngoài) — cần rà khi làm app.
- [ ] **MZ-Q-3:** Quy định hiện hành tại Việt Nam về người chơi chưa thành niên (thời gian chơi, chi tiêu), xác minh tuổi, và giấy phép trò chơi điện tử trực tuyến (G1) — **cần tư vấn pháp lý** trước khi mở thanh toán.
- [ ] **MZ-Q-4:** Đối tác cổng thanh toán web (ví điện tử, chuyển khoản QR, thẻ cào nhà mạng) và phí từng kênh.
- [ ] **MZ-Q-5:** Tên chính thức cửa hàng ("Vạn Tượng Các" là tên tạm).

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: bất biến không pay-to-win, ví Tiên Ngọc (paid / bonus), **bảng được phép / cấm**, cosmetic tài khoản giữ qua kiếp (≥ 30% có nguồn trong game), tiện ích (túi có trần 20 ô), Vấn Đạo Lệnh (mùa, hai nhánh, nhiệm vụ tích dồn, hoàn ≤ 80%), gói Tiên Ngọc + giá minh bạch, **không cosmetic ngẫu nhiên ở launch**, bảo vệ người chơi / hạn mức chi tiêu, thanh toán web + IAP, đồng bộ quyền sở hữu, hoàn tiền, quà tặng, API, DB, nghiệm thu |
| v0.2 | 2026-10-25 | — | `COS_FLIGHT_*`; Trỏ telemetry chuẩn GD-20 |

