package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Integer> {
    Optional<EmailVerification> findByEmail(String email);
    Optional<EmailVerification> findByTokenAndEmail(String token, String email);
    void deleteByEmail(String email);
}
