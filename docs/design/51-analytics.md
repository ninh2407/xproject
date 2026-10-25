# GD-20 — Telemetry & KPI

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | GD-20 · Prefix ID: `TM` |
| Owner | Director + Server Dev + Game Designer |
| Trạng thái | 🟡 Bản nháp v0.2 |
| Cần trước | Phase 2 (khung thu thập + sự kiện vòng đời), Phase 4 / Closed Alpha (đủ KPI + dashboard) |
| Quyết định nền | PLAN Phase 4 (KPI riêng game một mạng), PLAN §5.7.2 (PostHog self-host / bảng sự kiện + Metabase), D-17, D-21, D-22 (đa nền tảng) |
| Liên quan | Mục "Telemetry" của **mọi GD** · [TD-01 §10](../tech/00-tdd.md) (sự kiện domain — khác telemetry) · [TD-03 §13](../tech/02-database.md) (dữ liệu cá nhân) · [TD-08](../tech/07-security.md) · [TD-09 §4](../tech/08-infra-ops.md) (metric vận hành) · [TD-11](../tech/10-performance.md) · [TD-15](../tech/14-oss-register.md) (PostHog / Metabase) · [GD-13](19-economy.md) · [GD-19](50-monetization.md) · [GD-26](03-lifecycle.md) · [GD-29](05-rng-fairness.md) |

> **Telemetry ≠ sự kiện domain ≠ metric vận hành.** Sự kiện domain (TD-01 §10, `inventory.item_granted`) là hợp đồng nghiệp vụ giữa các module, đảm bảo giao nhận. Metric vận hành (TD-09, `tl_gs_tick_duration_ms`) phục vụ giám sát hệ thống. **Telemetry** (tài liệu này) phục vụ **hiểu người chơi và cân bằng game**: được phép mất một phần, lấy mẫu, ẩn danh hóa, không bao giờ dùng để quyết định nghiệp vụ.

**Tài liệu này cung cấp cho các tài liệu khác:**

| Tài liệu gọi | Cần gì từ GD-20 | Mục |
|--------------|-----------------|-----|
| Mọi GD | Quy ước đặt tên, danh mục sự kiện chuẩn (thay bảng "Telemetry" rời rạc) | §3, §4 |
| Client (TD-05), Game Server (TD-06), API | Envelope, đường gửi, lấy mẫu, quyền riêng tư | §5, §6 |
| PLAN Phase 4 / Alpha | Định nghĩa KPI, dashboard, ngưỡng cảnh báo sản phẩm | §7, §8 |
| GD-19, GD-13, GD-29 | Công thức KPI kinh tế, doanh thu, công bằng RNG | §7 |

---

## 1. Mục tiêu

- Trả lời được các câu hỏi sống còn của game một mạng ở Alpha: **người chơi có chơi lại sau khi chết không? Cái chết có thấy công bằng không? Tiến trình có đúng nhịp thiết kế không?**
- **Một danh mục sự kiện duy nhất**, tên ổn định, có schema — AI và dev không tự đặt tên sự kiện mới.
- Tôn trọng quyền riêng tư: dữ liệu bút danh, tối thiểu, có hạn lưu.

**Bất biến:**

| Mã | Bất biến |
|----|----------|
| <a id="tm-r-001"></a>**TM-R-001** | Telemetry **không bao giờ** là nguồn quyết định nghiệp vụ (không cấp thưởng, không xử phạt tự động dựa trên telemetry; xử phạt đi qua TD-08 với dữ liệu ledger / server). |
| <a id="tm-r-002"></a>**TM-R-002** | **Danh mục đóng:** mọi sự kiện phải có trong §4 và có JSON Schema ở `shared/telemetry/{name}.v{n}.json`; collector bỏ sự kiện lạ (đếm vào `tm_dropped_unknown`). |
| <a id="tm-r-003"></a>**TM-R-003** | **Không PII** trong telemetry: không email, tên đăng nhập, IP, nội dung chat, tọa độ thiết bị thật. Định danh là **bút danh** (§6). |
| <a id="tm-r-004"></a>**TM-R-004** | Gửi telemetry **không chặn gameplay**: bất đồng bộ, theo lô, lỗi thì bỏ; mất dữ liệu chấp nhận được. |
| <a id="tm-r-005"></a>**TM-R-005** | Ưu tiên **suy ra từ dữ liệu đã có** (ledger, `rng_audit`, `life_record`, log sự kiện domain) bằng ETL thay vì phát thêm sự kiện trùng thông tin. |

---

## 2. Tổng quan

```
Client (SDK @tienlo/telemetry) ─┐  batch 10s / 50 sự kiện, gzip
Game Server (TelemetryEmitter) ─┼──► POST /v1/telemetry (client) · /internal/v1/telemetry (server)
API Service (TelemetryEmitter) ─┘            │  validate schema, bút danh hóa, lấy mẫu, gắn envelope
                                             ▼
                                    Collector (module nhỏ cạnh API)
                                             │
                     ┌───────────────────────┴───────────────────────┐
                     ▼                                               ▼
           PostHog self-host (sản phẩm: funnel,           Kho phân tích (ClickHouse / bảng
           retention, cohort, dashboard)                  telemetry_event) + Metabase (KPI, ETL)
                                             ▲
               ETL hằng đêm từ PostgreSQL: currency_ledger, item_log, rng_audit,
               life_record, lifespan_ledger, cultivation_ledger (TM-R-005)
```

---

## 3. Quy ước đặt tên

- <a id="tm-r-010"></a>**TM-R-010** Tên sự kiện `snake_case`, dạng **`{miền}_{đối_tượng}[_{hành_động}]`**, duy nhất toàn danh mục. Miền là tiền tố ngắn cố định:

