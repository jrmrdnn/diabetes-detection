package com.medilabo.patientService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class PatientServiceApplicationTests {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> PatientServiceApplication.main(new String[]{}));
    }
}
