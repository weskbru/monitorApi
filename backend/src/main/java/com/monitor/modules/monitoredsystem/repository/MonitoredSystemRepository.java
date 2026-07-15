package com.monitor.modules.monitoredsystem.repository;

import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MonitoredSystemRepository extends JpaRepository<MonitoredSystem, Long> {
    Page<MonitoredSystem> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