| Tiền tố | Miền | GD |
|---------|------|----|
| `life_` | Vòng đời, kiếp | GD-26 |
| `creation_`, `talent_` | Tạo nhân vật, thiên phú | GD-27 |
| `cult_` | Tu luyện | GD-04 |
| `combat_`, `skill_` | Chiến đấu, kỹ năng | GD-06, GD-07 |
| `tech_` | Chế công pháp | GD-08 |
| `item_`, `currency_` | Vật phẩm, tiền tệ | GD-09, GD-13 |
| `alchemy_`, `forge_`, `prof_` | Luyện đan, luyện khí, nghề | GD-10, GD-11 |
| `monster_`, `boss_` | Quái | GD-12 |
| `social_`, `sect_` | Xã hội, tông môn | GD-14, GD-30 |
| `quest_` | Nhiệm vụ | GD-15 |
| `world_`, `map_` | Thế giới, bản đồ | GD-21, TD-12 |
| `enc_` | Cơ duyên | GD-22 |
| `ob_` | Onboarding | GD-18 |
| `input_`, `settings_` | Điều khiển | GD-24 |
| `beast_` | Linh thú | GD-25 |
| `dz_` | Hiểm địa | GD-28 |
| `rng_` | Ngẫu nhiên & công bằng | GD-29 |
| `cave_`, `site_` | Động phủ, sơn môn | GD-30 |
| `pvp_` | PvP | GD-32 |
| `shop_`, `pass_`, `pay_` | Monetization | GD-19 |
| `app_`, `perf_` | Ứng dụng, hiệu năng client | TD-05, TD-11 |

- <a id="tm-r-011"></a>**TM-R-011** Tên trường `camelCase`, dùng định danh Glossary (`realmCode`, `generation`, `endCause`…); thời lượng `…Sec` / `…Ms`; tỷ lệ là phân số (TD-07 DT-R-041); ID gamedata để nguyên (`ITM_*`), ID thực thể dùng bút danh.
- <a id="tm-r-012"></a>**TM-R-012** **Không đặt tên trùng sự kiện domain** (vd. telemetry không dùng `stage_up` khi đã có `cultivation.stage_up` — dùng `cult_stage_up`). Mỗi sự kiện có `v` (phiên bản schema); đổi trường không tương thích → tăng `v`.
- <a id="tm-r-013"></a>**TM-R-013** Phân loại mỗi dòng danh mục: **E** = sự kiện phát trực tiếp; **ETL** = suy ra hằng đêm từ dữ liệu nghiệp vụ; **D** = chỉ số dẫn xuất (tính trong kho phân tích, không phải sự kiện).

---

## 4. Danh mục sự kiện chuẩn

Cột: **Tên chuẩn** · **Loại** (E / ETL / D) · **Nguồn** (C = client, GS = Game Server, API, — = kho) · **Trường chính** · **Tần suất / lấy mẫu** · **GD** · **Đổi từ** (tên cũ trong GD, nếu đổi).

### 4.1. Ứng dụng & phiên
| Tên chuẩn | Loại | Nguồn | Trường chính | Tần suất | GD | Đổi từ |
|-----------|:---:|:---:|--------------|----------|----|--------|
| `app_session_start` / `app_session_end` | E | C | platform, deviceClass, qualityPreset, durationSec | mỗi phiên | GD-20 | *(mới)* |
| `app_screen_view` | E | C | screen (Boot / Login / CharacterSelect / MapLoading / InGame / Ending) | mỗi lần đổi màn | TD-05 | *(mới)* |
| `app_error` | E | C | code, screen, gamedataVersion | mỗi lỗi, gộp trùng 60s | TD-05 | *(mới)* |
| `perf_sample` | E | C | preset, fpsP50, fpsP5, frameMsP95, drawCalls, heapMb | mỗi 60s, **lấy mẫu 20% phiên** | TD-11 | *(mới)* |
| `perf_load` | E | C | phase (firstLoad / map), mapId, ms, bytes | mỗi lần tải | TD-11 | *(mới)* |

### 4.2. Vòng đời & tạo nhân vật (GD-26, GD-27)
| Tên chuẩn | Loại | Nguồn | Trường chính | Tần suất | GD | Đổi từ |
|-----------|:---:|:---:|--------------|----------|----|--------|
| `life_started` | E | API | generation, backgroundId, talentIds, **gapHoursSinceLastEnd** | mỗi kiếp | GD-26 | gộp `life_started` + `rebirth_started` |
| `life_lifespan_spent` | ETL | — | reasonCode, years, realmCode, ageYears | từ `lifespan_ledger` | GD-26 | `lifespan_spent` |
| `life_severe_injury` | E | GS | mapId, realmCode, cause, **monsterId?, skillId?, telegraphSec?, pingMs** | mỗi lần | GD-26, GD-12 | gộp `severe_injury` + `severe_injury_by_monster` |
| `life_warning_level` | E | API | level, ageYears, realmCode | mỗi lần đổi | GD-26 | `warning_level` |
| `life_last_light_breakthrough` | E | API | success | mỗi lần | GD-26 | `last_light_breakthrough` |
| `life_death_protected` | E | API | kind (NEWBIE / WARD) | mỗi lần | GD-26 | `death_protected` |
| `life_ended` | E | API | endCause, realmCode, ageYears, playHours, generation, **subCause** (dz phase / monster / trap / tribulation / pvp?) | mỗi kiếp | GD-26 | — |
| `life_death_survey` | E | C | score (1–5), endCause, skipped | sau chết thật, tùy chọn | GD-20 | *(mới, §7.2)* |
| `life_rebirth_unlock` | E | API | unlockId, cost | mỗi lần | GD-26 | `rebirth_unlock_purchased` |
| `life_remnant_trial` | E | GS | band, won, durationSec, attemptNo | mỗi lần | GD-26 §5.15 | *(mới)* |
| `creation_roll` / `creation_confirm` / `creation_abandon` | E | API / C | (như GD-27) | mỗi lần | GD-27 | — |
| `talent_change` / `talent_awakening_choice` / `talent_fusion_attempt` | E | API | (như GD-27) | mỗi lần | GD-27 | `awakening_choice`, `fusion_attempt` |
| `talent_vs_outcome` | D | — | talentIds × ageYears khi kết thúc, finalRealm | — | GD-27 | — |

