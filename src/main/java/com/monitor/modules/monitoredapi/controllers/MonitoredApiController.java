package com.monitor.modules.monitoredapi.controllers;

import com.monitor.modules.monitoredapi.dto.CreateMonitoredApiRequest;
import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.service.MonitoredApiService;
import com.monitor.modules.monitoredapi.dto.ApiCheckResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/monitored-apis")
public class MonitoredApiController {
    private final MonitoredApiService monitoredApiService;

    public MonitoredApiController(MonitoredApiService monitoredApiService) {
        this.monitoredApiService = monitoredApiService;
    }

    @PostMapping
    public MonitoredApi createMonitoredApi(@Valid @RequestBody CreateMonitoredApiRequest request) {
        return monitoredApiService.create(request.getName(), request.getUrl(), request.getDescription()); 
                
    }

    @GetMapping
    public List<MonitoredApi> listAll() {
        return monitoredApiService.listAll();
    }

    @GetMapping("/{id}")
    public MonitoredApi getMonitoredApiById(@PathVariable Long id) {
        return monitoredApiService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteMonitoredApi(@PathVariable Long id) {
        monitoredApiService.delete(id);
    }

    @PutMapping("/{id}")
    public MonitoredApi updateMonitoredApi(@PathVariable Long id, @Valid @RequestBody CreateMonitoredApiRequest request) {
        return monitoredApiService.update(id, request.getName(), request.getUrl(), request.getDescription());
    }   

    @PostMapping("/{id}/check")
    public ApiCheckResponse checkMonitoredApi(@PathVariable Long id) {
        return monitoredApiService.check(id);
    }
}
