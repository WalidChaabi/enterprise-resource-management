-- V9__create_notifications_schema.sql
-- Notifications table for user alerts

SET search_path TO erm, public;

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    time TIMESTAMP NOT NULL DEFAULT now(),
    unread BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id
    ON notifications (user_id);

CREATE INDEX idx_notifications_time
    ON notifications (time);

CREATE INDEX idx_notifications_unread
    ON notifications (unread);
