package com.monitor.modules.monitoredapi.exception;

public class MonitoredApiInactiveException extends RuntimeException {

    public MonitoredApiInactiveException(Long id) {
        super("API inativa nao pode ser verificada: " + id);
    }

}