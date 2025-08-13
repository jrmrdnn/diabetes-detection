package com.medilabo.frontendService.feign;

import com.medilabo.frontendService.config.FeignConfig;
import com.medilabo.frontendService.dto.PatientDto;
import com.medilabo.frontendService.dto.PatientsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "patient-service",
        path = "/api/patients",
        url = "${PATIENT_SERVICE_URL:http://localhost:8083}",
        configuration = FeignConfig.class
)
public interface PatientFeignClient {
    @GetMapping
    PatientsDto getAllPatients();

    @GetMapping
    PatientsDto getAllPatients(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sort,
            @RequestParam String sortBy
    );

    @GetMapping("/{id}")
    PatientDto getPatientById(@PathVariable UUID id);

    @PostMapping("/add")
    String addPatient(PatientDto addPatientDto);

    @PutMapping("/{id}")
    void updatePatient(@PathVariable UUID id, PatientDto patientDto);
}
