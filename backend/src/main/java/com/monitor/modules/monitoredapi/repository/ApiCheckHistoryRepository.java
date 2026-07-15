package com.monitor.modules.monitoredapi.repository;

import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ApiCheckHistoryRepository extends JpaRepository<ApiCheckHistory, Long> {
    List<ApiCheckHistory> findByMonitoredApiId(Long monitoredApiId);
    List<ApiCheckHistory> findByMonitoredApiIdOrderByCheckedAtDesc(Long monitoredApiId);
    Optional<ApiCheckHistory> findFirstByMonitoredApiIdOrderByCheckedAtDesc(Long monitoredApiId);
    Page<ApiCheckHistory> findByMonitoredApiId(Long monitoredApiId, Pageable pageable);
    void deleteByMonitoredApiId(Long monitoredApiId);
    void deleteByMonitoredApiMonitoredSystemId(Long monitoredSystemId);
}
