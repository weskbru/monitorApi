package com.monitor.modules.monitoredapi.entity;

import com.monitor.modules.monitoredsystem.entity.MonitoredSystem;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {
        @Index(name = "idx_monitored_api_system", columnList = "monitored_system_id"),
        @Index(name = "idx_monitored_api_active", columnList = "active")
})
public class MonitoredApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String url;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private Integer expectedStatusCode = 200;
    private Long slowThresholdMs = 3000L;
    private Long timeoutMs = 10000L;

    @ManyToOne
    @JoinColumn(name = "monitored_system_id")
    private MonitoredSystem monitoredSystem;

    // Constructors
    public MonitoredApi(String name, String url) {
        this.name = name;
        this.url = url;
        this.active = true; 
        this.createdAt = LocalDateTime.now();
        this.expectedStatusCode = 200;
        this.slowThresholdMs = 3000L;
        this.timeoutMs = 10000L;
    }

    public MonitoredApi() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSlowThresholdMs() {
        return slowThresholdMs;
    }

    public void setSlowThresholdMs(Long slowThresholdMs) {
        this.slowThresholdMs = slowThresholdMs;
    }

    public Integer getExpectedStatusCode() {
        return expectedStatusCode;
    }

    public void setExpectedStatusCode(Integer expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public MonitoredSystem getMonitoredSystem() {
        return monitoredSystem;
    }

    public void setMonitoredSystem(MonitoredSystem monitoredSystem) {
        this.monitoredSystem = monitoredSystem;
    }

}
