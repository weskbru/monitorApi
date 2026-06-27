package com.monitor.modules.monitoredapi.exception;

public class MonitoredApiNotFoundException extends RuntimeException {

    public MonitoredApiNotFoundException(Long id) {
        super("API nao encontrada com id: " + id);
    }
}
