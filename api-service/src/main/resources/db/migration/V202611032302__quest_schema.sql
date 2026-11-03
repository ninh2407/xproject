-- Consolidated PostgreSQL DDL for the quest module, sourced from project specifications.

-- Source: docs/design/21-quests.md
CREATE TABLE game.quest_progress (
  character_id UUID NOT NULL REFERENCES game.character(id),
  quest_id VARCHAR(64) NOT NULL,
  period_key VARCHAR(16) NOT NULL DEFAULT '',
  quest_version INT NOT NULL,
  state VARCHAR(20) NOT NULL,
  step_index SMALLINT NOT NULL DEFAULT 0,
  counters JSONB NOT NULL DEFAULT '{}',
  branch VARCHAR(32),
  tracked BOOLEAN NOT NULL DEFAULT FALSE,
  accepted_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at TIMESTAMPTZ,
  PRIMARY KEY (character_id, quest_id, period_key)
);

-- Source: docs/design/21-quests.md
CREATE TABLE game.quest_reward_log (
  idempotency_key VARCHAR(120) PRIMARY KEY,
  character_id UUID NOT NULL, quest_id VARCHAR(64) NOT NULL, result JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Source: docs/design/21-quests.md
CREATE TABLE game.sect_task_board (
  character_id UUID NOT NULL REFERENCES game.character(id), day DATE NOT NULL,
  slots JSONB NOT NULL, rerolls_used SMALLINT NOT NULL DEFAULT 0,
  PRIMARY KEY (character_id, day)
);

-- Source: docs/design/21-quests.md
CREATE INDEX ix_quest_active ON game.quest_progress(character_id) WHERE state IN ('ACTIVE', 'READY_TO_TURN_IN');
