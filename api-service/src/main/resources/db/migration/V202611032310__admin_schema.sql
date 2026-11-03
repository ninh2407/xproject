-- Consolidated PostgreSQL DDL for the admin module, sourced from project specifications.

-- Source: docs/tech/02-database.md
CREATE TABLE game.admin_audit (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, actor_account_id UUID NOT NULL, action VARCHAR(64) NOT NULL,
  target_type VARCHAR(32), target_id VARCHAR(80), before JSONB, after JSONB, reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/tech/02-database.md
CREATE TABLE game.fortune_dispute (
  id UUID PRIMARY KEY, account_id UUID NOT NULL, character_id UUID NOT NULL, ref_id VARCHAR(80) NOT NULL,
  note TEXT, status VARCHAR(12) NOT NULL DEFAULT 'OPEN', gm_note TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), resolved_at TIMESTAMPTZ
);
