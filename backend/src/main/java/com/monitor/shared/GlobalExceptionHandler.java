package com.monitor.shared;

import com.monitor.modules.monitoredapi.exception.MonitoredApiNotFoundException;
import com.monitor.modules.monitoredapi.exception.ApiCheckHistoryNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.monitor.modules.monitoredapi.exception.MonitoredApiInactiveException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MonitoredApiNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMonitoredApiNotFound(MonitoredApiNotFoundException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Erro de validacao");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ApiCheckHistoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleApiCheckHistoryNotFound(ApiCheckHistoryNotFoundException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MonitoredApiInactiveException.class)
    public ResponseEntity<Map<String, Object>> handleMonitoredApiInactive(MonitoredApiInactiveException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
