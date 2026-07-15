package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.client.ApiHealthCheckResult;
import com.monitor.modules.monitoredapi.client.ApiHealthClient;
import com.monitor.modules.monitoredapi.dto.ApiCheckHistoryResponse;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.dto.ApiCurrentStatusResponse;
import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.ApiCheckHistoryNotFoundException;
import com.monitor.modules.monitoredapi.exception.MonitoredApiInactiveException;
import com.monitor.modules.monitoredapi.repository.ApiCheckHistoryRepository;
import com.monitor.modules.monitoredapi.repository.ApiCurrentStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.TimeUnit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class ApiCheckService {

    private long staleAfterMs = Long.MAX_VALUE;
    private MeterRegistry meterRegistry;

    private final ApiCheckHistoryRepository apiCheckHistoryRepository;
    private final ApiCurrentStatusRepository apiCurrentStatusRepository;
    private final MonitoredApiService monitoredApiService;
    private final ApiHealthClient apiHealthClient;
    private final ApiCheckHistoryPolicy apiCheckHistoryPolicy;
    private final ApiStatusClassifier apiStatusClassifier;

    public ApiCheckService(
            ApiCheckHistoryRepository apiCheckHistoryRepository,
            ApiCurrentStatusRepository apiCurrentStatusRepository,
            MonitoredApiService monitoredApiService,
            ApiHealthClient apiHealthClient,
            ApiCheckHistoryPolicy apiCheckHistoryPolicy,
            ApiStatusClassifier apiStatusClassifier) {
        this.apiCheckHistoryRepository = apiCheckHistoryRepository;
        this.apiCurrentStatusRepository = apiCurrentStatusRepository;
        this.monitoredApiService = monitoredApiService;
        this.apiHealthClient = apiHealthClient;
        this.apiCheckHistoryPolicy = apiCheckHistoryPolicy;
        this.apiStatusClassifier = apiStatusClassifier;
    }

    public ApiCheckResponse check(Long id) {
        MonitoredApi api = monitoredApiService.getById(id);

        validateApiCanBeChecked(api);

        ApiHealthCheckResult result = apiHealthClient.check(api);

        return registerResult(
                api,
                result.available(),
                result.statusCode(),
                result.responseTimeMs(),
                result.checkedAt(),
                result.errorMessage()
        );
    }

    private void validateApiCanBeChecked(MonitoredApi api) {
        if (!Boolean.TRUE.equals(api.getActive())
                || api.getMonitoredSystem() != null && !Boolean.TRUE.equals(api.getMonitoredSystem().getActive())) {
            throw new MonitoredApiInactiveException(api.getId());
        }
    }

    @Value("${monitor.status.stale-after-ms:120000}")
    void setStaleAfterMs(long staleAfterMs) {
        this.staleAfterMs = staleAfterMs;
    }

    @Autowired(required = false)
    void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }


    private ApiCheckResponse registerResult(
            MonitoredApi api,
            boolean available,
            Integer statusCode,
            long responseTime,
            LocalDateTime checkedAt,
            String errorMessage
    ) {
        CheckStatus status = apiStatusClassifier.classifyStatus(
                available,
                responseTime,
                api.getSlowThresholdMs()
        );

        String message = buildMessage(status);
        if (meterRegistry != null) {
            meterRegistry.counter("monitor_api_checks_total", "status", status.name()).increment();
            meterRegistry.timer("monitor_api_check_duration", "apiId", String.valueOf(api.getId()))
                    .record(responseTime, TimeUnit.MILLISECONDS);
        }
        Optional<ApiCurrentStatus> currentStatusFound = apiCurrentStatusRepository
                .findByMonitoredApiId(api.getId());

        if (apiCheckHistoryPolicy.shouldSaveHistory(currentStatusFound, status, statusCode, errorMessage, checkedAt)) {
            saveCheckHistory(
                    api,
                    available,
                    status,
                    statusCode,
                    responseTime,
                    checkedAt,
                    errorMessage
            );
        }

        saveCurrentStatus(
                currentStatusFound.orElseGet(ApiCurrentStatus::new),
                api,
                available,
                status,
                statusCode,
                responseTime,
                checkedAt,
                errorMessage
        );

        return new ApiCheckResponse(
                api.getId(),
                api.getName(),
                api.getUrl(),
                available,
                status,
                message,
                statusCode,
                responseTime,
                checkedAt,
                errorMessage
        );
    }

    private void saveCheckHistory(
            MonitoredApi api,
            Boolean available,
            CheckStatus status,
            Integer statusCode,
            Long responseTime,
            LocalDateTime checkedAt,
            String errorMessage
    ) {
        ApiCheckHistory history = new ApiCheckHistory();
        history.setMonitoredApi(api);
        history.setAvailable(available);
        history.setStatus(status);
        history.setStatusCode(statusCode);
        history.setResponseTimeMs(responseTime);
        history.setCheckedAt(checkedAt);
        history.setErrorMessage(errorMessage);

        apiCheckHistoryRepository.save(history);
    }

    private void saveCurrentStatus(
            ApiCurrentStatus currentStatus,
            MonitoredApi api,
            Boolean available,
            CheckStatus status,
            Integer statusCode,
            Long responseTime,
            LocalDateTime checkedAt,
            String errorMessage
    ) {
        currentStatus.setMonitoredApi(api);
        currentStatus.setAvailable(available);
        currentStatus.setStatus(status);
        currentStatus.setStatusCode(statusCode);
        currentStatus.setResponseTimeMs(responseTime);
        currentStatus.setCheckedAt(checkedAt);
        currentStatus.setErrorMessage(errorMessage);

        apiCurrentStatusRepository.save(currentStatus);
    }

    private String buildMessage(CheckStatus status) {
        if (status == CheckStatus.DOWN) {
            return "O endpoint não está disponível.";
        }

        if (status == CheckStatus.SLOW) {
            return "O endpoint respondeu, mas está lento.";
        }

        return "O endpoint respondeu normalmente.";
    }

    public List<ApiCheckHistoryResponse> getHistory(Long id) {
        MonitoredApi api = monitoredApiService.getById(id);

        return apiCheckHistoryRepository
                .findByMonitoredApiIdOrderByCheckedAtDesc(api.getId())
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    public List<ApiCheckHistoryResponse> getHistory(Long id, int page, int size) {
        MonitoredApi api = monitoredApiService.getById(id);
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));

        return apiCheckHistoryRepository.findByMonitoredApiId(
                        api.getId(),
                        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "checkedAt")))
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    public ApiCurrentStatusResponse getCurrentStatus(Long id) {
        MonitoredApi api = monitoredApiService.getById(id);

        return apiCurrentStatusRepository
                .findByMonitoredApiId(api.getId())
                .map(this::toCurrentStatusResponse)
                .orElseThrow(() -> new ApiCheckHistoryNotFoundException(id));
    }

    private ApiCheckHistoryResponse toHistoryResponse(ApiCheckHistory history) {
        return new ApiCheckHistoryResponse(
                history.getId(),
                history.getStatus(),
                buildMessage(history.getStatus()),
                history.getAvailable(),
                history.getStatusCode(),
                history.getResponseTimeMs(),
                history.getCheckedAt(),
                history.getErrorMessage()
        );
    }

    private ApiCurrentStatusResponse toCurrentStatusResponse(ApiCurrentStatus currentStatus) {
        MonitoredApi api = currentStatus.getMonitoredApi();
        boolean stale = staleAfterMs != Long.MAX_VALUE && (currentStatus.getCheckedAt() == null
                || currentStatus.getCheckedAt().plusNanos(staleAfterMs * 1_000_000).isBefore(LocalDateTime.now()));
        CheckStatus effectiveStatus = stale ? CheckStatus.UNKNOWN : currentStatus.getStatus();
        return new ApiCurrentStatusResponse(
                currentStatus.getId(),
                api.getId(),
                api.getName(),
                api.getUrl(),
                effectiveStatus,
                stale ? "A ultima leitura expirou; execute uma nova verificacao." : buildMessage(effectiveStatus),
                stale ? false : currentStatus.getAvailable(),
                currentStatus.getStatusCode(),
                currentStatus.getResponseTimeMs(),
                currentStatus.getCheckedAt(),
                currentStatus.getErrorMessage()
        );
    }
}
