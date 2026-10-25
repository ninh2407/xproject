# LR-05 — Thuật ngữ & tên định danh trong code

| Thuộc tính | Giá trị |
|------------|---------|
| Mã tài liệu | LR-05 |
| Trạng thái | 🟡 v0.1 |
| Tên game / thế giới (tạm) | **Tiên Lộ** · thế giới **Huyền Hoang Giới**, đại lục **Huyền Hoang Đại Lục** (PLAN Q8) |
| Quy tắc | Code, gamedata, DB, API **chỉ dùng tên định danh ở cột "Định danh"**. Văn bản hiển thị cho người chơi đi qua i18n key, không viết cứng tiếng Việt trong code |

> Quy ước đặt tên: `camelCase` cho trường dữ liệu/biến, `PascalCase` cho kiểu, `UPPER_SNAKE` cho giá trị enum, `snake_case` cho bảng/cột DB. Không dùng tiếng Việt không dấu làm tên biến (vd. **không** `thoNguyen`), trừ tên riêng trong ID nội dung (vd. `ENC_V1_NGU_ONG`).

## 1. Nhân vật & vòng đời (GD-26)

| Thuật ngữ | Định danh | Kiểu | Ý nghĩa |
|-----------|-----------|------|---------|
| Kiếp / đời nhân vật | `Life`, `character.generation` | int ≥ 1 | Thứ tự kiếp của tài khoản trên server |
| Tuổi | `ageYears` | number (dẫn xuất) | `startAge + lifespanUsedSec / secPerYear` |
| Thời gian sống đã dùng | `lifespanUsedSec` | int64 | Giây "tuổi" đã tiêu (sau hệ số) |
| Thọ nguyên tối đa | `lifespanMaxYears` | number (dẫn xuất) | Xem GD-26 §5.2 |
| Thọ nguyên còn lại | `lifespanRemainingYears` | number (dẫn xuất) | `lifespanMaxYears − ageYears` |
| Trạng thái sống | `LifeState` | enum | `ALIVE`, `ENDING`, `ENDED` |
| Nguyên nhân kết thúc kiếp | `EndCause` | enum | `LIFESPAN_EXHAUSTED`, `SLAIN_DANGER_ZONE`, `TRIBULATION_FAILED`, `SELF_DISSOLUTION`, `ASCENDED` |
| Binh giải | `EndCause.SELF_DISSOLUTION` | | Tự kết thúc kiếp trong thiên kiếp lethal (GD-26 §5.16) |
| Tọa hóa | `EndCause.LIFESPAN_EXHAUSTED` | | Hết thọ |
| Thân tử đạo tiêu | `EndCause.SLAIN_DANGER_ZONE`, `EndCause.TRIBULATION_FAILED` | | Chết thật |
| Phi thăng | `EndCause.ASCENDED` | | End-game, sau launch |
| Trọng thương | `SevereInjury`, status `SEVERE_INJURY` | | "Chết" không thật ngoài hiểm địa |
| Suy yếu | status `ST_WEAKENED` (bí danh cũ `WEAKENED`) | | Debuff sau trọng thương |
| Đại hạn | status `ST_GREAT_LIMIT` (bí danh `GREAT_LIMIT`) | | Thọ còn < ngưỡng |
| Hồi quang phản chiếu | status `ST_LAST_LIGHT` (bí danh `LAST_LIGHT`) | | Khoảng thời gian cuối trước tọa hóa |
| Bảo hộ tân thủ | `newbieProtection` | bool (dẫn xuất) | D-13 |
| Thế Thân Phù | item tag `DEATH_WARD` | | Đỡ 1 lần chết thật |
| Bế quan | `Seclusion` | | Đổi thọ nguyên lấy tu vi |
| Luân hồi | `Rebirth` | | Tiến trình cấp tài khoản |
| Điểm Luân Hồi | `rebirthPoints` | int | |
| Tàng Bảo Các | `Vault`, `ItemLocation.VAULT` | | Kho tài khoản, giữ qua kiếp |
| Di sản / Động phủ truyền thừa | `LegacyCave` | | Beta |
| Bia Vạn Cổ | `LifeRecord` | | Tiểu sử mỗi kiếp |
| Ký ức tiền kiếp | unlock `PAST_LIFE_MEMORY` | | |
| Di hài | `Corpse` | | Đồ rơi khi chết trong hiểm địa (GD-28) |

## 2. Tu luyện (GD-04)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Cảnh giới | `Realm` enum (theo thứ tự): `PHAM_NHAN`, `LUYEN_KHI`, `TRUC_CO`, `KIM_DAN`, `NGUYEN_ANH`, `HOA_THAN` | `PHAM_NHAN` chỉ có tầng 1 (giai đoạn hướng dẫn đầu game) |
| Tầng | `realmStage` int | Phàm nhân 1; Luyện Khí 1–9; các cảnh giới khác 1–4 (1 sơ, 2 trung, 3 hậu, 4 viên mãn) |
| Mã cảnh giới | `RealmCode` string `{PN\|LK\|TC\|KD\|NA\|HT}{stage}` | `PN1`, `LK5`, `TC1`. So sánh theo thứ tự `Realm` rồi `realmStage` |
| Thứ tự cảnh giới | `realmOrder` int | PN 0 · LK 1 · TC 2 · KD 3 · NA 4 · HT 5 (biến Ink `realm_order`) |
| Tu vi | `cultivationExp` | Tu vi trong tầng hiện tại |
| Yêu cầu tu vi tầng | `requirementExp` (= `stageRequirementExp(realmCode)`) | GD-04 §6.2 |
| Chỉ số tầng toàn cục | `stageIndex` int | PN1 = 0 … LK9 = 9, TC1 = 10 … (GD-04 §6.2) |
| Tốc độ tu luyện | `cultivationSpeed` | GD-04 §6.1 |
| Nguồn tu vi | `ExpSource`: `MONSTER`, `MEDITATION`, `SECLUSION`, `PILL`, `TREASURE`, `ENCOUNTER`, `QUEST`, `BREAKTHROUGH_FAILED`, `SEVERE_INJURY`, `GM_ADJUST` | |
| Phẩm chất đan | `PillQuality`: `HA`, `TRUNG`, `THUONG`, `CUC` | Hạ / Trung / Thượng / Cực phẩm |
| Thiên tài địa bảo | item category `TREASURE` | |
| Vật phẩm phụ trợ đột phá | item category `BREAKTHROUGH_AID`, trường `breakthroughBonus` | |
| Hộ pháp | `guardian` | Thành viên tổ đội canh giữ khi đột phá |
| Lần đột phá | `BreakthroughAttempt`, `BreakthroughState` | GD-04 §4 |
| Thử thách tâm ma | `EncounterType.HEART_TRIAL` | GD-04 CU-R-070, GD-22 §5.12 |
| Địa điểm đả tọa | `MeditationLocation`: `LEY_LINE`, `HUB`, `OTHER` | |
| Bình cảnh | `bottleneck` | |
| Đột phá | `Breakthrough` | |
| Thiên kiếp | `Tribulation` | |
| Tâm cảnh | `heartState` int 0–100 | |
| Tâm ma | status `ST_HEART_DEMON` (bí danh `HEART_DEMON`) | |
| Đan độc | `pillToxicity` | |
| Linh mạch | POI kind `LEY_LINE` | |
| Đả tọa | `Meditation` | |

