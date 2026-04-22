package com.apiCodeGenerator.common.apputil.audit.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Slf4j
public class AuditSecurityContext {

    private AuditSecurityContext() {  }

    public static Optional<Long> findCurrentUserId() {
        return Optional.ofNullable(getAuthentication())
                .filter(AuditSecurityContext::isRealAuthentication)
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof Long)
                .map(p -> (Long) p);
    }

    public static Long getCurrentUserId() {
        return findCurrentUserId()
                .orElseGet(() -> {
                    log.warn("[AuditSecurityContext] No authenticated userId found in SecurityContext");
                    return null;
                });
    }

    public static Optional<String> findCurrentUserRole() {
        return Optional.ofNullable(getAuthentication())
                .filter(AuditSecurityContext::isRealAuthentication)
                .flatMap(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst());
    }

    public static String getCurrentUserRole() {
        return findCurrentUserRole()
                .orElseGet(() -> {
                    log.warn("[AuditSecurityContext] No role found in SecurityContext");
                    return null;
                });
    }

    public static Optional<Long> findCurrentUserRoleId() {
        return findCurrentUserRole()
                .flatMap(role -> switch (role) {
                    case "ROLE_MR"          -> Optional.of(1L);
                    case "ROLE_DOCTOR"      -> Optional.of(2L);
                    case "ROLE_DISTRIBUTOR" -> Optional.of(3L);
                    case "ROLE_PHARMACY"    -> Optional.of(4L);
                    default -> {
                        log.warn("[AuditSecurityContext] Unknown role '{}' — roleId will be null", role);
                        yield Optional.empty();
                    }
                });
    }

    public static Long getCurrentUserRoleId() {
        return findCurrentUserRoleId().orElse(null);
    }

    public static Optional<Long> findCurrentMahId() {

        return Optional.empty();
    }

    public static Long getCurrentMahId() {
        return findCurrentMahId().orElse(null);
    }

    public static Optional<String> findClientIpAddress() {
        return findCurrentRequest()
                .map(req -> {
                    String xff = req.getHeader("X-Forwarded-For");
                    if (isPresent(xff)) return xff.split(",")[0].trim();

                    String xri = req.getHeader("X-Real-IP");
                    if (isPresent(xri)) return xri.trim();

                    String pci = req.getHeader("Proxy-Client-IP");
                    if (isPresent(pci) && !"unknown".equalsIgnoreCase(pci)) return pci.trim();

                    String wlp = req.getHeader("WL-Proxy-Client-IP");
                    if (isPresent(wlp) && !"unknown".equalsIgnoreCase(wlp)) return wlp.trim();

                    return req.getRemoteAddr();
                });
    }

    public static String getClientIpAddress() {
        return findClientIpAddress().orElse(null);
    }

    public static Optional<String> findUserAgent() {
        return findCurrentRequest()
                .map(req -> req.getHeader("User-Agent"))
                .filter(ua -> ua != null && !ua.isBlank());
    }

    public static String getUserAgent() {
        return findUserAgent().orElse(null);
    }

    public static Optional<HttpServletRequest> findCurrentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attrs -> attrs instanceof ServletRequestAttributes)
                .map(attrs -> ((ServletRequestAttributes) attrs).getRequest());
    }

    public static boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }


    private static boolean isRealAuthentication(Authentication auth) {
        return auth.isAuthenticated()
                && auth.getPrincipal() != null
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}