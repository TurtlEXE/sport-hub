package com.mvc.mock_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ActivityTrackingInterceptor activityTrackingInterceptor;

    public WebConfig(ActivityTrackingInterceptor activityTrackingInterceptor) {
        this.activityTrackingInterceptor = activityTrackingInterceptor;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver clr = new CookieLocaleResolver("lang_cookie");
        clr.setDefaultLocale(new java.util.Locale("en"));
        clr.setCookieMaxAge(Duration.ofDays(365));
        clr.setCookiePath("/");
        return clr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang"); // Chuyển ngôn ngữ qua param ?lang=vi hoặc ?lang=en
        return lci;
    }

    @Bean
    public org.springframework.context.support.ResourceBundleMessageSource messageSource() {
        org.springframework.context.support.ResourceBundleMessageSource messageSource = new org.springframework.context.support.ResourceBundleMessageSource();
        messageSource.setBasenames("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(activityTrackingInterceptor)
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/webjars/**", "/api/**", "/favicon.ico");
    }
}