## 3. Thiên phú & thuộc tính bẩm sinh (GD-27)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Xuất thân | `Background`, `backgroundId` | ID `BG_*` |
| Linh căn | `SpiritRoot` | `{ elements: Element[], purity: number, mutated: boolean }` |
| Phẩm linh căn | `RootGrade`: `HEAVENLY`, `DUAL`, `TRIPLE`, `QUAD`, `PENTA` | Thiên / Song / Tam / Tứ / Ngũ linh căn, dẫn xuất từ số hệ |
| Hệ số tu luyện theo linh căn | `rootMultiplier` | GD-27 §6.2 |
| Chỉ số bẩm sinh | `InnateStatKey`: `constitution`, `comprehension`, `luck`, `spiritSense` | |
| Cấp thần thức | `senseLevel` | GD-27 TLN-R-055 |
| Ngũ hành / nguyên tố | `Element` enum: `KIM`, `MOC`, `THUY`, `HOA`, `THO`, `LOI`, `BANG`, `PHONG` | Tên riêng giữ phiên âm |
| Căn cốt | `constitution` | |
| Ngộ tính | `comprehension` | |
| Khí vận | `luck` int 0–100 (chuẩn 50) | |
| Thần thức | `spiritSense` | |
| Thiên phú | `Talent`, `talentId` | ID `TAL_*`, vd. `TAL_LONGEVITY_BODY` (Trường Sinh Thể), `TAL_SHORT_LIVED_GENIUS` (Đoản Mệnh Kỳ Tài), `TAL_PAST_WISDOM` (Túc Tuệ) |
| Độ hiếm thiên phú | `TalentRarity`: `COMMON`, `RARE`, `EPIC`, `LEGENDARY`, `MYTHIC` | |
| Cực tính thiên phú | `TalentPolarity`: `POSITIVE`, `DUAL`, `NEGATIVE` | `NEGATIVE` không xuất hiện lúc tạo nhân vật |
| Nguồn thiên phú | `TalentSource`: `CREATION`, `AWAKEN`, `EVENT`, `FUSION` | |
| Thức tỉnh | `Awakening` | Chọn 1/3 thiên phú khi đột phá lớn |
| Dung hợp | `Fusion` | Dị hỏa, huyết mạch yêu thú |
| Roll lại / khóa dòng | `CreationAttempt`, `CreationRollResult`, `CreationLine`: `ROOT`, `INNATE`, `TALENT_1..3` | |
| Mã xuất thân | `BG_VILLAGE_YOUTH`, `BG_HERB_BOY`, `BG_SMITH_APPRENTICE`, `BG_NOBLE`, `BG_ORPHAN`, `BG_SECT_SERVANT`, `BG_OUTCAST` | GD-27 §5.2 |
| Chỉ số chiến đấu | `CombatStat` | Danh sách chuẩn ở GD-06 §3 |

## 4. Thế giới (GD-21)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Vực | `Region`, ID `V{n}` | |
| Bản đồ | `MapDef`, ID `V{n}_{HUB\|F\|HD\|BC\|KN\|DP}{nn}` | |
| Loại bản đồ | `MapType` (tên khái niệm): `HUB`, `FIELD`, `DANGER_ZONE`, `DUNGEON`, `ENCOUNTER_INSTANCE`, `CAVE`, `SECT_GROUNDS` | **Giá trị trong dữ liệu** `MapDef.type` (TD-13 §3): `HUB` / `F` / `HD` / `BC` / `KN` / `DP` / `TD` / `TRIAL` — dùng mã ngắn này trong code |
| Khu | `Area`, ID `{map}_A{nn}` | |
| Điểm (POI) | `Poi`, `PoiKind` (danh sách đóng TD-13 §4) | `TELEPORT_ARRAY`, `LEY_LINE`, `MEDITATION_SPOT`, `HERB`, `ORE`, `NPC`, `ALCHEMY_STATION` (Đan Phòng, GD-10), `FORGE_STATION` (GD-11), `INSIGHT_STATION`, `REMNANT_ALTAR`, `CAVE_GATE`, `SECT_SITE`, `STELE`, `CHEST`, `SECRET`, `TRAP`, `EXIT`, `SECT_HQ`, `ENCOUNTER_POINT`, `LANDMARK` |
| Hiểm địa | `DangerZone`, `MapType.DANGER_ZONE` | GD-28 |
| Túi hiểm địa | `ItemLocation.DANGER_BAG` | Đồ chưa mang ra |
| Bí cảnh | `MapType.DUNGEON` | |
| Kênh | `channel` | |
| Truyền tống trận | `PoiKind.TELEPORT_ARRAY` | TD-13 |
| Fog of war | `fogBitmap` | |
| Vạn Giới Lục | `Codex`, `CodexEntry` | |
| Thần thức quét | `SpiritScan` | |
| Ẩn cấp | `hiddenLevel` int 0–5 | |
| Canh giờ | `ShiChen` enum: `TY`, `SUU`, `DAN`, `MAO`, `THIN`, `TI`, `NGO`, `MUI`, `THAN`, `DAU`, `TUAT`, `HOI` | Tý … Hợi |
| Pha ngày | `DayPhase`: `DAWN`, `DAY`, `DUSK`, `NIGHT` | |
| Thời tiết | `Weather`: `CLEAR`, `CLOUDY`, `RAIN`, `STORM`, `FOG`, `SPIRIT_RAIN`, `SNOW`, `SANDSTORM` | |

## 5. Cơ duyên (GD-22)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Cơ duyên | `Encounter`, `EncounterDef`, ID `ENC_{vực}_{TÊN}` | |
| Phiên cơ duyên | `EncounterSession` | Một lần xảy ra cụ thể |
| Phẩm cấp cơ duyên | `EncounterTier`: `PHAM`, `LINH`, `HUYEN`, `DIA`, `THIEN` | Phàm → Thiên |
| Chuỗi cơ duyên | `EncounterChain`, ID `CHAIN_*` | |
| Điểm cơ duyên | `PoiKind.ENCOUNTER_POINT` | |
| Kết quả | `Outcome`, `OutcomeType` | Danh sách trắng ở GD-22 §8 |
| Trạng thái phiên | `SessionState`: `OFFERED`, `ACTIVE`, `RESOLVED`, `EXPIRED`, `ABORTED` | |
| Kiểm tra may rủi | `CheckDef`, hàm Ink `ROLL_CHECK` | |
| Bảo hiểm (pity) | `pityMissCount`, `sessionsSinceHuyenPlus` | |

