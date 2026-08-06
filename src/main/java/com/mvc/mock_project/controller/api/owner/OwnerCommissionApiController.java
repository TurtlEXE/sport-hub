package com.mvc.mock_project.controller.api.owner;

import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.CommissionTierDTO;
import com.mvc.mock_project.entities.enums.TierStatus;
import com.mvc.mock_project.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/owner/commission")
@RequiredArgsConstructor
public class OwnerCommissionApiController {

    private final CommissionService commissionService;

    @GetMapping("/tiers")
    public ResponseEntity<ApiResponse<List<CommissionTierDTO>>> getCommissionTiers() {
        // Owner only needs to see ACTIVE and ANNOUNCED tiers
        Page<CommissionTierDTO> activeTiers = commissionService.getAllTiers(TierStatus.ACTIVE, null, Pageable.unpaged());
        Page<CommissionTierDTO> announcedTiers = commissionService.getAllTiers(TierStatus.ANNOUNCED, null, Pageable.unpaged());

        List<CommissionTierDTO> result = new ArrayList<>();
        result.addAll(activeTiers.getContent());
        result.addAll(announcedTiers.getContent());

        return ResponseEntity.ok(ApiResponse.success("Success", result));
    }
}
