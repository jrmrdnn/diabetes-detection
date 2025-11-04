package com.medilabo.frontendService.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling home page requests.
 */
@Controller
public class HomeController {

    @GetMapping
    public String showHome() {
        return "home";
    }
}