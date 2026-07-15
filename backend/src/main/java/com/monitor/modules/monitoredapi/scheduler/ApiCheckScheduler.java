package com.monitor.modules.monitoredapi.scheduler;

import com.monitor.modules.monitoredapi.service.AutomatedApiCheckService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "monitor.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ApiCheckScheduler {

    private final AutomatedApiCheckService automatedApiCheckService;
    private final SchedulerLockService schedulerLockService;

    public ApiCheckScheduler(AutomatedApiCheckService automatedApiCheckService,
            SchedulerLockService schedulerLockService) {
        this.automatedApiCheckService = automatedApiCheckService;
        this.schedulerLockService = schedulerLockService;
    }

    @Scheduled(fixedDelayString = "${monitor.scheduler.delay-ms:60000}")
    public void checkActiveApis() {
        if (!schedulerLockService.tryAcquire("active-api-checks")) {
            return;
        }

        try {
            automatedApiCheckService.checkActiveApis();
        } finally {
            schedulerLockService.release("active-api-checks");
        }
    }
}
