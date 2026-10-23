# GD-14 — Xã hội

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-14 · Prefix ID: `SO` |
| Owner | Game Designer + Server Dev |
| Trạng thái | 🟡 Bản nháp v0.7 |
| Cần trước | Phase 2 (tổ đội, chat, bạn bè, Thanh Vân Tông), Beta (tông môn tự lập) |
| Quyết định nền | PLAN D-03, D-12 · PLAN §4.15 |
| Liên quan | [PLAN §4.15](../../PLAN.md) · [Glossary](../lore/glossary.md) · [GD-26 Vòng đời](03-lifecycle.md) · [GD-04 Tu luyện](10-cultivation.md) · [GD-12 Quái](18-enemies-ai.md) · [GD-28 Hiểm địa](27-danger-zones.md) · [GD-22 Cơ duyên](23-encounters.md) · [GD-13 Kinh tế](19-economy.md) · [GD-21 Open World](22-open-world.md) · [GD-10 Luyện đan](16-alchemy.md) · [GD-11 Luyện khí](17-forging.md) · GD-15 Nhiệm vụ |

> Đọc [docs/README.md §3](../README.md) trước khi triển khai. Dữ liệu ở `shared/gamedata/social/*.json` (§7).
> **Lưu ý đánh số:** một số tài liệu v0.1 trước đây ghi nhầm "GD-15 Xã hội"; mã đúng của Xã hội là **GD-14** (PLAN §8.1). GD-15 là Nhiệm vụ.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-14 | Mục |
|--------------|-----------------|-----|
| GD-04 | Tổ đội cho hộ pháp; phần thưởng hộ pháp (trả lời CU-Q-4) | §5.1, §5.7 |
| GD-12 / GD-28 | Định nghĩa tổ đội (tính công, cứu người hấp hối, vào hiểm địa) | §5.1 |
| GD-13 | Cấp bậc Thanh Vân Tông mở khóa Tông Môn Bảo Khố; cống hiến tông môn; giao dịch trực tiếp (Beta, theo GD-13 §5.7) | §5.4 |
| GD-26 | Bạn bè, tông môn tự lập gắn tài khoản; "chuyển thế đệ tử"; tin tọa hóa | §5.3, §5.5, §5.6 |
| GD-22 | Kênh hệ thống, hàng đợi thông báo toàn server, tùy chọn ẩn tên | §5.6 |
| GD-10 / GD-11 | Công trình tông môn tăng chế tác (Beta) | §5.5 |
| GD-21 | Chọn kênh theo tổ đội / bạn bè | §5.1 |

---

## 1. Mục tiêu

**Trụ cột:** P1 *Mỗi kiếp một câu chuyện*, P3 *Chiến đấu có kỹ năng* (phối hợp), P5 *Thế giới đáng khám phá*.

- Trong game một mạng, **bạn tu là phao cứu sinh**: kéo dậy khi hấp hối, hộ pháp khi đột phá, nhặt hộ di vật.
- Quan hệ **sống lâu hơn một kiếp**: bạn bè, tông môn tự lập gắn với tài khoản; người chơi quay lại sau khi chết vẫn có "nhà".
- Giao tiếp an toàn: lọc từ ngữ, chống spam / quảng cáo bán vàng, báo cáo dễ dàng.
- MVP gọn: tổ đội, chat, bạn bè, Thanh Vân Tông (NPC). Tông môn do người chơi lập ở Beta.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="so-r-001"></a>**SO-R-001** | Hai loại tông môn tách bạch: **đại tông NPC** (Thanh Vân Tông, Tạc Thiên Bang, Hợp Hoan Tông — mỗi kiếp thuộc tối đa một, gắn với **kiếp**, §5.4b) và **Tông môn tự lập** (do người chơi lập, gắn với **tài khoản**, Beta). Code dùng hai thực thể khác nhau: `NpcSectMembership` và `PlayerSect`. |
| <a id="so-r-002"></a>**SO-R-002** | Bạn bè, danh sách chặn, tư cách thành viên tông môn tự lập thuộc **tài khoản**, giữ qua mọi kiếp (GD-26 LC-R-080). |
| <a id="so-r-003"></a>**SO-R-003** | Mọi tin nhắn người chơi gửi đi qua bộ lọc server trước khi phát; không tin bộ lọc phía client. |
| <a id="so-r-004"></a>**SO-R-004** | Rơi đồ cá nhân (GD-12 EN-R-004): tổ đội **không** có luật chia đồ. |
| <a id="so-r-005"></a>**SO-R-005** | *(Sửa bởi D-21)* Không có **chiến tranh tông môn** (tông đánh tông có tổ chức) ở MVP / Beta. PvP cá nhân ở dã ngoại theo [GD-32](31-pvp-infamy.md): thành viên cùng tông tự lập không đánh nhau được trừ chế độ Sát lục. |

---

## 2. Tổng quan

```
 KIẾP (nhân vật)                                 TÀI KHOẢN (qua mọi kiếp)
 ─────────────────                               ────────────────────────
 Tổ đội (1–5)  ── hộ pháp, cứu hấp hối,          Bạn bè (≤ 100) · Danh sách chặn
                  vào hiểm địa / bí cảnh,
                  tính công hạ quái              Tông môn tự lập (Beta): cấp, công trình,
 Thanh Vân Tông (NPC) ── cấp bậc, cống hiến,        trú địa, tông khố, chức vụ
                  Tông Môn Bảo Khố                  ("chuyển thế đệ tử" khi sang kiếp mới)

 CHAT: Thế giới · Bản đồ · Tổ đội · Tông môn tự lập · Mật thoại · Hệ thống (thông báo toàn server)
 Bảng Chiêu Mộ (tìm đội) · Đánh dấu (ping) · Xem thông tin người chơi
```

---

## 3. Thực thể & kiểu dữ liệu

```ts
// ===== Tổ đội =====
interface Party {
  partyId: string;
  leaderCharacterId: string;
  memberIds: string[];                // 1..5 characterId, gồm đội trưởng
  createdAt: string;
  listingId: string | null;           // tin Chiêu Mộ đang mở
  lockedByActivity: 'DANGER_RUN' | 'DUNGEON' | null;   // không thêm / đuổi khi đang khóa
}
interface PartyInvite { inviteId: string; partyId: string | null; fromCharacterId: string; toCharacterId: string; expiresAt: string }

interface LfgListing {                // Bảng Chiêu Mộ
  listingId: string; partyId: string;
  activity: { kind: 'DUNGEON' | 'DANGER_ZONE' | 'ELITE' | 'FREE'; refId?: string };   // vd. BC02, V1_HD01
  realmMin: string; realmMax?: string; slotsOpen: number; note: string;
  createdAt: string; expiresAt: string;
}

// ===== Đại tông NPC =====
type NpcSectId = 'SECT_THANH_VAN' | 'SECT_TAC_THIEN' | 'SECT_HOP_HOAN';
type Alignment = 'CHINH' | 'TA' | 'MA' | 'NONE';      // chính đạo · tà đạo (giang hồ) · ma đạo
type NpcSectRank = 'TAP_DICH' | 'NGOAI_MON' | 'NOI_MON' | 'CHAN_TRUYEN';   // tên hiển thị khác nhau theo tông (§5.4b)
interface NpcSectMembership {         // thuộc kiếp, tối đa 1 bản ghi ACTIVE
  characterId: string; sectId: NpcSectId;
  rank: NpcSectRank;
  contributionTotal: number;          // tổng cống hiến đã kiếm (để lên bậc); số dư là currency SECT_CONTRIBUTION (GD-09)
  joinedAt: string; rebornDisciple: boolean;   // "chuyển thế đệ tử"
  state: 'ACTIVE' | 'LEFT';           // LEFT: đã phản môn (SO-R-047)
}
interface FactionReputation {         // thuộc kiếp (§5.4c)
  characterId: string; CHINH: number; TA: number; MA: number;
  dailyGained: { day: string; CHINH: number; TA: number; MA: number };
}
interface NpcSectDef {                // shared/gamedata/social/npc_sects.json
  id: NpcSectId; nameKey: string; alignment: Alignment;
  hqMapId: string; hqPoiId: string;   // tổng đàn
  joinQuestBranch: string;            // nhánh của QC_MAIN_V1_JOIN_SECT (GD-15)
  rankNameKeys: Record<NpcSectRank, string>;
  treasuryShopId: string;             // SHOP_V1_*_TREASURY
  dailyTaskPool: string[];            // QD_* (GD-15)
  perks: TalentEffect[];              // hiệu lực khi ACTIVE (GD-27 union, giới hạn §5.4b)
  signatureTechniqueId: string;       // TEC_* bán ở Bảo Khố bậc NOI_MON (GD-07)
  phase: 'ALPHA' | 'BETA';
}

// ===== Bạn bè, chặn =====
interface Friendship { accountId: string; friendAccountId: string; createdAt: string }    // lưu 2 chiều
interface FriendRequest { requestId: string; fromAccountId: string; toAccountId: string; createdAt: string }
interface BlockEntry { accountId: string; blockedAccountId: string; createdAt: string }

// ===== Chat =====
type ChatChannel = 'WORLD' | 'MAP' | 'PARTY' | 'SECT' | 'WHISPER' | 'SYSTEM';
interface ChatMessage {
  messageId: string; channel: ChatChannel;
  scopeId: string | null;             // mapId:channel | partyId | playerSectId | toCharacterId
  fromCharacterId: string | null;     // null với SYSTEM
  text: string;                       // đã lọc
  itemLinks?: { linkId: string; templateId: string; snapshot: object }[];
  sentAt: string;
}

// ===== Tông môn tự lập (Beta) =====
type SectRole = 'CHUONG_MON' | 'TRUONG_LAO' | 'CHAP_SU' | 'DE_TU';
type SectBuilding = 'DAN_PHONG' | 'LUYEN_KHI_CAC' | 'LINH_MACH_DIEN';
interface PlayerSect {
  sectId: string; name: string; emblemId: string;
  level: number; exp: number;
  leaderAccountId: string;
  treasuryStones: number;             // tông khố
  buildings: Record<SectBuilding, number>;   // cấp 0..5
  upkeepDueAt: string; createdAt: string;
}
interface PlayerSectMember { sectId: string; accountId: string; role: SectRole; merit: number; joinedAt: string }
type BenefitSource = 'NPC_SECT' | 'PLAYER_SECT' | 'WANDERER';      // §5.4e
interface CharacterBenefit { characterId: string; source: BenefitSource; switchedAt: string }
interface PlayerSectEdicts { sectId: string; edictIds: string[]; changedAt: string; effectiveFrom: string }
```

