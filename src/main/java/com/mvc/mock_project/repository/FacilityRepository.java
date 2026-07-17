package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Integer> {
    List<Facility> findByIsActiveTrue();
    
    // For Customer: only active & approved
    List<Facility> findByIsActiveTrueAndApprovalStatus(ApprovalStatus status);

    // For Owner: all facilities owned by them
    List<Facility> findByOwner_IdOrderByCreatedAtDesc(Integer ownerAccountId);

    // For Owner: find specific facility by id & ownership
    Optional<Facility> findByIdAndOwner_Id(Integer facilityId, Integer ownerAccountId);

    // For Admin: filtered by status with pagination
    Page<Facility> findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Facility f ORDER BY CASE WHEN f.approvalStatus = 'PENDING' THEN 1 WHEN f.approvalStatus = 'APPROVED' THEN 2 ELSE 3 END, f.createdAt DESC")
    Page<Facility> findAllOrderByStatusAndDate(Pageable pageable);

    // For Admin Stats: count by status
    long countByApprovalStatus(ApprovalStatus status);
}
