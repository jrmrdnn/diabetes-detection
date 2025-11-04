package com.medilabo.frontendService.feign;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.medilabo.frontendService.config.FeignConfig;
import com.medilabo.frontendService.dto.AssessmentDto;

/**
 * Feign client for communicating with the Assessment Service.
 */
@FeignClient(name = "assessment-service", path = "/api/assessment", configuration = FeignConfig.class)
public interface AssessmentFeignClient {
    @GetMapping("/{patientId}")
    AssessmentDto assess(@PathVariable UUID patientId);
}
