-- Consolidated PostgreSQL DDL for the home module, sourced from project specifications.

-- Source: docs/design/28-caves-sect-sites.md
CREATE TABLE game.cave (
  account_id UUID PRIMARY KEY,
  gate_poi_id VARCHAR(48) NOT NULL,
  buildings JSONB NOT NULL DEFAULT '{}',
  visit_policy VARCHAR(8) NOT NULL DEFAULT 'FRIENDS',
  decorations JSONB NOT NULL DEFAULT '[]',
  claimed_at TIMESTAMPTZ NOT NULL DEFAULT now(), relocated_at TIMESTAMPTZ,
  archived BOOLEAN NOT NULL DEFAULT FALSE
);

-- Source: docs/design/28-caves-sect-sites.md
CREATE TABLE game.sect_prestige_member_week (
  sect_id UUID NOT NULL, account_id UUID NOT NULL, week_start DATE NOT NULL, source VARCHAR(24) NOT NULL, points INT NOT NULL,
  PRIMARY KEY (sect_id, account_id, week_start, source)
);

-- Source: docs/design/28-caves-sect-sites.md
CREATE TABLE game.sect_prestige_week (
  sect_id UUID NOT NULL, week_start DATE NOT NULL, points INT NOT NULL DEFAULT 0, breakdown JSONB NOT NULL DEFAULT '{}',
  PRIMARY KEY (sect_id, week_start)
);

-- Source: docs/design/28-caves-sect-sites.md
CREATE TABLE game.sect_site (
  site_id VARCHAR(32) PRIMARY KEY, region_id VARCHAR(8) NOT NULL, map_id VARCHAR(32) NOT NULL,
  grade CHAR(1) NOT NULL, state VARCHAR(8) NOT NULL DEFAULT 'VACANT',
  sect_id UUID REFERENCES game.player_sect(sect_id), held_since TIMESTAMPTZ,
  upkeep_due_at TIMESTAMPTZ, unpaid_weeks SMALLINT NOT NULL DEFAULT 0,
  ruins_until TIMESTAMPTZ, ruins_former_sect_level SMALLINT, ruins_former_sect_name VARCHAR(16)
);

-- Source: docs/design/28-caves-sect-sites.md
CREATE TABLE game.site_history (
  id BIGSERIAL PRIMARY KEY, site_id VARCHAR(32) NOT NULL, sect_id UUID NOT NULL, sect_name VARCHAR(16) NOT NULL,
  held_from TIMESTAMPTZ NOT NULL, held_to TIMESTAMPTZ, end_reason VARCHAR(16)
);

-- Source: docs/design/28-caves-sect-sites.md
CREATE TABLE game.cave_plot (
  account_id UUID NOT NULL REFERENCES game.cave(account_id), plot_index SMALLINT NOT NULL,
  seed_template_id VARCHAR(64), planted_at TIMESTAMPTZ, ready_at TIMESTAMPTZ,
  PRIMARY KEY (account_id, plot_index)
);

-- Source: docs/design/28-caves-sect-sites.md
CREATE INDEX ix_cave_gate ON game.cave(gate_poi_id) WHERE archived = FALSE;

-- Source: docs/design/28-caves-sect-sites.md
CREATE UNIQUE INDEX ux_site_one_per_sect ON game.sect_site(sect_id) WHERE sect_id IS NOT NULL;