### 4.3. Tu luyện, chiến đấu, kỹ năng (GD-04, GD-06, GD-07, GD-08)
| Tên chuẩn | Loại | Nguồn | Trường chính | Tần suất | GD | Đổi từ |
|-----------|:---:|:---:|--------------|----------|----|--------|
| `cult_exp_gain` | ETL | — | source, applied, wasted, realmCode (gộp giờ) | từ `cultivation_ledger` | GD-04 | `exp_gain` |
| `cult_stage_up` | E | API | realmCode, playHours, ageYears | mỗi lần | GD-04 | `stage_up` |
| `cult_pill_consumed` | E | API | itemId, quality, toxBefore, expGain | mỗi lần | GD-04 | `pill_consumed` |
| `cult_meditation_session` | E | GS | location (LEY_LINE / HUB / CAVE / SECT_GROUNDS), durationSec, expGain | mỗi phiên | GD-04, GD-30 | `meditation_session` |
| `cult_breakthrough_preview` / `cult_breakthrough_result` / `cult_tribulation_result` / `cult_heart_demon` | E | API | (như GD-04) | mỗi lần | GD-04 | bỏ tiền tố cũ |
| `combat_ttk` | E | GS | attackerRealm, targetId, rank, seconds, hits | **lấy mẫu 10%** | GD-06 | — |
| `combat_damage_taken_sample` | E | GS | realm, sourceRank, pctMaxHp | lấy mẫu 5% | GD-06 | `damage_taken_sample` |
| `combat_parry` / `combat_dodge_iframe` / `combat_poise_break` | E | GS | (như GD-06) | gộp 5 phút / nhân vật | GD-06 | `parry_result`, `dodge_iframe_hit`, `poise_break` |
| `combat_power_snapshot` | E | GS | realmCode, cp | mỗi giờ | GD-06 | `combat_power` |
| `skill_cast` | E | GS | skillRef, **device**, hitRate, avgTargets, damage (gộp 5 phút) | gộp | GD-07, GD-24 | gộp `skill_cast` + `skill_cast_hit_rate` |
| `skill_cast_rejected` | E | GS | reason | gộp | GD-07 | `cast_rejected` |
| `skill_loadout_snapshot` | E | GS | weapon, mainTechnique, slots, realm | mỗi giờ | GD-07 | `loadout_snapshot` |
| `skill_damage_share` | D | — | basic / skill / summon / dot / beast | — | GD-07, GD-25 | `damage_share` (+ gộp `beast_dps_share`) |
| `skill_mastery_up` | E | API | refId, level, playHours | mỗi lần | GD-07 | `mastery_up` (trùng tên sự kiện domain) |
| `skill_forbidden_art_used` | E | GS | skillRef, lifespanBefore, inDangerZone | mỗi lần | GD-07 | `forbidden_art_used` |
| `tech_crafted` / `tech_preview` / `tech_scroll_copy` / `tech_scroll_vault` | E | API | (như GD-08) | mỗi lần | GD-08 | `technique_*`, `scroll_*` |
| `tech_crafted_usage` / `tech_crafted_vs_authored_dps` | D | — | cskId, casts, damageShare / tier, dps | — | GD-08 | `crafted_skill_usage`, `crafted_vs_authored_dps` |

### 4.4. Vật phẩm, kinh tế, chế tác (GD-09, GD-10, GD-11, GD-13)
| Tên chuẩn | Loại | Nguồn | Trường chính | Tần suất | GD | Đổi từ |
|-----------|:---:|:---:|--------------|----------|----|--------|
| `item_granted` / `item_destroyed` | ETL | — | templateId, grade, reasonCode, realmCode | từ `item_log` | GD-09 | — |
| `item_equip_snapshot` | E | API | realmCode, gradeBySlot, setPieces, combatPower | mỗi giờ | GD-09 | `equip_snapshot` |
| `item_bag_full` | E | GS | context | mỗi lần | GD-09 | `bag_full_event` |
| `item_quick_slot_use` | E | GS | templateId, inCombat, inDangerZone | gộp | GD-09 | `quick_slot_use` |
| `currency_flow` | ETL | — | currency, reasonCode, band, sum (theo ngày) | từ `currency_ledger` | GD-09, GD-13 | — (GD-13 dùng lại, không định nghĩa riêng) |
| `alchemy_craft` | E | API | recipeId, mode, score, success, quality, pattern, batches, level | mỗi phiên | GD-10 | `craft_session` (mơ hồ) |
| `alchemy_overheat` | E | API | recipeId, tAtExplode | mỗi lần | GD-10 | `minigame_overheat` |
| `forge_craft` / `forge_rhythm` / `forge_enhance` / `forge_reroll` / `forge_upgrade` / `forge_dismantle` | E | API | (như GD-11) | mỗi lần | GD-11 | — |
| `prof_trial` | E | API | kind, tier, score, passed | mỗi lần | GD-10 §5.9 | *(mới)* |
| `item_pill_source` / `item_gear_source` / `alchemy_level_time` / `alchemy_breakthrough_pill_quality` | D | — | (như GD-10 / GD-11) | — | GD-10, GD-11 | `pill_source`, `gear_source`, `breakthrough_pill_quality` |

