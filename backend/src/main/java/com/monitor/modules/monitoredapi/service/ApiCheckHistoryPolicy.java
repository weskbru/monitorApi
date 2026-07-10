package com.monitor.modules.monitoredapi.service;

import com.monitor.modules.monitoredapi.CheckStatus;
import com.monitor.modules.monitoredapi.entity.ApiCurrentStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
class ApiCheckHistoryPolicy {

    boolean shouldSaveHistory(
            Optional<ApiCurrentStatus> currentStatusFound,
            CheckStatus newStatus,
            Integer newStatusCode,
            String newErrorMessage,
            LocalDateTime checkedAt
    ) {
        return currentStatusFound
                .map(currentStatus ->
                        hasStatusChanged(currentStatus, newStatus)
                                || hasStatusCodeCategoryChanged(currentStatus, newStatusCode)
                                || hasErrorChanged(currentStatus, newErrorMessage)
                                || shouldSavePeriodicSample(currentStatus, checkedAt)
                )
                .orElse(true);
    }

    private boolean hasStatusChanged(
            ApiCurrentStatus currentStatus,
            CheckStatus newStatus
    ) {
        return currentStatus.getStatus() != newStatus;
    }

    private boolean hasStatusCodeCategoryChanged(
            ApiCurrentStatus currentStatus,
            Integer newStatusCode
    ) {
        String currentCategory = statusCodeCategory(currentStatus.getStatusCode());
        String newCategory = statusCodeCategory(newStatusCode);

        return !currentCategory.equals(newCategory);
    }

    private boolean hasErrorChanged(
            ApiCurrentStatus currentStatus,
            String newErrorMessage
    ) {
        return !Objects.equals(currentStatus.getErrorMessage(), newErrorMessage);
    }

    private boolean shouldSavePeriodicSample(
            ApiCurrentStatus currentStatus,
            LocalDateTime checkedAt
    ) {
        LocalDateTime lastCheckedAt = currentStatus.getCheckedAt();

        if (lastCheckedAt == null) {
            return true;
        }

        return Duration.between(lastCheckedAt, checkedAt).toHours() >= 1;
    }

    private String statusCodeCategory(Integer statusCode) {
        if (statusCode == null) {
            return "null";
        }

        int category = statusCode / 100;

        if (category >= 1 && category <= 5) {
            return category + "xx";
        }

        return "outros";
    }
}
