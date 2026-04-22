package com.apiCodeGenerator.common.apputil.security.filters;

import com.apiCodeGenerator.common.apputil.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SecurityExceptionHandler
        implements AccessDeniedHandler, AuthenticationEntryPoint {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ─── 403: Authenticated but no permission ───────────────────
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        log.warn("Access denied — URI={}, user={}, reason={}",
                request.getRequestURI(),
                request.getUserPrincipal() != null
                        ? request.getUserPrincipal().getName() : "unknown",
                ex.getMessage());

        writeError(response, request,
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource.");
    }

    // ─── 401: Not authenticated at all ──────────────────────────
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        log.warn("Unauthorized access — URI={}, reason={}",
                request.getRequestURI(), ex.getMessage());

        writeError(response, request,
                HttpStatus.UNAUTHORIZED,
                "Authentication required. Please provide a valid token.");
    }

    // ─── Shared writer ───────────────────────────────────────────
    private void writeError(HttpServletResponse response,
                            HttpServletRequest request,
                            HttpStatus status,
                            String message) throws IOException {

        String requestId = request.getHeader("X-Request-ID");

        ApiResponse<Void> body = ApiResponse.error(message, null, requestId);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }
}





