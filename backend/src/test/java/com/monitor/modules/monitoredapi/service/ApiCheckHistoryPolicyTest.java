package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiCheckHistoryPolicyTest {

    private ApiCheckHistoryPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new ApiCheckHistoryPolicy();
    }

    @Test
    void shouldSaveHistoryWhenCurrentStatusDoesNotExist() {
        boolean shouldSave = policy.shouldSaveHistory(
                Optional.empty(),
                CheckStatus.UP,
                200,
                null,
                LocalDateTime.now()
        );

        assertTrue(shouldSave);
    }

    @Test
    void shouldSaveHistoryWhenBusinessStatusChanges() {
        ApiCurrentStatus currentStatus = createCurrentStatus(CheckStatus.UP, 200, null);

        boolean shouldSave = policy.shouldSaveHistory(
                Optional.of(currentStatus),
                CheckStatus.DOWN,
                200,
                null,
                LocalDateTime.now()
        );

        assertTrue(shouldSave);
    }

    @Test
    void shouldSaveHistoryWhenStatusCodeCategoryChanges() {
        ApiCurrentStatus currentStatus = createCurrentStatus(CheckStatus.DOWN, 500, null);

        boolean shouldSave = policy.shouldSaveHistory(
                Optional.of(currentStatus),
                CheckStatus.DOWN,
                404,
                null,
                LocalDateTime.now()
        );

        assertTrue(shouldSave);
    }

    @Test
    void shouldSaveHistoryWhenErrorMessageChanges() {
        ApiCurrentStatus currentStatus = createCurrentStatus(CheckStatus.DOWN, null, "Erro antigo");

        boolean shouldSave = policy.shouldSaveHistory(
                Optional.of(currentStatus),
                CheckStatus.DOWN,
                null,
                "Erro novo",
                LocalDateTime.now()
        );

        assertTrue(shouldSave);
    }

    @Test
    void shouldSaveHistoryWhenOneHourPassed() {
        LocalDateTime checkedAt = LocalDateTime.of(2026, 6, 27, 11, 0);
        ApiCurrentStatus currentStatus = createCurrentStatus(CheckStatus.UP, 200, null);
        currentStatus.setCheckedAt(checkedAt.minusHours(1));

        boolean shouldSave = policy.shouldSaveHistory(
                Optional.of(currentStatus),
                CheckStatus.UP,
                204,
                null,
                checkedAt
        );

        assertTrue(shouldSave);
    }

    @Test
    void shouldNotSaveHistoryWhenNothingRelevantChanges() {
        ApiCurrentStatus currentStatus = createCurrentStatus(CheckStatus.UP, 200, null);

        boolean shouldSave = policy.shouldSaveHistory(
                Optional.of(currentStatus),
                CheckStatus.UP,
                204,
                null,
                LocalDateTime.now()
        );

        assertFalse(shouldSave);
    }

    private ApiCurrentStatus createCurrentStatus(
            CheckStatus status,
            Integer statusCode,
            String errorMessage
    ) {
        ApiCurrentStatus currentStatus = new ApiCurrentStatus();
        currentStatus.setStatus(status);
        currentStatus.setStatusCode(statusCode);
        currentStatus.setErrorMessage(errorMessage);
        currentStatus.setCheckedAt(LocalDateTime.now());
        return currentStatus;
    }
}
