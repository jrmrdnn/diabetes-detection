package com.medilabo.assessmentService.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.medilabo.assessmentService.dto.AssessmentDto;
import com.medilabo.assessmentService.enums.RiskLevel;
import com.medilabo.assessmentService.feign.NoteFeignClient;
import com.medilabo.assessmentService.feign.PatientFeignClient;
import com.medilabo.assessmentService.dto.NoteDto;
import com.medilabo.assessmentService.dto.PatientDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for assessing diabetes risk based on patient data and notes.
 * It calculates the risk level based on age and the presence of specific trigger terms in patient notes.
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final NoteFeignClient noteFeignClient;
    private final TriggerTermsLoader termsLoader;
    private final PatientFeignClient patientFeignClient;

    /**
     * Assesses a given patient's diabetes risk.
     * It retrieves patient information and notes, counts triggers,
     * and calculates the risk level based on their data.
     * @param patientId the UUID of the patient to assess
     * @return AssessmentDto containing the risk level and count of trigger terms
     * @throws RuntimeException if the patient is not found or an error occurs during assessment
     * @see RiskLevel
     * @see NoteFeignClient
     * @see PatientFeignClient
     * @see NoteDto
     * @see PatientDto
     * @see AssessmentDto
     **/
    public AssessmentDto assessDiabetesRisk(UUID patientId) {
        try {
            PatientDto patient = patientFeignClient.getPatientById(patientId);
            if (patient == null) throw new RuntimeException("Patient not found");

            List<NoteDto> notes = noteFeignClient.getAllNotesByPatient(patientId);

            int age = Period.between(patient.getBirthDate(), LocalDate.now()).getYears();

            int triggerCount = countTriggerTerms(notes);

            RiskLevel riskLevel = calculateRiskLevel(age, patient.getGender().name(), triggerCount);

            AssessmentDto assessmentDto = new AssessmentDto();
            assessmentDto.setRiskLevel(riskLevel);
            assessmentDto.setTriggerTermsCount(triggerCount);

            return assessmentDto;
        } catch (feign.FeignException e) {
            log.error("Feign error assessing diabetes risk for patient {}: {}", patientId, e.getMessage());
            throw new RuntimeException("Error assessing diabetes risk: Feign client error");
        } catch (NullPointerException e) {
            log.error("Null pointer error assessing diabetes risk for patient {}: {}", patientId, e.getMessage());
            throw new RuntimeException("Error assessing diabetes risk: Null value encountered");
        }
    }

    /**
     * Counts the number of unique trigger terms found in the patient's notes.
     * Trigger terms are categorized and checked against the content of each note.
     * @param notes List of NoteDto objects containing patient notes
     * @return The count of unique trigger categories found in the notes
     * @see NoteDto
     * @see TriggerTermsLoader
     * @see StringUtils
     * @see Set
     * @see Map
     **/
    private int countTriggerTerms(List<NoteDto> notes) {
        Set<String> uniqueCategories = new HashSet<>();
        Map<String, List<String>> categorizedTerms = termsLoader.getCategorizedTerms();

        for (NoteDto note : notes) {
            String content = note.getNote();
            for (Map.Entry<String, List<String>> category : categorizedTerms.entrySet()) {
                for (String term : category.getValue()) {
                    if (StringUtils.containsIgnoreCase(content, term)) {
                        uniqueCategories.add(category.getKey());
                        break;
                    }
                }
            }
        }

        return uniqueCategories.size();
    }

    /**
     * Calculates the risk level based on the patient's
     * @param age the age of the patient
     * @param gender the gender of the patient (M/F)
     * @param triggerCount the count of unique trigger terms found in the patient's notes
     * @return RiskLevel enum representing the calculated risk level
     **/
    private RiskLevel calculateRiskLevel(int age, String gender, int triggerCount) {
        if (triggerCount == 0) return RiskLevel.NONE;

        boolean isMale = "M".equalsIgnoreCase(gender);
        boolean isUnder30 = age < 30;

        if (isUnder30) {
            if (isMale) {
                if (triggerCount >= 5) return RiskLevel.EARLY_ONSET;
                else if (triggerCount >= 3) return RiskLevel.IN_DANGER;
                else return RiskLevel.NONE;
            } else {
                if (triggerCount >= 7) return RiskLevel.EARLY_ONSET;
                else if (triggerCount >= 4) return RiskLevel.IN_DANGER;
                else return RiskLevel.NONE;
            }
        } else {
            if (triggerCount >= 8) return RiskLevel.EARLY_ONSET;
            else if (triggerCount >= 6) return RiskLevel.IN_DANGER;
            else if (triggerCount >= 2) return RiskLevel.BORDERLINE;
            else return RiskLevel.NONE;
        }
    }
}
