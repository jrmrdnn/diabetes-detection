package com.medilabo.frontendService.config;

import com.medilabo.frontendService.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * GlobalControllerAdvice is a Spring Controller Advice that adds the username
 * extracted from a JWT token in a cookie to the model for all controllers.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final JwtService jwtService;

    @Value("${cookie.auth-name}")
    private String authCookieName;

    /**
     * Adds the username extracted from the JWT token in the cookie to the model.
     *
     * @param model   the model to which the username will be added
     * @param request the HTTP request containing cookies
     */
    @ModelAttribute
    public void addUsernameToModel(Model model, HttpServletRequest request) {
        String jwtToken = "";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(authCookieName)) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }
        String username = jwtService.extractUsernameFromToken(jwtToken);
        model.addAttribute("username", username);
    }
}
