-- Consolidated PostgreSQL DDL for the character module, sourced from project specifications.

-- Source: docs/tech/02-database.md
CREATE TABLE game.character (
  id UUID PRIMARY KEY,
  account_id UUID NOT NULL REFERENCES game.account(id),
  server_id SMALLINT NOT NULL,
  name VARCHAR(16) NOT NULL,
  generation INT NOT NULL,
  background_id VARCHAR(48) NOT NULL,

  life_state VARCHAR(8) NOT NULL DEFAULT 'ALIVE' CHECK (life_state IN ('ALIVE','ENDING','ENDED')),
  start_age INT NOT NULL DEFAULT 16,
  lifespan_used_sec BIGINT NOT NULL DEFAULT 0,
  innate_lifespan_bonus_years INT NOT NULL DEFAULT 0,
  lifespan_extension_years NUMERIC(8,2) NOT NULL DEFAULT 0,
  last_light_until TIMESTAMPTZ,
  end_cause VARCHAR(24), died_at TIMESTAMPTZ, ending_deadline TIMESTAMPTZ, ended_at TIMESTAMPTZ,

  spirit_root JSONB NOT NULL, innate_stats JSONB NOT NULL, innate_stats_creation JSONB NOT NULL,
  modifiers_version INT NOT NULL DEFAULT 1,

  realm VARCHAR(4) NOT NULL DEFAULT 'PN', realm_stage INT NOT NULL DEFAULT 1, cultivation_exp BIGINT NOT NULL DEFAULT 0,
  pill_toxicity NUMERIC(6,2) NOT NULL DEFAULT 0, toxicity_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  heart_state INT NOT NULL DEFAULT 50,
  breakthrough_failures JSONB NOT NULL DEFAULT '{}', last_breakthrough_fail_at TIMESTAMPTZ,

  main_technique_id VARCHAR(48), main_technique_changed_at TIMESTAMPTZ,

  map_id VARCHAR(24), channel SMALLINT, pos_x NUMERIC(7,2), pos_z NUMERIC(7,2), facing SMALLINT,
  hp INT, mp INT, snapshot_seq BIGINT NOT NULL DEFAULT 0, snapshot_at TIMESTAMPTZ,

  combat_power INT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), last_login_at TIMESTAMPTZ,
  version INT NOT NULL DEFAULT 0
);

-- Source: docs/design/12-stats-formulas.md
CREATE TABLE game.character_status (
  character_id UUID NOT NULL REFERENCES game.character(id),
  status_id    VARCHAR(48) NOT NULL,
  stacks       INT NOT NULL DEFAULT 1,
  expires_at   TIMESTAMPTZ,
  source_ref   VARCHAR(80),
  PRIMARY KEY (character_id, status_id)
);

-- Source: docs/design/03-lifecycle.md
CREATE UNIQUE INDEX ux_character_one_active ON game.character(account_id)
  WHERE life_state IN ('ALIVE', 'ENDING');

-- Source: docs/tech/02-database.md
CREATE UNIQUE INDEX ux_character_alive_per_account ON game.character(account_id, server_id) WHERE life_state IN ('ALIVE','ENDING');

-- Source: docs/tech/02-database.md
CREATE UNIQUE INDEX ux_character_name_alive ON game.character(server_id, lower(name)) WHERE life_state <> 'ENDED';

-- Source: docs/tech/02-database.md
CREATE INDEX ix_character_cp ON game.character(server_id, combat_power DESC) WHERE life_state = 'ALIVE';
