package com.harmonia.common.web;

import com.harmonia.common.api.error.ApiError;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HarmoniaException.class)
    public ResponseEntity<ApiError> handleHarmonia(HarmoniaException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus()).body(ApiError.of(
                ex.getStatus(),
                ex.getCode(),
                ex.getMessage(),
                request.getRequestURI(),
                MDC.get(CorrelationIdFilter.TRACE_ID)
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldError> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ApiError.FieldError(err.getField(), err.getDefaultMessage(), err.getRejectedValue()))
                .toList();
        ApiError body = new ApiError(
                Instant.now(),
                400,
                ErrorCode.VALIDATION_FAILED.name(),
                "Request validation failed",
                request.getRequestURI(),
                MDC.get(CorrelationIdFilter.TRACE_ID),
                fields
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiError.FieldError> fields = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldError(v.getPropertyPath().toString(), v.getMessage(), v.getInvalidValue()))
                .toList();
        ApiError body = new ApiError(
                Instant.now(),
                400,
                ErrorCode.VALIDATION_FAILED.name(),
                "Request validation failed",
                request.getRequestURI(),
                MDC.get(CorrelationIdFilter.TRACE_ID),
                fields
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(
                401, ErrorCode.UNAUTHORIZED, "Authentication required", request.getRequestURI(),
                MDC.get(CorrelationIdFilter.TRACE_ID)
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(
                403, ErrorCode.FORBIDDEN, "Access denied", request.getRequestURI(),
                MDC.get(CorrelationIdFilter.TRACE_ID)
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.of(
                500, ErrorCode.INTERNAL_ERROR, "An unexpected error occurred", request.getRequestURI(),
                MDC.get(CorrelationIdFilter.TRACE_ID)
        ));
    }
}
