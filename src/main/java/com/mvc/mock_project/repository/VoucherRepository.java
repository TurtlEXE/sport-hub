package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    
    @Query("SELECT DISTINCT v FROM Voucher v " +
           "LEFT JOIN v.applicableFacilities f " +
           "LEFT JOIN v.applicableAccounts a " +
           "WHERE v.isActive = true " +
           "AND v.validFrom <= :now AND v.validTo >= :now " +
           "AND (v.issuerType = 'PLATFORM' OR f.id = :facilityId) " +
           "AND (a IS NULL OR a.id = :accountId)")
    List<Voucher> findValidVouchers(@Param("facilityId") Integer facilityId, @Param("accountId") Integer accountId, @Param("now") LocalDateTime now);

}