---

## 4. Máy trạng thái

**Lời mời tổ đội:** `PENDING` → `ACCEPTED` (vào đội; nếu người mời chưa có đội thì tạo đội mới) · `DECLINED` · `EXPIRED` (60s) · `CANCELLED` (đội đầy / người mời rời đội).

**Đơn gia nhập tông môn tự lập (Beta):** `PENDING` → `APPROVED` · `REJECTED` · `EXPIRED` (72h).

**Thanh Vân Tông:** chưa gia nhập → `TAP_DICH` (chỉ xuất thân Tông môn tạp dịch) / `NGOAI_MON` → `NOI_MON` → `CHAN_TRUYEN` (§5.4). Không tụt bậc.

---

## 5. Quy tắc

### 5.1. Tổ đội
- <a id="so-r-010"></a>**SO-R-010** Tổ đội 1–5 người. Mời được người chơi bất kỳ trên server (theo tên nhân vật), trừ người đã chặn mình. Lời mời hết hạn sau 60s. Đội còn 1 người thì tự giải tán.
- <a id="so-r-011"></a>**SO-R-011** Đội trưởng: mời, đuổi, chuyển quyền, mở tin Chiêu Mộ. Đội trưởng rời đội / đăng xuất quá `params.leaderOfflineSec` (300s) thì quyền chuyển cho thành viên vào đội sớm nhất còn online.
- <a id="so-r-012"></a>**SO-R-012** **Khóa đội** khi đang có lượt hiểm địa (GD-28) hoặc bí cảnh: không thêm / đuổi thành viên; rời đội được nhưng vẫn ở lại instance tới khi kết thúc lượt (GD-28 DZ-R-012).
- <a id="so-r-013"></a>**SO-R-013** Lợi ích tổ đội (định nghĩa tại tài liệu tương ứng, liệt kê để đối chiếu):

| Lợi ích | Tài liệu |
|---------|----------|
| Được tính công hạ quái khi đồng đội đóng góp và ở trong 40m | GD-12 EN-R-100 |
| Hộ pháp khi đột phá (+3 / người, tối đa +9) | GD-04 CU-R-063 |
| Cứu người hấp hối, nhặt di hài | GD-28 DZ-R-053, DZ-R-061 |
| Vào hiểm địa / bí cảnh cùng nhau; HP boss theo số người | GD-28 §5.1, GD-12 §6.3 |
| Kỹ năng hồi máu / buff lên đồng minh | GD-07 SK-R-075 |
| Hiệu ứng thiên phú lên đồng đội (vd. `PARTY_LUCK_FLAT`) trong 40m | GD-27 |

- <a id="so-r-014"></a>**SO-R-014** **Kênh:** khi vào bản đồ, ưu tiên kênh có đồng đội (GD-21 §3.3). Nút "Tới kênh đồng đội" chuyển kênh ngay nếu không trong chiến đấu (bỏ qua hồi chiêu đổi kênh 60s một lần mỗi 10 phút). Trả lời GD-21 OW-1: không tự gom đội về một kênh.
- <a id="so-r-015"></a>**SO-R-015** **Đánh dấu (ping):** Alt + click trên mặt đất / kẻ địch / vật phẩm hiện dấu cho đồng đội trong 4s; tối đa 3 dấu / 5s mỗi người.
- <a id="so-r-016"></a>**SO-R-016** **Bảng Chiêu Mộ:** đội trưởng đăng tin (hoạt động, cảnh giới, số chỗ, ghi chú ≤ 60 ký tự qua bộ lọc), tồn tại 30 phút hoặc tới khi đủ người; người chơi khác gửi yêu cầu, đội trưởng chấp nhận. Truy cập ở bảng NPC thành trấn và trong giao diện.

### 5.2. Chat
- <a id="so-r-020"></a>**SO-R-020** Kênh:

| Kênh | Phạm vi | Điều kiện gửi | Giới hạn tần suất | Độ dài |
|------|---------|---------------|-------------------|:---:|
| `WORLD` (Thế giới) | Toàn server | Cảnh giới ≥ `LK3` **và** tài khoản chơi ≥ 2 giờ | 1 tin / 30s | 120 |
| `MAP` (Bản đồ) | Bản đồ + kênh hiện tại | — | 1 tin / 5s | 200 |
| `PARTY` (Tổ đội) | Thành viên tổ đội | — | 1 tin / 1s | 200 |
| `SECT` (Tông môn) | Thành viên tông môn tự lập (Beta) | — | 1 tin / 2s | 200 |
| `WHISPER` (Mật thoại) | Một người (theo tên nhân vật đang sống) | Người nhận online và không chặn mình | 1 tin / 1s | 200 |
| `SYSTEM` (Hệ thống) | Toàn server / bản đồ | Chỉ server | §5.6 | — |

- <a id="so-r-021"></a>**SO-R-021** **Lọc nội dung** (server, SO-R-003): chuẩn hóa (bỏ dấu, viết thường, bỏ ký tự chèn, quy đổi ký tự thay thế) rồi so với danh sách từ cấm vi / en → thay bằng `***`. Ở `WORLD`/`MAP`: chặn hẳn tin chứa URL, số điện thoại, mẫu quảng cáo bán linh thạch (chống RMT), báo cho người gửi lý do.
- <a id="so-r-022"></a>**SO-R-022** **Liên kết vật phẩm:** chèn `[Tên vật phẩm]` từ túi; server lưu ảnh chụp instance 24h; người xem rê chuột thấy tooltip (GD-09). Tối đa 2 liên kết / tin.
- <a id="so-r-023"></a>**SO-R-023** Tin của người trong danh sách chặn không hiển thị ở mọi kênh (lọc ở server khi phát).
- <a id="so-r-024"></a>**SO-R-024** Lưu tin nhắn phục vụ kiểm duyệt `params.chatRetentionDays` (30 ngày), rồi xóa (chính sách riêng tư).

### 5.3. Bạn bè và chặn
- <a id="so-r-030"></a>**SO-R-030** Bạn bè theo **tài khoản**, tối đa 100; kết bạn bằng lời mời (chấp nhận / từ chối; hết hạn 7 ngày). Danh sách hiển thị nhân vật đang sống của bạn: tên, cảnh giới, kiếp thứ mấy, trạng thái online, bản đồ (trừ khi bạn bật "ẩn vị trí").
- <a id="so-r-031"></a>**SO-R-031** Thông báo bạn bè: online, đột phá cảnh giới lớn, **tọa hóa / tử vong** (kèm link tiểu sử Bia Vạn Cổ, GD-26 §5.12), bắt đầu kiếp mới.
- <a id="so-r-032"></a>**SO-R-032** Chặn theo tài khoản, tối đa 200: ẩn chat, chặn mật thoại, lời mời tổ đội, lời mời kết bạn, giao dịch (Beta). Chặn tự hủy kết bạn.

### 5.4. Thanh Vân Tông (tông môn NPC, MVP) — quy tắc chung cho mọi đại tông NPC
- <a id="so-r-040"></a>**SO-R-040** Mọi nhân vật gia nhập qua nhiệm vụ chính chương 1 `QC_MAIN_V1_JOIN_SECT` (GD-15), khả dụng từ `LK1`, nhận bậc `NGOAI_MON`. Xuất thân Tông môn tạp dịch (GD-27) bắt đầu ở bậc `TAP_DICH` và lên `NGOAI_MON` khi xong chuỗi `QC_SECT_SERVANT`.
- <a id="so-r-041"></a>**SO-R-041** **Chuyển thế đệ tử:** tài khoản có cờ `FLAG_ACC_JOINED_{SECT}` (từng gia nhập **tông đó** ở kiếp trước; `FLAG_ACC_JOINED_SECT` cũ = Thanh Vân) được bỏ qua nhiệm vụ gia nhập tông đó, vào thẳng `NGOAI_MON` khi tới tổng đàn, mang danh hiệu "Chuyển thế đệ tử" (`rebornDisciple = true`).
- <a id="so-r-042"></a>**SO-R-042** **Cấp bậc** (không tụt bậc):

| Bậc | Điều kiện | Mở khóa |
|-----|-----------|---------|
| `TAP_DICH` (Tạp dịch) | Xuất thân Tông môn tạp dịch | Nhiệm vụ tạp dịch; chưa mua Bảo Khố |
| `NGOAI_MON` (Ngoại môn) | Gia nhập | Bảo Khố cấp 1 (gồm `RCP_TRUC_CO_DAN`, GD-10 AL-R-071), nhiệm vụ tông môn hằng ngày |
| `NOI_MON` (Nội môn) | Cảnh giới ≥ `TC1` **và** `contributionTotal` ≥ 2.000 | Bảo Khố cấp 2 (ngọc giản Huyền phẩm, đan lô / búa T2), Linh mạch nội môn |
| `CHAN_TRUYEN` (Chân truyền, Beta) | Cảnh giới ≥ `KD1` **và** `contributionTotal` ≥ 10.000 | Bảo Khố cấp 3 |

