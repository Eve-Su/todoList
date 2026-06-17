-- ai-platform-api owns schema agent_bus.
-- Tables in this migration are used by the chat/session main path.

CREATE SCHEMA IF NOT EXISTS agent_bus;

CREATE TABLE IF NOT EXISTS agent_bus.agent_session (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    agent_code VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    summary TEXT,
    last_message_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_agent_session_user_id ON agent_bus.agent_session(user_id);
CREATE INDEX IF NOT EXISTS idx_agent_session_agent_code ON agent_bus.agent_session(agent_code);
CREATE INDEX IF NOT EXISTS idx_agent_session_last_message_at ON agent_bus.agent_session(last_message_at DESC);

CREATE TABLE IF NOT EXISTS agent_bus.agent_message (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    message_role VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_agent_message_session_id ON agent_bus.agent_message(session_id);
CREATE INDEX IF NOT EXISTS idx_agent_message_session_time ON agent_bus.agent_message(session_id, created_at DESC);
