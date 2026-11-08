# TD-03 — Database Schema

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | TD-03 · Prefix ID: `DB` |
| Owner | Tech Lead (Server) |
| Trạng thái | 🟡 Bản nháp v0.6 |
| Cần trước | Phase 1 (trước migration đầu tiên) |
| Quyết định nền | D-06 / D-19 (PostgreSQL + Valkey, chỉ API Service ghi DB), [TD-01](00-tdd.md) §5, §7–§10, [TD-07](06-gamedata.md) (ID gamedata) |
| Liên quan | Mục "Lưu trữ" (§9 / §10) của mọi GD · [TD-01 TDD](00-tdd.md) · [TD-02 Network](01-network.md) · [GD-26 Vòng đời](../design/03-lifecycle.md) (dữ liệu qua kiếp) · PLAN §5.4 (bản phác cũ — **được thay bằng tài liệu này**) |

> Các GD giữ **DDL chi tiết** của bảng mình sở hữu (mục "Lưu trữ"). TD-03 là **luật chung + sổ đăng ký toàn bộ bảng + bảng lõi không thuộc GD nào + cách xử lý dữ liệu khi hết kiếp + phân vùng / lưu giữ / migration**. Viết migration: đọc TD-03 (quy ước) rồi DDL ở GD sở hữu; mâu thuẫn về **quy ước** (tên, kiểu, khóa) → TD-03 thắng; về **ý nghĩa cột** → GD thắng.

---

## 1. Mục tiêu & bất biến

| Mã | Bất biến |
|----|----------|
| <a id="db-r-001"></a>**DB-R-001** | Chỉ **API Service** đọc / ghi PostgreSQL (TD-01 TA-R-001). Game Server, công cụ, GM panel đi qua API. |
| <a id="db-r-002"></a>**DB-R-002** | Mỗi bảng có **đúng một module sở hữu** (§5). Module khác chỉ đọc qua interface của module sở hữu (TD-01 TA-R-030); ngoại lệ đọc trực tiếp: `account`, `character` (bảng lõi, chỉ đọc). |
| <a id="db-r-003"></a>**DB-R-003** | Dữ liệu có giá trị thay đổi **kèm ledger trong cùng transaction** (TD-01 TA-R-004); ledger chỉ thêm (TA-R-061). |
| <a id="db-r-004"></a>**DB-R-004** | **Không** lưu dữ liệu dẫn xuất làm nguồn sự thật (tuổi, thọ tối đa, chỉ số chiến đấu, cảnh giới hiển thị): lưu đầu vào, tính khi đọc. Ngoại lệ có chủ đích được đánh dấu `-- cache` (vd. `character.combat_power` cho bảng xếp hạng). |
| <a id="db-r-005"></a>**DB-R-005** | ID gamedata (`ITM_*`, `TAL_*`…) lưu dạng `VARCHAR`, **không** có FK (gamedata nằm ngoài DB, TD-07); tính hợp lệ do code + validator TD-07 (ID không bao giờ bị xóa, DT-R-002). |

---

## 2. Quy ước

| Hạng mục | Quy ước |
|----------|---------|
| <a id="db-r-010"></a>Tên | `snake_case`, **số ít** (`item_instance`, không `item_instances`); bảng nhật ký hậu tố `_log` / `_ledger`; bảng liên kết `a_b` |
| Schema PostgreSQL | Một schema `game` (MVP). Ranh giới module do sổ đăng ký §5 + kiểm tra CI (DB-R-011), không tách schema vật lý |
| Khóa chính | `id UUID` (UUIDv7, sinh ở API — TD-01 §6.4) cho thực thể; khóa tự nhiên ghép cho bảng phụ thuộc (`(character_id, slot)`); `BIGSERIAL` / `BIGINT GENERATED ALWAYS AS IDENTITY` cho log |
| Thời gian | `TIMESTAMPTZ` (UTC), tên `*_at`; ngày nghiệp vụ `DATE` (`day`); thời lượng `*_sec INT` |
| Tiền / số lượng | `BIGINT` (linh thạch, merit…), `INT` (số lượng vật phẩm) |
| Tỷ lệ / hệ số | `NUMERIC(p,s)` (không dùng `REAL`/`DOUBLE` cho giá trị nghiệp vụ) |
| Enum | `VARCHAR(n)` + `CHECK (col IN (...))` sinh từ `shared/contracts` (không dùng `CREATE TYPE ENUM` — khó migrate) |
| JSONB | Chỉ cho dữ liệu linh hoạt không cần lọc thường xuyên (thuộc tính phụ, ảnh chụp, payload); trường cần lọc / join → cột riêng |
| Khóa ngoại | Có FK **trong** module và tới `account` / `character`; **không** FK chéo module khác (giữ khả năng tách service), toàn vẹn chéo module bằng code + job đối soát |
| Xóa | Không xóa cứng dữ liệu nghiệp vụ (trừ bảng tạm có TTL); dùng trạng thái (`state`, `departed_at`) |
| Khóa lạc quan | Bảng hay bị sửa đồng thời có `version INT NOT NULL DEFAULT 0` (`UPDATE … WHERE version = ?`) |
| Index | Tên `ix_{bảng}_{cột}` / `ux_` (unique) / partial index cho trạng thái hoạt động |

- <a id="db-r-011"></a>**DB-R-011** CI kiểm tra: mỗi bảng trong migration có trong sổ đăng ký §5; repository / jOOQ class của bảng chỉ nằm trong package của module sở hữu (ArchUnit).

---

## 3. Hạ tầng

