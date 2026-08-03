package com.mvc.mock_project.controller.api.admin;

import com.mvc.mock_project.dto.response.AdminDashboardDTO;
import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardApiController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    public ApiResponse<AdminDashboardDTO> getDashboardStats() {
        AdminDashboardDTO stats = adminDashboardService.getDashboardStats();
        return ApiResponse.<AdminDashboardDTO>builder()
                .success(true)
                .data(stats)
                .build();
    }

    @GetMapping("/facilities/{facilityId}/bookings")
    public ApiResponse<java.util.List<com.mvc.mock_project.dto.response.FacilityBookingDetailDTO>> getFacilityBookingsDetail(
            @PathVariable Long facilityId) {
        return ApiResponse.<java.util.List<com.mvc.mock_project.dto.response.FacilityBookingDetailDTO>>builder()
                .success(true)
                .data(adminDashboardService.getFacilityBookingsDetail(facilityId))
                .build();
    }
}
