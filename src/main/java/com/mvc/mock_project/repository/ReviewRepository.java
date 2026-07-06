package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT r FROM Review r WHERE r.booking.facility.id = :facilityId ORDER BY r.createdAt DESC")
    List<Review> findByFacilityIdOrderByCreatedAtDesc(@Param("facilityId") Integer facilityId);
}
