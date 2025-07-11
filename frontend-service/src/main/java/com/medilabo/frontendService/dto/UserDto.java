package com.medilabo.frontendService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for User authentication.
 * Contains fields for username and password with validation constraints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
    private String password;
}
