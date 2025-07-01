package com.medilabo.frontendService.controller;

import com.medilabo.frontendService.dto.PatientsDto;
import com.medilabo.frontendService.feign.PatientFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppController {

    private final PatientFeignClient patientFeignClient;

    @Value("${baseUrl}")
    private String baseUrl;

    @GetMapping
    public String showApp(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "asc") String sort,
            @RequestParam(defaultValue = "lastName") String sortBy,
            Model model
    ) {
        try {
            if (page < 1 || size < 1 || size > 50) return ("redirect:" + baseUrl + "/login");
            PatientsDto patients = patientFeignClient.getAllPatients(page - 1, size, sort, sortBy);
            if (patients.getTotalPages() < page) return ("redirect:" + baseUrl + "/app");
            model.addAttribute("patients", patients);
            return "app";
        } catch (Exception e) {
            System.err.println("Error fetching data: " + e.getMessage());
            model.addAttribute(
                    "errorMessage",
                    "Une erreur s'est produite lors de la récupération des données. Veuillez réessayer plus tard."
            );
            return "app";
        }
    }
}
