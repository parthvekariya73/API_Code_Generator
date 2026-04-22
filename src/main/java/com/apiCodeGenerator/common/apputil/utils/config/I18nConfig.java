package com.apiCodeGenerator.common.apputil.utils.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Internationalization (i18n) Configuration.
 *
 * LOCALE RESOLUTION STRATEGY (priority order):
 *  1. ?lang=xx  query param  →  stored in cookie for subsequent requests
 *  2. Cookie (ri_locale)     →  set by previous ?lang= request
 *  3. Accept-Language header →  browser/client preference (parsed from cookie default)
 *  4. Default                →  English (en)
 *
 * WHY CookieLocaleResolver instead of AcceptHeaderLocaleResolver?
 *  AcceptHeaderLocaleResolver is READ-ONLY — it cannot be changed at runtime.
 *  LocaleChangeInterceptor (which handles ?lang=xx) calls resolver.setLocale(),
 *  which throws UnsupportedOperationException on AcceptHeaderLocaleResolver.
 *  CookieLocaleResolver supports both reading AND writing, solving the error.
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /** Cookie name stored in the client browser / API client */
    public static final String LOCALE_COOKIE_NAME = "ri_locale";

    /** Query param to switch language:  POST /api/v1/auth/login?lang=fr */
    public static final String LOCALE_PARAM_NAME = "lang";

    /**
     * Supported locales.
     * Add new locales here after adding the matching messages_xx.properties file.
     */
    public static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
            Locale.ENGLISH,                     // en
            Locale.forLanguageTag("ar"),        // ar - Arabic
            Locale.FRENCH,                      // fr - French
            Locale.GERMAN,                      // de - German
            Locale.forLanguageTag("es"),        // es - Spanish
            Locale.forLanguageTag("hi"),        // hi - Hindi
            Locale.CHINESE,                     // zh - Chinese Simplified
            Locale.forLanguageTag("pt")         // pt - Portuguese
    );

    // ──────────────────────────────────────────────────────────────
    // MESSAGE SOURCE
    // Scans: src/main/resources/messages/messages{_lang}.properties
    // ──────────────────────────────────────────────────────────────
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages/messages");
        source.setDefaultEncoding(StandardCharsets.UTF_8.name());
        source.setDefaultLocale(Locale.ENGLISH);
        source.setFallbackToSystemLocale(false);   // always fall back to EN, not JVM locale
        source.setCacheSeconds(3600);              // reload every hour; use -1 to disable in dev
        source.setUseCodeAsDefaultMessage(false);
        return source;
    }

    // ──────────────────────────────────────────────────────────────
    // VALIDATOR — wires MessageSource into Bean Validation
    // Makes @NotBlank / @Size / @Pattern resolve {key} from .properties
    // ──────────────────────────────────────────────────────────────
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource);
        return factory;
    }

    // ──────────────────────────────────────────────────────────────
    // LOCALE RESOLVER — CookieLocaleResolver (supports read + write)
    //
    // Flow:
    //   Request hits LocaleChangeInterceptor → reads ?lang=fr
    //   → calls cookieLocaleResolver.setLocale(response, Locale.FRENCH)
    //   → sets cookie "ri_locale=fr" on response
    //   → next request reads cookie automatically
    // ──────────────────────────────────────────────────────────────
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver(LOCALE_COOKIE_NAME);
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieMaxAge(Duration.ofDays(30));
        resolver.setCookiePath("/");
        resolver.setCookieHttpOnly(true);
        // In production with HTTPS, set this to true:
        // resolver.setCookieSecure(true);
        return resolver;
    }

    // ──────────────────────────────────────────────────────────────
    // LOCALE CHANGE INTERCEPTOR
    // Reads ?lang=xx from any request and updates the cookie locale
    // ──────────────────────────────────────────────────────────────
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName(LOCALE_PARAM_NAME);
        interceptor.setIgnoreInvalidLocale(true); // don't crash on ?lang=gibberish
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}