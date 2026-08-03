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
public class OwnerDashboardDTO {
    private BigDecimal totalNetProfit;
    private BigDecimal totalCourtRevenue;
    private BigDecimal totalServiceRevenue;
    private Long totalOnlineBookings;
    private Long totalOfflineBookings;
    private Long totalCustomers;
    private Long totalFacilities;

    private List<MonthlyOwnerRevenueDTO> monthlyRevenueData;
    private List<OwnerFacilityStatDTO> facilityStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyOwnerRevenueDTO {
        private String month; // Format YYYY-MM
        private BigDecimal courtNetProfit; // Net profit from courts
        private BigDecimal serviceRevenue; // Service revenue
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerFacilityStatDTO {
        private Long facilityId;
        private String facilityName;
        private Long totalBookings;
        private BigDecimal courtRevenue;
        private BigDecimal serviceRevenue;
        private BigDecimal platformCommission;
        private BigDecimal netProfit;
    }
}
