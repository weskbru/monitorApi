package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.entity.MonitoredApi;
import com.monitor.modules.monitoredapi.repository.MonitoredApiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomatedApiCheckService {

    private static final Logger log =
            LoggerFactory.getLogger(AutomatedApiCheckService.class);

    private final MonitoredApiRepository monitoredApiRepository;
    private final ApiCheckService apiCheckService;

    public AutomatedApiCheckService(
            MonitoredApiRepository monitoredApiRepository,
            ApiCheckService apiCheckService
    ) {
        this.monitoredApiRepository = monitoredApiRepository;
        this.apiCheckService = apiCheckService;
    }

    public void checkActiveApis() {
        log.info("Iniciando verificacao automatica de APIs ativas");

        List<MonitoredApi> activeApis = monitoredApiRepository.findByActiveTrue();

        for (MonitoredApi api : activeApis) {
            try {
                apiCheckService.check(api.getId());
            } catch (Exception exception) {
                log.error("Erro ao verificar API com id {}", api.getId(), exception);
            }
        }

        log.info("Verificacao automatica finalizada. APIs verificadas: {}", activeApis.size());
    }
}