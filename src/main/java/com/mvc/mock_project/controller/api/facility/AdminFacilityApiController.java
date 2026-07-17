package com.mvc.mock_project.controller.api.facility;

import com.mvc.mock_project.dto.request.facility.AdminApprovalRequest;
import com.mvc.mock_project.dto.response.facility.AdminFacilityReviewDTO;
import com.mvc.mock_project.entities.enums.ApprovalStatus;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.AdminFacilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.mvc.mock_project.dto.response.facility.stats.AdminFacilityStatsDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/facilities")
@RequiredArgsConstructor
public class AdminFacilityApiController {

    private final AdminFacilityService adminFacilityService;

    @GetMapping("/stats")
    public ResponseEntity<AdminFacilityStatsDTO> getFacilityStats() {
        return ResponseEntity.ok(adminFacilityService.getFacilityStats());
    }

    @GetMapping
    public ResponseEntity<Page<AdminFacilityReviewDTO>> getFacilities(
            @RequestParam(required = false) ApprovalStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(adminFacilityService.getAllFacilities(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminFacilityReviewDTO> getFacilityDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(adminFacilityService.getFacilityDetailForReview(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id) {
        adminFacilityService.approveFacility(userDetails.getAccount().getId(), id);
        return ResponseEntity.ok().body("Facility approved successfully");
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody AdminApprovalRequest request) {
        adminFacilityService.rejectFacility(userDetails.getAccount().getId(), id, request);
        return ResponseEntity.ok().body("Facility rejected");
    }
}
