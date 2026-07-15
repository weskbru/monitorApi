package com.monitor.modules.monitoredapi.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiTargetValidatorTest {

    @Test
    void shouldBlockPrivateTargetByDefault() {
        ApiTargetValidator validator = new ApiTargetValidator(false);
        assertThrows(IllegalArgumentException.class, () -> validator.validate("http://127.0.0.1/health"));
    }

    @Test
    void shouldAllowPrivateTargetWhenExplicitlyConfigured() {
        ApiTargetValidator validator = new ApiTargetValidator(true);
        assertDoesNotThrow(() -> validator.validate("http://127.0.0.1/health"));
    }
}
