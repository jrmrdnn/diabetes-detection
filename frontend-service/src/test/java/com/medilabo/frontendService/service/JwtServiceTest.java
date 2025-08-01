package com.medilabo.frontendService.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "expiration", 2L);
    ReflectionTestUtils.setField(jwtService, "authCookieName", "AUTH_TOKEN");
  }

  @Test
  void extractUsernameFromToken_validToken_returnsSubject() {
    String username = "testuser";
    String token = JWT.create()
      .withSubject(username)
      .withIssuedAt(new Date())
      .sign(Algorithm.HMAC256("secret")); // secret is arbitrary for decode

    String result = jwtService.extractUsernameFromToken(token);
    assertEquals(username, result);
  }

  @Test
  void extractUsernameFromToken_tokenWithNoSubject_returnsAnonymous() {
    String token = JWT.create()
      .withIssuedAt(new Date())
      .sign(Algorithm.HMAC256("secret"));

    String result = jwtService.extractUsernameFromToken(token);
    assertEquals("Anonymous", result);
  }

  @Test
  void extractUsernameFromToken_nullOrEmptyToken_returnsNull() {
    assertNull(jwtService.extractUsernameFromToken(null));
    assertNull(jwtService.extractUsernameFromToken(""));
    assertNull(jwtService.extractUsernameFromToken("   "));
  }

  @Test
  void extractUsernameFromToken_invalidToken_returnsNull() {
    String invalidToken = "not.a.jwt.token";
    assertNull(jwtService.extractUsernameFromToken(invalidToken));
  }

  @Test
  void createAuthCookieHeader_addsCookieHeader() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    String token = "jwt.token.value";

    jwtService.createAuthCookieHeader(token, response);

    ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(response).addHeader(headerCaptor.capture(), valueCaptor.capture());

    assertEquals("Set-Cookie", headerCaptor.getValue());
    String cookie = valueCaptor.getValue();
    assertTrue(cookie.contains("AUTH_TOKEN=" + token));
    assertTrue(cookie.contains("HttpOnly"));
    assertTrue(cookie.contains("Secure"));
    assertTrue(cookie.contains("Path=/"));
  }

  @Test
  void deleteAuthCookieHeader_addsExpiredCookieHeader() {
    HttpServletResponse response = mock(HttpServletResponse.class);

    jwtService.deleteAuthCookieHeader(response);

    ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(response).addHeader(headerCaptor.capture(), valueCaptor.capture());

    assertEquals("Set-Cookie", headerCaptor.getValue());
    String cookie = valueCaptor.getValue();
    assertTrue(cookie.contains("AUTH_TOKEN="));
    assertTrue(cookie.contains("Max-Age=0"));
  }
}
