-- Add the nullable SHA-256 device fingerprint column specified by TD-03.

ALTER TABLE game.account_session ADD COLUMN device_hash BYTEA;
