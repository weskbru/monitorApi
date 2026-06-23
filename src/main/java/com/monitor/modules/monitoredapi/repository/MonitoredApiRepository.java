package com.monitor.modules.monitoredapi.repository;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoredApiRepository extends JpaRepository<MonitoredApi, Long> {
    
}