- <a id="so-r-043"></a>**SO-R-043** **Cống hiến tông môn** (`SECT_CONTRIBUTION`, GD-09, GD-13 EC-R-050): kiếm từ nhiệm vụ tông môn hằng ngày (GD-15), trần 300 / ngày; số dư là tiền tệ (mất khi hết kiếp), `contributionTotal` chỉ để xét bậc (cũng thuộc kiếp).
- <a id="so-r-044"></a>**SO-R-044** `ShopEntry` của `SHOP_V1_SECT_TREASURY` có thêm trường `rankMin: NpcSectRank` (bổ sung GD-13 §3); giá Trúc Cơ Đan đan phương 600 cống hiến (≈ 2 ngày).

### 5.4b. Ba đại tông NPC (chốt SO-Q-3)
- <a id="so-r-045"></a>**SO-R-045** Có **ba** đại tông NPC, mọi quy tắc §5.4 (bậc, cống hiến, Bảo Khố có `rankMin`, nhiệm vụ hằng ngày, chuyển thế đệ tử) áp dụng chung; khác nhau ở **phe, tổng đàn, phúc lợi, tâm pháp đặc trưng, nhiệm vụ**:

| | **Thanh Vân Tông** | **Tạc Thiên Bang** | **Hợp Hoan Tông** |
|---|---|---|---|
| ID | `SECT_THANH_VAN` | `SECT_TAC_THIEN` | `SECT_HOP_HOAN` |
| Phe (`Alignment`) | `CHINH` (chính đạo) | `TA` (giang hồ tà phái, "đục trời" — không kính thiên đạo) | `MA` (ma đạo, âm nhu) |
| Tổng đàn | Thanh Vân Trấn (`V1_HUB`) | **Tạc Thiên Trại**, hầm mỏ bỏ hoang ở `V1_F03` (nơi Khoáng Tặc hoạt động) | **Hợp Hoan Cốc**, thung lũng hoa đào ở `V1_F05` (gần Hồ Yêu Động) |
| Tên bậc | Tạp dịch · Ngoại môn · Nội môn · Chân truyền | Lâu la · Bang chúng · Đầu mục · Đường chủ | Tạp dịch · Ngoại môn · Nội môn · Thánh tử / Thánh nữ |
| Lối chơi | Cân bằng: tu luyện ổn định, luyện đan | **Hiểm địa, đoạt bảo, hắc thị, luyện khí** | **Tổ đội, song tu, mị thuật, tâm cảnh** |
| Phúc lợi (khi `ACTIVE`) | Đả tọa ở linh mạch +5% tu vi; −10% thời gian ấp phiên luyện đan | Phí vào hiểm địa −20%; pha `CALM` của hiểm địa +10% thời gian (GD-28); bán vật phẩm độc quyền hiểm địa cho **Hắc Thị** (NPC trong trại) +10% giá | **Song tu:** đả tọa cùng 1 đồng đội cũng đang đả tọa trong 5m → cả hai +10% tu vi (không cộng dồn nhiều người); tâm cảnh hồi phục +20% (GD-04) |
| Tâm pháp đặc trưng (Bảo Khố, `NOI_MON`) | `TEC_THANH_VAN_KIEM` (Kim, kiếm, cân bằng) | `TEC_TAC_THIEN_CUONG` (Thổ / Kim, sát thương tăng khi máu thấp) | `TEC_HOP_HOAN_MI` (Mộc / Thủy, quạt, gây `ST_CHARMED` — quái mất mục tiêu 1,5s) |
| Bảo Khố riêng | `SHOP_V1_SECT_TREASURY` | `SHOP_V1_TAC_THIEN_TREASURY` (+ Hắc Thị `SHOP_V1_BLACK_MARKET`) | `SHOP_V1_HOP_HOAN_TREASURY` |
| Thử thách gia nhập (nhánh `QC_MAIN_V1_JOIN_SECT`) | Hạ Trúc Vương Thử hoặc vượt Trận Thử Tâm | **Đầu danh trạng:** hạ Khoáng Ma Bạo Chúa (BC01) hoặc nộp 3 vật phẩm từ hiểm địa | **Trận Mị Tâm:** thử thách tâm cảnh (một phiên `HEART_TRIAL` rút gọn, GD-22) — không cần chiến đấu |
| Nhiệm vụ hằng ngày tiêu biểu | Tuần tra, hái thuốc, luyện đan nộp tông | Áp tải hàng lậu (hộ tống), đoạt bảo trong hiểm địa nhất giai, thu "phí bảo kê" từ quái mỏ | Hái hoa đào linh, hộ tống đồng môn (tổ đội), luyện đan tâm cảnh |
| Pha nội dung | MVP | **Beta** | **Beta** |

- <a id="so-r-045b"></a>**SO-R-045B** **NPC lãnh đạo mang danh hiệu phe** (lưu ý nội dung): Tạc Thiên Bang — Bang chủ mang *"Tạc Thiên Đại Đế"*, Trưởng lão / Đường chủ mang *"Tạc Thiên Chí Tôn"* / *"Tạc Thiên Tôn Giả"* (chính là các danh hiệu người chơi có thể đạt, GD-32 PK-R-056) để người chơi có mục tiêu và cảm giác thành tựu; lời thoại NPC phản ứng theo danh hiệu của người chơi. Thanh Vân Tông và Hợp Hoan Tông áp dụng cùng nguyên tắc khi các danh hiệu phe của họ được thiết kế.
- <a id="so-r-046"></a>**SO-R-046** **Đường tới Trúc Cơ luôn mở ở mọi tông:** Bảo Khố cấp 1 của **cả ba** tông đều bán đan phương `RCP_TRUC_CO_DAN` (600 cống hiến, GD-10 AL-R-071). Chọn tông là chọn **phong cách**, không phải bẫy tiến trình.
- <a id="so-r-047"></a>**SO-R-047** **Một tông mỗi kiếp; phản môn:** nhân vật chỉ có một tư cách `ACTIVE`. Rời tông (tại tổng đàn, xác nhận 2 bước): mất bậc và toàn bộ số dư cống hiến, `state = LEFT`, hồi chiêu 7 ngày thực trước khi gia nhập tông khác (vào ở `NGOAI_MON`, phải làm thử thách gia nhập của tông mới), **không** quay lại tông cũ trong kiếp này. Tông cũ ghi nhân vật là "phản đồ" (NPC tông cũ đổi lời thoại; không có truy sát ở MVP).
- <a id="so-r-048"></a>**SO-R-048** **Phe** (`alignment` của nhân vật = phe của tông `ACTIVE`, `NONE` nếu chưa gia nhập / đã rời) là điều kiện dùng được trong cơ duyên và nhiệm vụ (GD-22 `conditions.alignment`, GD-15): một số cơ duyên chỉ phe chính / tà / ma gặp, lựa chọn đạo đức trong cơ duyên có thể khác. Phe **không** tự động thù địch nhau; PvP giữa người chơi theo chế độ PK cá nhân ([GD-32](31-pvp-infamy.md)); ý tưởng giảm sát nghiệp khi giết người khác phe: PK-Q-2.
- <a id="so-r-049"></a>**SO-R-049** **Giới hạn phúc lợi:** `perks` chỉ dùng type trong union GD-27 được phép (tu vi, hiểm địa, giá NPC, tâm cảnh, thu thập), mỗi hiệu ứng ≤ 20% (phí, thời gian) / ≤ 10% (tu vi, sát thương, giá); **không** chạm tỷ lệ đột phá, thọ nguyên, tỷ lệ chế tác thành công (GD-29 RF-R-081 tinh thần). Validator (SO-V-xx) kiểm tra.
- <a id="so-r-04a"></a>**SO-R-04A** **Định hướng nội dung Hợp Hoan Tông:** chủ đề "song tu" được thể hiện bằng **tu luyện cùng nhau, tình nghĩa, mị thuật** (hiệu ứng, lời thoại), **không** có nội dung tình dục; trang phục và lời thoại theo hướng dẫn phân loại độ tuổi của game (AR-01 Art Bible, kiểm duyệt nội dung). Tạc Thiên Bang: phong cách giang hồ bất kham, không cổ súy tội ác ngoài đời thực.
- <a id="so-r-04b"></a>**SO-R-04B** **Xuất thân gắn tông** (GD-27): `BG_SECT_SERVANT` → Thanh Vân Tông; `BG_TAC_THIEN_GANG` (Bang chúng Tạc Thiên) → Tạc Thiên Bang; `BG_HOP_HOAN_DISCIPLE` (Hợp Hoan đệ tử) → Hợp Hoan Tông. Cả ba bắt đầu ở bậc `TAP_DICH` của tông đó, lên `NGOAI_MON` khi xong chuỗi xuất thân (GD-15), thay cho nhánh tương ứng của `QC_MAIN_V1_JOIN_SECT`. Mở bằng unlock Luân Hồi khi tài khoản từng vào tông đó (GD-26).