> Quy trình backup, diễn tập khôi phục, DR: [TD-09 §5](08-infra-ops.md).
- PostgreSQL 16+, 1 primary (+ 1 replica đọc cho Metabase / analytics khi cần), **PgBouncer** chế độ transaction pooling; ngân sách kết nối API Service ≤ 80% `max_connections`.
- WAL archiving + **PITR**, base backup hằng ngày; **RPO ≤ 5 phút, RTO ≤ 1 giờ**; diễn tập khôi phục hằng tháng (PLAN §5.6).
- Phân vùng bằng **pg_partman** (§7). Extension: `pgcrypto` (không bắt buộc, UUID sinh ở app), `pg_stat_statements`.

---

## 4. Phạm vi dữ liệu & xử lý khi hết kiếp

Mỗi bảng có một **phạm vi** quyết định số phận khi nhân vật hết kiếp (GD-26 §5.8 bước 9, §5.15):

| Phạm vi | Ý nghĩa | Khi hết kiếp |
|---------|---------|--------------|
| `ACCOUNT` | Thuộc tài khoản, qua mọi kiếp | **Giữ** |
| `CHARACTER` | Thuộc một kiếp | Đóng băng (giữ để tra cứu / Bia Vạn Cổ), không dùng cho kiếp mới |
| `CHARACTER_SEALABLE` | Tài sản cất giữ của kiếp | **Phong ấn** sang Di Tàng (`SEALED`, cấp tài khoản) |
| `CHARACTER_DROPPED` | Tài sản mang theo | **Hủy** / chuyển di hài |
| `ACCOUNT_IN_SECT` | Thuộc tài khoản trong tông môn tự lập | Giữ (mất khi rời tông) |
| `GLOBAL` | Toàn server (sơn môn, đấu giá…) | Không liên quan |
| `LOG` | Nhật ký / kiểm toán | Giữ theo hạn lưu (§7) |

**Ma trận "cái gì giữ qua kiếp" (kỹ thuật):**

| Dữ liệu | Bảng | Phạm vi | Khi hết kiếp |
|---------|------|---------|--------------|
| Tàng Bảo Các, linh thạch kho | `account_vault`, `account_meta.vault_spirit_stones` | ACCOUNT | Giữ |
| Kho đồ, thư chưa nhận, đồ treo đấu giá | `item_instance` (`WAREHOUSE`/`MAIL`/`AUCTION`) | CHARACTER_SEALABLE | → `SEALED` (Di Tàng), Di Tàng cũ còn sót → hủy |
| Túi, đồ đang mặc, túi hiểm địa, ví | `item_instance` (`BAG`/`EQUIP`/`DANGER_BAG`), `currency_wallet` (CHARACTER) | CHARACTER_DROPPED | Hủy / di hài; 20% ví → kho (tọa hóa), 10% (binh giải) |
| Di Tàng | `legacy_stash`, `item_instance` (`SEALED`) | ACCOUNT | Thay bằng Di Tàng mới |
| Động phủ, công trình, cây, linh thú | `cave`, `cave_plot`, `beast` | ACCOUNT | Giữ, `caveSealed = true` |
| Điểm Luân Hồi, unlock | `account_meta`, `rebirth_ledger` | ACCOUNT | Giữ (+ điểm mới) |
| Ký ức nghề | `profession_memory` | ACCOUNT | Cập nhật `max_level`, hợp công thức |
| Fog, truyền tống trận, codex | `map_discovery`, `discovered_poi`, `codex_entry` | ACCOUNT | Giữ |
| Cờ tài khoản, lịch sử cơ duyên tài khoản | `account_flag`, `encounter_account_history` | ACCOUNT | Giữ |
| Bạn bè, chặn, tông tự lập, merit | `friendship`, `block_entry`, `player_sect_member` | ACCOUNT / ACCOUNT_IN_SECT | Giữ |
| Linh thú kết khế | `beast.contract_character_id` | ACCOUNT | Chọn giữ → `STABLED` (phong ấn cùng động phủ), còn lại `DEPARTED` |
| Cảnh giới, tu vi, thiên phú, kỹ năng, nghề, công thức | `character`, `character_talent`, `character_technique`, `profession`, `recipe_known` | CHARACTER | Đóng băng |
| Tông NPC, cống hiến, danh vọng, sát nghiệp, gói phúc lợi | `npc_sect_membership`, `currency_wallet` (`SECT_CONTRIBUTION`), `faction_reputation`, `pk_state`, `character_benefit` | CHARACTER | Đóng băng (cống hiến hủy) |
| Bí mật đã tìm, cờ kiếp, nhiệm vụ | `character_secret`, `character_flag`, `quest_progress` | CHARACTER | Đóng băng |
| Bia Vạn Cổ | `life_record` | ACCOUNT (công khai) | Tạo mới |

- <a id="db-r-020"></a>**DB-R-020** Quy trình kết thúc kiếp chạy **set-based** (một vài câu `UPDATE … WHERE owner_character_id = ? AND location IN (...)`), không vòng lặp từng vật phẩm; `item_log` ghi bằng `INSERT … SELECT`. Mục tiêu: < 200ms cho nhân vật 200 vật phẩm.

---

## 5. Sổ đăng ký bảng

Cột: **Bảng** · **Module** (TD-01 §5.1) · **Nguồn DDL** · **Phạm vi** (§4) · **Ghi chú** (P = phân vùng tháng, R = hạn lưu, V = có `version`).

