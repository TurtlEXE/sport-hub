package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {

    List<Staff> findByOwner_IdAndIsActiveTrue(Integer ownerId);

    List<Staff> findByFacility_IdAndIsActiveTrue(Integer facilityId);

    Optional<Staff> findByIdAndIsActiveTrue(Integer staffId);

    Optional<Staff> findByAccount_Id(Integer accountId);

    boolean existsByAccount_IdAndIsActiveTrue(Integer accountId);
}
