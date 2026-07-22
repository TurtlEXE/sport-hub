package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.CourtSlotBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourtSlotBookingRepository extends JpaRepository<CourtSlotBooking, Integer> {

    @Query("SELECT csb FROM CourtSlotBooking csb WHERE csb.court.id IN :courtIds AND csb.bookingDate = :date")
    List<CourtSlotBooking> findActiveHoldsByCourtIdsAndDate(@Param("courtIds") List<Integer> courtIds, @Param("date") LocalDate date);
    
    @Query("SELECT csb FROM CourtSlotBooking csb WHERE csb.bookingSlot.booking.id = :bookingId")
    List<CourtSlotBooking> findByBookingId(@Param("bookingId") Integer bookingId);
}
