package com.medilabo.frontendService.feign;

import com.medilabo.frontendService.config.FeignConfig;
import com.medilabo.frontendService.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", path = "/api/auth", url = "http://localhost:8082")
public interface AuthFeignClient {
  @PostMapping
  String auth(@RequestBody UserDto userDto);
}
