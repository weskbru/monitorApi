package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ApiCheckServiceSuccessTest extends ApiCheckServiceTestSupport {

    @Test
    void shouldReturnUpWhenCheckSucceeds() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        when(monitoredApiService.getById(id)).thenReturn(api);

        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(api.getUrl()))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ApiCheckResponse response = apiCheckService.check(id);

        assertEquals(api.getId(), response.getApiId());
        assertEquals(api.getName(), response.getName());
        assertEquals(api.getUrl(), response.getUrl());
        assertTrue(response.getAvailable());
        assertEquals(CheckStatus.UP, response.getStatus());
        assertEquals(200, response.getStatusCode());
        assertNull(response.getErrorMessage());

        ArgumentCaptor<ApiCheckHistory> historyCaptor = ArgumentCaptor.forClass(ApiCheckHistory.class);
        verify(apiCheckHistoryRepository).save(historyCaptor.capture());
        assertEquals(CheckStatus.UP, historyCaptor.getValue().getStatus());

        ArgumentCaptor<ApiCurrentStatus> statusCaptor = ArgumentCaptor.forClass(ApiCurrentStatus.class);
        verify(apiCurrentStatusRepository).save(statusCaptor.capture());
        assertEquals(CheckStatus.UP, statusCaptor.getValue().getStatus());

        server.verify();
    }

    @Test
    void shouldNotSaveHistoryWhenNothingRelevantChanges() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        ApiCurrentStatus currentStatus = createCurrentStatus(api, CheckStatus.UP);
        currentStatus.setCheckedAt(LocalDateTime.now());

        when(monitoredApiService.getById(id)).thenReturn(api);
        when(apiCurrentStatusRepository.findByMonitoredApiId(id)).thenReturn(Optional.of(currentStatus));

        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(api.getUrl())).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ApiCheckResponse response = apiCheckService.check(id);

        assertEquals(CheckStatus.UP, response.getStatus());
        assertEquals(200, response.getStatusCode());
        verify(apiCheckHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any(ApiCheckHistory.class));
        verify(apiCurrentStatusRepository).save(org.mockito.ArgumentMatchers.any(ApiCurrentStatus.class));

        server.verify();
    }
}
