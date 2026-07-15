package com.monitor.modules.monitoredapi.repository;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ApiCheckHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ApiCheckHistoryRepository apiCheckHistoryRepository;

    @Test
    void shouldFindHistoryByMonitoredApiId() {
        MonitoredApi api = persistApi("ViaCEP");
        persistHistory(api, LocalDateTime.of(2026, 6, 27, 9, 0));
        persistHistory(api, LocalDateTime.of(2026, 6, 27, 10, 0));

        List<ApiCheckHistory> result = apiCheckHistoryRepository.findByMonitoredApiId(api.getId());

        assertEquals(2, result.size());
    }

    @Test
    void shouldFindHistoryByMonitoredApiIdOrderedByCheckedAtDesc() {
        MonitoredApi api = persistApi("ViaCEP");
        ApiCheckHistory olderHistory = persistHistory(api, LocalDateTime.of(2026, 6, 27, 9, 0));
        ApiCheckHistory newerHistory = persistHistory(api, LocalDateTime.of(2026, 6, 27, 10, 0));

        List<ApiCheckHistory> result = apiCheckHistoryRepository
                .findByMonitoredApiIdOrderByCheckedAtDesc(api.getId());

        assertEquals(2, result.size());
        assertEquals(newerHistory.getId(), result.get(0).getId());
        assertEquals(olderHistory.getId(), result.get(1).getId());
    }

    @Test
    void shouldFindCurrentStatusByMonitoredApiId() {
        MonitoredApi api = persistApi("ViaCEP");
        persistHistory(api, LocalDateTime.of(2026, 6, 27, 9, 0));
        ApiCheckHistory newerHistory = persistHistory(api, LocalDateTime.of(2026, 6, 27, 10, 0));

        Optional<ApiCheckHistory> result = apiCheckHistoryRepository
                .findFirstByMonitoredApiIdOrderByCheckedAtDesc(api.getId());

        assertTrue(result.isPresent());
        assertEquals(newerHistory.getId(), result.get().getId());
    }

    private MonitoredApi persistApi(String name) {
        MonitoredSystem system = new MonitoredSystem("Sistema de teste", "https://example.com");
        entityManager.persist(system);
        MonitoredApi api = new MonitoredApi(name, "https://viacep.com.br/ws/01001000/json/");
        api.setMonitoredSystem(system);
        entityManager.persist(api);
        return api;
    }

    private ApiCheckHistory persistHistory(MonitoredApi api, LocalDateTime checkedAt) {
        ApiCheckHistory history = new ApiCheckHistory();
        history.setMonitoredApi(api);
        history.setAvailable(true);
        history.setStatus(CheckStatus.UP);
        history.setStatusCode(200);
        history.setResponseTimeMs(120L);
        history.setCheckedAt(checkedAt);
        history.setErrorMessage(null);
        entityManager.persist(history);
        entityManager.flush();
        return history;
    }
}
