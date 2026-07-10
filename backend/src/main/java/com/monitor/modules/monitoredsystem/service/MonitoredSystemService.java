package com.monitor.modules.monitoredsystem.service;

import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.modules.monitoredsystem.exception.MonitoredSystemNotFoundException;
import com.monitor.modules.monitoredsystem.repository.MonitoredSystemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoredSystemService {

    private final MonitoredSystemRepository monitoredSystemRepository;

    public MonitoredSystemService(MonitoredSystemRepository monitoredSystemRepository) {
        this.monitoredSystemRepository = monitoredSystemRepository;
    }

    public MonitoredSystem create(String name, String baseUrl, String description) {
        MonitoredSystem system = new MonitoredSystem(name, baseUrl);
        system.setDescription(description);
        return monitoredSystemRepository.save(system);
    }

    public List<MonitoredSystem> listAll() {
        return monitoredSystemRepository.findAll();
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

    public void delete(Long id) {
        MonitoredSystem system = getById(id);
        monitoredSystemRepository.delete(system);
    }
}
