package com.harmonia.common.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message, Object rejectedValue) {
    }

    public static ApiError of(int status, ErrorCode code, String message, String path, String traceId) {
        return new ApiError(Instant.now(), status, code.name(), message, path, traceId, null);
    }
}
