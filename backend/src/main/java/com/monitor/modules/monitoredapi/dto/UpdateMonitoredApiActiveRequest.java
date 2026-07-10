package com.monitor.modules.monitoredapi.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateMonitoredApiActiveRequest {

    @NotNull(message = "active e obrigatorio")
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}