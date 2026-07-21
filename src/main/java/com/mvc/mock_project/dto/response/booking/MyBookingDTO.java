package com.mvc.mock_project.dto.response.booking;

import com.mvc.mock_project.dto.response.ReviewDTO;
import com.mvc.mock_project.entities.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyBookingDTO {
    private Integer id;
    private String transactionCode;
    
    private Integer facilityId;
    private String facilityName;
    private String facilityAddress;
    private String facilityImage;
    private String facilityPhone;

    private BookingStatus bookingStatus;
    private String bookingStatusText;
    private String statusBadgeClass;

    private LocalDate primaryBookingDate;
    private String timeRangeSummary;
    private List<String> courtNames;

    private BigDecimal courtAmount;
    private BigDecimal productAmount;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String paymentStatus;
    private String paymentStatusText;

    private LocalDateTime createdAt;
    private String note;

    private boolean canCancel;
    private boolean canReview;
    private ReviewDTO review;

    private List<BookingSlotDetailDTO> slots;
}
