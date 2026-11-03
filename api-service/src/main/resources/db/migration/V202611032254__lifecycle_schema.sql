-- Consolidated PostgreSQL DDL for the lifecycle module, sourced from project specifications.

-- Source: docs/design/03-lifecycle.md
CREATE TABLE game.account_meta (
  account_id            UUID PRIMARY KEY,
  rebirth_points        INT NOT NULL DEFAULT 0,
  rebirth_points_total  INT NOT NULL DEFAULT 0,
  generation_count      INT NOT NULL DEFAULT 0,
  unlocks               JSONB NOT NULL DEFAULT '[]',
  vault_slots           INT NOT NULL DEFAULT 3,
  vault_spirit_stones   BIGINT NOT NULL DEFAULT 0,
  first_reach_realms    JSONB NOT NULL DEFAULT '[]'
);

-- Source: docs/design/03-lifecycle.md
CREATE TABLE game.account_vault (
  account_id       UUID NOT NULL,
  slot             INT  NOT NULL,
  item_instance_id UUID NOT NULL UNIQUE,
  PRIMARY KEY (account_id, slot)
);

-- Source: docs/design/03-lifecycle.md
CREATE TABLE game.age_accrual_report (
  report_id       VARCHAR(64) PRIMARY KEY,
  character_id    UUID        NOT NULL REFERENCES game.character(id),
  window_start    TIMESTAMPTZ NOT NULL,
  window_end      TIMESTAMPTZ NOT NULL,
  effective_sec   NUMERIC(12,2) NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/03-lifecycle.md
CREATE TABLE game.legacy_stash (
  account_id UUID PRIMARY KEY, source_character_id UUID NOT NULL, sealed_at TIMESTAMPTZ NOT NULL,
  remnant_snapshot JSONB NOT NULL, bands JSONB NOT NULL
);

-- Source: docs/design/03-lifecycle.md
CREATE TABLE game.life_record (
  record_id     UUID PRIMARY KEY,
  account_id    UUID NOT NULL,
  character_id  UUID NOT NULL UNIQUE,
  summary       JSONB NOT NULL,
  final_realm   VARCHAR(8) NOT NULL,
  age_years     NUMERIC(8,2) NOT NULL,
  end_cause     VARCHAR(32) NOT NULL,
  rebirth_points INT NOT NULL,
  ended_at      TIMESTAMPTZ NOT NULL
);

-- Source: docs/design/03-lifecycle.md
CREATE TABLE game.lifespan_ledger (
  id              BIGSERIAL PRIMARY KEY,
  character_id    UUID        NOT NULL REFERENCES game.character(id),
  kind            VARCHAR(16) NOT NULL,
  delta_years     NUMERIC(10,4) NOT NULL,
  reason_code     VARCHAR(32) NOT NULL,
  ref_id          VARCHAR(64),
  idempotency_key VARCHAR(80) NOT NULL UNIQUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/03-lifecycle.md
CREATE TABLE game.pill_usage (
  character_id UUID NOT NULL REFERENCES game.character(id),
  item_id      VARCHAR(64) NOT NULL,
  uses         INT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, item_id)
);

-- Source: docs/design/03-lifecycle.md
CREATE TABLE game.rebirth_ledger (
  id BIGSERIAL PRIMARY KEY, account_id UUID NOT NULL, delta INT NOT NULL,
  reason_code VARCHAR(32) NOT NULL, ref_id VARCHAR(64), created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/03-lifecycle.md
CREATE TABLE game.severe_injury_log (
  id BIGSERIAL PRIMARY KEY, character_id UUID NOT NULL, map_id VARCHAR(32) NOT NULL,
  cause VARCHAR(32) NOT NULL, lifespan_lost_years NUMERIC(6,2) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/15-items-equipment.md
ALTER TABLE game.account_meta ADD COLUMN bag_expansion INT NOT NULL DEFAULT 0;

-- Source: docs/design/03-lifecycle.md
CREATE INDEX ix_accrual_char_window ON game.age_accrual_report(character_id, window_end);

-- Source: docs/design/03-lifecycle.md
CREATE INDEX ix_life_record_rank_realm ON game.life_record(final_realm, age_years DESC);
