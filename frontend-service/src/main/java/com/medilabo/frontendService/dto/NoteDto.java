package com.medilabo.frontendService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for Note.
 */
@Getter
@Setter
public class NoteDto {

  @NotBlank(message = "L'ID du patient est obligatoire")
  @NotNull(message = "L'ID du patient ne peut pas être nul")
  @Pattern(
    regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
    message = "L'ID du patient doit être un UUID valide"
  )
  private String patient;

  @NotBlank(message = "La note est obligatoire")
  @NotNull(message = "La note ne peut pas être nulle")
  @Size(
    min = 10,
    max = 1_000,
    message = "La note doit contenir entre 10 et 1 000 caractères"
  )
  private String note;
}
