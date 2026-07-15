package com.monitor.modules.monitoredapi.controllers;

import com.monitor.modules.monitoredapi.dto.ApiCheckHistoryResponse;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.dto.ApiCurrentStatusResponse;
import com.monitor.modules.monitoredapi.dto.CreateMonitoredApiRequest;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.service.ApiCheckService;
import com.monitor.modules.monitoredapi.service.MonitoredApiService;
import com.monitor.modules.monitoredapi.dto.UpdateMonitoredApiActiveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/monitored-apis")
@Tag(name = "APIs monitoradas", description = "Cadastro, consulta e verificacao de APIs monitoradas")
public class MonitoredApiController {
    
    private final MonitoredApiService monitoredApiService;
    private final ApiCheckService apiCheckService;

    public MonitoredApiController(MonitoredApiService monitoredApiService, ApiCheckService apiCheckService) {
        this.monitoredApiService = monitoredApiService;
        this.apiCheckService = apiCheckService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar API monitorada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "API monitorada cadastrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos")
    public MonitoredApi createMonitoredApi(@Valid @RequestBody CreateMonitoredApiRequest request) {
        return monitoredApiService.create(
                request.getSystemId(),
                request.getName(),
                request.getUrl(),
                request.getDescription(),
                request.getExpectedStatusCode(),
                request.getSlowThresholdMs(),
                request.getTimeoutMs());
                
    }

    @GetMapping
    @Operation(summary = "Listar APIs monitoradas")
    public List<MonitoredApi> listAll() {
        return monitoredApiService.listAll();
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar APIs monitoradas com paginacao")
    public Page<MonitoredApi> search(
            @RequestParam(value = "query", defaultValue = "") String query,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return monitoredApiService.search(query, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar API monitorada por id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API monitorada encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public MonitoredApi getMonitoredApiById(@PathVariable("id") Long id) {
        return monitoredApiService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover API monitorada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "API monitorada removida")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public void deleteMonitoredApi(@PathVariable("id") Long id) {
        monitoredApiService.delete(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar API monitorada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API monitorada atualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public MonitoredApi updateMonitoredApi(@PathVariable("id") Long id, @Valid @RequestBody CreateMonitoredApiRequest request) {
        return monitoredApiService.update(
                id,
                request.getSystemId(),
                request.getName(),
                request.getUrl(),
                request.getDescription(),
                request.getExpectedStatusCode(),
                request.getSlowThresholdMs(),
                request.getTimeoutMs());
    }   

    @PostMapping("/{id}/check")
    @Operation(summary = "Verificar disponibilidade manualmente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verificacao executada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public ApiCheckResponse checkMonitoredApi(@PathVariable("id") Long id) {
        return apiCheckService.check(id);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Consultar historico de verificacoes")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Historico retornado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public List<ApiCheckHistoryResponse> getHistory(@PathVariable("id") Long id,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "100") Integer size) {
        return page == null ? apiCheckService.getHistory(id) : apiCheckService.getHistory(id, page, size);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Consultar status atual")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status atual retornado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada ou status atual nao encontrado")
    public ApiCurrentStatusResponse getCurrentStatus(@PathVariable("id") Long id) {
        return apiCheckService.getCurrentStatus(id);
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Ativar ou desativar API monitorada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status ativo atualizado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public MonitoredApi updateActive(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateMonitoredApiActiveRequest request) {
        return monitoredApiService.updateActive(id, request.getActive());
    }
                        
}