### 5.1. Nền tảng & tài khoản
| Bảng | Module | Nguồn DDL | Phạm vi | Ghi chú |
|------|--------|-----------|---------|---------|
| `server_registry` | platform | TD-04 / GD-27 | GLOBAL | Danh mục server chọn được; ID ổn định |
| `account` | account | **TD-03 §6** | ACCOUNT | |
| `account_session` | account | **TD-03 §6** | ACCOUNT | `device_hash` lưu SHA-256 dạng BYTEA, nullable (TD-08) |
| `account_meta` | lifecycle | GD-26 §9 (+ GD-09 `bag_expansion`) | ACCOUNT | V |
| `account_server_meta` | lifecycle | GD-26 §9 | ACCOUNT × SERVER | Generation theo server |
| `account_flag` | encounter | GD-22 §10 | ACCOUNT | |
| `idempotency_record` | platform | **TD-03 §6** | LOG | R 7 ngày |
| `outbox_event` | platform | **TD-03 §6** | LOG | R 7 ngày |
| `consumed_event` | platform | **TD-03 §6** | LOG | R 7 ngày |
| `rng_audit` | platform | TD-01 §9.2 | LOG | P, R 18 tháng |
| `death_log` | platform | **TD-03 §6** | LOG | R 18 tháng |
| `admin_audit` | admin | **TD-03 §6** | LOG | R 5 năm |
| `player_report` | social | GD-14 §10 | LOG | |
| `fortune_dispute` | admin | **TD-03 §6** (GD-29 §10) | LOG | |
| `security_flag`, `sanction`, `login_attempt`, `appeal` | security *(module mới)* | TD-08 | ACCOUNT / LOG | `login_attempt` R 90 ngày |
| `purchase_receipt` | security | TD-08 §8 | ACCOUNT | Biên lai mua hàng (web / App Store / Google Play) |

### 5.2. Nhân vật, vòng đời, thiên phú, tu luyện
| Bảng | Module | Nguồn DDL | Phạm vi | Ghi chú |
|------|--------|-----------|---------|---------|
| `character` | character | **TD-03 §6** (cột từ nhiều GD) | CHARACTER | V; khóa hàng cho thao tác sinh tử |
| `lifespan_ledger` | lifecycle | GD-26 §9 | LOG | P |
| `age_accrual_report` | lifecycle | GD-26 §9 | LOG | R 90 ngày |
| `severe_injury_log` | lifecycle | GD-26 §9 | LOG | P |
| `pill_usage` | lifecycle | GD-26 §9 | CHARACTER | |
| `life_record` | lifecycle | GD-26 §9 | ACCOUNT | Công khai |
| `rebirth_ledger` | lifecycle | GD-26 §9 | LOG | |
| `account_vault` | lifecycle | GD-26 §9 | ACCOUNT | |
| `legacy_stash` | lifecycle | GD-26 §9 | ACCOUNT | |
| `legacy_cave` | encounter | GD-22 §10 / GD-26 §5.14 | GLOBAL | Beta |
| `character_talent` | talent | GD-27 §10 | CHARACTER | |
| `talent_change_log` | talent | GD-27 §10 | LOG | |
| `talent_pending_choice` | talent | GD-27 §10 | CHARACTER | |
| `creation_attempt`, `creation_roll` | talent | GD-27 §10 | LOG | |
| `account_talent_seen` | talent | GD-27 §10 | ACCOUNT | |
| `cultivation_ledger` | cultivation | GD-04 §10 | LOG | P |
| `breakthrough_attempt` | cultivation | GD-04 §10 | CHARACTER | |
| `meditation_report` | cultivation | GD-04 §10 | LOG | R 90 ngày |
| `character_status` | character | GD-06 §10 | CHARACTER | Buff có thời hạn bền |

### 5.3. Kỹ năng & công pháp
| Bảng | Module | Nguồn DDL | Phạm vi | Ghi chú |
|------|--------|-----------|---------|---------|
| `character_technique` | technique | GD-07 §10 | CHARACTER | Thay `character_skill` (PLAN cũ) |
| `character_loadout` | technique | GD-07 §10 | CHARACTER | |
| `character_persistent_cooldown` | technique | GD-07 §10 | CHARACTER | |
| `mastery_batch` | technique | GD-07 §10 | LOG | R 30 ngày (idempotency) |
| `crafted_technique` | technique | GD-08 §10 | GLOBAL | Vĩnh viễn (TF-R-004) |
| `technique_forge_sandbox_use` | technique | GD-08 §10 | CHARACTER | R 7 ngày |

### 5.4. Vật phẩm & kinh tế
| Bảng | Module | Nguồn DDL | Phạm vi | Ghi chú |
|------|--------|-----------|---------|---------|
| `item_instance` | inventory | GD-09 §10 (+ cột GD-08, GD-11, GD-26, GD-28) | theo `location` | V; index `(owner_character_id, location)`, `(owner_account_id, location)` |
| `item_log` | inventory | GD-09 §10 | LOG | P, R ≥ 12 tháng online |
| `currency_wallet` | inventory | GD-09 §10 | CHARACTER / ACCOUNT | V |
| `currency_ledger` | inventory | GD-09 §10 | LOG | P |
| `character_quick_slot` | inventory | GD-09 §10 | CHARACTER | |
| `mail` | inventory | GD-09 §10 | CHARACTER_SEALABLE | |
| `auction_listing` | economy | GD-13 §10 | GLOBAL | |
| `trade_session`, `trade_daily_value` | economy | GD-13 §10 | LOG | |
| `shop_purchase_limit` | economy | GD-13 §10 | CHARACTER | |
| `economy_snapshot`, `economy_alert` | economy | GD-13 §10 | LOG | |

### 5.5. Chế tác
| Bảng | Module | Nguồn DDL | Phạm vi | Ghi chú |
|------|--------|-----------|---------|---------|
| `profession` | craft | GD-10 §10 | CHARACTER | |
| `profession_memory` | craft | GD-10 §10 | ACCOUNT | |
| `profession_trial` | craft | GD-10 §10 | LOG | |
| `recipe_known` | craft | GD-10 §10 | CHARACTER | |
| `craft_session` | craft | GD-10 §10 | CHARACTER | Ảnh chụp dữ liệu (DT-R-095) |
| `forge_item_state` | craft | GD-11 §10 | theo vật phẩm | Pity đi theo vật phẩm |
| `forge_pending_reroll` | craft | GD-11 §10 | CHARACTER | TTL |
| `forge_action_log` | craft | GD-11 §10 | LOG | P |

