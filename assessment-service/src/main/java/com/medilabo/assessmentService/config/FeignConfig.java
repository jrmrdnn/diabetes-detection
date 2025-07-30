package com.medilabo.assessmentService.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Configuration class for Feign clients to propagate cookies.
 * This configuration ensures that the authentication cookie is included in requests made by Feign clients.
 */
@Configuration
public class FeignConfig {

    @Value("${cookie.auth-name}")
    private String authCookieName;

    /**
     * Creates a RequestInterceptor that propagates the authentication cookie
     * from the incoming HTTP request to outgoing Feign requests.
     * This interceptor checks for the presence of the authentication cookie in the request
     * and adds it to the headers of the Feign request.
     *
     * @return a RequestInterceptor that adds the authentication cookie to Feign requests
     * @see RequestInterceptor
     * @see HttpServletRequest
     * @see Cookie
     * @see ServletRequestAttributes
     * @see RequestContextHolder
     */
    @Bean
    public RequestInterceptor cookiePropagationInterceptor() {
        return template -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                if (request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if (authCookieName.equals(cookie.getName())) {
                            template.header("Cookie", authCookieName + "=" + cookie.getValue());
                            break;
                        }
                    }
                }
            }
        };
    }
}
