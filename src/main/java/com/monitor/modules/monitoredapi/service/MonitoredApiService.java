package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MonitoredApiService {
    private final MonitoredApiRepository monitoredApiRepository;

    public MonitoredApiService(MonitoredApiRepository monitoredApiRepository) {
        this.monitoredApiRepository = monitoredApiRepository;
    }

    public MonitoredApi create(String name, String url) {
        MonitoredApi api = new MonitoredApi(name, url);
        return monitoredApiRepository.save(api);
    }

    public List<MonitoredApi> listAll() {
        return monitoredApiRepository.findAll();
    }

    public MonitoredApi getById(Long id) {
        return monitoredApiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API nao encontrada com Id: " + id));
    }
}
