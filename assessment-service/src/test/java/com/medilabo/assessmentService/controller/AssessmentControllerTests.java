package com.medilabo.assessmentService.controller;

import com.medilabo.assessmentService.dto.AssessmentDto;
import com.medilabo.assessmentService.service.AssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentControllerTests {

    @Mock
    private AssessmentService assessmentService;

    @InjectMocks
    private AssessmentController assessmentController;

    private UUID patientId;
    private AssessmentDto expectedAssessment;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        expectedAssessment = new AssessmentDto(); // Initialiser avec les données nécessaires
    }

    @Test
    @DisplayName("Devrait retourner une évaluation valide quand le service s'exécute normalement")
    void assess_ShouldReturnValidAssessment_WhenServiceExecutesNormally() {
        // Arrange
        when(assessmentService.assessDiabetesRisk(patientId)).thenReturn(expectedAssessment);

        // Act
        AssessmentDto result = assessmentController.assess(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAssessment, result);
        verify(assessmentService, times(1)).assessDiabetesRisk(patientId);
    }

    @Test
    @DisplayName("Devrait lancer une RuntimeException quand le service échoue")
    void assess_ShouldThrowRuntimeException_WhenServiceFails() {
        // Arrange
        when(assessmentService.assessDiabetesRisk(patientId)).thenThrow(new RuntimeException("Erreur simulée"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            assessmentController.assess(patientId);
        });

        assertTrue(exception.getMessage().contains("Error assessing diabetes risk"));
        verify(assessmentService, times(1)).assessDiabetesRisk(patientId);
    }
}
