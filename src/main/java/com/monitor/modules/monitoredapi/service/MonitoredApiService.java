package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MonitoredApiService {
    private final MonitoredApiRepository monitoredApiRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public MonitoredApiService(MonitoredApiRepository monitoredApiRepository) {
        this.monitoredApiRepository = monitoredApiRepository;
    }

    public MonitoredApi create(String name, String url, String description) {
        MonitoredApi api = new MonitoredApi(name, url);
        api.setDescription(description);
        return monitoredApiRepository.save(api);
    }

    public List<MonitoredApi> listAll() {
        return monitoredApiRepository.findAll();
    }

    public MonitoredApi getById(Long id) {
        return monitoredApiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API nao encontrada com Id: " + id));
    }

    public void delete(Long id) {
        MonitoredApi api = getById(id);
        monitoredApiRepository.delete(api);
    }

    public MonitoredApi update(Long id, String name, String url, String description) {
        MonitoredApi api = getById(id);
        api.setName(name);
        api.setUrl(url);
        api.setDescription(description);
        return monitoredApiRepository.save(api);
    }

    public ApiCheckResponse check(Long id) {
        MonitoredApi api = getById(id);

        long startTime = System.currentTimeMillis();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(api.getUrl(), String.class);
            long responseTime = System.currentTimeMillis() - startTime;

            boolean available = response.getStatusCode().is2xxSuccessful();
            return new ApiCheckResponse(
                    api.getId(),
                    api.getName(),
                    api.getUrl(),
                    available,
                    response.getStatusCode().value(),
                    responseTime,
                    LocalDateTime.now(),
                    null
            );
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new ApiCheckResponse(
                    api.getId(),
                    api.getName(),
                    api.getUrl(),
                    false,
                    null,
                    responseTime,
                    LocalDateTime.now(),
                    e.getMessage()
            );
        }
       
    }
}
