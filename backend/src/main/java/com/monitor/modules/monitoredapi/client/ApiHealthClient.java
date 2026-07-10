package com.monitor.modules.monitoredapi.client;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
public class ApiHealthClient {

        private final RestTemplate restTemplate;

        public ApiHealthClient(RestTemplate restTemplate) {
                this.restTemplate = restTemplate;
        }

        public ApiHealthCheckResult check(MonitoredApi api) {
                long startTime = System.currentTimeMillis();
                LocalDateTime checkedAt = LocalDateTime.now();

                try {
                        ResponseEntity<String> response = restTemplate.getForEntity(api.getUrl(), String.class);

                        long responseTime = calculateResponseTime(startTime);
                        boolean available = response.getStatusCode().is2xxSuccessful();
                        int statusCode = response.getStatusCode().value();

                        return new ApiHealthCheckResult(
                                        available,
                                        statusCode,
                                        responseTime,
                                        checkedAt,
                                        null);

                } catch (RestClientResponseException exception) {
                        long responseTime = calculateResponseTime(startTime);

                        return new ApiHealthCheckResult(
                                        false,
                                        exception.getStatusCode().value(),
                                        responseTime,
                                        checkedAt,
                                        exception.getMessage());

                } catch (ResourceAccessException exception) {
                        long responseTime = calculateResponseTime(startTime);

                        return new ApiHealthCheckResult(
                                        false,
                                        null,
                                        responseTime,
                                        checkedAt,
                                        "Timeout ou falha de conexão ao acessar a API.");
                }
        }

        private long calculateResponseTime(long startTime) {
                return System.currentTimeMillis() - startTime;
        }
}