package com.monitor.modules.monitoredsystem.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.MonitoredApiNotFoundException;
import com.monitor.modules.monitoredapi.service.MonitoredApiService;
import com.monitor.modules.monitoredapi.service.ApiCheckService;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.modules.monitoredsystem.exception.MonitoredSystemNotFoundException;
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

@WebMvcTest(MonitoredSystemApiController.class)
@Import(GlobalExceptionHandler.class)
class MonitoredSystemApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MonitoredApiService monitoredApiService;

    @MockitoBean
    private ApiCheckService apiCheckService;

    @Test
    void shouldCreateMonitoredApiForSystem() throws Exception {
        MonitoredApi api = createApi(10L, 1L);

        when(monitoredApiService.createForSystem(
                eq(1L),
                eq("Login"),
                eq("https://financeiro.empresa.com/api/auth/status"),
                eq("Endpoint de login"),
                eq(200), eq(3000L), eq(10000L)))
                .thenReturn(api);

        Map<String, String> request = Map.of(
                "name", "Login",
                "url", "https://financeiro.empresa.com/api/auth/status",
                "description", "Endpoint de login");

        mockMvc.perform(post("/api/monitored-systems/1/apis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Login"))
                .andExpect(jsonPath("$.monitoredSystem.id").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenCreateApiForSystemRequestIsInvalid() throws Exception {
        Map<String, String> request = Map.of(
                "name", "",
                "url", "financeiro.empresa.com/api/auth/status");

        mockMvc.perform(post("/api/monitored-systems/1/apis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validacao"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.url").value("URL deve começar com http:// ou https://"));
    }

    @Test
    void shouldReturnNotFoundWhenCreatingApiForMissingSystem() throws Exception {
        when(monitoredApiService.createForSystem(
                eq(999L),
                eq("Login"),
                eq("https://financeiro.empresa.com/api/auth/status"),
                eq("Endpoint de login"),
                eq(200), eq(3000L), eq(10000L)))
                .thenThrow(new MonitoredSystemNotFoundException(999L));

        Map<String, String> request = Map.of(
                "name", "Login",
                "url", "https://financeiro.empresa.com/api/auth/status",
                "description", "Endpoint de login");

        mockMvc.perform(post("/api/monitored-systems/999/apis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sistema monitorado nao encontrado com id: 999"));
    }

    @Test
    void shouldListMonitoredApisBySystem() throws Exception {
        when(monitoredApiService.listBySystemId(1L)).thenReturn(List.of(createApi(10L, 1L), createApi(11L, 1L)));

        mockMvc.perform(get("/api/monitored-systems/1/apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].monitoredSystem.id").value(1))
                .andExpect(jsonPath("$[1].id").value(11));
    }

    @Test
    void shouldReturnMonitoredApiBySystemAndApiId() throws Exception {
        when(monitoredApiService.getBySystemIdAndApiId(1L, 10L)).thenReturn(createApi(10L, 1L));

        mockMvc.perform(get("/api/monitored-systems/1/apis/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.monitoredSystem.id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenApiDoesNotBelongToSystem() throws Exception {
        when(monitoredApiService.getBySystemIdAndApiId(1L, 999L))
                .thenThrow(new MonitoredApiNotFoundException(999L));

        mockMvc.perform(get("/api/monitored-systems/1/apis/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("API nao encontrada com id: 999"));
    }

    @Test
    void shouldUpdateMonitoredApiForSystem() throws Exception {
        MonitoredApi updatedApi = createApi(10L, 1L);
        updatedApi.setName("Login Atualizado");
        updatedApi.setDescription("Descricao atualizada");

        when(monitoredApiService.updateForSystem(
                eq(1L),
                eq(10L),
                eq("Login Atualizado"),
                eq("https://financeiro.empresa.com/api/auth/status"),
                eq("Descricao atualizada"),
                eq(200), eq(3000L), eq(10000L)))
                .thenReturn(updatedApi);

        Map<String, String> request = Map.of(
                "name", "Login Atualizado",
                "url", "https://financeiro.empresa.com/api/auth/status",
                "description", "Descricao atualizada");

        mockMvc.perform(put("/api/monitored-systems/1/apis/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Login Atualizado"))
                .andExpect(jsonPath("$.monitoredSystem.id").value(1));
    }

    @Test
    void shouldUpdateActiveStatusForSystemApi() throws Exception {
        MonitoredApi updatedApi = createApi(10L, 1L);
        updatedApi.setActive(false);

        when(monitoredApiService.updateActiveForSystem(eq(1L), eq(10L), eq(false))).thenReturn(updatedApi);

        Map<String, Boolean> request = Map.of("active", false);

        mockMvc.perform(patch("/api/monitored-systems/1/apis/10/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldDeleteMonitoredApiForSystem() throws Exception {
        doNothing().when(monitoredApiService).deleteForSystem(1L, 10L);

        mockMvc.perform(delete("/api/monitored-systems/1/apis/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingApiThatDoesNotBelongToSystem() throws Exception {
        doThrow(new MonitoredApiNotFoundException(999L)).when(monitoredApiService).deleteForSystem(1L, 999L);

        mockMvc.perform(delete("/api/monitored-systems/1/apis/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("API nao encontrada com id: 999"));
    }

    private MonitoredApi createApi(Long apiId, Long systemId) {
        MonitoredSystem system = new MonitoredSystem("Sistema Financeiro", "https://financeiro.empresa.com");
        system.setId(systemId);

        MonitoredApi api = new MonitoredApi("Login", "https://financeiro.empresa.com/api/auth/status");
        api.setId(apiId);
        api.setDescription("Endpoint de login");
        api.setMonitoredSystem(system);
        return api;
    }
}
