package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.CommissionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionPolicyRepository extends JpaRepository<CommissionPolicy, Integer> {
    
    @Query("SELECT p FROM CommissionPolicy p")
    List<CommissionPolicy> findAllPolicies();
    
    default Optional<CommissionPolicy> getSingletonPolicy() {
        return findById(1);
    }
}
