package com.medilabo.frontendService.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.medilabo.frontendService.dto.NoteDto;
import com.medilabo.frontendService.feign.NoteFeignClient;

import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller for handling note-related requests.
 * Provides endpoints for adding, updating, and deleting notes.
 */
@Controller
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteFeignClient noteFeignClient;

    @Value("${baseUrl}")
    private String baseUrl;

    @PostMapping
    public String addNote(
            RedirectAttributes redirectAttributes,
            @Valid @ModelAttribute NoteDto noteDto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) return "patient";

        try {
            noteFeignClient.addNote(noteDto);
            redirectAttributes.addFlashAttribute("successMessage", "Note ajoutée avec succès");
            return "redirect:" + baseUrl + "/patient/" + noteDto.getPatient();
        } catch (FeignException e) {
            model.addAttribute("errorMessage", "Erreur lors de l'ajout de la note");
            return "patient";
        }
    }

    @PutMapping("/{noteId}")
    public String updateNote(
            RedirectAttributes redirectAttributes,
            @PathVariable String noteId,
            @Valid @ModelAttribute NoteDto noteDto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Erreur de validation des données");
            return "patient";
        }

        try {
            noteFeignClient.updateNote(noteId, noteDto);
            redirectAttributes.addFlashAttribute("successMessage", "Note mise à jour avec succès");
            return "redirect:" + baseUrl + "/patient/" + noteDto.getPatient();
        } catch (FeignException e) {
            model.addAttribute("errorMessage", "Erreur lors de la mise à jour de la note");
            return "patient";
        }
    }

    @DeleteMapping("/{noteId}")
    public String deleteNote(
            RedirectAttributes redirectAttributes,
            @RequestParam String patientId,
            @PathVariable String noteId,
            Model model
    ) {
        try {
            noteFeignClient.deleteNote(noteId);
            redirectAttributes.addFlashAttribute("successMessage", "Note supprimée avec succès");
            return "redirect:" + baseUrl + "/patient/" + patientId;
        } catch (FeignException e) {
            model.addAttribute("errorMessage", "Erreur lors de la suppression de la note");
            return "redirect:" + baseUrl + "/patient/" + patientId;
        }
    }
}