package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityBookingDetailDTO {
    private Long bookingId;
    private String customerName;
    private BigDecimal courtRevenue;
    private BigDecimal discountAmount;
    private BigDecimal customerPaid;
    private BigDecimal commissionRate;
    private BigDecimal commissionEarned;
    private BigDecimal voucherCostPlatform;
    private BigDecimal netProfit;
    private BigDecimal ownerPayout;
}
