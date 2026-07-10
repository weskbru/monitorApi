package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.MonitoredApiInactiveException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class ApiCheckServiceFailureTest extends ApiCheckServiceTestSupport {

    @Test
    void shouldReturnDownWhenCheckReceivesErrorResponse() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        when(monitoredApiService.getById(id)).thenReturn(api);

        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(api.getUrl())).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ApiCheckResponse response = apiCheckService.check(id);

        assertFalse(response.getAvailable());
        assertEquals(CheckStatus.DOWN, response.getStatus());
        assertEquals(500, response.getStatusCode());

        ArgumentCaptor<ApiCheckHistory> historyCaptor = ArgumentCaptor.forClass(ApiCheckHistory.class);
        verify(apiCheckHistoryRepository).save(historyCaptor.capture());
        assertFalse(historyCaptor.getValue().getAvailable());
        assertEquals(CheckStatus.DOWN, historyCaptor.getValue().getStatus());

        ArgumentCaptor<ApiCurrentStatus> statusCaptor = ArgumentCaptor.forClass(ApiCurrentStatus.class);
        verify(apiCurrentStatusRepository).save(statusCaptor.capture());
        assertFalse(statusCaptor.getValue().getAvailable());
        assertEquals(CheckStatus.DOWN, statusCaptor.getValue().getStatus());

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenMonitoredApiIsInactive() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        api.setActive(false);

        when(monitoredApiService.getById(id)).thenReturn(api);

        assertThrows(MonitoredApiInactiveException.class, () -> apiCheckService.check(id));
        verify(apiCheckHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any(ApiCheckHistory.class));
        verify(apiCurrentStatusRepository, never()).save(org.mockito.ArgumentMatchers.any(ApiCurrentStatus.class));
    }
}
