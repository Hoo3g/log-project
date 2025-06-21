CREATE TABLE storage_event (
    id              VARCHAR(64) PRIMARY KEY,
    target_type     VARCHAR(64) NOT NULL,
    target_id       VARCHAR(64) NOT NULL,
    subject_type    VARCHAR(64) NOT NULL,
    subject_id      VARCHAR(64) NOT NULL,
    action          VARCHAR(64) NOT NULL,
    data            JSONB,
    correlation_id  VARCHAR(64),
    created_at      BIGINT  NOT NULL
);