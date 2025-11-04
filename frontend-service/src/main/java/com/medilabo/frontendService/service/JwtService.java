package com.medilabo.frontendService.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Service for handling JWT-related operations.
 */
@Service
public class JwtService {

  @Value("${jwt.expiration}")
  private long expiration;

  @Value("${cookie.auth-name}")
  private String authCookieName;

  public String extractUsernameFromToken(String jwtToken) {
    try {
      if (jwtToken == null || jwtToken.trim().isEmpty()) {
        return null;
      }

      DecodedJWT decodedJWT = JWT.decode(jwtToken);
      String subject = decodedJWT.getSubject();
      return subject != null ? subject : "Anonymous";
    } catch (JWTDecodeException | IllegalArgumentException e) {
      return null;
    }
  }

  public void createAuthCookieHeader(
    String token,
    HttpServletResponse httpResponse
  ) {
    ResponseCookie cookie = ResponseCookie.from(authCookieName, token)
      .maxAge(
        Instant.now().plus(expiration, ChronoUnit.HOURS).getEpochSecond() -
        Instant.now().getEpochSecond()
      )
      .httpOnly(true)
      .secure(true)
      .path("/")
      .build();
    httpResponse.addHeader("Set-Cookie", cookie.toString());
  }

  public void deleteAuthCookieHeader(HttpServletResponse httpResponse) {
    ResponseCookie cookie = ResponseCookie.from(authCookieName, "")
      .maxAge(0)
      .build();
    httpResponse.addHeader("Set-Cookie", cookie.toString());
  }
}
