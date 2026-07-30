package com.mvc.mock_project.mapper;

import com.mvc.mock_project.dto.request.CommissionPolicyRequest;
import com.mvc.mock_project.dto.request.CommissionTierRequest;
import com.mvc.mock_project.dto.response.CommissionPolicyDTO;
import com.mvc.mock_project.dto.response.CommissionTierDTO;
import com.mvc.mock_project.entities.CommissionPolicy;
import com.mvc.mock_project.entities.CommissionTier;
import com.mvc.mock_project.entities.enums.TierStatus;
import org.springframework.stereotype.Component;

@Component
public class CommissionMapper {

    // Tier Mappers
    public CommissionTier toEntity(CommissionTierRequest request) {
        if (request == null) return null;
        
        return CommissionTier.builder()
                .minPricePerMinute(request.getMinPricePerMinute())
                .maxPricePerMinute(request.getMaxPricePerMinute())
                .commissionRate(request.getCommissionRate())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .noticeDays(request.getNoticeDays())
                .description(request.getDescription())
                .status(TierStatus.DRAFT)
                .isCurrent(false)
                .build();
    }

    public void updateEntityFromRequest(CommissionTier tier, CommissionTierRequest request) {
        if (request == null || tier == null) return;
        
        tier.setMinPricePerMinute(request.getMinPricePerMinute());
        tier.setMaxPricePerMinute(request.getMaxPricePerMinute());
        tier.setCommissionRate(request.getCommissionRate());
        tier.setEffectiveFrom(request.getEffectiveFrom());
        tier.setEffectiveTo(request.getEffectiveTo());
        tier.setNoticeDays(request.getNoticeDays());
        tier.setDescription(request.getDescription());
    }

    public CommissionTierDTO toTierDto(CommissionTier tier) {
        if (tier == null) return null;
        
        return CommissionTierDTO.builder()
                .tierId(tier.getId())
                .minPricePerMinute(tier.getMinPricePerMinute())
                .maxPricePerMinute(tier.getMaxPricePerMinute())
                .commissionRate(tier.getCommissionRate())
                .effectiveFrom(tier.getEffectiveFrom())
                .effectiveTo(tier.getEffectiveTo())
                .isCurrent(tier.getIsCurrent())
                .status(tier.getStatus() != null ? tier.getStatus().name() : null)
                .announcedAt(tier.getAnnouncedAt())
                .noticeDays(tier.getNoticeDays())
                .description(tier.getDescription())
                .build();
    }

    // Policy Mappers
    public void updateEntityFromRequest(CommissionPolicy policy, CommissionPolicyRequest request) {
        if (request == null || policy == null) return;
        
        policy.setMinNoticeDays(request.getMinNoticeDays());
        policy.setDescription(request.getDescription());
    }

    public CommissionPolicyDTO toPolicyDto(CommissionPolicy policy) {
        if (policy == null) return null;
        
        return CommissionPolicyDTO.builder()
                .policyId(policy.getId())
                .minNoticeDays(policy.getMinNoticeDays())
                .description(policy.getDescription())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }
}
