package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.dto.ApiCheckHistoryResponse;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.entity.ApiCheckHistory;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.ApiCheckHistoryNotFoundException;
import com.monitor.modules.monitoredapi.exception.MonitoredApiInactiveException;
import com.monitor.modules.monitoredapi.repository.ApiCheckHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ApiCheckServiceTest {

    private ApiCheckHistoryRepository apiCheckHistoryRepository;
    private MonitoredApiService monitoredApiService;
    private ApiCheckService apiCheckService;

    @BeforeEach
    void setUp() {
        apiCheckHistoryRepository = mock(ApiCheckHistoryRepository.class);
        monitoredApiService = mock(MonitoredApiService.class);
        apiCheckService = new ApiCheckService(
                apiCheckHistoryRepository,
                monitoredApiService
        );
    }

    @Test
    void shouldReturnUpWhenCheckSucceeds() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        when(monitoredApiService.getById(id)).thenReturn(api);

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(apiCheckService, "restTemplate");
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

        ApiCheckHistory history = historyCaptor.getValue();
        assertEquals(api, history.getMonitoredApi());
        assertTrue(history.getAvailable());
        assertEquals(CheckStatus.UP, history.getStatus());
        assertEquals(200, history.getStatusCode());
        assertNull(history.getErrorMessage());

        server.verify();
    }

    @Test
    void shouldReturnDownWhenCheckReceivesErrorResponse() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        when(monitoredApiService.getById(id)).thenReturn(api);

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(apiCheckService, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo(api.getUrl()))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ApiCheckResponse response = apiCheckService.check(id);

        assertFalse(response.getAvailable());
        assertEquals(CheckStatus.DOWN, response.getStatus());
        assertEquals(500, response.getStatusCode());

        ArgumentCaptor<ApiCheckHistory> historyCaptor = ArgumentCaptor.forClass(ApiCheckHistory.class);
        verify(apiCheckHistoryRepository).save(historyCaptor.capture());

        ApiCheckHistory history = historyCaptor.getValue();
        assertFalse(history.getAvailable());
        assertEquals(CheckStatus.DOWN, history.getStatus());
        assertEquals(500, history.getStatusCode());

        server.verify();
    }

    @Test
    void shouldThrowExceptionWhenMonitoredApiIsInactive() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        api.setActive(false);

        when(monitoredApiService.getById(id)).thenReturn(api);

        assertThrows(MonitoredApiInactiveException.class, () -> {
            apiCheckService.check(id);
        });

        verify(apiCheckHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any(ApiCheckHistory.class));
    }

    @Test
    void shouldReturnHistory() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        ApiCheckHistory history = createHistory(api, CheckStatus.UP);

        when(monitoredApiService.getById(id)).thenReturn(api);
        when(apiCheckHistoryRepository.findByMonitoredApiIdOrderByCheckedAtDesc(id))
                .thenReturn(List.of(history));

        List<ApiCheckHistoryResponse> response = apiCheckService.getHistory(id);

        assertEquals(1, response.size());
        assertEquals(history.getId(), response.get(0).getId());
        assertEquals(CheckStatus.UP, response.get(0).getStatus());
        assertEquals("O endpoint respondeu normalmente.", response.get(0).getMessage());
        assertTrue(response.get(0).getAvailable());
        assertEquals(200, response.get(0).getStatusCode());
        assertEquals(120L, response.get(0).getResponseTimeMs());
        assertEquals(history.getCheckedAt(), response.get(0).getCheckedAt());
        assertNull(response.get(0).getErrorMessage());
    }

    @Test
    void shouldReturnCurrentStatusWhenItExists() {
        Long id = 1L;
        MonitoredApi api = createApi(id);
        ApiCheckHistory history = createHistory(api, CheckStatus.SLOW);
        history.setResponseTimeMs(3500L);

        when(monitoredApiService.getById(id)).thenReturn(api);
        when(apiCheckHistoryRepository.findFirstByMonitoredApiIdOrderByCheckedAtDesc(id))
                .thenReturn(Optional.of(history));

        ApiCheckHistoryResponse response = apiCheckService.getCurrentStatus(id);

        assertEquals(history.getId(), response.getId());
        assertEquals(CheckStatus.SLOW, response.getStatus());
        assertEquals("O endpoint respondeu, mas está lento.", response.getMessage());
        assertTrue(response.getAvailable());
        assertEquals(200, response.getStatusCode());
        assertEquals(3500L, response.getResponseTimeMs());
        assertEquals(history.getCheckedAt(), response.getCheckedAt());
        assertNull(response.getErrorMessage());
    }

    @Test
    void shouldThrowExceptionWhenCurrentStatusDoesNotExist() {
        Long id = 1L;

        MonitoredApi api = createApi(id);

        when(monitoredApiService.getById(id)).thenReturn(api);

        when(apiCheckHistoryRepository.findFirstByMonitoredApiIdOrderByCheckedAtDesc(id))
                .thenReturn(Optional.empty());

        assertThrows(ApiCheckHistoryNotFoundException.class, () -> {
            apiCheckService.getCurrentStatus(id);
        });
    }

    private MonitoredApi createApi(Long id) {
        MonitoredApi api = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        api.setId(id);
        return api;
    }

    private ApiCheckHistory createHistory(MonitoredApi api, CheckStatus status) {
        ApiCheckHistory history = new ApiCheckHistory();
        history.setId(10L);
        history.setMonitoredApi(api);
        history.setStatus(status);
        history.setAvailable(true);
        history.setStatusCode(200);
        history.setResponseTimeMs(120L);
        history.setCheckedAt(LocalDateTime.of(2026, 6, 27, 10, 0));
        history.setErrorMessage(null);
        return history;
    }
}
