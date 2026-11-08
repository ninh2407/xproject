ALTER TABLE game.account_session
    ADD COLUMN replaced_by_session_id UUID
        REFERENCES game.account_session(id);
