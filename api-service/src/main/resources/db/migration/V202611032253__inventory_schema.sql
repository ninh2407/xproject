-- Consolidated PostgreSQL DDL for the inventory module, sourced from project specifications.

-- Source: docs/design/15-items-equipment.md
CREATE TABLE game.character_quick_slot (
  character_id UUID NOT NULL, slot SMALLINT NOT NULL, template_id VARCHAR(64),
  PRIMARY KEY (character_id, slot)
);

-- Source: docs/design/15-items-equipment.md
CREATE TABLE game.currency_ledger (
  id BIGSERIAL PRIMARY KEY, owner_type VARCHAR(16) NOT NULL, owner_id UUID NOT NULL, currency VARCHAR(24) NOT NULL,
  delta BIGINT NOT NULL, balance_after BIGINT NOT NULL, reason_code VARCHAR(32) NOT NULL, ref_id VARCHAR(80),
  idempotency_key VARCHAR(80) NOT NULL UNIQUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/15-items-equipment.md
CREATE TABLE game.currency_wallet (
  owner_type VARCHAR(16) NOT NULL, owner_id UUID NOT NULL, currency VARCHAR(24) NOT NULL,
  balance BIGINT NOT NULL CHECK (balance >= 0),
  PRIMARY KEY (owner_type, owner_id, currency)
);

-- Source: docs/design/15-items-equipment.md
CREATE TABLE game.item_instance (
  id                  UUID PRIMARY KEY,
  template_id         VARCHAR(64) NOT NULL,
  qty                 INT NOT NULL CHECK (qty > 0),
  location            VARCHAR(16) NOT NULL,
  owner_character_id  UUID,
  owner_account_id    UUID NOT NULL,
  slot_index          INT,
  equip_slot          VARCHAR(16),
  bound               BOOLEAN NOT NULL DEFAULT FALSE,
  grade               VARCHAR(8),
  quality             VARCHAR(8),
  affixes             JSONB NOT NULL DEFAULT '[]',
  enhance_level       SMALLINT NOT NULL DEFAULT 0,
  seal_band           VARCHAR(4),
  sockets             JSONB NOT NULL DEFAULT '[]',
  origin_character_id UUID,
  danger_run_id       UUID,
  corpse_id           UUID,
  legacy_id           UUID,
  mail_id             UUID,
  crafter_character_id UUID,
  created_reason      VARCHAR(32) NOT NULL,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  version             INT NOT NULL DEFAULT 1
);

-- Source: docs/design/15-items-equipment.md
CREATE TABLE game.item_log (
  id              BIGSERIAL PRIMARY KEY,
  item_instance_id UUID NOT NULL,
  template_id     VARCHAR(64) NOT NULL,
  qty_delta       INT NOT NULL,
  from_location   VARCHAR(16), to_location VARCHAR(16),
  from_owner      UUID, to_owner UUID,
  reason_code     VARCHAR(32) NOT NULL,
  ref_id          VARCHAR(80),
  idempotency_key VARCHAR(80) NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/15-items-equipment.md
CREATE TABLE game.mail (
  mail_id UUID PRIMARY KEY, character_id UUID NOT NULL, sender_kind VARCHAR(16) NOT NULL,
  title_key VARCHAR(64), body TEXT, expires_at TIMESTAMPTZ NOT NULL, claimed_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/14-technique-carfting.md
ALTER TABLE game.item_instance ADD COLUMN crafted_ref_id VARCHAR(16);

-- Source: docs/design/17-forging.md
ALTER TABLE game.item_instance ADD COLUMN spirit_born BOOLEAN NOT NULL DEFAULT FALSE;

-- Source: docs/design/15-items-equipment.md
CREATE INDEX ix_item_owner_loc ON game.item_instance(owner_character_id, location) WHERE location <> 'DESTROYED';

-- Source: docs/design/15-items-equipment.md
CREATE INDEX ix_item_account_vault ON game.item_instance(owner_account_id) WHERE location = 'VAULT';

-- Source: docs/design/15-items-equipment.md
CREATE UNIQUE INDEX ux_item_equip_slot ON game.item_instance(owner_character_id, equip_slot) WHERE location = 'EQUIP';

-- Source: docs/design/15-items-equipment.md
CREATE INDEX ix_item_log_item ON game.item_log(item_instance_id);

-- Source: docs/design/15-items-equipment.md
CREATE UNIQUE INDEX ux_item_log_idem ON game.item_log(idempotency_key, item_instance_id);
