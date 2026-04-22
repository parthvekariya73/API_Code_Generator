package com.apiCodeGenerator.common.apputil.utils.securityfilter;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * LocaleFilter — bridges Accept-Language header → CookieLocaleResolver.
 *
 * Problem:
 *   CookieLocaleResolver reads its locale from a cookie. But API clients
 *   (mobile apps, Postman, frontend) typically send "Accept-Language: fr"
 *   rather than a cookie. Without this filter, the cookie resolver ignores
 *   the Accept-Language header and always falls back to English.
 *
 * Solution:
 *   On each request, if NO locale cookie exists yet AND the request has a valid
 *   Accept-Language header, this filter sets the locale via the resolver
 *   (which writes the cookie) so subsequent requests within the session work.
 *
 *   Priority:
 *     ?lang= param > existing cookie > Accept-Language header > default (en)
 *
 * NOTE: This runs BEFORE LocaleChangeInterceptor so ?lang= still wins.
 */
@Component
@Slf4j
public class LocaleFilter extends OncePerRequestFilter {

    private static final String LOCALE_COOKIE_NAME = "ri_locale";
    private static final String LOCALE_PARAM = "lang";

    private static final List<String> SUPPORTED_LANG_TAGS = Arrays.asList(
            "en", "ar", "fr", "de", "es", "hi", "zh", "pt"
    );

    private final LocaleResolver localeResolver;

    public LocaleFilter(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        // If ?lang= is present, LocaleChangeInterceptor handles it — skip
        String langParam = request.getParameter(LOCALE_PARAM);
        if (StringUtils.hasText(langParam)) {
            filterChain.doFilter(request, response);
            return;
        }

        // If cookie already exists — resolver will use it, skip
        if (hasCookie(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // No cookie, no ?lang= → try Accept-Language header
        String acceptLanguage = request.getHeader("Accept-Language");
        if (StringUtils.hasText(acceptLanguage)) {
            Locale resolved = resolveFromAcceptLanguage(acceptLanguage);
            if (resolved != null) {
                try {
                    localeResolver.setLocale(request, response, resolved);
                    log.debug("Locale set from Accept-Language header: {}", resolved);
                } catch (Exception e) {
                    log.warn("Could not set locale from Accept-Language: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Parse Accept-Language header and return best supported locale.
     * e.g. "fr-FR,fr;q=0.9,en;q=0.8" → Locale.FRENCH
     */
    private Locale resolveFromAcceptLanguage(String acceptLanguage) {
        try {
            List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(acceptLanguage);
            for (Locale.LanguageRange range : ranges) {
                String lang = range.getRange().toLowerCase();
                // Match on language subtag only (e.g. "fr" from "fr-FR")
                String primaryLang = lang.contains("-") ? lang.split("-")[0] : lang;
                if (SUPPORTED_LANG_TAGS.contains(primaryLang)) {
                    return Locale.forLanguageTag(primaryLang);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse Accept-Language '{}': {}", acceptLanguage, e.getMessage());
        }
        return null;
    }

    private boolean hasCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;
        return Arrays.stream(cookies)
                .anyMatch(c -> LOCALE_COOKIE_NAME.equals(c.getName())
                        && StringUtils.hasText(c.getValue()));
    }
}
