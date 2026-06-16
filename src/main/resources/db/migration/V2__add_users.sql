-- V2: add users table and link resumes to their owner.
--
-- password_hash is nullable so OAuth-only users (no local password) are valid rows.
-- A future V3 migration can add provider/provider_id columns (or a separate
-- oauth_identities table) without touching this schema.
--
-- user_id on resumes is nullable so existing ownerless rows survive the migration.
-- Ownerless resumes become inaccessible via the API (every query is filtered by user)
-- but are not deleted or corrupted. Run "docker compose down -v" to start fresh.

CREATE TABLE users (
    id            BIGSERIAL     PRIMARY KEY,
    email         VARCHAR(255)  NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    display_name  VARCHAR(255),
    created_at    TIMESTAMP     NOT NULL DEFAULT now()
);

ALTER TABLE resumes
    ADD COLUMN user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;
