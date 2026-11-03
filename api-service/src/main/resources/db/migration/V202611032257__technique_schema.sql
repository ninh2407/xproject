-- Consolidated PostgreSQL DDL for the technique module, sourced from project specifications.

-- Source: docs/design/13-skills.md
CREATE TABLE game.character_loadout (
  character_id UUID NOT NULL REFERENCES game.character(id),
  slot         CHAR(1) NOT NULL,
  skill_ref    VARCHAR(64) NOT NULL,
  PRIMARY KEY (character_id, slot)
);

-- Source: docs/design/13-skills.md
CREATE TABLE game.character_persistent_cooldown (
  character_id UUID NOT NULL REFERENCES game.character(id),
  skill_ref    VARCHAR(64) NOT NULL,
  ready_at     TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (character_id, skill_ref)
);

-- Source: docs/design/13-skills.md
CREATE TABLE game.character_technique (
  character_id   UUID NOT NULL REFERENCES game.character(id),
  ref_id         VARCHAR(64) NOT NULL,
  kind           VARCHAR(8)  NOT NULL,
  mastery_level  SMALLINT NOT NULL DEFAULT 1,
  mastery_points INT NOT NULL DEFAULT 0,
  source         VARCHAR(16) NOT NULL,
  learned_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, ref_id)
);

-- Source: docs/design/14-technique-carfting.md
CREATE TABLE game.crafted_technique (
  id VARCHAR(16) PRIMARY KEY,
  name VARCHAR(32) NOT NULL, name_status VARCHAR(16) NOT NULL DEFAULT 'OK',
  creator_account_id UUID NOT NULL, creator_character_id UUID NOT NULL, creator_name_snapshot VARCHAR(24) NOT NULL,
  tier VARCHAR(8) NOT NULL, sub VARCHAR(8) NOT NULL,
  core_id VARCHAR(32) NOT NULL, element_id VARCHAR(32), modifier_ids JSONB NOT NULL, mutation_modifier_id VARCHAR(32),
  damage_type VARCHAR(8) NOT NULL, q NUMERIC(5,4) NOT NULL, suppress_factor NUMERIC(5,4) NOT NULL,
  skill_def JSONB NOT NULL, power_score NUMERIC(6,4) NOT NULL,
  copies_made INT NOT NULL DEFAULT 1,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/13-skills.md
CREATE TABLE game.mastery_batch (
  batch_id     VARCHAR(80) PRIMARY KEY,
  character_id UUID NOT NULL,
  result       JSONB NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/14-technique-carfting.md
CREATE TABLE game.technique_forge_sandbox_use (
  character_id UUID NOT NULL, day DATE NOT NULL, runs SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, day)
);

-- Source: docs/design/14-technique-carfting.md
CREATE INDEX ix_crafted_creator ON game.crafted_technique(creator_character_id);
