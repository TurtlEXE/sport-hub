package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.response.AdminDashboardDTO;
import com.mvc.mock_project.service.AdminDashboardService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mvc.mock_project.dto.response.FacilityBookingDetailDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboardStats() {
        AdminDashboardDTO dto = new AdminDashboardDTO();

        // 1. KPI Cards
        String kpiQuery = "SELECT " +
                "COALESCE(SUM(i.total_amount), 0) AS totalOnlineRevenue, " +
                "COALESCE(SUM(pc.commission_amount - pc.voucher_cost_platform), 0) AS platformCommissionNet, " +
                "COUNT(b.booking_id) AS totalOnlineBookings " +
                "FROM booking b " +
                "JOIN invoice i ON b.booking_id = i.booking_id " +
                "LEFT JOIN platform_commission pc ON i.invoice_id = pc.invoice_id " +
                "WHERE b.staff_id IS NULL AND b.owner_id IS NULL " +
                "AND b.booking_status IN ('CONFIRMED', 'COMPLETED') " +
                "AND i.payment_status IN ('PAID', 'PARTIAL')";

        Object[] kpiResult = (Object[]) entityManager.createNativeQuery(kpiQuery).getSingleResult();
        dto.setTotalOnlineRevenue(kpiResult[0] != null ? new BigDecimal(kpiResult[0].toString()) : BigDecimal.ZERO);
        dto.setPlatformCommissionNet(kpiResult[1] != null ? new BigDecimal(kpiResult[1].toString()) : BigDecimal.ZERO);
        dto.setTotalOnlineBookings(kpiResult[2] != null ? ((Number) kpiResult[2]).longValue() : 0L);

        // Sidebar Summary Cards
        String summaryQuery = "SELECT " +
                "(SELECT COUNT(*) FROM facility WHERE approval_status = 'APPROVED') AS totalFacilities, " +
                "(SELECT COUNT(*) FROM facility WHERE approval_status = 'PENDING') AS pendingFacilities, " +
                "(SELECT COUNT(*) FROM account) AS totalUsers, " +
                "(SELECT COUNT(*) FROM voucher WHERE issuer_type = 'PLATFORM' AND is_active = true) AS activeVouchers";
        
        Object[] summaryResult = (Object[]) entityManager.createNativeQuery(summaryQuery).getSingleResult();
        dto.setTotalFacilities(summaryResult[0] != null ? ((Number) summaryResult[0]).longValue() : 0L);
        dto.setPendingFacilities(summaryResult[1] != null ? ((Number) summaryResult[1]).longValue() : 0L);
        dto.setTotalUsers(summaryResult[2] != null ? ((Number) summaryResult[2]).longValue() : 0L);
        dto.setActiveVouchers(summaryResult[3] != null ? ((Number) summaryResult[3]).longValue() : 0L);

        // 2. Monthly Revenue Data (Last 12 months)
        String monthlyQuery = "SELECT " +
                "DATE_FORMAT(b.created_at, '%Y-%m') AS month, " +
                "SUM(i.total_amount) AS revenue, " +
                "SUM(COALESCE(pc.commission_amount, 0) - COALESCE(pc.voucher_cost_platform, 0)) AS commissionNet " +
                "FROM booking b " +
                "JOIN invoice i ON b.booking_id = i.booking_id " +
                "LEFT JOIN platform_commission pc ON i.invoice_id = pc.invoice_id " +
                "WHERE b.staff_id IS NULL AND b.owner_id IS NULL " +
                "AND b.booking_status IN ('CONFIRMED', 'COMPLETED') " +
                "AND i.payment_status IN ('PAID', 'PARTIAL') " +
                "AND b.created_at >= DATE_SUB(CURDATE(), INTERVAL 11 MONTH) " +
                "GROUP BY DATE_FORMAT(b.created_at, '%Y-%m') " +
                "ORDER BY month ASC";

        List<Object[]> monthlyResults = entityManager.createNativeQuery(monthlyQuery).getResultList();
        List<AdminDashboardDTO.MonthlyRevenueDTO> monthlyRevenueData = new ArrayList<>();
        for (Object[] row : monthlyResults) {
            monthlyRevenueData.add(AdminDashboardDTO.MonthlyRevenueDTO.builder()
                    .month(row[0].toString())
                    .revenue(row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO)
                    .commissionNet(row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO)
                    .build());
        }
        dto.setMonthlyRevenueData(monthlyRevenueData);

        // 3. Facility Stats
        String facilityQuery = "SELECT " +
                "f.facility_id, " +
                "f.name AS facilityName, " +
                "a.full_name AS ownerName, " +
                "COUNT(b.booking_id) AS onlineBookings, " +
                "COALESCE(SUM(i.total_amount), 0) AS grossRevenue, " +
                "COALESCE(SUM(pc.commission_amount), 0) AS commissionEarned, " +
                "COALESCE(SUM(pc.voucher_cost_platform), 0) AS voucherCostPlatform, " +
                "COALESCE(SUM(pc.commission_amount - pc.voucher_cost_platform), 0) AS netProfit " +
                "FROM facility f " +
                "JOIN account a ON f.owner_account_id = a.account_id " +
                "LEFT JOIN booking b ON f.facility_id = b.facility_id " +
                "    AND b.staff_id IS NULL AND b.owner_id IS NULL " +
                "    AND b.booking_status IN ('CONFIRMED', 'COMPLETED') " +
                "LEFT JOIN invoice i ON b.booking_id = i.booking_id AND i.payment_status IN ('PAID', 'PARTIAL') " +
                "LEFT JOIN platform_commission pc ON i.invoice_id = pc.invoice_id " +
                "WHERE f.approval_status = 'APPROVED' " +
                "GROUP BY f.facility_id, f.name, a.full_name " +
                "ORDER BY netProfit DESC";

        List<Object[]> facilityResults = entityManager.createNativeQuery(facilityQuery).getResultList();
        List<AdminDashboardDTO.FacilityStatDTO> facilityStats = new ArrayList<>();
        for (Object[] row : facilityResults) {
            facilityStats.add(AdminDashboardDTO.FacilityStatDTO.builder()
                    .facilityId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .facilityName(row[1] != null ? row[1].toString() : "")
                    .ownerName(row[2] != null ? row[2].toString() : "")
                    .onlineBookings(row[3] != null ? ((Number) row[3]).longValue() : 0L)
                    .grossRevenue(row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO)
                    .commissionEarned(row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO)
                    .voucherCostPlatform(row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO)
                    .netProfit(row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO)
                    .build());
        }
        dto.setFacilityStats(facilityStats);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacilityBookingDetailDTO> getFacilityBookingsDetail(Long facilityId) {
        String detailQuery = "SELECT " +
                "b.booking_id, " +
                "a.full_name AS customerName, " +
                "pc.court_revenue, " +
                "i.discount_amount, " +
                "i.total_amount AS customerPaid, " +
                "pc.commission_rate, " +
                "pc.commission_amount, " +
                "pc.voucher_cost_platform, " +
                "(pc.commission_amount - pc.voucher_cost_platform) AS netProfit, " +
                "pc.owner_payout " +
                "FROM booking b " +
                "JOIN invoice i ON b.booking_id = i.booking_id " +
                "JOIN platform_commission pc ON i.invoice_id = pc.invoice_id " +
                "LEFT JOIN account a ON b.account_id = a.account_id " +
                "WHERE b.facility_id = :facilityId " +
                "AND b.staff_id IS NULL AND b.owner_id IS NULL " +
                "AND b.booking_status IN ('CONFIRMED', 'COMPLETED') " +
                "AND i.payment_status IN ('PAID', 'PARTIAL') " +
                "ORDER BY b.created_at DESC";

        List<Object[]> results = entityManager.createNativeQuery(detailQuery)
                .setParameter("facilityId", facilityId)
                .getResultList();

        List<FacilityBookingDetailDTO> details = new ArrayList<>();
        for (Object[] row : results) {
            details.add(FacilityBookingDetailDTO.builder()
                    .bookingId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .customerName(row[1] != null ? row[1].toString() : "Guest")
                    .courtRevenue(row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO)
                    .discountAmount(row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO)
                    .customerPaid(row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO)
                    .commissionRate(row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO)
                    .commissionEarned(row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO)
                    .voucherCostPlatform(row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO)
                    .netProfit(row[8] != null ? new BigDecimal(row[8].toString()) : BigDecimal.ZERO)
                    .ownerPayout(row[9] != null ? new BigDecimal(row[9].toString()) : BigDecimal.ZERO)
                    .build());
        }
        return details;
    }
}
