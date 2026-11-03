CREATE SCHEMA IF NOT EXISTS game;

CREATE TABLE game.account (
    id UUID PRIMARY KEY,
    login_email VARCHAR(254) NOT NULL UNIQUE CHECK (login_email = lower(login_email)),
    password_hash VARCHAR(255) NOT NULL,
    display_handle VARCHAR(24) UNIQUE,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED', 'DELETED')),
    suspended_until TIMESTAMPTZ,
    locale VARCHAR(8) NOT NULL DEFAULT 'vi',
    settings JSONB NOT NULL DEFAULT '{}'::jsonb,
    roles JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

CREATE TABLE game.account_session (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES game.account(id),
    refresh_token_hash BYTEA NOT NULL UNIQUE,
    device VARCHAR(128),
    ip INET,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX ix_account_session_account
    ON game.account_session(account_id)
    WHERE revoked_at IS NULL;

CREATE TABLE game.idempotency_record (
    key VARCHAR(80) PRIMARY KEY,
    endpoint VARCHAR(96) NOT NULL,
    request_hash BYTEA NOT NULL,
    status VARCHAR(12) NOT NULL CHECK (status IN ('IN_PROGRESS', 'DONE')),
    http_status SMALLINT,
    response JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_idempotency_record_created_at
    ON game.idempotency_record(created_at);
