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

    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.facility f " +
           "LEFT JOIN FETCH b.invoice i " +
           "LEFT JOIN FETCH b.review r " +
           "WHERE (b.account.id = :accountId) OR (b.guest IS NOT NULL AND LOWER(b.guest.email) = LOWER(:email)) " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findMyBookings(@Param("accountId") Integer accountId, @Param("email") String email);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING' AND b.holdExpiredAt < :now")
    List<Booking> findExpiredPendingBookings(@Param("now") LocalDateTime now);
}