### 5.6. Thế giới, quái, cơ duyên, hiểm địa, nhiệm vụ
| Bảng | Module | Nguồn DDL | Phạm vi | Ghi chú |
|------|--------|-----------|---------|---------|
| `map_discovery` | world | **TD-03 §6** (GD-21) | ACCOUNT | Fog bitmap |
| `discovered_poi` | world | **TD-03 §6** (GD-21) | ACCOUNT | |
| `character_secret` | world | **TD-03 §6** (GD-21) | CHARACTER | |
| `codex_entry` | world | **TD-03 §6** (GD-21 §3.6, GD-12) | ACCOUNT | |
| `boss_kill_log` | world | GD-12 §10 | LOG | |
| `encounter_session`, `encounter_commit` | encounter | GD-22 §10 | CHARACTER | |
| `encounter_history`, `character_flag` | encounter | GD-22 §10 | CHARACTER | |
| `encounter_account_history` | encounter | GD-22 §10 | ACCOUNT | |
| `encounter_pity`, `encounter_chain_progress` | encounter | GD-22 §10 | CHARACTER | |
| `danger_run`, `danger_run_member` | danger | GD-28 §10 | CHARACTER | |
| `corpse` | danger | GD-28 §10 | GLOBAL | TTL theo GD-28 |
| `dz_dormant_strike` | danger | GD-28 §10 | LOG | |
| `quest_progress`, `quest_reward_log` | quest | GD-15 §10 | CHARACTER / LOG | |
| `sect_task_board` | quest | GD-15 §10 | CHARACTER | |
| `world_event_log`, `world_event_participation`, `world_event_daily`, `event_reminder` | world | GD-23 §10 | LOG / CHARACTER / ACCOUNT | `world_event_log` P |

### 5.7. Xã hội, tông môn, động phủ, linh thú, PvP
| Bảng | Module | Nguồn DDL | Phạm vi | Ghi chú |
|------|--------|-----------|---------|---------|
| `friendship`, `friend_request`, `block_entry` | social | GD-14 §10 | ACCOUNT | |
| `chat_message_log` | social | GD-14 §10 | LOG | Phân vùng **ngày**, R 30 ngày |
| `chat_mute` | social | GD-14 §10 | ACCOUNT | |
| `npc_sect_membership` | social | GD-14 §10 **(sửa khóa — §8)** | CHARACTER | |
| `faction_reputation` | social | **TD-03 §6** (GD-14 §5.4c) | CHARACTER | |
| `character_benefit` | social | **TD-03 §6** (GD-14 §5.4e) | CHARACTER | |
| `player_sect`, `player_sect_member`, `player_sect_ledger` | social | GD-14 §10 | GLOBAL / ACCOUNT_IN_SECT / LOG | `player_sect` V |
| `player_sect_edicts` | social | **TD-03 §6** (GD-14 SO-R-05A) | GLOBAL | |
| `cave`, `cave_plot` | home | GD-30 §10 | ACCOUNT | V |
| `sect_site`, `site_history`, `sect_prestige_week`, `sect_prestige_member_week` | home | GD-30 §10 | GLOBAL / LOG | |
| `beast`, `beast_breeding` | beast *(module mới)* | GD-25 §10 | ACCOUNT | `beast` V |
| `beast_pity`, `beast_capture_pity`, `beast_boss_capture` | beast | GD-25 §10 | ACCOUNT / CHARACTER | |
| `beast_log` | beast | GD-25 §10 | LOG | P |
| `pk_state` | pvp *(module mới)* | **TD-03 §6** (GD-32 §4) | CHARACTER | |
| `pvp_kill_log` | pvp | **TD-03 §6** | LOG | P, R 12 tháng |
| `cosmetic_entitlement`, `character_appearance`, `season_pass_progress`, `payment_order`, `spend_limit`, `gift_log` | monetization *(module mới)* | GD-19 §10 | ACCOUNT (`character_appearance`: CHARACTER) | `payment_order` không xóa (kiểm toán) |

- <a id="db-r-030"></a>**DB-R-030** Bổ sung module `beast` (GD-25) và `pvp` (GD-32) vào danh sách module của TD-01 §5.1.

---

## 6. DDL của bảng lõi (không thuộc GD nào hoặc chưa có DDL)