### 5.4c. Danh vọng phe (chốt SO-Q-7)
- <a id="so-r-04c"></a>**SO-R-04C** Mỗi nhân vật có ba thanh **danh vọng** `reputation: { CHINH, TA, MA }` (số nguyên ≥ 0, **thuộc kiếp**), tách biệt với karma thiện / ác (GD-27): karma là *bản tính*, danh vọng là *giang hồ biết đến ngươi ở phe nào*.
- <a id="so-r-04d"></a>**SO-R-04D** **Nguồn:** outcome cơ duyên `CHANGE_REPUTATION` (GD-22, trần theo phẩm), hành động nhiệm vụ (GD-15), nhiệm vụ tông môn hằng ngày (+20 cho phe của tông), hạ tinh anh / boss có gắn phe (vd. Khoáng Tặc là người của Tạc Thiên Bang: hạ chúng −5 `TA`, +2 `CHINH`). Trần nhận **mỗi ngày thực** 300 mỗi phe (trừ thưởng chuỗi chính / xuất thân).
- <a id="so-r-04e"></a>**SO-R-04E** **Hệ số & đối nghịch:**
  - thành viên `ACTIVE` của tông thuộc phe X nhận ×1,5 danh vọng X;
  - **chính ↔ ma đối nghịch**: mỗi điểm `CHINH` nhận được trừ 0,5 điểm `MA` và ngược lại (không dưới 0);
  - **tà trung lập**: `TA` không trừ phe nào và không bị trừ bởi phe nào (giang hồ chơi với mọi người).
- <a id="so-r-04f"></a>**SO-R-04F** **Bậc danh vọng** (mỗi phe):

| Bậc | Điểm | Mở khóa (ví dụ) |
|-----|:---:|-----------------|
| Vô danh | 0 | — |
| Có tiếng | 500 | Danh hiệu; cơ duyên phe cấp 1 (điều kiện `reputationMin`) |
| Danh chấn | 2.000 | Giảm 10% giá ở thương nhân phe (Vực 1); nhiệm vụ phe đặc biệt |
| Uy chấn | 5.000 | **Vực 2:** cửa hàng phe (trang bị / đan phương phe, ngoại hình), thế lực NPC phe Vực 2 tiếp đón; cơ duyên phe cấp 2 |
| Truyền kỳ | 12.000 | Danh hiệu + hiệu ứng tên; cơ duyên phe cấp 3 (phẩm Địa); ghi vào Bia Vạn Cổ |

- <a id="so-r-04g"></a>**SO-R-04G** Danh vọng **không** cần là thành viên tông phe đó (tán tu vẫn có thể nổi danh chính đạo). Phản môn (SO-R-047) **không** xóa danh vọng nhưng trừ 30% danh vọng phe của tông vừa rời.
- <a id="so-r-04h"></a>**SO-R-04H** Qua kiếp: danh vọng thuộc kiếp (mất khi hết kiếp); **Bia Vạn Cổ** ghi bậc cao nhất mỗi phe. Phần thưởng phe **không** chạm tỷ lệ đột phá / thọ nguyên / tỷ lệ chế tác (giống SO-R-049).

### 5.4d. Bảng Chiến Lực (dùng cho thông báo, chốt SO-Q-5)
- <a id="so-r-04i"></a>**SO-R-04I** **Thiên Bảng Chiến Lực**: xếp hạng các nhân vật `ALIVE` trên server theo `combatPower` (GD-06 §6.8). Game Server báo `combatPower` về API khi chỉ số đổi (gom tối đa 1 lần / 60s / nhân vật; giá trị do server tính bằng `shared/sim`, không nhận từ client); API giữ sorted set Redis `rank:cp:{server}` + cột `character.combat_power`. Bảng hiển thị top 100, làm mới 10 phút; xem được hạng của mình kể cả ngoài top.
- <a id="so-r-04j"></a>**SO-R-04J** Nhân vật **chết thật / tọa hóa** khi đang ở **top 100** (theo bản chụp gần nhất) → thông báo toàn server ưu tiên 3 (SO-R-060), kèm hạng: *"Thiên Bảng hạng 37 — {tên} ({cảnh giới}) đã tọa hóa, hưởng thọ 182 năm."* Ngoài top 100: **không** thông báo toàn server; chỉ báo cho bạn bè, đồng môn tông môn tự lập và tổ đội hiện tại (kênh `SYSTEM` riêng).

### 5.4e. Gói phúc lợi môn phái (không cộng dồn)
- <a id="so-r-04k"></a>**SO-R-04K** Nhân vật được đồng thời ở **một đại tông NPC** (sư môn, thuộc kiếp) và **một tông môn tự lập** (thuộc tài khoản), nhưng chỉ hưởng **một gói phúc lợi** tại một thời điểm (`benefitSource`):

| Gói | Điều kiện | Gồm |
|-----|-----------|-----|
| `NPC_SECT` | Có sư môn NPC `ACTIVE` | `perks` của tông NPC (§5.4b) |
| `PLAYER_SECT` | Là thành viên tông môn tự lập | **Tông quy** do chưởng môn chọn (SO-R-05A) |
| `WANDERER` | Không có sư môn NPC (tán tu), có thể có hoặc không có tông tự lập | Gói **Tán tu tự tại** (SO-R-04L) |

  Người chơi chọn gói trong bảng tông môn; đổi gói tối đa **mỗi 7 ngày thực** (`benefitSwitchCooldownDays`). Mặc định: `NPC_SECT` nếu có sư môn, `PLAYER_SECT` nếu chỉ có tông tự lập, ngược lại `WANDERER`. Gói không còn hợp lệ (rời tông) → tự chuyển về gói hợp lệ, không tính hồi chiêu.
- <a id="so-r-04l"></a>**SO-R-04L** **Tán tu tự tại** (gói `WANDERER`, chỉ khi **không** có sư môn NPC): (1) mua ở thương nhân NPC thường −10%; (2) danh vọng đối nghịch chính ↔ ma chỉ trừ 0,25 thay vì 0,5 (SO-R-04E) — tán tu dễ giữ quan hệ với mọi phe; (3) mở nhóm cơ duyên "tán tu" (`conditions.alignment: [NONE]`, GD-22). Không có Bảo Khố / tâm pháp đặc trưng — đổi lại không bị ràng buộc phe và phản môn.
- <a id="so-r-04m"></a>**SO-R-04M** **Thứ không thuộc gói** (luôn dùng được nếu đủ điều kiện, không loại trừ nhau): Bảo Khố & nhiệm vụ hằng ngày của sư môn NPC; Tông Khố, nhiệm vụ hằng tuần và **công trình trú địa** của tông tự lập (Đan Phòng, Luyện Khí Các, Linh Mạch Điện — tiện ích theo địa điểm, đã có trần, GD-30 HM-R-004); danh vọng phe. Chỉ **phúc lợi chỉ số / kinh tế bị động** (`perks`, Tông quy, Tán tu tự tại) bị giới hạn một gói.
- <a id="so-r-04n"></a>**SO-R-04N** **Đường tới Trúc Cơ luôn mở** với mọi lựa chọn (mở rộng SO-R-046): đan phương `RCP_TRUC_CO_DAN` có ở (a) Bảo Khố cấp 1 của mọi tông NPC — 600 cống hiến; (b) **Tông Khố** tông tự lập từ cấp 2 — 300 `merit` (SO-R-05B); (c) **Tạp Hóa** `SHOP_V1_GENERAL` — 3.000 linh thạch, yêu cầu `LK7` (cho tán tu; GD-13); (d) rơi ở boss BC03.

### 5.5. Tông môn tự lập (Beta)
- <a id="so-r-050"></a>**SO-R-050** **Lập tông môn:** người lập có nhân vật `ALIVE` cảnh giới ≥ `TC1`, không thuộc tông môn tự lập nào; phí 10.000 linh thạch (sink `SECT_CREATE_FEE`); tên 2–8 ký tự, duy nhất, qua bộ lọc; chọn huy hiệu.
- <a id="so-r-051"></a>**SO-R-051** **Thành viên theo tài khoản** (SO-R-002): một tài khoản thuộc tối đa một tông môn tự lập trên server. Nhân vật hết kiếp: tư cách giữ nguyên; kiếp mới hiển thị danh hiệu "Chuyển thế" cạnh tên, giữ chức vụ. Rời tông môn: chờ 24h mới gia nhập tông môn khác.
- <a id="so-r-052"></a>**SO-R-052** Sức chứa theo cấp tông môn: 1 → 20 · 2 → 30 · 3 → 40 · 4 → 50 · 5 → 60.
- <a id="so-r-053"></a>**SO-R-053** **Chức vụ và quyền:**

| Quyền | Chưởng môn | Trưởng lão (≤ 4) | Chấp sự (≤ 8) | Đệ tử |
|-------|:---:|:---:|:---:|:---:|
| Duyệt đơn / mời | ✅ | ✅ | ✅ | |
| Đuổi Đệ tử / Chấp sự | ✅ | ✅ | | |
| Bổ nhiệm, bãi nhiệm | ✅ | | | |
| Nâng cấp công trình, chi tông khố | ✅ | ✅ | | |
| Đổi tên / huy hiệu, giải tán, chuyển Chưởng môn | ✅ | | | |
| Quyên góp tông khố | ✅ | ✅ | ✅ | ✅ |

- <a id="so-r-054"></a>**SO-R-054** **Tông khố:** thành viên quyên linh thạch từ ví nhân vật (`SECT_DONATE`, chuyển giao, không phải sink); nhận `merit` = số linh thạch / 10. Chi tiêu (nâng cấp, phí duy trì) là sink (`SECT_UPGRADE`, `SECT_UPKEEP`).
- <a id="so-r-055"></a>**SO-R-055** **Kinh nghiệm & cấp tông môn** 1–5: từ nhiệm vụ tông môn hằng tuần (GD-15) và quyên góp (1 exp / 100 linh thạch). Ngưỡng: 2 → 5.000 · 3 → 20.000 · 4 → 60.000 · 5 → 150.000.
- <a id="so-r-056"></a>**SO-R-056** **Trú địa** (instance riêng của tông môn, `MapType.SECT_GROUNDS`; vào từ **sơn môn** trên bản đồ nếu tông môn giữ địa điểm, ngược lại từ cổng chung ở `HUB`: [GD-30](28-caves-sect-sites.md)) chứa công trình, cấp 0–5, tối đa = cấp tông môn:

