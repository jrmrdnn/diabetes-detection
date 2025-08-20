package com.medilabo.frontendService.controller;

import com.medilabo.frontendService.dto.*;
import com.medilabo.frontendService.feign.AssessmentFeignClient;
import com.medilabo.frontendService.feign.NoteFeignClient;
import com.medilabo.frontendService.feign.PatientFeignClient;
import com.medilabo.frontendService.service.PatientService;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final AssessmentFeignClient assessmentFeignClient;
    private final PatientFeignClient patientFeignClient;
    private final NoteFeignClient noteFeignClient;
    private final PatientService patientService;

    @Value("${baseUrl}")
    private String baseUrl;

    @GetMapping("/{id}")
    public String showPatient(
            HttpServletRequest request,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        request.getSession(true);

        initializeDefaultModelAttributes(model, id);

        PatientDto patientDto = loadPatientData(id, model);
        if (patientDto == null) return "patient";

        loadNotesData(id, page - 1, size, model);
        loadAssessmentData(id, model);

        return "patient";
    }

    @GetMapping("/edit/{id}")
    public String editPatient(@PathVariable UUID id, Model model) {
        try {
            PatientDto patientDto = patientFeignClient.getPatientById(id);
            model.addAttribute("patientId", id.toString());
            model.addAttribute("patientDto", patientDto);
        } catch (FeignException e) {
            log.error("Error retrieving patient data for ID {}", id, e);
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
    public String addPatient(RedirectAttributes redirectAttributes, @Valid @ModelAttribute PatientDto PatientDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("genders", Gender.values());
            return "add-patient";
        }

        try {
            String id = patientFeignClient.addPatient(PatientDto);
            redirectAttributes.addFlashAttribute("successMessage", "Patient ajouté avec succès");
            return "redirect:" + baseUrl + "/patient/" + id;
        } catch (FeignException e) {
            log.error("Error adding patient {}", PatientDto, e);
            model.addAttribute("genders", Gender.values());
            model.addAttribute("errorMessage", "Erreur lors de l'ajout du patient");
        }

        return "add-patient";
    }

    @PutMapping("/edit/{id}")
    public String editPatient(RedirectAttributes redirectAttributes, @PathVariable UUID id, @Valid @ModelAttribute PatientDto patientDto, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("patientId", id.toString());
            model.addAttribute("errorMessage", "Erreur lors de la mise à jour du patient");
            return "edit-patient";
        }

        try {
            patientFeignClient.updatePatient(id, patientDto);
            redirectAttributes.addFlashAttribute("successMessage", "Patient mis à jour avec succès");
            return "redirect:" + baseUrl + "/patient/" + id;
        } catch (FeignException e) {
            log.error("Error updating patient {}", patientDto, e);
            model.addAttribute("errorMessage", "Erreur lors de la mise à jour du patient");
        }

        return "edit-patient";
    }

    private void initializeDefaultModelAttributes(Model model, UUID id) {
        model.addAttribute("id", id.toString());
        model.addAttribute("patientDto", new PatientDto());
        model.addAttribute("age", "0");
        model.addAttribute("noteDto", new NoteDto());
        model.addAttribute("assessment", new AssessmentDto());
    }

    private PatientDto loadPatientData(UUID id, Model model) {
        try {
            PatientDto patientDto = patientFeignClient.getPatientById(id);
            model.addAttribute("patientDto", patientDto);
            model.addAttribute("age", patientService.calculateAge(patientDto.getBirthDate()));
            return patientDto;
        } catch (FeignException e) {
            log.error("Error retrieving patient data for ID {}", id, e);
            model.addAttribute("errorMessage", "Patient introuvable pour l'ID : " + id);
        } catch (Exception e) {
            log.error("Unexpected error retrieving patient data for ID {}", id, e);
            model.addAttribute("errorMessage", "Une erreur inattendue est survenue lors du chargement du patient");
        }
        return null;
    }

    private void loadNotesData(UUID id, int pageIndex, int size, Model model) {
        try {
            NotesDto notesDto = noteFeignClient.getNotesByPatient(id.toString(), pageIndex, size);
            model.addAttribute("notes", notesDto);
        } catch (FeignException e) {
            log.error("Error retrieving notes for patient {}", id, e);
            model.addAttribute("notesError", "Notes indisponibles temporairement");
        } catch (Exception e) {
            log.error("Unexpected error retrieving notes for patient {}", id, e);
            model.addAttribute("notesError", "Erreur lors du chargement des notes");
        }
    }

    private void loadAssessmentData(UUID id, Model model) {
        try {
            AssessmentDto assessmentDto = assessmentFeignClient.assess(id);
            model.addAttribute("assessment", assessmentDto);
        } catch (FeignException e) {
            log.error("Error retrieving assessment for patient {}", id, e);
            model.addAttribute("assessmentError", "Évaluation indisponible temporairement");
        } catch (Exception e) {
            log.error("Unexpected error retrieving assessment for patient {}", id, e);
            model.addAttribute("assessmentError", "Erreur lors du chargement de l'évaluation");
        }
    }

}
