-- Apply after 20261110_create_notification_outbox.sql, before using updated Outbox code.
ALTER TABLE notification_outbox
    DROP CHECK chk_outbox_status,
    ADD COLUMN claim_token BINARY(16) NULL,
    ADD COLUMN lease_until TIMESTAMP(6) NULL,
    ADD COLUMN sent_at TIMESTAMP(6) NULL,
    ADD CONSTRAINT chk_outbox_status CHECK (status IN ('PENDING','PROCESSING','RETRY','SENT','FAILED','SKIPPED')),
    ADD INDEX ix_outbox_ready (status, next_attempt_at, id),
    ADD INDEX ix_outbox_lease (status, lease_until, id);
