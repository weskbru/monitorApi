package com.monitor.modules.monitoredsystem.controllers;

import com.monitor.modules.monitoredsystem.dto.CreateMonitoredSystemRequest;
import com.monitor.modules.monitoredsystem.dto.MonitoredSystemStatusResponse;
import com.monitor.modules.monitoredsystem.dto.UpdateMonitoredSystemActiveRequest;
import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import com.monitor.modules.monitoredsystem.service.MonitoredSystemStatusService;
import com.monitor.modules.monitoredsystem.service.MonitoredSystemService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitored-systems")
@Tag(name = "Sistemas monitorados", description = "Cadastro e consulta de sistemas monitorados")
public class MonitoredSystemController {

    private final MonitoredSystemService monitoredSystemService;
    private final MonitoredSystemStatusService monitoredSystemStatusService;

    public MonitoredSystemController(
            MonitoredSystemService monitoredSystemService,
            MonitoredSystemStatusService monitoredSystemStatusService) {
        this.monitoredSystemService = monitoredSystemService;
        this.monitoredSystemStatusService = monitoredSystemStatusService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar sistema monitorado")
    public MonitoredSystem createMonitoredSystem(@Valid @RequestBody CreateMonitoredSystemRequest request) {
        return monitoredSystemService.create(
                request.getName(),
                request.getBaseUrl(),
                request.getDescription());
    }

    @GetMapping
    @Operation(summary = "Listar sistemas monitorados")
    public List<MonitoredSystem> listAll() {
        return monitoredSystemService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sistema monitorado por id")
    public MonitoredSystem getMonitoredSystemById(@PathVariable("id") Long id) {
        return monitoredSystemService.getById(id);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Consultar status agregado do sistema monitorado")
    public MonitoredSystemStatusResponse getMonitoredSystemStatus(@PathVariable("id") Long id) {
        return monitoredSystemStatusService.getStatus(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar sistema monitorado")
    public MonitoredSystem updateMonitoredSystem(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateMonitoredSystemRequest request) {
        return monitoredSystemService.update(
                id,
                request.getName(),
                request.getBaseUrl(),
                request.getDescription());
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Ativar ou desativar sistema monitorado")
    public MonitoredSystem updateActive(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMonitoredSystemActiveRequest request) {
        return monitoredSystemService.updateActive(id, request.getActive());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover sistema monitorado")
    public void deleteMonitoredSystem(@PathVariable("id") Long id) {
        monitoredSystemService.delete(id);
    }
}
