package com.medilabo.frontendService.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.medilabo.frontendService.dto.UserDto;
import com.medilabo.frontendService.feign.AuthFeignClient;
import com.medilabo.frontendService.service.JwtService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for handling login and logout requests.
 */
@Slf4j
@Controller
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {
    private final JwtService jwtService;
    private final AuthFeignClient authFeignClient;

    @Value("${baseUrl}")
    private String baseUrl;

    @GetMapping
    public String showLogin(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "login";
    }

    @PostMapping
    public String login(
            RedirectAttributes redirectAttributes,
            HttpServletResponse httpResponse,
            @Valid @ModelAttribute UserDto userDto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) return "login";

        try {
            String response = authFeignClient.auth(userDto);

            if ("User not found".equals(response)) {
                model.addAttribute("errorMessage", "Nom d'utilisateur ou mot de passe incorrect");
                return "login";
            }

            jwtService.createAuthCookieHeader(response, httpResponse);
            redirectAttributes.addFlashAttribute("successMessage", "Connexion réussie");
            return "redirect:" + baseUrl + "/app";
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification: {}", e.getMessage());
            model.addAttribute("errorMessage", "Erreur lors de l'authentification");
        }
        return "login";
    }

    @DeleteMapping
    public String logout(RedirectAttributes redirectAttributes, HttpServletResponse httpResponse) {
        try {
            jwtService.deleteAuthCookieHeader(httpResponse);
            redirectAttributes.addFlashAttribute("successMessage", "Déconnexion réussie");
            return "redirect:" + baseUrl + "/login";
        } catch (Exception e) {
            log.error("Erreur lors de la déconnexion: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la déconnexion");
            return "redirect:" + baseUrl + "/login";
        }
    }
}
