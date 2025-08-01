package com.medilabo.frontendService.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class FeignConfigTest {

  private FeignConfig feignConfig;

  @BeforeEach
  void setUp() {
    feignConfig = new FeignConfig();
    ReflectionTestUtils.setField(feignConfig, "authCookieName", "AUTH_COOKIE");
  }

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void shouldAddAuthCookieToHeaderWhenPresent() {
    Cookie authCookie = new Cookie("AUTH_COOKIE", "cookie-value");
    Cookie otherCookie = new Cookie("OTHER", "other-value");
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getCookies()).thenReturn(
      new Cookie[] { otherCookie, authCookie }
    );
    ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
    when(attrs.getRequest()).thenReturn(request);
    RequestContextHolder.setRequestAttributes(attrs);

    RequestTemplate template = new RequestTemplate();
    RequestInterceptor interceptor = feignConfig.cookiePropagationInterceptor();

    interceptor.apply(template);

    assertThat(template.headers()).containsKey("Cookie");
    assertThat(template.headers().get("Cookie")).containsExactly(
      "AUTH_COOKIE=cookie-value"
    );
  }

  @Test
  void shouldNotAddHeaderWhenNoCookies() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getCookies()).thenReturn(null);
    ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
    when(attrs.getRequest()).thenReturn(request);
    RequestContextHolder.setRequestAttributes(attrs);

    RequestTemplate template = new RequestTemplate();
    RequestInterceptor interceptor = feignConfig.cookiePropagationInterceptor();

    interceptor.apply(template);

    assertThat(template.headers()).doesNotContainKey("Cookie");
  }

  @Test
  void shouldNotAddHeaderWhenAuthCookieNotPresent() {
    Cookie otherCookie = new Cookie("OTHER", "other-value");
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getCookies()).thenReturn(new Cookie[] { otherCookie });
    ServletRequestAttributes attrs = mock(ServletRequestAttributes.class);
    when(attrs.getRequest()).thenReturn(request);
    RequestContextHolder.setRequestAttributes(attrs);

    RequestTemplate template = new RequestTemplate();
    RequestInterceptor interceptor = feignConfig.cookiePropagationInterceptor();

    interceptor.apply(template);

    assertThat(template.headers()).doesNotContainKey("Cookie");
  }

  @Test
  void shouldNotFailWhenNoRequestAttributes() {
    RequestContextHolder.resetRequestAttributes();
    RequestTemplate template = new RequestTemplate();
    RequestInterceptor interceptor = feignConfig.cookiePropagationInterceptor();

    interceptor.apply(template);

    assertThat(template.headers()).doesNotContainKey("Cookie");
  }
}
