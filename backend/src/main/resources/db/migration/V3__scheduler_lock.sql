CREATE TABLE monitor_scheduler_lock (
    lock_name VARCHAR(100) PRIMARY KEY,
    locked_until TIMESTAMP NOT NULL,
    locked_by VARCHAR(100)
);
