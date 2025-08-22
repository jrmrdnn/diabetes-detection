package com.medilabo.frontendService;

import com.medilabo.frontendService.dto.PatientsDto;
import com.medilabo.frontendService.feign.PatientFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FrontendServiceIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientFeignClient patientFeignClient;

    @Test
    void showApp_shouldReturnPatients_whenAuthenticated() throws Exception {
        PatientsDto patientsDto = new PatientsDto();
        patientsDto.setData(Collections.emptyList());
        patientsDto.setTotalElements(0);
        patientsDto.setTotalPages(1);
        when(patientFeignClient.getAllPatients(0, 5, "asc", "lastName")).thenReturn(patientsDto);

        mockMvc.perform(get("/app?page=1&size=5&sort=asc&sortBy=lastName"))
                .andExpect(status().isOk())
                .andExpect(view().name("app"))
                .andExpect(model().attributeExists("patients"));
    }

    @Test
    void showPatient_shouldReturnPatientView_whenPatientExists() throws Exception {
        UUID patientId = UUID.randomUUID();
        when(patientFeignClient.getPatientById(patientId)).thenReturn(new PatientsDto.Patient());

        mockMvc.perform(get("/patient/" + patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("patient"))
                .andExpect(model().attributeExists("patientDto"));
    }
}
