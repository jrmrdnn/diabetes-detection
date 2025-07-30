package com.medilabo.assessmentService.service;

import com.medilabo.assessmentService.dto.AssessmentDto;
import com.medilabo.assessmentService.dto.NoteDto;
import com.medilabo.assessmentService.dto.PatientDto;
import com.medilabo.assessmentService.enums.Gender;
import com.medilabo.assessmentService.enums.RiskLevel;
import com.medilabo.assessmentService.feign.NoteFeignClient;
import com.medilabo.assessmentService.feign.PatientFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AssessmentServiceTest {

    @Mock
    private NoteFeignClient noteFeignClient;

    @Mock
    private PatientFeignClient patientFeignClient;

    @Mock
    private TriggerTermsLoader termsLoader;

    @InjectMocks
    private AssessmentService assessmentService;

    private UUID patientId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientId = UUID.randomUUID();
    }

    @Test
    void testAssessDiabetesRisk_Success() {
        PatientDto patientDto = new PatientDto();
        patientDto.setLastName(patientId.toString());
        patientDto.setFirstName("John");
        patientDto.setBirthDate(LocalDate.of(1980, 1, 1));
        patientDto.setGender(Gender.M);
        patientDto.setPostalAddress("123 Main St");
        patientDto.setPhoneNumber("1234567890");

        when(patientFeignClient.getPatientById(patientId)).thenReturn(patientDto);

        NoteDto noteDto = new NoteDto();
        noteDto.setPatient(patientId.toString());
        noteDto.setNote("Patient has diabetes");

        when(noteFeignClient.getAllNotesByPatient(patientId)).thenReturn(Collections.singletonList(noteDto));

        Map<String, List<String>> categorizedTerms = new HashMap<>();
        categorizedTerms.put("Category1", List.of("trigger"));
        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        AssessmentDto assessmentDto = assessmentService.assessDiabetesRisk(patientId);
        assertEquals(RiskLevel.NONE, assessmentDto.getRiskLevel());
        assertEquals(1, assessmentDto.getTriggerTermsCount());
    }

    @Test
    void testAssessDiabetesRisk_PatientNotFound() {
        when(patientFeignClient.getPatientById(patientId)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                assessmentService.assessDiabetesRisk(patientId)
        );

        assertEquals("Patient not found", exception.getMessage());
    }

    @Test
    void testAssessDiabetesRisk_FeignException() {
        PatientDto patientDto = new PatientDto();
        patientDto.setLastName(patientId.toString());
        patientDto.setFirstName("Jane");
        patientDto.setBirthDate(LocalDate.of(1990, 5, 10));
        patientDto.setGender(Gender.F);
        patientDto.setPostalAddress("456 Main St");
        patientDto.setPhoneNumber("987-654-3210");

        when(patientFeignClient.getPatientById(patientId)).thenReturn(patientDto);
        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.GET,
                "",
                Collections.emptyMap(),
                null,
                null,
                null
        );
        when(noteFeignClient.getAllNotesByPatient(patientId)).thenThrow(
                new feign.FeignException.BadRequest("", request, null, java.util.Collections.emptyMap())
        );

        RuntimeException exception = assertThrows(RuntimeException.class, () -> assessmentService.assessDiabetesRisk(patientId));
        assertEquals("Error assessing diabetes risk: Feign client error", exception.getMessage());
    }

    @Test
    void testAssessDiabetesRisk_NullPointerException() {
        PatientDto patientDto = new PatientDto();
        patientDto.setLastName(patientId.toString());
        patientDto.setFirstName("Jane");
        patientDto.setBirthDate(LocalDate.of(1990, 5, 10));
        patientDto.setGender(Gender.F);
        patientDto.setPostalAddress("456 Main St");
        patientDto.setPhoneNumber("987-654-3210");

        when(patientFeignClient.getPatientById(patientId)).thenReturn(patientDto);

        when(noteFeignClient.getAllNotesByPatient(patientId)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                assessmentService.assessDiabetesRisk(patientId)
        );

        assertEquals("Error assessing diabetes risk: Null value encountered", exception.getMessage());
    }

    @Test
    void testCountTriggerTerms_NoTriggers() throws Exception {
        NoteDto noteDto = new NoteDto();
        noteDto.setPatient(patientId.toString());
        noteDto.setNote("No trigger terms");

        List<NoteDto> notes = Collections.singletonList(noteDto);

        Map<String, List<String>> categorizedTerms = new HashMap<>();
        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        Method method = AssessmentService.class.getDeclaredMethod("countTriggerTerms", List.class);

        method.setAccessible(true);
        int count = (int) method.invoke(assessmentService, notes);

        assertEquals(0, count);
    }

    @Test
    void testCountTriggerTerms_MultipleTriggers() throws Exception {
        NoteDto noteDto = new NoteDto();
        noteDto.setPatient(patientId.toString());
        noteDto.setNote("Note with trigger1 and trigger2");

        List<NoteDto> notes = Collections.singletonList(noteDto);
        Map<String, List<String>> categorizedTerms = new HashMap<>();

        categorizedTerms.put("Category1", Arrays.asList("trigger1", "trigger3"));
        categorizedTerms.put("Category2", List.of("trigger2"));

        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        Method method = AssessmentService.class.getDeclaredMethod("countTriggerTerms", List.class);
        method.setAccessible(true);
        int count = (int) method.invoke(assessmentService, notes);

        assertEquals(2, count);
    }

    @Test
    void testCountTriggerTerms_EmptyNotes() throws Exception {
        List<NoteDto> notes = Collections.emptyList();
        Map<String, List<String>> categorizedTerms = new HashMap<>();
        categorizedTerms.put("Category1", Arrays.asList("trigger1", "trigger2"));
        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        Method method = AssessmentService.class.getDeclaredMethod("countTriggerTerms", List.class);
        method.setAccessible(true);

        int count = (int) method.invoke(assessmentService, notes);

        assertEquals(0, count);
    }

    @Test
    void testCountTriggerTerms_NullNote() throws Exception {
        NoteDto noteDto1 = new NoteDto();
        noteDto1.setPatient(patientId.toString());
        noteDto1.setNote("Valid note with trigger1");

        NoteDto noteDto2 = new NoteDto();
        noteDto2.setPatient(patientId.toString());
        noteDto2.setNote(null);

        List<NoteDto> notes = Arrays.asList(noteDto1, noteDto2);
        Map<String, List<String>> categorizedTerms = new HashMap<>();

        categorizedTerms.put("Category1", List.of("trigger1"));
        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        Method method = AssessmentService.class.getDeclaredMethod("countTriggerTerms", List.class);
        method.setAccessible(true);

        int count = (int) method.invoke(assessmentService, notes);

        assertEquals(1, count);
    }

    @Test
    void testCountTriggerTerms_EmptyNote() throws Exception {
        NoteDto noteDto1 = new NoteDto();
        noteDto1.setPatient(patientId.toString());
        noteDto1.setNote("");

        NoteDto noteDto2 = new NoteDto();
        noteDto2.setPatient(patientId.toString());
        noteDto2.setNote("   ");

        List<NoteDto> notes = Arrays.asList(noteDto1,noteDto2);

        Map<String, List<String>> categorizedTerms = new HashMap<>();
        categorizedTerms.put("Category1", List.of("trigger1"));
        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        Method method = AssessmentService.class.getDeclaredMethod("countTriggerTerms", List.class);
        method.setAccessible(true);

        int count = (int) method.invoke(assessmentService, notes);

        assertEquals(0, count);
    }

    @Test
    void testCountTriggerTerms_CaseInsensitive() throws Exception {
        NoteDto noteDto = new NoteDto();
        noteDto.setPatient(patientId.toString());
        noteDto.setNote("Patient has TRIGGER1 and Trigger2");

        List<NoteDto> notes = Collections.singletonList(noteDto);

        Map<String, List<String>> categorizedTerms = new HashMap<>();
        categorizedTerms.put("Category1", List.of("trigger1"));
        categorizedTerms.put("Category2", List.of("trigger2"));

        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        Method method = AssessmentService.class.getDeclaredMethod("countTriggerTerms", List.class);
        method.setAccessible(true);

        int count = (int) method.invoke(assessmentService, notes);

        assertEquals(2, count);
    }

    @Test
    void testCountTriggerTerms_SameCategoryMultipleTerms() throws Exception {
        NoteDto noteDto = new NoteDto();
        noteDto.setPatient(patientId.toString());
        noteDto.setNote("Patient has trigger1 and trigger3 in same category");

        List<NoteDto> notes = Collections.singletonList(noteDto);

        Map<String, List<String>> categorizedTerms = new HashMap<>();
        categorizedTerms.put("Category1", Arrays.asList("trigger1", "trigger3"));
        categorizedTerms.put("Category2", List.of("trigger2"));

        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        Method method = AssessmentService.class.getDeclaredMethod("countTriggerTerms", List.class);
        method.setAccessible(true);

        int count = (int) method.invoke(assessmentService, notes);

        assertEquals(1, count);
    }

    @Test
    void testCountTriggerTerms_MultipleNotesWithTriggers() throws Exception {
        NoteDto noteDto1 = new NoteDto();
        noteDto1.setPatient(patientId.toString());
        noteDto1.setNote("First note with trigger1");

        NoteDto noteDto2 = new NoteDto();
        noteDto2.setPatient(patientId.toString());
        noteDto2.setNote("Second note with trigger2");

        NoteDto noteDto3 = new NoteDto();
        noteDto3.setPatient(patientId.toString());
        noteDto3.setNote("Third note with trigger1 again");

        List<NoteDto> notes = Arrays.asList(noteDto1, noteDto2, noteDto3);

        Map<String, List<String>> categorizedTerms = new HashMap<>();
        categorizedTerms.put("Category1", List.of("trigger1"));
        categorizedTerms.put("Category2", List.of("trigger2"));

        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        Method method = AssessmentService.class.getDeclaredMethod("countTriggerTerms", List.class);
        method.setAccessible(true);

        int count = (int) method.invoke(assessmentService, notes);

        assertEquals(2, count);
    }

    @Test
    void testCountTriggerTerms_PartialMatch() throws Exception {
        NoteDto noteDto = new NoteDto();
        noteDto.setPatient(patientId.toString());
        noteDto.setNote("Note contains diabetes and diabetic");

        List<NoteDto> notes = Collections.singletonList(noteDto);

        Map<String, List<String>> categorizedTerms = new HashMap<>();
        categorizedTerms.put("Category1", List.of("diabet"));

        when(termsLoader.getCategorizedTerms()).thenReturn(categorizedTerms);

        Method method = AssessmentService.class.getDeclaredMethod("countTriggerTerms", List.class);
        method.setAccessible(true);

        int count = (int) method.invoke(assessmentService, notes);

        assertEquals(1, count);
    }

    @Test
    void testCalculateRiskLevel() throws Exception {
        Method method = AssessmentService.class.getDeclaredMethod("calculateRiskLevel", int.class, String.class, int.class);
        method.setAccessible(true);

        // Test cases NONE
        assertEquals(RiskLevel.NONE, method.invoke(assessmentService, 29, "M", 0));
        assertEquals(RiskLevel.NONE, method.invoke(assessmentService, 29, "F", 0));
        assertEquals(RiskLevel.NONE, method.invoke(assessmentService, 30, "M", 0));

        // Test cases BORDERLINE
        assertNotEquals(RiskLevel.BORDERLINE, method.invoke(assessmentService, 29, "M", 2));
        assertNotEquals(RiskLevel.BORDERLINE, method.invoke(assessmentService, 29, "F", 2));
        assertEquals(RiskLevel.BORDERLINE, method.invoke(assessmentService, 30, "M", 2));

        // Test cases IN_DANGER
        assertEquals(RiskLevel.IN_DANGER, method.invoke(assessmentService, 29, "M", 3));
        assertEquals(RiskLevel.IN_DANGER, method.invoke(assessmentService, 29, "F", 4));
        assertEquals(RiskLevel.IN_DANGER, method.invoke(assessmentService, 30, "M", 6));

        // Test cases EARLY_ONSET
        assertEquals(RiskLevel.EARLY_ONSET, method.invoke(assessmentService, 29, "M", 5));
        assertEquals(RiskLevel.EARLY_ONSET, method.invoke(assessmentService, 29, "F", 7));
        assertEquals(RiskLevel.EARLY_ONSET, method.invoke(assessmentService, 30, "M", 8));
    }
}
