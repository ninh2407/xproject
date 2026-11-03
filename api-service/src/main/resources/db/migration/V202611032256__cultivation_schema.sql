-- Consolidated PostgreSQL DDL for the cultivation module, sourced from project specifications.

-- Source: docs/design/10-cultivation.md
CREATE TABLE game.breakthrough_attempt (
  attempt_id      UUID PRIMARY KEY,
  character_id    UUID NOT NULL REFERENCES game.character(id),
  target_realm    VARCHAR(16) NOT NULL,
  from_realm_code VARCHAR(8) NOT NULL,
  state           VARCHAR(16) NOT NULL,
  pill_item_instance_id UUID NOT NULL,
  pill_quality    VARCHAR(8) NOT NULL,
  support_item_ids JSONB NOT NULL DEFAULT '[]',
  guardian_ids    JSONB NOT NULL DEFAULT '[]',
  location        JSONB NOT NULL,
  chance          JSONB NOT NULL,
  heart_trial_session_id UUID,
  roll            NUMERIC(8,6),
  roll_passed     BOOLEAN,
  tribulation     JSONB,
  needs_review    BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  resolved_at     TIMESTAMPTZ
);

-- Source: docs/design/10-cultivation.md
CREATE TABLE game.cultivation_ledger (
  id              BIGSERIAL PRIMARY KEY,
  character_id    UUID NOT NULL REFERENCES game.character(id),
  source          VARCHAR(24) NOT NULL,
  requested       NUMERIC(14,2) NOT NULL,
  applied         NUMERIC(14,2) NOT NULL,
  wasted          NUMERIC(14,2) NOT NULL,
  realm_code_after VARCHAR(8) NOT NULL,
  ref_id          VARCHAR(80),
  idempotency_key VARCHAR(80) NOT NULL UNIQUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/10-cultivation.md
CREATE TABLE game.meditation_report (
  report_id    VARCHAR(64) PRIMARY KEY,
  character_id UUID NOT NULL,
  window_start TIMESTAMPTZ NOT NULL,
  window_end   TIMESTAMPTZ NOT NULL,
  real_sec     INT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/10-cultivation.md
CREATE INDEX ix_cul_ledger_monster_window ON game.cultivation_ledger(character_id, created_at) WHERE source = 'MONSTER';

-- Source: docs/design/10-cultivation.md
CREATE UNIQUE INDEX ux_bt_one_open ON game.breakthrough_attempt(character_id)
  WHERE state IN ('PREPARED', 'CHANNELING', 'HEART_TRIAL', 'ROLLED', 'TRIBULATION');
