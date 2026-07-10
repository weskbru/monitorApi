package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiStatusClassifier {

    private static final long DEFAULT_SLOW_THRESHOLD_MS = 3000L;

    CheckStatus classifyStatus(
            Boolean available,
            Long responseTime,
            Long slowThresholdMs
    ) {
        Long threshold = slowThresholdMs != null
                ? slowThresholdMs
                : DEFAULT_SLOW_THRESHOLD_MS;

        if (!available) {
            return CheckStatus.DOWN;
        }

        if (responseTime > threshold) {
            return CheckStatus.SLOW;
        }

        return CheckStatus.UP;
    }
}
