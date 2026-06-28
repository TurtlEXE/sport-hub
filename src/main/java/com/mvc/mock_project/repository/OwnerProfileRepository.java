package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.OwnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OwnerProfileRepository extends JpaRepository<OwnerProfile, Integer> {
    Optional<OwnerProfile> findByAccountId(Integer accountId);
}