### 4.5. Quái, thế giới, cơ duyên, hiểm địa (GD-12, GD-21, GD-22, GD-28)
| Tên chuẩn | Loại | Nguồn | Trường chính | Tần suất | GD | Đổi từ |
|-----------|:---:|:---:|--------------|----------|----|--------|
| `monster_kill` | E | GS | monsterId, rank, ttkSec, hitsTaken, playerRealm | **lấy mẫu 10%** (tinh anh / boss: 100%) | GD-12 | — |
| `monster_loot_drop` | ETL | — | lootTableId, rarity, luck | từ `rng_audit` `MONSTER_LOOT` | GD-12 | `loot_drop` |
| `boss_attempt` | E | GS | monsterId, partySize, result, durationSec, phaseReached | mỗi lần | GD-12 | — |
| `monster_elite_contest` / `monster_reset` | E | GS | (như GD-12) | mỗi lần | GD-12 | `elite_contest` |
| `map_transfer` | E | GS / C | from, to, channel, via (seam / gate / teleport / instance), **ms** | mỗi lần | GD-21, TD-12 | gộp `map_enter` / `map_exit` / `map_transfer_time` |
| `map_position_sample` | E | GS | mapId, x, z (làm tròn 4m) | mỗi 10s, **lấy mẫu 10% nhân vật** | GD-21 | `position_sample` |
| `world_fog_milestone` / `world_discovery` / `world_codex_unlock` / `world_scan_used` / `world_teleport_used` / `world_stuck_escape` | E | GS / API | (như GD-21) | mỗi lần | GD-21 | bỏ tiền tố cũ |
| `world_gather` | E | GS | herbId, tier, phase, weather, **rare, contested (đã bị người khác chen / tranh)** | gộp (thường) / mỗi lần (hiếm) | GD-21 §3.10 | `gather` (+ trường tranh đoạt) |
| `map_channel_population` | E | GS | mapId, channel, players | mỗi phút | GD-21 | `channel_population` |
| `enc_opportunity` / `enc_offered` / `enc_engaged` / `enc_expired` / `enc_choice` / `enc_check` / `enc_resolved` / `enc_chain` | E | GS / API | (như GD-22) | mỗi lần (`enc_opportunity` lấy mẫu 10%) | GD-22 | — |
| `enc_per_hour` | D | — | — | — | GD-22 | — |
| `dz_run` / `dz_member_result` / `dz_downed` / `dz_death` / `dz_extract_choice` / `dz_scan` / `dz_corpse_loot` / `dz_dormant` | E | GS / API | (như GD-28) | mỗi lần | GD-28 | — |

### 4.6. Xã hội, nhiệm vụ, động phủ, linh thú, PvP (GD-14, GD-15, GD-25, GD-30, GD-32)
| Tên chuẩn | Loại | Nguồn | Trường chính | Tần suất | GD | Đổi từ |
|-----------|:---:|:---:|--------------|----------|----|--------|
| `social_party_session` | E | API | size, durationMin, activities | mỗi đội giải tán | GD-14 | gộp `party_formed` / `party_session` |
| `social_lfg_listing` / `social_friend_added` / `social_report_filed` | E | API | (như GD-14) | mỗi lần | GD-14 | bỏ tiền tố cũ |
| `social_chat_stats` | E | GS | channel, messages, filteredRate, blockedRate | gộp 10 phút / kênh (không nội dung) | GD-14 | `chat_message` |
| `sect_npc_rank` / `sect_benefit_switch` / `sect_reputation_tier` | E | API | rank, playHours / source / faction, tier | mỗi lần | GD-14 | `npc_sect_rank` (+ mới) |
| `quest_funnel` / `quest_completed` / `quest_abandoned` / `quest_sect_daily` | E | API | (như GD-15) | mỗi bước / lần | GD-15 | `sect_daily` |
| `quest_exp_share` | D | — | — | — | GD-15 | — |
| `cave_claimed` / `cave_relocated` / `cave_building_upgraded` / `cave_plot_harvest` / `cave_guardian_trial` | E | API / GS | (như GD-30) + band (thủ vệ) | mỗi lần | GD-30 | `plot_harvest` (+ mới) |
| `site_state` / `site_conclave_result` / `site_ruins_event` | E | API | (như GD-30) | mỗi lần | GD-30 | `conclave_result`, `ruins_event` |
| `beast_linh_chung` | E | GS | event (spawn / seen / captured), mapId, bloodline, timeToFindSec | mỗi lần | GD-25 | gộp `linh_chung_spawn` / `_seen` / `_captured` |
| `beast_capture_attempt` / `beast_breed` / `beast_kept_on_ending` | E | API | (như GD-25) | mỗi lần | GD-25 | `capture_attempt` |
| `pvp_kill` | E | API | killerTier, victimMode, victimTier, realmGap, infamyDelta, dropCount, mapId | mỗi lần | GD-32 | *(mới — GD-32 chưa có mục Telemetry)* |
| `pvp_mode_change` / `pvp_tier_change` / `pvp_enforcer_fight` / `pvp_heaven_avatar` | E | GS / API | mode / tier / result, partySize / result, durationSec | mỗi lần | GD-32 | *(mới)* |

### 4.7. Điều khiển, RNG, monetization (GD-24, GD-29, GD-19)
| Tên chuẩn | Loại | Nguồn | Trường chính | Tần suất | GD | Đổi từ |
|-----------|:---:|:---:|--------------|----------|----|--------|
| `input_device_session` / `input_rebind` / `settings_changed` | E | C | (như GD-24) | mỗi lần | GD-24 | `rebind` |
| `input_camera_zoom_avg` | E | C | distanceAvg | mỗi phiên | GD-24 | `camera_zoom_avg` |
| `rng_roll` | ETL | — | kind, class, shownPct, success | từ `rng_audit` | GD-29 | — |
| `rng_fortune_log_viewed` / `rng_dispute_created` / `rng_chance_changed_rejected` | E | C / API | (như GD-29) | mỗi lần | GD-29 | `fortune_log_viewed`, `fortune_dispute_created`, `chance_changed_rejected` |
| `shop_view` / `shop_preview` / `shop_purchase` | E | C / API | offerId, category, channel | mỗi lần | GD-19 | — |
| `pass_progress` | E | API | seasonId, level, premium, hoursPlayed | mỗi cấp | GD-19 | — |
| `pay_refund` / `pay_spend_limit_hit` | E | API | channel, reason / limitType | mỗi lần | GD-19 | `refund` (+ mới) |

