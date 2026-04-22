package com.apiCodeGenerator.common.apputil.exception;

import com.apiCodeGenerator.common.apputil.exception.custom.AlreadyExistException;
import com.apiCodeGenerator.common.apputil.exception.custom.BusinessException;
import com.apiCodeGenerator.common.apputil.exception.custom.ResourceAlreadyExistsException;
import com.apiCodeGenerator.common.apputil.exception.custom.ResourceNotFoundException;
import com.apiCodeGenerator.common.apputil.response.ApiResponse;
import com.apiCodeGenerator.common.apputil.response.ErrorDetails;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // Error Code Constants
    // =========================================================

    private static final String ERR_VALIDATION        = "VALIDATION_ERROR";
    private static final String ERR_BIND              = "BIND_ERROR";
    private static final String ERR_BAD_REQUEST       = "BAD_REQUEST";
    private static final String ERR_BUSINESS          = "BUSINESS_ERROR";
    private static final String ERR_ALREADY_EXIST     = "ALREADY_EXIST";
    private static final String ERR_UNAUTHORIZED      = "UNAUTHORIZED";
    private static final String ERR_FORBIDDEN         = "FORBIDDEN";
    private static final String ERR_NOT_FOUND         = "NOT_FOUND";
    private static final String ERR_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
    private static final String ERR_CONFLICT          = "CONFLICT";
    private static final String ERR_TIMEOUT           = "TIMEOUT";
    private static final String ERR_SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";
    private static final String ERR_INTERNAL          = "INTERNAL_ERROR";

    private static final String MSG_DEFAULT           = "An unexpected error occurred";
    private static final String MSG_INTERNAL          = "Internal server error";
    private static final String MSG_NOT_FOUND         = "Resource not found";
    private static final String MSG_CONFLICT          = "Data conflict or duplicate entry";
    private static final String MSG_TIMEOUT           = "Request timeout";
    private static final String MSG_SERVICE_DOWN      = "Service unavailable";

    // =========================================================
    // Utility Helpers
    // =========================================================

    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId != null ? requestId : UUID.randomUUID().toString();
    }

    private ResponseEntity<ApiResponse<?>> respond(
            HttpStatus status, String message, String errorCode,
            List<ErrorDetails.FieldIssue> issues, HttpServletRequest request) {

        return ResponseEntity.status(status)
                .body(ApiResponse.error(message, errorCode, issues, getRequestId(request)));
    }

    private ResponseEntity<ApiResponse<?>> respond(
            HttpStatus status, String message, String errorCode, HttpServletRequest request) {
        return respond(status, message, errorCode, Collections.emptyList(), request);
    }

    private ResponseEntity<ApiResponse<?>> bad(String message, String errorCode, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, message, errorCode, request);
    }

    private ResponseEntity<ApiResponse<?>> internal(String message, HttpServletRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, message, ERR_INTERNAL, request);
    }

    // =========================================================
    // 400 — Validation (with field issues list)
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<ErrorDetails.FieldIssue> issues = ex.getBindingResult()
                .getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField))
                .values().stream()
                .map(errors -> {
                    FieldError first = errors.get(0);
                    return ErrorDetails.FieldIssue.builder()
                            .field(first.getField())
                            .issue(first.getDefaultMessage())
                            .build();
                })
                .collect(Collectors.toList());

        log.warn("Validation failed: {} field error(s)", issues.size());
        return respond(HttpStatus.UNPROCESSABLE_CONTENT,
                "Validation failed", ERR_VALIDATION, issues, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ErrorDetails.FieldIssue> issues = ex.getConstraintViolations().stream()
                .map(v -> ErrorDetails.FieldIssue.builder()
                        .field(v.getPropertyPath().toString())
                        .issue(v.getMessage())
                        .build())
                .collect(Collectors.toList());

        log.warn("Constraint violation: {} issue(s)", issues.size());
        return respond(HttpStatus.BAD_REQUEST,
                "Validation failed", ERR_VALIDATION, issues, request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<?>> handleBindException(
            BindException ex, HttpServletRequest request) {

        List<ErrorDetails.FieldIssue> issues = ex.getAllErrors().stream()
                .map(error -> {
                    String field = error instanceof FieldError fe
                            ? fe.getField() : error.getObjectName();
                    return ErrorDetails.FieldIssue.builder()
                            .field(field)
                            .issue(error.getDefaultMessage())
                            .build();
                })
                .collect(Collectors.toList());

        log.warn("Bind exception: {} issue(s)", issues.size());
        return respond(HttpStatus.BAD_REQUEST,
                "Request binding failed", ERR_BIND, issues, request);
    }

    // =========================================================
    // 400 — Bad Request / Input
    // =========================================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        String message = "Malformed JSON request or invalid data types";
        if (ex.getCause() instanceof InvalidFormatException ife) {
            message = String.format("Invalid value '%s' for field type '%s'",
                    ife.getValue(), ife.getTargetType().getSimpleName());
        }
        log.warn("Unreadable request: {}", ex.getMessage());
        return bad(message, ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' value '%s' is invalid for type '%s'",
                ex.getName(), ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        log.warn("Type mismatch: {}", message);
        return bad(message, ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        String message = String.format("Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());
        log.warn("Missing parameter: {}", message);
        return bad(message, ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingPathVariable(
            MissingPathVariableException ex, HttpServletRequest request) {

        String message = String.format("Required path variable '%s' is missing", ex.getVariableName());
        log.warn("Missing path variable: {}", message);
        return bad(message, ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingRequestHeader(
            MissingRequestHeaderException ex, HttpServletRequest request) {

        String message = String.format("Required header '%s' is missing", ex.getHeaderName());
        log.warn("Missing header: {}", message);
        return bad(message, ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        log.warn("Validation exception: {}", ex.getMessage());
        return bad("Validation failed: " + ex.getMessage(), ERR_VALIDATION, request);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ApiResponse<?>> handleNumberFormat(
            NumberFormatException ex, HttpServletRequest request) {
        log.warn("Number format error: {}", ex.getMessage());
        return bad("Invalid number format", ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Already Exist: {}", ex.getMessage());
        return bad(ex.getMessage(), ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiResponse<?>> handleDateTimeParse(
            DateTimeParseException ex, HttpServletRequest request) {
        log.warn("Date parse error: {}", ex.getMessage());
        return bad("Invalid date format", ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<ApiResponse<?>> handleDateTimeException(
            DateTimeException ex, HttpServletRequest request) {
        log.warn("Date time error: {}", ex.getMessage());
        return bad("Invalid date/time value", ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return bad(ex.getMessage(), ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiResponse<?>> handleUnsupportedOperation(
            UnsupportedOperationException ex, HttpServletRequest request) {
        log.warn("Unsupported operation: {}", ex.getMessage());
        return bad("Operation not supported", ERR_BAD_REQUEST, request);
    }

    // =========================================================
    // 400 — Business
    // =========================================================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(
            BusinessException ex, HttpServletRequest request) {
        log.warn("Business exception: {}", ex.getMessage());
        return bad(ex.getMessage(), ERR_BUSINESS, request);
    }

    // =========================================================
    // 401 — Authentication
    // =========================================================

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class,
            org.springframework.security.core.AuthenticationException.class
    })
    public ResponseEntity<ApiResponse<?>> handleAuthentication(
            Exception ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return respond(HttpStatus.UNAUTHORIZED, "Invalid credentials", ERR_UNAUTHORIZED, request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<?>> handleDisabled(
            DisabledException ex, HttpServletRequest request) {
        log.warn("Account disabled: {}", ex.getMessage());
        return respond(HttpStatus.UNAUTHORIZED, "Account is disabled", ERR_UNAUTHORIZED, request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<?>> handleLocked(
            LockedException ex, HttpServletRequest request) {
        log.warn("Account locked: {}", ex.getMessage());
        return respond(HttpStatus.UNAUTHORIZED, "Account is locked", ERR_UNAUTHORIZED, request);
    }

    // =========================================================
    // 403 — Forbidden
    // =========================================================

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return respond(HttpStatus.FORBIDDEN, "Access denied", ERR_FORBIDDEN, request);
    }

    // =========================================================
    // 404 — Not Found
    // =========================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, ex.getMessage(), ERR_NOT_FOUND, request);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<?>> handleNoHandlerFound(
            Exception ex, HttpServletRequest request) {
        log.warn("Endpoint not found: {}", ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, MSG_NOT_FOUND, ERR_NOT_FOUND, request);
    }

    // =========================================================
    // 405 / 415 — Method / Media Type
    // =========================================================

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        String message = String.format("Method '%s' is not supported for this endpoint", ex.getMethod());
        log.warn("Method not supported: {}", message);
        return respond(HttpStatus.METHOD_NOT_ALLOWED, message, ERR_METHOD_NOT_ALLOWED, request);
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handleMediaTypeNotSupported(
            org.springframework.web.HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        String message = String.format("Content type '%s' is not supported", ex.getContentType());
        log.warn("Media type not supported: {}", message);
        return respond(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, ERR_BAD_REQUEST, request);
    }

    // =========================================================
    // 408 — Timeout
    // =========================================================

    @ExceptionHandler({SocketTimeoutException.class, AsyncRequestTimeoutException.class})
    public ResponseEntity<ApiResponse<?>> handleTimeout(
            Exception ex, HttpServletRequest request) {
        log.warn("Timeout: {}", ex.getMessage());
        return respond(HttpStatus.REQUEST_TIMEOUT, MSG_TIMEOUT, ERR_TIMEOUT, request);
    }

    // =========================================================
    // 413 — Payload / Multipart
    // =========================================================

//    @ExceptionHandler(MaxUploadSizeExceededException.class)
//    public ResponseEntity<ApiResponse<?>> handleMaxUploadSize(
//            MaxUploadSizeExceededException ex, HttpServletRequest request) {
//        log.warn("Upload size exceeded: {}", ex.getMessage());
//        return respond(HttpStatus.CONTENT_TOO_LARGE,
//                "File size exceeds maximum limit", ERR_BAD_REQUEST, request);
//    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleFileSizeLimitExceededException(
            FileSizeLimitExceededException ex, HttpServletRequest request) {
        log.warn("Upload size exceeded: {}", ex.getMessage());
        return respond(HttpStatus.CONTENT_TOO_LARGE,
                "File size exceeds maximum limit", ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<?>> handleMultipart(
            MultipartException ex, HttpServletRequest request) {
        log.warn("Multipart error: {}", ex.getMessage());
        return bad("File upload error: " + ex.getMessage(), ERR_BAD_REQUEST, request);
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, InvalidParameterException.class})
    public ResponseEntity<ApiResponse<?>> handleFileSizeExceeded(Exception ex,  HttpServletRequest request) {
        return respond(HttpStatus.CONTENT_TOO_LARGE,
                "File size exceeds maximum limit", ERR_BAD_REQUEST, request);
    }

    // =========================================================
    // 503 — Service Unavailable
    // =========================================================

    @ExceptionHandler({ConnectException.class, UnknownHostException.class})
    public ResponseEntity<ApiResponse<?>> handleServiceUnavailable(
            Exception ex, HttpServletRequest request) {
        log.error("Service unavailable: {}", ex.getMessage());
        return respond(HttpStatus.SERVICE_UNAVAILABLE,
                MSG_SERVICE_DOWN, ERR_SERVICE_UNAVAILABLE, request);
    }

    // =========================================================
    // 500 — HTTP
    // =========================================================

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex, HttpServletRequest request) {
        log.error("Error writing response: {}", ex.getMessage());
        return internal("Error generating response", request);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<?>> handleIO(
            IOException ex, HttpServletRequest request) {
        log.error("IO error: {}", ex.getMessage(), ex);
        return internal("IO operation failed", request);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<?>> handleNullPointer(
            NullPointerException ex, HttpServletRequest request) {
        log.error("Null pointer: {}", ex.getMessage(), ex);
        return internal(MSG_INTERNAL, request);
    }

    // =========================================================
    // Fallback
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return internal(MSG_DEFAULT, request);
    }
}
