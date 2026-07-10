package com.monitor.modules.monitoredsystem.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.repository.ApiCurrentStatusRepository;
import com.monitor.modules.monitoredapi.service.MonitoredApiService;
import com.monitor.modules.monitoredsystem.dto.MonitoredSystemStatusResponse;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MonitoredSystemStatusService {

    private final MonitoredSystemService monitoredSystemService;
    private final MonitoredApiService monitoredApiService;
    private final ApiCurrentStatusRepository apiCurrentStatusRepository;

    public MonitoredSystemStatusService(
            MonitoredSystemService monitoredSystemService,
            MonitoredApiService monitoredApiService,
            ApiCurrentStatusRepository apiCurrentStatusRepository) {
        this.monitoredSystemService = monitoredSystemService;
        this.monitoredApiService = monitoredApiService;
        this.apiCurrentStatusRepository = apiCurrentStatusRepository;
    }

    public MonitoredSystemStatusResponse getStatus(Long systemId) {
        MonitoredSystem system = monitoredSystemService.getById(systemId);
        List<MonitoredApi> activeApis = monitoredApiService.listBySystemId(systemId)
                .stream()
                .filter(api -> Boolean.TRUE.equals(api.getActive()))
                .toList();

        Map<Long, ApiCurrentStatus> statusesByApiId = apiCurrentStatusRepository
                .findByMonitoredApiMonitoredSystemId(systemId)
                .stream()
                .collect(Collectors.toMap(
                        status -> status.getMonitoredApi().getId(),
                        Function.identity()));

        int upEndpoints = 0;
        int slowEndpoints = 0;
        int downEndpoints = 0;
        int unknownEndpoints = 0;

        for (MonitoredApi api : activeApis) {
            ApiCurrentStatus currentStatus = statusesByApiId.get(api.getId());

            if (currentStatus == null || currentStatus.getStatus() == null) {
                unknownEndpoints++;
                continue;
            }

            if (currentStatus.getStatus() == CheckStatus.UP) {
                upEndpoints++;
            } else if (currentStatus.getStatus() == CheckStatus.SLOW) {
                slowEndpoints++;
            } else if (currentStatus.getStatus() == CheckStatus.DOWN) {
                downEndpoints++;
            } else {
                unknownEndpoints++;
            }
        }

        CheckStatus systemStatus = classifySystemStatus(activeApis.size(), upEndpoints, slowEndpoints, downEndpoints, unknownEndpoints);
        LocalDateTime lastCheckedAt = latestCheckedAt(activeApis, statusesByApiId);

        return new MonitoredSystemStatusResponse(
                system.getId(),
                system.getName(),
                systemStatus,
                buildMessage(systemStatus),
                activeApis.size(),
                upEndpoints,
                slowEndpoints,
                downEndpoints,
                unknownEndpoints,
                lastCheckedAt
        );
    }

    private CheckStatus classifySystemStatus(
            int totalEndpoints,
            int upEndpoints,
            int slowEndpoints,
            int downEndpoints,
            int unknownEndpoints) {
        if (totalEndpoints == 0) {
            return CheckStatus.UNKNOWN;
        }

        if (downEndpoints > 0) {
            return CheckStatus.DOWN;
        }

        if (slowEndpoints > 0) {
            return CheckStatus.SLOW;
        }

        if (unknownEndpoints > 0) {
            return CheckStatus.UNKNOWN;
        }

        if (upEndpoints == totalEndpoints) {
            return CheckStatus.UP;
        }

        return CheckStatus.UNKNOWN;
    }

    private String buildMessage(CheckStatus status) {
        if (status == CheckStatus.DOWN) {
            return "Existe endpoint critico indisponivel.";
        }

        if (status == CheckStatus.SLOW) {
            return "Existe endpoint critico respondendo lentamente.";
        }

        if (status == CheckStatus.UP) {
            return "Todos os endpoints criticos ativos estao saudaveis.";
        }

        return "Ainda nao ha leitura suficiente para calcular a saude do sistema.";
    }

    private LocalDateTime latestCheckedAt(List<MonitoredApi> activeApis, Map<Long, ApiCurrentStatus> statusesByApiId) {
        return activeApis
                .stream()
                .map(api -> statusesByApiId.get(api.getId()))
                .filter(status -> status != null)
                .map(ApiCurrentStatus::getCheckedAt)
                .filter(checkedAt -> checkedAt != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
