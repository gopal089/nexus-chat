-- V3__create_chat_messages.sql
-- Flyway migration: chat_messages table

CREATE TYPE message_type AS ENUM ('TEXT', 'FILE');

CREATE TABLE chat_messages (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id      UUID         NOT NULL REFERENCES chat_rooms(id) ON DELETE CASCADE,
    sender_id    UUID         NOT NULL REFERENCES users(id)      ON DELETE CASCADE,
    content      TEXT,
    message_type message_type NOT NULL DEFAULT 'TEXT',
    file_url     VARCHAR(500),
    file_name    VARCHAR(255),
    sent_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_room_sent   ON chat_messages(room_id, sent_at DESC);
CREATE INDEX idx_messages_sender      ON chat_messages(sender_id);
