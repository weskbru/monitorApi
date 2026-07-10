package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.MonitoredApiNotFoundException;
import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.modules.monitoredsystem.service.MonitoredSystemService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonitoredApiServiceTest {

    @Test
    void shouldCreateMonitoredApi() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);
        MonitoredSystem system = createSystem(1L);

        MonitoredApi savedApi = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        savedApi.setId(1L);
        savedApi.setDescription("API publica de CEP");
        savedApi.setMonitoredSystem(system);

        when(monitoredSystemService.getById(1L)).thenReturn(system);
        when(monitoredApiRepository.save(org.mockito.ArgumentMatchers.any(MonitoredApi.class)))
                .thenReturn(savedApi);

        MonitoredApi result = monitoredApiService.create(
                1L,
                "ViaCEP",
                "https://viacep.com.br/ws/01001000/json/",
                "API publica de CEP"
        );

        assertEquals(savedApi, result);

        ArgumentCaptor<MonitoredApi> apiCaptor = ArgumentCaptor.forClass(MonitoredApi.class);
        verify(monitoredApiRepository).save(apiCaptor.capture());

        MonitoredApi apiToSave = apiCaptor.getValue();
        assertEquals("ViaCEP", apiToSave.getName());
        assertEquals("https://viacep.com.br/ws/01001000/json/", apiToSave.getUrl());
        assertEquals("API publica de CEP", apiToSave.getDescription());
        assertEquals(system, apiToSave.getMonitoredSystem());
        assertTrue(apiToSave.getActive());
    }

    @Test
    void shouldListAllMonitoredApis() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);

        List<MonitoredApi> apis = List.of(
                new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/"),
                new MonitoredApi("HttpBin", "https://httpbin.org")
        );

        when(monitoredApiRepository.findAll()).thenReturn(apis);

        List<MonitoredApi> result = monitoredApiService.listAll();

        assertEquals(apis, result);
    }

    @Test
    void shouldThrowExceptionWhenMonitoredApiDoesNotExist() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);

        Long nonExistentId = 999L;

        when(monitoredApiRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(MonitoredApiNotFoundException.class, () -> {
            monitoredApiService.getById(nonExistentId);
        });
    }

    @Test
    void shouldReturnMonitoredApiWhenItExists() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);

        Long id = 1L;
        MonitoredApi api = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        api.setId(id);

        when(monitoredApiRepository.findById(id)).thenReturn(Optional.of(api));

        MonitoredApi result = monitoredApiService.getById(id);

        assertEquals(api, result);
    }

    @Test
    void shouldDeleteMonitoredApiWhenItExists() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);

        Long id = 1L;
        MonitoredApi api = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        api.setId(id);

        when(monitoredApiRepository.findById(id)).thenReturn(Optional.of(api));

        monitoredApiService.delete(id);

        verify(monitoredApiRepository).delete(api);
    }

    @Test
    void shouldUpdateMonitoredApiWhenItExists() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);

        Long id = 1L;
        MonitoredSystem system = createSystem(1L);
        MonitoredApi api = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        api.setId(id);

        MonitoredApi savedApi = new MonitoredApi("ViaCEP Atualizada", "https://viacep.com.br/ws/01001000/json/");
        savedApi.setId(id);
        savedApi.setDescription("Descricao atualizada");
        savedApi.setMonitoredSystem(system);

        when(monitoredApiRepository.findById(id)).thenReturn(Optional.of(api));
        when(monitoredSystemService.getById(1L)).thenReturn(system);
        when(monitoredApiRepository.save(api)).thenReturn(savedApi);

        MonitoredApi result = monitoredApiService.update(
                id,
                1L,
                "ViaCEP Atualizada",
                "https://viacep.com.br/ws/01001000/json/",
                "Descricao atualizada"
        );

        assertEquals(savedApi, result);
        assertEquals("ViaCEP Atualizada", api.getName());
        assertEquals("https://viacep.com.br/ws/01001000/json/", api.getUrl());
        assertEquals("Descricao atualizada", api.getDescription());
        assertEquals(system, api.getMonitoredSystem());
        verify(monitoredApiRepository).save(api);
    }

    @Test
    void shouldUpdateActiveStatusWhenMonitoredApiExists() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);

        Long id = 1L;
        MonitoredApi api = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        api.setId(id);

        when(monitoredApiRepository.findById(id)).thenReturn(Optional.of(api));
        when(monitoredApiRepository.save(api)).thenReturn(api);

        MonitoredApi result = monitoredApiService.updateActive(id, false);

        assertEquals(api, result);
        assertFalse(api.getActive());
        verify(monitoredApiRepository).save(api);
    }

    @Test
    void shouldListMonitoredApisBySystem() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);

        Long systemId = 1L;
        MonitoredSystem system = createSystem(systemId);
        List<MonitoredApi> apis = List.of(
                new MonitoredApi("Login", "https://financeiro.empresa.com/api/auth/status"),
                new MonitoredApi("Pagamentos", "https://financeiro.empresa.com/api/payments/health")
        );

        when(monitoredSystemService.getById(systemId)).thenReturn(system);
        when(monitoredApiRepository.findByMonitoredSystemId(systemId)).thenReturn(apis);

        List<MonitoredApi> result = monitoredApiService.listBySystemId(systemId);

        assertEquals(apis, result);
        verify(monitoredSystemService).getById(systemId);
    }

    @Test
    void shouldReturnMonitoredApiBySystemAndApiId() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);

        Long systemId = 1L;
        Long apiId = 10L;
        MonitoredSystem system = createSystem(systemId);
        MonitoredApi api = new MonitoredApi("Login", "https://financeiro.empresa.com/api/auth/status");
        api.setId(apiId);
        api.setMonitoredSystem(system);

        when(monitoredSystemService.getById(systemId)).thenReturn(system);
        when(monitoredApiRepository.findByIdAndMonitoredSystemId(apiId, systemId)).thenReturn(Optional.of(api));

        MonitoredApi result = monitoredApiService.getBySystemIdAndApiId(systemId, apiId);

        assertEquals(api, result);
        verify(monitoredSystemService).getById(systemId);
    }

    @Test
    void shouldThrowExceptionWhenApiDoesNotBelongToSystem() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredSystemService monitoredSystemService = mock(MonitoredSystemService.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository, monitoredSystemService);

        Long systemId = 1L;
        Long apiId = 999L;
        MonitoredSystem system = createSystem(systemId);

        when(monitoredSystemService.getById(systemId)).thenReturn(system);
        when(monitoredApiRepository.findByIdAndMonitoredSystemId(apiId, systemId)).thenReturn(Optional.empty());

        assertThrows(MonitoredApiNotFoundException.class, () -> {
            monitoredApiService.getBySystemIdAndApiId(systemId, apiId);
        });
    }

    private MonitoredSystem createSystem(Long id) {
        MonitoredSystem system = new MonitoredSystem("Sistema Financeiro", "https://financeiro.empresa.com");
        system.setId(id);
        return system;
    }
}
