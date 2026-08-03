package com.mvc.mock_project.controller.api.owner;

import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.OwnerDashboardDTO;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.OwnerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/dashboard")
@RequiredArgsConstructor
public class OwnerDashboardApiController {

    private final OwnerDashboardService ownerDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<OwnerDashboardDTO>> getDashboardStats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return ResponseEntity.status(401).body(ApiResponse.<OwnerDashboardDTO>builder()
                    .success(false)
                    .message("Unauthorized")
                    .build());
        }

        OwnerDashboardDTO stats = ownerDashboardService.getDashboardStats(userDetails.getAccount().getId());
        return ResponseEntity.ok(ApiResponse.<OwnerDashboardDTO>builder()
                .success(true)
                .message("Successfully fetched owner dashboard stats")
                .data(stats)
                .build());
    }
}
