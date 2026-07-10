package com.monitor.modules.monitoredapi.repository;

import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiCurrentStatusRepository extends JpaRepository<ApiCurrentStatus, Long> {

    Optional<ApiCurrentStatus> findByMonitoredApiId(Long monitoredApiId);
}
