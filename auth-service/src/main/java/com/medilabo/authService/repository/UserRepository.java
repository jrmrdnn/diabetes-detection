package com.medilabo.authService.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.medilabo.authService.model.User;

/**
 * Repository interface for managing User entities.
 * Provides methods to perform CRUD operations and custom queries on User data.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
  /**
   * Finds a User by their username.
   *
   * @param username the username of the user to find
   * @return an Optional containing the User if found, or empty if not found
   */
  Optional<User> findByUsername(String username);
}
