package com.medilabo.frontendService.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.medilabo.frontendService.dto.NoteDto;
import com.medilabo.frontendService.feign.NoteFeignClient;
import feign.FeignException;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ExtendWith(MockitoExtension.class)
public class NoteControllerTest {

  @Mock
  private NoteFeignClient noteFeignClient;

  @Mock
  private RedirectAttributes redirectAttributes;

  @Mock
  private BindingResult bindingResult;

  @Mock
  private Model model;

  private NoteController controller;

  @BeforeEach
  void setUp() throws Exception {
    controller = new NoteController(noteFeignClient);
    Field f = NoteController.class.getDeclaredField("baseUrl");
    f.setAccessible(true);
    f.set(controller, "/base");
  }

  @Test
  void addNote_success() {
    NoteDto noteDto = mock(NoteDto.class);
    when(bindingResult.hasErrors()).thenReturn(false);
    when(noteDto.getPatient()).thenReturn("p1");

    String view = controller.addNote(
      redirectAttributes,
      noteDto,
      bindingResult,
      model
    );

    assertEquals("redirect:/base/patient/p1", view);
    verify(noteFeignClient).addNote(noteDto);
    verify(redirectAttributes).addFlashAttribute(
      "successMessage",
      "Note ajoutée avec succès"
    );
    verifyNoInteractions(model);
  }

  @Test
  void addNote_validationError() {
    NoteDto noteDto = mock(NoteDto.class);
    when(bindingResult.hasErrors()).thenReturn(true);

    String view = controller.addNote(
      redirectAttributes,
      noteDto,
      bindingResult,
      model
    );

    assertEquals("patient", view);
    verifyNoInteractions(noteFeignClient, redirectAttributes);
  }

  @Test
  void addNote_feignException() {
    NoteDto noteDto = mock(NoteDto.class);
    when(bindingResult.hasErrors()).thenReturn(false);
    doThrow(mock(FeignException.class)).when(noteFeignClient).addNote(noteDto);

    String view = controller.addNote(
      redirectAttributes,
      noteDto,
      bindingResult,
      model
    );

    assertEquals("patient", view);
    verify(model).addAttribute(
      "errorMessage",
      "Erreur lors de l'ajout de la note"
    );
  }

  @Test
  void updateNote_success() {
    NoteDto noteDto = mock(NoteDto.class);
    when(bindingResult.hasErrors()).thenReturn(false);
    when(noteDto.getPatient()).thenReturn("p2");

    String view = controller.updateNote(
      redirectAttributes,
      "note1",
      noteDto,
      bindingResult,
      model
    );

    assertEquals("redirect:/base/patient/p2", view);
    verify(noteFeignClient).updateNote("note1", noteDto);
    verify(redirectAttributes).addFlashAttribute(
      "successMessage",
      "Note mise à jour avec succès"
    );
  }

  @Test
  void updateNote_validationError() {
    NoteDto noteDto = mock(NoteDto.class);
    when(bindingResult.hasErrors()).thenReturn(true);

    String view = controller.updateNote(
      redirectAttributes,
      "note1",
      noteDto,
      bindingResult,
      model
    );

    assertEquals("patient", view);
    verify(model).addAttribute(
      "errorMessage",
      "Erreur de validation des données"
    );
    verifyNoInteractions(noteFeignClient, redirectAttributes);
  }

  @Test
  void updateNote_feignException() {
    NoteDto noteDto = mock(NoteDto.class);
    when(bindingResult.hasErrors()).thenReturn(false);
    doThrow(mock(FeignException.class))
      .when(noteFeignClient)
      .updateNote("note1", noteDto);

    String view = controller.updateNote(
      redirectAttributes,
      "note1",
      noteDto,
      bindingResult,
      model
    );

    assertEquals("patient", view);
    verify(model).addAttribute(
      "errorMessage",
      "Erreur lors de la mise à jour de la note"
    );
  }

  @Test
  void deleteNote_success() {
    String view = controller.deleteNote(
      redirectAttributes,
      "patientX",
      "noteX",
      model
    );

    assertEquals("redirect:/base/patient/patientX", view);
    verify(noteFeignClient).deleteNote("noteX");
    verify(redirectAttributes).addFlashAttribute(
      "successMessage",
      "Note supprimée avec succès"
    );
  }

  @Test
  void deleteNote_feignException() {
    doThrow(mock(FeignException.class))
      .when(noteFeignClient)
      .deleteNote("noteY");

    String view = controller.deleteNote(
      redirectAttributes,
      "patientY",
      "noteY",
      model
    );

    assertEquals("redirect:/base/patient/patientY", view);
    verify(model).addAttribute(
      "errorMessage",
      "Erreur lors de la suppression de la note"
    );
  }
}
