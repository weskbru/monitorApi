package com.monitor.modules.monitoredapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateMonitoredApiRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^(http|https)://.*$", message = "URL deve começar com http:// ou https://")
    private String url;

    // Getters and Setters

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

}
