package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.MonitoredApiNotFoundException;

import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.modules.monitoredsystem.service.MonitoredSystemService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.monitor.modules.monitoredapi.repository.ApiCheckHistoryRepository;
import com.monitor.modules.monitoredapi.repository.ApiCurrentStatusRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class MonitoredApiService {

    private final MonitoredApiRepository monitoredApiRepository;
    private final MonitoredSystemService monitoredSystemService;
    private ApiCheckHistoryRepository apiCheckHistoryRepository;
    private ApiCurrentStatusRepository apiCurrentStatusRepository;

    public MonitoredApiService(
            MonitoredApiRepository monitoredApiRepository,
            MonitoredSystemService monitoredSystemService) {
        this.monitoredApiRepository = monitoredApiRepository;
        this.monitoredSystemService = monitoredSystemService;
    }

    @Autowired
    void setStatusRepositories(ApiCheckHistoryRepository apiCheckHistoryRepository,
            ApiCurrentStatusRepository apiCurrentStatusRepository) {
        this.apiCheckHistoryRepository = apiCheckHistoryRepository;
        this.apiCurrentStatusRepository = apiCurrentStatusRepository;
    }

    public MonitoredApi create(Long systemId, String name, String url, String description) {
        return create(systemId, name, url, description, 200, 3000L, 10000L);
    }

    public MonitoredApi create(Long systemId, String name, String url, String description,
            Integer expectedStatusCode, Long slowThresholdMs, Long timeoutMs) {
        MonitoredSystem system = monitoredSystemService.getById(systemId);

        MonitoredApi api = new MonitoredApi(name, url);
        api.setDescription(description);
        api.setMonitoredSystem(system);
        applyCheckConfiguration(api, expectedStatusCode, slowThresholdMs, timeoutMs);
        return monitoredApiRepository.save(api);
    }

    public MonitoredApi createForSystem(Long systemId, String name, String url, String description) {
        return create(systemId, name, url, description);
    }

    public MonitoredApi createForSystem(Long systemId, String name, String url, String description,
            Integer expectedStatusCode, Long slowThresholdMs, Long timeoutMs) {
        return create(systemId, name, url, description, expectedStatusCode, slowThresholdMs, timeoutMs);
    }

    public List<MonitoredApi> listAll() {
        return monitoredApiRepository.findAll();
    }

    public Page<MonitoredApi> search(String query, int page, int size) {
        return monitoredApiRepository.findByNameContainingIgnoreCase(
                query == null ? "" : query.trim(), pageRequest(page, size));
    }

    public Page<MonitoredApi> searchBySystem(Long systemId, String query, int page, int size) {
        monitoredSystemService.getById(systemId);
        return monitoredApiRepository.findByMonitoredSystemIdAndNameContainingIgnoreCase(
                systemId, query == null ? "" : query.trim(), pageRequest(page, size));
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Direction.ASC, "name"));
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

    @Transactional
    public void delete(Long id) {
        MonitoredApi api = getById(id);
        deleteStatuses(id);
        monitoredApiRepository.delete(api);
    }

    public MonitoredApi update(Long id, Long systemId, String name, String url, String description) {
        return update(id, systemId, name, url, description, 200, 3000L, 10000L);
    }

    public MonitoredApi update(Long id, Long systemId, String name, String url, String description,
            Integer expectedStatusCode, Long slowThresholdMs, Long timeoutMs) {
        MonitoredApi api = getById(id);
        MonitoredSystem system = monitoredSystemService.getById(systemId);

        api.setName(name);
        api.setUrl(url);
        api.setDescription(description);
        api.setMonitoredSystem(system);
        applyCheckConfiguration(api, expectedStatusCode, slowThresholdMs, timeoutMs);
        return monitoredApiRepository.save(api);
    }

    public MonitoredApi updateForSystem(Long systemId, Long apiId, String name, String url, String description) {
        return updateForSystem(systemId, apiId, name, url, description, 200, 3000L, 10000L);
    }

    public MonitoredApi updateForSystem(Long systemId, Long apiId, String name, String url, String description,
            Integer expectedStatusCode, Long slowThresholdMs, Long timeoutMs) {
        MonitoredApi api = getBySystemIdAndApiId(systemId, apiId);
        api.setName(name);
        api.setUrl(url);
        api.setDescription(description);
        applyCheckConfiguration(api, expectedStatusCode, slowThresholdMs, timeoutMs);
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

    @Transactional
    public void deleteForSystem(Long systemId, Long apiId) {
        MonitoredApi api = getBySystemIdAndApiId(systemId, apiId);
        deleteStatuses(apiId);
        monitoredApiRepository.delete(api);
    }

    private void deleteStatuses(Long apiId) {
        if (apiCheckHistoryRepository != null) apiCheckHistoryRepository.deleteByMonitoredApiId(apiId);
        if (apiCurrentStatusRepository != null) apiCurrentStatusRepository.deleteByMonitoredApiId(apiId);
    }

    private void applyCheckConfiguration(MonitoredApi api, Integer expectedStatusCode,
            Long slowThresholdMs, Long timeoutMs) {
        api.setExpectedStatusCode(expectedStatusCode != null ? expectedStatusCode : 200);
        api.setSlowThresholdMs(slowThresholdMs != null ? slowThresholdMs : 3000L);
        api.setTimeoutMs(timeoutMs != null ? timeoutMs : 10000L);
    }

}
