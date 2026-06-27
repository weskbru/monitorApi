package com.monitor.modules.monitoredapi.controllers;

import com.monitor.modules.monitoredapi.dto.ApiCheckHistoryResponse;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import com.monitor.modules.monitoredapi.dto.CreateMonitoredApiRequest;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.service.ApiCheckService;
import com.monitor.modules.monitoredapi.service.MonitoredApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return monitoredApiService.create(request.getName(), request.getUrl(), request.getDescription()); 
                
    }

    @GetMapping
    @Operation(summary = "Listar APIs monitoradas")
    public List<MonitoredApi> listAll() {
        return monitoredApiService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar API monitorada por id")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API monitorada encontrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public MonitoredApi getMonitoredApiById(@PathVariable Long id) {
        return monitoredApiService.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover API monitorada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "API monitorada removida")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public void deleteMonitoredApi(@PathVariable Long id) {
        monitoredApiService.delete(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar API monitorada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API monitorada atualizada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados invalidos")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public MonitoredApi updateMonitoredApi(@PathVariable Long id, @Valid @RequestBody CreateMonitoredApiRequest request) {
        return monitoredApiService.update(id, request.getName(), request.getUrl(), request.getDescription());
    }   

    @PostMapping("/{id}/check")
    @Operation(summary = "Verificar disponibilidade manualmente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verificacao executada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public ApiCheckResponse checkMonitoredApi(@PathVariable Long id) {
        return apiCheckService.check(id);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Consultar historico de verificacoes")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Historico retornado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada nao encontrada")
    public List<ApiCheckHistoryResponse> getHistory(@PathVariable Long id) {
        return apiCheckService.getHistory(id);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Consultar status atual")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status atual retornado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API monitorada ou status atual nao encontrado")
    public ApiCheckHistoryResponse getCurrentStatus(@PathVariable Long id) {
        return apiCheckService.getCurrentStatus(id);
    }
                        
}
