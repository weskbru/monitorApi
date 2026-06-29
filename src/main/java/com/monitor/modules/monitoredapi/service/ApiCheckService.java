package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.dto.ApiCheckHistoryResponse;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.ApiCheckHistoryNotFoundException;
import com.monitor.modules.monitoredapi.exception.MonitoredApiInactiveException;
import com.monitor.modules.monitoredapi.repository.ApiCheckHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiCheckService {

    private static final long DEFAULT_SLOW_THRESHOLD_MS = 3000L;

    private final ApiCheckHistoryRepository apiCheckHistoryRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MonitoredApiService monitoredApiService;

    public ApiCheckService(
            ApiCheckHistoryRepository apiCheckHistoryRepository,
            MonitoredApiService monitoredApiService
    ) {
        this.apiCheckHistoryRepository = apiCheckHistoryRepository;
        this.monitoredApiService = monitoredApiService;
    }

    public ApiCheckResponse check(Long id) {
        MonitoredApi api = monitoredApiService.getById(id);

        validateApiCanBeChecked(api);

        long startTime = System.currentTimeMillis();
        LocalDateTime checkedAt = LocalDateTime.now();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(api.getUrl(), String.class);

            long responseTime = calculateResponseTime(startTime);
            boolean available = response.getStatusCode().is2xxSuccessful();
            int statusCode = response.getStatusCode().value();

            return registerResult(
                    api,
                    available,
                    statusCode,
                    responseTime,
                    checkedAt,
                    null
            );

        } catch (RestClientResponseException exception) {
            long responseTime = calculateResponseTime(startTime);

            return registerResult(
                    api,
                    false,
                    exception.getStatusCode().value(),
                    responseTime,
                    checkedAt,
                    exception.getMessage()
            );

        } catch (RestClientException exception) {
            long responseTime = calculateResponseTime(startTime);

            return registerResult(
                    api,
                    false,
                    null,
                    responseTime,
                    checkedAt,
                    exception.getMessage()
            );
        }
    }

    private void validateApiCanBeChecked(MonitoredApi api) {
        if (!Boolean.TRUE.equals(api.getActive())) {
            throw new MonitoredApiInactiveException(api.getId());
        }
    }

    private long calculateResponseTime(long startTime) {
        return System.currentTimeMillis() - startTime;
    }

    private ApiCheckResponse registerResult(
            MonitoredApi api,
            boolean available,
            Integer statusCode,
            long responseTime,
            LocalDateTime checkedAt,
            String errorMessage
    ) {
        CheckStatus status = classifyStatus(
                available,
                responseTime,
                api.getSlowThresholdMs()
        );

        String message = buildMessage(status);

        saveCheckHistory(
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

    private CheckStatus classifyStatus(
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

    public ApiCheckHistoryResponse getCurrentStatus(Long id) {
        MonitoredApi api = monitoredApiService.getById(id);

        return apiCheckHistoryRepository
                .findFirstByMonitoredApiIdOrderByCheckedAtDesc(api.getId())
                .map(this::toHistoryResponse)
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
}