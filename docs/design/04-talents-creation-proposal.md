# Đề xuất đặc tả triển khai tạo nhân vật

| Thuộc tính | Giá trị |
|------------|---------|
| Trạng thái | Các quyết định cấu trúc đã được duyệt; nội dung gamedata còn thiếu vẫn chờ nguồn chuẩn |
| Phạm vi | Chọn server, dữ liệu tạo nhân vật, ngoại hình và transaction xác nhận |
| Nguồn | GD-27, GD-26, TD-03, TD-04, TD-07; quyết định người dùng đã chốt về generation theo server |

> Các quyết định cấu trúc ở §2–§5 đã được người dùng duyệt và đang được đồng bộ vào tài liệu chuẩn. Người dùng cung cấp `serverId = 3`, nhưng `nameKey`, bản dịch và trạng thái server chưa được chốt. Appearance catalog và danh sách tên cấm chưa có nguồn; starting package dưới đây là đề xuất, chưa được duyệt và chưa được seed.

## 1. Hiện trạng và điểm lệch

- GD-27 đã quy định roll linh căn, chỉ số bẩm sinh, thiên phú, xuất thân, lượt roll, khóa dòng, pity và khởi đầu tại `V1_F01`; GD-27 §7 cũng liệt kê các file gamedata dự kiến.
- `shared/gamedata/registry.json` hiện chỉ đăng ký dữ liệu world và stats. Chưa có file nguồn talents, backgrounds, creation parameters, appearance presets hoặc i18n cho nội dung này; `shared/gamedata-types` cũng chưa có các kiểu GD-27.
- Contract đã có catalog backgrounds/talents, odds và các API attempt/roll/confirm. Server selection, `serverId` trong attempt/summary, `baseAppearance` và naming limit đã được đồng bộ. Việc mở/resume attempt chưa roll và chọn `backgroundId` ở lần gọi roll đầu đã được chốt trong GD-27 §4.1/TLN-R-065.
- Thiết kế ban đầu đã có `server_id` và giới hạn một nhân vật `ALIVE`/`ENDING` trên mỗi `(account_id, server_id)`, nhưng `account_meta.generation_count` và unique index attempt còn giới hạn theo account. Migration hiện đã chuyển generation và attempt sang `(account_id, server_id)`.
- `game.character_appearance.slots` đã được dành cho appearance/cosmetic slots, nhưng chưa có schema preset/màu của nhân vật tạo mới.
- Bảng xuất thân nêu nội dung khởi đầu ở mức mô tả; nhiều item/recipe chưa có định danh được nối tới các bộ dữ liệu chuẩn.

## 2. Đề xuất lựa chọn server và generation

### 2.1. Mô hình dữ liệu

1. Dùng một PostgreSQL chung cho nhiều game server; `server_id` là định danh ổn định, kiểu `SMALLINT` như bảng `character` hiện tại.
2. Tách số generation khỏi metadata toàn account bằng `account_server_meta(account_id, server_id, generation_count)`. `account_meta` tiếp tục giữ rebirth points, unlocks, vault và tiến trình thật sự cấp account.
3. Thêm `server_id` vào `creation_attempt`; giới hạn một attempt chưa confirm theo `(account_id, server_id)`. Attempt lưu generation kế tiếp và server được chọn.
4. Khi mở attempt, generation là `account_server_meta.generation_count + 1`; chỉ tăng bộ đếm trong transaction confirm thành công cùng lúc tạo character. Mọi lần roll/confirm lấy server từ attempt đã lưu, không tin server ID gửi lại.
5. Unlock vẫn thuộc account; điều kiện phụ thuộc generation được đánh giá theo `max(generationCount)` trên các server của account. Khi tạo nhân vật, generation mới vẫn được tính riêng theo server được chọn.

### 2.2. Catalog và API

Đã thêm catalog server quản trị bằng dữ liệu DB, thay vì cấu hình rải rác ở từng process:

- Bảng registry có ID ổn định và `nameKey`; trạng thái dùng để ngăn tạo nhân vật trong thời gian bảo trì/đóng server.
- Thêm `GET /v1/servers` để client hiển thị lựa chọn hợp lệ.
- Thêm body `{ serverId }` cho `POST /v1/creation/attempts`; thêm `serverId` vào `CreationAttempt` và `CharacterSummary`.

Trạng thái đã chốt và có trong contract: `OPEN` nhận tạo nhân vật; `MAINTENANCE` hiện trong danh sách nhưng không chọn được; `CLOSED` không trả về danh sách.

`serverId = 3` đã được cung cấp. Cần xác nhận `nameKey`, bản dịch hiển thị và trạng thái ban đầu (`OPEN`, `MAINTENANCE` hoặc `CLOSED`) trước khi thêm dòng registry; không tự suy diễn các giá trị này.

### 2.3. Chuyển đổi dữ liệu generation

Migration hiện tạo bảng theo server nhưng chủ động dừng nếu đã có character, creation attempt hoặc generation count account-global khác 0. Với dữ liệu cũ, cần lập kế hoạch ánh xạ sang server và chạy migration dữ liệu có kiểm soát trước khi áp dụng thay đổi schema; không gán server mặc định hoặc phân bổ generation im lặng.

## 3. Đề xuất hoàn thiện nguồn gamedata GD-27

### 3.1. Những gì có thể chuyển trực tiếp từ spec

Tạo kiểu TypeScript nguồn theo TD-07 và đăng ký các file GD-27 §7:

- `shared/gamedata-types` cho `TalentDef`, `Background`, creation params, root tables và awakening weights.
- `shared/gamedata/talents/params.json`, `talents.json`, `backgrounds.json`, `root.json`, `awakening.json`.
- Bổ sung registry, validator tham chiếu và i18n keys.

Các tham số đã được GD-27 ghi cụ thể (ví dụ rarity weights, `innateStatTotal`, `baseRerolls`, `mutationChance`, `rootGradeWeights`, `gradeBase`) được chép nguyên trạng từ spec, không tạo bảng xác suất mới. Cần chuyển cả ví dụ thuật toán §6.1 thành test vector chuẩn để Java API và mọi bản mô phỏng dùng cùng thứ tự rút số theo TA-R-070/071.

### 3.2. Những dữ liệu chưa đủ để nhập

- Mỗi background cần `statOffsets`, điều kiện unlock/root rules và starting package có ID tham chiếu. Các ID/package đang có và phần đề xuất:

| Background | Dữ liệu nguồn đã có | Đề xuất / còn thiếu |
|---|---|---|
| `BG_VILLAGE_YOUTH` | 100 linh thạch | Tạo item template chuẩn cho đồ vải và kiếm gỗ trong GD-09; chưa có item ID hoặc chỉ số cân bằng để tham chiếu. |
| `BG_HERB_BOY` | 50 linh thạch; `RCP_HOI_KHI_DAN`, `RCP_CAM_HUYET_DAN`, `ITM_FURNACE_T1`; 100 kinh nghiệm luyện đan (GD-16 AL-R-101) | Có thể dùng các ID đã định nghĩa; đưa đầy đủ các grant vào gamedata background. |
| `BG_SMITH_APPRENTICE` | 50 linh thạch; `RCP_THIET_KIEM`, `ITM_HAMMER_T1`; 100 kinh nghiệm luyện khí (GD-17 FG-R-040) | Có thể dùng các ID đã định nghĩa; đưa đầy đủ các grant vào gamedata background. |
| `BG_NOBLE` | 1.000 linh thạch | Cần item kiếm có `requiredRealm = LK3`, item áo gấm và định nghĩa nguồn cho `FLAG_FAMILY_ENEMY`; các ID hiện chưa có. Không thay bằng kiếm `LK4` hoặc một item gần giống. |
| `BG_ORPHAN` | 0 linh thạch; karma +10 | Cần item áo rách có ID chuẩn; tag `orphan` đã có trong GD-27. |
| `BG_SECT_SERVANT` | 30 linh thạch; gia nhập Thanh Vân rank `NGOAI_MON` | Khởi tạo membership của nhân vật và `FLAG_ACC_JOINED_SECT`, vì tài khoản thực sự đã gia nhập; dùng ID sect chuẩn. |
| `BG_TAC_THIEN_GANG` | 30 linh thạch; 1 `ITM_TRUYEN_TONG_PHU`; rank `TAP_DICH`; karma −5 | Item và rank đã có nguồn; khởi tạo membership cùng `FLAG_ACC_JOINED_TAC_THIEN`; cần xác minh ID sect. |
| `BG_HOP_HOAN_DISCIPLE` | 30 linh thạch; rank `TAP_DICH` | Khởi tạo membership cùng `FLAG_ACC_JOINED_HOP_HOAN`; cần item quạt gỗ và ID sect chuẩn, tạo template trong GD-09 trước khi tham chiếu. |
| `BG_OUTCAST` | 0 linh thạch; quy tắc linh căn và thiên phú ẩn trong TLN-R-062 | Không thêm vật phẩm; giữ các bảo đảm roll đã quy định. |