## 6. Vật phẩm & kinh tế (GD-09, GD-13)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Vị trí vật phẩm | `ItemLocation`: `BAG`, `EQUIP`, `WAREHOUSE`, `MAIL`, `AUCTION`, `VAULT`, `DANGER_BAG`, `CORPSE`, `LEGACY`, `DESTROYED` | |
| Linh thạch | currency `SPIRIT_STONE` | |
| Tiên Ngọc | currency `JADE` (premium) | Không dùng mua sức mạnh (D-17) |
| Đan dược | item category `PILL` | |
| Mảnh ngọc giản | item category `TECHNIQUE_FRAGMENT` | |
| Ledger | `*_ledger` (bảng), `reasonCode` | Danh mục `item_log` hợp nhất ở GD-09 IT-R-100 |
| Mẫu / bản thể vật phẩm | `ItemTemplate` (ID `ITM_*`), `ItemInstance` | GD-09 §3 |
| Loại vật phẩm | `ItemCategory`: `EQUIPMENT`, `ARTIFACT`, `PILL`, `TREASURE`, `BREAKTHROUGH_AID`, `TALISMAN`, `CONTAINER`, `MATERIAL`, `TECHNIQUE_SCROLL`, `TECHNIQUE_FRAGMENT`, `RECIPE`, `RUNE`, `QUEST`, `COSMETIC_TOKEN` | |
| Phẩm vật phẩm | `ItemGrade`: `PHAM` (Phàm khí), `LINH` (Linh khí), `BAO` (Bảo khí), `TIEN` (Tiên khí), `THAN` (Thần khí) | ↔ độ hiếm rơi đồ `COMMON..LEGENDARY` (GD-09 IT-R-060) |
| Ô trang bị | `EquipSlot`: `WEAPON`, `HEAD` (Quan), `BODY` (Bào), `BELT` (Đai), `BOOTS` (Hài), `RING_1`, `RING_2`, `PENDANT` (Ngọc bội), `ARTIFACT` (Pháp bảo) | |
| Pháp bảo | category `ARTIFACT`, phím Tab | GD-09 IT-R-024 |
| Thuộc tính phụ | `AffixRoll`, ID `AFX_*` | |
| Hiệu ứng bộ | ID `SET_*`, nguồn `SET_BONUS` | |
| Ràng buộc | `BindRule`: `NONE`, `ON_PICKUP`, `ON_EQUIP`, `ACCOUNT` | |
| Cường hóa | `enhanceLevel` 0–15 | GD-11 |
| Cống hiến tông môn | currency `SECT_CONTRIBUTION` | |
| Cách dùng vật phẩm | `UseSpec.kind`: `CULTIVATION`, `HEAL`, `STATUS`, `DETOX`, `TEACH`, `LEARN_RECIPE`, `LIFESPAN_EXTENSION`, `FUSION`, `MODIFY_ROOT`, `HEART_DEMON_CURE`, `ESCAPE`, `DEATH_WARD`, `OPEN_CONTAINER`, `COSMETIC_UNLOCK` | GD-09 IT-R-070 |
| Cổng inventory | `inventory/grant`, `inventory/consume`, `inventory/move`, `currency/change` | GD-09 §9.1 |
| Trang bị chuẩn | — | GD-09 IT-R-031 (≈ +30%) |

## 7. Chiến đấu, kỹ năng, công pháp (GD-06, GD-07)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Bảng chỉ số | `StatSheet`, hàm `computeStats` | GD-06 SF-R-001 |
| Chiến lực | `combatPower` | GD-06 §6.8 |
| Status | `StatusDef`, ID `ST_*` | GD-06 §5.12; tên không có tiền tố trong tài liệu cũ là bí danh |
| Hệ của thực thể | `elementAffinity` | Người chơi = hệ tâm pháp chính |
| Kiên định | `maxPoise`, `POISE_BREAK` (phá thế) | |
| Phản ứng trúng đòn | `HitReaction`: `NONE`, `FLINCH`, `STAGGER`, `KNOCKBACK`, `KNOCKDOWN` | |
| Giáp hành động | `ArmorLevel`: `NONE`, `LIGHT`, `HEAVY`, `IMMUNE` | |
| Đỡ / đỡ hoàn hảo | `block`, `perfectParry` | |
| Vũ khí | `WeaponType`: `SWORD` (Kiếm), `FAN` (Quạt) | Beta: `DUAL_BLADE`, `FIST` |
| Phẩm công pháp | `Grade { tier: HOANG / HUYEN / DIA / THIEN, sub: HA / TRUNG / THUONG }` | |
| Thuần thục | `MasteryLevel` 1–4 | Nhập Môn · Tiểu Thành · Đại Thành · Viên Mãn |
| Tâm pháp chính | `MainTechniqueDef`, ID `TEC_*` | |
| Kỹ năng | `SkillDef`, ID `SKL_*` | |
| Ô kỹ năng | `SkillSlot`: `Q`, `E`, `R`, `F`, `V`, `T` | `T` = tuyệt kỹ |
| Hệ kế thừa | `SkillElement.INHERIT` | Hệ của tâm pháp chính |
| Bí pháp liều mạng | tag `forbidden`, `costs.lifespanYears` | Tiêu thọ (`FORBIDDEN_ART`) |
| Ngọc giản / bí tịch | item category `TECHNIQUE_SCROLL`, trường `teaches` | |
| Điểm sức mạnh kỹ năng | `powerScore`, `budget[tier]` | GD-07 §6.5 |

## 8. Quái vật & AI (GD-12)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Quái | `MonsterDef`, ID `MON_*` (thường), `MON_E_*` (tinh anh), `MON_B_*` (boss) | |
| Hạng quái | `MonsterRank`: `NORMAL`, `ELITE`, `BOSS` | |
| Họ quái | `MonsterFamily`: `BEAST`, `HUMANOID`, `SPIRIT`, `PLANT`, `INSECT`, `CONSTRUCT`, `UNDEAD`, `DEMON` | |
| Khung xương dùng chung | `SkeletonGroup`: `HUMANOID`, `QUADRUPED`, `BIRD`, `SERPENT`, `NONE` | |
| Kiểu AI | `AiArchetype`: `MELEE_BRUISER`, `MELEE_SKIRMISHER`, `RANGED_KITER`, `CASTER`, `SWARM`, `AMBUSHER`, `TANK`, `PASSIVE`, `BOSS_SCRIPTED` | |
| Trạng thái AI | `IDLE`, `PATROL`, `ALERT`, `COMBAT`, `RETURNING`, `FLEEING`, `DEAD` | GD-12 §4 |
| Bảng spawn | `SpawnTable`, ID `SP_{map}_{area}` | |
| Tinh anh định kỳ | `EliteSpawnDef` | |
| Nhóm quái cơ duyên | `SpawnGroupDef`, ID `SG_*` | |
| Pha boss | `BossDef`, `BossPhase`, `BossAction` | |
| Cuồng nộ | `enrage` | |
| Threat / mục tiêu | `threat` | GD-12 EN-R-042 |
| Dây xích | `leashRadius` | |
| Suất cận chiến | `meleeToken` | GD-12 EN-R-080 |
| Bảng rơi đồ | `LootTable`, ID `LT_*`; độ hiếm `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY` | |
| Túi đồ cá nhân | `lootBag` | |
| Được tính công | `credited` | GD-12 EN-R-100 |