| Công trình | Hiệu ứng mỗi cấp (khi dùng tại trú địa) | Chi phí nâng cấp cấp L |
|------------|----------------------------------------|------------------------|
| Đan Phòng (`DAN_PHONG`) | Trạm luyện đan: thành công +2, phẩm chất +2 (GD-10 §6.2–6.3) | `5.000 × L²` linh thạch |
| Luyện Khí Các (`LUYEN_KHI_CAC`) | Trạm rèn: thành công +2, phẩm chất +2 (GD-11 §6.2–6.3) | `5.000 × L²` |
| Linh Mạch Điện (`LINH_MACH_DIEN`) | Điểm đả tọa / bế quan `SECT_GROUNDS`, hệ số `min(1,0 + 0,05 × L + thưởng phẩm sơn môn, 1,35)` ([GD-30 §6.1](28-caves-sect-sites.md)) | `8.000 × L²` |

- <a id="so-r-05a"></a>**SO-R-05A** **Tông quy** (phúc lợi gói `PLAYER_SECT`): chưởng môn chọn từ danh mục `sectEdicts` (cùng giới hạn SO-R-049); số tông quy theo cấp tông môn: cấp 1–2 → 1 · 3–4 → 2 · 5 → 3. Đổi tông quy: chưởng môn, hồi chiêu 7 ngày, có hiệu lực từ 05:00 hôm sau. Danh mục MVP:

| ID | Tông quy | Hiệu ứng |
|----|----------|----------|
| `EDICT_CULTIVATION` | Cần tu khổ luyện | Đả tọa ở linh mạch +5% tu vi |
| `EDICT_DANGER` | Mạo hiểm đoạt bảo | Phí vào hiểm địa −15% |
| `EDICT_BROTHERHOOD` | Đồng môn tương trợ | Đả tọa cạnh đồng môn (cùng tông) trong 5m: +6% tu vi (không cộng nhiều người) |
| `EDICT_ALCHEMY` | Đan đạo hưng thịnh | −10% thời gian phiên luyện đan, −10% phí luyện |
| `EDICT_FORGING` | Khí đạo hưng thịnh | −10% phí rèn / cường hóa |
| `EDICT_GATHERING` | Thu thập thiên địa | +8% cơ hội thêm nguyên liệu khi hái thuốc / khai khoáng |
| `EDICT_TRADE` | Thương hội | Thuế đấu giá −1 điểm % (GD-13) |
| `EDICT_HEART` | Tĩnh tâm | Tâm cảnh hồi phục +15% (GD-04) |

  Cân bằng: một tông quy ≈ một phúc lợi của tông NPC (hơi thấp hơn vì tông tự lập còn có công trình trú địa); 3 tông quy ở cấp 5 ≈ gói tông NPC đầy đủ.
- <a id="so-r-05b"></a>**SO-R-05B** **Tông Khố đổi thưởng** (cửa hàng của tông tự lập, tiền tệ `merit` của thành viên — SO-R-054, nhiệm vụ hằng tuần GD-15): cấp 2 mở `RCP_TRUC_CO_DAN` (300 merit), cấp 3 ngọc giản Huyền phẩm Hạ, cấp 4 đan lô / búa T2. `merit` là tiền tệ cấp **tài khoản trong tông** (không mất khi hết kiếp, mất khi rời tông).
- <a id="so-r-057"></a>**SO-R-057** **Phí duy trì** hằng tuần = `500 × Σ cấp công trình` linh thạch, trừ từ tông khố; không đủ thì công trình **ngừng hiệu lực** (không mất cấp) tới khi trả đủ.
- <a id="so-r-058"></a>**SO-R-058** Chưởng môn không đăng nhập 14 ngày: quyền tự chuyển cho Trưởng lão (rồi Chấp sự) hoạt động gần nhất. Giải tán: chỉ Chưởng môn, xác nhận lại sau 24h; tông khố bị xóa (sink).

### 5.6. Thông báo toàn server (kênh `SYSTEM`)
- <a id="so-r-060"></a>**SO-R-060** Một **hàng đợi thông báo chung** cho mọi nguồn (thay cho hàng đợi riêng của GD-22 ENC-R-102): tối đa 1 tin / 60s mỗi server; tin chờ quá 10 phút bị bỏ; ưu tiên theo bảng:

| Ưu tiên | Sự kiện | Nguồn |
|:---:|---------|-------|
| 1 | Hạ boss lần đầu trên server; cơ duyên phẩm Thiên | GD-12, GD-22 |
| 2 | Đột phá Kim Đan trở lên; cơ duyên phẩm Địa; rèn ra Tiên khí dị tượng | GD-04, GD-22, GD-11 |
| 3 | Tọa hóa / tử vong của nhân vật đang ở **top 100 Thiên Bảng Chiến Lực** (SO-R-04J) | GD-26, GD-06 |
| 4 | Tinh anh xuất hiện (chỉ trong bản đồ đó), sự kiện thế giới | GD-12, GD-23 |

- <a id="so-r-061"></a>**SO-R-061** Tùy chọn tài khoản `hideNameInBroadcast` (GD-22 ENC-R-101) áp cho **mọi** thông báo: hiện "Một vị đạo hữu".

### 5.7. Hộ pháp: phần thưởng (trả lời GD-04 CU-Q-4)
- <a id="so-r-070"></a>**SO-R-070** Mỗi hộ pháp được tính trong `BreakthroughAttempt` (GD-04 CU-R-063) nhận **+2 thiện ác (`karma`)** và +1 tiến độ danh hiệu "Hộ Pháp" khi lần đột phá kết thúc (thành hay bại); tối đa 5 lần thưởng / ngày / nhân vật. Không thưởng tu vi (tránh lạm dụng).

### 5.8. Xem thông tin người chơi
- <a id="so-r-080"></a>**SO-R-080** Click vào người chơi khác: tên, cảnh giới, **tuổi** (không hiện thọ tối đa: trả lời GD-26 LC-Q-5), kiếp thứ mấy, bậc Thanh Vân Tông, tông môn tự lập, danh hiệu, trang bị (phẩm, cấp cường hóa, ngoại hình), chiến lực. Không hiện thuộc tính phụ chi tiết, thiên phú, linh căn (thông tin "tu luyện" là riêng tư).

### 5.9. Báo cáo và kiểm duyệt
- <a id="so-r-090"></a>**SO-R-090** Báo cáo người chơi từ chat / bảng thông tin: lý do (chửi bới, spam / quảng cáo, gian lận, tên xấu, khác); đính kèm tự động 20 tin gần nhất của người bị báo cáo.
- <a id="so-r-091"></a>**SO-R-091** **Tự động cấm chat tạm:** ≥ 5 báo cáo từ 5 tài khoản khác nhau trong 10 phút → cấm chat 30 phút chờ GM xem. GM có các mức: cảnh cáo, cấm chat (1h / 24h / 7 ngày), khóa tài khoản (GD-20, admin panel).
- <a id="so-r-092"></a>**SO-R-092** Tài khoản bị cấm chat vẫn chơi bình thường, chỉ dùng được `PARTY`.

### 5.10. Sư đồ (sau launch)
- <a id="so-r-100"></a>**SO-R-100** Người chơi có tổng điểm Luân Hồi ≥ 1.000 (kỳ cựu, kể cả đang ở kiếp mới) nhận đệ tử là nhân vật kiếp 1 dưới `TC1`; cùng tổ đội làm nội dung → sư phụ nhận điểm Luân Hồi nhỏ, đệ tử nhận tu vi thêm. Đặc tả ở phiên bản sau.

---

## 6. Công thức
```
Phí nâng công trình (cấp L):  DAN_PHONG, LUYEN_KHI_CAC: 5.000 × L²   ·   LINH_MACH_DIEN: 8.000 × L²
Phí duy trì tuần: 500 × Σ cấp công trình
Merit quyên góp: floor(linh thạch / 10)   ·   Exp tông môn từ quyên góp: floor(linh thạch / 100)
Hệ số đả tọa Linh Mạch Điện: min(1,0 + 0,05 × L + siteBonus, 1,35)   (GD-30 §6.1; luôn < linh mạch ngoài đồng 1,5)
```
Ví dụ: tông môn có Đan Phòng 3, Luyện Khí Các 2, Linh Mạch Điện 2 → phí duy trì `500 × 7 = 3.500` / tuần; nâng Đan Phòng lên 4: `5.000 × 16 = 80.000`.

---

## 7. Gamedata

| File | Nội dung |
|------|----------|
| `shared/gamedata/social/params.json` | Tham số |
| `shared/gamedata/social/npc_sects.json` | `NpcSectDef[]` cho 3 đại tông: phe, tổng đàn, tên bậc, Bảo Khố, phúc lợi, tâm pháp đặc trưng, nhánh gia nhập (§5.4b) |
| `shared/gamedata/social/sect_edicts.json` | Danh mục Tông quy `EDICT_*` (SO-R-05A); validator dùng chung giới hạn SO-R-049 |
| `shared/gamedata/social/player_sect.json` | Cấp, sức chứa, công trình, chi phí (Beta) |
| `shared/gamedata/social/chat_filter/*.txt` | Danh sách từ cấm vi / en, mẫu chặn (URL, số điện thoại, quảng cáo) |
| `shared/gamedata/social/broadcast.json` | Bảng ưu tiên thông báo §5.6 |

