CREATE TABLE IF NOT EXISTS call_sessions (
    id BIGSERIAL PRIMARY KEY,
    caller_user_id BIGINT NOT NULL REFERENCES users(id),
    recipient_user_id BIGINT NOT NULL REFERENCES users(id),
    room_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RINGING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
