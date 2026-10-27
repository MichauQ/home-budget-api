package io.github.michauq.homebudget.common.exception;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String message,
        String path) {
}
