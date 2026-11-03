-- Consolidated PostgreSQL DDL for the platform module, sourced from project specifications.

-- Source: docs/tech/02-database.md
CREATE TABLE game.consumed_event (consumer VARCHAR(48) NOT NULL, event_id UUID NOT NULL, consumed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (consumer, event_id));

-- Source: docs/tech/02-database.md
CREATE TABLE game.death_log (
  id UUID PRIMARY KEY, character_id UUID NOT NULL, map_id VARCHAR(24) NOT NULL, cause VARCHAR(24) NOT NULL,
  payload BYTEA NOT NULL, size_bytes INT NOT NULL, needs_review BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/tech/02-database.md
CREATE TABLE game.outbox_event (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, event_id UUID NOT NULL UNIQUE,
  type VARCHAR(64) NOT NULL, version SMALLINT NOT NULL, stream VARCHAR(32) NOT NULL,
  account_id UUID, character_id UUID, payload JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), published_at TIMESTAMPTZ
);

-- Source: docs/tech/00-tdd.md
CREATE TABLE game.rng_audit (
  id              BIGSERIAL,
  kind            VARCHAR(40) NOT NULL,
  source          VARCHAR(16) NOT NULL,
  account_id      UUID, character_id UUID,
  ref_id          VARCHAR(80),
  seed            BYTEA NOT NULL,
  gamedata_version VARCHAR(40) NOT NULL,
  inputs          JSONB NOT NULL,
  outcome         JSONB NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- Keeps writes valid until DB-R-040 partition maintenance creates time ranges.
CREATE TABLE game.rng_audit_default PARTITION OF game.rng_audit DEFAULT;

-- Source: docs/tech/00-tdd.md
CREATE INDEX ix_rng_char ON game.rng_audit(character_id, created_at);

-- Source: docs/tech/00-tdd.md
CREATE INDEX ix_rng_ref ON game.rng_audit(ref_id);

-- Source: docs/tech/02-database.md
CREATE INDEX ix_outbox_unpublished ON game.outbox_event(id) WHERE published_at IS NULL;
