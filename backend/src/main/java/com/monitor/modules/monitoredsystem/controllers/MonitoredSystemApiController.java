package com.monitor.modules.monitoredsystem.controllers;

import com.monitor.modules.monitoredapi.dto.CreateMonitoredApiForSystemRequest;
import com.monitor.modules.monitoredapi.dto.UpdateMonitoredApiActiveRequest;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.service.MonitoredApiService;
import com.monitor.modules.monitoredapi.service.ApiCheckService;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.dto.ApiCurrentStatusResponse;
import com.monitor.modules.monitoredapi.dto.ApiCheckHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/monitored-systems/{systemId}/apis")
@Tag(name = "APIs por sistema", description = "Cadastro e consulta de endpoints criticos de um sistema")
public class MonitoredSystemApiController {

    private final MonitoredApiService monitoredApiService;
    private final ApiCheckService apiCheckService;

    public MonitoredSystemApiController(MonitoredApiService monitoredApiService, ApiCheckService apiCheckService) {
        this.monitoredApiService = monitoredApiService;
        this.apiCheckService = apiCheckService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar API monitorada em um sistema")
    public MonitoredApi createMonitoredApiForSystem(
            @PathVariable("systemId") Long systemId,
            @Valid @RequestBody CreateMonitoredApiForSystemRequest request) {
        return monitoredApiService.createForSystem(
                systemId,
                request.getName(),
                request.getUrl(),
                request.getDescription(),
                request.getExpectedStatusCode(),
                request.getSlowThresholdMs(),
                request.getTimeoutMs());
    }

    @GetMapping
    @Operation(summary = "Listar APIs monitoradas de um sistema")
    public List<MonitoredApi> listBySystem(@PathVariable("systemId") Long systemId) {
        return monitoredApiService.listBySystemId(systemId);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar APIs de um sistema com paginacao")
    public Page<MonitoredApi> searchBySystem(@PathVariable("systemId") Long systemId,
            @RequestParam(value = "query", defaultValue = "") String query,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return monitoredApiService.searchBySystem(systemId, query, page, size);
    }

    @GetMapping("/{apiId}")
    @Operation(summary = "Buscar API monitorada de um sistema por id")
    public MonitoredApi getBySystemAndApiId(
            @PathVariable("systemId") Long systemId,
            @PathVariable("apiId") Long apiId) {
        return monitoredApiService.getBySystemIdAndApiId(systemId, apiId);
    }

    @PutMapping("/{apiId}")
    @Operation(summary = "Atualizar API monitorada de um sistema")
    public MonitoredApi updateForSystem(
            @PathVariable("systemId") Long systemId,
            @PathVariable("apiId") Long apiId,
            @Valid @RequestBody CreateMonitoredApiForSystemRequest request) {
        return monitoredApiService.updateForSystem(
                systemId,
                apiId,
                request.getName(),
                request.getUrl(),
                request.getDescription(),
                request.getExpectedStatusCode(),
                request.getSlowThresholdMs(),
                request.getTimeoutMs());
    }

    @PatchMapping("/{apiId}/active")
    @Operation(summary = "Ativar ou desativar API monitorada de um sistema")
    public MonitoredApi updateActiveForSystem(
            @PathVariable("systemId") Long systemId,
            @PathVariable("apiId") Long apiId,
            @Valid @RequestBody UpdateMonitoredApiActiveRequest request) {
        return monitoredApiService.updateActiveForSystem(systemId, apiId, request.getActive());
    }

    @DeleteMapping("/{apiId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover API monitorada de um sistema")
    public void deleteForSystem(
            @PathVariable("systemId") Long systemId,
            @PathVariable("apiId") Long apiId) {
        monitoredApiService.deleteForSystem(systemId, apiId);
    }

    @PostMapping("/{apiId}/check")
    @Operation(summary = "Verificar API monitorada do sistema")
    public ApiCheckResponse checkForSystem(@PathVariable("systemId") Long systemId,
            @PathVariable("apiId") Long apiId) {
        monitoredApiService.getBySystemIdAndApiId(systemId, apiId);
        return apiCheckService.check(apiId);
    }

    @GetMapping("/{apiId}/status")
    @Operation(summary = "Consultar status atual da API do sistema")
    public ApiCurrentStatusResponse getStatusForSystem(@PathVariable("systemId") Long systemId,
            @PathVariable("apiId") Long apiId) {
        monitoredApiService.getBySystemIdAndApiId(systemId, apiId);
        return apiCheckService.getCurrentStatus(apiId);
    }

    @GetMapping("/{apiId}/history")
    @Operation(summary = "Consultar historico da API do sistema")
    public List<ApiCheckHistoryResponse> getHistoryForSystem(@PathVariable("systemId") Long systemId,
            @PathVariable("apiId") Long apiId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "100") Integer size) {
        monitoredApiService.getBySystemIdAndApiId(systemId, apiId);
        return apiCheckService.getHistory(apiId, page, size);
    }
}
