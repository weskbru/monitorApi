package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.dto.ApiCheckHistoryResponse;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ApiCheckHistoryServiceTest extends ApiCheckServiceTestSupport {

    @Test
    void shouldSaveHistoryWhenStatusCodeCategoryChanges() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        ApiCurrentStatus currentStatus = createCurrentStatus(api, CheckStatus.DOWN);
        currentStatus.setStatusCode(500);
        currentStatus.setAvailable(false);
        currentStatus.setCheckedAt(LocalDateTime.now());

        when(monitoredApiService.getById(id)).thenReturn(api);
        when(apiCurrentStatusRepository.findByMonitoredApiId(id)).thenReturn(Optional.of(currentStatus));

        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(api.getUrl())).andRespond(withStatus(HttpStatus.FOUND));

        ApiCheckResponse response = apiCheckService.check(id);

        assertEquals(CheckStatus.DOWN, response.getStatus());
        assertEquals(302, response.getStatusCode());
        assertSavedHistoryStatusCode(302);
        server.verify();
    }

    @Test
    void shouldSaveHistoryWhenErrorMessageChanges() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        ApiCurrentStatus currentStatus = createCurrentStatus(api, CheckStatus.DOWN);
        currentStatus.setStatusCode(null);
        currentStatus.setAvailable(false);
        currentStatus.setErrorMessage("Erro anterior");
        currentStatus.setCheckedAt(LocalDateTime.now());

        when(monitoredApiService.getById(id)).thenReturn(api);
        when(apiCurrentStatusRepository.findByMonitoredApiId(id)).thenReturn(Optional.of(currentStatus));

        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(api.getUrl())).andRespond(request -> {
            throw new org.springframework.web.client.ResourceAccessException("Falha nova");
        });

        ApiCheckResponse response = apiCheckService.check(id);

        assertEquals(CheckStatus.DOWN, response.getStatus());
        assertEquals("Timeout ou falha de conexão ao acessar a API.", response.getErrorMessage());
        assertSavedHistoryError("Timeout ou falha de conexão ao acessar a API.");
        server.verify();
    }

    @Test
    void shouldSaveHistoryWhenOneHourPassedSinceCurrentStatus() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        ApiCurrentStatus currentStatus = createCurrentStatus(api, CheckStatus.UP);
        currentStatus.setCheckedAt(LocalDateTime.now().minusHours(1));

        when(monitoredApiService.getById(id)).thenReturn(api);
        when(apiCurrentStatusRepository.findByMonitoredApiId(id)).thenReturn(Optional.of(currentStatus));

        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(api.getUrl())).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ApiCheckResponse response = apiCheckService.check(id);

        assertEquals(CheckStatus.UP, response.getStatus());
        assertEquals(200, response.getStatusCode());
        verify(apiCheckHistoryRepository).save(org.mockito.ArgumentMatchers.any(ApiCheckHistory.class));
        server.verify();
    }

    @Test
    void shouldReturnHistory() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        ApiCheckHistory history = createHistory(api, CheckStatus.UP);

        when(monitoredApiService.getById(id)).thenReturn(api);
        when(apiCheckHistoryRepository.findByMonitoredApiIdOrderByCheckedAtDesc(id)).thenReturn(List.of(history));

        List<ApiCheckHistoryResponse> response = apiCheckService.getHistory(id);

        assertEquals(1, response.size());
        assertEquals(history.getId(), response.get(0).getId());
        assertEquals(CheckStatus.UP, response.get(0).getStatus());
        assertTrue(response.get(0).getAvailable());
        assertEquals(200, response.get(0).getStatusCode());
        assertNull(response.get(0).getErrorMessage());
    }

    private void assertSavedHistoryStatusCode(Integer statusCode) {
        ArgumentCaptor<ApiCheckHistory> captor = ArgumentCaptor.forClass(ApiCheckHistory.class);
        verify(apiCheckHistoryRepository).save(captor.capture());
        assertEquals(statusCode, captor.getValue().getStatusCode());
    }

    private void assertSavedHistoryError(String errorMessage) {
        ArgumentCaptor<ApiCheckHistory> captor = ArgumentCaptor.forClass(ApiCheckHistory.class);
        verify(apiCheckHistoryRepository).save(captor.capture());
        assertEquals(errorMessage, captor.getValue().getErrorMessage());
    }
}