```sql
-- ===== account =====
CREATE TABLE account (
  id UUID PRIMARY KEY,
  login_email VARCHAR(254) NOT NULL UNIQUE,           -- chuẩn hóa chữ thường
  password_hash VARCHAR(255) NOT NULL,                -- Argon2id
  display_handle VARCHAR(24) UNIQUE,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','SUSPENDED','BANNED','DELETED')),
  suspended_until TIMESTAMPTZ,                       -- SUSPENDED / BANNED deadline; NULL means indefinite
  locale VARCHAR(8) NOT NULL DEFAULT 'vi',
  settings JSONB NOT NULL DEFAULT '{}',               -- hideNameInBroadcast (GD-14 SO-R-061), ControlSettings (GD-24), hintsSeen (GD-18 OB-R-011)
  roles JSONB NOT NULL DEFAULT '[]',                  -- GM, ADMIN
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), last_login_at TIMESTAMPTZ, deleted_at TIMESTAMPTZ
);
CREATE TABLE account_session (
  id UUID PRIMARY KEY, account_id UUID NOT NULL REFERENCES account(id),
  refresh_token_hash BYTEA NOT NULL UNIQUE, device_hash BYTEA, device VARCHAR(128), ip INET,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), expires_at TIMESTAMPTZ NOT NULL, revoked_at TIMESTAMPTZ,
  replaced_by_session_id UUID REFERENCES account_session(id)
);
CREATE INDEX ix_account_session_account ON account_session(account_id) WHERE revoked_at IS NULL;

-- ===== server registry and per-server account progress =====
CREATE TABLE server_registry (
  server_id SMALLINT PRIMARY KEY,
  name_key VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL CHECK (status IN ('OPEN', 'MAINTENANCE', 'CLOSED'))
);
CREATE TABLE account_server_meta (
  account_id UUID NOT NULL REFERENCES account(id),
  server_id SMALLINT NOT NULL REFERENCES server_registry(server_id),
  generation_count INT NOT NULL DEFAULT 0 CHECK (generation_count >= 0),
  PRIMARY KEY (account_id, server_id)
);

-- ===== character (gộp cột từ các GD; cột nào thuộc GD nào ghi chú bên phải) =====
CREATE TABLE character (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES account(id),
  server_id SMALLINT NOT NULL,
  name VARCHAR(12) NOT NULL,
  generation INT NOT NULL,                                        -- GD-26
  base_appearance JSONB NOT NULL,                                  -- GD-27 preset + palette color IDs
  background_id VARCHAR(48) NOT NULL,                             -- GD-27
  -- vòng đời (GD-26)
  life_state VARCHAR(8) NOT NULL DEFAULT 'ALIVE' CHECK (life_state IN ('ALIVE','ENDING','ENDED')),
  start_age INT NOT NULL DEFAULT 16,
  lifespan_used_sec BIGINT NOT NULL DEFAULT 0,
  innate_lifespan_bonus_years INT NOT NULL DEFAULT 0,
  lifespan_extension_years NUMERIC(8,2) NOT NULL DEFAULT 0,
  last_light_until TIMESTAMPTZ,
  end_cause VARCHAR(24), died_at TIMESTAMPTZ, ending_deadline TIMESTAMPTZ, ended_at TIMESTAMPTZ,
  -- thiên phú (GD-27)
  spirit_root JSONB NOT NULL, innate_stats JSONB NOT NULL, innate_stats_creation JSONB NOT NULL,  -- karma nằm trong innate_stats
  modifiers_version INT NOT NULL DEFAULT 1,
  -- tu luyện (GD-04)
  realm VARCHAR(4) NOT NULL DEFAULT 'PN', realm_stage INT NOT NULL DEFAULT 1, cultivation_exp BIGINT NOT NULL DEFAULT 0,
  pill_toxicity NUMERIC(6,2) NOT NULL DEFAULT 0, toxicity_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  heart_state INT NOT NULL DEFAULT 50,
  breakthrough_failures JSONB NOT NULL DEFAULT '{}', last_breakthrough_fail_at TIMESTAMPTZ,
  -- kỹ năng (GD-07)
  main_technique_id VARCHAR(48), main_technique_changed_at TIMESTAMPTZ,
  -- ảnh chụp thế giới do Game Server gửi (TD-01 TA-R-110)
  map_id VARCHAR(24), channel SMALLINT, pos_x NUMERIC(7,2), pos_z NUMERIC(7,2), facing SMALLINT,
  hp INT, mp INT, snapshot_seq BIGINT NOT NULL DEFAULT 0, snapshot_at TIMESTAMPTZ,
  -- cache (DB-R-004)
  combat_power INT,                                               -- cache, GD-14 SO-R-04I
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), last_login_at TIMESTAMPTZ,
  version INT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX ux_character_alive_per_account ON character(account_id, server_id) WHERE life_state IN ('ALIVE','ENDING');   -- D-12, LC-R-006
CREATE UNIQUE INDEX ux_character_name_alive ON character(server_id, lower(name)) WHERE life_state <> 'ENDED';
CREATE INDEX ix_character_cp ON character(server_id, combat_power DESC) WHERE life_state = 'ALIVE';

-- ===== platform (TD-01) =====
CREATE TABLE idempotency_record (
  key VARCHAR(80) PRIMARY KEY, endpoint VARCHAR(96) NOT NULL, request_hash BYTEA NOT NULL,
  status VARCHAR(12) NOT NULL CHECK (status IN ('IN_PROGRESS','DONE')),
  http_status SMALLINT, response JSONB, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE outbox_event (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, event_id UUID NOT NULL UNIQUE,
  type VARCHAR(64) NOT NULL, version SMALLINT NOT NULL, stream VARCHAR(32) NOT NULL,
  account_id UUID, character_id UUID, payload JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), published_at TIMESTAMPTZ
);
CREATE INDEX ix_outbox_unpublished ON outbox_event(id) WHERE published_at IS NULL;
CREATE TABLE consumed_event (consumer VARCHAR(48) NOT NULL, event_id UUID NOT NULL, consumed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (consumer, event_id));
CREATE TABLE death_log (
  id UUID PRIMARY KEY, character_id UUID NOT NULL, map_id VARCHAR(24) NOT NULL, cause VARCHAR(24) NOT NULL,
  payload BYTEA NOT NULL, size_bytes INT NOT NULL, needs_review BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE admin_audit (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, actor_account_id UUID NOT NULL, action VARCHAR(64) NOT NULL,
  target_type VARCHAR(32), target_id VARCHAR(80), before JSONB, after JSONB, reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE fortune_dispute (                                     -- GD-29 §10
  id UUID PRIMARY KEY, account_id UUID NOT NULL, character_id UUID NOT NULL, ref_id VARCHAR(80) NOT NULL,
  note TEXT, status VARCHAR(12) NOT NULL DEFAULT 'OPEN', gm_note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), resolved_at TIMESTAMPTZ
);

-- ===== world (GD-21) =====
CREATE TABLE map_discovery (
  account_id UUID NOT NULL, map_id VARCHAR(24) NOT NULL, fog_bitmap BYTEA NOT NULL,   -- ô 8m (GD-21 §4)
  discovered_pct NUMERIC(5,2) NOT NULL DEFAULT 0, updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, map_id)
);
CREATE TABLE discovered_poi (account_id UUID NOT NULL, poi_id VARCHAR(48) NOT NULL, discovered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, poi_id));
CREATE TABLE character_secret (character_id UUID NOT NULL, group_id VARCHAR(48) NOT NULL, position_index SMALLINT NOT NULL,
  found_at TIMESTAMPTZ, PRIMARY KEY (character_id, group_id));       -- vị trí rút mỗi kiếp; found_at = đã tìm
CREATE TABLE codex_entry (
  account_id UUID NOT NULL, entry_type VARCHAR(16) NOT NULL CHECK (entry_type IN ('MONSTER','HERB','PLACE','ITEM')),
  entry_id VARCHAR(48) NOT NULL, level SMALLINT NOT NULL DEFAULT 0, progress INT NOT NULL DEFAULT 0, unlocked_at TIMESTAMPTZ,
  PRIMARY KEY (account_id, entry_type, entry_id)
);

-- ===== social bổ sung (GD-14) =====
CREATE TABLE faction_reputation (
  character_id UUID PRIMARY KEY REFERENCES character(id),
  chinh INT NOT NULL DEFAULT 0, ta INT NOT NULL DEFAULT 0, ma INT NOT NULL DEFAULT 0,
  daily_day DATE, daily_chinh INT NOT NULL DEFAULT 0, daily_ta INT NOT NULL DEFAULT 0, daily_ma INT NOT NULL DEFAULT 0,
  best JSONB NOT NULL DEFAULT '{}',                                  -- bậc cao nhất mỗi phe (Bia Vạn Cổ)
  CHECK (chinh >= 0 AND ta >= 0 AND ma >= 0)
);
CREATE TABLE character_benefit (
  character_id UUID PRIMARY KEY REFERENCES character(id),
  source VARCHAR(12) NOT NULL CHECK (source IN ('NPC_SECT','PLAYER_SECT','WANDERER')), switched_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE player_sect_edicts (
  sect_id UUID PRIMARY KEY, edict_ids JSONB NOT NULL DEFAULT '[]', changed_at TIMESTAMPTZ, effective_from TIMESTAMPTZ
);

-- ===== pvp (GD-32) =====
CREATE TABLE pk_state (
  character_id UUID PRIMARY KEY REFERENCES character(id),
  mode VARCHAR(10) NOT NULL DEFAULT 'PEACE' CHECK (mode IN ('PEACE','GUILD','SLAUGHTER')), mode_changed_at TIMESTAMPTZ,
  infamy INT NOT NULL DEFAULT 0 CHECK (infamy >= 0), infamy_peak INT NOT NULL DEFAULT 0,
  grey_until TIMESTAMPTZ, last_pvp_combat_at TIMESTAMPTZ,
  heaven_punishment_field_sec INT,                                  -- null = không có Thiên phạt; còn lại = giây dã ngoại còn phải chịu
  feuds JSONB NOT NULL DEFAULT '[]', version INT NOT NULL DEFAULT 0
);
CREATE TABLE pvp_kill_log (
  id BIGINT GENERATED ALWAYS AS IDENTITY, killer_id UUID NOT NULL, victim_id UUID NOT NULL, map_id VARCHAR(24) NOT NULL,
  victim_mode VARCHAR(10) NOT NULL, victim_tier VARCHAR(8) NOT NULL, infamy_delta INT NOT NULL, dropped JSONB NOT NULL DEFAULT '[]',
  event_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (id, created_at), UNIQUE (event_id, created_at)
) PARTITION BY RANGE (created_at);
CREATE INDEX ix_pvp_kill_pair ON pvp_kill_log(killer_id, victim_id, created_at DESC);   -- PK-R-043 (giết lại trong 30 phút)
```

