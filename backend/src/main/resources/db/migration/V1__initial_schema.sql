CREATE TABLE monitored_system (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    base_url VARCHAR(2048) NOT NULL,
    description VARCHAR(2000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE monitored_api (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    description VARCHAR(2000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    slow_threshold_ms BIGINT NOT NULL DEFAULT 3000,
    monitored_system_id BIGINT NOT NULL REFERENCES monitored_system(id)
);

CREATE TABLE api_current_status (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(32),
    available BOOLEAN,
    status_code INTEGER,
    response_time_ms BIGINT,
    checked_at TIMESTAMP,
    error_message VARCHAR(2000),
    monitored_api_id BIGINT NOT NULL UNIQUE REFERENCES monitored_api(id) ON DELETE CASCADE
);

CREATE TABLE api_check_history (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(32),
    available BOOLEAN,
    status_code INTEGER,
    response_time_ms BIGINT,
    checked_at TIMESTAMP,
    error_message VARCHAR(2000),
    monitored_api_id BIGINT NOT NULL REFERENCES monitored_api(id) ON DELETE CASCADE
);

CREATE INDEX idx_monitored_api_system ON monitored_api(monitored_system_id);
CREATE INDEX idx_monitored_api_active ON monitored_api(active);
CREATE INDEX idx_current_status_system_api ON api_current_status(monitored_api_id);
CREATE INDEX idx_history_api_checked_at ON api_check_history(monitored_api_id, checked_at DESC);
