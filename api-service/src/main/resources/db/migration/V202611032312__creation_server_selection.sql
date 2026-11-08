DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM game.character)
       OR EXISTS (SELECT 1 FROM game.creation_attempt)
       OR EXISTS (SELECT 1 FROM game.account_meta WHERE generation_count <> 0) THEN
        RAISE EXCEPTION
            'Server-selection migration requires empty character/creation data; map existing generations before retrying';
    END IF;
END
$$;

CREATE TABLE game.server_registry (
    server_id SMALLINT PRIMARY KEY,
    name_key VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL
        CHECK (status IN ('OPEN', 'MAINTENANCE', 'CLOSED'))
);

CREATE TABLE game.account_server_meta (
    account_id UUID NOT NULL REFERENCES game.account(id),
    server_id SMALLINT NOT NULL REFERENCES game.server_registry(server_id),
    generation_count INT NOT NULL DEFAULT 0 CHECK (generation_count >= 0),
    PRIMARY KEY (account_id, server_id)
);

ALTER TABLE game.character
    ALTER COLUMN name TYPE VARCHAR(12),
    ADD CONSTRAINT ck_character_name_length CHECK (char_length(name) BETWEEN 2 AND 12),
    ADD COLUMN base_appearance JSONB NOT NULL;

ALTER TABLE game.character
    ADD CONSTRAINT fk_character_server
    FOREIGN KEY (server_id) REFERENCES game.server_registry(server_id);

ALTER TABLE game.creation_attempt
    ADD COLUMN server_id SMALLINT NOT NULL
    REFERENCES game.server_registry(server_id);

DROP INDEX game.ux_attempt_open;
CREATE UNIQUE INDEX ux_attempt_open
    ON game.creation_attempt(account_id, server_id)
    WHERE confirmed_roll_id IS NULL;

ALTER TABLE game.account_meta DROP COLUMN generation_count;