---

## 7. Phân vùng & hạn lưu

| Bảng | Phân vùng | Hạn lưu online | Sau đó |
|------|-----------|----------------|--------|
| `item_log`, `currency_ledger`, `lifespan_ledger`, `cultivation_ledger`, `beast_log`, `forge_action_log` | Tháng | 12 tháng | Xuất Parquet sang kho lạnh (S3-compatible), giữ 5 năm |
| `rng_audit`, `death_log` | Tháng (`death_log`: không phân vùng, xóa theo hạn) | 18 tháng | Xóa |
| `pvp_kill_log`, `severe_injury_log` | Tháng | 12 tháng | Xóa |
| `chat_message_log` | **Ngày** | 30 ngày | Xóa (trừ tin bị báo cáo: chép sang `player_report`) |
| `idempotency_record`, `outbox_event` (đã phát), `consumed_event` | — | 7 ngày | Job xóa hằng giờ (theo lô 5.000 dòng) |
| `age_accrual_report`, `meditation_report`, `mastery_batch` | — | 30–90 ngày | Xóa |
| `admin_audit` | Năm | 5 năm | — |

- <a id="db-r-040"></a>**DB-R-040** pg_partman tạo trước 3 phân vùng tương lai; job kiểm tra hằng ngày cảnh báo khi còn < 2 phân vùng tương lai. Migration khởi tạo `DEFAULT` partition cho `rng_audit` và `pvp_kill_log` để không chặn ghi trước khi bộ quản lý phân vùng hoạt động; trước khi tạo range partition chồng lên dữ liệu trong `DEFAULT`, phải chuyển dữ liệu đó ra trước.
- <a id="db-r-041"></a>**DB-R-041** Bảng phân vùng: khóa chính / unique phải chứa cột phân vùng (`created_at`). Idempotency của ledger phân vùng dựa vào `idempotency_record` (không phân vùng) thay vì unique toàn cục trên bảng phân vùng.

---

## 8. Sửa chữa so với PLAN §5.4 và các GD

