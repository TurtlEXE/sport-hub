package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.CommissionPolicyRequest;
import com.mvc.mock_project.dto.request.CommissionTierRequest;
import com.mvc.mock_project.dto.response.CommissionPolicyDTO;
import com.mvc.mock_project.dto.response.CommissionTierDTO;
import com.mvc.mock_project.entities.CommissionPolicy;
import com.mvc.mock_project.entities.CommissionTier;
import com.mvc.mock_project.entities.enums.TierStatus;
import com.mvc.mock_project.exception.CommissionTierOverlapException;
import com.mvc.mock_project.exception.InvalidTierStatusTransitionException;
import com.mvc.mock_project.mapper.CommissionMapper;
import com.mvc.mock_project.repository.CommissionPolicyRepository;
import com.mvc.mock_project.repository.CommissionTierRepository;
import com.mvc.mock_project.service.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final CommissionTierRepository commissionTierRepository;
    private final CommissionPolicyRepository commissionPolicyRepository;
    private final CommissionMapper commissionMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CommissionTierDTO> getAllTiers(TierStatus status, Boolean isCurrent, Pageable pageable) {
        if (status != null && isCurrent != null) {
            return commissionTierRepository.findByStatusAndIsCurrent(status, isCurrent, pageable).map(commissionMapper::toTierDto);
        } else if (status != null) {
            return commissionTierRepository.findByStatus(status, pageable).map(commissionMapper::toTierDto);
        } else if (isCurrent != null) {
            return commissionTierRepository.findByIsCurrent(isCurrent, pageable).map(commissionMapper::toTierDto);
        }
        return commissionTierRepository.findAll(pageable).map(commissionMapper::toTierDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionTierDTO getTierById(Integer id) {
        CommissionTier tier = commissionTierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission Tier not found"));
        return commissionMapper.toTierDto(tier);
    }

    @Override
    @Transactional
    public CommissionTierDTO createTier(CommissionTierRequest request) {
        if (request.getEffectiveTo() != null && request.getEffectiveFrom().isAfter(request.getEffectiveTo())) {
            throw new RuntimeException("Effective from date must be before effective to date");
        }

        CommissionTier tier = commissionMapper.toEntity(request);
        CommissionTier saved = commissionTierRepository.save(tier);
        log.info("Created new commission tier with id: {}", saved.getId());
        return commissionMapper.toTierDto(saved);
    }

    @Override
    @Transactional
    public CommissionTierDTO updateTier(Integer id, CommissionTierRequest request) {
        CommissionTier tier = commissionTierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission Tier not found"));

        if (tier.getStatus() != TierStatus.DRAFT) {
            throw new InvalidTierStatusTransitionException("Can only update tiers in DRAFT status");
        }

        if (request.getEffectiveTo() != null && request.getEffectiveFrom().isAfter(request.getEffectiveTo())) {
            throw new RuntimeException("Effective from date must be before effective to date");
        }

        commissionMapper.updateEntityFromRequest(tier, request);
        CommissionTier saved = commissionTierRepository.save(tier);
        log.info("Updated commission tier with id: {}", saved.getId());
        return commissionMapper.toTierDto(saved);
    }

    @Override
    @Transactional
    public CommissionTierDTO announceTier(Integer id) {
        CommissionTier tier = commissionTierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission Tier not found"));

        if (tier.getStatus() != TierStatus.DRAFT) {
            throw new InvalidTierStatusTransitionException("Only DRAFT tiers can be announced");
        }

        // Check for overlaps with ACTIVE or ANNOUNCED tiers
        boolean overlaps = commissionTierRepository.existsOverlappingTier(
                tier.getEffectiveFrom(),
                tier.getEffectiveTo(),
                tier.getMinPricePerMinute(),
                tier.getMaxPricePerMinute(),
                tier.getId()
        );

        if (overlaps) {
            throw new CommissionTierOverlapException("Tier overlaps with an existing announced or active tier");
        }

        CommissionPolicy policy = commissionPolicyRepository.getSingletonPolicy()
                .orElseGet(() -> {
                    CommissionPolicy p = new CommissionPolicy();
                    p.setMinNoticeDays(14);
                    return p;
                });

        long actualNoticeDays = ChronoUnit.DAYS.between(LocalDateTime.now(), tier.getEffectiveFrom());
        if (actualNoticeDays < policy.getMinNoticeDays()) {
            throw new RuntimeException("Notice period (" + actualNoticeDays + " days) is less than the required minimum policy (" + policy.getMinNoticeDays() + " days)");
        }

        tier.setStatus(TierStatus.ANNOUNCED);
        tier.setAnnouncedAt(LocalDateTime.now());
        tier.setNoticeDays((int) actualNoticeDays);
        
        CommissionTier saved = commissionTierRepository.save(tier);
        log.info("Announced commission tier with id: {}", saved.getId());
        return commissionMapper.toTierDto(saved);
    }

    @Override
    @Transactional
    public void deleteTier(Integer id) {
        CommissionTier tier = commissionTierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commission Tier not found"));

        if (tier.getStatus() == TierStatus.ACTIVE) {
            throw new RuntimeException("Cannot delete an ACTIVE commission tier");
        }

        // Hard delete for DRAFT/ANNOUNCED that are not active yet
        commissionTierRepository.delete(tier);
        log.info("Deleted commission tier with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionPolicyDTO getPolicy() {
        CommissionPolicy policy = commissionPolicyRepository.getSingletonPolicy()
                .orElseThrow(() -> new RuntimeException("Commission Policy not configured"));
        return commissionMapper.toPolicyDto(policy);
    }

    @Override
    @Transactional
    public CommissionPolicyDTO updatePolicy(CommissionPolicyRequest request) {
        CommissionPolicy policy = commissionPolicyRepository.getSingletonPolicy()
                .orElseGet(() -> {
                    CommissionPolicy newPolicy = new CommissionPolicy();
                    newPolicy.setId(1);
                    return newPolicy;
                });

        commissionMapper.updateEntityFromRequest(policy, request);
        CommissionPolicy saved = commissionPolicyRepository.save(policy);
        log.info("Updated commission policy");
        return commissionMapper.toPolicyDto(saved);
    }
}
