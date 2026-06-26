package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.repository.ApiCheckHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import com.monitor.modules.monitoredapi.CheckStatus;

import java.time.LocalDateTime;

@Service
public class ApiCheckService {

    private static final long DEFAULT_SLOW_THRESHOLD_MS = 3000L;

    private final ApiCheckHistoryRepository apiCheckHistoryRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MonitoredApiService monitoredApiService;


    public ApiCheckService(ApiCheckHistoryRepository apiCheckHistoryRepository, MonitoredApiService monitoredApiService) {
        this.apiCheckHistoryRepository = apiCheckHistoryRepository;
        this.monitoredApiService = monitoredApiService;
    }

    public ApiCheckResponse check(Long id) {
        MonitoredApi api = monitoredApiService.getById(id);
        long startTime = System.currentTimeMillis();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(api.getUrl(), String.class);
            long responseTime = System.currentTimeMillis() - startTime;
            LocalDateTime checkedAt = LocalDateTime.now();
            boolean available = response.getStatusCode().is2xxSuccessful();
            int statusCode = response.getStatusCode().value();

            CheckStatus status = classifyStatus(available, responseTime, api.getSlowThresholdMs());
            String message = buildMessage(status);

            saveCheckHistory(api, available, status, statusCode, responseTime, checkedAt, null);

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
                    null
            );
        } catch (RestClientResponseException exception) {
            long responseTime = System.currentTimeMillis() - startTime;
            LocalDateTime checkedAt = LocalDateTime.now();
            int statusCode = exception.getStatusCode().value();
            String errorMessage = exception.getMessage();
            CheckStatus status = CheckStatus.DOWN;
            String message = buildMessage(status);

            saveCheckHistory(api, false, status, statusCode, responseTime, checkedAt, errorMessage);

            return new ApiCheckResponse(
                    api.getId(),
                    api.getName(),
                    api.getUrl(),
                    false,
                    status,
                    message,
                    statusCode,
                    responseTime,
                    checkedAt,
                    errorMessage
            );
        } catch (Exception exception) {
            long responseTime = System.currentTimeMillis() - startTime;
            LocalDateTime checkedAt = LocalDateTime.now();
            String errorMessage = exception.getMessage();
            CheckStatus status = CheckStatus.DOWN;
            String message = buildMessage(status);

            saveCheckHistory(api, false, status, null, responseTime, checkedAt, errorMessage);

            return new ApiCheckResponse(
                    api.getId(),
                    api.getName(),
                    api.getUrl(),
                    false,
                    status,
                    message,
                    null,
                    responseTime,
                    checkedAt,
                    errorMessage
                    
            );
        }
    }

    private void saveCheckHistory(
            MonitoredApi api,
            Boolean available,
            CheckStatus status,
            Integer statusCode,
            Long responseTime,
            LocalDateTime checkedAt,
            String errorMessage) {
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


    private CheckStatus classifyStatus(Boolean available, Long responseTime, Long slowThresholdMs) {
        Long threshold = slowThresholdMs != null ? slowThresholdMs : DEFAULT_SLOW_THRESHOLD_MS;

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
        } else if (status == CheckStatus.SLOW) {
            return "O endpoint respondeu, mas está lento.";
        } else {
            return "O endpoint respondeu normalmente.";
        }
    }

}