```jsonc
// params.json
{
  "partyMaxSize": 5, "inviteTtlSec": 60, "leaderOfflineSec": 300, "partyEffectRadius": 40,
  "pingDurationSec": 4, "pingMaxPer5s": 3,
  "lfgTtlMin": 30, "lfgNoteMax": 60,
  "chat": {
    "WORLD":   { "realmMin": "LK3", "accountPlayHoursMin": 2, "intervalSec": 30, "maxLength": 120 },
    "MAP":     { "intervalSec": 5,  "maxLength": 200 },
    "PARTY":   { "intervalSec": 1,  "maxLength": 200 },
    "SECT":    { "intervalSec": 2,  "maxLength": 200 },
    "WHISPER": { "intervalSec": 1,  "maxLength": 200 },
    "maxItemLinks": 2, "itemLinkTtlHours": 24, "retentionDays": 30 },
  "friendMax": 100, "friendRequestTtlDays": 7, "blockMax": 200,
  "npcSect": { "dailyContributionCap": 300,
               "ranks": { "NOI_MON": { "realmMin": "TC1", "contributionTotal": 2000 },
                          "CHAN_TRUYEN": { "realmMin": "KD1", "contributionTotal": 10000 } } },
  "broadcastIntervalSec": 60, "broadcastMaxQueueMin": 10,
  "guardianKarma": 2, "guardianDailyRewards": 5,
  "autoMute": { "reports": 5, "windowMin": 10, "muteMin": 30 },
  "playerSect": { "createRealmMin": "TC1", "createFee": 10000, "nameMin": 2, "nameMax": 8,
                  "capacityByLevel": [20, 30, 40, 50, 60], "levelExp": [5000, 20000, 60000, 150000],
                  "elderMax": 4, "deaconMax": 8, "rejoinCooldownHours": 24, "leaderInactiveDays": 14,
                  "upkeepPerLevel": 500, "meritPerStones": 10, "expPerStones": 100,
                  "edictSlotsByLevel": [1, 1, 2, 2, 3], "edictChangeCooldownDays": 7 },
  "benefitSwitchCooldownDays": 7,
  "wanderer": { "npcShopDiscount": 0.10, "opposingRepFactor": 0.25 },
  "sectEdicts": "social/sect_edicts.json"
}
```

---

## 8. (Dành chỗ) Chỉ tiêu xã hội
Mục tiêu Alpha: ≥ 50% người chơi có ít nhất 1 bạn bè sau 3 giờ chơi; ≥ 40% lượt hiểm địa đi theo tổ đội; tỷ lệ người chơi quay lại sau khi chết (GD-26 KPI) ở người có bạn bè cao hơn người không có. Đo bằng telemetry §14.

---

## 9. Hợp đồng giao tiếp

### 9.1. API công khai

| Endpoint | Mục đích | Quy tắc |
|----------|----------|---------|
| `POST /v1/parties/invites` · `POST /v1/parties/invites/{id}/accept` · `/decline` | Mời / nhận lời mời tổ đội | SO-R-010 |
| `POST /v1/parties/{id}/kick` · `/leave` · `/transfer` | Quản lý đội | SO-R-011, 012 |
| `POST /v1/characters/{id}/benefit` | `{ source }` chọn gói phúc lợi | SO-R-04K |
| `PUT /v1/sects/{id}/edicts` | `{ edictIds }` (chưởng môn) | SO-R-05A |
| `GET /v1/rankings/combat-power` | Top 100 Thiên Bảng + hạng của mình | SO-R-04I |
| `GET /v1/characters/{id}/reputation` | Ba thanh danh vọng + bậc | §5.4c |
| `GET /v1/lfg?activity&realm` · `POST /v1/lfg` · `POST /v1/lfg/{id}/apply` · `/accept` | Bảng Chiêu Mộ | SO-R-016 |
| `GET /v1/accounts/me/friends` · `POST /v1/accounts/me/friend-requests` · `POST .../{id}/accept` · `DELETE /v1/accounts/me/friends/{accountId}` | Bạn bè | SO-R-030..032 |
| `POST /v1/accounts/me/blocks` · `DELETE .../{accountId}` | Chặn | SO-R-032 |
| `GET /v1/characters/{id}/npc-sect` | Bậc, cống hiến, điều kiện bậc tiếp theo | SO-R-040..044 |
| `GET /v1/characters/{id}/profile-public` | Xem thông tin người chơi | SO-R-080 |
| `POST /v1/reports` | Báo cáo | SO-R-090 |
| `POST /v1/player-sects` · `GET /v1/player-sects/{id}` · `POST .../applications` · `POST .../members/{accountId}/role` · `POST .../donate` · `POST .../buildings/{b}/upgrade` · `POST .../disband` | Tông môn tự lập (Beta) | §5.5 |

### 9.2. Message thời gian thực

| Message | Hướng | Payload |
|---------|-------|---------|
| `C2S_ChatSend` | C→S | `{ channel, scopeTarget?, text, itemLinkInstanceIds? }` |
| `S2C_ChatMessage` | S→C | `ChatMessage` |
| `S2C_ChatRejected` | S→C | `{ reason: 'RATE_LIMIT' \| 'FILTERED_BLOCKED' \| 'MUTED' \| 'NOT_ALLOWED' \| 'TARGET_OFFLINE' }` |
| `S2C_PartyUpdate` | S→C | `{ party: Party, members: [{ characterId, name, realmCode, hpPct, mapId, state: 'IN' \| 'DOWNED' \| 'OFFLINE' }] }` |
| `S2C_PartyInvite` | S→C | `{ inviteId, fromName, partySize }` |
| `C2S_Ping` · `S2C_Ping` | C↔S | `{ kind: 'GROUND' \| 'ENEMY' \| 'ITEM', position, targetId? }` |
| `S2C_FriendNotify` | S→C | `{ friendAccountId, characterName, kind: 'ONLINE' \| 'BREAKTHROUGH' \| 'LIFE_ENDED' \| 'NEW_LIFE', refId? }` |

### 9.3. Sự kiện domain

| Sự kiện | Payload | Bên nhận |
|---------|---------|----------|
| `party.changed` | `{ partyId, memberIds, leaderId }` | Game Server (hộ pháp, tính công, kênh), GD-28 |
| `npc_sect.rank_up` | `{ characterId, rank }` | Thông báo, Bia Vạn Cổ |
| `broadcast.requested` | `{ priority, templateKey, params, characterId? }` | Hàng đợi `SYSTEM` (§5.6); GD-04, GD-11, GD-12, GD-22, GD-26 phát sự kiện này |
| Tiêu thụ `lifecycle.life_ended` / `life_started` (GD-26) | | Thông báo bạn bè, tông môn tự lập ("Chuyển thế"), hủy tổ đội |

---

## 10. Lưu trữ

```sql
-- Tổ đội, lời mời, Chiêu Mộ: Redis (TTL), khôi phục được sau khởi động lại; không cần bảng lâu dài.

CREATE TABLE npc_sect_membership (                -- khóa sửa theo TD-03 §8 (phản môn cần lịch sử)
  character_id UUID NOT NULL REFERENCES character(id),
  sect_id VARCHAR(32) NOT NULL, rank VARCHAR(16) NOT NULL,
  contribution_total INT NOT NULL DEFAULT 0, reborn_disciple BOOLEAN NOT NULL DEFAULT FALSE,
  state VARCHAR(8) NOT NULL DEFAULT 'ACTIVE' CHECK (state IN ('ACTIVE','LEFT')),
  joined_at TIMESTAMPTZ NOT NULL DEFAULT now(), left_at TIMESTAMPTZ,
  PRIMARY KEY (character_id, sect_id)
);
CREATE UNIQUE INDEX ux_npc_sect_active ON npc_sect_membership(character_id) WHERE state = 'ACTIVE';
-- faction_reputation, character_benefit, player_sect_edicts: DDL ở TD-03 §6

CREATE TABLE friendship (
  account_id UUID NOT NULL, friend_account_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, friend_account_id)
);
CREATE TABLE friend_request (
  request_id UUID PRIMARY KEY, from_account_id UUID NOT NULL, to_account_id UUID NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE (from_account_id, to_account_id)
);
CREATE TABLE block_entry (
  account_id UUID NOT NULL, blocked_account_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, blocked_account_id)
);

CREATE TABLE chat_message_log (                    -- partition theo ngày, xóa sau 30 ngày
  message_id UUID NOT NULL, channel VARCHAR(8) NOT NULL, scope_id VARCHAR(80),
  from_character_id UUID, from_account_id UUID, text_raw TEXT NOT NULL, text_filtered TEXT NOT NULL,
  sent_at TIMESTAMPTZ NOT NULL, PRIMARY KEY (sent_at, message_id)
);
CREATE TABLE player_report (
  report_id UUID PRIMARY KEY, reporter_account_id UUID NOT NULL, target_account_id UUID NOT NULL,
  reason VARCHAR(24) NOT NULL, context JSONB NOT NULL, status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE chat_mute (
  account_id UUID NOT NULL, until_at TIMESTAMPTZ NOT NULL, reason VARCHAR(48), by_gm UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY (account_id, created_at)
);

-- Beta: tông môn tự lập (thay bảng sect / sect_member sơ bộ ở PLAN §5.4)
CREATE TABLE player_sect (
  sect_id UUID PRIMARY KEY, name VARCHAR(16) NOT NULL UNIQUE, emblem_id VARCHAR(32) NOT NULL,
  level SMALLINT NOT NULL DEFAULT 1, exp INT NOT NULL DEFAULT 0, leader_account_id UUID NOT NULL,
  treasury_stones BIGINT NOT NULL DEFAULT 0 CHECK (treasury_stones >= 0),
  buildings JSONB NOT NULL DEFAULT '{}', upkeep_due_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), disbanded_at TIMESTAMPTZ
);
CREATE TABLE player_sect_member (
  account_id UUID PRIMARY KEY,                     -- SO-R-051: một tông môn / tài khoản
  sect_id UUID NOT NULL REFERENCES player_sect(sect_id), role VARCHAR(16) NOT NULL,
  merit BIGINT NOT NULL DEFAULT 0, joined_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE player_sect_ledger (
  id BIGSERIAL PRIMARY KEY, sect_id UUID NOT NULL, delta BIGINT NOT NULL, reason_code VARCHAR(24) NOT NULL,
  account_id UUID, ref_id VARCHAR(80), created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```
