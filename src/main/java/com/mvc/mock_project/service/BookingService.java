package com.mvc.mock_project.service;

import java.util.List;
import java.util.Map;

public interface BookingService {
    List<Map<String, Object>> getFacilitySportsByVenue(Integer venueId);
    Map<String, Object> getBookingTimeline(Integer facilitySportId, String date);
    
    List<Map<String, Object>> getFacilityProducts(Integer facilityId);
    
    List<Map<String, Object>> getFacilityVouchers(Integer facilityId);
}
