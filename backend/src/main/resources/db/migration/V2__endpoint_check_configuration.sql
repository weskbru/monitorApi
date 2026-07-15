ALTER TABLE monitored_api ADD COLUMN IF NOT EXISTS expected_status_code INTEGER NOT NULL DEFAULT 200;
ALTER TABLE monitored_api ADD COLUMN IF NOT EXISTS timeout_ms BIGINT NOT NULL DEFAULT 10000;
ALTER TABLE monitored_api ADD COLUMN IF NOT EXISTS slow_threshold_ms BIGINT NOT NULL DEFAULT 3000;

CREATE INDEX IF NOT EXISTS idx_monitored_api_system ON monitored_api(monitored_system_id);
CREATE INDEX IF NOT EXISTS idx_monitored_api_active ON monitored_api(active);
CREATE INDEX IF NOT EXISTS idx_history_api_checked_at ON api_check_history(monitored_api_id, checked_at DESC);
