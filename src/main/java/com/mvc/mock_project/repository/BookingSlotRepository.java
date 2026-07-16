package com.mvc.mock_project.repository;

import com.mvc.mock_project.entities.BookingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingSlotRepository extends JpaRepository<BookingSlot, Integer> {
    
    @Query("SELECT bs FROM BookingSlot bs WHERE bs.court.id IN :courtIds AND bs.bookingDate = :date AND bs.slotStatus != 'CANCELLED'")
    List<BookingSlot> findActiveSlotsByCourtIdsAndDate(@Param("courtIds") List<Integer> courtIds, @Param("date") LocalDate date);
}
