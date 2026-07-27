package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.CommissionPolicyRequest;
import com.mvc.mock_project.dto.request.CommissionTierRequest;
import com.mvc.mock_project.dto.response.CommissionPolicyDTO;
import com.mvc.mock_project.dto.response.CommissionTierDTO;
import com.mvc.mock_project.entities.enums.TierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommissionService {
    // Commission Tier
    Page<CommissionTierDTO> getAllTiers(TierStatus status, Boolean isCurrent, Pageable pageable);
    CommissionTierDTO getTierById(Integer id);
    CommissionTierDTO createTier(CommissionTierRequest request);
    CommissionTierDTO updateTier(Integer id, CommissionTierRequest request);
    CommissionTierDTO announceTier(Integer id);
    void deleteTier(Integer id);

    // Commission Policy
    CommissionPolicyDTO getPolicy();
    CommissionPolicyDTO updatePolicy(CommissionPolicyRequest request);
}
