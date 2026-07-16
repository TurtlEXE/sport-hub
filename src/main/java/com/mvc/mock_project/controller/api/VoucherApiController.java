package com.mvc.mock_project.controller.api;

import com.mvc.mock_project.dto.VoucherDto;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.Voucher;
import com.mvc.mock_project.repository.AccountRepository;
import com.mvc.mock_project.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherApiController {

    private final VoucherRepository voucherRepository;
    private final AccountRepository accountRepository;

    @GetMapping("/valid")
    public ResponseEntity<List<VoucherDto>> getValidVouchers(@RequestParam("facilityId") Integer facilityId) {
        
        // 1. Determine logged-in user accountId
        Integer accountId = -1; // -1 means guest, so it won't match any real account ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.mvc.mock_project.security.CustomUserDetails) {
                Account acc = ((com.mvc.mock_project.security.CustomUserDetails) principal).getAccount();
                if (acc != null) accountId = acc.getId();
            } else if (principal instanceof com.mvc.mock_project.security.CustomOAuth2User) {
                Account acc = ((com.mvc.mock_project.security.CustomOAuth2User) principal).getAccount();
                if (acc != null) accountId = acc.getId();
            } else if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                Account acc = accountRepository.findByEmail(username).orElse(null);
                if (acc != null) accountId = acc.getId();
            }
        }

        // 2. Fetch vouchers
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findValidVouchers(facilityId, accountId, now);

        // 3. Map to DTO
        List<VoucherDto> dtos = vouchers.stream().map(v -> VoucherDto.builder()
                .voucherId(v.getId())
                .code(v.getCode())
                .name(v.getName())
                .description(v.getDescription())
                .issuerType(v.getIssuerType() != null ? v.getIssuerType().name() : null)
                .discountType(v.getDiscountType() != null ? v.getDiscountType().name() : null)
                .discountValue(v.getDiscountValue())
                .minOrderAmount(v.getMinOrderAmount())
                .maxDiscountAmount(v.getMaxDiscountAmount())
                .usageLimit(v.getUsageLimit())
                .perUserLimit(v.getPerUserLimit())
                .applicableTo(v.getApplicableTo() != null ? v.getApplicableTo().name() : "ALL")
                .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
