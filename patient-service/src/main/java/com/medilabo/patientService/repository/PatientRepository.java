package com.medilabo.patientService.repository;

import com.medilabo.patientService.model.Patient;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    @Override
    @NonNull
    Optional<Patient> findById(@NonNull UUID id);

    @Query(
            "SELECT p FROM Patient p " +
                    "WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
                    "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
                    "OR LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :query, '%')) " +
                    "OR LOWER(CONCAT(p.lastName, ' ', p.firstName)) LIKE LOWER(CONCAT('%', :query, '%'))"
    )
    Page<Patient> searchByName(@Param("query") String query, Pageable pageable);
}
