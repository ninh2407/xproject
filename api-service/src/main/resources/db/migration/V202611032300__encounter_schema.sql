-- Consolidated PostgreSQL DDL for the encounter module, sourced from project specifications.

-- Source: docs/design/23-encounters.md
CREATE TABLE game.account_flag (
  account_id UUID NOT NULL, flag VARCHAR(64) NOT NULL, set_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, flag)
);

-- Source: docs/design/23-encounters.md
CREATE TABLE game.character_flag (
  character_id UUID NOT NULL, flag VARCHAR(64) NOT NULL, set_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, flag)
);

-- Source: docs/design/23-encounters.md
CREATE TABLE game.encounter_account_history (
  account_id UUID NOT NULL, encounter_id VARCHAR(64) NOT NULL,
  times INT NOT NULL DEFAULT 0, endings_seen JSONB NOT NULL DEFAULT '[]',
  PRIMARY KEY (account_id, encounter_id)
);

-- Source: docs/design/23-encounters.md
CREATE TABLE game.encounter_chain_progress (
  character_id UUID NOT NULL, chain_id VARCHAR(64) NOT NULL,
  step_id VARCHAR(16) NOT NULL, state VARCHAR(16) NOT NULL,
  step_expires_at TIMESTAMPTZ, updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, chain_id)
);

-- Source: docs/design/23-encounters.md
CREATE TABLE game.encounter_history (
  character_id UUID NOT NULL, encounter_id VARCHAR(64) NOT NULL,
  times INT NOT NULL DEFAULT 0, last_offered_at TIMESTAMPTZ NOT NULL, cooldown_until TIMESTAMPTZ,
  PRIMARY KEY (character_id, encounter_id)
);

-- Source: docs/design/23-encounters.md
CREATE TABLE game.encounter_pity (
  character_id UUID PRIMARY KEY, pity_miss_count INT NOT NULL DEFAULT 0,
  sessions_since_huyen_plus INT NOT NULL DEFAULT 0, updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/23-encounters.md
CREATE TABLE game.encounter_session (
  session_id        UUID PRIMARY KEY,
  character_id      UUID NOT NULL REFERENCES game.character(id),
  encounter_id      VARCHAR(64) NOT NULL,
  encounter_version INT NOT NULL,
  state             VARCHAR(16) NOT NULL,
  trigger_type      VARCHAR(24) NOT NULL,
  trigger_ref       VARCHAR(64),
  map_id            VARCHAR(32) NOT NULL,
  rng_seed          VARCHAR(64) NOT NULL,
  ink_state         JSONB,
  committed_keys    JSONB NOT NULL DEFAULT '[]',
  check_results     JSONB NOT NULL DEFAULT '[]',
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  offered_until     TIMESTAMPTZ NOT NULL,
  resolved_at       TIMESTAMPTZ
);

-- Source: docs/design/23-encounters.md
CREATE TABLE game.legacy_cave (
  id UUID PRIMARY KEY, from_account_id UUID NOT NULL, from_character_id UUID NOT NULL,
  region_id VARCHAR(8) NOT NULL, message TEXT, item_ids JSONB NOT NULL,
  claimed_by UUID, claimed_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/23-encounters.md
CREATE TABLE game.encounter_commit (
  commit_id   VARCHAR(80) PRIMARY KEY,
  session_id  UUID NOT NULL REFERENCES game.encounter_session(session_id),
  outcome_keys JSONB NOT NULL,
  results     JSONB NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/23-encounters.md
CREATE UNIQUE INDEX ux_enc_one_open ON game.encounter_session(character_id) WHERE state IN ('OFFERED', 'ACTIVE');

-- Source: docs/design/23-encounters.md
CREATE INDEX ix_legacy_unclaimed ON game.legacy_cave(region_id) WHERE claimed_by IS NULL;
