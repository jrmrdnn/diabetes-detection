package com.medilabo.frontendService.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.medilabo.frontendService.dto.AssessmentDto;
import com.medilabo.frontendService.dto.Gender;
import com.medilabo.frontendService.dto.NotesDto;
import com.medilabo.frontendService.dto.PatientDto;
import com.medilabo.frontendService.dto.PatientsDto;
import com.medilabo.frontendService.feign.AssessmentFeignClient;
import com.medilabo.frontendService.feign.NoteFeignClient;
import com.medilabo.frontendService.feign.PatientFeignClient;
import com.medilabo.frontendService.service.PatientService;
import feign.FeignException;
import feign.Request;
import feign.Response;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

public class PatientControllerTest {

  @Mock
  private AssessmentFeignClient assessmentFeignClient;

  @Mock
  private PatientFeignClient patientFeignClient;

  @Mock
  private NoteFeignClient noteFeignClient;

  @Mock
  private PatientService patientService;

  private PatientController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    controller = new PatientController(
      assessmentFeignClient,
      patientFeignClient,
      noteFeignClient,
      patientService
    );

    Field baseUrlField = ReflectionUtils.findField(
      PatientController.class,
      "baseUrl"
    );
    ReflectionUtils.makeAccessible(baseUrlField);
    ReflectionUtils.setField(baseUrlField, controller, "http://localhost:8080");
  }

  private FeignException makeFeignNotFound() {
    Request req = Request.create(
      Request.HttpMethod.GET,
      "/",
      Collections.emptyMap(),
      new byte[0],
      Charset.defaultCharset(),
      null
    );
    Response res = Response.builder()
      .request(req)
      .status(404)
      .reason("Not Found")
      .build();
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
    when(
      noteFeignClient.getNotesByPatient(eq(id.toString()), anyInt(), anyInt())
    ).thenReturn(mock(NotesDto.class));
    AssessmentDto mockAssessment = mock(AssessmentDto.class);
    when(assessmentFeignClient.assess(id)).thenReturn(mockAssessment);

    String view = controller.showPatient(id, 1, 5, model);

    assertEquals("patient", view);
    assertSame(model.getAttribute("patient"), mockPatient);
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

    String view = controller.showPatient(id, 1, 5, model);

    assertEquals("patient", view);
    assertEquals(
      "Patient introuvable pour l’ID : " + id,
      model.getAttribute("errorMessage")
    );
  }

  @Test
  void showPatient_whenOtherException_setsGenericError() {
    UUID id = UUID.randomUUID();
    Model model = new ExtendedModelMap();
    when(patientFeignClient.getPatientById(id)).thenThrow(
      new RuntimeException("boom")
    );

    String view = controller.showPatient(id, 1, 5, model);

    assertEquals("patient", view);
    assertEquals(
      "Une erreur inattendue est survenue",
      model.getAttribute("errorMessage")
    );
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
    assertSame(patient, model.getAttribute("patient"));
    assertSame(patientDto, model.getAttribute("patientDto"));
  }

  @Test
  void editPatientGet_whenFeignException_setsError() {
    UUID id = UUID.randomUUID();
    Model model = new ExtendedModelMap();

    when(patientFeignClient.getPatientById(id)).thenThrow(makeFeignNotFound());

    String view = controller.editPatient(id, model);

    assertEquals("edit-patient", view);
    assertEquals(
      "Patient introuvable pour l’ID : " + id,
      model.getAttribute("errorMessage")
    );
  }

  @Test
  void addPatientGet_preparesForm() {
    Model model = new ExtendedModelMap();

    String view = controller.addPatient(model);

    assertEquals("add-patient", view);
    assertNotNull(model.getAttribute("patientDto"));
    Object genders = model.getAttribute("genders");
    assertTrue(genders instanceof Gender[]);
    assertTrue(((Gender[]) genders).length > 0);
  }

  @Test
  void addPatientPost_withValidationErrors_returnsForm() {
    RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(true);
    Model model = new ExtendedModelMap();

    String view = controller.addPatient(
      ra,
      mock(PatientDto.class),
      result,
      model
    );

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

    String view = controller.addPatient(
      ra,
      dto,
      result,
      new ExtendedModelMap()
    );

    assertEquals("redirect:http://localhost:8080/patient/the-id", view);
    assertEquals(
      "Patient ajouté avec succès",
      ra.getFlashAttributes().get("successMessage")
    );
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
    assertEquals(
      "Erreur lors de l'ajout du patient",
      model.getAttribute("errorMessage")
    );
  }

  @Test
  void editPatientPut_withValidationErrors_returnsForm() {
    RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(true);
    Model model = new ExtendedModelMap();

    String view = controller.editPatient(
      ra,
      UUID.randomUUID(),
      mock(PatientDto.class),
      result,
      model
    );

    assertEquals("edit-patient", view);
    assertNotNull(model.getAttribute("genders"));
  }

  @Test
  void editPatientPut_success_redirects() {
    RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    UUID id = UUID.randomUUID();
    PatientDto dto = mock(PatientDto.class);

    // no exception on update
    doNothing().when(patientFeignClient).updatePatient(id.toString(), dto);

    String view = controller.editPatient(
      ra,
      id,
      dto,
      result,
      new ExtendedModelMap()
    );

    assertEquals("redirect:http://localhost:8080/patient/" + id, view);
    assertEquals(
      "Patient mis à jour avec succès",
      ra.getFlashAttributes().get("successMessage")
    );
  }

  @Test
  void editPatientPut_feignException_returnsFormWithError() {
    RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
    BindingResult result = mock(BindingResult.class);
    when(result.hasErrors()).thenReturn(false);

    UUID id = UUID.randomUUID();
    PatientDto dto = mock(PatientDto.class);

    doThrow(makeFeignNotFound())
      .when(patientFeignClient)
      .updatePatient(id.toString(), dto);

    ExtendedModelMap model = new ExtendedModelMap();
    String view = controller.editPatient(ra, id, dto, result, model);

    assertEquals("edit-patient", view);
    assertNotNull(model.getAttribute("genders"));
    assertEquals(
      "Erreur lors de la mise à jour du patient",
      model.getAttribute("errorMessage")
    );
  }
}
