package com.mvc.mock_project.controller.api.admin;

import com.mvc.mock_project.dto.request.CommissionPolicyRequest;
import com.mvc.mock_project.dto.request.CommissionTierRequest;
import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.CommissionPolicyDTO;
import com.mvc.mock_project.dto.response.CommissionTierDTO;
import com.mvc.mock_project.entities.enums.TierStatus;
import com.mvc.mock_project.service.CommissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/commission")
@RequiredArgsConstructor
public class AdminCommissionApiController {

    private final CommissionService commissionService;

    // Policy Endpoints
    @GetMapping("/policy")
    public ResponseEntity<ApiResponse<CommissionPolicyDTO>> getPolicy() {
        return ResponseEntity.ok(ApiResponse.success("Success", commissionService.getPolicy()));
    }

    @PutMapping("/policy")
    public ResponseEntity<ApiResponse<CommissionPolicyDTO>> updatePolicy(@Valid @RequestBody CommissionPolicyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated policy successfully", commissionService.updatePolicy(request)));
    }

    // Tiers Endpoints
    @GetMapping("/tiers")
    public ResponseEntity<ApiResponse<Page<CommissionTierDTO>>> getAllTiers(
            @RequestParam(required = false) TierStatus status,
            @RequestParam(required = false) Boolean isCurrent,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Success", commissionService.getAllTiers(status, isCurrent, pageable)));
    }

    @GetMapping("/tiers/{id}")
    public ResponseEntity<ApiResponse<CommissionTierDTO>> getTierById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Success", commissionService.getTierById(id)));
    }

    @PostMapping("/tiers")
    public ResponseEntity<ApiResponse<CommissionTierDTO>> createTier(@Valid @RequestBody CommissionTierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Created tier successfully", commissionService.createTier(request)));
    }

    @PutMapping("/tiers/{id}")
    public ResponseEntity<ApiResponse<CommissionTierDTO>> updateTier(
            @PathVariable Integer id,
            @Valid @RequestBody CommissionTierRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated tier successfully", commissionService.updateTier(id, request)));
    }

    @PatchMapping("/tiers/{id}/announce")
    public ResponseEntity<ApiResponse<CommissionTierDTO>> announceTier(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success("Announced tier successfully", commissionService.announceTier(id)));
    }

    @DeleteMapping("/tiers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTier(@PathVariable Integer id) {
        commissionService.deleteTier(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted tier successfully"));
    }
}
