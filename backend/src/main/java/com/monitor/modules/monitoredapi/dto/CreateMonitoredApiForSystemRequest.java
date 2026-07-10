package com.monitor.modules.monitoredapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateMonitoredApiForSystemRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^(http|https)://.*$", message = "URL deve começar com http:// ou https://")
    private String url;

    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