- <a id="tm-r-020"></a>**TM-R-020** Bảng §4 là **nguồn duy nhất**. Mục "Telemetry" trong từng GD chỉ còn giữ phần "Câu hỏi trả lời" và trỏ về đây; khi thêm sự kiện mới, sửa §4 + schema trước (giống quy tắc danh sách đóng TD-10 CC-R-003).

---

### 4.8. Onboarding (GD-18)
| Tên chuẩn | Loại | Nguồn | Trường chính | Tần suất | GD | Đổi từ |
|-----------|:---:|:---:|--------------|----------|----|--------|
| `ob_client_ready` / `ob_character_created` / `ob_intro_done` / `ob_tutorial_qi_done` / `ob_first_encounter_done` / `ob_hub_reached` / `ob_sect_joined` | E | C / API | (như GD-18 §6: `skipped`, `ms`) | 1 lần mỗi tài khoản (funnel) | GD-18 | *(mới)* |
| `ob_hint_shown` / `ob_hint_dismissed` | E | C | hintId | mỗi lần, lấy mẫu 20% | GD-18 | *(mới)* |

## 5. Envelope & đường gửi

```ts
interface TelemetryEnvelope {
  name: string; v: number;                    // §4, phiên bản schema
  ts: string;                                  // ISO UTC (server ghi đè ts nhận nếu lệch > 5 phút)
  source: 'CLIENT' | 'GAME_SERVER' | 'API';
  serverId: number;
  pid: string;                                 // bút danh tài khoản = HMAC-SHA256(accountId, telemetryKey) cắt 16 byte, base32 (§6)
  cid?: string;                                // bút danh nhân vật (HMAC characterId)
  generation?: number; realmCode?: string;     // ngữ cảnh tiến trình thường dùng để cắt lát
  sessionId: string;                           // UUID mỗi phiên client
  platform: 'WEB' | 'PWA' | 'IOS' | 'ANDROID'; deviceClass?: 'LOW' | 'MID' | 'HIGH';
  clientVersion?: string; gamedataVersion: string;
  sampleRate: number;                          // 1 = không lấy mẫu; 0,1 = 10% (để nhân ngược khi tính)
  props: Record<string, unknown>;              // theo schema của `name`
}
```

- <a id="tm-r-030"></a>**TM-R-030** **Client** dùng SDK `@tienlo/telemetry` (trong `client/src/telemetry`, gọi qua lớp nền tảng TD-01 TA-R-162): gom lô 10s hoặc 50 sự kiện, gửi `POST /v1/telemetry` (gzip, ≤ 64KB / lô, rate limit theo TD-04); mất mạng → giữ tối đa 200 sự kiện trong bộ nhớ, quá thì bỏ cũ nhất; app xuống nền → gửi `sendBeacon` / flush.
- <a id="tm-r-031"></a>**TM-R-031** **Game Server / API** dùng `TelemetryEmitter` (không chặn, hàng đợi trong RAM ≤ 10.000, gửi lô 5s tới collector qua `POST /internal/v1/telemetry`); đầy hàng đợi → bỏ và tăng metric `tl_tm_dropped_total`.
- <a id="tm-r-032"></a>**TM-R-032** **Collector** (module `telemetry` cạnh API Service, không dùng chung transaction nghiệp vụ): validate schema (TM-R-002), **bút danh hóa** mọi ID còn lại, bỏ trường lạ, gắn envelope, áp lấy mẫu phía server nếu sự kiện đánh dấu, ghi vào PostHog (sản phẩm) và kho phân tích (§9).
- <a id="tm-r-033"></a>**TM-R-033** **Lấy mẫu** quyết định bằng hash bút danh (`hash(pid) mod 100 < rate×100`) để một người được lấy mẫu **nhất quán** cả phiên; trường `sampleRate` luôn đi kèm để kho tính lại tổng.

---

## 6. Quyền riêng tư & lưu giữ

- <a id="tm-r-040"></a>**TM-R-040** Bút danh `pid` / `cid` dùng khóa `telemetryKey` riêng (kho bí mật, TD-08), **khác** mọi khóa nghiệp vụ; bảng ánh xạ ngược chỉ tồn tại trong API Service (để xử lý yêu cầu xóa dữ liệu), không có trong kho phân tích.
- <a id="tm-r-041"></a>**TM-R-041** **IP** chỉ dùng tại collector để suy quốc gia / tỉnh (độ phân giải tỉnh), rồi bỏ; không lưu user-agent đầy đủ (chỉ `platform`, `deviceClass`, phiên bản trình duyệt lớn).
- <a id="tm-r-042"></a>**TM-R-042** **Đồng ý:** điều khoản khi đăng ký nêu rõ thu thập dữ liệu vận hành / cân bằng; cài đặt tài khoản có **tắt telemetry không thiết yếu** (nhóm `app_*`, `perf_*`, `input_*`, `shop_view` / `shop_preview` phía client). Sự kiện gameplay phía server cần cho vận hành và công bằng vẫn thu (dạng bút danh). Cơ sở pháp lý cụ thể theo Nghị định 13/2023/NĐ-CP **cần tư vấn pháp lý xác nhận** (TD-03 §13).
- <a id="tm-r-043"></a>**TM-R-043** **Xóa dữ liệu:** yêu cầu xóa tài khoản (TD-03 DB-R-100) → collector thêm `pid` vào danh sách loại trừ; job hằng tuần xóa sự kiện của `pid` đó trong PostHog / kho phân tích.
- <a id="tm-r-044"></a>**TM-R-044** **Hạn lưu:** sự kiện thô 13 tháng (đủ so sánh cùng kỳ năm); `map_position_sample` thô 90 ngày (chỉ giữ heatmap tổng hợp sau đó); bảng tổng hợp KPI giữ vô thời hạn (không còn bút danh cá nhân).
- <a id="tm-r-045"></a>**TM-R-045** Không có telemetry nào ghi nội dung chat, tên nhân vật / tông môn người chơi đặt, mô tả báo cáo.

