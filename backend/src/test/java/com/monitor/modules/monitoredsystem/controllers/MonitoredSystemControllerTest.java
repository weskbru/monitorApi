package com.monitor.modules.monitoredsystem.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredsystem.dto.MonitoredSystemStatusResponse;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.modules.monitoredsystem.exception.MonitoredSystemNotFoundException;
import com.monitor.modules.monitoredsystem.service.MonitoredSystemStatusService;
import com.monitor.modules.monitoredsystem.service.MonitoredSystemService;
import com.monitor.shared.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MonitoredSystemController.class)
@Import(GlobalExceptionHandler.class)
class MonitoredSystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MonitoredSystemService monitoredSystemService;

    @MockitoBean
    private MonitoredSystemStatusService monitoredSystemStatusService;

    @Test
    void shouldCreateMonitoredSystem() throws Exception {
        MonitoredSystem system = createSystem(1L);
        when(monitoredSystemService.create(
                eq("Sistema Financeiro"),
                eq("https://financeiro.empresa.com"),
                eq("Sistema de pagamentos")))
                .thenReturn(system);

        Map<String, String> request = Map.of(
                "name", "Sistema Financeiro",
                "baseUrl", "https://financeiro.empresa.com",
                "description", "Sistema de pagamentos");

        mockMvc.perform(post("/api/monitored-systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sistema Financeiro"))
                .andExpect(jsonPath("$.baseUrl").value("https://financeiro.empresa.com"))
                .andExpect(jsonPath("$.description").value("Sistema de pagamentos"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {
        Map<String, String> request = Map.of(
                "name", "",
                "baseUrl", "financeiro.empresa.com");

        mockMvc.perform(post("/api/monitored-systems")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validacao"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.baseUrl").value("URL base deve começar com http:// ou https://"));
    }

    @Test
    void shouldListAllMonitoredSystems() throws Exception {
        when(monitoredSystemService.listAll()).thenReturn(List.of(createSystem(1L), createSystem(2L)));

        mockMvc.perform(get("/api/monitored-systems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Sistema Financeiro"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldReturnMonitoredSystemById() throws Exception {
        when(monitoredSystemService.getById(1L)).thenReturn(createSystem(1L));

        mockMvc.perform(get("/api/monitored-systems/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sistema Financeiro"));
    }

    @Test
    void shouldReturnMonitoredSystemStatus() throws Exception {
        MonitoredSystemStatusResponse response = new MonitoredSystemStatusResponse(
                1L,
                "Sistema Financeiro",
                CheckStatus.DOWN,
                "Existe endpoint critico indisponivel.",
                4,
                2,
                1,
                1,
                0,
                LocalDateTime.of(2026, 7, 9, 10, 0));

        when(monitoredSystemStatusService.getStatus(1L)).thenReturn(response);

        mockMvc.perform(get("/api/monitored-systems/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemId").value(1))
                .andExpect(jsonPath("$.name").value("Sistema Financeiro"))
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.totalEndpoints").value(4))
                .andExpect(jsonPath("$.downEndpoints").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenMonitoredSystemDoesNotExist() throws Exception {
        when(monitoredSystemService.getById(999L)).thenThrow(new MonitoredSystemNotFoundException(999L));

        mockMvc.perform(get("/api/monitored-systems/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sistema monitorado nao encontrado com id: 999"));
    }

    @Test
    void shouldUpdateMonitoredSystem() throws Exception {
        MonitoredSystem updatedSystem = createSystem(1L);
        updatedSystem.setName("Sistema Financeiro Atualizado");
        updatedSystem.setDescription("Descricao atualizada");

        when(monitoredSystemService.update(
                eq(1L),
                eq("Sistema Financeiro Atualizado"),
                eq("https://financeiro.empresa.com"),
                eq("Descricao atualizada")))
                .thenReturn(updatedSystem);

        Map<String, String> request = Map.of(
                "name", "Sistema Financeiro Atualizado",
                "baseUrl", "https://financeiro.empresa.com",
                "description", "Descricao atualizada");

        mockMvc.perform(put("/api/monitored-systems/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sistema Financeiro Atualizado"))
                .andExpect(jsonPath("$.description").value("Descricao atualizada"));
    }

    @Test
    void shouldUpdateActiveStatus() throws Exception {
        MonitoredSystem updatedSystem = createSystem(1L);
        updatedSystem.setActive(false);

        when(monitoredSystemService.updateActive(eq(1L), eq(false))).thenReturn(updatedSystem);

        Map<String, Boolean> request = Map.of("active", false);

        mockMvc.perform(patch("/api/monitored-systems/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldReturnBadRequestWhenActiveStatusIsMissing() throws Exception {
        mockMvc.perform(patch("/api/monitored-systems/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validacao"))
                .andExpect(jsonPath("$.errors.active").exists());
    }

    @Test
    void shouldDeleteMonitoredSystem() throws Exception {
        doNothing().when(monitoredSystemService).delete(1L);

        mockMvc.perform(delete("/api/monitored-systems/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMonitoredSystemThatDoesNotExist() throws Exception {
        doThrow(new MonitoredSystemNotFoundException(999L)).when(monitoredSystemService).delete(999L);

        mockMvc.perform(delete("/api/monitored-systems/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sistema monitorado nao encontrado com id: 999"));
    }

    private MonitoredSystem createSystem(Long id) {
        MonitoredSystem system = new MonitoredSystem("Sistema Financeiro", "https://financeiro.empresa.com");
        system.setId(id);
        system.setDescription("Sistema de pagamentos");
        return system;
    }
}
