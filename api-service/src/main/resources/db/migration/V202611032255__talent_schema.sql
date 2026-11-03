-- Consolidated PostgreSQL DDL for the talent module, sourced from project specifications.

-- Source: docs/design/04-talents.md
CREATE TABLE game.account_talent_seen (
  account_id UUID NOT NULL, talent_id VARCHAR(48) NOT NULL, first_seen_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, talent_id)
);

-- Source: docs/design/04-talents.md
CREATE TABLE game.character_talent (
  character_id UUID NOT NULL REFERENCES game.character(id),
  talent_id    VARCHAR(48) NOT NULL,
  source       VARCHAR(16) NOT NULL,
  slot         INT NOT NULL,
  revealed     BOOLEAN NOT NULL DEFAULT TRUE,
  ref_id       VARCHAR(64),
  acquired_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, talent_id)
);

-- Source: docs/design/04-talents.md
CREATE TABLE game.creation_attempt (
  attempt_id      UUID PRIMARY KEY,
  account_id      UUID NOT NULL,
  generation      INT  NOT NULL,
  rolls_allowed   INT  NOT NULL,
  rolls_used      INT  NOT NULL DEFAULT 0,
  lock_available  BOOLEAN NOT NULL,
  no_rare_plus_streak INT NOT NULL DEFAULT 0,
  confirmed_roll_id UUID,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/04-talents.md
CREATE TABLE game.talent_change_log (
  change_id    VARCHAR(80) PRIMARY KEY,
  character_id UUID NOT NULL,
  op           VARCHAR(24) NOT NULL,
  params       JSONB NOT NULL,
  source       VARCHAR(16) NOT NULL,
  ref_id       VARCHAR(64),
  result       JSONB NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/04-talents.md
CREATE TABLE game.talent_pending_choice (
  pending_id   UUID PRIMARY KEY,
  character_id UUID NOT NULL REFERENCES game.character(id),
  kind         VARCHAR(16) NOT NULL,
  offers       JSONB NOT NULL,
  context      JSONB NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  resolved_at  TIMESTAMPTZ,
  resolution   JSONB
);

-- Source: docs/design/04-talents.md
CREATE TABLE game.creation_roll (
  roll_id     UUID PRIMARY KEY,
  attempt_id  UUID NOT NULL REFERENCES game.creation_attempt(attempt_id),
  idx         INT  NOT NULL,
  background_id VARCHAR(32) NOT NULL,
  locked_line VARCHAR(16),
  seed        VARCHAR(64) NOT NULL,
  result      JSONB NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (attempt_id, idx)
);

-- Source: docs/design/04-talents.md
CREATE UNIQUE INDEX ux_attempt_open ON game.creation_attempt(account_id) WHERE confirmed_roll_id IS NULL;

-- Source: docs/design/04-talents.md
CREATE UNIQUE INDEX ux_one_awaken_pending ON game.talent_pending_choice(character_id)
  WHERE kind = 'AWAKEN' AND resolved_at IS NULL;
