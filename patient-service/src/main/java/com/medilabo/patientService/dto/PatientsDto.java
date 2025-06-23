package com.medilabo.patientService.dto;

import com.medilabo.patientService.model.Patient;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientsDto {

  private List<Patient> data;
  private long totalElements;
  private int totalPages;
  private int currentPage;
  private int pageSize;
}
