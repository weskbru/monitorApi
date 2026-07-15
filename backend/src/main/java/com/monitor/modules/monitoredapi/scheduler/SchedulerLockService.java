package com.monitor.modules.monitoredapi.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
public class SchedulerLockService {

    private final JdbcTemplate jdbcTemplate;
    private final String owner = UUID.randomUUID().toString();
    private final long lockAtMostMs;

    public SchedulerLockService(JdbcTemplate jdbcTemplate,
            @Value("${monitor.scheduler.lock-at-most-ms:300000}") long lockAtMostMs) {
        this.jdbcTemplate = jdbcTemplate;
        this.lockAtMostMs = lockAtMostMs;
    }

    public boolean tryAcquire(String name) {
        Instant now = Instant.now();
        Timestamp until = Timestamp.from(now.plusMillis(lockAtMostMs));
        int updated = jdbcTemplate.update(
                "UPDATE monitor_scheduler_lock SET locked_until = ?, locked_by = ? WHERE lock_name = ? AND locked_until < ?",
                until, owner, name, Timestamp.from(now));
        if (updated == 1) return true;

        try {
            return jdbcTemplate.update(
                    "INSERT INTO monitor_scheduler_lock(lock_name, locked_until, locked_by) VALUES (?, ?, ?)",
                    name, until, owner) == 1;
        } catch (DuplicateKeyException exception) {
            return false;
        }
    }

    public void release(String name) {
        jdbcTemplate.update(
                "UPDATE monitor_scheduler_lock SET locked_until = ?, locked_by = NULL WHERE lock_name = ? AND locked_by = ?",
                Timestamp.from(Instant.now()), name, owner);
    }
}
