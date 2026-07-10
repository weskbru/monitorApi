package com.monitor.modules.monitoredapi.dto;

import com.monitor.modules.monitoredapi.CheckStatus;

import java.time.LocalDateTime;

public class ApiCurrentStatusResponse {

    private Long id;
    private Long monitoredApiId;
    private String monitoredApiName;
    private String monitoredApiUrl;
    private CheckStatus status;
    private String message;
    private Boolean available;
    private Integer statusCode;
    private Long responseTimeMs;
    private LocalDateTime checkedAt;
    private String errorMessage;

    public ApiCurrentStatusResponse(
            Long id,
            Long monitoredApiId,
            String monitoredApiName,
            String monitoredApiUrl,
            CheckStatus status,
            String message,
            Boolean available,
            Integer statusCode,
            Long responseTimeMs,
            LocalDateTime checkedAt,
            String errorMessage) {
        this.id = id;
        this.monitoredApiId = monitoredApiId;
        this.monitoredApiName = monitoredApiName;
        this.monitoredApiUrl = monitoredApiUrl;
        this.status = status;
        this.message = message;
        this.available = available;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.checkedAt = checkedAt;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public void setStatus(CheckStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getMonitoredApiId() {
        return monitoredApiId;
    }

    public void setMonitoredApiId(Long monitoredApiId) {
        this.monitoredApiId = monitoredApiId;
    }

    public String getMonitoredApiName() {
        return monitoredApiName;
    }

    public void setMonitoredApiName(String monitoredApiName) {
        this.monitoredApiName = monitoredApiName;
    }

    public String getMonitoredApiUrl() {
        return monitoredApiUrl;
    }

    public void setMonitoredApiUrl(String monitoredApiUrl) {
        this.monitoredApiUrl = monitoredApiUrl;
    }
}
