package com.monitor.modules.monitoredsystem.exception;

public class MonitoredSystemNotFoundException extends RuntimeException {

    public MonitoredSystemNotFoundException(Long id) {
        super("Sistema monitorado nao encontrado com id: " + id);
    }
}
