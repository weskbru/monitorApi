package com.monitor.modules.monitoredapi.client;

import java.time.LocalDateTime;

public record ApiHealthCheckResult(
        boolean available,
        Integer statusCode,
        long responseTimeMs,
        LocalDateTime checkedAt,
        String errorMessage) {
}