| Chỗ cũ | Sửa thành | Lý do |
|--------|-----------|-------|
| PLAN `character.status [ALIVE|DEAD|ASCENDED]`, `age_years`, `lifespan_max`, `death_cause`, `stats JSONB`, `karma` | `life_state`, `lifespan_used_sec` (tuổi dẫn xuất), `end_cause`; chỉ số không lưu; karma trong `innate_stats` | GD-26, DB-R-004 |
| PLAN `character_skill` | `character_technique` (GD-07) | |
| PLAN `spirit_beast` | `beast` (GD-25, thuộc tài khoản) | |
| PLAN `sect`, `sect_member` | `player_sect`, `player_sect_member` (tài khoản) + `npc_sect_membership` (kiếp) | GD-14 SO-R-001 |
| PLAN `item_instance.owner_character_id` duy nhất | `owner_account_id NOT NULL` + `owner_character_id` (null với `VAULT`/`SEALED`/`LEGACY`) | GD-09 §10 |
| PLAN `rng_audit (… result JSONB)` | Theo TD-01 §9.2 (seed, inputs, outcome, source) | |
| **GD-14 `npc_sect_membership` khóa `character_id`** | Khóa `(character_id, sect_id)` + cột `state` (`ACTIVE`/`LEFT`) + `left_at`; `CREATE UNIQUE INDEX ux_npc_sect_active ON npc_sect_membership(character_id) WHERE state = 'ACTIVE'` | Phản môn rồi vào tông khác (SO-R-047) cần giữ lịch sử |
| GD-26 "cột `seal_band` (GD-09)" chưa có ở GD-09 | `item_instance.seal_band VARCHAR(4)` (null trừ khi `location = 'SEALED'`) | §4 |

- <a id="db-r-050"></a>**DB-R-050** PLAN §5.4 chỉ còn là tóm tắt và trỏ về TD-03; không viết DDL ở PLAN nữa.

---

## 9. Truy vấn nóng & index

| Truy vấn | Tần suất | Index / cách làm |
|----------|----------|------------------|
| Nạp nhân vật khi vào bản đồ (`session-load`) | Mỗi lần vào / chuyển bản đồ | PK `character`; `item_instance (owner_character_id, location)` cho `BAG`/`EQUIP`; `character_technique (character_id)`; một round-trip gom bằng nhiều câu trong 1 transaction đọc |
| Ghi snapshot (30s / nhân vật) | ~5.000 / phút ở 2.500 CCU | `UPDATE character SET … WHERE id = ? AND snapshot_seq < ?` (HOT update — các cột snapshot không có index) |
| Gom lô tu vi / thuần thục / tuổi | Mỗi 30–60s / nhân vật | Một transaction mỗi lô; ledger `INSERT` + `UPDATE character` |
| Kho tài khoản / Di Tàng | Khi mở UI | `item_instance (owner_account_id, location)` |
| Đấu giá: tìm theo mẫu / giá | Thường xuyên | `auction_listing (template_id, unit_price) WHERE state='ACTIVE'` (GD-13 §10) |
| Bảng xếp hạng chiến lực | 10 phút | Valkey sorted set (§10) + `ix_character_cp` dự phòng |
| Kiểm toán: lịch sử một vật phẩm | Thỉnh thoảng (GM) | `item_log (item_instance_id)` |

- <a id="db-r-060"></a>**DB-R-060** Mục tiêu: p95 mọi truy vấn API < 20ms ở 2.500 CCU; truy vấn > 100ms ghi log chậm (`pg_stat_statements`) và cảnh báo.

---

## 10. Đồng thời & giao dịch

- <a id="db-r-070"></a>**DB-R-070** Mức cô lập mặc định **READ COMMITTED**. Thao tác sinh tử / kết thúc kiếp / đột phá khóa hàng `character` (`SELECT … FOR UPDATE`, GD-26 bước 1).
- <a id="db-r-071"></a>**DB-R-071** **Thứ tự khóa cố định** để tránh deadlock: `account_meta` → `character` → `currency_wallet` (theo `id`) → `item_instance` (theo `id` tăng dần) → bảng khác của module. Mọi transaction đa bảng tuân theo thứ tự này.
- <a id="db-r-072"></a>**DB-R-072** Bảng có `version` dùng khóa lạc quan; xung đột → thử lại tối đa 3 lần rồi trả `409 INVALID_STATE` (TD-01 §6.3).
- <a id="db-r-073"></a>**DB-R-073** Transaction ngắn: không gọi mạng (Valkey, dịch vụ khác) bên trong transaction, trừ ghi `outbox_event` (cùng DB).

---

## 11. Migration

- <a id="db-r-080"></a>**DB-R-080** **Flyway**, file `V{yyyyMMddHHmm}__{module}_{mô_tả}.sql`; mỗi migration chỉ đụng bảng của **một** module; không sửa migration đã chạy ở môi trường dùng chung.
- <a id="db-r-081"></a>**DB-R-081** Đổi cấu trúc theo **expand → migrate → contract** (thêm cột mới, ghi cả hai, chuyển dữ liệu, bỏ cột cũ ở bản phát hành sau) để triển khai rolling không downtime (TD-01 §13).
- <a id="db-r-082"></a>**DB-R-082** Migration dữ liệu lớn (> 100.000 dòng) chạy bằng job theo lô ngoài Flyway, có tiến độ và có thể dừng.
- <a id="db-r-083"></a>**DB-R-083** Thứ tự migration Phase 1: `account` → `character` → `platform` → `inventory` → `lifecycle` → `talent` → `cultivation` → `technique` → các module còn lại.

---

## 12. Valkey (dữ liệu tạm, khôi phục được)

