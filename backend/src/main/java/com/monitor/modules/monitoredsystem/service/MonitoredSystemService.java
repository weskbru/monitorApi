package com.monitor.modules.monitoredsystem.service;

import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.modules.monitoredsystem.exception.MonitoredSystemNotFoundException;
import com.monitor.modules.monitoredsystem.repository.MonitoredSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
import com.monitor.modules.monitoredapi.repository.ApiCheckHistoryRepository;
import com.monitor.modules.monitoredapi.repository.ApiCurrentStatusRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class MonitoredSystemService {

    private final MonitoredSystemRepository monitoredSystemRepository;
    private MonitoredApiRepository monitoredApiRepository;
    private ApiCheckHistoryRepository apiCheckHistoryRepository;
    private ApiCurrentStatusRepository apiCurrentStatusRepository;

    public MonitoredSystemService(MonitoredSystemRepository monitoredSystemRepository) {
        this.monitoredSystemRepository = monitoredSystemRepository;
    }

    @Autowired
    void setApiRepositories(MonitoredApiRepository monitoredApiRepository,
            ApiCheckHistoryRepository apiCheckHistoryRepository,
            ApiCurrentStatusRepository apiCurrentStatusRepository) {
        this.monitoredApiRepository = monitoredApiRepository;
        this.apiCheckHistoryRepository = apiCheckHistoryRepository;
        this.apiCurrentStatusRepository = apiCurrentStatusRepository;
    }

    public MonitoredSystem create(String name, String baseUrl, String description) {
        MonitoredSystem system = new MonitoredSystem(name, baseUrl);
        system.setDescription(description);
        return monitoredSystemRepository.save(system);
    }

    public List<MonitoredSystem> listAll() {
        return monitoredSystemRepository.findAll();
    }

    public Page<MonitoredSystem> search(String query, int page, int size) {
        return monitoredSystemRepository.findByNameContainingIgnoreCase(
                query == null ? "" : query.trim(),
                PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)),
                        Sort.by(Sort.Direction.ASC, "name")));
    }

    public MonitoredSystem getById(Long id) {
        return monitoredSystemRepository.findById(id)
                .orElseThrow(() -> new MonitoredSystemNotFoundException(id));
    }

    public MonitoredSystem update(Long id, String name, String baseUrl, String description) {
        MonitoredSystem system = getById(id);
        system.setName(name);
        system.setBaseUrl(baseUrl);
        system.setDescription(description);
        return monitoredSystemRepository.save(system);
    }

    public MonitoredSystem updateActive(Long id, Boolean active) {
        MonitoredSystem system = getById(id);
        system.setActive(active);
        return monitoredSystemRepository.save(system);
    }

    @Transactional
    public void delete(Long id) {
        MonitoredSystem system = getById(id);
        if (monitoredApiRepository != null) {
            apiCheckHistoryRepository.deleteByMonitoredApiMonitoredSystemId(id);
            apiCurrentStatusRepository.deleteByMonitoredApiMonitoredSystemId(id);
            monitoredApiRepository.deleteByMonitoredSystemId(id);
        }
        monitoredSystemRepository.delete(system);
    }
}
