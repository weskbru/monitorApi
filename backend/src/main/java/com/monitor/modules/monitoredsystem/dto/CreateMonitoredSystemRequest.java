package com.monitor.modules.monitoredsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateMonitoredSystemRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^(http|https)://.*$", message = "URL base deve começar com http:// ou https://")
    private String baseUrl;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
