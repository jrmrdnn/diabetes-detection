package com.medilabo.frontendService.feign;

import com.medilabo.frontendService.config.FeignConfig;
import com.medilabo.frontendService.dto.AssessmentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "assessment-service", path = "/api/assessment", url = "${ASSESSMENT_SERVICE_URL:http://localhost:8085}", configuration = FeignConfig.class)
public interface AssessmentFeignClient {
    @GetMapping("/{patientId}")
    AssessmentDto assess(@PathVariable UUID patientId);
}
