package com.medilabo.authService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

  @NotBlank(message = "Le nom d'utilisateur est requis.")
  @Size(
    min = 2,
    max = 50,
    message = "Le nom d'utilisateur doit avoir entre 2 et 50 caractères."
  )
  private String username;

  @NotBlank(message = "Le mot de passe est requis.")
  @Size(
    min = 8,
    max = 50,
    message = "Le mot de passe doit être entre 8 et 50 caractères."
  )
  private String password;
}
