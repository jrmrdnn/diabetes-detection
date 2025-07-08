package com.medilabo.authService.model;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a user in the authentication system.
 * Contains fields for user ID, username, and password.
 * The ID is generated as a UUID and the username must be unique.
 */
@Getter
@Setter
@Entity
public class User {

  @Id
  @UuidGenerator
  private UUID id;

  @Column(unique = true, nullable = false, length = 50)
  private String username;

  @Column(nullable = false)
  private String password;
}
