package com.monitor.modules.monitoredapi.exception;

public class ApiCheckHistoryNotFoundException extends RuntimeException {

    public ApiCheckHistoryNotFoundException(Long id) {
        super("Nenhuma verificacao encontrada para a API com id: " + id);
    }   

}
