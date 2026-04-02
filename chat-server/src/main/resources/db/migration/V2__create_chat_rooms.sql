-- V2__create_chat_rooms.sql
-- Flyway migration: chat_rooms table

CREATE TABLE chat_rooms (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_rooms_name ON chat_rooms(name);
