package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING' " +
           "AND b.holdExpiredAt < :now " +
           "AND (b.invoice IS NULL OR b.invoice.paymentStatus = 'UNPAID') " +
           "AND NOT EXISTS (" +
           "    SELECT p FROM Payment p WHERE p.invoice = b.invoice " +
           "    AND (p.paymentStatus = 'PAID' OR p.paymentStatus = 'PARTIAL')" +
           ")")
    List<Booking> findExpiredPendingBookings(@Param("now") LocalDateTime now);
}
