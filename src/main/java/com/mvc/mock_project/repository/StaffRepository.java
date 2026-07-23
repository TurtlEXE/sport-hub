package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findByAccountId(Integer accountId);
    Optional<Staff> findByAccountIdAndIsActiveTrue(Integer accountId);
}
