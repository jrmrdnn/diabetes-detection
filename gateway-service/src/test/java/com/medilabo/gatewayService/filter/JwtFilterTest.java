package com.medilabo.gatewayService.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.medilabo.gatewayService.constant.SecurityConstants;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JwtFilterTest {

  private JwtFilter jwtFilter;
  private String publicKeyPath;
  private RSAPublicKey publicKey;
  private RSAPrivateKey privateKey;
  private final String authCookieName = "auth-token";

  @BeforeEach
  void setUp() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(1024);
    KeyPair keyPair = keyGen.generateKeyPair();
    publicKey = (RSAPublicKey) keyPair.getPublic();
    privateKey = (RSAPrivateKey) keyPair.getPrivate();

    String pubKeyPEM =
      "-----BEGIN PUBLIC KEY-----\n" +
      java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
      "\n-----END PUBLIC KEY-----";
    Path tempPubKey = Files.createTempFile("jwt-test-pub", ".pem");
    Files.writeString(tempPubKey, pubKeyPEM);
    publicKeyPath = tempPubKey.toString();

    jwtFilter = new JwtFilter();
    Field pubKeyField = JwtFilter.class.getDeclaredField("publicKeyPath");
    pubKeyField.setAccessible(true);
    pubKeyField.set(jwtFilter, publicKeyPath);

    Field cookieField = JwtFilter.class.getDeclaredField("authCookieName");
    cookieField.setAccessible(true);
    cookieField.set(jwtFilter, authCookieName);
  }

  @Test
  void filter_shouldAllowPublicEndpoint() {
    String publicEndpoint = SecurityConstants.PUBLIC_ENDPOINTS[0];
    MockServerHttpRequest request = MockServerHttpRequest.get(
      publicEndpoint
    ).build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilterChain chain = mock(GatewayFilterChain.class);
    when(chain.filter(any())).thenReturn(Mono.empty());

    StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();

    verify(chain, times(1)).filter(any());
  }

  @Test
  void filter_shouldAllowRequestToWildcardPublicEndpoint() {
    String publicEndpoint = null;
    for (String endpoint : SecurityConstants.PUBLIC_ENDPOINTS) {
      if (endpoint.contains("*")) {
        publicEndpoint = endpoint.replace("*", "test");
        break;
      }
    }

    if (publicEndpoint == null) {
      throw new IllegalStateException("No wildcard public endpoint found");
    }

    MockServerHttpRequest request = MockServerHttpRequest.get(
      publicEndpoint
    ).build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilterChain chain = mock(GatewayFilterChain.class);
    when(chain.filter(any())).thenReturn(Mono.empty());

    StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();
    verify(chain, times(1)).filter(any());
  }

  @Test
  void filter_shouldRedirectIfTokenMissing() {
    MockServerHttpRequest request = MockServerHttpRequest.get(
      "/private"
    ).build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilterChain chain = mock(GatewayFilterChain.class);

    StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();

    MockServerHttpResponse response = exchange.getResponse();
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertTrue(response.getCookies().containsKey(authCookieName));
    assertEquals(
      "/login#error=missing_token",
      response.getHeaders().getFirst(HttpHeaders.LOCATION)
    );
  }

  @Test
  void filter_shouldRedirectIfTokenInvalid() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/private")
      .header("Authorization", "Bearer invalid.token.value")
      .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilterChain chain = mock(GatewayFilterChain.class);
    when(chain.filter(any())).thenReturn(Mono.empty());

    StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();

    MockServerHttpResponse response = exchange.getResponse();
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertTrue(response.getCookies().containsKey(authCookieName));
    assertEquals(
      "/login#error=invalid_token",
      response.getHeaders().getFirst(HttpHeaders.LOCATION)
    );
  }

  @Test
  void extractToken_shouldReturnNullWithInvalidBearerFormat() throws Exception {
    MockServerHttpRequest request = MockServerHttpRequest.get("/private")
      .header("Authorization", "InvalidBearer token123")
      .build();

    Method extractToken =
      JwtFilter.class.getDeclaredMethod(
          "extractToken",
          ServerHttpRequest.class
        );
    extractToken.setAccessible(true);
    String result = (String) extractToken.invoke(jwtFilter, request);
    assertNull(result);
  }

  @Test
  void filter_shouldAuthenticateAndContinueWithValidToken() {
    String role = "USER";
    String subject = "testuser";
    String token = JWT.create()
      .withIssuer("auth-service")
      .withSubject(subject)
      .withClaim("role", role)
      .sign(Algorithm.RSA512(publicKey, privateKey));

    MockServerHttpRequest request = MockServerHttpRequest.get("/private")
      .header("Authorization", "Bearer " + token)
      .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    GatewayFilterChain chain = mock(GatewayFilterChain.class);
    when(chain.filter(any())).thenReturn(Mono.empty());

    StepVerifier.create(jwtFilter.filter(exchange, chain)).verifyComplete();

    verify(chain, times(1)).filter(any());
  }

  @Test
  void extractToken_shouldReturnTokenFromHeader() throws Exception {
    String token = "headerToken";
    MockServerHttpRequest request = MockServerHttpRequest.get("/private")
      .header("Authorization", "Bearer " + token)
      .build();

    Method extractToken =
      JwtFilter.class.getDeclaredMethod(
          "extractToken",
          ServerHttpRequest.class
        );
    extractToken.setAccessible(true);
    String result = (String) extractToken.invoke(jwtFilter, request);
    assertEquals(token, result);
  }

  @Test
  void extractToken_shouldReturnTokenFromCookie() throws Exception {
    String token = "cookieToken";
    MockServerHttpRequest request = MockServerHttpRequest.get("/private")
      .cookie(new HttpCookie(authCookieName, token))
      .build();

    Method extractToken =
      JwtFilter.class.getDeclaredMethod(
          "extractToken",
          ServerHttpRequest.class
        );
    extractToken.setAccessible(true);
    String result = (String) extractToken.invoke(jwtFilter, request);
    assertEquals(token, result);
  }

  @Test
  void extractToken_shouldReturnNullIfNoToken() throws Exception {
    MockServerHttpRequest request = MockServerHttpRequest.get(
      "/private"
    ).build();

    Method extractToken =
      JwtFilter.class.getDeclaredMethod(
          "extractToken",
          ServerHttpRequest.class
        );
    extractToken.setAccessible(true);
    String result = (String) extractToken.invoke(jwtFilter, request);
    assertNull(result);
  }

  @Test
  void parsePemPublicKey_shouldThrowIfFileMissing() throws Exception {
    Field pubKeyField = JwtFilter.class.getDeclaredField("publicKeyPath");
    pubKeyField.setAccessible(true);
    pubKeyField.set(jwtFilter, "/non/existent/file.pem");

    Method parsePemPublicKey =
      JwtFilter.class.getDeclaredMethod("parsePemPublicKey");
    parsePemPublicKey.setAccessible(true);

    InvocationTargetException ex = assertThrows(
      InvocationTargetException.class,
      () -> parsePemPublicKey.invoke(jwtFilter)
    );
    assertTrue(ex.getCause().getMessage().contains("Error loading public key"));
  }

  @Test
  void parsePemPublicKey_shouldThrowIfKeyInvalid() throws Exception {
    Path invalidKey = Files.createTempFile("jwt-test-invalid", ".pem");
    Files.writeString(invalidKey, "not a valid key");
    Field pubKeyField = JwtFilter.class.getDeclaredField("publicKeyPath");
    pubKeyField.setAccessible(true);
    pubKeyField.set(jwtFilter, invalidKey.toString());

    Method parsePemPublicKey =
      JwtFilter.class.getDeclaredMethod("parsePemPublicKey");
    parsePemPublicKey.setAccessible(true);

    InvocationTargetException ex = assertThrows(
      InvocationTargetException.class,
      () -> parsePemPublicKey.invoke(jwtFilter)
    );
    assertTrue(ex.getCause().getMessage().contains("Error loading public key"));

    Files.deleteIfExists(invalidKey);
  }
}
