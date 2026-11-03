-- Consolidated PostgreSQL DDL for the monetization module, sourced from project specifications.

-- Source: docs/design/50-monetization.md
CREATE TABLE game.character_appearance (
  character_id UUID PRIMARY KEY, slots JSONB NOT NULL DEFAULT '{}', updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/50-monetization.md
CREATE TABLE game.cosmetic_entitlement (
  account_id UUID NOT NULL, cosmetic_id VARCHAR(48) NOT NULL, source VARCHAR(48) NOT NULL,
  granted_at TIMESTAMPTZ NOT NULL DEFAULT now(), revoked_at TIMESTAMPTZ,
  PRIMARY KEY (account_id, cosmetic_id)
);

-- Source: docs/design/50-monetization.md
CREATE TABLE game.gift_log (id BIGSERIAL PRIMARY KEY, from_account UUID NOT NULL, to_account UUID NOT NULL, offer_id VARCHAR(32) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now());

-- Source: docs/design/50-monetization.md
CREATE TABLE game.payment_order (
  id UUID PRIMARY KEY, account_id UUID NOT NULL, pack_id VARCHAR(32) NOT NULL, provider VARCHAR(24) NOT NULL,
  amount_vnd BIGINT NOT NULL, status VARCHAR(12) NOT NULL, provider_ref VARCHAR(128),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), paid_at TIMESTAMPTZ
);

-- Source: docs/design/50-monetization.md
CREATE TABLE game.season_pass_progress (
  account_id UUID NOT NULL, season_id VARCHAR(16) NOT NULL, premium BOOLEAN NOT NULL DEFAULT FALSE,
  xp INT NOT NULL DEFAULT 0, claimed JSONB NOT NULL DEFAULT '[]',
  PRIMARY KEY (account_id, season_id)
);

-- Source: docs/design/50-monetization.md
CREATE TABLE game.spend_limit (account_id UUID PRIMARY KEY, monthly_cap_vnd BIGINT, is_minor BOOLEAN NOT NULL DEFAULT FALSE, updated_at TIMESTAMPTZ);
