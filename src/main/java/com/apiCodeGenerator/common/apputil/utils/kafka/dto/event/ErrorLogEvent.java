package com.apiCodeGenerator.common.apputil.utils.kafka.dto.event;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLogEvent {
    private UUID eventId;
    private UUID errorLogUuid;

    // Error Classification
    private String errorCode;
    private String errorType;
    private String errorSeverity; // CRITICAL, HIGH, MEDIUM, LOW, INFO
    private String errorCategory;

    // Error Details
    private String errorMessage;
    private String errorDetail;
    private String stackTrace;
    private String exceptionClass;
    private String rootCause;

    // Request Information
    private UUID requestId;
    private String requestUrl;
    private String requestMethod;
    private Map<String, Object> requestParameters;
    private String requestBody;
    private Map<String, String> requestHeaders;

    // User Information
    private Long userId;
    private String username;
    private String userIpAddress;
    private String userAgent;
    private UUID sessionId;

    // Application Information
    private String applicationName;
    private String applicationVersion;
    private String environment; // DEVELOPMENT, STAGING, PRODUCTION
    private String serverName;

    // Technical Details
    private String threadName;
    private String loggerName;
    private String methodName;
    private Integer lineNumber;

    // Database Errors
    private String databaseQuery;
    private String databaseErrorCode;
    private String databaseConstraintName;

    // Resolution Tracking
    private Boolean isResolved = false;
    private LocalDateTime resolvedAt;
    private Long resolvedBy;
    private String resolutionNotes;

    // Notification Tracking
    private Boolean isNotified = false;
    private LocalDateTime notifiedAt;
    private List<String> notificationRecipients;

    // Retry Information
    private Integer retryCount = 0;
    private LocalDateTime lastRetryAt;
    private Boolean isRetryable = false;

    // Additional Data
    private Map<String, Object> customData;
    private List<String> tags;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp;

    private String eventType = "ERROR_LOG";
}
