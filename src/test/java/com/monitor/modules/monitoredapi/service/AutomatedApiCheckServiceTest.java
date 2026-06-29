package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutomatedApiCheckServiceTest {

    @Test
    void shouldCheckAllActiveApis() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        ApiCheckService apiCheckService = mock(ApiCheckService.class);
        AutomatedApiCheckService automatedApiCheckService = new AutomatedApiCheckService(
                monitoredApiRepository,
                apiCheckService
        );

        MonitoredApi firstApi = createApi(1L);
        MonitoredApi secondApi = createApi(2L);

        when(monitoredApiRepository.findByActiveTrue())
                .thenReturn(List.of(firstApi, secondApi));

        automatedApiCheckService.checkActiveApis();

        verify(apiCheckService).check(1L);
        verify(apiCheckService).check(2L);
    }

    @Test
    void shouldContinueCheckingWhenOneApiFails() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        ApiCheckService apiCheckService = mock(ApiCheckService.class);
        AutomatedApiCheckService automatedApiCheckService = new AutomatedApiCheckService(
                monitoredApiRepository,
                apiCheckService
        );

        MonitoredApi firstApi = createApi(1L);
        MonitoredApi secondApi = createApi(2L);

        when(monitoredApiRepository.findByActiveTrue())
                .thenReturn(List.of(firstApi, secondApi));

        doThrow(new RuntimeException("Falha na verificacao"))
                .when(apiCheckService)
                .check(1L);

        automatedApiCheckService.checkActiveApis();

        verify(apiCheckService).check(1L);
        verify(apiCheckService).check(2L);
    }

    @Test
    void shouldNotCheckAnyApiWhenThereAreNoActiveApis() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        ApiCheckService apiCheckService = mock(ApiCheckService.class);
        AutomatedApiCheckService automatedApiCheckService = new AutomatedApiCheckService(
                monitoredApiRepository,
                apiCheckService
        );

        when(monitoredApiRepository.findByActiveTrue())
                .thenReturn(List.of());

        automatedApiCheckService.checkActiveApis();

        verify(apiCheckService, times(0)).check(org.mockito.ArgumentMatchers.anyLong());
    }

    private MonitoredApi createApi(Long id) {
        MonitoredApi api = new MonitoredApi("API " + id, "https://example.com/" + id);
        api.setId(id);
        return api;
    }
}
