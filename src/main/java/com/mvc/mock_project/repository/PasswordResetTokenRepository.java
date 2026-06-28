package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByTokenAndEmail(String token, String email);
    void deleteByEmail(String email);
}