## 9. Hiểm địa (GD-28)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Hiểm địa | `DangerZoneDef`, ID `V{n}_HD{nn}` | |
| Cấp hiểm địa | `DangerTier` 1–5 | Nhất … Ngũ giai |
| Lượt hiểm địa | `DangerRun`, `DangerRunMember` | |
| Pha | `DangerPhase`: `CALM` (Yên tĩnh), `UNREST` (Bất an), `STORM` (Bạo loạn), `COLLAPSE` (Sụp đổ) | |
| Trạng thái thành viên | `MemberState`: `IN`, `DOWNED`, `DORMANT_GUARD`, `EXTRACTED`, `DIED`, `PROTECTED_EXPELLED`, `FORCE_EXTRACTED` | |
| Hấp hối | action state `DOWNED` | |
| Quy tức | action state `DORMANT_GUARD`, status `ST_DORMANT_GUARD` | Mất kết nối |
| Thoát | `extract`; lối thoát `ExitDef` | |
| Truyền Tống Phù | `ITM_TRUYEN_TONG_PHU` | |
| Di hài | `Corpse`, `ItemLocation.CORPSE` | |
| Di vật | `originCharacterId` | |
| Bẫy trận pháp | `TrapDef`, ID `TRP_*` | |
| Nguy hiểm môi trường | `HazardKind`: `POISON_FOG`, `KILLING_INTENT`, `QI_STORM` | |
| Phí vào | `entryFee` | Sink linh thạch |
| Chiêu của quái | `SkillDef`, ID `SKL_MON_*` | |
| Thiện ác | `karma` int −100..100 | |
| Cờ trạng thái | `lifeFlag` (theo kiếp), `accountFlag` (theo tài khoản), ID `FLAG_*` | Dùng làm điều kiện cơ duyên / nhiệm vụ |

## 10. Kinh tế (GD-13)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Thu nhập mỗi giờ chơi | `IPH`, `iphByBand` | Thước đo gốc cho giá / phí (GD-13 EC-R-020) |
| Dải kinh tế | `RealmBand`: `PN`, `LK_EARLY`, `LK_MID`, `LK_LATE`, `TC`, `KD`, `NA` | |
| Hạ / Trung / Thượng phẩm linh thạch | `displayUnits` | Chỉ hiển thị; đơn vị lưu là số nguyên |
| Giá trị tham chiếu | `value`, `gradeValueMult` | GD-13 §5.3, §6.2 |
| Nguồn / nơi tiêu tiền | faucet / sink, `CurrencyReason` | |
| Cửa hàng NPC | `ShopDef`, ID `SHOP_*` | |
| Vạn Bảo Lâu | `AuctionListing` | Sàn đấu giá, Beta |
| Giao dịch trực tiếp | `TradeSession` | Beta |
| Ảnh chụp kinh tế | `EconomySnapshot` | Dashboard |

## 11. Luyện đan (GD-10)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Đan phương / khí phổ | `RecipeDef`, ID `RCP_*`, `RecipeKind`: `ALCHEMY`, `FORGING` | |
| Vai trò nguyên liệu | `InputRole`: `MAIN`, `AUX`, `CATALYST` | |
| Phiên chế tác | `CraftSession`, `CraftMode`: `MINIGAME`, `AUTO` | |
| Khống chế hỏa hầu | minigame, `MinigameEvent` (`FIRE_ON`, `FIRE_OFF`, `ADD`) | Server mô phỏng lại (GD-10 AL-R-001) |
| Tạc lô | `overheat` | Thất bại khi quá nhiệt |
| Đan lô | category `TOOL`, `FurnaceSpec` | |
| Hỏa chủng | `FireSpec` | |
| Luyện đan sư (cấp) | `profession.kind = ALCHEMY`, `level` 1–9 | |
| Thuần thục đan phương | `KnownRecipe.crafts` → 1–4 | |
| Hỏa hầu tâm đắc | `pityFails` | Bảo hiểm thất bại |
| Đan văn | `AFX_PILL_PATTERN` | Hiệu ứng ×1,2 |
| Bạo đan | `bonusYield` | +1 viên / mẻ |
| Đan Tra (phế đan) | `ITM_DAN_TRA` | |

## 12. Luyện khí (GD-11)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Khí phổ | `RecipeDef` với `kind = FORGING`, ID `RCP_FORGE_*` / `RCP_THIET_KIEM` | |
| Lò rèn | POI `FORGE_STATION` | |
| Búa rèn | category `TOOL`, `HammerSpec` | |
| Xúc tác | `CatalystSpec`, `affixBias` | Định hướng thuộc tính phụ |
| Minigame rèn | nhịp búa, `rhythm` (`bpm`, `beats`) | Hoàn hảo / Tốt / Trượt / Đập lệch |
| Dị tượng / khí linh | `spiritBorn` | +1 dòng thuộc tính phụ |
| Phân giải | `DISMANTLE` → `ITM_LINH_KIM_TIET` | |
| Luyện đá | `REFINE_STONE` → `ITM_CUONG_HOA_THACH_{BAND}` | |
| Cường hóa | `ENHANCE`, `enhanceLevel` 0–15, `enhancePity`, Hộ Phù `ITM_HO_PHU` | Không phá hủy vật phẩm |
| Tẩy luyện | `REROLL`, `PendingReroll` (`VALUES` / `LINE`) | Chọn giữ cũ / lấy mới |
| Thăng phẩm | `UPGRADE`, `upgradePity` | |
| Trận văn / khắc | category `RUNE`, `RuneSpec`, `sockets` | Beta |
| Phế phôi | `ITM_PHE_PHOI` | |
| Luyện khí sư (cấp) | `profession.kind = FORGING` | |