Mã lý do tiền tệ mới (bổ sung GD-13 `CurrencyReason`): `SECT_CREATE_FEE`, `SECT_DONATE`, `SECT_UPGRADE`, `SECT_UPKEEP`, `SECT_DISBAND`.

---

## 11. UI/UX
- **Khung tổ đội** (trái màn hình): tên, cảnh giới, thanh máu, biểu tượng hấp hối / offline, bản đồ khác thì hiện tên bản đồ; đội trưởng có biểu tượng vương miện.
- **Chat:** tab theo kênh, màu riêng (Thế giới vàng, Bản đồ trắng, Tổ đội xanh, Tông môn tím, Mật thoại hồng, Hệ thống cam); click tên mở menu (mật thoại, mời đội, kết bạn, xem thông tin, chặn, báo cáo).
- **Bạn bè** (phím `O`): danh sách theo online; mục "Cố nhân" cho bạn đang không có nhân vật sống (vừa tọa hóa) kèm link tiểu sử.
- **Thanh Vân Tông:** bảng bậc hiện tại, thanh tiến độ lên bậc, nút tới Bảo Khố.
- **Tông môn tự lập (Beta):** tab Thành viên, Công trình, Tông khố (lịch sử), Đơn gia nhập.
- **Thông báo hệ thống:** dải chữ chạy ngang trên đầu màn hình cho ưu tiên 1–2; ưu tiên 3–4 chỉ vào kênh chat.

---

## 12. Tương tác với hệ thống khác

| Hệ thống | Tương tác |
|----------|-----------|
| GD-04 | Hộ pháp (tổ đội), phần thưởng hộ pháp; Linh Mạch Điện |
| GD-12 | Tính công theo tổ đội; HP boss theo số người |
| GD-28 | Tổ đội vào hiểm địa, khóa đội, cứu hấp hối, di hài |
| GD-21 | Chọn kênh theo đội / bạn bè |
| GD-22 | Hàng đợi thông báo chung, ẩn tên |
| GD-26 | Bạn bè / tông môn tự lập theo tài khoản; thông báo tọa hóa; hủy tổ đội khi hết kiếp |
| GD-13 | Bảo Khố Thanh Vân Tông (`rankMin`), tông khố, giao dịch trực tiếp (Beta, chặn theo SO-R-032) |
| GD-10 / GD-11 | Đan Phòng, Luyện Khí Các tông môn |
| GD-15 | Nhiệm vụ gia nhập Thanh Vân Tông, nhiệm vụ tông môn hằng ngày / hằng tuần |
| GD-27 | Hiệu ứng thiên phú lên đồng đội |

---

## 13. Trường hợp biên

| Tình huống | Xử lý |
|-----------|-------|
| Mời người đang ở đội khác | Lời mời bị từ chối tự động, báo "Đã có tổ đội" |
| Đội trưởng vào hiểm địa một mình, đội 3 người | Không được: lượt hiểm địa yêu cầu mọi thành viên trong đội cùng xác nhận (GD-28), hoặc đội trưởng rời đội trước |
| Thành viên hết kiếp khi đang ở đội | Rời đội tự động (GD-26 bước 4) |
| Gửi mật thoại tới tên nhân vật đã `ENDED` | `TARGET_OFFLINE` (tên có thể đã được người khác dùng lại, GD-27 TLN-R-025) |
| Bạn bè tọa hóa rồi tạo kiếp mới tên khác | Vẫn trong danh sách (theo tài khoản), hiển thị tên mới |
| Chưởng môn tông môn tự lập hết kiếp | Vẫn là Chưởng môn (theo tài khoản) |
| Tông khố không đủ phí duy trì | Công trình ngừng hiệu lực, không mất cấp (SO-R-057) |
| Server khởi động lại | Tổ đội khôi phục từ Redis; lời mời đang chờ bị hủy |

---

## 14. Telemetry

| Sự kiện | Trường | Câu hỏi trả lời |
|---------|--------|-----------------|
| `party_formed` / `party_session` | size, durationMin, activities | Tỷ lệ chơi nhóm |
| `lfg_listing` | activity, filledSec | Bảng Chiêu Mộ có hiệu quả |
| `friend_added` | playHoursAtAdd | Khi nào người chơi kết bạn |
| `chat_message` (gộp) | channel, filteredRate, blockedRate | Lọc có quá gắt / quá lỏng |
| `report_filed` | reason, autoMuted | Mức độ độc hại |
| `npc_sect_rank` | rank, playHours | Tốc độ lên bậc |
| `retention_after_death` × có bạn bè | | Giả thuyết "bạn tu giữ chân người chơi" (§8) |

---

## 15. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="so-ac-001"></a>SO-AC-001 | Đội 5 người | Mời người thứ 6 | Từ chối (đội đầy) |
| SO-AC-002 | Lời mời gửi lúc T | T + 61s chấp nhận | `EXPIRED` |
| SO-AC-003 | Đội trưởng offline 301s | Theo dõi | Quyền chuyển cho thành viên vào sớm nhất đang online |
| SO-AC-004 | Đội đang có lượt hiểm địa | Đội trưởng đuổi thành viên | Từ chối (khóa đội) |
| SO-AC-005 | Đội 2 người, một người rời | Theo dõi | Đội giải tán |
| SO-AC-006 | Nhân vật `LK2` | Gửi tin `WORLD` | `NOT_ALLOWED` |
| SO-AC-007 | Gửi 2 tin `WORLD` cách 10s | Tin thứ 2 | `RATE_LIMIT` |
| SO-AC-008 | Tin `MAP` chứa URL | Gửi | Bị chặn hẳn, `FILTERED_BLOCKED` |
| SO-AC-009 | Tin chứa từ cấm viết không dấu, chèn dấu chấm giữa các chữ | Gửi | Từ đó bị thay `***` |
| SO-AC-010 | A chặn B | B gửi `WORLD`, mời tổ đội, mật thoại A | A không thấy tin; lời mời / mật thoại bị từ chối |
| SO-AC-011 | Kết bạn ở kiếp 1, nhân vật A tọa hóa và tạo kiếp 2 | Danh sách bạn của B | Vẫn có A, hiện tên nhân vật mới; B nhận `LIFE_ENDED` rồi `NEW_LIFE` |
| SO-AC-012 | Nhân vật mới, tài khoản chưa từng vào Thanh Vân Tông | Hoàn thành `QC_MAIN_V1_JOIN_SECT` | Bậc `NGOAI_MON`; Bảo Khố cấp 1 mở, có `RCP_TRUC_CO_DAN` giá 600 |
| SO-AC-013 | Kiếp 2, tài khoản có `FLAG_ACC_JOINED_SECT` | Tới Thanh Vân Trấn | Vào thẳng `NGOAI_MON`, `rebornDisciple = true` |
| SO-AC-013b | Nhân vật chọn nhánh Hợp Hoan, qua Trận Mị Tâm | Kết thúc `QC_MAIN_V1_JOIN_SECT` | `NpcSectMembership { sectId: SECT_HOP_HOAN, rank: NGOAI_MON }`, `alignment = MA`, cờ `FLAG_ACC_JOINED_HOP_HOAN` |
| SO-AC-013c | Thành viên Tạc Thiên Bang `NGOAI_MON` | Xem Bảo Khố | Có `RCP_TRUC_CO_DAN` giá 600 (SO-R-046) |
| SO-AC-013d | Rời Tạc Thiên Bang | Gia nhập Thanh Vân ngay / sau 7 ngày / quay lại Tạc Thiên | Từ chối / được (ở `NGOAI_MON`, cống hiến 0) / từ chối |
| SO-AC-013e | Hai thành viên Hợp Hoan cùng tổ, cùng đả tọa cách 3m | Tick tu vi | Mỗi người +10%; thêm người thứ ba không cộng thêm |
| SO-AC-013f | Thành viên Tạc Thiên vào hiểm địa phí 200 | Thu phí | 160 |
| SO-AC-013g | `npc_sects.json` có perk `BREAKTHROUGH_CHANCE_FLAT` | Validator | Fail (SO-R-049) |
| SO-AC-013h | Thành viên Thanh Vân nhận outcome `CHANGE_REPUTATION CHINH +100`, đang có `MA` 200 | Áp dụng | `CHINH` +150 (×1,5), `MA` 200 − 75 = 125 |
| SO-AC-013i | Tán tu nhận `TA +100` | Áp dụng | `TA` +100, các phe khác không đổi |
| SO-AC-013j | Đã nhận 300 `CHINH` trong ngày | Thêm từ nhiệm vụ hằng ngày | 0 (trần ngày) |
| SO-AC-013k | Nhân vật hạng 37 Thiên Bảng tọa hóa | Thông báo | Toàn server, ưu tiên 3, có hạng |
| SO-AC-013l | Nhân vật hạng 250 tử vong ở hiểm địa | Thông báo | Không toàn server; bạn bè / tông môn / tổ đội nhận tin |
| SO-AC-013m | Client tự gửi `combatPower` | API | Bỏ qua (chỉ nhận từ Game Server) |
| SO-AC-013n | Thành viên Hợp Hoan Tông kiêm tông tự lập có `EDICT_CULTIVATION`, gói `NPC_SECT` | Đả tọa song tu ở linh mạch | Chỉ +10% song tu (Hợp Hoan); **không** cộng +5% tông quy |
| SO-AC-013o | Như trên, đổi gói sang `PLAYER_SECT` | Đổi lại sau 3 ngày | Từ chối (hồi chiêu 7 ngày) |
| SO-AC-013p | Gói `NPC_SECT` | Dùng Đan Phòng trú địa tông tự lập | Vẫn được (+2 thành công, SO-R-04M) |
| SO-AC-013q | Tán tu `LK7` | Xem Tạp Hóa | Có `RCP_TRUC_CO_DAN` giá 3.000 linh thạch |
| SO-AC-013r | Tán tu nhận `CHINH +100`, đang có `MA` 200 | Áp dụng | `MA` 200 − 25 = 175 |
| SO-AC-013s | Tông tự lập cấp 3 | Chưởng môn chọn 3 tông quy | Từ chối (tối đa 2) |
| SO-AC-013t | Rời Thanh Vân Tông khi đang gói `NPC_SECT`, vẫn ở tông tự lập | Sau đó | Tự chuyển `PLAYER_SECT`, không tính hồi chiêu |
| SO-AC-014 | `TC1` với `contributionTotal` 1.990 | Nhận thêm 10 cống hiến | Lên `NOI_MON` |
| SO-AC-015 | Nhận cống hiến lần thứ n trong ngày làm tổng vượt 300 | Cộng | Chỉ cộng tới 300 |
| SO-AC-016 | Hộ pháp thứ 6 trong ngày | Đột phá của người khác kết thúc | Không nhận karma (trần 5) |
| SO-AC-017 | Xem thông tin người chơi khác | Mở bảng | Có tuổi, không có thọ tối đa, không có thiên phú / linh căn / thuộc tính phụ |
| SO-AC-018 | 5 tài khoản khác nhau báo cáo X trong 8 phút | Hệ thống | X bị cấm chat 30 phút, chỉ dùng được `PARTY` |
| SO-AC-019 | 2 thông báo ưu tiên 2 và 1 cùng chờ | Hàng đợi phát | Ưu tiên 1 phát trước; khoảng cách giữa hai tin ≥ 60s |
| SO-AC-020 | `hideNameInBroadcast = true`, đột phá Kim Đan | Thông báo | "Một vị đạo hữu" |
| SO-AC-021 | (Beta) Lập tông môn với nhân vật `LK9` | API | Từ chối (`TC1`) |
| SO-AC-022 | (Beta) Ví dụ §6 | Tính | Phí duy trì 3.500; nâng Đan Phòng lên 4 tốn 80.000 |
| SO-AC-023 | (Beta) Tông khố 2.000, phí duy trì 3.500 | Đến hạn | Công trình ngừng hiệu lực, cấp giữ nguyên |
| SO-AC-024 | (Beta) Thành viên hết kiếp | Kiếp mới | Vẫn trong tông môn, giữ chức vụ, hiện danh hiệu "Chuyển thế" |
| SO-AC-025 | (Beta) Tài khoản đã thuộc tông môn A | Xin vào tông môn B | Từ chối |

