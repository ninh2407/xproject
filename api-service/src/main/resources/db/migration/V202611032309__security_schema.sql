-- Consolidated PostgreSQL DDL for the security module, sourced from project specifications.

-- Source: docs/tech/07-security.md
CREATE TABLE game.login_attempt (
  id BIGSERIAL PRIMARY KEY,
  account_id UUID REFERENCES game.account(id),
  ip INET NOT NULL,
  succeeded BOOLEAN NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/tech/07-security.md
CREATE TABLE game.purchase_receipt (
  transaction_id VARCHAR(128) PRIMARY KEY,
  store VARCHAR(16) NOT NULL,
  product_id VARCHAR(128) NOT NULL,
  account_id UUID NOT NULL REFERENCES game.account(id),
  status VARCHAR(16) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  verified_at TIMESTAMPTZ,
  refunded_at TIMESTAMPTZ
);

-- Source: docs/tech/07-security.md
CREATE TABLE game.sanction (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES game.account(id),
  level SMALLINT NOT NULL CHECK (level BETWEEN 1 AND 4),
  kind VARCHAR(32) NOT NULL,
  reason TEXT NOT NULL,
  evidence_flag_ids JSONB NOT NULL DEFAULT '[]',
  actor_id UUID NOT NULL,
  approved_by UUID,
  starts_at TIMESTAMPTZ NOT NULL,
  ends_at TIMESTAMPTZ,
  revoked_at TIMESTAMPTZ,
  revoked_by UUID,
  revoke_reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (ends_at IS NULL OR ends_at > starts_at),
  CHECK (level NOT IN (2, 3) OR
         (ends_at IS NOT NULL AND ends_at >= starts_at + INTERVAL '1 day')),
  CHECK (level <> 2 OR ends_at <= starts_at + INTERVAL '7 days'),
  CHECK (level <> 3 OR ends_at <= starts_at + INTERVAL '30 days'),
  CHECK (level <> 4 OR
         (ends_at IS NULL AND approved_by IS NOT NULL AND approved_by <> actor_id))
);

-- Source: docs/tech/07-security.md
CREATE TABLE game.security_flag (
  id UUID PRIMARY KEY, account_id UUID NOT NULL, character_id UUID,
  kind VARCHAR(32) NOT NULL,
  score INT NOT NULL, evidence JSONB NOT NULL,
  status VARCHAR(12) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','REVIEWING','CONFIRMED','DISMISSED')),
  reviewer_id UUID, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), resolved_at TIMESTAMPTZ
);

-- Source: docs/tech/07-security.md
CREATE TABLE game.appeal (
  id UUID PRIMARY KEY,
  sanction_id UUID NOT NULL REFERENCES game.sanction(id),
  account_id UUID NOT NULL REFERENCES game.account(id),
  body TEXT NOT NULL,
  reviewer_id UUID,
  response TEXT,
  upheld BOOLEAN,
  submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  resolved_at TIMESTAMPTZ
);

-- Source: docs/tech/07-security.md
CREATE INDEX ix_login_attempt_account_time ON game.login_attempt(account_id, created_at DESC);

-- Source: docs/tech/07-security.md
CREATE INDEX ix_login_attempt_ip_time ON game.login_attempt(ip, created_at DESC);

-- Source: docs/tech/07-security.md
CREATE INDEX ix_security_flag_open ON game.security_flag(status, score DESC) WHERE status IN ('OPEN','REVIEWING');

-- Source: docs/tech/07-security.md
CREATE INDEX ix_sanction_account_time ON game.sanction(account_id, created_at DESC);

-- Source: docs/tech/07-security.md
CREATE INDEX ix_appeal_sanction_time ON game.appeal(sanction_id, submitted_at DESC);

-- Source: docs/tech/07-security.md
CREATE INDEX ix_appeal_pending ON game.appeal(submitted_at) WHERE resolved_at IS NULL;