## 13. Xã hội (GD-14)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Tổ đội | `Party`, `PartyInvite` | 1–5 người, rơi đồ cá nhân |
| Bảng Chiêu Mộ | `LfgListing` | Tìm đội |
| Đánh dấu | `ping` | |
| Thanh Vân Tông (tông môn NPC) | `NpcSectMembership`, `sectId = SECT_THANH_VAN` | Thuộc kiếp |
| Bậc tông môn NPC | `NpcSectRank`: `TAP_DICH`, `NGOAI_MON`, `NOI_MON`, `CHAN_TRUYEN` | |
| Đại tông NPC | `NpcSectId`: `SECT_THANH_VAN` (Thanh Vân Tông), `SECT_TAC_THIEN` (Tạc Thiên Bang), `SECT_HOP_HOAN` (Hợp Hoan Tông) | Một tông mỗi kiếp (GD-14 §5.4b) |
| Phe | `Alignment`: `CHINH`, `TA`, `MA`, `NONE` | Theo tông đang tham gia |
| Tổng đàn | Thanh Vân Trấn · Tạc Thiên Trại (`V1_F03`) · Hợp Hoan Cốc (`V1_F05`) | |
| Phản môn | `NpcSectMembership.state = LEFT` | Hồi chiêu 7 ngày |
| Song tu | phúc lợi Hợp Hoan Tông: đả tọa cùng đồng đội +10% | |
| Hắc Thị | `SHOP_V1_BLACK_MARKET` | Tạc Thiên Trại |
| Chuyển thế đệ tử | `rebornDisciple`, cờ `FLAG_ACC_JOINED_SECT` | |
| Tông môn tự lập (guild) | `PlayerSect`, `PlayerSectMember` | Thuộc tài khoản, Beta |
| Chức vụ | `SectRole`: `CHUONG_MON`, `TRUONG_LAO`, `CHAP_SU`, `DE_TU` | |
| Công trình / trú địa | `SectBuilding`: `DAN_PHONG`, `LUYEN_KHI_CAC`, `LINH_MACH_DIEN` | |
| Tông khố, công trạng | `treasuryStones`, `merit` | |
| Kênh chat | `ChatChannel`: `WORLD`, `MAP`, `PARTY`, `SECT`, `WHISPER`, `SYSTEM` | |
| Bạn bè / chặn | `Friendship`, `BlockEntry` | Thuộc tài khoản |
| Thông báo toàn server | `broadcast.requested`, ưu tiên 1–4 | GD-14 SO-R-060 |

## 14. Động phủ & Sơn môn (GD-30)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Cửa động | POI kind `CAVE_GATE`, `gateGrade` 1–3 (phẩm địa mạch) | Nhiều chủ dùng chung (phasing) |
| Động phủ | `Cave` (thuộc tài khoản), `MapType.CAVE` | Không có kho chứa đồ |
| Công trình động phủ | `CaveBuilding`: `TINH_THAT` (Tĩnh Thất), `LINH_DIEN` (Linh Điền), `DAN_THAT` (Đan Thất), `KHI_THAT` (Khí Thất), `THU_VIEN` (Linh Thú Viên — GD-25) | Cấp 0–3 |
| Quyền thăm | `VisitPolicy`: `PRIVATE`, `FRIENDS`, `PARTY`, `SECT` | |
| Linh Điền / hạt giống | `SpiritPlot`, `SeedDef`, ID `ITM_SEED_*` | |
| Địa điểm sơn môn | POI kind `SECT_SITE`, `SectSite`, `SiteGrade`: `A`, `B`, `C` | |
| Trạng thái sơn môn | `SiteState`: `VACANT`, `HELD`, `RUINS` | |
| Trú địa tông môn | `MapType.SECT_GROUNDS` | |
| Uy vọng | `SectPrestigeWeek` | Điểm PvE của tông môn |
| Sơn Môn Đại Hội | `conclave` | Phân sơn môn hằng tháng |
| Phế tích | `RUINS`, sự kiện Phế Tích | 7 ngày |

## 15. Nhiệm vụ (GD-15)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Nhiệm vụ / chuỗi | `QuestDef`, ID `QC_*`; mẫu hằng ngày `QD_*`; mẫu hằng tuần `QW_*` | |
| Loại nhiệm vụ | `QuestCategory`: `TUTORIAL`, `MAIN`, `SIDE`, `BACKGROUND`, `SYSTEM`, `SECT_DAILY`, `SECT_WEEKLY` | |
| Trạng thái nhiệm vụ | `QuestState`: `ACTIVE`, `READY_TO_TURN_IN`, `COMPLETED`, `FAILED`, `ABANDONED` | |
| Bước / mục tiêu | `QuestStep`, `Objective` (17 loại, GD-15 §3) | |
| Hành động nhiệm vụ | `QuestAction` (danh sách trắng GD-15 §8) | |
| Bảng nhiệm vụ tông môn | `SectTaskBoard` | 3 ô / ngày, làm mới 05:00 |
| Lược truyện | `rebornSkip` | Từ kiếp 2, thưởng × 0,5 |

## 16. Chế tạo công pháp (GD-08)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Mảnh ngọc giản | `TECHNIQUE_FRAGMENT`, `FragmentSpec`, ID `ITM_FRAG_{componentId}_{TIER}` | |
| Hạch tâm / Nguyên tố / Biến thức | `CoreDef` (`CORE_*`), `ElementDef` (`ELEM_*`), `ModifierDef` (`MOD_*`) | `ComponentKind` |
| Phép vá | `PatchOp`, `PatchPath` | Danh sách trắng GD-08 §3 |
| Ngộ Đạo Thạch | POI `INSIGHT_STATION` | Tàng Kinh Các, Thanh Vân Tông |
| Diễn Pháp Tràng | `sandbox` | Thử chiêu 60s, 5 lần / ngày |
| Điểm ngộ đạo | `q` ∈ [0, 1] | |
| Áp chế ngân sách | `suppressFactor` | "Thiên đạo bất dung" khi < 0,5 |
| Công pháp tự chế | `CraftedTechnique`, ID `CSK_*`, `source = CRAFTED` | Bất biến |
| Ngọc giản tự chế | `ITM_SCROLL_CRAFTED_{TIER}`, `craftedRefId` | |
| Ngọc Giản Trống | `ITM_BLANK_JADE` | Sao chép (Beta) |

