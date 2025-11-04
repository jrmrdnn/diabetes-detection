package com.medilabo.frontendService.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.medilabo.frontendService.config.FeignConfig;
import com.medilabo.frontendService.dto.PatientDto;
import com.medilabo.frontendService.dto.PatientsDto;

/**
 * Feign client for communicating with the Patient Service.
 */
@FeignClient(name = "patient-service", path = "/api/patients", configuration = FeignConfig.class)
public interface PatientFeignClient {
    @GetMapping
    PatientsDto getAllPatients();

    @GetMapping
    PatientsDto getAllPatients(@RequestParam int page, @RequestParam int size, @RequestParam String sort, @RequestParam String sortBy);

    @GetMapping("/{id}")
    PatientDto getPatientById(@PathVariable UUID id);

    @PostMapping("/add")
    String addPatient(PatientDto addPatientDto);

    @PutMapping("/{id}")
    void updatePatient(@PathVariable UUID id, PatientDto patientDto);
}
