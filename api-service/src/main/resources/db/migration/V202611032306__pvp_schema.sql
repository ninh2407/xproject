-- Consolidated PostgreSQL DDL for the pvp module, sourced from project specifications.

-- Source: docs/tech/02-database.md
CREATE TABLE game.pk_state (
  character_id UUID PRIMARY KEY REFERENCES game.character(id),
  mode VARCHAR(10) NOT NULL DEFAULT 'PEACE' CHECK (mode IN ('PEACE','GUILD','SLAUGHTER')), mode_changed_at TIMESTAMPTZ,
  infamy INT NOT NULL DEFAULT 0 CHECK (infamy >= 0), infamy_peak INT NOT NULL DEFAULT 0,
  grey_until TIMESTAMPTZ, last_pvp_combat_at TIMESTAMPTZ,
  heaven_punishment_field_sec INT,
  feuds JSONB NOT NULL DEFAULT '[]', version INT NOT NULL DEFAULT 0
);

-- Source: docs/tech/02-database.md
CREATE TABLE game.pvp_kill_log (
  id BIGINT GENERATED ALWAYS AS IDENTITY, killer_id UUID NOT NULL, victim_id UUID NOT NULL, map_id VARCHAR(24) NOT NULL,
  victim_mode VARCHAR(10) NOT NULL, victim_tier VARCHAR(8) NOT NULL, infamy_delta INT NOT NULL, dropped JSONB NOT NULL DEFAULT '[]',
  event_id UUID NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (id, created_at), UNIQUE (event_id, created_at)
) PARTITION BY RANGE (created_at);

-- Keeps writes valid until DB-R-040 partition maintenance creates time ranges.
CREATE TABLE game.pvp_kill_log_default PARTITION OF game.pvp_kill_log DEFAULT;

-- Source: docs/tech/02-database.md
CREATE INDEX ix_pvp_kill_pair ON game.pvp_kill_log(killer_id, victim_id, created_at DESC);
