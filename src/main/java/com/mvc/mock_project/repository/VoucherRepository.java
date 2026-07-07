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
    
    @Query("SELECT v FROM Voucher v LEFT JOIN v.applicableFacilities f " +
           "WHERE v.isActive = true " +
           "AND v.validFrom <= :now AND v.validTo >= :now " +
           "AND (v.applicableTo = 'ALL' OR f.id = :facilityId)")
    List<Voucher> findActiveVouchersForFacility(@Param("facilityId") Integer facilityId, @Param("now") LocalDateTime now);
}