---

## 7. KPI — định nghĩa

> Mọi KPI tính theo **giờ server UTC+7**, theo `serverId`, có thể cắt lát theo `platform`, `generation` (kiếp 1 vs kiếp ≥ 2), cohort tuần đăng ký. "Hoạt động" = có phiên ≥ 5 phút.

### 7.1. Tăng trưởng & giữ chân
| KPI | Công thức | Mục tiêu Alpha (giả định, hiệu chỉnh sau) |
|-----|-----------|------------------------------------------|
| DAU / WAU / MAU | Số `pid` hoạt động trong ngày / 7 ngày / 30 ngày | — |
| Retention Dn | Trong cohort `pid` có phiên đầu ngày d0: tỷ lệ có phiên hoạt động vào **đúng ngày** d0+n (n = 1, 7, 30) | D1 ≥ 40%, D7 ≥ 20%, D30 ≥ 10% |
| Độ dài phiên | Trung vị `durationSec` của `app_session_end` | 40–90 phút |
| Phiên / người / ngày | Số phiên hoạt động / DAU | — |

### 7.2. KPI riêng game một mạng (PLAN Phase 4)
| KPI | Công thức | Mục tiêu Alpha |
|-----|-----------|----------------|
| **Độ dài kiếp** | Phân vị (p25 / p50 / p75) của `playHours` trong `life_ended`, tách theo `endCause` và `generation`; kèm `ageYears` | Kiếp đầu 40–80 giờ (D-11) |
| **Phân bố nguyên nhân kết thúc kiếp** | % `life_ended` theo `endCause`; với chết thật: % theo `subCause` | Tọa hóa là nhóm lớn nhất; chết thật kiếp đầu ≤ 35% |
| **Tỷ lệ chơi lại 24h / 7 ngày** | Trong các `pid` có `life_ended` ở ngày d: tỷ lệ có `life_started` với `generation` +1 trong **24 giờ** / **7 ngày** sau `endedAt` | **≥ 50% / ≥ 70%** — chỉ số sống còn |
| Khoảng nghỉ trước kiếp mới | Trung vị `gapHoursSinceLastEnd` của `life_started` (generation ≥ 2) | ≤ 24 giờ |
| **Cảm nhận công bằng khi chết** | Trong `life_death_survey` không bỏ qua: % `score ≥ 4` ; kèm tỷ lệ phản hồi | ≥ 70% |
| Tỷ lệ cần xét lại cái chết | Số `death_log.needs_review` / số chết thật; số cái chết bị đảo ngược ở Death Review / số chết thật | Đảo ngược ≤ 1% |
| Chết thật trên 100 giờ hiểm địa | Số `dz_death` / tổng giờ trong hiểm địa × 100 | Theo GD-28 §6.5 |
| Chơi lại theo bạn bè | Tỷ lệ chơi lại 7 ngày, tách theo có / không có ≥ 1 bạn (GD-14) | Nhóm có bạn cao hơn |

### 7.3. Tiến trình & cân bằng
| KPI | Công thức | Mục tiêu |
|-----|-----------|----------|
| Thời gian lên cảnh giới | Trung vị `playHours` tại `cult_stage_up` theo `realmCode`, kiếp 1 vs kiếp ≥ 2 | Theo đường cong GD-04 / GD-26 (GD-03 Core Loop khi có) |
| Tu vi lãng phí ở bình cảnh | Σ `wasted` / Σ (`applied` + `wasted`) từ `cult_exp_gain` | ≤ 15% |
| Tỷ lệ đột phá | Thành công / tổng `cult_breakthrough_result`, theo `targetRealm`; so với `total` trung bình đã hiển thị | Thực tế ≈ kỳ vọng (GD-29) |
| TTK & độ khó | Trung vị `combat_ttk.seconds` theo rank × chênh cảnh giới; `life_severe_injury` / giờ chơi theo bản đồ | Theo GD-06 / GD-12 §8 |
| Cơ duyên / giờ | `enc_resolved` / giờ chơi | Theo GD-22 §8 |
| Phễu nhiệm vụ | Tỷ lệ rời ở từng bước `quest_funnel` | Không bước nào rời > 25% |
| Độ phủ bản đồ | % ô fog được ≥ 10% người chơi đi qua (từ `map_position_sample`) | Không vùng "chết" > 15% diện tích |

### 7.4. Kinh tế (GD-13)
| KPI | Công thức | Mục tiêu |
|-----|-----------|----------|
| Cung linh thạch | Σ ví nhân vật + kho Tàng Bảo Các + tông khố (ETL hằng ngày) | Tăng chậm, ổn định |
| Tỷ lệ hố / nguồn | Σ sink / Σ faucet theo ngày, từ `currency_flow` | 0,85–1,00 (GD-13); cảnh báo ngoài 0,7–1,2 trong 3 ngày |
| Chỉ số giá | Trung vị giá giao dịch của rổ 10 mặt hàng chuẩn (Trúc Cơ Đan, Ngưng Cơ Thảo, Diên Thọ Quả…) | Biến động tuần ≤ 20% |
| Hố hết kiếp | Giá trị đồ hủy / tiêu tán Di Tàng (`LIFE_ENDED_DESTROYED`, `SEAL_DISSIPATED`) / tổng sink | Theo dõi (EC-Q-6) |

