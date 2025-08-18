package com.medilabo.assessmentService.feign;

import com.medilabo.assessmentService.config.FeignConfig;
import com.medilabo.assessmentService.dto.PatientDto;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for interacting with the Patient Service.
 * This client provides methods to retrieve patient information by ID.
 */
@FeignClient(
  name = "patient-service",
  path = "/api/patients",
  configuration = FeignConfig.class
)
public interface PatientFeignClient {
  /**
   * Retrieves a patient by their unique identifier.
   *
   * @param patientId the UUID of the patient
   * @return a PatientDto object containing the patient's details
   */
  @GetMapping("/{patientId}")
  PatientDto getPatientById(@PathVariable UUID patientId);
}
