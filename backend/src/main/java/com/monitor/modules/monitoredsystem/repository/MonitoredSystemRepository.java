package com.monitor.modules.monitoredsystem.repository;

import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoredSystemRepository extends JpaRepository<MonitoredSystem, Long> {
}
