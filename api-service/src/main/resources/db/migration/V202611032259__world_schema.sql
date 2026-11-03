-- Consolidated PostgreSQL DDL for the world module, sourced from project specifications.

-- Source: docs/design/18-enemies-ai.md
CREATE TABLE game.boss_kill_log (
  id BIGSERIAL PRIMARY KEY, monster_id VARCHAR(48) NOT NULL, map_id VARCHAR(32) NOT NULL,
  party JSONB NOT NULL, duration_sec INT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/tech/02-database.md
CREATE TABLE game.character_secret (character_id UUID NOT NULL, group_id VARCHAR(48) NOT NULL, position_index SMALLINT NOT NULL,
  found_at TIMESTAMPTZ, PRIMARY KEY (character_id, group_id));

-- Source: docs/tech/02-database.md
CREATE TABLE game.codex_entry (
  account_id UUID NOT NULL, entry_type VARCHAR(16) NOT NULL CHECK (entry_type IN ('MONSTER','HERB','PLACE','ITEM')),
  entry_id VARCHAR(48) NOT NULL, level SMALLINT NOT NULL DEFAULT 0, progress INT NOT NULL DEFAULT 0, unlocked_at TIMESTAMPTZ,
  PRIMARY KEY (account_id, entry_type, entry_id)
);

-- Source: docs/tech/02-database.md
CREATE TABLE game.discovered_poi (account_id UUID NOT NULL, poi_id VARCHAR(48) NOT NULL, discovered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, poi_id));

-- Source: docs/design/24-world-events.md
CREATE TABLE game.event_reminder (account_id UUID NOT NULL, event_id VARCHAR(48) NOT NULL, PRIMARY KEY (account_id, event_id));

-- Source: docs/tech/02-database.md
CREATE TABLE game.map_discovery (
  account_id UUID NOT NULL, map_id VARCHAR(24) NOT NULL, fog_bitmap BYTEA NOT NULL,
  discovered_pct NUMERIC(5,2) NOT NULL DEFAULT 0, updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, map_id)
);

-- Source: docs/design/24-world-events.md
CREATE TABLE game.world_event_daily (account_id UUID NOT NULL, day DATE NOT NULL, full_rewards SMALLINT NOT NULL DEFAULT 0,
  world_boss_claims JSONB NOT NULL DEFAULT '[]', PRIMARY KEY (account_id, day));

-- Source: docs/design/24-world-events.md
CREATE TABLE game.world_event_log (
  id VARCHAR(64) PRIMARY KEY,
  event_id VARCHAR(48) NOT NULL, state VARCHAR(12) NOT NULL,
  scope_resolved JSONB NOT NULL, seed BYTEA NOT NULL,
  announced_at TIMESTAMPTZ NOT NULL, starts_at TIMESTAMPTZ NOT NULL, ends_at TIMESTAMPTZ NOT NULL, ended_at TIMESTAMPTZ,
  outcome VARCHAR(12), participants INT, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/24-world-events.md
CREATE TABLE game.world_event_participation (
  instance_id VARCHAR(64) NOT NULL, character_id UUID NOT NULL, account_id UUID NOT NULL,
  points INT NOT NULL, presence_sec INT NOT NULL, tier VARCHAR(8), rewarded BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (instance_id, character_id)
);
