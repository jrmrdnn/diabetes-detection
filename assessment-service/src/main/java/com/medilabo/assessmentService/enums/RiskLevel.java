package com.medilabo.assessmentService.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RiskLevel is an enumeration that represents the risk levels for a patient assessment.
 * It includes four levels: NONE, BORDERLINE, IN_DANGER, and EARLY_ONSET.
 * Each level has a description associated with it.
 */
@Getter
@RequiredArgsConstructor
public enum RiskLevel {
    NONE("None"),
    BORDERLINE("Borderline"),
    IN_DANGER("In Danger"),
    EARLY_ONSET("Early onset");

    private final String description;
}
