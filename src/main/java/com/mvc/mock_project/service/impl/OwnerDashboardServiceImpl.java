package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.response.OwnerDashboardDTO;
import com.mvc.mock_project.dto.response.OwnerDashboardDTO.MonthlyOwnerRevenueDTO;
import com.mvc.mock_project.dto.response.OwnerDashboardDTO.OwnerFacilityStatDTO;
import com.mvc.mock_project.service.OwnerDashboardService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerDashboardServiceImpl implements OwnerDashboardService {

    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public OwnerDashboardDTO getDashboardStats(Integer ownerId) {
        OwnerDashboardDTO dto = new OwnerDashboardDTO();
        
        // 1. Total Facilities
        String facilityQuery = "SELECT COUNT(f.facility_id) FROM facility f WHERE f.owner_account_id = :ownerId";
        Number facilityCount = (Number) entityManager.createNativeQuery(facilityQuery)
                .setParameter("ownerId", ownerId)
                .getSingleResult();
        dto.setTotalFacilities(facilityCount != null ? facilityCount.longValue() : 0L);

        // 2. Global KPIs (Total Profit, Revenue, Bookings, Customers)
        String kpiQuery = "SELECT " +
                "SUM(i.court_amount) AS totalCourtRevenue, " +
                "SUM(i.product_amount) AS totalServiceRevenue, " +
                "SUM(CASE WHEN b.staff_id IS NULL AND b.owner_id IS NULL THEN COALESCE(pc.commission_amount, 0) - COALESCE(pc.voucher_cost_platform, 0) ELSE 0 END) AS totalPlatformCommission, " +
                "SUM(CASE WHEN b.staff_id IS NULL AND b.owner_id IS NULL THEN 1 ELSE 0 END) AS totalOnlineBookings, " +
                "SUM(CASE WHEN b.staff_id IS NOT NULL OR b.owner_id IS NOT NULL THEN 1 ELSE 0 END) AS totalOfflineBookings, " +
                "COUNT(DISTINCT b.account_id) AS totalCustomers " +
                "FROM booking b " +
                "JOIN facility f ON b.facility_id = f.facility_id " +
                "JOIN invoice i ON b.booking_id = i.booking_id " +
                "LEFT JOIN platform_commission pc ON i.invoice_id = pc.invoice_id " +
                "WHERE f.owner_account_id = :ownerId " +
                "AND b.booking_status IN ('CONFIRMED', 'COMPLETED') " +
                "AND i.payment_status IN ('PAID', 'PARTIAL')";

        Object[] kpiResult = (Object[]) entityManager.createNativeQuery(kpiQuery)
                .setParameter("ownerId", ownerId)
                .getSingleResult();

        BigDecimal totalCourtRev = getBigDecimal(kpiResult[0]);
        BigDecimal totalServiceRev = getBigDecimal(kpiResult[1]);
        BigDecimal totalComm = getBigDecimal(kpiResult[2]);
        BigDecimal totalNetProfit = totalCourtRev.add(totalServiceRev).subtract(totalComm);

        dto.setTotalCourtRevenue(totalCourtRev);
        dto.setTotalServiceRevenue(totalServiceRev);
        dto.setTotalNetProfit(totalNetProfit);
        dto.setTotalOnlineBookings(kpiResult[3] != null ? ((Number) kpiResult[3]).longValue() : 0L);
        dto.setTotalOfflineBookings(kpiResult[4] != null ? ((Number) kpiResult[4]).longValue() : 0L);
        dto.setTotalCustomers(kpiResult[5] != null ? ((Number) kpiResult[5]).longValue() : 0L);

        // 3. Monthly Trend (Last 12 months)
        String monthlyQuery = "SELECT " +
                "DATE_FORMAT(b.created_at, '%Y-%m') AS month, " +
                "SUM(i.court_amount) AS courtRevenue, " +
                "SUM(i.product_amount) AS serviceRevenue, " +
                "SUM(CASE WHEN b.staff_id IS NULL AND b.owner_id IS NULL THEN COALESCE(pc.commission_amount, 0) - COALESCE(pc.voucher_cost_platform, 0) ELSE 0 END) AS commission " +
                "FROM booking b " +
                "JOIN facility f ON b.facility_id = f.facility_id " +
                "JOIN invoice i ON b.booking_id = i.booking_id " +
                "LEFT JOIN platform_commission pc ON i.invoice_id = pc.invoice_id " +
                "WHERE f.owner_account_id = :ownerId " +
                "AND b.booking_status IN ('CONFIRMED', 'COMPLETED') " +
                "AND i.payment_status IN ('PAID', 'PARTIAL') " +
                "AND b.created_at >= DATE_SUB(CURDATE(), INTERVAL 11 MONTH) " +
                "GROUP BY DATE_FORMAT(b.created_at, '%Y-%m') " +
                "ORDER BY month ASC";

        List<Object[]> monthlyResults = entityManager.createNativeQuery(monthlyQuery)
                .setParameter("ownerId", ownerId)
                .getResultList();

        List<MonthlyOwnerRevenueDTO> monthlyList = new ArrayList<>();
        for (Object[] row : monthlyResults) {
            String month = (String) row[0];
            BigDecimal mCourtRev = getBigDecimal(row[1]);
            BigDecimal mServiceRev = getBigDecimal(row[2]);
            BigDecimal mComm = getBigDecimal(row[3]);
            BigDecimal mCourtNet = mCourtRev.subtract(mComm); // We subtract commission from Court Rev (assuming platform commission is on the total, but we assign it to court to simplify)
            
            monthlyList.add(MonthlyOwnerRevenueDTO.builder()
                    .month(month)
                    .courtNetProfit(mCourtNet)
                    .serviceRevenue(mServiceRev)
                    .build());
        }
        dto.setMonthlyRevenueData(monthlyList);

        // 4. Facility Statistics
        String facilityStatQuery = "SELECT " +
                "f.facility_id, f.name, " +
                "COUNT(b.booking_id) AS totalBookings, " +
                "SUM(i.court_amount) AS courtRevenue, " +
                "SUM(i.product_amount) AS serviceRevenue, " +
                "SUM(CASE WHEN b.staff_id IS NULL AND b.owner_id IS NULL THEN COALESCE(pc.commission_amount, 0) - COALESCE(pc.voucher_cost_platform, 0) ELSE 0 END) AS commission " +
                "FROM facility f " +
                "LEFT JOIN booking b ON f.facility_id = b.facility_id AND b.booking_status IN ('CONFIRMED', 'COMPLETED') " +
                "LEFT JOIN invoice i ON b.booking_id = i.booking_id AND i.payment_status IN ('PAID', 'PARTIAL') " +
                "LEFT JOIN platform_commission pc ON i.invoice_id = pc.invoice_id " +
                "WHERE f.owner_account_id = :ownerId " +
                "GROUP BY f.facility_id, f.name " +
                "ORDER BY totalBookings DESC";

        List<Object[]> facResults = entityManager.createNativeQuery(facilityStatQuery)
                .setParameter("ownerId", ownerId)
                .getResultList();

        List<OwnerFacilityStatDTO> facList = new ArrayList<>();
        for (Object[] row : facResults) {
            BigDecimal fCourtRev = getBigDecimal(row[3]);
            BigDecimal fServiceRev = getBigDecimal(row[4]);
            BigDecimal fComm = getBigDecimal(row[5]);
            BigDecimal fNetProfit = fCourtRev.add(fServiceRev).subtract(fComm);

            facList.add(OwnerFacilityStatDTO.builder()
                    .facilityId(((Number) row[0]).longValue())
                    .facilityName((String) row[1])
                    .totalBookings(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                    .courtRevenue(fCourtRev)
                    .serviceRevenue(fServiceRev)
                    .platformCommission(fComm)
                    .netProfit(fNetProfit)
                    .build());
        }
        dto.setFacilityStats(facList);

        return dto;
    }

    private BigDecimal getBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return new BigDecimal(((Number) val).doubleValue());
        return BigDecimal.ZERO;
    }
}
