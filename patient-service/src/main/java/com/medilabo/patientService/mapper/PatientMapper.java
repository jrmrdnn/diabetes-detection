package com.medilabo.patientService.mapper;

import com.medilabo.patientService.dto.PatientDto;
import com.medilabo.patientService.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PatientMapper {
    PatientDto toDto(Patient patient);

    Patient toPatient(PatientDto patientDto);
}
