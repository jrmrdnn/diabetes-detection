package com.medilabo.assessmentService.dto;

import com.medilabo.assessmentService.enums.RiskLevel;
import lombok.*;

/**
 * AssessmentDto is a Data Transfer Object that represents the assessment results for a patient.
 * It contains the risk level and the count of trigger terms identified during the assessment.
 */
@Getter
@Setter
public class AssessmentDto {
  private RiskLevel riskLevel;
  private int triggerTermsCount;
}
