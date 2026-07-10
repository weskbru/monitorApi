package com.monitor.modules.monitoredsystem.dto;

import com.monitor.modules.monitoredapi.CheckStatus;

import java.time.LocalDateTime;

public class MonitoredSystemStatusResponse {

    private Long systemId;
    private String name;
    private CheckStatus status;
    private String message;
    private int totalEndpoints;
    private int upEndpoints;
    private int slowEndpoints;
    private int downEndpoints;
    private int unknownEndpoints;
    private LocalDateTime lastCheckedAt;

    public MonitoredSystemStatusResponse(
            Long systemId,
            String name,
            CheckStatus status,
            String message,
            int totalEndpoints,
            int upEndpoints,
            int slowEndpoints,
            int downEndpoints,
            int unknownEndpoints,
            LocalDateTime lastCheckedAt) {
        this.systemId = systemId;
        this.name = name;
        this.status = status;
        this.message = message;
        this.totalEndpoints = totalEndpoints;
        this.upEndpoints = upEndpoints;
        this.slowEndpoints = slowEndpoints;
        this.downEndpoints = downEndpoints;
        this.unknownEndpoints = unknownEndpoints;
        this.lastCheckedAt = lastCheckedAt;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getTotalEndpoints() {
        return totalEndpoints;
    }

    public void setTotalEndpoints(int totalEndpoints) {
        this.totalEndpoints = totalEndpoints;
    }

    public int getUpEndpoints() {
        return upEndpoints;
    }

    public void setUpEndpoints(int upEndpoints) {
        this.upEndpoints = upEndpoints;
    }

    public int getSlowEndpoints() {
        return slowEndpoints;
    }

    public void setSlowEndpoints(int slowEndpoints) {
        this.slowEndpoints = slowEndpoints;
    }

    public int getDownEndpoints() {
        return downEndpoints;
    }

    public void setDownEndpoints(int downEndpoints) {
        this.downEndpoints = downEndpoints;
    }

    public int getUnknownEndpoints() {
        return unknownEndpoints;
    }

    public void setUnknownEndpoints(int unknownEndpoints) {
        this.unknownEndpoints = unknownEndpoints;
    }

    public LocalDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(LocalDateTime lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }
}