| Khóa | Kiểu | TTL | Nguồn |
|------|------|-----|-------|
| `ctx:{characterId}` | Hash / JSON | 10s | TD-01 §11 |
| `charlock:{characterId}` | String (roomId) | 30s, gia hạn | TD-01 TA-R-120 |
| `ticket:{jti}` | String | 60s | TD-01 §14 |
| `evt:{module}`, `evt:dlq` | Stream | MAXLEN | TD-01 §10 |
| `gs:retry:{processId}` | List | — | TD-01 TA-R-054 |
| `rank:cp:{server}` | Sorted set | — | GD-14 SO-R-04I |
| `party:{id}`, `party:invite:{id}` | Hash | theo GD-14 | GD-14 |
| `chat:{channel}:{scope}` | Pub/Sub | — | TD-02 §11 |
| `rl:{scope}:{id}` | Token bucket | ngắn | TD-02 NW-R-064 |
| `world:clock`, `world:weather:{vực}` | Hash | — | World Service (GD-21) |
| `wd:{mapId}:{channel}` | Hash | — | World Directory (TD-12) |

- <a id="db-r-090"></a>**DB-R-090** Mất toàn bộ Valkey không được làm mất dữ liệu có giá trị (TD-01 §13): mọi thứ trong bảng trên tái tạo được từ PostgreSQL / Game Server, hoặc chấp nhận mất (tổ đội, lời mời).

---

## 12b. Kho phân tích
Dữ liệu telemetry / KPI nằm ở **kho phân tích tách riêng** (PostHog self-host hoặc bảng sự kiện + Metabase), **không** thuộc DB nghiệp vụ; ETL chỉ đọc từ replica (GD-20).

## 13. Dữ liệu cá nhân
- <a id="db-r-100"></a>**DB-R-100** Dữ liệu cá nhân tối thiểu: email, mật khẩu băm, IP phiên. Tuân thủ quy định bảo vệ dữ liệu cá nhân của Việt Nam (Nghị định 13/2023/NĐ-CP): có quy trình yêu cầu xóa tài khoản → `account.status = DELETED`, ẩn danh email / handle, giữ ledger (nghĩa vụ kiểm toán, không chứa dữ liệu cá nhân trực tiếp), xóa `account_session`. Chi tiết pháp lý: TD-08 / tư vấn pháp lý.

---

## 14. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="db-ac-001"></a>DB-AC-001 | Migration tạo bảng không có trong §5 | CI | Fail (DB-R-011) |
| DB-AC-002 | Module `craft` dùng repository của `item_instance` | CI | Fail (ArchUnit) |
| DB-AC-003 | Tài khoản đã có nhân vật `ALIVE` | Tạo nhân vật thứ hai cùng server | Vi phạm `ux_character_alive_per_account` → `409` |
| DB-AC-004 | Nhân vật 200 vật phẩm (60 kho, 40 túi…) hết kiếp | Bước 9 | < 200ms; kho → `SEALED` có `seal_band`; túi → `DESTROYED`; `item_log` đủ dòng |
| DB-AC-005 | Snapshot `seq = 10` đến sau snapshot `seq = 12` | Ghi | Bỏ qua (không ghi đè) |
| DB-AC-006 | Hai transaction cùng sửa ví + vật phẩm theo thứ tự khác nhau | Chạy song song 1.000 lần | 0 deadlock (DB-R-071) |
| DB-AC-007 | Phân vùng tháng tới chưa tạo, còn 1 phân vùng tương lai | Job hằng ngày | Cảnh báo |
| DB-AC-008 | Phản môn Thanh Vân rồi vào Hợp Hoan | Kiểm tra `npc_sect_membership` | 2 dòng (`LEFT`, `ACTIVE`), unique `ACTIVE` giữ đúng 1 |
| DB-AC-009 | Xóa Valkey hoàn toàn | Hệ thống chạy tiếp | Không mất vật phẩm / tiền / tu vi; người chơi kết nối lại được |
| DB-AC-010 | Yêu cầu xóa tài khoản | Xử lý | Email / handle ẩn danh, phiên bị xóa, ledger còn nguyên |
| DB-AC-011 | Khôi phục PITR về 10 phút trước | Diễn tập | Thành công trong ≤ 1 giờ, dữ liệu nhất quán (job đối soát TA-R-062 sạch) |

---

## 15. Câu hỏi mở
- [ ] **DB-Q-1:** Tách schema PostgreSQL theo module ngay từ đầu (dễ tách service sau) hay một schema `game` (đơn giản)? Đề xuất: một schema ở MVP.
- [x] **DB-Q-2:** *(Đã chốt: DB chung cho nhiều server; `character.server_id` và generation theo server. Metadata Luân Hồi / unlocks / vault vẫn cấp account.)*
- [ ] **DB-Q-3:** Kho lạnh cho log quá hạn: S3-compatible của nhà cung cấp cloud nào (liên quan PLAN Q10)?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-23 | — | Bản nháp đầu: bất biến, quy ước, hạ tầng, phạm vi dữ liệu & **ma trận giữ qua kiếp**, sổ đăng ký ~120 bảng theo module, DDL bảng lõi (account, character gộp, platform, world, social bổ sung, pvp), phân vùng & hạn lưu, sửa chữa so với PLAN §5.4 / GD-14, truy vấn nóng, đồng thời, migration, Redis, dữ liệu cá nhân, nghiệm thu |
| v0.2 | 2026-10-25 | — | Sổ đăng ký thêm bảng module `security` (TD-08), `account_session.device_hash` |
| v0.3 | 2026-10-25 | — | D-24: Valkey thay Redis (tên khóa / lệnh không đổi) |
| v0.4 | 2026-10-25 | — | Bảng monetization; `account.settings` gồm ControlSettings; trỏ TD-09 §5 |
| v0.5 | 2026-10-25 | — | Bảng GD-23; ghi chú kho phân tích tách riêng (GD-20) |
| v0.6 | 2026-10-25 | — | `account.settings.hintsSeen` (GD-18) |