## 17. Kiến trúc kỹ thuật (TD-01)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Cổng ghi | inventory / cultivation / lifecycle / talent / technique / quest gate | Một miền một cổng (TA-R-006) |
| Khóa idempotency | `{loại}:{uuidv7}`, bảng `idempotency_record` | Giữ 7 ngày |
| Sự kiện domain | `DomainEvent`, bảng `outbox_event`, Valkey Stream `evt:{module}` | Ít nhất một lần |
| Kiểm toán ngẫu nhiên | `rng_audit`, `kind` (danh sách đóng), xoshiro128** | Tái lập từ seed |
| Ngữ cảnh nhân vật | `CharacterContext`, Valkey `ctx:{characterId}` | Kiểm tra vị trí cho request REST |
| Vé vào room | `joinTicket` (JWT 60s, dùng một lần) | |
| Nhật ký tử vong | `death_log`, `deathLogId` | 60s cuối |
| Mã dùng chung | `shared/sim`, `shared/contracts`, `shared/events`, `shared/testvectors` | |
| Phiên bản gamedata | `gamedataVersion` (12 hex SHA-256) | TD-07 DT-R-090 |
| Đăng ký file gamedata | `shared/gamedata/registry.json` | TD-07 §9.1 |
| Nguồn kiểu gamedata | `shared/gamedata-types/*.ts` | Sinh JSON Schema + Java |
| Gắn phase nội dung | `phase`: `ALPHA`, `BETA`, `LAUNCH`, `POST_LAUNCH` | Build theo hồ sơ |
| Ngừng dùng / bí danh | `deprecated: true`, `aliases.json` | Không đổi tên / tái dùng ID |
| Vector kiểm thử hai ngôn ngữ | `shared/testvectors/*.json` | TD-07 §11 |
| Input di chuyển | `C2S_Input` (`seq`, `move`, `aim`) | TD-02 §5 |
| Tua lại / bù trễ | `rewindMs`, `maxRewindMs`, `defenseLagCompMs`, `interpDelayMs` | TD-02 §7 |
| Vùng quan tâm | AOI (vào 40m / ra 48m) | TD-02 §6 |
| Thân thực thể | `bodyRadius`, cỡ quái `size`: `S`, `M`, `L`, `XL` | TD-14 §3 |
| Lớp va chạm | `CollisionLayer`, cờ `move` / `projectile` / `sight` | TD-14 §3.3 |
| Hình học đòn | `Sector`, `OBB`, `Capsule`, `shapeGeometry`, `queryTargets` | TD-14 §5 |

## 18. Ngẫu nhiên & Công bằng (GD-29)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Loại roll | `RollClass`: `A` (quyết định có rủi ro), `B` (khám phá), `C` (khởi đầu), `D` (chiến đấu), `E` (tiền thật) | |
| Xem trước tỷ lệ | `ChanceView`, `ChanceLine`, `quoteId` | Lỗi `CHANCE_CHANGED` |
| Điểm phần trăm | `@unit pp` | Chỉ cho xác suất cộng dồn |
| Mức độ hiếm | `RarityBand`: `COMMON`, `UNCOMMON`, `RARE`, `VERY_RARE`, `LEGENDARY` | Thay cho tỷ lệ loại B |
| Thiên Cơ Lục | `FortuneLogEntry`, `fortune-log` | 50 roll loại A gần nhất |
| Khiếu nại thiên đạo | `fortune_dispute` | |
| Dạng bảo hiểm | `ADDITIVE_PP`, `MULTIPLICATIVE`, `WEIGHT_BOOST`, `GUARANTEE` | |
| Dữ liệu chỉ ở server | `@serverOnly`, `bundle.server` / `bundle.client` | |

## 19. Linh thú (GD-25)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Loài linh thú | `BeastSpeciesDef`, ID `BST_*` | Model từ quái `MON_*` |
| Đặc tính thú | `BeastTraitDef`, ID `BTR_*` | Dùng `TalentEffect` |
| Linh thú (cá thể) | `Beast`, trạng thái `BeastState`: `CONTRACTED`, `STABLED`, `BREEDING`, `SEALED`, `DEPARTED` | Thuộc tài khoản |
| Huyết mạch | `BloodlineGrade`: `PHAM`, `LINH`, `HUYEN`, `DIA`, `THIEN` | |
| Tư chất | `aptitudes`: `vigor` (Cương), `might` (Lực), `spirit` (Linh), `agility` (Tốc) | 0–100 |
| Vai thú | `BeastRole`: `ASSAULT`, `GUARDIAN`, `MYSTIC`, `FORAGER` | |
| Linh chủng | biến thể quái hiếm bắt được | `linhChungChance` |
| Ngự Thú Phù | `UseSpec CAPTURE_BEAST` | |
| Trứng | category `BEAST_EGG`, `ITM_EGG_*`, `ITM_EGG_BRED` | |
| Kết khế / xuất chiến | `contractCharacterId`, phím `G`; chiêu lệnh phím `T` | |
| Thân mật | `bond` 1–10 | |
| Biến dị | `MutationKind`: `BLOODLINE_UP`, `RARE_TRAIT`, `APTITUDE_SURGE`, `COLOR_VARIANT` | |
| Linh Thú Viên | `CaveBuilding.THU_VIEN` | GD-30 |
| Ngự Thú Sư | NPC `NPC_BEAST_KEEPER` | `HUB` |
| Thú Hồn Tinh / Thú Ấn | `ITM_BEAST_SOUL_*`, `ITM_BEAST_SEAL` | |

## 20. Di Tàng & Tàn hồn (GD-26 §5.15)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Di Tàng | `LegacyStash`, `ItemLocation.SEALED` | Kho đồ kiếp trước bị phong ấn |
| Tầng ấn | `SealBand`: `LK`, `TC`, `KD`, `NA` | Theo cảnh giới vật phẩm |
| Tàn hồn | `RemnantSnapshot`, quái `MON_B_REMNANT` | Bóng ma nhân vật kiếp trước |
| Luân Hồi Đài | POI `REMNANT_ALTAR` | Nơi mở ấn |
| Tàn Hồn Cảnh | instance một người | Thua không chết |
| Tàn hồn tiêu tán | `SEAL_DISSIPATED` | Kiếp sau kết thúc mà chưa mở |
| Động phủ phong ấn | `caveSealed`, thủ vệ / thú canh cửa, `MON_CAVE_WARDEN` | GD-30 HM-R-045 |
| Ký ức nghề | `profession_memory` (`maxLevel`, đan phương / khí phổ) | Cấp tài khoản, không tiêu tán (GD-10 §5.9) |
| Giai đoạn nghề | `professionTiers`: `SO` (1–3), `TRUNG` (4–6), `CAO` (7–9) | |
| Khảo hạch nghề | `RCP_TRIAL_ALCHEMY_*`, `RCP_TRIAL_FORGE_*`, NPC `NPC_ALCHEMY_ELDER`, `NPC_FORGE_ELDER` | Minigame, không roll |
| Danh vọng phe | `FactionReputation` (`CHINH`, `TA`, `MA`), bậc Vô danh · Có tiếng · Danh chấn · Uy chấn · Truyền kỳ | GD-14 §5.4c |
| Thiên Bảng Chiến Lực | `rank:cp:{server}`, top 100 theo `combatPower` | GD-14 §5.4d |
| Xuất thân gắn tông | `BG_TAC_THIEN_GANG`, `BG_HOP_HOAN_DISCIPLE` | GD-27 |
| Gói phúc lợi môn phái | `BenefitSource`: `NPC_SECT`, `PLAYER_SECT`, `WANDERER` | Chỉ hưởng 1 gói, đổi mỗi 7 ngày (GD-14 §5.4e) |
| Tông quy | `sectEdicts`, ID `EDICT_*` | Phúc lợi của tông tự lập, 1–3 theo cấp |
| Tán tu tự tại | gói `WANDERER` | Không có sư môn NPC |

