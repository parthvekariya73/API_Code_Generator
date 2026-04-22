package com.apiCodeGenerator.common.apputil.utils.commonutil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public final class SecurityUtils {

    private SecurityUtils() {}

    // USER ID
    // Principal is set as Long userId in JwtAuthenticationFilter
    public static Long getCurrentUserId() {
        return Optional.ofNullable(getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(p -> p instanceof Long)
                .map(p -> (Long) p)
                .orElseGet(() -> {
                    log.warn("[SecurityUtils] No authenticated user found in SecurityContext");
                    return null;
                });
    }

    // ROLE  (e.g. "ROLE_MR", "ROLE_DOCTOR")
    public static String getCurrentUserRole() {
        return Optional.ofNullable(getAuthentication())
                .flatMap(auth -> auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst())
                .orElseGet(() -> {
                    log.warn("[SecurityUtils] No role found in SecurityContext");
                    return null;
                });
    }

    // ROLE ID  — derived from role name using RoleConstants
    // Add more mappings as your user_types grow
    public static Long getCurrentUserRoleId() {
        String role = getCurrentUserRole();
        if (role == null) return null;

        return switch (role) {
            case "ROLE_MR"          -> 1L;
            case "ROLE_DOCTOR"      -> 2L;
            case "ROLE_DISTRIBUTOR" -> 3L;
            case "ROLE_PHARMACY"    -> 4L;
            default -> {
                log.warn("[SecurityUtils] Unknown role: {}", role);
                yield null;
            }
        };
    }

    // IS AUTHENTICATED
    public static boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    // HAS ROLE  — e.g. SecurityUtils.hasRole("MR")
    public static boolean hasRole(String roleCode) {
        return Optional.ofNullable(getAuthentication())
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleCode)))
                .orElse(false);
    }

    // PRIVATE — get Authentication safely
    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
