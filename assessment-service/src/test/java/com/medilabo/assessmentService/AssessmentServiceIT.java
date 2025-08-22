package com.medilabo.assessmentService;

import com.medilabo.assessmentService.dto.AssessmentDto;
import com.medilabo.assessmentService.enums.RiskLevel;
import com.medilabo.assessmentService.feign.NoteFeignClient;
import com.medilabo.assessmentService.feign.PatientFeignClient;
import com.medilabo.assessmentService.dto.NoteDto;
import com.medilabo.assessmentService.dto.PatientDto;
import com.medilabo.assessmentService.enums.Gender;
import com.medilabo.assessmentService.service.AssessmentService;
import com.medilabo.assessmentService.service.TriggerTermsLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class AssessmentServiceIT {
    @Autowired
    private AssessmentService assessmentService;
    @MockBean
    private PatientFeignClient patientFeignClient;
    @MockBean
    private NoteFeignClient noteFeignClient;
    @MockBean
    private TriggerTermsLoader triggerTermsLoader;

    @Test
    void assessDiabetesRisk_shouldReturnNoneRisk_whenNoTriggerTerms() {
        UUID patientId = UUID.randomUUID();
        PatientDto patientDto = new PatientDto();
        patientDto.setFirstName("John");
        patientDto.setLastName("Doe");
        patientDto.setBirthDate(LocalDate.of(1980, 1, 1));
        patientDto.setGender(Gender.M);
        when(patientFeignClient.getPatientById(patientId)).thenReturn(patientDto);
        when(noteFeignClient.getAllNotesByPatient(patientId)).thenReturn(Collections.emptyList());
        Map<String, List<String>> categorizedTerms = new HashMap<>();
        when(triggerTermsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        AssessmentDto result = assessmentService.assessDiabetesRisk(patientId);

        assertEquals(RiskLevel.NONE, result.getRiskLevel());
        assertEquals(0, result.getTriggerTermsCount());
    }

    @Test
    void assessDiabetesRisk_shouldReturnEarlyOnset_whenManyTriggerTermsAndYoungMale() {
        UUID patientId = UUID.randomUUID();
        PatientDto patientDto = new PatientDto();
        patientDto.setFirstName("John");
        patientDto.setLastName("Doe");
        patientDto.setBirthDate(LocalDate.of(LocalDate.now().getYear() - 25, 1, 1));
        patientDto.setGender(Gender.M);
        when(patientFeignClient.getPatientById(patientId)).thenReturn(patientDto);

        NoteDto noteDto = new NoteDto();
        noteDto.setPatient(patientId.toString());
        noteDto.setNote("Patient a du cholestérol, à des vertiges, fume intensif et à de la perte de poids.");
        when(noteFeignClient.getAllNotesByPatient(patientId)).thenReturn(List.of(noteDto));

        Map<String, List<String>> categorizedTerms = new HashMap<>();
        categorizedTerms.put("cholesterol", List.of("cholestérol"));
        categorizedTerms.put("vertige", List.of("vertiges"));
        categorizedTerms.put("fumeur", List.of("fume"));
        categorizedTerms.put("poids", List.of("poids"));
        when(triggerTermsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        AssessmentDto result = assessmentService.assessDiabetesRisk(patientId);

        assertEquals(RiskLevel.IN_DANGER, result.getRiskLevel());
        assertEquals(4, result.getTriggerTermsCount());
    }
}