## 21. PvP dã ngoại & Sát nghiệp (GD-32)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Chế độ PK | `PkMode`: `PEACE` (Hòa bình), `GUILD` (Tông môn), `SLAUGHTER` (Sát lục) | |
| Sát nghiệp | `PkState.infamy` | Điểm ác nhân, thuộc kiếp |
| Bậc sát nghiệp | `InfamyTier`: `CLEAN`, `GREY` (cờ xám 60s), `RED` (Ác nhân ≥ 100), `CRIMSON` (Đại ác nhân ≥ 300, truy nã) | |
| Huyết thù | `PkState.feuds` | 24h trả thù không mang nghiệp |
| Chấp Pháp | NPC `MON_E_ENFORCER`, Chấp Pháp Đường | Truy sát ác nhân |
| Truy nã | Bảng Truy Nã, tiền thưởng từ ví ác nhân | |
| Hệ số PvP | `pvpDamageMult` 0,5, `pvpCcDurationMult` 0,6 | |
| Chấp Pháp Trưởng Lão | `MON_E_ENFORCER_ELDER` | Truy sát Đại ác nhân |
| Chấp Pháp Lệnh | `ITM_ENFORCER_TOKEN` | Thưởng khi hạ Chấp Pháp, đổi ở Hắc Thị |
| Thiên phạt / Thiên Đạo Hóa Thân | `ST_HEAVEN_PUNISHMENT`, `MON_B_HEAVEN_AVATAR` | Giết Chấp Pháp → bị trời truy sát (GD-32 PK-R-055) |
| Thiên Đạo Tàn Ấn / Nghịch Thiên | `ITM_HEAVEN_REMNANT_SEAL`, danh hiệu | Thưởng khi hạ Hóa Thân |
| Danh hiệu Tạc Thiên | Tạc Thiên Tôn Giả (1 lần) · Tạc Thiên Chí Tôn (3) · Tạc Thiên Đại Đế (10) — biểu tượng (GD-32 PK-R-056) | Thành viên Tạc Thiên Bang hạ Thiên Đạo Hóa Thân |
| NPC lãnh đạo Tạc Thiên Bang | `NPC_TAC_THIEN_CHIEF` (Tạc Thiên Đại Đế), `NPC_TAC_THIEN_ELDER_*` (Chí Tôn / Tôn Giả) | Mang danh hiệu người chơi có thể đạt |
| Ma Vực | `dangerKind = MA_VUC` | Hiểm địa PvP chết thật (GD-28 §5.12) |

## 22. Thế giới & bản đồ (TD-12, TD-13)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Room bản đồ mở / instance | `OpenMapRoom` (`{mapId}#{channel}`), `InstanceRoom` (`{mapId}~{id}`, `CAVE~`, `SECT~`, `TRIAL~`) | TD-12 §2 |
| World Directory / Transfer Coordinator | module `world` của API | Chọn room, cấp vé, chuyển bản đồ |
| Chuyển bản đồ | `transferId`, `S2C_TransferTicket`, `arrival` | TD-12 §5 |
| Định nghĩa bản đồ | `MapDef`, `ConnectionDef` (`SEAM`, `GATE`, `REGION_GATE`, `INSTANCE_DOOR`), `ZoneDef` (`SAFE`, `NO_ENFORCER`, `NO_PVP`, `DORMANT_SAFE`, `NO_FLY`) | TD-13 §3 |
| Loại POI | `PoiKind` (danh sách đóng, TD-13 §4) | |

## 23. Kỹ thuật client / server / code (TD-04..TD-10)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Gói mã nguồn | `@tienlo/client`, `@tienlo/game-server`, `@tienlo/sim`, `@tienlo/schema`, `@tienlo/gamedata`, `@tienlo/contracts`; Java `vn.tienlo.api.{module}` | TD-10 §2 |
| Khung ý định đầu vào | `IntentFrame` | TD-05 §2.3, TD-01 TA-R-161 |
| Mức chất lượng đồ họa | `LOW`, `MEDIUM`, `HIGH` | TD-05 |
| Khóa idempotency (API công khai) | header `Idempotency-Key` | TD-04 |
| Cờ an ninh / xử phạt | `security_flag`, `sanction` | TD-08 |
| Tệp ngữ cảnh cho AI | `AGENTS.md` | TD-10 CC-R-110 |

## 24. Monetization & điều khiển (GD-19, GD-24)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Tiên Ngọc | currency `JADE`, ledger `JADE_*` | Tách biệt linh thạch (GD-13 EC-R-001) |
| Ngoại hình / ưu đãi | `CosmeticDef` (`COS_*`), `ShopOffer` (`OFR_*`) | Thuộc tài khoản, giữ qua kiếp |
| Vấn Đạo Lệnh | mùa `SS_*`, nhiệm vụ mùa `QW_SEASON_*` | Battle pass, chỉ cosmetic / tiện ích |
| Hạn mức chi tiêu | `spend_limit`, lỗi `SPEND_LIMIT_REACHED` | GD-19 |
| Thiết lập điều khiển | `ControlSettings` (trong `account.settings`) | GD-24 |
| Hành động điều khiển | `ActionId` (danh sách đóng) | Sơ đồ phím chuẩn GD-24 §5 |

## 25. Sự kiện thế giới, ngự kiếm, lore (GD-23, GD-31, LR-01)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Sự kiện thế giới | `WorldEventDef` (`WE_*`), bản ghi chạy `WEI_*`, trạng thái `ANNOUNCED` → `ACTIVE` → `RESOLVING` → `ENDED` / `CANCELLED` | GD-23 |
| Đóng góp sự kiện | `contribution`, bậc Đồng / Bạc / Vàng | GD-23 |
| Linh khí bùng phát | status `ST_QI_SURGE` | GD-23 |
| Ngự kiếm | `FlightState`: `GROUNDED`, `TAKING_OFF`, `FLYING`, `LANDING`, `FALLING`; phím `FLIGHT_TOGGLE` = `N` | GD-31 |
| Vùng cấm bay / cờ bay | `ZoneDef.kind = NO_FLY`, cờ va chạm `fly` | TD-13, TD-14 |
| Quái đánh được người bay | tag `anti_air` | GD-12 |
| Tên lore (đại lục, vực, kỷ nguyên, chân linh, Chấp Pháp Đường, Vạn Bảo Lâu…) | xem [LR-01 §10](world-bible.md) | |
| Phi Thăng | cảnh giới sau Hóa Thần (sau launch), `EndCause.ASCENDED` | GD-04, LR-01 |

