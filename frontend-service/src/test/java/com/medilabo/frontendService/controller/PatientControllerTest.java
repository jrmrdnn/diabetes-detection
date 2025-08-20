package com.medilabo.frontendService.controller;

import com.medilabo.frontendService.dto.*;
import com.medilabo.frontendService.feign.AssessmentFeignClient;
import com.medilabo.frontendService.feign.NoteFeignClient;
import com.medilabo.frontendService.feign.PatientFeignClient;
import com.medilabo.frontendService.service.PatientService;
import feign.FeignException;
import feign.Request;
import feign.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PatientControllerTest {

    @Mock
    private AssessmentFeignClient assessmentFeignClient;

    @Mock
    private PatientFeignClient patientFeignClient;

    @Mock
    private NoteFeignClient noteFeignClient;

    @Mock
    private PatientService patientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;


    private PatientController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PatientController(assessmentFeignClient, patientFeignClient, noteFeignClient, patientService);

        Field baseUrlField = ReflectionUtils.findField(PatientController.class, "baseUrl");
        assertNotNull(baseUrlField);
        ReflectionUtils.makeAccessible(baseUrlField);
        ReflectionUtils.setField(baseUrlField, controller, "http://localhost:8080");

        when(request.getSession(true)).thenReturn(session);
    }

    private FeignException makeFeignNotFound() {
        Request req = Request.create(Request.HttpMethod.GET, "/", Collections.emptyMap(), new byte[0], Charset.defaultCharset(), null);
        Response res = Response.builder().request(req).status(404).reason("Not Found").build();
        return FeignException.errorStatus("getPatientById", res);
    }

    @Test
    void showPatient_success_populatesModel() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        PatientsDto.Patient mockPatient = mock(PatientsDto.Patient.class);
        when(patientFeignClient.getPatientById(id)).thenReturn(mockPatient);
        when(mockPatient.getBirthDate()).thenReturn(LocalDate.of(1990, 1, 1));
        doReturn("35").when(patientService).calculateAge(LocalDate.of(1990, 1, 1));
        when(noteFeignClient.getNotesByPatient(eq(id.toString()), anyInt(), anyInt())).thenReturn(mock(NotesDto.class));
        AssessmentDto mockAssessment = mock(AssessmentDto.class);
        when(assessmentFeignClient.assess(id)).thenReturn(mockAssessment);

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertSame(model.getAttribute("patientDto"), mockPatient);
        assertEquals("35", model.getAttribute("age"));
        assertNotNull(model.getAttribute("notes"));
        assertTrue(model.containsAttribute("noteDto"));
        assertNotNull(model.getAttribute("assessment"));
    }

    @Test
    void showPatient_whenFeignException_setsNotFoundMessage() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();
        when(patientFeignClient.getPatientById(id)).thenThrow(makeFeignNotFound());

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertEquals("Patient introuvable pour l'ID : " + id, model.getAttribute("errorMessage"));
    }

    @Test
    void showPatient_whenOtherException_setsGenericError() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();
        when(patientFeignClient.getPatientById(id)).thenThrow(new RuntimeException("boom"));

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertEquals("Une erreur inattendue est survenue lors du chargement du patient", model.getAttribute("errorMessage"));
    }

    @Test
    void editPatientGet_success_populatesModel() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        PatientsDto.Patient patient = mock(PatientsDto.Patient.class);
        PatientDto patientDto = mock(PatientDto.class);

        when(patientFeignClient.getPatientById(id)).thenReturn(patient);
        when(patientService.getPatientDto(patient)).thenReturn(patientDto);

        String view = controller.editPatient(id, model);

        assertEquals("edit-patient", view);
    }

    @Test
    void editPatientGet_whenFeignException_setsError() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        when(patientFeignClient.getPatientById(id)).thenThrow(makeFeignNotFound());

        String view = controller.editPatient(id, model);

        assertEquals("edit-patient", view);
        assertEquals("Patient introuvable pour l’ID : " + id, model.getAttribute("errorMessage"));
    }

    @Test
    void addPatientGet_preparesForm() {
        Model model = new ExtendedModelMap();

        String view = controller.addPatient(model);

        assertEquals("add-patient", view);
        assertNotNull(model.getAttribute("patientDto"));
        Object genders = model.getAttribute("genders");
        assertInstanceOf(Gender[].class, genders);
        assertTrue(((Gender[]) genders).length > 0);
    }

    @Test
    void addPatientPost_withValidationErrors_returnsForm() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);
        Model model = new ExtendedModelMap();

        String view = controller.addPatient(ra, mock(PatientDto.class), result, model);

        assertEquals("add-patient", view);
        assertNotNull(model.getAttribute("genders"));
    }

    @Test
    void addPatientPost_success_redirectsToPatient() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        PatientDto dto = mock(PatientDto.class);
        when(patientFeignClient.addPatient(dto)).thenReturn("the-id");

        String view = controller.addPatient(ra, dto, result, new ExtendedModelMap());

        assertEquals("redirect:http://localhost:8080/patient/the-id", view);
        assertEquals("Patient ajouté avec succès", ra.getFlashAttributes().get("successMessage"));
    }

    @Test
    void addPatientPost_feignException_returnsFormWithError() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        PatientDto dto = mock(PatientDto.class);
        when(patientFeignClient.addPatient(dto)).thenThrow(makeFeignNotFound());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.addPatient(ra, dto, result, model);

        assertEquals("add-patient", view);
        assertNotNull(model.getAttribute("genders"));
        assertEquals("Erreur lors de l'ajout du patient", model.getAttribute("errorMessage"));
    }

    @Test
    void editPatientPut_withValidationErrors_returnsForm() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(true);
        Model model = new ExtendedModelMap();

        String view = controller.editPatient(ra, UUID.randomUUID(), mock(PatientDto.class), result, model);

        assertEquals("edit-patient", view);
        assertNotNull(model.getAttribute("patientId"));
    }

    @Test
    void editPatientPut_success_redirects() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        UUID id = UUID.randomUUID();
        PatientDto dto = mock(PatientDto.class);

        doNothing().when(patientFeignClient).updatePatient(id, dto);

        String view = controller.editPatient(ra, id, dto, result, new ExtendedModelMap());

        assertEquals("redirect:http://localhost:8080/patient/" + id, view);
        assertEquals("Patient mis à jour avec succès", ra.getFlashAttributes().get("successMessage"));
    }

    @Test
    void editPatientPut_feignException_returnsFormWithError() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);

        UUID id = UUID.randomUUID();
        PatientDto dto = mock(PatientDto.class);

        doThrow(makeFeignNotFound()).when(patientFeignClient).updatePatient(id, dto);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editPatient(ra, id, dto, result, model);

        assertEquals("edit-patient", view);
        assertEquals("Erreur lors de la mise à jour du patient", model.getAttribute("errorMessage"));
    }

    @Test
    void loadPatientData_whenFeignException_setsSpecificErrorMessage() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        when(patientFeignClient.getPatientById(id)).thenThrow(makeFeignNotFound());

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertEquals("Patient introuvable pour l'ID : " + id, model.getAttribute("errorMessage"));
        //assertNull(model.getAttribute("patientDto"));
        verify(patientFeignClient).getPatientById(id);
        verify(noteFeignClient, never()).getNotesByPatient(anyString(), anyInt(), anyInt());
        verify(assessmentFeignClient, never()).assess(any(UUID.class));
    }

    @Test
    void loadPatientData_whenGenericException_setsGenericErrorMessage() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        when(patientFeignClient.getPatientById(id)).thenThrow(new IllegalArgumentException("Erreur de validation"));

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertEquals("Une erreur inattendue est survenue lors du chargement du patient", model.getAttribute("errorMessage"));
        //assertNull(model.getAttribute("patientDto"));
        verify(patientFeignClient).getPatientById(id);
        verify(noteFeignClient, never()).getNotesByPatient(anyString(), anyInt(), anyInt());
        verify(assessmentFeignClient, never()).assess(any(UUID.class));
    }

    @Test
    void loadPatientData_whenNullPointerException_setsGenericErrorMessage() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        when(patientFeignClient.getPatientById(id)).thenThrow(new NullPointerException("Données null inattendues"));

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertEquals("Une erreur inattendue est survenue lors du chargement du patient", model.getAttribute("errorMessage"));
        //assertNull(model.getAttribute("patientDto"));
        verify(patientFeignClient).getPatientById(id);
    }

    @Test
    void loadNotesData_whenFeignException_setsNotesError() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        PatientDto mockPatient = mock(PatientDto.class);
        when(patientFeignClient.getPatientById(id)).thenReturn(mockPatient);
        when(mockPatient.getBirthDate()).thenReturn(LocalDate.of(1990, 1, 1));
        doReturn("35").when(patientService).calculateAge(LocalDate.of(1990, 1, 1));

        when(noteFeignClient.getNotesByPatient(eq(id.toString()), anyInt(), anyInt())).thenThrow(makeFeignNotFound());

        AssessmentDto mockAssessment = mock(AssessmentDto.class);
        when(assessmentFeignClient.assess(id)).thenReturn(mockAssessment);

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertEquals("Notes indisponibles temporairement", model.getAttribute("notesError"));
        assertNotNull(model.getAttribute("patientDto"));
        assertNotNull(model.getAttribute("assessment"));
    }

    @Test
    void loadNotesData_whenGenericException_setsNotesError() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        PatientDto mockPatient = mock(PatientDto.class);
        when(patientFeignClient.getPatientById(id)).thenReturn(mockPatient);
        when(mockPatient.getBirthDate()).thenReturn(LocalDate.of(1990, 1, 1));
        doReturn("35").when(patientService).calculateAge(LocalDate.of(1990, 1, 1));

        when(noteFeignClient.getNotesByPatient(eq(id.toString()), anyInt(), anyInt())).thenThrow(new RuntimeException("Erreur réseau"));

        AssessmentDto mockAssessment = mock(AssessmentDto.class);
        when(assessmentFeignClient.assess(id)).thenReturn(mockAssessment);

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertEquals("Erreur lors du chargement des notes", model.getAttribute("notesError"));
        assertNotNull(model.getAttribute("patientDto"));
        assertNotNull(model.getAttribute("assessment"));
    }

    @Test
    void loadAssessmentData_whenFeignException_setsAssessmentError() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        PatientDto mockPatient = mock(PatientDto.class);
        when(patientFeignClient.getPatientById(id)).thenReturn(mockPatient);
        when(mockPatient.getBirthDate()).thenReturn(LocalDate.of(1990, 1, 1));
        doReturn("35").when(patientService).calculateAge(LocalDate.of(1990, 1, 1));

        when(noteFeignClient.getNotesByPatient(eq(id.toString()), anyInt(), anyInt())).thenReturn(mock(NotesDto.class));
        when(assessmentFeignClient.assess(id)).thenThrow(makeFeignNotFound());

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertEquals("Évaluation indisponible temporairement", model.getAttribute("assessmentError"));
        assertNotNull(model.getAttribute("patientDto"));
        assertNotNull(model.getAttribute("notes"));
    }

    @Test
    void loadAssessmentData_whenGenericException_setsAssessmentError() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        PatientDto mockPatient = mock(PatientDto.class);
        when(patientFeignClient.getPatientById(id)).thenReturn(mockPatient);
        when(mockPatient.getBirthDate()).thenReturn(LocalDate.of(1990, 1, 1));
        doReturn("35").when(patientService).calculateAge(LocalDate.of(1990, 1, 1));

        when(noteFeignClient.getNotesByPatient(eq(id.toString()), anyInt(), anyInt())).thenReturn(mock(NotesDto.class));
        when(assessmentFeignClient.assess(id)).thenThrow(new IllegalStateException("Service indisponible"));

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertEquals("Erreur lors du chargement de l'évaluation", model.getAttribute("assessmentError"));
        assertNotNull(model.getAttribute("patientDto"));
        assertNotNull(model.getAttribute("notes"));
    }

    @Test
    void showPatient_multipleCascadingErrors_handlesAllErrorsGracefully() {
        UUID id = UUID.randomUUID();
        Model model = new ExtendedModelMap();

        PatientDto mockPatient = mock(PatientDto.class);
        when(patientFeignClient.getPatientById(id)).thenReturn(mockPatient);
        when(mockPatient.getBirthDate()).thenReturn(LocalDate.of(1990, 1, 1));
        doReturn("35").when(patientService).calculateAge(LocalDate.of(1990, 1, 1));

        when(noteFeignClient.getNotesByPatient(eq(id.toString()), anyInt(), anyInt())).thenThrow(makeFeignNotFound());
        when(assessmentFeignClient.assess(id)).thenThrow(new RuntimeException("Service indisponible"));

        String view = controller.showPatient(request, id, 1, 5, model);

        assertEquals("patient", view);
        assertNotNull(model.getAttribute("patientDto"));
        assertEquals("Notes indisponibles temporairement", model.getAttribute("notesError"));
        assertEquals("Erreur lors du chargement de l'évaluation", model.getAttribute("assessmentError"));
        assertFalse(model.containsAttribute("errorMessage"));
    }
}
