package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.repository.ApiCheckHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class ApiCheckService {

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

            saveCheckHistory(api, available, statusCode, responseTime, checkedAt, null);

            return new ApiCheckResponse(
                    api.getId(),
                    api.getName(),
                    api.getUrl(),
                    available,
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

            saveCheckHistory(api, false, statusCode, responseTime, checkedAt, errorMessage);

            return new ApiCheckResponse(
                    api.getId(),
                    api.getName(),
                    api.getUrl(),
                    false,
                    statusCode,
                    responseTime,
                    checkedAt,
                    errorMessage
            );
        } catch (Exception exception) {
            long responseTime = System.currentTimeMillis() - startTime;
            LocalDateTime checkedAt = LocalDateTime.now();
            String errorMessage = exception.getMessage();

            saveCheckHistory(api, false, null, responseTime, checkedAt, errorMessage);

            return new ApiCheckResponse(
                    api.getId(),
                    api.getName(),
                    api.getUrl(),
                    false,
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
            Integer statusCode,
            Long responseTime,
            LocalDateTime checkedAt,
            String errorMessage) {
        ApiCheckHistory history = new ApiCheckHistory();
        history.setMonitoredApi(api);
        history.setAvailable(available);
        history.setStatusCode(statusCode);
        history.setResponseTimeMs(responseTime);
        history.setCheckedAt(checkedAt);
        history.setErrorMessage(errorMessage);

        apiCheckHistoryRepository.save(history);
    }


}
