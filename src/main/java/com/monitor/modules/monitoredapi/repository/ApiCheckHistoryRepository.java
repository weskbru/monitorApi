package com.monitor.modules.monitoredapi.repository;

import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface ApiCheckHistoryRepository extends JpaRepository<ApiCheckHistory, Long> {
    List<ApiCheckHistory> findByMonitoredApiId(Long monitoredApiId);
    List<ApiCheckHistory> findByMonitoredApiIdOrderByCheckedAtDesc(Long monitoredApiId);
    Optional<ApiCheckHistory> findFirstByMonitoredApiIdOrderByCheckedAtDesc(Long monitoredApiId);
}
