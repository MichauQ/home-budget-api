package io.github.michauq.homebudget.common.exception;

import java.util.Map;

public record ValidationErrorResponse(
        int status,
        Map<String, String> errors
) {
}
