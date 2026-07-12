package com.mvc.mock_project.service;

import java.util.List;
import java.util.Map;

public interface BookingService {
    List<Map<String, Object>> getFacilitySportsByVenue(Integer venueId);
    Map<String, Object> getBookingTimeline(Integer facilitySportId, String date);
    
    List<Map<String, Object>> getFacilityProducts(Integer facilityId);
    
    List<Map<String, Object>> getFacilityVouchers(Integer facilityId);
    
    com.mvc.mock_project.entities.Invoice createBookingTransaction(
        String guestName, String guestPhone, String email, 
        java.math.BigDecimal courtAmount, java.math.BigDecimal productAmount, Integer facilityId,
        String slotsJson, String bookingDate, com.mvc.mock_project.entities.Account account, Integer voucherId
    );
}
