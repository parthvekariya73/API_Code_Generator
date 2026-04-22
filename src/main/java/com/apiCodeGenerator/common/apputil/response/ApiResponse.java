package com.apiCodeGenerator.common.apputil.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Meta meta;
    private final String timestamp;
    private final String requestId;
    private final ErrorDetails error;

    private ApiResponse(Builder<T> builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data;
        this.meta = builder.meta;
        this.timestamp = builder.timestamp != null
                ? builder.timestamp
                : DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        this.requestId = builder.requestId;
        this.error = builder.error;
    }

    // ─────────────────────────────────────────────
    // Success factory methods
    // ─────────────────────────────────────────────
    public static <T> ApiResponse<T> success(String message, T data, Meta meta, String requestId) {
        return new Builder<T>()
                .success(true)
                .message(message)
                .data(data)
                .meta(meta)
                .requestId(requestId)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data, String requestId) {
        return success(message, data, null, requestId);
    }

    public static <T> ApiResponse<T> success(String message, String requestId) {
        return success(message, null, null, requestId);
    }

    // ─────────────────────────────────────────────
    // Error factory methods
    // ─────────────────────────────────────────────
    public static <T> ApiResponse<T> error(String message, String errorCode,
                                           List<ErrorDetails.FieldIssue> details, String requestId) {

        List<ErrorDetails.FieldIssue> safeDetails =
                details == null ? Collections.emptyList() : details;

        ErrorDetails error = ErrorDetails.builder()
                .code(errorCode)
                .message(message)
                .details(safeDetails)
                .build();

        return new Builder<T>()
                .success(false)
                .message(message)
                .error(error)
                .requestId(requestId)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String requestId) {

        List<ErrorDetails.FieldIssue> details = List.of();

        if (errorCode.hasFieldIssue()) {
            details = List.of(
                    ErrorDetails.FieldIssue.builder()
                            .field(errorCode.getField())
                            .issue(errorCode.getIssue())
                            .build()
            );
        }

        ErrorDetails errorDetails = ErrorDetails.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(details)
                .build();

        return new Builder<T>()
                .success(false)
                .message(errorCode.getMessage())
                .error(errorDetails)
                .requestId(requestId)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode, String requestId) {
        return error(message, errorCode, Collections.emptyList(), requestId);
    }

    public static <T> ApiResponse<T> error(String message, String requestId) {
        return error(message, null, Collections.emptyList(), requestId);
    }



    // ─────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────
    public static class Builder<T> {
        private boolean success;
        private String message;
        private T data;
        private Meta meta;
        private String timestamp;
        private String requestId;
        private ErrorDetails error;

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> meta(Meta meta) {
            this.meta = meta;
            return this;
        }

        public Builder<T> timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder<T> requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder<T> error(ErrorDetails error) {
            this.error = error;
            return this;
        }

        public ApiResponse<T> build() {
            if (this.timestamp == null) {
                this.timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
            }
            return new ApiResponse<>(this);
        }
    }
}
