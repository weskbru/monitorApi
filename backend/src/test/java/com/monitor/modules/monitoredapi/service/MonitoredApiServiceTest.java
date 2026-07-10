package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.MonitoredApiNotFoundException;
import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
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
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository);

        MonitoredApi savedApi = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        savedApi.setId(1L);
        savedApi.setDescription("API publica de CEP");

        when(monitoredApiRepository.save(org.mockito.ArgumentMatchers.any(MonitoredApi.class)))
                .thenReturn(savedApi);

        MonitoredApi result = monitoredApiService.create(
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
        assertTrue(apiToSave.getActive());
    }

    @Test
    void shouldListAllMonitoredApis() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository);

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
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository);

        Long nonExistentId = 999L;

        when(monitoredApiRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(MonitoredApiNotFoundException.class, () -> {
            monitoredApiService.getById(nonExistentId);
        });
    }

    @Test
    void shouldReturnMonitoredApiWhenItExists() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository);

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
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository);

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
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository);

        Long id = 1L;
        MonitoredApi api = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        api.setId(id);

        MonitoredApi savedApi = new MonitoredApi("ViaCEP Atualizada", "https://viacep.com.br/ws/01001000/json/");
        savedApi.setId(id);
        savedApi.setDescription("Descricao atualizada");

        when(monitoredApiRepository.findById(id)).thenReturn(Optional.of(api));
        when(monitoredApiRepository.save(api)).thenReturn(savedApi);

        MonitoredApi result = monitoredApiService.update(
                id,
                "ViaCEP Atualizada",
                "https://viacep.com.br/ws/01001000/json/",
                "Descricao atualizada"
        );

        assertEquals(savedApi, result);
        assertEquals("ViaCEP Atualizada", api.getName());
        assertEquals("https://viacep.com.br/ws/01001000/json/", api.getUrl());
        assertEquals("Descricao atualizada", api.getDescription());
        verify(monitoredApiRepository).save(api);
    }

    @Test
    void shouldUpdateActiveStatusWhenMonitoredApiExists() {
        MonitoredApiRepository monitoredApiRepository = mock(MonitoredApiRepository.class);
        MonitoredApiService monitoredApiService = new MonitoredApiService(monitoredApiRepository);

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
}
