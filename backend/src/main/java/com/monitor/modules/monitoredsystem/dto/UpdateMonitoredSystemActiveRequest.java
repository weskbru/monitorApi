package com.monitor.modules.monitoredsystem.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateMonitoredSystemActiveRequest {

    @NotNull(message = "active e obrigatorio")
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
