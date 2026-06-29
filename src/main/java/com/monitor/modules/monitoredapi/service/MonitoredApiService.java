package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.MonitoredApiNotFoundException;

import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoredApiService {

    private final MonitoredApiRepository monitoredApiRepository;

    public MonitoredApiService(
            MonitoredApiRepository monitoredApiRepository) {
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
                .orElseThrow(() -> new MonitoredApiNotFoundException(id));
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

    public MonitoredApi updateActive(Long id, Boolean active) {
        MonitoredApi api = getById(id);
        api.setActive(active);
        return monitoredApiRepository.save(api);
    }

}
