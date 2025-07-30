package com.medilabo.assessmentService.controller;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medilabo.assessmentService.dto.AssessmentDto;
import com.medilabo.assessmentService.service.AssessmentService;

@ExtendWith(MockitoExtension.class)
class AssessmentControllerTest {

  @Mock
  private AssessmentService assessmentService;

  @InjectMocks
  private AssessmentController assessmentController;

  private UUID patientId;
  private AssessmentDto expectedAssessment;

  @BeforeEach
  void setUp() {
    patientId = UUID.randomUUID();
    expectedAssessment = new AssessmentDto();
  }

  @Test
  void assess_ShouldReturnValidAssessment_WhenServiceExecutesNormally() {
    when(assessmentService.assessDiabetesRisk(patientId)).thenReturn(
      expectedAssessment
    );

    AssessmentDto result = assessmentController.assess(patientId);

    assertNotNull(result);
    assertEquals(expectedAssessment, result);
    verify(assessmentService, times(1)).assessDiabetesRisk(patientId);
  }

  @Test
  void assess_ShouldThrowRuntimeException_WhenServiceFails() {
    when(assessmentService.assessDiabetesRisk(patientId)).thenThrow(
      new RuntimeException("Error assessing diabetes risk")
    );

    Exception exception = assertThrows(RuntimeException.class, () -> {
      assessmentController.assess(patientId);
    });

    assertTrue(
      exception.getMessage().contains("Error assessing diabetes risk")
    );
    verify(assessmentService, times(1)).assessDiabetesRisk(patientId);
  }
}
