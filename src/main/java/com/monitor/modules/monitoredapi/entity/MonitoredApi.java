package com.monitor.modules.monitoredapi.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class MonitoredApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String url;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private Long slowThresholdMs; // Novo campo para armazenar o limite de lentidão em milissegundos

    // Constructors
    public MonitoredApi(String name, String url) {
        this.name = name;
        this.url = url;
        this.active = true; 
        this.createdAt = LocalDateTime.now();
        this.slowThresholdMs = 3000L; 
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


}