package com.medilabo.frontendService.feign;

import com.medilabo.frontendService.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthFeignClient {
    @PostMapping
    String auth(@RequestBody UserDto userDto);
}
