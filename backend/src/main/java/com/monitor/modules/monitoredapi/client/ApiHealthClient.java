package com.monitor.modules.monitoredapi.client;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.Duration;

@Component
public class ApiHealthClient {

        private final RestTemplateBuilder restTemplateBuilder;
        private final RestTemplate fixedRestTemplate;
        private final ApiTargetValidator targetValidator;

        @Autowired
        public ApiHealthClient(RestTemplateBuilder restTemplateBuilder, ApiTargetValidator targetValidator) {
                this.restTemplateBuilder = restTemplateBuilder;
                this.fixedRestTemplate = null;
                this.targetValidator = targetValidator;
        }

        // Mantem testes unitarios capazes de controlar o transporte HTTP.
        public ApiHealthClient(RestTemplate restTemplate) {
                this.restTemplateBuilder = null;
                this.fixedRestTemplate = restTemplate;
                this.targetValidator = new ApiTargetValidator(true);
        }

        public ApiHealthCheckResult check(MonitoredApi api) {
                long startTime = System.currentTimeMillis();
                LocalDateTime checkedAt = LocalDateTime.now();

                try {
                        targetValidator.validate(api.getUrl());
                        RestTemplate restTemplate = createRestTemplate(api);
                        ResponseEntity<String> response = restTemplate.getForEntity(api.getUrl(), String.class);

                        long responseTime = calculateResponseTime(startTime);
                        int statusCode = response.getStatusCode().value();
                        int expectedStatusCode = api.getExpectedStatusCode() != null ? api.getExpectedStatusCode() : 200;
                        boolean available = statusCode == expectedStatusCode;

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
                } catch (IllegalArgumentException exception) {
                        long responseTime = calculateResponseTime(startTime);
                        return new ApiHealthCheckResult(false, null, responseTime, checkedAt, exception.getMessage());
                }
        }

        private RestTemplate createRestTemplate(MonitoredApi api) {
                if (fixedRestTemplate != null) {
                        return fixedRestTemplate;
                }

                long timeoutMs = api.getTimeoutMs() != null ? api.getTimeoutMs() : 10000L;
                Duration timeout = Duration.ofMillis(timeoutMs);
                return restTemplateBuilder.connectTimeout(timeout).readTimeout(timeout).build();
        }

        private long calculateResponseTime(long startTime) {
                return System.currentTimeMillis() - startTime;
        }
}
