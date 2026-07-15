package com.monitor.modules.monitoredapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.dto.ApiCheckHistoryResponse;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.dto.ApiCurrentStatusResponse;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.exception.ApiCheckHistoryNotFoundException;
import com.monitor.modules.monitoredapi.exception.MonitoredApiInactiveException;
import com.monitor.modules.monitoredapi.exception.MonitoredApiNotFoundException;
import com.monitor.modules.monitoredapi.service.ApiCheckService;
import com.monitor.modules.monitoredapi.service.MonitoredApiService;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.shared.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@WebMvcTest(MonitoredApiController.class)
@Import(GlobalExceptionHandler.class)
class MonitoredApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MonitoredApiService monitoredApiService;

    @MockitoBean
    private ApiCheckService apiCheckService;

    @Test
    void shouldCreateMonitoredApi() throws Exception {
        MonitoredApi api = createApi(1L);
        when(monitoredApiService.create(
                eq(1L),
                eq("ViaCEP"),
                eq("https://viacep.com.br/ws/01001000/json/"),
                eq("API publica de CEP"),
                eq(200), eq(3000L), eq(10000L)
        )).thenReturn(api);

        Map<String, String> request = Map.of(
                "name", "ViaCEP",
                "url", "https://viacep.com.br/ws/01001000/json/",
                "description", "API publica de CEP",
                "systemId", "1"
        );

        mockMvc.perform(post("/api/monitored-apis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ViaCEP"))
                .andExpect(jsonPath("$.url").value("https://viacep.com.br/ws/01001000/json/"))
                .andExpect(jsonPath("$.description").value("API publica de CEP"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {
        Map<String, String> request = Map.of(
                "name", "",
                "url", "viacep.com.br",
                "systemId", "1"
        );

        mockMvc.perform(post("/api/monitored-apis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validacao"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.url").value("URL deve começar com http:// ou https://"));
    }

    @Test
    void shouldListAllMonitoredApis() throws Exception {
        when(monitoredApiService.listAll()).thenReturn(List.of(createApi(1L), createApi(2L)));

        mockMvc.perform(get("/api/monitored-apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("ViaCEP"))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldReturnMonitoredApiById() throws Exception {
        when(monitoredApiService.getById(1L)).thenReturn(createApi(1L));

        mockMvc.perform(get("/api/monitored-apis/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ViaCEP"));
    }

    @Test
    void shouldReturnNotFoundWhenMonitoredApiDoesNotExist() throws Exception {
        when(monitoredApiService.getById(999L)).thenThrow(new MonitoredApiNotFoundException(999L));

        mockMvc.perform(get("/api/monitored-apis/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("API nao encontrada com id: 999"));
    }

    @Test
    void shouldUpdateMonitoredApi() throws Exception {
        MonitoredApi updatedApi = createApi(1L);
        updatedApi.setName("ViaCEP Atualizada");
        updatedApi.setDescription("Descricao atualizada");

        when(monitoredApiService.update(
                eq(1L),
                eq(1L),
                eq("ViaCEP Atualizada"),
                eq("https://viacep.com.br/ws/01001000/json/"),
                eq("Descricao atualizada"),
                eq(200), eq(3000L), eq(10000L)
        )).thenReturn(updatedApi);

        Map<String, String> request = Map.of(
                "name", "ViaCEP Atualizada",
                "url", "https://viacep.com.br/ws/01001000/json/",
                "description", "Descricao atualizada",
                "systemId", "1"
        );

        mockMvc.perform(put("/api/monitored-apis/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ViaCEP Atualizada"))
                .andExpect(jsonPath("$.description").value("Descricao atualizada"));
    }

    @Test
    void shouldUpdateActiveStatus() throws Exception {
        MonitoredApi updatedApi = createApi(1L);
        updatedApi.setActive(false);

        when(monitoredApiService.updateActive(eq(1L), eq(false))).thenReturn(updatedApi);

        Map<String, Boolean> request = Map.of("active", false);

        mockMvc.perform(patch("/api/monitored-apis/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldReturnBadRequestWhenActiveStatusIsMissing() throws Exception {
        mockMvc.perform(patch("/api/monitored-apis/1/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validacao"))
                .andExpect(jsonPath("$.errors.active").exists());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingActiveStatusForMissingApi() throws Exception {
        when(monitoredApiService.updateActive(eq(999L), eq(false)))
                .thenThrow(new MonitoredApiNotFoundException(999L));

        Map<String, Boolean> request = Map.of("active", false);

        mockMvc.perform(patch("/api/monitored-apis/999/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("API nao encontrada com id: 999"));
    }

    @Test
    void shouldDeleteMonitoredApi() throws Exception {
        doNothing().when(monitoredApiService).delete(1L);

        mockMvc.perform(delete("/api/monitored-apis/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMonitoredApiThatDoesNotExist() throws Exception {
        doThrow(new MonitoredApiNotFoundException(999L)).when(monitoredApiService).delete(999L);

        mockMvc.perform(delete("/api/monitored-apis/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("API nao encontrada com id: 999"));
    }

    @Test
    void shouldCheckMonitoredApi() throws Exception {
        ApiCheckResponse response = new ApiCheckResponse(
                1L,
                "ViaCEP",
                "https://viacep.com.br/ws/01001000/json/",
                true,
                CheckStatus.UP,
                "O endpoint respondeu normalmente.",
                200,
                120L,
                LocalDateTime.of(2026, 6, 27, 10, 0),
                null
        );

        when(apiCheckService.check(1L)).thenReturn(response);

        mockMvc.perform(post("/api/monitored-apis/1/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiId").value(1))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void shouldReturnConflictWhenCheckingInactiveMonitoredApi() throws Exception {
        when(apiCheckService.check(1L)).thenThrow(new MonitoredApiInactiveException(1L));

        mockMvc.perform(post("/api/monitored-apis/1/check"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("API inativa nao pode ser verificada: 1"));
    }

    @Test
    void shouldReturnHistory() throws Exception {
        ApiCheckHistoryResponse response = createHistoryResponse(CheckStatus.UP);
        when(apiCheckService.getHistory(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/monitored-apis/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("UP"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].statusCode").value(200));
    }

    @Test
    void shouldReturnCurrentStatus() throws Exception {
        ApiCurrentStatusResponse response = createCurrentStatusResponse(CheckStatus.SLOW);
        when(apiCheckService.getCurrentStatus(1L)).thenReturn(response);

        mockMvc.perform(get("/api/monitored-apis/1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("SLOW"))
                .andExpect(jsonPath("$.message").value("O endpoint respondeu, mas está lento."));
    }

    @Test
    void shouldReturnNotFoundWhenCurrentStatusDoesNotExist() throws Exception {
        when(apiCheckService.getCurrentStatus(1L))
                .thenThrow(new ApiCheckHistoryNotFoundException(1L));

        mockMvc.perform(get("/api/monitored-apis/1/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nenhuma verificacao encontrada para a API com id: 1"));
    }

    private MonitoredApi createApi(Long id) {
        MonitoredApi api = new MonitoredApi("ViaCEP", "https://viacep.com.br/ws/01001000/json/");
        api.setId(id);
        api.setDescription("API publica de CEP");
        api.setMonitoredSystem(createSystem(1L));
        return api;
    }

    private MonitoredSystem createSystem(Long id) {
        MonitoredSystem system = new MonitoredSystem("Sistema Financeiro", "https://financeiro.empresa.com");
        system.setId(id);
        return system;
    }

    private ApiCheckHistoryResponse createHistoryResponse(CheckStatus status) {
        return new ApiCheckHistoryResponse(
                10L,
                status,
                status == CheckStatus.SLOW
                        ? "O endpoint respondeu, mas está lento."
                        : "O endpoint respondeu normalmente.",
                true,
                200,
                status == CheckStatus.SLOW ? 3500L : 120L,
                LocalDateTime.of(2026, 6, 27, 10, 0),
                null
        );
    }

    private ApiCurrentStatusResponse createCurrentStatusResponse(CheckStatus status) {
        return new ApiCurrentStatusResponse(
                10L,
                1L,
                "ViaCEP",
                "https://viacep.com.br/ws/01001000/json/",
                status,
                status == CheckStatus.SLOW
                        ? "O endpoint respondeu, mas está lento."
                        : "O endpoint respondeu normalmente.",
                true,
                200,
                status == CheckStatus.SLOW ? 3500L : 120L,
                LocalDateTime.of(2026, 6, 27, 10, 0),
                null
        );
    }
}
