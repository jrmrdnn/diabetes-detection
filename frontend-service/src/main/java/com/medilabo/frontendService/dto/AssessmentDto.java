package com.medilabo.frontendService.dto;

import lombok.Getter;
import lombok.Setter;

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
