-- Consolidated PostgreSQL DDL for the craft module, sourced from project specifications.

-- Source: docs/design/16-alchemy.md
CREATE TABLE game.craft_session (
  session_id UUID PRIMARY KEY,
  character_id UUID NOT NULL REFERENCES game.character(id),
  recipe_id VARCHAR(48) NOT NULL,
  mode VARCHAR(16) NOT NULL, batches SMALLINT NOT NULL,
  furnace_instance_id UUID NOT NULL, fire_instance_id UUID,
  seed VARCHAR(64) NOT NULL,
  state VARCHAR(16) NOT NULL,
  events JSONB, score NUMERIC(6,4), result JSONB,
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(), ends_at TIMESTAMPTZ NOT NULL, completed_at TIMESTAMPTZ
);

-- Source: docs/design/17-forging.md
CREATE TABLE game.forge_action_log (
  request_id VARCHAR(80) PRIMARY KEY,
  character_id UUID NOT NULL, item_instance_id UUID, action VARCHAR(16) NOT NULL,
  input JSONB NOT NULL, result JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/17-forging.md
CREATE TABLE game.forge_item_state (
  item_instance_id UUID PRIMARY KEY REFERENCES game.item_instance(id),
  enhance_pity SMALLINT NOT NULL DEFAULT 0,
  upgrade_pity SMALLINT NOT NULL DEFAULT 0,
  enhance_stones_invested JSONB NOT NULL DEFAULT '{}'
);

-- Source: docs/design/17-forging.md
CREATE TABLE game.forge_pending_reroll (
  pending_id UUID PRIMARY KEY,
  item_instance_id UUID NOT NULL UNIQUE REFERENCES game.item_instance(id),
  character_id UUID NOT NULL,
  mode VARCHAR(8) NOT NULL, line_index SMALLINT,
  old_affixes JSONB NOT NULL, new_affixes JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), expires_at TIMESTAMPTZ NOT NULL
);

-- Source: docs/design/16-alchemy.md
CREATE TABLE game.profession (
  character_id UUID NOT NULL REFERENCES game.character(id),
  kind VARCHAR(16) NOT NULL,
  level SMALLINT NOT NULL DEFAULT 1,
  exp INT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, kind)
);

-- Source: docs/design/16-alchemy.md
CREATE TABLE game.profession_memory (
  account_id UUID NOT NULL, kind VARCHAR(16) NOT NULL,
  max_level SMALLINT NOT NULL, recipe_ids JSONB NOT NULL, updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, kind)
);

-- Source: docs/design/16-alchemy.md
CREATE TABLE game.profession_trial (
  id UUID PRIMARY KEY, character_id UUID NOT NULL, kind VARCHAR(16) NOT NULL, tier VARCHAR(8) NOT NULL,
  session_id UUID NOT NULL, score NUMERIC(5,4), passed BOOLEAN, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/16-alchemy.md
CREATE TABLE game.recipe_known (
  character_id UUID NOT NULL REFERENCES game.character(id),
  recipe_id VARCHAR(48) NOT NULL,
  crafts INT NOT NULL DEFAULT 0,
  pity_fails INT NOT NULL DEFAULT 0,
  source VARCHAR(16) NOT NULL,
  learned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (character_id, recipe_id)
);

-- Source: docs/design/16-alchemy.md
CREATE UNIQUE INDEX ux_craft_one_running ON game.craft_session(character_id) WHERE state = 'RUNNING';
