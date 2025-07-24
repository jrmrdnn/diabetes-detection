package com.medilabo.frontendService.controller;

import com.medilabo.frontendService.dto.Gender;
import com.medilabo.frontendService.dto.NoteDto;
import com.medilabo.frontendService.dto.PatientDto;
import com.medilabo.frontendService.dto.PatientsDto;
import com.medilabo.frontendService.feign.NoteFeignClient;
import com.medilabo.frontendService.feign.PatientFeignClient;
import com.medilabo.frontendService.service.PatientService;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientFeignClient patientFeignClient;
    private final NoteFeignClient noteFeignClient;
    private final PatientService patientService;

    @Value("${baseUrl}")
    private String baseUrl;

    @GetMapping("/{id}")
    public String showPatient(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        try {
            PatientDto patient = patientFeignClient.getPatientById(id);
            model.addAttribute("patient", patient);
            model.addAttribute("age", patientService.calculateAge(patient.getBirthDate()));
            model.addAttribute("notes", noteFeignClient.getNotesByPatient(id.toString(), page - 1, size));
            model.addAttribute("noteDto", new NoteDto());
        } catch (FeignException e) {
            model.addAttribute("errorMessage", "Patient introuvable pour l’ID : " + id);
            return "patient";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Une erreur inattendue est survenue");
            return "patient";
        }
        return "patient";
    }

    @GetMapping("/edit/{id}")
    public String editPatient(@PathVariable UUID id, Model model) {
        try {
            PatientsDto.Patient patient = patientFeignClient.getPatientById(id);
            model.addAttribute("patient", patient);
            model.addAttribute("patientDto", patientService.getPatientDto(patient));
        } catch (FeignException e) {
            model.addAttribute("errorMessage", "Patient introuvable pour l’ID : " + id);
        }

        return "edit-patient";
    }

    @GetMapping("/add")
    public String addPatient(Model model) {
        model.addAttribute("patientDto", new PatientDto());
        model.addAttribute("genders", Gender.values());
        return "add-patient";
    }

    @PostMapping("/add")
    public String addPatient(
            RedirectAttributes redirectAttributes,
            @Valid @ModelAttribute PatientDto PatientDto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            return "add-patient";
        }

        try {
            String id = patientFeignClient.addPatient(PatientDto);
            redirectAttributes.addFlashAttribute("successMessage", "Patient ajouté avec succès");
            return "redirect:" + baseUrl + "/patient/" + id;
        } catch (FeignException e) {
            model.addAttribute("genders", Gender.values());
            model.addAttribute("errorMessage", "Erreur lors de l'ajout du patient");
        }

        return "add-patient";
    }

    @PutMapping("/edit/{id}")
    public String editPatient(
            RedirectAttributes redirectAttributes,
            @PathVariable UUID id,
            @Valid @ModelAttribute PatientDto PatientDto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            return "edit-patient";
        }

        try {
            patientFeignClient.updatePatient(id.toString(), PatientDto);
            redirectAttributes.addFlashAttribute("successMessage", "Patient mis à jour avec succès");
            return "redirect:" + baseUrl + "/patient/" + id;
        } catch (FeignException e) {
            model.addAttribute("genders", Gender.values());
            model.addAttribute("errorMessage", "Erreur lors de la mise à jour du patient");
        }

        return "edit-patient";
    }
}
