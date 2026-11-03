-- Consolidated PostgreSQL DDL for the beast module, sourced from project specifications.

-- Source: docs/design/26-spirit-beasts.md
CREATE TABLE game.beast (
  id UUID PRIMARY KEY, account_id UUID NOT NULL, species_id VARCHAR(48) NOT NULL, name VARCHAR(24) NOT NULL,
  bloodline VARCHAR(8) NOT NULL, aptitudes JSONB NOT NULL, trait_ids JSONB NOT NULL, mutations JSONB NOT NULL DEFAULT '[]',
  color_variant VARCHAR(32), age_years NUMERIC(7,2) NOT NULL DEFAULT 0, lifespan_max_years INT NOT NULL,
  bond SMALLINT NOT NULL DEFAULT 1, breed_count SMALLINT NOT NULL DEFAULT 0, generation SMALLINT NOT NULL DEFAULT 1,
  parent_a UUID, parent_b UUID, state VARCHAR(12) NOT NULL, contract_character_id UUID, injured_until TIMESTAMPTZ,
  origin VARCHAR(12) NOT NULL, born_at TIMESTAMPTZ NOT NULL DEFAULT now(), departed_at TIMESTAMPTZ, departed_reason VARCHAR(16),
  version INT NOT NULL DEFAULT 0
);

-- Source: docs/design/26-spirit-beasts.md
CREATE TABLE game.beast_boss_capture (
  account_id UUID NOT NULL, boss_monster_id VARCHAR(48) NOT NULL,
  fails SMALLINT NOT NULL DEFAULT 0, attempts_day DATE, attempts_used SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (account_id, boss_monster_id)
);

-- Source: docs/design/26-spirit-beasts.md
CREATE TABLE game.beast_breeding (
  id UUID PRIMARY KEY, account_id UUID NOT NULL, parent_a UUID NOT NULL, parent_b UUID NOT NULL,
  quote JSONB NOT NULL, result JSONB NOT NULL,
  started_at TIMESTAMPTZ NOT NULL, ready_at TIMESTAMPTZ NOT NULL, state VARCHAR(12) NOT NULL, egg_item_instance_id UUID
);

-- Source: docs/design/26-spirit-beasts.md
CREATE TABLE game.beast_capture_pity (
  character_id UUID NOT NULL, species_id VARCHAR(48) NOT NULL, fails SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, species_id)
);

-- Source: docs/design/26-spirit-beasts.md
CREATE TABLE game.beast_log (
  id BIGSERIAL PRIMARY KEY, beast_id UUID NOT NULL, account_id UUID NOT NULL, reason_code VARCHAR(32) NOT NULL,
  detail JSONB, ref_id VARCHAR(80), idempotency_key VARCHAR(80) NOT NULL UNIQUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/26-spirit-beasts.md
CREATE TABLE game.beast_pity (
  account_id UUID PRIMARY KEY, breed_no_mutation SMALLINT NOT NULL DEFAULT 0
);

-- Source: docs/design/26-spirit-beasts.md
CREATE INDEX ix_beast_account ON game.beast(account_id, state);

-- Source: docs/design/26-spirit-beasts.md
CREATE INDEX ix_beast_contract ON game.beast(contract_character_id) WHERE contract_character_id IS NOT NULL;
