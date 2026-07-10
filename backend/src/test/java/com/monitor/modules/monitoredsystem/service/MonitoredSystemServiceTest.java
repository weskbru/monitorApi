package com.monitor.modules.monitoredsystem.service;

import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.modules.monitoredsystem.exception.MonitoredSystemNotFoundException;
import com.monitor.modules.monitoredsystem.repository.MonitoredSystemRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonitoredSystemServiceTest {

    @Test
    void shouldCreateMonitoredSystem() {
        MonitoredSystemRepository monitoredSystemRepository = mock(MonitoredSystemRepository.class);
        MonitoredSystemService monitoredSystemService = new MonitoredSystemService(monitoredSystemRepository);

        MonitoredSystem savedSystem = new MonitoredSystem("Sistema Financeiro", "https://financeiro.empresa.com");
        savedSystem.setId(1L);
        savedSystem.setDescription("Sistema de pagamentos");

        when(monitoredSystemRepository.save(org.mockito.ArgumentMatchers.any(MonitoredSystem.class)))
                .thenReturn(savedSystem);

        MonitoredSystem result = monitoredSystemService.create(
                "Sistema Financeiro",
                "https://financeiro.empresa.com",
                "Sistema de pagamentos");

        assertEquals(savedSystem, result);

        ArgumentCaptor<MonitoredSystem> systemCaptor = ArgumentCaptor.forClass(MonitoredSystem.class);
        verify(monitoredSystemRepository).save(systemCaptor.capture());

        MonitoredSystem systemToSave = systemCaptor.getValue();
        assertEquals("Sistema Financeiro", systemToSave.getName());
        assertEquals("https://financeiro.empresa.com", systemToSave.getBaseUrl());
        assertEquals("Sistema de pagamentos", systemToSave.getDescription());
        assertTrue(systemToSave.getActive());
    }

    @Test
    void shouldListAllMonitoredSystems() {
        MonitoredSystemRepository monitoredSystemRepository = mock(MonitoredSystemRepository.class);
        MonitoredSystemService monitoredSystemService = new MonitoredSystemService(monitoredSystemRepository);

        List<MonitoredSystem> systems = List.of(
                new MonitoredSystem("Financeiro", "https://financeiro.empresa.com"),
                new MonitoredSystem("Comercial", "https://comercial.empresa.com"));

        when(monitoredSystemRepository.findAll()).thenReturn(systems);

        List<MonitoredSystem> result = monitoredSystemService.listAll();

        assertEquals(systems, result);
    }

    @Test
    void shouldReturnMonitoredSystemWhenItExists() {
        MonitoredSystemRepository monitoredSystemRepository = mock(MonitoredSystemRepository.class);
        MonitoredSystemService monitoredSystemService = new MonitoredSystemService(monitoredSystemRepository);

        Long id = 1L;
        MonitoredSystem system = new MonitoredSystem("Sistema Financeiro", "https://financeiro.empresa.com");
        system.setId(id);

        when(monitoredSystemRepository.findById(id)).thenReturn(Optional.of(system));

        MonitoredSystem result = monitoredSystemService.getById(id);

        assertEquals(system, result);
    }

    @Test
    void shouldThrowExceptionWhenMonitoredSystemDoesNotExist() {
        MonitoredSystemRepository monitoredSystemRepository = mock(MonitoredSystemRepository.class);
        MonitoredSystemService monitoredSystemService = new MonitoredSystemService(monitoredSystemRepository);

        Long nonExistentId = 999L;

        when(monitoredSystemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(MonitoredSystemNotFoundException.class, () -> monitoredSystemService.getById(nonExistentId));
    }

    @Test
    void shouldUpdateMonitoredSystemWhenItExists() {
        MonitoredSystemRepository monitoredSystemRepository = mock(MonitoredSystemRepository.class);
        MonitoredSystemService monitoredSystemService = new MonitoredSystemService(monitoredSystemRepository);

        Long id = 1L;
        MonitoredSystem system = new MonitoredSystem("Financeiro", "https://financeiro.empresa.com");
        system.setId(id);

        MonitoredSystem savedSystem = new MonitoredSystem("Financeiro Atualizado", "https://financeiro-novo.empresa.com");
        savedSystem.setId(id);
        savedSystem.setDescription("Descricao atualizada");

        when(monitoredSystemRepository.findById(id)).thenReturn(Optional.of(system));
        when(monitoredSystemRepository.save(system)).thenReturn(savedSystem);

        MonitoredSystem result = monitoredSystemService.update(
                id,
                "Financeiro Atualizado",
                "https://financeiro-novo.empresa.com",
                "Descricao atualizada");

        assertEquals(savedSystem, result);
        assertEquals("Financeiro Atualizado", system.getName());
        assertEquals("https://financeiro-novo.empresa.com", system.getBaseUrl());
        assertEquals("Descricao atualizada", system.getDescription());
        verify(monitoredSystemRepository).save(system);
    }

    @Test
    void shouldUpdateActiveStatusWhenMonitoredSystemExists() {
        MonitoredSystemRepository monitoredSystemRepository = mock(MonitoredSystemRepository.class);
        MonitoredSystemService monitoredSystemService = new MonitoredSystemService(monitoredSystemRepository);

        Long id = 1L;
        MonitoredSystem system = new MonitoredSystem("Financeiro", "https://financeiro.empresa.com");
        system.setId(id);

        when(monitoredSystemRepository.findById(id)).thenReturn(Optional.of(system));
        when(monitoredSystemRepository.save(system)).thenReturn(system);

        MonitoredSystem result = monitoredSystemService.updateActive(id, false);

        assertEquals(system, result);
        assertFalse(system.getActive());
        verify(monitoredSystemRepository).save(system);
    }

    @Test
    void shouldDeleteMonitoredSystemWhenItExists() {
        MonitoredSystemRepository monitoredSystemRepository = mock(MonitoredSystemRepository.class);
        MonitoredSystemService monitoredSystemService = new MonitoredSystemService(monitoredSystemRepository);

        Long id = 1L;
        MonitoredSystem system = new MonitoredSystem("Financeiro", "https://financeiro.empresa.com");
        system.setId(id);

        when(monitoredSystemRepository.findById(id)).thenReturn(Optional.of(system));

        monitoredSystemService.delete(id);

        verify(monitoredSystemRepository).delete(system);
    }
}
