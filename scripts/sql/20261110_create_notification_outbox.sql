-- MySQL 8.0. Apply explicitly before deploying code that writes Outbox tasks.
-- No production database is modified by adding this file.
CREATE TABLE notification_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    application_uuid BINARY(16) NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    recipient_profile_id BIGINT NOT NULL,
    recipient_token TEXT NULL,
    result VARCHAR(10) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    next_attempt_at TIMESTAMP(6) NULL,
    last_error TEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_outbox_application_event UNIQUE (application_uuid, event_type),
    CONSTRAINT chk_outbox_result CHECK (result IN ('PASS', 'FAIL')),
    CONSTRAINT chk_outbox_event CHECK (event_type = 'APPLICATION_RESULT'),
    CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING', 'SKIPPED')),
    CONSTRAINT chk_outbox_attempt_count CHECK (attempt_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
