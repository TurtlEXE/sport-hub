package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByGoogleId(String googleId);
    Optional<Account> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByPhoneAndIdNot(String phone, Integer id);
    long countByRole(com.mvc.mock_project.entities.enums.Role role);
}
