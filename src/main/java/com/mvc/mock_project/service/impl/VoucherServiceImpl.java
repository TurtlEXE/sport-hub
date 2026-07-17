package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.VoucherFormDTO;
import com.mvc.mock_project.entities.Voucher;
import com.mvc.mock_project.entities.enums.IssuerType;
import com.mvc.mock_project.repository.VoucherRepository;
import com.mvc.mock_project.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

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
}