### 7.5. Công bằng ngẫu nhiên (GD-29)
| KPI | Công thức | Mục tiêu |
|-----|-----------|----------|
| Lệch tỷ lệ | `z = (thực tế − Σp) / √Σp(1−p)` theo `kind` loại A, cửa sổ 24 giờ (GD-29 §6.4) | \|z\| ≤ 4 |
| Tỷ lệ khiếu nại | `rng_dispute_created` / 1.000 roll loại A | < 0,5 |

### 7.6. Monetization (GD-19)
| KPI | Công thức | Ghi chú |
|-----|-----------|---------|
| Doanh thu / ARPDAU | Doanh thu thuần (sau hoàn tiền, trước phí nền tảng) / DAU | Theo kênh web / iOS / Android |
| Tỷ lệ chuyển đổi | `pid` có ≥ 1 giao dịch trong tháng / MAU | — |
| ARPPU | Doanh thu thuần / số người trả tiền | — |
| Tỷ lệ mua Vấn Đạo Lệnh | Người mua nhánh trả phí / người hoạt động trong mùa | — |
| Tỷ lệ hoàn tiền | Giá trị `pay_refund` / doanh thu gộp | ≤ 2% |
| **Kiểm tra không P2W** | Tương quan giữa chi tiêu và tỷ lệ chết thật / tốc độ lên cảnh giới | Phải ≈ 0 (vi phạm → xem lại D-17) |

### 7.7. Hiệu năng & ổn định (TD-11)
| KPI | Công thức | Mục tiêu |
|-----|-----------|----------|
| FPS | p50 / p5 của `perf_sample` theo `preset` × `deviceClass` | Theo TD-11 |
| Thời gian tải | p95 `perf_load` (lần đầu, bản đồ); p95 `map_transfer.ms` | Theo TD-11 / TD-12 (< 2s) |
| Tỷ lệ lỗi client | Phiên có `app_error` nghiêm trọng / tổng phiên | ≤ 2% |

---

## 8. Dashboard & cảnh báo sản phẩm

- <a id="tm-r-050"></a>**TM-R-050** **Dashboard bắt buộc trước Closed Alpha** (PostHog / Metabase): (1) Tổng quan (DAU, retention, phiên); (2) **Một mạng** (§7.2); (3) Tiến trình & cân bằng (§7.3); (4) Kinh tế (§7.4); (5) Chiến đấu & độ khó (TTK, trọng thương theo quái / chiêu, boss); (6) Nội dung (cơ duyên, phễu nhiệm vụ, heatmap); (7) Công bằng RNG (§7.5); (8) Hiệu năng client (§7.7). Beta thêm: (9) Monetization (§7.6), (10) PvP (giết người vô tội / giờ, phân bố sát nghiệp, Chấp Pháp).
- <a id="tm-r-051"></a>**TM-R-051** **Cảnh báo sản phẩm** (gửi kênh team, không đánh thức on-call — on-call là của TD-09):

| Cảnh báo | Điều kiện |
|----------|-----------|
| Chơi lại sau chết giảm | Tỷ lệ chơi lại 24h giảm > 15 điểm % so với trung bình 7 ngày trước |
| Một chiêu / quái gây trọng thương bất thường | `life_severe_injury` theo `skillId` > 3× trung bình 7 ngày (khả năng lỗi cảnh báo / cân bằng; phối hợp cảnh báo "chết hàng loạt" TD-09) |
| Kinh tế lệch | Tỷ lệ hố / nguồn ngoài 0,7–1,2 ba ngày liền |
| RNG lệch | \|z\| > 4 (GD-29) |
| Phễu gãy | Một bước nhiệm vụ chính có tỷ lệ rời > 40% trong 3 ngày |
| Giết người vô tội tăng vọt (Beta) | `pvp_kill` với nạn nhân `PEACE` + `CLEAN` / giờ > 2× tuần trước ở một bản đồ |

---

## 9. Lưu trữ

```sql
-- Kho phân tích (ClickHouse khuyến nghị; Alpha có thể dùng PostgreSQL riêng, KHÔNG dùng DB nghiệp vụ)
CREATE TABLE telemetry_event (
  ts DateTime64(3), name LowCardinality(String), v UInt8, source LowCardinality(String),
  server_id UInt16, pid FixedString(26), cid Nullable(FixedString(26)), generation Nullable(UInt16),
  realm_code LowCardinality(Nullable(String)), session_id UUID, platform LowCardinality(String),
  device_class LowCardinality(Nullable(String)), client_version LowCardinality(Nullable(String)),
  gamedata_version LowCardinality(String), sample_rate Float32, props String  -- JSON
) ENGINE = MergeTree PARTITION BY toYYYYMM(ts) ORDER BY (name, server_id, ts) TTL ts + INTERVAL 13 MONTH;
-- Bảng tổng hợp KPI ngày: kpi_daily (day, server_id, kpi, dims, value) — không chứa pid.
```
- <a id="tm-r-060"></a>**TM-R-060** Kho phân tích **tách hẳn** khỏi PostgreSQL nghiệp vụ (DB-R-001 vẫn đúng: chỉ API ghi DB nghiệp vụ). ETL đọc từ **replica** PostgreSQL (TD-03 §3), không đọc primary.
- PostHog self-host: chỉ dùng phần lõi MIT (TD-15 — thư mục `ee` có license riêng, **không** bật tính năng `ee`).

---

## 10. A/B test

