package com.medilabo.gatewayService.constant;

/**
 * Security constants for the gateway service.
 */
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
