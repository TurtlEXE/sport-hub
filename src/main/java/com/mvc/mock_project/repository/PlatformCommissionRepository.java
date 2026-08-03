package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.PlatformCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformCommissionRepository extends JpaRepository<PlatformCommission, Integer> {
}
