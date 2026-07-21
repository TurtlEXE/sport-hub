package com.mvc.mock_project.controller.api.staff;

import com.mvc.mock_project.dto.request.StaffFormDTO;
import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.StaffResponseDTO;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.OwnerStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/staff")
@RequiredArgsConstructor
public class OwnerStaffApiController {

    private final OwnerStaffService ownerStaffService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffResponseDTO>>> getAllStaff(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<StaffResponseDTO> staffList = ownerStaffService.getStaffByOwner(userDetails.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Staff list retrieved", staffList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponseDTO>> getStaffById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id) {
        StaffResponseDTO staff = ownerStaffService.getStaffById(id, userDetails.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Staff details retrieved", staff));
    }

    @GetMapping("/by-facility/{facilityId}")
    public ResponseEntity<ApiResponse<List<StaffResponseDTO>>> getStaffByFacility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer facilityId) {
        List<StaffResponseDTO> staffList = ownerStaffService.getStaffByFacility(facilityId, userDetails.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Facility staff list retrieved", staffList));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createStaff(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StaffFormDTO form) {
        ownerStaffService.createStaff(form, userDetails.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Staff created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateStaff(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody StaffFormDTO form) {
        ownerStaffService.updateStaff(id, form, userDetails.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Staff updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id) {
        ownerStaffService.deleteStaff(id, userDetails.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.success("Staff deleted successfully"));
    }
}
