-- Consolidated PostgreSQL DDL for the social module, sourced from project specifications.

-- Source: docs/design/20-social.md
CREATE TABLE game.block_entry (
  account_id UUID NOT NULL, blocked_account_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, blocked_account_id)
);

-- Source: docs/tech/02-database.md
CREATE TABLE game.character_benefit (
  character_id UUID PRIMARY KEY REFERENCES game.character(id),
  source VARCHAR(12) NOT NULL CHECK (source IN ('NPC_SECT','PLAYER_SECT','WANDERER')), switched_at TIMESTAMPTZ NOT NULL
);

-- Source: docs/design/20-social.md
CREATE TABLE game.chat_message_log (
  message_id UUID NOT NULL, channel VARCHAR(8) NOT NULL, scope_id VARCHAR(80),
  from_character_id UUID, from_account_id UUID, text_raw TEXT NOT NULL, text_filtered TEXT NOT NULL,
  sent_at TIMESTAMPTZ NOT NULL, PRIMARY KEY (sent_at, message_id)
);

-- Source: docs/design/20-social.md
CREATE TABLE game.chat_mute (
  account_id UUID NOT NULL, until_at TIMESTAMPTZ NOT NULL, reason VARCHAR(48), by_gm UUID,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), PRIMARY KEY (account_id, created_at)
);

-- Source: docs/tech/02-database.md
CREATE TABLE game.faction_reputation (
  character_id UUID PRIMARY KEY REFERENCES game.character(id),
  chinh INT NOT NULL DEFAULT 0, ta INT NOT NULL DEFAULT 0, ma INT NOT NULL DEFAULT 0,
  daily_day DATE, daily_chinh INT NOT NULL DEFAULT 0, daily_ta INT NOT NULL DEFAULT 0, daily_ma INT NOT NULL DEFAULT 0,
  best JSONB NOT NULL DEFAULT '{}',
  CHECK (chinh >= 0 AND ta >= 0 AND ma >= 0)
);

-- Source: docs/design/20-social.md
CREATE TABLE game.friend_request (
  request_id UUID PRIMARY KEY, from_account_id UUID NOT NULL, to_account_id UUID NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), UNIQUE (from_account_id, to_account_id)
);

-- Source: docs/design/20-social.md
CREATE TABLE game.friendship (
  account_id UUID NOT NULL, friend_account_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, friend_account_id)
);

-- Source: docs/design/20-social.md
CREATE TABLE game.npc_sect_membership (
  character_id UUID NOT NULL REFERENCES game.character(id),
  sect_id VARCHAR(32) NOT NULL, rank VARCHAR(16) NOT NULL,
  contribution_total INT NOT NULL DEFAULT 0, reborn_disciple BOOLEAN NOT NULL DEFAULT FALSE,
  state VARCHAR(8) NOT NULL DEFAULT 'ACTIVE' CHECK (state IN ('ACTIVE','LEFT')),
  joined_at TIMESTAMPTZ NOT NULL DEFAULT now(), left_at TIMESTAMPTZ,
  PRIMARY KEY (character_id, sect_id)
);

-- Source: docs/design/20-social.md
CREATE TABLE game.player_report (
  report_id UUID PRIMARY KEY, reporter_account_id UUID NOT NULL, target_account_id UUID NOT NULL,
  reason VARCHAR(24) NOT NULL, context JSONB NOT NULL, status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/20-social.md
CREATE TABLE game.player_sect (
  sect_id UUID PRIMARY KEY, name VARCHAR(16) NOT NULL UNIQUE, emblem_id VARCHAR(32) NOT NULL,
  level SMALLINT NOT NULL DEFAULT 1, exp INT NOT NULL DEFAULT 0, leader_account_id UUID NOT NULL,
  treasury_stones BIGINT NOT NULL DEFAULT 0 CHECK (treasury_stones >= 0),
  buildings JSONB NOT NULL DEFAULT '{}', upkeep_due_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), disbanded_at TIMESTAMPTZ
);

-- Source: docs/tech/02-database.md
CREATE TABLE game.player_sect_edicts (
  sect_id UUID PRIMARY KEY, edict_ids JSONB NOT NULL DEFAULT '[]', changed_at TIMESTAMPTZ, effective_from TIMESTAMPTZ
);

-- Source: docs/design/20-social.md
CREATE TABLE game.player_sect_ledger (
  id BIGSERIAL PRIMARY KEY, sect_id UUID NOT NULL, delta BIGINT NOT NULL, reason_code VARCHAR(24) NOT NULL,
  account_id UUID, ref_id VARCHAR(80), created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/20-social.md
CREATE TABLE game.player_sect_member (
  account_id UUID PRIMARY KEY,
  sect_id UUID NOT NULL REFERENCES game.player_sect(sect_id), role VARCHAR(16) NOT NULL,
  merit BIGINT NOT NULL DEFAULT 0, joined_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/20-social.md
CREATE UNIQUE INDEX ux_npc_sect_active ON game.npc_sect_membership(character_id) WHERE state = 'ACTIVE';
