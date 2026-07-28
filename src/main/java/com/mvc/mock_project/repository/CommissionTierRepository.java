package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.CommissionTier;
import com.mvc.mock_project.entities.enums.TierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CommissionTierRepository extends JpaRepository<CommissionTier, Integer> {
    
    Page<CommissionTier> findByStatus(TierStatus status, Pageable pageable);
    
    Page<CommissionTier> findByIsCurrent(Boolean isCurrent, Pageable pageable);
    
    Page<CommissionTier> findByStatusAndIsCurrent(TierStatus status, Boolean isCurrent, Pageable pageable);

    @Query("SELECT COUNT(c) > 0 FROM CommissionTier c WHERE " +
           "(c.status = 'ACTIVE' OR c.status = 'ANNOUNCED') AND " +
           "(c.effectiveTo IS NULL OR c.effectiveTo > :start) AND " +
           "(cast(:end as timestamp) IS NULL OR c.effectiveFrom < :end) AND " +
           "(c.maxPricePerMinute IS NULL OR c.maxPricePerMinute > :minPrice) AND " +
           "(cast(:maxPrice as bigdecimal) IS NULL OR c.minPricePerMinute < :maxPrice) AND " +
           "c.id != :excludeId")
    boolean existsOverlappingTier(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("excludeId") Integer excludeId);
}
