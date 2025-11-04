package com.medilabo.frontendService.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.medilabo.frontendService.dto.UserDto;

/**
 * Feign client for communicating with the Auth Service.
 */
@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthFeignClient {
    @PostMapping
    String auth(@RequestBody UserDto userDto);
}
