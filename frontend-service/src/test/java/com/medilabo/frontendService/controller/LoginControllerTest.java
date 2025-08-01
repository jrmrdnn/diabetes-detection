package com.medilabo.frontendService.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.medilabo.frontendService.dto.UserDto;
import com.medilabo.frontendService.feign.AuthFeignClient;
import com.medilabo.frontendService.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

  @Mock
  private JwtService jwtService;

  @Mock
  private AuthFeignClient authFeignClient;

  @Mock
  private Model model;

  @Mock
  private RedirectAttributes redirectAttributes;

  @Mock
  private BindingResult bindingResult;

  @Mock
  private HttpServletResponse httpResponse;

  @InjectMocks
  private LoginController controller;

  @Captor
  private ArgumentCaptor<Object> objectCaptor;

  @BeforeEach
  void setUp() throws Exception {
    Field baseUrlField = LoginController.class.getDeclaredField("baseUrl");
    baseUrlField.setAccessible(true);
    baseUrlField.set(controller, "http://test-base");
  }

  @Test
  void showLogin_addsUserDtoAndReturnsLoginView() {
    String view = controller.showLogin(model);
    assertEquals("login", view);
    verify(model).addAttribute(eq("userDto"), any(UserDto.class));
    verifyNoMoreInteractions(model);
  }

  @Test
  void login_whenBindingHasErrors_returnsLoginView() {
    when(bindingResult.hasErrors()).thenReturn(true);

    UserDto dto = new UserDto();
    String view = controller.login(
      redirectAttributes,
      httpResponse,
      dto,
      bindingResult,
      model
    );

    assertEquals("/login", view);
    verifyNoInteractions(authFeignClient, jwtService);
  }

  @Test
  void login_whenUserNotFound_addsErrorMessageAndReturnsLogin() {
    when(bindingResult.hasErrors()).thenReturn(false);
    UserDto dto = new UserDto();
    when(authFeignClient.auth(dto)).thenReturn("Utilisateur non trouvé");

    String view = controller.login(
      redirectAttributes,
      httpResponse,
      dto,
      bindingResult,
      model
    );

    assertEquals("/login", view);
    verify(model).addAttribute(
      "errorMessage",
      "Nom d'utilisateur ou mot de passe incorrect"
    );
    verifyNoInteractions(jwtService);
  }

  @Test
  void login_success_createsCookieAndRedirectsToApp() {
    when(bindingResult.hasErrors()).thenReturn(false);
    UserDto dto = new UserDto();
    String token = "jwt-token";
    when(authFeignClient.auth(dto)).thenReturn(token);
    when(
      redirectAttributes.addFlashAttribute(
        eq("successMessage"),
        eq("Connexion réussie")
      )
    ).thenReturn(redirectAttributes);

    String view = controller.login(
      redirectAttributes,
      httpResponse,
      dto,
      bindingResult,
      model
    );

    assertEquals("redirect:http://test-base/app", view);
    verify(jwtService).createAuthCookieHeader(token, httpResponse);
    verify(redirectAttributes).addFlashAttribute(
      "successMessage",
      "Connexion réussie"
    );
  }

  @Test
  void login_whenAuthFeignThrows_addsErrorFlashAndReturnsLogin() {
    when(bindingResult.hasErrors()).thenReturn(false);
    UserDto dto = new UserDto();
    when(authFeignClient.auth(dto)).thenThrow(new RuntimeException("boom"));
    when(
      redirectAttributes.addFlashAttribute(
        eq("errorMessage"),
        eq("Erreur lors de l'authentification")
      )
    ).thenReturn(redirectAttributes);

    String view = controller.login(
      redirectAttributes,
      httpResponse,
      dto,
      bindingResult,
      model
    );

    assertEquals("/login", view);
    verify(redirectAttributes).addFlashAttribute(
      "errorMessage",
      "Erreur lors de l'authentification"
    );
    verifyNoInteractions(jwtService);
  }

  @Test
  void logout_success_deletesCookieAndRedirectsToLogin() {
    when(
      redirectAttributes.addFlashAttribute(
        eq("successMessage"),
        eq("Déconnexion réussie")
      )
    ).thenReturn(redirectAttributes);

    String view = controller.logout(redirectAttributes, httpResponse);

    assertEquals("redirect:http://test-base/login", view);
    verify(jwtService).deleteAuthCookieHeader(httpResponse);
    verify(redirectAttributes).addFlashAttribute(
      "successMessage",
      "Déconnexion réussie"
    );
  }

  @Test
  void logout_whenDeleteThrows_addsErrorFlashAndRedirectsToLogin() {
    doThrow(new RuntimeException("boom"))
      .when(jwtService)
      .deleteAuthCookieHeader(httpResponse);
    when(
      redirectAttributes.addFlashAttribute(
        eq("errorMessage"),
        eq("Erreur lors de la déconnexion")
      )
    ).thenReturn(redirectAttributes);

    String view = controller.logout(redirectAttributes, httpResponse);

    assertEquals("redirect:http://test-base/login", view);
    verify(jwtService).deleteAuthCookieHeader(httpResponse);
    verify(redirectAttributes).addFlashAttribute(
      "errorMessage",
      "Erreur lors de la déconnexion"
    );
  }
}
