package com.monitor.modules.monitoredapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class CreateMonitoredApiForSystemRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^(http|https)://.*$", message = "URL deve começar com http:// ou https://")
    private String url;

    private String description;

    @Min(100)
    @Max(599)
    private Integer expectedStatusCode = 200;

    @Min(1)
    private Long slowThresholdMs = 3000L;

    @Min(100)
    @Max(120000)
    private Long timeoutMs = 10000L;

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

    public Integer getExpectedStatusCode() { return expectedStatusCode; }
    public void setExpectedStatusCode(Integer expectedStatusCode) { this.expectedStatusCode = expectedStatusCode; }
    public Long getSlowThresholdMs() { return slowThresholdMs; }
    public void setSlowThresholdMs(Long slowThresholdMs) { this.slowThresholdMs = slowThresholdMs; }
    public Long getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Long timeoutMs) { this.timeoutMs = timeoutMs; }
}