---

## 16. Phạm vi MVP vs sau này

| Hạng mục | Phase 2 | MVP | Beta | Sau launch |
|----------|:---:|:---:|:----:|:----------:|
| Tổ đội, khóa đội, đánh dấu | ✅ | | | |
| Chat 5 kênh + lọc + liên kết vật phẩm | ✅ | | | |
| Bạn bè, chặn (tài khoản) | | ✅ | | |
| Thanh Vân Tông: gia nhập, bậc, cống hiến, chuyển thế đệ tử | | ✅ | | |
| Tạc Thiên Bang, Hợp Hoan Tông (tổng đàn, nhánh gia nhập, Bảo Khố, phúc lợi, nhiệm vụ hằng ngày), phản môn, phe | | | ✅ | |
| Bảng Chiêu Mộ | | ✅ | | |
| Hàng đợi thông báo chung, báo cáo / tự cấm chat | | ✅ | | |
| Phần thưởng hộ pháp | | ✅ | | |
| Tông môn tự lập: chức vụ, tông khố, trú địa, 3 công trình | | | ✅ | |
| Chân truyền (Thanh Vân Tông) | | | ✅ | |
| Sư đồ | | | | ✅ |
| Đạo lữ, kết bái | | | | ? |

---

## 17. Câu hỏi mở
- [ ] **SO-Q-1:** Kênh `WORLD` yêu cầu `LK3` + 2 giờ có đủ chặn spam tài khoản mới? Cân nhắc thêm yêu cầu xác minh số điện thoại cho kênh này.
- [ ] **SO-Q-2:** Có cho tông môn tự lập "chiêu mộ" tự động qua Bảng Chiêu Mộ không?
- [x] **SO-Q-3:** *(Đã chốt 2026-10-23: thêm **Tạc Thiên Bang** (tà / giang hồ) và **Hợp Hoan Tông** (ma đạo) ngay từ Vực 1, chọn khi bái sư chương 1, nội dung ở Beta — §5.4b.)* Thanh Vân Tông có nên có thêm 1–2 đại tông NPC khác?
- [x] **SO-Q-6:** *(Đã duyệt 2026-10-23: `BG_TAC_THIEN_GANG`, `BG_HOP_HOAN_DISCIPLE` — SO-R-04B, GD-27.)* Có thêm hai xuất thân "Bang chúng Tạc Thiên" và "Hợp Hoan đệ tử"?
- [x] **SO-Q-7:** *(Đã duyệt 2026-10-23: ba thanh danh vọng, chính ↔ ma đối nghịch, tà trung lập, 5 bậc, cửa hàng phe Vực 2 — §5.4c.)* Danh vọng chính / tà / ma có nên thành thanh điểm?
- [ ] **SO-Q-4:** Bạn bè có nên thấy vị trí bản đồ mặc định, hay mặc định ẩn?
- [x] **SO-Q-5:** *(Đã chốt 2026-10-23: chỉ thông báo toàn server cho nhân vật trong **top 100 Thiên Bảng Chiến Lực**; còn lại báo bạn bè / tông môn / tổ đội — SO-R-04I, SO-R-04J.)* Thông báo tọa hóa ưu tiên 3 cho mọi nhân vật ≥ `TC1` có làm kênh hệ thống quá ồn khi server đông?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.7 | 2026-10-23 | — | Sửa khóa `npc_sect_membership` thành `(character_id, sect_id)` + `state` / `left_at` (TD-03 §8) |
| v0.6 | 2026-10-23 | — | D-21: SO-R-005, SO-R-048 cập nhật theo PvP dã ngoại (GD-32) |
| v0.5 | 2026-10-23 | — | **Gói phúc lợi môn phái** (§5.4e): ở được cả tông NPC và tông tự lập nhưng chỉ hưởng 1 gói (`NPC_SECT` / `PLAYER_SECT` / `WANDERER`), đổi mỗi 7 ngày; **Tông quy** 8 lựa chọn theo cấp tông (SO-R-05A); gói **Tán tu tự tại**; Tông Khố đổi thưởng bằng merit; Trúc Cơ Đan mở ở mọi con đường |
| v0.4 | 2026-10-23 | — | Duyệt SO-Q-6 (xuất thân Tạc Thiên / Hợp Hoan, SO-R-04B), SO-Q-7 (**danh vọng phe** §5.4c: 3 thanh, chính ↔ ma đối nghịch, tà trung lập, 5 bậc, cửa hàng phe Vực 2), chốt SO-Q-5 (**Thiên Bảng Chiến Lực** §5.4d: thông báo tử vong toàn server chỉ cho top 100) |
| v0.3 | 2026-10-23 | — | Chốt SO-Q-3: **ba đại tông NPC** — thêm **Tạc Thiên Bang** (tà, Tạc Thiên Trại F03: hiểm địa, hắc thị) và **Hợp Hoan Tông** (ma, Hợp Hoan Cốc F05: song tu, mị thuật) (§5.4b); `NpcSectId`, `Alignment`, `NpcSectDef`; một tông mỗi kiếp + phản môn; Trúc Cơ Đan ở mọi Bảo Khố; giới hạn phúc lợi; định hướng nội dung; SO-Q-6, SO-Q-7 |
| v0.2 | 2026-10-22 | — | Linh Mạch Điện hạ hệ số xuống trần 1,35 (trước: 1,5–1,75, mạnh hơn linh mạch ngoài đồng mà tuổi chỉ ×0,25); trú địa vào từ sơn môn (GD-30) |
| v0.1 | 2026-10-21 | — | Bản nháp đầu: bất biến (tách Thanh Vân Tông NPC / tông môn tự lập), tổ đội & Chiêu Mộ, chat 6 kênh + lọc + kiểm duyệt, bạn bè & chặn theo tài khoản, Thanh Vân Tông (bậc, cống hiến, chuyển thế đệ tử), tông môn tự lập (Beta: chức vụ, tông khố, trú địa, công trình, duy trì), hàng đợi thông báo chung, phần thưởng hộ pháp, xem thông tin, công thức, API, DB, nghiệm thu; sửa lỗi đánh số GD-15 → GD-14 |

