package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
    @org.springframework.data.jpa.repository.Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING' AND b.holdExpiredAt < :now")
    java.util.List<Booking> findExpiredPendingBookings(@org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);
}
