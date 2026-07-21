package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.VoucherFormDTO;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.Voucher;
import com.mvc.mock_project.entities.enums.IssuerType;
import com.mvc.mock_project.entities.enums.VoucherApplicableTo;
import com.mvc.mock_project.repository.AccountRepository;
import com.mvc.mock_project.repository.FacilityRepository;
import com.mvc.mock_project.repository.VoucherRepository;
import com.mvc.mock_project.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final AccountRepository accountRepository;
    private final FacilityRepository facilityRepository;

    @Override
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher findById(Integer id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
    }

    @Override
    @Transactional
    public void save(VoucherFormDTO form) {
        Voucher voucher = Voucher.builder()
                .code(form.getCode())
                .name(form.getName())
                .description(form.getDescription())
                .discountType(form.getDiscountType())
                .discountValue(form.getDiscountValue())
                .validFrom(form.getValidFrom())
                .validTo(form.getValidTo())
                .minOrderAmount(form.getMinOrderAmount() != null ? form.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(form.getMaxDiscountAmount())
                .usageLimit(form.getUsageLimit())
                .perUserLimit(form.getPerUserLimit() != null ? form.getPerUserLimit() : 1)
                .isActive(form.getIsActive() != null ? form.getIsActive() : true)
                .issuerType(IssuerType.PLATFORM)
                .applicableTo(VoucherApplicableTo.ALL)
                .build();
        
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void update(Integer id, VoucherFormDTO form) {
        Voucher voucher = findById(id);
        
        voucher.setCode(form.getCode());
        voucher.setName(form.getName());
        voucher.setDescription(form.getDescription());
        voucher.setDiscountType(form.getDiscountType());
        voucher.setDiscountValue(form.getDiscountValue());
        voucher.setValidFrom(form.getValidFrom());
        voucher.setValidTo(form.getValidTo());
        voucher.setMinOrderAmount(form.getMinOrderAmount() != null ? form.getMinOrderAmount() : BigDecimal.ZERO);
        voucher.setMaxDiscountAmount(form.getMaxDiscountAmount());
        voucher.setUsageLimit(form.getUsageLimit());
        voucher.setPerUserLimit(form.getPerUserLimit() != null ? form.getPerUserLimit() : 1);
        voucher.setIsActive(form.getIsActive() != null ? form.getIsActive() : false);
        
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        voucherRepository.deleteById(id);
    }

    @Override
    public List<Voucher> findByIssuerAccountId(Integer accountId) {
        return voucherRepository.findByIssuerAccount_IdOrderByIdDesc(accountId);
    }

    @Override
    public Voucher findByIdAndIssuerAccountId(Integer id, Integer accountId) {
        return voucherRepository.findByIdAndIssuerAccount_Id(id, accountId)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại hoặc bạn không có quyền thao tác"));
    }

    @Override
    @Transactional
    public void createOwnerVoucher(Integer ownerAccountId, VoucherFormDTO form) {
        Account owner = accountRepository.findById(ownerAccountId)
                .orElseThrow(() -> new RuntimeException("Tài khoản chủ sân không tồn tại"));

        Voucher voucher = Voucher.builder()
                .code(form.getCode() != null ? form.getCode().trim().toUpperCase() : null)
                .name(form.getName())
                .description(form.getDescription())
                .discountType(form.getDiscountType())
                .discountValue(form.getDiscountValue())
                .validFrom(form.getValidFrom())
                .validTo(form.getValidTo())
                .minOrderAmount(form.getMinOrderAmount() != null ? form.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(form.getMaxDiscountAmount())
                .usageLimit(form.getUsageLimit())
                .perUserLimit(form.getPerUserLimit() != null ? form.getPerUserLimit() : 1)
                .isActive(form.getIsActive() != null ? form.getIsActive() : true)
                .issuerType(IssuerType.OWNER)
                .issuerAccount(owner)
                .applicableTo(VoucherApplicableTo.COURT_BOOKING)
                .build();

        if (form.getFacilityIds() != null && !form.getFacilityIds().isEmpty()) {
            List<Facility> facilities = facilityRepository.findAllById(form.getFacilityIds()).stream()
                    .filter(f -> f.getOwner().getId().equals(ownerAccountId))
                    .collect(Collectors.toList());
            voucher.setApplicableFacilities(facilities);
        } else {
            voucher.setApplicableFacilities(new ArrayList<>());
        }

        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void updateOwnerVoucher(Integer ownerAccountId, Integer voucherId, VoucherFormDTO form) {
        Voucher voucher = findByIdAndIssuerAccountId(voucherId, ownerAccountId);

        voucher.setCode(form.getCode() != null ? form.getCode().trim().toUpperCase() : null);
        voucher.setName(form.getName());
        voucher.setDescription(form.getDescription());
        voucher.setDiscountType(form.getDiscountType());
        voucher.setDiscountValue(form.getDiscountValue());
        voucher.setValidFrom(form.getValidFrom());
        voucher.setValidTo(form.getValidTo());
        voucher.setMinOrderAmount(form.getMinOrderAmount() != null ? form.getMinOrderAmount() : BigDecimal.ZERO);
        voucher.setMaxDiscountAmount(form.getMaxDiscountAmount());
        voucher.setUsageLimit(form.getUsageLimit());
        voucher.setPerUserLimit(form.getPerUserLimit() != null ? form.getPerUserLimit() : 1);
        voucher.setIsActive(form.getIsActive() != null ? form.getIsActive() : false);

        if (form.getFacilityIds() != null && !form.getFacilityIds().isEmpty()) {
            List<Facility> facilities = facilityRepository.findAllById(form.getFacilityIds()).stream()
                    .filter(f -> f.getOwner().getId().equals(ownerAccountId))
                    .collect(Collectors.toList());
            voucher.setApplicableFacilities(facilities);
        } else {
            if (voucher.getApplicableFacilities() != null) {
                voucher.getApplicableFacilities().clear();
            }
        }

        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void deleteOwnerVoucher(Integer ownerAccountId, Integer voucherId) {
        Voucher voucher = findByIdAndIssuerAccountId(voucherId, ownerAccountId);
        voucherRepository.delete(voucher);
    }
}
