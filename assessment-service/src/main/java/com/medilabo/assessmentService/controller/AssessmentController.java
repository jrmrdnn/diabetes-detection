package com.medilabo.assessmentService.controller;

import com.medilabo.assessmentService.dto.AssessmentDto;
import com.medilabo.assessmentService.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller for handling assessment requests related to diabetes risk.
 * Provides endpoints to assess the diabetes risk for a given patient.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    /**
     * Endpoint to assess the diabetes risk for a patient.
     *
     * @param patientId the UUID of the patient to assess
     * @return AssessmentDto containing the assessment results
     */
    @GetMapping("/{patientId}")
    public AssessmentDto assess(@PathVariable UUID patientId) {
        try {
            return assessmentService.assessDiabetesRisk(patientId);
        } catch (Exception e) {
            log.error("Error assessing diabetes risk for patient {}: {}", patientId, e.getMessage());
            throw new RuntimeException("Error assessing diabetes risk", e);
        }
    }
}