**Đề xuất triển khai:** giữ nguyên số dư, kinh nghiệm, karma và hiệu ứng đã nêu trong GD-27/GD liên quan; grant chúng atomically trong transaction confirm. Dùng các ID đã có cho Dược đồng, Thợ rèn và Bang chúng Tạc Thiên. Với đồ vải/kiếm gỗ, kiếm LK3/áo gấm, áo rách và quạt gỗ, bổ sung item definitions và chỉ số cân bằng vào GD-09 trước; bổ sung `FLAG_FAMILY_ENEMY` vào registry flags với scope/consumer rõ ràng. Các cờ `FLAG_ACC_JOINED_*` là sự kiện account thực tế, nên được ghi khi starting background khởi tạo membership; theo SO-R-041, chúng cũng là account-scoped. Không tạo ID placeholder hoặc thay item bằng template khác tier.
- Mỗi talent cần đủ `requires`, `unlockId`, `exclusiveGroup`, `hiddenAtCreation`, `revealOn`, `tags`, `effects` và i18n. Bảng mô tả GD-27 là nguồn để nhập, nhưng validator phải xác minh không có pool rỗng sau điều kiện/unlock.
- Cần có quy trình tạo `gamedataVersion` và bundle server dùng bởi API Service; `rng_audit` phải lưu đúng seed, inputs, outcome và version được dùng.
- Cần danh sách từ bị cấm cho tên nhân vật và quy tắc cập nhật danh sách; TLN-R-025 yêu cầu lọc nhưng chưa chỉ rõ nguồn từ điển.

## 4. Đề xuất appearance

Giữ lựa chọn preset và màu như D-08, nhưng chốt contract theo allowlist dữ liệu thay vì nhận chuỗi tùy ý:

- Preset và các slot màu được khai báo trong một catalog có version, dùng chung giữa client và API Service.
- Client gửi `appearancePresetId` cùng map `{ colorSlotId: paletteColorId }`; API chỉ chấp nhận preset, slot và màu có trong catalog/version đang chạy. Không nhận arbitrary CSS/hex để tránh dữ liệu không hợp lệ.
- Lưu dữ liệu nền tảng của nhân vật tách biệt với cosmetic slots đã trang bị. Đề xuất thêm object `baseAppearance` vào character persistence; không tái sử dụng `character_appearance.slots` nếu chưa xác nhận semantics của bảng đó.
- `GET /v1/creation/appearance-presets` không bắt buộc nếu catalog được phân phối trong gamedata bundle và client đã tải cùng version; ưu tiên cách này để tránh API catalog trùng nguồn.

