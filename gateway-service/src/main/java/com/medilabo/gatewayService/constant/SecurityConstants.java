package com.medilabo.gatewayService.constant;

public final class SecurityConstants {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/",
            "/login",
            "/api/auth",
            "/**.js",
            "/**.css",
            "/**.json",
            "/img/**",
            "/ico/**",
    };
}
