package com.apiCodeGenerator.common.apputil.utils.kafka.service;

import com.apiCodeGenerator.common.apputil.utils.kafka.dto.event.ErrorLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ErrorLoggingService {

    private static final Logger log = LoggerFactory.getLogger(ErrorLoggingService.class);

    private final SystemEventProducer systemEventProducer;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active:dev}")
    private String environment;

    public ErrorLoggingService(SystemEventProducer systemEventProducer) {
        this.systemEventProducer = systemEventProducer;
    }


    public void logError(Exception exception, String errorCategory) {
        logError(exception, errorCategory, null, null);
    }


    public void logError(Exception exception, String errorCategory, Map<String, Object> customData) {
        logError(exception, errorCategory, customData, null);
    }


    public void logError(Exception exception, String errorCategory,
                         Map<String, Object> customData, List<String> tags) {
        try {
            ErrorLogEvent errorEvent = new ErrorLogEvent();
            errorEvent.setErrorLogUuid(UUID.randomUUID());

            // Error Classification
            errorEvent.setErrorCode(generateErrorCode(exception));
            errorEvent.setErrorType(exception.getClass().getSimpleName());
            errorEvent.setErrorSeverity(determineSeverity(exception));
            errorEvent.setErrorCategory(errorCategory);

            // Error Details
            errorEvent.setErrorMessage(exception.getMessage());
            errorEvent.setErrorDetail(extractErrorDetail(exception));
            errorEvent.setStackTrace(getStackTraceAsString(exception));
            errorEvent.setExceptionClass(exception.getClass().getName());
            errorEvent.setRootCause(getRootCause(exception));

            // Request Information
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                errorEvent.setRequestId(UUID.randomUUID());
                errorEvent.setRequestUrl(request.getRequestURL().toString());
                errorEvent.setRequestMethod(request.getMethod());
                errorEvent.setRequestParameters(extractRequestParameters(request));
                errorEvent.setRequestHeaders(extractRequestHeaders(request));
            }

            // User Information
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                errorEvent.setUsername(auth.getName());
                errorEvent.setUserId(extractUserId(auth));
            }
            if (request != null) {
                errorEvent.setUserIpAddress(getClientIP(request));
                errorEvent.setUserAgent(request.getHeader("User-Agent"));
            }

            // Application Information
            errorEvent.setApplicationName(applicationName);
            errorEvent.setApplicationVersion(getApplicationVersion());
            errorEvent.setEnvironment(environment.toUpperCase());
            errorEvent.setServerName(getServerName());

            // Technical Details
            StackTraceElement[] stackTrace = exception.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                StackTraceElement element = stackTrace[0];
                errorEvent.setThreadName(Thread.currentThread().getName());
                errorEvent.setLoggerName(element.getClassName());
                errorEvent.setMethodName(element.getMethodName());
                errorEvent.setLineNumber(element.getLineNumber());
            }

            // Database Errors
            if (isDatabaseException(exception)) {
                extractDatabaseErrorInfo(exception, errorEvent);
            }

            // Retry Information
            errorEvent.setRetryCount(0);
            errorEvent.setIsRetryable(isRetryable(exception));

            // Custom Data and Tags
            errorEvent.setCustomData(customData);
            errorEvent.setTags(tags != null ? tags : new ArrayList<>());

            // Publish to Kafka
            systemEventProducer.publishErrorLogEvent(errorEvent);

            log.debug("Error logged and sent to Kafka: {}", errorEvent.getErrorType());

        } catch (Exception e) {
            log.error("Failed to log error to Kafka: {}", e.getMessage(), e);
        }
    }


    public void logCriticalError(Exception exception, String errorCategory, Map<String, Object> customData) {
        ErrorLogEvent errorEvent = buildBasicErrorEvent(exception, errorCategory, customData);
        errorEvent.setErrorSeverity("CRITICAL");
        errorEvent.setIsNotified(true);
        errorEvent.setNotifiedAt(LocalDateTime.now());
        systemEventProducer.publishErrorLogEvent(errorEvent);
    }


    public void logDatabaseError(Exception exception, String query) {
        ErrorLogEvent errorEvent = buildBasicErrorEvent(exception, "DATABASE_ERROR", null);
        errorEvent.setDatabaseQuery(query);
        extractDatabaseErrorInfo(exception, errorEvent);
        systemEventProducer.publishErrorLogEvent(errorEvent);
    }


    public void logApiError(Exception exception, String apiEndpoint, String httpMethod) {
        Map<String, Object> customData = new HashMap<>();
        customData.put("apiEndpoint", apiEndpoint);
        customData.put("httpMethod", httpMethod);

        logError(exception, "API_ERROR", customData, Arrays.asList("API", "EXTERNAL"));
    }

    // Helper Methods

    private ErrorLogEvent buildBasicErrorEvent(Exception exception, String errorCategory,
                                               Map<String, Object> customData) {
        ErrorLogEvent errorEvent = new ErrorLogEvent();
        errorEvent.setErrorLogUuid(UUID.randomUUID());
        errorEvent.setErrorCode(generateErrorCode(exception));
        errorEvent.setErrorType(exception.getClass().getSimpleName());
        errorEvent.setErrorSeverity(determineSeverity(exception));
        errorEvent.setErrorCategory(errorCategory);
        errorEvent.setErrorMessage(exception.getMessage());
        errorEvent.setStackTrace(getStackTraceAsString(exception));
        errorEvent.setExceptionClass(exception.getClass().getName());
        errorEvent.setCustomData(customData);
        errorEvent.setApplicationName(applicationName);
        errorEvent.setEnvironment(environment.toUpperCase());
        return errorEvent;
    }

    private String generateErrorCode(Exception exception) {
        // Generate unique error code based on exception type and hash
        String prefix = exception.getClass().getSimpleName()
                .replaceAll("Exception", "")
                .toUpperCase();
        int hash = Math.abs(exception.getMessage() != null ?
                exception.getMessage().hashCode() :
                exception.getClass().hashCode());
        return String.format("%s-%05d", prefix, hash % 100000);
    }

    private String determineSeverity(Exception exception) {
        String exceptionName = exception.getClass().getSimpleName().toLowerCase();

        if (exceptionName.contains("nullpointer") ||
                exceptionName.contains("outofmemory") ||
                exceptionName.contains("stackoverflow")) {
            return "CRITICAL";
        } else if (exceptionName.contains("sql") ||
                exceptionName.contains("data") ||
                exceptionName.contains("security")) {
            return "HIGH";
        } else if (exceptionName.contains("illegal") ||
                exceptionName.contains("unsupported")) {
            return "MEDIUM";
        } else if (exceptionName.contains("validation")) {
            return "LOW";
        } else {
            return "MEDIUM";
        }
    }

    private String extractErrorDetail(Exception exception) {
        StringBuilder detail = new StringBuilder();
        detail.append("Exception: ").append(exception.getClass().getName()).append("\n");
        detail.append("Message: ").append(exception.getMessage()).append("\n");

        Throwable cause = exception.getCause();
        if (cause != null) {
            detail.append("Caused by: ").append(cause.getClass().getName()).append("\n");
            detail.append("Cause Message: ").append(cause.getMessage()).append("\n");
        }

        return detail.toString();
    }

    private String getStackTraceAsString(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    private String getRootCause(Exception exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage();
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private Map<String, Object> extractRequestParameters(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().length == 1 ? e.getValue()[0] : Arrays.asList(e.getValue())
                ));
    }

    private Map<String, String> extractRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Skip sensitive headers
            if (!headerName.equalsIgnoreCase("Authorization") &&
                    !headerName.equalsIgnoreCase("Cookie")) {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        return headers;
    }

    private Long extractUserId(Authentication auth) {
        // Extract user ID from authentication principal
        try {
            Object principal = auth.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                // Implement logic to extract user ID
                return 1L; // Placeholder
            }
        } catch (Exception e) {
            log.debug("Could not extract user ID: {}", e.getMessage());
        }
        return null;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String getApplicationVersion() {
        // Get from manifest or properties
        return "1.0.0"; // Placeholder
    }

    private String getServerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private boolean isDatabaseException(Exception exception) {
        String className = exception.getClass().getName().toLowerCase();
        return className.contains("sql") ||
                className.contains("jdbc") ||
                className.contains("hibernate") ||
                className.contains("persistence");
    }

    private void extractDatabaseErrorInfo(Exception exception, ErrorLogEvent errorEvent) {
        try {
            // Extract SQL state and error code for SQL exceptions
            if (exception instanceof java.sql.SQLException) {
                java.sql.SQLException sqlException = (java.sql.SQLException) exception;
                errorEvent.setDatabaseErrorCode(String.valueOf(sqlException.getErrorCode()));
                errorEvent.setDatabaseQuery(sqlException.getMessage());
            }

            // Extract constraint name if it's a constraint violation
            String message = exception.getMessage();
            if (message != null && message.contains("constraint")) {
                int start = message.indexOf("[") + 1;
                int end = message.indexOf("]", start);
                if (start > 0 && end > start) {
                    errorEvent.setDatabaseConstraintName(message.substring(start, end));
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract database error info: {}", e.getMessage());
        }
    }

    private boolean isRetryable(Exception exception) {
        String exceptionName = exception.getClass().getSimpleName().toLowerCase();

        // Retryable exceptions
        return exceptionName.contains("timeout") ||
                exceptionName.contains("connection") ||
                exceptionName.contains("unavailable") ||
                exceptionName.contains("temporary");
    }
}
