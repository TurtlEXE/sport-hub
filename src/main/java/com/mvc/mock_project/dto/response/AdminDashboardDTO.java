package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {

    private BigDecimal totalOnlineRevenue;
    private BigDecimal platformCommissionNet;
    private Long totalOnlineBookings;
    private Long totalFacilities;

    private Long pendingFacilities;
    private Long totalUsers;
    private Long activeVouchers;
    
    private Boolean hasCommissionGaps;

    private List<MonthlyRevenueDTO> monthlyRevenueData;
    private List<FacilityStatDTO> facilityStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenueDTO {
        private String month; // e.g. "2023-01"
        private BigDecimal revenue;
        private BigDecimal commissionNet;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacilityStatDTO {
        private Long facilityId;
        private String facilityName;
        private String ownerName;
        private Long onlineBookings;
        private BigDecimal grossRevenue;
        private BigDecimal commissionEarned;
        private BigDecimal voucherCostPlatform;
        private BigDecimal netProfit;
    }
}
