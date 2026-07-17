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
import com.mvc.mock_project.repository.CourtRepository;
import com.mvc.mock_project.dto.response.facility.stats.AdminFacilityStatsDTO;
import java.util.stream.Collectors;
import java.util.List;
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
    private final CourtRepository courtRepository;
    private final FacilityMapper facilityMapper;

    @Transactional(readOnly = true)
    public Page<AdminFacilityReviewDTO> getPendingFacilities(Pageable pageable) {
        return facilityRepository.findByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING, pageable)
                .map(facilityMapper::toAdminFacilityReviewDTO);
    }

    @Transactional(readOnly = true)
    public Page<AdminFacilityReviewDTO> getAllFacilities(ApprovalStatus status, Pageable pageable) {
        if (status != null) {
            return facilityRepository.findByApprovalStatusOrderByCreatedAtDesc(status, pageable)
                    .map(facilityMapper::toAdminFacilityReviewDTO);
        }
        return facilityRepository.findAllOrderByStatusAndDate(pageable)
                .map(facilityMapper::toAdminFacilityReviewDTO);
    }

    @Transactional(readOnly = true)
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

    public AdminFacilityStatsDTO getFacilityStats() {
        long pending = facilityRepository.countByApprovalStatus(ApprovalStatus.PENDING);
        long approved = facilityRepository.countByApprovalStatus(ApprovalStatus.APPROVED);
        long rejected = facilityRepository.countByApprovalStatus(ApprovalStatus.REJECTED);
        long total = pending + approved + rejected;

        long totalActiveCourts = courtRepository.countTotalActiveCourts();
        
        List<Object[]> sportsData = courtRepository.countCourtsBySport();
        List<AdminFacilityStatsDTO.SportDistribution> sportDistribution = sportsData.stream()
                .map(data -> {
                    String sportName = (String) data[0];
                    long courtCount = ((Number) data[1]).longValue();
                    double percentage = totalActiveCourts > 0 ? (double) courtCount / totalActiveCourts * 100 : 0;
                    return AdminFacilityStatsDTO.SportDistribution.builder()
                            .sportName(sportName)
                            .courtCount(courtCount)
                            .percentage(Math.round(percentage * 10.0) / 10.0)
                            .build();
                }).collect(Collectors.toList());

        return AdminFacilityStatsDTO.builder()
                .totalRequests(total)
                .pendingReview(pending)
                .approved(approved)
                .rejected(rejected)
                .totalActiveCourts(totalActiveCourts)
                // Assuming active facilities are the approved ones for now, or we can count all active
                .totalActiveFacilities(approved) 
                .sportDistribution(sportDistribution)
                .build();
    }
}
