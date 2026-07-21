package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.CreateReviewRequest;
import com.mvc.mock_project.dto.response.booking.MyBookingDTO;

import java.util.List;

public interface CustomerBookingService {
    List<MyBookingDTO> getMyBookings(Integer accountId, String email);
    MyBookingDTO getBookingDetail(Integer bookingId, Integer accountId, String email);
    void cancelBooking(Integer bookingId, Integer accountId, String email, String reason);
    void submitReview(Integer accountId, String email, CreateReviewRequest request);
}
