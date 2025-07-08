package com.medilabo.authService.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

  private JwtUtil jwtUtil;
  private KeyUtil keyUtil;
  private RSAPrivateKey privateKey;

  @BeforeEach
  void setUp() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    KeyPair keyPair = keyGen.generateKeyPair();
    privateKey = (RSAPrivateKey) keyPair.getPrivate();

    keyUtil = mock(KeyUtil.class);
    when(keyUtil.getPrivateKey()).thenReturn(privateKey);

    jwtUtil = new JwtUtil(keyUtil);
    ReflectionTestUtils.setField(jwtUtil, "expiration", 2L);
  }

  @Test
  void generateToken_shouldReturnValidJwtToken() {
    String userId = "123";
    String username = "testuser";
    String ip = "127.0.0.1";
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteAddr()).thenReturn(ip);

    Instant before = Instant.now();
    String token = jwtUtil.generateToken(userId, username, request);

    assertNotNull(token);
    DecodedJWT decoded = JWT.decode(token);
    assertEquals(username, decoded.getSubject());
    assertEquals(userId, decoded.getClaim("id").asString());
    assertEquals("USER", decoded.getClaim("role").asString());
    assertEquals(ip, decoded.getClaim("ip").asString());
    assertEquals("auth-service", decoded.getIssuer());

    Instant expiresAt = decoded.getExpiresAt().toInstant();
    long diffSeconds = Math.abs(
      expiresAt.getEpochSecond() - before.plusSeconds(2 * 3600).getEpochSecond()
    );
    assertTrue(
      diffSeconds < 5,
      "Expiration should be approximately 2 hours from now"
    );
  }

  @Test
  void generateToken_shouldThrowRuntimeException() {
    KeyUtil badKeyUtil = mock(KeyUtil.class);
    when(badKeyUtil.getPrivateKey()).thenThrow(
      new RuntimeException("Key error")
    );
    JwtUtil brokenJwtUtil = new JwtUtil(badKeyUtil);
    ReflectionTestUtils.setField(brokenJwtUtil, "expiration", 2L);

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    assertThrows(RuntimeException.class, () ->
      brokenJwtUtil.generateToken("id", "user", request)
    );
  }
}
