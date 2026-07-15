package com.monitor.modules.monitoredapi.repository;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MonitoredApiRepository extends JpaRepository<MonitoredApi, Long> {
    List<MonitoredApi> findByActiveTrue();

    List<MonitoredApi> findByActiveTrueAndMonitoredSystemActiveTrue();

    List<MonitoredApi> findByMonitoredSystemId(Long monitoredSystemId);

    Optional<MonitoredApi> findByIdAndMonitoredSystemId(Long id, Long monitoredSystemId);
    void deleteByMonitoredSystemId(Long monitoredSystemId);
    Page<MonitoredApi> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<MonitoredApi> findByMonitoredSystemIdAndNameContainingIgnoreCase(Long monitoredSystemId, String name, Pageable pageable);
}