Đã chốt cách lưu riêng `baseAppearance` và dùng allowlist gamedata. Còn thiếu định dạng asset/preset, slot màu và palette IDs từ nguồn asset, cùng quy tắc tương thích khi preset bị deprecated; chưa có các ID này trong repo.

## 5. Hợp đồng và transaction cần hoàn thiện

Contract đã được cập nhật theo API-R-001. Phần nghiệp vụ cần triển khai sau khi có dữ liệu chuẩn:

1. `GET /v1/servers` trả catalog có thể chọn.
2. `POST /v1/creation/attempts` nhận `serverId`; resume đúng attempt của account/server và trả lịch sử roll. Attempt mới chưa roll; lần gọi roll đầu nhận `backgroundId`.
3. Roll kiểm tra ownership, server-scoped generation, unlocks và giới hạn theo GD-27; mọi RNG được thực hiện ở API Service và audit trong cùng transaction.
4. Confirm kiểm tra tên, filter, appearance catalog, tên trùng trong server và roll thuộc lịch sử attempt. Server chỉ lấy từ attempt persistence.
5. Một transaction duy nhất tạo character, base appearance, vật phẩm khởi đầu, recipes, flags, talent rows, cập nhật server generation, đánh dấu attempt confirmed và ghi các outbox events đã định nghĩa.
6. Giữ `Idempotency-Key` cho mọi POST có side effects. Unique violation race của character/name phải được map theo mã lỗi đã có trong `errors.yaml`; không thêm error code mới nếu chưa sửa spec/contracts.

Các API odds/background/talent catalogs đang có cần đọc cùng bundle/version và áp unlock state của account; không trả catalog rỗng thành công khi gamedata chưa nạp được.

## 6. Trình tự triển khai tiếp theo

1. Cấp registry values cho server đầu tiên và hoàn thiện appearance/name-filter data.
2. Thêm types, source gamedata, registry, i18n và validation; xác minh item/recipe references.
3. Triển khai attempt/resume, server-side roll và confirm transaction sau khi có đủ nguồn dữ liệu.
4. Validate contract, gamedata, migration và build. Tuân thủ lưu ý hiện có: không chạy unit tests nếu không được yêu cầu.

## 7. Trạng thái quyết định và dữ liệu còn thiếu

- [x] DB chung; generation và attempt theo `(account_id, server_id)`; account unlocks/vault giữ scope hiện tại.
- [x] Điều kiện generation của unlock cấp account dùng generation cao nhất trên bất kỳ server nào; unlock đã mua dùng chung.
- [x] DB-backed server registry và `GET /v1/servers`; trạng thái `OPEN`, `MAINTENANCE`, `CLOSED` với semantics ở §2.2.
- [x] Mở/resume attempt chưa roll; client chọn `backgroundId` khi gửi roll đầu.
- [x] Appearance từ gamedata allowlist, lưu riêng dưới `baseAppearance`; không tái sử dụng cosmetic slots.
- [x] Tên NFC, 2–12 code points, bảng chữ cái tiếng Việt + một dấu cách đơn; từ cấm versioned ở server.
- [x] Chỉ nhập dữ liệu được quy định rõ trong GD-27; không tự đặt item/recipe IDs.
- [x] Người dùng cấp `serverId = 3` cho server đầu tiên.
- [ ] Chốt `nameKey`, bản dịch i18n và trạng thái khởi tạo cho server `3`.
- [ ] Cung cấp/publish appearance preset, color slot và palette IDs từ asset source.
- [ ] Đối chiếu và hoàn thiện mọi starting item, recipe, flag và economy state bằng ID chuẩn từ GD-09/các GD liên quan.
- [ ] Duyệt đề xuất starting package tại §3.2; bổ sung item definitions/flag source còn thiếu trước khi seed.
- [ ] Cấp danh sách từ cấm versioned; tạo nhân vật phải fail closed cho tới khi danh sách được nạp.
