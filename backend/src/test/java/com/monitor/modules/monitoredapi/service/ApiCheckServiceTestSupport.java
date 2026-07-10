package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.client.ApiHealthClient;
import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.repository.ApiCheckHistoryRepository;
import com.monitor.modules.monitoredapi.repository.ApiCurrentStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;

abstract class ApiCheckServiceTestSupport {

    protected ApiCheckHistoryRepository apiCheckHistoryRepository;
    protected ApiCurrentStatusRepository apiCurrentStatusRepository;
    protected MonitoredApiService monitoredApiService;
    protected ApiHealthClient apiHealthClient;
    protected ApiCheckHistoryPolicy apiCheckHistoryPolicy;
    protected ApiStatusClassifier apiStatusClassifier;
    protected ApiCheckService apiCheckService;
    protected RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        apiCheckHistoryRepository = mock(ApiCheckHistoryRepository.class);
        apiCurrentStatusRepository = mock(ApiCurrentStatusRepository.class);
        monitoredApiService = mock(MonitoredApiService.class);
        restTemplate = new RestTemplate();
        apiHealthClient = new ApiHealthClient(restTemplate);

        apiCheckHistoryPolicy = new ApiCheckHistoryPolicy();
        apiStatusClassifier = new ApiStatusClassifier();

        apiCheckService = new ApiCheckService(
                apiCheckHistoryRepository,
                apiCurrentStatusRepository,
                monitoredApiService,
                apiHealthClient,
                apiCheckHistoryPolicy,
                apiStatusClassifier);
    }

    protected MonitoredApi createApi(Long id) {
        MonitoredApi api = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        api.setId(id);
        api.setSlowThresholdMs(3000L);
        return api;
    }

    protected ApiCurrentStatus createCurrentStatus(MonitoredApi api, CheckStatus status) {
        ApiCurrentStatus currentStatus = new ApiCurrentStatus();
        currentStatus.setId(1L);
        currentStatus.setMonitoredApi(api);
        currentStatus.setStatus(status);
        currentStatus.setAvailable(status != CheckStatus.DOWN);
        currentStatus.setStatusCode(status == CheckStatus.DOWN ? 500 : 200);
        currentStatus.setResponseTimeMs(status == CheckStatus.SLOW ? api.getSlowThresholdMs() + 1 : 100L);
        currentStatus.setCheckedAt(LocalDateTime.now());
        currentStatus.setErrorMessage(status == CheckStatus.DOWN ? "Erro ao acessar API" : null);
        return currentStatus;
    }

    protected ApiCheckHistory createHistory(MonitoredApi api, CheckStatus status) {
        ApiCheckHistory history = new ApiCheckHistory();
        history.setId(1L);
        history.setMonitoredApi(api);
        history.setStatus(status);
        history.setAvailable(status != CheckStatus.DOWN);
        history.setStatusCode(status == CheckStatus.DOWN ? 500 : 200);
        history.setResponseTimeMs(status == CheckStatus.SLOW ? api.getSlowThresholdMs() + 1 : 100L);
        history.setCheckedAt(LocalDateTime.now());
        history.setErrorMessage(status == CheckStatus.DOWN ? "Erro ao acessar API" : null);
        return history;
    }
}
