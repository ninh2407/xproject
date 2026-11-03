-- Consolidated PostgreSQL DDL for the danger module, sourced from project specifications.

-- Source: docs/design/27-danger-zones.md
CREATE TABLE game.danger_run (
  run_id      UUID PRIMARY KEY,
  dz_id       VARCHAR(16) NOT NULL,
  layout_id   VARCHAR(24),
  instance_id VARCHAR(64),
  party_id    UUID,
  state       VARCHAR(16) NOT NULL,
  started_at  TIMESTAMPTZ, ends_at TIMESTAMPTZ, closed_at TIMESTAMPTZ,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/27-danger-zones.md
CREATE TABLE game.dz_dormant_strike (
  account_id UUID NOT NULL, at TIMESTAMPTZ NOT NULL, run_id UUID NOT NULL,
  PRIMARY KEY (account_id, at)
);

-- Source: docs/design/27-danger-zones.md
CREATE TABLE game.corpse (
  corpse_id    UUID PRIMARY KEY,
  run_id       UUID NOT NULL REFERENCES game.danger_run(run_id),
  character_id UUID NOT NULL,
  position     JSONB NOT NULL,
  expires_at   TIMESTAMPTZ NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/27-danger-zones.md
CREATE TABLE game.danger_run_member (
  run_id        UUID NOT NULL REFERENCES game.danger_run(run_id),
  character_id  UUID NOT NULL REFERENCES game.character(id),
  state         VARCHAR(24) NOT NULL,
  confirmed     BOOLEAN NOT NULL DEFAULT FALSE,
  downed_count  INT NOT NULL DEFAULT 0,
  carried_snapshot JSONB,
  entered_at    TIMESTAMPTZ, resolved_at TIMESTAMPTZ,
  needs_review  BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (run_id, character_id)
);

-- Source: docs/design/27-danger-zones.md
CREATE UNIQUE INDEX ux_dz_member_open ON game.danger_run_member(character_id)
  WHERE state IN ('IN', 'DOWNED', 'DORMANT_GUARD');
