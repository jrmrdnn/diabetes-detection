package com.medilabo.patientService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabo.patientService.dto.PatientDto;
import com.medilabo.patientService.model.Gender;
import com.medilabo.patientService.model.Patient;
import com.medilabo.patientService.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientServiceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addPatient_shouldCreatePatient_whenValidData() throws Exception {
        PatientDto patientDto = new PatientDto();
        patientDto.setFirstName("John");
        patientDto.setLastName("Doe");
        patientDto.setBirthDate(LocalDate.of(1980, 1, 1));
        patientDto.setGender(Gender.M);
        patientDto.setPostalAddress("123 Main St");
        patientDto.setPhoneNumber("01 23 45 67 89");

        MvcResult result = mockMvc.perform(post("/api/patients/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientDto)))
                .andExpect(status().isOk())
                .andReturn();

        String patientId = result.getResponse().getContentAsString();
        assertNotNull(patientId);

        UUID uuid = UUID.fromString(patientId);
        Patient patient = patientRepository.findById(uuid).orElseThrow();
        assertEquals("John", patient.getFirstName());
        assertEquals("Doe", patient.getLastName());
    }

    @Test
    void getAllPatients_shouldReturnPaginatedPatients_whenPatientsExist() throws Exception {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setBirthDate(LocalDate.of(1980, 1, 1));
        patient.setGender(Gender.M);
        patientRepository.save(patient);

        mockMvc.perform(get("/api/patients")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "asc")
                        .param("sortBy", "lastName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].firstName").value("John"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getPatientById_shouldReturnPatient_whenPatientExists() throws Exception {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setBirthDate(LocalDate.of(1980, 1, 1));
        patient.setGender(Gender.M);
        patient = patientRepository.save(patient);

        mockMvc.perform(get("/api/patients/" + patient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getPatientById_shouldReturnNotFound_whenPatientDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/patients/" + "invalid-id"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void searchPatients_shouldReturnMatchingPatients_whenQueryMatches() throws Exception {
        Patient patient1 = new Patient();
        patient1.setFirstName("John");
        patient1.setLastName("Doe");
        patient1.setBirthDate(LocalDate.of(1980, 1, 1));
        patient1.setGender(Gender.M);
        patientRepository.save(patient1);

        Patient patient2 = new Patient();
        patient2.setFirstName("Jane");
        patient2.setLastName("Smith");
        patient2.setBirthDate(LocalDate.of(1990, 1, 1));
        patient2.setGender(Gender.F);
        patientRepository.save(patient2);

        mockMvc.perform(get("/api/patients/search")
                        .param("q", "John")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].firstName").value("John"));
    }

    @Test
    void updatePatient_shouldModifyPatient_whenPatientExists() throws Exception {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setBirthDate(LocalDate.of(1980, 1, 1));
        patient.setGender(Gender.M);
        patient = patientRepository.save(patient);

        PatientDto updatedPatientDto = new PatientDto();
        updatedPatientDto.setFirstName("John");
        updatedPatientDto.setLastName("Doe");
        updatedPatientDto.setBirthDate(LocalDate.of(1980, 1, 1));
        updatedPatientDto.setGender(Gender.M);
        updatedPatientDto.setPostalAddress("456 Updated St");
        updatedPatientDto.setPhoneNumber("09 87 65 43 21");

        mockMvc.perform(put("/api/patients/" + patient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPatientDto)))
                .andExpect(status().isOk());

        Patient updatedPatient = patientRepository.findById(patient.getId()).orElseThrow();
        assertEquals("456 Updated St", updatedPatient.getPostalAddress());
        assertEquals("09 87 65 43 21", updatedPatient.getPhoneNumber());
    }

    @Test
    void deletePatient_shouldRemovePatient_whenPatientExists() throws Exception {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setBirthDate(LocalDate.of(1980, 1, 1));
        patient.setGender(Gender.M);
        patient = patientRepository.save(patient);

        mockMvc.perform(delete("/api/patients/" + patient.getId()))
                .andExpect(status().isOk());

        assertFalse(patientRepository.findById(patient.getId()).isPresent());
    }

    @Test
    void getAllPatients_shouldReturnEmptyList_whenNoPatientsExist() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "asc")
                        .param("sortBy", "lastName"))
                .andExpect(status().isOk());
    }
}
