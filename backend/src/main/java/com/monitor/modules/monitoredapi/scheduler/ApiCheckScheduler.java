package com.monitor.modules.monitoredapi.scheduler;

import com.monitor.modules.monitoredapi.service.AutomatedApiCheckService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ApiCheckScheduler {

    private final AutomatedApiCheckService automatedApiCheckService;

    public ApiCheckScheduler(AutomatedApiCheckService automatedApiCheckService) {
        this.automatedApiCheckService = automatedApiCheckService;
    }

    @Scheduled(fixedDelayString = "${monitor.scheduler.delay-ms:60000}")
    public void checkActiveApis() {
        automatedApiCheckService.checkActiveApis();
    }
}