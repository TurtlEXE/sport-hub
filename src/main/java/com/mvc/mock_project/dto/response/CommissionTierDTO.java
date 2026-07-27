package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionTierDTO {
    private Integer tierId;
    private BigDecimal minPricePerMinute;
    private BigDecimal maxPricePerMinute;
    private BigDecimal commissionRate;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private Boolean isCurrent;
    private String status;
    private LocalDateTime announcedAt;
    private Integer noticeDays;
    private String description;
}
