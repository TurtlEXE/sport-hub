package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.facility.AdminApprovalRequest;
import com.mvc.mock_project.dto.response.facility.AdminFacilityReviewDTO;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.enums.ApprovalStatus;
import com.mvc.mock_project.exception.FacilityNotFoundException;
import com.mvc.mock_project.mapper.FacilityMapper;
import com.mvc.mock_project.repository.AccountRepository;
import com.mvc.mock_project.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminFacilityService {

    private final FacilityRepository facilityRepository;
    private final AccountRepository accountRepository;
    private final FacilityMapper facilityMapper;

    public Page<AdminFacilityReviewDTO> getPendingFacilities(Pageable pageable) {
        return facilityRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable)
                .map(facilityMapper::toAdminFacilityReviewDTO);
    }

    public Page<AdminFacilityReviewDTO> getAllFacilities(ApprovalStatus status, Pageable pageable) {
        if (status != null) {
            return facilityRepository.findByApprovalStatus(status, pageable)
                    .map(facilityMapper::toAdminFacilityReviewDTO);
        }
        return facilityRepository.findAll(pageable)
                .map(facilityMapper::toAdminFacilityReviewDTO);
    }

    public AdminFacilityReviewDTO getFacilityDetailForReview(Integer facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));
        return facilityMapper.toAdminFacilityReviewDTO(facility);
    }

    @Transactional
    public void approveFacility(Integer adminAccountId, Integer facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));
        
        Account admin = accountRepository.findById(adminAccountId)
                .orElseThrow(() -> new RuntimeException("Admin account not found"));

        facility.setApprovalStatus(ApprovalStatus.APPROVED);
        facility.setApprovedBy(admin);
        facility.setApprovedAt(LocalDateTime.now());
        facility.setRejectionReason(null);
        facilityRepository.save(facility);
        
        // TODO: Send Notification and Email to owner
    }

    @Transactional
    public void rejectFacility(Integer adminAccountId, Integer facilityId, AdminApprovalRequest request) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new FacilityNotFoundException("Facility not found"));
        
        Account admin = accountRepository.findById(adminAccountId)
                .orElseThrow(() -> new RuntimeException("Admin account not found"));

        if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        facility.setApprovalStatus(ApprovalStatus.REJECTED);
        facility.setApprovedBy(admin);
        facility.setApprovedAt(LocalDateTime.now());
        facility.setRejectionReason(request.getRejectionReason());
        facilityRepository.save(facility);
        
        // TODO: Send Notification and Email to owner
    }
}