## 26. Mỹ thuật, UI, onboarding (AR-01, AR-02, GD-17, GD-18)

| Thuật ngữ | Định danh | Ghi chú |
|-----------|-----------|---------|
| Điểm gắn | socket `socket_weapon_r`, `socket_weapon_l`, `socket_back`, `socket_head_fx`, `socket_feet_fx`, `socket_chest_fx` | AR-02 §4 |
| Marker va chạm hoạt ảnh | marker `hit` trong clip (khớp `windupSec + hit.atSec`) | AR-02 §5.1, TD-05 CL-R-071 |
| Atlas gradient | texture dải màu dùng chung cho prop / môi trường low-poly | AR-02 §6 |
| KTX2 | định dạng texture nén GPU (qua glTF-Transform) | AR-02 AST-R-002 |
| Mẫu va chạm prop | `collisionTemplate` `CIRCLE` / `POLYGON` + cờ `move` / `projectile` / `sight` / `fly` | AR-02 §7, TD-13 |
| Hộp xác nhận rủi ro | `RiskConfirm` (giữ nút 2s) | GD-17 |
| Hiển thị tỷ lệ | `ChanceView` | GD-17, GD-29 |
| Gợi ý onboarding | `HintDef` (`HINT_*`), `account.settings.hintsSeen` | GD-18 |
| Cơ duyên dàn dựng | `EncounterDef.scripted`, `ENC_V1_FIRST_OMEN` | GD-18, GD-22 |
| Trạng thái hành động | `ActionState` (danh sách đóng GD-07 §4) → clip theo AR-03 | GD-07, AR-03 |
| Clip hoạt ảnh | `ANM_{SKELETON}_…` | AR-03 |
| Mẫu / bảng màu VFX | `VFX_*`, `VFXO_*`, `VPAL_*`, `TGPAL_*` | AR-04 |
| Kit môi trường | `KIT_*`, mảnh `PRP_*`, bảng màu `PAL_*`, mẫu biome `BIO_*` | AR-06 |
| Mã NPC | `NPC_{ROLE}`, `NPC_{SECT}_{ROLE}`, `NPC_V{n}_*`; hồ sơ ở LR-02 | LR-02, TD-07 |
| Vũ khí đánh đàn (chưa có `WeaponDef`) | `WeaponType.ZITHER` (Cầm) — SK-Q-6 | GD-07 |
| Mê hoặc | status `ST_CHARMED` | GD-06 SF-R-124 |
| Hệ tâm pháp công khai | `PlayerPublic.elementAffinity` (`NONE` = 0, rồi theo `Element`) | TD-02 NW-R-025 |
| Thời lượng phản ứng / né | `stats/params.json` → `reactionSec`, `dodge.durationSec` | GD-06 SF-R-104 |
| Phân loại VFX | `VfxCategory`, `VfxPhase`, `VfxMotif` (`BLADE_SHARD`, `LEAF_VINE`, `WATER_RIPPLE`, `FLAME_TONGUE`, `ROCK_DUST`, `ZIGZAG_BOLT`, `HEX_CRYSTAL`, `WIND_RIBBON`, `PLAIN_QI`) | AR-04 |
| VFX đặc biệt | `VPAL_NONE` (vô hệ), `VFX_THRUST` (hình `LINE`), `VFX_BOSS_{TÊN}_{CHIÊU}` | AR-04, GD-07, GD-12 |
| Hiển thị dữ liệu gamedata | `visibility`: `all` / `server` / `client` | TD-07 DT-R-101 |
| Loại / ngân sách asset | `kind` (… `ENV`, `TER`, `UI`, `POR`, `ILL`, `WMAP`), `budgetClass` | AR-02 §3, §6 |
| Tiền tố asset bổ sung | `ENV_`, `TER_`, `UI_`, `POR_`, `ILL_`, `WMAP_`, `KIT_CM_`, hậu tố `_SAMPLE_X` | AR-02 §3 |
| Kiểu môi trường | `EdgeProfile`, `KitCategory`, `MaterialGroup`, `BiomeTemplate` | AR-06, TD-13 §5.3 |
| Nguồn asset | provenance `source` … `SERVICE` (dịch vụ kiểu Mixamo) | TD-15 OSS-R-034 |
| Trạng thái license | ✅ · ⚠️ chờ duyệt · ⚠️ rủi ro · ❌ | TD-15 §4.7 |
| Mã NPC Vực 1 (LR-02) | `NPC_V1_LAM_THANH_PHONG`, `NPC_THANH_VAN_CHIEF`, `NPC_V1_DISCIPLINE_ELDER`, `NPC_TAC_THIEN_ELDER_TAN_NGAO` / `_LIEU` / `_CUNG`, `NPC_V1_TAC_THIEN_CONTACT`, `NPC_HOP_HOAN_CHIEF`, `NPC_HOP_HOAN_ELDER_DIEP`, `NPC_HOP_HOAN_SAINTESS`, `NPC_V1_HOP_HOAN_ENVOY`, `NPC_V1_ENFORCER_CHIEF`, `NPC_V1_AUCTION_MANAGER`, `NPC_V1_PHARMACIST`, `NPC_V1_ARMORER`, `NPC_V1_GROCER`, `NPC_V1_SKY_WATCHER`, `NPC_V1_QUIET_HEART_MASTER`, `NPC_V1_LONGEVITY_ELDER`, `NPC_V1_F02_HEADMAN`, `NPC_V1_F03_OLD_MINER`, `NPC_V1_F04_HERB_GATHERER`, `NPC_V1_F05_HUNTER`, `NPC_V1_F03_BANDIT_CHIEF` | hồ sơ: LR-02 |
| Cờ chứng cứ mỏ cũ | `FLAG_MINE_EVIDENCE`, `FLAG_TOLD_LAM_TRUTH` | GD-15 §5.10a |
| Lệnh kiểm tra | `pnpm art:check`, `pnpm docs:check` | TD-10 CC-R-105 |
| NPC `V1_F01` | `NPC_V1_F01_HEADMAN` (Nghiêm Bá), `NPC_V1_F01_FIREFLY_GIRL` (A Huỳnh, chỉ ban đêm) | LR-02, GD-16 |
| NPC chủ trì hội thoại của bước | `QuestStep.dialogueNpcId` | GD-15 QS-R-056 |
| Spawn quái theo khu | `SpawnAreaDef` (`id` = `SP_*`, `polygon`, `spawnPoints`), `SpawnTable.minPlayerDistance` | TD-13, GD-12 EN-R-036 |
| Mã nội dung bổ sung | `ESP_{mapId}_{TÊN}` (tinh anh đặt chỗ), `LT_*` (bảng rơi), `CDX_*` (Vạn Giới Lục), `LP_*`, `CHEST_*` (mẫu rương), `BGM_*` (nhạc nền) | TD-07, GD-12, AU-01 |

