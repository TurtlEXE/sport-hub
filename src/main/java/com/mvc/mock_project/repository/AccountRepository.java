package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByGoogleId(String googleId);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
