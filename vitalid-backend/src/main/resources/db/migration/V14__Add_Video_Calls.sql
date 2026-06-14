CREATE TABLE IF NOT EXISTS video_calls (
    id BIGSERIAL PRIMARY KEY,
    caller_user_id BIGINT NOT NULL REFERENCES users(id),
    recipient_user_id BIGINT NOT NULL REFERENCES users(id),
    room_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RINGING',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_video_calls_recipient_status
    ON video_calls(recipient_user_id, status);
