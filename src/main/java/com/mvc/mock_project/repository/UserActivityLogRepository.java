package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    @Modifying
    @Query("DELETE FROM UserActivityLog u WHERE u.timestamp < :thresholdDate")
    void deleteLogsOlderThan(@Param("thresholdDate") LocalDateTime thresholdDate);

    @Query("SELECT u FROM UserActivityLog u WHERE " +
           "(:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.url) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:startDate IS NULL OR u.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR u.timestamp <= :endDate) AND " +
           "(:role IS NULL OR u.account.role = :role)")
    org.springframework.data.domain.Page<UserActivityLog> findFilteredActivities(
            @Param("keyword") String keyword, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            @Param("role") com.mvc.mock_project.entities.enums.Role role,
            org.springframework.data.domain.Pageable pageable);
}
