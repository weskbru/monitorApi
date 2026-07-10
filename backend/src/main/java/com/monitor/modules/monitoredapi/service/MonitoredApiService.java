package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.MonitoredApiNotFoundException;

import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.modules.monitoredsystem.service.MonitoredSystemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoredApiService {

    private final MonitoredApiRepository monitoredApiRepository;
    private final MonitoredSystemService monitoredSystemService;

    public MonitoredApiService(
            MonitoredApiRepository monitoredApiRepository,
            MonitoredSystemService monitoredSystemService) {
        this.monitoredApiRepository = monitoredApiRepository;
        this.monitoredSystemService = monitoredSystemService;
    }

    public MonitoredApi create(Long systemId, String name, String url, String description) {
        MonitoredSystem system = monitoredSystemService.getById(systemId);

        MonitoredApi api = new MonitoredApi(name, url);
        api.setDescription(description);
        api.setMonitoredSystem(system);
        return monitoredApiRepository.save(api);
    }

    public MonitoredApi createForSystem(Long systemId, String name, String url, String description) {
        return create(systemId, name, url, description);
    }

    public List<MonitoredApi> listAll() {
        return monitoredApiRepository.findAll();
    }

    public List<MonitoredApi> listBySystemId(Long systemId) {
        monitoredSystemService.getById(systemId);
        return monitoredApiRepository.findByMonitoredSystemId(systemId);
    }

    public MonitoredApi getById(Long id) {
        return monitoredApiRepository.findById(id)
                .orElseThrow(() -> new MonitoredApiNotFoundException(id));
    }

    public MonitoredApi getBySystemIdAndApiId(Long systemId, Long apiId) {
        monitoredSystemService.getById(systemId);
        return monitoredApiRepository.findByIdAndMonitoredSystemId(apiId, systemId)
                .orElseThrow(() -> new MonitoredApiNotFoundException(apiId));
    }

    public void delete(Long id) {
        MonitoredApi api = getById(id);
        monitoredApiRepository.delete(api);
    }

    public MonitoredApi update(Long id, Long systemId, String name, String url, String description) {
        MonitoredApi api = getById(id);
        MonitoredSystem system = monitoredSystemService.getById(systemId);

        api.setName(name);
        api.setUrl(url);
        api.setDescription(description);
        api.setMonitoredSystem(system);
        return monitoredApiRepository.save(api);
    }

    public MonitoredApi updateForSystem(Long systemId, Long apiId, String name, String url, String description) {
        MonitoredApi api = getBySystemIdAndApiId(systemId, apiId);
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

    public MonitoredApi updateActiveForSystem(Long systemId, Long apiId, Boolean active) {
        MonitoredApi api = getBySystemIdAndApiId(systemId, apiId);
        api.setActive(active);
        return monitoredApiRepository.save(api);
    }

    public void deleteForSystem(Long systemId, Long apiId) {
        MonitoredApi api = getBySystemIdAndApiId(systemId, apiId);
        monitoredApiRepository.delete(api);
    }

}
