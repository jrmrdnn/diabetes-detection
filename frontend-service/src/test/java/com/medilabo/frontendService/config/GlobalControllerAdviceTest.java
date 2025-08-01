package com.medilabo.frontendService.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.medilabo.frontendService.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

class GlobalControllerAdviceTest {

  private JwtService jwtService;
  private GlobalControllerAdvice globalControllerAdvice;
  private Model model;
  private HttpServletRequest request;

  private static final String AUTH_COOKIE_NAME = "authCookie";
  private static final String JWT_TOKEN = "jwt.token.value";
  private static final String USERNAME = "testUser";

  @BeforeEach
  void setUp() throws Exception {
    jwtService = mock(JwtService.class);
    globalControllerAdvice = new GlobalControllerAdvice(jwtService);
    ReflectionTestUtils.setField(
      globalControllerAdvice,
      "authCookieName",
      AUTH_COOKIE_NAME
    );
    model = mock(Model.class);
    request = mock(HttpServletRequest.class);
  }

  @Test
  void addUsernameToModel_shouldAddUsername_whenAuthCookiePresent() {
    Cookie[] cookies = { new Cookie(AUTH_COOKIE_NAME, JWT_TOKEN) };
    when(request.getCookies()).thenReturn(cookies);
    when(jwtService.extractUsernameFromToken(JWT_TOKEN)).thenReturn(USERNAME);

    globalControllerAdvice.addUsernameToModel(model, request);

    verify(model).addAttribute("username", USERNAME);
  }

  @Test
  void addUsernameToModel_shouldAddEmptyUsername_whenNoCookies() {
    when(request.getCookies()).thenReturn(null);
    when(jwtService.extractUsernameFromToken("")).thenReturn("");

    globalControllerAdvice.addUsernameToModel(model, request);

    verify(model).addAttribute("username", "");
  }

  @Test
  void addUsernameToModel_shouldAddEmptyUsername_whenAuthCookieNotPresent() {
    Cookie[] cookies = { new Cookie("otherCookie", "value") };
    when(request.getCookies()).thenReturn(cookies);
    when(jwtService.extractUsernameFromToken("")).thenReturn("");

    globalControllerAdvice.addUsernameToModel(model, request);

    verify(model).addAttribute("username", "");
  }

  @Test
  void addUsernameToModel_shouldAddUsername_whenMultipleCookiesPresent() {
    Cookie[] cookies = {
      new Cookie("otherCookie", "value"),
      new Cookie(AUTH_COOKIE_NAME, JWT_TOKEN),
    };
    when(request.getCookies()).thenReturn(cookies);
    when(jwtService.extractUsernameFromToken(JWT_TOKEN)).thenReturn(USERNAME);

    globalControllerAdvice.addUsernameToModel(model, request);

    verify(model).addAttribute("username", USERNAME);
  }
}
