package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.dto.ApiCurrentStatusResponse;
import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.ApiCheckHistoryNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ApiCurrentStatusServiceTest extends ApiCheckServiceTestSupport {

    @Test
    void shouldReturnCurrentStatusWhenItExists() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        ApiCurrentStatus currentStatus = createCurrentStatus(api, CheckStatus.SLOW);
        currentStatus.setResponseTimeMs(3500L);

        when(monitoredApiService.getById(id)).thenReturn(api);
        when(apiCurrentStatusRepository.findByMonitoredApiId(id)).thenReturn(Optional.of(currentStatus));

        ApiCurrentStatusResponse response = apiCheckService.getCurrentStatus(id);

        assertEquals(currentStatus.getId(), response.getId());
        assertEquals(api.getId(), response.getMonitoredApiId());
        assertEquals(api.getName(), response.getMonitoredApiName());
        assertEquals(api.getUrl(), response.getMonitoredApiUrl());
        assertEquals(CheckStatus.SLOW, response.getStatus());
        assertEquals("O endpoint respondeu, mas está lento.", response.getMessage());
        assertTrue(response.getAvailable());
        assertEquals(200, response.getStatusCode());
        assertEquals(3500L, response.getResponseTimeMs());
        assertEquals(currentStatus.getCheckedAt(), response.getCheckedAt());
        assertNull(response.getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenCurrentStatusDoesNotExist() {
        Long id = 1L;
        MonitoredApi api = createApi(id);

        when(monitoredApiService.getById(id)).thenReturn(api);
        when(apiCurrentStatusRepository.findByMonitoredApiId(id)).thenReturn(Optional.empty());

        assertThrows(ApiCheckHistoryNotFoundException.class, () -> apiCheckService.getCurrentStatus(id));
    }
}
