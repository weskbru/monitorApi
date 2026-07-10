package com.monitor.modules.monitoredsystem.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.repository.ApiCurrentStatusRepository;
import com.monitor.modules.monitoredapi.service.MonitoredApiService;
import com.monitor.modules.monitoredsystem.dto.MonitoredSystemStatusResponse;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonitoredSystemStatusServiceTest {

    @Test
    void shouldReturnDownWhenAnyActiveEndpointIsDown() {
        TestContext context = createContext();
        MonitoredApi login = createApi(10L, "Login");
        MonitoredApi payments = createApi(11L, "Pagamentos");

        when(context.monitoredApiService.listBySystemId(1L)).thenReturn(List.of(login, payments));
        when(context.apiCurrentStatusRepository.findByMonitoredApiMonitoredSystemId(1L))
                .thenReturn(List.of(
                        createStatus(login, CheckStatus.UP, LocalDateTime.of(2026, 7, 9, 10, 0)),
                        createStatus(payments, CheckStatus.DOWN, LocalDateTime.of(2026, 7, 9, 10, 5))));

        MonitoredSystemStatusResponse response = context.service.getStatus(1L);

        assertEquals(CheckStatus.DOWN, response.getStatus());
        assertEquals("Existe endpoint critico indisponivel.", response.getMessage());
        assertEquals(2, response.getTotalEndpoints());
        assertEquals(1, response.getUpEndpoints());
        assertEquals(1, response.getDownEndpoints());
        assertEquals(LocalDateTime.of(2026, 7, 9, 10, 5), response.getLastCheckedAt());
    }

    @Test
    void shouldReturnSlowWhenNoEndpointIsDownButOneIsSlow() {
        TestContext context = createContext();
        MonitoredApi login = createApi(10L, "Login");
        MonitoredApi users = createApi(11L, "Usuarios");

        when(context.monitoredApiService.listBySystemId(1L)).thenReturn(List.of(login, users));
        when(context.apiCurrentStatusRepository.findByMonitoredApiMonitoredSystemId(1L))
                .thenReturn(List.of(
                        createStatus(login, CheckStatus.UP, LocalDateTime.of(2026, 7, 9, 10, 0)),
                        createStatus(users, CheckStatus.SLOW, LocalDateTime.of(2026, 7, 9, 10, 2))));

        MonitoredSystemStatusResponse response = context.service.getStatus(1L);

        assertEquals(CheckStatus.SLOW, response.getStatus());
        assertEquals(1, response.getUpEndpoints());
        assertEquals(1, response.getSlowEndpoints());
        assertEquals(0, response.getDownEndpoints());
    }

    @Test
    void shouldReturnUpWhenAllActiveEndpointsAreUp() {
        TestContext context = createContext();
        MonitoredApi login = createApi(10L, "Login");
        MonitoredApi users = createApi(11L, "Usuarios");

        when(context.monitoredApiService.listBySystemId(1L)).thenReturn(List.of(login, users));
        when(context.apiCurrentStatusRepository.findByMonitoredApiMonitoredSystemId(1L))
                .thenReturn(List.of(
                        createStatus(login, CheckStatus.UP, LocalDateTime.of(2026, 7, 9, 10, 0)),
                        createStatus(users, CheckStatus.UP, LocalDateTime.of(2026, 7, 9, 10, 1))));

        MonitoredSystemStatusResponse response = context.service.getStatus(1L);

        assertEquals(CheckStatus.UP, response.getStatus());
        assertEquals(2, response.getUpEndpoints());
        assertEquals(0, response.getUnknownEndpoints());
    }

    @Test
    void shouldReturnUnknownWhenThereAreNoActiveEndpoints() {
        TestContext context = createContext();

        when(context.monitoredApiService.listBySystemId(1L)).thenReturn(List.of());
        when(context.apiCurrentStatusRepository.findByMonitoredApiMonitoredSystemId(1L)).thenReturn(List.of());

        MonitoredSystemStatusResponse response = context.service.getStatus(1L);

        assertEquals(CheckStatus.UNKNOWN, response.getStatus());
        assertEquals(0, response.getTotalEndpoints());
        assertNull(response.getLastCheckedAt());
    }

    @Test
    void shouldReturnUnknownWhenAnyActiveEndpointHasNoCurrentStatus() {
        TestContext context = createContext();
        MonitoredApi login = createApi(10L, "Login");
        MonitoredApi users = createApi(11L, "Usuarios");

        when(context.monitoredApiService.listBySystemId(1L)).thenReturn(List.of(login, users));
        when(context.apiCurrentStatusRepository.findByMonitoredApiMonitoredSystemId(1L))
                .thenReturn(List.of(createStatus(login, CheckStatus.UP, LocalDateTime.of(2026, 7, 9, 10, 0))));

        MonitoredSystemStatusResponse response = context.service.getStatus(1L);

        assertEquals(CheckStatus.UNKNOWN, response.getStatus());
        assertEquals(1, response.getUpEndpoints());
        assertEquals(1, response.getUnknownEndpoints());
    }

    @Test
    void shouldReturnDownWhenOneEndpointIsDownEvenIfAnotherHasNoCurrentStatus() {
        TestContext context = createContext();
        MonitoredApi login = createApi(10L, "Login");
        MonitoredApi payments = createApi(11L, "Pagamentos");
        MonitoredApi users = createApi(12L, "Usuarios");

        when(context.monitoredApiService.listBySystemId(1L)).thenReturn(List.of(login, payments, users));
        when(context.apiCurrentStatusRepository.findByMonitoredApiMonitoredSystemId(1L))
                .thenReturn(List.of(
                        createStatus(login, CheckStatus.UP, LocalDateTime.of(2026, 7, 9, 10, 0)),
                        createStatus(payments, CheckStatus.DOWN, LocalDateTime.of(2026, 7, 9, 10, 5))));

        MonitoredSystemStatusResponse response = context.service.getStatus(1L);

        assertEquals(CheckStatus.DOWN, response.getStatus());
        assertEquals(1, response.getUpEndpoints());
        assertEquals(1, response.getDownEndpoints());
        assertEquals(1, response.getUnknownEndpoints());
    }

    @Test
    void shouldIgnoreInactiveEndpointsWhenCalculatingStatus() {
        TestContext context = createContext();
        MonitoredApi login = createApi(10L, "Login");
        MonitoredApi disabledApi = createApi(11L, "Relatorios");
        disabledApi.setActive(false);

        when(context.monitoredApiService.listBySystemId(1L)).thenReturn(List.of(login, disabledApi));
        when(context.apiCurrentStatusRepository.findByMonitoredApiMonitoredSystemId(1L))
                .thenReturn(List.of(
                        createStatus(login, CheckStatus.UP, LocalDateTime.of(2026, 7, 9, 10, 0)),
                        createStatus(disabledApi, CheckStatus.DOWN, LocalDateTime.of(2026, 7, 9, 10, 1))));

        MonitoredSystemStatusResponse response = context.service.getStatus(1L);

        assertEquals(CheckStatus.UP, response.getStatus());
        assertEquals(1, response.getTotalEndpoints());
        assertEquals(1, response.getUpEndpoints());
        assertEquals(0, response.getDownEndpoints());
        assertEquals(LocalDateTime.of(2026, 7, 9, 10, 0), response.getLastCheckedAt());
    }

    private TestContext createContext() {
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = mock(MonitoredApiService.class);
        ApiCurrentStatusRepository apiCurrentStatusRepository = mock(ApiCurrentStatusRepository.class);

        MonitoredSystem system = new MonitoredSystem("Sistema Financeiro", "https://financeiro.empresa.com");
        system.setId(1L);

        when(monitoredSystemService.getById(1L)).thenReturn(system);

        MonitoredSystemStatusService service = new MonitoredSystemStatusService(
                monitoredSystemService,
                monitoredApiService,
                apiCurrentStatusRepository);

        return new TestContext(service, monitoredApiService, apiCurrentStatusRepository);
    }

    private MonitoredApi createApi(Long id, String name) {
        MonitoredApi api = new MonitoredApi(name, "https://financeiro.empresa.com/" + name.toLowerCase());
        api.setId(id);
        return api;
    }

    private ApiCurrentStatus createStatus(MonitoredApi api, CheckStatus status, LocalDateTime checkedAt) {
        ApiCurrentStatus currentStatus = new ApiCurrentStatus();
        currentStatus.setMonitoredApi(api);
        currentStatus.setStatus(status);
        currentStatus.setCheckedAt(checkedAt);
        return currentStatus;
    }

    private record TestContext(
            MonitoredSystemStatusService service,
            MonitoredApiService monitoredApiService,
            ApiCurrentStatusRepository apiCurrentStatusRepository) {
    }
}
