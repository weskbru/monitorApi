package com.monitor.modules.monitoredapi.repository;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonitoredApiRepository extends JpaRepository<MonitoredApi, Long> {
    List<MonitoredApi> findByActiveTrue();

    List<MonitoredApi> findByMonitoredSystemId(Long monitoredSystemId);

    Optional<MonitoredApi> findByIdAndMonitoredSystemId(Long id, Long monitoredSystemId);
}