- <a id="tm-r-070"></a>**TM-R-070** **Được phép A/B:** UI / UX, onboarding, câu chữ hướng dẫn, bố cục cửa hàng (không đổi giá), thứ tự gợi ý nhiệm vụ, mức chất lượng mặc định.
- <a id="tm-r-071"></a>**TM-R-071** **Cấm A/B** — vì game một mạng phải công bằng tuyệt đối giữa người chơi: mọi thứ ảnh hưởng **rủi ro chết, thọ nguyên, tỷ lệ / RNG, phần thưởng, giá trị kinh tế, sức mạnh**, và **giá tiền thật** (không phân biệt giá theo nhóm). Cân bằng thay đổi cho mọi người cùng lúc qua bản phát hành (TD-07 §8.1).
- <a id="tm-r-072"></a>**TM-R-072** Gán nhóm bằng `hash(pid, experimentId)` (ổn định), qua hệ thống cờ tính năng (TD-09 §6); mỗi thí nghiệm có giả thuyết, KPI chính, cỡ mẫu tối thiểu và thời hạn ghi trước khi chạy; điều khoản sử dụng nêu có thử nghiệm giao diện.

---

## 11. Tiêu chí nghiệm thu

| Mã | Given | When | Then |
|----|-------|------|------|
| <a id="tm-ac-001"></a>TM-AC-001 | Client gửi sự kiện `foo_bar` không có trong §4 | Collector | Bỏ, tăng `tm_dropped_unknown` |
| TM-AC-002 | Sự kiện client có trường `email` | Collector | Trường bị bỏ trước khi lưu (TM-R-003) |
| TM-AC-003 | Cùng tài khoản, hai phiên | Kiểm tra `pid` | Giống nhau; không suy ngược được `accountId` từ kho phân tích |
| TM-AC-004 | `map_position_sample` lấy mẫu 10% | Một người được chọn | Được lấy mẫu suốt phiên (nhất quán), `sampleRate = 0.1` |
| TM-AC-005 | Collector ngừng 10 phút | Gameplay | Không ảnh hưởng; sự kiện server bị bỏ có đếm metric |
| TM-AC-006 | Người chơi tắt telemetry không thiết yếu | Phiên tiếp theo | Không có `app_*`, `perf_*`, `input_*` từ client; sự kiện gameplay server vẫn có |
| TM-AC-007 | 100 người kết thúc kiếp ngày d, 55 tạo kiếp mới trong 24h | KPI | Tỷ lệ chơi lại 24h = 55% |
| TM-AC-008 | Cohort 200 người ngày d0, 80 có phiên ngày d0+1 | KPI | D1 = 40% |
| TM-AC-009 | Yêu cầu xóa tài khoản | Job tuần | Không còn sự kiện của `pid` đó trong kho |
| TM-AC-010 | Đề xuất A/B đổi tỷ lệ rơi đồ | Duyệt thí nghiệm | Từ chối (TM-R-071) |
| TM-AC-011 | ETL đọc dữ liệu | Kiểm tra kết nối | Từ replica, không từ primary |
| TM-AC-012 | Sự kiện `skill_cast` gửi từ thiết bị cảm ứng | Kho | Có trường `device = TOUCH`, gộp chung với PC (không còn `skill_cast_hit_rate` riêng) |

---

## 12. Phạm vi

| Hạng mục | Phase 2 | Alpha | Beta | Sau launch |
|----------|:---:|:---:|:---:|:---:|
| SDK client + emitter server + collector + schema | ✅ | | | |
| Sự kiện vòng đời, tu luyện, chiến đấu, hiểm địa, cơ duyên | ✅ | | | |
| ETL từ ledger / `rng_audit`, dashboard 1–8, khảo sát công bằng | | ✅ | | |
| Dashboard monetization, PvP, cảnh báo sản phẩm đầy đủ | | | ✅ | |
| A/B test UI / onboarding | | | ✅ | |
| Heatmap tương tác trong editor bản đồ (TD-13) | | | | ✅ |

---

## 13. Câu hỏi mở
- [ ] **TM-Q-1:** PostHog self-host (vận hành thêm ClickHouse + Kafka) hay tự dựng ClickHouse + Metabase gọn hơn cho team nhỏ? Đề xuất: Alpha dùng PostgreSQL riêng + Metabase, chuyển ClickHouse / PostHog khi > 1.000 CCU.
- [ ] **TM-Q-2:** Khảo sát "cái chết có công bằng" hiện ngay màn tổng kết kiếp hay lần đăng nhập sau (tránh làm phiền lúc cảm xúc mạnh)?
- [ ] **TM-Q-3:** Có chia sẻ một phần KPI công khai cho cộng đồng (vd. "tuổi thọ trung bình kiếp trên server", "nguyên nhân chết phổ biến") làm nội dung xã hội không?

---

## Lịch sử thay đổi

| Phiên bản | Ngày | Người sửa | Nội dung |
|-----------|------|-----------|----------|
| v0.1 | 2026-10-25 | — | Bản nháp đầu: phân biệt telemetry / sự kiện domain / metric, quy ước tên theo miền, **danh mục chuẩn ~150 sự kiện gom từ mọi GD** (đánh dấu E / ETL / D, gộp trùng, đổi tên vênh, bổ sung sự kiện thiếu cho PvP, Di Tàng, khảo hạch nghề, thủ vệ động phủ), envelope + đường gửi + lấy mẫu nhất quán, quyền riêng tư (bút danh HMAC, opt-out, xóa dữ liệu, hạn lưu), định nghĩa KPI (giữ chân, **một mạng**, tiến trình, kinh tế, RNG, monetization, hiệu năng), dashboard + cảnh báo sản phẩm, chính sách A/B, lưu trữ, nghiệm thu |
| v0.2 | 2026-10-25 | — | Nhóm sự kiện `ob_*` (GD-18) §4.8 |

