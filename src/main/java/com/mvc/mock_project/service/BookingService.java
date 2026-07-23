package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.OnSiteBookingRequestDTO;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.Invoice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BookingService {
    List<Map<String, Object>> getFacilitySportsByVenue(Integer venueId);
    Map<String, Object> getBookingTimeline(Integer facilitySportId, String date);
    
    List<Map<String, Object>> getFacilityProducts(Integer facilityId);
    
    List<Map<String, Object>> getFacilityVouchers(Integer facilityId);
    
    Invoice createBookingTransaction(
        String guestName, String guestPhone, String email, 
        BigDecimal courtAmount, BigDecimal productAmount, Integer facilityId,
        String slotsJson, String bookingDate, Account account, Integer voucherId, Integer voucherPlatformId
    );
    
    void cancelExpiredBookings();
    
    void processPaymentSuccess(Integer invoiceId, String totalPrice, String transactionId, String paymentTime, String email);
    
    Invoice createOnSiteBooking(OnSiteBookingRequestDTO request, Account creatorAccount);
}
