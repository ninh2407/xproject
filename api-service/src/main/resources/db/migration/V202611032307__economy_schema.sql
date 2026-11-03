-- Consolidated PostgreSQL DDL for the economy module, sourced from project specifications.

-- Source: docs/design/19-economy.md
CREATE TABLE game.auction_listing (
  listing_id UUID PRIMARY KEY, seller_character_id UUID NOT NULL, item_instance_id UUID NOT NULL UNIQUE,
  template_id VARCHAR(64) NOT NULL, qty INT NOT NULL, unit_price BIGINT NOT NULL, deposit BIGINT NOT NULL,
  state VARCHAR(16) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), expires_at TIMESTAMPTZ NOT NULL,
  buyer_character_id UUID, sold_at TIMESTAMPTZ
);

-- Source: docs/design/19-economy.md
CREATE TABLE game.economy_alert (
  id BIGSERIAL PRIMARY KEY, kind VARCHAR(48) NOT NULL, subject VARCHAR(80), value NUMERIC NOT NULL,
  threshold NUMERIC NOT NULL, status VARCHAR(16) NOT NULL DEFAULT 'OPEN', created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/19-economy.md
CREATE TABLE game.economy_snapshot (
  day DATE PRIMARY KEY, data JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/19-economy.md
CREATE TABLE game.shop_purchase_limit (
  character_id UUID NOT NULL, shop_id VARCHAR(32) NOT NULL, template_id VARCHAR(64) NOT NULL,
  day DATE NOT NULL, count INT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, shop_id, template_id, day)
);

-- Source: docs/design/19-economy.md
CREATE TABLE game.trade_daily_value (
  character_id UUID NOT NULL, day DATE NOT NULL, received_value BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, day)
);

-- Source: docs/design/19-economy.md
CREATE TABLE game.trade_session (
  trade_id UUID PRIMARY KEY, a_character_id UUID NOT NULL, b_character_id UUID NOT NULL,
  a_offer JSONB NOT NULL, b_offer JSONB NOT NULL, state VARCHAR(16) NOT NULL,
  locked_at TIMESTAMPTZ, completed_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/19-economy.md
CREATE INDEX ix_auction_search ON game.auction_listing(template_id, unit_price) WHERE state = 'ACTIVE';
