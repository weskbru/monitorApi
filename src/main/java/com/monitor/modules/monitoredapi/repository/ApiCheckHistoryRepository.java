package com.monitor.modules.monitoredapi.repository;

import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ApiCheckHistoryRepository extends JpaRepository<ApiCheckHistory, Long> {
    List<ApiCheckHistory> findByMonitoredApiId(Long monitoredApiId);
}
