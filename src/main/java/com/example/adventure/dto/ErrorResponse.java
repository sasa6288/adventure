package com.example.adventure.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        String requestId,
        Map<String, String> validationErrors
) {
}
