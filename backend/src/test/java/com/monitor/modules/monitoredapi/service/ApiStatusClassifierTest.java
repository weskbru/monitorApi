package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiStatusClassifierTest {

    private ApiStatusClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new ApiStatusClassifier();
    }

    @Test
    void shouldReturnDownWhenApiIsNotAvailable() {
        CheckStatus status = classifier.classifyStatus(false, 100L, 3000L);

        assertEquals(CheckStatus.DOWN, status);
    }

    @Test
    void shouldReturnSlowWhenApiIsAvailableButResponseTimeIsAboveThreshold() {
        CheckStatus status = classifier.classifyStatus(true, 3001L, 3000L);

        assertEquals(CheckStatus.SLOW, status);
    }

    @Test
    void shouldReturnUpWhenApiIsAvailableWithinThreshold() {
        CheckStatus status = classifier.classifyStatus(true, 3000L, 3000L);

        assertEquals(CheckStatus.UP, status);
    }

    @Test
    void shouldUseDefaultThresholdWhenSlowThresholdIsNull() {
        CheckStatus status = classifier.classifyStatus(true, 3001L, null);

        assertEquals(CheckStatus.SLOW, status);
    }
}
